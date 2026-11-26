Phiên bản: v0.1 · 2026-11-27
# S3-E2 — phát hiện của Dev (T-261126-29) và giả định TẠM

> Không sửa `stories_ac.md`, `test_cases.md`, `test_design.md`. Mỗi dòng là một giả định TẠM hoặc một điểm cần PO/QC/epic khác xử lý.

## Dev

| Mã | Điểm | Cách làm TẠM trong code | Ảnh hưởng / cần ai |
|---|---|---|---|
| DEV-S3E2-01 | Một mô hình bộ bài (PO-66 G1): S3-E2 không đổi schema; chỉ V335 thêm quyền `UPDATE (class_id)` cho bộ Nháp chọn / đổi lớp trước khi giao. | API mới `/api/v1/teacher/assignment-sets…`, `/api/v1/student/assignment-sets…` trên `assignment_set/item` của V180; giao vẫn qua `fn_assign_practice_set`; học sinh "Làm bài" ở màn S2-E8 `/hs/bai-luyen/:id`. | — |
| DEV-S3E2-02 | BR-S3E2-04 cho chọn đề "Của tôi" của chính mình. `fn_assign_practice_set` (V180) chỉ nhận đề Kho chung có đáp số; đề "Của tôi" (sổ lời giải S2-E5/E6) không có hàng `problem` để sinh bài giao. | Màn soạn chỉ liệt kê đề Kho chung trường thấy; id ngoài phạm vi (đề Của tôi của người khác, bộ đề trường không thấy) → NOT_FOUND (FC6). Đề Của tôi của chính giáo viên CHƯA chọn được. | PO: chấp nhận cho P0, hay mở FIX nối đề Của tôi vào bộ bài. |
| DEV-S3E2-03 | US-S3E2-01-FC8 / TC-S3E2-14 "mỗi học sinh nhận đúng một Notification bài mới": module `notify` thuộc T-261126-31 (S3-E4), chưa có. | Giao idempotent theo khóa yêu cầu (bấm lặp trả bộ đã giao, không sinh hàng assignment lần hai). E2E kiểm một lần giao, một bộ ở Bài của em mỗi em, một dòng Đã giao; phần Notification chờ T-31 (cần phát `DomainEvents` khi giao — T-31 nghe sự kiện). | Phụ thuộc T-261126-31. |
| DEV-S3E2-04 | "Tổng quan giáo viên" (SC1, SC4, FC8) là màn của S3-E4 (T-31). | Kiểm bằng danh sách "Bài đang giao" của S3-E2 (`/gv/bo-bai`, tab Bài đang giao). | T-31 dùng cùng bảng. |
| DEV-S3E2-05 | Khóa yêu cầu của Giao / Đóng / Lưu trữ không lưu ở CSDL (không đổi schema). | Idempotent theo trạng thái: bộ đã ở trạng thái đích và có khóa → trả lại bộ (200). Hai khóa khác nhau bấm sau khi đã giao: Giao → CONFLICT; Đóng / Lưu trữ → 200 (đã ở trạng thái đích). | QC xác nhận chấp nhận. |
| DEV-S3E2-06 | Phiên bản lịch (TC-S3E2-25 "phiên bản số 3"). | `scheduleVersion` = tổng `release_rev` các hàng assignment của bộ; đổi giờ bộ đã giao gửi `version`, lệch → CONFLICT; mỗi câu theo giờ bộ đổi qua `fn_set_release` (một cổng giờ mở, ghi audit_log). Câu có giờ riêng không đổi theo bộ. | — |
| DEV-S3E2-07 | Dữ liệu mẫu: F-S3-052 "Bộ bài tuần 16" (910) chỉ có 2 đề (Đề 5, Đề 6) trong khi TC ghi 4 đề; mọi đề của bộ 's3_tuan' đều "Cần xem lại" (chưa có báo cáo kiểm chứng) nên không dựng được ca "đúng một đề Cần xem lại" (TC-32). | E2E S3-E2 dùng sandbox riêng `/test-hooks/s3e2/sandbox` + bộ đề riêng 's3e2_kho' (seed 920: 22 đề, chỉ Đề 3 "Cần xem lại"); không làm bẩn F-S3-050/052. | T-42/QC: cân nhắc sửa 910 nếu muốn ca thủ công khớp. |
| DEV-S3E2-08 | F-S3-040 ghi "mỗi lớp có một bộ Đã giao" nhưng 910 chưa có bộ của 10B. | Seed 920 thêm F-S3E2-050 "Bộ bài tuần 15 lớp 10B" (Thầy Khôi, 10B, Đề 5 + Đề 6, Đã giao) cho TC-S3E2-30. | T-28/T-30/T-31: biết thêm một bộ của 10B trong dữ liệu chung. |
| DEV-S3E2-09 | TC-S3E2-29 kỳ vọng tiến độ "2 / 4" của Bình An với F-S3-051 (An: Đúng, Xem lại, Đúng, Xem lại) — theo QA-S3E2-05 (Đúng hoặc Xem lại là xong) thì An là 4 / 4. | Tiến độ theo QA-S3E2-05; E2E dựng em làm 2 trên 4 đề (đúng ý TC). | QC sửa TC hoặc F-S3-051 cho khớp. |
| DEV-S3E2-10 | Cảnh báo giờ mở sớm hơn hạn (BR-S3E2-13) và hạn đã qua (QA-S3E2-02) là cảnh báo ở màn soạn; máy chủ không chặn (quyết định cuối thuộc giáo viên). | Nút Giao chờ chọn "Dời giờ mở sau hạn" (giờ mở = hạn + 1 giờ) hoặc "Giữ giờ mở sớm"; `releaseBeforeDue` có trong dữ liệu trả về để màn khác dùng. | — |
| DEV-S3E2-11 | ClassMembership (Chờ duyệt / Đã rời) do T-28 làm. | Dùng `class_member.status` của V110 (`active` mới thấy bộ); sandbox đặt `pending` trực tiếp. | Phụ thuộc T-28 khi có luồng duyệt thật. |

## Lịch sử

| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-27 | Tạo mới: 11 phát hiện / giả định TẠM của Dev S3-E2 | runner-T-261126-29 |
