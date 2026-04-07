package org.example.crm.application.port.out;

import org.example.crm.application.service.imports.ParsedOrganizationsExcel;

public interface OrganizationsExcelParserPort {
    ParsedOrganizationsExcel parse(byte[] fileContent);
}