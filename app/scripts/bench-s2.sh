#!/bin/bash
# Bộ đo hình học phẳng S2-E2 (US-S2E2-05, T-261126-19): nạp bộ đề chuẩn hình phẳng (30 đề PLS-nn,
# server/src/main/resources/content/plane/plane-benchmark.json), chạy mọi đề qua khung môn học với AI GIẢ LẬP,
# tính ba chỉ số: M-S2-01 thỏa ràng buộc ≥ 95 %, M-S2-02 đáp số khớp mẫu ≥ 90 %, M-S2-03 sai mang nhãn = 0.
# Xuất artifacts/GEO/evidence/S2-BENCH-<yyyymmdd-HHmm>/report.md + report.json (GEO_BENCH_OUT để đổi thư mục).
# Mã thoát 0 khi cả ba ngưỡng đạt; khác 0 khi rớt một ngưỡng, bộ đề lỗi hoặc rỗng.
# Từ chối chạy khi cấu hình có AI thật (BR-S2E2-17): GEO_AI_PROVIDER khác mock hoặc có GEO_AI_API_KEY.
# Không nằm trong `make verify` (lớp PlaneBench không khớp mẫu *IT/*Test). Gọi qua `make bench-s2`.
set -euo pipefail
cd "$(dirname "$0")/.."
if [ "${GEO_AI_PROVIDER:-mock}" != "mock" ] || [ -n "${GEO_AI_API_KEY:-}" ]; then
  echo "bench-s2: chỉ chạy với AI giả lập (bỏ GEO_AI_PROVIDER/GEO_AI_API_KEY). Không chạy, không gọi mạng." >&2
  exit 3
fi
source scripts/env.sh
scripts/dev-up.sh
scripts/db-init.sh
cd server
./mvnw -B -ntp verify \
  -Dtest=NoSuchTest -Dsurefire.failIfNoSpecifiedTests=false \
  -Dit.test=PlaneBench -Dfailsafe.failIfNoSpecifiedTests=false "$@"
echo "BENCH-S2 OK (ba ngưỡng đạt)"
