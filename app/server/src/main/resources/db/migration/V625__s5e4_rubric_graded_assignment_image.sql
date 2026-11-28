-- T-261128-57 · S5-E4 (sau cờ geo.s5.grading.enabled): US-S5E4-01 rubric có phiên bản, US-S5E4-02 bài có chấm,
-- US-S5E4-08 kho ảnh bài làm. Module assessment (PLAN_code-s5 §2), gói rubric/, assign/, storage/.
--   rubric             định danh rubric của một giáo viên (mã RB-nn), trỏ phiên bản hiện hành.
--   rubric_version     phiên bản (tên, thang, hướng dẫn chấm theo luật chặn KPI §2). Phiên bản đã gắn với một bài đã giao là
--                      BẤT BIẾN: trigger từ chối UPDATE/DELETE của phiên bản và INSERT/UPDATE/DELETE tiêu chí (BR-S5E4-03).
--   rubric_criterion   1–10 tiêu chí; điểm tối đa bội 0,25; cờ "phụ thuộc hướng" (BR-S5E4-01, QS5-15).
--   graded_assignment  bài có chấm (mã BC-nn): đề kho HOẶC bộ đề đã duyệt + mã đề, rubric phiên bản cụ thể, thang, hạn nộp,
--                      nộp muộn, chế độ chấm (QS5-16), ReleasePolicy (khi Trả bài | giờ cố định) (BR-S5E4-04…06).
--   stored_image       siêu dữ liệu ảnh bài làm trong kho đối tượng mã hoá (AES-256-GCM): không có đường dẫn công khai; băm
--                      tính SAU khi gỡ EXIF/XMP; năm học để job xoá 31/07 (BR-S5E4-25…28; QS5-01, QS5-14, QS5-18).
--   assessment_audit   AuditEvent của module (xoá ảnh theo tài khoản / năm học: chỉ số lượng, không nội dung).
-- submission / submission_page (T-261128-59) chưa có: stored_image giữ submission_id, student_id, class_id để kiểm quan hệ.

CREATE TABLE rubric (
  id              uuid PRIMARY KEY,
  org_id          uuid NOT NULL,
  teacher_id      uuid NOT NULL,
  code            text NOT NULL CHECK (code ~ '^RB-[0-9]{2,4}$'),
  current_version int  NOT NULL DEFAULT 1 CHECK (current_version >= 1),
  created_at      timestamptz NOT NULL DEFAULT now(),
  updated_at      timestamptz NOT NULL DEFAULT now(),
  UNIQUE (org_id, id),
  UNIQUE (org_id, teacher_id, code),
  FOREIGN KEY (org_id, teacher_id) REFERENCES app_user (org_id, id) ON DELETE CASCADE
);
COMMENT ON TABLE rubric IS 'Rubric của giáo viên (US-S5E4-01). Nội dung ở rubric_version; current_version là bản dùng khi giao bài mới.';

CREATE TABLE rubric_version (
  id         uuid PRIMARY KEY,
  org_id     uuid NOT NULL,
  rubric_id  uuid NOT NULL,
  version    int  NOT NULL CHECK (version >= 1),
  name       text NOT NULL CHECK (length(name) BETWEEN 1 AND 120),
  template   text CHECK (template IN ('tinh-toan', 'chung-minh')),
  scale      numeric(5,2) NOT NULL DEFAULT 10 CHECK (scale > 0 AND scale * 4 = floor(scale * 4)),
  guide      jsonb NOT NULL DEFAULT '[]',
  created_by uuid NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now(),
  UNIQUE (org_id, id),
  UNIQUE (org_id, rubric_id, version),
  FOREIGN KEY (org_id, rubric_id) REFERENCES rubric (org_id, id) ON DELETE CASCADE
);
COMMENT ON TABLE rubric_version IS 'Phiên bản rubric. Đã gắn bài có chấm thì bất biến (trigger tg_rubric_version_frozen, BR-S5E4-03).';

CREATE TABLE rubric_criterion (
  id                  uuid PRIMARY KEY,
  org_id              uuid NOT NULL,
  rubric_version_id   uuid NOT NULL,
  ord                 smallint NOT NULL CHECK (ord BETWEEN 1 AND 10),
  name                text NOT NULL CHECK (length(name) BETWEEN 1 AND 60),
  description         text NOT NULL DEFAULT '' CHECK (length(description) <= 500),
  max_points          numeric(5,2) NOT NULL CHECK (max_points > 0 AND max_points * 4 = floor(max_points * 4)),
  direction_dependent boolean NOT NULL DEFAULT false,
  UNIQUE (org_id, rubric_version_id, ord),
  FOREIGN KEY (org_id, rubric_version_id) REFERENCES rubric_version (org_id, id) ON DELETE CASCADE
);
COMMENT ON TABLE rubric_criterion IS 'Tiêu chí rubric (BR-S5E4-01): 1–10 tiêu chí, điểm bội 0,25. Bất biến khi phiên bản đã dùng.';

CREATE TABLE graded_assignment (
  id                 uuid PRIMARY KEY,
  org_id             uuid NOT NULL,
  teacher_id         uuid NOT NULL,
  class_id           uuid NOT NULL,
  code               text NOT NULL CHECK (code ~ '^BC-[0-9]{2,4}$'),
  title              text NOT NULL CHECK (length(title) BETWEEN 1 AND 120),
  problem_version_id uuid,
  exam_set_id        uuid,
  exam_code          text CHECK (exam_code ~ '^[0-9]{3}$'),
  rubric_version_id  uuid NOT NULL,
  scale              numeric(5,2) NOT NULL CHECK (scale > 0),
  due_at             timestamptz NOT NULL,
  allow_late         boolean NOT NULL DEFAULT true,
  grading_mode       text NOT NULL DEFAULT 'manual' CHECK (grading_mode IN ('manual', 'parallel', 'ai_proposal')),
  release_mode       text NOT NULL DEFAULT 'on_return' CHECK (release_mode IN ('on_return', 'at_time')),
  release_at         timestamptz,
  status             text NOT NULL DEFAULT 'assigned' CHECK (status IN ('assigned', 'returned')),
  created_at         timestamptz NOT NULL DEFAULT now(),
  updated_at         timestamptz NOT NULL DEFAULT now(),
  UNIQUE (org_id, id),
  UNIQUE (org_id, teacher_id, code),
  CHECK ((problem_version_id IS NOT NULL) <> (exam_set_id IS NOT NULL)),
  CHECK ((release_mode = 'at_time') = (release_at IS NOT NULL)),
  FOREIGN KEY (org_id, teacher_id) REFERENCES app_user (org_id, id) ON DELETE CASCADE,
  FOREIGN KEY (org_id, class_id) REFERENCES class (org_id, id) ON DELETE CASCADE,
  FOREIGN KEY (org_id, rubric_version_id) REFERENCES rubric_version (org_id, id)
);
CREATE INDEX graded_assignment_class_idx ON graded_assignment (org_id, class_id, due_at DESC);
COMMENT ON TABLE graded_assignment IS 'Bài có chấm (US-S5E4-02, BR-S5E4-04…06). Lời giải mở cùng lúc công bố điểm (PO-22).';

-- Bất biến phiên bản đã dùng (BR-S5E4-03; cách làm như NFR-GEO-025): kể cả câu lệnh thẳng của vai ứng dụng.
CREATE FUNCTION tg_rubric_version_frozen() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE v_org uuid; v_id uuid;
BEGIN
  IF TG_TABLE_NAME = 'rubric_criterion' THEN
    v_org := coalesce(OLD.org_id, NEW.org_id);
    v_id := CASE WHEN TG_OP = 'INSERT' THEN NEW.rubric_version_id ELSE OLD.rubric_version_id END;
  ELSE
    v_org := OLD.org_id; v_id := OLD.id;
  END IF;
  IF EXISTS (SELECT 1 FROM graded_assignment g WHERE g.org_id = v_org AND g.rubric_version_id = v_id) THEN
    RAISE EXCEPTION 'phiên bản rubric % đã dùng cho bài đã giao là bất biến (BR-S5E4-03): tạo phiên bản mới', v_id
      USING ERRCODE = 'check_violation';
  END IF;
  RETURN CASE WHEN TG_OP = 'DELETE' THEN OLD ELSE NEW END;
END $$;
CREATE TRIGGER rubric_criterion_frozen BEFORE INSERT OR UPDATE OR DELETE ON rubric_criterion
  FOR EACH ROW EXECUTE FUNCTION tg_rubric_version_frozen();
CREATE TRIGGER rubric_version_frozen BEFORE UPDATE OR DELETE ON rubric_version
  FOR EACH ROW EXECUTE FUNCTION tg_rubric_version_frozen();

CREATE TABLE stored_image (
  id            uuid PRIMARY KEY,
  org_id        uuid NOT NULL,
  assignment_id uuid NOT NULL,
  submission_id uuid NOT NULL,
  page_no       smallint NOT NULL CHECK (page_no BETWEEN 1 AND 20),
  kind          text NOT NULL DEFAULT 'original' CHECK (kind IN ('original', 'derived')),
  student_id    uuid NOT NULL,
  class_id      uuid NOT NULL,
  academic_year   text NOT NULL CHECK (academic_year ~ '^[0-9]{4}-[0-9]{4}$'),
  content_type  text NOT NULL CHECK (content_type IN ('image/jpeg', 'image/png')),
  sha256        text NOT NULL CHECK (sha256 ~ '^[0-9a-f]{64}$'),
  bytes         int  NOT NULL CHECK (bytes > 0),
  object_key    text NOT NULL CHECK (object_key ~ '^[0-9a-f-]{36}/[0-9a-f-]{36}$'),
  created_at    timestamptz NOT NULL DEFAULT now(),
  UNIQUE (org_id, submission_id, page_no, kind),
  FOREIGN KEY (org_id, assignment_id) REFERENCES graded_assignment (org_id, id) ON DELETE CASCADE
);
CREATE INDEX stored_image_student_idx ON stored_image (org_id, student_id);
CREATE INDEX stored_image_year_idx ON stored_image (academic_year);
COMMENT ON TABLE stored_image IS 'Ảnh bài làm trong kho mã hoá (US-S5E4-08). Không đường dẫn công khai; chỉ URL ký 5 phút. Không tên học sinh.';

CREATE TABLE assessment_audit (
  id        uuid PRIMARY KEY,
  org_id    uuid NOT NULL,
  actor_id  uuid,
  action    text NOT NULL CHECK (action IN ('image_purge_account', 'image_purge_year')),
  target_id uuid,
  detail    text NOT NULL DEFAULT '' CHECK (length(detail) <= 40),
  n         int  NOT NULL CHECK (n >= 0),
  at        timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX assessment_audit_org_idx ON assessment_audit (org_id, at DESC);
COMMENT ON TABLE assessment_audit IS 'AuditEvent module assessment (BR-S5E4-28): số lượng đã xoá, không nội dung. Chỉ thêm.';

GRANT SELECT, INSERT, UPDATE, DELETE ON rubric, rubric_version, rubric_criterion, graded_assignment, stored_image TO geo_app;
GRANT SELECT, INSERT ON assessment_audit TO geo_app;
