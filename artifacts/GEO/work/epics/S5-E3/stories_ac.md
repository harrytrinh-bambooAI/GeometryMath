Phiên bản: v0.1 · 2026-11-28
# S5-E3 — Kho tri thức của giáo viên, hỏi đáp của học sinh theo kho, kiểm soát đáp án đầy đủ: story Dev-Ready và tiêu chí chấp nhận

> Chủ sở hữu: BA (goal run, PO quyết cao nhất). Nguồn: `PRODUCT-MASTER.md` (22/11/2026) D1 dòng 5, D2 D-08 ("bản đầy đủ ở chặng 5"), C4 "Tri thức do giáo viên kiểm soát", C5 "Tri thức…", "Kiểm soát đáp án", "Hỏi đáp theo giáo trình"; `STAGES-AFTER-MVP.md` Chặng 5 §5 luồng C, §6 C5-07, §7, §8 (`KnowledgeItem`, `KnowledgeVersion`, `KnowledgeGrant`, `AIInteraction`, `AIPolicyDecision`); PO-33, PO-34 (Final), PO-37, PO-43, PO-44, PO-49; ADR-GEO-14 (KS-1…KS-5, PO-79); NFR-GEO-020…022, 071; mã hiện có: `knowledge` (LectureCatalog), task `qa` trong `AnthropicPrompts.java`; `materials/PLAN-kho-bai-giang.md` (kho 3 tầng, mã kiến thức cố định).
> **Trạng thái: ready for R3 (goal run)**; mức epic **P0**. Giả định đang dùng và câu hỏi cho owner ở `work/s5/questions-s5.md`; tự soát ở `work/s5/review-s5.md`.

**Mục tiêu epic.** Giáo viên tự quản lý tài liệu có phiên bản và trạng thái, gắn cho lớp hoặc bài; AI phía học sinh chỉ trả lời từ tài liệu Đang dùng được gắn, luôn ghi nguồn, tuân mức trợ giúp và lịch công bố, mọi quyết định cho phép hay chặn được ghi và giáo viên xem lại được.

**Điều chỉnh khung so với brief.** Tách "kiểm soát đáp án đầy đủ" thành hai story (mức trợ giúp và lịch công bố kiểm trước khi gọi AI; bộ kiểm đầu ra KS mở rộng và nhật ký quyết định) để mỗi story ≤ 3 ngày; thêm story giáo viên xem lịch sử hỏi đáp và báo chặn sai (luồng C bước 3, rủi ro "chặn nhầm" STAGES §10).

**Phạm vi PO-43.** Ngoại lệ "bài giảng khái niệm AI viết chưa duyệt vẫn dùng làm nguồn" chỉ áp cho bài giảng mẫu của hệ thống (MVP). Kho tri thức do giáo viên tự quản lý theo STAGES §7: AI học sinh chỉ dùng tài liệu "Đang dùng" (QS5-10).

**Ước lượng epic:** dev 15 ngày · test 4,5 ngày · tổng 19,5 ngày công (6 story; bảng chung ở `work/s5/estimate-s5.md`).

## 0. Hướng dẫn đọc, quy ước và dữ liệu mẫu chung (giống nhau ở mọi epic S5)

**Cách đọc.** Mỗi mục `## US-S5En-nn — …` là một story: câu story, dòng **Ước lượng** (ngày công dev và test, test ≤ 30 % dev), nguồn, góc nhìn người vận hành, phụ thuộc, quy tắc nghiệp vụ (BR), tiêu chí chấp nhận (AC). Mỗi AC nói **một** hành vi quan sát được, dạng Given / When / Then, giá trị cụ thể lấy từ dữ liệu mẫu bên dưới. `SC` = kịch bản thành công, `FC` = lỗi, biên hoặc ngoại lệ.

**Quy ước chung cho mọi AC chặng 5** (kế thừa chặng 3, `epics/S3-E1/stories_ac.md` mục 0; PO-61…66):
- Mốc giờ lấy từ giờ máy chủ (T0); API dùng ISO-8601 UTC; màn hình hiện `HH:mm, dd/MM/yyyy` giờ Việt Nam.
- Lỗi máy chủ có dạng `{code, message, requestId, details[]}`; `code` ổn định: VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN (sai vai), NOT_FOUND (đối tượng ngoài quan hệ; không lộ đối tượng có tồn tại), CONFLICT, RATE_LIMITED (kèm `Retry-After`), AI_UNAVAILABLE (cổng AI hết giờ hoặc lỗi sau một lần gọi lại; PO-78).
- Quyền theo quan hệ (US-S3E1-05): giáo viên chỉ thấy và tác động lên lớp mình tạo, học sinh Đang học của lớp đó và tài liệu, rubric, bài nộp thuộc các lớp đó; học sinh chỉ thấy bài nộp, điểm và hỏi đáp của chính mình.
- **AI chỉ đề xuất, giáo viên quyết định** (PO-03, C2 nguyên tắc 4): không có đường nào chuyển nội dung AI thành điểm chính thức, tài liệu Đang dùng, đề trong kho, giáo án đã duyệt hay thông báo đã gửi mà thiếu thao tác của giáo viên chủ lớp. Mọi bản nháp AI mang nhãn người "AI soạn" cho tới khi giáo viên duyệt (PO-02, PO-30).
- **Đáp số do engine** (C2 nguyên tắc 13): AI không bao giờ là nguồn đáp số. Đáp số của câu thuộc kho lấy từ kho (`bo-mau`, `bai-tap`, ngân hàng `de-thi`); câu mới đưa sang engine; không tính được thì nhãn máy "Cần xem lại".
- Mọi lời gọi AI đi qua module `aigateway` với một task có tên (`lesson_plan`, `lesson_patch`, `exam_draft`, `kb_qa`, `grade`, `grade_ocr`, `notice_draft`, `class_errors`); mỗi lượt ghi `AiCallLog` gồm task, model, phiên bản prompt, token vào/ra, kết quả kiểm máy chủ. Nội dung gửi nhà cung cấp không chứa tên hiển thị, mã người dùng, mã lớp (NFR-GEO-071).
- AI giữ giả lập trong CI (D-GEO-047): `MockAiProvider` trả câu trả lời cố định theo dữ liệu mẫu `F-S5-090…099`; không AC nào kỳ vọng chất lượng câu chữ AI thật. Chất lượng AI thật đo bằng bench ngoài CI theo `materials/baseline/KPI.md` v1.0.
- Phản hồi chứa bài nộp, điểm, lời giải hoặc trạng thái khóa mang `Cache-Control: no-store` (D-GEO-145).
- Thiết bị: giáo viên dùng web trên máy tính; học sinh iPhone để chụp bài, iPad để làm và xem (PWA, PO-65 Q9).
- AC mang mã `QS5-nn` dựa trên giả định của PO trong goal run; câu hỏi và giả định đang dùng ở `work/s5/questions-s5.md`.

| Id / viết tắt | Nghĩa | Ở đâu |
|---|---|---|
| `US-S5En-nn` · `-SCn` / `-FCn` | story · tiêu chí thành công / thất bại thứ n | tệp này |
| `BR-S5En-nn` | quy tắc nghiệp vụ (nguồn dẫn kèm) | tệp này |
| `TC-S5En-nn` | test case của epic | `test_cases.md` cùng thư mục |
| `F-S5-nnn` | hàng dữ liệu mẫu chung chặng 5 | bảng dưới |
| `MSG-S5-nn` | thông điệp hiện cho người dùng | bảng "Thông điệp" của từng epic |
| `QS5-nn` | câu hỏi cho owner kèm giả định đang dùng | `work/s5/questions-s5.md` |
| `PO-nn` · `D-GEO-nnn` · `NFR-GEO-nnn` · `KS-n` | quyết định gốc · quyết định MVP · yêu cầu phi chức năng · luật kiểm soát đáp án ADR-GEO-14 | `PO-DECISIONS.md` · `work/03_decisions.md` · `work/architecture/06_nfr.md` · `work/architecture/01_adr.md` |
| `C5-nn` | mã màn hình chặng 5 | `STAGES-AFTER-MVP.md` Chặng 5 §6 |
| Luật A1–A10 · trường B/C | luật cố định và trường form của prompt soạn giáo án V8 | `prompt-lab/giao-an/prompt-final.md` v1.1, `truong.md` v1.1 |
| KPI §n | trọng số chấm, luật chặn, ngưỡng phát hành | `materials/baseline/KPI.md` v1.0 |
| Nhãn hai trục | máy "Đã kiểm chứng" / "Cần xem lại"; người "AI soạn" / "Giáo viên đã duyệt" | PO-02 |

### Dữ liệu mẫu chung của chặng 5 (giờ Việt Nam)

| Mã | Nội dung |
|---|---|
| `F-S5-001` | Cô Lan · `GV-7K2M-9QPA` · chủ lớp 11A1 (`HK-4P7Q`) và 11A2 (`HK-8M3C`) |
| `F-S5-002` | Thầy Khôi · `GV-4D8R-2WNX` · chủ lớp 10B (`HK-5T9X`); không có quan hệ với 11A1 |
| `F-S5-010` | Lớp 11A1 · 8 em Đang học: Bình An `HS-4P9T-37WD`, Chi Mai `HS-8L2K-77AB`, Đức Huy `HS-3M7Q-85XN`, Gia Bảo `HS-9D4W-67CE`, Hà My `HS-2X6R-48PL`, Khánh Linh `HS-7T7B-98QZ`, Minh Quân `HS-5N8C-24TR`, Ngọc Anh `HS-6V3F-78HM`; Văn Tú `HS-3H6J-72KP` Đã rời |
| `F-S5-011` | Lớp 11A2 · Quốc Bảo `HS-3C9D-77AA` Đang học |
| `F-S5-020` | Yêu cầu giáo án: Tiết PPCT "Chương VII · Bài 26. Khoảng cách · Tiết 1/2"; Đối tượng Khá; Phong cách Truyền thống (chọn sẵn); Kiến thức trọng tâm tích sẵn `KT11-C07-DN-010`, `KT11-C07-CT-003`; Số tiết tự điền 1; Phiếu học tập Bật |
| `F-S5-021` | Giáo án nháp `GA-0001` của cô Lan từ F-S5-020: HĐ1 Mở đầu 5', HĐ2 Hình thành 20', HĐ3 Luyện tập 15', HĐ4 Vận dụng 5' (Σ 45'); PHT1 3 câu nguồn kho `BM-007`, `BM-008`, `BM-009`; Mục tiêu và Thiết bị đã khoá |
| `F-S5-030` | Ma trận `KT15-HHKG11` (15 phút; 6 TN4 × 1,0 + 2 trả lời ngắn × 2,0; 3 ô xoay vòng) |
| `F-S5-031` | Ma trận `TN-THPT-764` (90 phút; 12 TN4 × 0,25; 4 đúng/sai; 6 trả lời ngắn × 0,5) |
| `F-S5-032` | Ngân hàng `de-thi/ngan-hang` 1 774 câu, 110 mẫu câu; seed 2026 |
| `F-S5-040` | Kho tri thức cô Lan: `TL-01` "Ghi chú Khoảng cách 11A1" v2 Đang dùng, gắn lớp 11A1 (v1 Lưu trữ); `TL-02` "Phiếu ôn chương VII" v1 Chờ duyệt; `TL-03` "Bài giảng AI · Góc giữa hai mặt phẳng" v1 Nháp nhãn "AI soạn" |
| `F-S5-041` | Tài liệu của thầy Khôi `TL-09` "Hệ thức lượng 10B" v1 Đang dùng, gắn 10B |
| `F-S5-050` | Đề `D-KC-01`: "Cho hình chóp S.ABCD đáy là hình vuông cạnh a, SA ⊥ (ABCD), SA = a√2. Tính khoảng cách từ A đến mặt phẳng (SBC)." Đáp số engine `a√6/3` (≈ 0,8165 tại a = 1) |
| `F-S5-051` | Rubric `RB-01` "Tính toán – khoảng cách" v1 theo KPI §1 quy về thang 10: Hướng 2 · Các bước 4 · Kết quả 2 · Hình 1 · Trình bày 1 (Σ 10) |
| `F-S5-052` | Bài có chấm `BC-01` "Kiểm tra 15 phút – Khoảng cách" giao 11A1, đề F-S5-050, rubric RB-01 v1, hạn nộp T6 11/12/2026 20:00, nộp muộn: cho phép có nhãn, công bố điểm và lời giải: "khi giáo viên bấm Trả bài" |
| `F-S5-053` | Bài nộp `BN-01` của Bình An: 2 trang rõ, đáp số viết `a√6/3` |
| `F-S5-054` | Bài nộp `BN-02` của Chi Mai: 3 trang rõ, đáp số viết `a√3/3` (sai, lỗi `sai-tinh-toan` ở bước tính SB) |
| `F-S5-055` | Bài nộp `BN-03` của Đức Huy: trang 2 mờ (điểm độ rõ 0,31 < 0,5) |
| `F-S5-057` | Bài nộp `BN-07` của Minh Quân: 2 trang, viết đúng `a√6/3` nhưng thiếu bước dựng AH ⊥ SB |
| `F-S5-056` | Gia Bảo chưa nộp; Hà My nộp `BN-05` lúc 11/12/2026 20:07 (muộn 7 phút) |
| `F-S5-090` | Giả lập `grade` cho BN-01: Hướng 2 · Bước 4 · Kết quả 2 · Hình 1 · Trình bày 1 = 10; tin cậy 0,86; mỗi tiêu chí có lý do và vùng ảnh `{trang, x, y, w, h}` |
| `F-S5-091` | Giả lập `grade` cho BN-02: Hướng 2 · Bước 3 · Kết quả 0 · Hình 1 · Trình bày 1 = 7; tin cậy 0,74; lỗi `sai-tinh-toan` ở bước 3 |
| `F-S5-092` | Giả lập `grade` cho BN-03: `{"kind":"refuse","reason":"Trang 2 mờ, không đọc được bước 2–3"}` |
| `F-S5-093` | Giả lập `lesson_plan` cho F-S5-020 trả giáo án hợp lệ F-S5-021; biến thể `F-S5-093b` trả tổng 50' (phạm A5) |
| `F-S5-094` | Giả lập `kb_qa`: câu trả lời dẫn `TL-01` v2 mục "Hệ thức 1/AH²"; biến thể `F-S5-094b` có chuỗi "a√6/3" (để thử kiểm soát đáp án) |
| `F-S5-095` | Giả lập `grade_ocr` cho BN-01: bản đọc chữ 2 trang, 2 đoạn tô "chưa chắc" (tin cậy < 0,6) |


### Thông điệp dùng trong tệp này (đề xuất của BA)

| Mã | Loại | Câu chữ | Mã lỗi API |
|---|---|---|---|
| MSG-S5-20 | Page state | "Thầy cô chưa cung cấp tài liệu cho phần này. Em hỏi thầy cô trên lớp nhé." | — |
| MSG-S5-21 | Page state | "Thầy cô đang tắt Hỏi đáp cho bài này. Em vẫn làm bài và xem gợi ý như thường." | — |
| MSG-S5-22 | Inline | "Tệp tối đa 10 MB, định dạng docx, pdf hoặc md." | VALIDATION_FAILED |
| MSG-S5-23 | Toast | "Phiên bản này đã được dùng, sửa sẽ tạo phiên bản mới." | — |
| MSG-S5-24 | Page state | "Câu trả lời chưa an toàn để hiện. Em thử hỏi cách khác hoặc xem gợi ý." | — |
| MSG-S5-25 | Toast | "Đã ghi báo cáo. Cảm ơn thầy cô." | — |

---

## US-S5E3-01 — Kho tri thức: thêm tài liệu, phiên bản bất biến, trạng thái Nháp → Chờ duyệt → Đang dùng → Lưu trữ
- **Story:** Là giáo viên, tôi muốn đưa ghi chú, phiếu và bài giảng của tôi vào một kho có phiên bản, để AI của học sinh chỉ nói điều tôi đã duyệt và tôi biết bản nào đang được dùng. · Loại Logic · P0
- **Ước lượng: dev 3 ngày · test 0,9 ngày** — gồm 3 bảng mới, tách tệp thành mục để truy xuất, máy trạng thái và kiểm trùng.
- **Nguồn:** C5 "Tri thức do giáo viên kiểm soát" (chủ sở hữu, trạng thái, version, phạm vi); STAGES Ch5 §6 C5-07, §8; PO-30, PO-47 (đối chiếu trùng)
- **Góc nhìn người vận hành:** cô Lan tải 5–10 tài liệu đầu học kỳ, sửa dần; ca đáng kể: sửa tài liệu đang dùng, tải tệp 30 MB, tải lại bản trùng, giáo án đã duyệt ở S5-E1 muốn đưa vào kho.
- **Phụ thuộc:** module `knowledge`; US-S5E1-05 (giáo án đã duyệt làm nguồn); S2-E4 (tách mục từ tệp, nếu đã có)

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S5E3-01 | Tài liệu (`KnowledgeItem`) có chủ sở hữu là một giáo viên; mỗi lần lưu nội dung tạo `KnowledgeVersion` mới, phiên bản đã từng Đang dùng là bất biến. | STAGES §7, §8; C5 |
| BR-S5E3-02 | Trạng thái: Nháp → Chờ duyệt → Đang dùng → Lưu trữ; chỉ chủ sở hữu chuyển; tại một thời điểm mỗi tài liệu có tối đa một phiên bản Đang dùng; đưa phiên bản mới lên Đang dùng thì bản cũ sang Lưu trữ. | STAGES §8 |
| BR-S5E3-03 | Nguồn nhập: soạn tay (Markdown), tải tệp docx, pdf, md ≤ 10 MB, hoặc chọn giáo án đã duyệt (US-S5E1-05); nội dung tách thành mục có tiêu đề để truy xuất và dẫn nguồn. | PO-27; QS5-09 |
| BR-S5E3-04 | Tải tệp trùng nội dung với một phiên bản đã có (băm sau chuẩn hoá) không tạo bản mới, báo "đã có trong kho". | PO-47 |
| BR-S5E3-05 | Bản nháp do AI soạn (ví dụ `TL-03`) mang nhãn "AI soạn"; chỉ lên Đang dùng sau khi giáo viên duyệt. | PO-02, PO-30; QS5-10 |

- **DoR:** Ready for R3 (goal run, PO quyết); giả định ghi ở `work/s5/questions-s5.md`.

### Tiêu chí chấp nhận

**US-S5E3-01-SC1 — Tải tệp docx tạo tài liệu Nháp tách mục** · Nguồn: BR-S5E3-01, 03
- Given: cô Lan đã đăng nhập
- When: gửi `POST /api/knowledge` tệp "Phieu-on-chuong-VII.docx" 240 KB
- Then: tài liệu mới trạng thái Nháp, phiên bản 1, chủ sở hữu cô Lan; danh sách mục có ít nhất 1 mục có tiêu đề; tệp gốc giữ nguyên, tải lại được

**US-S5E3-01-SC2 — Duyệt đưa phiên bản mới lên Đang dùng, bản cũ sang Lưu trữ** · Nguồn: BR-S5E3-02
- Given: `TL-01` v2 Đang dùng; cô Lan vừa lưu v3 rồi gửi Chờ duyệt
- When: gửi `POST /api/knowledge/TL-01/versions/3/activate`
- Then: v3 Đang dùng; v2 Lưu trữ; chỉ một phiên bản Đang dùng

**US-S5E3-01-SC3 — Giáo án đã duyệt đưa vào kho** · Nguồn: BR-S5E3-03
- Given: giáo án F-S5-021 đã duyệt
- When: gửi `POST /api/knowledge` với `fromLessonPlan` = `GA-0001`
- Then: tài liệu mới Nháp, nội dung tách theo 4 mục khung 5512; nguồn ghi "Giáo án GA-0001 v2"

**US-S5E3-01-FC1 — Sửa phiên bản đã dùng tạo phiên bản mới, không ghi đè** · Nguồn: BR-S5E3-01
- Given: `TL-01` v2 Đang dùng
- When: gửi `PUT /api/knowledge/TL-01/versions/2` với nội dung sửa
- Then: trả 201 kèm phiên bản 3 trạng thái Nháp và MSG-S5-23; nội dung v2 không đổi (so băm)

**US-S5E3-01-FC2 — Tệp 11 MB hoặc định dạng xlsx bị từ chối** · Nguồn: BR-S5E3-03
- Given: cô Lan đã đăng nhập
- When: tải tệp pdf 11 MB; tải tệp xlsx 50 KB
- Then: cả hai VALIDATION_FAILED, MSG-S5-22

**US-S5E3-01-FC3 — Tải lại tệp trùng không tạo bản mới** · Nguồn: BR-S5E3-04
- Given: tệp ở SC1 đã có trong kho
- When: tải lại đúng tệp đó
- Then: trả 200 kèm id tài liệu đã có và cờ `duplicate = true`; số phiên bản không đổi

**US-S5E3-01-FC4 — Giáo viên khác và học sinh không đọc được kho của cô Lan** · Nguồn: quy ước chung
- Given: kho F-S5-040
- When: thầy Khôi gọi `GET /api/knowledge/TL-01`; Bình An gọi cùng đường dẫn
- Then: thầy Khôi NOT_FOUND; Bình An FORBIDDEN (học sinh không đọc kho trực tiếp, chỉ thấy trích dẫn trong câu trả lời)

---

## US-S5E3-02 — Gắn tài liệu cho lớp hoặc bài, thời hạn hiệu lực
- **Story:** Là giáo viên, tôi muốn gắn tài liệu cho cả lớp hoặc cho riêng một bài, để AI dùng đúng tài liệu ở đúng chỗ. · Loại Logic · P0
- **Ước lượng: dev 2 ngày · test 0,6 ngày**
- **Nguồn:** C4 "cấp quyền theo lớp"; STAGES §5 luồng C bước 2 ("tài liệu đã duyệt gắn với bài"), §8 `KnowledgeGrant`
- **Góc nhìn người vận hành:** cô Lan gắn `TL-01` cho 11A1 cả học kỳ, gắn phiếu riêng cho bài kiểm tra; ca đáng kể: gắn tài liệu Nháp, gắn cho lớp người khác, hết hạn hiệu lực.
- **Phụ thuộc:** US-S5E3-01; S3-E1 (lớp), S3-E2 (bộ bài)

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S5E3-06 | `KnowledgeGrant` gắn một tài liệu (không phải một phiên bản) cho một lớp hoặc một bài của lớp mình, có `tu_ngay`, `den_ngay` tuỳ chọn; AI dùng phiên bản Đang dùng tại thời điểm hỏi. | STAGES §8; QS5-11 |
| BR-S5E3-07 | Chỉ gắn được tài liệu có phiên bản Đang dùng; tài liệu bị Lưu trữ toàn bộ thì các gắn kết hết hiệu lực ngay. | STAGES §7 |

- **DoR:** Ready for R3 (goal run, PO quyết); giả định ghi ở `work/s5/questions-s5.md`.

### Tiêu chí chấp nhận

**US-S5E3-02-SC1 — Gắn TL-01 cho lớp 11A1** · Nguồn: BR-S5E3-06
- Given: kho F-S5-040
- When: gửi `POST /api/knowledge/TL-01/grants` với `classId` 11A1
- Then: gắn kết hiệu lực; `GET /api/classes/11A1/knowledge` trả `TL-01` kèm phiên bản Đang dùng v2

**US-S5E3-02-SC2 — Gắn kết có hạn tự hết hiệu lực** · Nguồn: BR-S5E3-06
- Given: gắn `TL-01` cho bài BC-01 với `den_ngay` = T0 + 1 ngày
- When: gọi danh sách tài liệu của bài tại T0 và tại T0 + 1 ngày + 1 giây
- Then: lần 1 có `TL-01`; lần 2 không có

**US-S5E3-02-FC1 — Không gắn được tài liệu chưa Đang dùng** · Nguồn: BR-S5E3-07
- Given: `TL-02` Chờ duyệt
- When: gửi gắn `TL-02` cho 11A1
- Then: CONFLICT; không có gắn kết mới

**US-S5E3-02-FC2 — Không gắn được cho lớp của người khác** · Nguồn: quy ước chung
- Given: lớp 10B của thầy Khôi
- When: cô Lan gắn `TL-01` cho 10B
- Then: NOT_FOUND

---

## US-S5E3-03 — Hỏi đáp của học sinh dựa trên kho đã gắn, luôn kèm nguồn; không có tài liệu thì nói rõ
- **Story:** Là học sinh, tôi muốn hỏi AI khi làm bài và nhận câu trả lời lấy từ tài liệu thầy cô đã duyệt, có ghi rõ lấy từ đâu, để tin được và mở lại tài liệu đó. · Loại Logic · P0
- **Ước lượng: dev 3 ngày · test 0,9 ngày** — task `kb_qa` mới (mở rộng `qa`), bộ truy xuất mục tài liệu bằng tìm kiếm toàn văn PostgreSQL (QS5-09), kiểm nguồn hợp lệ.
- **Nguồn:** PO-33, PO-34 (Final); PO-37, PO-44; STAGES Ch5 §5 luồng C; C5 "Hỏi đáp theo giáo trình"; task `qa` hiện có (QA_HEAD: `kind`, `reply`, `source`)
- **Góc nhìn người vận hành:** 8 em 11A1 hỏi rải rác trong buổi tối trước hạn; ca đáng kể: lớp chưa có tài liệu nào, câu hỏi ngoài môn, AI dẫn nguồn không có trong danh sách.
- **Phụ thuộc:** US-S5E3-01, 02; US-S5E3-04 (kiểm trước khi gọi); US-S5E3-05 (kiểm đầu ra)

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S5E3-08 | Máy chủ chọn tối đa 5 mục từ các tài liệu được gắn cho lớp hoặc bài đang làm (phiên bản Đang dùng) cộng bài giảng mẫu của hệ thống theo PO-43; chỉ các mục này được gửi cho AI. | STAGES §7; QS5-09 |
| BR-S5E3-09 | Câu trả lời `concept`, `hint`, `explain_step` phải có `source` là một mục trong danh sách đã gửi; nguồn không thuộc danh sách thì bỏ câu trả lời và trả MSG-S5-20. Màn hiện nguồn dạng "Theo: Ghi chú Khoảng cách 11A1 · v2 · mục Hệ thức 1/AH²" bấm mở được mục đó. | PO-33; PO-34 |
| BR-S5E3-10 | Không có mục nào khớp (điểm truy xuất dưới ngưỡng) thì không gọi AI, trả MSG-S5-20. | STAGES §5 luồng C ngoại lệ |
| BR-S5E3-11 | Nội dung gửi AI không có tên hiển thị, mã người dùng, mã lớp; câu hỏi của học sinh nằm trong thẻ dữ liệu. | NFR-GEO-071 |

- **DoR:** Ready for R3 (goal run, PO quyết); giả định ghi ở `work/s5/questions-s5.md`.

### Tiêu chí chấp nhận

**US-S5E3-03-SC1 — Câu hỏi khái niệm nhận trả lời kèm nguồn TL-01 v2** · Nguồn: BR-S5E3-08, 09
- Given: `TL-01` v2 gắn 11A1; bài BC-01 đang làm; giả lập F-S5-094
- When: Bình An gửi `POST /api/qa` "Công thức tính khoảng cách từ A đến (SBC) khi SA vuông góc đáy là gì?"
- Then: `kind` = `concept`; `source` trỏ `TL-01` v2 mục "Hệ thức 1/AH²"; thân phản hồi có tiêu đề tài liệu, phiên bản, mục; không có nội dung mục nào khác; dòng `AIInteraction` lưu câu hỏi, câu trả lời, nguồn

**US-S5E3-03-SC2 — Bấm nguồn mở đúng mục tài liệu** · Nguồn: BR-S5E3-09
- Given: câu trả lời ở SC1 trên iPad
- When: bấm "Theo: Ghi chú Khoảng cách 11A1 · v2"
- Then: mở khung đọc mục "Hệ thức 1/AH²" của v2; không thấy các mục khác của tài liệu ngoài mục được dẫn

**US-S5E3-03-FC1 — Lớp chưa gắn tài liệu nào thì không gọi AI** · Nguồn: BR-S5E3-10
- Given: lớp 11A2 không có gắn kết nào và không có bài giảng mẫu khớp
- When: Quốc Bảo hỏi "Mặt phẳng trung trực là gì?"
- Then: trả MSG-S5-20; `AiCallLog` không có dòng mới; `AIPolicyDecision` ghi `no_source`

**US-S5E3-03-FC2 — AI dẫn nguồn không có trong danh sách thì câu trả lời bị bỏ** · Nguồn: BR-S5E3-09
- Given: giả lập trả `source` = `TL-09` (của thầy Khôi, không được gửi)
- When: Bình An hỏi như SC1
- Then: trả MSG-S5-20; không có chữ nào của câu trả lời giả lập trong phản hồi; `AIPolicyDecision` ghi `invalid_source`

**US-S5E3-03-FC3 — Học sinh lớp khác không dùng được tài liệu của 11A1** · Nguồn: BR-S5E3-08
- Given: `TL-01` gắn 11A1; Mi Na ở 10B
- When: Mi Na hỏi đúng câu của SC1
- Then: danh sách mục gửi AI không có mục nào của `TL-01` (soát nhật ký yêu cầu ở giả lập); câu trả lời không dẫn `TL-01`

**US-S5E3-03-FC4 — Nội dung gửi AI không chứa danh tính** · Nguồn: BR-S5E3-11
- Given: như SC1
- When: Bình An hỏi như SC1
- Then: yêu cầu ở nhà cung cấp giả lập không chứa "Bình An", `HS-4P9T-37WD`, `HK-4P7Q`

---

## US-S5E3-04 — Mức trợ giúp theo bài và lịch công bố đáp số, kiểm ở máy chủ trước khi gọi AI
- **Story:** Là giáo viên, tôi muốn đặt cho mỗi bài mức trợ giúp của AI và giờ công bố đáp số, để AI không thành đường tắt trong giờ kiểm tra. · Loại Logic · P0
- **Ước lượng: dev 2 ngày · test 0,6 ngày**
- **Nguồn:** C5 "Kiểm soát đáp án" (kiểm danh tính, lớp, bài, trạng thái, lịch công bố); STAGES §4 P0, §6 C5-01; PO-22, PO-37; US-S3E5-04 (công tắc AI lớp); PO-85
- **Góc nhìn người vận hành:** cô Lan đặt "Tắt" cho bài kiểm tra 15 phút, "Chỉ gợi ý" cho bài luyện; ca đáng kể: đổi mức giữa giờ, lớp đã tắt AI, giờ công bố đã qua.
- **Phụ thuộc:** US-S5E3-03; S3-E2 (bài trong bộ); US-S3E5-04

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S5E3-12 | Mức trợ giúp mỗi bài: Tắt · Chỉ gợi ý (`hint`, `explain_step`) · Gợi ý và khái niệm (thêm `concept`); mặc định Gợi ý và khái niệm cho bài luyện, Tắt cho bài có chấm (QS5-12). | STAGES §5 C bước 1; QS5-12 |
| BR-S5E3-13 | Máy chủ kiểm theo thứ tự trước khi gọi AI: công tắc AI của lớp (US-S3E5-04) → học sinh Đang học → bài thuộc lớp → mức trợ giúp → trạng thái khoá (giờ công bố, `fn_get_solution`). Bị chặn ở bước nào thì không gọi AI và ghi `AIPolicyDecision` với mã bước. | C5; ADR-GEO-14 KS-4 (chặn sớm) |
| BR-S5E3-14 | Sau giờ công bố đáp số, mức trợ giúp vẫn giữ nguyên nhưng bộ kiểm đầu ra không chặn đáp số của chính bài đó. | PO-22, PO-37 |

- **DoR:** Ready for R3 (goal run, PO quyết); giả định ghi ở `work/s5/questions-s5.md`.

### Tiêu chí chấp nhận

**US-S5E3-04-SC1 — Bài có mức "Tắt" thì Hỏi đáp báo đang tắt, không gọi AI** · Nguồn: BR-S5E3-12, 13
- Given: bài BC-01 mức Tắt
- When: Bình An gửi câu hỏi trên BC-01
- Then: trả MSG-S5-21; không có dòng `AiCallLog` mới; `AIPolicyDecision` = `blocked`, bước `help_level`

**US-S5E3-04-SC2 — Mức "Chỉ gợi ý" không trả lời khái niệm** · Nguồn: BR-S5E3-12
- Given: bài luyện mức Chỉ gợi ý; giả lập trả `kind` = `concept`
- When: Bình An hỏi "Định nghĩa khoảng cách từ điểm đến mặt phẳng?"
- Then: câu trả lời `concept` bị thay bằng câu dẫn tới gợi ý hiện có; `AIPolicyDecision` ghi `kind_not_allowed`

**US-S5E3-04-SC3 — Giáo viên đặt mức trợ giúp khi tạo bài** · Nguồn: BR-S5E3-12
- Given: cô Lan tạo bài có chấm BC-01 (C5-01)
- When: mở mục "Trợ giúp của AI"; chọn "Chỉ gợi ý" rồi Lưu
- Then: mặc định hiện "Tắt" cho bài có chấm; sau khi Lưu, bài hiện "Trợ giúp: Chỉ gợi ý"

**US-S5E3-04-FC1 — Lớp tắt AI thì chặn trước mọi bước khác** · Nguồn: BR-S5E3-13; PO-85
- Given: lớp 11A1 tắt trợ lý AI (US-S3E5-04), bài mức Gợi ý và khái niệm
- When: Bình An gửi câu hỏi
- Then: trả thông điệp lớp tắt AI của S3-E5; không gọi AI; `AIPolicyDecision` bước `class_switch`

**US-S5E3-04-FC2 — Em Đã rời không hỏi được trên bài của lớp** · Nguồn: BR-S5E3-13
- Given: Văn Tú Đã rời 11A1
- When: Văn Tú gửi câu hỏi trên BC-01
- Then: NOT_FOUND; không gọi AI

---

## US-S5E3-05 — Kiểm soát đáp án đầy đủ: bộ kiểm đầu ra KS-1…KS-5 cho mọi câu trả lời, không lộ ghi chú riêng và bài của bạn, nhật ký quyết định
- **Story:** Là giáo viên, tôi muốn mọi câu trả lời của AI cho học sinh đều qua bộ kiểm lộ đáp án ở máy chủ, kể cả khi nguồn là tài liệu của tôi, để không em nào moi được đáp số trước giờ công bố. · Loại Logic · P0
- **Ước lượng: dev 3 ngày · test 0,9 ngày** — mở rộng `answer_forms` cho bài có chấm (đề F-S5-050) và lọc mục tài liệu trước khi gửi; thêm bảng `AIPolicyDecision`.
- **Nguồn:** ADR-GEO-14 KS-1…KS-5 (PO-79); C5 "Kiểm soát đáp án (Answer Firewall)"; NFR-GEO-020; STAGES §5 luồng C ngoại lệ "câu trả lời bị nghi lộ đáp án → thay bằng gợi ý an toàn, ghi lại cho giáo viên"
- **Góc nhìn người vận hành:** chạy trên mọi lượt hỏi; ca đáng kể: tài liệu của giáo viên vô tình chứa đáp số của chính bài đang khoá, học sinh dán đáp số vào câu hỏi để AI xác nhận, ghi chú riêng của giáo viên.
- **Phụ thuộc:** ADR-GEO-14 (bộ kiểm MVP đã cài, V60); US-S5E3-03, 04

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S5E3-15 | Mọi câu trả lời được đệm trọn và qua KS-1…KS-5 trước khi tới thiết bị; bài có chấm dùng `answer_forms` do engine tính khi tạo bài. Lỗi hoặc quá 200 ms thì đóng (KS-5). | ADR-GEO-14 |
| BR-S5E3-16 | Mục tài liệu chứa đáp số của bài đang khoá (khớp KS-1/KS-2) không được gửi cho AI khi bài còn khoá. | C5; QS5-13 |
| BR-S5E3-17 | Không gửi cho AI và không trả cho học sinh: ghi chú riêng của giáo viên (trường `ghi_chu_rieng`), hướng dẫn chấm, rubric chi tiết, bài nộp và câu hỏi của học sinh khác. | C5 "không lộ … ghi chú riêng của giáo viên, hay bài của học sinh khác"; STAGES Ý 10 "lộ rubric" |
| BR-S5E3-18 | Mỗi lượt ghi một `AIPolicyDecision`: `allowed` · `blocked` (mã luật KS-n hoặc bước kiểm trước) · `error_closed`, kèm id `AIInteraction`; giữ 1 năm học (QS5-14). | STAGES §8; ADR-GEO-14 |

- **DoR:** Ready for R3 (goal run, PO quyết); giả định ghi ở `work/s5/questions-s5.md`.

### Tiêu chí chấp nhận

**US-S5E3-05-SC1 — Câu trả lời chứa đáp số bài đang khoá bị thay bằng gợi ý an toàn** · Nguồn: BR-S5E3-15, 18
- Given: bài BC-01 mức Gợi ý và khái niệm, chưa công bố; giả lập F-S5-094b (chứa "a√6/3")
- When: Bình An hỏi "Khoảng cách từ A đến (SBC) bằng bao nhiêu?"
- Then: phản hồi `kind` = `refused_answer`, không chứa "a√6/3", "√6/3", "0,816"; `AIPolicyDecision` = `blocked`, luật KS-1; không tính thêm lượt gợi ý (PO-44)

**US-S5E3-05-SC2 — Học sinh dán đáp số nhờ xác nhận bị chặn trước khi gọi AI** · Nguồn: BR-S5E3-15; KS-4
- Given: như SC1
- When: Bình An hỏi "Có phải đáp số là a√6/3 không?"
- Then: phản hồi từ chối, không có từ xác nhận hay phủ nhận; không có dòng `AiCallLog` mới; `AIPolicyDecision` luật KS-4

**US-S5E3-05-SC3 — Sau giờ công bố thì không chặn đáp số của chính bài** · Nguồn: BR-S5E3-14
- Given: BC-01 đã công bố đáp số (cô Lan bấm Trả bài)
- When: Bình An hỏi như SC1 với giả lập F-S5-094b
- Then: câu trả lời giữ "a√6/3"; `AIPolicyDecision` = `allowed`

**US-S5E3-05-FC1 — Mục tài liệu chứa đáp số của bài đang khoá không được gửi AI** · Nguồn: BR-S5E3-16
- Given: `TL-01` v3 có mục "Đáp án bài 15 phút: a√6/3", gắn BC-01; BC-01 còn khoá
- When: Bình An hỏi câu khái niệm liên quan
- Then: yêu cầu ở giả lập không chứa mục đó; giáo viên thấy cảnh báo "Tài liệu TL-01 v3 có đáp số của bài đang khoá" trong lịch sử hỏi đáp

**US-S5E3-05-FC2 — Ghi chú riêng, rubric và bài của bạn không bao giờ tới AI hay học sinh** · Nguồn: BR-S5E3-17
- Given: BC-01 có `ghi_chu_rieng` "Em Mai hay nhầm SB"; rubric RB-01; bài nộp BN-02 của Chi Mai
- When: Bình An hỏi "Bạn Mai làm bài này thế nào?"; Bình An hỏi "Rubric chấm bài này ra sao?"
- Then: yêu cầu ở giả lập không chứa ghi chú riêng, rubric hay nội dung BN-02; phản hồi không chứa các nội dung đó

**US-S5E3-05-FC3 — Bộ kiểm lỗi thì đóng, không trả nội dung dở** · Nguồn: BR-S5E3-15; KS-5
- Given: bộ kiểm giả lập ném lỗi
- When: Bình An hỏi như SC1
- Then: phản hồi MSG-S5-24 và nút Thử lại; không có chữ nào của câu trả lời AI; `AIPolicyDecision` = `error_closed`

---

## US-S5E3-06 — Giáo viên xem lịch sử hỏi đáp theo bài, báo "chặn sai" hoặc "lẽ ra phải chặn"
- **Story:** Là giáo viên, tôi muốn xem học sinh đã hỏi gì, AI trả lời gì, dựa trên tài liệu nào và vì sao bị chặn, để sửa tài liệu và báo khi máy chặn nhầm. · Loại UI · P0
- **Ước lượng: dev 2 ngày · test 0,6 ngày**
- **Nguồn:** STAGES Ch5 §5 luồng C bước 3; §10 rủi ro "kiểm soát đáp án chặn nhầm"; C5 "Hồ sơ học tập: giáo viên xem lịch sử câu hỏi của học sinh lớp mình"; PO-04 (không xếp hạng)
- **Góc nhìn người vận hành:** cô Lan xem sau buổi tối học sinh làm bài, lọc các lượt bị chặn; ca đáng kể: lượt lỗi đóng, học sinh Đã rời, báo chặn sai hai lần.
- **Phụ thuộc:** US-S5E3-03, 05

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S5E3-19 | Danh sách theo bài: giờ, tên hiển thị học sinh, câu hỏi, câu trả lời đã hiện (hoặc câu từ chối), nguồn, quyết định và luật; lọc theo quyết định; phân trang theo con trỏ 50 dòng. | STAGES §5 C3; ADR-GEO-12 |
| BR-S5E3-20 | Giáo viên đánh dấu một lượt "chặn sai" hoặc "lẽ ra phải chặn" kèm ghi chú ≤ 200 ký tự; mỗi lượt một đánh dấu, sửa được; tổng hợp đếm theo luật cho người vận hành, không đổi câu trả lời đã gửi. | STAGES §10 |
| BR-S5E3-21 | Học sinh không xem được lịch sử của bạn; giáo viên không xem được lịch sử của lớp khác. | quy ước chung |

- **DoR:** Ready for R3 (goal run, PO quyết); giả định ghi ở `work/s5/questions-s5.md`.

### Tiêu chí chấp nhận

**US-S5E3-06-SC1 — Lọc các lượt bị chặn của bài BC-01** · Nguồn: BR-S5E3-19
- Given: BC-01 có 6 lượt: 4 `allowed`, 1 `blocked` KS-1, 1 `blocked` KS-4
- When: mở "Hỏi đáp" của bài BC-01; chọn lọc "Bị chặn"
- Then: thấy đúng 2 dòng, mỗi dòng có câu hỏi, câu từ chối đã hiện, luật KS-1 hoặc KS-4; không có cột xếp hạng hay đếm theo học sinh

**US-S5E3-06-SC2 — Báo "chặn sai" một lượt** · Nguồn: BR-S5E3-20
- Given: lượt bị chặn KS-4
- When: gửi `POST /api/qa/{id}/feedback` `{"type":"false_block","note":"Em hỏi so sánh với 1, không phải đáp số"}`
- Then: trả 200, MSG-S5-25; tổng hợp người vận hành KS-4 `false_block` tăng 1; câu trả lời đã gửi không đổi

**US-S5E3-06-FC1 — Thầy Khôi không xem được lịch sử của BC-01** · Nguồn: BR-S5E3-21
- Given: BC-01 của 11A1
- When: thầy Khôi gọi `GET /api/assignments/BC-01/qa`
- Then: NOT_FOUND

**US-S5E3-06-FC2 — Học sinh chỉ thấy lượt hỏi của chính mình** · Nguồn: BR-S5E3-21
- Given: BC-01 có lượt của Bình An và Chi Mai
- When: Bình An gọi `GET /api/qa/mine?assignment=BC-01`; Bình An gọi `GET /api/assignments/BC-01/qa`
- Then: lần 1 chỉ có lượt của Bình An; lần 2 FORBIDDEN

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-28 | Tạo story, BR, AC và ước lượng chặng 5 (goal run) | agent spec-s5 (PO → BA → QC) |
