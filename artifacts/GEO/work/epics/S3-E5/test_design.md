Phiên bản: v0.1 · 2026-11-26
# S3-E5 — thiết kế kiểm thử (Nhật ký thao tác nhạy cảm (AuditEvent), thông báo riêng tư, danh sách trường dữ liệu được phép, công tắc AI theo lớp)

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
| CL-FN-01 | US-S3E5-01…04 | Y |  |
| CL-FN-02 | US-S3E5-02 (phiên bản), 03 (tên hiển thị, trường thừa), 04 (giá trị công tắc) | Y |  |
| CL-FN-03 | US-S3E5-02, 04 | Y |  |
| CL-FN-04 | US-S3E5-02 (cờ chưa đọc, đã đọc, bản mới), 04 (bật, tắt) | Y |  |
| CL-FN-05 | US-S3E5-01, 02, 04 | Y |  |
| CL-FN-06 | US-S3E5-01, 02, 04 | Y |  |
| CL-FN-07 | US-S3E5-01 (không mật khẩu), 02, 03 | Y |  |
| CL-FN-08 | — | N | nhật ký không phân trang ở P0 (khối lượng nhỏ) |
| CL-FN-09 | US-S3E5-01 (giờ máy chủ) | Y |  |
| CL-FN-10 | US-S3E5-01 (khóa chống lặp), 04 (tắt giữa lượt) | Y |  |
| CL-FN-11 | — | N | không nhãn máy |
| CL-FN-12 | — | N | không định dạng số |
| CL-FN-13 | US-S3E5-01 | Y |  |
| CL-FN-14 | US-S3E5-04 (AI tắt, AI gộp vẫn chạy) | Y |  |
| CL-UI-01 | US-S3E5-02 (iPad, iPhone) | Y |  |
| CL-UI-02 | — | N | gộp vào CL-FN-03 |
| CL-UI-04 | — | N | hiếm |
| CL-X-01 | — | N | không tải riêng |
| CL-X-02 | US-S3E5-01 (đếm theo lớp dùng chỉ mục) | Y |  |
| CL-X-03 | US-S3E5-01 (nhật ký chỉ thêm), 04 (sửa giao diện) | Y |  |
| CL-X-04 | US-S3E5-02, 03 (cả story) | Y |  |

## Design rows: `CL id · condition · expected rule · technique · level`

| CL id | Story | Condition | Expected rule (nguồn) | Technique | Level |
|---|---|---|---|---|---|
| CL-FN-13 | 01 | Mở ngay, Đổi giờ, Đặt lại mật khẩu | ba loại AuditEvent đủ trường, đúng người, đúng giờ máy chủ | EP | tích hợp API |
| CL-FN-07 | 01 | mật khẩu tạm có trong nhật ký, bản ghi, thân phản hồi? | không ở đâu cả | soát | tích hợp API, CSDL |
| CL-FN-10 | 01 | ghi AuditEvent lỗi giữa chừng | thao tác không xảy ra (đóng kín), mật khẩu không đổi | đoán lỗi, tiêm lỗi | tích hợp CSDL |
| CL-X-03 | 01 | UPDATE, DELETE nhật ký | API không có đường; CSDL từ chối | đoán lỗi | tích hợp CSDL |
| CL-FN-04 | 01 | đổi giờ lúc Nháp và sau khi giao | chỉ lần sau khi giao có bản ghi | bảng chuyển trạng thái | tích hợp API |
| CL-FN-05 | 01 | từ chối vì quyền hoặc quan hệ | sự kiện Từ chối, không AuditEvent thành công (ma trận bên dưới) | bảng quyết định | tích hợp API |
| CL-FN-06 | 01 | chủ lớp đọc; giáo viên khác; học sinh | đúng 3 và 1; NOT_FOUND; FORBIDDEN | bảng quyết định | tích hợp API |
| CL-X-02 | 01 | đếm PASSWORD_RESET theo lớp | 4, 1, 0; dùng chỉ mục | soát | tích hợp CSDL |
| CL-FN-01 | 02 | đăng ký có và không tick; tài khoản MVP; bản 1.0 lên 1.1 | cờ và phiên bản đúng; hiện lại một lần; không hiện khi đã đọc | EP, bảng trạng thái | E2E trình duyệt |
| CL-X-04 | 02 | bản ghi cờ | chỉ mã người dùng, phiên bản, giờ | soát | tích hợp CSDL |
| CL-FN-02 | 02 | thiết bị gửi phiên bản "9.9", "1.0" | VALIDATION_FAILED; máy chủ tự ghi bản hiện hành | đoán lỗi | tích hợp API |
| CL-FN-05 | 02 | chưa đọc mà gọi API khác | FORBIDDEN kèm details | EP | tích hợp API |
| CL-X-04 | 03 | quét cột, khóa phản hồi, khóa yêu cầu, sự kiện đo, bảng xuất | 0 ngoài danh sách; số mục quét in ra | soát toàn bộ | tích hợp CSDL, bộ quét |
| CL-X-04 | 03 | migration giả thêm `contact_note`, DTO giả thêm `parentName` | test đỏ, nêu đúng tên | kiểm thử kiểm thử | bộ quét |
| CL-FN-02 | 03 | tên hiển thị: email, 8 và 9 chữ số, "Minh 10A", có dấu | chặn email và 9 chữ số; nhận 8 chữ số và các tên còn lại (bảng biên bên dưới) | BVA, EP | tích hợp API |
| CL-FN-02 | 03 | khóa cấm mọi dạng viết và trường thừa | VALIDATION_FAILED; không lưu | EP | tích hợp API, bộ quét |
| CL-X-04 | 03 | `org_id`, IP, thiết bị | không có trong phản hồi hay bảng ứng dụng | soát | tích hợp CSDL |
| CL-FN-14 | 04 | tắt công tắc; gọi trợ lý; luồng cơ bản; bật lại; hai lớp; Tự học | khung ẩn; FORBIDDEN, 0 lượt gọi nhà cung cấp; luồng chạy; có hiệu lực lượt kế | bảng quyết định | E2E trình duyệt, tích hợp API |
| CL-X-03 | 04 | sửa giao diện hiện lại nút; giá trị công tắc sai kiểu | máy chủ vẫn chặn; VALIDATION_FAILED | đoán lỗi | tích hợp API |
| CL-FN-10 | 04 | tắt giữa lúc em đang hỏi | lượt đang chạy hoàn tất, lượt sau chặn, không mất bài | đồng thời | tích hợp API |

## Ma trận AuditEvent và sự kiện từ chối (US-S3E5-01)
| Thao tác | Người gọi | Kết quả | Bản ghi AuditEvent | Sự kiện bảo mật (Từ chối) |
|---|---|---|---|---|
| Mở ngay, bộ Sắp mở của lớp mình | Cô Lan | thành công | EARLY_RELEASE (trước, sau) | không |
| Mở ngay, bộ đã mở | Cô Lan | CONFLICT | không | không |
| Mở ngay, bộ lớp khác | Thầy Khôi | NOT_FOUND | không | có |
| Đổi giờ, bộ Nháp | Cô Lan | thành công | không (BR-S3E5-06) | không |
| Đổi giờ, bộ Đã giao Sắp mở | Cô Lan | thành công | RELEASE_TIME_CHANGE (trước, sau) | không |
| Đặt lại mật khẩu em Đang học | Cô Lan | thành công | PASSWORD_RESET | không |
| Đặt lại mật khẩu lần 11 trong giờ | Cô Lan | RATE_LIMITED | không | có |
| Đặt lại mật khẩu em ngoài lớp | Thầy Khôi | NOT_FOUND | không | có |
| Bất kỳ thao tác nào, ghi nhật ký lỗi | bất kỳ | lỗi máy chủ | không | không; thao tác không xảy ra |

## Bảng biên tên hiển thị (BR-S3E5-35)
| Giá trị | Kết quả | Lý do |
|---|---|---|
| "mai@example.org" | từ chối | giống email |
| "0912 345 678" | từ chối | 9 chữ số sau khi bỏ khoảng trắng |
| "0912345678" | từ chối | 10 chữ số |
| "Lớp 12345678" | nhận | 8 chữ số |
| "Minh 10A" | nhận | số ngắn |
| "Nguyễn Thị Ánh Tuyết" | nhận | có dấu |
| "Đức-Huy (11A1)" | nhận | ký tự thường gặp |
| "=1+1" | nhận (xuất tệp thì vô hiệu công thức, US-S3E6-02) | không phải email hay điện thoại |
| rỗng, 61 ký tự | từ chối | độ dài tên hiển thị 1…60 (MVP BR-GEO-102) |

## Danh sách tên trường cấm cho bộ quét (BR-S3E5-33)
email, e-mail, mail, phone, phoneNumber, so_dien_thoai, sdt, mobile, dob, birthday, birth, ngay_sinh, address, dia_chi, school, school_name, truong, avatar, avatarUrl, photo, image, anh_dai_dien, cmnd, cccd, ip, ipAddress, device, deviceId. So khớp không phân biệt hoa thường, gạch dưới, gạch ngang và dạng camelCase.

## Phân loại dữ liệu phải soát
| Nơi | Cách soát | Case |
|---|---|---|
| Cột cơ sở dữ liệu | quét schema sau migration | SC1, SC2 của US-S3E5-03 |
| Khóa phản hồi API | gọi mọi API bằng dữ liệu mẫu chung | SC1 |
| Khóa thân yêu cầu | bộ quét thân yêu cầu hợp lệ và thân cố ý thừa | SC3, FC4 |
| Nhật ký ứng dụng | quét mẫu email và số điện thoại sau bộ E2E | SC6 |
| Bảng sao lưu, đo, xuất | quét như cột | FC6 |
| Nhật ký truy cập máy chủ web | cấu hình giữ không quá 7 ngày (đề xuất) | FC5 |

## Tự động hóa và cổng kiểm

- Cột "Tự động hóa" của `test_cases.md` là đầu vào của bộ E2E (PO-50); case `IT` chạy ở tầng tích hợp với cơ sở dữ liệu thử; case `Manual` chỉ khi cần thiết bị thật (iPad, iPhone).
- AI giả lập trong CI (D-GEO-047); không gọi AI thật. Dữ liệu dùng bộ mẫu chung F-S3-nnn; khôi phục dữ liệu sau case ghi ở Remark.
- Case chặn trường dữ liệu cá nhân chạy trong cổng CI của mọi PR (không chỉ vòng kiểm thử cuối).

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: thiết kế kiểm thử S3-E5 | runner-T-261126-14 (QC) |
