package org.example.crm.infrastructure.config;

/**
 * BootstrapAdminRunner
 *
 * Production-safe bootstrap mechanism for initial administrative access.
 *
 * Responsibilities:
 * - Ensures a deterministic, idempotent creation of a bootstrap admin user.
 * - Assigns ROLE_ADMIN explicitly via AdminUserServicePort.
 *
 * Architectural rationale:
 * - We intentionally do NOT bypass the application layer.
 * - User creation goes through AuthServicePort to guarantee:
 *      - password hashing
 *      - validation rules
 *      - domain invariants
 *      - refresh token policy
 *
 * - Role assignment goes through AdminUserServicePort to preserve
 *   authorization boundaries and avoid leaking persistence concerns
 *   into infrastructure bootstrap code.
 *
 * Idempotency:
 * - Safe to run on every startup.
 * - If user exists, it will not be recreated.
 * - ROLE_ADMIN is always enforced.
 *
 * Security considerations:
 * - Controlled entirely via environment variables.
 * - If BOOTSTRAP_ADMIN_EMAIL is not set, bootstrap is disabled.
 * - Intended for dev / first deploy scenarios.
 *
 * Important:
 * - This is NOT a migration.
 * - This is an application-level bootstrap.
 * - Must not be used as a substitute for real user provisioning in production.
 */
// NOTE:
// We deliberately avoid embedding role logic inside AuthService.
// Separation of concerns:
//  - AuthService: identity lifecycle
//  - AdminUserService: authorization policy mutation

import org.example.crm.application.dto.RegisterRequest;
import org.example.crm.application.port.in.AdminUserServicePort;
import org.example.crm.application.port.in.AuthServicePort;
import org.example.crm.application.port.out.UserStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class BootstrapAdminRunner {

    @Bean
    CommandLineRunner bootstrapAdmin(
            AuthServicePort auth,
            AdminUserServicePort admin,
            UserStore userStore,
            @Value("${BOOTSTRAP_ADMIN_EMAIL:}") String email,
            @Value("${BOOTSTRAP_ADMIN_PASSWORD:}") String password,
            @Value("${BOOTSTRAP_ADMIN_NAME:Admin}") String name
    ) {
        return args -> {
            if (email == null || email.isBlank()) return;

            try {
                // 1) ensure user exists
                var existing = userStore.findByEmail(email.toLowerCase());

                if (existing.isEmpty()) {
                    RegisterRequest req = new RegisterRequest();
                    req.setEmail(email);
                    req.setPassword(password);
                    req.setName(name);

                    auth.register(req, "bootstrap", "127.0.0.1");
                    System.out.println("BOOTSTRAP: created user " + email);
                } else {
                    System.out.println("BOOTSTRAP: user already exists " + email);
                }

                // 2) ensure admin role
                var user = userStore.findByEmail(email.toLowerCase())
                        .orElseThrow(() -> new IllegalStateException("bootstrap user not found after register: " + email));

                user.setHasAccess(true);
                userStore.save(user);

                admin.setRoles(user.getId(), Set.of("ROLE_ADMIN"));
                System.out.println("BOOTSTRAP: ensured ROLE_ADMIN for " + email);

            } catch (Exception e) {
                System.out.println("BOOTSTRAP: failed: " + e.getMessage());
            }
        };
    }
}
