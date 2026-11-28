-- T-261128-63 · S5-E2 (US-S5E2-06) trợ lý soạn đề nháp từ tài liệu Đang dùng (luồng D) và S5-E3 (US-S5E3-06) phản hồi
-- của giáo viên trên lượt hỏi đáp ("chặn sai" / "lẽ ra phải chặn"). Chủ bảng: exam (exam_draft_*), knowledge (ai_qa_feedback*).

-- Một lần xin soạn: nguồn là tài liệu Đang dùng của chính giáo viên (BR-S5E2-14), ghi mã + phiên bản lúc soạn.
CREATE TABLE exam_draft_batch (
  id          uuid PRIMARY KEY,
  org_id      uuid NOT NULL,
  teacher_id  uuid NOT NULL,
  class_id    uuid NOT NULL,
  doc_id      uuid NOT NULL,
  doc_code    text NOT NULL CHECK (doc_code ~ '^TL-[0-9]{2,4}$'),
  doc_version int  NOT NULL CHECK (doc_version >= 1),
  requested   int  NOT NULL CHECK (requested BETWEEN 1 AND 10),
  created_at  timestamptz NOT NULL DEFAULT now(),
  UNIQUE (org_id, id),
  FOREIGN KEY (org_id, teacher_id) REFERENCES app_user (org_id, id) ON DELETE CASCADE
);
CREATE INDEX exam_draft_batch_teacher_idx ON exam_draft_batch (org_id, teacher_id, created_at);

-- Một đề nháp: nhãn người "AI soạn" (status draft) tới khi giáo viên Duyệt (approved = ngân hàng "Của tôi", BR-S5E2-16);
-- nhãn máy chỉ do engine (verified | needs_review, BR-S5E2-15); đáp số lưu là đáp số ENGINE, đáp số AI giữ để đối chiếu.
CREATE TABLE exam_draft_item (
  id             uuid PRIMARY KEY,
  org_id         uuid NOT NULL,
  batch_id       uuid NOT NULL,
  teacher_id     uuid NOT NULL,
  ord            int  NOT NULL CHECK (ord BETWEEN 1 AND 10),
  statement      text NOT NULL CHECK (length(statement) BETWEEN 1 AND 2000),
  source_section text CHECK (source_section IS NULL OR length(source_section) <= 200),
  scene          jsonb,
  ai_answer      text CHECK (ai_answer IS NULL OR length(ai_answer) <= 200),
  engine_answer  text CHECK (engine_answer IS NULL OR length(engine_answer) <= 200),
  machine_label  text NOT NULL CHECK (machine_label IN ('verified', 'needs_review')),
  review_reason  text CHECK (review_reason IS NULL OR length(review_reason) <= 200),
  status         text NOT NULL DEFAULT 'draft' CHECK (status IN ('draft', 'approved')),
  approved_at    timestamptz,
  created_at     timestamptz NOT NULL DEFAULT now(),
  UNIQUE (org_id, id),
  UNIQUE (org_id, batch_id, ord),
  CHECK ((status = 'approved') = (approved_at IS NOT NULL)),
  CHECK (status = 'draft' OR (machine_label = 'verified' AND engine_answer IS NOT NULL)),
  FOREIGN KEY (org_id, batch_id) REFERENCES exam_draft_batch (org_id, id) ON DELETE CASCADE,
  FOREIGN KEY (org_id, teacher_id) REFERENCES app_user (org_id, id) ON DELETE CASCADE
);
CREATE INDEX exam_draft_item_bank_idx ON exam_draft_item (org_id, teacher_id, status, approved_at);

-- Đánh dấu của giáo viên trên một lượt hỏi đáp: mỗi lượt một đánh dấu, sửa được (BR-S5E3-20); không đổi câu trả lời đã gửi.
CREATE TABLE ai_qa_feedback (
  org_id         uuid NOT NULL,
  interaction_id uuid NOT NULL,
  teacher_id     uuid NOT NULL,
  type           text NOT NULL CHECK (type IN ('false_block', 'should_block')),
  note           text CHECK (note IS NULL OR length(note) <= 200),
  rule_code      text NOT NULL DEFAULT '',
  created_at     timestamptz NOT NULL DEFAULT now(),
  updated_at     timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (org_id, interaction_id),
  FOREIGN KEY (org_id, interaction_id) REFERENCES ai_interaction (org_id, id) ON DELETE CASCADE,
  FOREIGN KEY (org_id, teacher_id) REFERENCES app_user (org_id, id) ON DELETE CASCADE
);

-- Tổng hợp cho người vận hành theo luật × loại đánh dấu (đếm đánh dấu hiện hành; sửa loại thì chuyển đếm).
CREATE TABLE ai_qa_feedback_counter (
  org_id    uuid NOT NULL,
  rule_code text NOT NULL DEFAULT '',
  type      text NOT NULL CHECK (type IN ('false_block', 'should_block')),
  n         int  NOT NULL DEFAULT 0 CHECK (n >= 0),
  PRIMARY KEY (org_id, rule_code, type)
);

GRANT SELECT, INSERT, UPDATE, DELETE ON exam_draft_batch, exam_draft_item TO geo_app;
GRANT SELECT, INSERT, UPDATE ON ai_qa_feedback, ai_qa_feedback_counter TO geo_app;
