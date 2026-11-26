Phiên bản: v0.3 · 2026-11-26
# S2-E8 — test case (Bài luyện điền đáp số: bàn phím toán, chấm tương đương, làm bài, kết quả, soạn bài luyện)

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
| bài luyện `PracticeSet` | tập câu hỏi điền đáp số do giáo viên soạn và giao; trạng thái Nháp, Đã giao, Đã đóng | STAGES Ch2 mục 8 |
| câu luyện `PracticeItem` | một câu của bài luyện: đề, kiểu đáp số, sai số, số lượt thử | STAGES Ch2 mục 8 |
| lượt thử `Attempt` | một lần học sinh bấm Kiểm tra; kết quả Đúng, Sai, Sai cú pháp; chỉ Sai mới trừ lượt | STAGES Ch2 mục 8 |
| `grade` | thao tác chấm đáp số của khung môn học: so đáp số nộp với đáp số mẫu theo kiểu và sai số | US-S2E2-01 |
| dạng chuẩn | cách viết đáp số mẫu do giáo viên nhập, hiện cho học sinh khi đáp số của em đúng nhưng viết khác | STAGES Ch2 mục 5 luồng C |
| kiểu đáp số | "chính xác" (so bằng giá trị biểu thức) hoặc "xấp xỉ" (so trong sai số ±tol) (đã chốt PO-66, QA-S2E8-01) | DQ-12 |

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
| US-S2E8-01 Bàn phím toán (căn, phân số, π) trên iPad và iPhone | 14 | 14 | 11 / 3 |
| US-S2E8-02 Chấm đáp số dạng tương đương bằng `grade`; sai cú pháp không trừ lượt | 41 | 42 | 42 / 0 |
| US-S2E8-03 Làm bài luyện: lượt thử, gợi ý mức 1, "xem lại cùng thầy cô" khi hết lượt | 19 | 21 | 20 / 1 |
| US-S2E8-04 Kết quả bài luyện; không hiện số tổng; đáp số khóa tới giờ mở | 13 | 13 | 12 / 1 |
| US-S2E8-05 Soạn bài luyện; nút "Em nghĩ đáp số này đúng" gửi về giáo viên | 25 | 25 | 25 / 0 |
| **Tổng** | **112** | **115** | **110 / 5** |

| Theo ưu tiên | Số case |
|---|---|
| High | 86 |
| Medium | 26 |
| Low | 3 |

## 2. Test case

### US-S2E8-01 — Bàn phím toán (căn, phân số, π) trên iPad và iPhone

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | Status | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S2E8-001 | Phím số chèn chữ số vào ô | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) trên iPad hoặc iPhone | • An ở màn bài luyện trên iPad, ô đáp số trống | — | 1. Chạm 4 rồi 5 | • Ô hiện "45" | US-S2E8-01-SC1; DQ-11 |
| TC-S2E8-002 | Phím căn và phân số chèn đúng ký hiệu | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) trên iPad hoặc iPhone | • Ô trống | — | 1. Chạm 4, √, 3; xóa hết; chạm 1, /, 2 | • Lần 1: ô hiện "4√3"<br>• lần 2: ô hiện "1/2" | US-S2E8-01-SC2; DQ-11; STAGES Ch2 mục 5 luồng C |
| TC-S2E8-003 | Phím π, mũ và độ chèn đúng | Medium | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) trên iPad hoặc iPhone | • Ô trống | — | 1. Chạm 2, π; xóa hết; chạm 2, ^, 3; xóa hết; chạm 4, 5, ° | • Ô lần lượt hiện "2π", "2^3", "45°" | US-S2E8-01-SC3; DQ-11 |
| TC-S2E8-004 | Dấu phẩy nhập số thập phân | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) trên iPad hoặc iPhone | • Ô trống | — | 1. Chạm 0, dấu phẩy, 5 | • Ô hiện "0,5" | US-S2E8-01-SC4; DQ-11 |
| TC-S2E8-005 | Xóa lùi bỏ ký tự ngay trước con trỏ | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) trên iPad hoặc iPhone | • Ô có "4√3" và con trỏ ở cuối | — | 1. Chạm ⌫ hai lần | • Ô hiện "4" | US-S2E8-01-SC5; DQ-11 |
| TC-S2E8-006 | Chèn giữa chuỗi theo vị trí con trỏ | Medium | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) trên iPad hoặc iPhone | • Ô có "45" | — | 1. Chạm giữa hai chữ số để đặt con trỏ; chạm ° | • Ô hiện "4°5" (ký tự chèn đúng vị trí con trỏ; việc chấm xử lý sau) | US-S2E8-01-SC6; BR-S2E8-02 |
| TC-S2E8-007 | Xóa hết làm trống ô | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) trên iPad hoặc iPhone | • Ô có "4√3" | — | 1. Chạm "Xóa hết" | • Ô trống; nút "Kiểm tra" khóa | US-S2E8-01-SC7; DQ-11 |
| TC-S2E8-008 | Ô trống thì nút Kiểm tra khóa | High | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) trên iPad hoặc iPhone | • Ô trống | — | 1. Chạm "Kiểm tra" | • Nút khóa; không gửi yêu cầu | US-S2E8-01-FC1; BR-S2E8-04 |
| TC-S2E8-009 | Đủ 50 ký tự vẫn nhập được | High | Boundary (Edge case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) trên iPad hoặc iPhone | • Ô có 49 ký tự | • 49 rồi 50 ký tự | 1. Chạm thêm 1 chữ số (thành 50) | • Ô có 50 ký tự; chưa có thông báo giới hạn | US-S2E8-01-BV1; BR-S2E8-03; QA-S2E8-03 |
| TC-S2E8-010 | Ký tự thứ 51 không được chèn | High | Boundary (Edge case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) trên iPad hoặc iPhone | • Ô có 50 ký tự | • Ô 50 ký tự | 1. Chạm thêm 1 chữ số | • Ô vẫn 50 ký tự; hiện "Tối đa 50 ký tự" | US-S2E8-01-BV2; BR-S2E8-03; QA-S2E8-03 |
| TC-S2E8-011 | Chạm ô không bật bàn phím hệ thống | High | Failure (Error case) | chức năng | thủ công, có lý do | Manual — cần thiết bị iPad và iPhone thật để biết bàn phím hệ thống có bật không | Not run | Học sinh An (FX-02) trên iPad hoặc iPhone | • iPad và iPhone thật, bài luyện mở | — | 1. Chạm vào ô đáp số | • Bàn phím hệ thống không hiện; bàn phím riêng vẫn ở vị trí; hình không bị đẩy lên | US-S2E8-01-FC2; BR-S2E8-02; DQ-11 |
| TC-S2E8-012 | Chuỗi dán bị lọc theo bảng phím | Medium | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) trên iPad hoặc iPhone | • Ô trống; trong bộ nhớ tạm có "45 độ <b>" | — | 1. Dán vào ô | • Ô chỉ giữ "45" (ký tự ngoài bảng phím bị bỏ); không có hộp thoại lỗi | US-S2E8-01-FC3; BR-S2E8-05 |
| TC-S2E8-013 | iPhone cùng bố cục, nén hàng, không cuộn ngang, phím đủ lớn | High | Normal (Success case) | chức năng | thủ công, có lý do | Manual — cần iPhone thật để đo cỡ chạm và bố cục | Not run | Học sinh An (FX-02) trên iPad hoặc iPhone | • An trên iPhone 390 × 844 | — | 1. Mở bài luyện | • Bàn phím 6 cột cùng thứ tự như iPad; hàng nén; không cuộn ngang; mọi phím cao không dưới 44 điểm; hình và ô đáp số vẫn thấy cùng lúc | US-S2E8-01-SC8; BR-S2E8-01, BR-S2E8-04; DQ-11 |
| TC-S2E8-014 | Giữ nguyên nội dung ô khi xoay iPad | Low | Boundary (Edge case) | chức năng | thủ công, có lý do | Manual — xoay thiết bị cần máy thật | Not run | Học sinh An (FX-02) trên iPad hoặc iPhone | • Ô có "4√3" | — | 1. Xoay iPad dọc sang ngang | • Ô vẫn "4√3"; bàn phím vẽ lại cân đối | US-S2E8-01-SC9; DQ-11 |

### US-S2E8-02 — Chấm đáp số dạng tương đương bằng `grade`; sai cú pháp không trừ lượt

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | Status | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S2E8-015 | Đáp số đúng đúng dạng chuẩn | High | Normal (Success case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Học sinh An (FX-02) | • Câu A, lượt 3/3 | • Đáp số mẫu 4√3; nộp "4√3" | 1. Gửi "4√3" | • Đúng; thông báo "Đúng rồi!" (không dòng dạng chuẩn); lượt vẫn 3/3 | US-S2E8-02-SC1; BR-S2E8-08; STAGES Ch2 mục 5 |
| TC-S2E8-016 | Căn chưa rút gọn vẫn đúng và hiện dạng chuẩn | High | Normal (Success case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Học sinh An (FX-02) | • Câu A | • Nộp √48 | 1. Gửi "√48" | • Đúng; hiện "Đúng rồi! Dạng chuẩn: 4√3"; lượt không trừ | US-S2E8-02-SC2; BR-S2E8-08; STAGES Ch2 mục 5 ngoại lệ |
| TC-S2E8-017 | Phân số chưa rút gọn vẫn đúng | High | Normal (Success case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Học sinh An (FX-02) | • Câu B (đáp số mẫu 1/2) | • Nộp 2/4 | 1. Gửi "2/4" | • Đúng; hiện "Dạng chuẩn: 1/2" | US-S2E8-02-SC3; BR-S2E8-08 |
| TC-S2E8-018 | Thập phân hữu hạn bằng số hữu tỉ | High | Normal (Success case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Học sinh An (FX-02) | • Câu B | • Nộp 0,5 và 0.5 | 1. Gửi "0,5"; gửi "0.5" | • Cả hai Đúng kèm "Dạng chuẩn: 1/2" | US-S2E8-02-SC4; BR-S2E8-08; BR-S2E8-10 |
| TC-S2E8-019 | Số nguyên viết nhiều cách | High | Normal (Success case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Học sinh An (FX-02) | • Câu C (đáp số mẫu 6) | • 12/2, 6,0, 06, 2×3, (1+5) | 1. Gửi "12/2"; gửi "6,0"; gửi "06"; gửi "2×3"; gửi "(1+5)" | • Cả năm Đúng | US-S2E8-02-SC5; BR-S2E8-08 |
| TC-S2E8-020 | Lũy thừa và căn của số chính phương | Medium | Normal (Success case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Học sinh An (FX-02) | • Câu C | • 2^3-2 và √36 | 1. Gửi "2^3-2"; gửi "√36" | • Cả hai Đúng | US-S2E8-02-SC6; BR-S2E8-08; BR-S2E8-10 |
| TC-S2E8-021 | Đáp số có tham số a so khớp theo giá trị | Medium | Normal (Success case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Học sinh An (FX-02) | • Câu E (đáp số mẫu a√2) | • a√2, √2×a, √(2a^2) | 1. Gửi "a√2"; gửi "√2×a"; gửi "√(2a^2)" | • Hai cách đầu Đúng<br>• cách thứ ba (√(2a^2) bằng a√2 chỉ khi a ≥ 0) vẫn Đúng vì so khớp với giá trị a dương thử | US-S2E8-02-SC7; BR-S2E8-08; BR-S2E8-10 |
| TC-S2E8-022 | Kiểu xấp xỉ nhận 45, 45° và 45,0 | High | Normal (Success case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Học sinh An (FX-02) | • Câu D (xấp xỉ 45, ±0,5, °) | • 45, 45°, 45,0, 90/2 | 1. Gửi "45"; gửi "45°"; gửi "45,0"; gửi "90/2" | • Cả bốn Đúng; với "90/2" hiện thêm "Dạng chuẩn: 45°" | US-S2E8-02-SC8; BR-S2E8-09; DQ-12 |
| TC-S2E8-023 | Sát biên trên 45,5 là Đúng | High | Boundary (Edge case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Học sinh An (FX-02) | • Câu D (tol 0,5) | • 45,5 | 1. Gửi "45,5" | • Đúng (biên bao gồm) | US-S2E8-02-BV1; BR-S2E8-09; DQ-12 |
| TC-S2E8-024 | Vượt biên trên 45,51 là Sai | High | Boundary (Edge case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Học sinh An (FX-02) | • Câu D | • 45,51 | 1. Gửi "45,51" | • Sai; trừ một lượt | US-S2E8-02-BV2; BR-S2E8-09; DQ-12 |
| TC-S2E8-025 | Sát biên dưới 44,5 là Đúng | High | Boundary (Edge case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Học sinh An (FX-02) | • Câu D | • 44,5 | 1. Gửi "44,5" | • Đúng | US-S2E8-02-BV3; BR-S2E8-09 |
| TC-S2E8-026 | Vượt biên dưới 44,49 là Sai | High | Boundary (Edge case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Học sinh An (FX-02) | • Câu D | • 44,49 | 1. Gửi "44,49" | • Sai; trừ một lượt | US-S2E8-02-BV4; BR-S2E8-09 |
| TC-S2E8-027 | Biên không bị sai số dấu phẩy động: 45,50 và 44,50 | High | Boundary (Edge case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Học sinh An (FX-02) | • Câu D | • 45,50 / 44,50 / 45,500000000001 | 1. Gửi "45,50"; gửi "44,50"; gửi "45,500000000001" | • 45,50: Đúng<br>• 44,50: Đúng<br>• 45,500000000001: Sai (tính bằng số thập phân chính xác, không dùng số dấu phẩy động nhị phân) | US-S2E8-02-BV5; BR-S2E8-09 |
| TC-S2E8-028 | Sai số 0 cho kiểu xấp xỉ nghĩa là bằng đúng | Medium | Boundary (Edge case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Học sinh An (FX-02) | • Câu D đổi sai số thành 0 | — | 1. Gửi "45"; gửi "45,001" | • 45: Đúng<br>• 45,001: Sai | US-S2E8-02-BV6; BR-S2E8-09; QA-S2E8-05 |
| TC-S2E8-029 | Giá trị sai trừ một lượt và không lộ hướng | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Học sinh An (FX-02) | • Câu D, lượt 3/3 | • Nộp 60° | 1. Gửi "60°" | • Sai; lượt còn 2/3; hiện "Chưa đúng. Còn 2 lượt."; phản hồi không chứa đáp số, không chứa "lớn hơn" hay "nhỏ hơn" | US-S2E8-02-FC1; BR-S2E8-07; BR-S2E8-12; DQ-12 |
| TC-S2E8-030 | Thập phân làm tròn của số vô tỉ là Sai ở kiểu chính xác | High | Failure (Error case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Học sinh An (FX-02) | • Câu A (đáp số mẫu 4√3) | • Nộp 6,93 | 1. Gửi "6,93" | • Sai; trừ một lượt; gợi ý chữ "Em hãy viết đáp số ở dạng chính xác (có căn)" | US-S2E8-02-FC2; BR-S2E8-08; QA-S2E8-02 |
| TC-S2E8-031 | Ô trống hoặc chỉ khoảng trắng là Sai cú pháp, không trừ lượt | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Học sinh An (FX-02) | • Câu D, lượt 3/3 | • "" và "   " | 1. Gọi chấm với "" và với "   " | • Sai cú pháp "Em hãy nhập đáp số."; lượt vẫn 3/3 | US-S2E8-02-FC3; BR-S2E8-07, BR-S2E8-11 |
| TC-S2E8-032 | Căn thiếu số | High | Failure (Error case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Học sinh An (FX-02) | • Câu A | • Nộp √ | 1. Gửi "√" | • Sai cú pháp báo "Thiếu số sau dấu căn (ký tự 1)"; lượt không đổi | US-S2E8-02-FC4; BR-S2E8-11; STAGES Ch2 mục 5 |
| TC-S2E8-033 | Ngoặc không cân | High | Failure (Error case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Học sinh An (FX-02) | • Câu C | • ((1+2) và 1+2) | 1. Gửi "((1+2)"; gửi "1+2)" | • Cả hai Sai cú pháp: "Thiếu dấu )" và "Thừa dấu )" kèm vị trí; lượt không đổi | US-S2E8-02-FC5; BR-S2E8-11 |
| TC-S2E8-034 | Chia cho 0 | High | Failure (Error case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Học sinh An (FX-02) | • Câu C | • 1/0 và 5÷(2-2) | 1. Gửi "1/0"; gửi "5÷(2-2)" | • Cả hai Sai cú pháp "Chia cho 0 không có nghĩa"; không trừ lượt | US-S2E8-02-FC6; BR-S2E8-11 |
| TC-S2E8-035 | Chữ lạ | High | Failure (Error case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Học sinh An (FX-02) | • Câu C | • abc và 45 độ | 1. Gửi "abc"; gửi "45 độ" | • Cả hai Sai cú pháp "Chữ chưa dùng được"; không trừ lượt (chỉ nhận chữ a và x) | US-S2E8-02-FC7; BR-S2E8-10, BR-S2E8-11 |
| TC-S2E8-036 | Hai số liền nhau | Medium | Failure (Error case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Học sinh An (FX-02) | • Câu C | • 5 5 và 4 √3 | 1. Gửi "5 5"; gửi "4 √3" | • "5 5": Sai cú pháp "Hai số liền nhau, thiếu phép tính"<br>• "4 √3": coi dấu cách là bỏ qua, chấm như "4√3" | US-S2E8-02-FC8; BR-S2E8-11 |
| TC-S2E8-037 | Hai dấu phẩy trong một số | Medium | Failure (Error case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Học sinh An (FX-02) | • Câu B | • 1,2,3 và 3..5 | 1. Gửi "1,2,3"; gửi "3..5" | • Cả hai Sai cú pháp "Số có hai dấu thập phân"; không trừ lượt | US-S2E8-02-FC9; BR-S2E8-11 |
| TC-S2E8-038 | Dấu cộng, nhân liền nhau | Medium | Failure (Error case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Học sinh An (FX-02) | • Câu C | • ++3, 2××3, --3 | 1. Gửi "++3"; gửi "2××3"; gửi "--3" | • Cả ba Sai cú pháp báo thiếu toán hạng hoặc phép tính thừa; không trừ lượt | US-S2E8-02-FC10; BR-S2E8-11 |
| TC-S2E8-039 | Dấu âm đứng đầu hợp lệ | Medium | Normal (Success case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Học sinh An (FX-02) | • Câu có đáp số mẫu -3 | • -3 và 0-3 | 1. Gửi "-3"; gửi "0-3" | • Cả hai Đúng | US-S2E8-02-SC9; BR-S2E8-10 |
| TC-S2E8-040 | Đáp số dài đúng 50 ký tự được chấm | Medium | Boundary (Edge case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Học sinh An (FX-02) | • Câu C | • Biểu thức 50 ký tự | 1. Gửi biểu thức hợp lệ dài 50 ký tự có giá trị 6 (ví dụ nhiều dấu +0) | • Chấm bình thường: Đúng | US-S2E8-02-BV7; BR-S2E8-11 |
| TC-S2E8-041 | Đáp số 51 ký tự là Sai cú pháp | High | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Học sinh An (FX-02) | • Câu C | • Biểu thức 51 ký tự | 1. Gọi API chấm với biểu thức 51 ký tự | • Sai cú pháp "Đáp số tối đa 50 ký tự"; không trừ lượt; không chấm giá trị | US-S2E8-02-BV8; BR-S2E8-11 |
| TC-S2E8-042 | Số 15 chữ số nhận, 16 chữ số là Sai cú pháp | Medium | Boundary (Edge case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Học sinh An (FX-02) | • Câu C | • 15 và 16 chữ số | 1. Gửi số 15 chữ số "100000000000000"; gửi số 16 chữ số "1000000000000000" | • Số 15 chữ số: được chấm (Sai giá trị, trừ lượt)<br>• số 16 chữ số: Sai cú pháp "Số quá lớn", không trừ lượt | US-S2E8-02-BV9; BR-S2E8-10; QA-S2E8-04 |
| TC-S2E8-043 | Dấu chấm là dấu thập phân, không phải ngăn nghìn | High | Boundary (Edge case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Học sinh An (FX-02) | • Câu C (đáp số mẫu 6); câu F (đáp số mẫu 1) | • 1.000 và 6.0 | 1. Gửi "1.000" cho câu F; gửi "6.0" cho câu C | • "1.000" bằng 1: Đúng<br>• "6.0": Đúng | US-S2E8-02-BV10; BR-S2E8-10; QA-S2E8-06 |
| TC-S2E8-044 | Lượt thử đếm đúng khi Sai lần lượt 3 lần | High | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Học sinh An (FX-02) | • Câu D, 3 lượt | — | 1. Gửi "60", rồi "61", rồi "62" (ba lần Sai) | • Sau lần 1: còn 2<br>• sau lần 2: còn 1<br>• sau lần 3: còn 0, câu hết lượt "xem lại cùng thầy cô" | US-S2E8-02-BV11; BR-S2E8-07; STAGES Ch2 mục 5 |
| TC-S2E8-045 | Đúng ở lượt cuối vẫn đúng | High | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Học sinh An (FX-02) | • Câu D, đã Sai 2 lần (còn 1 lượt) | — | 1. Gửi "45" | • Đúng; câu đúng (không "xem lại cùng thầy cô"); lượt đã dùng ghi 3/3 | US-S2E8-02-BV12; BR-S2E8-07 |
| TC-S2E8-046 | Sai cú pháp ở lượt cuối không trừ và không hết lượt | High | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Học sinh An (FX-02) | • Câu D, đã Sai 2 lần (còn 1 lượt) | — | 1. Gửi "√"; gửi "45" | • "√": Sai cú pháp, vẫn còn 1 lượt<br>• "45": Đúng | US-S2E8-02-BV13; BR-S2E8-07 |
| TC-S2E8-047 | Hết lượt rồi gửi tiếp thì CONFLICT và không chấm | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Học sinh An (FX-02) | • Câu D đã hết lượt (3 lần Sai) | — | 1. Gửi "45" (đáp số đúng) | • CONFLICT; không chấm; không lộ đúng sai; kết quả câu vẫn "xem lại cùng thầy cô" | US-S2E8-02-FC11; BR-S2E8-13 |
| TC-S2E8-048 | Đã Đúng rồi gửi tiếp thì CONFLICT | Medium | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Học sinh An (FX-02) | • Câu D đã Đúng | — | 1. Gửi "46" | • CONFLICT "Câu này đã đúng"; kết quả không đổi | US-S2E8-02-FC12; BR-S2E8-13 |
| TC-S2E8-049 | Bấm đôi chỉ trừ một lượt | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Học sinh An (FX-02) | • Câu D lượt 3/3; hai yêu cầu cùng mã chống lặp | — | 1. Gửi "60" hai lần trong 1 giây với cùng mã | • Chỉ một Attempt; lượt còn 2/3; hai phản hồi giống hệt | US-S2E8-02-FC13; BR-S2E8-14 |
| TC-S2E8-050 | Mạng chập chờn gửi lại cùng mã không trừ hai lần | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Học sinh An (FX-02) | • Yêu cầu "60" đã xử lý nhưng phản hồi không về tới thiết bị | — | 1. Thiết bị gửi lại cùng mã | • Máy chủ trả đúng phản hồi lần đầu; lượt còn 2/3 (không phải 1/3) | US-S2E8-02-FC14; BR-S2E8-14 |
| TC-S2E8-051 | Đáp số mẫu không có trong dữ liệu trả về của bài luyện | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Học sinh An (FX-02) | • An mở BL-1 câu 2; trước giờ mở lời giải | • FX-20 | 1. Xem toàn bộ dữ liệu trả về khi mở bài, sau mỗi lần Sai và sau khi hết lượt | • Không có 45 làm đáp số, không có sai số, không có bước giải ở mọi dạng viết (45, 45°, 90/2, "bốn mươi lăm"); tiêu đề `Cache-Control: no-store` | US-S2E8-02-FC15; BR-S2E8-12; STAGES mục 0.2 nguyên tắc 1 |
| TC-S2E8-052 | Ba lần Sai liên tiếp không để lộ hướng | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Học sinh An (FX-02) | • Câu D | • 40, 50, 46 | 1. Gửi "40", "50", "46" (ba lần Sai) | • Ba phản hồi chỉ nói chưa đúng và số lượt còn; không nói lớn hơn hay nhỏ hơn; không có độ lệch | US-S2E8-02-FC16; BR-S2E8-12 |
| TC-S2E8-053 | Gọi chấm không đăng nhập | Medium | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Học sinh An (FX-02) | • Chưa đăng nhập | — | 1. Gọi chấm câu D | • UNAUTHENTICATED; không Attempt | US-S2E8-02-FC17; D-GEO-143 |
| TC-S2E8-054 | Sai cú pháp báo đúng vị trí ký tự | High | Normal (Success case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Học sinh An (FX-02) | • Câu C | • Nộp 2+*3 | 1. Gửi "2+*3" | • Báo "Thừa dấu * (ký tự 3)"; vị trí ký tự tính từ 1 | US-S2E8-02-SC10; BR-S2E8-07; STAGES Ch2 mục 5 |
| TC-S2E8-055 | Thử đáp số bằng cách thay đổi sai số hoặc kiểu từ thiết bị | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Học sinh An (FX-02) | • Câu D | — | 1. Gọi chấm với thân yêu cầu có thêm trường `tol` = 100 và `type` = "approx" | • Máy chủ từ chối VALIDATION_FAILED, không tốn lượt (chặng 3: BR-S3E5-34 · PO-61 thay "bỏ qua" → VALIDATION_FAILED, không ghi gì; FIX runner-fix-s3f3 2026-11-27)<br/>• Lần nộp không có trường thừa: kết quả theo cấu hình của câu | US-S2E8-02-FC18; BR-S2E8-12 |
| TC-S2E8-111 | Bảng dữ liệu tương đương: 20 dòng đúng và 20 dòng sai trên cùng bộ đáp số mẫu | High | Normal (Success case) | chức năng | đơn vị (hàm thuần) | UT | Not run | Kiểm thử đơn vị (hàm chấm) | • Bộ đáp số mẫu: 4√3, 1/2, 6, 45 (±0,5), a√2, -3<br>• chưa chạy bảng | • Bảng 40 dòng: mỗi dòng gồm đáp số mẫu, đáp số nộp, kết quả mong đợi | 1. Chạy hàm chấm lần lượt trên 40 dòng | • 40/40 dòng cho đúng kết quả mong đợi (Đúng kèm dạng chuẩn, Sai, Sai cú pháp); không dòng nào ném lỗi; thời gian chấm mỗi dòng dưới 50 ms (đã chốt PO-66) | US-S2E8-02 (case viết tay); AC US-S2E8-02 theo bảng (bổ sung case hồi quy cho bộ câu thử dạng viết) |

### US-S2E8-03 — Làm bài luyện: lượt thử, gợi ý mức 1, "xem lại cùng thầy cô" khi hết lượt

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | Status | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S2E8-056 | Mở bài hiện câu 1 với đủ thành phần | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) trên iPad | • An mở BL-1 lần đầu trên iPad | • FX-20 | 1. Mở bài | • Hiện "Câu 1/5", chip "Còn 3/3 lượt", thanh tiến độ 5 đoạn (đoạn 1 vàng, 4 đoạn xám), hình, ô đáp số trống, bàn phím toán | US-S2E8-03-SC1; BR-S2E8-15; DQ-12 |
| TC-S2E8-057 | Đáp số đúng thì xác nhận và sang câu kế | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) trên iPad | • An ở câu 2 (đáp số 45 ±0,5°) | — | 1. Gõ 45; bấm "Kiểm tra"; bấm "Câu kế" | • Hiện "Đúng rồi!"; đoạn 2 của thanh tiến độ thành xanh; sau "Câu kế" mở "Câu 3/5" với chip "Còn 3/3 lượt" | US-S2E8-03-SC2; BR-S2E8-16 |
| TC-S2E8-058 | Đáp số sai hiện gợi ý mức 1 và trừ lượt | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) trên iPad | • An ở câu 2, lượt 3/3 | — | 1. Gõ 60°; bấm "Kiểm tra" | • Hiện "Chưa đúng. Còn 2 lượt." và gợi ý mức 1 (hình chiếu của SC); chip "Còn 2/3 lượt"; không đáp số; ô đáp số viền đỏ; giữ "60°" trong ô | US-S2E8-03-SC3; BR-S2E8-16; DQ-12 |
| TC-S2E8-059 | Sai cú pháp báo chỗ sai và không trừ lượt | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) trên iPad | • An ở câu 2, lượt 2/3 | — | 1. Gõ "√"; bấm "Kiểm tra" | • Hiện "Đáp số chưa viết đúng: thiếu số sau dấu căn (ký tự 1). Lượt thử chưa bị trừ."; chip vẫn "Còn 2/3 lượt"; không gợi ý mới | US-S2E8-03-SC4; BR-S2E8-16; STAGES Ch2 mục 5 |
| TC-S2E8-060 | Hết lượt đánh dấu xem lại cùng thầy cô và khóa nhập | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) trên iPad | • An ở câu 2 đã Sai 2 lần (còn 1 lượt) | • Ca ví dụ c2-06-het-luot.png | 1. Gõ 70; bấm "Kiểm tra" | • Hiện "Chưa đúng. Em xem lại cùng thầy cô nhé."; chip "Còn 0/3 lượt"; câu đánh dấu "xem lại cùng thầy cô"; ô đáp số và "Kiểm tra" khóa; nút "Câu kế" mở; lời giải vẫn khóa | US-S2E8-03-SC5; BR-S2E8-17; DQ-12 |
| TC-S2E8-061 | Hết lượt vẫn xem được gợi ý mức 1 | Medium | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) trên iPad | • Câu 2 đã hết lượt | — | 1. Bấm "Xem gợi ý" | • Gợi ý mức 1 hiện lại; không đáp số | US-S2E8-03-SC6; BR-S2E8-17; DQ-12 |
| TC-S2E8-062 | Chip lượt đổi 3, 2, 1, 0 theo từng lần Sai | High | Boundary (Edge case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) trên iPad | • Câu 2, 3 lượt | — | 1. Sai ba lần (60, 61, 62) | • Chip lần lượt "Còn 2/3", "Còn 1/3", "Còn 0/3"; gợi ý hiện ở lần Sai thứ nhất và thứ hai, câu "xem lại cùng thầy cô" ở lần thứ ba | US-S2E8-03-BV1; BR-S2E8-16; BR-S2E8-17 |
| TC-S2E8-063 | Thoát giữa chừng rồi vào lại giữ trạng thái | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) trên iPad | • An ở câu 2 đã Sai một lần (còn 2 lượt) | — | 1. Đóng trình duyệt; mở lại và đăng nhập; mở BL-1 | • Vào đúng "Câu 2/5", chip "Còn 2/3 lượt"; câu 1 đã xanh; gợi ý đã hiện vẫn hiện | US-S2E8-03-SC7; BR-S2E8-18 |
| TC-S2E8-064 | Mất mạng khi gửi không trừ lượt và giữ đáp số | High | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) trên iPad | • An ở câu 2 lượt 3/3, đã gõ "60"; ngắt mạng | — | 1. Bấm "Kiểm tra"; bật mạng; bấm "Thử lại" | • Hiện "Chưa gửi được, đáp số của em vẫn còn" và nút "Thử lại"; ô giữ "60"; lượt vẫn 3/3 trước khi gửi lại; sau "Thử lại" trừ đúng một lượt (còn 2/3) | US-S2E8-03-FC1; BR-S2E8-20 |
| TC-S2E8-065 | Đang tải hiện khung giữ chỗ | Medium | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) trên iPad | • Mạng chậm | — | 1. Mở BL-1 | • Hiện khung giữ chỗ cho hình và bàn phím; ô đáp số chưa nhận chữ khi chưa tải xong; không hiện hình rỗng | US-S2E8-03-FC2; BR-S2E8-20 |
| TC-S2E8-066 | Bài của lớp khác không mở được | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Học sinh An (FX-02) trên iPad | • Học sinh Bình thuộc lớp 11A2 không được giao BL-1 | — | 1. Bình gọi mở BL-1 | • NOT_FOUND; không câu hỏi nào trả về | US-S2E8-03-FC3; BR-S2E8-19 |
| TC-S2E8-067 | Chưa đăng nhập không mở được bài | Medium | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Học sinh An (FX-02) trên iPad | • Chưa đăng nhập | — | 1. Gọi mở BL-1 | • UNAUTHENTICATED; không dữ liệu bài | US-S2E8-03-FC4; D-GEO-143 |
| TC-S2E8-068 | Câu đã đúng không làm lại | Medium | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) trên iPad | • Câu 1 đã đúng | — | 1. Quay lại câu 1; gõ đáp số khác | • Ô khóa; hiện trạng thái đúng của câu 1 (không đổi) | US-S2E8-03-FC5; BR-S2E8-18 |
| TC-S2E8-069 | Bài đã đóng chỉ xem kết quả | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Học sinh An (FX-02) trên iPad | • BL-1 ở trạng thái Đã đóng | — | 1. An mở BL-1; thử nhập đáp số | • Không nhập được; gọi chấm nhận CONFLICT; chuyển tới màn kết quả | US-S2E8-03-FC6; BR-S2E8-19 |
| TC-S2E8-070 | Làm hết 5 câu thì sang kết quả | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) trên iPad | • An ở câu 5/5 vừa xong | — | 1. Bấm "Xem kết quả" | • Mở màn kết quả bài luyện (US-S2E8-04) | US-S2E8-03-SC8; BR-S2E8-15; STAGES Ch2 mục 6 (C2-07) |
| TC-S2E8-071 | Bài 1 câu: tiến độ một đoạn, câu cuối | Low | Boundary (Edge case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) trên iPad | • Bài luyện 1 câu | — | 1. Mở bài; làm xong | • "Câu 1/1"; thanh một đoạn; nút ở cuối ghi "Xem kết quả" | US-S2E8-03-BV2; BR-S2E8-15 |
| TC-S2E8-072 | Bài 20 câu thanh tiến độ 20 đoạn | Medium | Boundary (Edge case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) trên iPad | • Bài luyện 20 câu (tối đa của S2E8-05) | — | 1. Mở bài trên iPhone | • Thanh tiến độ có 20 đoạn, không tràn ngang; "Câu 1/20" | US-S2E8-03-BV3; BR-S2E8-15 |
| TC-S2E8-073 | Gợi ý mức 1 không nêu đáp số ở mọi dạng viết | High | Normal (Success case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Học sinh An (FX-02) trên iPad | • Câu 2 sau khi Sai | — | 1. Đọc gợi ý và dữ liệu trả về | • Gợi ý không chứa 45, 45°, 90/2, "bốn mươi lăm", "tan = 1" hay bước giải | US-S2E8-03-SC9; BR-S2E8-16; STAGES mục 0.2 nguyên tắc 1 |
| TC-S2E8-074 | Bố cục iPhone dùng được | High | Normal (Success case) | chức năng | thủ công, có lý do | Manual — cần iPhone thật xét bố cục | Not run | Học sinh An (FX-02) trên iPad | • An trên iPhone 390 × 844, câu 2 | — | 1. Làm một lượt Sai | • Hình, ô, chip lượt, gợi ý, bàn phím cùng dùng được; không cuộn ngang; gợi ý có thể cuộn trong khung | US-S2E8-03-SC10; BR-S2E8-15 |
| TC-S2E8-112 | Luồng C đầy đủ trên iPad: làm 5 câu gồm đúng, sai, sai cú pháp, hết lượt | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) | • An được giao BL-1 (FX-20); lời giải khóa<br>• iPad | • BL-1 | 1. Câu 1: gõ đáp số đúng<br>2. câu 2: sai cú pháp, rồi sai hai lần, rồi sai lần ba<br>3. câu 3: gõ đáp số khác dạng viết<br>4. câu 4 và 5: đúng<br>5. bấm "Xem kết quả" | • Câu 1 đúng; câu 2 sau lần sai cú pháp còn 3/3 lượt, sau ba lần sai hết lượt "xem lại cùng thầy cô"; câu 3 đúng kèm dạng chuẩn; kết quả "4 câu đúng · 1 câu cần xem lại"; không điểm; lời giải khóa | US-S2E8-03 (case viết tay); chuỗi US-S2E8-01…04 (luồng C của STAGES) |
| TC-S2E8-113 | Bộ câu thử moi đáp số cho bài luyện: hỏi đáp, gợi ý lặp, thử nhị phân bằng đáp số | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Học sinh An (FX-02) | • BL-1 chưa tới `release_at`; An hết lượt câu 2 | • Bộ câu thử moi đáp số MVP mở rộng cho bài luyện | 1. Chạy bộ câu thử: gửi 3 đáp số dò (lớn, nhỏ, gần), lặp gợi ý, đổi cách hỏi, gọi lại sau khi hết lượt | • Không câu thử nào lấy được đáp số; sau khi hết lượt mọi gửi tiếp nhận CONFLICT không chấm; không chênh lệch thông tin giữa các lần Sai | US-S2E8-03 (case viết tay); BRD delta mục 4 (bộ câu thử moi đáp án chạy lại cho S2-E8) |

### US-S2E8-04 — Kết quả bài luyện; không hiện số tổng; đáp số khóa tới giờ mở

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | Status | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S2E8-075 | Làm hết thì hiện số câu đúng và câu cần xem lại | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) trên iPad | • An làm xong BL-1: 3 câu đúng, 2 câu hết lượt | — | 1. Mở màn kết quả | • Hiện "3 câu đúng · 2 câu cần xem lại"; mỗi câu một dòng trạng thái | US-S2E8-04-SC1; BR-S2E8-21; DQ-13 |
| TC-S2E8-076 | Không có điểm, phần trăm hay xếp hạng | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) trên iPad | • Màn kết quả của SC1 | — | 1. Quét chữ trên màn và dữ liệu trả về tìm "điểm", "%", "/5", "hạng", "xếp hạng" | • Không có chuỗi nào trong số đó<br>• không có số tổng kiểu "3/5"; chỉ có hai số: câu đúng và câu cần xem lại | US-S2E8-04-SC2; BR-S2E8-21; PO-65 Q6 |
| TC-S2E8-077 | Chưa làm xong thì báo còn bao nhiêu câu | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) trên iPad | • An mới làm 3 câu (2 đúng, 1 hết lượt); còn 2 câu chưa làm | — | 1. Mở màn kết quả | • Hiện "Chưa làm xong: còn 2 câu"; hai số "2 câu đúng · 1 câu cần xem lại" không gồm câu chưa làm | US-S2E8-04-SC3; BR-S2E8-22 |
| TC-S2E8-078 | Lời giải còn khóa thì ghi giờ mở và không có trong dữ liệu | High | Normal (Success case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Học sinh An (FX-02) trên iPad | • `release_at` = T0 + 2 giờ; giờ máy chủ T0; An có câu hết lượt | • FX-20 | 1. Mở màn kết quả; xem dữ liệu trả về | • Câu hết lượt hiện "Lời giải mở lúc HH:mm, dd/MM/yyyy" (giờ T0 + 2 giờ); không đáp số và không bước giải trong dữ liệu trả về; `Cache-Control: no-store` | US-S2E8-04-SC4; BR-S2E8-23, BR-S2E8-24 |
| TC-S2E8-079 | Tới giờ mở thì xem được lời giải | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) trên iPad | • Giờ máy chủ ≥ `release_at` | — | 1. Mở lại màn kết quả; bấm xem lời giải câu 2 | • Lời giải hiện đủ bước kèm nhãn hai trục; trạng thái "Lời giải đã mở" | US-S2E8-04-SC5; BR-S2E8-23 |
| TC-S2E8-080 | Một giây trước giờ mở còn khóa, đúng giờ mở thì mở | High | Boundary (Edge case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Học sinh An (FX-02) trên iPad | • `release_at` = R; đồng hồ giả | — | 1. Gọi lấy lời giải câu 2 ở R - 1 giây; ở R | • R - 1 giây: khóa, không dữ liệu lời giải<br>• R: mở | US-S2E8-04-BV1; BR-S2E8-23; M-GEO-13 |
| TC-S2E8-081 | Chưa đặt giờ mở thì ghi Thầy cô chưa đặt giờ mở | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) trên iPad | • Bài luyện chưa đặt `release_at` | — | 1. Mở màn kết quả | • Câu hết lượt hiện "Thầy cô chưa đặt giờ mở"; lời giải khóa mãi tới khi có giờ | US-S2E8-04-SC6; BR-S2E8-23 |
| TC-S2E8-082 | Học sinh khác không xem kết quả bạn | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Học sinh An (FX-02) trên iPad | • Kết quả của An; Bình cùng lớp | — | 1. Bình gọi kết quả của An | • NOT_FOUND; không dữ liệu của An | US-S2E8-04-FC1; BR-S2E8-25 |
| TC-S2E8-083 | Chưa làm câu nào | Medium | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) trên iPad | • An mở bài rồi thoát, chưa làm câu nào | — | 1. Mở màn kết quả | • Hiện "Chưa làm xong: còn 5 câu"; "0 câu đúng · 0 câu cần xem lại" hoặc dòng "Em chưa làm câu nào" | US-S2E8-04-FC2; BR-S2E8-22 |
| TC-S2E8-084 | Làm đúng cả 5 câu | Medium | Boundary (Edge case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) trên iPad | • An làm đúng cả 5 câu | — | 1. Mở màn kết quả | • Hiện "5 câu đúng · 0 câu cần xem lại"; không câu chúc điểm số, không xếp hạng | US-S2E8-04-BV2; BR-S2E8-21 |
| TC-S2E8-085 | Không câu nào đúng | Medium | Boundary (Edge case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Học sinh An (FX-02) trên iPad | • An hết lượt cả 5 câu | — | 1. Mở màn kết quả | • Hiện "0 câu đúng · 5 câu cần xem lại"; giọng nhẹ, không chữ chê; có dòng "Em hỏi thầy cô nhé" (đã chốt PO-66, DQ-13) | US-S2E8-04-BV3; BR-S2E8-21 |
| TC-S2E8-086 | Lời giải không nằm trong bộ nhớ trình duyệt trước giờ mở | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Học sinh An (FX-02) trên iPad | • Trước `release_at`; An đã mở kết quả | — | 1. Kiểm bộ nhớ trình duyệt (cache, lưu trữ cục bộ) | • Không có nội dung lời giải hay đáp số của BL-1 | US-S2E8-04-FC3; BR-S2E8-24 |
| TC-S2E8-087 | Bố cục iPad của kết quả | Low | Normal (Success case) | chức năng | thủ công, có lý do | Manual — cần mắt người so với ảnh mẫu | Not run | Học sinh An (FX-02) trên iPad | • An mở kết quả trên iPad (ảnh `c2-07-ket-qua.png`) | — | 1. Xem màn | • Danh sách câu bố cục như ảnh mẫu; nhãn trạng thái đọc rõ; không cuộn ngang | US-S2E8-04-SC7; DQ-13; BR-S2E8-23 |

### US-S2E8-05 — Soạn bài luyện; nút "Em nghĩ đáp số này đúng" gửi về giáo viên

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | Status | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S2E8-088 | Chọn đề có đáp số và tạo bài nháp | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • Cô Trang có 5 đề đã duyệt có đáp số máy chấm được | — | 1. Mở "Soạn bài luyện"; tick 5 đề; đặt tên "Góc trong hình chóp"; bấm "Lưu nháp" | • Tạo bài nháp 5 câu; mỗi câu mặc định 3 lượt, giờ mở chưa đặt; trạng thái Nháp | US-S2E8-05-SC1; BR-S2E8-26 |
| TC-S2E8-089 | Đề chưa có đáp số máy chấm hiện nhưng không chọn được | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • Có 1 đề chưa có đáp số máy chấm được | — | 1. Mở danh sách đề | • Đề viền nét đứt, chip vàng; không tick được; chỉ dẫn "Thêm đáp số ở Soát lời giải" | US-S2E8-05-SC2; BR-S2E8-27; DQ-14 |
| TC-S2E8-090 | Đặt kiểu xấp xỉ, sai số, lượt và giờ mở | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • Bài nháp câu 2 | — | 1. Chọn "xấp xỉ"; sai số 0,5; lượt 3; giờ mở T0 + 2 giờ; bấm "Lưu nháp" | • Câu 2 lưu kiểu xấp xỉ, ±0,5, 3 lượt, giờ mở T0 + 2 giờ | US-S2E8-05-SC3; BR-S2E8-26 |
| TC-S2E8-091 | Xem trước như học sinh không ghi gì | High | Normal (Success case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • Bài nháp 5 câu | — | 1. Bấm "Xem trước như học sinh"; làm sai câu 2 hai lần; thoát | • Hiện màn làm bài giống học sinh; chip lượt đổi trong phiên xem trước; không có `Attempt` thật; mở lại xem trước thì lượt về đủ | US-S2E8-05-SC4; BR-S2E8-28 |
| TC-S2E8-092 | Giao bài chụp lại cấu hình từng câu | High | Normal (Success case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • Bài nháp đầy đủ; lớp 11A1 | — | 1. Bấm "Giao"; xác nhận | • Bài thành Đã giao; mỗi câu lưu snapshot đáp số, kiểu, sai số, lượt; học sinh 11A1 thấy bài | US-S2E8-05-SC5; BR-S2E8-29 |
| TC-S2E8-093 | Bài đã giao không sửa được | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • Bài đã giao | — | 1. Gọi API đổi sai số của câu 2 | • CONFLICT "Bài đã giao không sửa được"; snapshot không đổi | US-S2E8-05-FC1; BR-S2E8-29 |
| TC-S2E8-094 | Sửa đáp số của đề gốc không đổi snapshot bài đã giao | High | Failure (Error case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • Bài đã giao câu 2 snapshot 45; giáo viên sửa đề gốc thành đáp số 60 | — | 1. An gửi "45" | • Chấm theo snapshot 45: Đúng | US-S2E8-05-FC2; BR-S2E8-29 |
| TC-S2E8-095 | Tên bài 80 ký tự nhận, 81 bị chặn, rỗng bị chặn | Medium | Boundary (Edge case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • Soạn bài | • Tên 0, 80, 81 ký tự | 1. Lưu với tên 80 ký tự; tên 81 ký tự; tên rỗng | • 80: lưu được<br>• 81: lỗi "Tên tối đa 80 ký tự"<br>• rỗng: lỗi "Hãy đặt tên bài" | US-S2E8-05-BV1; BR-S2E8-26 |
| TC-S2E8-096 | Số câu 1 và 20 nhận, 0 và 21 bị chặn | High | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • Soạn bài | — | 1. Lưu bài 1 câu; 20 câu; 0 câu; 21 câu | • 1 và 20: lưu được<br>• 0: lỗi "Chọn ít nhất 1 câu"<br>• 21: lỗi "Tối đa 20 câu" | US-S2E8-05-BV2; BR-S2E8-26 |
| TC-S2E8-097 | Số lượt 1 và 5 nhận, 0 và 6 bị chặn | High | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • Câu của bài nháp | — | 1. Đặt lượt 1; 5; 0; 6 | • 1 và 5: lưu được<br>• 0 và 6: VALIDATION_FAILED "Số lượt từ 1 đến 5" | US-S2E8-05-BV3; BR-S2E8-26 |
| TC-S2E8-098 | Sai số 0 và 10 nhận, âm và 10,01 bị chặn | High | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • Câu kiểu xấp xỉ | — | 1. Đặt sai số 0; 10; -0,1; 10,01; 0,555 | • 0 và 10: lưu được<br>• -0,1, 10,01: VALIDATION_FAILED<br>• 0,555 (ba chữ số thập phân): VALIDATION_FAILED "Tối đa 2 chữ số thập phân" | US-S2E8-05-BV4; BR-S2E8-26; QA-S2E8-05 |
| TC-S2E8-099 | Sai số không áp dụng cho kiểu chính xác | Medium | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • Câu kiểu chính xác | — | 1. Thử nhập sai số 0,5 qua API | • Trường sai số bị khóa trên giao diện; API bỏ qua hoặc từ chối; kết quả chấm không đổi theo | US-S2E8-05-FC3; BR-S2E8-26 |
| TC-S2E8-100 | Giờ mở đã qua cần xác nhận | High | Failure (Error case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • Giờ máy chủ T0 | — | 1. Đặt giờ mở T0 - 1 giờ; bấm "Giao" | • Hiện cảnh báo "Giờ mở đã qua, lời giải mở ngay cho học sinh. Vẫn giao?"; chỉ khi xác nhận mới giao | US-S2E8-05-FC4; BR-S2E8-26 |
| TC-S2E8-101 | Học sinh gửi báo Em nghĩ đáp số này đúng sau khi bị chấm Sai | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • An đã nộp "44,4" ở câu 2 và bị chấm Sai (đáp số mẫu 45, sai số ±0,5) | — | 1. Bấm "Em nghĩ đáp số này đúng"; gõ ghi chú "Em đo ra 44,4"; gửi | • Hiện "Đã gửi cho thầy cô"; báo ghi đáp số "44,4", ghi chú, giờ; lượt và kết quả câu không đổi | US-S2E8-05-SC6; BR-S2E8-30; STAGES Ch2 mục 10 |
| TC-S2E8-102 | Nút báo không hiện khi chưa bị chấm Sai | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • An ở câu 2 chưa gửi lần nào | — | 1. Xem màn làm bài | • Không có nút "Em nghĩ đáp số này đúng"; gọi API gửi báo nhận CONFLICT | US-S2E8-05-FC5; BR-S2E8-30 |
| TC-S2E8-103 | Mỗi em một báo mỗi câu | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • An đã báo câu 2 | — | 1. Báo lần hai cho câu 2 | • CONFLICT "Em đã báo câu này"; còn đúng một báo | US-S2E8-05-FC6; BR-S2E8-30 |
| TC-S2E8-104 | Ghi chú 200 ký tự nhận, 201 bị chặn, rỗng được | Medium | Boundary (Edge case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • An đã bị chấm Sai ở câu 2 và câu 3 | • Ghi chú 0, 200, 201 ký tự | 1. Báo câu 2 với ghi chú 200 ký tự; báo câu 3 với ghi chú 201 ký tự; báo một câu khác không ghi chú | • 200 ký tự: gửi được<br>• 201 ký tự: VALIDATION_FAILED "Ghi chú tối đa 200 ký tự"<br>• không ghi chú: gửi được | US-S2E8-05-BV5; BR-S2E8-30 |
| TC-S2E8-105 | Giáo viên thấy báo kèm đáp số và dạng chuẩn | High | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • An đã gửi báo cho câu 2 | — | 1. Cô Trang mở danh sách "Báo chấm sai" | • Hiện dòng: tên An (mã `HS-3F8A-2L6P`), câu 2, đáp số em nhập "44,4", ghi chú, dạng chuẩn "45°"; hai nút "Xác nhận đúng", "Giữ nguyên" | US-S2E8-05-SC7; BR-S2E8-31 |
| TC-S2E8-106 | Xác nhận đúng thì câu của em thành Đúng và đếm vào chỉ số | High | Normal (Success case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • Báo của An đang chờ | — | 1. Cô Trang bấm "Xác nhận đúng" | • Câu 2 của An thành Đúng; dạng viết "44,4" thêm vào danh sách chấp nhận của câu; chỉ số M-S2-07 tăng 1; An thấy kết quả đã đổi ở lần mở sau | US-S2E8-05-SC8; BR-S2E8-31; M-S2-07; QA-S2E8-09 |
| TC-S2E8-107 | Giữ nguyên thì kết quả không đổi | Medium | Normal (Success case) | chức năng | giao diện (trình duyệt, iPad / iPhone / web) | E2E-UI | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • Báo của An đang chờ | — | 1. Cô Trang bấm "Giữ nguyên" | • Báo chuyển "Đã xem, giữ nguyên"; kết quả câu 2 của An không đổi; M-S2-07 không đổi | US-S2E8-05-SC9; BR-S2E8-31 |
| TC-S2E8-108 | Giáo viên khác không thấy báo của lớp mình | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • Báo của An cho bài của Cô Trang; Cô Lan đăng nhập | — | 1. Cô Lan gọi danh sách báo; gọi xác nhận một báo | • Danh sách không có báo của An; xác nhận nhận NOT_FOUND | US-S2E8-05-FC7; BR-S2E8-31; BR-S2-06 |
| TC-S2E8-109 | Học sinh không gửi báo cho bài không thuộc mình | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • Bình (11A2) không được giao BL-1 | — | 1. Bình gọi gửi báo cho câu 2 của BL-1 | • NOT_FOUND; không báo nào được tạo | US-S2E8-05-FC8; BR-S2E8-30 |
| TC-S2E8-110 | Báo không làm lộ đáp số cho học sinh | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01); học sinh An (FX-02) | • An vừa gửi báo | — | 1. Xem phản hồi và màn sau khi gửi | • Chỉ "Đã gửi cho thầy cô"; không đáp số, không dạng chuẩn, không đúng sai của đáp số em | US-S2E8-05-FC9; BR-S2E8-30; STAGES mục 0.2 nguyên tắc 1 |
| TC-S2E8-114 | Bài luyện được lưu thành bộ bài dùng chung mô hình với S3-E2 | High | Normal (Success case) | chức năng | tích hợp (máy chủ + cơ sở dữ liệu) | IT | Not run | Giáo viên chủ lớp 11A1 (lớp mẫu từ MVP) | • Bài nháp 5 câu (FX-20); lớp 11A1 chuyển từ MVP | — | 1. Bấm "Giao"; xác nhận | • Có đúng một `AssignmentSet` Đã giao với 5 `AssignmentItem` kèm cấu hình luyện<br>• không có bảng bài luyện riêng<br>• bộ gắn lớp 11A1 | US-S2E8-05-SC10; BR-S2E8-32; PO-66 |
| TC-S2E8-115 | Giáo viên không phải chủ lớp mẫu không giao được cho lớp đó | High | Failure (Error case) | chức năng | API (gọi trực tiếp máy chủ) | E2E-API | Not run | Giáo viên Cô Trang (FX-01) | • Cô Trang không phải chủ lớp mẫu 11A1; bài nháp đầy đủ | — | 1. Gọi giao bài cho lớp 11A1 | • NOT_FOUND<br>• không tạo `AssignmentSet`<br>• học sinh 11A1 không thấy bài | US-S2E8-05-FC10; BR-S2E8-32; PO-66 |

## 3. Truy vết tiêu chí sang case

| Tiêu chí | Case |
|---|---|
| US-S2E8-01-SC1 | TC-S2E8-001 |
| US-S2E8-01-SC2 | TC-S2E8-002 |
| US-S2E8-01-SC3 | TC-S2E8-003 |
| US-S2E8-01-SC4 | TC-S2E8-004 |
| US-S2E8-01-SC5 | TC-S2E8-005 |
| US-S2E8-01-SC6 | TC-S2E8-006 |
| US-S2E8-01-SC7 | TC-S2E8-007 |
| US-S2E8-01-FC1 | TC-S2E8-008 |
| US-S2E8-01-BV1 | TC-S2E8-009 |
| US-S2E8-01-BV2 | TC-S2E8-010 |
| US-S2E8-01-FC2 | TC-S2E8-011 |
| US-S2E8-01-FC3 | TC-S2E8-012 |
| US-S2E8-01-SC8 | TC-S2E8-013 |
| US-S2E8-01-SC9 | TC-S2E8-014 |
| US-S2E8-02-SC1 | TC-S2E8-015 |
| US-S2E8-02-SC2 | TC-S2E8-016 |
| US-S2E8-02-SC3 | TC-S2E8-017 |
| US-S2E8-02-SC4 | TC-S2E8-018 |
| US-S2E8-02-SC5 | TC-S2E8-019 |
| US-S2E8-02-SC6 | TC-S2E8-020 |
| US-S2E8-02-SC7 | TC-S2E8-021 |
| US-S2E8-02-SC8 | TC-S2E8-022 |
| US-S2E8-02-BV1 | TC-S2E8-023 |
| US-S2E8-02-BV2 | TC-S2E8-024 |
| US-S2E8-02-BV3 | TC-S2E8-025 |
| US-S2E8-02-BV4 | TC-S2E8-026 |
| US-S2E8-02-BV5 | TC-S2E8-027 |
| US-S2E8-02-BV6 | TC-S2E8-028 |
| US-S2E8-02-FC1 | TC-S2E8-029 |
| US-S2E8-02-FC2 | TC-S2E8-030 |
| US-S2E8-02-FC3 | TC-S2E8-031 |
| US-S2E8-02-FC4 | TC-S2E8-032 |
| US-S2E8-02-FC5 | TC-S2E8-033 |
| US-S2E8-02-FC6 | TC-S2E8-034 |
| US-S2E8-02-FC7 | TC-S2E8-035 |
| US-S2E8-02-FC8 | TC-S2E8-036 |
| US-S2E8-02-FC9 | TC-S2E8-037 |
| US-S2E8-02-FC10 | TC-S2E8-038 |
| US-S2E8-02-SC9 | TC-S2E8-039 |
| US-S2E8-02-BV7 | TC-S2E8-040 |
| US-S2E8-02-BV8 | TC-S2E8-041 |
| US-S2E8-02-BV9 | TC-S2E8-042 |
| US-S2E8-02-BV10 | TC-S2E8-043 |
| US-S2E8-02-BV11 | TC-S2E8-044 |
| US-S2E8-02-BV12 | TC-S2E8-045 |
| US-S2E8-02-BV13 | TC-S2E8-046 |
| US-S2E8-02-FC11 | TC-S2E8-047 |
| US-S2E8-02-FC12 | TC-S2E8-048 |
| US-S2E8-02-FC13 | TC-S2E8-049 |
| US-S2E8-02-FC14 | TC-S2E8-050 |
| US-S2E8-02-FC15 | TC-S2E8-051 |
| US-S2E8-02-FC16 | TC-S2E8-052 |
| US-S2E8-02-FC17 | TC-S2E8-053 |
| US-S2E8-02-SC10 | TC-S2E8-054 |
| US-S2E8-02-FC18 | TC-S2E8-055 |
| US-S2E8-03-SC1 | TC-S2E8-056 |
| US-S2E8-03-SC2 | TC-S2E8-057 |
| US-S2E8-03-SC3 | TC-S2E8-058 |
| US-S2E8-03-SC4 | TC-S2E8-059 |
| US-S2E8-03-SC5 | TC-S2E8-060 |
| US-S2E8-03-SC6 | TC-S2E8-061 |
| US-S2E8-03-BV1 | TC-S2E8-062 |
| US-S2E8-03-SC7 | TC-S2E8-063 |
| US-S2E8-03-FC1 | TC-S2E8-064 |
| US-S2E8-03-FC2 | TC-S2E8-065 |
| US-S2E8-03-FC3 | TC-S2E8-066 |
| US-S2E8-03-FC4 | TC-S2E8-067 |
| US-S2E8-03-FC5 | TC-S2E8-068 |
| US-S2E8-03-FC6 | TC-S2E8-069 |
| US-S2E8-03-SC8 | TC-S2E8-070 |
| US-S2E8-03-BV2 | TC-S2E8-071 |
| US-S2E8-03-BV3 | TC-S2E8-072 |
| US-S2E8-03-SC9 | TC-S2E8-073 |
| US-S2E8-03-SC10 | TC-S2E8-074 |
| US-S2E8-04-SC1 | TC-S2E8-075 |
| US-S2E8-04-SC2 | TC-S2E8-076 |
| US-S2E8-04-SC3 | TC-S2E8-077 |
| US-S2E8-04-SC4 | TC-S2E8-078 |
| US-S2E8-04-SC5 | TC-S2E8-079 |
| US-S2E8-04-BV1 | TC-S2E8-080 |
| US-S2E8-04-SC6 | TC-S2E8-081 |
| US-S2E8-04-FC1 | TC-S2E8-082 |
| US-S2E8-04-FC2 | TC-S2E8-083 |
| US-S2E8-04-BV2 | TC-S2E8-084 |
| US-S2E8-04-BV3 | TC-S2E8-085 |
| US-S2E8-04-FC3 | TC-S2E8-086 |
| US-S2E8-04-SC7 | TC-S2E8-087 |
| US-S2E8-05-SC1 | TC-S2E8-088 |
| US-S2E8-05-SC2 | TC-S2E8-089 |
| US-S2E8-05-SC3 | TC-S2E8-090 |
| US-S2E8-05-SC4 | TC-S2E8-091 |
| US-S2E8-05-SC5 | TC-S2E8-092 |
| US-S2E8-05-FC1 | TC-S2E8-093 |
| US-S2E8-05-FC2 | TC-S2E8-094 |
| US-S2E8-05-BV1 | TC-S2E8-095 |
| US-S2E8-05-BV2 | TC-S2E8-096 |
| US-S2E8-05-BV3 | TC-S2E8-097 |
| US-S2E8-05-BV4 | TC-S2E8-098 |
| US-S2E8-05-FC3 | TC-S2E8-099 |
| US-S2E8-05-FC4 | TC-S2E8-100 |
| US-S2E8-05-SC6 | TC-S2E8-101 |
| US-S2E8-05-FC5 | TC-S2E8-102 |
| US-S2E8-05-FC6 | TC-S2E8-103 |
| US-S2E8-05-BV5 | TC-S2E8-104 |
| US-S2E8-05-SC7 | TC-S2E8-105 |
| US-S2E8-05-SC8 | TC-S2E8-106 |
| US-S2E8-05-SC9 | TC-S2E8-107 |
| US-S2E8-05-FC7 | TC-S2E8-108 |
| US-S2E8-05-FC8 | TC-S2E8-109 |
| US-S2E8-05-FC9 | TC-S2E8-110 |
| US-S2E8-05-SC10 | TC-S2E8-114 |
| US-S2E8-05-FC10 | TC-S2E8-115 |
| US-S2E8-02 | TC-S2E8-111 |
| US-S2E8-03 | TC-S2E8-112 |
| US-S2E8-03 | TC-S2E8-113 |

### Khoảng trống

- Không tiêu chí nào thiếu case. Case chưa chạy được vì code chặng 2 chưa có (PO-65 Q1): trạng thái Not run.
- Câu hỏi và đề xuất đổi story map: `questions.md` cùng thư mục.

## 4. Dữ liệu và môi trường

- Dữ liệu: bảng `FX-nn` đầu tệp (đồng bộ với `stories_ac.md`).
- Môi trường: profile `dev` / `test`, AI giả lập, không bao giờ chạy trên `prod` (PO-51). Thiết bị theo mục quy ước của `stories_ac.md`.

## 5. Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: 113 case cho 110 tiêu chí, có cột Tự động hóa | runner-T-261126-12 (QC) |
| v0.2 | 2026-11-26 | đổi mã mẫu `HS-`/`GV-` có ký tự 0, 1, O, I sang ký tự hợp lệ (BR-S2E1-04), RV_s23 m-01 | runner-T-261126-15 (QC soát phản biện, RV_s23) |
| v0.3 | 2026-11-26 | G1: BR-S2E8-32 một mô hình bộ bài, giao cho lớp mẫu 11A1; thêm SC10, FC10; bỏ nhãn giả định chưa chốt (PO-66) | runner-T-261126-37 (BA/QC) |
