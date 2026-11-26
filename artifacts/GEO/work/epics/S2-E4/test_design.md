Phiên bản: v0.1 · 2026-11-26
# S2-E4 — thiết kế kiểm thử (Chat chỉnh nháp đề, import docx/pdf, đối chiếu trùng với kho)

> Chủ sở hữu: QC. **Trạng thái: ready for R3** · Mức epic: **P1**. Lập từ `story-map-s23.md` và `stories_ac.md` cùng thư mục.

## 0. Reader guide and glossary

Mã dùng trong tệp: `TC-S2En-nnn` (case), `US-S2En-nn` (story), `QA-S2En-nn` (hỏi đáp mở), `FX-nn` (dữ liệu mẫu, bảng ở `test_cases.md` mục 0), `PO-nn` (sổ quyết định), `DQ-nn` (câu hỏi design), `M-S2-nn` (chỉ số). Mã chỉ cho biết chỗ; nội dung ghi trong từng dòng.

## 1. Applicability map (mỗi nhóm: Y / N kèm lý do)

| Group | Screen / story | Applies (Y/N) | Reason if N |
|---|---|---|---|
| CL01 Luồng chính | 01, 02, 03 | Y |  |
| CL02 Dữ liệu nhập (biên) | 01 (độ dài tin, lượt chat), 02 (kích thước, số bài), 03 (độ giống) | Y |  |
| CL03 Lỗi và thông báo | 01, 02, 03 | Y |  |
| CL04 Chuyển trạng thái | 01 (đề xuất: chờ, nhận, bác, hoàn tác) | Y |  |
| CL05 Phân quyền theo vai | 03 (riêng tư Của tôi) | Y |  |
| CL06 Dữ liệu máy chủ trả về | 03 (không lộ đề người khác) | Y |  |
| CL07 Thời gian | — | N | Không có quy tắc theo giờ |
| CL08 Đồng thời, gửi lặp | 03 (import lại cùng tệp) | Y |  |
| CL09 Nhãn hai trục | 01, 02 | Y |  |
| CL10 Thiết bị | — (chỉ web) | N | Giáo viên dùng web |

## 2. Kỹ thuật thiết kế

- Phân vùng tương đương và giá trị biên cho mọi ô nhập, ngưỡng, giới hạn (mục 3).
- Bảng quyết định cho quy tắc nhiều điều kiện (nhãn hai trục, khóa giờ mở, quyền).
- Chuyển trạng thái cho thực thể có vòng đời; thử âm (lỗi, từ chối, hết hạn) cho mỗi quy tắc chặn.
- Mỗi AC có đúng một case; Type ghi Normal, Failure hoặc Boundary; case viết tay (Remark ghi "case viết tay") cho chỗ AC chưa phủ hết.

## 3. Phân tích giá trị biên

| Mã | Biến / giới hạn | Ngoài dưới | Biên dưới | Biên trên | Ngoài trên | Case |
|---|---|---|---|---|---|---|
| B1 | Tin chat (ký tự) | 0 | 1 | 1000 | 1001 | US-S2E4-01-BV2 |
| B2 | Lượt chat mỗi nháp | — | 1 | 30 | 31 | US-S2E4-01-BV3 |
| B3 | Bước hoàn tác | — | 1 | 10 | 11 | US-S2E4-01-BV1 |
| B4 | Kích thước tệp import (byte) | — | 1 | 10 485 760 | 10 485 761 | US-S2E4-02-BV1 |
| B5 | Số bài mỗi tệp | — | 1 | 50 | 51 | US-S2E4-02-BV2 |
| B6 | Độ giống đề | 0,89 (không báo) | 0,90 (gần giống) | 0,99 | 1,00 (trùng) | US-S2E4-03-BV1 |

## 4. Rủi ro và ưu tiên

| Mã | Rủi ro | Mức | Kỹ thuật | Case chính |
|---|---|---|---|---|
| R1 | AI tự lưu hoặc tự đoán khi câu mơ hồ | Cao | Thử âm, kiểm không tự lưu | US-S2E4-01-SC2, FC1 |
| R2 | Import tạo bản trùng làm rối kho | Trung bình | Chạy lại cùng tệp | US-S2E4-03-FC2 |
| R3 | Lộ đề Của tôi của người khác qua đối chiếu | Cao | Thử hai tài khoản | US-S2E4-03-FC1 |
| R4 | Tệp độc hại (macro) | Cao | Thử tệp có macro | US-S2E4-02-FC3 |

## 5. Chiến lược tự động hóa

| Tầng | Số case |
|---|---|
| E2E-API | 7 |
| E2E-UI | 13 |
| IT | 5 |
| UT | 2 |

Manual chỉ khi cần mắt người (bố cục thiết bị, cảm giác thao tác) hoặc chờ thiết kế còn thiếu; lý do ghi ở cột "Tự động hóa" của từng case.

## 6. Độ phủ

- Tiêu chí: 27 · Case: 27 · Tiêu chí chưa có case: 0.
- Dữ liệu mẫu dùng chung `FX-nn` ở `test_cases.md`; không dùng dữ liệu thật của người dùng (PO-61: không thu dữ liệu cá nhân).
- Quyết định và giả định đang dùng: xem `questions.md` (cột quyết định).

## 7. Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới | runner-T-261126-12 (QC) |
