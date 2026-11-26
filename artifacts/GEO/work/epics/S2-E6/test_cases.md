Phiên bản: v0.2 · 2026-11-26
# S2-E6 — test case (Kho đề: ngăn Của tôi / Kho chung, người soát nội dung, riêng tư và giới hạn tần suất)

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
| Của tôi | ngăn chứa đề do chính giáo viên tạo hoặc sao chép; chỉ chủ sở hữu thấy | STAGES Ch2 mục 7 |
| Kho chung | ngăn chứa đề đã được người soát nội dung duyệt; mọi tài khoản đọc được | STAGES Ch2 mục 7, PO-65 Q9 |
| người soát nội dung | tài khoản có vai soát, do chủ sản phẩm gán; chặng 2 là owner tạm soát | PO-65 Q9 |

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
| US-S2E6-01 Kho đề có ngăn Của tôi / Kho chung và lọc loại hình | 12 | 12 | 12 / 0 |
| US-S2E6-02 Người soát nội dung duyệt đề vào Kho chung | 12 | 12 | 12 / 0 |
| US-S2E6-03 Đề Của tôi chỉ chủ sở hữu thấy; giới hạn tần suất thêm đề | 12 | 12 | 12 / 0 |
| **Tổng** | **36** | **36** | **36 / 0** |

| Theo ưu tiên | Số case |
|---|---|
| High | 26 |
| Medium | 10 |
| Low | 0 |

## 2. Test case

### US-S2E6-01 — Kho đề có ngăn Của tôi / Kho chung và lọc loại hình

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | Status | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S2E6-001 | Ngăn Của tôi mặc định hiện đề của chủ, mới nhất ở trên | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Cô Trang có 3 đề: FX-10 (thêm lúc 09:00), FX-14 (10:00), FX-13 (11:00) | • FX-10, FX-13, FX-14 | 1. Mở Kho đề | • Ngăn "Của tôi" đang chọn; thứ tự FX-13, FX-14, FX-10; mỗi thẻ có tên, loại hình và hai nhãn | US-S2E6-01-SC1; BR-S2E6-01 |
| TC-S2E6-002 | Ngăn Kho chung chỉ có đề đã soát | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Kho chung có 2 đề đã soát và có 1 đề chờ soát | — | 1. Bấm ngăn "Kho chung" | • Hiện đúng 2 đề đã soát; đề chờ soát không có | US-S2E6-01-SC2; BR-S2E6-01 |
| TC-S2E6-003 | Lọc Hình phẳng chỉ giữ đề phẳng | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Của tôi có FX-10, FX-13 (phẳng) và FX-14 (không gian) | — | 1. Chọn lọc "Phẳng" | • Chỉ FX-13 và FX-10; FX-14 ẩn; không gọi lại toàn trang (danh sách đổi nhanh) | US-S2E6-01-SC3; BR-S2E6-02 |
| TC-S2E6-004 | Đổi ngăn đưa bộ lọc về Tất cả | Medium | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Đang lọc "Phẳng" ở "Của tôi" | — | 1. Bấm sang "Kho chung" | • Bộ lọc hiện "Tất cả"; danh sách Kho chung đủ loại | US-S2E6-01-SC4; BR-S2E6-02; QA-S2E6-02 |
| TC-S2E6-005 | Chưa có đề riêng hiện trạng thái trống | High | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Giáo viên mới không có đề | — | 1. Mở "Của tôi" | • Hiện "Chưa có đề riêng" kèm nút "Thêm đề"; không hiện bảng rỗng trơ | US-S2E6-01-FC1; BR-S2E6-04 |
| TC-S2E6-006 | Lọc không có kết quả | High | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Của tôi chỉ có đề phẳng | — | 1. Chọn lọc "Không gian" | • Hiện "Không có đề phù hợp" kèm nút "Xóa bộ lọc"; bấm nút trở về danh sách đủ | US-S2E6-01-FC2; BR-S2E6-04 |
| TC-S2E6-007 | Trạng thái đang tải | Medium | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Mạng chậm | — | 1. Mở Kho đề | • Hiện khung giữ chỗ (không hiện "trống" khi đang tải); sau khi có dữ liệu thay bằng danh sách | US-S2E6-01-FC3; BR-S2E6-04 |
| TC-S2E6-008 | Đúng 20 đề không có trang hai | High | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Của tôi có đúng 20 đề | — | 1. Mở Kho đề; cuộn hết | • Hiện 20 đề; không có nút "Xem thêm"; không có con trỏ trang kế | US-S2E6-01-BV1; BR-S2E6-03 |
| TC-S2E6-009 | 21 đề có trang hai với 1 đề | High | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Của tôi có 21 đề | — | 1. Mở Kho đề; bấm "Xem thêm" | • Trang 1 có 20 đề, có nút "Xem thêm"; trang 2 có đúng 1 đề; không trùng đề giữa hai trang | US-S2E6-01-BV2; BR-S2E6-03 |
| TC-S2E6-010 | Yêu cầu 50 đề nhận, 51 bị từ chối | Medium | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Của tôi có 60 đề | • limit 50 và 51 | 1. Gọi danh sách với `limit` 50, rồi `limit` 51 | • Lần 1: trả 50 đề<br>• lần 2: VALIDATION_FAILED | US-S2E6-01-BV3; BR-S2E6-03 |
| TC-S2E6-011 | Con trỏ của ngăn khác không dùng được | Medium | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Con trỏ lấy từ ngăn "Của tôi" | — | 1. Dùng con trỏ đó để xin trang của ngăn "Kho chung" | • INVALID_CURSOR; hiện "Danh sách đã thay đổi. Vui lòng tải lại từ đầu." | US-S2E6-01-FC4; MVP, MSG-GEO-05 |
| TC-S2E6-012 | Học sinh không vào được Kho đề | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Học sinh An (FX-02) | — | 1. Gọi danh sách "Của tôi" và "Kho chung" của Kho đề giáo viên | • FORBIDDEN; không dữ liệu đề nào trả về | US-S2E6-01-FC5; PO-61; STAGES mục 0.2 nguyên tắc 6 |

### US-S2E6-02 — Người soát nội dung duyệt đề vào Kho chung

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | Status | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S2E6-013 | Giáo viên gửi đề đã duyệt vào hàng đợi | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Người soát nội dung "Mai" (FX-04); giáo viên Cô Trang (FX-01) gửi đề | • Đề FX-14 của Cô Trang có trục người "Giáo viên đã duyệt" | • FX-14 | 1. Bấm "Đề xuất vào Kho chung" | • Đề chuyển "Chờ soát"; xuất hiện trong hàng đợi của người soát; vẫn ở "Của tôi" và chưa có trong Kho chung | US-S2E6-02-SC1; BR-S2E6-05 |
| TC-S2E6-014 | Người soát duyệt thì đề vào Kho chung | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Người soát nội dung "Mai" (FX-04); giáo viên Cô Trang (FX-01) gửi đề | • Đề FX-14 Chờ soát; Mai đăng nhập | — | 1. Mở hàng đợi; bấm "Duyệt vào Kho chung" | • Đề thành "Đã vào Kho chung"; xuất hiện trong ngăn "Kho chung" với nhãn của nó; Cô Trang thấy trạng thái đã vào | US-S2E6-02-SC2; BR-S2E6-07 |
| TC-S2E6-015 | Người soát từ chối có lý do | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Người soát nội dung "Mai" (FX-04); giáo viên Cô Trang (FX-01) gửi đề | • Đề Chờ soát | — | 1. Bấm "Từ chối"; gõ lý do "Đáp số chưa khớp phần b"; xác nhận | • Đề thành "Bị từ chối" kèm lý do; Cô Trang thấy lý do; đề không vào Kho chung | US-S2E6-02-SC3; BR-S2E6-08 |
| TC-S2E6-016 | Đề chưa duyệt không gửi được | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Người soát nội dung "Mai" (FX-04); giáo viên Cô Trang (FX-01) gửi đề | • Đề có trục người "AI soạn" | — | 1. Bấm "Đề xuất vào Kho chung"; gọi API gửi | • Nút khóa kèm "Hãy duyệt lời giải trước"; API: CONFLICT; không vào hàng đợi | US-S2E6-02-FC1; BR-S2E6-05 |
| TC-S2E6-017 | Gửi hai lần chỉ giữ một yêu cầu | Medium | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Người soát nội dung "Mai" (FX-04); giáo viên Cô Trang (FX-01) gửi đề | • Đề đã gửi, đang Chờ soát | — | 1. Bấm "Đề xuất vào Kho chung" lần hai | • Không tạo yêu cầu thứ hai; hàng đợi vẫn một dòng cho đề này | US-S2E6-02-FC2; BR-S2E6-06 |
| TC-S2E6-018 | Người không phải người soát không thấy hàng đợi | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Người soát nội dung "Mai" (FX-04); giáo viên Cô Trang (FX-01) gửi đề | • Cô Trang (giáo viên thường) | — | 1. Gọi hàng đợi chờ soát; gọi duyệt | • FORBIDDEN; không dữ liệu hàng đợi | US-S2E6-02-FC3; BR-S2E6-07 |
| TC-S2E6-019 | Người soát không soát đề của chính mình | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Người soát nội dung "Mai" (FX-04); giáo viên Cô Trang (FX-01) gửi đề | • Mai là chủ đề FX-13 và đã gửi soát | • FX-13 | 1. Mai thử duyệt đề FX-13 | • FORBIDDEN với thông báo "Bạn không thể soát đề của chính mình"; đề vẫn Chờ soát | US-S2E6-02-FC4; BR-S2E6-07 |
| TC-S2E6-020 | Lý do từ chối 4 ký tự bị chặn, 5 ký tự nhận | Medium | Boundary (Edge case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Người soát nội dung "Mai" (FX-04); giáo viên Cô Trang (FX-01) gửi đề | • Đề Chờ soát | • Lý do 4 và 5 ký tự | 1. Từ chối với lý do 4 ký tự; từ chối với lý do 5 ký tự | • 4 ký tự: lỗi "Lý do từ 5 đến 500 ký tự", đề vẫn Chờ soát<br>• 5 ký tự: từ chối thành công | US-S2E6-02-BV1; BR-S2E6-08; QA-S2E6-03 |
| TC-S2E6-021 | Lý do 500 ký tự nhận, 501 bị chặn | Medium | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Người soát nội dung "Mai" (FX-04); giáo viên Cô Trang (FX-01) gửi đề | • Hai đề Chờ soát | • Lý do 500 và 501 ký tự | 1. Từ chối đề 1 với lý do 500 ký tự; đề 2 với lý do 501 ký tự | • Đề 1: từ chối thành công<br>• đề 2: VALIDATION_FAILED | US-S2E6-02-BV2; BR-S2E6-08; QA-S2E6-03 |
| TC-S2E6-022 | Đề thứ 10 Chờ soát nhận, đề thứ 11 bị chặn | Medium | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Người soát nội dung "Mai" (FX-04); giáo viên Cô Trang (FX-01) gửi đề | • Cô Trang có 9 đề đã duyệt đang Chờ soát | — | 1. Gửi đề thứ 10; gửi đề thứ 11 | • Đề 10: vào hàng đợi<br>• đề 11: lỗi "Tối đa 10 đề chờ soát cùng lúc"; CONFLICT; không vào hàng đợi | US-S2E6-02-BV3; BR-S2E6-09; QA-S2E6-04 |
| TC-S2E6-023 | Sửa bản Của tôi không đổi bản Kho chung | High | Normal (Success case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Người soát nội dung "Mai" (FX-04); giáo viên Cô Trang (FX-01) gửi đề | • Đề FX-14 đã vào Kho chung | — | 1. Cô Trang sửa lời giải bản Của tôi và lưu | • Bản trong Kho chung giữ nguyên nội dung và nhãn; người khác đọc thấy bản cũ | US-S2E6-02-SC4; BR-S2E6-10; QA-S2E6-05 |
| TC-S2E6-024 | Đề bị từ chối gửi lại được sau khi sửa | Medium | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Người soát nội dung "Mai" (FX-04); giáo viên Cô Trang (FX-01) gửi đề | • Đề "Bị từ chối" | — | 1. Sửa đề rồi bấm "Đề xuất vào Kho chung" | • Đề thành "Chờ soát" lại; lịch sử giữ dòng từ chối cũ | US-S2E6-02-FC5; BR-S2E6-06 |

### US-S2E6-03 — Đề Của tôi chỉ chủ sở hữu thấy; giới hạn tần suất thêm đề

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | Status | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S2E6-025 | Chủ sở hữu thấy đề Của tôi | High | Normal (Success case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01); giáo viên "Cô Lan"; học sinh An (FX-02) | • Đề FX-10 trong Của tôi của Cô Trang | • FX-10 | 1. Cô Trang gọi chi tiết đề | • Trả đủ nội dung đề | US-S2E6-03-SC1; BR-S2E6-11 |
| TC-S2E6-026 | Giáo viên khác không lấy được chi tiết đề Của tôi | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01); giáo viên "Cô Lan"; học sinh An (FX-02) | • Đề FX-10 của Cô Trang; Cô Lan đăng nhập | • FX-10 | 1. Cô Lan gọi chi tiết bằng mã đề của Cô Trang | • NOT_FOUND; không phân biệt "không có" và "của người khác"; không dữ liệu đề | US-S2E6-03-FC1; BR-S2E6-11 |
| TC-S2E6-027 | Đề Của tôi không lộ qua danh sách, tìm và đối chiếu | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01); giáo viên "Cô Lan"; học sinh An (FX-02) | • Cô Trang có đề riêng FX-10; Cô Lan đăng nhập | — | 1. Cô Lan xem danh sách Kho đề, tìm "5, 12, 13", import đề giống FX-10 | • Không đường nào hiện đề của Cô Trang | US-S2E6-03-FC2; BR-S2E6-11 |
| TC-S2E6-028 | Học sinh không thấy ngăn Của tôi của giáo viên | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01); giáo viên "Cô Lan"; học sinh An (FX-02) | • An thuộc lớp 11A1 của Cô Trang | — | 1. An gọi danh sách đề Của tôi của Cô Trang | • FORBIDDEN; An chỉ thấy đề qua bài giao của lớp | US-S2E6-03-FC3; BR-S2E6-12 |
| TC-S2E6-029 | Người soát chỉ thấy đề đang Chờ soát | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01); giáo viên "Cô Lan"; học sinh An (FX-02) | • Mai là người soát; Cô Trang có 1 đề Chờ soát và 2 đề Của tôi bình thường | — | 1. Mai gọi chi tiết cho từng đề | • Đề Chờ soát: thấy; hai đề còn lại: NOT_FOUND | US-S2E6-03-FC4; BR-S2E6-11 |
| TC-S2E6-030 | Đề thứ 20 trong giờ nhận, đề thứ 21 bị chặn | High | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01); giáo viên "Cô Lan"; học sinh An (FX-02) | • Cô Trang chưa thêm đề nào trong 60 phút qua | — | 1. Thêm lần lượt 21 đề (nhập chữ hợp lệ) | • 20 đề đầu tạo được<br>• đề 21: RATE_LIMITED kèm `Retry-After`; không đề nào được tạo thêm | US-S2E6-03-BV1; BR-S2E6-13; PO-63 |
| TC-S2E6-031 | Cửa sổ trượt: đề đầu hết hạn sau 60 phút thì thêm được | High | Boundary (Edge case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Giáo viên Cô Trang (FX-01); giáo viên "Cô Lan"; học sinh An (FX-02) | • Cô Trang đã thêm đủ 20 đề, đề đầu lúc T0; đồng hồ giả | — | 1. Thêm đề lúc T0 + 59:59; thêm đề lúc T0 + 60:00 | • T0 + 59:59: RATE_LIMITED<br>• T0 + 60:00: tạo được | US-S2E6-03-BV2; BR-S2E6-13 |
| TC-S2E6-032 | Bộ đếm tính chung mọi đường tạo đề | High | Failure (Error case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Giáo viên Cô Trang (FX-01); giáo viên "Cô Lan"; học sinh An (FX-02) | • Cô Trang đã thêm 10 đề bằng dán chữ, 5 bằng ảnh, 5 bằng import | — | 1. Thêm đề thứ 21 bằng sao chép từ Kho chung | • RATE_LIMITED; không đường nào lách được bộ đếm | US-S2E6-03-FC5; BR-S2E6-13 |
| TC-S2E6-033 | Bộ đếm theo tài khoản, không theo địa chỉ mạng | Medium | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01); giáo viên "Cô Lan"; học sinh An (FX-02) | • Cô Trang đủ 20 đề; Cô Lan cùng địa chỉ mạng | — | 1. Cô Lan thêm một đề | • Cô Lan thêm được; bộ đếm của Cô Trang không bị ảnh hưởng | US-S2E6-03-FC6; BR-S2E6-13 |
| TC-S2E6-034 | Giáo viên xem được lời giải đề Kho chung | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01); giáo viên "Cô Lan"; học sinh An (FX-02) | • Đề Kho chung có lời giải đã duyệt; Cô Lan vai Giáo viên | — | 1. Cô Lan mở lời giải | • Xem được kèm nhãn hai trục; không cần xác minh giáo viên | US-S2E6-03-SC2; BR-S2E6-14; PO-63 |
| TC-S2E6-035 | Học sinh không xem lời giải đề Kho chung ngoài bài giao | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01); giáo viên "Cô Lan"; học sinh An (FX-02) | • Học sinh An; đề Kho chung không nằm trong bài giao nào của An | — | 1. An gọi lời giải đề đó | • Khóa: không bước giải, không đáp số (kiểm dữ liệu trả về); thông báo "Lời giải mở theo bài giao của thầy cô" | US-S2E6-03-FC7; BR-S2E6-14; QA-S2E6-06 |
| TC-S2E6-036 | Bộ câu thử "giả làm giáo viên" không lấy được gì ngoài quyền giáo viên | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01); giáo viên "Cô Lan"; học sinh An (FX-02) | • Tài khoản vừa đăng ký vai Giáo viên (không lớp, không xác minh) | — | 1. Thử xem kết quả học sinh của lớp khác, đề Của tôi của người khác, đặt giờ mở bài giao của lớp khác | • Mọi thử đều FORBIDDEN hoặc NOT_FOUND; chỉ có quyền đọc Kho chung và tạo đề của chính mình | US-S2E6-03-FC8; PO-63 |

## 3. Truy vết tiêu chí sang case

| Tiêu chí | Case |
|---|---|
| US-S2E6-01-SC1 | TC-S2E6-001 |
| US-S2E6-01-SC2 | TC-S2E6-002 |
| US-S2E6-01-SC3 | TC-S2E6-003 |
| US-S2E6-01-SC4 | TC-S2E6-004 |
| US-S2E6-01-FC1 | TC-S2E6-005 |
| US-S2E6-01-FC2 | TC-S2E6-006 |
| US-S2E6-01-FC3 | TC-S2E6-007 |
| US-S2E6-01-BV1 | TC-S2E6-008 |
| US-S2E6-01-BV2 | TC-S2E6-009 |
| US-S2E6-01-BV3 | TC-S2E6-010 |
| US-S2E6-01-FC4 | TC-S2E6-011 |
| US-S2E6-01-FC5 | TC-S2E6-012 |
| US-S2E6-02-SC1 | TC-S2E6-013 |
| US-S2E6-02-SC2 | TC-S2E6-014 |
| US-S2E6-02-SC3 | TC-S2E6-015 |
| US-S2E6-02-FC1 | TC-S2E6-016 |
| US-S2E6-02-FC2 | TC-S2E6-017 |
| US-S2E6-02-FC3 | TC-S2E6-018 |
| US-S2E6-02-FC4 | TC-S2E6-019 |
| US-S2E6-02-BV1 | TC-S2E6-020 |
| US-S2E6-02-BV2 | TC-S2E6-021 |
| US-S2E6-02-BV3 | TC-S2E6-022 |
| US-S2E6-02-SC4 | TC-S2E6-023 |
| US-S2E6-02-FC5 | TC-S2E6-024 |
| US-S2E6-03-SC1 | TC-S2E6-025 |
| US-S2E6-03-FC1 | TC-S2E6-026 |
| US-S2E6-03-FC2 | TC-S2E6-027 |
| US-S2E6-03-FC3 | TC-S2E6-028 |
| US-S2E6-03-FC4 | TC-S2E6-029 |
| US-S2E6-03-BV1 | TC-S2E6-030 |
| US-S2E6-03-BV2 | TC-S2E6-031 |
| US-S2E6-03-FC5 | TC-S2E6-032 |
| US-S2E6-03-FC6 | TC-S2E6-033 |
| US-S2E6-03-SC2 | TC-S2E6-034 |
| US-S2E6-03-FC7 | TC-S2E6-035 |
| US-S2E6-03-FC8 | TC-S2E6-036 |

### Khoảng trống

- Không tiêu chí nào thiếu case. Case chưa chạy được vì code chặng 2 chưa có (PO-65 Q1): trạng thái Not run.
- Câu hỏi và đề xuất đổi story map: `questions.md` cùng thư mục.

## 4. Dữ liệu và môi trường

- Dữ liệu: bảng `FX-nn` đầu tệp (đồng bộ với `stories_ac.md`).
- Môi trường: profile `dev` / `test`, AI giả lập, không bao giờ chạy trên `prod` (PO-51). Thiết bị theo mục quy ước của `stories_ac.md`.

## 5. Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: 36 case cho 36 tiêu chí, có cột Tự động hóa | runner-T-261126-12 (QC) |
| v0.2 | 2026-11-26 | đổi mã mẫu `HS-`/`GV-` có ký tự 0, 1, O, I sang ký tự hợp lệ (BR-S2E1-04), RV_s23 m-01 | runner-T-261126-15 (QC soát phản biện, RV_s23) |
