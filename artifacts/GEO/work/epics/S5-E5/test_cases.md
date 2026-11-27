Phiên bản: v0.2 · 2026-11-28
# S5-E5 — test case (Thông báo do trợ lý soạn, tổng hợp lỗi sai của lớp (COP-02), công tắc và hạn mức AI của lớp)

> Chủ sở hữu: QC. Lập từ `stories_ac.md` cùng thư mục: mỗi tiêu chí có ít nhất một case, kỳ vọng bám Given / When / Then. **Trạng thái: ready for R3 (goal run)**. AI là giả lập (`MockAiProvider`, D-GEO-047): không gọi AI thật, kỳ vọng chỉ soát hành vi máy chủ và dữ liệu trả về, không soát chất lượng câu chữ AI.

- Mỗi dòng là một case. Chuẩn bị theo **Pre-condition**, dùng **Test data**, làm lần lượt **Test step**, so với **Expected output**; đạt khi mọi gạch đầu dòng đều đúng.
- **Tự động hóa** chỉ có hai giá trị: `E2E-API` (gọi HTTP thẳng, soát `code` và thân phản hồi; mặc định) và `E2E-UI` (Playwright, chỉ dùng khi điều được kiểm là tương tác trên màn: form, chụp ảnh, khoanh vùng, nhận/bỏ từng dòng).
- **Status** của vòng thực thi: Pass · Fail · Blocked (+ cần gì) · UT (+ lý do) · Not run. Kỳ vọng mang mã `QS5-nn` là giả định của PO; màn chỉ khác đúng điểm đó thì đánh Blocked kèm mã, không đánh Fail.
- Dữ liệu mẫu chung `F-S5-nnn` ở mục 0 của `stories_ac.md` cùng thư mục.

### US-S5E5-01 — Trợ lý soạn nháp thông báo lớp, giáo viên sửa và tự bấm Gửi (trong ứng dụng)

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S5E5-01 | Soạn nháp nhắc hạn BC-01, sửa một câu và gửi | High | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (F-S5-001) | • BC-01 hạn T6 11/12/2026 20:00; giả lập `notice_draft` trả 3 câu | • Hàng dữ liệu: F-S5-052<br/>• Giá trị nhập: "Trợ lý · Thông báo"; "Nhắc hạn nộp"; `notice_draft` | 1. mở "Trợ lý · Thông báo", chọn "Nhắc hạn nộp" và bài BC-01<br/>2. sửa câu cuối<br/>3. bấm Gửi | • nháp nhãn "AI soạn" có "20:00, 11/12/2026"<br/>• sau Gửi hiện MSG-S5-55 với n = 8<br/>• 8 em Đang học có thông báo; Văn Tú (Đã rời) không có | US-S5E5-01-SC1 |
| TC-S5E5-02 | Nháp chưa gửi thì học sinh không thấy | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Bình An | • nháp ở SC1 chưa bấm Gửi | • Hàng dữ liệu: F-S5-010<br/>• Giá trị nhập: `GET /api/notifications` | 1. Bình An gọi `GET /api/notifications` | • không có thông báo nào từ nháp đó | US-S5E5-01-SC2 |
| TC-S5E5-03 | AI chèn tên học sinh vào thông báo thì bị loại khỏi nháp | High | Abnormal (Fail case/Edge case) | dữ liệu trả về | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • giả lập `notice_draft` trả "Nhắc Chi Mai và cả lớp nộp bài trước 20:00" | • Hàng dữ liệu: F-S5-010, F-S5-052<br/>• Giá trị: F-S5-010, F-S5-052 | 1. gửi tạo nháp nhắc hạn BC-01 | • nháp không chứa "Chi Mai"; câu đó gắn "AI chưa chắc"<br/>• yêu cầu gửi AI không chứa tên hay mã học sinh nào của 11A1 | US-S5E5-01-FC4 |
| TC-S5E5-04 | Thông báo đã gửi có dòng "Soạn với trợ lý AI, thầy cô đã duyệt" | Medium | Normal (Success case) | chức năng | tích hợp API | E2E-API | Bình An | • nháp ở SC1 đã gửi | • Hàng dữ liệu: F-S5-001, F-S5-010<br/>• Giá trị: F-S5-001, F-S5-010 | 1. Bình An gọi `GET /api/notifications/{id}` | • người gửi "Cô Lan"<br/>• có dòng "Soạn với trợ lý AI, thầy cô đã duyệt" | US-S5E5-01-SC3 |
| TC-S5E5-05 | Lượt soạn thứ 31 trong ngày bị chặn ở trợ lý thông báo | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • cô Lan đã dùng 30 lượt soạn hôm nay (giáo án và đề) | • Hàng dữ liệu: F-S5-001<br/>• Giá trị: F-S5-001<br/>• Giá trị: bộ đếm 30/30 | 1. gửi tạo nháp thông báo | • RATE_LIMITED, MSG-S5-06<br/>• không gọi AI | US-S5E5-01-FC5 |
| TC-S5E5-06 | AI viết sai giờ hạn thì máy chủ thay bằng giờ đúng | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • giả lập trả "hạn nộp 21:00 thứ Sáu" | • Hàng dữ liệu: F-S5-052<br/>• Giá trị nhập: "hạn nộp 21:00 thứ Sáu" | 1. gửi tạo nháp nhắc hạn BC-01 | • nháp có "20:00"<br/>• chỗ đó gắn "AI chưa chắc" | US-S5E5-01-FC1 |
| TC-S5E5-07 | Thông báo 1 001 ký tự bị từ chối | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • nháp đã sửa thành 1 001 ký tự | • Giá trị nhập: `POST /api/notices/{id}/send`; 001 ký tự | 1. bấm Gửi (gọi `POST /api/notices/{id}/send`) | • VALIDATION_FAILED, MSG-S5-56<br/>• không em nào nhận | US-S5E5-01-FC2 |
| TC-S5E5-08 | Không gửi được tới lớp của người khác | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • lớp 10B | • Hàng dữ liệu: F-S5-001, F-S5-002<br/>• Giá trị nhập: `classId` | 1. cô Lan tạo nháp và gửi với `classId` 10B | • NOT_FOUND | US-S5E5-01-FC3 |

### US-S5E5-02 — Tổng hợp lỗi sai của lớp theo tiêu chí và loại lỗi (COP-02), có bằng chứng, không gắn nhãn học sinh

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S5E5-09 | Tổng hợp BC-01 với 6 bài đã duyệt | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • BC-01 có 6 `ApprovedGrade`: 3 bài mất điểm "Bước" loại `sai-tinh-toan`, 1 bài `sai-hinh-dung`; giả lập `class_errors` trả 2 nhận định | • Hàng dữ liệu: F-S5-052<br/>• Giá trị nhập: `GET /api/graded-assignments/BC-01/error-summary`; `ApprovedGrade`; 3 bài; "Bước"; `sai-tinh-toan`; 1 bài; `sai-hinh-dung`; `class_errors` | 1. gọi `GET /api/graded-assignments/BC-01/error-summary` | • số liệu thô: "Bước" 4/6 bài mất điểm; `sai-tinh-toan` 3, `sai-hinh-dung` 1<br/>• 2 nhận định, mỗi nhận định có danh sách bài căn cứ<br/>• không có tên học sinh trong phần nhận định | US-S5E5-02-SC1 |
| TC-S5E5-10 | Bấm căn cứ mở đúng bài làm | Medium | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (F-S5-001) | • kết quả ở SC1 | • Giá trị nhập: "Cả lớp hay sai ở đâu"; "Bài 2" | 1. mở thẻ "Cả lớp hay sai ở đâu"<br/>2. bấm "Bài 2" ở nhận định 1 | • mở màn xem bài nộp tương ứng (ảnh, điểm đã duyệt)<br/>• thẻ không có cột xếp hạng hay phần trăm | US-S5E5-02-SC2 |
| TC-S5E5-11 | Nhận xét của giáo viên có tên học sinh được gỡ trước khi gửi AI | High | Normal (Success case) | dữ liệu trả về | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • BC-01 có 6 bài đã duyệt; nhận xét BN-02 "Chi Mai nhầm SB, xem lại"; nhận xét BN-07 "minh quan thieu buoc dung AH" | • Hàng dữ liệu: F-S5-010, F-S5-052, F-S5-054, F-S5-057<br/>• Giá trị: F-S5-054, F-S5-057<br/>• Giá trị: nhận xét có dấu và không dấu | 1. gọi error-summary | • yêu cầu gửi `class_errors` có "[HS] nhầm SB" và "[HS] thieu buoc dung AH"<br/>• không có "Chi Mai", "minh quan", mã `HS-` nào | US-S5E5-02-SC3 |
| TC-S5E5-12 | Dưới 5 bài thì không tổng hợp | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • BC-01 có 4 bài đã duyệt | • Hàng dữ liệu: F-S5-052<br/>• Giá trị nhập: 4 bài | 1. gọi error-summary | • trả MSG-S5-57, không có số liệu<br/>• không gọi AI | US-S5E5-02-FC1 |
| TC-S5E5-13 | Nhận định có tên học sinh hoặc phần trăm bị loại | High | Abnormal (Fail case/Edge case) | dữ liệu trả về | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • giả lập trả nhận định "Chi Mai và 50 % lớp sai bước tính SB" | • Hàng dữ liệu: F-S5-010<br/>• Giá trị nhập: "Chi Mai và 50 % lớp sai bước tính SB" | 1. gọi error-summary | • nhận định đó không có trong phản hồi<br/>• số liệu thô vẫn hiện<br/>• `AiCallLog` ghi kết quả kiểm "loại 1 nhận định" | US-S5E5-02-FC2 |
| TC-S5E5-14 | Lớp tắt trợ lý AI thì chỉ có số liệu thô | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • 11A1 tắt trợ lý AI (US-S5E5-04) | • Hàng dữ liệu: F-S5-010 | 1. gọi error-summary | • số liệu thô và MSG-S5-58<br/>• không có dòng `AiCallLog` mới | US-S5E5-02-FC3 |
| TC-S5E5-15 | Học sinh không xem được tổng hợp lỗi | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Bình An | • BC-01 đã tổng hợp | • Hàng dữ liệu: F-S5-010, F-S5-052 | 1. Bình An gọi error-summary | • FORBIDDEN | US-S5E5-02-FC4 |

### US-S5E5-03 — "Nên nhắc lại trên lớp": từ nhận định gợi ý bài luyện và tài liệu trong kho, giáo viên chọn rồi giao hoặc chiếu

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S5E5-16 | Từ nhận định chọn 2 câu và tạo bộ luyện nháp | High | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (F-S5-001) | • kết quả tổng hợp BC-01 có nhận định loại `sai-tinh-toan` | • Hàng dữ liệu: F-S5-052<br/>• Giá trị nhập: "Nên nhắc lại trên lớp"; "Tạo bộ luyện"; `sai-tinh-toan` | 1. bấm "Nên nhắc lại trên lớp"<br/>2. tick 2 câu gợi ý<br/>3. bấm "Tạo bộ luyện" | • danh sách có tối đa 3 câu cùng chủ đề khoảng cách và tối đa 2 mục tài liệu<br/>• bộ luyện mới trạng thái Nháp, có 2 câu đã chọn, chưa giao lớp nào | US-S5E5-03-SC1 |
| TC-S5E5-17 | Câu có nhiễu cùng loại lỗi được xếp trước | Medium | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • ngân hàng có 5 câu cùng dạng, 2 câu có nhiễu `sai-tinh-toan` | • Hàng dữ liệu: F-S5-052<br/>• Giá trị nhập: `GET /api/graded-assignments/BC-01/error-summary/1/suggestions`; `sai-tinh-toan` | 1. gọi `GET /api/graded-assignments/BC-01/error-summary/1/suggestions` | • 2 câu có nhiễu `sai-tinh-toan` đứng đầu<br/>• `AiCallLog` không có dòng mới | US-S5E5-03-SC2 |
| TC-S5E5-18 | Kho không có câu cùng dạng thì nói rõ | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • đề của bài thuộc dạng chưa có trong ngân hàng | • Hàng dữ liệu: F-S5-032, F-S5-040<br/>• Giá trị: F-S5-032, F-S5-040<br/>• Giá trị: đề dạng không có trong ngân hàng | 1. gọi suggestions | • danh sách câu rỗng kèm câu "Kho chưa có bài cùng dạng"<br/>• mục tài liệu vẫn hiện nếu có | US-S5E5-03-FC1 |

### US-S5E5-04 — Công tắc AI theo lớp, tắt khẩn toàn hệ thống, định nghĩa lượt, hạn mức theo lớp, học sinh và token, báo cáo dùng AI

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S5E5-19 | Tắt AI chấm của 11A1 có hiệu lực ngay và có nhật ký | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • 11A1 AI chấm bật; BN-01 đang chờ chấm nền | • Hàng dữ liệu: F-S5-010, F-S5-053<br/>• Giá trị nhập: `PUT /api/classes/11A1/switches`; `{"aiGrading":false}` | 1. gửi `PUT /api/classes/11A1/switches` `{"aiGrading":false}` | • trả 200, MSG-S5-59<br/>• việc chấm nền của BN-01 bị huỷ, không có `GradingProposal` mới<br/>• `AuditEvent` ghi người thực hiện cô Lan, trước bật, sau tắt | US-S5E5-04-SC1 |
| TC-S5E5-20 | Báo cáo dùng AI 30 ngày của lớp | Medium | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • 11A1 có 120 lượt `kb_qa`, 40 lượt `grade`, 5 lượt bị chặn | • Hàng dữ liệu: F-S5-010<br/>• Giá trị nhập: `GET /api/classes/11A1/ai-usage?days=30`; 120 lượt; `kb_qa`; 40 lượt; `grade`; 5 lượt | 1. gọi `GET /api/classes/11A1/ai-usage?days=30` | • trả số lượt theo task và số lượt bị chặn<br/>• không có tên hay mã học sinh | US-S5E5-04-SC2 |
| TC-S5E5-21 | Lượt hỏi thứ 301 trong ngày của lớp bị chặn, bài vẫn làm được | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Bình An | • 11A1 đã dùng 300 lượt hỏi đáp hôm nay | • Hàng dữ liệu: F-S5-010<br/>• Giá trị nhập: 300 lượt | 1. Bình An gửi câu hỏi<br/>2. Bình An gửi đáp số bài luyện | • câu hỏi trả RATE_LIMITED kèm `Retry-After`, MSG-S5-60<br/>• gửi đáp số vẫn được chấm tự động bình thường | US-S5E5-04-FC1 |
| TC-S5E5-22 | Học sinh và giáo viên khác không đổi được công tắc | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Bình An; thầy Khôi | • 11A1 của cô Lan | • Hàng dữ liệu: F-S5-001, F-S5-002, F-S5-010<br/>• Giá trị nhập: `PUT /api/classes/11A1/switches` | 1. Bình An gửi `PUT /api/classes/11A1/switches`<br/>2. thầy Khôi gửi cùng yêu cầu | • Bình An FORBIDDEN<br/>• thầy Khôi NOT_FOUND<br/>• công tắc không đổi | US-S5E5-04-FC2 |
| TC-S5E5-23 | Lượt chấm thứ 201 của lớp: bài thành "AI không chấm", vẫn chấm tay | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • 11A1 đã dùng 200 lượt chấm hôm nay; BC-01 chế độ AI đề xuất | • Hàng dữ liệu: F-S5-010, F-S5-052<br/>• Giá trị: F-S5-010 (Ngọc Anh)<br/>• Giá trị: bộ đếm chấm 200/200 (biên: 199 còn chấm 1 trang) | 1. Ngọc Anh nộp bài 2 trang | • không có dòng `AiCallLog` task `grade` hay `grade_ocr` mới<br/>• bài cờ "AI không chấm", lý do "lớp đã hết lượt AI hôm nay"<br/>• cô Lan chấm tay được | US-S5E5-04-FC3 |
| TC-S5E5-24 | Một học sinh hết 30 lượt hỏi, bạn cùng lớp vẫn hỏi được | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Bình An; Chi Mai | • Bình An đã hỏi 30 lượt hôm nay; lớp mới dùng 45/300 | • Hàng dữ liệu: F-S5-010<br/>• Giá trị: F-S5-010<br/>• Giá trị: bộ đếm Bình An 30/30, lớp 45/300 | 1. Bình An hỏi lượt 31<br/>2. Chi Mai hỏi | • Bình An RATE_LIMITED kèm `Retry-After`<br/>• Chi Mai nhận câu trả lời | US-S5E5-04-FC4 |
| TC-S5E5-25 | Gọi lại tự động không tính thêm lượt; mỗi trang chấm tính một lượt | Medium | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • giả lập `grade` lỗi lần 1, đạt lần 2; BN-01 có 2 trang | • Hàng dữ liệu: F-S5-053, F-S5-090<br/>• Giá trị: F-S5-053, F-S5-090 | 1. chờ chấm nền BN-01 | • bộ đếm chấm của lớp tăng 2 (2 trang `grade_ocr`) + 2 (2 trang `grade`) = 4, không tính lần gọi lại<br/>• `AiCallLog` vẫn ghi đủ mọi lần gọi | US-S5E5-04-SC3 |
| TC-S5E5-26 | Đạt 80 % trần token thì giáo viên được cảnh báo một lần | Medium | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • 11A1 đã dùng 1 590 000 token hôm nay | • Hàng dữ liệu: F-S5-010<br/>• Giá trị: bộ đếm token 1 590 000 / 2 000 000 (biên 1 600 000) | 1. một lượt hỏi đáp dùng thêm 20 000 token<br/>2. một lượt nữa dùng thêm 20 000 token | • sau lượt đầu (1 610 000 ≥ 80 %) cô Lan có một thông báo cảnh báo<br/>• sau lượt thứ hai không có cảnh báo mới | US-S5E5-04-SC4 |
| TC-S5E5-27 | "Đọc lại" lần thứ 4 bị chặn | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • BN-01 đã bấm "Đọc lại" 3 lần | • Hàng dữ liệu: F-S5-001, F-S5-053<br/>• Giá trị: F-S5-053<br/>• Giá trị: bộ đếm đọc lại 3/3 | 1. cô Lan gửi `POST /api/submissions/BN-01/ocr/retry` | • RATE_LIMITED<br/>• không gọi `grade_ocr` | US-S5E5-04-FC5 |
| TC-S5E5-28 | Người vận hành tắt khẩn task `kb_qa` toàn hệ thống | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Người vận hành; Bình An; Mi Na | • người vận hành có vai Vận hành; 11A1 và 10B đang bật AI phía học sinh | • Hàng dữ liệu: F-S5-002, F-S5-010<br/>• Giá trị: F-S5-010, F-S5-002 (10B) | 1. gửi `PUT /api/admin/ai/kill` `{"task":"kb_qa","off":true}`<br/>2. sau 10 giây Bình An và Mi Na hỏi | • cả hai nhận thông báo AI đang tắt, không gọi AI<br/>• `AuditEvent` ghi người vận hành<br/>• giáo viên không đổi được công tắc này (FORBIDDEN) | US-S5E5-04-SC5 |
| TC-S5E5-29 | Báo cáo tỉ lệ nháp trợ lý được duyệt sau khi sửa | Medium | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • cô Lan trong 30 ngày có 10 giáo án nháp: 6 duyệt (4 có sửa), 4 bỏ; 3 thông báo nháp: 3 gửi (1 có sửa) | • Hàng dữ liệu: F-S5-001, F-S5-021<br/>• Giá trị: F-S5-001, F-S5-021<br/>• Giá trị: số nháp như Given | 1. gọi `GET /api/copilot/usage?days=30` | • giáo án: duyệt 60 %, duyệt sau sửa 40 % (n = 10)<br/>• thông báo: gửi 100 %, gửi sau sửa 33 % (n = 3) | US-S5E5-04-SC6 |

## Truy vết story → tiêu chí → case

Tổng: 29 case · E2E-API 26 · E2E-UI 3.

| Story | Tiêu chí | Loại | Tiêu đề | Case | Tự động hóa |
|---|---|---|---|---|---|
| US-S5E5-01 | US-S5E5-01-SC1 | Success | Soạn nháp nhắc hạn BC-01, sửa một câu và gửi | TC-S5E5-01 | E2E-UI |
| US-S5E5-01 | US-S5E5-01-SC2 | Success | Nháp chưa gửi thì học sinh không thấy | TC-S5E5-02 | E2E-API |
| US-S5E5-01 | US-S5E5-01-FC4 | Fail / biên | AI chèn tên học sinh vào thông báo thì bị loại khỏi nháp | TC-S5E5-03 | E2E-API |
| US-S5E5-01 | US-S5E5-01-SC3 | Success | Thông báo đã gửi có dòng "Soạn với trợ lý AI, thầy cô đã duyệt" | TC-S5E5-04 | E2E-API |
| US-S5E5-01 | US-S5E5-01-FC5 | Fail / biên | Lượt soạn thứ 31 trong ngày bị chặn ở trợ lý thông báo | TC-S5E5-05 | E2E-API |
| US-S5E5-01 | US-S5E5-01-FC1 | Fail / biên | AI viết sai giờ hạn thì máy chủ thay bằng giờ đúng | TC-S5E5-06 | E2E-API |
| US-S5E5-01 | US-S5E5-01-FC2 | Fail / biên | Thông báo 1 001 ký tự bị từ chối | TC-S5E5-07 | E2E-API |
| US-S5E5-01 | US-S5E5-01-FC3 | Fail / biên | Không gửi được tới lớp của người khác | TC-S5E5-08 | E2E-API |
| US-S5E5-02 | US-S5E5-02-SC1 | Success | Tổng hợp BC-01 với 6 bài đã duyệt | TC-S5E5-09 | E2E-API |
| US-S5E5-02 | US-S5E5-02-SC2 | Success | Bấm căn cứ mở đúng bài làm | TC-S5E5-10 | E2E-UI |
| US-S5E5-02 | US-S5E5-02-SC3 | Success | Nhận xét của giáo viên có tên học sinh được gỡ trước khi gửi AI | TC-S5E5-11 | E2E-API |
| US-S5E5-02 | US-S5E5-02-FC1 | Fail / biên | Dưới 5 bài thì không tổng hợp | TC-S5E5-12 | E2E-API |
| US-S5E5-02 | US-S5E5-02-FC2 | Fail / biên | Nhận định có tên học sinh hoặc phần trăm bị loại | TC-S5E5-13 | E2E-API |
| US-S5E5-02 | US-S5E5-02-FC3 | Fail / biên | Lớp tắt trợ lý AI thì chỉ có số liệu thô | TC-S5E5-14 | E2E-API |
| US-S5E5-02 | US-S5E5-02-FC4 | Fail / biên | Học sinh không xem được tổng hợp lỗi | TC-S5E5-15 | E2E-API |
| US-S5E5-03 | US-S5E5-03-SC1 | Success | Từ nhận định chọn 2 câu và tạo bộ luyện nháp | TC-S5E5-16 | E2E-UI |
| US-S5E5-03 | US-S5E5-03-SC2 | Success | Câu có nhiễu cùng loại lỗi được xếp trước | TC-S5E5-17 | E2E-API |
| US-S5E5-03 | US-S5E5-03-FC1 | Fail / biên | Kho không có câu cùng dạng thì nói rõ | TC-S5E5-18 | E2E-API |
| US-S5E5-04 | US-S5E5-04-SC1 | Success | Tắt AI chấm của 11A1 có hiệu lực ngay và có nhật ký | TC-S5E5-19 | E2E-API |
| US-S5E5-04 | US-S5E5-04-SC2 | Success | Báo cáo dùng AI 30 ngày của lớp | TC-S5E5-20 | E2E-API |
| US-S5E5-04 | US-S5E5-04-FC1 | Fail / biên | Lượt hỏi thứ 301 trong ngày của lớp bị chặn, bài vẫn làm được | TC-S5E5-21 | E2E-API |
| US-S5E5-04 | US-S5E5-04-FC2 | Fail / biên | Học sinh và giáo viên khác không đổi được công tắc | TC-S5E5-22 | E2E-API |
| US-S5E5-04 | US-S5E5-04-FC3 | Fail / biên | Lượt chấm thứ 201 của lớp: bài thành "AI không chấm", vẫn chấm tay | TC-S5E5-23 | E2E-API |
| US-S5E5-04 | US-S5E5-04-FC4 | Fail / biên | Một học sinh hết 30 lượt hỏi, bạn cùng lớp vẫn hỏi được | TC-S5E5-24 | E2E-API |
| US-S5E5-04 | US-S5E5-04-SC3 | Success | Gọi lại tự động không tính thêm lượt; mỗi trang chấm tính một lượt | TC-S5E5-25 | E2E-API |
| US-S5E5-04 | US-S5E5-04-SC4 | Success | Đạt 80 % trần token thì giáo viên được cảnh báo một lần | TC-S5E5-26 | E2E-API |
| US-S5E5-04 | US-S5E5-04-FC5 | Fail / biên | "Đọc lại" lần thứ 4 bị chặn | TC-S5E5-27 | E2E-API |
| US-S5E5-04 | US-S5E5-04-SC5 | Success | Người vận hành tắt khẩn task `kb_qa` toàn hệ thống | TC-S5E5-28 | E2E-API |
| US-S5E5-04 | US-S5E5-04-SC6 | Success | Báo cáo tỉ lệ nháp trợ lý được duyệt sau khi sửa | TC-S5E5-29 | E2E-API |

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-28 | Tạo test case chặng 5 từ AC (goal run) | agent spec-s5 (QC) |
| v0.2 | 2026-11-28 | Sửa theo soát độc lập: cột dữ liệu cụ thể, case phi chức năng, đổi tầng 4 case | agent spec-s5 (QC) |
