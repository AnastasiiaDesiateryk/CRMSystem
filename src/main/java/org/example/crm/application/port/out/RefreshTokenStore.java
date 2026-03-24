package org.example.crm.application.port.out;



import org.example.crm.domain.model.RefreshToken;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenStore {

    Optional<RefreshToken> findById(UUID id);

    RefreshToken save(RefreshToken token);

    void revokeById(UUID id, OffsetDateTime revokedAt);

    void revokeAllForUser(UUID userId, OffsetDateTime revokedAt);

    List<RefreshToken> findAllForUser(UUID userId); // useful for admin/debug
}
