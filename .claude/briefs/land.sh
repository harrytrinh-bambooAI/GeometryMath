#!/bin/bash
# land.sh <TASK-ID> <worktree> "<commit message dòng đầu>"
# Sau khi `queue.py check` ĐẠT: khôi phục ảnh theme (tác dụng phụ test), commit worktree task, gộp vào worktree-mvp-wave0, push, set done.
set -e
R=/Users/harry/IdeaProjects/GeometryMathSupport/.claude/worktrees/mvp-wave0
ID=$1; WT=$2; MSG=$3
W=$R/.claude/worktrees/$WT
TRAILER=$'\n\nCo-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>\nClaude-Session: https://claude.ai/code/session_017Bpd7Fh1u5Nr6Eem9tBewj'
git -C "$W" checkout -- artifacts/GEO/evidence/theme 2>/dev/null || true
git -C "$W" add -A
git -C "$W" diff --cached --quiet || git -C "$W" commit -q -m "$ID $MSG$TRAILER"
git -C "$R" merge --no-ff "$WT" -m "Gộp $WT ($ID)$TRAILER" | tail -1
git -C "$R" push -q
python3 ~/.claude/skills/epic-runner/queue.py --queue "$R/.claude/queue.md" set "$ID" done "check exit 0; gộp $(git -C "$R" rev-parse --short HEAD)"
