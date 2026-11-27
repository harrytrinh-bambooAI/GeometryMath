# PROGRESS · GeometryMath (GEO) · cập nhật 27/11/2026 chiều
Mục tiêu hiện tại: code chặng 3 ĐÃ XONG trên nhánh tích hợp; chờ owner kiểm tay 7 ca + nghiệm thu L7 (S2, S3).
Nhánh `worktree-mvp-wave0` (repo `/Users/harry/IdeaProjects/GeometryMathSupport/.claude/worktrees/mvp-wave0`), đã push. Không agent nào đang chạy.

Đã xong 27/11 chiều (đều đã gộp):
- Kho đề MVP cân chuyên đề 46 → 84 đề (mỗi dạng 16–20, có THCS + THPT); engine thêm 7 phép đo; MVP_MAX 100; soát độc lập 0 major (`artifacts/GEO/evidence/CONTENT-BAL-20261127/REPORT.md`).
- Soát chéo toán Toán 11: 305/305, 0 sai đáp số; sửa 4 major + 2 minor → v1.3 (`artifacts/GEO/evidence/REVIEW-L11-MATH-20261127.md`).
- FIX theo PO-DECISIONS 27/11 + câu mở T-05 + sửa soát độc lập (`artifacts/GEO/evidence/FIX-PO27-20261127/REPORT.md`): verify 360 UT + 266 IT, web 101/101, e2e-affected 1189/1189.
- Hồi quy TC-GEO-0484 (luật AI giả lập F235 dùng loại đại lượng không có → mất chữ ký v2 từ FIX T-05) đã sửa + test khoá.
- E2E tách UI/API đợt 2: 11 ca B sang API, 20 ca đổi B→A, UI 538→527 (`artifacts/GEO/evidence/E2E-SPLIT2-20261127/REPORT.md`).
- Thư mục rác MVP-METRICS-20261127-0803 đã xoá.

Bước tiếp theo:
1. Owner: kiểm tay 7 ca thiết bị thật (S3E2-37, E3-07, E4-02, E4-21, E4-27, E5-26, E6-10), rồi nghiệm thu L7 chặng 2 và chặng 3.
2. Gate trước nghiệm thu: một lượt `make e2e` trọn bộ trên nhánh tích hợp (lượt cuối 1794/1800 trước khi sửa 0484 và gộp fix-po27).
3. Inbox: prompt AI biết 7 loại đại lượng mới (AnthropicPrompts); TC-S3E5-53 quyết B/A; rà tay 506 ca A; BA dời TC-S3E5-62.
4. Kho bài giảng Toán 11: phiên riêng, `đọc artifacts/GEO/materials/PLAN-kho-bai-giang.md và làm`.

Lưu ý vận hành: hook worktree chặn `source`, lệnh ghép `cd …; git`, `git -C $VAR` → dùng `git -C <đường dẫn tuyệt đối>` từng lệnh. Worktree con ở `.claude/worktrees/` của wave0, mỗi cái app/.env.ports riêng; cổng 54399 bị tiến trình ngoài (fake-supabase) chiếm — không dùng. Đo AI thật: `cd app && zsh -i -c 'GEO_AI_PROVIDER=anthropic make bench-mvp'`.
Bài học: gộp nhánh đổi engine/chữ ký phải chạy e2e-affected trước khi gộp (lỗi 0484 lọt vì chỉ chạy verify).
Inbox: .claude/inbox.md. Quyết định: DECISIONS.md (mới nhất 27/11 "PO-76 áp cả relation_denial").
Rủi ro: chưa có lượt e2e trọn bộ sau khi gộp đủ 4 nhánh chiều 27/11.
