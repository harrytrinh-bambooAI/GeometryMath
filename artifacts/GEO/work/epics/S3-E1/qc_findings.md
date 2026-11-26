Phiên bản: v0.1 · 2026-11-27
# S3-E1 — phát hiện QC / Dev

## Dev (runner-T-261126-28)
| Mã | Nội dung | Mức | Xử lý TẠM |
|---|---|---|---|
| Dev-S3E1-01 | API lớp chặng 3 đặt ở `/api/v1/teacher/my-classes/**` vì `/api/v1/teacher/classes` đã là API MVP (AuthController). Học sinh: `/api/v1/student/class-joins`, `/api/v1/student/classes`. | Thấp | Ghi trong `ClassController`. |
| Dev-S3E1-02 | Truy vấn MVP/S2 coi `class_member.left_at IS NULL` là "đang ở trong lớp". Để em Chờ duyệt / Bị từ chối không thấy bài lớp ở các truy vấn cũ (BR-S3E1-12, 53), trigger V315 `class_member_status_sync` giữ `left_at` có giá trị khi `status <> 'active'` (mốc đổi trạng thái), `NULL` khi Đang học. Hàng Chờ duyệt của `910_s3_core.sql` tự nhận `left_at` khi nạp. | Vừa | TẠM; task sau lọc theo `status` thay vì `left_at`. |
| Dev-S3E1-03 | Câu hỏi mở T-42 (nới `class_member_name_uq`): KHÔNG nới chỉ mục (đăng nhập tên MVP dựa vào nó). Thành viên vào bằng mã trùng tên một em Đang học khác nhận `name_key` = tên chuẩn hóa + " (mã người dùng)" khi được duyệt; hiển thị luôn dùng tên + mã (BR-S3E1-21). | Thấp | TẠM. |
| Dev-S3E1-04 | Câu hỏi mở T-42 (năm học): thêm `class.academic_year` (V315, CHECK `YYYY–YYYY`, năm sau = năm trước + 1), `class.join_mode` ('name' MVP / 'code' S3), `code_join_enabled`, `ai_enabled`; lớp F-S3-010…012 cập nhật ở `911_s3e1_classes.sql`. Lớp `join_mode = 'code'` không nhận đường vào bằng tên MVP (`fn_student_login` thay bằng bản lọc `join_mode = 'name'`) — tránh vào thẳng Đang học không qua duyệt (PO-66 G4). | Vừa | Đã làm. |
| Dev-S3E1-05 | TC-S3E1-01 "thân phản hồi không có trường nào ngoài tên lớp, cấp, năm học, mã lớp, trạng thái": phản hồi tạo lớp có thêm `id` (cần để mở lớp). | Thấp | TẠM; QC xác nhận. |
| Dev-S3E1-06 | Dấu "đã rời" ở Kết quả lớp (TC-16, 27; US-S3E1-03-SC4) thuộc S3-E3 (task 30); T-28 kiểm ở danh sách "Học sinh trong lớp" (chip "đã rời") và em hết thấy bài mới. | Thấp | Task 30 kiểm tiếp ở Kết quả lớp. |
| Dev-S3E1-07 | AuditEvent PASSWORD_RESET (TC-34 "có một AuditEvent"): T-28 phát sự kiện miền `iam.api.StudentPasswordResetEvent` (không mật khẩu); ghi `audit_log` ở task 31 (US-S3E5-01). Sự kiện bảo mật khi bị từ chối vì quan hệ ghi bảng `relation_denial` (loại, người gọi, loại đối tượng, giờ) — task 31 có thể gộp vào AuditEvent. | Vừa | Chuyển task 31. |
| Dev-S3E1-08 | TC-48/50 nêu API của S3-E2…E4 (kết quả lớp, bộ bài, đổi giờ, Mở ngay): T-28 kiểm các API của S3-E1/E5 và API bộ bài S2-E8 hiện có; task 29–31 áp `RelationDenials`/NOT_FOUND vào API của mình. | Thấp | Task 29–31. |
| Dev-S3E1-09 | "Xóa tài khoản" (TC-33) chưa có (task 32): ca E2E giả lập bằng `deactivated_at` (móc `/test-hooks/s3e1/deactivate`); danh sách chờ và duyệt bỏ tài khoản đã khóa. | Thấp | Task 32 kiểm lại với xóa thật. |
| Dev-S3E1-10 | Mã QR của trang in sinh phía web bằng bộ mã QR tối giản (`features/s3e1/qr.ts`, byte, mức L, phiên bản 1–5) vì `package.json` đợt S3 không thuộc T-28. Nội dung QR: `<origin>/hs/lop-cua-em?ma=HK-XXXX`. | Thấp | TẠM; đổi sang thư viện nếu owner muốn. |
| Dev-S3E1-11 | Giới hạn dò mã đếm lượt sai trong bảng `class_join_attempt` (không lưu mã đã gõ, IP, thiết bị); thời gian trong E2E giả lập bằng móc dời mốc (`join-attempts/age`, `seed`), mật khẩu tạm và giới hạn đặt lại tương tự. | Thấp | — |
| Dev-S3E1-12 | Lệnh chuyển hồ sơ lớp MVP (BR-S3E1-16) là dịch vụ `ClassProfileMigration` (kiểm ở IT TC-54), chưa có đích `make` riêng. | Thấp | TẠM. |

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-27 | Mục Dev đầu tiên | runner-T-261126-28 |
