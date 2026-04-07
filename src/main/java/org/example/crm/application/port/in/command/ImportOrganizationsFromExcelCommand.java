package org.example.crm.application.port.in.command;

import java.util.Objects;

public record ImportOrganizationsFromExcelCommand(
        String fileName,
        byte[] fileContent
) {
    public ImportOrganizationsFromExcelCommand {
        Objects.requireNonNull(fileName, "fileName must not be null");
        Objects.requireNonNull(fileContent, "fileContent must not be null");
    }
}