Phiên bản: v0.1 · 2026-11-26
# S2-E9 — thiết kế kiểm thử (Biến thể đề, xuất ảnh/PDF và liên kết chỉ xem, câu trắc nghiệm trong bài luyện)

> Chủ sở hữu: QC. **Trạng thái: ready for R3** · Mức epic: **P1**. Lập từ `story-map-s23.md` và `stories_ac.md` cùng thư mục.

## 0. Reader guide and glossary

Mã dùng trong tệp: `TC-S2En-nnn` (case), `US-S2En-nn` (story), `QA-S2En-nn` (hỏi đáp mở), `FX-nn` (dữ liệu mẫu, bảng ở `test_cases.md` mục 0), `PO-nn` (sổ quyết định), `DQ-nn` (câu hỏi design), `M-S2-nn` (chỉ số). Mã chỉ cho biết chỗ; nội dung ghi trong từng dòng.

## 1. Applicability map (mỗi nhóm: Y / N kèm lý do)

| Group | Screen / story | Applies (Y/N) | Reason if N |
|---|---|---|---|
| CL01 Luồng chính | 01, 02, 03 | Y |  |
| CL02 Dữ liệu nhập (biên) | 01 (số lượng, miền số), 02 (số đề, số liên kết, hạn), 03 (số lựa chọn, độ dài) | Y |  |
| CL03 Lỗi và thông báo | 01, 02, 03 | Y |  |
| CL04 Chuyển trạng thái | 02 (liên kết: hoạt động - hết hạn - thu hồi) | Y |  |
| CL05 Phân quyền theo vai | 01, 02 (giáo viên khác), 02 (người xem liên kết) | Y |  |
| CL06 Dữ liệu máy chủ trả về | 02 (liên kết không lộ lời giải, tên), 03 (cờ đúng) | Y |  |
| CL07 Thời gian | 02 (hạn liên kết đúng mốc) | Y |  |
| CL08 Đồng thời, gửi lặp | 03 (bấm đôi) | Y |  |
| CL09 Nhãn hai trục | 01 (biến thể), 02 (bản xuất lời giải) | Y |  |
| CL10 Thiết bị | 03 (iPad; iPhone kiểm theo US-S2E8-03) | Y |  |

## 2. Kỹ thuật thiết kế

- Phân vùng tương đương và giá trị biên cho mọi ô nhập, ngưỡng, giới hạn (mục 3).
- Bảng quyết định cho quy tắc nhiều điều kiện (nhãn hai trục, khóa giờ mở, quyền).
- Chuyển trạng thái cho thực thể có vòng đời; thử âm (lỗi, từ chối, hết hạn) cho mỗi quy tắc chặn.
- Mỗi AC có đúng một case; Type ghi Normal, Failure hoặc Boundary; case viết tay (Remark ghi "case viết tay") cho chỗ AC chưa phủ hết.

## 3. Phân tích giá trị biên

| Mã | Biến / giới hạn | Ngoài dưới | Biên dưới | Biên trên | Ngoài trên | Case |
|---|---|---|---|---|---|---|
| B1 | Số biến thể mỗi lần | 0 | 1 | 10 | 11 | US-S2E9-01-BV1 |
| B2 | Số trong biến thể | 0,5 / 100,5 | 1 | 100 | — | US-S2E9-01-BV2 |
| B3 | Đề thêm mỗi 60 phút (gồm biến thể) | — | 19 | 20 | 21 | US-S2E9-01-BV3 |
| B4 | Số đề mỗi tệp xuất | 0 | 1 | 20 | 21 | US-S2E9-02-BV1 |
| B5 | Hạn liên kết | T0 + 7 ngày - 1 giây | còn xem | — | T0 + 7 ngày (hết) | US-S2E9-02-BV2 |
| B6 | Liên kết đang hoạt động mỗi đề | — | 1 | 5 | 6 | US-S2E9-02-BV3 |
| B7 | Số lựa chọn trắc nghiệm | 1 | 2 | 5 | 6 | US-S2E9-03-BV1 |
| B8 | Độ dài lựa chọn (ký tự) | 0 | 1 | 100 | 101 | US-S2E9-03-BV2 |

## 4. Rủi ro và ưu tiên

| Mã | Rủi ro | Mức | Kỹ thuật | Case chính |
|---|---|---|---|---|
| R1 | Biến thể có số vô nghĩa hoặc đáp số sai | Cao | Sinh khối lượng, so engine | US-S2E9-01-FC3, SC1 |
| R2 | Lộ lời giải qua file xuất hoặc liên kết | Cao | Hộp xác nhận, kiểm dữ liệu trả về | US-S2E9-02-FC2, SC5 |
| R3 | Liên kết bị đoán hoặc dò | Cao | Giới hạn tần suất, mã dài | US-S2E9-02-FC4, FC3 |
| R4 | Câu trắc nghiệm bị dò bằng loại trừ hoặc lộ cờ đúng | Trung bình | Kiểm dữ liệu trả về, mặc định 1 lượt | US-S2E9-03-FC5, SC3 |
| R5 | Lộ danh tính giáo viên qua liên kết (PO-61) | Trung bình | Kiểm nội dung trang chia sẻ | US-S2E9-02-SC5 |

## 5. Chiến lược tự động hóa

| Tầng | Số case |
|---|---|
| E2E-API | 17 |
| E2E-UI | 16 |
| IT | 3 |
| UT | 8 |

Manual chỉ khi cần mắt người (bố cục thiết bị, cảm giác thao tác) hoặc chờ thiết kế còn thiếu; lý do ghi ở cột "Tự động hóa" của từng case.

## 6. Độ phủ

- Tiêu chí: 44 · Case: 44 · Tiêu chí chưa có case: 0.
- Dữ liệu mẫu dùng chung `FX-nn` ở `test_cases.md`; không dùng dữ liệu thật của người dùng (PO-61: không thu dữ liệu cá nhân).
- Quyết định và giả định đang dùng: xem `questions.md` (cột mặc định TẠM).

## 7. Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới | runner-T-261126-12 (QC) |
