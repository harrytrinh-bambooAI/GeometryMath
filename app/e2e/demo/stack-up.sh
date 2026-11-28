#!/bin/bash
# T-261128-73 (DEMO): bật hạ tầng + máy chủ (profile dev, AI giả lập, DB geo_e2e) + web (bản build e2e, vite preview)
# trên cổng của worktree (app/.env.ports) để quay video giới thiệu. KHÔNG thuộc bộ test e2e.
# Tắt: kill PID trong .data/demo/*.pid rồi scripts/dev-down.sh (xem stack-down.sh).
# Cờ chặng 5 (geo.s5.grading, geo.s5.ai-grading) bật sẵn trong application-dev.yml: chỉ phiên dev local này.
set -euo pipefail
cd "$(dirname "$0")/../.."
source scripts/env.sh
LOG="$PWD/.data/demo"; mkdir -p "$LOG"
unset GEO_AI_PROVIDER GEO_AI_API_KEY   # không gọi AI thật
scripts/dev-up.sh >/dev/null
scripts/db-init.sh >/dev/null
export GEO_E2E_REDIS_PREFIX="${GEO_E2E_REDIS_PREFIX:-geoe2e:}"
GEO_DB_URL="jdbc:postgresql://127.0.0.1:$GEO_PG_PORT/geo_e2e?ApplicationName=geo-demo&connectTimeout=3&socketTimeout=30" \
GEO_REDIS_PREFIX="$GEO_E2E_REDIS_PREFIX" GEO_CONTENT_PACK_ON_START=false \
  nohup java -jar server/target/geo-server-0.1.0-SNAPSHOT.jar --spring.profiles.active=dev \
    "--spring.datasource.hikari.connection-init-sql=SET statement_timeout = 10000" \
    "--spring.data.redis.timeout=1000ms" >"$LOG/server.log" 2>&1 &
echo $! > "$LOG/server.pid"
(cd web && nohup node node_modules/vite/bin/vite.js preview --strictPort --outDir dist-e2e >"$LOG/web.log" 2>&1 & echo $! > "$LOG/web.pid")
for _ in $(seq 1 240); do curl -fsS -o /dev/null "http://127.0.0.1:$GEO_PORT/health" 2>/dev/null && break; sleep 1; done
curl -fsS -o /dev/null "http://127.0.0.1:$GEO_PORT/health" || { echo "máy chủ không lên, xem $LOG/server.log" >&2; exit 1; }
for _ in $(seq 1 60); do curl -fsS -o /dev/null "http://127.0.0.1:$GEO_WEB_PORT/" 2>/dev/null && break; sleep 1; done
scripts/seed-reset.sh geo_e2e | tail -2
echo "demo stack: api http://127.0.0.1:$GEO_PORT · web http://127.0.0.1:$GEO_WEB_PORT"
