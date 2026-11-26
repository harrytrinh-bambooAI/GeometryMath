-- S2-E8 (T-261126-21, PLAN_code-s23 A4/A5; PO-66 G1; US-S2E8-02…05).
-- 1) MỘT mô hình "bộ bài" (BR-S2E8-32): assignment_set + assignment_item (module assignment), đủ cột cho S3-E2
--    (status draft/assigned/closed/archived, due_at nhãn, release_at theo bộ và theo item, cấu hình luyện theo item,
--    ảnh chụp đáp số lúc giao). Khi giao, mỗi item sinh MỘT hàng assignment MVP (kind = class) để dùng lại cổng
--    release_at, khóa đáp số (fn_solution_gate), assignment_progress và bộ chặn moi đáp án. Không có bảng bài luyện riêng.
-- 2) practice_attempt (module tracking, A5): lượt thử, khóa chống lặp (assignment_id, user_id, request_key).
-- 3) practice_report (module tracking): báo "Em nghĩ đáp số này đúng" (BR-S2E8-30, 31; M-S2-07).

CREATE TYPE assignment_set_status AS ENUM ('draft', 'assigned', 'closed', 'archived');
CREATE TYPE practice_answer_kind  AS ENUM ('exact', 'approx');

CREATE TABLE assignment_set (
  id            uuid PRIMARY KEY,                    -- UUIDv7
  org_id        uuid NOT NULL,
  owner_user_id uuid NOT NULL,                       -- giáo viên soạn
  class_id      uuid,                                -- lớp được giao (NULL khi còn nháp)
  title         text NOT NULL CHECK (length(title) BETWEEN 1 AND 80),
  status        assignment_set_status NOT NULL DEFAULT 'draft',
  due_at        timestamptz,                         -- nhãn hạn làm (S3-E2), không khóa gì
  release_at    timestamptz,                         -- giờ mở lời giải theo bộ; NULL = chưa đặt (khóa)
  assigned_at   timestamptz,
  closed_at     timestamptz,
  archived_at   timestamptz,
  created_at    timestamptz NOT NULL DEFAULT now(),
  updated_at    timestamptz NOT NULL DEFAULT now(),
  UNIQUE (org_id, id),
  FOREIGN KEY (org_id, owner_user_id) REFERENCES app_user (org_id, id),
  FOREIGN KEY (org_id, class_id)      REFERENCES class (org_id, id),
  CONSTRAINT assignment_set_class_ck CHECK (status = 'draft' OR class_id IS NOT NULL)
);
CREATE INDEX assignment_set_owner_idx ON assignment_set (org_id, owner_user_id, id DESC);
CREATE INDEX assignment_set_class_idx ON assignment_set (org_id, class_id, id DESC) WHERE status <> 'draft';
COMMENT ON TABLE assignment_set IS 'Bộ bài (AssignmentSet, PO-66 G1): bài luyện S2-E8 và bộ bài S3-E2 dùng chung. Nháp sửa tự do; giao qua fn_assign_practice_set.';

CREATE TABLE assignment_item (
  id                 uuid PRIMARY KEY,
  org_id             uuid NOT NULL,
  set_id             uuid NOT NULL,
  position           smallint NOT NULL CHECK (position BETWEEN 1 AND 20),
  content_org_id     uuid NOT NULL,
  problem_id         uuid NOT NULL,
  problem_version_id uuid NOT NULL,
  answer_kind        practice_answer_kind NOT NULL DEFAULT 'exact',
  tolerance          numeric(4, 2) CHECK (tolerance BETWEEN 0 AND 10),      -- chỉ kiểu xấp xỉ
  max_attempts       smallint NOT NULL DEFAULT 3 CHECK (max_attempts BETWEEN 1 AND 5),
  release_at         timestamptz,                    -- giờ mở riêng của item (S3-E2); NULL = theo bộ
  assignment_id      uuid,                           -- hàng assignment MVP sinh lúc giao
  answer_snapshot    text CHECK (length(answer_snapshot) BETWEEN 1 AND 500),  -- đáp số chụp lúc giao (BR-S2E8-29)
  accepted_forms     jsonb NOT NULL DEFAULT '[]',    -- dạng viết giáo viên xác nhận thêm (BR-S2E8-31)
  created_at         timestamptz NOT NULL DEFAULT now(),
  UNIQUE (org_id, id),
  UNIQUE (org_id, set_id, position),
  UNIQUE (org_id, set_id, problem_id),
  FOREIGN KEY (org_id, set_id) REFERENCES assignment_set (org_id, id) ON DELETE CASCADE,
  FOREIGN KEY (content_org_id, problem_id, problem_version_id) REFERENCES problem_version (org_id, problem_id, id),
  FOREIGN KEY (org_id, assignment_id) REFERENCES assignment (org_id, id),
  CONSTRAINT assignment_item_tolerance_ck CHECK ((answer_kind = 'approx') = (tolerance IS NOT NULL)),
  CONSTRAINT assignment_item_snapshot_ck  CHECK ((assignment_id IS NULL) = (answer_snapshot IS NULL))
);
CREATE UNIQUE INDEX assignment_item_assignment_uq ON assignment_item (org_id, assignment_id) WHERE assignment_id IS NOT NULL;
COMMENT ON TABLE assignment_item IS 'Câu của bộ bài (AssignmentItem) kèm cấu hình luyện. answer_snapshot + assignment_id chỉ do fn_assign_practice_set ghi.';

CREATE TABLE practice_attempt (
  id            uuid PRIMARY KEY,
  org_id        uuid NOT NULL,
  assignment_id uuid NOT NULL,                       -- hàng assignment MVP của item
  user_id       uuid NOT NULL,
  request_key   text NOT NULL CHECK (length(request_key) BETWEEN 1 AND 64),   -- mã chống lặp (BR-S2E8-14)
  submitted     text NOT NULL CHECK (length(submitted) <= 200),
  outcome       text NOT NULL CHECK (outcome IN ('correct', 'wrong', 'syntax')),
  detail        jsonb NOT NULL DEFAULT '{}',         -- dạng chuẩn, lý do cú pháp, lời nhắc (để trả lại y nguyên khi gửi lặp)
  wrong_after   smallint NOT NULL CHECK (wrong_after >= 0),                   -- số lượt Sai sau lượt này
  source        text NOT NULL DEFAULT 'student' CHECK (source IN ('student', 'teacher_confirm')),
  created_at    timestamptz NOT NULL DEFAULT now(),
  UNIQUE (org_id, assignment_id, user_id, request_key),
  FOREIGN KEY (org_id, assignment_id) REFERENCES assignment (org_id, id),
  FOREIGN KEY (org_id, user_id)       REFERENCES app_user (org_id, id)
);
CREATE INDEX practice_attempt_user_idx ON practice_attempt (org_id, user_id, assignment_id, created_at);
COMMENT ON TABLE practice_attempt IS 'Lượt thử bài luyện (Attempt). Chỉ ghi qua fn_record_practice_attempt (khóa theo câu + học sinh, chống lặp, chặn sau khi Đúng/hết lượt).';

CREATE TABLE practice_report (
  id            uuid PRIMARY KEY,
  org_id        uuid NOT NULL,
  assignment_id uuid NOT NULL,
  user_id       uuid NOT NULL,
  submitted     text NOT NULL CHECK (length(submitted) <= 200),               -- đáp số em nhập bị chấm Sai
  note          text CHECK (length(note) <= 200),
  status        text NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'confirmed', 'kept')),
  created_at    timestamptz NOT NULL DEFAULT now(),
  decided_at    timestamptz,
  decided_by    uuid,
  UNIQUE (org_id, assignment_id, user_id),                                    -- mỗi em một báo mỗi câu (BR-S2E8-30)
  FOREIGN KEY (org_id, assignment_id) REFERENCES assignment (org_id, id),
  FOREIGN KEY (org_id, user_id)       REFERENCES app_user (org_id, id),
  FOREIGN KEY (org_id, decided_by)    REFERENCES app_user (org_id, id)
);
CREATE INDEX practice_report_list_idx ON practice_report (org_id, created_at DESC, id DESC);
COMMENT ON TABLE practice_report IS 'Báo "Em nghĩ đáp số này đúng" gửi giáo viên; confirmed đếm vào M-S2-07.';

-- ---------------------------------------------------------------- đáp số tham chiếu cho giáo viên soạn bài
-- geo_app không đọc bảng solution: giáo viên của trường lấy đáp số đề kho (bộ trường thấy) qua hàm này để máy chủ
-- xét "đề có đáp số máy chấm được" (BR-S2E8-27) và chấm thử ở "Xem trước như học sinh" (BR-S2E8-28).
CREATE FUNCTION fn_practice_reference(p_org uuid, p_user uuid, p_versions uuid[])
RETURNS TABLE (problem_version_id uuid, final_answer text)
LANGUAGE plpgsql STABLE SECURITY DEFINER SET search_path = public, pg_temp
AS $$
#variable_conflict use_column
BEGIN
  IF NOT EXISTS (SELECT 1 FROM app_user u WHERE u.org_id = p_org AND u.id = p_user AND u.role = 'teacher'
                 AND u.deactivated_at IS NULL) THEN
    RETURN;
  END IF;
  RETURN QUERY
  SELECT pv.id, s.final_answer
  FROM problem_version pv
  JOIN problem p ON p.org_id = pv.org_id AND p.id = pv.problem_id
  JOIN solution s ON s.org_id = pv.org_id AND s.problem_version_id = pv.id
  WHERE pv.org_id = platform_org_id() AND pv.id = ANY (p_versions) AND p.source = 'catalog' AND p.archived_at IS NULL
    AND fn_catalog_bank_visible(p_org, p.bank);
END $$;
COMMENT ON FUNCTION fn_practice_reference IS 'Đáp số đề kho cho GIÁO VIÊN soạn bài luyện (S2-E8). Không bao giờ gọi cho học sinh.';

-- ---------------------------------------------------------------- giao bộ bài
-- Đường DUY NHẤT chuyển bộ Nháp thành Đã giao: chủ bộ phải là giáo viên của lớp (sai => NOT_FOUND, BR-S2E8-32),
-- mỗi item sinh một hàng assignment MVP (cùng ràng buộc fn_create_assignment: đề kho thuộc bộ trường thấy, một đề một
-- lần mỗi lớp D-GEO-161) và chụp đáp số từ solution. Giờ mở đã qua chỉ nhận khi p_allow_past (lời giải mở ngay).
CREATE FUNCTION fn_assign_practice_set(p_org uuid, p_actor uuid, p_set uuid, p_class uuid, p_allow_past boolean,
                                       p_request uuid DEFAULT NULL)
RETURNS text
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
DECLARE s record; c record; it record; v_answer text; v_rel timestamptz; v_aid uuid; v_ok boolean;
BEGIN
  SELECT x.owner_user_id, x.status, x.release_at INTO s
    FROM assignment_set x WHERE x.org_id = p_org AND x.id = p_set FOR UPDATE;
  IF s.owner_user_id IS NULL OR s.owner_user_id <> p_actor THEN RETURN 'NOT_FOUND'; END IF;
  SELECT x.teacher_id, x.archived_at INTO c FROM class x WHERE x.org_id = p_org AND x.id = p_class;
  IF c.teacher_id IS NULL OR c.archived_at IS NOT NULL OR c.teacher_id <> p_actor THEN RETURN 'NOT_FOUND'; END IF;
  IF s.status <> 'draft' THEN RETURN 'CONFLICT_ASSIGNED'; END IF;
  IF NOT EXISTS (SELECT 1 FROM assignment_item i WHERE i.org_id = p_org AND i.set_id = p_set) THEN RETURN 'EMPTY'; END IF;
  IF NOT COALESCE(p_allow_past, false) AND EXISTS (
       SELECT 1 FROM assignment_item i WHERE i.org_id = p_org AND i.set_id = p_set
          AND COALESCE(i.release_at, s.release_at) <= now()) THEN
    RETURN 'PAST_RELEASE';
  END IF;
  BEGIN
    FOR it IN SELECT i.id, i.content_org_id, i.problem_id, i.problem_version_id, i.release_at
                FROM assignment_item i WHERE i.org_id = p_org AND i.set_id = p_set ORDER BY i.position LOOP
      SELECT true INTO v_ok FROM problem p
       WHERE p.org_id = platform_org_id() AND p.id = it.problem_id AND it.content_org_id = platform_org_id()
         AND p.source = 'catalog' AND p.archived_at IS NULL AND fn_catalog_bank_visible(p_org, p.bank);
      IF v_ok IS NULL THEN RAISE EXCEPTION 'ITEM_NOT_FOUND' USING ERRCODE = 'GS281'; END IF;
      v_ok := NULL;
      SELECT so.final_answer INTO v_answer FROM solution so
       WHERE so.org_id = it.content_org_id AND so.problem_version_id = it.problem_version_id;
      IF v_answer IS NULL THEN RAISE EXCEPTION 'NO_ANSWER' USING ERRCODE = 'GS282'; END IF;
      v_rel := COALESCE(it.release_at, s.release_at);
      IF v_rel IS NOT NULL AND v_rel < now() THEN v_rel := now(); END IF;
      v_aid := uuid_v7();
      INSERT INTO assignment (id, org_id, kind, class_id, content_org_id, problem_id, problem_version_id, assigned_by,
                              release_at, allow_hint, allow_qa, created_at)
      VALUES (v_aid, p_org, 'class', p_class, it.content_org_id, it.problem_id, it.problem_version_id, p_actor,
              v_rel, true, true, now());
      INSERT INTO audit_log (org_id, actor_user_id, action, target_id, before_release_at, after_release_at, request_id)
      VALUES (p_org, p_actor, 'assignment_create', v_aid, NULL, v_rel, p_request);
      UPDATE assignment_item SET assignment_id = v_aid, answer_snapshot = v_answer WHERE org_id = p_org AND id = it.id;
      v_answer := NULL;
    END LOOP;
  EXCEPTION
    WHEN unique_violation THEN RETURN 'CONFLICT_DUPLICATE';
    WHEN SQLSTATE 'GS281' THEN RETURN 'ITEM_NOT_FOUND';
    WHEN SQLSTATE 'GS282' THEN RETURN 'NO_ANSWER';
  END;
  UPDATE assignment_set SET status = 'assigned', class_id = p_class, assigned_at = now(), updated_at = now()
   WHERE org_id = p_org AND id = p_set;
  RETURN 'OK';
END $$;
COMMENT ON FUNCTION fn_assign_practice_set IS 'Giao bộ bài (S2-E8/S3-E2): mỗi item một hàng assignment MVP + ảnh chụp đáp số. Trả OK | NOT_FOUND | CONFLICT_ASSIGNED | EMPTY | PAST_RELEASE | CONFLICT_DUPLICATE | ITEM_NOT_FOUND | NO_ANSWER.';

-- ---------------------------------------------------------------- ghi lượt thử (nguyên tử theo câu + học sinh)
-- Gửi lặp cùng mã: trả hàng đã ghi (DUPLICATE), không trừ lượt lần hai (BR-S2E8-14). Đã Đúng / hết lượt: không ghi
-- (BR-S2E8-13). Chỉ outcome 'wrong' tăng wrong_after (BR-S2E8-07).
CREATE FUNCTION fn_record_practice_attempt(p_org uuid, p_user uuid, p_assignment uuid, p_key text, p_submitted text,
                                           p_outcome text, p_detail jsonb, p_max integer, p_source text DEFAULT 'student')
RETURNS TABLE (status text, outcome text, submitted text, detail jsonb, wrong_after integer)
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
#variable_conflict use_column
DECLARE e record; v_wrong integer; v_correct boolean; v_after integer;
BEGIN
  PERFORM pg_advisory_xact_lock(hashtextextended(p_assignment::text || ':' || p_user::text, 2108));
  SELECT a.outcome, a.submitted, a.detail, a.wrong_after INTO e FROM practice_attempt a
   WHERE a.org_id = p_org AND a.assignment_id = p_assignment AND a.user_id = p_user AND a.request_key = p_key;
  IF e.outcome IS NOT NULL THEN
    RETURN QUERY SELECT 'DUPLICATE'::text, e.outcome, e.submitted, e.detail, e.wrong_after::integer; RETURN;
  END IF;
  SELECT count(*) FILTER (WHERE a.outcome = 'wrong'), COALESCE(bool_or(a.outcome = 'correct'), false)
    INTO v_wrong, v_correct FROM practice_attempt a
   WHERE a.org_id = p_org AND a.assignment_id = p_assignment AND a.user_id = p_user;
  IF v_correct THEN RETURN QUERY SELECT 'CONFLICT_CORRECT'::text, NULL::text, NULL::text, NULL::jsonb, v_wrong; RETURN; END IF;
  IF v_wrong >= p_max AND p_source = 'student' THEN
    RETURN QUERY SELECT 'CONFLICT_EXHAUSTED'::text, NULL::text, NULL::text, NULL::jsonb, v_wrong; RETURN;
  END IF;
  v_after := v_wrong + CASE WHEN p_outcome = 'wrong' THEN 1 ELSE 0 END;
  INSERT INTO practice_attempt (id, org_id, assignment_id, user_id, request_key, submitted, outcome, detail, wrong_after, source)
  VALUES (uuid_v7(), p_org, p_assignment, p_user, p_key, left(p_submitted, 200), p_outcome, COALESCE(p_detail, '{}'::jsonb),
          v_after, p_source);
  RETURN QUERY SELECT 'OK'::text, p_outcome, left(p_submitted, 200), COALESCE(p_detail, '{}'::jsonb), v_after;
END $$;
COMMENT ON FUNCTION fn_record_practice_attempt IS 'Đường DUY NHẤT ghi lượt thử bài luyện: khóa theo (câu, học sinh), chống lặp theo request_key, chặn sau khi Đúng hoặc hết lượt.';

-- ---------------------------------------------------------------- quyền (mẫu V10)
REVOKE ALL ON FUNCTION fn_practice_reference(uuid, uuid, uuid[]),
                       fn_assign_practice_set(uuid, uuid, uuid, uuid, boolean, uuid),
                       fn_record_practice_attempt(uuid, uuid, uuid, text, text, text, jsonb, integer, text) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION fn_practice_reference(uuid, uuid, uuid[]),
                          fn_assign_practice_set(uuid, uuid, uuid, uuid, boolean, uuid),
                          fn_record_practice_attempt(uuid, uuid, uuid, text, text, text, jsonb, integer, text) TO geo_app;
GRANT SELECT, INSERT, DELETE ON assignment_set, assignment_item TO geo_app;
GRANT UPDATE (title, due_at, release_at, status, closed_at, archived_at, updated_at) ON assignment_set TO geo_app;
GRANT UPDATE (accepted_forms) ON assignment_item TO geo_app;
GRANT SELECT ON practice_attempt TO geo_app;
GRANT SELECT, INSERT ON practice_report TO geo_app;
GRANT UPDATE (status, decided_at, decided_by) ON practice_report TO geo_app;
