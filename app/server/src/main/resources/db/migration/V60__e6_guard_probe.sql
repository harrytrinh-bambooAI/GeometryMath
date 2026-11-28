-- E6 — Giao bài và khóa lời giải (US-GEO-601…612).
-- 1) Ngữ cảnh của Kiểm soát đáp án đầy đủ (US-GEO-609, ADR-GEO-14, D-GEO-156): thêm các dạng tương đương của đáp số
--    (solution.answer_forms) cho bài lớp/tự học và cho màn xem trước của giáo viên (đề kho, D-GEO-153: chỉ dùng phía máy chủ
--    để kiểm, KHÔNG BAO GIỜ gửi xuống thiết bị).
-- 2) Bộ câu thử moi đáp án có phiên bản, chống câu trùng, câu phải gắn bài (US-GEO-611) và báo cáo mỗi lượt chạy (US-GEO-610).

-- ---------------------------------------------------------------- 1. ngữ cảnh Kiểm soát đáp án
-- Như fn_answer_guard_context (V40) cộng answer_forms của lời giải đang khóa (bài lớp hoặc nguồn khóa của bài tự học kế thừa)
-- và của chính đề. locked = false khi người gọi xem được lời giải (khi đó không trả gì của lời giải).
CREATE FUNCTION fn_guard_context(p_org uuid, p_user uuid, p_assignment uuid)
RETURNS TABLE (status text, locked boolean, final_answer text, steps jsonb, answer_forms jsonb,
               own_final_answer text, own_steps jsonb, own_answer_forms jsonb)
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
    RETURN QUERY SELECT 'NOT_FOUND'::text, NULL::boolean, NULL::text, NULL::jsonb, NULL::jsonb, NULL::text, NULL::jsonb, NULL::jsonb;
    RETURN;
  END IF;
  SELECT * INTO g FROM fn_solution_gate(p_org, p_user, p_assignment);
  IF g.status = 'OK' THEN
    RETURN QUERY SELECT 'OK'::text, false, NULL::text, NULL::jsonb, NULL::jsonb, NULL::text, NULL::jsonb, NULL::jsonb; RETURN;
  END IF;
  SELECT x.final_answer AS fa, x.steps AS st, x.answer_forms AS af INTO o FROM solution x
   WHERE x.org_id = a.corg AND x.problem_version_id = a.pv;
  SELECT x.final_answer AS fa, x.steps AS st, x.answer_forms AS af INTO s FROM solution x
    JOIN assignment l ON l.org_id = p_org AND l.id = COALESCE(g.lock_source, CASE WHEN a.kind = 'self'
                                                                       THEN fn_self_lock_source(p_org, p_user, a.pv) END)
   WHERE x.org_id = l.content_org_id AND x.problem_version_id = l.problem_version_id;
  RETURN QUERY SELECT 'OK'::text, true, s.fa, s.st, s.af, o.fa, o.st, o.af;
END $$;
COMMENT ON FUNCTION fn_guard_context IS 'E6 Kiểm soát đáp án (ADR-GEO-14): đáp số, bước, dạng tương đương của lời giải đang khóa. Chỉ phía máy chủ.';

-- Màn xem trước của giáo viên (US-GEO-607-FC2, D-GEO-153): đề kho, giáo viên mẫu/đã xác minh; luôn coi như đang khóa.
CREATE FUNCTION fn_guard_context_catalog(p_user_org uuid, p_user uuid, p_problem_version uuid)
RETURNS TABLE (final_answer text, steps jsonb, answer_forms jsonb)
LANGUAGE plpgsql STABLE SECURITY DEFINER SET search_path = public, pg_temp
AS $$
#variable_conflict use_column
BEGIN
  RETURN QUERY
  SELECT s.final_answer, s.steps, s.answer_forms
  FROM app_user u
  JOIN problem_version pv ON pv.org_id = platform_org_id() AND pv.id = p_problem_version
  JOIN problem p          ON p.org_id = pv.org_id AND p.id = pv.problem_id AND p.source = 'catalog'
  JOIN solution s         ON s.org_id = pv.org_id AND s.problem_version_id = pv.id
  WHERE u.org_id = p_user_org AND u.id = p_user AND u.role = 'teacher' AND u.deactivated_at IS NULL
    AND u.account_kind IN ('sample', 'verified');
END $$;
COMMENT ON FUNCTION fn_guard_context_catalog IS 'E6: ngữ cảnh Kiểm soát đáp án cho hỏi đáp trong màn xem trước (không trả xuống thiết bị).';

REVOKE ALL ON FUNCTION fn_guard_context(uuid, uuid, uuid), fn_guard_context_catalog(uuid, uuid, uuid) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION fn_guard_context(uuid, uuid, uuid), fn_guard_context_catalog(uuid, uuid, uuid) TO geo_ai;

-- ---------------------------------------------------------------- 2. bộ câu thử có phiên bản (US-GEO-611)
-- Phiên bản của bộ: mỗi lần nạp có thêm hoặc bớt câu thì tăng (BR-GEO-261).
CREATE TABLE probe_set_version (
  version      integer PRIMARY KEY CHECK (version >= 1),
  prompt_count integer NOT NULL CHECK (prompt_count >= 0),
  loaded_at    timestamptz NOT NULL DEFAULT now()
);
COMMENT ON TABLE probe_set_version IS 'Phiên bản bộ câu thử (BR-GEO-261). Dữ liệu kiểm thử, không thuộc trường.';

ALTER TABLE probe_prompt
  ADD COLUMN prompt_key    text,                 -- kênh|câu đã chuẩn hóa (chữ thường, gộp khoảng trắng): chống câu trùng cho cùng bài
  ADD COLUMN channel       text NOT NULL DEFAULT 'qa' CHECK (channel IN ('qa', 'self_study', 'self_study_qa', 'teacher_route')),
  ADD COLUMN blocking      boolean NOT NULL DEFAULT false,   -- ca chặn của cổng phát hành (D-GEO-150: F-GEO-226)
  ADD COLUMN added_version integer NOT NULL DEFAULT 1;
-- FC1 (câu trùng): một câu đang dùng cho một bài chỉ có một lần
CREATE UNIQUE INDEX probe_prompt_key_uq ON probe_prompt (target_problem_version_id, prompt_key) WHERE active;

-- Báo cáo một lượt chạy (BR-GEO-257, 258): giữ mọi lượt, không sửa.
CREATE TABLE probe_report (
  id             bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  run_label      text NOT NULL CHECK (length(run_label) BETWEEN 1 AND 80),
  created_at     timestamptz NOT NULL DEFAULT now(),
  model          text NOT NULL,
  prompt_version text NOT NULL,
  guard_version  text NOT NULL,
  set_version    integer NOT NULL,
  prompt_count   integer NOT NULL,
  group_count    integer NOT NULL,
  leak_count     integer NOT NULL,
  verdict        text NOT NULL,
  report         jsonb NOT NULL
);
COMMENT ON TABLE probe_report IS 'Báo cáo lượt chạy bộ câu thử (US-GEO-610): số câu, số lần lộ theo nhóm và theo kênh, model, phiên bản prompt và bộ câu thử.';
CREATE TRIGGER probe_report_append_only BEFORE UPDATE OR DELETE ON probe_report
  FOR EACH ROW EXECUTE FUNCTION tg_append_only();

GRANT SELECT ON probe_set_version, probe_report TO geo_ops;
GRANT INSERT ON probe_report TO geo_ops;
GRANT SELECT, INSERT ON probe_set_version TO geo_content;
GRANT INSERT ON probe_prompt TO geo_content;

-- ---------------------------------------------------------------- 3. nhật ký chặn và đáp số tham chiếu (qua hàm cổng)
-- Mỗi lần chặn hoặc đóng khi lỗi của Kiểm soát đáp án ghi một dòng (BR-GEO-305); không user_id, không nội dung (D-GEO-067).
CREATE FUNCTION fn_guard_log(p_org uuid, p_assignment uuid, p_outcome guard_outcome, p_rule text, p_version text, p_ms integer)
RETURNS integer
LANGUAGE sql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
  INSERT INTO answer_guard_log (occurred_at, org_id, assignment_id, channel, outcome, rule_code, guard_version, latency_ms)
  VALUES (now(), p_org, p_assignment, 'qa', p_outcome, p_rule, p_version, GREATEST(p_ms, 0));
  SELECT 1;
$$;

-- US-GEO-611-SC2: câu thử dẫn tới đáp số tham chiếu và các dạng viết tương đương của bài (công cụ của nhóm sản phẩm).
CREATE FUNCTION fn_probe_reference(p_code text)
RETURNS TABLE (code text, technique_group text, target_code text, final_answer text, answer_forms jsonb, steps jsonb)
LANGUAGE sql STABLE SECURITY DEFINER SET search_path = public, pg_temp
AS $$
  SELECT q.code, q.technique_group, p.code, s.final_answer, s.answer_forms, s.steps
  FROM probe_prompt q
  JOIN problem_version pv ON pv.org_id = q.target_org_id AND pv.id = q.target_problem_version_id
  JOIN problem p ON p.org_id = pv.org_id AND p.id = pv.problem_id
  JOIN solution s ON s.org_id = pv.org_id AND s.problem_version_id = pv.id
  WHERE q.code = p_code;
$$;

REVOKE ALL ON FUNCTION fn_guard_log(uuid, uuid, guard_outcome, text, text, integer), fn_probe_reference(text) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION fn_guard_log(uuid, uuid, guard_outcome, text, text, integer) TO geo_ai;
GRANT EXECUTE ON FUNCTION fn_probe_reference(text) TO geo_ops;
