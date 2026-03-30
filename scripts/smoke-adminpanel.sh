#!/usr/bin/env bash
set -euo pipefail

# Usage:
#   BASE_URL=http://localhost:8080 \
#   ADMIN_EMAIL=admin@example.com \
#   ADMIN_PASSWORD=secret \
#   USER_EMAIL=user.$(date +%s)@example.com \
#   USER_PASSWORD=secret123 \
#   USER_NAME='Smoke User' \
#   bash smoke-adminpanel.sh
#
# Requires: curl, jq

BASE_URL="${BASE_URL:-http://localhost:8080}"
ADMIN_EMAIL="${ADMIN_EMAIL:?ADMIN_EMAIL is required}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:?ADMIN_PASSWORD is required}"
USER_EMAIL="${USER_EMAIL:-smoke.user.$(date +%s)@example.com}"
USER_PASSWORD="${USER_PASSWORD:-secret12345}"
USER_NAME="${USER_NAME:-Smoke User}"

TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT

red()   { printf '\033[31m%s\033[0m\n' "$*"; }
green() { printf '\033[32m%s\033[0m\n' "$*"; }
blue()  { printf '\033[34m%s\033[0m\n' "$*"; }

fail() {
  red "FAIL: $*"
  exit 1
}

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || fail "Missing required command: $1"
}

require_cmd curl
require_cmd jq

request() {
  local method="$1"; shift
  local url="$1"; shift
  local body="${1:-}"
  local auth="${2:-}"
  local out_body="$TMP_DIR/body.json"
  local out_status="$TMP_DIR/status.txt"

  local -a args=(
    -sS
    -X "$method"
    -H 'Content-Type: application/json'
    -o "$out_body"
    -w '%{http_code}'
  )

  if [[ -n "$auth" ]]; then
    args+=( -H "Authorization: Bearer $auth" )
  fi

  if [[ -n "$body" ]]; then
    args+=( -d "$body" )
  fi

  local status
  status="$(curl "${args[@]}" "$url")"
  printf '%s' "$status" > "$out_status"

  cat "$out_body"
}

status_code() {
  cat "$TMP_DIR/status.txt"
}

assert_status() {
  local expected="$1"
  local actual
  actual="$(status_code)"
  [[ "$actual" == "$expected" ]] || fail "Expected HTTP $expected but got $actual. Body: $(cat "$TMP_DIR/body.json")"
}

assert_jq() {
  local expr="$1"
  jq -e "$expr" "$TMP_DIR/body.json" >/dev/null || fail "jq assertion failed: $expr. Body: $(cat "$TMP_DIR/body.json")"
}

login() {
  local email="$1"
  local password="$2"
  request POST "$BASE_URL/api/auth/login" "$(jq -nc --arg email "$email" --arg password "$password" '{email:$email,password:$password}')" >/dev/null
  assert_status 200
  jq -r '.accessToken' "$TMP_DIR/body.json"
}

register_user() {
  request POST "$BASE_URL/api/auth/register" "$(jq -nc --arg email "$USER_EMAIL" --arg password "$USER_PASSWORD" --arg name "$USER_NAME" '{email:$email,password:$password,name:$name}')" >/dev/null
  assert_status 200
  assert_jq '.accessToken | type == "string" and length > 10'
}

blue "1) Login as bootstrap/admin user"
ADMIN_TOKEN="$(login "$ADMIN_EMAIL" "$ADMIN_PASSWORD")"
green "OK: admin login"

blue "2) GET /api/me returns current admin profile"
request GET "$BASE_URL/api/me" "" "$ADMIN_TOKEN" >/dev/null
assert_status 200
EMAIL_FROM_ME="$(jq -r '.email' "$TMP_DIR/body.json")"
[[ "$EMAIL_FROM_ME" == "$ADMIN_EMAIL" ]] || fail "/api/me returned unexpected email: $EMAIL_FROM_ME"
assert_jq '.roles | index("ROLE_ADMIN") != null'
assert_jq 'has("hasAccess")'
green "OK: /api/me"

blue "3) Register a normal test user"
register_user
green "OK: register user $USER_EMAIL"

blue "4) Newly registered normal user must NOT access admin endpoints"
USER_TOKEN_FROM_REGISTER="$(jq -r '.accessToken' "$TMP_DIR/body.json")"
request GET "$BASE_URL/api/admin/users" "" "$USER_TOKEN_FROM_REGISTER" >/dev/null
STATUS="$(status_code)"
[[ "$STATUS" == "403" || "$STATUS" == "401" ]] || fail "Expected 401/403 for non-admin on /api/admin/users but got $STATUS. Body: $(cat "$TMP_DIR/body.json")"
green "OK: non-admin blocked from /api/admin/users"

blue "5) Admin can list users"
request GET "$BASE_URL/api/admin/users" "" "$ADMIN_TOKEN" >/dev/null
assert_status 200
assert_jq 'type == "array"'
jq -e --arg email "$USER_EMAIL" 'map(select(.email == $email)) | length == 1' "$TMP_DIR/body.json" >/dev/null \
  || fail "Expected registered user in admin list. Body: $(cat "$TMP_DIR/body.json")"
TARGET_USER_ID="$(jq -r --arg email "$USER_EMAIL" 'map(select(.email == $email))[0].id' "$TMP_DIR/body.json")"
[[ -n "$TARGET_USER_ID" && "$TARGET_USER_ID" != "null" ]] || fail "Could not find target user id in admin list"
green "OK: admin list users, target user id=$TARGET_USER_ID"

blue "6) Admin grants ROLE_ADMIN to target user"
request PUT "$BASE_URL/api/admin/users/$TARGET_USER_ID/roles" "$(jq -nc '{roles:["ROLE_ADMIN"]}')" "$ADMIN_TOKEN" >/dev/null
assert_status 200
assert_jq '.roles | index("ROLE_ADMIN") != null'
assert_jq '.roles | index("ROLE_USER") != null'
green "OK: roles updated"

blue "7) Admin revokes target user's access"
request PATCH "$BASE_URL/api/admin/users/$TARGET_USER_ID/access" "$(jq -nc '{hasAccess:false}')" "$ADMIN_TOKEN" >/dev/null
assert_status 200
assert_jq '.hasAccess == false'
green "OK: access revoked"

blue "8) Blocked user cannot login"
request POST "$BASE_URL/api/auth/login" "$(jq -nc --arg email "$USER_EMAIL" --arg password "$USER_PASSWORD" '{email:$email,password:$password}')" >/dev/null
STATUS="$(status_code)"
[[ "$STATUS" == "401" || "$STATUS" == "400" || "$STATUS" == "403" ]] || fail "Expected blocked login to fail with 400/401/403 but got $STATUS. Body: $(cat "$TMP_DIR/body.json")"
CODE="$(jq -r '.code // empty' "$TMP_DIR/body.json")"
[[ "$CODE" == "access_denied" || -n "$CODE" ]] || fail "Expected error code in blocked login response"
green "OK: blocked user cannot login (status=$STATUS code=$CODE)"

blue "9) Admin re-enables access"
request PATCH "$BASE_URL/api/admin/users/$TARGET_USER_ID/access" "$(jq -nc '{hasAccess:true}')" "$ADMIN_TOKEN" >/dev/null
assert_status 200
assert_jq '.hasAccess == true'
green "OK: access restored"

blue "10) Promoted user can now login and see admin panel data"
PROMOTED_TOKEN="$(login "$USER_EMAIL" "$USER_PASSWORD")"
request GET "$BASE_URL/api/admin/users" "" "$PROMOTED_TOKEN" >/dev/null
assert_status 200
assert_jq 'type == "array"'
green "OK: promoted user can access admin endpoints"

green "All admin panel backend smoke checks passed."

# usage:  BASE_URL=http://localhost:8080 \
          #ADMIN_EMAIL=admin@crm.local \
          #ADMIN_PASSWORD='ChangeMe123!' \
          #USER_EMAIL=smoke.user.$(date +%s)@example.com \
          #USER_PASSWORD=secret12345 \
          #USER_NAME="Smoke User" \
          #bash scripts/smoke-adminpanel.sh