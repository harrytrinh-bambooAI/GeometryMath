#!/bin/bash
# Chạy TRỌN BỘ E2E có bằng chứng (make e2e):
#   hạ tầng riêng (dev-up, db-init) -> build máy chủ + web -> bật máy chủ (geo_e2e, cổng GEO_PORT) và web
#   (vite preview, cổng GEO_WEB_PORT) -> Playwright (globalSetup reset seed vàng) (api + ui) -> báo cáo bằng chứng
#   -> traceability -> tắt máy chủ/web do script này bật.
# Bằng chứng: e2e/evidence/<run>/index.html, summary.json, traceability.txt (run = GEO_E2E_RUN hoặc giờ VN).
# Tham số thêm truyền thẳng cho Playwright, ví dụ: scripts/e2e.sh --project=api  hoặc  -g @TC-GEO-0100
# GEO_E2E_SKIP_BUILD=1: dùng jar và web/dist có sẵn.
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/env.sh
APP="$PWD"
LOGDIR="$APP/.data/e2e"; mkdir -p "$LOGDIR"
export GEO_E2E_RUN="${GEO_E2E_RUN:-$(TZ=Asia/Ho_Chi_Minh date +%Y%m%d-%H%M%S)}"
export GEO_E2E_REDIS_PREFIX="${GEO_E2E_REDIS_PREFIX:-geoe2e:}"
API_URL="http://127.0.0.1:$GEO_PORT"
WEB_URL="http://127.0.0.1:$GEO_WEB_PORT"

port_busy() { lsof -nP -iTCP:"$1" -sTCP:LISTEN >/dev/null 2>&1; }
for p in "$GEO_PORT" "$GEO_WEB_PORT"; do
  if port_busy "$p"; then
    echo "LỖI: cổng $p đang bận. make e2e tự bật máy chủ/web của nó; tắt tiến trình đang chiếm cổng hoặc đổi GEO_PORT/GEO_WEB_PORT." >&2
    exit 1
  fi
done

scripts/dev-up.sh >/dev/null
scripts/db-init.sh >/dev/null

if [ "${GEO_E2E_SKIP_BUILD:-0}" != "1" ]; then
  echo "== build máy chủ"; (cd server && ./mvnw -B -ntp -q -DskipTests package)
  echo "== build web (móc kiểm thử __geoScene bật, PO-52)"; (cd web && { [ -d node_modules ] || npm ci --no-audit --no-fund; } && VITE_GEO_TEST_HOOKS=1 npm run build >/dev/null)
fi
(cd e2e && { [ -d node_modules ] || npm ci --no-audit --no-fund; })

PIDS=()
cleanup() {
  for pid in "${PIDS[@]:-}"; do [ -n "$pid" ] && kill "$pid" 2>/dev/null || true; done
  wait 2>/dev/null || true
}
trap cleanup EXIT

wait_http() { # url, tên, số giây
  for _ in $(seq 1 "$3"); do curl -fsS -o /dev/null "$1" 2>/dev/null && return 0; sleep 1; done
  echo "LỖI: $2 không lên sau $3 giây ($1). Xem $LOGDIR/*.log" >&2; return 1
}

echo "== bật máy chủ $API_URL (geo_e2e@$GEO_PG_PORT, Redis $GEO_REDIS_PORT '$GEO_E2E_REDIS_PREFIX')"
GEO_DB_URL="jdbc:postgresql://127.0.0.1:$GEO_PG_PORT/geo_e2e?ApplicationName=geo-e2e&connectTimeout=3&socketTimeout=30" \
GEO_REDIS_PREFIX="$GEO_E2E_REDIS_PREFIX" \
  java -jar server/target/geo-server-0.1.0-SNAPSHOT.jar --spring.profiles.active=dev >"$LOGDIR/server.log" 2>&1 &
PIDS+=($!)
wait_http "$API_URL/health" "máy chủ" 90

echo "== bật web $WEB_URL (vite preview, proxy -> $API_URL)"
(cd web && exec node node_modules/vite/bin/vite.js preview --strictPort >"$LOGDIR/web.log" 2>&1) &
PIDS+=($!)
wait_http "$WEB_URL/" "web" 30

echo "== Playwright (run $GEO_E2E_RUN)"
set +e
(cd e2e && GEO_E2E_BASE_URL="$API_URL" GEO_E2E_WEB_URL="$WEB_URL" node node_modules/@playwright/test/cli.js test "$@")
rc=$?
set -e

(cd e2e && node scripts/traceability.ts --run "$GEO_E2E_RUN" | tee "evidence/$GEO_E2E_RUN/traceability.txt" | head -40) || true
echo "Báo cáo bằng chứng: $APP/e2e/evidence/$GEO_E2E_RUN/index.html"
exit $rc
