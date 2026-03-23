package org.example.crm.adapter.in.web.security;

import org.example.crm.application.port.out.AccessTokenIssuer;
import org.example.crm.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Adapter: application port -> technical JWT implementation.
 *
 * Why this exists:
 * - application layer depends only on AccessTokenIssuer (port)
 * - JwtService is Spring/Nimbus/keys technical stuff (adapter)
 *
 * Related files:
 * - AuthService (application.service): calls AccessTokenIssuer.issue()
 * - JwtService (adapter.in.web.security): actually creates signed JWT
 */
@Component
public class JwtAccessTokenIssuer implements AccessTokenIssuer {

    private static final Logger LOG = Logger.getLogger(JwtAccessTokenIssuer.class.getName());

    private final JwtService jwtService;

    public JwtAccessTokenIssuer(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public String issue(User user) {
        try {
            return jwtService.generateAccessToken(user);
        } catch (Exception e) {
            // This is infrastructure failure (keys missing, crypto broken, etc.)
            LOG.log(Level.SEVERE, "Failed to issue access token", e);
            throw new IllegalStateException("access_token_issue_failed");
        }
    }

    @Override
    public long accessTokenTtlSeconds() {
        return JwtService.ACCESS_TOKEN_SECONDS;
    }
}
