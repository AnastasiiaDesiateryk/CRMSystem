# API Contract

## Overview

This document defines the HTTP contract between the frontend and backend.
All protected endpoints require a valid JWT access token.

Base URL (dev):

```
http://localhost:8080
```

Authentication header:

```
Authorization: Bearer <accessToken>
```

All mutation endpoints that modify resources require `If-Match` for optimistic locking.

---

# 1. Authentication

## 1.1 Login

### Request

```
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "email": "admin@supplychaintech.ch",
  "password": "..."
}
```

### Response (200)

```json
{
  "accessToken": "jwt",
  "refreshToken": "uuid.raw",
  "expiresInSeconds": 900,
  "user": {
    "id": "uuid",
    "email": "admin@supplychaintech.ch",
    "name": "Admin User",
    "roles": ["ROLE_ADMIN"],
    "createdAt": "2024-01-01T10:00:00Z"
  }
}
```

Frontend stores:

* `accessToken`
* `refreshToken`
* `user` in state

---

## 1.2 Register

```
POST /api/auth/register
```

```json
{
  "email": "x@y.com",
  "password": "...",
  "name": "..."
}
```

Response:

* Either returns tokens (auto-login)
* Or returns success response

---

## 1.3 Logout

```
POST /api/auth/logout
Authorization: Bearer <accessToken>
```

```json
{
  "refreshToken": "uuid.raw"
}
```

Response:

```
204 No Content
```

---

## 1.4 Refresh Token

```
POST /api/auth/refresh
```

```json
{
  "refreshToken": "uuid.raw"
}
```

Response:

```json
{
  "accessToken": "new.jwt",
  "refreshToken": "new.uuid.raw",
  "expiresInSeconds": 900
}
```

Old refresh token is invalidated (rotation policy).

---

# 2. Identity

## 2.1 Current User

```
GET /api/me
Authorization: Bearer <accessToken>
```

Response (200):

```json
{
  "id": "uuid",
  "email": "...",
  "name": "...",
  "roles": ["ROLE_USER"]
}
```

Without token → `401 Unauthorized`.

---

# 3. Organizations

## 3.1 List Organizations

```
GET /api/organizations?page=0&size=50&q=&category=
Authorization: Bearer <accessToken>
```

Response:

```json
{
  "items": [
    {
      "id": "uuid",
      "name": "Swiss Post",
      "website": "https://post.ch",
      "websiteStatus": "working",
      "linkedinUrl": "...",
      "countryRegion": "Switzerland",
      "email": "...",
      "category": "...",
      "status": "active",
      "notes": "...",
      "createdAt": "...",
      "updatedAt": "...",
      "contactsCount": 1,
      "etag": "W/\"3\""
    }
  ],
  "page": 0,
  "size": 50,
  "total": 3
}
```

---

## 3.2 Create Organization

```
POST /api/organizations
Authorization: Bearer <accessToken>
Content-Type: application/json
```

Response (201):

* Returns created entity
* Includes `etag`

---

## 3.3 Update Organization

```
PATCH /api/organizations/{id}
Authorization: Bearer <accessToken>
If-Match: W/"3"
```

Response (200):

* Updated entity
* New `etag`

If version mismatch → `412 Precondition Failed`.

---

## 3.4 Delete Organization

```
DELETE /api/organizations/{id}
Authorization: Bearer <accessToken>
If-Match: W/"3"
```

Response:

```
204 No Content
```

Contacts are cascade-deleted.

---

# 4. Contacts

## 4.1 List Contacts by Organization

```
GET /api/organizations/{orgId}/contacts
Authorization: Bearer <accessToken>
```

---

## 4.2 Create Contact

```
POST /api/organizations/{orgId}/contacts
Authorization: Bearer <accessToken>
```

---

## 4.3 Update Contact

```
PATCH /api/contacts/{id}
Authorization: Bearer <accessToken>
If-Match: W/"1"
```

---

## 4.4 Delete Contact

```
DELETE /api/contacts/{id}
Authorization: Bearer <accessToken>
If-Match: W/"1"
```

---

# 5. Custom Fields

```
GET /api/custom-fields?entityType=ORGANIZATION
POST /api/custom-fields
DELETE /api/custom-fields/{id}
```

Protected endpoints.

---

# 6. Import

```
POST /api/import
Authorization: Bearer <accessToken>
```

Upsert strategy recommended for enterprise usage.

---

# 7. Concurrency Model

* All mutable resources expose `etag`
* All update/delete require `If-Match`
* Conflict → 412

---

# 8. Error Contract

Errors follow consistent structure (see ADR-007).

---
