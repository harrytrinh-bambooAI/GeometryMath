#!/bin/bash
cd "$(dirname "$0")/.."
/opt/homebrew/opt/postgresql@17/bin/pg_ctl -D "$PWD/.data/pg" -m fast stop 2>&1 | tail -1
redis-cli -p 6389 shutdown nosave 2>/dev/null; echo stopped
