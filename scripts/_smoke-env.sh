#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
APP_ENV_FILE="${APP_ENV_FILE:-$ROOT_DIR/.env}"
ADMIN_SECRET_FILE="${ADMIN_SECRET_FILE:-$ROOT_DIR/secrets/bootstrap_admin_password.txt}"

read_env_value() {
  local key="$1"
  local file="$2"
  [[ -f "$file" ]] || return 1
  grep -E "^${key}=" "$file" | tail -n1 | cut -d= -f2-
}

SMOKE_BASE_URL="${SMOKE_BASE_URL:-${BASE_URL:-${BASE:-http://localhost:8080}}}"

SMOKE_ADMIN_EMAIL="${SMOKE_ADMIN_EMAIL:-${ADMIN_EMAIL:-${EMAIL:-${LOGIN_EMAIL:-}}}}"
if [[ -z "${SMOKE_ADMIN_EMAIL}" ]]; then
  SMOKE_ADMIN_EMAIL="$(read_env_value BOOTSTRAP_ADMIN_EMAIL "$APP_ENV_FILE" || true)"
fi

SMOKE_ADMIN_PASSWORD="${SMOKE_ADMIN_PASSWORD:-${ADMIN_PASSWORD:-${PASS:-${LOGIN_PASSWORD:-}}}}"
if [[ -z "${SMOKE_ADMIN_PASSWORD}" && -f "$ADMIN_SECRET_FILE" ]]; then
  SMOKE_ADMIN_PASSWORD="$(tr -d '\r\n' < "$ADMIN_SECRET_FILE")"
fi

[[ -n "${SMOKE_ADMIN_EMAIL}" ]] || { echo "Missing bootstrap admin email"; exit 1; }
[[ -n "${SMOKE_ADMIN_PASSWORD}" ]] || { echo "Missing bootstrap admin password"; exit 1; }

export SMOKE_BASE_URL
export SMOKE_ADMIN_EMAIL
export SMOKE_ADMIN_PASSWORD