-- T-261126-32 · S3-E5 US-S3E5-05: xóa tài khoản và chính sách lưu/xóa dữ liệu bài làm (BR-S3E5-50…55, PO-64, PO-66 G5).
-- Khoảng migration của task: V415–V439 (PLAN_code-s23 §4).
--
-- Cách làm:
--   * Xóa có hiệu lực ngay (fn_account_delete): tài khoản thành "bia mộ" (deleted_at, tên "Đã xóa", không mã, không mật
--     khẩu, không mã khôi phục, mọi phiên bị hủy); bài làm, lượt hỏi AI, thành viên lớp, cờ đã đọc, nhật ký đo của
--     người đó bị xóa vật lý NGAY (bảng lớp và số liệu tính lại không có em). AuditEvent cũ giữ loại, giờ, đối tượng;
--     mã người dùng thay bằng mã "đã xóa" (UUID 0) — BR-S3E5-53.
--   * Mã người dùng không cấp lại: chỉ giữ băm SHA-256 của mã ở retired_user_code (không còn bản ghi mang mã rõ).
--   * Công việc dọn (fn_account_purge, chạy hằng ngày): bia mộ cũ hơn 30 ngày bị xóa vật lý nếu không còn nội dung
--     dùng chung trỏ tới (đề, lời giải, lớp đã lưu trữ của giáo viên); còn thì giữ bia mộ ẩn danh.

ALTER TABLE app_user ADD COLUMN deleted_at timestamptz;
COMMENT ON COLUMN app_user.deleted_at IS 'US-S3E5-05: giờ máy chủ lúc chủ tài khoản xóa tài khoản; khác NULL = bia mộ ẩn danh, chờ dọn sau 30 ngày.';

CREATE TABLE retired_user_code (
  code_sha256 bytea PRIMARY KEY CHECK (length(code_sha256) = 32),
  retired_at  timestamptz NOT NULL DEFAULT now()
);
COMMENT ON TABLE retired_user_code IS 'US-S3E5-05 (BR-S3E5-51): băm mã người dùng của tài khoản đã xóa, để mã không bao giờ được cấp lại. Không giữ mã rõ, không giữ mã tài khoản.';

CREATE FUNCTION fn_user_code_sha256(p_code text) RETURNS bytea LANGUAGE sql IMMUTABLE
AS $$ SELECT sha256(convert_to('geo-user-code:' || p_code, 'UTF8')) $$;

-- Đăng ký (US-S2E1-01) không cấp mã đã nghỉ: false như trùng mã, người gọi sinh mã khác và thử lại.
CREATE OR REPLACE FUNCTION fn_register_code_user(p_id uuid, p_org uuid, p_org_name text, p_teacher boolean, p_display_name text,
                                                 p_user_code text, p_password_hash text, p_recovery_hmac bytea,
                                                 p_privacy_version text, p_at timestamptz)
RETURNS boolean LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
BEGIN
  IF EXISTS (SELECT 1 FROM retired_user_code WHERE code_sha256 = fn_user_code_sha256(p_user_code)) THEN
    RETURN false;
  END IF;
  INSERT INTO org (id, parent_id, kind, name) VALUES (p_org, platform_org_id(), 'group', p_org_name)
  ON CONFLICT (id) DO NOTHING;
  INSERT INTO app_user (id, org_id, role, account_kind, display_name, user_code, password_hash, recovery_hmac,
                        privacy_ack_version, privacy_ack_at)
  VALUES (p_id, p_org,
          CASE WHEN p_teacher THEN 'teacher'::user_role ELSE 'student'::user_role END,
          CASE WHEN p_teacher THEN 'verified'::account_kind ELSE 'light'::account_kind END,
          p_display_name, p_user_code, p_password_hash, p_recovery_hmac, p_privacy_version, p_at)
  ON CONFLICT (user_code) WHERE user_code IS NOT NULL DO NOTHING;
  RETURN FOUND;
END $$;

-- Dữ liệu riêng của một người (bài làm, lượt hỏi AI, thành viên lớp, nhật ký đo, đếm thử sai, quyền phụ…).
CREATE FUNCTION fn_account_purge_rows(p_org uuid, p_id uuid) RETURNS void
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
BEGIN
  DELETE FROM assignment_progress WHERE org_id = p_org AND user_id = p_id;
  DELETE FROM practice_attempt    WHERE org_id = p_org AND user_id = p_id;
  DELETE FROM practice_report     WHERE org_id = p_org AND user_id = p_id;
  DELETE FROM qa_turn             WHERE org_id = p_org AND user_id = p_id;
  DELETE FROM event_log           WHERE org_id = p_org AND user_id = p_id;
  DELETE FROM ai_call_log         WHERE org_id = p_org AND user_id = p_id;
  DELETE FROM class_member        WHERE org_id = p_org AND user_id = p_id;
  DELETE FROM class_join_attempt  WHERE org_id = p_org AND user_id = p_id;
  DELETE FROM student_password_reset WHERE org_id = p_org AND (student_id = p_id OR teacher_id = p_id);
  DELETE FROM relation_denial     WHERE org_id = p_org AND actor_id = p_id;
  DELETE FROM export_confirm_log  WHERE org_id = p_org AND user_id = p_id;
  DELETE FROM problem_create_event WHERE org_id = p_org AND user_id = p_id;
  DELETE FROM user_preference     WHERE org_id = p_org AND user_id = p_id;
  DELETE FROM user_role_grant     WHERE org_id = p_org AND user_id = p_id;
  DELETE FROM teacher_access_code WHERE org_id = p_org AND user_id = p_id;
  DELETE FROM auth_failure        WHERE user_id = p_id;
  DELETE FROM notification        WHERE org_id = p_org AND user_id = p_id;   -- thông báo trong ứng dụng (V386, S3-E4)
  -- AuditEvent chỉ thêm: thay mã người dùng bằng mã "đã xóa" (UUID 0), giữ loại, giờ, đối tượng (BR-S3E5-53).
  PERFORM set_config('session_replication_role', 'replica', true);
  UPDATE audit_log SET actor_user_id = '00000000-0000-0000-0000-000000000000'
   WHERE org_id = p_org AND actor_user_id = p_id;
  UPDATE audit_log SET target_id = '00000000-0000-0000-0000-000000000000'
   WHERE org_id = p_org AND target_id = p_id;
  PERFORM set_config('session_replication_role', 'origin', true);
END $$;

-- Xóa tài khoản của chính mình (người gọi đã kiểm mật khẩu, chữ XÓA, lớp Đang dạy). false nếu đã xóa trước đó.
CREATE FUNCTION fn_account_delete(p_org uuid, p_id uuid, p_at timestamptz) RETURNS boolean
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
DECLARE v_code text;
BEGIN
  SELECT user_code INTO v_code FROM app_user
   WHERE org_id = p_org AND id = p_id AND deleted_at IS NULL FOR UPDATE;
  IF NOT FOUND THEN
    RETURN false;
  END IF;
  IF v_code IS NOT NULL THEN
    INSERT INTO retired_user_code (code_sha256, retired_at) VALUES (fn_user_code_sha256(v_code), p_at)
    ON CONFLICT DO NOTHING;
  END IF;
  UPDATE app_user
     SET deleted_at = p_at, deactivated_at = coalesce(deactivated_at, p_at), display_name = 'Đã xóa',
         user_code = NULL, password_hash = NULL, recovery_hmac = NULL, must_change_password = false,
         privacy_ack_version = NULL, privacy_ack_at = NULL, login_fail_count = 0, login_locked_until = NULL,
         recovery_fail_count = 0, recovery_window_start = NULL, recovery_locked_until = NULL,
         sessions_valid_after = p_at, temp_password_expires_at = NULL
   WHERE org_id = p_org AND id = p_id;
  PERFORM fn_account_purge_rows(p_org, p_id);
  RETURN true;
END $$;

-- Công việc dọn: bia mộ có deleted_at <= p_cutoff (giờ máy chủ − 30 ngày) bị xóa vật lý nếu không còn nội dung dùng
-- chung trỏ tới; trả số tài khoản đã xóa hẳn.
CREATE FUNCTION fn_account_purge(p_cutoff timestamptz) RETURNS integer
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
DECLARE r record; n integer := 0;
BEGIN
  FOR r IN SELECT org_id, id FROM app_user WHERE deleted_at IS NOT NULL AND deleted_at <= p_cutoff LOOP
    PERFORM fn_account_purge_rows(r.org_id, r.id);
    BEGIN
      DELETE FROM app_user WHERE org_id = r.org_id AND id = r.id;
      n := n + 1;
    EXCEPTION WHEN foreign_key_violation THEN
      NULL; -- giáo viên còn đề, lời giải, lớp đã lưu trữ: giữ bia mộ ẩn danh
    END;
  END LOOP;
  RETURN n;
END $$;

COMMENT ON FUNCTION fn_account_delete IS 'US-S3E5-05: xóa tài khoản của chính mình (BR-S3E5-51, 53).';
COMMENT ON FUNCTION fn_account_purge IS 'US-S3E5-05 SC4: dọn vật lý tài khoản đã xóa quá 30 ngày (BR-S3E5-51).';

-- Văn bản thông báo riêng tư bản hiện hành nêu chính sách lưu và xóa (BR-S3E5-54). Sửa tại chỗ như V315 (Dev-S3E5-01):
-- tăng phiên bản buộc mọi tài khoản đọc lại.
UPDATE privacy_notice
   SET body = replace(body,
                      'Bạn có thể xóa tài khoản cùng dữ liệu bài làm.',
                      'Bài làm được giữ tới khi bạn xóa tài khoản. Nhật ký truy cập của máy chủ web (có địa chỉ IP) giữ không quá 7 ngày. '
                      || 'Khi bạn xóa tài khoản (Cài đặt > Xóa tài khoản), dữ liệu được xóa trong tối đa 30 ngày.')
 WHERE version = '1.0';

GRANT EXECUTE ON FUNCTION fn_account_delete(uuid, uuid, timestamptz) TO geo_app;
GRANT EXECUTE ON FUNCTION fn_account_purge(timestamptz) TO geo_app;
GRANT SELECT ON retired_user_code TO geo_app;
