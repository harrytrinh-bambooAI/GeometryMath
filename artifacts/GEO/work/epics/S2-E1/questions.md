Phiên bản: v0.1 · 2026-11-26
# S2-E1 — câu hỏi mở và đề xuất (mỗi câu có mặc định TẠM)

> Chủ sở hữu: BA + QC. Không sửa story map (R2 khóa 26/11/2026); đề xuất đổi story map ghi ở đây, PO xử lý bằng CR. Mọi mặc định: `TẠM · tin cậy · nguồn`.

| QA id | Loại | Hỏi ai | Câu hỏi hoặc đề xuất | Mặc định đang dùng (TẠM · tin cậy · nguồn) | Trạng thái |
|---|---|---|---|---|---|
| QA-S2E1-01 | Câu hỏi | Owner | Mã khôi phục dùng một lần rồi sinh mã mới, hay dùng nhiều lần? Đổi mật khẩu có hủy phiên đang mở không? | Dùng một lần, sinh mã mới, hủy phiên khác · TẠM · vừa · PO-61 | Open |
| QA-S2E1-02 | Câu hỏi | Owner | Luật mật khẩu: độ dài tối thiểu, tối đa, có yêu cầu chữ số hay không? | 8 đến 64 ký tự, không yêu cầu loại ký tự · TẠM · vừa · PO-61 (không nêu luật) | Open |
| QA-S2E1-03 | Câu hỏi | Owner | Ngưỡng giới hạn tạo tài khoản theo địa chỉ mạng (PO-63)? Nhiều học sinh cùng lớp dùng chung mạng trường có bị chặn oan? | 5 lần mỗi giờ mỗi địa chỉ; lớp học nên cấp ngoại lệ sau · TẠM · thấp · PO-63, plan mục 5 Q3 | Open |
| QA-S2E1-04 | Câu hỏi | Owner | Học sinh tự học không lớp quên mật khẩu: chưa có đường khôi phục, chấp nhận tạo tài khoản mới (DQ-17)? | Chấp nhận tạo tài khoản mới · TẠM · vừa · DQ-17, PO-61 | Open |
| QA-S2E1-05 | Câu hỏi | PO | Tài khoản chuyển từ MVP: mật khẩu tạm phát bằng cách nào (giáo viên đọc cho học sinh)? | Mật khẩu tạm sinh ngẫu nhiên, in trong báo cáo chuyển cho giáo viên · TẠM · thấp · PO-61 | Open |
| QA-S2E1-06 | Đề xuất đổi story map | PO | Dữ liệu MVP chỉ dùng cho môi trường thử; có cần chuyển cho tài khoản thật không (MVP chưa có người dùng thật)? Nếu không, US-S2E1-04 có thể giảm xuống Should | Giữ P0 cho tới khi PO xác nhận · TẠM · thấp · PO-65 | Open |
| QA-S2E1-07 | Đề xuất đổi story map | PO | Tick "đã đọc thông báo riêng tư" (DQ-15) cần văn bản bản 1.0, nhưng story văn bản nằm ở US-S3E5-02 (chặng 3). Nên chuyển phần "hiện văn bản và lưu cờ" về S2-E1 | Dùng văn bản tạm 3 dòng trong S2-E1; S3-E5-02 mở rộng sau · TẠM · vừa · PO-64 | Open |
| QA-S2E1-08 | Câu hỏi | Design | Mã người dùng không phân biệt hoa thường để học sinh gõ trên iPad đỡ nhầm? | Không phân biệt, cắt khoảng trắng đầu cuối · TẠM · vừa · DQ-18 | Open |
| QA-S2E1-09 | Đề xuất đổi story map | PO | Q8 (PO-64) nêu "nút xóa tài khoản" nhưng không story nào của chặng 2 hoặc 3 có. Đề xuất thêm story "Xóa tài khoản và dữ liệu bài làm" vào S2-E1 hoặc S3-E5 | Chưa có AC; chờ PO · TẠM · vừa · PO-64 | Open |

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: 9 câu | runner-T-261126-12 |
