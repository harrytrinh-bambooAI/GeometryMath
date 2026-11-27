Phiên bản: v0.2 · 2026-11-28
# Ước lượng chặng 5 · GEO · Đánh giá và trợ lý giáo viên

> Chủ sở hữu: PO (goal run). Bảng số được sinh cùng lúc với các tệp `epics/S5-E*/stories_ac.md` từ một nguồn; `check_s5.py` kiểm khớp từng dòng. Phạm vi và thứ tự làm ở `brief-s5.md`; giả định ở `questions-s5.md`.

## 1. Luật và đơn vị
- **Ngày công** = một người làm 8 giờ. Giả định đội **1 dev full-stack** (Java Spring + React) làm cả dev lẫn test của story mình, nên người-ngày = dev + test.
- **Dev** của story gồm: migration và bảng, API, giao diện, dữ liệu giả lập `MockAiProvider` cho ca của story, test đơn vị và tích hợp của tầng mình, sửa sau review code. Mỗi story dev 1–3 ngày; story lớn đã tách, story nhỏ đã gộp (cỡ thực tế: 2–3 ngày, trung bình 2,38). Spike 0,5 ngày của phần kỹ thuật mới (camera PWA iOS, tìm kiếm toàn văn tiếng Việt) đã nằm trong dev của story tương ứng sau khi tách (US-S5E4-03, US-S5E3-07).
- **Test** của story gồm: viết và chạy các case E2E-API / E2E-UI trong `test_cases.md`, ghi kết quả, sửa dữ liệu thử. Test ≤ 30 % dev: 0,9 ngày cho story 3 ngày, 0,5–0,6 ngày cho story 2 ngày (story form thuần 0,5).
- **Không tính** trong bảng: thiết kế màn (Designer), soát độc lập, bench AI thật ngoài CI, triển khai, vận hành pilot, đào tạo giáo viên. Các khoản này ở mục 4.

## 2. Tóm tắt theo epic và toàn chặng

<!-- SUM:BEGIN -->
| Epic | Tên | Số US | Số AC | Số TC | Dev (ngày) | Test (ngày) | Tổng (ngày công) | Người-ngày (1 dev) |
|---|---|---|---|---|---|---|---|---|
| S5-E1 | Trợ lý soạn giáo án (form B/C, chat vá bản nháp, máy chủ kiểm luật A, xuất file) | 5 | 31 | 31 | 12 | 3,5 | 15,5 | 15,5 |
| S5-E2 | Ra đề theo ma trận và trợ lý soạn bộ đề (chọn loại đề và phạm vi, lắp đề, nhiều mã đề, xuất) | 6 | 29 | 29 | 14 | 4,1 | 18,1 | 18,1 |
| S5-E3 | Kho tri thức của giáo viên, hỏi đáp của học sinh theo kho, kiểm soát đáp án đầy đủ | 7 | 42 | 42 | 16 | 4,8 | 20,8 | 20,8 |
| S5-E4 | Rubric, giao bài có chấm, nộp bài viết tay (ảnh, OCR), chấm tay và trả bài (đợt bật 1) | 9 | 51 | 51 | 21 | 6,3 | 27,3 | 27,3 |
| S5-E5 | Thông báo do trợ lý soạn, tổng hợp lỗi sai của lớp (COP-02), công tắc và hạn mức AI của lớp | 4 | 29 | 29 | 10 | 3 | 13 | 13 |
| S5-E6 | AI đề xuất điểm theo rubric và KPI, engine làm tín hiệu thứ hai, chạy song song, giáo viên duyệt (đợt bật 2–3) | 6 | 37 | 37 | 15 | 4,5 | 19,5 | 19,5 |
| **Toàn chặng** | | **37** | **219** | **219** | **88** | **26,2** | **114,2** | **114,2** |
<!-- SUM:END -->

Với 1 dev: khoảng **23 tuần làm việc** (5 ngày/tuần) cho phần đã đặc tả, chưa gồm dự phòng.

**Thay đổi v0.1 → v0.2 (sửa theo `review-independent.md`):** +3 story, +10,4 người-ngày (103,8 → 114,2). Tách US-S5E4-03 thành 03 (chụp, độ rõ, HEIC) + 09 (gửi lại lũy đẳng, nộp lại, nộp muộn, tải); thêm US-S5E4-08 (kho ảnh mã hoá, URL ký 5 phút, gỡ EXIF, xoá theo năm học và tài khoản); tách US-S5E3-03 thành 03 (hỏi đáp có nguồn) + 07 (bộ truy xuất); US-S5E6-06 2 → 3 ngày (đủ KPI §2–§4, KTC, đóng/mở cổng); US-S5E5-04 2 → 3 ngày (định nghĩa lượt, hạn mức học sinh và token, tắt khẩn toàn hệ thống, báo cáo trợ lý).

## 3. Chi tiết epic × story

<!-- TBL:BEGIN -->
| Epic | Story | Tiêu đề | Dev (ngày) | Test (ngày) | Tổng (ngày) |
|---|---|---|---|---|---|
| S5-E1 | US-S5E1-01 | Form "Soạn giáo án mới": trường B bắt buộc, trường C tuỳ chọn, tích sẵn theo Tiết PPCT | 2 | 0,5 | 2,5 |
| S5-E1 | US-S5E1-02 | Máy chủ dựng tin nhắn, gọi task `lesson_plan` và kiểm tất định luật A (gọi lại một lần) | 3 | 0,9 | 3,9 |
| S5-E1 | US-S5E1-03 | Màn kết quả: dựng giáo án theo `kind`, nhãn "AI chưa chắc", khung đề xuất, câu hỏi lại | 2 | 0,6 | 2,6 |
| S5-E1 | US-S5E1-04 | Chat vá bản nháp: chọn mục, bản vá trước → sau, nhận từng dòng hoặc cả bản, khoá mục, hoàn tác | 3 | 0,9 | 3,9 |
| S5-E1 | US-S5E1-05 | Duyệt và xuất giáo án (DOCX, PDF), danh sách "Giáo án của tôi" | 2 | 0,6 | 2,6 |
| **S5-E1 cộng** | | | **12** | **3,5** | **15,5** |
| S5-E2 | US-S5E2-01 | Nạp ma trận chuẩn, mẫu câu và ngân hàng câu vào máy chủ, kiểm schema và đáp số | 2 | 0,6 | 2,6 |
| S5-E2 | US-S5E2-02 | Form ra đề: loại đề, lớp, chương, thời gian, số câu và điểm, tỉ lệ mức độ, số mã đề | 2 | 0,5 | 2,5 |
| S5-E2 | US-S5E2-03 | Lắp nhiều mã đề tương đương theo ràng buộc của `lap_de.py`, tái lập theo seed | 3 | 0,9 | 3,9 |
| S5-E2 | US-S5E2-04 | Xem và sửa bộ đề nháp: thay một câu cùng ô, nhãn hai trục, duyệt vào kho "Của tôi" | 2 | 0,6 | 2,6 |
| S5-E2 | US-S5E2-05 | Xuất mã đề: đề, đáp án, hướng dẫn chấm, bảng đặc tả (DOCX, PDF) | 2 | 0,6 | 2,6 |
| S5-E2 | US-S5E2-06 | Trợ lý soạn đề nháp từ tài liệu Đang dùng của giáo viên (luồng D) | 3 | 0,9 | 3,9 |
| **S5-E2 cộng** | | | **14** | **4,1** | **18,1** |
| S5-E3 | US-S5E3-01 | Kho tri thức: thêm tài liệu, phiên bản bất biến, trạng thái Nháp → Chờ duyệt → Đang dùng → Lưu trữ | 3 | 0,9 | 3,9 |
| S5-E3 | US-S5E3-02 | Gắn tài liệu cho lớp hoặc bài, thời hạn hiệu lực | 2 | 0,6 | 2,6 |
| S5-E3 | US-S5E3-03 | Hỏi đáp của học sinh dựa trên kho đã gắn, luôn kèm nguồn, thông báo riêng tư trước lần hỏi đầu | 2 | 0,6 | 2,6 |
| S5-E3 | US-S5E3-07 | Bộ truy xuất mục tài liệu: tìm kiếm toàn văn tiếng Việt, ưu tiên mã kiến thức, ngưỡng điểm | 2 | 0,6 | 2,6 |
| S5-E3 | US-S5E3-04 | Mức trợ giúp theo bài và lịch công bố đáp số, kiểm ở máy chủ trước khi gọi AI | 2 | 0,6 | 2,6 |
| S5-E3 | US-S5E3-05 | Kiểm soát đáp án đầy đủ: bộ kiểm đầu ra KS-1…KS-5 cho mọi câu trả lời, không lộ ghi chú riêng và bài của bạn, nhật ký quyết định | 3 | 0,9 | 3,9 |
| S5-E3 | US-S5E3-06 | Giáo viên xem lịch sử hỏi đáp theo bài, báo "chặn sai" hoặc "lẽ ra phải chặn" | 2 | 0,6 | 2,6 |
| **S5-E3 cộng** | | | **16** | **4,8** | **20,8** |
| S5-E4 | US-S5E4-01 | Rubric có phiên bản, mẫu sẵn theo dạng bài, bất biến khi đã dùng | 3 | 0,9 | 3,9 |
| S5-E4 | US-S5E4-02 | Giao bài có chấm: đề hoặc bộ đề, rubric, chế độ chấm, hạn nộp, nộp muộn, lịch công bố điểm và lời giải | 2 | 0,6 | 2,6 |
| S5-E4 | US-S5E4-03 | Học sinh chụp và nộp nhiều trang trên iPhone: kiểm độ rõ, xếp trang, HEIC, thông báo riêng tư | 3 | 0,9 | 3,9 |
| S5-E4 | US-S5E4-04 | Bản đọc chữ (OCR) của bài nộp: phái sinh, tô chỗ chưa chắc, giáo viên sửa có lịch sử | 2 | 0,6 | 2,6 |
| S5-E4 | US-S5E4-05 | Hàng chờ chấm: danh sách theo bài, trạng thái, cờ xếp trước, lọc lớp và bài | 2 | 0,6 | 2,6 |
| S5-E4 | US-S5E4-06 | Chấm tay theo rubric trên web, Duyệt thành điểm chính thức, nhật ký trước và sau | 2 | 0,6 | 2,6 |
| S5-E4 | US-S5E4-07 | Trả bài: học sinh thấy điểm, nhận xét, lời giải theo lịch công bố; không thấy gì của AI | 2 | 0,6 | 2,6 |
| S5-E4 | US-S5E4-08 | Lưu ảnh bài làm an toàn: mã hoá khi lưu, URL ký 5 phút, gỡ EXIF/GPS, xoá theo năm học và theo tài khoản, ảnh gửi AI | 3 | 0,9 | 3,9 |
| S5-E4 | US-S5E4-09 | Gửi lại khi mất mạng (lũy đẳng), nộp lại trước hạn, nộp muộn, tải dồn trước hạn | 2 | 0,6 | 2,6 |
| **S5-E4 cộng** | | | **21** | **6,3** | **27,3** |
| S5-E5 | US-S5E5-01 | Trợ lý soạn nháp thông báo lớp, giáo viên sửa và tự bấm Gửi (trong ứng dụng) | 2 | 0,6 | 2,6 |
| S5-E5 | US-S5E5-02 | Tổng hợp lỗi sai của lớp theo tiêu chí và loại lỗi (COP-02), có bằng chứng, không gắn nhãn học sinh | 3 | 0,9 | 3,9 |
| S5-E5 | US-S5E5-03 | "Nên nhắc lại trên lớp": từ nhận định gợi ý bài luyện và tài liệu trong kho, giáo viên chọn rồi giao hoặc chiếu | 2 | 0,6 | 2,6 |
| S5-E5 | US-S5E5-04 | Công tắc AI theo lớp, tắt khẩn toàn hệ thống, định nghĩa lượt, hạn mức theo lớp, học sinh và token, báo cáo dùng AI | 3 | 0,9 | 3,9 |
| **S5-E5 cộng** | | | **10** | **3** | **13** |
| S5-E6 | US-S5E6-01 | AI đề xuất điểm theo từng tiêu chí: lý do, vùng ảnh, độ tin cậy; máy chủ kiểm thang, tổng và luật chặn KPI | 3 | 0,9 | 3,9 |
| S5-E6 | US-S5E6-02 | AI từ chối chấm và lỗi: ảnh kém, tin cậy thấp, hết giờ; chuyển chấm tay có lý do | 2 | 0,6 | 2,6 |
| S5-E6 | US-S5E6-03 | Engine làm tín hiệu thứ hai: đối chiếu đáp số cuối trong bài với engine, lệch thì gắn cờ | 2 | 0,6 | 2,6 |
| S5-E6 | US-S5E6-04 | Màn chấm có đề xuất AI: nhận, sửa, bỏ từng tiêu chí, khoanh vùng ảnh, nhận cả bài khi không có cờ | 3 | 0,9 | 3,9 |
| S5-E6 | US-S5E6-05 | Chạy song song: giáo viên chấm tay trước, đề xuất AI hiện sau khi duyệt, lưu cặp so sánh | 2 | 0,6 | 2,6 |
| S5-E6 | US-S5E6-06 | Báo cáo khớp AI–giáo viên theo đủ KPI §2–§4 (có KTC) và cổng bật, đóng, mở lại "AI đề xuất" cho lớp | 3 | 0,9 | 3,9 |
| **S5-E6 cộng** | | | **15** | **4,5** | **19,5** |
| **Toàn chặng** | | | **88** | **26,2** | **114,2** |
<!-- TBL:END -->

## 4. Ngoài bảng nhưng cần lịch (đề xuất của PO, không cộng vào tổng)

| Khoản | Ước tính | Vì sao |
|---|---|---|
| Dự phòng rủi ro ước lượng | +20 % ≈ 21 người-ngày | ba rủi ro lớn ở mục 5; chặng có nhiều AI mới nhất từ trước tới nay |
| Bench AI chấm bài viết tay (prompt-lab `grade`, `grade_ocr`) trước khi code S5-E6 | 5 người-ngày + chi phí API (ước 3–5M token theo mức prompt-lab giáo án) | prompt-lab mới đo giáo án và bắt bước sai trên chữ gõ; chưa có số nào cho ảnh viết tay; KPI §4 cần đo trước khi tin ngưỡng (QS5-27) |
| Thu bộ ảnh bài viết tay mẫu có điểm giáo viên (≥ 30 bài, 3 dạng) | phụ thuộc trường pilot | đầu vào bench trên; STAGES §9 "bài mẫu đã được giáo viên chấm" |
| Thiết kế màn C5-01…C5-08 (Designer) | 6–8 người-ngày | chỉ có concept `vision-01`, `vision-03`; `giao-dien.md` mới phủ giáo án |
| Soát độc lập và sửa gom mỗi epic | ≈ 1 người-ngày/epic | quy trình dự án (CLAUDE.md §7) |

## 5. Rủi ro ước lượng (xếp theo mức ảnh hưởng)

| # | Rủi ro | Story chịu | Biên độ nếu xảy ra | Giảm bằng |
|---|---|---|---|---|
| R1 | **Chưa được thu ảnh bài làm** (PO-64 còn hiệu lực, QS5-01) → code S5-E4/S5-E6 xong nhưng không phát hành; nếu owner chọn lưu ảnh có điều kiện riêng (mã hoá theo trường, thời hạn khác, che vùng tên) thì thêm việc | US-S5E4-03, 04; cả S5-E6 | +2–4 ngày; lịch phát hành trượt tới khi có quyết định | làm S5-E1, S5-E3, S5-E2 trước (không cần ảnh); hỏi owner ngay ở `questions-s5.md` |
| R2 | **Chất lượng AI trên chữ viết tay toán chưa đo**: OCR ký hiệu (√, ⊥, phân số) và đề xuất điểm có thể không đạt KPI §4, kéo theo nhiều vòng chỉnh prompt, luật chặn, ngưỡng tin cậy | US-S5E4-04, US-S5E6-01, 02, 06 | +3–6 ngày dev; tệ nhất: cổng "AI đề xuất" không mở được cho lớp nào, S5-E6 chỉ còn giá trị chạy song song | bench 5 ngày ở mục 4 trước khi code S5-E6; S5-E4 (chấm tay) có giá trị độc lập |
| R3 | **Phần kỹ thuật mới chưa có mẫu trong code** (đã tách US-S5E4-03, US-S5E3-03 và đưa spike vào dev ở v0.2; phần còn lại vẫn là rủi ro): camera PWA trên iOS + gửi lại khi mất mạng; chuyển `lap_de.py` sang Java giữ đúng 7 ràng buộc; xuất DOCX đúng khung 5512; truy xuất tài liệu bằng tìm kiếm toàn văn tiếng Việt | US-S5E4-03, US-S5E2-03, US-S5E1-05, US-S5E3-03 | mỗi story +1–2 ngày (có thể vượt trần 3 ngày → tách lại) | spike 0,5 ngày mỗi phần ở đầu story; giữ `check.py` làm bộ kiểm đối chứng cho lắp đề |
| R4 | Kiểm soát đáp án mở rộng chặn nhầm nhiều câu chính đáng khi tài liệu giáo viên chứa số liệu của bài | US-S5E3-05, 06 | +1–2 ngày chỉnh luật | đo bằng báo "chặn sai" (US-S5E3-06) trong pilot |
| R5 | Phụ thuộc vòng: chế độ chấm ở US-S5E4-02 cần cổng của US-S5E6-06 và công tắc của US-S5E5-04 | US-S5E4-02 | +0,5 ngày | làm US-S5E5-04 trước S5-E4; US-S5E4-02 trả cổng "đóng" cho tới khi S5-E6 xong (brief §4) |
| R7 | **Test sát trần 30 % dev (lệnh owner giữ trần):** 26,2 ngày cho 219 TC ≈ 0,96 giờ/TC (bản v0.1: 23,8 ngày cho 172 TC ≈ 1,1 giờ/TC) cho viết case, dựng dữ liệu, chạy, ghi bằng chứng và chạy lại; chưa có thời gian hồi quy giữa các epic, chạy tay thiết bị thật (iOS Safari, HEIC) và kịch bản tải | mọi story; nặng nhất S5-E4, S5-E6 | thiếu ước tính 8–15 người-ngày test nếu tính từ dưới lên (hồi quy ≈ 1 ngày/epic, thiết bị thật 2 ngày, tải 1 ngày) | giữ trần theo lệnh owner; tự động hoá E2E-API ngay trong dev; gom hồi quy vào cổng L5a–L5c; dùng một phần dự phòng +20 % |
| R6 | Dữ liệu chặng 2–3 (S2-E3 đường kiểm chứng đề, S2-E4 tách mục từ tệp, S3-E2 bộ bài, S3-E4 thông báo) chưa code xong khi bắt đầu S5 | US-S5E2-06, US-S5E3-01, US-S5E5-01 | +1–3 ngày mỗi chỗ nếu phải tự làm phần thiếu | kiểm trạng thái code S2/S3 ở gate mở chặng 5 |

**Ước tính khoảng:** kỳ vọng 114,2 người-ngày; khoảng tin cậy chủ quan của PO 105–150 người-ngày (thấp: dùng lại tốt code S2/S3; cao: R1–R3 cùng xảy ra). Chưa gồm mục 4.

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-28 | Ước lượng ban đầu chặng 5: 6 epic, 34 story (goal run) | agent spec-s5 (PO) |
| v0.2 | 2026-11-28 | Sửa theo soát độc lập: 37 story, 114,2 người-ngày; thêm R7 (giờ/TC) | agent spec-s5 (PO) |
