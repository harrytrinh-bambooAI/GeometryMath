# E4 — open questions (each with its default)

> PO gieo từ `work/questions_for_stakeholders.md` (bản gộp là nguồn duy nhất). BA, QC, ARCH thêm `QA-GEO-nn` theo dải id ở `board/CONVENTIONS.md` §2.

| Q id | Decision ref | Question | Default in use | Owner | Status |
|---|---|---|---|---|---|
| Q-CDT-07 | D-GEO-029 | Bài giảng AI viết chưa duyệt có hiện cho học sinh (kèm nhãn "AI soạn · chờ giáo viên duyệt") không? Điểm này chạm nguyên tắc chỉ nội dung đã duyệt tới học sinh. | A (PO-43) | ChuDauTu | Open |
| Q-CDT-08 | D-GEO-052, D-GEO-026 | Học sinh tự học phải đã vào bằng mã lớp; đề dán trùng bài đang khóa thì kế thừa khóa của bài đó. Đúng ý không? | A | ChuDauTu | Open |
| Q-CDT-10 | D-GEO-023 | Bài tự học mở lời giải sau khi học sinh xem Gợi ý 1 và 2. Giữ không? | A | ChuDauTu | Open |
| Q-CDT-14 | D-GEO-030 | PO-49 đặt chúng trong MVP ở mức bài giảng và giáo trình mẫu do hệ thống cung cấp; hồ sơ domain không xếp chức năng này vào epic nào nên PO xếp vào E4. Đồng ý không? | A | ChuDauTu | Open |
| Q-CDT-15 | D-GEO-078 | Khung vẽ "Trình chiếu", "Sửa (sắp có)", "Sau buổi học tới", "Nên nhắc lại trên lớp", thẻ chuỗi ngày, phần trăm theo dạng (có "Thiết diện"), mục "Lịch sử". PO đề xuất không xây phía sau, ẩn hoặc gắn nhãn Concept. Đồng ý không? | A | ChuDauTu | Open |
| Q-GV-06 | D-GEO-027 | Trước giờ mở, trợ lý nêu công thức và phương pháp tổng quát, dẫn tới bài giảng; không nêu đáp số hay các bước của bài đang làm; khi từ chối thì nhắc lại gợi ý đang có, không tính thêm lượt gợi ý. Đồng ý không? | A (PO-22, 37, 44) | GiaoVienCoVan | Open |
| Q-TL-06 | D-GEO-072, D-GEO-074 | Khi cổng AI lỗi hoặc quá thời gian: báo lỗi dễ hiểu, cho thử lại (thời gian chờ tối đa?). Trang đang mở cập nhật khóa không muộn hơn 10 giây sau giờ mở. Đồng ý và cơ chế nào? | A | TechLead | Open |

## Phần BA (QA-GEO-01…39)

| QA id | Hỏi ai | Câu hỏi | Mặc định đang dùng trong AC | Trạng thái |
|---|---|---|---|---|
| QA-GEO-08 | PO | Thứ tự gợi ý | Gợi ý 1 trước Gợi ý 2 | Ruled D-GEO-108 |
| QA-GEO-10 | ARCH → PO (M-204) | Quy tắc "gần trùng" của D-GEO-026 | đề đổi tên điểm cũng kế thừa khóa (406-SC5) | Ruled D-GEO-150 (chữ ký chuẩn), 151 (kiểm ở mỗi lần xin) |
| QA-GEO-11 | PO | Nhập ảnh: bước xem và sửa chữ; ảnh > 1 MB | có bước sửa; từ chối > 1 MB | Ruled D-GEO-110 |
| QA-GEO-12 | PO | Nhãn người của bài giảng "Giáo án mẫu" | chỉ dòng nguồn | Ruled D-GEO-111 |
| QA-GEO-13 | PO | Điều khiển trên khung không có REQ | không xây, không hiện | Ruled D-GEO-112 |
| QA-GEO-32 | PO (M-214) | Màn khám phá hình mẫu có gợi ý, lời giải, hỏi đáp không | không | Ruled D-GEO-136 |
| QA-GEO-33 | PO (M-214) | Nội dung chữ của 7 bài giảng "Giáo án mẫu": tệp docx không có; ai soạn (KG-02) | chỉ kiểm cấu trúc, tên theo prototype | Ruled D-GEO-137 (nội dung lấy từ bản mẫu, cờ "chưa có người soát"; người soát còn mở ở KG-02, KG-22) |
