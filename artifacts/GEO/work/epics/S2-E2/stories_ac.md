Phiên bản: v0.3 · 2026-11-26
# S2-E2 — Khung môn học và hình học phẳng (dựng từ ràng buộc, bộ đề chuẩn, đo bench): story Dev-Ready và tiêu chí chấp nhận

> Chủ sở hữu: BA (goal run T-261126-12, BA + QC liền). Nguồn: `artifacts/GEO/work/s23/story-map-s23.md` (R2 khóa 26/11/2026), `brief-s23.md`, `brd-delta-s23.md`, `STAGES-AFTER-MVP.md` Chặng 2 mục 5–8 (22/11/2026), `Design/s23/DQ.md` và ảnh `Design/s23/c2-*.png` (26/11/2026), `PO-DECISIONS.md` PO-61…65 (PO-61 mới nhất, thắng mọi nguồn cũ).
> **Trạng thái: ready for R3** · Mức epic: **P0** · Năm story: khung môn học, dựng hình phẳng, làm bài phẳng trên iPad/iPhone, bộ đề chuẩn, đo `make bench-s2` (mock). Khung C2-05 (concept `shots/16`).

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

## US-S2E2-01 — Khung môn học: `parse/solve/verify/render/hint` cộng `grade`, `concepts`

- **Story:** Là nhà phát triển, tôi muốn môn học cắm vào một khung chung có đủ sáu thao tác, để thêm môn thứ hai ở chặng 4 mà không viết lại luồng · Loại Tech · Mức P0 · Khung (không có khung)
- **Nguồn:** STAGES Ch2 mục 8–9; 0.3 ý 1; BR-S2-02
- **Góc nhìn người vận hành:** người đọc là Dev và QC; không có giao diện. Ca đáng kể: một môn thiếu một thao tác; môn Hình học không gian của MVP phải chạy y như cũ qua khung mới.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S2E2-01 | Khung môn học gồm sáu thao tác bắt buộc: `parse`, `solve`, `verify`, `render`, `hint`, `grade`, cộng danh sách `concepts` (mã khái niệm không rỗng) | BR-S2-02, STAGES Ch2 mục 8 |
| BR-S2E2-02 | Môn thiếu một thao tác thì không đăng ký được; hệ thống báo tên thao tác thiếu lúc khởi động, không đợi tới lúc dùng | BR-S2-02 |
| BR-S2E2-03 | Môn Hình học không gian của MVP chuyển sang khung mới mà kết quả bộ đề chuẩn MVP không đổi | 0.3 ý 1 |

### Tiêu chí chấp nhận

**US-S2E2-01-SC1 — Môn Hình học đăng ký đủ sáu thao tác và danh sách khái niệm** · Nguồn: BR-S2-02
- Given: Khung môn học đã nạp môn Hình học
- When: Hỏi danh mục môn của khung
- Then: Môn "Hình học" có đủ `parse`, `solve`, `verify`, `render`, `hint`, `grade`; `concepts` có ít nhất 1 mã khái niệm

**US-S2E2-01-FC1 — Môn thiếu một thao tác bị từ chối lúc khởi động** · Nguồn: BR-S2E2-02
- Given: Môn thử có `parse`, `solve`, `verify`, `render`, `grade`, `concepts` nhưng thiếu `hint`
- When: Khởi động khung với môn thử
- Then: Khung từ chối đăng ký môn; thông báo ghi tên thao tác thiếu là `hint`; không môn nào bị nạp dở

**US-S2E2-01-FC2 — Môn có `concepts` rỗng bị từ chối** · Nguồn: BR-S2E2-01
- Given: Môn thử đủ sáu thao tác nhưng `concepts` là danh sách rỗng
- When: Khởi động khung với môn thử
- Then: Khung từ chối; thông báo ghi `concepts` rỗng

**US-S2E2-01-FC3 — Gọi môn không tồn tại** · Nguồn: D-GEO-143
- Given: Khung chỉ có môn "Hình học"
- When: Gọi `solve` cho môn "Vật lý"
- Then: NOT_FOUND; không lỗi nội bộ

**US-S2E2-01-SC2 — Môn không gian của MVP không đổi kết quả qua khung mới** · Nguồn: BR-S2E2-03
- Given: Bộ đề chuẩn MVP (đề không gian) và đáp số tham chiếu của MVP
- When: Chạy bộ đề chuẩn MVP qua khung mới
- Then: Mỗi đề cho cùng đáp số và cùng nhãn máy như trước khi đổi khung (so từng đề); không đề nào đổi

**US-S2E2-01-SC3 — Thao tác `grade` có chữ ký chung cho mọi kiểu đáp số** · Nguồn: BR-S2E2-01; BR-S2-08
- Given: Môn Hình học nạp xong
- When: Gọi `grade` với đáp số mẫu 45, đáp số học sinh 45, sai số 0,5
- Then: Kết quả "đúng" kèm dạng chuẩn "45"; chữ ký `grade` giống ở mọi môn (cùng ba tham số: đáp số mẫu, đáp số nộp, dung sai)

**US-S2E2-01-FC4 — Môn lỗi giữa chừng không làm sập khung** · Nguồn: BR-S2E2-02
- Given: Môn thử có `solve` ném lỗi
- When: Gọi `solve`
- Then: Khung trả TIMEOUT hoặc lỗi dịch vụ theo D-GEO-143 và vẫn phục vụ môn khác

- **Mở và quyết định đang dùng:** không có
- **DoR:** ready for R3, mức P0.

## US-S2E2-02 — Dựng hình phẳng từ ràng buộc; `Problem.kind`; `SceneSpec` 2D

- **Story:** Là giáo viên, tôi muốn máy dựng hình phẳng từ các ràng buộc trong đề, để có hình đúng số liệu mà không phải vẽ tay · Loại Logic · Mức P0 · Khung C2-05
- **Nguồn:** STAGES Ch2 mục 3 ý 1, mục 8; BR-S2-02
- **Góc nhìn người vận hành:** người dùng thấy hình trong C2-02, C2-03, C2-05; QC kiểm bằng toạ độ trong `SceneSpec`. Ca đáng kể: số liệu mâu thuẫn (tam giác 5, 12, 20); đề thiếu dữ kiện; dạng hình ngoài danh sách hỗ trợ; tam giác suy biến.
- **Phụ thuộc:** US-S2E2-01

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S2E2-04 | `Problem.kind` là `plane` khi mọi điểm của đề nằm trong một mặt phẳng, là `space` khi có điểm ngoài mặt phẳng đáy | STAGES Ch2 mục 8 |
| BR-S2E2-05 | Dạng hỗ trợ ở chặng 2 (đã chốt PO-66, QA-S2E2-01): tam giác (thường, cân, đều, vuông), tứ giác (chữ nhật, vuông, hình thang, bình hành), đường tròn với dây và tiếp tuyến. Dạng ngoài danh sách đi nhánh "Cần xem lại", không dựng bừa | STAGES Ch2 mục 10 |
| BR-S2E2-06 | Hình "thỏa ràng buộc" khi mọi ràng buộc đề cho đúng với sai số tương đối không quá 1e-6 trên tọa độ dựng (đã chốt PO-66, QA-S2E2-04) | M-S2-01 |
| BR-S2E2-07 | `SceneSpec` chỉ chứa điểm, nhãn, ràng buộc, tọa độ; không chứa đáp số, bước giải hay gợi ý | STAGES Ch2 mục 0.2 nguyên tắc 1 |
| BR-S2E2-08 | Dựng hai lần cùng đề cho `SceneSpec` giống hệt từng ký tự | BR-S2-02 |

### Tiêu chí chấp nhận

**US-S2E2-02-SC1 — Dựng tam giác 5, 12, 13 thỏa ràng buộc** · Nguồn: STAGES Ch2 mục 3 ý 1
- Given: Đề PL-01 (FX-10)
- When: Gọi dựng hình
- Then: Ba cạnh dài 5, 12, 13 (sai số tương đối ≤ 1e-6); góc A = 90°; `Problem.kind` = `plane`

**US-S2E2-02-SC2 — Dựng tam giác cân tại A góc A 40°** · Nguồn: STAGES Ch2 mục 3 ý 1
- Given: Đề PL-02 (FX-11)
- When: Gọi dựng hình
- Then: AB = AC; góc A = 40°; góc B = 70° (sai số ≤ 1e-6)

**US-S2E2-02-SC3 — Dựng hình chữ nhật 6 × 8** · Nguồn: STAGES Ch2 mục 3 ý 1
- Given: Đề PL-03 (FX-12)
- When: Gọi dựng hình
- Then: AB = 6, AD = 8, bốn góc vuông, đường chéo AC = 10

**US-S2E2-02-SC4 — Dựng tam giác đều cạnh 4** · Nguồn: STAGES Ch2 mục 3 ý 1
- Given: Đề PL-04 (FX-13)
- When: Gọi dựng hình
- Then: Ba cạnh bằng 4; ba góc 60°; diện tích 4√3 xấp xỉ 6,9282

**US-S2E2-02-SC5 — Đề không gian vẫn mang loại `space`** · Nguồn: BR-S2E2-04
- Given: Đề SP-01 (FX-14)
- When: Gọi `parse`
- Then: `Problem.kind` = `space`; `SceneSpec` 3D như MVP, không đổi

**US-S2E2-02-SC6 — `SceneSpec` 2D không chứa đáp số hay bước giải** · Nguồn: BR-S2E2-07
- Given: Đề PL-01 đã dựng
- When: Lấy `SceneSpec` gửi xuống thiết bị; tìm trường đáp số, bước giải, gợi ý
- Then: Không có trường nào chứa đáp số hoặc bước giải; chỉ có điểm, nhãn, ràng buộc, tọa độ

**US-S2E2-02-SC7 — Dựng hai lần cho kết quả giống hệt** · Nguồn: BR-S2E2-08
- Given: Đề PL-02
- When: Dựng hình hai lần liên tiếp
- Then: Hai `SceneSpec` giống hệt từng ký tự

**US-S2E2-02-FC1 — Số liệu mâu thuẫn thì không dựng** · Nguồn: BR-S2E2-05
- Given: Đề "tam giác ABC có AB = 5, AC = 12, BC = 20"
- When: Gọi dựng hình
- Then: Không có `SceneSpec`; kết quả nhãn "Cần xem lại" kèm lý do "ba cạnh không lập thành tam giác"

**US-S2E2-02-BV1 — Tam giác suy biến (tổng hai cạnh bằng cạnh còn lại) bị từ chối** · Nguồn: BR-S2E2-05
- Given: Đề "AB = 5, AC = 5, BC = 10"
- When: Gọi dựng hình
- Then: Từ chối như FC1 (ba điểm thẳng hàng không phải tam giác)

**US-S2E2-02-BV2 — Tam giác gần suy biến vẫn dựng được** · Nguồn: BR-S2E2-06
- Given: Đề "AB = 5, AC = 5, BC = 9,99"
- When: Gọi dựng hình
- Then: Dựng được; ba cạnh đúng 5, 5, 9,99 trong sai số 1e-6; góc A xấp xỉ 179,8°

**US-S2E2-02-BV3 — Góc đề cho sát 0° hoặc 180°** · Nguồn: BR-S2E2-05
- Given: Hai đề: tam giác cân có góc A = 0° và góc A = 179°
- When: Gọi dựng hình cho từng đề
- Then: Góc A = 0°: từ chối, lý do "góc không hợp lệ"; góc A = 179°: dựng được và thỏa ràng buộc

**US-S2E2-02-FC2 — Đề thiếu ràng buộc thì nhãn "Cần xem lại"** · Nguồn: STAGES Ch2 mục 10
- Given: Đề "tam giác ABC có AB = 5; tính góc A"
- When: Gọi dựng hình
- Then: Không dựng hình duy nhất; trả "Cần xem lại" kèm lý do "hình chưa xác định, thiếu dữ kiện"; không có đáp số

**US-S2E2-02-FC3 — Dạng ngoài danh sách hỗ trợ đi nhánh Cần xem lại** · Nguồn: BR-S2E2-05; QA-S2E2-01
- Given: Đề về đường elip (dạng ngoài danh sách)
- When: Gọi dựng hình
- Then: Không dựng bừa; nhãn máy "Cần xem lại"; lý do "dạng chưa hỗ trợ"; hệ thống ghi một dòng thống kê dạng chưa hỗ trợ

**US-S2E2-02-FC4 — Đề có cả điểm ngoài mặt phẳng chạy nhánh không gian** · Nguồn: BR-S2E2-04
- Given: Đề có điểm S nằm ngoài mặt đáy
- When: Gọi `parse`
- Then: `Problem.kind` = `space`; không vào nhánh dựng phẳng

- **Mở và quyết định đang dùng:** QA-S2E2-01, QA-S2E2-04
- **DoR:** ready for R3, mức P0.

## US-S2E2-03 — Làm bài hình phẳng trên iPad và iPhone

- **Story:** Là học sinh, tôi muốn xem hình phẳng và dùng công cụ đo trên iPad hoặc iPhone, để hiểu đề mà không bị lộ đáp số · Loại UI · Mức P0 · Khung C2-05
- **Nguồn:** STAGES Ch2 mục 6 (C2-05); STAGES mục 0.2 nguyên tắc 1
- **Góc nhìn người vận hành:** học sinh chạm vào đỉnh để đo; ca đáng kể: đo ngay đại lượng đề yêu cầu tính (sẽ lộ đáp số); hình chưa kiểm chứng; xoay iPad; mạng chập chờn.
- **Phụ thuộc:** US-S2E2-02; S2-E1

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S2E2-09 | Công cụ đo cho phép đo độ dài và góc trên hình; riêng đại lượng chính là đáp số của đề thì bị khóa tới `release_at` (đã chốt PO-66, QA-S2E2-03) | STAGES mục 0.2 nguyên tắc 1 |
| BR-S2E2-10 | Nhãn hai trục luôn hiện ở đầu thẻ hình; đề có nhãn máy "Cần xem lại" hiện nhãn cho học sinh | PO-02, DQ-07 |
| BR-S2E2-11 | Chạm tối thiểu 44 điểm cho nút và đỉnh; không cuộn ngang trên iPhone 390 điểm | STAGES mục 0.2 nguyên tắc 6 (đã chốt PO-66, DQ-28) |

### Tiêu chí chấp nhận

**US-S2E2-03-SC1 — Mở bài phẳng trên iPad hiện hình, nhãn đỉnh và công cụ đo** · Nguồn: STAGES Ch2 mục 6
- Given: An đăng nhập trên iPad; bài PL-01 đã giao
- When: Mở bài PL-01
- Then: Hình tam giác ABC có nhãn A, B, C; có công cụ đo độ dài và đo góc; có bảng gợi ý; nhãn hai trục ở đầu thẻ hình

**US-S2E2-03-SC2 — Đo cạnh AB cho đúng 5** · Nguồn: BR-S2E2-09
- Given: Bài PL-01 mở, cạnh AB không phải đáp số
- When: Chọn công cụ đo độ dài; chạm A rồi B
- Then: Hiện "AB = 5" (làm tròn tới hai chữ số thập phân, dấu phẩy)

**US-S2E2-03-FC1 — Đo đại lượng là đáp số bị khóa** · Nguồn: BR-S2E2-09; QA-S2E2-03
- Given: Bài PL-01 hỏi góc A; lời giải còn khóa
- When: Chọn công cụ đo góc; chạm B, A, C
- Then: Không hiện số đo; hiện dòng "Đại lượng này là đáp số, chưa đo được"; dữ liệu trả về không chứa giá trị góc A

**US-S2E2-03-SC3 — Sau `release_at` công cụ đo mở cho đáp số** · Nguồn: BR-S2E2-09
- Given: Bài PL-01, giờ máy chủ vượt `release_at`
- When: Đo góc A
- Then: Hiện "A = 90°"

**US-S2E2-03-FC2 — Lời giải và đáp số khóa trong dữ liệu trả về** · Nguồn: STAGES mục 0.2 nguyên tắc 1
- Given: Bài PL-02 đang khóa
- When: Mở bài; xem toàn bộ dữ liệu trả về của mọi yêu cầu
- Then: Không có đáp số 70° hay bước giải ở bất kỳ dạng viết nào; tiêu đề `Cache-Control: no-store`

**US-S2E2-03-SC4 — Hình chưa kiểm chứng hiện nhãn "Cần xem lại"** · Nguồn: BR-S2E2-10; STAGES Ch2 mục 6
- Given: Bài có nhãn máy "Cần xem lại" (hình do AI dựng)
- When: Mở bài trên iPad
- Then: Đầu thẻ hình hiện "Cần xem lại" và "AI soạn"; vẫn làm bài được

**US-S2E2-03-SC5 — Bố cục iPhone một cột, không cuộn ngang** · Nguồn: BR-S2E2-11
- Given: iPhone 390 × 844, bài PL-03
- When: Mở bài
- Then: Hình ở trên, công cụ và gợi ý ở dưới; không cuộn ngang; mọi nút chạm được (≥ 44 điểm)

**US-S2E2-03-FC3 — Trạng thái đang tải và mất mạng** · Nguồn: STAGES Ch2 mục 6
- Given: Mạng chậm rồi ngắt khi mở bài
- When: Mở bài; ngắt mạng giữa chừng
- Then: Hiện màn "đang tải" có khung giữ chỗ (không hình rỗng); khi mất mạng hiện thông báo và nút "Thử lại"; không hiện hình nửa vời

**US-S2E2-03-BV1 — Xoay iPad giữ trạng thái đo** · Nguồn: BR-S2E2-11
- Given: Bài PL-03 đang mở với một phép đo AB hiển thị
- When: Xoay iPad từ dọc sang ngang
- Then: Hình vẽ lại cân đối; phép đo vẫn hiện; không mất gợi ý đã mở

- **Mở và quyết định đang dùng:** QA-S2E2-03
- **DoR:** ready for R3, mức P0.

## US-S2E2-04 — Dữ liệu: bộ đề chuẩn hình phẳng và đáp số mẫu

- **Story:** Là chủ sản phẩm, tôi muốn có bộ đề chuẩn hình phẳng với đáp số tham chiếu, để đo độ đúng của máy trước khi mở cho giáo viên · Loại Data · Mức P0 · Khung (dữ liệu)
- **Nguồn:** STAGES Ch2 mục 9
- **Góc nhìn người vận hành:** đáp số tham chiếu phải do người có chuyên môn Toán soát (STAGES mục 9). Ca đáng kể: chép sai đáp số, trùng mã đề, thiếu dạng hình.
- **Phụ thuộc:** US-S2E2-02

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S2E2-12 | Mỗi đề có mã `PLS-nn`, văn bản đề, danh sách ràng buộc, dạng hình, đáp số tham chiếu và kiểu đáp số; nguồn đáp số ghi người soát (đã chốt PO-66, QA-S2E2-02) | STAGES Ch2 mục 9 |
| BR-S2E2-13 | Bộ có ít nhất 30 đề; mỗi dạng hỗ trợ có ít nhất 5 đề (đã chốt PO-66, QA-S2E2-02) | M-S2-01, M-S2-02 |
| BR-S2E2-14 | Lệnh nạp kiểm đáp số tham chiếu với `solve` của engine; lệch thì ghi lỗi dữ liệu (bắt chép sai) | M-S2-02 |

### Tiêu chí chấp nhận

**US-S2E2-04-SC1 — Bộ chuẩn có đủ 30 đề với đủ trường** · Nguồn: BR-S2E2-12, 13
- Given: Tệp bộ đề chuẩn hình phẳng đã soạn
- When: Chạy lệnh nạp
- Then: Nạp đủ 30 đề; mỗi đề có mã, đề, ràng buộc, dạng, đáp số, kiểu đáp số, người soát

**US-S2E2-04-SC2 — Mỗi dạng hỗ trợ có tối thiểu 5 đề** · Nguồn: BR-S2E2-13
- Given: Bộ đã nạp
- When: Đếm đề theo dạng (tam giác, tứ giác, đường tròn)
- Then: Mỗi dạng có từ 5 đề trở lên

**US-S2E2-04-SC3 — Bốn đề mẫu FX-10…FX-13 có trong bộ với đáp số khớp** · Nguồn: BR-S2E2-12
- Given: Bộ đã nạp
- When: Tìm đề PL-01…PL-04
- Then: Đáp số: 90°, 70°, 10, `4√3` đúng như bảng `FX`

**US-S2E2-04-FC1 — Đề thiếu đáp số tham chiếu bị từ chối** · Nguồn: BR-S2E2-12
- Given: Tệp thử có một đề không có đáp số
- When: Chạy lệnh nạp
- Then: Đề đó vào báo cáo lỗi kèm mã đề; các đề khác vẫn nạp; mã thoát khác 0

**US-S2E2-04-FC2 — Trùng mã đề bị từ chối** · Nguồn: BR-S2E2-12
- Given: Tệp thử có hai đề cùng mã `PLS-07`
- When: Chạy lệnh nạp
- Then: Từ chối đề thứ hai; báo trùng mã `PLS-07`; mã thoát khác 0

**US-S2E2-04-FC3 — Đáp số tham chiếu lệch engine bị báo** · Nguồn: BR-S2E2-14
- Given: Tệp thử: đề PL-03 ghi đáp số 12 (đúng là 10)
- When: Chạy lệnh nạp
- Then: Báo lỗi dữ liệu: "đáp số tham chiếu 12 lệch kết quả engine 10" kèm mã đề; đề không vào bộ

**US-S2E2-04-BV1 — Bộ 29 đề bị báo thiếu, 30 đề đạt** · Nguồn: BR-S2E2-13
- Given: Hai bộ thử: 29 đề và 30 đề
- When: Chạy lệnh nạp từng bộ
- Then: Bộ 29 đề: cảnh báo "dưới 30 đề", mã thoát khác 0; bộ 30 đề: đạt

- **Mở và quyết định đang dùng:** QA-S2E2-02
- **DoR:** ready for R3, mức P0.

## US-S2E2-05 — Đo `make bench-s2` (mock): ≥ 95 % thỏa ràng buộc, ≥ 90 % đáp số, 0 sai mang nhãn

- **Story:** Là chủ sản phẩm, tôi muốn một lệnh đo độ đúng của máy trên bộ đề chuẩn, để biết khi nào đủ tin cậy mở cho giáo viên thật · Loại Tech · Mức P0 · Khung (báo cáo)
- **Nguồn:** M-S2-01…03 (BRD delta mục 1)
- **Góc nhìn người vận hành:** lệnh chạy trong CI với AI giả lập, không gọi AI thật (brief mục Ngoài phạm vi); số mẫu bằng 30 nên 95 % nghĩa là tối thiểu 29 đề (96,7 %), còn 28 đề (93,3 %) thì rớt; 90 % nghĩa là tối thiểu 27 đề (đúng 90,0 %).
- **Phụ thuộc:** US-S2E2-04

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S2E2-15 | M-S2-01 = đề thỏa ràng buộc / tổng đề; ngưỡng ≥ 95 %. M-S2-02 = đề đáp số khớp mẫu / tổng; ngưỡng ≥ 90 %. M-S2-03 = số bài sai mang nhãn "Đã kiểm chứng"; ngưỡng 0 | BRD delta mục 1 |
| BR-S2E2-16 | Lệnh thoát 0 khi cả ba ngưỡng đạt, khác 0 khi một ngưỡng rớt; báo cáo ghi ngày chạy, số đề, ba chỉ số, danh sách đề rớt kèm lý do | M-S2-01…03 |
| BR-S2E2-17 | Lệnh từ chối chạy khi cấu hình có khóa AI thật; chạy hai lần cùng dữ liệu cho cùng kết quả | D-GEO-047 |

### Tiêu chí chấp nhận

**US-S2E2-05-SC1 — Báo cáo có đủ ba chỉ số và danh sách đề rớt** · Nguồn: BR-S2E2-16
- Given: Bộ 30 đề đã nạp; AI giả lập
- When: Chạy `make bench-s2`
- Then: Báo cáo ghi ngày chạy, số đề 30, M-S2-01, M-S2-02, M-S2-03; có mục "đề rớt" (rỗng khi không có)

**US-S2E2-05-SC2 — Đạt cả ba ngưỡng thì thoát 0** · Nguồn: BR-S2E2-16
- Given: Kết quả mẫu: 30/30 thỏa ràng buộc, 28/30 đáp số khớp, 0 sai mang nhãn
- When: Chạy lệnh
- Then: Mã thoát 0; ba chỉ số đều được đánh dấu đạt

**US-S2E2-05-BV1 — Thỏa ràng buộc 29/30 đạt, 28/30 rớt** · Nguồn: BR-S2E2-15
- Given: Hai kết quả giả lập: 29 và 28 đề thỏa ràng buộc
- When: Chạy lệnh tính cho từng kết quả
- Then: 29/30 = 96,7 %: đạt; 28/30 = 93,3 %: rớt, mã thoát khác 0, danh sách đề rớt có 2 đề

**US-S2E2-05-BV2 — Đáp số khớp 27/30 đạt đúng ngưỡng, 26/30 rớt** · Nguồn: BR-S2E2-15
- Given: Hai kết quả giả lập: 27 và 26 đề khớp đáp số
- When: Chạy lệnh tính cho từng kết quả
- Then: 27/30 = 90,0 %: đạt (biên, dấu ≥); 26/30 = 86,7 %: rớt, mã thoát khác 0

**US-S2E2-05-BV3 — Một bài sai mang nhãn Đã kiểm chứng thì rớt** · Nguồn: BR-S2E2-15; M-S2-03
- Given: Kết quả giả lập: 30/30 ràng buộc, 30/30 đáp số nhưng 1 bài có đáp số sai mà nhãn máy là "Đã kiểm chứng"
- When: Chạy lệnh tính
- Then: M-S2-03 = 1 > 0: rớt; mã thoát khác 0; bài đó có tên trong danh sách rớt kèm "sai mang nhãn"

**US-S2E2-05-FC1 — Bộ đề rỗng không chia cho 0** · Nguồn: BR-S2E2-16
- Given: Bộ đề rỗng
- When: Chạy lệnh
- Then: Báo cáo ghi "—" cho tỉ lệ; mã thoát khác 0; thông báo "bộ đề rỗng"

**US-S2E2-05-FC2 — Có khóa AI thật thì từ chối chạy** · Nguồn: BR-S2E2-17
- Given: Biến môi trường chứa khóa AI thật
- When: Chạy lệnh
- Then: Từ chối chạy với thông báo "chỉ chạy với AI giả lập"; không gọi mạng

**US-S2E2-05-SC3 — Chạy hai lần cho cùng kết quả** · Nguồn: BR-S2E2-17
- Given: Cùng bộ đề, AI giả lập
- When: Chạy lệnh hai lần
- Then: Ba chỉ số và danh sách đề rớt giống hệt

- **Mở và quyết định đang dùng:** QA-S2E2-02
- **DoR:** ready for R3, mức P0.

## Kiểm phủ

- Story trong epic: 5; tiêu chí: 45. Mọi story của epic trong `story-map-s23.md` đều có mặt, không story thừa.
- Câu hỏi mở và đề xuất đổi story map: `questions.md` cùng thư mục (không sửa story map, R2 đã khóa).

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: 5 story, 45 tiêu chí, ready for R3 | runner-T-261126-12 (BA) |
| v0.2 | 2026-11-26 | đổi mã mẫu `HS-`/`GV-` có ký tự 0, 1, O, I sang ký tự hợp lệ (BR-S2E1-04), RV_s23 m-01 | runner-T-261126-15 (QC soát phản biện, RV_s23) |
| v0.3 | 2026-11-26 | Áp PO-66 (gate R3, T-261126-37): bỏ nhãn giả định chưa chốt, ghi "đã chốt PO-66" | runner-T-261126-37 (BA/QC) |
