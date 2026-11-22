#!/bin/bash
# Kiểm tra trọn bộ: hạ tầng -> định dạng (Spotless) -> build -> kiểm thử đơn vị -> quét SQL -> kiểm thử kiến trúc
# -> kiểm thử tích hợp (geo_test@54329, Redis 6389). Mã thoát khác 0 nếu bất kỳ bước nào đỏ.
set -euo pipefail
cd "$(dirname "$0")/.."
scripts/dev-up.sh
scripts/db-init.sh
cd server
./mvnw -B -ntp verify "$@"
echo "VERIFY OK"
