#!/bin/bash
# start.sh <TASK-ID> <worktree> <PG> <REDIS> <SERVER> <WEB> <task song song|->
# Tạo worktree từ worktree-mvp-wave0, ghi app/.env.ports (kiểm cổng trống), sinh brief, set running. In đường dẫn brief.
set -e
R=/Users/harry/IdeaProjects/GeometryMathSupport/.claude/worktrees/mvp-wave0
ID=$1; WT=$2; PG=$3; RD=$4; SV=$5; WB=$6; PAR=$7
for p in $PG $RD $SV $WB; do
  case $p in 5432|54399) echo "cổng cấm $p"; exit 1;; esac
  if lsof -iTCP:$p -sTCP:LISTEN -n -P >/dev/null 2>&1; then echo "cổng bận $p"; exit 1; fi
done
W=$R/.claude/worktrees/$WT
[ -d "$W" ] || git -C "$R" worktree add -q -b "$WT" "$W" worktree-mvp-wave0
printf 'GEO_PG_PORT=%s\nGEO_REDIS_PORT=%s\nGEO_PORT=%s\nGEO_WEB_PORT=%s\n' $PG $RD $SV $WB > "$W/app/.env.ports"
bash "$R/.claude/briefs/mkbrief.sh" "$ID" "$WT" "$PAR" >/dev/null
python3 ~/.claude/skills/epic-runner/queue.py --queue "$R/.claude/queue.md" set "$ID" running >/dev/null
grep -q "$R/.claude/progress/runner-$ID.md" "$R/.claude/briefs/$ID.md" || { echo "brief trỏ sai tệp tiến độ"; exit 1; }
echo "$R/.claude/briefs/$ID.md"
