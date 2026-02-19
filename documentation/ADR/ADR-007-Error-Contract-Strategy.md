# ADR-007: Consistent Error Contract Strategy (Problem Details)

## Status

Accepted

## Context

Without a consistent error contract, APIs tend to:

* Return inconsistent error formats
* Leak internal exception messages
* Break clients when error payloads change
* Make frontend error handling unpredictable

The system requires:

* Stable error responses
* Machine-readable error codes
* Predictable HTTP status semantics
* No internal stack traces exposed

## Decision

Adopt a unified error contract aligned conceptually with **RFC 7807 (Problem Details)**.

All API errors will return a consistent JSON structure:

```json
{
  "type": "https://api.example.com/errors/validation-error",
  "title": "Validation failed",
  "status": 400,
  "code": "validation_error",
  "detail": "Email must not be blank",
  "instance": "/api/organizations"
}
```

### Rules

* HTTP status code must reflect semantic meaning
* `code` is stable and client-consumable
* `detail` is human-readable
* No stack traces in production
* Internal exception classes are not exposed

## Error Categories

| Category                 | HTTP | Code             |
| ------------------------ | ---- | ---------------- |
| Validation error         | 400  | validation_error |
| Unauthorized             | 401  | unauthorized     |
| Forbidden                | 403  | forbidden        |
| Conflict (ETag mismatch) | 412  | version_conflict |
| Not found                | 404  | not_found        |
| Internal error           | 500  | internal_error   |

## Rationale

* Stable client contract
* Improved frontend UX consistency
* Easier monitoring and observability
* Security: prevents information leakage

## Consequences

Positive:

* Predictable integration behavior
* Easier API documentation
* Stronger separation between internal exceptions and external contracts

Trade-offs:

* Requires central exception handling layer
* Developers must map exceptions to error codes intentionally

## Implementation Notes

* Use `@ControllerAdvice` for centralized exception mapping
* Map domain exceptions → standardized error DTO
* Disable detailed stack traces in production profile
* Ensure Swagger/OpenAPI documents error responses

---
