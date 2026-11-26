# PROGRESS · GeometryMath (GEO) · cập nhật 27/11/2026 02:05
Mục tiêu hiện tại: CODE tới hết chặng 3 (goal "xong chặng 3"). Phiên điều phối + agent con Dev chạy Fable (DECISIONS 27/11). Luôn 2 agent song song. Delivery-lead tự quyết + ghi DECISIONS. Nhánh: `worktree-mvp-wave0` (repo `/Users/harry/IdeaProjects/GeometryMathSupport/.claude/worktrees/mvp-wave0`).
Tiến độ epic (code đã gộp): 8/15 — S2-E1, S2-E2, S2-E3, S2-E4 (T-24, ca680d3), S2-E5, S2-E6, S2-E7, S2-E8.
Đã xong 27/11:
- Regression C trên wave0 sau T-22 + T-23: ĐẠT 1379/1383 (4 bỏ qua có chủ đích ui-webkit, 0 trượt, 630/630 TC) — be30127.
- T-24 S2-E4 (Fable): 282 UT + 199 IT, e2e-affected L2 665/665, traceability S2-E4 27/27, bench-mvp OK — gộp ca680d3.
Đang dở (02:05, cả hai bị ngắt 00:40 vì đăng nhập hết hạn, đã gửi tin cho chạy tiếp):
- T-25 S2-E9 (wt `s2-e9`, cổng PG 54719): mã + test viết xong; đang gộp lại wave0 (có T-24) rồi chạy lại chuỗi kiểm. Tiến độ: .claude/progress/runner-T-261126-25.md
- T-42 seed chặng 3 (wt `s3-base`, cổng PG 54729): chạy lại từ đầu. Tiến độ: .claude/progress/runner-T-261126-42.md
Bước tiếp theo:
1. Nghiệm thu T-25: `git status --porcelain -uall` trong wt (đối chiếu regex brief25), đọc summary.json e2e + báo cáo JUnit; commit trên nhánh s2-e9; `git merge --no-ff s2-e9` vào wave0. Tương tự T-42 (nhánh s3-base).
2. Regression B trọn bộ trên wave0 (dropdb geo_e2e cụm wave0 cổng 54359 rồi `make e2e`).
3. Queue: 26 gate L6 (Tester, sonnet) → 27 → 28 ‖ 29 → 30 ‖ 31 → 32 → 33 gate L6 → 34.
4. Mỗi task: `git worktree add .claude/worktrees/<wt> -b <nhánh> HEAD` (lệnh git đơn, không ghép) → ghi `app/.env.ports` cổng riêng (đã dùng tới PG 54729/Redis 6779/server 8136/web 5216) → `queue.py brief <ID> > $CLAUDE_JOB_DIR/tmp/briefNN.txt` → Agent Fable với mục Bổ sung (xem tin gửi T-24/T-25: worktree tuyệt đối, epic-common v1.5, Icon, không source env.sh, git đơn, merge wave0 trước khi báo xong, không commit, báo cáo ≤ 10 dòng).
Inbox còn: 8 việc mở (thêm câu mở sau T-24) + BA AC góc 174,87° + FIX sau T-41 (.claude/inbox.md).
Quyết định đã chốt: DECISIONS.md (mới nhất 27/11: Fable tới hết chặng 3; regression C đạt).
Rủi ro / chặn:
- CHƯA push 150+ commit (push bị lớp kiểm quyền từ chối). Owner: `! git push origin worktree-mvp-wave0`.
- T-20…T-24 chưa qua soát độc lập (§7). Test chập chờn mở: 0209, 0804, 0539.
- `DraftService.create` (S2-E3) chưa gọi LibraryQuota; impact-map thiếu luật pom.xml (→ L2). Font chữ vẫn gọi Google.
- T-05 đo AI thật cần owner chạy trước T-34.
