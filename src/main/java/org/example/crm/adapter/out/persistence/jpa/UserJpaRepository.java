package org.example.crm.adapter.out.persistence.jpa;

import org.example.crm.adapter.out.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Technical Spring Data JPA repository for {@link UserEntity}.
 *
 * This interface belongs to the outbound persistence adapter layer.
 * It is responsible for database access and persistence concerns only.
 * Business logic must not depend on this repository directly; instead,
 * application services should work through outbound ports, with the
 * persistence adapter delegating to this repository.
 *
 * Spring Data provides the implementation at runtime, including standard
 * CRUD operations inherited from {@link JpaRepository} and custom query
 * methods such as {@link #findByEmail(String)}.
 */
public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);
    List<UserEntity> findAllByOrderByCreatedAtDesc();
}
