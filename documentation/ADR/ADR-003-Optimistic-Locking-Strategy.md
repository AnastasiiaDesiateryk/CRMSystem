# ADR-003: Optimistic Locking via HTTP ETag / If-Match

## Status

Accepted

## Context

The CRM API supports concurrent edits (e.g., multiple users updating the same Organization).
Without concurrency control, last-write-wins leads to silent overwrites and data loss.

The system already exposes version semantics and requires a protocol-level mechanism to prevent lost updates.

## Decision

Adopt HTTP-native optimistic locking using:

* `ETag` response header returned on read and write operations
* `If-Match` request header required on mutation endpoints (PATCH/DELETE)
* Version mismatch results in **412 Precondition Failed**
* Weak ETag format supported (e.g., `W/"3"`)

Version parsing and HTTP contract enforcement remains in the **inbound web adapter**, while the application layer receives a numeric `expectedVersion`.

## Rationale

* Aligns with standard HTTP semantics and existing client tooling
* Prevents silent overwrites without introducing server-side session state
* Keeps concurrency concerns out of the domain model (transport-level concern)
* Scales horizontally (no centralized lock coordinator required)

## Consequences

Positive:

* Predictable concurrency control and explicit conflict handling
* No last-write-wins data corruption
* Works naturally with REST clients and caching semantics

Trade-offs:

* Clients must implement If-Match handling and retry logic
* Additional 412 conflict path to document and test

## Implementation Notes

* Controllers return `ETag` for GET/POST/PATCH responses
* PATCH/DELETE require `If-Match`
* Application layer validates `expectedVersion` and rejects stale updates

---
