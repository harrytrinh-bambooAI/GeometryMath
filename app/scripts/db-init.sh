#!/bin/bash
# Tạo cơ sở dữ liệu geo_dev và geo_test trên cụm riêng (cổng 54329) nếu chưa có. Không đụng cổng 5432.
set -e
PGBIN=/opt/homebrew/opt/postgresql@17/bin
for db in geo_dev geo_test; do
  if ! $PGBIN/psql -h 127.0.0.1 -p 54329 -U geo -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='$db'" | grep -q 1; then
    $PGBIN/createdb -h 127.0.0.1 -p 54329 -U geo "$db"
    echo "đã tạo $db"
  fi
  $PGBIN/psql -h 127.0.0.1 -p 54329 -U geo -d "$db" -qc "CREATE EXTENSION IF NOT EXISTS pg_stat_statements" >/dev/null
done
echo "db-init xong"
