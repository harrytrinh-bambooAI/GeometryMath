#!/bin/bash
# In (và mở, nếu có lệnh `open`) báo cáo bằng chứng E2E của lượt mới nhất hoặc lượt chỉ định: scripts/e2e-report.sh [run]
set -euo pipefail
cd "$(dirname "$0")/../e2e"
run="${1:-$(cat evidence/latest.txt 2>/dev/null || true)}"
if [ -z "$run" ] || [ ! -f "evidence/$run/index.html" ]; then
  echo "Chưa có lượt E2E nào (chạy: make e2e)" >&2
  exit 1
fi
node scripts/traceability.ts --run "$run" | head -12
echo
echo "Báo cáo: $PWD/evidence/$run/index.html"
if [ -z "${CI:-}" ] && command -v open >/dev/null 2>&1; then open "evidence/$run/index.html"; fi
