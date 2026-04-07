package org.example.crm.adapter.out.persistence;

import org.example.crm.adapter.out.persistence.entity.OrganizationEntity;
import org.example.crm.application.port.out.OrganizationImportPersistencePort;
import org.example.crm.application.port.out.model.ImportedOrganizationData;
import org.example.crm.application.port.out.model.ImportedOrganizationSnapshot;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrganizationImportPersistenceAdapter implements OrganizationImportPersistencePort {

    private final OrganizationRepository organizationRepository;

    public OrganizationImportPersistenceAdapter(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @Override
    public Optional<ImportedOrganizationSnapshot> findByWebsite(String website) {
        return organizationRepository.findByWebsiteIgnoreCase(website)
                .map(this::toSnapshot);
    }

    @Override
    public Optional<ImportedOrganizationSnapshot> findByName(String name) {
        return organizationRepository.findByNameIgnoreCase(name)
                .map(this::toSnapshot);
    }

    @Override
    public String createOrganization(ImportedOrganizationData organization) {
        OrganizationEntity entity = new OrganizationEntity();
        apply(entity, organization);

        OrganizationEntity saved = organizationRepository.save(entity);
        return saved.getId().toString();
    }

    @Override
    public void updateOrganization(String organizationId, ImportedOrganizationData organization) {
        OrganizationEntity entity = organizationRepository.findById(java.util.UUID.fromString(organizationId))
                .orElseThrow(() -> new IllegalArgumentException("Organization not found: " + organizationId));

        apply(entity, organization);
        organizationRepository.save(entity);
    }

    private ImportedOrganizationSnapshot toSnapshot(OrganizationEntity entity) {
        return new ImportedOrganizationSnapshot(
                entity.getId().toString(),
                entity.getName(),
                entity.getWebsite(),
                entity.getWebsiteStatus(),
                entity.getLinkedinUrl(),
                entity.getCountryRegion(),
                entity.getEmail(),
                entity.getCategory(),
                entity.getStatus(),
                entity.getNotes(),
                entity.getPreferredLanguage()
        );
    }

    private void apply(OrganizationEntity entity, ImportedOrganizationData data) {
        entity.setName(data.name());
        entity.setWebsite(data.website());
        entity.setWebsiteStatus(data.websiteStatus());
        entity.setLinkedinUrl(data.linkedinUrl());
        entity.setCountryRegion(data.countryRegion());
        entity.setEmail(data.email());
        entity.setCategory(data.category());
        entity.setStatus(data.status());
        entity.setNotes(data.notes());
        entity.setPreferredLanguage(data.preferredLanguage());
    }
}