package org.example.crm.adapter.out.persistence;


import org.example.crm.domain.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.example.crm.adapter.out.persistence.entity.ContactEntity;

@Repository
public interface ContactRepository extends JpaRepository<ContactEntity, UUID> {
}