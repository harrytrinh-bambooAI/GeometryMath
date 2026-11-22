#!/bin/bash
# Tạo geo_dev, geo_test, geo_e2e trên cụm riêng (cổng GEO_PG_PORT, mặc định 54329) nếu chưa có. Không đụng cổng 5432.
set -e
cd "$(dirname "$0")/.."
source scripts/env.sh
for db in geo_dev geo_test geo_e2e; do
  if ! $PGBIN/psql -h 127.0.0.1 -p $GEO_PG_PORT -U geo -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='$db'" | grep -q 1; then
    $PGBIN/createdb -h 127.0.0.1 -p $GEO_PG_PORT -U geo "$db"
    echo "đã tạo $db"
  fi
  $PGBIN/psql -h 127.0.0.1 -p $GEO_PG_PORT -U geo -d "$db" -qc "CREATE EXTENSION IF NOT EXISTS pg_stat_statements" >/dev/null
done
echo "db-init xong (cổng $GEO_PG_PORT)"
