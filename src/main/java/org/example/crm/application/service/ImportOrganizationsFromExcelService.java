package org.example.crm.application.service;

import org.example.crm.application.port.in.ImportOrganizationsFromExcelUseCase;
import org.example.crm.application.port.in.command.ImportOrganizationsFromExcelCommand;
import org.example.crm.application.port.in.result.ImportOrganizationsFromExcelResult;
import org.example.crm.application.port.out.ContactImportPersistencePort;
import org.example.crm.application.port.out.OrganizationImportPersistencePort;
import org.example.crm.application.port.out.OrganizationsExcelParserPort;
import org.example.crm.application.port.out.model.ImportedContactData;
import org.example.crm.application.port.out.model.ImportedOrganizationData;
import org.example.crm.application.port.out.model.ImportedOrganizationSnapshot;
import org.example.crm.application.service.imports.ImportedOrganizationRow;
import org.example.crm.application.service.imports.ParsedOrganizationsExcel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ImportOrganizationsFromExcelService implements ImportOrganizationsFromExcelUseCase {

    private static final String DEFAULT_ORGANIZATION_STATUS = "active";
    private static final String DEFAULT_PRIMARY_CONTACT_NAME = "Unknown Contact";
    private static final String DEFAULT_SECONDARY_CONTACT_NAME = "Secondary Contact";

    private final OrganizationsExcelParserPort organizationsExcelParserPort;
    private final OrganizationImportPersistencePort organizationImportPersistencePort;
    private final ContactImportPersistencePort contactImportPersistencePort;

    public ImportOrganizationsFromExcelService(
            OrganizationsExcelParserPort organizationsExcelParserPort,
            OrganizationImportPersistencePort organizationImportPersistencePort,
            ContactImportPersistencePort contactImportPersistencePort
    ) {
        this.organizationsExcelParserPort = organizationsExcelParserPort;
        this.organizationImportPersistencePort = organizationImportPersistencePort;
        this.contactImportPersistencePort = contactImportPersistencePort;
    }

    @Override
    @Transactional
    public ImportOrganizationsFromExcelResult importFromExcel(ImportOrganizationsFromExcelCommand command) {
        ParsedOrganizationsExcel parsed = organizationsExcelParserPort.parse(command.fileContent());

        int organizationsCreated = 0;
        int organizationsUpdated = 0;
        int contactsCreated = 0;
        int rowsSkipped = parsed.rowsSkipped();

        List<String> warnings = new ArrayList<>(parsed.warnings());

        for (ImportedOrganizationRow row : parsed.rows()) {
            ImportedOrganizationData patch = mapOrganizationPatch(row);

            Optional<ImportedOrganizationSnapshot> existingOpt = findExistingOrganization(patch);

            String organizationId;
            if (existingOpt.isPresent()) {
                ImportedOrganizationSnapshot existing = existingOpt.get();
                ImportedOrganizationData merged = mergeOrganization(existing, patch);

                organizationImportPersistencePort.updateOrganization(existing.id(), merged);
                organizationId = existing.id();
                organizationsUpdated++;
            } else {
                ImportedOrganizationData createData = toCreateOrganizationData(patch);
                organizationId = organizationImportPersistencePort.createOrganization(createData);
                organizationsCreated++;
            }

            contactsCreated += createContactIfPresent(
                    organizationId,
                    row.contactEmail1(),
                    resolvePrimaryContactName(row.contactName()),
                    warnings,
                    row.rowNumber()
            );

            contactsCreated += createContactIfPresent(
                    organizationId,
                    row.contactEmail2(),
                    resolveSecondaryContactName(row.contactName()),
                    warnings,
                    row.rowNumber()
            );
        }

        return new ImportOrganizationsFromExcelResult(
                organizationsCreated,
                organizationsUpdated,
                contactsCreated,
                rowsSkipped,
                warnings
        );
    }

    private ImportedOrganizationData mapOrganizationPatch(ImportedOrganizationRow row) {
        return new ImportedOrganizationData(
                requiredTrimmed(row.company()),
                blankToNull(row.website()),
                null,
                blankToNull(row.linkedinUrl()),
                blankToNull(row.cantone()),
                blankToNull(row.emailOrganization()),
                blankToNull(row.categoryNew()),
                null,
                null,
                null
        );
    }

    private ImportedOrganizationData toCreateOrganizationData(ImportedOrganizationData patch) {
        return new ImportedOrganizationData(
                patch.name(),
                patch.website(),
                null,
                patch.linkedinUrl(),
                patch.countryRegion(),
                patch.email(),
                patch.category(),
                DEFAULT_ORGANIZATION_STATUS,
                null,
                null
        );
    }

    private ImportedOrganizationData mergeOrganization(
            ImportedOrganizationSnapshot existing,
            ImportedOrganizationData patch
    ) {
        return new ImportedOrganizationData(
                preferNonBlank(patch.name(), existing.name()),
                preferNonBlank(patch.website(), existing.website()),
                preferNonBlank(patch.websiteStatus(), existing.websiteStatus()),
                preferNonBlank(patch.linkedinUrl(), existing.linkedinUrl()),
                preferNonBlank(patch.countryRegion(), existing.countryRegion()),
                preferNonBlank(patch.email(), existing.email()),
                preferNonBlank(patch.category(), existing.category()),
                preferNonBlank(existing.status(), DEFAULT_ORGANIZATION_STATUS),
                preferNonBlank(existing.notes(), null),
                preferNonBlank(existing.preferredLanguage(), null)
        );
    }

    private Optional<ImportedOrganizationSnapshot> findExistingOrganization(ImportedOrganizationData patch) {
        if (hasText(patch.website())) {
            return organizationImportPersistencePort.findByWebsite(patch.website());
        }
        return organizationImportPersistencePort.findByName(patch.name());
    }

    private int createContactIfPresent(
            String organizationId,
            String rawEmail,
            String resolvedName,
            List<String> warnings,
            int rowNumber
    ) {
        String email = normalizeEmail(rawEmail);
        if (email == null) {
            return 0;
        }

        if (contactImportPersistencePort.existsByOrganizationIdAndEmail(organizationId, email)) {
            warnings.add(
                    "Row " + rowNumber + ": contact with email '" + email + "' already exists for organization " + organizationId + ", skipped"
            );
            return 0;
        }

        ImportedContactData contact = new ImportedContactData(
                organizationId,
                resolvedName,
                null,
                email,
                null,
                null
        );

        contactImportPersistencePort.createContact(contact);
        return 1;
    }

    private String resolvePrimaryContactName(String rawName) {
        String value = blankToNull(rawName);
        return value != null ? value : DEFAULT_PRIMARY_CONTACT_NAME;
    }

    private String resolveSecondaryContactName(String rawName) {
        String value = blankToNull(rawName);
        return value != null ? value : DEFAULT_SECONDARY_CONTACT_NAME;
    }

    private String requiredTrimmed(String value) {
        String trimmed = blankToNull(value);
        if (trimmed == null) {
            throw new IllegalArgumentException("Organization name must not be blank");
        }
        return trimmed;
    }

    private String normalizeEmail(String value) {
        String trimmed = blankToNull(value);
        if (trimmed == null) {
            return null;
        }
        return trimmed.toLowerCase();
    }

    private String preferNonBlank(String incoming, String existing) {
        String trimmedIncoming = blankToNull(incoming);
        return trimmedIncoming != null ? trimmedIncoming : existing;
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}