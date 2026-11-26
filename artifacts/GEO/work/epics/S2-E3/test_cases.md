Phiên bản: v0.2 · 2026-11-26
# S2-E3 — test case (Thêm đề bằng chữ hoặc ảnh, bốn bước đọc–dựng–giải–kiểm và ngoại lệ)

> Chủ sở hữu: QC (goal run T-261126-12). **Trạng thái: ready for R3** · Mức epic: **P0**. Case bám tiêu chí của `stories_ac.md` cùng thư mục; thiết kế ở `test_design.md`. Mọi case ở trạng thái Not run: đây là bản viết, chưa thực thi (code chặng 2 chờ B7, PO-65).

## 0. Reader guide and glossary (hướng dẫn đọc và bảng mã)

**Cách đọc.** Mỗi dòng bảng là một test case bám đúng một tiêu chí (AC) của `stories_ac.md` cùng thư mục, hoặc một case viết tay (ghi rõ ở Remark). Cột "Tự động hóa" cho biết tầng chạy máy (E2E-UI, E2E-API, IT, UT) hoặc Manual kèm lý do. Status cố định: Pass, Fail, Blocked (kèm cần gì), UT (kèm lý do), Not run. AI là giả lập (D-GEO-047).

| Id / abbreviation | Meaning | Where it comes from (pointer only) |
|---|---|---|
| `US-S2En-nn` · `…-SCn` / `-FCn` / `-BVn` | story của epic S2-En · tiêu chí thành công / lỗi / biên thứ n của story | tệp này |
| `BR-S2En-nn` | quy tắc nghiệp vụ BA làm rõ cho epic; đi kèm `BR-S2-nn` của BRD delta khi có | `artifacts/GEO/work/s23/brd-delta-s23.md` |
| `M-S2-nn` | chỉ số chặng 2 (công thức ở BRD delta mục 1) | `brd-delta-s23.md` |
| `PO-nn` | dòng của sổ quyết định gốc | `PO-DECISIONS.md` |
| `DQ-nn` | câu hỏi design và mặc định đang dùng | `Design/s23/DQ.md` |
| `C2-nn` | mã khung màn hình chặng 2 (C2-01…C2-08) | `STAGES-AFTER-MVP.md` Chặng 2 mục 6 |
| `D-GEO-nnn` | quyết định của MVP còn hiệu lực (mã lỗi ổn định D-GEO-143, mock AI D-GEO-047, tiêu đề no-store D-GEO-145) | `artifacts/GEO/work/03_decisions.md` (MVP) |
| `QA-S2En-nn` | câu hỏi mở của epic, luôn kèm mặc định TẠM | `questions.md` cùng thư mục |
| `TC-S2En-nnn` | test case của epic | `test_cases.md` cùng thư mục |
| `FX-nn` | dữ liệu mẫu chặng 2 (bảng dưới) | tệp này |
| `BG-n` | bài giao mẫu của MVP (BG-1…BG-5) | `artifacts/GEO/work/fixture.md` (MVP) |
| `BR-GEO-nnn` | quy tắc nghiệp vụ của MVP còn hiệu lực | `artifacts/GEO/work/02_gate2_brd.md` (MVP) |
| `MSG-GEO-nn` | thông điệp hiện cho người dùng của MVP (câu chữ nguyên văn) | `artifacts/GEO/work/05_dictionary.md` (MVP) |
| TẠM | giả định đang dùng, chờ owner xác nhận; ghi theo mẫu `TẠM · tin cậy · nguồn` | PO-65 |
| Nhãn hai trục | trục máy: "Đã kiểm chứng" / "Cần xem lại" (chỉ engine gắn) · trục người: "AI soạn" / "Giáo viên đã duyệt" | PO-02 |
| `release_at` | giờ mở lời giải; máy chủ so giờ máy chủ, không tin giờ thiết bị | MVP, M-GEO-13 |
| bốn bước | đọc đề, dựng hình, giải, kiểm chứng; mỗi bước có trạng thái chờ / đang chạy / xong / lỗi | STAGES Ch2 mục 5 luồng A |
| ô chưa chắc | ô chữ do máy đọc từ ảnh có độ tin cậy dưới 0,80, tô vàng để giáo viên sửa | PO-65 Q4, DQ-02 |

### Dữ liệu mẫu chặng 2 (`FX-nn`)

| Mã | Nội dung | Giá trị |
|---|---|---|
| FX-01 | Giáo viên "Cô Trang" | mã `GV-7K2M-9Q4D`, mật khẩu `Trang@2026x` |
| FX-02 | Học sinh "An" | mã `HS-3F8A-2L6P`, mật khẩu `AnHoc#2026` |
| FX-03 | Học sinh "Bình" | mã `HS-5B7C-8R2T`, cùng lớp 11A1 với An |
| FX-04 | Người soát nội dung "Mai" | mã `GV-2N6P-4W8X`, vai soát nội dung do chủ sản phẩm gán (TẠM, QA-S2E6-01) |
| FX-10 | Đề phẳng PL-01 | Tam giác ABC có AB = 5, AC = 12, BC = 13; tính góc A. Đáp số 90° |
| FX-11 | Đề phẳng PL-02 | Tam giác ABC cân tại A, góc A = 40°; tính góc B. Đáp số 70° |
| FX-12 | Đề phẳng PL-03 | Hình chữ nhật ABCD có AB = 6, AD = 8; tính AC. Đáp số 10 |
| FX-13 | Đề phẳng PL-04 | Tam giác đều cạnh 4; tính diện tích. Đáp số `4√3` (xấp xỉ 6,93) |
| FX-14 | Đề không gian SP-01 | Hình chóp S.ABCD, đáy ABCD là hình vuông cạnh a, SA ⊥ (ABCD), SA = a√2; tính góc giữa SC và mặt đáy. Đáp số 45° (tan SCA = SA / AC = 1) |
| FX-20 | Bài luyện BL-1 | 5 câu; câu 2 là FX-14, kiểu đáp số "số đo góc", sai số ±0,5°, 3 lượt thử; giờ mở lời giải `release_at` = T0 + 2 giờ |

## 1. Tóm tắt

| Story | Số tiêu chí | Số case | Tự động hóa (máy / Manual) |
|---|---|---|---|
| US-S2E3-01 Thêm đề bằng dán chữ, chép lại để sửa | 9 | 9 | 9 / 0 |
| US-S2E3-02 Thêm đề bằng ảnh; ô chưa chắc tô để giáo viên sửa | 11 | 11 | 11 / 0 |
| US-S2E3-03 Bốn bước đọc–dựng–giải–kiểm với màn chờ từng bước | 9 | 9 | 9 / 0 |
| US-S2E3-04 Ngoại lệ: engine không dựng thì lưu "Cần xem lại"; lệch đáp số thì chặn Duyệt | 10 | 10 | 10 / 0 |
| **Tổng** | **39** | **39** | **39 / 0** |

| Theo ưu tiên | Số case |
|---|---|
| High | 22 |
| Medium | 16 |
| Low | 1 |

## 2. Test case

### US-S2E3-01 — Thêm đề bằng dán chữ, chép lại để sửa

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | Status | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S2E3-001 | Dán chữ rồi thấy đề đã chép lại để sửa | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Cô Trang ở Kho đề, bấm "Thêm đề", tab "Dán chữ" | • FX-10 | 1. Dán đề của FX-10 ("Tam giác ABC có AB = 5, AC = 12, BC = 13; tính góc A") | • Hiện ô "Đề đã chép lại" chứa đúng chữ đã dán, sửa được; có nút "Dựng hình" | US-S2E3-01-SC1; STAGES Ch2 mục 5 luồng A |
| TC-S2E3-002 | Ký hiệu toán giữ nguyên khi dán | Medium | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Tab "Dán chữ" | — | 1. Dán chuỗi "SA ⊥ (ABCD), SA = a√2, góc 45°" | • Chuỗi hiện nguyên vẹn, không đổi ký hiệu, không vỡ chữ | US-S2E3-01-SC2; BR-S2E3-03 |
| TC-S2E3-003 | Ô trống không cho dựng | High | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Tab "Dán chữ", ô trống | — | 1. Bấm "Dựng hình" | • Nút khóa hoặc hiện lỗi "Hãy dán hoặc gõ đề"; không tạo tác vụ dựng | US-S2E3-01-FC1; BR-S2E3-02 |
| TC-S2E3-004 | Đề 9 ký tự bị chặn, 10 ký tự được nhận | Medium | Boundary (Edge case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Tab "Dán chữ" | • Đề 9 và 10 ký tự | 1. Gõ đề 9 ký tự; gõ thêm 1 ký tự thành 10 | • 9 ký tự: nút "Dựng hình" khóa kèm lỗi "Đề quá ngắn (tối thiểu 10 ký tự)"<br>• 10 ký tự: nút mở | US-S2E3-01-BV1; BR-S2E3-02 |
| TC-S2E3-005 | Đề đúng 2000 ký tự được nhận | Medium | Boundary (Edge case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Tab "Dán chữ" | • Đề 2000 ký tự | 1. Dán đề dài 2000 ký tự | • Nút "Dựng hình" mở; không báo lỗi độ dài | US-S2E3-01-BV2; BR-S2E3-02; QA-S2E3-01 |
| TC-S2E3-006 | Đề 2001 ký tự bị chặn | Medium | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Tab "Dán chữ" | • Đề 2001 ký tự | 1. Dán đề dài 2001 ký tự (và gọi API với 2001 ký tự) | • Giao diện: lỗi "Đề tối đa 2000 ký tự", nút khóa<br>• API: VALIDATION_FAILED, không tạo đề | US-S2E3-01-BV3; BR-S2E3-02; QA-S2E3-01 |
| TC-S2E3-007 | Mã HTML và script hiện thành chữ | High | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Tab "Dán chữ" | • Chuỗi chứa thẻ script | 1. Dán chuỗi `<script>alert(1)</script> Tam giác ABC` | • Chuỗi hiện nguyên chữ trong ô; không có hộp thoại; trang không đổi | US-S2E3-01-FC2; BR-S2E3-03 |
| TC-S2E3-008 | Nháp chữ còn khi rời màn rồi quay lại | Medium | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Cô Trang đã gõ nửa đề, chưa bấm "Dựng hình" | — | 1. Chuyển sang màn "Kho đề" rồi quay lại "Thêm đề" | • Ô "Đề đã chép lại" còn đúng nửa đề đã gõ | US-S2E3-01-SC3; BR-S2E3-04 |
| TC-S2E3-009 | Quá giới hạn thêm đề theo giờ | Medium | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Cô Trang đã tạo 20 đề trong giờ này | — | 1. Tạo đề thứ 21 | • RATE_LIMITED kèm `Retry-After`; giao diện hiện "Bạn thêm đề hơi nhanh, thử lại sau"; không tạo đề | US-S2E3-01-FC3; BR-S2E3-05; PO-63 |

### US-S2E3-02 — Thêm đề bằng ảnh; ô chưa chắc tô để giáo viên sửa

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | Status | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S2E3-010 | Tải ảnh rõ thì hiện chữ đã đọc không có ô vàng | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Cô Trang ở tab "Tải ảnh"; ảnh JPG rõ của đề FX-10 (mọi ô tin cậy ≥ 0,95) | • Ảnh đề PL-01 | 1. Tải ảnh | • Hiện chữ đã đọc trong ô sửa được; không có ô vàng; nút "Dựng hình" mở | US-S2E3-02-SC1; DQ-01; PO-27 |
| TC-S2E3-011 | Ô chưa chắc tô vàng và nút xác nhận khóa | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Ảnh mờ có 2 ô tin cậy 0,60 và 0,79, các ô khác ≥ 0,80 | • Ô tin cậy 0,60 và 0,79 | 1. Tải ảnh | • Hai ô được tô vàng<br>• nút "Xác nhận chữ và dựng tiếp" khóa; nhãn máy "Cần xem lại" | US-S2E3-02-SC2; DQ-02; PO-65 |
| TC-S2E3-012 | Ô tin cậy 0,80 không tô, 0,79 tô | High | Boundary (Edge case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Giáo viên Cô Trang (FX-01) | • Ảnh giả lập có ô A tin cậy 0,80 và ô B tin cậy 0,79 | • Tin cậy 0,80 và 0,79 | 1. Tải ảnh | • Ô A không tô vàng<br>• ô B tô vàng | US-S2E3-02-BV1; BR-S2E3-07 |
| TC-S2E3-013 | Sửa hết ô vàng mới xác nhận được | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Hai ô vàng chưa xử lý | — | 1. Sửa ô 1; bấm "đúng" ở ô 2; bấm "Xác nhận chữ và dựng tiếp" | • Sau bước 2 nút mở; sau xác nhận hiện đề đã xác nhận, nhãn máy vẫn "Cần xem lại" cho tới khi bước kiểm đạt (PO-38) | US-S2E3-02-SC3; BR-S2E3-08; DQ-02 |
| TC-S2E3-014 | Chưa xác nhận ô vàng vẫn lưu được nháp | Medium | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Hai ô vàng chưa xử lý | — | 1. Bấm "Lưu nháp"; mở lại nháp | • Nháp lưu với 2 ô vàng còn nguyên; không dựng hình; chưa vào ngăn "Của tôi" như đề đã duyệt | US-S2E3-02-SC4; PO-38; BR-S2E3-08 |
| TC-S2E3-015 | Ảnh không có chữ thì yêu cầu gõ lại, không tự đoán | High | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Ảnh trắng (không có chữ nào) | • Ảnh trắng | 1. Tải ảnh | • Hiện "Máy không đọc được chữ trong ảnh, hãy gõ lại đề"; ô sửa để trống; không có đề đoán | US-S2E3-02-FC1; BR-S2E3-10; STAGES Ch2 mục 5 |
| TC-S2E3-016 | Ảnh đúng 5 MB được nhận | Medium | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Tab "Tải ảnh" | • Ảnh 5 242 880 byte | 1. Tải ảnh JPG dài đúng 5 242 880 byte | • Nhận ảnh, tiếp tục đọc chữ | US-S2E3-02-BV2; BR-S2E3-06; QA-S2E3-03 |
| TC-S2E3-017 | Ảnh 5 MB cộng 1 byte bị chặn | Medium | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Tab "Tải ảnh" | • Ảnh 5 242 881 byte | 1. Tải ảnh dài 5 242 881 byte | • Hiện "Ảnh tối đa 5 MB"; API trả PAYLOAD_TOO_LARGE; không đọc chữ | US-S2E3-02-BV3; BR-S2E3-06; QA-S2E3-03 |
| TC-S2E3-018 | Sai định dạng ảnh bị chặn | Medium | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Tab "Tải ảnh" | — | 1. Tải tệp `.gif`; tải tệp `.pdf` đổi đuôi thành `.jpg` | • Cả hai bị từ chối; hiện "Chỉ nhận ảnh JPG hoặc PNG"; tệp đổi đuôi bị nhận ra theo nội dung | US-S2E3-02-FC2; BR-S2E3-06 |
| TC-S2E3-019 | Chọn hai ảnh một lúc chỉ nhận ảnh đầu | Low | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Tab "Tải ảnh" | — | 1. Chọn hai ảnh cùng lúc | • Chỉ ảnh thứ nhất được đọc; hiện "Mỗi lần tải một ảnh" | US-S2E3-02-FC3; BR-S2E3-06; QA-S2E3-03 |
| TC-S2E3-020 | Siêu dữ liệu ảnh bị xóa và ảnh gốc không giữ sau xác nhận | High | Normal (Success case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Giáo viên Cô Trang (FX-01) | • Ảnh có siêu dữ liệu vị trí; đề đã xác nhận | — | 1. Tải ảnh; xác nhận chữ; kiểm kho lưu trữ | • Bản lưu tạm không có siêu dữ liệu vị trí; sau xác nhận không còn tệp ảnh gốc | US-S2E3-02-SC5; BR-S2E3-09; PO-61 |

### US-S2E3-03 — Bốn bước đọc–dựng–giải–kiểm với màn chờ từng bước

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | Status | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S2E3-021 | Bốn bước chạy đúng thứ tự và kết quả đủ | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Đề FX-10 đã chép lại; AI giả lập trả lời đúng | • FX-10 | 1. Bấm "Dựng hình" | • Màn chờ hiện 4 bước theo thứ tự Đọc đề, Dựng hình, Giải, Kiểm chứng, mỗi bước chuyển chờ - đang - xong<br>• kết thúc hiện hình, lời giải nháp, nhãn "Đã kiểm chứng" và "AI soạn" | US-S2E3-03-SC1; STAGES Ch2 mục 5 |
| TC-S2E3-022 | Kiểm chứng không đạt thì nhãn máy Cần xem lại | High | Normal (Success case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Giáo viên Cô Trang (FX-01) | • AI giả lập trả lời bước giải thiếu một bước; engine kiểm không đạt | — | 1. Bấm "Dựng hình" | • Kết thúc với nhãn "Cần xem lại" và "AI soạn"; không có nhãn "Đã kiểm chứng" | US-S2E3-03-SC2; BR-S2E3-14 |
| TC-S2E3-023 | Bước lỗi dừng các bước sau | High | Failure (Error case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Giáo viên Cô Trang (FX-01) | • AI giả lập báo lỗi ở bước Dựng hình | — | 1. Bấm "Dựng hình" | • Bước Dựng hình ghi "lỗi"; hai bước sau giữ "chờ" (không chạy); có nút "Thử lại bước này" | US-S2E3-03-FC1; BR-S2E3-11 |
| TC-S2E3-024 | Bước chạy 30 giây còn đạt, 31 giây quá hạn | High | Boundary (Edge case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Giáo viên Cô Trang (FX-01) | • AI giả lập trả lời sau 30 giây (lần 1) và 31 giây (lần 2), đồng hồ giả | — | 1. Chạy bước "Giải" hai lần | • Lần 1 (30 giây): bước xong<br>• lần 2 (31 giây): bước lỗi TIMEOUT, có "Thử lại bước này", hai bước đã xong trước đó giữ kết quả | US-S2E3-03-BV1; BR-S2E3-12; QA-S2E3-05 |
| TC-S2E3-025 | Thử lại bước lỗi giữ kết quả các bước đã xong | High | Normal (Success case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Giáo viên Cô Trang (FX-01) | • Bước Giải vừa TIMEOUT; các bước Đọc đề, Dựng hình đã xong | — | 1. Bấm "Thử lại bước này" khi AI trả lời bình thường | • Chỉ bước Giải chạy lại; hai bước đầu không chạy lại (không gọi lại AI cho chúng); tiến trình đi tiếp tới Kiểm chứng | US-S2E3-03-SC3; BR-S2E3-12 |
| TC-S2E3-026 | Đóng tab rồi mở lại vẫn thấy tiến trình | Medium | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Tác vụ dựng đang ở bước Giải | — | 1. Đóng tab; mở lại "Thêm đề" sau 10 giây | • Hiện đúng tiến trình (bước Giải đang chạy hoặc đã xong); không tạo tác vụ thứ hai | US-S2E3-03-SC4; BR-S2E3-13 |
| TC-S2E3-027 | Bấm Dựng hình hai lần chỉ tạo một tác vụ | Medium | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Đề đã chép lại | — | 1. Bấm "Dựng hình" hai lần trong 1 giây | • Chỉ một tác vụ được tạo; AI giả lập chỉ nhận một lần gọi cho mỗi bước | US-S2E3-03-FC2; BR-S2E3-13 |
| TC-S2E3-028 | Hủy tác vụ đang chạy | Medium | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Tác vụ đang ở bước Dựng hình | — | 1. Bấm "Hủy" | • Tác vụ dừng; trạng thái "đã hủy"; đề vẫn là nháp chữ; không tạo đề mới trong Kho đề | US-S2E3-03-SC5; BR-S2E3-11 |
| TC-S2E3-029 | Đề của người khác không xem được tiến trình | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Tác vụ dựng của Cô Trang; tài khoản giáo viên thứ hai "Cô Lan" | — | 1. Cô Lan gọi lấy tiến trình theo mã tác vụ của Cô Trang | • NOT_FOUND (không lộ tác vụ có tồn tại); không dữ liệu của Cô Trang trả về | US-S2E3-03-FC3; BR-S2E3-13; D-GEO-143 |

### US-S2E3-04 — Ngoại lệ: engine không dựng thì lưu "Cần xem lại"; lệch đáp số thì chặn Duyệt

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | Status | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S2E3-030 | Engine không dựng thì lưu Cần xem lại với ba lối đi | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Đề hình lạ ngoài danh sách (như elip) đã qua bước Đọc đề | — | 1. Chạy bốn bước | • Bước Dựng hình báo "không dựng được"; đề lưu với "Cần xem lại · hình do AI dựng"<br>• hiện ba lối "Bổ sung điều kiện", "Dựng tay", "Lưu bản nháp chữ" | US-S2E3-04-SC1; DQ-03 |
| TC-S2E3-031 | Lưu nháp chữ thì đề chưa giao được | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Đề lưu bằng lối "Lưu bản nháp chữ" | — | 1. Mở Soạn bài giao và tìm đề này | • Đề không chọn được để giao, kèm chú thích "bản nháp chữ" | US-S2E3-04-SC2; BR-S2E3-15 |
| TC-S2E3-032 | Giao đề Cần xem lại phải bấm xác nhận riêng | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Đề "Cần xem lại · hình do AI dựng" đã lưu | — | 1. Chọn đề để giao | • Hiện hộp hỏi "Hình do AI dựng, chưa kiểm chứng. Vẫn giao?" với nút xác nhận riêng; không bấm thì không giao | US-S2E3-04-SC3; BR-S2E3-16 |
| TC-S2E3-033 | Lệch đáp số hiện bảng đối chiếu và chặn Duyệt | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Đề FX-10, lời giải AI ghi đáp số 80°, máy tính lại 90° | • FX-10; đáp số AI 80° | 1. Mở đề trong hộp duyệt | • Bảng "Đề ghi / AI giải / Máy tính lại" hiện 90° / 80° / 90°; nút "Duyệt lời giải này" khóa kèm "Chặn duyệt: đáp số chưa khớp"; không có đường tắt | US-S2E3-04-SC4; DQ-04 |
| TC-S2E3-034 | Chọn dùng đáp số máy thì kiểm lại và mở Duyệt | High | Normal (Success case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Giáo viên Cô Trang (FX-01) | • Đề lệch như SC4 | — | 1. Chọn "Dùng đáp số máy"; chờ kiểm lại | • Đáp số lời giải thành 90°; ba đáp số khớp; nút Duyệt mở | US-S2E3-04-SC5; BR-S2E3-18 |
| TC-S2E3-035 | Sửa đáp số đề ghi cho khớp thì mở Duyệt | Medium | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Đề lệch: đề ghi 80°, AI và máy 90° | — | 1. Chọn "Sửa đáp số đề ghi"; nhập 90; bấm "Kiểm tra lại" | • Ba đáp số khớp; Duyệt mở | US-S2E3-04-SC6; BR-S2E3-18 |
| TC-S2E3-036 | Không có đường tắt: gọi thẳng API duyệt khi còn lệch | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Đề còn lệch | — | 1. Gọi API Duyệt trực tiếp | • CONFLICT; đề không đổi trạng thái; không có nhãn "Giáo viên đã duyệt" | US-S2E3-04-FC1; BR-S2E3-18; STAGES mục 0.2 nguyên tắc 3 |
| TC-S2E3-037 | Lệch 0,01 coi là khớp, lệch 0,02 là lệch | High | Boundary (Edge case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Giáo viên Cô Trang (FX-01) | • Hai đề: đáp số đề 6,93 và máy 6,94; đáp số đề 6,93 và máy 6,95 | — | 1. Chạy đối chiếu cho từng đề | • Đề 1 (lệch 0,01): khớp, Duyệt không bị chặn vì lệch<br>• đề 2 (lệch 0,02): lệch, chặn Duyệt | US-S2E3-04-BV1; BR-S2E3-17; QA-S2E3-06 |
| TC-S2E3-038 | Dạng viết tương đương tính là khớp | Medium | Normal (Success case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Giáo viên Cô Trang (FX-01) | • Đề FX-13: đề ghi `4√3`, máy tính lại 6,9282 | • FX-13 | 1. Chạy đối chiếu | • Khớp (4√3 bằng 6,9282 trong dung sai 0,01); không chặn Duyệt | US-S2E3-04-SC7; BR-S2E3-17 |
| TC-S2E3-039 | Đề không có đáp số ghi sẵn thì không tính là lệch | Medium | Failure (Error case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Giáo viên Cô Trang (FX-01) | • Đề không ghi đáp số; AI và máy cùng 90° | — | 1. Chạy đối chiếu | • Hai đáp số khớp; không chặn; bảng đối chiếu ghi "Đề ghi: chưa có" | US-S2E3-04-FC2; BR-S2E3-17 |

## 3. Truy vết tiêu chí sang case

| Tiêu chí | Case |
|---|---|
| US-S2E3-01-SC1 | TC-S2E3-001 |
| US-S2E3-01-SC2 | TC-S2E3-002 |
| US-S2E3-01-FC1 | TC-S2E3-003 |
| US-S2E3-01-BV1 | TC-S2E3-004 |
| US-S2E3-01-BV2 | TC-S2E3-005 |
| US-S2E3-01-BV3 | TC-S2E3-006 |
| US-S2E3-01-FC2 | TC-S2E3-007 |
| US-S2E3-01-SC3 | TC-S2E3-008 |
| US-S2E3-01-FC3 | TC-S2E3-009 |
| US-S2E3-02-SC1 | TC-S2E3-010 |
| US-S2E3-02-SC2 | TC-S2E3-011 |
| US-S2E3-02-BV1 | TC-S2E3-012 |
| US-S2E3-02-SC3 | TC-S2E3-013 |
| US-S2E3-02-SC4 | TC-S2E3-014 |
| US-S2E3-02-FC1 | TC-S2E3-015 |
| US-S2E3-02-BV2 | TC-S2E3-016 |
| US-S2E3-02-BV3 | TC-S2E3-017 |
| US-S2E3-02-FC2 | TC-S2E3-018 |
| US-S2E3-02-FC3 | TC-S2E3-019 |
| US-S2E3-02-SC5 | TC-S2E3-020 |
| US-S2E3-03-SC1 | TC-S2E3-021 |
| US-S2E3-03-SC2 | TC-S2E3-022 |
| US-S2E3-03-FC1 | TC-S2E3-023 |
| US-S2E3-03-BV1 | TC-S2E3-024 |
| US-S2E3-03-SC3 | TC-S2E3-025 |
| US-S2E3-03-SC4 | TC-S2E3-026 |
| US-S2E3-03-FC2 | TC-S2E3-027 |
| US-S2E3-03-SC5 | TC-S2E3-028 |
| US-S2E3-03-FC3 | TC-S2E3-029 |
| US-S2E3-04-SC1 | TC-S2E3-030 |
| US-S2E3-04-SC2 | TC-S2E3-031 |
| US-S2E3-04-SC3 | TC-S2E3-032 |
| US-S2E3-04-SC4 | TC-S2E3-033 |
| US-S2E3-04-SC5 | TC-S2E3-034 |
| US-S2E3-04-SC6 | TC-S2E3-035 |
| US-S2E3-04-FC1 | TC-S2E3-036 |
| US-S2E3-04-BV1 | TC-S2E3-037 |
| US-S2E3-04-SC7 | TC-S2E3-038 |
| US-S2E3-04-FC2 | TC-S2E3-039 |

### Khoảng trống

- Không tiêu chí nào thiếu case. Case chưa chạy được vì code chặng 2 chưa có (PO-65 Q1): trạng thái Not run.
- Câu hỏi và đề xuất đổi story map: `questions.md` cùng thư mục.

## 4. Dữ liệu và môi trường

- Dữ liệu: bảng `FX-nn` đầu tệp (đồng bộ với `stories_ac.md`).
- Môi trường: profile `dev` / `test`, AI giả lập, không bao giờ chạy trên `prod` (PO-51). Thiết bị theo mục quy ước của `stories_ac.md`.

## 5. Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: 39 case cho 39 tiêu chí, có cột Tự động hóa | runner-T-261126-12 (QC) |
| v0.2 | 2026-11-26 | đổi mã mẫu `HS-`/`GV-` có ký tự 0, 1, O, I sang ký tự hợp lệ (BR-S2E1-04), RV_s23 m-01 | runner-T-261126-15 (QC soát phản biện, RV_s23) |
