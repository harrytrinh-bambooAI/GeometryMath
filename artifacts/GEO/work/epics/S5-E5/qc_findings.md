Phiên bản: v0.1 · 2026-11-28
# S5-E5 — phát hiện QC / Dev

## Dev
| Mã | Phát hiện | Xử lý TẠM | Cần ai |
|---|---|---|---|
| Dev-S5E5-01 | Đặc tả ghi `/api/classes/{lớp}/switches`, `/ai-usage`, `/api/notices`, `/api/copilot/usage`, `/api/admin/ai/kill`; app dùng quy ước `/api/v1/teacher/my-classes/{id}/ai-switches`, `/ai-usage`, `/api/v1/teacher/notices`, `/api/v1/teacher/copilot/usage`, `/api/v1/admin/ai/kill`. Task AI tên `class_notice` (V500) thay `notice_draft` của AC. | Theo quy ước app và V500 | QC cập nhật TC khi soát |
| Dev-S5E5-02 | Chấm nền (S5-E4/E6) và `POST /submissions/{id}/ocr/retry` chưa có ở W2: TC-19, 23, 25, 27 chạy qua bộ chạy chấm nền giả `/test-hooks/s5e5/grading-run`, `/ocr-retry` dùng đúng `AiQuota.registerJob/admitGrading/jobActive/takeOcrRetry`. | Task 57/59/61 PHẢI gọi `aigateway.api.quota.AiQuota` (mỗi trang một lượt, gọi lại không xin lượt, kiểm `jobActive` trước khi lưu đề xuất) | Dev 57, 59, 61 |
| Dev-S5E5-03 | Không có vai "Vận hành" trong `Role`: dùng bảng `ai_ops_grant` (gán bằng seed / lệnh vận hành, móc dev cấp). Tắt khẩn ở dev/test theo làn `X-Geo-Test-Lane` để E2E song song không tắt AI của ca khác; prod luôn toàn hệ thống. | TẠM | PO/ARCH xác nhận cách gán vai |
| Dev-S5E5-04 | Lượt hỏi đáp của học sinh hiện là task `qa` (S3); hạn mức 300/30 và tắt khẩn `kb_qa` áp lên cùng đường hỏi `/api/v1/student/assignments/{id}/questions` (coi `qa` ≡ `kb_qa`). Hạn mức soạn 30/ngày = lượt `lesson_plan_turn` (S5-E1) + lượt thông báo; S5-E1 chưa ghi lượt của nó vào bộ đếm lớp. | TẠM | Task 56/58 (kb_qa) |
| Dev-S5E5-05 | Thông báo đã gửi đi qua bảng `notification` (S3-E4) loại `class_notice`, thân ≤ 1 200 (nội dung ≤ 1 000 + "— {người gửi}" + dòng "Soạn với trợ lý AI, thầy cô đã duyệt"); API S3-E4 chưa có trường người gửi riêng. Cảnh báo 80 % loại `ai_quota`. | TẠM | QC xác nhận cách hiển thị |

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-28 | Mục Dev đầu tiên | runner-T-261128-55 |
