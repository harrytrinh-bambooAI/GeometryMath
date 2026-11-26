# PROGRESS · GeometryMath (GEO) · cập nhật 26/11/2026 12:55
Mục tiêu hiện tại: hoàn thành MVP theo MVP.md §9 phần làm được bằng máy (PO-60). Khung delivery-lead v0.2 (L0 đạt, 1ff48bf). Sổ task `.claude/TASKS.md` + `.claude/queue.md`.
Đã xong: MVP E1–E7 + FIX1–FIX4b (1ce17bd, đã push; PR draft #1). Setup L0 (1ff48bf, fb82717). Key `GEO_AI_API_KEY` trong ~/.zshrc, kiểm HTTP 200, 0 tệp/commit chứa key (NEEDS A1 xong).
NGHIỆM THU T-261126-01 mvp-ai @46fbcde ĐẠT (phiên chính chạy lại 12:21–12:46, tuần tự): verify xanh (134 IT), web-build xanh, bench-mvp mock OK, e2e hai lượt 615 đạt/1 bỏ qua. Lượt e2e 12:02 của agent trượt 3 ca webkit E4 (TC-0423/0428/0489) do tải song song; chạy lại riêng 19/19 đạt → chập chờn, không hồi quy.
mvp-content: agent VẪN SỐNG (không dừng như ghi trước). e2e lượt 2 (12:10) 857/860, trượt 2 webkit (TC-0729, TC-KHUNG-03); lượt 3 858/860 trượt TC-0428 webkit; lượt final-1 rc=0; đang chạy lượt cuối, phiên chính canh bằng agent-watch.
Đang dở (ghi cũ 12:14):
- T-261126-01 `mvp-ai` · worktree `.claude/worktrees/mvp-ai` · nhánh mvp-ai @ 46fbcde · agent DỪNG 12:1x vì "Login expired" (lỗi xác thực, không phải lỗi code). Đã: ProfileGuard cấm mock prod + AnthropicAiProvider (5746f0a), make bench-mvp mock 6/6 đạt (3c2331c), CONFIG.md (863c331), verify 150+134 + web 41/41 (46fbcde). CÒN: make e2e hai lượt + báo cáo.
- T-261126-02 `mvp-content` · worktree `.claude/worktrees/mvp-content` · nhánh mvp-content @ 3004153 · có thể cũng dừng (cùng phiên đăng nhập). Đã: 46 đề HKN engine kiểm chứng 46/46 (6ea1298), Playwright mobile-iphone/pixel 244 ca (a688886), sửa bộ lọc Kho đề mobile (3004153). CÒN: make verify + web-build + e2e hai lượt + báo cáo.
Bước tiếp theo:
1. `/login`, rồi phiên mới: "đọc PROGRESS.md và làm tiếp".
2. Giao lại 2 agent MỚI (brief cũ `.claude/briefs/T-261126-0{1,2}-*.md` + dòng "tiếp tục từ commit trên, chỉ còn: …") — hoặc phiên chính tự chạy lệnh kiểm trong từng worktree.
3. Nghiệm thu: chạy lại verify / web-build / e2e 2 lượt trong mỗi worktree → T-03 gộp vào worktree-mvp-wave0 + bench-mvp mock trên 46+6 đề → T-04 soát độc lập (Sonnet) → T-05 đo thật: `eval "$(grep '^export GEO_AI_API_KEY=' ~/.zshrc)"` trong cùng lệnh (shell không tự nạp .zshrc) → T-06 kiểm cuối + cập nhật PR #1.
Việc owner: `.claude/NEEDS.md` (A2 DB rỗng geo_e2e; C1 câu chờ PO/BA). Quyết định: PO-DECISIONS.md (PO-60); DECISIONS.md.
Rủi ro: KHÔNG commit/log key (kiểm `grep -r sk-ant app` = 0 và `git log --all -G 'sk-ant-'` = 0 trước mỗi gộp). Cổng: chính 54329/6389/8080; mvp-ai 54479/6529/8103/5187; mvp-content 54489/6539/8104/5188 (docker của 2 worktree có thể còn chạy).
