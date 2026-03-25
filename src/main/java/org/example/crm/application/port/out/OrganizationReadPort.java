package org.example.crm.application.port.out;

import org.example.crm.application.dto.OrganizationDetails;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port: read-only access to organizations for query use-cases.
 *
 * Implemented by:
 * - adapter.out.persistence.OrganizationReadAdapter (JPA)
 *
 * Called by:
 * - application.service.OrganizationQueryService
 */
public interface OrganizationReadPort {

    List<OrganizationDetails> findAll();

    Optional<OrganizationDetails> findById(UUID id);
}
