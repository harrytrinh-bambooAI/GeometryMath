# PROGRESS · GeometryMath (GEO) · cập nhật 26/11/2026 11:40
Mục tiêu hiện tại: hoàn thành MVP theo MVP.md §9 phần làm được bằng máy (PO-60). Khung điều phối delivery-lead v0.2 (L0 đạt `check_project.py --strict`, commit 1ff48bf).
Đã xong: MVP E1–E7 + FIX1–FIX4b (1ce17bd, đã push; PR draft #1 vào main). Setup L0 (1ff48bf).
Đang dở (2 agent Opus song song, bắt đầu 11:39):
- T-261126-01 `mvp-ai` · worktree `.claude/worktrees/mvp-ai` · AI thật (Claude sonnet-5-5) + ProfileGuard cấm mock prod + `make bench-mvp`
- T-261126-02 `mvp-content` · worktree `.claude/worktrees/mvp-content` · kho ≥ 40 đề verified + Playwright mobile P0
Tiến độ agent: `.claude/progress/mvp-ai.md`, `.claude/progress/mvp-content.md`.
Bước tiếp theo: khi 2 agent xong → nghiệm thu (chạy lại verify/web-build/e2e trong từng worktree) → T-03 gộp vào worktree-mvp-wave0 + bench-mvp mock → T-04 soát độc lập → T-05 đo thật (cần key) → T-06 kiểm cuối + cập nhật PR #1.
Sổ task: `.claude/TASKS.md`, `.claude/queue.md`. Việc của owner: `.claude/NEEDS.md` (A1 key `GEO_AI_API_KEY`).
Quyết định: PO-DECISIONS.md (mới nhất PO-60); điều phối: DECISIONS.md.
Rủi ro / chặn: đo thật chờ key (A1). Cổng 54329/6389/8080 của checkout chính; mvp-ai 54479/6529/8103/5187; mvp-content 54489/6539/8104/5188.
