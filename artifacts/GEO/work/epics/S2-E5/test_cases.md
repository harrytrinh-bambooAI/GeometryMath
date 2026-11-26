Phiên bản: v0.2 · 2026-11-26
# S2-E5 — test case (Soát và duyệt lời giải có phiên bản, StepCheck, nhãn hai trục, hộp duyệt, lời giải đã giao bất biến)

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
| `StepCheck` | kết quả kiểm số của từng bước lời giải: Đạt / Không đạt / Không kiểm được | STAGES Ch2 mục 8 |
| phiên bản `vN` | mỗi lần lưu lời giải tạo một phiên bản mới v1, v2, v3…; không ghi đè, không xóa | STAGES Ch2 mục 3 ý 3 |
| hộp duyệt | cột bên phải của C2-03: nhãn hai trục, lịch sử phiên bản, nút Duyệt, đề xuất của AI chờ nhận / sửa / bỏ | 0.3 ý 4 |
| bước lệch | bước có `StepCheck` = Không đạt (tô đỏ, nhãn "Lệch") | DQ-04 |

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
| US-S2E5-01 Sửa lời giải từng bước, mỗi lần lưu thành phiên bản mới | 14 | 14 | 14 / 0 |
| US-S2E5-02 `StepCheck`: kiểm số từng bước, bước sai tô đỏ kèm lý do | 13 | 13 | 13 / 0 |
| US-S2E5-03 Nhãn hai trục và dòng "Máy đã kiểm tra phần tính toán"; chỉ engine gắn "Đã kiểm chứng" | 11 | 11 | 10 / 1 |
| US-S2E5-04 Hộp duyệt chung: nhận, sửa, bỏ | 14 | 15 | 15 / 0 |
| US-S2E5-05 Lời giải đã giao bất biến; sửa tạo phiên bản mới và chọn có áp cho bài đang giao | 12 | 13 | 13 / 0 |
| **Tổng** | **64** | **66** | **65 / 1** |

| Theo ưu tiên | Số case |
|---|---|
| High | 50 |
| Medium | 14 |
| Low | 2 |

## 2. Test case

### US-S2E5-01 — Sửa lời giải từng bước, mỗi lần lưu thành phiên bản mới

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | Status | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S2E5-001 | Sửa bước 3 rồi lưu thành v2, v1 còn nguyên | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Lời giải v1 của FX-14 (bước 3 sai) đang mở trong C2-03 | • FX-14, lời giải v1 | 1. Sửa bước 3 thành "tan SCA = a√2 / a√2 = 1"; bấm "Lưu" | • Có phiên bản v2 mang bước 3 mới<br>• v1 vẫn xem được với bước 3 cũ "= √2"; lịch sử ghi "v2 · bạn sửa bước 3" | US-S2E5-01-SC1; STAGES Ch2 mục 3 ý 3; DQ-06 |
| TC-S2E5-002 | Thêm một bước ở giữa thì đánh số lại | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Lời giải v2 có 4 bước | — | 1. Bấm "Thêm bước" sau bước 2; gõ "Vậy tam giác SAC vuông cân tại A"; bấm "Lưu" | • Lời giải 5 bước đánh số lại 1 đến 5; bước mới là bước 3; lưu thành v3 | US-S2E5-01-SC2; BR-S2E5-01 |
| TC-S2E5-003 | Xóa một bước và kéo đổi thứ tự | Medium | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Lời giải 5 bước | — | 1. Xóa bước 3; kéo bước 4 lên trước bước 3; bấm "Lưu" | • Còn 4 bước, đánh số lại 1 đến 4, thứ tự đúng như đã kéo; lưu thành một phiên bản mới (một lần lưu một phiên bản dù nhiều thay đổi) | US-S2E5-01-SC3; BR-S2E5-01 |
| TC-S2E5-004 | Lịch sử phiên bản đủ thông tin và mới nhất ở trên | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Đề có v1 (AI soạn) và v2 (Cô Trang sửa lúc 11:58) | — | 1. Mở hộp duyệt | • Hiện v2 ở trên, v1 ở dưới; mỗi dòng có số phiên bản, người sửa, giờ, mô tả ("AI soạn · máy chưa kiểm", "bạn sửa bước 3") | US-S2E5-01-SC4; BR-S2E5-03; DQ-06 |
| TC-S2E5-005 | Dùng bản cũ tạo phiên bản mới là bản sao | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Đề có v1, v2; đang xem v1 (chỉ đọc) | — | 1. Bấm "Dùng bản này" | • Tạo v3 có nội dung giống hệt v1; v1 và v2 còn nguyên trong lịch sử; v3 mang mô tả "sao từ v1" | US-S2E5-01-SC5; BR-S2E5-04 |
| TC-S2E5-006 | Lưu khi chưa đổi gì không tạo phiên bản | High | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Lời giải v2 vừa mở, chưa sửa gì | — | 1. Bấm "Lưu" | • Hiện "Chưa có thay đổi để lưu"; vẫn là v2; không có v3 | US-S2E5-01-FC1; BR-S2E5-02 |
| TC-S2E5-007 | Bước trống bị chặn | High | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Lời giải 4 bước | • Bước "" và "   " | 1. Xóa hết chữ của bước 3 (hoặc gõ ba dấu cách); bấm "Lưu" | • Hiện lỗi "Bước 3 đang trống"; bước 3 viền đỏ; không tạo phiên bản | US-S2E5-01-FC2; BR-S2E5-01 |
| TC-S2E5-008 | Bước đúng 2000 ký tự được lưu | Medium | Boundary (Edge case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Lời giải mở | • Bước 2000 ký tự | 1. Gõ bước 2 dài 2000 ký tự; bấm "Lưu" | • Lưu thành công, tạo phiên bản mới | US-S2E5-01-BV1; BR-S2E5-01; QA-S2E5-01 |
| TC-S2E5-009 | Bước 2001 ký tự bị chặn | Medium | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Lời giải mở | • Bước 2001 ký tự | 1. Gõ bước 2 dài 2001 ký tự; bấm "Lưu" (và gọi API với 2001 ký tự) | • Giao diện: lỗi "Bước tối đa 2000 ký tự"; API: VALIDATION_FAILED; không tạo phiên bản | US-S2E5-01-BV2; BR-S2E5-01; QA-S2E5-01 |
| TC-S2E5-010 | Lời giải 30 bước được lưu, thêm bước 31 bị chặn | High | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Lời giải 29 bước | • 30 và 31 bước | 1. Thêm bước thứ 30 rồi lưu; thêm bước thứ 31 | • Bước 30: lưu được<br>• bước 31: nút "Thêm bước" khóa kèm "Tối đa 30 bước"; API: VALIDATION_FAILED | US-S2E5-01-BV3; BR-S2E5-01; QA-S2E5-01 |
| TC-S2E5-011 | Xóa còn 1 bước được, xóa bước cuối cùng bị chặn | Medium | Boundary (Edge case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Lời giải 2 bước | — | 1. Xóa một bước (còn 1); xóa nốt bước còn lại | • Còn 1 bước: lưu được<br>• xóa nốt: lỗi "Lời giải cần ít nhất 1 bước"; không lưu | US-S2E5-01-BV4; BR-S2E5-01 |
| TC-S2E5-012 | Hai tab sửa cùng lúc: tab lưu sau nhận CONFLICT | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Cô Trang mở lời giải v1 ở tab A và tab B | — | 1. Tab A sửa bước 3 và lưu thành v2; tab B (vẫn từ v1) sửa bước 4 và bấm "Lưu" | • Tab B nhận CONFLICT, không ghi đè v2; hiện "Có bản mới hơn (v2)" với hai lựa chọn "Tải bản mới" và "Giữ phần sửa của tôi để ghép tay"; v2 nguyên vẹn | US-S2E5-01-FC3; BR-S2E5-05 |
| TC-S2E5-013 | Giáo viên khác không sửa được lời giải của đề Của tôi | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Đề Của tôi của Cô Trang; giáo viên "Cô Lan" đăng nhập | — | 1. Cô Lan gọi sửa lời giải đề này | • NOT_FOUND; không lộ đề có tồn tại; phiên bản không đổi | US-S2E5-01-FC4; BR-S2E5-06 |
| TC-S2E5-014 | Học sinh không sửa được lời giải | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Học sinh An (FX-02) | — | 1. Gọi sửa lời giải một đề | • FORBIDDEN hoặc NOT_FOUND; không phiên bản mới | US-S2E5-01-FC5; BR-S2E5-06; PO-63 |

### US-S2E5-02 — `StepCheck`: kiểm số từng bước, bước sai tô đỏ kèm lý do

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | Status | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S2E5-015 | Bước 3 sai tô đỏ kèm lý do, các bước khác Đạt | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Lời giải v2 của FX-14 với bước 3 "tan SCA = √2" | • FX-14 | 1. Bấm "Kiểm tra lại" | • Bước 3 tô đỏ nhãn "Lệch", lý do nêu "ghi √2 ≈ 1,41; máy tính lại 1"<br>• bước 2 Đạt<br>• bước 4 Đạt theo giá trị bước 3 (54,7°)<br>• dòng tóm tắt "1 bước lệch" | US-S2E5-02-SC1; DQ-04; STAGES Ch2 mục 5 |
| TC-S2E5-016 | Sửa đúng bước 3 rồi kiểm lại thì mọi bước Đạt | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Lời giải v3 với bước 3 và 4 đúng (tan = 1, góc 45°) | — | 1. Bấm "Kiểm tra lại" | • Cả bốn bước Đạt; dòng "Máy đã kiểm tra phần tính toán"; không bước đỏ | US-S2E5-02-SC2; BR-S2E5-07 |
| TC-S2E5-017 | Bước chỉ chữ là Không kiểm được, không tính là lệch | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Bước 1 chỉ có lập luận "Vì SA ⊥ (ABCD) nên AC là hình chiếu" | — | 1. Bấm "Kiểm tra lại" | • Bước 1 hiện "Không kiểm được" màu xám với lý do "bước không có phép tính"; không tính vào số bước lệch | US-S2E5-02-SC3; BR-S2E5-07 |
| TC-S2E5-018 | Lệch 0,0018 (6,93 so với 6,9282) là Đạt | High | Boundary (Edge case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Giáo viên Cô Trang (FX-01) | • Bước ghi 6,93; máy tính lại 6,9282 | • 6,93 và 6,9282 | 1. Chạy kiểm bước | • Đạt | US-S2E5-02-BV1; BR-S2E5-08; QA-S2E5-02 |
| TC-S2E5-019 | Lệch đúng 0,01 là Đạt | High | Boundary (Edge case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Giáo viên Cô Trang (FX-01) | • Bước ghi 6,94; máy tính lại 6,93 | • 6,94 và 6,93 | 1. Chạy kiểm bước | • Đạt (biên, dấu ≤ 0,01) | US-S2E5-02-BV2; BR-S2E5-08; QA-S2E5-02 |
| TC-S2E5-020 | Lệch 0,011 là Không đạt | High | Boundary (Edge case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Giáo viên Cô Trang (FX-01) | • Bước ghi 6,941; máy tính lại 6,93 | • 6,941 và 6,93 | 1. Chạy kiểm bước | • Không đạt kèm lý do ghi cả hai giá trị | US-S2E5-02-BV3; BR-S2E5-08; QA-S2E5-02 |
| TC-S2E5-021 | Biểu thức không đọc được là Không kiểm được, không lỗi hệ thống | High | Failure (Error case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Giáo viên Cô Trang (FX-01) | • Bước 3 ghi "tan SCA = abc" | — | 1. Bấm "Kiểm tra lại" | • Bước 3 "Không kiểm được" với lý do "không đọc được biểu thức"; các bước khác vẫn kiểm; không lỗi 500 | US-S2E5-02-FC1; BR-S2E5-07 |
| TC-S2E5-022 | Sửa bước 3 thì bước 3 và sau về Chưa kiểm, bước trước giữ Đạt | High | Normal (Success case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Giáo viên Cô Trang (FX-01) | • v2 đã kiểm: bước 1 Không kiểm được, bước 2 Đạt, bước 3 đỏ, bước 4 Đạt | — | 1. Sửa bước 3 và lưu thành v3 | • Ở v3: bước 1 và 2 giữ kết quả cũ; bước 3 và 4 hiện "Chưa kiểm"; v2 giữ kết quả của v2 | US-S2E5-02-SC4; BR-S2E5-10 |
| TC-S2E5-023 | Xem v1 hiện kết quả kiểm của v1, không phải của v2 | Medium | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • v1 chưa kiểm; v2 đã kiểm | — | 1. Xem v1 rồi xem v2 | • v1: mọi bước "Chưa kiểm"; v2: kết quả của v2 | US-S2E5-02-SC5; BR-S2E5-10 |
| TC-S2E5-024 | Kiểm 10 giây xong, 11 giây quá hạn | High | Boundary (Edge case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Giáo viên Cô Trang (FX-01) | • Engine giả lập trả lời sau 10 giây (lần 1) và 11 giây (lần 2), đồng hồ giả | — | 1. Bấm "Kiểm tra lại" hai lần | • Lần 1: có kết quả<br>• lần 2: TIMEOUT; mọi bước "Chưa kiểm"; có nút "Thử lại"; nhãn máy không đổi sang "Đã kiểm chứng" | US-S2E5-02-BV4; BR-S2E5-11; QA-S2E5-04 |
| TC-S2E5-025 | Lời giải 30 bước có đủ 30 kết quả | Medium | Boundary (Edge case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Giáo viên Cô Trang (FX-01) | • Lời giải 30 bước | — | 1. Bấm "Kiểm tra lại" | • Có đúng 30 kết quả theo thứ tự bước; không bước nào thiếu | US-S2E5-02-BV5; BR-S2E5-07 |
| TC-S2E5-026 | Học sinh không đọc được StepCheck | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Học sinh An (FX-02); đề đang giao | — | 1. Gọi lấy kết quả `StepCheck` của đề | • FORBIDDEN hoặc NOT_FOUND; dữ liệu trả về của mọi yêu cầu của An không chứa `StepCheck` | US-S2E5-02-FC2; BR-S2E5-11 |
| TC-S2E5-027 | Bước đầu vào thiếu giá trị bước trước thì không kiểm được | Medium | Failure (Error case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Giáo viên Cô Trang (FX-01) | • Bước 2 bị xóa; bước 3 dùng AC từ bước 2 | — | 1. Bấm "Kiểm tra lại" | • Bước 3 "Không kiểm được" với lý do "thiếu giá trị đầu vào từ bước trước"; không báo Đạt | US-S2E5-02-FC3; BR-S2E5-08 |

### US-S2E5-03 — Nhãn hai trục và dòng "Máy đã kiểm tra phần tính toán"; chỉ engine gắn "Đã kiểm chứng"

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | Status | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S2E5-028 | Lời giải AI chưa kiểm hiện Cần xem lại và AI soạn cùng nhau | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) cho nhãn phía học sinh | • Lời giải v1 mới sinh, chưa kiểm | — | 1. Mở C2-03 | • Đầu thẻ hình và hộp duyệt đều hiện "Cần xem lại" (vàng) và "AI soạn" (tím); không có nhãn "Đã kiểm chứng" | US-S2E5-03-SC1; DQ-07 |
| TC-S2E5-029 | Kiểm đạt toàn bộ hiện Đã kiểm chứng kèm dòng chú thích | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) cho nhãn phía học sinh | • Lời giải v3 đúng, đáp số 45° khớp máy, mọi bước phép tính Đạt | — | 1. Bấm "Kiểm tra lại" | • Nhãn máy "Đã kiểm chứng"<br>• dòng "Máy đã kiểm tra phần tính toán" ngay cạnh nhãn; trục người vẫn "AI soạn" hoặc "Đang sửa" (không phải "Giáo viên đã duyệt") | US-S2E5-03-SC2; BR-S2E5-12; BR-S2E5-14 |
| TC-S2E5-030 | Duyệt không đổi nhãn máy | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) cho nhãn phía học sinh | • Lời giải v2 nhãn máy "Cần xem lại" (chưa kiểm xong) | — | 1. Duyệt và xác nhận cảnh báo | • Trục người "Giáo viên đã duyệt"; trục máy vẫn "Cần xem lại" | US-S2E5-03-SC3; BR-S2E5-15; PO-38 |
| TC-S2E5-031 | Không thao tác nào đặt được nhãn Đã kiểm chứng từ phía người dùng | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) cho nhãn phía học sinh | • Lời giải nhãn máy "Cần xem lại"; Cô Trang đã duyệt | — | 1. Gọi API đặt nhãn máy "Đã kiểm chứng" (thân yêu cầu có trường nhãn) | • FORBIDDEN hoặc VALIDATION_FAILED; nhãn máy không đổi; giao diện không có nút nào làm việc này | US-S2E5-03-FC1; BR-S2E5-15; PO-02 |
| TC-S2E5-032 | Còn bước Không đạt thì không thể Đã kiểm chứng dù đã duyệt | High | Failure (Error case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) cho nhãn phía học sinh | • Lời giải có một bước Không đạt; trục người "Giáo viên đã duyệt" (do duyệt trước khi sửa) | — | 1. Chạy kiểm tra lại | • Nhãn máy "Cần xem lại" | US-S2E5-03-FC2; BR-S2E5-12 |
| TC-S2E5-033 | Mọi bước Không kiểm được thì Cần xem lại | High | Boundary (Edge case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) cho nhãn phía học sinh | • Lời giải chỉ có các bước lập luận chữ (không phép tính) | — | 1. Chạy kiểm tra lại | • Nhãn máy "Cần xem lại" vì không có bước Đạt | US-S2E5-03-FC3; BR-S2E5-12; QA-S2E5-05 |
| TC-S2E5-034 | Mọi bước Đạt nhưng đáp số cuối lệch engine thì Cần xem lại | High | Failure (Error case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) cho nhãn phía học sinh | • Mọi bước Đạt theo giá trị bước trước nhưng đáp số cuối ghi 54,7° (engine 45°) | — | 1. Chạy kiểm tra lại | • Nhãn máy "Cần xem lại"<br>• hộp duyệt báo lệch đáp số | US-S2E5-03-FC4; BR-S2E5-12 |
| TC-S2E5-035 | Sửa sau khi Đã kiểm chứng thì về Cần xem lại | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) cho nhãn phía học sinh | • Lời giải v3 đang "Đã kiểm chứng" | — | 1. Sửa bước 2 thành "AC = a√3"; lưu v4 | • v4: nhãn máy "Cần xem lại" cho tới khi kiểm lại đạt; v3 vẫn "Đã kiểm chứng" | US-S2E5-03-SC4; BR-S2E5-16; PO-48 |
| TC-S2E5-036 | Học sinh thấy nhãn của phiên bản đã giao | Medium | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) cho nhãn phía học sinh | • Bài giao dùng v2 nhãn "Cần xem lại" và "Giáo viên đã duyệt" | — | 1. An mở bài trên iPad | • Thẻ hình hiện đủ hai nhãn của v2; không hiện nhãn của phiên bản mới hơn chưa áp dụng | US-S2E5-03-SC5; BR-S2E5-14 |
| TC-S2E5-037 | Bộ câu thử bài sai không có mẫu nào mang Đã kiểm chứng | High | Failure (Error case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) cho nhãn phía học sinh | • Bộ 10 lời giải sai cố ý (sai bước tính, sai đáp số, sai đơn vị, thiếu bước) | • 10 lời giải sai | 1. Chạy kiểm cho từng lời giải | • Không lời giải nào nhận "Đã kiểm chứng" (M-S2-03 = 0) | US-S2E5-03-FC5; M-S2-03; BR-S2E5-12 |
| TC-S2E5-038 | Hai nhãn hiện đúng cả trên iPhone | Low | Normal (Success case) | chức năng | thủ công, có lý do | Manual — cần máy iPhone thật xét cắt chữ | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) cho nhãn phía học sinh | • An mở bài trên iPhone 390 × 844 | — | 1. Mở bài | • Hai nhãn không bị cắt chữ; dòng chú thích xuống hàng gọn; không cuộn ngang | US-S2E5-03-SC6; BR-S2E5-14 |

### US-S2E5-04 — Hộp duyệt chung: nhận, sửa, bỏ

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | Status | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S2E5-039 | Duyệt khi máy Đã kiểm chứng | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Lời giải v3 nhãn máy "Đã kiểm chứng" | — | 1. Bấm "Duyệt lời giải này" | • Trục người "Giáo viên đã duyệt"; ghi mã `GV-7K2M-9Q4D`, giờ, phiên bản v3; đề vào "Của tôi"; không hộp cảnh báo | US-S2E5-04-SC1; BR-S2E5-19; STAGES Ch2 mục 5 |
| TC-S2E5-040 | Duyệt khi Cần xem lại phải xác nhận cảnh báo | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Lời giải v2 nhãn "Cần xem lại" (chưa kiểm), không bước Không đạt | — | 1. Bấm "Duyệt lời giải này"; ở hộp cảnh báo bấm "Vẫn duyệt" | • Hiện hộp "Nhãn máy vẫn Cần xem lại. Vẫn duyệt?"<br>• sau "Vẫn duyệt": trục người "Giáo viên đã duyệt", trục máy vẫn "Cần xem lại" | US-S2E5-04-SC2; BR-S2E5-18; DQ-05 |
| TC-S2E5-041 | Hủy ở hộp cảnh báo thì không duyệt | Medium | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Như SC2 | — | 1. Bấm "Duyệt lời giải này"; ở hộp cảnh báo bấm "Quay lại" | • Không duyệt; trục người giữ nguyên; không bản ghi duyệt | US-S2E5-04-SC3; BR-S2E5-18 |
| TC-S2E5-042 | Còn một bước lệch thì Duyệt khóa | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Lời giải v2 có đúng 1 bước Không đạt (bước 3) | — | 1. Xem nút Duyệt; bấm thử; gọi API duyệt | • Nút khóa kèm "Chặn duyệt: còn 1 bước lệch"<br>• API: CONFLICT; không bản ghi duyệt | US-S2E5-04-FC1; BR-S2E5-17; DQ-04 |
| TC-S2E5-043 | 0 bước lệch thì Duyệt mở, 1 bước lệch thì khóa | High | Boundary (Edge case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Hai lời giải: A không có bước Không đạt; B có đúng 1 bước Không đạt | — | 1. Xem nút Duyệt của từng lời giải | • A: Duyệt mở<br>• B: Duyệt khóa "còn 1 bước lệch" | US-S2E5-04-BV1; BR-S2E5-17 |
| TC-S2E5-044 | Còn 3 bước lệch thì thông báo đúng số | Medium | Boundary (Edge case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Lời giải có 3 bước Không đạt | — | 1. Xem hộp duyệt | • Hiện "Chặn duyệt: còn 3 bước lệch" | US-S2E5-04-BV2; BR-S2E5-17 |
| TC-S2E5-045 | Nhận đề xuất của AI thành phiên bản mới | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • AI giả lập đề xuất thêm bước giải thích sau bước 2 | — | 1. Bấm "Nhận" | • Tạo phiên bản mới có bước thêm; hộp duyệt mất đề xuất đó; lịch sử ghi "nhận đề xuất của AI" | US-S2E5-04-SC4; BR-S2E5-20 |
| TC-S2E5-046 | Sửa đề xuất mở soạn với nội dung đề xuất | Medium | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Đề xuất AI chờ | — | 1. Bấm "Sửa" | • Mở trình soạn với nội dung đề xuất đã điền; chưa tạo phiên bản cho tới khi bấm "Lưu" | US-S2E5-04-SC5; BR-S2E5-20 |
| TC-S2E5-047 | Bỏ đề xuất thì loại và còn dấu vết | Medium | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Đề xuất AI chờ | — | 1. Bấm "Bỏ" | • Đề xuất rời hộp duyệt; lịch sử ghi "đã bỏ"; phiên bản không đổi | US-S2E5-04-SC6; BR-S2E5-20 |
| TC-S2E5-048 | Bấm Duyệt hai lần chỉ ghi một lần | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Lời giải v3 đã sẵn sàng duyệt | — | 1. Bấm "Duyệt lời giải này" hai lần trong 1 giây | • Chỉ một bản ghi duyệt; không lỗi cho lần hai (kết quả như lần một) | US-S2E5-04-FC2; BR-S2E5-21 |
| TC-S2E5-049 | Duyệt phiên bản đã bị thay thế nhận CONFLICT | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Đề có v2 (đã duyệt) và v3 (đang hiện hành, chưa duyệt) | — | 1. Gọi API duyệt trên v2 sau khi v3 đã có | • CONFLICT với thông báo "Phiên bản đã cũ"; không thay đổi nhãn | US-S2E5-04-FC3; BR-S2E5-21 |
| TC-S2E5-050 | Duyệt phiên bản mới thì bản duyệt trước thành Bị thay thế | High | Normal (Success case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Giáo viên Cô Trang (FX-01) | • v2 đã duyệt; v3 sẵn sàng duyệt | — | 1. Duyệt v3 | • v3 "Giáo viên đã duyệt"; v2 "Bị thay thế"; đề chỉ có một bản duyệt hiện hành | US-S2E5-04-SC7; BR-S2E5-19 |
| TC-S2E5-051 | Giáo viên khác không duyệt được đề của người khác | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Đề Của tôi của Cô Trang; Cô Lan đăng nhập | — | 1. Cô Lan gọi API duyệt | • NOT_FOUND; không bản ghi duyệt | US-S2E5-04-FC4; BR-S2E5-21; BR-S2-06 |
| TC-S2E5-052 | Hộp duyệt rỗng | Low | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Không còn đề xuất nào chờ | — | 1. Mở hộp duyệt | • Hiện "Không có gì chờ duyệt"; nút Duyệt vẫn theo trạng thái lời giải | US-S2E5-04-FC5; 0.3 ý 4 |
| TC-S2E5-065 | Luồng đầy đủ: AI soạn sai, sửa, kiểm, duyệt, giao | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Đề FX-14 có lời giải AI v1 (bước 3 sai)<br>• chưa giao | • FX-14 | 1. Mở C2-03 và bấm "Kiểm tra lại"<br>2. sửa bước 3 và 4, lưu v2<br>3. bấm "Kiểm tra lại"<br>4. bấm "Duyệt lời giải này"<br>5. giao cho lớp 11A1 | • Lần kiểm 1: bước 3 đỏ, Duyệt khóa<br>• sau sửa: mọi bước Đạt, nhãn máy "Đã kiểm chứng" và "Máy đã kiểm tra phần tính toán"<br>• sau duyệt: "Giáo viên đã duyệt"<br>• bài giao dùng v2; lịch sử có v1 và v2 | US-S2E5-04 (case viết tay); chuỗi US-S2E5-01…05 (luồng A của STAGES) |

### US-S2E5-05 — Lời giải đã giao bất biến; sửa tạo phiên bản mới và chọn có áp cho bài đang giao

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | Status | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S2E5-053 | Sửa đề đã giao tạo phiên bản mới, bản đã giao không đổi | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • BG-1 và BG-2 đang dùng v2 | • FX-14 | 1. Sửa bước 4 của đề rồi bấm "Lưu" | • Tạo v3; BG-1 và BG-2 vẫn dùng v2 (mặc định "Giữ v2"); hiện hộp chọn "Áp dụng cho lớp" hay "Giữ v2" cho từng bài giao | US-S2E5-05-SC1; STAGES Ch2 mục 7; DQ-06 |
| TC-S2E5-054 | Hộp đã giao hiện đúng phiên bản đang dùng | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • BG-1 dùng v2, BG-2 dùng v2 | — | 1. Mở C2-03 trạng thái đã giao | • Thấy "Đã giao cho 11A1 (v2)" và "Đã giao cho 11A2 (v2)"; chú thích "Bản đã giao không sửa được, sửa sẽ tạo phiên bản mới" | US-S2E5-05-SC2; DQ-06 |
| TC-S2E5-055 | Sửa trực tiếp bản đã giao nhận CONFLICT | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • v2 đang giao | — | 1. Gọi API ghi đè nội dung v2 | • CONFLICT với thông báo "Bản đã giao không sửa được, hãy tạo phiên bản mới"; v2 không đổi từng ký tự | US-S2E5-05-FC1; BR-S2E5-22 |
| TC-S2E5-056 | Phiên bản chưa duyệt không áp dụng được | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • v3 chưa duyệt | — | 1. Xem nút "Áp dụng cho lớp"; gọi API áp dụng | • Nút khóa kèm "Duyệt v3 trước"; API: CONFLICT; bài giao vẫn v2 | US-S2E5-05-FC2; BR-S2E5-24; QA-S2E5-06 |
| TC-S2E5-057 | Duyệt v3 rồi áp dụng cho một bài giao | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • v3 đã duyệt; BG-1 chưa có học sinh nào xem lời giải | — | 1. Chọn "Áp dụng cho lớp" ở BG-1 | • BG-1 chuyển sang v3; hộp ghi "Đã giao cho 11A1 (v3)"; BG-2 vẫn v2 | US-S2E5-05-SC3; BR-S2E5-23 |
| TC-S2E5-058 | Áp cho BG-1 không đổi BG-2 cùng đề | High | Boundary (Edge case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • BG-1 và BG-2 cùng đề; v3 đã duyệt | — | 1. Áp dụng v3 chỉ cho BG-1 | • BG-1 dùng v3; BG-2 vẫn dùng v2; học sinh 11A2 thấy v2 | US-S2E5-05-BV1; BR-S2E5-23 |
| TC-S2E5-059 | Chọn Giữ v2 thì bài giao không đổi | Medium | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • v3 đã duyệt | — | 1. Chọn "Giữ v2" cho cả hai bài giao | • Cả hai vẫn v2; v3 chỉ nằm ở "Của tôi" | US-S2E5-05-SC4; BR-S2E5-23 |
| TC-S2E5-060 | Áp dụng cho bài đã có người xem lời giải cũ phải xác nhận | High | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • BG-1 đã có 2 học sinh xem lời giải v2; v3 đã duyệt | — | 1. Chọn "Áp dụng cho lớp" ở BG-1 | • Hiện cảnh báo "2 học sinh đã xem bản v2. Vẫn áp dụng?"; chỉ khi xác nhận mới chuyển v3; hủy thì giữ v2 | US-S2E5-05-FC3; BR-S2E5-25 |
| TC-S2E5-061 | Đổi đáp số thì cảnh báo kết quả không tính lại | High | Failure (Error case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • v3 có đáp số 60° thay vì 45° của v2; BG-1 đã có bài luyện được chấm | — | 1. Chọn "Áp dụng cho lớp" ở BG-1 | • Hiện "Đáp số đổi từ 45° thành 60°; kết quả đã chấm không tính lại"; sau xác nhận, kết quả đã chấm giữ nguyên | US-S2E5-05-FC4; BR-S2E5-26; QA-S2E5-07 |
| TC-S2E5-062 | Áp dụng không mở lời giải sớm hơn giờ mở | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • BG-1 chưa tới `release_at`; v3 đã duyệt | — | 1. Áp dụng v3 cho BG-1; An gọi lấy lời giải | • Dữ liệu trả về cho An không chứa bước giải v3 hay đáp số ở bất kỳ dạng viết nào; tiêu đề `Cache-Control: no-store` | US-S2E5-05-FC5; BR-S2E5-25; STAGES mục 0.2 nguyên tắc 1 |
| TC-S2E5-063 | Giáo viên khác không áp dụng được | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • Đề của Cô Trang; Cô Lan đăng nhập | — | 1. Cô Lan gọi API áp dụng v3 | • NOT_FOUND; bài giao không đổi | US-S2E5-05-FC6; BR-S2E5-23; BR-S2-06 |
| TC-S2E5-064 | Học sinh đang mở bài vẫn thấy ổn định, lần tải sau thấy v3 | Medium | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • An đang mở BG-1 ở iPad; sau đó giáo viên áp dụng v3 | — | 1. An tải lại bài | • Trước khi tải lại nội dung bài không đổi; sau tải lại hiện nhãn và nội dung theo v3 | US-S2E5-05-SC5; BR-S2E5-23 |
| TC-S2E5-066 | Hồi quy khóa lời giải: bộ câu thử moi đáp số cho bài giao phiên bản mới | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Học sinh An (FX-02) | • BG-1 chưa tới `release_at`; v3 đã áp dụng | • Bộ câu thử moi đáp số của MVP | 1. Chạy bộ câu thử moi đáp số (hỏi đáp, gợi ý lặp, đổi dạng viết) với bài giao dùng v3 | • Không câu thử nào lấy được đáp số hay bước giải; kết quả như bộ MVP | US-S2E5-05 (case viết tay); BRD delta mục 4 (một cổng công bố, bộ câu thử chạy lại) |

## 3. Truy vết tiêu chí sang case

| Tiêu chí | Case |
|---|---|
| US-S2E5-01-SC1 | TC-S2E5-001 |
| US-S2E5-01-SC2 | TC-S2E5-002 |
| US-S2E5-01-SC3 | TC-S2E5-003 |
| US-S2E5-01-SC4 | TC-S2E5-004 |
| US-S2E5-01-SC5 | TC-S2E5-005 |
| US-S2E5-01-FC1 | TC-S2E5-006 |
| US-S2E5-01-FC2 | TC-S2E5-007 |
| US-S2E5-01-BV1 | TC-S2E5-008 |
| US-S2E5-01-BV2 | TC-S2E5-009 |
| US-S2E5-01-BV3 | TC-S2E5-010 |
| US-S2E5-01-BV4 | TC-S2E5-011 |
| US-S2E5-01-FC3 | TC-S2E5-012 |
| US-S2E5-01-FC4 | TC-S2E5-013 |
| US-S2E5-01-FC5 | TC-S2E5-014 |
| US-S2E5-02-SC1 | TC-S2E5-015 |
| US-S2E5-02-SC2 | TC-S2E5-016 |
| US-S2E5-02-SC3 | TC-S2E5-017 |
| US-S2E5-02-BV1 | TC-S2E5-018 |
| US-S2E5-02-BV2 | TC-S2E5-019 |
| US-S2E5-02-BV3 | TC-S2E5-020 |
| US-S2E5-02-FC1 | TC-S2E5-021 |
| US-S2E5-02-SC4 | TC-S2E5-022 |
| US-S2E5-02-SC5 | TC-S2E5-023 |
| US-S2E5-02-BV4 | TC-S2E5-024 |
| US-S2E5-02-BV5 | TC-S2E5-025 |
| US-S2E5-02-FC2 | TC-S2E5-026 |
| US-S2E5-02-FC3 | TC-S2E5-027 |
| US-S2E5-03-SC1 | TC-S2E5-028 |
| US-S2E5-03-SC2 | TC-S2E5-029 |
| US-S2E5-03-SC3 | TC-S2E5-030 |
| US-S2E5-03-FC1 | TC-S2E5-031 |
| US-S2E5-03-FC2 | TC-S2E5-032 |
| US-S2E5-03-FC3 | TC-S2E5-033 |
| US-S2E5-03-FC4 | TC-S2E5-034 |
| US-S2E5-03-SC4 | TC-S2E5-035 |
| US-S2E5-03-SC5 | TC-S2E5-036 |
| US-S2E5-03-FC5 | TC-S2E5-037 |
| US-S2E5-03-SC6 | TC-S2E5-038 |
| US-S2E5-04-SC1 | TC-S2E5-039 |
| US-S2E5-04-SC2 | TC-S2E5-040 |
| US-S2E5-04-SC3 | TC-S2E5-041 |
| US-S2E5-04-FC1 | TC-S2E5-042 |
| US-S2E5-04-BV1 | TC-S2E5-043 |
| US-S2E5-04-BV2 | TC-S2E5-044 |
| US-S2E5-04-SC4 | TC-S2E5-045 |
| US-S2E5-04-SC5 | TC-S2E5-046 |
| US-S2E5-04-SC6 | TC-S2E5-047 |
| US-S2E5-04-FC2 | TC-S2E5-048 |
| US-S2E5-04-FC3 | TC-S2E5-049 |
| US-S2E5-04-SC7 | TC-S2E5-050 |
| US-S2E5-04-FC4 | TC-S2E5-051 |
| US-S2E5-04-FC5 | TC-S2E5-052 |
| US-S2E5-05-SC1 | TC-S2E5-053 |
| US-S2E5-05-SC2 | TC-S2E5-054 |
| US-S2E5-05-FC1 | TC-S2E5-055 |
| US-S2E5-05-FC2 | TC-S2E5-056 |
| US-S2E5-05-SC3 | TC-S2E5-057 |
| US-S2E5-05-BV1 | TC-S2E5-058 |
| US-S2E5-05-SC4 | TC-S2E5-059 |
| US-S2E5-05-FC3 | TC-S2E5-060 |
| US-S2E5-05-FC4 | TC-S2E5-061 |
| US-S2E5-05-FC5 | TC-S2E5-062 |
| US-S2E5-05-FC6 | TC-S2E5-063 |
| US-S2E5-05-SC5 | TC-S2E5-064 |
| US-S2E5-04 | TC-S2E5-065 |
| US-S2E5-05 | TC-S2E5-066 |

### Khoảng trống

- Không tiêu chí nào thiếu case. Case chưa chạy được vì code chặng 2 chưa có (PO-65 Q1): trạng thái Not run.
- Câu hỏi và đề xuất đổi story map: `questions.md` cùng thư mục.

## 4. Dữ liệu và môi trường

- Dữ liệu: bảng `FX-nn` đầu tệp (đồng bộ với `stories_ac.md`).
- Môi trường: profile `dev` / `test`, AI giả lập, không bao giờ chạy trên `prod` (PO-51). Thiết bị theo mục quy ước của `stories_ac.md`.

## 5. Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: 66 case cho 64 tiêu chí, có cột Tự động hóa | runner-T-261126-12 (QC) |
| v0.2 | 2026-11-26 | đổi mã mẫu `HS-`/`GV-` có ký tự 0, 1, O, I sang ký tự hợp lệ (BR-S2E1-04), RV_s23 m-01 | runner-T-261126-15 (QC soát phản biện, RV_s23) |
