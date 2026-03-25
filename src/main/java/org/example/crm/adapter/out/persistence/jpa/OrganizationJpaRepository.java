package org.example.crm.adapter.out.persistence.jpa;

import org.example.crm.adapter.out.persistence.entity.OrganizationEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for organizations.
 *
 * Technical DB API:
 * - used ONLY by persistence adapters (adapter.out.persistence.*)
 * - NOT visible to application layer
 */
public interface OrganizationJpaRepository extends JpaRepository<OrganizationEntity, UUID> {
    // ничего не нужно, findAll/findById уже есть добавила
    @EntityGraph(attributePaths = {"contacts"})
    List<OrganizationEntity> findAll();

    @EntityGraph(attributePaths = {"contacts"})
    Optional<OrganizationEntity> findById(UUID id);
}
