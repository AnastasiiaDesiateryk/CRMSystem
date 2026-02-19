# CRMSystem
<!-- Documentation badges -->
[![Architecture](https://img.shields.io/badge/Docs-Architecture-blue)](documentation/architecture/ARCHITECTURE.md)
[![API Contract](https://img.shields.io/badge/Docs-API%20Contract-brightgreen)](documentation/api/API_CONTRACT.md)
[![Auth Flow](https://img.shields.io/badge/Docs-Auth%20Flow-orange)](documentation/api/AUTHENTICATION_FLOW.md)
[![Security](https://img.shields.io/badge/Docs-Security-red)](documentation/security/SECURITY.md)
[![Threat Model](https://img.shields.io/badge/Docs-Threat%20Model-critical)](documentation/security/THREAT_MODEL.md)
[![OWASP](https://img.shields.io/badge/Security-OWASP%20Alignment-black)](documentation/security/COMPLIANCE.md)
[![ADRs](https://img.shields.io/badge/Docs-ADRs-lightgrey)](documentation/ADR/)
[![Hardening Roadmap](https://img.shields.io/badge/Roadmap-Production%20Hardening-purple)](documentation/roadmap/PRODUCTION_HARDENING.md)
[![Scaling](https://img.shields.io/badge/Architecture-Scaling%20Strategy-9cf)](documentation/architecture/SCALING_STRATEGY.md)
[![API Versioning](https://img.shields.io/badge/Architecture-API%20Versioning-ffd700)](documentation/architecture/API_VERSIONING.md)


## Overview

CRMSystem is a full-stack CRM foundation built with an enterprise-oriented backend architecture.

The backend strictly follows Hexagonal Architecture (Ports & Adapters), ensuring domain isolation, framework independence, and infrastructure replaceability.

Security is stateless and enforced server-side via JWT (RS256) + RBAC.
Concurrency is handled using HTTP-native optimistic locking (ETag / If-Match).

This project demonstrates production-grade backend design patterns beyond simple CRUD implementations.

---

## Key Features

* Hexagonal Architecture with strict inward dependency rule
* Stateless JWT authentication (RS256) with public JWKS endpoint
* Role-Based Access Control (`ROLE_ADMIN`, `ROLE_USER`)
* Optimistic locking with ETag / If-Match (412 on conflict)
* Flyway-managed schema migrations
* Dockerized full-stack environment
* Clean separation of domain, application, adapters, and infrastructure

---

## Architecture

The system is structured around use cases and ports.

```
               ┌──────────────────────────┐
               │        Controllers       │
               │   (Inbound Web Adapter)  │
               └──────────────┬───────────┘
                              │
                              ▼
               ┌──────────────────────────┐
               │     Application Layer    │
               │  (Use Cases + Ports)     │
               └──────────────┬───────────┘
                              │
                              ▼
               ┌──────────────────────────┐
               │        Domain Model      │
               │     (Pure Business)      │
               └──────────────┬───────────┘
                              │
                              ▼
               ┌──────────────────────────┐
               │    Persistence Adapter   │
               │     (JPA / PostgreSQL)   │
               └──────────────────────────┘
```

**Dependency rule:**
Outer layers depend on inner layers.
The domain never depends on Spring, JPA, or web frameworks.

---

## Project Structure

```
src/main/java/org/example/crm
│
├── domain.model
│   └── User, Organization, Contact
│
├── application
│   ├── port.in
│   ├── port.out
│   └── service
│
├── adapter
│   ├── in.web
│   └── out.persistence
│
├── infrastructure
│   ├── config
│   ├── security
│   └── time
```

---

## Tech Stack

* Java 21
* Spring Boot 3
* Spring Security
* PostgreSQL
* Flyway
* Docker / Docker Compose
* React + Vite + TypeScript
* JUnit 5, JaCoCo, PIT, PMD
* CycloneDX SBOM

---

## Local Development

### Start full stack

```bash
docker compose up --build
```

### Health check

```bash
curl -fsS http://localhost:8080/actuator/health
```

### Access

Frontend:

```
http://localhost:5173
```

Backend:

```
http://localhost:8080
```

Swagger UI:

```
http://localhost:8080/swagger-ui/index.html
```

### Rebuild backend only

```bash
docker compose build --no-cache app
docker compose up -d
```

---

## Demo Flow (E2E)

Typical request lifecycle:

```
login → /api/me → create organization → patch with If-Match
```

This demonstrates:

* Authentication
* Identity resolution
* Authorization
* Write operation
* Optimistic locking enforcement

---

## Demo curl Scenario

### 1. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@crm.local","password":"ChangeMe123!"}'
```

Copy the `accessToken`.

---

### 2. Who am I

```bash
curl http://localhost:8080/api/me \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

---

### 3. Create Organization

```bash
curl -X POST http://localhost:8080/api/organizations \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Org","website":"example.com"}'
```

Save returned `ETag`.

---

### 4. Update with If-Match

```bash
curl -X PATCH http://localhost:8080/api/organizations/<ID> \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "If-Match: \"1\"" \
  -H "Content-Type: application/json" \
  -d '{"name":"Updated Org"}'
```

If version mismatches → **412 Precondition Failed**

---

## Security Model

### Public Endpoints

* `/api/auth/**`
* `/.well-known/jwks.json`
* `/actuator/health`
* `/actuator/info`
* `/swagger-ui/**`
* `/v3/api-docs/**`

### Protected Endpoints

All other endpoints require:

```
Authorization: Bearer <access_token>
```

### Token

* Algorithm: RS256
* Public key exposed via JWKS
* Access token contains `roles` claim
* No session state

---

## Concurrency Control

Write operations require:

* Server returns `ETag`
* Client must send `If-Match`
* Version conflict → HTTP 412

This prevents silent overwrites in concurrent edits.

---

## Quality Gates

Run tests:

```bash
./mvnw test
```

Coverage report:

```
target/site/jacoco/index.html
```

Mutation testing:

```
target/pit-reports/
```

Static analysis:

```
target/site/pmd.html
```

SBOM:

```
target/bom.json
```

---

## Roadmap

See GitHub **Milestones** for architecture evolution and delivery phases.

---
## Documentation

### Architecture
- [Architecture Overview](documentation/architecture/ARCHITECTURE.md)
- [Design Principles](documentation/architecture/DESIGN_PRINCIPLES.md)
- [C4 Diagrams](documentation/architecture/c4/)
- [API Versioning Strategy](documentation/architecture/API_VERSIONING.md)
- [Scaling Strategy](documentation/architecture/SCALING_STRATEGY.md)

### API
- [API Contract](documentation/api/API_CONTRACT.md)
- [Authentication Flow](documentation/api/AUTHENTICATION_FLOW.md)

### Security
- [Security Model](documentation/security/SECURITY.md)
- [Threat Model (STRIDE)](documentation/security/THREAT_MODEL.md)
- [OWASP Alignment](documentation/security/COMPLIANCE.md)

### Decisions & Roadmap
- [Architecture Decision Records](documentation/ADR/)
- [Production Hardening Roadmap](documentation/roadmap/PRODUCTION_HARDENING.md)


