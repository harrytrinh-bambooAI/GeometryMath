-- T-261128-62 · S5-E6b (US-S5E6-04, 05): hành động của giáo viên trên từng tiêu chí có đề xuất AI và cặp so sánh chạy
-- song song. Chủ bảng: module aigrade (PLAN_code-s5 §2). Sau cờ geo.s5.ai-grading.enabled (bảng luôn tạo).
--   ai_review_action  CriterionAssessment phần giáo viên (BR-S5E6-12): mỗi lần Duyệt / sửa điểm của bài có đề xuất ghi
--                     một hàng mỗi tiêu chí: nhận / sửa / bỏ, điểm AI, điểm giáo viên. Chỉ thêm. Dùng cho "tỉ lệ sửa".
--   ai_shadow_pair    cặp (điểm AI, điểm giáo viên lần duyệt ĐẦU) của bài chạy song song hoặc bài lấy mẫu sau khi bật
--                     AI đề xuất (BR-S5E6-14, QS5-24). Sửa điểm sau đó không đổi cặp. ai_points điền MỘT lần khi AI chấm
--                     xong sau lúc giáo viên duyệt. student_id NULL sau khi xoá tài khoản (ẩn danh cho số liệu lớp).

CREATE TABLE ai_review_action (
  id             uuid PRIMARY KEY,
  org_id         uuid NOT NULL,
  submission_id  uuid NOT NULL,
  proposal_id    uuid NOT NULL,
  student_id     uuid,
  grade_seq      int  NOT NULL CHECK (grade_seq >= 1),
  ord            smallint NOT NULL CHECK (ord BETWEEN 1 AND 10),
  name           text NOT NULL CHECK (length(name) BETWEEN 1 AND 60),
  max_points     numeric(5,2) NOT NULL CHECK (max_points > 0),
  action         text NOT NULL CHECK (action IN ('accept', 'edit', 'drop')),
  ai_points      numeric(5,2) NOT NULL CHECK (ai_points >= 0),
  teacher_points numeric(5,2) NOT NULL CHECK (teacher_points >= 0),
  academic_year  text NOT NULL CHECK (academic_year ~ '^[0-9]{4}-[0-9]{4}$'),
  created_at     timestamptz NOT NULL DEFAULT now(),
  UNIQUE (org_id, submission_id, grade_seq, ord),
  FOREIGN KEY (org_id, proposal_id) REFERENCES ai_grade_proposal (org_id, id) ON DELETE CASCADE
);
CREATE INDEX ai_review_action_student_idx ON ai_review_action (org_id, student_id);
CREATE INDEX ai_review_action_year_idx ON ai_review_action (academic_year);
COMMENT ON TABLE ai_review_action IS 'CriterionAssessment phần giáo viên (US-S5E6-04, BR-S5E6-12): nhận/sửa/bỏ từng tiêu chí, điểm AI, điểm giáo viên; chỉ thêm. Không tên học sinh.';

CREATE TABLE ai_shadow_pair (
  id             uuid PRIMARY KEY,
  org_id         uuid NOT NULL,
  submission_id  uuid NOT NULL,
  assignment_id  uuid NOT NULL,
  class_id       uuid NOT NULL,
  student_id     uuid,
  sampled        boolean NOT NULL DEFAULT false,
  ord            smallint NOT NULL CHECK (ord BETWEEN 1 AND 10),
  name           text NOT NULL CHECK (length(name) BETWEEN 1 AND 60),
  max_points     numeric(5,2) NOT NULL CHECK (max_points > 0),
  teacher_points numeric(5,2) NOT NULL CHECK (teacher_points >= 0),
  ai_points      numeric(5,2) CHECK (ai_points IS NULL OR ai_points >= 0),
  proposal_id    uuid,
  academic_year  text NOT NULL CHECK (academic_year ~ '^[0-9]{4}-[0-9]{4}$'),
  created_at     timestamptz NOT NULL DEFAULT now(),
  UNIQUE (org_id, submission_id, ord),
  CHECK ((ai_points IS NULL) = (proposal_id IS NULL))
);
CREATE INDEX ai_shadow_pair_class_idx ON ai_shadow_pair (org_id, class_id, created_at);
CREATE INDEX ai_shadow_pair_student_idx ON ai_shadow_pair (org_id, student_id);
CREATE INDEX ai_shadow_pair_year_idx ON ai_shadow_pair (academic_year);
COMMENT ON TABLE ai_shadow_pair IS 'Cặp so sánh chạy song song (US-S5E6-05, BR-S5E6-14; QS5-24 lấy mẫu): điểm AI và điểm giáo viên lần duyệt đầu theo tiêu chí; không đổi khi sửa điểm. Không tên học sinh.';

GRANT SELECT, INSERT, DELETE ON ai_review_action TO geo_app;
GRANT SELECT, INSERT, DELETE ON ai_shadow_pair TO geo_app;
GRANT UPDATE (ai_points, proposal_id, student_id) ON ai_shadow_pair TO geo_app;
GRANT UPDATE (student_id) ON ai_review_action TO geo_app;
