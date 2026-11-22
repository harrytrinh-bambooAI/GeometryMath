#!/bin/bash
# pgbench qua vai geo_app, giao thức prepared (kế hoạch tổng quát sau 5 lần như JDBC). Cảnh báo: bộ sinh tải và CSDL cùng một máy.
cd "$(dirname "$0")/.."
. bench/env.sh
export PGOPTIONS="-c role=geo_app"
python3 bench/pgbench/gen_nplus1.py
D=20   # giây mỗi phép đo
run() { pgbench -n -M prepared -c $1 -j $2 -T $D "${@:3}" $PGDATABASE 2> geo_arch 2>&11 | grep -E "latency average|tps =|failed|latency stddev" | tr '\n' ' '; echo; }
echo "### A. từng kịch bản riêng, 16 kết nối"
for s in login list open event_batch hint critical gate_only qa keyword tracking catalog; do printf "%-12s " $s; run 16 8 -f bench/pgbench/p_$s.sql; done
echo "### B. hỗn hợp theo mô hình 05_capacity.md (trọng số %): event_batch 35, qa 12, keyword 12, open 10, list 8, hint 8, critical 5, catalog 5, tracking 3, login 2; theo số kết nối"
MIX="-f bench/pgbench/p_event_batch.sql@35 -f bench/pgbench/p_qa.sql@12 -f bench/pgbench/p_keyword.sql@12 -f bench/pgbench/p_open.sql@10 -f bench/pgbench/p_list.sql@8 -f bench/pgbench/p_hint.sql@8 -f bench/pgbench/p_critical.sql@5 -f bench/pgbench/p_catalog.sql@5 -f bench/pgbench/p_tracking.sql@3 -f bench/pgbench/p_login.sql@2"
for c in 2 4 8 16 32 64 128 256; do printf "c=%-4s " $c; j=$((c<8?c:8)); run $c $j $MIX; done
echo "### C. đỉnh giờ mở: 1000 học sinh mở lời giải. Cổng quyết định thuần (đọc) và giao dịch đầy đủ (cổng + sự kiện bền + tiến độ)"
for c in 20 50 100; do printf "gate_only c=%-4s " $c; j=$((c<8?c:8)); run $c $j -f bench/pgbench/p_gate_only.sql; done
for c in 20 50 100; do printf "critical  c=%-4s " $c; j=$((c<8?c:8)); run $c $j -f bench/pgbench/p_critical.sql; done
echo "### D. mức chịu tải cố định (tốc độ giới hạn): hỗn hợp ở 500 giao dịch/giây, 32 kết nối, báo cáo độ trễ và tỉ lệ trễ"
pgbench -n -M prepared -c 32 -j 8 -T 30 -R 500 -L 50 $MIX $PGDATABASE 2> geo_arch 2>&11 | grep -E "latency average|tps =|skipped|late|failed|stddev"
echo "### E. đối chiếu: số kết nối vs thông lượng cho gate_only (đường nhẹ nhất)"
for c in 1 4 8 16 32 64 128; do printf "c=%-4s " $c; j=$((c<8?c:8)); run $c $j -f bench/pgbench/p_gate_only.sql; done
echo "### F. mẫu bị cấm N+1 so với một câu JOIN (cùng dữ liệu một trang; 10 bài x 2 câu lẻ = 20 câu + 2 câu chuẩn bị, so với 1 câu JOIN của Q02). Cục bộ qua TCP 127.0.0.1: mỗi vòng thêm vài chục micro giây; qua mạng thật mỗi vòng thêm 0,3 đến 1 ms"
printf "join   c=16  "; run 16 8 -f bench/pgbench/p_list.sql
printf "n+1    c=16  "; run 16 8 -f bench/pgbench/p_nplus1.sql
