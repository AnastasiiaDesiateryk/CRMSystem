package org.example.crm.application.port.in;

import org.example.crm.application.port.in.command.ImportOrganizationsFromExcelCommand;
import org.example.crm.application.port.in.result.ImportOrganizationsFromExcelResult;

public interface ImportOrganizationsFromExcelUseCase {
    ImportOrganizationsFromExcelResult importFromExcel(ImportOrganizationsFromExcelCommand command);
}