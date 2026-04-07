package org.example.crm.application.service.imports;

public record ImportedOrganizationRow(
        int rowNumber,
        String company,
        String website,
        String linkedinUrl,
        String cantone,
        String emailOrganization,
        String categoryNew,
        String contactName,
        String contactEmail1,
        String contactEmail2
) {
}