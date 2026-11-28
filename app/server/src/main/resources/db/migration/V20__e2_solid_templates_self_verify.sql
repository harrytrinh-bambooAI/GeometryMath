-- V20 (E2): danh mục hình mẫu (US-GEO-205, D-GEO-056, 133) và hàm cổng ghi báo cáo kiểm chứng của đề tự nhập
-- (US-GEO-203: nhãn máy chỉ engine tính; mã ứng dụng không chạm verification_report, sql-lint forbidden.app.tables).

-- ---------------------------------------------------------------- danh mục hình mẫu (nội dung hệ thống, không org_id)
-- param_spec: {order, card, builder, params:[{key, label, default}]}. Giá trị mặc định chưa có nguồn (D-GEO-109): số > 0
-- do E2 đặt. "Hình lập phương" là hộp ba cạnh bằng nhau, không có thẻ riêng (US-GEO-205-SC1, SC2).
INSERT INTO solid_template (id, code, solid_kind, name, schema_version, param_spec) VALUES
  (1, 'pyramid',  'pyramid',  'Hình chóp', 1,
   '{"order":1,"card":true,"builder":"pyramid","params":[{"key":"a","label":"Cạnh đáy","default":2},{"key":"h","label":"Chiều cao","default":3}]}'),
  (2, 'prism',    'prism',    'Lăng trụ', 1,
   '{"order":2,"card":true,"builder":"prism","params":[{"key":"a","label":"Cạnh đáy","default":2},{"key":"h","label":"Chiều cao","default":3}]}'),
  (3, 'box',      'box',      'Hình hộp', 1,
   '{"order":3,"card":true,"builder":"box","params":[{"key":"a","label":"Chiều dài","default":3},{"key":"b","label":"Chiều rộng","default":2},{"key":"c","label":"Chiều cao","default":2}]}'),
  (4, 'cube',     'cube',     'Hình lập phương', 1,
   '{"order":4,"card":false,"builder":"cube","params":[{"key":"a","label":"Cạnh","default":2}]}'),
  (5, 'cone',     'cone',     'Hình nón', 1,
   '{"order":5,"card":true,"builder":"cone","params":[{"key":"r","label":"Bán kính đáy","default":2},{"key":"h","label":"Chiều cao","default":3}]}'),
  (6, 'cylinder', 'cylinder', 'Hình trụ', 1,
   '{"order":6,"card":true,"builder":"cylinder","params":[{"key":"r","label":"Bán kính đáy","default":2},{"key":"h","label":"Chiều cao","default":3}]}'),
  (7, 'sphere',   'sphere',   'Hình cầu', 1,
   '{"order":7,"card":true,"builder":"sphere","params":[{"key":"r","label":"Bán kính","default":2}]}')
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------- báo cáo kiểm chứng của đề tự nhập (đường tự học)
-- Ghi báo cáo của MỘT version đề tự nhập thuộc trường p_org và đặt nhãn máy theo đúng báo cáo (BR-07, D-GEO-158).
-- Đề kho đi qua fn_record_verification (V11, vai geo_content).
CREATE FUNCTION fn_record_self_verification(p_org uuid, p_pv uuid, p_report uuid, p_scene_ok boolean, p_answer_ok boolean,
                                            p_checks jsonb, p_actor uuid)
RETURNS verify_status LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
DECLARE v_result verify_status := CASE WHEN p_scene_ok AND p_answer_ok THEN 'verified' ELSE 'needs_review' END::verify_status;
BEGIN
  IF NOT EXISTS (SELECT 1 FROM problem_version pv JOIN problem p ON p.org_id = pv.org_id AND p.id = pv.problem_id
                 WHERE pv.org_id = p_org AND pv.id = p_pv AND p.source = 'student_input') THEN
    RAISE EXCEPTION 'không có version đề tự nhập % trong trường %', p_pv, p_org USING ERRCODE = 'no_data_found';
  END IF;
  INSERT INTO verification_report (id, org_id, problem_version_id, scene_ok, answer_ok, result, checks)
  VALUES (p_report, p_org, p_pv, p_scene_ok, p_answer_ok, v_result, p_checks);
  PERFORM fn_set_verify_status(p_org, p_pv, p_report, p_actor);
  RETURN v_result;
END $$;
COMMENT ON FUNCTION fn_record_self_verification IS 'E2: báo cáo kiểm chứng + nhãn máy của đề tự nhập (bộ kiểm chứng của tự học, vai geo_ai). Nhãn = kết quả engine; không có tham số nhãn.';

REVOKE ALL ON FUNCTION fn_record_self_verification(uuid, uuid, uuid, boolean, boolean, jsonb, uuid) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION fn_record_self_verification(uuid, uuid, uuid, boolean, boolean, jsonb, uuid) TO geo_ai;
