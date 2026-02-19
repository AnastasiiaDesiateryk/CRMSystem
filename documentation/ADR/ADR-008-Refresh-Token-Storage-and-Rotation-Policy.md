# ADR-008: Refresh Token Storage & Rotation Policy

## Status

Accepted

## Context

Access tokens are short-lived and stateless (JWT RS256).
To support long-lived sessions without re-authentication, refresh tokens are required.

However, naive refresh token handling introduces risks:

* Token replay attacks
* Token theft reuse
* Lack of revocation capability
* Unlimited session duration

The system must implement a secure and scalable refresh strategy.

## Decision

Adopt the following refresh token policy:

### 1. Storage Strategy

* Refresh tokens are stored server-side
* Stored hashed (never plaintext)
* Linked to:

  * User ID
  * Device metadata (optional: user agent / IP)
  * Expiration timestamp
  * Revocation flag

### 2. Rotation Policy

On each refresh:

* Old refresh token is invalidated (revoked)
* New refresh token is issued
* Reuse of a revoked refresh token invalidates the session

### 3. Expiration

* Access token lifetime: short (e.g., 15 minutes)
* Refresh token lifetime: longer (e.g., 7–30 days)
* Absolute session lifetime may be enforced (optional future enhancement)

### 4. Revocation

Refresh tokens can be revoked:

* On logout
* On suspicious activity
* On password change (future enhancement)

## Rationale

* Prevents replay attacks
* Limits damage from token leakage
* Supports logout semantics in stateless architecture
* Aligns with modern OAuth2 security recommendations

## Consequences

Positive:

* Strong session security posture
* Controlled token lifecycle
* Server-side revocation capability

Trade-offs:

* Introduces server-side state (refresh token store)
* Requires additional DB operations
* Slightly increased complexity

## Implementation Notes

* Hash refresh tokens using secure hashing (e.g., SHA-256 + salt)
* Store only hashed value in database
* On refresh:

  * Validate token
  * Check revocation flag
  * Issue new pair
  * Mark old token revoked
* Log suspicious reuse attempts

---

