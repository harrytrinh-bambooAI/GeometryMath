Phiên bản: v0.1 · 2026-11-26
# S3-E1 — thiết kế kiểm thử (Lớp, mã lớp, duyệt vào lớp, đặt lại mật khẩu, quyền theo quan hệ)

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
| CL-FN-01 | US-S3E1-01…04 | Y |  |
| CL-FN-02 | US-S3E1-01 (tên lớp, năm học), 02 (mã lớp) | Y |  |
| CL-FN-03 | US-S3E1-01…04 | Y |  |
| CL-FN-04 | US-S3E1-02, 03, 04 (Chờ duyệt, Đang học, Đã rời, Bị từ chối) | Y |  |
| CL-FN-05 | US-S3E1-01, 03, 04, 05 | Y |  |
| CL-FN-06 | US-S3E1-03, 05 (ngoài quan hệ) | Y |  |
| CL-FN-07 | US-S3E1-04 (mật khẩu tạm không trả lần hai), 05 | Y |  |
| CL-FN-08 | US-S3E1-05 (con trỏ lớp khác) | Y |  |
| CL-FN-09 | US-S3E1-04 (mật khẩu tạm hết hạn 24 giờ) | Y |  |
| CL-FN-10 | US-S3E1-02, 03, 04 (gửi lặp, hai tab, dò mã) | Y |  |
| CL-FN-11 | — | N | E1 không có nhãn máy |
| CL-FN-12 | — | N | chưa có định dạng số |
| CL-FN-13 | US-S3E1-04 (AuditEvent PASSWORD_RESET, kiểm ở S3-E5) | Y |  |
| CL-FN-14 | — | N | không có AI ở E1 |
| CL-UI-01 | US-S3E1-02 (iPad) | Y |  |
| CL-UI-02 | — | N | đã gộp vào CL-FN-03 |
| CL-UI-04 | — | N | không thao tác nhạy cảm cần offline; hiếm |
| CL-X-01 | — | N | tải của epic ghi ở S3-E3 |
| CL-X-02 | — | N | không có danh sách lớn ở E1 |
| CL-X-03 | US-S3E1-05 (đổi mã trên đường dẫn, sự kiện từ chối) | Y |  |
| CL-X-04 | US-S3E1-01, 04 (thân phản hồi chỉ trường được phép) | Y |  |

## Design rows: `CL id · condition · expected rule · technique · level`

| CL id | Story | Condition | Expected rule (nguồn) | Technique | Level |
|---|---|---|---|---|---|
| CL-FN-02 | 01 | tên lớp 0, 1, 60, 61 ký tự; năm học 2026–2027 đúng, 2026–2028 sai | vào/ra theo BR-S3E1-02, VALIDATION_FAILED có `details` | BVA, EP | đơn vị, tích hợp API |
| CL-FN-10 | 01 | mã lớp sinh trùng một mã đang có | sinh lại, ràng buộc duy nhất của cơ sở dữ liệu chặn bản ghi trùng | đoán lỗi | tích hợp CSDL |
| CL-FN-02 | 01 | tạo lớp thứ 20 và thứ 21 | 20 được, 21 bị chặn; lưu trữ một lớp thì tạo tiếp được | BVA | tích hợp API |
| CL-FN-04 | 01 | đặt lại mã lớp; tắt/bật "Cho vào lớp bằng mã" | mã cũ hết hiệu lực, thành viên giữ nguyên; mã khóa cùng thông điệp với mã sai | chuyển trạng thái | E2E trình duyệt |
| CL-FN-10 | 02 | nhập sai mã 9, 10, 11 lần trong 15 phút (theo tài khoản) | 9 lần chưa khóa, 11 bị RATE_LIMITED, 16 phút sau bình thường (PO-53) | BVA, bảng thời gian | tích hợp API |
| CL-FN-02 | 02 | mã chữ thường, thừa khoảng trắng, mã rỗng, mã quá dài | chuẩn hóa hoặc VALIDATION_FAILED; không tiết lộ lớp tồn tại | EP, đoán lỗi | tích hợp API |
| CL-FN-10 | 02 | gửi hai lần liên tiếp; em đã Đang học nhập lại; em Đã rời xin lại | một bản ghi; CONFLICT khi đã Đang học; Chờ duyệt mới khi Đã rời | đoán lỗi | tích hợp API |
| CL-FN-04 | 03 | duyệt, từ chối, cho rời, duyệt lặp, quyết định trái nhau từ hai tab | chuyển đúng bảng trạng thái; lặp cùng quyết định thành công; trái nhau CONFLICT | bảng chuyển trạng thái | tích hợp API |
| CL-FN-10 | 03 | hai em "Hoàng Nam" Chờ duyệt | phân biệt bằng mã người dùng, không hỏi ký hiệu (DQ-25) | đoán lỗi | E2E trình duyệt |
| CL-FN-07 | 04 | mật khẩu tạm sau khi đóng hộp | mọi API không trả lại, không vào nhật ký hay AuditEvent | soát | tích hợp API |
| CL-FN-09 | 04 | mật khẩu tạm ở 23 giờ 59 phút và 24 giờ 01 phút | dùng được rồi hết hạn (TẠM 24 giờ) | BVA | tích hợp API |
| CL-FN-10 | 04 | đặt lại lần 10 và 11 trong 1 giờ | lần 11 RATE_LIMITED (TẠM) | BVA | tích hợp API |
| CL-FN-05 | 04 | đặt lại cho em Đã rời, Chờ duyệt, tài khoản giáo viên, em lớp khác | NOT_FOUND ở mọi trường hợp; mật khẩu không đổi | bảng quyết định | tích hợp API |
| CL-FN-06 | 05 | giáo viên A gọi API lớp B; học sinh gọi API giáo viên; đổi mã lớp/mã em; con trỏ lạ | ma trận quyền bên dưới | bảng quyết định | tích hợp API |
| CL-X-03 | 05 | 5 yêu cầu ngoài quan hệ | bộ đếm sự kiện bảo mật tăng đúng 5, bản ghi không chứa nội dung | soát | tích hợp CSDL |

## Ma trận quyền theo quan hệ (cho US-S3E1-05, dùng lại ở E2…E6)
Cột là người gọi; hàng là hành động; ô là kết quả mong đợi. Dữ liệu: F-S3-001, 002, 010, 011, 012, 020…027, 028, 030…032, 040, 041.

| Hành động (lớp 11A1) | Cô Lan (chủ lớp) | Thầy Khôi (giáo viên khác) | Bình An (Đang học) | Hoàng Nam (Chờ duyệt) | Văn Tú (Đã rời) | Chưa đăng nhập |
|---|---|---|---|---|---|---|
| Xem danh sách học sinh | cho phép | NOT_FOUND | FORBIDDEN | FORBIDDEN | FORBIDDEN | UNAUTHENTICATED |
| Duyệt / từ chối / cho rời | cho phép | NOT_FOUND | FORBIDDEN | FORBIDDEN | FORBIDDEN | UNAUTHENTICATED |
| Đặt lại mật khẩu em Đang học | cho phép | NOT_FOUND | FORBIDDEN | FORBIDDEN | FORBIDDEN | UNAUTHENTICATED |
| Đặt lại mật khẩu em Đã rời hoặc Chờ duyệt | NOT_FOUND | NOT_FOUND | FORBIDDEN | FORBIDDEN | FORBIDDEN | UNAUTHENTICATED |
| Đặt lại mật khẩu tài khoản giáo viên | NOT_FOUND | NOT_FOUND | FORBIDDEN | FORBIDDEN | FORBIDDEN | UNAUTHENTICATED |
| Xem bộ bài của lớp | cho phép | NOT_FOUND | cho phép (chỉ của mình) | NOT_FOUND | NOT_FOUND (bộ mới); lịch sử của mình ở Tự học | UNAUTHENTICATED |
| Xem tiến độ hay kết quả của bạn | — | — | không có đường nào (FORBIDDEN) | không có | không có | UNAUTHENTICATED |

Quy ước: sai vai thì FORBIDDEN; đúng vai nhưng ngoài quan hệ thì NOT_FOUND (QA-S3E1-04). Mỗi ô khác "cho phép" kiểm cả thân lỗi không chứa tên, mã hay số liệu của đối tượng.

## Tự động hóa và cổng kiểm

- Cột "Tự động hóa" của `test_cases.md` là đầu vào của bộ E2E (PO-50); case `IT` chạy ở tầng tích hợp với cơ sở dữ liệu thử; case `Manual` chỉ khi cần thiết bị thật (iPad, iPhone).
- AI giả lập trong CI (D-GEO-047); không gọi AI thật. Dữ liệu dùng bộ mẫu chung F-S3-nnn; khôi phục dữ liệu sau case ghi ở Remark.
- Case chặn trường dữ liệu cá nhân chạy trong cổng CI của mọi PR (không chỉ vòng kiểm thử cuối).

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: thiết kế kiểm thử S3-E1 | runner-T-261126-14 (QC) |
