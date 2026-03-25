package org.example.crm.adapter.in.web.dto;

public record CreateOrganizationRequest(
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
