# PROGRESS · GeometryMath (GEO) · cập nhật 27/11/2026 03:15
Mục tiêu hiện tại: xong CODE chặng 3, nhanh nhất. e2e vẫn viết + chạy (e2e-affected), báo cáo vẫn giữ, BỎ bằng chứng (GEO_E2E_EVIDENCE mặc định off). Agent con Fable, 2 song song. Nhánh `worktree-mvp-wave0` (repo `/Users/harry/IdeaProjects/GeometryMathSupport/.claude/worktrees/mvp-wave0`), đã push tới 3bde6a7 (sau đó còn commit cục bộ).
Đã gộp: S2-E1…S2-E9 (chặng 2 đủ code) + T-42 seed chặng 3 (F-S3). Regression C đạt 1379/1383. Regression B dừng giữa chừng theo lệnh owner (verify bản kết hợp BUILD SUCCESS).
Đang dở (hạn mức tài khoản ~95% lúc 03:15):
- T-28 S3-E1+E5 · wt `.claude/worktrees/s3-e1` (cổng PG 54739) · tiến độ .claude/progress/runner-T-261126-28.md
- T-29 S3-E2 · wt `.claude/worktrees/s3-e2` (cổng PG 54749) · đang chạy lệnh kiểm cuối · .claude/progress/runner-T-261126-29.md
- Brief: /Users/harry/.claude/jobs/74cc88b7/tmp/brief28.txt, brief29.txt (mất khi xoá job → sinh lại: `python3 ~/.claude/skills/epic-runner/queue.py --queue .claude/queue.md brief T-261126-28`).
Bước tiếp theo:
1. Đọc đuôi tiến độ T-28/T-29. Agent bị cắt → mở agent Fable mới, brief = briefNN + "Bổ sung" (worktree tuyệt đối; epic-common v1.6; git đơn mỗi lệnh; không source env.sh; GEO_E2E_EVIDENCE=off; merge wave0 trước khi báo xong; make down; không commit; báo cáo ≤ 10 dòng) + "tiếp tục từ trạng thái hiện có trong worktree".
2. Nghiệm thu: `git status --porcelain -uall` trong wt (đối chiếu regex brief), đọc summary.json e2e + JUnit target/*-reports; commit nhánh; `make down`; `git merge --no-ff` vào wave0; `git worktree remove`.
3. Queue: T-30 ‖ T-31 → T-32 → T-33 (Tester chặng 3). T-26 (Tester chặng 2) khi có chỗ trống. Gate owner: T-27, T-34.
4. Cổng đã dùng tới PG 54749 / Redis 6799 / server 8138 / web 5218.
Inbox: câu mở sau T-24, T-25, T-42 + cũ (.claude/inbox.md).
Quyết định: DECISIONS.md (mới nhất 27/11: bỏ bằng chứng e2e giữ báo cáo; make down + xoá worktree sau gộp; T-28/T-29 không chờ T-26).
Rủi ro: hạn mức tài khoản; T-20…T-29 chưa soát độc lập; test chập chờn 0209/0804/0539/0618; DraftService chưa gọi LibraryQuota.
