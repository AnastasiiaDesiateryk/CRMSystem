package org.example.crm.application.port.out;

import org.example.crm.application.dto.CreateOrganizationCommand;
import org.example.crm.application.dto.OrganizationDetails;
import org.example.crm.application.dto.PatchOrganizationCommand;

import java.util.Optional;
import java.util.UUID;

public interface OrganizationWritePort {
    OrganizationDetails create(CreateOrganizationCommand cmd);
    Optional<OrganizationDetails> findById(UUID id);

    /**
     * Must enforce optimistic locking using expectedVersion.
     * Returns empty if not found or version mismatch.
     */
    Optional<OrganizationDetails> patch(UUID id, PatchOrganizationCommand cmd, long expectedVersion);

    /**
     * Must enforce optimistic locking using expectedVersion.
     * Returns false if not found or version mismatch.
     */
    boolean delete(UUID id, long expectedVersion);
}
