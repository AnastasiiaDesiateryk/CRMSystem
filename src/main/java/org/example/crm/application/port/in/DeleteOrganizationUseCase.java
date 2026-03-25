package org.example.crm.application.port.in;

import java.util.UUID;

public interface DeleteOrganizationUseCase {
    void delete(UUID id, long expectedVersion);
}
