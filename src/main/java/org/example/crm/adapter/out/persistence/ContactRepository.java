package org.example.crm.adapter.out.persistence;

import org.example.crm.adapter.out.persistence.entity.ContactEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;



@Repository
public interface ContactRepository extends JpaRepository<ContactEntity, UUID> {
    boolean existsByOrganization_IdAndEmailIgnoreCase(UUID organizationId, String email);
}