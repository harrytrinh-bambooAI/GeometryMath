-- S3-E4 + US-S3E5-01 (T-261126-31). Bước 2/2 (sau V385): chỉ mục + hàm ghi AuditEvent; bảng notification (PLAN A9).

-- ---------------------------------------------------------------- AuditEvent
-- Nhật ký theo lớp, mới nhất trước (BR-S3E5-08).
CREATE INDEX audit_log_class_idx ON audit_log (org_id, class_id, id DESC)
  WHERE class_id IS NOT NULL AND outcome = 'success';
-- M-S3-05: đếm sự kiện thành công theo lớp và loại (US-S3E5-01-FC9), không quét toàn bảng.
CREATE INDEX audit_log_class_action_idx ON audit_log (org_id, class_id, action)
  WHERE class_id IS NOT NULL AND outcome = 'success';
-- M-S3-06 + móc kiểm thử: bản ghi Từ chối của một người gọi.
CREATE INDEX audit_log_denied_idx ON audit_log (org_id, actor_user_id, id DESC) WHERE outcome = 'denied';
-- BR-S3E4-16: gửi lặp cùng khóa chỉ một AuditEvent.
CREATE UNIQUE INDEX audit_log_request_key_uq ON audit_log (org_id, actor_user_id, action, target_id, request_key)
  WHERE request_key IS NOT NULL;

-- Đường DUY NHẤT ứng dụng ghi AuditEvent chặng 3 (geo_app không INSERT audit_log; bảng chỉ thêm, trigger V10 chặn
-- UPDATE/DELETE). Chỉ nhận ba loại của BR-S3E5-01. Hàng Từ chối bỏ mọi trường ngoài loại, người gọi, loại đối tượng.
CREATE FUNCTION fn_audit_event(p_org uuid, p_actor uuid, p_actor_role text, p_action text, p_target_type text,
                               p_target uuid, p_class uuid, p_before timestamptz, p_after timestamptz,
                               p_outcome text, p_request_key text)
RETURNS bigint
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
DECLARE v_id bigint;
BEGIN
  IF p_action NOT IN ('early_release', 'release_time_change', 'password_reset') THEN
    RAISE EXCEPTION 'fn_audit_event: loại % không được phép', p_action;
  END IF;
  IF p_outcome = 'denied' THEN
    INSERT INTO audit_log (org_id, actor_user_id, actor_role, action, target_type, outcome)
    VALUES (p_org, p_actor, p_actor_role, p_action::audit_action, p_target_type, 'denied')
    RETURNING id INTO v_id;
  ELSE
    INSERT INTO audit_log (org_id, actor_user_id, actor_role, action, target_type, target_id, class_id,
                           before_release_at, after_release_at, outcome, request_key)
    VALUES (p_org, p_actor, p_actor_role, p_action::audit_action, p_target_type, p_target, p_class,
            p_before, p_after, 'success', p_request_key)
    RETURNING id INTO v_id;
  END IF;
  RETURN v_id;
END $$;
COMMENT ON FUNCTION fn_audit_event IS 'Ghi một AuditEvent chặng 3 (EARLY_RELEASE, RELEASE_TIME_CHANGE, PASSWORD_RESET) vào audit_log; gọi trong CÙNG giao dịch với thao tác nhạy cảm (BR-S3E5-04). Không nhận mật khẩu hay nội dung yêu cầu.';
GRANT EXECUTE ON FUNCTION fn_audit_event(uuid, uuid, text, text, text, uuid, uuid, timestamptz, timestamptz, text, text) TO geo_app;

-- ---------------------------------------------------------------- Notification trong ứng dụng (module notify)
-- Một hàng = một thông báo của một người nhận. Nội dung chỉ có tên bộ và giờ (BR-S3E4-25): không đáp số, không tên bạn.
-- Không có cột địa chỉ gửi ngoài (email, điện thoại, mã đăng ký đẩy): chỉ trong ứng dụng (BR-S3E4-26, PO-65).
CREATE TABLE notification (
  id         uuid PRIMARY KEY,                       -- UUIDv7 (sắp theo thời gian, khóa phân trang)
  org_id     uuid NOT NULL,
  user_id    uuid NOT NULL,                          -- người nhận
  kind       text NOT NULL CHECK (kind IN ('new_set', 'solution_open', 'release_changed')),
  event_id   uuid NOT NULL,                          -- sự kiện miền sinh ra thông báo
  set_id     uuid,                                   -- bộ bài để mở khi bấm
  body       text NOT NULL CHECK (length(body) BETWEEN 1 AND 240),
  created_at timestamptz NOT NULL DEFAULT now(),
  read_at    timestamptz,                            -- NULL = Chưa đọc
  CONSTRAINT notification_event_user_uq UNIQUE (org_id, event_id, user_id),   -- BR-S3E4-22
  FOREIGN KEY (org_id, user_id) REFERENCES app_user (org_id, id) ON DELETE CASCADE
);
CREATE INDEX notification_user_idx ON notification (org_id, user_id, id DESC);
CREATE INDEX notification_unread_idx ON notification (org_id, user_id) WHERE read_at IS NULL;
CREATE INDEX notification_created_idx ON notification (created_at);
COMMENT ON TABLE notification IS 'Thông báo trong ứng dụng (S3-E4; S3-E6 nhắc việc dùng lại). Giữ 90 ngày rồi xóa (BR-S3E4-27). Chỉ người nhận đọc.';

GRANT SELECT, INSERT, DELETE ON notification TO geo_app;
GRANT UPDATE (read_at) ON notification TO geo_app;
