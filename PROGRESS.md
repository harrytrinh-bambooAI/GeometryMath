# PROGRESS · GeometryMath (GEO) · cập nhật 26/11/2026 19:12
Mục tiêu hiện tại: CODE tới hết chặng 3 (lệnh owner 26/11 "start coding", goal "hoàn thành chặng 3"). Owner chỉ kiểm sản phẩm lúc cuối; delivery-lead TỰ QUYẾT + ghi DECISIONS, không hỏi, không chờ owner (memory delivery-lead-decides). UI theo HTML bản mẫu `Design/prototype/s23*` + theme D mặc định (đổi A/B/C/D trong Cài đặt). AI mock; T-05 (đo thật) chỉ chặn gate cuối T-34; T-27 delivery-lead chốt theo S2-L6/REPORT.md. Phiên chính chỉ điều phối; mỗi task = 1 agent con + 1 worktree/nhánh; Opus ≤ 2 cùng lúc.
Đã xong (nhánh worktree-mvp-wave0):
- Spec S2/S3 R3 OK + PO-66 (ebec17a: 58 story / 746 AC / 751 TC). PLAN_code-s23 v1.0 (30df78c). epic-common v1.3 (fface0d).
- Thiết kế: kịch bản 7 KB / 61 bước + 15 màn mới (d1d5b87), PDF `Design/s23/GEO-kich-ban-S23.pdf`; 4 style + PDF `Design/styles/GEO-4-huong-style.pdf`; DQ-50 chốt (9b192b8).
- T-41 khung chung S2/S3: traceability --strict, 15 feature rỗng (gộp b350990).
- T-43 FIX e2e (gộp 4645aa3: 859/859 ×2, 8 phút/lượt). T-50 theme (gộp 82e37cb): mặc định C "Giấy kẻ ô" + Bad Script, nội dung toán --font-math (lệnh owner 26/11, DECISIONS). Khóa e2e toàn máy trong app/scripts/e2e.sh (1f25493): chỉ 1 bộ e2e chạy cùng lúc.
Đang dở (nền, 2 Opus):
- T-261126-18 S2-E1 định danh (wt `.claude/worktrees/s2-e1`, runner-T-261126-18.md): e2e lượt 1 909/913, S2-E1 53/53; 3 trượt do tải.
- T-261126-19 S2-E2 khung môn + hình phẳng (wt `.claude/worktrees/s2-e2`, runner-T-261126-19.md): đã gộp wave0 (theme C), đang kiểm lại.
- Cả hai đã được nhắn: gộp worktree-mvp-wave0 trước kiểm cuối (lấy T-43, T-50, khóa e2e).
Bước tiếp theo:
1. Agent 18/19 báo xong: chạy lại Kiểm (gốc repo) — e2e giờ tuần tự nhờ khóa; trượt rải rác do tải → so với lượt khác, ghi rõ khi chấp nhận. `cd <wt>` commit → merge --no-ff từ mvp-wave0 → header queue [done] (sed) → `make down` trong wt.
2. Kế: FIX T-44 (theme hoàn thiện: màu cứng → token, font toán ở màn MVP, chữ D ≥ 14 px, sql-lint user_preference) chạy khi có slot Opus; rồi 20‖21 → 22‖23 → 24‖25 → 26‖42 → 27 (delivery-lead) → 28‖29 → 30‖31 → 32 → 33 → 34 (owner + T-05).
3. Lệnh: `git -C <mvp-wave0> worktree add <tuyệt đối .claude/worktrees/<wt>> -b <nhánh> HEAD` → `queue.py brief <ID> > $CLAUDE_JOB_DIR/tmp/briefNN.txt` → Agent Opus đọc tệp brief + "Bổ sung" (worktree tuyệt đối, đọc epic-common v1.4, gộp wave0 trước kiểm cuối, không commit).
Inbox còn: 3 mục (.claude/inbox.md): FIX sau T-41 (một phần do T-43; còn ẩn feature rỗng /gv sau T-50), Dom vẽ bổ sung (đã xong T-40 — đóng), PO soát sau T-37 (story = 58, đóng).
Quyết định đã chốt: DECISIONS.md (mới nhất: sau T-41 — strict "chưa chạy" chỉ trượt ở gate T-26/T-33); PO-DECISIONS PO-66.
Rủi ro / chặn: phiên chính bị auto mode chặn chạy `make bench-mvp` bằng key → T-05 cần owner chạy (chỉ trước T-34). Máy tải cao làm e2e chập chờn. Ước tính còn ~15 task Opus × 3–5 giờ (~150M token) → có thể chạm hạn mức; dừng ở trạng thái đã gộp xanh. Lệnh git trong worktree lồng: phải `cd` vào worktree rồi chạy git trơn (guard chặn `git -C` phức tạp).
Việc owner: T-05 đo thật — `! cd /Users/harry/IdeaProjects/GeometryMathSupport/.claude/worktrees/mvp-wave0/app && eval "$(grep '^export GEO_AI_API_KEY=' ~/.zshrc)" && GEO_AI_PROVIDER=anthropic make bench-mvp > /tmp/geo-bench-real.log 2>&1; echo rc=$?` (~$3).
