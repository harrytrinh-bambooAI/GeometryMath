# E1 — test case (Nền tảng, cổng AI, bộ đề chuẩn, thử tải)

> Chủ sở hữu: QC. **Tệp sinh ra, không sửa tay**: sửa `work/src/qc/E1_meta.txt` (phân loại từng tiêu chí), `work/src/qc/E1_extra.md` (case viết tay) hoặc `work/src/qc/E1_override.txt` (chỉnh của QC theo một D-GEO khi AC chưa kịp đổi; mỗi chỉnh ghi nguồn ở Remark) rồi chạy `python3 work/src/qc/gen_cases.py E1`. Tiền điều kiện, bước và kỳ vọng của case bám tiêu chí lấy nguyên từ Given / When / Then của `stories_ac.md` (BA); giá trị cụ thể là của `work/fixture.md` và công thức M-GEO. Id ổn định; tiêu chí bị gạch bỏ thì case giữ id với N/A. AI ở MVP là giả lập (D-GEO-047): không có kỳ vọng về chất lượng AI thật.

## 0. Reader guide and glossary

| Id / abbreviation | Meaning | Where used |
|---|---|---|
| `TC-GEO-nnnn` | test case; hàng trăm là epic, 0800…0899 là case xuyên epic | cột No |
| `US-GEO-nnn` · `SCn` / `FCn` | story của BA · tiêu chí thành công / thất bại của story đó | cột Remark; `stories_ac.md` cùng thư mục |
| `F-GEO-nnn` | hàng dữ liệu mẫu: 001…199 ở `work/fixture.md` (BA), 200…299 ở `review/QC_test_design.md` §6 (QC), 300…399 dữ liệu thử tải (ARCH) | cột Test data |
| `MSG-GEO-nn` | mã thông báo; câu chữ nguyên văn in ngay trước mã, lấy từ `work/05_dictionary.md` | cột Expected output |
| `NFR-GEO-nnn` | yêu cầu phi chức năng của ARCH (`work/architecture/06_nfr.md`); ngưỡng là đề xuất | cột Remark |
| `D-GEO-nnn` | quyết định hoặc giả định của PO (`work/03_decisions.md`) | cột Remark, sau chữ PROVISIONAL |
| `QA-GEO-nn` | hỏi đáp còn mở của BA / QC / ARCH (`questions.md` cùng thư mục) | cột Remark |
| `BR-GEO-nn` | quy tắc nghiệp vụ (`work/02_gate2_brd.md`; 100…199 do BA làm rõ) | cột Remark |
| `REQ-GEO-nn` | yêu cầu thô (`work/00_gate0_intake_voc.md`) | cột Remark |
| `CL-FN-nn` | nhóm kiểm tra chức năng (`review/QC_test_design.md` §1) | cột Remark |
| `CL-UI-nn` | nhóm kiểm tra giao diện và thiết bị | cột Remark |
| `HKG-nn` | mã bài của Kho đề | dữ liệu |
| `BG-n`, `BG-C` | bài giao mẫu của `work/fixture.md` mục 3 | dữ liệu |
| `KG-nn` | known gap (`work/known_gaps.md`) | cột Remark |
| `PO-nn` | quyết định trong sổ quyết định của chủ sản phẩm | cột Remark |
| `RV-GEO-nn` | phát hiện của phản biện RV (`review/RV_findings.md`) | cột Remark |
| `M-nnn` | tin nhắn trên bảng chung `board/BOARD.md` | cột Remark |
| PROVISIONAL · ENG · X · N/A | kỳ vọng theo giả định chưa xác nhận · case cần công cụ kỹ thuật · case xuyên suốt không bám một tiêu chí · case giữ id nhưng không chạy | cột Remark |
| R · t · T0 | giờ mở lời giải (`release_at`) · giờ máy chủ · giờ máy chủ lúc bắt đầu ca kiểm | ca giờ mở |
| Chưa đặt giờ · Sắp mở · Đã mở | ba trạng thái giờ mở: chưa có giờ (khóa) · t < R (khóa) · t ≥ R (được xem) | kỳ vọng |
| Đã kiểm chứng · Cần xem lại · AI soạn | nhãn máy do engine tính · nhãn máy · nhãn người | kỳ vọng |

## 1. Tóm tắt

| Story | Số tiêu chí (SC / FC) | Số case | PROVISIONAL |
|---|---|---|---|
| US-GEO-101 Học sinh vào lớp bằng tên hiển thị và mã lớp | 12 (7 / 5) | 12 | 12 |
| US-GEO-102 Giáo viên mẫu vào hệ thống | 11 (4 / 7) | 11 | 11 |
| US-GEO-103 Máy chủ gọi AI qua một cổng | 7 (4 / 3) | 7 | 6 |
| US-GEO-104 Cách ly dữ liệu theo trường và phân quyền ở máy chủ (xuyên suốt) | 14 (5 / 9) | 15 | 4 |
| US-GEO-105 Chạy bộ đề chuẩn và đo tỉ lệ đúng | 8 (5 / 3) | 8 | 4 |
| US-GEO-106 Dữ liệu: đề chuẩn và đáp số tham chiếu | 6 (4 / 2) | 6 | 1 |
| US-GEO-107 Thử tải 1000 người dùng đồng thời | 7 (5 / 2) | 7 | 6 |
| US-GEO-108 Dữ liệu: bộ dữ liệu thử tải | 5 (3 / 2) | 5 | 2 |
| X (xuyên suốt, phi chức năng) | — | 4 | 1 |
| **Tổng** | **70** | **75** | **47** |

| Theo ưu tiên | Số case |
|---|---|
| High | 45 |
| Medium | 29 |
| Low | 1 |

| Theo loại | Số case |
|---|---|
| chức năng | 28 |
| phân quyền | 22 |
| dữ liệu trả về | 9 |
| phi chức năng | 14 |
| thiết bị | 2 |

| Theo tầng tự động hóa (một case có thể ở hai tầng) | Số case |
|---|---|
| đơn vị | 3 |
| tích hợp API | 46 |
| tích hợp CSDL | 15 |
| E2E trình duyệt | 0 |
| tải | 8 |
| thủ công trên thiết bị | 20 |

## 2. Test case

### Cách đọc một test case
- Mỗi dòng là một case. Chuẩn bị theo **Pre-condition**, dùng giá trị ở **Test data**, làm lần lượt các bước đánh số ở **Test step**, rồi so với **Expected output**.
- **Expected output**: mỗi gạch đầu dòng là một điều phải đúng; chữ trong ngoặc kép và giá trị phải khớp nguyên văn. Case đạt khi mọi gạch đầu dòng đều đúng.
- **Loại**: chức năng · phân quyền · dữ liệu trả về (soát thân phản hồi của máy chủ, không chỉ giao diện) · phi chức năng · thiết bị.
- **Tầng tự động hóa**: đơn vị · tích hợp API · tích hợp CSDL · E2E trình duyệt (Playwright) · tải (Gatling) · thủ công trên thiết bị; đếm câu lệnh JDBC dùng datasource-proxy (D-GEO-170). Case có chữ ENG ở Remark cần công cụ kỹ thuật và thuộc tờ kỹ thuật; case còn lại người kiểm thử chạy trên màn.
- **PROVISIONAL**: kỳ vọng theo một giả định chưa được xác nhận, mã ghi ngay sau. Vẫn chạy; nếu màn chỉ khác ở đúng điểm đó thì ghi lại điều thấy và đánh dấu Blocked, không đánh Fail.
- Thực tế khác kỳ vọng thì ghi vào vòng thực thi, không sửa ô Expected.

### US-GEO-101 — Học sinh vào lớp bằng tên hiển thị và mã lớp

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0100 | Vào lớp bằng tên và mã lớp đúng | High | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | Học sinh An (11A1) | • Lớp 11A1 có mã lớp `HK7M2A` và học sinh An (F-GEO-010, F-GEO-020)<br/>• An đang ở màn "Vào lớp" | • Hàng dữ liệu: F-GEO-010, F-GEO-020, F-GEO-040…042<br/>• Giá trị nhập: `HK7M2A`<br/>• Giá trị nhập: "An" | 1. An gõ Tên hiển thị "An", Mã lớp `HK7M2A`<br/>2. Bấm "Vào lớp" | • Màn "Bài của em" mở với dòng "Chào An" và đúng ba bài giao của 11A1: BG-1, BG-2, BG-3 (F-GEO-040…042) | US-GEO-101 · SC1; CL-FN-01; PROVISIONAL D-GEO-048 |
| TC-GEO-0101 | Tên chưa có trong lớp thì thành học sinh mới của lớp đó | Medium | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | Học sinh mới "Yến" | • Lớp 11A1 chưa có học sinh tên "Yến" (F-GEO-028) | • Hàng dữ liệu: F-GEO-028<br/>• Giá trị nhập: `HK7M2A`<br/>• Giá trị nhập: "Yến" | 1. Gõ "Yến" và `HK7M2A`, bấm "Vào lớp" | • "Bài của em" hiện "Chào Yến", ba bài giao của 11A1, mỗi bài tiến độ 0/3 "Chưa bắt đầu"<br/>• Theo dõi lớp 11A1 của Giáo viên mẫu từ lúc đó có sáu học sinh | US-GEO-101 · SC2; CL-FN-01; PROVISIONAL D-GEO-106; khôi phục: xóa học sinh Yến sau case (nếu không, số "4 / 5" của Theo dõi lớp thành "4 / 6") |
| TC-GEO-0102 | Vào lại với cùng tên thì giữ đúng dữ liệu của mình | High | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | Học sinh An, thiết bị thứ hai | • An đã có tiến độ 2/3 trên BG-1 (F-GEO-050) | • Hàng dữ liệu: F-GEO-050<br/>• Giá trị nhập: `HK7M2A`<br/>• Giá trị nhập: "an" | 1. Trên một thiết bị khác, gõ "an" (chữ thường) và `HK7M2A`, bấm "Vào lớp" | • Vào đúng tài khoản An: BG-1 hiện tiến độ 2/3 "Đã xem gợi ý 2"<br/>• Không tạo học sinh thứ hai tên "an" | US-GEO-101 · SC3; CL-FN-10; PROVISIONAL D-GEO-106 |
| TC-GEO-0103 | Mã lớp quyết định trường và lớp | High | Normal (Success case) | phân quyền | tích hợp API + thủ công trên thiết bị | Học sinh An của lớp 9C (trường khác) | • Trường "THCS Mẫu C" có lớp 9C mã `MC9C6R` với một học sinh cũng tên An (F-GEO-012, F-GEO-026) | • Hàng dữ liệu: F-GEO-012, F-GEO-026, F-GEO-045<br/>• Giá trị nhập: `MC9C6R`<br/>• Giá trị nhập: "An" | 1. Gõ "An" và `MC9C6R`, bấm "Vào lớp" | • "Bài của em" chỉ có BG-C (F-GEO-045)<br/>• Không có BG-1, BG-2, BG-3<br/>• Tiến độ của An lớp 11A1 không hiện ở đây | US-GEO-101 · SC4; CL-FN-06; PROVISIONAL D-GEO-051 |
| TC-GEO-0104 | Màn "Vào lớp" chỉ có hai ô nhập | High | Normal (Success case) | dữ liệu trả về | tích hợp API + thủ công trên thiết bị | Chưa vào lớp | • Người dùng chưa vào lớp | • Không có dữ liệu nhập riêng | 1. Mở màn "Vào lớp" | • Chỉ có ô "Tên hiển thị", ô "Mã lớp", nút "Vào lớp" và liên kết "Tôi là giáo viên"<br/>• Không có ô email, số điện thoại, ngày sinh<br/>• Yêu cầu gửi lên máy chủ chỉ chứa tên hiển thị và mã lớp | US-GEO-101 · SC5; CL-X-04; PROVISIONAL D-GEO-067 |
| TC-GEO-0105 | Tên dài đúng 60 ký tự được nhận | Medium | Normal (Success case) | chức năng | tích hợp API | Học sinh tên 60 ký tự | • Tên gồm 60 ký tự "A" (F-GEO-027) | • Hàng dữ liệu: F-GEO-027<br/>• Giá trị nhập: `HK7M2A` | 1. Gõ tên đó và `HK7M2A`, bấm "Vào lớp" | • Vào được "Bài của em" | US-GEO-101 · SC6; CL-FN-02; PROVISIONAL QA-GEO-01; ENG; BVA cận trên hợp lệ; khôi phục: xóa học sinh vừa tạo |
| TC-GEO-0106 | Dùng được trên iPhone màn dọc | High | Normal (Success case) | thiết bị | thủ công trên thiết bị | Chưa vào lớp, iPhone | • IPhone, trình duyệt web, màn dọc (F-GEO-101) | • Hàng dữ liệu: F-GEO-101 | 1. Mở màn "Vào lớp" và bật bàn phím để gõ | • Hai ô và nút "Vào lớp" nằm trọn chiều ngang màn hình, không phải cuộn ngang<br/>• Nút "Vào lớp" vẫn bấm được khi bàn phím đang mở | US-GEO-101 · SC7; CL-UI-01; PROVISIONAL KG-09 (phiên bản tối thiểu) |
| TC-GEO-0107 | Tên hiển thị rỗng | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API + thủ công trên thiết bị | Chưa vào lớp | • Ô Tên hiển thị rỗng hoặc chỉ có khoảng trắng (F-GEO-027)<br/>• Mã lớp `HK7M2A` | • Hàng dữ liệu: F-GEO-027 | 1. Bấm "Vào lớp" | • Dưới ô Tên hiển thị hiện "Em hãy nhập tên hiển thị." (MSG-GEO-09)<br/>• Vẫn ở màn "Vào lớp"<br/>• Không có học sinh nào được tạo | US-GEO-101 · FC1; CL-FN-02; PROVISIONAL QA-GEO-01 |
| TC-GEO-0108 | Tên hiển thị 61 ký tự | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API + thủ công trên thiết bị | Chưa vào lớp | • Tên gồm 61 ký tự "A" (F-GEO-027) | • Hàng dữ liệu: F-GEO-027<br/>• Giá trị nhập: `HK7M2A` | 1. Bấm "Vào lớp" với mã `HK7M2A` | • Máy chủ trả `VALIDATION_FAILED`<br/>• Hiện "Tên hiển thị tối đa 60 ký tự." (MSG-GEO-10) dưới ô tên<br/>• Không tạo học sinh | US-GEO-101 · FC2; CL-FN-02; PROVISIONAL QA-GEO-01; BVA cận trên không hợp lệ; chạy cả trên màn và gửi thẳng máy chủ |
| TC-GEO-0109 | Mã lớp không tồn tại | High | Abnormal (Fail case/Edge case) | dữ liệu trả về | tích hợp API + thủ công trên thiết bị | Chưa vào lớp | • Mã `ZZ9ZZ9` không thuộc lớp nào (F-GEO-013) | • Hàng dữ liệu: F-GEO-013<br/>• Giá trị nhập: `ZZ9ZZ9`<br/>• Giá trị nhập: "An" | 1. Gõ "An" và `ZZ9ZZ9`, bấm "Vào lớp" | • Hiện "Mã lớp không đúng. Em hỏi lại giáo viên nhé." (MSG-GEO-11) dưới ô Mã lớp<br/>• Ô tên giữ nguyên chữ "An"<br/>• Dữ liệu trả về không chứa tên lớp hay tên trường nào | US-GEO-101 · FC3; CL-FN-02; PROVISIONAL D-GEO-051 |
| TC-GEO-0110 | Phiên hết hạn | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | Học sinh An, thẻ hết hạn | • An đang ở "Bài của em" và thẻ xác thực đã hết hạn (thời hạn thẻ là giả định của ARCH, NFR-GEO-059) | • Không có dữ liệu nhập riêng | 1. An bấm vào một bài | • Máy chủ trả `UNAUTHENTICATED`<br/>• Màn hiện "Phiên đã hết. Vui lòng vào lại." (MSG-GEO-02) và đưa về "Vào lớp"<br/>• Sau khi vào lại bằng "An" và `HK7M2A`, tiến độ cũ còn nguyên | US-GEO-101 · FC4; CL-FN-03; PROVISIONAL QA-GEO-01; ENG |
| TC-GEO-0173 | Gõ sai mã lớp nhiều lần không bị khóa riêng | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API + thủ công trên thiết bị | Chưa vào lớp, iPad | • An ở màn "Vào lớp" trên iPad<br/>• Chưa có lần thử nào trong 15 phút qua | • Hàng dữ liệu: F-GEO-013<br/>• Giá trị nhập: `ZZ9ZZ9`<br/>• Giá trị nhập: `HK7M2A`<br/>• Giá trị nhập: "An" | 1. An gõ "An" với mã `ZZ9ZZ9` (F-GEO-013) 11 lần liên tiếp trong vòng 2 phút,<br/>2. Gõ "An" với `HK7M2A` | • 11 lần sai đều hiện "Mã lớp không đúng. Em hỏi lại giáo viên nhé." (MSG-GEO-11), không lần nào nhận `RATE_LIMITED`<br/>• Lần gõ đúng vào được "Bài của em" (giới hạn 10 lần sai trong 15 phút chỉ áp cho đăng nhập giáo viên) | US-GEO-101 · FC5; CL-FN-02; PROVISIONAL D-GEO-107; cặp đảo của TC-GEO-0171: học sinh giữ D-GEO-107 |

### US-GEO-102 — Giáo viên mẫu vào hệ thống

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0111 | Mã giáo viên đúng | High | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | Giáo viên mẫu, máy tính | • Màn "Giáo viên mẫu" (mở từ liên kết "Tôi là giáo viên"), máy tính (F-GEO-102) | • Hàng dữ liệu: F-GEO-102, F-GEO-003<br/>• Giá trị nhập: `GQ7KX2MTB4HN` | 1. Gõ Mã giáo viên `GQ7KX2MTB4HN` (F-GEO-003), bấm "Vào" | • Màn "Kho đề" mở<br/>• Mục lớp của giáo viên có đúng hai lớp 11A1 và 9B<br/>• Góc màn hiện tên "Giáo viên mẫu" | US-GEO-102 · SC1; CL-FN-01; PROVISIONAL D-GEO-152 |
| TC-GEO-0112 | Vào được từ điện thoại | Medium | Normal (Success case) | thiết bị | thủ công trên thiết bị | Giáo viên mẫu, điện thoại | • Điện thoại, trình duyệt web (F-GEO-103) | • Hàng dữ liệu: F-GEO-103<br/>• Giá trị nhập: `GQ7KX2MTB4HN` | 1. Gõ `GQ7KX2MTB4HN`, bấm "Vào" | • "Kho đề" mở, không phải cuộn ngang để thấy danh sách đề và nút "Giao bài" | US-GEO-102 · SC2; CL-UI-01; PROVISIONAL D-GEO-152 |
| TC-GEO-0113 | Mã giáo viên sai | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API + thủ công trên thiết bị | Chưa vào | • Màn "Giáo viên mẫu" | • Hàng dữ liệu: F-GEO-004<br/>• Giá trị nhập: `AAAAAAAAAAAA` | 1. Gõ `AAAAAAAAAAAA` (F-GEO-004: đúng định dạng nhưng không cấp cho tài khoản nào), bấm "Vào" | • Hiện "Mã giáo viên không đúng." (MSG-GEO-12)<br/>• Vẫn ở màn đó<br/>• Không có phiên giáo viên | US-GEO-102 · FC1; CL-FN-05; PROVISIONAL D-GEO-152; ca dò mã ở TC-GEO-0171, mã thu hồi và quá hạn ở TC-GEO-0175, 0176 (D-GEO-152) |
| TC-GEO-0114 | Học sinh không làm được việc của giáo viên | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | Học sinh An | • An đã vào lớp 11A1 bằng tên và mã lớp<br/>• BG-2 đang Chưa đặt giờ (F-GEO-041) | • Hàng dữ liệu: F-GEO-041 | 1. Từ phiên của An, gửi thẳng tới máy chủ yêu cầu đặt giờ mở của BG-2 thành T0 + 1 phút | • Máy chủ trả `FORBIDDEN` ("Bạn không có quyền thực hiện thao tác này." (MSG-GEO-03))<br/>• BG-2 vẫn Chưa đặt giờ<br/>• Lời giải BG-2 vẫn khóa | US-GEO-102 · FC2; CL-FN-05; PROVISIONAL D-GEO-073; ENG |
| TC-GEO-0115 | Không có công tắc đổi vai | Medium | Abnormal (Fail case/Edge case) | chức năng | thủ công trên thiết bị | Học sinh An | • An đang ở "Bài của em" | • Không có dữ liệu nhập riêng | 1. An xem toàn bộ màn và thanh bên | • Không có công tắc "Giáo viên / Học sinh"<br/>• Đường duy nhất sang vai giáo viên là ra màn "Vào lớp", bấm "Tôi là giáo viên" và nhập mã | US-GEO-102 · FC3; CL-UI-03; PROVISIONAL D-GEO-073 |
| TC-GEO-0171 | Dò mã: lần thử thứ 11 trong 15 phút bị chặn | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | Chưa vào (một máy, một địa chỉ IP) | • Từ một địa chỉ IP, đã có 10 lần gõ `AAAAAAAAAAAA` sai trong 5 phút gần nhất, lần thứ 10 hiện MSG-GEO-12 (F-GEO-009) | • Hàng dữ liệu: F-GEO-009<br/>• Giá trị nhập: `GQ7KX2MTB4HN` | 1. Từ cùng địa chỉ IP, trong 15 phút kể từ lần sai đầu, gõ mã đúng `GQ7KX2MTB4HN` và bấm "Vào" | • Máy chủ trả `RATE_LIMITED` kèm tiêu đề `Retry-After`<br/>• Hiện "Đã nhập sai mã quá nhiều lần. Vui lòng thử lại sau ít phút." (MSG-GEO-59)<br/>• Không có phiên giáo viên | US-GEO-102 · FC6; CL-FN-05; PROVISIONAL D-GEO-173; ENG; công cụ: kiểm thử tích hợp API; BVA 10 / 11 lần (D-GEO-152, RV-GEO-02) |
| TC-GEO-0172 | Máy chủ không lưu mã rõ | High | Normal (Success case) | dữ liệu trả về | tích hợp CSDL | Kỹ thuật (đọc CSDL và nhật ký máy chủ) | • Dữ liệu mẫu đã nạp với mã `GQ7KX2MTB4HN` của Giáo viên mẫu (F-GEO-003) | • Hàng dữ liệu: F-GEO-003 | 1. Rà mọi bảng của cơ sở dữ liệu và nhật ký máy chủ của lần đăng nhập ở SC1 | • Chuỗi `GQ7KX2MTB4HN` không xuất hiện ở bất kỳ cột hay dòng nhật ký nào<br/>• Chỉ có giá trị băm của mã | US-GEO-102 · SC4; CL-FN-07; PROVISIONAL D-GEO-152; ENG; D-GEO-152, RV-GEO-02 |
| TC-GEO-0174 | Mỗi mã vào đúng tài khoản được cấp | High | Normal (Success case) | phân quyền | tích hợp API + thủ công trên thiết bị | Giáo viên C (trường khác) | • Màn "Giáo viên mẫu"<br/>• Mã `RW3NJ5PZC6TD` được cấp cho Giáo viên C của "THCS Mẫu C" (F-GEO-005) | • Hàng dữ liệu: F-GEO-005<br/>• Giá trị nhập: `RW3NJ5PZC6TD` | 1. Gõ `RW3NJ5PZC6TD`, bấm "Vào" | • Vào tài khoản Giáo viên C: mục lớp chỉ có 9C<br/>• Không có 11A1, 9B hay dữ liệu nào của "THPT Mẫu A" | US-GEO-102 · SC3; CL-FN-06; PROVISIONAL D-GEO-152 |
| TC-GEO-0175 | Mã đã thu hồi | Medium | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API + thủ công trên thiết bị | Chưa vào | • Mã `HX4QW7PLZ2KM` từng cấp cho Giáo viên mẫu và đã bị thu hồi (F-GEO-006) | • Hàng dữ liệu: F-GEO-006<br/>• Giá trị nhập: `HX4QW7PLZ2KM` | 1. Gõ `HX4QW7PLZ2KM`, bấm "Vào" | • Hiện "Mã giáo viên không đúng." (MSG-GEO-12) (cùng câu với mã sai)<br/>• Không có phiên giáo viên | US-GEO-102 · FC4; CL-FN-05; PROVISIONAL D-GEO-152 |
| TC-GEO-0176 | Mã quá hạn dùng | Medium | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | Chưa vào | • Mã `T5BN3JQ6WRYE` cấp cho Giáo viên mẫu, hạn dùng đã qua lúc T0 − 1 phút (F-GEO-007) | • Hàng dữ liệu: F-GEO-007<br/>• Giá trị nhập: `T5BN3JQ6WRYE` | 1. Gõ `T5BN3JQ6WRYE` tại T0, bấm "Vào" | • Hiện "Mã giáo viên không đúng." (MSG-GEO-12)<br/>• Không có phiên giáo viên | US-GEO-102 · FC5; CL-FN-05; PROVISIONAL D-GEO-152; ENG; BVA: hạn dùng qua lúc T0 − 1 phút |
| TC-GEO-0177 | Phiên học sinh không dùng được đường lời giải của giáo viên | High | Abnormal (Fail case/Edge case) | dữ liệu trả về | tích hợp API | Học sinh An | • An đã vào lớp 11A1 bằng tên và mã lớp<br/>• BG-1 (HKG-01) đang Sắp mở | • Hàng dữ liệu: F-GEO-037 | 1. Từ phiên của An, gửi thẳng yêu cầu lời giải dành cho giáo viên của HKG-01 | • `FORBIDDEN` ("Bạn không có quyền thực hiện thao tác này." (MSG-GEO-03))<br/>• Dữ liệu trả về không có bước giải F-GEO-037 và không có "45°" | US-GEO-102 · FC7; CL-FN-07; PROVISIONAL D-GEO-152; ENG; ca giả làm giáo viên ở tầng API (PO-18) |

### US-GEO-103 — Máy chủ gọi AI qua một cổng

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0116 | Một lượt gọi, một bản ghi đủ trường | High | Normal (Success case) | chức năng | tích hợp API | Học sinh Dũng | • Nhà cung cấp giả lập với model `gia-lap-1`, phiên bản prompt `p1`, chi phí mô phỏng 0,12 mỗi lượt (F-GEO-096)<br/>• Dũng đang làm BG-1 | • Hàng dữ liệu: F-GEO-096, F-GEO-058<br/>• Giá trị nhập: "Tan của một góc là gì?" | 1. Dũng gửi câu hỏi "Tan của một góc là gì?" (F-GEO-058) ở tab Hỏi đáp | • Có thêm đúng một bản ghi lượt gọi với mục đích hỏi đáp, model `gia-lap-1`, phiên bản prompt `p1`, chi phí 0,12, trạng thái thành công và thời gian xử lý | US-GEO-103 · SC1; CL-FN-14; PROVISIONAL D-GEO-047; ENG; chi phí 0,12 là số mô phỏng (KG-07) |
| TC-GEO-0117 | Đổi model bằng cấu hình | Medium | Normal (Success case) | chức năng | tích hợp API | Học sinh Dũng | • Cấu hình cổng đang là model `gia-lap-1` | • Hàng dữ liệu: F-GEO-058<br/>• Giá trị nhập: `gia-lap-2` | 1. Đổi cấu hình thành `gia-lap-2` (không sửa mã của các module gọi cổng)<br/>2. Dũng gửi lại câu hỏi F-GEO-058 | • Bản ghi lượt gọi mới ghi model `gia-lap-2`<br/>• Các bản ghi cũ vẫn ghi `gia-lap-1` | US-GEO-103 · SC2; CL-FN-14; PROVISIONAL D-GEO-047; ENG |
| TC-GEO-0118 | Khóa của nhà cung cấp không xuống trình duyệt | High | Normal (Success case) | dữ liệu trả về | tích hợp API | Học sinh An | • Cổng được cấu hình một khóa thử có giá trị `KHOA-THU-KHONG-LO` | • Không có dữ liệu nhập riêng | 1. An thực hiện một lượt hỏi đáp và một lượt dựng hình tự học<br/>2. Ghi lại toàn bộ dữ liệu trả về và mã trang tải về thiết bị | • Chuỗi `KHOA-THU-KHONG-LO` không xuất hiện ở bất kỳ đâu trong dữ liệu trả về hay mã trang | US-GEO-103 · SC3; CL-FN-07; ENG |
| TC-GEO-0119 | Không gửi định danh học sinh cho nhà cung cấp | High | Normal (Success case) | dữ liệu trả về | tích hợp API | Học sinh An | • An (lớp 11A1, mã lớp `HK7M2A`) gửi câu hỏi F-GEO-055 trên BG-1 | • Hàng dữ liệu: F-GEO-055 | 1. Xem nội dung yêu cầu mà nhà cung cấp giả lập nhận được | • Nội dung không chứa "An", `HK7M2A`, "11A1", "THPT Mẫu A" hay định danh người dùng | US-GEO-103 · SC4; CL-X-04; PROVISIONAL D-GEO-067; ENG |
| TC-GEO-0120 | Nhà cung cấp trả lỗi | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API + thủ công trên thiết bị | Học sinh An | • Nhà cung cấp giả lập được đặt ở chế độ trả lỗi | • Hàng dữ liệu: F-GEO-055 | 1. An gửi câu hỏi F-GEO-055 | • Khung hỏi đáp hiện "Trợ lý đang bận. Em thử lại sau ít phút nhé." (MSG-GEO-14) và nút "Thử lại"<br/>• Không có câu trả lời dở dang<br/>• Nhãn máy của bài vẫn "Đã kiểm chứng"<br/>• Có một bản ghi lượt gọi trạng thái lỗi | US-GEO-103 · FC1; CL-FN-14; PROVISIONAL D-GEO-072 |
| TC-GEO-0121 | Nhà cung cấp quá thời gian | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | Học sinh An | • Nhà cung cấp giả lập không trả lời<br/>• Thời gian chờ tối đa của cổng là giá trị cấu hình, chưa có số trong nguồn (D-GEO-072, QA-GEO-20) | • Hàng dữ liệu: F-GEO-055 | 1. An gửi câu hỏi F-GEO-055 và thời gian chờ cấu hình trôi qua | • Hiện "Trợ lý đang bận. Em thử lại sau ít phút nhé." (MSG-GEO-14) và "Thử lại"<br/>• Bản ghi lượt gọi có trạng thái quá thời gian<br/>• Màn chờ không quay vô hạn | US-GEO-103 · FC2; CL-FN-14; PROVISIONAL D-GEO-072; ENG; thời gian chờ là giá trị cấu hình của lần chạy (chưa có nguồn) |
| TC-GEO-0122 | Bấm "Thử lại" sau lỗi | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API + thủ công trên thiết bị | Học sinh An | • Màn đang hiện MSG-GEO-14 sau FC1<br/>• Nhà cung cấp giả lập đã trở lại bình thường | • Không có dữ liệu nhập riêng | 1. An bấm "Thử lại" | • Câu hỏi cũ được gửi lại (An không phải gõ lại), câu trả lời hiện ra<br/>• Có thêm một bản ghi lượt gọi thành công, bản ghi lỗi trước đó vẫn còn | US-GEO-103 · FC3; CL-FN-14; PROVISIONAL D-GEO-072 |

### US-GEO-104 — Cách ly dữ liệu theo trường và phân quyền ở máy chủ (xuyên suốt)

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0123 | Học sinh chỉ nhận bài của lớp mình | High | Normal (Success case) | phân quyền | tích hợp API | Học sinh An | • An thuộc 11A1<br/>• Có BG-1…BG-3 (11A1), BG-4, BG-5 (9B), BG-C (9C trường khác) (F-GEO-040…045) | • Hàng dữ liệu: F-GEO-040…045 | 1. An mở "Bài của em" | • Dữ liệu trả về chứa đúng BG-1, BG-2, BG-3<br/>• Không chứa BG-4, BG-5, BG-C dưới bất kỳ dạng nào | US-GEO-104 · SC1; CL-FN-05; ENG |
| TC-GEO-0124 | Giáo viên chỉ thấy lớp mình dạy | High | Normal (Success case) | phân quyền | tích hợp API + thủ công trên thiết bị | Giáo viên mẫu | • Giáo viên mẫu dạy 11A1 và 9B<br/>• Lớp 9C thuộc Giáo viên C (F-GEO-003, F-GEO-005) | • Hàng dữ liệu: F-GEO-003, F-GEO-005 | 1. Giáo viên mẫu mở danh sách lớp ở bước "1 · Chọn lớp" của màn giao bài | • Chỉ có "11A1" và "9B"<br/>• Không có "9C" | US-GEO-104 · SC2; CL-FN-05 |
| TC-GEO-0125 | Nội dung hệ thống đọc được từ mọi trường | Medium | Normal (Success case) | phân quyền | tích hợp API | Giáo viên C (trường khác) | • Giáo viên C của "THCS Mẫu C" đã vào bằng mã `RW3NJ5PZC6TD` (F-GEO-005) | • Hàng dữ liệu: F-GEO-005 | 1. Mở "Kho đề" | • Thấy cùng các đề HKG-01…HKG-06 với cùng nội dung như Giáo viên mẫu thấy<br/>• Không thấy bài giao, lớp hay học sinh nào của "THPT Mẫu A" | US-GEO-104 · SC3; CL-FN-06; PROVISIONAL D-GEO-076; ENG |
| TC-GEO-0126 | Bản ghi mới mang trường của người tạo | High | Normal (Success case) | phân quyền | tích hợp API | Học sinh An | • An (trường "THPT Mẫu A") đang làm BG-1 | • Hàng dữ liệu: F-GEO-055 | 1. An gửi câu hỏi F-GEO-055, trong thân yêu cầu cố ý kèm định danh của trường "THCS Mẫu C" | • Câu hỏi được lưu thuộc "THPT Mẫu A"<br/>• Giáo viên mẫu thấy câu hỏi ở Theo dõi lớp 11A1<br/>• Giáo viên C không thấy | US-GEO-104 · SC4; CL-FN-06; ENG |
| TC-GEO-0127 | Bản ghi có chủ sở hữu, phiên bản lược đồ, nguồn gốc lời giải | Medium | Normal (Success case) | dữ liệu trả về | tích hợp CSDL | Kỹ thuật (đọc CSDL) | • Dữ liệu mẫu đã nạp (US-GEO-601) và kho đề đã nạp (US-GEO-504) | • Hàng dữ liệu: F-GEO-003, F-GEO-008 | 1. Kiểm bản ghi của BG-1, của một sự kiện của An, của dữ liệu hình và lời giải HKG-01 | • BG-1 có trường "THPT Mẫu A" và chủ sở hữu là tài khoản Giáo viên mẫu (F-GEO-003)<br/>• Sự kiện của An có trường "THPT Mẫu A"<br/>• Lời giải HKG-01 có model, phiên bản prompt và tác giả là tác giả nền tảng (F-GEO-008), không phải tài khoản Giáo viên mẫu của một trường<br/>• Dữ liệu hình và lời giải HKG-01 mang số phiên bản lược đồ | US-GEO-104 · SC5; CL-FN-01; PROVISIONAL D-GEO-160; ENG |
| TC-GEO-0128 | Học sinh gọi bài của lớp khác cùng trường | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API + thủ công trên thiết bị | Học sinh An | • An (11A1)<br/>• BG-4 thuộc lớp 9B cùng trường | • Không có dữ liệu nhập riêng | 1. An thay mã bài trên địa chỉ bằng mã của BG-4 và tải trang | • Máy chủ trả `NOT_FOUND`<br/>• Màn hiện "Không tìm thấy nội dung này." (MSG-GEO-04)<br/>• Dữ liệu trả về không chứa đề, hình, gợi ý hay giờ mở của BG-4 | US-GEO-104 · FC1; CL-FN-05; PROVISIONAL QA-GEO-05 |
| TC-GEO-0129 | Học sinh gọi bài của trường khác | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | Học sinh An | • An (trường "THPT Mẫu A")<br/>• BG-C thuộc "THCS Mẫu C" | • Không có dữ liệu nhập riêng | 1. An gọi mã của BG-C | • `NOT_FOUND` và "Không tìm thấy nội dung này." (MSG-GEO-04)<br/>• Phản hồi giống hệt (cùng `code`, cùng `message`) phản hồi khi gọi một mã bài không tồn tại | US-GEO-104 · FC2; CL-FN-06; ENG |
| TC-GEO-0130 | Học sinh gọi dữ liệu của bạn cùng lớp | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | Học sinh An | • An và Bình cùng lớp 11A1<br/>• Bình có 2 câu hỏi trên BG-1 (F-GEO-051) | • Hàng dữ liệu: F-GEO-051 | 1. An gọi nhật ký hỏi đáp và tiến độ của Bình bằng định danh của Bình | • `NOT_FOUND`<br/>• Không có câu hỏi hay tiến độ nào của Bình trong dữ liệu trả về | US-GEO-104 · FC3; CL-FN-05; ENG |
| TC-GEO-0131 | Giáo viên trường khác đổi giờ mở | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | Giáo viên C (trường khác) | • Giáo viên C<br/>• BG-1 đang Sắp mở với `release_at` = T0 + 60 phút | • Không có dữ liệu nhập riêng | 1. Giáo viên C gửi yêu cầu "Mở ngay" cho BG-1 bằng mã của BG-1 | • `NOT_FOUND`<br/>• BG-1 vẫn Sắp mở với `release_at` = T0 + 60 phút<br/>• Không có dòng nhật ký đổi giờ nào được ghi | US-GEO-104 · FC4; CL-FN-06; ENG |
| TC-GEO-0132 | Giáo viên trường khác xem Theo dõi lớp | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | Giáo viên C (trường khác) | • Giáo viên C | • Không có dữ liệu nhập riêng | 1. Gọi trang Theo dõi lớp của BG-1 (lớp 11A1) | • `NOT_FOUND`<br/>• Dữ liệu trả về không có tên An, Bình, Chi, Dũng, Hà | US-GEO-104 · FC5; CL-FN-06; ENG |
| TC-GEO-0133 | Giao bài chéo trường | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | Giáo viên mẫu | • Giáo viên mẫu (trường "THPT Mẫu A") | • Không có dữ liệu nhập riêng | 1. Gửi yêu cầu giao HKG-01 với lớp nhận là lớp 9C của "THCS Mẫu C" | • `NOT_FOUND`<br/>• Không bài giao nào được tạo<br/>• Học sinh 9C không thấy bài mới | US-GEO-104 · FC6; CL-FN-06; ENG |
| TC-GEO-0134 | Thẻ xác thực bị sửa | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | Thẻ đã sửa | • Thẻ của An bị sửa phần trường thành "THCS Mẫu C" | • Không có dữ liệu nhập riêng | 1. Gọi "Bài của em" bằng thẻ đã sửa | • `UNAUTHENTICATED` ("Phiên đã hết. Vui lòng vào lại." (MSG-GEO-02))<br/>• Không có dữ liệu của trường nào được trả | US-GEO-104 · FC7; CL-FN-06; ENG |
| TC-GEO-0170 | Bộ kiểm cách ly duyệt mọi điểm cuối bằng thẻ của trường khác | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | Học sinh An của lớp 9C và Giáo viên C (trường "THCS Mẫu C") | • Dữ liệu mẫu hai trường đã nạp<br/>• Có danh mục mọi điểm cuối danh sách, chi tiết, ghi | • Hàng dữ liệu: F-GEO-001, F-GEO-002, F-GEO-005, F-GEO-026, F-GEO-040…045<br/>• Id của BG-1, BG-2, BG-3, lớp 11A1, học sinh An (11A1) | 1. Với từng điểm cuối trong danh mục, gọi bằng thẻ của trường "THCS Mẫu C" và id của trường "THPT Mẫu A"<br/>2. Ghi lại `code` và toàn bộ thân phản hồi | • Mọi điểm cuối chi tiết và ghi trả `NOT_FOUND`<br/>• Mọi điểm cuối danh sách trả danh sách không có bản ghi nào của "THPT Mẫu A"<br/>• Không thân phản hồi nào chứa "11A1", "Bình", "Chi", "Dũng", "Hà" hay đề của BG-1…BG-3<br/>• Số điểm cuối rò rỉ = 0 trên 100% điểm cuối | US-GEO-104 · FC2; US-GEO-104 · FC5; NFR-GEO-030; CL-FN-06; ENG |
| TC-GEO-0178 | Giáo viên không đọc được câu trả lời đã lưu của học sinh | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | Giáo viên mẫu | • An đã hỏi F-GEO-055 trên BG-1 và có câu trả lời đã lưu<br/>• Giáo viên mẫu dạy 11A1 | • Hàng dữ liệu: F-GEO-055 | 1. Từ phiên của Giáo viên mẫu, gửi thẳng yêu cầu đọc câu trả lời đã lưu của câu hỏi đó | • `FORBIDDEN` ("Bạn không có quyền thực hiện thao tác này." (MSG-GEO-03))<br/>• Dữ liệu trả về không có câu trả lời<br/>• Câu hỏi vẫn hiện ở Theo dõi lớp của BG-1 | US-GEO-104 · FC9; CL-FN-05; PROVISIONAL D-GEO-159; ENG; D-GEO-159 |
| TC-GEO-0800 | Con trỏ phân trang của trường này dùng với thẻ của trường khác | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | Giáo viên C (trường "THCS Mẫu C") | • Giáo viên mẫu đã tải trang 1 của một danh sách thuộc trường (Theo dõi lớp BG-1) và nhận con trỏ trang kế | • Hàng dữ liệu: F-GEO-203, F-GEO-005<br/>• Con trỏ phát cho thẻ của "THPT Mẫu A" | 1. Bằng thẻ của Giáo viên C, gọi cùng danh sách với con trỏ F-GEO-203<br/>2. Ghi lại `code` và thân phản hồi | • Thân phản hồi không chứa bản ghi nào của "THPT Mẫu A"<br/>• Máy chủ trả `INVALID_CURSOR`<br/>• Không trả trang rỗng giả | US-GEO-104 · FC8; NFR-GEO-030; BR-GEO-19; CL-FN-08; M-402 (c); con trỏ mờ gắn trường và loại danh sách (D-GEO-168, RV-GEO-37); ENG |

### US-GEO-105 — Chạy bộ đề chuẩn và đo tỉ lệ đúng

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0135 | Báo cáo của một lần chạy trên sáu đề | High | Normal (Success case) | chức năng | tích hợp API | Kỹ thuật (chạy bộ đề chuẩn) | • Bộ đề chuẩn gồm sáu đề F-GEO-031…036 với đáp số như dữ liệu vàng<br/>• Model `gia-lap-1`, phiên bản prompt `p1` | • Hàng dữ liệu: F-GEO-031…036 | 1. Chạy bộ đề chuẩn | • Báo cáo ghi N = 6<br/>• Hình thỏa ràng buộc 6/6 = 100,0%<br/>• Đáp số đúng 6/6 = 100,0%<br/>• Bài sai mang "Đã kiểm chứng" = 0<br/>• Số đề đã kiểm chứng = 6<br/>• Model `gia-lap-1`<br/>• Phiên bản prompt `p1`<br/>• Phiên bản bộ đề | US-GEO-105 · SC1; CL-FN-01; PROVISIONAL D-GEO-040; ENG; nhãn kỳ vọng của HKG-06 theo phép tính, khác nhãn trong tệp nguồn (QA-GEO-03) |
| TC-GEO-0136 | Một đề có đáp số sai làm tỉ lệ giảm đúng công thức | High | Normal (Success case) | chức năng | tích hợp API | Kỹ thuật | • Như SC1 nhưng đáp số AI của HKG-01 là "60°" (F-GEO-070) | • Hàng dữ liệu: F-GEO-070 | 1. Chạy bộ đề chuẩn | • Đáp số đúng 5/6 = 83,3%<br/>• Hình thỏa ràng buộc 100,0%<br/>• HKG-01 mang "Cần xem lại"<br/>• Bài sai mang "Đã kiểm chứng" = 0<br/>• Số đề đã kiểm chứng = 5 | US-GEO-105 · SC2; CL-FN-11; PROVISIONAL D-GEO-040; ENG |
| TC-GEO-0137 | Chạy lại khi đổi model giữ cả hai báo cáo | Medium | Normal (Success case) | chức năng | tích hợp API | Kỹ thuật | • Đã có báo cáo của SC1 với model `gia-lap-1` | • Giá trị nhập: `gia-lap-2` | 1. Đổi model sang `gia-lap-2`<br/>2. Chạy lại | • Có báo cáo thứ hai ghi model `gia-lap-2`<br/>• Báo cáo thứ nhất còn nguyên và vẫn ghi `gia-lap-1` | US-GEO-105 · SC3; CL-FN-01; ENG |
| TC-GEO-0138 | So với ngưỡng đề xuất | Medium | Normal (Success case) | chức năng | đơn vị | Kỹ thuật | • Một bộ 40 đề trong đó 38 đề hình thỏa ràng buộc và 36 đề đáp số đúng | • Không có dữ liệu nhập riêng | 1. Chạy bộ đề chuẩn | • Báo cáo ghi 95,0% "đạt (ngưỡng đề xuất ≥ 95,0%)" và 90,0% "đạt (ngưỡng đề xuất ≥ 90,0%)"<br/>• Với 37 và 35 đề thì ghi 92,5% và 87,5% "chưa đạt (ngưỡng đề xuất)" | US-GEO-105 · SC4; CL-FN-12; PROVISIONAL D-GEO-045; ENG; BVA quanh ngưỡng đề xuất |
| TC-GEO-0139 | Ghi chú AI giả lập | Low | Normal (Success case) | chức năng | tích hợp API | Kỹ thuật | • Nhà cung cấp là giả lập | • Không có dữ liệu nhập riêng | 1. Chạy bộ đề chuẩn | • Đầu báo cáo có dòng "Nhà cung cấp AI giả lập: số đo chưa phản ánh độ đúng của AI thật." | US-GEO-105 · SC5; CL-FN-12; PROVISIONAL D-GEO-047; ENG |
| TC-GEO-0140 | Bộ đề trống | Medium | Abnormal (Fail case/Edge case) | chức năng | đơn vị | Kỹ thuật | • Bộ đề chuẩn không có đề nào | • Không có dữ liệu nhập riêng | 1. Chạy bộ đề chuẩn | • Báo cáo ghi N = 0, hai tỉ lệ ghi "—", bài sai mang "Đã kiểm chứng" = 0<br/>• Lần chạy kết thúc không lỗi chia cho 0 | US-GEO-105 · FC1; CL-FN-12; ENG |
| TC-GEO-0141 | Một đề thiếu đáp số tham chiếu | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | Kỹ thuật | • Bộ sáu đề trong đó HKG-05 không có đáp số tham chiếu | • Không có dữ liệu nhập riêng | 1. Chạy bộ đề chuẩn | • HKG-05 vẫn tính vào N = 6, không tính vào số đáp số đúng (5/6 = 83,3%), mang "Cần xem lại" và được liệt kê ở mục "Thiếu đáp số tham chiếu: HKG-05" | US-GEO-105 · FC2; CL-FN-03; ENG |
| TC-GEO-0142 | Không bản ghi nào mang "Đã kiểm chứng" khi hình hoặc đáp số chưa khớp | High | Abnormal (Fail case/Edge case) | dữ liệu trả về | tích hợp CSDL | Kỹ thuật (đọc và ghi CSDL) | • Sau lần chạy của SC2 | • Không có dữ liệu nhập riêng | 1. Đếm các đề mang "Đã kiểm chứng" mà hình không thỏa ràng buộc hoặc đáp số không khớp | • Kết quả bằng 0<br/>• Cố ghi nhãn "Đã kiểm chứng" cho HKG-01 (đang không khớp) bị từ chối ở tầng dữ liệu | US-GEO-105 · FC3; CL-FN-11; ENG |

### US-GEO-106 — Dữ liệu: đề chuẩn và đáp số tham chiếu

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0143 | Sáu đề có đáp số tham chiếu đúng nguồn | High | Normal (Success case) | chức năng | tích hợp CSDL | Kỹ thuật | • Tệp `materials/Ngan-hang-bai-tap-Hinh-khong-gian.xlsx` | • Hàng dữ liệu: F-GEO-031…036 | 1. Nạp bộ đề chuẩn | • Có sáu đề HKG-01…HKG-06 với đề bài và đáp số đúng từng ký tự như F-GEO-031…036 (ví dụ HKG-03: "24√3 cm³") | US-GEO-106 · SC1; CL-FN-01; ENG |
| TC-GEO-0144 | Đáp số nhiều giá trị lưu thành từng đại lượng | High | Normal (Success case) | chức năng | tích hợp CSDL | Kỹ thuật | • HKG-04 có đáp số "Sxq = 15π cm²; V = 12π cm³" (F-GEO-034) | • Hàng dữ liệu: F-GEO-034 | 1. Nạp | • Đề có hai đại lượng tham chiếu riêng: diện tích xung quanh 15π (≈ 47,1239) và thể tích 12π (≈ 37,6991) | US-GEO-106 · SC2; CL-FN-01; ENG |
| TC-GEO-0145 | Đáp số theo tham số có ba bộ giá trị kiểm | Medium | Normal (Success case) | chức năng | tích hợp CSDL | Kỹ thuật | • HKG-02 có đáp số "a" (F-GEO-032) | • Hàng dữ liệu: F-GEO-032 | 1. Nạp | • Đề có ba bộ giá trị tham số a = 1, 2, 3 với giá trị tham chiếu 1, 2, 3 | US-GEO-106 · SC3; CL-FN-01; PROVISIONAL D-GEO-040; ENG |
| TC-GEO-0146 | Một đề, một bản ghi cho cả kho đề và bộ đề chuẩn | Medium | Normal (Success case) | chức năng | tích hợp CSDL | Kỹ thuật | • Đã nạp kho đề (US-GEO-504) và bộ đề chuẩn | • Không có dữ liệu nhập riêng | 1. So đề HKG-01 hiện ở "Kho đề" với đề HKG-01 trong báo cáo bộ đề chuẩn | • Cùng một bản ghi và cùng phiên bản<br/>• Toàn hệ thống chỉ có một đề mang mã HKG-01 | US-GEO-106 · SC4; CL-FN-01; ENG |
| TC-GEO-0147 | Đề thiếu đáp số không vào bộ đề chuẩn một cách im lặng | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp CSDL | Kỹ thuật | • Một tệp nạp trong đó HKG-05 để trống ô Đáp số | • Không có dữ liệu nhập riêng | 1. Nạp | • Kết quả nạp liệt kê "HKG-05: thiếu đáp số tham chiếu"<br/>• Năm đề còn lại nạp bình thường | US-GEO-106 · FC1; CL-FN-03; ENG |
| TC-GEO-0148 | Nhãn trong tệp nguồn không thành nhãn máy | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp CSDL | Kỹ thuật | • Tệp nguồn ghi "Đã kiểm chứng" cho HKG-01 và "Cần xem lại" cho HKG-06 | • Không có dữ liệu nhập riêng | 1. Nạp xong và engine chưa chạy kiểm chứng | • Không đề nào mang "Đã kiểm chứng"<br/>• Nhãn chỉ xuất hiện sau khi engine tính (US-GEO-203, 504) | US-GEO-106 · FC2; CL-FN-11; ENG |

### US-GEO-107 — Thử tải 1000 người dùng đồng thời

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0149 | p95 đọc dưới tải đều | High | Normal (Success case) | phi chức năng | tải | Kỹ thuật (thử tải) | • Dữ liệu thử tải F-GEO-097<br/>• 1000 phiên đang hoạt động trong 10 phút liên tục, 200 đến 333,3 yêu cầu/giây | • Hàng dữ liệu: F-GEO-097 | 1. Đo p95 thời gian phản hồi của yêu cầu đọc | • Báo cáo ghi giá trị đo và "ngưỡng đề xuất ≤ 300 ms"<br/>• Đạt khi giá trị đo ≤ 300 ms | US-GEO-107 · SC1; CL-X-01; PROVISIONAL D-GEO-045; ENG; kịch bản L1 của `review/test_plan.md` §13; công cụ: Gatling (D-GEO-170) |
| TC-GEO-0150 | p95 ghi dưới tải đều | High | Normal (Success case) | phi chức năng | tải | Kỹ thuật (thử tải) | • Như SC1 | • Không có dữ liệu nhập riêng | 1. Đo p95 của yêu cầu ghi (lô sự kiện, xem gợi ý, hỏi đáp) | • Báo cáo ghi giá trị đo và "ngưỡng đề xuất ≤ 500 ms"<br/>• Đạt khi ≤ 500 ms | US-GEO-107 · SC2; CL-X-01; PROVISIONAL D-GEO-045; ENG; kịch bản L1; công cụ: Gatling (D-GEO-170) |
| TC-GEO-0151 | Tỉ lệ lỗi dưới tải đều | High | Normal (Success case) | phi chức năng | tải | Kỹ thuật (thử tải) | • Như SC1 | • Không có dữ liệu nhập riêng | 1. Tính lỗi phía máy chủ và quá thời gian trên tổng yêu cầu | • Báo cáo ghi tỉ lệ với hai chữ số thập phân và "ngưỡng đề xuất < 0,50%"<br/>• Đạt khi nhỏ hơn 0,50% | US-GEO-107 · SC3; CL-X-01; PROVISIONAL D-GEO-045; ENG; kịch bản L1; công cụ: Gatling (D-GEO-170) |
| TC-GEO-0152 | Đỉnh 1000 học sinh mở lời giải trong 10 giây | High | Normal (Success case) | phi chức năng | tải | Kỹ thuật (thử tải) | • 1000 học sinh thử tải có bài giao cùng `release_at` = R<br/>• Tải đều của SC1 đang chạy | • Không có dữ liệu nhập riêng | 1. Từ R tới R + 10 giây, 1000 học sinh đó yêu cầu lời giải (100 yêu cầu/giây) | • Cả 1000 em nhận được lời giải<br/>• P95 của yêu cầu xem lời giải ≤ 500 ms và lỗi < 0,50% (ngưỡng đề xuất) | US-GEO-107 · SC4; CL-X-01; PROVISIONAL D-GEO-044; ENG; kịch bản L2; công cụ: Gatling (D-GEO-170) |
| TC-GEO-0153 | Không lộ lời giải dưới tải | High | Normal (Success case) | dữ liệu trả về | tải | Kỹ thuật (thử tải) | • Lần chạy của SC4<br/>• Các yêu cầu lời giải gửi trước R cũng được ghi lại | • Không có dữ liệu nhập riêng | 1. Rà toàn bộ dữ liệu trả về của các yêu cầu đến trước R theo giờ máy chủ | • 0 phản hồi chứa bước giải hoặc đáp số | US-GEO-107 · SC5; CL-FN-07; ENG; kịch bản L2; số lộ = 0 là quy tắc, không phải ngưỡng đề xuất; công cụ: Gatling (D-GEO-170) |
| TC-GEO-0154 | Vượt ngưỡng đề xuất | Medium | Abnormal (Fail case/Edge case) | phi chức năng | tải | Kỹ thuật (thử tải) | • Một lần chạy có p95 đọc đo được 340 ms | • Không có dữ liệu nhập riêng | 1. Sinh báo cáo | • Dòng p95 đọc ghi "340 ms · chưa đạt (ngưỡng đề xuất ≤ 300 ms)"<br/>• Báo cáo không đổi ngưỡng và không ẩn dòng này | US-GEO-107 · FC1; CL-X-01; PROVISIONAL D-GEO-045; ENG; công cụ: Gatling (D-GEO-170) |
| TC-GEO-0155 | Quá tải thì từ chối nhanh, không treo | Medium | Abnormal (Fail case/Edge case) | phi chức năng | tải | Kỹ thuật (thử tải) | • Tải gấp đôi tải thiết kế | • Không có dữ liệu nhập riêng | 1. Một yêu cầu không được phục vụ | • Máy chủ trả `SERVICE_OVERLOADED` (HTTP 503) trong tối đa 2 giây, kèm tiêu đề `Retry-After` từ 1 đến 3 giây (NFR-GEO-055)<br/>• Người dùng thấy "Hệ thống đang đông. Vui lòng thử lại sau vài giây." (MSG-GEO-57)<br/>• Không yêu cầu nào treo vô hạn | US-GEO-107 · FC2; CL-X-01; PROVISIONAL D-GEO-125; ENG; kịch bản L4; công cụ: Gatling (D-GEO-170); mã lỗi `SERVICE_OVERLOADED` theo D-GEO-143 |

### US-GEO-108 — Dữ liệu: bộ dữ liệu thử tải

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0156 | Sinh đủ quy mô bằng một lệnh | High | Normal (Success case) | phi chức năng | tích hợp CSDL | Kỹ thuật | • Cơ sở dữ liệu thử trống | • Hàng dữ liệu: F-GEO-097 | 1. Chạy lệnh sinh dữ liệu thử tải | • Có 50 trường, 600 lớp, 20.000 học sinh và từ 1.900.000 đến 2.100.000 sự kiện (F-GEO-097) | US-GEO-108 · SC1; CL-X-01; PROVISIONAL D-GEO-049; ENG |
| TC-GEO-0157 | Phân bố theo công thức | Medium | Normal (Success case) | phi chức năng | tích hợp CSDL | Kỹ thuật | • Dữ liệu của SC1 | • Không có dữ liệu nhập riêng | 1. Tính trung bình | • 12 lớp mỗi trường, 400 học sinh mỗi trường, trung bình ≈ 33,3 học sinh mỗi lớp, trung bình 100 sự kiện mỗi học sinh (trong sai lệch ±5%) | US-GEO-108 · SC2; CL-X-01; PROVISIONAL D-GEO-049; ENG |
| TC-GEO-0158 | Chạy lại cho cùng quy mô | Medium | Normal (Success case) | phi chức năng | tích hợp CSDL | Kỹ thuật | • Đã sinh dữ liệu một lần | • Không có dữ liệu nhập riêng | 1. Xóa và chạy lại lệnh | • Số trường, lớp, học sinh giống hệt lần trước<br/>• Số sự kiện vẫn trong khoảng của SC1 | US-GEO-108 · SC3; CL-X-01; ENG |
| TC-GEO-0159 | Không lẫn vào dữ liệu mẫu | High | Abnormal (Fail case/Edge case) | phi chức năng | tích hợp CSDL | Kỹ thuật | • Môi trường có dữ liệu mẫu (trường "THPT Mẫu A", lớp 11A1, 9B, 10 học sinh) | • Không có dữ liệu nhập riêng | 1. Chạy lệnh sinh dữ liệu thử tải ở chế độ mặc định | • Dữ liệu thử tải chỉ được ghi vào cơ sở dữ liệu thử riêng<br/>• Trong dữ liệu mẫu, "THPT Mẫu A" vẫn đúng 2 lớp và 10 học sinh, không có thêm trường nào | US-GEO-108 · FC1; CL-X-01; ENG |
| TC-GEO-0160 | Tên sinh ra không phải tên thật | Medium | Abnormal (Fail case/Edge case) | phi chức năng | tích hợp CSDL | Kỹ thuật | • Dữ liệu của SC1 | • Không có dữ liệu nhập riêng | 1. Xem tên trường và tên hiển thị của học sinh | • Tên có dạng đánh số như "Trường thử 01", "Học sinh 00001"<br/>• Không có tên riêng của người hay tổ chức | US-GEO-108 · FC2; CL-X-04; ENG |

### X — Case xuyên suốt và phi chức năng (trỏ về NFR-GEO, BR-GEO hoặc REQ-GEO)

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0801 | Bộ quét SQL bắt các mẫu cấm | High | Abnormal (Fail case/Edge case) | phi chức năng | đơn vị | Kỹ thuật (chạy bộ quét của `app/server/quality/`) | • Bảng thật đã khai báo trong `sql-lint.properties` (bảng lớn, bảng thuộc trường) | • Hàng dữ liệu: F-GEO-204<br/>• Bốn câu SQL mẫu: có `OFFSET`; có `SELECT *`; truy vấn bảng thuộc trường thiếu lọc mã trường; `SELECT` bảng lớn thiếu `LIMIT` | 1. Thêm lần lượt từng câu mẫu vào một tệp thử<br/>2. Chạy bộ quét SQL<br/>3. Gỡ câu mẫu, chạy lại | • Mỗi câu mẫu làm bộ quét báo đúng một lỗi và bản dựng không qua<br/>• Sau khi gỡ, bộ quét báo 0 lỗi | NFR-GEO-031; NFR-GEO-040; REQ-GEO-39; CL-X-03; D-GEO-125; công cụ: bộ quét `SqlLint` của khung mã chạy trong `app/scripts/verify.sh`; ENG; X |
| TC-GEO-0802 | Truy vấn nóng không quét tuần tự bảng lớn | High | Normal (Success case) | phi chức năng | tích hợp CSDL | Kỹ thuật (`psql` tới cổng 54329) | • Dữ liệu thử tải đã sinh: 50 trường, 600 lớp, 20.000 học sinh, 2.000.000 sự kiện<br/>• Có danh sách truy vấn nóng của `architecture/04_query_catalogue.md` | • Hàng dữ liệu: F-GEO-097 | 1. Chạy `EXPLAIN (ANALYZE, BUFFERS)` cho từng truy vấn nóng<br/>2. Tìm nút quét tuần tự trong từng kế hoạch | • 0 truy vấn nóng có quét tuần tự trên bảng hơn 10.000 hàng<br/>• Không kế hoạch nào lọc mã trường sau khi quét (mã trường nằm trong điều kiện chỉ mục) | NFR-GEO-042; NFR-GEO-031; REQ-GEO-39; CL-X-02; D-GEO-125; công cụ: `psql` và `work/architecture/bench/lint_queries.py`; ENG; X |
| TC-GEO-0803 | Redis ngừng giữa lúc tải đều: không lộ lời giải, không lẫn trường | Medium | Abnormal (Fail case/Edge case) | phi chức năng | tải | Kỹ thuật (thử tải) | • Kịch bản L1 đang chạy (1000 phiên đang hoạt động)<br/>• Một số bài giao đang khóa | • Hàng dữ liệu: F-GEO-097 | 1. Tắt Redis cổng 6389 trong lúc L1 đang chạy<br/>2. Tiếp tục tải 2 phút<br/>3. Rà toàn bộ thân phản hồi của học sinh | • 0 phản hồi của bài đang khóa chứa bước giải hoặc đáp số<br/>• 0 phản hồi chứa bản ghi của trường khác với thẻ<br/>• Tỉ lệ lỗi < 0,50% (ngưỡng đề xuất, D-GEO-125) | NFR-GEO-057; BR-GEO-01; BR-GEO-19; CL-X-01; PROVISIONAL D-GEO-045, D-GEO-125; công cụ: Gatling (D-GEO-170), kịch bản L5; ENG; X |
| TC-GEO-0804 | Khóa bộ nhớ đệm của trường này không đọc được bằng thẻ trường khác | Medium | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | Giáo viên mẫu rồi Giáo viên C | • Redis cổng 6389 đang chạy và rỗng | • Hàng dữ liệu: F-GEO-003, F-GEO-005 | 1. Bằng thẻ Giáo viên mẫu, mở Theo dõi lớp BG-1 để tạo dữ liệu đệm<br/>2. Liệt kê mọi khóa trong Redis<br/>3. Bằng thẻ Giáo viên C, gọi cùng địa chỉ | • Mọi khóa chứa dữ liệu thuộc trường đều mang tiền tố trường<br/>• Bước 3 trả `NOT_FOUND`, không trả dữ liệu đệm của "THPT Mẫu A" | NFR-GEO-034; BR-GEO-19; CL-FN-06; D-GEO-125; công cụ: kiểm thử tích hợp API của khung mã và `redis-cli` tới cổng 6389; ENG; X |

## 3. Đối chiếu hai chiều AC ↔ TC

### 3.1 Chiều xuôi: tiêu chí → case (70 / 70 tiêu chí còn hiệu lực có case, 100.0%)

| User story | Criterion ID | Kind | What it checks | Status | Test cases |
|---|---|---|---|---|---|
| US-GEO-101 | US-GEO-101-SC1 | Success | Vào lớp bằng tên và mã lớp đúng | Active · PROVISIONAL | TC-GEO-0100 |
| US-GEO-101 | US-GEO-101-SC2 | Success | Tên chưa có trong lớp thì thành học sinh mới của lớp đó | Active · PROVISIONAL | TC-GEO-0101 |
| US-GEO-101 | US-GEO-101-SC3 | Success | Vào lại với cùng tên thì giữ đúng dữ liệu của mình | Active · PROVISIONAL | TC-GEO-0102 |
| US-GEO-101 | US-GEO-101-SC4 | Success | Mã lớp quyết định trường và lớp | Active · PROVISIONAL | TC-GEO-0103 |
| US-GEO-101 | US-GEO-101-SC5 | Success | Màn "Vào lớp" chỉ có hai ô nhập | Active · PROVISIONAL | TC-GEO-0104 |
| US-GEO-101 | US-GEO-101-SC6 | Success | Tên dài đúng 60 ký tự được nhận | Active · PROVISIONAL | TC-GEO-0105 |
| US-GEO-101 | US-GEO-101-SC7 | Success | Dùng được trên iPhone màn dọc | Active | TC-GEO-0106 |
| US-GEO-101 | US-GEO-101-FC1 | Failure | Tên hiển thị rỗng | Active · PROVISIONAL | TC-GEO-0107 |
| US-GEO-101 | US-GEO-101-FC2 | Failure | Tên hiển thị 61 ký tự | Active · PROVISIONAL | TC-GEO-0108 |
| US-GEO-101 | US-GEO-101-FC3 | Failure | Mã lớp không tồn tại | Active · PROVISIONAL | TC-GEO-0109 |
| US-GEO-101 | US-GEO-101-FC4 | Failure | Phiên hết hạn | Active · PROVISIONAL | TC-GEO-0110 |
| US-GEO-101 | US-GEO-101-FC5 | Failure | Gõ sai mã lớp nhiều lần không bị khóa riêng | Active · PROVISIONAL | TC-GEO-0173 |
| US-GEO-102 | US-GEO-102-SC1 | Success | Mã giáo viên đúng | Active · PROVISIONAL | TC-GEO-0111 |
| US-GEO-102 | US-GEO-102-SC2 | Success | Vào được từ điện thoại | Active · PROVISIONAL | TC-GEO-0112 |
| US-GEO-102 | US-GEO-102-SC3 | Success | Mỗi mã vào đúng tài khoản được cấp | Active · PROVISIONAL | TC-GEO-0174 |
| US-GEO-102 | US-GEO-102-SC4 | Success | Máy chủ không lưu mã rõ | Active · PROVISIONAL | TC-GEO-0172 |
| US-GEO-102 | US-GEO-102-FC1 | Failure | Mã giáo viên sai | Active · PROVISIONAL | TC-GEO-0113 |
| US-GEO-102 | US-GEO-102-FC2 | Failure | Học sinh không làm được việc của giáo viên | Active · PROVISIONAL | TC-GEO-0114 |
| US-GEO-102 | US-GEO-102-FC3 | Failure | Không có công tắc đổi vai | Active · PROVISIONAL | TC-GEO-0115 |
| US-GEO-102 | US-GEO-102-FC4 | Failure | Mã đã thu hồi | Active · PROVISIONAL | TC-GEO-0175 |
| US-GEO-102 | US-GEO-102-FC5 | Failure | Mã quá hạn dùng | Active · PROVISIONAL | TC-GEO-0176 |
| US-GEO-102 | US-GEO-102-FC6 | Failure | Dò mã: lần thử thứ 11 trong 15 phút bị chặn | Active · PROVISIONAL | TC-GEO-0171 |
| US-GEO-102 | US-GEO-102-FC7 | Failure | Phiên học sinh không dùng được đường lời giải của giáo viên | Active · PROVISIONAL | TC-GEO-0177 |
| US-GEO-103 | US-GEO-103-SC1 | Success | Một lượt gọi, một bản ghi đủ trường | Active · PROVISIONAL | TC-GEO-0116 |
| US-GEO-103 | US-GEO-103-SC2 | Success | Đổi model bằng cấu hình | Active · PROVISIONAL | TC-GEO-0117 |
| US-GEO-103 | US-GEO-103-SC3 | Success | Khóa của nhà cung cấp không xuống trình duyệt | Active | TC-GEO-0118 |
| US-GEO-103 | US-GEO-103-SC4 | Success | Không gửi định danh học sinh cho nhà cung cấp | Active · PROVISIONAL | TC-GEO-0119 |
| US-GEO-103 | US-GEO-103-FC1 | Failure | Nhà cung cấp trả lỗi | Active · PROVISIONAL | TC-GEO-0120 |
| US-GEO-103 | US-GEO-103-FC2 | Failure | Nhà cung cấp quá thời gian | Active · PROVISIONAL | TC-GEO-0121 |
| US-GEO-103 | US-GEO-103-FC3 | Failure | Bấm "Thử lại" sau lỗi | Active · PROVISIONAL | TC-GEO-0122 |
| US-GEO-104 | US-GEO-104-SC1 | Success | Học sinh chỉ nhận bài của lớp mình | Active | TC-GEO-0123 |
| US-GEO-104 | US-GEO-104-SC2 | Success | Giáo viên chỉ thấy lớp mình dạy | Active | TC-GEO-0124 |
| US-GEO-104 | US-GEO-104-SC3 | Success | Nội dung hệ thống đọc được từ mọi trường | Active · PROVISIONAL | TC-GEO-0125 |
| US-GEO-104 | US-GEO-104-SC4 | Success | Bản ghi mới mang trường của người tạo | Active | TC-GEO-0126 |
| US-GEO-104 | US-GEO-104-SC5 | Success | Bản ghi có chủ sở hữu, phiên bản lược đồ, nguồn gốc lời giải | Active · PROVISIONAL | TC-GEO-0127 |
| US-GEO-104 | US-GEO-104-FC1 | Failure | Học sinh gọi bài của lớp khác cùng trường | Active · PROVISIONAL | TC-GEO-0128 |
| US-GEO-104 | US-GEO-104-FC2 | Failure | Học sinh gọi bài của trường khác | Active | TC-GEO-0129, TC-GEO-0170 |
| US-GEO-104 | US-GEO-104-FC3 | Failure | Học sinh gọi dữ liệu của bạn cùng lớp | Active | TC-GEO-0130 |
| US-GEO-104 | US-GEO-104-FC4 | Failure | Giáo viên trường khác đổi giờ mở | Active | TC-GEO-0131 |
| US-GEO-104 | US-GEO-104-FC5 | Failure | Giáo viên trường khác xem Theo dõi lớp | Active | TC-GEO-0132, TC-GEO-0170 |
| US-GEO-104 | US-GEO-104-FC6 | Failure | Giao bài chéo trường | Active | TC-GEO-0133 |
| US-GEO-104 | US-GEO-104-FC7 | Failure | Thẻ xác thực bị sửa | Active | TC-GEO-0134 |
| US-GEO-104 | US-GEO-104-FC8 | Failure | Con trỏ của trường khác | Active · PROVISIONAL | TC-GEO-0800 |
| US-GEO-104 | US-GEO-104-FC9 | Failure | Giáo viên không đọc được câu trả lời đã lưu của học sinh | Active · PROVISIONAL | TC-GEO-0178 |
| US-GEO-105 | US-GEO-105-SC1 | Success | Báo cáo của một lần chạy trên sáu đề | Active · PROVISIONAL | TC-GEO-0135 |
| US-GEO-105 | US-GEO-105-SC2 | Success | Một đề có đáp số sai làm tỉ lệ giảm đúng công thức | Active · PROVISIONAL | TC-GEO-0136 |
| US-GEO-105 | US-GEO-105-SC3 | Success | Chạy lại khi đổi model giữ cả hai báo cáo | Active | TC-GEO-0137 |
| US-GEO-105 | US-GEO-105-SC4 | Success | So với ngưỡng đề xuất | Active · PROVISIONAL | TC-GEO-0138 |
| US-GEO-105 | US-GEO-105-SC5 | Success | Ghi chú AI giả lập | Active · PROVISIONAL | TC-GEO-0139 |
| US-GEO-105 | US-GEO-105-FC1 | Failure | Bộ đề trống | Active | TC-GEO-0140 |
| US-GEO-105 | US-GEO-105-FC2 | Failure | Một đề thiếu đáp số tham chiếu | Active | TC-GEO-0141 |
| US-GEO-105 | US-GEO-105-FC3 | Failure | Không bản ghi nào mang "Đã kiểm chứng" khi hình hoặc đáp số chưa khớp | Active | TC-GEO-0142 |
| US-GEO-106 | US-GEO-106-SC1 | Success | Sáu đề có đáp số tham chiếu đúng nguồn | Active | TC-GEO-0143 |
| US-GEO-106 | US-GEO-106-SC2 | Success | Đáp số nhiều giá trị lưu thành từng đại lượng | Active | TC-GEO-0144 |
| US-GEO-106 | US-GEO-106-SC3 | Success | Đáp số theo tham số có ba bộ giá trị kiểm | Active · PROVISIONAL | TC-GEO-0145 |
| US-GEO-106 | US-GEO-106-SC4 | Success | Một đề, một bản ghi cho cả kho đề và bộ đề chuẩn | Active | TC-GEO-0146 |
| US-GEO-106 | US-GEO-106-FC1 | Failure | Đề thiếu đáp số không vào bộ đề chuẩn một cách im lặng | Active | TC-GEO-0147 |
| US-GEO-106 | US-GEO-106-FC2 | Failure | Nhãn trong tệp nguồn không thành nhãn máy | Active | TC-GEO-0148 |
| US-GEO-107 | US-GEO-107-SC1 | Success | p95 đọc dưới tải đều | Active · PROVISIONAL | TC-GEO-0149 |
| US-GEO-107 | US-GEO-107-SC2 | Success | p95 ghi dưới tải đều | Active · PROVISIONAL | TC-GEO-0150 |
| US-GEO-107 | US-GEO-107-SC3 | Success | Tỉ lệ lỗi dưới tải đều | Active · PROVISIONAL | TC-GEO-0151 |
| US-GEO-107 | US-GEO-107-SC4 | Success | Đỉnh 1000 học sinh mở lời giải trong 10 giây | Active · PROVISIONAL | TC-GEO-0152 |
| US-GEO-107 | US-GEO-107-SC5 | Success | Không lộ lời giải dưới tải | Active | TC-GEO-0153 |
| US-GEO-107 | US-GEO-107-FC1 | Failure | Vượt ngưỡng đề xuất | Active · PROVISIONAL | TC-GEO-0154 |
| US-GEO-107 | US-GEO-107-FC2 | Failure | Quá tải thì từ chối nhanh, không treo | Active · PROVISIONAL | TC-GEO-0155 |
| US-GEO-108 | US-GEO-108-SC1 | Success | Sinh đủ quy mô bằng một lệnh | Active · PROVISIONAL | TC-GEO-0156 |
| US-GEO-108 | US-GEO-108-SC2 | Success | Phân bố theo công thức | Active · PROVISIONAL | TC-GEO-0157 |
| US-GEO-108 | US-GEO-108-SC3 | Success | Chạy lại cho cùng quy mô | Active | TC-GEO-0158 |
| US-GEO-108 | US-GEO-108-FC1 | Failure | Không lẫn vào dữ liệu mẫu | Active | TC-GEO-0159 |
| US-GEO-108 | US-GEO-108-FC2 | Failure | Tên sinh ra không phải tên thật | Active | TC-GEO-0160 |

### 3.2 Chiều ngược: case → tiêu chí hoặc NFR (75 / 75 case trỏ về một tiêu chí của BA hoặc một NFR-GEO, 100.0%; riêng trỏ về tiêu chí của BA: 71 / 75, 94.7%)

Kiểm bằng `gen_cases.py`: mỗi case bám tiêu chí lấy đúng một tiêu chí từ bảng phân loại; case viết tay ghi tiêu chí ở Remark và bộ sinh từ chối id tiêu chí không tồn tại.

Case không trỏ về tiêu chí nào của BA (loại X: chỉ trỏ về NFR-GEO, BR-GEO, REQ-GEO hoặc D-GEO; đã gửi BA xem có cần tiêu chí không, xem `qc_findings.md`):

| Case | Trỏ về |
|---|---|
| TC-GEO-0801 | D-GEO-125, NFR-GEO-031, NFR-GEO-040, REQ-GEO-39 |
| TC-GEO-0802 | D-GEO-125, NFR-GEO-031, NFR-GEO-042, REQ-GEO-39 |
| TC-GEO-0803 | BR-GEO-01, BR-GEO-19, D-GEO-045, D-GEO-125, D-GEO-170, NFR-GEO-057 |
| TC-GEO-0804 | BR-GEO-19, D-GEO-125, NFR-GEO-034 |

### 3.3 NFR-GEO được case trong tệp này nhắc tới

NFR-GEO-001, NFR-GEO-002, NFR-GEO-003, NFR-GEO-004, NFR-GEO-020, NFR-GEO-024, NFR-GEO-026, NFR-GEO-030, NFR-GEO-031, NFR-GEO-032, NFR-GEO-034, NFR-GEO-040, NFR-GEO-042, NFR-GEO-049, NFR-GEO-054, NFR-GEO-055, NFR-GEO-057, NFR-GEO-059, NFR-GEO-060, NFR-GEO-070, NFR-GEO-071

### 3.4 Khoảng trống

- Không tiêu chí nào thiếu case.
- Tiêu chí không kiểm được như đang viết, mâu thuẫn, thiếu giá trị: xem `qc_findings.md` cùng thư mục.
- Ca hiếm đã hoãn: `work/backlog_cases.md`.

## 4. Dữ liệu và môi trường

- Dữ liệu: `work/fixture.md` (BA), `review/QC_test_design.md` §6 (QC), `work/architecture/bench/` (ARCH, thử tải).
- Môi trường, người thực thi, công cụ, known gaps, quyết định đang dùng: `review/test_plan.md` §3, §5, §6, §7.

## 5. Change log

| No | Thay đổi | Bởi |
|---|---|---|
| 1 | bản đầu của goal run: 67 case cho 62 tiêu chí | QC |
| 2 | lượt sửa gom sau phản biện (M-304): thêm TC-GEO-0171 (dò mã giáo viên → RATE_LIMITED), 0172 (mã lưu băm) theo D-GEO-152, RV-GEO-02; TC-GEO-0155 mã SERVICE_OVERLOADED (D-GEO-143); công cụ Gatling cho case tải và tầng, công cụ cho 4 case X (D-GEO-170, RV-GEO-35); dẫn D-GEO-125 ở case X; con trỏ gắn trường (D-GEO-168): 75 case cho 70 tiêu chí | QC |
| 3 | đồng bộ với bàn giao của BA (M-217, M-218): case viết tay trùng tiêu chí mới chuyển thành case bám tiêu chí (giữ id), thêm case cho mọi tiêu chí mới, gỡ chỉnh không còn cần: 75 case cho 70 tiêu chí | QC |
