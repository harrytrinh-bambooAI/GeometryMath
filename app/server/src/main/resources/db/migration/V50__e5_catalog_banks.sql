-- V50 (E5): Kho đề của Giáo viên mẫu (US-GEO-501…505).
-- 1) "Bộ đề" (bank) của kho nền tảng: mọi trường thấy bộ 'main' (kho thật, D-GEO-076); một tổ chức có thể được cấp thêm
--    bộ khác qua catalog_bank_access (ví dụ kho thử F-GEO-080 "chỉ để kiểm lọc và phân trang, không phải nội dung kho thật").
--    Kho vẫn là nội dung nền tảng (problem_scope_ck giữ nguyên); bộ chỉ quyết định ai THẤY đề trong danh sách Kho đề.
-- 2) Hàm cổng đọc số bước lời giải cho thẻ đề ("AI soạn · k bước", BR-GEO-204) mà mã ứng dụng không chạm bảng solution.
-- 3) Hàm cổng cho bộ kiểm chứng cả kho (BR-GEO-213) và cho bộ nạp so lời giải cũ/mới (BR-GEO-11, US-GEO-504-SC5).

ALTER TABLE problem ADD COLUMN bank text NOT NULL DEFAULT 'main' CHECK (bank ~ '^[a-z0-9_]{2,40}$');
COMMENT ON COLUMN problem.bank IS 'Bộ đề của kho nền tảng (E5). main = kho thật mọi trường thấy; bộ khác chỉ tổ chức có hàng catalog_bank_access thấy.';

-- Q13 (E5): danh sách Kho đề theo bộ, sắp theo mã bài, keyset (code, id); lọc cấp/hình/dạng bài trên cùng chỉ mục.
CREATE INDEX problem_catalog_bank_code_idx ON problem (org_id, bank, code, id)
  INCLUDE (level, solid_kind, topic_id, current_version_id)
  WHERE source = 'catalog' AND archived_at IS NULL AND code IS NOT NULL;

CREATE TABLE catalog_bank_access (
  org_id     uuid NOT NULL REFERENCES org (id),
  bank       text NOT NULL CHECK (bank ~ '^[a-z0-9_]{2,40}$' AND bank <> 'main'),
  granted_at timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (org_id, bank)
);
COMMENT ON TABLE catalog_bank_access IS 'Bộ đề thêm (ngoài main) mà một tổ chức thấy trong Kho đề (E5). Không có hàng = chỉ kho thật.';
GRANT SELECT ON catalog_bank_access TO geo_app, geo_content;
GRANT INSERT, DELETE ON catalog_bank_access TO geo_content;

-- ---------------------------------------------------------------- số bước lời giải của đề kho (không chứa lời giải)
CREATE FUNCTION fn_catalog_step_counts(p_pvs uuid[])
RETURNS TABLE (problem_version_id uuid, step_count integer)
LANGUAGE plpgsql STABLE SECURITY DEFINER SET search_path = public, pg_temp
AS $$
#variable_conflict use_column
BEGIN
  RETURN QUERY
  SELECT s.problem_version_id, jsonb_array_length(s.steps)
  FROM solution s
  WHERE s.org_id = platform_org_id() AND s.problem_version_id = ANY (p_pvs) AND jsonb_typeof(s.steps) = 'array';
END $$;
COMMENT ON FUNCTION fn_catalog_step_counts IS 'E5: số bước lời giải của các version đề kho cho thẻ đề (BR-GEO-204). Chỉ số đếm, không lộ nội dung lời giải.';

-- ---------------------------------------------------------------- bộ kiểm chứng cả kho (vai geo_content)
-- Mọi version hiện hành của đề kho đang dùng (mọi bộ), kèm đáp số của lời giải (rỗng khi chưa có lời giải).
CREATE FUNCTION fn_catalog_reference()
RETURNS TABLE (code text, bank text, problem_id uuid, problem_version_id uuid, final_answer text)
LANGUAGE plpgsql STABLE SECURITY DEFINER SET search_path = public, pg_temp
AS $$
#variable_conflict use_column
BEGIN
  RETURN QUERY
  SELECT p.code, p.bank, p.id, pv.id, s.final_answer
  FROM problem p
  JOIN problem_version pv ON pv.org_id = p.org_id AND pv.id = p.current_version_id
  LEFT JOIN solution s    ON s.org_id = pv.org_id AND s.problem_version_id = pv.id
  WHERE p.org_id = platform_org_id() AND p.source = 'catalog' AND p.archived_at IS NULL AND p.code IS NOT NULL
  ORDER BY p.code, p.id;
END $$;

-- Lời giải của version có trùng với lời giải trong tệp nạp không (bộ nạp quyết định tạo version mới, BR-GEO-11).
CREATE FUNCTION fn_content_solution_same(p_pv uuid, p_final text, p_steps jsonb)
RETURNS boolean
LANGUAGE plpgsql STABLE SECURITY DEFINER SET search_path = public, pg_temp
AS $$
BEGIN
  RETURN EXISTS (SELECT 1 FROM solution s
                 WHERE s.org_id = platform_org_id() AND s.problem_version_id = p_pv
                   AND s.final_answer = p_final AND s.steps = p_steps);
END $$;

REVOKE ALL ON FUNCTION fn_catalog_step_counts(uuid[]), fn_catalog_reference(), fn_content_solution_same(uuid, text, jsonb) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION fn_catalog_step_counts(uuid[]) TO geo_app;
GRANT EXECUTE ON FUNCTION fn_catalog_reference(), fn_content_solution_same(uuid, text, jsonb) TO geo_content;
