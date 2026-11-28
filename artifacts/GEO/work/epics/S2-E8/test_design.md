Phiên bản: v0.2 · 2026-11-26
# S2-E8 — thiết kế kiểm thử (Bài luyện điền đáp số: bàn phím toán, chấm tương đương, làm bài, kết quả, soạn bài luyện)

> Chủ sở hữu: QC. **Trạng thái: ready for R3** · Mức epic: **P0**. Lập từ `story-map-s23.md` và `stories_ac.md` cùng thư mục.

## 0. Reader guide and glossary

Mã dùng trong tệp: `TC-S2En-nnn` (case), `US-S2En-nn` (story), `QA-S2En-nn` (hỏi đáp mở), `FX-nn` (dữ liệu mẫu, bảng ở `test_cases.md` mục 0), `PO-nn` (sổ quyết định), `DQ-nn` (câu hỏi design), `M-S2-nn` (chỉ số). Mã chỉ cho biết chỗ; nội dung ghi trong từng dòng.

## 1. Applicability map (mỗi nhóm: Y / N kèm lý do)

| Group | Screen / story | Applies (Y/N) | Reason if N |
|---|---|---|---|
| CL01 Luồng chính | 01…05 | Y |  |
| CL02 Dữ liệu nhập (biên) | 01 (50 ký tự), 02 (sai số, 15 chữ số, ranh giới), 05 (tên, số câu, lượt, sai số, ghi chú) | Y |  |
| CL03 Lỗi và thông báo | 02 (ba kết quả), 03 (mất mạng), 05 | Y |  |
| CL04 Chuyển trạng thái | 02, 03 (lượt thử), 05 (Nháp - Đã giao - Đã đóng) | Y |  |
| CL05 Phân quyền theo vai | 03, 04, 05 (học sinh, giáo viên, giáo viên khác) | Y |  |
| CL06 Cách ly dữ liệu | 03 (lớp khác), 04 (kết quả bạn), 05 (báo của lớp) | Y |  |
| CL07 Dữ liệu máy chủ trả về | 02 (đáp số mẫu, hướng), 03, 04 (khóa tới giờ mở) | Y |  |
| CL08 Thời gian | 04 (`release_at`) | Y |  |
| CL09 Đồng thời, gửi lặp | 02 (bấm đôi, mã chống lặp), 05 (báo hai lần) | Y |  |
| CL10 Nhãn hai trục | 04 (lời giải khi mở) | Y |  |
| CL11 Thiết bị | 01, 03, 04 (iPad, iPhone) | Y |  |
| CL12 Hiệu năng | 02 (thời gian chấm mỗi dòng) | N | Chưa có số liệu; ghi sau B7 |

## 2. Kỹ thuật thiết kế

- Phân vùng tương đương và giá trị biên cho mọi ô nhập, ngưỡng, giới hạn (mục 3).
- Bảng quyết định cho quy tắc nhiều điều kiện (nhãn hai trục, khóa giờ mở, quyền).
- Chuyển trạng thái cho thực thể có vòng đời; thử âm (lỗi, từ chối, hết hạn) cho mỗi quy tắc chặn.
- Mỗi AC có đúng một case; Type ghi Normal, Failure hoặc Boundary; case viết tay (Remark ghi "case viết tay") cho chỗ AC chưa phủ hết.

## 3. Phân tích giá trị biên

| Mã | Biến / giới hạn | Ngoài dưới | Biên dưới | Biên trên | Ngoài trên | Case |
|---|---|---|---|---|---|---|
| B1 | Sai số kiểu xấp xỉ (đáp số 45, ±0,5) | 44,49 / 45,51 | 44,5 | 45,5 | 45,500000000001 | US-S2E8-02-BV1…BV5 |
| B2 | Sai số cho phép khi soạn | -0,1 / 10,01 | 0 | 10 | 10,01 | US-S2E8-05-BV4 |
| B3 | Số lượt thử khi soạn | 0 | 1 | 5 | 6 | US-S2E8-05-BV3 |
| B4 | Số lượt còn lại khi làm | — | 3 (đầy) | 0 (hết) | gửi tiếp: CONFLICT | US-S2E8-02-BV11, BV12, BV13, FC11 |
| B5 | Độ dài đáp số (ký tự) | 0 | 1 | 50 | 51 | US-S2E8-01-BV1, BV2; US-S2E8-02-BV7, BV8 |
| B6 | Chữ số mỗi số | — | 1 | 15 | 16 | US-S2E8-02-BV9 |
| B7 | Số câu của bài luyện | 0 | 1 | 20 | 21 | US-S2E8-05-BV2, US-S2E8-03-BV2, BV3 |
| B8 | Tên bài (ký tự) | 0 | 1 | 80 | 81 | US-S2E8-05-BV1 |
| B9 | Ghi chú báo chấm sai (ký tự) | — | 0 | 200 | 201 | US-S2E8-05-BV5 |
| B10 | Giờ lấy lời giải so với `release_at` | R - 1 giây (khóa) | — | R (mở) | — | US-S2E8-04-BV1 |

## 4. Rủi ro và ưu tiên

| Mã | Rủi ro | Mức | Kỹ thuật | Case chính |
|---|---|---|---|---|
| R1 | Lộ đáp số (mẫu, hướng, độ lệch) qua phản hồi, gợi ý, hộp báo | Cao | Kiểm dữ liệu trả về, bộ câu thử moi đáp số | US-S2E8-02-FC15, FC16, FC11; US-S2E8-03-SC9 |
| R2 | Trừ lượt oan (cú pháp, bấm đôi, mạng) | Cao | Biên lượt, mã chống lặp | US-S2E8-02-FC13, FC14, BV13; US-S2E8-03-FC1 |
| R3 | Chấm sai do dạng viết (STAGES mục 10) | Cao | Bảng phân vùng tương đương, biên sai số | US-S2E8-02-SC2…SC8, BV1…BV5 |
| R4 | Chấm nhầm dấu chấm / dấu phẩy thập phân | Trung bình | Ca biên 1.000 | US-S2E8-02-BV10 |
| R5 | Tin vào thiết bị (sai số, kiểu do máy học sinh gửi) | Cao | Thử thân yêu cầu bị chèn | US-S2E8-02-FC18 |
| R6 | Hiện số tổng hay điểm làm học sinh so nhau (Q6) | Trung bình | Quét chuỗi giao diện | US-S2E8-04-SC2 |
| R7 | Bàn phím hệ thống che hình hoặc cỡ chạm quá nhỏ | Trung bình | Kiểm thiết bị thật | US-S2E8-01-FC2, SC8 |

## 5. Chiến lược tự động hóa

| Tầng | Số case |
|---|---|
| E2E-API | 33 |
| E2E-UI | 42 |
| IT | 5 |
| Manual | 5 |
| UT | 28 |

Manual chỉ khi cần mắt người (bố cục thiết bị, cảm giác thao tác) hoặc chờ thiết kế còn thiếu; lý do ghi ở cột "Tự động hóa" của từng case.

## 6. Độ phủ

- Tiêu chí: 112 · Case: 115 · Tiêu chí chưa có case: 0.
- Dữ liệu mẫu dùng chung `FX-nn` ở `test_cases.md`; không dùng dữ liệu thật của người dùng (PO-61: không thu dữ liệu cá nhân).
- Quyết định và giả định đang dùng: xem `questions.md` (cột quyết định).

## 7. Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới | runner-T-261126-12 (QC) |
| v0.2 | 2026-11-26 | Áp PO-66: bỏ nhãn giả định chưa chốt | runner-T-261126-37 (BA/QC) |
