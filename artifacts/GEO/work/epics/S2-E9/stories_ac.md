Phiên bản: v0.1 · 2026-11-26
# S2-E9 — Biến thể đề, xuất ảnh/PDF và liên kết chỉ xem, câu trắc nghiệm trong bài luyện: story Dev-Ready và tiêu chí chấp nhận

> Chủ sở hữu: BA (goal run T-261126-12, BA + QC liền). Nguồn: `artifacts/GEO/work/s23/story-map-s23.md` (R2 khóa 26/11/2026), `brief-s23.md`, `brd-delta-s23.md`, `STAGES-AFTER-MVP.md` Chặng 2 mục 5–8 (22/11/2026), `Design/s23/DQ.md` và ảnh `Design/s23/c2-*.png` (26/11/2026), `PO-DECISIONS.md` PO-61…65 (PO-61 mới nhất, thắng mọi nguồn cũ).
> **Trạng thái: ready for R3** · Mức epic: **P1** · Ba story mức P1 (BR-S2-09, Could). Thiết diện động không thuộc epic này (Q7, PO-65). Khung dùng C2-06, C2-08; riêng biến thể và xuất chưa có khung (QA-S2E9-01).

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
| `ProblemVariant` | biến thể của một đề gốc: bộ số liệu mới sinh từ đề gốc và đáp số do engine tính lại | STAGES Ch2 mục 8 |
| liên kết chỉ xem | địa chỉ ngẫu nhiên không đoán được cho phép xem đề và hình, không đăng nhập, không ghi, có hạn dùng và thu hồi được (TẠM) | STAGES Ch2 mục 4 P1 |

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

## US-S2E9-01 — Biến thể đề `ProblemVariant`, đáp số tính lại

- **Story:** Là giáo viên, tôi muốn tạo biến thể của một đề với số liệu mới và đáp số tính lại, để có nhiều đề cùng dạng mà không phải ra từng đề · Loại Logic · Mức P1 · Khung (khung do task 10 đề xuất)
- **Nguồn:** STAGES Ch2 mục 3 ý 7; BR-S2-09; PO-65 Q7
- **Góc nhìn người vận hành:** giáo viên cần 3 đề cùng dạng cho 3 nhóm; dễ sinh bộ số vô nghĩa (tam giác không tồn tại), trùng gốc hoặc trùng nhau. Đáp số chỉ do engine tính, không do AI; biến thể chưa duyệt mang nhãn "Cần xem lại".
- **Phụ thuộc:** S2-E2, S2-E5

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S2E9-01 | Biến thể giữ cấu trúc đề gốc; chỉ đổi số liệu trong ràng buộc; đáp số tính lại bằng engine (không gọi AI); hình dựng lại | STAGES Ch2 mục 3 ý 7 |
| BR-S2E9-02 | Mỗi lần tạo 1 đến 10 biến thể (TẠM, QA-S2E9-02); bộ số liệu mới khác đề gốc và khác nhau đôi một; không đủ bộ khả dĩ thì trả ít hơn kèm thông báo | BR-S2-09 |
| BR-S2E9-03 | Số mới nằm từ 1 đến 100, tối đa 1 chữ số thập phân, số nguyên nếu gốc là số nguyên; mọi bộ sinh ra phải dựng được hình thỏa ràng buộc | BR-S2E2-06 (TẠM, QA-S2E9-02) |
| BR-S2E9-04 | Sinh theo hạt giống lưu kèm biến thể: cùng đề gốc và cùng hạt giống cho cùng bộ số | BR-S2-09 |
| BR-S2E9-05 | Đề không có tham số số (thuần chữ) hoặc thuộc dạng thiết diện động bị từ chối "chưa hỗ trợ" | PO-65 Q7 |
| BR-S2E9-06 | Biến thể nằm ngăn "Của tôi", nhãn máy "Cần xem lại" và nhãn người "AI soạn" tới khi duyệt; mỗi biến thể tính một đề vào giới hạn thêm đề (US-S2E6-03) | PO-02, PO-63 |

### Tiêu chí chấp nhận

**US-S2E9-01-SC1 — Tạo 3 biến thể hình chữ nhật với đáp số tính lại** · Nguồn: STAGES Ch2 mục 3 ý 7
- Given: Đề PL-03 (FX-12: AB = 6, AD = 8, đáp số AC = 10)
- When: Bấm "Tạo biến thể" với số lượng 3
- Then: Có 3 biến thể; mỗi biến thể có cặp (AB, AD) khác (6, 8) và khác nhau; đáp số AC của từng biến thể bằng căn của AB bình phương cộng AD bình phương (engine tính)

**US-S2E9-01-SC2 — Biến thể giữ dạng chuẩn của đáp số** · Nguồn: BR-S2E9-01
- Given: Đề PL-04 (FX-13: tam giác đều cạnh 4, đáp số 4√3), engine chọn cạnh 6
- When: Tạo biến thể
- Then: Đáp số biến thể là 9√3 (dạng chuẩn có căn) cùng xấp xỉ 15,59

**US-S2E9-01-SC3 — Biến thể mới mang Cần xem lại và AI soạn** · Nguồn: BR-S2E9-06; PO-02
- Given: Biến thể vừa tạo
- When: Mở biến thể trong "Của tôi"
- Then: Hiện hai nhãn "Cần xem lại" và "AI soạn"; có hình dựng thỏa ràng buộc; chưa "Giáo viên đã duyệt"

**US-S2E9-01-SC4 — Cùng hạt giống cho cùng bộ số** · Nguồn: BR-S2E9-04
- Given: Đề PL-03; hạt giống 42
- When: Tạo 3 biến thể với hạt giống 42 hai lần
- Then: Hai lần cho đúng cùng ba bộ số

**US-S2E9-01-FC1 — Đề không có tham số số bị từ chối** · Nguồn: BR-S2E9-05
- Given: Đề thuần chữ "Chứng minh tam giác cân"
- When: Bấm "Tạo biến thể"
- Then: Từ chối "Đề này không có số liệu để đổi"; không biến thể nào được tạo

**US-S2E9-01-FC2 — Thiết diện động chưa hỗ trợ** · Nguồn: BR-S2E9-05; PO-65 Q7
- Given: Đề dạng thiết diện động
- When: Bấm "Tạo biến thể"
- Then: Từ chối "Dạng này chưa hỗ trợ biến thể"; không biến thể

**US-S2E9-01-FC3 — Không bộ nào vô nghĩa trong 1000 lần sinh** · Nguồn: BR-S2E9-03; BR-S2E2-06
- Given: Đề tam giác ABC với ba cạnh tham số
- When: Sinh 1000 biến thể (mỗi lần 1, hạt giống khác nhau)
- Then: Mọi bộ thỏa bất đẳng thức tam giác chặt (không suy biến) và dựng được hình thỏa ràng buộc; không bộ nào có số ngoài 1 đến 100

**US-S2E9-01-BV1 — Tạo 1 và 10 biến thể nhận, 0 và 11 bị chặn** · Nguồn: BR-S2E9-02; QA-S2E9-02
- Given: Đề PL-03
- When: Tạo với số lượng 1; 10; 0; 11
- Then: 1 và 10: thành công; 0: VALIDATION_FAILED "Chọn từ 1 đến 10"; 11: VALIDATION_FAILED

**US-S2E9-01-BV2 — Số gốc sát cận 1 và 100 không sinh số ngoài 1 đến 100** · Nguồn: BR-S2E9-03; QA-S2E9-02
- Given: Hai đề: cạnh hình vuông gốc 1 và 100
- When: Tạo 10 biến thể cho mỗi đề
- Then: Mọi cạnh sinh ra nằm trong [1; 100]; không có 0,5 hay 100,5; không trùng gốc

**US-S2E9-01-FC4 — Ít bộ khả dĩ thì trả ít hơn kèm thông báo** · Nguồn: BR-S2E9-02
- Given: Đề có miền số chỉ cho phép đúng 4 bộ khác gốc
- When: Tạo 10 biến thể
- Then: Trả 4 biến thể không trùng; hiện "Chỉ tạo được 4 biến thể khác nhau"

**US-S2E9-01-FC5 — Giáo viên khác không tạo biến thể từ đề Của tôi của người khác** · Nguồn: BR-S2E9-06; BR-S2-06
- Given: Đề FX-12 của Cô Trang; Cô Lan đăng nhập
- When: Cô Lan gọi tạo biến thể
- Then: NOT_FOUND; không biến thể

**US-S2E9-01-BV3 — Biến thể tính vào giới hạn thêm đề: 20 nhận, 21 bị chặn** · Nguồn: BR-S2E9-06; BR-S2E6-13
- Given: Cô Trang đã thêm 18 đề trong 60 phút
- When: Tạo 3 biến thể một lần
- Then: Hai biến thể đầu tạo được (đủ 20); biến thể thứ ba: RATE_LIMITED; thông báo nêu "tạo được 2 biến thể"

**US-S2E9-01-FC6 — Đáp số biến thể do engine tính, không gọi AI** · Nguồn: BR-S2E9-01; D-GEO-047
- Given: Bộ đếm lần gọi AI giả lập về 0
- When: Tạo 3 biến thể
- Then: Bộ đếm gọi AI vẫn 0; đáp số có nguồn "engine"

- **Mở và quyết định đang dùng:** QA-S2E9-01, QA-S2E9-02
- **DoR:** ready for R3, mức P1.

## US-S2E9-02 — Xuất ảnh/PDF và liên kết chỉ xem

- **Story:** Là giáo viên, tôi muốn xuất đề ra ảnh hoặc PDF và chia sẻ bằng liên kết chỉ xem, để in hoặc gửi cho đồng nghiệp mà không lộ lời giải · Loại UI · Mức P1 · Khung (như trên)
- **Nguồn:** STAGES Ch2 mục 4 P1
- **Góc nhìn người vận hành:** giáo viên in phiếu cho lớp, gửi đề cho đồng nghiệp. Lời giải chỉ xuất khi giáo viên chủ động tick; đề đang khóa lời giải thì xuất kèm lời giải có thể lộ ra ngoài hệ thống, nên phải hỏi lại. Liên kết không đăng nhập nên không được lộ danh tính giáo viên hay đoán được.
- **Phụ thuộc:** S2-E5, S2-E6

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S2E9-07 | Xuất PNG (một đề) hoặc PDF (1 đến 20 đề) gồm đề và hình; "Kèm lời giải" mặc định tắt và chỉ bật được khi lời giải là "Giáo viên đã duyệt"; bản có lời giải in cả hai nhãn | STAGES Ch2 mục 4 P1, PO-02 |
| BR-S2E9-08 | Xuất kèm lời giải cho đề đang giao mà lời giải còn khóa phải xác nhận "File xuất có lời giải, lớp có thể thấy nếu giáo viên chia sẻ. Vẫn xuất?" | STAGES mục 0.2 nguyên tắc 1 |
| BR-S2E9-09 | Liên kết chỉ xem: mã ngẫu nhiên không đoán được (tối thiểu 128 bit); hạn 1, 7 hoặc 30 ngày (mặc định 7); thu hồi được; tối đa 5 liên kết đang hoạt động mỗi đề (TẠM, QA-S2E9-03) | STAGES Ch2 mục 4 P1 |
| BR-S2E9-10 | Người mở liên kết chỉ thấy chữ đề và hình; không lời giải, không tên hay mã người dùng của giáo viên, không thao tác ghi; mã sai, hết hạn hay đã thu hồi cùng một thông báo | PO-61, STAGES mục 0.2 nguyên tắc 5 |

### Tiêu chí chấp nhận

**US-S2E9-02-SC1 — Xuất ảnh PNG của một đề** · Nguồn: BR-S2E9-07
- Given: Đề FX-10 của Cô Trang
- When: Bấm "Xuất ảnh"
- Then: Tải về tệp PNG một trang có chữ đề và hình tam giác 5, 12, 13; không có lời giải

**US-S2E9-02-SC2 — Xuất PDF nhiều đề** · Nguồn: BR-S2E9-07
- Given: Cô Trang chọn 3 đề (FX-10, FX-11, FX-13)
- When: Bấm "Xuất PDF"
- Then: Tệp PDF có 3 đề theo thứ tự đã chọn, mỗi đề có hình; không lời giải

**US-S2E9-02-SC3 — Kèm lời giải thì in cả hai nhãn** · Nguồn: BR-S2E9-07; PO-02
- Given: Đề FX-14 có lời giải "Giáo viên đã duyệt", nhãn máy "Đã kiểm chứng"; chưa giao
- When: Tick "Kèm lời giải"; bấm "Xuất PDF"
- Then: PDF có lời giải đủ bước; đầu trang in "Đã kiểm chứng" và "Giáo viên đã duyệt"

**US-S2E9-02-FC1 — Lời giải chưa duyệt không kèm được** · Nguồn: BR-S2E9-07
- Given: Đề có lời giải "AI soạn" chưa duyệt
- When: Xem ô "Kèm lời giải"; gọi API xuất kèm lời giải
- Then: Ô khóa kèm "Cần duyệt lời giải trước"; API: CONFLICT; không tệp nào

**US-S2E9-02-FC2 — Đề đang giao và khóa phải xác nhận trước khi xuất kèm lời giải** · Nguồn: BR-S2E9-08
- Given: Đề FX-14 giao BG-1, lời giải khóa tới T0 + 2 giờ; lời giải đã duyệt
- When: Tick "Kèm lời giải"; bấm "Xuất PDF"; bấm "Hủy" ở hộp hỏi; bấm lại và "Vẫn xuất"
- Then: Hiện hộp hỏi; "Hủy": không tệp; "Vẫn xuất": có tệp; ghi lại việc xác nhận

**US-S2E9-02-BV1 — Xuất 1 và 20 đề nhận, 0 và 21 bị chặn** · Nguồn: BR-S2E9-07
- Given: Cô Trang có 25 đề
- When: Xuất PDF với 1; 20; 0; 21 đề
- Then: 1 và 20: thành công; 0: lỗi "Chọn ít nhất 1 đề"; 21: VALIDATION_FAILED "Tối đa 20 đề mỗi tệp"

**US-S2E9-02-SC4 — Tạo liên kết chỉ xem mặc định hạn 7 ngày** · Nguồn: BR-S2E9-09
- Given: Đề FX-10
- When: Bấm "Tạo liên kết chỉ xem"
- Then: Hiện địa chỉ có mã ngẫu nhiên dài; hạn "7 ngày" mặc định; nút "Sao chép" và "Thu hồi"

**US-S2E9-02-SC5 — Mở liên kết không đăng nhập chỉ thấy đề và hình** · Nguồn: BR-S2E9-10
- Given: Liên kết còn hạn của FX-10
- When: Mở liên kết ở trình duyệt chưa đăng nhập
- Then: Hiện chữ đề và hình; không lời giải, không tên hay mã `GV-` của Cô Trang; không nút sửa; dữ liệu trả về cũng không chứa lời giải hay tên giáo viên

**US-S2E9-02-BV2 — Liên kết hết hạn đúng mốc 7 ngày** · Nguồn: BR-S2E9-09
- Given: Liên kết tạo lúc T0 hạn 7 ngày; đồng hồ giả
- When: Mở ở T0 + 7 ngày - 1 giây; mở ở T0 + 7 ngày
- Then: Mốc trước: xem được; đúng mốc: "Liên kết không còn dùng được" (cùng thông báo mã sai)

**US-S2E9-02-FC3 — Thu hồi thì liên kết như mã sai** · Nguồn: BR-S2E9-09; BR-S2E9-10
- Given: Liên kết còn hạn
- When: Bấm "Thu hồi"; mở liên kết đó; mở một mã sai
- Then: Hai lần đều NOT_FOUND với cùng câu "Liên kết không còn dùng được"; không phân biệt đã thu hồi hay sai

**US-S2E9-02-FC4 — Dò mã liên kết bị giới hạn** · Nguồn: BR-S2E9-09; PO-63
- Given: Một địa chỉ mạng thử nhiều mã sai
- When: Gửi 100 yêu cầu với mã sai; gửi yêu cầu thứ 101 với mã đúng
- Then: 100 lần đầu: NOT_FOUND; lần 101 từ địa chỉ đó: RATE_LIMITED kèm `Retry-After` (kể cả với mã đúng)

**US-S2E9-02-BV3 — Liên kết thứ 5 nhận, thứ 6 bị chặn** · Nguồn: BR-S2E9-09; QA-S2E9-03
- Given: Đề FX-10 có 4 liên kết đang hoạt động
- When: Tạo liên kết thứ 5; tạo liên kết thứ 6
- Then: Liên kết 5 thành công; liên kết 6: lỗi "Tối đa 5 liên kết đang dùng", CONFLICT

**US-S2E9-02-FC5 — Liên kết chỉ xem không có thao tác ghi** · Nguồn: BR-S2E9-10
- Given: Liên kết còn hạn
- When: Gửi yêu cầu sửa đề qua liên kết; gửi yêu cầu giao bài
- Then: FORBIDDEN; đề không đổi

**US-S2E9-02-FC6 — Giáo viên khác không xuất hay tạo liên kết cho đề Của tôi của người khác** · Nguồn: BR-S2-06
- Given: Đề FX-10 của Cô Trang; Cô Lan
- When: Cô Lan gọi xuất và tạo liên kết
- Then: NOT_FOUND; không tệp, không liên kết

**US-S2E9-02-SC6 — Chọn hạn 1 hoặc 30 ngày** · Nguồn: BR-S2E9-09
- Given: Hộp tạo liên kết
- When: Chọn "1 ngày"; tạo; chọn "30 ngày"; tạo
- Then: Hạn ghi đúng 1 ngày và 30 ngày; không có lựa chọn khác (ví dụ không giới hạn)

- **Mở và quyết định đang dùng:** QA-S2E9-01, QA-S2E9-03
- **DoR:** ready for R3, mức P1.

## US-S2E9-03 — Câu trắc nghiệm trong bài luyện

- **Story:** Là giáo viên, tôi muốn thêm câu trắc nghiệm vào bài luyện, để học sinh có cả câu chọn đáp án lẫn câu điền đáp số · Loại UI · Mức P1 · Khung C2-06
- **Nguồn:** STAGES Ch2 mục 4 P1; BR-S2-09
- **Góc nhìn người vận hành:** câu trắc nghiệm dễ bị dò bằng loại trừ, nên mặc định 1 lượt và phản hồi không lộ lựa chọn đúng. Câu trắc nghiệm không có kiểu "sai cú pháp".
- **Phụ thuộc:** S2-E8

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S2E9-11 | Câu trắc nghiệm có từ 2 đến 5 lựa chọn; đúng 1 lựa chọn đúng; mỗi lựa chọn 1 đến 100 ký tự và không trùng nhau sau khi chuẩn hóa chữ | STAGES Ch2 mục 4 P1 (TẠM, QA-S2E9-04) |
| BR-S2E9-12 | Câu trắc nghiệm mặc định 1 lượt thử (giáo viên đặt từ 1 đến số lựa chọn trừ 1); chọn sai trừ một lượt và lựa chọn đó bị làm mờ, không chọn lại | BR-S2E8-07 (TẠM, QA-S2E9-04) |
| BR-S2E9-13 | Học sinh chọn một lựa chọn rồi bấm "Kiểm tra"; chưa chọn thì nút khóa; bàn phím toán ẩn ở câu trắc nghiệm và hiện ở câu điền | DQ-11 |
| BR-S2E9-14 | Dữ liệu trả về cho học sinh chứa lựa chọn nhưng không có cờ lựa chọn đúng; phản hồi Sai không nói lựa chọn nào đúng; không có nút "Em nghĩ đáp số này đúng" ở câu trắc nghiệm | STAGES mục 0.2 nguyên tắc 1, BR-S2E8-30 |

### Tiêu chí chấp nhận

**US-S2E9-03-SC1 — Soạn câu trắc nghiệm 4 lựa chọn** · Nguồn: BR-S2E9-11
- Given: Cô Trang ở "Soạn bài luyện"; đề FX-14 đã chọn
- When: Thêm câu kiểu "Trắc nghiệm"; nhập 4 lựa chọn "30°", "45°", "60°", "90°"; chọn "45°" là đúng; lưu nháp
- Then: Câu lưu với 4 lựa chọn, đúng 1 lựa chọn "45°"; mặc định 1 lượt

**US-S2E9-03-SC2 — Chọn đúng thì Đúng** · Nguồn: BR-S2E9-13
- Given: An ở câu trắc nghiệm trên iPad
- When: Chọn "45°"; bấm "Kiểm tra"
- Then: Hiện "Đúng rồi!"; câu đúng; sang câu kế được; không bàn phím toán

**US-S2E9-03-SC3 — Chọn sai trừ lượt, hết lượt thì xem lại cùng thầy cô** · Nguồn: BR-S2E9-12; DQ-12
- Given: Câu 1 lượt; An chọn "60°"
- When: Chọn "60°"; bấm "Kiểm tra"
- Then: Hiện "Chưa đúng. Em xem lại cùng thầy cô nhé."; chip "Còn 0/1 lượt"; "60°" bị làm mờ; không nói lựa chọn nào đúng; câu "xem lại cùng thầy cô"

**US-S2E9-03-SC4 — Câu có nhiều lượt: lựa chọn sai bị làm mờ** · Nguồn: BR-S2E9-12
- Given: Câu trắc nghiệm 4 lựa chọn đặt 2 lượt
- When: Chọn "60°"; bấm "Kiểm tra"; thử chọn lại "60°"
- Then: Lượt còn 1; "60°" làm mờ và không chọn lại được; các lựa chọn khác còn chọn được

**US-S2E9-03-FC1 — Chưa chọn thì nút Kiểm tra khóa** · Nguồn: BR-S2E9-13
- Given: Câu trắc nghiệm chưa chọn
- When: Bấm "Kiểm tra"
- Then: Nút khóa; không yêu cầu nào gửi

**US-S2E9-03-BV1 — 2 và 5 lựa chọn nhận, 1 và 6 bị chặn** · Nguồn: BR-S2E9-11; QA-S2E9-04
- Given: Soạn câu trắc nghiệm
- When: Lưu với 1; 2; 5; 6 lựa chọn
- Then: 1: lỗi "Cần từ 2 đến 5 lựa chọn"; 2 và 5: lưu được; 6: lỗi tương tự

**US-S2E9-03-BV2 — Lựa chọn 100 ký tự nhận, 101 bị chặn, rỗng bị chặn** · Nguồn: BR-S2E9-11
- Given: Soạn câu trắc nghiệm
- When: Lưu với một lựa chọn dài 100 ký tự; 101 ký tự; rỗng
- Then: 100: lưu được; 101: VALIDATION_FAILED; rỗng: lỗi "Lựa chọn đang trống"

**US-S2E9-03-FC2 — Không có lựa chọn đúng bị chặn** · Nguồn: BR-S2E9-11
- Given: Soạn câu 4 lựa chọn không chọn đúng
- When: Bấm "Lưu nháp"
- Then: Lỗi "Hãy chọn một đáp án đúng"; không lưu

**US-S2E9-03-FC3 — Hai lựa chọn đúng bị chặn** · Nguồn: BR-S2E9-11
- Given: Câu 4 lựa chọn; gọi API đánh dấu 2 lựa chọn đúng
- When: Lưu
- Then: VALIDATION_FAILED "Chỉ một đáp án đúng"; không lưu

**US-S2E9-03-FC4 — Lựa chọn trùng nhau bị chặn** · Nguồn: BR-S2E9-11
- Given: Lựa chọn "45°" và " 45° " (khác khoảng trắng)
- When: Lưu
- Then: Lỗi "Hai lựa chọn giống nhau"; không lưu

**US-S2E9-03-FC5 — Dữ liệu trả về không chứa cờ lựa chọn đúng** · Nguồn: BR-S2E9-14
- Given: An mở bài; câu trắc nghiệm trước giờ mở lời giải
- When: Xem toàn bộ dữ liệu trả về khi mở và sau khi chọn sai
- Then: Không có trường cho biết lựa chọn đúng, không có chỉ số vị trí đúng; phản hồi Sai không nêu lựa chọn đúng

**US-S2E9-03-FC6 — Gửi mã lựa chọn không thuộc câu không trừ lượt** · Nguồn: BR-S2E9-12
- Given: Câu trắc nghiệm 1 lượt
- When: Gọi chấm với mã lựa chọn của câu khác
- Then: VALIDATION_FAILED; không Attempt; lượt vẫn 1

**US-S2E9-03-FC7 — Bấm đôi chỉ trừ một lượt** · Nguồn: BR-S2E8-14
- Given: Câu 2 lượt; hai yêu cầu cùng mã chống lặp
- When: Gửi lựa chọn sai hai lần cùng mã trong 1 giây
- Then: Một Attempt; lượt còn 1

**US-S2E9-03-SC5 — Bài luyện hỗn hợp: kết quả đếm chung hai kiểu câu** · Nguồn: BR-S2E8-21
- Given: Bài có 3 câu điền và 2 câu trắc nghiệm; An đúng 4, hết lượt 1
- When: Mở màn kết quả
- Then: Hiện "4 câu đúng · 1 câu cần xem lại" (đếm chung); không điểm

**US-S2E9-03-SC6 — Nút Em nghĩ đáp số này đúng không hiện ở câu trắc nghiệm** · Nguồn: BR-S2E9-14
- Given: An hết lượt ở câu trắc nghiệm
- When: Xem màn
- Then: Không có nút "Em nghĩ đáp số này đúng"; API báo chấm sai cho câu này nhận CONFLICT

**US-S2E9-03-BV3 — Số lượt trắc nghiệm đặt tối đa bằng số lựa chọn trừ 1** · Nguồn: BR-S2E9-12
- Given: Câu 4 lựa chọn
- When: Đặt số lượt 3; đặt số lượt 4
- Then: 3: lưu được; 4: VALIDATION_FAILED "Tối đa 3 lượt cho câu 4 lựa chọn"

- **Mở và quyết định đang dùng:** QA-S2E9-04
- **DoR:** ready for R3, mức P1.

## Kiểm phủ

- Story trong epic: 3; tiêu chí: 44. Mọi story của epic trong `story-map-s23.md` đều có mặt, không story thừa.
- Câu hỏi mở và đề xuất đổi story map: `questions.md` cùng thư mục (không sửa story map, R2 đã khóa).

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: 3 story, 44 tiêu chí, ready for R3 | runner-T-261126-12 (BA) |
