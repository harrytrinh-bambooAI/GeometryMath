#!/bin/bash
# Dựng PostgreSQL và Redis RIÊNG của dự án (không đụng tới bản cài sẵn trên máy). Dữ liệu nằm trong app/.data
# Cổng đọc từ biến môi trường / app/.env.ports (scripts/env.sh); mặc định PG 54329, Redis 6389.
set -e
cd "$(dirname "$0")/.."
source scripts/env.sh
PGDATA="$PWD/.data/pg"; PGPORT=$GEO_PG_PORT; RPORT=$GEO_REDIS_PORT
# Thư mục socket Unix: macOS giới hạn ~103 ký tự đường dẫn; worktree lồng sâu thì dùng /tmp
SOCKDIR="$PWD/.data"
if [ ${#SOCKDIR} -gt 80 ]; then SOCKDIR="/tmp/geo-pg-$PGPORT"; fi
mkdir -p "$SOCKDIR" "$PWD/.data"
if [ ! -d "$PGDATA" ]; then
  $PGBIN/initdb -D "$PGDATA" -U geo --auth=trust -E UTF8 --locale=C.UTF-8 >/dev/null
  cat >> "$PGDATA/postgresql.conf" <<CONF
port = $PGPORT
listen_addresses = '127.0.0.1'
unix_socket_directories = '$SOCKDIR'
max_connections = 300
shared_buffers = 2GB
effective_cache_size = 8GB
work_mem = 16MB
maintenance_work_mem = 512MB
wal_compression = on
max_wal_size = 4GB
checkpoint_completion_target = 0.9
random_page_cost = 1.1
effective_io_concurrency = 0
track_io_timing = on
shared_preload_libraries = 'pg_stat_statements'
pg_stat_statements.track = all
log_min_duration_statement = 250
jit = off
CONF
fi
# -p/-k ghi đè postgresql.conf: đổi cổng không cần initdb lại
$PGBIN/pg_ctl -D "$PGDATA" -l "$PWD/.data/pg.log" -o "-p $PGPORT -k $SOCKDIR" -w start >/dev/null 2>&1 || true
$PGBIN/pg_isready -h 127.0.0.1 -p $PGPORT
# Chốt chặn: cổng phải do CỤM CỦA THƯ MỤC NÀY phục vụ (không lẫn sang cụm của checkout khác)
actual=$($PGBIN/psql -h 127.0.0.1 -p $PGPORT -U geo -d postgres -tAc "SHOW data_directory")
if [ "$(cd "$actual" && pwd -P)" != "$(cd "$PGDATA" && pwd -P)" ]; then
  echo "LỖI: cổng $PGPORT đang do cụm khác phục vụ ($actual), không phải $PGDATA. Đặt GEO_PG_PORT khác (xem README)." >&2
  exit 1
fi
if ! redis-cli -p $RPORT ping >/dev/null 2>&1; then
  redis-server --port $RPORT --bind 127.0.0.1 --dir "$PWD/.data" --save "" --appendonly no --maxmemory 512mb --maxmemory-policy allkeys-lru --daemonize yes >/dev/null
fi
rdir=$(redis-cli -p $RPORT config get dir | tail -1)
if [ "$(cd "$rdir" && pwd -P)" != "$(cd "$PWD/.data" && pwd -P)" ]; then
  echo "LỖI: Redis cổng $RPORT thuộc thư mục khác ($rdir). Đặt GEO_REDIS_PORT khác." >&2
  exit 1
fi
redis-cli -p $RPORT ping
