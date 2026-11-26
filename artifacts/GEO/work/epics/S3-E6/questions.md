Phiên bản: v0.1 · 2026-11-26
# S3-E6 — câu hỏi và đề xuất đổi story map

> Chủ sở hữu: BA và QC. Mọi câu đều có mặc định đang dùng (TẠM) để việc không dừng; chủ sản phẩm chỉ cần sửa chỗ không đồng ý ở gate 16 (T-261126-16). Sau khi chốt, ghi vào `PO-DECISIONS.md` (ngoài phạm vi của task này) và cập nhật AC theo mã QA.

## Assumptions in use (giả định đang dùng)

| Mã | Câu hỏi | Mặc định đang dùng (TẠM · tin cậy · nguồn) | Ảnh hưởng | Hỏi ai |
|---|---|---|---|---|
| QA-S3E6-01 | Đề vừa sửa phiên bản mà bước của thẻ không còn: mở ở đâu? | Mở từ đầu đề kèm thông báo. TẠM · thấp · S2-E5 | US-S3E6-01 FC2 | PO |
| QA-S3E6-02 | Nút "Chữa bài này" chưa có trong bản mẫu `c3-04`. Vẽ thêm? | Cần Designer bổ sung ở vòng sau; AC dựng theo mô tả. TẠM · cao · DQ.md mục "Chưa vẽ" | US-S3E6-01 (cả story) | Designer |
| QA-S3E6-03 | Đo "số lần bấm" M-S3-04: gộp lần bấm lặp? | Gộp trong 5 giây thành một. TẠM · thấp · M-S3-04 | US-S3E6-01 SC4, FC3 | PO |
| QA-S3E6-04 | Định dạng xuất kết quả: CSV hay PDF hay cả hai? | CSV UTF-8 có BOM; in bằng nút in của trình duyệt. TẠM · vừa · Ch3 §4 P1 | US-S3E6-02 SC1…SC4 | PO |
| QA-S3E6-05 | Giới hạn xuất? | 10 lần/giờ mỗi giáo viên. TẠM · thấp | US-S3E6-02 FC3 | PO |
| QA-S3E6-06 | Lớp đã lưu trữ: em chờ duyệt, đặt lại mật khẩu, lịch mở lời giải xử lý thế nào? | Em chờ duyệt thành Bị từ chối; không đặt lại mật khẩu; lịch mở vẫn chạy; học sinh chỉ xem. TẠM · vừa · Ch3 §8 | US-S3E6-02 SC5, SC6, FC7, FC8 | PO |
| QA-S3E6-07 | Bỏ lưu trữ lớp: có không, mã lớp thế nào? | Có; nhận mã lớp mới. TẠM · thấp | US-S3E6-02 SC7 | PO |
| QA-S3E6-08 | Quy tắc nhắc việc: nhắc trước hạn bao lâu, giờ yên lặng, đối tượng? | 24 giờ trước hạn; giờ yên lặng 21:00 đến 06:00; chỉ em còn đề chưa xong; bộ giao khi còn dưới 24 giờ thì không nhắc hạn. TẠM · thấp · STAGES Ch3 §3 ý 8 chưa nêu thông số | US-S3E6-03 (đa số AC) | PO |
| QA-S3E6-09 | Câu chữ MSG-S3-91…96 do ai duyệt? | BA đề xuất, Designer duyệt. TẠM · cao | Mọi AC dẫn MSG-S3-9 | Designer |

## Đề xuất đổi story map (chưa áp dụng; story map đã khóa R2, đổi là CR)

| Đề xuất | Căn cứ |
|---|---|
| Bản mẫu cần thêm nút "Chữa bài này" ở thẻ nhận định (Design), không đổi story map | QA-S3E6-02 |

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: 9 câu hỏi và 1 đề xuất của S3-E6 | runner-T-261126-14 |
