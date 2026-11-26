# PROGRESS · GeometryMath (GEO) · cập nhật 26/11/2026 13:40
Mục tiêu hiện tại: hoàn thành MVP theo MVP.md §9 phần làm được bằng máy (PO-60). Sổ task `.claude/TASKS.md` + `.claude/queue.md`.
Đã xong:
- MVP E1–E7 + FIX1–FIX4b (1ce17bd; PR draft #1).
- T-01 mvp-ai + T-02 mvp-content nghiệm thu đạt (phiên chính chạy lại kiểm) → T-03 gộp (afda3dd, 77850f4) + sửa bench nạp kho đề, mock chỉ đo 6 đề có câu trả lời mẫu (6b62557). Bản gộp: verify 197 + 136 IT xanh, web-build xanh, e2e hai lượt 859/860 (1 bỏ qua), truy vết 0 thiếu, bench-mvp mock 4/4 chỉ số đạt.
- T-04 soát độc lập (Sonnet): 0 blocker, 0 major, 3 minor → inbox "FIX5".
Đang dở: T-05 đo thật — BỊ CHẶN: bộ phân loại quyền từ chối lệnh đọc GEO_AI_API_KEY từ ~/.zshrc. Owner chạy:
  `! cd <worktree>/app && eval "$(grep '^export GEO_AI_API_KEY=' ~/.zshrc)" && GEO_AI_PROVIDER=anthropic make bench-mvp > /tmp/geo-bench-real.log 2>&1; echo rc=$?` (~$3, 10–30 phút) hoặc cấp quyền.
Bước tiếp theo: đọc báo cáo artifacts/GEO/evidence/MVP-METRICS-* mới nhất (provider anthropic) → FIX5 (nếu kịp) → T-06 kiểm cuối + cập nhật PR #1 (push nhánh worktree-mvp-wave0).
Việc owner: `.claude/NEEDS.md` (A2; C1; C2 nạp gói đề ở prod; C3 engine thiết diện). Quyết định: DECISIONS.md (mới nhất: bench mock chỉ đo đề có câu trả lời mẫu), PO-DECISIONS.md (PO-60).
Rủi ro: KHÔNG commit/log key (kiểm `grep -r sk-ant app` = 0, `git log --all -G 'sk-ant-'` chỉ khớp dòng quy tắc trong PROGRESS). Cổng worktree chính: 54359/6419/8092/5176.
