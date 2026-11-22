#!/bin/bash
# Chạy toàn bộ kiểm chứng theo thứ tự: dựng lại -> truy vấn (EXPLAIN) -> quy mô lớn -> tách quyền -> pgbench -> RLS -> kích thước.
# Mất khoảng 6-10 phút trên máy 10 lõi. Chỉ cụm dự án cổng 54329.
set -e  # dừng ngay khi một bước lỗi
cd "$(dirname "$0")/.."
. bench/env.sh
./bench/00_reset.sh > bench/results_seed.txt 2>&1
mkdir -p bench/results
R=bench/results
psql -X -q -d $PGDATABASE -f bench/04_pgbench_setup.sql > $R/pgbench_setup.txt 2>&1
psql -X -q -d $PGDATABASE -c "SELECT pg_stat_reset(); SELECT pg_stat_statements_reset();" > /dev/null
psql -X -q -d $PGDATABASE -f bench/07_sizes.sql > $R/sizes_initial.txt 2>&1           # kích thước của bộ dữ liệu thử sạch (trước mọi thử tải)
./bench/run_queries.sh > $R/queries_summary.txt
python3 bench/lint_queries.py > $R/lint.txt || true
QDIR=bench/lint_selftest python3 bench/lint_queries.py > $R/lint_selftest.txt || true
psql -X -q -d $PGDATABASE -f bench/06_isolation_test.sql 2>&1 | grep -E "PASS|FAIL" | sed 's/^psql:[^ ]* NOTICE:  //' > $R/isolation_test.txt
psql -X -q -d $PGDATABASE -f bench/08_generic_plan_check.sql > $R/generic_plan_check.txt 2>&1
psql -X -q -d $PGDATABASE -f bench/09_antipatterns.sql > $R/antipatterns.txt 2>&1
psql -X -q -d $PGDATABASE -f bench/03_scale_checks.sql > $R/scale_checks.txt 2>&1
psql -X -q -d $PGDATABASE -f bench/10_event_index_cost.sql > $R/event_index_cost.txt 2>&1
psql -X -q -d $PGDATABASE -c "VACUUM (ANALYZE)" > /dev/null
./bench/run_pgbench.sh > $R/pgbench.txt 2>&1
./bench/run_rls.sh 2>&1 | grep -v NOTICE > $R/rls.txt
psql -X -q -d $PGDATABASE -f bench/07_sizes.sql > $R/sizes_after_load.txt 2>&1       # sau thử tải: bảng sự kiện đã phình do ghi của pgbench
echo "xong"
