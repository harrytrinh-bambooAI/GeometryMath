Phiên bản: v0.1 · 2026-11-28
# S5-E3 — phát hiện QC / Dev

## Dev
| Mã | Phát hiện | Xử lý TẠM | Cần ai |
|---|---|---|---|
| Dev-S5E3-01 | Đặc tả ghi `/api/knowledge…`, `/api/classes/{lớp}/knowledge`; app dùng quy ước `/api/v1/teacher/knowledge…`, `/api/v1/teacher/classes/{id}/knowledge`. | Theo quy ước app | QC cập nhật TC khi soát |
| Dev-S5E3-02 | Thân yêu cầu của máy chủ ≤ 1 MB (`BodySizeLimitFilter`, khung): tải tệp đi theo đoạn như S2-E4 (`POST /uploads` khai tên + cỡ → `PUT /uploads/{id}?offset=` ≤ 768 KB → `POST` {uploadId}). Tệp 11 MB / xlsx bị từ chối ngay ở bước khai (VALIDATION_FAILED, MSG-S5-22); máy chủ kiểm lại cỡ thật và nội dung (đuôi docx mà không phải zip = MSG-S5-22). | TẠM | QC xác nhận bước TC-S5E3-05 |
| Dev-S5E3-03 | "4 mục khung 5512" hiểu là 4 hoạt động (Mở đầu, Hình thành kiến thức, Luyện tập, Vận dụng); mục tiêu + thiết bị của bài ghép vào mục Hoạt động 1; phiếu học tập KHÔNG đưa vào kho vì chứa đáp số (tránh nguồn lộ đáp số cho hỏi đáp). | TẠM | PO xác nhận |
| Dev-S5E3-04 | "Bài" của gắn kết = bài giao (`assignment`, S3); `BC-01` (bài có chấm, S5-E4, T-57) chưa có ở nhánh này: E2E dựng "Kiểm tra 15 phút – Khoảng cách" bằng bộ bài S3. Khi S5-E4 có `graded_assignment` riêng, cần nối gắn kết (cột `assignment_id` FK `assignment`). | TẠM | Dev 57/58 |
| Dev-S5E3-05 | Đề chưa có cột mã kiến thức: `KbRetriever.retrieve(…, knowledgeCodes, …)` nhận mã KT của đề từ người gọi (hỏi đáp T-58 lấy từ đề / ngân hàng). Mục lấy mã `KT..-…` nêu trong tiêu đề hoặc thân. Mục trùng mã +1 điểm (luôn đứng trước mục không trùng). | TẠM | Dev 58 |
| Dev-S5E3-06 | Không có đồng hồ dời được ở E2E: TC-S5E3-09 kiểm thời hạn bằng tham số `at` của `GET …/classes/{id}/knowledge` (và của bộ truy xuất); TC-S5E3-23 đo p95 phía máy chủ (`tookMs` của móc truy xuất), không gồm mạng. | TẠM | QC xác nhận |
| Dev-S5E3-07 | Ngưỡng 0,1 áp lên `ts_rank_cd(…, 1|32)` (chia 1 + log độ dài mục rồi đưa về 0…1); câu hỏi bỏ từ dừng tiếng Việt (của, là, và, em, …) trước khi tìm. TC-S5E3-22 trả MSG-S5-20 + `aiCalled=false` ở móc truy xuất; lượt hỏi thật (MSG-S5-20, không gọi AI) do T-58 nối. | TẠM | Dev 58 |

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-28 | Mục Dev đầu tiên | runner-T-261128-56 |
