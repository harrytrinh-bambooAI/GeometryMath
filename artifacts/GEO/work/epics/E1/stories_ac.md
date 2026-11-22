# E1 — Nền tảng, cổng AI, bộ đề chuẩn: story Dev-Ready và tiêu chí chấp nhận

> Tệp sinh bởi `work/src/build_stories.py` từ `work/src/E1_body.md`, `work/src/story_header.md` và `work/05_dictionary.md`; không sửa tay, sửa nguồn rồi chạy lại. Chủ sở hữu: BA.

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
| MSG-GEO-02 | Page state | "Phiên đã hết. Vui lòng vào lại." | UNAUTHENTICATED |
| MSG-GEO-03 | Toast | "Bạn không có quyền thực hiện thao tác này." | FORBIDDEN |
| MSG-GEO-04 | Page state | "Không tìm thấy nội dung này." | NOT_FOUND |
| MSG-GEO-05 | Inline | "Danh sách đã thay đổi. Vui lòng tải lại từ đầu." | INVALID_CURSOR |
| MSG-GEO-09 | Inline | "Em hãy nhập tên hiển thị." | VALIDATION_FAILED |
| MSG-GEO-10 | Inline | "Tên hiển thị tối đa 60 ký tự." | VALIDATION_FAILED |
| MSG-GEO-11 | Inline | "Mã lớp không đúng. Em hỏi lại giáo viên nhé." | VALIDATION_FAILED |
| MSG-GEO-12 | Inline | "Mã giáo viên không đúng." | VALIDATION_FAILED |
| MSG-GEO-14 | Inline + nút "Thử lại" | "Trợ lý đang bận. Em thử lại sau ít phút nhé." | TIMEOUT hoặc lỗi máy chủ |
| MSG-GEO-57 | Toast | "Hệ thống đang đông. Vui lòng thử lại sau vài giây." | SERVICE_OVERLOADED hoặc RATE_LIMITED (D-GEO-143; NFR-GEO-055, 056) |
| MSG-GEO-59 | Inline | "Đã nhập sai mã quá nhiều lần. Vui lòng thử lại sau ít phút." | RATE_LIMITED |

---

## US-GEO-101 — Học sinh vào lớp bằng tên hiển thị và mã lớp

- **Story:** Là học sinh, tôi muốn vào lớp bằng tên hiển thị và mã lớp, để thấy bài của lớp mình mà không phải đăng ký. · Loại UI · Must · Cỡ M · Độ khó 3
- **Nguồn:** REQ-GEO-23, 44, 28 · D-GEO-048, 051, 067 · SRC-095, SRC-010 · NFR-GEO-059, 070
- **Góc nhìn người vận hành (M18):** học sinh mở trang trên iPad hoặc iPhone ở đầu buổi học, gõ tên và mã lớp giáo viên đọc. Ca đáng kể: gõ sai mã lớp; quên gõ tên; vào lại từ máy khác và muốn thấy đúng tiến độ của mình. Giả mạo tên bạn cùng lớp là rủi ro đã biết của MVP, chủ đầu tư chấp nhận tạm vì chưa thu định danh trẻ em (D-GEO-149; ghi ở `work/known_gaps.md` KG-20); không viết AC chặn.
- **Phụ thuộc:** US-GEO-104, US-GEO-601 (dữ liệu lớp mẫu).

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-12 | Học sinh chỉ thấy bài của lớp mình và dữ liệu của mình. | SRC-059 |
| BR-GEO-100 | Màn "Vào lớp" chỉ thu hai ô: "Tên hiển thị" và "Mã lớp". Không thu email, số điện thoại, ngày sinh. | REQ-GEO-44; D-GEO-048, 067 |
| BR-GEO-101 | Mã lớp duy nhất toàn hệ thống; từ mã lớp máy chủ suy ra lớp và trường. Thiết bị không gửi mã trường. | D-GEO-051; M-004 (4) |
| BR-GEO-102 | Tên hiển thị từ 1 đến 60 ký tự sau khi bỏ khoảng trắng đầu và cuối. | `architecture/03_schema.sql#app_user` (đề xuất ARCH; QA-GEO-01) |
| BR-GEO-103 | Trong một lớp, hai lần vào với cùng tên (không phân biệt hoa thường) là cùng một học sinh; tên chưa có trong lớp thì tạo học sinh mới của lớp đó. | `architecture/03_schema.sql#class_member` (đề xuất ARCH; D-GEO-106) |

- **Ánh xạ dữ liệu:** `architecture/03_schema.sql#app_user`, `#class`, `#class_member` (không chép lược đồ). Chuỗi màn hình: `work/05_dictionary.md` §2 "Vào lớp".

### Tiêu chí chấp nhận

**US-GEO-101-SC1 — Vào lớp bằng tên và mã lớp đúng** · Nguồn: REQ-GEO-23; D-GEO-048 · PROVISIONAL D-GEO-048
- Given: lớp 11A1 có mã lớp `HK7M2A` và học sinh An (F-GEO-010, F-GEO-020); An đang ở màn "Vào lớp".
- When: An gõ Tên hiển thị "An", Mã lớp `HK7M2A` rồi bấm "Vào lớp".
- Then: màn "Bài của em" mở với dòng "Chào An" và đúng ba bài giao của 11A1: BG-1, BG-2, BG-3 (F-GEO-040…042).

**US-GEO-101-SC2 — Tên chưa có trong lớp thì thành học sinh mới của lớp đó** · Nguồn: REQ-GEO-23; D-GEO-106 · PROVISIONAL D-GEO-106
- Given: lớp 11A1 chưa có học sinh tên "Yến" (F-GEO-028).
- When: gõ "Yến" và `HK7M2A`, bấm "Vào lớp".
- Then: "Bài của em" hiện "Chào Yến", ba bài giao của 11A1, mỗi bài tiến độ 0/3 "Chưa bắt đầu"; Theo dõi lớp 11A1 của Giáo viên mẫu từ lúc đó có sáu học sinh.

**US-GEO-101-SC3 — Vào lại với cùng tên thì giữ đúng dữ liệu của mình** · Nguồn: REQ-GEO-23, 28; D-GEO-106 · PROVISIONAL D-GEO-106
- Given: An đã có tiến độ 2/3 trên BG-1 (F-GEO-050).
- When: trên một thiết bị khác, gõ "an" (chữ thường) và `HK7M2A`, bấm "Vào lớp".
- Then: vào đúng tài khoản An: BG-1 hiện tiến độ 2/3 "Đã xem gợi ý 2"; không tạo học sinh thứ hai tên "an".

**US-GEO-101-SC4 — Mã lớp quyết định trường và lớp** · Nguồn: REQ-GEO-28; D-GEO-051 · PROVISIONAL D-GEO-051
- Given: trường "THCS Mẫu C" có lớp 9C mã `MC9C6R` với một học sinh cũng tên An (F-GEO-012, F-GEO-026).
- When: gõ "An" và `MC9C6R`, bấm "Vào lớp".
- Then: "Bài của em" chỉ có BG-C (F-GEO-045); không có BG-1, BG-2, BG-3; tiến độ của An lớp 11A1 không hiện ở đây.

**US-GEO-101-SC5 — Màn "Vào lớp" chỉ có hai ô nhập** · Nguồn: REQ-GEO-44; D-GEO-048, 067; NFR-GEO-070 · PROVISIONAL D-GEO-067
- Given: người dùng chưa vào lớp.
- When: mở màn "Vào lớp".
- Then: chỉ có ô "Tên hiển thị", ô "Mã lớp", nút "Vào lớp" và liên kết "Tôi là giáo viên"; không có ô email, số điện thoại, ngày sinh; yêu cầu gửi lên máy chủ chỉ chứa tên hiển thị và mã lớp.

**US-GEO-101-SC6 — Tên dài đúng 60 ký tự được nhận** · Nguồn: QA-GEO-01 · PROVISIONAL QA-GEO-01
- Given: tên gồm 60 ký tự "A" (F-GEO-027).
- When: gõ tên đó và `HK7M2A`, bấm "Vào lớp".
- Then: vào được "Bài của em".

**US-GEO-101-SC7 — Dùng được trên iPhone màn dọc** · Nguồn: REQ-GEO-42; D-GEO-001
- Given: iPhone, trình duyệt web, màn dọc (F-GEO-101).
- When: mở màn "Vào lớp" và bật bàn phím để gõ.
- Then: hai ô và nút "Vào lớp" nằm trọn chiều ngang màn hình, không phải cuộn ngang; nút "Vào lớp" vẫn bấm được khi bàn phím đang mở.

**US-GEO-101-FC1 — Tên hiển thị rỗng** · Nguồn: REQ-GEO-23, 43; QA-GEO-01 · PROVISIONAL QA-GEO-01
- Given: ô Tên hiển thị rỗng hoặc chỉ có khoảng trắng (F-GEO-027); Mã lớp `HK7M2A`.
- When: bấm "Vào lớp".
- Then: dưới ô Tên hiển thị hiện MSG-GEO-09; vẫn ở màn "Vào lớp"; không có học sinh nào được tạo.

**US-GEO-101-FC2 — Tên hiển thị 61 ký tự** · Nguồn: QA-GEO-01 · PROVISIONAL QA-GEO-01
- Given: tên gồm 61 ký tự "A" (F-GEO-027).
- When: bấm "Vào lớp" với mã `HK7M2A`.
- Then: máy chủ trả `VALIDATION_FAILED`; hiện MSG-GEO-10 dưới ô tên; không tạo học sinh.

**US-GEO-101-FC3 — Mã lớp không tồn tại** · Nguồn: REQ-GEO-23, 43; D-GEO-051 · PROVISIONAL D-GEO-051
- Given: mã `ZZ9ZZ9` không thuộc lớp nào (F-GEO-013).
- When: gõ "An" và `ZZ9ZZ9`, bấm "Vào lớp".
- Then: hiện MSG-GEO-11 dưới ô Mã lớp; ô tên giữ nguyên chữ "An"; dữ liệu trả về không chứa tên lớp hay tên trường nào.

**US-GEO-101-FC4 — Phiên hết hạn** · Nguồn: NFR-GEO-059; REQ-GEO-43 · PROVISIONAL QA-GEO-01
- Given: An đang ở "Bài của em" và thẻ xác thực đã hết hạn (thời hạn thẻ là giả định của ARCH, NFR-GEO-059).
- When: An bấm vào một bài.
- Then: máy chủ trả `UNAUTHENTICATED`; màn hiện MSG-GEO-02 và đưa về "Vào lớp"; sau khi vào lại bằng "An" và `HK7M2A`, tiến độ cũ còn nguyên.

**US-GEO-101-FC5 — Gõ sai mã lớp nhiều lần không bị khóa riêng** · Nguồn: D-GEO-107, 152 · PROVISIONAL D-GEO-107
- Given: An ở màn "Vào lớp" trên iPad; chưa có lần thử nào trong 15 phút qua.
- When: An gõ "An" với mã `ZZ9ZZ9` (F-GEO-013) 11 lần liên tiếp trong vòng 2 phút, rồi gõ "An" với `HK7M2A`.
- Then: 11 lần sai đều hiện MSG-GEO-11, không lần nào nhận `RATE_LIMITED`; lần gõ đúng vào được "Bài của em" (giới hạn 10 lần sai trong 15 phút chỉ áp cho đăng nhập giáo viên).

- **Mở và quyết định đang dùng:** D-GEO-048, 051, 067 (giả định PO); QA-GEO-01 (độ dài, định dạng mã lớp theo lược đồ đề xuất); D-GEO-106 (trùng tên trong lớp); D-GEO-107 (đăng nhập nhẹ chỉ có giới hạn tốc độ chung); D-GEO-149 (giả mạo tên là rủi ro đã biết, không AC). Không viết hành vi xóa hay xuất dữ liệu (D-GEO-067, KG-08).
- **DoR:** Ready, có AC PROVISIONAL theo D-GEO-048, 051 và QA-GEO-01, 06; không điểm mở nào chặn luồng chính.

## US-GEO-102 — Giáo viên mẫu vào hệ thống

- **Story:** Là Giáo viên mẫu, tôi muốn vào bằng mã riêng của tài khoản mình, để chỉ tôi (không phải học sinh) đặt được giờ mở lời giải. · Loại UI · Must · Cỡ S · Độ khó 2
- **Nguồn:** REQ-GEO-24, 28 · D-GEO-073, 034, 107, 152 · SRC-096 · BR-GEO-03 · RV-GEO-02
- **Góc nhìn người vận hành (M18):** giáo viên vào từ máy tính trước giờ dạy, đôi khi từ điện thoại để xem nhanh. Ca đáng kể: gõ sai mã; học sinh tò mò bấm "Tôi là giáo viên"; ai đó thử liên tục để dò mã; mã cũ đã bị thu hồi hoặc hết hạn. Mã giáo viên là chìa xem mọi lời giải của kho đề nên có giới hạn thử sai riêng (D-GEO-152 đảo D-GEO-107 cho riêng đăng nhập giáo viên).
- **Phụ thuộc:** US-GEO-104, US-GEO-601.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-104 | Vai giáo viên chỉ có được bằng mã giáo viên mẫu; không có công tắc đổi vai không cần mã (công tắc "Giáo viên / Học sinh" trên khung thiết kế là của bản mẫu). | D-GEO-073; D-GEO-078 |
| BR-GEO-105 | MVP chỉ có Giáo viên mẫu; không có đăng ký giáo viên thật. | D-GEO-034; REQ-GEO-54 (OUT) |
| BR-GEO-03 | Chỉ giáo viên của lớp được đặt, đổi, mở ngay giờ mở. | SRC-050 |
| BR-GEO-284 | Mỗi tài khoản giáo viên (tài khoản Giáo viên mẫu của từng trường) có mã riêng, sinh ngẫu nhiên, dài ít nhất 12 ký tự base32 (bảng chữ A–Z và 2–7, khoảng 60 bit). Máy chủ chỉ lưu băm của mã, không lưu mã rõ. Mã thu hồi được và có hạn dùng. Một mã chỉ vào đúng tài khoản được cấp. | D-GEO-152 |
| BR-GEO-285 | Mã sai, mã đã thu hồi và mã quá hạn đều nhận cùng câu MSG-GEO-12, không nói lý do; không có phiên giáo viên nào được tạo. | D-GEO-152, 173 |
| BR-GEO-286 | Sau 10 lần nhập mã sai trong 15 phút từ cùng một địa chỉ IP, mọi lần thử kế tiếp trong cửa sổ đó nhận `RATE_LIMITED` (HTTP 429, kèm tiêu đề `Retry-After`) và MSG-GEO-59, kể cả khi mã gõ đúng. Chỉ đếm theo IP vì màn chỉ có ô mã (D-GEO-173). Đăng nhập nhẹ của học sinh không áp quy tắc này. | D-GEO-152, 173, 143; D-GEO-107 (học sinh) |

- **Ánh xạ dữ liệu:** `architecture/03_schema.sql#app_user` (loại tài khoản mẫu), `#class`. Chỗ lưu băm mã, hạn dùng, trạng thái thu hồi và bộ đếm thử sai do ARCH thiết kế theo D-GEO-152 (RV-GEO-02); BA không chép lược đồ.

### Tiêu chí chấp nhận

**US-GEO-102-SC1 — Mã giáo viên đúng** · Nguồn: REQ-GEO-24; D-GEO-073, 152 · PROVISIONAL D-GEO-152
- Given: màn "Giáo viên mẫu" (mở từ liên kết "Tôi là giáo viên"), máy tính (F-GEO-102).
- When: gõ Mã giáo viên `GQ7KX2MTB4HN` (F-GEO-003), bấm "Vào".
- Then: màn "Kho đề" mở; mục lớp của giáo viên có đúng hai lớp 11A1 và 9B; góc màn hiện tên "Giáo viên mẫu".

**US-GEO-102-SC2 — Vào được từ điện thoại** · Nguồn: REQ-GEO-42; D-GEO-001, 073, 152 · PROVISIONAL D-GEO-152
- Given: điện thoại, trình duyệt web (F-GEO-103).
- When: gõ `GQ7KX2MTB4HN`, bấm "Vào".
- Then: "Kho đề" mở, không phải cuộn ngang để thấy danh sách đề và nút "Giao bài".

**US-GEO-102-SC3 — Mỗi mã vào đúng tài khoản được cấp** · Nguồn: D-GEO-152, 054 · PROVISIONAL D-GEO-152
- Given: màn "Giáo viên mẫu"; mã `RW3NJ5PZC6TD` được cấp cho Giáo viên C của "THCS Mẫu C" (F-GEO-005).
- When: gõ `RW3NJ5PZC6TD`, bấm "Vào".
- Then: vào tài khoản Giáo viên C: mục lớp chỉ có 9C; không có 11A1, 9B hay dữ liệu nào của "THPT Mẫu A".

**US-GEO-102-SC4 — Máy chủ không lưu mã rõ** · Nguồn: D-GEO-152; REQ-GEO-40 · PROVISIONAL D-GEO-152
- Given: dữ liệu mẫu đã nạp với mã `GQ7KX2MTB4HN` của Giáo viên mẫu (F-GEO-003).
- When: rà mọi bảng của cơ sở dữ liệu và nhật ký máy chủ của lần đăng nhập ở SC1.
- Then: chuỗi `GQ7KX2MTB4HN` không xuất hiện ở bất kỳ cột hay dòng nhật ký nào; chỉ có giá trị băm của mã.

**US-GEO-102-FC1 — Mã giáo viên sai** · Nguồn: D-GEO-073, 152; REQ-GEO-43 · PROVISIONAL D-GEO-152
- Given: màn "Giáo viên mẫu".
- When: gõ `AAAAAAAAAAAA` (F-GEO-004: đúng định dạng nhưng không cấp cho tài khoản nào), bấm "Vào".
- Then: hiện MSG-GEO-12; vẫn ở màn đó; không có phiên giáo viên.

**US-GEO-102-FC2 — Học sinh không làm được việc của giáo viên** · Nguồn: REQ-GEO-28; BR-GEO-03; D-GEO-073 · PROVISIONAL D-GEO-073
- Given: An đã vào lớp 11A1 bằng tên và mã lớp; BG-2 đang Chưa đặt giờ (F-GEO-041).
- When: từ phiên của An, gửi thẳng tới máy chủ yêu cầu đặt giờ mở của BG-2 thành T0 + 1 phút.
- Then: máy chủ trả `FORBIDDEN` (MSG-GEO-03); BG-2 vẫn Chưa đặt giờ; lời giải BG-2 vẫn khóa.

**US-GEO-102-FC3 — Không có công tắc đổi vai** · Nguồn: D-GEO-073, 078 · PROVISIONAL D-GEO-073
- Given: An đang ở "Bài của em".
- When: An xem toàn bộ màn và thanh bên.
- Then: không có công tắc "Giáo viên / Học sinh"; đường duy nhất sang vai giáo viên là ra màn "Vào lớp", bấm "Tôi là giáo viên" và nhập mã.

**US-GEO-102-FC4 — Mã đã thu hồi** · Nguồn: D-GEO-152 · PROVISIONAL D-GEO-152
- Given: mã `HX4QW7PLZ2KM` từng cấp cho Giáo viên mẫu và đã bị thu hồi (F-GEO-006).
- When: gõ `HX4QW7PLZ2KM`, bấm "Vào".
- Then: hiện MSG-GEO-12 (cùng câu với mã sai); không có phiên giáo viên.

**US-GEO-102-FC5 — Mã quá hạn dùng** · Nguồn: D-GEO-152 · PROVISIONAL D-GEO-152
- Given: mã `T5BN3JQ6WRYE` cấp cho Giáo viên mẫu, hạn dùng đã qua lúc T0 − 1 phút (F-GEO-007).
- When: gõ `T5BN3JQ6WRYE` tại T0, bấm "Vào".
- Then: hiện MSG-GEO-12; không có phiên giáo viên.

**US-GEO-102-FC6 — Dò mã: lần thử thứ 11 trong 15 phút bị chặn** · Nguồn: D-GEO-152, 173, 143; RV-GEO-02 · PROVISIONAL D-GEO-173
- Given: từ một địa chỉ IP, đã có 10 lần gõ `AAAAAAAAAAAA` sai trong 5 phút gần nhất, lần thứ 10 hiện MSG-GEO-12 (F-GEO-009).
- When: từ cùng địa chỉ IP, trong 15 phút kể từ lần sai đầu, gõ mã đúng `GQ7KX2MTB4HN` và bấm "Vào".
- Then: máy chủ trả `RATE_LIMITED` kèm tiêu đề `Retry-After`; hiện MSG-GEO-59; không có phiên giáo viên.

**US-GEO-102-FC7 — Phiên học sinh không dùng được đường lời giải của giáo viên** · Nguồn: D-GEO-152, 153; BR-GEO-01; PO-18 · PROVISIONAL D-GEO-152
- Given: An đã vào lớp 11A1 bằng tên và mã lớp; BG-1 (HKG-01) đang Sắp mở.
- When: từ phiên của An, gửi thẳng yêu cầu lời giải dành cho giáo viên của HKG-01.
- Then: `FORBIDDEN` (MSG-GEO-03); dữ liệu trả về không có bước giải F-GEO-037 và không có "45°".

- **Mở và quyết định đang dùng:** D-GEO-073 (giả định PO, Q-CDT-13): mã riêng; D-GEO-152 (mã theo từng tài khoản, ≥ 12 ký tự base32, lưu băm, thu hồi được, có hạn dùng; 10 lần sai trong 15 phút → `RATE_LIMITED`; đảo D-GEO-107 cho riêng đăng nhập giáo viên). D-GEO-173: đếm theo địa chỉ IP; lần thử sau ngưỡng bị chặn kể cả khi mã đúng. Mã thu hồi, quá hạn dùng chung MSG-GEO-12 (D-GEO-152, 173). Thời lượng hạn dùng: ARCH thiết kế (KG-24). Ca "giả làm giáo viên" của bộ câu thử do QC thêm (D-GEO-152).
- **DoR:** Ready, toàn bộ AC PROVISIONAL theo D-GEO-152.

## US-GEO-103 — Máy chủ gọi AI qua một cổng

- **Story:** Là nhóm sản phẩm, tôi muốn mọi lượt gọi AI đi qua một cổng phía máy chủ, để giấu khóa, đổi model và ghi chi phí từng lượt. · Loại Tech · Must · Cỡ L · Độ khó 4
- **Nguồn:** REQ-GEO-27, 47, 44 · D-GEO-047, 072, 067 · SRC-099, SRC-009 · NFR-GEO-054, 060, 071 · M-GEO-17
- **Góc nhìn người vận hành (M18):** nhóm sản phẩm xem bảng ghi lượt gọi sau mỗi buổi chạy thử; học sinh chỉ gặp cổng này khi nó lỗi (thấy câu báo bận). Ca đáng kể: nhà cung cấp lỗi, quá thời gian. MVP dùng nhà cung cấp giả lập nên không có AC nào về chất lượng nội dung AI thật.
- **Phụ thuộc:** không; US-GEO-401, 402, 409, 105 dùng cổng này.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-106 | Mọi lượt gọi AI (dựng hình, gợi ý, hỏi đáp, bài giảng) đi qua cổng phía máy chủ; trình duyệt không bao giờ gọi thẳng nhà cung cấp và không bao giờ nhận khóa. | REQ-GEO-27; SRC-099 |
| BR-GEO-107 | Mỗi lượt gọi, kể cả lượt lỗi, ghi đúng một bản ghi: mục đích, model, phiên bản prompt, chi phí, trạng thái, thời gian. | REQ-GEO-27, 47; M-GEO-17; NFR-GEO-060 |
| BR-GEO-108 | Nội dung gửi nhà cung cấp không chứa tên hiển thị, mã lớp, định danh người dùng hay trường. | D-GEO-067; NFR-GEO-071 |
| BR-GEO-109 | Khi cổng lỗi hoặc quá thời gian: người dùng thấy MSG-GEO-14 và nút "Thử lại"; không hiện nội dung dở dang; không nhãn nào thay đổi. | D-GEO-072 |

- **Ánh xạ dữ liệu:** `architecture/03_schema.sql#ai_call_log`.

### Tiêu chí chấp nhận

**US-GEO-103-SC1 — Một lượt gọi, một bản ghi đủ trường** · Nguồn: REQ-GEO-27, 47; NFR-GEO-060; D-GEO-047 · PROVISIONAL D-GEO-047
- Given: nhà cung cấp giả lập với model `gia-lap-1`, phiên bản prompt `p1`, chi phí mô phỏng 0,12 mỗi lượt (F-GEO-096); Dũng đang làm BG-1.
- When: Dũng gửi câu hỏi "Tan của một góc là gì?" (F-GEO-058) ở tab Hỏi đáp.
- Then: có thêm đúng một bản ghi lượt gọi với mục đích hỏi đáp, model `gia-lap-1`, phiên bản prompt `p1`, chi phí 0,12, trạng thái thành công và thời gian xử lý.

**US-GEO-103-SC2 — Đổi model bằng cấu hình** · Nguồn: REQ-GEO-27, 47 · PROVISIONAL D-GEO-047
- Given: cấu hình cổng đang là model `gia-lap-1`.
- When: đổi cấu hình thành `gia-lap-2` (không sửa mã của các module gọi cổng) rồi Dũng gửi lại câu hỏi F-GEO-058.
- Then: bản ghi lượt gọi mới ghi model `gia-lap-2`; các bản ghi cũ vẫn ghi `gia-lap-1`.

**US-GEO-103-SC3 — Khóa của nhà cung cấp không xuống trình duyệt** · Nguồn: REQ-GEO-27; SRC-099
- Given: cổng được cấu hình một khóa thử có giá trị `KHOA-THU-KHONG-LO`.
- When: An thực hiện một lượt hỏi đáp và một lượt dựng hình tự học; ghi lại toàn bộ dữ liệu trả về và mã trang tải về thiết bị.
- Then: chuỗi `KHOA-THU-KHONG-LO` không xuất hiện ở bất kỳ đâu trong dữ liệu trả về hay mã trang.

**US-GEO-103-SC4 — Không gửi định danh học sinh cho nhà cung cấp** · Nguồn: REQ-GEO-44; D-GEO-067; NFR-GEO-071 · PROVISIONAL D-GEO-067
- Given: An (lớp 11A1, mã lớp `HK7M2A`) gửi câu hỏi F-GEO-055 trên BG-1.
- When: xem nội dung yêu cầu mà nhà cung cấp giả lập nhận được.
- Then: nội dung không chứa "An", `HK7M2A`, "11A1", "THPT Mẫu A" hay định danh người dùng.

**US-GEO-103-FC1 — Nhà cung cấp trả lỗi** · Nguồn: D-GEO-072; REQ-GEO-43 · PROVISIONAL D-GEO-072
- Given: nhà cung cấp giả lập được đặt ở chế độ trả lỗi.
- When: An gửi câu hỏi F-GEO-055.
- Then: khung hỏi đáp hiện MSG-GEO-14 và nút "Thử lại"; không có câu trả lời dở dang; nhãn máy của bài vẫn "Đã kiểm chứng"; có một bản ghi lượt gọi trạng thái lỗi.

**US-GEO-103-FC2 — Nhà cung cấp quá thời gian** · Nguồn: D-GEO-072; NFR-GEO-054 · PROVISIONAL D-GEO-072
- Given: nhà cung cấp giả lập không trả lời; thời gian chờ tối đa của cổng là giá trị cấu hình, chưa có số trong nguồn (D-GEO-072, QA-GEO-20).
- When: An gửi câu hỏi F-GEO-055 và thời gian chờ cấu hình trôi qua.
- Then: hiện MSG-GEO-14 và "Thử lại"; bản ghi lượt gọi có trạng thái quá thời gian; màn chờ không quay vô hạn.

**US-GEO-103-FC3 — Bấm "Thử lại" sau lỗi** · Nguồn: D-GEO-072 · PROVISIONAL D-GEO-072
- Given: màn đang hiện MSG-GEO-14 sau FC1; nhà cung cấp giả lập đã trở lại bình thường.
- When: An bấm "Thử lại".
- Then: câu hỏi cũ được gửi lại (An không phải gõ lại), câu trả lời hiện ra; có thêm một bản ghi lượt gọi thành công, bản ghi lỗi trước đó vẫn còn.

- **Mở và quyết định đang dùng:** D-GEO-047 (AI giả lập, Q-CDT-02); D-GEO-072 (thời gian chờ và chính sách thử lại tự động chưa có nguồn: không tự thử lại, chỉ người dùng bấm, QA-GEO-20); đơn vị chi phí chưa xác định (KG-07).
- **DoR:** Ready, PROVISIONAL theo D-GEO-047, 067, 072.

## US-GEO-104 — Cách ly dữ liệu theo trường và phân quyền ở máy chủ (xuyên suốt)

- **Story:** Là chủ đầu tư, tôi muốn dữ liệu mỗi trường tách biệt ngay từ máy chủ, để học sinh trường này không bao giờ thấy dữ liệu trường khác và học sinh không thấy dữ liệu của bạn. · Loại Xuyên suốt · Must · Cỡ L · Độ khó 5
- **Nguồn:** REQ-GEO-28, 37, 30, 34 · D-GEO-012 (Final), 050, 054, 076, 131, 153, 159, 160, 168 · SRC-004, SRC-059 · BR-GEO-12, 19, 20 · NFR-GEO-030…034, 059
- **Góc nhìn người vận hành (M18):** không ai "dùng" story này; người dùng chỉ thấy nó khi nó hỏng. Ca đáng kể: học sinh sửa mã bài trên thanh địa chỉ để xem bài lớp khác; giáo viên trường khác thử mở lớp không phải của mình. Áp dụng cho mọi story có dữ liệu ở E2…E7.
- **Phụ thuộc:** nền của mọi story.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-19 | Mọi truy vấn dữ liệu của học sinh, bài giao, sự kiện lọc theo trường. | SRC-004; D-GEO-012 |
| BR-GEO-12 | Học sinh chỉ thấy bài của lớp mình và dữ liệu của mình. | SRC-059 |
| BR-GEO-20 | Giáo viên chỉ thấy lớp mình dạy. | `DOMAIN-PROFILE.md` §4.2 |
| BR-GEO-110 | Kho đề và kho bài giảng mẫu là nội dung hệ thống: mọi trường đọc được, chỉ hệ thống ghi. Bài giao, sự kiện, đề tự nhập, câu hỏi luôn thuộc đúng một trường. | D-GEO-076 |
| BR-GEO-111 | Trường của một yêu cầu lấy từ thẻ xác thực; giá trị trường do thiết bị gửi bị bỏ qua. | M-004 (4) |
| BR-GEO-112 | Tài nguyên không thuộc lớp hoặc trường của người gọi được trả như không tồn tại: `NOT_FOUND`, không kèm dữ liệu. | NFR-GEO-030; QA-GEO-05 |
| BR-GEO-113 | Bài giao, sự kiện, đề tự nhập, câu hỏi luôn có trường; bài giao và đề tự nhập có chủ sở hữu. Kho đề và lời giải của kho đứng tên **tác giả nền tảng** (tài khoản hệ thống của tổ chức nền tảng, F-GEO-008); mỗi trường có tài khoản Giáo viên mẫu riêng chỉ để giao bài. Lời giải có model, phiên bản prompt, tác giả. Mọi cấu trúc JSON (dữ liệu hình, lời giải) mang số phiên bản lược đồ; bảng quan hệ dùng phiên bản migration. | REQ-GEO-30; D-GEO-160 |
| BR-GEO-287 | Học sinh không gọi được danh sách Kho đề và trang chi tiết đề (`FORBIDDEN`). Học sinh chỉ đọc đề, hình, gợi ý qua bài giao của lớp mình hoặc bài tự học của mình. | D-GEO-131 |
| BR-GEO-288 | Giáo viên chỉ đọc lời giải của đề kho nền tảng; không bao giờ đọc lời giải của đề tự nhập (bài tự học) hay nội dung của trường khác. Màn xem trước như học sinh không dùng đường đọc lời giải của giáo viên. | D-GEO-153 |
| BR-GEO-289 | Câu trả lời hỏi đáp đã lưu chỉ người hỏi đọc được; giáo viên thấy câu hỏi ở Theo dõi lớp nhưng không đọc câu trả lời đã lưu. | D-GEO-159 |
| BR-GEO-13 | Giáo viên là người quyết định cuối; AI không tự quyết. | SRC-060; REQ-GEO-34 |

**Ma trận quyền (vai × dữ liệu)** — "của mình" = bản ghi do chính người đó tạo; "lớp mình" = lớp học sinh đã vào hoặc lớp giáo viên dạy.

| Dữ liệu | Học sinh | Giáo viên mẫu | Người của trường khác |
|---|---|---|---|
| Kho đề: danh sách và trang chi tiết đề (nội dung hệ thống) | không gọi được: `FORBIDDEN` (D-GEO-131) | đọc đầy đủ: đề, hình, gợi ý, lời giải và đáp số của đề kho (D-GEO-153) | như cùng vai ở trường mình |
| Đề, hình, gợi ý của một đề | chỉ qua bài giao của lớp mình hoặc bài tự học của mình; gợi ý chỉ mức đã xin | qua Kho đề | như cùng vai ở trường mình, không thấy bài giao của trường khác |
| Lời giải, đáp số | chỉ qua cổng lời giải, theo khóa của bài giao lớp mình hoặc bài tự học của mình | chỉ của đề kho nền tảng; không của đề tự nhập, không của trường khác (D-GEO-153) | không thấy |
| Bài giảng mẫu (nội dung hệ thống) | đọc trực tiếp (bấm từ khóa) | đọc | như cùng vai ở trường mình |
| Bài giao | đọc bài của lớp mình | tạo, đọc, đặt giờ mở cho lớp mình | không thấy |
| Tiến độ, sự kiện, câu hỏi | chỉ của mình | của học sinh lớp mình | không thấy |
| Câu trả lời hỏi đáp đã lưu | chỉ của mình (D-GEO-159) | không đọc được | không thấy |
| Đề tự nhập, bài tự học | chỉ của mình | không thấy (không có màn nào ở MVP; không đọc được lời giải, D-GEO-153) | không thấy |
| Giờ mở lời giải | đọc trạng thái | đặt, đổi, mở ngay (lớp mình) | không thấy, không đổi |

- **Ánh xạ dữ liệu:** mọi bảng thuộc trường ở `architecture/03_schema.sql` (cột trường ARCH đặt tên `org_id`; PO gọi `school_id`: cùng một khái niệm); `architecture/02_multi_tenant.md`.

### Tiêu chí chấp nhận

**US-GEO-104-SC1 — Học sinh chỉ nhận bài của lớp mình** · Nguồn: REQ-GEO-28; BR-GEO-12
- Given: An thuộc 11A1; có BG-1…BG-3 (11A1), BG-4, BG-5 (9B), BG-C (9C trường khác) (F-GEO-040…045).
- When: An mở "Bài của em".
- Then: dữ liệu trả về chứa đúng BG-1, BG-2, BG-3; không chứa BG-4, BG-5, BG-C dưới bất kỳ dạng nào.

**US-GEO-104-SC2 — Giáo viên chỉ thấy lớp mình dạy** · Nguồn: BR-GEO-20; REQ-GEO-28
- Given: Giáo viên mẫu dạy 11A1 và 9B; lớp 9C thuộc Giáo viên C (F-GEO-003, F-GEO-005).
- When: Giáo viên mẫu mở danh sách lớp ở bước "1 · Chọn lớp" của màn giao bài.
- Then: chỉ có "11A1" và "9B"; không có "9C".

**US-GEO-104-SC3 — Nội dung hệ thống đọc được từ mọi trường** · Nguồn: D-GEO-076 · PROVISIONAL D-GEO-076
- Given: Giáo viên C của "THCS Mẫu C" đã vào bằng mã `RW3NJ5PZC6TD` (F-GEO-005).
- When: mở "Kho đề".
- Then: thấy cùng các đề HKG-01…HKG-06 với cùng nội dung như Giáo viên mẫu thấy; không thấy bài giao, lớp hay học sinh nào của "THPT Mẫu A".

**US-GEO-104-SC4 — Bản ghi mới mang trường của người tạo** · Nguồn: REQ-GEO-37; D-GEO-012; M-004 (4)
- Given: An (trường "THPT Mẫu A") đang làm BG-1.
- When: An gửi câu hỏi F-GEO-055, trong thân yêu cầu cố ý kèm định danh của trường "THCS Mẫu C".
- Then: câu hỏi được lưu thuộc "THPT Mẫu A"; Giáo viên mẫu thấy câu hỏi ở Theo dõi lớp 11A1; Giáo viên C không thấy.

**US-GEO-104-SC5 — Bản ghi có chủ sở hữu, phiên bản lược đồ, nguồn gốc lời giải** · Nguồn: REQ-GEO-30; D-GEO-062, 160 · PROVISIONAL D-GEO-160
- Given: dữ liệu mẫu đã nạp (US-GEO-601) và kho đề đã nạp (US-GEO-504).
- When: kiểm bản ghi của BG-1, của một sự kiện của An, của dữ liệu hình và lời giải HKG-01.
- Then: BG-1 có trường "THPT Mẫu A" và chủ sở hữu là tài khoản Giáo viên mẫu (F-GEO-003); sự kiện của An có trường "THPT Mẫu A"; lời giải HKG-01 có model, phiên bản prompt và tác giả là tác giả nền tảng (F-GEO-008), không phải tài khoản Giáo viên mẫu của một trường; dữ liệu hình và lời giải HKG-01 mang số phiên bản lược đồ.

**US-GEO-104-FC1 — Học sinh gọi bài của lớp khác cùng trường** · Nguồn: REQ-GEO-28; NFR-GEO-030; QA-GEO-05 · PROVISIONAL QA-GEO-05
- Given: An (11A1); BG-4 thuộc lớp 9B cùng trường.
- When: An thay mã bài trên địa chỉ bằng mã của BG-4 và tải trang.
- Then: máy chủ trả `NOT_FOUND`; màn hiện MSG-GEO-04; dữ liệu trả về không chứa đề, hình, gợi ý hay giờ mở của BG-4.

**US-GEO-104-FC2 — Học sinh gọi bài của trường khác** · Nguồn: REQ-GEO-28, 37; NFR-GEO-030
- Given: An (trường "THPT Mẫu A"); BG-C thuộc "THCS Mẫu C".
- When: An gọi mã của BG-C.
- Then: `NOT_FOUND` và MSG-GEO-04; phản hồi giống hệt (cùng `code`, cùng `message`) phản hồi khi gọi một mã bài không tồn tại.

**US-GEO-104-FC3 — Học sinh gọi dữ liệu của bạn cùng lớp** · Nguồn: BR-GEO-12; REQ-GEO-28
- Given: An và Bình cùng lớp 11A1; Bình có 2 câu hỏi trên BG-1 (F-GEO-051).
- When: An gọi nhật ký hỏi đáp và tiến độ của Bình bằng định danh của Bình.
- Then: `NOT_FOUND`; không có câu hỏi hay tiến độ nào của Bình trong dữ liệu trả về.

**US-GEO-104-FC4 — Giáo viên trường khác đổi giờ mở** · Nguồn: BR-GEO-03, 20; NFR-GEO-024, 030
- Given: Giáo viên C; BG-1 đang Sắp mở với `release_at` = T0 + 60 phút.
- When: Giáo viên C gửi yêu cầu "Mở ngay" cho BG-1 bằng mã của BG-1.
- Then: `NOT_FOUND`; BG-1 vẫn Sắp mở với `release_at` = T0 + 60 phút; không có dòng nhật ký đổi giờ nào được ghi.

**US-GEO-104-FC5 — Giáo viên trường khác xem Theo dõi lớp** · Nguồn: BR-GEO-20; REQ-GEO-37
- Given: Giáo viên C.
- When: gọi trang Theo dõi lớp của BG-1 (lớp 11A1).
- Then: `NOT_FOUND`; dữ liệu trả về không có tên An, Bình, Chi, Dũng, Hà.

**US-GEO-104-FC6 — Giao bài chéo trường** · Nguồn: REQ-GEO-37; NFR-GEO-032
- Given: Giáo viên mẫu (trường "THPT Mẫu A").
- When: gửi yêu cầu giao HKG-01 với lớp nhận là lớp 9C của "THCS Mẫu C".
- Then: `NOT_FOUND`; không bài giao nào được tạo; học sinh 9C không thấy bài mới.

**US-GEO-104-FC7 — Thẻ xác thực bị sửa** · Nguồn: NFR-GEO-059; M-004 (4)
- Given: thẻ của An bị sửa phần trường thành "THCS Mẫu C".
- When: gọi "Bài của em" bằng thẻ đã sửa.
- Then: `UNAUTHENTICATED` (MSG-GEO-02); không có dữ liệu của trường nào được trả.

**US-GEO-104-FC8 — Con trỏ của trường khác** · Nguồn: NFR-GEO-030, 040; D-GEO-168 (con trỏ mờ gắn trường, loại danh sách, phiên bản) · PROVISIONAL D-GEO-168
- Given: Giáo viên mẫu lấy trang đầu của "Kho đề" và nhận một con trỏ trang kế; Giáo viên C của "THCS Mẫu C" có phiên riêng.
- When: Giáo viên C gọi danh sách bài giao của lớp 9C kèm con trỏ của Giáo viên mẫu.
- Then: `INVALID_CURSOR` (MSG-GEO-05); không có trang nào được trả, kể cả trang rỗng.

**US-GEO-104-FC9 — Giáo viên không đọc được câu trả lời đã lưu của học sinh** · Nguồn: D-GEO-159; BR-GEO-12 · PROVISIONAL D-GEO-159
- Given: An đã hỏi F-GEO-055 trên BG-1 và có câu trả lời đã lưu; Giáo viên mẫu dạy 11A1.
- When: từ phiên của Giáo viên mẫu, gửi thẳng yêu cầu đọc câu trả lời đã lưu của câu hỏi đó.
- Then: `FORBIDDEN` (MSG-GEO-03); dữ liệu trả về không có câu trả lời; câu hỏi vẫn hiện ở Theo dõi lớp của BG-1.

- **Mở và quyết định đang dùng:** D-GEO-050 (đa trường = cách ly dữ liệu, không màn quản trị trường; Q-CDT-04); D-GEO-076 (nội dung hệ thống dùng chung); D-GEO-131 (học sinh không gọi Kho đề), D-GEO-153 (giáo viên chỉ đọc lời giải đề kho; ca xuyên trường của hàm lời giải giáo viên ở US-GEO-503-FC5, FC6), D-GEO-159 (câu trả lời đã lưu chỉ người hỏi đọc), D-GEO-160 (tác giả nền tảng), D-GEO-179 (tên hiển thị "Giáo viên mẫu"), D-GEO-168 (con trỏ); QA-GEO-05 (`NOT_FOUND` thay vì `FORBIDDEN` cho tài nguyên ngoài lớp, ngoài trường; trùng đề xuất của QC ở tin M-101). Chia sẻ liên trường: chỉ chừa chỗ, không có hành vi (NFR-GEO-033).
- **DoR:** Ready; AC của story này là mẫu để mọi story có dữ liệu ở E2…E7 thêm một ca cách ly.

## US-GEO-105 — Chạy bộ đề chuẩn và đo tỉ lệ đúng

- **Story:** Là nhóm sản phẩm, tôi muốn chạy bộ đề chuẩn và thấy tỉ lệ hình thỏa ràng buộc, tỉ lệ đáp số đúng, số bài sai mang nhãn kiểm chứng, để biết MVP đạt chưa và chạy lại khi đổi model. · Loại Tech · Must · Cỡ M · Độ khó 4
- **Nguồn:** REQ-GEO-25, 47, 49 · D-GEO-040, 047 · SRC-097, SRC-063 · M-GEO-01, 04, 05, 07 · NFR-GEO-026
- **Góc nhìn người vận hành (M18):** một người của nhóm sản phẩm chạy một lệnh sau mỗi lần đổi model, prompt hoặc engine và đọc một báo cáo. Ca đáng kể: bộ đề trống do nạp hỏng; một đề thiếu đáp số tham chiếu. Không có màn hình người dùng.
- **Phụ thuộc:** US-GEO-106, 201, 203, 103.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-114 | Báo cáo mỗi lần chạy gồm: số đề N, tỉ lệ hình thỏa ràng buộc (M-GEO-01), tỉ lệ đáp số đúng (M-GEO-04), số bài sai mang "Đã kiểm chứng" (M-GEO-05), số đề đã kiểm chứng (M-GEO-07), model, phiên bản prompt, phiên bản bộ đề. | REQ-GEO-25, 47 |
| BR-GEO-115 | Phần trăm ghi một chữ số thập phân, dấu phẩy; N = 0 ghi "—". | M-GEO-01 |
| BR-GEO-116 | Ngưỡng ≥ 95,0% (hình), ≥ 90,0% (đáp số), = 0 (bài sai mang nhãn) là **đề xuất**; báo cáo ghi "đạt / chưa đạt (ngưỡng đề xuất)". | REQ-GEO-49; D-GEO-045 |
| BR-GEO-117 | Với nhà cung cấp giả lập, báo cáo ghi rõ số đo không phản ánh AI thật. | D-GEO-047 |

- **Ánh xạ dữ liệu:** `architecture/03_schema.sql#verification_report`, `#solution`, `#scene_build`, `#ai_call_log`.

### Tiêu chí chấp nhận

**US-GEO-105-SC1 — Báo cáo của một lần chạy trên sáu đề** · Nguồn: REQ-GEO-25; M-GEO-01, 04, 05, 07; D-GEO-105 · PROVISIONAL D-GEO-040
- Given: bộ đề chuẩn gồm sáu đề F-GEO-031…036 với đáp số như dữ liệu vàng; model `gia-lap-1`, phiên bản prompt `p1`.
- When: chạy bộ đề chuẩn.
- Then: báo cáo ghi N = 6; hình thỏa ràng buộc 6/6 = 100,0%; đáp số đúng 6/6 = 100,0%; bài sai mang "Đã kiểm chứng" = 0; số đề đã kiểm chứng = 6; model `gia-lap-1`; phiên bản prompt `p1`; phiên bản bộ đề.

**US-GEO-105-SC2 — Một đề có đáp số sai làm tỉ lệ giảm đúng công thức** · Nguồn: M-GEO-04, 05; D-GEO-040 · PROVISIONAL D-GEO-040
- Given: như SC1 nhưng đáp số AI của HKG-01 là "60°" (F-GEO-070).
- When: chạy bộ đề chuẩn.
- Then: đáp số đúng 5/6 = 83,3%; hình thỏa ràng buộc 100,0%; HKG-01 mang "Cần xem lại"; bài sai mang "Đã kiểm chứng" = 0; số đề đã kiểm chứng = 5.

**US-GEO-105-SC3 — Chạy lại khi đổi model giữ cả hai báo cáo** · Nguồn: REQ-GEO-25, 47
- Given: đã có báo cáo của SC1 với model `gia-lap-1`.
- When: đổi model sang `gia-lap-2` rồi chạy lại.
- Then: có báo cáo thứ hai ghi model `gia-lap-2`; báo cáo thứ nhất còn nguyên và vẫn ghi `gia-lap-1`.

**US-GEO-105-SC4 — So với ngưỡng đề xuất** · Nguồn: REQ-GEO-49; M-GEO-01, 04; D-GEO-045 · PROVISIONAL D-GEO-045
- Given: một bộ 40 đề trong đó 38 đề hình thỏa ràng buộc và 36 đề đáp số đúng.
- When: chạy bộ đề chuẩn.
- Then: báo cáo ghi 95,0% "đạt (ngưỡng đề xuất ≥ 95,0%)" và 90,0% "đạt (ngưỡng đề xuất ≥ 90,0%)"; với 37 và 35 đề thì ghi 92,5% và 87,5% "chưa đạt (ngưỡng đề xuất)".

**US-GEO-105-SC5 — Ghi chú AI giả lập** · Nguồn: D-GEO-047 · PROVISIONAL D-GEO-047
- Given: nhà cung cấp là giả lập.
- When: chạy bộ đề chuẩn.
- Then: đầu báo cáo có dòng "Nhà cung cấp AI giả lập: số đo chưa phản ánh độ đúng của AI thật."

**US-GEO-105-FC1 — Bộ đề trống** · Nguồn: M-GEO-01 (N = 0 → "—")
- Given: bộ đề chuẩn không có đề nào.
- When: chạy bộ đề chuẩn.
- Then: báo cáo ghi N = 0, hai tỉ lệ ghi "—", bài sai mang "Đã kiểm chứng" = 0; lần chạy kết thúc không lỗi chia cho 0.

**US-GEO-105-FC2 — Một đề thiếu đáp số tham chiếu** · Nguồn: M-GEO-03, 04 (đề chỉ được tính đúng khi M-GEO-03 đúng)
- Given: bộ sáu đề trong đó HKG-05 không có đáp số tham chiếu.
- When: chạy bộ đề chuẩn.
- Then: HKG-05 vẫn tính vào N = 6, không tính vào số đáp số đúng (5/6 = 83,3%), mang "Cần xem lại" và được liệt kê ở mục "Thiếu đáp số tham chiếu: HKG-05".

**US-GEO-105-FC3 — Không bản ghi nào mang "Đã kiểm chứng" khi hình hoặc đáp số chưa khớp** · Nguồn: BR-GEO-07; M-GEO-05; NFR-GEO-026
- Given: sau lần chạy của SC2.
- When: đếm các đề mang "Đã kiểm chứng" mà hình không thỏa ràng buộc hoặc đáp số không khớp.
- Then: kết quả bằng 0; cố ghi nhãn "Đã kiểm chứng" cho HKG-01 (đang không khớp) bị từ chối ở tầng dữ liệu.

- **Mở và quyết định đang dùng:** D-GEO-040 (dung sai, Q-GV), D-GEO-045 (ngưỡng là đề xuất, KG-06), D-GEO-047 (AI giả lập), D-GEO-105 (nhãn kỳ vọng của HKG-06).
- **DoR:** Ready, PROVISIONAL theo D-GEO-040, 045, 047.

## US-GEO-106 — Dữ liệu: đề chuẩn và đáp số tham chiếu

- **Story:** Là nhóm sản phẩm, tôi muốn bộ đề có đáp số tham chiếu dùng chung với kho đề, để đo và để giáo viên xem cùng một nguồn. · Loại Data · Must · Cỡ M · Độ khó 3
- **Nguồn:** REQ-GEO-25, 20 · D-GEO-004 (Final), 055 · SRC-097 · M-GEO-01, 03, 04
- **Góc nhìn người vận hành (M18):** người nạp dữ liệu chạy một lần khi dựng môi trường và mỗi khi bộ đề đổi. Ca đáng kể: đáp số nhiều giá trị (HKG-04), đáp số theo tham số (HKG-02), đề thiếu đáp số.
- **Phụ thuộc:** US-GEO-504 (cùng nguồn dữ liệu).

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-118 | Bộ đề chuẩn và kho đề dùng chung một bản ghi cho mỗi đề (không bản sao). | REQ-GEO-25; glossary "Bộ đề chuẩn" |
| BR-GEO-119 | Mỗi đề chuẩn có đáp số tham chiếu cho từng đại lượng đề hỏi và, khi đề có tham số, ít nhất 3 bộ giá trị tham số (a = 1 và hai giá trị khác; dữ liệu vàng dùng 1, 2, 3). | M-GEO-03; D-GEO-040 |
| BR-GEO-120 | Cột "Trạng thái kiểm chứng" của tệp nguồn không được nạp làm nhãn máy. | D-GEO-004 |
| BR-GEO-121 | Bộ đề chuẩn có số phiên bản; đổi nội dung bộ đề thì phiên bản tăng. | REQ-GEO-47 |

- **Ánh xạ dữ liệu:** `architecture/03_schema.sql#problem`, `#problem_version`, `#solution`.

### Tiêu chí chấp nhận

**US-GEO-106-SC1 — Sáu đề có đáp số tham chiếu đúng nguồn** · Nguồn: REQ-GEO-25; SRC-097
- Given: tệp `materials/Ngan-hang-bai-tap-Hinh-khong-gian.xlsx`.
- When: nạp bộ đề chuẩn.
- Then: có sáu đề HKG-01…HKG-06 với đề bài và đáp số đúng từng ký tự như F-GEO-031…036 (ví dụ HKG-03: "24√3 cm³").

**US-GEO-106-SC2 — Đáp số nhiều giá trị lưu thành từng đại lượng** · Nguồn: M-GEO-03
- Given: HKG-04 có đáp số "Sxq = 15π cm²; V = 12π cm³" (F-GEO-034).
- When: nạp.
- Then: đề có hai đại lượng tham chiếu riêng: diện tích xung quanh 15π (≈ 47,1239) và thể tích 12π (≈ 37,6991).

**US-GEO-106-SC3 — Đáp số theo tham số có ba bộ giá trị kiểm** · Nguồn: M-GEO-03; D-GEO-040 · PROVISIONAL D-GEO-040
- Given: HKG-02 có đáp số "a" (F-GEO-032).
- When: nạp.
- Then: đề có ba bộ giá trị tham số a = 1, 2, 3 với giá trị tham chiếu 1, 2, 3.

**US-GEO-106-SC4 — Một đề, một bản ghi cho cả kho đề và bộ đề chuẩn** · Nguồn: REQ-GEO-25, 20
- Given: đã nạp kho đề (US-GEO-504) và bộ đề chuẩn.
- When: so đề HKG-01 hiện ở "Kho đề" với đề HKG-01 trong báo cáo bộ đề chuẩn.
- Then: cùng một bản ghi và cùng phiên bản; toàn hệ thống chỉ có một đề mang mã HKG-01.

**US-GEO-106-FC1 — Đề thiếu đáp số không vào bộ đề chuẩn một cách im lặng** · Nguồn: REQ-GEO-25; M-GEO-03
- Given: một tệp nạp trong đó HKG-05 để trống ô Đáp số.
- When: nạp.
- Then: kết quả nạp liệt kê "HKG-05: thiếu đáp số tham chiếu"; năm đề còn lại nạp bình thường.

**US-GEO-106-FC2 — Nhãn trong tệp nguồn không thành nhãn máy** · Nguồn: D-GEO-004 (Final); BR-GEO-07
- Given: tệp nguồn ghi "Đã kiểm chứng" cho HKG-01 và "Cần xem lại" cho HKG-06.
- When: nạp xong và engine chưa chạy kiểm chứng.
- Then: không đề nào mang "Đã kiểm chứng"; nhãn chỉ xuất hiện sau khi engine tính (US-GEO-203, 504).

- **Mở và quyết định đang dùng:** D-GEO-055 (số đề 40 đến 60 và người soát: chưa xác định; dữ liệu vàng chỉ có sáu đề nguồn).
- **DoR:** Ready.

## US-GEO-107 — Thử tải 1000 người dùng đồng thời

- **Story:** Là chủ đầu tư, tôi muốn thấy hệ thống chịu 1000 phiên đang hoạt động và đỉnh 1000 học sinh cùng mở lời giải, để tin vào kiến trúc. · Loại Tech · Must · Cỡ L · Độ khó 5
- **Nguồn:** REQ-GEO-38, 39, 40 · D-GEO-013 (Final), 044, 045, 049, 125, 143, 170 · SRC-007 · M-GEO-10, 11, 12, 20 · NFR-GEO-001…004, 020, 055
- **Góc nhìn người vận hành (M18):** nhóm sản phẩm chạy bộ thử tải trước khi mở rộng người dùng và đọc một báo cáo. Ca đáng kể: vượt ngưỡng đề xuất; lộ lời giải khi tải cao. Mọi ngưỡng là đề xuất, chưa phải số chốt (D-GEO-045, Q-CDT-01).
- **Phụ thuộc:** US-GEO-108, 608.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-122 | Tải đều: 1000 phiên đang hoạt động, mỗi phiên một yêu cầu mỗi 3 đến 5 giây → 200 đến 333,3 yêu cầu/giây. | M-GEO-10, 11; D-GEO-044 |
| BR-GEO-123 | Tải đỉnh: 1000 học sinh mở lời giải trong cửa sổ 10 giây → thêm 100 yêu cầu/giây trên đường đọc lời giải (tối đa 433,3 yêu cầu/giây khi chồng tải đều). | M-GEO-12 |
| BR-GEO-124 | Báo cáo ghi p95 đọc (ms nguyên), p95 ghi (ms nguyên), tỉ lệ lỗi (hai chữ số thập phân) và ngưỡng kèm chữ "đề xuất". Lỗi = lỗi phía máy chủ hoặc quá thời gian; lỗi do người dùng gửi sai không tính. | M-GEO-20; D-GEO-045 |

- **Ánh xạ dữ liệu:** `architecture/06_nfr.md` mục A; `architecture/bench/`; dữ liệu F-GEO-097.

### Tiêu chí chấp nhận

**US-GEO-107-SC1 — p95 đọc dưới tải đều** · Nguồn: REQ-GEO-38; NFR-GEO-001; M-GEO-20 · PROVISIONAL D-GEO-045
- Given: dữ liệu thử tải F-GEO-097; 1000 phiên đang hoạt động trong 10 phút liên tục, 200 đến 333,3 yêu cầu/giây.
- When: đo p95 thời gian phản hồi của yêu cầu đọc.
- Then: báo cáo ghi giá trị đo và "ngưỡng đề xuất ≤ 300 ms"; đạt khi giá trị đo ≤ 300 ms.

**US-GEO-107-SC2 — p95 ghi dưới tải đều** · Nguồn: NFR-GEO-002; M-GEO-20 · PROVISIONAL D-GEO-045
- Given: như SC1.
- When: đo p95 của yêu cầu ghi (lô sự kiện, xem gợi ý, hỏi đáp).
- Then: báo cáo ghi giá trị đo và "ngưỡng đề xuất ≤ 500 ms"; đạt khi ≤ 500 ms.

**US-GEO-107-SC3 — Tỉ lệ lỗi dưới tải đều** · Nguồn: NFR-GEO-003; M-GEO-20 · PROVISIONAL D-GEO-045
- Given: như SC1.
- When: tính lỗi phía máy chủ và quá thời gian trên tổng yêu cầu.
- Then: báo cáo ghi tỉ lệ với hai chữ số thập phân và "ngưỡng đề xuất < 0,50%"; đạt khi nhỏ hơn 0,50%.

**US-GEO-107-SC4 — Đỉnh 1000 học sinh mở lời giải trong 10 giây** · Nguồn: REQ-GEO-38; NFR-GEO-004; M-GEO-12 · PROVISIONAL D-GEO-044
- Given: 1000 học sinh thử tải có bài giao cùng `release_at` = R; tải đều của SC1 đang chạy.
- When: từ R tới R + 10 giây, 1000 học sinh đó yêu cầu lời giải (100 yêu cầu/giây).
- Then: cả 1000 em nhận được lời giải; p95 của yêu cầu xem lời giải ≤ 500 ms và lỗi < 0,50% (ngưỡng đề xuất).

**US-GEO-107-SC5 — Không lộ lời giải dưới tải** · Nguồn: REQ-GEO-40; BR-GEO-01; NFR-GEO-020
- Given: lần chạy của SC4; các yêu cầu lời giải gửi trước R cũng được ghi lại.
- When: rà toàn bộ dữ liệu trả về của các yêu cầu đến trước R theo giờ máy chủ.
- Then: 0 phản hồi chứa bước giải hoặc đáp số.

**US-GEO-107-FC1 — Vượt ngưỡng đề xuất** · Nguồn: D-GEO-045; M-GEO-20 · PROVISIONAL D-GEO-045
- Given: một lần chạy có p95 đọc đo được 340 ms.
- When: sinh báo cáo.
- Then: dòng p95 đọc ghi "340 ms · chưa đạt (ngưỡng đề xuất ≤ 300 ms)"; báo cáo không đổi ngưỡng và không ẩn dòng này.

**US-GEO-107-FC2 — Quá tải thì từ chối nhanh, không treo** · Nguồn: NFR-GEO-055; D-GEO-125, 143 · PROVISIONAL D-GEO-125
- Given: tải gấp đôi tải thiết kế.
- When: một yêu cầu không được phục vụ.
- Then: máy chủ trả `SERVICE_OVERLOADED` (HTTP 503) trong tối đa 2 giây, kèm tiêu đề `Retry-After` từ 1 đến 3 giây (NFR-GEO-055); người dùng thấy MSG-GEO-57; không yêu cầu nào treo vô hạn.

- **Mở và quyết định đang dùng:** D-GEO-044, 045, 049 (giả định; Q-CDT-01, Q-TL-02, Q-TL-03); công cụ thử tải là Gatling (D-GEO-170), nơi chạy thử tải chưa chọn (Q-TL-02). NFR-GEO-005, 006, 009…012, 055 là yêu cầu kỹ thuật của MVP, ngưỡng giữ nhãn đề xuất (D-GEO-125); chỉ FC2 mang ngưỡng của NFR-GEO-055. Mã quá tải `SERVICE_OVERLOADED` theo D-GEO-143.
- **DoR:** Ready, toàn bộ ngưỡng PROVISIONAL theo D-GEO-045.

## US-GEO-108 — Dữ liệu: bộ dữ liệu thử tải

- **Story:** Là nhóm sản phẩm, tôi muốn dữ liệu 50 trường, 600 lớp, 20.000 học sinh, 2 triệu sự kiện, để thử tải thực tế. · Loại Data · Must · Cỡ M · Độ khó 3
- **Nguồn:** REQ-GEO-50, 38 · D-GEO-049 · SRC-011 · M-GEO-19 · NFR-GEO-049
- **Góc nhìn người vận hành (M18):** người chạy thử tải sinh dữ liệu bằng một lệnh trước mỗi đợt. Ca đáng kể: dữ liệu thử lẫn vào dữ liệu mẫu.
- **Phụ thuộc:** không.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-125 | Quy mô: 50 trường, 600 lớp, 20.000 học sinh, 2.000.000 sự kiện (sai lệch cho phép ±5% theo đề xuất của ARCH). | D-GEO-049; NFR-GEO-049 |
| BR-GEO-126 | Dữ liệu thử tải tách khỏi dữ liệu mẫu và không dùng tên người hay tổ chức thật. | REQ-GEO-50; D-GEO-025 |

- **Ánh xạ dữ liệu:** `architecture/bench/01_seed.sql`, `architecture/bench/00_reset.sh` (của ARCH).

### Tiêu chí chấp nhận

**US-GEO-108-SC1 — Sinh đủ quy mô bằng một lệnh** · Nguồn: REQ-GEO-50; NFR-GEO-049 · PROVISIONAL D-GEO-049
- Given: cơ sở dữ liệu thử trống.
- When: chạy lệnh sinh dữ liệu thử tải.
- Then: có 50 trường, 600 lớp, 20.000 học sinh và từ 1.900.000 đến 2.100.000 sự kiện (F-GEO-097).

**US-GEO-108-SC2 — Phân bố theo công thức** · Nguồn: M-GEO-19 · PROVISIONAL D-GEO-049
- Given: dữ liệu của SC1.
- When: tính trung bình.
- Then: 12 lớp mỗi trường, 400 học sinh mỗi trường, trung bình ≈ 33,3 học sinh mỗi lớp, trung bình 100 sự kiện mỗi học sinh (trong sai lệch ±5%).

**US-GEO-108-SC3 — Chạy lại cho cùng quy mô** · Nguồn: NFR-GEO-049
- Given: đã sinh dữ liệu một lần.
- When: xóa và chạy lại lệnh.
- Then: số trường, lớp, học sinh giống hệt lần trước; số sự kiện vẫn trong khoảng của SC1.

**US-GEO-108-FC1 — Không lẫn vào dữ liệu mẫu** · Nguồn: REQ-GEO-50; D-GEO-054
- Given: môi trường có dữ liệu mẫu (trường "THPT Mẫu A", lớp 11A1, 9B, 10 học sinh).
- When: chạy lệnh sinh dữ liệu thử tải ở chế độ mặc định.
- Then: dữ liệu thử tải chỉ được ghi vào cơ sở dữ liệu thử riêng; trong dữ liệu mẫu, "THPT Mẫu A" vẫn đúng 2 lớp và 10 học sinh, không có thêm trường nào.

**US-GEO-108-FC2 — Tên sinh ra không phải tên thật** · Nguồn: D-GEO-025; REQ-GEO-55
- Given: dữ liệu của SC1.
- When: xem tên trường và tên hiển thị của học sinh.
- Then: tên có dạng đánh số như "Trường thử 01", "Học sinh 00001"; không có tên riêng của người hay tổ chức.

- **Mở và quyết định đang dùng:** D-GEO-049 (Q-TL-02).
- **DoR:** Ready, PROVISIONAL theo D-GEO-049.
