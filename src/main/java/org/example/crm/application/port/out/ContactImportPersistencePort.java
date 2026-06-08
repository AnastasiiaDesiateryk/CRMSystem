package org.example.crm.application.port.out;

import org.example.crm.application.port.out.model.ImportedContactData;

public interface ContactImportPersistencePort {

    boolean existsByOrganizationIdAndEmail(String organizationId, String email);

    void createContact(ImportedContactData contact);

    boolean updatePrimaryEmailFlags(String organizationId, String primaryEmail);
}