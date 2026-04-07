package org.example.crm.application.port.out.model;

public record ImportedContactData(
        String organizationId,
        String name,
        String rolePosition,
        String email,
        String preferredLanguage,
        String notes
) {
}