Phiên bản: v0.2 · 2026-11-26
# R3 · Xem nhanh spec chặng 2–3 (S2-E1…E9, S3-E1…E6)

> Nguồn: epic S2-*, S3-* (T-12, T-14, 26/11/2026), `RV_s23.md` (soát phản biện), `PO-DECISIONS.md` PO-61…66. Số liệu do `check_ac_tc.py` đếm sau khi áp PO-66 (T-261126-37): 746 AC còn hiệu lực, 751 TC (S2: 447/452 · S3: 299/299); trước đó 723/728. Mọi epic "ready for R3"; owner đã duyệt gate T-261126-16 (PO-66, 26/11/2026, "OK hết"). Cột "Câu mở chính" giữ nguyên làm lịch sử: các câu đó đã chốt PO-66.

## Chặng 2 (35 story)
| Epic | Story | AC | TC | Mức | Câu mở chính |
|---|---|---|---|---|---|
| S2-E1 Đăng ký/đăng nhập mã, khôi phục | 4 | 46 | 46 | P0 | QA-04 đã chốt PO-66 (G3): mã khôi phục cho cả học sinh; QA-06 có chuyển dữ liệu MVP không |
| S2-E2 Khung môn học, hình phẳng, bench | 5 | 45 | 45 | P0 | QA-01 dạng hình phẳng hỗ trợ; QA-02 ai soạn bộ đề chuẩn |
| S2-E3 Thêm đề: dán chữ, ảnh, 4 bước | 4 | 39 | 39 | P0 | QA-02 giới hạn thêm đề theo giờ (PO-63); ngưỡng 0,80 ô chưa chắc |
| S2-E4 Chat chỉnh nháp, import docx/pdf, trùng | 3 | 27 | 27 | P1 | khung màn hình do task 10 đề xuất, chưa vẽ |
| S2-E5 Sửa lời giải, StepCheck, nhãn, duyệt, bất biến | 5 | 64 | 66 | P0 | QA-02 quy tắc kiểm từng bước và dung sai (m-05) |
| S2-E6 Kho đề, soát Kho chung, đề Của tôi | 3 | 37 | 37 | P0 | QA-01 ai gán vai người soát; quyền xem lời giải: đã chốt PO-66 (G2) |
| S2-E7 Trình chiếu, mất mạng, hỏi lại khi khóa | 3 | 33 | 33 | P0 | QA-01 cỡ chữ và thiết bị chiếu mẫu |
| S2-E8 Bàn phím toán, chấm, làm bài, kết quả, soạn | 5 | 112 | 115 | P0 | QA-02 đáp số vô tỉ nộp thập phân (đã chốt PO-66, G11); bộ bài S3: một mô hình, đã chốt PO-66 (G1) |
| S2-E9 Biến thể, xuất ảnh/PDF, trắc nghiệm | 3 | 44 | 44 | P1 | QA-01 chưa có khung màn hình cho Biến thể và Xuất |
| **Chặng 2** | **35** | **447** | **452** | P0 29 · P1 6 | |

## Chặng 3 (23 story, story map v0.2 ghi 23 sau PO-66 G5)
| Epic | Story | AC | TC | Mức | Câu mở chính |
|---|---|---|---|---|---|
| S3-E1 Lớp, mã lớp, duyệt, đặt lại mật khẩu, quyền | 5 | 56 | 56 | P0 | QA-05 tối đa 20 lớp; QA-07 chống dò mã: đã chốt PO-66 (G4); US-05 nhận vào (G5) |
| S3-E2 Bộ bài, lịch mở lời giải, Bài của em, đóng/lưu trữ bộ | 4 | 45 | 45 | P0 | khóa giờ mở; cảnh báo giờ mở sớm hơn hạn; US-04 vòng đời bộ (PO-66 G5) |
| S3-E3 Bảng lớp × bài, bằng chứng, "Cả lớp vướng ở đâu" | 3 | 57 | 57 | P0 | QA-02 ngưỡng 5 bài là cấu hình; QA-09 p95 2 giây |
| S3-E4 Tổng quan GV, theo dõi nhanh, Notification | 3 | 41 | 41 | P0 | QA-01 "chưa mở bài"; giữ thông báo 90 ngày |
| S3-E5 AuditEvent, thông báo riêng tư, trường được phép, công tắc AI, xóa tài khoản | 5 | 60 | 60 | P0 | QA-01 ai đọc nhật ký; QA-05 tên giống email/điện thoại; US-05 xóa tài khoản (PO-66 G5) |
| S3-E6 Chữa bài sang Trình chiếu, xuất, nhắc việc | 3 | 40 | 40 | P1 | QA-01 chữa bài từ kết quả; MSG-S3-91 lệch S2-E7 (m-04) |
| **Chặng 3** | **23** | **299** | **299** | P0 20 · P1 3 | |

(Mức P0/P1 của S3 lấy theo story map v0.2; US-S3E1-05, US-S3E2-04, US-S3E5-05 xếp P0.)

## Soát phản biện (RV_s23)
- 1 blocker, 10 major, 8 minor. Đã sửa trong spec: 6 major, 2 minor. Đã chốt PO-66: 1 blocker + 4 major + m-02. Còn mở: 5 minor (m-04…m-08). 0 blocker, 0 major mở (RV_s23 v0.3).
- Đã sửa: lớp MVP đã chuyển thành lớp chặng 3 (thêm AC + TC); bộ đếm IP so với "không lưu IP"; danh sách trường được phép thiếu thực thể chặng 2 và chặn nhầm ảnh đề; ngoại lệ của snapshot bài giao; luật tên giống email/điện thoại; kênh Trình chiếu của giáo viên so với "mọi kênh khóa"; 12 mã mẫu sai bảng ký tự.
- Công cụ: `check_ac_tc.py` bỏ sót AC loại biên (BV) của S2/S3; đã sửa, 723 AC đều có TC.

## Cần owner (quyết ở R3) — Đã chốt PO-66 (26/11/2026, "OK hết")
1. **Đã chốt PO-66 (G1)** · **B-1 (blocker)** bài luyện S2-E8 và bộ bài S3-E2 là một hay hai mô hình; chặng 2 giao bài cho lớp nào khi lớp chỉ tạo ở S3. Khuyến nghị: một mô hình.
2. **Đã chốt PO-66 (G2)** · **M-01** lời giải Kho chung: vai Giáo viên xem ngay (BR-S2E6-14) hay theo giờ mở (PO-63); Tự học dùng luật nào.
3. **Đã chốt PO-66 (G4)** · **M-08** chống dò mã lớp 4 ký tự (giới hạn tổng, mã dài hơn, hoặc bắt buộc duyệt từng em).
4. **Đã chốt PO-66 (G3)** · **M-09** học sinh Tự học/Chờ duyệt quên mật khẩu (hiện: tạo tài khoản mới, mất bài làm).
5. **Đã chốt PO-66 (G5)** · **M-10, m-02** ghi CR: story US-S3E1-05 ngoài story map; trạng thái "Bị từ chối".
6. **Đã chốt PO-66 (G9)** · xác nhận 3 giả định mới: BR-S3E1-16 (lớp MVP), bộ đếm IP tạm, khóa `problem_image`.
7. **Đã chốt PO-66 (G8, G7)** · câu mở gốc đã có sẵn: Q3 (giả làm giáo viên), Q8 (xóa tài khoản, QA-S2E1-09), Q1 (code chặng 2 chờ đo thật).

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo mới | runner-T-261126-15 (QC) |
| v0.2 | 2026-11-26 | Áp PO-66 (T-261126-37): số liệu 746 AC / 751 TC, 58 story; mục "Cần owner" ghi "Đã chốt PO-66 (G…)" | runner-T-261126-37 (BA/QC) |
