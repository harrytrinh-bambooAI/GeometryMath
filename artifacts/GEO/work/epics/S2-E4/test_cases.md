Phiên bản: v0.2 · 2026-11-26
# S2-E4 — test case (Chat chỉnh nháp đề, import docx/pdf, đối chiếu trùng với kho)

> Chủ sở hữu: QC (goal run T-261126-12). **Trạng thái: ready for R3** · Mức epic: **P1**. Case bám tiêu chí của `stories_ac.md` cùng thư mục; thiết kế ở `test_design.md`. Mọi case ở trạng thái Not run: đây là bản viết, chưa thực thi (code chặng 2 chờ B7, PO-65).

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
| `QA-S2En-nn` | câu hỏi mở của epic, luôn kèm mặc định (đã chốt PO-66) | `questions.md` cùng thư mục |
| `TC-S2En-nnn` | test case của epic | `test_cases.md` cùng thư mục |
| `FX-nn` | dữ liệu mẫu chặng 2 (bảng dưới) | tệp này |
| `BG-n` | bài giao mẫu của MVP (BG-1…BG-5) | `artifacts/GEO/work/fixture.md` (MVP) |
| `BR-GEO-nnn` | quy tắc nghiệp vụ của MVP còn hiệu lực | `artifacts/GEO/work/02_gate2_brd.md` (MVP) |
| `MSG-GEO-nn` | thông điệp hiện cho người dùng của MVP (câu chữ nguyên văn) | `artifacts/GEO/work/05_dictionary.md` (MVP) |
| Đã chốt PO-66 | mặc định đã được chủ đầu tư chốt ở PO-66 (26/11/2026); ghi theo mẫu `quyết định · tin cậy · nguồn`; ngưỡng chỉnh sau L6 (G12) | PO-65, PO-66 |
| Nhãn hai trục | trục máy: "Đã kiểm chứng" / "Cần xem lại" (chỉ engine gắn) · trục người: "AI soạn" / "Giáo viên đã duyệt" | PO-02 |
| `release_at` | giờ mở lời giải; máy chủ so giờ máy chủ, không tin giờ thiết bị | MVP, M-GEO-13 |
| nháp | bản đề hoặc lời giải do AI soạn hoặc giáo viên đang sửa, chưa vào giáo trình; chỉ giáo viên bấm duyệt mới vào | PO-30, PO-46 |
| đề xuất | một thay đổi AI nêu trong chat (có trước / sau, nguồn, chỗ chưa chắc); giáo viên nhận, sửa tay hoặc bác | PO-46 |

### Dữ liệu mẫu chặng 2 (`FX-nn`)

| Mã | Nội dung | Giá trị |
|---|---|---|
| FX-01 | Giáo viên "Cô Trang" | mã `GV-7K2M-9Q4D`, mật khẩu `Trang@2026x` |
| FX-02 | Học sinh "An" | mã `HS-3F8A-2L6P`, mật khẩu `AnHoc#2026` |
| FX-03 | Học sinh "Bình" | mã `HS-5B7C-8R2T`, cùng lớp 11A1 với An |
| FX-04 | Người soát nội dung "Mai" | mã `GV-2N6P-4W8X`, vai soát nội dung do chủ sản phẩm gán (đã chốt PO-66, QA-S2E6-01) |
| FX-10 | Đề phẳng PL-01 | Tam giác ABC có AB = 5, AC = 12, BC = 13; tính góc A. Đáp số 90° |
| FX-11 | Đề phẳng PL-02 | Tam giác ABC cân tại A, góc A = 40°; tính góc B. Đáp số 70° |
| FX-12 | Đề phẳng PL-03 | Hình chữ nhật ABCD có AB = 6, AD = 8; tính AC. Đáp số 10 |
| FX-13 | Đề phẳng PL-04 | Tam giác đều cạnh 4; tính diện tích. Đáp số `4√3` (xấp xỉ 6,93) |
| FX-14 | Đề không gian SP-01 | Hình chóp S.ABCD, đáy ABCD là hình vuông cạnh a, SA ⊥ (ABCD), SA = a√2; tính góc giữa SC và mặt đáy. Đáp số 45° (tan SCA = SA / AC = 1) |
| FX-20 | Bài luyện BL-1 | 5 câu; câu 2 là FX-14, kiểu đáp số "số đo góc", sai số ±0,5°, 3 lượt thử; giờ mở lời giải `release_at` = T0 + 2 giờ |

## 1. Tóm tắt

| Story | Số tiêu chí | Số case | Tự động hóa (máy / Manual) |
|---|---|---|---|
| US-S2E4-01 Chat chỉnh nháp đề, AI hỏi lại và nói rõ trước/sau | 11 | 11 | 11 / 0 |
| US-S2E4-02 Import docx/pdf, tách từng bài tập | 9 | 9 | 9 / 0 |
| US-S2E4-03 Đối chiếu trùng với kho | 7 | 7 | 7 / 0 |
| **Tổng** | **27** | **27** | **27 / 0** |

| Theo ưu tiên | Số case |
|---|---|
| High | 16 |
| Medium | 9 |
| Low | 2 |

## 2. Test case

### US-S2E4-01 — Chat chỉnh nháp đề, AI hỏi lại và nói rõ trước/sau

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | Status | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S2E4-001 | Định hướng rõ cho một đề xuất có trước và sau | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Nháp đề FX-10 (BC = 13) đang mở; AI giả lập | — | 1. Gõ "đổi cạnh BC thành 14" | • AI trả một đề xuất ghi: trước "BC = 13", sau "BC = 14"<br>• ghi nguồn "nháp hiện tại"<br>• chưa đổi nháp cho tới khi giáo viên nhận | US-S2E4-01-SC1; PO-46 |
| TC-S2E4-002 | Định hướng mơ hồ thì AI hỏi lại, không tự đoán | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Nháp FX-10 đang mở | — | 1. Gõ "làm đề khó hơn" | • AI hỏi lại một câu (ví dụ "Khó hơn theo hướng nào: thêm ý hỏi, đổi số, hay thêm bước?"); không có đề xuất đổi nháp nào | US-S2E4-01-SC2; PO-46; BR-S2E4-01 |
| TC-S2E4-003 | Nhận đề xuất thì nháp đổi và nhãn máy chuyển Cần xem lại | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Đề xuất đổi BC từ 13 thành 14; nháp đang "Đã kiểm chứng" | — | 1. Bấm "Nhận" | • Nháp có BC = 14; nhãn máy "Cần xem lại" cho tới khi máy kiểm lại đạt; trục người vẫn "AI soạn" | US-S2E4-01-SC3; PO-48; BR-S2E4-05 |
| TC-S2E4-004 | Bác đề xuất thì nháp giữ nguyên | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Một đề xuất đang chờ | — | 1. Bấm "Bác" | • Nháp không đổi; đề xuất ghi "đã bác" trong lịch sử chat | US-S2E4-01-SC4; PO-46 |
| TC-S2E4-005 | Sửa tay đề xuất rồi mới nhận | Medium | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Đề xuất đổi BC thành 14 | — | 1. Bấm "Sửa tay"; đổi 14 thành 15; bấm "Nhận" | • Nháp có BC = 15; lịch sử ghi "đã sửa tay từ 14 thành 15" | US-S2E4-01-SC5; PO-46 |
| TC-S2E4-006 | Hoàn tác trả nháp về bước trước | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Đã nhận 2 đề xuất liên tiếp | — | 1. Bấm "Hoàn tác" một lần | • Nháp về đúng trạng thái sau đề xuất thứ nhất | US-S2E4-01-SC6; PO-46; BR-S2E4-03 |
| TC-S2E4-007 | Hoàn tác 10 bước được, bước thứ 11 không | Medium | Boundary (Edge case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Đã nhận 11 đề xuất liên tiếp | — | 1. Bấm "Hoàn tác" 11 lần | • 10 lần đầu hoàn tác được; lần 11 nút khóa kèm "Hết bước hoàn tác" | US-S2E4-01-BV1; BR-S2E4-03; QA-S2E4-02 |
| TC-S2E4-008 | AI không tự lưu: nháp không vào giáo trình khi chưa duyệt | High | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Đã nhận một đề xuất, chưa bấm Duyệt | — | 1. Mở "Của tôi" ở tab khác | • Nháp không có trong "Của tôi" như đề đã duyệt; chỉ hiện ở mục "Nháp" | US-S2E4-01-FC1; PO-30; BR-S2E4-04 |
| TC-S2E4-009 | Tin dài 1000 ký tự nhận, 1001 bị chặn, rỗng bị chặn | Medium | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Nháp đang mở | • Tin 0, 1000, 1001 ký tự | 1. Gửi tin 1000 ký tự; tin 1001 ký tự; tin rỗng | • Tin 1000 ký tự gửi được<br>• tin 1001 ký tự: lỗi "Tin tối đa 1000 ký tự"<br>• tin rỗng: không gửi | US-S2E4-01-BV2; BR-S2E4-06 |
| TC-S2E4-010 | Lượt chat thứ 30 nhận, lượt 31 bị chặn | Medium | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Nháp đã có 29 lượt chat | — | 1. Gửi lượt thứ 30; gửi lượt thứ 31 | • Lượt 30 gửi được<br>• lượt 31: lỗi "Nháp này đã đủ 30 lượt chat, hãy duyệt hoặc tạo nháp mới"; không gọi AI | US-S2E4-01-BV3; BR-S2E4-06; QA-S2E4-02 |
| TC-S2E4-011 | Đề xuất đổi đáp số không kiểm được thì giữ nhãn Cần xem lại | High | Failure (Error case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Giáo viên Cô Trang (FX-01) | • Đề xuất sửa đáp số của FX-10 thành 80° | — | 1. Nhận đề xuất; chạy kiểm | • Máy tính lại 90°; nhãn "Cần xem lại"; hộp duyệt báo lệch như US-S2E3-04 | US-S2E4-01-FC2; PO-48 |

### US-S2E4-02 — Import docx/pdf, tách từng bài tập

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | Status | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S2E4-012 | Tách đúng từng bài tập của tệp docx | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Tệp docx 3 bài tập và 1 đoạn lý thuyết | — | 1. Tải tệp | • Hiện 3 mục bài tập có ô chọn; dòng "Phần lý thuyết chưa hỗ trợ ở chặng này"; chưa có đề nào vào Kho | US-S2E4-02-SC1; PO-47 |
| TC-S2E4-013 | Chỉ bài được tick mới thành nháp | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Ba mục đã tách | — | 1. Tick bài 1 và 3; bấm "Tạo nháp" | • Có 2 nháp (bài 1, bài 3), mỗi nháp nhãn "AI soạn" và "Cần xem lại"; bài 2 không tạo | US-S2E4-02-SC2; BR-S2E4-08 |
| TC-S2E4-014 | Tệp pdf có lớp chữ được tách như docx | Medium | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Tệp pdf có lớp chữ chứa 2 bài | — | 1. Tải tệp | • Hiện 2 mục bài tập | US-S2E4-02-SC3; PO-47 |
| TC-S2E4-015 | Pdf ảnh quét bị từ chối | High | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Tệp pdf chỉ chứa ảnh quét | — | 1. Tải tệp | • Hiện "Tệp không có lớp chữ. Hãy dùng Tải ảnh cho từng trang"; không tạo mục | US-S2E4-02-FC1; BR-S2E4-10 |
| TC-S2E4-016 | Tệp hỏng hoặc đặt mật khẩu bị từ chối | Medium | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Hai tệp: docx hỏng, pdf đặt mật khẩu | — | 1. Tải từng tệp | • Tệp hỏng: "Không mở được tệp"; tệp mật khẩu: "Tệp đặt mật khẩu, hãy bỏ mật khẩu rồi tải lại"; không mục nào được tạo | US-S2E4-02-FC2; BR-S2E4-10 |
| TC-S2E4-017 | Tệp đúng 10 MB nhận, 10 MB cộng 1 byte bị chặn | Medium | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Hai tệp docx 10 485 760 và 10 485 761 byte | • Tệp 10 485 760 và 10 485 761 byte | 1. Tải từng tệp | • Tệp 1 nhận<br>• tệp 2: PAYLOAD_TOO_LARGE, hiện "Tệp tối đa 10 MB" | US-S2E4-02-BV1; BR-S2E4-07; QA-S2E4-03 |
| TC-S2E4-018 | Tệp 50 bài nhận, 51 bài báo vượt | Medium | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Hai tệp: 50 bài và 51 bài | — | 1. Tải từng tệp | • Tệp 50 bài: hiện đủ 50 mục<br>• tệp 51 bài: hiện 50 mục đầu kèm "Tệp có 51 bài, chỉ lấy 50 bài đầu" | US-S2E4-02-BV2; BR-S2E4-07; QA-S2E4-03 |
| TC-S2E4-019 | Lệnh macro trong docx không chạy | High | Failure (Error case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Giáo viên Cô Trang (FX-01) | • Tệp docx có macro tự chạy | — | 1. Tải tệp | • Macro không chạy; tệp được đọc như văn bản; có dòng cảnh báo "Tệp có macro, đã bỏ qua" | US-S2E4-02-FC3; BR-S2E4-10 |
| TC-S2E4-020 | Định dạng xlsx chưa nhận | Low | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Tệp xlsx | — | 1. Tải tệp | • Hiện "Chỉ nhận docx và pdf"; không mục nào được tạo | US-S2E4-02-FC4; BR-S2E4-07; PO-27 |

### US-S2E4-03 — Đối chiếu trùng với kho

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | Status | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S2E4-021 | Bài trùng đề trong Kho chung hiện đã có trong kho | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Kho chung có đề FX-10; tệp import chứa đúng đề FX-10 | • FX-10 | 1. Tải tệp | • Mục FX-10 ghi "đã có trong kho", không tick sẵn; liên kết mở đề trong kho | US-S2E4-03-SC1; PO-47 |
| TC-S2E4-022 | Khác khoảng trắng và hoa thường vẫn là trùng | Medium | Normal (Success case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Giáo viên Cô Trang (FX-01) | • Kho có "Tam giác ABC có AB = 5." | — | 1. Import đề "tam giác abc có  ab = 5" | • Nhận ra trùng sau chuẩn hóa; ghi "đã có trong kho" | US-S2E4-03-SC2; BR-S2E4-11 |
| TC-S2E4-023 | Độ giống 0,89 không báo, 0,90 báo gần giống, 1,00 là trùng | High | Boundary (Edge case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Giáo viên Cô Trang (FX-01) | • Ba cặp đề giả lập độ giống 0,89 / 0,90 / 1,00 | • Độ giống 0,89 / 0,90 / 1,00 | 1. Đối chiếu từng cặp | • 0,89: không ghi chú<br>• 0,90: "gần giống bài PLS-nn, xem?" (không chặn)<br>• 1,00: "đã có trong kho" | US-S2E4-03-BV1; BR-S2E4-11; QA-S2E4-05 |
| TC-S2E4-024 | Mục đã sửa ghi cập nhật khi lưu | High | Normal (Success case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Giáo viên Cô Trang (FX-01) | • Đề trong "Của tôi" của Cô Trang đã chỉnh số liệu so với bản import | — | 1. Import lại tệp | • Mục ghi "cập nhật khi lưu"; lưu xong chỉ có một bản của đề (không trùng) | US-S2E4-03-SC3; PO-47; BR-S2E4-13 |
| TC-S2E4-025 | Không lộ đề Của tôi của người khác qua đối chiếu | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Cô Lan có đề trùng trong "Của tôi"; Cô Trang import cùng đề | — | 1. Cô Trang import | • Không ghi "đã có trong kho"; không nhắc tới đề của Cô Lan; đối chiếu chỉ so với Kho chung và Của tôi của Cô Trang | US-S2E4-03-FC1; BR-S2E4-12; BR-S2-06 |
| TC-S2E4-026 | Import lần hai cùng tệp không tạo bản trùng | High | Failure (Error case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Giáo viên Cô Trang (FX-01) | • Đã lưu 3 đề từ tệp T | — | 1. Import lại đúng tệp T và bấm lưu tất cả | • Cả 3 mục ghi "đã có trong kho" và mặc định không tick; số đề trong "Của tôi" giữ 3 | US-S2E4-03-FC2; PO-47 |
| TC-S2E4-027 | Kho rỗng thì không mục nào bị đánh trùng | Low | Failure (Error case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Giáo viên Cô Trang (FX-01) | • Kho chung và Của tôi rỗng | — | 1. Import tệp 3 bài | • Không có mục "đã có trong kho"; cả 3 tick sẵn | US-S2E4-03-FC3; BR-S2E4-11 |

## 3. Truy vết tiêu chí sang case

| Tiêu chí | Case |
|---|---|
| US-S2E4-01-SC1 | TC-S2E4-001 |
| US-S2E4-01-SC2 | TC-S2E4-002 |
| US-S2E4-01-SC3 | TC-S2E4-003 |
| US-S2E4-01-SC4 | TC-S2E4-004 |
| US-S2E4-01-SC5 | TC-S2E4-005 |
| US-S2E4-01-SC6 | TC-S2E4-006 |
| US-S2E4-01-BV1 | TC-S2E4-007 |
| US-S2E4-01-FC1 | TC-S2E4-008 |
| US-S2E4-01-BV2 | TC-S2E4-009 |
| US-S2E4-01-BV3 | TC-S2E4-010 |
| US-S2E4-01-FC2 | TC-S2E4-011 |
| US-S2E4-02-SC1 | TC-S2E4-012 |
| US-S2E4-02-SC2 | TC-S2E4-013 |
| US-S2E4-02-SC3 | TC-S2E4-014 |
| US-S2E4-02-FC1 | TC-S2E4-015 |
| US-S2E4-02-FC2 | TC-S2E4-016 |
| US-S2E4-02-BV1 | TC-S2E4-017 |
| US-S2E4-02-BV2 | TC-S2E4-018 |
| US-S2E4-02-FC3 | TC-S2E4-019 |
| US-S2E4-02-FC4 | TC-S2E4-020 |
| US-S2E4-03-SC1 | TC-S2E4-021 |
| US-S2E4-03-SC2 | TC-S2E4-022 |
| US-S2E4-03-BV1 | TC-S2E4-023 |
| US-S2E4-03-SC3 | TC-S2E4-024 |
| US-S2E4-03-FC1 | TC-S2E4-025 |
| US-S2E4-03-FC2 | TC-S2E4-026 |
| US-S2E4-03-FC3 | TC-S2E4-027 |

### Khoảng trống

- Không tiêu chí nào thiếu case. Case chưa chạy được vì code chặng 2 chưa có (PO-65 Q1): trạng thái Not run.
- Câu hỏi và đề xuất đổi story map: `questions.md` cùng thư mục.

## 4. Dữ liệu và môi trường

- Dữ liệu: bảng `FX-nn` đầu tệp (đồng bộ với `stories_ac.md`).
- Môi trường: profile `dev` / `test`, AI giả lập, không bao giờ chạy trên `prod` (PO-51). Thiết bị theo mục quy ước của `stories_ac.md`.

## 5. Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: 27 case cho 27 tiêu chí, có cột Tự động hóa | runner-T-261126-12 (QC) |
| v0.2 | 2026-11-26 | đổi mã mẫu `HS-`/`GV-` có ký tự 0, 1, O, I sang ký tự hợp lệ (BR-S2E1-04), RV_s23 m-01 | runner-T-261126-15 (QC soát phản biện, RV_s23) |
