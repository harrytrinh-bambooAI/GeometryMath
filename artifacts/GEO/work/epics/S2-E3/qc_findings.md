Phiên bản: v0.1 · 2026-11-26
# S2-E3 — Phát hiện QC

## Dev

Ghi bởi runner-T-261126-22 (L5 Dev). Không sửa `stories_ac.md`, `test_cases.md`, `test_design.md`; BA/QC xử lý.

| Mã | Chỗ | Phát hiện | Cách Dev làm (TẠM · tin cậy · nguồn) | Cần ai |
|---|---|---|---|---|
| Dev-S2E3-01 | BR-S2E3-06 (ảnh ≤ 5 MB), TC-S2E3-016/017 | Bộ lọc chung `common/web/BodySizeLimitFilter` chặn mọi thân yêu cầu > 1 MB (ngoài phạm vi tệp của task). | Ảnh gửi nhiều đoạn `PUT …/problem-drafts/{id}/image?offset=&total=` (application/octet-stream, mỗi đoạn ≤ 1 000 000 byte); `total` > 5 242 880 → `PAYLOAD_TOO_LARGE` "Ảnh tối đa 5 MB" ngay đoạn đầu; định dạng nhận theo byte đầu (JPG/PNG). TẠM · cao · BR-S2E3-06 | L4: có muốn nới bộ lọc cho đường ảnh đề (FIX) thay vì gửi đoạn |
| Dev-S2E3-02 | US-S2E3-03 bước "Đọc đề" với ảnh | Spec không nói đọc chữ từ ảnh là một phần của bước 1 hay trước bốn bước. | Đọc chữ (OCR giả lập) chạy lúc tải ảnh để giáo viên sửa ô vàng; bốn bước chạy sau "Xác nhận chữ và dựng tiếp". Bước 1 nhận loại đề và đáp số đề ghi. TẠM · vừa · DQ-02, Design c2-02-anh-mo | BA |
| Dev-S2E3-03 | BR-S2E3-17 "đáp số do đề ghi" | C2-02 không có ô riêng cho đáp số đề ghi. | Đáp số đề ghi = phần sau "Đáp số:" ở cuối chữ đề (`AnswerCompare.statedIn`); sửa ở C2-03 bằng "Sửa đáp số đề ghi" (lưu `solution_doc.stated_answer`, cần "Kiểm tra lại"). TẠM · vừa · DQ-04 | BA/Designer |
| Dev-S2E3-04 | US-S2E3-04-SC1, BR-S2E3-15 | Engine không dựng thì các bước sau có chạy không? "Dựng tay" cần trình dựng hình (chưa có). | Tác vụ dừng ở bước Dựng hình (lỗi `UNBUILDABLE`, không có "Thử lại bước này"); nháp lưu trạng thái "Cần xem lại · hình do AI dựng" (giữ mô tả AI đề xuất). Ba lối: "Bổ sung điều kiện" (thêm vào cuối đề rồi dựng lại), "Dựng tay" (chỉ ghi chú, trình dựng tay chưa có), "Lưu bản nháp chữ". TẠM · vừa · DQ-03 | PO (Dựng tay) |
| Dev-S2E3-05 | US-S2E3-04-SC2, SC3 ("Soạn bài giao") | Đề tự soạn của giáo viên chưa thành `problem`/`problem_version` giao được (bảng `problem` có chốt nền tảng; ngăn "Của tôi" là S2-E6, bộ bài S2-E8). | Màn "Đề của tôi" (`/gv/de-cua-toi`) có bước "Soạn bài giao · chọn đề": bản nháp chữ khóa kèm "bản nháp chữ"; đề hình do AI dựng phải bấm "Vẫn giao" ở hộp hỏi (API `POST …/assign` thiếu `confirmAiFigure` → `CONFLICT`). Chỉ ghi xác nhận (`assign_confirmed_at`); chọn lớp/giờ mở vẫn ở luồng giao bài hiện có. TẠM · thấp · PLAN §2 | L4 / T-23 nối ngăn "Của tôi" với giao bài |
| Dev-S2E3-06 | US-S2E3-04-SC5 "Dùng đáp số máy" | Không nói đáp số đề ghi (nếu lệch máy) có đổi theo không. | Tạo phiên bản mới với bước cuối mang đáp số máy rồi kiểm lại; nếu đáp số đề ghi cũng lệch máy thì đổi theo đáp số máy (để ba đáp số khớp). TẠM · vừa · BR-S2E3-18 | BA |
| Dev-S2E3-07 | BR-S2E3-05 (20 đề mỗi giờ) | "Đề" tính lúc nào. | Đếm nháp tạo trong 60 phút gần nhất (mỗi lần bắt đầu thêm đề / tải ảnh tạo một nháp); quá thì `RATE_LIMITED` + `Retry-After` (giây tới khi nháp cũ nhất hết tính). TẠM · vừa · PO-63 | BA |
| Dev-S2E3-08 | BR-S2E3-12 (30 giây mỗi bước) | Cổng AI có trần riêng `geo.ai.timeout` (mặc định 10 s). | Hạn 30 s đo ở tác vụ bằng đồng hồ tiêm được (IT dùng đồng hồ giả: 30 s xong, 31 s `TIMEOUT`); lượt gọi AI quá trần cổng cũng thành lỗi `TIMEOUT` của bước. TẠM · vừa · QA-S2E3-05 | TechLead |
| Dev-S2E3-09 | TC-S2E3-026 "mở lại sau 10 giây" | Chờ 10 giây làm chậm bộ E2E. | Ca chờ 3 giây sau khi đóng tab (bước Giải giả lập 6 giây) rồi kiểm "đang chạy hoặc đã xong" và cùng một tác vụ. TẠM · cao · TC-S2E3-026 | QC |
| Dev-S2E3-10 | Mục rail | Bản mẫu vào C2-02 từ "Kho đề › Thêm đề"; Kho đề thuộc E5/S2-E6. | Hai mục rail mới "Thêm đề" (`/gv/them-de`) và "Đề của tôi" (`/gv/de-cua-toi`); không sửa màn Kho đề. TẠM · vừa · PLAN §5 | Designer / T-23 |

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mục Dev (10 phát hiện) | runner-T-261126-22 |
