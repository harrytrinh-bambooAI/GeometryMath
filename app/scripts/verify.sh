#!/bin/bash
# Kiểm tra trọn bộ: hạ tầng -> định dạng (Spotless) -> build -> kiểm thử đơn vị -> quét SQL -> kiểm thử kiến trúc
# -> kiểm thử tích hợp (geo_test@GEO_PG_PORT, Redis GEO_REDIS_PORT; mặc định 54329/6389). Mã thoát khác 0 nếu bất kỳ bước nào đỏ.
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/env.sh   # cổng của checkout này (app/.env.ports), truyền xuống Maven qua biến môi trường
scripts/dev-up.sh
scripts/db-init.sh
cd server
./mvnw -B -ntp verify "$@"
echo "VERIFY OK"
