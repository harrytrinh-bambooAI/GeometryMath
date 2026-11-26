Phiên bản: v0.1 · 2026-11-26
# S2-E3 — thiết kế kiểm thử (Thêm đề bằng chữ hoặc ảnh, bốn bước đọc–dựng–giải–kiểm và ngoại lệ)

> Chủ sở hữu: QC. **Trạng thái: ready for R3** · Mức epic: **P0**. Lập từ `story-map-s23.md` và `stories_ac.md` cùng thư mục.

## 0. Reader guide and glossary

Mã dùng trong tệp: `TC-S2En-nnn` (case), `US-S2En-nn` (story), `QA-S2En-nn` (hỏi đáp mở), `FX-nn` (dữ liệu mẫu, bảng ở `test_cases.md` mục 0), `PO-nn` (sổ quyết định), `DQ-nn` (câu hỏi design), `M-S2-nn` (chỉ số). Mã chỉ cho biết chỗ; nội dung ghi trong từng dòng.

## 1. Applicability map (mỗi nhóm: Y / N kèm lý do)

| Group | Screen / story | Applies (Y/N) | Reason if N |
|---|---|---|---|
| CL01 Luồng chính | 01…04 | Y |  |
| CL02 Dữ liệu nhập (biên) | 01 (độ dài đề), 02 (kích thước ảnh, độ tin cậy), 04 (dung sai đáp số) | Y |  |
| CL03 Lỗi và thông báo | 01, 02, 03, 04 | Y |  |
| CL04 Chuyển trạng thái | 03 (chờ, đang chạy, xong, lỗi, hủy) | Y |  |
| CL05 Phân quyền theo vai | 03 (tác vụ của người khác) | Y |  |
| CL06 Dữ liệu máy chủ trả về | 02 (siêu dữ liệu ảnh), 03 | Y |  |
| CL07 Thời gian | 03 (hạn 30 giây) | Y |  |
| CL08 Đồng thời, gửi lặp | 03 (bấm đôi), tải ảnh hai tệp | Y |  |
| CL09 Nhãn hai trục | 02, 03, 04 | Y |  |
| CL10 Thiết bị | — (chỉ web) | N | Giáo viên dùng web; điện thoại không soạn đề |

## 2. Kỹ thuật thiết kế

- Phân vùng tương đương và giá trị biên cho mọi ô nhập, ngưỡng, giới hạn (mục 3).
- Bảng quyết định cho quy tắc nhiều điều kiện (nhãn hai trục, khóa giờ mở, quyền).
- Chuyển trạng thái cho thực thể có vòng đời; thử âm (lỗi, từ chối, hết hạn) cho mỗi quy tắc chặn.
- Mỗi AC có đúng một case; Type ghi Normal, Failure hoặc Boundary; case viết tay (Remark ghi "case viết tay") cho chỗ AC chưa phủ hết.

## 3. Phân tích giá trị biên

| Mã | Biến / giới hạn | Ngoài dưới | Biên dưới | Biên trên | Ngoài trên | Case |
|---|---|---|---|---|---|---|
| B1 | Độ dài đề dán (ký tự) | 9 | 10 | 2000 | 2001 | US-S2E3-01-BV1, BV2, BV3 |
| B2 | Kích thước ảnh (byte) | — | 1 | 5 242 880 | 5 242 881 | US-S2E3-02-BV2, BV3 |
| B3 | Độ tin cậy ô chữ | 0,79 (tô vàng) | 0,80 (không tô) | — | — | US-S2E3-02-BV1 |
| B4 | Thời gian một bước (giây) | — | — | 30 | 31 (TIMEOUT) | US-S2E3-03-BV1 |
| B5 | Lệch đáp số (tuyệt đối) | — | 0,01 (khớp) | — | 0,02 (lệch) | US-S2E3-04-BV1 |
| B6 | Số đề thêm mỗi giờ | — | 1 | 20 | 21 | US-S2E3-01-FC3 |

## 4. Rủi ro và ưu tiên

| Mã | Rủi ro | Mức | Kỹ thuật | Case chính |
|---|---|---|---|---|
| R1 | Máy tự đoán chữ khi ảnh không rõ | Cao | Thử âm ảnh trắng, ô vàng | US-S2E3-02-FC1, SC2 |
| R2 | Đường tắt từ lệch đáp số tới Duyệt | Cao | Thử gọi API trực tiếp | US-S2E3-04-FC1 |
| R3 | Mã script trong đề thực thi | Cao | Thử chuỗi độc hại | US-S2E3-01-FC2 |
| R4 | Ảnh gốc hoặc siêu dữ liệu vị trí bị lưu | Trung bình | Kiểm kho lưu trữ | US-S2E3-02-SC5 |
| R5 | Gọi AI lặp tốn tiền khi bấm đôi hoặc thử lại | Trung bình | Đếm lần gọi AI giả lập | US-S2E3-03-FC2, SC3 |

## 5. Chiến lược tự động hóa

| Tầng | Số case |
|---|---|
| E2E-API | 8 |
| E2E-UI | 21 |
| IT | 6 |
| UT | 4 |

Manual chỉ khi cần mắt người (bố cục thiết bị, cảm giác thao tác) hoặc chờ thiết kế còn thiếu; lý do ghi ở cột "Tự động hóa" của từng case.

## 6. Độ phủ

- Tiêu chí: 39 · Case: 39 · Tiêu chí chưa có case: 0.
- Dữ liệu mẫu dùng chung `FX-nn` ở `test_cases.md`; không dùng dữ liệu thật của người dùng (PO-61: không thu dữ liệu cá nhân).
- Quyết định và giả định đang dùng: xem `questions.md` (cột mặc định TẠM).

## 7. Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới | runner-T-261126-12 (QC) |
