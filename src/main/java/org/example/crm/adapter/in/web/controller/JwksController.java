package org.example.crm.adapter.in.web.controller;

import org.example.crm.adapter.in.web.security.JwtService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Public JWKS endpoint.
 *
 * Needed for:
 * - external clients/services to validate your JWT signature using public key
 *
 * NOTE:
 * - pure inbound web adapter
 * - delegates to JwtService (technical security service)
 */
@RestController
public class JwksController {

    private final JwtService jwtService;

    public JwksController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwks() {
        return jwtService.getJwks();
    }
}
