Phiên bản: v0.2 · 2026-11-26
# S2-E6 — thiết kế kiểm thử (Kho đề: ngăn Của tôi / Kho chung, người soát nội dung, riêng tư và giới hạn tần suất)

> Chủ sở hữu: QC. **Trạng thái: ready for R3** · Mức epic: **P0**. Lập từ `story-map-s23.md` và `stories_ac.md` cùng thư mục.

## 0. Reader guide and glossary

Mã dùng trong tệp: `TC-S2En-nnn` (case), `US-S2En-nn` (story), `QA-S2En-nn` (hỏi đáp mở), `FX-nn` (dữ liệu mẫu, bảng ở `test_cases.md` mục 0), `PO-nn` (sổ quyết định), `DQ-nn` (câu hỏi design), `M-S2-nn` (chỉ số). Mã chỉ cho biết chỗ; nội dung ghi trong từng dòng.

## 1. Applicability map (mỗi nhóm: Y / N kèm lý do)

| Group | Screen / story | Applies (Y/N) | Reason if N |
|---|---|---|---|
| CL01 Luồng chính | 01, 02, 03 | Y |  |
| CL02 Dữ liệu nhập (biên) | 01 (phân trang), 02 (lý do, hàng đợi), 03 (bộ đếm) | Y |  |
| CL03 Lỗi và thông báo | 01, 02, 03 | Y |  |
| CL04 Chuyển trạng thái | 02 (Của tôi - Chờ soát - Đã vào - Bị từ chối) | Y |  |
| CL05 Phân quyền theo vai | 01, 02, 03 (giáo viên, học sinh, người soát) | Y |  |
| CL06 Cách ly dữ liệu | 03 (Của tôi riêng tư) | Y |  |
| CL07 Dữ liệu máy chủ trả về | 03 (khóa lời giải Kho chung với học sinh) | Y |  |
| CL08 Phân trang | 01 | Y |  |
| CL09 Thời gian | 03 (cửa sổ trượt 60 phút) | Y |  |
| CL10 Đồng thời, gửi lặp | 02 (gửi hai lần) | Y |  |
| CL11 Nhãn hai trục | 01 (thẻ đề) | Y |  |
| CL12 Thiết bị | — (chỉ web) | N | Giáo viên dùng web |

## 2. Kỹ thuật thiết kế

- Phân vùng tương đương và giá trị biên cho mọi ô nhập, ngưỡng, giới hạn (mục 3).
- Bảng quyết định cho quy tắc nhiều điều kiện (nhãn hai trục, khóa giờ mở, quyền).
- Chuyển trạng thái cho thực thể có vòng đời; thử âm (lỗi, từ chối, hết hạn) cho mỗi quy tắc chặn.
- Mỗi AC có đúng một case; Type ghi Normal, Failure hoặc Boundary; case viết tay (Remark ghi "case viết tay") cho chỗ AC chưa phủ hết.

## 3. Phân tích giá trị biên

| Mã | Biến / giới hạn | Ngoài dưới | Biên dưới | Biên trên | Ngoài trên | Case |
|---|---|---|---|---|---|---|
| B1 | Số đề mỗi trang | — | 1 | 20 | 21 (sang trang 2) | US-S2E6-01-BV1, BV2 |
| B2 | limit mỗi yêu cầu | 0 | 1 | 50 | 51 | US-S2E6-01-BV3 |
| B3 | Lý do từ chối (ký tự) | 4 | 5 | 500 | 501 | US-S2E6-02-BV1, BV2 |
| B4 | Đề chờ soát mỗi giáo viên | — | 1 | 10 | 11 | US-S2E6-02-BV3 |
| B5 | Đề thêm mỗi 60 phút trượt | — | 1 | 20 | 21 | US-S2E6-03-BV1 |
| B6 | Mốc hết cửa sổ (phút:giây) | — | — | 59:59 (còn chặn) | 60:00 (được thêm) | US-S2E6-03-BV2 |

## 4. Rủi ro và ưu tiên

| Mã | Rủi ro | Mức | Kỹ thuật | Case chính |
|---|---|---|---|---|
| R1 | Lộ đề Của tôi của người khác | Cao | Thử hai tài khoản trên mọi đường | US-S2E6-03-FC1, FC2, FC3, FC4 |
| R2 | Giả làm giáo viên (PO-63) | Cao | Bộ câu thử quyền | US-S2E6-03-FC8, FC7 |
| R3 | Nhồi Kho bằng nhiều đường tạo đề | Trung bình | Biên bộ đếm, cửa sổ trượt | US-S2E6-03-BV1, BV2, FC5 |
| R4 | Kho chung bẩn vì không ai soát | Trung bình | Kiểm quy trình soát | US-S2E6-02-SC2, SC3 |
| R5 | Người soát tự duyệt đề mình | Trung bình | Thử âm | US-S2E6-02-FC4 |

## 5. Chiến lược tự động hóa

| Tầng | Số case |
|---|---|
| E2E-API | 20 |
| E2E-UI | 13 |
| IT | 3 |

Manual chỉ khi cần mắt người (bố cục thiết bị, cảm giác thao tác) hoặc chờ thiết kế còn thiếu; lý do ghi ở cột "Tự động hóa" của từng case.

## 6. Độ phủ

- Tiêu chí: 37 · Case: 37 · Tiêu chí chưa có case: 0.
- Dữ liệu mẫu dùng chung `FX-nn` ở `test_cases.md`; không dùng dữ liệu thật của người dùng (PO-61: không thu dữ liệu cá nhân).
- Quyết định và giả định đang dùng: xem `questions.md` (cột quyết định).

## 7. Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới | runner-T-261126-12 (QC) |
| v0.2 | 2026-11-26 | Áp PO-66: bỏ nhãn TẠM | runner-T-261126-37 (BA/QC) |
