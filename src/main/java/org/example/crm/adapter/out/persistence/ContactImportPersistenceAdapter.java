package org.example.crm.adapter.out.persistence;

import org.example.crm.adapter.out.persistence.entity.ContactEntity;
import org.example.crm.adapter.out.persistence.entity.OrganizationEntity;
import org.example.crm.application.port.out.ContactImportPersistencePort;
import org.example.crm.application.port.out.model.ImportedContactData;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ContactImportPersistenceAdapter implements ContactImportPersistencePort {

    private final ContactRepository contactRepository;
    private final OrganizationRepository organizationRepository;

    public ContactImportPersistenceAdapter(
            ContactRepository contactRepository,
            OrganizationRepository organizationRepository
    ) {
        this.contactRepository = contactRepository;
        this.organizationRepository = organizationRepository;
    }

    @Override
    public boolean existsByOrganizationIdAndEmail(String organizationId, String email) {
        return contactRepository.existsByOrganization_IdAndEmailIgnoreCase(
                UUID.fromString(organizationId),
                email
        );
    }

    @Override
    public void createContact(ImportedContactData contact) {
        OrganizationEntity organization = organizationRepository.findById(UUID.fromString(contact.organizationId()))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Organization not found for contact import: " + contact.organizationId()
                ));

        ContactEntity entity = new ContactEntity();
        entity.setOrganization(organization);
        entity.setName(contact.name());
        entity.setRolePosition(contact.rolePosition());
        entity.setEmail(contact.email());
        entity.setPreferredLanguage(contact.preferredLanguage());
        entity.setNotes(contact.notes());

        contactRepository.save(entity);
    }
}