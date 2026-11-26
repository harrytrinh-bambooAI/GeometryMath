Phiên bản: v0.1 · 2026-11-26
# S3-E6 — thiết kế kiểm thử (Chữa bài từ kết quả sang Trình chiếu, xuất kết quả lớp, lưu trữ lớp, nhắc việc trong ứng dụng (P1))

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
| CL-FN-01 | US-S3E6-01…03 | Y |  |
| CL-FN-02 | US-S3E6-02 (tên em có dấu công thức) | Y |  |
| CL-FN-03 | US-S3E6-01 (mất mạng), 02 | Y |  |
| CL-FN-04 | US-S3E6-02 (Đang dạy, Đã lưu trữ), 03 (bộ Đã đóng) | Y |  |
| CL-FN-05 | US-S3E6-01, 02 | Y |  |
| CL-FN-06 | US-S3E6-01, 02 | Y |  |
| CL-FN-07 | US-S3E6-01 (màn chiếu không tên em), 03 (nội dung nhắc) | Y |  |
| CL-FN-08 | — | N | không danh sách lớn |
| CL-FN-09 | US-S3E6-02 (lịch mở vẫn chạy), 03 (24 giờ, 21:00, 06:00) | Y |  |
| CL-FN-10 | US-S3E6-01 (bấm hai lần), 02 (xuất liên tục), 03 (tác vụ lặp) | Y |  |
| CL-FN-11 | — | N | không nhãn máy |
| CL-FN-12 | US-S3E6-02 (tên tệp, không tổng) | Y |  |
| CL-FN-13 | US-S3E6-01 (sự kiện đo), 03 (thông báo) | Y |  |
| CL-FN-14 | — | N | không AI |
| CL-UI-01 | — | N | giáo viên web; chữa bài chiếu toàn màn hình kiểm ở S2-E7 |
| CL-UI-02 | — | N | gộp vào CL-FN-03 |
| CL-UI-04 | US-S3E6-01 (mất mạng) | Y |  |
| CL-X-01 | — | N | không tải riêng |
| CL-X-02 | — | N | không danh sách lớn |
| CL-X-03 | US-S3E6-02 (công thức trong tệp CSV) | Y |  |
| CL-X-04 | US-S3E6-01, 02, 03 | Y |  |

## Design rows: `CL id · condition · expected rule · technique · level`

| CL id | Story | Condition | Expected rule (nguồn) | Technique | Level |
|---|---|---|---|---|---|
| CL-FN-01 | 01 | bấm Chữa bài này từ thẻ Đề 3 | Trình chiếu mở đúng đề, đúng bước | EP | E2E trình duyệt |
| CL-FN-04 | 01 | lời giải còn khóa | hộp hỏi lại; Hủy không mở; Tiếp tục mở; máy học sinh vẫn khóa | bảng quyết định | E2E trình duyệt |
| CL-X-04 | 01 | màn chiếu và thân phản hồi | không tên, mã, số em | soát | tích hợp API |
| CL-FN-10 | 01 | bấm hai lần trong 1 giây; hai lần cách 10 giây | một phiên, một sự kiện đo; hai sự kiện | BVA | tích hợp API |
| CL-FN-03 | 01 | thẻ dữ liệu thô; đề đã sửa phiên bản | không có nút; mở từ đầu đề kèm toast | EP | E2E trình duyệt |
| CL-FN-06 | 01 | giáo viên khác, học sinh | NOT_FOUND; FORBIDDEN | bảng quyết định | tích hợp API |
| CL-FN-12 | 02 | xuất bảng tuần 15; tên tệp | đúng hàng, cột, không tổng; tên không tên em | EP | tích hợp API |
| CL-X-03 | 02 | tên em bắt đầu bằng `=`, `+` | thêm dấu nháy đơn, bảng tính không chạy công thức | đoán lỗi | tích hợp API |
| CL-X-04 | 02 | mọi ô của tệp xuất | chỉ tên, mã, cờ đã rời, trạng thái | soát | tích hợp API |
| CL-FN-10 | 02 | xuất lần 10 và 11 trong 1 giờ | lần 11 RATE_LIMITED (TẠM) | BVA | tích hợp API |
| CL-FN-04 | 02 | lưu trữ lớp: mã, em chờ duyệt, học sinh thấy gì, bỏ lưu trữ, giao bộ mới, đặt lại mật khẩu, lịch mở | theo BR-S3E6-14…17 | bảng chuyển trạng thái | E2E trình duyệt, tích hợp API |
| CL-FN-09 | 03 | giờ nhắc 20:59:59, 21:00:00, 06:00:00 | gửi ngay; hoãn tới 06:00; gửi ngay (bảng biên bên dưới) | BVA | tích hợp CSDL |
| CL-FN-13 | 03 | nhắc trước 24 giờ cho 4 em chưa xong; 4 em đã xong | đúng 4 em nhận, 4 em không | EP | tích hợp CSDL |
| CL-FN-10 | 03 | tác vụ chạy lặp | không gửi hai lần | đoán lỗi | tích hợp CSDL |
| CL-FN-04 | 03 | đổi hạn; bộ Đã đóng; Chờ duyệt, Đã rời, Bị từ chối; bộ không có hạn; giao khi còn dưới 24 giờ | nhắc theo hạn mới hoặc không có nhắc | bảng quyết định | tích hợp CSDL |
| CL-X-04 | 03 | nội dung nhắc; kênh ngoài | chỉ tên bộ và giờ; 0 lượt gửi ngoài | soát | tích hợp API, CSDL |

## Bảng biên giờ nhắc (BV-n) cho US-S3E6-03
Hạn `due_at` = 04/12/2026 20:00:00 giờ Việt Nam; giờ nhắc = 24 giờ trước = 03/12/2026 20:00:00; giờ yên lặng 21:00:00 đến 06:00:00.

| BV | Giờ nhắc (giờ Việt Nam) | Kết quả |
|---|---|---|
| BV-1 | 20:59:59 | gửi ngay |
| BV-2 | 21:00:00 | hoãn tới 06:00:00 hôm sau |
| BV-3 | 02:00:00 | hoãn tới 06:00:00 |
| BV-4 | 06:00:00 | gửi ngay |
| BV-5 | bộ giao lúc 21:00 hôm trước hạn (còn 23 giờ) | không có nhắc hạn |
| BV-6 | bộ không có hạn | không có nhắc hạn |
| BV-7 | đổi hạn sang 05/12/2026 20:00 | nhắc cũ hủy; nhắc mới lên 04/12/2026 20:00 |
| BV-8 | hạn 04/12 nhưng bộ Đã đóng lúc 10:00 ngày 03/12 | không gửi |

Em "chưa xong bộ" = còn ít nhất một đề Chưa làm hoặc Đang làm: theo F-S3-051 là Chi Mai, Gia Bảo, Khánh Linh, Ngọc Anh; đã xong: Bình An, Đức Huy, Hà My, Minh Quân.

## Tự động hóa và cổng kiểm

- Cột "Tự động hóa" của `test_cases.md` là đầu vào của bộ E2E (PO-50); case `IT` chạy ở tầng tích hợp với cơ sở dữ liệu thử; case `Manual` chỉ khi cần thiết bị thật (iPad, iPhone).
- AI giả lập trong CI (D-GEO-047); không gọi AI thật. Dữ liệu dùng bộ mẫu chung F-S3-nnn; khôi phục dữ liệu sau case ghi ở Remark.
- Case chặn trường dữ liệu cá nhân chạy trong cổng CI của mọi PR (không chỉ vòng kiểm thử cuối).

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: thiết kế kiểm thử S3-E6 | runner-T-261126-14 (QC) |
