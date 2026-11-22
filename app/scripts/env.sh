#!/bin/bash
# Nạp cổng dùng chung cho mọi script (source, không chạy trực tiếp).
# Thứ tự ưu tiên: biến môi trường đã đặt > tệp app/.env.ports (không commit; mỗi checkout/worktree một bản) > mặc định.
# Mặc định giữ nguyên như checkout chính: PG 54329, Redis 6389, máy chủ 8080, Vite 5173.
_GEO_APP_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
if [ -f "$_GEO_APP_DIR/.env.ports" ]; then
  # chỉ đặt biến chưa có trong môi trường
  while IFS='=' read -r k v; do
    case "$k" in ''|\#*) continue ;; esac
    if [ -z "${!k+x}" ]; then export "$k=$v"; fi
  done < "$_GEO_APP_DIR/.env.ports"
fi
export GEO_PG_PORT="${GEO_PG_PORT:-54329}"
export GEO_REDIS_PORT="${GEO_REDIS_PORT:-6389}"
export GEO_PORT="${GEO_PORT:-8080}"
export GEO_WEB_PORT="${GEO_WEB_PORT:-5173}"
export PGBIN="${PGBIN:-/opt/homebrew/opt/postgresql@17/bin}"
