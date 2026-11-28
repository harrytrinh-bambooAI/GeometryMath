-- T-261128-58 (S5-E3): hỏi đáp của học sinh theo kho (US-S5E3-03), mức trợ giúp theo bài + lịch công bố (US-S5E3-04),
-- kiểm soát đáp án đầy đủ KS-1…KS-5 và nhật ký quyết định (US-S5E3-05).
--   assignment_help_level  mức trợ giúp của AI theo bài (bài luyện S3 `assignment` hoặc bài có chấm S5-E4
--                          `graded_assignment`), ghi chú riêng của giáo viên (không bao giờ gửi AI / học sinh, BR-S5E3-17),
--                          mã kiến thức của đề (đề chưa có cột mã kiến thức: giáo viên/seed khai, dùng cho KbRetriever).
--   ai_interaction         một lượt hỏi đáp (câu hỏi, câu đã hiện, nguồn); giữ tới hết năm học (QS5-14).
--   ai_policy_decision     một quyết định mỗi lượt: allowed | blocked (KS-n hoặc bước kiểm trước) | error_closed (BR-S5E3-18).
--   ai_policy_counter      bộ đếm theo luật, KHÔNG gắn người (giữ khi xoá tài khoản học sinh).
--   kb_privacy_ack         cờ đã đọc thông báo riêng tư MSG-S5-26 theo phiên bản văn bản (BR-S5E3-22).
--   kb_answer_warning      mục tài liệu có đáp số của bài đang khoá (lọc khỏi AI, báo giáo viên; BR-S5E3-16, QS5-13).
-- Xoá tài khoản học sinh (deleted_at): xoá ngay ai_interaction, ai_policy_decision, kb_privacy_ack của em (trong 24 giờ,
-- BR-S5E3-18); dọn vật lý (fn_account_purge) còn xoá theo khoá ngoại.

CREATE TABLE assignment_help_level (
  org_id          uuid NOT NULL,
  target_id       uuid NOT NULL,                     -- assignment.id (bài luyện) hoặc graded_assignment.id (bài có chấm)
  target_kind     text NOT NULL CHECK (target_kind IN ('practice', 'graded')),
  class_id        uuid NOT NULL,
  help_level      text NOT NULL CHECK (help_level IN ('off', 'hints', 'hints_concepts')),
  private_note    text CHECK (private_note IS NULL OR length(private_note) <= 2000),
  knowledge_codes text[] NOT NULL DEFAULT '{}',
  updated_by      uuid NOT NULL,
  updated_at      timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (org_id, target_id),
  FOREIGN KEY (org_id, class_id) REFERENCES class (org_id, id) ON DELETE CASCADE
);
COMMENT ON TABLE assignment_help_level IS 'S5-E3 BR-S5E3-12: mức trợ giúp AI theo bài; không có hàng = mặc định (bài có chấm Tắt, bài luyện Gợi ý và khái niệm, QS5-12).';
COMMENT ON COLUMN assignment_help_level.private_note IS 'Ghi chú riêng của giáo viên (ghi_chu_rieng): KHÔNG gửi AI, KHÔNG trả học sinh (BR-S5E3-17).';

CREATE TABLE ai_interaction (
  id          uuid PRIMARY KEY,
  org_id      uuid NOT NULL,
  student_id  uuid NOT NULL,
  class_id    uuid NOT NULL,
  target_id   uuid NOT NULL,
  target_kind text NOT NULL CHECK (target_kind IN ('practice', 'graded')),
  question    text NOT NULL CHECK (length(question) BETWEEN 1 AND 2000),
  reply_kind  text NOT NULL CHECK (reply_kind IN ('concept', 'hint', 'explain_step', 'refused_answer', 'no_source',
                                                 'off', 'error_closed')),
  reply       text NOT NULL,
  source      jsonb,                                  -- {type, docId, docCode, docTitle, version, sectionId, sectionTitle} | {type: lecture, …}
  created_at  timestamptz NOT NULL DEFAULT now(),
  UNIQUE (org_id, id),
  FOREIGN KEY (org_id, student_id) REFERENCES app_user (org_id, id) ON DELETE CASCADE
);
CREATE INDEX ai_interaction_target_idx ON ai_interaction (org_id, target_id, created_at);
CREATE INDEX ai_interaction_student_idx ON ai_interaction (org_id, student_id);
COMMENT ON TABLE ai_interaction IS 'S5-E3 AIInteraction: câu hỏi, câu trả lời ĐÃ HIỆN (sau bộ kiểm), nguồn; giữ tới hết năm học (QS5-14).';

CREATE TABLE ai_policy_decision (
  id               uuid PRIMARY KEY,
  org_id           uuid NOT NULL,
  interaction_id   uuid,
  student_id       uuid NOT NULL,
  target_id        uuid NOT NULL,
  decision         text NOT NULL CHECK (decision IN ('allowed', 'blocked', 'error_closed')),
  rule_code        text CHECK (rule_code IS NULL OR rule_code ~ '^[A-Za-z0-9_-]{1,40}$'),
  check_ms         int CHECK (check_ms IS NULL OR check_ms >= 0),
  firewall_version text NOT NULL,
  created_at       timestamptz NOT NULL DEFAULT now(),
  FOREIGN KEY (org_id, interaction_id) REFERENCES ai_interaction (org_id, id) ON DELETE CASCADE,
  FOREIGN KEY (org_id, student_id) REFERENCES app_user (org_id, id) ON DELETE CASCADE
);
CREATE INDEX ai_policy_decision_target_idx ON ai_policy_decision (org_id, target_id, created_at);
CREATE INDEX ai_policy_decision_student_idx ON ai_policy_decision (org_id, student_id);
COMMENT ON TABLE ai_policy_decision IS 'S5-E3 AIPolicyDecision (BR-S5E3-13, 18): allowed | blocked (KS-1…KS-4, private_content, class_switch, membership, help_level, no_source, invalid_source, kind_not_allowed) | error_closed (KS-5 ERR_*).';

CREATE TABLE ai_policy_counter (
  org_id    uuid NOT NULL,
  day       date NOT NULL,
  decision  text NOT NULL,
  rule_code text NOT NULL DEFAULT '',
  n         int  NOT NULL DEFAULT 0 CHECK (n >= 0),
  PRIMARY KEY (org_id, day, decision, rule_code)
);
COMMENT ON TABLE ai_policy_counter IS 'S5-E3 BR-S5E3-18: bộ đếm theo luật không gắn người (giữ khi xoá tài khoản học sinh).';

CREATE TABLE kb_privacy_ack (
  org_id   uuid NOT NULL,
  user_id  uuid NOT NULL,
  version  text NOT NULL CHECK (version ~ '^[a-z0-9.-]{1,40}$'),
  acked_at timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (org_id, user_id, version),
  FOREIGN KEY (org_id, user_id) REFERENCES app_user (org_id, id) ON DELETE CASCADE
);
COMMENT ON TABLE kb_privacy_ack IS 'S5-E3 BR-S5E3-22: học sinh đã bấm "Em đã hiểu" ở MSG-S5-26 (phiên bản văn bản, ví dụ privacy-qa-s5-1.0).';

CREATE TABLE kb_answer_warning (
  id            uuid PRIMARY KEY,
  org_id        uuid NOT NULL,
  target_id     uuid NOT NULL,
  doc_id        uuid NOT NULL,
  version       int  NOT NULL,
  section_id    uuid NOT NULL,
  first_seen_at timestamptz NOT NULL DEFAULT now(),
  last_seen_at  timestamptz NOT NULL DEFAULT now(),
  hits          int  NOT NULL DEFAULT 1 CHECK (hits >= 1),
  UNIQUE (org_id, target_id, section_id),
  FOREIGN KEY (org_id, doc_id) REFERENCES kb_document (org_id, id) ON DELETE CASCADE
);
COMMENT ON TABLE kb_answer_warning IS 'S5-E3 BR-S5E3-16: mục tài liệu chứa đáp số của bài đang khoá đã bị lọc khỏi AI; giáo viên thấy cảnh báo.';

-- ---------------------------------------------------------------- ngữ cảnh một lượt hỏi (chỉ phía máy chủ)
-- Bài luyện (assignment kind 'class') hoặc bài có chấm: lớp, thành viên Đang học, công tắc AI của lớp, trạng thái khoá và
-- đáp số/bước/dạng tương đương của lời giải đang khoá (KHÔNG BAO GIỜ gửi xuống thiết bị), nội dung rubric (để chặn lộ).
-- Không có hàng = bài không tồn tại. member = false: không trả gì của lời giải.
CREATE FUNCTION fn_kb_qa_context(p_org uuid, p_user uuid, p_target uuid)
RETURNS TABLE (target_kind text, class_id uuid, member boolean, ai_enabled boolean, problem_version_id uuid,
               locked boolean, final_answer text, steps jsonb, answer_forms jsonb, rubric_text text)
LANGUAGE plpgsql STABLE SECURITY DEFINER SET search_path = public, pg_temp
AS $$
#variable_conflict use_column
DECLARE a record; g record; c record; s record; m boolean; en boolean; rt text;
BEGIN
  SELECT x.kind, x.class_id AS cid, x.problem_version_id AS pv INTO a
    FROM assignment x WHERE x.org_id = p_org AND x.id = p_target;
  IF FOUND THEN
    IF a.kind <> 'class' THEN RETURN; END IF;
    m := EXISTS (SELECT 1 FROM class_member cm WHERE cm.org_id = p_org AND cm.class_id = a.cid
                   AND cm.user_id = p_user AND cm.left_at IS NULL AND cm.status = 'active');
    SELECT k.ai_enabled INTO en FROM class k WHERE k.org_id = p_org AND k.id = a.cid;
    IF NOT m THEN
      RETURN QUERY SELECT 'practice'::text, a.cid, false, en, a.pv, NULL::boolean, NULL::text, NULL::jsonb, NULL::jsonb, NULL::text;
      RETURN;
    END IF;
    SELECT * INTO c FROM fn_guard_context(p_org, p_user, p_target);
    RETURN QUERY SELECT 'practice'::text, a.cid, true, en, a.pv, COALESCE(c.locked, false),
                        c.final_answer, c.steps, c.answer_forms, NULL::text;
    RETURN;
  END IF;
  SELECT x.class_id AS cid, x.problem_version_id AS pv, x.status AS st, x.release_mode AS rm, x.release_at AS ra,
         x.rubric_version_id AS rv INTO g
    FROM graded_assignment x WHERE x.org_id = p_org AND x.id = p_target;
  IF NOT FOUND THEN RETURN; END IF;
  m := EXISTS (SELECT 1 FROM class_member cm WHERE cm.org_id = p_org AND cm.class_id = g.cid
                 AND cm.user_id = p_user AND cm.left_at IS NULL AND cm.status = 'active');
  SELECT k.ai_enabled INTO en FROM class k WHERE k.org_id = p_org AND k.id = g.cid;
  IF NOT m THEN
    RETURN QUERY SELECT 'graded'::text, g.cid, false, en, g.pv, NULL::boolean, NULL::text, NULL::jsonb, NULL::jsonb, NULL::text;
    RETURN;
  END IF;
  IF g.st = 'returned' OR (g.rm = 'at_time' AND g.ra <= now()) THEN
    RETURN QUERY SELECT 'graded'::text, g.cid, true, en, g.pv, false, NULL::text, NULL::jsonb, NULL::jsonb, NULL::text;
    RETURN;
  END IF;
  SELECT so.final_answer AS fa, so.steps AS st, so.answer_forms AS af INTO s
    FROM problem_version pv JOIN solution so ON so.org_id = pv.org_id AND so.problem_version_id = pv.id
   WHERE pv.id = g.pv LIMIT 1;
  SELECT concat_ws(E'\n',
           (SELECT string_agg(rc.name || ': ' || rc.description, E'\n' ORDER BY rc.ord) FROM rubric_criterion rc
             WHERE rc.org_id = p_org AND rc.rubric_version_id = g.rv),
           (SELECT string_agg(e.v, E'\n') FROM rubric_version r, jsonb_array_elements_text(r.guide) AS e(v)
             WHERE r.org_id = p_org AND r.id = g.rv))
    INTO rt;
  RETURN QUERY SELECT 'graded'::text, g.cid, true, en, g.pv, true, s.fa, s.st, s.af, rt;
END $$;
COMMENT ON FUNCTION fn_kb_qa_context IS 'S5-E3 (T-261128-58): ngữ cảnh hỏi đáp theo kho + Kiểm soát đáp án của bài luyện / bài có chấm. Chỉ phía máy chủ.';

-- Giáo viên đặt mức trợ giúp: bài thuộc lớp mình (bài luyện) hoặc bài có chấm của mình.
CREATE FUNCTION fn_help_level_target(p_org uuid, p_teacher uuid, p_target uuid)
RETURNS TABLE (target_kind text, class_id uuid, title text)
LANGUAGE plpgsql STABLE SECURITY DEFINER SET search_path = public, pg_temp
AS $$
#variable_conflict use_column
BEGIN
  RETURN QUERY
  SELECT 'practice'::text, x.class_id, COALESCE(p.title, '')::text
    FROM assignment x
    JOIN class k ON k.org_id = p_org AND k.id = x.class_id AND k.teacher_id = p_teacher
    LEFT JOIN problem_version pv ON pv.id = x.problem_version_id
    LEFT JOIN problem p ON p.org_id = pv.org_id AND p.id = pv.problem_id
   WHERE x.org_id = p_org AND x.id = p_target AND x.kind = 'class'
  UNION ALL
  SELECT 'graded'::text, g.class_id, g.title
    FROM graded_assignment g
   WHERE g.org_id = p_org AND g.id = p_target AND g.teacher_id = p_teacher
  LIMIT 1;
END $$;
COMMENT ON FUNCTION fn_help_level_target IS 'S5-E3 (T-261128-58): bài mà giáo viên được đặt mức trợ giúp AI (bài luyện của lớp mình, bài có chấm của mình).';

-- Xoá tài khoản học sinh: xoá ngay lịch sử hỏi đáp của em (BR-S5E3-18); bộ đếm theo luật không đổi.
CREATE FUNCTION tg_kb_qa_erase_student() RETURNS trigger
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
BEGIN
  IF NEW.deleted_at IS NOT NULL AND OLD.deleted_at IS NULL THEN
    DELETE FROM ai_policy_decision WHERE org_id = NEW.org_id AND student_id = NEW.id;
    DELETE FROM ai_interaction     WHERE org_id = NEW.org_id AND student_id = NEW.id;
    DELETE FROM kb_privacy_ack     WHERE org_id = NEW.org_id AND user_id = NEW.id;
  END IF;
  RETURN NEW;
END $$;
CREATE TRIGGER app_user_kb_qa_erase AFTER UPDATE OF deleted_at ON app_user
  FOR EACH ROW EXECUTE FUNCTION tg_kb_qa_erase_student();

REVOKE ALL ON FUNCTION fn_kb_qa_context(uuid, uuid, uuid), fn_help_level_target(uuid, uuid, uuid),
                       tg_kb_qa_erase_student() FROM PUBLIC;
GRANT EXECUTE ON FUNCTION fn_kb_qa_context(uuid, uuid, uuid), fn_help_level_target(uuid, uuid, uuid) TO geo_app;

GRANT SELECT, INSERT, UPDATE ON assignment_help_level TO geo_app;
GRANT SELECT, INSERT, DELETE ON ai_interaction, ai_policy_decision TO geo_app;
GRANT SELECT, INSERT, UPDATE ON ai_policy_counter, kb_answer_warning TO geo_app;
GRANT SELECT, INSERT ON kb_privacy_ack TO geo_app;
