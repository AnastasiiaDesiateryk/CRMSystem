# ADR-002: Stateless JWT Authentication

## Status

Accepted

## Context

The system must:

* Scale horizontally
* Avoid server-side session storage
* Enforce clear security boundary

---

## Decision

Use stateless JWT access tokens (RS256) with:

* Asymmetric key signing
* Public JWKS endpoint
* Role claim embedded in token

---

## Consequences

Positive:

* Horizontal scalability
* No session replication
* Clear identity propagation

Trade-offs:

* Token revocation complexity
* Requires key rotation strategy

---
