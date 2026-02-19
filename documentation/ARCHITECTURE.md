
#  ARCHITECTURE.md

# Architecture Overview

CRMSystem follows Hexagonal Architecture (Ports & Adapters).

---

# C4 Model – Level 1 (System Context)

```
        ┌────────────────────┐
        │      End User       │
        └──────────┬─────────┘
                   │
                   ▼
        ┌────────────────────┐
        │     Frontend       │
        │  (React + Vite)    │
        └──────────┬─────────┘
                   │ REST
                   ▼
        ┌────────────────────┐
        │   CRM Backend      │
        │  (Spring Boot)     │
        └──────────┬─────────┘
                   │
                   ▼
        ┌────────────────────┐
        │   PostgreSQL DB    │
        └────────────────────┘
```

---

# C4 Model – Level 2 (Container View)

```
CRM Backend
│
├── Web Layer (Controllers, Filters)
├── Application Layer (Use Cases)
├── Domain Layer (Business Models)
├── Persistence Adapter (JPA)
└── Security Infrastructure (JWT)
```

---

# C4 Model – Level 3 (Component View – Backend)

```
[Inbound Adapter]
   AuthController
   OrganizationController
   UserController

          ↓

[Application Layer]
   AuthService
   OrganizationCommandService
   OrganizationQueryService
   UserService

          ↓

[Outbound Ports]
   UserStore
   OrganizationWritePort
   OrganizationReadPort
   AccessTokenIssuer

          ↓

[Outbound Adapters]
   UserStoreJpaAdapter
   OrganizationWriteAdapter
   JwtService
```

---

# Architectural Rules

1. Domain has zero framework annotations.
2. Application defines ports.
3. Adapters implement ports.
4. Controllers are thin transport adapters.
5. Persistence entities are never exposed to domain.

---

# Cross-Cutting Concerns

* Security via JWT filter
* ETag handling at web adapter
* Flyway migrations at startup
* Profile-based configuration

---
