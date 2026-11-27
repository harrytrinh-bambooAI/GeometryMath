Phiên bản: v0.1 · 2026-11-28
# S5-E4 — test case (Rubric, giao bài có chấm, nộp bài viết tay (ảnh, OCR), chấm tay và trả bài (đợt bật 1))

> Chủ sở hữu: QC. Lập từ `stories_ac.md` cùng thư mục: mỗi tiêu chí có ít nhất một case, kỳ vọng bám Given / When / Then. **Trạng thái: ready for R3 (goal run)**. AI là giả lập (`MockAiProvider`, D-GEO-047): không gọi AI thật, kỳ vọng chỉ soát hành vi máy chủ và dữ liệu trả về, không soát chất lượng câu chữ AI.

- Mỗi dòng là một case. Chuẩn bị theo **Pre-condition**, dùng **Test data**, làm lần lượt **Test step**, so với **Expected output**; đạt khi mọi gạch đầu dòng đều đúng.
- **Tự động hóa** chỉ có hai giá trị: `E2E-API` (gọi HTTP thẳng, soát `code` và thân phản hồi; mặc định) và `E2E-UI` (Playwright, chỉ dùng khi điều được kiểm là tương tác trên màn: form, chụp ảnh, khoanh vùng, nhận/bỏ từng dòng).
- **Status** của vòng thực thi: Pass · Fail · Blocked (+ cần gì) · UT (+ lý do) · Not run. Kỳ vọng mang mã `QS5-nn` là giả định của PO; màn chỉ khác đúng điểm đó thì đánh Blocked kèm mã, không đánh Fail.
- Dữ liệu mẫu chung `F-S5-nnn` ở mục 0 của `stories_ac.md` cùng thư mục.

### US-S5E4-01 — Rubric có phiên bản, mẫu sẵn theo dạng bài, bất biến khi đã dùng

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S5E4-01 | Tạo rubric từ mẫu Tính toán | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • cô Lan đã đăng nhập | • Hàng dữ liệu theo Pre-condition | 1. gửi `POST /api/rubrics` `{"fromTemplate":"tinh-toan","name":"Tính toán – khoảng cách"}` | • rubric `RB-01` phiên bản 1 có 5 tiêu chí 2 / 4 / 2 / 1 / 1, tổng 10 (F-S5-051)<br/>• tiêu chí "Bước" có cờ phụ thuộc hướng<br/>• hướng dẫn chấm có 5 luật chặn KPI §2 | US-S5E4-01-SC1 |
| TC-S5E4-02 | Sửa rubric đã giao tạo phiên bản mới, bài đã giao giữ bản cũ | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • RB-01 v1 gắn bài BC-01 đã giao | • Hàng dữ liệu theo Pre-condition | 1. gửi `PUT /api/rubrics/RB-01` đổi "Trình bày" thành 0,5 và "Hình" thành 1,5 | • trả phiên bản 2 kèm MSG-S5-31<br/>• BC-01 vẫn trỏ RB-01 v1<br/>• nội dung v1 không đổi | US-S5E4-01-SC2 |
| TC-S5E4-03 | Màn soạn rubric hiện tổng điểm sống và chặn lưu khi lệch | Medium | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (F-S5-001) | • cô Lan mở C5-02 với RB-01 | • Hàng dữ liệu theo Pre-condition | 1. đổi điểm "Kết quả" từ 2 thành 3 | • dòng tổng hiện "11 / 10" màu cảnh báo<br/>• nút Lưu mờ, MSG-S5-30 | US-S5E4-01-SC3 |
| TC-S5E4-04 | Tổng điểm khác thang bị từ chối | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • cô Lan đã đăng nhập | • Hàng dữ liệu theo Pre-condition | 1. gửi rubric 5 tiêu chí tổng 9,75 | • VALIDATION_FAILED, MSG-S5-30 | US-S5E4-01-FC1 |
| TC-S5E4-05 | Điểm không phải bội 0,25 hoặc 11 tiêu chí bị từ chối | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • cô Lan đã đăng nhập | • Hàng dữ liệu theo Pre-condition | 1. gửi tiêu chí điểm 0,3<br/>2. gửi rubric 11 tiêu chí | • cả hai VALIDATION_FAILED với `details` đúng trường | US-S5E4-01-FC2 |
| TC-S5E4-06 | Cập nhật thẳng phiên bản đã dùng bị CSDL từ chối | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • RB-01 v1 đã gắn bài đã giao | • Hàng dữ liệu theo Pre-condition | 1. chạy câu lệnh UPDATE `rubric_criterion` của v1 bằng vai ứng dụng (ca kỹ thuật qua API thử nghiệm của profile e2e) | • lỗi từ CSDL; dữ liệu v1 không đổi | US-S5E4-01-FC3; ENG: dùng móc thử nghiệm của profile e2e, không bật ở prod (PO-51) |

### US-S5E4-02 — Giao bài có chấm: đề hoặc bộ đề, rubric, chế độ chấm, hạn nộp, nộp muộn, lịch công bố điểm và lời giải

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S5E4-07 | Giao bài BC-01 chế độ Chấm tay | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • RB-01 v1, đề F-S5-050, lớp 11A1 | • Hàng dữ liệu theo Pre-condition | 1. gửi `POST /api/graded-assignments` theo F-S5-052 | • bài BC-01 Đã giao, 8 em Đang học thấy bài trong "Bài cần nộp"<br/>• chế độ Chấm tay<br/>• thân phản hồi phía học sinh không có rubric chi tiết, hướng dẫn chấm hay lời giải | US-S5E4-02-SC1 |
| TC-S5E4-08 | Kéo dài hạn nộp sau khi đã có bài nộp | Medium | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • BC-01 đã có BN-01 | • Hàng dữ liệu theo Pre-condition | 1. đổi hạn sang 12/12/2026 20:00 | • trả 200<br/>• hạn mới hiện cho học sinh | US-S5E4-02-SC2 |
| TC-S5E4-09 | Chọn "AI đề xuất" khi lớp chưa đạt cổng bị từ chối | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • lớp 11A1 chưa có kết quả chạy song song | • Hàng dữ liệu theo Pre-condition | 1. gửi giao bài với chế độ `ai_proposal` | • CONFLICT, `details` nêu "lớp chưa đạt ngưỡng chạy song song"<br/>• không có bài nào được tạo | US-S5E4-02-FC1 |
| TC-S5E4-10 | Rút ngắn hạn hoặc đổi rubric sau khi có bài nộp bị từ chối | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • BC-01 đã có BN-01 | • Hàng dữ liệu theo Pre-condition | 1. đổi hạn sớm hơn 1 ngày<br/>2. đổi sang RB-01 v2 | • cả hai CONFLICT<br/>• bài giữ nguyên | US-S5E4-02-FC2 |
| TC-S5E4-11 | Không giao bài có chấm cho lớp của người khác | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • lớp 10B của thầy Khôi | • Hàng dữ liệu theo Pre-condition | 1. cô Lan giao BC-01 cho 10B | • NOT_FOUND | US-S5E4-02-FC3 |

### US-S5E4-03 — Học sinh chụp và nộp nhiều trang: kiểm độ rõ, xếp trang, giữ ảnh gốc, gửi lại không trùng, nộp muộn

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S5E4-12 | Chụp 2 trang, xếp lại, nộp | High | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Học sinh Bình An (iPhone) | • Bình An mở BC-01 trên iPhone, đã đọc thông báo riêng tư | • Hàng dữ liệu theo Pre-condition | 1. bấm "Nộp bài" rồi chụp 2 trang<br/>2. kéo trang 2 lên trước rồi kéo lại<br/>3. bấm "Nộp" | • màn hiện MSG-S5-33 với giờ máy chủ<br/>• bài nộp BN-01 có 2 trang đúng thứ tự cuối cùng<br/>• băm ảnh gốc trên máy chủ trùng băm tệp đã tải lên | US-S5E4-03-SC1 |
| TC-S5E4-13 | Gửi lại cùng khóa lũy đẳng không tạo bài trùng | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Học sinh Bình An (iPhone) | • BN-01 vừa nộp với khóa `k-01` | • Hàng dữ liệu theo Pre-condition | 1. gửi lại `POST /api/submissions` cùng khóa `k-01` | • trả 200 với id BN-01<br/>• BC-01 có đúng 1 bài nộp của Bình An | US-S5E4-03-SC2 |
| TC-S5E4-14 | Nộp muộn có nhãn khi bài cho nộp muộn | Medium | Normal (Success case) | chức năng | tích hợp API | E2E-API | Hà My | • BC-01 cho nộp muộn; giờ máy chủ 11/12/2026 20:07 | • Hàng dữ liệu theo Pre-condition | 1. Hà My nộp BN-05 | • BN-05 trạng thái Đã nộp, nhãn "Nộp muộn 7 phút" | US-S5E4-03-SC3 |
| TC-S5E4-15 | Nộp lại trước hạn tạo phiên bản mới, bản cũ giữ | Medium | Normal (Success case) | chức năng | tích hợp API | E2E-API | Học sinh Bình An (iPhone) | • BN-01 phiên bản 1 | • Hàng dữ liệu theo Pre-condition | 1. Bình An nộp lại 3 trang trước hạn | • BN-01 phiên bản 2 là bản chấm<br/>• phiên bản 1 vẫn đọc được bởi giáo viên<br/>• ảnh của phiên bản 1 không bị xoá | US-S5E4-03-SC4 |
| TC-S5E4-16 | Ảnh mờ bị yêu cầu chụp lại ngay | High | Abnormal (Fail case/Edge case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Đức Huy (iPhone) | • Đức Huy chụp trang 2 điểm độ rõ 0,31 (F-S5-055) | • Hàng dữ liệu theo Pre-condition | 1. chụp trang 2<br/>2. bấm "Dùng ảnh này" | • hiện MSG-S5-32 cho trang 2<br/>• nút Nộp mờ cho tới khi trang 2 được chụp lại đạt | US-S5E4-03-FC1 |
| TC-S5E4-17 | Mất mạng khi nộp: giữ nháp, tự gửi lại | High | Abnormal (Fail case/Edge case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Học sinh Bình An (iPhone) | • Bình An đã chụp 2 trang; mạng tắt (giả lập offline của trình duyệt) | • Hàng dữ liệu theo Pre-condition | 1. bấm Nộp<br/>2. bật lại mạng | • sau bước 1 hiện MSG-S5-35, bài lưu trên máy<br/>• sau bước 2 bài tự gửi, đúng 1 bài nộp trên máy chủ | US-S5E4-03-FC2 |
| TC-S5E4-18 | Quá hạn khi bài không cho nộp muộn | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Gia Bảo | • BC-01 không cho nộp muộn; giờ máy chủ sau hạn 1 giây | • Hàng dữ liệu theo Pre-condition | 1. Gia Bảo nộp | • CONFLICT, MSG-S5-34<br/>• không có bài nộp | US-S5E4-03-FC3 |
| TC-S5E4-19 | Trang 9 MB hoặc 11 trang bị từ chối | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Học sinh Bình An (iPhone) | • Bình An đã đăng nhập | • Hàng dữ liệu theo Pre-condition | 1. gửi trang 9 MB<br/>2. gửi bài 11 trang | • cả hai VALIDATION_FAILED | US-S5E4-03-FC4 |
| TC-S5E4-20 | Học sinh không đọc được bài nộp của bạn | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Học sinh Bình An (iPhone) | • BN-02 của Chi Mai | • Hàng dữ liệu theo Pre-condition | 1. Bình An gọi `GET /api/submissions/BN-02` | • NOT_FOUND<br/>• không có URL ảnh nào trong thân phản hồi | US-S5E4-03-FC5 |

### US-S5E4-04 — Bản đọc chữ (OCR) của bài nộp: phái sinh, tô chỗ chưa chắc, giáo viên sửa có lịch sử

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S5E4-21 | Bản đọc chữ 2 trang có đoạn tô chưa chắc | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Hệ thống và giáo viên Lan | • BN-01 vừa nộp; giả lập F-S5-095 | • Hàng dữ liệu theo Pre-condition | 1. chờ xử lý nền xong<br/>2. gọi `GET /api/submissions/BN-01/ocr` | • bản đọc chữ phiên bản 1 có 2 trang<br/>• 2 đoạn `unsure = true` kèm vùng ảnh<br/>• `AiCallLog` task `grade_ocr`, nội dung gửi không có tên hay mã người dùng | US-S5E4-04-SC1 |
| TC-S5E4-22 | Giáo viên sửa bản đọc chữ tạo phiên bản mới | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Hệ thống và giáo viên Lan | • bản đọc chữ BN-01 phiên bản 1 | • Hàng dữ liệu theo Pre-condition | 1. cô Lan gửi `PUT /api/submissions/BN-01/ocr` sửa "a√6/2" thành "a√6/3" | • phiên bản 2, người sửa cô Lan<br/>• phiên bản 1 vẫn đọc được<br/>• băm ảnh gốc không đổi | US-S5E4-04-SC2 |
| TC-S5E4-23 | AI lỗi thì bài vẫn chấm tay được | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Hệ thống và giáo viên Lan | • giả lập `grade_ocr` lỗi cả hai lần | • Hàng dữ liệu theo Pre-condition | 1. chờ xử lý nền<br/>2. cô Lan mở BN-01 trong hàng chờ | • bài trạng thái Chờ chấm<br/>• bản đọc chữ "Chưa đọc được" kèm nút Đọc lại<br/>• ảnh gốc vẫn xem được | US-S5E4-04-FC1 |
| TC-S5E4-24 | Học sinh không xem hay sửa được bản đọc chữ | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Bình An | • BN-01 có bản đọc chữ | • Hàng dữ liệu theo Pre-condition | 1. Bình An gọi `GET` và `PUT /api/submissions/BN-01/ocr` | • cả hai FORBIDDEN | US-S5E4-04-FC2 |

### US-S5E4-05 — Hàng chờ chấm: danh sách theo bài, trạng thái, cờ xếp trước, lọc lớp và bài

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S5E4-25 | Hàng chờ BC-01 sau hạn | High | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (F-S5-001) | • BC-01: BN-01, BN-02 Chờ chấm, BN-03 Chờ chấm có cờ "Chưa đọc được", BN-05 nộp muộn; Gia Bảo chưa nộp | • Hàng dữ liệu theo Pre-condition | 1. mở C5-04 lọc lớp 11A1, bài BC-01 | • BN-03 và BN-05 đứng đầu với nhãn cờ<br/>• tiếp theo BN-01, BN-02 theo giờ nộp<br/>• dòng tóm tắt "4 chờ chấm · 1 chưa nộp · 0 đã duyệt"<br/>• không có cột xếp hạng | US-S5E4-05-SC1 |
| TC-S5E4-26 | Số bài chờ cho màn điện thoại | Medium | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • như SC1 | • Hàng dữ liệu theo Pre-condition | 1. gọi `GET /api/grading/queue/summary` | • trả `{"waiting":4,"processing":0}` theo từng bài<br/>• không có tên học sinh | US-S5E4-05-SC2 |
| TC-S5E4-27 | Hàng chờ không có bài của lớp khác | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • 10B của thầy Khôi có 3 bài Chờ chấm | • Hàng dữ liệu theo Pre-condition | 1. cô Lan gọi `GET /api/grading/queue?classId=<10B>` | • NOT_FOUND | US-S5E4-05-FC1 |
| TC-S5E4-28 | Trống khi chưa có bài nộp | Low | Abnormal (Fail case/Edge case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (F-S5-001) | • bài mới giao, chưa em nào nộp | • Hàng dữ liệu theo Pre-condition | 1. mở hàng chờ của bài đó | • trạng thái trống "Chưa có bài nộp" kèm hạn nộp | US-S5E4-05-FC2 |

### US-S5E4-06 — Chấm tay theo rubric trên web, Duyệt thành điểm chính thức, nhật ký trước và sau

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S5E4-29 | Chấm và duyệt BN-02 | High | Normal (Success case) | chức năng | E2E trình duyệt (Playwright) + tích hợp API | E2E-UI | Giáo viên Lan (F-S5-001) | • BN-02 Chờ chấm; RB-01 v1 | • Hàng dữ liệu theo Pre-condition | 1. mở BN-02<br/>2. nhập Hướng 2, Bước 3, Kết quả 0, Hình 1, Trình bày 1 và nhận xét "Xem lại cách tính SB"<br/>3. bấm Duyệt | • ảnh 3 trang ở trái, rubric 5 tiêu chí ở phải<br/>• tổng hiện 7<br/>• sau Duyệt hiện MSG-S5-38, bài sang Đã duyệt, mở bài kế tiếp trong hàng chờ | US-S5E4-06-SC1 |
| TC-S5E4-30 | Sửa điểm đã duyệt ghi trước và sau | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • BN-02 đã duyệt tổng 7 | • Hàng dữ liệu theo Pre-condition | 1. gửi sửa "Bước" từ 3 thành 3,5 | • `ApprovedGrade` mới tổng 7,5<br/>• `AuditEvent` có trước 3 · sau 3,5, người thực hiện cô Lan | US-S5E4-06-SC2 |
| TC-S5E4-31 | Điểm vượt tối đa hoặc lẻ 0,1 bị từ chối | High | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • BN-01 Chờ chấm | • Hàng dữ liệu theo Pre-condition | 1. gửi "Bước" = 4,5<br/>2. gửi "Hình" = 0,6 | • cả hai VALIDATION_FAILED, MSG-S5-37 | US-S5E4-06-FC1 |
| TC-S5E4-32 | Giáo viên không chủ lớp không duyệt được | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Thầy Khôi | • BN-01 của 11A1 | • Hàng dữ liệu theo Pre-condition | 1. thầy Khôi gửi `POST /api/submissions/BN-01/grade` | • NOT_FOUND<br/>• không có `ApprovedGrade` mới | US-S5E4-06-FC2 |
| TC-S5E4-33 | Hai lần lưu đồng thời thì lần sau bị CONFLICT | Medium | Abnormal (Fail case/Edge case) | chức năng | tích hợp API | E2E-API | Giáo viên Lan (F-S5-001) | • BN-01 Chờ chấm, phiên bản bản ghi 1 | • Hàng dữ liệu theo Pre-condition | 1. gửi hai yêu cầu duyệt cùng `version` = 1 | • một yêu cầu thành công, yêu cầu còn lại CONFLICT<br/>• đúng 1 `ApprovedGrade` | US-S5E4-06-FC3 |

### US-S5E4-07 — Trả bài: học sinh thấy điểm, nhận xét, lời giải theo lịch công bố; không thấy gì của AI

| No | Tiêu đề | Priority | Type | Loại | Tầng tự động hóa | Tự động hóa | User Type/Account Permission | Pre-condition | Test data/Input data | Test step | Expected output | Remark |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TC-S5E4-34 | Trả bài cho cả lớp, học sinh xem được điểm và lời giải | High | Normal (Success case) | chức năng | tích hợp API | E2E-API | Học sinh Bình An (iPad) | • BC-01: BN-01 Đã duyệt 10, BN-02 Đã duyệt 7, BN-03 Chờ chấm | • Hàng dữ liệu theo Pre-condition | 1. cô Lan gửi `POST /api/graded-assignments/BC-01/return`<br/>2. Bình An gọi `GET /api/submissions/BN-01/result` | • BN-01, BN-02 sang Đã trả; BN-03 vẫn Chờ chấm<br/>• Bình An thấy 5 tiêu chí, tổng 10, nhận xét và lời giải<br/>• Bình An có thông báo MSG-S5-39 | US-S5E4-07-SC1 |
| TC-S5E4-35 | Lịch công bố giờ cố định mở đúng ranh giới | Medium | Normal (Success case) | chức năng | tích hợp API | E2E-API | Học sinh Bình An (iPad) | • BC-01 `ReleasePolicy` = 14/12/2026 07:00; BN-01 Đã duyệt | • Hàng dữ liệu theo Pre-condition | 1. Bình An gọi kết quả tại R − 1 giây<br/>2. gọi lại tại R | • lần 1 chỉ có trạng thái "Đang chờ thầy cô trả bài", không có điểm<br/>• lần 2 có điểm và lời giải | US-S5E4-07-SC2 |
| TC-S5E4-36 | Trước khi trả, phản hồi không chứa điểm hay dữ liệu AI | High | Abnormal (Fail case/Edge case) | dữ liệu trả về | tích hợp API | E2E-API | Học sinh Bình An (iPad) | • BN-01 Đã duyệt nhưng chưa trả | • Hàng dữ liệu theo Pre-condition | 1. Bình An gọi `GET /api/submissions/BN-01` và `/result` | • thân phản hồi không có trường `score`, `criteria`, `proposal`, `confidence`, `ocr`<br/>• tiêu đề `Cache-Control: no-store` | US-S5E4-07-FC1 |
| TC-S5E4-37 | Học sinh không xem được điểm của bạn sau khi trả | High | Abnormal (Fail case/Edge case) | phân quyền | tích hợp API | E2E-API | Học sinh Bình An (iPad) | • BN-02 Đã trả | • Hàng dữ liệu theo Pre-condition | 1. Bình An gọi `GET /api/submissions/BN-02/result` | • NOT_FOUND | US-S5E4-07-FC2 |

## Truy vết story → tiêu chí → case

Tổng: 37 case · E2E-API 30 · E2E-UI 7.

| Story | Tiêu chí | Loại | Tiêu đề | Case | Tự động hóa |
|---|---|---|---|---|---|
| US-S5E4-01 | US-S5E4-01-SC1 | Success | Tạo rubric từ mẫu Tính toán | TC-S5E4-01 | E2E-API |
| US-S5E4-01 | US-S5E4-01-SC2 | Success | Sửa rubric đã giao tạo phiên bản mới, bài đã giao giữ bản cũ | TC-S5E4-02 | E2E-API |
| US-S5E4-01 | US-S5E4-01-SC3 | Success | Màn soạn rubric hiện tổng điểm sống và chặn lưu khi lệch | TC-S5E4-03 | E2E-UI |
| US-S5E4-01 | US-S5E4-01-FC1 | Fail / biên | Tổng điểm khác thang bị từ chối | TC-S5E4-04 | E2E-API |
| US-S5E4-01 | US-S5E4-01-FC2 | Fail / biên | Điểm không phải bội 0,25 hoặc 11 tiêu chí bị từ chối | TC-S5E4-05 | E2E-API |
| US-S5E4-01 | US-S5E4-01-FC3 | Fail / biên | Cập nhật thẳng phiên bản đã dùng bị CSDL từ chối | TC-S5E4-06 | E2E-API |
| US-S5E4-02 | US-S5E4-02-SC1 | Success | Giao bài BC-01 chế độ Chấm tay | TC-S5E4-07 | E2E-API |
| US-S5E4-02 | US-S5E4-02-SC2 | Success | Kéo dài hạn nộp sau khi đã có bài nộp | TC-S5E4-08 | E2E-API |
| US-S5E4-02 | US-S5E4-02-FC1 | Fail / biên | Chọn "AI đề xuất" khi lớp chưa đạt cổng bị từ chối | TC-S5E4-09 | E2E-API |
| US-S5E4-02 | US-S5E4-02-FC2 | Fail / biên | Rút ngắn hạn hoặc đổi rubric sau khi có bài nộp bị từ chối | TC-S5E4-10 | E2E-API |
| US-S5E4-02 | US-S5E4-02-FC3 | Fail / biên | Không giao bài có chấm cho lớp của người khác | TC-S5E4-11 | E2E-API |
| US-S5E4-03 | US-S5E4-03-SC1 | Success | Chụp 2 trang, xếp lại, nộp | TC-S5E4-12 | E2E-UI |
| US-S5E4-03 | US-S5E4-03-SC2 | Success | Gửi lại cùng khóa lũy đẳng không tạo bài trùng | TC-S5E4-13 | E2E-API |
| US-S5E4-03 | US-S5E4-03-SC3 | Success | Nộp muộn có nhãn khi bài cho nộp muộn | TC-S5E4-14 | E2E-API |
| US-S5E4-03 | US-S5E4-03-SC4 | Success | Nộp lại trước hạn tạo phiên bản mới, bản cũ giữ | TC-S5E4-15 | E2E-API |
| US-S5E4-03 | US-S5E4-03-FC1 | Fail / biên | Ảnh mờ bị yêu cầu chụp lại ngay | TC-S5E4-16 | E2E-UI |
| US-S5E4-03 | US-S5E4-03-FC2 | Fail / biên | Mất mạng khi nộp: giữ nháp, tự gửi lại | TC-S5E4-17 | E2E-UI |
| US-S5E4-03 | US-S5E4-03-FC3 | Fail / biên | Quá hạn khi bài không cho nộp muộn | TC-S5E4-18 | E2E-API |
| US-S5E4-03 | US-S5E4-03-FC4 | Fail / biên | Trang 9 MB hoặc 11 trang bị từ chối | TC-S5E4-19 | E2E-API |
| US-S5E4-03 | US-S5E4-03-FC5 | Fail / biên | Học sinh không đọc được bài nộp của bạn | TC-S5E4-20 | E2E-API |
| US-S5E4-04 | US-S5E4-04-SC1 | Success | Bản đọc chữ 2 trang có đoạn tô chưa chắc | TC-S5E4-21 | E2E-API |
| US-S5E4-04 | US-S5E4-04-SC2 | Success | Giáo viên sửa bản đọc chữ tạo phiên bản mới | TC-S5E4-22 | E2E-API |
| US-S5E4-04 | US-S5E4-04-FC1 | Fail / biên | AI lỗi thì bài vẫn chấm tay được | TC-S5E4-23 | E2E-API |
| US-S5E4-04 | US-S5E4-04-FC2 | Fail / biên | Học sinh không xem hay sửa được bản đọc chữ | TC-S5E4-24 | E2E-API |
| US-S5E4-05 | US-S5E4-05-SC1 | Success | Hàng chờ BC-01 sau hạn | TC-S5E4-25 | E2E-UI |
| US-S5E4-05 | US-S5E4-05-SC2 | Success | Số bài chờ cho màn điện thoại | TC-S5E4-26 | E2E-API |
| US-S5E4-05 | US-S5E4-05-FC1 | Fail / biên | Hàng chờ không có bài của lớp khác | TC-S5E4-27 | E2E-API |
| US-S5E4-05 | US-S5E4-05-FC2 | Fail / biên | Trống khi chưa có bài nộp | TC-S5E4-28 | E2E-UI |
| US-S5E4-06 | US-S5E4-06-SC1 | Success | Chấm và duyệt BN-02 | TC-S5E4-29 | E2E-UI |
| US-S5E4-06 | US-S5E4-06-SC2 | Success | Sửa điểm đã duyệt ghi trước và sau | TC-S5E4-30 | E2E-API |
| US-S5E4-06 | US-S5E4-06-FC1 | Fail / biên | Điểm vượt tối đa hoặc lẻ 0,1 bị từ chối | TC-S5E4-31 | E2E-API |
| US-S5E4-06 | US-S5E4-06-FC2 | Fail / biên | Giáo viên không chủ lớp không duyệt được | TC-S5E4-32 | E2E-API |
| US-S5E4-06 | US-S5E4-06-FC3 | Fail / biên | Hai lần lưu đồng thời thì lần sau bị CONFLICT | TC-S5E4-33 | E2E-API |
| US-S5E4-07 | US-S5E4-07-SC1 | Success | Trả bài cho cả lớp, học sinh xem được điểm và lời giải | TC-S5E4-34 | E2E-API |
| US-S5E4-07 | US-S5E4-07-SC2 | Success | Lịch công bố giờ cố định mở đúng ranh giới | TC-S5E4-35 | E2E-API |
| US-S5E4-07 | US-S5E4-07-FC1 | Fail / biên | Trước khi trả, phản hồi không chứa điểm hay dữ liệu AI | TC-S5E4-36 | E2E-API |
| US-S5E4-07 | US-S5E4-07-FC2 | Fail / biên | Học sinh không xem được điểm của bạn sau khi trả | TC-S5E4-37 | E2E-API |

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-28 | Tạo test case chặng 5 từ AC (goal run) | agent spec-s5 (QC) |
