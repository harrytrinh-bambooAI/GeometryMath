#!/bin/bash
# Dựng PostgreSQL và Redis RIÊNG của dự án (không đụng tới bản cài sẵn trên máy). Dữ liệu nằm trong app/.data
set -e
cd "$(dirname "$0")/.."
PGBIN=/opt/homebrew/opt/postgresql@17/bin
PGDATA="$PWD/.data/pg"; PGPORT=54329; RPORT=6389
if [ ! -d "$PGDATA" ]; then
  $PGBIN/initdb -D "$PGDATA" -U geo --auth=trust -E UTF8 --locale=C.UTF-8 >/dev/null
  cat >> "$PGDATA/postgresql.conf" <<CONF
port = $PGPORT
listen_addresses = '127.0.0.1'
unix_socket_directories = '$PWD/.data'
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
$PGBIN/pg_ctl -D "$PGDATA" -l "$PWD/.data/pg.log" -w start >/dev/null 2>&1 || true
$PGBIN/pg_isready -h 127.0.0.1 -p $PGPORT
if ! redis-cli -p $RPORT ping >/dev/null 2>&1; then
  redis-server --port $RPORT --bind 127.0.0.1 --dir "$PWD/.data" --save "" --appendonly no --maxmemory 512mb --maxmemory-policy allkeys-lru --daemonize yes >/dev/null
fi
redis-cli -p $RPORT ping
