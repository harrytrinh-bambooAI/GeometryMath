Phiên bản: v0.2 · 2026-11-28
# S5-E2 — Ra đề theo ma trận và trợ lý soạn bộ đề (chọn loại đề và phạm vi, lắp đề, nhiều mã đề, xuất): story Dev-Ready và tiêu chí chấp nhận

> Chủ sở hữu: BA (goal run, PO quyết cao nhất). Nguồn: `PRODUCT-MASTER.md` (22/11/2026) D1 dòng 5 ("trợ lý soạn … bộ đề"), C1b "Trợ lý giáo viên"; `STAGES-AFTER-MVP.md` Chặng 5 §5 luồng D, §6 C5-08; DECISIONS 27/11 "Dữ liệu ĐỀ là miền riêng"; tài sản dùng lại nguyên trạng: `materials/de-thi/README.md` v1.0 (bảng trường form), `lap_de.py`, `ma-tran/` (4 ma trận + `yccd.json`), `mau-cau/` (110 mẫu, `engine-values.json`), `ngan-hang/` (1 774 câu), `schema/`, `check.py`.
> **Trạng thái: ready for R3 (goal run)**; mức epic **P0**. Giả định đang dùng và câu hỏi cho owner ở `work/s5/questions-s5.md`; tự soát ở `work/s5/review-s5.md`.

**Mục tiêu epic.** Giáo viên chọn loại đề, lớp, chương và vài thông số trên form, nhận các mã đề tương đương lắp từ ngân hàng câu có đáp số engine, thay được từng câu, duyệt và xuất đề, đáp án, hướng dẫn chấm, bảng đặc tả; trợ lý soạn thêm đề nháp từ tài liệu của thầy cô, chỉ vào kho khi duyệt.

**Điều chỉnh khung so với brief.** Thêm US-S5E2-01 (nạp dữ liệu đề vào máy chủ có kiểm) vì ngân hàng hiện chỉ là tệp JSON ngoài app; thêm US-S5E2-06 (trợ lý soạn đề nháp từ tài liệu, luồng D của STAGES) vì D1 dòng 5 ghi "trợ lý soạn bộ đề" là P0. Lắp đề dùng thuật toán và ràng buộc của `lap_de.py` (giả định QS5-06: chuyển sang Java trong khối chính, giữ `check.py` làm bộ kiểm đối chứng).

**Ước lượng epic:** dev 14 ngày · test 4,1 ngày · tổng 18,1 ngày công (6 story; bảng chung ở `work/s5/estimate-s5.md`).

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
| MSG-S5-10 | Inline | "Tổng điểm phải bằng 10." | VALIDATION_FAILED |
| MSG-S5-11 | Inline | "Tổng thời gian dự kiến của các câu vượt thời gian làm bài." | VALIDATION_FAILED |
| MSG-S5-12 | Page state | "Ngân hàng chưa đủ câu cho ô {ô}. Thầy cô bỏ bớt ràng buộc hoặc giảm số mã đề." | CONFLICT |
| MSG-S5-13 | Inline | "Số mã đề từ 1 đến 8." | VALIDATION_FAILED |
| MSG-S5-14 | Toast | "Đề nháp chưa qua kiểm chứng: không chọn sẵn, đặt cuối danh sách." | — |
| MSG-S5-15 | Page state | "Chưa có tài liệu Đang dùng nào để soạn đề." | CONFLICT |

---

## US-S5E2-01 — Nạp ma trận chuẩn, mẫu câu và ngân hàng câu vào máy chủ, kiểm schema và đáp số
- **Story:** Là người vận hành, tôi muốn nạp bốn ma trận chuẩn, 110 mẫu câu và 1 774 câu vào máy chủ qua một lệnh có kiểm, để bộ lắp đề chỉ dùng câu đã khớp đáp số engine. · Loại Logic · P0
- **Ước lượng: dev 2 ngày · test 0,6 ngày**
- **Nguồn:** `de-thi/README.md` mục Nội dung, Kiểm 1–5; `schema/` (cau, mau_cau, ma_tran, de); PO-54 (migration chỉ chứa dữ liệu tham chiếu)
- **Góc nhìn người vận hành:** chạy một lần mỗi khi dữ liệu đề đổi phiên bản; ca đáng kể: chạy lại (lũy đẳng), một câu lệch đáp số, ma trận tổng điểm khác 10.
- **Phụ thuộc:** module `content`; engine (`geometry`) để kiểm lại mẫu MVP

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S5E2-01 | Lệnh nạp đọc `ma-tran/*.json`, `mau-cau/*.json`, `ngan-hang/*.json`, kiểm theo `schema/`; bản ghi gắn `phien_ban_du_lieu` (ví dụ `de-thi v1.0`); chạy lại cùng phiên bản không tạo bản trùng. | README "Cách dùng"; PO-54 |
| BR-S5E2-02 | Câu từ mẫu MVP chỉ được nạp khi đáp số khớp engine trong 1e-6; câu lệch bị loại và ghi báo cáo, lệnh vẫn nạp phần còn lại. | README "Cách sinh và kiểm"; Kiểm 3 |
| BR-S5E2-03 | Ma trận chỉ được nạp khi tổng điểm = 10 và trọng số mỗi nhóm ô xoay vòng cộng = 1. | README Kiểm 5 |

- **DoR:** Ready for R3 (goal run, PO quyết); giả định ghi ở `work/s5/questions-s5.md`.

### Tiêu chí chấp nhận

**US-S5E2-01-SC1 — Nạp đủ dữ liệu đề v1.0** · Nguồn: BR-S5E2-01
- Given: CSDL thử chưa có dữ liệu đề
- When: chạy lệnh nạp với `de-thi v1.0`; gọi `GET /api/exam/catalog`
- Then: báo cáo nạp: 4 ma trận, 110 mẫu, 1 774 câu, 0 lỗi; danh mục trả 4 loại đề KT15-HHKG11, GK2-L11, CK2-L11, TN-THPT-764

**US-S5E2-01-SC2 — Chạy lại cùng phiên bản là lũy đẳng** · Nguồn: BR-S5E2-01
- Given: đã nạp `de-thi v1.0`
- When: chạy lại lệnh nạp
- Then: số bản ghi không đổi (4 / 110 / 1 774); báo cáo "0 thêm, 0 đổi"

**US-S5E2-01-FC1 — Câu lệch đáp số engine bị loại, phần còn lại vẫn nạp** · Nguồn: BR-S5E2-02
- Given: bản sao ngân hàng có 1 câu HHKG sửa đáp số thành 0,8200 (engine 0,8165)
- When: chạy lệnh nạp
- Then: báo cáo 1 lỗi nêu id câu và hai giá trị; 1 773 câu được nạp; câu lỗi không có trong ngân hàng máy chủ
- Dữ liệu nhập: F-S5-032; 1 câu HHKG sửa đáp số 0,8200 (engine 0,8165, sai số > 1e-6)

**US-S5E2-01-FC2 — Ma trận tổng điểm khác 10 bị từ chối** · Nguồn: BR-S5E2-03
- Given: bản sao `KT15-HHKG11` có tổng điểm 9
- When: chạy lệnh nạp
- Then: ma trận đó không được nạp, báo cáo nêu "tổng điểm 9 ≠ 10"; ba ma trận còn lại được nạp

---

## US-S5E2-02 — Form ra đề: loại đề, lớp, chương, thời gian, số câu và điểm, tỉ lệ mức độ, số mã đề
- **Story:** Là giáo viên, tôi muốn chọn loại đề và phạm vi chương trên một form, để có đề đúng khung của trường mà không phải dựng ma trận từ đầu. · Loại UI · P0
- **Ước lượng: dev 2 ngày · test 0,5 ngày**
- **Nguồn:** `de-thi/README.md` bảng "Trường của bộ lắp đề" (form / tùy chọn / cố định); `STAGES` Ch5 §6 C5-08
- **Góc nhìn người vận hành:** cô Lan ra đề 15 phút chương Quan hệ vuông góc cho 11A1, và đề thi thử cho lớp 12 cuối năm; ca đáng kể: bỏ một chương, đổi điểm làm tổng khác 10, xin 10 mã đề.
- **Phụ thuộc:** US-S5E2-01

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S5E2-04 | Trường form: loại đề (ma trận chuẩn), lớp, chương (tick), thời gian (phút), số câu và điểm mỗi phần, tỉ lệ NB/TH/VD/VDC (thanh trượt), số mã đề 1–8 (QS5-07); trường tùy chọn: seed, xem YCCĐ từng ô; trường cố định (cấu trúc QĐ 764, tổng điểm 10, đảo câu và phương án) không hiện ô sửa. | README bảng trường |
| BR-S5E2-05 | Bỏ một chương thì các ô của chương đó chuyển sang chương còn lại cùng loại câu; máy chủ kiểm tổng điểm = 10 và tổng thời gian dự kiến ≤ thời gian làm bài. | README bảng trường |

- **DoR:** Ready for R3 (goal run, PO quyết); giả định ghi ở `work/s5/questions-s5.md`.

### Tiêu chí chấp nhận

**US-S5E2-02-SC1 — Chọn đề 15 phút, form điền theo ma trận chuẩn** · Nguồn: BR-S5E2-04
- Given: cô Lan mở "Ra đề theo ma trận"
- When: chọn loại đề "Kiểm tra 15 phút – Quan hệ vuông góc" (F-S5-030)
- Then: form điền thời gian 15, phần TN4 6 câu × 1,0 và trả lời ngắn 2 câu × 2,0, tổng 10; không có ô sửa cấu trúc cố định; số mã đề mặc định 4

**US-S5E2-02-SC2 — Bỏ một chương thì ô chuyển sang chương còn lại** · Nguồn: BR-S5E2-05
- Given: ma trận `GK2-L11` (mũ-lôgarit và quan hệ vuông góc)
- When: gửi `POST /api/exams/preview` bỏ chương mũ-lôgarit
- Then: bảng ô trả về chỉ có chủ đề quan hệ vuông góc; số câu mỗi loại không đổi; tổng điểm vẫn 10

**US-S5E2-02-FC1 — Tổng điểm khác 10 bị từ chối** · Nguồn: BR-S5E2-05
- Given: ma trận F-S5-030
- When: gửi preview với điểm câu trả lời ngắn 1,5
- Then: VALIDATION_FAILED, MSG-S5-10

**US-S5E2-02-FC2 — Thời gian dự kiến vượt thời gian làm bài bị từ chối** · Nguồn: BR-S5E2-05
- Given: ma trận F-S5-030
- When: gửi preview với thời gian 8 phút
- Then: VALIDATION_FAILED, MSG-S5-11

**US-S5E2-02-FC3 — Số mã đề 9 bị từ chối** · Nguồn: BR-S5E2-04
- Given: ma trận F-S5-030
- When: gửi với số mã đề 9
- Then: VALIDATION_FAILED, MSG-S5-13

---

## US-S5E2-03 — Lắp nhiều mã đề tương đương theo ràng buộc của `lap_de.py`, tái lập theo seed
- **Story:** Là giáo viên, tôi muốn nhận nhiều mã đề tương đương về độ khó và phủ đúng đặc tả, để học sinh ngồi cạnh nhau không chép được bài mà đề vẫn công bằng. · Loại Logic · P0
- **Ước lượng: dev 3 ngày · test 0,9 ngày** — chuyển thuật toán bốc và 7 ràng buộc sang Java, dùng `check.py` (Kiểm 6) làm bộ kiểm đối chứng độc lập trên cùng seed.
- **Nguồn:** `lap_de.py`; README bảng trường (ràng buộc cố định), Kiểm 6; QS5-06
- **Góc nhìn người vận hành:** chạy trong vài giây khi bấm "Lắp đề"; ca đáng kể: ngân hàng mỏng (lượng giác 1 mẫu) không đủ câu, seed giữ để in lại, bốc lại tới 600 lần/mã.
- **Phụ thuộc:** US-S5E2-01, 02

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S5E2-06 | Ràng buộc mỗi mã: phủ đặc tả 100 %; khớp ô (chủ đề, dạng, YCCĐ, loại, mức độ, điểm); không trùng dạng; A/B/C/D lệch ≤ 1; không hai câu chung bối cảnh, chuỗi đáp số không xuất hiện trong đề câu khác; độ khó các mã lệch ≤ 5 %, với độ khó dự kiến của mã = Σ(điểm câu × `do_kho` câu) / tổng điểm (`do_kho` = tỉ lệ làm sai dự kiến trong ngân hàng) và độ lệch = (lớn nhất − nhỏ nhất) / trung bình các mã, đúng `lap_de.py` (`do_kho_de`, `lech_do_kho`). Bốc lại tối đa 600 lần/mã rồi báo không đủ. Lắp 8 mã ≤ 5 giây. | README ràng buộc; Kiểm 6 |
| BR-S5E2-07 | Cùng ma trận, cùng tham số form và cùng seed cho ra đúng các mã đề như lần trước (tái lập); đảo thứ tự câu trong phần và đảo phương án luôn bật. | README "seed giữ để in lại" |
| BR-S5E2-08 | Đáp số và đáp án chữ của mỗi câu lấy từ ngân hàng (engine hoặc sympy có phép kiểm), không gọi AI ở bước lắp. | C2 nguyên tắc 13; README |

- **DoR:** Ready for R3 (goal run, PO quyết); giả định ghi ở `work/s5/questions-s5.md`.

### Tiêu chí chấp nhận

**US-S5E2-03-SC1 — Lắp 4 mã đề 15 phút đạt mọi ràng buộc** · Nguồn: BR-S5E2-06, 08
- Given: dữ liệu F-S5-032 đã nạp
- When: gửi `POST /api/exams` ma trận F-S5-030, 4 mã, seed 2026
- Then: trả 4 mã đề, mỗi mã 8 câu, tổng điểm 10; bộ kiểm đối chứng (`check.py` Kiểm 6) đạt cho cả 4 mã; `AiCallLog` không có dòng mới

**US-S5E2-03-SC2 — Cùng seed cho ra cùng bộ đề** · Nguồn: BR-S5E2-07
- Given: đã lắp bộ đề ở SC1
- When: gửi lại cùng yêu cầu với seed 2026
- Then: danh sách id câu và thứ tự phương án của 4 mã trùng khớp lần trước
- Dữ liệu nhập: F-S5-030, F-S5-032; 4 mã, seed 2026 (chạy hai lần)

**US-S5E2-03-SC3 — Đề TN THPT đúng cấu trúc QĐ 764** · Nguồn: BR-S5E2-06
- Given: dữ liệu F-S5-032
- When: lắp ma trận F-S5-031, 2 mã, seed 2026
- Then: mỗi mã có 12 TN4, 4 đúng/sai, 6 trả lời ngắn (đáp án ≤ 4 ký tự); A/B/C/D lệch ≤ 1; độ khó hai mã lệch ≤ 5 %

**US-S5E2-03-SC4 — Lắp đủ 8 mã trong ≤ 5 giây, độ khó lệch ≤ 5 %** · Nguồn: BR-S5E2-06; QS5-07
- Given: dữ liệu F-S5-032; ma trận F-S5-030
- When: gửi `POST /api/exams` 8 mã, seed 2026; đo thời gian phản hồi
- Then: trả 8 mã (101–108), mỗi mã đạt bộ kiểm đối chứng; `lech_do_kho` = (max − min) / trung bình ≤ 0,05; thời gian ≤ 5 giây trên máy CI chuẩn
- Dữ liệu nhập: F-S5-030, F-S5-032; 8 mã, seed 2026 (biên trên QS5-07)

**US-S5E2-03-FC1 — Ngân hàng không đủ câu thì báo ô thiếu, không trả đề thiếu** · Nguồn: BR-S5E2-06
- Given: ma trận CK2-L11 chỉ giữ chương lượng giác (1 mẫu)
- When: lắp 8 mã
- Then: CONFLICT, MSG-S5-12 nêu ô thiếu; không có mã đề nào được lưu

**US-S5E2-03-FC2 — Giáo viên không lắp đề cho lớp không phải của mình** · Nguồn: quy ước chung
- Given: lớp 10B của thầy Khôi
- When: cô Lan gửi `POST /api/exams` với `classId` của 10B
- Then: NOT_FOUND

---

## US-S5E2-04 — Xem và sửa bộ đề nháp: thay một câu cùng ô, nhãn hai trục, duyệt vào kho "Của tôi"
- **Story:** Là giáo viên, tôi muốn thay câu tôi không ưng bằng câu khác cùng ô rồi duyệt cả bộ, để đề là của tôi chứ không phải của máy. · Loại UI · P0
- **Ước lượng: dev 2 ngày · test 0,6 ngày**
- **Nguồn:** `STAGES` Ch5 §5 luồng D bước 2; PO-02, PO-30; README ràng buộc
- **Góc nhìn người vận hành:** cô Lan đọc từng mã, thay 1–2 câu trùng bối cảnh với bài đã giao; ca đáng kể: thay câu làm lệch độ khó quá 5 %, duyệt rồi sửa.
- **Phụ thuộc:** US-S5E2-03

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S5E2-09 | Thay câu: máy chủ bốc câu khác cùng ô (cùng chủ đề, dạng, mức độ, loại, điểm) rồi kiểm lại ràng buộc của cả mã; vi phạm thì bốc lại, hết lượt thì giữ câu cũ và báo. | BR-S5E2-06 |
| BR-S5E2-10 | Bộ đề do máy lắp từ ngân hàng mang nhãn người "Máy lắp" cho tới khi giáo viên duyệt thành "Giáo viên đã duyệt"; nhãn máy theo từng câu. | PO-02 (bổ sung giá trị "Máy lắp", QS5-08) |
| BR-S5E2-11 | Chỉ bộ đề đã duyệt mới vào ngăn "Của tôi" và mới giao hoặc gắn rubric được (US-S5E4-02). | STAGES Ch5 §5 D; C2 nguyên tắc 4 |

- **DoR:** Ready for R3 (goal run, PO quyết); giả định ghi ở `work/s5/questions-s5.md`.

### Tiêu chí chấp nhận

**US-S5E2-04-SC1 — Thay một câu cùng ô** · Nguồn: BR-S5E2-09
- Given: bộ đề 4 mã từ US-S5E2-03 SC1
- When: mở mã 101; bấm "Thay câu" ở câu 3
- Then: câu 3 mới cùng chủ đề, dạng, mức độ và điểm; các câu khác không đổi; nhãn "Đã kiểm chứng" của câu mới hiện cạnh câu

**US-S5E2-04-SC2 — Duyệt bộ đề vào "Của tôi"** · Nguồn: BR-S5E2-10, 11
- Given: bộ đề nháp của cô Lan
- When: gửi `POST /api/exams/{id}/approve`
- Then: nhãn người "Giáo viên đã duyệt", người duyệt, giờ; bộ đề có trong `GET /api/exams?shelf=mine`

**US-S5E2-04-FC1 — Hết lượt bốc thì giữ câu cũ và báo** · Nguồn: BR-S5E2-09
- Given: ô của câu 3 chỉ có 1 câu trong ngân hàng
- When: gửi thay câu 3
- Then: CONFLICT, MSG-S5-12 nêu ô; câu 3 giữ nguyên
- Dữ liệu nhập: F-S5-030, F-S5-032; ô câu 3 còn 1 câu; 600 lần bốc

**US-S5E2-04-FC2 — Bộ đề chưa duyệt không giao được** · Nguồn: BR-S5E2-11
- Given: bộ đề nháp chưa duyệt
- When: gửi tạo bài có chấm (US-S5E4-02) với bộ đề này
- Then: CONFLICT; không có bài nào được tạo
- Dữ liệu nhập: F-S5-030, F-S5-051, F-S5-010; bộ đề trạng thái Nháp

---

## US-S5E2-05 — Xuất mã đề: đề, đáp án, hướng dẫn chấm, bảng đặc tả (DOCX, PDF)
- **Story:** Là giáo viên, tôi muốn tải đề, đáp án, hướng dẫn chấm và bảng đặc tả của từng mã, để in cho lớp và nộp tổ chuyên môn. · Loại UI · P0
- **Ước lượng: dev 2 ngày · test 0,6 ngày**
- **Nguồn:** `lap_de.py` đầu ra Markdown (đề, đáp án, hướng dẫn chấm, bảng đặc tả); `de-mau/TN-THPT-764/ma-101.md`; PO-22 (giờ mở lời giải cho mọi kênh); QS5-05
- **Góc nhìn người vận hành:** cô Lan in đề trước buổi kiểm tra; ca đáng kể: học sinh có tài khoản cố tải đáp án, xuất bộ chưa duyệt.
- **Phụ thuộc:** US-S5E2-04; US-S5E1-05 (dùng chung bộ dựng DOCX/PDF)

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S5E2-12 | Mỗi mã xuất 4 phần: Đề (không đáp án), Đáp án, Hướng dẫn chấm (tự luận cộng đủ điểm), Bảng đặc tả (ô, YCCĐ, mức độ, điểm); có thể xuất gộp một tệp nén. | `lap_de.py`; README Kiểm 6 |
| BR-S5E2-13 | Chỉ giáo viên chủ bộ đề xuất được Đáp án và Hướng dẫn chấm; vai học sinh luôn FORBIDDEN, kể cả sau giờ mở lời giải (bài nộp dùng luồng trả bài US-S5E4-07). | PO-22; NFR-GEO-020 |

- **DoR:** Ready for R3 (goal run, PO quyết); giả định ghi ở `work/s5/questions-s5.md`.

### Tiêu chí chấp nhận

**US-S5E2-05-SC1 — Xuất 4 phần của mã 101** · Nguồn: BR-S5E2-12
- Given: bộ đề đã duyệt
- When: gửi `GET /api/exams/{id}/codes/101/export?format=docx&parts=all`
- Then: tệp nén có 4 tệp: đề, đáp án, hướng dẫn chấm, bảng đặc tả; tệp đề không chứa chuỗi đáp án nào của mã đó; điểm trong hướng dẫn chấm cộng đủ 10

**US-S5E2-05-SC2 — Xuất PDF chỉ phần đề cho cả 4 mã** · Nguồn: BR-S5E2-12
- Given: bộ đề đã duyệt
- When: gửi export `format=pdf&parts=de` cho cả 4 mã
- Then: 4 tệp PDF, mỗi tệp ghi mã đề ở đầu trang; không có trang đáp án

**US-S5E2-05-FC1 — Học sinh không tải được đáp án** · Nguồn: BR-S5E2-13
- Given: bộ đề đã duyệt, đã giao 11A1
- When: Bình An gọi export `parts=dap-an`
- Then: FORBIDDEN; thân phản hồi không có nội dung đề hay đáp án

**US-S5E2-05-FC2 — Bộ đề nháp xuất được nhưng có chân trang nháp** · Nguồn: BR-S5E1-14
- Given: bộ đề chưa duyệt
- When: xuất `parts=de`
- Then: mọi trang có chân trang "Bản nháp – chưa duyệt"

---

## US-S5E2-06 — Trợ lý soạn đề nháp từ tài liệu Đang dùng của giáo viên (luồng D)
- **Story:** Là giáo viên, tôi muốn trợ lý soạn vài đề nháp từ ghi chú và phiếu của tôi, có đáp số máy tính và nhãn rõ, để bổ sung ngân hàng bằng dạng bài lớp tôi đang học. · Loại Logic · P0
- **Ước lượng: dev 3 ngày · test 0,9 ngày** — task `exam_draft` mới + nối vào đường kiểm chứng sẵn có của S2-E3; phần chấm nhãn và sắp xếp nhỏ.
- **Nguồn:** `STAGES` Ch5 §5 luồng D, §6 C5-08; C1b "Trợ lý giáo viên … có nhãn kiểm chứng trên từng bài. Chỉ vào kho đề khi thầy cô duyệt"; dùng lại đường bốn bước đọc–dựng–giải–kiểm của S2-E3 (US-S2E3-03, 04)
- **Góc nhìn người vận hành:** cô Lan chọn `TL-01` và lớp 11A1, xin 5 đề; ca đáng kể: đề AI soạn mà engine không dựng được, tài liệu chưa duyệt, hết hạn mức.
- **Phụ thuộc:** US-S5E3-01 (kho tri thức); S2-E3 (đường kiểm chứng đề); US-S5E1-02 BR-S5E1-08 (hạn mức)

| BR | Quy tắc | Nguồn |
|---|---|---|
| BR-S5E2-14 | Nguồn chỉ là tài liệu Đang dùng của chính giáo viên (US-S5E3-01); tài liệu Nháp hoặc Chờ duyệt không chọn được. | STAGES Ch5 §7; PO-30 |
| BR-S5E2-15 | Mỗi đề nháp qua đường đọc–dựng–giải–kiểm: dựng được và đáp số khớp engine thì nhãn máy "Đã kiểm chứng" và chọn sẵn; không thì "Cần xem lại", đặt cuối, không chọn sẵn. Đáp số luôn từ engine. | STAGES luồng D ngoại lệ; US-S2E3-04 |
| BR-S5E2-16 | Đề nháp nhãn người "AI soạn" kèm nguồn (tài liệu, phiên bản, mục); chỉ vào ngân hàng "Của tôi" khi giáo viên bấm Duyệt từng đề. | PO-02, PO-30 |

- **DoR:** Ready for R3 (goal run, PO quyết); giả định ghi ở `work/s5/questions-s5.md`.

### Tiêu chí chấp nhận

**US-S5E2-06-SC1 — Soạn 5 đề nháp từ TL-01, đề đạt kiểm chứng chọn sẵn** · Nguồn: BR-S5E2-15, 16
- Given: kho F-S5-040; giả lập `exam_draft` trả 5 đề, 4 đề engine dựng được
- When: gửi `POST /api/copilot/exam-drafts` với `TL-01` v2, lớp 11A1, 5 đề
- Then: 5 đề nháp nhãn "AI soạn", mỗi đề ghi nguồn "TL-01 v2"; 4 đề "Đã kiểm chứng" chọn sẵn; 1 đề "Cần xem lại" ở cuối, không chọn sẵn, MSG-S5-14

**US-S5E2-06-SC2 — Duyệt từng đề đưa vào ngân hàng "Của tôi"** · Nguồn: BR-S5E2-16
- Given: 5 đề nháp ở SC1
- When: duyệt 2 đề "Đã kiểm chứng"
- Then: 2 đề vào ngân hàng của cô Lan, nhãn "Giáo viên đã duyệt"; 3 đề còn lại vẫn là nháp; đáp số 2 đề bằng giá trị engine

**US-S5E2-06-FC1 — Tài liệu chưa Đang dùng không làm nguồn được** · Nguồn: BR-S5E2-14
- Given: `TL-02` Chờ duyệt
- When: gửi soạn đề với `TL-02`
- Then: CONFLICT, MSG-S5-15; không gọi AI

**US-S5E2-06-FC2 — Tài liệu của giáo viên khác không làm nguồn được** · Nguồn: BR-S5E2-14
- Given: `TL-09` của thầy Khôi (F-S5-041)
- When: cô Lan gửi soạn đề với `TL-09`
- Then: NOT_FOUND; không gọi AI

**US-S5E2-06-FC4 — Hết 30 lượt soạn trong ngày thì không soạn đề nháp** · Nguồn: BR-S5E1-08; BR-S5E5-12
- Given: cô Lan đã dùng 30 lượt soạn hôm nay
- When: gửi soạn đề với `TL-01`
- Then: RATE_LIMITED, MSG-S5-06; không gọi AI
- Dữ liệu nhập: F-S5-001, F-S5-040; bộ đếm 30/30

**US-S5E2-06-FC3 — AI ghi đáp số khác engine thì đáp số engine thắng, đề "Cần xem lại"** · Nguồn: BR-S5E2-15
- Given: giả lập trả 1 đề ghi đáp số "a√3/3" trong khi engine tính `a√6/3`
- When: gửi soạn đề
- Then: đề đó nhãn "Cần xem lại", ghi rõ "đáp số AI khác máy tính"; ô đáp số hiện giá trị engine

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-28 | Tạo story, BR, AC và ước lượng chặng 5 (goal run) | agent spec-s5 (PO → BA → QC) |
| v0.2 | 2026-11-28 | Sửa theo `work/s5/review-independent.md` (27 lỗi) | agent spec-s5 |
