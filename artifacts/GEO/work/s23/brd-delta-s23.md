Phiên bản: v0.1 · 2026-11-26
# BRD delta S23 · GEO · chặng 2–3 so với BRD MVP (02_gate2_brd.md, 22/11/2026)

Chỉ ghi phần thêm và phần đổi. Phần MVP không nhắc thì giữ nguyên. Nguồn: brief-s23.md; STAGES-AFTER-MVP.md Ch2, Ch3 (22/11); PO-61 (26/11) và PO-62…65. Mọi mục TẠM chờ owner ở gate 16.

## 1. Giá trị và chỉ số (đo được, mỗi số có công thức F1)
| Mã | Chỉ số | Công thức | Chặng |
|---|---|---|---|
| M-S2-01 | Hình phẳng thỏa ràng buộc | số đề phẳng bộ chuẩn thỏa ràng buộc / tổng đề phẳng bộ chuẩn; ngưỡng ≥ 95 % (mock) | 2 |
| M-S2-02 | Đáp số đúng hình phẳng | số đề đáp số khớp mẫu / tổng; ngưỡng ≥ 90 % | 2 |
| M-S2-03 | Sai mang nhãn "Đã kiểm chứng" | số bài sai có nhãn; ngưỡng 0 | 2 |
| M-S2-04 | Đề giáo viên tự thêm và tỉ lệ duyệt | đề tạo qua Thêm đề; đề được duyệt / đề tạo | 2 |
| M-S2-05 | Bước lời giải bị sửa | bước có phiên bản sửa / tổng bước của đề đã duyệt | 2 |
| M-S2-06 | Phiên trình chiếu mỗi giáo viên | số phiên Trình chiếu / số giáo viên có phiên | 2 |
| M-S2-07 | Bài luyện làm hết; báo "chấm sai" đúng | học sinh xong mọi câu / học sinh mở; báo được giáo viên xác nhận đúng | 2 |
| M-S3-01 | Giáo viên giao lặp lại | giáo viên giao ≥ 2 bộ bài cho cùng một lớp | 3 |
| M-S3-02 | Học sinh mở bài | học sinh mở ít nhất một bài của bộ / học sinh đang học trong lớp | 3 |
| M-S3-03 | Mở Kết quả lớp sau khi giao | giáo viên mở C3-04 trong 7 ngày sau khi giao / giáo viên đã giao | 3 |
| M-S3-04 | Từ "Cả lớp vướng ở đâu" sang Trình chiếu | số lần bấm | 3 |
| M-S3-05 | Đặt lại mật khẩu mỗi lớp | AuditEvent đặt lại / số lớp | 3 |
| M-S3-06 | Sự cố truy cập sai dữ liệu | số lần test hoặc nhật ký ghi quyền sai; ngưỡng 0 | 3 |

## 2. Thay đổi so với STAGES (PO-61 thắng)
| # | STAGES / quyết định cũ | Nay | Hệ quả hiển thị (M4) |
|---|---|---|---|
| 1 | Giáo viên vào bằng lời mời, `TeacherInvite` (Ch2 §2, §4, §8; PO-05) | Bỏ. Tự đăng ký bằng tên hiển thị + vai (PO-62) | Màn đăng ký hiện mã người dùng và mã khôi phục một lần |
| 2 | C3-01 đăng ký và xác nhận giáo viên, `TeacherVerification`, "chờ xác nhận" (Ch3 §5, §7, §8) | Bỏ. Dùng chung màn đăng ký cho hai vai (S2-E1) | Không có trạng thái chờ xác nhận |
| 3 | Chưa xác nhận không giao bài, không xem lời giải Kho chung (PO-18) | Bỏ (PO-63, Q3 TẠM) | Lời giải Kho chung mở theo giờ như học sinh; tần suất tạo tài khoản và thêm đề có giới hạn |
| 4 | `ConsentRecord`, đồng thuận cha mẹ (PO-21) | Cờ "đã đọc thông báo riêng tư" + phiên bản văn bản (PO-64) | Thông báo riêng tư hiện lần đầu vào ứng dụng, không hỏi danh tính |
| 5 | Trùng tên trong lớp: hỏi ký hiệu phân biệt (Ch3 §5) | Phân biệt bằng mã người dùng | Danh sách lớp hiện tên + mã |
| 6 | Quên mật khẩu: mã đặt lại dùng một lần | Giáo viên đặt lại cho học sinh lớp mình; giáo viên dùng mã khôi phục (TẠM, PO-61) | Mất cả mật khẩu lẫn mã khôi phục: không lấy lại, nói rõ lúc đăng ký |
| 7 | Tư vấn pháp lý tài khoản trẻ em (Ch3 §9) | Thu hẹp: chính sách lưu và xóa dữ liệu bài làm (Q8) | Nút xóa tài khoản |
| 8 | Học sinh "tên đăng nhập" (Ch3 §7) | Mã người dùng sinh tự động | Học sinh chép lại mã khi đăng ký |

## 3. Yêu cầu nghiệp vụ mới (BR) và MoSCoW
| BR | Yêu cầu | MoSCoW | Epic |
|---|---|---|---|
| BR-S2-01 | Đăng ký tên + vai, mã người dùng duy nhất, đăng nhập mã + mật khẩu, khóa 10 lần sai / 15 phút theo tài khoản | Must | S2-E1 |
| BR-S2-02 | Khung môn học có `grade` và `concepts`; hình phẳng dựng từ ràng buộc, có `Problem.kind`, `SceneSpec` 2D | Must | S2-E2 |
| BR-S2-03 | Thêm đề từ chữ hoặc ảnh qua 4 bước; ngoại lệ ảnh mờ, engine không dựng, lệch đáp số | Must | S2-E3 |
| BR-S2-04 | Chat chỉnh nháp, import docx/pdf, đối chiếu trùng kho | Should (P1, Q5) | S2-E4 |
| BR-S2-05 | Sửa lời giải từng bước có phiên bản; `StepCheck`; nhãn hai trục; hộp duyệt; lời giải đã giao bất biến | Must | S2-E5 |
| BR-S2-06 | Ngăn Của tôi / Kho chung, vai người soát nội dung, lọc loại hình | Must | S2-E6 |
| BR-S2-07 | Trình chiếu toàn màn hình, chạy khi mất mạng, hỏi lại khi lời giải còn khóa | Must | S2-E7 |
| BR-S2-08 | Bài luyện điền đáp số, chấm tương đương, lượt thử, không gọi là "điểm" | Must | S2-E8 |
| BR-S2-09 | Biến thể đề, xuất ảnh/PDF, liên kết chỉ xem, trắc nghiệm | Could (P1); thiết diện động Won't (Q7) | S2-E9 |
| BR-S3-01 | Nhiều lớp, mã lớp, trang in, vào lớp chờ duyệt, giáo viên đặt lại mật khẩu học sinh | Must | S3-E1 |
| BR-S3-02 | Bộ bài, hạn làm chỉ là nhãn, lịch mở lời giải theo bộ hoặc bài, cảnh báo đề "Cần xem lại" | Must | S3-E2 |
| BR-S3-03 | Bảng lớp × bài không cột tổng; "Cả lớp vướng ở đâu" có ngưỡng 5 bài (Q6) | Must | S3-E3 |
| BR-S3-04 | Tổng quan giáo viên web + điện thoại, Mở ngay, Đổi giờ, thông báo trong ứng dụng | Must | S3-E4 |
| BR-S3-05 | `AuditEvent`, thông báo riêng tư, danh sách trường dữ liệu được phép, công tắc AI theo lớp | Must | S3-E5 |
| BR-S3-06 | Chữa bài từ kết quả, xuất kết quả lớp, lưu trữ lớp, nhắc việc trong ứng dụng | Should (P1) | S3-E6 |

## 4. NFR và tác động hệ thống
- Riêng tư: chỉ thu tên hiển thị, mã người dùng, vai, lớp, bài làm. Không email, điện thoại, ngày sinh, trường, ảnh. Test chặn trường mới ngoài danh sách (S3-E5).
- Quyền theo quan hệ: giáo viên chỉ thấy học sinh của lớp mình; học sinh không thấy kết quả bạn; hết quan hệ thì hết quyền (0.3 ý 5).
- Một cổng công bố: `release_at` mở rộng cho bài luyện và bộ bài, kiểm ở máy chủ, bộ câu thử moi đáp án chạy lại cho S2-E8 và S3-E2.
- Bằng chứng đi kèm mọi con số ở Kết quả lớp (0.3 ý 6); dưới ngưỡng thì chỉ hiện dữ liệu thô.
- Thiết bị: giáo viên web (điện thoại chỉ liếc và quyết nhanh); học sinh iPad chính, iPhone phụ; học sinh dùng web PWA (Q9 TẠM).
- AI: giữ mock trong CI (D-GEO-047); đọc ảnh bằng Claude vision (Q4 TẠM).
- Tác động module: `iam` (S2-E1, S3-E1, S3-E5), `geometry` (S2-E2), `aigateway` (S2-E3, S2-E4), `catalog`/kho đề (S2-E6), `learning`/lớp (S3-*). Tên module chính thức do PLAN task 17 chốt.
- Phụ thuộc: S2-E2 trước S2-E5 và S2-E8; S2-E5 trước S2-E3 và S2-E6; S3-E2 trước S3-E3 và S3-E4; S2-E1 trước mọi epic cần đăng nhập.

## 5. Rủi ro còn lại
| Rủi ro | Cách giảm | Chủ |
|---|---|---|
| Người ngoài giả làm giáo viên để xem lời giải Kho chung (PO-63) | Giới hạn tần suất; bộ câu thử "giả làm giáo viên"; owner xem lại ở Q3 | Owner |
| Mất mật khẩu và mã khôi phục của giáo viên | Nói rõ lúc đăng ký; không có đường lấy lại (TẠM) | Owner |
| Kho chung không ai soát | Owner tạm soát (Q9) | Owner |
| Hình phẳng đa dạng hơn bộ chuẩn | Dạng ngoài danh sách đi nhánh "Cần xem lại" | PO + BA |

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo BRD delta chặng 2–3 áp PO-61 | runner-T-261126-08 (PO) |
