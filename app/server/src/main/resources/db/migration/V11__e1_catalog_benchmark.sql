-- V11 (E1): mã bài của kho đề, bộ đề chuẩn có phiên bản, báo cáo chạy bộ đề chuẩn, hàm cổng cho bảng nhạy cảm
-- (solution, verification_report: mã ứng dụng không chạm trực tiếp, sql-lint forbidden.app.tables), đọc câu trả lời
-- hỏi đáp đã lưu của chính người hỏi (D-GEO-159). US-GEO-104, 105, 106.

-- ---------------------------------------------------------------- mã bài kho đề (HKG-nn), một bản ghi mỗi mã (BR-GEO-118)
ALTER TABLE problem ADD COLUMN code text CHECK (code ~ '^[A-Z]{2,5}-[0-9]{2,4}$');
-- Kho đề sắp theo mã bài, keyset (code, id); mã duy nhất trong tổ chức sở hữu nội dung
CREATE UNIQUE INDEX problem_code_uq ON problem (org_id, code) WHERE code IS NOT NULL;

-- ---------------------------------------------------------------- bộ đề chuẩn (US-GEO-105, 106)
CREATE TABLE benchmark_set (
  version      integer PRIMARY KEY CHECK (version >= 1),
  content_hash bytea NOT NULL CHECK (length(content_hash) = 32),   -- sha256 nội dung bộ đề: đổi nội dung => phiên bản mới (BR-GEO-121)
  source       text NOT NULL CHECK (length(source) BETWEEN 1 AND 300),
  created_at   timestamptz NOT NULL DEFAULT now()
);
COMMENT ON TABLE benchmark_set IS 'Phiên bản bộ đề chuẩn (BR-GEO-121). Nội dung hệ thống, không thuộc trường.';

CREATE TABLE benchmark_item (
  set_version        integer NOT NULL REFERENCES benchmark_set (version),
  code               text NOT NULL,
  org_id             uuid NOT NULL,
  problem_id         uuid NOT NULL,
  problem_version_id uuid NOT NULL,
  missing_reference  boolean NOT NULL,       -- đề thiếu đáp số tham chiếu vẫn thuộc bộ (US-GEO-105-FC2, 106-FC1)
  PRIMARY KEY (set_version, code),
  FOREIGN KEY (org_id, problem_id, problem_version_id) REFERENCES problem_version (org_id, problem_id, id)
);
COMMENT ON TABLE benchmark_item IS 'Đề thuộc một phiên bản bộ đề chuẩn; trỏ đúng bản ghi của kho đề (BR-GEO-118: không bản sao).';

CREATE TABLE benchmark_run (
  id             uuid PRIMARY KEY,
  set_version    integer REFERENCES benchmark_set (version),
  created_at     timestamptz NOT NULL DEFAULT now(),
  model          text NOT NULL,
  prompt_version text NOT NULL,
  simulated      boolean NOT NULL,
  n              integer NOT NULL CHECK (n >= 0),
  scene_ok       integer NOT NULL CHECK (scene_ok >= 0),
  answer_ok      integer NOT NULL CHECK (answer_ok >= 0),
  wrong_verified integer NOT NULL CHECK (wrong_verified >= 0),
  verified       integer NOT NULL CHECK (verified >= 0),
  items          jsonb NOT NULL,             -- từng đề: mã, hình thỏa, đáp số khớp, nhãn (không chứa giá trị đáp số)
  report         text NOT NULL
);
COMMENT ON TABLE benchmark_run IS 'Báo cáo mỗi lần chạy bộ đề chuẩn (BR-GEO-114…117). Chỉ thêm; chạy lại khi đổi model giữ báo cáo cũ (US-GEO-105-SC3).';
CREATE TRIGGER benchmark_run_append_only BEFORE UPDATE OR DELETE ON benchmark_run
  FOR EACH ROW EXECUTE FUNCTION tg_append_only();

-- ---------------------------------------------------------------- hàm cổng của nạp kho đề và bộ kiểm chứng (vai geo_content)
-- Ghi lời giải + đáp số tham chiếu (answer_forms: từng đại lượng, giá trị theo bộ tham số) của đề kho.
CREATE FUNCTION fn_content_put_solution(p_pv uuid, p_final text, p_steps jsonb, p_forms jsonb, p_model text, p_prompt text)
RETURNS text LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM problem_version pv WHERE pv.org_id = platform_org_id() AND pv.id = p_pv) THEN RETURN 'NOT_FOUND'; END IF;
  INSERT INTO solution (org_id, problem_version_id, final_answer, steps, answer_forms, model, prompt_version, author_user_id, ai_authored)
  VALUES (platform_org_id(), p_pv, p_final, p_steps, p_forms, p_model, p_prompt,
          (SELECT u.id FROM app_user u WHERE u.org_id = platform_org_id() AND u.account_kind = 'system' ORDER BY u.id LIMIT 1), true)
  ON CONFLICT (org_id, problem_version_id) DO NOTHING;
  RETURN 'OK';
END $$;

-- Đáp số tham chiếu của các đề trong một phiên bản bộ đề (chỉ bộ kiểm chứng dùng; chứa đáp số).
CREATE FUNCTION fn_benchmark_reference(p_set integer)
RETURNS TABLE (code text, problem_id uuid, problem_version_id uuid, statement text, missing_reference boolean,
               final_answer text, answer_forms jsonb)
LANGUAGE plpgsql STABLE SECURITY DEFINER SET search_path = public, pg_temp
AS $$
#variable_conflict use_column
BEGIN
  RETURN QUERY
  SELECT bi.code, bi.problem_id, bi.problem_version_id, pv.statement, bi.missing_reference, s.final_answer, s.answer_forms
  FROM benchmark_item bi
  JOIN problem_version pv ON pv.org_id = bi.org_id AND pv.id = bi.problem_version_id
  LEFT JOIN solution s    ON s.org_id = bi.org_id AND s.problem_version_id = bi.problem_version_id
  WHERE bi.set_version = p_set
  ORDER BY bi.code;
END $$;

-- Ghi báo cáo kiểm chứng của một version đề kho và đặt nhãn máy theo đúng báo cáo (BR-07, D-GEO-158).
CREATE FUNCTION fn_record_verification(p_pv uuid, p_report uuid, p_scene_ok boolean, p_answer_ok boolean, p_checks jsonb)
RETURNS verify_status LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
DECLARE v_result verify_status := CASE WHEN p_scene_ok AND p_answer_ok THEN 'verified' ELSE 'needs_review' END::verify_status;
        v_actor uuid;
BEGIN
  INSERT INTO verification_report (id, org_id, problem_version_id, scene_ok, answer_ok, result, checks)
  VALUES (p_report, platform_org_id(), p_pv, p_scene_ok, p_answer_ok, v_result, p_checks);
  SELECT u.id INTO v_actor FROM app_user u WHERE u.org_id = platform_org_id() AND u.account_kind = 'system' ORDER BY u.id LIMIT 1;
  PERFORM fn_set_verify_status(platform_org_id(), p_pv, p_report, v_actor);
  RETURN v_result;
END $$;

-- Câu trả lời đã lưu của MỘT lượt hỏi đáp, chỉ cho chính người hỏi (D-GEO-159, US-GEO-104-FC9).
CREATE FUNCTION fn_qa_answer(p_org uuid, p_user uuid, p_id bigint)
RETURNS TABLE (id bigint, assignment_id uuid, asked_at timestamptz, question text, reply_kind qa_reply_kind, reply_text text)
LANGUAGE plpgsql STABLE SECURITY DEFINER SET search_path = public, pg_temp
AS $$
#variable_conflict use_column
BEGIN
  RETURN QUERY
  SELECT q.id, q.assignment_id, q.asked_at, q.question, q.reply_kind, q.reply_text
  FROM qa_turn q WHERE q.org_id = p_org AND q.user_id = p_user AND q.id = p_id;
END $$;

GRANT SELECT ON benchmark_set, benchmark_item, benchmark_run TO geo_app, geo_content;
GRANT INSERT ON benchmark_set, benchmark_item, benchmark_run TO geo_content;
GRANT EXECUTE ON FUNCTION fn_qa_answer(uuid, uuid, bigint) TO geo_app;
GRANT EXECUTE ON FUNCTION fn_content_put_solution(uuid, text, jsonb, jsonb, text, text),
                          fn_benchmark_reference(integer),
                          fn_record_verification(uuid, uuid, boolean, boolean, jsonb) TO geo_content;
