Phiên bản: v0.3 · 2026-11-28
# S5-E5 — phát hiện QC / Dev

## Dev
| Mã | Phát hiện | Xử lý TẠM | Cần ai |
|---|---|---|---|
| Dev-S5E5-01 | Đặc tả ghi `/api/classes/{lớp}/switches`, `/ai-usage`, `/api/notices`, `/api/copilot/usage`, `/api/admin/ai/kill`; app dùng quy ước `/api/v1/teacher/my-classes/{id}/ai-switches`, `/ai-usage`, `/api/v1/teacher/notices`, `/api/v1/teacher/copilot/usage`, `/api/v1/admin/ai/kill`. Task AI tên `class_notice` (V500) thay `notice_draft` của AC. | Theo quy ước app và V500 | QC cập nhật TC khi soát |
| Dev-S5E5-02 | Chấm nền (S5-E4/E6) và `POST /submissions/{id}/ocr/retry` chưa có ở W2: TC-19, 23, 25, 27 chạy qua bộ chạy chấm nền giả `/test-hooks/s5e5/grading-run`, `/ocr-retry` dùng đúng `AiQuota.registerJob/admitGrading/jobActive/takeOcrRetry`. | Task 57/59/61 PHẢI gọi `aigateway.api.quota.AiQuota` (mỗi trang một lượt, gọi lại không xin lượt, kiểm `jobActive` trước khi lưu đề xuất) | Dev 57, 59, 61 |
| Dev-S5E5-03 | Không có vai "Vận hành" trong `Role`: dùng bảng `ai_ops_grant` (gán bằng seed / lệnh vận hành, móc dev cấp). Tắt khẩn ở dev/test theo làn `X-Geo-Test-Lane` để E2E song song không tắt AI của ca khác; prod luôn toàn hệ thống. | TẠM | PO/ARCH xác nhận cách gán vai |
| Dev-S5E5-04 | Lượt hỏi đáp của học sinh hiện là task `qa` (S3); hạn mức 300/30 và tắt khẩn `kb_qa` áp lên cùng đường hỏi `/api/v1/student/assignments/{id}/questions` (coi `qa` ≡ `kb_qa`). Hạn mức soạn 30/ngày = lượt `lesson_plan_turn` (S5-E1) + lượt thông báo; S5-E1 chưa ghi lượt của nó vào bộ đếm lớp. | TẠM | Task 56/58 (kb_qa) |
| Dev-S5E5-05 | Thông báo đã gửi đi qua bảng `notification` (S3-E4) loại `class_notice`, thân ≤ 1 200 (nội dung ≤ 1 000 + "— {người gửi}" + dòng "Soạn với trợ lý AI, thầy cô đã duyệt"); API S3-E4 chưa có trường người gửi riêng. Cảnh báo 80 % loại `ai_quota`. | TẠM | QC xác nhận cách hiển thị |
| Dev-S5E5-06 | Đặc tả ghi `/api/graded-assignments/BC-01/error-summary`; app dùng quy ước `/api/v1/teacher/graded-assignments/{id}/error-summary` và `/error-summary/{n}/suggestions`. Tên tiêu chí theo RB-01 thật ("Các bước", không phải "Bước"). | Theo quy ước app | QC cập nhật TC khi soát |
| Dev-S5E5-07 | `AiCallLog` không có cột kết quả kiểm máy chủ: kết quả "loại n nhận định" ghi ở `class_error_summary.check_note` (V805) của lượt tổng hợp và trả trong phản hồi (`check`); TC-13 đọc qua móc `/test-hooks/s5e5b/error-runs`. | TẠM | ARCH/QC xác nhận nơi ghi |
| Dev-S5E5-08 | BR-S5E5-08 "cùng chủ đề và dạng": đề BC-01 không mang mã dạng của ngân hàng → máy chủ xếp họ dạng tất định theo từ khóa của đề (`kc-diem-mp`, `kc-cheo-nhau`, `goc-dt-mp`, `goc-mp-mp`, `the-tich`…) rồi chọn dạng con giống đề nhất (Jaccard). Câu ngân hàng de-thi không giao thẳng được qua S3-E2 (không phải `problem_version`): gợi ý là đề kho trường thấy (bộ 'main' + bộ được cấp) cùng họ dạng; dạng con và nhiễu lấy từ câu ngân hàng giống đề nhất, đề có dạng mang nhiễu cùng loại lỗi xếp trước. Kho E2E chỉ còn 6 đề 'main' nên seed thêm bộ `s5_kc` (3 đề khoảng cách, `s5_700_s5e5b_kc.sql`), cấp cho thế giới qua móc. Tài liệu "cùng mã kiến thức" thay bằng mục tài liệu Đang dùng gắn lớp có từ khóa của dạng ("khoảng cách", "góc"…). | TẠM | PO/BA xác nhận; dài hạn: gắn mã dạng/mã kiến thức vào đề và câu ngân hàng |
| Dev-S5E5-09 | TC-19 luồng thật: sau khi huỷ việc chấm đang chạy, job nền quét lại bài (chưa có đề xuất) và ghi đề xuất `no_grade` lý do "AI chấm của lớp đang tắt" (cờ "AI không chấm", chấm tay) — không có đề xuất điểm AI mới; test kiểm điều đó thay cho "không có GradingProposal nào". | TẠM | QC xác nhận câu chữ AC |
| Dev-S5E5-10 | TC-23 luồng thật: ở 200/200 bài 2 trang không nhận lượt đọc chữ → bản đọc chữ "Chưa đọc được" (không gọi AI), cờ/lý do "lớp đã hết lượt AI hôm nay" chỉ hiện ở ca biên 199 (đọc chữ nhận, `grade` không nhận). Muốn hiện lý do hạn mức ngay ở bước đọc chữ cần sửa `assessment` (ngoài phạm vi T-64). | Đã xử lý ở T-261128-68 (xem Dev-S5E5-11) | Dev S5-E4 (FIX) |
| Dev-S5E5-11 | T-261128-68 (Dev-S5E5-10): `ocr_artifact.reason` (V846) ghi lý do khi việc đọc chữ nền không được nhận lượt AI (`AiQuota.admitGrading`); bản đọc chữ hiện `statusLabel` "AI không chấm: lớp đã hết lượt AI hôm nay" (+ trường `reason`) thay cho "Chưa đọc được", cờ hàng chờ cùng nhãn (mã cờ giữ `unreadable`). Áp cho mọi lý do không nhận lượt, kể cả "AI chấm của lớp đang tắt"; AI lỗi thật vẫn "Chưa đọc được". TC-S5E5-23 kiểm nhãn + cờ. | TẠM | PO xác nhận nhãn khi AI chấm tắt |

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-28 | Mục Dev đầu tiên | runner-T-261128-55 |
| v0.2 | 2026-11-28 | Dev-S5E5-06…10 (US-S5E5-02, 03; TC luồng chấm thật) | runner-T-261128-64 |
| v0.3 | 2026-11-28 | Dev-S5E5-10 đã xử lý; Dev-S5E5-11 (lý do hạn mức ở bản đọc chữ) | runner-T-261128-68 |
