-- T-261128-68 · S5-FIX W7 (Dev-S5E6-12): giờ giáo viên MỞ màn chấm lần đầu cho mỗi bài nộp, để KPI (g) "trung vị thời
-- gian duyệt một bài (từ mở màn chấm tới Duyệt)" (BR-S5E6-15) có số liệu thật. Ghi ở GET màn chấm
-- (/api/v1/teacher/grading/submissions/{id}, dùng cho cả /gv/bai-co-cham và /gv/cham-ai); chỉ lần mở đầu (giữ hàng cũ).
-- Không nội dung bài làm. Xoá theo bài nộp (job năm học, xoá tài khoản) qua khoá ngoại.
CREATE TABLE grade_review_open (
  org_id        uuid NOT NULL,
  submission_id uuid NOT NULL,
  teacher_id    uuid NOT NULL,
  opened_at     timestamptz NOT NULL DEFAULT clock_timestamp(),
  PRIMARY KEY (org_id, submission_id),
  FOREIGN KEY (org_id, submission_id) REFERENCES submission (org_id, id) ON DELETE CASCADE
);
COMMENT ON TABLE grade_review_open IS 'Lần đầu giáo viên mở màn chấm một bài nộp (KPI (g), BR-S5E6-15; T-261128-68).';
GRANT SELECT, INSERT ON grade_review_open TO geo_app;
