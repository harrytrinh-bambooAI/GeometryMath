Phiên bản: v0.1 · 2026-11-26
# S3-E5 — câu hỏi và đề xuất đổi story map

> Chủ sở hữu: BA và QC. Mọi câu đều có mặc định đang dùng (đã chốt PO-66) để việc không dừng; chủ sản phẩm đã chốt toàn bộ ở gate 16 (PO-66, 26/11/2026, "OK hết"); AC đã cập nhật theo mã QA ở T-261126-37.

## Assumptions in use (giả định đang dùng)

| Mã | Câu hỏi | Mặc định đang dùng (quyết định · tin cậy · nguồn) | Ảnh hưởng | Hỏi ai |
|---|---|---|---|---|
| QA-S3E5-01 | Ai đọc được AuditEvent và qua đâu (STAGES không nêu màn xem)? | Chủ lớp đọc nhật ký lớp mình qua API chỉ đọc (chưa có màn); chủ sản phẩm đọc qua truy vấn quản trị. Đã chốt PO-66 · thấp · Ch3 §7 | US-S3E5-01 SC6, FC5, FC6 | PO |
| QA-S3E5-02 | Có ghi cả lần bị từ chối vì quyền hoặc quan hệ không (để đếm M-S3-06)? | Có, dưới dạng sự kiện bảo mật chỉ có loại, người gọi, loại đối tượng, giờ. Đã chốt PO-66 · vừa · M-S3-06 | US-S3E5-01 FC3; US-S3E1-05 FC6; US-S3E1-04 FC1 | PO, ARCH |
| QA-S3E5-03 | Đặt lịch lần đầu khi bộ còn Nháp có tính là sự kiện không? | Không; chỉ đổi giờ sau khi giao. Đã chốt PO-66 · vừa · Ch3 §7 "đổi giờ" | US-S3E5-01 FC2; US-S3E2-02 FC3 | PO |
| QA-S3E5-04 | Chưa bấm "Đã đọc" thông báo riêng tư: chặn cả ứng dụng hay chỉ nhắc? | Chặn dùng phần còn lại cho tới khi bấm. Đã chốt PO-66 · thấp · PO-64 | US-S3E5-02 FC3 | PO |
| QA-S3E5-05 | Tên hiển thị trông như email hoặc số điện thoại: có chặn không, ngưỡng bao nhiêu chữ số? | Chặn email và từ 9 chữ số liền (bỏ khoảng trắng, dấu chấm, gạch). Đã chốt PO-66 · thấp · PO-61 | US-S3E5-03 FC1…FC3 | PO |
| QA-S3E5-06 | Địa chỉ IP trong nhật ký truy cập của máy chủ web: giữ bao lâu? | Không quá 7 ngày, không có trong bảng ứng dụng. Đã chốt PO-66 · thấp · Q8 chính sách lưu và xóa | US-S3E5-03 FC5 | PO, ARCH |
| QA-S3E5-07 | MVP dùng mã trường (`org_id`) để cách ly dữ liệu; PO-61 nói không thu "trường". Giữ `org_id` kỹ thuật không do người dùng nhập? | Giữ như định danh nội bộ, không hiển thị, không có trong phản hồi API. Đã chốt PO-66 · vừa · PO-61, MVP D-GEO-051 | US-S3E5-03 FC7 | PO, ARCH |
| QA-S3E5-08 | Công tắc AI: mặc định bật hay tắt; tắt có ảnh hưởng AI gộp của giáo viên và Tự học; đổi công tắc có cần AuditEvent? | Mặc định bật; không ảnh hưởng AI gộp và Tự học; không ghi AuditEvent (đề xuất owner cân nhắc). Lượt đang chạy hoàn tất. Đã chốt PO-66 · vừa · STAGES 0.3 ý 7 | US-S3E5-04 SC6, FC3, FC5, FC6 | PO |
| QA-S3E5-09 | Câu chữ MSG-S3-81…85 do ai duyệt; văn bản thông báo riêng tư cần tư vấn pháp lý thu hẹp (Q8)? | BA đề xuất, Designer duyệt; văn bản chính thức theo Q8 đã thu hẹp còn chính sách lưu và xóa (US-S3E5-05, đã chốt PO-66). Đã chốt PO-66 · cao · PO-64 | US-S3E5-02 | PO, Designer |
| QA-S3E5-10 | Thiếu story: chính sách lưu và xóa dữ liệu bài làm, nút "Xóa tài khoản" (PO-64 yêu cầu làm trước L7 chặng 3) không có story trong story map. Thêm? | Đã thêm US-S3E5-05 "Xóa tài khoản và chính sách lưu/xóa" (P0) kèm AC. Đã chốt PO-66 (G5) · PO-64 | Ngoài AC | PO |

## Đề xuất đổi story map (chưa áp dụng; story map đã khóa R2, đổi là CR)

| Đề xuất | Căn cứ |
|---|---|
| Thêm story `US-S3E5-05` Xóa tài khoản và chính sách lưu/xóa dữ liệu bài làm (theo PO-64, Q8): story map chưa có · **Đã nhận, PO-66 (G5)** | QA-S3E5-10 |
| Làm rõ `US-S3E5-01`: có ghi sự kiện bảo mật cho lần bị từ chối (cho M-S3-06) | QA-S3E5-02 |

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: 10 câu hỏi và 2 đề xuất của S3-E5 | runner-T-261126-14 |
