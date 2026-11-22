#!/bin/bash
# Tắt PostgreSQL + Redis của THƯ MỤC NÀY (cổng từ scripts/env.sh). Không đụng cụm của checkout khác.
cd "$(dirname "$0")/.."
source scripts/env.sh
$PGBIN/pg_ctl -D "$PWD/.data/pg" -m fast stop 2>&1 | tail -1
rdir=$(redis-cli -p $GEO_REDIS_PORT config get dir 2>/dev/null | tail -1)
if [ -n "$rdir" ] && [ "$(cd "$rdir" 2>/dev/null && pwd -P)" = "$(cd "$PWD/.data" && pwd -P)" ]; then
  redis-cli -p $GEO_REDIS_PORT shutdown nosave 2>/dev/null
fi
echo stopped
