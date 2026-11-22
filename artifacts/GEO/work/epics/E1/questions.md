# E1 — open questions (each with its default)

> PO gieo từ `work/questions_for_stakeholders.md` (bản gộp là nguồn duy nhất). BA, QC, ARCH thêm `QA-GEO-nn` theo dải id ở `board/CONVENTIONS.md` §2.

| Q id | Decision ref | Question | Default in use | Owner | Status |
|---|---|---|---|---|---|
| Q-CDT-01 | D-GEO-045 | Các ngưỡng p95 đọc ≤ 300 ms, ghi ≤ 500 ms, lỗi < 0,5%, đỉnh 1000 học sinh cùng mở lời giải có đúng ý anh/chị không, hay đổi? | A. Đề xuất, chưa phải ngưỡng chốt | ChuDauTu | Open |
| Q-CDT-02 | D-GEO-047 | MVP dùng nhà cung cấp AI giả lập (nội dung soạn sẵn) thì có chấp nhận được không, và nhà cung cấp thật chọn khi nào? | A. Chỉ số tốc độ và độ đúng thật chưa đo được | ChuDauTu | Open |
| Q-CDT-03 | D-GEO-048 | Đăng nhập nhẹ bằng tên hiển thị và mã lớp có đủ cho MVP không (ai biết mã lớp đều vào được lớp)? | A. Không thu email hay số điện thoại | ChuDauTu | Open |
| Q-CDT-04 | D-GEO-050 | Hồ sơ cũ để trống `org_id` còn anh/chị yêu cầu đa trường từ đầu: trong MVP chỉ cần cách ly dữ liệu theo trường, không có màn quản trị trường, đúng không? | A | ChuDauTu | Open |
| Q-CDT-13 | D-GEO-073 | Nguồn chỉ nói "vào vai giáo viên bằng tài khoản mẫu". Nếu chọn vai không mật khẩu thì học sinh nào cũng thành giáo viên và đổi được giờ mở. Dùng một mã riêng do anh/chị cấp được không? | A. PROVISIONAL | ChuDauTu | Open |
| Q-CDT-16 | D-GEO-067 | Pháp nhân vận hành, quy định áp dụng, nơi lưu, chính sách lưu giữ, xóa, đồng thuận đều chưa xác định. MVP chỉ thu tên hiển thị và mã lớp và không viết hành vi xóa hay xuất dữ liệu. Có đúng không? | A. TBD — owner ChuDauTu | ChuDauTu | Open |
| Q-TL-01 | D-GEO-046 | PostgreSQL (cổng 54329) và Redis (cổng 6389) cục bộ làm nền; môi trường chạy thử và vận hành nơi nào? | A | TechLead | Open |
| Q-TL-02 | D-GEO-049 | Quy mô 50 trường, 600 lớp, 20.000 học sinh, 2 triệu sự kiện có đủ không? Nơi chạy và công cụ thử tải? | A | TechLead | Open |
| Q-TL-03 | D-GEO-044 | Phiên đang hoạt động là có yêu cầu trong 60 giây gần nhất; cửa sổ đỉnh mở lời giải 10 giây (1000 / 10 = 100 yêu cầu/giây). Đúng cách đo không? | A | TechLead | Open |
| Q-TL-05 | D-GEO-068 | Java 26 cài sẵn; khung, công cụ build, thư viện chọn gì? Phiên bản iOS và trình duyệt tối thiểu cho iPad, iPhone? | Chưa chọn; không ảnh hưởng AC | TechLead | Open |

## Phần BA (QA-GEO-01…39)

| QA id | Hỏi ai | Câu hỏi | Mặc định đang dùng trong AC | Trạng thái |
|---|---|---|---|---|
| QA-GEO-01 | ARCH (M-204) | Giới hạn lấy từ lược đồ đề xuất: tên hiển thị 1…60 ký tự, mã lớp 6 ký tự, đề 1…8000 ký tự, câu hỏi 1…1000 ký tự, thời hạn thẻ | dùng đúng các số của lược đồ | Open |
| QA-GEO-05 | ARCH | Mã lỗi cho tài nguyên ngoài lớp, ngoài trường | `NOT_FOUND`; `FORBIDDEN` chỉ khi thấy được nhưng không được làm | Answered (ARCH M-402 b, trạng thái đề xuất) |
| QA-GEO-06 | PO | Hai lần vào cùng tên trong một lớp | cùng một học sinh | Ruled D-GEO-106 |
| QA-GEO-07 | PO | Giới hạn thử sai mã giáo viên | không có; chỉ giới hạn tốc độ chung | Ruled D-GEO-107; với đăng nhập giáo viên đảo bởi D-GEO-152 (10 lần sai trong 15 phút → `RATE_LIMITED`) |
| QA-GEO-20 | ARCH (M-204) | Thời gian chờ tối đa và thử lại tự động của cổng AI (D-GEO-072) | không tự thử lại; chỉ nút "Thử lại" | Open |
| QA-GEO-21 | ARCH → PO (M-204) | NFR do ARCH đề xuất thêm, không có REQ (NFR-GEO-005, 006, 009…012, 043, 048, 051, 052, 055…058) | không viết thành AC có ngưỡng; riêng 107-FC2, 703-FC2, FC3, 704-FC2 PROVISIONAL | Ruled D-GEO-125 (yêu cầu kỹ thuật MVP, ngưỡng giữ nhãn đề xuất) |
