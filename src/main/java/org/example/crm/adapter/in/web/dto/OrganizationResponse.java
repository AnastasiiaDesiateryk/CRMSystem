package org.example.crm.adapter.in.web.dto;

import java.util.List;

public record OrganizationResponse(
        String id,
        String name,
        String website,
        String websiteStatus,
        String linkedinUrl,
        String countryRegion,
        String email,
        String category,
        String status,
        String importance,
        String notes,
        String preferredLanguage,
        String createdAt,
        String updatedAt,
        String etag,
        List<ContactResponse> contacts
) {
    public record ContactResponse(
            String id,
            String organizationId,
            String name,
            String rolePosition,
            String email,
            String preferredLanguage,
            String notes,
            Boolean isPrimaryEmail,
            String createdAt,
            String updatedAt,
            String etag
    ) {}
}