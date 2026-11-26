Phiên bản: v0.1 · 2026-11-27
# S3-E6 — phát hiện QC / Dev

## Dev (runner-T-261126-32: US-S3E6-01, 02)
| Mã | Nội dung | Mức | Xử lý TẠM |
|---|---|---|---|
| Dev-S3E6-01 | TC-S3E6-19 (BR-S3E6-14): giao bộ mới cho lớp Đã lưu trữ — module `assignment` (`AssignmentSetService.classOf`, `ownClass` lọc `archived_at IS NULL`) trả NOT_FOUND, spec đòi CONFLICT. Ngoài phạm vi tệp của task; spec E2E giữ kỳ vọng CONFLICT nên sẽ đỏ tới khi FIX. Thân yêu cầu tạo bộ trong spec chưa đối chiếu lượt chạy. | Vừa | Đã FIX 2026-11-27 (runner-fix-s3f3): `classOf` trả CONFLICT khi lớp của mình đã lưu trữ. |
| Dev-S3E6-02 | "Chữa bài này" mở màn Trình chiếu riêng `/gv/chua-bai?lop&bo&de&buoc` (S2-E7 `PresentPage` chiếu tài liệu Kho đề của tôi theo `docId`, không nhận đề kho của bộ hay bước bắt đầu). Màn có đề + các bước lời giải của phiên bản hiện hành (qua hàm cổng `fn_get_solution_teacher`), "Bước trước"/"Hiện bước kế", Thoát về `ket-qua-lop?lop&bo&the=` (cuộn tới thẻ). Chưa có hình (đề `s3_tuan`/BOB là hình phẳng không có hình dựng). | Vừa | PO/Designer xác nhận màn chiếu rút gọn; FIX S2-E7 nếu muốn dùng chung một màn. |
| Dev-S3E6-03 | Nút "Chữa bài này" và "Xuất kết quả" gắn vào màn Kết quả lớp của S3-E3 bằng route `ket-qua-lop` của `features/s3e6` (bọc màn gốc; nút gắn vào thẻ `s3e3-insight` qua portal), không sửa `features/s3e3`. Thẻ dữ liệu thô (`s3e3-raw`) không có nút. | Thấp | FIX S3-E3 nếu muốn nút nằm trong component gốc. |
| Dev-S3E6-04 | Lưu trữ / Bỏ lưu trữ đặt ở màn một lớp (route `lop-hoc/:id` bọc màn S3-E1); mục "Lớp đã lưu trữ" (MSG-S3-96) ở Bài của em (route `bai-cua-em` bọc cổng riêng tư S3-E5 + màn E1). API học sinh `/api/v1/student/classes` thêm cờ `archived`. | Thấp | — |
| Dev-S3E6-05 | BR-S3E6-14 "Kết quả lớp và xuất vẫn dùng được": danh sách chọn lớp của Kết quả lớp (`tracking`) nay gồm lớp Đã lưu trữ (sau lớp Đang dạy). Bỏ lưu trữ vẫn giữ giới hạn 20 lớp Đang dạy (VALIDATION_FAILED `limit`, ngoài spec). | Thấp | PO xác nhận. |
| Dev-S3E6-06 | Sự kiện đo M-S3-04: bảng `present_from_results_event` (mã giáo viên, mã đề hiện hành, giờ); lần mở trong 5 giây trả cùng `sessionId` (một phiên). Giới hạn xuất: bảng `class_export_log` (10 lần / 1 giờ / giáo viên, Retry-After). Hai bảng có trong `allowed_fields` và được dọn khi xóa tài khoản. | Thấp | — |
| Dev-S3E6-07 | Bộ của sandbox S3-E3 "Chưa đặt giờ" (lời giải khóa); móc `/test-hooks/s3e6/set-release` đặt giờ mở của bộ cho ca SC1/SC2/FC2/FC8. | Thấp | — |
| Dev-S3E6-08 | US-S3E6-03 (nhắc việc) do agent S3-E4 làm (T-31); TC-S3E6-10 Manual (thiết bị thật). | — | — |

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-27 | Mục Dev đầu tiên (US-S3E6-01, 02) | runner-T-261126-32 |
