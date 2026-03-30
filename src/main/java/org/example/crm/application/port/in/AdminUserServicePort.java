package org.example.crm.application.port.in;

import org.example.crm.application.dto.AdminUserDto;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface AdminUserServicePort {
    List<AdminUserDto> listUsers();
    AdminUserDto setHasAccess(UUID userId, boolean hasAccess);
    AdminUserDto setRoles(UUID userId, Set<String> roles);
//    void setRoles(UUID userId, Set<String> roles);
}
