package org.example.crm.adapter.out.persistence;

import org.example.crm.adapter.out.persistence.entity.RefreshTokenEntity;
import org.example.crm.adapter.out.persistence.jpa.RefreshTokenJpaRepository;
import org.example.crm.application.port.out.RefreshTokenStore;
import org.example.crm.domain.model.RefreshToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence adapter for refresh tokens.
 *
 * Role in hex architecture:
 * - Implements application port RefreshTokenStore
 * - Internally uses Spring Data JPA repository + entities
 * - Converts Entity <-> Domain model
 *
 * Related files:
 * - application.port.out.RefreshTokenStore (this implements it)
 * - adapter.out.persistence.jpa.RefreshTokenJpaRepository (DB operations)
 * - adapter.out.persistence.entity.RefreshTokenEntity (JPA mapping)
 * - domain.model.RefreshToken (business object used by application layer)
 */
@Component
public class RefreshTokenPersistenceAdapter implements RefreshTokenStore {

    private final RefreshTokenJpaRepository repo;

    public RefreshTokenPersistenceAdapter(RefreshTokenJpaRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<RefreshToken> findById(UUID id) {
        return repo.findById(id).map(RefreshTokenPersistenceAdapter::toDomain);
    }

    @Transactional
    @Override
    public RefreshToken save(RefreshToken token) {
        RefreshTokenEntity e = toEntity(token);
        RefreshTokenEntity saved = repo.save(e);
        return toDomain(saved);
    }

    @Transactional
    @Override
    public void revokeById(UUID id, OffsetDateTime revokedAt) {
        RefreshTokenEntity e = repo.findById(id).orElse(null);
        if (e == null) return;
        e.setRevokedAt(revokedAt);
        repo.save(e);
    }

    @Transactional
    @Override
    public void revokeAllForUser(UUID userId, OffsetDateTime revokedAt) {
        // Enterprise: делаем "soft revoke", а не delete — это аудит/безопасность.
        List<RefreshTokenEntity> tokens = repo.findAllByUserId(userId);
        for (RefreshTokenEntity e : tokens) {
            if (e.getRevokedAt() == null) e.setRevokedAt(revokedAt);
        }
        repo.saveAll(tokens);
    }

    @Transactional(readOnly = true)
    @Override
    public List<RefreshToken> findAllForUser(UUID userId) {
        return repo.findAllByUserId(userId).stream()
                .map(RefreshTokenPersistenceAdapter::toDomain)
                .toList();
    }

    // ===== mapping =====

    private static RefreshToken toDomain(RefreshTokenEntity e) {
        RefreshToken t = new RefreshToken();
        t.setId(e.getId());
        t.setUserId(e.getUserId());
        t.setTokenHash(e.getTokenHash());
        t.setCreatedAt(e.getCreatedAt());
        t.setExpiresAt(e.getExpiresAt());
        t.setRevokedAt(e.getRevokedAt());
        t.setRotatedFromId(e.getRotatedFromId());
        t.setUserAgent(e.getUserAgent());
        t.setIp(e.getIp());
        return t;
    }

    private static RefreshTokenEntity toEntity(RefreshToken t) {
        RefreshTokenEntity e = new RefreshTokenEntity();
        e.setId(t.getId());
        e.setUserId(t.getUserId());
        e.setTokenHash(t.getTokenHash());
        e.setCreatedAt(t.getCreatedAt());
        e.setExpiresAt(t.getExpiresAt());
        e.setRevokedAt(t.getRevokedAt());
        e.setRotatedFromId(t.getRotatedFromId());
        e.setUserAgent(t.getUserAgent());
        e.setIp(t.getIp());
        return e;
    }
}
