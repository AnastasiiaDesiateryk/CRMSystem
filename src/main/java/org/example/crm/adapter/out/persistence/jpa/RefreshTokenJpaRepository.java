package org.example.crm.adapter.out.persistence.jpa;

import org.example.crm.adapter.out.persistence.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository (technical DB API).
 *
 * IMPORTANT:
 * - used ONLY by persistence adapters
 * - does NOT expose domain models, only entities
 */
public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    List<RefreshTokenEntity> findAllByUserId(UUID userId);

    // если тебе нужно "удалить всё" физически:
    void deleteAllByUserId(UUID userId);
}
