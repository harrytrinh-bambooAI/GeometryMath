# E3 — Thao tác hình 3D trên thiết bị cảm ứng: story Dev-Ready và tiêu chí chấp nhận

> Tệp sinh bởi `work/src/build_stories.py` từ `work/src/E3_body.md`, `work/src/story_header.md` và `work/05_dictionary.md`; không sửa tay, sửa nguồn rồi chạy lại. Chủ sở hữu: BA.

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
| MSG-GEO-19 | Inline | "Giá trị phải là số lớn hơn 0." | VALIDATION_FAILED |
| MSG-GEO-23 | Toast | "Hai đường thẳng này chéo nhau nên không có giao điểm." | — |
| MSG-GEO-24 | Tooltip trên kết quả đo | "Đây là đại lượng đề hỏi. Giá trị sẽ hiện khi lời giải mở." | — |
| MSG-GEO-25 | Toast | "Ba điểm thẳng hàng nên chưa xác định được mặt phẳng." | — |
| MSG-GEO-26 | Toast | "Điểm này đã có trên hình." | — |

---

> Dùng chung cho cả epic: hình thử là HKG-01 (F-GEO-031) với a = 1, tọa độ A(0,0,0), B(1,0,0), C(1,1,0), D(0,1,0), S(0,0,1), mở từ BG-1 bằng tài khoản An trên iPad (F-GEO-100) trừ khi AC nói khác. Thanh công cụ (khung `mvp-05`): "Chọn", "Điểm", "Đường", "Mặt phẳng", "Mở rộng", "Đo dài", "Đo góc", "Oxyz", "Đặt lại"; nút góc nhìn "Nhìn từ trước", "Nhìn từ trên", "Xoay tự do". BA đã đọc `SPEC.md` và `SPEC-technical.md` (bản cũ) để lấy ca tái hiện, đóng KG-01. Có thêm lệnh "Xóa hết" để xóa mọi đối tượng học sinh vẽ thêm (D-GEO-134). Kéo mặt phẳng bằng tay nắm, thiết diện động là ngoài MVP (REQ-GEO-54): không có điều khiển nào cho việc đó.

## US-GEO-301 — Xoay, thu phóng, đặt lại góc nhìn; nét đứt cạnh khuất; trục Oxyz

- **Story:** Là học sinh, tôi muốn xoay và thu phóng hình, thấy cạnh khuất bằng nét đứt, để hình dung hình không gian. · Loại UI · Must · Cỡ M · Độ khó 3
- **Nguồn:** REQ-GEO-05, 07 · D-GEO-008 (Final), 058 · SRC-077, SRC-079
- **Góc nhìn người vận hành (M18):** việc đầu tiên học sinh làm khi mở bài là kéo ngón tay để xoay hình. Ca đáng kể: xoay lạc mất hình và cần "Đặt lại"; thu phóng tới mức hình biến mất.
- **Phụ thuộc:** US-GEO-201.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-141 | Kéo một ngón (hoặc kéo chuột) để xoay; chụm hai ngón (hoặc lăn chuột) để thu phóng. | REQ-GEO-05, 07; `SPEC.md` bước 3 |
| BR-GEO-142 | Cạnh bị mặt khác che ở góc nhìn hiện tại vẽ nét đứt; cạnh thấy được vẽ nét liền; tính lại sau mỗi lần xoay. | REQ-GEO-07 |
| BR-GEO-143 | "Đặt lại" đưa góc nhìn và mức thu phóng về lúc mới mở bài; không xóa điểm, đoạn, mặt phẳng học sinh đã thêm. | REQ-GEO-07 ("đặt lại góc nhìn"); D-GEO-134 |
| BR-GEO-290 | Lệnh "Xóa hết" xóa mọi điểm, đoạn, mặt phẳng, phần mở rộng mà học sinh đã vẽ thêm trên bài đang mở; hình gốc của đề, góc nhìn và mức thu phóng hiện tại giữ nguyên. | D-GEO-134 |
| BR-GEO-144 | Xoay, thu phóng, đặt lại không được ghi là "thao tác trên hình". | D-GEO-058 |

- **Ánh xạ dữ liệu:** không có dữ liệu máy chủ mới; hình lấy từ `architecture/03_schema.sql#scene_build`.

### Tiêu chí chấp nhận

**US-GEO-301-SC1 — Kéo để xoay** · Nguồn: REQ-GEO-05; D-GEO-008
- Given: hình HKG-01 ở góc nhìn lúc mới mở, công cụ "Xoay tự do".
- When: An kéo một ngón sang trái trên khung hình.
- Then: hình quay quanh trục thẳng đứng theo hướng kéo và dừng khi nhấc ngón; nhãn đỉnh S, A, B, C, D đi theo đỉnh.

**US-GEO-301-SC2 — Chụm ngón để thu phóng trên iPad** · Nguồn: REQ-GEO-07
- Given: hình ở mức thu phóng lúc mới mở.
- When: An dang hai ngón trên khung hình.
- Then: hình to lên quanh điểm giữa hai ngón; trang web không bị phóng theo.

**US-GEO-301-SC3 — Lăn chuột để thu phóng trên máy tính** · Nguồn: REQ-GEO-05, 42
- Given: Giáo viên mẫu ở trang chi tiết đề HKG-01 trên máy tính (F-GEO-102), con trỏ trong khung hình.
- When: lăn chuột lên.
- Then: hình to lên; trang không cuộn.

**US-GEO-301-SC4 — "Nhìn từ trên"** · Nguồn: REQ-GEO-07; `Design/mvp-05`
- Given: hình đã bị xoay bất kỳ.
- When: bấm "Nhìn từ trên".
- Then: hình hiện như nhìn thẳng từ trên xuống đáy: ABCD là hình vuông, S nằm trùng vị trí A.

**US-GEO-301-SC5 — "Nhìn từ trước"** · Nguồn: REQ-GEO-07; `Design/mvp-05`
- Given: hình đã bị xoay bất kỳ.
- When: bấm "Nhìn từ trước".
- Then: hình hiện với cạnh SA thẳng đứng và mặt đáy nằm ngang thành một đoạn thẳng.

**US-GEO-301-SC6 — "Đặt lại" về góc nhìn ban đầu** · Nguồn: REQ-GEO-07; D-GEO-134 · PROVISIONAL D-GEO-134
- Given: An đã xoay, phóng to hình và đã thêm trung điểm M của SD.
- When: bấm "Đặt lại".
- Then: góc nhìn và mức thu phóng trở về như lúc mới mở bài; điểm M vẫn còn trên hình.

**US-GEO-301-SC7 — Cạnh khuất vẽ nét đứt và tính lại khi xoay** · Nguồn: REQ-GEO-07
- Given: hình ở góc nhìn lúc mới mở (khung `mvp-05`): SA, AB, AD nét đứt; SB, SC, SD, BC, CD nét liền.
- When: An xoay hình nửa vòng quanh trục thẳng đứng.
- Then: các cạnh đang bị mặt khác che ở góc nhìn mới chuyển thành nét đứt, các cạnh hết bị che chuyển thành nét liền.

**US-GEO-301-SC8 — Bật, tắt trục Oxyz** · Nguồn: REQ-GEO-05
- Given: hình không có trục.
- When: bấm "Oxyz" rồi bấm lần nữa.
- Then: lần đầu hiện ba trục có nhãn x, y, z; lần hai ba trục biến mất; hình không đổi.

**US-GEO-301-SC9 — "Xóa hết" xóa phần vẽ thêm, giữ hình gốc và góc nhìn** · Nguồn: D-GEO-134 · PROVISIONAL D-GEO-134
- Given: An đã thêm trung điểm M của SD và đoạn AC, rồi xoay hình sang một góc nhìn khác lúc mới mở.
- When: bấm "Xóa hết".
- Then: điểm M và đoạn AC biến mất; năm đỉnh S, A, B, C, D và các cạnh của hình chóp còn đủ; góc nhìn vẫn là góc An vừa xoay tới.

**US-GEO-301-FC1 — Thu phóng có giới hạn** · Nguồn: REQ-GEO-05; D-GEO-117 · PROVISIONAL D-GEO-117
- Given: hình đang hiện.
- When: An chụm ngón liên tục để thu nhỏ, rồi dang liên tục để phóng to.
- Then: thu phóng dừng ở một mức nhỏ nhất và một mức lớn nhất; ở cả hai đầu vẫn thấy ít nhất một phần hình trong khung và nút "Đặt lại" đưa hình về bình thường.

**US-GEO-301-FC2 — Xoay và thu phóng không tính là thao tác trên hình** · Nguồn: D-GEO-058 · PROVISIONAL D-GEO-058
- Given: Hà mở BG-1 lần đầu.
- When: Hà chỉ xoay, thu phóng và bấm "Đặt lại" rồi rời bài.
- Then: Hà có sự kiện mở bài; số thao tác trên hình của Hà là 0.

- **Mở và quyết định đang dùng:** D-GEO-117 (không có số giới hạn thu phóng trong nguồn); D-GEO-134 ("Đặt lại" chỉ đặt lại góc nhìn; "Xóa hết" riêng cho hình vẽ thêm). Vị trí nút "Xóa hết" trên thanh công cụ do thiết kế.
- **DoR:** Ready.

## US-GEO-302 — Chạm chọn đối tượng; tô màu cạnh và mặt

- **Story:** Là học sinh, tôi muốn chạm chọn điểm, cạnh, mặt và thấy được tô màu, để chỉ đúng đối tượng. · Loại UI · Must · Cỡ M · Độ khó 3
- **Nguồn:** REQ-GEO-05, 07 · D-GEO-001 (Final), 058 · SRC-077, SRC-079
- **Góc nhìn người vận hành (M18):** học sinh chạm vào cạnh hoặc mặt mà gợi ý vừa nhắc tới. Ca đáng kể: chạm vào chỗ điểm, cạnh và mặt chồng lên nhau; chạm trượt ra ngoài hình.
- **Phụ thuộc:** US-GEO-301.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-145 | Với công cụ "Chọn", chạm vào một điểm, cạnh hoặc mặt thì đối tượng đó được tô màu nổi bật và tên của nó hiện ra ("Điểm S", "Cạnh SD", "Mặt SAD"). | REQ-GEO-05, 07 |
| BR-GEO-146 | Khi chỗ chạm trúng nhiều đối tượng: ưu tiên điểm, rồi cạnh, rồi mặt. Chạm ra ngoài hình thì bỏ chọn. | D-GEO-134 |
| BR-GEO-147 | Mỗi lần chọn một đối tượng bằng công cụ "Chọn" là một thao tác trên hình. | D-GEO-058 |

- **Ánh xạ dữ liệu:** sự kiện thao tác: `architecture/03_schema.sql#event_log` (US-GEO-703).

### Tiêu chí chấp nhận

**US-GEO-302-SC1 — Chạm chọn một cạnh** · Nguồn: REQ-GEO-05, 07
- Given: công cụ "Chọn" đang bật.
- When: An chạm vào cạnh SD.
- Then: cạnh SD được tô màu nổi bật và dày hơn các cạnh khác; hiện tên "Cạnh SD".

**US-GEO-302-SC2 — Chạm chọn một mặt** · Nguồn: REQ-GEO-05
- Given: công cụ "Chọn".
- When: An chạm vào giữa mặt SAD.
- Then: mặt SAD được tô màu bán trong suốt; hiện tên "Mặt SAD".

**US-GEO-302-SC3 — Chạm chọn một điểm** · Nguồn: REQ-GEO-07
- Given: công cụ "Chọn".
- When: An chạm vào đỉnh A.
- Then: điểm A được tô nổi bật; hiện tên "Điểm A".

**US-GEO-302-SC4 — Mỗi lần chọn là một thao tác trên hình** · Nguồn: D-GEO-058 · PROVISIONAL D-GEO-058
- Given: Hà mở BG-1, chưa có thao tác nào.
- When: Hà dùng "Chọn" chạm cạnh SD rồi chạm mặt SAD.
- Then: số thao tác trên hình của Hà trên BG-1 là 2.

**US-GEO-302-FC1 — Chạm vào chỗ chồng nhau** · Nguồn: REQ-GEO-07; D-GEO-134 · PROVISIONAL D-GEO-134
- Given: công cụ "Chọn".
- When: An chạm đúng đỉnh D (nơi gặp nhau của cạnh SD, CD, AD và các mặt).
- Then: chỉ điểm D được chọn, hiện "Điểm D"; không cạnh hay mặt nào bị tô.

**US-GEO-302-FC2 — Chạm ra ngoài hình** · Nguồn: REQ-GEO-07; D-GEO-134 · PROVISIONAL D-GEO-134
- Given: cạnh SD đang được chọn.
- When: An chạm vào vùng trống của khung hình.
- Then: SD hết tô màu, không có đối tượng nào được chọn; không có thao tác trên hình nào được ghi thêm.

- **Mở và quyết định đang dùng:** D-GEO-134 (ưu tiên điểm > cạnh > mặt). Màu tô cụ thể theo thiết kế, không phải tiêu chí.
- **DoR:** Ready.

## US-GEO-303 — Vẽ thêm đoạn, mặt phẳng; giao điểm, giao tuyến; mở rộng mặt, kéo dài cạnh

- **Story:** Là học sinh, tôi muốn vẽ thêm đoạn, mặt phẳng, tìm giao điểm, giao tuyến, mở rộng mặt và kéo dài cạnh, để dựng đường phụ. · Loại Logic · Must · Cỡ L · Độ khó 4
- **Nguồn:** REQ-GEO-05 · D-GEO-008 (Final) · SRC-077, SRC-036 · `SPEC.md` bước 4, 5
- **Góc nhìn người vận hành (M18):** học sinh làm theo gợi ý "nối AC", "dựng mặt phẳng (SBD)". Ca đáng kể: hai đường chéo nhau mà em tưởng cắt nhau; chọn ba điểm thẳng hàng để dựng mặt phẳng.
- **Phụ thuộc:** US-GEO-302, 304.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-148 | "Đường": chọn hai điểm để vẽ đoạn nối; đoạn mới tuân quy tắc nét đứt khi bị che. | REQ-GEO-05 |
| BR-GEO-149 | Hai đường cùng nằm trong một mặt phẳng và cắt nhau thì giao điểm được tạo thành một điểm có tên, dùng lại được cho bước sau. Hai đường chéo nhau không có giao điểm. | REQ-GEO-05, 36; `SPEC.md` bước 5 |
| BR-GEO-150 | "Mặt phẳng": chọn ba điểm không thẳng hàng; giao tuyến của mặt phẳng mới với các mặt của hình được vẽ ra. | REQ-GEO-05 |
| BR-GEO-151 | "Mở rộng": áp vào một mặt thì vẽ mặt phẳng chứa mặt đó vượt ra ngoài mép hình; áp vào một cạnh thì kéo dài cạnh về hai phía. | REQ-GEO-05; D-GEO-008 |

- **Ánh xạ dữ liệu:** không có dữ liệu máy chủ ngoài sự kiện thao tác (US-GEO-703).

### Tiêu chí chấp nhận

**US-GEO-303-SC1 — Vẽ đoạn nối hai điểm** · Nguồn: REQ-GEO-05
- Given: công cụ "Đường".
- When: An chạm điểm A rồi điểm C.
- Then: đoạn AC hiện trên mặt đáy; ở góc nhìn lúc mới mở, AC là nét đứt vì nằm sau các mặt bên.

**US-GEO-303-SC2 — Giao điểm của hai đường cắt nhau có tên** · Nguồn: REQ-GEO-05; D-GEO-118 · PROVISIONAL D-GEO-118
- Given: đã có đoạn AC; chưa có điểm thêm nào.
- When: An vẽ đoạn BD.
- Then: tại chỗ AC cắt BD xuất hiện một điểm mới ở (0,5; 0,5; 0) mang tên "M" (chữ cái in hoa kế tiếp chưa dùng, bắt đầu từ M); An chọn được điểm này làm đầu mút cho đoạn tiếp theo.

**US-GEO-303-SC3 — Dựng mặt phẳng qua ba điểm** · Nguồn: REQ-GEO-05
- Given: công cụ "Mặt phẳng".
- When: An chạm lần lượt S, B, D.
- Then: mặt phẳng (SBD) hiện dạng mặt bán trong suốt qua ba điểm đó.

**US-GEO-303-SC4 — Giao tuyến của mặt phẳng mới với mặt của hình** · Nguồn: REQ-GEO-05
- Given: mặt phẳng (SBD) vừa dựng.
- When: hình vẽ lại.
- Then: giao tuyến của (SBD) với mặt đáy (ABCD) là đoạn BD được vẽ nổi bật.

**US-GEO-303-SC5 — Mở rộng một mặt** · Nguồn: REQ-GEO-05; D-GEO-008
- Given: công cụ "Mở rộng".
- When: An chạm mặt đáy ABCD.
- Then: mặt phẳng chứa ABCD được vẽ rộng ra ngoài bốn cạnh của hình vuông, bán trong suốt.

**US-GEO-303-SC6 — Kéo dài một cạnh** · Nguồn: REQ-GEO-05; D-GEO-008
- Given: công cụ "Mở rộng".
- When: An chạm cạnh SD.
- Then: đường thẳng SD được vẽ kéo dài về cả hai phía qua S và qua D; đoạn SD gốc vẫn nét liền.

**US-GEO-303-FC1 — Hai đường chéo nhau** · Nguồn: REQ-GEO-36; SRC-105
- Given: An đã kéo dài cạnh SB và cạnh AD (hai đường này chéo nhau trong HKG-01).
- When: An yêu cầu giao điểm của hai đường đó bằng cách chạm lần lượt hai đường với công cụ "Đường".
- Then: hiện MSG-GEO-23; không có điểm mới nào được tạo.

**US-GEO-303-FC2 — Ba điểm thẳng hàng** · Nguồn: REQ-GEO-05
- Given: đã có trung điểm M của SD; công cụ "Mặt phẳng".
- When: An chạm S, M, D.
- Then: hiện MSG-GEO-25; không có mặt phẳng nào được tạo.

**US-GEO-303-FC3 — Không có điều khiển kéo mặt phẳng** · Nguồn: REQ-GEO-54 (OUT, hệ quả người dùng thấy)
- Given: mặt phẳng (SBD) đã dựng.
- When: An chạm giữ và kéo mặt phẳng đó.
- Then: mặt phẳng không dịch chuyển và không có tay nắm kéo; hình chỉ xoay như bình thường.

- **Mở và quyết định đang dùng:** D-GEO-118 (đặt tên điểm mới). Xóa mọi đối tượng vẽ thêm: lệnh "Xóa hết" ở US-GEO-301-SC9 (D-GEO-134). Xóa từng đối tượng, hoàn tác, và việc giữ hình vẽ thêm sau khi tải lại trang: nguồn không nêu, không viết AC (xem `work/backlog_cases.md` qua đề xuất của BA).
- **DoR:** Ready.

## US-GEO-304 — Thêm điểm: trung điểm, điểm chia tỉ lệ, điểm tự do trên cạnh

- **Story:** Là học sinh, tôi muốn thêm trung điểm, điểm chia tỉ lệ và điểm tự do trên cạnh, để dựng đường phụ. · Loại Logic · Must · Cỡ M · Độ khó 3
- **Nguồn:** REQ-GEO-06 · D-GEO-008 (Final) · SRC-078
- **Góc nhìn người vận hành (M18):** học sinh thêm trung điểm M của SD như khung `mvp-05`. Ca đáng kể: gõ tỉ lệ có số 0; thêm lại điểm đã có.
- **Phụ thuộc:** US-GEO-302.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-152 | Công cụ "Điểm" có ba cách trên một cạnh đã chọn: trung điểm; điểm chia theo tỉ lệ m : n (m, n là số lớn hơn 0, tính từ đầu mút gọi tên trước); điểm tự do tại chỗ chạm trên cạnh. | REQ-GEO-06 |
| BR-GEO-153 | Điểm mới có tên là chữ cái in hoa kế tiếp chưa dùng trên hình, bắt đầu từ M. | D-GEO-118 (BA đề xuất; khung `mvp-05` đặt trung điểm SD là M) |
| BR-GEO-154 | Điểm mới dùng được như mọi điểm khác cho "Đường", "Mặt phẳng", "Đo dài", "Đo góc". | `SPEC.md` bước 5 |

- **Ánh xạ dữ liệu:** không có dữ liệu máy chủ ngoài sự kiện thao tác.

### Tiêu chí chấp nhận

**US-GEO-304-SC1 — Thêm trung điểm** · Nguồn: REQ-GEO-06; D-GEO-118 · PROVISIONAL D-GEO-118
- Given: công cụ "Điểm", chưa có điểm thêm nào.
- When: An chọn cạnh SD và chọn "Trung điểm".
- Then: điểm "M" hiện tại (0; 0,5; 0,5), đúng giữa S và D (F-GEO-076).

**US-GEO-304-SC2 — Thêm điểm chia tỉ lệ** · Nguồn: REQ-GEO-06; D-GEO-118 · PROVISIONAL D-GEO-118
- Given: đã có điểm M.
- When: An chọn cạnh SD, chọn "Chia tỉ lệ" và nhập 1 : 2 tính từ S.
- Then: điểm "N" hiện tại (0; 1/3; 2/3), với SN bằng một nửa ND (F-GEO-076).

**US-GEO-304-SC3 — Thêm điểm tự do trên cạnh** · Nguồn: REQ-GEO-06
- Given: công cụ "Điểm".
- When: An chạm vào một vị trí bất kỳ trên cạnh SC và chọn "Điểm tự do".
- Then: một điểm mới có tên nằm đúng trên cạnh SC tại chỗ chạm (không lệch ra ngoài cạnh).

**US-GEO-304-SC4 — Điểm mới dùng được cho công cụ khác** · Nguồn: REQ-GEO-05, 06
- Given: đã có trung điểm M của SD.
- When: An dùng "Đường" nối A với M.
- Then: đoạn AM hiện trên hình.

**US-GEO-304-FC1 — Tỉ lệ không hợp lệ** · Nguồn: REQ-GEO-06, 43
- Given: An chọn cạnh SD, "Chia tỉ lệ".
- When: nhập 0 : 2, rồi −1 : 2 (F-GEO-076).
- Then: mỗi lần hiện MSG-GEO-19; không có điểm nào được tạo.

**US-GEO-304-FC2 — Điểm trùng điểm đã có** · Nguồn: REQ-GEO-06
- Given: đã có trung điểm M của SD.
- When: An lại chọn cạnh SD và "Trung điểm".
- Then: hiện MSG-GEO-26; hình vẫn chỉ có một điểm M.

- **Mở và quyết định đang dùng:** D-GEO-118.
- **DoR:** Ready.

## US-GEO-305 — Đo độ dài, góc, khoảng cách; ẩn đại lượng đề hỏi

- **Story:** Là học sinh, tôi muốn đo độ dài, góc, khoảng cách trên hình, để kiểm yếu tố trung gian mà không đọc được đáp số. · Loại Logic · Must · Cỡ L · Độ khó 5
- **Nguồn:** REQ-GEO-06, 31 · D-GEO-024, 133, 154 · SRC-078, SRC-023, SRC-024, SRC-027 · BR-GEO-16 · XF-05 · RV-GEO-05
- **Góc nhìn người vận hành (M18):** học sinh thử đo luôn cái đề hỏi để "lấy đáp số cho nhanh"; giáo viên cần chắc rằng công cụ đo không thành đường tắt. Ca đáng kể: đo đại lượng đề hỏi; đo đại lượng "anh em" cùng giá trị trong tam giác lời giải (góc ASD thay cho góc SDA); đo sau giờ mở. Đo các yếu tố trung gian rồi tự tính ra đáp số là việc được phép (đó là làm bài), không chặn.
- **Phụ thuộc:** US-GEO-608 (trạng thái khóa), US-GEO-304.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-16 | Công cụ đo ẩn đại lượng đề hỏi và đại lượng cùng giá trị, cùng vai trò trước giờ mở. | SRC-023, SRC-024 |
| BR-GEO-155 | Mỗi đề có một danh sách đại lượng ẩn (đại lượng đề hỏi và các đại lượng cùng giá trị, cùng vai trò trong tam giác lời giải). Với HKG-01: góc SDA và góc ASD. Danh sách do máy chủ gửi chỉ nêu đối tượng, không nêu giá trị. | D-GEO-024; `02_domain_model.md` §5 |
| BR-GEO-156 | Khi lời giải khóa (bài giao Chưa đặt giờ hoặc Sắp mở; bài tự học chưa được mở lời giải), đo một đại lượng trong danh sách ẩn không hiện giá trị mà hiện MSG-GEO-24. Yếu tố khác vẫn đo được. | D-GEO-024; REQ-GEO-31 |
| BR-GEO-157 | Khi lời giải Đã mở, công cụ đo hiện đủ mọi giá trị. | XF-05 |
| BR-GEO-291 | Tọa độ đỉnh của hình gửi xuống trang Làm bài là dữ kiện của đề, không phải đáp số theo BR-GEO-01. Việc ẩn số đo (BR-GEO-16) là biện pháp chống đường tắt ở giao diện. "Lộ" (M-GEO-06, NFR-GEO-020) là khi hệ thống hiển thị hoặc trả về giá trị đáp số, kể cả khi công cụ đo hiện giá trị đại lượng đề hỏi; học sinh tự tính đáp số từ tọa độ không tính là lộ. | D-GEO-154 |
| BR-GEO-158 | Số đo hiển thị dùng dấu phẩy thập phân; góc tính bằng độ; hình có tham số thì độ dài hiện theo tham số (ví dụ "a√2"). | REQ-GEO-43; `02_domain_model.md` §4; D-GEO-115 |

**Bảng trạng thái của kết quả đo (HKG-01)**

| Trạng thái lời giải | Đo SA, AD, SD, góc SAD | Đo góc SDA (đề hỏi) | Đo góc ASD (cùng vai trò) |
|---|---|---|---|
| Chưa đặt giờ / Sắp mở | hiện giá trị | ẩn, MSG-GEO-24 | ẩn, MSG-GEO-24 |
| Đã mở | hiện giá trị | "45°" | "45°" |
| Tự học, lời giải chưa mở | hiện giá trị | ẩn, MSG-GEO-24 | ẩn, MSG-GEO-24 |

- **Ánh xạ dữ liệu:** danh sách đại lượng ẩn đi cùng dữ liệu hình của đề; chỗ lưu do ARCH bổ sung vào `architecture/03_schema.sql` trước khi tách migration (D-GEO-133).

### Tiêu chí chấp nhận

**US-GEO-305-SC1 — Đo độ dài một yếu tố trung gian khi đang khóa** · Nguồn: REQ-GEO-06; D-GEO-024; D-GEO-115 · PROVISIONAL D-GEO-024
- Given: BG-1 đang Sắp mở; công cụ "Đo dài".
- When: An chạm S rồi D.
- Then: hiện "SD = a√2".

**US-GEO-305-SC2 — Đo một góc trung gian khi đang khóa** · Nguồn: REQ-GEO-06; D-GEO-024 · PROVISIONAL D-GEO-024
- Given: BG-1 đang Sắp mở; công cụ "Đo góc".
- When: An chạm S, A, D (góc tại A).
- Then: hiện "góc SAD = 90°".

**US-GEO-305-SC3 — Đo khoảng cách từ điểm tới mặt** · Nguồn: REQ-GEO-06; D-GEO-115 · PROVISIONAL D-GEO-115
- Given: BG-1; công cụ "Đo dài".
- When: An chạm điểm S rồi chạm mặt đáy ABCD.
- Then: hiện "d(S, (ABCD)) = a" và vẽ đoạn vuông góc từ S xuống đáy (chính là SA).

**US-GEO-305-SC4 — Sau giờ mở, đại lượng đề hỏi đo được** · Nguồn: XF-05; BR-GEO-16
- Given: Giáo viên mẫu đã bấm "Mở ngay" cho BG-1 (BG-1 Đã mở).
- When: An dùng "Đo góc" chạm S, D, A (góc tại D).
- Then: hiện "góc SDA = 45°".

**US-GEO-305-SC5 — Số đo dùng dấu phẩy** · Nguồn: REQ-GEO-43
- Given: hình nón mẫu với bán kính đáy 2,5 và chiều cao 4 (US-GEO-202).
- When: An đo đường sinh bằng "Đo dài".
- Then: kết quả bắt đầu bằng "4,7" (√22,25 ≈ 4,717), dùng dấu phẩy, không dùng dấu chấm.

**US-GEO-305-SC6 — Bài chuyển Đã mở khi đang mở sẵn công cụ đo** · Nguồn: XF-05; D-GEO-074; NFR-GEO-008 · PROVISIONAL D-GEO-074
- Given: An đang mở BG-1 (Sắp mở), công cụ "Đo góc" đang bật và đã đo góc SDA (không có số); Giáo viên mẫu bấm "Mở ngay".
- When: sau khi trang của An nhận trạng thái Đã mở (không muộn hơn 10 giây, không tải lại), An đo lại góc SDA.
- Then: hiện "góc SDA = 45°".

**US-GEO-305-FC1 — Đo đại lượng đề hỏi khi đang khóa** · Nguồn: REQ-GEO-31; BR-GEO-16; D-GEO-024 · PROVISIONAL D-GEO-024
- Given: BG-1 đang Sắp mở.
- When: An dùng "Đo góc" chạm S, D, A.
- Then: cung góc tại D được vẽ nhưng không có số; hiện MSG-GEO-24; chuỗi "45" không xuất hiện ở bất kỳ đâu trên màn.

**US-GEO-305-FC2 — Đo đại lượng cùng vai trò khi đang khóa** · Nguồn: REQ-GEO-31; D-GEO-024; SRC-027 · PROVISIONAL D-GEO-024
- Given: BG-1 đang Sắp mở.
- When: An dùng "Đo góc" chạm A, S, D (góc tại S).
- Then: không có số; hiện MSG-GEO-24.

**US-GEO-305-FC3 — Bài Chưa đặt giờ cũng ẩn** · Nguồn: REQ-GEO-31, 35; D-GEO-043 · PROVISIONAL D-GEO-043
- Given: BG-2 (HKG-02) Chưa đặt giờ; An mở bài; đại lượng đề hỏi là khoảng cách giữa AC và B′D′.
- When: An dùng "Đo dài" chạm đường AC rồi đường B′D′.
- Then: không có số; hiện MSG-GEO-24.

**US-GEO-305-FC4 — Bài tự học đang khóa cũng ẩn** · Nguồn: D-GEO-024, 026 · PROVISIONAL D-GEO-024
- Given: An dán đề HKG-01 vào "Tự học" khi BG-1 đang khóa (F-GEO-062).
- When: An đo góc SDA trên hình của bài tự học đó.
- Then: không có số; hiện MSG-GEO-24.

**US-GEO-305-FC5 — Danh sách đại lượng ẩn không mang giá trị** · Nguồn: BR-GEO-01; REQ-GEO-40; D-GEO-024 · PROVISIONAL D-GEO-024
- Given: BG-1 đang Sắp mở.
- When: ghi lại dữ liệu trả về của trang Làm bài.
- Then: phần mô tả đại lượng ẩn chỉ nêu đối tượng (góc tại D tạo bởi S, D, A; góc tại S tạo bởi A, S, D); không có giá trị "45", "45°", "π/4".

- **Mở và quyết định đang dùng:** D-GEO-024 (Q-GV: tập đại lượng ẩn của mỗi đề do ai lập chưa rõ; dữ liệu vàng §2 ghi tập của HKG-01, 02, 06; HKG-03…05 không có đại lượng ẩn đo được); D-GEO-115 (cách hiện số đo khi hình có tham số). D-GEO-154: tọa độ đỉnh là dữ kiện của đề, thiết bị cần nó để vẽ hình; việc ẩn số đo là ở công cụ đo và ở dữ liệu mô tả đại lượng ẩn (FC5). Ca kiểm "lộ" chỉ tìm giá trị đáp số trong dữ liệu trả về, không tính tọa độ (US-GEO-608-SC5).
- **DoR:** Ready, PROVISIONAL theo D-GEO-024.

## US-GEO-306 — Thao tác trên iPhone: chụm ngón, nút đặt lại, bố cục điện thoại

- **Story:** Là học sinh dùng iPhone, tôi muốn hình ở trên và bảng gợi ý trượt lên từ dưới, chụm ngón để thu phóng, để làm bài trên màn nhỏ. · Loại UI · Must · Cỡ M · Độ khó 3
- **Nguồn:** REQ-GEO-07, 42 · D-GEO-001 (Final), 033 · SRC-012, SRC-017, SRC-043 · `Design/mvp-08`
- **Góc nhìn người vận hành (M18):** học sinh làm bài trên iPhone ở nhà, một tay cầm máy. Ca đáng kể: bảng gợi ý che mất hình; xoay ngang máy giữa chừng.
- **Phụ thuộc:** US-GEO-301…305, 404.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-159 | Trên iPhone màn dọc: hình ở nửa trên; bảng trượt từ dưới có ba tab "Gợi ý", "Lời giải", "Hỏi"; thanh công cụ nằm dưới hình. | `Design/mvp-08`; REQ-GEO-42 |
| BR-GEO-160 | Chạy trên trình duyệt web của iPhone, không cài ứng dụng. | D-GEO-033 |
| BR-GEO-161 | Trên iPhone, thanh công cụ hiện sáu nút chính như khung `mvp-08` và một nút "Thêm"; bấm "Thêm" mở các công cụ còn lại. Mọi công cụ hình của iPad đều tới được trên iPhone. | REQ-GEO-05, 06 (không giới hạn theo thiết bị); D-GEO-135 |

- **Ánh xạ dữ liệu:** không có.

### Tiêu chí chấp nhận

**US-GEO-306-SC1 — Bố cục điện thoại** · Nguồn: REQ-GEO-42; D-GEO-001; `Design/mvp-08`
- Given: An mở BG-1 trên iPhone màn dọc (F-GEO-101).
- When: trang hiện xong.
- Then: hình ở phía trên, thanh công cụ ngay dưới hình, bảng dưới cùng có ba tab "Gợi ý", "Lời giải", "Hỏi"; không phải cuộn ngang.

**US-GEO-306-SC2 — Chụm ngón để thu phóng** · Nguồn: REQ-GEO-07
- Given: như SC1.
- When: An chụm hai ngón trên hình.
- Then: hình nhỏ lại; trang web không bị thu phóng theo.

**US-GEO-306-SC3 — Nút đặt lại** · Nguồn: REQ-GEO-07
- Given: An đã xoay và phóng to hình trên iPhone.
- When: bấm nút "Đặt lại" trên thanh công cụ.
- Then: hình về góc nhìn và mức thu phóng lúc mới mở.

**US-GEO-306-SC4 — Bảng gợi ý trượt lên, xuống** · Nguồn: `04_story_map.md` (US-GEO-306); `Design/mvp-08`
- Given: bảng dưới đang ở mức thấp.
- When: An kéo tay nắm của bảng lên, rồi kéo xuống.
- Then: bảng cao lên che bớt hình để đọc gợi ý; kéo xuống thì hình hiện lại đủ và vẫn xoay được.

**US-GEO-306-SC5 — Sáu nút chính và nút "Thêm" trên iPhone** · Nguồn: REQ-GEO-05, 06; D-GEO-135 · PROVISIONAL D-GEO-135
- Given: như SC1.
- When: An xem thanh công cụ, bấm "Thêm", rồi tìm các công cụ "Chọn", "Điểm", "Đường", "Mặt phẳng", "Mở rộng", "Đo dài", "Đo góc", "Oxyz", "Đặt lại" và lệnh "Xóa hết".
- Then: thanh công cụ có sáu nút chính như khung `mvp-08` và nút "Thêm"; bấm "Thêm" hiện các công cụ còn lại; cả chín công cụ và lệnh "Xóa hết" đều tới được và cho kết quả như trên iPad.

**US-GEO-306-FC1 — Xoay ngang máy giữa chừng** · Nguồn: REQ-GEO-42; D-GEO-117 · PROVISIONAL D-GEO-117
- Given: An đã thêm trung điểm M và đang mở tab "Gợi ý" với Gợi ý 1 đã xem.
- When: An xoay iPhone sang ngang rồi xoay lại dọc.
- Then: điểm M, tab đang mở và Gợi ý 1 vẫn còn; trang không tải lại.

**US-GEO-306-FC2 — Vùng chạm đủ lớn** · Nguồn: D-GEO-117 · PROVISIONAL D-GEO-117
- Given: An mở BG-1 trên iPhone màn dọc.
- When: đo vùng chạm của từng nút trên thanh công cụ và ba tab của bảng dưới.
- Then: mỗi vùng chạm có chiều rộng và chiều cao không nhỏ hơn 44 điểm.

- **Mở và quyết định đang dùng:** D-GEO-117 (vùng chạm tối thiểu 44 điểm; dùng được ở màn dọc; không đặt số cho thu phóng; với màn ngang chỉ yêu cầu không mất việc đang làm); D-GEO-135 (sáu nút chính và nút "Thêm"; nút nào nằm trong sáu nút chính theo khung `mvp-08`); phiên bản iOS và trình duyệt tối thiểu chưa có (KG-09, D-GEO-068).
- **DoR:** Ready.

## US-GEO-307 — Sửa lỗi hình đã biết

- **Story:** Là học sinh, tôi muốn nhãn không lặp "Mặt Mặt …", có cảnh báo khi hai đường chéo nhau và mặt nhiều đỉnh được chia tam giác đúng, để hình không gây hiểu sai. · Loại Logic · Must · Cỡ M · Độ khó 3
- **Nguồn:** REQ-GEO-36 · SRC-105 · `SPEC-technical.md` (mục lỗi đã biết 1, 4, 5)
- **Góc nhìn người vận hành (M18):** ba lỗi này học sinh đã gặp ở bản cũ. Ca đáng kể duy nhất ngoài ba ca tái hiện: mặt lõm (bản cũ tô sai).
- **Phụ thuộc:** US-GEO-302, 303.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-162 | Tên mặt hiện đúng một chữ "Mặt": "Mặt SAB". | SRC-105 |
| BR-GEO-163 | Hai đường chéo nhau không được nối bằng một "giao điểm" xấp xỉ; hiện MSG-GEO-23. | SRC-105 |
| BR-GEO-164 | Mặt có nhiều hơn ba đỉnh được tô kín đúng vùng của mặt, kể cả mặt lõm. | SRC-105 |

- **Ánh xạ dữ liệu:** không có.

### Tiêu chí chấp nhận

**US-GEO-307-SC1 — Tên mặt không lặp chữ** · Nguồn: REQ-GEO-36; SRC-105
- Given: hình HKG-01; công cụ "Chọn".
- When: An chạm mặt SAB.
- Then: tên hiện là "Mặt SAB"; chuỗi "Mặt Mặt" không xuất hiện ở đâu trên màn.

**US-GEO-307-SC2 — Mặt bốn đỉnh tô kín** · Nguồn: REQ-GEO-36; SRC-105
- Given: hình HKG-01.
- When: An chọn mặt đáy ABCD.
- Then: vùng tô phủ đúng toàn bộ hình vuông ABCD, không hở tam giác nào và không tràn ra ngoài bốn cạnh.

**US-GEO-307-FC1 — Hai đường chéo nhau trên hình lập phương** · Nguồn: REQ-GEO-36; SRC-105
- Given: hình HKG-02 (lập phương ABCD.A′B′C′D′); An đã vẽ AC và B′D′.
- When: An yêu cầu giao điểm của AC và B′D′.
- Then: hiện MSG-GEO-23; không có điểm nào được tạo ở khoảng giữa hai đường.

**US-GEO-307-FC2 — Mặt lõm** · Nguồn: REQ-GEO-36; `SPEC-technical.md` (lỗi 5)
- Given: một lăng trụ đứng có đáy chữ L với sáu đỉnh đáy theo thứ tự (0,0), (2,0), (2,1), (1,1), (1,2), (0,2).
- When: chọn mặt đáy.
- Then: vùng tô đúng hình chữ L (diện tích 3); ô vuông lõm có góc (1,1) và (2,2) không bị tô.

- **Mở và quyết định đang dùng:** không có điểm mở. Lỗi thứ tư của REQ-GEO-36 (nhật ký hỏi đáp lọc theo bài) nằm ở US-GEO-410.
- **DoR:** Ready.
