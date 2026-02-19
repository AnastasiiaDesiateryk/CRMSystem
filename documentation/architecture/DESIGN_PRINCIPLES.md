# Design Principles

## 1. Inward Dependency Rule

All dependencies point toward the domain and application layers.

---

## 2. Explicit Security Boundaries

Security is handled:

* At HTTP filter layer
* At authorization boundary
* Not inside domain models

---

## 3. Transport-Agnostic Use Cases

Application services are unaware of:

* HTTP
* JSON
* JPA

---

## 4. Fail Fast

* Missing If-Match → error
* Invalid version → 412
* Invalid token → unauthenticated

---

## 5. Infrastructure Replaceability

Persistence, hashing, clock, and token generation are defined as ports.

---
