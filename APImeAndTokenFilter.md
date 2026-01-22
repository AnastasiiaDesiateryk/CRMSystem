## `/api/me`: How authentication context is built (why `TokenFilter` is mandatory)

This endpoint returns the current authenticated user.  
It relies on `SecurityContext` being populated **before** the controller is called.

### Request flow (Hexagonal)



HTTP GET /api/me
```
→ UserController (adapter.in.web.controller)
→ UserServicePort.me() (application.port.in)
→ UserService (application.service)
→ CurrentUserIdPort.currentUserId() (application.port.out)
→ SecurityContextCurrentUserIdAdapter (adapter.in.web.security)
```
и параллельно
```
  → LoadUserPort.findById() (application.port.out)
  → UserPersistenceAdapter (adapter.out.persistence)
  → UserJpaRepository (adapter.out.persistence.jpa)
  → DB
```



### Why `TokenFilter` is required

`SecurityContextCurrentUserIdAdapter` reads the authenticated principal from Spring Security's `SecurityContext`.

If **TokenFilter is not registered**, the `SecurityContext` stays empty, so:
- `CurrentUserIdPort.currentUserId()` returns empty / throws
- `/api/me` always responds as **unauthenticated**

### Where the filter is wired

`TokenFilter` must run **before** Spring's `UsernamePasswordAuthenticationFilter`:



### Quick manual test

1. Call without token → should be `401`
2. Call with token → should return the current user

```bash
curl -i http://localhost:8080/api/me

curl -i http://localhost:8080/api/me \
  -H "Authorization: Bearer <JWT>"
```

**Expected:**

* without header: `401 Unauthorized`
* with valid token: `200 OK` + user payload

```

