package org.example.crm.application.port.in;

import org.example.crm.application.dto.OrganizationDetails;
import org.example.crm.application.dto.OrganizationSearchQuery;
import org.example.crm.application.dto.PageResponse;

/**
 * Use-case: получить список организаций с фильтрами/пагинацией.
 */
public interface ListOrganizationsUseCase {
    PageResponse<OrganizationDetails> list(OrganizationSearchQuery query);
}
