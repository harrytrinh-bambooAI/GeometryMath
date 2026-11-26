Phiên bản: v0.1 · 2026-11-26
# S2-E7 — test case (Trình chiếu trên lớp: toàn màn hình, hiện từng bước, mất mạng, hỏi lại khi lời giải khóa)

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
| Trình chiếu | chế độ toàn màn hình của giáo viên để giảng; lời giải che sẵn, hiện từng bước | STAGES Ch2 mục 5 luồng B |
| che toàn cục | nút "Che lời giải" ẩn mọi bước đang hiện nhưng nhớ vị trí bước | DQ-08 |

### Dữ liệu mẫu chặng 2 (`FX-nn`)

| Mã | Nội dung | Giá trị |
|---|---|---|
| FX-01 | Giáo viên "Cô Trang" | mã `GV-7K2M-9Q4D`, mật khẩu `Trang@2026x` |
| FX-02 | Học sinh "An" | mã `HS-3F8A-2L6P`, mật khẩu `AnHoc#2026` |
| FX-03 | Học sinh "Bình" | mã `HS-5B1C-8R2T`, cùng lớp 11A1 với An |
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
| US-S2E7-01 Trình chiếu toàn màn hình, hiện từng bước, nút che lời giải | 14 | 14 | 13 / 1 |
| US-S2E7-02 Chạy được khi mất mạng; hỏi đáp AI tạm tắt kèm thông báo | 9 | 9 | 9 / 0 |
| US-S2E7-03 Hỏi lại "Lớp sẽ thấy lời giải trên màn chiếu" khi lời giải đang khóa | 10 | 10 | 10 / 0 |
| **Tổng** | **33** | **33** | **32 / 1** |

| Theo ưu tiên | Số case |
|---|---|
| High | 21 |
| Medium | 12 |
| Low | 0 |

## 2. Test case

### US-S2E7-01 — Trình chiếu toàn màn hình, hiện từng bước, nút che lời giải

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | Status | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S2E7-001 | Vào Trình chiếu thấy hình lớn, đề và lời giải che sẵn | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Cô Trang mở đề FX-14 (lời giải 4 bước) ở C2-03 trên web | • FX-14 | 1. Bấm "Trình chiếu" | • Chuyển toàn màn hình; hiện hình lớn và chữ đề; không bước lời giải nào hiện; có thanh công cụ gọn với "Hiện bước kế", "Bước trước", "Che lời giải" | US-S2E7-01-SC1; BR-S2E7-01 |
| TC-S2E7-002 | Hiện bước kế hiện đúng một bước và tô màu hình | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Đang ở 0 bước hiện | — | 1. Bấm "Hiện bước kế" | • Bước 1 hiện; phần hình của bước 1 (AC là hình chiếu, góc SCA) tô màu; các bước 2 đến 4 chưa hiện | US-S2E7-01-SC2; BR-S2E7-02; STAGES Ch2 mục 5 |
| TC-S2E7-003 | Phím mũi tên và Space điều khiển bước | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Đang ở 1 bước hiện | — | 1. Nhấn →; nhấn Space; nhấn ← | • → : bước 2 hiện<br>• Space: bước 3 hiện<br>• ←: ẩn bước 3, còn 2 bước | US-S2E7-01-SC3; BR-S2E7-02 |
| TC-S2E7-004 | Che lời giải ẩn tất cả, bỏ che hiện lại đúng bước | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Đang hiện 3 bước | — | 1. Bấm "Che lời giải"; bấm lần nữa | • Lần 1: không bước nào hiện (cả chữ và phần tô màu của hình); lần 2: hiện lại đúng 3 bước | US-S2E7-01-SC4; BR-S2E7-03 |
| TC-S2E7-005 | Xoay hình, đo, thêm đường phụ thì hình đổi ngay | Medium | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Đang trình chiếu đề FX-14 | — | 1. Kéo xoay hình; đo đoạn AC; thêm đường phụ nối A với trung điểm BC | • Hình xoay theo tay kéo; đo hiện giá trị của AC; đường phụ vẽ ngay; các thao tác không đổi dữ liệu đề đã lưu | US-S2E7-01-SC5; STAGES Ch2 mục 5 luồng B |
| TC-S2E7-006 | Ở bước cuối nút Hiện bước kế khóa | High | Boundary (Edge case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Lời giải 4 bước, đang hiện cả 4 | — | 1. Bấm "Hiện bước kế" thêm một lần; nhấn → | • Nút khóa kèm "Hết bước"; phím → không đổi gì; vẫn 4 bước hiện | US-S2E7-01-BV1; BR-S2E7-02 |
| TC-S2E7-007 | Ở bước 0 nút Bước trước khóa | High | Boundary (Edge case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Mới vào Trình chiếu (0 bước hiện) | — | 1. Bấm "Bước trước"; nhấn ← | • Nút khóa; không đổi gì | US-S2E7-01-BV2; BR-S2E7-02 |
| TC-S2E7-008 | Lời giải 30 bước chiếu được đủ 30 bước | Medium | Boundary (Edge case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Đề có lời giải 30 bước (giới hạn của S2-E5) | — | 1. Bấm "Hiện bước kế" 30 lần | • Cả 30 bước hiện lần lượt; ở lần thứ 31 nút khóa; hiệu năng không giật (chuyển bước dưới 200 ms, TẠM) | US-S2E7-01-BV3; BR-S2E7-02 |
| TC-S2E7-009 | Esc thoát về C2-03 và che lại lời giải | High | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Đang chiếu, hiện 2 bước | — | 1. Nhấn Esc; bấm "Trình chiếu" lại | • Về C2-03; vào lại thấy 0 bước hiện (che sẵn) | US-S2E7-01-FC1; BR-S2E7-05 |
| TC-S2E7-010 | Trình duyệt từ chối toàn màn hình vẫn chiếu được | Medium | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Trình duyệt chặn toàn màn hình | — | 1. Bấm "Trình chiếu" | • Hiện bố cục cửa sổ lớn lấp đầy trang kèm dòng "Trình duyệt không cho toàn màn hình, nhấn F11"; mọi nút hoạt động | US-S2E7-01-FC2; BR-S2E7-01 |
| TC-S2E7-011 | Học sinh không vào được Trình chiếu | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Học sinh An | — | 1. An gọi trang và dữ liệu của Trình chiếu | • FORBIDDEN; không bước lời giải nào trả về | US-S2E7-01-FC3; BR-S2E7-05; STAGES mục 0.2 nguyên tắc 6 |
| TC-S2E7-012 | Đề chưa có lời giải thì khóa nút hiện bước | Medium | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Đề FX-12 chưa có lời giải | • FX-12 | 1. Bấm "Trình chiếu" | • Hiện hình và đề; nút "Hiện bước kế" khóa kèm "Chưa có lời giải" | US-S2E7-01-FC4; BR-S2E7-02 |
| TC-S2E7-013 | Hai nhãn luôn nằm ở góc màn hình | Medium | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Lời giải nhãn "Cần xem lại" và "Giáo viên đã duyệt" | — | 1. Trình chiếu; hiện bước | • Hai nhãn nhỏ hiện ở góc cả khi lời giải che và khi hiện | US-S2E7-01-SC6; BR-S2E7-06; DQ-07 |
| TC-S2E7-014 | Chữ lời giải đủ lớn để đọc từ cuối lớp | Medium | Normal (Success case) | chức năng | thủ công, có lý do | Manual — cần mắt người đánh giá độ đọc được trên màn chiếu | Not run | Giáo viên Cô Trang (FX-01) | • Màn 1920 × 1080; bước 3 dài 120 ký tự | — | 1. Hiện bước 3 | • Chữ lời giải không nhỏ hơn 28 điểm; không bị cắt; xuống dòng gọn | US-S2E7-01-SC7; BR-S2E7-04; QA-S2E7-01 |

### US-S2E7-02 — Chạy được khi mất mạng; hỏi đáp AI tạm tắt kèm thông báo

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | Status | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S2E7-015 | Vào Trình chiếu có mạng thì hiện chip Đã tải sẵn | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Có mạng; đề FX-14 | — | 1. Bấm "Trình chiếu" | • Sau khi tải xong hiện chip "Đã tải sẵn · mất mạng vẫn chiếu" | US-S2E7-02-SC1; BR-S2E7-07; DQ-10 |
| TC-S2E7-016 | Mất mạng giữa giờ vẫn hiện bước và xoay hình | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Đang chiếu, đã tải sẵn; ngắt mạng | — | 1. Bấm "Hiện bước kế" hai lần; kéo xoay hình; đo | • Hai bước hiện; hình xoay; đo hoạt động; không thông báo lỗi mạng cho các thao tác này | US-S2E7-02-SC2; BR-S2E7-08 |
| TC-S2E7-017 | Mất mạng thì hỏi đáp AI tắt kèm thông báo | High | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Đang chiếu; ngắt mạng | — | 1. Bấm nút hỏi đáp AI | • Nút ở trạng thái tắt; hiện "Hỏi đáp AI tạm tắt vì mất mạng"; không gửi yêu cầu | US-S2E7-02-FC1; BR-S2E7-08; STAGES Ch2 mục 5 |
| TC-S2E7-018 | Có mạng lại thì hỏi đáp AI bật lại | Medium | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Đang mất mạng, hỏi đáp AI tắt | — | 1. Bật lại mạng; chờ 5 giây | • Trong 5 giây nút hỏi đáp bật lại; thông báo mất mạng biến mất | US-S2E7-02-SC3; BR-S2E7-08; QA-S2E7-02 |
| TC-S2E7-019 | Bật lại sau 5 giây là hạn, 6 giây là quá | Medium | Boundary (Edge case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Giáo viên Cô Trang (FX-01) | • Mất mạng; đồng hồ giả | — | 1. Có mạng lại; kiểm nút ở 5 giây và ở 6 giây | • Đến mốc 5 giây nút đã bật; không trễ tới 6 giây | US-S2E7-02-BV1; BR-S2E7-08; QA-S2E7-02 |
| TC-S2E7-020 | Vào Trình chiếu khi đã mất mạng và chưa tải sẵn | High | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Mất mạng; chưa mở đề này trước đó | — | 1. Bấm "Trình chiếu" | • Hiện "Cần mạng để mở lần đầu" và nút "Thử lại"; không vào chế độ chiếu; không hiện hình nửa vời | US-S2E7-02-FC2; BR-S2E7-09 |
| TC-S2E7-021 | Mất mạng khi đang tải sẵn thì báo chưa đủ | Medium | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Đang tải sẵn 2 trong 4 bước; ngắt mạng | — | 1. Chờ tải | • Hiện "Chưa tải sẵn đủ, cần mạng"; nút "Thử lại"; không cho vào chế độ chiếu | US-S2E7-02-FC3; BR-S2E7-09 |
| TC-S2E7-022 | Thoát Trình chiếu xóa dữ liệu tải sẵn | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Đã chiếu đề FX-14, tải sẵn 4 bước | — | 1. Nhấn Esc; kiểm bộ nhớ trình duyệt (cache, lưu trữ cục bộ) | • Không còn nội dung lời giải của đề trong bộ nhớ trình duyệt | US-S2E7-02-FC4; BR-S2E7-10; QA-S2E7-03 |
| TC-S2E7-023 | Phiên hết hạn lúc mất mạng thì chiếu tiếp, về lại thì đăng nhập | Medium | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Đang chiếu; phiên hết hạn trong lúc mất mạng | — | 1. Tiếp tục hiện bước; bật lại mạng; gọi yêu cầu cần xác thực | • Chiếu vẫn tiếp tục khi mất mạng; khi có mạng, yêu cầu có xác thực nhận UNAUTHENTICATED và hiện "Phiên đã hết. Vui lòng vào lại." sau khi giáo viên thoát chiếu, không ngắt buổi chiếu đột ngột | US-S2E7-02-FC5; BR-S2E7-08 |

### US-S2E7-03 — Hỏi lại "Lớp sẽ thấy lời giải trên màn chiếu" khi lời giải đang khóa

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | Status | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S2E7-024 | Đề đang giao và lời giải khóa thì hỏi lại | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Đề FX-14 giao BG-1 (11A1), lời giải mở lúc T0 + 2 giờ | • FX-14 | 1. Bấm "Trình chiếu" lúc T0 | • Hiện hộp "Lớp sẽ thấy lời giải trên màn chiếu, tiếp tục?" nêu "11A1" và hai nút "Tiếp tục", "Hủy"; chưa vào chế độ chiếu | US-S2E7-03-SC1; BR-S2E7-11 |
| TC-S2E7-025 | Tiếp tục thì vào chiếu, máy học sinh vẫn khóa | High | Normal (Success case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Hộp hỏi đang hiện (SC1) | — | 1. Bấm "Tiếp tục"; hiện vài bước; An (học sinh 11A1) gọi lời giải bài giao | • Vào Trình chiếu che sẵn với chip "Máy học sinh vẫn khóa lời giải"; dữ liệu trả về cho An không chứa bước giải hay đáp số | US-S2E7-03-SC2; BR-S2E7-13; DQ-09 |
| TC-S2E7-026 | Hủy thì ở lại và không thay đổi gì | Medium | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Hộp hỏi đang hiện | — | 1. Bấm "Hủy" | • Ở lại C2-03; không toàn màn hình; không bản ghi chiếu | US-S2E7-03-SC3; BR-S2E7-13 |
| TC-S2E7-027 | Đề chưa giao thì không hỏi | High | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Đề FX-12 chưa giao lớp nào | • FX-12 | 1. Bấm "Trình chiếu" | • Vào thẳng Trình chiếu; không hộp hỏi | US-S2E7-03-FC1; BR-S2E7-12 |
| TC-S2E7-028 | Bài giao đã mở lời giải thì không hỏi | High | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Đề FX-14 chỉ giao BG-2 (11A2) với `release_at` = T0 - 1 giờ | — | 1. Bấm "Trình chiếu" | • Vào thẳng; không hộp hỏi | US-S2E7-03-FC2; BR-S2E7-12 |
| TC-S2E7-029 | Giờ máy chủ bằng release_at thì không hỏi, trước 1 giây thì hỏi | High | Boundary (Edge case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Giáo viên Cô Trang (FX-01) | • Bài giao có `release_at` = R; đồng hồ giả | — | 1. Bấm "Trình chiếu" ở R - 1 giây; bấm ở đúng R | • R - 1 giây: hỏi<br>• đúng R: không hỏi (đã mở) | US-S2E7-03-BV1; BR-S2E7-11, BR-S2E7-12; M-GEO-13 |
| TC-S2E7-030 | Chưa đặt giờ mở thì cũng hỏi | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Đề giao BG-3 chưa đặt `release_at` | — | 1. Bấm "Trình chiếu" | • Hiện hộp hỏi nêu tên lớp của BG-3 | US-S2E7-03-SC4; BR-S2E7-11 |
| TC-S2E7-031 | Nhiều lớp thì nêu tên các lớp còn khóa | Medium | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • FX-14 giao BG-1 (11A1, khóa) và BG-2 (11A2, đã mở) | — | 1. Bấm "Trình chiếu" | • Hộp hỏi chỉ nêu "11A1" (lớp còn khóa), không nêu 11A2 | US-S2E7-03-SC5; BR-S2E7-11 |
| TC-S2E7-032 | Giờ máy giáo viên lệch không đổi kết quả hỏi | High | Failure (Error case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Giáo viên Cô Trang (FX-01) | • BG-1 `release_at` = T0 + 2 giờ; đồng hồ máy giáo viên chạy nhanh 3 giờ | — | 1. Bấm "Trình chiếu" | • Vẫn hiện hộp hỏi (theo giờ máy chủ) | US-S2E7-03-FC3; BR-S2E7-14 |
| TC-S2E7-033 | Học sinh không thể mở khóa bằng cách giáo viên bấm Tiếp tục | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Đã bấm "Tiếp tục" | — | 1. Học sinh An làm mới trang bài giao nhiều lần | • Bài giao vẫn khóa: không bước giải, không đáp số ở mọi dạng viết; `release_at` không đổi | US-S2E7-03-FC4; BR-S2E7-13 |

## 3. Truy vết tiêu chí sang case

| Tiêu chí | Case |
|---|---|
| US-S2E7-01-SC1 | TC-S2E7-001 |
| US-S2E7-01-SC2 | TC-S2E7-002 |
| US-S2E7-01-SC3 | TC-S2E7-003 |
| US-S2E7-01-SC4 | TC-S2E7-004 |
| US-S2E7-01-SC5 | TC-S2E7-005 |
| US-S2E7-01-BV1 | TC-S2E7-006 |
| US-S2E7-01-BV2 | TC-S2E7-007 |
| US-S2E7-01-BV3 | TC-S2E7-008 |
| US-S2E7-01-FC1 | TC-S2E7-009 |
| US-S2E7-01-FC2 | TC-S2E7-010 |
| US-S2E7-01-FC3 | TC-S2E7-011 |
| US-S2E7-01-FC4 | TC-S2E7-012 |
| US-S2E7-01-SC6 | TC-S2E7-013 |
| US-S2E7-01-SC7 | TC-S2E7-014 |
| US-S2E7-02-SC1 | TC-S2E7-015 |
| US-S2E7-02-SC2 | TC-S2E7-016 |
| US-S2E7-02-FC1 | TC-S2E7-017 |
| US-S2E7-02-SC3 | TC-S2E7-018 |
| US-S2E7-02-BV1 | TC-S2E7-019 |
| US-S2E7-02-FC2 | TC-S2E7-020 |
| US-S2E7-02-FC3 | TC-S2E7-021 |
| US-S2E7-02-FC4 | TC-S2E7-022 |
| US-S2E7-02-FC5 | TC-S2E7-023 |
| US-S2E7-03-SC1 | TC-S2E7-024 |
| US-S2E7-03-SC2 | TC-S2E7-025 |
| US-S2E7-03-SC3 | TC-S2E7-026 |
| US-S2E7-03-FC1 | TC-S2E7-027 |
| US-S2E7-03-FC2 | TC-S2E7-028 |
| US-S2E7-03-BV1 | TC-S2E7-029 |
| US-S2E7-03-SC4 | TC-S2E7-030 |
| US-S2E7-03-SC5 | TC-S2E7-031 |
| US-S2E7-03-FC3 | TC-S2E7-032 |
| US-S2E7-03-FC4 | TC-S2E7-033 |

### Khoảng trống

- Không tiêu chí nào thiếu case. Case chưa chạy được vì code chặng 2 chưa có (PO-65 Q1): trạng thái Not run.
- Câu hỏi và đề xuất đổi story map: `questions.md` cùng thư mục.

## 4. Dữ liệu và môi trường

- Dữ liệu: bảng `FX-nn` đầu tệp (đồng bộ với `stories_ac.md`).
- Môi trường: profile `dev` / `test`, AI giả lập, không bao giờ chạy trên `prod` (PO-51). Thiết bị theo mục quy ước của `stories_ac.md`.

## 5. Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: 33 case cho 33 tiêu chí, có cột Tự động hóa | runner-T-261126-12 (QC) |
