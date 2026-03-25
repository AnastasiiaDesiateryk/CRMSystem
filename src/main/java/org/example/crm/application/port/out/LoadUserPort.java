package org.example.crm.application.port.out;

import org.example.crm.domain.model.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port: loads user from persistence.
 * Implemented by: adapter.out.persistence (JPA).
 */
public interface LoadUserPort {
    Optional<User> findById(UUID id);
}
