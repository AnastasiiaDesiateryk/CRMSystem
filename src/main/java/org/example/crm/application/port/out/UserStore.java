package org.example.crm.application.port.out;

import org.example.crm.domain.model.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Set;


public interface UserStore {
    Optional<User> findByEmail(String emailLower);
    Optional<User> findById(UUID id);
    List<User> findAll();
    User save(User user);
    void setHasAccess(UUID userId, boolean hasAccess);
    void setRoles(UUID userId, Set<String> roles);

}
