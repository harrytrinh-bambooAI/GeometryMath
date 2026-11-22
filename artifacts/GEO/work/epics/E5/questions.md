# E5 — open questions (each with its default)

> PO gieo từ `work/questions_for_stakeholders.md` (bản gộp là nguồn duy nhất). BA, QC, ARCH thêm `QA-GEO-nn` theo dải id ở `board/CONVENTIONS.md` §2.

| Q id | Decision ref | Question | Default in use | Owner | Status |
|---|---|---|---|---|---|
| Q-CDT-11 | D-GEO-055 | Kho 40 đến 60 đề, ít nhất 40 đề đã kiểm chứng; ai soát nội dung Toán trước khi học sinh thật dùng, và có nguồn đề ưu tiên (sách giáo khoa, đề thi) không? | Chưa có người soát; chỉ nhãn "AI soạn" | ChuDauTu | Open |
| Q-CDT-12 | D-GEO-020, D-GEO-028, D-GEO-079 | Dùng hai trục nhãn (máy: "Đã kiểm chứng" / "Cần xem lại"; người: "AI soạn" / "Giáo viên đã duyệt"); MVP chỉ có "AI soạn"; thiết kế ghi lẫn "Hình đã kiểm chứng", dùng một chuỗi "Đã kiểm chứng". Đồng ý không? | A | ChuDauTu | Open |
| Q-GV-07 | D-GEO-070 | Lọc theo cấp (THCS, THPT); chủ đề (Góc, Khoảng cách, Thể tích, Diện tích, Mặt cầu theo tài liệu mẫu); loại hình; "dạng bài" hiểu là chủ đề. Có thiếu chủ đề nào không? | A | GiaoVienCoVan | Open |

## Phần BA (QA-GEO-01…39)

| QA id | Hỏi ai | Câu hỏi | Mặc định đang dùng trong AC | Trạng thái |
|---|---|---|---|---|
| QA-GEO-04 | ARCH (M-204) | Kích thước trang và trần của từng danh sách | Kho đề 20 = trần; chờ `04_query_catalogue.md` | Ruled D-GEO-142, 168 (Kho đề trang 20, trần 50; trần chung 100); trang của danh sách khác theo `architecture/04_query_catalogue.md` |
| QA-GEO-25 | PO | Dòng "{n} đề · {m} đã kiểm chứng" | từ bộ đếm M-GEO-07 | Ruled D-GEO-122 |
| QA-GEO-27 | PO (M-214) | Học sinh có gọi được Kho đề, chi tiết đề không | không, `FORBIDDEN` | Ruled D-GEO-131 |
