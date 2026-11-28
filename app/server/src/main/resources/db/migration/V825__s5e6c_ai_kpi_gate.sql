-- T-261128-65 · S5-E6c (US-S5E6-06): báo cáo khớp AI–giáo viên theo KPI §2–§4 và cổng bật "AI đề xuất" theo lớp.
-- Chủ bảng: module aigrade. Sau cờ geo.s5.ai-grading.enabled (bảng luôn tạo).
--   ai_kpi_paper   một hàng "bài" cho số liệu KPI của lớp (BR-S5E6-15), dựng lại từ cặp song song (ai_shadow_pair),
--                  đề xuất AI, tín hiệu engine, hành động duyệt (V725, V755). kind = shadow (bài song song: a–e, h)
--                  hoặc proposal (bài AI đề xuất đã duyệt: f, g). criteria = [{max, ai, gv}] (shadow) hoặc
--                  [{max, edited}] (proposal). KHÔNG mã học sinh, không tên (ẩn danh, chỉ số lớp).
--   ai_class_gate  trạng thái cổng của lớp (BR-S5E6-16, 17): mở lúc nào, mốc tính lại sau lần tự đóng, chuỗi đợt
--                  không đạt sau khi bật.
-- Thêm loại thông báo ai_gate ("AI đề xuất đã tạm tắt cho {lớp}"); V705 định nghĩa lại cả danh sách nên viết lại đủ.

CREATE TABLE ai_kpi_paper (
  org_id          uuid NOT NULL,
  submission_id   uuid NOT NULL,
  kind            text NOT NULL CHECK (kind IN ('shadow', 'proposal')),
  class_id        uuid NOT NULL,
  topic           text NOT NULL CHECK (length(topic) BETWEEN 1 AND 80),
  criteria        jsonb NOT NULL DEFAULT '[]'::jsonb,
  result_ok       boolean,
  invented        int NOT NULL DEFAULT 0 CHECK (invented >= 0),
  blocked         int NOT NULL DEFAULT 0 CHECK (blocked >= 0),
  no_grade        boolean NOT NULL DEFAULT false,
  review_seconds  int CHECK (review_seconds IS NULL OR review_seconds >= 0),
  created_at      timestamptz NOT NULL DEFAULT clock_timestamp(),
  PRIMARY KEY (org_id, submission_id, kind)
);
CREATE INDEX ai_kpi_paper_class_idx ON ai_kpi_paper (org_id, class_id, kind, created_at);
COMMENT ON TABLE ai_kpi_paper IS 'Số liệu KPI theo bài (US-S5E6-06, BR-S5E6-15): điểm AI/GV theo tiêu chí, đúng chiều engine, cờ bịa kiến thức, AI không chấm, thời gian duyệt. Không mã, không tên học sinh.';

CREATE TABLE ai_class_gate (
  org_id        uuid NOT NULL,
  class_id      uuid NOT NULL,
  opened_at     timestamptz,
  since         timestamptz NOT NULL DEFAULT '-infinity',
  fail_streak   int NOT NULL DEFAULT 0 CHECK (fail_streak >= 0),
  batches_done  int NOT NULL DEFAULT 0 CHECK (batches_done >= 0),
  closed_at     timestamptz,
  close_reason  text CHECK (close_reason IS NULL OR length(close_reason) <= 300),
  updated_at    timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (org_id, class_id)
);
COMMENT ON TABLE ai_class_gate IS 'Cổng "AI đề xuất" của lớp (US-S5E6-06, BR-S5E6-16, 17; QS5-21, QS5-24): mở khi đạt KPI §4 trên ≥ 20 bài song song từ mốc since; tự đóng sau hai đợt 10 bài không đạt hoặc một lần bịa kiến thức.';

ALTER TABLE notification DROP CONSTRAINT notification_kind_check;
ALTER TABLE notification ADD CONSTRAINT notification_kind_check
  CHECK (kind IN ('new_set', 'solution_open', 'release_changed', 'due_soon', 'class_notice', 'ai_quota',
                  'graded_returned', 'grade_updated', 'ai_gate'));

GRANT SELECT, INSERT, UPDATE, DELETE ON ai_kpi_paper TO geo_app;
GRANT SELECT, INSERT, UPDATE ON ai_class_gate TO geo_app;
