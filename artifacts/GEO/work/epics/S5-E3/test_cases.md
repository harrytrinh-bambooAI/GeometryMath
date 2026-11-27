Phiên bản: v0.1 · 2026-11-28
# S5-E3 — test case (Kho tri thức của giáo viên, hỏi đáp của học sinh theo kho, kiểm soát đáp án đầy đủ)

> Chủ sở hữu: QC. Lập từ `stories_ac.md` cùng thư mục: mỗi tiêu chí có ít nhất một case, kỳ vọng bám Given / When / Then. **Trạng thái: ready for R3 (goal run)**. AI là giả lập (`MockAiProvider`, D-GEO-047): không gọi AI thật, kỳ vọng chỉ soát hành vi máy chủ và dữ liệu trả về, không soát chất lượng câu chữ AI.

- Mỗi dòng là một case. Chuẩn bị theo **Pre-condition**, dùng **Test data**, làm lần lượt **Test step**, so với **Expected output**; đạt khi mọi gạch đầu dòng đều đúng.
- **Tự động hóa** chỉ có hai giá trị: `E2E-API` (gọi HTTP thẳng, soát `code` và thân phản hồi; mặc định) và `E2E-UI` (Playwright, chỉ dùng khi điều được kiểm là tương tác trên màn: form, chụp ảnh, khoanh vùng, nhận/bỏ từng dòng).
- **Status** của vòng thực thi: Pass · Fail · Blocked (+ cần gì) · UT (+ lý do) · Not run. Kỳ vọng mang mã `QS5-nn` là giả định của PO; màn chỉ khác đúng điểm đó thì đánh Blocked kèm mã, không đánh Fail.
- Dữ liệu mẫu chung `F-S5-nnn` ở mục 0 của `stories_ac.md` cùng thư mục.

### US-S5E3-01 — Kho tri thức: thêm tài liệu, phiên bản bất biến, trạng thái Nháp → Chờ duyệt → Đang dùng → Lưu trữ

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S5E3-01 | Tải tệp docx tạo tài liệu Nháp tách mục | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • cô Lan đã đăng nhập | • Hàng dữ liệu theo Pre-condition | 1. gửi `POST /api/knowledge` tệp "Phieu-on-chuong-VII.docx" 240 KB | • tài liệu mới trạng thái Nháp, phiên bản 1, chủ sở hữu cô Lan<br/>• danh sách mục có ít nhất 1 mục có tiêu đề<br/>• tệp gốc giữ nguyên, tải lại được | US-S5E3-01-SC1 |
| TC-S5E3-02 | Duyệt đưa phiên bản mới lên Đang dùng, bản cũ sang Lưu trữ | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • `TL-01` v2 Đang dùng; cô Lan vừa lưu v3 rồi gửi Chờ duyệt | • Hàng dữ liệu theo Pre-condition | 1. gửi `POST /api/knowledge/TL-01/versions/3/activate` | • v3 Đang dùng<br/>• v2 Lưu trữ<br/>• chỉ một phiên bản Đang dùng | US-S5E3-01-SC2 |
| TC-S5E3-03 | Giáo án đã duyệt đưa vào kho | Medium | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • giáo án F-S5-021 đã duyệt | • Hàng dữ liệu theo Pre-condition | 1. gửi `POST /api/knowledge` với `fromLessonPlan` = `GA-0001` | • tài liệu mới Nháp, nội dung tách theo 4 mục khung 5512<br/>• nguồn ghi "Giáo án GA-0001 v2" | US-S5E3-01-SC3 |
| TC-S5E3-04 | Sửa phiên bản đã dùng tạo phiên bản mới, không ghi đè | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • `TL-01` v2 Đang dùng | • Hàng dữ liệu theo Pre-condition | 1. gửi `PUT /api/knowledge/TL-01/versions/2` với nội dung sửa | • trả 201 kèm phiên bản 3 trạng thái Nháp và MSG-S5-23<br/>• nội dung v2 không đổi (so băm) | US-S5E3-01-FC1 |
| TC-S5E3-05 | Tệp 11 MB hoặc định dạng xlsx bị từ chối | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • cô Lan đã đăng nhập | • Hàng dữ liệu theo Pre-condition | 1. tải tệp pdf 11 MB<br/>2. tải tệp xlsx 50 KB | • cả hai VALIDATION_FAILED, MSG-S5-22 | US-S5E3-01-FC2 |
| TC-S5E3-06 | Tải lại tệp trùng không tạo bản mới | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • tệp ở SC1 đã có trong kho | • Hàng dữ liệu theo Pre-condition | 1. tải lại đúng tệp đó | • trả 200 kèm id tài liệu đã có và cờ `duplicate = true`<br/>• số phiên bản không đổi | US-S5E3-01-FC3 |
| TC-S5E3-07 | Giáo viên khác và học sinh không đọc được kho của cô Lan | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Thầy Khôi; Bình An | • kho F-S5-040 | • Hàng dữ liệu theo Pre-condition | 1. thầy Khôi gọi `GET /api/knowledge/TL-01`<br/>2. Bình An gọi cùng đường dẫn | • thầy Khôi NOT_FOUND<br/>• Bình An FORBIDDEN (học sinh không đọc kho trực tiếp, chỉ thấy trích dẫn trong câu trả lời) | US-S5E3-01-FC4 |

### US-S5E3-02 — Gắn tài liệu cho lớp hoặc bài, thời hạn hiệu lực

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S5E3-08 | Gắn TL-01 cho lớp 11A1 | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • kho F-S5-040 | • Hàng dữ liệu theo Pre-condition | 1. gửi `POST /api/knowledge/TL-01/grants` với `classId` 11A1 | • gắn kết hiệu lực<br/>• `GET /api/classes/11A1/knowledge` trả `TL-01` kèm phiên bản Đang dùng v2 | US-S5E3-02-SC1 |
| TC-S5E3-09 | Gắn kết có hạn tự hết hiệu lực | Medium | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • gắn `TL-01` cho bài BC-01 với `den_ngay` = T0 + 1 ngày | • Hàng dữ liệu theo Pre-condition | 1. gọi danh sách tài liệu của bài tại T0 và tại T0 + 1 ngày + 1 giây | • lần 1 có `TL-01`<br/>• lần 2 không có | US-S5E3-02-SC2 |
| TC-S5E3-10 | Không gắn được tài liệu chưa Đang dùng | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • `TL-02` Chờ duyệt | • Hàng dữ liệu theo Pre-condition | 1. gửi gắn `TL-02` cho 11A1 | • CONFLICT<br/>• không có gắn kết mới | US-S5E3-02-FC1 |
| TC-S5E3-11 | Không gắn được cho lớp của người khác | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • lớp 10B của thầy Khôi | • Hàng dữ liệu theo Pre-condition | 1. cô Lan gắn `TL-01` cho 10B | • NOT_FOUND | US-S5E3-02-FC2 |

### US-S5E3-03 — Hỏi đáp của học sinh dựa trên kho đã gắn, luôn kèm nguồn; không có tài liệu thì nói rõ

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S5E3-12 | Câu hỏi khái niệm nhận trả lời kèm nguồn TL-01 v2 | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Học sinh Bình An (11A1) | • `TL-01` v2 gắn 11A1; bài BC-01 đang làm; giả lập F-S5-094 | • Hàng dữ liệu theo Pre-condition | 1. Bình An gửi `POST /api/qa` "Công thức tính khoảng cách từ A đến (SBC) khi SA vuông góc đáy là gì?" | • `kind` = `concept`<br/>• `source` trỏ `TL-01` v2 mục "Hệ thức 1/AH²"<br/>• thân phản hồi có tiêu đề tài liệu, phiên bản, mục; không có nội dung mục nào khác<br/>• dòng `AIInteraction` lưu câu hỏi, câu trả lời, nguồn | US-S5E3-03-SC1 |
| TC-S5E3-13 | Bấm nguồn mở đúng mục tài liệu | Medium | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Học sinh Bình An (iPad) | • câu trả lời ở SC1 trên iPad | • Hàng dữ liệu theo Pre-condition | 1. bấm "Theo: Ghi chú Khoảng cách 11A1 · v2" | • mở khung đọc mục "Hệ thức 1/AH²" của v2<br/>• không thấy các mục khác của tài liệu ngoài mục được dẫn | US-S5E3-03-SC2 |
| TC-S5E3-14 | Lớp chưa gắn tài liệu nào thì không gọi AI | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Quốc Bảo (11A2) | • lớp 11A2 không có gắn kết nào và không có bài giảng mẫu khớp | • Hàng dữ liệu theo Pre-condition | 1. Quốc Bảo hỏi "Mặt phẳng trung trực là gì?" | • trả MSG-S5-20<br/>• `AiCallLog` không có dòng mới<br/>• `AIPolicyDecision` ghi `no_source` | US-S5E3-03-FC1 |
| TC-S5E3-15 | AI dẫn nguồn không có trong danh sách thì câu trả lời bị bỏ | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Học sinh Bình An (11A1) | • giả lập trả `source` = `TL-09` (của thầy Khôi, không được gửi) | • Hàng dữ liệu theo Pre-condition | 1. Bình An hỏi như SC1 | • trả MSG-S5-20<br/>• không có chữ nào của câu trả lời giả lập trong phản hồi<br/>• `AIPolicyDecision` ghi `invalid_source` | US-S5E3-03-FC2 |
| TC-S5E3-16 | Học sinh lớp khác không dùng được tài liệu của 11A1 | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Mi Na (10B) | • `TL-01` gắn 11A1; Mi Na ở 10B | • Hàng dữ liệu theo Pre-condition | 1. Mi Na hỏi đúng câu của SC1 | • danh sách mục gửi AI không có mục nào của `TL-01` (soát nhật ký yêu cầu ở giả lập)<br/>• câu trả lời không dẫn `TL-01` | US-S5E3-03-FC3 |
| TC-S5E3-17 | Nội dung gửi AI không chứa danh tính | High | Abnormal (Fail case/Edge case) | dữ liệu trả về | tích hợp API | E2E-API | Học sinh Bình An (11A1) | • như SC1 | • Hàng dữ liệu theo Pre-condition | 1. Bình An hỏi như SC1 | • yêu cầu ở nhà cung cấp giả lập không chứa "Bình An", `HS-4P9T-37WD`, `HK-4P7Q` | US-S5E3-03-FC4 |

### US-S5E3-04 — Mức trợ giúp theo bài và lịch công bố đáp số, kiểm ở máy chủ trước khi gọi AI

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S5E3-18 | Bài có mức "Tắt" thì Hỏi đáp báo đang tắt, không gọi AI | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan và học sinh 11A1 | • bài BC-01 mức Tắt | • Hàng dữ liệu theo Pre-condition | 1. Bình An gửi câu hỏi trên BC-01 | • trả MSG-S5-21<br/>• không có dòng `AiCallLog` mới<br/>• `AIPolicyDecision` = `blocked`, bước `help_level` | US-S5E3-04-SC1 |
| TC-S5E3-19 | Mức "Chỉ gợi ý" không trả lời khái niệm | Medium | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan và học sinh 11A1 | • bài luyện mức Chỉ gợi ý; giả lập trả `kind` = `concept` | • Hàng dữ liệu theo Pre-condition | 1. Bình An hỏi "Định nghĩa khoảng cách từ điểm đến mặt phẳng?" | • câu trả lời `concept` bị thay bằng câu dẫn tới gợi ý hiện có<br/>• `AIPolicyDecision` ghi `kind_not_allowed` | US-S5E3-04-SC2 |
| TC-S5E3-20 | Giáo viên đặt mức trợ giúp khi tạo bài | Medium | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan | • cô Lan tạo bài có chấm BC-01 (C5-01) | • Hàng dữ liệu theo Pre-condition | 1. mở mục "Trợ giúp của AI"<br/>2. chọn "Chỉ gợi ý" rồi Lưu | • mặc định hiện "Tắt" cho bài có chấm<br/>• sau khi Lưu, bài hiện "Trợ giúp: Chỉ gợi ý" | US-S5E3-04-SC3 |
| TC-S5E3-21 | Lớp tắt AI thì chặn trước mọi bước khác | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan và học sinh 11A1 | • lớp 11A1 tắt trợ lý AI (US-S3E5-04), bài mức Gợi ý và khái niệm | • Hàng dữ liệu theo Pre-condition | 1. Bình An gửi câu hỏi | • trả thông điệp lớp tắt AI của S3-E5<br/>• không gọi AI<br/>• `AIPolicyDecision` bước `class_switch` | US-S5E3-04-FC1 |
| TC-S5E3-22 | Em Đã rời không hỏi được trên bài của lớp | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Văn Tú (Đã rời) | • Văn Tú Đã rời 11A1 | • Hàng dữ liệu theo Pre-condition | 1. Văn Tú gửi câu hỏi trên BC-01 | • NOT_FOUND<br/>• không gọi AI | US-S5E3-04-FC2 |

### US-S5E3-05 — Kiểm soát đáp án đầy đủ: bộ kiểm đầu ra KS-1…KS-5 cho mọi câu trả lời, không lộ ghi chú riêng và bài của bạn, nhật ký quyết định

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S5E3-23 | Câu trả lời chứa đáp số bài đang khoá bị thay bằng gợi ý an toàn | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Học sinh Bình An (11A1) | • bài BC-01 mức Gợi ý và khái niệm, chưa công bố; giả lập F-S5-094b (chứa "a√6/3") | • Hàng dữ liệu theo Pre-condition | 1. Bình An hỏi "Khoảng cách từ A đến (SBC) bằng bao nhiêu?" | • phản hồi `kind` = `refused_answer`, không chứa "a√6/3", "√6/3", "0,816"<br/>• `AIPolicyDecision` = `blocked`, luật KS-1<br/>• không tính thêm lượt gợi ý (PO-44) | US-S5E3-05-SC1 |
| TC-S5E3-24 | Học sinh dán đáp số nhờ xác nhận bị chặn trước khi gọi AI | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Học sinh Bình An (11A1) | • như SC1 | • Hàng dữ liệu theo Pre-condition | 1. Bình An hỏi "Có phải đáp số là a√6/3 không?" | • phản hồi từ chối, không có từ xác nhận hay phủ nhận<br/>• không có dòng `AiCallLog` mới<br/>• `AIPolicyDecision` luật KS-4 | US-S5E3-05-SC2 |
| TC-S5E3-25 | Sau giờ công bố thì không chặn đáp số của chính bài | Medium | Normal (Success case) | chức năng | tích hợp API | E2E-API | Học sinh Bình An (11A1) | • BC-01 đã công bố đáp số (cô Lan bấm Trả bài) | • Hàng dữ liệu theo Pre-condition | 1. Bình An hỏi như SC1 với giả lập F-S5-094b | • câu trả lời giữ "a√6/3"<br/>• `AIPolicyDecision` = `allowed` | US-S5E3-05-SC3 |
| TC-S5E3-26 | Mục tài liệu chứa đáp số của bài đang khoá không được gửi AI | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Học sinh Bình An (11A1) | • `TL-01` v3 có mục "Đáp án bài 15 phút: a√6/3", gắn BC-01; BC-01 còn khoá | • Hàng dữ liệu theo Pre-condition | 1. Bình An hỏi câu khái niệm liên quan | • yêu cầu ở giả lập không chứa mục đó<br/>• giáo viên thấy cảnh báo "Tài liệu TL-01 v3 có đáp số của bài đang khoá" trong lịch sử hỏi đáp | US-S5E3-05-FC1 |
| TC-S5E3-27 | Ghi chú riêng, rubric và bài của bạn không bao giờ tới AI hay học sinh | High | Abnormal (Fail case/Edge case) | dữ liệu trả về | tích hợp API | E2E-API | Học sinh Bình An (11A1) | • BC-01 có `ghi_chu_rieng` "Em Mai hay nhầm SB"; rubric RB-01; bài nộp BN-02 của Chi Mai | • Hàng dữ liệu theo Pre-condition | 1. Bình An hỏi "Bạn Mai làm bài này thế nào?"<br/>2. Bình An hỏi "Rubric chấm bài này ra sao?" | • yêu cầu ở giả lập không chứa ghi chú riêng, rubric hay nội dung BN-02<br/>• phản hồi không chứa các nội dung đó | US-S5E3-05-FC2 |
| TC-S5E3-28 | Bộ kiểm lỗi thì đóng, không trả nội dung dở | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Học sinh Bình An (11A1) | • bộ kiểm giả lập ném lỗi | • Hàng dữ liệu theo Pre-condition | 1. Bình An hỏi như SC1 | • phản hồi MSG-S5-24 và nút Thử lại<br/>• không có chữ nào của câu trả lời AI<br/>• `AIPolicyDecision` = `error_closed` | US-S5E3-05-FC3 |

### US-S5E3-06 — Giáo viên xem lịch sử hỏi đáp theo bài, báo "chặn sai" hoặc "lẽ ra phải chặn"

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S5E3-29 | Lọc các lượt bị chặn của bài BC-01 | High | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (F-S5-001) | • BC-01 có 6 lượt: 4 `allowed`, 1 `blocked` KS-1, 1 `blocked` KS-4 | • Hàng dữ liệu theo Pre-condition | 1. mở "Hỏi đáp" của bài BC-01<br/>2. chọn lọc "Bị chặn" | • thấy đúng 2 dòng, mỗi dòng có câu hỏi, câu từ chối đã hiện, luật KS-1 hoặc KS-4<br/>• không có cột xếp hạng hay đếm theo học sinh | US-S5E3-06-SC1 |
| TC-S5E3-30 | Báo "chặn sai" một lượt | Medium | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • lượt bị chặn KS-4 | • Hàng dữ liệu theo Pre-condition | 1. gửi `POST /api/qa/{id}/feedback` `{"type":"false_block","note":"Em hỏi so sánh với 1, không phải đáp số"}` | • trả 200, MSG-S5-25<br/>• tổng hợp người vận hành KS-4 `false_block` tăng 1<br/>• câu trả lời đã gửi không đổi | US-S5E3-06-SC2 |
| TC-S5E3-31 | Thầy Khôi không xem được lịch sử của BC-01 | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Thầy Khôi | • BC-01 của 11A1 | • Hàng dữ liệu theo Pre-condition | 1. thầy Khôi gọi `GET /api/assignments/BC-01/qa` | • NOT_FOUND | US-S5E3-06-FC1 |
| TC-S5E3-32 | Học sinh chỉ thấy lượt hỏi của chính mình | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Bình An | • BC-01 có lượt của Bình An và Chi Mai | • Hàng dữ liệu theo Pre-condition | 1. Bình An gọi `GET /api/qa/mine?assignment=BC-01`<br/>2. Bình An gọi `GET /api/assignments/BC-01/qa` | • lần 1 chỉ có lượt của Bình An<br/>• lần 2 FORBIDDEN | US-S5E3-06-FC2 |

## Truy vết story → tiêu chí → case

Tổng: 32 case · E2E-API 29 · E2E-UI 3.

| Story | Tiêu chí | Loại | Tiêu đề | Case | Tự động hóa |
|---|---|---|---|---|---|
| US-S5E3-01 | US-S5E3-01-SC1 | Success | Tải tệp docx tạo tài liệu Nháp tách mục | TC-S5E3-01 | E2E-API |
| US-S5E3-01 | US-S5E3-01-SC2 | Success | Duyệt đưa phiên bản mới lên Đang dùng, bản cũ sang Lưu trữ | TC-S5E3-02 | E2E-API |
| US-S5E3-01 | US-S5E3-01-SC3 | Success | Giáo án đã duyệt đưa vào kho | TC-S5E3-03 | E2E-API |
| US-S5E3-01 | US-S5E3-01-FC1 | Fail / biên | Sửa phiên bản đã dùng tạo phiên bản mới, không ghi đè | TC-S5E3-04 | E2E-API |
| US-S5E3-01 | US-S5E3-01-FC2 | Fail / biên | Tệp 11 MB hoặc định dạng xlsx bị từ chối | TC-S5E3-05 | E2E-API |
| US-S5E3-01 | US-S5E3-01-FC3 | Fail / biên | Tải lại tệp trùng không tạo bản mới | TC-S5E3-06 | E2E-API |
| US-S5E3-01 | US-S5E3-01-FC4 | Fail / biên | Giáo viên khác và học sinh không đọc được kho của cô Lan | TC-S5E3-07 | E2E-API |
| US-S5E3-02 | US-S5E3-02-SC1 | Success | Gắn TL-01 cho lớp 11A1 | TC-S5E3-08 | E2E-API |
| US-S5E3-02 | US-S5E3-02-SC2 | Success | Gắn kết có hạn tự hết hiệu lực | TC-S5E3-09 | E2E-API |
| US-S5E3-02 | US-S5E3-02-FC1 | Fail / biên | Không gắn được tài liệu chưa Đang dùng | TC-S5E3-10 | E2E-API |
| US-S5E3-02 | US-S5E3-02-FC2 | Fail / biên | Không gắn được cho lớp của người khác | TC-S5E3-11 | E2E-API |
| US-S5E3-03 | US-S5E3-03-SC1 | Success | Câu hỏi khái niệm nhận trả lời kèm nguồn TL-01 v2 | TC-S5E3-12 | E2E-API |
| US-S5E3-03 | US-S5E3-03-SC2 | Success | Bấm nguồn mở đúng mục tài liệu | TC-S5E3-13 | E2E-UI |
| US-S5E3-03 | US-S5E3-03-FC1 | Fail / biên | Lớp chưa gắn tài liệu nào thì không gọi AI | TC-S5E3-14 | E2E-API |
| US-S5E3-03 | US-S5E3-03-FC2 | Fail / biên | AI dẫn nguồn không có trong danh sách thì câu trả lời bị bỏ | TC-S5E3-15 | E2E-API |
| US-S5E3-03 | US-S5E3-03-FC3 | Fail / biên | Học sinh lớp khác không dùng được tài liệu của 11A1 | TC-S5E3-16 | E2E-API |
| US-S5E3-03 | US-S5E3-03-FC4 | Fail / biên | Nội dung gửi AI không chứa danh tính | TC-S5E3-17 | E2E-API |
| US-S5E3-04 | US-S5E3-04-SC1 | Success | Bài có mức "Tắt" thì Hỏi đáp báo đang tắt, không gọi AI | TC-S5E3-18 | E2E-API |
| US-S5E3-04 | US-S5E3-04-SC2 | Success | Mức "Chỉ gợi ý" không trả lời khái niệm | TC-S5E3-19 | E2E-API |
| US-S5E3-04 | US-S5E3-04-SC3 | Success | Giáo viên đặt mức trợ giúp khi tạo bài | TC-S5E3-20 | E2E-UI |
| US-S5E3-04 | US-S5E3-04-FC1 | Fail / biên | Lớp tắt AI thì chặn trước mọi bước khác | TC-S5E3-21 | E2E-API |
| US-S5E3-04 | US-S5E3-04-FC2 | Fail / biên | Em Đã rời không hỏi được trên bài của lớp | TC-S5E3-22 | E2E-API |
| US-S5E3-05 | US-S5E3-05-SC1 | Success | Câu trả lời chứa đáp số bài đang khoá bị thay bằng gợi ý an toàn | TC-S5E3-23 | E2E-API |
| US-S5E3-05 | US-S5E3-05-SC2 | Success | Học sinh dán đáp số nhờ xác nhận bị chặn trước khi gọi AI | TC-S5E3-24 | E2E-API |
| US-S5E3-05 | US-S5E3-05-SC3 | Success | Sau giờ công bố thì không chặn đáp số của chính bài | TC-S5E3-25 | E2E-API |
| US-S5E3-05 | US-S5E3-05-FC1 | Fail / biên | Mục tài liệu chứa đáp số của bài đang khoá không được gửi AI | TC-S5E3-26 | E2E-API |
| US-S5E3-05 | US-S5E3-05-FC2 | Fail / biên | Ghi chú riêng, rubric và bài của bạn không bao giờ tới AI hay học sinh | TC-S5E3-27 | E2E-API |
| US-S5E3-05 | US-S5E3-05-FC3 | Fail / biên | Bộ kiểm lỗi thì đóng, không trả nội dung dở | TC-S5E3-28 | E2E-API |
| US-S5E3-06 | US-S5E3-06-SC1 | Success | Lọc các lượt bị chặn của bài BC-01 | TC-S5E3-29 | E2E-UI |
| US-S5E3-06 | US-S5E3-06-SC2 | Success | Báo "chặn sai" một lượt | TC-S5E3-30 | E2E-API |
| US-S5E3-06 | US-S5E3-06-FC1 | Fail / biên | Thầy Khôi không xem được lịch sử của BC-01 | TC-S5E3-31 | E2E-API |
| US-S5E3-06 | US-S5E3-06-FC2 | Fail / biên | Học sinh chỉ thấy lượt hỏi của chính mình | TC-S5E3-32 | E2E-API |

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-28 | Tạo test case chặng 5 từ AC (goal run) | agent spec-s5 (QC) |
