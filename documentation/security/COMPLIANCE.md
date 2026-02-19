# Security Posture & OWASP Alignment

This project aligns conceptually with OWASP Top 10 mitigation strategies.

---

## A01 – Broken Access Control

Mitigation:

* Server-side RBAC enforcement
* No client-trusted roles
* hasAuthority checks

---

## A02 – Cryptographic Failures

Mitigation:

* Argon2 password hashing
* RS256 JWT signing
* No symmetric secret exposure

---

## A03 – Injection

Mitigation:

* JPA parameter binding
* No dynamic SQL construction

---

## A04 – Insecure Design

Mitigation:

* Explicit threat model
* Separation of concerns
* Concurrency control

---

## A05 – Security Misconfiguration

Mitigation:

* Limited actuator exposure
* Profile-based config
* Explicit endpoint allowlist

---

## A07 – Identification & Authentication Failures

Mitigation:

* Short-lived access tokens
* Refresh token hashing
* Secure random token generation

---

## Gaps

* No rate limiting
* No WAF integration
* No centralized logging
* No TLS enforcement in repo

Architecture supports adding these without refactoring.

---
