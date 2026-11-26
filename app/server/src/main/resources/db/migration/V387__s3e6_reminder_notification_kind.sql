-- US-S3E6-03 (T-261126-31, mở rộng): nhắc việc trong ứng dụng dùng lại bảng notification (US-S3E4-03).
-- Thêm loại 'due_soon' (MSG-S3-95, BR-S3E6-20). "Lời giải đã mở theo lịch" dùng loại 'solution_open' sẵn có.
-- Chống gửi lặp (BR-S3E6-25): event_id tất định theo (bộ, loại, giờ) + ràng buộc notification_event_user_uq.
ALTER TABLE notification DROP CONSTRAINT notification_kind_check;
ALTER TABLE notification ADD CONSTRAINT notification_kind_check
  CHECK (kind IN ('new_set', 'solution_open', 'release_changed', 'due_soon'));

-- Tác vụ nhắc chạy mỗi phút: tìm bộ Đã giao có hạn hoặc giờ mở lời giải gần "bây giờ".
CREATE INDEX assignment_set_due_reminder_idx ON assignment_set (due_at) WHERE status = 'assigned' AND due_at IS NOT NULL;
CREATE INDEX assignment_set_release_reminder_idx ON assignment_set (release_at) WHERE status = 'assigned' AND release_at IS NOT NULL;

-- Thông báo "lời giải đã mở" theo lịch: kiểm em đã có thông báo loại này của bộ chưa (Mở ngay đã gửi thì thôi).
CREATE INDEX notification_set_kind_idx ON notification (org_id, set_id, kind, user_id) WHERE set_id IS NOT NULL;
