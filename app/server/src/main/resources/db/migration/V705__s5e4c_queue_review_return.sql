-- T-261128-60 · S5-E4c: hàng chờ chấm (US-S5E4-05), chấm tay theo rubric + Duyệt (US-S5E4-06), trả bài theo lịch công bố
-- (US-S5E4-07). Sau cờ geo.s5.grading.enabled (bảng luôn tạo; API 404 khi tắt).
--   submission (+cột)  trạng thái chấm waiting → approved → returned (BR-S5E4-15); row_version cho khoá lạc quan
--                      (BR-S5E4-19); return_id = lượt Trả bài đang hiệu lực.
--   approved_grade     điểm chính thức: mỗi lần Duyệt / sửa một hàng mới (chỉ thêm; BR-S5E4-18, 19). student_id NULL sau
--                      khi xoá tài khoản (ẩn danh cho số liệu lớp, BR-S5E4-28). Không khoá ngoại tới submission: job năm
--                      học xoá bài nộp nhưng điểm chính thức giữ lại.
--   grade_audit        AuditEvent chấm: Duyệt, sửa (trước/sau từng tiêu chí), Trả bài, Thu hồi. Không nội dung bài làm.
--   grade_return       lượt Trả bài của một bài có chấm; thu hồi trong 15 phút (BR-S5E4-24, QS5-31).
--   fn_graded_solution lời giải của bài có chấm cho học sinh: chỉ khi bài của em Đã trả (hoặc giờ công bố cố định đã tới
--                      theo đồng hồ CSDL và bài đã duyệt) (BR-S5E4-20, 23; NFR-GEO-023). geo_app không đọc bảng solution.
--   notification       thêm loại 'graded_returned' (MSG-S5-39) và 'grade_updated' (sửa điểm bài đã trả).

ALTER TABLE submission
  ADD COLUMN grading_state text NOT NULL DEFAULT 'waiting' CHECK (grading_state IN ('waiting', 'approved', 'returned')),
  ADD COLUMN row_version   int  NOT NULL DEFAULT 1 CHECK (row_version >= 1),
  ADD COLUMN return_id     uuid,
  ADD COLUMN returned_at   timestamptz;
CREATE INDEX submission_queue_idx ON submission (org_id, assignment_id, grading_state);
COMMENT ON COLUMN submission.grading_state IS 'Trạng thái chấm (BR-S5E4-15): waiting = Chờ chấm / Đang xử lý (chưa có bản đọc chữ), approved, returned.';

CREATE TABLE approved_grade (
  id                 uuid PRIMARY KEY,
  org_id             uuid NOT NULL,
  submission_id      uuid,
  assignment_id      uuid NOT NULL,
  class_id           uuid NOT NULL,
  student_id         uuid,
  seq                int  NOT NULL CHECK (seq >= 1),
  submission_version int  NOT NULL CHECK (submission_version >= 1),
  rubric_version_id  uuid NOT NULL,
  criteria           jsonb NOT NULL,
  total              numeric(6,2) NOT NULL CHECK (total >= 0),
  comment            text NOT NULL DEFAULT '' CHECK (length(comment) <= 1000),
  approved_by        uuid NOT NULL,
  approved_at        timestamptz NOT NULL DEFAULT now(),
  academic_year      text NOT NULL CHECK (academic_year ~ '^[0-9]{4}-[0-9]{4}$'),
  UNIQUE (org_id, submission_id, seq),
  FOREIGN KEY (org_id, assignment_id) REFERENCES graded_assignment (org_id, id) ON DELETE CASCADE
);
CREATE INDEX approved_grade_student_idx ON approved_grade (org_id, student_id);
CREATE INDEX approved_grade_assignment_idx ON approved_grade (org_id, assignment_id);
COMMENT ON TABLE approved_grade IS 'ApprovedGrade (BR-S5E4-18, 19): chỉ thêm; bản hiện hành = seq lớn nhất của bài nộp. Chỉ giáo viên chủ lớp tạo.';

CREATE TABLE grade_audit (
  id            uuid PRIMARY KEY,
  org_id        uuid NOT NULL,
  assignment_id uuid NOT NULL,
  submission_id uuid,
  actor_id      uuid NOT NULL,
  action        text NOT NULL CHECK (action IN ('approve', 'edit', 'return', 'return_undo')),
  before_value  jsonb,
  after_value   jsonb,
  n             int  NOT NULL DEFAULT 1 CHECK (n >= 0),
  at            timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX grade_audit_submission_idx ON grade_audit (org_id, submission_id, at);
CREATE INDEX grade_audit_assignment_idx ON grade_audit (org_id, assignment_id, at);
COMMENT ON TABLE grade_audit IS 'AuditEvent chấm và trả bài (BR-S5E4-18, 19, 24): trước/sau theo tiêu chí, người thực hiện. Chỉ thêm.';

CREATE TABLE grade_return (
  id            uuid PRIMARY KEY,
  org_id        uuid NOT NULL,
  assignment_id uuid NOT NULL,
  returned_by   uuid NOT NULL,
  returned_at   timestamptz NOT NULL,
  n             int  NOT NULL CHECK (n >= 0),
  undone_at     timestamptz,
  FOREIGN KEY (org_id, assignment_id) REFERENCES graded_assignment (org_id, id) ON DELETE CASCADE
);
CREATE INDEX grade_return_assignment_idx ON grade_return (org_id, assignment_id, returned_at DESC);
COMMENT ON TABLE grade_return IS 'Lượt Trả bài (US-S5E4-07): thu hồi trong 15 phút (BR-S5E4-24, QS5-31) đưa bài về Đã duyệt, gỡ thông báo.';

-- Lời giải bài có chấm cho học sinh (BR-S5E4-20, 23). Luôn một hàng: LOCKED | NO_SOLUTION | OK (+ nội dung).
CREATE FUNCTION fn_graded_solution(p_org uuid, p_student uuid, p_assignment uuid)
RETURNS TABLE (status text, final_answer text, steps jsonb)
LANGUAGE plpgsql STABLE SECURITY DEFINER SET search_path = public, pg_temp
AS $$
#variable_conflict use_column
DECLARE g record; open boolean;
BEGIN
  SELECT ga.problem_version_id AS pv, ga.release_mode AS rm, ga.release_at AS ra INTO g
  FROM graded_assignment ga WHERE ga.org_id = p_org AND ga.id = p_assignment;
  IF NOT FOUND THEN
    RETURN QUERY SELECT 'LOCKED'::text, NULL::text, NULL::jsonb; RETURN;
  END IF;
  SELECT EXISTS (
    SELECT 1 FROM submission s
    WHERE s.org_id = p_org AND s.assignment_id = p_assignment AND s.student_id = p_student
      AND (s.grading_state = 'returned'
           OR (g.rm = 'at_time' AND g.ra <= now() AND s.grading_state = 'approved'))) INTO open;
  IF NOT open THEN
    RETURN QUERY SELECT 'LOCKED'::text, NULL::text, NULL::jsonb; RETURN;
  END IF;
  IF g.pv IS NULL THEN
    RETURN QUERY SELECT 'NO_SOLUTION'::text, NULL::text, NULL::jsonb; RETURN;
  END IF;
  RETURN QUERY SELECT 'OK'::text, s.final_answer, s.steps
    FROM problem_version pv
    JOIN solution s ON s.org_id = pv.org_id AND s.problem_version_id = pv.id
    WHERE pv.id = g.pv AND (pv.org_id = p_org OR pv.org_id = platform_org_id());
  IF NOT FOUND THEN
    RETURN QUERY SELECT 'NO_SOLUTION'::text, NULL::text, NULL::jsonb;
  END IF;
END $$;
COMMENT ON FUNCTION fn_graded_solution IS 'Lời giải bài có chấm (US-S5E4-07): chỉ học sinh có bài Đã trả (hoặc giờ công bố cố định đã tới theo đồng hồ CSDL và bài Đã duyệt). Em chưa nộp / Chờ chấm / Đã duyệt chưa trả: LOCKED.';
REVOKE ALL ON FUNCTION fn_graded_solution(uuid, uuid, uuid) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION fn_graded_solution(uuid, uuid, uuid) TO geo_app;

ALTER TABLE notification DROP CONSTRAINT notification_kind_check;
ALTER TABLE notification ADD CONSTRAINT notification_kind_check
  CHECK (kind IN ('new_set', 'solution_open', 'release_changed', 'due_soon', 'class_notice', 'ai_quota',
                  'graded_returned', 'grade_updated'));

GRANT SELECT, INSERT, UPDATE ON approved_grade TO geo_app;
GRANT SELECT, INSERT ON grade_audit TO geo_app;
GRANT SELECT, INSERT, UPDATE ON grade_return TO geo_app;
