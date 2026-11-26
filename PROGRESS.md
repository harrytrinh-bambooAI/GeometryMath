# PROGRESS · GeometryMath (GEO) · cập nhật 27/11/2026 05:35
**CODE CHẶNG 3 XONG** — regression 6 project 1855/1855 (4 bỏ qua), traceability strict ĐẠT (DECISIONS 27/11). Việc kế: dựng app local cho owner thử; FIX inbox trước gate T-34; kho bài giảng Toán 11 chạy ở phiên khác theo `artifacts/GEO/materials/PLAN-kho-bai-giang.md`.
Mục tiêu (goal owner): xong code chặng 3 + e2e theo nhóm 2 epic (feature) + regression cuối phase. Agent con Opus, mỗi agent một epic/nhóm US, 2 song song. Nhánh `worktree-mvp-wave0` (repo `/Users/harry/IdeaProjects/GeometryMathSupport/.claude/worktrees/mvp-wave0`), push tới 5db3799 (sau đó còn commit cục bộ).
Đã gộp vào wave0:
- Chặng 2: S2-E1…S2-E9 đủ.
- Chặng 3: T-42 seed F-S3; S3-E1 + E5-02/04 (T-28); S3-E2 (T-29); S3-E3 (T-30, agent chính sửa SQL lọc org_id); S3-E4 + E5-01 AuditEvent + E6-03 nhắc việc (T-31 lượt 2); FIX tích hợp (thanh dưới màn ≤720px cuộn ngang; TC-0744 theo thanh bên chặng 3; TC-0804 sửa gốc).
E2E theo nhóm (DECISIONS 27/11): F1 = S3-E1+E2 → 900/909 trước FIX; F2 = S3-E3+E4 → 624/636 trước FIX; sau FIX spec trượt 110/110, phần ảnh hưởng 107/107. Lệnh: `cd app && node scripts/e2e-affected.mjs --files "$(paste -sd, <danh sách tệp>)"`; danh sách = `git diff --name-only <gộp trước> <gộp sau> -- app` (F2 bỏ application.yml).
Đang dở:
- Agent B (Opus) · worktree `.claude/worktrees/s3-e6` (PG 54779) · US-S3E5-03/05 (danh sách trường được phép, xoá tài khoản) rồi US-S3E6-01/02 (chữa bài sang trình chiếu, xuất kết quả + lưu trữ lớp) · tiến độ `.claude/progress/runner-T-261126-32.md` · "Xong khi": verify + web-build + check_ac_tc S3-E5 S3-E6 (không e2e).
Bước tiếp theo:
1. Khi B báo xong: `git status --porcelain -uall` trong s3-e6 (đối chiếu regex brief32), đọc JUnit `app/server/target/*-reports`; commit nhánh; `make down`; `git merge --no-ff s3-e6`; `git worktree remove`; queue T-32 done.
2. E2E nhóm F3 (S3-E5-03/05 + S3-E6-01/02): e2e-affected --files từ commit gộp của B. Trượt → một agent FIX gom lô.
3. Regression cuối phase: `cd app && make up`, dropdb geo_e2e cụm wave0 (cổng 54359, kiểm data_directory), `make e2e` (6 project) + traceability --strict MVP,S2-E1…E9,S3-E1…E6. Sau đó Tester (T-26 + T-33 gộp, Sonnet) nếu owner muốn báo cáo L6.
4. Sau mỗi lượt e2e: `git checkout -- artifacts/GEO/evidence/theme` (test theme ghi đè ảnh); `make down`.
Lưu ý vận hành: git trong worktree lồng = `cd` riêng rồi git trơn, mỗi lệnh một lần gọi; không ghép `cd …; git` trong cùng lệnh; Makefile dùng `make up` (không có dev-up). GEO_E2E_EVIDENCE mặc định off.
Inbox: câu mở sau T-24/25/42/28/29/31 (.claude/inbox.md). Quan trọng: FIX iam phát StudentPasswordResetEvent sau commit (Dev-S3E5-08) trước gate T-34.
Quyết định: DECISIONS.md (mới nhất 27/11: mỗi agent một epic; e2e theo nhóm 2 epic; cỡ US T-shirt; FIX tích hợp).
Rủi ro: hạn mức 5 giờ (đã chạm 03:30); ngữ cảnh phiên điều phối ~410k; T-20…T-31 chưa soát độc lập.
