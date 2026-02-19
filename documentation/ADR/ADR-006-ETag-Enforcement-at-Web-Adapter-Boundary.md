# ADR-006: ETag / If-Match Enforcement at Web Adapter Boundary

## Status

Accepted

## Context

Optimistic locking is implemented via version checks to prevent lost updates.
The HTTP protocol already provides standard semantics for this: `ETag` and `If-Match`.

The system must decide where to enforce and parse these HTTP-specific headers while keeping the domain and application layers transport-agnostic.

## Decision

Enforce and parse `ETag` / `If-Match` headers in the inbound web adapter (controller layer), not in the application or domain layers.

* Controllers:

  * return `ETag` in responses for relevant resources
  * require `If-Match` on mutation endpoints (PATCH/DELETE)
  * parse weak ETag formats (e.g., `W/"3"`, `"3"`)
* Application services receive:

  * a numeric `expectedVersion` (already normalized)
* Application services enforce version consistency and raise a conflict signal
* Web adapter maps conflicts to HTTP **412 Precondition Failed**

## Rationale

* `ETag` and `If-Match` are HTTP transport concerns
* Keeps application layer independent of web protocol details
* Reduces coupling and improves testability of use cases
* Allows reuse of application services from non-HTTP contexts (e.g., messaging, batch jobs)

## Consequences

Positive:

* Clean separation of concerns (protocol vs business)
* Consistent HTTP contract enforcement at the edge
* Domain/application remain framework- and transport-agnostic

Trade-offs:

* Requires explicit documentation for clients (If-Match mandatory on writes)
* Controllers must include parsing and validation logic for header formats

## Implementation Notes

* Parsing helper (e.g., `parseWeakEtagVersion`) stays in the controller or a web-adapter utility class
* Missing `If-Match` triggers a client error (e.g., 400/428 based on chosen policy)
* Conflict from application layer is mapped to 412 consistently

---
