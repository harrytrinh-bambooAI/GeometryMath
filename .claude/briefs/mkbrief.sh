#!/bin/bash
# mkbrief.sh <TASK-ID> <worktree-name> <song-song-task-ID hoặc ->  → ghi brief ra .claude/briefs/<ID>.md, in đường dẫn
set -e
R=/Users/harry/IdeaProjects/GeometryMathSupport/.claude/worktrees/mvp-wave0
ID=$1; WT=$2; PAR=$3
OUT=$R/.claude/briefs/$ID.md
mkdir -p "$R/.claude/briefs"
python3 ~/.claude/skills/epic-runner/queue.py --queue "$R/.claude/queue.md" brief "$ID" > "$OUT"
HEAD=$(git -C "$R" rev-parse --short HEAD)
PORTS=$(tr '\n' ' ' < "$R/.claude/worktrees/$WT/app/.env.ports")
cat >> "$OUT" <<EOF

Bổ sung của phiên chính (bắt buộc):
- Worktree đã tạo sẵn: $R/.claude/worktrees/$WT (nhánh $WT từ worktree-mvp-wave0 @ $HEAD). Ports: $PORTS. Làm mọi việc trong worktree này. Worktree mới chưa có node_modules: chạy \`make web-install\` trước \`make web-build\`.
- Task song song: $PAR (worktree khác). Tệp chung chỉ sửa trong khối mốc \`$ID\` có sẵn. \`app/server/src/test/java/vn/geo/server/quality/allowed_fields.tsv\`: KHÔNG sửa dòng "Phiên bản"; thêm dòng mới vào CUỐI tệp dưới một dòng chú thích \`# --- $ID ---\`; trước khi thêm khóa, grep xem khóa (cột 1–3) đã có chưa — có rồi thì không thêm (AllowedFieldsTest báo "trùng dòng").
- Ảnh \`artifacts/GEO/evidence/theme/*.png\` do test theme chụp lại là tác dụng phụ — bỏ qua.
- Không gọi AI thật: không đặt GEO_AI_PROVIDER=anthropic, không đọc/in khóa API.
- Dòng tiến độ: các bước giữa ghi \`đạt\`/\`đang làm\`; chỉ bước cuối ghi \`xong\` hoặc \`lỗi\`.
EOF
echo "$OUT"
