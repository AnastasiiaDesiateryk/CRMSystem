package org.example.crm.application.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.crm.adapter.out.persistence.entity.ContactEntity;
import org.example.crm.adapter.out.persistence.jpa.ContactJpaRepository;
import org.example.crm.adapter.out.persistence.jpa.OrganizationJpaRepository;
import org.example.crm.application.port.in.ContactUseCases.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ContactJpaService implements
        CreateContactUseCase,
        ListContactsUseCase,
        GetContactUseCase,
        PatchContactUseCase,
        DeleteContactUseCase {

    private final ContactJpaRepository contacts;
    private final OrganizationJpaRepository organizations;

    @PersistenceContext
    private EntityManager entityManager;

    public ContactJpaService(ContactJpaRepository contacts, OrganizationJpaRepository organizations) {
        this.contacts = contacts;
        this.organizations = organizations;
    }

    @Override
    public ContactDetails create(CreateContactCommand command) {
        var org = organizations.findById(command.organizationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "organization_not_found"));

        var e = new ContactEntity();
        e.setOrganization(org);
        e.setName(command.name());
        e.setRolePosition(command.rolePosition());
        e.setEmail(command.email());
        e.setPreferredLanguage(command.preferredLanguage());
        e.setNotes(command.notes());

        var saved = contacts.saveAndFlush(e); // важно: flush, чтобы версия/etag были актуальные
        entityManager.refresh(saved);         // чтобы подтянуть updated_at из DB trigger

        return toDetails(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactDetails> list(UUID organizationId) {
        if (!organizations.existsById(organizationId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "organization_not_found");
        }

        return contacts.findAllByOrganization_Id(organizationId)
                .stream()
                .map(this::toDetails)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ContactDetails get(UUID organizationId, UUID contactId) {
        var e = contacts.findByIdAndOrganization_Id(contactId, organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "contact_not_found"));

        return toDetails(e);
    }

    @Override
    public ContactDetails patch(UUID organizationId, UUID contactId, PatchContactCommand command, long expectedVersion) {
        var e = contacts.findByIdAndOrganization_Id(contactId, organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "contact_not_found"));

        if (e.getVersion() != expectedVersion) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "etag_mismatch");
        }

        if (command.name() != null) e.setName(command.name());
        if (command.rolePosition() != null) e.setRolePosition(command.rolePosition());
        if (command.email() != null) e.setEmail(command.email());
        if (command.preferredLanguage() != null) e.setPreferredLanguage(command.preferredLanguage());
        if (command.notes() != null) e.setNotes(command.notes());

        var saved = contacts.saveAndFlush(e); // flush => @Version станет +1 сразу
        entityManager.refresh(saved);         // подтянуть updated_at из DB trigger

        return toDetails(saved);
    }

    @Override
    public void delete(UUID organizationId, UUID contactId, long expectedVersion) {
        var e = contacts.findByIdAndOrganization_Id(contactId, organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "contact_not_found"));

        if (e.getVersion() != expectedVersion) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "etag_mismatch");
        }

        contacts.delete(e);
        contacts.flush();
    }

    private ContactDetails toDetails(ContactEntity e) {
        return new ContactDetails(
                e.getId(),
                e.getOrganization().getId(),
                e.getName(),
                e.getRolePosition(),
                e.getEmail(),
                e.getPreferredLanguage(),
                e.getNotes(),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                weakEtag(e.getVersion())
        );
    }

    private static String weakEtag(long version) {
        return "W/\"" + version + "\"";
    }
}