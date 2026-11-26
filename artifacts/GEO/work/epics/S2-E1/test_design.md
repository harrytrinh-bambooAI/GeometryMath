Phiên bản: v0.1 · 2026-11-26
# S2-E1 — thiết kế kiểm thử (Đăng ký, đăng nhập bằng mã và khôi phục (định danh tối giản PO-61))

> Chủ sở hữu: QC. **Trạng thái: ready for R3** · Mức epic: **P0**. Lập từ `story-map-s23.md` và `stories_ac.md` cùng thư mục.

## 0. Reader guide and glossary

Mã dùng trong tệp: `TC-S2En-nnn` (case), `US-S2En-nn` (story), `QA-S2En-nn` (hỏi đáp mở), `FX-nn` (dữ liệu mẫu, bảng ở `test_cases.md` mục 0), `PO-nn` (sổ quyết định), `DQ-nn` (câu hỏi design), `M-S2-nn` (chỉ số). Mã chỉ cho biết chỗ; nội dung ghi trong từng dòng.

## 1. Applicability map (mỗi nhóm: Y / N kèm lý do)

| Group | Screen / story | Applies (Y/N) | Reason if N |
|---|---|---|---|
| CL01 Luồng chính | 01, 02, 03 | Y |  |
| CL02 Dữ liệu nhập (biên) | 01 (tên, mật khẩu), 03 (mật khẩu mới) | Y |  |
| CL03 Lỗi và thông báo | 01, 02, 03 | Y |  |
| CL04 Chuyển trạng thái | 02 (bình thường - bị khóa - mở khóa) | Y |  |
| CL05 Phân quyền theo vai | 03 (học sinh dùng đường giáo viên) | Y |  |
| CL06 Dữ liệu máy chủ trả về | 01 (mã khôi phục không lộ lần hai), 02 (không lộ mã tồn tại) | Y |  |
| CL07 Thời gian | 02 (mốc khóa 15 phút) | Y |  |
| CL08 Đồng thời, gửi lặp | 01 (bấm đôi), 04 (chạy lại) | Y |  |
| CL09 Nhãn hai trục | — | N | Epic không có nhãn kiểm chứng |
| CL10 Thiết bị | 01, 02 (iPad, iPhone, web) | Y |  |
| CL11 Dữ liệu trẻ em | 01 (không thu dữ liệu cá nhân) | Y |  |

## 2. Kỹ thuật thiết kế

- Phân vùng tương đương và giá trị biên cho mọi ô nhập, ngưỡng, giới hạn (mục 3).
- Bảng quyết định cho quy tắc nhiều điều kiện (nhãn hai trục, khóa giờ mở, quyền).
- Chuyển trạng thái cho thực thể có vòng đời; thử âm (lỗi, từ chối, hết hạn) cho mỗi quy tắc chặn.
- Mỗi AC có đúng một case; Type ghi Normal, Failure hoặc Boundary; case viết tay (Remark ghi "case viết tay") cho chỗ AC chưa phủ hết.

## 3. Phân tích giá trị biên

| Mã | Biến / giới hạn | Ngoài dưới | Biên dưới | Biên trên | Ngoài trên | Case |
|---|---|---|---|---|---|---|
| B1 | Tên hiển thị (ký tự) | 0 (rỗng) | 1 | 60 | 61 | US-S2E1-01-FC1, BV1, BV2, BV3 |
| B2 | Mật khẩu (ký tự) | 7 | 8 | 64 | 65 | US-S2E1-01-BV4, BV5, BV6 |
| B3 | Đăng ký mỗi giờ mỗi địa chỉ | — | 1 | 5 | 6 | US-S2E1-01-BV7 |
| B4 | Số lần đăng nhập sai liên tiếp | — | 1 | 9 | 10 (khóa) | US-S2E1-02-BV1, BV2, BV4 |
| B5 | Thời gian khóa (phút) | 14:59 | — | 15:00 | — | US-S2E1-02-BV3 |

## 4. Rủi ro và ưu tiên

| Mã | Rủi ro | Mức | Kỹ thuật | Case chính |
|---|---|---|---|---|
| R1 | Dò mật khẩu giáo viên (khóa nhầm người thật là DoS) | Cao | Biên khóa, thử âm | US-S2E1-02-BV2 |
| R2 | Lộ mã khôi phục lần hai hoặc trong phản hồi | Cao | Kiểm dữ liệu trả về | US-S2E1-01-SC4 |
| R3 | Thu thêm dữ liệu cá nhân ngoài PO-61 | Cao | Kiểm thân yêu cầu | US-S2E1-01-SC6 |
| R4 | Mã người dùng trùng | Cao | Thử khối lượng | US-S2E1-01-FC4 |
| R5 | Mất dữ liệu khi chuyển từ MVP | Trung bình | So sánh trước sau, chạy lại | US-S2E1-04-SC1, SC3 |

## 5. Chiến lược tự động hóa

| Tầng | Số case |
|---|---|
| E2E-API | 19 |
| E2E-UI | 16 |
| IT | 8 |
| Manual | 1 |

Manual chỉ khi cần mắt người (bố cục thiết bị, cảm giác thao tác) hoặc chờ thiết kế còn thiếu; lý do ghi ở cột "Tự động hóa" của từng case.

## 6. Độ phủ

- Tiêu chí: 44 · Case: 44 · Tiêu chí chưa có case: 0.
- Dữ liệu mẫu dùng chung `FX-nn` ở `test_cases.md`; không dùng dữ liệu thật của người dùng (PO-61: không thu dữ liệu cá nhân).
- Quyết định và giả định đang dùng: xem `questions.md` (cột mặc định TẠM).

## 7. Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới | runner-T-261126-12 (QC) |
