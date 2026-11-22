#!/bin/bash
# Sinh DỮ LIỆU THỬ TẢI bằng một lệnh (US-GEO-108): scripts/bench-seed.sh [geo_bench]   (hoặc make bench-seed)
# Dựng lại CSDL thử tải riêng từ đầu (DROP + CREATE + mọi migration V*), rồi chạy db/bench/01_seed.sql:
# 50 trường, 600 lớp, 20.000 học sinh, 2.000.000 sự kiện. Chỉ chạy trên geo_bench hoặc geo_bench_* — không bao giờ
# ghi vào geo_dev / geo_test / geo_e2e (dữ liệu mẫu, FC1). In số đếm cuối cùng.
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/env.sh
DB="${1:-geo_bench}"
case "$DB" in
  geo_bench|geo_bench_*) ;;
  *) echo "LỖI: dữ liệu thử tải chỉ ghi vào CSDL geo_bench hoặc geo_bench_*, nhận: $DB" >&2; exit 2 ;;
esac
CONN=(-h 127.0.0.1 -p "$GEO_PG_PORT" -U geo)
"$PGBIN/dropdb" "${CONN[@]}" --if-exists "$DB"
"$PGBIN/createdb" "${CONN[@]}" "$DB"
PSQL=("$PGBIN/psql" "${CONN[@]}" -d "$DB" -v ON_ERROR_STOP=1 -qX)
# migration theo đúng thứ tự số phiên bản (V1, V2, V10, V11…)
for f in $(ls server/src/main/resources/db/migration/V*__*.sql | sed -E 's#^(.*/V)([0-9]+)(__.*)$#\2 &#' | sort -n | cut -d' ' -f2); do
  "${PSQL[@]}" -1 -f "$f" >/dev/null
done
"${PSQL[@]}" -f db/bench/01_seed.sql >/dev/null
"${PSQL[@]}" -tA -F' ' -c "SELECT 'schools', count(*) FROM org WHERE kind = 'school'
  UNION ALL SELECT 'classes', count(*) FROM class
  UNION ALL SELECT 'students', count(*) FROM app_user WHERE role = 'student'
  UNION ALL SELECT 'events', count(*) FROM event_log"
echo "bench-seed xong: $DB@$GEO_PG_PORT"
