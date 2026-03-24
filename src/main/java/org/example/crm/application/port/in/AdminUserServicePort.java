package org.example.crm.application.port.in;

import java.util.Set;
import java.util.UUID;

public interface AdminUserServicePort {
    void setRoles(UUID userId, Set<String> roles);
}
