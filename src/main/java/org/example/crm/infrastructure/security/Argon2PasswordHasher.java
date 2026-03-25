package org.example.crm.infrastructure.security;

import org.example.crm.application.port.out.PasswordHasher;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Infrastructure adapter for password hashing.
 *
 * Implements:
 * - PasswordHasher (application port)
 *
 * Uses:
 * - Spring Security Argon2 implementation
 *
 * Why infrastructure:
 * - cryptography is a technical concern
 * - application layer must not depend on Spring Security
 */
@Component
public class Argon2PasswordHasher implements PasswordHasher {

    private final Argon2PasswordEncoder encoder;

    public Argon2PasswordHasher() {
        // parametrs  SecurityConfig
        this.encoder = new Argon2PasswordEncoder(
                16,     // salt length
                32,     // hash length
                2,      // parallelism
                65536,  // memory (64MB)
                3       // iterations
        );
    }

    @Override
    public String hash(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("password_required");
        }
        return encoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String passwordHash) {
        if (rawPassword == null || passwordHash == null) {
            return false;
        }
        return encoder.matches(rawPassword, passwordHash);
    }
}
