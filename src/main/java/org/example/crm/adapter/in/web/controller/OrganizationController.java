package org.example.crm.adapter.in.web.controller;

import org.example.crm.adapter.in.web.dto.*;
import org.example.crm.application.dto.*;
import org.example.crm.application.port.in.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ModelAttribute;
import java.net.URI;


import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.List;

/**
 * OrganizationController
 *
 * Inbound HTTP adapter responsible for translating REST semantics
 * into application-level use case invocations.
 *
 * Architectural role:
 * - Acts as a thin transport layer.
 * - Does NOT contain business logic.
 * - Delegates all domain rules to application use cases.
 *
 * Responsibilities:
 * - HTTP request mapping
 * - ETag / If-Match handling (protocol-level concern)
 * - DTO ↔ domain mapping
 *
 * Concurrency control:
 * - Uses weak ETag (W/"version") for optimistic locking.
 * - Enforces If-Match header on PATCH/DELETE.
 * - Prevents lost updates in concurrent modification scenarios.
 *
 * Security boundary:
 * - Authorization is enforced upstream (Spring Security filter chain).
 * - Controller assumes authenticated principal.
 *
 * Design principle:
 * - Web layer depends on application ports.
 * - No persistence or entity leakage.
 */
@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final ListOrganizationsUseCase listOrganizations;
    private final CreateOrganizationUseCase createOrganization;
    private final GetOrganizationUseCase getOrganization;
    private final PatchOrganizationUseCase patchOrganization;
    private final DeleteOrganizationUseCase deleteOrganization;

    public OrganizationController(
            ListOrganizationsUseCase listOrganizations,
            CreateOrganizationUseCase createOrganization,
            GetOrganizationUseCase getOrganization,
            PatchOrganizationUseCase patchOrganization,
            DeleteOrganizationUseCase deleteOrganization
    ) {
        this.listOrganizations = listOrganizations;
        this.createOrganization = createOrganization;
        this.getOrganization = getOrganization;
        this.patchOrganization = patchOrganization;
        this.deleteOrganization = deleteOrganization;
    }


    @PostMapping
    public ResponseEntity<OrganizationResponse> create(@RequestBody CreateOrganizationRequest req) {
        var created = createOrganization.create(new CreateOrganizationCommand(
                req.name(), req.website(), req.websiteStatus(), req.linkedinUrl(),
                req.countryRegion(), req.email(), req.category(), req.status(), req.notes(), req.preferredLanguage()
        ));

        var body = toResponse(created);

        return ResponseEntity
                .created(URI.create("/api/organizations/" + created.id()))
                .eTag(created.etag())
                .body(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponse> get(@PathVariable("id") UUID id) {
        var org = getOrganization.get(id);
        return ResponseEntity.ok().eTag(org.etag()).body(toResponse(org));
    }

    @GetMapping
    public ResponseEntity<PageResponse<OrganizationResponse>> list(@ModelAttribute OrganizationSearchQuery query) {
        var page = listOrganizations.list(query);

        List<OrganizationResponse> mapped = page.items()
                .stream()
                .map(OrganizationController::toResponse)
                .toList();

        var resp = new PageResponse<>(
                mapped,
                page.total(),
                page.page(),
                page.size()
        );

        return ResponseEntity.ok(resp);
    }


    @PatchMapping("/{id}")
    public ResponseEntity<OrganizationResponse> patch(
            @PathVariable("id") UUID id,
            @RequestHeader(name = HttpHeaders.IF_MATCH) String ifMatch,
            @RequestBody PatchOrganizationRequest req
    ) {
        long expectedVersion = parseWeakEtagVersion(ifMatch);

        var updated = patchOrganization.patch(id, new PatchOrganizationCommand(
                req.name(), req.website(), req.websiteStatus(), req.linkedinUrl(),
                req.countryRegion(), req.email(), req.category(), req.status(), req.notes(), req.preferredLanguage()
        ), expectedVersion);

        return ResponseEntity.ok().eTag(updated.etag()).body(toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable("id") UUID id,
            @RequestHeader(name = HttpHeaders.IF_MATCH) String ifMatch
    ) {
        long expectedVersion = parseWeakEtagVersion(ifMatch);
        deleteOrganization.delete(id, expectedVersion);
        return ResponseEntity.noContent().build();
    }

//    private static OrganizationResponse toResponse(OrganizationDetails o) {
//        var fmt = DateTimeFormatter.ISO_INSTANT;
//        return new OrganizationResponse(
//                o.id(),
//                o.name(),
//                o.website(),
//                o.websiteStatus(),
//                o.linkedinUrl(),
//                o.countryRegion(),
//                o.email(),
//                o.category(),
//                o.status(),
//                o.notes(),
//                o.preferredLanguage(),
//                o.createdAt() == null ? null : fmt.format(o.createdAt()),
//                o.updatedAt() == null ? null : fmt.format(o.updatedAt()),
//                o.etag()
//        );
//    }
private static OrganizationResponse toResponse(OrganizationDetails o) {
    var fmt = DateTimeFormatter.ISO_INSTANT;

    List<OrganizationResponse.ContactResponse> contacts =
            o.contacts() == null ? List.of() :
                    o.contacts().stream().map(c ->
                            new OrganizationResponse.ContactResponse(
                                    c.id(),
                                    c.organizationId(),
                                    c.name(),
                                    c.rolePosition(),
                                    c.email(),
                                    c.preferredLanguage(),
                                    c.notes(),
                                    c.createdAt() == null ? null : fmt.format(c.createdAt()),
                                    c.updatedAt() == null ? null : fmt.format(c.updatedAt()),
                                    c.etag()
                            )
                    ).toList();

    return new OrganizationResponse(
            o.id(),
            o.name(),
            o.website(),
            o.websiteStatus(),
            o.linkedinUrl(),
            o.countryRegion(),
            o.email(),
            o.category(),
            o.status(),
            o.notes(),
            o.preferredLanguage(),
            o.createdAt() == null ? null : fmt.format(o.createdAt()),
            o.updatedAt() == null ? null : fmt.format(o.updatedAt()),
            o.etag(),
            contacts
    );
}


    /**
     * Parses weak ETag values according to RFC 7232.
     *
     * Accepts:
     * - W/"3"
     * - "3"
     * - 3
     *
     * Converts to numeric version used by application layer.
     * Throws IllegalArgumentException if format invalid.
     *
     * Rationale:
     * Keeps HTTP semantics isolated in web adapter.
     */
    static long parseWeakEtagVersion(String etagHeader) {
        if (etagHeader == null || etagHeader.isBlank()) throw new IllegalArgumentException("missing_if_match");

        String v = etagHeader.trim();
        if (v.startsWith("W/")) v = v.substring(2).trim();
        if (v.startsWith("\"") && v.endsWith("\"") && v.length() >= 2) v = v.substring(1, v.length() - 1);

        try {
            return Long.parseLong(v);
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid_if_match");
        }
    }
}
