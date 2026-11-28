Phiên bản: v0.1 · 2026-11-28
# S5-E4 — phát hiện QC / Dev

## Dev
| Mã | Phát hiện | Xử lý TẠM | Cần ai |
|---|---|---|---|
| Dev-S5E4-01 | Đặc tả ghi `/api/rubrics`, `/api/graded-assignments`, `/api/submissions/{id}/pages/{n}/url`; app dùng quy ước `/api/v1/teacher/rubrics`, `/api/v1/teacher/graded-assignments` (PATCH để đổi hạn/rubric), `/api/v1/student/graded-assignments` ("Bài cần nộp"), `/api/v1/submissions/{id}/pages/{n}/url`. URL ký tải ảnh: `/api/v1/files/images/{id}?o=&exp=&sig=` (không cần thẻ; sai/quá hạn 403). | Theo quy ước app | QC cập nhật TC khi soát |
| Dev-S5E4-02 | Bảng `submission`, `ocr_artifact`, `grading_proposal`, `criterion_assessment`, `approved_grade` là của T-261128-59/60/61, chưa có ở W3. `stored_image` giữ `submission_id`, `student_id`, `class_id`; học sinh "nộp ảnh" qua móc `/test-hooks/s5e4/image`. "Đã có bài nộp" (BR-S5E4-06) đếm theo ảnh đã lưu. TC-S5E4-43 hiện soát ảnh + đối tượng kho + AuditEvent; phần OCR/đề xuất/điểm ẩn danh đi qua điểm mở rộng `assessment.api.SubmissionDataEraser` mà job gọi. | 59 thay bộ đếm bài nộp bằng bảng `submission`; 59/60/61 cài `SubmissionDataEraser` và bổ sung kỳ vọng vào TC-43, TC-45 chuyển sang luồng `grade_ocr` thật | Dev 59, 60, 61 |
| Dev-S5E4-03 | Thế giới E2E riêng mỗi ca không thấy bộ `s5_core` (đề D-KC-01, F-S5-050): ca giao bài dùng đề kho `TUAN-11`. Seed `s5_350` cấp `s5_core` cho trường F-S2-100 và nạp RB-01 / BC-01 đúng id của `s5.json`. | TẠM | QC xác nhận |
| Dev-S5E4-04 | CONFLICT có `details` (US-S5E4-02-FC1) dựng ở controller vì `AppException` chung chỉ mang details cho VALIDATION_FAILED. Cổng "AI đề xuất" là stub luôn đóng `ClosedClassAiGate` (`assessment.api.ClassAiGateQuery`); "Chạy song song" cần công tắc AI chấm của lớp (`AiQuota.gradingOn`). | 65 thay cổng bằng bean `@Primary` | Dev 65 |
| Dev-S5E4-05 | Kho ảnh bản `local` (AES-256-GCM, khoá `GEO_IMAGE_KEY`; trống chỉ được ở dev/test). Bản S3 tại VN (QS5-18) chưa cấu hình: `geo.s5.images.backend=s3` báo lỗi rõ khi dùng. Mốc giờ của TC-41, TC-44 điều khiển qua móc (cấp URL lùi giờ; job năm học nhận ngày), không đổi đồng hồ CSDL. Job xoá chạy mỗi giờ (`geo.s5.images.purge-cron`). | Chờ owner chốt QS5-18 trước khi bật prod | PO / vận hành |

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-28 | Mục Dev đầu tiên | runner-T-261128-57 |
