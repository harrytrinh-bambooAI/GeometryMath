# E5 — test case (Kho đề của Giáo viên mẫu)

> Chủ sở hữu: QC. **Tệp sinh ra, không sửa tay**: sửa `work/src/qc/E5_meta.txt` (phân loại từng tiêu chí), `work/src/qc/E5_extra.md` (case viết tay) hoặc `work/src/qc/E5_override.txt` (chỉnh của QC theo một D-GEO khi AC chưa kịp đổi; mỗi chỉnh ghi nguồn ở Remark) rồi chạy `python3 work/src/qc/gen_cases.py E5`. Tiền điều kiện, bước và kỳ vọng của case bám tiêu chí lấy nguyên từ Given / When / Then của `stories_ac.md` (BA); giá trị cụ thể là của `work/fixture.md` và công thức M-GEO. Id ổn định; tiêu chí bị gạch bỏ thì case giữ id với N/A. AI ở MVP là giả lập (D-GEO-047): không có kỳ vọng về chất lượng AI thật.

## 0. Reader guide and glossary

| Id / abbreviation | Meaning | Where used |
|---|---|---|
| `TC-GEO-nnnn` | test case; hàng trăm là epic, 0800…0899 là case xuyên epic | cột No |
| `US-GEO-nnn` · `SCn` / `FCn` | story của BA · tiêu chí thành công / thất bại của story đó | cột Remark; `stories_ac.md` cùng thư mục |
| `F-GEO-nnn` | hàng dữ liệu mẫu: 001…199 ở `work/fixture.md` (BA), 200…299 ở `review/QC_test_design.md` §6 (QC), 300…399 dữ liệu thử tải (ARCH) | cột Test data |
| `MSG-GEO-nn` | mã thông báo; câu chữ nguyên văn in ngay trước mã, lấy từ `work/05_dictionary.md` | cột Expected output |
| `NFR-GEO-nnn` | yêu cầu phi chức năng của ARCH (`work/architecture/06_nfr.md`); ngưỡng là đề xuất | cột Remark |
| `D-GEO-nnn` | quyết định hoặc giả định của PO (`work/03_decisions.md`) | cột Remark, sau chữ PROVISIONAL |
| `M-GEO-nn` | công thức chỉ số, một công thức mỗi con số (`work/02_domain_model.md` §3) | cột Expected output |
| `BR-GEO-nn` | quy tắc nghiệp vụ (`work/02_gate2_brd.md`; 100…199 do BA làm rõ) | cột Remark |
| `CL-FN-nn` | nhóm kiểm tra chức năng (`review/QC_test_design.md` §1) | cột Remark |
| `CL-UI-nn` | nhóm kiểm tra giao diện và thiết bị | cột Remark |
| `HKG-nn` | mã bài của Kho đề | dữ liệu |
| `BG-n`, `BG-C` | bài giao mẫu của `work/fixture.md` mục 3 | dữ liệu |
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
| US-GEO-501 Xem Kho đề và lọc theo cấp, loại hình, dạng bài | 10 (6 / 4) | 10 | 7 |
| US-GEO-502 Thẻ đề hiện nhãn máy và "AI soạn" | 5 (3 / 2) | 5 | 3 |
| US-GEO-503 Xem một đề: hình, đáp số, lời giải từng bước, gợi ý | 11 (5 / 6) | 11 | 5 |
| US-GEO-504 Dữ liệu: nạp 40 đến 60 đề của Giáo viên mẫu và tính lại nhãn | 8 (6 / 2) | 8 | 3 |
| US-GEO-505 Kho đề tải theo trang theo khóa | 9 (5 / 4) | 9 | 3 |
| **Tổng** | **43** | **43** | **21** |

| Theo ưu tiên | Số case |
|---|---|
| High | 19 |
| Medium | 22 |
| Low | 2 |

| Theo loại | Số case |
|---|---|
| chức năng | 34 |
| phân quyền | 4 |
| dữ liệu trả về | 2 |
| phi chức năng | 1 |
| thiết bị | 2 |

| Theo tầng tự động hóa (một case có thể ở hai tầng) | Số case |
|---|---|
| đơn vị | 0 |
| tích hợp API | 24 |
| tích hợp CSDL | 10 |
| E2E trình duyệt | 0 |
| tải | 0 |
| thủ công trên thiết bị | 23 |

| Theo Tự động hóa (một case một loại) | Số case |
|---|---|
| E2E-UI | 23 |
| E2E-API | 11 |
| IT | 9 |
| Manual | 0 |
| **Tổng** | **43** |

## 2. Test case

### Cách đọc một test case
- Mỗi dòng là một case. Chuẩn bị theo **Pre-condition**, dùng giá trị ở **Test data**, làm lần lượt các bước đánh số ở **Test step**, rồi so với **Expected output**.
- **Expected output**: mỗi gạch đầu dòng là một điều phải đúng; chữ trong ngoặc kép và giá trị phải khớp nguyên văn. Case đạt khi mọi gạch đầu dòng đều đúng.
- **Loại**: chức năng · phân quyền · dữ liệu trả về (soát thân phản hồi của máy chủ, không chỉ giao diện) · phi chức năng · thiết bị.
- **Tầng tự động hóa**: đơn vị · tích hợp API · tích hợp CSDL · E2E trình duyệt (Playwright) · tải (Gatling) · thủ công trên thiết bị; đếm câu lệnh JDBC dùng datasource-proxy (D-GEO-170). Case có chữ ENG ở Remark cần công cụ kỹ thuật và thuộc tờ kỹ thuật; case còn lại người kiểm thử chạy trên màn.
- **Tự động hóa** (PO-50): cách tự động hóa chính của case, đầu vào của bộ E2E. `E2E-UI` · Playwright qua trình duyệt, theo đúng bước trên màn và kỳ vọng nhìn thấy (phần "máy chủ trả …" của kỳ vọng thì soát thêm qua phản hồi mạng mà trang nhận) · `E2E-API` · gọi HTTP thẳng tới máy chủ, soát `code` và thân phản hồi · `IT` · kiểm thử tích hợp phía máy chủ (CSDL, bộ quét, tầng dịch vụ) · `Manual` · người chạy, lý do ghi ngay sau.
- **PROVISIONAL**: kỳ vọng theo một giả định chưa được xác nhận, mã ghi ngay sau. Vẫn chạy; nếu màn chỉ khác ở đúng điểm đó thì ghi lại điều thấy và đánh dấu Blocked, không đánh Fail.
- Thực tế khác kỳ vọng thì ghi vào vòng thực thi, không sửa ô Expected.

### US-GEO-501 — Xem Kho đề và lọc theo cấp, loại hình, dạng bài

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0500 | Mở Kho đề | High | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Giáo viên mẫu, máy tính | • Kho thử F-GEO-080 | • Hàng dữ liệu: F-GEO-080, F-GEO-083 | 1. Giáo viên mẫu mở "Kho đề" | • Dòng đầu trang ghi "48 đề · 44 đã kiểm chứng · tài liệu của Giáo viên mẫu"<br/>• Trang đầu có 20 thẻ đề (F-GEO-083) và nút "Tải thêm"<br/>• Ba nhóm lọc đều ở "Tất cả" | US-GEO-501 · SC1; CL-FN-01; PROVISIONAL D-GEO-122 |
| TC-GEO-0501 | Lọc theo hai nhóm cùng lúc | High | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Giáo viên mẫu | • Như SC1 | • Hàng dữ liệu: F-GEO-082 | 1. Chọn Cấp "THPT" và Dạng bài "Góc" (F-GEO-082) | • Danh sách có đúng 8 đề, trong đó có HKG-01<br/>• Mọi thẻ mang "THPT" và "Góc" | US-GEO-501 · SC2; CL-FN-02; PROVISIONAL D-GEO-070; bảng quyết định bộ lọc: hai nhóm cùng lúc |
| TC-GEO-0502 | Lọc theo hình | Medium | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Giáo viên mẫu | • Như SC1 | • Không có dữ liệu nhập riêng | 1. Chọn Hình "Hình chóp" | • Danh sách có đúng 8 đề, đều là hình chóp | US-GEO-501 · SC3; CL-FN-02; PROVISIONAL D-GEO-070 |
| TC-GEO-0503 | Dòng đếm không đổi theo bộ lọc | Medium | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Giáo viên mẫu | • Đang lọc Cấp "THPT" và Dạng bài "Góc" (8 đề) | • Không có dữ liệu nhập riêng | 1. Xem dòng đầu trang,<br/>2. Đưa cả hai nhóm về "Tất cả" | • Trong lúc lọc dòng đầu trang vẫn ghi "48 đề · 44 đã kiểm chứng"<br/>• Sau khi bỏ lọc danh sách trở lại trang đầu 20 thẻ | US-GEO-501 · SC4; CL-FN-12; PROVISIONAL D-GEO-122 |
| TC-GEO-0504 | Nội dung một thẻ đề | Medium | Normal (Success case) | chức năng | thủ công trên thiết bị | E2E-UI | Giáo viên mẫu | • Như SC1 | • Hàng dữ liệu: F-GEO-031 | 1. Xem thẻ của HKG-01 | • Thẻ có hình chóp thu nhỏ, "THPT", "Góc", nhãn "Đã kiểm chứng", đề bài nguyên văn của F-GEO-031, dòng "AI soạn · 3 bước" và nút "Giao bài" | US-GEO-501 · SC5; CL-UI-02 |
| TC-GEO-0505 | Dùng được trên điện thoại | Medium | Normal (Success case) | thiết bị | thủ công trên thiết bị | E2E-UI | Giáo viên mẫu, điện thoại | • Giáo viên mẫu trên điện thoại (F-GEO-103) | • Hàng dữ liệu: F-GEO-103 | 1. Mở "Kho đề" và chọn Cấp "THCS" | • Các thẻ xếp một cột, không phải cuộn ngang<br/>• Lọc cho đúng các đề THCS<br/>• Nút "Giao bài" bấm được | US-GEO-501 · SC6; CL-UI-01; PROVISIONAL KG-09 |
| TC-GEO-0506 | Lọc không có kết quả | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Giáo viên mẫu | • Như SC1 | • Hàng dữ liệu: F-GEO-081 | 1. Chọn Cấp "THCS" và Dạng bài "Mặt cầu" (F-GEO-081) | • Không có thẻ nào<br/>• Hiện "Không có đề nào khớp bộ lọc. Hãy bỏ bớt điều kiện." (MSG-GEO-40)<br/>• Không có nút "Tải thêm" | US-GEO-501 · FC1; CL-FN-02 |
| TC-GEO-0507 | Học sinh gọi Kho đề | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API + thủ công trên thiết bị | E2E-UI | Học sinh An | • Phiên của An (học sinh) | • Không có dữ liệu nhập riêng | 1. Gọi thẳng danh sách Kho đề | • `FORBIDDEN` ("Bạn không có quyền thực hiện thao tác này." (MSG-GEO-03))<br/>• Không có đề nào trong dữ liệu trả về<br/>• Thanh bên của học sinh không có mục "Kho đề" | US-GEO-501 · FC2; CL-FN-05; PROVISIONAL D-GEO-131 |
| TC-GEO-0508 | Không có tìm kiếm và thêm đề | Medium | Abnormal (Fail case/Edge case) | chức năng | thủ công trên thiết bị | E2E-UI | Giáo viên mẫu | • Giáo viên mẫu ở "Kho đề" | • Không có dữ liệu nhập riêng | 1. Xem toàn bộ màn | • Không có ô tìm kiếm, không có nút "Thêm đề", không có nút soạn hay import<br/>• Nhóm "Dạng bài" không có "Thiết diện" | US-GEO-501 · FC3; CL-UI-03; PROVISIONAL D-GEO-112; xác nhận hệ quả nhìn thấy của phần ngoài phạm vi (soạn, import) |
| TC-GEO-0509 | Giá trị lọc không hợp lệ | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên mẫu | • Phiên của Giáo viên mẫu | • Không có dữ liệu nhập riêng | 1. Gọi thẳng danh sách với Dạng bài "Thiết diện" | • `VALIDATION_FAILED` ("Thông tin chưa hợp lệ. Vui lòng kiểm tra lại các ô được đánh dấu." (MSG-GEO-01)), `details[]` nêu trường lọc sai<br/>• Không có đề nào được trả | US-GEO-501 · FC4; CL-FN-03; ENG |

### US-GEO-502 — Thẻ đề hiện nhãn máy và "AI soạn"

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0510 | Đề đã kiểm chứng | High | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Giáo viên mẫu | • Kho thử F-GEO-080 | • Hàng dữ liệu: F-GEO-080 | 1. Xem thẻ HKG-01 | • Thẻ có nhãn "Đã kiểm chứng" và dòng "AI soạn · 3 bước" | US-GEO-502 · SC1; CL-FN-11; PROVISIONAL D-GEO-079 |
| TC-GEO-0511 | Đề cần xem lại | High | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Giáo viên mẫu | • Kho thử F-GEO-080 có 4 đề "Cần xem lại" | • Hàng dữ liệu: F-GEO-080 | 1. Xem thẻ của một trong bốn đề đó | • Thẻ có nhãn "Cần xem lại" (khác màu với "Đã kiểm chứng") và vẫn có "AI soạn"<br/>• Nút "Giao bài" vẫn bấm được (giáo viên là người quyết định, BR-GEO-13) | US-GEO-502 · SC2; CL-FN-11 |
| TC-GEO-0512 | Kho đề đứng tên Giáo viên mẫu | Medium | Normal (Success case) | chức năng | thủ công trên thiết bị | E2E-UI | Giáo viên mẫu | • Giáo viên C của "THCS Mẫu C" mở "Kho đề" | • Không có dữ liệu nhập riêng | 1. Xem dòng đầu trang và khối "Lời giải" của trang chi tiết HKG-01 | • Dòng đầu trang ghi "tài liệu của Giáo viên mẫu"<br/>• Khối "Lời giải" ghi "AI soạn · tài liệu của Giáo viên mẫu" | US-GEO-502 · SC3; CL-UI-02; PROVISIONAL D-GEO-160 |
| TC-GEO-0513 | Đề chưa tính nhãn | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên mẫu | • Một đề vừa nạp, engine chưa chạy kiểm chứng | • Không có dữ liệu nhập riêng | 1. Xem thẻ của đề đó | • Nhãn là "Cần xem lại"<br/>• Thẻ không để trống nhãn và không hiện "Đã kiểm chứng" | US-GEO-502 · FC1; CL-FN-11; ENG |
| TC-GEO-0514 | Không có "Giáo viên đã duyệt" | Medium | Abnormal (Fail case/Edge case) | chức năng | thủ công trên thiết bị | E2E-UI | Giáo viên mẫu | • Kho thử F-GEO-080 | • Hàng dữ liệu: F-GEO-080 | 1. Duyệt hết 48 thẻ và mở trang chi tiết của HKG-01 | • Không chỗ nào hiện nhãn "Giáo viên đã duyệt"<br/>• Không có nút duyệt lời giải | US-GEO-502 · FC2; CL-UI-02; PROVISIONAL D-GEO-028 |

### US-GEO-503 — Xem một đề: hình, đáp số, lời giải từng bước, gợi ý

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0515 | Bốn phần của trang chi tiết | High | Normal (Success case) | chức năng | thủ công trên thiết bị | E2E-UI | Giáo viên mẫu | • Giáo viên mẫu ở "Kho đề" | • Hàng dữ liệu: F-GEO-031, F-GEO-037 | 1. Bấm vào đề HKG-01 | • Trang "Chi tiết đề & giao bài" hiện đề bài F-GEO-031<br/>• Hình chóp S.ABCD<br/>• Đáp số "45°"<br/>• Ba bước lời giải F-GEO-037 đúng thứ tự<br/>• Gợi ý 1 và Gợi ý 2 nguyên văn | US-GEO-503 · SC1; CL-FN-01 |
| TC-GEO-0516 | Giáo viên thấy lời giải dù bài đang khóa với học sinh | High | Normal (Success case) | phân quyền | tích hợp API + thủ công trên thiết bị | E2E-UI | Giáo viên mẫu | • BG-1 (HKG-01) đang Sắp mở với lớp 11A1 | • Không có dữ liệu nhập riêng | 1. Giáo viên mẫu mở trang chi tiết HKG-01 | • Lời giải ba bước và đáp số "45°" hiện đầy đủ | US-GEO-503 · SC2; CL-FN-05; DT-5: giáo viên thấy lời giải ở cả ba trạng thái giờ mở; D-GEO-153: giáo viên chỉ xem lời giải của đề kho nền tảng; ca đề tự nhập ở TC-GEO-0541, 0542 |
| TC-GEO-0517 | Chip đáp số khớp tính toán | Medium | Normal (Success case) | chức năng | thủ công trên thiết bị | E2E-UI | Giáo viên mẫu | • HKG-01 mang "Đã kiểm chứng"<br/>• Một đề khác trong bốn đề "Cần xem lại" | • Không có dữ liệu nhập riêng | 1. Mở trang chi tiết của từng đề | • HKG-01 có chip "Đáp số khớp tính toán: 45°"<br/>• Đề "Cần xem lại" không có chip này | US-GEO-503 · SC3; CL-FN-11 |
| TC-GEO-0518 | Hình xoay được trên trang chi tiết | Medium | Normal (Success case) | chức năng | thủ công trên thiết bị | E2E-UI | Giáo viên mẫu | • Trang chi tiết HKG-01 | • Không có dữ liệu nhập riêng | 1. Kéo chuột trên hình | • Hình xoay như ở màn Làm bài (US-GEO-301) | US-GEO-503 · SC4; CL-FN-01 |
| TC-GEO-0519 | Xem trên điện thoại | Medium | Normal (Success case) | thiết bị | thủ công trên thiết bị | E2E-UI | Giáo viên mẫu, điện thoại | • Giáo viên mẫu trên điện thoại (F-GEO-103) | • Hàng dữ liệu: F-GEO-103 | 1. Mở trang chi tiết HKG-01 | • Các phần xếp dọc theo thứ tự đề, hình, lời giải, gợi ý, giao bài<br/>• Không phải cuộn ngang | US-GEO-503 · SC5; CL-UI-01; PROVISIONAL KG-09 |
| TC-GEO-0520 | Đề không có lời giải | Low | Abnormal (Fail case/Edge case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Giáo viên mẫu | • Một đề trong kho không có bản ghi lời giải | • Không có dữ liệu nhập riêng | 1. Mở trang chi tiết của đề đó | • Khối "Lời giải" hiện "Đề này chưa có lời giải." (MSG-GEO-54)<br/>• Đề và hình vẫn hiện | US-GEO-503 · FC1; CL-FN-03 |
| TC-GEO-0521 | Không có nút "Sửa (sắp có)" và "Trình chiếu" | Medium | Abnormal (Fail case/Edge case) | chức năng | thủ công trên thiết bị | E2E-UI | Giáo viên mẫu | • Trang chi tiết HKG-01 trên máy tính và trên điện thoại | • Không có dữ liệu nhập riêng | 1. Xem toàn bộ trang | • Không có nút "Sửa (sắp có)", không có nút "Trình chiếu" và không có nhãn "Concept · Chặng 2" ở bất kỳ đâu trên trang | US-GEO-503 · FC2; CL-UI-03; PROVISIONAL D-GEO-164 |
| TC-GEO-0522 | Học sinh gọi trang chi tiết đề | High | Abnormal (Fail case/Edge case) | dữ liệu trả về | tích hợp API | E2E-API | Học sinh An | • Phiên của An<br/>• BG-1 đang Sắp mở | • Không có dữ liệu nhập riêng | 1. Gọi thẳng trang chi tiết HKG-01 và yêu cầu lời giải dành cho giáo viên | • `FORBIDDEN`<br/>• Dữ liệu trả về không có bước giải, không có "45°" | US-GEO-503 · FC3; CL-FN-07; PROVISIONAL D-GEO-131; ENG |
| TC-GEO-0523 | Mã đề không tồn tại | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên mẫu | • Phiên của Giáo viên mẫu | • Không có dữ liệu nhập riêng | 1. Mở trang chi tiết với một mã đề không có trong kho | • `NOT_FOUND`<br/>• Màn hiện "Không tìm thấy nội dung này." (MSG-GEO-04) | US-GEO-503 · FC4; CL-FN-03; ENG |
| TC-GEO-0541 | Giáo viên trường khác gọi lời giải của đề tự nhập | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API + tích hợp CSDL | E2E-API | Giáo viên C (trường khác) | • An của "THPT Mẫu A" có bài tự học từ đề F-GEO-060<br/>• Giáo viên C của "THCS Mẫu C" đã vào bằng `RW3NJ5PZC6TD` | • Hàng dữ liệu: F-GEO-060 | 1. Từ phiên của Giáo viên C, gửi thẳng yêu cầu lời giải dành cho giáo viên với mã đề tự nhập đó, kèm định danh trường "THPT Mẫu A" trong thân yêu cầu | • `NOT_FOUND`<br/>• Phản hồi giống hệt phản hồi cho một mã đề không tồn tại<br/>• Không có bước giải hay đáp số | US-GEO-503 · FC6; CL-FN-06; PROVISIONAL D-GEO-153; ENG; DT-10; tầng CSDL: hàm lời giải của giáo viên trả 0 hàng (D-GEO-153, RV-GEO-04) |
| TC-GEO-0542 | Giáo viên gọi lời giải của đề tự nhập của học sinh trường mình | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API + tích hợp CSDL | E2E-API | Giáo viên mẫu | • An (11A1) có bài tự học từ đề F-GEO-060 và đã mở được lời giải của bài đó<br/>• Phiên của Giáo viên mẫu cùng trường | • Hàng dữ liệu: F-GEO-060 | 1. Từ phiên của Giáo viên mẫu, gửi thẳng yêu cầu lời giải dành cho giáo viên với mã đề tự nhập đó | • `NOT_FOUND` ("Không tìm thấy nội dung này." (MSG-GEO-04))<br/>• Dữ liệu trả về không có bước giải, không có "60/√769", không có "2,1637" | US-GEO-503 · FC5; CL-FN-05; PROVISIONAL D-GEO-175; ENG; DT-10; tầng CSDL: hàm lời giải của giáo viên trả 0 hàng; đối chứng HKG-01 ở TC-GEO-0516 (D-GEO-153) |

### US-GEO-504 — Dữ liệu: nạp 40 đến 60 đề của Giáo viên mẫu và tính lại nhãn

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0524 | Nạp sáu đề nguồn đủ trường | High | Normal (Success case) | chức năng | tích hợp CSDL | IT | Kỹ thuật (nạp kho) | • Tệp `materials/Ngan-hang-bai-tap-Hinh-khong-gian.xlsx` | • Hàng dữ liệu: F-GEO-031…039 | 1. Nạp kho đề | • Có HKG-01…HKG-06, mỗi đề đủ đề bài, hai gợi ý, các bước kèm "vì sao", đáp số đúng như F-GEO-031…039<br/>• Tác giả "Giáo viên mẫu"<br/>• Cờ "AI soạn"<br/>• Có model và phiên bản prompt | US-GEO-504 · SC1; CL-FN-01; ENG |
| TC-GEO-0525 | Nhãn do engine tính lại | High | Normal (Success case) | chức năng | tích hợp CSDL | IT | Kỹ thuật | • Sáu đề vừa nạp | • Không có dữ liệu nhập riêng | 1. Engine chạy kiểm chứng cả kho | • Cả sáu đề mang "Đã kiểm chứng", kể cả HKG-06 mà tệp nguồn ghi "Cần xem lại" | US-GEO-504 · SC2; CL-FN-11; PROVISIONAL D-GEO-105; ENG |
| TC-GEO-0526 | Kết quả nạp nêu chỗ nhãn khác tệp | Medium | Normal (Success case) | chức năng | tích hợp CSDL | IT | Kỹ thuật | • Như SC2 | • Không có dữ liệu nhập riêng | 1. Đọc kết quả nạp | • Có dòng "HKG-06: tệp ghi Cần xem lại, engine tính Đã kiểm chứng"<br/>• Nhãn dùng trên mọi màn là nhãn engine tính | US-GEO-504 · SC3; CL-FN-11; PROVISIONAL D-GEO-105; ENG; xung đột dữ liệu đã biết: nhãn HKG-06 trong tệp nguồn khác nhãn engine tính |
| TC-GEO-0527 | Đếm số đề đã kiểm chứng so với tiêu chí MVP | High | Normal (Success case) | chức năng | tích hợp CSDL | IT | Kỹ thuật | • Kho chỉ có sáu đề nguồn (6 "Đã kiểm chứng")<br/>• Và kho thử F-GEO-080 (48 đề, 44 "Đã kiểm chứng") | • Hàng dữ liệu: F-GEO-080 | 1. Tính M-GEO-07 cho từng kho | • Kho sáu đề: 6, "chưa đạt (cần ≥ 40 đề đã kiểm chứng, kho 40 đến 60 đề)"<br/>• Kho thử: 44, "đạt" | US-GEO-504 · SC4; CL-FN-11; PROVISIONAL D-GEO-055; ENG |
| TC-GEO-0528 | Sửa lời giải tạo phiên bản mới, bài đã giao giữ phiên bản cũ | High | Normal (Success case) | chức năng | tích hợp CSDL | IT | Kỹ thuật | • BG-1 đã giao với lời giải HKG-01 phiên bản 1 | • Không có dữ liệu nhập riêng | 1. Nạp lại kho với bước 3 của HKG-01 được sửa câu chữ | • HKG-01 có lời giải phiên bản 2<br/>• BG-1 vẫn hiện lời giải phiên bản 1 cho lớp 11A1<br/>• Bài giao tạo sau đó dùng phiên bản 2 | US-GEO-504 · SC5; CL-FN-04; ENG |
| TC-GEO-0529 | Tính lại nhãn khi engine đổi | Medium | Normal (Success case) | chức năng | tích hợp CSDL | IT | Kỹ thuật | • Kho đã có nhãn<br/>• Engine được cập nhật phiên bản | • Không có dữ liệu nhập riêng | 1. Chạy lại kiểm chứng cả kho | • Mỗi đề có báo cáo kiểm chứng mới ghi phiên bản engine<br/>• Nhãn hiển thị là nhãn của lần chạy mới nhất | US-GEO-504 · SC6; CL-FN-04; ENG |
| TC-GEO-0530 | Đề thiếu đáp số | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp CSDL | IT | Kỹ thuật | • Tệp nạp có HKG-05 để trống ô Đáp số | • Không có dữ liệu nhập riêng | 1. Nạp | • HKG-05 không vào kho<br/>• Kết quả nạp ghi "HKG-05: thiếu đáp số"<br/>• Năm đề còn lại vào kho | US-GEO-504 · FC1; CL-FN-03; ENG |
| TC-GEO-0531 | Sửa tại chỗ lời giải đã giao bị từ chối | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp CSDL | IT | Kỹ thuật | • Lời giải HKG-01 phiên bản 1 đang được BG-1 dùng | • Không có dữ liệu nhập riêng | 1. Một thao tác cố ghi đè đáp số của phiên bản 1 thành "60°" | • Thao tác bị tầng dữ liệu từ chối<br/>• Phiên bản 1 vẫn ghi "45°" | US-GEO-504 · FC2; CL-FN-04; ENG |

### US-GEO-505 — Kho đề tải theo trang theo khóa

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0532 | Tải lần lượt hết kho | High | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Giáo viên mẫu | • Kho thử F-GEO-080 (48 đề), không lọc | • Hàng dữ liệu: F-GEO-080 | 1. Mở "Kho đề"<br/>2. Bấm "Tải thêm" cho tới khi nút biến mất | • Ba lần tải cho 20, 20 và 8 đề<br/>• Tổng cộng 48 đề, mỗi mã HKG-01…HKG-48 xuất hiện đúng một lần<br/>• Sau trang thứ ba không còn nút "Tải thêm" | US-GEO-505 · SC1; CL-FN-08; PROVISIONAL D-GEO-142 |
| TC-GEO-0533 | Dữ liệu đổi giữa chừng | Medium | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên mẫu | • Giáo viên mẫu đã tải trang đầu (20 đề)<br/>• Ngay sau đó một đề mới được nạp vào kho | • Không có dữ liệu nhập riêng | 1. Bấm "Tải thêm" tới hết | • Không đề nào của trang đầu lặp lại<br/>• Cả 28 đề còn lại của 48 đề ban đầu đều xuất hiện đúng một lần | US-GEO-505 · SC2; CL-FN-10; ENG |
| TC-GEO-0534 | Phân trang cùng bộ lọc | Medium | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên mẫu | • Kho thử<br/>• Lọc Cấp "THPT" (24 đề) | • Không có dữ liệu nhập riêng | 1. Tải hết | • Hai lần tải cho 20 và 4 đề, đều THPT, không trùng | US-GEO-505 · SC3; CL-FN-08; PROVISIONAL D-GEO-142; ENG |
| TC-GEO-0535 | Phản hồi không có tổng số | High | Normal (Success case) | dữ liệu trả về | tích hợp API | E2E-API | Giáo viên mẫu | • Phiên của Giáo viên mẫu | • Không có dữ liệu nhập riêng | 1. Ghi lại dữ liệu trả về của một trang danh sách Kho đề | • Có danh sách đề, cờ còn trang sau và con trỏ trang kế<br/>• Không có trường tổng số đề hay tổng số trang | US-GEO-505 · SC4; CL-FN-08; ENG |
| TC-GEO-0536 | Không OFFSET, không truy vấn lặp theo dòng | High | Normal (Success case) | phi chức năng | tích hợp API | IT | Kỹ thuật (bộ đếm câu lệnh) | • Kho thử<br/>• Công cụ đếm câu lệnh cơ sở dữ liệu của một yêu cầu | • Không có dữ liệu nhập riêng | 1. Gọi danh sách một lần với trang 1 đề và một lần với trang 20 đề | • Hai lần dùng cùng số câu lệnh<br/>• Không câu lệnh nào chứa OFFSET | US-GEO-505 · SC5; CL-X-03; ENG; công cụ: datasource-proxy (D-GEO-170) |
| TC-GEO-0537 | Hết danh sách | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Giáo viên mẫu | • Đã tải tới trang cuối (8 đề) của kho thử | • Không có dữ liệu nhập riêng | 1. Xem phản hồi của trang cuối | • Cờ còn trang sau là "không"<br/>• Màn không hiện nút "Tải thêm" và không hiện thông báo lỗi | US-GEO-505 · FC1; CL-FN-08 |
| TC-GEO-0538 | Đổi bộ lọc giữa chừng | Low | Abnormal (Fail case/Edge case) | chức năng | thủ công trên thiết bị | E2E-UI | Giáo viên mẫu | • Đã tải hai trang không lọc (40 đề) | • Không có dữ liệu nhập riêng | 1. Chọn Cấp "THCS" | • Danh sách được làm lại từ đầu theo bộ lọc mới: trang đầu 20 đề THCS<br/>• Không còn đề THPT nào của lần tải trước trên màn | US-GEO-505 · FC2; CL-FN-08 |
| TC-GEO-0539 | Con trỏ hỏng | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên mẫu | • Phiên của Giáo viên mẫu | • Hàng dữ liệu: F-GEO-084<br/>• Giá trị nhập: `khong-phai-con-tro` | 1. Gọi danh sách với con trỏ `khong-phai-con-tro` (F-GEO-084) | • `INVALID_CURSOR`<br/>• Màn hiện "Danh sách đã thay đổi. Vui lòng tải lại từ đầu." (MSG-GEO-05)<br/>• Không có đề nào được trả | US-GEO-505 · FC3; CL-FN-03; ENG |
| TC-GEO-0540 | Xin trang quá lớn | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên mẫu | • Kho thử F-GEO-080 (48 đề), không lọc | • Hàng dữ liệu: F-GEO-080, F-GEO-083 | 1. Gọi thẳng danh sách với kích thước trang 10000 | • Phản hồi thành công với đúng 48 đề (kích thước bị kẹp về trần 50 của Kho đề, F-GEO-083)<br/>• Cờ còn trang sau là "không" và không có con trỏ trang kế | US-GEO-505 · FC4; CL-FN-08; PROVISIONAL D-GEO-142; ENG; BVA: kích thước trang 10000; trang mặc định 20, trần Kho đề 50 (D-GEO-142); kho thử 48 đề |

## 3. Đối chiếu hai chiều AC ↔ TC

### 3.1 Chiều xuôi: tiêu chí → case (43 / 43 tiêu chí còn hiệu lực có case, 100.0%)

| User story | Criterion ID | Kind | What it checks | Status | Test cases |
|---|---|---|---|---|---|
| US-GEO-501 | US-GEO-501-SC1 | Success | Mở Kho đề | Active · PROVISIONAL | TC-GEO-0500 |
| US-GEO-501 | US-GEO-501-SC2 | Success | Lọc theo hai nhóm cùng lúc | Active · PROVISIONAL | TC-GEO-0501 |
| US-GEO-501 | US-GEO-501-SC3 | Success | Lọc theo hình | Active · PROVISIONAL | TC-GEO-0502 |
| US-GEO-501 | US-GEO-501-SC4 | Success | Dòng đếm không đổi theo bộ lọc | Active · PROVISIONAL | TC-GEO-0503 |
| US-GEO-501 | US-GEO-501-SC5 | Success | Nội dung một thẻ đề | Active | TC-GEO-0504 |
| US-GEO-501 | US-GEO-501-SC6 | Success | Dùng được trên điện thoại | Active | TC-GEO-0505 |
| US-GEO-501 | US-GEO-501-FC1 | Failure | Lọc không có kết quả | Active | TC-GEO-0506 |
| US-GEO-501 | US-GEO-501-FC2 | Failure | Học sinh gọi Kho đề | Active · PROVISIONAL | TC-GEO-0507 |
| US-GEO-501 | US-GEO-501-FC3 | Failure | Không có tìm kiếm và thêm đề | Active · PROVISIONAL | TC-GEO-0508 |
| US-GEO-501 | US-GEO-501-FC4 | Failure | Giá trị lọc không hợp lệ | Active | TC-GEO-0509 |
| US-GEO-502 | US-GEO-502-SC1 | Success | Đề đã kiểm chứng | Active · PROVISIONAL | TC-GEO-0510 |
| US-GEO-502 | US-GEO-502-SC2 | Success | Đề cần xem lại | Active | TC-GEO-0511 |
| US-GEO-502 | US-GEO-502-SC3 | Success | Kho đề đứng tên Giáo viên mẫu | Active · PROVISIONAL | TC-GEO-0512 |
| US-GEO-502 | US-GEO-502-FC1 | Failure | Đề chưa tính nhãn | Active | TC-GEO-0513 |
| US-GEO-502 | US-GEO-502-FC2 | Failure | Không có "Giáo viên đã duyệt" | Active · PROVISIONAL | TC-GEO-0514 |
| US-GEO-503 | US-GEO-503-SC1 | Success | Bốn phần của trang chi tiết | Active | TC-GEO-0515 |
| US-GEO-503 | US-GEO-503-SC2 | Success | Giáo viên thấy lời giải dù bài đang khóa với học sinh | Active | TC-GEO-0516 |
| US-GEO-503 | US-GEO-503-SC3 | Success | Chip đáp số khớp tính toán | Active | TC-GEO-0517 |
| US-GEO-503 | US-GEO-503-SC4 | Success | Hình xoay được trên trang chi tiết | Active | TC-GEO-0518 |
| US-GEO-503 | US-GEO-503-SC5 | Success | Xem trên điện thoại | Active | TC-GEO-0519 |
| US-GEO-503 | US-GEO-503-FC1 | Failure | Đề không có lời giải | Active | TC-GEO-0520 |
| US-GEO-503 | US-GEO-503-FC2 | Failure | Không có nút "Sửa (sắp có)" và "Trình chiếu" | Active · PROVISIONAL | TC-GEO-0521 |
| US-GEO-503 | US-GEO-503-FC3 | Failure | Học sinh gọi trang chi tiết đề | Active · PROVISIONAL | TC-GEO-0522 |
| US-GEO-503 | US-GEO-503-FC4 | Failure | Mã đề không tồn tại | Active | TC-GEO-0523 |
| US-GEO-503 | US-GEO-503-FC5 | Failure | Giáo viên gọi lời giải của đề tự nhập của học sinh trường mình | Active · PROVISIONAL | TC-GEO-0542 |
| US-GEO-503 | US-GEO-503-FC6 | Failure | Giáo viên trường khác gọi lời giải của đề tự nhập | Active · PROVISIONAL | TC-GEO-0541 |
| US-GEO-504 | US-GEO-504-SC1 | Success | Nạp sáu đề nguồn đủ trường | Active | TC-GEO-0524 |
| US-GEO-504 | US-GEO-504-SC2 | Success | Nhãn do engine tính lại | Active · PROVISIONAL | TC-GEO-0525 |
| US-GEO-504 | US-GEO-504-SC3 | Success | Kết quả nạp nêu chỗ nhãn khác tệp | Active · PROVISIONAL | TC-GEO-0526 |
| US-GEO-504 | US-GEO-504-SC4 | Success | Đếm số đề đã kiểm chứng so với tiêu chí MVP | Active · PROVISIONAL | TC-GEO-0527 |
| US-GEO-504 | US-GEO-504-SC5 | Success | Sửa lời giải tạo phiên bản mới, bài đã giao giữ phiên bản cũ | Active | TC-GEO-0528 |
| US-GEO-504 | US-GEO-504-SC6 | Success | Tính lại nhãn khi engine đổi | Active | TC-GEO-0529 |
| US-GEO-504 | US-GEO-504-FC1 | Failure | Đề thiếu đáp số | Active | TC-GEO-0530 |
| US-GEO-504 | US-GEO-504-FC2 | Failure | Sửa tại chỗ lời giải đã giao bị từ chối | Active | TC-GEO-0531 |
| US-GEO-505 | US-GEO-505-SC1 | Success | Tải lần lượt hết kho | Active · PROVISIONAL | TC-GEO-0532 |
| US-GEO-505 | US-GEO-505-SC2 | Success | Dữ liệu đổi giữa chừng | Active | TC-GEO-0533 |
| US-GEO-505 | US-GEO-505-SC3 | Success | Phân trang cùng bộ lọc | Active · PROVISIONAL | TC-GEO-0534 |
| US-GEO-505 | US-GEO-505-SC4 | Success | Phản hồi không có tổng số | Active | TC-GEO-0535 |
| US-GEO-505 | US-GEO-505-SC5 | Success | Không OFFSET, không truy vấn lặp theo dòng | Active | TC-GEO-0536 |
| US-GEO-505 | US-GEO-505-FC1 | Failure | Hết danh sách | Active | TC-GEO-0537 |
| US-GEO-505 | US-GEO-505-FC2 | Failure | Đổi bộ lọc giữa chừng | Active | TC-GEO-0538 |
| US-GEO-505 | US-GEO-505-FC3 | Failure | Con trỏ hỏng | Active | TC-GEO-0539 |
| US-GEO-505 | US-GEO-505-FC4 | Failure | Xin trang quá lớn | Active · PROVISIONAL | TC-GEO-0540 |

### 3.2 Chiều ngược: case → tiêu chí hoặc NFR (43 / 43 case trỏ về một tiêu chí của BA hoặc một NFR-GEO, 100.0%; riêng trỏ về tiêu chí của BA: 43 / 43, 100.0%)

Kiểm bằng `gen_cases.py`: mỗi case bám tiêu chí lấy đúng một tiêu chí từ bảng phân loại; case viết tay ghi tiêu chí ở Remark và bộ sinh từ chối id tiêu chí không tồn tại.

### 3.3 NFR-GEO được case trong tệp này nhắc tới

NFR-GEO-009, NFR-GEO-022, NFR-GEO-025, NFR-GEO-040, NFR-GEO-041

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
| 1 | bản đầu của goal run: 41 case cho 41 tiêu chí | QC |
| 2 | lượt sửa gom sau phản biện (M-304): TC-GEO-0540 theo trang 20 trần 50 (D-GEO-142); 0507, 0522 theo D-GEO-131; 0521 không hiện nút Concept (D-GEO-164); thêm ca xuyên trường lời giải giáo viên 0541, 0542 (D-GEO-153, RV-GEO-04); datasource-proxy cho 0536 (D-GEO-170): 43 case cho 43 tiêu chí | QC |
| 3 | đồng bộ với bàn giao của BA (M-217, M-218): case viết tay trùng tiêu chí mới chuyển thành case bám tiêu chí (giữ id), thêm case cho mọi tiêu chí mới, gỡ chỉnh không còn cần: 43 case cho 43 tiêu chí | QC |
| 4 | sinh lại theo AC hiện hành (PO-50, Đợt 0): bộ sinh cho cùng nội dung với AC hiện hành (không case nào lệch); thêm cột "Tự động hóa" (E2E-UI / E2E-API / IT / Manual có lý do) cho mọi case: 43 case cho 43 tiêu chí | QC |
