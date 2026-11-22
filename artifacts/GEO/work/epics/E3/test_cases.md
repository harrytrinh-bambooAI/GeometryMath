# E3 — test case (Thao tác hình 3D trên thiết bị cảm ứng)

> Chủ sở hữu: QC. **Tệp sinh ra, không sửa tay**: sửa `work/src/qc/E3_meta.txt` (phân loại từng tiêu chí), `work/src/qc/E3_extra.md` (case viết tay) hoặc `work/src/qc/E3_override.txt` (chỉnh của QC theo một D-GEO khi AC chưa kịp đổi; mỗi chỉnh ghi nguồn ở Remark) rồi chạy `python3 work/src/qc/gen_cases.py E3`. Tiền điều kiện, bước và kỳ vọng của case bám tiêu chí lấy nguyên từ Given / When / Then của `stories_ac.md` (BA); giá trị cụ thể là của `work/fixture.md` và công thức M-GEO. Id ổn định; tiêu chí bị gạch bỏ thì case giữ id với N/A. AI ở MVP là giả lập (D-GEO-047): không có kỳ vọng về chất lượng AI thật.

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
| `DT-n` | dòng bảng quyết định khóa lời giải × vai × trạng thái (`review/QC_test_design.md` §3) | cột Remark |
| `KG-nn` | known gap (`work/known_gaps.md`) | cột Remark |
| `RV-GEO-nn` | phát hiện của phản biện RV (`review/RV_findings.md`) | cột Remark |
| PROVISIONAL · ENG · X · N/A | kỳ vọng theo giả định chưa xác nhận · case cần công cụ kỹ thuật · case xuyên suốt không bám một tiêu chí · case giữ id nhưng không chạy | cột Remark |
| R · t · T0 | giờ mở lời giải (`release_at`) · giờ máy chủ · giờ máy chủ lúc bắt đầu ca kiểm | ca giờ mở |
| Chưa đặt giờ · Sắp mở · Đã mở | ba trạng thái giờ mở: chưa có giờ (khóa) · t < R (khóa) · t ≥ R (được xem) | kỳ vọng |
| Đã kiểm chứng · Cần xem lại · AI soạn | nhãn máy do engine tính · nhãn máy · nhãn người | kỳ vọng |

## 1. Tóm tắt

| Story | Số tiêu chí (SC / FC) | Số case | PROVISIONAL |
|---|---|---|---|
| US-GEO-301 Xoay, thu phóng, đặt lại góc nhìn; nét đứt cạnh khuất; trục Oxyz | 11 (9 / 2) | 11 | 6 |
| US-GEO-302 Chạm chọn đối tượng; tô màu cạnh và mặt | 6 (4 / 2) | 6 | 6 |
| US-GEO-303 Vẽ thêm đoạn, mặt phẳng; giao điểm, giao tuyến; mở rộng mặt, kéo dài cạnh | 9 (6 / 3) | 9 | 1 |
| US-GEO-304 Thêm điểm: trung điểm, điểm chia tỉ lệ, điểm tự do trên cạnh | 6 (4 / 2) | 6 | 2 |
| US-GEO-305 Đo độ dài, góc, khoảng cách; ẩn đại lượng đề hỏi | 11 (6 / 5) | 11 | 9 |
| US-GEO-306 Thao tác trên iPhone: chụm ngón, nút đặt lại, bố cục điện thoại | 7 (5 / 2) | 7 | 7 |
| US-GEO-307 Sửa lỗi hình đã biết | 4 (2 / 2) | 4 | 0 |
| **Tổng** | **54** | **54** | **31** |

| Theo ưu tiên | Số case |
|---|---|
| High | 25 |
| Medium | 26 |
| Low | 3 |

| Theo loại | Số case |
|---|---|
| chức năng | 38 |
| phân quyền | 0 |
| dữ liệu trả về | 1 |
| phi chức năng | 0 |
| thiết bị | 15 |

| Theo tầng tự động hóa (một case có thể ở hai tầng) | Số case |
|---|---|
| đơn vị | 11 |
| tích hợp API | 6 |
| tích hợp CSDL | 0 |
| E2E trình duyệt | 0 |
| tải | 0 |
| thủ công trên thiết bị | 51 |

## 2. Test case

### Cách đọc một test case
- Mỗi dòng là một case. Chuẩn bị theo **Pre-condition**, dùng giá trị ở **Test data**, làm lần lượt các bước đánh số ở **Test step**, rồi so với **Expected output**.
- **Expected output**: mỗi gạch đầu dòng là một điều phải đúng; chữ trong ngoặc kép và giá trị phải khớp nguyên văn. Case đạt khi mọi gạch đầu dòng đều đúng.
- **Loại**: chức năng · phân quyền · dữ liệu trả về (soát thân phản hồi của máy chủ, không chỉ giao diện) · phi chức năng · thiết bị.
- **Tầng tự động hóa**: đơn vị · tích hợp API · tích hợp CSDL · E2E trình duyệt (Playwright) · tải (Gatling) · thủ công trên thiết bị; đếm câu lệnh JDBC dùng datasource-proxy (D-GEO-170). Case có chữ ENG ở Remark cần công cụ kỹ thuật và thuộc tờ kỹ thuật; case còn lại người kiểm thử chạy trên màn.
- **PROVISIONAL**: kỳ vọng theo một giả định chưa được xác nhận, mã ghi ngay sau. Vẫn chạy; nếu màn chỉ khác ở đúng điểm đó thì ghi lại điều thấy và đánh dấu Blocked, không đánh Fail.
- Thực tế khác kỳ vọng thì ghi vào vòng thực thi, không sửa ô Expected.

### US-GEO-301 — Xoay, thu phóng, đặt lại góc nhìn; nét đứt cạnh khuất; trục Oxyz

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0300 | Kéo để xoay | High | Normal (Success case) | thiết bị | thủ công trên thiết bị | Học sinh An, iPad | • Hình HKG-01 ở góc nhìn lúc mới mở, công cụ "Xoay tự do" | • Không có dữ liệu nhập riêng | 1. An kéo một ngón sang trái trên khung hình | • Hình quay quanh trục thẳng đứng theo hướng kéo và dừng khi nhấc ngón<br/>• Nhãn đỉnh S, A, B, C, D đi theo đỉnh | US-GEO-301 · SC1; CL-UI-05; PROVISIONAL KG-09 |
| TC-GEO-0301 | Chụm ngón để thu phóng trên iPad | High | Normal (Success case) | thiết bị | thủ công trên thiết bị | Học sinh An, iPad | • Hình ở mức thu phóng lúc mới mở | • Không có dữ liệu nhập riêng | 1. An dang hai ngón trên khung hình | • Hình to lên quanh điểm giữa hai ngón<br/>• Trang web không bị phóng theo | US-GEO-301 · SC2; CL-UI-05; PROVISIONAL KG-09 |
| TC-GEO-0302 | Lăn chuột để thu phóng trên máy tính | Medium | Normal (Success case) | thiết bị | thủ công trên thiết bị | Máy tính, trình duyệt web | • Giáo viên mẫu ở trang chi tiết đề HKG-01 trên máy tính (F-GEO-102), con trỏ trong khung hình | • Hàng dữ liệu: F-GEO-102 | 1. Lăn chuột lên | • Hình to lên<br/>• Trang không cuộn | US-GEO-301 · SC3; CL-UI-01 |
| TC-GEO-0303 | "Nhìn từ trên" | Medium | Normal (Success case) | chức năng | thủ công trên thiết bị | Học sinh An, iPad | • Hình đã bị xoay bất kỳ | • Không có dữ liệu nhập riêng | 1. Bấm "Nhìn từ trên" | • Hình hiện như nhìn thẳng từ trên xuống đáy: ABCD là hình vuông, S nằm trùng vị trí A | US-GEO-301 · SC4; CL-FN-04 |
| TC-GEO-0304 | "Nhìn từ trước" | Medium | Normal (Success case) | chức năng | thủ công trên thiết bị | Học sinh An, iPad | • Hình đã bị xoay bất kỳ | • Không có dữ liệu nhập riêng | 1. Bấm "Nhìn từ trước" | • Hình hiện với cạnh SA thẳng đứng và mặt đáy nằm ngang thành một đoạn thẳng | US-GEO-301 · SC5; CL-FN-04 |
| TC-GEO-0305 | "Đặt lại" về góc nhìn ban đầu | Medium | Normal (Success case) | chức năng | thủ công trên thiết bị | Học sinh An, iPad | • An đã xoay, phóng to hình và đã thêm trung điểm M của SD | • Không có dữ liệu nhập riêng | 1. Bấm "Đặt lại" | • Góc nhìn và mức thu phóng trở về như lúc mới mở bài<br/>• Điểm M vẫn còn trên hình | US-GEO-301 · SC6; CL-FN-04; PROVISIONAL D-GEO-134 |
| TC-GEO-0306 | Cạnh khuất vẽ nét đứt và tính lại khi xoay | High | Normal (Success case) | chức năng | đơn vị + thủ công trên thiết bị | Học sinh An, iPad | • Hình ở góc nhìn lúc mới mở (khung `mvp-05`): SA, AB, AD nét đứt<br/>• SB, SC, SD, BC, CD nét liền | • Không có dữ liệu nhập riêng | 1. An xoay hình nửa vòng quanh trục thẳng đứng | • Các cạnh đang bị mặt khác che ở góc nhìn mới chuyển thành nét đứt, các cạnh hết bị che chuyển thành nét liền | US-GEO-301 · SC7; CL-FN-01 |
| TC-GEO-0307 | Bật, tắt trục Oxyz | Medium | Normal (Success case) | chức năng | thủ công trên thiết bị | Học sinh An, iPad | • Hình không có trục | • Không có dữ liệu nhập riêng | 1. Bấm "Oxyz"<br/>2. Bấm lần nữa | • Lần đầu hiện ba trục có nhãn x, y, z<br/>• Lần hai ba trục biến mất<br/>• Hình không đổi | US-GEO-301 · SC8; CL-FN-01 |
| TC-GEO-0308 | Thu phóng có giới hạn | Medium | Abnormal (Fail case/Edge case) | chức năng | thủ công trên thiết bị | Học sinh An, iPad | • Hình đang hiện | • Không có dữ liệu nhập riêng | 1. An chụm ngón liên tục để thu nhỏ,<br/>2. Dang liên tục để phóng to | • Thu phóng dừng ở một mức nhỏ nhất và một mức lớn nhất<br/>• Ở cả hai đầu vẫn thấy ít nhất một phần hình trong khung và nút "Đặt lại" đưa hình về bình thường | US-GEO-301 · FC1; CL-FN-02; PROVISIONAL D-GEO-117 |
| TC-GEO-0309 | Xoay và thu phóng không tính là thao tác trên hình | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | Học sinh An | • Hà mở BG-1 lần đầu | • Không có dữ liệu nhập riêng | 1. Hà chỉ xoay, thu phóng và bấm "Đặt lại"<br/>2. Rời bài | • Hà có sự kiện mở bài<br/>• Số thao tác trên hình của Hà là 0 | US-GEO-301 · FC2; CL-FN-13; PROVISIONAL D-GEO-058; ENG |
| TC-GEO-0353 | "Xóa hết" xóa phần vẽ thêm, giữ hình gốc và góc nhìn | Medium | Normal (Success case) | chức năng | thủ công trên thiết bị | Học sinh An, iPad | • An đã thêm trung điểm M của SD và đoạn AC, rồi xoay hình sang một góc nhìn khác lúc mới mở | • Không có dữ liệu nhập riêng | 1. Bấm "Xóa hết" | • Điểm M và đoạn AC biến mất<br/>• Năm đỉnh S, A, B, C, D và các cạnh của hình chóp còn đủ<br/>• Góc nhìn vẫn là góc An vừa xoay tới | US-GEO-301 · SC9; CL-FN-04; PROVISIONAL D-GEO-134; D-GEO-134 |

### US-GEO-302 — Chạm chọn đối tượng; tô màu cạnh và mặt

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0310 | Chạm chọn một cạnh | High | Normal (Success case) | thiết bị | thủ công trên thiết bị | Học sinh An, iPad | • Công cụ "Chọn" đang bật | • Không có dữ liệu nhập riêng | 1. An chạm vào cạnh SD | • Cạnh SD được tô màu nổi bật và dày hơn các cạnh khác<br/>• Hiện tên "Cạnh SD" | US-GEO-302 · SC1; CL-UI-05; PROVISIONAL KG-09 |
| TC-GEO-0311 | Chạm chọn một mặt | High | Normal (Success case) | thiết bị | thủ công trên thiết bị | Học sinh An, iPad | • Công cụ "Chọn" | • Không có dữ liệu nhập riêng | 1. An chạm vào giữa mặt SAD | • Mặt SAD được tô màu bán trong suốt<br/>• Hiện tên "Mặt SAD" | US-GEO-302 · SC2; CL-UI-05; PROVISIONAL KG-09 |
| TC-GEO-0312 | Chạm chọn một điểm | High | Normal (Success case) | thiết bị | thủ công trên thiết bị | Học sinh An, iPad | • Công cụ "Chọn" | • Không có dữ liệu nhập riêng | 1. An chạm vào đỉnh A | • Điểm A được tô nổi bật<br/>• Hiện tên "Điểm A" | US-GEO-302 · SC3; CL-UI-05; PROVISIONAL KG-09 |
| TC-GEO-0313 | Mỗi lần chọn là một thao tác trên hình | Medium | Normal (Success case) | chức năng | tích hợp API | Học sinh An | • Hà mở BG-1, chưa có thao tác nào | • Không có dữ liệu nhập riêng | 1. Hà dùng "Chọn" chạm cạnh SD<br/>2. Chạm mặt SAD | • Số thao tác trên hình của Hà trên BG-1 là 2 | US-GEO-302 · SC4; CL-FN-13; PROVISIONAL D-GEO-058; ENG |
| TC-GEO-0314 | Chạm vào chỗ chồng nhau | Medium | Abnormal (Fail case/Edge case) | thiết bị | thủ công trên thiết bị | Học sinh An, iPad và iPhone | • Công cụ "Chọn" | • Không có dữ liệu nhập riêng | 1. An chạm đúng đỉnh D (nơi gặp nhau của cạnh SD, CD, AD và các mặt) | • Chỉ điểm D được chọn, hiện "Điểm D"<br/>• Không cạnh hay mặt nào bị tô | US-GEO-302 · FC1; CL-UI-05; PROVISIONAL D-GEO-134; PROVISIONAL KG-09 |
| TC-GEO-0315 | Chạm ra ngoài hình | Low | Abnormal (Fail case/Edge case) | thiết bị | thủ công trên thiết bị | Học sinh An, iPad | • Cạnh SD đang được chọn | • Không có dữ liệu nhập riêng | 1. An chạm vào vùng trống của khung hình | • SD hết tô màu, không có đối tượng nào được chọn<br/>• Không có thao tác trên hình nào được ghi thêm | US-GEO-302 · FC2; CL-UI-05; PROVISIONAL D-GEO-134; PROVISIONAL KG-09 |

### US-GEO-303 — Vẽ thêm đoạn, mặt phẳng; giao điểm, giao tuyến; mở rộng mặt, kéo dài cạnh

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0316 | Vẽ đoạn nối hai điểm | High | Normal (Success case) | chức năng | thủ công trên thiết bị | Học sinh An, iPad | • Công cụ "Đường" | • Không có dữ liệu nhập riêng | 1. An chạm điểm A<br/>2. Điểm C | • Đoạn AC hiện trên mặt đáy<br/>• Ở góc nhìn lúc mới mở, AC là nét đứt vì nằm sau các mặt bên | US-GEO-303 · SC1; CL-FN-01 |
| TC-GEO-0317 | Giao điểm của hai đường cắt nhau có tên | High | Normal (Success case) | chức năng | đơn vị + thủ công trên thiết bị | Học sinh An, iPad | • Đã có đoạn AC<br/>• Chưa có điểm thêm nào | • Không có dữ liệu nhập riêng | 1. An vẽ đoạn BD | • Tại chỗ AC cắt BD xuất hiện một điểm mới ở (0,5; 0,5; 0) mang tên "M" (chữ cái in hoa kế tiếp chưa dùng, bắt đầu từ M)<br/>• An chọn được điểm này làm đầu mút cho đoạn tiếp theo | US-GEO-303 · SC2; CL-FN-01; PROVISIONAL D-GEO-118 |
| TC-GEO-0318 | Dựng mặt phẳng qua ba điểm | High | Normal (Success case) | chức năng | đơn vị + thủ công trên thiết bị | Học sinh An, iPad | • Công cụ "Mặt phẳng" | • Không có dữ liệu nhập riêng | 1. An chạm lần lượt S, B, D | • Mặt phẳng (SBD) hiện dạng mặt bán trong suốt qua ba điểm đó | US-GEO-303 · SC3; CL-FN-01 |
| TC-GEO-0319 | Giao tuyến của mặt phẳng mới với mặt của hình | High | Normal (Success case) | chức năng | đơn vị + thủ công trên thiết bị | Học sinh An, iPad | • Mặt phẳng (SBD) vừa dựng | • Không có dữ liệu nhập riêng | 1. Hình vẽ lại | • Giao tuyến của (SBD) với mặt đáy (ABCD) là đoạn BD được vẽ nổi bật | US-GEO-303 · SC4; CL-FN-01 |
| TC-GEO-0320 | Mở rộng một mặt | Medium | Normal (Success case) | chức năng | thủ công trên thiết bị | Học sinh An, iPad | • Công cụ "Mở rộng" | • Không có dữ liệu nhập riêng | 1. An chạm mặt đáy ABCD | • Mặt phẳng chứa ABCD được vẽ rộng ra ngoài bốn cạnh của hình vuông, bán trong suốt | US-GEO-303 · SC5; CL-FN-01 |
| TC-GEO-0321 | Kéo dài một cạnh | Medium | Normal (Success case) | chức năng | thủ công trên thiết bị | Học sinh An, iPad | • Công cụ "Mở rộng" | • Không có dữ liệu nhập riêng | 1. An chạm cạnh SD | • Đường thẳng SD được vẽ kéo dài về cả hai phía qua S và qua D<br/>• Đoạn SD gốc vẫn nét liền | US-GEO-303 · SC6; CL-FN-01 |
| TC-GEO-0322 | Hai đường chéo nhau | High | Abnormal (Fail case/Edge case) | chức năng | đơn vị + thủ công trên thiết bị | Học sinh An, iPad | • An đã kéo dài cạnh SB và cạnh AD (hai đường này chéo nhau trong HKG-01) | • Không có dữ liệu nhập riêng | 1. An yêu cầu giao điểm của hai đường đó bằng cách chạm lần lượt hai đường với công cụ "Đường" | • Hiện "Hai đường thẳng này chéo nhau nên không có giao điểm." (MSG-GEO-23)<br/>• Không có điểm mới nào được tạo | US-GEO-303 · FC1; CL-FN-03 |
| TC-GEO-0323 | Ba điểm thẳng hàng | Medium | Abnormal (Fail case/Edge case) | chức năng | đơn vị + thủ công trên thiết bị | Học sinh An, iPad | • Đã có trung điểm M của SD<br/>• Công cụ "Mặt phẳng" | • Không có dữ liệu nhập riêng | 1. An chạm S, M, D | • Hiện "Ba điểm thẳng hàng nên chưa xác định được mặt phẳng." (MSG-GEO-25)<br/>• Không có mặt phẳng nào được tạo | US-GEO-303 · FC2; CL-FN-03 |
| TC-GEO-0324 | Không có điều khiển kéo mặt phẳng | Medium | Abnormal (Fail case/Edge case) | chức năng | thủ công trên thiết bị | Học sinh An, iPad | • Mặt phẳng (SBD) đã dựng | • Không có dữ liệu nhập riêng | 1. An chạm giữ và kéo mặt phẳng đó | • Mặt phẳng không dịch chuyển và không có tay nắm kéo<br/>• Hình chỉ xoay như bình thường | US-GEO-303 · FC3; CL-UI-03 |

### US-GEO-304 — Thêm điểm: trung điểm, điểm chia tỉ lệ, điểm tự do trên cạnh

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0325 | Thêm trung điểm | High | Normal (Success case) | chức năng | đơn vị + thủ công trên thiết bị | Học sinh An, iPad | • Công cụ "Điểm", chưa có điểm thêm nào | • Hàng dữ liệu: F-GEO-076 | 1. An chọn cạnh SD và chọn "Trung điểm" | • Điểm "M" hiện tại (0; 0,5; 0,5), đúng giữa S và D (F-GEO-076) | US-GEO-304 · SC1; CL-FN-01; PROVISIONAL D-GEO-118 |
| TC-GEO-0326 | Thêm điểm chia tỉ lệ | High | Normal (Success case) | chức năng | đơn vị + thủ công trên thiết bị | Học sinh An, iPad | • Đã có điểm M | • Hàng dữ liệu: F-GEO-076 | 1. An chọn cạnh SD, chọn "Chia tỉ lệ" và nhập 1 : 2 tính từ S | • Điểm "N" hiện tại (0; 1/3; 2/3), với SN bằng một nửa ND (F-GEO-076) | US-GEO-304 · SC2; CL-FN-02; PROVISIONAL D-GEO-118 |
| TC-GEO-0327 | Thêm điểm tự do trên cạnh | Medium | Normal (Success case) | chức năng | thủ công trên thiết bị | Học sinh An, iPad | • Công cụ "Điểm" | • Không có dữ liệu nhập riêng | 1. An chạm vào một vị trí bất kỳ trên cạnh SC và chọn "Điểm tự do" | • Một điểm mới có tên nằm đúng trên cạnh SC tại chỗ chạm (không lệch ra ngoài cạnh) | US-GEO-304 · SC3; CL-FN-01 |
| TC-GEO-0328 | Điểm mới dùng được cho công cụ khác | Medium | Normal (Success case) | chức năng | thủ công trên thiết bị | Học sinh An, iPad | • Đã có trung điểm M của SD | • Không có dữ liệu nhập riêng | 1. An dùng "Đường" nối A với M | • Đoạn AM hiện trên hình | US-GEO-304 · SC4; CL-FN-01 |
| TC-GEO-0329 | Tỉ lệ không hợp lệ | Medium | Abnormal (Fail case/Edge case) | chức năng | đơn vị + thủ công trên thiết bị | Học sinh An, iPad | • An chọn cạnh SD, "Chia tỉ lệ" | • Hàng dữ liệu: F-GEO-076 | 1. Nhập 0 : 2,<br/>2. −1 : 2 (F-GEO-076) | • Mỗi lần hiện "Giá trị phải là số lớn hơn 0." (MSG-GEO-19)<br/>• Không có điểm nào được tạo | US-GEO-304 · FC1; CL-FN-02 |
| TC-GEO-0330 | Điểm trùng điểm đã có | Low | Abnormal (Fail case/Edge case) | chức năng | thủ công trên thiết bị | Học sinh An, iPad | • Đã có trung điểm M của SD | • Không có dữ liệu nhập riêng | 1. An lại chọn cạnh SD và "Trung điểm" | • Hiện "Điểm này đã có trên hình." (MSG-GEO-26)<br/>• Hình vẫn chỉ có một điểm M | US-GEO-304 · FC2; CL-FN-02 |

### US-GEO-305 — Đo độ dài, góc, khoảng cách; ẩn đại lượng đề hỏi

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0331 | Đo độ dài một yếu tố trung gian khi đang khóa | High | Normal (Success case) | chức năng | thủ công trên thiết bị | Học sinh An, iPad | • BG-1 đang Sắp mở<br/>• Công cụ "Đo dài" | • Không có dữ liệu nhập riêng | 1. An chạm S<br/>2. D | • Hiện "SD = a√2" | US-GEO-305 · SC1; CL-FN-01; PROVISIONAL D-GEO-024 |
| TC-GEO-0332 | Đo một góc trung gian khi đang khóa | High | Normal (Success case) | chức năng | thủ công trên thiết bị | Học sinh An, iPad | • BG-1 đang Sắp mở<br/>• Công cụ "Đo góc" | • Không có dữ liệu nhập riêng | 1. An chạm S, A, D (góc tại A) | • Hiện "góc SAD = 90°" | US-GEO-305 · SC2; CL-FN-01; PROVISIONAL D-GEO-024 |
| TC-GEO-0333 | Đo khoảng cách từ điểm tới mặt | Medium | Normal (Success case) | chức năng | thủ công trên thiết bị | Học sinh An, iPad | • BG-1<br/>• Công cụ "Đo dài" | • Không có dữ liệu nhập riêng | 1. An chạm điểm S<br/>2. Chạm mặt đáy ABCD | • Hiện "d(S, (ABCD)) = a" và vẽ đoạn vuông góc từ S xuống đáy (chính là SA) | US-GEO-305 · SC3; CL-FN-01; PROVISIONAL D-GEO-115 |
| TC-GEO-0334 | Sau giờ mở, đại lượng đề hỏi đo được | High | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | Giáo viên mẫu rồi học sinh An | • Giáo viên mẫu đã bấm "Mở ngay" cho BG-1 (BG-1 Đã mở) | • Không có dữ liệu nhập riêng | 1. An dùng "Đo góc" chạm S, D, A (góc tại D) | • Hiện "góc SDA = 45°" | US-GEO-305 · SC4; CL-FN-09; DT-7, cột Đã mở |
| TC-GEO-0335 | Số đo dùng dấu phẩy | Medium | Normal (Success case) | chức năng | thủ công trên thiết bị | Học sinh An, iPad | • Hình nón mẫu với bán kính đáy 2,5 và chiều cao 4 (US-GEO-202) | • Không có dữ liệu nhập riêng | 1. An đo đường sinh bằng "Đo dài" | • Kết quả bắt đầu bằng "4,7" (√22,25 ≈ 4,717), dùng dấu phẩy, không dùng dấu chấm | US-GEO-305 · SC5; CL-FN-12 |
| TC-GEO-0336 | Đo đại lượng đề hỏi khi đang khóa | High | Abnormal (Fail case/Edge case) | chức năng | thủ công trên thiết bị | Học sinh An, iPad | • BG-1 đang Sắp mở | • Không có dữ liệu nhập riêng | 1. An dùng "Đo góc" chạm S, D, A | • Cung góc tại D được vẽ nhưng không có số<br/>• Hiện "Đây là đại lượng đề hỏi. Giá trị sẽ hiện khi lời giải mở." (MSG-GEO-24)<br/>• Chuỗi "45" không xuất hiện ở bất kỳ đâu trên màn | US-GEO-305 · FC1; CL-FN-07; PROVISIONAL D-GEO-024; DT-7, cột Sắp mở; phần dữ liệu trả về kiểm ở TC-GEO-0340 |
| TC-GEO-0337 | Đo đại lượng cùng vai trò khi đang khóa | High | Abnormal (Fail case/Edge case) | chức năng | thủ công trên thiết bị | Học sinh An, iPad | • BG-1 đang Sắp mở | • Không có dữ liệu nhập riêng | 1. An dùng "Đo góc" chạm A, S, D (góc tại S) | • Không có số<br/>• Hiện "Đây là đại lượng đề hỏi. Giá trị sẽ hiện khi lời giải mở." (MSG-GEO-24) | US-GEO-305 · FC2; CL-FN-07; PROVISIONAL D-GEO-024; đại lượng cùng vai trò (góc ASD) |
| TC-GEO-0338 | Bài Chưa đặt giờ cũng ẩn | High | Abnormal (Fail case/Edge case) | chức năng | thủ công trên thiết bị | Học sinh An, iPad | • BG-2 (HKG-02) Chưa đặt giờ<br/>• An mở bài<br/>• Đại lượng đề hỏi là khoảng cách giữa AC và B′D′ | • Không có dữ liệu nhập riêng | 1. An dùng "Đo dài" chạm đường AC<br/>2. Đường B′D′ | • Không có số<br/>• Hiện "Đây là đại lượng đề hỏi. Giá trị sẽ hiện khi lời giải mở." (MSG-GEO-24) | US-GEO-305 · FC3; CL-FN-07; PROVISIONAL D-GEO-043; DT-7, cột Chưa đặt giờ |
| TC-GEO-0339 | Bài tự học đang khóa cũng ẩn | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API + thủ công trên thiết bị | Học sinh An, iPad | • An dán đề HKG-01 vào "Tự học" khi BG-1 đang khóa (F-GEO-062) | • Hàng dữ liệu: F-GEO-062 | 1. An đo góc SDA trên hình của bài tự học đó | • Không có số<br/>• Hiện "Đây là đại lượng đề hỏi. Giá trị sẽ hiện khi lời giải mở." (MSG-GEO-24) | US-GEO-305 · FC4; CL-FN-07; PROVISIONAL D-GEO-024; DT-8 |
| TC-GEO-0340 | Danh sách đại lượng ẩn không mang giá trị | High | Abnormal (Fail case/Edge case) | dữ liệu trả về | tích hợp API | Học sinh An | • BG-1 đang Sắp mở | • Không có dữ liệu nhập riêng | 1. Ghi lại dữ liệu trả về của trang Làm bài | • Phần mô tả đại lượng ẩn chỉ nêu đối tượng (góc tại D tạo bởi S, D, A; góc tại S tạo bởi A, S, D)<br/>• Không có giá trị "45", "45°", "π/4" | US-GEO-305 · FC5; CL-FN-07; PROVISIONAL D-GEO-024; ENG; soát thân phản hồi, không soát màn; tọa độ đỉnh trong dữ liệu trả về là dữ kiện của đề, không tính là lộ; chỉ tính khi hệ thống hiển thị hoặc trả giá trị đáp số (D-GEO-154, RV-GEO-05) |
| TC-GEO-0352 | Bài chuyển Đã mở khi đang mở sẵn công cụ đo | High | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | Giáo viên mẫu rồi học sinh An, iPad | • An đang mở BG-1 (Sắp mở), công cụ "Đo góc" đang bật và đã đo góc SDA (không có số)<br/>• Giáo viên mẫu bấm "Mở ngay" | • Không có dữ liệu nhập riêng | 1. Sau khi trang của An nhận trạng thái Đã mở (không muộn hơn 10 giây, không tải lại), An đo lại góc SDA | • Hiện "góc SDA = 45°" | US-GEO-305 · SC6; CL-FN-09; PROVISIONAL D-GEO-074; hai đầu của quy tắc: giáo viên mở, học sinh đang đo |

### US-GEO-306 — Thao tác trên iPhone: chụm ngón, nút đặt lại, bố cục điện thoại

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0341 | Bố cục điện thoại | High | Normal (Success case) | thiết bị | thủ công trên thiết bị | Học sinh An, iPhone màn dọc | • An mở BG-1 trên iPhone màn dọc (F-GEO-101) | • Hàng dữ liệu: F-GEO-101 | 1. Trang hiện xong | • Hình ở phía trên, thanh công cụ ngay dưới hình, bảng dưới cùng có ba tab "Gợi ý", "Lời giải", "Hỏi"<br/>• Không phải cuộn ngang | US-GEO-306 · SC1; CL-UI-01; PROVISIONAL KG-09 |
| TC-GEO-0342 | Chụm ngón để thu phóng | High | Normal (Success case) | thiết bị | thủ công trên thiết bị | Học sinh An, iPhone màn dọc | • Như SC1 | • Không có dữ liệu nhập riêng | 1. An chụm hai ngón trên hình | • Hình nhỏ lại<br/>• Trang web không bị thu phóng theo | US-GEO-306 · SC2; CL-UI-05; PROVISIONAL KG-09 |
| TC-GEO-0343 | Nút đặt lại | Medium | Normal (Success case) | thiết bị | thủ công trên thiết bị | Học sinh An, iPhone màn dọc | • An đã xoay và phóng to hình trên iPhone | • Không có dữ liệu nhập riêng | 1. Bấm nút "Đặt lại" trên thanh công cụ | • Hình về góc nhìn và mức thu phóng lúc mới mở | US-GEO-306 · SC3; CL-UI-05; PROVISIONAL KG-09 |
| TC-GEO-0344 | Bảng gợi ý trượt lên, xuống | Medium | Normal (Success case) | thiết bị | thủ công trên thiết bị | Học sinh An, iPhone màn dọc | • Bảng dưới đang ở mức thấp | • Không có dữ liệu nhập riêng | 1. An kéo tay nắm của bảng lên,<br/>2. Kéo xuống | • Bảng cao lên che bớt hình để đọc gợi ý<br/>• Kéo xuống thì hình hiện lại đủ và vẫn xoay được | US-GEO-306 · SC4; CL-UI-01; PROVISIONAL KG-09 |
| TC-GEO-0345 | Sáu nút chính và nút "Thêm" trên iPhone | High | Normal (Success case) | thiết bị | thủ công trên thiết bị | Học sinh An, iPhone màn dọc | • Như SC1 | • Không có dữ liệu nhập riêng | 1. An xem thanh công cụ, bấm "Thêm",<br/>2. Tìm các công cụ "Chọn", "Điểm", "Đường", "Mặt phẳng", "Mở rộng", "Đo dài", "Đo góc", "Oxyz", "Đặt lại" và lệnh "Xóa hết" | • Thanh công cụ có sáu nút chính như khung `mvp-08` và nút "Thêm"<br/>• Bấm "Thêm" hiện các công cụ còn lại<br/>• Cả chín công cụ và lệnh "Xóa hết" đều tới được và cho kết quả như trên iPad | US-GEO-306 · SC5; CL-UI-01; PROVISIONAL D-GEO-135 |
| TC-GEO-0346 | Xoay ngang máy giữa chừng | Medium | Abnormal (Fail case/Edge case) | thiết bị | thủ công trên thiết bị | Học sinh An, iPhone | • An đã thêm trung điểm M và đang mở tab "Gợi ý" với Gợi ý 1 đã xem | • Không có dữ liệu nhập riêng | 1. An xoay iPhone sang ngang<br/>2. Xoay lại dọc | • Điểm M, tab đang mở và Gợi ý 1 vẫn còn<br/>• Trang không tải lại | US-GEO-306 · FC1; CL-UI-04; PROVISIONAL D-GEO-117; PROVISIONAL KG-09 |
| TC-GEO-0351 | Vùng chạm đủ lớn | Medium | Abnormal (Fail case/Edge case) | thiết bị | thủ công trên thiết bị | Học sinh An, iPhone màn dọc | • An mở BG-1 trên iPhone màn dọc | • Không có dữ liệu nhập riêng | 1. Đo vùng chạm của từng nút trên thanh công cụ và ba tab của bảng dưới | • Mỗi vùng chạm có chiều rộng và chiều cao không nhỏ hơn 44 điểm | US-GEO-306 · FC2; CL-UI-05; PROVISIONAL D-GEO-117; PROVISIONAL KG-09 |

### US-GEO-307 — Sửa lỗi hình đã biết

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0347 | Tên mặt không lặp chữ | Medium | Normal (Success case) | chức năng | thủ công trên thiết bị | Học sinh An, iPad | • Hình HKG-01<br/>• Công cụ "Chọn" | • Không có dữ liệu nhập riêng | 1. An chạm mặt SAB | • Tên hiện là "Mặt SAB"<br/>• Chuỗi "Mặt Mặt" không xuất hiện ở đâu trên màn | US-GEO-307 · SC1; CL-FN-01; hồi quy lỗi đã biết của bản cũ |
| TC-GEO-0348 | Mặt bốn đỉnh tô kín | Medium | Normal (Success case) | chức năng | đơn vị + thủ công trên thiết bị | Học sinh An, iPad | • Hình HKG-01 | • Không có dữ liệu nhập riêng | 1. An chọn mặt đáy ABCD | • Vùng tô phủ đúng toàn bộ hình vuông ABCD, không hở tam giác nào và không tràn ra ngoài bốn cạnh | US-GEO-307 · SC2; CL-FN-01; hồi quy lỗi đã biết |
| TC-GEO-0349 | Hai đường chéo nhau trên hình lập phương | Medium | Abnormal (Fail case/Edge case) | chức năng | thủ công trên thiết bị | Học sinh An, iPad | • Hình HKG-02 (lập phương ABCD.A′B′C′D′)<br/>• An đã vẽ AC và B′D′ | • Không có dữ liệu nhập riêng | 1. An yêu cầu giao điểm của AC và B′D′ | • Hiện "Hai đường thẳng này chéo nhau nên không có giao điểm." (MSG-GEO-23)<br/>• Không có điểm nào được tạo ở khoảng giữa hai đường | US-GEO-307 · FC1; CL-FN-03; hồi quy lỗi đã biết |
| TC-GEO-0350 | Mặt lõm | Low | Abnormal (Fail case/Edge case) | chức năng | đơn vị + thủ công trên thiết bị | Học sinh An, iPad | • Một lăng trụ đứng có đáy chữ L với sáu đỉnh đáy theo thứ tự (0,0), (2,0), (2,1), (1,1), (1,2), (0,2) | • Không có dữ liệu nhập riêng | 1. Chọn mặt đáy | • Vùng tô đúng hình chữ L (diện tích 3)<br/>• Ô vuông lõm có góc (1,1) và (2,2) không bị tô | US-GEO-307 · FC2; CL-FN-03 |

## 3. Đối chiếu hai chiều AC ↔ TC

### 3.1 Chiều xuôi: tiêu chí → case (54 / 54 tiêu chí còn hiệu lực có case, 100.0%)

| User story | Criterion ID | Kind | What it checks | Status | Test cases |
|---|---|---|---|---|---|
| US-GEO-301 | US-GEO-301-SC1 | Success | Kéo để xoay | Active | TC-GEO-0300 |
| US-GEO-301 | US-GEO-301-SC2 | Success | Chụm ngón để thu phóng trên iPad | Active | TC-GEO-0301 |
| US-GEO-301 | US-GEO-301-SC3 | Success | Lăn chuột để thu phóng trên máy tính | Active | TC-GEO-0302 |
| US-GEO-301 | US-GEO-301-SC4 | Success | "Nhìn từ trên" | Active | TC-GEO-0303 |
| US-GEO-301 | US-GEO-301-SC5 | Success | "Nhìn từ trước" | Active | TC-GEO-0304 |
| US-GEO-301 | US-GEO-301-SC6 | Success | "Đặt lại" về góc nhìn ban đầu | Active · PROVISIONAL | TC-GEO-0305 |
| US-GEO-301 | US-GEO-301-SC7 | Success | Cạnh khuất vẽ nét đứt và tính lại khi xoay | Active | TC-GEO-0306 |
| US-GEO-301 | US-GEO-301-SC8 | Success | Bật, tắt trục Oxyz | Active | TC-GEO-0307 |
| US-GEO-301 | US-GEO-301-SC9 | Success | "Xóa hết" xóa phần vẽ thêm, giữ hình gốc và góc nhìn | Active · PROVISIONAL | TC-GEO-0353 |
| US-GEO-301 | US-GEO-301-FC1 | Failure | Thu phóng có giới hạn | Active · PROVISIONAL | TC-GEO-0308 |
| US-GEO-301 | US-GEO-301-FC2 | Failure | Xoay và thu phóng không tính là thao tác trên hình | Active · PROVISIONAL | TC-GEO-0309 |
| US-GEO-302 | US-GEO-302-SC1 | Success | Chạm chọn một cạnh | Active | TC-GEO-0310 |
| US-GEO-302 | US-GEO-302-SC2 | Success | Chạm chọn một mặt | Active | TC-GEO-0311 |
| US-GEO-302 | US-GEO-302-SC3 | Success | Chạm chọn một điểm | Active | TC-GEO-0312 |
| US-GEO-302 | US-GEO-302-SC4 | Success | Mỗi lần chọn là một thao tác trên hình | Active · PROVISIONAL | TC-GEO-0313 |
| US-GEO-302 | US-GEO-302-FC1 | Failure | Chạm vào chỗ chồng nhau | Active · PROVISIONAL | TC-GEO-0314 |
| US-GEO-302 | US-GEO-302-FC2 | Failure | Chạm ra ngoài hình | Active · PROVISIONAL | TC-GEO-0315 |
| US-GEO-303 | US-GEO-303-SC1 | Success | Vẽ đoạn nối hai điểm | Active | TC-GEO-0316 |
| US-GEO-303 | US-GEO-303-SC2 | Success | Giao điểm của hai đường cắt nhau có tên | Active · PROVISIONAL | TC-GEO-0317 |
| US-GEO-303 | US-GEO-303-SC3 | Success | Dựng mặt phẳng qua ba điểm | Active | TC-GEO-0318 |
| US-GEO-303 | US-GEO-303-SC4 | Success | Giao tuyến của mặt phẳng mới với mặt của hình | Active | TC-GEO-0319 |
| US-GEO-303 | US-GEO-303-SC5 | Success | Mở rộng một mặt | Active | TC-GEO-0320 |
| US-GEO-303 | US-GEO-303-SC6 | Success | Kéo dài một cạnh | Active | TC-GEO-0321 |
| US-GEO-303 | US-GEO-303-FC1 | Failure | Hai đường chéo nhau | Active | TC-GEO-0322 |
| US-GEO-303 | US-GEO-303-FC2 | Failure | Ba điểm thẳng hàng | Active | TC-GEO-0323 |
| US-GEO-303 | US-GEO-303-FC3 | Failure | Không có điều khiển kéo mặt phẳng | Active | TC-GEO-0324 |
| US-GEO-304 | US-GEO-304-SC1 | Success | Thêm trung điểm | Active · PROVISIONAL | TC-GEO-0325 |
| US-GEO-304 | US-GEO-304-SC2 | Success | Thêm điểm chia tỉ lệ | Active · PROVISIONAL | TC-GEO-0326 |
| US-GEO-304 | US-GEO-304-SC3 | Success | Thêm điểm tự do trên cạnh | Active | TC-GEO-0327 |
| US-GEO-304 | US-GEO-304-SC4 | Success | Điểm mới dùng được cho công cụ khác | Active | TC-GEO-0328 |
| US-GEO-304 | US-GEO-304-FC1 | Failure | Tỉ lệ không hợp lệ | Active | TC-GEO-0329 |
| US-GEO-304 | US-GEO-304-FC2 | Failure | Điểm trùng điểm đã có | Active | TC-GEO-0330 |
| US-GEO-305 | US-GEO-305-SC1 | Success | Đo độ dài một yếu tố trung gian khi đang khóa | Active · PROVISIONAL | TC-GEO-0331 |
| US-GEO-305 | US-GEO-305-SC2 | Success | Đo một góc trung gian khi đang khóa | Active · PROVISIONAL | TC-GEO-0332 |
| US-GEO-305 | US-GEO-305-SC3 | Success | Đo khoảng cách từ điểm tới mặt | Active · PROVISIONAL | TC-GEO-0333 |
| US-GEO-305 | US-GEO-305-SC4 | Success | Sau giờ mở, đại lượng đề hỏi đo được | Active | TC-GEO-0334 |
| US-GEO-305 | US-GEO-305-SC5 | Success | Số đo dùng dấu phẩy | Active | TC-GEO-0335 |
| US-GEO-305 | US-GEO-305-SC6 | Success | Bài chuyển Đã mở khi đang mở sẵn công cụ đo | Active · PROVISIONAL | TC-GEO-0352 |
| US-GEO-305 | US-GEO-305-FC1 | Failure | Đo đại lượng đề hỏi khi đang khóa | Active · PROVISIONAL | TC-GEO-0336 |
| US-GEO-305 | US-GEO-305-FC2 | Failure | Đo đại lượng cùng vai trò khi đang khóa | Active · PROVISIONAL | TC-GEO-0337 |
| US-GEO-305 | US-GEO-305-FC3 | Failure | Bài Chưa đặt giờ cũng ẩn | Active · PROVISIONAL | TC-GEO-0338 |
| US-GEO-305 | US-GEO-305-FC4 | Failure | Bài tự học đang khóa cũng ẩn | Active · PROVISIONAL | TC-GEO-0339 |
| US-GEO-305 | US-GEO-305-FC5 | Failure | Danh sách đại lượng ẩn không mang giá trị | Active · PROVISIONAL | TC-GEO-0340 |
| US-GEO-306 | US-GEO-306-SC1 | Success | Bố cục điện thoại | Active | TC-GEO-0341 |
| US-GEO-306 | US-GEO-306-SC2 | Success | Chụm ngón để thu phóng | Active | TC-GEO-0342 |
| US-GEO-306 | US-GEO-306-SC3 | Success | Nút đặt lại | Active | TC-GEO-0343 |
| US-GEO-306 | US-GEO-306-SC4 | Success | Bảng gợi ý trượt lên, xuống | Active | TC-GEO-0344 |
| US-GEO-306 | US-GEO-306-SC5 | Success | Sáu nút chính và nút "Thêm" trên iPhone | Active · PROVISIONAL | TC-GEO-0345 |
| US-GEO-306 | US-GEO-306-FC1 | Failure | Xoay ngang máy giữa chừng | Active · PROVISIONAL | TC-GEO-0346 |
| US-GEO-306 | US-GEO-306-FC2 | Failure | Vùng chạm đủ lớn | Active · PROVISIONAL | TC-GEO-0351 |
| US-GEO-307 | US-GEO-307-SC1 | Success | Tên mặt không lặp chữ | Active | TC-GEO-0347 |
| US-GEO-307 | US-GEO-307-SC2 | Success | Mặt bốn đỉnh tô kín | Active | TC-GEO-0348 |
| US-GEO-307 | US-GEO-307-FC1 | Failure | Hai đường chéo nhau trên hình lập phương | Active | TC-GEO-0349 |
| US-GEO-307 | US-GEO-307-FC2 | Failure | Mặt lõm | Active | TC-GEO-0350 |

### 3.2 Chiều ngược: case → tiêu chí hoặc NFR (54 / 54 case trỏ về một tiêu chí của BA hoặc một NFR-GEO, 100.0%; riêng trỏ về tiêu chí của BA: 54 / 54, 100.0%)

Kiểm bằng `gen_cases.py`: mỗi case bám tiêu chí lấy đúng một tiêu chí từ bảng phân loại; case viết tay ghi tiêu chí ở Remark và bộ sinh từ chối id tiêu chí không tồn tại.

### 3.3 NFR-GEO được case trong tệp này nhắc tới

NFR-GEO-008

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
| 1 | bản đầu của goal run: 53 case cho 53 tiêu chí | QC |
| 2 | lượt sửa gom sau phản biện (M-304): TC-GEO-0340 ghi rõ tọa độ đỉnh là dữ kiện của đề (D-GEO-154, RV-GEO-05): 54 case cho 54 tiêu chí | QC |
| 3 | đồng bộ với bàn giao của BA (M-217, M-218): case viết tay trùng tiêu chí mới chuyển thành case bám tiêu chí (giữ id), thêm case cho mọi tiêu chí mới, gỡ chỉnh không còn cần: 54 case cho 54 tiêu chí | QC |
