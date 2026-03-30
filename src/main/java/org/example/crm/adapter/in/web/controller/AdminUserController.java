package org.example.crm.adapter.in.web.controller;

import org.example.crm.adapter.in.web.controller.request.UpdateUserAccessRequest;
import org.example.crm.adapter.in.web.controller.request.UpdateUserRolesRequest;
import org.example.crm.application.dto.AdminUserDto;
import org.example.crm.application.port.in.AdminUserServicePort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
//import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserServicePort admin;

    public AdminUserController(AdminUserServicePort admin) {
        this.admin = admin;
    }

    @GetMapping
    public ResponseEntity<List<AdminUserDto>> listUsers() {
        return ResponseEntity.ok(admin.listUsers());
    }

    @PatchMapping("/{id}/access")
    public ResponseEntity<AdminUserDto> setAccess(
            @PathVariable UUID id,
            @RequestBody UpdateUserAccessRequest req
    ) {
        return ResponseEntity.ok(admin.setHasAccess(id, req.hasAccess()));
    }

    @PutMapping("/{id}/roles")
    public ResponseEntity<AdminUserDto> setRoles(
            @PathVariable UUID id,
            @RequestBody UpdateUserRolesRequest req
    ) {
        return ResponseEntity.ok(admin.setRoles(id, req.roles()));
    }

//    public record SetRolesRequest(Set<String> roles) {}
}
