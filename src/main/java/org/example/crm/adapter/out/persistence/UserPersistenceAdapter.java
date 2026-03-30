package org.example.crm.adapter.out.persistence;

import org.example.crm.adapter.out.persistence.entity.UserEntity;
import org.example.crm.adapter.out.persistence.jpa.UserJpaRepository;
import org.example.crm.application.port.out.LoadUserPort;
import org.example.crm.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound persistence adapter.
 *
 * Responsibility:
 * - use JPA repository
 * - map persistence entities -> domain model
 *
 * Used by: application.service.UserService via LoadUserPort.
 */
@Component
public class UserPersistenceAdapter implements LoadUserPort {

    private final UserJpaRepository userJpaRepository;

    public UserPersistenceAdapter(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userJpaRepository.findById(id).map(UserPersistenceAdapter::toDomain);
    }

    private static User toDomain(UserEntity e) {
        User u = new User();
        u.setId(e.getId());
        u.setEmail(e.getEmail());
        u.setPasswordHash(e.getPasswordHash());
        u.setName(e.getName());
        u.setRoles(e.getRoles());
        u.setHasAccess(e.isHasAccess());
        u.setCreatedAt(e.getCreatedAt());
        u.setUpdatedAt(e.getUpdatedAt());
        return u;
    }
}
