-- V59 (FIX3): tích hợp E4 × E5 và quyền bộ đề (bank) trên mọi đường đọc/giao đề kho.
-- 1) Bộ nạp so lời giải cũ/mới (BR-GEO-11) chỉ so phần CHỮ của bước (title, body, why) và đáp số — đúng những gì tệp
--    nguồn mang. Khóa trình bày E4 thêm vào bước (highlight, draw: US-GEO-407, 408) không có trong tệp nguồn nên không được
--    coi là "lời giải đổi" (gốc của TC-0528 trượt sau gộp E4+E5: so nguyên jsonb → nạp lại tạo version mới).
-- 2) Bộ đề ngoài 'main' chỉ tổ chức có hàng catalog_bank_access thấy — áp cả khi GIAO BÀI và XEM LỜI GIẢI, không chỉ ở
--    danh sách/đếm (phát hiện soát E5). Không thấy = NOT_FOUND (BR-GEO-112). Guard Java dùng chung
--    vn.geo.server.content.api.BankAccess kiểm lại ở tầng dịch vụ (phòng khi migration sau định nghĩa lại các hàm này).

CREATE FUNCTION fn_solution_text(p_steps jsonb)
RETURNS jsonb LANGUAGE sql IMMUTABLE SET search_path = public, pg_temp
AS $$
  SELECT COALESCE(jsonb_agg(jsonb_build_object('title', e -> 'title', 'body', e -> 'body', 'why', e -> 'why') ORDER BY i),
                  '[]'::jsonb)
  FROM jsonb_array_elements(CASE WHEN jsonb_typeof(p_steps) = 'array' THEN p_steps ELSE '[]'::jsonb END)
       WITH ORDINALITY AS x(e, i)
$$;
COMMENT ON FUNCTION fn_solution_text IS 'Phần chữ của các bước lời giải (title, body, why theo thứ tự), bỏ khóa trình bày (highlight, draw).';

CREATE OR REPLACE FUNCTION fn_content_solution_same(p_pv uuid, p_final text, p_steps jsonb)
RETURNS boolean
LANGUAGE plpgsql STABLE SECURITY DEFINER SET search_path = public, pg_temp
AS $$
BEGIN
  RETURN EXISTS (SELECT 1 FROM solution s
                 WHERE s.org_id = platform_org_id() AND s.problem_version_id = p_pv
                   AND s.final_answer = p_final AND fn_solution_text(s.steps) = fn_solution_text(p_steps));
END $$;

-- Đề kho thuộc bộ p_bank có được tổ chức p_org thấy không (main hoặc bộ được cấp).
CREATE FUNCTION fn_catalog_bank_visible(p_org uuid, p_bank text)
RETURNS boolean LANGUAGE sql STABLE SECURITY DEFINER SET search_path = public, pg_temp
AS $$
  SELECT p_bank = 'main' OR EXISTS (SELECT 1 FROM catalog_bank_access b WHERE b.org_id = p_org AND b.bank = p_bank)
$$;
COMMENT ON FUNCTION fn_catalog_bank_visible IS 'Quyền bộ đề (E5, FIX3): main mọi trường thấy; bộ khác chỉ tổ chức có hàng catalog_bank_access.';

REVOKE ALL ON FUNCTION fn_solution_text(jsonb), fn_catalog_bank_visible(uuid, text) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION fn_solution_text(jsonb) TO geo_content;
GRANT EXECUTE ON FUNCTION fn_catalog_bank_visible(uuid, text) TO geo_app, geo_content;

-- ---------------------------------------------------------------- 9.5 Bài giao: thêm điều kiện bộ đề (FIX3)
CREATE OR REPLACE FUNCTION fn_create_assignment(p_org uuid, p_actor uuid, p_assignment_id uuid, p_class uuid, p_problem_version uuid,
                                     p_release_at timestamptz, p_allow_hint boolean, p_allow_qa boolean, p_request uuid DEFAULT NULL)
RETURNS TABLE (status text, release_rev integer)
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
#variable_conflict use_column
DECLARE c record; v_problem uuid; e record;
BEGIN
  SELECT x.teacher_id, x.archived_at INTO c FROM class x WHERE x.org_id = p_org AND x.id = p_class;
  IF c.teacher_id IS NULL OR c.archived_at IS NOT NULL THEN RETURN QUERY SELECT 'NOT_FOUND'::text, NULL::int; RETURN; END IF;
  IF c.teacher_id <> p_actor THEN RETURN QUERY SELECT 'FORBIDDEN'::text, NULL::int; RETURN; END IF;
  SELECT pv.problem_id INTO v_problem
    FROM problem_version pv JOIN problem p ON p.org_id = pv.org_id AND p.id = pv.problem_id
   WHERE pv.org_id = platform_org_id() AND pv.id = p_problem_version AND p.source = 'catalog' AND p.archived_at IS NULL
     AND fn_catalog_bank_visible(p_org, p.bank);                      -- FIX3: bộ đề ngoài quyền = không tồn tại
  IF v_problem IS NULL THEN RETURN QUERY SELECT 'NOT_FOUND'::text, NULL::int; RETURN; END IF;
  IF p_release_at IS NOT NULL AND p_release_at <= now() THEN RETURN QUERY SELECT 'VALIDATION_PAST'::text, NULL::int; RETURN; END IF;
  -- gửi lại cùng id (thử lại sau hết thời gian chờ): lũy đẳng
  SELECT a.class_id, a.assigned_by, a.release_rev INTO e FROM assignment a WHERE a.org_id = p_org AND a.id = p_assignment_id;
  IF e.class_id IS NOT NULL THEN
    IF e.class_id = p_class AND e.assigned_by = p_actor THEN RETURN QUERY SELECT 'DUPLICATE'::text, e.release_rev; RETURN; END IF;
    RETURN QUERY SELECT 'CONFLICT_DUPLICATE'::text, NULL::int; RETURN;
  END IF;
  BEGIN
    INSERT INTO assignment (id, org_id, kind, class_id, content_org_id, problem_id, problem_version_id, assigned_by,
                            release_at, allow_hint, allow_qa)
    VALUES (p_assignment_id, p_org, 'class', p_class, platform_org_id(), v_problem, p_problem_version, p_actor,
            p_release_at, p_allow_hint, p_allow_qa);
  EXCEPTION WHEN unique_violation THEN
    RETURN QUERY SELECT 'CONFLICT_DUPLICATE'::text, NULL::int; RETURN;      -- D-GEO-161: cùng problem, cùng lớp
  END;
  INSERT INTO audit_log (org_id, actor_user_id, action, target_id, before_release_at, after_release_at, request_id)
  VALUES (p_org, p_actor, 'assignment_create', p_assignment_id, NULL, p_release_at, p_request);
  RETURN QUERY SELECT 'OK'::text, 0;
END $$;
COMMENT ON FUNCTION fn_create_assignment IS 'Đường DUY NHẤT tạo bài giao lớp (D-GEO-158 (5), RV-10): giáo viên của lớp, đề kho thuộc bộ trường thấy (FIX3), giờ mở NULL hoặc ở tương lai, CONFLICT_DUPLICATE khi cùng problem cùng lớp (D-GEO-161), luôn ghi audit_log. Định nghĩa lại hàm này PHẢI giữ điều kiện fn_catalog_bank_visible.';

-- Q19b: giáo viên xem lời giải — CHỈ đề kho nền tảng thuộc bộ trường thấy (D-GEO-153, RV-04, FIX3).
CREATE OR REPLACE FUNCTION fn_get_solution_teacher(p_user_org uuid, p_user uuid, p_problem_version uuid)
RETURNS TABLE (problem_version_id uuid, final_answer text, steps jsonb, schema_version smallint, ai_authored boolean)
LANGUAGE plpgsql STABLE SECURITY DEFINER SET search_path = public, pg_temp
AS $$
#variable_conflict use_column
BEGIN
  RETURN QUERY
  SELECT s.problem_version_id, s.final_answer, s.steps, s.schema_version, s.ai_authored
  FROM app_user u
  JOIN problem_version pv ON pv.org_id = platform_org_id() AND pv.id = p_problem_version
  JOIN problem p          ON p.org_id = pv.org_id AND p.id = pv.problem_id AND p.source = 'catalog'
  JOIN solution s         ON s.org_id = pv.org_id AND s.problem_version_id = pv.id
  WHERE u.org_id = p_user_org AND u.id = p_user AND u.role = 'teacher' AND u.deactivated_at IS NULL
    AND u.account_kind IN ('sample', 'verified')                 -- PO-18; tài khoản system (tác giả) không đăng nhập
    AND fn_catalog_bank_visible(p_user_org, p.bank);             -- FIX3: bộ đề ngoài quyền không lộ lời giải
END $$;
COMMENT ON FUNCTION fn_get_solution_teacher IS 'Giáo viên xem lời giải đề kho thuộc bộ trường thấy (KNW-02, FIX3). Màn xem trước KHÔNG gọi hàm này (D-GEO-153, Q23).';
