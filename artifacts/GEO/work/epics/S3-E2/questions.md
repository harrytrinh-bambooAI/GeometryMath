Phiên bản: v0.1 · 2026-11-26
# S3-E2 — câu hỏi và đề xuất đổi story map

> Chủ sở hữu: BA và QC. Mọi câu đều có mặc định đang dùng (TẠM) để việc không dừng; chủ sản phẩm chỉ cần sửa chỗ không đồng ý ở gate 16 (T-261126-16). Sau khi chốt, ghi vào `PO-DECISIONS.md` (ngoài phạm vi của task này) và cập nhật AC theo mã QA.

## Assumptions in use (giả định đang dùng)

| Mã | Câu hỏi | Mặc định đang dùng (TẠM · tin cậy · nguồn) | Ảnh hưởng | Hỏi ai |
|---|---|---|---|---|
| QA-S3E2-01 | Số đề tối thiểu và tối đa trong một bộ; một bộ giao cho một lớp hay nhiều lớp? | 1 đến 20 đề; một bộ một lớp, muốn giao lớp khác thì nhân bản. TẠM · vừa · Ch3 §5 luồng B | US-S3E2-01 SC6, FC1, FC2 | PO |
| QA-S3E2-02 | Hạn làm nằm trong quá khứ lúc giao: chặn hay cảnh báo? | Cảnh báo, cho tiếp; hạn chỉ là nhãn. TẠM · vừa · Ch3 §7 ý 4 | US-S3E2-01 FC7 | PO |
| QA-S3E2-03 | Biên cảnh báo "giờ mở sớm hơn hạn": cảnh báo khi sớm hơn từ 1 phút trở lên? | Có; bằng đúng hạn không cảnh báo. TẠM · vừa · DQ-19 | US-S3E2-02 FC2 | PO, Designer |
| QA-S3E2-04 | Học sinh có thấy nhãn nội bộ "Cần xem lại" của đề không? | Không; chỉ giáo viên. TẠM · vừa · PO-38 | US-S3E2-03 SC5 | PO |
| QA-S3E2-05 | Tiêu chí "xong" của một bộ (khi nào một đề tính vào tiến độ n/N)? | Đề có đáp số cuối đã gửi (Đúng hoặc Xem lại) tính là xong; Đang làm chưa. TẠM · vừa · BR-S3E3-02 | US-S3E2-03 SC1 | BA, PO |
| QA-S3E2-06 | Sửa bộ sau khi đã giao (thêm bỏ đề, đổi thứ tự): cho hay khóa? | Khóa nội dung sau khi giao (chỉ đổi hạn và lịch); muốn khác thì tạo bộ mới. TẠM · thấp · chưa có nguồn. Chưa có AC. | Ngoài AC; đề xuất story mới | PO |
| QA-S3E2-07 | Vòng đời Đã đóng và Lưu trữ của bộ (Ch3 §8) chưa có story nào ở P0: ai đóng bộ, đóng thì học sinh thấy gì? | Chưa làm ở chặng 3 P0; bộ Đã giao ở "Bài đang giao" cho tới khi giáo viên lưu trữ lớp. TẠM · thấp · Ch3 §8. Đề xuất thêm story. | US-S3E4-01 SC6; US-S3E6-03 FC4 | PO |
| QA-S3E2-08 | Câu chữ MSG-S3-21…28 do ai duyệt? | BA đề xuất, Designer duyệt. TẠM · cao | Mọi AC dẫn MSG-S3-2 | Designer |

## Đề xuất đổi story map (chưa áp dụng; story map đã khóa R2, đổi là CR)

| Đề xuất | Căn cứ |
|---|---|
| Thêm story "Đóng bộ / lưu trữ bộ" (vòng đời `AssignmentSet` Đã đóng, Lưu trữ) vào S3-E2 hoặc S3-E6 | QA-S3E2-07 |
| Thêm story "Sửa bộ sau khi giao" nếu owner muốn cho phép | QA-S3E2-06 |

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: 8 câu hỏi và 2 đề xuất của S3-E2 | runner-T-261126-14 |
