Phiên bản: v0.2 · 2026-11-28
# S5-E4 — Rubric, giao bài có chấm, nộp bài viết tay (ảnh, OCR), chấm tay và trả bài (đợt bật 1): story Dev-Ready và tiêu chí chấp nhận

> Chủ sở hữu: BA (goal run, PO quyết cao nhất). Nguồn: `PRODUCT-MASTER.md` (22/11/2026) D1 dòng 5, C1b "Đánh giá và AI chấm bài", C5 "Dạy và học", "Đánh giá và AI chấm bài"; `STAGES-AFTER-MVP.md` Chặng 5 §4 P0, §5 luồng A, B (bước 2–5), §6 C5-01…C5-06, §7, §8 (`Rubric*`, `Submission*`, `OCRArtifact`, `ApprovedGrade`, `ReleasePolicy`), §10 "tách ba đợt bật"; PO-03, PO-22, PO-25 ("chỉ giáo viên dạy lớp đó duyệt điểm"), PO-64 (ảnh bài làm cần quyết định riêng: QS5-01); `materials/baseline/KPI.md` v1.0 §1 (trọng số làm mẫu rubric); `materials/baseline/bo-mau/` (71 bài có bước, `diem_dung`, `tieu_chi_dat`, lời giải sai).
> **Trạng thái: ready for R3 (goal run)**; mức epic **P0**. Giả định đang dùng và câu hỏi cho owner ở `work/s5/questions-s5.md`; tự soát ở `work/s5/review-s5.md`.

**Mục tiêu epic.** Giáo viên giao bài có rubric phiên bản, học sinh chụp và nộp nhiều trang viết tay, hệ thống giữ ảnh gốc và tạo bản đọc chữ sửa được, giáo viên chấm theo từng tiêu chí, bấm Duyệt thành điểm chính thức rồi trả bài theo lịch công bố. Chưa có AI chấm: đây là nền để chạy song song ở S5-E6.

**Điều chỉnh khung so với brief.** Khung dự kiến gộp nộp bài, AI chấm và giáo viên duyệt trong một epic S5-E4 (khoảng 30 ngày dev, 13 story). PO tách thành S5-E4 (đợt bật 1: nộp bài và chấm tay) và S5-E6 (đợt bật 2–3: chạy song song rồi AI đề xuất), đúng cách giảm rủi ro "phạm vi quá lớn" của STAGES §10 và điều kiện "AI chạy song song với giáo viên trên bài thật trước khi bật" (§1). S5-E4 tự phát hành được, có giá trị ngay (giáo viên chấm trên web thay giấy) và tạo dữ liệu đối chứng cho S5-E6.

**Điều kiện phát hành.** PO-64 vẫn hiệu lực: không thu ảnh bài làm trước khi có quyết định riêng. Spec chạy trước theo giả định QS5-01 (thông báo riêng tư phiên bản mới, nhắc không ghi họ tên lên giấy, ảnh lưu trong nước, giữ tới hết năm học rồi xoá, xoá theo tài khoản); code được làm, **phát hành chờ owner chốt QS5-01**.

**Ước lượng epic:** dev 21 ngày · test 6,3 ngày · tổng 27,3 ngày công (9 story; bảng chung ở `work/s5/estimate-s5.md`).

## 0. Hướng dẫn đọc, quy ước và dữ liệu mẫu chung (giống nhau ở mọi epic S5)

**Cách đọc.** Mỗi mục `## US-S5En-nn — …` là một story: câu story, dòng **Ước lượng** (ngày công dev và test, test ≤ 30 % dev), nguồn, góc nhìn người vận hành, phụ thuộc, quy tắc nghiệp vụ (BR), tiêu chí chấp nhận (AC). Mỗi AC nói **một** hành vi quan sát được, dạng Given / When / Then, giá trị cụ thể lấy từ dữ liệu mẫu bên dưới. `SC` = kịch bản thành công, `FC` = lỗi, biên hoặc ngoại lệ.

**Quy ước chung cho mọi AC chặng 5** (kế thừa chặng 3, `epics/S3-E1/stories_ac.md` mục 0; PO-61…66):
- Mốc giờ lấy từ giờ máy chủ (T0); API dùng ISO-8601 UTC; màn hình hiện `HH:mm, dd/MM/yyyy` giờ Việt Nam.
- Lỗi máy chủ có dạng `{code, message, requestId, details[]}`; `code` ổn định: VALIDATION_FAILED, UNAUTHENTICATED, FORBIDDEN (sai vai), NOT_FOUND (đối tượng ngoài quan hệ; không lộ đối tượng có tồn tại), CONFLICT, RATE_LIMITED (kèm `Retry-After`), AI_UNAVAILABLE (cổng AI hết giờ hoặc lỗi sau một lần gọi lại; PO-78).
- Quyền theo quan hệ (US-S3E1-05): giáo viên chỉ thấy và tác động lên lớp mình tạo, học sinh Đang học của lớp đó và tài liệu, rubric, bài nộp thuộc các lớp đó; học sinh chỉ thấy bài nộp, điểm và hỏi đáp của chính mình.
- **AI chỉ đề xuất, giáo viên quyết định** (PO-03, C2 nguyên tắc 4): không có đường nào chuyển nội dung AI thành điểm chính thức, tài liệu Đang dùng, đề trong kho, giáo án đã duyệt hay thông báo đã gửi mà thiếu thao tác của giáo viên chủ lớp. Mọi bản nháp AI mang nhãn người "AI soạn" cho tới khi giáo viên duyệt (PO-02, PO-30).
- **Đáp số do engine** (C2 nguyên tắc 13): AI không bao giờ là nguồn đáp số. Đáp số của câu thuộc kho lấy từ kho (`bo-mau`, `bai-tap`, ngân hàng `de-thi`); câu mới đưa sang engine; không tính được thì nhãn máy "Cần xem lại".
- Mọi lời gọi AI đi qua module `aigateway` với một task có tên (`lesson_plan`, `lesson_patch`, `exam_draft`, `kb_qa`, `grade`, `grade_ocr`, `notice_draft`, `class_errors`); mỗi lượt ghi `AiCallLog` gồm task, model, phiên bản prompt, token vào/ra, kết quả kiểm máy chủ. Nội dung gửi nhà cung cấp không chứa tên hiển thị, mã người dùng, mã lớp (NFR-GEO-071).
- **Lượt AI** (BR-S5E5-12): một yêu cầu của người dùng hoặc một việc nền tới một task; gọi lại tự động trong lượt không tính thêm; `grade_ocr`, `grade` tính mỗi trang một lượt. Hạn mức: giáo viên 30 lượt soạn/ngày; lớp 300 hỏi đáp, 200 chấm, 2 000 000 token/ngày; học sinh 30 hỏi đáp/ngày; người vận hành có công tắc tắt khẩn toàn hệ thống theo task.
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
| MSG-S5-30 | Inline | "Tổng điểm các tiêu chí phải bằng thang điểm của bài." | VALIDATION_FAILED |
| MSG-S5-31 | Toast | "Rubric này đã dùng cho bài đã giao. Lưu sẽ tạo phiên bản mới; bài đã giao giữ phiên bản cũ." | — |
| MSG-S5-32 | Inline | "Ảnh trang {n} hơi mờ. Em chụp lại trang này nhé." | VALIDATION_FAILED |
| MSG-S5-33 | Page state | "Đã nộp lúc {HH:mm, dd/MM/yyyy}. Đang chờ thầy cô chấm." | — |
| MSG-S5-34 | Inline | "Đã quá hạn nộp. Thầy cô không nhận bài muộn cho bài này." | CONFLICT |
| MSG-S5-35 | Page state | "Mất kết nối. Bài của em đã lưu trên máy và sẽ gửi lại khi có mạng." | — |
| MSG-S5-36 | Page state | "Nhớ: không ghi họ tên, số điện thoại lên bài. Ảnh chỉ thầy cô lớp em xem, được xử lý bằng AI để đọc chữ và đề xuất điểm, và bị xoá cuối năm học." | — |
| MSG-S5-37 | Inline | "Điểm tiêu chí {tên} phải từ 0 đến {tối đa}, bước 0,25." | VALIDATION_FAILED |
| MSG-S5-38 | Toast | "Đã duyệt điểm. Học sinh sẽ thấy khi thầy cô bấm Trả bài." | — |
| MSG-S5-39 | Page state | "Em có bài mới được trả: {tên bài}." | — |

---

## US-S5E4-01 — Rubric có phiên bản, mẫu sẵn theo dạng bài, bất biến khi đã dùng
- **Story:** Là giáo viên, tôi muốn soạn rubric từ mẫu sẵn theo dạng bài và biết chắc rubric đã dùng cho bài đã giao không bị đổi, để chấm nhất quán và minh bạch. · Loại Logic · P0
- **Ước lượng: dev 3 ngày · test 0,9 ngày** — 3 bảng (`Rubric`, `RubricVersion`, `RubricCriterion`), hai mẫu từ KPI, ràng buộc bất biến ở CSDL như NFR-GEO-025.
- **Nguồn:** C5 "Rubric có version bất biến"; STAGES §3 ý 2, §6 C5-02, §7 "Rubric … đã dùng cho bài đã giao là bất biến", §10 "mẫu rubric theo dạng bài"; KPI §1 (tính toán 20/40/20/10/10, chứng minh 25/50/0/15/10), KPI §2 (luật chặn làm hướng dẫn chấm)
- **Góc nhìn người vận hành:** cô Lan lấy mẫu "Tính toán", đổi tên, chỉnh mô tả; ca đáng kể: tổng điểm lệch, sửa rubric đã giao, mẫu chứng minh không có tiêu chí kết quả.
- **Phụ thuộc:** module mới `assessment` (theo ADR-GEO-01 thêm một module trong khối chính)

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S5E4-01 | Rubric gồm 1–10 tiêu chí; mỗi tiêu chí có tên, mô tả mức đạt, điểm tối đa (bội 0,25) và cờ "phụ thuộc hướng"; tổng điểm tối đa = thang của bài (mặc định 10). | STAGES §3 ý 2; QS5-15 |
| BR-S5E4-02 | Hai mẫu hệ thống: "Tính toán" (Hướng 2 · Bước 4 · Kết quả 2 · Hình 1 · Trình bày 1) và "Chứng minh" (Hướng 2,5 · Lập luận 5 · Hình 1,5 · Trình bày 1), suy từ KPI §1 trên thang 10; kèm hướng dẫn chấm theo luật chặn KPI §2. | KPI §1, §2 |
| BR-S5E4-03 | Phiên bản đã gắn với một bài đã giao là bất biến (CSDL từ chối UPDATE); lưu thay đổi tạo phiên bản mới; bài đã giao và bài đã chấm giữ phiên bản cũ. | STAGES §5 B ngoại lệ, §7; NFR-GEO-025 (cách làm) |

- **DoR:** Ready for R3 (goal run, PO quyết); giả định ghi ở `work/s5/questions-s5.md`.

### Tiêu chí chấp nhận

**US-S5E4-01-SC1 — Tạo rubric từ mẫu Tính toán** · Nguồn: BR-S5E4-01, 02
- Given: cô Lan đã đăng nhập
- When: gửi `POST /api/rubrics` `{"fromTemplate":"tinh-toan","name":"Tính toán – khoảng cách"}`
- Then: rubric `RB-01` phiên bản 1 có 5 tiêu chí 2 / 4 / 2 / 1 / 1, tổng 10 (F-S5-051); tiêu chí "Bước" có cờ phụ thuộc hướng; hướng dẫn chấm có 5 luật chặn KPI §2

**US-S5E4-01-SC2 — Sửa rubric đã giao tạo phiên bản mới, bài đã giao giữ bản cũ** · Nguồn: BR-S5E4-03
- Given: RB-01 v1 gắn bài BC-01 đã giao
- When: gửi `PUT /api/rubrics/RB-01` đổi "Trình bày" thành 0,5 và "Hình" thành 1,5
- Then: trả phiên bản 2 kèm MSG-S5-31; BC-01 vẫn trỏ RB-01 v1; nội dung v1 không đổi

**US-S5E4-01-SC3 — Màn soạn rubric hiện tổng điểm sống và chặn lưu khi lệch** · Nguồn: BR-S5E4-01
- Given: cô Lan mở C5-02 với RB-01
- When: đổi điểm "Kết quả" từ 2 thành 3
- Then: dòng tổng hiện "11 / 10" màu cảnh báo; nút Lưu mờ, MSG-S5-30

**US-S5E4-01-FC1 — Tổng điểm khác thang bị từ chối** · Nguồn: BR-S5E4-01
- Given: cô Lan đã đăng nhập
- When: gửi rubric 5 tiêu chí tổng 9,75
- Then: VALIDATION_FAILED, MSG-S5-30

**US-S5E4-01-FC2 — Điểm không phải bội 0,25 hoặc 11 tiêu chí bị từ chối** · Nguồn: BR-S5E4-01
- Given: cô Lan đã đăng nhập
- When: gửi tiêu chí điểm 0,3; gửi rubric 11 tiêu chí
- Then: cả hai VALIDATION_FAILED với `details` đúng trường

**US-S5E4-01-FC3 — Cập nhật thẳng phiên bản đã dùng bị CSDL từ chối** · Nguồn: BR-S5E4-03
- Given: RB-01 v1 đã gắn bài đã giao
- When: chạy câu lệnh UPDATE `rubric_criterion` của v1 bằng vai ứng dụng (ca kỹ thuật qua API thử nghiệm của profile e2e)
- Then: lỗi từ CSDL; dữ liệu v1 không đổi

---

## US-S5E4-02 — Giao bài có chấm: đề hoặc bộ đề, rubric, chế độ chấm, hạn nộp, nộp muộn, lịch công bố điểm và lời giải
- **Story:** Là giáo viên, tôi muốn giao một bài có chấm với rubric, hạn nộp và thời điểm công bố, để học sinh biết phải nộp gì và khi nào thấy điểm. · Loại Logic · P0
- **Ước lượng: dev 2 ngày · test 0,6 ngày**
- **Nguồn:** STAGES §6 C5-01, §8 `ReleasePolicy`; C5 "Giáo viên điều khiển thời điểm công bố điểm, đáp án, lời giải"; PO-22; S3-E2 (bộ bài, `due_at`, `release_at`)
- **Góc nhìn người vận hành:** cô Lan giao bài 15 phút cho 11A1 hạn tối thứ Sáu; ca đáng kể: rubric chưa hợp lệ, chọn chế độ AI khi lớp chưa đủ dữ liệu song song, đổi hạn sau khi đã có bài nộp.
- **Phụ thuộc:** US-S5E4-01; S3-E2 (mô hình bộ bài); US-S5E2-04 (bộ đề đã duyệt)

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S5E4-04 | Bài có chấm gồm: đề (một đề kho hoặc một mã đề đã duyệt), rubric phiên bản cụ thể, thang điểm, hạn nộp, cho nộp muộn (có/không), chế độ chấm, `ReleasePolicy` (khi Trả bài · vào giờ cố định); lời giải mở cùng lúc công bố điểm (PO-22). | STAGES §6 C5-01; PO-22 |
| BR-S5E4-05 | Chế độ chấm: Chấm tay (mặc định) · Chạy song song · AI đề xuất; "AI đề xuất" chỉ chọn được khi lớp đã đạt cổng của US-S5E6-06; "Chạy song song" chỉ chọn được khi công tắc AI chấm của lớp đang bật (US-S5E5-04). | STAGES §1, §10; QS5-16 |
| BR-S5E4-06 | Đổi hạn nộp sau khi đã có bài nộp chỉ được kéo dài; đổi rubric sau khi đã có bài nộp bị từ chối. | QS5-17 |

- **DoR:** Ready for R3 (goal run, PO quyết); giả định ghi ở `work/s5/questions-s5.md`.

### Tiêu chí chấp nhận

**US-S5E4-02-SC1 — Giao bài BC-01 chế độ Chấm tay** · Nguồn: BR-S5E4-04
- Given: RB-01 v1, đề F-S5-050, lớp 11A1
- When: gửi `POST /api/graded-assignments` theo F-S5-052
- Then: bài BC-01 Đã giao, 8 em Đang học thấy bài trong "Bài cần nộp"; chế độ Chấm tay; thân phản hồi phía học sinh không có rubric chi tiết, hướng dẫn chấm hay lời giải

**US-S5E4-02-SC2 — Kéo dài hạn nộp sau khi đã có bài nộp** · Nguồn: BR-S5E4-06
- Given: BC-01 đã có BN-01
- When: đổi hạn sang 12/12/2026 20:00
- Then: trả 200; hạn mới hiện cho học sinh

**US-S5E4-02-FC1 — Chọn "AI đề xuất" khi lớp chưa đạt cổng bị từ chối** · Nguồn: BR-S5E4-05
- Given: lớp 11A1 chưa có kết quả chạy song song
- When: gửi giao bài với chế độ `ai_proposal`
- Then: CONFLICT, `details` nêu "lớp chưa đạt ngưỡng chạy song song"; không có bài nào được tạo

**US-S5E4-02-FC2 — Rút ngắn hạn hoặc đổi rubric sau khi có bài nộp bị từ chối** · Nguồn: BR-S5E4-06
- Given: BC-01 đã có BN-01
- When: đổi hạn sớm hơn 1 ngày; đổi sang RB-01 v2
- Then: cả hai CONFLICT; bài giữ nguyên

**US-S5E4-02-FC3 — Không giao bài có chấm cho lớp của người khác** · Nguồn: quy ước chung
- Given: lớp 10B của thầy Khôi
- When: cô Lan giao BC-01 cho 10B
- Then: NOT_FOUND

---

## US-S5E4-03 — Học sinh chụp và nộp nhiều trang trên iPhone: kiểm độ rõ, xếp trang, HEIC, thông báo riêng tư
- **Story:** Là học sinh, tôi muốn chụp từng trang bài làm bằng iPhone, xếp lại và nộp, để thầy cô nhận đủ và đọc rõ bài của em. · Loại UI · P0
- **Ước lượng: dev 3 ngày · test 0,9 ngày** — đã tách theo soát độc lập #15; phần còn lại là camera PWA iOS (gồm spike 0,5 ngày), nén, chấm độ rõ, xếp trang.
- **Nguồn:** STAGES §5 luồng A, §6 C5-03; C5 "nộp bài chữ hoặc nhiều ảnh viết tay; ảnh gốc được giữ nguyên"; PO-12 (web trên iPhone), PO-64, QS5-01, QS5-30
- **Góc nhìn người vận hành:** 8 em nộp dồn trong 30 phút trước hạn tối thứ Sáu; ca đáng kể: ảnh mờ, ảnh HEIC từ thư viện, máy hết bộ nhớ. Gửi lại khi mất mạng, nộp lại và nộp muộn tách sang US-S5E4-09; lưu ảnh an toàn sang US-S5E4-08.
- **Phụ thuộc:** US-S5E4-02, US-S5E4-08 (kho ảnh)

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S5E4-07 | Mỗi bài nộp 1–10 trang ảnh JPEG/HEIC. "Ảnh gốc của hệ thống" = tệp sau khi máy nén chất lượng để ≤ 8 MB/trang, ngay trước khi tải; từ lúc tải lên tệp này không bao giờ đổi. Điểm độ rõ < 0,5 thì yêu cầu chụp lại ngay trang đó. | STAGES §5 A1; C5; QS5-19 |
| BR-S5E4-08 | Máy chủ tạo bản phái sinh: HEIC chuyển JPEG cho đọc chữ và hiển thị, bản xoay, cắt mép; ảnh gốc lưu theo băm, không bị sửa hay thay thế. | STAGES §7; C5 |
| BR-S5E4-11 | Trước lần nộp đầu tiên, học sinh xem MSG-S5-36 (thông báo riêng tư S5: ảnh chỉ thầy cô lớp em xem, được xử lý bằng AI để đọc chữ và đề xuất điểm, xoá cuối năm học) và bấm "Em đã hiểu"; ghi cờ đã đọc cùng phiên bản văn bản. Chưa bấm thì không nộp được. | US-S3E5-02; PO-64; QS5-01, QS5-30 |

- **DoR:** Ready for R3 (goal run, PO quyết); giả định ghi ở `work/s5/questions-s5.md`.

### Tiêu chí chấp nhận

**US-S5E4-03-SC1 — Chụp 2 trang, xếp lại, nộp** · Nguồn: BR-S5E4-07, 08
- Given: Bình An mở BC-01 trên iPhone, đã đọc thông báo riêng tư
- When: bấm "Nộp bài" rồi chụp 2 trang; kéo trang 2 lên trước rồi kéo lại; bấm "Nộp"
- Then: màn hiện MSG-S5-33 với giờ máy chủ; bài nộp BN-01 có 2 trang đúng thứ tự cuối cùng; băm ảnh gốc trên máy chủ trùng băm tệp đã tải lên
- Dữ liệu nhập: F-S5-052, F-S5-053; ảnh mẫu `bn01-p1.jpg` 2,1 MB, `bn01-p2.jpg` 1,8 MB

**US-S5E4-03-SC2 — Ảnh HEIC được nhận, bản phái sinh JPEG dùng cho đọc chữ** · Nguồn: BR-S5E4-08
- Given: Bình An đã đọc thông báo riêng tư
- When: gửi `POST /api/submissions` 1 trang `bn01-p1.heic` (1,4 MB)
- Then: trả 201; ảnh gốc lưu đúng tệp HEIC (so băm); có bản phái sinh `image/jpeg`; task `grade_ocr` nhận bản JPEG
- Dữ liệu nhập: F-S5-052; tệp `bn01-p1.heic` 1,4 MB

**US-S5E4-03-SC3 — Lần nộp đầu phải xác nhận thông báo riêng tư có nói xử lý bằng AI** · Nguồn: BR-S5E4-11
- Given: Chi Mai chưa đọc thông báo riêng tư S5
- When: bấm "Nộp bài" lần đầu; bấm "Em đã hiểu"
- Then: hiện MSG-S5-36 có cụm "được xử lý bằng AI" và "xoá cuối năm học"; trước khi bấm, nút Chụp mờ; sau khi bấm, cờ đã đọc lưu phiên bản `privacy-s5-1.0`
- Dữ liệu nhập: F-S5-010 (Chi Mai `HS-8L2K-77AB`)

**US-S5E4-03-FC1 — Ảnh mờ bị yêu cầu chụp lại ngay** · Nguồn: BR-S5E4-07
- Given: Đức Huy chụp trang 2 điểm độ rõ 0,31 (F-S5-055)
- When: chụp trang 2; bấm "Dùng ảnh này"
- Then: hiện MSG-S5-32 cho trang 2; nút Nộp mờ cho tới khi trang 2 được chụp lại đạt
- Dữ liệu nhập: F-S5-055; ảnh `bn03-p2-mo.jpg` độ rõ 0,31 (biên: 0,50 đạt, 0,49 không đạt)

**US-S5E4-03-FC2 — Trang đúng 8 MB được nhận; 8 MB + 1 byte hoặc 11 trang bị từ chối** · Nguồn: BR-S5E4-07
- Given: Bình An đã đăng nhập
- When: gửi trang đúng 8 388 608 byte; gửi trang 8 388 609 byte; gửi bài 11 trang
- Then: lần 1 trả 201; lần 2 và lần 3 VALIDATION_FAILED
- Dữ liệu nhập: tệp 8 388 608 B; tệp 8 388 609 B; 11 tệp 500 KB

**US-S5E4-03-FC3 — Máy không đủ chỗ lưu nháp thì báo, trang đã tải không mất** · Nguồn: BR-S5E4-07
- Given: Bình An đã tải trang 1; trình duyệt báo hết hạn mức lưu trữ (giả lập `QuotaExceededError`)
- When: chụp trang 2
- Then: hiện "Máy em hết chỗ lưu, hãy nộp các trang đã chụp hoặc xoá bớt ảnh"; trang 1 vẫn còn trên máy chủ
- Dữ liệu nhập: F-S5-053

**US-S5E4-03-FC4 — Học sinh không đọc được bài nộp của bạn** · Nguồn: quy ước chung
- Given: BN-02 của Chi Mai
- When: Bình An gọi `GET /api/submissions/BN-02`
- Then: NOT_FOUND; không có URL ảnh nào trong thân phản hồi
- Dữ liệu nhập: F-S5-054

---

## US-S5E4-04 — Bản đọc chữ (OCR) của bài nộp: phái sinh, tô chỗ chưa chắc, giáo viên sửa có lịch sử
- **Story:** Là giáo viên, tôi muốn có bản đọc chữ của bài viết tay đặt cạnh ảnh gốc và sửa được, để đọc nhanh bài khó đọc và làm đầu vào cho chấm. · Loại Logic · P0
- **Ước lượng: dev 2 ngày · test 0,6 ngày**
- **Nguồn:** C5 "bản đọc chữ là bản phái sinh sửa được"; STAGES §5 B1, ngoại lệ "bản đọc chữ sai → giáo viên sửa và yêu cầu chấm lại; ảnh gốc không đổi", §7, §8 `OCRArtifact`; PO-30, PO-65 Q4 (Claude vision, ô chưa chắc tô để sửa); task `ocr` hiện có
- **Góc nhìn người vận hành:** chạy nền sau khi nộp; ca đáng kể: chữ viết tay ký hiệu toán (√, ⊥), trang trống, AI lỗi, giáo viên sửa rồi muốn xem lại bản trước.
- **Phụ thuộc:** US-S5E4-03; `aigateway`

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S5E4-12 | Task `grade_ocr` đọc từng trang thành văn bản có ký hiệu toán, mỗi đoạn kèm độ tin cậy 0–1 và vùng ảnh; đoạn < 0,6 tô vàng "chưa chắc". Không giải bài, không chấm. | PO-65 Q4; PO-30; QS5-20 |
| BR-S5E4-13 | `OCRArtifact` là bản phái sinh có phiên bản; sửa của giáo viên tạo phiên bản mới, ghi người sửa và giờ; ảnh gốc không đổi. | STAGES §7 |
| BR-S5E4-14 | AI lỗi sau một lần gọi lại thì bài vẫn ở trạng thái Chờ chấm, ô bản đọc chữ hiện "Chưa đọc được" và nút "Đọc lại" (tối đa 3 lần mỗi bài; mỗi trang tính 1 lượt chấm theo BR-S5E5-12); chấm tay không phụ thuộc bản đọc chữ. | PO-78 |

- **DoR:** Ready for R3 (goal run, PO quyết); giả định ghi ở `work/s5/questions-s5.md`.

### Tiêu chí chấp nhận

**US-S5E4-04-SC1 — Bản đọc chữ 2 trang có đoạn tô chưa chắc** · Nguồn: BR-S5E4-12
- Given: BN-01 vừa nộp; giả lập F-S5-095
- When: chờ xử lý nền xong; gọi `GET /api/submissions/BN-01/ocr`
- Then: bản đọc chữ phiên bản 1 có 2 trang; 2 đoạn `unsure = true` kèm vùng ảnh; `AiCallLog` task `grade_ocr`, nội dung gửi không có tên hay mã người dùng

**US-S5E4-04-SC2 — Giáo viên sửa bản đọc chữ tạo phiên bản mới** · Nguồn: BR-S5E4-13
- Given: bản đọc chữ BN-01 phiên bản 1
- When: cô Lan gửi `PUT /api/submissions/BN-01/ocr` sửa "a√6/2" thành "a√6/3"
- Then: phiên bản 2, người sửa cô Lan; phiên bản 1 vẫn đọc được; băm ảnh gốc không đổi

**US-S5E4-04-FC1 — AI lỗi thì bài vẫn chấm tay được** · Nguồn: BR-S5E4-14
- Given: giả lập `grade_ocr` lỗi cả hai lần
- When: chờ xử lý nền; cô Lan mở BN-01 trong hàng chờ
- Then: bài trạng thái Chờ chấm; bản đọc chữ "Chưa đọc được" kèm nút Đọc lại; ảnh gốc vẫn xem được

**US-S5E4-04-FC2 — Học sinh không xem hay sửa được bản đọc chữ** · Nguồn: BR-S5E4-13
- Given: BN-01 có bản đọc chữ
- When: Bình An gọi `GET` và `PUT /api/submissions/BN-01/ocr`
- Then: cả hai FORBIDDEN

---

## US-S5E4-05 — Hàng chờ chấm: danh sách theo bài, trạng thái, cờ xếp trước, lọc lớp và bài
- **Story:** Là giáo viên, tôi muốn thấy bài nào đã nộp, bài nào cần xem trước và còn bao nhiêu bài chưa chấm, để chấm theo đúng thứ tự ưu tiên. · Loại UI · P0
- **Ước lượng: dev 2 ngày · test 0,6 ngày**
- **Nguồn:** STAGES §5 B2, §6 C5-04 (trạng thái Trống, AI đang xử lý, AI không chấm, AI đang tắt); S3-E4 (tổng quan giáo viên, số bài chờ trên điện thoại)
- **Góc nhìn người vận hành:** cô Lan mở hàng chờ tối thứ Sáu sau hạn; điện thoại chỉ xem số bài chờ; ca đáng kể: bài muộn, bài có cờ, bài đã duyệt chưa trả.
- **Phụ thuộc:** US-S5E4-03, 04

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S5E4-15 | Trạng thái bài nộp: Nháp → Đã nộp → Đang xử lý → Chờ chấm → Đã duyệt → Đã trả; hàng chờ chỉ có bài Chờ chấm và Đang xử lý của lớp mình. | STAGES §8 |
| BR-S5E4-16 | Thứ tự: bài có cờ trước (độ tin cậy thấp, AI không chấm, lệch engine ở S5-E6; ở đợt 1 chỉ cờ "Nộp muộn" và "Chưa đọc được"), rồi theo giờ nộp tăng dần; không có cột điểm hay xếp hạng học sinh. | STAGES §5 B2; PO-04 |

- **DoR:** Ready for R3 (goal run, PO quyết); giả định ghi ở `work/s5/questions-s5.md`.

### Tiêu chí chấp nhận

**US-S5E4-05-SC1 — Hàng chờ BC-01 sau hạn** · Nguồn: BR-S5E4-15, 16
- Given: BC-01: BN-01, BN-02 Chờ chấm, BN-03 Chờ chấm có cờ "Chưa đọc được", BN-05 nộp muộn; Gia Bảo chưa nộp
- When: mở C5-04 lọc lớp 11A1, bài BC-01
- Then: BN-03 và BN-05 đứng đầu với nhãn cờ; tiếp theo BN-01, BN-02 theo giờ nộp; dòng tóm tắt "4 chờ chấm · 1 chưa nộp · 0 đã duyệt"; không có cột xếp hạng

**US-S5E4-05-SC2 — Số bài chờ cho màn điện thoại** · Nguồn: BR-S5E4-15
- Given: như SC1
- When: gọi `GET /api/grading/queue/summary`
- Then: trả `{"waiting":4,"processing":0}` theo từng bài; không có tên học sinh

**US-S5E4-05-FC1 — Hàng chờ không có bài của lớp khác** · Nguồn: BR-S5E4-15
- Given: 10B của thầy Khôi có 3 bài Chờ chấm
- When: cô Lan gọi `GET /api/grading/queue?classId=<10B>`
- Then: NOT_FOUND

**US-S5E4-05-FC2 — Trống khi chưa có bài nộp** · Nguồn: BR-S5E4-15
- Given: bài mới giao, chưa em nào nộp
- When: gọi `GET /api/grading/queue?assignment=<bài>`
- Then: danh sách rỗng, `state` = `empty` kèm hạn nộp

---

## US-S5E4-06 — Chấm tay theo rubric trên web, Duyệt thành điểm chính thức, nhật ký trước và sau
- **Story:** Là giáo viên, tôi muốn xem ảnh bài làm bên trái, rubric bên phải, nhập điểm từng tiêu chí và nhận xét rồi bấm Duyệt, để có điểm chính thức nhanh hơn chấm giấy. · Loại UI · P0
- **Ước lượng: dev 2 ngày · test 0,6 ngày**
- **Nguồn:** STAGES §5 B3–B4, §6 C5-05, §7 "không có chuyển trạng thái … mà thiếu hành động duyệt của giáo viên dạy lớp đó"; PO-03, PO-25; C2 nguyên tắc 10
- **Góc nhìn người vận hành:** cô Lan chấm 8 bài trong một buổi; ca đáng kể: điểm ngoài khoảng, sửa điểm sau duyệt, hai tab cùng chấm một bài.
- **Phụ thuộc:** US-S5E4-01, 05

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S5E4-17 | Điểm mỗi tiêu chí 0 đến tối đa, bước 0,25; tổng tự tính; nhận xét ≤ 1 000 ký tự. | BR-S5E4-01 |
| BR-S5E4-18 | Chỉ giáo viên chủ lớp của bài bấm Duyệt được; Duyệt tạo `ApprovedGrade` (điểm từng tiêu chí, tổng, nhận xét, người duyệt, giờ, phiên bản rubric, phiên bản bài nộp) và một `AuditEvent`. | PO-03, PO-25; US-S3E5-01 |
| BR-S5E4-19 | Sửa điểm đã duyệt tạo `ApprovedGrade` mới, ghi giá trị trước và sau trong `AuditEvent`; hai lần lưu đồng thời thì lần sau nhận CONFLICT (khóa lạc quan). | STAGES §5 B4; C2 nguyên tắc 10 |

- **DoR:** Ready for R3 (goal run, PO quyết); giả định ghi ở `work/s5/questions-s5.md`.

### Tiêu chí chấp nhận

**US-S5E4-06-SC1 — Chấm và duyệt BN-02** · Nguồn: BR-S5E4-17, 18
- Given: BN-02 Chờ chấm; RB-01 v1
- When: mở BN-02; nhập Hướng 2, Bước 3, Kết quả 0, Hình 1, Trình bày 1 và nhận xét "Xem lại cách tính SB"; bấm Duyệt
- Then: ảnh 3 trang ở trái, rubric 5 tiêu chí ở phải; tổng hiện 7; sau Duyệt hiện MSG-S5-38, bài sang Đã duyệt, mở bài kế tiếp trong hàng chờ

**US-S5E4-06-SC2 — Sửa điểm đã duyệt ghi trước và sau** · Nguồn: BR-S5E4-19
- Given: BN-02 đã duyệt tổng 7
- When: gửi sửa "Bước" từ 3 thành 3,5
- Then: `ApprovedGrade` mới tổng 7,5; `AuditEvent` có trước 3 · sau 3,5, người thực hiện cô Lan

**US-S5E4-06-FC1 — Điểm vượt tối đa hoặc lẻ 0,1 bị từ chối** · Nguồn: BR-S5E4-17
- Given: BN-01 Chờ chấm
- When: gửi "Bước" = 4,5; gửi "Hình" = 0,6
- Then: cả hai VALIDATION_FAILED, MSG-S5-37

**US-S5E4-06-FC2 — Giáo viên không chủ lớp không duyệt được** · Nguồn: BR-S5E4-18
- Given: BN-01 của 11A1
- When: thầy Khôi gửi `POST /api/submissions/BN-01/grade`
- Then: NOT_FOUND; không có `ApprovedGrade` mới

**US-S5E4-06-FC3 — Hai lần lưu đồng thời thì lần sau bị CONFLICT** · Nguồn: BR-S5E4-19
- Given: BN-01 Chờ chấm, phiên bản bản ghi 1
- When: gửi hai yêu cầu duyệt cùng `version` = 1
- Then: một yêu cầu thành công, yêu cầu còn lại CONFLICT; đúng 1 `ApprovedGrade`

---

## US-S5E4-07 — Trả bài: học sinh thấy điểm, nhận xét, lời giải theo lịch công bố; không thấy gì của AI
- **Story:** Là học sinh, tôi muốn xem điểm từng tiêu chí, nhận xét và lời giải khi thầy cô trả bài, để biết mình sai ở đâu. · Loại Logic · P0
- **Ước lượng: dev 2 ngày · test 0,6 ngày**
- **Nguồn:** STAGES §5 A3, B5, §6 C5-06, §7 "Đề xuất của AI chỉ giáo viên của lớp thấy"; PO-22 (giờ mở cho mọi kênh), PO-09 (phụ huynh không thuộc chặng này); S3-E6-03 (nhắc việc trong ứng dụng)
- **Góc nhìn người vận hành:** cô Lan bấm Trả bài cho cả lớp sáng thứ Hai; ca đáng kể: bài chưa duyệt hết, lịch công bố giờ cố định, học sinh mở trước giờ.
- **Phụ thuộc:** US-S5E4-06; S3-E4-03 / S3-E6-03 (thông báo trong ứng dụng)

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S5E4-20 | Trả bài chỉ gồm bài Đã duyệt; bài chưa duyệt vẫn ở hàng chờ. Với `ReleasePolicy` giờ cố định, máy chủ mở theo đồng hồ CSDL như NFR-GEO-023. | STAGES §5 B5; NFR-GEO-023 |
| BR-S5E4-21 | Trước khi trả: học sinh chỉ thấy "Đã nộp · Đang chờ thầy cô chấm"; không có điểm, rubric chi tiết, đề xuất AI, độ tin cậy hay bản đọc chữ trong bất kỳ phản hồi nào. | STAGES §5 A3, §7; PO-03 |
| BR-S5E4-23 | Lời giải và đáp án của bài chỉ hiện cho học sinh có bài Đã trả; em chưa nộp, có bài Chờ chấm hoặc Đã duyệt chưa trả thì vẫn khoá. Trả bài cho cả lớp đóng nhận bài nộp mới, kể cả khi bài cho nộp muộn. | PO-22; soát độc lập #12 |
| BR-S5E4-24 | Thu hồi Trả bài được trong 15 phút sau khi trả: bài về Đã duyệt, lời giải khoá lại, thông báo bị gỡ. Sửa điểm của bài Đã trả thì học sinh nhận thông báo "Điểm bài {tên bài} đã được thầy cô cập nhật". | soát độc lập #27; QS5-31 |
| BR-S5E4-22 | Sau khi trả: thấy điểm từng tiêu chí, tổng, nhận xét, ảnh bài của mình và lời giải; nhận thông báo trong ứng dụng MSG-S5-39. Không thấy điểm của bạn, không có thứ hạng. | PO-04; PO-22 |

- **DoR:** Ready for R3 (goal run, PO quyết); giả định ghi ở `work/s5/questions-s5.md`.

### Tiêu chí chấp nhận

**US-S5E4-07-SC1 — Trả bài cho cả lớp, học sinh xem được điểm và lời giải** · Nguồn: BR-S5E4-20, 22
- Given: BC-01: BN-01 Đã duyệt 10, BN-02 Đã duyệt 7, BN-03 Chờ chấm
- When: cô Lan gửi `POST /api/graded-assignments/BC-01/return`; Bình An gọi `GET /api/submissions/BN-01/result`
- Then: BN-01, BN-02 sang Đã trả; BN-03 vẫn Chờ chấm; Bình An thấy 5 tiêu chí, tổng 10, nhận xét và lời giải; Bình An có thông báo MSG-S5-39

**US-S5E4-07-SC2 — Lịch công bố giờ cố định mở đúng ranh giới** · Nguồn: BR-S5E4-20
- Given: BC-01 `ReleasePolicy` = 14/12/2026 07:00; BN-01 Đã duyệt
- When: Bình An gọi kết quả tại R − 1 giây; gọi lại tại R
- Then: lần 1 chỉ có trạng thái "Đang chờ thầy cô trả bài", không có điểm; lần 2 có điểm và lời giải

**US-S5E4-07-FC1 — Trước khi trả, phản hồi không chứa điểm hay dữ liệu AI** · Nguồn: BR-S5E4-21
- Given: BN-01 Đã duyệt nhưng chưa trả
- When: Bình An gọi `GET /api/submissions/BN-01` và `/result`
- Then: thân phản hồi không có trường `score`, `criteria`, `proposal`, `confidence`, `ocr`; tiêu đề `Cache-Control: no-store`

**US-S5E4-07-FC2 — Học sinh không xem được điểm của bạn sau khi trả** · Nguồn: BR-S5E4-22
- Given: BN-02 Đã trả
- When: Bình An gọi `GET /api/submissions/BN-02/result`
- Then: NOT_FOUND

**US-S5E4-07-FC3 — Em chưa nộp hoặc bài Chờ chấm không thấy lời giải sau khi lớp được trả bài** · Nguồn: BR-S5E4-23
- Given: BC-01 đã trả cho lớp; Gia Bảo chưa nộp; BN-03 của Đức Huy Chờ chấm
- When: Gia Bảo gọi `GET /api/graded-assignments/BC-01/solution`; Đức Huy gọi cùng đường dẫn
- Then: cả hai nhận trạng thái LOCKED, không có nội dung lời giải hay chuỗi `a√6/3`
- Dữ liệu nhập: F-S5-052, F-S5-055, F-S5-056

**US-S5E4-07-FC4 — Sau khi trả bài, nộp mới bị từ chối dù bài cho nộp muộn** · Nguồn: BR-S5E4-23
- Given: BC-01 cho nộp muộn, đã trả cho lớp
- When: Gia Bảo gửi `POST /api/submissions` cho BC-01
- Then: CONFLICT; không có bài nộp
- Dữ liệu nhập: F-S5-052, F-S5-056

**US-S5E4-07-SC3 — Thu hồi Trả bài trong 15 phút** · Nguồn: BR-S5E4-24
- Given: BC-01 vừa trả lúc T0; BN-01 Đã trả
- When: cô Lan gửi `POST /api/graded-assignments/BC-01/return/undo` tại T0 + 10 phút; Bình An gọi kết quả; cô Lan gửi undo lần nữa tại T0 + 16 phút (sau khi trả lại)
- Then: BN-01 về Đã duyệt; Bình An chỉ thấy "Đang chờ thầy cô trả bài", lời giải khoá; thông báo MSG-S5-39 bị gỡ; undo tại T0 + 16 phút trả CONFLICT
- Dữ liệu nhập: F-S5-052, F-S5-053; mốc T0 + 10 phút, T0 + 16 phút (biên 15 phút)

**US-S5E4-07-SC4 — Sửa điểm bài đã trả thì học sinh được báo** · Nguồn: BR-S5E4-24
- Given: BN-02 Đã trả tổng 7
- When: cô Lan sửa "Bước" thành 3,5
- Then: Chi Mai có thông báo "Điểm bài Kiểm tra 15 phút – Khoảng cách đã được thầy cô cập nhật"; kết quả hiện tổng 7,5
- Dữ liệu nhập: F-S5-054; Bước 3 → 3,5

---

## US-S5E4-08 — Lưu ảnh bài làm an toàn: mã hoá khi lưu, URL ký 5 phút, gỡ EXIF/GPS, xoá theo năm học và theo tài khoản, ảnh gửi AI
- **Story:** Là học sinh, tôi muốn ảnh bài làm của em được lưu kín, chỉ thầy cô lớp em xem trong thời gian ngắn và bị xoá đúng hạn, để em yên tâm nộp bài. · Loại Logic · P0
- **Ước lượng: dev 3 ngày · test 0,9 ngày** — gồm hạ tầng kho ảnh (bucket, khoá mã hoá, URL ký), gỡ metadata, job xoá theo năm học và theo tài khoản; là điều kiện cổng L5b.
- **Nguồn:** PO-64, PO-61; QS5-01, QS5-14, QS5-18, QS5-30; STAGES Ch5 §9 ("chốt nơi lưu ảnh"); NFR-GEO-070, 071; US-S3E5-05 (xoá tài khoản); soát độc lập #1, #3
- **Góc nhìn người vận hành:** chạy cho mọi ảnh; job xoá chạy hằng đêm; ca đáng kể: ảnh có GPS của nhà em, URL ảnh bị chép gửi ra ngoài, học sinh xoá tài khoản giữa năm, hết năm học.
- **Phụ thuộc:** kho lưu đối tượng (QS5-18); US-S3E5-05; US-S5E4-03

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S5E4-25 | Ảnh gốc và mọi phái sinh lưu ở kho đối tượng đặt tại Việt Nam, mã hoá khi lưu (AES-256, khoá do máy chủ quản lý, tách theo môi trường); không có đường dẫn công khai. | QS5-01, QS5-18 |
| BR-S5E4-26 | Ảnh chỉ trả qua URL ký hạn 5 phút, cấp cho giáo viên chủ lớp hoặc chính học sinh nộp; mỗi lần cấp kiểm lại quan hệ; URL hết hạn bị kho trả 403. | QS5-18 |
| BR-S5E4-27 | Khi nhận ảnh, máy chủ gỡ toàn bộ EXIF/XMP (GPS, thiết bị, giờ chụp) trước khi lưu ảnh gốc của hệ thống; băm tính sau khi gỡ. | soát độc lập #1 |
| BR-S5E4-28 | Xoá: (a) xoá tài khoản học sinh → trong 24 giờ xoá ảnh gốc, phái sinh, bản đọc chữ, `GradingProposal`, `CriterionAssessment` của bài em; `ApprovedGrade` giữ dạng ẩn danh cho số liệu lớp; (b) ngày 31/07 hằng năm job xoá mọi ảnh và bản đọc chữ của năm học vừa kết thúc. Mỗi lần xoá ghi `AuditEvent` số lượng, không ghi nội dung. | QS5-01, QS5-14; US-S3E5-05 |
| BR-S5E4-29 | Ảnh gửi nhà cung cấp AI (task `grade_ocr`, `grade`) chỉ là bản phái sinh đã gỡ EXIF, không kèm tên, mã người dùng, mã lớp; xử lý ở hạ tầng của nhà cung cấp (có thể ngoài Việt Nam) theo chế độ không lưu dữ liệu; MSG-S5-36 nói rõ việc này. "Lưu trong nước" áp cho lưu trữ, không áp cho xử lý AI (QS5-30). Họ tên viết tay trên giấy không tự che được, chỉ nhắc học sinh. | QS5-30; NFR-GEO-071; soát độc lập #3 |

- **DoR:** Ready for R3 (goal run, PO quyết); giả định ghi ở `work/s5/questions-s5.md`.

### Tiêu chí chấp nhận

**US-S5E4-08-SC1 — Ảnh lưu đã mã hoá và không còn EXIF/GPS** · Nguồn: BR-S5E4-25, 27
- Given: ảnh mẫu `exif-gps.jpg` có GPS 21.0285,105.8542 và model iPhone
- When: Bình An nộp ảnh đó; đọc đối tượng thô trong kho (móc e2e); tải ảnh qua URL ký
- Then: đối tượng thô trong kho không giải được thành JPEG (đã mã hoá); ảnh tải qua URL ký không có thẻ EXIF/GPS; băm lưu bằng băm ảnh đã gỡ metadata
- Dữ liệu nhập: F-S5-053; tệp `exif-gps.jpg` (GPS 21.0285,105.8542)

**US-S5E4-08-SC2 — URL ký hết hạn sau 5 phút** · Nguồn: BR-S5E4-26
- Given: cô Lan nhận URL ký của BN-01 trang 1 lúc T0
- When: tải URL lúc T0 + 299 giây; tải lại lúc T0 + 301 giây
- Then: lần 1 trả 200 kèm ảnh; lần 2 trả 403
- Dữ liệu nhập: F-S5-053; mốc T0 + 299 s, T0 + 301 s

**US-S5E4-08-FC1 — Không lấy được URL ký ảnh của bài ngoài quan hệ (IDOR)** · Nguồn: BR-S5E4-26
- Given: BN-01 của 11A1
- When: thầy Khôi gọi `GET /api/submissions/BN-01/pages/1/url`; Chi Mai gọi cùng đường dẫn
- Then: cả hai NOT_FOUND, không có URL trong thân phản hồi
- Dữ liệu nhập: F-S5-002, F-S5-053, F-S5-054

**US-S5E4-08-SC3 — Xoá tài khoản học sinh xoá ảnh, bản đọc chữ, đề xuất AI** · Nguồn: BR-S5E4-28
- Given: BN-01 có 2 ảnh, bản đọc chữ 2 phiên bản, 1 `GradingProposal`, `ApprovedGrade` 10
- When: Bình An xoá tài khoản (US-S3E5-05); chạy job xoá; kiểm kho và CSDL (móc e2e)
- Then: 0 đối tượng ảnh của BN-01 trong kho; 0 hàng `ocr_artifact`, `grading_proposal`, `criterion_assessment` của BN-01; `ApprovedGrade` còn nhưng không gắn `user_id`; `AuditEvent` ghi số đối tượng đã xoá
- Dữ liệu nhập: F-S5-053, F-S5-090

**US-S5E4-08-SC4 — Hết năm học job xoá ảnh và bản đọc chữ** · Nguồn: BR-S5E4-28
- Given: năm học 2026–2027 có 37 ảnh; đồng hồ CSDL đặt 31/07/2027 00:05
- When: chạy job xoá theo năm học
- Then: 0 ảnh và 0 bản đọc chữ của năm 2026–2027 còn lại; dữ liệu năm 2027–2028 không bị đụng; một `AuditEvent` với số lượng 37
- Dữ liệu nhập: năm học 2026–2027, 37 ảnh; mốc 31/07/2027 00:05

**US-S5E4-08-FC2 — Yêu cầu gửi AI không có EXIF và danh tính** · Nguồn: BR-S5E4-29
- Given: ảnh `exif-gps.jpg` của Bình An đã nộp; giả lập F-S5-095 ghi lại yêu cầu
- When: chờ task `grade_ocr` chạy
- Then: ảnh trong yêu cầu ở giả lập không có EXIF/GPS; yêu cầu không chứa "Bình An", `HS-4P9T-37WD`, `HK-4P7Q`
- Dữ liệu nhập: F-S5-053, F-S5-095

---

## US-S5E4-09 — Gửi lại khi mất mạng (lũy đẳng), nộp lại trước hạn, nộp muộn, tải dồn trước hạn
- **Story:** Là học sinh, tôi muốn bài đã chụp không mất khi mạng chập chờn và được nộp lại trước hạn, để không bị tính thiếu bài. · Loại Logic · P0
- **Ước lượng: dev 2 ngày · test 0,6 ngày**
- **Nguồn:** STAGES §5 luồng A ngoại lệ; soát độc lập #15 (tách từ US-S5E4-03), #16 (tải)
- **Góc nhìn người vận hành:** 8–45 em nộp dồn 30 phút trước hạn; ca đáng kể: bấm Nộp hai lần, mất mạng giữa chừng, nộp muộn 7 phút.
- **Phụ thuộc:** US-S5E4-03, 08

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S5E4-09 | Gửi kèm khóa lũy đẳng sinh trên máy; gửi lại cùng khóa không tạo bài trùng; mất mạng thì giữ bản nháp trên máy và tự gửi lại khi có mạng. | STAGES §5 A ngoại lệ |
| BR-S5E4-10 | Sau hạn: cho nộp muộn thì nhận và gắn nhãn "Nộp muộn" (giờ máy chủ); không cho thì CONFLICT. Nộp lại trước hạn tạo `SubmissionVersion` mới, bản cũ giữ. | STAGES §5 A ngoại lệ |
| BR-S5E4-30 | Tải: 45 học sinh, mỗi em 3 trang 2 MB, nộp trong 5 phút → p95 thời gian nhận một trang ≤ 3 giây, 0 lỗi 5xx. | soát độc lập #16 |

- **DoR:** Ready for R3 (goal run, PO quyết); giả định ghi ở `work/s5/questions-s5.md`.

### Tiêu chí chấp nhận

**US-S5E4-09-SC1 — Gửi lại cùng khóa lũy đẳng không tạo bài trùng** · Nguồn: BR-S5E4-09
- Given: BN-01 vừa nộp với khóa `k-01`
- When: gửi lại `POST /api/submissions` cùng khóa `k-01`
- Then: trả 200 với id BN-01; BC-01 có đúng 1 bài nộp của Bình An
- Dữ liệu nhập: F-S5-053; khoá lũy đẳng `k-01`

**US-S5E4-09-SC2 — Nộp muộn có nhãn khi bài cho nộp muộn** · Nguồn: BR-S5E4-10
- Given: BC-01 cho nộp muộn; giờ máy chủ 11/12/2026 20:07
- When: Hà My nộp BN-05
- Then: BN-05 trạng thái Đã nộp, nhãn "Nộp muộn 7 phút"
- Dữ liệu nhập: F-S5-056; mốc 11/12/2026 20:07 (biên: 20:00:00 đúng hạn, 20:00:01 muộn)

**US-S5E4-09-SC3 — Nộp lại trước hạn tạo phiên bản mới, bản cũ giữ** · Nguồn: BR-S5E4-10
- Given: BN-01 phiên bản 1
- When: Bình An nộp lại 3 trang trước hạn
- Then: BN-01 phiên bản 2 là bản chấm; phiên bản 1 vẫn đọc được bởi giáo viên; ảnh của phiên bản 1 không bị xoá
- Dữ liệu nhập: F-S5-053; 3 tệp 1,5 MB

**US-S5E4-09-FC1 — Mất mạng khi nộp: giữ nháp, tự gửi lại** · Nguồn: BR-S5E4-09
- Given: Bình An đã chụp 2 trang; mạng tắt (giả lập offline của trình duyệt)
- When: bấm Nộp; bật lại mạng
- Then: sau bước 1 hiện MSG-S5-35, bài lưu trên máy; sau bước 2 bài tự gửi, đúng 1 bài nộp trên máy chủ
- Dữ liệu nhập: F-S5-053

**US-S5E4-09-FC2 — Quá hạn khi bài không cho nộp muộn** · Nguồn: BR-S5E4-10
- Given: BC-01 không cho nộp muộn; giờ máy chủ sau hạn 1 giây
- When: Gia Bảo nộp
- Then: CONFLICT, MSG-S5-34; không có bài nộp
- Dữ liệu nhập: F-S5-056; mốc 11/12/2026 20:00:01

**US-S5E4-09-FC3 — 45 em nộp dồn trong 5 phút** · Nguồn: BR-S5E4-30
- Given: lớp thử 45 học sinh Đang học, BC-01 đã giao
- When: chạy kịch bản tải: 45 em × 3 trang 2 MB trong 5 phút
- Then: p95 nhận một trang ≤ 3 giây; 0 phản hồi 5xx; đúng 45 bài nộp, 135 trang
- Dữ liệu nhập: 45 tài khoản HS thử; 135 tệp 2 MB

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-28 | Tạo story, BR, AC và ước lượng chặng 5 (goal run) | agent spec-s5 (PO → BA → QC) |
| v0.2 | 2026-11-28 | Sửa theo `work/s5/review-independent.md` (27 lỗi) | agent spec-s5 |
