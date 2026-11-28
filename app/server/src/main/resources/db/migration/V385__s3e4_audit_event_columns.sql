-- S3-E4 + US-S3E5-01 (T-261126-31, PLAN_code-s23 A8): AuditEvent = mở rộng audit_log của MVP (module assignment).
-- Bước 1/2: giá trị enum mới + cột mới. Giá trị enum vừa thêm chưa dùng được trong CÙNG giao dịch, nên chỉ mục có điều
-- kiện, hàm ghi và bảng notification nằm ở V386.
--   * early_release        Mở ngay một bộ đã giao (BR-S3E4-11, BR-S3E5-01)
--   * release_time_change  Đổi giờ mở sau khi bộ Đã giao (BR-S3E5-06: đặt giờ khi còn Nháp không ghi)
--   * password_reset       Giáo viên đặt lại mật khẩu học sinh (nghe iam.api.StudentPasswordResetEvent)
-- Cột mới (BR-S3E5-02): vai người thực hiện, loại đối tượng, lớp, kết quả, khóa chống lặp (BR-S3E4-16).
-- Bản ghi Từ chối (BR-S3E5-07) CHỈ có loại, người gọi, loại đối tượng, giờ: target_id, class_id để trống.
ALTER TYPE audit_action ADD VALUE IF NOT EXISTS 'early_release';
ALTER TYPE audit_action ADD VALUE IF NOT EXISTS 'release_time_change';
ALTER TYPE audit_action ADD VALUE IF NOT EXISTS 'password_reset';

ALTER TABLE audit_log
  ADD COLUMN actor_role  text CHECK (actor_role ~ '^[a-z_]{1,20}$'),
  ADD COLUMN target_type text CHECK (target_type IN ('assignment_set', 'user')),
  ADD COLUMN class_id    uuid,
  ADD COLUMN outcome     text NOT NULL DEFAULT 'success' CHECK (outcome IN ('success', 'denied')),
  ADD COLUMN request_key text CHECK (length(request_key) BETWEEN 1 AND 64);
ALTER TABLE audit_log ALTER COLUMN target_id DROP NOT NULL;
ALTER TABLE audit_log ADD CONSTRAINT audit_log_target_ck CHECK (outcome = 'denied' OR target_id IS NOT NULL);
ALTER TABLE audit_log ADD CONSTRAINT audit_log_denied_ck
  CHECK (outcome = 'success' OR (target_id IS NULL AND class_id IS NULL AND before_release_at IS NULL
                                 AND after_release_at IS NULL AND request_key IS NULL));

COMMENT ON COLUMN audit_log.outcome IS 'success (Thành công) | denied (Từ chối vì vai hoặc quan hệ, BR-S3E5-07: không mã đối tượng, không lớp, không nội dung yêu cầu).';
COMMENT ON COLUMN audit_log.class_id IS 'Lớp của AuditEvent chặng 3 (nhật ký theo lớp, M-S3-05); NULL với hàng MVP và hàng Từ chối.';
COMMENT ON COLUMN audit_log.request_key IS 'Khóa chống lặp của yêu cầu (BR-S3E4-16): cùng người, cùng loại, cùng đối tượng, cùng khóa chỉ một hàng.';
