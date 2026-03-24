package org.example.crm.infrastructure.security;

import org.example.crm.application.port.out.TokenHashing;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * Infrastructure adapter:
 * SHA-256 hashing for tokens (hex encoded).
 */
@Component
public class Sha256TokenHashing implements TokenHashing {

    @Override
    public String sha256Hex(String value) {
        if (value == null) {
            throw new IllegalArgumentException("token_hash_input_null");
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("sha256_failed", e);
        }
    }
}
