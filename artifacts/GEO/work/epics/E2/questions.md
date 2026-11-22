# E2 — open questions (each with its default)

> PO gieo từ `work/questions_for_stakeholders.md` (bản gộp là nguồn duy nhất). BA, QC, ARCH thêm `QA-GEO-nn` theo dải id ở `board/CONVENTIONS.md` §2.

| Q id | Decision ref | Question | Default in use | Owner | Status |
|---|---|---|---|---|---|
| Q-GV-01 | D-GEO-040 | Một đáp số được coi là khớp khi sai số tương đối ≤ 1e-4 so với giá trị tính từ tọa độ, thử với ít nhất 3 bộ giá trị tham số; ràng buộc hình khớp khi sai số tuyệt đối ≤ 1e-6. Có hợp lý về mặt Toán không? | A. Không có nguồn số, do PO đặt | GiaoVienCoVan | Open |
| Q-CDT-12 | D-GEO-020, D-GEO-028, D-GEO-079 | Dùng hai trục nhãn (máy: "Đã kiểm chứng" / "Cần xem lại"; người: "AI soạn" / "Giáo viên đã duyệt"); MVP chỉ có "AI soạn"; thiết kế ghi lẫn "Hình đã kiểm chứng", dùng một chuỗi "Đã kiểm chứng". Đồng ý không? | A | ChuDauTu | Open |

## Phần BA (QA-GEO-01…39)

| QA id | Hỏi ai | Câu hỏi | Mặc định đang dùng trong AC | Trạng thái |
|---|---|---|---|---|
| QA-GEO-03 | PO | Nhãn máy kỳ vọng của HKG-06 | theo phép tính: "Đã kiểm chứng" | Ruled D-GEO-105 |
| QA-GEO-09 | PO | Phạm vi, đơn vị, mặc định của tham số hình mẫu | số > 0, không đơn vị | Ruled D-GEO-109 |
| QA-GEO-29 | ARCH (M-214) | Lược đồ chưa có chỗ lưu danh mục hình mẫu (US-GEO-205), danh sách đại lượng ẩn của mỗi đề (US-GEO-305) và bộ câu thử (US-GEO-611) | AC chỉ nêu hành vi, ánh xạ dữ liệu để ngỏ | Ruled D-GEO-133 (ARCH bổ sung lược đồ trước khi tách migration) |
