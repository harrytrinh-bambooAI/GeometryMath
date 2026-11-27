Phiên bản: v0.1 · 2026-11-28
# S5-E6 — AI đề xuất điểm theo rubric và KPI, engine làm tín hiệu thứ hai, chạy song song, giáo viên duyệt (đợt bật 2–3): story Dev-Ready và tiêu chí chấp nhận

> Chủ sở hữu: BA (goal run, PO quyết cao nhất). Nguồn: `PRODUCT-MASTER.md` (22/11/2026) D1 dòng 5, D2 D-05 ("AI chấm bài lùi về chặng 5"), C1b "Đánh giá và AI chấm bài", C5; `STAGES-AFTER-MVP.md` Chặng 5 §1 (chạy song song trước khi bật), §3 ý 3, 4, 5, 9 (bỏ tự duyệt), 10 (bỏ "AI chấm thử"), §5 B1–B4 và ngoại lệ, §6 C5-05, C5-09, §7, §8 (`GradingProposal`, `CriterionAssessment`), §10; PO-03; DECISIONS 27/11 "KPI chấm AI" (owner "KPI OK"); `materials/baseline/KPI.md` v1.0 §1–§4; `materials/baseline/bo-mau/` (71 bài, bước, `diem_dung`, `tieu_chi_dat`, lời giải sai) làm bộ đánh giá ngoài CI; số liệu prompt-lab "bắt bước sai 135/142, báo nhầm 0/71".
> **Trạng thái: ready for R3 (goal run)**; mức epic **P0**. Giả định đang dùng và câu hỏi cho owner ở `work/s5/questions-s5.md`; tự soát ở `work/s5/review-s5.md`.

**Mục tiêu epic.** AI đọc bài nộp và đề xuất điểm từng tiêu chí kèm lý do, vùng ảnh và độ tin cậy, máy chủ áp luật chặn KPI và đối chiếu đáp số với engine; trước khi bật cho một lớp, AI chạy song song với giáo viên tới khi đạt ngưỡng KPI; giáo viên luôn là người nhận, sửa, bỏ và duyệt.

**Điều chỉnh khung so với brief.** Phần AI của S5-E4 trong khung dự kiến tách thành epic này (lý do ở S5-E4). "Điều khiển AI của trường" (C5-09) thu về mức lớp vì multi-tenant ngoài phạm vi: cổng bật theo lớp ở US-S5E6-06, công tắc tắt khẩn ở US-S5E5-04.

**Không bao giờ:** tự duyệt khi AI tin cậy cao (STAGES ý 9, PO-03); học sinh xem "AI chấm thử" (ý 10); học sinh, phụ huynh thấy đề xuất AI (§7).

**Ước lượng epic:** dev 14 ngày · test 4,2 ngày · tổng 18,2 ngày công (6 story; bảng chung ở `work/s5/estimate-s5.md`).

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
| MSG-S5-45 | Page state | "AI không chấm bài này: {lý do}. Thầy cô chấm tay nhé." | — |
| MSG-S5-46 | Inline | "Đáp số trong bài khác kết quả máy tính ({giá trị engine})." | — |
| MSG-S5-47 | Page state | "Chế độ chạy song song: thầy cô chấm trước, đề xuất của AI hiện sau khi duyệt." | — |
| MSG-S5-48 | Page state | "Lớp chưa đủ {n}/20 bài chạy song song để bật AI đề xuất." | CONFLICT |
| MSG-S5-49 | Toast | "Đã nhận đề xuất của tiêu chí {tên}." | — |

---

## US-S5E6-01 — AI đề xuất điểm theo từng tiêu chí: lý do, vùng ảnh, độ tin cậy; máy chủ kiểm thang, tổng và luật chặn KPI
- **Story:** Là giáo viên, tôi muốn có sẵn đề xuất điểm từng tiêu chí với lý do và chỗ trên ảnh, để duyệt nhanh hơn chấm từ đầu mà vẫn tự quyết. · Loại Logic · P0
- **Ước lượng: dev 3 ngày · test 0,9 ngày** — task `grade` mới (system cố định theo KPI §1–§2, structured output), bộ kiểm luật chặn tất định và lưu `GradingProposal` / `CriterionAssessment` có phiên bản.
- **Nguồn:** STAGES §3 ý 3, §5 B1 ("kiểm tra điểm bằng quy tắc cứng: đúng thang, đúng tổng"), §7 ("mỗi đề xuất ghi rõ phiên bản đề, rubric, bài nộp, model và prompt"); KPI §1, §2; C5 "AI chấm từng tiêu chí, kèm lý do, bằng chứng, độ tin cậy"
- **Góc nhìn người vận hành:** chạy nền ngay sau bản đọc chữ; ca đáng kể: AI cho điểm vượt tối đa, đúng kết quả nhưng lập luận sai, bước sai kéo theo bước sau, prompt đổi phiên bản giữa đợt chấm.
- **Phụ thuộc:** US-S5E4-04 (bản đọc chữ); US-S5E4-01 (rubric); `aigateway`

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S5E6-01 | Đầu vào task `grade`: đề, đáp số engine, rubric phiên bản, bản đọc chữ phiên bản mới nhất và ảnh trang; không gửi tên, mã người dùng, mã lớp. Đầu ra mỗi tiêu chí: điểm, lý do ≤ 300 ký tự, vùng ảnh `{trang,x,y,w,h}`, độ tin cậy 0–1, loại lỗi theo bộ nhãn baseline (`sai-dinh-li`, `sai-tinh-toan`, `sai-hinh-dung`, `nham-dieu-kien`). | STAGES §3 ý 3; KPI §1; NFR-GEO-071 |
| BR-S5E6-02 | Máy chủ kiểm cứng: mỗi điểm trong [0, tối đa] và bội 0,25; tổng = tổng tiêu chí; đủ mọi tiêu chí của rubric. Sai thì gọi lại một lần; vẫn sai thì "AI không chấm" (US-S5E6-02). | STAGES §5 B1 |
| BR-S5E6-03 | Máy chủ áp luật chặn KPI §2 lên đề xuất: (1) Hướng = 0 thì tiêu chí có cờ phụ thuộc hướng = 0; (2) Kết quả đạt nhưng Bước < 50 % thì Kết quả tối đa 50 %; (5) bước dùng kiến thức ngoài kho thì gắn cờ "bịa kiến thức" và độ tin cậy ≤ 0,5. Mỗi lần điều chỉnh ghi lý do "luật KPI §2.n". | KPI §2 |
| BR-S5E6-04 | `GradingProposal` ghi: phiên bản đề, rubric, bài nộp, bản đọc chữ, model, phiên bản prompt, token; bất biến. Đề xuất chỉ giáo viên chủ lớp đọc được. | STAGES §7, §8 |

- **DoR:** Ready for R3 (goal run, PO quyết); giả định ghi ở `work/s5/questions-s5.md`.

### Tiêu chí chấp nhận

**US-S5E6-01-SC1 — Đề xuất cho BN-01 đủ 5 tiêu chí, tổng 10** · Nguồn: BR-S5E6-01, 02, 04
- Given: BC-01 chế độ AI đề xuất (lớp đã đạt cổng); BN-01 có bản đọc chữ v1; giả lập F-S5-090
- When: chờ xử lý nền xong; cô Lan gọi `GET /api/submissions/BN-01/proposal`
- Then: 5 tiêu chí 2 / 4 / 2 / 1 / 1, tổng 10, độ tin cậy 0,86; mỗi tiêu chí có lý do và vùng ảnh; bản ghi có phiên bản rubric RB-01 v1, bài nộp v1, prompt `grade`, model; yêu cầu ở giả lập không có "Bình An" hay `HS-4P9T-37WD`

**US-S5E6-01-SC2 — Luật chặn 2: đúng kết quả nhưng lập luận thiếu thì kết quả bị hạ** · Nguồn: BR-S5E6-03
- Given: BN-07 (F-S5-057); giả lập trả Hướng 2, Bước 1, Kết quả 2, Hình 1, Trình bày 1
- When: chờ xử lý nền
- Then: đề xuất lưu Kết quả = 1 với lý do "luật KPI §2.2"; tổng 6

**US-S5E6-01-SC3 — Luật chặn 1: sai hướng thì bước phụ thuộc hướng = 0** · Nguồn: BR-S5E6-03
- Given: giả lập trả Hướng 0, Bước 3
- When: chờ xử lý nền
- Then: đề xuất lưu Bước = 0 với lý do "luật KPI §2.1"

**US-S5E6-01-FC1 — Điểm vượt tối đa: gọi lại một lần rồi AI không chấm** · Nguồn: BR-S5E6-02
- Given: giả lập trả Bước = 5 (tối đa 4) cả hai lần
- When: chờ xử lý nền
- Then: `AiCallLog` có 2 dòng task `grade`; bài có cờ "AI không chấm" lý do "điểm ngoài thang"; không có `GradingProposal` hợp lệ

**US-S5E6-01-FC2 — Học sinh không đọc được đề xuất** · Nguồn: BR-S5E6-04
- Given: BN-01 có đề xuất
- When: Bình An gọi `GET /api/submissions/BN-01/proposal`
- Then: FORBIDDEN; thân phản hồi không có điểm hay độ tin cậy

**US-S5E6-01-FC3 — Bịa kiến thức bị gắn cờ và hạ độ tin cậy** · Nguồn: BR-S5E6-03
- Given: giả lập trả tiêu chí Bước dẫn "định lí ba đường cao không gian" (không có trong kho)
- When: chờ xử lý nền
- Then: tiêu chí đó cờ "bịa kiến thức", độ tin cậy ≤ 0,5; bài có cờ trong hàng chờ

---

## US-S5E6-02 — AI từ chối chấm và lỗi: ảnh kém, tin cậy thấp, hết giờ; chuyển chấm tay có lý do
- **Story:** Là giáo viên, tôi muốn AI nói rõ khi nó không chấm được và vì sao, để tôi chấm tay đúng những bài đó thay vì tin một điểm đoán. · Loại Logic · P0
- **Ước lượng: dev 2 ngày · test 0,6 ngày**
- **Nguồn:** STAGES §3 ý 4 ("khi ảnh kém hoặc độ tin cậy thấp thì AI nói rõ lý do và chuyển chấm tay"), §6 C5-04 trạng thái "AI không chấm"; PO-78 (thời gian chờ cổng AI)
- **Góc nhìn người vận hành:** vài bài mỗi lớp; ca đáng kể: trang mờ đã qua kiểm lúc chụp nhưng AI vẫn không đọc được, AI hết giờ ở giờ cao điểm, lớp tắt AI chấm giữa chừng.
- **Phụ thuộc:** US-S5E6-01

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S5E6-05 | "AI không chấm" khi: AI trả `refuse` kèm lý do; độ tin cậy tổng < 0,6 (QS5-22); kiểm cứng sai hai lần; cổng AI lỗi hoặc hết giờ sau một lần gọi lại. Lý do lưu và hiện cho giáo viên. | STAGES §3 ý 4; PO-78; QS5-22 |
| BR-S5E6-06 | Bài "AI không chấm" vẫn Chờ chấm, có cờ, xếp đầu hàng chờ; không có đề xuất điểm nào hiện cho giáo viên; giáo viên chấm tay như US-S5E4-06. | STAGES §5 B1 |
| BR-S5E6-07 | Công tắc AI chấm của lớp tắt (US-S5E5-04) thì bài mới nộp không gọi `grade`; hàng chờ hiện trạng thái "AI đang tắt". | STAGES §6 C5-04 |

- **DoR:** Ready for R3 (goal run, PO quyết); giả định ghi ở `work/s5/questions-s5.md`.

### Tiêu chí chấp nhận

**US-S5E6-02-SC1 — AI từ chối vì trang mờ** · Nguồn: BR-S5E6-05, 06
- Given: BN-03 (F-S5-055); giả lập F-S5-092
- When: chờ xử lý nền; cô Lan mở BN-03
- Then: bài Chờ chấm, cờ "AI không chấm"; hiện MSG-S5-45 với lý do "Trang 2 mờ, không đọc được bước 2–3"; không có điểm đề xuất

**US-S5E6-02-SC2 — Độ tin cậy thấp thì không hiện điểm** · Nguồn: BR-S5E6-05
- Given: giả lập trả đề xuất hợp lệ với độ tin cậy tổng 0,55
- When: chờ xử lý nền
- Then: cờ "AI không chấm", lý do "độ tin cậy thấp (0,55)"; giáo viên không thấy điểm của đề xuất đó

**US-S5E6-02-FC1 — Cổng AI hết giờ hai lần** · Nguồn: BR-S5E6-05
- Given: giả lập hết giờ cả hai lần
- When: chờ xử lý nền
- Then: cờ "AI không chấm", lý do "AI tạm thời không phản hồi"; nút "Thử chấm lại"; bài vẫn chấm tay được

**US-S5E6-02-FC2 — Lớp tắt AI chấm thì không gọi AI** · Nguồn: BR-S5E6-07
- Given: lớp 11A1 tắt AI chấm (US-S5E5-04); BC-01 chế độ AI đề xuất
- When: Ngọc Anh nộp bài
- Then: không có dòng `AiCallLog` task `grade`; hàng chờ hiện "AI đang tắt" cho bài đó

---

## US-S5E6-03 — Engine làm tín hiệu thứ hai: đối chiếu đáp số cuối trong bài với engine, lệch thì gắn cờ
- **Story:** Là giáo viên, tôi muốn máy tính hình học kiểm đáp số cuối trong bài làm độc lập với AI, để bắt được những bài AI chấm nhầm phần kết quả. · Loại Logic · P0
- **Ước lượng: dev 2 ngày · test 0,6 ngày** — tái dùng bộ so khớp biểu thức của ADR-GEO-14; phần mới là tách đáp số cuối từ bản đọc chữ và quy tắc cờ.
- **Nguồn:** STAGES §3 ý 5 ("ý khác biệt"), §5 B2 ("AI và engine lệch nhau xếp trước"); C2 nguyên tắc 13; ADR-GEO-14 KS-1, KS-2 (khớp biểu thức và giá trị số); `de-thi` đáp số engine
- **Góc nhìn người vận hành:** áp cho bài tính toán có đáp số engine; ca đáng kể: học sinh viết đáp số dạng thập phân, đổi biến, bài chứng minh không có đáp số.
- **Phụ thuộc:** US-S5E6-01; bộ so khớp KS-1/KS-2 của MVP (V60)

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S5E6-08 | Máy chủ lấy đáp số cuối từ bản đọc chữ (dòng kết luận, theo KS-3) và so với đáp số engine bằng KS-1/KS-2; kết quả: Khớp · Lệch · Không tìm thấy. Không gọi AI. | STAGES §3 ý 5; ADR-GEO-14 |
| BR-S5E6-09 | Cờ "AI và engine lệch" khi: engine Khớp mà AI cho Kết quả < 50 % tối đa, hoặc engine Lệch mà AI cho Kết quả > 50 % tối đa. Bài chứng minh (rubric không có tiêu chí Kết quả) bỏ qua. | STAGES §5 B2 |

- **DoR:** Ready for R3 (goal run, PO quyết); giả định ghi ở `work/s5/questions-s5.md`.

### Tiêu chí chấp nhận

**US-S5E6-03-SC1 — BN-01 đáp số khớp engine** · Nguồn: BR-S5E6-08
- Given: BN-01 bản đọc chữ có "Vậy d(A,(SBC)) = a√6/3"
- When: chờ xử lý nền
- Then: tín hiệu engine "Khớp" với giá trị engine `a√6/3`; không có cờ lệch (AI cho Kết quả 2/2)

**US-S5E6-03-SC2 — Đáp số viết thập phân vẫn khớp** · Nguồn: BR-S5E6-08
- Given: bản đọc chữ có "d ≈ 0,816a"
- When: chờ xử lý nền
- Then: tín hiệu "Khớp" (KS-2 làm tròn 3 chữ số)

**US-S5E6-03-FC1 — AI cho điểm kết quả nhưng engine báo lệch thì gắn cờ** · Nguồn: BR-S5E6-09
- Given: BN-02 đáp số `a√3/3`; giả lập trả Kết quả 2/2 cho BN-02
- When: chờ xử lý nền; cô Lan mở hàng chờ
- Then: tín hiệu engine "Lệch"; cờ "AI và engine lệch" xếp đầu hàng chờ; màn chấm hiện MSG-S5-46 với `a√6/3`

**US-S5E6-03-FC2 — Không tìm thấy đáp số cuối thì không gắn cờ lệch** · Nguồn: BR-S5E6-08
- Given: bản đọc chữ không có dòng kết luận
- When: chờ xử lý nền
- Then: tín hiệu "Không tìm thấy"; không có cờ lệch; giáo viên thấy ghi chú "Không thấy đáp số cuối trong bản đọc chữ"

---

## US-S5E6-04 — Màn chấm có đề xuất AI: nhận, sửa, bỏ từng tiêu chí, khoanh vùng ảnh, nhận cả bài khi không có cờ
- **Story:** Là giáo viên, tôi muốn bấm một tiêu chí là thấy chỗ trên ảnh được khoanh, rồi nhận, sửa hoặc bỏ từng tiêu chí trước khi duyệt, để quyết định nhanh mà vẫn có căn cứ. · Loại UI · P0
- **Ước lượng: dev 3 ngày · test 0,9 ngày** — khoanh vùng trên ảnh có thu phóng, trạng thái từng tiêu chí, phím tắt; ghi hành động từng tiêu chí cho chỉ số "tỉ lệ sửa".
- **Nguồn:** STAGES §5 B3–B4, §6 C5-05 (`vision-01`, `shots/14`), §3 "phản biện: làm cho việc duyệt nhanh hơn (phím tắt, nhận cả bài khi không có cờ)"; PO-03
- **Góc nhìn người vận hành:** cô Lan duyệt 30 bài một tối; ca đáng kể: duyệt theo quán tính, sửa một tiêu chí rồi muốn quay lại đề xuất, bài có cờ không được "nhận cả bài".
- **Phụ thuộc:** US-S5E6-01, 03; US-S5E4-06 (màn chấm tay, `ApprovedGrade`)

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S5E6-10 | Mỗi tiêu chí có ba thao tác: Nhận (dùng điểm đề xuất), Sửa (nhập điểm khác, lý do tuỳ chọn), Bỏ (để trống, phải nhập điểm tay trước khi duyệt). Duyệt chỉ bật khi mọi tiêu chí có điểm. | STAGES §5 B4 |
| BR-S5E6-11 | "Nhận cả bài" chỉ có khi bài không có cờ nào; bài có cờ phải xử lý từng tiêu chí. Phím tắt: J/K tiêu chí kế/trước, A nhận, E sửa, X bỏ, Enter duyệt. | STAGES §3 phản biện; QS5-23 |
| BR-S5E6-12 | Ghi hành động từng tiêu chí (nhận/sửa/bỏ, điểm AI, điểm giáo viên) vào `CriterionAssessment` để đo tỉ lệ sửa; `ApprovedGrade` ghi nguồn "AI đề xuất, giáo viên duyệt". | STAGES §10 chỉ số; §8 |

- **DoR:** Ready for R3 (goal run, PO quyết); giả định ghi ở `work/s5/questions-s5.md`.

### Tiêu chí chấp nhận

**US-S5E6-04-SC1 — Bấm tiêu chí khoanh vùng ảnh, nhận từng tiêu chí rồi duyệt** · Nguồn: BR-S5E6-10, 12
- Given: BN-01 có đề xuất F-S5-090, không cờ
- When: mở BN-01; bấm tiêu chí "Bước"; bấm Nhận ở cả 5 tiêu chí; bấm Duyệt
- Then: ảnh trang có vùng khoanh đúng `{trang,x,y,w,h}` của tiêu chí "Bước"; mỗi tiêu chí sau Nhận hiện dấu đã nhận và MSG-S5-49; sau Duyệt `ApprovedGrade` tổng 10, nguồn "AI đề xuất, giáo viên duyệt"

**US-S5E6-04-SC2 — Sửa một tiêu chí, bỏ một tiêu chí rồi nhập tay** · Nguồn: BR-S5E6-10, 12
- Given: BN-02 có đề xuất F-S5-091
- When: Sửa "Bước" từ 3 thành 2,5; Bỏ "Trình bày"; thử bấm Duyệt; nhập "Trình bày" 0,5 rồi Duyệt
- Then: sau bước 3 nút Duyệt mờ; sau bước 4 tổng 6,5; `CriterionAssessment` ghi Bước sửa (AI 3 · GV 2,5), Trình bày bỏ (AI 1 · GV 0,5)

**US-S5E6-04-SC3 — Nhận cả bài và phím tắt cho bài không cờ** · Nguồn: BR-S5E6-11
- Given: BN-01 không cờ
- When: bấm "Nhận cả bài"; bấm Enter
- Then: cả 5 tiêu chí đã nhận; bài Đã duyệt, mở bài kế tiếp

**US-S5E6-04-FC1 — Bài có cờ không có "Nhận cả bài"** · Nguồn: BR-S5E6-11
- Given: BN-02 có cờ "AI và engine lệch"
- When: mở BN-02
- Then: không có nút "Nhận cả bài"; phím tắt nhận cả bài không có tác dụng

**US-S5E6-04-FC2 — Gọi API duyệt cả bài có cờ bị từ chối** · Nguồn: BR-S5E6-11
- Given: BN-02 có cờ
- When: gửi `POST /api/submissions/BN-02/grade` `{"acceptAll":true}`
- Then: CONFLICT; không có `ApprovedGrade`

---

## US-S5E6-05 — Chạy song song: giáo viên chấm tay trước, đề xuất AI hiện sau khi duyệt, lưu cặp so sánh
- **Story:** Là giáo viên, tôi muốn chấm tay mà không bị đề xuất của AI ảnh hưởng, rồi mới xem AI chấm thế nào, để đánh giá AI công bằng trên chính bài lớp tôi. · Loại Logic · P0
- **Ước lượng: dev 2 ngày · test 0,6 ngày**
- **Nguồn:** STAGES §1 ("trước khi bật AI chấm cho lớp nào, AI phải chạy song song với giáo viên trên bài thật của lớp đó"), §3 ý 4, §5 B ngoại lệ ("giáo viên chấm tay trước, sau đó mới thấy đề xuất"); C5-05 trạng thái "Chạy song song (ẩn đề xuất)"
- **Góc nhìn người vận hành:** cô Lan chấm 20–30 bài đầu của 11A1 ở chế độ này; ca đáng kể: giáo viên cố mở đề xuất trước khi duyệt qua API, sửa điểm sau khi đã thấy đề xuất.
- **Phụ thuộc:** US-S5E6-01; US-S5E4-06

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S5E6-13 | Chế độ Chạy song song: AI vẫn chấm nền; mọi phản hồi cho giáo viên trước khi bài Đã duyệt không chứa điểm, lý do, độ tin cậy của AI hay cờ suy từ AI ("AI và engine lệch", "bịa kiến thức"); chỉ có "AI đã chấm xong" và tín hiệu engine riêng. | STAGES §5 B ngoại lệ; C5-05 |
| BR-S5E6-14 | Sau khi duyệt: hiện so sánh từng tiêu chí AI và giáo viên; lưu cặp (điểm AI, điểm giáo viên lần duyệt đầu) để tính khớp; sửa điểm sau đó không đổi cặp đã lưu. | STAGES §10 chỉ số; KPI §3 |

- **DoR:** Ready for R3 (goal run, PO quyết); giả định ghi ở `work/s5/questions-s5.md`.

### Tiêu chí chấp nhận

**US-S5E6-05-SC1 — Trước khi duyệt không thấy đề xuất** · Nguồn: BR-S5E6-13
- Given: BC-01 chế độ Chạy song song; BN-02 AI đã chấm (F-S5-091)
- When: cô Lan gọi `GET /api/submissions/BN-02` và `/proposal`
- Then: `/proposal` trả CONFLICT kèm MSG-S5-47; `GET` bài nộp chỉ có cờ `aiDone = true`, không có `score`, `reason`, `confidence`

**US-S5E6-05-SC2 — Sau khi duyệt hiện so sánh và lưu cặp** · Nguồn: BR-S5E6-14
- Given: như SC1
- When: cô Lan duyệt BN-02 tổng 6,5 (Bước 2,5, Trình bày 0,5); gọi `GET /api/submissions/BN-02/comparison`
- Then: so sánh 5 tiêu chí: lệch ở Bước (AI 3 · GV 2,5) và Trình bày (AI 1 · GV 0,5); bảng cặp có 5 dòng của BN-02

**US-S5E6-05-FC1 — Sửa điểm sau khi thấy đề xuất không đổi cặp đã lưu** · Nguồn: BR-S5E6-14
- Given: BN-02 đã duyệt và đã xem so sánh
- When: cô Lan sửa Bước thành 3
- Then: `ApprovedGrade` mới tổng 7; cặp so sánh của tiêu chí Bước vẫn (AI 3 · GV 2,5)

**US-S5E6-05-FC2 — Màn chấm ở chế độ song song không hiện khung đề xuất** · Nguồn: BR-S5E6-13
- Given: như SC1
- When: cô Lan mở BN-02 trên màn chấm
- Then: hiện MSG-S5-47; không có ô điểm đề xuất, vùng khoanh hay độ tin cậy

---

## US-S5E6-06 — Báo cáo khớp AI–giáo viên theo tiêu chí và cổng bật "AI đề xuất" cho lớp theo ngưỡng KPI
- **Story:** Là giáo viên, tôi muốn thấy AI khớp với tôi đến đâu trên bài lớp mình và chỉ bật "AI đề xuất" khi đã đạt ngưỡng, để quyết định dựa trên số liệu chứ không dựa vào cảm giác. · Loại Logic · P0
- **Ước lượng: dev 2 ngày · test 0,6 ngày**
- **Nguồn:** STAGES §1, §6 C5-09 ("bảng so sánh AI và giáo viên"), §10 chỉ số (mức khớp theo tiêu chí và chủ đề, tỉ lệ sửa, tỉ lệ từ chối, thời gian duyệt); KPI §3, §4 (ngưỡng phát hành khởi điểm); PO-24 (không so sánh theo từng giáo viên ở cấp trường)
- **Góc nhìn người vận hành:** cô Lan xem sau khi chấm song song 20 bài; ca đáng kể: chưa đủ bài, đạt tổng nhưng một tiêu chí thấp, sau khi bật vẫn lấy mẫu song song định kỳ.
- **Phụ thuộc:** US-S5E6-05

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S5E6-15 | Chỉ số theo lớp, từ các cặp chạy song song: tỉ lệ tiêu chí lệch trong ±0,25; độ lệch tuyệt đối trung bình; tỉ lệ đáp số đúng của AI so engine; bắt đúng bước sai và báo nhầm; tỉ lệ "AI không chấm"; luôn kèm n. | KPI §3; STAGES §10 |
| BR-S5E6-16 | Cổng bật "AI đề xuất" cho một lớp: n ≥ 20 bài song song (QS5-21) và đạt KPI §4: lệch trong ±0,25 ≥ 85 %, kết quả đúng ≥ 95 %, bắt bước sai ≥ 80 % với báo nhầm ≤ 10 %, 0 lần bịa kiến thức; không đạt thì không chọn được chế độ AI đề xuất (US-S5E4-02 BR-S5E4-05). | KPI §4; STAGES §1 |
| BR-S5E6-17 | Sau khi bật, 10 % bài nộp ngẫu nhiên mỗi bài có chấm vẫn chạy song song để theo dõi trôi; hai đợt liên tiếp dưới ngưỡng thì cổng tự đóng và báo giáo viên. | STAGES §10 "chạy song song định kỳ trên mẫu ngẫu nhiên"; QS5-24 |

- **DoR:** Ready for R3 (goal run, PO quyết); giả định ghi ở `work/s5/questions-s5.md`.

### Tiêu chí chấp nhận

**US-S5E6-06-SC1 — Báo cáo khớp của 11A1 sau 20 bài** · Nguồn: BR-S5E6-15
- Given: 11A1 có 20 bài song song với 100 cặp tiêu chí, 88 cặp lệch trong ±0,25
- When: gọi `GET /api/classes/11A1/ai-grading/report`
- Then: trả `n = 20`, tỉ lệ trong ±0,25 = 88 % kèm KTC 95 %; số liệu theo từng tiêu chí; không có tên học sinh hay xếp hạng

**US-S5E6-06-SC2 — Đạt ngưỡng thì mở được chế độ AI đề xuất** · Nguồn: BR-S5E6-16
- Given: như SC1, mọi chỉ số đạt KPI §4
- When: cô Lan giao bài mới với chế độ `ai_proposal`
- Then: trả 201; bài chế độ AI đề xuất

**US-S5E6-06-FC1 — Chưa đủ 20 bài thì cổng đóng** · Nguồn: BR-S5E6-16
- Given: 11A1 có 12 bài song song
- When: gọi báo cáo; giao bài chế độ `ai_proposal`
- Then: báo cáo `gateOpen = false` kèm MSG-S5-48 "12/20"; giao bài trả CONFLICT

**US-S5E6-06-FC2 — Đủ bài nhưng một chỉ số dưới ngưỡng thì cổng đóng** · Nguồn: BR-S5E6-16
- Given: 20 bài; lệch trong ±0,25 = 80 %
- When: gọi báo cáo
- Then: `gateOpen = false`, nêu chỉ số "±0,25: 80 % < 85 %"

**US-S5E6-06-FC3 — Hai đợt lấy mẫu dưới ngưỡng thì cổng tự đóng** · Nguồn: BR-S5E6-17
- Given: 11A1 đã bật AI đề xuất; hai bài có chấm liên tiếp có mẫu song song lệch trong ±0,25 = 70 %
- When: hệ thống tính lại sau bài thứ hai
- Then: `gateOpen = false`; cô Lan có thông báo trong ứng dụng "AI đề xuất đã tạm tắt cho 11A1"; bài kế tiếp mặc định Chạy song song

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-28 | Tạo story, BR, AC và ước lượng chặng 5 (goal run) | agent spec-s5 (PO → BA → QC) |
