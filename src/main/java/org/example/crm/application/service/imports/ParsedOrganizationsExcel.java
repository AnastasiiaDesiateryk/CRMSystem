package org.example.crm.application.service.imports;

import java.util.List;

public record ParsedOrganizationsExcel(
        List<ImportedOrganizationRow> rows,
        int rowsSkipped,
        List<String> warnings
) {
}