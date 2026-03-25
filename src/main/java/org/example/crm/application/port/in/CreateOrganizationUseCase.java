package org.example.crm.application.port.in;

import org.example.crm.application.dto.CreateOrganizationCommand;
import org.example.crm.application.dto.OrganizationDetails;

public interface CreateOrganizationUseCase {
    OrganizationDetails create(CreateOrganizationCommand cmd);
}
