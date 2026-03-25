package org.example.crm.application.service;

import org.example.crm.application.dto.OrganizationDetails;
import org.example.crm.application.dto.OrganizationSearchQuery;
import org.example.crm.application.dto.PageResponse;
import org.example.crm.application.port.in.ListOrganizationsUseCase;
import org.example.crm.application.port.out.OrganizationReadPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrganizationQueryService implements ListOrganizationsUseCase {

    private final OrganizationReadPort readPort;

    public OrganizationQueryService(OrganizationReadPort readPort) {
        this.readPort = readPort;
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<OrganizationDetails> list(OrganizationSearchQuery query) {
        List<OrganizationDetails> all = readPort.findAll();

        // временно без фильтров и пагинации
        return new PageResponse<>(all, all.size(), 0, all.size());
    }
}
