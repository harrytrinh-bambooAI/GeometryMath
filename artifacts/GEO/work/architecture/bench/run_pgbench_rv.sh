#!/bin/bash
# pgbench rút gọn cho lượt sửa sau RV (đường ghi mới qua hàm SECURITY DEFINER). Vai geo_bench (= geo_app + geo_ai), giao thức prepared.
# Cảnh báo: bộ sinh tải và CSDL cùng một máy; 10 giây mỗi phép đo (lượt trước 20 giây). Chỉ cụm 54329, CSDL $PGDATABASE.
cd "$(dirname "$0")/.."
. bench/env.sh
[ "$PGDATABASE" = "geo_arch" ] && { echo "không chạy trên geo_arch"; exit 1; }
export PGOPTIONS="-c role=geo_bench"
D=10
run() { pgbench -n -M prepared -c $1 -j $2 -T $D "${@:3}" $PGDATABASE 2>&1 | grep -E "latency average|tps =|failed|latency stddev" | tr '\n' ' '; echo; }
echo "### A. từng kịch bản riêng, 16 kết nối"
for s in login list open event_batch hint critical gate_only qa keyword tracking catalog status; do printf "%-12s " $s; run 16 8 -f bench/pgbench/p_$s.sql; done
MIX="-f bench/pgbench/p_event_batch.sql@35 -f bench/pgbench/p_qa.sql@12 -f bench/pgbench/p_keyword.sql@12 -f bench/pgbench/p_open.sql@10 -f bench/pgbench/p_list.sql@8 -f bench/pgbench/p_hint.sql@8 -f bench/pgbench/p_critical.sql@5 -f bench/pgbench/p_catalog.sql@5 -f bench/pgbench/p_tracking.sql@3 -f bench/pgbench/p_login.sql@2"
echo "### B. hỗn hợp theo 05_capacity.md §2 (không gồm thăm dò Q04b), theo số kết nối"
for c in 8 16 32 64; do printf "c=%-4s " $c; run $c 8 $MIX; done
echo "### C. đỉnh giờ mở: cổng thuần (đọc) và giao dịch đầy đủ (cổng + unlock bền lũy đẳng)"
for c in 20 50 100; do printf "gate_only c=%-4s " $c; run $c 8 -f bench/pgbench/p_gate_only.sql; done
for c in 20 50 100; do printf "critical  c=%-4s " $c; run $c 8 -f bench/pgbench/p_critical.sql; done
echo "### D. mức chịu tải cố định: hỗn hợp + thăm dò Q04b thường trực ở 650 giao dịch/giây (576 nhu cầu đỉnh mới, 05_capacity.md §2), 32 kết nối, 30 giây"
pgbench -n -M prepared -c 32 -j 8 -T 30 -R 650 -L 50 $MIX -f bench/pgbench/p_status.sql@30 $PGDATABASE 2>&1 | grep -E "latency average|tps =|skipped|late|failed|stddev"
