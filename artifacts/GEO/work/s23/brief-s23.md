Phiên bản: v0.1 · 2026-11-26
# Brief S23 · GEO · chặng 2 (Trọn môn Hình học) + chặng 3 (Lớp học thật)

Trạng thái: nháp của PO chờ owner xác nhận ở gate 16 (T-261126-16). Mọi ô Nghiêng là TẠM.

| Mục tiêu | Giáo viên thật tự thêm đề, soát và duyệt lời giải AI, trình chiếu, giao bài luyện (chặng 2, S2-E1…E9), rồi mở nhiều lớp, giao bộ bài và xem kết quả lớp (chặng 3, S3-E1…E6). Định danh theo PO-61: đăng ký chỉ bằng tên hiển thị và vai, hệ thống sinh mã người dùng, đăng nhập mã + mật khẩu, không thu dữ liệu cá nhân, giáo viên đặt lại mật khẩu cho học sinh lớp mình, giáo viên tự khôi phục bằng mã khôi phục (TẠM). Sản phẩm cộng đồng, miễn phí. Đo được: số đề giáo viên tự thêm, số phiên trình chiếu, số bài luyện giao, số giáo viên giao từ hai bộ bài trở lên cho một lớp. |
| Nguồn | STAGES-AFTER-MVP.md (22/11/2026) Chặng 2, Chặng 3, mục 0.2–0.4; PRODUCT-MASTER.md (22/11/2026) B7, C4, C12, D1–D3; PO-DECISIONS.md PO-61 (26/11/2026, mới nhất, thắng mọi nguồn cũ) cùng PO-62…65 viết hôm nay; .claude/plans/STAGE-2-3-PLAN.md v0.2 (26/11/2026) mục 1 và mục 5; DOMAIN-PROFILE.md. Khi STAGES và PO-61 mâu thuẫn thì theo PO-61 (xem brd-delta-s23.md mục 2). |
| Xong khi | Brief này đủ 4 dòng; brd-delta-s23.md và story-map-s23.md có ít nhất 1 story cho mỗi epic trong 15 epic; epic_board.md có 15 dòng S2-E1…S3-E6; PO-62…65 trong PO-DECISIONS.md; R2 khóa (PO) 26/11/2026 và chờ owner R3 ở gate 16. Lệnh kiểm: test "$(grep -cE '^[|] (Mục tiêu|Nguồn|Xong khi|Ngoài phạm vi) [|] [^<]' artifacts/GEO/work/s23/brief-s23.md)" -eq 4 && test "$(grep -cE '^[|] S[23]-E[0-9]' artifacts/GEO/work/epic_board.md)" -ge 15 |
| Ngoài phạm vi | Chặng 4–9 (môn thứ hai, rubric, nộp bài viết tay, phụ huynh, trường, liên trường, giáo trình riêng học sinh); dựng hình tự do; kiểm chứng chứng minh; bảng xếp hạng; nhiều giáo viên một lớp; đăng nhập mạng xã hội; thông báo đẩy ngoài ứng dụng; xác minh giáo viên và đồng thuận cha mẹ (PO-61 bỏ); AC và test case (task 12–14); code app/; Design/; sửa STAGES-AFTER-MVP.md và PRODUCT-MASTER.md; deploy prod; gọi AI thật trong CI (giữ mock). |

## Câu hỏi owner (gate 16, cột Nghiêng đang dùng là TẠM)
| Câu | Nghiêng (TẠM · tin cậy) | Ghi ở |
|---|---|---|
| Q1 code chặng 2 chờ B7 đo thật | chờ, spec chạy trước · cao · plan §2 | PO-65 |
| Q2 cổng "giáo viên dùng lặp lại" thành cổng phát hành L7 chặng 3 | code S3 ngay, owner chốt L7 · vừa · plan §2 | PO-65 |
| Q3 không xác minh giáo viên thì ai cũng xem lời giải Kho chung | chấp nhận, giới hạn tần suất, ghi rủi ro · vừa · plan §5 | PO-63 |
| Q4 đọc ảnh đề | Claude vision, ô chưa chắc tô để giáo viên sửa · vừa · plan §5 | PO-65 |
| Q5 S2-E4 chat chỉnh nháp và import file | P1 chặng 2; bài giảng với AI dời chặng 5 · vừa · plan §5 | PO-65 |
| Q6 ngưỡng nhận định gộp; số tổng bài luyện | 5 bài; không hiện số · vừa · plan §5 | PO-65 |
| Q7 thiết diện động | để sau · cao · plan §5 | PO-65 |
| Q8 dữ liệu bài làm gắn tên hiển thị | chính sách tối thiểu + nút xóa tài khoản · vừa · plan §5 | PO-64 |
| Q9 ai soát Kho chung; web hay app; tự học không lớp | owner tạm soát; web (PWA), chỉ thông báo trong ứng dụng; cho tự học · vừa · plan §5 | PO-65 |

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Tạo brief S23 áp PO-61 | runner-T-261126-08 (PO) |
