# Known gaps and decisions in force — GEO (copied into the test plan §6 / §7 before each round)

> Cột "Needed by" ghi cổng (không ghi ngày). "Primary flow?" Có nghĩa là chặn R3 nếu còn mở tới lúc đó.

## Known gaps (M12)
| ID (DQ / QF / QA / D) | Item | Where it sits | Primary flow? | Owner | Needed by | Provisional behaviour in force | Cases marked PROVISIONAL |
|---|---|---|---|---|---|---|---|
| KG-01 (D-GEO-008) | Danh sách công cụ hình đầy đủ của bản cũ ("giữ toàn bộ công cụ hiện có"): `SPEC.md` chưa được đọc | E3, `01_understanding.md` C-11 | Không (BA đọc trước khi viết AC) | BA | trước AC của E3 | dùng bộ công cụ ở `mvp-05` và PO-35 | TC của E3 chưa viết |
| KG-02 (D-GEO-030) | Tệp `Giao-an-Hinh-khong-gian-11A1.docx` chưa mở; nội dung 7 bài giảng lấy từ prototype README | E4 US-GEO-412 | Không | BA | trước AC của E4 | dùng mô tả của prototype README | TC của E4 chưa viết |
| KG-03 (D-GEO-001) | Ảnh `mvp-01`, `mvp-06`, `mvp-07`, `mvp-08` chưa được PO xem | E5, E4, E3 | Không | BA | trước AC mỗi epic liên quan | dùng mô tả `MVP.md` §3 | — |
| KG-04 (D-GEO-065) | Số câu và nhóm kỹ thuật tối thiểu của bộ câu thử moi đáp án | E6 US-GEO-610, 611 | Có (khóa lời giải là tiêu chí bắt buộc) | QC đề xuất, PO phán quyết | R3 của E6 | tối thiểu gồm ca trực tiếp, đổi cách diễn đạt, yêu cầu từng bước, dán lại đề đang khóa | TC của E6 chưa viết |
| KG-05 (D-GEO-055) | Người soát nội dung Toán của kho đề (Q-CDT-11) | E5 | Không | ChuDauTu | R3 của E5 | chỉ nhãn "AI soạn" | — |
| KG-06 (D-GEO-045) | Ngưỡng hiệu năng là đề xuất (Q-CDT-01) | E1 US-GEO-107 | Không | ChuDauTu | R3 của E1 | dùng ngưỡng đề xuất | TC thử tải PROVISIONAL |
| KG-07 (D-GEO-047) | Đơn vị tiền tệ và nguồn chi phí AI thật | M-GEO-17 | Không | ChuDauTu | R3 của E1 | số mô phỏng của nhà cung cấp giả lập | — |
| KG-08 (D-GEO-067) | Chính sách lưu giữ, xóa, xuất dữ liệu trẻ em (Q-CDT-16) | E1 | Không | ChuDauTu | trước khi mở rộng ngoài MVP | AC không viết hành vi xóa hay xuất | — |
| KG-09 (D-GEO-068) | Phiên bản iOS và trình duyệt tối thiểu (Q-TL-05) | E3 US-GEO-306 | Không | TechLead | R3 của E3 | iPad, iPhone mới nhất dùng được | TC thiết bị PROVISIONAL |
| KG-10 | Yêu cầu sao lưu, khả dụng, truy cập khẩn cấp: không có nguồn trong MVP | NFR | Không | ARCH nêu, TechLead trả lời | R3 của E1 | không đặt yêu cầu | — |
| KG-20 (D-GEO-149) | Giả mạo tên bạn cùng lớp khi đăng nhập nhẹ (tên hiển thị + mã lớp): rủi ro đã biết của MVP, chấp nhận tạm vì chưa thu định danh trẻ em (PO-21) | E1 US-GEO-101 | Không | ChuDauTu | trước khi mở rộng ngoài MVP | không có AC chặn | — |
| KG-21 (D-GEO-141) | Kho đề mới có 6 đề, tiêu chí MVP cần ít nhất 40 đề đã kiểm chứng; soạn thêm từ 34 đề trở lên là việc nội dung cần người soát, không giao cho agent viết mã | E5 US-GEO-504-SC4 | Có (tiêu chí MVP) | ChuDauTu (người soát nội dung Toán) | cổng phát hành MVP | SC4 báo kho chưa đạt tiêu chí | case của 504-SC4 |
| KG-22 (D-GEO-137) | Bảy bài giảng "Giáo án mẫu" lấy nội dung từ bản mẫu tương tác, mang cờ "chưa có người soát"; người soát chưa xác định; cờ hiện cho học sinh và giáo viên (D-GEO-181) | E4 US-GEO-412 | Không | GiaoVienCoVan | trước khi mở rộng ngoài MVP | dùng nội dung bản mẫu | case của 412-SC1 PROVISIONAL |
| KG-23 (D-GEO-150, 133) | Phép khớp chữ ký chuẩn (hình mẫu, tham số quy về a = 1, đại lượng đề hỏi) và nơi lưu chưa có thiết kế; F-GEO-226 là ca chặn của cổng phát hành | E4 US-GEO-406; E6 US-GEO-610-SC5 | Có (khóa lời giải) | ARCH thiết kế, PO duyệt | trước khi chia việc module `content` (tự học) | AC theo D-GEO-150 | case của 406, 610-SC5 PROVISIONAL |
| KG-24 (D-GEO-152) | Chỗ lưu băm mã giáo viên, thời lượng hạn dùng, cách thu hồi, bộ đếm thử sai theo IP và theo tài khoản (màn chỉ có ô mã) | E1 US-GEO-102 | Có (đăng nhập giáo viên) | ARCH (PO đã phán D-GEO-173: đếm theo IP) | trước khi chia việc module `iam` | đếm theo IP, mã đúng trong cửa sổ chặn vẫn `RATE_LIMITED` (D-GEO-173) | case của 102 PROVISIONAL |

## Decisions in force (M14)
| Decision | Source (CR / QA / D, date, who) | Affected stories / cases |
|---|---|---|
| Final của chủ đầu tư: D-GEO-001…010, 012…016, 031 | `03_decisions.md` mục A (22-11-2026, chủ đầu tư) | toàn bộ |
| Đa trường là cách ly dữ liệu theo `school_id`, không màn quản trị trường | D-GEO-050 (Assumption PO) | US-GEO-104 và mọi truy vấn |
| Giờ mở: mở khi giờ máy chủ ≥ `release_at`; đã mở không khóa lại; chưa đặt thì khóa | D-GEO-041, 042, 043 (Assumption PO) | US-GEO-604, 605, 608, 612 |
| Nhãn "Đã kiểm chứng" chỉ do engine, dung sai theo D-GEO-040 | D-GEO-004 (Final), D-GEO-040, 061 | US-GEO-203, 105 |
| Soạn, import, giáo trình riêng, giao bộ đề ngoài MVP | D-GEO-011, 035 | REQ-GEO-52, 53 |
| Điều khiển khung thiết kế không nguồn không xây, không hiện | D-GEO-078, 112, 164 | US-GEO-503, 602, 604, 606, 701 |
| Tự học kế thừa khóa theo chữ ký chuẩn, kiểm ở mỗi lần xin Gợi ý 2 hoặc lời giải; bài kế thừa khóa dùng gợi ý của đề kho | D-GEO-150, 151, 155 (Assumption PO) | US-GEO-404, 405, 406, 610 |
| Mã giáo viên theo tài khoản, ≥ 12 ký tự base32, lưu băm, thu hồi được, có hạn dùng; 10 lần sai trong 15 phút → `RATE_LIMITED` | D-GEO-152 (Assumption PO) | US-GEO-101-FC5, 102, 610-SC6 |
| Giáo viên chỉ đọc lời giải đề kho; xem trước không tải lời giải; tọa độ hình là dữ kiện của đề | D-GEO-153, 154 (Assumption PO) | US-GEO-104, 305, 503, 607, 608 |
| Lưu câu trả lời hỏi đáp đã qua Kiểm soát đáp án, chỉ người hỏi đọc | D-GEO-159 (Assumption PO) | US-GEO-104-FC9, 409, 410 |
| Mã lỗi `CONFLICT`, `RATE_LIMITED`, `SERVICE_OVERLOADED`; `Cache-Control: no-store` cho lời giải, gợi ý, trạng thái khóa | D-GEO-143, 145, 161 | US-GEO-102, 107, 404, 602, 605, 608, 612 |
