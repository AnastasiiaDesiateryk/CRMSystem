#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SCRIPTS_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

source "$SCRIPTS_DIR/_smoke-env.sh"

BASE_URL="${BASE_URL:-$SMOKE_BASE_URL}"
EMAIL="${EMAIL:-$SMOKE_ADMIN_EMAIL}"
PASS="${PASS:-$SMOKE_ADMIN_PASSWORD}"

need() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "Missing: $1"
    exit 1
  }
}

need curl
need jq
need k6

echo "==> Login: $EMAIL"
LOGIN_RESP="$(
  curl -sS -X POST "$BASE_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    -H "Accept: application/json" \
    -d "$(jq -nc --arg email "$EMAIL" --arg password "$PASS" \
      '{email:$email,password:$password}')"
)"

TOKEN="$(echo "$LOGIN_RESP" | jq -r '.accessToken // empty')"

if [[ -z "$TOKEN" ]]; then
  echo "FAIL: accessToken missing"
  echo "$LOGIN_RESP"
  exit 1
fi

echo "==> token=OK"
echo "==> Running k6 against $BASE_URL"

BASE_URL="$BASE_URL" TOKEN="$TOKEN" \
  k6 run "$SCRIPT_DIR/create-organizations-load-test.js"