Phiên bản: v0.1 · 2026-11-26
# S3-E6 — test case (Chữa bài từ kết quả sang Trình chiếu, xuất kết quả lớp, lưu trữ lớp, nhắc việc trong ứng dụng (P1))

> Chủ sở hữu: QC. Lập từ `stories_ac.md` cùng thư mục (mỗi tiêu chí có ít nhất một case) và `test_design.md`. **Trạng thái: ready for R3**; mức epic **P1**. Id ổn định; tiêu chí bị gạch bỏ thì case giữ id với N/A. AI là giả lập (D-GEO-047). Kỳ vọng bám Given / When / Then của AC, giá trị cụ thể lấy từ dữ liệu mẫu chung.

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
| `F-S3-020…027` | Học sinh Đang học của 11A1: Bình An `HS-4P9T-31WD`, Chi Mai `HS-8L2K-77AB`, Đức Huy `HS-3M7Q-05XN`, Gia Bảo `HS-9D4W-61CE`, Hà My `HS-2X6R-48PL`, Khánh Linh `HS-7T1B-90QZ`, Minh Quân `HS-5N8C-24TR`, Ngọc Anh `HS-6V3F-18HM` |
| `F-S3-028` | Văn Tú `HS-3H6J-72KP` · Đã rời 11A1 (còn dữ liệu bài làm cũ) |
| `F-S3-030…032` | Chờ duyệt ở 11A1: Hoàng Nam `HS-1Q8Z-40DF`, Thu Hà `HS-7C5J-63LS`, Hoàng Nam thứ hai `HS-9B2L-85VG` (trùng tên với em đầu) |
| `F-S3-040` | Lớp 10B: Mi Na `HS-2A5F-11BC`, Bình An thứ hai `HS-6Z1N-52YT` (trùng tên với Bình An của 11A1), hai em khác; Chi Mai `HS-8L2K-77AB` cũng Đang học 10B (em thuộc hai lớp của hai giáo viên) |
| `F-S3-041` | Lớp 11A2: Quốc Bảo `HS-3C9D-77AA`, Thảo Vy `HS-8F2E-33BB`, Tuấn Kiệt `HS-1G4H-99CC` |
| `F-S3-050` | Bộ "Bộ bài tuần 15" của 11A1 · Đã giao · hạn làm T6 04/12/2026 20:00 · giờ mở lời giải 06/12/2026 08:00 · bốn đề: Đề 1 SC-đáy, Đề 2 d(A,SBC), Đề 3 hai mặt phẳng, Đề 4 thể tích |
| `F-S3-051` | Bảng trạng thái 11A1 × bộ tuần 15 (Đề 1, 2, 3, 4): An Đúng, Xem lại, Đúng, Xem lại · Mai Đúng, Xem lại, Xem lại, Chưa làm · Huy Đúng, Đúng, Xem lại, Đúng · Bảo Xem lại, Xem lại, Đang làm, Chưa làm · My Đúng, Đúng, Đúng, Đúng · Linh Chưa làm bốn đề · Quân Đúng, Xem lại, Xem lại, Xem lại · Ngọc Anh Đúng, Đúng, Xem lại, Đang làm |
| `F-S3-052` | Bộ "Bộ bài tuần 16" của 11A1 · Nháp · chưa đặt hạn, chưa đặt giờ mở lời giải |
| `F-S3-053` | Bộ tuần 15 của 11A2 · Đã giao · Đề 3 có ba bài làm (Quốc Bảo, Thảo Vy, Tuấn Kiệt) |


### US-S3E6-01 — Chữa bài từ kết quả sang Trình chiếu

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S3E6-01 | Bấm "Chữa bài này" mở Trình chiếu đúng đề và bước | High | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • thẻ Đề 3 · hai mặt phẳng với bước dừng nhiều nhất là bước 2 "xác định giao tuyến của (SAB) và (SCD)"<br/>• lời giải Đề 3 đã mở (F-S3-051) | • Hàng dữ liệu: F-S3-051 | 1. ở Kết quả lớp bấm "Chữa bài này" trên thẻ Đề 3 | • Trình chiếu toàn màn hình mở với Đề 3 ở bước 2<br/>• nút "Hiện bước kế" và "Bước trước" dùng được<br/>• nút thoát đưa về Kết quả lớp đúng bộ và đúng thẻ | P1 · US-S3E6-01 · SC1; CL-FN-01 |
| TC-S3E6-02 | Lời giải còn khóa thì hỏi lại trước khi chiếu | High | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • bộ tuần 15 Sắp mở (giờ mở 06/12/2026 08:00)<br/>• giờ máy chủ 05/12/2026 (F-S3-050) | • Hàng dữ liệu: F-S3-050 | 1. bấm "Chữa bài này" trên thẻ Đề 3<br/>2. chọn "Hủy"<br/>3. bấm lại<br/>4. chọn "Tiếp tục" | • hộp MSG-S3-91 hiện ở mỗi lần bấm<br/>• "Hủy" không mở Trình chiếu<br/>• "Tiếp tục" mở Trình chiếu kèm chip "Máy học sinh vẫn khóa lời giải"<br/>• Bình An vẫn không lấy được lời giải (trạng thái Sắp mở) | P1 · US-S3E6-01 · SC2; CL-FN-04 |
| TC-S3E6-03 | Màn chiếu không có tên hay mã học sinh | High | Normal (Success case) | dữ liệu trả về | tích hợp API | E2E-API | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • Trình chiếu vừa mở từ thẻ Đề 3 (F-S3-051) | • Hàng dữ liệu: F-S3-051 | 1. soát giao diện, phần tử ẩn và thân phản hồi của yêu cầu tải Trình chiếu | • không có tên hiển thị, mã người dùng `HS-…`, số bài làm hay số em của lớp<br/>• chỉ có đề, hình và các bước lời giải | P1 · US-S3E6-01 · SC3; CL-X-04 |
| TC-S3E6-04 | Mỗi lần chữa bài ghi một sự kiện đo | Medium | Normal (Success case) | phi chức năng | tích hợp CSDL / đơn vị | IT | Kỹ sư kiểm thử với quyền đọc cơ sở dữ liệu thử | • bảng sự kiện đo của dữ liệu thử đang trống | • Không có dữ liệu nhập riêng | 1. cô Lan bấm "Chữa bài này" ở Đề 3<br/>2. thoát<br/>3. 10 giây sau bấm lại ở Đề 3 | • có đúng 2 sự kiện đo, mỗi sự kiện chỉ có mã giáo viên, mã đề, giờ máy chủ<br/>• không có tên hay mã học sinh | P1 · US-S3E6-01 · SC4; CL-FN-13 · ENG |
| TC-S3E6-05 | Thẻ dữ liệu thô không có nút | High | Abnormal (Fail case/Edge case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • Đề 4 chỉ có 4 bài làm nên hiện dữ liệu thô với MSG-S3-43 (F-S3-051) | • Hàng dữ liệu: F-S3-051 | 1. xem khung của Đề 4 | • không có nút "Chữa bài này"<br/>• chỉ có danh sách thô | P1 · US-S3E6-01 · FC1; CL-FN-04 |
| TC-S3E6-06 | Đề đã sửa phiên bản, bước không còn | Medium | Abnormal (Fail case/Edge case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • thẻ Đề 3 trỏ bước 5 nhưng Đề 3 vừa có phiên bản mới chỉ còn 4 bước | • Không có dữ liệu nhập riêng | 1. bấm "Chữa bài này" | • Trình chiếu mở ở bước 1 của phiên bản mới kèm MSG-S3-92<br/>• không lỗi, không màn trắng | P1 · US-S3E6-01 · FC2; CL-FN-10; PROVISIONAL QA-S3E6-01 |
| TC-S3E6-07 | Bấm hai lần trong 1 giây chỉ mở một phiên | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • thẻ Đề 3 (F-S3-051) | • Hàng dữ liệu: F-S3-051 | 1. gửi hai yêu cầu mở Trình chiếu từ thẻ cách nhau 1 giây | • chỉ một phiên Trình chiếu<br/>• chỉ một sự kiện đo | P1 · US-S3E6-01 · FC3; CL-FN-10; PROVISIONAL QA-S3E6-03 |
| TC-S3E6-08 | Giáo viên khác không mở được | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Giáo viên Khôi (chủ lớp 10B, không phải chủ lớp 11A1) | • thầy Khôi đã đăng nhập<br/>• thẻ Đề 3 của lớp 11A1 (F-S3-002) | • Hàng dữ liệu: F-S3-002 | 1. gọi thẳng yêu cầu chữa bài từ thẻ của 11A1 | • máy chủ trả NOT_FOUND<br/>• không có phiên Trình chiếu<br/>• không có sự kiện đo | P1 · US-S3E6-01 · FC4; CL-FN-06 |
| TC-S3E6-09 | Học sinh không gọi được | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Học sinh Bình An (11A1, Đang học) | • Bình An đã đăng nhập vai Học sinh (F-S3-020) | • Hàng dữ liệu: F-S3-020 | 1. gọi thẳng yêu cầu chữa bài từ thẻ | • máy chủ trả FORBIDDEN<br/>• không có phiên, không có sự kiện đo | P1 · US-S3E6-01 · FC5; CL-FN-05 |
| TC-S3E6-10 | Mất mạng khi bấm | Medium | Abnormal (Fail case/Edge case) | thiết bị | thủ công trên thiết bị | Manual — cần thiết bị thật (cảm ứng, bàn phím ảo, màn hình thật); Playwright giả lập không thay được | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • mạng bị cắt trước khi bấm "Chữa bài này" | • Không có dữ liệu nhập riêng | 1. bấm "Chữa bài này" | • hiện thông báo chưa mở được và gợi ý kiểm tra mạng<br/>• không màn trắng<br/>• không sự kiện đo giả | P1 · US-S3E6-01 · FC6; CL-UI-04 |

### US-S3E6-02 — Xuất kết quả lớp; lưu trữ lớp cuối năm

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S3E6-11 | Xuất bảng tuần 15 đúng hàng và cột | High | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • Kết quả lớp "Bộ bài tuần 15" của 11A1 như F-S3-051 đang mở (F-S3-050) | • Hàng dữ liệu: F-S3-050, F-S3-051 | 1. bấm "Xuất kết quả" | • tải về một tệp CSV có dòng tiêu đề "Tên hiển thị, Mã người dùng, Đã rời, Đề 1, Đề 2, Đề 3, Đề 4" và 8 dòng học sinh theo thứ tự tên<br/>• mỗi ô trạng thái khớp F-S3-051 (ví dụ dòng Khánh Linh: bốn ô "Chưa làm")<br/>• không cột hay dòng tổng, phần trăm, điểm hay hạng | P1 · US-S3E6-02 · SC1; CL-FN-01 |
| TC-S3E6-12 | Tên tệp không chứa tên học sinh | Medium | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • giờ máy chủ 07/12/2026<br/>• lớp "11A1"<br/>• bộ "Bộ bài tuần 15" | • Không có dữ liệu nhập riêng | 1. xuất kết quả | • tên tệp là `ket-qua-11A1-bo-bai-tuan-15-20261207.csv`<br/>• không chứa tên hay mã người dùng | P1 · US-S3E6-02 · SC2; CL-FN-12 |
| TC-S3E6-13 | Ô bắt đầu bằng dấu công thức được vô hiệu | High | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • một học sinh Đang học 11A1 có tên hiển thị "=HYPERLINK(""http://x"",""a"")" và một học sinh tên "+84 Lan" (tên hợp lệ, không phải 9 chữ số liền) | • Giá trị nhập: `=HYPERLINK("http://x","a")`<br/>• Giá trị nhập: "+84 Lan" | 1. xuất kết quả<br/>2. mở tệp trong bảng tính | • trong tệp hai ô tên bắt đầu bằng dấu nháy đơn trước `=` và `+`<br/>• bảng tính hiển thị đúng chuỗi như chữ, không chạy công thức, không mở liên kết | P1 · US-S3E6-02 · SC3; CL-X-03 |
| TC-S3E6-14 | Tệp xuất chỉ có trường được phép | High | Normal (Success case) | dữ liệu trả về | tích hợp API | E2E-API | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • tệp xuất của bộ tuần 15 | • Không có dữ liệu nhập riêng | 1. đọc mọi cột và mọi ô của tệp | • chỉ có tên hiển thị, mã người dùng, cờ đã rời và trạng thái ô<br/>• không có câu hỏi của học sinh, đáp số em nhập, địa chỉ IP, mã lớp, tên giáo viên | P1 · US-S3E6-02 · SC4; CL-X-04 |
| TC-S3E6-15 | Lưu trữ lớp: trạng thái, mã và hiển thị của học sinh | High | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • lớp 11A2 Đang dạy, mã `HK-8M3C`, 3 em Đang học (F-S3-011, F-S3-041) | • Hàng dữ liệu: F-S3-011, F-S3-041 | 1. bấm "Lưu trữ lớp"<br/>2. xác nhận ở hộp MSG-S3-93<br/>3. Quốc Bảo mở Bài của em | • 11A2 chuyển sang Đã lưu trữ và rời danh sách lớp Đang dạy<br/>• Quốc Bảo thấy 11A2 trong mục "Lớp đã lưu trữ" với MSG-S3-96, bài đã làm vẫn xem được, không có bộ mới | P1 · US-S3E6-02 · SC5; CL-FN-04 |
| TC-S3E6-16 | Lưu trữ lớp có em Chờ duyệt thì các em đó bị từ chối tự động | Medium | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • 11A1 có 3 em Chờ duyệt (F-S3-030…032) | • Hàng dữ liệu: F-S3-030…032 | 1. lưu trữ lớp 11A1<br/>2. Hoàng Nam `HS-1Q8Z-40DF` mở màn Vào lớp | • 3 em chuyển Bị từ chối<br/>• Hoàng Nam thấy trạng thái chưa vào được lớp và gợi ý hỏi giáo viên<br/>• danh sách Chờ duyệt của 11A1 trống | P1 · US-S3E6-02 · SC6; CL-FN-04; PROVISIONAL QA-S3E6-06 |
| TC-S3E6-17 | Bỏ lưu trữ thì lớp về Đang dạy với mã lớp mới | Medium | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • lớp 11A2 đã lưu trữ, mã cũ `HK-8M3C` đã ngừng dùng (F-S3-011) | • Hàng dữ liệu: F-S3-011 | 1. bấm "Bỏ lưu trữ"<br/>2. xác nhận | • 11A2 Đang dạy<br/>• có mã lớp mới khác `HK-8M3C`<br/>• 3 em Đang học thấy lại bài của lớp | P1 · US-S3E6-02 · SC7; CL-FN-04; PROVISIONAL QA-S3E6-07 |
| TC-S3E6-18 | Học sinh nhập mã của lớp đã lưu trữ | High | Abnormal (Fail case/Edge case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Học sinh Bình An trên iPad | • lớp 11A2 đã lưu trữ (F-S3-011) | • Hàng dữ liệu: F-S3-011 | 1. học sinh chưa thuộc lớp nào nhập mã `HK-8M3C` | • hiện MSG-S3-01 như mã sai<br/>• không tạo bản ghi vào lớp | P1 · US-S3E6-02 · FC1; CL-FN-03 |
| TC-S3E6-19 | Giao bộ mới cho lớp đã lưu trữ | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • lớp 11A2 đã lưu trữ (F-S3-011) | • Hàng dữ liệu: F-S3-011 | 1. gọi thẳng yêu cầu giao một bộ mới cho 11A2 | • máy chủ trả CONFLICT<br/>• không tạo bộ, không Notification | P1 · US-S3E6-02 · FC2; CL-FN-04 |
| TC-S3E6-20 | Xuất quá 10 lần trong 1 giờ | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • cô Lan đã xuất 10 lần trong 1 giờ | • Không có dữ liệu nhập riêng | 1. xuất lần thứ 11 trong cùng giờ | • RATE_LIMITED kèm `Retry-After` và MSG-S3-94<br/>• không có tệp<br/>• lần xuất sau khi hết giờ giới hạn thành công | P1 · US-S3E6-02 · FC3; CL-FN-10; PROVISIONAL QA-S3E6-05 |
| TC-S3E6-21 | Giáo viên khác không xuất được lớp 11A1 | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Giáo viên Khôi (chủ lớp 10B, không phải chủ lớp 11A1) | • thầy Khôi đã đăng nhập (F-S3-002) | • Hàng dữ liệu: F-S3-002 | 1. gọi thẳng yêu cầu xuất kết quả của 11A1, bộ tuần 15 | • máy chủ trả NOT_FOUND<br/>• không có tệp, không có tên em trong thân lỗi | P1 · US-S3E6-02 · FC4; CL-FN-06 |
| TC-S3E6-22 | Học sinh không xuất được | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Học sinh Bình An (11A1, Đang học) | • Bình An đã đăng nhập vai Học sinh (F-S3-020) | • Hàng dữ liệu: F-S3-020 | 1. gọi thẳng yêu cầu xuất kết quả của bộ tuần 15 | • máy chủ trả FORBIDDEN<br/>• không có tệp | P1 · US-S3E6-02 · FC5; CL-FN-05 |
| TC-S3E6-23 | Lưu trữ không tính vào giới hạn 20 lớp | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • một giáo viên có 20 lớp Đang dạy | • Không có dữ liệu nhập riêng | 1. lưu trữ một lớp<br/>2. tạo lớp mới | • lớp mới được tạo (19 lớp Đang dạy rồi 20)<br/>• giáo viên có tổng 21 lớp gồm 1 lớp Đã lưu trữ | P1 · US-S3E6-02 · FC6; CL-FN-02; PROVISIONAL QA-S3E1-05 |
| TC-S3E6-24 | Lớp đã lưu trữ không đặt lại mật khẩu | Medium | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • lớp 11A2 đã lưu trữ<br/>• Thảo Vy `HS-8F2E-33BB` thuộc 11A2 (F-S3-041) | • Hàng dữ liệu: F-S3-041 | 1. cô Lan gọi thẳng yêu cầu đặt lại mật khẩu cho Thảo Vy | • máy chủ trả NOT_FOUND<br/>• mật khẩu không đổi<br/>• không có AuditEvent thành công | P1 · US-S3E6-02 · FC7; CL-FN-05; PROVISIONAL QA-S3E6-06 |
| TC-S3E6-25 | Lịch mở lời giải của lớp đã lưu trữ vẫn chạy | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Học sinh Bình An (11A1, Đang học) | • bộ tuần 15 của 11A2 Sắp mở lúc 06/12/2026 08:00<br/>• lớp 11A2 đã lưu trữ ngày 05/12/2026 (F-S3-053) | • Hàng dữ liệu: F-S3-053 | 1. Quốc Bảo lấy chi tiết lời giải Đề 3 ở giờ máy chủ 06/12/2026 08:00:00 | • lời giải trả đầy đủ chỉ đọc<br/>• trước 08:00:00 vẫn khóa | P1 · US-S3E6-02 · FC8; CL-FN-09; PROVISIONAL QA-S3E6-06 |
| TC-S3E6-26 | Hủy ở hộp lưu trữ không đổi gì | Low | Abnormal (Fail case/Edge case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (chủ lớp 11A1 và 11A2) | • hộp MSG-S3-93 đang mở cho lớp 11A1 (F-S3-010) | • Hàng dữ liệu: F-S3-010 | 1. bấm "Hủy" | • 11A1 vẫn Đang dạy<br/>• mã `HK-4P7Q` vẫn dùng được | P1 · US-S3E6-02 · FC9; CL-FN-01 |

### US-S3E6-03 — Nhắc việc trong ứng dụng: bài mới, sắp hết hạn, lời giải đã mở

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S3E6-27 | Nhắc sắp hết hạn tới đúng bốn em chưa xong | High | Normal (Success case) | chức năng | tích hợp CSDL / đơn vị | IT | Hệ thống (tác vụ chạy nền, không có người dùng) | • bộ tuần 15 có hạn 04/12/2026 20:00<br/>• giờ máy chủ 03/12/2026 20:00:00<br/>• trạng thái ô như F-S3-051 (F-S3-050) | • Hàng dữ liệu: F-S3-050, F-S3-051 | 1. tác vụ nhắc chạy | • bốn em còn đề Chưa làm hoặc Đang làm là Chi Mai, Gia Bảo, Khánh Linh, Ngọc Anh mỗi em nhận đúng một thông báo MSG-S3-95 "Bộ bài tuần 15 sắp hết hạn: 20:00, 04/12/2026"<br/>• các em khác không nhận | P1 · US-S3E6-03 · SC1; CL-FN-13 · ENG; PROVISIONAL QA-S3E6-08 |
| TC-S3E6-28 | Em đã xong bộ không nhận nhắc | High | Normal (Success case) | chức năng | tích hợp CSDL / đơn vị | IT | Hệ thống (tác vụ chạy nền, không có người dùng) | • như AC SC1<br/>• Bình An, Đức Huy, Hà My, Minh Quân đã xong mọi đề (F-S3-051) | • Hàng dữ liệu: F-S3-051 | 1. tác vụ nhắc chạy ở 03/12/2026 20:00:00 | • bốn em này không có thông báo MSG-S3-95 | P1 · US-S3E6-03 · SC2; CL-FN-13 · ENG |
| TC-S3E6-29 | Biên giờ yên lặng 21:00 và 06:00 | High | Normal (Success case) | chức năng | tích hợp CSDL / đơn vị | IT | Hệ thống (tác vụ chạy nền, không có người dùng) | • ba bộ có giờ nhắc lần lượt rơi vào 20:59:59, 21:00:00 và 06:00:00 giờ Việt Nam (dữ liệu thử) | • Không có dữ liệu nhập riêng | 1. tác vụ nhắc chạy ở từng giờ đó | • nhắc 20:59:59 được gửi ngay<br/>• nhắc 21:00:00 hoãn tới 06:00 hôm sau<br/>• nhắc 06:00:00 được gửi ngay | P1 · US-S3E6-03 · SC3; CL-FN-09 · ENG; PROVISIONAL QA-S3E6-08 |
| TC-S3E6-30 | Nhắc rơi giữa đêm được hoãn tới 06:00 | Medium | Normal (Success case) | chức năng | tích hợp CSDL / đơn vị | IT | Hệ thống (tác vụ chạy nền, không có người dùng) | • bộ có hạn 05/12/2026 02:00 nên giờ nhắc là 04/12/2026 02:00<br/>• em Gia Bảo chưa xong (F-S3-024) | • Hàng dữ liệu: F-S3-024 | 1. tác vụ nhắc chạy lúc 02:00<br/>2. chạy lại lúc 06:00 | • lúc 02:00 không có thông báo<br/>• lúc 06:00 Gia Bảo nhận một thông báo MSG-S3-95 | P1 · US-S3E6-03 · SC4; CL-FN-09 · ENG; PROVISIONAL QA-S3E6-08 |
| TC-S3E6-31 | Tác vụ chạy lặp không gửi hai lần | High | Normal (Success case) | chức năng | tích hợp CSDL / đơn vị | IT | Hệ thống (tác vụ chạy nền, không có người dùng) | • tác vụ nhắc của AC SC1 đã chạy một lần | • Không có dữ liệu nhập riêng | 1. chạy lại tác vụ ba lần nữa | • mỗi em trong bốn em vẫn đúng một thông báo MSG-S3-95<br/>• ràng buộc duy nhất từ chối bản ghi trùng | P1 · US-S3E6-03 · SC5; CL-FN-10 · ENG |
| TC-S3E6-32 | Tới giờ mở lời giải thì thông báo cho em Đang học | High | Normal (Success case) | chức năng | tích hợp CSDL / đơn vị | IT | Hệ thống (tác vụ chạy nền, không có người dùng) | • bộ tuần 15 mở lời giải lúc 06/12/2026 08:00 theo lịch<br/>• 8 em Đang học, 3 em Chờ duyệt (F-S3-050, F-S3-010) | • Hàng dữ liệu: F-S3-010, F-S3-050 | 1. tác vụ chạy ở 06/12/2026 08:00:00 | • 8 em Đang học mỗi em nhận "Lời giải Bộ bài tuần 15 đã mở"<br/>• 3 em Chờ duyệt không nhận<br/>• chạy lại vẫn mỗi em một thông báo | P1 · US-S3E6-03 · SC6; CL-FN-13 · ENG |
| TC-S3E6-33 | Giáo viên đổi hạn thì nhắc theo hạn mới | Medium | Normal (Success case) | chức năng | tích hợp CSDL / đơn vị | IT | Hệ thống (tác vụ chạy nền, không có người dùng) | • bộ tuần 15 hạn 04/12/2026 20:00 có nhắc lên lịch 03/12/2026 20:00<br/>• cô Lan đổi hạn sang 05/12/2026 20:00 | • Không có dữ liệu nhập riêng | 1. tác vụ chạy ở 03/12/2026 20:00:00<br/>2. chạy ở 04/12/2026 20:00:00 | • lúc 03/12 không có nhắc<br/>• lúc 04/12 20:00:00 bốn em chưa xong nhận nhắc với hạn "20:00, 05/12/2026" | P1 · US-S3E6-03 · SC7; CL-FN-04 · ENG; PROVISIONAL QA-S3E6-08 |
| TC-S3E6-34 | Bộ giao khi còn dưới 24 giờ đến hạn | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp CSDL / đơn vị | IT | Hệ thống (tác vụ chạy nền, không có người dùng) | • bộ giao lúc 03/12/2026 21:00 với hạn 04/12/2026 20:00 (còn 23 giờ) | • Không có dữ liệu nhập riêng | 1. tác vụ nhắc chạy sau khi giao | • học sinh chỉ có thông báo "Có bài mới"<br/>• không có thông báo MSG-S3-95 | P1 · US-S3E6-03 · FC1; CL-FN-09 · ENG; PROVISIONAL QA-S3E6-08 |
| TC-S3E6-35 | Bộ không có hạn | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp CSDL / đơn vị | IT | Hệ thống (tác vụ chạy nền, không có người dùng) | • "Bộ bài tuần 16" Đã giao, không có hạn làm (F-S3-052) | • Hàng dữ liệu: F-S3-052 | 1. tác vụ nhắc chạy mỗi giờ trong 48 giờ giả lập | • không có thông báo MSG-S3-95 nào cho bộ này | P1 · US-S3E6-03 · FC2; CL-FN-09 · ENG |
| TC-S3E6-36 | Em Chờ duyệt, Đã rời, Bị từ chối không nhận nhắc | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp CSDL / đơn vị | IT | Hệ thống (tác vụ chạy nền, không có người dùng) | • Hoàng Nam Chờ duyệt, Văn Tú Đã rời, một em Bị từ chối ở 11A1<br/>• bộ tuần 15 sắp hết hạn (F-S3-030, F-S3-028) | • Hàng dữ liệu: F-S3-028, F-S3-030 | 1. tác vụ nhắc chạy | • ba em này không có thông báo nào của bộ tuần 15 | P1 · US-S3E6-03 · FC3; CL-FN-04 · ENG |
| TC-S3E6-37 | Bộ Đã đóng thì không nhắc | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp CSDL / đơn vị | IT | Hệ thống (tác vụ chạy nền, không có người dùng) | • bộ tuần 15 chuyển Đã đóng lúc 03/12/2026 10:00, trước giờ nhắc 20:00 | • Không có dữ liệu nhập riêng | 1. tác vụ nhắc chạy ở 03/12/2026 20:00:00 | • không có thông báo MSG-S3-95 | P1 · US-S3E6-03 · FC4; CL-FN-04 · ENG; PROVISIONAL QA-S3E6-08 |
| TC-S3E6-38 | Nội dung nhắc chỉ có tên bộ và giờ | High | Abnormal (Fail case/Edge case) | dữ liệu trả về | tích hợp API | E2E-API | Học sinh Bình An (11A1, Đang học) | • Gia Bảo vừa nhận thông báo MSG-S3-95 (F-S3-051) | • Hàng dữ liệu: F-S3-051 | 1. gọi API danh sách thông báo của Gia Bảo | • thông báo chỉ có tên bộ, giờ hạn và loại<br/>• không có đáp số, tiến độ, tên hay trạng thái của bạn<br/>• không có từ ngữ so sánh với lớp | P1 · US-S3E6-03 · FC5; CL-FN-07 |
| TC-S3E6-39 | Chỉ nhắc trong ứng dụng | Medium | Abnormal (Fail case/Edge case) | phi chức năng | tích hợp CSDL / đơn vị | IT | Hệ thống (tác vụ chạy nền, không có người dùng) | • nhà cung cấp gửi tin ngoài (push, email, SMS) được giả lập để đếm lượt gọi | • Không có dữ liệu nhập riêng | 1. chạy trọn bộ tác vụ nhắc của AC SC1, SC3 và SC6 | • số lượt gọi nhà cung cấp gửi tin ngoài bằng 0 | P1 · US-S3E6-03 · FC6; CL-X-04 · ENG |
| TC-S3E6-40 | Giờ gửi nhắc đúng 24 giờ trước hạn, sai số không quá 1 phút | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp CSDL / đơn vị | IT | Hệ thống (tác vụ chạy nền, không có người dùng) | • bộ có hạn 04/12/2026 20:00<br/>• tác vụ chạy mỗi phút | • Không có dữ liệu nhập riêng | 1. giả lập đồng hồ từ 03/12/2026 19:58 đến 03/12/2026 20:02 | • thông báo được tạo trong khoảng 20:00:00 đến 20:00:59 ngày 03/12/2026, không sớm hơn 20:00:00 và không muộn hơn 20:01:00 | P1 · US-S3E6-03 · FC7; CL-FN-09 · ENG; PROVISIONAL QA-S3E6-08 |

## 4. Truy vết story → tiêu chí → case

| Story | Tiêu chí | Loại | Tiêu đề | Trạng thái DoR | Case |
|---|---|---|---|---|---|
| US-S3E6-01 | US-S3E6-01-SC1 | Success | Bấm "Chữa bài này" mở Trình chiếu đúng đề và bước | Ready for R3 | TC-S3E6-01 |
| US-S3E6-01 | US-S3E6-01-SC2 | Success | Lời giải còn khóa thì hỏi lại trước khi chiếu | Ready for R3 | TC-S3E6-02 |
| US-S3E6-01 | US-S3E6-01-SC3 | Success | Màn chiếu không có tên hay mã học sinh | Ready for R3 | TC-S3E6-03 |
| US-S3E6-01 | US-S3E6-01-SC4 | Success | Mỗi lần chữa bài ghi một sự kiện đo | Ready for R3 | TC-S3E6-04 |
| US-S3E6-01 | US-S3E6-01-FC1 | Fail / biên | Thẻ dữ liệu thô không có nút | Ready for R3 | TC-S3E6-05 |
| US-S3E6-01 | US-S3E6-01-FC2 | Fail / biên | Đề đã sửa phiên bản, bước không còn | Ready for R3 · PROVISIONAL QA-S3E6-01 | TC-S3E6-06 |
| US-S3E6-01 | US-S3E6-01-FC3 | Fail / biên | Bấm hai lần trong 1 giây chỉ mở một phiên | Ready for R3 · PROVISIONAL QA-S3E6-03 | TC-S3E6-07 |
| US-S3E6-01 | US-S3E6-01-FC4 | Fail / biên | Giáo viên khác không mở được | Ready for R3 | TC-S3E6-08 |
| US-S3E6-01 | US-S3E6-01-FC5 | Fail / biên | Học sinh không gọi được | Ready for R3 | TC-S3E6-09 |
| US-S3E6-01 | US-S3E6-01-FC6 | Fail / biên | Mất mạng khi bấm | Ready for R3 | TC-S3E6-10 |
| US-S3E6-02 | US-S3E6-02-SC1 | Success | Xuất bảng tuần 15 đúng hàng và cột | Ready for R3 | TC-S3E6-11 |
| US-S3E6-02 | US-S3E6-02-SC2 | Success | Tên tệp không chứa tên học sinh | Ready for R3 | TC-S3E6-12 |
| US-S3E6-02 | US-S3E6-02-SC3 | Success | Ô bắt đầu bằng dấu công thức được vô hiệu | Ready for R3 | TC-S3E6-13 |
| US-S3E6-02 | US-S3E6-02-SC4 | Success | Tệp xuất chỉ có trường được phép | Ready for R3 | TC-S3E6-14 |
| US-S3E6-02 | US-S3E6-02-SC5 | Success | Lưu trữ lớp: trạng thái, mã và hiển thị của học sinh | Ready for R3 | TC-S3E6-15 |
| US-S3E6-02 | US-S3E6-02-SC6 | Success | Lưu trữ lớp có em Chờ duyệt thì các em đó bị từ chối tự động | Ready for R3 · PROVISIONAL QA-S3E6-06 | TC-S3E6-16 |
| US-S3E6-02 | US-S3E6-02-SC7 | Success | Bỏ lưu trữ thì lớp về Đang dạy với mã lớp mới | Ready for R3 · PROVISIONAL QA-S3E6-07 | TC-S3E6-17 |
| US-S3E6-02 | US-S3E6-02-FC1 | Fail / biên | Học sinh nhập mã của lớp đã lưu trữ | Ready for R3 | TC-S3E6-18 |
| US-S3E6-02 | US-S3E6-02-FC2 | Fail / biên | Giao bộ mới cho lớp đã lưu trữ | Ready for R3 | TC-S3E6-19 |
| US-S3E6-02 | US-S3E6-02-FC3 | Fail / biên | Xuất quá 10 lần trong 1 giờ | Ready for R3 · PROVISIONAL QA-S3E6-05 | TC-S3E6-20 |
| US-S3E6-02 | US-S3E6-02-FC4 | Fail / biên | Giáo viên khác không xuất được lớp 11A1 | Ready for R3 | TC-S3E6-21 |
| US-S3E6-02 | US-S3E6-02-FC5 | Fail / biên | Học sinh không xuất được | Ready for R3 | TC-S3E6-22 |
| US-S3E6-02 | US-S3E6-02-FC6 | Fail / biên | Lưu trữ không tính vào giới hạn 20 lớp | Ready for R3 · PROVISIONAL QA-S3E1-05 | TC-S3E6-23 |
| US-S3E6-02 | US-S3E6-02-FC7 | Fail / biên | Lớp đã lưu trữ không đặt lại mật khẩu | Ready for R3 · PROVISIONAL QA-S3E6-06 | TC-S3E6-24 |
| US-S3E6-02 | US-S3E6-02-FC8 | Fail / biên | Lịch mở lời giải của lớp đã lưu trữ vẫn chạy | Ready for R3 · PROVISIONAL QA-S3E6-06 | TC-S3E6-25 |
| US-S3E6-02 | US-S3E6-02-FC9 | Fail / biên | Hủy ở hộp lưu trữ không đổi gì | Ready for R3 | TC-S3E6-26 |
| US-S3E6-03 | US-S3E6-03-SC1 | Success | Nhắc sắp hết hạn tới đúng bốn em chưa xong | Ready for R3 · PROVISIONAL QA-S3E6-08 | TC-S3E6-27 |
| US-S3E6-03 | US-S3E6-03-SC2 | Success | Em đã xong bộ không nhận nhắc | Ready for R3 | TC-S3E6-28 |
| US-S3E6-03 | US-S3E6-03-SC3 | Success | Biên giờ yên lặng 21:00 và 06:00 | Ready for R3 · PROVISIONAL QA-S3E6-08 | TC-S3E6-29 |
| US-S3E6-03 | US-S3E6-03-SC4 | Success | Nhắc rơi giữa đêm được hoãn tới 06:00 | Ready for R3 · PROVISIONAL QA-S3E6-08 | TC-S3E6-30 |
| US-S3E6-03 | US-S3E6-03-SC5 | Success | Tác vụ chạy lặp không gửi hai lần | Ready for R3 | TC-S3E6-31 |
| US-S3E6-03 | US-S3E6-03-SC6 | Success | Tới giờ mở lời giải thì thông báo cho em Đang học | Ready for R3 | TC-S3E6-32 |
| US-S3E6-03 | US-S3E6-03-SC7 | Success | Giáo viên đổi hạn thì nhắc theo hạn mới | Ready for R3 · PROVISIONAL QA-S3E6-08 | TC-S3E6-33 |
| US-S3E6-03 | US-S3E6-03-FC1 | Fail / biên | Bộ giao khi còn dưới 24 giờ đến hạn | Ready for R3 · PROVISIONAL QA-S3E6-08 | TC-S3E6-34 |
| US-S3E6-03 | US-S3E6-03-FC2 | Fail / biên | Bộ không có hạn | Ready for R3 | TC-S3E6-35 |
| US-S3E6-03 | US-S3E6-03-FC3 | Fail / biên | Em Chờ duyệt, Đã rời, Bị từ chối không nhận nhắc | Ready for R3 | TC-S3E6-36 |
| US-S3E6-03 | US-S3E6-03-FC4 | Fail / biên | Bộ Đã đóng thì không nhắc | Ready for R3 · PROVISIONAL QA-S3E6-08 | TC-S3E6-37 |
| US-S3E6-03 | US-S3E6-03-FC5 | Fail / biên | Nội dung nhắc chỉ có tên bộ và giờ | Ready for R3 | TC-S3E6-38 |
| US-S3E6-03 | US-S3E6-03-FC6 | Fail / biên | Chỉ nhắc trong ứng dụng | Ready for R3 | TC-S3E6-39 |
| US-S3E6-03 | US-S3E6-03-FC7 | Fail / biên | Giờ gửi nhắc đúng 24 giờ trước hạn, sai số không quá 1 phút | Ready for R3 · PROVISIONAL QA-S3E6-08 | TC-S3E6-40 |

## 5. Change log

| No | Thay đổi | Bởi |
|---|---|---|
| 1 | bản đầu: 40 case cho 40 tiêu chí của S3-E6, có cột Tự động hóa cho mọi case; ready for R3 | QC |

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: 40 case của S3-E6 | runner-T-261126-14 (QC) |
