# API Versioning Strategy

## Goals

* Enable backward-compatible evolution of the API
* Avoid breaking frontend clients unexpectedly
* Provide a clear deprecation and migration path

## Strategy

Use URI-based versioning:

* `/api/v1/...`

Reason:

* Simple for clients
* Clear routing separation
* Compatible with OpenAPI/Swagger and gateways

## Backward Compatibility Policy

* Minor changes (new fields) are backward compatible
* Removing fields, changing semantics, or changing required inputs is breaking
* Breaking changes trigger a new version (`/api/v2`)

## Deprecation Policy

* Deprecate endpoints with explicit notice in release notes
* Keep deprecated endpoints for a defined window (e.g., 60–90 days in real prod environments)
* Provide a migration guide for clients

## Contract Discipline

* DTOs are treated as contracts, not implementation details
* Errors are stable and documented
* HTTP status codes remain consistent

---
