Phiên bản: v0.3 · 2026-11-26
# S2-E3 — Thêm đề bằng chữ hoặc ảnh, bốn bước đọc–dựng–giải–kiểm và ngoại lệ: story Dev-Ready và tiêu chí chấp nhận

> Chủ sở hữu: BA (goal run T-261126-12, BA + QC liền). Nguồn: `artifacts/GEO/work/s23/story-map-s23.md` (R2 khóa 26/11/2026), `brief-s23.md`, `brd-delta-s23.md`, `STAGES-AFTER-MVP.md` Chặng 2 mục 5–8 (22/11/2026), `Design/s23/DQ.md` và ảnh `Design/s23/c2-*.png` (26/11/2026), `PO-DECISIONS.md` PO-61…65 (PO-61 mới nhất, thắng mọi nguồn cũ).
> **Trạng thái: ready for R3** · Mức epic: **P0** · Bốn story: dán chữ, tải ảnh, bốn bước có màn chờ, ngoại lệ (engine không dựng, lệch đáp số). Khung C2-02 (ảnh `c2-02-*.png`, DQ-01…DQ-04).

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
| bốn bước | đọc đề, dựng hình, giải, kiểm chứng; mỗi bước có trạng thái chờ / đang chạy / xong / lỗi | STAGES Ch2 mục 5 luồng A |
| ô chưa chắc | ô chữ do máy đọc từ ảnh có độ tin cậy dưới 0,80, tô vàng để giáo viên sửa | PO-65 Q4, DQ-02 |

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

## US-S2E3-01 — Thêm đề bằng dán chữ, chép lại để sửa

- **Story:** Là giáo viên, tôi muốn dán chữ của đề vào Kho đề, để máy chép lại cho tôi sửa trước khi dựng hình · Loại UI · Mức P0 · Khung C2-02
- **Nguồn:** STAGES Ch2 mục 5 luồng A; DQ-01
- **Góc nhìn người vận hành:** giáo viên dán từ Word hoặc Zalo, thường dính ký hiệu lạ (√, ⊥, °), xuống dòng thừa, đôi khi dán cả trang. Ca đáng kể: ô trống; đề dài quá; ký tự điều khiển hoặc mã HTML; rời màn giữa chừng.
- **Phụ thuộc:** S2-E1 (đăng nhập)

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S2E3-01 | Thêm đề là một màn có hai tab "Dán chữ" và "Tải ảnh" (đã chốt PO-66, DQ-01) | DQ-01 |
| BR-S2E3-02 | Đề dán từ 10 đến 2000 ký tự sau khi cắt khoảng trắng đầu cuối (đã chốt PO-66, QA-S2E3-01); ngoài khoảng đó không cho bấm "Dựng hình" | BR-S2-03 |
| BR-S2E3-03 | Chữ hiển thị luôn là văn bản thuần: thẻ HTML và lệnh script được hiện nguyên chữ, không thực thi | D-GEO-143 |
| BR-S2E3-04 | Đề đang gõ được lưu thành nháp chữ trên máy chủ; rời màn rồi quay lại vẫn thấy | DQ-03 |
| BR-S2E3-05 | Mỗi tài khoản thêm tối đa 20 đề mỗi giờ; quá thì RATE_LIMITED (đã chốt PO-66, QA-S2E3-02; luật chung ở US-S2E6-03) | PO-63 |

### Tiêu chí chấp nhận

**US-S2E3-01-SC1 — Dán chữ rồi thấy đề đã chép lại để sửa** · Nguồn: STAGES Ch2 mục 5 luồng A
- Given: Cô Trang ở Kho đề, bấm "Thêm đề", tab "Dán chữ"
- When: Dán đề của FX-10 ("Tam giác ABC có AB = 5, AC = 12, BC = 13; tính góc A")
- Then: Hiện ô "Đề đã chép lại" chứa đúng chữ đã dán, sửa được; có nút "Dựng hình"

**US-S2E3-01-SC2 — Ký hiệu toán giữ nguyên khi dán** · Nguồn: BR-S2E3-03
- Given: Tab "Dán chữ"
- When: Dán chuỗi "SA ⊥ (ABCD), SA = a√2, góc 45°"
- Then: Chuỗi hiện nguyên vẹn, không đổi ký hiệu, không vỡ chữ

**US-S2E3-01-FC1 — Ô trống không cho dựng** · Nguồn: BR-S2E3-02
- Given: Tab "Dán chữ", ô trống
- When: Bấm "Dựng hình"
- Then: Nút khóa hoặc hiện lỗi "Hãy dán hoặc gõ đề"; không tạo tác vụ dựng

**US-S2E3-01-BV1 — Đề 9 ký tự bị chặn, 10 ký tự được nhận** · Nguồn: BR-S2E3-02
- Given: Tab "Dán chữ"
- When: Gõ đề 9 ký tự; gõ thêm 1 ký tự thành 10
- Then: 9 ký tự: nút "Dựng hình" khóa kèm lỗi "Đề quá ngắn (tối thiểu 10 ký tự)"; 10 ký tự: nút mở

**US-S2E3-01-BV2 — Đề đúng 2000 ký tự được nhận** · Nguồn: BR-S2E3-02; QA-S2E3-01
- Given: Tab "Dán chữ"
- When: Dán đề dài 2000 ký tự
- Then: Nút "Dựng hình" mở; không báo lỗi độ dài

**US-S2E3-01-BV3 — Đề 2001 ký tự bị chặn** · Nguồn: BR-S2E3-02; QA-S2E3-01
- Given: Tab "Dán chữ"
- When: Dán đề dài 2001 ký tự (và gọi API với 2001 ký tự)
- Then: Giao diện: lỗi "Đề tối đa 2000 ký tự", nút khóa; API: VALIDATION_FAILED, không tạo đề

**US-S2E3-01-FC2 — Mã HTML và script hiện thành chữ** · Nguồn: BR-S2E3-03
- Given: Tab "Dán chữ"
- When: Dán chuỗi `<script>alert(1)</script> Tam giác ABC`
- Then: Chuỗi hiện nguyên chữ trong ô; không có hộp thoại; trang không đổi

**US-S2E3-01-SC3 — Nháp chữ còn khi rời màn rồi quay lại** · Nguồn: BR-S2E3-04
- Given: Cô Trang đã gõ nửa đề, chưa bấm "Dựng hình"
- When: Chuyển sang màn "Kho đề" rồi quay lại "Thêm đề"
- Then: Ô "Đề đã chép lại" còn đúng nửa đề đã gõ

**US-S2E3-01-FC3 — Quá giới hạn thêm đề theo giờ** · Nguồn: BR-S2E3-05; PO-63
- Given: Cô Trang đã tạo 20 đề trong giờ này
- When: Tạo đề thứ 21
- Then: RATE_LIMITED kèm `Retry-After`; giao diện hiện "Bạn thêm đề hơi nhanh, thử lại sau"; không tạo đề

- **Mở và quyết định đang dùng:** QA-S2E3-01, QA-S2E3-02
- **DoR:** ready for R3, mức P0.

## US-S2E3-02 — Thêm đề bằng ảnh; ô chưa chắc tô để giáo viên sửa

- **Story:** Là giáo viên, tôi muốn chụp hoặc tải ảnh đề và sửa chỗ máy đọc chưa chắc, để không phải gõ lại cả đề · Loại UI · Mức P0 · Khung C2-02
- **Nguồn:** PO-65 (Q4, Đã chốt PO-66 · vừa); PO-27; PO-38; DQ-02
- **Góc nhìn người vận hành:** ảnh chụp bằng điện thoại, hay mờ, nghiêng, có chữ viết tay. Ca đáng kể: ảnh quá lớn; sai định dạng; máy đọc được 80 % rồi chắc chắn thiếu 20 %; ảnh trắng không có chữ.
- **Phụ thuộc:** S2-E1

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S2E3-06 | Tải một ảnh mỗi lần, định dạng JPG hoặc PNG, tối đa 5 MB (5 242 880 byte) (đã chốt PO-66, QA-S2E3-03) | PO-27, BR-S2-03 |
| BR-S2E3-07 | Máy đọc ảnh bằng Claude vision (giả lập trong kiểm thử); ô có độ tin cậy dưới 0,80 tô vàng; từ 0,80 trở lên không tô | PO-65 Q4, DQ-02 |
| BR-S2E3-08 | Nút "Xác nhận chữ và dựng tiếp" mở khi mọi ô vàng đã được sửa hoặc bấm "đúng"; nhãn máy giữ "Cần xem lại" (PO-38); chưa xác nhận thì vẫn lưu được nháp | DQ-02, PO-38 |
| BR-S2E3-09 | Không giữ ảnh gốc sau khi chữ được xác nhận; ảnh lưu tạm bị xóa siêu dữ liệu (vị trí, thiết bị) ngay lúc tải (đã chốt PO-66, QA-S2E3-04) | PO-61 |
| BR-S2E3-10 | Ảnh không đọc được chữ nào thì yêu cầu giáo viên gõ lại phần thiếu, máy không tự đoán | STAGES Ch2 mục 5 ngoại lệ |

### Tiêu chí chấp nhận

**US-S2E3-02-SC1 — Tải ảnh rõ thì hiện chữ đã đọc không có ô vàng** · Nguồn: DQ-01; PO-27
- Given: Cô Trang ở tab "Tải ảnh"; ảnh JPG rõ của đề FX-10 (mọi ô tin cậy ≥ 0,95)
- When: Tải ảnh
- Then: Hiện chữ đã đọc trong ô sửa được; không có ô vàng; nút "Dựng hình" mở

**US-S2E3-02-SC2 — Ô chưa chắc tô vàng và nút xác nhận khóa** · Nguồn: DQ-02; PO-65
- Given: Ảnh mờ có 2 ô tin cậy 0,60 và 0,79, các ô khác ≥ 0,80
- When: Tải ảnh
- Then: Hai ô được tô vàng; nút "Xác nhận chữ và dựng tiếp" khóa; nhãn máy "Cần xem lại"

**US-S2E3-02-BV1 — Ô tin cậy 0,80 không tô, 0,79 tô** · Nguồn: BR-S2E3-07
- Given: Ảnh giả lập có ô A tin cậy 0,80 và ô B tin cậy 0,79
- When: Tải ảnh
- Then: Ô A không tô vàng; ô B tô vàng

**US-S2E3-02-SC3 — Sửa hết ô vàng mới xác nhận được** · Nguồn: BR-S2E3-08; DQ-02
- Given: Hai ô vàng chưa xử lý
- When: Sửa ô 1; bấm "đúng" ở ô 2; bấm "Xác nhận chữ và dựng tiếp"
- Then: Sau bước 2 nút mở; sau xác nhận hiện đề đã xác nhận, nhãn máy vẫn "Cần xem lại" cho tới khi bước kiểm đạt (PO-38)

**US-S2E3-02-SC4 — Chưa xác nhận ô vàng vẫn lưu được nháp** · Nguồn: PO-38; BR-S2E3-08
- Given: Hai ô vàng chưa xử lý
- When: Bấm "Lưu nháp"; mở lại nháp
- Then: Nháp lưu với 2 ô vàng còn nguyên; không dựng hình; chưa vào ngăn "Của tôi" như đề đã duyệt

**US-S2E3-02-FC1 — Ảnh không có chữ thì yêu cầu gõ lại, không tự đoán** · Nguồn: BR-S2E3-10; STAGES Ch2 mục 5
- Given: Ảnh trắng (không có chữ nào)
- When: Tải ảnh
- Then: Hiện "Máy không đọc được chữ trong ảnh, hãy gõ lại đề"; ô sửa để trống; không có đề đoán

**US-S2E3-02-BV2 — Ảnh đúng 5 MB được nhận** · Nguồn: BR-S2E3-06; QA-S2E3-03
- Given: Tab "Tải ảnh"
- When: Tải ảnh JPG dài đúng 5 242 880 byte
- Then: Nhận ảnh, tiếp tục đọc chữ

**US-S2E3-02-BV3 — Ảnh 5 MB cộng 1 byte bị chặn** · Nguồn: BR-S2E3-06; QA-S2E3-03
- Given: Tab "Tải ảnh"
- When: Tải ảnh dài 5 242 881 byte
- Then: Hiện "Ảnh tối đa 5 MB"; API trả PAYLOAD_TOO_LARGE; không đọc chữ

**US-S2E3-02-FC2 — Sai định dạng ảnh bị chặn** · Nguồn: BR-S2E3-06
- Given: Tab "Tải ảnh"
- When: Tải tệp `.gif`; tải tệp `.pdf` đổi đuôi thành `.jpg`
- Then: Cả hai bị từ chối; hiện "Chỉ nhận ảnh JPG hoặc PNG"; tệp đổi đuôi bị nhận ra theo nội dung

**US-S2E3-02-FC3 — Chọn hai ảnh một lúc chỉ nhận ảnh đầu** · Nguồn: BR-S2E3-06; QA-S2E3-03
- Given: Tab "Tải ảnh"
- When: Chọn hai ảnh cùng lúc
- Then: Chỉ ảnh thứ nhất được đọc; hiện "Mỗi lần tải một ảnh"

**US-S2E3-02-SC5 — Siêu dữ liệu ảnh bị xóa và ảnh gốc không giữ sau xác nhận** · Nguồn: BR-S2E3-09; PO-61
- Given: Ảnh có siêu dữ liệu vị trí; đề đã xác nhận
- When: Tải ảnh; xác nhận chữ; kiểm kho lưu trữ
- Then: Bản lưu tạm không có siêu dữ liệu vị trí; sau xác nhận không còn tệp ảnh gốc

- **Mở và quyết định đang dùng:** QA-S2E3-03, QA-S2E3-04
- **DoR:** ready for R3, mức P0.

## US-S2E3-03 — Bốn bước đọc–dựng–giải–kiểm với màn chờ từng bước

- **Story:** Là giáo viên, tôi muốn thấy máy đang làm bước nào khi dựng đề, để biết còn chờ bao lâu và bước nào lỗi · Loại Logic · Mức P0 · Khung C2-02
- **Nguồn:** PO-27, PO-30; DQ-01; STAGES Ch2 mục 5 luồng A
- **Góc nhìn người vận hành:** bước giải gọi AI có thể chậm; giáo viên có thể đóng tab giữa chừng hoặc bấm hai lần. AI là giả lập trong kiểm thử (D-GEO-047).
- **Phụ thuộc:** US-S2E3-01, US-S2E3-02, S2-E2

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S2E3-11 | Bốn bước chạy tuần tự: Đọc đề, Dựng hình, Giải, Kiểm chứng; mỗi bước có trạng thái chờ, đang chạy, xong, lỗi; bước lỗi dừng các bước sau | STAGES Ch2 mục 5 |
| BR-S2E3-12 | Mỗi bước có hạn 30 giây (đã chốt PO-66, QA-S2E3-05); quá hạn thì bước lỗi TIMEOUT, có nút "Thử lại bước này", kết quả các bước đã xong được giữ | D-GEO-143 |
| BR-S2E3-13 | Tác vụ dựng chạy ở máy chủ; đóng tab rồi mở lại thấy đúng tiến trình; bấm "Dựng hình" hai lần chỉ tạo một tác vụ | BR-S2-03 |
| BR-S2E3-14 | Kết quả cuối: hình, lời giải nháp, nhãn hai trục. Trục người "AI soạn". Trục máy "Đã kiểm chứng" chỉ khi bước Kiểm chứng đạt, ngược lại "Cần xem lại" | PO-02, PO-30 |

### Tiêu chí chấp nhận

**US-S2E3-03-SC1 — Bốn bước chạy đúng thứ tự và kết quả đủ** · Nguồn: STAGES Ch2 mục 5
- Given: Đề FX-10 đã chép lại; AI giả lập trả lời đúng
- When: Bấm "Dựng hình"
- Then: Màn chờ hiện 4 bước theo thứ tự Đọc đề, Dựng hình, Giải, Kiểm chứng, mỗi bước chuyển chờ - đang - xong; kết thúc hiện hình, lời giải nháp, nhãn "Đã kiểm chứng" và "AI soạn"

**US-S2E3-03-SC2 — Kiểm chứng không đạt thì nhãn máy Cần xem lại** · Nguồn: BR-S2E3-14
- Given: AI giả lập trả lời bước giải thiếu một bước; engine kiểm không đạt
- When: Bấm "Dựng hình"
- Then: Kết thúc với nhãn "Cần xem lại" và "AI soạn"; không có nhãn "Đã kiểm chứng"

**US-S2E3-03-FC1 — Bước lỗi dừng các bước sau** · Nguồn: BR-S2E3-11
- Given: AI giả lập báo lỗi ở bước Dựng hình
- When: Bấm "Dựng hình"
- Then: Bước Dựng hình ghi "lỗi"; hai bước sau giữ "chờ" (không chạy); có nút "Thử lại bước này"

**US-S2E3-03-BV1 — Bước chạy 30 giây còn đạt, 31 giây quá hạn** · Nguồn: BR-S2E3-12; QA-S2E3-05
- Given: AI giả lập trả lời sau 30 giây (lần 1) và 31 giây (lần 2), đồng hồ giả
- When: Chạy bước "Giải" hai lần
- Then: Lần 1 (30 giây): bước xong; lần 2 (31 giây): bước lỗi TIMEOUT, có "Thử lại bước này", hai bước đã xong trước đó giữ kết quả

**US-S2E3-03-SC3 — Thử lại bước lỗi giữ kết quả các bước đã xong** · Nguồn: BR-S2E3-12
- Given: Bước Giải vừa TIMEOUT; các bước Đọc đề, Dựng hình đã xong
- When: Bấm "Thử lại bước này" khi AI trả lời bình thường
- Then: Chỉ bước Giải chạy lại; hai bước đầu không chạy lại (không gọi lại AI cho chúng); tiến trình đi tiếp tới Kiểm chứng

**US-S2E3-03-SC4 — Đóng tab rồi mở lại vẫn thấy tiến trình** · Nguồn: BR-S2E3-13
- Given: Tác vụ dựng đang ở bước Giải
- When: Đóng tab; mở lại "Thêm đề" sau 10 giây
- Then: Hiện đúng tiến trình (bước Giải đang chạy hoặc đã xong); không tạo tác vụ thứ hai

**US-S2E3-03-FC2 — Bấm Dựng hình hai lần chỉ tạo một tác vụ** · Nguồn: BR-S2E3-13
- Given: Đề đã chép lại
- When: Bấm "Dựng hình" hai lần trong 1 giây
- Then: Chỉ một tác vụ được tạo; AI giả lập chỉ nhận một lần gọi cho mỗi bước

**US-S2E3-03-SC5 — Hủy tác vụ đang chạy** · Nguồn: BR-S2E3-11
- Given: Tác vụ đang ở bước Dựng hình
- When: Bấm "Hủy"
- Then: Tác vụ dừng; trạng thái "đã hủy"; đề vẫn là nháp chữ; không tạo đề mới trong Kho đề

**US-S2E3-03-FC3 — Đề của người khác không xem được tiến trình** · Nguồn: BR-S2E3-13; D-GEO-143
- Given: Tác vụ dựng của Cô Trang; tài khoản giáo viên thứ hai "Cô Lan"
- When: Cô Lan gọi lấy tiến trình theo mã tác vụ của Cô Trang
- Then: NOT_FOUND (không lộ tác vụ có tồn tại); không dữ liệu của Cô Trang trả về

- **Mở và quyết định đang dùng:** QA-S2E3-05
- **DoR:** ready for R3, mức P0.

## US-S2E3-04 — Ngoại lệ: engine không dựng thì lưu "Cần xem lại"; lệch đáp số thì chặn Duyệt

- **Story:** Là giáo viên, tôi muốn biết rõ khi máy không dựng được hình hoặc đáp số lệch, và có lối đi tiếp, để không phải bỏ đề · Loại Logic · Mức P0 · Khung C2-02, C2-03
- **Nguồn:** STAGES Ch2 mục 5 ngoại lệ; DQ-03, DQ-04
- **Góc nhìn người vận hành:** đề hay hỏng ở hai chỗ: hình quá lạ để máy dựng, hoặc đáp số giáo viên ghi khác đáp số máy tính. Cả hai không được âm thầm bỏ qua và không có đường tắt tới nút Duyệt.
- **Phụ thuộc:** US-S2E3-03, S2-E5 (hộp duyệt)

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S2E3-15 | Engine không dựng được: đề lưu với nhãn máy "Cần xem lại · hình do AI dựng"; ba lối đi: bổ sung điều kiện, dựng tay, lưu nháp chữ (nháp chữ chưa giao được) | DQ-03, STAGES Ch2 mục 5 |
| BR-S2E3-16 | Đề "Cần xem lại · hình do AI dựng" muốn giao bài phải bấm xác nhận riêng ở lúc giao | STAGES Ch2 mục 5 ngoại lệ |
| BR-S2E3-17 | Ba đáp số: do đề ghi, do AI giải, do máy tính lại. Khớp khi hai đáp số bằng nhau về giá trị (dạng viết tương đương tính là bằng) với dung sai tuyệt đối 0,01 (đã chốt PO-66, QA-S2E3-06) | DQ-04 |
| BR-S2E3-18 | Còn lệch thì nút Duyệt khóa; ba cách xử lý: dùng đáp số máy, sửa đáp số đề ghi, sửa bước giải rồi kiểm lại; mọi cách đều kiểm lại và chỉ mở Duyệt khi ba đáp số khớp (đã chốt PO-66, QA-S2E3-06) | DQ-04, STAGES mục 0.2 nguyên tắc 2 |

### Tiêu chí chấp nhận

**US-S2E3-04-SC1 — Engine không dựng thì lưu Cần xem lại với ba lối đi** · Nguồn: DQ-03
- Given: Đề hình lạ ngoài danh sách (như elip) đã qua bước Đọc đề
- When: Chạy bốn bước
- Then: Bước Dựng hình báo "không dựng được"; đề lưu với "Cần xem lại · hình do AI dựng"; hiện ba lối "Bổ sung điều kiện", "Dựng tay", "Lưu bản nháp chữ"

**US-S2E3-04-SC2 — Lưu nháp chữ thì đề chưa giao được** · Nguồn: BR-S2E3-15
- Given: Đề lưu bằng lối "Lưu bản nháp chữ"
- When: Mở Soạn bài giao và tìm đề này
- Then: Đề không chọn được để giao, kèm chú thích "bản nháp chữ"

**US-S2E3-04-SC3 — Giao đề Cần xem lại phải bấm xác nhận riêng** · Nguồn: BR-S2E3-16
- Given: Đề "Cần xem lại · hình do AI dựng" đã lưu
- When: Chọn đề để giao
- Then: Hiện hộp hỏi "Hình do AI dựng, chưa kiểm chứng. Vẫn giao?" với nút xác nhận riêng; không bấm thì không giao

**US-S2E3-04-SC4 — Lệch đáp số hiện bảng đối chiếu và chặn Duyệt** · Nguồn: DQ-04
- Given: Đề FX-10, lời giải AI ghi đáp số 80°, máy tính lại 90°
- When: Mở đề trong hộp duyệt
- Then: Bảng "Đề ghi / AI giải / Máy tính lại" hiện 90° / 80° / 90°; nút "Duyệt lời giải này" khóa kèm "Chặn duyệt: đáp số chưa khớp"; không có đường tắt

**US-S2E3-04-SC5 — Chọn dùng đáp số máy thì kiểm lại và mở Duyệt** · Nguồn: BR-S2E3-18
- Given: Đề lệch như SC4
- When: Chọn "Dùng đáp số máy"; chờ kiểm lại
- Then: Đáp số lời giải thành 90°; ba đáp số khớp; nút Duyệt mở

**US-S2E3-04-SC6 — Sửa đáp số đề ghi cho khớp thì mở Duyệt** · Nguồn: BR-S2E3-18
- Given: Đề lệch: đề ghi 80°, AI và máy 90°
- When: Chọn "Sửa đáp số đề ghi"; nhập 90; bấm "Kiểm tra lại"
- Then: Ba đáp số khớp; Duyệt mở

**US-S2E3-04-FC1 — Không có đường tắt: gọi thẳng API duyệt khi còn lệch** · Nguồn: BR-S2E3-18; STAGES mục 0.2 nguyên tắc 3
- Given: Đề còn lệch
- When: Gọi API Duyệt trực tiếp
- Then: CONFLICT; đề không đổi trạng thái; không có nhãn "Giáo viên đã duyệt"

**US-S2E3-04-BV1 — Lệch 0,01 coi là khớp, lệch 0,02 là lệch** · Nguồn: BR-S2E3-17; QA-S2E3-06
- Given: Hai đề: đáp số đề 6,93 và máy 6,94; đáp số đề 6,93 và máy 6,95
- When: Chạy đối chiếu cho từng đề
- Then: Đề 1 (lệch 0,01): khớp, Duyệt không bị chặn vì lệch; đề 2 (lệch 0,02): lệch, chặn Duyệt

**US-S2E3-04-SC7 — Dạng viết tương đương tính là khớp** · Nguồn: BR-S2E3-17
- Given: Đề FX-13: đề ghi `4√3`, máy tính lại 6,9282
- When: Chạy đối chiếu
- Then: Khớp (4√3 bằng 6,9282 trong dung sai 0,01); không chặn Duyệt

**US-S2E3-04-FC2 — Đề không có đáp số ghi sẵn thì không tính là lệch** · Nguồn: BR-S2E3-17
- Given: Đề không ghi đáp số; AI và máy cùng 90°
- When: Chạy đối chiếu
- Then: Hai đáp số khớp; không chặn; bảng đối chiếu ghi "Đề ghi: chưa có"

- **Mở và quyết định đang dùng:** QA-S2E3-03, QA-S2E3-06
- **DoR:** ready for R3, mức P0.

## Kiểm phủ

- Story trong epic: 4; tiêu chí: 39. Mọi story của epic trong `story-map-s23.md` đều có mặt, không story thừa.
- Câu hỏi mở và đề xuất đổi story map: `questions.md` cùng thư mục (không sửa story map, R2 đã khóa).

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: 4 story, 39 tiêu chí, ready for R3 | runner-T-261126-12 (BA) |
| v0.2 | 2026-11-26 | đổi mã mẫu `HS-`/`GV-` có ký tự 0, 1, O, I sang ký tự hợp lệ (BR-S2E1-04), RV_s23 m-01 | runner-T-261126-15 (QC soát phản biện, RV_s23) |
| v0.3 | 2026-11-26 | Áp PO-66 (gate R3, T-261126-37): bỏ nhãn giả định chưa chốt, ghi "đã chốt PO-66" | runner-T-261126-37 (BA/QC) |
