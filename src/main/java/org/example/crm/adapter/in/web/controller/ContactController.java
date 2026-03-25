package org.example.crm.adapter.in.web.controller;

import org.example.crm.adapter.in.web.dto.ContactDtos.*;
import org.example.crm.application.port.in.ContactUseCases.*;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/organizations/{orgId}/contacts")
public class ContactController {

    private final ListContactsUseCase listContacts;
    private final CreateContactUseCase createContact;
    private final GetContactUseCase getContact;
    private final PatchContactUseCase patchContact;
    private final DeleteContactUseCase deleteContact;

    public ContactController(
            ListContactsUseCase listContacts,
            CreateContactUseCase createContact,
            GetContactUseCase getContact,
            PatchContactUseCase patchContact,
            DeleteContactUseCase deleteContact
    ) {
        this.listContacts = listContacts;
        this.createContact = createContact;
        this.getContact = getContact;
        this.patchContact = patchContact;
        this.deleteContact = deleteContact;
    }

    @PostMapping
    public ResponseEntity<ContactResponse> create(
            @PathVariable UUID orgId,
            @RequestBody CreateContactRequest req
    ) {
        var created = createContact.create(new CreateContactCommand(
                orgId,
                req.name(),
                req.rolePosition(),
                req.email(),
                req.preferredLanguage(),
                req.notes()
        ));

        return ResponseEntity
                .created(URI.create("/api/organizations/" + orgId + "/contacts/" + created.id()))
                .eTag(created.etag())
                .body(toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<ContactResponse>> list(@PathVariable UUID orgId) {
        var items = listContacts.list(orgId).stream()
                .map(ContactController::toResponse)
                .toList();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{contactId}")
    public ResponseEntity<ContactResponse> get(
            @PathVariable UUID orgId,
            @PathVariable UUID contactId
    ) {
        var c = getContact.get(orgId, contactId);
        return ResponseEntity.ok().eTag(c.etag()).body(toResponse(c));
    }

    @PatchMapping("/{contactId}")
    public ResponseEntity<ContactResponse> patch(
            @PathVariable UUID orgId,
            @PathVariable UUID contactId,
            @RequestHeader(HttpHeaders.IF_MATCH) String ifMatch,
            @RequestBody PatchContactRequest req
    ) {
        long expectedVersion = OrganizationController.parseWeakEtagVersion(ifMatch);

        var updated = patchContact.patch(
                orgId,
                contactId,
                new PatchContactCommand(
                        req.name(),
                        req.rolePosition(),
                        req.email(),
                        req.preferredLanguage(),
                        req.notes()
                ),
                expectedVersion
        );

        return ResponseEntity.ok().eTag(updated.etag()).body(toResponse(updated));
    }

    @DeleteMapping("/{contactId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID orgId,
            @PathVariable UUID contactId,
            @RequestHeader(HttpHeaders.IF_MATCH) String ifMatch
    ) {
        long expectedVersion = OrganizationController.parseWeakEtagVersion(ifMatch);
        deleteContact.delete(orgId, contactId, expectedVersion);
        return ResponseEntity.noContent().build();
    }

    private static ContactResponse toResponse(ContactDetails c) {
        var fmt = DateTimeFormatter.ISO_INSTANT;
        return new ContactResponse(
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
        );
    }
}