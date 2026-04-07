#!/usr/bin/env bash
set -euo pipefail

BASE="${BASE:-http://localhost:8080}"

LOGIN_EMAIL="${LOGIN_EMAIL:-admin@crm.local}"
LOGIN_PASSWORD="${LOGIN_PASSWORD:-ChangeMe123!}"

CREATE_FILE="${CREATE_FILE:-./tmp/import-smoke/import-create.xlsx}"
MERGE_FILE="${MERGE_FILE:-./tmp/import-smoke/import-merge.xlsx}"

SMOKE_NAME="${SMOKE_NAME:-Smoke Org AG}"
SMOKE_WEBSITE="${SMOKE_WEBSITE:-https://smoke-org.example}"
SMOKE_LINKEDIN="${SMOKE_LINKEDIN:-https://www.linkedin.com/company/smoke-org/}"
SMOKE_ORG_EMAIL="${SMOKE_ORG_EMAIL:-hello@smoke-org.example}"
SMOKE_CONTACT_1="${SMOKE_CONTACT_1:-alice@smoke-org.example}"
SMOKE_CONTACT_2="${SMOKE_CONTACT_2:-ops@smoke-org.example}"
SMOKE_CONTACT_3="${SMOKE_CONTACT_3:-new@smoke-org.example}"

tmpdir="$(mktemp -d)"
trap 'rm -rf "$tmpdir"' EXIT

fail() { echo "ERROR: $*" >&2; exit 1; }

require_file() {
  local f="$1"
  [[ -f "$f" ]] || fail "missing file: $f"
}

json_get() {
  local file="$1"
  local expr="$2"
  python3 - "$file" "$expr" <<'PY'
import json, sys
path = sys.argv[1]
expr = sys.argv[2]
data = json.load(open(path))

cur = data
for part in expr.split("."):
    cur = cur[part]
print(cur if cur is not None else "")
PY
}

json_query_org() {
  local file="$1"
  local name="$2"
  local website="$3"
  python3 - "$file" "$name" "$website" <<'PY'
import json, sys

path, name, website = sys.argv[1], sys.argv[2], sys.argv[3]
data = json.load(open(path))

orgs = None

if isinstance(data, list):
    orgs = data
elif isinstance(data, dict):
    if isinstance(data.get("content"), list):
        orgs = data["content"]
    elif isinstance(data.get("items"), list):
        orgs = data["items"]
    elif isinstance(data.get("_embedded"), dict) and isinstance(data["_embedded"].get("organizations"), list):
        orgs = data["_embedded"]["organizations"]

if orgs is None:
    raise SystemExit(f"Unsupported /api/organizations response shape: {type(data).__name__}")

for org in orgs:
    if (org.get("website") or "").strip() == website or (org.get("name") or "").strip() == name:
        print(json.dumps(org))
        raise SystemExit(0)

raise SystemExit(1)
PY
}

assert_org_field_equals() {
  local file="$1"
  local field="$2"
  local expected="$3"
  python3 - "$file" "$field" "$expected" <<'PY'
import json, sys
path, field, expected = sys.argv[1], sys.argv[2], sys.argv[3]
org = json.load(open(path))
actual = org.get(field)
if actual != expected:
    raise SystemExit(f"Field {field!r} mismatch. expected={expected!r} actual={actual!r}")
PY
}

assert_contacts_len() {
  local file="$1"
  local expected="$2"
  python3 - "$file" "$expected" <<'PY'
import json, sys
path, expected = sys.argv[1], int(sys.argv[2])
org = json.load(open(path))
contacts = org.get("contacts") or []
actual = len(contacts)
if actual != expected:
    raise SystemExit(f"Expected {expected} contacts, got {actual}")
PY
}

assert_contact_email_exists() {
  local file="$1"
  local email="$2"
  python3 - "$file" "$email" <<'PY'
import json, sys
path, email = sys.argv[1], sys.argv[2].lower()
org = json.load(open(path))
emails = [str((c.get("email") or "")).strip().lower() for c in (org.get("contacts") or [])]
if email not in emails:
    raise SystemExit(f"Missing contact email: {email}")
PY
}

assert_contact_email_count() {
  local file="$1"
  local email="$2"
  local expected="$3"
  python3 - "$file" "$email" "$expected" <<'PY'
import json, sys
path, email, expected = sys.argv[1], sys.argv[2].lower(), int(sys.argv[3])
org = json.load(open(path))
emails = [str((c.get("email") or "")).strip().lower() for c in (org.get("contacts") or [])]
actual = sum(1 for e in emails if e == email)
if actual != expected:
    raise SystemExit(f"Email {email!r} expected count {expected}, got {actual}")
PY
}

assert_unique_contact_emails_len() {
  local file="$1"
  local expected="$2"
  python3 - "$file" "$expected" <<'PY'
import json, sys
path, expected = sys.argv[1], int(sys.argv[2])
org = json.load(open(path))
emails = [
    str((c.get("email") or "")).strip().lower()
    for c in (org.get("contacts") or [])
    if str((c.get("email") or "")).strip()
]
actual = len(set(emails))
if actual != expected:
    raise SystemExit(f"Expected {expected} unique contact emails, got {actual}: {sorted(set(emails))}")
PY
}

req() {
  local name="$1"; shift
  local method="$1"; shift
  local url="$1"; shift

  echo "-> $method $url"

  curl -sS -D "$tmpdir/$name.h" -o "$tmpdir/$name.json" \
    "${CURL_AUTH[@]}" \
    -X "$method" "$url" \
    "$@"

  local status
  status="$(head -n 1 "$tmpdir/$name.h" | tr -d '\r')"
  echo "   $status"

  if ! echo "$status" | grep -Eq 'HTTP/[0-9.]+\s+(2|3)[0-9]{2}\b'; then
    echo "---- response headers ----" >&2
    cat "$tmpdir/$name.h" >&2
    echo "---- response body ----" >&2
    cat "$tmpdir/$name.json" >&2
    fail "request failed: $method $url"
  fi
}

echo "0) PRECHECK"
require_file "$CREATE_FILE"
require_file "$MERGE_FILE"

echo "1) LOGIN -> get accessToken"
LOGIN_JSON="$(printf '{"email":"%s","password":"%s"}' "$LOGIN_EMAIL" "$LOGIN_PASSWORD")"

curl -sS -D "$tmpdir/login.h" -o "$tmpdir/login.json" \
  -H "Content-Type: application/json" \
  -X POST "$BASE/api/auth/login" \
  --data "$LOGIN_JSON"

ACCESS_TOKEN="$(python3 -c 'import json,sys; print(json.load(open(sys.argv[1])).get("accessToken",""))' "$tmpdir/login.json")"
[[ -n "$ACCESS_TOKEN" ]] || fail "login did not return accessToken. Check credentials and /api/auth/login."

AUTH_HEADER="Authorization: Bearer $ACCESS_TOKEN"
echo "token_len=${#ACCESS_TOKEN}"

declare -a CURL_AUTH
CURL_AUTH=(-H "$AUTH_HEADER")

echo "2) IMPORT create file"
req import_create POST "$BASE/api/imports/organizations/excel" \
  -F "file=@${CREATE_FILE};type=application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

echo "---- import_create response ----"
cat "$tmpdir/import_create.json"
echo

echo "3) GET organizations after first import"
req orgs_after_create GET "$BASE/api/organizations"

if ! json_query_org "$tmpdir/orgs_after_create.json" "$SMOKE_NAME" "$SMOKE_WEBSITE" > "$tmpdir/org1.json"; then
  fail "organization not found after first import: name=$SMOKE_NAME website=$SMOKE_WEBSITE"
fi

echo "4) ASSERT first import"
assert_org_field_equals "$tmpdir/org1.json" "name" "$SMOKE_NAME"
assert_org_field_equals "$tmpdir/org1.json" "website" "$SMOKE_WEBSITE"
assert_org_field_equals "$tmpdir/org1.json" "linkedinUrl" "$SMOKE_LINKEDIN"
assert_org_field_equals "$tmpdir/org1.json" "email" "$SMOKE_ORG_EMAIL"
assert_org_field_equals "$tmpdir/org1.json" "status" "active"
assert_contacts_len "$tmpdir/org1.json" 2
assert_contact_email_exists "$tmpdir/org1.json" "$SMOKE_CONTACT_1"
assert_contact_email_exists "$tmpdir/org1.json" "$SMOKE_CONTACT_2"

echo "5) IMPORT merge file"
req import_merge POST "$BASE/api/imports/organizations/excel" \
  -F "file=@${MERGE_FILE};type=application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

echo "---- import_merge response ----"
cat "$tmpdir/import_merge.json"
echo

echo "6) GET organizations after merge import"
req orgs_after_merge GET "$BASE/api/organizations"

if ! json_query_org "$tmpdir/orgs_after_merge.json" "$SMOKE_NAME" "$SMOKE_WEBSITE" > "$tmpdir/org2.json"; then
  fail "organization not found after merge import: name=$SMOKE_NAME website=$SMOKE_WEBSITE"
fi

echo "7) ASSERT merge behavior"
assert_org_field_equals "$tmpdir/org2.json" "linkedinUrl" "$SMOKE_LINKEDIN"
assert_org_field_equals "$tmpdir/org2.json" "email" "$SMOKE_ORG_EMAIL"
assert_unique_contact_emails_len "$tmpdir/org2.json" 3
assert_contact_email_exists "$tmpdir/org2.json" "$SMOKE_CONTACT_3"
assert_contact_email_count "$tmpdir/org2.json" "$SMOKE_CONTACT_1" 1

echo "OK: smoke import passed"

#python scripts/generate-import-fixtures.py
 #./scripts/smoke-import.sh