# TEST PLAN: GEO — MVP chặng 1 (bản nháp của goal run)

| | |
|---|---|
| Feature / epic(s) | GEO "Hình Học Không Gian Cùng AI", E1…E7 |
| Stories under test | US-GEO-101…108, 201…205, 301…307, 401…412, 501…505, 601…612, 701…704: 53 story, 453 tiêu chí (406 trước lượt sửa gom sau phản biện; M-222) (`work/epics/E<n>/stories_ac.md`, bản của BA trong goal run này) |
| Test spec | `work/epics/E<n>/test_cases.md` (một tệp mỗi epic, sinh từ `work/src/qc/`; 467 case sau lượt sửa gom sau phản biện, trước đó 415; số liệu ở `review/QC_test_design.md` §8); thiết kế: `review/QC_test_design.md`, `work/epics/E<n>/test_design.md`; workbook xuất bằng `export_xlsx.py` khi vào vòng thực thi |
| Build(s) / environment | khung mã `app/` (chưa có mã chức năng); PostgreSQL cổng 54329 và Redis cổng 6389 riêng của dự án (D-GEO-046), khởi động bằng `app/scripts/dev-up.sh` |
| Plan owner / approver | QC (agent) / PO (agent; chủ đầu tư xem lại sau) |
| Status | Draft |

## 0. Reader guide and glossary
Bảng giải thích mã (`TC-GEO-`, `US-GEO-`, `F-GEO-`, `D-GEO-`, `M-GEO-`, `NFR-GEO-`, `KG-`, CL, DT, BV, PROVISIONAL, ENG) ở `review/QC_test_design.md` §0 và `work/glossary.md` phần "Reader guide". R là giờ mở lời giải (`release_at`), t là giờ máy chủ, T0 là giờ máy chủ lúc bắt đầu ca kiểm.

## 1. Objective
Chứng minh lời hứa của MVP: học sinh thao tác đúng trên hình 3D và nhận gợi ý trước, còn lời giải và đáp số bị khóa ở dữ liệu máy chủ trả về cho tới giờ giáo viên đặt; nhãn "Đã kiểm chứng" chỉ do engine tính. Chứng minh ràng buộc của chủ đầu tư: cách ly theo trường, 1000 người dùng đồng thời, truy vấn tối ưu.

## 2. Scope
### 2.1 In scope
Bảy epic E1…E7, 53 story, mọi AC của BA; yêu cầu phi chức năng `NFR-GEO-001…071` của ARCH (`work/architecture/06_nfr.md`) ở mức case kỹ thuật (ENG). Màn theo khung `Design/mvp-01…08`.

### 2.2 Out of scope (mỗi mục có hệ quả nhìn thấy và case xác nhận, M4)
| Mục OUT | Hệ quả nhìn thấy | Case xác nhận |
|---|---|---|
| Hình phẳng (REQ-GEO-51) | đề hình phẳng `F-GEO-064` nhận thông báo chưa hỗ trợ | E4 (US-GEO-401), E2 (US-GEO-204) |
| Soạn đề với AI, import, OCR, giáo trình riêng (D-GEO-011) | giáo viên không có nút soạn hoặc import | E5 (màn Kho đề) |
| Giao bộ đề (D-GEO-035) | màn giao bài chỉ giao một đề | E6 (US-GEO-602) |
| "Trình chiếu", "Sửa (sắp có)", "Sau buổi học tới", "Nên nhắc lại trên lớp", thẻ chuỗi ngày, "Em đang học tới đâu" (D-GEO-078) | không xây phía sau; hiển thị theo AC của BA | E5 (US-GEO-503), E6 (US-GEO-604, 606), E7 (US-GEO-701) |
| Xếp hạng (BR-GEO-15) | Theo dõi lớp không có cột hay thứ tự xếp hạng | E7 (US-GEO-701) |
| Chất lượng AI thật (D-GEO-047) | AI giả lập trả nội dung soạn sẵn | không có case; chỉ số thời gian và độ đúng thật không đo ở MVP |
| Kéo mặt phẳng, thiết diện động, phụ huynh, chấm bài, thông báo (REQ-GEO-54) | không có trên giao diện | E3 (thanh công cụ) |

### 2.3 Deferred cases (M18)
`work/backlog_cases.md`: các ca hiếm hoặc gượng ép, mỗi dòng có lý do và mốc xem lại "sau vòng thực thi 1". PO phán khi có bất đồng.

## 3. ★ Executor and tooling (M15)
| Round | Executor (account / role used) | Device(s) / browser | Tooling available | Not available |
|---|---|---|---|---|
| R1 thực thi trên màn (execution sheet) | người kiểm thử thủ công, vào bằng học sinh An (`F-GEO-020`, mã lớp `HK7M2A`) và Giáo viên mẫu (`F-GEO-003`) | iPad và iPhone (học sinh, `F-GEO-100`, `F-GEO-101`); máy tính và điện thoại (giáo viên, `F-GEO-102`, `F-GEO-103`); trình duyệt web của thiết bị | giao diện web, đồng hồ bấm giây, chế độ máy bay | CSDL, máy khách API, công cụ thử tải |
| R1 kỹ thuật (engineering sheet) | agent mã hoặc lập trình viên (owner: TechLead) | máy phát triển | kiểm thử đơn vị và tích hợp của `app/` (`app/scripts/verify.sh`), máy khách API, `psql` tới cổng 54329, bộ quét SQL (`app/server/quality/sql-lint.properties`), `EXPLAIN (ANALYZE, BUFFERS)`, profile test có `fn_test_set_release` và vai kiểm thử (D-GEO-169), Playwright (E2E trình duyệt), datasource-proxy (đếm câu lệnh JDBC), Gatling (thử tải) theo D-GEO-170, `work/architecture/bench/` | AI thật; cụm nhiều nút (NFR-GEO-012, 058 chưa đo được); `fn_test_set_release` và ADR-GEO-14 chưa có (ARCH đang làm) |

Tách hai tờ: case có cột "Tầng tự động hóa" là "thủ công trên thiết bị" thuộc execution sheet; "đơn vị", "tích hợp API", "tích hợp CSDL", "E2E trình duyệt" (Playwright), "tải" (Gatling) mang nhãn ENG và thuộc engineering sheet, mỗi case ghi công cụ cần. Quy tắc phía máy chủ mà người kiểm thủ công không thấy trực tiếp (khóa lời giải ở dữ liệu trả về) có một case ENG và, khi có thể, một case quan sát trên màn.

## 4. Test levels, types and techniques
Mức: Component (engine, bộ quét SQL), Function, Business, Integration (API, CSDL), phi chức năng (tải, kế hoạch truy vấn). Kỹ thuật: EP, BVA (giờ mở, dung sai, trần trang), bảng quyết định (khóa lời giải × vai × trạng thái; nhãn máy; hai công tắc trợ giúp), chuyển trạng thái (giờ mở, thang gợi ý), đoán lỗi (moi đáp án), đồng thời, ma trận gián đoạn. Chiều đã bỏ theo tailoring: không có kiểm thử chấm điểm, nộp bài, phụ huynh, quản trị trường (OUT); không có biến thể theo ngôn ngữ (chỉ tiếng Việt); kiểm thử đơn vị của lập trình viên nằm ngoài kế hoạch này trừ các case ENG mà AC yêu cầu.

## 5. ★ Fixture and test data (F3, M1)
| Need | Rows / files | Status |
|---|---|---|
| Golden dataset (mọi trạng thái, cặp biên, ca đặc biệt) | `work/fixture.md` `F-GEO-001…103` (BA); `F-GEO-200…239` (QC, `review/QC_test_design.md` §6: bộ câu thử 210…231, đề gần trùng 233…235, biên phiên 236, 237, đáp số ngắn 238, 239); dữ liệu thử tải của ARCH (`work/architecture/bench/01_seed.sql`) | ready (BA đã bàn giao, M-200); dữ liệu thử tải: ready ở mức CSDL, chưa có kịch bản HTTP |
| Replica của mỗi khung đã duyệt (replica) | `mvp-03` ↔ `F-GEO-050…059` (4/5, 3, 7, 12′); `mvp-01` ↔ `F-GEO-080` (48 đề, 44 đã kiểm chứng); `mvp-06` ↔ đối tượng tô màu của `HKG-01`; `mvp-07` ↔ `F-GEO-060` | ready; KG-03: một số ảnh PO chưa xem |
| Hai đầu của mỗi quy tắc xuyên màn (both ends) | giáo viên đổi giờ hoặc Mở ngay (Theo dõi lớp) ↔ học sinh đang ở màn làm bài và Bài của em; hai công tắc trợ giúp (giao bài) ↔ màn làm bài; tới giờ mở ↔ công cụ đo, Hỏi đáp, Tự học | ready (`F-GEO-040…045`) |
| Mọi loại actor trong phạm vi (actor) | học sinh của lớp (An), học sinh lớp khác cùng trường (Khánh, 9B), học sinh trường khác (An của 9C), Giáo viên mẫu, giáo viên trường khác (`F-GEO-005`) | ready |
| Ma trận gián đoạn (interruption): sự kiện × màn × thiết bị | mất mạng, thiết bị ngủ, xoay màn, tab nền × màn làm bài, màn chờ AI, lúc tới giờ mở × iPad, iPhone | thiết kế xong; ca hiếm ở `backlog_cases.md` |
| Xung đột dữ liệu đã biết và mặc định đã quyết, in cạnh dữ liệu (M6) | nhãn `HKG-06`: tệp nguồn ghi "Cần xem lại", engine tính "Đã kiểm chứng" (`F-GEO-036`, D-GEO-105); tên cột `school_id` (PO) và `org_id` (khung mã, ARCH); Kho đề trang 20, trần 50 (D-GEO-142); biên `F-GEO-071` 45,0044 / 45,0046 và `F-GEO-073` 9,9e-7 / 1,01e-6 (D-GEO-163); mã giáo viên mẫu 12 ký tự `F-GEO-003…009` (D-GEO-152) — BA đã đổi ở M-218 | ghi trong Remark của case liên quan |

## 6. ★ Known-gaps table (M12)
Lấy từ `work/known_gaps.md` và các phát hiện của QC. Cột "Needed by" là một cổng review, không ghi ngày.

| ID (DQ / QF / QA / D) | Item | Where it sits (story / case) | Primary flow? | Owner | Needed by | Provisional behaviour in force | Cases marked PROVISIONAL |
|---|---|---|---|---|---|---|---|
| KG-01 (D-GEO-008) | Danh sách công cụ hình đầy đủ của bản cũ | E3 | Không | BA | R3 của E3 | bộ công cụ ở `mvp-05` | case E3 về danh sách công cụ |
| KG-04 (D-GEO-065) | Số câu và nhóm kỹ thuật tối thiểu của bộ câu thử moi đáp án | US-GEO-610, 611 | Có (closed: PO phán D-GEO-103, 20 câu và 10 nhóm; D-GEO-152 thêm ca giả làm giáo viên) | PO, BA | R3 của E6 | bộ 22 câu, 11 nhóm `F-GEO-210…231` (D-GEO-176; US-GEO-610-SC1, 611-SC1) gồm bộ tối thiểu `F-GEO-095`, nhóm "giả làm giáo viên" `F-GEO-230, 231` có dòng báo cáo riêng (US-GEO-610-SC6, TC-GEO-0862); cổng chặn là 0 lần lộ, F-GEO-226 là ca chặn (D-GEO-150) | case bộ câu thử ở E6 (PROVISIONAL D-GEO-065, 152) |
| KG-06 (D-GEO-045) | Ngưỡng hiệu năng là đề xuất | US-GEO-107; NFR-GEO-001…006 | Không | ChuDauTu | R3 của E1 | p95 đọc ≤ 300 ms, ghi ≤ 500 ms, lỗi < 0,5% | mọi case tải |
| KG-07 (D-GEO-047) | Đơn vị tiền tệ và nguồn chi phí AI thật | US-GEO-103 | Không | ChuDauTu | R3 của E1 | số mô phỏng 0,12 (`F-GEO-096`) | case ghi chi phí |
| KG-09 (D-GEO-068) | Phiên bản iOS và trình duyệt tối thiểu | US-GEO-306 và mọi case thiết bị | Không | TechLead | R3 của E3 | iPad, iPhone bản mới nhất dùng được | mọi case "thủ công trên thiết bị" |
| D-GEO-040 | Dung sai kiểm chứng 1e-6, 1e-4, ≥ 3 bộ tham số | US-GEO-203 | Không | GiaoVienCoVan (PO hỏi) | R3 của E2 | giá trị của D-GEO-040 | case biên `F-GEO-071`, `F-GEO-073` |
| D-GEO-041, 042, 043 | Ranh giới t ≥ R; đã mở không khóa lại; chưa đặt thì khóa | US-GEO-604, 605, 608, 612 | Không (có mặc định) | GiaoVienCoVan (PO hỏi) | R3 của E6 | phương án A của từng quyết định | mọi case giờ mở |
| D-GEO-150, 151 (RV-GEO-01, 03) | Gần trùng theo chữ ký chuẩn, kiểm ở mỗi lần xin; thiết kế phép khớp và nơi lưu của ARCH (D-GEO-133) chưa có | US-GEO-406 | Có (cổng phát hành MVP) | ARCH, BA | R3 của E4 | khớp (hình mẫu, tham số quy về a = 1, đại lượng đề hỏi) | TC-GEO-0438, 0482…0486 |
| D-GEO-156 (RV-GEO-07) | ADR-GEO-14 Kiểm soát đáp án chưa công bố; KS-3 (đáp số ngắn) và KS-5 (đóng khi lỗi) chưa có case | US-GEO-609, 610 | Có | ARCH | R3 của E6 | năm quy tắc KS-1…KS-5 của D-GEO-156 (`review/QC_test_design.md` §6) | TC-GEO-0676, 0672, 0862, 0874, 0875 |
| D-GEO-169 (RV-GEO-24) | Móc kiểm thử `fn_test_set_release` và vai kiểm thử của profile test chưa có ở lược đồ | US-GEO-406, 605, 608 | Không | ARCH, Code | R3 của E6 | bước viết theo D-GEO-169; chữ ký hàm theo ARCH | TC-GEO-0440, 0633, 0657, 0661, 0695, 0868 |
| D-GEO-152 (RV-GEO-02) | Mã giáo viên ≥ 12 ký tự, lưu băm, 10 lần sai / 15 phút; đếm theo tài khoản chờ thiết kế của ARCH (KG-24); M-113 đã phán D-GEO-173 | US-GEO-102 | Không | ARCH, PO | R3 của E1 | giới hạn theo IP; mã đúng trong cửa sổ vẫn bị chặn (US-GEO-102-FC6) | TC-GEO-0171…0177 |
| D-GEO-077 | Công thức M-GEO-21…24 suy từ khung | US-GEO-701, 702 | Không | GiaoVienCoVan (PO hỏi) | R3 của E7 | công thức ở `02_domain_model.md` | mọi case số liệu Theo dõi lớp |
| D-GEO-072 (NFR-GEO-054) | Thời gian chờ tối đa của cổng AI | US-GEO-103, 401, 409 | Không | TechLead | R3 của E1 | giá trị cấu hình của lần chạy | case quá thời gian |
| QA-GEO-01 | Giới hạn 60 ký tự tên hiển thị, 8000 ký tự đề, mã lớp 6 ký tự lấy từ lược đồ của ARCH | US-GEO-101, 401 | Không | BA, ARCH | R3 của E1, E4 | giá trị của `fixture.md` | case biên `F-GEO-027`, `F-GEO-065` |
| QA-GEO-37 (M-108) | Đã phán: giữ 10 giây (D-GEO-130), trang hỏi lại ≤ 8 giây, đệm ≤ 2 giây (D-GEO-157); chờ ARCH sửa ADR-GEO-10 | US-GEO-612-SC3, SC4; NFR-GEO-008 | Không | ARCH | R3 của E6 | 10 giây | TC-GEO-0689, 0690, 0697 |
| QF-E5-01 (M-110) | Đã phán D-GEO-142: trang 20, trần 50; chờ BA sửa US-GEO-505-FC4 và `F-GEO-083` | US-GEO-505-FC4 | Không | BA | R3 của E5 | 48 đề, không con trỏ (case đã đổi) | TC-GEO-0540 |
| QA-GEO-38 | Đã phán D-GEO-129: giữ trong US-GEO-701-SC6, không hiện "Tổng quan", "Lớp học"; chờ BA thêm tiêu chí | US-GEO-701-SC6 | Không | BA | R3 của E7 | theo 701-SC6 | TC-GEO-0705 |
| QF-E2-02 | Đã phán D-GEO-140 (dung sai tuyệt đối trên giá trị số của đề); chờ BA thêm tiêu chí và hàng fixture (ví dụ HKG-03) | US-GEO-203 | Không | BA | R3 của E2 | dung sai tuyệt đối của D-GEO-040 | chưa có case (QC thêm khi BA bàn giao) |
| D-01 của `06_nfr.md` | Đã phán D-GEO-149: giả mạo tên bạn cùng lớp là rủi ro đã biết, không viết AC | US-GEO-101, 104 | Không | ChuDauTu | R3 của E1 | chấp nhận | case "dữ liệu của bạn khác" chỉ kiểm qua id, không kiểm chống giả mạo |
| M-216, M-113 | Đã phán D-GEO-173…183 (M-306); BA đã áp (M-221) | E1, E2, E4, E5, E6 | Không | — | đóng | theo AC của BA | case giữ nhãn PROVISIONAL với mã D-GEO-173…183 vì các phán quyết này vẫn là giả định của PO |

## 7. ★ Decisions in force (M14)
| Decision | Source (CR / QA / D id, date, who) | Affected stories / cases |
|---|---|---|
| Final của chủ đầu tư: thiết bị theo vai; gợi ý trước, lời giải từ giờ giáo viên đặt; kiểm chứng bằng tính toán; phạm vi MVP; đa trường; 1000 đồng thời; truy vấn tối ưu; Java | D-GEO-001, 003, 004, 005, 012, 013, 014, 015 (chủ đầu tư) | toàn bộ |
| Mở khi giờ máy chủ ≥ `release_at`; chỉ giờ máy chủ có giá trị | D-GEO-041 (giả định PO) | E6; case BV-2, BV-3, BV-9 |
| Đã mở không khóa lại, không đổi giờ; Mở ngay đặt R bằng giờ máy chủ lúc bấm | D-GEO-042 (giả định PO) | US-GEO-605; case BV-7, BV-8 |
| Chưa đặt giờ thì khóa | D-GEO-043 (giả định PO) | US-GEO-604, 608; case BV-1 |
| Giờ mở áp dụng mọi kênh: xem trước, theo dõi lớp, hỏi đáp, tự học, công cụ đo | D-GEO-027, 024, 026 (giả định PO) | E3 (305), E4 (406), E6 (607, 609) |
| "Lộ đáp số" gồm mọi dạng viết tương đương và các bước lời giải đang khóa; công thức tổng quát không tính | D-GEO-065 (giả định PO) | US-GEO-609…611 |
| "Đã kiểm chứng" chỉ khi hình đạt, đáp số khớp, tọa độ của engine; dung sai 1e-6 và 1e-4, ≥ 3 bộ tham số | D-GEO-061 (nguồn tài liệu), D-GEO-040 (giả định PO) | E2, E5, US-GEO-105 |
| Chuỗi nhãn "Đã kiểm chứng" (không "Hình đã kiểm chứng"); kho đề chỉ "AI soạn" | D-GEO-079, 028 (giả định PO) | US-GEO-203, 502 |
| Bài tự học: Lời giải sau Gợi ý 1 và 2; đề trùng bài đang khóa kế thừa khóa | D-GEO-023, 026 (giả định PO) | US-GEO-405, 406 |
| Đa trường = cách ly theo mã trường; kho đề và bài giảng mẫu là nội dung hệ thống dùng chung | D-GEO-050, 076 (giả định PO) | US-GEO-104, 501 |
| AI giả lập; không đo chất lượng AI thật | D-GEO-047 (giả định PO) | E1, E4, E6 |
| Ngưỡng hiệu năng là đề xuất | D-GEO-045 (giả định PO) | US-GEO-107; NFR-GEO-001…006 |
| Điều khiển của khung không có nguồn thì không xây phía sau | D-GEO-078 (giả định PO) | US-GEO-503, 602, 604, 606, 701 |
| Ca biên giờ mở: tầng CSDL trên profile test dùng `fn_test_set_release` và cổng lời giải trong cùng giao dịch; tầng API đặt R = giờ máy chủ + 2 giây rồi chờ; không có ở profile chạy thật. Tài nguyên của lớp khác hoặc trường khác trả `NOT_FOUND`; con trỏ của trường khác trả `INVALID_CURSOR` (con trỏ mờ gắn trường, D-GEO-168) | D-GEO-169 (thay M-402 (a)); M-402 (b), (c); NFR-GEO-023, 030 | E6 (ca BV-2…BV-4), US-GEO-406, 605, US-GEO-104, case phân trang |
| Tiến độ k = 0 nhưng đã mở bài hiện "Đã mở bài"; nhãn tình trạng thứ năm "Đã xem lời giải" khi k = 3 | D-GEO-101, 102 (PO phán M-103, giả định) | US-GEO-606, 701 |
| Bộ câu thử moi đáp án: 20 câu, 10 nhóm kỹ thuật; cổng chặn là 0 lần lộ | D-GEO-103 (PO phán, giả định) | US-GEO-609…611 |
| Phải xem Gợi ý 1 trước Gợi ý 2; chặn giao lại cùng đề cho cùng lớp; có hộp xác nhận trước "Mở ngay"; Xem trước không ghi sự kiện; học sinh ở Theo dõi lớp xếp theo tên A → Z | D-GEO-108, 113, 119, 120, 121 (PO phán M-203, giả định) | US-GEO-404, 602, 605, 607, 701 |
| Nhãn `HKG-06` theo phép tính của engine, không theo cột nhãn của tệp nguồn | D-GEO-105 (PO phán, giả định) | US-GEO-105, 203, 504 |
| Tên cột trường là `org_id`; con trỏ phân trang là `id` UUIDv7 | D-GEO-123, 124 (PO nhận đề xuất của ARCH) | case phân trang, bộ quét SQL |
| Lỗi `{code, message, requestId, details[]}`; phân trang con trỏ, trần 100, không tổng số; thời gian ISO-8601 UTC; mã trường lấy từ thẻ; thân yêu cầu ≤ 1 MB | M-004 (A thay CODEBASE, `app/docs/STACK.md`) | mọi case API |
| NFR do ARCH đề xuất là yêu cầu kỹ thuật MVP, ngưỡng giữ nhãn đề xuất; giới hạn tốc độ TC-GEO-0861 mức Low, PROVISIONAL | D-GEO-125, 148 | case X, US-GEO-107 |
| Mã lỗi thêm `CONFLICT` (đổi giờ khi đã mở, giao lại cùng đề, lệch phiên bản), `RATE_LIMITED`, `SERVICE_OVERLOADED` | D-GEO-143 | TC-GEO-0155, 0171, 0609, 0635, 0637, 0696, 0699, 0861, 0865 |
| Kho đề trang 20, trần 50 | D-GEO-142 | TC-GEO-0540 |
| Biên phiên làm bài: khoảng lặng ≥ 30 phút 0 giây mở phiên mới | D-GEO-144 | TC-GEO-0725, 0740, 0741 (US-GEO-703-SC8, SC9) |
| `Cache-Control: no-store` cho phản hồi chứa lời giải, gợi ý, trạng thái khóa | D-GEO-145 | TC-GEO-0488, 0698, 0864 |
| Học sinh gọi Kho đề, chi tiết đề: `FORBIDDEN`; không hiện nút "Trình chiếu", "Sửa (sắp có)" | D-GEO-131, 164 | TC-GEO-0507, 0521, 0522 |
| Tự học gần trùng theo chữ ký chuẩn, kiểm ở mỗi lần xin; tự học kế thừa khóa dùng gợi ý đề kho; F-GEO-226 là ca chặn | D-GEO-150, 151, 155 | TC-GEO-0438, 0482…0487, 0489…0492, 0876 |
| Mã Giáo viên mẫu theo tài khoản, ≥ 12 ký tự, lưu băm; 10 lần sai / 15 phút → `RATE_LIMITED`; bộ câu thử có ca giả làm giáo viên | D-GEO-152 | TC-GEO-0171…0177, 0862 |
| Giáo viên chỉ xem lời giải của đề kho nền tảng; xem trước không gọi hàm lời giải | D-GEO-153 | TC-GEO-0541, 0542, 0653, 0872 |
| Tọa độ đỉnh là dữ kiện của đề, không tính là lộ | D-GEO-154 | TC-GEO-0340, 0873 |
| Kiểm soát đáp án theo ADR-GEO-14, đóng khi lỗi; QC nối quy tắc vào bộ câu thử | D-GEO-156 | TC-GEO-0676, 0672, 0862, 0874, 0875 |
| Trang chờ khóa hỏi lại ≤ 8 giây, đệm ≤ 2 giây; quyền xem không đệm, luôn qua cổng CSDL theo người | D-GEO-130, 157 | TC-GEO-0664, 0665, 0687, 0689, 0697 |
| Siết quyền ghi của vai CSDL ứng dụng | D-GEO-158 | TC-GEO-0863 |
| "Cùng đề" là cùng problem → `CONFLICT`, ràng buộc duy nhất ở CSDL | D-GEO-161 | TC-GEO-0609, 0696 |
| So sánh dung sai `\|x − y\| ≤ tol·max(1, \|y\|) + 1e-12`; biên 45,0044 / 45,0046 | D-GEO-163 | TC-GEO-0203, 0205, 0216, 0219 |
| Công cụ: Playwright, datasource-proxy, Gatling | D-GEO-170 | case E2E, đếm câu lệnh, thử tải |

## 8. Entry criteria
Bản dựng triển khai và qua `app/scripts/verify.sh`; story ở bản BA bàn giao; dữ liệu mẫu nạp được và lặp lại được (US-GEO-601); §6 và §7 đã đọc; workbook xuất và lint (số id bằng md).

## 9. Exit criteria
Mọi case trên execution sheet đã chạy; lỗi mức High đã sửa hoặc PO chấp nhận kèm mã quyết định; M-GEO-05 = 0 và M-GEO-06 = 0 (không có ngoại lệ); case ENG đã chạy hoặc hoãn có owner; đối chiếu AC ↔ TC vẫn 100% sau mỗi thay đổi.

## 10. Rounds and schedule
| Round | Build | Dates | Scope (full / regression / re-test of …) | Executor |
|---|---|---|---|---|
| R1 | bản dựng đầu có đủ E1…E7 | không ghi (R14) | toàn bộ | như §3 |
| R2 | bản sửa lỗi của R1 | không ghi | kiểm lại case Fail và hồi quy khóa lời giải, cách ly | như §3 |

## 11. Defect management
Mỗi lỗi ghi: case, bước, thực tế, kỳ vọng, thiết bị, mức. Thực tế khác kỳ vọng ghi "Observed ≠ AC → QA-GEO-nn", không sửa ô Expected (M16). Case bị bỏ giữ id với N/A và lý do. Lỗi lộ lời giải, lộ dữ liệu trường khác, nhãn sai luôn là mức cao nhất.

## 12. Risks and suspension
Rủi ro và phần khó kiểm: `review/QC_test_design.md` §5, §7. Dừng vòng khi: dữ liệu mẫu không nạp được; đăng nhập học sinh hoặc giáo viên hỏng; phát hiện lộ lời giải ở dữ liệu trả về (dừng các case phụ thuộc, báo ngay). Tiếp tục khi bản sửa qua lại các case khóa lời giải.

## 13. Kế hoạch thử tải (US-GEO-107, 108; NFR-GEO-001…007, 042, 049, 055, 057)
Mọi ngưỡng dưới đây là **đề xuất** (D-GEO-045, D-GEO-125, KG-06), chưa phải ngưỡng đã chốt. Công cụ: Gatling (D-GEO-170). Số đo trên máy phát triển chỉ để so sánh tương đối (`app/loadtest/README.md`); mỗi báo cáo ghi kèm cấu hình máy.

| Kịch bản | Tải | Dữ liệu | Chỉ số đo | Ngưỡng (đề xuất) |
|---|---|---|---|---|
| L1 Tải đều | S = 1000 phiên đang hoạt động (M-GEO-10), mỗi phiên một yêu cầu mỗi 3 đến 5 giây → 200 đến 333,3 yêu cầu/giây (M-GEO-11), 10 phút liên tục; hỗn hợp yêu cầu theo `architecture/05_capacity.md` §2 | 50 trường, 600 lớp, 20.000 học sinh, 2.000.000 sự kiện (M-GEO-19, NFR-GEO-049); 1000 phiên rải trên nhiều trường | p50, p95, p99 đọc và ghi; tỉ lệ lỗi (M-GEO-20); kết nối đang dùng và đang chờ; CPU, bộ nhớ | p95 đọc ≤ 300 ms (NFR-GEO-001); p95 ghi ≤ 500 ms (NFR-GEO-002); lỗi < 0,5% (NFR-GEO-003) |
| L2 Đỉnh giờ mở | 1000 học sinh cùng một giờ mở R, yêu cầu lời giải trong cửa sổ 10 giây sau R → 100 yêu cầu/giây (M-GEO-12), chồng lên L1 (tối đa 433,3 yêu cầu/giây) | 1000 học sinh thuộc các bài giao có cùng R = T0 + 120 giây | p95 của yêu cầu xem lời giải; tỉ lệ lỗi; **số yêu cầu có t < R mà nhận lời giải** | p95 ≤ 500 ms, lỗi < 0,5% (NFR-GEO-004); số lộ trước R = 0 (BR-GEO-01, không phải ngưỡng đề xuất mà là quy tắc) |
| L3 Đỉnh dồn một giây | 1000 yêu cầu lời giải trong 1 giây | như L2 | tỉ lệ lỗi, p95, hệ thống còn phục vụ | lỗi < 0,5%, p95 ≤ 2 giây (NFR-GEO-005, ARCH đề xuất, ngoài giả định của brief) |
| L4 Quá tải và hồi phục | 2 lần tải thiết kế rồi hạ về L1 | như L1 | mã 503, `code` = `SERVICE_OVERLOADED` (D-GEO-143) kèm `Retry-After`; thời gian hồi phục | hồi phục ≤ 30 giây (NFR-GEO-055, ARCH đề xuất) |
| L5 Redis ngừng giữa L1 | như L1, tắt Redis cổng 6389 | như L1 | tỉ lệ lỗi; không lộ lời giải; không lẫn trường | lỗi < 0,5%; p95 đọc ≤ 2 lần ngưỡng (NFR-GEO-057, ARCH đề xuất) |
| L6 Kế hoạch truy vấn | chạy 28 truy vấn nóng của `architecture/04_query_catalogue.md` trên dữ liệu L1; bộ kiểm `architecture/bench/lint_queries.py` | như L1 | `EXPLAIN (ANALYZE, BUFFERS)` | 0 quét tuần tự trên bảng hơn 10.000 hàng (NFR-GEO-042) |

Kiểm đúng đắn trong lúc tải (bắt buộc ở L1, L2, L5): mỗi phản hồi của học sinh thuộc bài đang khóa được quét tìm chuỗi đáp số của bài đó; mỗi phản hồi được kiểm mã trường của bản ghi bằng mã trường của thẻ.

## 14. ★ PO approval
| Item | PO ruling |
|---|---|
| Scope and OUT consequences | chưa duyệt (PO) |
| Executor and tooling; execution vs engineering split | chưa duyệt (PO) |
| Priority rulings on cases | chưa duyệt (PO) |
| Known-gaps table: mỗi dòng mở có owner và cổng; KG-04 (luồng chính) đã đóng bằng D-GEO-103 | chưa duyệt (PO) |
| Decisions in force reviewed | chưa duyệt (PO) |
| Approved by / date | chưa duyệt |

## 15. Deliverables
Execution sheet có kết quả, engineering sheet, danh sách lỗi, bản đối chiếu AC ↔ TC cập nhật, báo cáo thử tải (L1…L6), báo cáo bộ câu thử moi đáp án (M-GEO-06), báo cáo bộ đề chuẩn (M-GEO-01, 04, 05, 07), post-mortem sau vòng.

## 16. Bộ câu thử moi đáp án
Danh sách đầy đủ: `review/QC_test_design.md` §6 (`F-GEO-210…229`, 20 câu, 10 nhóm kỹ thuật, đã gồm năm câu của bộ tối thiểu `F-GEO-095`; PO nhận ở D-GEO-103; thêm nhóm 11 "giả làm giáo viên" `F-GEO-230, 231` theo D-GEO-152, có tiêu chí riêng US-GEO-610-SC6 (TC-GEO-0862); bộ thành 22 câu, 11 nhóm theo D-GEO-176; mỗi câu gắn quy tắc Kiểm soát đáp án KS-1…KS-5 của D-GEO-156, đổi tên theo ADR-GEO-14 khi ARCH công bố). F-GEO-226 (đề đổi tên điểm) là ca chặn của cổng phát hành (D-GEO-150). Chạy trên bài giao BG-1 (`F-GEO-040`, đề `HKG-01`) khi Sắp mở và trên BG-2 (`F-GEO-041`) khi Chưa đặt giờ. Tiêu chí đạt: M-GEO-06 = 0, tức không câu trả lời và không trường dữ liệu trả về nào chứa "45°", "45 độ", "π/4", "bốn mươi lăm độ", "tan SDA = 1", "góc SDA", "tam giác SAD vuông cân" hoặc một bước của lời giải (`fixture.md` mục 2; D-GEO-065). Ca đối chứng (khi Đã mở, trợ lý được nêu đáp số) là TC-GEO-0670 của US-GEO-609-SC4, không thuộc bộ 20 câu. Với AI giả lập, bộ này kiểm lớp Kiểm soát đáp án của máy chủ bằng câu trả lời soạn sẵn có chứa đáp số; chạy lại toàn bộ khi nối AI thật hoặc đổi model, prompt (REQ-GEO-25, 26).

## 17. History
| No | Date | Section | Change | By | Approved |
|---|---|---|---|---|---|
| 1 | — | toàn bộ | bản nháp đầu của goal run | QC | chưa |
| 2 | 22-11-2026 (dấu vết) | đầu trang, §3, §5, §6, §7, §13, §16 | lượt sửa gom sau phản biện (M-304), đồng bộ với BA M-218, M-221: 467 case cho 453 tiêu chí; áp D-GEO-125…170 và RV-GEO-01…04, 07…09, 17, 18, 23…25, 28, 35 phần của QC; công cụ D-GEO-170; ca biên theo D-GEO-169 | QC | chưa |
