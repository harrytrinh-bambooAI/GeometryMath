Phiên bản: v0.1 · 2026-11-26
# S3-E2 — thiết kế kiểm thử (Bộ bài, lịch mở lời giải, Bài của em theo bộ)

> Chủ sở hữu: QC. Lập từ `stories_ac.md` cùng thư mục và `work/s23/story-map-s23.md`; phần chung dưới đây dùng cho cả S3-E1…E6. **Trạng thái: ready for R3**. Mỗi dòng thiết kế dẫn tới ít nhất một case ở `test_cases.md`; quyền theo quan hệ, chặn trường dữ liệu cá nhân (PO-61, PO-64) và AuditEvent là bắt buộc của chặng 3.

## Danh mục nhóm kiểm tra (CL) dùng chung cho S3-E1…E6
| Nhóm | Nội dung |
|---|---|
| CL-FN-01 | Luồng chính |
| CL-FN-02 | Dữ liệu nhập (biên, định dạng, độ dài) |
| CL-FN-03 | Lỗi, trạng thái rỗng và thông báo |
| CL-FN-04 | Chuyển trạng thái (lớp, thành viên, bộ, giờ mở) |
| CL-FN-05 | Phân quyền theo vai (sai vai thì FORBIDDEN) |
| CL-FN-06 | Cách ly theo quan hệ (ngoài quan hệ thì NOT_FOUND, không lộ dữ liệu) |
| CL-FN-07 | Dữ liệu máy chủ trả về (soát cả thân phản hồi, tiêu đề no-store) |
| CL-FN-08 | Phân trang và con trỏ |
| CL-FN-09 | Thời gian (biên giờ, giờ máy chủ, múi giờ, giờ yên lặng) |
| CL-FN-10 | Đồng thời, gửi lặp, trùng tên, hai thiết bị |
| CL-FN-11 | Nhãn máy và nhãn người (không ai ngoài engine gắn "Đã kiểm chứng") |
| CL-FN-12 | Định dạng và quy tắc hiển thị (không tổng, không hạng, thứ tự tên) |
| CL-FN-13 | Sự kiện, AuditEvent, Notification |
| CL-FN-14 | Cổng AI (giả lập, quá giờ, căn cứ, ẩn danh) |
| CL-UI-01 | Thiết bị và khổ màn (web, iPad, iPhone) |
| CL-UI-02 | Chuỗi hiển thị và khung chờ |
| CL-UI-04 | Gián đoạn (mất mạng) |
| CL-X-01 | Tải và hiệu năng |
| CL-X-02 | Kế hoạch truy vấn, chỉ mục |
| CL-X-03 | An toàn (IDOR, nội dung độc, ràng buộc cơ sở dữ liệu) |
| CL-X-04 | Dữ liệu cá nhân và trẻ em (PO-61, PO-64) |

## Applicability map (mỗi nhóm CL: Y / N kèm lý do)

| Group | Screen / story | Applies (Y/N) | Reason if N |
|---|---|---|---|
| CL-FN-01 | US-S3E2-01…03 | Y |  |
| CL-FN-02 | US-S3E2-01 (1…20 đề), 02 (giờ sai định dạng) | Y |  |
| CL-FN-03 | US-S3E2-01, 03 | Y |  |
| CL-FN-04 | US-S3E2-01 (Nháp, Đã giao), 02 (Chưa đặt giờ, Sắp mở, Đã mở) | Y |  |
| CL-FN-05 | US-S3E2-02, 03 | Y |  |
| CL-FN-06 | US-S3E2-01 (lớp, đề của người khác), 03 | Y |  |
| CL-FN-07 | US-S3E2-02 (lời giải khóa), 03 (tiến độ của bạn) | Y |  |
| CL-FN-08 | — | N | danh sách bộ ngắn; phân trang kiểm ở S3-E3, E4 |
| CL-FN-09 | US-S3E2-01, 02 (hạn, giờ mở, biên 1 phút) | Y |  |
| CL-FN-10 | US-S3E2-01 (Giao hai lần), 02 (hai lần đổi giờ) | Y |  |
| CL-FN-11 | US-S3E2-03 (đề "Cần xem lại") | Y |  |
| CL-FN-12 | — | N | định dạng giờ gộp vào CL-FN-09 |
| CL-FN-13 | US-S3E2-01 (Notification bài mới, kiểm ở S3-E4) | Y |  |
| CL-FN-14 | — | N | không AI |
| CL-UI-01 | US-S3E2-03 (iPad, iPhone) | Y |  |
| CL-UI-02 | — | N | gộp vào CL-FN-03 |
| CL-UI-04 | — | N | hiếm |
| CL-X-01 | — | N | không tải riêng |
| CL-X-02 | — | N | không danh sách lớn |
| CL-X-03 | US-S3E2-02 (bộ câu thử moi đáp số) | Y |  |
| CL-X-04 | US-S3E2-01, 03 (thân phản hồi) | Y |  |

## Design rows: `CL id · condition · expected rule · technique · level`

| CL id | Story | Condition | Expected rule (nguồn) | Technique | Level |
|---|---|---|---|---|---|
| CL-FN-02 | 01 | bộ 0, 1, 20, 21 đề; đề trùng trong bộ | 0 và 21 bị chặn; 1 và 20 nhận; trùng bị chặn | BVA | tích hợp API |
| CL-FN-04 | 01 | Nháp rồi Giao; Giao hai lần cùng khóa | một bộ Đã giao, một đợt thông báo | chuyển trạng thái, đoán lỗi | tích hợp API |
| CL-FN-06 | 01 | chọn lớp hoặc đề Của tôi của giáo viên khác | NOT_FOUND, bộ không đổi | bảng quyết định | tích hợp API |
| CL-FN-07 | 01 | xem trước như học sinh | không ghi tiến độ, không thông báo, lời giải theo lịch | soát | E2E trình duyệt |
| CL-FN-09 | 01 | giao khi hạn nằm quá khứ; quá hạn vẫn làm được | cảnh báo rồi cho giao; chip Quá hạn chỉ là nhãn | EP | E2E trình duyệt |
| CL-FN-09 | 02 | giờ máy chủ 07:59:59, 08:00:00, 08:00:01 với giờ mở 08:00 | khóa, mở, mở (bảng biên bên dưới) | BVA | tích hợp API |
| CL-FN-09 | 02 | giờ mở sớm hơn hạn 0, 1 phút, 1 ngày | không cảnh báo, cảnh báo, cảnh báo | BVA | E2E trình duyệt |
| CL-FN-09 | 02 | đồng hồ thiết bị lệch; tiêu đề giờ thiết bị | bỏ qua, chỉ giờ máy chủ | đoán lỗi | tích hợp API |
| CL-X-03 | 02 | bộ câu thử moi đáp số qua mọi kênh khi khóa | không kênh nào lộ đáp số hay bước giải | soát, đoán lỗi | tích hợp API, bộ thử riêng |
| CL-FN-10 | 02 | hai lần đổi giờ cùng phiên bản | lần sau CONFLICT | đồng thời | tích hợp API |
| CL-FN-04 | 02 | đổi giờ khi đã mở | CONFLICT | bảng chuyển trạng thái | tích hợp API |
| CL-FN-11 | 03 | giao bộ có đề Cần xem lại | liệt kê trước khi giao, giáo viên quyết; học sinh không thấy nhãn | EP | E2E trình duyệt |
| CL-FN-06 | 03 | học sinh Chờ duyệt, bộ Nháp, tiến độ bạn | không thấy; NOT_FOUND; thân phản hồi không có dữ liệu bạn | bảng quyết định | tích hợp API |
| CL-UI-01 | 03 | iPad ngang, iPhone dọc | một cột, đọc được, không cuộn ngang | soát | thủ công trên thiết bị |

## Bảng biên giờ mở (BV-n) cho US-S3E2-02
Giờ mở `release_at` = 06/12/2026 08:00:00 giờ Việt Nam. t là giờ máy chủ. Thân phản hồi lời giải của học sinh Đang học.

| BV | t | Trạng thái | Lời giải trong thân phản hồi |
|---|---|---|---|
| BV-1 | 07:59:59 | Sắp mở | không |
| BV-2 | 08:00:00 | Đã mở | có |
| BV-3 | 08:00:01 | Đã mở | có |
| BV-4 | chưa có giờ | Chưa đặt giờ | không |
| BV-5 | giờ thiết bị 10/12 nhưng t = 05/12 | Sắp mở | không (giờ thiết bị bị bỏ qua) |

Biên giờ mở so với hạn làm `due_at` = 11/12/2026 20:00:00: giờ mở 20:00 không cảnh báo; 19:59 cảnh báo; 10/12 08:00 cảnh báo.

## Tự động hóa và cổng kiểm

- Cột "Tự động hóa" của `test_cases.md` là đầu vào của bộ E2E (PO-50); case `IT` chạy ở tầng tích hợp với cơ sở dữ liệu thử; case `Manual` chỉ khi cần thiết bị thật (iPad, iPhone).
- AI giả lập trong CI (D-GEO-047); không gọi AI thật. Dữ liệu dùng bộ mẫu chung F-S3-nnn; khôi phục dữ liệu sau case ghi ở Remark.
- Case chặn trường dữ liệu cá nhân chạy trong cổng CI của mọi PR (không chỉ vòng kiểm thử cuối).

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: thiết kế kiểm thử S3-E2 | runner-T-261126-14 (QC) |
