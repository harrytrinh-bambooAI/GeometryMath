-- T-261128-68 · S5-FIX W7 (Dev-S5E3-13): hỏi đáp theo kho nối "Trả bài" thật và bài nộp của bạn.
--   fn_kb_qa_context   bài có chấm mở khoá đáp số cho học sinh theo đúng luật lời giải (fn_graded_solution, BR-S5E4-20,
--                      23): bài của CHÍNH em Đã trả (lượt grade_return đang hiệu lực) hoặc giờ công bố cố định đã tới và
--                      bài của em Đã duyệt. Trước đây mở theo graded_assignment.status (móc e2e) cho cả lớp.
--   fn_kb_qa_peer_texts văn bản bản đọc chữ (bản mới nhất) của bài nộp của các bạn khác trong cùng bài có chấm: bộ kiểm
--                      KsS5Firewall chặn câu trả lời chứa nội dung bài của bạn (private_content, BR-S5E3-17).
--                      Chỉ phía máy chủ; không bao giờ đưa vào yêu cầu AI.

CREATE OR REPLACE FUNCTION fn_kb_qa_context(p_org uuid, p_user uuid, p_target uuid)
RETURNS TABLE (target_kind text, class_id uuid, member boolean, ai_enabled boolean, problem_version_id uuid,
               locked boolean, final_answer text, steps jsonb, answer_forms jsonb, rubric_text text)
LANGUAGE plpgsql STABLE SECURITY DEFINER SET search_path = public, pg_temp
AS $$
#variable_conflict use_column
DECLARE a record; g record; c record; s record; m boolean; en boolean; rt text; open boolean;
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
  SELECT x.class_id AS cid, x.problem_version_id AS pv, x.release_mode AS rm, x.release_at AS ra,
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
  -- cùng luật với fn_graded_solution (V705): chỉ bài của chính em
  SELECT EXISTS (
    SELECT 1 FROM submission sb
     WHERE sb.org_id = p_org AND sb.assignment_id = p_target AND sb.student_id = p_user
       AND (sb.grading_state = 'returned'
            OR (g.rm = 'at_time' AND g.ra <= now() AND sb.grading_state = 'approved'))) INTO open;
  IF open THEN
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
COMMENT ON FUNCTION fn_kb_qa_context IS 'S5-E3 (T-261128-58, sửa T-261128-68): ngữ cảnh hỏi đáp theo kho + Kiểm soát đáp án; bài có chấm mở khoá theo bài của chính em (như fn_graded_solution). Chỉ phía máy chủ.';

-- Văn bản bài nộp của các bạn (bản đọc chữ mới nhất của phiên bản chấm), tối đa 60 bài; người hỏi phải là thành viên lớp.
CREATE FUNCTION fn_kb_qa_peer_texts(p_org uuid, p_user uuid, p_target uuid)
RETURNS TABLE (body text)
LANGUAGE sql STABLE SECURITY DEFINER SET search_path = public, pg_temp
AS $$
  WITH g AS (
    SELECT x.id, x.class_id FROM graded_assignment x
     WHERE x.org_id = p_org AND x.id = p_target
       AND EXISTS (SELECT 1 FROM class_member cm WHERE cm.org_id = p_org AND cm.class_id = x.class_id
                     AND cm.user_id = p_user AND cm.left_at IS NULL AND cm.status = 'active')
  ), latest AS (
    SELECT DISTINCT ON (o.submission_id) o.pages
      FROM g
      JOIN submission sb ON sb.org_id = p_org AND sb.assignment_id = g.id AND sb.student_id <> p_user
      JOIN submission_version sv ON sv.org_id = p_org AND sv.submission_id = sb.id AND sv.version = sb.current_version
      JOIN ocr_artifact o ON o.org_id = p_org AND o.version_id = sv.id AND o.status = 'done'
     ORDER BY o.submission_id, o.version DESC
     LIMIT 60
  )
  SELECT string_agg(t.v #>> '{}', E'\n') FROM latest l, jsonb_path_query(l.pages, 'lax $.**.text') AS t(v)
  GROUP BY l.pages;
$$;
COMMENT ON FUNCTION fn_kb_qa_peer_texts IS 'S5-FIX (T-261128-68, Dev-S5E3-13): văn bản bài nộp của các bạn cùng bài có chấm cho bộ kiểm private_content. Chỉ phía máy chủ, không gửi AI.';
REVOKE ALL ON FUNCTION fn_kb_qa_peer_texts(uuid, uuid, uuid) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION fn_kb_qa_peer_texts(uuid, uuid, uuid) TO geo_app;
