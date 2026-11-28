-- T-261126-32 · S3-E6 US-S3E6-01 (chữa bài từ kết quả sang Trình chiếu) và US-S3E6-02 (xuất kết quả lớp).
-- Khoảng migration của task: V415–V439 (PLAN_code-s23 §4).

-- Nhật ký xuất kết quả lớp: chỉ để giới hạn 10 lần / 1 giờ mỗi giáo viên (BR-S3E6-13). Không lưu nội dung tệp,
-- không tên lớp, không tên học sinh.
CREATE TABLE class_export_log (
  id          uuid PRIMARY KEY,
  org_id      uuid NOT NULL,
  teacher_id  uuid NOT NULL,
  class_id    uuid NOT NULL,
  created_at  timestamptz NOT NULL DEFAULT now(),
  FOREIGN KEY (org_id, teacher_id) REFERENCES app_user (org_id, id) ON DELETE CASCADE,
  FOREIGN KEY (org_id, class_id) REFERENCES class (org_id, id) ON DELETE CASCADE
);
CREATE INDEX class_export_log_teacher_idx ON class_export_log (org_id, teacher_id, created_at);
COMMENT ON TABLE class_export_log IS 'US-S3E6-02: mỗi lần giáo viên xuất kết quả lớp (giới hạn 10 lần/giờ, BR-S3E6-13).';

-- Sự kiện đo M-S3-04 "chữa bài từ kết quả": chỉ mã giáo viên, mã đề, giờ máy chủ (BR-S3E6-05). Không tên, không mã
-- học sinh, không số liệu theo em.
CREATE TABLE present_from_results_event (
  id                 uuid PRIMARY KEY,
  org_id             uuid NOT NULL,
  teacher_id         uuid NOT NULL,
  problem_version_id uuid NOT NULL,
  created_at         timestamptz NOT NULL DEFAULT now(),
  FOREIGN KEY (org_id, teacher_id) REFERENCES app_user (org_id, id) ON DELETE CASCADE
);
CREATE INDEX present_from_results_event_idx ON present_from_results_event (org_id, teacher_id, problem_version_id, created_at);
COMMENT ON TABLE present_from_results_event IS 'US-S3E6-01 (M-S3-04): một dòng mỗi lần bấm "Chữa bài này" hợp lệ; các lần bấm trong 5 giây gộp một.';

-- Xóa tài khoản (US-S3E5-05, V415) dọn cả hai bảng mới.
CREATE OR REPLACE FUNCTION fn_account_purge_rows(p_org uuid, p_id uuid) RETURNS void
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
  DELETE FROM class_export_log    WHERE org_id = p_org AND teacher_id = p_id;
  DELETE FROM present_from_results_event WHERE org_id = p_org AND teacher_id = p_id;
  -- AuditEvent chỉ thêm: thay mã người dùng bằng mã "đã xóa" (UUID 0), giữ loại, giờ, đối tượng (BR-S3E5-53).
  PERFORM set_config('session_replication_role', 'replica', true);
  UPDATE audit_log SET actor_user_id = '00000000-0000-0000-0000-000000000000'
   WHERE org_id = p_org AND actor_user_id = p_id;
  UPDATE audit_log SET target_id = '00000000-0000-0000-0000-000000000000'
   WHERE org_id = p_org AND target_id = p_id;
  PERFORM set_config('session_replication_role', 'origin', true);
END $$;

GRANT SELECT, INSERT ON class_export_log TO geo_app;
GRANT DELETE ON class_export_log TO geo_app;
GRANT SELECT, INSERT ON present_from_results_event TO geo_app;
