Phiên bản: v0.2 · 2026-11-26
# S2-E5 — thiết kế kiểm thử (Soát và duyệt lời giải có phiên bản, StepCheck, nhãn hai trục, hộp duyệt, lời giải đã giao bất biến)

> Chủ sở hữu: QC. **Trạng thái: ready for R3** · Mức epic: **P0**. Lập từ `story-map-s23.md` và `stories_ac.md` cùng thư mục.

## 0. Reader guide and glossary

Mã dùng trong tệp: `TC-S2En-nnn` (case), `US-S2En-nn` (story), `QA-S2En-nn` (hỏi đáp mở), `FX-nn` (dữ liệu mẫu, bảng ở `test_cases.md` mục 0), `PO-nn` (sổ quyết định), `DQ-nn` (câu hỏi design), `M-S2-nn` (chỉ số). Mã chỉ cho biết chỗ; nội dung ghi trong từng dòng.

## 1. Applicability map (mỗi nhóm: Y / N kèm lý do)

| Group | Screen / story | Applies (Y/N) | Reason if N |
|---|---|---|---|
| CL01 Luồng chính | 01…05 | Y |  |
| CL02 Dữ liệu nhập (biên) | 01 (bước, độ dài), 02 (dung sai, 10 giây) | Y |  |
| CL03 Lỗi và thông báo | 01…05 | Y |  |
| CL04 Chuyển trạng thái | 03, 04, 05 (AI soạn - đang sửa - đã duyệt - bị thay thế; phiên bản vN) | Y |  |
| CL05 Phân quyền theo vai | 01, 02, 04, 05 (chủ đề, giáo viên khác, học sinh) | Y |  |
| CL06 Dữ liệu máy chủ trả về | 02 (StepCheck không tới học sinh), 05 (khóa lời giải khi áp dụng) | Y |  |
| CL07 Thời gian | 02 (hạn 10 giây), 05 (`release_at`) | Y |  |
| CL08 Đồng thời, gửi lặp | 01 (hai tab), 04 (bấm đôi) | Y |  |
| CL09 Nhãn hai trục | 03 (bảng quyết định nhãn), 04 | Y |  |
| CL10 Thiết bị | 03 (iPhone cho nhãn học sinh), còn lại web | Y |  |

## 2. Kỹ thuật thiết kế

- Phân vùng tương đương và giá trị biên cho mọi ô nhập, ngưỡng, giới hạn (mục 3).
- Bảng quyết định cho quy tắc nhiều điều kiện (nhãn hai trục, khóa giờ mở, quyền).
- Chuyển trạng thái cho thực thể có vòng đời; thử âm (lỗi, từ chối, hết hạn) cho mỗi quy tắc chặn.
- Mỗi AC có đúng một case; Type ghi Normal, Failure hoặc Boundary; case viết tay (Remark ghi "case viết tay") cho chỗ AC chưa phủ hết.

## 3. Phân tích giá trị biên

| Mã | Biến / giới hạn | Ngoài dưới | Biên dưới | Biên trên | Ngoài trên | Case |
|---|---|---|---|---|---|---|
| B1 | Số bước của lời giải | 0 | 1 | 30 | 31 | US-S2E5-01-BV3, BV4 |
| B2 | Độ dài một bước (ký tự) | 0 (rỗng) | 1 | 2000 | 2001 | US-S2E5-01-FC2, BV1, BV2 |
| B3 | Lệch StepCheck (tuyệt đối) | — | 0,0018 / 0,01 (Đạt) | 0,01 | 0,011 (Không đạt) | US-S2E5-02-BV1, BV2, BV3 |
| B4 | Thời gian kiểm (giây) | — | — | 10 | 11 (TIMEOUT) | US-S2E5-02-BV4 |
| B5 | Số bước lệch khi duyệt | — | 0 (duyệt được) | 0 | 1 (chặn) | US-S2E5-04-BV1, BV2 |
| B6 | Số phiên bản | — | v1 | không giới hạn | — | US-S2E5-01-SC1, SC5 |
| B7 | Học sinh đã xem bản cũ khi áp dụng | — | 0 (không cảnh báo) | 2 (cảnh báo) | — | US-S2E5-05-SC3, FC3 |

## 4. Rủi ro và ưu tiên

| Mã | Rủi ro | Mức | Kỹ thuật | Case chính |
|---|---|---|---|---|
| R1 | Nhãn "Đã kiểm chứng" gắn nhầm cho bài sai (M-S2-03 > 0) | Cao | Bảng quyết định nhãn, bộ câu thử bài sai | US-S2E5-03-FC5, FC1, FC2, FC3, FC4 |
| R2 | Đường tắt tới Duyệt khi còn bước lệch hoặc lệch đáp số | Cao | Gọi thẳng API, biên số bước lệch | US-S2E5-04-FC1, BV1 |
| R3 | Lời giải đã giao bị đổi hoặc lộ sớm qua phiên bản mới | Cao | Thử ghi đè, thử moi đáp số | US-S2E5-05-FC1, FC5 |
| R4 | Mất bản cũ hoặc ghi đè khi hai tab cùng sửa | Cao | Khóa lạc quan, hai tab | US-S2E5-01-FC3 |
| R5 | Giáo viên bấm Duyệt mà không đọc (STAGES mục 10) | Trung bình | Cảnh báo bắt buộc, tô đỏ bước | US-S2E5-04-SC2, US-S2E5-02-SC1 |
| R6 | StepCheck lộ cho học sinh | Cao | Kiểm dữ liệu trả về | US-S2E5-02-FC2 |

## 5. Chiến lược tự động hóa

| Tầng | Số case |
|---|---|
| E2E-API | 16 |
| E2E-UI | 34 |
| IT | 12 |
| Manual | 1 |
| UT | 3 |

Manual chỉ khi cần mắt người (bố cục thiết bị, cảm giác thao tác) hoặc chờ thiết kế còn thiếu; lý do ghi ở cột "Tự động hóa" của từng case.

## 6. Độ phủ

- Tiêu chí: 64 · Case: 66 · Tiêu chí chưa có case: 0.
- Dữ liệu mẫu dùng chung `FX-nn` ở `test_cases.md`; không dùng dữ liệu thật của người dùng (PO-61: không thu dữ liệu cá nhân).
- Quyết định và giả định đang dùng: xem `questions.md` (cột quyết định).

## 7. Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới | runner-T-261126-12 (QC) |
| v0.2 | 2026-11-26 | Áp PO-66: bỏ nhãn TẠM | runner-T-261126-37 (BA/QC) |
