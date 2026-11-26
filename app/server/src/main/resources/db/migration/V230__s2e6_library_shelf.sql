-- S2-E6 (T-261126-23, PLAN_code-s23 §1 A10, §2; US-S2E6-01…03). Chủ: iam (vai người soát), content (ngăn, Kho chung).
--   user_role_grant      (iam)    : vai thêm của tài khoản do CHỦ SẢN PHẨM gán (BR-S2E6-07, QA-S2E6-01). Chặng 2 chỉ có
--                                   'content_reviewer'. Ứng dụng chỉ ĐỌC; gán bằng seed / lệnh vận hành (không có API gán).
--   library_submission   (content): đề xuất một đề "Của tôi" (solution_doc) vào Kho chung, gắn MỘT phiên bản lời giải
--                                   "Giáo viên đã duyệt" (bất biến). Trạng thái pending → accepted | rejected (kèm lý do
--                                   5…500 ký tự). Không xóa: lịch sử giữ dòng từ chối cũ (BR-S2E6-06). Mỗi đề nhiều nhất
--                                   một dòng pending (gửi lại khi đang Chờ soát không tạo dòng thứ hai).
--   library_problem      (content): bản Kho chung ĐỘC LẬP chép lúc duyệt (BR-S2E6-10): đề bài, hình, loại hình, bước giải,
--                                   nhãn. Sửa bản Của tôi sau đó không đổi bản này. Kho chung theo tổ chức (cộng đồng giáo
--                                   viên chặng 2 cùng một tổ chức "Cộng đồng"); TẠM · vừa · PLAN A10.
--   problem_create_event (content): bộ đếm thêm đề theo TÀI KHOẢN, cửa sổ trượt 60 phút, chung mọi đường tạo đề (text,
--                                   image, import, copy) (BR-S2E6-13, PO-63). Đường tạo đề của S2-E3/S2-E4 gọi
--                                   content.api.LibraryQuota trước khi ghi.
-- Bước giải của Kho chung KHÔNG có SELECT cho geo_app (cùng nguyên tắc TC-GEO-0666): đọc qua fn_library_steps_teacher
-- (chỉ giáo viên đang hoạt động cùng tổ chức) và fn_library_submission_steps (chỉ người soát). Học sinh không có đường.

-- ---------------------------------------------------------------- iam: vai người soát nội dung
CREATE TABLE user_role_grant (
  org_id     uuid NOT NULL,
  user_id    uuid NOT NULL,
  role       text NOT NULL CHECK (role IN ('content_reviewer')),
  granted_by text NOT NULL CHECK (length(granted_by) BETWEEN 1 AND 60),
  granted_at timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (org_id, user_id, role),
  FOREIGN KEY (org_id, user_id) REFERENCES app_user (org_id, id)
);
COMMENT ON TABLE user_role_grant IS 'Vai thêm do chủ sản phẩm gán (S2-E6, QA-S2E6-01). Ứng dụng chỉ đọc.';

-- ---------------------------------------------------------------- content: ngăn Của tôi / Kho chung
CREATE TYPE library_submission_status AS ENUM ('pending', 'accepted', 'rejected');

CREATE TABLE library_submission (
  id            uuid PRIMARY KEY,
  org_id        uuid NOT NULL,
  doc_id        uuid NOT NULL,
  version_id    uuid NOT NULL,
  owner_user_id uuid NOT NULL,
  status        library_submission_status NOT NULL DEFAULT 'pending',
  reason        text CHECK (reason IS NULL OR length(reason) BETWEEN 5 AND 500),
  submitted_at  timestamptz NOT NULL DEFAULT now(),
  decided_by    uuid,
  decided_at    timestamptz,
  library_id    uuid,
  UNIQUE (org_id, id),
  FOREIGN KEY (org_id, doc_id) REFERENCES solution_doc (org_id, id),
  FOREIGN KEY (org_id, version_id) REFERENCES solution_version (org_id, id),
  FOREIGN KEY (org_id, owner_user_id) REFERENCES app_user (org_id, id),
  FOREIGN KEY (org_id, decided_by) REFERENCES app_user (org_id, id),
  CONSTRAINT library_submission_reason_ck CHECK ((status = 'rejected') = (reason IS NOT NULL)),
  CONSTRAINT library_submission_decided_ck CHECK ((status = 'pending') = (decided_at IS NULL)),
  CONSTRAINT library_submission_self_ck CHECK (decided_by IS NULL OR decided_by <> owner_user_id)
);
CREATE UNIQUE INDEX library_submission_pending_uq ON library_submission (org_id, doc_id) WHERE status = 'pending';
CREATE INDEX library_submission_queue_idx ON library_submission (org_id, status, submitted_at, id);
CREATE INDEX library_submission_owner_idx ON library_submission (org_id, owner_user_id, status);
CREATE INDEX library_submission_doc_idx ON library_submission (org_id, doc_id, submitted_at DESC);
CREATE TRIGGER library_submission_immutable BEFORE UPDATE ON library_submission
  FOR EACH ROW EXECUTE FUNCTION tg_immutable_cols('org_id', 'doc_id', 'version_id', 'owner_user_id', 'submitted_at');

CREATE TABLE library_problem (
  id             uuid PRIMARY KEY,
  org_id         uuid NOT NULL,
  submission_id  uuid NOT NULL,
  source_doc_id  uuid NOT NULL,
  owner_user_id  uuid NOT NULL,
  title          text NOT NULL CHECK (length(title) BETWEEN 1 AND 200),
  statement      text NOT NULL CHECK (length(statement) BETWEEN 1 AND 8000),
  figure         jsonb,
  kind           problem_kind NOT NULL,
  steps          jsonb NOT NULL CHECK (jsonb_typeof(steps) = 'array' AND jsonb_array_length(steps) BETWEEN 1 AND 30),
  hint           text CHECK (hint IS NULL OR length(hint) BETWEEN 1 AND 500),
  machine_status verify_status NOT NULL,
  human_status   solution_human_status NOT NULL CHECK (human_status = 'teacher_approved'),
  approved_code  text,
  accepted_by    uuid NOT NULL,
  accepted_at    timestamptz NOT NULL,
  UNIQUE (org_id, id),
  UNIQUE (org_id, submission_id),
  FOREIGN KEY (org_id, submission_id) REFERENCES library_submission (org_id, id),
  FOREIGN KEY (org_id, accepted_by) REFERENCES app_user (org_id, id)
);
CREATE INDEX library_problem_list_idx ON library_problem (org_id, accepted_at DESC, id DESC);
COMMENT ON TABLE library_problem IS 'Bản Kho chung độc lập (S2-E6, BR-S2E6-10). Không sửa, không xóa. steps chỉ đọc qua fn_library_steps_teacher.';

CREATE TABLE problem_create_event (
  id         uuid PRIMARY KEY,
  org_id     uuid NOT NULL,
  user_id    uuid NOT NULL,
  channel    text NOT NULL CHECK (channel IN ('text', 'image', 'import', 'copy')),
  ref_id     uuid,
  created_at timestamptz NOT NULL,
  FOREIGN KEY (org_id, user_id) REFERENCES app_user (org_id, id)
);
CREATE INDEX problem_create_event_user_idx ON problem_create_event (org_id, user_id, created_at DESC);

-- Gợi ý (không lộ lời giải) do giáo viên ghi lúc gửi soát; chép sang Kho chung khi duyệt.
ALTER TABLE library_submission ADD COLUMN hint text CHECK (hint IS NULL OR length(hint) BETWEEN 1 AND 500);

-- ---------------------------------------------------------------- cổng đọc / chép bước giải
-- Người soát (vai content_reviewer, cùng tổ chức) đọc bước giải của đề ĐANG Chờ soát (BR-S2E6-11: chỉ đề Chờ soát).
CREATE FUNCTION fn_library_submission_steps(p_org uuid, p_user uuid, p_submission uuid)
RETURNS TABLE (steps jsonb)
LANGUAGE sql STABLE SECURITY DEFINER SET search_path = public, pg_temp
AS $$
  SELECT v.steps
  FROM library_submission s
  JOIN solution_version v ON v.org_id = s.org_id AND v.id = s.version_id
  JOIN user_role_grant g ON g.org_id = s.org_id AND g.user_id = p_user AND g.role = 'content_reviewer'
  JOIN app_user u ON u.org_id = g.org_id AND u.id = g.user_id AND u.role = 'teacher' AND u.deactivated_at IS NULL
  WHERE s.org_id = p_org AND s.id = p_submission AND s.status = 'pending'
$$;

-- Giáo viên đang hoạt động cùng tổ chức đọc bước giải Kho chung (BR-S2E6-14: vai Giáo viên xem ngay).
CREATE FUNCTION fn_library_steps_teacher(p_org uuid, p_user uuid, p_item uuid)
RETURNS TABLE (steps jsonb)
LANGUAGE sql STABLE SECURITY DEFINER SET search_path = public, pg_temp
AS $$
  SELECT l.steps
  FROM library_problem l
  JOIN app_user u ON u.org_id = l.org_id AND u.id = p_user AND u.role = 'teacher' AND u.deactivated_at IS NULL
  WHERE l.org_id = p_org AND l.id = p_item
$$;

-- Duyệt vào Kho chung: chép đề, hình, bước giải, nhãn của phiên bản đã gửi thành bản độc lập; dữ liệu không rời CSDL.
-- Kiểm lại vai người soát và "không tự soát" ngay trong hàm (phòng thủ hai lớp; tầng dịch vụ đã kiểm).
CREATE FUNCTION fn_library_accept(p_org uuid, p_reviewer uuid, p_submission uuid, p_item uuid, p_at timestamptz)
RETURNS integer
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
DECLARE n integer;
BEGIN
  INSERT INTO library_problem (id, org_id, submission_id, source_doc_id, owner_user_id, title, statement, figure, kind,
                               steps, hint, machine_status, human_status, approved_code, accepted_by, accepted_at)
  SELECT p_item, s.org_id, s.id, d.id, s.owner_user_id, d.title, d.statement, d.figure,
         CASE WHEN d.figure ? 'plane' THEN 'plane' ELSE 'space' END::problem_kind,
         v.steps, s.hint, v.machine_status, 'teacher_approved', v.approved_code, p_reviewer, p_at
  FROM library_submission s
  JOIN solution_doc d ON d.org_id = s.org_id AND d.id = s.doc_id
  JOIN solution_version v ON v.org_id = s.org_id AND v.id = s.version_id AND v.human_status IN ('teacher_approved', 'superseded')
  JOIN user_role_grant g ON g.org_id = s.org_id AND g.user_id = p_reviewer AND g.role = 'content_reviewer'
  WHERE s.org_id = p_org AND s.id = p_submission AND s.status = 'pending' AND s.owner_user_id <> p_reviewer;
  GET DIAGNOSTICS n = ROW_COUNT;
  IF n = 1 THEN
    UPDATE library_submission SET status = 'accepted', decided_by = p_reviewer, decided_at = p_at, library_id = p_item
    WHERE org_id = p_org AND id = p_submission;
  END IF;
  RETURN n;
END $$;

-- ---------------------------------------------------------------- quyền theo vai (mẫu V160)
GRANT SELECT ON user_role_grant TO geo_app;
GRANT SELECT, INSERT ON library_submission TO geo_app;
GRANT UPDATE (status, reason, decided_by, decided_at) ON library_submission TO geo_app;
GRANT SELECT (id, org_id, submission_id, source_doc_id, owner_user_id, title, statement, figure, kind, hint,
              machine_status, human_status, approved_code, accepted_by, accepted_at) ON library_problem TO geo_app;
GRANT SELECT, INSERT ON problem_create_event TO geo_app;
REVOKE ALL ON FUNCTION fn_library_submission_steps(uuid, uuid, uuid), fn_library_steps_teacher(uuid, uuid, uuid),
                       fn_library_accept(uuid, uuid, uuid, uuid, timestamptz) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION fn_library_submission_steps(uuid, uuid, uuid), fn_library_steps_teacher(uuid, uuid, uuid),
                          fn_library_accept(uuid, uuid, uuid, uuid, timestamptz) TO geo_app;
