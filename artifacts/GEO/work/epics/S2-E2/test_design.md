Phiên bản: v0.2 · 2026-11-26
# S2-E2 — thiết kế kiểm thử (Khung môn học và hình học phẳng (dựng từ ràng buộc, bộ đề chuẩn, đo bench))

> Chủ sở hữu: QC. **Trạng thái: ready for R3** · Mức epic: **P0**. Lập từ `story-map-s23.md` và `stories_ac.md` cùng thư mục.

## 0. Reader guide and glossary

Mã dùng trong tệp: `TC-S2En-nnn` (case), `US-S2En-nn` (story), `QA-S2En-nn` (hỏi đáp mở), `FX-nn` (dữ liệu mẫu, bảng ở `test_cases.md` mục 0), `PO-nn` (sổ quyết định), `DQ-nn` (câu hỏi design), `M-S2-nn` (chỉ số). Mã chỉ cho biết chỗ; nội dung ghi trong từng dòng.

## 1. Applicability map (mỗi nhóm: Y / N kèm lý do)

| Group | Screen / story | Applies (Y/N) | Reason if N |
|---|---|---|---|
| CL01 Luồng chính | 01…05 | Y |  |
| CL02 Dữ liệu nhập (biên) | 02 (cạnh, góc), 04 (số đề), 05 (ngưỡng phần trăm) | Y |  |
| CL03 Lỗi và thông báo | 01, 02, 04, 05 | Y |  |
| CL04 Chuyển trạng thái | — | N | Epic không có thực thể có vòng đời; trạng thái bài làm ở S2-E8 |
| CL05 Phân quyền theo vai | — | N | Không có thao tác phân quyền riêng; khóa đáp số kiểm ở 03 |
| CL06 Dữ liệu máy chủ trả về | 02 (SceneSpec), 03 (khóa đáp số) | Y |  |
| CL07 Thời gian | 03 (mốc `release_at`) | Y |  |
| CL08 Nhãn hai trục | 03 (hình "Cần xem lại"), 05 (sai mang nhãn) | Y |  |
| CL09 Thiết bị | 03 (iPad, iPhone) | Y |  |
| CL10 Hiệu năng | 05 (chạy lệnh đo, không đặt ngưỡng giây ở chặng 2) | N | Chưa có số liệu; ghi sau B7 |

## 2. Kỹ thuật thiết kế

- Phân vùng tương đương và giá trị biên cho mọi ô nhập, ngưỡng, giới hạn (mục 3).
- Bảng quyết định cho quy tắc nhiều điều kiện (nhãn hai trục, khóa giờ mở, quyền).
- Chuyển trạng thái cho thực thể có vòng đời; thử âm (lỗi, từ chối, hết hạn) cho mỗi quy tắc chặn.
- Mỗi AC có đúng một case; Type ghi Normal, Failure hoặc Boundary; case viết tay (Remark ghi "case viết tay") cho chỗ AC chưa phủ hết.

## 3. Phân tích giá trị biên

| Mã | Biến / giới hạn | Ngoài dưới | Biên dưới | Biên trên | Ngoài trên | Case |
|---|---|---|---|---|---|---|
| B1 | Cạnh BC của tam giác có AB = AC = 5 | 10 (suy biến, từ chối) | 9,99 (dựng được) | — | — | US-S2E2-02-BV1, BV2 |
| B2 | Góc A của tam giác cân (độ) | 0 | 179 | 179 | 180 | US-S2E2-02-BV3 |
| B3 | Số đề trong bộ chuẩn | 29 | 30 | — | — | US-S2E2-04-BV1 |
| B4 | Thỏa ràng buộc (ngưỡng 95 %) | 28/30 = 93,3 % | 29/30 = 96,7 % | — | — | US-S2E2-05-BV1 |
| B5 | Đáp số khớp (ngưỡng 90 %) | 26/30 = 86,7 % | 27/30 = 90,0 % | — | — | US-S2E2-05-BV2 |
| B6 | Sai mang nhãn Đã kiểm chứng (ngưỡng 0) | — | 0 | 0 | 1 | US-S2E2-05-BV3 |

## 4. Rủi ro và ưu tiên

| Mã | Rủi ro | Mức | Kỹ thuật | Case chính |
|---|---|---|---|---|
| R1 | Hình phẳng đa dạng hơn bộ chuẩn | Cao | Thử âm dạng ngoài danh sách | US-S2E2-02-FC3 |
| R2 | Lộ đáp số qua công cụ đo hoặc SceneSpec | Cao | Kiểm dữ liệu trả về | US-S2E2-03-FC1, US-S2E2-02-SC6 |
| R3 | Đáp số tham chiếu chép sai làm lệch số đo | Cao | Đối chiếu engine khi nạp | US-S2E2-04-FC3 |
| R4 | Môn không gian của MVP hỏng khi đổi khung | Cao | Hồi quy trên bộ chuẩn MVP | US-S2E2-01-SC2 |
| R5 | Bench gọi nhầm AI thật tốn tiền | Trung bình | Kiểm cấu hình | US-S2E2-05-FC2 |

## 5. Chiến lược tự động hóa

| Tầng | Số case |
|---|---|
| E2E-API | 3 |
| E2E-UI | 6 |
| IT | 13 |
| Manual | 2 |
| UT | 21 |

Manual chỉ khi cần mắt người (bố cục thiết bị, cảm giác thao tác) hoặc chờ thiết kế còn thiếu; lý do ghi ở cột "Tự động hóa" của từng case.

## 6. Độ phủ

- Tiêu chí: 45 · Case: 45 · Tiêu chí chưa có case: 0.
- Dữ liệu mẫu dùng chung `FX-nn` ở `test_cases.md`; không dùng dữ liệu thật của người dùng (PO-61: không thu dữ liệu cá nhân).
- Quyết định và giả định đang dùng: xem `questions.md` (cột quyết định).

## 7. Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới | runner-T-261126-12 (QC) |
| v0.2 | 2026-11-26 | Áp PO-66: bỏ nhãn giả định chưa chốt | runner-T-261126-37 (BA/QC) |
