# E7 — Theo dõi lớp và ghi sự kiện: story Dev-Ready và tiêu chí chấp nhận

> Tệp sinh bởi `work/src/build_stories.py` từ `work/src/E7_body.md`, `work/src/story_header.md` và `work/05_dictionary.md`; không sửa tay, sửa nguồn rồi chạy lại. Chủ sở hữu: BA.

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
| MSG-GEO-52 | Empty | "Chưa có học sinh nào mở bài." | — |
| MSG-GEO-53 | Empty | "Chưa có câu hỏi nào." | — |

---

> Dùng chung cho cả epic: dữ liệu lớp 11A1 trên BG-1 là F-GEO-050…059 (khớp khung `Design/mvp-03`): An mở bài, 2 gợi ý, 3 câu, 4 thao tác, 18 phút · Bình mở, 1 gợi ý, 2 câu, 2 thao tác, 11 phút · Chi mở, 0 gợi ý, 0 câu, 6 thao tác, 9 phút · Dũng mở, 2 gợi ý, 2 câu, 3 thao tác, 21 phút · Hà chưa mở. Người dùng là Giáo viên mẫu trên máy tính trừ khi AC nói khác. "Thao tác trên hình" chỉ gồm việc dùng công cụ "Chọn", "Điểm", "Đường", "Mặt phẳng", "Mở rộng", "Đo dài", "Đo góc" (D-GEO-058). Không có xếp hạng ở bất kỳ đâu (BR-GEO-15).

## US-GEO-701 — Theo dõi lớp: ai đã mở bài, tình trạng

- **Story:** Là Giáo viên mẫu, tôi muốn thấy ai đã mở bài, thời gian và tình trạng của cả lớp, để biết lớp làm tới đâu, mà không có xếp hạng. · Loại UI · Must · Cỡ M · Độ khó 3
- **Nguồn:** REQ-GEO-14, 33 · D-GEO-022, 027, 077, 078, 102, 121, 129, 167 · SRC-086, SRC-021 · BR-GEO-15, 20 · M-GEO-15, 21, 22, 23, 24 · NFR-GEO-047 · XF-02, XF-09 · `Design/mvp-03`
- **Góc nhìn người vận hành (M18):** giữa buổi hoặc buổi tối, giáo viên mở Theo dõi lớp của một bài để xem em nào chưa mở, em nào đang chờ lời giải, rồi quyết định có mở ngay không; đôi khi xem nhanh bằng điện thoại. Ca đáng kể: chưa em nào mở bài; một em vừa mở bài vài giây trước.
- **Phụ thuộc:** US-GEO-703, 605 (hai nút của thẻ vàng).

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-265 | Đầu trang: "Theo dõi lớp {tên lớp}" và dòng "Bài: {tên bài} · giao lúc {HH:mm, dd/MM/yyyy}". | `Design/mvp-03`; glossary (định dạng giờ) |
| BR-GEO-266 | Bốn thẻ: "đã mở bài" = n/N, n là số học sinh có ít nhất một sự kiện mở bài, N là số học sinh của lớp (M-GEO-15); "dùng gợi ý" = số học sinh có ít nhất một sự kiện gợi ý (M-GEO-21); "câu hỏi" = tổng số câu hỏi của lớp trên bài (M-GEO-22); "thao tác hình" = tổng thời gian của cả lớp chia N, làm tròn phút, viết "{x}′" (M-GEO-23). | M-GEO-15, 21, 22, 23; D-GEO-077 |
| BR-GEO-267 | Bảng "Học sinh" có một dòng cho mỗi học sinh của lớp, xếp theo tên A → Z, không có cách xếp nào khác, không có cột thứ hạng. Cột: "Học sinh"; "Mở bài" ("Đã mở bài" / "Chưa mở"); "Gợi ý 1 · 2"; "Hỏi đáp"; "Thời gian" ("{n} phút" hoặc "—" khi chưa mở); "Tình trạng". | D-GEO-121, 022; BR-GEO-15; XF-02 |
| BR-GEO-268 | Tình trạng của một học sinh, xét theo thứ tự: chưa có sự kiện mở bài → "Chưa bắt đầu"; đã xem lời giải → "Đã xem lời giải"; đã xem Gợi ý 1 và 2 và lời giải còn khóa → "Đang chờ lời giải"; đã mở bài, 0 gợi ý, 0 câu hỏi → "Tự làm, không cần gợi ý"; còn lại → "Đang làm". | M-GEO-24; D-GEO-077, 102 |
| BR-GEO-269 | Bài Chưa đặt giờ hoặc Sắp mở: có thẻ vàng "Lời giải đang khóa" (dòng phụ "Mở lúc {HH:mm, dd/MM/yyyy} · còn {thời gian}" hoặc "Chưa đặt giờ mở") với nút "Mở ngay", "Đổi giờ" (hành vi ở US-GEO-605). | `Design/mvp-03`; XF-09 |
| BR-GEO-270 | Thẻ "Nên nhắc lại trên lớp" của khung không có trong MVP. | D-GEO-078 |
| BR-GEO-271 | Trang chỉ dành cho giáo viên của lớp; dữ liệu của trang không chứa lời giải, đáp số của bài. | BR-GEO-20; D-GEO-027 |
| BR-GEO-272 | Giáo viên vào trang từ mục "Theo dõi" của thanh bên: danh sách bài đã giao của các lớp mình dạy, mới giao ở trên, tải theo con trỏ. Danh sách này thuộc story này, không có story riêng. | D-GEO-129 |
| BR-GEO-307 | Thanh bên của giáo viên không có mục "Tổng quan" và "Lớp học" ở MVP. | D-GEO-129 |

- **Ánh xạ dữ liệu:** `architecture/03_schema.sql#assignment_progress` (trang đọc bảng tiến độ, không đọc bảng sự kiện), `#assignment`, `#class_member`.

### Tiêu chí chấp nhận

**US-GEO-701-SC1 — Bốn thẻ tổng** · Nguồn: REQ-GEO-14; M-GEO-15, 21, 22, 23; D-GEO-077 · PROVISIONAL D-GEO-077
- Given: dữ liệu F-GEO-050…054 trên BG-1.
- When: Giáo viên mẫu mở Theo dõi lớp của BG-1.
- Then: bốn thẻ ghi "4/5 đã mở bài", "3 dùng gợi ý", "7 câu hỏi", "12′ thao tác hình" ((18 + 11 + 9 + 21 + 0) / 5 = 11,8 → 12).

**US-GEO-701-SC2 — Bảng học sinh** · Nguồn: REQ-GEO-14; M-GEO-15, 23; D-GEO-121 · PROVISIONAL D-GEO-121
- Given: như SC1.
- When: xem bảng "Học sinh".
- Then: năm dòng theo thứ tự An, Bình, Chi, Dũng, Hà; cột "Mở bài" ghi "Đã mở bài" cho bốn em đầu và "Chưa mở" cho Hà; cột "Thời gian" ghi "18 phút", "11 phút", "9 phút", "21 phút", "—".

**US-GEO-701-SC3 — Tình trạng khi bài còn khóa** · Nguồn: M-GEO-24; D-GEO-077 · PROVISIONAL D-GEO-077
- Given: như SC1; BG-1 Sắp mở.
- When: xem cột "Tình trạng".
- Then: An "Đang chờ lời giải"; Bình "Đang làm"; Chi "Tự làm, không cần gợi ý"; Dũng "Đang chờ lời giải"; Hà "Chưa bắt đầu".

**US-GEO-701-SC4 — Tình trạng sau khi mở lời giải** · Nguồn: D-GEO-102; M-GEO-24 · PROVISIONAL D-GEO-102
- Given: Giáo viên mẫu đã mở ngay BG-1; An đã xem lời giải; Dũng chưa xem.
- When: tải lại Theo dõi lớp của BG-1.
- Then: An "Đã xem lời giải"; Dũng "Đang làm"; ba em còn lại như SC3.

**US-GEO-701-SC5 — Thẻ vàng khi lời giải khóa** · Nguồn: REQ-GEO-16; XF-09; `Design/mvp-03`
- Given: BG-1 Sắp mở (mở lúc T0 + 60 phút); BG-2 Chưa đặt giờ; BG-3 Đã mở.
- When: lần lượt mở Theo dõi lớp của ba bài tại T0.
- Then: BG-1 có thẻ vàng "Lời giải đang khóa", dòng phụ "Mở lúc" kèm giờ và "còn 1 giờ 0 phút", nút "Mở ngay", "Đổi giờ"; BG-2 có thẻ vàng với dòng phụ "Chưa đặt giờ mở" và hai nút; BG-3 không có thẻ vàng.

**US-GEO-701-SC6 — Vào trang từ mục "Theo dõi"** · Nguồn: REQ-GEO-14; D-GEO-129 · PROVISIONAL D-GEO-129
- Given: Giáo viên mẫu có năm bài giao BG-1…BG-5.
- When: bấm "Theo dõi" ở thanh bên.
- Then: danh sách năm bài giao hiện tên bài, tên lớp, trạng thái giờ mở, bài mới giao ở trên; bấm BG-1 thì mở Theo dõi lớp của BG-1; không có bài của lớp 9C.

**US-GEO-701-SC7 — Xem nhanh trên điện thoại** · Nguồn: REQ-GEO-42; D-GEO-001
- Given: Giáo viên mẫu trên điện thoại (F-GEO-103).
- When: mở Theo dõi lớp của BG-1.
- Then: bốn thẻ, thẻ vàng và năm dòng học sinh đều đọc được không phải cuộn ngang trang; nút "Mở ngay" bấm được.

**US-GEO-701-FC1 — Chưa em nào mở bài** · Nguồn: REQ-GEO-14; M-GEO-15, 23
- Given: một bài vừa giao cho 11A1, chưa học sinh nào mở.
- When: mở Theo dõi lớp của bài đó.
- Then: thẻ ghi "0/5", "0", "0", "0′"; bảng vẫn có năm dòng, mỗi dòng "Chưa mở", "—", "—", "Chưa bắt đầu"; phía trên bảng có MSG-GEO-52.

**US-GEO-701-FC2 — Không có xếp hạng, không sắp theo kết quả** · Nguồn: REQ-GEO-33; BR-GEO-15; D-GEO-022 · PROVISIONAL D-GEO-022
- Given: Theo dõi lớp của BG-1.
- When: bấm vào tiêu đề các cột "Thời gian", "Gợi ý 1 · 2", "Hỏi đáp", "Tình trạng".
- Then: thứ tự dòng không đổi (vẫn A → Z); không có số thứ hạng, huy hiệu, điểm hay so sánh giữa học sinh.

**US-GEO-701-FC3 — Không có "Nên nhắc lại trên lớp"** · Nguồn: D-GEO-078 · PROVISIONAL D-GEO-078
- Given: Theo dõi lớp của BG-1.
- When: xem cột bên phải.
- Then: có thẻ vàng và khối "Học sinh đang hỏi gì"; không có thẻ "Nên nhắc lại trên lớp" hay câu nhận định nào do máy tự viết.

**US-GEO-701-FC4 — Học sinh gọi Theo dõi lớp** · Nguồn: BR-GEO-12, 20; REQ-GEO-28
- Given: phiên của An.
- When: gọi thẳng trang Theo dõi lớp của BG-1.
- Then: `FORBIDDEN` (MSG-GEO-03); không có tên, thời gian hay câu hỏi của bạn nào trong dữ liệu trả về.

**US-GEO-701-FC5 — Trang không mang lời giải** · Nguồn: D-GEO-027; BR-GEO-18 · PROVISIONAL D-GEO-027
- Given: BG-1 Sắp mở.
- When: ghi lại dữ liệu trả về của Theo dõi lớp của BG-1.
- Then: không có bước giải F-GEO-037 và không có đáp số "45°".

**US-GEO-701-FC6 — Trang không đọc bảng sự kiện, không truy vấn lặp theo học sinh** · Nguồn: D-GEO-014 (Final); REQ-GEO-39; NFR-GEO-009, 047
- Given: dữ liệu thử tải F-GEO-097; một lớp 5 học sinh và một lớp 34 học sinh.
- When: mở Theo dõi lớp của một bài ở mỗi lớp, có công cụ đếm câu lệnh cơ sở dữ liệu.
- Then: hai lần dùng cùng số câu lệnh; không câu lệnh nào đọc bảng sự kiện.

**US-GEO-701-FC7 — Thanh bên không có "Tổng quan" và "Lớp học"** · Nguồn: D-GEO-129, 112 · PROVISIONAL D-GEO-129
- Given: Giáo viên mẫu đã vào bằng mã của mình, trên máy tính.
- When: xem toàn bộ thanh bên.
- Then: có mục "Theo dõi"; không có mục "Tổng quan" và không có mục "Lớp học".

- **Mở và quyết định đang dùng:** D-GEO-077 (Q-GV: cách tính thẻ "thao tác hình" và ranh giới tình trạng là suy luận của PO từ khung), D-GEO-102, 121, 022, 078; D-GEO-129 (danh sách bài đã giao nằm trong story này, SC6; "Tổng quan", "Lớp học" không hiện, FC7); D-GEO-167 (bảng học sinh phân trang theo tên rồi mã học sinh, không giới hạn cỡ lớp ở MVP). Truy vấn danh sách bài đã giao trên mọi lớp: ARCH (RV-GEO-19). Cách ly trường: US-GEO-104-FC5.
- **DoR:** Ready, PROVISIONAL theo D-GEO-077, 129.

## US-GEO-702 — Theo dõi lớp: mức gợi ý và câu hỏi của từng học sinh

- **Story:** Là Giáo viên mẫu, tôi muốn thấy mỗi học sinh đã dùng gợi ý mức nào và hỏi gì, để giúp đúng chỗ. · Loại UI · Must · Cỡ M · Độ khó 3
- **Nguồn:** REQ-GEO-14 · D-GEO-022, 077, 116 · SRC-086 · M-GEO-15, 22 · `Design/mvp-03`
- **Góc nhìn người vận hành (M18):** giáo viên nhìn khối "Học sinh đang hỏi gì" để biết câu nào nhiều em hỏi và chữa trên lớp. Ca đáng kể: nhiều em hỏi cùng một câu; chưa ai hỏi.
- **Phụ thuộc:** US-GEO-701, 409, 404.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-273 | Cột "Gợi ý 1 · 2" có hai đèn: sáng 0, 1 hoặc 2 đèn theo mức gợi ý cao nhất học sinh đã xem trên bài. | M-GEO-15 |
| BR-GEO-274 | Cột "Hỏi đáp" ghi "{n} câu" với n là số câu hỏi của học sinh trên bài, hoặc "—" khi chưa hỏi; bấm vào thì hiện các câu hỏi của em đó trên bài, mới nhất ở trên. | M-GEO-15, 22 |
| BR-GEO-275 | Khối "Học sinh đang hỏi gì" gộp các câu trùng nguyên văn sau khi bỏ khác biệt hoa thường và khoảng trắng; mỗi nhóm ghi câu hỏi, tên các học sinh đã hỏi và "{n} lượt"; nhóm có câu hỏi mới nhất ở trên. Không dùng AI để nhóm hay nhận định. | D-GEO-116; M-GEO-15 |

- **Ánh xạ dữ liệu:** `architecture/03_schema.sql#assignment_progress` (mức gợi ý, số câu hỏi), `#qa_turn`.

### Tiêu chí chấp nhận

**US-GEO-702-SC1 — Mức gợi ý của từng em** · Nguồn: REQ-GEO-14; M-GEO-15
- Given: dữ liệu F-GEO-050…054 trên BG-1.
- When: xem cột "Gợi ý 1 · 2".
- Then: An hai đèn sáng; Bình một đèn; Chi không đèn; Dũng hai đèn; Hà không đèn.

**US-GEO-702-SC2 — Số câu hỏi của từng em** · Nguồn: REQ-GEO-14; M-GEO-22
- Given: như SC1.
- When: xem cột "Hỏi đáp".
- Then: An "3 câu"; Bình "2 câu"; Chi "—"; Dũng "2 câu"; Hà "—".

**US-GEO-702-SC3 — Câu hỏi của một em** · Nguồn: REQ-GEO-14; M-GEO-15
- Given: như SC1.
- When: bấm "3 câu" ở dòng An.
- Then: hiện ba câu của An trên BG-1 (F-GEO-055, 056, 057), câu mới nhất ở trên; không có câu của An trên bài khác.

**US-GEO-702-SC4 — Khối "Học sinh đang hỏi gì" gộp câu trùng** · Nguồn: D-GEO-116; `Design/mvp-03` · PROVISIONAL D-GEO-116
- Given: bảy câu hỏi của lớp trên BG-1 (F-GEO-055…058).
- When: xem khối "Học sinh đang hỏi gì".
- Then: có bốn nhóm: "Vì sao A là hình chiếu của S?" · An, Dũng · 2 lượt; "Góc cần tìm là góc nào trên hình?" · Bình, An · 3 lượt; "Cho em xin đáp số luôn được không?" · An · 1 lượt; "Tan của một góc là gì?" · Dũng · 1 lượt.

**US-GEO-702-SC5 — Gộp không phân biệt hoa thường và khoảng trắng** · Nguồn: D-GEO-116 · PROVISIONAL D-GEO-116
- Given: như SC4; Chi hỏi thêm "vì sao  A là hình chiếu của S? " (chữ đầu viết thường, thừa khoảng trắng).
- When: tải lại khối.
- Then: nhóm "Vì sao A là hình chiếu của S?" ghi An, Dũng, Chi · 3 lượt; không có nhóm mới.

**US-GEO-702-FC1 — Chưa ai hỏi** · Nguồn: REQ-GEO-14
- Given: một bài chưa có câu hỏi nào (BG-2).
- When: mở Theo dõi lớp của bài đó.
- Then: khối "Học sinh đang hỏi gì" hiện MSG-GEO-53; mọi dòng ở cột "Hỏi đáp" ghi "—".

**US-GEO-702-FC2 — Không lẫn câu hỏi của bài khác, lớp khác** · Nguồn: BR-GEO-20; REQ-GEO-28
- Given: An có một câu hỏi trên BG-3; Khánh (9B) có một câu hỏi trên BG-4.
- When: mở Theo dõi lớp của BG-1.
- Then: thẻ "câu hỏi" vẫn là 7; khối câu hỏi không có hai câu kia.

**US-GEO-702-FC3 — Không có nhận định do AI viết** · Nguồn: D-GEO-116, 078; REQ-GEO-34 · PROVISIONAL D-GEO-116
- Given: như SC4; số bản ghi lượt gọi AI là n.
- When: mở Theo dõi lớp của BG-1.
- Then: khối chỉ có câu hỏi nguyên văn của học sinh, tên và số lượt; số bản ghi lượt gọi AI vẫn là n.

- **Mở và quyết định đang dùng:** D-GEO-116, 077. Câu hỏi có nội dung nhạy cảm: nguồn không có quy tắc lọc hay che; không viết AC (BA đề xuất đưa vào `work/backlog_cases.md`).
- **DoR:** Ready.

## US-GEO-703 — Ghi sự kiện tương tác

- **Story:** Là nhóm sản phẩm, tôi muốn ghi mở bài, thao tác hình, gợi ý, hỏi đáp, mở lời giải, để theo dõi lớp và đo lường. · Loại Logic · Must · Cỡ L · Độ khó 4
- **Nguồn:** REQ-GEO-22, 49 · D-GEO-058, 059, 125, 126, 144, 162, 165 · SRC-094, SRC-108 · M-GEO-08, 16 · NFR-GEO-050, 051, 052 · ADR-GEO-11 · RV-GEO-17, 22
- **Góc nhìn người vận hành (M18):** học sinh không thấy việc ghi này; giáo viên thấy kết quả ở Theo dõi lớp; nhóm sản phẩm đọc tỉ lệ phiên có thao tác trên hình. Ca đáng kể: mạng rớt giữa lúc đang vẽ; thiết bị gửi lại cùng một lô.
- **Phụ thuộc:** US-GEO-704; nguồn sự kiện ở E3, E4, E6.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-276 | Năm loại sự kiện: mở bài; thao tác trên hình; xem gợi ý; hỏi đáp; mở lời giải. Mỗi sự kiện có học sinh, trường, bài (bài giao hoặc bài tự học), loại, chi tiết, thời điểm. Học sinh và trường lấy từ thẻ xác thực. | REQ-GEO-22; `02_domain_model.md` §1; M-004 (4) |
| BR-GEO-277 | Thao tác trên hình = một lần dùng "Chọn", "Điểm", "Đường", "Mặt phẳng", "Mở rộng", "Đo dài", "Đo góc". Xoay, thu phóng, "Đặt lại", "Oxyz", các nút góc nhìn không tính. | D-GEO-058 |
| BR-GEO-278 | Phiên làm bài = một lần học sinh mở một bài, kết thúc khi 30 phút không có sự kiện: khoảng nghỉ từ 30 phút 0 giây trở lên mở phiên mới, 29 phút 59 giây vẫn cùng phiên (D-GEO-144). Tỉ lệ phiên có thao tác trên hình = số phiên có ít nhất một thao tác / tổng số phiên, một chữ số thập phân; không có phiên nào thì "—". | D-GEO-059; M-GEO-08 |
| BR-GEO-279 | Sự kiện mở bài, gợi ý, hỏi đáp, mở lời giải ghi bền: khi máy chủ đã trả thành công thì không mất. Mở bài: đúng một sự kiện cho mỗi lần mở, gửi ngay, không gom lô. Chỉ sự kiện thao tác trên hình được thiết bị gom và gửi theo lô (tối đa mỗi 10 giây hoặc 50 sự kiện) và có thể mất tối đa khoảng 10,6 giây khi máy chủ sập đột ngột. Sự kiện `scene_shown` (US-GEO-401) cũng không bền và chỉ dùng làm số đo. | NFR-GEO-050, 051; ADR-GEO-11; D-GEO-125, 126, 162, 165 |
| BR-GEO-280 | Một lô gửi lại không làm số đếm tăng gấp đôi. | NFR-GEO-052; `03_gate3_preliminary_stories.md` US-GEO-703 |

- **Ánh xạ dữ liệu:** `architecture/03_schema.sql#event_log`, `#assignment_progress`.

### Tiêu chí chấp nhận

**US-GEO-703-SC1 — Mở bài tạo sự kiện mở bài** · Nguồn: REQ-GEO-22; M-GEO-15
- Given: Hà chưa mở BG-1; Theo dõi lớp ghi "4/5 đã mở bài".
- When: Hà mở BG-1 và chờ 10 giây.
- Then: Theo dõi lớp ghi "5/5 đã mở bài"; dòng Hà ghi "Đã mở bài" và tình trạng "Tự làm, không cần gợi ý".

**US-GEO-703-SC2 — Mỗi công cụ là một thao tác trên hình** · Nguồn: REQ-GEO-22; D-GEO-058 · PROVISIONAL D-GEO-058
- Given: Hà vừa mở BG-1, 0 thao tác.
- When: Hà dùng mỗi công cụ "Chọn", "Điểm", "Đường", "Mặt phẳng", "Mở rộng", "Đo dài", "Đo góc" đúng một lần.
- Then: số thao tác trên hình của Hà trên BG-1 là 7.

**US-GEO-703-SC3 — Sự kiện gợi ý ghi bền** · Nguồn: REQ-GEO-22; NFR-GEO-050
- Given: Hà bấm "Xem gợi ý 1" trên BG-1 và đã thấy nội dung gợi ý.
- When: máy chủ ứng dụng bị khởi động lại ngay sau đó; Hà tải lại bài.
- Then: tiến độ của Hà vẫn 1/3 và Gợi ý 1 vẫn hiện; Theo dõi lớp vẫn tính Hà vào "dùng gợi ý".

**US-GEO-703-SC4 — Tỉ lệ phiên có thao tác trên hình** · Nguồn: REQ-GEO-49; M-GEO-08; D-GEO-058, 059 · PROVISIONAL D-GEO-059
- Given: dữ liệu F-GEO-050…053: bốn phiên làm bài của An, Bình, Chi, Dũng, phiên nào cũng có thao tác; thêm một phiên của Hà chỉ xoay hình.
- When: tính M-GEO-08 trước và sau phiên của Hà.
- Then: trước: 4/4 = 100,0%; sau: 4/5 = 80,0%; báo cáo ghi "ngưỡng đề xuất ≥ 60,0%".

**US-GEO-703-SC5 — Ranh giới phiên làm bài** · Nguồn: D-GEO-059, 144; M-GEO-23 · PROVISIONAL D-GEO-059
- Given: một học sinh có sự kiện trên một bài tại T0, T0 + 10 phút và T0 + 45 phút.
- When: tính phiên và thời gian.
- Then: có hai phiên (khoảng lặng 35 phút lớn hơn 30 phút); thời gian của em trên bài là 10 phút.

**US-GEO-703-SC6 — Bài tự học cũng ghi sự kiện** · Nguồn: REQ-GEO-22; M-GEO-16
- Given: An làm bài tự học từ đề F-GEO-060.
- When: An mở bài, dùng "Đo dài" một lần, xem Gợi ý 1, Gợi ý 2, mở lời giải.
- Then: có một sự kiện mở bài, một thao tác trên hình, hai sự kiện gợi ý và một sự kiện mở lời giải gắn với bài tự học đó; các sự kiện này không xuất hiện ở Theo dõi lớp của bài giao nào.

**US-GEO-703-FC1 — Công cụ không tính** · Nguồn: D-GEO-058 · PROVISIONAL D-GEO-058
- Given: Hà vừa mở BG-1, 0 thao tác.
- When: Hà bật "Oxyz", bấm "Nhìn từ trên", "Đặt lại", xoay và thu phóng.
- Then: số thao tác trên hình của Hà vẫn là 0.

**US-GEO-703-FC2 — Gửi lại cùng một lô** · Nguồn: NFR-GEO-052; D-GEO-125 · PROVISIONAL D-GEO-125
- Given: thiết bị của Hà gửi một lô gồm 3 thao tác trên hình và nhận lỗi mạng trước khi có phản hồi.
- When: thiết bị gửi lại đúng lô đó.
- Then: số thao tác trên hình của Hà tăng 3, không tăng 6.

**US-GEO-703-FC3 — Mất mạng khi đang thao tác** · Nguồn: `03_gate3_preliminary_stories.md` US-GEO-703; NFR-GEO-051; D-GEO-125 · PROVISIONAL D-GEO-125
- Given: Hà đang làm BG-1; mạng mất 20 giây, trong lúc đó Hà dùng "Điểm" hai lần; trang vẫn mở.
- When: mạng có lại.
- Then: hình và hai điểm mới vẫn còn trên màn; hai thao tác đó được gửi và tính trong vòng 10 giây sau khi có mạng; Hà không thấy thông báo lỗi nào chặn việc đang làm.

**US-GEO-703-FC4 — Không ghi sự kiện thay người khác** · Nguồn: BR-GEO-12; M-004 (4)
- Given: phiên của An.
- When: gửi thẳng một lô sự kiện thao tác trên BG-1, trong thân ghi định danh của Bình.
- Then: sự kiện được tính cho An; số thao tác của Bình không đổi.

**US-GEO-703-FC5 — Sự kiện cho bài không thuộc lớp mình** · Nguồn: BR-GEO-12; NFR-GEO-030
- Given: phiên của An (11A1).
- When: gửi thẳng một sự kiện mở bài cho BG-4 (lớp 9B).
- Then: `NOT_FOUND`; Theo dõi lớp của BG-4 không có An.

**US-GEO-703-SC7 — Sự kiện mở bài ghi bền** · Nguồn: D-GEO-162, 126; NFR-GEO-050; RV-GEO-22 · PROVISIONAL D-GEO-162
- Given: Hà chưa mở BG-1; Theo dõi lớp ghi "4/5 đã mở bài".
- When: Hà mở BG-1; máy chủ trả thành công cho sự kiện mở bài; ngay sau đó máy chủ ứng dụng bị khởi động lại.
- Then: sau khi máy chủ chạy lại, Theo dõi lớp ghi "5/5 đã mở bài" và dòng Hà ghi "Đã mở bài"; có đúng một sự kiện mở bài của Hà cho lần mở đó.

**US-GEO-703-SC8 — Khoảng nghỉ 29 phút 59 giây vẫn cùng phiên** · Nguồn: D-GEO-144; M-GEO-08, 23 · PROVISIONAL D-GEO-144
- Given: một học sinh có hai sự kiện trên một bài, tại T0 và T0 + 29 phút 59 giây, không có sự kiện nào khác.
- When: tính phiên và thời gian.
- Then: có một phiên; thời gian của em trên bài là 30 phút (29 phút 59 giây làm tròn phút).

**US-GEO-703-SC9 — Khoảng nghỉ đúng 30 phút 0 giây mở phiên mới** · Nguồn: D-GEO-144; M-GEO-08, 23; RV-GEO-17 · PROVISIONAL D-GEO-144
- Given: một học sinh có hai sự kiện trên một bài, tại T0 và T0 + 30 phút 0 giây, không có sự kiện nào khác.
- When: tính phiên và thời gian.
- Then: có hai phiên; thời gian của em trên bài là 0 phút (khoảng nghỉ 30 phút không được cộng vào thời gian).

- **Mở và quyết định đang dùng:** D-GEO-058, 059 (Q-CDT); D-GEO-144 (biên phiên 30 phút 0 giây); D-GEO-162 (mở bài là sự kiện bền); D-GEO-126 (chỉ sự kiện thao tác trên hình được mất khi sập); D-GEO-125 (gom lô 10 giây hoặc 50 sự kiện là yêu cầu kỹ thuật với ngưỡng đề xuất, câu D-02 ở `06_nfr.md`). Khóa lũy đẳng cho sự kiện bền gửi lại: ARCH (RV-GEO-18).
- **DoR:** Ready.

## US-GEO-704 — Dữ liệu: bảng sự kiện chia theo thời gian và phân trang theo khóa

- **Story:** Là nhóm sản phẩm, tôi muốn bảng sự kiện chia theo thời gian và đọc theo khóa, để tải lớn vẫn nhanh. · Loại Data · Must · Cỡ L · Độ khó 4
- **Nguồn:** REQ-GEO-22, 39, 38 · D-GEO-014 (Final), 124 · SRC-005 · M-GEO-19 · NFR-GEO-040, 042, 045, 046 · ADR-GEO-12, 13
- **Góc nhìn người vận hành (M18):** không có người dùng cuối; người vận hành và người viết mã cần bảng sự kiện 2 triệu dòng vẫn đọc nhanh và không ai viết truy vấn quét cả bảng. Ca đáng kể: quên tạo phần của tháng tới; truy vấn không có cận thời gian.
- **Phụ thuộc:** US-GEO-108 (dữ liệu thử), US-GEO-703.

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-GEO-281 | Bảng sự kiện chia thành các phần theo thời gian (theo tháng trong đề xuất của ARCH); luôn có sẵn ít nhất một phần cho thời gian sắp tới. | D-GEO-014 (Final); NFR-GEO-045 |
| BR-GEO-282 | Đọc danh sách sự kiện theo con trỏ (thời điểm, mã sự kiện), có cận thời gian, không OFFSET, không trả tổng số. | D-GEO-014; NFR-GEO-040, 046; M-004 (2) |
| BR-GEO-283 | Mọi truy vấn nóng trên dữ liệu thử 2 triệu sự kiện có chỉ mục phục vụ và được kiểm bằng kế hoạch thực thi: không quét toàn bộ một bảng lớn. | D-GEO-014 (Final); REQ-GEO-39; NFR-GEO-042 |

- **Ánh xạ dữ liệu:** `architecture/03_schema.sql#event_log` và phần quản lý phần chia theo tháng ở cuối tệp; `architecture/04_query_catalogue.md` (chưa có lúc viết).

### Tiêu chí chấp nhận

**US-GEO-704-SC1 — Sự kiện vào đúng phần theo thời gian** · Nguồn: D-GEO-014 (Final); NFR-GEO-045
- Given: bảng sự kiện đã có phần cho tháng hiện tại và tháng kế tiếp.
- When: ghi một sự kiện có thời điểm hiện tại.
- Then: sự kiện nằm trong phần của tháng hiện tại; phần mặc định (dành cho thời điểm không thuộc phần nào) vẫn rỗng.

**US-GEO-704-SC2 — Luôn có phần cho thời gian sắp tới** · Nguồn: NFR-GEO-045
- Given: hệ thống đang chạy.
- When: kiểm danh sách phần của bảng sự kiện.
- Then: có ít nhất một phần cho tháng sau tháng hiện tại.

**US-GEO-704-SC3 — Đọc sự kiện theo con trỏ, không trùng không sót** · Nguồn: REQ-GEO-39; D-GEO-014; NFR-GEO-040, 046
- Given: dữ liệu thử tải F-GEO-097; một học sinh thử có 100 sự kiện trên một bài.
- When: đọc sự kiện của em đó trên bài đó theo trang 50, lần lượt tới hết, trong khoảng thời gian chứa cả 100 sự kiện.
- Then: hai trang 50 và 50, mới nhất trước, 100 sự kiện mỗi cái đúng một lần; phản hồi không có tổng số.

**US-GEO-704-SC4 — Truy vấn nóng không quét toàn bảng** · Nguồn: D-GEO-014 (Final); REQ-GEO-39; NFR-GEO-042
- Given: dữ liệu thử tải F-GEO-097 (2.000.000 sự kiện).
- When: lấy kế hoạch thực thi của các truy vấn nóng: Bài của em, trang Làm bài, yêu cầu lời giải, Theo dõi lớp, Kho đề, đọc sự kiện.
- Then: không kế hoạch nào quét toàn bộ một bảng có hơn 10.000 dòng; truy vấn đọc sự kiện chỉ chạm các phần nằm trong cận thời gian.

**US-GEO-704-FC1 — Truy vấn sự kiện không có cận thời gian hoặc dùng OFFSET** · Nguồn: D-GEO-014 (Final); NFR-GEO-040, 046
- Given: bộ kiểm truy vấn tự động của dự án.
- When: thêm một truy vấn đọc bảng sự kiện không có cận thời gian, và một truy vấn dùng OFFSET.
- Then: bộ kiểm báo lỗi cho cả hai và không cho qua.

**US-GEO-704-FC2 — Giờ thiết bị ở tương lai bị kẹp về giờ nhận** · Nguồn: NFR-GEO-045; ADR-GEO-06; D-GEO-125; RV-GEO-29 · PROVISIONAL D-GEO-125
- Given: bảng sự kiện có phần cho tháng hiện tại và tháng kế tiếp; iPad của Hà bị chỉnh đồng hồ nhanh 3 tháng.
- When: thiết bị của Hà gửi một lô thao tác trên hình mang thời điểm theo đồng hồ thiết bị.
- Then: sự kiện được ghi với thời điểm bằng giờ nhận của máy chủ (quy tắc kẹp thời điểm vào khoảng [giờ nhận − 24 giờ, giờ nhận] của ADR-GEO-06), nằm trong phần của tháng hiện tại; phần mặc định vẫn rỗng.

**US-GEO-704-FC3 — Con trỏ hỏng** · Nguồn: M-004 (1, 2)
- Given: một yêu cầu đọc sự kiện.
- When: gửi con trỏ `khong-phai-con-tro` (F-GEO-084).
- Then: `INVALID_CURSOR`; không có sự kiện nào được trả.

- **Mở và quyết định đang dùng:** D-GEO-014 (Final), D-GEO-124. Chia theo tháng, kích thước trang 50, ngưỡng 10.000 dòng là yêu cầu kỹ thuật với ngưỡng đề xuất (D-GEO-125); MVP chưa dọn sự kiện cũ (D-GEO-128); AC không đặt số thời gian thực thi. Ca "phần mặc định không rỗng" không xảy ra qua API vì quy tắc kẹp; nếu cần là ca vận hành chỉ ở cơ sở dữ liệu (ARCH, QC).
- **DoR:** Ready.
