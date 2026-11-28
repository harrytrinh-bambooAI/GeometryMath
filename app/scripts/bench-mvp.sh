#!/bin/bash
# Bộ đo MVP (MVP.md §9, PO-60): chạy MỌI đề của bộ đề chuẩn qua cổng AI + engine + Tự học + bộ câu thử,
# xuất artifacts/GEO/evidence/MVP-METRICS-<yyyymmdd-HHmm>/report.md + report.json. Mã thoát ≠ 0 nếu một
# trong bốn chỉ số bắt buộc trượt. Không nằm trong `make verify` (lớp MvpBench không khớp mẫu *IT/*Test).
#
# Nhà cung cấp AI theo biến môi trường (mặc định mock, dùng cho CI):
#   make bench-mvp                                                   # mock
#   GEO_AI_PROVIDER=anthropic GEO_AI_API_KEY=... make bench-mvp      # AI thật (model: GEO_AI_MODEL, mặc định claude-sonnet-5-5)
# Thư mục xuất khác: GEO_BENCH_OUT=/đường/dẫn. Dùng geo_test của checkout này (cổng app/.env.ports).
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/env.sh
if [ "${GEO_AI_PROVIDER:-mock}" = "anthropic" ]; then
  : "${GEO_AI_API_KEY:?GEO_AI_API_KEY chưa đặt (bắt buộc khi GEO_AI_PROVIDER=anthropic)}"
  export GEO_AI_TIMEOUT="${GEO_AI_TIMEOUT:-90s}"   # AI thật chậm hơn trần 10 s của dev/test
fi
scripts/dev-up.sh
scripts/db-init.sh
cd server
./mvnw -B -ntp verify \
  -Dtest=NoSuchTest -Dsurefire.failIfNoSpecifiedTests=false \
  -Dit.test=MvpBench -Dfailsafe.failIfNoSpecifiedTests=false "$@"
echo "BENCH-MVP OK (chỉ số bắt buộc đạt)"
