#!/bin/bash
# So sánh chi phí RLS: (1) cơ sở không RLS, không set_config; (2) có set_config + giao dịch bọc, RLS tắt; (3) RLS bật.
cd "$(dirname "$0")/.."
. bench/env.sh
export PGOPTIONS="-c role=geo_app"
D=15
python3 bench/gen_rls_variants.py
run() { env PGOPTIONS="-c role=geo_app" pgbench -n -M prepared -c 16 -j 8 -T $D -f "$1" $PGDATABASE 2> geo_arch 2>&11 | grep -E "tps =|latency average" | tr '\n' ' '; echo; }
PGOPTIONS= psql -X -q -d $PGDATABASE -f bench/05_rls_off.sql
for s in list tracking event_batch open; do
  echo "### $s"
  printf "1 cơ sở (không RLS)         "; run bench/pgbench/p_$s.sql
  printf "2 set_config, RLS tắt       "; run bench/pgbench/rls/p_$s.sql
  PGOPTIONS= psql -X -q -d $PGDATABASE -f bench/05_rls_on.sql
  printf "3 RLS bật                   "; run bench/pgbench/rls/p_$s.sql
  PGOPTIONS= psql -X -q -d $PGDATABASE -f bench/05_rls_off.sql
done
echo "### Kiểm cô lập khi RLS bật"
PGOPTIONS= psql -X -q -d $PGDATABASE -f bench/05_rls_on.sql
PGOPTIONS= psql -X -q -d $PGDATABASE -v ON_ERROR_STOP=0 -f bench/05_rls_check.sql 2>&1 | grep -v "^$"
PGOPTIONS= psql -X -q -d $PGDATABASE -f bench/05_rls_off.sql
echo "### EXPLAIN Q02 với RLS bật (kế hoạch)"
PGOPTIONS= psql -X -q -d $PGDATABASE -f bench/05_rls_on.sql
PGOPTIONS= psql -X -q -d $PGDATABASE -f bench/05_rls_explain.sql 2>&1
PGOPTIONS= psql -X -q -d $PGDATABASE -f bench/05_rls_off.sql
