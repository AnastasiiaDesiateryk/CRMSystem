# Authentication Flow & Security Context

## Purpose

Explain how authentication context is built and why `TokenFilter` is mandatory.

---

# 1. Request Flow (Hexagonal)

```
HTTP GET /api/me
```

Flow:

```
Client
  → TokenFilter (adapter.in.web.security)
  → SecurityContext populated
  → UserController
  → UserServicePort
  → CurrentUserIdPort
  → SecurityContextCurrentUserIdAdapter
  → UserStore
  → Database
```

---

# 2. TokenFilter Responsibility

TokenFilter:

* Extracts `Authorization: Bearer <jwt>`
* Validates signature & expiration
* Reads subject (UUID) and roles
* Creates Authentication object
* Stores it in Spring Security `SecurityContext`

Without this step, authentication does not exist.

---

# 3. Why `/api/me` Depends on It

`SecurityContextCurrentUserIdAdapter`:

```java
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
```

If `TokenFilter` does not run:

* `SecurityContext` is empty
* `currentUserId()` throws
* `/api/me` returns 401

---

# 4. Filter Registration

`TokenFilter` must run before:

```
UsernamePasswordAuthenticationFilter
```

Configured in `SecurityFilterChain`.

---

# 5. Manual Verification

Without token:

```
curl -i http://localhost:8080/api/me
```

Expected:

```
401 Unauthorized
```

With token:

```
curl -i http://localhost:8080/api/me \
  -H "Authorization: Bearer <JWT>"
```

Expected:

```
200 OK
```

---

# 6. Security Boundary Summary

* Domain layer has zero knowledge of JWT
* Application layer knows only CurrentUserIdPort
* Web adapter handles token parsing
* Security enforcement is centralized

This ensures:

* Clean separation of concerns
* Stateless scaling
* Framework isolation from domain logic

---
