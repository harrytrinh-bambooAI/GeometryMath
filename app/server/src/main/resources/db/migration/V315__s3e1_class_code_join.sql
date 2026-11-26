-- S3-E1 + US-S3E5-02, 04 (T-261126-28) · Lớp theo mô hình chặng 3 (PLAN_code-s23 A6, §2 iam): năm học, cách vào lớp
-- (MVP: tên + mã lớp · S3: mã lớp + giáo viên duyệt), công tắc "Cho vào lớp bằng mã", công tắc trợ lý AI phía học sinh;
-- bảng đếm lượt nhập mã sai (PO-66 G4), nhật ký đặt lại mật khẩu (giới hạn tần suất BR-S3E1-35), sự kiện bảo mật khi bị
-- từ chối vì quan hệ (BR-S3E1-54, M-S3-06). Không thu dữ liệu cá nhân mới (PO-61): không IP, không thiết bị.

-- ---------------------------------------------------------------- class: năm học, cách vào lớp, công tắc
ALTER TABLE class
  ADD COLUMN academic_year     text,
  ADD COLUMN join_mode         text NOT NULL DEFAULT 'name',
  ADD COLUMN code_join_enabled boolean NOT NULL DEFAULT true,
  ADD COLUMN ai_enabled        boolean NOT NULL DEFAULT true,
  ADD CONSTRAINT class_join_mode_ck CHECK (join_mode IN ('name', 'code')),
  ADD CONSTRAINT class_academic_year_ck CHECK (
       academic_year IS NULL
    OR (academic_year ~ '^[0-9]{4}–[0-9]{4}$'
        AND substr(academic_year, 6, 4)::int = substr(academic_year, 1, 4)::int + 1));
COMMENT ON COLUMN class.academic_year IS 'Năm học dạng YYYY–YYYY (gạch ngang dài), năm sau = năm trước + 1 (BR-S3E1-02). NULL ở lớp MVP.';
COMMENT ON COLUMN class.join_mode IS 'name = lớp MVP (học sinh vào bằng tên + mã lớp, fn_student_login) · code = lớp chặng 3 (mã HK-, giáo viên duyệt từng em, PO-66 G4).';
COMMENT ON COLUMN class.code_join_enabled IS 'Công tắc "Cho vào lớp bằng mã" (BR-S3E1-06). Tắt: mã bị từ chối như mã sai.';
COMMENT ON COLUMN class.ai_enabled IS 'Trợ lý AI phía học sinh của lớp (US-S3E5-04), mặc định bật; không ảnh hưởng "AI gộp" phía giáo viên.';

-- Lớp chặng 3 không nhận đường vào bằng tên của MVP (không có bước duyệt): chỉ lớp join_mode = 'name'.
CREATE OR REPLACE FUNCTION fn_student_login(p_join_code text, p_name_key text, p_display_name text, p_new_user_id uuid)
RETURNS TABLE (status text, org_id uuid, class_id uuid, level edu_level, user_id uuid, created boolean)
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
#variable_conflict use_column
DECLARE c record; v_user uuid;
BEGIN
  SELECT x.id, x.org_id, x.level INTO c FROM class x
   WHERE x.join_code = p_join_code AND x.archived_at IS NULL AND x.join_mode = 'name';
  IF c.id IS NULL THEN
    RETURN QUERY SELECT 'NOT_FOUND'::text, NULL::uuid, NULL::uuid, NULL::edu_level, NULL::uuid, false; RETURN;
  END IF;
  SELECT cm.user_id INTO v_user FROM class_member cm
   WHERE cm.org_id = c.org_id AND cm.class_id = c.id AND cm.name_key = p_name_key AND cm.left_at IS NULL;
  IF v_user IS NULL AND p_new_user_id IS NOT NULL THEN
    PERFORM pg_advisory_xact_lock(hashtextextended(c.id::text || '|' || p_name_key, 0));
    SELECT cm.user_id INTO v_user FROM class_member cm
     WHERE cm.org_id = c.org_id AND cm.class_id = c.id AND cm.name_key = p_name_key AND cm.left_at IS NULL;
    IF v_user IS NULL THEN
      INSERT INTO app_user (id, org_id, role, account_kind, display_name) VALUES (p_new_user_id, c.org_id, 'student', 'light', p_display_name);
      INSERT INTO class_member (org_id, class_id, user_id, name_key) VALUES (c.org_id, c.id, p_new_user_id, p_name_key);
      RETURN QUERY SELECT 'OK'::text, c.org_id, c.id, c.level, p_new_user_id, true; RETURN;
    END IF;
  END IF;
  IF v_user IS NULL THEN
    RETURN QUERY SELECT 'NOT_FOUND'::text, c.org_id, c.id, c.level, NULL::uuid, false; RETURN;
  END IF;
  RETURN QUERY SELECT 'OK'::text, c.org_id, c.id, c.level, v_user, false;
END $$;

-- ---------------------------------------------------------------- class_member: trạng thái ↔ left_at
-- Mọi truy vấn MVP/S2 coi "left_at IS NULL" là đang ở trong lớp. Để em Chờ duyệt / Bị từ chối không thấy bài lớp ở các
-- truy vấn cũ (BR-S3E1-12, 53), left_at luôn có giá trị khi status <> 'active' (mốc của lần đổi trạng thái); status
-- 'active' thì left_at NULL. Mã cũ chỉ đặt left_at (cho rời lớp MVP) thì status suy ra 'left'.
CREATE FUNCTION trg_class_member_status_sync() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
  IF TG_OP = 'INSERT' THEN
    IF NEW.status = 'active' AND NEW.left_at IS NOT NULL THEN
      NEW.status := 'left';
    ELSIF NEW.status <> 'active' AND NEW.left_at IS NULL THEN
      NEW.left_at := now();
    END IF;
  ELSIF NEW.status IS DISTINCT FROM OLD.status THEN
    IF NEW.status = 'active' THEN
      NEW.left_at := NULL;
    ELSIF NEW.left_at IS NULL OR NEW.left_at IS NOT DISTINCT FROM OLD.left_at THEN
      NEW.left_at := now();
    END IF;
  ELSIF NEW.left_at IS DISTINCT FROM OLD.left_at THEN
    NEW.status := CASE WHEN NEW.left_at IS NULL THEN 'active'::class_member_status ELSE 'left'::class_member_status END;
  END IF;
  RETURN NEW;
END $$;
CREATE TRIGGER class_member_status_sync BEFORE INSERT OR UPDATE ON class_member
  FOR EACH ROW EXECUTE FUNCTION trg_class_member_status_sync();
UPDATE class_member SET left_at = joined_at WHERE status <> 'active' AND left_at IS NULL;

-- Danh sách chờ duyệt / học sinh của một lớp (Q mới S3-E1): theo trạng thái rồi tên.
CREATE INDEX class_member_class_status_idx ON class_member (org_id, class_id, status, name_key, user_id);

-- Yêu cầu vào lớp bằng mã (BR-S3E1-11, 15): một hàng mỗi (lớp, học sinh); mới → Chờ duyệt; Đã rời / Bị từ chối → Chờ
-- duyệt mới; Chờ duyệt / Đang học giữ nguyên. Trả trạng thái trước lệnh ('new', 'pending', 'active', 'left', 'rejected').
CREATE FUNCTION fn_class_join_request(p_org uuid, p_class uuid, p_user uuid, p_name_key text)
RETURNS text LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
DECLARE v_prev class_member_status;
BEGIN
  SELECT cm.status INTO v_prev FROM class_member cm
   WHERE cm.org_id = p_org AND cm.class_id = p_class AND cm.user_id = p_user FOR UPDATE;
  IF v_prev IS NULL THEN
    INSERT INTO class_member (org_id, class_id, user_id, name_key, status, joined_at)
    VALUES (p_org, p_class, p_user, p_name_key, 'pending', now())
    ON CONFLICT (org_id, class_id, user_id) DO NOTHING;
    IF NOT FOUND THEN
      SELECT cm.status INTO v_prev FROM class_member cm
       WHERE cm.org_id = p_org AND cm.class_id = p_class AND cm.user_id = p_user;
      RETURN v_prev::text;
    END IF;
    RETURN 'new';
  END IF;
  IF v_prev IN ('left', 'rejected') THEN
    UPDATE class_member SET status = 'pending', joined_at = now(), name_key = p_name_key
     WHERE org_id = p_org AND class_id = p_class AND user_id = p_user;
  END IF;
  RETURN v_prev::text;
END $$;
COMMENT ON FUNCTION fn_class_join_request IS 'US-S3E1-02: học sinh nhập đúng mã lớp chặng 3 → Chờ duyệt (không có đường vào thẳng Đang học, PO-66 G4).';

-- ---------------------------------------------------------------- lượt nhập mã lớp sai (BR-S3E1-14, 17)
CREATE TABLE class_join_attempt (
  id           uuid PRIMARY KEY,
  org_id       uuid NOT NULL,
  user_id      uuid NOT NULL,
  attempted_at timestamptz NOT NULL DEFAULT now(),
  FOREIGN KEY (org_id, user_id) REFERENCES app_user (org_id, id) ON DELETE CASCADE
);
COMMENT ON TABLE class_join_attempt IS 'Một hàng = một lần nhập mã lớp sai của tài khoản học sinh (10 lần/15 phút, 30 lần/24 giờ; đếm theo tài khoản, không theo mã). Không lưu mã đã gõ, IP hay thiết bị. Hàng quá 24 giờ được xóa khi ghi lượt mới.';
CREATE INDEX class_join_attempt_user_idx ON class_join_attempt (org_id, user_id, attempted_at);

-- ---------------------------------------------------------------- đặt lại mật khẩu học sinh (US-S3E1-04)
ALTER TABLE app_user ADD COLUMN temp_password_expires_at timestamptz;
COMMENT ON COLUMN app_user.temp_password_expires_at IS 'Hạn mật khẩu tạm do giáo viên đặt lại (24 giờ, BR-S3E1-34); NULL khi không có mật khẩu tạm. Mật khẩu tạm chỉ lưu băm ở password_hash.';

CREATE TABLE student_password_reset (
  id          uuid PRIMARY KEY,
  org_id      uuid NOT NULL,
  teacher_id  uuid NOT NULL,
  student_id  uuid NOT NULL,
  class_id    uuid NOT NULL,
  created_at  timestamptz NOT NULL DEFAULT now(),
  FOREIGN KEY (org_id, teacher_id) REFERENCES app_user (org_id, id) ON DELETE CASCADE,
  FOREIGN KEY (org_id, student_id) REFERENCES app_user (org_id, id) ON DELETE CASCADE
);
COMMENT ON TABLE student_password_reset IS 'Lần đặt lại mật khẩu thành công (đếm giới hạn 10 lần/giờ/giáo viên, BR-S3E1-35). Không chứa mật khẩu hay băm. Nhật ký thao tác (AuditEvent PASSWORD_RESET) do module assignment ghi từ sự kiện miền (US-S3E5-01, task 31).';
CREATE INDEX student_password_reset_teacher_idx ON student_password_reset (org_id, teacher_id, created_at);

-- ---------------------------------------------------------------- sự kiện bảo mật: bị từ chối vì quan hệ (BR-S3E1-54)
CREATE TABLE relation_denial (
  id           uuid PRIMARY KEY,
  org_id       uuid NOT NULL,
  actor_id     uuid NOT NULL,
  kind         text NOT NULL CHECK (kind IN ('NOT_FOUND', 'FORBIDDEN', 'INVALID_CURSOR')),
  object_type  text NOT NULL CHECK (object_type ~ '^[a-z_]{1,40}$'),
  at           timestamptz NOT NULL DEFAULT now()
);
COMMENT ON TABLE relation_denial IS 'Một hàng = một yêu cầu bị từ chối vì quan hệ (ngoài lớp, sai vai, con trỏ lạ) để đếm M-S3-06. Chỉ loại, mã người gọi, loại đối tượng, giờ máy chủ; không nội dung yêu cầu.';
CREATE INDEX relation_denial_org_at_idx ON relation_denial (org_id, at);

-- ---------------------------------------------------------------- văn bản riêng tư bản 1.0 nêu đủ ý BR-S3E5-25
UPDATE privacy_notice SET body =
'Ứng dụng chỉ thu tên hiển thị, mã người dùng, vai (giáo viên hoặc học sinh), lớp và bài làm. Chúng tôi không thu email, số điện thoại, ngày sinh, trường hay ảnh, và không xác minh phụ huynh.
Mã người dùng dùng để đăng nhập. Học sinh quên mật khẩu thì dùng mã khôi phục của mình hoặc nhờ giáo viên của lớp đặt lại. Giáo viên mất cả mật khẩu lẫn mã khôi phục thì không lấy lại được tài khoản.
Bài làm, tiến độ và câu hỏi gửi trợ lý được lưu để thầy cô của lớp theo dõi việc học. Bạn có thể xóa tài khoản cùng dữ liệu bài làm.'
WHERE version = '1.0';

-- ---------------------------------------------------------------- quyền (geo_app)
GRANT UPDATE (join_code, academic_year, join_mode, code_join_enabled, ai_enabled) ON class TO geo_app;
GRANT UPDATE (name_key, joined_at) ON class_member TO geo_app;
GRANT EXECUTE ON FUNCTION fn_class_join_request(uuid, uuid, uuid, text) TO geo_app;
GRANT SELECT, INSERT, DELETE ON class_join_attempt TO geo_app;
GRANT UPDATE (attempted_at) ON class_join_attempt TO geo_app;
GRANT SELECT, INSERT ON student_password_reset TO geo_app;
GRANT UPDATE (created_at) ON student_password_reset TO geo_app;
GRANT SELECT, INSERT ON relation_denial TO geo_app;
GRANT UPDATE (temp_password_expires_at) ON app_user TO geo_app;
