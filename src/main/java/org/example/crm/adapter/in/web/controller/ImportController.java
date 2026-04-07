package org.example.crm.adapter.in.web.controller;

import org.example.crm.adapter.in.web.dto.ImportOrganizationsResponse;
import org.example.crm.application.port.in.ImportOrganizationsFromExcelUseCase;
import org.example.crm.application.port.in.command.ImportOrganizationsFromExcelCommand;
import org.example.crm.application.port.in.result.ImportOrganizationsFromExcelResult;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/imports")
public class ImportController {

    private final ImportOrganizationsFromExcelUseCase importOrganizationsFromExcelUseCase;

    public ImportController(ImportOrganizationsFromExcelUseCase importOrganizationsFromExcelUseCase) {
        this.importOrganizationsFromExcelUseCase = importOrganizationsFromExcelUseCase;
    }

    @PostMapping(
            value = "/organizations/excel",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ImportOrganizationsResponse> importOrganizationsExcel(
            @RequestPart("file") MultipartFile file
    ) throws IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }

        ImportOrganizationsFromExcelCommand command = new ImportOrganizationsFromExcelCommand(
                file.getOriginalFilename() == null ? "unknown.xlsx" : file.getOriginalFilename(),
                file.getBytes()
        );

        ImportOrganizationsFromExcelResult result =
                importOrganizationsFromExcelUseCase.importFromExcel(command);

        ImportOrganizationsResponse response = new ImportOrganizationsResponse(
                result.organizationsCreated(),
                result.organizationsUpdated(),
                result.contactsCreated(),
                result.rowsSkipped(),
                result.warnings()
        );

        return ResponseEntity.ok(response);
    }
}