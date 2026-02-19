# ADR-005: Bootstrap Admin Provisioning Strategy

## Status

Accepted

## Context

A newly deployed system must be operable from a clean database state.
Without an initial privileged identity, administrative actions (user onboarding, role management) are blocked.

The system needs a safe “first login” mechanism that:

* requires no manual SQL steps
* is reproducible in development environments
* does not introduce a permanent security backdoor

## Decision

Implement a bootstrap admin provisioning mechanism driven by environment configuration and activated only in non-production profiles.

* Environment variables:

  * `BOOTSTRAP_ADMIN_EMAIL`
  * `BOOTSTRAP_ADMIN_PASSWORD`
  * `BOOTSTRAP_ADMIN_NAME`
* Execution via an application startup hook (e.g., `CommandLineRunner`)
* Bootstrap runs only when:

  * active profile is `dev` (and optionally `test`)
  * the user table is empty (or no users exist)
* Created bootstrap user is assigned `ROLE_ADMIN`

After the first user exists, bootstrap provisioning is no longer applied.

## Rationale

* Enables “day-0 operability” for local/dev deployments
* Avoids manual DB manipulation and reduces onboarding friction
* Ensures bootstrap behavior is deterministic and environment-controlled
* Constrains the bootstrap to non-prod profiles to prevent persistent backdoors

## Consequences

Positive:

* Fresh deployments become immediately usable
* Clear and repeatable setup for development and demo environments
* Minimal code impact and aligned with environment-based configuration

Trade-offs:

* Requires correct profile configuration to avoid accidental production enablement
* Bootstrap credentials must be treated as secrets and managed accordingly

## Implementation Notes

* Bootstrap logic belongs to infrastructure layer (startup concerns)
* User creation uses outbound ports (e.g., `UserStore`, `PasswordHasher`) rather than direct JPA usage
* Password is stored hashed (Argon2) as per standard authentication pipeline

---
