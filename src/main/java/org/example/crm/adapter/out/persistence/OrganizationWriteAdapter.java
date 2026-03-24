package org.example.crm.adapter.out.persistence;

import org.example.crm.adapter.out.persistence.entity.OrganizationEntity;
import org.example.crm.adapter.out.persistence.jpa.OrganizationJpaRepository;
import org.example.crm.application.dto.CreateOrganizationCommand;
import org.example.crm.application.dto.OrganizationDetails;
import org.example.crm.application.dto.PatchOrganizationCommand;
import org.example.crm.application.port.out.OrganizationWritePort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.example.crm.adapter.out.persistence.entity.ContactEntity;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Component
public class OrganizationWriteAdapter implements OrganizationWritePort {

    private final OrganizationJpaRepository repo;

    public OrganizationWriteAdapter(OrganizationJpaRepository repo) {
        this.repo = repo;
    }

    @Transactional
    @Override
    public OrganizationDetails create(CreateOrganizationCommand cmd) {
        OrganizationEntity e = new OrganizationEntity();
        // лучше НЕ задавать id руками, дай БД/Entity решать (но можно и так, если везде так)
        e.setId(UUID.randomUUID());

        applyCreate(e, cmd);
        OrganizationEntity saved = repo.save(e);
        return toDetails(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<OrganizationDetails> findById(UUID id) {
        return repo.findById(id).map(this::toDetails);
    }

    @Transactional
    @Override
    public Optional<OrganizationDetails> patch(UUID id, PatchOrganizationCommand cmd, long expectedVersion) {
        OrganizationEntity e = repo.findById(id).orElse(null);
        if (e == null) return Optional.empty();

        // contract: version mismatch -> empty (НЕ exception)
        if (e.getVersion() != expectedVersion) return Optional.empty();

        applyPatch(e, cmd);

        // ВАЖНО: flush, чтобы Hibernate выполнил UPDATE и обновил @Version в entity ДО возврата
        OrganizationEntity saved = repo.saveAndFlush(e);

        return Optional.of(toDetails(saved));
    }

    @Transactional
    @Override
    public boolean delete(UUID id, long expectedVersion) {
        OrganizationEntity e = repo.findById(id).orElse(null);
        if (e == null) return false;

        // contract: version mismatch -> false (НЕ exception)
        if (e.getVersion() != expectedVersion) return false;

        repo.delete(e);
        repo.flush();
        return true;
    }

    private void applyCreate(OrganizationEntity e, CreateOrganizationCommand c) {
        e.setName(requireNonBlank(c.name(), "name_required"));
        e.setWebsite(c.website());
        e.setWebsiteStatus(c.websiteStatus());
        e.setLinkedinUrl(c.linkedinUrl());
        e.setCountryRegion(c.countryRegion());
        e.setEmail(c.email());
        e.setCategory(c.category());
        e.setStatus(c.status());
        e.setNotes(c.notes());
        e.setPreferredLanguage(c.preferredLanguage());
    }

    private void applyPatch(OrganizationEntity e, PatchOrganizationCommand c) {
        if (c.name() != null) e.setName(requireNonBlank(c.name(), "name_required"));
        if (c.website() != null) e.setWebsite(c.website());
        if (c.websiteStatus() != null) e.setWebsiteStatus(c.websiteStatus());
        if (c.linkedinUrl() != null) e.setLinkedinUrl(c.linkedinUrl());
        if (c.countryRegion() != null) e.setCountryRegion(c.countryRegion());
        if (c.email() != null) e.setEmail(c.email());
        if (c.category() != null) e.setCategory(c.category());
        if (c.status() != null) e.setStatus(c.status());
        if (c.notes() != null) e.setNotes(c.notes());
        if (c.preferredLanguage() != null) e.setPreferredLanguage(c.preferredLanguage());
    }

//    private OrganizationDetails toDetails(OrganizationEntity e) {
//        return new OrganizationDetails(
//                e.getId().toString(),
//                e.getName(),
//                e.getWebsite(),
//                e.getWebsiteStatus(),
//                e.getLinkedinUrl(),
//                e.getCountryRegion(),
//                e.getEmail(),
//                e.getCategory(),
//                e.getStatus(),
//                e.getNotes(),
//                e.getPreferredLanguage(),
//                e.getCreatedAt(),
//                e.getUpdatedAt(),
//                e.getVersion()
//        );
//    }


// ...

    private OrganizationDetails toDetails(OrganizationEntity e) {
        return new OrganizationDetails(
                e.getId().toString(),
                e.getName(),
                e.getWebsite(),
                e.getWebsiteStatus(),
                e.getLinkedinUrl(),
                e.getCountryRegion(),
                e.getEmail(),
                e.getCategory(),
                e.getStatus(),
                e.getNotes(),
                e.getPreferredLanguage(),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                e.getVersion(),
                toContactDetails(e.getContacts())
        );
    }

    private static List<OrganizationDetails.ContactDetails> toContactDetails(List<ContactEntity> contacts) {
        if (contacts == null) return List.of();
        return contacts.stream().map(c ->
                new OrganizationDetails.ContactDetails(
                        c.getId().toString(),
                        c.getOrganization().getId().toString(),
                        c.getName(),
                        c.getRolePosition(),
                        c.getEmail(),
                        c.getPreferredLanguage(),
                        c.getNotes(),
                        c.getCreatedAt(),
                        c.getUpdatedAt(),
                        c.getVersion()
                )
        ).toList();
    }

    private static String requireNonBlank(String v, String errorCode) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(errorCode);
        return v.trim();
    }
}