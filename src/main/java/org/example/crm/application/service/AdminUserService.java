package org.example.crm.application.service;

import org.example.crm.application.port.in.AdminUserServicePort;
import org.example.crm.application.port.out.UserStore;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
public class AdminUserService implements AdminUserServicePort {

    private final UserStore userStore;

    public AdminUserService(UserStore userStore) {
        this.userStore = userStore;
    }

    @Override
    public void setRoles(UUID userId, Set<String> roles) {
        userStore.setRoles(userId, roles);
    }
}
