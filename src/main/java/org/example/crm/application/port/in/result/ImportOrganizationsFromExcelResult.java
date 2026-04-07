package org.example.crm.application.port.in.result;

import java.util.List;

public record ImportOrganizationsFromExcelResult(
        int organizationsCreated,
        int organizationsUpdated,
        int contactsCreated,
        int rowsSkipped,
        List<String> warnings
) {
}