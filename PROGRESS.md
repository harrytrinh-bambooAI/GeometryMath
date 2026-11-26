# PROGRESS · GeometryMath (GEO) · cập nhật 27/11/2026 07:10
**CODE CHẶNG 3 XONG + FIX trước gate T-34 đã gộp.** Regression cuối phase 1855/1855 (DECISIONS 27/11). Nhánh `worktree-mvp-wave0` (repo `/Users/harry/IdeaProjects/GeometryMathSupport/.claude/worktrees/mvp-wave0`), còn commit cục bộ chưa push.
Đã xong phiên 27/11 sáng:
- FIX T-34 (nhánh `fix-t34`, worktree lồng `.claude/worktrees/fix-t34`, đã gộp): Dev-S3E5-08 adc4a30, Dev-S3E5-16 77ccd29, assign lớp lưu trữ → CONFLICT 3f1d412 (V417). make verify 301 UT + 243 IT; e2e-affected 805/806 → ca 0605 chạy lại đạt.
- Soát độc lập T-20…T-32 có trọng tâm (an ninh/dữ liệu): `artifacts/GEO/evidence/REVIEW-T20-T32-20261127.md` — 0 critical, 4 major, 9 minor; ghi lô FIX vào inbox.
- App local cho owner thử: `make up` + `make run` (8092) + `make web-dev` (5176), geo_dev nạp seed e2e (F-S3). Đăng nhập: Cô Lan GV-7K2M-9QPA / Lan@2026x; Thầy Khôi GV-4D8R-2WNX / Khoi@2026x; học sinh HocSinh#2026 (mã trong `app/db/seed/fixture-ids.d/s3.json`).
Bước tiếp theo:
1. FIX lô soát T-20…T-32 (inbox "FIX lô soát độc lập"): ca thử tái hiện ClassAiSwitchFilter `;x=1` trước; REVOKE hàm SECURITY DEFINER; giới hạn audit denied; AccountPurgeJob trễ đầu. Kèm 2 ca chập chờn mới (probeLockedSolution phút 45, TC-GEO-0605).
2. Ghi PO-DECISIONS các mục [PO]/[BA] trong inbox trước gate T-34 (gồm phạm vi US-S3E5-05 với học sinh vào lớp bằng tên).
3. Owner: T-05 đo AI thật; gate T-27, T-34.
4. Kho bài giảng Toán 11: phiên khác, `artifacts/GEO/materials/PLAN-kho-bai-giang.md`.
Vùng chưa soát: xem cuối báo cáo REVIEW-T20-T32.
Lưu ý vận hành: hook worktree chặn `source`/lệnh ghép — dùng đích make và `git -C <repo>`; worktree con tạo trong `.claude/worktrees/` của wave0; cổng fix-t34 54369/6429/8102/5186.
Inbox: .claude/inbox.md (lô FIX soát + câu mở PO/BA).
Quyết định: DECISIONS.md (mới nhất 27/11).
Rủi ro: hạn mức 5 giờ; vùng code chưa soát; major #1 chưa có ca thử xác nhận.
