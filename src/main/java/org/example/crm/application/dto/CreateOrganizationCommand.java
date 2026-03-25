package org.example.crm.application.dto;

public record CreateOrganizationCommand(
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
) {}
