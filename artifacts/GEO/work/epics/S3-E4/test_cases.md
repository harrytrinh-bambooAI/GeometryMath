Phiên bản: v0.4 · 2026-11-27
# S3-E4 — test case (Tổng quan giáo viên, theo dõi nhanh trên điện thoại, thông báo trong ứng dụng)

> Chủ sở hữu: QC. Lập từ `stories_ac.md` cùng thư mục (mỗi tiêu chí có ít nhất một case) và `test_design.md`. **Trạng thái: ready for R3**; mức epic **P0**. Id ổn định; tiêu chí bị gạch bỏ thì case giữ id với N/A. AI là giả lập (D-GEO-047). Kỳ vọng bám Given / When / Then của AC, giá trị cụ thể lấy từ dữ liệu mẫu chung.

## 0. Reader guide and glossary

| Id / abbreviation | Meaning | Where used |
|---|---|---|
| `US-S3En-nn` · `-SCn` / `-FCn` | story (En = epic S3-En, nn = số story) · tiêu chí thành công / thất bại thứ n | tệp này; khóa bởi `work/s23/story-map-s23.md` |
| `BR-S3En-nn` | quy tắc nghiệp vụ của story (nguồn dẫn kèm) | tệp này |
| `TC-S3En-nn` | test case của epic S3-En | cột No của `test_cases.md` |
| `F-S3-nnn` | hàng dữ liệu mẫu chung của chặng 3 | mục "Dữ liệu mẫu" của tệp này |
| `QA-S3En-nn` | câu hỏi mở của epic, kèm mặc định đang dùng (đã chốt PO-66) | `questions.md` cùng thư mục |
| `PO-nn` | dòng của sổ quyết định gốc: PO-53, PO-61…65 | `PO-DECISIONS.md` |
| `DQ-nn` | câu hỏi và mặc định của thiết kế chặng 2–3 | `Design/s23/DQ.md` |
| `M-S3-nn` | chỉ số chặng 3 (M-S3-01…06) | `work/s23/brd-delta-s23.md` §1 |
| `C3-nn` | mã màn hình chặng 3 | `STAGES-AFTER-MVP.md` Ch3 §6; ảnh ở `Design/s23/c3-*.png` |
| `D-GEO-nnn` | quyết định của MVP còn hiệu lực (ví dụ D-GEO-047 AI giả lập, D-GEO-185 khóa theo tài khoản) | `work/03_decisions.md` |
| `GV-…` · `HS-…` · `HK-…` · `RC-…` | mã người dùng giáo viên · học sinh · mã lớp · mã khôi phục (định dạng đã chốt PO-66, DQ-16, DQ-23) | `Design/s23/DQ.md` |
| `release_at` · `due_at` | giờ mở lời giải · hạn làm (chỉ là nhãn, không khóa bài) | `STAGES-AFTER-MVP.md` Ch3 §7, §8 |
| Chưa đặt giờ · Sắp mở · Đã mở | ba trạng thái giờ mở: chưa có `release_at` (khóa) · giờ máy chủ < `release_at` (khóa) · giờ máy chủ ≥ `release_at` (được xem) | MVP, M-GEO-13 |
| Chờ duyệt · Đang học · Đã rời · Bị từ chối | trạng thái `ClassMembership` (Bị từ chối là đề xuất của BA, QA-S3E1-02) | tệp này |
| Đúng · Xem lại · Đang làm · Chưa làm | bốn trạng thái ô của bảng lớp × bài (nghĩa ở US-S3E3-01) | `Design/s23/c3-04-ket-qua-lop.png`, DQ-21 |
| Bài làm | một học sinh Đang học có ít nhất một tương tác (gửi đáp số, xin gợi ý hoặc hỏi) trên một đề của bộ; tính theo số học sinh khác nhau | US-S3E3-03, QA-S3E3-02 |
| T0 · t | giờ máy chủ lúc bắt đầu ca kiểm · giờ máy chủ tại thời điểm xét | mọi mốc giờ là tương đối |
| `CL-FN-nn` · `CL-UI-nn` · `CL-X-nn` | nhóm kiểm tra chức năng · giao diện và thiết bị · xuyên suốt (ma trận quyền, trường dữ liệu, tải) | `test_design.md` cùng thư mục |
| PO-66 · ENG · X | kỳ vọng theo giả định đã chốt PO-66 (kèm mã QA-) · case cần công cụ kỹ thuật · case xuyên suốt | cột Remark |
| `MSG-S3-nn` | thông điệp hiện cho người dùng của chặng 3, câu chữ ở bảng "Thông điệp" của tệp này | tệp này |

- Mỗi dòng là một case. Chuẩn bị theo **Pre-condition**, dùng **Test data**, làm lần lượt các bước đánh số ở **Test step**, rồi so với **Expected output**; case đạt khi mọi gạch đầu dòng đều đúng.
- **Loại**: chức năng · phân quyền · dữ liệu trả về (soát thân phản hồi, không chỉ giao diện) · phi chức năng · thiết bị.
- **Tự động hóa** (PO-50): `E2E-UI` Playwright theo bước trên màn · `E2E-API` gọi HTTP thẳng, soát `code` và thân phản hồi · `IT` kiểm thử tích hợp hoặc đơn vị có CSDL thử, công cụ soát schema · `Manual` có lý do (thiết bị thật).
- **Status** của vòng thực thi chỉ dùng: Pass · Fail · Blocked (+ cần gì) · UT (+ lý do) · Not run. Chưa chạy: Not run. Kỳ vọng ghi PO-66 (QA-) là ngưỡng đã chốt, chỉnh sau L6 (G12); màn chỉ khác đúng điểm đó thì đánh Blocked kèm mã QA-, không đánh Fail.
- Thực tế khác kỳ vọng thì ghi vào vòng thực thi, không sửa ô Expected. Mọi case dùng dữ liệu mẫu chung dưới đây; khôi phục dữ liệu sau case nếu Remark có ghi "khôi phục".

### Dữ liệu mẫu chung của chặng 3 (trùng `stories_ac.md`)

| Mã | Nội dung (dữ liệu mẫu chung của chặng 3; giờ Việt Nam) |
|---|---|
| `F-S3-001` | Giáo viên Lan · mã người dùng `GV-7K2M-9QPA` · vai Giáo viên · chủ lớp 11A1 và 11A2 |
| `F-S3-002` | Giáo viên Khôi · `GV-4D8R-2WNX` · chủ lớp 10B |
| `F-S3-010` | Lớp 11A1 · Lớp 11 · năm học 2026–2027 · mã lớp `HK-4P7Q` · Đang dạy · 8 em Đang học, 3 em Chờ duyệt, 1 em Đã rời |
| `F-S3-011` | Lớp 11A2 của cô Lan · mã lớp `HK-8M3C` · 3 em Đang học |
| `F-S3-012` | Lớp 10B của thầy Khôi · mã lớp `HK-5T9X` · 4 em Đang học |
| `F-S3-020…027` | Học sinh Đang học của 11A1: Bình An `HS-4P9T-37WD`, Chi Mai `HS-8L2K-77AB`, Đức Huy `HS-3M7Q-85XN`, Gia Bảo `HS-9D4W-67CE`, Hà My `HS-2X6R-48PL`, Khánh Linh `HS-7T7B-98QZ`, Minh Quân `HS-5N8C-24TR`, Ngọc Anh `HS-6V3F-78HM` |
| `F-S3-028` | Văn Tú `HS-3H6J-72KP` · Đã rời 11A1 (còn dữ liệu bài làm cũ) |
| `F-S3-030…032` | Chờ duyệt ở 11A1: Hoàng Nam `HS-7Q8Z-48DF`, Thu Hà `HS-7C5J-63LS`, Hoàng Nam thứ hai `HS-9B2L-85VG` (trùng tên với em đầu) |
| `F-S3-040` | Lớp 10B: Mi Na `HS-2A5F-77BC`, Bình An thứ hai `HS-6Z7N-52YT` (trùng tên với Bình An của 11A1), hai em khác; Chi Mai `HS-8L2K-77AB` cũng Đang học 10B (em thuộc hai lớp của hai giáo viên) |
| `F-S3-041` | Lớp 11A2: Quốc Bảo `HS-3C9D-77AA`, Thảo Vy `HS-8F2E-33BB`, Tuấn Kiệt `HS-7G4H-99CC` |
| `F-S3-050` | Bộ "Bộ bài tuần 15" của 11A1 · Đã giao · hạn làm T6 04/12/2026 20:00 · giờ mở lời giải 06/12/2026 08:00 · bốn đề: Đề 1 SC-đáy, Đề 2 d(A,SBC), Đề 3 hai mặt phẳng, Đề 4 thể tích |
| `F-S3-051` | Bảng trạng thái 11A1 × bộ tuần 15 (Đề 1, 2, 3, 4): An Đúng, Xem lại, Đúng, Xem lại · Mai Đúng, Xem lại, Xem lại, Chưa làm · Huy Đúng, Đúng, Xem lại, Đúng · Bảo Xem lại, Xem lại, Đang làm, Chưa làm · My Đúng, Đúng, Đúng, Đúng · Linh Chưa làm bốn đề · Quân Đúng, Xem lại, Xem lại, Xem lại · Ngọc Anh Đúng, Đúng, Xem lại, Đang làm |
| `F-S3-052` | Bộ "Bộ bài tuần 16" của 11A1 · Nháp · chưa đặt hạn, chưa đặt giờ mở lời giải |
| `F-S3-053` | Bộ tuần 15 của 11A2 · Đã giao · Đề 3 có ba bài làm (Quốc Bảo, Thảo Vy, Tuấn Kiệt) |


### US-S3E4-01 — Tổng quan giáo viên web + điện thoại: bài đang giao, lời giải đang khóa, em chờ duyệt

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S3E4-01 | Ba thẻ đúng số liệu trên web | High | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • cô Lan có 11A1 và 11A2<br/>• bộ tuần 15 của 11A1 Đã giao, Sắp mở lúc 06/12/2026 08:00, 1 em chưa mở (Khánh Linh)<br/>• bộ tuần 15 của 11A2 Đã giao, Sắp mở, 0 em chưa mở<br/>• 3 em Chờ duyệt ở 11A1 (F-S3-050, F-S3-053, F-S3-030…032) | • Hàng dữ liệu: F-S3-030…032, F-S3-050, F-S3-053 | 1. mở Tổng quan trên web | • thẻ "Bài đang giao" có hai dòng: "Bộ bài tuần 15 · 11A1 · hạn T6 04/12/2026 20:00 · 1 em chưa mở" và dòng của 11A2 "0 em chưa mở"<br/>• thẻ "Lời giải đang khóa" có hai dòng cùng giờ mở "08:00, 06/12/2026"<br/>• thẻ "Em chờ duyệt" ghi 3, tách "11A1: 3" | US-S3E4-01 · SC1; CL-FN-01 |
| TC-S3E4-02 | Cùng dữ liệu trên điện thoại, xếp một cột | High | Normal (Success case) | thiết bị | thủ công trên thiết bị | Manual — cần thiết bị thật (cảm ứng, bàn phím ảo, màn hình thật); Playwright giả lập không thay được | Giáo viên Lan trên điện thoại | • dữ liệu như AC SC1<br/>• điện thoại màn dọc 390×844 | • Không có dữ liệu nhập riêng | 1. mở Tổng quan trên điện thoại | • ba thẻ xếp một cột với cùng số liệu như web<br/>• liên kết tới Theo dõi nhanh nằm ở đầu trang<br/>• không có thanh cuộn ngang<br/>• nút và dòng bấm được cao ít nhất 44 điểm ảnh | US-S3E4-01 · SC2; CL-UI-01 |
| TC-S3E4-03 | Chưa có lớp | Medium | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • một giáo viên mới đăng ký, chưa có lớp nào | • Không có dữ liệu nhập riêng | 1. mở Tổng quan | • hiện MSG-S3-66 kèm nút "Tạo lớp" dẫn tới màn tạo lớp<br/>• không có ba thẻ rỗng | US-S3E4-01 · SC3; CL-FN-03 |
| TC-S3E4-04 | Có lớp nhưng không có việc | Medium | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • giáo viên có một lớp không có bộ Đã giao nào và không có em Chờ duyệt | • Không có dữ liệu nhập riêng | 1. mở Tổng quan | • hiện MSG-S3-65<br/>• không có thẻ nào mang số 0 gây rối ngoài lời chú | US-S3E4-01 · SC4; CL-FN-03 |
| TC-S3E4-05 | Sau khi giao một bộ mới, tổng quan thêm một dòng | Medium | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • tổng quan đang có 2 dòng "Bài đang giao"<br/>• bộ "Bộ bài tuần 16" Nháp (F-S3-052) | • Hàng dữ liệu: F-S3-052 | 1. giao "Bộ bài tuần 16"<br/>2. mở lại Tổng quan | • thẻ "Bài đang giao" có 3 dòng, dòng mới ghi "Bộ bài tuần 16 · 11A1" với 8 em chưa mở | US-S3E4-01 · SC5; CL-FN-01 |
| TC-S3E4-06 | Quá hạn vẫn còn trong "Bài đang giao" cho tới khi đóng | Medium | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • bộ tuần 15 đã quá hạn làm 04/12/2026 20:00 và chưa Đã đóng<br/>• giờ máy chủ 05/12/2026 (F-S3-050) | • Hàng dữ liệu: F-S3-050 | 1. mở Tổng quan | • dòng bộ tuần 15 vẫn có trong "Bài đang giao" kèm nhãn "Quá hạn"<br/>• không biến mất vì quá hạn | US-S3E4-01 · SC6; CL-FN-09 |
| TC-S3E4-07 | Lời giải đã mở rời khỏi thẻ "đang khóa" | Medium | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • bộ tuần 15 mở lời giải lúc 06/12/2026 08:00<br/>• giờ máy chủ 06/12/2026 08:01 (F-S3-050) | • Hàng dữ liệu: F-S3-050 | 1. mở Tổng quan | • dòng bộ tuần 15 không còn trong "Lời giải đang khóa" và có chip MSG-S3-64 "Đã mở" ở thẻ "Bài đang giao" | US-S3E4-01 · SC7; CL-FN-04 |
| TC-S3E4-08 | Biên số em chờ duyệt: 99 và 100 | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • lần lượt 99 rồi 100 học sinh Chờ duyệt ở một lớp (dữ liệu thử) | • Không có dữ liệu nhập riêng | 1. mở Tổng quan ở mỗi trường hợp | • với 99 thẻ ghi "99"<br/>• với 100 thẻ ghi "99+"<br/>• thân phản hồi vẫn chứa số đúng là 100 cho lần gọi API | US-S3E4-01 · FC1; CL-FN-12; PO-66 (QA-S3E4-03) |
| TC-S3E4-09 | Thẻ "Em chờ duyệt" ẩn khi 0 | Low | Abnormal (Fail case/Edge case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • không em nào Chờ duyệt ở các lớp của cô Lan | • Không có dữ liệu nhập riêng | 1. mở Tổng quan | • thẻ "Em chờ duyệt" không hiện<br/>• hai thẻ còn lại hiện bình thường | US-S3E4-01 · FC2; CL-FN-12 |
| TC-S3E4-10 | Giáo viên khác chỉ thấy lớp của mình | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Giáo viên Khôi (chủ lớp 10B, không phải chủ lớp 11A1) | • thầy Khôi đã đăng nhập<br/>• cô Lan có em Chờ duyệt và bộ tuần 15 (F-S3-002, F-S3-050) | • Hàng dữ liệu: F-S3-002, F-S3-050 | 1. thầy Khôi gọi API Tổng quan | • thân phản hồi chỉ có dữ liệu của 10B<br/>• không có tên lớp 11A1, 11A2, tên bộ tuần 15 hay số em Chờ duyệt của cô Lan | US-S3E4-01 · FC3; CL-FN-06 |
| TC-S3E4-11 | Tổng quan không chứa tên học sinh | High | Abnormal (Fail case/Edge case) | dữ liệu trả về | tích hợp API | E2E-API | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • dữ liệu như AC SC1 | • Không có dữ liệu nhập riêng | 1. gọi API Tổng quan và liệt kê mọi giá trị chuỗi | • không có tên hiển thị hay mã người dùng `HS-…` của học sinh nào<br/>• chỉ có số và tên lớp, tên bộ, giờ<br/>• có `Cache-Control: no-store` | US-S3E4-01 · FC4; CL-X-04 |
| TC-S3E4-12 | Học sinh không gọi được Tổng quan | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Học sinh Bình An (11A1, Đang học) | • Bình An đã đăng nhập vai Học sinh (F-S3-020) | • Hàng dữ liệu: F-S3-020 | 1. gọi API Tổng quan của giáo viên | • máy chủ trả FORBIDDEN<br/>• không có dữ liệu nào | US-S3E4-01 · FC5; CL-FN-05 |
| TC-S3E4-13 | Bộ Nháp không vào "Bài đang giao" | Medium | Abnormal (Fail case/Edge case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • "Bộ bài tuần 16" Nháp (F-S3-052) | • Hàng dữ liệu: F-S3-052 | 1. mở Tổng quan | • không có dòng "Bộ bài tuần 16" ở bất kỳ thẻ nào | US-S3E4-01 · FC6; CL-FN-04 |

### US-S3E4-02 — Theo dõi nhanh trên điện thoại: em chưa làm, Mở ngay, Đổi giờ

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S3E4-14 | Danh sách em chưa làm | High | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan trên điện thoại | • bộ tuần 15 của 11A1<br/>• chỉ Khánh Linh chưa có tương tác nào ở bộ (F-S3-051, F-S3-025) | • Hàng dữ liệu: F-S3-025, F-S3-051 | 1. mở Theo dõi nhanh trên điện thoại, chọn lớp 11A1 và "Bộ bài tuần 15" | • danh sách có đúng một dòng "Khánh Linh · `HS-7T7B-98QZ`"<br/>• không có em nào đã làm một phần (Bảo, Mai, Ngọc Anh)<br/>• không có điểm hay xếp hạng | US-S3E4-02 · SC1; CL-FN-01; PO-66 (QA-S3E4-01) |
| TC-S3E4-15 | Mở ngay: lời giải mở, thông báo, AuditEvent | High | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan trên điện thoại | • bộ tuần 15 Sắp mở (giờ mở 06/12/2026 08:00), giờ máy chủ 05/12/2026 21:00 (F-S3-050) | • Hàng dữ liệu: F-S3-050 | 1. bấm "Mở ngay"<br/>2. bấm "Mở ngay" ở hộp MSG-S3-61 | • giờ mở của bộ thành giờ máy chủ lúc xử lý (05/12/2026 21:00)<br/>• thẻ đổi sang chip MSG-S3-64 "Đã mở"<br/>• Bình An lấy được lời giải đầy đủ ngay yêu cầu kế tiếp<br/>• có một AuditEvent EARLY_RELEASE ghi giờ cũ 06/12/2026 08:00 và giờ mới 05/12/2026 21:00<br/>• 8 em Đang học nhận mỗi em một Notification "Lời giải Bộ bài tuần 15 đã mở" | US-S3E4-02 · SC2; CL-FN-04 |
| TC-S3E4-16 | Đổi giờ sang giờ tương lai hợp lệ | High | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan trên điện thoại | • bộ tuần 15 Sắp mở, giờ mở 06/12/2026 08:00<br/>• giờ máy chủ 05/12/2026 21:00 (F-S3-050) | • Hàng dữ liệu: F-S3-050<br/>• Giá trị nhập: "06/12/2026 19:00" | 1. bấm "Đổi giờ"<br/>2. chọn "06/12/2026 19:00"<br/>3. xác nhận | • giờ mở của bộ là 06/12/2026 19:00<br/>• thẻ vẫn Sắp mở<br/>• có một AuditEvent RELEASE_TIME_CHANGE ghi giờ cũ 08:00 và giờ mới 19:00 ngày 06/12/2026<br/>• 8 em Đang học nhận Notification "Giờ mở lời giải Bộ bài tuần 15 đổi sang 19:00, 06/12/2026" | US-S3E4-02 · SC3; CL-FN-04 |
| TC-S3E4-17 | Biên giờ mới: sau giờ hiện tại đúng 1 phút | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • bộ tuần 15 Sắp mở<br/>• giờ máy chủ 05/12/2026 21:00:00 (F-S3-050) | • Hàng dữ liệu: F-S3-050 | 1. gửi đổi giờ sang 21:01:00 cùng ngày<br/>2. gửi đổi giờ sang 21:00:59 cùng ngày | • giờ 21:01:00 được nhận<br/>• giờ 21:00:59 bị VALIDATION_FAILED kèm MSG-S3-63 và giờ mở giữ nguyên | US-S3E4-02 · SC4; CL-FN-09; PO-66 (QA-S3E4-04) |
| TC-S3E4-18 | Mở ngay khi bộ chưa đặt giờ | Medium | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan trên điện thoại | • bộ "Bộ bài tuần 16" đã giao, Chưa đặt giờ (F-S3-052) | • Hàng dữ liệu: F-S3-052 | 1. bấm "Mở ngay"<br/>2. xác nhận | • lời giải mở ngay<br/>• AuditEvent EARLY_RELEASE ghi giờ cũ là "chưa đặt" và giờ mới là giờ máy chủ | US-S3E4-02 · SC5; CL-FN-04 |
| TC-S3E4-19 | Đã mở thì Mở ngay và Đổi giờ bị chặn | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • bộ tuần 15 đã mở lời giải từ 06/12/2026 08:00 (F-S3-050) | • Hàng dữ liệu: F-S3-050 | 1. gửi "Mở ngay"<br/>2. gửi "Đổi giờ" sang 07/12/2026 08:00 | • cả hai trả CONFLICT kèm MSG-S3-67<br/>• giờ mở không đổi<br/>• không có AuditEvent hay Notification mới | US-S3E4-02 · FC1; CL-FN-04 |
| TC-S3E4-20 | Bấm Mở ngay hai lần chỉ một tác dụng | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • bộ tuần 15 Sắp mở (F-S3-050) | • Hàng dữ liệu: F-S3-050 | 1. gửi hai yêu cầu "Mở ngay" cùng khóa chống lặp, cách nhau 1 giây | • chỉ một AuditEvent EARLY_RELEASE<br/>• mỗi học sinh Đang học nhận đúng một Notification<br/>• hai phản hồi cùng kết quả thành công | US-S3E4-02 · FC2; CL-FN-10 |
| TC-S3E4-21 | Mất mạng thì nút tắt, không tự gửi lại | High | Abnormal (Fail case/Edge case) | thiết bị | thủ công trên thiết bị | Manual — cần thiết bị thật (cảm ứng, bàn phím ảo, màn hình thật); Playwright giả lập không thay được | Giáo viên Lan trên điện thoại | • cô Lan đang ở hộp MSG-S3-61 trên điện thoại<br/>• mạng bị cắt | • Không có dữ liệu nhập riêng | 1. bấm "Mở ngay"<br/>2. bật lại mạng và chờ 60 giây | • hiện MSG-S3-62<br/>• nút "Mở ngay" không báo đã thành công<br/>• sau khi có mạng không có yêu cầu nào tự gửi đi<br/>• lời giải vẫn khóa<br/>• không AuditEvent | US-S3E4-02 · FC3; CL-UI-04; PO-66 (QA-S3E4-05) |
| TC-S3E4-22 | Web và điện thoại đổi chồng nhau | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • bộ tuần 15 Sắp mở<br/>• cô Lan mở hai thiết bị cùng đọc lịch ở phiên bản số 3 (F-S3-050) | • Hàng dữ liệu: F-S3-050 | 1. web đổi giờ sang 06/12/2026 10:00<br/>2. điện thoại bấm "Mở ngay" với phiên bản số 3 | • yêu cầu của điện thoại bị CONFLICT<br/>• giờ mở là 06/12/2026 10:00<br/>• điện thoại tải lại thấy giờ mới<br/>• chỉ một AuditEvent | US-S3E4-02 · FC4; CL-FN-10 |
| TC-S3E4-23 | Giáo viên khác không Mở ngay được | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Giáo viên Khôi (chủ lớp 10B, không phải chủ lớp 11A1) | • bộ tuần 15 của 11A1 (F-S3-050) | • Hàng dữ liệu: F-S3-050 | 1. thầy Khôi gọi thẳng "Mở ngay" và "Đổi giờ" của bộ đó | • cả hai trả NOT_FOUND<br/>• giờ mở không đổi<br/>• không có Notification<br/>• có một sự kiện từ chối quan hệ ghi cho mỗi (người gọi, loại đối tượng) trong mỗi giờ, không phải cho mỗi yêu cầu (QA-S3E5-02, BR-S3E5-07, PO-76) | US-S3E4-02 · FC5; CL-FN-06 |
| TC-S3E4-24 | Học sinh không Mở ngay được | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Học sinh Bình An (11A1, Đang học) | • Bình An Đang học 11A1 (F-S3-020) | • Hàng dữ liệu: F-S3-020 | 1. gọi thẳng "Mở ngay" của bộ tuần 15 | • máy chủ trả FORBIDDEN<br/>• lời giải vẫn khóa<br/>• không có AuditEvent EARLY_RELEASE | US-S3E4-02 · FC6; CL-FN-05 |
| TC-S3E4-25 | Giờ điện thoại sai không ảnh hưởng | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan trên điện thoại | • đồng hồ điện thoại của cô Lan chạy nhanh 3 giờ so với máy chủ<br/>• bộ tuần 15 Sắp mở (F-S3-050) | • Hàng dữ liệu: F-S3-050 | 1. bấm "Mở ngay"<br/>2. gửi kèm giờ điện thoại trong thân yêu cầu | • giờ mở của bộ bằng giờ máy chủ<br/>• giờ điện thoại (trường `clientTime`) bị bỏ qua và không xuất hiện trong AuditEvent<br/>• gửi kèm khóa ngoài hợp đồng (`releaseAt`) bị từ chối VALIDATION_FAILED, bộ vẫn Sắp mở (chặng 3: BR-S3E5-34 · PO-61; FIX runner-fix-reg 2026-11-27) | US-S3E4-02 · FC7; CL-FN-09 |
| TC-S3E4-26 | Hủy ở hộp xác nhận không đổi gì | Medium | Abnormal (Fail case/Edge case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan trên điện thoại | • hộp MSG-S3-61 đang mở (F-S3-050) | • Hàng dữ liệu: F-S3-050 | 1. bấm "Hủy" | • lời giải vẫn khóa<br/>• không AuditEvent<br/>• không Notification | US-S3E4-02 · FC8; CL-FN-01 |
| TC-S3E4-27 | Hiển thị trên điện thoại màn dọc | Medium | Abnormal (Fail case/Edge case) | thiết bị | thủ công trên thiết bị | Manual — cần thiết bị thật (cảm ứng, bàn phím ảo, màn hình thật); Playwright giả lập không thay được | Giáo viên Lan trên điện thoại | • điện thoại màn dọc 390×844<br/>• bộ tuần 15 Sắp mở (F-S3-050) | • Hàng dữ liệu: F-S3-050 | 1. mở Theo dõi nhanh | • danh sách em chưa làm và hai nút "Mở ngay", "Đổi giờ" nằm trong một màn không cuộn ngang<br/>• nút cao ít nhất 44 điểm ảnh<br/>• chữ không bị cắt | US-S3E4-02 · FC9; CL-UI-01 |

### US-S3E4-03 — Notification trong ứng dụng cho học sinh (chưa đọc, đã đọc)

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S3E4-28 | Giao bộ thì 8 em Đang học nhận thông báo "bài mới" | High | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Học sinh Bình An trên iPad | • "Bộ bài tuần 16" vừa được giao cho 11A1<br/>• 8 em Đang học, 3 em Chờ duyệt (F-S3-052, F-S3-010) | • Hàng dữ liệu: F-S3-010, F-S3-030…032, F-S3-052 | 1. Bình An mở ứng dụng | • chấm thông báo ghi 1<br/>• danh sách có thông báo "Có bài mới: Bộ bài tuần 16" ở trạng thái Chưa đọc<br/>• 3 em Chờ duyệt (F-S3-030…032) không có thông báo nào | US-S3E4-03 · SC1; CL-FN-13 |
| TC-S3E4-29 | Mở ngay thì có thông báo "lời giải đã mở" | High | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Học sinh Bình An trên iPad | • cô Lan vừa "Mở ngay" bộ tuần 15 (F-S3-050) | • Hàng dữ liệu: F-S3-050 | 1. Bình An mở danh sách thông báo | • có "Lời giải Bộ bài tuần 15 đã mở" Chưa đọc<br/>• bấm vào đưa tới bộ tuần 15 với lời giải đã mở | US-S3E4-03 · SC2; CL-FN-13 |
| TC-S3E4-30 | Mở thông báo thì đánh dấu đã đọc | High | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Học sinh Bình An trên iPad | • Bình An có 2 thông báo Chưa đọc | • Không có dữ liệu nhập riêng | 1. bấm vào thông báo thứ nhất | • thông báo đó thành Đã đọc<br/>• chấm thông báo giảm từ 2 xuống 1<br/>• thông báo thứ hai vẫn Chưa đọc | US-S3E4-03 · SC3; CL-FN-13 |
| TC-S3E4-31 | Đánh dấu tất cả đã đọc | Medium | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Học sinh Bình An trên iPad | • Bình An có 5 thông báo Chưa đọc | • Không có dữ liệu nhập riêng | 1. bấm "Đánh dấu tất cả đã đọc" | • cả 5 thành Đã đọc<br/>• chấm thông báo biến mất<br/>• thông báo của các bạn không đổi | US-S3E4-03 · SC4; CL-FN-13 |
| TC-S3E4-32 | Đổi giờ thì thông báo ghi giờ mới, không lộ lời giải | Medium | Normal (Success case) | dữ liệu trả về | tích hợp API | E2E-API | Học sinh Bình An (11A1, Đang học) | • cô Lan vừa đổi giờ mở bộ tuần 15 sang 19:00, 06/12/2026 (F-S3-050) | • Hàng dữ liệu: F-S3-050 | 1. Bình An gọi API danh sách thông báo | • có "Giờ mở lời giải Bộ bài tuần 15 đổi sang 19:00, 06/12/2026"<br/>• thân thông báo không chứa đáp số, bước giải hay tên bạn nào | US-S3E4-03 · SC5; CL-FN-07 |
| TC-S3E4-33 | Em vào lớp sau khi giao không nhận thông báo cũ | Medium | Abnormal (Fail case/Edge case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Học sinh Bình An (11A1, Đang học) | • bộ tuần 15 giao trước khi Thu Hà được duyệt vào 11A1 (F-S3-031) | • Hàng dữ liệu: F-S3-031 | 1. Thu Hà được duyệt<br/>2. mở danh sách thông báo | • không có thông báo "Có bài mới" của bộ tuần 15<br/>• bộ tuần 15 vẫn có trong Bài của em | US-S3E4-03 · FC1; CL-FN-04; PO-66 (QA-S3E4-07) |
| TC-S3E4-34 | Biên chấm số: 99 và 100 thông báo chưa đọc | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Học sinh Bình An (11A1, Đang học) | • lần lượt 99 rồi 100 thông báo Chưa đọc của một học sinh thử | • Không có dữ liệu nhập riêng | 1. mở ứng dụng ở mỗi trường hợp | • với 99 chấm ghi "99"<br/>• với 100 chấm ghi "99+"<br/>• thân phản hồi chứa số đúng | US-S3E4-03 · FC2; CL-FN-12 |
| TC-S3E4-35 | Học sinh không đọc được thông báo của bạn | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Học sinh Bình An (11A1, Đang học) | • Chi Mai có một thông báo mang mã nội bộ đã biết (F-S3-021) | • Hàng dữ liệu: F-S3-021 | 1. Bình An gọi chi tiết thông báo đó và gọi danh sách thông báo | • chi tiết trả NOT_FOUND<br/>• danh sách chỉ có thông báo của Bình An | US-S3E4-03 · FC3; CL-FN-06 |
| TC-S3E4-36 | Chỉ thông báo trong ứng dụng, không gửi ra ngoài | Medium | Abnormal (Fail case/Edge case) | phi chức năng | tích hợp CSDL / đơn vị | IT | Kỹ sư kiểm thử với quyền đọc cơ sở dữ liệu thử | • nhà cung cấp gửi tin ngoài (push, email, SMS) được giả lập để đếm lượt gọi | • Không có dữ liệu nhập riêng | 1. giao bộ, Mở ngay và Đổi giờ ở 11A1 | • số lượt gọi nhà cung cấp gửi tin ngoài bằng 0<br/>• bảng cấu hình không có địa chỉ email, số điện thoại hay mã đăng ký đẩy của học sinh nào | US-S3E4-03 · FC4; CL-X-04 · ENG |
| TC-S3E4-37 | Sự kiện xử lý lặp không tạo hai thông báo | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp CSDL / đơn vị | IT | Hệ thống (tác vụ chạy nền, không có người dùng) | • sự kiện "bài mới" của bộ tuần 16 bị xử lý hai lần do thử lại | • Không có dữ liệu nhập riêng | 1. chạy lại bộ xử lý sự kiện | • mỗi học sinh Đang học vẫn đúng một thông báo cho sự kiện đó<br/>• ràng buộc duy nhất theo sự kiện và người nhận từ chối bản ghi trùng | US-S3E4-03 · FC5; CL-FN-10 · ENG |
| TC-S3E4-38 | Danh sách thông báo phân trang 20 dòng | Low | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Học sinh Bình An (11A1, Đang học) | • một học sinh có 45 thông báo | • Không có dữ liệu nhập riêng | 1. gọi trang đầu<br/>2. gọi trang kế bằng con trỏ<br/>3. gọi trang cuối<br/>4. gọi với con trỏ đã bị sửa | • ba trang có 20, 20, 5 dòng, mới nhất trước, không trùng<br/>• con trỏ bị sửa trả INVALID_CURSOR<br/>• không có tổng số bản ghi | US-S3E4-03 · FC6; CL-FN-08 |
| TC-S3E4-39 | Học sinh Đã rời không nhận thông báo mới | Medium | Abnormal (Fail case/Edge case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Học sinh Văn Tú (Đã rời 11A1) | • Văn Tú Đã rời 11A1 (F-S3-028) | • Hàng dữ liệu: F-S3-028 | 1. cô Lan giao "Bộ bài tuần 16"<br/>2. Văn Tú mở danh sách thông báo | • Văn Tú không có thông báo "Có bài mới: Bộ bài tuần 16" | US-S3E4-03 · FC7; CL-FN-04 |
| TC-S3E4-40 | Thông báo quá 90 ngày bị xóa | Low | Abnormal (Fail case/Edge case) | phi chức năng | tích hợp CSDL / đơn vị | IT | Hệ thống (tác vụ chạy nền, không có người dùng) | • hai thông báo, một tạo cách đây 89 ngày và một cách đây 91 ngày (dữ liệu thử) | • Không có dữ liệu nhập riêng | 1. chạy tác vụ dọn dẹp | • thông báo 91 ngày bị xóa<br/>• thông báo 89 ngày còn nguyên | US-S3E4-03 · FC8; CL-FN-09 · ENG; PO-66 (QA-S3E4-09) |
| TC-S3E4-41 | Học sinh không tạo được thông báo | Medium | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Học sinh Bình An (11A1, Đang học) | • Bình An đã đăng nhập vai Học sinh (F-S3-020) | • Hàng dữ liệu: F-S3-020 | 1. gọi thẳng yêu cầu tạo thông báo cho Chi Mai | • máy chủ trả FORBIDDEN<br/>• Chi Mai không có thông báo mới | US-S3E4-03 · FC9; CL-FN-05 |

## 4. Truy vết story → tiêu chí → case

| Story | Tiêu chí | Loại | Tiêu đề | Trạng thái DoR | Case |
|---|---|---|---|---|---|
| US-S3E4-01 | US-S3E4-01-SC1 | Success | Ba thẻ đúng số liệu trên web | Ready for R3 | TC-S3E4-01 |
| US-S3E4-01 | US-S3E4-01-SC2 | Success | Cùng dữ liệu trên điện thoại, xếp một cột | Ready for R3 | TC-S3E4-02 |
| US-S3E4-01 | US-S3E4-01-SC3 | Success | Chưa có lớp | Ready for R3 | TC-S3E4-03 |
| US-S3E4-01 | US-S3E4-01-SC4 | Success | Có lớp nhưng không có việc | Ready for R3 | TC-S3E4-04 |
| US-S3E4-01 | US-S3E4-01-SC5 | Success | Sau khi giao một bộ mới, tổng quan thêm một dòng | Ready for R3 | TC-S3E4-05 |
| US-S3E4-01 | US-S3E4-01-SC6 | Success | Quá hạn vẫn còn trong "Bài đang giao" cho tới khi đóng | Ready for R3 | TC-S3E4-06 |
| US-S3E4-01 | US-S3E4-01-SC7 | Success | Lời giải đã mở rời khỏi thẻ "đang khóa" | Ready for R3 | TC-S3E4-07 |
| US-S3E4-01 | US-S3E4-01-FC1 | Fail / biên | Biên số em chờ duyệt: 99 và 100 | Ready for R3 · PO-66 (QA-S3E4-03) | TC-S3E4-08 |
| US-S3E4-01 | US-S3E4-01-FC2 | Fail / biên | Thẻ "Em chờ duyệt" ẩn khi 0 | Ready for R3 | TC-S3E4-09 |
| US-S3E4-01 | US-S3E4-01-FC3 | Fail / biên | Giáo viên khác chỉ thấy lớp của mình | Ready for R3 | TC-S3E4-10 |
| US-S3E4-01 | US-S3E4-01-FC4 | Fail / biên | Tổng quan không chứa tên học sinh | Ready for R3 | TC-S3E4-11 |
| US-S3E4-01 | US-S3E4-01-FC5 | Fail / biên | Học sinh không gọi được Tổng quan | Ready for R3 | TC-S3E4-12 |
| US-S3E4-01 | US-S3E4-01-FC6 | Fail / biên | Bộ Nháp không vào "Bài đang giao" | Ready for R3 | TC-S3E4-13 |
| US-S3E4-02 | US-S3E4-02-SC1 | Success | Danh sách em chưa làm | Ready for R3 · PO-66 (QA-S3E4-01) | TC-S3E4-14 |
| US-S3E4-02 | US-S3E4-02-SC2 | Success | Mở ngay: lời giải mở, thông báo, AuditEvent | Ready for R3 | TC-S3E4-15 |
| US-S3E4-02 | US-S3E4-02-SC3 | Success | Đổi giờ sang giờ tương lai hợp lệ | Ready for R3 | TC-S3E4-16 |
| US-S3E4-02 | US-S3E4-02-SC4 | Success | Biên giờ mới: sau giờ hiện tại đúng 1 phút | Ready for R3 · PO-66 (QA-S3E4-04) | TC-S3E4-17 |
| US-S3E4-02 | US-S3E4-02-SC5 | Success | Mở ngay khi bộ chưa đặt giờ | Ready for R3 | TC-S3E4-18 |
| US-S3E4-02 | US-S3E4-02-FC1 | Fail / biên | Đã mở thì Mở ngay và Đổi giờ bị chặn | Ready for R3 | TC-S3E4-19 |
| US-S3E4-02 | US-S3E4-02-FC2 | Fail / biên | Bấm Mở ngay hai lần chỉ một tác dụng | Ready for R3 | TC-S3E4-20 |
| US-S3E4-02 | US-S3E4-02-FC3 | Fail / biên | Mất mạng thì nút tắt, không tự gửi lại | Ready for R3 · PO-66 (QA-S3E4-05) | TC-S3E4-21 |
| US-S3E4-02 | US-S3E4-02-FC4 | Fail / biên | Web và điện thoại đổi chồng nhau | Ready for R3 | TC-S3E4-22 |
| US-S3E4-02 | US-S3E4-02-FC5 | Fail / biên | Giáo viên khác không Mở ngay được | Ready for R3 | TC-S3E4-23 |
| US-S3E4-02 | US-S3E4-02-FC6 | Fail / biên | Học sinh không Mở ngay được | Ready for R3 | TC-S3E4-24 |
| US-S3E4-02 | US-S3E4-02-FC7 | Fail / biên | Giờ điện thoại sai không ảnh hưởng | Ready for R3 | TC-S3E4-25 |
| US-S3E4-02 | US-S3E4-02-FC8 | Fail / biên | Hủy ở hộp xác nhận không đổi gì | Ready for R3 | TC-S3E4-26 |
| US-S3E4-02 | US-S3E4-02-FC9 | Fail / biên | Hiển thị trên điện thoại màn dọc | Ready for R3 | TC-S3E4-27 |
| US-S3E4-03 | US-S3E4-03-SC1 | Success | Giao bộ thì 8 em Đang học nhận thông báo "bài mới" | Ready for R3 | TC-S3E4-28 |
| US-S3E4-03 | US-S3E4-03-SC2 | Success | Mở ngay thì có thông báo "lời giải đã mở" | Ready for R3 | TC-S3E4-29 |
| US-S3E4-03 | US-S3E4-03-SC3 | Success | Mở thông báo thì đánh dấu đã đọc | Ready for R3 | TC-S3E4-30 |
| US-S3E4-03 | US-S3E4-03-SC4 | Success | Đánh dấu tất cả đã đọc | Ready for R3 | TC-S3E4-31 |
| US-S3E4-03 | US-S3E4-03-SC5 | Success | Đổi giờ thì thông báo ghi giờ mới, không lộ lời giải | Ready for R3 | TC-S3E4-32 |
| US-S3E4-03 | US-S3E4-03-FC1 | Fail / biên | Em vào lớp sau khi giao không nhận thông báo cũ | Ready for R3 · PO-66 (QA-S3E4-07) | TC-S3E4-33 |
| US-S3E4-03 | US-S3E4-03-FC2 | Fail / biên | Biên chấm số: 99 và 100 thông báo chưa đọc | Ready for R3 | TC-S3E4-34 |
| US-S3E4-03 | US-S3E4-03-FC3 | Fail / biên | Học sinh không đọc được thông báo của bạn | Ready for R3 | TC-S3E4-35 |
| US-S3E4-03 | US-S3E4-03-FC4 | Fail / biên | Chỉ thông báo trong ứng dụng, không gửi ra ngoài | Ready for R3 | TC-S3E4-36 |
| US-S3E4-03 | US-S3E4-03-FC5 | Fail / biên | Sự kiện xử lý lặp không tạo hai thông báo | Ready for R3 | TC-S3E4-37 |
| US-S3E4-03 | US-S3E4-03-FC6 | Fail / biên | Danh sách thông báo phân trang 20 dòng | Ready for R3 | TC-S3E4-38 |
| US-S3E4-03 | US-S3E4-03-FC7 | Fail / biên | Học sinh Đã rời không nhận thông báo mới | Ready for R3 | TC-S3E4-39 |
| US-S3E4-03 | US-S3E4-03-FC8 | Fail / biên | Thông báo quá 90 ngày bị xóa | Ready for R3 · PO-66 (QA-S3E4-09) | TC-S3E4-40 |
| US-S3E4-03 | US-S3E4-03-FC9 | Fail / biên | Học sinh không tạo được thông báo | Ready for R3 | TC-S3E4-41 |

## 5. Change log

| No | Thay đổi | Bởi |
|---|---|---|
| 1 | bản đầu: 41 case cho 41 tiêu chí của S3-E4, có cột Tự động hóa cho mọi case; ready for R3 | QC |

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: 41 case của S3-E4 | runner-T-261126-14 (QC) |
| v0.2 | 2026-11-26 | đổi mã mẫu `HS-`/`GV-` có ký tự 0, 1, O, I sang ký tự hợp lệ (BR-S2E1-04), RV_s23 m-01 | runner-T-261126-15 (QC soát phản biện, RV_s23) |
| v0.3 | 2026-11-26 | Áp PO-66: đổi nhãn PROVISIONAL thành PO-66 | runner-T-261126-37 (BA/QC) |
| v0.4 | 2026-11-27 | TC-S3E4-23: kỳ vọng ≤1 hàng Từ chối/giờ/(người, loại) thay cho "mỗi yêu cầu một hàng" (PO-76) | runner-po-s3 (PO/BA) |
