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
# Chờ dịch vụ NHẬN KẾT NỐI (ping lặp có hạn giờ, T-261126-43) trước khi kiểm cụm/thư mục: máy tải cao thì tiến trình
# đã chạy nhưng chưa nghe cổng vài giây; kiểm ngay sẽ đỏ giả. Hạn giờ: GEO_DEV_UP_WAIT (giây, mặc định 60).
WAIT_S="${GEO_DEV_UP_WAIT:-60}"
wait_until() { # tên, lệnh kiểm...
  local name="$1"; shift
  local end=$((SECONDS + WAIT_S))
  until "$@" >/dev/null 2>&1; do
    if [ $SECONDS -ge $end ]; then echo "LỖI: $name không nhận kết nối sau ${WAIT_S} giây." >&2; return 1; fi
    sleep 0.5
  done
}
$PGBIN/pg_ctl -D "$PGDATA" -l "$PWD/.data/pg.log" -o "-p $PGPORT -k $SOCKDIR" -w -t "$WAIT_S" start >/dev/null 2>&1 || true
wait_until "PostgreSQL cổng $PGPORT (xem $PWD/.data/pg.log)" \
  $PGBIN/psql -h 127.0.0.1 -p $PGPORT -U geo -d postgres -tAc "SELECT 1"
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
# --daemonize trả về trước khi Redis nghe cổng: chờ PONG rồi mới hỏi thư mục (trước đây `config get dir` rỗng -> đỏ giả)
redis_pong() { [ "$(redis-cli -p $RPORT ping 2>/dev/null)" = "PONG" ]; }
wait_until "Redis cổng $RPORT" redis_pong
rdir=$(redis-cli -p $RPORT config get dir | tail -1)
if [ "$(cd "$rdir" && pwd -P)" != "$(cd "$PWD/.data" && pwd -P)" ]; then
  echo "LỖI: Redis cổng $RPORT thuộc thư mục khác ($rdir). Đặt GEO_REDIS_PORT khác." >&2
  exit 1
fi
redis-cli -p $RPORT ping
