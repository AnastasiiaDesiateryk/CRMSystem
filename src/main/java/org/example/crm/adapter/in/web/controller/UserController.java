package org.example.crm.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.example.crm.application.dto.UserDto;
import org.example.crm.application.port.in.UserServicePort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoint для "кто я" (текущий пользователь).
 * Требует аутентификации (JWT фильтр должен поставить principal в SecurityContext).
 */
@RestController
@RequestMapping("/api")
public class UserController {

    private final UserServicePort userService;

    public UserController(UserServicePort userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get current authenticated user")
    @GetMapping("/me")
    public ResponseEntity<UserDto> me() {
        return ResponseEntity.ok(userService.me());
    }
}
