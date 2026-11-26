Phiên bản: v0.1 · 2026-11-26
# S3-E4 — thiết kế kiểm thử (Tổng quan giáo viên, theo dõi nhanh trên điện thoại, thông báo trong ứng dụng)

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
| CL-FN-01 | US-S3E4-01…03 | Y |  |
| CL-FN-02 | US-S3E4-02 (giờ mới) | Y |  |
| CL-FN-03 | US-S3E4-01 (chưa có lớp, không có việc), 02 (mất mạng) | Y |  |
| CL-FN-04 | US-S3E4-02 (Sắp mở, Đã mở) | Y |  |
| CL-FN-05 | US-S3E4-01…03 | Y |  |
| CL-FN-06 | US-S3E4-01, 02, 03 | Y |  |
| CL-FN-07 | US-S3E4-01 (không tên em), 03 (không lộ đáp số) | Y |  |
| CL-FN-08 | US-S3E4-03 (20 dòng mỗi trang) | Y |  |
| CL-FN-09 | US-S3E4-02 (biên 1 phút, giờ máy chủ), 03 (90 ngày) | Y |  |
| CL-FN-10 | US-S3E4-02 (bấm lặp, web và điện thoại), 03 (sự kiện lặp) | Y |  |
| CL-FN-11 | — | N | không nhãn máy |
| CL-FN-12 | US-S3E4-01, 03 (99+) | Y |  |
| CL-FN-13 | US-S3E4-02, 03 (AuditEvent, Notification) | Y |  |
| CL-FN-14 | — | N | không AI |
| CL-UI-01 | US-S3E4-01, 02 (điện thoại 390×844) | Y |  |
| CL-UI-02 | — | N | gộp vào CL-FN-03 |
| CL-UI-04 | US-S3E4-02 (mất mạng) | Y |  |
| CL-X-01 | — | N | không tải riêng |
| CL-X-02 | — | N | chưa có danh sách lớn |
| CL-X-03 | — | N | gộp vào CL-FN-06 |
| CL-X-04 | US-S3E4-01 (tổng quan không tên em), 03 (không đẩy ngoài) | Y |  |

## Design rows: `CL id · condition · expected rule · technique · level`

| CL id | Story | Condition | Expected rule (nguồn) | Technique | Level |
|---|---|---|---|---|---|
| CL-FN-01 | 01 | dữ liệu F-S3-050, 053, 030…032 | ba thẻ đúng số liệu, "1 em chưa mở", "3 em chờ duyệt" | EP | E2E trình duyệt |
| CL-FN-12 | 01 | số em chờ duyệt 0, 1, 99, 100 | ẩn thẻ khi 0; "99"; "99+" ở 100 | BVA | tích hợp API |
| CL-FN-03 | 01 | chưa có lớp; có lớp không việc | MSG-S3-66; MSG-S3-65 | bảng trạng thái | E2E trình duyệt |
| CL-FN-04 | 01 | bộ Đã giao, Nháp, quá hạn, đã mở lời giải | đúng thẻ; Nháp không vào; quá hạn còn tới khi đóng | bảng trạng thái | E2E trình duyệt |
| CL-FN-07 | 01 | liệt kê mọi chuỗi trong thân Tổng quan | không tên hay mã học sinh, no-store | soát | tích hợp API |
| CL-UI-01 | 01, 02 | điện thoại 390×844 | một cột, nút 44 điểm ảnh, không cuộn ngang | soát | thủ công trên thiết bị |
| CL-FN-01 | 02 | danh sách em chưa làm của bộ tuần 15 | chỉ Khánh Linh | EP | E2E trình duyệt |
| CL-FN-09 | 02 | giờ máy chủ 21:00:00, giờ mới 21:01:00, 21:00:59, quá khứ | 21:01:00 nhận; 21:00:59 và quá khứ VALIDATION_FAILED (bảng biên bên dưới) | BVA | tích hợp API |
| CL-FN-13 | 02 | Mở ngay; Đổi giờ | một AuditEvent đúng loại; một thông báo mỗi em Đang học | EP | tích hợp API |
| CL-FN-10 | 02 | bấm hai lần cùng khóa; web đổi cùng lúc điện thoại Mở ngay | một tác dụng; lần sau CONFLICT | đồng thời | tích hợp API |
| CL-FN-04 | 02 | đã mở rồi Mở ngay hay Đổi giờ | CONFLICT | bảng chuyển trạng thái | tích hợp API |
| CL-UI-04 | 02 | mất mạng ở hộp xác nhận | nút tắt, không tự gửi lại | đoán lỗi | thủ công trên thiết bị |
| CL-FN-09 | 02 | đồng hồ điện thoại lệch 3 giờ | dùng giờ máy chủ, bỏ giờ điện thoại | đoán lỗi | tích hợp API |
| CL-FN-06 | 02 | giáo viên khác, học sinh gọi Mở ngay | NOT_FOUND; FORBIDDEN; bộ đếm từ chối tăng | bảng quyết định | tích hợp API |
| CL-FN-13 | 03 | giao bộ; Mở ngay; Đổi giờ; mở thông báo; Đánh dấu tất cả | đúng người nhận, đúng nội dung, Chưa đọc rồi Đã đọc, chấm số đúng | EP | E2E trình duyệt |
| CL-FN-12 | 03 | chấm số 99 và 100 | "99" và "99+" | BVA | tích hợp API |
| CL-FN-08 | 03 | 45 thông báo | trang 20, 20, 5; con trỏ sửa thì INVALID_CURSOR | BVA | tích hợp API |
| CL-FN-10 | 03 | sự kiện xử lý hai lần | mỗi em một thông báo, ràng buộc duy nhất chặn bản ghi trùng | đoán lỗi | tích hợp CSDL |
| CL-FN-09 | 03 | thông báo 89 và 91 ngày tuổi | 91 ngày xóa, 89 còn (đã chốt PO-66) | BVA | tích hợp CSDL |
| CL-X-04 | 03 | gửi tin ngoài (push, email, SMS) | 0 lượt gọi | soát | tích hợp CSDL |

## Bảng biên cho US-S3E4-02 và US-S3E4-03
Giờ máy chủ t = 05/12/2026 21:00:00; giờ mở hiện tại 06/12/2026 08:00.

| BV | Hành động | Kết quả |
|---|---|---|
| BV-1 | Đổi giờ sang 21:01:00 cùng ngày | nhận |
| BV-2 | Đổi giờ sang 21:00:59 cùng ngày | VALIDATION_FAILED, MSG-S3-63 |
| BV-3 | Đổi giờ sang quá khứ | VALIDATION_FAILED, MSG-S3-63 |
| BV-4 | Mở ngay khi chưa đặt giờ | nhận, giờ cũ ghi "chưa đặt" |
| BV-5 | Mở ngay khi đã mở | CONFLICT, MSG-S3-67 |
| BV-6 | Chấm số chưa đọc 99 / 100 | "99" / "99+" |
| BV-7 | Số em chờ duyệt 99 / 100 | "99" / "99+" |
| BV-8 | Thông báo 89 / 91 ngày tuổi | còn / xóa |

## Tự động hóa và cổng kiểm

- Cột "Tự động hóa" của `test_cases.md` là đầu vào của bộ E2E (PO-50); case `IT` chạy ở tầng tích hợp với cơ sở dữ liệu thử; case `Manual` chỉ khi cần thiết bị thật (iPad, iPhone).
- AI giả lập trong CI (D-GEO-047); không gọi AI thật. Dữ liệu dùng bộ mẫu chung F-S3-nnn; khôi phục dữ liệu sau case ghi ở Remark.
- Case chặn trường dữ liệu cá nhân chạy trong cổng CI của mọi PR (không chỉ vòng kiểm thử cuối).

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: thiết kế kiểm thử S3-E4 | runner-T-261126-14 (QC) |
