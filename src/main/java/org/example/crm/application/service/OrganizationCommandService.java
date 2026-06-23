package org.example.crm.application.service;

import org.example.crm.application.dto.*;
import org.example.crm.application.port.in.*;
import org.example.crm.application.port.out.OrganizationWritePort;
import org.example.crm.application.port.out.OrganizationReadPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OrganizationCommandService implements
        CreateOrganizationUseCase,
        GetOrganizationUseCase,
        PatchOrganizationUseCase,
        DeleteOrganizationUseCase {

    private final OrganizationWritePort writePort;
    private final OrganizationReadPort readPort;

    public OrganizationCommandService(OrganizationWritePort writePort, OrganizationReadPort readPort) {
        this.writePort = writePort;
        this.readPort = readPort;
    }

    @Transactional
    @Override
    public OrganizationDetails create(CreateOrganizationCommand cmd) {

        return writePort.create(normalize(cmd));
    }

    @Transactional(readOnly = true)
    @Override
    public OrganizationDetails get(UUID id) {
        return readPort.findById(id)
                .orElseThrow(() -> new NotFoundException("organization_not_found"));
    }

    @Transactional
    @Override
    public OrganizationDetails patch(UUID id, PatchOrganizationCommand cmd, long expectedVersion) {

        var normalized = normalize(cmd);

        return writePort.patch(id, normalized, expectedVersion)
                .orElseGet(() -> {
                    if (readPort.findById(id).isEmpty()) {
                        throw new NotFoundException("organization_not_found");
                    }
                    throw new PreconditionFailedException("etag_mismatch");
                });
    }

    @Transactional
    @Override
    public void delete(UUID id, long expectedVersion) {
        boolean deleted = writePort.delete(id, expectedVersion);
        if (deleted) return;

        // delete failed -> either not found OR etag mismatch
        if (readPort.findById(id).isEmpty()) {
            throw new NotFoundException("organization_not_found");
        }
        throw new PreconditionFailedException("etag_mismatch");
    }

    private CreateOrganizationCommand normalize(CreateOrganizationCommand c) {
        return new CreateOrganizationCommand(
                trimToNull(c.name()),
                trimToNull(c.website()),
                trimToNull(c.websiteStatus()),
                trimToNull(c.linkedinUrl()),
                trimToNull(c.countryRegion()),
                trimToNull(c.email()),
                trimToNull(c.category()),
                trimToNull(c.status()),
                normalizeImportance(c.importance()),
                trimToNull(c.notes()),
                trimToNull(c.preferredLanguage())
        );
    }

    private static String normalizeImportance(String v) {
        String value = trimToNull(v);
        if (value == null) return null;

        String normalized = value.toLowerCase();
        if (!normalized.equals("high") && !normalized.equals("medium") && !normalized.equals("low")) {
            throw new IllegalArgumentException("invalid importance");
        }
        return normalized;
    }

    private PatchOrganizationCommand normalize(PatchOrganizationCommand c) {
        return new PatchOrganizationCommand(
                trimToNull(c.name()),
                trimToNull(c.website()),
                trimToNull(c.websiteStatus()),
                trimToNull(c.linkedinUrl()),
                trimToNull(c.countryRegion()),
                trimToNull(c.email()),
                trimToNull(c.category()),
                trimToNull(c.status()),
                normalizeImportance(c.importance()),
                trimToNull(c.notes()),
                trimToNull(c.preferredLanguage())
        );
    }

    private static String trimToNull(String v) {
        return (v == null || v.isBlank()) ? null : v.trim();
    }
}
