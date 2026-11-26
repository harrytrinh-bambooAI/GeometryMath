#!/bin/bash
# Chạy TRỌN BỘ E2E có bằng chứng (make e2e):
#   hạ tầng riêng (dev-up, db-init) -> build máy chủ + web -> bật máy chủ (geo_e2e, cổng GEO_PORT) và web
#   (vite preview, cổng GEO_WEB_PORT) -> Playwright (globalSetup reset seed vàng) (api + ui) -> báo cáo bằng chứng
#   -> traceability -> tắt máy chủ/web do script này bật.
# Bằng chứng: e2e/evidence/<run>/index.html, summary.json, traceability.txt (run = GEO_E2E_RUN hoặc giờ VN).
# Tham số thêm truyền thẳng cho Playwright, ví dụ: scripts/e2e.sh --project=api  hoặc  -g @TC-GEO-0100
# GEO_E2E_SKIP_BUILD=1: dùng jar và web/dist-e2e có sẵn. Bản web của E2E (có móc __geoScene, PO-52) ghi vào web/dist-e2e, không đè web/dist (production).
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/env.sh
APP="$PWD"
LOGDIR="$APP/.data/e2e"; mkdir -p "$LOGDIR"
export GEO_E2E_RUN="${GEO_E2E_RUN:-$(TZ=Asia/Ho_Chi_Minh date +%Y%m%d-%H%M%S)}"
export GEO_E2E_REDIS_PREFIX="${GEO_E2E_REDIS_PREFIX:-geoe2e:}"
API_URL="http://127.0.0.1:$GEO_PORT"

# Khóa toàn máy: chỉ MỘT bộ E2E chạy cùng lúc trên máy (nhiều worktree chạy song song làm tải ~48, mỗi lượt 30 phút
# và trượt rải rác do quá thời gian; chạy lần lượt ~8 phút/lượt, xanh). GEO_E2E_NO_LOCK=1 để bỏ khóa.
LOCK_DIR="${GEO_E2E_LOCK_DIR:-$HOME/.cache/geo-e2e.lock}"
release_lock() { [ "${HAVE_LOCK:-0}" = "1" ] && rm -rf "$LOCK_DIR" 2>/dev/null; HAVE_LOCK=0; }
if [ "${GEO_E2E_NO_LOCK:-0}" != "1" ]; then
  mkdir -p "$(dirname "$LOCK_DIR")"
  waited=0
  until mkdir "$LOCK_DIR" 2>/dev/null; do
    holder=$(cat "$LOCK_DIR/pid" 2>/dev/null || true)
    if [ -n "$holder" ] && ! kill -0 "$holder" 2>/dev/null; then rm -rf "$LOCK_DIR"; continue; fi  # khóa mồ côi
    [ $((waited % 60)) -eq 0 ] && echo "e2e: chờ khóa toàn máy (đang chạy: $(cat "$LOCK_DIR/where" 2>/dev/null || echo '?')) ${waited}s" >&2
    sleep 10; waited=$((waited + 10))
    [ "$waited" -ge 5400 ] && { echo "LỖI: chờ khóa e2e quá 90 phút ($LOCK_DIR)" >&2; exit 1; }
  done
  HAVE_LOCK=1; echo $$ > "$LOCK_DIR/pid"; echo "$APP" > "$LOCK_DIR/where"
  trap release_lock EXIT
fi
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
  echo "== build web (móc kiểm thử __geoScene bật, PO-52)"; (cd web && { [ -d node_modules ] || npm ci --no-audit --no-fund; } && npm run build:e2e >/dev/null)
fi
(cd e2e && { [ -d node_modules ] || npm ci --no-audit --no-fund; })

PIDS=()
cleanup() {
  for pid in "${PIDS[@]:-}"; do [ -n "$pid" ] && kill "$pid" 2>/dev/null || true; done
  wait 2>/dev/null || true
  release_lock
}
trap cleanup EXIT

wait_http() { # url, tên, số giây
  for _ in $(seq 1 "$3"); do curl -fsS -o /dev/null "$1" 2>/dev/null && return 0; sleep 1; done
  echo "LỖI: $2 không lên sau $3 giây ($1). Xem $LOGDIR/*.log" >&2; return 1
}

# Trần CSDL cho máy chủ E2E (T-261126-43). Giá trị sản phẩm (statement_timeout 2 s, chờ kết nối 2 s; ADR-GEO-04/12,
# application.yml) được kiểm ở tầng tích hợp máy chủ; E2E kiểm HÀNH VI. Máy dùng chung tải cao (load 36–43 trên 10 lõi,
# 26/11) làm SELECT một dòng mất 1–2,5 s (pg.log s23-base) -> 504 TIMEOUT / 503 giả ở TC-GEO-0428, 0617.
# Không e2e nào kiểm 504/503 do trần này. Đặt GEO_E2E_DB_STATEMENT_MS=2000 GEO_E2E_DB_CONN_MS=2000 để chạy đúng trần thật.
E2E_STMT_MS="${GEO_E2E_DB_STATEMENT_MS:-10000}"
E2E_CONN_MS="${GEO_E2E_DB_CONN_MS:-10000}"
# Lệnh Redis: sản phẩm 100 ms (ADR-GEO-05). Khi tải cao, lệnh Redis quá 100 ms -> 504 "Redis command timed out" ở
# /test-hooks/e7/restart và cả POST gợi ý (TC-GEO-0722, 26/11 17:48). E2E dùng 1000 ms; GEO_E2E_REDIS_MS=100 = trần thật.
E2E_REDIS_MS="${GEO_E2E_REDIS_MS:-1000}"
echo "== bật máy chủ $API_URL (geo_e2e@$GEO_PG_PORT, Redis $GEO_REDIS_PORT '$GEO_E2E_REDIS_PREFIX')"
GEO_DB_URL="jdbc:postgresql://127.0.0.1:$GEO_PG_PORT/geo_e2e?ApplicationName=geo-e2e&connectTimeout=3&socketTimeout=30" \
GEO_REDIS_PREFIX="$GEO_E2E_REDIS_PREFIX" \
GEO_CONTENT_PACK_ON_START=false \
  java -jar server/target/geo-server-0.1.0-SNAPSHOT.jar --spring.profiles.active=dev \
    "--spring.datasource.hikari.connection-init-sql=SET statement_timeout = $E2E_STMT_MS; SET idle_in_transaction_session_timeout = 10000" \
    "--spring.datasource.hikari.connection-timeout=$E2E_CONN_MS" \
    "--spring.data.redis.timeout=${E2E_REDIS_MS}ms" >"$LOGDIR/server.log" 2>&1 &
PIDS+=($!)
wait_http "$API_URL/health" "máy chủ" "${GEO_E2E_SERVER_WAIT:-180}"

echo "== bật web $WEB_URL (vite preview, proxy -> $API_URL)"
(cd web && exec node node_modules/vite/bin/vite.js preview --strictPort --outDir dist-e2e >"$LOGDIR/web.log" 2>&1) &
PIDS+=($!)
wait_http "$WEB_URL/" "web" "${GEO_E2E_WEB_WAIT:-60}"

echo "== Playwright (run $GEO_E2E_RUN)"
set +e
(cd e2e && GEO_E2E_BASE_URL="$API_URL" GEO_E2E_WEB_URL="$WEB_URL" node node_modules/@playwright/test/cli.js test "$@")
rc=$?
set -e

(cd e2e && node scripts/traceability.ts --run "$GEO_E2E_RUN" | tee "evidence/$GEO_E2E_RUN/traceability.txt" | head -40) || true
echo "Báo cáo bằng chứng: $APP/e2e/evidence/$GEO_E2E_RUN/index.html"
exit $rc
