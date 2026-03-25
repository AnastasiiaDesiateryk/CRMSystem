#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
EMAIL="${EMAIL:-admin@crm.local}"
PASS="${PASS:-ChangeMe123!}"

need() { command -v "$1" >/dev/null 2>&1 || { echo "Missing: $1"; exit 1; }; }
need curl
need python3

log() { printf "\n==> %s\n" "$*"; }

json_get() { # usage: json_get '<json>' 'path.to.key'
  local json="$1"
  local path="$2"

  python3 -c 'import json,sys
s=sys.stdin.read().strip()
if not s:
  print(""); raise SystemExit(0)
data=json.loads(s)
cur=data
for p in sys.argv[1].split("."):
  if not p:
    continue
  cur=cur[p]
print("" if cur is None else cur)
' "$path" <<<"$json"
}

wait_health() {
  log "Waiting for actuator health UP: $BASE_URL/actuator/health"
  for _ in $(seq 1 60); do
    if curl -fsS "$BASE_URL/actuator/health" >/tmp/health.json 2>/dev/null; then
      STATUS="$(python3 -c 'import json;print(json.load(open("/tmp/health.json")).get("status",""))' || true)"
      if [[ "$STATUS" == "UP" ]]; then
        echo "health=UP"
        return 0
      fi
    fi
    sleep 1
  done
  echo "health check failed (not UP)"
  cat /tmp/health.json 2>/dev/null || true
  exit 1
}

http() { # http METHOD URL [data] [content_type]
  local method="$1" url="$2" data="${3:-}" ctype="${4:-application/json}"
  local hfile bfile code
  hfile="$(mktemp)"; bfile="$(mktemp)"
  if [[ -n "$data" ]]; then
    code="$(curl -sS -D "$hfile" -o "$bfile" -w "%{http_code}" -X "$method" "$url" \
      -H "Accept: application/json" -H "Content-Type: $ctype" \
      ${TOKEN:+-H "Authorization: Bearer $TOKEN"} \
      ${IF_MATCH:+-H "If-Match: $IF_MATCH"} \
      --data "$data")"
  else
    code="$(curl -sS -D "$hfile" -o "$bfile" -w "%{http_code}" -X "$method" "$url" \
      -H "Accept: application/json" \
      ${TOKEN:+-H "Authorization: Bearer $TOKEN"} \
      ${IF_MATCH:+-H "If-Match: $IF_MATCH"})"
  fi
  echo "$code" > "${hfile}.code"
  echo "$hfile" > "${hfile}.path"
  echo "$bfile" > "${bfile}.path"
  echo "$hfile" "$bfile" "$code"
}

get_header() { # get_header headers_file 'ETag'
  local file="$1"
  local header="$2"
  python3 -c 'import sys
name=sys.argv[1].lower()
for line in sys.stdin.read().splitlines():
    if ":" not in line:
        continue
    k,v=line.split(":",1)
    if k.strip().lower()==name:
        print(v.strip())
        break
' "$header" < "$file"
}

assert_code() {
  local got="$1" exp="$2" context="$3"
  if [[ "$got" != "$exp" ]]; then
    echo "FAIL: $context expected HTTP $exp, got $got"
    exit 1
  fi
}

# --- Main ---
log "Smoke: backend API contract + optimistic locking"

wait_health

log "Login: get accessToken"
LOGIN_BODY="$(printf '{"email":"%s","password":"%s"}' "$EMAIL" "$PASS")"
read -r H B CODE < <(http POST "$BASE_URL/api/auth/login" "$LOGIN_BODY" "application/json")
assert_code "$CODE" "200" "login"
LOGIN_RESP="$(cat "$B")"
TOKEN="$(json_get "$LOGIN_RESP" "accessToken")"
[[ -n "$TOKEN" ]] || { echo "FAIL: accessToken missing in login response"; echo "$LOGIN_RESP"; exit 1; }
echo "token=OK"

log "Me: /api/me"
read -r H B CODE < <(http GET "$BASE_URL/api/me")
assert_code "$CODE" "200" "/api/me"
echo "/me=OK"

log "Organizations: list"
read -r H B CODE < <(http GET "$BASE_URL/api/organizations")
assert_code "$CODE" "200" "organizations list"
echo "list=OK"

log "Organizations: create (expect ETag header + body etag)"
CREATE_BODY='{
  "name":"Test Org",
  "website":"https://example.com",
  "websiteStatus":"working",
  "linkedinUrl":null,
  "countryRegion":"CH",
  "email":"test@example.com",
  "category":"mobility-fleet-management",
  "status":"active",
  "notes":"created from smoke",
  "preferredLanguage":"EN"
}'
read -r H B CODE < <(http POST "$BASE_URL/api/organizations" "$CREATE_BODY" "application/json")
# иногда делают 200 вместо 201 — это плохо, но не смертельно. хочешь — ужесточи.
if [[ "$CODE" != "201" && "$CODE" != "200" ]]; then
  echo "FAIL: create expected HTTP 201/200, got $CODE"
  echo "---- headers ----"; cat "$H"
  echo "---- body ----"; cat "$B"
  exit 1
fi

ETAG_HDR="$(get_header "$H" "ETag")"
RESP_BODY="$(cat "$B")"
ORG_ID="$(json_get "$RESP_BODY" "id")"
ETAG_BODY="$(json_get "$RESP_BODY" "etag")"

[[ -n "$ORG_ID" ]] || { echo "FAIL: id missing in create response"; echo "$RESP_BODY"; exit 1; }
[[ -n "$ETAG_HDR" ]] || { echo "FAIL: ETag header missing on create"; echo "headers:"; cat "$H"; exit 1; }

echo "created id=$ORG_ID"
echo "etag_header=$ETAG_HDR"
echo "etag_body=$ETAG_BODY"

log "Organizations: PATCH with If-Match (positive)"
# ВАЖНО: If-Match должен совпадать строка-в-строку с тем, что вернул ETag (включая W/ и кавычки)
IF_MATCH="$ETAG_HDR"

# Если у тебя PATCH контроллер ожидает merge-patch, поменяй Content-Type на application/merge-patch+json
PATCH_CT="${PATCH_CT:-application/json}"

read -r H B CODE < <(http PATCH "$BASE_URL/api/organizations/$ORG_ID" '{"notes":"patched"}' "$PATCH_CT")
assert_code "$CODE" "200" "patch positive"
NEW_ETAG="$(get_header "$H" "ETag")"
[[ -n "$NEW_ETAG" ]] || { echo "FAIL: new ETag header missing after patch"; cat "$H"; exit 1; }
echo "patch=OK new_etag=$NEW_ETAG"

log "Organizations: PATCH with wrong If-Match (negative expect 412)"
IF_MATCH='"999999"'
read -r H B CODE < <(http PATCH "$BASE_URL/api/organizations/$ORG_ID" '{"notes":"should fail"}' "$PATCH_CT")
assert_code "$CODE" "412" "patch negative (wrong If-Match)"
echo "patch_wrong=OK (412)"

log "Organizations: DELETE with If-Match (expect 204/200)"
IF_MATCH="$NEW_ETAG"
read -r H B CODE < <(http DELETE "$BASE_URL/api/organizations/$ORG_ID")
if [[ "$CODE" != "204" && "$CODE" != "200" ]]; then
  echo "FAIL: delete expected HTTP 204/200, got $CODE"
  echo "---- headers ----"; cat "$H"
  echo "---- body ----"; cat "$B"
  exit 1
fi
echo "delete=OK ($CODE)"

log "DONE: smoke passed"
