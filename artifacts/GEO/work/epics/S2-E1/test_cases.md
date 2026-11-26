Phiên bản: v0.2 · 2026-11-26
# S2-E1 — test case (Đăng ký, đăng nhập bằng mã và khôi phục (định danh tối giản PO-61))

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
| `RC-xxxx-xxxx-xxxx` | mã khôi phục của giáo viên, hiện một lần lúc đăng ký (TẠM) | PO-61, DQ-16 |
| `GV-xxxx-xxxx` · `HS-xxxx-xxxx` | mã người dùng sinh tự động, tám ký tự sau tiền tố vai (TẠM) | PO-61, DQ-16 |

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
| US-S2E1-01 Đăng ký bằng tên hiển thị và vai, nhận mã người dùng và mã khôi phục | 18 | 18 | 18 / 0 |
| US-S2E1-02 Đăng nhập bằng mã và mật khẩu; khóa 10 lần sai trong 15 phút theo tài khoản | 12 | 12 | 12 / 0 |
| US-S2E1-03 Giáo viên tự khôi phục mật khẩu bằng mã khôi phục (TẠM) | 8 | 8 | 7 / 1 |
| US-S2E1-04 Dữ liệu: chuyển tài khoản mẫu MVP sang mô hình mã người dùng | 6 | 6 | 6 / 0 |
| **Tổng** | **44** | **44** | **43 / 1** |

| Theo ưu tiên | Số case |
|---|---|
| High | 26 |
| Medium | 16 |
| Low | 2 |

## 2. Test case

### US-S2E1-01 — Đăng ký bằng tên hiển thị và vai, nhận mã người dùng và mã khôi phục

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | Status | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S2E1-001 | Giáo viên đăng ký và nhận hai mã | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Người chưa có tài khoản (vai chọn khi đăng ký) | • Màn "Đăng ký" ở web; chưa có tài khoản tên "Cô Trang" (FX-01) | • Tên "Cô Trang"; mật khẩu `Trang@2026x` | 1. Chọn vai "Giáo viên"; gõ tên "Cô Trang", mật khẩu `Trang@2026x`; tick "đã đọc thông báo riêng tư (bản 1.0)"; bấm "Đăng ký" | • Màn mã hiện mã người dùng dạng `GV-xxxx-xxxx` (8 ký tự hợp lệ)<br>• hiện mã khôi phục dạng `RC-xxxx-xxxx-xxxx`<br>• có nút "Sao chép" và "In / lưu ảnh"<br>• có dòng cảnh báo mất cả mật khẩu lẫn mã khôi phục thì không lấy lại được | US-S2E1-01-SC1; PO-61; DQ-16 |
| TC-S2E1-002 | Học sinh đăng ký chỉ nhận mã người dùng | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Người chưa có tài khoản (vai chọn khi đăng ký) | • Màn "Đăng ký" ở iPad; chưa có tài khoản tên "An" | • Tên "An"; mật khẩu `AnHoc#2026` | 1. Chọn vai "Học sinh"; gõ tên "An", mật khẩu `AnHoc#2026`; tick đã đọc thông báo; bấm "Đăng ký" | • Màn mã hiện mã dạng `HS-xxxx-xxxx`<br>• không hiện mã khôi phục<br>• có dòng "Quên mật khẩu thì nhờ giáo viên của lớp đặt lại" | US-S2E1-01-SC2; PO-61; DQ-17 |
| TC-S2E1-003 | Nút Tiếp tục chỉ mở khi đã tick "Tôi đã lưu mã" | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Người chưa có tài khoản (vai chọn khi đăng ký) | • Màn mã đang hiện (giáo viên) | — | 1. Bấm "Tiếp tục" khi chưa tick<br>2. tick "Tôi đã lưu mã" rồi bấm "Tiếp tục" | • Lần 1: nút bị khóa, không chuyển màn<br>• lần 2: chuyển vào Kho đề đã đăng nhập | US-S2E1-01-SC3; DQ-16 |
| TC-S2E1-004 | Mã khôi phục không hiện lại lần hai | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Người chưa có tài khoản (vai chọn khi đăng ký) | • Giáo viên vừa qua màn mã | — | 1. Đăng xuất; đăng nhập lại; mở mọi màn tài khoản | • Không màn nào hiện lại mã khôi phục; dữ liệu trả về của mọi yêu cầu sau đăng ký không chứa mã khôi phục (chỉ chứa dấu hiệu "đã có") | US-S2E1-01-SC4; DQ-16; PO-61 |
| TC-S2E1-005 | Hai người cùng tên có hai mã khác nhau | Medium | Normal (Success case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Người chưa có tài khoản (vai chọn khi đăng ký) | • Đã có học sinh "An" mã `HS-3F8A-2L6P` | — | 1. Đăng ký thêm học sinh tên "An" | • Tạo thành công, mã mới khác `HS-3F8A-2L6P`; hai tài khoản tồn tại độc lập | US-S2E1-01-SC5; DQ-25; BR-S2E1-02 |
| TC-S2E1-006 | Yêu cầu đăng ký chỉ chứa các trường cho phép | High | Normal (Success case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Người chưa có tài khoản (vai chọn khi đăng ký) | • Màn "Đăng ký" mở | — | 1. Đăng ký thành công, bắt yêu cầu gửi lên máy chủ | • Thân yêu cầu chỉ có tên hiển thị, vai, mật khẩu, cờ đã đọc thông báo và số phiên bản văn bản; không có email, số điện thoại, ngày sinh, trường, ảnh | US-S2E1-01-SC6; PO-61; PO-64 |
| TC-S2E1-007 | Tên rỗng hoặc chỉ khoảng trắng bị chặn | High | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Người chưa có tài khoản (vai chọn khi đăng ký) | • Màn "Đăng ký" | • Tên "" và "   " | 1. Để trống tên (hoặc gõ 3 dấu cách); điền phần còn lại; bấm "Đăng ký" | • Hiện lỗi "Hãy nhập tên hiển thị." cạnh ô tên<br>• không tạo tài khoản; mã lỗi VALIDATION_FAILED nếu gọi thẳng | US-S2E1-01-FC1; BR-S2E1-02 |
| TC-S2E1-008 | Tên đúng 60 ký tự được nhận | Medium | Boundary (Edge case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Người chưa có tài khoản (vai chọn khi đăng ký) | • Màn "Đăng ký" | • Tên 60 ký tự | 1. Gõ tên gồm 60 chữ "A"; điền phần còn lại; bấm "Đăng ký" | • Tạo tài khoản thành công; tên hiển thị lưu đủ 60 ký tự | US-S2E1-01-BV1; BR-S2E1-02 |
| TC-S2E1-009 | Tên 61 ký tự bị chặn | Medium | Boundary (Edge case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Người chưa có tài khoản (vai chọn khi đăng ký) | • Màn "Đăng ký" | • Tên 61 ký tự | 1. Gõ tên gồm 61 chữ "A"; bấm "Đăng ký" | • Hiện lỗi "Tên hiển thị tối đa 60 ký tự."; không tạo tài khoản | US-S2E1-01-BV2; BR-S2E1-02 |
| TC-S2E1-010 | Tên 1 ký tự và khoảng trắng đầu cuối được cắt | Low | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Người chưa có tài khoản (vai chọn khi đăng ký) | • Màn "Đăng ký" | • Tên "  B  " | 1. Gõ tên "  B  " (một chữ B giữa hai dấu cách); bấm "Đăng ký" | • Tạo thành công; tên lưu là "B" | US-S2E1-01-BV3; BR-S2E1-02 |
| TC-S2E1-011 | Mật khẩu 8 ký tự được nhận | Medium | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Người chưa có tài khoản (vai chọn khi đăng ký) | • Màn "Đăng ký" | • Mật khẩu 8 ký tự | 1. Đặt mật khẩu 8 ký tự `Abcd1234`; bấm "Đăng ký" | • Tạo tài khoản thành công | US-S2E1-01-BV4; BR-S2E1-03; QA-S2E1-02 |
| TC-S2E1-012 | Mật khẩu 7 ký tự bị chặn | Medium | Boundary (Edge case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Người chưa có tài khoản (vai chọn khi đăng ký) | • Màn "Đăng ký" | • Mật khẩu 7 ký tự | 1. Đặt mật khẩu 7 ký tự `Abcd123`; bấm "Đăng ký" | • Hiện lỗi "Mật khẩu từ 8 đến 64 ký tự."; không tạo tài khoản | US-S2E1-01-BV5; BR-S2E1-03; QA-S2E1-02 |
| TC-S2E1-013 | Mật khẩu 64 nhận, 65 bị chặn | Low | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Người chưa có tài khoản (vai chọn khi đăng ký) | • Gọi đăng ký qua API hai lần | • Mật khẩu 64 và 65 ký tự | 1. Lần 1: mật khẩu dài 64 ký tự<br>2. lần 2: mật khẩu dài 65 ký tự | • Lần 1 thành công<br>• lần 2: VALIDATION_FAILED, không tạo tài khoản | US-S2E1-01-BV6; BR-S2E1-03; QA-S2E1-02 |
| TC-S2E1-014 | Chưa tick thông báo riêng tư thì không đăng ký được | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Người chưa có tài khoản (vai chọn khi đăng ký) | • Màn "Đăng ký", điền đủ tên, vai, mật khẩu | — | 1. Bấm "Đăng ký" khi chưa tick; gọi API với cờ đã đọc = sai | • Nút khóa trên giao diện; API trả VALIDATION_FAILED; không tạo tài khoản | US-S2E1-01-FC2; PO-64; DQ-15 |
| TC-S2E1-015 | Vai không hợp lệ bị từ chối | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Người chưa có tài khoản (vai chọn khi đăng ký) | • Gọi API đăng ký với vai "admin" (và vai rỗng) | • Vai "admin", "" | 1. Gửi hai yêu cầu | • Cả hai trả VALIDATION_FAILED; không có tài khoản nào mới | US-S2E1-01-FC3; PO-61; BR-S2-01 |
| TC-S2E1-016 | Lần đăng ký thứ 5 trong giờ nhận, lần thứ 6 bị giới hạn | High | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Người chưa có tài khoản (vai chọn khi đăng ký) | • Một địa chỉ mạng chưa đăng ký lần nào trong giờ này | — | 1. Đăng ký liên tiếp 6 tài khoản khác tên<br>2. chờ theo `Retry-After`; đăng ký lần nữa | • Năm lần đầu thành công<br>• lần 6: RATE_LIMITED kèm `Retry-After`<br>• sau thời gian đó đăng ký được | US-S2E1-01-BV7; PO-63; BR-S2E1-06; QA-S2E1-03 |
| TC-S2E1-017 | Mã người dùng không bao giờ trùng | Medium | Failure (Error case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Người chưa có tài khoản (vai chọn khi đăng ký) | • Cơ sở dữ liệu thử rỗng | — | 1. Đăng ký 1000 tài khoản liên tiếp (vai trộn) | • 1000 mã khác nhau; mọi mã khớp `GV-` hoặc `HS-` + 8 ký tự hợp lệ; không có O, 0, I, 1 | US-S2E1-01-FC4; BR-S2E1-04; DQ-16 |
| TC-S2E1-018 | Bấm Đăng ký liên tiếp chỉ tạo một tài khoản | Medium | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Người chưa có tài khoản (vai chọn khi đăng ký) | • Màn "Đăng ký" điền đủ hợp lệ | — | 1. Bấm "Đăng ký" hai lần trong 1 giây | • Chỉ một tài khoản được tạo; màn mã hiện một lần | US-S2E1-01-FC5; DQ-15 |

### US-S2E1-02 — Đăng nhập bằng mã và mật khẩu; khóa 10 lần sai trong 15 phút theo tài khoản

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | Status | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S2E1-019 | Học sinh đăng nhập đúng | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) hoặc giáo viên Cô Trang (FX-01) | • Học sinh An (FX-02) ở màn "Đăng nhập" trên iPad | — | 1. Gõ mã `HS-3F8A-2L6P`, mật khẩu `AnHoc#2026`; bấm "Đăng nhập" | • Mở màn "Bài của em" với dòng "Chào An" | US-S2E1-02-SC1; PO-61 |
| TC-S2E1-020 | Giáo viên đăng nhập đúng | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) hoặc giáo viên Cô Trang (FX-01) | • Cô Trang (FX-01) ở màn "Đăng nhập" trên web | — | 1. Gõ mã `GV-7K2M-9Q4D`, mật khẩu `Trang@2026x`; bấm "Đăng nhập" | • Mở Kho đề với ngăn "Của tôi" | US-S2E1-02-SC2; PO-61 |
| TC-S2E1-021 | Mã gõ chữ thường và có dấu cách vẫn vào được | Medium | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Học sinh An (FX-02) hoặc giáo viên Cô Trang (FX-01) | • Học sinh An (FX-02) | • Mã " hs-3f8a-2l6p " | 1. Gõ mã " hs-3f8a-2l6p " (chữ thường, dấu cách hai đầu) và đúng mật khẩu; bấm "Đăng nhập" | • Vào đúng tài khoản An | US-S2E1-02-SC3; BR-S2E1-07; QA-S2E1-08 |
| TC-S2E1-022 | Sai mật khẩu hiện câu chung | High | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) hoặc giáo viên Cô Trang (FX-01) | • Học sinh An (FX-02) | — | 1. Gõ mã đúng, mật khẩu sai; bấm "Đăng nhập" | • Hiện "Mã hoặc mật khẩu chưa đúng"; không vào được; không chỉ ra ô nào sai | US-S2E1-02-FC1; DQ-18 |
| TC-S2E1-023 | Mã không tồn tại trả đúng cùng một phản hồi | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Học sinh An (FX-02) hoặc giáo viên Cô Trang (FX-01) | • Mã `HS-ZZZZ-ZZZZ` chưa đăng ký; học sinh An có mật khẩu sai | — | 1. Đăng nhập với mã không tồn tại; đăng nhập với mã An và mật khẩu sai | • Hai phản hồi cùng mã lỗi, cùng câu chữ, cùng cấu trúc; không phân biệt được mã nào có thật | US-S2E1-02-FC2; DQ-18; BR-S2E1-08 |
| TC-S2E1-024 | Sai 9 lần chưa bị khóa | High | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Học sinh An (FX-02) hoặc giáo viên Cô Trang (FX-01) | • Tài khoản An chưa sai lần nào | — | 1. Đăng nhập sai mật khẩu 9 lần liên tiếp; lần thứ 10 nhập mật khẩu đúng | • Chín lần đầu hiện câu chung; lần thứ 10 (đúng) vào được; bộ đếm về 0 | US-S2E1-02-BV1; PO-53; BR-S2E1-09 |
| TC-S2E1-025 | Sai lần thứ 10 thì khóa | High | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Học sinh An (FX-02) hoặc giáo viên Cô Trang (FX-01) | • Tài khoản An chưa sai lần nào | — | 1. Đăng nhập sai 10 lần liên tiếp; lần 11 nhập mật khẩu đúng | • Sau lần sai thứ 10 hiện "Tài khoản tạm khóa, thử lại sau 15 phút"<br>• lần 11 dù đúng mật khẩu vẫn bị từ chối trong lúc khóa; mã lỗi RATE_LIMITED kèm `Retry-After` | US-S2E1-02-BV2; PO-53; BR-S2E1-09 |
| TC-S2E1-026 | Hết khóa đúng ở mốc 15 phút | High | Boundary (Edge case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Học sinh An (FX-02) hoặc giáo viên Cô Trang (FX-01) | • Tài khoản An vừa bị khóa lúc T0 (sau lần sai thứ 10) | — | 1. Ở T0 + 14:59 đăng nhập đúng mật khẩu<br>2. ở T0 + 15:00 đăng nhập đúng mật khẩu | • T0 + 14:59: vẫn bị khóa<br>• T0 + 15:00: vào được (giờ máy chủ, giả lập đồng hồ) | US-S2E1-02-BV3; PO-53; BR-S2E1-09 |
| TC-S2E1-027 | Đăng nhập đúng ở lần thứ 9 làm bộ đếm về 0 | Medium | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Học sinh An (FX-02) hoặc giáo viên Cô Trang (FX-01) | • Tài khoản An đã sai 9 lần liên tiếp | — | 1. Đăng nhập đúng; sau đó sai thêm 9 lần liên tiếp | • Đăng nhập đúng được; 9 lần sai sau đó chưa khóa (bộ đếm không cộng dồn qua lần đúng) | US-S2E1-02-BV4; BR-S2E1-09 |
| TC-S2E1-028 | Khóa tài khoản này không ảnh hưởng tài khoản khác cùng máy | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Học sinh An (FX-02) hoặc giáo viên Cô Trang (FX-01) | • An bị khóa; Bình (FX-03) dùng cùng địa chỉ mạng | — | 1. Bình đăng nhập đúng mật khẩu | • Bình vào được bình thường | US-S2E1-02-FC3; BR-S2E1-09; PO-53 |
| TC-S2E1-029 | Để trống một trong hai ô | Medium | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) hoặc giáo viên Cô Trang (FX-01) | • Màn "Đăng nhập" | — | 1. Bấm "Đăng nhập" khi ô mã trống; rồi khi ô mật khẩu trống | • Hiện lỗi cạnh ô trống; không gửi yêu cầu lên máy chủ; không tính một lần sai | US-S2E1-02-FC4; DQ-18 |
| TC-S2E1-030 | Phiên hết hạn thì yêu cầu bị từ chối | Medium | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) hoặc giáo viên Cô Trang (FX-01) | • Học sinh An đã đăng nhập; phiên bị hết hạn ở máy chủ | — | 1. Bấm vào một bài trong "Bài của em" | • Hiện "Phiên đã hết. Vui lòng vào lại."<br>• chuyển về màn "Đăng nhập"; mã lỗi UNAUTHENTICATED | US-S2E1-02-FC5; MVP, MSG-GEO-02 |

### US-S2E1-03 — Giáo viên tự khôi phục mật khẩu bằng mã khôi phục (TẠM)

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | Status | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S2E1-031 | Khôi phục đúng đặt được mật khẩu mới | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Cô Trang (FX-01) đã quên mật khẩu, còn mã khôi phục hợp lệ | — | 1. Ở "Đăng nhập" chọn "Quên mật khẩu (giáo viên)"; nhập mã `GV-7K2M-9Q4D`, mã khôi phục, mật khẩu mới `Trang@2027y`; bấm "Đặt lại" | • Đổi mật khẩu thành công<br>• hiện mã khôi phục MỚI một lần kèm nút tick "đã lưu"<br>• đăng nhập bằng `Trang@2027y` được | US-S2E1-03-SC1; PO-61 |
| TC-S2E1-032 | Mật khẩu cũ và mã khôi phục cũ hết hiệu lực | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Cô Trang vừa khôi phục thành công | — | 1. Đăng nhập bằng mật khẩu cũ; dùng lại mã khôi phục cũ để khôi phục lần nữa | • Mật khẩu cũ bị từ chối<br>• mã khôi phục cũ bị từ chối với câu lỗi chung | US-S2E1-03-SC2; BR-S2E1-10; QA-S2E1-01 |
| TC-S2E1-033 | Sai mã khôi phục hiện câu chung | High | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01) | • Cô Trang | — | 1. Nhập mã người dùng đúng, mã khôi phục sai; bấm "Đặt lại" | • Hiện "Mã người dùng hoặc mã khôi phục chưa đúng"; mật khẩu không đổi | US-S2E1-03-FC1; PO-61 |
| TC-S2E1-034 | Sai 9 lần chưa khóa, sai 10 lần khóa đường khôi phục | High | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Cô Trang, bộ đếm khôi phục bằng 0 | — | 1. Nhập sai mã khôi phục 9 lần; nhập đúng ở lần 10 (nhánh A)<br>2. nhánh B: sai 10 lần rồi nhập mã đúng ở lần 11 | • Nhánh A: lần 10 thành công<br>• nhánh B: sau lần sai thứ 10 bị khóa 15 phút, lần 11 đúng vẫn bị từ chối (RATE_LIMITED); đường đăng nhập bằng mật khẩu cũ vẫn dùng được | US-S2E1-03-BV1; BR-S2E1-11; PO-53 |
| TC-S2E1-035 | Mã học sinh dùng đường khôi phục nhận câu chung | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Học sinh An (FX-02) | — | 1. Nhập mã `HS-3F8A-2L6P` và một chuỗi bất kỳ làm mã khôi phục | • Hiện cùng câu "Mã người dùng hoặc mã khôi phục chưa đúng"; không nói "học sinh không có mã khôi phục" | US-S2E1-03-FC2; BR-S2E1-12; DQ-17 |
| TC-S2E1-036 | Mật khẩu mới 7 ký tự bị chặn, 8 ký tự nhận | Medium | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Cô Trang, mã khôi phục hợp lệ | • Mật khẩu 7 và 8 ký tự | 1. Đặt lại với mật khẩu mới 7 ký tự; đặt lại với 8 ký tự | • Lần 1: lỗi "Mật khẩu từ 8 đến 64 ký tự.", mã khôi phục vẫn còn hiệu lực<br>• lần 2: thành công | US-S2E1-03-BV2; BR-S2E1-10 |
| TC-S2E1-037 | Màn đăng ký nói rõ không lấy lại được khi mất cả hai | Medium | Normal (Success case) | chức năng | thủ công, có lý do | Manual — chữ cảnh báo cần mắt người xét rõ ràng | Not run | Giáo viên Cô Trang (FX-01) | • Màn mã sau đăng ký của giáo viên | — | 1. Đọc dòng cảnh báo cạnh mã khôi phục | • Có câu: mất cả mật khẩu lẫn mã khôi phục thì không lấy lại được tài khoản | US-S2E1-03-SC3; BRD delta mục 2 dòng 6; PO-61 |
| TC-S2E1-038 | Đổi mật khẩu bằng khôi phục hủy phiên đang đăng nhập khác | Medium | Failure (Error case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Giáo viên Cô Trang (FX-01) | • Cô Trang đang đăng nhập ở máy B | — | 1. Khôi phục mật khẩu ở máy A; ở máy B bấm sang một màn khác | • Máy B nhận UNAUTHENTICATED và về màn "Đăng nhập" | US-S2E1-03-FC3; QA-S2E1-01 |

### US-S2E1-04 — Dữ liệu: chuyển tài khoản mẫu MVP sang mô hình mã người dùng

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | Status | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S2E1-039 | Mỗi học sinh MVP thành một tài khoản có mã | High | Normal (Success case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Quản trị chạy lệnh chuyển dữ liệu (không có giao diện) | • Dữ liệu mẫu MVP với 5 học sinh lớp 11A1 | — | 1. Chạy lệnh chuyển | • Có 5 tài khoản `HS-` mới (mỗi mã khác nhau)<br>• tiến độ của An vẫn 2/3 trên BG-1<br>• số bài làm trước và sau bằng nhau | US-S2E1-04-SC1; PO-61 |
| TC-S2E1-040 | Giáo viên mẫu thành một tài khoản có mã | High | Normal (Success case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Quản trị chạy lệnh chuyển dữ liệu (không có giao diện) | • Dữ liệu mẫu MVP có "Giáo viên mẫu" | — | 1. Chạy lệnh chuyển | • Có đúng một tài khoản `GV-` cho giáo viên mẫu, vẫn là chủ lớp 11A1; có mã khôi phục sinh một lần ghi vào báo cáo chuyển | US-S2E1-04-SC2; PO-61 |
| TC-S2E1-041 | Chạy lại lệnh không tạo trùng | High | Boundary (Edge case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Quản trị chạy lệnh chuyển dữ liệu (không có giao diện) | • Lệnh chuyển đã chạy một lần | — | 1. Chạy lệnh chuyển lần hai | • Số tài khoản, số bài làm, số lớp không đổi; báo cáo ghi "đã chuyển trước đó" cho mọi dòng | US-S2E1-04-SC3; BR-S2E1-14 |
| TC-S2E1-042 | Dòng dữ liệu lỗi không làm dừng cả lô | Medium | Failure (Error case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Quản trị chạy lệnh chuyển dữ liệu (không có giao diện) | • Bộ thử có một học sinh tên rỗng | — | 1. Chạy lệnh chuyển | • Dòng lỗi vào báo cáo lỗi kèm lý do; 4 học sinh còn lại được chuyển; lệnh kết thúc với mã thoát khác 0 | US-S2E1-04-FC1; BR-S2E1-13 |
| TC-S2E1-043 | Tài khoản chuyển phải đặt mật khẩu mới ở lần đăng nhập đầu | Medium | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Quản trị chạy lệnh chuyển dữ liệu (không có giao diện) | • Tài khoản An sau chuyển có mật khẩu tạm | — | 1. Đăng nhập bằng mã và mật khẩu tạm | • Hiện màn "Đặt mật khẩu mới" trước khi vào "Bài của em"; sau khi đặt xong vào được | US-S2E1-04-SC4; BR-S2E1-15; QA-S2E1-05 |
| TC-S2E1-044 | Lệnh từ chối chạy trên prod | High | Failure (Error case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Quản trị chạy lệnh chuyển dữ liệu (không có giao diện) | • Môi trường được đặt là `prod` | — | 1. Chạy lệnh chuyển | • Lệnh thoát ngay với thông báo từ chối; không đọc hay ghi dữ liệu | US-S2E1-04-FC2; PO-51 |

## 3. Truy vết tiêu chí sang case

| Tiêu chí | Case |
|---|---|
| US-S2E1-01-SC1 | TC-S2E1-001 |
| US-S2E1-01-SC2 | TC-S2E1-002 |
| US-S2E1-01-SC3 | TC-S2E1-003 |
| US-S2E1-01-SC4 | TC-S2E1-004 |
| US-S2E1-01-SC5 | TC-S2E1-005 |
| US-S2E1-01-SC6 | TC-S2E1-006 |
| US-S2E1-01-FC1 | TC-S2E1-007 |
| US-S2E1-01-BV1 | TC-S2E1-008 |
| US-S2E1-01-BV2 | TC-S2E1-009 |
| US-S2E1-01-BV3 | TC-S2E1-010 |
| US-S2E1-01-BV4 | TC-S2E1-011 |
| US-S2E1-01-BV5 | TC-S2E1-012 |
| US-S2E1-01-BV6 | TC-S2E1-013 |
| US-S2E1-01-FC2 | TC-S2E1-014 |
| US-S2E1-01-FC3 | TC-S2E1-015 |
| US-S2E1-01-BV7 | TC-S2E1-016 |
| US-S2E1-01-FC4 | TC-S2E1-017 |
| US-S2E1-01-FC5 | TC-S2E1-018 |
| US-S2E1-02-SC1 | TC-S2E1-019 |
| US-S2E1-02-SC2 | TC-S2E1-020 |
| US-S2E1-02-SC3 | TC-S2E1-021 |
| US-S2E1-02-FC1 | TC-S2E1-022 |
| US-S2E1-02-FC2 | TC-S2E1-023 |
| US-S2E1-02-BV1 | TC-S2E1-024 |
| US-S2E1-02-BV2 | TC-S2E1-025 |
| US-S2E1-02-BV3 | TC-S2E1-026 |
| US-S2E1-02-BV4 | TC-S2E1-027 |
| US-S2E1-02-FC3 | TC-S2E1-028 |
| US-S2E1-02-FC4 | TC-S2E1-029 |
| US-S2E1-02-FC5 | TC-S2E1-030 |
| US-S2E1-03-SC1 | TC-S2E1-031 |
| US-S2E1-03-SC2 | TC-S2E1-032 |
| US-S2E1-03-FC1 | TC-S2E1-033 |
| US-S2E1-03-BV1 | TC-S2E1-034 |
| US-S2E1-03-FC2 | TC-S2E1-035 |
| US-S2E1-03-BV2 | TC-S2E1-036 |
| US-S2E1-03-SC3 | TC-S2E1-037 |
| US-S2E1-03-FC3 | TC-S2E1-038 |
| US-S2E1-04-SC1 | TC-S2E1-039 |
| US-S2E1-04-SC2 | TC-S2E1-040 |
| US-S2E1-04-SC3 | TC-S2E1-041 |
| US-S2E1-04-FC1 | TC-S2E1-042 |
| US-S2E1-04-SC4 | TC-S2E1-043 |
| US-S2E1-04-FC2 | TC-S2E1-044 |

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
