Phiên bản: v0.1 · 2026-11-26
# S2-E6 — phát hiện của QC / Dev

## Dev (runner-T-261126-23)

| Mã | Nguồn | Phát hiện | Cách làm TẠM (quyết định · tin cậy · nguồn) | Ai xử lý tiếp |
|---|---|---|---|---|
| Dev-S2E6-01 | PLAN A10, BR-S2E6-01 | "Đề Của tôi" chưa có bảng riêng (nháp đề là S2-E3); S2-E5 neo đề của giáo viên vào `solution_doc`. | Ngăn "Của tôi" = mọi `solution_doc` của chính giáo viên (kể cả đề chưa duyệt; bỏ qua cờ `in_my_shelf` của S2-E5 khi liệt kê). Loại hình suy từ `figure` (`{"plane": …}` = Phẳng, còn lại Không gian). TẠM · vừa · PLAN §2 | T-22 (nháp đề), L4 |
| Dev-S2E6-02 | PLAN A10 ("cột trên problem") | Đặt cột ngăn trên `problem` không dùng được vì đề của giáo viên không nằm ở `problem`. | Bảng `library_submission` (trạng thái gửi soát, lịch sử) + `library_problem` (bản chép độc lập lúc duyệt, BR-S2E6-10). Không sửa bảng `problem`. TẠM · vừa · PLAN §2 | L4 |
| Dev-S2E6-03 | STAGES Ch2 §7, PO-65 Q9 | "Kho chung: mọi tài khoản đọc được" — chưa rõ toàn nền tảng hay theo tổ chức. Giáo viên chặng 2 tự đăng ký cùng tổ chức "Cộng đồng". | Kho chung theo tổ chức (lọc `org_id`, giữ luật quét SQL theo trường). Đổi sang toàn nền tảng cần quyết định ARCH (bảng chung, M-503). TẠM · vừa · STAGES Ch2 §7 | PO / ARCH |
| Dev-S2E6-04 | QA-S2E6-01 | Không có màn gán vai người soát. | Bảng `user_role_grant` (iam), chỉ gán bằng SQL / seed (chủ sản phẩm); ứng dụng chỉ đọc. TẠM · cao · QA-S2E6-01 | Vận hành |
| Dev-S2E6-05 | BR-S2E6-13, PO-63 | Đường thêm đề bằng ảnh / import (S2-E3, S2-E4) chưa có trong nhánh này. | `content.api.LibraryQuota.consume(channel, ref)` + bảng `problem_create_event`; đường dán chữ (`POST /api/v1/teacher/library/problems`) và sao chép Kho chung đã gọi. TC-S2E6-032 ghi sự kiện ảnh/import trực tiếp vào bảng (mô phỏng). T-22/T-24 PHẢI gọi `LibraryQuota` trong cùng giao dịch ghi đề. TẠM · cao · BR-S2E6-13 | T-22, T-24 |
| Dev-S2E6-06 | US-S2E6-01-FC1 | Nút "Thêm đề" ở trạng thái trống: màn thêm đề đầy đủ (ảnh, AI soạn) thuộc S2-E3. | Mở ô "Dán đề bài" tối giản (tạo đề vào Của tôi qua bộ đếm). TẠM · vừa · PLAN §5 | T-22 nối màn thêm đề |
| Dev-S2E6-07 | US-S2E6-03-FC2 | "Import đề giống FX-10" (S2-E4) chưa có; đối chiếu trùng mới có `POST /api/v1/teacher/library/similar` (chỉ đề của mình + Kho chung). | TC-S2E6-027 kiểm danh sách, tìm và đối chiếu trùng qua API này. TẠM · vừa | T-24 dùng chung khi import |
| Dev-S2E6-08 | BR-S2E6-14, US-S2E6-03-FC9 | Đề Kho chung chưa có gợi ý theo bậc. | Gợi ý một câu ghi lúc gửi soát (`hint`, tùy chọn), mặc định câu gợi ý chung không lộ lời giải. Lời giải phía học sinh luôn khóa ngoài bài giao. TẠM · vừa · PO-66 G2 | PO |
| Dev-S2E6-09 | Rail | MVP đã có mục "Kho đề" (kho nền tảng). | Mục rail mới "Kho của tôi" (`/gv/kho-de-cua-toi`), hai ngăn ở trong. TẠM · vừa · Design C2-01 | Designer |

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Mục Dev đầu tiên | runner-T-261126-23 |
