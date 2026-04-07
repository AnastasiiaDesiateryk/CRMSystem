package org.example.crm.adapter.out.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.example.crm.application.port.out.OrganizationsExcelParserPort;
import org.example.crm.application.service.imports.ImportedOrganizationRow;
import org.example.crm.application.service.imports.ParsedOrganizationsExcel;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class ApachePoiOrganizationsExcelParser implements OrganizationsExcelParserPort {

    private static final String SHEET_NAME = "Database";

    private static final String COL_COMPANY = "Company";
    private static final String COL_WEBSITE = "Website";
    private static final String COL_LINKEDIN = "LinkedIn";
    private static final String COL_CANTONE = "Cantone";
    private static final String COL_EMAIL_ORGANIZATION = "Email organization";
    private static final String COL_CATEGORY_NEW = "Category NEW";
    private static final String COL_CONTACT_NAME = "Name personal contact";
    private static final String COL_CONTACT_EMAIL_1 = "Email personal contact (1)";
    private static final String COL_CONTACT_EMAIL_2 = "Email personal contact (2)";

    private static final List<String> REQUIRED_HEADERS = List.of(
            COL_COMPANY,
            COL_WEBSITE,
            COL_LINKEDIN,
            COL_CANTONE,
            COL_EMAIL_ORGANIZATION,
            COL_CATEGORY_NEW,
            COL_CONTACT_NAME,
            COL_CONTACT_EMAIL_1,
            COL_CONTACT_EMAIL_2
    );

    @Override
    public ParsedOrganizationsExcel parse(byte[] fileContent) {
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(fileContent))) {
            Sheet sheet = workbook.getSheet(SHEET_NAME);
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet '" + SHEET_NAME + "' not found");
            }

            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) {
                throw new IllegalArgumentException("Header row is missing in sheet '" + SHEET_NAME + "'");
            }

            DataFormatter formatter = new DataFormatter();
            Map<String, Integer> headerIndexes = resolveHeaderIndexes(headerRow, formatter);

            List<ImportedOrganizationRow> rows = new ArrayList<>();
            List<String> warnings = new ArrayList<>();
            int rowsSkipped = 0;

            for (int rowIndex = headerRow.getRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);

                if (row == null || isRelevantRowEmpty(row, headerIndexes, formatter)) {
                    rowsSkipped++;
                    continue;
                }

                String company = getCellValue(row, headerIndexes, COL_COMPANY, formatter);
                if (company.isBlank()) {
                    rowsSkipped++;
                    warnings.add("Row " + (rowIndex + 1) + " skipped: Company is blank");
                    continue;
                }

                rows.add(new ImportedOrganizationRow(
                        rowIndex + 1,
                        company,
                        getCellValue(row, headerIndexes, COL_WEBSITE, formatter),
                        getCellValue(row, headerIndexes, COL_LINKEDIN, formatter),
                        getCellValue(row, headerIndexes, COL_CANTONE, formatter),
                        getCellValue(row, headerIndexes, COL_EMAIL_ORGANIZATION, formatter),
                        getCellValue(row, headerIndexes, COL_CATEGORY_NEW, formatter),
                        getCellValue(row, headerIndexes, COL_CONTACT_NAME, formatter),
                        getCellValue(row, headerIndexes, COL_CONTACT_EMAIL_1, formatter),
                        getCellValue(row, headerIndexes, COL_CONTACT_EMAIL_2, formatter)
                ));
            }

            return new ParsedOrganizationsExcel(rows, rowsSkipped, warnings);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse Excel file", e);
        }
    }

    private Map<String, Integer> resolveHeaderIndexes(Row headerRow, DataFormatter formatter) {
        Map<String, Integer> normalizedHeaderIndexes = new HashMap<>();

        for (Cell cell : headerRow) {
            String rawHeader = formatter.formatCellValue(cell);
            String normalizedHeader = normalizeHeader(rawHeader);

            if (!normalizedHeader.isBlank()) {
                normalizedHeaderIndexes.put(normalizedHeader, cell.getColumnIndex());
            }
        }

        List<String> missingHeaders = new ArrayList<>();
        Map<String, Integer> resolved = new HashMap<>();

        for (String requiredHeader : REQUIRED_HEADERS) {
            String normalizedRequired = normalizeHeader(requiredHeader);
            Integer index = normalizedHeaderIndexes.get(normalizedRequired);

            if (index == null) {
                missingHeaders.add(requiredHeader);
            } else {
                resolved.put(requiredHeader, index);
            }
        }

        if (!missingHeaders.isEmpty()) {
            throw new IllegalArgumentException(
                    "Missing required headers in sheet '" + SHEET_NAME + "': " + String.join(", ", missingHeaders)
            );
        }

        return resolved;
    }

    private boolean isRelevantRowEmpty(Row row, Map<String, Integer> headerIndexes, DataFormatter formatter) {
        for (String header : REQUIRED_HEADERS) {
            String value = getCellValue(row, headerIndexes, header, formatter);
            if (!value.isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String getCellValue(
            Row row,
            Map<String, Integer> headerIndexes,
            String header,
            DataFormatter formatter
    ) {
        Integer index = headerIndexes.get(header);
        if (index == null) {
            return "";
        }

        Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return "";
        }

        return formatter.formatCellValue(cell).trim();
    }

    private String normalizeHeader(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}