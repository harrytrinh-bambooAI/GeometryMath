#!/bin/bash
# T-261128-73 (DEMO): tắt máy chủ + web do stack-up.sh bật, rồi PostgreSQL + Redis của worktree (scripts/dev-down.sh).
cd "$(dirname "$0")/../.."
for f in .data/demo/server.pid .data/demo/web.pid; do
  [ -f "$f" ] && kill "$(cat "$f")" 2>/dev/null; rm -f "$f"
done
sleep 2
scripts/dev-down.sh
