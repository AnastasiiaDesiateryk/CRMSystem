# SECURITY.md

# Security Model

## Authentication

* JWT access tokens
* RS256 asymmetric signing
* Short-lived access tokens
* Refresh token storage with hashing

---

## Token Validation

1. Token extracted from `Authorization: Bearer`
2. Signature verified via public key
3. Expiration validated
4. Roles extracted from claim
5. Authentication stored in SecurityContext

---

## Authorization

RBAC enforced via:

```
.requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
.anyRequest().authenticated()
```

Roles are included in JWT claim `roles`.

---

## Public Endpoints

* `/api/auth/**`
* `/.well-known/jwks.json`
* `/actuator/health`
* `/swagger-ui/**`

---

## Protected Endpoints

All other endpoints require valid Bearer token.

---

## Concurrency Security

* PATCH/DELETE require `If-Match`
* Version mismatch returns 412
* Prevents lost updates

---

## Bootstrap Admin (Dev Only)

Admin can be provisioned via environment variables:

```
BOOTSTRAP_ADMIN_EMAIL
BOOTSTRAP_ADMIN_PASSWORD
BOOTSTRAP_ADMIN_NAME
```

Enabled only in `dev` profile.

---

## Security Boundaries

* Domain unaware of JWT
* Application unaware of Spring Security
* SecurityContext bridged via adapter
* Cryptography isolated in infrastructure layer

---

## Security Considerations

Current implementation assumes:

* HTTPS termination at reverse proxy (not included)
* No rate limiting implemented
* No account lockout mechanism
* No brute-force mitigation

These are infrastructure-level concerns and can be added without architectural refactoring.

---
