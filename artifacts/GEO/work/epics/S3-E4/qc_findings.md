Phiên bản: v0.1 · 2026-11-27
# S3-E4 — phát hiện QC / Dev

## Dev (runner-T-261126-31: US-S3E4-01…03, kèm US-S3E5-01 và US-S3E6-03)
| Mã | Nội dung | Mức | Xử lý TẠM |
|---|---|---|---|
| Dev-S3E4-01 | Bản mẫu chặng 2–3 không vẽ Tổng quan giáo viên (C3-05) và Theo dõi nhanh (C3-06) của S3-E4 (mã C3-05/06 trong `DQ.md` là màn khác). Dựng theo AC bằng lớp dùng chung + token theme, icon `shared/ui/Icon`: `/gv/tong-quan`, `/gv/theo-doi-nhanh?set=…`, `/hs/thong-bao`; chấm thông báo bọc route `/hs/bo-bai`, `/hs/bo-bai/:id` của S3-E2 (epic sau thay epic trước, màn gốc giữ nguyên bên trong). | Vừa | Designer vẽ C3-05/06 rồi so lại. |
| Dev-S3E4-02 | S3-E2 chưa phát sự kiện khi giao bộ: thêm một lệnh gọi `AuditEventService.assigned(...)` ở `AssignmentSetService.assign` (module assignment, trong phạm vi) phát `AssignmentSetNotice(ASSIGNED)` để notify tạo "Có bài mới" (US-S3E4-03-SC1, TC-S3E2-14). | Thấp | — |
| Dev-S3E4-03 | Thông báo tạo đồng bộ trong cùng giao dịch với thao tác của giáo viên (DomainEvents + `@EventListener`), không có hàng đợi ngoài; người nhận chốt lúc sự kiện (học sinh Đang học của lớp, tối đa 500/lớp). | Thấp | TẠM (một nút). |
| Dev-S3E4-04 | `notify/` dùng chung với US-S3E6-03 (cùng task): `NotificationRepository.insertAll` thêm bản có giờ tạo theo sự kiện + tùy chọn "mỗi em một thông báo cùng loại của cùng bộ"; `NotificationService` thêm loại `due_soon` (MSG-S3-95) và `SOLUTION_RELEASED` (lời giải mở theo lịch, dùng loại `solution_open`). Bản ghi `AssignmentSetNotice` thêm thành phần `dueAt` (giữ hàm dựng 9 tham số cũ). V387 đổi CHECK `notification_kind_check`. | Thấp | Ghi lại cho task S3-E6 (s3-e6) khi gộp. |
| Dev-S3E4-05 | TC-S3E4-02, 21, 27 là Manual (thiết bị thật) — không có test tự động; giao diện điện thoại có spec `follow.p0m.ui.spec.ts`. | Thấp | Tester chạy tay. |
| Dev-S3E4-06 | Theo lệnh owner (lượt 2): e2e chỉ viết + typecheck (`npx tsc --noEmit -p .`), CHƯA chạy; `make e2e-affected` và traceability chạy khi tích hợp nhóm epic. `make bench-mvp` (task đụng `assignment`) không có trong "Xong khi" mới nên chưa chạy. | Vừa | Phiên chính chạy khi tích hợp. |

## Dev US-S3E6-03 nhắc việc (ghi ở đây để không đụng `S3-E6/qc_findings.md` của task s3-e6)
| Mã | Nội dung | Mức | Xử lý TẠM |
|---|---|---|---|
| Dev-S3E6R-01 | Tác vụ nhắc nằm ở module assignment (`ReminderService`, `ReminderRepository`, cron `geo.s3e6-reminders.cron` mỗi phút giờ VN, "-" để tắt) vì cần dữ liệu bộ / lớp / lượt thử; chỉ phát `AssignmentSetNotice` (DUE_SOON, SOLUTION_RELEASED), notify tạo thông báo. Không có kênh gửi ngoài. | Thấp | — |
| Dev-S3E6R-02 | Chống gửi lặp: eventId tất định UUID(loại, bộ, giờ hạn / giờ mở) + ràng buộc `notification_event_user_uq`. Đổi hạn ⇒ eventId mới, nhắc theo hạn cũ không còn được tính (coi như hủy). Đã nhận nhắc rồi mà giáo viên lùi hạn thì nhận thêm một nhắc theo hạn mới. | Thấp | PO xác nhận. |
| Dev-S3E6R-03 | Giờ yên lặng 21:00–06:00 chỉ áp cho nhắc "sắp hết hạn" (BR-S3E6-21 nói về "giờ nhắc"); "lời giải đã mở" theo lịch gửi đúng giờ mở. Lời giải theo lịch chỉ xét bộ có giờ mở trong 1 ngày gần nhất và sau lúc giao; em đã nhận "lời giải đã mở" của bộ (Mở ngay) thì không nhận lần hai. | Thấp | PO xác nhận. |
| Dev-S3E6R-04 | MSG-S3-95 mẫu "Bộ {tên bộ} sắp hết hạn: …" nhưng AC SC1 ghi "Bộ bài tuần 15 sắp hết hạn" với tên bộ "Bộ bài tuần 15": tên đã bắt đầu bằng "Bộ " thì không thêm chữ "Bộ". | Thấp | BA sửa mẫu MSG nếu cần. |
| Dev-S3E6R-05 | "Em chưa xong" = còn ít nhất một đề chưa có lượt Đúng và chưa dùng hết lượt Sai (cùng định nghĩa "xong" của màn học sinh S3-E2). Bộ giao khi còn dưới 24 giờ (assigned_at > hạn − 24 giờ): không nhắc. | Thấp | — |
| Dev-S3E6R-06 | IT `it/s3e6/ReminderS3E6IT` (13 ca: TC-S3E6-27…37, 39, 40) giả lập đồng hồ qua `ReminderService.runAt`; e2e `tests/s3e6/reminders.api.spec.ts` (TC-S3E6-38) chạy tác vụ qua móc `/test-hooks/s3e6/reminders/run` (dev/test). Đổi hạn (TC-33) đặt cột `due_at` trực tiếp ở IT. Không đụng `features/s3e6/index.tsx`; giao diện chỉ thêm loại `due_soon` vào kiểu của `features/s3e4/api.ts` (màn thông báo S3-E4 hiện mọi loại). | Thấp | — |

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-27 | Mục Dev đầu tiên | runner-T-261126-31 |
