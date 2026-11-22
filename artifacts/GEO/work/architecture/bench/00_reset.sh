#!/bin/bash
# Dựng lại cơ sở dữ liệu thử $PGDATABASE (mặc định geo_arch2; CHỈ cụm dự án cổng 54329) rồi nạp lược đồ, móc kiểm thử và dữ liệu.
set -e
cd "$(dirname "$0")/.."
. bench/env.sh
[ "$PGPORT" = "54329" ] || { echo "cổng sai"; exit 1; }
[ "$PGDATABASE" = "geo_arch" ] && { echo "không dựng lại geo_arch (bằng chứng của lượt trước)"; exit 1; }
psql -d postgres -q -c "DROP DATABASE IF EXISTS $PGDATABASE WITH (FORCE)" -c "CREATE DATABASE $PGDATABASE"
psql -d $PGDATABASE -v ON_ERROR_STOP=1 -q -f 03_schema.sql >/dev/null
psql -d $PGDATABASE -v ON_ERROR_STOP=1 -q -f bench/test_hooks.sql >/dev/null     # profile test (D-GEO-169)
psql -d $PGDATABASE -v ON_ERROR_STOP=1 -q -f bench/01_seed.sql
