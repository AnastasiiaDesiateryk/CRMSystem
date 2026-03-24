package org.example.crm.application.port.out;

import java.util.UUID;

/**
 * Outbound port: provides authenticated user id (subject).
 * Implemented by: adapter.in.web.security (reads SecurityContext / JWT).
 */
public interface CurrentUserIdPort {
    UUID currentUserId();
}
