Phiên bản: v0.3 · 2026-11-26
# S2-E9 — test case (Biến thể đề, xuất ảnh/PDF và liên kết chỉ xem, câu trắc nghiệm trong bài luyện)

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
| `ProblemVariant` | biến thể của một đề gốc: bộ số liệu mới sinh từ đề gốc và đáp số do engine tính lại | STAGES Ch2 mục 8 |
| liên kết chỉ xem | địa chỉ ngẫu nhiên không đoán được cho phép xem đề và hình, không đăng nhập, không ghi, có hạn dùng và thu hồi được (đã chốt PO-66) | STAGES Ch2 mục 4 P1 |

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
| US-S2E9-01 Biến thể đề `ProblemVariant`, đáp số tính lại | 13 | 13 | 13 / 0 |
| US-S2E9-02 Xuất ảnh/PDF và liên kết chỉ xem | 15 | 15 | 15 / 0 |
| US-S2E9-03 Câu trắc nghiệm trong bài luyện | 16 | 16 | 16 / 0 |
| **Tổng** | **44** | **44** | **44 / 0** |

| Theo ưu tiên | Số case |
|---|---|
| High | 32 |
| Medium | 12 |
| Low | 0 |

## 2. Test case

### US-S2E9-01 — Biến thể đề `ProblemVariant`, đáp số tính lại

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | Status | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S2E9-001 | Tạo 3 biến thể hình chữ nhật với đáp số tính lại | High | Normal (Success case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Giáo viên Cô Trang (FX-01) | • Đề PL-03 (FX-12: AB = 6, AD = 8, đáp số AC = 10) | • FX-12; số lượng 3 | 1. Bấm "Tạo biến thể" với số lượng 3 | • Có 3 biến thể; mỗi biến thể có cặp (AB, AD) khác (6, 8) và khác nhau; đáp số AC của từng biến thể bằng căn của AB bình phương cộng AD bình phương (engine tính) | US-S2E9-01-SC1; STAGES Ch2 mục 3 ý 7 |
| TC-S2E9-002 | Biến thể giữ dạng chuẩn của đáp số | High | Normal (Success case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Giáo viên Cô Trang (FX-01) | • Đề PL-04 (FX-13: tam giác đều cạnh 4, đáp số 4√3), engine chọn cạnh 6 | • FX-13 | 1. Tạo biến thể | • Đáp số biến thể là 9√3 (dạng chuẩn có căn) cùng xấp xỉ 15,59 | US-S2E9-01-SC2; BR-S2E9-01 |
| TC-S2E9-003 | Biến thể mới mang Cần xem lại và AI soạn | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Biến thể vừa tạo | — | 1. Mở biến thể trong "Của tôi" | • Hiện hai nhãn "Cần xem lại" và "AI soạn"; có hình dựng thỏa ràng buộc; chưa "Giáo viên đã duyệt" | US-S2E9-01-SC3; BR-S2E9-06; PO-02 |
| TC-S2E9-004 | Cùng hạt giống cho cùng bộ số | Medium | Normal (Success case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Giáo viên Cô Trang (FX-01) | • Đề PL-03; hạt giống 42 | • Hạt giống 42 | 1. Tạo 3 biến thể với hạt giống 42 hai lần | • Hai lần cho đúng cùng ba bộ số | US-S2E9-01-SC4; BR-S2E9-04 |
| TC-S2E9-005 | Đề không có tham số số bị từ chối | High | Failure (Error case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Giáo viên Cô Trang (FX-01) | • Đề thuần chữ "Chứng minh tam giác cân" | — | 1. Bấm "Tạo biến thể" | • Từ chối "Đề này không có số liệu để đổi"; không biến thể nào được tạo | US-S2E9-01-FC1; BR-S2E9-05 |
| TC-S2E9-006 | Thiết diện động chưa hỗ trợ | Medium | Failure (Error case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Giáo viên Cô Trang (FX-01) | • Đề dạng thiết diện động | — | 1. Bấm "Tạo biến thể" | • Từ chối "Dạng này chưa hỗ trợ biến thể"; không biến thể | US-S2E9-01-FC2; BR-S2E9-05; PO-65 Q7 |
| TC-S2E9-007 | Không bộ nào vô nghĩa trong 1000 lần sinh | High | Failure (Error case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Giáo viên Cô Trang (FX-01) | • Đề tam giác ABC với ba cạnh tham số | — | 1. Sinh 1000 biến thể (mỗi lần 1, hạt giống khác nhau) | • Mọi bộ thỏa bất đẳng thức tam giác chặt (không suy biến) và dựng được hình thỏa ràng buộc; không bộ nào có số ngoài 1 đến 100 | US-S2E9-01-FC3; BR-S2E9-03; BR-S2E2-06 |
| TC-S2E9-008 | Tạo 1 và 10 biến thể nhận, 0 và 11 bị chặn | High | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Đề PL-03 | • Số lượng 0, 1, 10, 11 | 1. Tạo với số lượng 1; 10; 0; 11 | • 1 và 10: thành công<br>• 0: VALIDATION_FAILED "Chọn từ 1 đến 10"<br>• 11: VALIDATION_FAILED | US-S2E9-01-BV1; BR-S2E9-02; QA-S2E9-02 |
| TC-S2E9-009 | Số gốc sát cận 1 và 100 không sinh số ngoài 1 đến 100 | High | Boundary (Edge case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Giáo viên Cô Trang (FX-01) | • Hai đề: cạnh hình vuông gốc 1 và 100 | • Cạnh gốc 1 và 100 | 1. Tạo 10 biến thể cho mỗi đề | • Mọi cạnh sinh ra nằm trong [1; 100]; không có 0,5 hay 100,5; không trùng gốc | US-S2E9-01-BV2; BR-S2E9-03; QA-S2E9-02 |
| TC-S2E9-010 | Ít bộ khả dĩ thì trả ít hơn kèm thông báo | Medium | Failure (Error case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Giáo viên Cô Trang (FX-01) | • Đề có miền số chỉ cho phép đúng 4 bộ khác gốc | — | 1. Tạo 10 biến thể | • Trả 4 biến thể không trùng; hiện "Chỉ tạo được 4 biến thể khác nhau" | US-S2E9-01-FC4; BR-S2E9-02 |
| TC-S2E9-011 | Giáo viên khác không tạo biến thể từ đề Của tôi của người khác | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Đề FX-12 của Cô Trang; Cô Lan đăng nhập | — | 1. Cô Lan gọi tạo biến thể | • NOT_FOUND; không biến thể | US-S2E9-01-FC5; BR-S2E9-06; BR-S2-06 |
| TC-S2E9-012 | Biến thể tính vào giới hạn thêm đề: 20 nhận, 21 bị chặn | High | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Cô Trang đã thêm 18 đề trong 60 phút | — | 1. Tạo 3 biến thể một lần | • Hai biến thể đầu tạo được (đủ 20); biến thể thứ ba: RATE_LIMITED; thông báo nêu "tạo được 2 biến thể" | US-S2E9-01-BV3; BR-S2E9-06; BR-S2E6-13 |
| TC-S2E9-013 | Đáp số biến thể do engine tính, không gọi AI | High | Failure (Error case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Giáo viên Cô Trang (FX-01) | • Bộ đếm lần gọi AI giả lập về 0 | — | 1. Tạo 3 biến thể | • Bộ đếm gọi AI vẫn 0; đáp số có nguồn "engine" | US-S2E9-01-FC6; BR-S2E9-01; D-GEO-047 |

### US-S2E9-02 — Xuất ảnh/PDF và liên kết chỉ xem

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | Status | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S2E9-014 | Xuất ảnh PNG của một đề | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01); người xem liên kết (không đăng nhập) | • Đề FX-10 của Cô Trang | • FX-10 | 1. Bấm "Xuất ảnh" | • Tải về tệp PNG một trang có chữ đề và hình tam giác 5, 12, 13; không có lời giải | US-S2E9-02-SC1; BR-S2E9-07 |
| TC-S2E9-015 | Xuất PDF nhiều đề | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01); người xem liên kết (không đăng nhập) | • Cô Trang chọn 3 đề (FX-10, FX-11, FX-13) | — | 1. Bấm "Xuất PDF" | • Tệp PDF có 3 đề theo thứ tự đã chọn, mỗi đề có hình; không lời giải | US-S2E9-02-SC2; BR-S2E9-07 |
| TC-S2E9-016 | Kèm lời giải thì in cả hai nhãn | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01); người xem liên kết (không đăng nhập) | • Đề FX-14 có lời giải "Giáo viên đã duyệt", nhãn máy "Đã kiểm chứng"; chưa giao | • FX-14 | 1. Tick "Kèm lời giải"; bấm "Xuất PDF" | • PDF có lời giải đủ bước; đầu trang in "Đã kiểm chứng" và "Giáo viên đã duyệt" | US-S2E9-02-SC3; BR-S2E9-07; PO-02 |
| TC-S2E9-017 | Lời giải chưa duyệt không kèm được | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01); người xem liên kết (không đăng nhập) | • Đề có lời giải "AI soạn" chưa duyệt | — | 1. Xem ô "Kèm lời giải"; gọi API xuất kèm lời giải | • Ô khóa kèm "Cần duyệt lời giải trước"; API: CONFLICT; không tệp nào | US-S2E9-02-FC1; BR-S2E9-07 |
| TC-S2E9-018 | Đề đang giao và khóa phải xác nhận trước khi xuất kèm lời giải | High | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01); người xem liên kết (không đăng nhập) | • Đề FX-14 giao BG-1, lời giải khóa tới T0 + 2 giờ; lời giải đã duyệt | — | 1. Tick "Kèm lời giải"; bấm "Xuất PDF"; bấm "Hủy" ở hộp hỏi; bấm lại và "Vẫn xuất" | • Hiện hộp hỏi; "Hủy": không tệp<br>• "Vẫn xuất": có tệp; ghi lại việc xác nhận | US-S2E9-02-FC2; BR-S2E9-08 |
| TC-S2E9-019 | Xuất 1 và 20 đề nhận, 0 và 21 bị chặn | Medium | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01); người xem liên kết (không đăng nhập) | • Cô Trang có 25 đề | • 0, 1, 20, 21 đề | 1. Xuất PDF với 1; 20; 0; 21 đề | • 1 và 20: thành công<br>• 0: lỗi "Chọn ít nhất 1 đề"<br>• 21: VALIDATION_FAILED "Tối đa 20 đề mỗi tệp" | US-S2E9-02-BV1; BR-S2E9-07 |
| TC-S2E9-020 | Tạo liên kết chỉ xem mặc định hạn 7 ngày | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01); người xem liên kết (không đăng nhập) | • Đề FX-10 | — | 1. Bấm "Tạo liên kết chỉ xem" | • Hiện địa chỉ có mã ngẫu nhiên dài; hạn "7 ngày" mặc định; nút "Sao chép" và "Thu hồi" | US-S2E9-02-SC4; BR-S2E9-09 |
| TC-S2E9-021 | Mở liên kết không đăng nhập chỉ thấy đề và hình | High | Normal (Success case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01); người xem liên kết (không đăng nhập) | • Liên kết còn hạn của FX-10 | — | 1. Mở liên kết ở trình duyệt chưa đăng nhập | • Hiện chữ đề và hình; không lời giải, không tên hay mã `GV-` của Cô Trang; không nút sửa; dữ liệu trả về cũng không chứa lời giải hay tên giáo viên | US-S2E9-02-SC5; BR-S2E9-10 |
| TC-S2E9-022 | Liên kết hết hạn đúng mốc 7 ngày | High | Boundary (Edge case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Giáo viên Cô Trang (FX-01); người xem liên kết (không đăng nhập) | • Liên kết tạo lúc T0 hạn 7 ngày; đồng hồ giả | — | 1. Mở ở T0 + 7 ngày - 1 giây; mở ở T0 + 7 ngày | • Mốc trước: xem được<br>• đúng mốc: "Liên kết không còn dùng được" (cùng thông báo mã sai) | US-S2E9-02-BV2; BR-S2E9-09 |
| TC-S2E9-023 | Thu hồi thì liên kết như mã sai | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01); người xem liên kết (không đăng nhập) | • Liên kết còn hạn | — | 1. Bấm "Thu hồi"; mở liên kết đó; mở một mã sai | • Hai lần đều NOT_FOUND với cùng câu "Liên kết không còn dùng được"; không phân biệt đã thu hồi hay sai | US-S2E9-02-FC3; BR-S2E9-09; BR-S2E9-10 |
| TC-S2E9-024 | Dò mã liên kết bị giới hạn | High | Failure (Error case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Giáo viên Cô Trang (FX-01); người xem liên kết (không đăng nhập) | • Một địa chỉ mạng thử nhiều mã sai | — | 1. Gửi 100 yêu cầu với mã sai; gửi yêu cầu thứ 101 với mã đúng | • 100 lần đầu: NOT_FOUND<br>• lần 101 từ địa chỉ đó: RATE_LIMITED kèm `Retry-After` (kể cả với mã đúng) | US-S2E9-02-FC4; BR-S2E9-09; PO-63 |
| TC-S2E9-025 | Liên kết thứ 5 nhận, thứ 6 bị chặn | Medium | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01); người xem liên kết (không đăng nhập) | • Đề FX-10 có 4 liên kết đang hoạt động | — | 1. Tạo liên kết thứ 5; tạo liên kết thứ 6 | • Liên kết 5 thành công<br>• liên kết 6: lỗi "Tối đa 5 liên kết đang dùng", CONFLICT | US-S2E9-02-BV3; BR-S2E9-09; QA-S2E9-03 |
| TC-S2E9-026 | Liên kết chỉ xem không có thao tác ghi | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01); người xem liên kết (không đăng nhập) | • Liên kết còn hạn | — | 1. Gửi yêu cầu sửa đề qua liên kết; gửi yêu cầu giao bài | • FORBIDDEN; đề không đổi | US-S2E9-02-FC5; BR-S2E9-10 |
| TC-S2E9-027 | Giáo viên khác không xuất hay tạo liên kết cho đề Của tôi của người khác | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01); người xem liên kết (không đăng nhập) | • Đề FX-10 của Cô Trang; Cô Lan | — | 1. Cô Lan gọi xuất và tạo liên kết | • NOT_FOUND; không tệp, không liên kết | US-S2E9-02-FC6; BR-S2-06 |
| TC-S2E9-028 | Chọn hạn 1 hoặc 30 ngày | Medium | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01); người xem liên kết (không đăng nhập) | • Hộp tạo liên kết | — | 1. Chọn "1 ngày"; tạo; chọn "30 ngày"; tạo | • Hạn ghi đúng 1 ngày và 30 ngày; không có lựa chọn khác (ví dụ không giới hạn) | US-S2E9-02-SC6; BR-S2E9-09 |

### US-S2E9-03 — Câu trắc nghiệm trong bài luyện

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | Status | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S2E9-029 | Soạn câu trắc nghiệm 4 lựa chọn | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • Cô Trang ở "Soạn bài luyện"; đề FX-14 đã chọn | • FX-14 | 1. Thêm câu kiểu "Trắc nghiệm"; nhập 4 lựa chọn "30°", "45°", "60°", "90°"; chọn "45°" là đúng; lưu nháp | • Câu lưu với 4 lựa chọn, đúng 1 lựa chọn "45°"; mặc định 1 lượt | US-S2E9-03-SC1; BR-S2E9-11 |
| TC-S2E9-030 | Chọn đúng thì Đúng | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • An ở câu trắc nghiệm trên iPad | — | 1. Chọn "45°"; bấm "Kiểm tra" | • Hiện "Đúng rồi!"; câu đúng; sang câu kế được; không bàn phím toán | US-S2E9-03-SC2; BR-S2E9-13 |
| TC-S2E9-031 | Chọn sai trừ lượt, hết lượt thì xem lại cùng thầy cô | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • Câu 1 lượt; An chọn "60°" | — | 1. Chọn "60°"; bấm "Kiểm tra" | • Hiện "Chưa đúng. Em xem lại cùng thầy cô nhé."; chip "Còn 0/1 lượt"; "60°" bị làm mờ; không nói lựa chọn nào đúng; câu "xem lại cùng thầy cô" | US-S2E9-03-SC3; BR-S2E9-12; DQ-12 |
| TC-S2E9-032 | Câu có nhiều lượt: lựa chọn sai bị làm mờ | Medium | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • Câu trắc nghiệm 4 lựa chọn đặt 2 lượt | — | 1. Chọn "60°"; bấm "Kiểm tra"; thử chọn lại "60°" | • Lượt còn 1; "60°" làm mờ và không chọn lại được; các lựa chọn khác còn chọn được | US-S2E9-03-SC4; BR-S2E9-12 |
| TC-S2E9-033 | Chưa chọn thì nút Kiểm tra khóa | High | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • Câu trắc nghiệm chưa chọn | — | 1. Bấm "Kiểm tra" | • Nút khóa; không yêu cầu nào gửi | US-S2E9-03-FC1; BR-S2E9-13 |
| TC-S2E9-034 | 2 và 5 lựa chọn nhận, 1 và 6 bị chặn | High | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • Soạn câu trắc nghiệm | • 1, 2, 5, 6 lựa chọn | 1. Lưu với 1; 2; 5; 6 lựa chọn | • 1: lỗi "Cần từ 2 đến 5 lựa chọn"<br>• 2 và 5: lưu được<br>• 6: lỗi tương tự | US-S2E9-03-BV1; BR-S2E9-11; QA-S2E9-04 |
| TC-S2E9-035 | Lựa chọn 100 ký tự nhận, 101 bị chặn, rỗng bị chặn | Medium | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • Soạn câu trắc nghiệm | • 0, 100, 101 ký tự | 1. Lưu với một lựa chọn dài 100 ký tự; 101 ký tự; rỗng | • 100: lưu được<br>• 101: VALIDATION_FAILED<br>• rỗng: lỗi "Lựa chọn đang trống" | US-S2E9-03-BV2; BR-S2E9-11 |
| TC-S2E9-036 | Không có lựa chọn đúng bị chặn | High | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • Soạn câu 4 lựa chọn không chọn đúng | — | 1. Bấm "Lưu nháp" | • Lỗi "Hãy chọn một đáp án đúng"; không lưu | US-S2E9-03-FC2; BR-S2E9-11 |
| TC-S2E9-037 | Hai lựa chọn đúng bị chặn | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • Câu 4 lựa chọn; gọi API đánh dấu 2 lựa chọn đúng | — | 1. Lưu | • VALIDATION_FAILED "Chỉ một đáp án đúng"; không lưu | US-S2E9-03-FC3; BR-S2E9-11 |
| TC-S2E9-038 | Lựa chọn trùng nhau bị chặn | Medium | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • Lựa chọn "45°" và " 45° " (khác khoảng trắng) | — | 1. Lưu | • Lỗi "Hai lựa chọn giống nhau"; không lưu | US-S2E9-03-FC4; BR-S2E9-11 |
| TC-S2E9-039 | Dữ liệu trả về không chứa cờ lựa chọn đúng | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • An mở bài; câu trắc nghiệm trước giờ mở lời giải | — | 1. Xem toàn bộ dữ liệu trả về khi mở và sau khi chọn sai | • Không có trường cho biết lựa chọn đúng, không có chỉ số vị trí đúng; phản hồi Sai không nêu lựa chọn đúng | US-S2E9-03-FC5; BR-S2E9-14 |
| TC-S2E9-040 | Gửi mã lựa chọn không thuộc câu không trừ lượt | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • Câu trắc nghiệm 1 lượt | — | 1. Gọi chấm với mã lựa chọn của câu khác | • VALIDATION_FAILED; không Attempt; lượt vẫn 1 | US-S2E9-03-FC6; BR-S2E9-12 |
| TC-S2E9-041 | Bấm đôi chỉ trừ một lượt | Medium | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • Câu 2 lượt; hai yêu cầu cùng mã chống lặp | — | 1. Gửi lựa chọn sai hai lần cùng mã trong 1 giây | • Một Attempt; lượt còn 1 | US-S2E9-03-FC7; BR-S2E8-14 |
| TC-S2E9-042 | Bài luyện hỗn hợp: kết quả đếm chung hai kiểu câu | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • Bài có 3 câu điền và 2 câu trắc nghiệm; An đúng 4, hết lượt 1 | — | 1. Mở màn kết quả | • Hiện "4 câu đúng · 1 câu cần xem lại" (đếm chung); không điểm | US-S2E9-03-SC5; BR-S2E8-21 |
| TC-S2E9-043 | Nút Em nghĩ đáp số này đúng không hiện ở câu trắc nghiệm | Medium | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • An hết lượt ở câu trắc nghiệm | — | 1. Xem màn | • Không có nút "Em nghĩ đáp số này đúng"; API báo chấm sai cho câu này nhận CONFLICT | US-S2E9-03-SC6; BR-S2E9-14 |
| TC-S2E9-044 | Số lượt trắc nghiệm đặt tối đa bằng số lựa chọn trừ 1 | Medium | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • Câu 4 lựa chọn | — | 1. Đặt số lượt 3; đặt số lượt 4 | • 3: lưu được<br>• 4: VALIDATION_FAILED "Tối đa 3 lượt cho câu 4 lựa chọn" | US-S2E9-03-BV3; BR-S2E9-12 |

## 3. Truy vết tiêu chí sang case

| Tiêu chí | Case |
|---|---|
| US-S2E9-01-SC1 | TC-S2E9-001 |
| US-S2E9-01-SC2 | TC-S2E9-002 |
| US-S2E9-01-SC3 | TC-S2E9-003 |
| US-S2E9-01-SC4 | TC-S2E9-004 |
| US-S2E9-01-FC1 | TC-S2E9-005 |
| US-S2E9-01-FC2 | TC-S2E9-006 |
| US-S2E9-01-FC3 | TC-S2E9-007 |
| US-S2E9-01-BV1 | TC-S2E9-008 |
| US-S2E9-01-BV2 | TC-S2E9-009 |
| US-S2E9-01-FC4 | TC-S2E9-010 |
| US-S2E9-01-FC5 | TC-S2E9-011 |
| US-S2E9-01-BV3 | TC-S2E9-012 |
| US-S2E9-01-FC6 | TC-S2E9-013 |
| US-S2E9-02-SC1 | TC-S2E9-014 |
| US-S2E9-02-SC2 | TC-S2E9-015 |
| US-S2E9-02-SC3 | TC-S2E9-016 |
| US-S2E9-02-FC1 | TC-S2E9-017 |
| US-S2E9-02-FC2 | TC-S2E9-018 |
| US-S2E9-02-BV1 | TC-S2E9-019 |
| US-S2E9-02-SC4 | TC-S2E9-020 |
| US-S2E9-02-SC5 | TC-S2E9-021 |
| US-S2E9-02-BV2 | TC-S2E9-022 |
| US-S2E9-02-FC3 | TC-S2E9-023 |
| US-S2E9-02-FC4 | TC-S2E9-024 |
| US-S2E9-02-BV3 | TC-S2E9-025 |
| US-S2E9-02-FC5 | TC-S2E9-026 |
| US-S2E9-02-FC6 | TC-S2E9-027 |
| US-S2E9-02-SC6 | TC-S2E9-028 |
| US-S2E9-03-SC1 | TC-S2E9-029 |
| US-S2E9-03-SC2 | TC-S2E9-030 |
| US-S2E9-03-SC3 | TC-S2E9-031 |
| US-S2E9-03-SC4 | TC-S2E9-032 |
| US-S2E9-03-FC1 | TC-S2E9-033 |
| US-S2E9-03-BV1 | TC-S2E9-034 |
| US-S2E9-03-BV2 | TC-S2E9-035 |
| US-S2E9-03-FC2 | TC-S2E9-036 |
| US-S2E9-03-FC3 | TC-S2E9-037 |
| US-S2E9-03-FC4 | TC-S2E9-038 |
| US-S2E9-03-FC5 | TC-S2E9-039 |
| US-S2E9-03-FC6 | TC-S2E9-040 |
| US-S2E9-03-FC7 | TC-S2E9-041 |
| US-S2E9-03-SC5 | TC-S2E9-042 |
| US-S2E9-03-SC6 | TC-S2E9-043 |
| US-S2E9-03-BV3 | TC-S2E9-044 |

### Khoảng trống

- Không tiêu chí nào thiếu case. Case chưa chạy được vì code chặng 2 chưa có (PO-65 Q1): trạng thái Not run.
- Câu hỏi và đề xuất đổi story map: `questions.md` cùng thư mục.

## 4. Dữ liệu và môi trường

- Dữ liệu: bảng `FX-nn` đầu tệp (đồng bộ với `stories_ac.md`).
- Môi trường: profile `dev` / `test`, AI giả lập, không bao giờ chạy trên `prod` (PO-51). Thiết bị theo mục quy ước của `stories_ac.md`.

## 5. Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: 44 case cho 44 tiêu chí, có cột Tự động hóa | runner-T-261126-12 (QC) |
| v0.2 | 2026-11-26 | đổi mã mẫu `HS-`/`GV-` có ký tự 0, 1, O, I sang ký tự hợp lệ (BR-S2E1-04), RV_s23 m-01 | runner-T-261126-15 (QC soát phản biện, RV_s23) |
| v0.3 | 2026-11-26 | Áp PO-66: đổi nhãn PROVISIONAL thành PO-66 | runner-T-261126-37 (BA/QC) |
