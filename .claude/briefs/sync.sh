#!/bin/bash
# sync.sh <TASK-ID> <worktree> "<commit message>"
# Commit worktree task, kéo worktree-mvp-wave0 vào (dừng nếu xung đột), rồi reset geo_e2e/geo_dev của worktree (Flyway không out-of-order).
set -e
R=/Users/harry/IdeaProjects/GeometryMathSupport/.claude/worktrees/mvp-wave0
ID=$1; WT=$2; MSG=$3
W=$R/.claude/worktrees/$WT
TRAILER=$'\n\nCo-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>\nClaude-Session: https://claude.ai/code/session_017Bpd7Fh1u5Nr6Eem9tBewj'
git -C "$W" checkout -- artifacts/GEO/evidence/theme 2>/dev/null || true
git -C "$W" add -A
git -C "$W" diff --cached --quiet || git -C "$W" commit -q -m "$ID $MSG$TRAILER"
if ! git -C "$W" merge --no-ff worktree-mvp-wave0 -m "Kéo worktree-mvp-wave0 vào $WT$TRAILER" >/dev/null 2>&1; then
  echo "XUNG ĐỘT:"; git -C "$W" diff --name-only --diff-filter=U; exit 3
fi
(
  cd "$W/app"
  source scripts/env.sh
  case "$GEO_PG_PORT" in 5432|54399|"") echo "từ chối cổng '$GEO_PG_PORT'"; exit 1;; esac
  bash scripts/dev-up.sh >/dev/null
  for db in geo_e2e geo_dev; do "$PGBIN/dropdb" -h 127.0.0.1 -p "$GEO_PG_PORT" -U geo --if-exists --force "$db"; done
  bash scripts/db-init.sh >/dev/null
  echo "reset DB cổng $GEO_PG_PORT"
)
echo "SYNC OK $(git -C "$W" rev-parse --short HEAD)"
