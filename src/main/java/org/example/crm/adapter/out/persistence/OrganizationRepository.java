//package org.example.crm.adapter.out.persistence;
//
//import org.example.crm.domain.model.Organization;
//import org.springframework.data.jpa.repository.JpaRepository;
//import java.util.UUID;
//import org.springframework.stereotype.Repository;
//import org.example.crm.adapter.out.persistence.entity.OrganizationEntity;
//
//@Repository
//public interface OrganizationRepository extends JpaRepository<OrganizationEntity, UUID> {
//}

package org.example.crm.adapter.out.persistence;

import org.example.crm.adapter.out.persistence.entity.OrganizationEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for OrganizationEntity.
 *
 * Notes:
 * - We use @EntityGraph to eagerly fetch related contacts when reading organizations.
 *   This prevents LazyInitializationException in web mapping and avoids N+1 queries.
 * - Write operations still rely on standard JPA behavior and optimistic locking (@Version).
 */

@Repository
public interface OrganizationRepository extends JpaRepository<OrganizationEntity, UUID> {

    @Override
    @EntityGraph(attributePaths = {"contacts"})
    List<OrganizationEntity> findAll();

    @Override
    @EntityGraph(attributePaths = {"contacts"})
    Optional<OrganizationEntity> findById(UUID id);
}