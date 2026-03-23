package org.example.crm.adapter.in.web.controller;

import org.example.crm.application.port.in.AdminUserServicePort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserServicePort admin;

    public AdminUserController(AdminUserServicePort admin) {
        this.admin = admin;
    }

    @PutMapping("/{id}/roles")
    public ResponseEntity<Void> setRoles(@PathVariable UUID id, @RequestBody SetRolesRequest req) {
        admin.setRoles(id, req.roles());
        return ResponseEntity.noContent().build();
    }

    public record SetRolesRequest(Set<String> roles) {}
}
