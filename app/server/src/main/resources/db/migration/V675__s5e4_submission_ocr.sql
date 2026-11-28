-- T-261128-59 · S5-E4b: nộp bài viết tay nhiều trang (US-S5E4-03), gửi lại lũy đẳng / nộp lại / nộp muộn (US-S5E4-09),
-- bản đọc chữ phái sinh có phiên bản (US-S5E4-04). Sau cờ geo.s5.grading.enabled (bảng luôn tạo; API 404 khi tắt).
--   submission_upload        lượt tải một trang theo đoạn (thân yêu cầu máy chủ ≤ 1 MB): giữ tạm tới khi nộp.
--   submission               bài nộp của một học sinh cho một bài có chấm (mã BN-nn theo bài); trỏ phiên bản chấm.
--   submission_version       mỗi lần nộp (hoặc nộp lại trước hạn) một phiên bản; khóa lũy đẳng sinh trên máy
--                            (BR-S5E4-09); bản cũ giữ nguyên (BR-S5E4-10). Ảnh trang lưu ở stored_image với
--                            submission_id = id phiên bản.
--   ocr_artifact             bản đọc chữ phái sinh có phiên bản (BR-S5E4-13): v1 do task grade_ocr, v2+ do giáo viên sửa.
--   submission_privacy_ack   cờ đã đọc thông báo riêng tư S5 kèm phiên bản văn bản (BR-S5E4-11, QS5-01).

-- HEIC: ảnh gốc lưu đúng tệp HEIC; bản phái sinh JPEG (kind = 'derived') dùng cho đọc chữ và hiển thị (BR-S5E4-08).
ALTER TABLE stored_image DROP CONSTRAINT stored_image_content_type_check;
ALTER TABLE stored_image ADD CONSTRAINT stored_image_content_type_check
  CHECK (content_type IN ('image/jpeg', 'image/png', 'image/heic'));

CREATE TABLE submission_upload (
  id            uuid PRIMARY KEY,
  org_id        uuid NOT NULL,
  student_id    uuid NOT NULL,
  content_type  text NOT NULL CHECK (content_type IN ('image/jpeg', 'image/png', 'image/heic')),
  size_bytes    int  NOT NULL CHECK (size_bytes BETWEEN 1 AND 8388608),
  data          bytea,
  created_at    timestamptz NOT NULL DEFAULT now(),
  FOREIGN KEY (org_id, student_id) REFERENCES app_user (org_id, id) ON DELETE CASCADE
);
CREATE INDEX submission_upload_student_idx ON submission_upload (org_id, student_id, created_at);
COMMENT ON TABLE submission_upload IS 'Trang ảnh đang tải theo đoạn (US-S5E4-03); xoá khi nộp. Trần 8 MB/trang (QS5-19).';

CREATE TABLE submission (
  id              uuid PRIMARY KEY,
  org_id          uuid NOT NULL,
  assignment_id   uuid NOT NULL,
  student_id      uuid NOT NULL,
  class_id        uuid NOT NULL,
  code            text NOT NULL CHECK (code ~ '^BN-[0-9]{2,4}$'),
  current_version int  NOT NULL DEFAULT 1 CHECK (current_version >= 1),
  academic_year   text NOT NULL CHECK (academic_year ~ '^[0-9]{4}-[0-9]{4}$'),
  created_at      timestamptz NOT NULL DEFAULT now(),
  updated_at      timestamptz NOT NULL DEFAULT now(),
  UNIQUE (org_id, id),
  UNIQUE (org_id, assignment_id, student_id),
  UNIQUE (org_id, assignment_id, code),
  FOREIGN KEY (org_id, assignment_id) REFERENCES graded_assignment (org_id, id) ON DELETE CASCADE
);
CREATE INDEX submission_student_idx ON submission (org_id, student_id);
CREATE INDEX submission_year_idx ON submission (academic_year);
COMMENT ON TABLE submission IS 'Bài nộp (US-S5E4-03, 09). Một bài mỗi học sinh mỗi bài có chấm; phiên bản chấm = current_version.';

CREATE TABLE submission_version (
  id              uuid PRIMARY KEY,
  org_id          uuid NOT NULL,
  submission_id   uuid NOT NULL,
  student_id      uuid NOT NULL,
  version         int  NOT NULL CHECK (version >= 1),
  idempotency_key text NOT NULL CHECK (length(idempotency_key) BETWEEN 1 AND 80),
  pages           smallint NOT NULL CHECK (pages BETWEEN 1 AND 10),
  submitted_at    timestamptz NOT NULL,
  late_seconds    int CHECK (late_seconds IS NULL OR late_seconds > 0),
  academic_year   text NOT NULL CHECK (academic_year ~ '^[0-9]{4}-[0-9]{4}$'),
  UNIQUE (org_id, submission_id, version),
  UNIQUE (org_id, student_id, idempotency_key),
  FOREIGN KEY (org_id, submission_id) REFERENCES submission (org_id, id) ON DELETE CASCADE
);
COMMENT ON TABLE submission_version IS 'Phiên bản bài nộp (BR-S5E4-09, 10): khóa lũy đẳng của máy; nộp muộn ghi số giây muộn theo giờ máy chủ.';

CREATE TABLE ocr_artifact (
  id             uuid PRIMARY KEY,
  org_id         uuid NOT NULL,
  submission_id  uuid NOT NULL,
  version_id     uuid NOT NULL,
  student_id     uuid NOT NULL,
  version        int  NOT NULL CHECK (version >= 1),
  status         text NOT NULL CHECK (status IN ('done', 'failed')),
  pages          jsonb NOT NULL DEFAULT '[]'::jsonb,
  edited_by      uuid,
  academic_year  text NOT NULL CHECK (academic_year ~ '^[0-9]{4}-[0-9]{4}$'),
  created_at     timestamptz NOT NULL DEFAULT now(),
  UNIQUE (org_id, version_id, version),
  FOREIGN KEY (org_id, submission_id) REFERENCES submission (org_id, id) ON DELETE CASCADE
);
CREATE INDEX ocr_artifact_student_idx ON ocr_artifact (org_id, student_id);
CREATE INDEX ocr_artifact_year_idx ON ocr_artifact (academic_year);
COMMENT ON TABLE ocr_artifact IS 'Bản đọc chữ phái sinh (US-S5E4-04, BR-S5E4-12…14): chỉ thêm phiên bản; ảnh gốc không đổi. Không tên học sinh.';

CREATE TABLE submission_privacy_ack (
  org_id         uuid NOT NULL,
  student_id     uuid NOT NULL,
  notice_version text NOT NULL CHECK (notice_version ~ '^privacy-s5-[0-9]+\.[0-9]+$'),
  acked_at       timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (org_id, student_id, notice_version),
  FOREIGN KEY (org_id, student_id) REFERENCES app_user (org_id, id) ON DELETE CASCADE
);
COMMENT ON TABLE submission_privacy_ack IS 'Học sinh đã đọc thông báo riêng tư S5 (MSG-S5-36) theo phiên bản văn bản (BR-S5E4-11).';

GRANT SELECT, INSERT, UPDATE, DELETE ON submission_upload, submission, submission_version, ocr_artifact TO geo_app;
GRANT SELECT, INSERT, DELETE ON submission_privacy_ack TO geo_app;
