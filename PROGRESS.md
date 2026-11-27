# PROGRESS · GeometryMath (GEO) · cập nhật 27/11/2026 chiều
Mục tiêu hiện tại: chặng 3 chờ owner duyệt gate T-27, T-34; dọn inbox (FIX theo PO-DECISIONS, E2E đợt 2); kho bài giảng Toán 11.
Nhánh `worktree-mvp-wave0` (repo `/Users/harry/IdeaProjects/GeometryMathSupport/.claude/worktrees/mvp-wave0`), nhiều commit cục bộ CHƯA push (lần push cuối 5db3799). Đã push 16567a3. ĐANG CHẠY: content-bal (cân Kho đề 46→84, mỗi dạng 16–20) + fix-po27 (FIX PO-DECISIONS 27/11 + câu mở T-05); hàng đợi tiếp theo xem cuối .claude/inbox.md "Hàng đợi chạy".
Đã xong 27/11 (phiên sáng):
- FIX trước gate T-34 (Dev-S3E5-08, -16, assign lớp lưu trữ → CONFLICT V417).
- Soát độc lập T-20…T-32 (`artifacts/GEO/evidence/REVIEW-T20-T32-20261127.md`) + lô FIX A/B (V418 REVOKE, V419, ClassAiSwitchFilter, audit denied, AccountPurgeJob cron, CSV, nhắc việc, sourcemap).
- T-05 đo AI thật ĐẠT sau FIX (prompt a2, chữ ký v2): `MVP-METRICS-20261127-0840` — hình 100 %, lộ 0/22, 0,0138 USD/bài.
- T-33 kiểm thử chặng 3: `artifacts/GEO/evidence/S3-L6/REPORT.md` — S3 292/292 tự động, 7 manual, traceability lệch 0; kỹ thuật ĐẠT gate T-34.
- PO chốt câu mở + TBD: PO-67…85 (`PO-DECISIONS.md`).
- E2E tách UI/API đợt 1 (DECISIONS 27/11): 26/58 ca B → `*.flow.api.spec.ts` + ảnh cuối; affected chỉ server −41 %; `artifacts/GEO/evidence/E2E-SPLIT-20261127/REPORT.md`.
- Toán 11 bài tập v1.2: 305 bài, lời giải từng bước (`artifacts/GEO/materials/lop-11/`).
Kiểm gần nhất: make verify 313 UT + 253 IT (nhánh fix-t05); e2e trọn bộ split-full 1821/1825, 0 trượt.
Bước tiếp theo:
1. Owner: gate T-27 ("S2 L7 OK"), T-34 ("S3 L7 OK") + 7 ca manual thiết bị thật (S3E2-37, E3-07, E4-02, E4-21, E4-27, E5-26, E6-10).
2. FIX theo PO-DECISIONS 27/11 (10 thay đổi code + TC-S3E5-61, TC-S3E1-55, TC-S3E3-58) — inbox.
3. Câu mở sau FIX T-05 (tính lại chữ ký v1→v2; cầu tâm I) — inbox.
4. E2E đợt 2: 32 ca B còn lại + rà tay bảng phân loại — inbox.
5. Kho bài giảng Toán 11: phiên riêng, `đọc artifacts/GEO/materials/PLAN-kho-bai-giang.md và làm`. Bài tập 305 bài chưa có soát chéo toán bởi model khác.
Lưu ý vận hành: hook worktree chặn `source`, lệnh ghép `cd …; git`, vòng lặp git → dùng `git -C <repo>` từng lệnh và đích make; worktree con tạo trong `.claude/worktrees/` của wave0, mỗi cái app/.env.ports riêng. Đo AI thật: `cd app && zsh -i -c 'GEO_AI_PROVIDER=anthropic make bench-mvp'` (khoá trong ~/.zshrc).
Inbox: .claude/inbox.md. Quyết định: DECISIONS.md (mới nhất 27/11 "Chiến lược E2E", "E2E tách UI/API").
Rủi ro: không. Owner tự kiểm tay 7 ca thiết bị thật rồi nghiệm thu L7 (S2, S3).
