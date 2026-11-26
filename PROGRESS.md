# PROGRESS · GeometryMath (GEO) · cập nhật 27/11/2026 05:35
**CODE CHẶNG 3 XONG** — regression 6 project 1855/1855 (4 bỏ qua), traceability strict ĐẠT (DECISIONS 27/11). Việc kế: dựng app local cho owner thử; FIX inbox trước gate T-34; kho bài giảng Toán 11 chạy ở phiên khác theo `artifacts/GEO/materials/PLAN-kho-bai-giang.md`.
Mục tiêu (goal owner): xong code chặng 3 + e2e theo nhóm 2 epic (feature) + regression cuối phase. Agent con Opus, mỗi agent một epic/nhóm US, 2 song song. Nhánh `worktree-mvp-wave0` (repo `/Users/harry/IdeaProjects/GeometryMathSupport/.claude/worktrees/mvp-wave0`), push tới 5db3799 (sau đó còn commit cục bộ).
Đã gộp vào wave0:
- Chặng 2: S2-E1…S2-E9 đủ.
- Chặng 3: T-42 seed F-S3; S3-E1 + E5-02/04 (T-28); S3-E2 (T-29); S3-E3 (T-30, agent chính sửa SQL lọc org_id); S3-E4 + E5-01 AuditEvent + E6-03 nhắc việc (T-31 lượt 2); FIX tích hợp (thanh dưới màn ≤720px cuộn ngang; TC-0744 theo thanh bên chặng 3; TC-0804 sửa gốc).
E2E theo nhóm (DECISIONS 27/11): F1 = S3-E1+E2 → 900/909 trước FIX; F2 = S3-E3+E4 → 624/636 trước FIX; sau FIX spec trượt 110/110, phần ảnh hưởng 107/107. Lệnh: `cd app && node scripts/e2e-affected.mjs --files "$(paste -sd, <danh sách tệp>)"`; danh sách = `git diff --name-only <gộp trước> <gộp sau> -- app` (F2 bỏ application.yml).
Đã xong thêm: F3 (S3-E5-03/05 + S3-E6-01/02, T-32) gộp; e2e F3 540/553 → FIX 13 ca xanh; regression cuối phase lượt 1 1842/1851 → FIX (traceability đọc JUnit, TC-0209 gốc) → lượt 20261127-050757 1855 đạt / 0 trượt; traceability strict ĐẠT. Không còn agent/worktree code nào đang chạy.
Bước tiếp theo:
1. Dựng app local cho owner thử (make up, server + web cổng wave0 trong app/.env.ports, seed mẫu F-S3: cô Lan, thầy Khôi, lớp 11A1/10B).
2. FIX inbox trước gate T-34: iam phát StudentPasswordResetEvent sau commit (Dev-S3E5-08); danh sách AuditEvent LEFT JOIN app_user; practice-sets/{id}/assign lớp lưu trữ → CONFLICT.
3. Soát độc lập code T-20…T-32 (CLAUDE.md §7); T-05 đo AI thật (owner); gate T-27, T-34 (owner).
4. Kho bài giảng Toán 11: phiên khác, `artifacts/GEO/materials/PLAN-kho-bai-giang.md`.
Lưu ý vận hành: git trong worktree lồng = `cd` riêng rồi git trơn, mỗi lệnh một lần gọi; không ghép `cd …; git` trong cùng lệnh; Makefile dùng `make up` (không có dev-up). GEO_E2E_EVIDENCE mặc định off.
Inbox: câu mở sau T-24/25/42/28/29/31 (.claude/inbox.md). Quan trọng: FIX iam phát StudentPasswordResetEvent sau commit (Dev-S3E5-08) trước gate T-34.
Quyết định: DECISIONS.md (mới nhất 27/11: mỗi agent một epic; e2e theo nhóm 2 epic; cỡ US T-shirt; FIX tích hợp).
Rủi ro: hạn mức 5 giờ (đã chạm 03:30); ngữ cảnh phiên điều phối ~410k; T-20…T-31 chưa soát độc lập.
