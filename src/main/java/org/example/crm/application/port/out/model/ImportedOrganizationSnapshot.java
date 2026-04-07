package org.example.crm.application.port.out.model;

public record ImportedOrganizationSnapshot(
        String id,
        String name,
        String website,
        String websiteStatus,
        String linkedinUrl,
        String countryRegion,
        String email,
        String category,
        String status,
        String notes,
        String preferredLanguage
) {
}