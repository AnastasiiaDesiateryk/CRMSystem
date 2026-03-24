package org.example.crm.application.port.out;

import org.example.crm.domain.model.User;

import java.util.Optional;
import java.util.UUID;
import java.util.Set;


public interface UserStore {
    Optional<User> findByEmail(String emailLower);
    Optional<User> findById(UUID id);
    User save(User user);
    void setRoles(UUID userId, Set<String> roles);

}
