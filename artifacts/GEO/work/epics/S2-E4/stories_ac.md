Phiên bản: v0.2 · 2026-11-26
# S2-E4 — Chat chỉnh nháp đề, import docx/pdf, đối chiếu trùng với kho: story Dev-Ready và tiêu chí chấp nhận

> Chủ sở hữu: BA (goal run T-261126-12, BA + QC liền). Nguồn: `artifacts/GEO/work/s23/story-map-s23.md` (R2 khóa 26/11/2026), `brief-s23.md`, `brd-delta-s23.md`, `STAGES-AFTER-MVP.md` Chặng 2 mục 5–8 (22/11/2026), `Design/s23/DQ.md` và ảnh `Design/s23/c2-*.png` (26/11/2026), `PO-DECISIONS.md` PO-61…65 (PO-61 mới nhất, thắng mọi nguồn cũ).
> **Trạng thái: ready for R3** · Mức epic: **P1** · Ba story mức P1 (PO-65 Q5): chat với AI để chỉnh nháp, import file tách từng bài tập, đối chiếu trùng. Chưa có khung màn hình (task 10 chưa vẽ), xem QA-S2E4-01.

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

## US-S2E4-01 — Chat chỉnh nháp đề, AI hỏi lại và nói rõ trước/sau

- **Story:** Là giáo viên, tôi muốn trao đổi với AI để chỉnh nháp đề, để sửa nhanh mà vẫn là người quyết định · Loại Logic · Mức P1 · Khung (khung do task 10 đề xuất)
- **Nguồn:** PO-45, PO-46, PO-48; PO-65 Q5
- **Góc nhìn người vận hành:** giáo viên gõ "đổi cạnh BC thành 14", "làm đề khó hơn". Câu mơ hồ AI phải hỏi lại. Mỗi đề xuất thấy rõ trước và sau. AI không tự lưu (PO-46).
- **Phụ thuộc:** S2-E3, S2-E5

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S2E4-01 | Giáo viên nêu định hướng bằng câu chữ; nếu định hướng chưa rõ, AI hỏi lại một câu cụ thể thay vì tự đoán | PO-46 |
| BR-S2E4-02 | Mỗi đề xuất của AI ghi: đã đổi gì (trước và sau), dựa trên tài liệu nào, chỗ nào AI chưa chắc | PO-46 |
| BR-S2E4-03 | Giáo viên nhận, sửa tay hoặc bác từng đề xuất; hoàn tác được tối đa 10 bước gần nhất (đã chốt PO-66, QA-S2E4-02) | PO-46 |
| BR-S2E4-04 | AI không tự lưu; chỉ khi giáo viên bấm Duyệt thì nháp vào giáo trình | PO-30, PO-46 |
| BR-S2E4-05 | Đổi dữ kiện đề (số liệu, điều kiện) làm nhãn máy chuyển "Cần xem lại" tới khi máy kiểm lại đạt | PO-48 |
| BR-S2E4-06 | Mỗi tin của giáo viên từ 1 đến 1000 ký tự; mỗi nháp tối đa 30 lượt chat (đã chốt PO-66, QA-S2E4-02) | BR-S2-04 |

### Tiêu chí chấp nhận

**US-S2E4-01-SC1 — Định hướng rõ cho một đề xuất có trước và sau** · Nguồn: PO-46
- Given: Nháp đề FX-10 (BC = 13) đang mở; AI giả lập
- When: Gõ "đổi cạnh BC thành 14"
- Then: AI trả một đề xuất ghi: trước "BC = 13", sau "BC = 14"; ghi nguồn "nháp hiện tại"; chưa đổi nháp cho tới khi giáo viên nhận

**US-S2E4-01-SC2 — Định hướng mơ hồ thì AI hỏi lại, không tự đoán** · Nguồn: PO-46; BR-S2E4-01
- Given: Nháp FX-10 đang mở
- When: Gõ "làm đề khó hơn"
- Then: AI hỏi lại một câu (ví dụ "Khó hơn theo hướng nào: thêm ý hỏi, đổi số, hay thêm bước?"); không có đề xuất đổi nháp nào

**US-S2E4-01-SC3 — Nhận đề xuất thì nháp đổi và nhãn máy chuyển Cần xem lại** · Nguồn: PO-48; BR-S2E4-05
- Given: Đề xuất đổi BC từ 13 thành 14; nháp đang "Đã kiểm chứng"
- When: Bấm "Nhận"
- Then: Nháp có BC = 14; nhãn máy "Cần xem lại" cho tới khi máy kiểm lại đạt; trục người vẫn "AI soạn"

**US-S2E4-01-SC4 — Bác đề xuất thì nháp giữ nguyên** · Nguồn: PO-46
- Given: Một đề xuất đang chờ
- When: Bấm "Bác"
- Then: Nháp không đổi; đề xuất ghi "đã bác" trong lịch sử chat

**US-S2E4-01-SC5 — Sửa tay đề xuất rồi mới nhận** · Nguồn: PO-46
- Given: Đề xuất đổi BC thành 14
- When: Bấm "Sửa tay"; đổi 14 thành 15; bấm "Nhận"
- Then: Nháp có BC = 15; lịch sử ghi "đã sửa tay từ 14 thành 15"

**US-S2E4-01-SC6 — Hoàn tác trả nháp về bước trước** · Nguồn: PO-46; BR-S2E4-03
- Given: Đã nhận 2 đề xuất liên tiếp
- When: Bấm "Hoàn tác" một lần
- Then: Nháp về đúng trạng thái sau đề xuất thứ nhất

**US-S2E4-01-BV1 — Hoàn tác 10 bước được, bước thứ 11 không** · Nguồn: BR-S2E4-03; QA-S2E4-02
- Given: Đã nhận 11 đề xuất liên tiếp
- When: Bấm "Hoàn tác" 11 lần
- Then: 10 lần đầu hoàn tác được; lần 11 nút khóa kèm "Hết bước hoàn tác"

**US-S2E4-01-FC1 — AI không tự lưu: nháp không vào giáo trình khi chưa duyệt** · Nguồn: PO-30; BR-S2E4-04
- Given: Đã nhận một đề xuất, chưa bấm Duyệt
- When: Mở "Của tôi" ở tab khác
- Then: Nháp không có trong "Của tôi" như đề đã duyệt; chỉ hiện ở mục "Nháp"

**US-S2E4-01-BV2 — Tin dài 1000 ký tự nhận, 1001 bị chặn, rỗng bị chặn** · Nguồn: BR-S2E4-06
- Given: Nháp đang mở
- When: Gửi tin 1000 ký tự; tin 1001 ký tự; tin rỗng
- Then: Tin 1000 ký tự gửi được; tin 1001 ký tự: lỗi "Tin tối đa 1000 ký tự"; tin rỗng: không gửi

**US-S2E4-01-BV3 — Lượt chat thứ 30 nhận, lượt 31 bị chặn** · Nguồn: BR-S2E4-06; QA-S2E4-02
- Given: Nháp đã có 29 lượt chat
- When: Gửi lượt thứ 30; gửi lượt thứ 31
- Then: Lượt 30 gửi được; lượt 31: lỗi "Nháp này đã đủ 30 lượt chat, hãy duyệt hoặc tạo nháp mới"; không gọi AI

**US-S2E4-01-FC2 — Đề xuất đổi đáp số không kiểm được thì giữ nhãn Cần xem lại** · Nguồn: PO-48
- Given: Đề xuất sửa đáp số của FX-10 thành 80°
- When: Nhận đề xuất; chạy kiểm
- Then: Máy tính lại 90°; nhãn "Cần xem lại"; hộp duyệt báo lệch như US-S2E3-04

- **Mở và quyết định đang dùng:** QA-S2E4-01, QA-S2E4-02
- **DoR:** ready for R3, mức P1.

## US-S2E4-02 — Import docx/pdf, tách từng bài tập

- **Story:** Là giáo viên, tôi muốn nạp một tệp giáo án docx hoặc pdf và để máy tách từng bài tập, để khỏi nhập từng đề · Loại Logic · Mức P1 · Khung (như trên)
- **Nguồn:** PO-47, PO-27; PO-65 Q5
- **Góc nhìn người vận hành:** tệp Word nhiều bài, lẫn lý thuyết; PDF quét ảnh; tệp hỏng hoặc đặt mật khẩu. Phần lý thuyết (bài giảng) dời chặng 5 (PO-65 Q5).
- **Phụ thuộc:** S2-E3

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S2E4-07 | Nhận docx và pdf có lớp chữ, tối đa 10 MB, tối đa 50 bài mỗi tệp (đã chốt PO-66, QA-S2E4-03); xlsx của PO-27 chưa nhận | PO-27, PO-47 |
| BR-S2E4-08 | Máy tách từng bài tập thành mục riêng, có ô chọn từng bài; giáo viên xem trước từng mục, chỉ bài được tick mới thành nháp; mỗi bài mang nhãn "AI soạn" và "Cần xem lại" | PO-47, PO-48 |
| BR-S2E4-09 | Phần lý thuyết trong tệp được bỏ qua kèm dòng "Phần lý thuyết chưa hỗ trợ ở chặng này" | PO-65 Q5 |
| BR-S2E4-10 | Tệp pdf không có lớp chữ (ảnh quét) bị từ chối kèm hướng dẫn dùng "Tải ảnh"; tệp hỏng hoặc đặt mật khẩu bị từ chối; lệnh gộp macro trong docx không chạy | BR-S2-04 |

### Tiêu chí chấp nhận

**US-S2E4-02-SC1 — Tách đúng từng bài tập của tệp docx** · Nguồn: PO-47
- Given: Tệp docx 3 bài tập và 1 đoạn lý thuyết
- When: Tải tệp
- Then: Hiện 3 mục bài tập có ô chọn; dòng "Phần lý thuyết chưa hỗ trợ ở chặng này"; chưa có đề nào vào Kho

**US-S2E4-02-SC2 — Chỉ bài được tick mới thành nháp** · Nguồn: BR-S2E4-08
- Given: Ba mục đã tách
- When: Tick bài 1 và 3; bấm "Tạo nháp"
- Then: Có 2 nháp (bài 1, bài 3), mỗi nháp nhãn "AI soạn" và "Cần xem lại"; bài 2 không tạo

**US-S2E4-02-SC3 — Tệp pdf có lớp chữ được tách như docx** · Nguồn: PO-47
- Given: Tệp pdf có lớp chữ chứa 2 bài
- When: Tải tệp
- Then: Hiện 2 mục bài tập

**US-S2E4-02-FC1 — Pdf ảnh quét bị từ chối** · Nguồn: BR-S2E4-10
- Given: Tệp pdf chỉ chứa ảnh quét
- When: Tải tệp
- Then: Hiện "Tệp không có lớp chữ. Hãy dùng Tải ảnh cho từng trang"; không tạo mục

**US-S2E4-02-FC2 — Tệp hỏng hoặc đặt mật khẩu bị từ chối** · Nguồn: BR-S2E4-10
- Given: Hai tệp: docx hỏng, pdf đặt mật khẩu
- When: Tải từng tệp
- Then: Tệp hỏng: "Không mở được tệp"; tệp mật khẩu: "Tệp đặt mật khẩu, hãy bỏ mật khẩu rồi tải lại"; không mục nào được tạo

**US-S2E4-02-BV1 — Tệp đúng 10 MB nhận, 10 MB cộng 1 byte bị chặn** · Nguồn: BR-S2E4-07; QA-S2E4-03
- Given: Hai tệp docx 10 485 760 và 10 485 761 byte
- When: Tải từng tệp
- Then: Tệp 1 nhận; tệp 2: PAYLOAD_TOO_LARGE, hiện "Tệp tối đa 10 MB"

**US-S2E4-02-BV2 — Tệp 50 bài nhận, 51 bài báo vượt** · Nguồn: BR-S2E4-07; QA-S2E4-03
- Given: Hai tệp: 50 bài và 51 bài
- When: Tải từng tệp
- Then: Tệp 50 bài: hiện đủ 50 mục; tệp 51 bài: hiện 50 mục đầu kèm "Tệp có 51 bài, chỉ lấy 50 bài đầu"

**US-S2E4-02-FC3 — Lệnh macro trong docx không chạy** · Nguồn: BR-S2E4-10
- Given: Tệp docx có macro tự chạy
- When: Tải tệp
- Then: Macro không chạy; tệp được đọc như văn bản; có dòng cảnh báo "Tệp có macro, đã bỏ qua"

**US-S2E4-02-FC4 — Định dạng xlsx chưa nhận** · Nguồn: BR-S2E4-07; PO-27
- Given: Tệp xlsx
- When: Tải tệp
- Then: Hiện "Chỉ nhận docx và pdf"; không mục nào được tạo

- **Mở và quyết định đang dùng:** QA-S2E4-03, QA-S2E4-04
- **DoR:** ready for R3, mức P1.

## US-S2E4-03 — Đối chiếu trùng với kho

- **Story:** Là giáo viên, tôi muốn thấy bài nào đã có trong kho khi import hoặc thêm đề, để không tạo bản trùng · Loại Logic · Mức P1 · Khung (như trên)
- **Nguồn:** PO-45, PO-47
- **Góc nhìn người vận hành:** import lần hai cùng tệp; bài giống nhau chỉ khác dấu cách; đề của giáo viên khác trong "Của tôi" không được lộ qua việc đối chiếu (đề "Của tôi" riêng tư).
- **Phụ thuộc:** US-S2E4-02, S2-E6

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S2E4-11 | Chuẩn hóa chữ (cắt khoảng trắng thừa, chữ thường, bỏ dấu câu cuối) rồi so; bằng nhau là "đã có trong kho"; độ giống từ 0,90 đến dưới 1,00 là "gần giống", chỉ báo và không chặn (đã chốt PO-66, QA-S2E4-05) | PO-47 |
| BR-S2E4-12 | Phạm vi đối chiếu: ngăn "Của tôi" của chính giáo viên và "Kho chung"; không đối chiếu với "Của tôi" của người khác | BR-S2-06, PO-47 |
| BR-S2E4-13 | Mục đã có trong kho ghi "đã có trong kho" và mặc định không tick; mục đã sửa ghi "cập nhật khi lưu"; không tạo bản trùng | PO-47 |

### Tiêu chí chấp nhận

**US-S2E4-03-SC1 — Bài trùng đề trong Kho chung hiện đã có trong kho** · Nguồn: PO-47
- Given: Kho chung có đề FX-10; tệp import chứa đúng đề FX-10
- When: Tải tệp
- Then: Mục FX-10 ghi "đã có trong kho", không tick sẵn; liên kết mở đề trong kho

**US-S2E4-03-SC2 — Khác khoảng trắng và hoa thường vẫn là trùng** · Nguồn: BR-S2E4-11
- Given: Kho có "Tam giác ABC có AB = 5."
- When: Import đề "tam giác abc có  ab = 5"
- Then: Nhận ra trùng sau chuẩn hóa; ghi "đã có trong kho"

**US-S2E4-03-BV1 — Độ giống 0,89 không báo, 0,90 báo gần giống, 1,00 là trùng** · Nguồn: BR-S2E4-11; QA-S2E4-05
- Given: Ba cặp đề giả lập độ giống 0,89 / 0,90 / 1,00
- When: Đối chiếu từng cặp
- Then: 0,89: không ghi chú; 0,90: "gần giống bài PLS-nn, xem?" (không chặn); 1,00: "đã có trong kho"

**US-S2E4-03-SC3 — Mục đã sửa ghi cập nhật khi lưu** · Nguồn: PO-47; BR-S2E4-13
- Given: Đề trong "Của tôi" của Cô Trang đã chỉnh số liệu so với bản import
- When: Import lại tệp
- Then: Mục ghi "cập nhật khi lưu"; lưu xong chỉ có một bản của đề (không trùng)

**US-S2E4-03-FC1 — Không lộ đề Của tôi của người khác qua đối chiếu** · Nguồn: BR-S2E4-12; BR-S2-06
- Given: Cô Lan có đề trùng trong "Của tôi"; Cô Trang import cùng đề
- When: Cô Trang import
- Then: Không ghi "đã có trong kho"; không nhắc tới đề của Cô Lan; đối chiếu chỉ so với Kho chung và Của tôi của Cô Trang

**US-S2E4-03-FC2 — Import lần hai cùng tệp không tạo bản trùng** · Nguồn: PO-47
- Given: Đã lưu 3 đề từ tệp T
- When: Import lại đúng tệp T và bấm lưu tất cả
- Then: Cả 3 mục ghi "đã có trong kho" và mặc định không tick; số đề trong "Của tôi" giữ 3

**US-S2E4-03-FC3 — Kho rỗng thì không mục nào bị đánh trùng** · Nguồn: BR-S2E4-11
- Given: Kho chung và Của tôi rỗng
- When: Import tệp 3 bài
- Then: Không có mục "đã có trong kho"; cả 3 tick sẵn

- **Mở và quyết định đang dùng:** QA-S2E4-05
- **DoR:** ready for R3, mức P1.

## Kiểm phủ

- Story trong epic: 3; tiêu chí: 27. Mọi story của epic trong `story-map-s23.md` đều có mặt, không story thừa.
- Câu hỏi mở và đề xuất đổi story map: `questions.md` cùng thư mục (không sửa story map, R2 đã khóa).

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: 3 story, 27 tiêu chí, ready for R3 | runner-T-261126-12 (BA) |
| v0.2 | 2026-11-26 | đổi mã mẫu `HS-`/`GV-` có ký tự 0, 1, O, I sang ký tự hợp lệ (BR-S2E1-04), RV_s23 m-01 | runner-T-261126-15 (QC soát phản biện, RV_s23) |
