Phiên bản: v0.2 · 2026-11-28
# S5-E6 — test case (AI đề xuất điểm theo rubric và KPI, engine làm tín hiệu thứ hai, chạy song song, giáo viên duyệt (đợt bật 2–3))

> Chủ sở hữu: QC. Lập từ `stories_ac.md` cùng thư mục: mỗi tiêu chí có ít nhất một case, kỳ vọng bám Given / When / Then. **Trạng thái: ready for R3 (goal run)**. AI là giả lập (`MockAiProvider`, D-GEO-047): không gọi AI thật, kỳ vọng chỉ soát hành vi máy chủ và dữ liệu trả về, không soát chất lượng câu chữ AI.

- Mỗi dòng là một case. Chuẩn bị theo **Pre-condition**, dùng **Test data**, làm lần lượt **Test step**, so với **Expected output**; đạt khi mọi gạch đầu dòng đều đúng.
- **Tự động hóa** chỉ có hai giá trị: `E2E-API` (gọi HTTP thẳng, soát `code` và thân phản hồi; mặc định) và `E2E-UI` (Playwright, chỉ dùng khi điều được kiểm là tương tác trên màn: form, chụp ảnh, khoanh vùng, nhận/bỏ từng dòng).
- **Status** của vòng thực thi: Pass · Fail · Blocked (+ cần gì) · UT (+ lý do) · Not run. Kỳ vọng mang mã `QS5-nn` là giả định của PO; màn chỉ khác đúng điểm đó thì đánh Blocked kèm mã, không đánh Fail.
- Dữ liệu mẫu chung `F-S5-nnn` ở mục 0 của `stories_ac.md` cùng thư mục.

### US-S5E6-01 — AI đề xuất điểm theo từng tiêu chí: lý do, vùng ảnh, độ tin cậy; máy chủ kiểm thang, tổng và luật chặn KPI

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S5E6-01 | Đề xuất cho BN-01 đủ 5 tiêu chí, tổng 10 | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Hệ thống (bài nộp BN-01, BN-02) | • BC-01 chế độ AI đề xuất (lớp đã đạt cổng); BN-01 có bản đọc chữ v1; giả lập F-S5-090 | • Hàng dữ liệu: F-S5-001, F-S5-052, F-S5-053, F-S5-090<br/>• Giá trị nhập: `GET /api/submissions/BN-01/proposal` | 1. chờ xử lý nền xong<br/>2. cô Lan gọi `GET /api/submissions/BN-01/proposal` | • 5 tiêu chí 2 / 4 / 2 / 1 / 1, tổng 10, độ tin cậy 0,86<br/>• mỗi tiêu chí có lý do và vùng ảnh<br/>• bản ghi có phiên bản rubric RB-01 v1, bài nộp v1, prompt `grade`, model<br/>• yêu cầu ở giả lập không có "Bình An" hay `HS-4P9T-37WD` | US-S5E6-01-SC1 |
| TC-S5E6-02 | Luật chặn 2: đúng kết quả nhưng lập luận thiếu thì kết quả bị hạ | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Hệ thống (bài nộp BN-01, BN-02) | • BN-07 (F-S5-057); giả lập trả Hướng 2, Bước 1, Kết quả 2, Hình 1, Trình bày 1 | • Hàng dữ liệu: F-S5-057 | 1. chờ xử lý nền | • đề xuất lưu Kết quả = 1 với lý do "luật KPI §2.2"<br/>• tổng 6 | US-S5E6-01-SC2 |
| TC-S5E6-03 | Luật chặn 1: sai hướng thì bước phụ thuộc hướng = 0 | Medium | Normal (Success case) | chức năng | tích hợp API | E2E-API | Hệ thống (bài nộp BN-01, BN-02) | • giả lập trả Hướng 0, Bước 3 | • Hàng dữ liệu: F-S5-091<br/>• Giá trị: F-S5-091 biến thể Hướng 0 | 1. chờ xử lý nền | • đề xuất lưu Bước = 0 với lý do "luật KPI §2.1" | US-S5E6-01-SC3 |
| TC-S5E6-04 | Luật chặn 3: bước sai kéo bước phụ thuộc về 0, bước độc lập giữ | Medium | Normal (Success case) | chức năng | tích hợp API | E2E-API | Hệ thống (bài nộp BN-01, BN-02) | • giả lập trả cho BN-02 bước B2 sai, B3 `phu_thuoc` [B2], B4 độc lập, Bước 3/4 | • Hàng dữ liệu: F-S5-054, F-S5-091<br/>• Giá trị: F-S5-054, F-S5-091 (bước B2 sai) | 1. chờ xử lý nền | • điểm phần B3 = 0 với lý do "luật KPI §2.3"<br/>• phần B4 giữ nguyên<br/>• tổng Bước giảm đúng phần B3 | US-S5E6-01-SC4 |
| TC-S5E6-05 | Luật chặn 4: hình sai thì mất phần hình và bước dựa trên hình | Medium | Normal (Success case) | chức năng | tích hợp API | E2E-API | Hệ thống (bài nộp BN-01, BN-02) | • giả lập trả `hinh` = "hình sai", B1 `dua_tren_hinh` = true, Hình 1 | • Hàng dữ liệu: F-S5-053<br/>• Giá trị: F-S5-053 biến thể hình sai | 1. chờ xử lý nền | • Hình = 0 và phần B1 = 0 với lý do "luật KPI §2.4" | US-S5E6-01-SC5 |
| TC-S5E6-06 | Điểm vượt tối đa: gọi lại một lần rồi AI không chấm | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Hệ thống (bài nộp BN-01, BN-02) | • giả lập trả Bước = 5 (tối đa 4) cả hai lần | • Hàng dữ liệu: F-S5-051, F-S5-053<br/>• Giá trị: F-S5-051, F-S5-053<br/>• Giá trị: Bước = 5 (tối đa 4; biên 4 hợp lệ) | 1. chờ xử lý nền | • `AiCallLog` có 2 dòng task `grade`<br/>• bài có cờ "AI không chấm" lý do "điểm ngoài thang"<br/>• không có `GradingProposal` hợp lệ | US-S5E6-01-FC1 |
| TC-S5E6-07 | Học sinh không đọc được đề xuất | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Bình An | • BN-01 có đề xuất | • Hàng dữ liệu: F-S5-010, F-S5-053<br/>• Giá trị nhập: `GET /api/submissions/BN-01/proposal` | 1. Bình An gọi `GET /api/submissions/BN-01/proposal` | • FORBIDDEN<br/>• thân phản hồi không có điểm hay độ tin cậy | US-S5E6-01-FC2 |
| TC-S5E6-08 | Bịa kiến thức bị gắn cờ và hạ độ tin cậy | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Hệ thống (bài nộp BN-01, BN-02) | • giả lập trả tiêu chí Bước dẫn "định lí ba đường cao không gian" (không có trong kho) | • Giá trị nhập: "định lí ba đường cao không gian" | 1. chờ xử lý nền | • tiêu chí đó cờ "bịa kiến thức", độ tin cậy ≤ 0,5<br/>• bài có cờ trong hàng chờ | US-S5E6-01-FC3 |

### US-S5E6-02 — AI từ chối chấm và lỗi: ảnh kém, tin cậy thấp, hết giờ; chuyển chấm tay có lý do

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S5E6-09 | AI từ chối vì trang mờ | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Hệ thống và giáo viên Lan | • BN-03 (F-S5-055); giả lập F-S5-092 | • Hàng dữ liệu: F-S5-001, F-S5-055, F-S5-092 | 1. chờ xử lý nền<br/>2. cô Lan mở BN-03 | • bài Chờ chấm, cờ "AI không chấm"<br/>• hiện MSG-S5-45 với lý do "Trang 2 mờ, không đọc được bước 2–3"<br/>• không có điểm đề xuất | US-S5E6-02-SC1 |
| TC-S5E6-10 | Độ tin cậy thấp thì không hiện điểm | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Hệ thống và giáo viên Lan | • giả lập trả đề xuất hợp lệ với độ tin cậy tổng 0,55 | • Hàng dữ liệu: F-S5-051, F-S5-053<br/>• Giá trị: F-S5-051, F-S5-053<br/>• Giá trị: tin cậy tổng 0,55 | 1. chờ xử lý nền | • cờ "AI không chấm", lý do "độ tin cậy thấp (0,55)"<br/>• giáo viên không thấy điểm của đề xuất đó | US-S5E6-02-SC2 |
| TC-S5E6-11 | Biên độ tin cậy tổng: 0,60 được chấm, 0,59 thì không | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Hệ thống và giáo viên Lan | • giả lập trả hai đề xuất hợp lệ: (A) tin cậy từng tiêu chí cho tổng có trọng số đúng 0,60; (B) cho tổng 0,59 | • Hàng dữ liệu: F-S5-051<br/>• Giá trị: RB-01 (F-S5-051) trọng số 2/4/2/1/1<br/>• Giá trị: A: 0,6 ở mọi tiêu chí<br/>• Giá trị: B: 0,59 ở mọi tiêu chí | 1. chờ xử lý nền cho hai bài | • bài A có đề xuất hiện cho giáo viên<br/>• bài B cờ "AI không chấm", lý do "độ tin cậy thấp (0,59)" | US-S5E6-02-FC3 |
| TC-S5E6-12 | Cổng AI hết giờ hai lần | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Hệ thống và giáo viên Lan | • giả lập hết giờ cả hai lần | • Hàng dữ liệu: F-S5-053<br/>• Giá trị: F-S5-053<br/>• Giá trị: `geo.ai.timeout` 10 giây (dev/test, PO-78), hết giờ 2 lần | 1. chờ xử lý nền | • cờ "AI không chấm", lý do "AI tạm thời không phản hồi"<br/>• nút "Thử chấm lại"<br/>• bài vẫn chấm tay được | US-S5E6-02-FC1 |
| TC-S5E6-13 | Lớp tắt AI chấm thì không gọi AI | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Hệ thống | • lớp 11A1 tắt AI chấm (US-S5E5-04); BC-01 chế độ AI đề xuất | • Hàng dữ liệu: F-S5-010, F-S5-052 | 1. Ngọc Anh nộp bài | • không có dòng `AiCallLog` task `grade`<br/>• hàng chờ hiện "AI đang tắt" cho bài đó | US-S5E6-02-FC2 |

### US-S5E6-03 — Engine làm tín hiệu thứ hai: đối chiếu đáp số cuối trong bài với engine, lệch thì gắn cờ

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S5E6-14 | BN-01 đáp số khớp engine | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Hệ thống và giáo viên Lan | • BN-01 bản đọc chữ có "Vậy d(A,(SBC)) = a√6/3" | • Hàng dữ liệu: F-S5-053<br/>• Giá trị nhập: "Vậy d(A,(SBC)) = a√6/3" | 1. chờ xử lý nền | • tín hiệu engine "Khớp" với giá trị engine `a√6/3`<br/>• không có cờ lệch (AI cho Kết quả 2/2) | US-S5E6-03-SC1 |
| TC-S5E6-15 | Đáp số viết thập phân vẫn khớp | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Hệ thống và giáo viên Lan | • bản đọc chữ có "d ≈ 0,816a" | • Giá trị nhập: "d ≈ 0,816a" | 1. chờ xử lý nền | • tín hiệu "Khớp" (KS-2 làm tròn 3 chữ số) | US-S5E6-03-SC2 |
| TC-S5E6-16 | AI cho điểm kết quả nhưng engine báo lệch thì gắn cờ | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Hệ thống và giáo viên Lan | • BN-02 đáp số `a√3/3`; giả lập trả Kết quả 2/2 cho BN-02 | • Hàng dữ liệu: F-S5-001, F-S5-054<br/>• Giá trị nhập: `a√3/3` | 1. chờ xử lý nền<br/>2. cô Lan mở hàng chờ | • tín hiệu engine "Lệch"<br/>• cờ "AI và engine lệch" xếp đầu hàng chờ<br/>• màn chấm hiện MSG-S5-46 với `a√6/3` | US-S5E6-03-FC1 |
| TC-S5E6-17 | Không tìm thấy đáp số cuối thì không gắn cờ lệch | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Hệ thống và giáo viên Lan | • bản đọc chữ không có dòng kết luận | • Hàng dữ liệu: F-S5-050, F-S5-053<br/>• Giá trị: F-S5-050, F-S5-053<br/>• Giá trị: bản đọc chữ không có dòng kết luận | 1. chờ xử lý nền | • tín hiệu "Không tìm thấy"<br/>• không có cờ lệch<br/>• giáo viên thấy ghi chú "Không thấy đáp số cuối trong bản đọc chữ" | US-S5E6-03-FC2 |

### US-S5E6-04 — Màn chấm có đề xuất AI: nhận, sửa, bỏ từng tiêu chí, khoanh vùng ảnh, nhận cả bài khi không có cờ

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S5E6-18 | Bấm tiêu chí khoanh vùng ảnh, nhận từng tiêu chí rồi duyệt | High | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (F-S5-001) | • BN-01 có đề xuất F-S5-090, không cờ | • Hàng dữ liệu: F-S5-053, F-S5-090<br/>• Giá trị nhập: "Bước" | 1. mở BN-01<br/>2. bấm tiêu chí "Bước"<br/>3. bấm Nhận ở cả 5 tiêu chí<br/>4. bấm Duyệt | • ảnh trang có vùng khoanh đúng `{trang,x,y,w,h}` của tiêu chí "Bước"<br/>• mỗi tiêu chí sau Nhận hiện dấu đã nhận và MSG-S5-49<br/>• sau Duyệt `ApprovedGrade` tổng 10, nguồn "AI đề xuất, giáo viên duyệt" | US-S5E6-04-SC1 |
| TC-S5E6-19 | Sửa một tiêu chí, bỏ một tiêu chí rồi nhập tay | High | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (F-S5-001) | • BN-02 có đề xuất F-S5-091 | • Hàng dữ liệu: F-S5-054, F-S5-091<br/>• Giá trị nhập: "Bước"; "Trình bày" | 1. Sửa "Bước" từ 3 thành 2,5<br/>2. Bỏ "Trình bày"<br/>3. thử bấm Duyệt<br/>4. nhập "Trình bày" 0,5 rồi Duyệt | • sau bước 3 nút Duyệt mờ<br/>• sau bước 4 tổng 6,5<br/>• `CriterionAssessment` ghi Bước sửa (AI 3 · GV 2,5), Trình bày bỏ (AI 1 · GV 0,5) | US-S5E6-04-SC2 |
| TC-S5E6-20 | Nhận cả bài và phím tắt cho bài không cờ | Medium | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (F-S5-001) | • BN-01 không cờ | • Hàng dữ liệu: F-S5-053<br/>• Giá trị nhập: "Nhận cả bài" | 1. bấm "Nhận cả bài"<br/>2. bấm Enter | • cả 5 tiêu chí đã nhận<br/>• bài Đã duyệt, mở bài kế tiếp | US-S5E6-04-SC3 |
| TC-S5E6-21 | Bài có cờ không có "Nhận cả bài" | High | Abnormal (Fail case/Edge case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (F-S5-001) | • BN-02 có cờ "AI và engine lệch" | • Hàng dữ liệu: F-S5-054<br/>• Giá trị nhập: "AI và engine lệch" | 1. mở BN-02 | • không có nút "Nhận cả bài"<br/>• phím tắt nhận cả bài không có tác dụng | US-S5E6-04-FC1 |
| TC-S5E6-22 | Gọi API duyệt cả bài có cờ bị từ chối | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • BN-02 có cờ | • Hàng dữ liệu: F-S5-054<br/>• Giá trị nhập: `POST /api/submissions/BN-02/grade`; `{"acceptAll":true}` | 1. gửi `POST /api/submissions/BN-02/grade` `{"acceptAll":true}` | • CONFLICT<br/>• không có `ApprovedGrade` | US-S5E6-04-FC2 |

### US-S5E6-05 — Chạy song song: giáo viên chấm tay trước, đề xuất AI hiện sau khi duyệt, lưu cặp so sánh

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S5E6-23 | Trước khi duyệt không thấy đề xuất | High | Normal (Success case) | dữ liệu trả về | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • BC-01 chế độ Chạy song song; BN-02 AI đã chấm (F-S5-091) | • Hàng dữ liệu: F-S5-001, F-S5-052, F-S5-054, F-S5-091<br/>• Giá trị nhập: `GET /api/submissions/BN-02`; `/proposal` | 1. cô Lan gọi `GET /api/submissions/BN-02` và `/proposal` | • `/proposal` trả CONFLICT kèm MSG-S5-47<br/>• `GET` bài nộp chỉ có cờ `aiDone = true`, không có `score`, `reason`, `confidence` | US-S5E6-05-SC1 |
| TC-S5E6-24 | Sau khi duyệt hiện so sánh và lưu cặp | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • như SC1 | • Hàng dữ liệu: F-S5-001, F-S5-054<br/>• Giá trị nhập: `GET /api/submissions/BN-02/comparison` | 1. cô Lan duyệt BN-02 tổng 6,5 (Bước 2,5, Trình bày 0,5)<br/>2. gọi `GET /api/submissions/BN-02/comparison` | • so sánh 5 tiêu chí: lệch ở Bước (AI 3 · GV 2,5) và Trình bày (AI 1 · GV 0,5)<br/>• bảng cặp có 5 dòng của BN-02 | US-S5E6-05-SC2 |
| TC-S5E6-25 | Hàng chờ ở chế độ song song không lộ trạng thái AI | High | Normal (Success case) | dữ liệu trả về | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • BC-01 Chạy song song; BN-02 AI chấm lệch engine, BN-03 AI không chấm, BN-01 bình thường; giờ nộp BN-01 < BN-02 < BN-03 | • Hàng dữ liệu: F-S5-001, F-S5-052, F-S5-053, F-S5-054, F-S5-055, F-S5-091, F-S5-092<br/>• Giá trị: F-S5-053, F-S5-054, F-S5-055, F-S5-091, F-S5-092 | 1. cô Lan gọi `GET /api/grading/queue?assignment=BC-01` | • thứ tự BN-01, BN-02, BN-03 theo giờ nộp<br/>• không phần tử nào có trường `aiFlags`, `confidence`, `aiStatus` ngoài `aiDone` | US-S5E6-05-SC3 |
| TC-S5E6-26 | Sửa điểm sau khi thấy đề xuất không đổi cặp đã lưu | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • BN-02 đã duyệt và đã xem so sánh | • Hàng dữ liệu: F-S5-001, F-S5-054 | 1. cô Lan sửa Bước thành 3 | • `ApprovedGrade` mới tổng 7<br/>• cặp so sánh của tiêu chí Bước vẫn (AI 3 · GV 2,5) | US-S5E6-05-FC1 |
| TC-S5E6-27 | Màn chấm ở chế độ song song không hiện khung đề xuất | Medium | Abnormal (Fail case/Edge case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (F-S5-001) | • như SC1 | • Hàng dữ liệu: F-S5-001, F-S5-054 | 1. cô Lan mở BN-02 trên màn chấm | • hiện MSG-S5-47<br/>• không có ô điểm đề xuất, vùng khoanh hay độ tin cậy | US-S5E6-05-FC2 |

### US-S5E6-06 — Báo cáo khớp AI–giáo viên theo đủ KPI §2–§4 (có KTC) và cổng bật, đóng, mở lại "AI đề xuất" cho lớp

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S5E6-28 | Báo cáo khớp của 11A1 có n và KTC Wilson | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • 11A1 có 20 bài song song, 100 cặp tiêu chí, 88 cặp lệch trong ±0,25 | • Hàng dữ liệu: F-S5-010<br/>• Giá trị: 11A1 (F-S5-010)<br/>• Giá trị: 20 bài, 100 cặp, 88 trong ±0,25 | 1. gọi `GET /api/classes/11A1/ai-grading/report` | • `n_bai` = 20, `n_cap` = 100<br/>• (b) = 88 % với KTC 95 % Wilson 80,2 %–93,0 %<br/>• có đủ chỉ số (a)–(h) kèm n<br/>• không có tên học sinh hay xếp hạng<br/>• `gateOpen = false` vì cận dưới 80,2 % < 85 % | US-S5E6-06-SC1 |
| TC-S5E6-29 | Đạt đủ 5 điều kiện bằng cận dưới KTC thì mở được chế độ AI đề xuất | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • 40 bài song song, 200 cặp: (b) 190/200; (a) macro 92 %, dạng thấp nhất 81 %; (c) 40/40; (d) bắt 45/50, báo nhầm 3/150; (e) 0 | • Hàng dữ liệu: F-S5-001<br/>• Giá trị: 40 bài, 200 cặp, số đếm như Given | 1. gọi báo cáo<br/>2. cô Lan giao bài mới chế độ `ai_proposal` | • `gateOpen = true` (cận dưới (b) 91,0 %)<br/>• giao bài trả 201 | US-S5E6-06-SC2 |
| TC-S5E6-30 | Chưa đủ 20 bài thì cổng đóng | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • 11A1 có 12 bài song song | • Hàng dữ liệu: F-S5-010<br/>• Giá trị: 12 bài (biên: 19 đóng, 20 xét tiếp) | 1. gọi báo cáo<br/>2. giao bài chế độ `ai_proposal` | • `gateOpen = false`, MSG-S5-48 "12/20"<br/>• giao bài trả CONFLICT | US-S5E6-06-FC1 |
| TC-S5E6-31 | Một dạng bài dưới 70 % thì cổng đóng dù macro đạt | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • như SC2 nhưng dạng "góc" có điểm khớp 66 % | • Giá trị: như SC2; dạng góc 66 % | 1. gọi báo cáo | • `gateOpen = false`, lý do nêu "dạng góc 66 % < 70 %" | US-S5E6-06-FC2 |
| TC-S5E6-32 | Báo nhầm vượt 10 % thì cổng đóng | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • như SC2 nhưng báo nhầm 25/150 | • Giá trị: báo nhầm 25/150 | 1. gọi báo cáo | • `gateOpen = false`, lý do nêu chỉ số báo nhầm | US-S5E6-06-FC3 |
| TC-S5E6-33 | Một lần bịa kiến thức thì cổng đóng | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • như SC2 nhưng 1 đề xuất có cờ "bịa kiến thức" | • Giá trị: 1 cờ bịa kiến thức | 1. gọi báo cáo | • `gateOpen = false`, lý do "bịa kiến thức: 1" | US-S5E6-06-FC4 |
| TC-S5E6-34 | Kết quả AI đúng chiều engine dưới 95 % thì cổng đóng | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • như SC2 nhưng (c) 36/40 | • Giá trị: (c) 36/40 | 1. gọi báo cáo | • `gateOpen = false`, lý do nêu chỉ số kết quả | US-S5E6-06-FC5 |
| TC-S5E6-35 | Hai đợt liên tiếp không đạt thì cổng tự đóng và báo giáo viên | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • 11A1 đã bật AI đề xuất; hai đợt 10 bài liên tiếp, mỗi đợt 50 cặp, (b) 35/50 | • Hàng dữ liệu: F-S5-010<br/>• Giá trị: 2 đợt × 10 bài × 50 cặp; (b) 35/50 | 1. hệ thống tính lại sau bài cuối của đợt thứ hai | • `gateOpen = false`<br/>• cô Lan có thông báo "AI đề xuất đã tạm tắt cho 11A1"<br/>• bài kế tiếp mặc định Chạy song song<br/>• sau một đợt đầu không đạt, cổng vẫn mở | US-S5E6-06-FC6 |
| TC-S5E6-36 | Mở lại cổng khi đạt lại trên 20 bài song song mới | Medium | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • cổng 11A1 đã tự đóng ở FC6; sau đó 20 bài song song mới với số liệu như SC2 (tỉ lệ) | • Hàng dữ liệu: F-S5-010<br/>• Giá trị: 20 bài mới, (b) 95/100 | 1. gọi báo cáo | • `gateOpen = true`<br/>• báo cáo chỉ tính 20 bài sau thời điểm đóng | US-S5E6-06-SC3 |
| TC-S5E6-37 | Báo cáo có tỉ lệ sửa và thời gian duyệt | Medium | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • 20 bài đã duyệt ở chế độ AI đề xuất; 14/100 tiêu chí bị sửa; thời gian duyệt từng bài đã ghi | • Giá trị: 20 bài, 14/100 tiêu chí sửa | 1. gọi báo cáo | • (f) = 14 % kèm n = 100<br/>• (g) trung vị thời gian duyệt tính bằng giây<br/>• (h) tỉ lệ AI không chấm kèm n | US-S5E6-06-SC4 |

## Truy vết story → tiêu chí → case

Tổng: 37 case · E2E-API 32 · E2E-UI 5.

| Story | Tiêu chí | Loại | Tiêu đề | Case | Tự động hóa |
|---|---|---|---|---|---|
| US-S5E6-01 | US-S5E6-01-SC1 | Success | Đề xuất cho BN-01 đủ 5 tiêu chí, tổng 10 | TC-S5E6-01 | E2E-API |
| US-S5E6-01 | US-S5E6-01-SC2 | Success | Luật chặn 2: đúng kết quả nhưng lập luận thiếu thì kết quả bị hạ | TC-S5E6-02 | E2E-API |
| US-S5E6-01 | US-S5E6-01-SC3 | Success | Luật chặn 1: sai hướng thì bước phụ thuộc hướng = 0 | TC-S5E6-03 | E2E-API |
| US-S5E6-01 | US-S5E6-01-SC4 | Success | Luật chặn 3: bước sai kéo bước phụ thuộc về 0, bước độc lập giữ | TC-S5E6-04 | E2E-API |
| US-S5E6-01 | US-S5E6-01-SC5 | Success | Luật chặn 4: hình sai thì mất phần hình và bước dựa trên hình | TC-S5E6-05 | E2E-API |
| US-S5E6-01 | US-S5E6-01-FC1 | Fail / biên | Điểm vượt tối đa: gọi lại một lần rồi AI không chấm | TC-S5E6-06 | E2E-API |
| US-S5E6-01 | US-S5E6-01-FC2 | Fail / biên | Học sinh không đọc được đề xuất | TC-S5E6-07 | E2E-API |
| US-S5E6-01 | US-S5E6-01-FC3 | Fail / biên | Bịa kiến thức bị gắn cờ và hạ độ tin cậy | TC-S5E6-08 | E2E-API |
| US-S5E6-02 | US-S5E6-02-SC1 | Success | AI từ chối vì trang mờ | TC-S5E6-09 | E2E-API |
| US-S5E6-02 | US-S5E6-02-SC2 | Success | Độ tin cậy thấp thì không hiện điểm | TC-S5E6-10 | E2E-API |
| US-S5E6-02 | US-S5E6-02-FC3 | Fail / biên | Biên độ tin cậy tổng: 0,60 được chấm, 0,59 thì không | TC-S5E6-11 | E2E-API |
| US-S5E6-02 | US-S5E6-02-FC1 | Fail / biên | Cổng AI hết giờ hai lần | TC-S5E6-12 | E2E-API |
| US-S5E6-02 | US-S5E6-02-FC2 | Fail / biên | Lớp tắt AI chấm thì không gọi AI | TC-S5E6-13 | E2E-API |
| US-S5E6-03 | US-S5E6-03-SC1 | Success | BN-01 đáp số khớp engine | TC-S5E6-14 | E2E-API |
| US-S5E6-03 | US-S5E6-03-SC2 | Success | Đáp số viết thập phân vẫn khớp | TC-S5E6-15 | E2E-API |
| US-S5E6-03 | US-S5E6-03-FC1 | Fail / biên | AI cho điểm kết quả nhưng engine báo lệch thì gắn cờ | TC-S5E6-16 | E2E-API |
| US-S5E6-03 | US-S5E6-03-FC2 | Fail / biên | Không tìm thấy đáp số cuối thì không gắn cờ lệch | TC-S5E6-17 | E2E-API |
| US-S5E6-04 | US-S5E6-04-SC1 | Success | Bấm tiêu chí khoanh vùng ảnh, nhận từng tiêu chí rồi duyệt | TC-S5E6-18 | E2E-UI |
| US-S5E6-04 | US-S5E6-04-SC2 | Success | Sửa một tiêu chí, bỏ một tiêu chí rồi nhập tay | TC-S5E6-19 | E2E-UI |
| US-S5E6-04 | US-S5E6-04-SC3 | Success | Nhận cả bài và phím tắt cho bài không cờ | TC-S5E6-20 | E2E-UI |
| US-S5E6-04 | US-S5E6-04-FC1 | Fail / biên | Bài có cờ không có "Nhận cả bài" | TC-S5E6-21 | E2E-UI |
| US-S5E6-04 | US-S5E6-04-FC2 | Fail / biên | Gọi API duyệt cả bài có cờ bị từ chối | TC-S5E6-22 | E2E-API |
| US-S5E6-05 | US-S5E6-05-SC1 | Success | Trước khi duyệt không thấy đề xuất | TC-S5E6-23 | E2E-API |
| US-S5E6-05 | US-S5E6-05-SC2 | Success | Sau khi duyệt hiện so sánh và lưu cặp | TC-S5E6-24 | E2E-API |
| US-S5E6-05 | US-S5E6-05-SC3 | Success | Hàng chờ ở chế độ song song không lộ trạng thái AI | TC-S5E6-25 | E2E-API |
| US-S5E6-05 | US-S5E6-05-FC1 | Fail / biên | Sửa điểm sau khi thấy đề xuất không đổi cặp đã lưu | TC-S5E6-26 | E2E-API |
| US-S5E6-05 | US-S5E6-05-FC2 | Fail / biên | Màn chấm ở chế độ song song không hiện khung đề xuất | TC-S5E6-27 | E2E-UI |
| US-S5E6-06 | US-S5E6-06-SC1 | Success | Báo cáo khớp của 11A1 có n và KTC Wilson | TC-S5E6-28 | E2E-API |
| US-S5E6-06 | US-S5E6-06-SC2 | Success | Đạt đủ 5 điều kiện bằng cận dưới KTC thì mở được chế độ AI đề xuất | TC-S5E6-29 | E2E-API |
| US-S5E6-06 | US-S5E6-06-FC1 | Fail / biên | Chưa đủ 20 bài thì cổng đóng | TC-S5E6-30 | E2E-API |
| US-S5E6-06 | US-S5E6-06-FC2 | Fail / biên | Một dạng bài dưới 70 % thì cổng đóng dù macro đạt | TC-S5E6-31 | E2E-API |
| US-S5E6-06 | US-S5E6-06-FC3 | Fail / biên | Báo nhầm vượt 10 % thì cổng đóng | TC-S5E6-32 | E2E-API |
| US-S5E6-06 | US-S5E6-06-FC4 | Fail / biên | Một lần bịa kiến thức thì cổng đóng | TC-S5E6-33 | E2E-API |
| US-S5E6-06 | US-S5E6-06-FC5 | Fail / biên | Kết quả AI đúng chiều engine dưới 95 % thì cổng đóng | TC-S5E6-34 | E2E-API |
| US-S5E6-06 | US-S5E6-06-FC6 | Fail / biên | Hai đợt liên tiếp không đạt thì cổng tự đóng và báo giáo viên | TC-S5E6-35 | E2E-API |
| US-S5E6-06 | US-S5E6-06-SC3 | Success | Mở lại cổng khi đạt lại trên 20 bài song song mới | TC-S5E6-36 | E2E-API |
| US-S5E6-06 | US-S5E6-06-SC4 | Success | Báo cáo có tỉ lệ sửa và thời gian duyệt | TC-S5E6-37 | E2E-API |

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-28 | Tạo test case chặng 5 từ AC (goal run) | agent spec-s5 (QC) |
| v0.2 | 2026-11-28 | Sửa theo soát độc lập: cột dữ liệu cụ thể, case phi chức năng, đổi tầng 4 case | agent spec-s5 (QC) |
