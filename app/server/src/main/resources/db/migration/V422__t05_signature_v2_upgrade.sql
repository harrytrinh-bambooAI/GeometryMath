-- Câu mở sau FIX T-05 (inbox 27/11/2026): chữ ký chuẩn đổi bản 1 → 2 (GeometryEngine.SIG_VERSION = 2). Hàng problem_signature
-- bản cũ ở môi trường có dữ liệu thật không còn khớp đề kho bản 2 (fn_self_lock_source so cùng sig_version) nên bài tự học
-- không kế thừa khóa. Công việc SignatureUpgrade (ứng dụng, chạy khi khởi động, lũy đẳng) đọc các version còn thiếu chữ ký bản
-- mới cùng mô tả dựng đã lưu (scene_build.spec bản dựng mới nhất), engine tính lại, ghi hàng bản mới qua fn_signature_put.
-- Hàng bản cũ giữ nguyên (bảng không cho geo_app xóa; không còn đề kho bản cũ nào để khớp). Đề kho (org nền tảng) do seed /
-- vai nạp nội dung ghi sẵn bản mới nên không thuộc công việc này.

CREATE FUNCTION fn_signature_stale(p_version smallint, p_after_org uuid, p_after_pv uuid, p_limit integer)
RETURNS TABLE (org_id uuid, problem_version_id uuid, spec text)
LANGUAGE plpgsql STABLE SECURITY DEFINER SET search_path = public, pg_temp
AS $$
#variable_conflict use_column
BEGIN
  RETURN QUERY
  SELECT s.org_id, s.problem_version_id,
         (SELECT sb.spec::text FROM scene_build sb
           WHERE sb.org_id = s.org_id AND sb.problem_version_id = s.problem_version_id
           ORDER BY sb.build_no DESC LIMIT 1)
  FROM (SELECT DISTINCT o.org_id, o.problem_version_id FROM problem_signature o
         WHERE o.sig_version < p_version AND o.org_id <> platform_org_id()) s
  WHERE NOT EXISTS (SELECT 1 FROM problem_signature n
                     WHERE n.org_id = s.org_id AND n.problem_version_id = s.problem_version_id AND n.sig_version = p_version)
    AND (p_after_org IS NULL OR (s.org_id, s.problem_version_id) > (p_after_org, p_after_pv))
  ORDER BY s.org_id, s.problem_version_id
  LIMIT p_limit;
END $$;
COMMENT ON FUNCTION fn_signature_stale IS 'T-05: version đề tự nhập còn thiếu chữ ký bản p_version (có bản cũ hơn), kèm mô tả dựng mới nhất; phân trang theo khóa (org, version).';

CREATE FUNCTION fn_signature_put(p_org uuid, p_pv uuid, p_version smallint, p_solid text, p_params jsonb, p_asked text,
                                 p_signature bytea)
RETURNS text
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
BEGIN
  IF p_org = platform_org_id() THEN RETURN 'PLATFORM'; END IF;
  INSERT INTO problem_signature (org_id, problem_version_id, sig_version, template_id, params_norm, asked_quantity, signature)
  SELECT p_org, p_pv, p_version, st.id, p_params, p_asked, p_signature FROM solid_template st WHERE st.code = p_solid
  ON CONFLICT (org_id, problem_version_id, sig_version) DO NOTHING;
  RETURN CASE WHEN FOUND THEN 'OK' ELSE 'SKIPPED' END;
END $$;
COMMENT ON FUNCTION fn_signature_put IS 'T-05: ghi chữ ký bản mới của một version đề tự nhập (lũy đẳng). Không ghi đề kho nền tảng.';

REVOKE ALL ON FUNCTION fn_signature_stale(smallint, uuid, uuid, integer) FROM PUBLIC;
REVOKE ALL ON FUNCTION fn_signature_put(uuid, uuid, smallint, text, jsonb, text, bytea) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION fn_signature_stale(smallint, uuid, uuid, integer) TO geo_app;
GRANT EXECUTE ON FUNCTION fn_signature_put(uuid, uuid, smallint, text, jsonb, text, bytea) TO geo_app;
