Phiên bản: v0.2 · 2026-11-26
# S3-E2 — test case (Bộ bài, lịch mở lời giải, Bài của em theo bộ)

> Chủ sở hữu: QC. Lập từ `stories_ac.md` cùng thư mục (mỗi tiêu chí có ít nhất một case) và `test_design.md`. **Trạng thái: ready for R3**; mức epic **P0**. Id ổn định; tiêu chí bị gạch bỏ thì case giữ id với N/A. AI là giả lập (D-GEO-047). Kỳ vọng bám Given / When / Then của AC, giá trị cụ thể lấy từ dữ liệu mẫu chung.

## 0. Reader guide and glossary

| Id / abbreviation | Meaning | Where used |
|---|---|---|
| `US-S3En-nn` · `-SCn` / `-FCn` | story (En = epic S3-En, nn = số story) · tiêu chí thành công / thất bại thứ n | tệp này; khóa bởi `work/s23/story-map-s23.md` |
| `BR-S3En-nn` | quy tắc nghiệp vụ của story (nguồn dẫn kèm) | tệp này |
| `TC-S3En-nn` | test case của epic S3-En | cột No của `test_cases.md` |
| `F-S3-nnn` | hàng dữ liệu mẫu chung của chặng 3 | mục "Dữ liệu mẫu" của tệp này |
| `QA-S3En-nn` | câu hỏi mở của epic, kèm mặc định đang dùng (TẠM) | `questions.md` cùng thư mục |
| `PO-nn` | dòng của sổ quyết định gốc: PO-53, PO-61…65 | `PO-DECISIONS.md` |
| `DQ-nn` | câu hỏi và mặc định của thiết kế chặng 2–3 | `Design/s23/DQ.md` |
| `M-S3-nn` | chỉ số chặng 3 (M-S3-01…06) | `work/s23/brd-delta-s23.md` §1 |
| `C3-nn` | mã màn hình chặng 3 | `STAGES-AFTER-MVP.md` Ch3 §6; ảnh ở `Design/s23/c3-*.png` |
| `D-GEO-nnn` | quyết định của MVP còn hiệu lực (ví dụ D-GEO-047 AI giả lập, D-GEO-185 khóa theo tài khoản) | `work/03_decisions.md` |
| `GV-…` · `HS-…` · `HK-…` · `RC-…` | mã người dùng giáo viên · học sinh · mã lớp · mã khôi phục (định dạng TẠM, DQ-16, DQ-23) | `Design/s23/DQ.md` |
| `release_at` · `due_at` | giờ mở lời giải · hạn làm (chỉ là nhãn, không khóa bài) | `STAGES-AFTER-MVP.md` Ch3 §7, §8 |
| Chưa đặt giờ · Sắp mở · Đã mở | ba trạng thái giờ mở: chưa có `release_at` (khóa) · giờ máy chủ < `release_at` (khóa) · giờ máy chủ ≥ `release_at` (được xem) | MVP, M-GEO-13 |
| Chờ duyệt · Đang học · Đã rời · Bị từ chối | trạng thái `ClassMembership` (Bị từ chối là đề xuất của BA, QA-S3E1-02) | tệp này |
| Đúng · Xem lại · Đang làm · Chưa làm | bốn trạng thái ô của bảng lớp × bài (nghĩa ở US-S3E3-01) | `Design/s23/c3-04-ket-qua-lop.png`, DQ-21 |
| Bài làm | một học sinh Đang học có ít nhất một tương tác (gửi đáp số, xin gợi ý hoặc hỏi) trên một đề của bộ; tính theo số học sinh khác nhau | US-S3E3-03, QA-S3E3-02 |
| T0 · t | giờ máy chủ lúc bắt đầu ca kiểm · giờ máy chủ tại thời điểm xét | mọi mốc giờ là tương đối |
| `CL-FN-nn` · `CL-UI-nn` · `CL-X-nn` | nhóm kiểm tra chức năng · giao diện và thiết bị · xuyên suốt (ma trận quyền, trường dữ liệu, tải) | `test_design.md` cùng thư mục |
| PROVISIONAL · ENG · X | kỳ vọng theo giả định chưa xác nhận (kèm mã QA-) · case cần công cụ kỹ thuật · case xuyên suốt | cột Remark |
| `MSG-S3-nn` | thông điệp hiện cho người dùng của chặng 3, câu chữ ở bảng "Thông điệp" của tệp này | tệp này |

- Mỗi dòng là một case. Chuẩn bị theo **Pre-condition**, dùng **Test data**, làm lần lượt các bước đánh số ở **Test step**, rồi so với **Expected output**; case đạt khi mọi gạch đầu dòng đều đúng.
- **Loại**: chức năng · phân quyền · dữ liệu trả về (soát thân phản hồi, không chỉ giao diện) · phi chức năng · thiết bị.
- **Tự động hóa** (PO-50): `E2E-UI` Playwright theo bước trên màn · `E2E-API` gọi HTTP thẳng, soát `code` và thân phản hồi · `IT` kiểm thử tích hợp hoặc đơn vị có CSDL thử, công cụ soát schema · `Manual` có lý do (thiết bị thật).
- **Status** của vòng thực thi chỉ dùng: Pass · Fail · Blocked (+ cần gì) · UT (+ lý do) · Not run. Chưa chạy: Not run. Kỳ vọng PROVISIONAL mà màn chỉ khác đúng điểm đó thì đánh Blocked kèm mã QA-, không đánh Fail.
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


### US-S3E2-01 — Soạn bộ bài: chọn đề, thứ tự, hạn làm (chỉ là nhãn), xem trước như học sinh

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S3E2-01 | Tạo bộ nháp từ bốn đề và lưu | High | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • cô Lan ở Kho đề, đã tick 4 đề: Đề 1 SC-đáy, Đề 2 d(A,SBC), Đề 3 hai mặt phẳng, Đề 4 thể tích (F-S3-050) | • Hàng dữ liệu: F-S3-050<br/>• Giá trị nhập: "Bộ bài tuần 16"<br/>• Giá trị nhập: `HK-4P7Q` lớp 11A1<br/>• Giá trị nhập: "11/12/2026 20:00" | 1. bấm "Tạo bộ bài"<br/>2. đặt tên "Bộ bài tuần 16", chọn lớp 11A1, hạn làm "11/12/2026 20:00"<br/>3. bấm "Lưu nháp" | • bộ "Bộ bài tuần 16" ở trạng thái Nháp với 4 đề theo thứ tự đã tick<br/>• học sinh 11A1 chưa thấy bộ này<br/>• ở tổng quan giáo viên chưa có dòng mới | US-S3E2-01 · SC1; CL-FN-01 |
| TC-S3E2-02 | Kéo thả đổi thứ tự đề, thứ tự lưu lại | Medium | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • bộ nháp "Bộ bài tuần 16" có 4 đề theo thứ tự 1, 2, 3, 4 (F-S3-052) | • Hàng dữ liệu: F-S3-052 | 1. kéo Đề 4 lên đầu<br/>2. bấm "Lưu nháp"<br/>3. tải lại trang | • thứ tự sau khi tải lại là Đề 4, Đề 1, Đề 2, Đề 3<br/>• khi giao, học sinh thấy đúng thứ tự này | US-S3E2-01 · SC2; CL-FN-01 |
| TC-S3E2-03 | Xem trước như học sinh không ghi gì | Medium | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • bộ nháp "Bộ bài tuần 16" (F-S3-052)<br/>• 11A1 đang có 8 em Đang học | • Hàng dữ liệu: F-S3-052 | 1. bấm "Xem trước như học sinh"<br/>2. làm thử Đề 1 đến hết (gõ đáp số, xin một gợi ý) | • màn xem trước giống màn Bài của em<br/>• ở Kết quả lớp không có ô, không có bài làm nào mới<br/>• không Notification nào được tạo<br/>• lời giải vẫn khóa theo lịch | US-S3E2-01 · SC3; CL-FN-07 |
| TC-S3E2-04 | Giao bộ: học sinh Đang học thấy, em Chờ duyệt không thấy | High | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • bộ nháp "Bộ bài tuần 16" đã chọn lớp 11A1 và hạn làm (F-S3-052)<br/>• 8 em Đang học, 3 em Chờ duyệt (F-S3-010) | • Hàng dữ liệu: F-S3-010, F-S3-030…032, F-S3-052 | 1. bấm "Giao"<br/>2. xác nhận | • bộ chuyển Đã giao<br/>• 8 em Đang học thấy "Bộ bài tuần 16" ở Bài của em<br/>• 3 em Chờ duyệt (F-S3-030…032) không thấy<br/>• tổng quan giáo viên thêm một dòng bài đang giao | US-S3E2-01 · SC4; CL-FN-01 |
| TC-S3E2-05 | Quá hạn chỉ là nhãn, em vẫn làm được | High | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Học sinh Bình An trên iPad | • bộ tuần 15 có hạn làm 04/12/2026 20:00 và giờ máy chủ là 04/12/2026 20:01 (F-S3-050) | • Hàng dữ liệu: F-S3-050 | 1. Bình An mở Đề 1 và gửi đáp số | • bài hiện chip MSG-S3-28 "Quá hạn"<br/>• đáp số được nhận và tính vào tiến độ như bình thường<br/>• không có chức năng nào khóa bài vì quá hạn | US-S3E2-01 · SC5; CL-FN-09 |
| TC-S3E2-06 | Bộ đúng 20 đề được nhận | Medium | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • cô Lan có 20 đề khác nhau để chọn | • Giá trị nhập: 20 đề | 1. tạo bộ gồm đúng 20 đề<br/>2. bấm "Giao" | • bộ được giao<br/>• học sinh thấy 20 đề đúng thứ tự | US-S3E2-01 · SC6; CL-FN-02 |
| TC-S3E2-07 | Bộ không có đề không giao được | Medium | Abnormal (Fail case/Edge case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • bộ nháp chưa có đề nào, đã chọn lớp 11A1 | • Không có dữ liệu nhập riêng | 1. bấm "Giao" | • hiện MSG-S3-23<br/>• máy chủ trả VALIDATION_FAILED nếu gọi thẳng<br/>• bộ vẫn Nháp | US-S3E2-01 · FC1; CL-FN-02 |
| TC-S3E2-08 | Bộ 21 đề bị chặn | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • cô Lan có 21 đề khác nhau để chọn | • Giá trị nhập: đề thứ 21 | 1. thêm đề thứ 21 vào bộ đã có 20 đề | • hiện MSG-S3-23<br/>• VALIDATION_FAILED nếu gọi thẳng<br/>• bộ vẫn 20 đề | US-S3E2-01 · FC2; CL-FN-02; PROVISIONAL QA-S3E2-01 |
| TC-S3E2-09 | Thêm trùng một đề vào bộ | Medium | Abnormal (Fail case/Edge case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • bộ nháp "Bộ bài tuần 16" đã có Đề 1 SC-đáy (F-S3-052) | • Hàng dữ liệu: F-S3-052 | 1. thêm lại Đề 1 SC-đáy | • hiện MSG-S3-24<br/>• bộ vẫn một lần Đề 1 | US-S3E2-01 · FC3; CL-FN-02 |
| TC-S3E2-10 | Chưa chọn lớp thì nút Giao chờ chọn | Medium | Abnormal (Fail case/Edge case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • bộ nháp có 4 đề, chưa chọn lớp | • Không có dữ liệu nhập riêng | 1. nhìn nút Giao<br/>2. gọi thẳng yêu cầu giao | • nút "Giao" không bấm được và có chú thích MSG-S3-25<br/>• yêu cầu gọi thẳng trả VALIDATION_FAILED<br/>• bộ vẫn Nháp | US-S3E2-01 · FC4; CL-FN-03 |
| TC-S3E2-11 | Chọn lớp của giáo viên khác | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • cô Lan tạo bộ nháp có 4 đề | • Hàng dữ liệu: F-S3-012 | 1. gọi thẳng yêu cầu tạo bộ với lớp 10B của thầy Khôi (F-S3-012) | • máy chủ trả NOT_FOUND<br/>• không tạo bộ<br/>• thân lỗi không chứa tên lớp 10B | US-S3E2-01 · FC5; CL-FN-06 |
| TC-S3E2-12 | Chọn đề Của tôi của giáo viên khác | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • thầy Khôi có một đề "Của tôi" riêng chưa vào Kho chung (F-S3-002) | • Hàng dữ liệu: F-S3-002 | 1. cô Lan gọi thẳng yêu cầu thêm đề đó vào bộ của cô | • máy chủ trả NOT_FOUND<br/>• bộ không đổi | US-S3E2-01 · FC6; CL-FN-06 |
| TC-S3E2-13 | Hạn làm trong quá khứ khi giao | Low | Abnormal (Fail case/Edge case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • bộ nháp có hạn làm đặt ở quá khứ so với giờ máy chủ | • Không có dữ liệu nhập riêng | 1. bấm "Giao" | • hiện cảnh báo "hạn làm đã qua" và cho tiếp tục nếu giáo viên xác nhận<br/>• sau khi giao học sinh thấy chip "Quá hạn" ngay | US-S3E2-01 · FC7; CL-FN-09; PROVISIONAL QA-S3E2-02 |
| TC-S3E2-14 | Bấm Giao hai lần chỉ giao một lần | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • bộ nháp "Bộ bài tuần 16" sẵn sàng giao (F-S3-052) | • Hàng dữ liệu: F-S3-052 | 1. gửi hai yêu cầu Giao liên tiếp cùng khóa yêu cầu | • chỉ có một bộ Đã giao<br/>• mỗi học sinh Đang học nhận đúng một Notification "bài mới"<br/>• tổng quan giáo viên có một dòng | US-S3E2-01 · FC8; CL-FN-10 |

### US-S3E2-02 — Lịch mở lời giải theo bộ hoặc từng bài; cảnh báo giờ mở sớm hơn hạn

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S3E2-15 | Đặt giờ mở cho cả bộ | High | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • bộ nháp "Bộ bài tuần 16" có 4 đề, hạn làm 11/12/2026 20:00 (F-S3-052) | • Hàng dữ liệu: F-S3-052 | 1. ở "Lịch mở lời giải" chọn "Cả bộ"<br/>2. đặt "13/12/2026 08:00"<br/>3. bấm "Lưu nháp" | • cả 4 đề cùng có giờ mở lời giải 13/12/2026 08:00<br/>• không hiện cảnh báo vì giờ mở sau hạn làm | US-S3E2-02 · SC1; CL-FN-01 |
| TC-S3E2-16 | Giờ mở riêng của một đề ghi đè giờ của bộ | Medium | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • bộ nháp có giờ mở chung 13/12/2026 08:00 (F-S3-052) | • Hàng dữ liệu: F-S3-052 | 1. chọn "Từng bài"<br/>2. đặt Đề 3 mở "14/12/2026 08:00"<br/>3. lưu | • Đề 1, 2, 4 mở lúc 13/12/2026 08:00<br/>• Đề 3 mở lúc 14/12/2026 08:00<br/>• sau khi giao, ở Bài của em mỗi đề hiện đúng giờ mở của mình | US-S3E2-02 · SC2; CL-FN-01 |
| TC-S3E2-17 | Biên giờ mở: đúng giờ mở là đã mở | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Học sinh Bình An (11A1, Đang học) | • bộ tuần 15 có `release_at` 06/12/2026 08:00 giờ Việt Nam (F-S3-050)<br/>• Bình An Đang học | • Hàng dữ liệu: F-S3-050 | 1. lấy chi tiết lời giải Đề 1 ở giờ máy chủ 07:59:59, rồi ở đúng 08:00:00, rồi ở 08:00:01 | • lúc 07:59:59 trả trạng thái Sắp mở và thân phản hồi không có lời giải<br/>• lúc 08:00:00 và 08:00:01 trả lời giải đầy đủ<br/>• mọi thân trả lời có `Cache-Control: no-store` | US-S3E2-02 · SC3; CL-FN-09 |
| TC-S3E2-18 | Bộ chưa đặt giờ thì lời giải khóa | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Học sinh Bình An (11A1, Đang học) | • bộ "Bộ bài tuần 16" đã giao nhưng chưa đặt giờ mở (F-S3-052) | • Hàng dữ liệu: F-S3-052 | 1. Bình An lấy chi tiết lời giải của từng đề | • trạng thái Chưa đặt giờ<br/>• không thân phản hồi nào chứa đáp số, bước giải hay gợi ý giải<br/>• màn hiện "Chưa có giờ mở lời giải" | US-S3E2-02 · SC4; CL-FN-07 |
| TC-S3E2-19 | Bộ câu thử moi đáp số không lấy được lời giải đang khóa | High | Normal (Success case) | dữ liệu trả về | tích hợp CSDL / đơn vị | IT | Kỹ sư kiểm thử với quyền đọc cơ sở dữ liệu thử | • bộ tuần 15 ở trạng thái Sắp mở (giờ máy chủ trước 06/12/2026 08:00) với đáp số mẫu của Đề 1 là giá trị đã biết (F-S3-050) | • Hàng dữ liệu: F-S3-050 | 1. chạy bộ câu thử moi đáp số của học sinh qua mọi kênh: danh sách bộ, chi tiết đề, gợi ý mức 1, tìm kiếm, thông báo, bộ nhớ đệm trình duyệt, phản hồi lỗi, đổi tiêu đề ngôn ngữ, gọi lại với `If-None-Match` | • không phản hồi nào chứa đáp số mẫu hay bước giải ở bất kỳ dạng viết tương đương nào<br/>• không có phản hồi nào bị lưu bộ nhớ đệm dùng chung | US-S3E2-02 · SC5; CL-X-03 · ENG |
| TC-S3E2-20 | Giờ mở sớm hơn hạn làm thì cảnh báo | High | Abnormal (Fail case/Edge case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • bộ nháp có hạn làm 11/12/2026 20:00 (F-S3-052) | • Hàng dữ liệu: F-S3-052 | 1. đặt giờ mở "10/12/2026 08:00" cho cả bộ | • cột cài đặt hiện banner đỏ MSG-S3-21 kèm hai cách sửa "Dời giờ mở sau hạn" và "Giữ giờ mở sớm"<br/>• nút "Giao" chờ giáo viên chọn một trong hai<br/>• không tự chặn giao | US-S3E2-02 · FC1; CL-FN-09 |
| TC-S3E2-21 | Biên cảnh báo: giờ mở bằng đúng hạn thì không cảnh báo | Medium | Abnormal (Fail case/Edge case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • bộ nháp có hạn làm 11/12/2026 20:00 (F-S3-052) | • Hàng dữ liệu: F-S3-052 | 1. đặt giờ mở "11/12/2026 20:00" rồi đổi thành "11/12/2026 19:59" | • với 20:00 không có cảnh báo<br/>• với 19:59 hiện MSG-S3-21 | US-S3E2-02 · FC2; CL-FN-09; PROVISIONAL QA-S3E2-03 |
| TC-S3E2-22 | Giáo viên chọn "Giữ giờ mở sớm" thì giao được và có ghi nhận | Medium | Abnormal (Fail case/Edge case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • bộ nháp đang có banner MSG-S3-21 (F-S3-052) | • Hàng dữ liệu: F-S3-052 | 1. bấm "Giữ giờ mở sớm"<br/>2. bấm "Giao" | • bộ được giao với giờ mở đã đặt<br/>• màn Bài của em hiện đúng giờ mở sớm đó<br/>• không có AuditEvent vì đây là lần đặt đầu của bản nháp (QA-S3E5-03) | US-S3E2-02 · FC3; CL-FN-09 |
| TC-S3E2-23 | Học sinh đổi đồng hồ máy không mở được lời giải | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Học sinh Bình An (11A1, Đang học) | • bộ tuần 15 ở trạng thái Sắp mở<br/>• đồng hồ thiết bị của Bình An chỉnh sang 10/12/2026 (F-S3-050) | • Hàng dữ liệu: F-S3-050 | 1. Bình An lấy chi tiết lời giải kèm tiêu đề giờ thiết bị | • máy chủ vẫn trả Sắp mở và không có lời giải<br/>• giờ thiết bị bị bỏ qua | US-S3E2-02 · FC4; CL-FN-09 |
| TC-S3E2-24 | Đổi giờ khi lời giải đã mở | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • bộ tuần 15 đã mở lời giải từ 06/12/2026 08:00<br/>• giờ máy chủ là 06/12/2026 09:00 (F-S3-050) | • Hàng dữ liệu: F-S3-050 | 1. cô Lan gửi yêu cầu đổi giờ mở sang 07/12/2026 08:00 | • máy chủ trả CONFLICT kèm MSG-S3-26<br/>• giờ mở không đổi<br/>• không AuditEvent đổi giờ thành công | US-S3E2-02 · FC5; CL-FN-04 |
| TC-S3E2-25 | Hai lần đổi giờ chồng nhau | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • bộ tuần 15 ở trạng thái Sắp mở<br/>• cô Lan mở hai tab cùng đọc phiên bản lịch số 3 (F-S3-050) | • Hàng dữ liệu: F-S3-050 | 1. tab một lưu giờ "06/12/2026 10:00"<br/>2. tab hai lưu giờ "06/12/2026 12:00" với phiên bản số 3 | • lần của tab hai bị CONFLICT<br/>• giờ mở là 06/12/2026 10:00<br/>• tab hai thấy giờ hiện hành sau khi tải lại | US-S3E2-02 · FC6; CL-FN-10 |
| TC-S3E2-26 | Giáo viên khác không đặt được lịch của lớp cô Lan | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Giáo viên Khôi (chủ lớp 10B, không phải chủ lớp 11A1) | • bộ tuần 15 của 11A1 (F-S3-050) | • Hàng dữ liệu: F-S3-050 | 1. thầy Khôi gọi thẳng yêu cầu đổi giờ mở của bộ đó | • máy chủ trả NOT_FOUND<br/>• giờ mở không đổi | US-S3E2-02 · FC7; CL-FN-05 |
| TC-S3E2-27 | Học sinh không đặt được lịch | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Học sinh Bình An (11A1, Đang học) | • Bình An Đang học 11A1 (F-S3-020) | • Hàng dữ liệu: F-S3-020 | 1. Bình An gọi thẳng yêu cầu đặt giờ mở của bộ tuần 15 | • máy chủ trả FORBIDDEN<br/>• giờ mở không đổi | US-S3E2-02 · FC8; CL-FN-05 |
| TC-S3E2-28 | Giờ mở sai định dạng | Low | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • bộ nháp "Bộ bài tuần 16" (F-S3-052) | • Hàng dữ liệu: F-S3-052<br/>• Giá trị nhập: "31/02/2026 08:00" | 1. gửi giờ mở "31/02/2026 08:00" rồi giờ mở rỗng kèm cờ "đặt giờ" | • cả hai bị VALIDATION_FAILED với `details` chỉ ra trường giờ mở<br/>• giờ mở của bộ không đổi | US-S3E2-02 · FC9; CL-FN-02 |

### US-S3E2-03 — Bài của em theo bộ; liệt kê đề "Cần xem lại" trước khi giao

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S3E2-29 | Bài của em hiện theo bộ với hạn, tiến độ, giờ mở | High | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Học sinh Bình An trên iPad | • Bình An Đang học 11A1, đã làm 2 trên 4 đề của bộ tuần 15 (F-S3-020, F-S3-050) | • Hàng dữ liệu: F-S3-020, F-S3-050 | 1. mở Bài của em trên iPad | • có một nhóm "11A1" chứa "Bộ bài tuần 15" với hạn "T6 04/12/2026 20:00", tiến độ "2 / 4", giờ mở lời giải "06/12/2026 08:00" và trạng thái Sắp mở<br/>• bốn đề theo đúng thứ tự của giáo viên | US-S3E2-03 · SC1; CL-UI-01 |
| TC-S3E2-30 | Học sinh ở hai lớp thấy hai nhóm | Medium | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Học sinh Chi Mai (Đang học cả 11A1 và 10B) | • Chi Mai Đang học 11A1 và 10B<br/>• mỗi lớp có một bộ Đã giao (F-S3-021, F-S3-040) | • Hàng dữ liệu: F-S3-021, F-S3-040 | 1. mở Bài của em | • có hai nhóm "11A1" và "10B", mỗi nhóm chỉ chứa bộ của lớp đó<br/>• không nhóm nào lộ tên giáo viên của lớp kia | US-S3E2-03 · SC2; CL-FN-06 |
| TC-S3E2-31 | Chưa có bài | Medium | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Học sinh Bình An trên iPad | • học sinh mới vào lớp 11A2, lớp chưa giao bộ nào (F-S3-041) | • Hàng dữ liệu: F-S3-041 | 1. mở Bài của em | • hiện MSG-S3-27<br/>• không có nhóm nào | US-S3E2-03 · SC3; CL-FN-03 |
| TC-S3E2-32 | Màn Giao liệt kê đề "Cần xem lại" | High | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • bộ nháp "Bộ bài tuần 16" có Đề 3 mang nhãn máy "Cần xem lại" (F-S3-052) | • Hàng dữ liệu: F-S3-052 | 1. bấm "Giao" | • trước khi giao hiện banner MSG-S3-22 và danh sách đúng một đề "Đề 3 · hai mặt phẳng" viền vàng<br/>• giáo viên chọn "Vẫn giao" hoặc "Bỏ đề khỏi bộ"<br/>• chưa chọn thì bộ chưa được giao | US-S3E2-03 · SC4; CL-FN-11 |
| TC-S3E2-33 | Giáo viên chọn "Vẫn giao" thì bộ giao và đề giữ nhãn | Medium | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • banner MSG-S3-22 đang hiện với Đề 3 (F-S3-052) | • Hàng dữ liệu: F-S3-052 | 1. bấm "Vẫn giao" | • bộ chuyển Đã giao với đủ 4 đề<br/>• nhãn máy của Đề 3 vẫn là "Cần xem lại" ở Kho đề của giáo viên<br/>• học sinh không thấy nhãn đó (BR-S3E2-23) | US-S3E2-03 · SC5; CL-FN-11 |
| TC-S3E2-34 | Học sinh Chờ duyệt không thấy bộ | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Học sinh Hoàng Nam (Chờ duyệt ở 11A1) | • Hoàng Nam Chờ duyệt ở 11A1<br/>• bộ tuần 15 đã giao (F-S3-030, F-S3-050) | • Hàng dữ liệu: F-S3-030, F-S3-050 | 1. Hoàng Nam mở Bài của em và gọi thẳng chi tiết bộ tuần 15 | • Bài của em hiện MSG-S3-27 hoặc chỉ Tự học<br/>• chi tiết bộ trả NOT_FOUND | US-S3E2-03 · FC1; CL-FN-05 |
| TC-S3E2-35 | Bộ Nháp không hiện cho học sinh | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Học sinh Bình An (11A1, Đang học) | • bộ "Bộ bài tuần 16" đang Nháp (F-S3-052) | • Hàng dữ liệu: F-S3-052 | 1. Bình An mở Bài của em và gọi thẳng chi tiết bộ đó | • Bài của em không có bộ tuần 16<br/>• chi tiết trả NOT_FOUND | US-S3E2-03 · FC2; CL-FN-05 |
| TC-S3E2-36 | Tiến độ của bạn không lộ | High | Abnormal (Fail case/Edge case) | dữ liệu trả về | tích hợp API | E2E-API | Học sinh Bình An (11A1, Đang học) | • Bình An Đang học 11A1<br/>• các bạn đã làm nhiều đề của bộ tuần 15 (F-S3-051) | • Hàng dữ liệu: F-S3-051 | 1. Bình An gọi Bài của em và chi tiết bộ tuần 15, soát toàn bộ thân phản hồi | • thân phản hồi chỉ có tiến độ của Bình An<br/>• không có tên, mã người dùng, số lượng hay trạng thái của bạn nào<br/>• không có tổng hay xếp hạng của lớp | US-S3E2-03 · FC3; CL-FN-07 |
| TC-S3E2-37 | Bộ bài hiện đúng trên iPhone màn dọc | Medium | Abnormal (Fail case/Edge case) | thiết bị | thủ công trên thiết bị | Manual — cần thiết bị thật (cảm ứng, bàn phím ảo, màn hình thật); Playwright giả lập không thay được | Học sinh Bình An trên iPhone | • Bình An dùng iPhone trình duyệt web màn dọc 390×844 (F-S3-020) | • Hàng dữ liệu: F-S3-020 | 1. mở Bài của em | • nhóm lớp và thẻ bộ xếp một cột<br/>• chip hạn, tiến độ, giờ mở đều đọc được không cắt chữ<br/>• không có thanh cuộn ngang | US-S3E2-03 · FC4; CL-UI-01 |

## 4. Truy vết story → tiêu chí → case

| Story | Tiêu chí | Loại | Tiêu đề | Trạng thái DoR | Case |
|---|---|---|---|---|---|
| US-S3E2-01 | US-S3E2-01-SC1 | Success | Tạo bộ nháp từ bốn đề và lưu | Ready for R3 | TC-S3E2-01 |
| US-S3E2-01 | US-S3E2-01-SC2 | Success | Kéo thả đổi thứ tự đề, thứ tự lưu lại | Ready for R3 | TC-S3E2-02 |
| US-S3E2-01 | US-S3E2-01-SC3 | Success | Xem trước như học sinh không ghi gì | Ready for R3 | TC-S3E2-03 |
| US-S3E2-01 | US-S3E2-01-SC4 | Success | Giao bộ: học sinh Đang học thấy, em Chờ duyệt không thấy | Ready for R3 | TC-S3E2-04 |
| US-S3E2-01 | US-S3E2-01-SC5 | Success | Quá hạn chỉ là nhãn, em vẫn làm được | Ready for R3 | TC-S3E2-05 |
| US-S3E2-01 | US-S3E2-01-SC6 | Success | Bộ đúng 20 đề được nhận | Ready for R3 | TC-S3E2-06 |
| US-S3E2-01 | US-S3E2-01-FC1 | Fail / biên | Bộ không có đề không giao được | Ready for R3 | TC-S3E2-07 |
| US-S3E2-01 | US-S3E2-01-FC2 | Fail / biên | Bộ 21 đề bị chặn | Ready for R3 · PROVISIONAL QA-S3E2-01 | TC-S3E2-08 |
| US-S3E2-01 | US-S3E2-01-FC3 | Fail / biên | Thêm trùng một đề vào bộ | Ready for R3 | TC-S3E2-09 |
| US-S3E2-01 | US-S3E2-01-FC4 | Fail / biên | Chưa chọn lớp thì nút Giao chờ chọn | Ready for R3 | TC-S3E2-10 |
| US-S3E2-01 | US-S3E2-01-FC5 | Fail / biên | Chọn lớp của giáo viên khác | Ready for R3 | TC-S3E2-11 |
| US-S3E2-01 | US-S3E2-01-FC6 | Fail / biên | Chọn đề Của tôi của giáo viên khác | Ready for R3 | TC-S3E2-12 |
| US-S3E2-01 | US-S3E2-01-FC7 | Fail / biên | Hạn làm trong quá khứ khi giao | Ready for R3 · PROVISIONAL QA-S3E2-02 | TC-S3E2-13 |
| US-S3E2-01 | US-S3E2-01-FC8 | Fail / biên | Bấm Giao hai lần chỉ giao một lần | Ready for R3 | TC-S3E2-14 |
| US-S3E2-02 | US-S3E2-02-SC1 | Success | Đặt giờ mở cho cả bộ | Ready for R3 | TC-S3E2-15 |
| US-S3E2-02 | US-S3E2-02-SC2 | Success | Giờ mở riêng của một đề ghi đè giờ của bộ | Ready for R3 | TC-S3E2-16 |
| US-S3E2-02 | US-S3E2-02-SC3 | Success | Biên giờ mở: đúng giờ mở là đã mở | Ready for R3 | TC-S3E2-17 |
| US-S3E2-02 | US-S3E2-02-SC4 | Success | Bộ chưa đặt giờ thì lời giải khóa | Ready for R3 | TC-S3E2-18 |
| US-S3E2-02 | US-S3E2-02-SC5 | Success | Bộ câu thử moi đáp số không lấy được lời giải đang khóa | Ready for R3 | TC-S3E2-19 |
| US-S3E2-02 | US-S3E2-02-FC1 | Fail / biên | Giờ mở sớm hơn hạn làm thì cảnh báo | Ready for R3 | TC-S3E2-20 |
| US-S3E2-02 | US-S3E2-02-FC2 | Fail / biên | Biên cảnh báo: giờ mở bằng đúng hạn thì không cảnh báo | Ready for R3 · PROVISIONAL QA-S3E2-03 | TC-S3E2-21 |
| US-S3E2-02 | US-S3E2-02-FC3 | Fail / biên | Giáo viên chọn "Giữ giờ mở sớm" thì giao được và có ghi nhận | Ready for R3 | TC-S3E2-22 |
| US-S3E2-02 | US-S3E2-02-FC4 | Fail / biên | Học sinh đổi đồng hồ máy không mở được lời giải | Ready for R3 | TC-S3E2-23 |
| US-S3E2-02 | US-S3E2-02-FC5 | Fail / biên | Đổi giờ khi lời giải đã mở | Ready for R3 | TC-S3E2-24 |
| US-S3E2-02 | US-S3E2-02-FC6 | Fail / biên | Hai lần đổi giờ chồng nhau | Ready for R3 | TC-S3E2-25 |
| US-S3E2-02 | US-S3E2-02-FC7 | Fail / biên | Giáo viên khác không đặt được lịch của lớp cô Lan | Ready for R3 | TC-S3E2-26 |
| US-S3E2-02 | US-S3E2-02-FC8 | Fail / biên | Học sinh không đặt được lịch | Ready for R3 | TC-S3E2-27 |
| US-S3E2-02 | US-S3E2-02-FC9 | Fail / biên | Giờ mở sai định dạng | Ready for R3 | TC-S3E2-28 |
| US-S3E2-03 | US-S3E2-03-SC1 | Success | Bài của em hiện theo bộ với hạn, tiến độ, giờ mở | Ready for R3 | TC-S3E2-29 |
| US-S3E2-03 | US-S3E2-03-SC2 | Success | Học sinh ở hai lớp thấy hai nhóm | Ready for R3 | TC-S3E2-30 |
| US-S3E2-03 | US-S3E2-03-SC3 | Success | Chưa có bài | Ready for R3 | TC-S3E2-31 |
| US-S3E2-03 | US-S3E2-03-SC4 | Success | Màn Giao liệt kê đề "Cần xem lại" | Ready for R3 | TC-S3E2-32 |
| US-S3E2-03 | US-S3E2-03-SC5 | Success | Giáo viên chọn "Vẫn giao" thì bộ giao và đề giữ nhãn | Ready for R3 | TC-S3E2-33 |
| US-S3E2-03 | US-S3E2-03-FC1 | Fail / biên | Học sinh Chờ duyệt không thấy bộ | Ready for R3 | TC-S3E2-34 |
| US-S3E2-03 | US-S3E2-03-FC2 | Fail / biên | Bộ Nháp không hiện cho học sinh | Ready for R3 | TC-S3E2-35 |
| US-S3E2-03 | US-S3E2-03-FC3 | Fail / biên | Tiến độ của bạn không lộ | Ready for R3 | TC-S3E2-36 |
| US-S3E2-03 | US-S3E2-03-FC4 | Fail / biên | Bộ bài hiện đúng trên iPhone màn dọc | Ready for R3 | TC-S3E2-37 |

## 5. Change log

| No | Thay đổi | Bởi |
|---|---|---|
| 1 | bản đầu: 37 case cho 37 tiêu chí của S3-E2, có cột Tự động hóa cho mọi case; ready for R3 | QC |

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: 37 case của S3-E2 | runner-T-261126-14 (QC) |
| v0.2 | 2026-11-26 | đổi mã mẫu `HS-`/`GV-` có ký tự 0, 1, O, I sang ký tự hợp lệ (BR-S2E1-04), RV_s23 m-01 | runner-T-261126-15 (QC soát phản biện, RV_s23) |
