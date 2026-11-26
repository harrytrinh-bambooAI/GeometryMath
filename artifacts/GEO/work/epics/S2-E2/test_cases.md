Phiên bản: v0.2 · 2026-11-26
# S2-E2 — test case (Khung môn học và hình học phẳng (dựng từ ràng buộc, bộ đề chuẩn, đo bench))

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
| `QA-S2En-nn` | câu hỏi mở của epic, luôn kèm mặc định (đã chốt PO-66) | `questions.md` cùng thư mục |
| `TC-S2En-nnn` | test case của epic | `test_cases.md` cùng thư mục |
| `FX-nn` | dữ liệu mẫu chặng 2 (bảng dưới) | tệp này |
| `BG-n` | bài giao mẫu của MVP (BG-1…BG-5) | `artifacts/GEO/work/fixture.md` (MVP) |
| `BR-GEO-nnn` | quy tắc nghiệp vụ của MVP còn hiệu lực | `artifacts/GEO/work/02_gate2_brd.md` (MVP) |
| `MSG-GEO-nn` | thông điệp hiện cho người dùng của MVP (câu chữ nguyên văn) | `artifacts/GEO/work/05_dictionary.md` (MVP) |
| Đã chốt PO-66 | mặc định đã được chủ đầu tư chốt ở PO-66 (26/11/2026); ghi theo mẫu `quyết định · tin cậy · nguồn`; ngưỡng chỉnh sau L6 (G12) | PO-65, PO-66 |
| Nhãn hai trục | trục máy: "Đã kiểm chứng" / "Cần xem lại" (chỉ engine gắn) · trục người: "AI soạn" / "Giáo viên đã duyệt" | PO-02 |
| `release_at` | giờ mở lời giải; máy chủ so giờ máy chủ, không tin giờ thiết bị | MVP, M-GEO-13 |
| `PLS-nn` | mã đề trong bộ đề chuẩn hình phẳng của chặng 2 | US-S2E2-04 |
| `SceneSpec` | mô tả hình (điểm, ràng buộc, tọa độ) gửi xuống thiết bị để vẽ; chặng 2 thêm bản 2D | STAGES Ch2 mục 8 |
| `Problem.kind` | loại đề: `space` (không gian) hoặc `plane` (phẳng) | STAGES Ch2 mục 8 |

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
| US-S2E2-01 Khung môn học: `parse/solve/verify/render/hint` cộng `grade`, `concepts` | 7 | 7 | 7 / 0 |
| US-S2E2-02 Dựng hình phẳng từ ràng buộc; `Problem.kind`; `SceneSpec` 2D | 14 | 14 | 14 / 0 |
| US-S2E2-03 Làm bài hình phẳng trên iPad và iPhone | 9 | 9 | 7 / 2 |
| US-S2E2-04 Dữ liệu: bộ đề chuẩn hình phẳng và đáp số mẫu | 7 | 7 | 7 / 0 |
| US-S2E2-05 Đo `make bench-s2` (mock): ≥ 95 % thỏa ràng buộc, ≥ 90 % đáp số, 0 sai mang nhãn | 8 | 8 | 8 / 0 |
| **Tổng** | **45** | **45** | **43 / 2** |

| Theo ưu tiên | Số case |
|---|---|
| High | 27 |
| Medium | 17 |
| Low | 1 |

## 2. Test case

### US-S2E2-01 — Khung môn học: `parse/solve/verify/render/hint` cộng `grade`, `concepts`

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | Status | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S2E2-001 | Môn Hình học đăng ký đủ sáu thao tác và danh sách khái niệm | High | Normal (Success case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Quản trị kỹ thuật (kiểm bằng lệnh và API nội bộ) | • Khung môn học đã nạp môn Hình học | — | 1. Hỏi danh mục môn của khung | • Môn "Hình học" có đủ `parse`, `solve`, `verify`, `render`, `hint`, `grade`<br>• `concepts` có ít nhất 1 mã khái niệm | US-S2E2-01-SC1; BR-S2-02 |
| TC-S2E2-002 | Môn thiếu một thao tác bị từ chối lúc khởi động | High | Failure (Error case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Quản trị kỹ thuật (kiểm bằng lệnh và API nội bộ) | • Môn thử có `parse`, `solve`, `verify`, `render`, `grade`, `concepts` nhưng thiếu `hint` | — | 1. Khởi động khung với môn thử | • Khung từ chối đăng ký môn; thông báo ghi tên thao tác thiếu là `hint`; không môn nào bị nạp dở | US-S2E2-01-FC1; BR-S2E2-02 |
| TC-S2E2-003 | Môn có `concepts` rỗng bị từ chối | Medium | Boundary (Edge case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Quản trị kỹ thuật (kiểm bằng lệnh và API nội bộ) | • Môn thử đủ sáu thao tác nhưng `concepts` là danh sách rỗng | — | 1. Khởi động khung với môn thử | • Khung từ chối; thông báo ghi `concepts` rỗng | US-S2E2-01-FC2; BR-S2E2-01 |
| TC-S2E2-004 | Gọi môn không tồn tại | Medium | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Quản trị kỹ thuật (kiểm bằng lệnh và API nội bộ) | • Khung chỉ có môn "Hình học" | — | 1. Gọi `solve` cho môn "Vật lý" | • NOT_FOUND; không lỗi nội bộ | US-S2E2-01-FC3; D-GEO-143 |
| TC-S2E2-005 | Môn không gian của MVP không đổi kết quả qua khung mới | High | Normal (Success case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Quản trị kỹ thuật (kiểm bằng lệnh và API nội bộ) | • Bộ đề chuẩn MVP (đề không gian) và đáp số tham chiếu của MVP | — | 1. Chạy bộ đề chuẩn MVP qua khung mới | • Mỗi đề cho cùng đáp số và cùng nhãn máy như trước khi đổi khung (so từng đề); không đề nào đổi | US-S2E2-01-SC2; BR-S2E2-03 |
| TC-S2E2-006 | Thao tác `grade` có chữ ký chung cho mọi kiểu đáp số | Medium | Normal (Success case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Quản trị kỹ thuật (kiểm bằng lệnh và API nội bộ) | • Môn Hình học nạp xong | — | 1. Gọi `grade` với đáp số mẫu 45, đáp số học sinh 45, sai số 0,5 | • Kết quả "đúng" kèm dạng chuẩn "45"; chữ ký `grade` giống ở mọi môn (cùng ba tham số: đáp số mẫu, đáp số nộp, dung sai) | US-S2E2-01-SC3; BR-S2E2-01; BR-S2-08 |
| TC-S2E2-007 | Môn lỗi giữa chừng không làm sập khung | Medium | Failure (Error case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Quản trị kỹ thuật (kiểm bằng lệnh và API nội bộ) | • Môn thử có `solve` ném lỗi | — | 1. Gọi `solve` | • Khung trả TIMEOUT hoặc lỗi dịch vụ theo D-GEO-143 và vẫn phục vụ môn khác | US-S2E2-01-FC4; BR-S2E2-02 |

### US-S2E2-02 — Dựng hình phẳng từ ràng buộc; `Problem.kind`; `SceneSpec` 2D

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | Status | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S2E2-008 | Dựng tam giác 5, 12, 13 thỏa ràng buộc | High | Normal (Success case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Quản trị kỹ thuật (kiểm bằng API nội bộ); giáo viên Cô Trang (FX-01) cho case có UI | • Đề PL-01 (FX-10) | • FX-10 | 1. Gọi dựng hình | • Ba cạnh dài 5, 12, 13 (sai số tương đối ≤ 1e-6)<br>• góc A = 90°<br>• `Problem.kind` = `plane` | US-S2E2-02-SC1; STAGES Ch2 mục 3 ý 1 |
| TC-S2E2-009 | Dựng tam giác cân tại A góc A 40° | High | Normal (Success case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Quản trị kỹ thuật (kiểm bằng API nội bộ); giáo viên Cô Trang (FX-01) cho case có UI | • Đề PL-02 (FX-11) | • FX-11 | 1. Gọi dựng hình | • AB = AC; góc A = 40°; góc B = 70° (sai số ≤ 1e-6) | US-S2E2-02-SC2; STAGES Ch2 mục 3 ý 1 |
| TC-S2E2-010 | Dựng hình chữ nhật 6 × 8 | High | Normal (Success case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Quản trị kỹ thuật (kiểm bằng API nội bộ); giáo viên Cô Trang (FX-01) cho case có UI | • Đề PL-03 (FX-12) | • FX-12 | 1. Gọi dựng hình | • AB = 6, AD = 8, bốn góc vuông, đường chéo AC = 10 | US-S2E2-02-SC3; STAGES Ch2 mục 3 ý 1 |
| TC-S2E2-011 | Dựng tam giác đều cạnh 4 | Medium | Normal (Success case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Quản trị kỹ thuật (kiểm bằng API nội bộ); giáo viên Cô Trang (FX-01) cho case có UI | • Đề PL-04 (FX-13) | • FX-13 | 1. Gọi dựng hình | • Ba cạnh bằng 4; ba góc 60°; diện tích 4√3 xấp xỉ 6,9282 | US-S2E2-02-SC4; STAGES Ch2 mục 3 ý 1 |
| TC-S2E2-012 | Đề không gian vẫn mang loại `space` | High | Normal (Success case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Quản trị kỹ thuật (kiểm bằng API nội bộ); giáo viên Cô Trang (FX-01) cho case có UI | • Đề SP-01 (FX-14) | • FX-14 | 1. Gọi `parse` | • `Problem.kind` = `space`; `SceneSpec` 3D như MVP, không đổi | US-S2E2-02-SC5; BR-S2E2-04 |
| TC-S2E2-013 | `SceneSpec` 2D không chứa đáp số hay bước giải | High | Normal (Success case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Quản trị kỹ thuật (kiểm bằng API nội bộ); giáo viên Cô Trang (FX-01) cho case có UI | • Đề PL-01 đã dựng | — | 1. Lấy `SceneSpec` gửi xuống thiết bị; tìm trường đáp số, bước giải, gợi ý | • Không có trường nào chứa đáp số hoặc bước giải; chỉ có điểm, nhãn, ràng buộc, tọa độ | US-S2E2-02-SC6; BR-S2E2-07 |
| TC-S2E2-014 | Dựng hai lần cho kết quả giống hệt | Medium | Normal (Success case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Quản trị kỹ thuật (kiểm bằng API nội bộ); giáo viên Cô Trang (FX-01) cho case có UI | • Đề PL-02 | — | 1. Dựng hình hai lần liên tiếp | • Hai `SceneSpec` giống hệt từng ký tự | US-S2E2-02-SC7; BR-S2E2-08 |
| TC-S2E2-015 | Số liệu mâu thuẫn thì không dựng | High | Failure (Error case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Quản trị kỹ thuật (kiểm bằng API nội bộ); giáo viên Cô Trang (FX-01) cho case có UI | • Đề "tam giác ABC có AB = 5, AC = 12, BC = 20" | — | 1. Gọi dựng hình | • Không có `SceneSpec`; kết quả nhãn "Cần xem lại" kèm lý do "ba cạnh không lập thành tam giác" | US-S2E2-02-FC1; BR-S2E2-05 |
| TC-S2E2-016 | Tam giác suy biến (tổng hai cạnh bằng cạnh còn lại) bị từ chối | High | Boundary (Edge case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Quản trị kỹ thuật (kiểm bằng API nội bộ); giáo viên Cô Trang (FX-01) cho case có UI | • Đề "AB = 5, AC = 5, BC = 10" | — | 1. Gọi dựng hình | • Từ chối như FC1 (ba điểm thẳng hàng không phải tam giác) | US-S2E2-02-BV1; BR-S2E2-05 |
| TC-S2E2-017 | Tam giác gần suy biến vẫn dựng được | Medium | Boundary (Edge case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Quản trị kỹ thuật (kiểm bằng API nội bộ); giáo viên Cô Trang (FX-01) cho case có UI | • Đề "AB = 5, AC = 5, BC = 9,99" | — | 1. Gọi dựng hình | • Dựng được; ba cạnh đúng 5, 5, 9,99 trong sai số 1e-6; góc A xấp xỉ 179,8° | US-S2E2-02-BV2; BR-S2E2-06 |
| TC-S2E2-018 | Góc đề cho sát 0° hoặc 180° | Medium | Boundary (Edge case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Quản trị kỹ thuật (kiểm bằng API nội bộ); giáo viên Cô Trang (FX-01) cho case có UI | • Hai đề: tam giác cân có góc A = 0° và góc A = 179° | — | 1. Gọi dựng hình cho từng đề | • Góc A = 0°: từ chối, lý do "góc không hợp lệ"<br>• góc A = 179°: dựng được và thỏa ràng buộc | US-S2E2-02-BV3; BR-S2E2-05 |
| TC-S2E2-019 | Đề thiếu ràng buộc thì nhãn "Cần xem lại" | High | Failure (Error case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Quản trị kỹ thuật (kiểm bằng API nội bộ); giáo viên Cô Trang (FX-01) cho case có UI | • Đề "tam giác ABC có AB = 5; tính góc A" | — | 1. Gọi dựng hình | • Không dựng hình duy nhất; trả "Cần xem lại" kèm lý do "hình chưa xác định, thiếu dữ kiện"; không có đáp số | US-S2E2-02-FC2; STAGES Ch2 mục 10 |
| TC-S2E2-020 | Dạng ngoài danh sách hỗ trợ đi nhánh Cần xem lại | High | Failure (Error case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Quản trị kỹ thuật (kiểm bằng API nội bộ); giáo viên Cô Trang (FX-01) cho case có UI | • Đề về đường elip (dạng ngoài danh sách) | — | 1. Gọi dựng hình | • Không dựng bừa; nhãn máy "Cần xem lại"; lý do "dạng chưa hỗ trợ"; hệ thống ghi một dòng thống kê dạng chưa hỗ trợ | US-S2E2-02-FC3; BR-S2E2-05; QA-S2E2-01 |
| TC-S2E2-021 | Đề có cả điểm ngoài mặt phẳng chạy nhánh không gian | Medium | Failure (Error case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Quản trị kỹ thuật (kiểm bằng API nội bộ); giáo viên Cô Trang (FX-01) cho case có UI | • Đề có điểm S nằm ngoài mặt đáy | — | 1. Gọi `parse` | • `Problem.kind` = `space`; không vào nhánh dựng phẳng | US-S2E2-02-FC4; BR-S2E2-04 |

### US-S2E2-03 — Làm bài hình phẳng trên iPad và iPhone

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | Status | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S2E2-022 | Mở bài phẳng trên iPad hiện hình, nhãn đỉnh và công cụ đo | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) | • An đăng nhập trên iPad; bài PL-01 đã giao | • FX-10 | 1. Mở bài PL-01 | • Hình tam giác ABC có nhãn A, B, C<br>• có công cụ đo độ dài và đo góc<br>• có bảng gợi ý<br>• nhãn hai trục ở đầu thẻ hình | US-S2E2-03-SC1; STAGES Ch2 mục 6 |
| TC-S2E2-023 | Đo cạnh AB cho đúng 5 | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) | • Bài PL-01 mở, cạnh AB không phải đáp số | — | 1. Chọn công cụ đo độ dài; chạm A rồi B | • Hiện "AB = 5" (làm tròn tới hai chữ số thập phân, dấu phẩy) | US-S2E2-03-SC2; BR-S2E2-09 |
| TC-S2E2-024 | Đo đại lượng là đáp số bị khóa | High | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) | • Bài PL-01 hỏi góc A; lời giải còn khóa | — | 1. Chọn công cụ đo góc; chạm B, A, C | • Không hiện số đo; hiện dòng "Đại lượng này là đáp số, chưa đo được"; dữ liệu trả về không chứa giá trị góc A | US-S2E2-03-FC1; BR-S2E2-09; QA-S2E2-03 |
| TC-S2E2-025 | Sau `release_at` công cụ đo mở cho đáp số | Medium | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) | • Bài PL-01, giờ máy chủ vượt `release_at` | — | 1. Đo góc A | • Hiện "A = 90°" | US-S2E2-03-SC3; BR-S2E2-09 |
| TC-S2E2-026 | Lời giải và đáp số khóa trong dữ liệu trả về | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Học sinh An (FX-02) | • Bài PL-02 đang khóa | • FX-11 | 1. Mở bài; xem toàn bộ dữ liệu trả về của mọi yêu cầu | • Không có đáp số 70° hay bước giải ở bất kỳ dạng viết nào; tiêu đề `Cache-Control: no-store` | US-S2E2-03-FC2; STAGES mục 0.2 nguyên tắc 1 |
| TC-S2E2-027 | Hình chưa kiểm chứng hiện nhãn "Cần xem lại" | Medium | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) | • Bài có nhãn máy "Cần xem lại" (hình do AI dựng) | — | 1. Mở bài trên iPad | • Đầu thẻ hình hiện "Cần xem lại" và "AI soạn"<br>• vẫn làm bài được | US-S2E2-03-SC4; BR-S2E2-10; STAGES Ch2 mục 6 |
| TC-S2E2-028 | Bố cục iPhone một cột, không cuộn ngang | Medium | Normal (Success case) | chức năng | thủ công, có lý do | Manual — cần thiết bị thật để cảm nhận cỡ chạm | Not run | Học sinh An (FX-02) | • iPhone 390 × 844, bài PL-03 | — | 1. Mở bài | • Hình ở trên, công cụ và gợi ý ở dưới; không cuộn ngang; mọi nút chạm được (≥ 44 điểm) | US-S2E2-03-SC5; BR-S2E2-11 |
| TC-S2E2-029 | Trạng thái đang tải và mất mạng | Medium | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) | • Mạng chậm rồi ngắt khi mở bài | — | 1. Mở bài; ngắt mạng giữa chừng | • Hiện màn "đang tải" có khung giữ chỗ (không hình rỗng); khi mất mạng hiện thông báo và nút "Thử lại"; không hiện hình nửa vời | US-S2E2-03-FC3; STAGES Ch2 mục 6 |
| TC-S2E2-030 | Xoay iPad giữ trạng thái đo | Low | Boundary (Edge case) | chức năng | thủ công, có lý do | Manual — xoay thiết bị cần máy thật | Not run | Học sinh An (FX-02) | • Bài PL-03 đang mở với một phép đo AB hiển thị | — | 1. Xoay iPad từ dọc sang ngang | • Hình vẽ lại cân đối; phép đo vẫn hiện; không mất gợi ý đã mở | US-S2E2-03-BV1; BR-S2E2-11 |

### US-S2E2-04 — Dữ liệu: bộ đề chuẩn hình phẳng và đáp số mẫu

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | Status | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S2E2-031 | Bộ chuẩn có đủ 30 đề với đủ trường | High | Normal (Success case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Quản trị kỹ thuật (lệnh nạp dữ liệu) | • Tệp bộ đề chuẩn hình phẳng đã soạn | — | 1. Chạy lệnh nạp | • Nạp đủ 30 đề; mỗi đề có mã, đề, ràng buộc, dạng, đáp số, kiểu đáp số, người soát | US-S2E2-04-SC1; BR-S2E2-12, 13 |
| TC-S2E2-032 | Mỗi dạng hỗ trợ có tối thiểu 5 đề | High | Normal (Success case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Quản trị kỹ thuật (lệnh nạp dữ liệu) | • Bộ đã nạp | — | 1. Đếm đề theo dạng (tam giác, tứ giác, đường tròn) | • Mỗi dạng có từ 5 đề trở lên | US-S2E2-04-SC2; BR-S2E2-13 |
| TC-S2E2-033 | Bốn đề mẫu FX-10…FX-13 có trong bộ với đáp số khớp | High | Normal (Success case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Quản trị kỹ thuật (lệnh nạp dữ liệu) | • Bộ đã nạp | — | 1. Tìm đề PL-01…PL-04 | • Đáp số: 90°, 70°, 10, `4√3` đúng như bảng `FX` | US-S2E2-04-SC3; BR-S2E2-12 |
| TC-S2E2-034 | Đề thiếu đáp số tham chiếu bị từ chối | High | Failure (Error case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Quản trị kỹ thuật (lệnh nạp dữ liệu) | • Tệp thử có một đề không có đáp số | — | 1. Chạy lệnh nạp | • Đề đó vào báo cáo lỗi kèm mã đề; các đề khác vẫn nạp; mã thoát khác 0 | US-S2E2-04-FC1; BR-S2E2-12 |
| TC-S2E2-035 | Trùng mã đề bị từ chối | Medium | Failure (Error case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Quản trị kỹ thuật (lệnh nạp dữ liệu) | • Tệp thử có hai đề cùng mã `PLS-07` | — | 1. Chạy lệnh nạp | • Từ chối đề thứ hai; báo trùng mã `PLS-07`; mã thoát khác 0 | US-S2E2-04-FC2; BR-S2E2-12 |
| TC-S2E2-036 | Đáp số tham chiếu lệch engine bị báo | High | Failure (Error case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Quản trị kỹ thuật (lệnh nạp dữ liệu) | • Tệp thử: đề PL-03 ghi đáp số 12 (đúng là 10) | — | 1. Chạy lệnh nạp | • Báo lỗi dữ liệu: "đáp số tham chiếu 12 lệch kết quả engine 10" kèm mã đề; đề không vào bộ | US-S2E2-04-FC3; BR-S2E2-14 |
| TC-S2E2-037 | Bộ 29 đề bị báo thiếu, 30 đề đạt | Medium | Boundary (Edge case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Quản trị kỹ thuật (lệnh nạp dữ liệu) | • Hai bộ thử: 29 đề và 30 đề | — | 1. Chạy lệnh nạp từng bộ | • Bộ 29 đề: cảnh báo "dưới 30 đề", mã thoát khác 0<br>• bộ 30 đề: đạt | US-S2E2-04-BV1; BR-S2E2-13 |

### US-S2E2-05 — Đo `make bench-s2` (mock): ≥ 95 % thỏa ràng buộc, ≥ 90 % đáp số, 0 sai mang nhãn

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | Status | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S2E2-038 | Báo cáo có đủ ba chỉ số và danh sách đề rớt | High | Normal (Success case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Quản trị kỹ thuật (chạy lệnh, đọc báo cáo) | • Bộ 30 đề đã nạp; AI giả lập | — | 1. Chạy `make bench-s2` | • Báo cáo ghi ngày chạy, số đề 30, M-S2-01, M-S2-02, M-S2-03; có mục "đề rớt" (rỗng khi không có) | US-S2E2-05-SC1; BR-S2E2-16 |
| TC-S2E2-039 | Đạt cả ba ngưỡng thì thoát 0 | High | Normal (Success case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Quản trị kỹ thuật (chạy lệnh, đọc báo cáo) | • Kết quả mẫu: 30/30 thỏa ràng buộc, 28/30 đáp số khớp, 0 sai mang nhãn | — | 1. Chạy lệnh | • Mã thoát 0; ba chỉ số đều được đánh dấu đạt | US-S2E2-05-SC2; BR-S2E2-16 |
| TC-S2E2-040 | Thỏa ràng buộc 29/30 đạt, 28/30 rớt | High | Boundary (Edge case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Quản trị kỹ thuật (chạy lệnh, đọc báo cáo) | • Hai kết quả giả lập: 29 và 28 đề thỏa ràng buộc | — | 1. Chạy lệnh tính cho từng kết quả | • 29/30 = 96,7 %: đạt<br>• 28/30 = 93,3 %: rớt, mã thoát khác 0, danh sách đề rớt có 2 đề | US-S2E2-05-BV1; BR-S2E2-15 |
| TC-S2E2-041 | Đáp số khớp 27/30 đạt đúng ngưỡng, 26/30 rớt | High | Boundary (Edge case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Quản trị kỹ thuật (chạy lệnh, đọc báo cáo) | • Hai kết quả giả lập: 27 và 26 đề khớp đáp số | — | 1. Chạy lệnh tính cho từng kết quả | • 27/30 = 90,0 %: đạt (biên, dấu ≥)<br>• 26/30 = 86,7 %: rớt, mã thoát khác 0 | US-S2E2-05-BV2; BR-S2E2-15 |
| TC-S2E2-042 | Một bài sai mang nhãn Đã kiểm chứng thì rớt | High | Boundary (Edge case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Quản trị kỹ thuật (chạy lệnh, đọc báo cáo) | • Kết quả giả lập: 30/30 ràng buộc, 30/30 đáp số nhưng 1 bài có đáp số sai mà nhãn máy là "Đã kiểm chứng" | — | 1. Chạy lệnh tính | • M-S2-03 = 1 > 0: rớt; mã thoát khác 0; bài đó có tên trong danh sách rớt kèm "sai mang nhãn" | US-S2E2-05-BV3; BR-S2E2-15; M-S2-03 |
| TC-S2E2-043 | Bộ đề rỗng không chia cho 0 | Medium | Failure (Error case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Quản trị kỹ thuật (chạy lệnh, đọc báo cáo) | • Bộ đề rỗng | — | 1. Chạy lệnh | • Báo cáo ghi "—" cho tỉ lệ; mã thoát khác 0; thông báo "bộ đề rỗng" | US-S2E2-05-FC1; BR-S2E2-16 |
| TC-S2E2-044 | Có khóa AI thật thì từ chối chạy | High | Failure (Error case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Quản trị kỹ thuật (chạy lệnh, đọc báo cáo) | • Biến môi trường chứa khóa AI thật | — | 1. Chạy lệnh | • Từ chối chạy với thông báo "chỉ chạy với AI giả lập"; không gọi mạng | US-S2E2-05-FC2; BR-S2E2-17 |
| TC-S2E2-045 | Chạy hai lần cho cùng kết quả | Medium | Normal (Success case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Quản trị kỹ thuật (chạy lệnh, đọc báo cáo) | • Cùng bộ đề, AI giả lập | — | 1. Chạy lệnh hai lần | • Ba chỉ số và danh sách đề rớt giống hệt | US-S2E2-05-SC3; BR-S2E2-17 |

## 3. Truy vết tiêu chí sang case

| Tiêu chí | Case |
|---|---|
| US-S2E2-01-SC1 | TC-S2E2-001 |
| US-S2E2-01-FC1 | TC-S2E2-002 |
| US-S2E2-01-FC2 | TC-S2E2-003 |
| US-S2E2-01-FC3 | TC-S2E2-004 |
| US-S2E2-01-SC2 | TC-S2E2-005 |
| US-S2E2-01-SC3 | TC-S2E2-006 |
| US-S2E2-01-FC4 | TC-S2E2-007 |
| US-S2E2-02-SC1 | TC-S2E2-008 |
| US-S2E2-02-SC2 | TC-S2E2-009 |
| US-S2E2-02-SC3 | TC-S2E2-010 |
| US-S2E2-02-SC4 | TC-S2E2-011 |
| US-S2E2-02-SC5 | TC-S2E2-012 |
| US-S2E2-02-SC6 | TC-S2E2-013 |
| US-S2E2-02-SC7 | TC-S2E2-014 |
| US-S2E2-02-FC1 | TC-S2E2-015 |
| US-S2E2-02-BV1 | TC-S2E2-016 |
| US-S2E2-02-BV2 | TC-S2E2-017 |
| US-S2E2-02-BV3 | TC-S2E2-018 |
| US-S2E2-02-FC2 | TC-S2E2-019 |
| US-S2E2-02-FC3 | TC-S2E2-020 |
| US-S2E2-02-FC4 | TC-S2E2-021 |
| US-S2E2-03-SC1 | TC-S2E2-022 |
| US-S2E2-03-SC2 | TC-S2E2-023 |
| US-S2E2-03-FC1 | TC-S2E2-024 |
| US-S2E2-03-SC3 | TC-S2E2-025 |
| US-S2E2-03-FC2 | TC-S2E2-026 |
| US-S2E2-03-SC4 | TC-S2E2-027 |
| US-S2E2-03-SC5 | TC-S2E2-028 |
| US-S2E2-03-FC3 | TC-S2E2-029 |
| US-S2E2-03-BV1 | TC-S2E2-030 |
| US-S2E2-04-SC1 | TC-S2E2-031 |
| US-S2E2-04-SC2 | TC-S2E2-032 |
| US-S2E2-04-SC3 | TC-S2E2-033 |
| US-S2E2-04-FC1 | TC-S2E2-034 |
| US-S2E2-04-FC2 | TC-S2E2-035 |
| US-S2E2-04-FC3 | TC-S2E2-036 |
| US-S2E2-04-BV1 | TC-S2E2-037 |
| US-S2E2-05-SC1 | TC-S2E2-038 |
| US-S2E2-05-SC2 | TC-S2E2-039 |
| US-S2E2-05-BV1 | TC-S2E2-040 |
| US-S2E2-05-BV2 | TC-S2E2-041 |
| US-S2E2-05-BV3 | TC-S2E2-042 |
| US-S2E2-05-FC1 | TC-S2E2-043 |
| US-S2E2-05-FC2 | TC-S2E2-044 |
| US-S2E2-05-SC3 | TC-S2E2-045 |

### Khoảng trống

- Không tiêu chí nào thiếu case. Case chưa chạy được vì code chặng 2 chưa có (PO-65 Q1): trạng thái Not run.
- Câu hỏi và đề xuất đổi story map: `questions.md` cùng thư mục.

## 4. Dữ liệu và môi trường

- Dữ liệu: bảng `FX-nn` đầu tệp (đồng bộ với `stories_ac.md`).
- Môi trường: profile `dev` / `test`, AI giả lập, không bao giờ chạy trên `prod` (PO-51). Thiết bị theo mục quy ước của `stories_ac.md`.

## 5. Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: 45 case cho 45 tiêu chí, có cột Tự động hóa | runner-T-261126-12 (QC) |
| v0.2 | 2026-11-26 | đổi mã mẫu `HS-`/`GV-` có ký tự 0, 1, O, I sang ký tự hợp lệ (BR-S2E1-04), RV_s23 m-01 | runner-T-261126-15 (QC soát phản biện, RV_s23) |
