Phiên bản: v0.1 · 2026-11-28
# S5-E5 — test case (Thông báo do trợ lý soạn, tổng hợp lỗi sai của lớp (COP-02), công tắc và hạn mức AI của lớp)

> Chủ sở hữu: QC. Lập từ `stories_ac.md` cùng thư mục: mỗi tiêu chí có ít nhất một case, kỳ vọng bám Given / When / Then. **Trạng thái: ready for R3 (goal run)**. AI là giả lập (`MockAiProvider`, D-GEO-047): không gọi AI thật, kỳ vọng chỉ soát hành vi máy chủ và dữ liệu trả về, không soát chất lượng câu chữ AI.

- Mỗi dòng là một case. Chuẩn bị theo **Pre-condition**, dùng **Test data**, làm lần lượt **Test step**, so với **Expected output**; đạt khi mọi gạch đầu dòng đều đúng.
- **Tự động hóa** chỉ có hai giá trị: `E2E-API` (gọi HTTP thẳng, soát `code` và thân phản hồi; mặc định) và `E2E-UI` (Playwright, chỉ dùng khi điều được kiểm là tương tác trên màn: form, chụp ảnh, khoanh vùng, nhận/bỏ từng dòng).
- **Status** của vòng thực thi: Pass · Fail · Blocked (+ cần gì) · UT (+ lý do) · Not run. Kỳ vọng mang mã `QS5-nn` là giả định của PO; màn chỉ khác đúng điểm đó thì đánh Blocked kèm mã, không đánh Fail.
- Dữ liệu mẫu chung `F-S5-nnn` ở mục 0 của `stories_ac.md` cùng thư mục.

### US-S5E5-01 — Trợ lý soạn nháp thông báo lớp, giáo viên sửa và tự bấm Gửi (trong ứng dụng)

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S5E5-01 | Soạn nháp nhắc hạn BC-01, sửa một câu và gửi | High | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (F-S5-001) | • BC-01 hạn T6 11/12/2026 20:00; giả lập `notice_draft` trả 3 câu | • Hàng dữ liệu theo Pre-condition | 1. mở "Trợ lý · Thông báo", chọn "Nhắc hạn nộp" và bài BC-01<br/>2. sửa câu cuối<br/>3. bấm Gửi | • nháp nhãn "AI soạn" có "20:00, 11/12/2026"<br/>• sau Gửi hiện MSG-S5-55 với n = 8<br/>• 8 em Đang học có thông báo; Văn Tú (Đã rời) không có | US-S5E5-01-SC1 |
| TC-S5E5-02 | Nháp chưa gửi thì học sinh không thấy | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Bình An | • nháp ở SC1 chưa bấm Gửi | • Hàng dữ liệu theo Pre-condition | 1. Bình An gọi `GET /api/notifications` | • không có thông báo nào từ nháp đó | US-S5E5-01-SC2 |
| TC-S5E5-03 | AI viết sai giờ hạn thì máy chủ thay bằng giờ đúng | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • giả lập trả "hạn nộp 21:00 thứ Sáu" | • Hàng dữ liệu theo Pre-condition | 1. gửi tạo nháp nhắc hạn BC-01 | • nháp có "20:00"<br/>• chỗ đó gắn "AI chưa chắc" | US-S5E5-01-FC1 |
| TC-S5E5-04 | Thông báo 1 001 ký tự bị từ chối | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • nháp đã sửa thành 1 001 ký tự | • Hàng dữ liệu theo Pre-condition | 1. bấm Gửi (gọi `POST /api/notices/{id}/send`) | • VALIDATION_FAILED, MSG-S5-56<br/>• không em nào nhận | US-S5E5-01-FC2 |
| TC-S5E5-05 | Không gửi được tới lớp của người khác | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • lớp 10B | • Hàng dữ liệu theo Pre-condition | 1. cô Lan tạo nháp và gửi với `classId` 10B | • NOT_FOUND | US-S5E5-01-FC3 |

### US-S5E5-02 — Tổng hợp lỗi sai của lớp theo tiêu chí và loại lỗi (COP-02), có bằng chứng, không gắn nhãn học sinh

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S5E5-06 | Tổng hợp BC-01 với 6 bài đã duyệt | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • BC-01 có 6 `ApprovedGrade`: 3 bài mất điểm "Bước" loại `sai-tinh-toan`, 1 bài `sai-hinh-dung`; giả lập `class_errors` trả 2 nhận định | • Hàng dữ liệu theo Pre-condition | 1. gọi `GET /api/graded-assignments/BC-01/error-summary` | • số liệu thô: "Bước" 4/6 bài mất điểm; `sai-tinh-toan` 3, `sai-hinh-dung` 1<br/>• 2 nhận định, mỗi nhận định có danh sách bài căn cứ<br/>• không có tên học sinh trong phần nhận định | US-S5E5-02-SC1 |
| TC-S5E5-07 | Bấm căn cứ mở đúng bài làm | Medium | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (F-S5-001) | • kết quả ở SC1 | • Hàng dữ liệu theo Pre-condition | 1. mở thẻ "Cả lớp hay sai ở đâu"<br/>2. bấm "Bài 2" ở nhận định 1 | • mở màn xem bài nộp tương ứng (ảnh, điểm đã duyệt)<br/>• thẻ không có cột xếp hạng hay phần trăm | US-S5E5-02-SC2 |
| TC-S5E5-08 | Dưới 5 bài thì không tổng hợp | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • BC-01 có 4 bài đã duyệt | • Hàng dữ liệu theo Pre-condition | 1. gọi error-summary | • trả MSG-S5-57, không có số liệu<br/>• không gọi AI | US-S5E5-02-FC1 |
| TC-S5E5-09 | Nhận định có tên học sinh hoặc phần trăm bị loại | High | Abnormal (Fail case/Edge case) | dữ liệu trả về | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • giả lập trả nhận định "Chi Mai và 50 % lớp sai bước tính SB" | • Hàng dữ liệu theo Pre-condition | 1. gọi error-summary | • nhận định đó không có trong phản hồi<br/>• số liệu thô vẫn hiện<br/>• `AiCallLog` ghi kết quả kiểm "loại 1 nhận định" | US-S5E5-02-FC2 |
| TC-S5E5-10 | Lớp tắt trợ lý AI thì chỉ có số liệu thô | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • 11A1 tắt trợ lý AI (US-S5E5-04) | • Hàng dữ liệu theo Pre-condition | 1. gọi error-summary | • số liệu thô và MSG-S5-58<br/>• không có dòng `AiCallLog` mới | US-S5E5-02-FC3 |
| TC-S5E5-11 | Học sinh không xem được tổng hợp lỗi | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Bình An | • BC-01 đã tổng hợp | • Hàng dữ liệu theo Pre-condition | 1. Bình An gọi error-summary | • FORBIDDEN | US-S5E5-02-FC4 |

### US-S5E5-03 — "Nên nhắc lại trên lớp": từ nhận định gợi ý bài luyện và tài liệu trong kho, giáo viên chọn rồi giao hoặc chiếu

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S5E5-12 | Từ nhận định chọn 2 câu và tạo bộ luyện nháp | High | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (F-S5-001) | • kết quả tổng hợp BC-01 có nhận định loại `sai-tinh-toan` | • Hàng dữ liệu theo Pre-condition | 1. bấm "Nên nhắc lại trên lớp"<br/>2. tick 2 câu gợi ý<br/>3. bấm "Tạo bộ luyện" | • danh sách có tối đa 3 câu cùng chủ đề khoảng cách và tối đa 2 mục tài liệu<br/>• bộ luyện mới trạng thái Nháp, có 2 câu đã chọn, chưa giao lớp nào | US-S5E5-03-SC1 |
| TC-S5E5-13 | Câu có nhiễu cùng loại lỗi được xếp trước | Medium | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • ngân hàng có 5 câu cùng dạng, 2 câu có nhiễu `sai-tinh-toan` | • Hàng dữ liệu theo Pre-condition | 1. gọi `GET /api/graded-assignments/BC-01/error-summary/1/suggestions` | • 2 câu có nhiễu `sai-tinh-toan` đứng đầu<br/>• `AiCallLog` không có dòng mới | US-S5E5-03-SC2 |
| TC-S5E5-14 | Kho không có câu cùng dạng thì nói rõ | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • đề của bài thuộc dạng chưa có trong ngân hàng | • Hàng dữ liệu theo Pre-condition | 1. gọi suggestions | • danh sách câu rỗng kèm câu "Kho chưa có bài cùng dạng"<br/>• mục tài liệu vẫn hiện nếu có | US-S5E5-03-FC1 |

### US-S5E5-04 — Công tắc AI chấm và trợ lý theo lớp, tắt khẩn, hạn mức lượt AI mỗi lớp và nhật ký dùng AI cho giáo viên

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S5E5-15 | Tắt AI chấm của 11A1 có hiệu lực ngay và có nhật ký | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • 11A1 AI chấm bật; BN-01 đang chờ chấm nền | • Hàng dữ liệu theo Pre-condition | 1. gửi `PUT /api/classes/11A1/switches` `{"aiGrading":false}` | • trả 200, MSG-S5-59<br/>• việc chấm nền của BN-01 bị huỷ, không có `GradingProposal` mới<br/>• `AuditEvent` ghi người thực hiện cô Lan, trước bật, sau tắt | US-S5E5-04-SC1 |
| TC-S5E5-16 | Báo cáo dùng AI 30 ngày của lớp | Medium | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • 11A1 có 120 lượt `kb_qa`, 40 lượt `grade`, 5 lượt bị chặn | • Hàng dữ liệu theo Pre-condition | 1. gọi `GET /api/classes/11A1/ai-usage?days=30` | • trả số lượt theo task và số lượt bị chặn<br/>• không có tên hay mã học sinh | US-S5E5-04-SC2 |
| TC-S5E5-17 | Lượt hỏi thứ 301 trong ngày của lớp bị chặn, bài vẫn làm được | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Bình An | • 11A1 đã dùng 300 lượt hỏi đáp hôm nay | • Hàng dữ liệu theo Pre-condition | 1. Bình An gửi câu hỏi<br/>2. Bình An gửi đáp số bài luyện | • câu hỏi trả RATE_LIMITED kèm `Retry-After`, MSG-S5-60<br/>• gửi đáp số vẫn được chấm tự động bình thường | US-S5E5-04-FC1 |
| TC-S5E5-18 | Học sinh và giáo viên khác không đổi được công tắc | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Bình An; thầy Khôi | • 11A1 của cô Lan | • Hàng dữ liệu theo Pre-condition | 1. Bình An gửi `PUT /api/classes/11A1/switches`<br/>2. thầy Khôi gửi cùng yêu cầu | • Bình An FORBIDDEN<br/>• thầy Khôi NOT_FOUND<br/>• công tắc không đổi | US-S5E5-04-FC2 |

## Truy vết story → tiêu chí → case

Tổng: 18 case · E2E-API 15 · E2E-UI 3.

| Story | Tiêu chí | Loại | Tiêu đề | Case | Tự động hóa |
|---|---|---|---|---|---|
| US-S5E5-01 | US-S5E5-01-SC1 | Success | Soạn nháp nhắc hạn BC-01, sửa một câu và gửi | TC-S5E5-01 | E2E-UI |
| US-S5E5-01 | US-S5E5-01-SC2 | Success | Nháp chưa gửi thì học sinh không thấy | TC-S5E5-02 | E2E-API |
| US-S5E5-01 | US-S5E5-01-FC1 | Fail / biên | AI viết sai giờ hạn thì máy chủ thay bằng giờ đúng | TC-S5E5-03 | E2E-API |
| US-S5E5-01 | US-S5E5-01-FC2 | Fail / biên | Thông báo 1 001 ký tự bị từ chối | TC-S5E5-04 | E2E-API |
| US-S5E5-01 | US-S5E5-01-FC3 | Fail / biên | Không gửi được tới lớp của người khác | TC-S5E5-05 | E2E-API |
| US-S5E5-02 | US-S5E5-02-SC1 | Success | Tổng hợp BC-01 với 6 bài đã duyệt | TC-S5E5-06 | E2E-API |
| US-S5E5-02 | US-S5E5-02-SC2 | Success | Bấm căn cứ mở đúng bài làm | TC-S5E5-07 | E2E-UI |
| US-S5E5-02 | US-S5E5-02-FC1 | Fail / biên | Dưới 5 bài thì không tổng hợp | TC-S5E5-08 | E2E-API |
| US-S5E5-02 | US-S5E5-02-FC2 | Fail / biên | Nhận định có tên học sinh hoặc phần trăm bị loại | TC-S5E5-09 | E2E-API |
| US-S5E5-02 | US-S5E5-02-FC3 | Fail / biên | Lớp tắt trợ lý AI thì chỉ có số liệu thô | TC-S5E5-10 | E2E-API |
| US-S5E5-02 | US-S5E5-02-FC4 | Fail / biên | Học sinh không xem được tổng hợp lỗi | TC-S5E5-11 | E2E-API |
| US-S5E5-03 | US-S5E5-03-SC1 | Success | Từ nhận định chọn 2 câu và tạo bộ luyện nháp | TC-S5E5-12 | E2E-UI |
| US-S5E5-03 | US-S5E5-03-SC2 | Success | Câu có nhiễu cùng loại lỗi được xếp trước | TC-S5E5-13 | E2E-API |
| US-S5E5-03 | US-S5E5-03-FC1 | Fail / biên | Kho không có câu cùng dạng thì nói rõ | TC-S5E5-14 | E2E-API |
| US-S5E5-04 | US-S5E5-04-SC1 | Success | Tắt AI chấm của 11A1 có hiệu lực ngay và có nhật ký | TC-S5E5-15 | E2E-API |
| US-S5E5-04 | US-S5E5-04-SC2 | Success | Báo cáo dùng AI 30 ngày của lớp | TC-S5E5-16 | E2E-API |
| US-S5E5-04 | US-S5E5-04-FC1 | Fail / biên | Lượt hỏi thứ 301 trong ngày của lớp bị chặn, bài vẫn làm được | TC-S5E5-17 | E2E-API |
| US-S5E5-04 | US-S5E5-04-FC2 | Fail / biên | Học sinh và giáo viên khác không đổi được công tắc | TC-S5E5-18 | E2E-API |

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-28 | Tạo test case chặng 5 từ AC (goal run) | agent spec-s5 (QC) |
