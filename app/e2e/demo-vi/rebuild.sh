#!/bin/bash
# T-261128-76/77 (DEMO-VI): dựng lại video giới thiệu + bộ video hướng dẫn bằng MỘT lệnh, KHÔNG cần quay lại
# (dùng clip đã quay trong app/e2e/test-results/demo-vi-raw và demo-vi-guides-raw).
#   bash app/e2e/demo-vi/rebuild.sh [đường dẫn node_modules có ffmpeg-static + @ffprobe-installer]
# Giọng: tệp voice/<mã>.mp3|wav mới nhất thắng; dòng chưa có tệp giọng thì sinh giọng tạm macOS "Linh".
# Quay lại toàn bộ (khi giao diện đổi): thêm GEO_VI_RECORD=1 (cần demo/stack-up.sh đang chạy).
set -euo pipefail
cd "$(dirname "$0")/.."
NM="${1:-${GEO_FFMPEG_NM:-/Users/harry/.claude/jobs/74cc88b7/tmp/ffm/node_modules}}"
FF="$NM/ffmpeg-static/ffmpeg"
if [ "${GEO_VI_RECORD:-0}" = "1" ]; then
  source ../scripts/env.sh
  GEO_PORT=$GEO_PORT GEO_WEB_PORT=$GEO_WEB_PORT npx playwright test -c demo-vi/demo.config.ts
  GEO_PORT=$GEO_PORT GEO_WEB_PORT=$GEO_WEB_PORT npx playwright test -c demo-vi/guides/guides.config.ts
fi
GEO_VI_KEEP_PHOTOS=1 node demo-vi/make-assets-vi.mjs "$FF" >/dev/null
node demo-vi/gen-voice.mjs "$FF"
node demo-vi/make-video-vi.mjs "$NM"
node demo-vi/make-guides.mjs "$NM" all
