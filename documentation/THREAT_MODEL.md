# Threat Model (STRIDE)

## Scope

This document models security threats against the CRM backend using the STRIDE framework.

System components in scope:

* REST API
* JWT authentication
* Persistence layer
* Dockerized deployment (dev environment)

---

## 1. Spoofing Identity

**Threat:**
Attacker forges JWT or impersonates another user.

**Mitigations:**

* RS256 asymmetric signing
* Signature verification using public key
* Expiration validation
* Subject stored as UUID
* No trust in client-sent roles

Residual risk:

* Compromised private key

---

## 2. Tampering

**Threat:**
Modification of requests or data in transit.

**Mitigations:**

* JWT signature integrity
* ETag / If-Match for write operations
* Server-side validation of version

Residual risk:

* Lack of enforced HTTPS (in local dev)

---

## 3. Repudiation

**Threat:**
User denies performing an action.

**Mitigations:**

* JWT contains subject (user ID)
* Server-side authentication resolution
* Structured logging

Missing:

* Audit log persistence
* Immutable event log

---

## 4. Information Disclosure

**Threat:**
Exposure of sensitive data.

**Mitigations:**

* No password storage in plain text (Argon2 hashing)
* Refresh tokens hashed before storage
* No exposure of internal entities
* Limited actuator exposure

Missing:

* Data masking layer
* Response-level field filtering

---

## 5. Denial of Service

**Threat:**
API flooding or resource exhaustion.

Current state:

* No rate limiting
* No throttling
* No circuit breaker

Mitigation potential:

* Reverse proxy rate limiting
* API gateway enforcement

---

## 6. Elevation of Privilege

**Threat:**
User escalates role to admin.

Mitigations:

* Role checks server-side
* Admin endpoints restricted via hasAuthority
* Role stored in DB and included in JWT

Residual risk:

* Misconfiguration of SecurityFilterChain

---

## Summary

The architecture supports extension with:

* Rate limiting
* Centralized audit logging
* HTTPS enforcement
* API gateway integration

Security boundaries are explicit and layered.

---
