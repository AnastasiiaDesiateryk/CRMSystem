package org.example.crm.application.service;

import org.example.crm.application.port.in.AdminUserServicePort;
import org.example.crm.application.dto.ConflictException;
import org.example.crm.application.dto.NotFoundException;
import org.example.crm.application.port.out.CurrentUserIdPort;
import org.example.crm.application.port.out.UserStore;
import org.example.crm.application.dto.AdminUserDto;
import org.example.crm.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class AdminUserService implements AdminUserServicePort {
    private static final Set<String> ALLOWED_ROLES = Set.of("ROLE_USER", "ROLE_ADMIN");

    private final UserStore userStore;
    private final CurrentUserIdPort currentUserIdPort;

    public AdminUserService(UserStore userStore, CurrentUserIdPort currentUserIdPort) {
        this.userStore = userStore;
        this.currentUserIdPort = currentUserIdPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserDto> listUsers() {
        return userStore.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public AdminUserDto setHasAccess(UUID userId, boolean hasAccess) {
        User target = requireUser(userId);
        UUID currentUserId = currentUserIdOrNull();

        if (currentUserId != null && currentUserId.equals(target.getId()) && !hasAccess) {
            throw new ConflictException("cannot_block_self");
        }

        userStore.setHasAccess(target.getId(), hasAccess);
        return toDto(requireUser(target.getId()));
    }

    @Override
    @Transactional
    public AdminUserDto setRoles(UUID userId, Set<String> roles) {
        User target = requireUser(userId);
        UUID currentUserId = currentUserIdOrNull();

        Set<String> normalizedRoles = normalizeRoles(roles);

        if (currentUserId != null && currentUserId.equals(target.getId()) && !normalizedRoles.contains("ROLE_ADMIN")) {
            throw new ConflictException("cannot_remove_own_admin_role");
        }

        userStore.setRoles(target.getId(), normalizedRoles);
        return toDto(requireUser(target.getId()));
    }

    private User requireUser(UUID userId) {
        return userStore.findById(userId)
                .orElseThrow(() -> new NotFoundException("user_not_found"));
    }

    private UUID currentUserIdOrNull() {
        try {
            return currentUserIdPort.currentUserId();
        } catch (IllegalStateException ex) {
            return null;
        }
    }

    private Set<String> normalizeRoles(Set<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return Set.of("ROLE_USER");
        }

        Set<String> normalized = new LinkedHashSet<>();
        for (String role : roles) {
            if (role == null || role.isBlank()) {
                continue;
            }

            String value = role.trim().toUpperCase();
            if (!ALLOWED_ROLES.contains(value)) {
                throw new IllegalArgumentException("unsupported_role");
            }
            normalized.add(value);
        }

        if (normalized.isEmpty()) {
            normalized.add("ROLE_USER");
        }
        if (normalized.contains("ROLE_ADMIN")) {
            normalized.add("ROLE_USER");
        }

        return normalized;
    }

    private AdminUserDto toDto(User user) {
        AdminUserDto dto = new AdminUserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setRoles(user.getRoles());
        dto.setHasAccess(user.isHasAccess());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}
