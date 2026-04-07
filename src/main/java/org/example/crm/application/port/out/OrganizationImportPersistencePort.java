package org.example.crm.application.port.out;

import org.example.crm.application.port.out.model.ImportedOrganizationData;
import org.example.crm.application.port.out.model.ImportedOrganizationSnapshot;

import java.util.Optional;

public interface OrganizationImportPersistencePort {

    Optional<ImportedOrganizationSnapshot> findByWebsite(String website);

    Optional<ImportedOrganizationSnapshot> findByName(String name);

    String createOrganization(ImportedOrganizationData organization);

    void updateOrganization(String organizationId, ImportedOrganizationData organization);
}