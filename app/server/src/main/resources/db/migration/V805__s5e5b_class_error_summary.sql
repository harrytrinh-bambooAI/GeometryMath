-- T-261128-64 (S5-E5b, US-S5E5-02/03): tổng hợp lỗi sai của lớp (COP-02) sau bài có chấm.
-- Mỗi lượt tổng hợp có gọi (hoặc không gọi) AI ghi một dòng: băm đầu vào (đầu vào không đổi thì dùng lại, không gọi lại),
-- nhận định ĐÃ LỌC (không tên, không phần trăm), thứ tự "Bài k" → bài nộp (bấm căn cứ mở đúng bài), kết quả kiểm máy chủ
-- ("loại 1 nhận định"; ai_call_log không có cột kết quả kiểm). Gợi ý "Nên nhắc lại trên lớp" đọc nhận định từ đây (không gọi AI).
-- Không lưu tên học sinh hay nội dung gửi nhà cung cấp.

CREATE TABLE class_error_summary (
  id            uuid PRIMARY KEY,
  org_id        uuid NOT NULL,
  assignment_id uuid NOT NULL,
  teacher_id    uuid NOT NULL,
  input_hash    text NOT NULL CHECK (input_hash ~ '^[0-9a-f]{64}$'),
  mode          text NOT NULL CHECK (mode IN ('insight', 'ai_off', 'failed')),
  insights      jsonb NOT NULL DEFAULT '[]'::jsonb,
  works         jsonb NOT NULL DEFAULT '[]'::jsonb,
  check_note    text NOT NULL DEFAULT '' CHECK (length(check_note) <= 120),
  removed       smallint NOT NULL DEFAULT 0 CHECK (removed >= 0),
  created_at    timestamptz NOT NULL DEFAULT now(),
  FOREIGN KEY (org_id, assignment_id) REFERENCES graded_assignment (org_id, id) ON DELETE CASCADE
);
CREATE INDEX class_error_summary_assignment_idx ON class_error_summary (org_id, assignment_id, created_at DESC);
COMMENT ON TABLE class_error_summary IS 'US-S5E5-02: lượt tổng hợp lỗi của lớp (nhận định đã lọc, thứ tự bài làm căn cứ, kết quả kiểm máy chủ).';
