Phiên bản: v0.2 · 2026-11-26
# S3-E4 — câu hỏi và đề xuất đổi story map

> Chủ sở hữu: BA và QC. Mọi câu đều có mặc định đang dùng (đã chốt PO-66) để việc không dừng; chủ sản phẩm đã chốt toàn bộ ở gate 16 (PO-66, 26/11/2026, "OK hết"); AC đã cập nhật theo mã QA ở T-261126-37.

## Assumptions in use (giả định đang dùng)

| Mã | Câu hỏi | Mặc định đang dùng (quyết định · tin cậy · nguồn) | Ảnh hưởng | Hỏi ai |
|---|---|---|---|---|
| QA-S3E4-01 | "Em chưa làm" nghĩa là gì: chưa có tương tác nào trên mọi đề của bộ, hay còn đề chưa làm? | Chưa có tương tác nào trên mọi đề của bộ (khớp ví dụ bản mẫu "em chưa làm"). Đã chốt PO-66 · vừa · STAGES Ch3 §5 luồng C | US-S3E4-01 BR-S3E4-06; US-S3E4-02 SC1 | PO |
| QA-S3E4-02 | Bộ đã quá hạn thì rời "Bài đang giao" khi nào (không có thao tác Đóng bộ ở P0)? | Chưa rời cho tới khi lưu trữ lớp; xem QA-S3E2-07. Đã chốt PO-66 · thấp · Ch3 §8 | US-S3E4-01 SC6 | PO |
| QA-S3E4-03 | Số lớn hiển thị thế nào? | "99+" từ 100 trở lên. Đã chốt PO-66 · vừa · thông lệ | US-S3E4-01 FC1; US-S3E4-03 FC2 | Designer |
| QA-S3E4-04 | Biên giờ mới của "Đổi giờ": sau giờ hiện tại ít nhất bao lâu? | Ít nhất 1 phút; muốn mở ngay dùng Mở ngay. Đã chốt PO-66 · vừa · chống nhầm | US-S3E4-02 SC4 | PO |
| QA-S3E4-05 | Thao tác nhạy cảm khi mất mạng: xếp hàng gửi sau hay chặn? | Chặn, không tự gửi lại. Đã chốt PO-66 · vừa · tránh mở nhầm lời giải khi giáo viên đã đổi ý | US-S3E4-02 FC3 | PO |
| QA-S3E4-06 | Trên điện thoại "Mở ngay" và "Đổi giờ" tác động cả bộ hay từng đề? | Cả bộ; từng đề chỉ ở web. Đã chốt PO-66 · vừa · DQ-19, C3-06 "liếc và quyết nhanh" | US-S3E4-02 BR-S3E4-17 | Designer, PO |
| QA-S3E4-07 | Em vào lớp sau khi giao có nhận thông báo cũ không? | Không; bộ vẫn có trong Bài của em. Đã chốt PO-66 · vừa | US-S3E4-03 FC1 | PO |
| QA-S3E4-08 | Câu chữ MSG-S3-61…68 do ai duyệt? | BA đề xuất, Designer duyệt. Đã chốt PO-66 · cao | Mọi AC dẫn MSG-S3-6 | Designer |
| QA-S3E4-09 | Thời gian giữ thông báo? | 90 ngày. Đã chốt PO-66 · thấp · chính sách lưu và xóa dữ liệu (Q8) | US-S3E4-03 FC8 | PO, ARCH |

## Đề xuất đổi story map (chưa áp dụng; story map đã khóa R2, đổi là CR)

| Đề xuất | Căn cứ |
|---|---|
| Làm rõ `US-S3E4-02`: "Mở ngay" và "Đổi giờ" trên điện thoại tác động cả bộ | QA-S3E4-06 |

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: 9 câu hỏi và 1 đề xuất của S3-E4 | runner-T-261126-14 |
| v0.2 | 2026-11-26 | Áp PO-66: đóng các QA đã chốt ("Đã chốt PO-66"), giữ Open cho đề xuất ngoài gate | runner-T-261126-37 (BA/QC) |
