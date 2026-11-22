# E2 — Hình đúng (dựng hình, hình mẫu, kiểm chứng, nhãn máy): story Dev-Ready và tiêu chí chấp nhận

> Tệp sinh bởi `work/src/build_stories.py` từ `work/src/E2_body.md`, `work/src/story_header.md` và `work/05_dictionary.md`; không sửa tay, sửa nguồn rồi chạy lại. Chủ sở hữu: BA.

## 0. Reader guide and glossary (hướng dẫn đọc và bảng mã; chép từ `work/glossary.md`)

**Cách đọc.** Mỗi mục `## US-GEO-nnn — …` là một story: câu story, góc nhìn người vận hành, quy tắc nghiệp vụ (BR), ánh xạ dữ liệu, bảng trạng thái (khi có), tiêu chí chấp nhận (AC). Mỗi AC nói **một** hành vi quan sát được, dạng Given / When / Then, với giá trị cụ thể của dữ liệu vàng `work/fixture.md`; dòng tiêu đề AC ghi nguồn. `SC` = kịch bản chính, `FC` = lỗi hoặc ngoại lệ. Người viết mã dựng theo BR và AC; người kiểm thử chạy AC. Mã chỉ cho biết **chỗ** một điều được ghi, câu chữ nói **điều đó là gì**.

**Quy ước chung cho mọi AC trong tệp này** (nguồn: tin M-004 trên `board/BOARD.md`; REQ-GEO-42, 43):
- "T0" là giờ máy chủ lúc bắt đầu ca kiểm; mọi mốc giờ là tương đối. Chỉ giờ máy chủ có giá trị; giờ của thiết bị không được tin. API dùng ISO-8601 UTC; màn hình hiện `HH:mm, dd/MM/yyyy` giờ Việt Nam.
- Lỗi của máy chủ có dạng `{code, message, requestId, details[]}`; `code` ổn định: VALIDATION_FAILED, INVALID_CURSOR, UNAUTHENTICATED, FORBIDDEN, NOT_FOUND, PAYLOAD_TOO_LARGE, TIMEOUT, CONFLICT (xung đột trạng thái: đổi giờ khi đã mở, hai lần đổi chồng nhau, giao lại cùng đề), RATE_LIMITED (HTTP 429, kèm `Retry-After`), SERVICE_OVERLOADED (HTTP 503, kèm `Retry-After`) (D-GEO-143). Màn hình hiện câu tiếng Việt của bảng thông điệp bên dưới qua thành phần `COM_LOI`.
- Danh sách phân trang theo con trỏ, tối đa 100 dòng mỗi trang (trần chung; Kho đề: trang 20, trần 50), không trả tổng số bản ghi; con trỏ mờ, gắn trường, loại danh sách và phiên bản (D-GEO-142, 168). Mã trường lấy từ thẻ xác thực, không nhận từ thiết bị. Thân yêu cầu tối đa 1 MB.
- Phản hồi chứa lời giải, gợi ý hoặc trạng thái khóa mang tiêu đề `Cache-Control: no-store` (D-GEO-145).
- "Dữ liệu trả về" nghĩa là toàn bộ nội dung máy chủ gửi xuống thiết bị cho yêu cầu đó (không chỉ phần giao diện vẽ ra).
- Thiết bị: học sinh dùng iPad (F-GEO-100) và iPhone (F-GEO-101) qua trình duyệt; giáo viên dùng máy tính (F-GEO-102) và điện thoại (F-GEO-103). Số thập phân hiển thị dùng dấu phẩy.
- AC mang chữ PROVISIONAL dựng trên một giả định chưa được xác nhận; mã `D-GEO-nnn` hoặc `QA-GEO-nn` đứng cạnh cho biết giả định nào. Ngưỡng hiệu năng luôn là đề xuất.

| Id / abbreviation | Meaning | Where it comes from (pointer only) |
|---|---|---|
| `US-GEO-nnn` · `US-GEO-nnn-SCn` / `-FCn` | story (hàng trăm = epic) · tiêu chí thành công / thất bại thứ n của story | tệp này |
| `BR-GEO-nn` | quy tắc nghiệp vụ; 01…20 của PO, 100…199 là làm rõ của BA (luôn dẫn quyết định hoặc yêu cầu) | `work/02_gate2_brd.md`, tệp này |
| `F-GEO-nnn` · `BG-n` | hàng dữ liệu vàng · bài giao mẫu số n trong dữ liệu vàng (BG-1…BG-5, BG-C) | `work/fixture.md` §3 |
| `REQ-GEO-nn` · `SRC-nnn` | yêu cầu thô · trích dẫn nguồn gốc | `work/00_gate0_intake_voc.md`, `work/00_sources.md` |
| `D-GEO-nnn` · `QA-GEO-nn` | quyết định (Final hoặc giả định của PO) · hỏi đáp còn mở của epic, kèm mặc định đang dùng | `work/03_decisions.md`, `work/epics/*/questions.md` |
| `M-GEO-nn` | công thức của một con số | `work/02_domain_model.md` §3 |
| `NFR-GEO-nnn` · `ADR-GEO-nn` | yêu cầu phi chức năng · quyết định kiến trúc (đề xuất của ARCH) | `work/architecture/06_nfr.md`, `01_adr.md` |
| `MSG-GEO-nn` | thông điệp hiện cho người dùng; câu chữ ở bảng bên dưới (mẫu của skill gọi là `MSG_nn`) | `work/05_dictionary.md` |
| `COM_*` | thành phần giao diện dùng chung | `work/05_dictionary.md` §3 |
| `HKG-nn` | mã bài của kho đề | `materials/Ngan-hang-bai-tap-Hinh-khong-gian.xlsx` |
| `RV-GEO-nn` | phát hiện của phản biện đối kháng (RV) mà một quy tắc hay AC sửa theo | `review/RV_findings.md` |
| `XF-nn` · `KG-nn` · `PO-nn` | phát hiện chéo epic · điểm còn hở · dòng của sổ quyết định gốc | `work/04_story_map.md`, `work/known_gaps.md`, `PO-DECISIONS.md` |
| `Q-CDT-nn` · `Q-GV-nn` · `Q-TL-nn` | câu hỏi cho ChuDauTu · GiaoVienCoVan · TechLead | `work/questions_for_stakeholders.md` |
| `GEO-nn`, `LRN-`, `ANS-`, `KNW-`, `PRF-`, `IAM-`, `TRU-` + số | mã chức năng của hồ sơ domain | `DOMAIN-PROFILE.md` §5 |
| `mvp-01…08` | tám khung thiết kế (tham chiếu hành vi, không phải nguồn yêu cầu) | `Design/mvp-*.png` |
| `release_at` | giờ mở lời giải của một bài giao; rỗng = Chưa đặt giờ | `work/02_domain_model.md` §2 |
| Chưa đặt giờ · Sắp mở · Đã mở | ba trạng thái giờ mở: chưa có `release_at` (khóa) · giờ máy chủ < `release_at` (khóa) · giờ máy chủ ≥ `release_at` (được xem) | M-GEO-13 |
| Đã kiểm chứng · Cần xem lại · AI soạn | nhãn máy (chỉ engine tính) · nhãn máy · nhãn người | `work/glossary.md` |
| Lộ đáp số | dữ liệu trả về hoặc câu trả lời chứa đáp số, giá trị quyết định ở mọi dạng viết tương đương, hoặc bước giải của lời giải đang khóa; tọa độ đỉnh của hình là dữ kiện của đề, không tính là lộ | D-GEO-065, 154 |

### Thông điệp dùng trong tệp này (chép từ `work/05_dictionary.md`)

| Mã | Loại | Câu chữ (nguyên văn) | Mã lỗi API |
|---|---|---|---|
| MSG-GEO-03 | Toast | "Bạn không có quyền thực hiện thao tác này." | FORBIDDEN |
| MSG-GEO-16 | Info (cố định trên hình) | "Hình này do AI dựng và chưa qua kiểm tra bằng tính toán. Em hãy đối chiếu với đề." | — |
| MSG-GEO-17 | Inline | "Hiện chưa hỗ trợ hình học phẳng. Em hãy nhập đề hình học không gian." | VALIDATION_FAILED |
| MSG-GEO-18 | Inline | "Chưa dựng được hình từ đề này. Em kiểm tra lại đề hoặc chọn một hình mẫu." | VALIDATION_FAILED |
| MSG-GEO-19 | Inline | "Giá trị phải là số lớn hơn 0." | VALIDATION_FAILED |

---

## US-GEO-201 — Engine dựng hình từ ràng buộc

- **Story:** Là học sinh, tôi muốn hình do engine tính từ ràng buộc của đề, để hình đúng thay vì tin tọa độ AI. · Loại Logic · Must · Cỡ L · Độ khó 5
- **Nguồn:** REQ-GEO-02, 47, 46 · D-GEO-040, 061 · SRC-074, SRC-053 · BR-GEO-06 · M-GEO-02
- **Góc nhìn người vận hành (M18):** học sinh và giáo viên không thấy engine, chỉ thấy hình và mấy thẻ ràng buộc ("SA = a") ở góc hình. Ca đáng kể: đề có ràng buộc mâu thuẫn; hình suy biến. Sai số sát ngưỡng là ca của người kiểm, giữ một cặp giá trị.
- **Phụ thuộc:** US-GEO-205; dùng bởi US-GEO-203, 401, 504.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-06 | Hình từ ràng buộc do engine tính tọa độ; không tin tọa độ AI. | SRC-053 |
| BR-GEO-127 | Một hình "thỏa ràng buộc" khi mọi ràng buộc của đề có sai số ≤ 1e-6 + 1e-12 (dung sai máy ε = 1e-12), tính trên hình đã quy cạnh lớn nhất về 1. Ví dụ vuông góc: `|tích vô hướng| / (|u|·|v|)` ≤ 1e-6 + 1e-12. | M-GEO-02; D-GEO-040, 163 |
| BR-GEO-128 | Loại ràng buộc phải dựng được ở MVP (suy từ sáu bài mẫu và danh mục hình mẫu): độ dài cạnh bằng một số hoặc một tham số; đáy là hình vuông, tam giác đều, tam giác vuông; đường thẳng vuông góc mặt phẳng; lăng trụ đứng; hình lập phương, hình hộp; nón và trụ theo bán kính, chiều cao hoặc đường sinh; mặt cầu ngoại tiếp hình lập phương. | REQ-GEO-02; `materials/`; D-GEO-056 |
| BR-GEO-129 | Engine là hàm thuần: cùng ràng buộc và tham số cho cùng tọa độ. | REQ-GEO-46 |
| BR-GEO-130 | Các ràng buộc độ dài của đề hiện thành thẻ ở góc hình, ví dụ "SA = a", "AD = a". | `04_story_map.md` (mvp-05, chip ràng buộc) |

- **Ánh xạ dữ liệu:** `architecture/03_schema.sql#scene_build`, `#problem_version`.

### Tiêu chí chấp nhận

**US-GEO-201-SC1 — Dựng hình HKG-01 thỏa mọi ràng buộc** · Nguồn: REQ-GEO-02; M-GEO-02; D-GEO-040 · PROVISIONAL D-GEO-040
- Given: đề HKG-01 (F-GEO-031) với a = 1.
- When: engine dựng hình.
- Then: hình có năm đỉnh S, A, B, C, D với AB = BC = CD = DA = 1, SA = 1, SA vuông góc AB và AD (sai số mỗi ràng buộc ≤ 1e-6); nguồn tọa độ được ghi là engine.

**US-GEO-201-SC2 — Dựng hình có số đo cụ thể** · Nguồn: REQ-GEO-02
- Given: đề HKG-03 (F-GEO-033): đáy tam giác đều cạnh 4 cm, chiều cao 6 cm.
- When: engine dựng hình.
- Then: ba cạnh đáy đều bằng 4, ba cạnh bên bằng 6 và vuông góc với đáy; đo cạnh đáy bằng công cụ Đo dài cho "4 cm".

**US-GEO-201-SC3 — Thẻ ràng buộc hiện ở góc hình** · Nguồn: `04_story_map.md` (mvp-05); REQ-GEO-02
- Given: An mở BG-1 (HKG-01) trên iPad.
- When: hình hiện xong.
- Then: góc dưới trái của khung hình có hai thẻ "SA = a" và "AD = a".

**US-GEO-201-SC4 — Sai số sát dưới ngưỡng vẫn thỏa** · Nguồn: M-GEO-02; D-GEO-040, 163 · PROVISIONAL D-GEO-163
- Given: một hình có ràng buộc vuông góc với sai số 9,9e-7 (F-GEO-073).
- When: kiểm hình.
- Then: kết quả "thỏa ràng buộc".

**US-GEO-201-SC5 — Cùng đầu vào cho cùng hình** · Nguồn: REQ-GEO-46
- Given: đề HKG-02 với a = 2.
- When: dựng hai lần liên tiếp.
- Then: hai bộ tọa độ giống nhau từng số.

**US-GEO-201-FC1 — Sai số vượt ngưỡng** · Nguồn: M-GEO-02; D-GEO-040, 061, 163 · PROVISIONAL D-GEO-163
- Given: hình có ràng buộc vuông góc với sai số 1,01e-6 (F-GEO-073).
- When: kiểm hình.
- Then: kết quả "không thỏa ràng buộc"; đề đó mang nhãn máy "Cần xem lại".

**US-GEO-201-FC2 — Ràng buộc mâu thuẫn** · Nguồn: REQ-GEO-02; REQ-GEO-43
- Given: đề tự nhập có đáy tam giác ABC với AB = 1, BC = 1, CA = 3 (F-GEO-074).
- When: An bấm "Dựng hình".
- Then: không có hình; hiện MSG-GEO-18; đề An đã gõ còn nguyên trong ô nhập; hệ thống không lặng lẽ thay bằng tọa độ AI.

**US-GEO-201-FC3 — Hình suy biến** · Nguồn: REQ-GEO-02
- Given: đề tự nhập "Lăng trụ đứng tam giác có đáy là tam giác đều cạnh 4 cm, chiều cao 0 cm. Tính thể tích."
- When: bấm "Dựng hình".
- Then: không có hình; hiện MSG-GEO-18.

- **Mở và quyết định đang dùng:** D-GEO-040 (dung sai, Q-GV-01); D-GEO-163 (so sánh có dung sai máy ε = 1e-12; biên của F-GEO-073 dời vào trong và ra ngoài ngưỡng để không phụ thuộc làm tròn số thực). Danh sách loại ràng buộc BR-GEO-128 là suy từ `materials/`, không phải danh sách của chủ đầu tư.
- **DoR:** Ready, PROVISIONAL theo D-GEO-040.

## US-GEO-202 — Dựng hình mẫu có tham số không cần AI

- **Story:** Là học sinh, tôi muốn chọn một hình mẫu và đổi tham số, để khám phá mà không cần gõ đề. · Loại UI · Must · Cỡ M · Độ khó 3
- **Nguồn:** REQ-GEO-03 · D-GEO-056 · SRC-075, SRC-114
- **Góc nhìn người vận hành (M18):** học sinh rảnh tay bấm một hình ở "Tự khám phá" để xoay thử, thỉnh thoảng đổi số. Ca đáng kể: gõ số 0, số âm hoặc chữ vào ô tham số.
- **Phụ thuộc:** US-GEO-205, 201; đi vào từ US-GEO-403.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-131 | Hình mẫu dựng hoàn toàn bằng engine, không có lượt gọi AI nào. | REQ-GEO-03 |
| BR-GEO-132 | Tham số là số lớn hơn 0; nhận dấu phẩy thập phân ("2,5"). Phạm vi trên và đơn vị chưa có nguồn (D-GEO-109). | REQ-GEO-43; D-GEO-109 |

- **Ánh xạ dữ liệu:** danh mục hình mẫu của US-GEO-205; `architecture/03_schema.sql#scene_build`.

### Tiêu chí chấp nhận

**US-GEO-202-SC1 — Chọn hình mẫu và thấy hình ngay, không gọi AI** · Nguồn: REQ-GEO-03; D-GEO-056
- Given: An ở màn "Tự học"; số bản ghi lượt gọi AI hiện có là n.
- When: An bấm thẻ "Hình nón".
- Then: hình nón hiện với tham số mặc định, có ô tham số "Bán kính đáy" và "Chiều cao"; số bản ghi lượt gọi AI vẫn là n; không hiện màn chờ AI.

**US-GEO-202-SC2 — Đổi tham số thì hình đổi theo** · Nguồn: REQ-GEO-03
- Given: hình nón đang hiện.
- When: An đặt Bán kính đáy 3, Chiều cao 4 (F-GEO-075).
- Then: hình vẽ lại với bán kính 3 và chiều cao 4; đo đường sinh bằng công cụ Đo dài cho "5".

**US-GEO-202-SC3 — Nhận số thập phân dấu phẩy** · Nguồn: REQ-GEO-43
- Given: hình nón đang hiện.
- When: gõ Bán kính đáy "2,5".
- Then: hình vẽ lại với bán kính 2,5; ô vẫn hiện "2,5".

**US-GEO-202-SC4 — Sáu hình mẫu đều dựng được** · Nguồn: D-GEO-056
- Given: màn "Tự học" trên iPad (F-GEO-100) và trên iPhone (F-GEO-101).
- When: lần lượt bấm "Hình chóp", "Lăng trụ", "Hình hộp", "Hình nón", "Hình trụ", "Hình cầu".
- Then: mỗi lần đều có hình với tham số mặc định, xoay được.

**US-GEO-202-FC1 — Tham số không hợp lệ** · Nguồn: REQ-GEO-03, 43; D-GEO-109 · PROVISIONAL D-GEO-109
- Given: hình nón đang hiện với bán kính 3.
- When: gõ Bán kính đáy lần lượt "0", "−2", "abc" (F-GEO-075).
- Then: mỗi lần hiện MSG-GEO-19 dưới ô; hình giữ nguyên bán kính 3.

- **Mở và quyết định đang dùng:** D-GEO-109 (phạm vi, đơn vị, giá trị mặc định của tham số).
- **DoR:** Ready, một AC PROVISIONAL theo D-GEO-109.

## US-GEO-203 — Kiểm chứng hình và đáp số, gắn nhãn máy

- **Story:** Là giáo viên và học sinh, tôi muốn mỗi bài có nhãn do máy tính, để biết hình và đáp số đã được tính lại hay cần xem lại. · Loại Logic · Must · Cỡ L · Độ khó 5
- **Nguồn:** REQ-GEO-04, 34, 47 · D-GEO-004 (Final), 040, 061, 079, 140, 163 · SRC-076, SRC-054, SRC-113 · BR-GEO-07, 09, 14 · M-GEO-02, 03, 05 · NFR-GEO-021, 026
- **Góc nhìn người vận hành (M18):** giáo viên nhìn nhãn trên thẻ đề để quyết định giao; học sinh nhìn nhãn cạnh đề khi làm bài. Ca đáng kể: đáp số AI sai nhưng trông hợp lý; đáp số hai giá trị chỉ đúng một; AI tự khai là đã kiểm chứng. Đây là chỗ không để AI tự quyết.
- **Phụ thuộc:** US-GEO-201; dùng bởi US-GEO-502, 504, 105.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-07 | "Đã kiểm chứng" chỉ khi đáp số khớp tính toán; còn lại "Cần xem lại". | SRC-054 |
| BR-GEO-133 | "Đã kiểm chứng" cần đủ ba điều: hình do engine dựng thỏa ràng buộc; mọi đại lượng đề hỏi khớp; nguồn tọa độ là engine. | D-GEO-061 |
| BR-GEO-134 | Đề có tham số: đáp số khớp khi, với từng đại lượng đề hỏi và từng bộ trong ít nhất 3 bộ giá trị tham số, `|q_AI − q_tính| ≤ 1e-4 · max(1, |q_tính|) + 1e-12`. Đề không có tham số (mọi số đo cho bằng số, ví dụ HKG-03): tính một lần trên giá trị số của đề, khớp khi `|q_AI − q_tính| ≤ tol + 1e-12` với tol = max(1e-6, nửa đơn vị của chữ số thập phân cuối mà đáp số ghi). Ví dụ: "41,57 cm³" ghi hai chữ số thập phân nên tol = 0,005, lệch 24√3 ≈ 41,5692 khoảng 7,8e-4 → khớp; "41,56 cm³" lệch khoảng 9,2e-3 → không khớp; đáp số dạng căn "24√3 cm³" dùng tol = 1e-6. | M-GEO-03; D-GEO-040, 163, 178 (sửa D-GEO-140) |
| BR-GEO-135 | Nhãn máy chỉ do engine tính. Nhãn do AI tự khai, nhãn trong `materials/*.xlsx` và thao tác của giáo viên đều không đặt được nhãn máy. | D-GEO-004; BR-GEO-14; REQ-GEO-34 |
| BR-GEO-136 | Chuỗi nhãn là đúng "Đã kiểm chứng" hoặc "Cần xem lại" trên mọi màn; không dùng "Hình đã kiểm chứng". | D-GEO-079 |
| BR-GEO-09 | Engine chỉ kiểm chứng tính toán, không kiểm chứng lập luận; lời giải vẫn mang "AI soạn". | SRC-056 |

**Bảng trạng thái nhãn của một đề**

| Hình engine thỏa ràng buộc | Mọi đáp số khớp | Nguồn tọa độ | Nhãn máy | Cảnh báo |
|---|---|---|---|---|
| có | có | engine | Đã kiểm chứng | không |
| có | không (dù chỉ một giá trị) | engine | Cần xem lại | không |
| không | bất kỳ | engine | Cần xem lại | không |
| không áp dụng (đề lạ) | không kiểm được | AI | Cần xem lại | MSG-GEO-16 |

- **Ánh xạ dữ liệu:** `architecture/03_schema.sql#verification_report`, `#solution`, `#scene_build`.

### Tiêu chí chấp nhận

**US-GEO-203-SC1 — Đề khớp mang "Đã kiểm chứng"** · Nguồn: REQ-GEO-04; BR-GEO-07; D-GEO-040, 061 · PROVISIONAL D-GEO-040
- Given: HKG-01 với đáp số AI "45°" (F-GEO-031); góc tính từ tọa độ engine là 45°.
- When: engine kiểm chứng.
- Then: nhãn máy của HKG-01 là "Đã kiểm chứng".

**US-GEO-203-SC2 — Đề không khớp mang "Cần xem lại"** · Nguồn: REQ-GEO-04; BR-GEO-07
- Given: HKG-01 với đáp số AI "60°" (F-GEO-070).
- When: engine kiểm chứng.
- Then: nhãn máy là "Cần xem lại".

**US-GEO-203-SC3 — Đáp số theo tham số kiểm trên ba bộ giá trị** · Nguồn: M-GEO-03; D-GEO-040 · PROVISIONAL D-GEO-040
- Given: HKG-02 với đáp số "a" (F-GEO-032).
- When: engine kiểm chứng với a = 1, 2, 3.
- Then: khoảng cách tính được lần lượt 1, 2, 3, khớp cả ba; nhãn "Đã kiểm chứng"; báo cáo kiểm chứng ghi kết quả của từng bộ.

**US-GEO-203-SC4 — Sai lệch sát dưới ngưỡng vẫn khớp** · Nguồn: M-GEO-03; D-GEO-040, 163 · PROVISIONAL D-GEO-163
- Given: giá trị tính 45, đáp số AI 45,0044 (F-GEO-071; ngưỡng 1e-4 · 45 + 1e-12 ≈ 0,0045).
- When: so đáp số.
- Then: "khớp" (|45,0044 − 45| = 0,0044 nhỏ hơn ngưỡng).

**US-GEO-203-SC5 — Nhãn do engine tính có thể khác nhãn trong tệp nguồn** · Nguồn: D-GEO-004 (Final); D-GEO-105 · PROVISIONAL D-GEO-105
- Given: HKG-06 (F-GEO-036): tệp nguồn ghi "Cần xem lại"; với a = 1 engine tính R ≈ 0,8660 và V ≈ 2,7207, khớp đáp số "R = a√3/2; V = (√3/2)·π·a³".
- When: engine kiểm chứng.
- Then: nhãn máy là "Đã kiểm chứng".

**US-GEO-203-SC6 — Một chuỗi nhãn trên mọi màn** · Nguồn: D-GEO-079; BR-GEO-14 · PROVISIONAL D-GEO-079
- Given: HKG-01 mang "Đã kiểm chứng".
- When: Giáo viên mẫu xem thẻ đề ở "Kho đề" và trang chi tiết đề; An xem BG-1 ở màn Làm bài trên iPad và iPhone.
- Then: cả bốn chỗ hiện đúng chuỗi "Đã kiểm chứng"; không chỗ nào ghi "Hình đã kiểm chứng".

**US-GEO-203-FC1 — Sai lệch vượt ngưỡng** · Nguồn: M-GEO-03; D-GEO-040, 163 · PROVISIONAL D-GEO-163
- Given: giá trị tính 45, đáp số AI 45,0046 (F-GEO-071; |45,0046 − 45| = 0,0046 lớn hơn ngưỡng ≈ 0,0045).
- When: so đáp số.
- Then: "không khớp"; nhãn "Cần xem lại".

**US-GEO-203-FC2 — Đáp số hai giá trị, một giá trị sai** · Nguồn: M-GEO-03
- Given: HKG-04 với đáp số AI "Sxq = 15π cm²; V = 16π cm³" (F-GEO-072); engine tính 15π và 12π.
- When: engine kiểm chứng.
- Then: nhãn "Cần xem lại"; báo cáo kiểm chứng ghi diện tích xung quanh khớp, thể tích không khớp.

**US-GEO-203-FC3 — AI tự khai nhãn bị bỏ qua** · Nguồn: D-GEO-004 (Final); REQ-GEO-34
- Given: kết quả AI cho HKG-01 có đáp số "60°" (F-GEO-070) và tự ghi kèm trạng thái "Đã kiểm chứng" (kịch bản F-GEO-112).
- When: hệ thống lưu lời giải và kiểm chứng.
- Then: nhãn máy là "Cần xem lại"; trạng thái AI tự khai không được dùng ở bất kỳ màn nào.

**US-GEO-203-FC4 — Giáo viên không đặt được nhãn máy** · Nguồn: BR-GEO-14; D-GEO-020 · PROVISIONAL D-GEO-020
- Given: Giáo viên mẫu ở trang chi tiết của một đề mang "Cần xem lại".
- When: xem mọi điều khiển của trang và gửi thẳng tới máy chủ một yêu cầu đổi nhãn thành "Đã kiểm chứng".
- Then: trang không có điều khiển đổi nhãn máy; yêu cầu gửi thẳng nhận `FORBIDDEN` (MSG-GEO-03); nhãn vẫn "Cần xem lại".

**US-GEO-203-FC5 — Nhãn không kéo theo giá trị tính xuống thiết bị học sinh** · Nguồn: BR-GEO-01; REQ-GEO-40; NFR-GEO-021
- Given: BG-1 đang Sắp mở; An mở bài.
- When: ghi lại dữ liệu trả về của trang Làm bài.
- Then: có nhãn "Đã kiểm chứng"; không có giá trị tính của báo cáo kiểm chứng ("45", "45°") và không có đáp số.

**US-GEO-203-SC7 — Đề không tham số khớp theo dung sai tuyệt đối** · Nguồn: M-GEO-03; D-GEO-163, 178 · PROVISIONAL D-GEO-178
- Given: HKG-03 (F-GEO-033), đề không có tham số; đáp số AI "24√3 cm³" (F-GEO-079); engine tính thể tích từ tọa độ với cạnh đáy 4, chiều cao 6.
- When: engine kiểm chứng.
- Then: tính một lần (không cần ba bộ giá trị tham số); |24√3 − giá trị tính| ≤ 1e-6 + 1e-12 nên "khớp"; nhãn máy "Đã kiểm chứng"; báo cáo kiểm chứng ghi dung sai tuyệt đối đã dùng.

**US-GEO-203-FC6 — Đề không tham số: lệch vượt dung sai theo chữ số ghi** · Nguồn: M-GEO-03; D-GEO-163, 178 · PROVISIONAL D-GEO-178
- Given: HKG-03 với đáp số AI "41,56 cm³" (F-GEO-079); giá trị tính 24√3 ≈ 41,569219.
- When: engine kiểm chứng.
- Then: dung sai = max(1e-6; 0,005) = 0,005; lệch khoảng 9,2e-3 lớn hơn 0,005 + 1e-12 nên "không khớp"; nhãn máy "Cần xem lại".

**US-GEO-203-SC8 — Đáp số thập phân khớp theo nửa đơn vị chữ số cuối** · Nguồn: M-GEO-03; D-GEO-163, 178 · PROVISIONAL D-GEO-178
- Given: HKG-03 với đáp số AI "41,57 cm³" (F-GEO-079); giá trị tính 24√3 ≈ 41,569219.
- When: engine kiểm chứng.
- Then: đáp số ghi hai chữ số thập phân nên dung sai = max(1e-6; 0,005) = 0,005; lệch khoảng 7,8e-4 nên "khớp"; nhãn máy "Đã kiểm chứng".

- **Mở và quyết định đang dùng:** D-GEO-040 (Q-GV-01), D-GEO-079, 020 (Q-CDT-12), D-GEO-105; D-GEO-178 (sửa D-GEO-140: đề không tham số dùng dung sai tuyệt đối max(1e-6, nửa đơn vị chữ số thập phân cuối mà đáp số ghi); FC6 đổi từ "41,57" sang "41,56", "41,57" khớp ở SC8); D-GEO-163 (so sánh có ε = 1e-12; biên F-GEO-071 là 45,0044 và 45,0046). Ca "không lưu được nhãn Đã kiểm chứng khi chưa khớp" nằm ở US-GEO-105-FC3 (NFR-GEO-026).
- **DoR:** Ready, PROVISIONAL theo D-GEO-040, 079.

## US-GEO-204 — Đề lạ dùng tọa độ AI kèm cảnh báo chưa kiểm chứng

- **Story:** Là học sinh, tôi muốn đề không khớp hình mẫu vẫn có hình nhưng được cảnh báo, để không tin nhầm hình chưa kiểm. · Loại UI · Must · Cỡ M · Độ khó 4
- **Nguồn:** REQ-GEO-01, 04, 51 · D-GEO-061 · SRC-055 · BR-GEO-08 · M-GEO-18
- **Góc nhìn người vận hành (M18):** học sinh dán một đề trong sách nâng cao mà hệ thống chưa có hình mẫu. Ca đáng kể: đề thật ra là hình phẳng; AI trả một hình hỏng.
- **Phụ thuộc:** US-GEO-401, 103, 203.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-08 | Đề lạ dùng tọa độ AI và hiện cảnh báo chưa kiểm chứng. | SRC-055 |
| BR-GEO-137 | Đề lạ luôn mang "Cần xem lại" và MSG-GEO-16 cố định trên khung hình; học sinh không tắt được cảnh báo. | D-GEO-061 |
| BR-GEO-138 | Đề hình học phẳng không được dựng ở MVP: báo MSG-GEO-17. | REQ-GEO-51 (OUT) |

- **Ánh xạ dữ liệu:** `architecture/03_schema.sql#scene_build` (nguồn tọa độ), `#verification_report`.

### Tiêu chí chấp nhận

**US-GEO-204-SC1 — Đề lạ có hình kèm cảnh báo** · Nguồn: REQ-GEO-01, 04; BR-GEO-08; D-GEO-061
- Given: An ở "Tự học", gõ đề bát diện đều (F-GEO-061).
- When: bấm "Dựng hình" và nhà cung cấp giả lập trả tọa độ sáu đỉnh (kịch bản F-GEO-110).
- Then: hình hiện; cạnh đề có nhãn "Cần xem lại"; trên khung hình có MSG-GEO-16.

**US-GEO-204-SC2 — Cảnh báo luôn hiện** · Nguồn: BR-GEO-08; D-GEO-061
- Given: hình đề lạ của SC1 đang hiện.
- When: An xoay hình, đổi qua tab "Hỏi đáp" rồi quay lại, tải lại trang.
- Then: MSG-GEO-16 và nhãn "Cần xem lại" vẫn hiện; không có nút đóng cảnh báo.

**US-GEO-204-SC3 — Đề lạ được đếm vào tỉ lệ đề lạ** · Nguồn: M-GEO-18
- Given: trong kỳ đo An đã dựng hai đề tự nhập: F-GEO-060 (khớp hình mẫu) và F-GEO-061 (đề lạ).
- When: tính tỉ lệ đề lạ.
- Then: 1 / 2 = 50,0%.

**US-GEO-204-FC1 — Đề lạ không bao giờ "Đã kiểm chứng"** · Nguồn: D-GEO-061; BR-GEO-07
- Given: đề lạ F-GEO-061 có đáp số AI.
- When: hệ thống gắn nhãn.
- Then: nhãn là "Cần xem lại", bất kể đáp số AI là gì.

**US-GEO-204-FC2 — Tọa độ AI hỏng** · Nguồn: REQ-GEO-01; D-GEO-072 · PROVISIONAL D-GEO-072
- Given: đề lạ F-GEO-061; nhà cung cấp giả lập trả sáu đỉnh trùng một điểm (kịch bản F-GEO-111).
- When: hệ thống nhận kết quả.
- Then: không vẽ hình; hiện MSG-GEO-18; đề còn nguyên trong ô nhập.

**US-GEO-204-FC3 — Đề hình phẳng** · Nguồn: REQ-GEO-51 (OUT, hệ quả người dùng thấy)
- Given: An gõ đề tam giác vuông (F-GEO-064).
- When: bấm "Dựng hình".
- Then: hiện MSG-GEO-17; không có hình; không có bài tự học nào được tạo.

- **Mở và quyết định đang dùng:** D-GEO-061 (nguồn tài liệu). Cách nhận ra "hình phẳng" và "không khớp hình mẫu" thuộc cổng AI (AI giả lập ở MVP, D-GEO-047); AC chỉ nói kết quả người dùng thấy.
- **DoR:** Ready.

## US-GEO-205 — Dữ liệu: danh mục hình mẫu và tham số

- **Story:** Là nhóm sản phẩm, tôi muốn danh mục hình mẫu có tên, tham số và ràng buộc, để dùng chung cho dựng hình, tự học và kiểm. · Loại Data · Must · Cỡ S · Độ khó 2
- **Nguồn:** REQ-GEO-03 · D-GEO-056, 076 · SRC-075, SRC-114
- **Góc nhìn người vận hành (M18):** người nạp dữ liệu chạy một lần; lỗi đáng kể duy nhất là hình thiếu giá trị mặc định làm thẻ hình mẫu bấm không ra hình.
- **Phụ thuộc:** không.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-139 | Danh mục gồm: chóp, lăng trụ, hộp (gồm lập phương), nón, trụ, cầu ở mức cơ bản, và hệ trục Oxyz. | D-GEO-056 |
| BR-GEO-140 | Mỗi hình mẫu có tên hiển thị, danh sách tham số, giá trị mặc định lớn hơn 0 cho từng tham số, và ràng buộc để engine dựng. | REQ-GEO-03; D-GEO-109 |
| BR-GEO-110 | Danh mục là nội dung hệ thống, mọi trường đọc được. | D-GEO-076 |

- **Ánh xạ dữ liệu:** chỗ lưu danh mục hình mẫu do ARCH bổ sung vào `architecture/03_schema.sql` trước khi tách migration (D-GEO-133); BA trỏ tới bảng khi có, không chép.

### Tiêu chí chấp nhận

**US-GEO-205-SC1 — Danh mục đủ hình** · Nguồn: D-GEO-056
- Given: dữ liệu danh mục hình mẫu.
- When: nạp.
- Then: có đủ chóp, lăng trụ, hộp, lập phương (hộp ba cạnh bằng nhau), nón, trụ, cầu; hệ trục Oxyz bật được trên mọi hình.

**US-GEO-205-SC2 — Sáu thẻ hình mẫu trên màn "Tự học"** · Nguồn: D-GEO-056; `Design/mvp-07`
- Given: An mở "Tự học".
- When: xem mục "Hoặc chọn một hình mẫu".
- Then: có đúng sáu thẻ theo thứ tự "Hình chóp", "Lăng trụ", "Hình hộp", "Hình nón", "Hình trụ", "Hình cầu".

**US-GEO-205-SC3 — Trường nào cũng đọc được danh mục** · Nguồn: D-GEO-076 · PROVISIONAL D-GEO-076
- Given: học sinh An của lớp 9C, trường "THCS Mẫu C" (F-GEO-026).
- When: mở "Tự học".
- Then: thấy cùng sáu thẻ hình mẫu như An lớp 11A1.

**US-GEO-205-FC1 — Hình mẫu thiếu giá trị mặc định** · Nguồn: REQ-GEO-03; D-GEO-109 · PROVISIONAL D-GEO-109
- Given: dữ liệu danh mục trong đó "Hình trụ" không có giá trị mặc định cho "Chiều cao".
- When: nạp.
- Then: việc nạp báo "Hình trụ: thiếu giá trị mặc định của Chiều cao" và không đưa hình đó vào danh mục.

- **Mở và quyết định đang dùng:** D-GEO-056 (nguồn tài liệu); D-GEO-109 (giá trị mặc định cụ thể chưa có nguồn nên dữ liệu vàng không ghi số); D-GEO-133 (ARCH bổ sung bảng lưu danh mục).
- **DoR:** Ready.
