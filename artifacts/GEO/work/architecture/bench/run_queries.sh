#!/bin/bash
# Chạy mọi truy vấn trong bench/queries bằng EXPLAIN (ANALYZE, BUFFERS) với vai geo_app (hoặc owner nếu tệp ghi "-- role: owner").
# Mỗi truy vấn: 3 lần khởi động (bộ nhớ đệm ấm) rồi 7 lần đo; lấy lần đo có thời gian thực thi TRUNG VỊ làm kết quả.
# Tùy chọn: PLAN_CACHE=generic để ép kế hoạch tổng quát (giống JDBC sau 5 lần chuẩn bị): kết quả vào bench/results_generic/.
# Chỉ chạy được trên cụm dự án (cổng 54329), cơ sở dữ liệu $PGDATABASE (mặc định geo_arch2).
cd "$(dirname "$0")/.."
. bench/env.sh
OUT=bench/results; EXTRA=""
if [ "$PLAN_CACHE" = "generic" ]; then OUT=bench/results_generic; EXTRA="SET plan_cache_mode = force_generic_plan;"; fi
mkdir -p $OUT; : > $OUT/summary.tsv; rm -f $OUT/.tmp*
for f in bench/queries/Q*.sql; do
  n=$(basename $f .sql)
  setrole="SET ROLE geo_app;"; grep -q '^-- role: owner' $f && setrole="RESET ROLE;"
  {
    echo '\o /dev/null'; echo '\i bench/02_params.sql'; echo "$setrole"; echo "$EXTRA"; echo "\\set explain ''"
    for i in 1 2 3; do echo "BEGIN;"; echo "\\i $f"; echo "ROLLBACK;"; done
    echo "\\set explain 'EXPLAIN (ANALYZE, BUFFERS)'"
    for i in 1 2 3 4 5 6 7; do echo "\\o $OUT/.tmp$i"; echo "BEGIN;"; echo "\\i $f"; echo "ROLLBACK;"; done
  } | psql -X -q -d $PGDATABASE -v ON_ERROR_STOP=1 > $OUT/.err 2>&1
  best=$(for i in 1 2 3 4 5 6 7; do echo "$(grep -E '^ *Execution Time' $OUT/.tmp$i | tail -1 | awk '{print $3}') $i"; done | sort -n | sed -n 4p | awk '{print $2}')
  [ -z "$best" ] && best=1
  cp $OUT/.tmp$best $OUT/$n.txt; [ -s $OUT/.err ] && cat $OUT/.err >> $OUT/$n.txt
  ex=$(grep -E '^ *Execution Time' $OUT/$n.txt | tail -1 | awk '{print $3}')
  pl=$(grep -E '^ *Planning Time' $OUT/$n.txt | tail -1 | awk '{print $3}')
  seq=$(grep -c 'Seq Scan' $OUT/$n.txt)
  top=$(grep -oE '(Index Only Scan|Index Scan|Bitmap Heap Scan|Seq Scan|Insert|Update|Limit|Sort|Nested Loop|Result)' $OUT/$n.txt | awk '!s[$0]++' | paste -sd, -)
  hit=$(grep -oE 'shared hit=[0-9]+' $OUT/$n.txt | head -1 | cut -d= -f2)
  echo -e "$n\t$ex\t$pl\t$seq\t$hit\t$top" >> $OUT/summary.tsv
done
rm -f $OUT/.tmp* $OUT/.err
column -t -s$'\t' $OUT/summary.tsv
