# E7 — test case (Theo dõi lớp và ghi sự kiện)

> Chủ sở hữu: QC. **Tệp sinh ra, không sửa tay**: sửa `work/src/qc/E7_meta.txt` (phân loại từng tiêu chí), `work/src/qc/E7_extra.md` (case viết tay) hoặc `work/src/qc/E7_override.txt` (chỉnh của QC theo một D-GEO khi AC chưa kịp đổi; mỗi chỉnh ghi nguồn ở Remark) rồi chạy `python3 work/src/qc/gen_cases.py E7`. Tiền điều kiện, bước và kỳ vọng của case bám tiêu chí lấy nguyên từ Given / When / Then của `stories_ac.md` (BA); giá trị cụ thể là của `work/fixture.md` và công thức M-GEO. Id ổn định; tiêu chí bị gạch bỏ thì case giữ id với N/A. AI ở MVP là giả lập (D-GEO-047): không có kỳ vọng về chất lượng AI thật.

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
| `M-GEO-nn` | công thức chỉ số, một công thức mỗi con số (`work/02_domain_model.md` §3) | cột Expected output |
| `ADR-GEO-nn` | quyết định kiến trúc (`work/architecture/01_adr.md`) | cột Remark |
| `CL-FN-nn` | nhóm kiểm tra chức năng (`review/QC_test_design.md` §1) | cột Remark |
| `CL-UI-nn` | nhóm kiểm tra giao diện và thiết bị | cột Remark |
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
| US-GEO-701 Theo dõi lớp: ai đã mở bài, tình trạng | 14 (7 / 7) | 14 | 10 |
| US-GEO-702 Theo dõi lớp: mức gợi ý và câu hỏi của từng học sinh | 8 (5 / 3) | 8 | 3 |
| US-GEO-703 Ghi sự kiện tương tác | 14 (9 / 5) | 15 | 10 |
| US-GEO-704 Dữ liệu: bảng sự kiện chia theo thời gian và phân trang theo khóa | 7 (4 / 3) | 8 | 1 |
| **Tổng** | **43** | **45** | **24** |

| Theo ưu tiên | Số case |
|---|---|
| High | 19 |
| Medium | 23 |
| Low | 3 |

| Theo loại | Số case |
|---|---|
| chức năng | 32 |
| phân quyền | 4 |
| dữ liệu trả về | 1 |
| phi chức năng | 6 |
| thiết bị | 2 |

| Theo tầng tự động hóa (một case có thể ở hai tầng) | Số case |
|---|---|
| đơn vị | 6 |
| tích hợp API | 26 |
| tích hợp CSDL | 7 |
| E2E trình duyệt | 0 |
| tải | 0 |
| thủ công trên thiết bị | 18 |

| Theo Tự động hóa (một case một loại) | Số case |
|---|---|
| E2E-UI | 18 |
| E2E-API | 16 |
| IT | 11 |
| Manual | 0 |
| **Tổng** | **45** |

## 2. Test case

### Cách đọc một test case
- Mỗi dòng là một case. Chuẩn bị theo **Pre-condition**, dùng giá trị ở **Test data**, làm lần lượt các bước đánh số ở **Test step**, rồi so với **Expected output**.
- **Expected output**: mỗi gạch đầu dòng là một điều phải đúng; chữ trong ngoặc kép và giá trị phải khớp nguyên văn. Case đạt khi mọi gạch đầu dòng đều đúng.
- **Loại**: chức năng · phân quyền · dữ liệu trả về (soát thân phản hồi của máy chủ, không chỉ giao diện) · phi chức năng · thiết bị.
- **Tầng tự động hóa**: đơn vị · tích hợp API · tích hợp CSDL · E2E trình duyệt (Playwright) · tải (Gatling) · thủ công trên thiết bị; đếm câu lệnh JDBC dùng datasource-proxy (D-GEO-170). Case có chữ ENG ở Remark cần công cụ kỹ thuật và thuộc tờ kỹ thuật; case còn lại người kiểm thử chạy trên màn.
- **Tự động hóa** (PO-50): cách tự động hóa chính của case, đầu vào của bộ E2E. `E2E-UI` · Playwright qua trình duyệt, theo đúng bước trên màn và kỳ vọng nhìn thấy (phần "máy chủ trả …" của kỳ vọng thì soát thêm qua phản hồi mạng mà trang nhận) · `E2E-API` · gọi HTTP thẳng tới máy chủ, soát `code` và thân phản hồi · `IT` · kiểm thử tích hợp phía máy chủ (CSDL, bộ quét, tầng dịch vụ) · `Manual` · người chạy, lý do ghi ngay sau.
- **PROVISIONAL**: kỳ vọng theo một giả định chưa được xác nhận, mã ghi ngay sau. Vẫn chạy; nếu màn chỉ khác ở đúng điểm đó thì ghi lại điều thấy và đánh dấu Blocked, không đánh Fail.
- Thực tế khác kỳ vọng thì ghi vào vòng thực thi, không sửa ô Expected.

### US-GEO-701 — Theo dõi lớp: ai đã mở bài, tình trạng

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0700 | Bốn thẻ tổng | High | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Giáo viên mẫu, máy tính | • Dữ liệu F-GEO-050…054 trên BG-1 | • Hàng dữ liệu: F-GEO-050…054 | 1. Giáo viên mẫu mở Theo dõi lớp của BG-1 | • Bốn thẻ ghi "4/5 đã mở bài", "3 dùng gợi ý", "7 câu hỏi", "12′ thao tác hình" ((18 + 11 + 9 + 21 + 0) / 5 = 11,8 → 12) | US-GEO-701 · SC1; CL-FN-01; PROVISIONAL D-GEO-077; bộ số của khung `mvp-03` |
| TC-GEO-0701 | Bảng học sinh | High | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Giáo viên mẫu | • Như SC1 | • Không có dữ liệu nhập riêng | 1. Xem bảng "Học sinh" | • Năm dòng theo thứ tự An, Bình, Chi, Dũng, Hà<br/>• Cột "Mở bài" ghi "Đã mở bài" cho bốn em đầu và "Chưa mở" cho Hà<br/>• Cột "Thời gian" ghi "18 phút", "11 phút", "9 phút", "21 phút", "—" | US-GEO-701 · SC2; CL-FN-12; PROVISIONAL D-GEO-121 |
| TC-GEO-0702 | Tình trạng khi bài còn khóa | High | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Giáo viên mẫu | • Như SC1<br/>• BG-1 Sắp mở | • Không có dữ liệu nhập riêng | 1. Xem cột "Tình trạng" | • An "Đang chờ lời giải"<br/>• Bình "Đang làm"<br/>• Chi "Tự làm, không cần gợi ý"<br/>• Dũng "Đang chờ lời giải"<br/>• Hà "Chưa bắt đầu" | US-GEO-701 · SC3; CL-FN-04; PROVISIONAL D-GEO-077 |
| TC-GEO-0703 | Tình trạng sau khi mở lời giải | Medium | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Giáo viên mẫu | • Giáo viên mẫu đã mở ngay BG-1<br/>• An đã xem lời giải<br/>• Dũng chưa xem | • Không có dữ liệu nhập riêng | 1. Tải lại Theo dõi lớp của BG-1 | • An "Đã xem lời giải"<br/>• Dũng "Đang làm"<br/>• Ba em còn lại như SC3 | US-GEO-701 · SC4; CL-FN-04; PROVISIONAL D-GEO-102; nhãn thứ năm "Đã xem lời giải" |
| TC-GEO-0704 | Thẻ vàng khi lời giải khóa | Medium | Normal (Success case) | chức năng | thủ công trên thiết bị | E2E-UI | Giáo viên mẫu | • BG-1 Sắp mở (mở lúc T0 + 60 phút)<br/>• BG-2 Chưa đặt giờ<br/>• BG-3 Đã mở | • Không có dữ liệu nhập riêng | 1. Lần lượt mở Theo dõi lớp của ba bài tại T0 | • BG-1 có thẻ vàng "Lời giải đang khóa", dòng phụ "Mở lúc" kèm giờ và "còn 1 giờ 0 phút", nút "Mở ngay", "Đổi giờ"<br/>• BG-2 có thẻ vàng với dòng phụ "Chưa đặt giờ mở" và hai nút<br/>• BG-3 không có thẻ vàng | US-GEO-701 · SC5; CL-FN-01 |
| TC-GEO-0705 | Vào trang từ mục "Theo dõi" | Medium | Normal (Success case) | chức năng | thủ công trên thiết bị | E2E-UI | Giáo viên mẫu | • Giáo viên mẫu có năm bài giao BG-1…BG-5 | • Không có dữ liệu nhập riêng | 1. Bấm "Theo dõi" ở thanh bên | • Danh sách năm bài giao hiện tên bài, tên lớp, trạng thái giờ mở, bài mới giao ở trên<br/>• Bấm BG-1 thì mở Theo dõi lớp của BG-1<br/>• Không có bài của lớp 9C | US-GEO-701 · SC6; CL-FN-01; PROVISIONAL D-GEO-129 |
| TC-GEO-0706 | Xem nhanh trên điện thoại | Medium | Normal (Success case) | thiết bị | thủ công trên thiết bị | E2E-UI | Giáo viên mẫu, điện thoại | • Giáo viên mẫu trên điện thoại (F-GEO-103) | • Hàng dữ liệu: F-GEO-103 | 1. Mở Theo dõi lớp của BG-1 | • Bốn thẻ, thẻ vàng và năm dòng học sinh đều đọc được không phải cuộn ngang trang<br/>• Nút "Mở ngay" bấm được | US-GEO-701 · SC7; CL-UI-01; PROVISIONAL KG-09 |
| TC-GEO-0707 | Chưa em nào mở bài | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Giáo viên mẫu | • Một bài vừa giao cho 11A1, chưa học sinh nào mở | • Không có dữ liệu nhập riêng | 1. Mở Theo dõi lớp của bài đó | • Thẻ ghi "0/5", "0", "0", "0′"<br/>• Bảng vẫn có năm dòng, mỗi dòng "Chưa mở", "—", "—", "Chưa bắt đầu"<br/>• Phía trên bảng có "Chưa có học sinh nào mở bài." (MSG-GEO-52) | US-GEO-701 · FC1; CL-FN-12; BVA: lớp chưa có sự kiện |
| TC-GEO-0708 | Không có xếp hạng, không sắp theo kết quả | Medium | Abnormal (Fail case/Edge case) | chức năng | thủ công trên thiết bị | E2E-UI | Giáo viên mẫu | • Theo dõi lớp của BG-1 | • Không có dữ liệu nhập riêng | 1. Bấm vào tiêu đề các cột "Thời gian", "Gợi ý 1 · 2", "Hỏi đáp", "Tình trạng" | • Thứ tự dòng không đổi (vẫn A → Z)<br/>• Không có số thứ hạng, huy hiệu, điểm hay so sánh giữa học sinh | US-GEO-701 · FC2; CL-UI-03; PROVISIONAL D-GEO-022 |
| TC-GEO-0709 | Không có "Nên nhắc lại trên lớp" | Medium | Abnormal (Fail case/Edge case) | chức năng | thủ công trên thiết bị | E2E-UI | Giáo viên mẫu | • Theo dõi lớp của BG-1 | • Không có dữ liệu nhập riêng | 1. Xem cột bên phải | • Có thẻ vàng và khối "Học sinh đang hỏi gì"<br/>• Không có thẻ "Nên nhắc lại trên lớp" hay câu nhận định nào do máy tự viết | US-GEO-701 · FC3; CL-UI-03; PROVISIONAL D-GEO-078 |
| TC-GEO-0710 | Học sinh gọi Theo dõi lớp | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Học sinh An | • Phiên của An | • Không có dữ liệu nhập riêng | 1. Gọi thẳng trang Theo dõi lớp của BG-1 | • `FORBIDDEN` ("Bạn không có quyền thực hiện thao tác này." (MSG-GEO-03))<br/>• Không có tên, thời gian hay câu hỏi của bạn nào trong dữ liệu trả về | US-GEO-701 · FC4; CL-FN-05; ENG |
| TC-GEO-0711 | Trang không mang lời giải | High | Abnormal (Fail case/Edge case) | dữ liệu trả về | tích hợp API | E2E-API | Giáo viên mẫu | • BG-1 Sắp mở | • Hàng dữ liệu: F-GEO-037 | 1. Ghi lại dữ liệu trả về của Theo dõi lớp của BG-1 | • Không có bước giải F-GEO-037 và không có đáp số "45°" | US-GEO-701 · FC5; CL-FN-07; PROVISIONAL D-GEO-027; ENG; DT-9 |
| TC-GEO-0712 | Trang không đọc bảng sự kiện, không truy vấn lặp theo học sinh | High | Abnormal (Fail case/Edge case) | phi chức năng | tích hợp CSDL + tích hợp API | IT | Kỹ thuật (bộ đếm câu lệnh) | • Dữ liệu thử tải F-GEO-097<br/>• Một lớp 5 học sinh và một lớp 34 học sinh | • Hàng dữ liệu: F-GEO-097 | 1. Mở Theo dõi lớp của một bài ở mỗi lớp, có công cụ đếm câu lệnh cơ sở dữ liệu | • Hai lần dùng cùng số câu lệnh<br/>• Không câu lệnh nào đọc bảng sự kiện | US-GEO-701 · FC6; CL-X-02; ENG; công cụ: datasource-proxy (D-GEO-170) |
| TC-GEO-0744 | Thanh bên không có "Tổng quan" và "Lớp học" | Medium | Abnormal (Fail case/Edge case) | chức năng | thủ công trên thiết bị | E2E-UI | Giáo viên mẫu, máy tính | • Giáo viên mẫu đã vào bằng mã của mình, trên máy tính | • Không có dữ liệu nhập riêng | 1. Xem toàn bộ thanh bên | • Có mục "Theo dõi"<br/>• Không có mục "Tổng quan" và không có mục "Lớp học" | US-GEO-701 · FC7; CL-UI-03; PROVISIONAL D-GEO-129; D-GEO-129 |

### US-GEO-702 — Theo dõi lớp: mức gợi ý và câu hỏi của từng học sinh

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0713 | Mức gợi ý của từng em | High | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Giáo viên mẫu | • Dữ liệu F-GEO-050…054 trên BG-1 | • Hàng dữ liệu: F-GEO-050…054 | 1. Xem cột "Gợi ý 1 · 2" | • An hai đèn sáng<br/>• Bình một đèn<br/>• Chi không đèn<br/>• Dũng hai đèn<br/>• Hà không đèn | US-GEO-702 · SC1; CL-FN-01 |
| TC-GEO-0714 | Số câu hỏi của từng em | Medium | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Giáo viên mẫu | • Như SC1 | • Không có dữ liệu nhập riêng | 1. Xem cột "Hỏi đáp" | • An "3 câu"<br/>• Bình "2 câu"<br/>• Chi "—"<br/>• Dũng "2 câu"<br/>• Hà "—" | US-GEO-702 · SC2; CL-FN-01 |
| TC-GEO-0715 | Câu hỏi của một em | Medium | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Giáo viên mẫu | • Như SC1 | • Hàng dữ liệu: F-GEO-055 | 1. Bấm "3 câu" ở dòng An | • Hiện ba câu của An trên BG-1 (F-GEO-055, 056, 057), câu mới nhất ở trên<br/>• Không có câu của An trên bài khác | US-GEO-702 · SC3; CL-FN-01 |
| TC-GEO-0716 | Khối "Học sinh đang hỏi gì" gộp câu trùng | Medium | Normal (Success case) | chức năng | tích hợp API + thủ công trên thiết bị | E2E-UI | Giáo viên mẫu | • Bảy câu hỏi của lớp trên BG-1 (F-GEO-055…058) | • Hàng dữ liệu: F-GEO-055…058 | 1. Xem khối "Học sinh đang hỏi gì" | • Có bốn nhóm: "Vì sao A là hình chiếu của S?" · An, Dũng · 2 lượt<br/>• "Góc cần tìm là góc nào trên hình?" · Bình, An · 3 lượt<br/>• "Cho em xin đáp số luôn được không?" · An · 1 lượt<br/>• "Tan của một góc là gì?" · Dũng · 1 lượt | US-GEO-702 · SC4; CL-FN-01; PROVISIONAL D-GEO-116 |
| TC-GEO-0717 | Gộp không phân biệt hoa thường và khoảng trắng | Low | Normal (Success case) | chức năng | đơn vị | IT | Kỹ thuật | • Như SC4<br/>• Chi hỏi thêm "vì sao  A là hình chiếu của S? " (chữ đầu viết thường, thừa khoảng trắng) | • Không có dữ liệu nhập riêng | 1. Tải lại khối | • Nhóm "Vì sao A là hình chiếu của S?" ghi An, Dũng, Chi · 3 lượt<br/>• Không có nhóm mới | US-GEO-702 · SC5; CL-FN-02; PROVISIONAL D-GEO-116; ENG |
| TC-GEO-0718 | Chưa ai hỏi | Low | Abnormal (Fail case/Edge case) | chức năng | thủ công trên thiết bị | E2E-UI | Giáo viên mẫu | • Một bài chưa có câu hỏi nào (BG-2) | • Không có dữ liệu nhập riêng | 1. Mở Theo dõi lớp của bài đó | • Khối "Học sinh đang hỏi gì" hiện "Chưa có câu hỏi nào." (MSG-GEO-53)<br/>• Mọi dòng ở cột "Hỏi đáp" ghi "—" | US-GEO-702 · FC1; CL-FN-12 |
| TC-GEO-0719 | Không lẫn câu hỏi của bài khác, lớp khác | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Giáo viên mẫu | • An có một câu hỏi trên BG-3<br/>• Khánh (9B) có một câu hỏi trên BG-4 | • Không có dữ liệu nhập riêng | 1. Mở Theo dõi lớp của BG-1 | • Thẻ "câu hỏi" vẫn là 7<br/>• Khối câu hỏi không có hai câu kia | US-GEO-702 · FC2; CL-FN-05; ENG |
| TC-GEO-0720 | Không có nhận định do AI viết | Medium | Abnormal (Fail case/Edge case) | chức năng | thủ công trên thiết bị | E2E-UI | Giáo viên mẫu | • Như SC4<br/>• Số bản ghi lượt gọi AI là n | • Không có dữ liệu nhập riêng | 1. Mở Theo dõi lớp của BG-1 | • Khối chỉ có câu hỏi nguyên văn của học sinh, tên và số lượt<br/>• Số bản ghi lượt gọi AI vẫn là n | US-GEO-702 · FC3; CL-FN-14; PROVISIONAL D-GEO-116 |

### US-GEO-703 — Ghi sự kiện tương tác

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0721 | Mở bài tạo sự kiện mở bài | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Học sinh Hà | • Hà chưa mở BG-1<br/>• Theo dõi lớp ghi "4/5 đã mở bài" | • Không có dữ liệu nhập riêng | 1. Hà mở BG-1 và chờ 10 giây | • Theo dõi lớp ghi "5/5 đã mở bài"<br/>• Dòng Hà ghi "Đã mở bài" và tình trạng "Tự làm, không cần gợi ý" | US-GEO-703 · SC1; CL-FN-13; ENG |
| TC-GEO-0722 | Mỗi công cụ là một thao tác trên hình | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Học sinh Hà | • Hà vừa mở BG-1, 0 thao tác | • Không có dữ liệu nhập riêng | 1. Hà dùng mỗi công cụ "Chọn", "Điểm", "Đường", "Mặt phẳng", "Mở rộng", "Đo dài", "Đo góc" đúng một lần | • Số thao tác trên hình của Hà trên BG-1 là 7 | US-GEO-703 · SC2; CL-FN-13; PROVISIONAL D-GEO-058; ENG |
| TC-GEO-0723 | Sự kiện gợi ý ghi bền | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Học sinh Hà | • Hà bấm "Xem gợi ý 1" trên BG-1 và đã thấy nội dung gợi ý | • Không có dữ liệu nhập riêng | 1. Máy chủ ứng dụng bị khởi động lại ngay sau đó<br/>2. Hà tải lại bài | • Tiến độ của Hà vẫn 1/3 và Gợi ý 1 vẫn hiện<br/>• Theo dõi lớp vẫn tính Hà vào "dùng gợi ý" | US-GEO-703 · SC3; CL-FN-13; ENG |
| TC-GEO-0724 | Tỉ lệ phiên có thao tác trên hình | Medium | Normal (Success case) | chức năng | đơn vị | IT | Kỹ thuật | • Dữ liệu F-GEO-050…053: bốn phiên làm bài của An, Bình, Chi, Dũng, phiên nào cũng có thao tác<br/>• Thêm một phiên của Hà chỉ xoay hình | • Hàng dữ liệu: F-GEO-050…053 | 1. Tính M-GEO-08 trước và sau phiên của Hà | • Trước: 4/4 = 100,0%<br/>• Sau: 4/5 = 80,0%<br/>• Báo cáo ghi "ngưỡng đề xuất ≥ 60,0%" | US-GEO-703 · SC4; CL-FN-12; PROVISIONAL D-GEO-059; ENG; BVA của M-GEO-08: 4/4 và 4/5 |
| TC-GEO-0725 | Ranh giới phiên làm bài | Medium | Normal (Success case) | chức năng | đơn vị | IT | Kỹ thuật | • Một học sinh có sự kiện trên một bài tại T0, T0 + 10 phút và T0 + 45 phút | • Không có dữ liệu nhập riêng | 1. Tính phiên và thời gian | • Có hai phiên (khoảng lặng 35 phút lớn hơn 30 phút)<br/>• Thời gian của em trên bài là 10 phút | US-GEO-703 · SC5; CL-FN-09; PROVISIONAL D-GEO-059; ENG; biên 29 phút 59 giây và 30 phút 0 giây ở TC-GEO-0740, 0741 (D-GEO-144) |
| TC-GEO-0726 | Bài tự học cũng ghi sự kiện | Medium | Normal (Success case) | chức năng | tích hợp API | E2E-API | Học sinh An | • An làm bài tự học từ đề F-GEO-060 | • Hàng dữ liệu: F-GEO-060 | 1. An mở bài, dùng "Đo dài" một lần, xem Gợi ý 1, Gợi ý 2, mở lời giải | • Có một sự kiện mở bài, một thao tác trên hình, hai sự kiện gợi ý và một sự kiện mở lời giải gắn với bài tự học đó<br/>• Các sự kiện này không xuất hiện ở Theo dõi lớp của bài giao nào | US-GEO-703 · SC6; CL-FN-13; ENG |
| TC-GEO-0727 | Công cụ không tính | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Học sinh Hà | • Hà vừa mở BG-1, 0 thao tác | • Không có dữ liệu nhập riêng | 1. Hà bật "Oxyz", bấm "Nhìn từ trên", "Đặt lại", xoay và thu phóng | • Số thao tác trên hình của Hà vẫn là 0 | US-GEO-703 · FC1; CL-FN-13; PROVISIONAL D-GEO-058; ENG |
| TC-GEO-0728 | Gửi lại cùng một lô | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Học sinh Hà | • Thiết bị của Hà gửi một lô gồm 3 thao tác trên hình và nhận lỗi mạng trước khi có phản hồi | • Không có dữ liệu nhập riêng | 1. Thiết bị gửi lại đúng lô đó | • Số thao tác trên hình của Hà tăng 3, không tăng 6 | US-GEO-703 · FC2; CL-FN-10; PROVISIONAL D-GEO-125; ENG |
| TC-GEO-0729 | Mất mạng khi đang thao tác | Medium | Abnormal (Fail case/Edge case) | thiết bị | thủ công trên thiết bị | E2E-UI | Học sinh Hà, iPad | • Hà đang làm BG-1<br/>• Mạng mất 20 giây, trong lúc đó Hà dùng "Điểm" hai lần<br/>• Trang vẫn mở | • Không có dữ liệu nhập riêng | 1. Mạng có lại | • Hình và hai điểm mới vẫn còn trên màn<br/>• Hai thao tác đó được gửi và tính trong vòng 10 giây sau khi có mạng<br/>• Hà không thấy thông báo lỗi nào chặn việc đang làm | US-GEO-703 · FC3; CL-UI-04; PROVISIONAL D-GEO-125 |
| TC-GEO-0730 | Không ghi sự kiện thay người khác | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Học sinh An | • Phiên của An | • Không có dữ liệu nhập riêng | 1. Gửi thẳng một lô sự kiện thao tác trên BG-1, trong thân ghi định danh của Bình | • Sự kiện được tính cho An<br/>• Số thao tác của Bình không đổi | US-GEO-703 · FC4; CL-FN-05; ENG |
| TC-GEO-0731 | Sự kiện cho bài không thuộc lớp mình | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Học sinh An | • Phiên của An (11A1) | • Không có dữ liệu nhập riêng | 1. Gửi thẳng một sự kiện mở bài cho BG-4 (lớp 9B) | • `NOT_FOUND`<br/>• Theo dõi lớp của BG-4 không có An | US-GEO-703 · FC5; CL-FN-06; ENG |
| TC-GEO-0740 | Khoảng nghỉ 29 phút 59 giây vẫn cùng phiên | Medium | Normal (Success case) | chức năng | đơn vị + tích hợp CSDL | IT | Kỹ thuật | • Một học sinh có hai sự kiện trên một bài, tại T0 và T0 + 29 phút 59 giây, không có sự kiện nào khác | • Không có dữ liệu nhập riêng | 1. Tính phiên và thời gian | • Có một phiên<br/>• Thời gian của em trên bài là 30 phút (29 phút 59 giây làm tròn phút) | US-GEO-703 · SC8; CL-FN-09; PROVISIONAL D-GEO-144; ENG; biên 29 phút 59 giây; tính bằng hàm của ứng dụng và bằng truy vấn ghi tiến độ của CSDL (D-GEO-144, RV-GEO-17) |
| TC-GEO-0741 | Khoảng nghỉ đúng 30 phút 0 giây mở phiên mới | Medium | Normal (Success case) | chức năng | đơn vị + tích hợp CSDL | IT | Kỹ thuật | • Một học sinh có hai sự kiện trên một bài, tại T0 và T0 + 30 phút 0 giây, không có sự kiện nào khác | • Không có dữ liệu nhập riêng | 1. Tính phiên và thời gian | • Có hai phiên<br/>• Thời gian của em trên bài là 0 phút (khoảng nghỉ 30 phút không được cộng vào thời gian) | US-GEO-703 · SC9; CL-FN-09; PROVISIONAL D-GEO-144; ENG; biên 30 phút 0 giây; tính bằng hàm của ứng dụng và bằng truy vấn ghi tiến độ của CSDL (D-GEO-144, RV-GEO-17) |
| TC-GEO-0742 | Gửi lại câu hỏi sau khi hết thời gian chờ không đếm hai lần | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Học sinh An rồi Giáo viên mẫu | • BG-1; Theo dõi lớp ghi thẻ "Câu hỏi" là 7, cột Hỏi đáp của An là 3<br/>• Thiết bị của An gửi câu hỏi và hết thời gian chờ trước khi có phản hồi | • Hàng dữ liệu: F-GEO-050, F-GEO-055…058<br/>• Câu hỏi "Vì sao A là hình chiếu của S?" | 1. Thiết bị gửi lại đúng yêu cầu đó (cùng mã yêu cầu)<br/>2. Giáo viên mẫu mở Theo dõi lớp của BG-1 sau 10 giây | • Thẻ "Câu hỏi" là 8, không phải 9<br/>• Cột Hỏi đáp của An là 4, không phải 5 | US-GEO-703 · FC2; NFR-GEO-052; CL-FN-10; ADR-GEO-11; RV-GEO-18; PROVISIONAL QA-GEO-21; ENG |
| TC-GEO-0743 | Sự kiện mở bài ghi bền | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Học sinh Hà rồi Giáo viên mẫu | • Hà chưa mở BG-1<br/>• Theo dõi lớp ghi "4/5 đã mở bài" | • Không có dữ liệu nhập riêng | 1. Hà mở BG-1<br/>2. Máy chủ trả thành công cho sự kiện mở bài<br/>3. Ngay sau đó máy chủ ứng dụng bị khởi động lại | • Sau khi máy chủ chạy lại, Theo dõi lớp ghi "5/5 đã mở bài" và dòng Hà ghi "Đã mở bài"<br/>• Có đúng một sự kiện mở bài của Hà cho lần mở đó | US-GEO-703 · SC7; CL-FN-13; PROVISIONAL D-GEO-162; ENG; D-GEO-162, RV-GEO-22 |

### US-GEO-704 — Dữ liệu: bảng sự kiện chia theo thời gian và phân trang theo khóa

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-GEO-0732 | Sự kiện vào đúng phần theo thời gian | Medium | Normal (Success case) | phi chức năng | tích hợp CSDL | IT | Kỹ thuật | • Bảng sự kiện đã có phần cho tháng hiện tại và tháng kế tiếp | • Không có dữ liệu nhập riêng | 1. Ghi một sự kiện có thời điểm hiện tại | • Sự kiện nằm trong phần của tháng hiện tại<br/>• Phần mặc định (dành cho thời điểm không thuộc phần nào) vẫn rỗng | US-GEO-704 · SC1; CL-X-02; ENG |
| TC-GEO-0733 | Luôn có phần cho thời gian sắp tới | Medium | Normal (Success case) | phi chức năng | tích hợp CSDL | IT | Kỹ thuật | • Hệ thống đang chạy | • Không có dữ liệu nhập riêng | 1. Kiểm danh sách phần của bảng sự kiện | • Có ít nhất một phần cho tháng sau tháng hiện tại | US-GEO-704 · SC2; CL-X-02; ENG |
| TC-GEO-0734 | Đọc sự kiện theo con trỏ, không trùng không sót | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Kỹ thuật | • Dữ liệu thử tải F-GEO-097<br/>• Một học sinh thử có 100 sự kiện trên một bài | • Hàng dữ liệu: F-GEO-097 | 1. Đọc sự kiện của em đó trên bài đó theo trang 50, lần lượt tới hết, trong khoảng thời gian chứa cả 100 sự kiện | • Hai trang 50 và 50, mới nhất trước, 100 sự kiện mỗi cái đúng một lần<br/>• Phản hồi không có tổng số | US-GEO-704 · SC3; CL-FN-08; ENG |
| TC-GEO-0735 | Truy vấn nóng không quét toàn bảng | High | Normal (Success case) | phi chức năng | tích hợp CSDL | IT | Kỹ thuật (`psql`, `bench/lint_queries.py`) | • Dữ liệu thử tải F-GEO-097 (2.000.000 sự kiện) | • Hàng dữ liệu: F-GEO-097 | 1. Lấy kế hoạch thực thi của các truy vấn nóng: Bài của em, trang Làm bài, yêu cầu lời giải, Theo dõi lớp, Kho đề, đọc sự kiện | • Không kế hoạch nào quét toàn bộ một bảng có hơn 10.000 dòng<br/>• Truy vấn đọc sự kiện chỉ chạm các phần nằm trong cận thời gian | US-GEO-704 · SC4; CL-X-02; ENG |
| TC-GEO-0736 | Truy vấn sự kiện không có cận thời gian hoặc dùng OFFSET | High | Abnormal (Fail case/Edge case) | phi chức năng | đơn vị | IT | Kỹ thuật (bộ kiểm truy vấn) | • Bộ kiểm truy vấn tự động của dự án | • Không có dữ liệu nhập riêng | 1. Thêm một truy vấn đọc bảng sự kiện không có cận thời gian, và một truy vấn dùng OFFSET | • Bộ kiểm báo lỗi cho cả hai và không cho qua | US-GEO-704 · FC1; CL-X-03; ENG |
| TC-GEO-0737 | Giờ thiết bị ở tương lai bị kẹp về giờ nhận | Low | Abnormal (Fail case/Edge case) | phi chức năng | tích hợp CSDL | IT | Kỹ thuật | • Bảng sự kiện có phần cho tháng hiện tại và tháng kế tiếp<br/>• IPad của Hà bị chỉnh đồng hồ nhanh 3 tháng | • Không có dữ liệu nhập riêng | 1. Thiết bị của Hà gửi một lô thao tác trên hình mang thời điểm theo đồng hồ thiết bị | • Sự kiện được ghi với thời điểm bằng giờ nhận của máy chủ (quy tắc kẹp thời điểm vào khoảng [giờ nhận − 24 giờ, giờ nhận] của ADR-GEO-06), nằm trong phần của tháng hiện tại<br/>• Phần mặc định vẫn rỗng | US-GEO-704 · FC2; CL-X-02; PROVISIONAL D-GEO-125; ENG |
| TC-GEO-0738 | Con trỏ hỏng | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Kỹ thuật | • Một yêu cầu đọc sự kiện | • Hàng dữ liệu: F-GEO-084<br/>• Giá trị nhập: `khong-phai-con-tro` | 1. Gửi con trỏ `khong-phai-con-tro` (F-GEO-084) | • `INVALID_CURSOR`<br/>• Không có sự kiện nào được trả | US-GEO-704 · FC3; CL-FN-03; ENG |
| TC-GEO-0739 | Hai sự kiện trùng mốc thời gian ở ranh giới trang không lặp, không sót | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Kỹ thuật | • Một học sinh thử có 100 sự kiện trên một bài<br/>• Sự kiện thứ 50 và thứ 51 (tính từ mới nhất) có cùng thời điểm tới micro giây, khác id | • Hàng dữ liệu: F-GEO-201, F-GEO-097<br/>• Kích thước trang = 50 | 1. Đọc trang 1 sự kiện của học sinh đó trên bài đó<br/>2. Đọc trang 2 bằng con trỏ của trang 1<br/>3. Ghép hai trang và so id | • Trang 1 có 50 sự kiện, trang 2 có 50 sự kiện<br/>• 100 id khác nhau, mỗi sự kiện đúng một lần<br/>• Hai sự kiện trùng mốc nằm ở hai trang khác nhau, không cái nào bị lặp hay mất<br/>• Phản hồi không có tổng số | US-GEO-704 · SC3; NFR-GEO-046; NFR-GEO-040; CL-FN-08; ENG |

## 3. Đối chiếu hai chiều AC ↔ TC

### 3.1 Chiều xuôi: tiêu chí → case (43 / 43 tiêu chí còn hiệu lực có case, 100.0%)

| User story | Criterion ID | Kind | What it checks | Status | Test cases |
|---|---|---|---|---|---|
| US-GEO-701 | US-GEO-701-SC1 | Success | Bốn thẻ tổng | Active · PROVISIONAL | TC-GEO-0700 |
| US-GEO-701 | US-GEO-701-SC2 | Success | Bảng học sinh | Active · PROVISIONAL | TC-GEO-0701 |
| US-GEO-701 | US-GEO-701-SC3 | Success | Tình trạng khi bài còn khóa | Active · PROVISIONAL | TC-GEO-0702 |
| US-GEO-701 | US-GEO-701-SC4 | Success | Tình trạng sau khi mở lời giải | Active · PROVISIONAL | TC-GEO-0703 |
| US-GEO-701 | US-GEO-701-SC5 | Success | Thẻ vàng khi lời giải khóa | Active | TC-GEO-0704 |
| US-GEO-701 | US-GEO-701-SC6 | Success | Vào trang từ mục "Theo dõi" | Active · PROVISIONAL | TC-GEO-0705 |
| US-GEO-701 | US-GEO-701-SC7 | Success | Xem nhanh trên điện thoại | Active | TC-GEO-0706 |
| US-GEO-701 | US-GEO-701-FC1 | Failure | Chưa em nào mở bài | Active | TC-GEO-0707 |
| US-GEO-701 | US-GEO-701-FC2 | Failure | Không có xếp hạng, không sắp theo kết quả | Active · PROVISIONAL | TC-GEO-0708 |
| US-GEO-701 | US-GEO-701-FC3 | Failure | Không có "Nên nhắc lại trên lớp" | Active · PROVISIONAL | TC-GEO-0709 |
| US-GEO-701 | US-GEO-701-FC4 | Failure | Học sinh gọi Theo dõi lớp | Active | TC-GEO-0710 |
| US-GEO-701 | US-GEO-701-FC5 | Failure | Trang không mang lời giải | Active · PROVISIONAL | TC-GEO-0711 |
| US-GEO-701 | US-GEO-701-FC6 | Failure | Trang không đọc bảng sự kiện, không truy vấn lặp theo học sinh | Active | TC-GEO-0712 |
| US-GEO-701 | US-GEO-701-FC7 | Failure | Thanh bên không có "Tổng quan" và "Lớp học" | Active · PROVISIONAL | TC-GEO-0744 |
| US-GEO-702 | US-GEO-702-SC1 | Success | Mức gợi ý của từng em | Active | TC-GEO-0713 |
| US-GEO-702 | US-GEO-702-SC2 | Success | Số câu hỏi của từng em | Active | TC-GEO-0714 |
| US-GEO-702 | US-GEO-702-SC3 | Success | Câu hỏi của một em | Active | TC-GEO-0715 |
| US-GEO-702 | US-GEO-702-SC4 | Success | Khối "Học sinh đang hỏi gì" gộp câu trùng | Active · PROVISIONAL | TC-GEO-0716 |
| US-GEO-702 | US-GEO-702-SC5 | Success | Gộp không phân biệt hoa thường và khoảng trắng | Active · PROVISIONAL | TC-GEO-0717 |
| US-GEO-702 | US-GEO-702-FC1 | Failure | Chưa ai hỏi | Active | TC-GEO-0718 |
| US-GEO-702 | US-GEO-702-FC2 | Failure | Không lẫn câu hỏi của bài khác, lớp khác | Active | TC-GEO-0719 |
| US-GEO-702 | US-GEO-702-FC3 | Failure | Không có nhận định do AI viết | Active · PROVISIONAL | TC-GEO-0720 |
| US-GEO-703 | US-GEO-703-SC1 | Success | Mở bài tạo sự kiện mở bài | Active | TC-GEO-0721 |
| US-GEO-703 | US-GEO-703-SC2 | Success | Mỗi công cụ là một thao tác trên hình | Active · PROVISIONAL | TC-GEO-0722 |
| US-GEO-703 | US-GEO-703-SC3 | Success | Sự kiện gợi ý ghi bền | Active | TC-GEO-0723 |
| US-GEO-703 | US-GEO-703-SC4 | Success | Tỉ lệ phiên có thao tác trên hình | Active · PROVISIONAL | TC-GEO-0724 |
| US-GEO-703 | US-GEO-703-SC5 | Success | Ranh giới phiên làm bài | Active · PROVISIONAL | TC-GEO-0725 |
| US-GEO-703 | US-GEO-703-SC6 | Success | Bài tự học cũng ghi sự kiện | Active | TC-GEO-0726 |
| US-GEO-703 | US-GEO-703-FC1 | Failure | Công cụ không tính | Active · PROVISIONAL | TC-GEO-0727 |
| US-GEO-703 | US-GEO-703-FC2 | Failure | Gửi lại cùng một lô | Active · PROVISIONAL | TC-GEO-0728, TC-GEO-0742 |
| US-GEO-703 | US-GEO-703-FC3 | Failure | Mất mạng khi đang thao tác | Active · PROVISIONAL | TC-GEO-0729 |
| US-GEO-703 | US-GEO-703-FC4 | Failure | Không ghi sự kiện thay người khác | Active | TC-GEO-0730 |
| US-GEO-703 | US-GEO-703-FC5 | Failure | Sự kiện cho bài không thuộc lớp mình | Active | TC-GEO-0731 |
| US-GEO-703 | US-GEO-703-SC7 | Success | Sự kiện mở bài ghi bền | Active · PROVISIONAL | TC-GEO-0743 |
| US-GEO-703 | US-GEO-703-SC8 | Success | Khoảng nghỉ 29 phút 59 giây vẫn cùng phiên | Active · PROVISIONAL | TC-GEO-0740 |
| US-GEO-703 | US-GEO-703-SC9 | Success | Khoảng nghỉ đúng 30 phút 0 giây mở phiên mới | Active · PROVISIONAL | TC-GEO-0741 |
| US-GEO-704 | US-GEO-704-SC1 | Success | Sự kiện vào đúng phần theo thời gian | Active | TC-GEO-0732 |
| US-GEO-704 | US-GEO-704-SC2 | Success | Luôn có phần cho thời gian sắp tới | Active | TC-GEO-0733 |
| US-GEO-704 | US-GEO-704-SC3 | Success | Đọc sự kiện theo con trỏ, không trùng không sót | Active | TC-GEO-0734, TC-GEO-0739 |
| US-GEO-704 | US-GEO-704-SC4 | Success | Truy vấn nóng không quét toàn bảng | Active | TC-GEO-0735 |
| US-GEO-704 | US-GEO-704-FC1 | Failure | Truy vấn sự kiện không có cận thời gian hoặc dùng OFFSET | Active | TC-GEO-0736 |
| US-GEO-704 | US-GEO-704-FC2 | Failure | Giờ thiết bị ở tương lai bị kẹp về giờ nhận | Active · PROVISIONAL | TC-GEO-0737 |
| US-GEO-704 | US-GEO-704-FC3 | Failure | Con trỏ hỏng | Active | TC-GEO-0738 |

### 3.2 Chiều ngược: case → tiêu chí hoặc NFR (45 / 45 case trỏ về một tiêu chí của BA hoặc một NFR-GEO, 100.0%; riêng trỏ về tiêu chí của BA: 45 / 45, 100.0%)

Kiểm bằng `gen_cases.py`: mỗi case bám tiêu chí lấy đúng một tiêu chí từ bảng phân loại; case viết tay ghi tiêu chí ở Remark và bộ sinh từ chối id tiêu chí không tồn tại.

### 3.3 NFR-GEO được case trong tệp này nhắc tới

NFR-GEO-009, NFR-GEO-030, NFR-GEO-040, NFR-GEO-042, NFR-GEO-045, NFR-GEO-046, NFR-GEO-050, NFR-GEO-051, NFR-GEO-052

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
| 1 | bản đầu của goal run: 40 case cho 39 tiêu chí | QC |
| 2 | lượt sửa gom sau phản biện (M-304): biên phiên 29 phút 59 giây và 30 phút 0 giây TC-GEO-0740, 0741 (D-GEO-144, RV-GEO-17); gửi lại câu hỏi không đếm đôi 0742 (RV-GEO-18); datasource-proxy cho 0712 (D-GEO-170): 45 case cho 43 tiêu chí | QC |
| 3 | đồng bộ với bàn giao của BA (M-217, M-218): case viết tay trùng tiêu chí mới chuyển thành case bám tiêu chí (giữ id), thêm case cho mọi tiêu chí mới, gỡ chỉnh không còn cần: 45 case cho 43 tiêu chí | QC |
| 4 | sinh lại theo AC hiện hành (PO-50, Đợt 0): bộ sinh cho cùng nội dung với AC hiện hành (không case nào lệch); thêm cột "Tự động hóa" (E2E-UI / E2E-API / IT / Manual có lý do) cho mọi case: 45 case cho 43 tiêu chí | QC |
