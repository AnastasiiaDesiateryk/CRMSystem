#!/usr/bin/env bash
set -euo pipefail

BASE="${BASE:-http://localhost:8080}"


LOGIN_EMAIL="${LOGIN_EMAIL:-admin@crm.local}"
LOGIN_PASSWORD="${LOGIN_PASSWORD:-ChangeMe123!}"

tmpdir="$(mktemp -d)"
trap 'rm -rf "$tmpdir"' EXIT

fail() { echo "ERROR: $*" >&2; exit 1; }

# --- 0) Get token ---
echo "0) LOGIN -> get accessToken"
LOGIN_JSON="$(printf '{"email":"%s","password":"%s"}' "$LOGIN_EMAIL" "$LOGIN_PASSWORD")"

TOKEN="$(curl -sS -D "$tmpdir/login.h" -o "$tmpdir/login.json" \
  -H "Content-Type: application/json" \
  -X POST "$BASE/api/auth/login" \
  --data "$LOGIN_JSON" \
  | true)"

# curl writes body to file, so parse from file:
ACCESS_TOKEN="$(python3 -c 'import json,sys; print(json.load(open(sys.argv[1])).get("accessToken",""))' "$tmpdir/login.json" || true)"
[[ -n "$ACCESS_TOKEN" ]] || fail "login did not return accessToken. Check $BASE/api/auth/login and credentials."

AUTH_HEADER="Authorization: Bearer $ACCESS_TOKEN"
echo "token_len=${#ACCESS_TOKEN}"

# safe curl auth args
declare -a CURL_AUTH
CURL_AUTH=(-H "$AUTH_HEADER")

# helper: do request and keep headers/body
# usage: req <name> <method> <url> [extra curl args...]
req() {
  local name="$1"; shift
  local method="$1"; shift
  local url="$1"; shift

  echo "-> $method $url"

  # -f to fail on 4xx/5xx? We want body for debug, so handle manually
  curl -sS -D "$tmpdir/$name.h" -o "$tmpdir/$name.json" \
    "${CURL_AUTH[@]}" \
    -X "$method" "$url" \
    "$@"

  # show status from headers (first line)
  local status
  status="$(head -n 1 "$tmpdir/$name.h" | tr -d '\r')"
  echo "   $status"

  # if not 2xx/3xx => print body and exit
  if ! echo "$status" | grep -Eq 'HTTP/[0-9.]+\s+(2|3)[0-9]{2}\b'; then
    echo "---- response headers ----" >&2
    cat "$tmpdir/$name.h" >&2
    echo "---- response body ----" >&2
    cat "$tmpdir/$name.json" >&2
    fail "request failed: $method $url"
  fi
}

get_etag() {
  local headers_file="$1"
  awk -F': ' 'tolower($1)=="etag"{print $2}' "$headers_file" | tr -d '\r'
}

get_json_field() {
  local file="$1"
  local field="$2"
  python3 -c 'import json,sys; print(json.load(open(sys.argv[1]))[sys.argv[2]])' "$file" "$field"
}

# --- 1) POST org ---
echo "1) POST /api/organizations"
req org POST "$BASE/api/organizations" \
  -H "Content-Type: application/json" \
  --data '{
    "name":"Smoke Org",
    "website":"https://example.com",
    "websiteStatus":"UNKNOWN",
    "linkedinUrl":"https://linkedin.com/company/example",
    "countryRegion":"CH",
    "email":"smoke@example.com",
    "category":"SMOKE",
    "status":"ACTIVE",
    "notes":"smoke",
    "preferredLanguage":"EN"
  }'

ORG_ID="$(get_json_field "$tmpdir/org.json" "id")"
echo "ORG_ID=$ORG_ID"

# --- 2) POST contact ---
echo "2) POST /api/organizations/$ORG_ID/contacts"
req c1 POST "$BASE/api/organizations/$ORG_ID/contacts" \
  -H "Content-Type: application/json" \
  --data '{
    "name":"Smoke Contact",
    "rolePosition":"QA",
    "email":"contact@example.com",
    "preferredLanguage":"EN",
    "notes":"smoke"
  }'

CONTACT_ID="$(get_json_field "$tmpdir/c1.json" "id")"
echo "CONTACT_ID=$CONTACT_ID"

# --- 3) GET contact + ETag ---
echo "3) GET contact + ETag"
req cget GET "$BASE/api/organizations/$ORG_ID/contacts/$CONTACT_ID"

ETAG="$(get_etag "$tmpdir/cget.h")"
[[ -n "$ETAG" ]] || fail "no ETag on GET contact"
echo "ETAG=$ETAG"

# --- 4) PATCH contact ---
echo "4) PATCH contact (If-Match required)"
req cpatch PATCH "$BASE/api/organizations/$ORG_ID/contacts/$CONTACT_ID" \
  -H "Content-Type: application/json" \
  -H "If-Match: $ETAG" \
  --data '{
    "name":"Smoke Contact Updated",
    "rolePosition":"QA",
    "email":"contact2@example.com",
    "preferredLanguage":"EN",
    "notes":"updated"
  }'

ETAG2="$(get_etag "$tmpdir/cpatch.h")"
[[ -n "$ETAG2" ]] || fail "no ETag on PATCH contact"
echo "ETAG2=$ETAG2"

# --- 5) DELETE contact ---
echo "5) DELETE contact (If-Match required)"
req cdel DELETE "$BASE/api/organizations/$ORG_ID/contacts/$CONTACT_ID" \
  -H "If-Match: $ETAG2"

echo "OK: smoke contacts passed"

# usage:  BASE=http://localhost:8080 ./scripts/smoke-contacts.sh