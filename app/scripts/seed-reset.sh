#!/bin/bash
# Đưa cơ sở dữ liệu E2E về TRẠNG THÁI VÀNG (fixture), chạy được nhiều lần, không cần khởi động lại máy chủ.
#   scripts/seed-reset.sh [geo_e2e|geo_dev]      (mặc định geo_e2e)
# Các bước, trong MỘT giao dịch:
#   1. TRUNCATE ... CASCADE mọi bảng của schema public, trừ db/seed/keep-tables.txt (lịch sử Flyway, danh mục do migration nạp)
#   2. chạy lần lượt db/seed/e2e/*.sql theo tên, với biến psql :'t0' = giờ máy chủ lúc reset (T0 của fixture)
# rồi xóa khóa Redis có tiền tố GEO_E2E_REDIS_PREFIX (mặc định geoe2e:).
# Schema do máy chủ tạo (Flyway khi khởi động); seed KHÔNG nằm trong migration nên không bao giờ vào môi trường thật.
# In ra dòng "T0=<ISO-8601 UTC>" để test đọc.
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/env.sh
DB="${1:-geo_e2e}"
case "$DB" in
  geo_e2e|geo_dev) ;;
  *) echo "LỖI: seed-reset chỉ chạy trên geo_e2e hoặc geo_dev, nhận: $DB" >&2; exit 2 ;;
esac
PSQL=("$PGBIN/psql" -h 127.0.0.1 -p "$GEO_PG_PORT" -U geo -d "$DB" -v ON_ERROR_STOP=1 -qtA)

if [ "$("${PSQL[@]}" -c "SELECT to_regclass('public.flyway_schema_history') IS NOT NULL")" != "t" ]; then
  echo "LỖI: $DB chưa có schema (chưa chạy Flyway). Khởi động máy chủ trỏ vào $DB trước (make e2e làm việc này)." >&2
  exit 3
fi

T0=$("${PSQL[@]}" -c "SELECT to_char(now() AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"')")
KEEP=$(grep -v '^\s*#' db/seed/keep-tables.txt | grep -v '^\s*$' | sed "s/.*/'&'/" | paste -sd, -)

{
  echo "BEGIN;"
  cat <<SQL
DO \$\$
DECLARE t text;
BEGIN
  SELECT string_agg(format('%I.%I', schemaname, tablename), ', ') INTO t
  FROM pg_tables WHERE schemaname = 'public' AND tablename NOT IN ($KEEP);
  IF t IS NOT NULL THEN EXECUTE 'TRUNCATE ' || t || ' RESTART IDENTITY CASCADE'; END IF;
END \$\$;
SQL
  for f in db/seed/e2e/*.sql; do
    [ -e "$f" ] || continue
    echo "\\echo seed: $f"
    cat "$f"
    echo
  done
  echo "COMMIT;"
} | "${PSQL[@]}" -v t0="$T0" -f -

PREFIX="${GEO_E2E_REDIS_PREFIX:-geoe2e:}"
n=0
while read -r k; do
  [ -n "$k" ] || continue
  redis-cli -p "$GEO_REDIS_PORT" DEL "$k" >/dev/null; n=$((n + 1))
done < <(redis-cli -p "$GEO_REDIS_PORT" --scan --pattern "${PREFIX}*")
echo "seed-reset xong: $DB@$GEO_PG_PORT, xóa $n khóa Redis '${PREFIX}*'"
echo "T0=$T0"
