-- S2-E2 (T-261126-19, PLAN_code-s23 A2/A3; US-S2E2-02, 04). Chủ: module content.
-- 1) Problem.kind: 'space' (mọi đề MVP, mặc định, không đổi đề cũ) | 'plane' (đề hình phẳng).
-- 2) plane_problem_spec: mô tả dựng hình phẳng của một version đề (ràng buộc, đại lượng hỏi; SCENE-CONTRACT-2D.md §2).
--    Không chứa đáp số. Đề phẳng dựng lại bằng engine mỗi lần xem (hàm thuần, tất định).
-- 3) plane_benchmark_problem: bộ đề chuẩn hình phẳng đã nạp (PLS-nn) với đáp số tham chiếu và người soát
--    (BR-S2E2-12…14). Dữ liệu nền tảng dùng chung, không thuộc trường nào (không có org_id). Mỗi lần nạp thay cả bộ.

CREATE TYPE problem_kind AS ENUM ('space', 'plane');

ALTER TABLE problem ADD COLUMN kind problem_kind NOT NULL DEFAULT 'space';
-- Loại khối (solid_kind) chỉ có nghĩa với đề không gian: đề phẳng để trống.
ALTER TABLE problem ALTER COLUMN solid_kind DROP NOT NULL;
ALTER TABLE problem ADD CONSTRAINT problem_solid_kind_ck CHECK (kind = 'plane' OR solid_kind IS NOT NULL);

-- Bộ kiểm chứng cả kho (CatalogVerifier, E5) và bộ đề chuẩn MVP chỉ xét đề KHÔNG GIAN: đề phẳng kiểm bằng
-- engine hình phẳng và bộ đề chuẩn riêng (plane_benchmark_problem). Thân hàm giữ nguyên V50, thêm p.kind = 'space'.
CREATE OR REPLACE FUNCTION fn_catalog_reference()
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
    AND p.kind = 'space'
  ORDER BY p.code, p.id;
END $$;

CREATE TABLE plane_problem_spec (
  org_id             uuid NOT NULL,
  problem_version_id uuid NOT NULL,
  spec               jsonb NOT NULL CHECK (jsonb_typeof(spec) = 'object' AND spec ? 'shape'),
  created_at         timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (org_id, problem_version_id),
  FOREIGN KEY (org_id, problem_version_id) REFERENCES problem_version (org_id, id)
);

CREATE TABLE plane_benchmark_problem (
  code        text PRIMARY KEY CHECK (code ~ '^PLS-[0-9]{2,3}$'),
  alias       text,
  statement   text NOT NULL CHECK (length(statement) BETWEEN 1 AND 4000),
  shape       text NOT NULL,
  form        text NOT NULL,
  spec        jsonb NOT NULL,
  answer      text NOT NULL CHECK (length(answer) BETWEEN 1 AND 200),
  answer_type text NOT NULL,
  reviewer    text NOT NULL CHECK (length(reviewer) BETWEEN 1 AND 200),
  loaded_at   timestamptz NOT NULL DEFAULT now()
);

-- Quyền theo vai (mẫu V10): ứng dụng đọc; vai nội dung nạp bộ đề chuẩn và mô tả dựng.
GRANT SELECT ON plane_problem_spec, plane_benchmark_problem TO geo_app, geo_content;
GRANT INSERT ON plane_problem_spec TO geo_content;
GRANT INSERT, DELETE ON plane_benchmark_problem TO geo_content;
