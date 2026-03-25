package org.example.crm.application.port.in;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Contacts application ports (single file).
 * Contains:
 * - 5 use case interfaces
 * - Commands
 * - Details DTO
 */
public final class ContactUseCases {
    private ContactUseCases() {}

    // ===== Application DTO =====

    public record ContactDetails(
            UUID id,
            UUID organizationId,
            String name,
            String rolePosition,
            String email,
            String preferredLanguage,
            String notes,
            Instant createdAt,
            Instant updatedAt,
            String etag
    ) {}

    // ===== Commands =====

    public record CreateContactCommand(
            UUID organizationId,
            String name,
            String rolePosition,
            String email,
            String preferredLanguage,
            String notes
    ) {}

    /**
     * PATCH command: null means "don't change this field".
     */
    public record PatchContactCommand(
            String name,
            String rolePosition,
            String email,
            String preferredLanguage,
            String notes
    ) {}

    // ===== Ports =====

    public interface CreateContactUseCase {
        ContactDetails create(CreateContactCommand command);
    }

    public interface ListContactsUseCase {
        List<ContactDetails> list(UUID organizationId);
    }

    public interface GetContactUseCase {
        ContactDetails get(UUID organizationId, UUID contactId);
    }

    public interface PatchContactUseCase {
        ContactDetails patch(UUID organizationId, UUID contactId, PatchContactCommand command, long expectedVersion);
    }

    public interface DeleteContactUseCase {
        void delete(UUID organizationId, UUID contactId, long expectedVersion);
    }
}