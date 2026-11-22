# E5 — Kho đề của Giáo viên mẫu: story Dev-Ready và tiêu chí chấp nhận

> Tệp sinh bởi `work/src/build_stories.py` từ `work/src/E5_body.md`, `work/src/story_header.md` và `work/05_dictionary.md`; không sửa tay, sửa nguồn rồi chạy lại. Chủ sở hữu: BA.

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
| MSG-GEO-01 | Inline | "Thông tin chưa hợp lệ. Vui lòng kiểm tra lại các ô được đánh dấu." | VALIDATION_FAILED |
| MSG-GEO-03 | Toast | "Bạn không có quyền thực hiện thao tác này." | FORBIDDEN |
| MSG-GEO-04 | Page state | "Không tìm thấy nội dung này." | NOT_FOUND |
| MSG-GEO-05 | Inline | "Danh sách đã thay đổi. Vui lòng tải lại từ đầu." | INVALID_CURSOR |
| MSG-GEO-40 | Empty | "Không có đề nào khớp bộ lọc. Hãy bỏ bớt điều kiện." | — |
| MSG-GEO-54 | Empty | "Đề này chưa có lời giải." | — |

---

> Dùng chung cho cả epic: người dùng là Giáo viên mẫu (F-GEO-003) trên máy tính (F-GEO-102) trừ khi AC nói khác. Kho thử là F-GEO-080: 48 đề HKG-01…HKG-48, 44 đề "Đã kiểm chứng", 4 đề "Cần xem lại"; 24 THPT, 24 THCS; theo dạng bài: Góc 8, Khoảng cách 8, Thể tích 16, Diện tích 8, Mặt cầu 8; theo hình: chóp 8, lập phương 8, lăng trụ 8, nón 8, trụ 8, cầu 8 (mỗi đề gốc HKG-01…06 nhân thành 8 đề). Kho đề là nội dung hệ thống dùng chung mọi trường (D-GEO-076). Soạn đề, import, sửa lời giải, trình chiếu là ngoài MVP (REQ-GEO-52, 54).

## US-GEO-501 — Xem Kho đề và lọc theo cấp, loại hình, dạng bài

- **Story:** Là Giáo viên mẫu, tôi muốn xem Kho đề và lọc theo cấp, loại hình, dạng bài, để chọn đề giao. · Loại UI · Must · Cỡ M · Độ khó 3
- **Nguồn:** REQ-GEO-20, 39 · D-GEO-070, 076, 112, 122 · SRC-092, SRC-117 · M-GEO-07 · `Design/mvp-01`
- **Góc nhìn người vận hành (M18):** giáo viên mở Kho đề trước buổi dạy, lọc "THPT" và "Góc" rồi chọn một đề để giao. Ca đáng kể: lọc ra không có đề nào; xem nhanh trên điện thoại.
- **Phụ thuộc:** US-GEO-102, 504, 505.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-202 | Ba nhóm lọc, mỗi nhóm chọn một giá trị hoặc "Tất cả": "Cấp" {THCS, THPT}; "Hình" {Hình chóp, Lăng trụ, Hình hộp, Lập phương, Nón, Trụ, Cầu}; "Dạng bài" {Góc, Khoảng cách, Thể tích, Diện tích, Mặt cầu}. Kết quả là các đề thỏa đồng thời mọi nhóm đang chọn. | D-GEO-070; REQ-GEO-20 |
| BR-GEO-203 | Dòng đầu trang "{n} đề · {m} đã kiểm chứng · tài liệu của Giáo viên mẫu": n, m lấy từ bộ đếm của kho (M-GEO-07), không đổi theo bộ lọc; API danh sách không trả tổng. | D-GEO-122; M-004 (2) |
| BR-GEO-204 | Mỗi thẻ đề có: hình thu nhỏ, cấp, dạng bài, nhãn máy, đề bài, dòng "AI soạn · {k} bước", nút "Giao bài". | REQ-GEO-20; `Design/mvp-01` |
| BR-GEO-205 | Màn Kho đề không có ô tìm kiếm, không có nút "Thêm đề", không có giá trị lọc "Thiết diện" và chủ đề "Độ dài" (có trên khung nhưng không có yêu cầu). | D-GEO-112; REQ-GEO-52 (OUT) |
| BR-GEO-206 | Kho đề là màn của giáo viên; thẻ học sinh không gọi được danh sách Kho đề (`FORBIDDEN`). | BR-GEO-10; D-GEO-131 |

- **Ánh xạ dữ liệu:** `architecture/03_schema.sql#problem`, `#problem_version`, `#problem_topic`, `#verification_report` (chỉ nhãn).

### Tiêu chí chấp nhận

**US-GEO-501-SC1 — Mở Kho đề** · Nguồn: REQ-GEO-20; D-GEO-122, 142 · PROVISIONAL D-GEO-122
- Given: kho thử F-GEO-080.
- When: Giáo viên mẫu mở "Kho đề".
- Then: dòng đầu trang ghi "48 đề · 44 đã kiểm chứng · tài liệu của Giáo viên mẫu"; trang đầu có 20 thẻ đề (F-GEO-083) và nút "Tải thêm"; ba nhóm lọc đều ở "Tất cả".

**US-GEO-501-SC2 — Lọc theo hai nhóm cùng lúc** · Nguồn: REQ-GEO-20; D-GEO-070 · PROVISIONAL D-GEO-070
- Given: như SC1.
- When: chọn Cấp "THPT" và Dạng bài "Góc" (F-GEO-082).
- Then: danh sách có đúng 8 đề, trong đó có HKG-01; mọi thẻ mang "THPT" và "Góc".

**US-GEO-501-SC3 — Lọc theo hình** · Nguồn: REQ-GEO-20; D-GEO-070 · PROVISIONAL D-GEO-070
- Given: như SC1.
- When: chọn Hình "Hình chóp".
- Then: danh sách có đúng 8 đề, đều là hình chóp.

**US-GEO-501-SC4 — Dòng đếm không đổi theo bộ lọc** · Nguồn: D-GEO-122; M-GEO-07 · PROVISIONAL D-GEO-122
- Given: đang lọc Cấp "THPT" và Dạng bài "Góc" (8 đề).
- When: xem dòng đầu trang, rồi đưa cả hai nhóm về "Tất cả".
- Then: trong lúc lọc dòng đầu trang vẫn ghi "48 đề · 44 đã kiểm chứng"; sau khi bỏ lọc danh sách trở lại trang đầu 20 thẻ.

**US-GEO-501-SC5 — Nội dung một thẻ đề** · Nguồn: REQ-GEO-20; `Design/mvp-01`
- Given: như SC1.
- When: xem thẻ của HKG-01.
- Then: thẻ có hình chóp thu nhỏ, "THPT", "Góc", nhãn "Đã kiểm chứng", đề bài nguyên văn của F-GEO-031, dòng "AI soạn · 3 bước" và nút "Giao bài".

**US-GEO-501-SC6 — Dùng được trên điện thoại** · Nguồn: REQ-GEO-42; D-GEO-001
- Given: Giáo viên mẫu trên điện thoại (F-GEO-103).
- When: mở "Kho đề" và chọn Cấp "THCS".
- Then: các thẻ xếp một cột, không phải cuộn ngang; lọc cho đúng các đề THCS; nút "Giao bài" bấm được.

**US-GEO-501-FC1 — Lọc không có kết quả** · Nguồn: REQ-GEO-20
- Given: như SC1.
- When: chọn Cấp "THCS" và Dạng bài "Mặt cầu" (F-GEO-081).
- Then: không có thẻ nào; hiện MSG-GEO-40; không có nút "Tải thêm".

**US-GEO-501-FC2 — Học sinh gọi Kho đề** · Nguồn: BR-GEO-10; D-GEO-131 · PROVISIONAL D-GEO-131
- Given: phiên của An (học sinh).
- When: gọi thẳng danh sách Kho đề.
- Then: `FORBIDDEN` (MSG-GEO-03); không có đề nào trong dữ liệu trả về; thanh bên của học sinh không có mục "Kho đề".

**US-GEO-501-FC3 — Không có tìm kiếm và thêm đề** · Nguồn: D-GEO-112; REQ-GEO-52 (OUT, hệ quả người dùng thấy) · PROVISIONAL D-GEO-112
- Given: Giáo viên mẫu ở "Kho đề".
- When: xem toàn bộ màn.
- Then: không có ô tìm kiếm, không có nút "Thêm đề", không có nút soạn hay import; nhóm "Dạng bài" không có "Thiết diện".

**US-GEO-501-FC4 — Giá trị lọc không hợp lệ** · Nguồn: M-004 (1); D-GEO-070
- Given: phiên của Giáo viên mẫu.
- When: gọi thẳng danh sách với Dạng bài "Thiết diện".
- Then: `VALIDATION_FAILED` (MSG-GEO-01), `details[]` nêu trường lọc sai; không có đề nào được trả.

- **Mở và quyết định đang dùng:** D-GEO-070 (Q-GV), D-GEO-112, 122; D-GEO-142 (trang 20, trần 50), D-GEO-131 (học sinh gọi Kho đề nhận `FORBIDDEN`). Khung `mvp-01` gộp "Nón · Trụ · Cầu" thành một nút; AC theo D-GEO-070 (từng hình riêng).
- **DoR:** Ready.

## US-GEO-502 — Thẻ đề hiện nhãn máy và "AI soạn"

- **Story:** Là Giáo viên mẫu, tôi muốn mỗi đề ghi "AI soạn" và nhãn máy, để biết mức tin cậy. · Loại UI · Must · Cỡ S · Độ khó 2
- **Nguồn:** REQ-GEO-20, 29, 04 · D-GEO-020, 028, 061, 079 · SRC-019, SRC-056, SRC-057 · BR-GEO-09, 10, 14
- **Góc nhìn người vận hành (M18):** giáo viên liếc nhãn để biết đề nào đã được máy tính lại. Ca đáng kể: đề mới nạp chưa kịp tính nhãn.
- **Phụ thuộc:** US-GEO-203, 501.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-14 | Nhãn hai trục: máy "Đã kiểm chứng" / "Cần xem lại"; người "AI soạn" / "Giáo viên đã duyệt"; giáo viên không tự gắn nhãn máy. | SRC-019 |
| BR-GEO-207 | Mỗi thẻ đề luôn có đúng một nhãn máy và nhãn người "AI soạn". Đề chưa có kết quả kiểm chứng hiện "Cần xem lại". | D-GEO-061 ("còn lại là Cần xem lại"); D-GEO-028 |
| BR-GEO-10 | Kho đề MVP thuộc Giáo viên mẫu; lời giải AI đứng tên giáo viên này. | SRC-057 |
| BR-GEO-297 | Người đứng tên kho đề và lời giải của kho là **tác giả nền tảng**: một tài khoản hệ thống của tổ chức nền tảng (F-GEO-008), hiển thị là "Giáo viên mẫu". Tài khoản Giáo viên mẫu của từng trường (F-GEO-003, F-GEO-005) chỉ dùng để giao bài, không phải tác giả của kho. | D-GEO-160, 179; D-GEO-005 (Final) |

- **Ánh xạ dữ liệu:** `architecture/03_schema.sql#verification_report`, `#solution` (nhãn người, tác giả).

### Tiêu chí chấp nhận

**US-GEO-502-SC1 — Đề đã kiểm chứng** · Nguồn: REQ-GEO-20, 29; D-GEO-079 · PROVISIONAL D-GEO-079
- Given: kho thử F-GEO-080.
- When: xem thẻ HKG-01.
- Then: thẻ có nhãn "Đã kiểm chứng" và dòng "AI soạn · 3 bước".

**US-GEO-502-SC2 — Đề cần xem lại** · Nguồn: REQ-GEO-04, 20
- Given: kho thử F-GEO-080 có 4 đề "Cần xem lại".
- When: xem thẻ của một trong bốn đề đó.
- Then: thẻ có nhãn "Cần xem lại" (khác màu với "Đã kiểm chứng") và vẫn có "AI soạn"; nút "Giao bài" vẫn bấm được (giáo viên là người quyết định, BR-GEO-13).

**US-GEO-502-SC3 — Kho đề đứng tên Giáo viên mẫu** · Nguồn: REQ-GEO-29; BR-GEO-10; D-GEO-160 · PROVISIONAL D-GEO-160
- Given: Giáo viên C của "THCS Mẫu C" mở "Kho đề".
- When: xem dòng đầu trang và khối "Lời giải" của trang chi tiết HKG-01.
- Then: dòng đầu trang ghi "tài liệu của Giáo viên mẫu"; khối "Lời giải" ghi "AI soạn · tài liệu của Giáo viên mẫu".

**US-GEO-502-FC1 — Đề chưa tính nhãn** · Nguồn: D-GEO-061; BR-GEO-07
- Given: một đề vừa nạp, engine chưa chạy kiểm chứng.
- When: xem thẻ của đề đó.
- Then: nhãn là "Cần xem lại"; thẻ không để trống nhãn và không hiện "Đã kiểm chứng".

**US-GEO-502-FC2 — Không có "Giáo viên đã duyệt"** · Nguồn: D-GEO-028 · PROVISIONAL D-GEO-028
- Given: kho thử F-GEO-080.
- When: duyệt hết 48 thẻ và mở trang chi tiết của HKG-01.
- Then: không chỗ nào hiện nhãn "Giáo viên đã duyệt"; không có nút duyệt lời giải.

- **Mở và quyết định đang dùng:** D-GEO-020, 028, 079 (Q-CDT-12); D-GEO-160 (tác giả nền tảng; Giáo viên C và Giáo viên mẫu cùng thấy một dòng "tài liệu của Giáo viên mẫu" vì cùng một tác giả nền tảng).
- **DoR:** Ready.

## US-GEO-503 — Xem một đề: hình, đáp số, lời giải từng bước, gợi ý

- **Story:** Là Giáo viên mẫu, tôi muốn xem hình, đáp số, lời giải từng bước và gợi ý của một đề, để quyết định giao. · Loại UI · Must · Cỡ M · Độ khó 3
- **Nguồn:** REQ-GEO-21 · D-GEO-078, 112, 131, 153, 164 · SRC-093 · BR-GEO-01, 13 · `Design/mvp-02` (bên trái) · RV-GEO-04, 32
- **Góc nhìn người vận hành (M18):** giáo viên đọc lời giải trước khi giao để chắc là đúng ý mình. Ca đáng kể: học sinh dò được địa chỉ trang chi tiết đề (trang này có lời giải đầy đủ); giáo viên tìm cách đọc lời giải của đề tự nhập của học sinh hoặc của trường khác.
- **Phụ thuộc:** US-GEO-501, 203; cùng màn với US-GEO-602…604, 607.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-208 | Trang chi tiết đề cho giáo viên có bốn phần: hình 3D xoay được; đáp số; lời giải từng bước; Gợi ý 1 và Gợi ý 2. Giáo viên thấy lời giải đầy đủ của đề kho bất kể giờ mở của bài giao nào. | REQ-GEO-21 |
| BR-GEO-298 | Đường đọc lời giải dành cho giáo viên chỉ trả lời giải của đề kho nền tảng; đề tự nhập (bài tự học của học sinh) và nội dung của trường khác nhận `NOT_FOUND`, không kèm dữ liệu. | D-GEO-153; BR-GEO-112 |
| BR-GEO-209 | Khi đề mang "Đã kiểm chứng", khối "Lời giải" có chip "Đáp số khớp tính toán: {đáp số}". | `Design/mvp-02`; BR-GEO-07 |
| BR-GEO-210 | Trang không hiện nút "Trình chiếu" và "Sửa (sắp có)" (có trên khung nhưng không có yêu cầu trong MVP); không có gì được xây phía sau. | D-GEO-164, 112 |
| BR-GEO-211 | Trang chi tiết đề chỉ dành cho giáo viên: thẻ học sinh gọi vào nhận `FORBIDDEN` và không nhận lời giải, đáp số. | BR-GEO-01; D-GEO-131 |

- **Ánh xạ dữ liệu:** `architecture/03_schema.sql#problem_version`, `#scene_build`, `#hint`, `#solution` (giáo viên đọc qua hàm cổng dành cho giáo viên).

### Tiêu chí chấp nhận

**US-GEO-503-SC1 — Bốn phần của trang chi tiết** · Nguồn: REQ-GEO-21
- Given: Giáo viên mẫu ở "Kho đề".
- When: bấm vào đề HKG-01.
- Then: trang "Chi tiết đề & giao bài" hiện đề bài F-GEO-031; hình chóp S.ABCD; đáp số "45°"; ba bước lời giải F-GEO-037 đúng thứ tự; Gợi ý 1 và Gợi ý 2 nguyên văn.

**US-GEO-503-SC2 — Giáo viên thấy lời giải dù bài đang khóa với học sinh** · Nguồn: REQ-GEO-21; BR-GEO-13
- Given: BG-1 (HKG-01) đang Sắp mở với lớp 11A1.
- When: Giáo viên mẫu mở trang chi tiết HKG-01.
- Then: lời giải ba bước và đáp số "45°" hiện đầy đủ.

**US-GEO-503-SC3 — Chip đáp số khớp tính toán** · Nguồn: `Design/mvp-02`; BR-GEO-07
- Given: HKG-01 mang "Đã kiểm chứng"; một đề khác trong bốn đề "Cần xem lại".
- When: mở trang chi tiết của từng đề.
- Then: HKG-01 có chip "Đáp số khớp tính toán: 45°"; đề "Cần xem lại" không có chip này.

**US-GEO-503-SC4 — Hình xoay được trên trang chi tiết** · Nguồn: REQ-GEO-21, 05
- Given: trang chi tiết HKG-01.
- When: kéo chuột trên hình.
- Then: hình xoay như ở màn Làm bài (US-GEO-301).

**US-GEO-503-SC5 — Xem trên điện thoại** · Nguồn: REQ-GEO-42; D-GEO-001
- Given: Giáo viên mẫu trên điện thoại (F-GEO-103).
- When: mở trang chi tiết HKG-01.
- Then: các phần xếp dọc theo thứ tự đề, hình, lời giải, gợi ý, giao bài; không phải cuộn ngang.

**US-GEO-503-FC1 — Đề không có lời giải** · Nguồn: REQ-GEO-21
- Given: một đề trong kho không có bản ghi lời giải.
- When: mở trang chi tiết của đề đó.
- Then: khối "Lời giải" hiện MSG-GEO-54; đề và hình vẫn hiện.

**US-GEO-503-FC2 — Không có nút "Sửa (sắp có)" và "Trình chiếu"** · Nguồn: D-GEO-164, 112; REQ-GEO-54 (OUT, hệ quả người dùng thấy) · PROVISIONAL D-GEO-164
- Given: trang chi tiết HKG-01 trên máy tính và trên điện thoại.
- When: xem toàn bộ trang.
- Then: không có nút "Sửa (sắp có)", không có nút "Trình chiếu" và không có nhãn "Concept · Chặng 2" ở bất kỳ đâu trên trang.

**US-GEO-503-FC3 — Học sinh gọi trang chi tiết đề** · Nguồn: BR-GEO-01; REQ-GEO-40; D-GEO-131 · PROVISIONAL D-GEO-131
- Given: phiên của An; BG-1 đang Sắp mở.
- When: gọi thẳng trang chi tiết HKG-01 và yêu cầu lời giải dành cho giáo viên.
- Then: `FORBIDDEN`; dữ liệu trả về không có bước giải, không có "45°".

**US-GEO-503-FC4 — Mã đề không tồn tại** · Nguồn: M-004 (1)
- Given: phiên của Giáo viên mẫu.
- When: mở trang chi tiết với một mã đề không có trong kho.
- Then: `NOT_FOUND`; màn hiện MSG-GEO-04.

**US-GEO-503-FC5 — Giáo viên gọi lời giải của đề tự nhập của học sinh trường mình** · Nguồn: D-GEO-153, 175; BR-GEO-01, 12 · PROVISIONAL D-GEO-175
- Given: An (11A1) có bài tự học từ đề F-GEO-060 và đã mở được lời giải của bài đó; phiên của Giáo viên mẫu cùng trường.
- When: từ phiên của Giáo viên mẫu, gửi thẳng yêu cầu lời giải dành cho giáo viên với mã đề tự nhập đó.
- Then: `NOT_FOUND` (MSG-GEO-04); dữ liệu trả về không có bước giải, không có "60/√769", không có "2,1637".

**US-GEO-503-FC6 — Giáo viên trường khác gọi lời giải của đề tự nhập** · Nguồn: D-GEO-153, 012 (Final); NFR-GEO-022, 030; RV-GEO-04 · PROVISIONAL D-GEO-153
- Given: An của "THPT Mẫu A" có bài tự học từ đề F-GEO-060; Giáo viên C của "THCS Mẫu C" đã vào bằng `RW3NJ5PZC6TD`.
- When: từ phiên của Giáo viên C, gửi thẳng yêu cầu lời giải dành cho giáo viên với mã đề tự nhập đó, kèm định danh trường "THPT Mẫu A" trong thân yêu cầu.
- Then: `NOT_FOUND`; phản hồi giống hệt phản hồi cho một mã đề không tồn tại; không có bước giải hay đáp số.

- **Mở và quyết định đang dùng:** D-GEO-078 (Q-CDT); D-GEO-131 (học sinh nhận `FORBIDDEN`); D-GEO-153 (giáo viên chỉ đọc lời giải đề kho nền tảng); D-GEO-164 (không hiện "Trình chiếu", "Sửa (sắp có)").
- **DoR:** Ready.

## US-GEO-504 — Dữ liệu: nạp 40 đến 60 đề của Giáo viên mẫu và tính lại nhãn

- **Story:** Là nhóm sản phẩm, tôi muốn kho có 40 đến 60 đề, ít nhất 40 đã kiểm chứng, để đủ tiêu chí hoàn thành. · Loại Data · Must · Cỡ L · Độ khó 4
- **Nguồn:** REQ-GEO-20, 49, 30 · D-GEO-004 (Final), 055, 062, 076, 105 · SRC-092, SRC-067, SRC-058 · BR-GEO-10, 11 · M-GEO-07 · NFR-GEO-025
- **Góc nhìn người vận hành (M18):** người nạp dữ liệu chạy khi dựng môi trường và mỗi khi kho hoặc engine đổi. Ca đáng kể: đề thiếu đáp số; nhãn engine tính khác nhãn ghi trong tệp; nạp lại làm đổi lời giải của bài đã giao.
- **Phụ thuộc:** US-GEO-201, 203, 106.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-212 | Mỗi đề của kho có: mã `HKG-nn`, cấp, dạng bài, loại hình, đề bài, hình, Gợi ý 1, Gợi ý 2, lời giải từng bước (mỗi bước kèm "vì sao"), đáp số; tác giả là Giáo viên mẫu, cờ "AI soạn", model, phiên bản prompt. | REQ-GEO-20, 30; BR-GEO-10 |
| BR-GEO-213 | Nhãn máy của mọi đề do engine tính lại khi nạp và mỗi lần engine đổi; cột nhãn trong tệp nguồn không được dùng. | D-GEO-004, 105; M-GEO-07 |
| BR-GEO-214 | MVP cần kho 40 đến 60 đề và ít nhất 40 đề "Đã kiểm chứng". Nguồn hiện chỉ có sáu đề; phần còn lại và người soát nội dung Toán chưa xác định. | REQ-GEO-49; D-GEO-055 |
| BR-GEO-11 | Lời giải đã giao không sửa tại chỗ; sửa là tạo phiên bản mới, bài đã giao giữ phiên bản lúc giao. | SRC-058; D-GEO-062 |

- **Ánh xạ dữ liệu:** `architecture/03_schema.sql#problem`, `#problem_version`, `#hint`, `#solution`, `#scene_build`, `#verification_report`.

### Tiêu chí chấp nhận

**US-GEO-504-SC1 — Nạp sáu đề nguồn đủ trường** · Nguồn: REQ-GEO-20, 30
- Given: tệp `materials/Ngan-hang-bai-tap-Hinh-khong-gian.xlsx`.
- When: nạp kho đề.
- Then: có HKG-01…HKG-06, mỗi đề đủ đề bài, hai gợi ý, các bước kèm "vì sao", đáp số đúng như F-GEO-031…039; tác giả "Giáo viên mẫu"; cờ "AI soạn"; có model và phiên bản prompt.

**US-GEO-504-SC2 — Nhãn do engine tính lại** · Nguồn: D-GEO-004 (Final), 105 · PROVISIONAL D-GEO-105
- Given: sáu đề vừa nạp.
- When: engine chạy kiểm chứng cả kho.
- Then: cả sáu đề mang "Đã kiểm chứng", kể cả HKG-06 mà tệp nguồn ghi "Cần xem lại".

**US-GEO-504-SC3 — Kết quả nạp nêu chỗ nhãn khác tệp** · Nguồn: D-GEO-004; `03_gate3_preliminary_stories.md` US-GEO-504 · PROVISIONAL D-GEO-105
- Given: như SC2.
- When: đọc kết quả nạp.
- Then: có dòng "HKG-06: tệp ghi Cần xem lại, engine tính Đã kiểm chứng"; nhãn dùng trên mọi màn là nhãn engine tính.

**US-GEO-504-SC4 — Đếm số đề đã kiểm chứng so với tiêu chí MVP** · Nguồn: REQ-GEO-49; M-GEO-07; D-GEO-055 · PROVISIONAL D-GEO-055
- Given: kho chỉ có sáu đề nguồn (6 "Đã kiểm chứng"); và kho thử F-GEO-080 (48 đề, 44 "Đã kiểm chứng").
- When: tính M-GEO-07 cho từng kho.
- Then: kho sáu đề: 6, "chưa đạt (cần ≥ 40 đề đã kiểm chứng, kho 40 đến 60 đề)"; kho thử: 44, "đạt".

**US-GEO-504-SC5 — Sửa lời giải tạo phiên bản mới, bài đã giao giữ phiên bản cũ** · Nguồn: BR-GEO-11; D-GEO-062; NFR-GEO-025
- Given: BG-1 đã giao với lời giải HKG-01 phiên bản 1.
- When: nạp lại kho với bước 3 của HKG-01 được sửa câu chữ.
- Then: HKG-01 có lời giải phiên bản 2; BG-1 vẫn hiện lời giải phiên bản 1 cho lớp 11A1; bài giao tạo sau đó dùng phiên bản 2.

**US-GEO-504-SC6 — Tính lại nhãn khi engine đổi** · Nguồn: M-GEO-07; REQ-GEO-47
- Given: kho đã có nhãn; engine được cập nhật phiên bản.
- When: chạy lại kiểm chứng cả kho.
- Then: mỗi đề có báo cáo kiểm chứng mới ghi phiên bản engine; nhãn hiển thị là nhãn của lần chạy mới nhất.

**US-GEO-504-FC1 — Đề thiếu đáp số** · Nguồn: REQ-GEO-20; `03_gate3_preliminary_stories.md` US-GEO-504
- Given: tệp nạp có HKG-05 để trống ô Đáp số.
- When: nạp.
- Then: HKG-05 không vào kho; kết quả nạp ghi "HKG-05: thiếu đáp số"; năm đề còn lại vào kho.

**US-GEO-504-FC2 — Sửa tại chỗ lời giải đã giao bị từ chối** · Nguồn: BR-GEO-11; NFR-GEO-025
- Given: lời giải HKG-01 phiên bản 1 đang được BG-1 dùng.
- When: một thao tác cố ghi đè đáp số của phiên bản 1 thành "60°".
- Then: thao tác bị tầng dữ liệu từ chối; phiên bản 1 vẫn ghi "45°".

- **Mở và quyết định đang dùng:** D-GEO-055 (nguồn của 34 đến 54 đề còn lại và người soát: chưa xác định, KG-05; SC4 cho thấy kho sáu đề chưa đạt tiêu chí MVP); D-GEO-141 (giữ tiêu chí ít nhất 40 đề đã kiểm chứng; soạn thêm từ 34 đề trở lên là việc nội dung cần người soát, không giao cho agent viết mã; KG-21); D-GEO-105 (nhãn kỳ vọng HKG-06); D-GEO-062.
- **DoR:** Ready về hành vi nạp; nội dung kho PROVISIONAL theo D-GEO-055.

## US-GEO-505 — Kho đề tải theo trang theo khóa

- **Story:** Là Giáo viên mẫu, tôi muốn danh sách đề tải thêm mượt khi cuộn, để dùng nhanh dù kho lớn. · Loại Tech · Must · Cỡ M · Độ khó 3
- **Nguồn:** REQ-GEO-39 · D-GEO-014 (Final), 124, 142, 168 · SRC-005 · NFR-GEO-009, 040, 041 · M-004 (2)
- **Góc nhìn người vận hành (M18):** giáo viên cuộn xuống và bấm "Tải thêm". Ca đáng kể: kho được nạp thêm đề trong lúc đang cuộn; đổi bộ lọc khi đã tải vài trang.
- **Phụ thuộc:** US-GEO-501.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-215 | Danh sách Kho đề trả theo trang bằng con trỏ: mỗi phản hồi có các đề của trang, cờ còn trang sau và con trỏ trang kế; không có tổng số đề, không có số trang. | D-GEO-014; M-004 (2) |
| BR-GEO-216 | Danh sách Kho đề có kích thước trang mặc định 20 và trần 50; yêu cầu kích thước lớn hơn trần bị kẹp về trần, không báo lỗi. Trần chung của mọi danh sách là 100. | NFR-GEO-040; D-GEO-142, 168 |
| BR-GEO-217 | Thứ tự danh sách ổn định: tải lần lượt hết các trang cho mỗi đề đúng một lần, kể cả khi có đề mới được thêm giữa chừng. | REQ-GEO-39; D-GEO-014 |
| BR-GEO-218 | Truy vấn danh sách không dùng OFFSET và số câu lệnh cơ sở dữ liệu không tăng theo số dòng của trang. | D-GEO-014 (Final); NFR-GEO-009, 040 |

- **Ánh xạ dữ liệu:** `architecture/03_schema.sql#problem` (chỉ mục kho đề); `architecture/04_query_catalogue.md` (truy vấn Kho đề); ADR-GEO-12; con trỏ mờ gắn trường, loại danh sách, phiên bản (D-GEO-168).

### Tiêu chí chấp nhận

**US-GEO-505-SC1 — Tải lần lượt hết kho** · Nguồn: REQ-GEO-39; D-GEO-014, 142 · PROVISIONAL D-GEO-142
- Given: kho thử F-GEO-080 (48 đề), không lọc.
- When: mở "Kho đề" rồi bấm "Tải thêm" cho tới khi nút biến mất.
- Then: ba lần tải cho 20, 20 và 8 đề; tổng cộng 48 đề, mỗi mã HKG-01…HKG-48 xuất hiện đúng một lần; sau trang thứ ba không còn nút "Tải thêm".

**US-GEO-505-SC2 — Dữ liệu đổi giữa chừng** · Nguồn: REQ-GEO-39; `03_gate3_preliminary_stories.md` US-GEO-505
- Given: Giáo viên mẫu đã tải trang đầu (20 đề); ngay sau đó một đề mới được nạp vào kho.
- When: bấm "Tải thêm" tới hết.
- Then: không đề nào của trang đầu lặp lại; cả 28 đề còn lại của 48 đề ban đầu đều xuất hiện đúng một lần.

**US-GEO-505-SC3 — Phân trang cùng bộ lọc** · Nguồn: REQ-GEO-39, 20; D-GEO-142 · PROVISIONAL D-GEO-142
- Given: kho thử; lọc Cấp "THPT" (24 đề).
- When: tải hết.
- Then: hai lần tải cho 20 và 4 đề, đều THPT, không trùng.

**US-GEO-505-SC4 — Phản hồi không có tổng số** · Nguồn: M-004 (2); NFR-GEO-041
- Given: phiên của Giáo viên mẫu.
- When: ghi lại dữ liệu trả về của một trang danh sách Kho đề.
- Then: có danh sách đề, cờ còn trang sau và con trỏ trang kế; không có trường tổng số đề hay tổng số trang.

**US-GEO-505-SC5 — Không OFFSET, không truy vấn lặp theo dòng** · Nguồn: D-GEO-014 (Final); NFR-GEO-009, 040
- Given: kho thử; công cụ đếm câu lệnh cơ sở dữ liệu của một yêu cầu.
- When: gọi danh sách một lần với trang 1 đề và một lần với trang 20 đề.
- Then: hai lần dùng cùng số câu lệnh; không câu lệnh nào chứa OFFSET.

**US-GEO-505-FC1 — Hết danh sách** · Nguồn: REQ-GEO-39
- Given: đã tải tới trang cuối (8 đề) của kho thử.
- When: xem phản hồi của trang cuối.
- Then: cờ còn trang sau là "không"; màn không hiện nút "Tải thêm" và không hiện thông báo lỗi.

**US-GEO-505-FC2 — Đổi bộ lọc giữa chừng** · Nguồn: `03_gate3_preliminary_stories.md` US-GEO-505
- Given: đã tải hai trang không lọc (40 đề).
- When: chọn Cấp "THCS".
- Then: danh sách được làm lại từ đầu theo bộ lọc mới: trang đầu 20 đề THCS; không còn đề THPT nào của lần tải trước trên màn.

**US-GEO-505-FC3 — Con trỏ hỏng** · Nguồn: M-004 (1)
- Given: phiên của Giáo viên mẫu.
- When: gọi danh sách với con trỏ `khong-phai-con-tro` (F-GEO-084).
- Then: `INVALID_CURSOR`; màn hiện MSG-GEO-05; không có đề nào được trả.

**US-GEO-505-FC4 — Xin trang quá lớn** · Nguồn: NFR-GEO-040; M-004 (2); D-GEO-142 · PROVISIONAL D-GEO-142
- Given: kho thử F-GEO-080 (48 đề), không lọc.
- When: gọi thẳng danh sách với kích thước trang 10000.
- Then: phản hồi thành công với đúng 48 đề (kích thước bị kẹp về trần 50 của Kho đề, F-GEO-083); cờ còn trang sau là "không" và không có con trỏ trang kế.

- **Mở và quyết định đang dùng:** D-GEO-142 (Kho đề: trang 20, trần 50; FC4 đổi từ "20 đề kèm con trỏ" thành "48 đề, không con trỏ"); D-GEO-168 (trần chung 100; con trỏ mờ); D-GEO-124. Ngưỡng số câu lệnh (≤ 4 đọc) và ngưỡng thời gian truy vấn của NFR-GEO-007, 009 là yêu cầu kỹ thuật với ngưỡng đề xuất (D-GEO-125), không đưa số vào AC.
- **DoR:** Ready.
