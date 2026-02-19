# C4 Model – Level 4

## Scope

This view describes internal backend components and their interactions across adapters, application services, and infrastructure.

---

## Inbound Web Adapter

### AuthController

* Receives login/register/refresh/logout HTTP requests
* Extracts user agent and IP for security context
* Delegates to `AuthServicePort`

### OrganizationController

* Handles CRUD operations for organizations
* Enforces HTTP concurrency contract (ETag / If-Match)
* Delegates to dedicated use cases:

  * CreateOrganizationUseCase
  * GetOrganizationUseCase
  * PatchOrganizationUseCase
  * DeleteOrganizationUseCase

### UserController (`/api/me`)

* Returns current authenticated user via `UserServicePort`

### AdminUserController (`/api/admin/**`)

* Admin-only role assignment (`setRoles`)
* Delegates to `AdminUserServicePort`

---

## Security Infrastructure (HTTP Boundary)

### TokenFilter

* Extracts Bearer token
* Validates token signature & expiry
* Loads subject and roles from claims
* Sets `Authentication` in `SecurityContext`

### JwtService

* Issues access tokens (RS256)
* Validates and parses JWT
* Exposes JWKS for external verification

### SecurityContextCurrentUserIdAdapter

* Bridges Spring Security context into application port `CurrentUserIdPort`
* Enforces principal contract: UUID string as subject

---

## Application Layer

### AuthService (Use Case)

* Validates credentials (via AuthenticationManager)
* Issues tokens via `JwtService`
* Persists refresh token state (if implemented)
* Returns `TokenResponse`

### AdminUserService

* Business use case: assign roles to users
* Calls `UserStore.setRoles(userId, roles)`

### Organization Use Cases

* Command-side operations enforce version correctness
* Query-side returns `OrganizationDetails` including ETag/version

### UserService (`me`)

* Uses `CurrentUserIdPort` + `UserStore` to resolve identity and roles

---

## Outbound Persistence Adapter

### UserStoreJpaAdapter

* Implements `UserStore`
* Maps between domain `User` and persistence `UserEntity`
* Supports role update via `setRoles`

### Organization persistence adapter (JPA)

* Stores organizations, version and audit metadata
* Supports read/write operations via ports

---

## Runtime Wiring

Spring Boot DI composes the system:

* Adapters depend on ports
* Application services depend on ports
* Infrastructure depends on technical frameworks only

---
