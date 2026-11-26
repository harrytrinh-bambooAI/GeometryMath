# PROGRESS · GeometryMath (GEO) · cập nhật 26/11/2026 (ngữ cảnh 1%, lưu Force)
Mục tiêu hiện tại: chặng 2 → hết chặng 3 theo `.claude/plans/STAGE-2-3-PLAN.md` v0.2 (owner duyệt). Phiên chính = delivery-lead v0.2: chỉ điều phối; mọi task → agent con, 1 worktree + nhánh / task. Model: Dev code = Opus; PO/BA/QC/UI/Tester/soát = Sonnet; Opus ≤ 2, tổng quy đổi ≤ 4.
Đã xong:
- MVP: T-01..T-04 gộp (afda3dd, 77850f4, 6b62557); FIX5 (T-07) nghiệm thu + gộp (merge sau 6f096fc; verify 201 + 137 IT, bench-mvp mock exit 0).
- Plan chặng 2–3 rà xong → v0.2 (sửa lệnh kiểm T-05: báo cáo in "Kết luận chỉ số bắt buộc: **ĐẠT**" + provider), nạp `.claude/queue.md` + TASKS (T-08…T-34; queue validate 28 task) (48f7483).
- W1 xong: T-08 PO brief/BRD delta/story map 55 story, PO-62…65 TẠM (0b9ee97); T-10 bản mẫu 24 màn + DQ.md 28 câu TẠM (887b97e). Cả hai đã gộp.
- W2 xong: T-14 spec S3 21 story/280 AC/280 TC (1d16746); T-12 spec S2 35 story/442 AC/447 TC (9493739). Lint 0 lỗi, đã gộp. Câu mở → NEEDS C8, C9.
- Designer: câu hỏi cho Designer Dom ở `Design/Q_Dom.md` v0.2 (owner đang tạo designer cho chặng sau; không tự làm design chặng 4+).
Đang dở (nền, 2 Sonnet):
- T-261126-15 soát phản biện 15 epic + check_ac_tc S2/S3 + R3 quickview + tester profile · wt `.claude/worktrees/s23-review` · tiến độ `.claude/progress/runner-T-261126-15.md`.
- T-261126-36 Designer Dom: REVIEW-Dom.md, trả lời Q_Dom.md, 8 màn thiếu (Q_Dom §C) · wt `.claude/worktrees/s23-dom` · tiến độ `.claude/progress/runner-design-dom.md`.
Bước tiếp theo:
1. Mỗi agent xong: chạy lại khối Kiểm trong `.claude/queue.md` ở gốc worktree, `git -C <wt> status`, rà phạm vi, xem vài ảnh (T-36) → commit nhánh → merge --no-ff → `queue.py set <ID> done` + TASKS.
2. Gate T-16: gửi owner MỘT danh sách = NEEDS §C (C1–C9) + R3_s23_quickview + "Cần owner" của RV_s23 + Q_Dom trả lời.
3. Sau gate 16: T-17 PLAN_code (Opus). Code W4+ chỉ khi T-05 ĐẠT.
Inbox còn: không mục mở (FIX5 đóng).
Quyết định: DECISIONS.md (mới nhất: duyệt plan chặng 2–3, model theo vai); PO-DECISIONS.md (PO-61).
Việc owner: T-05 đo thật — `! cd <worktree>/app && eval "$(grep '^export GEO_AI_API_KEY=' ~/.zshrc)" && GEO_AI_PROVIDER=anthropic make bench-mvp > /tmp/geo-bench-real.log 2>&1; echo rc=$?` (~$3); NEEDS A2, C1–C3.
Rủi ro / chặn: code chặng 2 chờ B7 (T-05). Không commit/log key. Ước tính toàn plan ≈ 190M token / 89 agent·giờ → nguy cơ chạm hạn mức; chạy theo đợt, dừng ở đợt đã gộp xanh.
