# E4 — test case (Gợi ý, lời giải đồng bộ hình, hỏi đáp, tự học)

> Chủ sở hữu: QC. **Tệp sinh ra, không sửa tay**: sửa `work/src/qc/E4_meta.txt` (phân loại từng tiêu chí), `work/src/qc/E4_extra.md` (case viết tay) hoặc `work/src/qc/E4_override.txt` (chỉnh của QC theo một D-GEO khi AC chưa kịp đổi; mỗi chỉnh ghi nguồn ở Remark) rồi chạy `python3 work/src/qc/gen_cases.py E4`. Tiền điều kiện, bước và kỳ vọng của case bám tiêu chí lấy nguyên từ Given / When / Then của `stories_ac.md` (BA); giá trị cụ thể là của `work/fixture.md` và công thức M-GEO. Id ổn định; tiêu chí bị gạch bỏ thì case giữ id với N/A. AI ở MVP là giả lập (D-GEO-047): không có kỳ vọng về chất lượng AI thật.

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
| `REQ-GEO-nn` | yêu cầu thô (`work/00_gate0_intake_voc.md`) | cột Remark |
| `CL-FN-nn` | nhóm kiểm tra chức năng (`review/QC_test_design.md` §1) | cột Remark |
| `CL-UI-nn` | nhóm kiểm tra giao diện và thiết bị | cột Remark |
| `HKG-nn` | mã bài của Kho đề | dữ liệu |
| `BG-n`, `BG-C` | bài giao mẫu của `work/fixture.md` mục 3 | dữ liệu |
| `BV-n` | ca biên giờ mở (`review/QC_test_design.md` §4) | cột Remark |
| `DT-n` | dòng bảng quyết định khóa lời giải × vai × trạng thái (`review/QC_test_design.md` §3) | cột Remark |
| `KG-nn` | known gap (`work/known_gaps.md`) | cột Remark |
| `PO-nn` | quyết định trong sổ quyết định của chủ sản phẩm | cột Remark |
| `RV-GEO-nn` | phát hiện của phản biện RV (`review/RV_findings.md`) | cột Remark |
| PROVISIONAL · ENG · X · N/A | kỳ vọng theo giả định chưa xác nhận · case cần công cụ kỹ thuật · case xuyên suốt không bám một tiêu chí · case giữ id nhưng không chạy | cột Remark |
| R · t · T0 | giờ mở lời giải (`release_at`) · giờ máy chủ · giờ máy chủ lúc bắt đầu ca kiểm | ca giờ mở |
| Chưa đặt giờ · Sắp mở · Đã mở | ba trạng thái giờ mở: chưa có giờ (khóa) · t < R (khóa) · t ≥ R (được xem) | kỳ vọng |
| Đã kiểm chứng · Cần xem lại · AI soạn | nhãn máy do engine tính · nhãn máy · nhãn người | kỳ vọng |

## 1. Tóm tắt

| Story | Số tiêu chí (SC / FC) | Số case | PROVISIONAL |
|---|---|---|---|
| US-GEO-401 Nhập đề tự học bằng chữ | 9 (4 / 5) | 9 | 6 |
| US-GEO-402 Nhập đề bằng ảnh | 6 (3 / 3) | 6 | 5 |
| US-GEO-403 Chọn hình mẫu để khám phá | 4 (3 / 1) | 4 | 2 |
| US-GEO-404 Xin Gợi ý 1 và Gợi ý 2 | 10 (5 / 5) | 10 | 5 |
| US-GEO-405 Mở lời giải của bài tự học sau Gợi ý 1 và 2 | 6 (2 / 4) | 6 | 4 |
| US-GEO-406 Tự học kế thừa khóa của bài đang khóa | 16 (10 / 6) | 18 | 17 |
| US-GEO-407 Xem lời giải đồng bộ hình: bấm bước thì hình tô màu | 8 (6 / 2) | 8 | 2 |
| US-GEO-408 Đường vẽ thêm của từng bước hiện dần; "Vì sao nghĩ ra bước này?" | 5 (4 / 1) | 5 | 0 |
| US-GEO-409 Hỏi đáp trong ngữ cảnh bài, có nguồn | 9 (6 / 3) | 9 | 5 |
| US-GEO-410 Nhật ký hỏi đáp lọc theo bài | 7 (5 / 2) | 7 | 3 |
| US-GEO-411 Từ khóa bấm được mở bài giảng có nguồn và nhãn | 7 (4 / 3) | 7 | 4 |
| US-GEO-412 Dữ liệu: kho bài giảng mẫu | 6 (4 / 2) | 6 | 4 |
| **Tổng** | **93** | **95** | **57** |

| Theo ưu tiên | Số case |
|---|---|
| High | 38 |
| Medium | 49 |
| Low | 8 |

| Theo loại | Số case |
|---|---|
| chức năng | 69 |
| phân quyền | 7 |
| dữ liệu trả về | 17 |
| phi chức năng | 0 |
| thiết bị | 2 |

| Theo tầng tự động hóa (một case có thể ở hai tầng) | Số case |
|---|---|
| đơn vị | 2 |
| tích hợp API | 60 |
| tích hợp CSDL | 4 |
| E2E trình duyệt | 0 |
| tải | 0 |
| thủ công trên thiết bị | 60 |

| Theo Tự động hóa (một case một loại) | Số case |
|---|---|
| E2E-UI | 59 |
| E2E-API | 29 |
| IT | 6 |
| Manual | 1 |
| **Tổng** | **95** |

## 2. Test case

### Cách đọc một test case
- Mỗi dòng là một case. Chuẩn bị theo **Pre-condition**, dùng giá trị ở **Test data**, làm lần lượt các bước đánh số ở **Test step**, rồi so với **Expected output**.
- **Expected output**: mỗi gạch đầu dòng là một điều phải đúng; chữ trong ngoặc kép và giá trị phải khớp nguyên văn. Case đạt khi mọi gạch đầu dòng đều đúng.
- **Loại**: chức năng · phân quyền · dữ liệu trả về (soát thân phản hồi của máy chủ, không chỉ giao diện) · phi chức năng · thiết bị.
- **Tầng tự động hóa**: đơn vị · tích hợp API · tích hợp CSDL · E2E trình duyệt (Playwright) · tải (Gatling) · thủ công trên thiết bị; đếm câu lệnh JDBC dùng datasource-proxy (D-GEO-170). Case có chữ ENG ở Remark cần công cụ kỹ thuật và thuộc tờ kỹ thuật; case còn lại người kiểm thử chạy trên màn.
- **Tự động hóa** (PO-50): cách tự động hóa chính của case, đầu vào của bộ E2E. `E2E-UI` · Playwright qua trình duyệt, theo đúng bước trên màn và kỳ vọng nhìn thấy (phần "máy chủ trả …" của kỳ vọng thì soát thêm qua phản hồi mạng mà trang nhận) · `E2E-API` · gọi HTTP thẳng tới máy chủ, soát `code` và thân phản hồi · `IT` · kiểm thử tích hợp phía máy chủ (CSDL, bộ quét, tầng dịch vụ) · `Manual` · người chạy, lý do ghi ngay sau.
- **PROVISIONAL**: kỳ vọng theo một giả định chưa được xác nhận, mã ghi ngay sau. Vẫn chạy; nếu màn chỉ khác ở đúng điểm đó thì ghi lại điều thấy và đánh dấu Blocked, không đánh Fail.
- Thực tế khác kỳ vọng thì ghi vào vòng thực thi, không sửa ô Expected.

### US-GEO-401 — Nhập đề tự học bằng chữ

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0400 | Dán đề và nhận hình | High | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Học sinh An | • An đã vào lớp 11A1, ở màn "Tự học", cấp "THPT" | • Hàng dữ liệu: F-GEO-060 | 1. An dán đề lăng trụ F-GEO-060 vào "Đề bài của em" và bấm "Dựng hình" | • Sau màn chờ, màn Làm bài của bài tự học mở với hình lăng trụ đứng ABC.A′B′C′ (AB = 3, AC = 4, AA′ = 5) do engine dựng, đề An đã dán và nhãn máy | US-GEO-401 · SC1; CL-FN-01; PROVISIONAL D-GEO-052 |
| TC-GEO-0401 | Màn chờ rõ ràng | Medium | Normal (Success case) | chức năng | thủ công trên thiết bị | E2E-UI | Học sinh An | • An vừa bấm "Dựng hình" với đề F-GEO-060<br/>• Nhà cung cấp giả lập trả lời sau 5 giây | • Hàng dữ liệu: F-GEO-060 | 1. Trong 5 giây đó | • Màn hiện "Đang dựng hình, em chờ một chút…" (MSG-GEO-13)<br/>• Nút "Dựng hình" không bấm được<br/>• Không có hình hay lời giải dở dang | US-GEO-401 · SC2; CL-FN-14 |
| TC-GEO-0402 | Bài tự học mới bắt đầu ở mức 0 | Medium | Normal (Success case) | chức năng | tích hợp API | E2E-API | Học sinh An | • Bài tự học của SC1 vừa tạo | • Không có dữ liệu nhập riêng | 1. An mở tab "Gợi ý" và tab "Lời giải" | • "Tiến độ của em 0/3"<br/>• Có nút "Xem gợi ý 1"<br/>• Tab "Lời giải" hiện MSG-GEO-30 | US-GEO-401 · SC3; CL-FN-04; PROVISIONAL D-GEO-023; ENG |
| TC-GEO-0403 | Ghi thời gian từ gửi đề tới có hình | Low | Normal (Success case) | chức năng | đơn vị | IT | Kỹ thuật | • Ba đề tự học thành công, mỗi đề có một sự kiện `scene_shown` với thời lượng 10, 20 và 60 giây<br/>• Một đề lỗi (không có `scene_shown`) | • Không có dữ liệu nhập riêng | 1. Tính chỉ số của kỳ đo | • Thời gian trung vị là 20,0 giây<br/>• Đề lỗi không tính vào trung vị và được đếm riêng là 1<br/>• Báo cáo ghi "ngưỡng đề xuất < 40 giây, chỉ có nghĩa trên AI thật" | US-GEO-401 · SC4; CL-FN-13; PROVISIONAL D-GEO-060; ENG; chỉ kiểm công thức trung vị; trên AI giả lập số đo không có nghĩa |
| TC-GEO-0404 | Đề rỗng | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Học sinh An | • Ô "Đề bài của em" rỗng hoặc chỉ có khoảng trắng (F-GEO-065) | • Hàng dữ liệu: F-GEO-065 | 1. Bấm "Dựng hình" | • Hiện "Em hãy nhập đề bài." (MSG-GEO-20)<br/>• Không có lượt gọi AI nào<br/>• Không tạo bài | US-GEO-401 · FC1; CL-FN-02 |
| TC-GEO-0405 | Đề 8001 ký tự | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Học sinh An | • Đề dài 8001 ký tự (F-GEO-065) | • Hàng dữ liệu: F-GEO-065 | 1. Bấm "Dựng hình" | • Hiện "Đề bài tối đa 8000 ký tự." (MSG-GEO-21)<br/>• Máy chủ trả `VALIDATION_FAILED` nếu yêu cầu vẫn được gửi<br/>• Không tạo bài | US-GEO-401 · FC2; CL-FN-02; PROVISIONAL QA-GEO-01; ENG; BVA: 8000 được, 8001 không |
| TC-GEO-0406 | Trợ lý lỗi khi dựng hình | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Học sinh An | • Nhà cung cấp giả lập ở chế độ trả lỗi | • Hàng dữ liệu: F-GEO-060 | 1. An bấm "Dựng hình" với đề F-GEO-060 | • Màn chờ đóng<br/>• Hiện "Trợ lý đang bận. Em thử lại sau ít phút nhé." (MSG-GEO-14) và nút "Thử lại"<br/>• Đề còn nguyên trong ô<br/>• Không có bài tự học dở dang trong danh sách của An | US-GEO-401 · FC3; CL-FN-14; PROVISIONAL D-GEO-072 |
| TC-GEO-0407 | Đề không phải hình học | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Học sinh An | • An gõ "Thủ đô của nước Pháp là gì?" | • Không có dữ liệu nhập riêng | 1. Bấm "Dựng hình" | • Hiện "Chưa dựng được hình từ đề này. Em kiểm tra lại đề hoặc chọn một hình mẫu." (MSG-GEO-18)<br/>• Không có hình<br/>• Không tạo bài | US-GEO-401 · FC4; CL-FN-02; ENG |
| TC-GEO-0408 | Bạn khác không thấy bài tự học của mình | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Học sinh Bình gọi bài tự học của An | • Bài tự học của An ở SC1 | • Không có dữ liệu nhập riêng | 1. Bình (cùng lớp) gọi bài đó bằng mã của nó | • `NOT_FOUND` ("Không tìm thấy nội dung này." (MSG-GEO-04))<br/>• Dữ liệu trả về không có đề hay hình của An | US-GEO-401 · FC5; CL-FN-05; PROVISIONAL D-GEO-076; ENG |

### US-GEO-402 — Nhập đề bằng ảnh

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0409 | Ảnh rõ được đọc thành chữ để sửa | Medium | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Học sinh An, iPhone | • An ở "Tự học" trên iPhone<br/>• Ảnh chụp đề HKG-03, 400 KB (F-GEO-066) | • Hàng dữ liệu: F-GEO-066 | 1. An bấm "Chụp hoặc dán ảnh đề" và chọn ảnh đó | • Sau màn chờ, ô "Đề bài của em" chứa "Lăng trụ đứng tam giác có đáy là tam giác đều cạnh 4 cm, chiều cao 6 cm. Tính thể tích."<br/>• Chưa có hình nào được dựng | US-GEO-402 · SC1; CL-FN-14; PROVISIONAL D-GEO-110; chữ đọc được là nội dung soạn sẵn của AI giả lập |
| TC-GEO-0410 | Sửa chữ rồi dựng hình | Medium | Normal (Success case) | chức năng | thủ công trên thiết bị | E2E-UI | Học sinh An, iPhone | • Ô đề đang chứa chữ đọc từ ảnh ở SC1 | • Không có dữ liệu nhập riêng | 1. An sửa "4 cm" thành "5 cm" và bấm "Dựng hình" | • Hình lăng trụ dựng với cạnh đáy 5<br/>• Đề lưu trong bài là bản An đã sửa | US-GEO-402 · SC2; CL-FN-01; PROVISIONAL D-GEO-110 |
| TC-GEO-0411 | Chụp trực tiếp bằng máy ảnh của iPad, iPhone | Medium | Normal (Success case) | thiết bị | thủ công trên thiết bị | Manual — cần thiết bị thật (bàn phím ảo, cảm ứng hoặc màn hình của máy thật); Playwright giả lập không thay được | Học sinh An, iPad và iPhone | • An ở "Tự học" trên iPad | • Không có dữ liệu nhập riêng | 1. Bấm "Chụp hoặc dán ảnh đề" | • Có lựa chọn chụp ảnh mới và lựa chọn lấy ảnh có sẵn của thiết bị | US-GEO-402 · SC3; CL-UI-01; PROVISIONAL KG-09 |
| TC-GEO-0412 | Ảnh mờ | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Học sinh An | • Ảnh nhòe (F-GEO-067) | • Hàng dữ liệu: F-GEO-067 | 1. An chọn ảnh đó | • Hiện "Chưa đọc được chữ trong ảnh. Em chụp lại rõ hơn hoặc gõ đề." (MSG-GEO-22)<br/>• Chữ đang có trong ô đề (nếu có) giữ nguyên | US-GEO-402 · FC1; CL-FN-03; ENG |
| TC-GEO-0413 | Ảnh vượt 1 MB | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Học sinh An | • Ảnh 1,5 MB (F-GEO-068) | • Hàng dữ liệu: F-GEO-068 | 1. An chọn ảnh đó và thiết bị gửi nguyên ảnh | • Máy chủ trả `PAYLOAD_TOO_LARGE`<br/>• Hiện "Nội dung gửi lên quá lớn (tối đa 1 MB)." (MSG-GEO-06)<br/>• Ô đề không đổi | US-GEO-402 · FC2; CL-FN-02; PROVISIONAL D-GEO-110; ENG; BVA: thân yêu cầu 1 MB |
| TC-GEO-0414 | Trợ lý lỗi khi đọc ảnh | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Học sinh An | • Nhà cung cấp giả lập ở chế độ trả lỗi | • Hàng dữ liệu: F-GEO-066 | 1. An chọn ảnh F-GEO-066 | • Hiện "Trợ lý đang bận. Em thử lại sau ít phút nhé." (MSG-GEO-14) và nút "Thử lại"<br/>• Ô đề không đổi | US-GEO-402 · FC3; CL-FN-14; PROVISIONAL D-GEO-072; ENG |

### US-GEO-403 — Chọn hình mẫu để khám phá

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0415 | Vào từ "Tự khám phá" của Bài của em | Medium | Normal (Success case) | chức năng | thủ công trên thiết bị | E2E-UI | Học sinh An | • An ở "Bài của em"<br/>• Khối "Tự khám phá" có các hình nhỏ và nút "Mở chế độ tự học" | • Không có dữ liệu nhập riêng | 1. An bấm hình chóp nhỏ | • Màn khám phá mở với hình chóp mẫu, xoay được ngay | US-GEO-403 · SC1; CL-FN-01 |
| TC-GEO-0416 | "Mở chế độ tự học" | Medium | Normal (Success case) | chức năng | thủ công trên thiết bị | E2E-UI | Học sinh An | • An ở "Bài của em" | • Không có dữ liệu nhập riêng | 1. Bấm "Mở chế độ tự học" | • Màn "Tự học" mở với ô "Đề bài của em" và sáu thẻ hình mẫu | US-GEO-403 · SC2; CL-FN-01 |
| TC-GEO-0417 | Màn khám phá không có gợi ý, lời giải, hỏi đáp | Medium | Normal (Success case) | chức năng | thủ công trên thiết bị | E2E-UI | Học sinh An | • An đang ở màn khám phá hình hộp mẫu | • Không có dữ liệu nhập riêng | 1. Xem các tab và nút của màn | • Có hình, ô tham số, thanh công cụ<br/>• Không có tab "Gợi ý", "Lời giải", "Hỏi đáp" và không có ô "Hỏi về bài này…" | US-GEO-403 · SC3; CL-UI-03; PROVISIONAL D-GEO-136 |
| TC-GEO-0418 | Chưa vào lớp | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Chưa vào lớp | • Một người chưa vào lớp | • Không có dữ liệu nhập riêng | 1. Mở thẳng địa chỉ của màn "Tự học" | • Máy chủ trả `UNAUTHENTICATED`<br/>• Hiện "Phiên đã hết. Vui lòng vào lại." (MSG-GEO-02) và chuyển tới màn "Vào lớp" | US-GEO-403 · FC1; CL-FN-05; PROVISIONAL D-GEO-052; ENG |

### US-GEO-404 — Xin Gợi ý 1 và Gợi ý 2

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0419 | Xem Gợi ý 1 | High | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Học sinh Hà | • Hà mở BG-1 lần đầu (F-GEO-054)<br/>• Tab "Gợi ý" hiện "Tiến độ của em 0/3" và nút "Xem gợi ý 1" | • Hàng dữ liệu: F-GEO-054 | 1. Hà bấm "Xem gợi ý 1" | • Hiện thẻ "Gợi ý 1 · Hướng nghĩ" với nội dung "Góc giữa đường thẳng và mặt phẳng là góc giữa đường đó và hình chiếu của nó trên mặt phẳng."<br/>• Tiến độ thành 1/3<br/>• Có nút "Xem gợi ý 2" | US-GEO-404 · SC1; CL-FN-04 |
| TC-GEO-0420 | Xem Gợi ý 2 | High | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Học sinh Hà | • Hà đã xem Gợi ý 1 của BG-1 | • Không có dữ liệu nhập riêng | 1. Bấm "Xem gợi ý 2" | • Hiện thẻ "Gợi ý 2 · Bước kế tiếp" với nội dung "Hình chiếu của S lên mặt đáy là điểm nào? Từ đó suy ra hình chiếu của SD."<br/>• Tiến độ 2/3<br/>• Ở "Bài của em" bài này ghi "Đã xem gợi ý 2" | US-GEO-404 · SC2; CL-FN-04; PROVISIONAL D-GEO-075 |
| TC-GEO-0421 | Giáo viên thấy mức gợi ý vừa dùng | Medium | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Học sinh rồi Giáo viên mẫu | • Dữ liệu lớp như F-GEO-050…054 ("dùng gợi ý" = 3)<br/>• Hà vừa xem Gợi ý 1 của BG-1 | • Hàng dữ liệu: F-GEO-050…054 | 1. Giáo viên mẫu mở Theo dõi lớp của BG-1 | • Thẻ "dùng gợi ý" là 4<br/>• Dòng Hà có một trong hai đèn "Gợi ý 1 · 2" sáng | US-GEO-404 · SC3; CL-FN-13; hai đầu của quy tắc: màn học sinh và Theo dõi lớp |
| TC-GEO-0422 | Xem lại gợi ý không làm tăng tiến độ | Medium | Normal (Success case) | chức năng | tích hợp API | E2E-API | Học sinh | • Bình đã xem Gợi ý 1 của BG-1 (tiến độ 1/3) | • Không có dữ liệu nhập riêng | 1. Bình rời bài, mở lại và đọc lại Gợi ý 1 | • Gợi ý 1 hiện sẵn không cần bấm lại<br/>• Tiến độ vẫn 1/3 | US-GEO-404 · SC4; CL-FN-10; ENG |
| TC-GEO-0423 | Gợi ý trên bài tự học | Medium | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Học sinh An | • Bài tự học của An từ đề F-GEO-060 (không trùng bài đang khóa nào nên không kế thừa khóa; gợi ý do AI sinh) | • Hàng dữ liệu: F-GEO-060 | 1. An bấm "Xem gợi ý 1"<br/>2. "Xem gợi ý 2" | • Hai gợi ý hiện lần lượt<br/>• Tiến độ 2/3<br/>• Tab "Lời giải" hết hiện MSG-GEO-30 và có nút "Xem lời giải" | US-GEO-404 · SC5; CL-FN-01 |
| TC-GEO-0424 | Xin Gợi ý 2 khi chưa xem Gợi ý 1 | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Học sinh Hà | • Hà chưa xem gợi ý nào của BG-1 | • Không có dữ liệu nhập riêng | 1. Từ phiên của Hà, gửi thẳng tới máy chủ yêu cầu Gợi ý 2 | • `FORBIDDEN` với "Em xem Gợi ý 1 trước nhé." (MSG-GEO-34)<br/>• Dữ liệu trả về không có nội dung Gợi ý 2<br/>• Tiến độ vẫn 0/3<br/>• Trên màn không có nút "Xem gợi ý 2" khi chưa xem Gợi ý 1 | US-GEO-404 · FC1; CL-FN-04; PROVISIONAL D-GEO-108 |
| TC-GEO-0425 | Giáo viên đã tắt gợi ý | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API + thủ công trên thiết bị | E2E-UI | Học sinh Khánh (9B) | • BG-5 (lớp 9B) có "Gợi ý mức 1 và 2" tắt (F-GEO-044)<br/>• Khánh mở BG-5 | • Hàng dữ liệu: F-GEO-044 | 1. Khánh mở tab "Gợi ý",<br/>2. Gửi thẳng yêu cầu Gợi ý 1 | • Tab hiện "Giáo viên đã tắt gợi ý cho bài này." (MSG-GEO-32), không có nút xin gợi ý<br/>• Yêu cầu gửi thẳng nhận `FORBIDDEN` và không có nội dung gợi ý | US-GEO-404 · FC2; CL-FN-05; PROVISIONAL D-GEO-053 |
| TC-GEO-0426 | Gợi ý chưa xin thì chưa được gửi xuống | High | Abnormal (Fail case/Edge case) | dữ liệu trả về | tích hợp API | E2E-API | Học sinh Hà | • Hà mở BG-1, chưa xin gợi ý nào | • Không có dữ liệu nhập riêng | 1. Ghi lại dữ liệu trả về của trang Làm bài | • Không có câu chữ của Gợi ý 1 hay Gợi ý 2 | US-GEO-404 · FC3; CL-FN-07; PROVISIONAL D-GEO-108; ENG; tiêu đề không lưu đệm của phản hồi gợi ý ở TC-GEO-0488 (D-GEO-145) |
| TC-GEO-0427 | Mất mạng lúc bấm | Medium | Abnormal (Fail case/Edge case) | chức năng | thủ công trên thiết bị | E2E-UI | Học sinh Hà | • Hà ở BG-1, tiến độ 0/3<br/>• Thiết bị mất mạng | • Không có dữ liệu nhập riêng | 1. Bấm "Xem gợi ý 1" | • Hiện "Không kết nối được. Vui lòng kiểm tra mạng và thử lại." (MSG-GEO-08)<br/>• Không có gợi ý nào hiện<br/>• Khi có mạng lại, tiến độ vẫn 0/3 cho tới khi Hà bấm lại | US-GEO-404 · FC4; CL-UI-04 |
| TC-GEO-0488 | Phản hồi gợi ý không cho lưu đệm | Medium | Abnormal (Fail case/Edge case) | dữ liệu trả về | tích hợp API | E2E-API | Học sinh Hà, iPad dùng chung | • Hà mở BG-1 trên iPad dùng chung của lớp | • Không có dữ liệu nhập riêng | 1. Hà bấm "Xem gợi ý 1"<br/>2. Ghi lại tiêu đề của phản hồi chứa Gợi ý 1 | • Phản hồi có tiêu đề `Cache-Control: no-store` | US-GEO-404 · FC5; CL-FN-07; PROVISIONAL D-GEO-145; ENG; D-GEO-145, RV-GEO-25 |

### US-GEO-405 — Mở lời giải của bài tự học sau Gợi ý 1 và 2

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0428 | Đủ hai gợi ý thì mở được lời giải | High | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Học sinh An | • Bài tự học của An từ đề F-GEO-060<br/>• An đã xem Gợi ý 1 và Gợi ý 2 | • Hàng dữ liệu: F-GEO-060 | 1. An mở tab "Lời giải" và bấm "Xem lời giải" | • Các bước giải và đáp số hiện ra<br/>• Tiến độ 3/3<br/>• Có một sự kiện mở lời giải | US-GEO-405 · SC1; CL-FN-04; PROVISIONAL D-GEO-023 |
| TC-GEO-0429 | Đếm lượt gợi ý trước khi mở lời giải | Low | Normal (Success case) | chức năng | đơn vị | IT | Kỹ thuật | • An đã xem Gợi ý 1 một lần, Gợi ý 2 một lần rồi mở lời giải như SC1 | • Không có dữ liệu nhập riêng | 1. Tính chỉ số của bài đó | • Lượt xem gợi ý trước khi xem lời giải = 2 | US-GEO-405 · SC2; CL-FN-13; ENG |
| TC-GEO-0430 | Chưa xem gợi ý nào | High | Abnormal (Fail case/Edge case) | chức năng | thủ công trên thiết bị | E2E-UI | Học sinh An | • Bài tự học mới tạo, tiến độ 0/3 | • Không có dữ liệu nhập riêng | 1. An mở tab "Lời giải" | • Hiện MSG-GEO-30<br/>• Không có nút "Xem lời giải"<br/>• Không có bước giải hay đáp số | US-GEO-405 · FC1; CL-FN-04; PROVISIONAL D-GEO-023 |
| TC-GEO-0431 | Mới xem Gợi ý 1 mà gọi thẳng lời giải | High | Abnormal (Fail case/Edge case) | dữ liệu trả về | tích hợp API | E2E-API | Học sinh An | • An mới xem Gợi ý 1 của bài tự học F-GEO-060 | • Hàng dữ liệu: F-GEO-060 | 1. Từ phiên của An, gửi thẳng tới máy chủ yêu cầu lời giải của bài đó | • `FORBIDDEN`<br/>• Dữ liệu trả về không có bước giải, không có "60/√769", không có "2,1637" | US-GEO-405 · FC2; CL-FN-07; PROVISIONAL D-GEO-023; ENG |
| TC-GEO-0432 | Trang bài tự học không mang sẵn lời giải | High | Abnormal (Fail case/Edge case) | dữ liệu trả về | tích hợp API | E2E-API | Học sinh An | • Bài tự học F-GEO-060, tiến độ 0/3 | • Hàng dữ liệu: F-GEO-060 | 1. Ghi lại dữ liệu trả về của trang Làm bài và của hai lần xin gợi ý | • Không phản hồi nào chứa bước giải hay đáp số | US-GEO-405 · FC3; CL-FN-07; ENG |
| TC-GEO-0433 | Hỏi đáp không mở trước lời giải của bài tự học | High | Abnormal (Fail case/Edge case) | dữ liệu trả về | tích hợp API | E2E-API | Học sinh An | • Bài tự học F-GEO-060, An mới xem Gợi ý 1 | • Hàng dữ liệu: F-GEO-060<br/>• Giá trị nhập: "Hỏi đáp" | 1. An hỏi ở tab "Hỏi đáp": "Đáp số bài này là bao nhiêu?" | • Câu trả lời theo "Lời giải đang khóa nên mình chưa nêu đáp số hay các bước giải. Em xem lại gợi ý nhé: {gợi ý cao nhất em đã xem}" (MSG-GEO-35), nhắc lại Gợi ý 1<br/>• Không chứa "60/√769" hay "2,1637"<br/>• Tiến độ vẫn 1/3 | US-GEO-405 · FC4; CL-FN-07; PROVISIONAL D-GEO-027; ENG; với AI giả lập: kiểm lớp Kiểm soát đáp án bằng câu trả lời soạn sẵn có chứa đáp số |

### US-GEO-406 — Tự học kế thừa khóa của bài đang khóa

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0434 | Dán lại đề đang khóa thì bị khóa theo bài giao | High | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Học sinh An | • BG-1 (HKG-01, lớp 11A1) đang Sắp mở<br/>• An dán nguyên văn đề HKG-01 vào "Tự học" (F-GEO-062) và dựng hình | • Hàng dữ liệu: F-GEO-062 | 1. An xem Gợi ý 1, Gợi ý 2<br/>2. Mở tab "Lời giải" | • Tab hiện "Đề này trùng với bài lớp em đang làm. Lời giải mở theo giờ giáo viên đặt." (MSG-GEO-31)<br/>• Không có nút "Xem lời giải"<br/>• Không có bước giải hay đáp số | US-GEO-406 · SC1; CL-FN-07; PROVISIONAL D-GEO-026; DT-8, cột Sắp mở |
| TC-GEO-0435 | Bài giao mở thì bài tự học kế thừa cũng mở | High | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Giáo viên mẫu rồi học sinh An | • Bài tự học của SC1<br/>• Giáo viên mẫu bấm "Mở ngay" cho BG-1 | • Không có dữ liệu nhập riêng | 1. An mở lại tab "Lời giải" của bài tự học đó | • Lời giải và đáp số "45°" xem được | US-GEO-406 · SC2; CL-FN-09; PROVISIONAL D-GEO-026; DT-8, cột Đã mở |
| TC-GEO-0436 | Đề trùng bài đã mở thì không kế thừa | Medium | Normal (Success case) | chức năng | tích hợp API | E2E-API | Học sinh An | • BG-3 (HKG-03) Đã mở<br/>• An dán nguyên văn đề HKG-03 vào "Tự học" | • Không có dữ liệu nhập riêng | 1. An xem Gợi ý 1 và Gợi ý 2 | • Tab "Lời giải" có nút "Xem lời giải" theo quy tắc tự học<br/>• Không hiện "Đề này trùng với bài lớp em đang làm. Lời giải mở theo giờ giáo viên đặt." (MSG-GEO-31) | US-GEO-406 · SC3; CL-FN-04; ENG |
| TC-GEO-0437 | Bài của lớp khác không gây kế thừa | Medium | Normal (Success case) | phân quyền | tích hợp API | E2E-API | Học sinh Khánh (9B) | • BG-1 chỉ giao cho 11A1<br/>• Khánh (lớp 9B) dán nguyên văn đề HKG-01 vào "Tự học" | • Không có dữ liệu nhập riêng | 1. Khánh xem Gợi ý 1 và Gợi ý 2 | • Khánh mở được lời giải theo quy tắc tự học | US-GEO-406 · SC4; CL-FN-05; PROVISIONAL D-GEO-026; ENG |
| TC-GEO-0438 | Đề gần trùng cũng kế thừa | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Học sinh An | • BG-1 đang Sắp mở<br/>• An dán đề đã đổi tên điểm S.MNPQ (F-GEO-063)<br/>• Băm văn bản khác đề HKG-01 nhưng chữ ký chuẩn trùng | • Hàng dữ liệu: F-GEO-063 | 1. An xem đủ hai gợi ý<br/>2. Mở tab "Lời giải" | • Tab hiện "Đề này trùng với bài lớp em đang làm. Lời giải mở theo giờ giáo viên đặt." (MSG-GEO-31)<br/>• Không có lời giải | US-GEO-406 · SC5; CL-FN-07; PROVISIONAL D-GEO-150; ENG; ca chặn của cổng phát hành (D-GEO-150, RV-GEO-01): khớp theo chữ ký chuẩn (hình mẫu, tham số quy về a = 1, đại lượng đề hỏi); biến thể ở TC-GEO-0482, 0483, 0484, 0489 |
| TC-GEO-0439 | Gọi thẳng lời giải của bài kế thừa khóa | High | Abnormal (Fail case/Edge case) | dữ liệu trả về | tích hợp API | E2E-API | Học sinh An | • Bài tự học của SC1, An đã xem đủ hai gợi ý, BG-1 vẫn Sắp mở | • Không có dữ liệu nhập riêng | 1. Từ phiên của An, gửi thẳng tới máy chủ yêu cầu lời giải của bài tự học đó | • `FORBIDDEN`<br/>• Dữ liệu trả về không có bước giải và không có "45°" | US-GEO-406 · FC1; CL-FN-07; PROVISIONAL D-GEO-026; ENG |
| TC-GEO-0440 | Giáo viên dời giờ mở | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên mẫu rồi học sinh Khánh | • BG-4 (HKG-04, lớp 9B) đang Sắp mở với R = T0 + 60 giây (F-GEO-043)<br/>• Khánh (lớp 9B) đã dán nguyên văn đề HKG-04 vào "Tự học"<br/>• Tại T0 + 30 giây Giáo viên mẫu đổi giờ BG-4 sang R′ = T0 + 120 giây<br/>• T0 là giờ máy chủ đọc từ tiêu đề `Date` của phản hồi khi giao BG-4; Giáo viên mẫu đổi giờ qua màn đổi giờ như thường | • Hàng dữ liệu: F-GEO-043 | 1. Khánh yêu cầu lời giải của bài tự học tại T0 + 60 giây | • Vẫn khóa ("Đề này trùng với bài lớp em đang làm. Lời giải mở theo giờ giáo viên đặt." (MSG-GEO-31))<br/>• Tại T0 + 122 giây (R′ + 2 giây, bù độ phân giải giây của `Date`) thì xem được | US-GEO-406 · FC2; CL-FN-09; PROVISIONAL D-GEO-026; ENG; BV-5, BV-6; T0 là giờ máy chủ đọc từ tiêu đề `Date`; chờ đồng hồ thật, không dùng giao dịch thử (D-GEO-169, RV-GEO-24); kế thừa khóa kiểm ở mỗi lần xin (D-GEO-151) |
| TC-GEO-0481 | Dán lại đề của bài giao Chưa đặt giờ thì lời giải tự học cũng khóa | High | Abnormal (Fail case/Edge case) | dữ liệu trả về | tích hợp API + thủ công trên thiết bị | E2E-UI | Học sinh An | • BG-2 (đề HKG-02, lớp 11A1) đang Chưa đặt giờ<br/>• An đang ở "Tự học" | • Hàng dữ liệu: F-GEO-032, F-GEO-041<br/>• Giá trị nhập: nguyên văn đề HKG-02 "Cho hình lập phương ABCD.A′B′C′D′ cạnh a. Tính khoảng cách giữa AC và B′D′." | 1. An dán nguyên văn đề HKG-02 vào "Tự học" và dựng hình<br/>2. An xem Gợi ý 1 rồi Gợi ý 2<br/>3. An mở tab "Lời giải"<br/>4. Từ phiên của An, gửi thẳng tới máy chủ yêu cầu lời giải của bài tự học đó | • Tab hiện "Đề này trùng với bài lớp em đang làm. Lời giải mở theo giờ giáo viên đặt." (MSG-GEO-31)<br/>• Không có nút "Xem lời giải"<br/>• Yêu cầu gửi thẳng nhận `FORBIDDEN`<br/>• Dữ liệu trả về không có bước giải của HKG-02 và không có đáp số "a" ở trường đáp số | US-GEO-406 · SC6; US-GEO-406 · FC1; D-GEO-043; CL-FN-07; DT-8, cột Chưa đặt giờ; PROVISIONAL D-GEO-026 |
| TC-GEO-0482 | Đề viết lại với cạnh 2a vẫn kế thừa | High | Normal (Success case) | dữ liệu trả về | tích hợp API + thủ công trên thiết bị | E2E-UI | Học sinh An | • BG-1 đang Sắp mở<br/>• An dán đề F-GEO-077 (đáy cạnh `2a`, SA = `2a`, câu chữ khác HKG-01) | • Hàng dữ liệu: F-GEO-077 | 1. An xem đủ hai gợi ý<br/>2. Mở tab "Lời giải" | • Tab hiện "Đề này trùng với bài lớp em đang làm. Lời giải mở theo giờ giáo viên đặt." (MSG-GEO-31) (bộ tham số quy về a = 1 trùng HKG-01)<br/>• Không có lời giải | US-GEO-406 · SC7; CL-FN-07; PROVISIONAL D-GEO-150; ca gần trùng đổi a thành 2a (D-GEO-150, RV-GEO-01) |
| TC-GEO-0483 | Đề viết lại câu chữ, thêm chữ vẫn kế thừa khóa | High | Abnormal (Fail case/Edge case) | dữ liệu trả về | tích hợp API + thủ công trên thiết bị | E2E-UI | Học sinh An | • BG-1 (HKG-01, lớp 11A1) đang Sắp mở<br/>• An đang ở "Tự học" | • Hàng dữ liệu: F-GEO-234 (`review/QC_test_design.md` §6), F-GEO-031, F-GEO-040 | 1. An dán đề F-GEO-234 vào "Tự học" và dựng hình<br/>2. An xem Gợi ý 1 rồi Gợi ý 2<br/>3. An mở tab "Lời giải"<br/>4. Từ phiên của An, gửi thẳng yêu cầu lời giải của bài tự học đó | • Tab hiện "Đề này trùng với bài lớp em đang làm. Lời giải mở theo giờ giáo viên đặt." (MSG-GEO-31); không có nút "Xem lời giải"<br/>• Yêu cầu gửi thẳng nhận `FORBIDDEN`<br/>• Dữ liệu trả về không có bước giải của HKG-01 và không có "45°" | US-GEO-406 · SC5; CL-FN-07; D-GEO-150; RV-GEO-01; PROVISIONAL D-GEO-150; ENG |
| TC-GEO-0484 | Cùng hình nhưng hỏi đại lượng khác thì không kế thừa khóa | Medium | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Học sinh An | • BG-1 (HKG-01, lớp 11A1) đang Sắp mở<br/>• An đang ở "Tự học" | • Hàng dữ liệu: F-GEO-235 (`review/QC_test_design.md` §6), F-GEO-040 | 1. An dán đề F-GEO-235 vào "Tự học" và dựng hình<br/>2. An xem Gợi ý 1 rồi Gợi ý 2<br/>3. An mở tab "Lời giải" | • Tab không hiện thẻ khóa của bài giao<br/>• Có nút "Xem lời giải" (bài tự học không kế thừa khóa, mở sau hai gợi ý)<br/>• Dữ liệu trả về không có "45°" của HKG-01 | US-GEO-406 · SC5; US-GEO-405 · SC1; CL-FN-07; D-GEO-150; D-GEO-023; cặp đảo của TC-GEO-0482; PROVISIONAL D-GEO-150 |
| TC-GEO-0485 | Dán đề trước, giáo viên giao sau: lần xin kế tiếp bị khóa | High | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Học sinh An rồi Giáo viên mẫu | • Lớp 11A1 chưa có bài giao nào dùng HKG-06<br/>• An dán nguyên văn đề HKG-06 vào "Tự học", dựng hình và xem Gợi ý 1<br/>• Sau đó Giáo viên mẫu giao HKG-06 cho 11A1 với giờ mở để trống (BG-6, F-GEO-048) | • Hàng dữ liệu: F-GEO-048 | 1. An bấm "Xem gợi ý 2"<br/>2. Mở tab "Lời giải" của bài tự học đó | • Gợi ý 2 hiện đúng Gợi ý 2 của đề kho HKG-06 "Bán kính bằng nửa đường chéo của hình lập phương."<br/>• Tab "Lời giải" hiện "Đề này trùng với bài lớp em đang làm. Lời giải mở theo giờ giáo viên đặt." (MSG-GEO-31)<br/>• Không có nút "Xem lời giải" | US-GEO-406 · SC9; CL-FN-09; PROVISIONAL D-GEO-151; dán trước, giao sau (D-GEO-151, RV-GEO-03) |
| TC-GEO-0486 | Đã xem lời giải tự học, sau đó giáo viên giao đúng đề: xin lại thì bị khóa | Medium | Abnormal (Fail case/Edge case) | dữ liệu trả về | tích hợp API + thủ công trên thiết bị | E2E-UI | Học sinh An rồi Giáo viên mẫu | • Lớp 11A1 chưa có bài giao nào dùng HKG-05<br/>• An dán nguyên văn đề HKG-05 vào "Tự học", xem Gợi ý 1, Gợi ý 2 và đã xem lời giải với đáp số "112π cm²"<br/>• Sau đó Giáo viên mẫu giao HKG-05 cho 11A1 với giờ mở T0 + 30 phút (BG-7, F-GEO-049, Sắp mở) | • Hàng dữ liệu: F-GEO-049 | 1. Tại T0 + 1 phút An mở lại tab "Lời giải" của bài tự học đó và gửi thẳng yêu cầu lời giải<br/>2. Rồi tại T0 + 30 phút An mở lại tab "Lời giải" | • Tại T0 + 1 phút tab hiện thẻ khóa "Đề này trùng với bài lớp em đang làm. Lời giải mở theo giờ giáo viên đặt." (MSG-GEO-31), yêu cầu gửi thẳng nhận `FORBIDDEN` và dữ liệu trả về không có bước giải, không có "112π"<br/>• Tại T0 + 30 phút (BG-7 Đã mở) An xem lại được lời giải và "112π cm²" | US-GEO-406 · FC6; CL-FN-09; PROVISIONAL D-GEO-174; D-GEO-174, 151 (RV-GEO-03); case viết tay trước đó chuyển thành case bám tiêu chí, giữ id |
| TC-GEO-0487 | Bài kế thừa khóa dùng gợi ý của đề kho | Medium | Normal (Success case) | dữ liệu trả về | tích hợp API | E2E-API | Học sinh An | • BG-1 đang Sắp mở<br/>• An dán đề F-GEO-063 vào "Tự học" và dựng hình | • Hàng dữ liệu: F-GEO-063, F-GEO-031 | 1. An bấm "Xem gợi ý 1"<br/>2. "Xem gợi ý 2" | • Hai gợi ý hiện nguyên văn Gợi ý 1 và Gợi ý 2 của HKG-01 (F-GEO-031)<br/>• Không có lượt gọi AI với mục đích sinh gợi ý cho bài tự học này | US-GEO-406 · SC10; CL-FN-07; PROVISIONAL D-GEO-155; ENG; D-GEO-155, RV-GEO-06 |
| TC-GEO-0489 | Cùng hình nhưng khác tham số thì không kế thừa | Medium | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Học sinh An | • BG-1 đang Sắp mở<br/>• An dán đề F-GEO-078 (SA = a√3) | • Hàng dữ liệu: F-GEO-078 | 1. An xem đủ hai gợi ý<br/>2. Mở tab "Lời giải" | • Không hiện "Đề này trùng với bài lớp em đang làm. Lời giải mở theo giờ giáo viên đặt." (MSG-GEO-31)<br/>• Có nút "Xem lời giải" theo quy tắc tự học (bộ tham số khác HKG-01 nên không kế thừa) | US-GEO-406 · SC8; CL-FN-07; PROVISIONAL D-GEO-150; cặp đảo của TC-GEO-0482 (khác tham số) |
| TC-GEO-0490 | Dán trước, giao sau: gọi thẳng lời giải bị từ chối | High | Abnormal (Fail case/Edge case) | dữ liệu trả về | tích hợp API | E2E-API | Học sinh An | • Tình huống của SC9 sau khi An đã xem Gợi ý 2<br/>• BG-6 vẫn Chưa đặt giờ | • Không có dữ liệu nhập riêng | 1. Từ phiên của An, gửi thẳng tới máy chủ yêu cầu lời giải của bài tự học HKG-06 | • `FORBIDDEN`<br/>• Dữ liệu trả về không có bước giải và không có "a√3/2" | US-GEO-406 · FC3; CL-FN-07; PROVISIONAL D-GEO-151; ENG; D-GEO-151 |
| TC-GEO-0491 | Dữ liệu gợi ý của bài kế thừa khóa không lộ | High | Abnormal (Fail case/Edge case) | dữ liệu trả về | tích hợp API | E2E-API | Học sinh An | • Tình huống của SC10 | • Hàng dữ liệu: F-GEO-037 | 1. Ghi lại toàn bộ dữ liệu trả về của hai lần xin gợi ý | • Không phản hồi nào chứa "45°", "45 độ", "π/4", "tan SDA = 1" hay nội dung một bước của F-GEO-037 | US-GEO-406 · FC4; CL-FN-07; PROVISIONAL D-GEO-155; ENG; D-GEO-155 |
| TC-GEO-0492 | Đề không dựng được bằng hình mẫu không có lời giải tự học | High | Abnormal (Fail case/Edge case) | dữ liệu trả về | tích hợp API + thủ công trên thiết bị | E2E-UI | Học sinh An | • An dán đề lạ F-GEO-061 (bát diện đều; hình dùng tọa độ AI kèm MSG-GEO-16, không khớp hình mẫu nào nên không có chữ ký chuẩn) | • Hàng dữ liệu: F-GEO-061 | 1. An xem Gợi ý 1, Gợi ý 2<br/>2. Mở tab "Lời giải"<br/>3. Sau đó gửi thẳng yêu cầu lời giải của bài tự học đó | • Tab hiện "Đề này chưa dựng được bằng hình mẫu nên chưa có lời giải tự học. Em dùng gợi ý nhé." (MSG-GEO-58), không có nút "Xem lời giải"<br/>• Yêu cầu gửi thẳng nhận `FORBIDDEN` và không có bước giải hay "a√6/3" | US-GEO-406 · FC5; CL-FN-07; PROVISIONAL D-GEO-177; D-GEO-150 |

### US-GEO-407 — Xem lời giải đồng bộ hình: bấm bước thì hình tô màu

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0441 | Bấm một bước, hình tô đúng đối tượng | High | Normal (Success case) | chức năng | thủ công trên thiết bị | E2E-UI | Học sinh An, iPad | • BG-1 Đã mở (sau "Mở ngay")<br/>• An ở tab "Lời giải" có ba bước của HKG-01 (F-GEO-037) | • Hàng dữ liệu: F-GEO-037 | 1. An bấm bước 2 "Xác định góc cần tìm" | • Bước 2 nổi lên<br/>• Hình tô SD, AD và góc SDA<br/>• Hiện dòng "Đang tô trên hình: SD, AD, góc SDA" | US-GEO-407 · SC1; CL-FN-01 |
| TC-GEO-0442 | Đổi bước thì đổi phần tô | Medium | Normal (Success case) | chức năng | thủ công trên thiết bị | E2E-UI | Học sinh An, iPad | • Bước 2 đang chọn | • Không có dữ liệu nhập riêng | 1. An bấm bước 1 "Tìm hình chiếu của SD" | • Hình tô SD và AD<br/>• Góc SDA hết tô<br/>• Dòng "Đang tô trên hình: SD, AD" | US-GEO-407 · SC2; CL-FN-04 |
| TC-GEO-0443 | "Bước sau", "Bước trước" | Medium | Normal (Success case) | chức năng | thủ công trên thiết bị | E2E-UI | Học sinh An, iPad | • Bước 1 đang chọn | • Không có dữ liệu nhập riêng | 1. An bấm "Bước sau" hai lần<br/>2. "Bước trước" một lần | • Bước đang chọn lần lượt là 2, 3, rồi 2<br/>• Ở bước 3 nút "Bước sau" không bấm được | US-GEO-407 · SC3; CL-FN-04 |
| TC-GEO-0444 | Nhãn của lời giải | Medium | Normal (Success case) | chức năng | thủ công trên thiết bị | E2E-UI | Học sinh An | • Như SC1 | • Không có dữ liệu nhập riêng | 1. An xem đầu tab "Lời giải" | • Có nhãn "Đã kiểm chứng" và nhãn "AI soạn"<br/>• Không có nhãn "Giáo viên đã duyệt" | US-GEO-407 · SC4; CL-UI-02; PROVISIONAL D-GEO-028 |
| TC-GEO-0445 | Xem lời giải được ghi nhận | Medium | Normal (Success case) | chức năng | tích hợp API | E2E-API | Học sinh An | • An có tiến độ 2/3 trên BG-1<br/>• BG-1 vừa Đã mở | • Không có dữ liệu nhập riêng | 1. An mở tab "Lời giải" lần đầu | • Tiến độ thành 3/3<br/>• "Bài của em" ghi "Đã xem lời giải" và nút "Xem lại"<br/>• Có đúng một sự kiện mở lời giải dù An mở tab nhiều lần | US-GEO-407 · SC5; CL-FN-13; ENG |
| TC-GEO-0446 | Trên iPhone | High | Normal (Success case) | thiết bị | thủ công trên thiết bị | E2E-UI | Học sinh An, iPhone | • BG-1 Đã mở<br/>• An dùng iPhone màn dọc | • Không có dữ liệu nhập riêng | 1. Mở tab "Lời giải" ở bảng dưới và bấm bước 2 | • Hình phía trên tô SD, AD, góc SDA<br/>• Bảng dưới không che hết hình | US-GEO-407 · SC6; CL-UI-01; PROVISIONAL KG-09 |
| TC-GEO-0447 | Bước không có đối tượng để tô | Low | Abnormal (Fail case/Edge case) | chức năng | thủ công trên thiết bị | E2E-UI | Học sinh An | • Một bài Đã mở có lời giải HKG-05, bước 1 "Áp dụng công thức" không gắn đối tượng nào | • Không có dữ liệu nhập riêng | 1. Học sinh bấm bước đó | • Hình không tô gì<br/>• Không hiện dòng "Đang tô trên hình"<br/>• Không có lỗi | US-GEO-407 · FC1; CL-FN-03 |
| TC-GEO-0448 | Chuyển bước nhanh | Medium | Abnormal (Fail case/Edge case) | chức năng | thủ công trên thiết bị | E2E-UI | Học sinh An | • Như SC1 | • Không có dữ liệu nhập riêng | 1. An bấm bước 1, 2, 3 liên tiếp trong chưa tới một giây | • Khi dừng, hình chỉ mang phần tô của bước 3<br/>• Không còn phần tô sót lại của bước 1 hoặc 2 | US-GEO-407 · FC2; CL-FN-10 |

### US-GEO-408 — Đường vẽ thêm của từng bước hiện dần; "Vì sao nghĩ ra bước này?"

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0449 | Phần vẽ thêm hiện dần theo bước | High | Normal (Success case) | chức năng | thủ công trên thiết bị | E2E-UI | Học sinh An, iPad | • BG-1 Đã mở<br/>• An ở bước 1 của lời giải HKG-01: hình có đường phụ AD nổi bật | • Không có dữ liệu nhập riêng | 1. An bấm "Bước sau" tới bước 2,<br/>2. Tới bước 3 | • Ở bước 2 hình có thêm ký hiệu góc vuông tại A<br/>• Ở bước 3 có thêm cung góc tại D và số "45°" | US-GEO-408 · SC1; CL-FN-04 |
| TC-GEO-0450 | "Vì sao nghĩ ra bước này?" | Medium | Normal (Success case) | chức năng | thủ công trên thiết bị | E2E-UI | Học sinh An | • An chọn bước 2 | • Hàng dữ liệu: F-GEO-037 | 1. Xem thẻ của bước 2 | • Có khối "Vì sao nghĩ ra bước này?" với câu "Có hình chiếu rồi thì góc cần tìm nằm trong một tam giác vuông." (F-GEO-037) | US-GEO-408 · SC2; CL-FN-01 |
| TC-GEO-0451 | Quay lại bước trước thì phần vẽ thêm bớt đi | Medium | Normal (Success case) | chức năng | thủ công trên thiết bị | E2E-UI | Học sinh An | • An đang ở bước 3 (hình có cung góc tại D và "45°") | • Không có dữ liệu nhập riêng | 1. An bấm bước 1 | • Cung góc tại D, số "45°" và ký hiệu góc vuông tại A biến mất<br/>• Đường phụ AD còn | US-GEO-408 · SC3; CL-FN-04 |
| TC-GEO-0452 | Nhảy thẳng tới bước cuối | Medium | Normal (Success case) | chức năng | thủ công trên thiết bị | E2E-UI | Học sinh An | • An vừa mở tab "Lời giải", bước 1 đang chọn | • Không có dữ liệu nhập riêng | 1. An bấm thẳng bước 3 | • Hình có đủ phần vẽ thêm của bước 1, 2 và 3 | US-GEO-408 · SC4; CL-FN-04 |
| TC-GEO-0453 | Bước không có phần vẽ thêm | Low | Abnormal (Fail case/Edge case) | chức năng | thủ công trên thiết bị | E2E-UI | Học sinh An | • Lời giải HKG-03, bước 2 "Tính thể tích" không có phần vẽ thêm | • Không có dữ liệu nhập riêng | 1. Học sinh chuyển từ bước 1 sang bước 2 | • Hình giữ nguyên như ở bước 1<br/>• Khối "Vì sao nghĩ ra bước này?" đổi sang câu của bước 2 | US-GEO-408 · FC1; CL-FN-03 |

### US-GEO-409 — Hỏi đáp trong ngữ cảnh bài, có nguồn

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0454 | Câu trả lời có nguồn "Giáo án mẫu" | High | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Học sinh An | • An đang làm BG-1 (Sắp mở), tab "Hỏi đáp", placeholder "Hỏi về bài này…" | • Hàng dữ liệu: F-GEO-055 | 1. An gửi "Vì sao A là hình chiếu của S?" (F-GEO-055) | • Câu trả lời giải thích khái niệm hình chiếu vuông góc và kết thúc bằng "Nguồn: Giáo án mẫu" kèm tên bài giảng "Hình chiếu vuông góc" bấm được | US-GEO-409 · SC1; CL-FN-01; PROVISIONAL D-GEO-030; AI giả lập: chỉ kiểm có dòng nguồn và liên kết bài giảng, không chấm nội dung giải thích |
| TC-GEO-0455 | Câu trả lời có nguồn "AI soạn" | Medium | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Học sinh Dũng | • Dũng đang làm BG-1 | • Hàng dữ liệu: F-GEO-058 | 1. Dũng gửi "Tan của một góc là gì?" (F-GEO-058) | • Câu trả lời kết thúc bằng "Nguồn: AI soạn" kèm tên bài giảng "Tỉ số lượng giác trong tam giác vuông" | US-GEO-409 · SC2; CL-FN-12; PROVISIONAL D-GEO-029 |
| TC-GEO-0456 | Câu hỏi ngoài giáo trình | High | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Học sinh An | • An đang làm BG-1 | • Hàng dữ liệu: F-GEO-059 | 1. An gửi "Thủ đô của nước Pháp là gì?" (F-GEO-059) | • Câu trả lời là MSG-GEO-36<br/>• Không có dòng "Nguồn" | US-GEO-409 · SC3; CL-FN-01 |
| TC-GEO-0457 | Màn chờ khi trợ lý đang trả lời | Medium | Normal (Success case) | chức năng | thủ công trên thiết bị | E2E-UI | Học sinh An | • Nhà cung cấp giả lập trả lời sau 5 giây | • Hàng dữ liệu: F-GEO-055 | 1. An gửi câu hỏi F-GEO-055 | • Trong 5 giây đó hiện "Trợ lý đang soạn câu trả lời…" (MSG-GEO-15), ô nhập không gửi được câu thứ hai<br/>• Sau đó câu trả lời hiện nguyên vẹn một lần | US-GEO-409 · SC4; CL-FN-14 |
| TC-GEO-0458 | Câu hỏi được ghi nhận | Medium | Normal (Success case) | chức năng | tích hợp API | E2E-API | Học sinh An | • Dữ liệu lớp như F-GEO-050…054 (thẻ "câu hỏi" = 7)<br/>• Chi chưa hỏi câu nào | • Hàng dữ liệu: F-GEO-050…054, F-GEO-055 | 1. Chi gửi câu F-GEO-055 trên BG-1 và nhận được câu trả lời | • Theo dõi lớp của BG-1 hiện thẻ "câu hỏi" = 8 và dòng Chi ghi "1 câu"<br/>• Câu hỏi có trong nhật ký hỏi đáp của Chi | US-GEO-409 · SC5; CL-FN-13; ENG |
| TC-GEO-0459 | Nguồn nêu ra luôn mở được | Medium | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Học sinh An | • Câu trả lời của SC1 | • Hàng dữ liệu: F-GEO-090, F-GEO-091<br/>• Giá trị nhập: "Nguồn" | 1. An bấm tên bài giảng ở dòng "Nguồn" | • Hộp bài giảng "Hình chiếu vuông góc" mở (US-GEO-411)<br/>• Không có câu trả lời nào mang tên một bài giảng không có trong kho F-GEO-090, F-GEO-091 | US-GEO-409 · SC6; CL-FN-01 |
| TC-GEO-0460 | Trợ lý lỗi | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Học sinh An | • Nhà cung cấp giả lập ở chế độ trả lỗi | • Hàng dữ liệu: F-GEO-055 | 1. An gửi câu F-GEO-055 | • Hiện "Trợ lý đang bận. Em thử lại sau ít phút nhé." (MSG-GEO-14) và "Thử lại"<br/>• Câu hỏi của An vẫn hiện trong khung<br/>• Không có sự kiện hỏi nào được tính cho tới khi có câu trả lời | US-GEO-409 · FC1; CL-FN-14; PROVISIONAL D-GEO-072 |
| TC-GEO-0461 | Câu hỏi rỗng hoặc quá dài | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Học sinh An | • Tab "Hỏi đáp" của BG-1 | • Không có dữ liệu nhập riêng | 1. An gửi câu rỗng,<br/>2. Gửi câu 1001 ký tự | • Mỗi lần hiện "Câu hỏi cần từ 1 đến 1000 ký tự." (MSG-GEO-37)<br/>• Không có lượt gọi AI<br/>• Không có sự kiện hỏi | US-GEO-409 · FC2; CL-FN-02; PROVISIONAL QA-GEO-01; ENG |
| TC-GEO-0462 | Giáo viên đã tắt hỏi đáp | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API + thủ công trên thiết bị | E2E-UI | Học sinh Khánh (9B) | • BG-5 có "Hỏi đáp (không nêu đáp số)" tắt (F-GEO-044)<br/>• Khánh mở BG-5 | • Hàng dữ liệu: F-GEO-044 | 1. Khánh mở tab "Hỏi đáp",<br/>2. Gửi thẳng một câu hỏi tới máy chủ | • Tab hiện "Giáo viên đã tắt hỏi đáp cho bài này." (MSG-GEO-33), không có ô nhập<br/>• Yêu cầu gửi thẳng nhận `FORBIDDEN`<br/>• Không có lượt gọi AI | US-GEO-409 · FC3; CL-FN-05; PROVISIONAL D-GEO-053 |

### US-GEO-410 — Nhật ký hỏi đáp lọc theo bài

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0463 | Xem nhật ký của mình | Medium | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Học sinh An | • An đã hỏi ba câu trên BG-1 (F-GEO-055, 056, 057) | • Hàng dữ liệu: F-GEO-055 | 1. An bấm "Lịch sử" ở thanh bên | • Danh sách có ba câu đó, câu mới nhất ở trên, mỗi dòng ghi câu hỏi, tên bài "Hình chóp S.ABCD: tính góc giữa SD và mặt đáy" và giờ hỏi | US-GEO-410 · SC1; CL-FN-01; PROVISIONAL D-GEO-078 |
| TC-GEO-0464 | Lọc theo bài | Medium | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Học sinh An | • An có ba câu trên BG-1 và một câu trên BG-3 | • Không có dữ liệu nhập riêng | 1. An chọn lọc theo bài BG-1 | • Chỉ còn ba câu của BG-1 | US-GEO-410 · SC2; CL-FN-01; hồi quy lỗi đã biết của bản cũ (REQ-GEO-36) |
| TC-GEO-0465 | Mở lại câu trả lời đã lưu | Low | Normal (Success case) | chức năng | thủ công trên thiết bị | E2E-UI | Học sinh An | • Danh sách của SC1<br/>• Số bản ghi lượt gọi AI là n | • Không có dữ liệu nhập riêng | 1. An bấm dòng "Vì sao A là hình chiếu của S?" | • Câu trả lời đã nhận hôm trước hiện ra cùng dòng "Nguồn"<br/>• Số bản ghi lượt gọi AI vẫn là n | US-GEO-410 · SC3; CL-FN-01; PROVISIONAL D-GEO-159 |
| TC-GEO-0466 | Chỉ thấy câu của mình | High | Normal (Success case) | phân quyền | tích hợp API | E2E-API | Học sinh An và Bình | • Bình có hai câu trên BG-1 | • Không có dữ liệu nhập riêng | 1. An mở "Lịch sử" và lọc theo BG-1 | • Không có câu nào của Bình | US-GEO-410 · SC4; CL-FN-05; ENG |
| TC-GEO-0467 | Bài chưa có câu hỏi | Low | Abnormal (Fail case/Edge case) | chức năng | thủ công trên thiết bị | E2E-UI | Học sinh An | • An chưa hỏi câu nào trên BG-2 | • Không có dữ liệu nhập riêng | 1. An lọc theo bài BG-2 | • Hiện "Em chưa hỏi câu nào ở bài này." (MSG-GEO-38) | US-GEO-410 · FC1; CL-FN-03 |
| TC-GEO-0468 | Con trỏ hỏng | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Học sinh An | • Phiên của An | • Hàng dữ liệu: F-GEO-084<br/>• Giá trị nhập: `khong-phai-con-tro` | 1. Gọi danh sách nhật ký với con trỏ `khong-phai-con-tro` (F-GEO-084) | • `INVALID_CURSOR`<br/>• Màn hiện "Danh sách đã thay đổi. Vui lòng tải lại từ đầu." (MSG-GEO-05)<br/>• Không có dòng nào được trả | US-GEO-410 · FC2; CL-FN-08; ENG |
| TC-GEO-0493 | Câu trả lời từng bị chặn mở lại vẫn là câu thay thế | High | Normal (Success case) | dữ liệu trả về | tích hợp API + thủ công trên thiết bị | E2E-UI | Học sinh Bình | • BG-1 Sắp mở<br/>• Bình đã gửi F-GEO-056 và nhà cung cấp giả lập trả "Góc giữa SD và mặt đáy là 45°." (F-GEO-114), câu này bị chặn và Bình đã nhận MSG-GEO-35 (US-GEO-609-FC1) | • Hàng dữ liệu: F-GEO-056, F-GEO-114 | 1. Bình mở "Lịch sử" và bấm dòng "Góc cần tìm là góc nào trên hình?" | • Câu trả lời hiện ra là câu "Lời giải đang khóa nên mình chưa nêu đáp số hay các bước giải. Em xem lại gợi ý nhé: {gợi ý cao nhất em đã xem}" (MSG-GEO-35) Bình đã nhận<br/>• Dữ liệu trả về không chứa "45°" và không chứa câu gốc của AI | US-GEO-410 · SC5; CL-FN-07; PROVISIONAL D-GEO-159; D-GEO-159 |

### US-GEO-411 — Từ khóa bấm được mở bài giảng có nguồn và nhãn

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0469 | Bấm từ khóa trong gợi ý | High | Normal (Success case) | chức năng | thủ công trên thiết bị | E2E-UI | Học sinh An | • An đang xem Gợi ý 1 của BG-1<br/>• Chữ "hình chiếu" được gạch chân chấm (F-GEO-092) | • Hàng dữ liệu: F-GEO-092 | 1. An bấm "hình chiếu" | • Hộp bài giảng "Hình chiếu vuông góc" mở với nội dung và dòng "Nguồn: Giáo án mẫu" | US-GEO-411 · SC1; CL-FN-01; PROVISIONAL D-GEO-030 |
| TC-GEO-0470 | Bài giảng do AI viết mang nhãn chờ duyệt | Medium | Normal (Success case) | chức năng | thủ công trên thiết bị | E2E-UI | Học sinh An | • Câu trả lời của trợ lý cho Dũng có từ khóa "Tỉ số lượng giác trong tam giác vuông" | • Không có dữ liệu nhập riêng | 1. Dũng bấm từ khóa đó | • Hộp bài giảng hiện "Nguồn: AI soạn" và nhãn "AI soạn · chờ giáo viên duyệt" | US-GEO-411 · SC2; CL-UI-02; PROVISIONAL D-GEO-029 |
| TC-GEO-0471 | Từ khóa trong lời giải | Medium | Normal (Success case) | chức năng | thủ công trên thiết bị | E2E-UI | Học sinh An | • BG-1 Đã mở<br/>• Bước 1 của lời giải có chữ "hình chiếu" | • Không có dữ liệu nhập riêng | 1. An bấm chữ đó | • Cùng hộp bài giảng "Hình chiếu vuông góc" như ở SC1 | US-GEO-411 · SC3; CL-FN-01 |
| TC-GEO-0472 | Đóng hộp bài giảng | Low | Normal (Success case) | chức năng | thủ công trên thiết bị | E2E-UI | Học sinh An | • Hộp bài giảng đang mở từ Gợi ý 1<br/>• An đã thêm điểm M trên hình | • Không có dữ liệu nhập riêng | 1. An đóng hộp | • An trở về tab "Gợi ý" đúng chỗ đang đọc<br/>• Điểm M và tiến độ không đổi | US-GEO-411 · SC4; CL-FN-01 |
| TC-GEO-0473 | Cụm từ không có bài giảng | Medium | Abnormal (Fail case/Edge case) | chức năng | thủ công trên thiết bị | E2E-UI | Học sinh An | • Một câu trả lời có chữ "thiết diện" (F-GEO-093), không thuộc kho bài giảng | • Hàng dữ liệu: F-GEO-093 | 1. An xem câu đó,<br/>2. Gọi thẳng máy chủ xin bài giảng của "thiết diện" | • Chữ "thiết diện" không gạch chân, không bấm được<br/>• Yêu cầu gọi thẳng nhận `NOT_FOUND` ("Chưa có bài giảng cho từ khóa này." (MSG-GEO-39)) | US-GEO-411 · FC1; CL-FN-03 |
| TC-GEO-0474 | Bài giảng không lộ đáp số của bài đang khóa | High | Abnormal (Fail case/Edge case) | dữ liệu trả về | tích hợp API | E2E-API | Học sinh An | • BG-1 đang Sắp mở | • Hàng dữ liệu: F-GEO-037 | 1. An mở bài giảng "Góc giữa đường thẳng và mặt phẳng" từ Gợi ý 1 và ghi lại dữ liệu trả về | • Nội dung không nhắc đề HKG-01 và không có bước nào của F-GEO-037 | US-GEO-411 · FC2; CL-FN-07; PROVISIONAL D-GEO-027; ENG |
| TC-GEO-0475 | Không dùng nhãn "Giáo viên đã duyệt" | Medium | Abnormal (Fail case/Edge case) | chức năng | thủ công trên thiết bị | E2E-UI | Học sinh An | • Bảy bài giảng nguồn "Giáo án mẫu" (F-GEO-090) | • Hàng dữ liệu: F-GEO-090 | 1. Mở lần lượt từng bài | • Mỗi bài có "Nguồn: Giáo án mẫu"<br/>• Không bài nào hiện nhãn "Giáo viên đã duyệt" | US-GEO-411 · FC3; CL-UI-02; PROVISIONAL D-GEO-111 |

### US-GEO-412 — Dữ liệu: kho bài giảng mẫu

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0476 | Đủ 10 bài giảng với nguồn và nhãn | Medium | Normal (Success case) | chức năng | tích hợp CSDL | IT | Kỹ thuật | • Dữ liệu kho bài giảng mẫu | • Hàng dữ liệu: F-GEO-090, F-GEO-091 | 1. Nạp | • Có đúng 10 bài<br/>• 7 bài F-GEO-090 mang nguồn "Giáo án mẫu" và cờ "chưa có người soát"<br/>• 3 bài F-GEO-091 mang nguồn "AI soạn" và nhãn "AI soạn · chờ giáo viên duyệt" | US-GEO-412 · SC1; CL-FN-01; PROVISIONAL D-GEO-137; ENG |
| TC-GEO-0477 | Mỗi bài có từ khóa trỏ tới | Medium | Normal (Success case) | chức năng | tích hợp CSDL | IT | Kỹ thuật | • Kho đã nạp | • Hàng dữ liệu: F-GEO-092 | 1. Tra từ khóa "hình chiếu" và "Góc giữa đường thẳng và mặt phẳng" (F-GEO-092) | • Lần lượt ra bài "Hình chiếu vuông góc" và bài "Góc giữa đường thẳng và mặt phẳng"<br/>• Không bài giảng nào thiếu từ khóa | US-GEO-412 · SC2; CL-FN-01; ENG |
| TC-GEO-0478 | Mọi trường đọc được | Medium | Normal (Success case) | phân quyền | tích hợp API | E2E-API | Học sinh An của lớp 9C (trường khác) | • Học sinh An lớp 9C của "THCS Mẫu C" | • Không có dữ liệu nhập riêng | 1. Bấm một từ khóa trong gợi ý của BG-C | • Mở được cùng bài giảng như học sinh "THPT Mẫu A" | US-GEO-412 · SC3; CL-FN-06; PROVISIONAL D-GEO-076; ENG |
| TC-GEO-0479 | Bài giảng chứa lời giải của một đề trong kho | High | Abnormal (Fail case/Edge case) | dữ liệu trả về | tích hợp CSDL | IT | Kỹ thuật | • Kho đã nạp và sáu đề F-GEO-031…036 | • Hàng dữ liệu: F-GEO-031…036, F-GEO-037…039 | 1. Rà nội dung 10 bài giảng | • Không bài nào chứa nguyên văn một đề bài của kho hay một bước giải F-GEO-037…039 | US-GEO-412 · FC1; CL-FN-07; PROVISIONAL D-GEO-027; ENG |
| TC-GEO-0480 | Bài giảng "Giáo án mẫu" thiếu tham chiếu nguồn | Low | Abnormal (Fail case/Edge case) | chức năng | tích hợp CSDL | IT | Kỹ thuật | • Dữ liệu nạp có một bài nguồn "Giáo án mẫu" không ghi tên tài liệu và mục | • Không có dữ liệu nhập riêng | 1. Nạp | • Bài đó bị từ chối, kết quả nạp nêu tên bài<br/>• Các bài khác nạp bình thường | US-GEO-412 · FC2; CL-FN-03; ENG |
| TC-GEO-0494 | Cờ chờ soát hiện cho học sinh và giáo viên | Medium | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Học sinh An rồi Giáo viên mẫu | • Kho đã nạp<br/>• Bài "Hình chiếu vuông góc" (F-GEO-090) mang cờ "chưa có người soát" | • Hàng dữ liệu: F-GEO-090, F-GEO-091 | 1. An bấm từ khóa "hình chiếu" trong Gợi ý 1 của BG-1<br/>2. Giáo viên mẫu bấm cùng từ khóa trong Gợi ý 1 ở trang chi tiết HKG-01 | • Ở cả hai hộp bài giảng có dòng "Nguồn: Giáo án mẫu" và dòng "Nội dung đang chờ giáo viên soát"<br/>• Ba bài F-GEO-091 không hiện dòng này | US-GEO-412 · SC4; CL-UI-02; PROVISIONAL D-GEO-181; D-GEO-181 |

## 3. Đối chiếu hai chiều AC ↔ TC

### 3.1 Chiều xuôi: tiêu chí → case (93 / 93 tiêu chí còn hiệu lực có case, 100.0%)

| User story | Criterion ID | Kind | What it checks | Status | Test cases |
|---|---|---|---|---|---|
| US-GEO-401 | US-GEO-401-SC1 | Success | Dán đề và nhận hình | Active · PROVISIONAL | TC-GEO-0400 |
| US-GEO-401 | US-GEO-401-SC2 | Success | Màn chờ rõ ràng | Active | TC-GEO-0401 |
| US-GEO-401 | US-GEO-401-SC3 | Success | Bài tự học mới bắt đầu ở mức 0 | Active · PROVISIONAL | TC-GEO-0402 |
| US-GEO-401 | US-GEO-401-SC4 | Success | Ghi thời gian từ gửi đề tới có hình | Active · PROVISIONAL | TC-GEO-0403 |
| US-GEO-401 | US-GEO-401-FC1 | Failure | Đề rỗng | Active | TC-GEO-0404 |
| US-GEO-401 | US-GEO-401-FC2 | Failure | Đề 8001 ký tự | Active · PROVISIONAL | TC-GEO-0405 |
| US-GEO-401 | US-GEO-401-FC3 | Failure | Trợ lý lỗi khi dựng hình | Active · PROVISIONAL | TC-GEO-0406 |
| US-GEO-401 | US-GEO-401-FC4 | Failure | Đề không phải hình học | Active | TC-GEO-0407 |
| US-GEO-401 | US-GEO-401-FC5 | Failure | Bạn khác không thấy bài tự học của mình | Active · PROVISIONAL | TC-GEO-0408 |
| US-GEO-402 | US-GEO-402-SC1 | Success | Ảnh rõ được đọc thành chữ để sửa | Active · PROVISIONAL | TC-GEO-0409 |
| US-GEO-402 | US-GEO-402-SC2 | Success | Sửa chữ rồi dựng hình | Active · PROVISIONAL | TC-GEO-0410 |
| US-GEO-402 | US-GEO-402-SC3 | Success | Chụp trực tiếp bằng máy ảnh của iPad, iPhone | Active | TC-GEO-0411 |
| US-GEO-402 | US-GEO-402-FC1 | Failure | Ảnh mờ | Active | TC-GEO-0412 |
| US-GEO-402 | US-GEO-402-FC2 | Failure | Ảnh vượt 1 MB | Active · PROVISIONAL | TC-GEO-0413 |
| US-GEO-402 | US-GEO-402-FC3 | Failure | Trợ lý lỗi khi đọc ảnh | Active · PROVISIONAL | TC-GEO-0414 |
| US-GEO-403 | US-GEO-403-SC1 | Success | Vào từ "Tự khám phá" của Bài của em | Active | TC-GEO-0415 |
| US-GEO-403 | US-GEO-403-SC2 | Success | "Mở chế độ tự học" | Active | TC-GEO-0416 |
| US-GEO-403 | US-GEO-403-SC3 | Success | Màn khám phá không có gợi ý, lời giải, hỏi đáp | Active · PROVISIONAL | TC-GEO-0417 |
| US-GEO-403 | US-GEO-403-FC1 | Failure | Chưa vào lớp | Active · PROVISIONAL | TC-GEO-0418 |
| US-GEO-404 | US-GEO-404-SC1 | Success | Xem Gợi ý 1 | Active | TC-GEO-0419 |
| US-GEO-404 | US-GEO-404-SC2 | Success | Xem Gợi ý 2 | Active · PROVISIONAL | TC-GEO-0420 |
| US-GEO-404 | US-GEO-404-SC3 | Success | Giáo viên thấy mức gợi ý vừa dùng | Active | TC-GEO-0421 |
| US-GEO-404 | US-GEO-404-SC4 | Success | Xem lại gợi ý không làm tăng tiến độ | Active | TC-GEO-0422 |
| US-GEO-404 | US-GEO-404-SC5 | Success | Gợi ý trên bài tự học | Active | TC-GEO-0423 |
| US-GEO-404 | US-GEO-404-FC1 | Failure | Xin Gợi ý 2 khi chưa xem Gợi ý 1 | Active · PROVISIONAL | TC-GEO-0424 |
| US-GEO-404 | US-GEO-404-FC2 | Failure | Giáo viên đã tắt gợi ý | Active · PROVISIONAL | TC-GEO-0425 |
| US-GEO-404 | US-GEO-404-FC3 | Failure | Gợi ý chưa xin thì chưa được gửi xuống | Active · PROVISIONAL | TC-GEO-0426 |
| US-GEO-404 | US-GEO-404-FC4 | Failure | Mất mạng lúc bấm | Active | TC-GEO-0427 |
| US-GEO-404 | US-GEO-404-FC5 | Failure | Phản hồi gợi ý không cho lưu đệm | Active · PROVISIONAL | TC-GEO-0488 |
| US-GEO-405 | US-GEO-405-SC1 | Success | Đủ hai gợi ý thì mở được lời giải | Active · PROVISIONAL | TC-GEO-0428, TC-GEO-0484 |
| US-GEO-405 | US-GEO-405-SC2 | Success | Đếm lượt gợi ý trước khi mở lời giải | Active | TC-GEO-0429 |
| US-GEO-405 | US-GEO-405-FC1 | Failure | Chưa xem gợi ý nào | Active · PROVISIONAL | TC-GEO-0430 |
| US-GEO-405 | US-GEO-405-FC2 | Failure | Mới xem Gợi ý 1 mà gọi thẳng lời giải | Active · PROVISIONAL | TC-GEO-0431 |
| US-GEO-405 | US-GEO-405-FC3 | Failure | Trang bài tự học không mang sẵn lời giải | Active | TC-GEO-0432 |
| US-GEO-405 | US-GEO-405-FC4 | Failure | Hỏi đáp không mở trước lời giải của bài tự học | Active · PROVISIONAL | TC-GEO-0433 |
| US-GEO-406 | US-GEO-406-SC1 | Success | Dán lại đề đang khóa thì bị khóa theo bài giao | Active · PROVISIONAL | TC-GEO-0434 |
| US-GEO-406 | US-GEO-406-SC2 | Success | Bài giao mở thì bài tự học kế thừa cũng mở | Active · PROVISIONAL | TC-GEO-0435 |
| US-GEO-406 | US-GEO-406-SC3 | Success | Đề trùng bài đã mở thì không kế thừa | Active | TC-GEO-0436 |
| US-GEO-406 | US-GEO-406-SC4 | Success | Bài của lớp khác không gây kế thừa | Active · PROVISIONAL | TC-GEO-0437 |
| US-GEO-406 | US-GEO-406-SC5 | Success | Đề gần trùng cũng kế thừa | Active · PROVISIONAL | TC-GEO-0438, TC-GEO-0483, TC-GEO-0484 |
| US-GEO-406 | US-GEO-406-SC6 | Success | Kế thừa khóa của bài Chưa đặt giờ | Active · PROVISIONAL | TC-GEO-0481 |
| US-GEO-406 | US-GEO-406-FC1 | Failure | Gọi thẳng lời giải của bài kế thừa khóa | Active · PROVISIONAL | TC-GEO-0439, TC-GEO-0481 |
| US-GEO-406 | US-GEO-406-FC2 | Failure | Giáo viên dời giờ mở | Active · PROVISIONAL | TC-GEO-0440 |
| US-GEO-406 | US-GEO-406-SC7 | Success | Đề viết lại với cạnh 2a vẫn kế thừa | Active · PROVISIONAL | TC-GEO-0482 |
| US-GEO-406 | US-GEO-406-SC8 | Success | Cùng hình nhưng khác tham số thì không kế thừa | Active · PROVISIONAL | TC-GEO-0489 |
| US-GEO-406 | US-GEO-406-SC9 | Success | Dán đề trước, giáo viên giao sau: lần xin kế tiếp bị khóa | Active · PROVISIONAL | TC-GEO-0485 |
| US-GEO-406 | US-GEO-406-SC10 | Success | Bài kế thừa khóa dùng gợi ý của đề kho | Active · PROVISIONAL | TC-GEO-0487 |
| US-GEO-406 | US-GEO-406-FC3 | Failure | Dán trước, giao sau: gọi thẳng lời giải bị từ chối | Active · PROVISIONAL | TC-GEO-0490 |
| US-GEO-406 | US-GEO-406-FC4 | Failure | Dữ liệu gợi ý của bài kế thừa khóa không lộ | Active · PROVISIONAL | TC-GEO-0491 |
| US-GEO-406 | US-GEO-406-FC5 | Failure | Đề không dựng được bằng hình mẫu không có lời giải tự học | Active · PROVISIONAL | TC-GEO-0492 |
| US-GEO-406 | US-GEO-406-FC6 | Failure | Đã xem lời giải tự học, sau đó giáo viên giao đúng đề: xin lại thì bị khóa | Active · PROVISIONAL | TC-GEO-0486 |
| US-GEO-407 | US-GEO-407-SC1 | Success | Bấm một bước, hình tô đúng đối tượng | Active | TC-GEO-0441 |
| US-GEO-407 | US-GEO-407-SC2 | Success | Đổi bước thì đổi phần tô | Active | TC-GEO-0442 |
| US-GEO-407 | US-GEO-407-SC3 | Success | "Bước sau", "Bước trước" | Active | TC-GEO-0443 |
| US-GEO-407 | US-GEO-407-SC4 | Success | Nhãn của lời giải | Active · PROVISIONAL | TC-GEO-0444 |
| US-GEO-407 | US-GEO-407-SC5 | Success | Xem lời giải được ghi nhận | Active | TC-GEO-0445 |
| US-GEO-407 | US-GEO-407-SC6 | Success | Trên iPhone | Active | TC-GEO-0446 |
| US-GEO-407 | US-GEO-407-FC1 | Failure | Bước không có đối tượng để tô | Active | TC-GEO-0447 |
| US-GEO-407 | US-GEO-407-FC2 | Failure | Chuyển bước nhanh | Active | TC-GEO-0448 |
| US-GEO-408 | US-GEO-408-SC1 | Success | Phần vẽ thêm hiện dần theo bước | Active | TC-GEO-0449 |
| US-GEO-408 | US-GEO-408-SC2 | Success | "Vì sao nghĩ ra bước này?" | Active | TC-GEO-0450 |
| US-GEO-408 | US-GEO-408-SC3 | Success | Quay lại bước trước thì phần vẽ thêm bớt đi | Active | TC-GEO-0451 |
| US-GEO-408 | US-GEO-408-SC4 | Success | Nhảy thẳng tới bước cuối | Active | TC-GEO-0452 |
| US-GEO-408 | US-GEO-408-FC1 | Failure | Bước không có phần vẽ thêm | Active | TC-GEO-0453 |
| US-GEO-409 | US-GEO-409-SC1 | Success | Câu trả lời có nguồn "Giáo án mẫu" | Active · PROVISIONAL | TC-GEO-0454 |
| US-GEO-409 | US-GEO-409-SC2 | Success | Câu trả lời có nguồn "AI soạn" | Active · PROVISIONAL | TC-GEO-0455 |
| US-GEO-409 | US-GEO-409-SC3 | Success | Câu hỏi ngoài giáo trình | Active | TC-GEO-0456 |
| US-GEO-409 | US-GEO-409-SC4 | Success | Màn chờ khi trợ lý đang trả lời | Active | TC-GEO-0457 |
| US-GEO-409 | US-GEO-409-SC5 | Success | Câu hỏi được ghi nhận | Active | TC-GEO-0458 |
| US-GEO-409 | US-GEO-409-SC6 | Success | Nguồn nêu ra luôn mở được | Active | TC-GEO-0459 |
| US-GEO-409 | US-GEO-409-FC1 | Failure | Trợ lý lỗi | Active · PROVISIONAL | TC-GEO-0460 |
| US-GEO-409 | US-GEO-409-FC2 | Failure | Câu hỏi rỗng hoặc quá dài | Active · PROVISIONAL | TC-GEO-0461 |
| US-GEO-409 | US-GEO-409-FC3 | Failure | Giáo viên đã tắt hỏi đáp | Active · PROVISIONAL | TC-GEO-0462 |
| US-GEO-410 | US-GEO-410-SC1 | Success | Xem nhật ký của mình | Active · PROVISIONAL | TC-GEO-0463 |
| US-GEO-410 | US-GEO-410-SC2 | Success | Lọc theo bài | Active | TC-GEO-0464 |
| US-GEO-410 | US-GEO-410-SC3 | Success | Mở lại câu trả lời đã lưu | Active · PROVISIONAL | TC-GEO-0465 |
| US-GEO-410 | US-GEO-410-SC4 | Success | Chỉ thấy câu của mình | Active | TC-GEO-0466 |
| US-GEO-410 | US-GEO-410-SC5 | Success | Câu trả lời từng bị chặn mở lại vẫn là câu thay thế | Active · PROVISIONAL | TC-GEO-0493 |
| US-GEO-410 | US-GEO-410-FC1 | Failure | Bài chưa có câu hỏi | Active | TC-GEO-0467 |
| US-GEO-410 | US-GEO-410-FC2 | Failure | Con trỏ hỏng | Active | TC-GEO-0468 |
| US-GEO-411 | US-GEO-411-SC1 | Success | Bấm từ khóa trong gợi ý | Active · PROVISIONAL | TC-GEO-0469 |
| US-GEO-411 | US-GEO-411-SC2 | Success | Bài giảng do AI viết mang nhãn chờ duyệt | Active · PROVISIONAL | TC-GEO-0470 |
| US-GEO-411 | US-GEO-411-SC3 | Success | Từ khóa trong lời giải | Active | TC-GEO-0471 |
| US-GEO-411 | US-GEO-411-SC4 | Success | Đóng hộp bài giảng | Active | TC-GEO-0472 |
| US-GEO-411 | US-GEO-411-FC1 | Failure | Cụm từ không có bài giảng | Active | TC-GEO-0473 |
| US-GEO-411 | US-GEO-411-FC2 | Failure | Bài giảng không lộ đáp số của bài đang khóa | Active · PROVISIONAL | TC-GEO-0474 |
| US-GEO-411 | US-GEO-411-FC3 | Failure | Không dùng nhãn "Giáo viên đã duyệt" | Active · PROVISIONAL | TC-GEO-0475 |
| US-GEO-412 | US-GEO-412-SC1 | Success | Đủ 10 bài giảng với nguồn và nhãn | Active · PROVISIONAL | TC-GEO-0476 |
| US-GEO-412 | US-GEO-412-SC4 | Success | Cờ chờ soát hiện cho học sinh và giáo viên | Active · PROVISIONAL | TC-GEO-0494 |
| US-GEO-412 | US-GEO-412-SC2 | Success | Mỗi bài có từ khóa trỏ tới | Active | TC-GEO-0477 |
| US-GEO-412 | US-GEO-412-SC3 | Success | Mọi trường đọc được | Active · PROVISIONAL | TC-GEO-0478 |
| US-GEO-412 | US-GEO-412-FC1 | Failure | Bài giảng chứa lời giải của một đề trong kho | Active · PROVISIONAL | TC-GEO-0479 |
| US-GEO-412 | US-GEO-412-FC2 | Failure | Bài giảng "Giáo án mẫu" thiếu tham chiếu nguồn | Active | TC-GEO-0480 |

### 3.2 Chiều ngược: case → tiêu chí hoặc NFR (95 / 95 case trỏ về một tiêu chí của BA hoặc một NFR-GEO, 100.0%; riêng trỏ về tiêu chí của BA: 95 / 95, 100.0%)

Kiểm bằng `gen_cases.py`: mỗi case bám tiêu chí lấy đúng một tiêu chí từ bảng phân loại; case viết tay ghi tiêu chí ở Remark và bộ sinh từ chối id tiêu chí không tồn tại.

### 3.3 NFR-GEO được case trong tệp này nhắc tới

NFR-GEO-030, NFR-GEO-040, NFR-GEO-050

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
| 1 | bản đầu của goal run: 82 case cho 82 tiêu chí | QC |
| 2 | lượt sửa gom sau phản biện (M-304): TC-GEO-0438 là ca chặn, thêm biến thể gần trùng 0482…0484 (D-GEO-150, RV-GEO-01); dán trước, giao sau 0485, 0486 (D-GEO-151, RV-GEO-03); gợi ý đề kho cho tự học 0487 (D-GEO-155); không lưu đệm 0488 (D-GEO-145); viết lại bước ranh giới TC-GEO-0440 (D-GEO-169, RV-GEO-24): 95 case cho 93 tiêu chí | QC |
| 3 | đồng bộ với bàn giao của BA (M-217, M-218): case viết tay trùng tiêu chí mới chuyển thành case bám tiêu chí (giữ id), thêm case cho mọi tiêu chí mới, gỡ chỉnh không còn cần: 95 case cho 93 tiêu chí | QC |
| 4 | đồng bộ M-222: TC-GEO-0486 nối vào US-GEO-406-FC6 (dữ liệu HKG-05, BG-7 của BA): 95 case cho 93 tiêu chí | QC |
| 5 | sinh lại theo AC hiện hành (PO-50, Đợt 0): bộ sinh cho cùng nội dung với AC hiện hành (không case nào lệch); thêm cột "Tự động hóa" (E2E-UI / E2E-API / IT / Manual có lý do) cho mọi case; case tô hình cần móc đọc cảnh (QF-E3-08): 95 case cho 93 tiêu chí | QC |
