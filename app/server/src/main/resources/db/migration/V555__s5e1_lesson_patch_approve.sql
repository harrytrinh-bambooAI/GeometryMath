-- T-261128-54 (S5-E1: US-S5E1-04 chat vá, US-S5E1-05 duyệt + xuất; PLAN_code-s5 §4 khoảng V555–V574).

-- Duyệt (BR-S5E1-13): phiên bản đã duyệt được khoá; sửa sau duyệt tạo phiên bản mới mang nhãn "AI soạn".
ALTER TABLE lesson_plan
  ADD COLUMN approved_version int,
  ADD COLUMN approved_by      uuid,
  ADD COLUMN approved_at      timestamptz;

-- Nguồn của từng phiên bản (soạn đầu, nhận bản vá, hoàn tác không tạo phiên bản).
ALTER TABLE lesson_plan_version
  ADD COLUMN source text NOT NULL DEFAULT 'plan' CHECK (source IN ('plan', 'patch', 'manual'));

-- Bản vá AI đề xuất (US-S5E1-04): chỉ áp khi giáo viên bấm Nhận (BR-S5E1-10). Không lưu câu chat của giáo viên.
-- decisions: trạng thái từng dòng (pending | accepted | skipped), cùng thứ tự với patches.
CREATE TABLE lesson_plan_patch (
  id           uuid PRIMARY KEY,
  org_id       uuid NOT NULL,
  plan_id      uuid NOT NULL REFERENCES lesson_plan (id) ON DELETE CASCADE,
  base_version int  NOT NULL,
  section      text,
  patches      jsonb NOT NULL,
  decisions    jsonb NOT NULL,
  created_at   timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX lesson_plan_patch_plan_idx ON lesson_plan_patch (org_id, plan_id);

GRANT SELECT, INSERT, UPDATE ON lesson_plan_patch TO geo_app;
