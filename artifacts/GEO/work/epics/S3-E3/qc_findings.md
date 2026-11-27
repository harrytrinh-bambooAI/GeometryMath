Phiên bản: v0.2 · 2026-11-27
# S3-E3 — phát hiện và giả định khi code (mục Dev)

> Chủ sở hữu: Dev (runner-T-261126-30). AC / TC không bị sửa; các điểm dưới đây là giả định TẠM hoặc chỗ AC chưa đủ để code. **PO chốt toàn bộ ngày 27/11/2026 (PO-74, 82, 83, 85); không còn giả định TẠM mở.**

## Dev — giả định TẠM

| Mã | Điểm | Đang làm (TẠM) | Ảnh hưởng | Cần ai chốt |
|---|---|---|---|---|
| DEV-S3E3-01 | "Bước em dừng" và "gom theo bước" (BR-S3E3-20, 31, 34): MVP không ghi bước giải của học sinh, chỉ có mức gợi ý (1, 2), câu hỏi và lượt gửi đáp số. | Bước = mức gợi ý cao nhất em đã xin trước thời điểm đó; 0 = "Bước đọc đề". Câu hỏi được xếp vào bước theo mức gợi ý em đã xin trước khi hỏi. | US-S3E3-02 (dòng "Bước em dừng"), US-S3E3-03 (dữ liệu thô, số bước gửi bộ gộp) | PO, giáo viên cố vấn → **chốt PO-82** |
| DEV-S3E3-02 | Trạng thái "Xem lại" (BR-S3E3-02): "đáp số cuối chưa khớp hoặc hết lượt". AC US-S3E3-02-SC1 cho một lượt gửi sai là Xem lại. | Có lượt Đúng → Đúng; không thì có ít nhất một lượt Sai → Xem lại (không đợi hết 3 lượt); có tương tác khác → Đang làm; không có gì → Chưa làm. Lượt lỗi cú pháp không tính là gửi đáp số. | US-S3E3-01 mọi ô | PO → **chốt PO-82** |
| DEV-S3E3-03 | Kết quả gộp đã có (BR-S3E3-38) lưu ở đâu. | Bộ nhớ của máy chủ (tối đa 5 000 đề, theo băm đầu vào); khởi động lại hoặc nhiều máy chủ thì gọi lại bộ gộp một lần. Chưa thêm bảng (khoảng V365–V384 còn nguyên). | chi phí AI khi chạy nhiều máy chủ | ARCH → **chốt PO-82** |
| DEV-S3E3-04 | Đáp mẫu của bộ gộp giả lập: `aigateway` chưa có đáp mẫu cho task `class_stuck` và nằm ngoài phạm vi tệp của task. | Khi nhà cung cấp là `mock`, module `tracking` tự dựng đáp mẫu (bước nhiều em dừng nhất) hoặc dùng kịch bản của ca kiểm (`/test-hooks/s3e3/stuck-script`). Lượt gọi vẫn đi qua cổng AI (ghi nhật ký lượt gọi với mục đích `qa` vì `ai_purpose` chưa có giá trị riêng). Nhà cung cấp thật cần prompt hệ thống cho task `class_stuck` trong `aigateway` (FIX ngoài phạm vi). | US-S3E3-03 khi nối AI thật | ARCH, chủ `aigateway` → **chốt PO-83** |
| DEV-S3E3-05 | Trần chờ bộ gộp 10 giây (BR-S3E3-37) khi nhiều đề cùng đủ ngưỡng. | Mỗi lượt gọi chịu trần `geo.ai.timeout` (mặc định 10 giây); gọi lần lượt từng đề, sau lượt đầu quá giờ thì các đề còn lại hiện dữ liệu thô kèm "Thử lại" mà không gọi tiếp. Nhiều đề chậm nhưng chưa quá giờ có thể cộng dồn lâu hơn 10 giây. | US-S3E3-03-FC2 | ARCH → **chốt PO-83** |
| DEV-S3E3-06 | Dòng phụ đề "8 em Đang học" trong khi thân phản hồi không được có tổng số bản ghi (BR-S3E3-03, 09). | Giao diện tự đếm số hàng Đang học đã tải; lớp trên 100 em ghi "hơn 100 em Đang học" cho tới khi tải hết. | US-S3E3-01-SC1, FC10 | Designer → **chốt PO-82** |
| DEV-S3E3-07 | Tên cột: AC ghi "Đề 1 SC-đáy", bản mẫu ghi "Đề 1 · SC–đáy". | Hiện "Đề {vị trí trong bộ} · {tên đề bỏ tiền tố "Đề n"}" (khớp bản mẫu và thẻ "Đề 3 · hai mặt phẳng"). | US-S3E3-01-SC1 | Designer → **chốt PO-82** |
| DEV-S3E3-08 | Học sinh MVP vào lớp bằng tên, chưa có mã người dùng. | Có hàng trên bảng (mã trống) nhưng không mở được bằng chứng (API tra theo mã người dùng). Chặng 3 mọi em đều có mã (PO-61). | lớp MVP cũ | PO → **chốt PO-74** |
| DEV-S3E3-09 | "Vào Theo dõi, chọn lớp, mở Kết quả lớp" (US-S3E3-01-SC1): mục Theo dõi là màn E7 (ngoài phạm vi tệp). | Thêm mục rail riêng "Kết quả lớp" (`/gv/ket-qua-lop`) có ô chọn lớp và bộ; chưa có liên kết từ màn Theo dõi của E7 / tổng quan S3-E4. | điều hướng | Designer, task 31 → **chốt PO-82** |

## Dev — chỗ TC chưa kiểm được bằng máy

| TC | Phần chưa kiểm | Lý do |
|---|---|---|
| TC-S3E3-07 | Thang xám trên thiết bị thật | Manual theo test_cases.md; chữ + biểu tượng + tên truy cập đã kiểm ở test đơn vị web. |
| TC-S3E3-26 | "Không Notification nào gửi cho em" | Module `notify` thuộc task 31; ca kiểm xác nhận trang chỉ gọi API đọc (0 yêu cầu ghi) và tương tác của em không đổi. |
| TC-S3E3-32 | "Nhật ký hệ thống không ghi nội dung câu hỏi" | E2E không đọc được nhật ký máy chủ; mã S3-E3 không ghi log nội dung nào, thân phản hồi chỉ có câu hỏi nguyên văn ở đúng một trường. |
| TC-S3E3-33, 46 | Giờ cụ thể 15:20 / 15:21 / 15:31 | Giờ lấy từ máy chủ; ca kiểm xác nhận dạng "Cập nhật lúc HH:mm" và dữ liệu sau khi làm mới. |
| TC-S3E3-35 | "Bộ gộp được gọi đúng một lần cho Đề 4" | Nhật ký lượt gọi không ghi mã đề; ca kiểm đếm tổng lượt gọi = số đề đủ ngưỡng (4) và không tăng khi mở lại. |

## Bàn giao từ task trước

- T-28 "dấu đã rời ở Kết quả lớp": đã có (hàng + chip "đã rời" khi em Đã rời có bài làm; không tính vào ngưỡng và n).
- T-28 "AI gộp" và công tắc AI theo lớp (TC-S3E5-51): bộ gộp chạy bằng thẻ của giáo viên nên công tắc AI của lớp (chặn trợ lý của học sinh) không chặn "Cả lớp vướng ở đâu". Cần PO chốt có tắt gộp khi lớp tắt AI hay không. → **chốt PO-85**: có, tắt gộp khi lớp tắt AI.
- T-29 seed 920 thêm bộ Đã giao của 10B: các ca S3-E3 dựng lớp riêng qua sandbox nên không phụ thuộc số bộ của 10B.

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-27 | Tạo mới: giả định TẠM và phần chưa kiểm được bằng máy của S3-E3 | runner-T-261126-30 (Dev) |
| v0.2 | 2026-11-27 | PO chốt DEV-S3E3-01…09 và câu công tắc AI theo lớp (PO-74, 82, 83, 85) | runner-po-s3 (PO/BA) |
