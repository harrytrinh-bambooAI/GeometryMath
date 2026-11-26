-- S2-E1 (T-261126-18) · Định danh tối giản PO-61 (PLAN_code-s23 A6): mã người dùng GV-/HS-, mật khẩu băm, mã khôi phục
-- băm, cờ phải đổi mật khẩu, cờ đã đọc thông báo riêng tư; khóa đăng nhập/khôi phục theo tài khoản (BR-S2E1-09, -11);
-- mốc hủy phiên (QA-S2E1-01); bảng privacy_notice; class_member.status (A6, cần cho S2-E8 "Đang học").
-- Không thu dữ liệu cá nhân (BR-S2E1-01): không có cột email, số điện thoại, ngày sinh, trường, ảnh.

-- ---------------------------------------------------------------- app_user: định danh mã người dùng
ALTER TABLE app_user
  ADD COLUMN user_code             text,
  ADD COLUMN password_hash         text,
  ADD COLUMN recovery_hmac         bytea,
  ADD COLUMN must_change_password  boolean NOT NULL DEFAULT false,
  ADD COLUMN privacy_ack_version   text,
  ADD COLUMN privacy_ack_at        timestamptz,
  ADD COLUMN login_fail_count      integer NOT NULL DEFAULT 0,
  ADD COLUMN login_locked_until    timestamptz,
  ADD COLUMN recovery_fail_count   integer NOT NULL DEFAULT 0,
  ADD COLUMN recovery_window_start timestamptz,
  ADD COLUMN recovery_locked_until timestamptz,
  ADD COLUMN sessions_valid_after  timestamptz,
  ADD CONSTRAINT app_user_code_ck CHECK (user_code IS NULL OR user_code ~ '^(GV|HS)-[A-HJ-NP-Z2-9]{4}-[A-HJ-NP-Z2-9]{4}$'),
  ADD CONSTRAINT app_user_code_role_ck CHECK (
       user_code IS NULL
    OR (user_code LIKE 'GV-%' AND role = 'teacher')
    OR (user_code LIKE 'HS-%' AND role = 'student')),
  ADD CONSTRAINT app_user_code_cred_ck CHECK (user_code IS NULL OR (password_hash IS NOT NULL AND recovery_hmac IS NOT NULL)),
  ADD CONSTRAINT app_user_recovery_len_ck CHECK (recovery_hmac IS NULL OR length(recovery_hmac) = 32),
  ADD CONSTRAINT app_user_fail_ck CHECK (login_fail_count >= 0 AND recovery_fail_count >= 0);

COMMENT ON COLUMN app_user.user_code IS 'Mã người dùng PO-61: GV-/HS- + 8 ký tự bảng 32 ký tự (bỏ O, 0, I, 1). Duy nhất toàn hệ thống; lưu chữ hoa, tra cứu không phân biệt hoa thường do ứng dụng chuẩn hóa (BR-S2E1-07).';
COMMENT ON COLUMN app_user.password_hash IS 'PBKDF2-HMAC-SHA256: "pbkdf2-sha256$<vòng>$<muối b64>$<băm b64>". Không bao giờ lưu mật khẩu rõ.';
COMMENT ON COLUMN app_user.recovery_hmac IS 'HMAC-SHA256(pepper, "rc:"+mã khôi phục) — mã khôi phục 60 bit, chỉ hiện một lần (BR-S2E1-05); dùng xong thay mã mới (BR-S2E1-10).';
COMMENT ON COLUMN app_user.sessions_valid_after IS 'Thẻ có iat trước mốc này bị từ chối (đổi mật khẩu bằng khôi phục hủy phiên khác, QA-S2E1-01).';

-- Đăng nhập bằng mã: tra theo mã (trước đăng nhập chưa biết trường, như mã lớp D-GEO-051).
CREATE UNIQUE INDEX app_user_user_code_uq ON app_user (user_code) WHERE user_code IS NOT NULL;

-- ---------------------------------------------------------------- văn bản thông báo riêng tư (PO-64, PO-66 G5)
CREATE TABLE privacy_notice (
  version      text PRIMARY KEY CHECK (version ~ '^[0-9]+\.[0-9]+$'),
  title        text NOT NULL CHECK (length(title) BETWEEN 1 AND 120),
  body         text NOT NULL CHECK (length(body) BETWEEN 1 AND 8000),
  published_at timestamptz NOT NULL DEFAULT now(),
  is_current   boolean NOT NULL DEFAULT false
);
COMMENT ON TABLE privacy_notice IS 'Văn bản thông báo riêng tư theo phiên bản (US-S2E1-01 tick "đã đọc"; đổi phiên bản ở US-S3E5-02). Một phiên bản hiện hành.';
CREATE UNIQUE INDEX privacy_notice_current_uq ON privacy_notice ((true)) WHERE is_current;

INSERT INTO privacy_notice (version, title, body, is_current) VALUES ('1.0', 'Thông báo riêng tư',
'Ứng dụng chỉ hỏi tên hiển thị, vai (giáo viên hoặc học sinh) và mật khẩu do bạn tự đặt. Chúng tôi không hỏi email, số điện thoại, ngày sinh, trường hay ảnh, và không xác minh phụ huynh.
Mã người dùng dùng để đăng nhập; mã khôi phục dùng khi quên mật khẩu và chỉ hiện một lần. Mất cả mật khẩu lẫn mã khôi phục thì không lấy lại được tài khoản.
Bài làm, tiến độ và câu hỏi gửi trợ lý được lưu để thầy cô của lớp theo dõi việc học. Bạn có thể xóa tài khoản cùng dữ liệu bài làm.',
true);

-- ---------------------------------------------------------------- class_member.status (A6; S3-E1 dùng pending/rejected)
CREATE TYPE class_member_status AS ENUM ('pending', 'active', 'left', 'rejected');
ALTER TABLE class_member ADD COLUMN status class_member_status NOT NULL DEFAULT 'active';
UPDATE class_member SET status = 'left' WHERE left_at IS NOT NULL;
COMMENT ON COLUMN class_member.status IS 'pending (Chờ duyệt) | active (Đang học, mặc định — giữ nghĩa MVP) | left | rejected. Hàng MVP: active, hoặc left khi left_at có giá trị.';

-- ---------------------------------------------------------------- đăng ký bằng mã (đường DUY NHẤT tạo tài khoản có mã)
-- geo_app không có INSERT trên app_user (TC-GEO-0863): tạo qua hàm SECURITY DEFINER, chỉ được vai student/light hoặc
-- teacher/verified, luôn có mã + băm mật khẩu + băm mã khôi phục. Trường "Cộng đồng" (kind = group, con của platform)
-- chứa tài khoản tự đăng ký; tạo nếu chưa có. Trả false nếu mã người dùng đã có (ứng dụng sinh mã khác, thử lại).
CREATE FUNCTION fn_register_code_user(p_id uuid, p_org uuid, p_org_name text, p_teacher boolean, p_display_name text,
                                      p_user_code text, p_password_hash text, p_recovery_hmac bytea,
                                      p_privacy_version text, p_at timestamptz)
RETURNS boolean LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
BEGIN
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
COMMENT ON FUNCTION fn_register_code_user IS 'US-S2E1-01: tạo tài khoản mã người dùng (PO-61). Không nhận email, số điện thoại, ngày sinh, trường, ảnh.';

-- ---------------------------------------------------------------- quyền (geo_app)
GRANT SELECT ON privacy_notice TO geo_app;
GRANT EXECUTE ON FUNCTION fn_register_code_user(uuid, uuid, text, boolean, text, text, text, bytea, text, timestamptz) TO geo_app;
GRANT UPDATE (user_code, password_hash, recovery_hmac, must_change_password, privacy_ack_version, privacy_ack_at,
              login_fail_count, login_locked_until, recovery_fail_count, recovery_window_start, recovery_locked_until,
              sessions_valid_after) ON app_user TO geo_app;
GRANT UPDATE (status) ON class_member TO geo_app;
