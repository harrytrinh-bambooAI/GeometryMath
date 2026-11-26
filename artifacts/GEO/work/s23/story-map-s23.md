Phiên bản: v0.1 · 2026-11-26
# Story map S23 · GEO · S2-E1…E9 và S3-E1…E6

R2 status: **khóa (PO, 26/11/2026); chờ owner R3 ở gate 16 (T-261126-16)**. Sau khi khóa, đổi là CR (`cr_register.md`). Story sơ bộ, chi tiết AC do BA ở task 12 và 14. Quy tắc cắt: một khung = một story; story dữ liệu tách riêng; một story xuyên suốt cho quyền. ID story: `US-S2E<n>-<nn>` và `US-S3E<n>-<nn>`. Nguồn khung: STAGES-AFTER-MVP.md Ch2 §6 (C2-xx), Ch3 §6 (C3-xx); LOGIN = màn đăng ký/đăng nhập mã (task 10 thiết kế). Mức: P0 trừ khi ghi P1. Quyết định: PO-61…65.

## Chặng 2
| Epic | Story | Loại | Khung | Mức | Nguồn · quyết định |
|---|---|---|---|---|---|
| S2-E1 | US-S2E1-01 Đăng ký bằng tên hiển thị và vai, nhận mã người dùng và mã khôi phục | UI | LOGIN | P0 | PO-61, PO-62 |
| S2-E1 | US-S2E1-02 Đăng nhập bằng mã + mật khẩu; khóa 10 lần sai / 15 phút theo tài khoản | UI | LOGIN | P0 | PO-61, PO-53 |
| S2-E1 | US-S2E1-03 Giáo viên tự khôi phục mật khẩu bằng mã khôi phục (TẠM) | UI | LOGIN | P0 | PO-61 |
| S2-E1 | US-S2E1-04 Dữ liệu: chuyển tài khoản mẫu MVP sang mô hình mã người dùng | Data | (dữ liệu) | P0 | PO-61 |
| S2-E2 | US-S2E2-01 Khung môn học: `parse/solve/verify/render/hint` + `grade`, `concepts` | Tech | (không có khung) | P0 | Ch2 §8–9; 0.3 ý 1 |
| S2-E2 | US-S2E2-02 Dựng hình phẳng từ ràng buộc; `Problem.kind`; `SceneSpec` 2D | Logic | C2-05 | P0 | Ch2 §3 ý 1, §8 |
| S2-E2 | US-S2E2-03 Làm bài hình phẳng trên iPad và iPhone | UI | C2-05 | P0 | Ch2 §6 |
| S2-E2 | US-S2E2-04 Dữ liệu: bộ đề chuẩn hình phẳng và đáp số mẫu | Data | (dữ liệu) | P0 | Ch2 §9 |
| S2-E2 | US-S2E2-05 Đo `make bench-s2` (mock): ≥ 95 % thỏa ràng buộc, ≥ 90 % đáp số, 0 sai mang nhãn | Tech | (báo cáo) | P0 | M-S2-01…03 |
| S2-E3 | US-S2E3-01 Thêm đề bằng dán chữ, chép lại để sửa | UI | C2-02 | P0 | Ch2 §5 luồng A |
| S2-E3 | US-S2E3-02 Thêm đề bằng ảnh; ô chưa chắc tô để giáo viên sửa | UI | C2-02 | P0 | PO-65 (Q4) |
| S2-E3 | US-S2E3-03 Bốn bước đọc–dựng–giải–kiểm với màn chờ từng bước | Logic | C2-02 | P0 | PO-27, PO-30 |
| S2-E3 | US-S2E3-04 Ngoại lệ: engine không dựng thì lưu "Cần xem lại"; lệch đáp số thì chặn Duyệt | Logic | C2-02, C2-03 | P0 | Ch2 §5 ngoại lệ |
| S2-E4 | US-S2E4-01 Chat chỉnh nháp đề, AI hỏi lại và nói rõ trước/sau | Logic | (khung do task 10 đề xuất) | P1 | PO-45, PO-46 |
| S2-E4 | US-S2E4-02 Import docx/pdf, tách từng bài tập | Logic | (như trên) | P1 | PO-47 |
| S2-E4 | US-S2E4-03 Đối chiếu trùng với kho | Logic | (như trên) | P1 | PO-45 |
| S2-E5 | US-S2E5-01 Sửa lời giải từng bước, mỗi lần lưu thành phiên bản mới | UI | C2-03 | P0 | Ch2 §3 ý 3 |
| S2-E5 | US-S2E5-02 `StepCheck`: kiểm số từng bước, bước sai tô đỏ kèm lý do | Logic | C2-03 | P0 | Ch2 §3 ý 4 |
| S2-E5 | US-S2E5-03 Nhãn hai trục và dòng "Máy đã kiểm tra phần tính toán"; chỉ engine gắn "Đã kiểm chứng" | UI | C2-03 | P0 | 0.3 ý 2; Ch2 §7 |
| S2-E5 | US-S2E5-04 Hộp duyệt chung: nhận, sửa, bỏ | UI | C2-03 | P0 | 0.3 ý 4 |
| S2-E5 | US-S2E5-05 Lời giải đã giao bất biến; sửa tạo phiên bản mới và chọn có áp cho bài đang giao | Xuyên suốt | C2-03 | P0 | Ch2 §7 |
| S2-E6 | US-S2E6-01 Kho đề có ngăn Của tôi / Kho chung và lọc loại hình | UI | C2-01 | P0 | Ch2 §6 |
| S2-E6 | US-S2E6-02 Người soát nội dung duyệt đề vào Kho chung | UI | C2-01 | P0 | PO-65 (Q9) |
| S2-E6 | US-S2E6-03 Đề Của tôi chỉ chủ sở hữu thấy; giới hạn tần suất thêm đề | Xuyên suốt | (không có khung) | P0 | Ch2 §7; PO-63 |
| S2-E7 | US-S2E7-01 Trình chiếu toàn màn hình, hiện từng bước, nút che lời giải | UI | C2-04 | P0 | Ch2 §5 luồng B |
| S2-E7 | US-S2E7-02 Chạy được khi mất mạng; hỏi đáp AI tạm tắt kèm thông báo | UI | C2-04 | P0 | Ch2 §5 ngoại lệ |
| S2-E7 | US-S2E7-03 Hỏi lại "Lớp sẽ thấy lời giải trên màn chiếu" khi lời giải đang khóa | Logic | C2-04 | P0 | Ch2 §5 ngoại lệ |
| S2-E8 | US-S2E8-01 Bàn phím toán (căn, phân số, π) trên iPad và iPhone | UI | C2-06 | P0 | Ch2 §10 |
| S2-E8 | US-S2E8-02 Chấm đáp số dạng tương đương bằng `grade`; sai cú pháp không trừ lượt | Logic | C2-06 | P0 | Ch2 §5 luồng C |
| S2-E8 | US-S2E8-03 Làm bài luyện: lượt thử, gợi ý mức 1, "xem lại cùng thầy cô" khi hết lượt | UI | C2-06 | P0 | Ch2 §5 luồng C |
| S2-E8 | US-S2E8-04 Kết quả bài luyện; không hiện số tổng; đáp số khóa tới giờ mở | UI | C2-07 | P0 | PO-65 (Q6); Ch2 §7 |
| S2-E8 | US-S2E8-05 Soạn bài luyện; nút "Em nghĩ đáp số này đúng" gửi về giáo viên | UI | C2-08 | P0 | Ch2 §10 |
| S2-E9 | US-S2E9-01 Biến thể đề `ProblemVariant`, đáp số tính lại | Logic | (khung do task 10 đề xuất) | P1 | Ch2 §3 ý 7 |
| S2-E9 | US-S2E9-02 Xuất ảnh/PDF và liên kết chỉ xem | UI | (như trên) | P1 | Ch2 §4 P1 |
| S2-E9 | US-S2E9-03 Câu trắc nghiệm trong bài luyện | UI | C2-06 | P1 | Ch2 §4 P1 |

## Chặng 3
| Epic | Story | Loại | Khung | Mức | Nguồn · quyết định |
|---|---|---|---|---|---|
| S3-E1 | US-S3E1-01 Giáo viên tạo lớp (tên, cấp, năm học), sinh mã lớp và trang in | UI | C3-02 | P0 | Ch3 §5 luồng A |
| S3-E1 | US-S3E1-02 Học sinh nhập mã lớp, trạng thái chờ duyệt; mã sai hoặc đã khóa | UI | C3-07 | P0 | Ch3 §5 luồng A |
| S3-E1 | US-S3E1-03 Giáo viên duyệt danh sách chờ; nhiều lớp; `ClassMembership` (chờ, đang học, đã rời) | UI | C3-02 | P0 | Ch3 §8 |
| S3-E1 | US-S3E1-04 Giáo viên đặt lại mật khẩu cho học sinh lớp mình | UI | C3-02 | P0 | PO-61 |
| S3-E2 | US-S3E2-01 Soạn bộ bài: chọn đề, thứ tự, hạn làm (chỉ là nhãn), xem trước như học sinh | UI | C3-03 | P0 | Ch3 §5 luồng B |
| S3-E2 | US-S3E2-02 Lịch mở lời giải theo bộ hoặc từng bài; cảnh báo giờ mở sớm hơn hạn | Logic | C3-03 | P0 | Ch3 §5 ngoại lệ; PO-25 |
| S3-E2 | US-S3E2-03 Bài của em theo bộ; liệt kê đề "Cần xem lại" trước khi giao | UI | C3-08, C3-03 | P0 | Ch3 §5 ngoại lệ |
| S3-E3 | US-S3E3-01 Bảng lớp × bài, ô là trạng thái, không cột tổng, không sắp "tốt nhất" | UI | C3-04 | P0 | Ch3 §3 ý 5; K3 |
| S3-E3 | US-S3E3-02 Bấm một ô xem gợi ý, câu hỏi và bằng chứng; học sinh đã rời đánh dấu | UI | C3-04 | P0 | 0.3 ý 6 |
| S3-E3 | US-S3E3-03 "Cả lớp vướng ở đâu": bước nhiều em dừng nhất; dưới ngưỡng 5 bài chỉ hiện dữ liệu thô | Logic | C3-04 | P0 | PO-65 (Q6) |
| S3-E4 | US-S3E4-01 Tổng quan giáo viên web + điện thoại: bài đang giao, lời giải đang khóa, em chờ duyệt | UI | C3-05 | P0 | Ch3 §6 |
| S3-E4 | US-S3E4-02 Theo dõi nhanh trên điện thoại: em chưa làm, Mở ngay, Đổi giờ | UI | C3-06 | P0 | Ch3 §5 luồng C |
| S3-E4 | US-S3E4-03 `Notification` trong ứng dụng cho học sinh (chưa đọc, đã đọc) | Logic | C3-08 | P0 | PO-65 (Q9) |
| S3-E5 | US-S3E5-01 `AuditEvent` cho mở sớm, đổi giờ, đặt lại mật khẩu, kèm người thực hiện | Logic | (không có khung) | P0 | Ch3 §7 |
| S3-E5 | US-S3E5-02 Thông báo riêng tư, cờ "đã đọc" + phiên bản văn bản | UI | LOGIN | P0 | PO-64 |
| S3-E5 | US-S3E5-03 Danh sách trường dữ liệu được phép thu; test chặn trường mới | Xuyên suốt | (không có khung) | P0 | Ch3 §10; PO-61 |
| S3-E5 | US-S3E5-04 Công tắc AI phía học sinh theo lớp; tắt thì luồng học cơ bản vẫn chạy | Logic | C3-02 | P0 | 0.3 ý 7 |
| S3-E6 | US-S3E6-01 Chữa bài từ kết quả sang Trình chiếu | UI | C3-04, C2-04 | P1 | Ch3 §4 P1 |
| S3-E6 | US-S3E6-02 Xuất kết quả lớp; lưu trữ lớp cuối năm | UI | C3-02, C3-04 | P1 | Ch3 §4 P1 |
| S3-E6 | US-S3E6-03 Nhắc việc trong ứng dụng: bài mới, sắp hết hạn, lời giải đã mở | Logic | C3-08 | P1 | Ch3 §3 ý 8 |

## Kiểm phủ (V14)
- Khung → story: C2-01 (S2E6-01, 02), C2-02 (S2E3-01…04), C2-03 (S2E5-01…05, S2E3-04), C2-04 (S2E7-01…03), C2-05 (S2E2-02, 03), C2-06 (S2E8-01…03, S2E9-03), C2-07 (S2E8-04), C2-08 (S2E8-05), C3-02 (S3E1-01, 03, 04), C3-03 (S3E2-01…03), C3-04 (S3E3-01…03), C3-05 (S3E4-01), C3-06 (S3E4-02), C3-07 (S3E1-02), C3-08 (S3E2-03, S3E4-03), C3-01 thay bằng LOGIN (S2E1-01…03, S3E5-02): đủ 16 khung và LOGIN.
- Mục STAGES đã bỏ theo PO-61 (không có story): `TeacherInvite`, `TeacherVerification`, "chờ xác nhận", luật chặn giáo viên chưa xác nhận.
- Số story: chặng 2 = 35 (P0 29, P1 6: E4 ×3, E9 ×3); chặng 3 = 20 (P0 17, P1 3). Đã đối chiếu `epic_board.md`.
- Ưu tiên dồn công cho BA/QC (K3): C2-03, C2-06 (chặng 2); C3-04 (chặng 3).

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo story map chặng 2–3; R2 khóa (PO), chờ R3 | runner-T-261126-08 (PO) |
