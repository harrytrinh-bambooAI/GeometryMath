Phiên bản: v0.1 · 2026-11-26
# S2-E5 — câu hỏi mở và đề xuất (mỗi câu có mặc định TẠM)

> Chủ sở hữu: BA + QC. Không sửa story map (R2 khóa 26/11/2026); đề xuất đổi story map ghi ở đây, PO xử lý bằng CR. Mọi mặc định: `TẠM · tin cậy · nguồn`.

| QA id | Loại | Hỏi ai | Câu hỏi hoặc đề xuất | Mặc định đang dùng (TẠM · tin cậy · nguồn) | Trạng thái |
|---|---|---|---|---|---|
| QA-S2E5-01 | Câu hỏi | Owner | Giới hạn số bước (30) và độ dài mỗi bước (2000 ký tự)? | 30 bước, 2000 ký tự · TẠM · thấp · không có nguồn | Open |
| QA-S2E5-02 | Câu hỏi | PO | Quy tắc kiểm từng bước: kiểm theo giá trị bước trước (lỗi không lan) hay theo đáp số đúng của máy? Dung sai bao nhiêu? Bản mẫu `c2-03-lech-chan-duyet.png` chỉ tô bước 3 | Theo giá trị bước trước; dung sai tuyệt đối 0,01 · TẠM · vừa · DQ-04 và ảnh c2-03 | Open |
| QA-S2E5-03 | Câu hỏi | PO | Duyệt có bị chặn khi nhãn máy "Cần xem lại" mà không có bước lệch không? | Không chặn, buộc xác nhận cảnh báo · TẠM · vừa · DQ-05, PO-38 | Open |
| QA-S2E5-04 | Câu hỏi | Dev | Hạn thời gian kiểm một lời giải (10 giây)? | 10 giây · TẠM · thấp · chờ đo B7 | Open |
| QA-S2E5-05 | Câu hỏi | PO | Điều kiện "Đã kiểm chứng": cần ít nhất một bước Đạt? | Cần ít nhất một bước Đạt, không bước Không đạt, đáp số cuối khớp · TẠM · vừa · STAGES Ch2 mục 10 (chú thích "máy đã kiểm tra phần tính toán") | Open |
| QA-S2E5-06 | Câu hỏi | PO | Có cho áp phiên bản chưa duyệt cho bài đang giao không? | Không, phải duyệt trước · TẠM · cao · STAGES mục 0.2 nguyên tắc 3 | Open |
| QA-S2E5-07 | Câu hỏi | PO | Đổi đáp số khi áp phiên bản mới: kết quả bài luyện đã chấm có tính lại không? | Không tính lại, cảnh báo · TẠM · vừa · BR-S2-08 | Open |
| QA-S2E5-08 | Đề xuất đổi story map | PO | US-S2E5-05 loại "Xuyên suốt" nhưng chỉ có phía giáo viên; phía học sinh (thấy v nào khi đang mở) chưa có story. Đề xuất thêm AC vào S2-E8 hoặc tách story | Đã thêm AC SC5 ở US-S2E5-05; chờ PO · TẠM · vừa · story-map-s23 | Open |

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: 8 câu | runner-T-261126-12 |
