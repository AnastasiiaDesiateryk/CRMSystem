package org.example.crm.adapter.in.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.example.crm.application.dto.*;
import org.example.crm.application.port.in.AuthServicePort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Auth endpoints: register/login/refresh/logout.
 * Это inbound adapter. Он собирает данные из HTTP (UA/IP) и вызывает use-case (порт).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthServicePort authService;

    public AuthController(AuthServicePort authService) {
        this.authService = authService;
    }

    @Operation(summary = "Register user")
    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        String ua = httpRequest.getHeader("User-Agent");
        String ip = httpRequest.getRemoteAddr();
        return ResponseEntity.ok(authService.register(request, ua, ip));
    }

    @Operation(summary = "Login user")
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ua = httpRequest.getHeader("User-Agent");
        String ip = httpRequest.getRemoteAddr();
        return ResponseEntity.ok(authService.login(request, ua, ip));
    }

    @Operation(summary = "Refresh tokens")
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody RefreshRequest request, HttpServletRequest httpRequest) {
        String ua = httpRequest.getHeader("User-Agent");
        String ip = httpRequest.getRemoteAddr();
        return ResponseEntity.ok(authService.refresh(request, ua, ip));
    }

    @Operation(summary = "Logout (revoke refresh token)")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest request, HttpServletRequest httpRequest) {
        String ua = httpRequest.getHeader("User-Agent");
        String ip = httpRequest.getRemoteAddr();
        authService.logout(request, ua, ip);
        return ResponseEntity.noContent().build();
    }
}
