Phiên bản: v0.1 · 2026-11-28
# S5-E5 — Thông báo do trợ lý soạn, tổng hợp lỗi sai của lớp (COP-02), công tắc và hạn mức AI của lớp: story Dev-Ready và tiêu chí chấp nhận

> Chủ sở hữu: BA (goal run, PO quyết cao nhất). Nguồn: `PRODUCT-MASTER.md` (22/11/2026) D1 dòng 5 ("trợ lý soạn … thông báo"), C4 "Trợ lý giáo viên: soạn bài, tạo đề, tổng hợp lỗi sai, soạn thông báo", C5 "Trợ lý giáo viên … Không gửi, không công bố, không đổi điểm nếu giáo viên chưa bấm duyệt"; `STAGES-AFTER-MVP.md` Chặng 5 §4 (P0 thông báo; P1 "tổng hợp lỗi phổ biến của lớp theo tiêu chí"), §7, §8 `CopilotDraft`, `FeatureSwitch`; COP-02 (`work/03_decisions.md` D-GEO-078: thẻ "Nên nhắc lại trên lớp" là tổng hợp lỗi sai COP-02 chặng 5); PO-04, PO-65 Q6 (ngưỡng nhận định gộp 5 bài), PO-83, PO-85; task `class_stuck` hiện có trong `AnthropicPrompts.java`; S3-E4-03 (thông báo trong ứng dụng), US-S3E5-04 (công tắc AI phía học sinh).
> **Trạng thái: ready for R3 (goal run)**; mức epic **P1**. Giả định đang dùng và câu hỏi cho owner ở `work/s5/questions-s5.md`; tự soát ở `work/s5/review-s5.md`.

**Mục tiêu epic.** Giáo viên có nháp thông báo lớp do trợ lý soạn và chỉ gửi khi tự bấm Gửi; sau mỗi bài có chấm thấy "cả lớp hay sai ở đâu" theo tiêu chí và loại lỗi, có bằng chứng, không gắn nhãn học sinh, kèm gợi ý bài nhắc lại lấy từ kho; công tắc AI chấm, trợ lý và hạn mức lượt AI theo lớp giữ chi phí và quyền tắt khẩn trong tay giáo viên.

**Điều chỉnh khung so với brief.** Thêm US-S5E5-04 (công tắc AI chấm và trợ lý theo lớp, hạn mức lượt AI) vì STAGES §4 P0 có "công tắc tắt AI theo trường, môn" mà multi-tenant ngoài phạm vi: PO thu về mức lớp và giáo viên. Mức epic P1 vì tổng hợp lỗi là P1 ở STAGES §4 và cần dữ liệu điểm đã duyệt của S5-E4/S5-E6; riêng US-S5E5-01 (thông báo) và US-S5E5-04 (công tắc) là P0.

**Ước lượng epic:** dev 9 ngày · test 2,7 ngày · tổng 11,7 ngày công (4 story; bảng chung ở `work/s5/estimate-s5.md`).

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
| MSG-S5-55 | Toast | "Đã gửi thông báo tới {n} em lớp {lớp}." | — |
| MSG-S5-56 | Inline | "Thông báo tối đa 1 000 ký tự." | VALIDATION_FAILED |
| MSG-S5-57 | Page state | "Cần ít nhất 5 bài đã duyệt để tổng hợp lỗi của lớp." | — |
| MSG-S5-58 | Page state | "AI tạm không tổng hợp được. Đây là số liệu thô theo tiêu chí." | — |
| MSG-S5-59 | Toast | "Đã tắt AI chấm cho lớp {lớp}. Bài mới nộp sẽ chấm tay." | — |
| MSG-S5-60 | Toast | "Lớp {lớp} đã dùng hết {n} lượt AI hôm nay. Lượt mới mở lúc 00:00." | RATE_LIMITED |

---

## US-S5E5-01 — Trợ lý soạn nháp thông báo lớp, giáo viên sửa và tự bấm Gửi (trong ứng dụng)
- **Story:** Là giáo viên, tôi muốn trợ lý soạn sẵn thông báo nhắc hạn, lịch kiểm tra hay trả bài từ dữ liệu lớp, để gửi nhanh mà vẫn đúng giọng của tôi. · Loại UI · P0
- **Ước lượng: dev 2 ngày · test 0,6 ngày**
- **Nguồn:** C5 "Trợ lý giáo viên"; STAGES §7 "Trợ lý không gửi, không công bố"; PO-65 Q9 (chỉ thông báo trong ứng dụng); S3-E4-03; PO-68 (giờ yên lặng chỉ áp cho nhắc tự động)
- **Góc nhìn người vận hành:** cô Lan soạn 2–3 thông báo mỗi tuần; ca đáng kể: AI chèn tên học sinh, thông báo dài, gửi nhầm lớp, lớp có em Đã rời.
- **Phụ thuộc:** S3-E4-03 (kênh thông báo trong ứng dụng); `aigateway`

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S5E5-01 | Loại thông báo: Nhắc hạn nộp · Lịch kiểm tra · Đã trả bài · Tự do; máy chủ điền dữ kiện (tên bài, hạn, giờ) từ dữ liệu lớp, AI chỉ viết câu chữ (task `notice_draft`); nội dung gửi AI không có tên hay mã học sinh. | C5; NFR-GEO-071 |
| BR-S5E5-02 | Nháp `CopilotDraft` loại `notice` nhãn "AI soạn"; chỉ gửi khi giáo viên bấm Gửi; nội dung ≤ 1 000 ký tự; gửi tới học sinh Đang học của lớp tại thời điểm gửi; nhãn trên thông báo đã gửi là tên giáo viên, không có chữ "AI". | STAGES §7; PO-03 (tinh thần); QS5-25 |
| BR-S5E5-03 | Dữ kiện trong nháp (hạn, giờ) phải khớp dữ liệu lớp; AI viết sai dữ kiện thì máy chủ thay bằng giá trị đúng và gắn "AI chưa chắc" ở chỗ đó. | C2 nguyên tắc 13 (tinh thần: dữ kiện do máy) |

- **DoR:** Ready for R3 (goal run, PO quyết); giả định ghi ở `work/s5/questions-s5.md`.

### Tiêu chí chấp nhận

**US-S5E5-01-SC1 — Soạn nháp nhắc hạn BC-01, sửa một câu và gửi** · Nguồn: BR-S5E5-01, 02
- Given: BC-01 hạn T6 11/12/2026 20:00; giả lập `notice_draft` trả 3 câu
- When: mở "Trợ lý · Thông báo", chọn "Nhắc hạn nộp" và bài BC-01; sửa câu cuối; bấm Gửi
- Then: nháp nhãn "AI soạn" có "20:00, 11/12/2026"; sau Gửi hiện MSG-S5-55 với n = 8; 8 em Đang học có thông báo; Văn Tú (Đã rời) không có

**US-S5E5-01-SC2 — Nháp chưa gửi thì học sinh không thấy** · Nguồn: BR-S5E5-02
- Given: nháp ở SC1 chưa bấm Gửi
- When: Bình An gọi `GET /api/notifications`
- Then: không có thông báo nào từ nháp đó

**US-S5E5-01-FC1 — AI viết sai giờ hạn thì máy chủ thay bằng giờ đúng** · Nguồn: BR-S5E5-03
- Given: giả lập trả "hạn nộp 21:00 thứ Sáu"
- When: gửi tạo nháp nhắc hạn BC-01
- Then: nháp có "20:00"; chỗ đó gắn "AI chưa chắc"

**US-S5E5-01-FC2 — Thông báo 1 001 ký tự bị từ chối** · Nguồn: BR-S5E5-02
- Given: nháp đã sửa thành 1 001 ký tự
- When: bấm Gửi (gọi `POST /api/notices/{id}/send`)
- Then: VALIDATION_FAILED, MSG-S5-56; không em nào nhận

**US-S5E5-01-FC3 — Không gửi được tới lớp của người khác** · Nguồn: quy ước chung
- Given: lớp 10B
- When: cô Lan tạo nháp và gửi với `classId` 10B
- Then: NOT_FOUND

---

## US-S5E5-02 — Tổng hợp lỗi sai của lớp theo tiêu chí và loại lỗi (COP-02), có bằng chứng, không gắn nhãn học sinh
- **Story:** Là giáo viên, tôi muốn sau mỗi bài có chấm thấy cả lớp hay mất điểm ở tiêu chí nào và vì loại lỗi gì, kèm bài làm làm căn cứ, để chữa bài đúng chỗ. · Loại Logic · P1
- **Ước lượng: dev 3 ngày · test 0,9 ngày** — phần số liệu thô tất định (theo tiêu chí, loại lỗi) và task `class_errors` theo khuôn `class_stuck`, bộ kiểm đầu ra chặn tên và phần trăm.
- **Nguồn:** C4 "tổng hợp lỗi sai"; STAGES §4 P1; D-GEO-078 (COP-02, thẻ "Nên nhắc lại trên lớp"); PO-04, PO-65 Q6 (5 bài), PO-83 (bộ gộp, thời gian chờ), PO-85 (lớp tắt AI thì dữ liệu thô); task `class_stuck` (ẩn danh, tối đa 3 nhận định, không phần trăm, không xếp hạng)
- **Góc nhìn người vận hành:** cô Lan xem sau khi duyệt xong bài 15 phút; ca đáng kể: dưới 5 bài, AI nêu tên học sinh, lớp tắt AI, điểm sửa sau khi đã tổng hợp.
- **Phụ thuộc:** US-S5E4-06 (`ApprovedGrade`); US-S5E6-01, 04 (`CriterionAssessment`, loại lỗi); US-S5E6-03 (tín hiệu engine)

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S5E5-04 | Số liệu thô (không AI): mỗi tiêu chí số bài mất điểm và điểm trung bình; mỗi loại lỗi (`sai-dinh-li`, `sai-tinh-toan`, `sai-hinh-dung`, `nham-dieu-kien`) số bài; chỉ tính `ApprovedGrade` mới nhất; loại lỗi chỉ lấy từ đề xuất AI mà giáo viên đã Nhận hoặc giữ khi Sửa (bài chấm tay chỉ vào số liệu theo tiêu chí); dưới 5 bài đã duyệt thì không hiện, MSG-S5-57. | PO-65 Q6; KPI §1 |
| BR-S5E5-05 | Task `class_errors` nhận dữ liệu ẩn danh ("Bài 1", "Bài 2"…: tiêu chí mất điểm, loại lỗi, lý do của AI hoặc nhận xét của giáo viên) và trả tối đa 3 nhận định một câu, mỗi nhận định dẫn số thứ tự bài làm căn cứ. | `class_stuck`; PO-83 |
| BR-S5E5-06 | Bộ kiểm đầu ra loại nhận định có tên hiển thị, mã người dùng, phần trăm, từ xếp hạng hoặc nhãn "yếu/kém/lười"; số thứ tự căn cứ phải có trong dữ liệu; bấm căn cứ mở bài làm đó cho giáo viên. | PO-04; C2 nguyên tắc 5 |
| BR-S5E5-07 | Lớp tắt trợ lý AI hoặc AI lỗi quá 10 giây thì chỉ hiện số liệu thô kèm MSG-S5-58, không gọi AI hoặc bỏ kết quả muộn. | PO-83, PO-85 |

- **DoR:** Ready for R3 (goal run, PO quyết); giả định ghi ở `work/s5/questions-s5.md`.

### Tiêu chí chấp nhận

**US-S5E5-02-SC1 — Tổng hợp BC-01 với 6 bài đã duyệt** · Nguồn: BR-S5E5-04, 05
- Given: BC-01 có 6 `ApprovedGrade`: 3 bài mất điểm "Bước" loại `sai-tinh-toan`, 1 bài `sai-hinh-dung`; giả lập `class_errors` trả 2 nhận định
- When: gọi `GET /api/graded-assignments/BC-01/error-summary`
- Then: số liệu thô: "Bước" 4/6 bài mất điểm; `sai-tinh-toan` 3, `sai-hinh-dung` 1; 2 nhận định, mỗi nhận định có danh sách bài căn cứ; không có tên học sinh trong phần nhận định

**US-S5E5-02-SC2 — Bấm căn cứ mở đúng bài làm** · Nguồn: BR-S5E5-06
- Given: kết quả ở SC1
- When: mở thẻ "Cả lớp hay sai ở đâu"; bấm "Bài 2" ở nhận định 1
- Then: mở màn xem bài nộp tương ứng (ảnh, điểm đã duyệt); thẻ không có cột xếp hạng hay phần trăm

**US-S5E5-02-FC1 — Dưới 5 bài thì không tổng hợp** · Nguồn: BR-S5E5-04
- Given: BC-01 có 4 bài đã duyệt
- When: gọi error-summary
- Then: trả MSG-S5-57, không có số liệu; không gọi AI

**US-S5E5-02-FC2 — Nhận định có tên học sinh hoặc phần trăm bị loại** · Nguồn: BR-S5E5-06
- Given: giả lập trả nhận định "Chi Mai và 50 % lớp sai bước tính SB"
- When: gọi error-summary
- Then: nhận định đó không có trong phản hồi; số liệu thô vẫn hiện; `AiCallLog` ghi kết quả kiểm "loại 1 nhận định"

**US-S5E5-02-FC3 — Lớp tắt trợ lý AI thì chỉ có số liệu thô** · Nguồn: BR-S5E5-07
- Given: 11A1 tắt trợ lý AI (US-S5E5-04)
- When: gọi error-summary
- Then: số liệu thô và MSG-S5-58; không có dòng `AiCallLog` mới

**US-S5E5-02-FC4 — Học sinh không xem được tổng hợp lỗi** · Nguồn: quy ước chung
- Given: BC-01 đã tổng hợp
- When: Bình An gọi error-summary
- Then: FORBIDDEN

---

## US-S5E5-03 — "Nên nhắc lại trên lớp": từ nhận định gợi ý bài luyện và tài liệu trong kho, giáo viên chọn rồi giao hoặc chiếu
- **Story:** Là giáo viên, tôi muốn từ chỗ cả lớp hay sai có ngay vài bài luyện cùng dạng và tài liệu của tôi để chữa, để không phải đi tìm lại trong kho. · Loại UI · P1
- **Ước lượng: dev 2 ngày · test 0,6 ngày**
- **Nguồn:** D-GEO-078 (thẻ "Nên nhắc lại trên lớp" = COP-02 chặng 5); C1b "Trợ lý giáo viên"; S3-E6-01 (chữa bài sang Trình chiếu); `de-thi/ngan-hang` (câu gắn `loai_loi` của nhiễu, chủ đề, dạng)
- **Góc nhìn người vận hành:** cô Lan chọn 2 bài cùng dạng để chữa đầu giờ thứ Hai; ca đáng kể: kho không có bài cùng dạng, giáo viên muốn giao luôn thành bộ luyện.
- **Phụ thuộc:** US-S5E5-02; US-S5E2-01 (ngân hàng); US-S5E3-01 (kho tri thức); S3-E2 (giao bộ bài); S3-E6-01 (trình chiếu)

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S5E5-08 | Gợi ý tất định, không gọi AI: tối đa 3 câu từ ngân hàng cùng chủ đề và dạng với đề của bài, ưu tiên câu có nhiễu gắn loại lỗi trùng loại lỗi của nhận định; tối đa 2 mục tài liệu Đang dùng gắn lớp có cùng mã kiến thức. | `de-thi` README "Nhiễu"; C2 nguyên tắc 3 |
| BR-S5E5-09 | Gợi ý chỉ là danh sách chọn; giáo viên tick rồi chọn "Tạo bộ luyện" (sang S3-E2, trạng thái Nháp) hoặc "Mở trình chiếu" (S3-E6-01); không tự giao. | STAGES §7 |

- **DoR:** Ready for R3 (goal run, PO quyết); giả định ghi ở `work/s5/questions-s5.md`.

### Tiêu chí chấp nhận

**US-S5E5-03-SC1 — Từ nhận định chọn 2 câu và tạo bộ luyện nháp** · Nguồn: BR-S5E5-08, 09
- Given: kết quả tổng hợp BC-01 có nhận định loại `sai-tinh-toan`
- When: bấm "Nên nhắc lại trên lớp"; tick 2 câu gợi ý; bấm "Tạo bộ luyện"
- Then: danh sách có tối đa 3 câu cùng chủ đề khoảng cách và tối đa 2 mục tài liệu; bộ luyện mới trạng thái Nháp, có 2 câu đã chọn, chưa giao lớp nào

**US-S5E5-03-SC2 — Câu có nhiễu cùng loại lỗi được xếp trước** · Nguồn: BR-S5E5-08
- Given: ngân hàng có 5 câu cùng dạng, 2 câu có nhiễu `sai-tinh-toan`
- When: gọi `GET /api/graded-assignments/BC-01/error-summary/1/suggestions`
- Then: 2 câu có nhiễu `sai-tinh-toan` đứng đầu; `AiCallLog` không có dòng mới

**US-S5E5-03-FC1 — Kho không có câu cùng dạng thì nói rõ** · Nguồn: BR-S5E5-08
- Given: đề của bài thuộc dạng chưa có trong ngân hàng
- When: gọi suggestions
- Then: danh sách câu rỗng kèm câu "Kho chưa có bài cùng dạng"; mục tài liệu vẫn hiện nếu có

---

## US-S5E5-04 — Công tắc AI chấm và trợ lý theo lớp, tắt khẩn, hạn mức lượt AI mỗi lớp và nhật ký dùng AI cho giáo viên
- **Story:** Là giáo viên, tôi muốn tắt ngay AI chấm hoặc trợ lý của một lớp và biết lớp đã dùng bao nhiêu lượt AI, để giữ quyền quyết định và chi phí trong tay. · Loại Logic · P0
- **Ước lượng: dev 2 ngày · test 0,6 ngày**
- **Nguồn:** STAGES §4 P0 "công tắc tắt AI theo trường, môn", §6 C5-09 ("đang tắt khẩn cấp"), §8 `FeatureSwitch`; US-S3E5-04 (công tắc AI phía học sinh); PO-85; C2 nguyên tắc 10
- **Góc nhìn người vận hành:** cô Lan tắt AI chấm khi thấy chấm lệch bất thường; ca đáng kể: tắt giữa lúc có bài đang chấm nền, lớp hết lượt AI giữa buổi tối.
- **Phụ thuộc:** US-S3E5-04; `aigateway` (`AiCallLog`)

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S5E5-10 | `FeatureSwitch` theo lớp có ba công tắc độc lập: AI phía học sinh (đã có ở US-S3E5-04), AI chấm, Trợ lý (giáo án, đề, thông báo, tổng hợp lỗi); mặc định AI chấm tắt, Trợ lý bật (QS5-26); mỗi lần đổi ghi `AuditEvent`. | STAGES §8; QS5-26 |
| BR-S5E5-11 | Tắt AI chấm có hiệu lực ngay: việc chấm nền đang chờ bị huỷ, đề xuất về sau không được lưu; bài đã có đề xuất vẫn giữ đề xuất. | STAGES §6 C5-09 |
| BR-S5E5-12 | Hạn mức lượt AI mỗi lớp mỗi ngày (giờ Việt Nam): 300 lượt cho hỏi đáp học sinh, 200 lượt chấm (đọc chữ + đề xuất); vượt thì RATE_LIMITED (MSG-S5-60), học sinh vẫn làm bài, bài vẫn chấm tay. Hạn mức soạn của giáo viên ở BR-S5E1-08. | QS5-04 |
| BR-S5E5-13 | Màn "Dùng AI" của lớp: số lượt theo task trong 30 ngày và số lượt bị chặn; không có tên học sinh. | C2 nguyên tắc 10 |

- **DoR:** Ready for R3 (goal run, PO quyết); giả định ghi ở `work/s5/questions-s5.md`.

### Tiêu chí chấp nhận

**US-S5E5-04-SC1 — Tắt AI chấm của 11A1 có hiệu lực ngay và có nhật ký** · Nguồn: BR-S5E5-10, 11
- Given: 11A1 AI chấm bật; BN-01 đang chờ chấm nền
- When: gửi `PUT /api/classes/11A1/switches` `{"aiGrading":false}`
- Then: trả 200, MSG-S5-59; việc chấm nền của BN-01 bị huỷ, không có `GradingProposal` mới; `AuditEvent` ghi người thực hiện cô Lan, trước bật, sau tắt

**US-S5E5-04-SC2 — Báo cáo dùng AI 30 ngày của lớp** · Nguồn: BR-S5E5-13
- Given: 11A1 có 120 lượt `kb_qa`, 40 lượt `grade`, 5 lượt bị chặn
- When: gọi `GET /api/classes/11A1/ai-usage?days=30`
- Then: trả số lượt theo task và số lượt bị chặn; không có tên hay mã học sinh

**US-S5E5-04-FC1 — Lượt hỏi thứ 301 trong ngày của lớp bị chặn, bài vẫn làm được** · Nguồn: BR-S5E5-12
- Given: 11A1 đã dùng 300 lượt hỏi đáp hôm nay
- When: Bình An gửi câu hỏi; Bình An gửi đáp số bài luyện
- Then: câu hỏi trả RATE_LIMITED kèm `Retry-After`, MSG-S5-60; gửi đáp số vẫn được chấm tự động bình thường

**US-S5E5-04-FC2 — Học sinh và giáo viên khác không đổi được công tắc** · Nguồn: BR-S5E5-10
- Given: 11A1 của cô Lan
- When: Bình An gửi `PUT /api/classes/11A1/switches`; thầy Khôi gửi cùng yêu cầu
- Then: Bình An FORBIDDEN; thầy Khôi NOT_FOUND; công tắc không đổi

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-28 | Tạo story, BR, AC và ước lượng chặng 5 (goal run) | agent spec-s5 (PO → BA → QC) |
