-- T-261128-61 · S5-E6 (US-S5E6-01, 03): đề xuất điểm AI theo tiêu chí (GradingProposal / CriterionAssessment, STAGES §8)
-- và tín hiệu engine (đáp số cuối trong bản đọc chữ so với đáp số engine). Chủ bảng: module aigrade (PLAN_code-s5 §2).
-- Bất biến: mỗi lần chấm nền thêm MỘT hàng mới (theo phiên bản bản đọc chữ); không UPDATE nội dung (BR-S5E6-04).

CREATE TABLE ai_grade_proposal (
  id                 uuid PRIMARY KEY,
  org_id             uuid NOT NULL,
  submission_id      uuid NOT NULL,
  version_id         uuid NOT NULL,
  submission_version int  NOT NULL CHECK (submission_version >= 1),
  ocr_artifact_id    uuid NOT NULL,
  ocr_version        int  NOT NULL CHECK (ocr_version >= 1),
  assignment_id      uuid NOT NULL,
  class_id           uuid NOT NULL,
  student_id         uuid NOT NULL,
  problem_version_id uuid,
  rubric_version_id  uuid NOT NULL,
  rubric_version     int  NOT NULL CHECK (rubric_version >= 1),
  status             text NOT NULL CHECK (status IN ('proposed', 'no_grade')),
  reason             text CHECK (reason IS NULL OR length(reason) <= 300),
  total              numeric(6,2),
  scale              numeric(5,2) NOT NULL,
  confidence         numeric(4,3) CHECK (confidence IS NULL OR confidence BETWEEN 0 AND 1),
  flags              jsonb NOT NULL DEFAULT '[]'::jsonb,
  step_marks         jsonb NOT NULL DEFAULT '[]'::jsonb,
  model              text NOT NULL,
  prompt_version     text NOT NULL,
  attempts           smallint NOT NULL CHECK (attempts BETWEEN 0 AND 2),
  tokens             int NOT NULL DEFAULT 0 CHECK (tokens >= 0),
  academic_year      text NOT NULL CHECK (academic_year ~ '^[0-9]{4}-[0-9]{4}$'),
  created_at         timestamptz NOT NULL DEFAULT now(),
  UNIQUE (org_id, id),
  UNIQUE (org_id, version_id, ocr_version),
  CHECK ((status = 'proposed') = (total IS NOT NULL)),
  CHECK (status = 'proposed' OR reason IS NOT NULL),
  FOREIGN KEY (org_id, submission_id) REFERENCES submission (org_id, id) ON DELETE CASCADE
);
CREATE INDEX ai_grade_proposal_sub_idx ON ai_grade_proposal (org_id, submission_id, created_at DESC);
CREATE INDEX ai_grade_proposal_student_idx ON ai_grade_proposal (org_id, student_id);
CREATE INDEX ai_grade_proposal_year_idx ON ai_grade_proposal (academic_year);
COMMENT ON TABLE ai_grade_proposal IS 'GradingProposal (US-S5E6-01, BR-S5E6-04): phiên bản đề, rubric, bài nộp, bản đọc chữ, model, prompt, token; bất biến. no_grade = "AI không chấm" kèm lý do. Không tên học sinh.';

CREATE TABLE criterion_assessment (
  id             uuid PRIMARY KEY,
  org_id         uuid NOT NULL,
  proposal_id    uuid NOT NULL,
  ord            smallint NOT NULL CHECK (ord BETWEEN 1 AND 10),
  name           text NOT NULL CHECK (length(name) BETWEEN 1 AND 60),
  max_points     numeric(5,2) NOT NULL CHECK (max_points > 0),
  ai_points      numeric(5,2) NOT NULL CHECK (ai_points >= 0),
  points         numeric(5,2) NOT NULL CHECK (points >= 0 AND points <= max_points AND points * 4 = floor(points * 4)),
  reason         text NOT NULL CHECK (length(reason) <= 300),
  region         jsonb,
  confidence     numeric(4,3) NOT NULL CHECK (confidence BETWEEN 0 AND 1),
  error_label    text CHECK (error_label IS NULL OR error_label IN ('sai-dinh-li', 'sai-tinh-toan', 'sai-hinh-dung', 'nham-dieu-kien')),
  adjustments    jsonb NOT NULL DEFAULT '[]'::jsonb,
  flags          jsonb NOT NULL DEFAULT '[]'::jsonb,
  academic_year  text NOT NULL CHECK (academic_year ~ '^[0-9]{4}-[0-9]{4}$'),
  UNIQUE (org_id, proposal_id, ord),
  FOREIGN KEY (org_id, proposal_id) REFERENCES ai_grade_proposal (org_id, id) ON DELETE CASCADE
);
CREATE INDEX criterion_assessment_year_idx ON criterion_assessment (academic_year);
COMMENT ON TABLE criterion_assessment IS 'CriterionAssessment (US-S5E6-01, BR-S5E6-01…03): điểm AI gốc, điểm sau luật chặn KPI §2 (adjustments ghi "luật KPI §2.n"), lý do, vùng ảnh, tin cậy.';

CREATE TABLE engine_signal (
  id              uuid PRIMARY KEY,
  org_id          uuid NOT NULL,
  proposal_id     uuid NOT NULL,
  submission_id   uuid NOT NULL,
  status          text NOT NULL CHECK (status IN ('match', 'mismatch', 'not_found', 'no_engine')),
  student_answer  text CHECK (student_answer IS NULL OR length(student_answer) <= 200),
  engine_answer   text CHECK (engine_answer IS NULL OR length(engine_answer) <= 200),
  rule            text CHECK (rule IS NULL OR rule IN ('KS-1', 'KS-2')),
  disagree        boolean NOT NULL DEFAULT false,
  academic_year   text NOT NULL CHECK (academic_year ~ '^[0-9]{4}-[0-9]{4}$'),
  created_at      timestamptz NOT NULL DEFAULT now(),
  UNIQUE (org_id, proposal_id),
  FOREIGN KEY (org_id, proposal_id) REFERENCES ai_grade_proposal (org_id, id) ON DELETE CASCADE
);
CREATE INDEX engine_signal_sub_idx ON engine_signal (org_id, submission_id);
CREATE INDEX engine_signal_year_idx ON engine_signal (academic_year);
COMMENT ON TABLE engine_signal IS 'Tín hiệu engine (US-S5E6-03, BR-S5E6-08, 09): đáp số cuối trong bản đọc chữ so với đáp số engine bằng KS-1/KS-2; disagree = cờ "AI và engine lệch". Không gọi AI.';

GRANT SELECT, INSERT, DELETE ON ai_grade_proposal, criterion_assessment, engine_signal TO geo_app;
