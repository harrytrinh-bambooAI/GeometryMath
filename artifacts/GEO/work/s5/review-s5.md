Phiên bản: v0.1 · 2026-11-28
# Tự soát chặng 5 theo ba vai PO / BA / QC

> Soát trong cùng phiên goal run, theo lượt riêng sau khi sinh xong tệp epic, **không phải soát độc lập trên model khác**. Theo CLAUDE.md §7 cần một agent khác soát diff này với tiêu chí "Xong khi" trước khi chốt R3. Trạng thái: **Đã sửa** (đã áp vào tệp, `check_s5.py` chạy lại đạt) · **Mở** (giữ, có giả định hoặc hướng xử lý) · **Chấp nhận** (PO chấp nhận khoảng trống, ghi lý do).

## Kiểm máy
`python3 artifacts/GEO/work/s5/check_s5.py` → ĐẠT: 6 epic · 34 US · 172 AC · 104 BR · 172 TC (E2E-API 145, E2E-UI 27) · dev 80 · test 23,8 · tổng 103,8. Đã thử đột biến (dev 4 ngày, TC kiểu `IT`, TC trùng mã, TC bỏ mã AC, lệch bảng ước lượng): script bắt đủ 10/10 lỗi cài vào.

## Vai PO (phạm vi, ưu tiên, nguyên tắc)

| # | Phát hiện | Xử lý | Trạng thái |
|---|---|---|---|
| PO-1 | Khung 5 epic: S5-E4 gộp nộp bài + AI chấm + duyệt ≈ 30 ngày dev, 13 story, quá lớn để giao và soát một lượt; trái "tách ba đợt bật" của STAGES §10 | Tách S5-E4 (đợt 1, chấm tay) và S5-E6 (đợt 2–3, AI) và ghi lý do ở brief §1, đầu hai tệp epic | Đã sửa |
| PO-2 | STAGES §4 xếp trợ lý giáo án P1, nhưng PO-27, PO-45 là Final của chủ đầu tư | S5-E1 P0; theo PO-19 sổ PO thắng STAGES; ghi ở đầu S5-E1 | Đã sửa |
| PO-3 | PO-64 còn cấm thu ảnh bài làm; spec nộp bài viết tay chạm thẳng điểm này | Spec chạy theo giả định QS5-01; brief §5 tách cổng L5b chờ owner; S5-E4 ghi "Điều kiện phát hành" | Mở (QS5-01) |
| PO-4 | Điều kiện mở chặng "có trường cam kết pilot" chưa đạt | QS5-28: chỉ chặn phát hành S5-E4/S5-E6; phần trợ lý và kho làm trước | Mở (QS5-28) |
| PO-5 | "Công tắc AI theo trường" (STAGES §4 P0, C5-09) đụng multi-tenant ngoài phạm vi | Thu về mức lớp: US-S5E5-04, US-S5E6-06; QS5-02 | Đã sửa (giả định QS5-02) |
| PO-6 | PO-43 (bài giảng AI chưa duyệt vẫn làm nguồn) chạm STAGES §7 ("chỉ tài liệu đang dùng") | PO-43 chỉ áp bài giảng mẫu hệ thống; kho giáo viên chỉ dùng Đang dùng (BR-S5E3-05, 08); QS5-10 | Mở (QS5-10) |
| PO-7 | Nguyên tắc "AI chỉ đề xuất, giáo viên quyết định": soát mọi đường ghi | Có thao tác giáo viên ở: duyệt giáo án (BR-S5E1-13), duyệt bộ đề (BR-S5E2-11), duyệt đề nháp (BR-S5E2-16), kích hoạt tài liệu (BR-S5E3-02), Duyệt điểm (BR-S5E4-18, BR-S5E6-10), gửi thông báo (BR-S5E5-02), tạo bộ luyện (BR-S5E5-09); không có tự duyệt; "Nhận cả bài" bị khoá khi có cờ (BR-S5E6-11) | Đã sửa |
| PO-8 | Nguyên tắc "đáp số do engine": soát mọi chỗ AI có thể ghi đáp số | `dap_so` = null ở giáo án, điền từ kho/engine (BR-S5E1-07); lắp đề không gọi AI (BR-S5E2-08); đề nháp AI lấy đáp số engine (BR-S5E2-15, FC3); thông báo dữ kiện do máy (BR-S5E5-03); chấm đối chiếu engine (US-S5E6-03) | Đã sửa |
| PO-9 | Phúc khảo, duyệt nhận xét theo cụm (STAGES P1) chưa có chỗ | Ngoài phạm vi chặng 5, backlog sau L5c; QS5-29 | Chấp nhận |
| PO-10 | Chất lượng AI chấm viết tay chưa có số đo nào; ước lượng S5-E6 dựa trên giả định prompt đạt | Đề xuất bench 5 ngày ngoài bảng (estimate §4, QS5-27), rủi ro R2 | Mở (QS5-27) |
| PO-11 | Hạn mức chấm 100 lượt/lớp/ngày không đủ cho lớp 45 em (mỗi bài 2 lượt: đọc chữ + đề xuất, cộng gọi lại) | Nâng 200 lượt (BR-S5E5-12, QS5-04) | Đã sửa |

## Vai BA (story, BR, AC)

| # | Phát hiện | Xử lý | Trạng thái |
|---|---|---|---|
| BA-1 | US-S5E1-03 SC3 cho AI hỏi lại khi "thiếu đối tượng", nhưng form và máy chủ đã chặn thiếu trường B (US-S5E1-01 FC1): ca không xảy ra | Đổi thành ghi chú mâu thuẫn với form → `question` kèm nút mở trường Số tiết | Đã sửa |
| BA-2 | "Ảnh gốc giữ nguyên" (BR-S5E4-08) mâu thuẫn với "≤ 8 MB sau nén" (BR-S5E4-07): ảnh nào là gốc? | Định nghĩa: tệp tải lên là ảnh gốc; nén chỉ ở máy trước khi tải; xoay, cắt mép phía máy chủ là phái sinh; sửa kỳ vọng SC1 so băm tệp tải lên | Đã sửa |
| BA-3 | Chế độ chạy song song chỉ ẩn điểm AI, nhưng cờ "AI và engine lệch" vẫn để lộ đánh giá của AI cho phần Kết quả | BR-S5E6-13 ẩn cả cờ suy từ AI; tín hiệu engine riêng vẫn hiện | Đã sửa |
| BA-4 | US-S5E6-01 SC2 dùng bài `BN-07` không có trong dữ liệu mẫu | Thêm `F-S5-057` | Đã sửa |
| BA-5 | Tổng hợp lỗi (US-S5E5-02) cần loại lỗi, nhưng bài chấm tay (S5-E4) không có loại lỗi | BR-S5E5-04: loại lỗi chỉ từ đề xuất AI giáo viên đã nhận; bài chấm tay chỉ vào số liệu theo tiêu chí | Đã sửa |
| BA-6 | Vòng phụ thuộc US-S5E4-02 (chế độ chấm) ↔ US-S5E6-06 (cổng) | US-S5E4-02 dựng trường và trả cổng "đóng" tới khi US-S5E6-06 xong; brief §4, rủi ro R5 | Đã sửa |
| BA-7 | BR-S5E2-10 ban đầu viết dạng tự hỏi tự đáp, không phải một quy tắc | Viết lại thành quy tắc nhãn "Máy lắp" (QS5-08) | Đã sửa |
| BA-8 | Câu chữ MSG-S5-01…60 là đề xuất của BA, chưa qua Designer; màn C5-01…C5-08 chưa có thiết kế (chỉ concept `vision-01`, `vision-03`, ASCII giáo án) | Ghi estimate §4 (6–8 ngày Designer); AC UI chỉ khẳng định thành phần và trạng thái, không khẳng định bố cục | Mở |
| BA-9 | Một số BR dẫn nguồn chéo epic (ví dụ US-S5E2-05 FC2 dùng BR-S5E1-14 cho chân trang bản nháp) | Giữ: hai story dùng chung bộ dựng tệp xuất; ghi ở mục Phụ thuộc | Chấp nhận |
| BA-10 | Chưa có `test_design.md`, `questions.md` riêng từng epic như S3 | Câu hỏi gom một tệp `questions-s5.md` theo brief giao việc; test design gói trong quy ước đầu `test_cases.md` | Chấp nhận |

## Vai QC (case, tầng tự động hoá, khả năng chạy)

| # | Phát hiện | Xử lý | Trạng thái |
|---|---|---|---|
| QC-1 | Tỉ lệ tầng: 145 E2E-API, 27 E2E-UI (16 %); soát 27 case UI: đều là tương tác trên màn (form điền lại, kéo xếp trang, chụp ảnh, khoanh vùng, nhận/bỏ từng dòng, phím tắt, lọc) | Giữ | Đã sửa |
| QC-2 | Ca cần chỉnh giờ (ranh giới R − 1 giây ở US-S5E4-07 SC2, hạn nộp, hạn mức "lượt mới lúc 00:00", gắn kết hết hạn US-S5E3-02 SC2) | Dùng đồng hồ CSDL như NFR-GEO-023; đặt mốc theo `now()` của CSDL | Mở (cần móc thời gian của profile e2e như PO-51) |
| QC-3 | US-S5E4-01 FC3 cần ghi thẳng CSDL bằng vai ứng dụng | Đánh dấu ENG, dùng móc profile e2e, không bật ở prod (PO-51) | Đã sửa |
| QC-4 | Mọi ca AI cần kịch bản giả lập cố định (F-S5-090…095, các biến thể "b") trong `MockAiProvider`; chưa có cơ chế chọn kịch bản theo ca | Gói trong dev của từng story (estimate §1); đề xuất khoá chọn kịch bản theo tiêu đề yêu cầu như `MockAiControl` hiện có | Mở |
| QC-5 | US-S5E4-03 SC1, FC1, FC2 chụp ảnh trên iPhone: Playwright WebKit không có camera thật | Dùng `input[type=file]` với ảnh mẫu và giả lập offline của trình duyệt; kiểm trên máy thật để ở vòng pilot | Mở |
| QC-6 | Ca kiểm "nội dung gửi AI không chứa danh tính" (US-S5E1-02 FC5, US-S5E3-03 FC4, US-S5E6-01 SC1) phụ thuộc giả lập lưu yêu cầu | `MockAiProvider` ghi lại yêu cầu cuối; mở qua móc e2e như NFR-GEO-071 | Mở |
| QC-7 | Ca bộ kiểm đối chứng lắp đề (US-S5E2-03 SC1) gọi `check.py` (Python) trong CI Java | Chạy `check.py --no-engine` trên JSON bộ đề xuất ra; CI cần Python 3 + sympy | Mở |
| QC-8 | Mỗi AC đúng 1 TC: đủ phủ theo luật, nhưng ca biên số (0,25, 1 000 ký tự, 10 trang) đã gộp vào FC có hai bước | Chấp nhận cho R3; QC tách thêm case biên ở vòng thực thi nếu lỗi tập trung | Chấp nhận |

## Tổng
- 29 phát hiện: Đã sửa 15 · Mở 10 · Chấp nhận 4 (đếm theo trạng thái ghi trong bảng; PO-5 đã sửa nhưng vẫn dựa trên giả định QS5-02).
- Câu hỏi owner: 29 câu ở `questions-s5.md`, 4 câu chặn phát hành hoặc lịch: QS5-01, QS5-27, QS5-28, QS5-10.

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-28 | Tự soát ba vai, áp 15 sửa (goal run) | agent spec-s5 (PO, BA, QC) |
