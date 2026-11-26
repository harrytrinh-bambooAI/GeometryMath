Phiên bản: v0.3 · 2026-11-26
# S2-E8 — Bài luyện điền đáp số: bàn phím toán, chấm tương đương, làm bài, kết quả, soạn bài luyện: story Dev-Ready và tiêu chí chấp nhận

> Chủ sở hữu: BA (goal run T-261126-12, BA + QC liền). Nguồn: `artifacts/GEO/work/s23/story-map-s23.md` (R2 khóa 26/11/2026), `brief-s23.md`, `brd-delta-s23.md`, `STAGES-AFTER-MVP.md` Chặng 2 mục 5–8 (22/11/2026), `Design/s23/DQ.md` và ảnh `Design/s23/c2-*.png` (26/11/2026), `PO-DECISIONS.md` PO-61…65 (PO-61 mới nhất, thắng mọi nguồn cũ).
> **Trạng thái: ready for R3** · Mức epic: **P0** · Năm story, ưu tiên dồn công cho khung C2-06 (ảnh `c2-06-luyen-ipad.png`, `c2-06-luyen-iphone.png`, `c2-06-sai-goi-y.png`, `c2-06-het-luot.png`; DQ-11…DQ-14): AC biên và case biên đầy đủ cho chấm đáp số, lượt thử, khóa đáp số.

## 0. Reader guide and glossary (hướng dẫn đọc và bảng mã)

**Cách đọc.** Mỗi mục `## US-S2En-nn — …` là một story: câu story, góc nhìn người vận hành, quy tắc nghiệp vụ (BR), tiêu chí chấp nhận (AC). Mỗi AC nói **một** hành vi quan sát được, dạng Given / When / Then, kèm giá trị cụ thể. `SC` = kịch bản chính, `FC` = lỗi hoặc ngoại lệ, `BV` = giá trị biên. Mã chỉ cho biết **chỗ** một điều được ghi, câu chữ nói điều đó là gì.

**Quy ước chung cho mọi AC trong tệp này** (kế thừa MVP, đối chiếu `stories_ac.md` của E1):
- "T0" là giờ máy chủ lúc bắt đầu ca kiểm; mọi mốc giờ tương đối so với T0. Giờ hiển thị `HH:mm, dd/MM/yyyy` giờ Việt Nam; số thập phân hiển thị dấu phẩy.
- Lỗi máy chủ có dạng `{code, message, requestId, details[]}`; `code` ổn định theo D-GEO-143 (VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, NOT_FOUND, CONFLICT, RATE_LIMITED kèm `Retry-After`).
- Phản hồi chứa lời giải, gợi ý hoặc trạng thái khóa mang `Cache-Control: no-store` (D-GEO-145). "Dữ liệu trả về" là toàn bộ nội dung máy chủ gửi xuống thiết bị, không chỉ phần giao diện vẽ ra.
- Thiết bị theo PO-61 và STAGES mục 0.2: giáo viên dùng web (điện thoại chỉ để liếc); học sinh dùng iPad (chính) và iPhone (phụ) qua trình duyệt (PWA, PO-65; chốt PO-66).
- AI trong kiểm thử là giả lập (D-GEO-047): không có kỳ vọng về chất lượng AI thật. Giá trị trong Given / Then lấy từ bảng `FX-nn` hoặc ghi ngay trong tiêu chí.
- AC mang chữ "đã chốt PO-66" dựa trên giả định đã được owner chốt ở PO-66 (26/11/2026); mã `QA-S2En-nn` hoặc `DQ-nn` đứng cạnh cho biết câu hỏi gốc.

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

## US-S2E8-01 — Bàn phím toán (căn, phân số, π) trên iPad và iPhone

- **Story:** Là học sinh, tôi muốn gõ đáp số có căn, phân số, π bằng bàn phím riêng, để không bị kẹt vì bàn phím điện thoại thiếu ký hiệu · Loại UI · Mức P0 · Khung C2-06
- **Nguồn:** STAGES Ch2 mục 10; DQ-11
- **Góc nhìn người vận hành:** học sinh chạm bằng ngón tay, hay chạm nhầm, hay muốn sửa giữa chuỗi. Bàn phím hệ thống không được bật (che mất hình). Bố cục: 6 cột gồm số, ngoặc, xóa lùi, + - ×, √ ÷ π, dấu phẩy, ° / ^ a, x, Xóa hết, Kiểm tra (ảnh `c2-06-luyen-ipad.png`).
- **Phụ thuộc:** S2-E1; US-S2E8-03 (màn làm bài)

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S2E8-01 | Bàn phím riêng có: chữ số 0 đến 9; ( ) ; + - × ÷ ; √ ; π ; ^ ; ° ; dấu phẩy ; / ; chữ a và x ; xóa lùi; "Xóa hết"; "Kiểm tra". iPad và iPhone cùng bố cục, iPhone nén chiều cao hàng | DQ-11 |
| BR-S2E8-02 | Ô đáp số chỉ nhận ký tự từ bàn phím riêng; chạm vào ô để đặt vị trí chèn; chạm ô không bật bàn phím hệ thống | DQ-11 |
| BR-S2E8-03 | Đáp số dài tối đa 50 ký tự; đủ 50 thì phím nhập không chèn thêm và hiện "Tối đa 50 ký tự" (đã chốt PO-66, QA-S2E8-03) | BR-S2-08 |
| BR-S2E8-04 | Nút "Kiểm tra" khóa khi ô trống; mọi phím cỡ chạm tối thiểu 44 điểm; iPhone 390 điểm không cuộn ngang | STAGES mục 0.2 nguyên tắc 6 |
| BR-S2E8-05 | Chuỗi dán hoặc đưa vào ô từ ngoài bàn phím bị lọc: chỉ giữ ký tự thuộc bảng phím | BR-S2-08 |

### Tiêu chí chấp nhận

**US-S2E8-01-SC1 — Phím số chèn chữ số vào ô** · Nguồn: DQ-11
- Given: An ở màn bài luyện trên iPad, ô đáp số trống
- When: Chạm 4 rồi 5
- Then: Ô hiện "45"

**US-S2E8-01-SC2 — Phím căn và phân số chèn đúng ký hiệu** · Nguồn: DQ-11; STAGES Ch2 mục 5 luồng C
- Given: Ô trống
- When: Chạm 4, √, 3; xóa hết; chạm 1, /, 2
- Then: Lần 1: ô hiện "4√3"; lần 2: ô hiện "1/2"

**US-S2E8-01-SC3 — Phím π, mũ và độ chèn đúng** · Nguồn: DQ-11
- Given: Ô trống
- When: Chạm 2, π; xóa hết; chạm 2, ^, 3; xóa hết; chạm 4, 5, °
- Then: Ô lần lượt hiện "2π", "2^3", "45°"

**US-S2E8-01-SC4 — Dấu phẩy nhập số thập phân** · Nguồn: DQ-11
- Given: Ô trống
- When: Chạm 0, dấu phẩy, 5
- Then: Ô hiện "0,5"

**US-S2E8-01-SC5 — Xóa lùi bỏ ký tự ngay trước con trỏ** · Nguồn: DQ-11
- Given: Ô có "4√3" và con trỏ ở cuối
- When: Chạm ⌫ hai lần
- Then: Ô hiện "4"

**US-S2E8-01-SC6 — Chèn giữa chuỗi theo vị trí con trỏ** · Nguồn: BR-S2E8-02
- Given: Ô có "45"
- When: Chạm giữa hai chữ số để đặt con trỏ; chạm °
- Then: Ô hiện "4°5" (ký tự chèn đúng vị trí con trỏ; việc chấm xử lý sau)

**US-S2E8-01-SC7 — Xóa hết làm trống ô** · Nguồn: DQ-11
- Given: Ô có "4√3"
- When: Chạm "Xóa hết"
- Then: Ô trống; nút "Kiểm tra" khóa

**US-S2E8-01-FC1 — Ô trống thì nút Kiểm tra khóa** · Nguồn: BR-S2E8-04
- Given: Ô trống
- When: Chạm "Kiểm tra"
- Then: Nút khóa; không gửi yêu cầu

**US-S2E8-01-BV1 — Đủ 50 ký tự vẫn nhập được** · Nguồn: BR-S2E8-03; QA-S2E8-03
- Given: Ô có 49 ký tự
- When: Chạm thêm 1 chữ số (thành 50)
- Then: Ô có 50 ký tự; chưa có thông báo giới hạn

**US-S2E8-01-BV2 — Ký tự thứ 51 không được chèn** · Nguồn: BR-S2E8-03; QA-S2E8-03
- Given: Ô có 50 ký tự
- When: Chạm thêm 1 chữ số
- Then: Ô vẫn 50 ký tự; hiện "Tối đa 50 ký tự"

**US-S2E8-01-FC2 — Chạm ô không bật bàn phím hệ thống** · Nguồn: BR-S2E8-02; DQ-11
- Given: iPad và iPhone thật, bài luyện mở
- When: Chạm vào ô đáp số
- Then: Bàn phím hệ thống không hiện; bàn phím riêng vẫn ở vị trí; hình không bị đẩy lên

**US-S2E8-01-FC3 — Chuỗi dán bị lọc theo bảng phím** · Nguồn: BR-S2E8-05
- Given: Ô trống; trong bộ nhớ tạm có "45 độ <b>"
- When: Dán vào ô
- Then: Ô chỉ giữ "45" (ký tự ngoài bảng phím bị bỏ); không có hộp thoại lỗi

**US-S2E8-01-SC8 — iPhone cùng bố cục, nén hàng, không cuộn ngang, phím đủ lớn** · Nguồn: BR-S2E8-01, BR-S2E8-04; DQ-11
- Given: An trên iPhone 390 × 844
- When: Mở bài luyện
- Then: Bàn phím 6 cột cùng thứ tự như iPad; hàng nén; không cuộn ngang; mọi phím cao không dưới 44 điểm; hình và ô đáp số vẫn thấy cùng lúc

**US-S2E8-01-SC9 — Giữ nguyên nội dung ô khi xoay iPad** · Nguồn: DQ-11
- Given: Ô có "4√3"
- When: Xoay iPad dọc sang ngang
- Then: Ô vẫn "4√3"; bàn phím vẽ lại cân đối

- **Mở và quyết định đang dùng:** QA-S2E8-03
- **DoR:** ready for R3, mức P0.

## US-S2E8-02 — Chấm đáp số dạng tương đương bằng `grade`; sai cú pháp không trừ lượt

- **Story:** Là học sinh, tôi muốn đáp số đúng giá trị nhưng viết khác vẫn được tính đúng và nhập lỗi cú pháp không bị trừ lượt, để không mất lượt oan · Loại Logic · Mức P0 · Khung C2-06
- **Nguồn:** STAGES Ch2 mục 5 luồng C; BR-S2-08; DQ-12
- **Góc nhìn người vận hành:** học sinh viết √48 thay vì 4√3, 2/4 thay vì 1/2, 45° thay vì 45, hay gõ sót ngoặc. Máy chấm chạy ở máy chủ, đáp số mẫu không bao giờ xuống thiết bị, phản hồi Sai không nói đáp số hay hướng. Cú pháp sai không phải sai giá trị, không trừ lượt. Ba kết quả: Đúng, Sai, Sai cú pháp.
- **Phụ thuộc:** US-S2E8-01, US-S2E2-01 (`grade`)

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S2E8-06 | `grade` có ba kết quả: Đúng (kèm dạng chuẩn nếu khác cách viết mẫu), Sai (đúng cú pháp nhưng sai giá trị), Sai cú pháp (không đọc được hoặc không có nghĩa) | STAGES Ch2 mục 5 luồng C |
| BR-S2E8-07 | Chỉ kết quả Sai mới trừ một lượt; Đúng và Sai cú pháp không trừ; Sai cú pháp báo vị trí ký tự lỗi (tính từ 1) và lý do bằng tiếng Việt | STAGES Ch2 mục 5 luồng C |
| BR-S2E8-08 | Kiểu chính xác: đúng khi giá trị của hai biểu thức bằng nhau (sai lệch tương đối không quá 1e-9; khi đáp số mẫu bằng 0 thì sai lệch tuyệt đối không quá 1e-9) ở mọi dạng viết (phân số rút gọn hay không, căn rút gọn hay không, thập phân hữu hạn của số hữu tỉ); thập phân làm tròn của số vô tỉ là Sai (đã chốt PO-66, QA-S2E8-02) | STAGES Ch2 mục 5, DQ-12 |
| BR-S2E8-09 | Kiểu xấp xỉ: đúng khi |đáp số nộp − đáp số mẫu| ≤ tol, tính bằng số thập phân chính xác (biên bao gồm); đơn vị ° có hoặc không đều nhận | DQ-12 |
| BR-S2E8-10 | Bảng ký hiệu nhận: số nguyên, thập phân (dấu phẩy hoặc dấu chấm đều là dấu thập phân, không có dấu phân cách hàng nghìn), phân số `/`, `÷`, `×`, `+`, `-` (cả dấu âm đứng đầu), `^`, `√` (kèm số hoặc ngoặc), `π`, ngoặc, chữ `a` và `x` (thử giá trị để so khớp), `°`; mỗi số tối đa 15 chữ số (đã chốt PO-66, QA-S2E8-04) | DQ-11 |
| BR-S2E8-11 | Chia cho 0, thiếu toán hạng, ngoặc không cân, chữ lạ, hai số liền nhau, hai dấu phẩy trong một số, quá 50 ký tự, số quá 15 chữ số đều là Sai cú pháp | STAGES Ch2 mục 5 luồng C |
| BR-S2E8-12 | Chấm ở máy chủ: đáp số mẫu không có trong dữ liệu trả về nào của bài luyện; phản hồi Sai không chứa đáp số, độ lệch hay hướng (lớn hơn, nhỏ hơn) | STAGES mục 0.2 nguyên tắc 1, PO-65 |
| BR-S2E8-13 | Sau khi câu hết lượt hoặc đã Đúng, mọi lần gửi tiếp nhận CONFLICT và không được chấm (không dùng được làm cách dò đáp số) | D-GEO-143 |
| BR-S2E8-14 | Mỗi lần gửi mang mã chống lặp; gửi lại cùng mã (bấm đôi, mạng thử lại) chỉ ghi và trừ lượt một lần | D-GEO-143 |

- **Dữ liệu:** câu A (kiểu chính xác, đáp số mẫu `4√3`); câu B (kiểu chính xác, đáp số mẫu `1/2`); câu C (kiểu chính xác, đáp số mẫu `6`); câu D (kiểu xấp xỉ, đáp số mẫu 45, sai số ±0,5, đơn vị °; chính là câu 2 của BL-1 trong FX-20); câu E (kiểu chính xác, đáp số mẫu `a√2`); câu F (kiểu chính xác, đáp số mẫu `1`); mỗi câu 3 lượt thử

### Tiêu chí chấp nhận

**US-S2E8-02-SC1 — Đáp số đúng đúng dạng chuẩn** · Nguồn: BR-S2E8-08; STAGES Ch2 mục 5
- Given: Câu A, lượt 3/3
- When: Gửi "4√3"
- Then: Đúng; thông báo "Đúng rồi!" (không dòng dạng chuẩn); lượt vẫn 3/3

**US-S2E8-02-SC2 — Căn chưa rút gọn vẫn đúng và hiện dạng chuẩn** · Nguồn: BR-S2E8-08; STAGES Ch2 mục 5 ngoại lệ
- Given: Câu A
- When: Gửi "√48"
- Then: Đúng; hiện "Đúng rồi! Dạng chuẩn: 4√3"; lượt không trừ

**US-S2E8-02-SC3 — Phân số chưa rút gọn vẫn đúng** · Nguồn: BR-S2E8-08
- Given: Câu B (đáp số mẫu 1/2)
- When: Gửi "2/4"
- Then: Đúng; hiện "Dạng chuẩn: 1/2"

**US-S2E8-02-SC4 — Thập phân hữu hạn bằng số hữu tỉ** · Nguồn: BR-S2E8-08; BR-S2E8-10
- Given: Câu B
- When: Gửi "0,5"; gửi "0.5"
- Then: Cả hai Đúng kèm "Dạng chuẩn: 1/2"

**US-S2E8-02-SC5 — Số nguyên viết nhiều cách** · Nguồn: BR-S2E8-08
- Given: Câu C (đáp số mẫu 6)
- When: Gửi "12/2"; gửi "6,0"; gửi "06"; gửi "2×3"; gửi "(1+5)"
- Then: Cả năm Đúng

**US-S2E8-02-SC6 — Lũy thừa và căn của số chính phương** · Nguồn: BR-S2E8-08; BR-S2E8-10
- Given: Câu C
- When: Gửi "2^3-2"; gửi "√36"
- Then: Cả hai Đúng

**US-S2E8-02-SC7 — Đáp số có tham số a so khớp theo giá trị** · Nguồn: BR-S2E8-08; BR-S2E8-10
- Given: Câu E (đáp số mẫu a√2)
- When: Gửi "a√2"; gửi "√2×a"; gửi "√(2a^2)"
- Then: Hai cách đầu Đúng; cách thứ ba (√(2a^2) bằng a√2 chỉ khi a ≥ 0) vẫn Đúng vì so khớp với giá trị a dương thử

**US-S2E8-02-SC8 — Kiểu xấp xỉ nhận 45, 45° và 45,0** · Nguồn: BR-S2E8-09; DQ-12
- Given: Câu D (xấp xỉ 45, ±0,5, °)
- When: Gửi "45"; gửi "45°"; gửi "45,0"; gửi "90/2"
- Then: Cả bốn Đúng; với "90/2" hiện thêm "Dạng chuẩn: 45°"

**US-S2E8-02-BV1 — Sát biên trên 45,5 là Đúng** · Nguồn: BR-S2E8-09; DQ-12
- Given: Câu D (tol 0,5)
- When: Gửi "45,5"
- Then: Đúng (biên bao gồm)

**US-S2E8-02-BV2 — Vượt biên trên 45,51 là Sai** · Nguồn: BR-S2E8-09; DQ-12
- Given: Câu D
- When: Gửi "45,51"
- Then: Sai; trừ một lượt

**US-S2E8-02-BV3 — Sát biên dưới 44,5 là Đúng** · Nguồn: BR-S2E8-09
- Given: Câu D
- When: Gửi "44,5"
- Then: Đúng

**US-S2E8-02-BV4 — Vượt biên dưới 44,49 là Sai** · Nguồn: BR-S2E8-09
- Given: Câu D
- When: Gửi "44,49"
- Then: Sai; trừ một lượt

**US-S2E8-02-BV5 — Biên không bị sai số dấu phẩy động: 45,50 và 44,50** · Nguồn: BR-S2E8-09
- Given: Câu D
- When: Gửi "45,50"; gửi "44,50"; gửi "45,500000000001"
- Then: 45,50: Đúng; 44,50: Đúng; 45,500000000001: Sai (tính bằng số thập phân chính xác, không dùng số dấu phẩy động nhị phân)

**US-S2E8-02-BV6 — Sai số 0 cho kiểu xấp xỉ nghĩa là bằng đúng** · Nguồn: BR-S2E8-09; QA-S2E8-05
- Given: Câu D đổi sai số thành 0
- When: Gửi "45"; gửi "45,001"
- Then: 45: Đúng; 45,001: Sai

**US-S2E8-02-FC1 — Giá trị sai trừ một lượt và không lộ hướng** · Nguồn: BR-S2E8-07; BR-S2E8-12; DQ-12
- Given: Câu D, lượt 3/3
- When: Gửi "60°"
- Then: Sai; lượt còn 2/3; hiện "Chưa đúng. Còn 2 lượt."; phản hồi không chứa đáp số, không chứa "lớn hơn" hay "nhỏ hơn"

**US-S2E8-02-FC2 — Thập phân làm tròn của số vô tỉ là Sai ở kiểu chính xác** · Nguồn: BR-S2E8-08; QA-S2E8-02
- Given: Câu A (đáp số mẫu 4√3)
- When: Gửi "6,93"
- Then: Sai; trừ một lượt; gợi ý chữ "Em hãy viết đáp số ở dạng chính xác (có căn)"

**US-S2E8-02-FC3 — Ô trống hoặc chỉ khoảng trắng là Sai cú pháp, không trừ lượt** · Nguồn: BR-S2E8-07, BR-S2E8-11
- Given: Câu D, lượt 3/3
- When: Gọi chấm với "" và với "   "
- Then: Sai cú pháp "Em hãy nhập đáp số."; lượt vẫn 3/3

**US-S2E8-02-FC4 — Căn thiếu số** · Nguồn: BR-S2E8-11; STAGES Ch2 mục 5
- Given: Câu A
- When: Gửi "√"
- Then: Sai cú pháp báo "Thiếu số sau dấu căn (ký tự 1)"; lượt không đổi

**US-S2E8-02-FC5 — Ngoặc không cân** · Nguồn: BR-S2E8-11
- Given: Câu C
- When: Gửi "((1+2)"; gửi "1+2)"
- Then: Cả hai Sai cú pháp: "Thiếu dấu )" và "Thừa dấu )" kèm vị trí; lượt không đổi

**US-S2E8-02-FC6 — Chia cho 0** · Nguồn: BR-S2E8-11
- Given: Câu C
- When: Gửi "1/0"; gửi "5÷(2-2)"
- Then: Cả hai Sai cú pháp "Chia cho 0 không có nghĩa"; không trừ lượt

**US-S2E8-02-FC7 — Chữ lạ** · Nguồn: BR-S2E8-10, BR-S2E8-11
- Given: Câu C
- When: Gửi "abc"; gửi "45 độ"
- Then: Cả hai Sai cú pháp "Chữ chưa dùng được"; không trừ lượt (chỉ nhận chữ a và x)

**US-S2E8-02-FC8 — Hai số liền nhau** · Nguồn: BR-S2E8-11
- Given: Câu C
- When: Gửi "5 5"; gửi "4 √3"
- Then: "5 5": Sai cú pháp "Hai số liền nhau, thiếu phép tính"; "4 √3": coi dấu cách là bỏ qua, chấm như "4√3"

**US-S2E8-02-FC9 — Hai dấu phẩy trong một số** · Nguồn: BR-S2E8-11
- Given: Câu B
- When: Gửi "1,2,3"; gửi "3..5"
- Then: Cả hai Sai cú pháp "Số có hai dấu thập phân"; không trừ lượt

**US-S2E8-02-FC10 — Dấu cộng, nhân liền nhau** · Nguồn: BR-S2E8-11
- Given: Câu C
- When: Gửi "++3"; gửi "2××3"; gửi "--3"
- Then: Cả ba Sai cú pháp báo thiếu toán hạng hoặc phép tính thừa; không trừ lượt

**US-S2E8-02-SC9 — Dấu âm đứng đầu hợp lệ** · Nguồn: BR-S2E8-10
- Given: Câu có đáp số mẫu -3
- When: Gửi "-3"; gửi "0-3"
- Then: Cả hai Đúng

**US-S2E8-02-BV7 — Đáp số dài đúng 50 ký tự được chấm** · Nguồn: BR-S2E8-11
- Given: Câu C
- When: Gửi biểu thức hợp lệ dài 50 ký tự có giá trị 6 (ví dụ nhiều dấu +0)
- Then: Chấm bình thường: Đúng

**US-S2E8-02-BV8 — Đáp số 51 ký tự là Sai cú pháp** · Nguồn: BR-S2E8-11
- Given: Câu C
- When: Gọi API chấm với biểu thức 51 ký tự
- Then: Sai cú pháp "Đáp số tối đa 50 ký tự"; không trừ lượt; không chấm giá trị

**US-S2E8-02-BV9 — Số 15 chữ số nhận, 16 chữ số là Sai cú pháp** · Nguồn: BR-S2E8-10; QA-S2E8-04
- Given: Câu C
- When: Gửi số 15 chữ số "100000000000000"; gửi số 16 chữ số "1000000000000000"
- Then: Số 15 chữ số: được chấm (Sai giá trị, trừ lượt); số 16 chữ số: Sai cú pháp "Số quá lớn", không trừ lượt

**US-S2E8-02-BV10 — Dấu chấm là dấu thập phân, không phải ngăn nghìn** · Nguồn: BR-S2E8-10; QA-S2E8-06
- Given: Câu C (đáp số mẫu 6); câu F (đáp số mẫu 1)
- When: Gửi "1.000" cho câu F; gửi "6.0" cho câu C
- Then: "1.000" bằng 1: Đúng; "6.0": Đúng

**US-S2E8-02-BV11 — Lượt thử đếm đúng khi Sai lần lượt 3 lần** · Nguồn: BR-S2E8-07; STAGES Ch2 mục 5
- Given: Câu D, 3 lượt
- When: Gửi "60", rồi "61", rồi "62" (ba lần Sai)
- Then: Sau lần 1: còn 2; sau lần 2: còn 1; sau lần 3: còn 0, câu hết lượt "xem lại cùng thầy cô"

**US-S2E8-02-BV12 — Đúng ở lượt cuối vẫn đúng** · Nguồn: BR-S2E8-07
- Given: Câu D, đã Sai 2 lần (còn 1 lượt)
- When: Gửi "45"
- Then: Đúng; câu đúng (không "xem lại cùng thầy cô"); lượt đã dùng ghi 3/3

**US-S2E8-02-BV13 — Sai cú pháp ở lượt cuối không trừ và không hết lượt** · Nguồn: BR-S2E8-07
- Given: Câu D, đã Sai 2 lần (còn 1 lượt)
- When: Gửi "√"; gửi "45"
- Then: "√": Sai cú pháp, vẫn còn 1 lượt; "45": Đúng

**US-S2E8-02-FC11 — Hết lượt rồi gửi tiếp thì CONFLICT và không chấm** · Nguồn: BR-S2E8-13
- Given: Câu D đã hết lượt (3 lần Sai)
- When: Gửi "45" (đáp số đúng)
- Then: CONFLICT; không chấm; không lộ đúng sai; kết quả câu vẫn "xem lại cùng thầy cô"

**US-S2E8-02-FC12 — Đã Đúng rồi gửi tiếp thì CONFLICT** · Nguồn: BR-S2E8-13
- Given: Câu D đã Đúng
- When: Gửi "46"
- Then: CONFLICT "Câu này đã đúng"; kết quả không đổi

**US-S2E8-02-FC13 — Bấm đôi chỉ trừ một lượt** · Nguồn: BR-S2E8-14
- Given: Câu D lượt 3/3; hai yêu cầu cùng mã chống lặp
- When: Gửi "60" hai lần trong 1 giây với cùng mã
- Then: Chỉ một Attempt; lượt còn 2/3; hai phản hồi giống hệt

**US-S2E8-02-FC14 — Mạng chập chờn gửi lại cùng mã không trừ hai lần** · Nguồn: BR-S2E8-14
- Given: Yêu cầu "60" đã xử lý nhưng phản hồi không về tới thiết bị
- When: Thiết bị gửi lại cùng mã
- Then: Máy chủ trả đúng phản hồi lần đầu; lượt còn 2/3 (không phải 1/3)

**US-S2E8-02-FC15 — Đáp số mẫu không có trong dữ liệu trả về của bài luyện** · Nguồn: BR-S2E8-12; STAGES mục 0.2 nguyên tắc 1
- Given: An mở BL-1 câu 2; trước giờ mở lời giải
- When: Xem toàn bộ dữ liệu trả về khi mở bài, sau mỗi lần Sai và sau khi hết lượt
- Then: Không có 45 làm đáp số, không có sai số, không có bước giải ở mọi dạng viết (45, 45°, 90/2, "bốn mươi lăm"); tiêu đề `Cache-Control: no-store`

**US-S2E8-02-FC16 — Ba lần Sai liên tiếp không để lộ hướng** · Nguồn: BR-S2E8-12
- Given: Câu D
- When: Gửi "40", "50", "46" (ba lần Sai)
- Then: Ba phản hồi chỉ nói chưa đúng và số lượt còn; không nói lớn hơn hay nhỏ hơn; không có độ lệch

**US-S2E8-02-FC17 — Gọi chấm không đăng nhập** · Nguồn: D-GEO-143
- Given: Chưa đăng nhập
- When: Gọi chấm câu D
- Then: UNAUTHENTICATED; không Attempt

**US-S2E8-02-SC10 — Sai cú pháp báo đúng vị trí ký tự** · Nguồn: BR-S2E8-07; STAGES Ch2 mục 5
- Given: Câu C
- When: Gửi "2+*3"
- Then: Báo "Thừa dấu * (ký tự 3)"; vị trí ký tự tính từ 1

**US-S2E8-02-FC18 — Thử đáp số bằng cách thay đổi sai số hoặc kiểu từ thiết bị** · Nguồn: BR-S2E8-12
- Given: Câu D
- When: Gọi chấm với thân yêu cầu có thêm trường `tol` = 100 và `type` = "approx"
- Then: Máy chủ bỏ qua các trường đó (chỉ dùng đáp số nộp và mã chống lặp); kết quả theo cấu hình của câu

- **Mở và quyết định đang dùng:** QA-S2E8-02, QA-S2E8-04, QA-S2E8-05, QA-S2E8-06
- **DoR:** ready for R3, mức P0.

## US-S2E8-03 — Làm bài luyện: lượt thử, gợi ý mức 1, "xem lại cùng thầy cô" khi hết lượt

- **Story:** Là học sinh, tôi muốn làm lần lượt từng câu với số lượt thử rõ ràng và gợi ý khi sai, để học mà không bị lộ đáp số · Loại UI · Mức P0 · Khung C2-06
- **Nguồn:** STAGES Ch2 mục 5 luồng C; DQ-12
- **Góc nhìn người vận hành:** học sinh làm trên iPad ở lớp hoặc ở nhà; có khi thoát giữa chừng, có khi mất mạng lúc bấm Kiểm tra, có khi hết lượt. Gợi ý mức 1 dẫn hướng, không nêu đáp số. Lời giải khóa tới giờ mở kể cả khi hết lượt (STAGES mục 7).
- **Phụ thuộc:** US-S2E8-01, US-S2E8-02, US-S2E8-05 (bài đã giao)

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S2E8-15 | Màn làm bài: tiêu đề bài, "Câu k/n", chip "Còn m/N lượt", thanh tiến độ n đoạn (xanh đúng, vàng đang làm, xám chưa), hình ở giữa, ô đáp số, bàn phím toán | DQ-11, DQ-12 |
| BR-S2E8-16 | Đúng: hiện xác nhận và nút "Câu kế"; Sai: hiện "Chưa đúng. Còn m lượt." kèm gợi ý mức 1; chip "Còn m/N lượt" cập nhật | STAGES Ch2 mục 5 luồng C, DQ-12 |
| BR-S2E8-17 | Hết lượt: câu đánh dấu "xem lại cùng thầy cô", khóa nhập, vẫn xem được gợi ý mức 1; lời giải và đáp số vẫn khóa tới `release_at`; được sang câu kế | STAGES Ch2 mục 5, mục 7; DQ-12 |
| BR-S2E8-18 | Tiến độ lưu ở máy chủ: thoát rồi vào lại thì quay đúng câu và đúng số lượt còn lại; câu đã Đúng hoặc hết lượt không làm lại | STAGES Ch2 mục 5 |
| BR-S2E8-19 | Chỉ học sinh có `ClassMembership` Đang học ở lớp được giao mới mở bài; bài đã đóng thì chỉ xem kết quả | STAGES mục 0.2 nguyên tắc 5; PO-66 (G1) |
| BR-S2E8-20 | Mất mạng khi gửi: hiện "Chưa gửi được, đáp số của em vẫn còn" và nút "Thử lại", không trừ lượt, giữ nội dung ô; đang tải: khung giữ chỗ (đã chốt PO-66, DQ.md "Chưa vẽ") | STAGES Ch2 mục 6 trạng thái cần thiết kế |

- **Dữ liệu:** BL-1 gồm 5 câu (FX-20), An thuộc lớp 11A1 được giao BL-1; câu 2 là FX-14 kiểu xấp xỉ 45 ±0,5°, 3 lượt; gợi ý mức 1 của câu 2: "Góc giữa SC và mặt đáy là góc giữa SC và hình chiếu của nó lên đáy. Hình chiếu của SC là đoạn nào?"

### Tiêu chí chấp nhận

**US-S2E8-03-SC1 — Mở bài hiện câu 1 với đủ thành phần** · Nguồn: BR-S2E8-15; DQ-12
- Given: An mở BL-1 lần đầu trên iPad
- When: Mở bài
- Then: Hiện "Câu 1/5", chip "Còn 3/3 lượt", thanh tiến độ 5 đoạn (đoạn 1 vàng, 4 đoạn xám), hình, ô đáp số trống, bàn phím toán

**US-S2E8-03-SC2 — Đáp số đúng thì xác nhận và sang câu kế** · Nguồn: BR-S2E8-16
- Given: An ở câu 2 (đáp số 45 ±0,5°)
- When: Gõ 45; bấm "Kiểm tra"; bấm "Câu kế"
- Then: Hiện "Đúng rồi!"; đoạn 2 của thanh tiến độ thành xanh; sau "Câu kế" mở "Câu 3/5" với chip "Còn 3/3 lượt"

**US-S2E8-03-SC3 — Đáp số sai hiện gợi ý mức 1 và trừ lượt** · Nguồn: BR-S2E8-16; DQ-12
- Given: An ở câu 2, lượt 3/3
- When: Gõ 60°; bấm "Kiểm tra"
- Then: Hiện "Chưa đúng. Còn 2 lượt." và gợi ý mức 1 (hình chiếu của SC); chip "Còn 2/3 lượt"; không đáp số; ô đáp số viền đỏ; giữ "60°" trong ô

**US-S2E8-03-SC4 — Sai cú pháp báo chỗ sai và không trừ lượt** · Nguồn: BR-S2E8-16; STAGES Ch2 mục 5
- Given: An ở câu 2, lượt 2/3
- When: Gõ "√"; bấm "Kiểm tra"
- Then: Hiện "Đáp số chưa viết đúng: thiếu số sau dấu căn (ký tự 1). Lượt thử chưa bị trừ."; chip vẫn "Còn 2/3 lượt"; không gợi ý mới

**US-S2E8-03-SC5 — Hết lượt đánh dấu xem lại cùng thầy cô và khóa nhập** · Nguồn: BR-S2E8-17; DQ-12
- Given: An ở câu 2 đã Sai 2 lần (còn 1 lượt)
- When: Gõ 70; bấm "Kiểm tra"
- Then: Hiện "Chưa đúng. Em xem lại cùng thầy cô nhé."; chip "Còn 0/3 lượt"; câu đánh dấu "xem lại cùng thầy cô"; ô đáp số và "Kiểm tra" khóa; nút "Câu kế" mở; lời giải vẫn khóa

**US-S2E8-03-SC6 — Hết lượt vẫn xem được gợi ý mức 1** · Nguồn: BR-S2E8-17; DQ-12
- Given: Câu 2 đã hết lượt
- When: Bấm "Xem gợi ý"
- Then: Gợi ý mức 1 hiện lại; không đáp số

**US-S2E8-03-BV1 — Chip lượt đổi 3, 2, 1, 0 theo từng lần Sai** · Nguồn: BR-S2E8-16; BR-S2E8-17
- Given: Câu 2, 3 lượt
- When: Sai ba lần (60, 61, 62)
- Then: Chip lần lượt "Còn 2/3", "Còn 1/3", "Còn 0/3"; gợi ý hiện ở lần Sai thứ nhất và thứ hai, câu "xem lại cùng thầy cô" ở lần thứ ba

**US-S2E8-03-SC7 — Thoát giữa chừng rồi vào lại giữ trạng thái** · Nguồn: BR-S2E8-18
- Given: An ở câu 2 đã Sai một lần (còn 2 lượt)
- When: Đóng trình duyệt; mở lại và đăng nhập; mở BL-1
- Then: Vào đúng "Câu 2/5", chip "Còn 2/3 lượt"; câu 1 đã xanh; gợi ý đã hiện vẫn hiện

**US-S2E8-03-FC1 — Mất mạng khi gửi không trừ lượt và giữ đáp số** · Nguồn: BR-S2E8-20
- Given: An ở câu 2 lượt 3/3, đã gõ "60"; ngắt mạng
- When: Bấm "Kiểm tra"; bật mạng; bấm "Thử lại"
- Then: Hiện "Chưa gửi được, đáp số của em vẫn còn" và nút "Thử lại"; ô giữ "60"; lượt vẫn 3/3 trước khi gửi lại; sau "Thử lại" trừ đúng một lượt (còn 2/3)

**US-S2E8-03-FC2 — Đang tải hiện khung giữ chỗ** · Nguồn: BR-S2E8-20
- Given: Mạng chậm
- When: Mở BL-1
- Then: Hiện khung giữ chỗ cho hình và bàn phím; ô đáp số chưa nhận chữ khi chưa tải xong; không hiện hình rỗng

**US-S2E8-03-FC3 — Bài của lớp khác không mở được** · Nguồn: BR-S2E8-19
- Given: Học sinh Bình thuộc lớp 11A2 không được giao BL-1
- When: Bình gọi mở BL-1
- Then: NOT_FOUND; không câu hỏi nào trả về

**US-S2E8-03-FC4 — Chưa đăng nhập không mở được bài** · Nguồn: D-GEO-143
- Given: Chưa đăng nhập
- When: Gọi mở BL-1
- Then: UNAUTHENTICATED; không dữ liệu bài

**US-S2E8-03-FC5 — Câu đã đúng không làm lại** · Nguồn: BR-S2E8-18
- Given: Câu 1 đã đúng
- When: Quay lại câu 1; gõ đáp số khác
- Then: Ô khóa; hiện trạng thái đúng của câu 1 (không đổi)

**US-S2E8-03-FC6 — Bài đã đóng chỉ xem kết quả** · Nguồn: BR-S2E8-19
- Given: BL-1 ở trạng thái Đã đóng
- When: An mở BL-1; thử nhập đáp số
- Then: Không nhập được; gọi chấm nhận CONFLICT; chuyển tới màn kết quả

**US-S2E8-03-SC8 — Làm hết 5 câu thì sang kết quả** · Nguồn: BR-S2E8-15; STAGES Ch2 mục 6 (C2-07)
- Given: An ở câu 5/5 vừa xong
- When: Bấm "Xem kết quả"
- Then: Mở màn kết quả bài luyện (US-S2E8-04)

**US-S2E8-03-BV2 — Bài 1 câu: tiến độ một đoạn, câu cuối** · Nguồn: BR-S2E8-15
- Given: Bài luyện 1 câu
- When: Mở bài; làm xong
- Then: "Câu 1/1"; thanh một đoạn; nút ở cuối ghi "Xem kết quả"

**US-S2E8-03-BV3 — Bài 20 câu thanh tiến độ 20 đoạn** · Nguồn: BR-S2E8-15
- Given: Bài luyện 20 câu (tối đa của S2E8-05)
- When: Mở bài trên iPhone
- Then: Thanh tiến độ có 20 đoạn, không tràn ngang; "Câu 1/20"

**US-S2E8-03-SC9 — Gợi ý mức 1 không nêu đáp số ở mọi dạng viết** · Nguồn: BR-S2E8-16; STAGES mục 0.2 nguyên tắc 1
- Given: Câu 2 sau khi Sai
- When: Đọc gợi ý và dữ liệu trả về
- Then: Gợi ý không chứa 45, 45°, 90/2, "bốn mươi lăm", "tan = 1" hay bước giải

**US-S2E8-03-SC10 — Bố cục iPhone dùng được** · Nguồn: BR-S2E8-15
- Given: An trên iPhone 390 × 844, câu 2
- When: Làm một lượt Sai
- Then: Hình, ô, chip lượt, gợi ý, bàn phím cùng dùng được; không cuộn ngang; gợi ý có thể cuộn trong khung

- **Mở và quyết định đang dùng:** QA-S2E8-07
- **DoR:** ready for R3, mức P0.

## US-S2E8-04 — Kết quả bài luyện; không hiện số tổng; đáp số khóa tới giờ mở

- **Story:** Là học sinh, tôi muốn xem câu nào đúng và câu nào cần xem lại sau khi làm, để biết chỗ cần hỏi thầy cô mà không bị so sánh bằng điểm · Loại UI · Mức P0 · Khung C2-07
- **Nguồn:** PO-65 (Q6); STAGES Ch2 mục 7; DQ-13
- **Góc nhìn người vận hành:** học sinh dễ so nhau nếu có điểm hoặc xếp hạng, nên không có số tổng, không dùng từ "điểm", không phần trăm (Q6 nghiêng). Đáp số và lời giải khóa tới giờ mở kể cả sau khi hết lượt.
- **Phụ thuộc:** US-S2E8-03, S2-E5 (lời giải)

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S2E8-21 | Màn kết quả chỉ ghi "x câu đúng · y câu cần xem lại"; không có điểm, phần trăm, thang điểm, xếp hạng; giao diện không dùng chữ "điểm" cho bài luyện | PO-65 Q6, DQ-13, STAGES Ch2 mục 7 |
| BR-S2E8-22 | Câu "cần xem lại" là câu hết lượt; câu chưa làm không tính vào hai số, bài chưa xong hiện "Chưa làm xong: còn z câu" | DQ-13 |
| BR-S2E8-23 | Mỗi câu hiện trạng thái và trạng thái lời giải: khóa kèm "Lời giải mở lúc HH:mm, dd/MM/yyyy" khi giờ máy chủ < `release_at` hoặc chưa đặt giờ ("Thầy cô chưa đặt giờ mở"); mở thì xem được | STAGES Ch2 mục 7 |
| BR-S2E8-24 | Đáp số và lời giải của bài luyện không có trong dữ liệu trả về trước `release_at`, kể cả sau khi hết lượt | STAGES Ch2 mục 7 |
| BR-S2E8-25 | Học sinh chỉ xem kết quả của chính mình; không thấy kết quả bạn | STAGES mục 0.2 nguyên tắc 5 |

### Tiêu chí chấp nhận

**US-S2E8-04-SC1 — Làm hết thì hiện số câu đúng và câu cần xem lại** · Nguồn: BR-S2E8-21; DQ-13
- Given: An làm xong BL-1: 3 câu đúng, 2 câu hết lượt
- When: Mở màn kết quả
- Then: Hiện "3 câu đúng · 2 câu cần xem lại"; mỗi câu một dòng trạng thái

**US-S2E8-04-SC2 — Không có điểm, phần trăm hay xếp hạng** · Nguồn: BR-S2E8-21; PO-65 Q6
- Given: Màn kết quả của SC1
- When: Quét chữ trên màn và dữ liệu trả về tìm "điểm", "%", "/5", "hạng", "xếp hạng"
- Then: Không có chuỗi nào trong số đó; không có số tổng kiểu "3/5"; chỉ có hai số: câu đúng và câu cần xem lại

**US-S2E8-04-SC3 — Chưa làm xong thì báo còn bao nhiêu câu** · Nguồn: BR-S2E8-22
- Given: An mới làm 3 câu (2 đúng, 1 hết lượt); còn 2 câu chưa làm
- When: Mở màn kết quả
- Then: Hiện "Chưa làm xong: còn 2 câu"; hai số "2 câu đúng · 1 câu cần xem lại" không gồm câu chưa làm

**US-S2E8-04-SC4 — Lời giải còn khóa thì ghi giờ mở và không có trong dữ liệu** · Nguồn: BR-S2E8-23, BR-S2E8-24
- Given: `release_at` = T0 + 2 giờ; giờ máy chủ T0; An có câu hết lượt
- When: Mở màn kết quả; xem dữ liệu trả về
- Then: Câu hết lượt hiện "Lời giải mở lúc HH:mm, dd/MM/yyyy" (giờ T0 + 2 giờ); không đáp số và không bước giải trong dữ liệu trả về; `Cache-Control: no-store`

**US-S2E8-04-SC5 — Tới giờ mở thì xem được lời giải** · Nguồn: BR-S2E8-23
- Given: Giờ máy chủ ≥ `release_at`
- When: Mở lại màn kết quả; bấm xem lời giải câu 2
- Then: Lời giải hiện đủ bước kèm nhãn hai trục; trạng thái "Lời giải đã mở"

**US-S2E8-04-BV1 — Một giây trước giờ mở còn khóa, đúng giờ mở thì mở** · Nguồn: BR-S2E8-23; M-GEO-13
- Given: `release_at` = R; đồng hồ giả
- When: Gọi lấy lời giải câu 2 ở R - 1 giây; ở R
- Then: R - 1 giây: khóa, không dữ liệu lời giải; R: mở

**US-S2E8-04-SC6 — Chưa đặt giờ mở thì ghi Thầy cô chưa đặt giờ mở** · Nguồn: BR-S2E8-23
- Given: Bài luyện chưa đặt `release_at`
- When: Mở màn kết quả
- Then: Câu hết lượt hiện "Thầy cô chưa đặt giờ mở"; lời giải khóa mãi tới khi có giờ

**US-S2E8-04-FC1 — Học sinh khác không xem kết quả bạn** · Nguồn: BR-S2E8-25
- Given: Kết quả của An; Bình cùng lớp
- When: Bình gọi kết quả của An
- Then: NOT_FOUND; không dữ liệu của An

**US-S2E8-04-FC2 — Chưa làm câu nào** · Nguồn: BR-S2E8-22
- Given: An mở bài rồi thoát, chưa làm câu nào
- When: Mở màn kết quả
- Then: Hiện "Chưa làm xong: còn 5 câu"; "0 câu đúng · 0 câu cần xem lại" hoặc dòng "Em chưa làm câu nào"

**US-S2E8-04-BV2 — Làm đúng cả 5 câu** · Nguồn: BR-S2E8-21
- Given: An làm đúng cả 5 câu
- When: Mở màn kết quả
- Then: Hiện "5 câu đúng · 0 câu cần xem lại"; không câu chúc điểm số, không xếp hạng

**US-S2E8-04-BV3 — Không câu nào đúng** · Nguồn: BR-S2E8-21
- Given: An hết lượt cả 5 câu
- When: Mở màn kết quả
- Then: Hiện "0 câu đúng · 5 câu cần xem lại"; giọng nhẹ, không chữ chê; có dòng "Em hỏi thầy cô nhé" (đã chốt PO-66, DQ-13)

**US-S2E8-04-FC3 — Lời giải không nằm trong bộ nhớ trình duyệt trước giờ mở** · Nguồn: BR-S2E8-24
- Given: Trước `release_at`; An đã mở kết quả
- When: Kiểm bộ nhớ trình duyệt (cache, lưu trữ cục bộ)
- Then: Không có nội dung lời giải hay đáp số của BL-1

**US-S2E8-04-SC7 — Bố cục iPad của kết quả** · Nguồn: DQ-13; BR-S2E8-23
- Given: An mở kết quả trên iPad (ảnh `c2-07-ket-qua.png`)
- When: Xem màn
- Then: Danh sách câu bố cục như ảnh mẫu; nhãn trạng thái đọc rõ; không cuộn ngang

- **Mở và quyết định đang dùng:** không có
- **DoR:** ready for R3, mức P0.

## US-S2E8-05 — Soạn bài luyện; nút "Em nghĩ đáp số này đúng" gửi về giáo viên

- **Story:** Là giáo viên, tôi muốn soạn bài luyện từ đề của mình và nhận báo "chấm sai" từ học sinh, để máy chấm có chỗ sửa khi nó hiểu sai dạng viết · Loại UI · Mức P0 · Khung C2-08
- **Nguồn:** STAGES Ch2 mục 10; DQ-14
- **Góc nhìn người vận hành:** giáo viên chọn 5 đề, đặt số lượt, sai số, giờ mở. Đề chưa có đáp số máy chấm được thì không chọn được. Học sinh nghĩ máy chấm sai bấm "Em nghĩ đáp số này đúng" và giáo viên quyết định. Bài đã giao thì không sửa dưới chân học sinh.
- **Phụ thuộc:** S2-E5 (đề đã duyệt), US-S2E8-02, lớp mẫu 11A1 chuyển từ MVP (US-S2E1-04, BR-S3E1-16); mô hình bộ bài dùng chung với S3-E2 (PO-66 G1, đã đóng QA-S2E8-08)

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S2E8-26 | Soạn bài: tên 1 đến 80 ký tự; từ 1 đến 20 câu; mỗi câu chọn đề, kiểu đáp số (chính xác hoặc xấp xỉ), sai số (chỉ cho kiểu xấp xỉ, từ 0 đến 10, tối đa 2 chữ số thập phân), số lượt thử từ 1 đến 5 (mặc định 3), giờ mở lời giải (mặc định chưa đặt, tức khóa) | STAGES Ch2 mục 6 (C2-08), DQ-12 |
| BR-S2E8-27 | Đề chưa có đáp số máy chấm được hiện trong danh sách nhưng không chọn được: viền nét đứt, chip vàng, chỉ dẫn "Thêm đáp số ở Soát lời giải" | DQ-14 |
| BR-S2E8-28 | "Xem trước như học sinh" mở màn làm bài thử; không ghi `Attempt` thật, không trừ lượt, không đổi kết quả | STAGES Ch2 mục 6 |
| BR-S2E8-29 | Giao bài: Nháp thành Đã giao, chụp lại (snapshot) đáp số, sai số, lượt cho từng câu; sau đó không sửa được, trừ hai ngoại lệ có chủ đích: (a) giáo viên chọn "Áp dụng cho lớp" phiên bản lời giải mới ở US-S2E5-05 (snapshot lấy theo phiên bản mới, kết quả đã chấm không tính lại, BR-S2E5-26); (b) giáo viên "Xác nhận đúng" báo của học sinh ở BR-S2E8-31 (thêm một dạng chấp nhận vào câu); đóng bài thành Đã đóng | STAGES Ch2 mục 8; BR-S2E5-23 |
| BR-S2E8-30 | Học sinh bấm "Em nghĩ đáp số này đúng" chỉ khi câu đã bị chấm Sai hoặc hết lượt; mỗi em một báo mỗi câu; có thể kèm ghi chú 0 đến 200 ký tự; báo không đổi lượt hay kết quả | STAGES Ch2 mục 10, M-S2-07 |
| BR-S2E8-31 | Giáo viên thấy báo của học sinh lớp mình kèm đáp số em nhập và dạng chuẩn; chọn "Xác nhận đúng" (câu của em thành Đúng, dạng viết đó thêm vào dạng chấp nhận của câu, đếm vào M-S2-07) hoặc "Giữ nguyên" (đã chốt PO-66, QA-S2E8-09) | M-S2-07 |
| BR-S2E8-32 | **Một mô hình "bộ bài":** bài luyện là một bộ bài (`AssignmentSet`) của S3-E2; mỗi câu là một `AssignmentItem` kèm cấu hình luyện (kiểu đáp số, sai số, lượt, giờ mở lời giải). "Giao" ở S2-E8 chính là giao bộ (BR-S3E2-01), không có bảng bài luyện riêng. Chặng 2 giao cho lớp mẫu 11A1 chuyển từ MVP (có `ClassMembership` Đang học, BR-S3E1-16); chặng 3 thêm lớp thật, nhiều lớp, lịch mở và bảng lớp, không đổi mô hình | PO-66 (G1); RV_s23 B-1 |

### Tiêu chí chấp nhận

**US-S2E8-05-SC1 — Chọn đề có đáp số và tạo bài nháp** · Nguồn: BR-S2E8-26
- Given: Cô Trang có 5 đề đã duyệt có đáp số máy chấm được
- When: Mở "Soạn bài luyện"; tick 5 đề; đặt tên "Góc trong hình chóp"; bấm "Lưu nháp"
- Then: Tạo bài nháp 5 câu; mỗi câu mặc định 3 lượt, giờ mở chưa đặt; trạng thái Nháp

**US-S2E8-05-SC2 — Đề chưa có đáp số máy chấm hiện nhưng không chọn được** · Nguồn: BR-S2E8-27; DQ-14
- Given: Có 1 đề chưa có đáp số máy chấm được
- When: Mở danh sách đề
- Then: Đề viền nét đứt, chip vàng; không tick được; chỉ dẫn "Thêm đáp số ở Soát lời giải"

**US-S2E8-05-SC3 — Đặt kiểu xấp xỉ, sai số, lượt và giờ mở** · Nguồn: BR-S2E8-26
- Given: Bài nháp câu 2
- When: Chọn "xấp xỉ"; sai số 0,5; lượt 3; giờ mở T0 + 2 giờ; bấm "Lưu nháp"
- Then: Câu 2 lưu kiểu xấp xỉ, ±0,5, 3 lượt, giờ mở T0 + 2 giờ

**US-S2E8-05-SC4 — Xem trước như học sinh không ghi gì** · Nguồn: BR-S2E8-28
- Given: Bài nháp 5 câu
- When: Bấm "Xem trước như học sinh"; làm sai câu 2 hai lần; thoát
- Then: Hiện màn làm bài giống học sinh; chip lượt đổi trong phiên xem trước; không có `Attempt` thật; mở lại xem trước thì lượt về đủ

**US-S2E8-05-SC5 — Giao bài chụp lại cấu hình từng câu** · Nguồn: BR-S2E8-29
- Given: Bài nháp đầy đủ; lớp 11A1
- When: Bấm "Giao"; xác nhận
- Then: Bài thành Đã giao; mỗi câu lưu snapshot đáp số, kiểu, sai số, lượt; học sinh 11A1 thấy bài

**US-S2E8-05-SC10 — Bài luyện được lưu thành bộ bài dùng chung mô hình với S3-E2** · Nguồn: BR-S2E8-32; PO-66 (G1)
- Given: Bài nháp 5 câu (FX-20) của giáo viên chủ lớp 11A1 (lớp mẫu chuyển từ MVP)
- When: Bấm "Giao"; xác nhận
- Then: Hệ thống tạo đúng một `AssignmentSet` Đã giao có 5 `AssignmentItem` kèm cấu hình luyện từng câu; không có bảng bài luyện riêng; bộ gắn với lớp 11A1

**US-S2E8-05-FC10 — Giáo viên không phải chủ lớp mẫu không giao được cho lớp đó** · Nguồn: BR-S2E8-32; BR-S3E1-16; PO-66 (G1)
- Given: Cô Trang (FX-01) không phải chủ lớp mẫu 11A1; bài nháp đầy đủ
- When: Cô Trang gọi giao bài cho lớp 11A1
- Then: NOT_FOUND; không có `AssignmentSet` nào được tạo; học sinh 11A1 không thấy bài

**US-S2E8-05-FC1 — Bài đã giao không sửa được** · Nguồn: BR-S2E8-29
- Given: Bài đã giao
- When: Gọi API đổi sai số của câu 2
- Then: CONFLICT "Bài đã giao không sửa được"; snapshot không đổi

**US-S2E8-05-FC2 — Sửa đáp số của đề gốc không đổi snapshot bài đã giao** · Nguồn: BR-S2E8-29
- Given: Bài đã giao câu 2 snapshot 45; giáo viên sửa đề gốc thành đáp số 60
- When: An gửi "45"
- Then: Chấm theo snapshot 45: Đúng

**US-S2E8-05-BV1 — Tên bài 80 ký tự nhận, 81 bị chặn, rỗng bị chặn** · Nguồn: BR-S2E8-26
- Given: Soạn bài
- When: Lưu với tên 80 ký tự; tên 81 ký tự; tên rỗng
- Then: 80: lưu được; 81: lỗi "Tên tối đa 80 ký tự"; rỗng: lỗi "Hãy đặt tên bài"

**US-S2E8-05-BV2 — Số câu 1 và 20 nhận, 0 và 21 bị chặn** · Nguồn: BR-S2E8-26
- Given: Soạn bài
- When: Lưu bài 1 câu; 20 câu; 0 câu; 21 câu
- Then: 1 và 20: lưu được; 0: lỗi "Chọn ít nhất 1 câu"; 21: lỗi "Tối đa 20 câu"

**US-S2E8-05-BV3 — Số lượt 1 và 5 nhận, 0 và 6 bị chặn** · Nguồn: BR-S2E8-26
- Given: Câu của bài nháp
- When: Đặt lượt 1; 5; 0; 6
- Then: 1 và 5: lưu được; 0 và 6: VALIDATION_FAILED "Số lượt từ 1 đến 5"

**US-S2E8-05-BV4 — Sai số 0 và 10 nhận, âm và 10,01 bị chặn** · Nguồn: BR-S2E8-26; QA-S2E8-05
- Given: Câu kiểu xấp xỉ
- When: Đặt sai số 0; 10; -0,1; 10,01; 0,555
- Then: 0 và 10: lưu được; -0,1, 10,01: VALIDATION_FAILED; 0,555 (ba chữ số thập phân): VALIDATION_FAILED "Tối đa 2 chữ số thập phân"

**US-S2E8-05-FC3 — Sai số không áp dụng cho kiểu chính xác** · Nguồn: BR-S2E8-26
- Given: Câu kiểu chính xác
- When: Thử nhập sai số 0,5 qua API
- Then: Trường sai số bị khóa trên giao diện; API bỏ qua hoặc từ chối; kết quả chấm không đổi theo

**US-S2E8-05-FC4 — Giờ mở đã qua cần xác nhận** · Nguồn: BR-S2E8-26
- Given: Giờ máy chủ T0
- When: Đặt giờ mở T0 - 1 giờ; bấm "Giao"
- Then: Hiện cảnh báo "Giờ mở đã qua, lời giải mở ngay cho học sinh. Vẫn giao?"; chỉ khi xác nhận mới giao

**US-S2E8-05-SC6 — Học sinh gửi báo Em nghĩ đáp số này đúng sau khi bị chấm Sai** · Nguồn: BR-S2E8-30; STAGES Ch2 mục 10
- Given: An đã nộp "44,4" ở câu 2 và bị chấm Sai (đáp số mẫu 45, sai số ±0,5)
- When: Bấm "Em nghĩ đáp số này đúng"; gõ ghi chú "Em đo ra 44,4"; gửi
- Then: Hiện "Đã gửi cho thầy cô"; báo ghi đáp số "44,4", ghi chú, giờ; lượt và kết quả câu không đổi

**US-S2E8-05-FC5 — Nút báo không hiện khi chưa bị chấm Sai** · Nguồn: BR-S2E8-30
- Given: An ở câu 2 chưa gửi lần nào
- When: Xem màn làm bài
- Then: Không có nút "Em nghĩ đáp số này đúng"; gọi API gửi báo nhận CONFLICT

**US-S2E8-05-FC6 — Mỗi em một báo mỗi câu** · Nguồn: BR-S2E8-30
- Given: An đã báo câu 2
- When: Báo lần hai cho câu 2
- Then: CONFLICT "Em đã báo câu này"; còn đúng một báo

**US-S2E8-05-BV5 — Ghi chú 200 ký tự nhận, 201 bị chặn, rỗng được** · Nguồn: BR-S2E8-30
- Given: An đã bị chấm Sai ở câu 2 và câu 3
- When: Báo câu 2 với ghi chú 200 ký tự; báo câu 3 với ghi chú 201 ký tự; báo một câu khác không ghi chú
- Then: 200 ký tự: gửi được; 201 ký tự: VALIDATION_FAILED "Ghi chú tối đa 200 ký tự"; không ghi chú: gửi được

**US-S2E8-05-SC7 — Giáo viên thấy báo kèm đáp số và dạng chuẩn** · Nguồn: BR-S2E8-31
- Given: An đã gửi báo cho câu 2
- When: Cô Trang mở danh sách "Báo chấm sai"
- Then: Hiện dòng: tên An (mã `HS-3F8A-2L6P`), câu 2, đáp số em nhập "44,4", ghi chú, dạng chuẩn "45°"; hai nút "Xác nhận đúng", "Giữ nguyên"

**US-S2E8-05-SC8 — Xác nhận đúng thì câu của em thành Đúng và đếm vào chỉ số** · Nguồn: BR-S2E8-31; M-S2-07; QA-S2E8-09
- Given: Báo của An đang chờ
- When: Cô Trang bấm "Xác nhận đúng"
- Then: Câu 2 của An thành Đúng; dạng viết "44,4" thêm vào danh sách chấp nhận của câu; chỉ số M-S2-07 tăng 1; An thấy kết quả đã đổi ở lần mở sau

**US-S2E8-05-SC9 — Giữ nguyên thì kết quả không đổi** · Nguồn: BR-S2E8-31
- Given: Báo của An đang chờ
- When: Cô Trang bấm "Giữ nguyên"
- Then: Báo chuyển "Đã xem, giữ nguyên"; kết quả câu 2 của An không đổi; M-S2-07 không đổi

**US-S2E8-05-FC7 — Giáo viên khác không thấy báo của lớp mình** · Nguồn: BR-S2E8-31; BR-S2-06
- Given: Báo của An cho bài của Cô Trang; Cô Lan đăng nhập
- When: Cô Lan gọi danh sách báo; gọi xác nhận một báo
- Then: Danh sách không có báo của An; xác nhận nhận NOT_FOUND

**US-S2E8-05-FC8 — Học sinh không gửi báo cho bài không thuộc mình** · Nguồn: BR-S2E8-30
- Given: Bình (11A2) không được giao BL-1
- When: Bình gọi gửi báo cho câu 2 của BL-1
- Then: NOT_FOUND; không báo nào được tạo

**US-S2E8-05-FC9 — Báo không làm lộ đáp số cho học sinh** · Nguồn: BR-S2E8-30; STAGES mục 0.2 nguyên tắc 1
- Given: An vừa gửi báo
- When: Xem phản hồi và màn sau khi gửi
- Then: Chỉ "Đã gửi cho thầy cô"; không đáp số, không dạng chuẩn, không đúng sai của đáp số em

- **Mở và quyết định đang dùng:** QA-S2E8-05, QA-S2E8-09 (QA-S2E8-08 đã chốt PO-66 G1)
- **DoR:** ready for R3, mức P0.

## Kiểm phủ

- Story trong epic: 5; tiêu chí: 112. Mọi story của epic trong `story-map-s23.md` đều có mặt, không story thừa.
- Câu hỏi mở và đề xuất đổi story map: `questions.md` cùng thư mục (không sửa story map, R2 đã khóa).

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: 5 story, 110 tiêu chí, ready for R3 | runner-T-261126-12 (BA) |
| v0.2 | 2026-11-26 | Sửa theo RV_s23: BR-S2E8-29 nêu hai ngoại lệ của snapshot (mâu thuẫn với US-S2E5-05, BR-S2E8-31); BR-S2E8-08 xử lý đáp số mẫu bằng 0; đổi mã mẫu `HS-`/`GV-` có ký tự 0, 1, O, I sang ký tự hợp lệ (BR-S2E1-04), RV_s23 m-01 | runner-T-261126-15 (QC soát phản biện, RV_s23) |
| v0.3 | 2026-11-26 | G1: BR-S2E8-32 một mô hình bộ bài, giao cho lớp mẫu 11A1; thêm SC10, FC10; bỏ nhãn giả định chưa chốt (PO-66) | runner-T-261126-37 (BA/QC) |
