package org.example.crm.adapter.in.web.dto;

import java.util.UUID;

/**
 * Contacts web DTOs.
 */
public final class ContactDtos {
    private ContactDtos() {}

    public record CreateContactRequest(
            String name,
            String rolePosition,
            String email,
            String preferredLanguage,
            String notes
    ) {}

    public record PatchContactRequest(
            String name,
            String rolePosition,
            String email,
            String preferredLanguage,
            String notes
    ) {}

    public record ContactResponse(
            UUID id,
            UUID organizationId,
            String name,
            String rolePosition,
            String email,
            String preferredLanguage,
            String notes,
            String createdAt,
            String updatedAt,
            String etag
    ) {}
}