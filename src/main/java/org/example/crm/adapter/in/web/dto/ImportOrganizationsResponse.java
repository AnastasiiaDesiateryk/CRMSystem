package org.example.crm.adapter.in.web.dto;

import java.util.List;

public record ImportOrganizationsResponse(
        int organizationsCreated,
        int organizationsUpdated,
        int contactsCreated,
        int rowsSkipped,
        List<String> warnings
) {
}