-- S2-E3 (T-261126-22, PLAN_code-s23 §2, §4; US-S2E3-01…04) + US-S2E5-05. Chủ: module content.
-- Thêm đề có AI soạn nháp (khung C2-02):
--   problem_draft     : nháp đề của MỘT giáo viên (BR-S2E3-04): chữ đang gõ, ô chữ đọc từ ảnh (độ tin cậy, đã sửa / bấm "đúng"),
--                       ảnh lưu tạm ĐÃ xóa siêu dữ liệu (BR-S2E3-09; xóa khi chữ được xác nhận), trạng thái nháp và sổ lời giải
--                       sinh ra (solution_doc của S2-E5) khi bốn bước chạy xong. Đếm nháp tạo trong 1 giờ = giới hạn thêm đề.
--   problem_draft_job : tác vụ bốn bước đọc–dựng–giải–kiểm chạy ở máy chủ (BR-S2E3-11…13). stages = trạng thái từng bước
--                       (chờ / đang / xong / lỗi), số lượt gọi AI của từng bước; outcome = loại đề, mô tả dựng, đáp số đề ghi.
--                       Không chứa bước giải: lời giải AI soạn ghi thẳng vào solution_version (cột nhạy cảm của V160).
--                       Một nháp có nhiều nhất MỘT tác vụ đang chạy (chỉ mục duy nhất một phần): bấm "Dựng hình" hai lần
--                       chỉ tạo một tác vụ (US-S2E3-03-FC2).
--   solution_doc      : + stated_answer (đáp số đề ghi, BR-S2E3-17; là một phần chữ đề giáo viên tự dán) + figure_source
--                       ('engine' | 'ai': hình do AI dựng, BR-S2E3-15) + draft_id.
-- US-S2E5-05: bài giao gắn phiên bản lời giải cố định (solution_assignment_pin của V160). Đổi ghim chỉ bằng "Áp dụng cho lớp"
-- (UPDATE version_id); fn_get_solution trả nội dung theo phiên bản ghim khi bài giao có ghim (giữ nguyên cổng giờ mở).

CREATE TYPE problem_draft_status AS ENUM ('editing', 'building', 'ready', 'ai_figure', 'text_only');

CREATE TABLE problem_draft (
  id             uuid PRIMARY KEY,
  org_id         uuid NOT NULL REFERENCES org (id),
  owner_user_id  uuid NOT NULL,
  source         text NOT NULL DEFAULT 'text' CHECK (source IN ('text', 'image')),
  statement      text NOT NULL DEFAULT '' CHECK (length(statement) <= 2000),
  cells          jsonb NOT NULL DEFAULT '[]' CHECK (jsonb_typeof(cells) = 'array'),
  text_confirmed boolean NOT NULL DEFAULT false,
  image_tmp      bytea,                                   -- ảnh lưu tạm đã xóa siêu dữ liệu; NULL sau khi xác nhận chữ
  image_total    integer CHECK (image_total IS NULL OR image_total BETWEEN 1 AND 5242880),
  image_type     text CHECK (image_type IS NULL OR image_type IN ('jpeg', 'png')),
  status         problem_draft_status NOT NULL DEFAULT 'editing',
  doc_id         uuid,
  assign_confirmed_at timestamptz,                        -- đã bấm xác nhận riêng "Hình do AI dựng… Vẫn giao?" (BR-S2E3-16)
  created_at     timestamptz NOT NULL DEFAULT now(),
  updated_at     timestamptz NOT NULL DEFAULT now(),
  UNIQUE (org_id, id),
  FOREIGN KEY (org_id, owner_user_id) REFERENCES app_user (org_id, id),
  FOREIGN KEY (org_id, doc_id) REFERENCES solution_doc (org_id, id)
);
CREATE INDEX problem_draft_owner_idx ON problem_draft (org_id, owner_user_id, created_at DESC, id DESC);

CREATE TABLE problem_draft_job (
  id            uuid PRIMARY KEY,
  org_id        uuid NOT NULL,
  draft_id      uuid NOT NULL,
  owner_user_id uuid NOT NULL,
  state         text NOT NULL DEFAULT 'running' CHECK (state IN ('running', 'done', 'failed', 'cancelled')),
  stages        jsonb NOT NULL CHECK (jsonb_typeof(stages) = 'array' AND jsonb_array_length(stages) = 4),
  outcome       jsonb NOT NULL DEFAULT '{}' CHECK (jsonb_typeof(outcome) = 'object'),
  created_at    timestamptz NOT NULL DEFAULT now(),
  updated_at    timestamptz NOT NULL DEFAULT now(),
  UNIQUE (org_id, id),
  FOREIGN KEY (org_id, draft_id) REFERENCES problem_draft (org_id, id),
  FOREIGN KEY (org_id, owner_user_id) REFERENCES app_user (org_id, id)
);
CREATE UNIQUE INDEX problem_draft_job_running_uq ON problem_draft_job (org_id, draft_id) WHERE state = 'running';
CREATE INDEX problem_draft_job_draft_idx ON problem_draft_job (org_id, draft_id, created_at DESC, id DESC);

ALTER TABLE solution_doc ADD COLUMN stated_answer text CHECK (stated_answer IS NULL OR length(stated_answer) BETWEEN 1 AND 100);
ALTER TABLE solution_doc ADD COLUMN figure_source text NOT NULL DEFAULT 'engine' CHECK (figure_source IN ('engine', 'ai'));
ALTER TABLE solution_doc ADD COLUMN draft_id uuid;

GRANT SELECT, INSERT, UPDATE ON problem_draft, problem_draft_job TO geo_app;
GRANT UPDATE (stated_answer, figure_source, draft_id) ON solution_doc TO geo_app;
GRANT UPDATE (version_id, pinned_at) ON solution_assignment_pin TO geo_app;

-- ---------------------------------------------------------------- US-S2E5-05: lời giải học sinh nhận theo phiên bản ghim
-- Giữ nguyên cổng giờ mở của MVP (fn_solution_gate). Bài giao có ghim: nội dung là phiên bản ghim (bước "Bước n", đáp số =
-- phần sau dấu "=" cuối cùng của bước cuối); không ghim: bảng solution của MVP như cũ.
CREATE OR REPLACE FUNCTION fn_get_solution(p_org uuid, p_user uuid, p_assignment uuid)
RETURNS TABLE (status text, problem_version_id uuid, final_answer text, steps jsonb, schema_version smallint, ai_authored boolean, release_rev integer)
LANGUAGE plpgsql STABLE SECURITY DEFINER SET search_path = public, pg_temp
AS $$
#variable_conflict use_column
DECLARE g record; pv record;
BEGIN
  SELECT * INTO g FROM fn_solution_gate(p_org, p_user, p_assignment);
  IF g.status <> 'OK' THEN
    RETURN QUERY SELECT g.status, NULL::uuid, NULL::text, NULL::jsonb, NULL::smallint, NULL::boolean, g.release_rev; RETURN;
  END IF;
  SELECT v.steps AS st, v.ai_authored AS ai INTO pv
  FROM solution_assignment_pin sp
  JOIN solution_version v ON v.org_id = sp.org_id AND v.id = sp.version_id
  WHERE sp.org_id = p_org AND sp.assignment_id = p_assignment;
  IF FOUND THEN
    RETURN QUERY SELECT 'OK'::text, g.problem_version_id,
      btrim(COALESCE((regexp_match(pv.st ->> (jsonb_array_length(pv.st) - 1), '=\s*([^=]+?)\.?\s*$'))[1],
                     pv.st ->> (jsonb_array_length(pv.st) - 1))),
      (SELECT jsonb_agg(jsonb_build_object('title', 'Bước ' || e.n, 'body', e.t, 'why', '') ORDER BY e.n)
       FROM jsonb_array_elements_text(pv.st) WITH ORDINALITY AS e(t, n)),
      1::smallint, pv.ai, g.release_rev;
    RETURN;
  END IF;
  RETURN QUERY SELECT 'OK'::text, s.problem_version_id, s.final_answer, s.steps, s.schema_version, s.ai_authored, g.release_rev
    FROM solution s WHERE s.org_id = g.content_org_id AND s.problem_version_id = g.problem_version_id;
  IF NOT FOUND THEN
    RETURN QUERY SELECT 'NO_SOLUTION'::text, NULL::uuid, NULL::text, NULL::jsonb, NULL::smallint, NULL::boolean, g.release_rev;
  END IF;
END $$;
COMMENT ON FUNCTION fn_get_solution IS 'Đường DUY NHẤT để học sinh nhận lời giải (BR-01, ANS-02). Luôn trả đúng 1 hàng: status + nội dung chỉ khi OK. Bài giao có ghim phiên bản lời giải (S2-E5) trả phiên bản ghim (US-S2E5-05). SECURITY DEFINER vì geo_app không có SELECT trên solution.';
