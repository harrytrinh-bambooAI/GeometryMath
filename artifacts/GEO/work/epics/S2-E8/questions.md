Phiên bản: v0.2 · 2026-11-26
# S2-E8 — câu hỏi mở và đề xuất (mỗi câu có mặc định (đã chốt PO-66))

> Chủ sở hữu: BA + QC. Không sửa story map (R2 khóa 26/11/2026); đề xuất đổi story map ghi ở đây, PO xử lý bằng CR. Mọi mặc định: `quyết định · tin cậy · nguồn`.

| QA id | Loại | Hỏi ai | Câu hỏi hoặc đề xuất | Mặc định đang dùng (quyết định · tin cậy · nguồn) | Trạng thái |
|---|---|---|---|---|---|
| QA-S2E8-01 | Câu hỏi | PO | Kiểu đáp số của bài luyện: chỉ hai kiểu chính xác và xấp xỉ (DQ-12 có "sai số ±0,5°" và C2-08 có "kiểu đáp số, sai số cho phép")? Có thêm kiểu "số nguyên", "phân số", "biểu thức"? | Hai kiểu chính xác và xấp xỉ · Đã chốt PO-66 · vừa · DQ-12, STAGES Ch2 mục 6 C2-08 | Đã chốt PO-66 |
| QA-S2E8-02 | Câu hỏi | PO | Đáp số vô tỉ (4√3) nộp bằng thập phân làm tròn (6,93): Đúng hay Sai? STAGES chỉ nói "đúng giá trị nhưng khác dạng viết" | Sai ở kiểu chính xác, hiện gợi ý viết dạng chính xác; giáo viên muốn nhận thì chọn kiểu xấp xỉ · Đã chốt PO-66 · vừa · STAGES Ch2 mục 5 luồng C | Đã chốt PO-66 |
| QA-S2E8-03 | Câu hỏi | Design | Độ dài tối đa của đáp số nhập? | 50 ký tự · Đã chốt PO-66 · thấp · không có nguồn | Đã chốt PO-66 |
| QA-S2E8-04 | Câu hỏi | Dev | Số chữ số tối đa của một số trong đáp số? | 15 chữ số · Đã chốt PO-66 · thấp · không có nguồn | Đã chốt PO-66 |
| QA-S2E8-05 | Câu hỏi | PO | Khoảng sai số cho phép của kiểu xấp xỉ (0 đến 10, hai chữ số thập phân)? | 0 đến 10, tối đa 2 chữ số thập phân · Đã chốt PO-66 · thấp · DQ-12 (chỉ nêu ±0,5°) | Đã chốt PO-66 |
| QA-S2E8-06 | Câu hỏi | PO | Dấu chấm trong "1.000": thập phân (bằng 1) hay ngăn nghìn (bằng 1000)? Học sinh Việt Nam hay viết chấm làm ngăn nghìn | Chấm là dấu thập phân; không có ngăn nghìn · Đã chốt PO-66 · thấp · không có nguồn (rủi ro chấm nhầm) | Đã chốt PO-66 |
| QA-S2E8-07 | Câu hỏi | PO | Gợi ý mức 1 sau lần Sai thứ hai: giữ nguyên hay lên mức 2? STAGES chỉ nói "gợi ý mức 1" | Giữ nguyên mức 1 ở chặng 2; mức cao hơn để sau · Đã chốt PO-66 · vừa · STAGES Ch2 mục 5 luồng C | Đã chốt PO-66 |
| QA-S2E8-08 | Đề xuất đổi story map | PO | Bài luyện cần giao cho một lớp nhưng lớp thật và đăng nhập lớp thuộc S3-E1 (chặng 3). Chặng 2 giao cho lớp mẫu MVP nào? Đề xuất ghi phụ thuộc rõ trong story map hoặc đưa US-S3E1-01 tối thiểu về S2 | Giao cho lớp mẫu 11A1 chuyển từ MVP (US-S2E1-04); bài luyện là bộ bài dùng chung mô hình S3-E2 · Đã chốt PO-66 (G1) | Đã chốt PO-66 (G1) |
| QA-S2E8-09 | Câu hỏi | PO | Khi giáo viên xác nhận "Em nghĩ đáp số này đúng": câu của em đổi thành Đúng và dạng viết đó thêm vào danh sách chấp nhận cho cả lớp? | Có, và đếm vào M-S2-07 · Đã chốt PO-66 · vừa · STAGES Ch2 mục 10, BRD delta M-S2-07 | Đã chốt PO-66 |
| QA-S2E8-10 | Đề xuất đổi story map | PO | "Hết lượt thì xem lại cùng thầy cô" (STAGES luồng C bước 3) cần chỗ giáo viên thấy danh sách em hết lượt, nhưng không story nào của S2-E8 có (bảng lớp ở S3-E3). Đề xuất thêm một mục "Câu cần xem lại" cho giáo viên vào S2-E8 | Chưa có AC; chờ PO · Đã chốt PO-66 · vừa · story-map-s23 | Open |

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới: 10 câu | runner-T-261126-12 |
| v0.2 | 2026-11-26 | Áp PO-66: đóng các QA đã chốt ("Đã chốt PO-66"), giữ Open cho đề xuất ngoài gate | runner-T-261126-37 (BA/QC) |
