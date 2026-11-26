#!/bin/bash
# Tắt PostgreSQL + Redis của THƯ MỤC NÀY (cổng từ scripts/env.sh). Không đụng cụm của checkout khác.
cd "$(dirname "$0")/.."
source scripts/env.sh
$PGBIN/pg_ctl -D "$PWD/.data/pg" -m fast stop 2>&1 | tail -1
rdir=$(redis-cli -p $GEO_REDIS_PORT config get dir 2>/dev/null | tail -1)
if [ -n "$rdir" ] && [ "$(cd "$rdir" 2>/dev/null && pwd -P)" = "$(cd "$PWD/.data" && pwd -P)" ]; then
  redis-cli -p $GEO_REDIS_PORT shutdown nosave 2>/dev/null
  # chờ Redis thật sự nhả cổng (máy tải cao: `make up` ngay sau có thể gặp tiến trình đang tắt) — tối đa GEO_DEV_UP_WAIT giây
  end=$((SECONDS + ${GEO_DEV_UP_WAIT:-60}))
  while redis-cli -p $GEO_REDIS_PORT ping >/dev/null 2>&1 && [ $SECONDS -lt $end ]; do sleep 0.5; done
fi
echo stopped
