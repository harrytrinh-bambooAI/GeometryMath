Phiên bản: v0.2 · 2026-11-26
# S2-E5 — Soát và duyệt lời giải có phiên bản, StepCheck, nhãn hai trục, hộp duyệt, lời giải đã giao bất biến: story Dev-Ready và tiêu chí chấp nhận

> Chủ sở hữu: BA (goal run T-261126-12, BA + QC liền). Nguồn: `artifacts/GEO/work/s23/story-map-s23.md` (R2 khóa 26/11/2026), `brief-s23.md`, `brd-delta-s23.md`, `STAGES-AFTER-MVP.md` Chặng 2 mục 5–8 (22/11/2026), `Design/s23/DQ.md` và ảnh `Design/s23/c2-*.png` (26/11/2026), `PO-DECISIONS.md` PO-61…65 (PO-61 mới nhất, thắng mọi nguồn cũ).
> **Trạng thái: ready for R3** · Mức epic: **P0** · Năm story, ưu tiên dồn công cho khung C2-03 (ảnh `c2-03-chua-kiem.png`, `c2-03-lech-chan-duyet.png`, `c2-03-da-giao.png`; DQ-04…DQ-07): AC biên và case biên đầy đủ.

## 0. Reader guide and glossary (hướng dẫn đọc và bảng mã)

**Cách đọc.** Mỗi mục `## US-S2En-nn — …` là một story: câu story, góc nhìn người vận hành, quy tắc nghiệp vụ (BR), tiêu chí chấp nhận (AC). Mỗi AC nói **một** hành vi quan sát được, dạng Given / When / Then, kèm giá trị cụ thể. `SC` = kịch bản chính, `FC` = lỗi hoặc ngoại lệ, `BV` = giá trị biên. Mã chỉ cho biết **chỗ** một điều được ghi, câu chữ nói điều đó là gì.

**Quy ước chung cho mọi AC trong tệp này** (kế thừa MVP, đối chiếu `stories_ac.md` của E1):
- "T0" là giờ máy chủ lúc bắt đầu ca kiểm; mọi mốc giờ tương đối so với T0. Giờ hiển thị `HH:mm, dd/MM/yyyy` giờ Việt Nam; số thập phân hiển thị dấu phẩy.
- Lỗi máy chủ có dạng `{code, message, requestId, details[]}`; `code` ổn định theo D-GEO-143 (VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN, NOT_FOUND, CONFLICT, RATE_LIMITED kèm `Retry-After`).
- Phản hồi chứa lời giải, gợi ý hoặc trạng thái khóa mang `Cache-Control: no-store` (D-GEO-145). "Dữ liệu trả về" là toàn bộ nội dung máy chủ gửi xuống thiết bị, không chỉ phần giao diện vẽ ra.
- Thiết bị theo PO-61 và STAGES mục 0.2: giáo viên dùng web (điện thoại chỉ để liếc); học sinh dùng iPad (chính) và iPhone (phụ) qua trình duyệt (PWA, TẠM, PO-65).
- AI trong kiểm thử là giả lập (D-GEO-047): không có kỳ vọng về chất lượng AI thật. Giá trị trong Given / Then lấy từ bảng `FX-nn` hoặc ghi ngay trong tiêu chí.
- AC mang chữ TẠM dựng trên giả định chưa được owner xác nhận; mã `QA-S2En-nn` hoặc `DQ-nn` đứng cạnh cho biết giả định nào.

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

## US-S2E5-01 — Sửa lời giải từng bước, mỗi lần lưu thành phiên bản mới

- **Story:** Là giáo viên, tôi muốn sửa từng bước của lời giải AI và mỗi lần lưu giữ một phiên bản, để không mất bản cũ và biết ai sửa gì · Loại UI · Mức P0 · Khung C2-03
- **Nguồn:** STAGES Ch2 mục 3 ý 3; BR-S2-05
- **Góc nhìn người vận hành:** giáo viên sửa bước 3 sai của lời giải AI, thêm một bước giải thích, kéo đổi thứ tự; mở hai tab cùng lúc; bấm Lưu khi chưa đổi gì. Bản cũ phải còn xem lại được.
- **Phụ thuộc:** S2-E3 (lời giải nháp), S2-E2

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S2E5-01 | Lời giải là danh sách bước đánh số liên tục từ 1; có từ 1 đến 30 bước; mỗi bước từ 1 đến 2000 ký tự sau khi cắt khoảng trắng đầu cuối (TẠM, QA-S2E5-01) | BR-S2-05 |
| BR-S2E5-02 | Mỗi lần bấm Lưu có thay đổi tạo phiên bản mới vN+1; phiên bản cũ giữ nguyên, không ghi đè; không có thay đổi thì không tạo phiên bản | STAGES Ch2 mục 3 ý 3 |
| BR-S2E5-03 | Lịch sử phiên bản liệt kê mới nhất ở trên: số phiên bản, người sửa (tên và mã), giờ, mô tả ngắn | DQ-06 |
| BR-S2E5-04 | "Dùng bản này" với phiên bản cũ tạo phiên bản mới là bản sao nội dung của bản cũ; lịch sử không bị xóa hay đổi thứ tự | BR-S2-05 |
| BR-S2E5-05 | Lưu có kiểm tra phiên bản gốc (khóa lạc quan): ai lưu từ phiên bản không còn là mới nhất thì nhận CONFLICT, không ghi đè | D-GEO-143 |
| BR-S2E5-06 | Chỉ chủ sở hữu của đề (hoặc người soát nội dung với đề đang chờ soát) sửa được lời giải; người khác nhận NOT_FOUND | BR-S2-06 |

- **Dữ liệu:** lời giải mẫu v1 của FX-14 gồm 4 bước. Bước 1: SA ⊥ (ABCD) nên AC là hình chiếu của SC, góc cần tìm là SCA. Bước 2: ABCD vuông cạnh a nên AC = a√2. Bước 3 (v1 sai): tan SCA = SA / AC = √2. Bước 4: góc bằng 54,7°. Bước 3 đúng: tan SCA = a√2 / a√2 = 1. Bước 4 đúng: góc bằng 45°

### Tiêu chí chấp nhận

**US-S2E5-01-SC1 — Sửa bước 3 rồi lưu thành v2, v1 còn nguyên** · Nguồn: STAGES Ch2 mục 3 ý 3; DQ-06
- Given: Lời giải v1 của FX-14 (bước 3 sai) đang mở trong C2-03
- When: Sửa bước 3 thành "tan SCA = a√2 / a√2 = 1"; bấm "Lưu"
- Then: Có phiên bản v2 mang bước 3 mới; v1 vẫn xem được với bước 3 cũ "= √2"; lịch sử ghi "v2 · bạn sửa bước 3"

**US-S2E5-01-SC2 — Thêm một bước ở giữa thì đánh số lại** · Nguồn: BR-S2E5-01
- Given: Lời giải v2 có 4 bước
- When: Bấm "Thêm bước" sau bước 2; gõ "Vậy tam giác SAC vuông cân tại A"; bấm "Lưu"
- Then: Lời giải 5 bước đánh số lại 1 đến 5; bước mới là bước 3; lưu thành v3

**US-S2E5-01-SC3 — Xóa một bước và kéo đổi thứ tự** · Nguồn: BR-S2E5-01
- Given: Lời giải 5 bước
- When: Xóa bước 3; kéo bước 4 lên trước bước 3; bấm "Lưu"
- Then: Còn 4 bước, đánh số lại 1 đến 4, thứ tự đúng như đã kéo; lưu thành một phiên bản mới (một lần lưu một phiên bản dù nhiều thay đổi)

**US-S2E5-01-SC4 — Lịch sử phiên bản đủ thông tin và mới nhất ở trên** · Nguồn: BR-S2E5-03; DQ-06
- Given: Đề có v1 (AI soạn) và v2 (Cô Trang sửa lúc 11:58)
- When: Mở hộp duyệt
- Then: Hiện v2 ở trên, v1 ở dưới; mỗi dòng có số phiên bản, người sửa, giờ, mô tả ("AI soạn · máy chưa kiểm", "bạn sửa bước 3")

**US-S2E5-01-SC5 — Dùng bản cũ tạo phiên bản mới là bản sao** · Nguồn: BR-S2E5-04
- Given: Đề có v1, v2; đang xem v1 (chỉ đọc)
- When: Bấm "Dùng bản này"
- Then: Tạo v3 có nội dung giống hệt v1; v1 và v2 còn nguyên trong lịch sử; v3 mang mô tả "sao từ v1"

**US-S2E5-01-FC1 — Lưu khi chưa đổi gì không tạo phiên bản** · Nguồn: BR-S2E5-02
- Given: Lời giải v2 vừa mở, chưa sửa gì
- When: Bấm "Lưu"
- Then: Hiện "Chưa có thay đổi để lưu"; vẫn là v2; không có v3

**US-S2E5-01-FC2 — Bước trống bị chặn** · Nguồn: BR-S2E5-01
- Given: Lời giải 4 bước
- When: Xóa hết chữ của bước 3 (hoặc gõ ba dấu cách); bấm "Lưu"
- Then: Hiện lỗi "Bước 3 đang trống"; bước 3 viền đỏ; không tạo phiên bản

**US-S2E5-01-BV1 — Bước đúng 2000 ký tự được lưu** · Nguồn: BR-S2E5-01; QA-S2E5-01
- Given: Lời giải mở
- When: Gõ bước 2 dài 2000 ký tự; bấm "Lưu"
- Then: Lưu thành công, tạo phiên bản mới

**US-S2E5-01-BV2 — Bước 2001 ký tự bị chặn** · Nguồn: BR-S2E5-01; QA-S2E5-01
- Given: Lời giải mở
- When: Gõ bước 2 dài 2001 ký tự; bấm "Lưu" (và gọi API với 2001 ký tự)
- Then: Giao diện: lỗi "Bước tối đa 2000 ký tự"; API: VALIDATION_FAILED; không tạo phiên bản

**US-S2E5-01-BV3 — Lời giải 30 bước được lưu, thêm bước 31 bị chặn** · Nguồn: BR-S2E5-01; QA-S2E5-01
- Given: Lời giải 29 bước
- When: Thêm bước thứ 30 rồi lưu; thêm bước thứ 31
- Then: Bước 30: lưu được; bước 31: nút "Thêm bước" khóa kèm "Tối đa 30 bước"; API: VALIDATION_FAILED

**US-S2E5-01-BV4 — Xóa còn 1 bước được, xóa bước cuối cùng bị chặn** · Nguồn: BR-S2E5-01
- Given: Lời giải 2 bước
- When: Xóa một bước (còn 1); xóa nốt bước còn lại
- Then: Còn 1 bước: lưu được; xóa nốt: lỗi "Lời giải cần ít nhất 1 bước"; không lưu

**US-S2E5-01-FC3 — Hai tab sửa cùng lúc: tab lưu sau nhận CONFLICT** · Nguồn: BR-S2E5-05
- Given: Cô Trang mở lời giải v1 ở tab A và tab B
- When: Tab A sửa bước 3 và lưu thành v2; tab B (vẫn từ v1) sửa bước 4 và bấm "Lưu"
- Then: Tab B nhận CONFLICT, không ghi đè v2; hiện "Có bản mới hơn (v2)" với hai lựa chọn "Tải bản mới" và "Giữ phần sửa của tôi để ghép tay"; v2 nguyên vẹn

**US-S2E5-01-FC4 — Giáo viên khác không sửa được lời giải của đề Của tôi** · Nguồn: BR-S2E5-06
- Given: Đề Của tôi của Cô Trang; giáo viên "Cô Lan" đăng nhập
- When: Cô Lan gọi sửa lời giải đề này
- Then: NOT_FOUND; không lộ đề có tồn tại; phiên bản không đổi

**US-S2E5-01-FC5 — Học sinh không sửa được lời giải** · Nguồn: BR-S2E5-06; PO-63
- Given: Học sinh An (FX-02)
- When: Gọi sửa lời giải một đề
- Then: FORBIDDEN hoặc NOT_FOUND; không phiên bản mới

- **Mở và quyết định đang dùng:** QA-S2E5-01
- **DoR:** ready for R3, mức P0.

## US-S2E5-02 — `StepCheck`: kiểm số từng bước, bước sai tô đỏ kèm lý do

- **Story:** Là giáo viên, tôi muốn máy kiểm số từng bước và chỉ ra bước sai kèm lý do, để duyệt có căn cứ · Loại Logic · Mức P0 · Khung C2-03
- **Nguồn:** STAGES Ch2 mục 3 ý 4; BR-S2-05; DQ-04
- **Góc nhìn người vận hành:** giáo viên bấm "Kiểm tra lại" sau khi sửa. Bước nào có phép tính thì máy kiểm; bước chỉ chữ thì không kiểm được và không bị coi là sai. Lỗi chỉ báo tại bước phát sinh, không lan sang bước sau (TẠM, QA-S2E5-02).
- **Phụ thuộc:** US-S2E5-01, S2-E2

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S2E5-07 | "Kiểm tra lại" chạy engine cho từng bước; mỗi bước có kết quả Đạt, Không đạt hoặc Không kiểm được (bước không có phép tính số) | STAGES Ch2 mục 8 |
| BR-S2E5-08 | Bước kiểm theo giá trị của bước trước trong chính lời giải: bước đạt khi đúng với đầu vào của nó trong dung sai tuyệt đối 0,01 (lỗi báo ở bước phát sinh, không lan); đáp số cuối so với engine là một kiểm tra riêng ở US-S2E3-04 (TẠM, QA-S2E5-02) | DQ-04 |
| BR-S2E5-09 | Bước Không đạt tô đỏ, nhãn "Lệch", dòng lý do nêu giá trị bước ghi và giá trị máy tính | DQ-04, STAGES Ch2 mục 5 luồng A |
| BR-S2E5-10 | `StepCheck` gắn với phiên bản; sửa bước nào thì bước đó và các bước sau của phiên bản mới về "Chưa kiểm", các bước trước giữ kết quả | BR-S2-05 |
| BR-S2E5-11 | Kiểm lời giải tối đa 10 giây; quá thì TIMEOUT, mọi bước "Chưa kiểm" (TẠM, QA-S2E5-04); học sinh không đọc được `StepCheck` | D-GEO-143 |

- **Dữ liệu:** dung sai kiểm tuyệt đối 0,01; ví dụ bước ghi 6,93 và máy tính 6,9282

### Tiêu chí chấp nhận

**US-S2E5-02-SC1 — Bước 3 sai tô đỏ kèm lý do, các bước khác Đạt** · Nguồn: DQ-04; STAGES Ch2 mục 5
- Given: Lời giải v2 của FX-14 với bước 3 "tan SCA = √2"
- When: Bấm "Kiểm tra lại"
- Then: Bước 3 tô đỏ nhãn "Lệch", lý do nêu "ghi √2 ≈ 1,41; máy tính lại 1"; bước 2 Đạt; bước 4 Đạt theo giá trị bước 3 (54,7°); dòng tóm tắt "1 bước lệch"

**US-S2E5-02-SC2 — Sửa đúng bước 3 rồi kiểm lại thì mọi bước Đạt** · Nguồn: BR-S2E5-07
- Given: Lời giải v3 với bước 3 và 4 đúng (tan = 1, góc 45°)
- When: Bấm "Kiểm tra lại"
- Then: Cả bốn bước Đạt; dòng "Máy đã kiểm tra phần tính toán"; không bước đỏ

**US-S2E5-02-SC3 — Bước chỉ chữ là Không kiểm được, không tính là lệch** · Nguồn: BR-S2E5-07
- Given: Bước 1 chỉ có lập luận "Vì SA ⊥ (ABCD) nên AC là hình chiếu"
- When: Bấm "Kiểm tra lại"
- Then: Bước 1 hiện "Không kiểm được" màu xám với lý do "bước không có phép tính"; không tính vào số bước lệch

**US-S2E5-02-BV1 — Lệch 0,0018 (6,93 so với 6,9282) là Đạt** · Nguồn: BR-S2E5-08; QA-S2E5-02
- Given: Bước ghi 6,93; máy tính lại 6,9282
- When: Chạy kiểm bước
- Then: Đạt

**US-S2E5-02-BV2 — Lệch đúng 0,01 là Đạt** · Nguồn: BR-S2E5-08; QA-S2E5-02
- Given: Bước ghi 6,94; máy tính lại 6,93
- When: Chạy kiểm bước
- Then: Đạt (biên, dấu ≤ 0,01)

**US-S2E5-02-BV3 — Lệch 0,011 là Không đạt** · Nguồn: BR-S2E5-08; QA-S2E5-02
- Given: Bước ghi 6,941; máy tính lại 6,93
- When: Chạy kiểm bước
- Then: Không đạt kèm lý do ghi cả hai giá trị

**US-S2E5-02-FC1 — Biểu thức không đọc được là Không kiểm được, không lỗi hệ thống** · Nguồn: BR-S2E5-07
- Given: Bước 3 ghi "tan SCA = abc"
- When: Bấm "Kiểm tra lại"
- Then: Bước 3 "Không kiểm được" với lý do "không đọc được biểu thức"; các bước khác vẫn kiểm; không lỗi 500

**US-S2E5-02-SC4 — Sửa bước 3 thì bước 3 và sau về Chưa kiểm, bước trước giữ Đạt** · Nguồn: BR-S2E5-10
- Given: v2 đã kiểm: bước 1 Không kiểm được, bước 2 Đạt, bước 3 đỏ, bước 4 Đạt
- When: Sửa bước 3 và lưu thành v3
- Then: Ở v3: bước 1 và 2 giữ kết quả cũ; bước 3 và 4 hiện "Chưa kiểm"; v2 giữ kết quả của v2

**US-S2E5-02-SC5 — Xem v1 hiện kết quả kiểm của v1, không phải của v2** · Nguồn: BR-S2E5-10
- Given: v1 chưa kiểm; v2 đã kiểm
- When: Xem v1 rồi xem v2
- Then: v1: mọi bước "Chưa kiểm"; v2: kết quả của v2

**US-S2E5-02-BV4 — Kiểm 10 giây xong, 11 giây quá hạn** · Nguồn: BR-S2E5-11; QA-S2E5-04
- Given: Engine giả lập trả lời sau 10 giây (lần 1) và 11 giây (lần 2), đồng hồ giả
- When: Bấm "Kiểm tra lại" hai lần
- Then: Lần 1: có kết quả; lần 2: TIMEOUT; mọi bước "Chưa kiểm"; có nút "Thử lại"; nhãn máy không đổi sang "Đã kiểm chứng"

**US-S2E5-02-BV5 — Lời giải 30 bước có đủ 30 kết quả** · Nguồn: BR-S2E5-07
- Given: Lời giải 30 bước
- When: Bấm "Kiểm tra lại"
- Then: Có đúng 30 kết quả theo thứ tự bước; không bước nào thiếu

**US-S2E5-02-FC2 — Học sinh không đọc được StepCheck** · Nguồn: BR-S2E5-11
- Given: Học sinh An (FX-02); đề đang giao
- When: Gọi lấy kết quả `StepCheck` của đề
- Then: FORBIDDEN hoặc NOT_FOUND; dữ liệu trả về của mọi yêu cầu của An không chứa `StepCheck`

**US-S2E5-02-FC3 — Bước đầu vào thiếu giá trị bước trước thì không kiểm được** · Nguồn: BR-S2E5-08
- Given: Bước 2 bị xóa; bước 3 dùng AC từ bước 2
- When: Bấm "Kiểm tra lại"
- Then: Bước 3 "Không kiểm được" với lý do "thiếu giá trị đầu vào từ bước trước"; không báo Đạt

- **Mở và quyết định đang dùng:** QA-S2E5-02, QA-S2E5-04
- **DoR:** ready for R3, mức P0.

## US-S2E5-03 — Nhãn hai trục và dòng "Máy đã kiểm tra phần tính toán"; chỉ engine gắn "Đã kiểm chứng"

- **Story:** Là giáo viên, tôi muốn thấy luôn hai nhãn độc lập và biết nhãn nào do máy, nhãn nào do người, để không hiểu nhầm máy đã kiểm cả lập luận · Loại UI · Mức P0 · Khung C2-03
- **Nguồn:** 0.3 ý 2; STAGES Ch2 mục 7; PO-02, PO-38, PO-48; DQ-07
- **Góc nhìn người vận hành:** giáo viên dễ nghĩ bấm Duyệt là máy cũng xác nhận. Nhãn máy chỉ do engine gắn, không có nút nào cho người đổi (PO-02). Bộ câu thử bài sai (M-S2-03) phải không có mẫu nào mang nhãn "Đã kiểm chứng".
- **Phụ thuộc:** US-S2E5-02

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S2E5-12 | Trục máy "Đã kiểm chứng" khi: không bước nào Không đạt, đáp số cuối khớp engine, và có ít nhất một bước Đạt; ngược lại "Cần xem lại" (TẠM, QA-S2E5-05) | PO-02, M-S2-03 |
| BR-S2E5-13 | Trục người: "AI soạn" lúc AI sinh; "Đang sửa" khi giáo viên sửa; "Giáo viên đã duyệt" sau khi duyệt; "Bị thay thế" khi có bản duyệt mới hơn | STAGES Ch2 mục 8 |
| BR-S2E5-14 | Hai nhãn luôn hiện cùng nhau ở đầu thẻ hình và trong hộp duyệt; cạnh "Đã kiểm chứng" luôn có dòng "Máy đã kiểm tra phần tính toán" | DQ-07, STAGES Ch2 mục 10 |
| BR-S2E5-15 | Chỉ engine gắn nhãn máy; không thao tác nào của người dùng đổi được, kể cả sau khi duyệt; duyệt chỉ đổi trục người | STAGES Ch2 mục 7, PO-38 |
| BR-S2E5-16 | Sửa nội dung làm đổi dữ kiện hoặc bước thì nhãn máy của phiên bản mới là "Cần xem lại" tới khi kiểm lại đạt | PO-48 |

### Tiêu chí chấp nhận

**US-S2E5-03-SC1 — Lời giải AI chưa kiểm hiện Cần xem lại và AI soạn cùng nhau** · Nguồn: DQ-07
- Given: Lời giải v1 mới sinh, chưa kiểm
- When: Mở C2-03
- Then: Đầu thẻ hình và hộp duyệt đều hiện "Cần xem lại" (vàng) và "AI soạn" (tím); không có nhãn "Đã kiểm chứng"

**US-S2E5-03-SC2 — Kiểm đạt toàn bộ hiện Đã kiểm chứng kèm dòng chú thích** · Nguồn: BR-S2E5-12; BR-S2E5-14
- Given: Lời giải v3 đúng, đáp số 45° khớp máy, mọi bước phép tính Đạt
- When: Bấm "Kiểm tra lại"
- Then: Nhãn máy "Đã kiểm chứng"; dòng "Máy đã kiểm tra phần tính toán" ngay cạnh nhãn; trục người vẫn "AI soạn" hoặc "Đang sửa" (không phải "Giáo viên đã duyệt")

**US-S2E5-03-SC3 — Duyệt không đổi nhãn máy** · Nguồn: BR-S2E5-15; PO-38
- Given: Lời giải v2 nhãn máy "Cần xem lại" (chưa kiểm xong)
- When: Duyệt và xác nhận cảnh báo
- Then: Trục người "Giáo viên đã duyệt"; trục máy vẫn "Cần xem lại"

**US-S2E5-03-FC1 — Không thao tác nào đặt được nhãn Đã kiểm chứng từ phía người dùng** · Nguồn: BR-S2E5-15; PO-02
- Given: Lời giải nhãn máy "Cần xem lại"; Cô Trang đã duyệt
- When: Gọi API đặt nhãn máy "Đã kiểm chứng" (thân yêu cầu có trường nhãn)
- Then: FORBIDDEN hoặc VALIDATION_FAILED; nhãn máy không đổi; giao diện không có nút nào làm việc này

**US-S2E5-03-FC2 — Còn bước Không đạt thì không thể Đã kiểm chứng dù đã duyệt** · Nguồn: BR-S2E5-12
- Given: Lời giải có một bước Không đạt; trục người "Giáo viên đã duyệt" (do duyệt trước khi sửa)
- When: Chạy kiểm tra lại
- Then: Nhãn máy "Cần xem lại"

**US-S2E5-03-FC3 — Mọi bước Không kiểm được thì Cần xem lại** · Nguồn: BR-S2E5-12; QA-S2E5-05
- Given: Lời giải chỉ có các bước lập luận chữ (không phép tính)
- When: Chạy kiểm tra lại
- Then: Nhãn máy "Cần xem lại" vì không có bước Đạt

**US-S2E5-03-FC4 — Mọi bước Đạt nhưng đáp số cuối lệch engine thì Cần xem lại** · Nguồn: BR-S2E5-12
- Given: Mọi bước Đạt theo giá trị bước trước nhưng đáp số cuối ghi 54,7° (engine 45°)
- When: Chạy kiểm tra lại
- Then: Nhãn máy "Cần xem lại"; hộp duyệt báo lệch đáp số

**US-S2E5-03-SC4 — Sửa sau khi Đã kiểm chứng thì về Cần xem lại** · Nguồn: BR-S2E5-16; PO-48
- Given: Lời giải v3 đang "Đã kiểm chứng"
- When: Sửa bước 2 thành "AC = a√3"; lưu v4
- Then: v4: nhãn máy "Cần xem lại" cho tới khi kiểm lại đạt; v3 vẫn "Đã kiểm chứng"

**US-S2E5-03-SC5 — Học sinh thấy nhãn của phiên bản đã giao** · Nguồn: BR-S2E5-14
- Given: Bài giao dùng v2 nhãn "Cần xem lại" và "Giáo viên đã duyệt"
- When: An mở bài trên iPad
- Then: Thẻ hình hiện đủ hai nhãn của v2; không hiện nhãn của phiên bản mới hơn chưa áp dụng

**US-S2E5-03-FC5 — Bộ câu thử bài sai không có mẫu nào mang Đã kiểm chứng** · Nguồn: M-S2-03; BR-S2E5-12
- Given: Bộ 10 lời giải sai cố ý (sai bước tính, sai đáp số, sai đơn vị, thiếu bước)
- When: Chạy kiểm cho từng lời giải
- Then: Không lời giải nào nhận "Đã kiểm chứng" (M-S2-03 = 0)

**US-S2E5-03-SC6 — Hai nhãn hiện đúng cả trên iPhone** · Nguồn: BR-S2E5-14
- Given: An mở bài trên iPhone 390 × 844
- When: Mở bài
- Then: Hai nhãn không bị cắt chữ; dòng chú thích xuống hàng gọn; không cuộn ngang

- **Mở và quyết định đang dùng:** QA-S2E5-05
- **DoR:** ready for R3, mức P0.

## US-S2E5-04 — Hộp duyệt chung: nhận, sửa, bỏ

- **Story:** Là giáo viên, tôi muốn một hộp duyệt để nhận, sửa hoặc bỏ từng đề xuất của AI rồi duyệt lời giải, để không có gì vào giáo trình khi tôi chưa quyết · Loại UI · Mức P0 · Khung C2-03
- **Nguồn:** 0.3 ý 4; STAGES Ch2 mục 5 luồng A; PO-46; DQ-05
- **Góc nhìn người vận hành:** giáo viên duyệt vội mà không đọc; bấm Duyệt hai lần; duyệt khi máy còn "Cần xem lại" (DQ-05: được, kèm cảnh báo) hoặc còn bước lệch (không được). Hộp duyệt rỗng khi không còn gì chờ.
- **Phụ thuộc:** US-S2E5-02, US-S2E5-03

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S2E5-17 | Duyệt bị chặn khi còn ít nhất một bước Không đạt hoặc khi đáp số lệch engine (US-S2E3-04); bị chặn thì nút khóa và hiện "Chặn duyệt: còn n bước lệch" | DQ-04, STAGES Ch2 mục 5 |
| BR-S2E5-18 | Duyệt được khi nhãn máy "Cần xem lại" mà không có bước Không đạt (chưa kiểm hoặc Không kiểm được), nhưng phải xác nhận ở hộp cảnh báo "Nhãn máy vẫn Cần xem lại. Vẫn duyệt?" (TẠM, QA-S2E5-03) | DQ-05, PO-38 |
| BR-S2E5-19 | Duyệt ghi người duyệt (mã người dùng), giờ, phiên bản; đề vào ngăn "Của tôi" với trục người "Giáo viên đã duyệt"; bản duyệt trước (nếu có) thành "Bị thay thế" | STAGES Ch2 mục 5 luồng A, mục 8 |
| BR-S2E5-20 | Mỗi đề xuất của AI có ba nút: Nhận (vào nháp thành phiên bản mới), Sửa (mở soạn với nội dung đề xuất), Bỏ (loại, ghi "đã bỏ" trong lịch sử, không xóa) | 0.3 ý 4, PO-46 |
| BR-S2E5-21 | Duyệt chỉ áp dụng cho phiên bản đang hiện hành; duyệt phiên bản đã bị thay thế nhận CONFLICT; bấm Duyệt hai lần chỉ ghi một lần duyệt | D-GEO-143 |

### Tiêu chí chấp nhận

**US-S2E5-04-SC1 — Duyệt khi máy Đã kiểm chứng** · Nguồn: BR-S2E5-19; STAGES Ch2 mục 5
- Given: Lời giải v3 nhãn máy "Đã kiểm chứng"
- When: Bấm "Duyệt lời giải này"
- Then: Trục người "Giáo viên đã duyệt"; ghi mã `GV-7K2M-9Q4D`, giờ, phiên bản v3; đề vào "Của tôi"; không hộp cảnh báo

**US-S2E5-04-SC2 — Duyệt khi Cần xem lại phải xác nhận cảnh báo** · Nguồn: BR-S2E5-18; DQ-05
- Given: Lời giải v2 nhãn "Cần xem lại" (chưa kiểm), không bước Không đạt
- When: Bấm "Duyệt lời giải này"; ở hộp cảnh báo bấm "Vẫn duyệt"
- Then: Hiện hộp "Nhãn máy vẫn Cần xem lại. Vẫn duyệt?"; sau "Vẫn duyệt": trục người "Giáo viên đã duyệt", trục máy vẫn "Cần xem lại"

**US-S2E5-04-SC3 — Hủy ở hộp cảnh báo thì không duyệt** · Nguồn: BR-S2E5-18
- Given: Như SC2
- When: Bấm "Duyệt lời giải này"; ở hộp cảnh báo bấm "Quay lại"
- Then: Không duyệt; trục người giữ nguyên; không bản ghi duyệt

**US-S2E5-04-FC1 — Còn một bước lệch thì Duyệt khóa** · Nguồn: BR-S2E5-17; DQ-04
- Given: Lời giải v2 có đúng 1 bước Không đạt (bước 3)
- When: Xem nút Duyệt; bấm thử; gọi API duyệt
- Then: Nút khóa kèm "Chặn duyệt: còn 1 bước lệch"; API: CONFLICT; không bản ghi duyệt

**US-S2E5-04-BV1 — 0 bước lệch thì Duyệt mở, 1 bước lệch thì khóa** · Nguồn: BR-S2E5-17
- Given: Hai lời giải: A không có bước Không đạt; B có đúng 1 bước Không đạt
- When: Xem nút Duyệt của từng lời giải
- Then: A: Duyệt mở; B: Duyệt khóa "còn 1 bước lệch"

**US-S2E5-04-BV2 — Còn 3 bước lệch thì thông báo đúng số** · Nguồn: BR-S2E5-17
- Given: Lời giải có 3 bước Không đạt
- When: Xem hộp duyệt
- Then: Hiện "Chặn duyệt: còn 3 bước lệch"

**US-S2E5-04-SC4 — Nhận đề xuất của AI thành phiên bản mới** · Nguồn: BR-S2E5-20
- Given: AI giả lập đề xuất thêm bước giải thích sau bước 2
- When: Bấm "Nhận"
- Then: Tạo phiên bản mới có bước thêm; hộp duyệt mất đề xuất đó; lịch sử ghi "nhận đề xuất của AI"

**US-S2E5-04-SC5 — Sửa đề xuất mở soạn với nội dung đề xuất** · Nguồn: BR-S2E5-20
- Given: Đề xuất AI chờ
- When: Bấm "Sửa"
- Then: Mở trình soạn với nội dung đề xuất đã điền; chưa tạo phiên bản cho tới khi bấm "Lưu"

**US-S2E5-04-SC6 — Bỏ đề xuất thì loại và còn dấu vết** · Nguồn: BR-S2E5-20
- Given: Đề xuất AI chờ
- When: Bấm "Bỏ"
- Then: Đề xuất rời hộp duyệt; lịch sử ghi "đã bỏ"; phiên bản không đổi

**US-S2E5-04-FC2 — Bấm Duyệt hai lần chỉ ghi một lần** · Nguồn: BR-S2E5-21
- Given: Lời giải v3 đã sẵn sàng duyệt
- When: Bấm "Duyệt lời giải này" hai lần trong 1 giây
- Then: Chỉ một bản ghi duyệt; không lỗi cho lần hai (kết quả như lần một)

**US-S2E5-04-FC3 — Duyệt phiên bản đã bị thay thế nhận CONFLICT** · Nguồn: BR-S2E5-21
- Given: Đề có v2 (đã duyệt) và v3 (đang hiện hành, chưa duyệt)
- When: Gọi API duyệt trên v2 sau khi v3 đã có
- Then: CONFLICT với thông báo "Phiên bản đã cũ"; không thay đổi nhãn

**US-S2E5-04-SC7 — Duyệt phiên bản mới thì bản duyệt trước thành Bị thay thế** · Nguồn: BR-S2E5-19
- Given: v2 đã duyệt; v3 sẵn sàng duyệt
- When: Duyệt v3
- Then: v3 "Giáo viên đã duyệt"; v2 "Bị thay thế"; đề chỉ có một bản duyệt hiện hành

**US-S2E5-04-FC4 — Giáo viên khác không duyệt được đề của người khác** · Nguồn: BR-S2E5-21; BR-S2-06
- Given: Đề Của tôi của Cô Trang; Cô Lan đăng nhập
- When: Cô Lan gọi API duyệt
- Then: NOT_FOUND; không bản ghi duyệt

**US-S2E5-04-FC5 — Hộp duyệt rỗng** · Nguồn: 0.3 ý 4
- Given: Không còn đề xuất nào chờ
- When: Mở hộp duyệt
- Then: Hiện "Không có gì chờ duyệt"; nút Duyệt vẫn theo trạng thái lời giải

- **Mở và quyết định đang dùng:** QA-S2E5-03
- **DoR:** ready for R3, mức P0.

## US-S2E5-05 — Lời giải đã giao bất biến; sửa tạo phiên bản mới và chọn có áp cho bài đang giao

- **Story:** Là giáo viên, tôi muốn lời giải đã giao không đổi dưới chân học sinh và tự chọn lúc nào áp bản mới, để lớp không thấy nội dung đổi bất ngờ · Loại Xuyên suốt · Mức P0 · Khung C2-03
- **Nguồn:** STAGES Ch2 mục 7; DQ-06
- **Góc nhìn người vận hành:** giáo viên phát hiện lỗi sau khi đã giao BG-1 (11A1) và BG-2 (cùng đề, 11A2). Học sinh đã mở lời giải v2 (lúc đã tới giờ mở). Áp bản mới cho từng bài giao riêng, không bao giờ mở lời giải sớm hơn `release_at`.
- **Phụ thuộc:** US-S2E5-01, US-S2E5-04, S2-E8 (bài luyện dùng đáp số)

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S2E5-22 | Bài giao gắn với một phiên bản lời giải cố định; phiên bản đó bất biến: không sửa, không xóa; sửa chỉ có thể tạo phiên bản mới | STAGES Ch2 mục 7 |
| BR-S2E5-23 | Sau khi lưu phiên bản mới của đề đã giao, giáo viên chọn cho từng bài giao: "Áp dụng cho lớp" hoặc "Giữ v cũ"; mặc định "Giữ v cũ" | STAGES Ch2 mục 7, DQ-06 |
| BR-S2E5-24 | Chỉ phiên bản "Giáo viên đã duyệt" mới áp dụng được cho bài giao (nút "Áp dụng cho lớp" khóa trước đó) (TẠM, QA-S2E5-06) | STAGES mục 0.2 nguyên tắc 3 |
| BR-S2E5-25 | Áp dụng cho bài đã có học sinh xem lời giải cũ: hiện cảnh báo nêu số học sinh đã xem và đòi xác nhận; áp dụng không mở lời giải sớm hơn `release_at` | STAGES mục 0.2 nguyên tắc 1 |
| BR-S2E5-26 | Phiên bản mới đổi đáp số so với bản đang giao: cảnh báo "Đáp số đổi từ X thành Y; kết quả đã chấm không tính lại" (TẠM, QA-S2E5-07) | BR-S2-08 |

- **Dữ liệu:** đề FX-14 đã giao: BG-1 lớp 11A1 và BG-2 lớp 11A2, cùng dùng lời giải v2; An thuộc 11A1; đã có 2 học sinh xem lời giải v2 ở BG-1

### Tiêu chí chấp nhận

**US-S2E5-05-SC1 — Sửa đề đã giao tạo phiên bản mới, bản đã giao không đổi** · Nguồn: STAGES Ch2 mục 7; DQ-06
- Given: BG-1 và BG-2 đang dùng v2
- When: Sửa bước 4 của đề rồi bấm "Lưu"
- Then: Tạo v3; BG-1 và BG-2 vẫn dùng v2 (mặc định "Giữ v2"); hiện hộp chọn "Áp dụng cho lớp" hay "Giữ v2" cho từng bài giao

**US-S2E5-05-SC2 — Hộp đã giao hiện đúng phiên bản đang dùng** · Nguồn: DQ-06
- Given: BG-1 dùng v2, BG-2 dùng v2
- When: Mở C2-03 trạng thái đã giao
- Then: Thấy "Đã giao cho 11A1 (v2)" và "Đã giao cho 11A2 (v2)"; chú thích "Bản đã giao không sửa được, sửa sẽ tạo phiên bản mới"

**US-S2E5-05-FC1 — Sửa trực tiếp bản đã giao nhận CONFLICT** · Nguồn: BR-S2E5-22
- Given: v2 đang giao
- When: Gọi API ghi đè nội dung v2
- Then: CONFLICT với thông báo "Bản đã giao không sửa được, hãy tạo phiên bản mới"; v2 không đổi từng ký tự

**US-S2E5-05-FC2 — Phiên bản chưa duyệt không áp dụng được** · Nguồn: BR-S2E5-24; QA-S2E5-06
- Given: v3 chưa duyệt
- When: Xem nút "Áp dụng cho lớp"; gọi API áp dụng
- Then: Nút khóa kèm "Duyệt v3 trước"; API: CONFLICT; bài giao vẫn v2

**US-S2E5-05-SC3 — Duyệt v3 rồi áp dụng cho một bài giao** · Nguồn: BR-S2E5-23
- Given: v3 đã duyệt; BG-1 chưa có học sinh nào xem lời giải
- When: Chọn "Áp dụng cho lớp" ở BG-1
- Then: BG-1 chuyển sang v3; hộp ghi "Đã giao cho 11A1 (v3)"; BG-2 vẫn v2

**US-S2E5-05-BV1 — Áp cho BG-1 không đổi BG-2 cùng đề** · Nguồn: BR-S2E5-23
- Given: BG-1 và BG-2 cùng đề; v3 đã duyệt
- When: Áp dụng v3 chỉ cho BG-1
- Then: BG-1 dùng v3; BG-2 vẫn dùng v2; học sinh 11A2 thấy v2

**US-S2E5-05-SC4 — Chọn Giữ v2 thì bài giao không đổi** · Nguồn: BR-S2E5-23
- Given: v3 đã duyệt
- When: Chọn "Giữ v2" cho cả hai bài giao
- Then: Cả hai vẫn v2; v3 chỉ nằm ở "Của tôi"

**US-S2E5-05-FC3 — Áp dụng cho bài đã có người xem lời giải cũ phải xác nhận** · Nguồn: BR-S2E5-25
- Given: BG-1 đã có 2 học sinh xem lời giải v2; v3 đã duyệt
- When: Chọn "Áp dụng cho lớp" ở BG-1
- Then: Hiện cảnh báo "2 học sinh đã xem bản v2. Vẫn áp dụng?"; chỉ khi xác nhận mới chuyển v3; hủy thì giữ v2

**US-S2E5-05-FC4 — Đổi đáp số thì cảnh báo kết quả không tính lại** · Nguồn: BR-S2E5-26; QA-S2E5-07
- Given: v3 có đáp số 60° thay vì 45° của v2; BG-1 đã có bài luyện được chấm
- When: Chọn "Áp dụng cho lớp" ở BG-1
- Then: Hiện "Đáp số đổi từ 45° thành 60°; kết quả đã chấm không tính lại"; sau xác nhận, kết quả đã chấm giữ nguyên

**US-S2E5-05-FC5 — Áp dụng không mở lời giải sớm hơn giờ mở** · Nguồn: BR-S2E5-25; STAGES mục 0.2 nguyên tắc 1
- Given: BG-1 chưa tới `release_at`; v3 đã duyệt
- When: Áp dụng v3 cho BG-1; An gọi lấy lời giải
- Then: Dữ liệu trả về cho An không chứa bước giải v3 hay đáp số ở bất kỳ dạng viết nào; tiêu đề `Cache-Control: no-store`

**US-S2E5-05-FC6 — Giáo viên khác không áp dụng được** · Nguồn: BR-S2E5-23; BR-S2-06
- Given: Đề của Cô Trang; Cô Lan đăng nhập
- When: Cô Lan gọi API áp dụng v3
- Then: NOT_FOUND; bài giao không đổi

**US-S2E5-05-SC5 — Học sinh đang mở bài vẫn thấy ổn định, lần tải sau thấy v3** · Nguồn: BR-S2E5-23
- Given: An đang mở BG-1 ở iPad; sau đó giáo viên áp dụng v3
- When: An tải lại bài
- Then: Trước khi tải lại nội dung bài không đổi; sau tải lại hiện nhãn và nội dung theo v3

- **Mở và quyết định đang dùng:** QA-S2E5-06, QA-S2E5-07
- **DoR:** ready for R3, mức P0.

## Kiểm phủ

- Story trong epic: 5; tiêu chí: 64. Mọi story của epic trong `story-map-s23.md` đều có mặt, không story thừa.
- Câu hỏi mở và đề xuất đổi story map: `questions.md` cùng thư mục (không sửa story map, R2 đã khóa).

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: 5 story, 64 tiêu chí, ready for R3 | runner-T-261126-12 (BA) |
| v0.2 | 2026-11-26 | đổi mã mẫu `HS-`/`GV-` có ký tự 0, 1, O, I sang ký tự hợp lệ (BR-S2E1-04), RV_s23 m-01 | runner-T-261126-15 (QC soát phản biện, RV_s23) |
