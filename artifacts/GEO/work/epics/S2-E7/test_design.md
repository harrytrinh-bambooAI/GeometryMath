Phiên bản: v0.2 · 2026-11-26
# S2-E7 — thiết kế kiểm thử (Trình chiếu trên lớp: toàn màn hình, hiện từng bước, mất mạng, hỏi lại khi lời giải khóa)

> Chủ sở hữu: QC. **Trạng thái: ready for R3** · Mức epic: **P0**. Lập từ `story-map-s23.md` và `stories_ac.md` cùng thư mục.

## 0. Reader guide and glossary

Mã dùng trong tệp: `TC-S2En-nnn` (case), `US-S2En-nn` (story), `QA-S2En-nn` (hỏi đáp mở), `FX-nn` (dữ liệu mẫu, bảng ở `test_cases.md` mục 0), `PO-nn` (sổ quyết định), `DQ-nn` (câu hỏi design), `M-S2-nn` (chỉ số). Mã chỉ cho biết chỗ; nội dung ghi trong từng dòng.

## 1. Applicability map (mỗi nhóm: Y / N kèm lý do)

| Group | Screen / story | Applies (Y/N) | Reason if N |
|---|---|---|---|
| CL01 Luồng chính | 01, 02, 03 | Y |  |
| CL02 Dữ liệu nhập (biên) | 01 (số bước 0…30), 02 (5 giây), 03 (R - 1 giây) | Y |  |
| CL03 Lỗi và thông báo | 01, 02, 03 | Y |  |
| CL04 Chuyển trạng thái | 01 (che / hiện, bước), 02 (có mạng / mất mạng) | Y |  |
| CL05 Phân quyền theo vai | 01 (học sinh không vào) | Y |  |
| CL06 Dữ liệu máy chủ trả về | 02 (xóa dữ liệu tải sẵn), 03 (máy học sinh vẫn khóa) | Y |  |
| CL07 Thời gian | 03 (giờ máy chủ, mốc R) | Y |  |
| CL08 Đồng thời, gửi lặp | — | N | Một người dùng một phiên chiếu, không có thao tác đồng thời đáng kể |
| CL09 Nhãn hai trục | 01 (nhãn ở góc) | Y |  |
| CL10 Thiết bị | 01 (web toàn màn hình, máy chiếu) | Y |  |

## 2. Kỹ thuật thiết kế

- Phân vùng tương đương và giá trị biên cho mọi ô nhập, ngưỡng, giới hạn (mục 3).
- Bảng quyết định cho quy tắc nhiều điều kiện (nhãn hai trục, khóa giờ mở, quyền).
- Chuyển trạng thái cho thực thể có vòng đời; thử âm (lỗi, từ chối, hết hạn) cho mỗi quy tắc chặn.
- Mỗi AC có đúng một case; Type ghi Normal, Failure hoặc Boundary; case viết tay (Remark ghi "case viết tay") cho chỗ AC chưa phủ hết.

## 3. Phân tích giá trị biên

| Mã | Biến / giới hạn | Ngoài dưới | Biên dưới | Biên trên | Ngoài trên | Case |
|---|---|---|---|---|---|---|
| B1 | Số bước hiện | -1 (Bước trước ở 0) | 0 | 30 (cuối) | 31 (khóa) | US-S2E7-01-BV1, BV2, BV3 |
| B2 | Giờ bấm Trình chiếu so với `release_at` | R - 1 giây (hỏi) | — | R (không hỏi) | R + 1 giây (không hỏi) | US-S2E7-03-BV1 |
| B3 | Tự bật lại hỏi đáp AI (giây) | — | — | 5 | 6 | US-S2E7-02-BV1 |
| B4 | Chữ lời giải (điểm, màn 1920 × 1080) | 27 (không đạt) | 28 | — | — | US-S2E7-01-SC7 |

## 4. Rủi ro và ưu tiên

| Mã | Rủi ro | Mức | Kỹ thuật | Case chính |
|---|---|---|---|---|
| R1 | Lộ lời giải khi chiếu trên màn lớp trước giờ mở | Cao | Hộp hỏi, kiểm máy học sinh | US-S2E7-03-SC1, SC2, FC4 |
| R2 | Giáo viên bấm Tiếp tục rồi máy học sinh bị mở khóa | Cao | Kiểm dữ liệu trả về | US-S2E7-03-FC4 |
| R3 | Mất mạng làm sập buổi chiếu | Cao | Ngắt mạng, thử âm | US-S2E7-02-SC2, FC2 |
| R4 | Dữ liệu lời giải ở lại trình duyệt dùng chung | Trung bình | Kiểm bộ nhớ sau khi thoát | US-S2E7-02-FC4 |
| R5 | Giờ máy giáo viên lệch làm sai quyết định hỏi | Trung bình | Đồng hồ giả | US-S2E7-03-FC3, BV1 |

## 5. Chiến lược tự động hóa

| Tầng | Số case |
|---|---|
| E2E-API | 4 |
| E2E-UI | 25 |
| IT | 3 |
| Manual | 1 |

Manual chỉ khi cần mắt người (bố cục thiết bị, cảm giác thao tác) hoặc chờ thiết kế còn thiếu; lý do ghi ở cột "Tự động hóa" của từng case.

## 6. Độ phủ

- Tiêu chí: 33 · Case: 33 · Tiêu chí chưa có case: 0.
- Dữ liệu mẫu dùng chung `FX-nn` ở `test_cases.md`; không dùng dữ liệu thật của người dùng (PO-61: không thu dữ liệu cá nhân).
- Quyết định và giả định đang dùng: xem `questions.md` (cột quyết định).

## 7. Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới | runner-T-261126-12 (QC) |
| v0.2 | 2026-11-26 | Áp PO-66: bỏ nhãn TẠM | runner-T-261126-37 (BA/QC) |
