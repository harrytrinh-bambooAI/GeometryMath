-- V40 (E4): hàm cổng cho Gợi ý, Lời giải đồng bộ hình, Hỏi đáp, Tự học (US-GEO-401…412).
-- Mọi hàm SECURITY DEFINER tự lọc org_id và tư cách (thành viên lớp | chủ bài tự học). Lời giải chỉ ra khỏi CSDL
-- qua fn_open_solution (cổng chung fn_solution_gate của V10) và qua fn_answer_guard_context (chỉ dùng phía máy chủ
-- cho Kiểm soát đáp án tối thiểu của E4; không bao giờ gửi xuống thiết bị).

-- ---------------------------------------------------------------- trạng thái bảng trợ giúp của một bài
-- solution_state: open (xem được) · release (bài lớp chưa tới giờ mở) · hints (tự học, chưa đủ Gợi ý 1 và 2, MSG-GEO-30)
--                 · no_signature (tự học, đề không dựng được bằng hình mẫu, MSG-GEO-58) · inherited (kế thừa khóa, MSG-GEO-31)
CREATE FUNCTION fn_help_state(p_org uuid, p_user uuid, p_assignment uuid)
RETURNS TABLE (status text, kind text, allow_hint boolean, allow_qa boolean, hint_level_max smallint, solution_viewed boolean,
               solution_state text, release_at timestamptz, statement text, problem_title text, verify_status text)
LANGUAGE plpgsql STABLE SECURITY DEFINER SET search_path = public, pg_temp
AS $$
#variable_conflict use_column
DECLARE a record; v_max smallint; v_seen timestamptz; g record; v_state text; v_rel timestamptz; v_lock uuid; pv record;
BEGIN
  SELECT x.kind, x.class_id, x.owner_user_id, x.content_org_id AS corg, x.problem_version_id AS pv, x.release_at AS rel,
         x.allow_hint AS ah, x.allow_qa AS aq
    INTO a FROM assignment x WHERE x.org_id = p_org AND x.id = p_assignment;
  IF a.kind IS NULL
     OR (a.kind = 'class' AND NOT EXISTS (SELECT 1 FROM class_member cm WHERE cm.org_id = p_org AND cm.class_id = a.class_id
                                           AND cm.user_id = p_user AND cm.left_at IS NULL))
     OR (a.kind = 'self' AND a.owner_user_id <> p_user) THEN
    RETURN QUERY SELECT 'NOT_FOUND'::text, NULL::text, NULL::boolean, NULL::boolean, NULL::smallint, NULL::boolean,
                        NULL::text, NULL::timestamptz, NULL::text, NULL::text, NULL::text;
    RETURN;
  END IF;
  SELECT ap.hint_level_max, ap.solution_viewed_at INTO v_max, v_seen FROM assignment_progress ap
   WHERE ap.org_id = p_org AND ap.assignment_id = p_assignment AND ap.user_id = p_user;
  SELECT * INTO g FROM fn_solution_gate(p_org, p_user, p_assignment);
  v_rel := a.rel;
  IF g.status = 'OK' THEN
    v_state := 'open';
  ELSIF a.kind = 'class' THEN
    v_state := 'release';
  ELSIF COALESCE(v_max, 0) < 2 THEN
    v_state := 'hints';
  ELSIF NOT EXISTS (SELECT 1 FROM problem_signature s WHERE s.org_id = p_org AND s.problem_version_id = a.pv) THEN
    v_state := 'no_signature';
  ELSE
    v_state := 'inherited';
    v_lock := COALESCE(g.lock_source, fn_self_lock_source(p_org, p_user, a.pv));
    SELECT l.release_at INTO v_rel FROM assignment l WHERE l.org_id = p_org AND l.id = v_lock;
  END IF;
  SELECT v.statement AS st, p.title AS ti, v.verify_status::text AS vs INTO pv
    FROM problem_version v JOIN problem p ON p.org_id = v.org_id AND p.id = v.problem_id
   WHERE v.org_id = a.corg AND v.id = a.pv;
  RETURN QUERY SELECT 'OK'::text, a.kind::text, a.ah, a.aq, COALESCE(v_max, 0::smallint), v_seen IS NOT NULL,
                      v_state, v_rel, pv.st, pv.ti, pv.vs;
END $$;
COMMENT ON FUNCTION fn_help_state IS 'E4: trạng thái bảng Gợi ý/Lời giải/Hỏi đáp của một bài cho học sinh (không chứa nội dung gợi ý chưa xin, lời giải hay đáp số; BR-GEO-177, ADR-GEO-09).';

-- ---------------------------------------------------------------- nguồn gợi ý (dùng chung)
-- Bài lớp: gợi ý của đề kho được giao. Bài tự học: kế thừa khóa thì gợi ý của đề kho đó (D-GEO-155), ngược lại của đề tự nhập.
CREATE FUNCTION _hint_source(p_org uuid, p_user uuid, p_assignment uuid)
RETURNS TABLE (src_org uuid, src_pv uuid, hint_source text, allow_hint boolean)
LANGUAGE plpgsql STABLE SECURITY DEFINER SET search_path = public, pg_temp
AS $$
#variable_conflict use_column
DECLARE a record; v_lock uuid; l record;
BEGIN
  SELECT x.kind, x.class_id, x.owner_user_id, x.content_org_id AS corg, x.problem_version_id AS pv, x.allow_hint AS ah
    INTO a FROM assignment x WHERE x.org_id = p_org AND x.id = p_assignment;
  IF a.kind IS NULL
     OR (a.kind = 'class' AND NOT EXISTS (SELECT 1 FROM class_member cm WHERE cm.org_id = p_org AND cm.class_id = a.class_id
                                           AND cm.user_id = p_user AND cm.left_at IS NULL))
     OR (a.kind = 'self' AND a.owner_user_id <> p_user) THEN
    RETURN;
  END IF;
  IF a.kind = 'class' THEN
    RETURN QUERY SELECT a.corg, a.pv, 'assignment'::text, a.ah; RETURN;
  END IF;
  v_lock := fn_self_lock_source(p_org, p_user, a.pv);
  IF v_lock IS NOT NULL THEN
    SELECT x.content_org_id AS corg, x.problem_version_id AS pv INTO l FROM assignment x WHERE x.org_id = p_org AND x.id = v_lock;
    RETURN QUERY SELECT l.corg, l.pv, 'catalog_inherited'::text, true; RETURN;
  END IF;
  RETURN QUERY SELECT a.corg, a.pv, 'self'::text, true;
END $$;

-- Gợi ý học sinh ĐÃ xem (mức <= hint_level_max): đọc lại khi mở lại bài, KHÔNG ghi sự kiện, KHÔNG tăng lượt xem
-- (US-GEO-404-SC4, M-GEO-16). Gợi ý chưa xin không bao giờ được trả (BR-GEO-177).
CREATE FUNCTION fn_seen_hints(p_org uuid, p_user uuid, p_assignment uuid)
RETURNS TABLE (level smallint, body text, hint_source text)
LANGUAGE plpgsql STABLE SECURITY DEFINER SET search_path = public, pg_temp
AS $$
#variable_conflict use_column
DECLARE s record; v_max smallint;
BEGIN
  SELECT * INTO s FROM _hint_source(p_org, p_user, p_assignment);
  IF s.src_pv IS NULL OR NOT s.allow_hint THEN RETURN; END IF;
  SELECT ap.hint_level_max INTO v_max FROM assignment_progress ap
   WHERE ap.org_id = p_org AND ap.assignment_id = p_assignment AND ap.user_id = p_user;
  RETURN QUERY SELECT h.level, h.body, s.hint_source FROM hint h
   WHERE h.org_id = s.src_org AND h.problem_version_id = s.src_pv AND h.level <= COALESCE(v_max, 0)
   ORDER BY h.level;
END $$;

-- Gợi ý của đề tự nhập do AI sinh (D-GEO-155): ghi khi học sinh xin lần đầu và bài KHÔNG kế thừa khóa.
CREATE FUNCTION fn_put_self_hints(p_org uuid, p_user uuid, p_assignment uuid, p_h1 text, p_h2 text)
RETURNS text LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
DECLARE v_pv uuid;
BEGIN
  SELECT x.problem_version_id INTO v_pv FROM assignment x
   WHERE x.org_id = p_org AND x.id = p_assignment AND x.kind = 'self' AND x.owner_user_id = p_user;
  IF v_pv IS NULL THEN RETURN 'NOT_FOUND'; END IF;
  INSERT INTO hint (org_id, problem_version_id, level, body) VALUES (p_org, v_pv, 1, p_h1), (p_org, v_pv, 2, p_h2)
  ON CONFLICT DO NOTHING;
  RETURN 'OK';
END $$;

-- Lời giải của đề tự nhập (đề dựng được bằng hình mẫu, có chữ ký chuẩn; D-GEO-150, 177). Ghi lúc tạo bài tự học.
CREATE FUNCTION fn_put_self_solution(p_org uuid, p_user uuid, p_pv uuid, p_final text, p_steps jsonb, p_model text, p_prompt text)
RETURNS text LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM problem_version pv JOIN problem p ON p.org_id = pv.org_id AND p.id = pv.problem_id
                 WHERE pv.org_id = p_org AND pv.id = p_pv AND p.source = 'student_input' AND p.owner_user_id = p_user)
     OR NOT EXISTS (SELECT 1 FROM problem_signature s WHERE s.org_id = p_org AND s.problem_version_id = p_pv) THEN
    RETURN 'NOT_FOUND';
  END IF;
  INSERT INTO solution (org_id, problem_version_id, final_answer, steps, model, prompt_version, ai_authored)
  VALUES (p_org, p_pv, p_final, p_steps, p_model, p_prompt, true)
  ON CONFLICT DO NOTHING;
  RETURN 'OK';
END $$;

-- ---------------------------------------------------------------- mở lời giải (US-GEO-405, 407)
-- Như fn_get_solution, thêm: lần ĐẦU học sinh xem lời giải của bài ghi đúng MỘT sự kiện 'unlock' và tiến độ 3/3
-- (BR-GEO-186; US-GEO-407-SC5: mở tab nhiều lần vẫn một sự kiện). Khóa tư vấn theo (bài, học sinh) chống chạy đua.
CREATE FUNCTION fn_open_solution(p_org uuid, p_user uuid, p_assignment uuid)
RETURNS TABLE (status text, final_answer text, steps jsonb, ai_authored boolean, verify_status text, first_view boolean)
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
#variable_conflict use_column
DECLARE g record; s record; v_vs text; v_first boolean := false;
BEGIN
  SELECT * INTO g FROM fn_solution_gate(p_org, p_user, p_assignment);
  IF g.status <> 'OK' THEN
    RETURN QUERY SELECT g.status, NULL::text, NULL::jsonb, NULL::boolean, NULL::text, false; RETURN;
  END IF;
  SELECT x.final_answer AS fa, x.steps AS st, x.ai_authored AS ai INTO s FROM solution x
   WHERE x.org_id = g.content_org_id AND x.problem_version_id = g.problem_version_id;
  IF s.fa IS NULL THEN
    RETURN QUERY SELECT 'NO_SOLUTION'::text, NULL::text, NULL::jsonb, NULL::boolean, NULL::text, false; RETURN;
  END IF;
  SELECT pv.verify_status::text INTO v_vs FROM problem_version pv WHERE pv.org_id = g.content_org_id AND pv.id = g.problem_version_id;
  PERFORM pg_advisory_xact_lock(hashtextextended(p_assignment::text || '|' || p_user::text || '|unlock', 0));
  IF NOT EXISTS (SELECT 1 FROM assignment_progress ap WHERE ap.org_id = p_org AND ap.assignment_id = p_assignment
                   AND ap.user_id = p_user AND ap.solution_viewed_at IS NOT NULL) THEN
    INSERT INTO event_log (occurred_at, org_id, assignment_id, user_id, kind) VALUES (now(), p_org, p_assignment, p_user, 'unlock');
    PERFORM _progress_apply(p_org, p_assignment, p_user, now(), now(), 0, 0::smallint, 0, 0, false, true);
    v_first := true;
  END IF;
  RETURN QUERY SELECT 'OK'::text, s.fa, s.st, s.ai, v_vs, v_first;
END $$;
COMMENT ON FUNCTION fn_open_solution IS 'E4: đường học sinh xem lời giải từng bước; cổng fn_solution_gate; ghi sự kiện mở lời giải đúng một lần (BR-GEO-186).';

-- ---------------------------------------------------------------- Kiểm soát đáp án tối thiểu của E4 (phía máy chủ)
-- Khi lời giải còn khóa với học sinh này: trả đáp số + bước của đề NGUỒN (bài lớp: đề kho được giao; tự học kế thừa khóa:
-- đề kho của bài đang khóa; tự học: đề tự nhập) để lọc câu trả lời hỏi đáp (BR-GEO-02, D-GEO-159). Không bao giờ trả về thiết bị.
CREATE FUNCTION fn_answer_guard_context(p_org uuid, p_user uuid, p_assignment uuid)
RETURNS TABLE (status text, locked boolean, final_answer text, steps jsonb, own_final_answer text, own_steps jsonb)
LANGUAGE plpgsql STABLE SECURITY DEFINER SET search_path = public, pg_temp
AS $$
#variable_conflict use_column
DECLARE a record; g record; s record; o record;
BEGIN
  SELECT x.kind, x.class_id, x.owner_user_id, x.content_org_id AS corg, x.problem_version_id AS pv
    INTO a FROM assignment x WHERE x.org_id = p_org AND x.id = p_assignment;
  IF a.kind IS NULL
     OR (a.kind = 'class' AND NOT EXISTS (SELECT 1 FROM class_member cm WHERE cm.org_id = p_org AND cm.class_id = a.class_id
                                           AND cm.user_id = p_user AND cm.left_at IS NULL))
     OR (a.kind = 'self' AND a.owner_user_id <> p_user) THEN
    RETURN QUERY SELECT 'NOT_FOUND'::text, NULL::boolean, NULL::text, NULL::jsonb, NULL::text, NULL::jsonb; RETURN;
  END IF;
  SELECT * INTO g FROM fn_solution_gate(p_org, p_user, p_assignment);
  IF g.status = 'OK' THEN
    RETURN QUERY SELECT 'OK'::text, false, NULL::text, NULL::jsonb, NULL::text, NULL::jsonb; RETURN;
  END IF;
  SELECT x.final_answer AS fa, x.steps AS st INTO o FROM solution x WHERE x.org_id = a.corg AND x.problem_version_id = a.pv;
  SELECT x.final_answer AS fa, x.steps AS st INTO s FROM solution x
    JOIN assignment l ON l.org_id = p_org AND l.id = COALESCE(g.lock_source, CASE WHEN a.kind = 'self'
                                                                       THEN fn_self_lock_source(p_org, p_user, a.pv) END)
   WHERE x.org_id = l.content_org_id AND x.problem_version_id = l.problem_version_id;
  RETURN QUERY SELECT 'OK'::text, true, s.fa, s.st, o.fa, o.st;
END $$;

-- ---------------------------------------------------------------- nhật ký hỏi đáp (US-GEO-410)
-- Câu hỏi của CHÍNH học sinh, mới nhất trước, lọc theo bài (tùy chọn), keyset theo id; kèm tên bài. Không kèm câu trả lời.
CREATE FUNCTION fn_qa_log(p_org uuid, p_user uuid, p_assignment uuid, p_before bigint, p_limit integer)
RETURNS TABLE (id bigint, assignment_id uuid, asked_at timestamptz, question text, problem_title text)
LANGUAGE plpgsql STABLE SECURITY DEFINER SET search_path = public, pg_temp
AS $$
#variable_conflict use_column
BEGIN
  RETURN QUERY
  SELECT q.id, q.assignment_id, q.asked_at, q.question, p.title
  FROM qa_turn q
  JOIN assignment a ON a.org_id = q.org_id AND a.id = q.assignment_id
  JOIN problem p    ON p.org_id = a.content_org_id AND p.id = a.problem_id
  WHERE q.org_id = p_org AND q.user_id = p_user
    AND (p_assignment IS NULL OR q.assignment_id = p_assignment)
    AND q.id < COALESCE(p_before, 9223372036854775807)
  ORDER BY q.id DESC
  LIMIT LEAST(GREATEST(p_limit, 1), 101);    -- trần trang 100 + 1 (D-GEO-168)
END $$;

-- Một lượt hỏi đáp đã lưu (câu trả lời đã qua Kiểm soát đáp án) kèm nguồn, chỉ cho chính người hỏi (D-GEO-159).
CREATE FUNCTION fn_qa_turn(p_org uuid, p_user uuid, p_id bigint)
RETURNS TABLE (id bigint, assignment_id uuid, asked_at timestamptz, question text, reply_kind text, reply_text text,
               source_lecture_ids uuid[])
LANGUAGE plpgsql STABLE SECURITY DEFINER SET search_path = public, pg_temp
AS $$
#variable_conflict use_column
BEGIN
  RETURN QUERY
  SELECT q.id, q.assignment_id, q.asked_at, q.question, q.reply_kind::text, q.reply_text, q.source_lecture_ids
  FROM qa_turn q WHERE q.org_id = p_org AND q.user_id = p_user AND q.id = p_id;
END $$;

REVOKE ALL ON FUNCTION fn_help_state(uuid, uuid, uuid), _hint_source(uuid, uuid, uuid), fn_seen_hints(uuid, uuid, uuid),
  fn_put_self_hints(uuid, uuid, uuid, text, text), fn_put_self_solution(uuid, uuid, uuid, text, jsonb, text, text),
  fn_open_solution(uuid, uuid, uuid), fn_answer_guard_context(uuid, uuid, uuid),
  fn_qa_log(uuid, uuid, uuid, bigint, integer), fn_qa_turn(uuid, uuid, bigint) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION fn_help_state(uuid, uuid, uuid), fn_seen_hints(uuid, uuid, uuid), fn_open_solution(uuid, uuid, uuid),
  fn_qa_log(uuid, uuid, uuid, bigint, integer), fn_qa_turn(uuid, uuid, bigint) TO geo_app;
GRANT EXECUTE ON FUNCTION fn_put_self_hints(uuid, uuid, uuid, text, text), fn_put_self_solution(uuid, uuid, uuid, text, jsonb, text, text),
  fn_answer_guard_context(uuid, uuid, uuid) TO geo_ai;
