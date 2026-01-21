

# 1) AuthProvider → какие запросы будет слать фронт

## ✅ Login

**Front call:** `login(email, password)`

**Request**

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{ "email": "admin@supplychaintech.ch", "password": "..." }
```

**Response (200)**

```json
{
  "accessToken": "jwt",
  "refreshToken": "uuid.raw",
  "expiresInSeconds": 900,
  "user": {
    "id": "uuid",
    "email": "admin@supplychaintech.ch",
    "name": "Admin User",
    "role": "admin",
    "hasAccess": true,
    "createdAt": "2024-01-01T10:00:00Z"
  }
}
```

**Что сохранит фронт**

* `accessToken` (memory/localStorage — как решишь)
* `user` в state (`setUser(user)`)

---

## ✅ Register

**Front call:** `register(email, password, name)`

**Request**

```http
POST /api/auth/register
```

```json
{ "email": "x@y.com", "password": "...", "name": "..." }
```

**Response (201 или 200)**
Вариант A (как сейчас boolean):

```json
{ "success": true }
```

Вариант B (лучше, сразу логинит):

```json
{
  "accessToken": "jwt",
  "refreshToken": "uuid.raw",
  "expiresInSeconds": 900,
  "user": { ... }
}
```

---

## ✅ Logout

**Front call:** `logout()`

**Request**

```http
POST /api/auth/logout
Authorization: Bearer <accessToken>
```

```json
{ "refreshToken": "uuid.raw" }
```

**Response (204)**
(no body)

> Да, с JWT logout можно и без сервера, но у тебя уже есть refresh tokens — так правильно.

---

# 2) DataProvider → какие запросы будет слать фронт

## 2.1 Organizations

### ✅ Initial load (вместо чтения localStorage)

**Front useEffect** → загрузка списка

**Request**

```http
GET /api/organizations?page=0&size=50&q=&category=
Authorization: Bearer <accessToken>
```

**Response**

```json
{
  "items": [
    {
      "id": "uuid",
      "name": "Swiss Post",
      "website": "https://post.ch",
      "websiteStatus": "working",
      "linkedinUrl": "https://linkedin.com/company/swiss-post",
      "countryRegion": "Switzerland",
      "email": "contact@post.ch",
      "category": "warehousing-intralogistics-robotics",
      "status": "active",
      "notes": "Major logistics partner",
      "createdAt": "2024-01-15T10:00:00Z",
      "updatedAt": "2024-01-15T10:00:00Z",
      "contactsCount": 1,
      "etag": "W/\"3\""
    }
  ],
  "page": 0,
  "size": 50,
  "total": 3
}
```

> Это покрывает **карточки**, фильтры и счётчик Contacts(1).

---

### ✅ Add organization

**Front call:** `addOrganization(org)`

**Request**

```http
POST /api/organizations
Content-Type: application/json
Authorization: Bearer <accessToken>
```

```json
{
  "name": "...",
  "website": "...",
  "websiteStatus": "working",
  "linkedinUrl": "...",
  "countryRegion": "Switzerland",
  "email": "...",
  "category": "...",
  "status": "active",
  "notes": "..."
}
```

**Response (201)**
Возвращаем созданный объект в том же формате, чтобы фронт просто добавил в список:

```json
{
  "id": "uuid",
  "createdAt": "...",
  "updatedAt": "...",
  "etag": "W/\"0\"",
  "...": "..."
}
```

---

### ✅ Update organization (partial)

**Front call:** `updateOrganization(id, partialOrg)`

**Request**

```http
PATCH /api/organizations/{id}
Authorization: Bearer <accessToken>
If-Match: W/"3"
```

```json
{
  "websiteStatus": "not-working",
  "notes": "..."
}
```

**Response (200)**

```json
{
  "id": "uuid",
  "updatedAt": "...",
  "etag": "W/\"4\"",
  "...": "..."
}
```

---

### ✅ Delete organization

**Front call:** `deleteOrganization(id)`

**Request**

```http
DELETE /api/organizations/{id}
Authorization: Bearer <accessToken>
If-Match: W/"3"
```

**Response (204)**

**Правило:** backend **каскадно** удаляет контакты (как у тебя сейчас в коде фронта).

---

## 2.2 Contacts

### ✅ Load contacts by organization

Когда жмёшь `Contacts (1)` или открываешь модал:

```http
GET /api/organizations/{orgId}/contacts
Authorization: Bearer <accessToken>
```

Response:

```json
[
  {
    "id": "uuid",
    "organizationId": "uuid",
    "name": "Hans Müller",
    "rolePosition": "Head of Logistics",
    "email": "hans.mueller@post.ch",
    "preferredLanguage": "DE",
    "notes": "...",
    "createdAt": "...",
    "updatedAt": "...",
    "etag": "W/\"1\""
  }
]
```

### ✅ Add contact

```http
POST /api/organizations/{orgId}/contacts
Authorization: Bearer <accessToken>
```

```json
{
  "name": "...",
  "rolePosition": "...",
  "email": "...",
  "preferredLanguage": "DE",
  "notes": "..."
}
```

### ✅ Update contact

```http
PATCH /api/contacts/{id}
Authorization: Bearer <accessToken>
If-Match: W/"1"
```

```json
{ "notes": "..." }
```

### ✅ Delete contact

```http
DELETE /api/contacts/{id}
Authorization: Bearer <accessToken>
If-Match: W/"1"
```

---

## 2.3 Custom Fields

### ✅ Load fields (на старте)

```http
GET /api/custom-fields?entityType=ORGANIZATION
Authorization: Bearer <accessToken>
```

### ✅ Add field

```http
POST /api/custom-fields
Authorization: Bearer <accessToken>
```

```json
{
  "entityType": "ORGANIZATION",
  "key": "source",
  "label": "Source",
  "fieldType": "select",
  "options": ["TED", "LinkedIn", "Referral"],
  "required": false,
  "sortOrder": 10
}
```

### ✅ Delete field

```http
DELETE /api/custom-fields/{id}
Authorization: Bearer <accessToken>
If-Match: W/"0"
```

---

# 3) Import/Export (твой `importData(orgs, conts)`)

У тебя на UI есть Import/Export в навигации.

## ✅ Import (bulk replace или upsert)

```http
POST /api/import
Authorization: Bearer <accessToken>
Content-Type: application/json
```

```json
{
  "organizations": [...],
  "contacts": [...]
}
```

**Response**

```json
{ "importedOrganizations": 123, "importedContacts": 456 }
```

> Важно: надо заранее решить — import делает **replace** или **upsert**. Сейчас фронт делает replace в localStorage. Для enterprise лучше upsert по email/website/имени, но это отдельная политика.

---

# 4) Что тебе важно решить прямо сейчас (иначе будет переделка)

## A) ID в фронте сейчас string (`"1"`, `"2"`)

В бекенде нормально использовать UUID. На фронте это всё равно string — **ничего не ломается**.

## B) ETag / If-Match

Фронт сейчас этого не шлёт. Чтобы не переписывать UI полностью:

* на update/delete ты просто добавишь заголовок `If-Match` из `org.etag`

## C) contactsCount

Либо:

* отдаём сразу в `GET /organizations` (как я выше заложил)


---

# 5) 

1. **GET /api/organizations** (list + filters + contactsCount + etag) — это уже почти сделали 
2. **POST /api/organizations**
3. **PATCH /api/organizations/{id}** + If-Match
4. **DELETE /api/organizations/{id}** + cascade contacts
5. **GET /api/organizations/{id}/contacts**

 **POST/PATCH/DELETE organizations**
