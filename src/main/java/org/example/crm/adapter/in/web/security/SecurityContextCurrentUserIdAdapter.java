package org.example.crm.adapter.in.web.security;

import org.example.crm.application.port.out.CurrentUserIdPort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Web/Security adapter: bridges Spring Security -> application port.
 *
 * Contract:
 * - Authentication.getName() should contain UUID string (subject)
 */
@Component
public class SecurityContextCurrentUserIdAdapter implements CurrentUserIdPort {

    private static final Logger LOG = Logger.getLogger(SecurityContextCurrentUserIdAdapter.class.getName());

    @Override
    public UUID currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            LOG.log(Level.FINE, "SECURITY: unauthenticated (no Authentication)");
            throw new IllegalStateException("unauthenticated");
        }

        String sub = auth.getName(); // standard location for subject
        if (sub == null || sub.isBlank() || "anonymousUser".equals(sub)) {
            LOG.log(Level.FINE, () -> "SECURITY: invalid_subject name=" + sub);
            throw new IllegalStateException("invalid_subject");
        }

        try {
            return UUID.fromString(sub.trim());
        } catch (Exception e) {
            LOG.log(Level.FINE, () -> "SECURITY: invalid_subject value=" + sub);
            throw new IllegalStateException("invalid_subject");
        }
    }
}
