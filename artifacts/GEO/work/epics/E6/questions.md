# E6 — open questions (each with its default)

> PO gieo từ `work/questions_for_stakeholders.md` (bản gộp là nguồn duy nhất). BA, QC, ARCH thêm `QA-GEO-nn` theo dải id ở `board/CONVENTIONS.md` §2.

| Q id | Decision ref | Question | Default in use | Owner | Status |
|---|---|---|---|---|---|
| Q-CDT-06 | D-GEO-035 | `MVP.md` ghi "một đề hoặc một bộ đề", PO-25 ghi MVP giao từng đề. MVP giao từng đề được không? | A | ChuDauTu | Open |
| Q-GV-02 | D-GEO-065 | Câu trả lời lộ khi chứa đáp số hoặc giá trị quyết định ở mọi dạng viết tương đương (thập phân, phân số, căn thức), hoặc các bước của lời giải đang khóa; công thức và phương pháp tổng quát thì không. Đúng không? | A | GiaoVienCoVan | Open |
| Q-GV-03 | D-GEO-041, D-GEO-042, D-GEO-043 | Đúng giờ đặt thì mở; đã mở rồi thì không khóa lại và không đổi giờ nữa; bài chưa đặt giờ thì lời giải vẫn khóa. Có đúng cách thầy cô muốn dùng trong lớp không? | A | GiaoVienCoVan | Open |
| Q-GV-04 | D-GEO-053 | Khung thiết kế có hai công tắc: "Gợi ý mức 1 và 2" và "Hỏi đáp (không nêu đáp số)". Đủ chưa, hay cần lựa chọn khác (ví dụ giới hạn một mức gợi ý)? | A | GiaoVienCoVan | Open |
| Q-GV-06 | D-GEO-027 | Trước giờ mở, trợ lý nêu công thức và phương pháp tổng quát, dẫn tới bài giảng; không nêu đáp số hay các bước của bài đang làm; khi từ chối thì nhắc lại gợi ý đang có, không tính thêm lượt gợi ý. Đồng ý không? | A (PO-22, 37, 44) | GiaoVienCoVan | Open |
| Q-TL-04 | D-GEO-054, D-GEO-051, D-GEO-076 | Mẫu có một trường "THPT Mẫu A" chứa hai lớp (cấp là thuộc tính lớp), dữ liệu thử có thêm "THCS Mẫu C"; mã lớp duy nhất toàn hệ thống; kho đề và bài giảng mẫu là nội dung hệ thống dùng chung, đọc được từ mọi trường. Khả thi không? | A | TechLead | Open |
| Q-TL-06 | D-GEO-072, D-GEO-074 | Khi cổng AI lỗi hoặc quá thời gian: báo lỗi dễ hiểu, cho thử lại (thời gian chờ tối đa?). Trang đang mở cập nhật khóa không muộn hơn 10 giây sau giờ mở. Đồng ý và cơ chế nào? | A | TechLead | Open |

## Phần BA (QA-GEO-01…39)

| QA id | Hỏi ai | Câu hỏi | Mặc định đang dùng trong AC | Trạng thái |
|---|---|---|---|---|
| QA-GEO-02 | PO | Tên học sinh 9B | Khánh, Linh, Minh, Ngân, Phúc | Ruled D-GEO-104 |
| QA-GEO-14 | PO | Giao lại cùng đề cho cùng lớp | chặn | Ruled D-GEO-113; "cùng đề" = cùng `problem` → `CONFLICT` (D-GEO-161) |
| QA-GEO-15 | PO | Đổi công tắc trợ giúp sau khi giao | không có | Ruled D-GEO-114 |
| QA-GEO-22 | PO | Hộp xác nhận trước "Mở ngay" | PO phán: CÓ | Ruled D-GEO-119 |
| QA-GEO-23 | PO | Xem trước có ghi sự kiện không | không | Ruled D-GEO-120 |
| QA-GEO-28 | PO (M-214) | Thiết bị ngủ hoặc mất mạng qua giờ mở: bao lâu sau khi hoạt động lại thì hiện lời giải | ≤ 10 giây từ lúc hoạt động lại | Ruled D-GEO-132 |
| QA-GEO-34 | PO (M-214) | Dòng "hôm nay em có 2 bài cần làm" của `mvp-04` không có định nghĩa | không hiện; chỉ "Lớp {tên lớp}" | Ruled D-GEO-138 (n = số bài được giao chưa xem lời giải; US-GEO-606-SC7) |
| QA-GEO-35 | PO (M-214) | Xem trước một đề đã giao: theo giờ mở của bài giao nào | chỉ viết AC cho đề chưa giao (coi như Chưa đặt giờ) và cho việc không lộ | Ruled D-GEO-139, 146 (US-GEO-607-SC5, SC6, FC4) |
| QA-GEO-37 | ARCH, PO (M-214) | D-GEO-074 (≤ 10 giây) khi "Mở ngay" hoặc "Đổi giờ" so với ADR-GEO-10 (hỏi lại trạng thái mỗi 30 ± 10 giây) | giữ 10 giây (612-SC3) | Ruled D-GEO-130, 157 (hỏi lại ≤ 8 giây) |
