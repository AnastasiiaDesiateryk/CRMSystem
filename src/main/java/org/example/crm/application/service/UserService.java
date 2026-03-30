package org.example.crm.application.service;

import org.example.crm.application.dto.UserDto;
import org.example.crm.application.port.in.UserServicePort;
import org.example.crm.application.port.out.CurrentUserIdPort;
import org.example.crm.application.port.out.LoadUserPort;
import org.example.crm.domain.model.User;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Application service (use-case implementation).
 *
 * Responsibilities:
 * - orchestrate "me" use-case
 * - do NOT access Spring Security directly
 * - do NOT access JPA repositories/entities directly
 *
 * Depends on:
 * - CurrentUserIdPort (who am I)  -> provided by web/security adapter
 * - LoadUserPort (load user)      -> provided by persistence adapter
 */
@Service
public class UserService implements UserServicePort {

    private static final Logger LOG = Logger.getLogger(UserService.class.getName());

    private final CurrentUserIdPort currentUserIdPort;
    private final LoadUserPort loadUserPort;

    public UserService(CurrentUserIdPort currentUserIdPort, LoadUserPort loadUserPort) {
        this.currentUserIdPort = currentUserIdPort;
        this.loadUserPort = loadUserPort;
    }

    @Override
    public UserDto me() {
        UUID userId = currentUserIdPort.currentUserId();

        User user = loadUserPort.findById(userId)
                .orElseThrow(() -> {
                    LOG.log(Level.WARNING, () -> "USER_ME: user_not_found id=" + userId);
                    return new IllegalStateException("user_not_found");
                });

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setRoles(user.getRoles());
        dto.setHasAccess(user.isHasAccess());
        return dto;
    }
}
