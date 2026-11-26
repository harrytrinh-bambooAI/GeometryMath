# PROGRESS · GeometryMath (GEO) · cập nhật 26/11/2026 (sau 13:40)
Mục tiêu hiện tại: đóng MVP (chặng 1) rồi chạy plan chặng 2 → hết chặng 3 (owner đã duyệt). Phiên chính = delivery-lead (skill delivery-lead v0.2): chỉ điều phối, mọi task code → agent con, mỗi task 1 worktree + nhánh riêng (DECISIONS 26/11, memory delegate-worktree-per-task).
Model theo vai (DECISIONS 26/11): Dev code = Opus; PO/BA/QC/thiết kế UI/Tester/soát = Sonnet. Skill: po-ba-qc (spec L1–L3), tester (L6, cần domain profile), delivery-lead brief + .claude/briefs/epic-common.md (Dev), queue.py. Song song: Opus ≤ 2, tổng quy đổi ≤ 4.
Đã xong:
- T-01 mvp-ai + T-02 mvp-content nghiệm thu đạt → T-03 gộp (afda3dd, 77850f4) + bench nạp kho đề, mock chỉ đo 6 đề (6b62557). Bản gộp: verify 197+136 IT, web-build, e2e hai lượt 859/860, truy vết 0 thiếu, bench mock 4/4.
- T-04 soát (Sonnet): 0 blocker/0 major/3 minor → T-261126-07 FIX5 (428a6a8).
- Profile small_task=delegate, PO-61 định danh tối giản (tên → mã người dùng, mã + mật khẩu, không thu thập dữ liệu; giáo viên đặt lại mật khẩu học sinh; giáo viên mã khôi phục TẠM) (7890c68, e1e74ba).
Đang dở:
- T-261126-07 FIX5 · agent con BÁO XONG, phiên chính CHƯA nghiệm thu · worktree `.claude/worktrees/fix5` nhánh `mvp-fix5` (cổng 54499/6549/8105/5189) · brief `.claude/briefs/T-261126-07-fix5.md` · tiến độ `.claude/progress/fix5.md`.
- Plan chặng 2–3 · agent `plan-s23` BÁO XONG (chưa rà) · `.claude/plans/STAGE-2-3-PLAN.md` (tiến độ `.claude/progress/plan-s23.md`); đã nhận PO-61 + chính sách model; plan phải có khối queue chạy được.
- T-261126-05 đo thật: Blocked — owner chạy `! cd <worktree>/app && eval "$(grep '^export GEO_AI_API_KEY=' ~/.zshrc)" && GEO_AI_PROVIDER=anthropic make bench-mvp > /tmp/geo-bench-real.log 2>&1; echo rc=$?` (~$3).
Bước tiếp theo:
1. Nghiệm thu FIX5: `cd .claude/worktrees/fix5/app && make verify && make bench-mvp` (exit 0), `git -C` log/diff --stat nhánh mvp-fix5, rà phạm vi tệp theo brief → gộp `git merge --no-ff mvp-fix5` vào worktree-mvp-wave0 (chờ lần đo thật của owner xong nếu đang chạy).
2. Đọc `tail -5 .claude/progress/plan-s23.md`; nếu plan xong: rà (vai/skill/≤2 Opus), nạp khối vào `.claude/queue.md` + TASKS.md (ID từ T-261126-08), mở đợt 1 (PO/BA/QC spec chặng 2, Sonnet, skill po-ba-qc).
3. Khi có số đo thật: đọc artifacts/GEO/evidence/MVP-METRICS-* mới nhất → B7 → T-06 kiểm cuối + push nhánh + cập nhật PR #1.
Inbox còn: lô FIX5 (đang đóng) (.claude/inbox.md)
Quyết định đã chốt: DECISIONS.md (mới nhất: duyệt plan chặng 2–3, Opus code/Sonnet design-spec-test); PO-DECISIONS.md (mới nhất PO-61).
Việc owner: .claude/NEEDS.md (A2; C1; C2 nạp gói đề ở prod; C3 engine thiết diện); chạy lệnh đo thật T-05.
Rủi ro / chặn: không commit/log key (`grep -r sk-ant app` = 0). Chặng 2 chỉ phát hành khi đạt B7 (đo thật); chặng 3 có điều kiện "giáo viên thật dùng lặp lại" → làm code trước, cổng sản phẩm owner chốt. Cổng worktree chính 54359/6419/8092/5176.
