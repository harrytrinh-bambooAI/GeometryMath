-- Soát độc lập FIX PO-27: đẩy điều kiện phân trang (org, version) > after vào truy vấn trong, trước DISTINCT, để mỗi trang
-- không quét lại các version đã duyệt. V422 giữ nguyên (đã có thể áp dụng); CREATE OR REPLACE giữ chữ ký và quyền.

CREATE OR REPLACE FUNCTION fn_signature_stale(p_version smallint, p_after_org uuid, p_after_pv uuid, p_limit integer)
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
         WHERE o.sig_version < p_version AND o.org_id <> platform_org_id()
           AND (p_after_org IS NULL OR (o.org_id, o.problem_version_id) > (p_after_org, p_after_pv))) s
  WHERE NOT EXISTS (SELECT 1 FROM problem_signature n
                     WHERE n.org_id = s.org_id AND n.problem_version_id = s.problem_version_id AND n.sig_version = p_version)
  ORDER BY s.org_id, s.problem_version_id
  LIMIT p_limit;
END $$;
