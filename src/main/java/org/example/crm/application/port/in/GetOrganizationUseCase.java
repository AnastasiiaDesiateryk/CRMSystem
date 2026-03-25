package org.example.crm.application.port.in;

import org.example.crm.application.dto.OrganizationDetails;

import java.util.UUID;

public interface GetOrganizationUseCase {
    OrganizationDetails get(UUID id);
}
