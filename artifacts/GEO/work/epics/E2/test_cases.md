# E2 — test case (Hình đúng: dựng hình, hình mẫu, kiểm chứng, nhãn)

> Chủ sở hữu: QC. **Tệp sinh ra, không sửa tay**: sửa `work/src/qc/E2_meta.txt` (phân loại từng tiêu chí), `work/src/qc/E2_extra.md` (case viết tay) hoặc `work/src/qc/E2_override.txt` (chỉnh của QC theo một D-GEO khi AC chưa kịp đổi; mỗi chỉnh ghi nguồn ở Remark) rồi chạy `python3 work/src/qc/gen_cases.py E2`. Tiền điều kiện, bước và kỳ vọng của case bám tiêu chí lấy nguyên từ Given / When / Then của `stories_ac.md` (BA); giá trị cụ thể là của `work/fixture.md` và công thức M-GEO. Id ổn định; tiêu chí bị gạch bỏ thì case giữ id với N/A. AI ở MVP là giả lập (D-GEO-047): không có kỳ vọng về chất lượng AI thật.

## 0. Reader guide and glossary

| Id / abbreviation | Meaning | Where used |
|---|---|---|
| `TC-GEO-nnnn` | test case; hàng trăm là epic, 0800…0899 là case xuyên epic | cột No |
| `US-GEO-nnn` · `SCn` / `FCn` | story của BA · tiêu chí thành công / thất bại của story đó | cột Remark; `stories_ac.md` cùng thư mục |
| `F-GEO-nnn` | hàng dữ liệu mẫu: 001…199 ở `work/fixture.md` (BA), 200…299 ở `review/QC_test_design.md` §6 (QC), 300…399 dữ liệu thử tải (ARCH) | cột Test data |
| `MSG-GEO-nn` | mã thông báo; câu chữ nguyên văn in ngay trước mã, lấy từ `work/05_dictionary.md` | cột Expected output |
| `NFR-GEO-nnn` | yêu cầu phi chức năng của ARCH (`work/architecture/06_nfr.md`); ngưỡng là đề xuất | cột Remark |
| `D-GEO-nnn` | quyết định hoặc giả định của PO (`work/03_decisions.md`) | cột Remark, sau chữ PROVISIONAL |
| `CL-FN-nn` | nhóm kiểm tra chức năng (`review/QC_test_design.md` §1) | cột Remark |
| `CL-UI-nn` | nhóm kiểm tra giao diện và thiết bị | cột Remark |
| `HKG-nn` | mã bài của Kho đề | dữ liệu |
| `BG-n`, `BG-C` | bài giao mẫu của `work/fixture.md` mục 3 | dữ liệu |
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
| US-GEO-201 Engine dựng hình từ ràng buộc | 8 (5 / 3) | 8 | 3 |
| US-GEO-202 Dựng hình mẫu có tham số không cần AI | 5 (4 / 1) | 5 | 1 |
| US-GEO-203 Kiểm chứng hình và đáp số, gắn nhãn máy | 14 (8 / 6) | 14 | 10 |
| US-GEO-204 Đề lạ dùng tọa độ AI kèm cảnh báo chưa kiểm chứng | 6 (3 / 3) | 6 | 1 |
| US-GEO-205 Dữ liệu: danh mục hình mẫu và tham số | 4 (3 / 1) | 4 | 2 |
| **Tổng** | **37** | **37** | **17** |

| Theo ưu tiên | Số case |
|---|---|
| High | 20 |
| Medium | 15 |
| Low | 2 |

| Theo loại | Số case |
|---|---|
| chức năng | 33 |
| phân quyền | 2 |
| dữ liệu trả về | 1 |
| phi chức năng | 0 |
| thiết bị | 1 |

| Theo tầng tự động hóa (một case có thể ở hai tầng) | Số case |
|---|---|
| đơn vị | 20 |
| tích hợp API | 9 |
| tích hợp CSDL | 2 |
| E2E trình duyệt | 0 |
| tải | 0 |
| thủ công trên thiết bị | 12 |

| Theo Tự động hóa (một case một loại) | Số case |
|---|---|
| E2E-UI | 12 |
| E2E-API | 5 |
| IT | 20 |
| Manual | 0 |
| **Tổng** | **37** |

## 2. Test case

### Cách đọc một test case
- Mỗi dòng là một case. Chuẩn bị theo **Pre-condition**, dùng giá trị ở **Test data**, làm lần lượt các bước đánh số ở **Test step**, rồi so với **Expected output**.
- **Expected output**: mỗi gạch đầu dòng là một điều phải đúng; chữ trong ngoặc kép và giá trị phải khớp nguyên văn. Case đạt khi mọi gạch đầu dòng đều đúng.
- **Loại**: chức năng · phân quyền · dữ liệu trả về (soát thân phản hồi của máy chủ, không chỉ giao diện) · phi chức năng · thiết bị.
- **Tầng tự động hóa**: đơn vị · tích hợp API · tích hợp CSDL · E2E trình duyệt (Playwright) · tải (Gatling) · thủ công trên thiết bị; đếm câu lệnh JDBC dùng datasource-proxy (D-GEO-170). Case có chữ ENG ở Remark cần công cụ kỹ thuật và thuộc tờ kỹ thuật; case còn lại người kiểm thử chạy trên màn.
- **Tự động hóa** (PO-50): cách tự động hóa chính của case, đầu vào của bộ E2E. `E2E-UI` · Playwright qua trình duyệt, theo đúng bước trên màn và kỳ vọng nhìn thấy (phần "máy chủ trả …" của kỳ vọng thì soát thêm qua phản hồi mạng mà trang nhận) · `E2E-API` · gọi HTTP thẳng tới máy chủ, soát `code` và thân phản hồi · `IT` · kiểm thử tích hợp phía máy chủ (CSDL, bộ quét, tầng dịch vụ) · `Manual` · người chạy, lý do ghi ngay sau.
- **PROVISIONAL**: kỳ vọng theo một giả định chưa được xác nhận, mã ghi ngay sau. Vẫn chạy; nếu màn chỉ khác ở đúng điểm đó thì ghi lại điều thấy và đánh dấu Blocked, không đánh Fail.
- Thực tế khác kỳ vọng thì ghi vào vòng thực thi, không sửa ô Expected.

### US-GEO-201 — Engine dựng hình từ ràng buộc

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0200 | Dựng hình HKG-01 thỏa mọi ràng buộc | High | Normal (Success case) | chức năng | đơn vị | IT | Kỹ thuật (engine) | • Đề HKG-01 (F-GEO-031) với a = 1 | • Hàng dữ liệu: F-GEO-031 | 1. Engine dựng hình | • Hình có năm đỉnh S, A, B, C, D với AB = BC = CD = DA = 1, SA = 1, SA vuông góc AB và AD (sai số mỗi ràng buộc ≤ 1e-6)<br/>• Nguồn tọa độ được ghi là engine | US-GEO-201 · SC1; CL-FN-01; PROVISIONAL D-GEO-040; ENG |
| TC-GEO-0201 | Dựng hình có số đo cụ thể | Medium | Normal (Success case) | chức năng | đơn vị | IT | Kỹ thuật (engine) | • Đề HKG-03 (F-GEO-033): đáy tam giác đều cạnh 4 cm, chiều cao 6 cm | • Hàng dữ liệu: F-GEO-033 | 1. Engine dựng hình | • Ba cạnh đáy đều bằng 4, ba cạnh bên bằng 6 và vuông góc với đáy<br/>• Đo cạnh đáy bằng công cụ Đo dài cho "4 cm" | US-GEO-201 · SC2; CL-FN-01; ENG |
| TC-GEO-0202 | Thẻ ràng buộc hiện ở góc hình | Medium | Normal (Success case) | chức năng | thủ công trên thiết bị | E2E-UI | Học sinh An, iPad | • An mở BG-1 (HKG-01) trên iPad | • Không có dữ liệu nhập riêng | 1. Hình hiện xong | • Góc dưới trái của khung hình có hai thẻ "SA = a" và "AD = a" | US-GEO-201 · SC3; CL-UI-02 |
| TC-GEO-0203 | Sai số sát dưới ngưỡng vẫn thỏa | High | Normal (Success case) | chức năng | đơn vị | IT | Kỹ thuật (engine) | • Một hình có ràng buộc vuông góc với sai số 9,9e-7 (F-GEO-073) | • Hàng dữ liệu: F-GEO-073 | 1. Kiểm hình | • Kết quả "thỏa ràng buộc" | US-GEO-201 · SC4; CL-FN-11; PROVISIONAL D-GEO-163; ENG; BVA: đúng ngưỡng 1e-6; so sánh có ε 1e-12 (D-GEO-163) |
| TC-GEO-0204 | Cùng đầu vào cho cùng hình | Medium | Normal (Success case) | chức năng | đơn vị | IT | Kỹ thuật (engine) | • Đề HKG-02 với a = 2 | • Không có dữ liệu nhập riêng | 1. Dựng hai lần liên tiếp | • Hai bộ tọa độ giống nhau từng số | US-GEO-201 · SC5; CL-FN-01; ENG |
| TC-GEO-0205 | Sai số vượt ngưỡng | High | Abnormal (Fail case/Edge case) | chức năng | đơn vị | IT | Kỹ thuật (engine) | • Hình có ràng buộc vuông góc với sai số 1,01e-6 (F-GEO-073) | • Hàng dữ liệu: F-GEO-073 | 1. Kiểm hình | • Kết quả "không thỏa ràng buộc"<br/>• Đề đó mang nhãn máy "Cần xem lại" | US-GEO-201 · FC1; CL-FN-11; PROVISIONAL D-GEO-163; ENG; BVA: vượt ngưỡng (2e-6); so sánh có ε 1e-12 (D-GEO-163) |
| TC-GEO-0206 | Ràng buộc mâu thuẫn | High | Abnormal (Fail case/Edge case) | chức năng | đơn vị | IT | Kỹ thuật (engine) | • Đề tự nhập có đáy tam giác ABC với AB = 1, BC = 1, CA = 3 (F-GEO-074) | • Hàng dữ liệu: F-GEO-074 | 1. An bấm "Dựng hình" | • Không có hình<br/>• Hiện "Chưa dựng được hình từ đề này. Em kiểm tra lại đề hoặc chọn một hình mẫu." (MSG-GEO-18)<br/>• Đề An đã gõ còn nguyên trong ô nhập<br/>• Hệ thống không lặng lẽ thay bằng tọa độ AI | US-GEO-201 · FC2; CL-FN-03; ENG |
| TC-GEO-0207 | Hình suy biến | Medium | Abnormal (Fail case/Edge case) | chức năng | đơn vị | IT | Kỹ thuật (engine) | • Đề tự nhập "Lăng trụ đứng tam giác có đáy là tam giác đều cạnh 4 cm, chiều cao 0 cm. Tính thể tích." | • Không có dữ liệu nhập riêng | 1. Bấm "Dựng hình" | • Không có hình<br/>• Hiện "Chưa dựng được hình từ đề này. Em kiểm tra lại đề hoặc chọn một hình mẫu." (MSG-GEO-18) | US-GEO-201 · FC3; CL-FN-03; ENG |

### US-GEO-202 — Dựng hình mẫu có tham số không cần AI

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0208 | Chọn hình mẫu và thấy hình ngay, không gọi AI | High | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Học sinh An | • An ở màn "Tự học"<br/>• Số bản ghi lượt gọi AI hiện có là n | • Không có dữ liệu nhập riêng | 1. An bấm thẻ "Hình nón" | • Hình nón hiện với tham số mặc định, có ô tham số "Bán kính đáy" và "Chiều cao"<br/>• Số bản ghi lượt gọi AI vẫn là n<br/>• Không hiện màn chờ AI | US-GEO-202 · SC1; CL-FN-01 |
| TC-GEO-0209 | Đổi tham số thì hình đổi theo | Medium | Normal (Success case) | chức năng | thủ công trên thiết bị | E2E-UI | Học sinh An | • Hình nón đang hiện | • Hàng dữ liệu: F-GEO-075 | 1. An đặt Bán kính đáy 3, Chiều cao 4 (F-GEO-075) | • Hình vẽ lại với bán kính 3 và chiều cao 4<br/>• Đo đường sinh bằng công cụ Đo dài cho "5" | US-GEO-202 · SC2; CL-FN-02 |
| TC-GEO-0210 | Nhận số thập phân dấu phẩy | Medium | Normal (Success case) | chức năng | thủ công trên thiết bị | E2E-UI | Học sinh An | • Hình nón đang hiện | • Giá trị nhập: "2,5" | 1. Gõ Bán kính đáy "2,5" | • Hình vẽ lại với bán kính 2,5<br/>• Ô vẫn hiện "2,5" | US-GEO-202 · SC3; CL-FN-12 |
| TC-GEO-0211 | Sáu hình mẫu đều dựng được | High | Normal (Success case) | chức năng | đơn vị + thủ công trên thiết bị | E2E-UI | Học sinh An | • Màn "Tự học" trên iPad (F-GEO-100) và trên iPhone (F-GEO-101) | • Hàng dữ liệu: F-GEO-100, F-GEO-101 | 1. Lần lượt bấm "Hình chóp", "Lăng trụ", "Hình hộp", "Hình nón", "Hình trụ", "Hình cầu" | • Mỗi lần đều có hình với tham số mặc định, xoay được | US-GEO-202 · SC4; CL-FN-01 |
| TC-GEO-0212 | Tham số không hợp lệ | Medium | Abnormal (Fail case/Edge case) | chức năng | đơn vị + thủ công trên thiết bị | E2E-UI | Học sinh An | • Hình nón đang hiện với bán kính 3 | • Hàng dữ liệu: F-GEO-075<br/>• Giá trị nhập: "0" | 1. Gõ Bán kính đáy lần lượt "0", "−2", "abc" (F-GEO-075) | • Mỗi lần hiện "Giá trị phải là số lớn hơn 0." (MSG-GEO-19) dưới ô<br/>• Hình giữ nguyên bán kính 3 | US-GEO-202 · FC1; CL-FN-02; PROVISIONAL D-GEO-109 |

### US-GEO-203 — Kiểm chứng hình và đáp số, gắn nhãn máy

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0213 | Đề khớp mang "Đã kiểm chứng" | High | Normal (Success case) | chức năng | đơn vị | IT | Kỹ thuật (engine) | • HKG-01 với đáp số AI "45°" (F-GEO-031)<br/>• Góc tính từ tọa độ engine là 45° | • Hàng dữ liệu: F-GEO-031 | 1. Engine kiểm chứng | • Nhãn máy của HKG-01 là "Đã kiểm chứng" | US-GEO-203 · SC1; CL-FN-11; PROVISIONAL D-GEO-040; ENG; bảng quyết định nhãn, dòng 1 |
| TC-GEO-0214 | Đề không khớp mang "Cần xem lại" | High | Normal (Success case) | chức năng | đơn vị | IT | Kỹ thuật (engine) | • HKG-01 với đáp số AI "60°" (F-GEO-070) | • Hàng dữ liệu: F-GEO-070 | 1. Engine kiểm chứng | • Nhãn máy là "Cần xem lại" | US-GEO-203 · SC2; CL-FN-11; ENG; bảng quyết định nhãn, dòng 2 |
| TC-GEO-0215 | Đáp số theo tham số kiểm trên ba bộ giá trị | High | Normal (Success case) | chức năng | đơn vị | IT | Kỹ thuật (engine) | • HKG-02 với đáp số "a" (F-GEO-032) | • Hàng dữ liệu: F-GEO-032 | 1. Engine kiểm chứng với a = 1, 2, 3 | • Khoảng cách tính được lần lượt 1, 2, 3, khớp cả ba<br/>• Nhãn "Đã kiểm chứng"<br/>• Báo cáo kiểm chứng ghi kết quả của từng bộ | US-GEO-203 · SC3; CL-FN-11; PROVISIONAL D-GEO-040; ENG |
| TC-GEO-0216 | Sai lệch sát dưới ngưỡng vẫn khớp | High | Normal (Success case) | chức năng | đơn vị | IT | Kỹ thuật (engine) | • Giá trị tính 45, đáp số AI 45,0044 (F-GEO-071; ngưỡng 1e-4 · 45 + 1e-12 ≈ 0,0045) | • Hàng dữ liệu: F-GEO-071 | 1. So đáp số | • "khớp" (\|45,0044 − 45\| = 0,0044 nhỏ hơn ngưỡng) | US-GEO-203 · SC4; CL-FN-11; PROVISIONAL D-GEO-163; ENG; BVA: đúng ngưỡng 1e-4; biên đổi thành 45,0044 vì 45,0045 nằm đúng ngưỡng dấu phẩy động; so sánh có ε 1e-12 (D-GEO-163, RV-GEO-23) |
| TC-GEO-0217 | Nhãn do engine tính có thể khác nhãn trong tệp nguồn | Medium | Normal (Success case) | chức năng | đơn vị | IT | Kỹ thuật (engine) | • HKG-06 (F-GEO-036): tệp nguồn ghi "Cần xem lại"<br/>• Với a = 1 engine tính R ≈ 0,8660 và V ≈ 2,7207, khớp đáp số "R = a√3/2; V = (√3/2)·π·a³" | • Hàng dữ liệu: F-GEO-036 | 1. Engine kiểm chứng | • Nhãn máy là "Đã kiểm chứng" | US-GEO-203 · SC5; CL-FN-11; PROVISIONAL D-GEO-105; ENG; xung đột dữ liệu đã biết: tệp nguồn ghi "Cần xem lại", phép tính cho "Đã kiểm chứng" |
| TC-GEO-0218 | Một chuỗi nhãn trên mọi màn | Medium | Normal (Success case) | thiết bị | thủ công trên thiết bị | E2E-UI | Giáo viên mẫu (máy tính) và học sinh An (iPad, iPhone) | • HKG-01 mang "Đã kiểm chứng" | • Không có dữ liệu nhập riêng | 1. Giáo viên mẫu xem thẻ đề ở "Kho đề" và trang chi tiết đề<br/>2. An xem BG-1 ở màn Làm bài trên iPad và iPhone | • Cả bốn chỗ hiện đúng chuỗi "Đã kiểm chứng"<br/>• Không chỗ nào ghi "Hình đã kiểm chứng" | US-GEO-203 · SC6; CL-UI-02; PROVISIONAL D-GEO-079; PROVISIONAL KG-09 (phiên bản thiết bị tối thiểu) |
| TC-GEO-0219 | Sai lệch vượt ngưỡng | High | Abnormal (Fail case/Edge case) | chức năng | đơn vị | IT | Kỹ thuật (engine) | • Giá trị tính 45, đáp số AI 45,0046 (F-GEO-071; \|45,0046 − 45\| = 0,0046 lớn hơn ngưỡng ≈ 0,0045) | • Hàng dữ liệu: F-GEO-071 | 1. So đáp số | • "không khớp"<br/>• Nhãn "Cần xem lại" | US-GEO-203 · FC1; CL-FN-11; PROVISIONAL D-GEO-163; ENG; BVA: vượt ngưỡng; so sánh có ε 1e-12 (D-GEO-163) |
| TC-GEO-0220 | Đáp số hai giá trị, một giá trị sai | High | Abnormal (Fail case/Edge case) | chức năng | đơn vị | IT | Kỹ thuật (engine) | • HKG-04 với đáp số AI "Sxq = 15π cm²; V = 16π cm³" (F-GEO-072)<br/>• Engine tính 15π và 12π | • Hàng dữ liệu: F-GEO-072 | 1. Engine kiểm chứng | • Nhãn "Cần xem lại"<br/>• Báo cáo kiểm chứng ghi diện tích xung quanh khớp, thể tích không khớp | US-GEO-203 · FC2; CL-FN-11; ENG |
| TC-GEO-0221 | AI tự khai nhãn bị bỏ qua | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Kỹ thuật (nhà cung cấp giả lập) | • Kết quả AI cho HKG-01 có đáp số "60°" (F-GEO-070) và tự ghi kèm trạng thái "Đã kiểm chứng" (kịch bản F-GEO-112) | • Hàng dữ liệu: F-GEO-070, F-GEO-112 | 1. Hệ thống lưu lời giải và kiểm chứng | • Nhãn máy là "Cần xem lại"<br/>• Trạng thái AI tự khai không được dùng ở bất kỳ màn nào | US-GEO-203 · FC3; CL-FN-11; ENG |
| TC-GEO-0222 | Giáo viên không đặt được nhãn máy | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API + thủ công trên thiết bị | E2E-UI | Giáo viên mẫu | • Giáo viên mẫu ở trang chi tiết của một đề mang "Cần xem lại" | • Không có dữ liệu nhập riêng | 1. Xem mọi điều khiển của trang và gửi thẳng tới máy chủ một yêu cầu đổi nhãn thành "Đã kiểm chứng" | • Trang không có điều khiển đổi nhãn máy<br/>• Yêu cầu gửi thẳng nhận `FORBIDDEN` ("Bạn không có quyền thực hiện thao tác này." (MSG-GEO-03))<br/>• Nhãn vẫn "Cần xem lại" | US-GEO-203 · FC4; CL-FN-05; PROVISIONAL D-GEO-020 |
| TC-GEO-0223 | Nhãn không kéo theo giá trị tính xuống thiết bị học sinh | High | Abnormal (Fail case/Edge case) | dữ liệu trả về | tích hợp API | E2E-API | Học sinh An | • BG-1 đang Sắp mở<br/>• An mở bài | • Không có dữ liệu nhập riêng | 1. Ghi lại dữ liệu trả về của trang Làm bài | • Có nhãn "Đã kiểm chứng"<br/>• Không có giá trị tính của báo cáo kiểm chứng ("45", "45°") và không có đáp số | US-GEO-203 · FC5; CL-FN-07; ENG |
| TC-GEO-0234 | Đề không tham số khớp theo dung sai tuyệt đối | High | Normal (Success case) | chức năng | đơn vị | IT | Kỹ thuật (engine) | • HKG-03 (F-GEO-033), đề không có tham số<br/>• Đáp số AI "24√3 cm³" (F-GEO-079)<br/>• Engine tính thể tích từ tọa độ với cạnh đáy 4, chiều cao 6 | • Hàng dữ liệu: F-GEO-033, F-GEO-079 | 1. Engine kiểm chứng | • Tính một lần (không cần ba bộ giá trị tham số)<br/>• \|24√3 − giá trị tính\| ≤ 1e-6 + 1e-12 nên "khớp"<br/>• Nhãn máy "Đã kiểm chứng"<br/>• Báo cáo kiểm chứng ghi dung sai tuyệt đối đã dùng | US-GEO-203 · SC7; CL-FN-11; PROVISIONAL D-GEO-178; ENG; D-GEO-140; BVA dung sai tuyệt đối |
| TC-GEO-0235 | Đề không tham số: lệch vượt dung sai theo chữ số ghi | High | Abnormal (Fail case/Edge case) | chức năng | đơn vị | IT | Kỹ thuật (engine) | • HKG-03 với đáp số AI "41,56 cm³" (F-GEO-079)<br/>• Giá trị tính 24√3 ≈ 41,569219 | • Hàng dữ liệu: F-GEO-079 | 1. Engine kiểm chứng | • Dung sai = max(1e-6; 0,005) = 0,005<br/>• Lệch khoảng 9,2e-3 lớn hơn 0,005 + 1e-12 nên "không khớp"<br/>• Nhãn máy "Cần xem lại" | US-GEO-203 · FC6; CL-FN-11; PROVISIONAL D-GEO-178; ENG; D-GEO-140; BVA dung sai tuyệt đối |
| TC-GEO-0236 | Đáp số thập phân khớp theo nửa đơn vị chữ số cuối | High | Normal (Success case) | chức năng | đơn vị | IT | Kỹ thuật (engine) | • HKG-03 với đáp số AI "41,57 cm³" (F-GEO-079)<br/>• Giá trị tính 24√3 ≈ 41,569219 | • Hàng dữ liệu: F-GEO-079 | 1. Engine kiểm chứng | • Đáp số ghi hai chữ số thập phân nên dung sai = max(1e-6; 0,005) = 0,005<br/>• Lệch khoảng 7,8e-4 nên "khớp"<br/>• Nhãn máy "Đã kiểm chứng" | US-GEO-203 · SC8; CL-FN-11; PROVISIONAL D-GEO-178; ENG; D-GEO-178; BVA: dung sai nửa đơn vị chữ số cuối (cặp với TC-GEO-0235) |

### US-GEO-204 — Đề lạ dùng tọa độ AI kèm cảnh báo chưa kiểm chứng

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0224 | Đề lạ có hình kèm cảnh báo | High | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Học sinh An | • An ở "Tự học", gõ đề bát diện đều (F-GEO-061) | • Hàng dữ liệu: F-GEO-061, F-GEO-110 | 1. Bấm "Dựng hình" và nhà cung cấp giả lập trả tọa độ sáu đỉnh (kịch bản F-GEO-110) | • Hình hiện<br/>• Cạnh đề có nhãn "Cần xem lại"<br/>• Trên khung hình có "Hình này do AI dựng và chưa qua kiểm tra bằng tính toán. Em hãy đối chiếu với đề." (MSG-GEO-16) | US-GEO-204 · SC1; CL-FN-11; bảng quyết định nhãn, dòng 4 |
| TC-GEO-0225 | Cảnh báo luôn hiện | Medium | Normal (Success case) | chức năng | thủ công trên thiết bị | E2E-UI | Học sinh An | • Hình đề lạ của SC1 đang hiện | • Không có dữ liệu nhập riêng | 1. An xoay hình, đổi qua tab "Hỏi đáp"<br/>2. Quay lại, tải lại trang | • "Hình này do AI dựng và chưa qua kiểm tra bằng tính toán. Em hãy đối chiếu với đề." (MSG-GEO-16) và nhãn "Cần xem lại" vẫn hiện<br/>• Không có nút đóng cảnh báo | US-GEO-204 · SC2; CL-FN-11 |
| TC-GEO-0226 | Đề lạ được đếm vào tỉ lệ đề lạ | Low | Normal (Success case) | chức năng | đơn vị | IT | Kỹ thuật | • Trong kỳ đo An đã dựng hai đề tự nhập: F-GEO-060 (khớp hình mẫu) và F-GEO-061 (đề lạ) | • Hàng dữ liệu: F-GEO-060, F-GEO-061 | 1. Tính tỉ lệ đề lạ | • 1 / 2 = 50,0% | US-GEO-204 · SC3; CL-FN-12; ENG |
| TC-GEO-0227 | Đề lạ không bao giờ "Đã kiểm chứng" | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Học sinh An | • Đề lạ F-GEO-061 có đáp số AI | • Hàng dữ liệu: F-GEO-061 | 1. Hệ thống gắn nhãn | • Nhãn là "Cần xem lại", bất kể đáp số AI là gì | US-GEO-204 · FC1; CL-FN-11; ENG |
| TC-GEO-0228 | Tọa độ AI hỏng | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Học sinh An | • Đề lạ F-GEO-061<br/>• Nhà cung cấp giả lập trả sáu đỉnh trùng một điểm (kịch bản F-GEO-111) | • Hàng dữ liệu: F-GEO-061, F-GEO-111 | 1. Hệ thống nhận kết quả | • Không vẽ hình<br/>• Hiện "Chưa dựng được hình từ đề này. Em kiểm tra lại đề hoặc chọn một hình mẫu." (MSG-GEO-18)<br/>• Đề còn nguyên trong ô nhập | US-GEO-204 · FC2; CL-FN-14; PROVISIONAL D-GEO-072; ENG |
| TC-GEO-0229 | Đề hình phẳng | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Học sinh An | • An gõ đề tam giác vuông (F-GEO-064) | • Hàng dữ liệu: F-GEO-064 | 1. Bấm "Dựng hình" | • Hiện "Hiện chưa hỗ trợ hình học phẳng. Em hãy nhập đề hình học không gian." (MSG-GEO-17)<br/>• Không có hình<br/>• Không có bài tự học nào được tạo | US-GEO-204 · FC3; CL-UI-03 |

### US-GEO-205 — Dữ liệu: danh mục hình mẫu và tham số

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0230 | Danh mục đủ hình | Medium | Normal (Success case) | chức năng | tích hợp CSDL | IT | Kỹ thuật | • Dữ liệu danh mục hình mẫu | • Không có dữ liệu nhập riêng | 1. Nạp | • Có đủ chóp, lăng trụ, hộp, lập phương (hộp ba cạnh bằng nhau), nón, trụ, cầu<br/>• Hệ trục Oxyz bật được trên mọi hình | US-GEO-205 · SC1; CL-FN-01; ENG |
| TC-GEO-0231 | Sáu thẻ hình mẫu trên màn "Tự học" | Medium | Normal (Success case) | chức năng | thủ công trên thiết bị | E2E-UI | Học sinh An | • An mở "Tự học" | • Không có dữ liệu nhập riêng | 1. Xem mục "Hoặc chọn một hình mẫu" | • Có đúng sáu thẻ theo thứ tự "Hình chóp", "Lăng trụ", "Hình hộp", "Hình nón", "Hình trụ", "Hình cầu" | US-GEO-205 · SC2; CL-UI-02 |
| TC-GEO-0232 | Trường nào cũng đọc được danh mục | Medium | Normal (Success case) | phân quyền | tích hợp API | E2E-API | Học sinh An của lớp 9C (trường khác) | • Học sinh An của lớp 9C, trường "THCS Mẫu C" (F-GEO-026) | • Hàng dữ liệu: F-GEO-026 | 1. Mở "Tự học" | • Thấy cùng sáu thẻ hình mẫu như An lớp 11A1 | US-GEO-205 · SC3; CL-FN-06; PROVISIONAL D-GEO-076; ENG |
| TC-GEO-0233 | Hình mẫu thiếu giá trị mặc định | Low | Abnormal (Fail case/Edge case) | chức năng | tích hợp CSDL | IT | Kỹ thuật | • Dữ liệu danh mục trong đó "Hình trụ" không có giá trị mặc định cho "Chiều cao" | • Không có dữ liệu nhập riêng | 1. Nạp | • Việc nạp báo "Hình trụ: thiếu giá trị mặc định của Chiều cao" và không đưa hình đó vào danh mục | US-GEO-205 · FC1; CL-FN-03; PROVISIONAL D-GEO-109; ENG |

## 3. Đối chiếu hai chiều AC ↔ TC

### 3.1 Chiều xuôi: tiêu chí → case (37 / 37 tiêu chí còn hiệu lực có case, 100.0%)

| User story | Criterion ID | Kind | What it checks | Status | Test cases |
|---|---|---|---|---|---|
| US-GEO-201 | US-GEO-201-SC1 | Success | Dựng hình HKG-01 thỏa mọi ràng buộc | Active · PROVISIONAL | TC-GEO-0200 |
| US-GEO-201 | US-GEO-201-SC2 | Success | Dựng hình có số đo cụ thể | Active | TC-GEO-0201 |
| US-GEO-201 | US-GEO-201-SC3 | Success | Thẻ ràng buộc hiện ở góc hình | Active | TC-GEO-0202 |
| US-GEO-201 | US-GEO-201-SC4 | Success | Sai số sát dưới ngưỡng vẫn thỏa | Active · PROVISIONAL | TC-GEO-0203 |
| US-GEO-201 | US-GEO-201-SC5 | Success | Cùng đầu vào cho cùng hình | Active | TC-GEO-0204 |
| US-GEO-201 | US-GEO-201-FC1 | Failure | Sai số vượt ngưỡng | Active · PROVISIONAL | TC-GEO-0205 |
| US-GEO-201 | US-GEO-201-FC2 | Failure | Ràng buộc mâu thuẫn | Active | TC-GEO-0206 |
| US-GEO-201 | US-GEO-201-FC3 | Failure | Hình suy biến | Active | TC-GEO-0207 |
| US-GEO-202 | US-GEO-202-SC1 | Success | Chọn hình mẫu và thấy hình ngay, không gọi AI | Active | TC-GEO-0208 |
| US-GEO-202 | US-GEO-202-SC2 | Success | Đổi tham số thì hình đổi theo | Active | TC-GEO-0209 |
| US-GEO-202 | US-GEO-202-SC3 | Success | Nhận số thập phân dấu phẩy | Active | TC-GEO-0210 |
| US-GEO-202 | US-GEO-202-SC4 | Success | Sáu hình mẫu đều dựng được | Active | TC-GEO-0211 |
| US-GEO-202 | US-GEO-202-FC1 | Failure | Tham số không hợp lệ | Active · PROVISIONAL | TC-GEO-0212 |
| US-GEO-203 | US-GEO-203-SC1 | Success | Đề khớp mang "Đã kiểm chứng" | Active · PROVISIONAL | TC-GEO-0213 |
| US-GEO-203 | US-GEO-203-SC2 | Success | Đề không khớp mang "Cần xem lại" | Active | TC-GEO-0214 |
| US-GEO-203 | US-GEO-203-SC3 | Success | Đáp số theo tham số kiểm trên ba bộ giá trị | Active · PROVISIONAL | TC-GEO-0215 |
| US-GEO-203 | US-GEO-203-SC4 | Success | Sai lệch sát dưới ngưỡng vẫn khớp | Active · PROVISIONAL | TC-GEO-0216 |
| US-GEO-203 | US-GEO-203-SC5 | Success | Nhãn do engine tính có thể khác nhãn trong tệp nguồn | Active · PROVISIONAL | TC-GEO-0217 |
| US-GEO-203 | US-GEO-203-SC6 | Success | Một chuỗi nhãn trên mọi màn | Active · PROVISIONAL | TC-GEO-0218 |
| US-GEO-203 | US-GEO-203-FC1 | Failure | Sai lệch vượt ngưỡng | Active · PROVISIONAL | TC-GEO-0219 |
| US-GEO-203 | US-GEO-203-FC2 | Failure | Đáp số hai giá trị, một giá trị sai | Active | TC-GEO-0220 |
| US-GEO-203 | US-GEO-203-FC3 | Failure | AI tự khai nhãn bị bỏ qua | Active | TC-GEO-0221 |
| US-GEO-203 | US-GEO-203-FC4 | Failure | Giáo viên không đặt được nhãn máy | Active · PROVISIONAL | TC-GEO-0222 |
| US-GEO-203 | US-GEO-203-FC5 | Failure | Nhãn không kéo theo giá trị tính xuống thiết bị học sinh | Active | TC-GEO-0223 |
| US-GEO-203 | US-GEO-203-SC7 | Success | Đề không tham số khớp theo dung sai tuyệt đối | Active · PROVISIONAL | TC-GEO-0234 |
| US-GEO-203 | US-GEO-203-FC6 | Failure | Đề không tham số: lệch vượt dung sai theo chữ số ghi | Active · PROVISIONAL | TC-GEO-0235 |
| US-GEO-203 | US-GEO-203-SC8 | Success | Đáp số thập phân khớp theo nửa đơn vị chữ số cuối | Active · PROVISIONAL | TC-GEO-0236 |
| US-GEO-204 | US-GEO-204-SC1 | Success | Đề lạ có hình kèm cảnh báo | Active | TC-GEO-0224 |
| US-GEO-204 | US-GEO-204-SC2 | Success | Cảnh báo luôn hiện | Active | TC-GEO-0225 |
| US-GEO-204 | US-GEO-204-SC3 | Success | Đề lạ được đếm vào tỉ lệ đề lạ | Active | TC-GEO-0226 |
| US-GEO-204 | US-GEO-204-FC1 | Failure | Đề lạ không bao giờ "Đã kiểm chứng" | Active | TC-GEO-0227 |
| US-GEO-204 | US-GEO-204-FC2 | Failure | Tọa độ AI hỏng | Active · PROVISIONAL | TC-GEO-0228 |
| US-GEO-204 | US-GEO-204-FC3 | Failure | Đề hình phẳng | Active | TC-GEO-0229 |
| US-GEO-205 | US-GEO-205-SC1 | Success | Danh mục đủ hình | Active | TC-GEO-0230 |
| US-GEO-205 | US-GEO-205-SC2 | Success | Sáu thẻ hình mẫu trên màn "Tự học" | Active | TC-GEO-0231 |
| US-GEO-205 | US-GEO-205-SC3 | Success | Trường nào cũng đọc được danh mục | Active · PROVISIONAL | TC-GEO-0232 |
| US-GEO-205 | US-GEO-205-FC1 | Failure | Hình mẫu thiếu giá trị mặc định | Active · PROVISIONAL | TC-GEO-0233 |

### 3.2 Chiều ngược: case → tiêu chí hoặc NFR (37 / 37 case trỏ về một tiêu chí của BA hoặc một NFR-GEO, 100.0%; riêng trỏ về tiêu chí của BA: 37 / 37, 100.0%)

Kiểm bằng `gen_cases.py`: mỗi case bám tiêu chí lấy đúng một tiêu chí từ bảng phân loại; case viết tay ghi tiêu chí ở Remark và bộ sinh từ chối id tiêu chí không tồn tại.

### 3.3 NFR-GEO được case trong tệp này nhắc tới

NFR-GEO-021

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
| 1 | bản đầu của goal run: 34 case cho 34 tiêu chí | QC |
| 2 | lượt sửa gom sau phản biện (M-304): biên F-GEO-071 đổi thành 45,0044 ở TC-GEO-0216, so sánh có ε ở TC-GEO-0203, 0205, 0216, 0219 (D-GEO-163, RV-GEO-23): 37 case cho 37 tiêu chí | QC |
| 3 | đồng bộ với bàn giao của BA (M-217, M-218): case viết tay trùng tiêu chí mới chuyển thành case bám tiêu chí (giữ id), thêm case cho mọi tiêu chí mới, gỡ chỉnh không còn cần: 37 case cho 37 tiêu chí | QC |
| 4 | sinh lại theo AC hiện hành (PO-50, Đợt 0): bộ sinh cho cùng nội dung với AC hiện hành (không case nào lệch); thêm cột "Tự động hóa" (E2E-UI / E2E-API / IT / Manual có lý do) cho mọi case: 37 case cho 37 tiêu chí | QC |
