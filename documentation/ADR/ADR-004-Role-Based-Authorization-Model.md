# ADR-004: Role-Based Authorization Model (RBAC)

## Status

Accepted

## Context

The system requires controlled access to protected resources and privileged administrative operations (e.g., user role management).

Client-side role logic is not trustworthy.
Authorization must be enforced server-side with a clear security boundary.

## Decision

Adopt a server-side RBAC model with:

* Roles represented as authorities (e.g., `ROLE_ADMIN`, `ROLE_USER`)
* Roles stored in the database and included as a claim in access tokens
* Administrative endpoints scoped under `/api/admin/**`
* Access policy enforced via Spring Security authorization rules:

  * `/api/admin/**` requires `ROLE_ADMIN`
  * all other non-public endpoints require authentication

Bootstrap admin provisioning is supported for dev/stage environments to ensure first-login operability.

## Rationale

* Clear, explainable security model aligned with enterprise norms
* Prevents client-side role spoofing
* Stateless enforcement compatible with horizontal scaling
* Keeps authorization decisions centralized and auditable

## Consequences

Positive:

* Strong security boundary and predictable behavior
* Role checks are consistent across the system
* Supports administrative governance workflows

Trade-offs:

* Token refresh needed after role changes to reflect updated claims
* Requires careful endpoint allowlist/denylist configuration

## Implementation Notes

* JWT contains `roles` claim
* `TokenFilter` maps roles to `GrantedAuthority`
* `SecurityFilterChain` enforces `/api/admin/**` restriction
* `/api/me` provides client identity source of truth

---
