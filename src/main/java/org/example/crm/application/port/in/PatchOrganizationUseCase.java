package org.example.crm.application.port.in;

import org.example.crm.application.dto.OrganizationDetails;
import org.example.crm.application.dto.PatchOrganizationCommand;

import java.util.UUID;

public interface PatchOrganizationUseCase {
    OrganizationDetails patch(UUID id, PatchOrganizationCommand cmd, long expectedVersion);
}
