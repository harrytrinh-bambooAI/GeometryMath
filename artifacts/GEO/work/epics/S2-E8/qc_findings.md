Phiên bản: v0.1 · 2026-11-26
# S2-E8 — Phát hiện QC

## Dev

Ghi bởi runner-T-261126-21 (L5 Dev). Không sửa `stories_ac.md`, `test_cases.md`, `test_design.md`; BA/QC xử lý.

| Mã | Chỗ | Phát hiện | Cách Dev làm (TẠM · tin cậy · nguồn) | Cần ai |
|---|---|---|---|---|
| Dev-S2E8-01 | PLAN_code-s23 A1, §5 (tệp sở hữu) | `grade` chung đặt ở `subject/internal/grade`, nhưng module khác chỉ được dùng `subject.api` (ArchUnit) và `subject/api/**` nằm ngoài phạm vi tệp của task 21. `subject` cũng không được phụ thuộc `assignment`/`tracking` (chu trình qua `content`, `learning`, `geometry`). | Cầu nối kiểu JDK thuần: bean `subjectAnswerGrader` (`AnswerGraderBridge`, `Function<Map, Map>`), bọc lại ở `assignment.api.PracticeGrader`. TẠM · vừa · README "Luật" | FIX: thêm `subject.api.AnswerGrader` rồi bỏ cầu nối |
| Dev-S2E8-02 | US-S2E8-05-FC2, TC-S2E8-094 ("giáo viên sửa đề gốc thành đáp số 60") | `solution.final_answer` bất biến ở CSDL (BR-11/BR-22: sửa = tạo phiên bản mới). | IT tạo phiên bản 2 của đề với đáp số 60° và đặt làm phiên bản hiện hành; bài đã giao vẫn chấm theo ảnh chụp 45° của phiên bản đã giao. TẠM · cao · V10 | BA xác nhận cách đọc "sửa đề gốc" |
| Dev-S2E8-03 | BR-S2E8-27 "đề có đáp số máy chấm được" | Chưa có nơi giáo viên nhập đáp số cho bài luyện (S2-E5 "Soát lời giải"). Đáp số kho MVP có đề viết kèm đơn vị hoặc nhiều vế ("24√3 cm³", "Sxq = 15π cm²; V = 12π cm³"). | "Máy chấm được" = `solution.final_answer` đọc được bằng bảng ký hiệu BR-S2E8-10 (có thể kèm °). Đề có đơn vị chữ hoặc nhiều vế hiện chip vàng, không chọn được. TẠM · vừa · DQ-14 | BA/S2-E5: trường đáp số số học riêng |
| Dev-S2E8-04 | US-S2E8-02-FC2 (gợi ý "Em hãy viết đáp số ở dạng chính xác (có căn)") | Nếu chỉ nhắc khi đáp số nộp gần đúng thì lời nhắc trở thành tín hiệu "gần đúng" (lộ hướng, trái BR-S2E8-12). | Nhắc với MỌI đáp số là số thập phân trơn bị chấm Sai khi đáp số mẫu vô tỉ, không phụ thuộc gần hay xa (lộ "đáp số có căn/π", không lộ giá trị). TẠM · vừa · BR-S2E8-12 | PO xác nhận |
| Dev-S2E8-05 | US-S2E8-02-SC8 (dạng chuẩn) | AC: "45", "45°", "45,0" không kèm dạng chuẩn, "90/2" thì có; SC4: "0,5" so với mẫu "1/2" thì có. Chưa có quy tắc chung. | Không hiện dạng chuẩn khi cách viết trùng mẫu (sau chuẩn hóa) hoặc khi cả mẫu lẫn bài nộp đều là MỘT số trơn (có thể kèm °); còn lại hiện. TẠM · vừa · suy từ SC1…SC8 | BA |
| Dev-S2E8-06 | BR-S2E8-10 (chữ a, x "thử giá trị") | Chưa nói thử bao nhiêu giá trị, miền nào. | Thử 3 bộ giá trị dương không đặc biệt cho (a, x); đúng khi khớp cả ba (SC7: √(2a²) = a√2 với a dương). TẠM · vừa | BA |
| Dev-S2E8-07 | BR-S2E8-08 | "sai lệch tương đối ≤ 1e-9" áp cho mọi cặp, nên số hữu tỉ viết thập phân rất dài (0,3333333333 so với 1/3) được tính Đúng dù không bằng tuyệt đối. | Làm đúng theo BR (1e-9 tương đối; mẫu bằng 0 thì tuyệt đối). Kiểu xấp xỉ so bằng phân số chính xác, biên bao gồm (BV5). TẠM · cao | — |
| Dev-S2E8-08 | US-S2E8-03 "hình ở giữa", FX-14 (đề không gian) | Đề kho `kind = 'space'` thêm vào kho nền tảng sẽ lọt vào bộ kiểm chứng cả kho của E5 (`fn_catalog_reference`) và làm lệch số đếm MVP. | Đề luyện mẫu là đề `kind = 'plane'`, bộ `s2e8_luyen`; FX-14 (SP-01) vẽ mặt cắt tam giác SAC bằng bộ vẽ 2D của S2-E2 (`/plane`), chưa vẽ hình chóp 3D trong màn bài luyện. TẠM · vừa | Design/PO: bài luyện với đề không gian dùng bộ xem 3D (task sau) |
| Dev-S2E8-09 | US-S2E8-05-SC4 "Xem trước như học sinh" | Bài nháp chưa có hàng bài giao nên chưa có hình qua `/student/assignments/{id}/plane`. | Xem trước hiện đề, ô đáp số, bàn phím, chấm thử (`POST …/preview/check`, không ghi Attempt), không hiện hình. TẠM · vừa | FIX nếu cần hình trong xem trước |
| Dev-S2E8-10 | BR-S2E8-26 (giờ mở lời giải) và PLAN A4 | AC SC3 đặt giờ mở theo câu; PLAN có cả giờ mở theo bộ. | Hỗ trợ cả hai: giờ mở của câu = giờ riêng của câu, không có thì theo bộ; cả hai trống = chưa đặt (khóa). Giờ đã qua + xác nhận thì lời giải mở ngay (giờ mở = lúc giao, vì `assignment.release_at ≥ created_at`). TẠM · cao | — |
| Dev-S2E8-11 | D-GEO-161 (một đề một lần mỗi lớp) | Mỗi câu sinh một hàng `assignment` MVP nên hai bài luyện giao cho CÙNG lớp không được trùng đề; giao trùng nhận CONFLICT "Có đề trong bài đã được giao cho lớp này." | Giữ ràng buộc MVP (PLAN A4 dùng lại hàng assignment). TẠM · vừa | PO/S3-E2: có cho giao lại cùng đề cho một lớp không |
| Dev-S2E8-12 | US-S2E8-04-FC1 "Bình gọi kết quả của An" | Không có đường nào nhận mã học sinh: kết quả luôn của người gọi. | Test gọi các đường đoán (…/result/{userId}) nhận NOT_FOUND và tham số `userId` bị bỏ qua. TẠM · cao | — |
| Dev-S2E8-13 | "Bài của em" (MVP) | Câu của bài luyện là hàng `assignment` nên sẽ lặp trong danh sách "Bài của em". | Danh sách "Bài của em" bỏ các hàng thuộc `assignment_item`; học sinh vào từ mục "Bài luyện" (rail). Chi tiết bài theo id vẫn mở được (hình, lời giải sau giờ mở). TẠM · vừa | PO xác nhận |
| Dev-S2E8-14 | TC-S2E8-011, 013, 014, 074, 087 (Manual) | Cần thiết bị thật. | Có kiểm máy phần đo được: ô đáp số không phải thẻ input (không bật bàn phím hệ thống), 6 cột cùng thứ tự, phím ≥ 44, không cuộn ngang, chữ ≥ 14 px trên iPhone/Pixel giả lập (`keypad.p0m.ui.spec.ts`). | Tester thiết bị |
| Dev-S2E8-15 | QA-S2E8-10 (Open) | Giáo viên chưa có danh sách "câu cần xem lại" của học sinh. | Không làm (chưa có AC; bảng lớp ở S3-E3). | PO |

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mục Dev (15 phát hiện) | runner-T-261126-21 |
