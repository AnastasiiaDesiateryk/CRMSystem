package org.example.crm.infrastructure.security;

import org.example.crm.application.port.out.RefreshTokenGenerator;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Infrastructure adapter:
 * generates cryptographically secure random refresh tokens.
 */
@Component
public class SecureRandomRefreshTokenGenerator implements RefreshTokenGenerator {

    private static final int RAW_TOKEN_BYTES = 32; // 256-bit
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generateRaw() {
        byte[] bytes = new byte[RAW_TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}
