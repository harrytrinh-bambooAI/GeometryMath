-- T-261128-55 · S5-E5 US-S5E5-04 (công tắc AI theo lớp, tắt khẩn, hạn mức lượt AI) + US-S5E5-01 (nháp thông báo trợ lý soạn).
-- Bảng mới (module aigateway, gói quota/killswitch):
--   ai_class_switch   công tắc theo lớp ngoài AI phía học sinh (class.ai_enabled, S3-E5): AI chấm (mặc định TẮT), Trợ lý
--                     (mặc định BẬT) — QS5-26. Không có dòng = mặc định.
--   ai_switch_audit   AuditEvent mỗi lần đổi công tắc lớp / tắt khẩn (người thực hiện, trước, sau) — BR-S5E5-10, 16.
--   ai_kill_switch    công tắc tắt khẩn TOÀN HỆ THỐNG theo task (geo.ai.kill.<task>) — không theo trường (QS5-02).
--   ai_ops_grant      tài khoản có vai Vận hành (gán bằng seed / lệnh vận hành; không có API gán).
--   ai_usage_counter  bộ đếm lượt/token theo ngày giờ VN: phạm vi class | student | teacher, chỉ số
--                     qa | grade | compose | tokens | blocked | turn.<task> (BR-S5E5-12, 13).
--   ai_quota_warning  cảnh báo 80 % đã gửi (một lần mỗi lớp, mỗi ngày, mỗi loại).
--   ai_ocr_retry      số lần "Đọc lại" bản đọc chữ của một bài nộp (tối đa 3).
--   ai_pending_job    việc AI nền đang chờ theo lớp (chấm nền S5-E4/E6 đăng ký); tắt AI chấm huỷ việc đang chờ (BR-S5E5-11).
-- Bảng mới (module notify, gói draft):
--   notice_draft      nháp thông báo lớp do trợ lý soạn (CopilotDraft loại notice), nhãn "AI soạn" tới khi giáo viên Gửi.
-- notification (S3-E4): thêm loại 'class_notice' (thân ≤ 1 200 ký tự: nội dung ≤ 1 000 + dòng người gửi/nhãn trợ lý).

CREATE TABLE ai_class_switch (
  org_id      uuid NOT NULL,
  class_id    uuid NOT NULL,
  ai_grading  boolean NOT NULL DEFAULT false,
  assistant   boolean NOT NULL DEFAULT true,
  updated_by  uuid,
  updated_at  timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (org_id, class_id),
  FOREIGN KEY (org_id, class_id) REFERENCES class (org_id, id) ON DELETE CASCADE
);
COMMENT ON TABLE ai_class_switch IS 'Công tắc AI chấm / Trợ lý theo lớp (US-S5E5-04, QS5-26). Không có dòng = AI chấm tắt, Trợ lý bật.';

CREATE TABLE ai_switch_audit (
  id          uuid PRIMARY KEY,
  org_id      uuid NOT NULL,
  actor_id    uuid NOT NULL,
  actor_role  text NOT NULL CHECK (actor_role IN ('teacher', 'operator')),
  class_id    uuid,
  switch_name text NOT NULL CHECK (switch_name ~ '^[a-z_.]{2,40}$'),
  before_on   boolean NOT NULL,
  after_on    boolean NOT NULL,
  at          timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX ai_switch_audit_class_idx ON ai_switch_audit (org_id, class_id, at DESC);
COMMENT ON TABLE ai_switch_audit IS 'AuditEvent đổi công tắc AI (BR-S5E5-10, 16): chỉ thêm, không sửa, không xoá.';

CREATE TABLE ai_kill_switch (
  task       text NOT NULL CHECK (task ~ '^[a-z_]{2,20}$'),
  lane       text NOT NULL DEFAULT '' CHECK (length(lane) <= 64),  -- '' = toàn hệ thống; khác '' chỉ dev/test (làn kiểm thử song song)
  off        boolean NOT NULL DEFAULT false,
  updated_by uuid,
  updated_at timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (task, lane)
);
COMMENT ON TABLE ai_kill_switch IS 'Tắt khẩn toàn hệ thống theo task AI (BR-S5E5-16; QS5-02 không multi-tenant). lane chỉ dùng ở dev/test.';

CREATE TABLE ai_ops_grant (
  user_id    uuid PRIMARY KEY,
  granted_by text NOT NULL CHECK (length(granted_by) BETWEEN 1 AND 60),
  granted_at timestamptz NOT NULL DEFAULT now()
);
COMMENT ON TABLE ai_ops_grant IS 'Vai Vận hành (tắt khẩn AI). Gán bằng seed / lệnh vận hành; ứng dụng chỉ đọc.';

CREATE TABLE ai_usage_counter (
  org_id   uuid NOT NULL,
  day      date NOT NULL,
  scope    text NOT NULL CHECK (scope IN ('class', 'student', 'teacher')),
  scope_id uuid NOT NULL,
  metric   text NOT NULL CHECK (metric ~ '^[a-z_.]{2,30}$'),
  n        bigint NOT NULL DEFAULT 0 CHECK (n >= 0),
  PRIMARY KEY (org_id, scope, scope_id, metric, day)
);
COMMENT ON TABLE ai_usage_counter IS 'Bộ đếm lượt / token AI theo ngày giờ Việt Nam (BR-S5E5-12, 13). Không chứa tên học sinh.';

CREATE TABLE ai_quota_warning (
  org_id   uuid NOT NULL,
  class_id uuid NOT NULL,
  day      date NOT NULL,
  kind     text NOT NULL CHECK (kind IN ('qa', 'grade', 'tokens')),
  sent_at  timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (org_id, class_id, day, kind)
);

CREATE TABLE ai_ocr_retry (
  org_id        uuid NOT NULL,
  submission_id uuid NOT NULL,
  n             int  NOT NULL DEFAULT 0 CHECK (n BETWEEN 0 AND 3),
  PRIMARY KEY (org_id, submission_id)
);

CREATE TABLE ai_pending_job (
  id         uuid PRIMARY KEY,
  org_id     uuid NOT NULL,
  class_id   uuid NOT NULL,
  task       text NOT NULL CHECK (task IN ('grade', 'grade_ocr')),
  ref_id     uuid NOT NULL,
  status     text NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'cancelled', 'done')),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX ai_pending_job_class_idx ON ai_pending_job (org_id, class_id) WHERE status = 'pending';

CREATE TABLE notice_draft (
  id          uuid PRIMARY KEY,
  org_id      uuid NOT NULL,
  teacher_id  uuid NOT NULL,
  class_id    uuid NOT NULL,
  kind        text NOT NULL CHECK (kind IN ('due', 'test', 'returned', 'free')),
  ref_id      uuid,
  ai_text     text NOT NULL DEFAULT '',
  body        text NOT NULL DEFAULT '',
  unsure      jsonb NOT NULL DEFAULT '[]',
  status      text NOT NULL DEFAULT 'draft' CHECK (status IN ('draft', 'sent')),
  edited      boolean NOT NULL DEFAULT false,
  recipients  int,
  sent_at     timestamptz,
  created_at  timestamptz NOT NULL DEFAULT now(),
  updated_at  timestamptz NOT NULL DEFAULT now(),
  FOREIGN KEY (org_id, teacher_id) REFERENCES app_user (org_id, id) ON DELETE CASCADE,
  FOREIGN KEY (org_id, class_id) REFERENCES class (org_id, id) ON DELETE CASCADE
);
CREATE INDEX notice_draft_teacher_idx ON notice_draft (org_id, teacher_id, id DESC);
COMMENT ON TABLE notice_draft IS 'Nháp thông báo lớp do trợ lý soạn (US-S5E5-01). Chỉ gửi khi giáo viên bấm Gửi (BR-S5E5-02).';

ALTER TABLE notification DROP CONSTRAINT notification_kind_check;
ALTER TABLE notification ADD CONSTRAINT notification_kind_check
  CHECK (kind IN ('new_set', 'solution_open', 'release_changed', 'due_soon', 'class_notice', 'ai_quota'));
ALTER TABLE notification DROP CONSTRAINT notification_body_check;
ALTER TABLE notification ADD CONSTRAINT notification_body_check
  CHECK (length(body) BETWEEN 1 AND 240 OR (kind = 'class_notice' AND length(body) BETWEEN 1 AND 1200));

GRANT SELECT, INSERT, UPDATE ON ai_class_switch, ai_kill_switch, ai_usage_counter, ai_ocr_retry, ai_pending_job, notice_draft TO geo_app;
GRANT SELECT, INSERT ON ai_switch_audit, ai_quota_warning TO geo_app;
GRANT SELECT, INSERT ON ai_ops_grant TO geo_app;  -- INSERT chỉ cho móc kiểm thử dev/test cấp vai Vận hành
