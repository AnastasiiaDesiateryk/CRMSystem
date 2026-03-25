package org.example.crm.adapter.out.persistence.jpa;

import org.example.crm.adapter.out.persistence.entity.ContactEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContactJpaRepository extends JpaRepository<ContactEntity, UUID> {

    List<ContactEntity> findAllByOrganization_Id(UUID organizationId);

    Optional<ContactEntity> findByIdAndOrganization_Id(UUID contactId, UUID organizationId);

    long countByOrganization_Id(UUID organizationId);
}