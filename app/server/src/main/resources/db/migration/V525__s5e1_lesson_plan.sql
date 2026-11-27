-- T-261128-53 (S5-E1: US-S5E1-01…03, PLAN_code-s5 §2 module lessonplan, §4 khoảng V525–V544).
-- Bảng của trợ lý soạn giáo án. Dữ liệu danh mục (lp_*) nạp ở V526.

-- ---------------------------------------------------------------- danh mục dùng chung (không theo trường)
-- Bài theo SGK Toán 11 KNTT (Chương → Bài).
CREATE TABLE lp_ppct_lesson (
  lesson_no     int PRIMARY KEY,
  chapter_no    int  NOT NULL,
  chapter_title text NOT NULL,
  title         text NOT NULL
);

-- Tiết PPCT của bài: số tiết tự suy ("Tiết 1-2/3" → 2), kiến thức tích sẵn (focus) và đơn vị nền cấp thêm.
CREATE TABLE lp_ppct_period (
  id              text PRIMARY KEY,
  lesson_no       int  NOT NULL REFERENCES lp_ppct_lesson (lesson_no),
  ord             int  NOT NULL,
  label           text NOT NULL,
  periods         int  NOT NULL CHECK (periods BETWEEN 1 AND 5),
  focus           text[] NOT NULL,
  extra_knowledge text[] NOT NULL DEFAULT '{}'
);

-- Đơn vị kiến thức (kho kiến thức materials/lop-11): chỉ 'da-kiem' và 'nhap-tam' được cấp cho AI (BR-S5E1-05, QS5-03).
CREATE TABLE lp_knowledge_unit (
  code      text PRIMARY KEY,
  lesson_no int  NOT NULL,
  statement text NOT NULL,
  status    text NOT NULL CHECK (status IN ('da-kiem', 'nhap-tam', 'loai-bo'))
);
CREATE INDEX lp_knowledge_unit_lesson_idx ON lp_knowledge_unit (lesson_no);

-- Kho bài tập của bài (bo-mau + bai-tap): đáp số do kho điền, không bao giờ từ AI (BR-S5E1-07).
CREATE TABLE lp_exercise (
  code      text PRIMARY KEY,
  lesson_no int  NOT NULL,
  level     text NOT NULL CHECK (level IN ('NB', 'TH', 'VD', 'VDC')),
  statement text NOT NULL,
  answer    text,
  source    text NOT NULL
);
CREATE INDEX lp_exercise_lesson_idx ON lp_exercise (lesson_no);

-- ---------------------------------------------------------------- giáo án (theo trường, theo giáo viên)
-- Mã hiển thị GA-nnnn duy nhất toàn hệ thống; seed dùng GA-0001…0999, máy chủ cấp từ 1001.
CREATE SEQUENCE lesson_plan_code_seq START 1001;

CREATE TABLE lesson_plan (
  id              uuid PRIMARY KEY,
  org_id          uuid NOT NULL,
  teacher_id      uuid NOT NULL,
  code            text NOT NULL UNIQUE,
  period_id       text NOT NULL REFERENCES lp_ppct_period (id),
  request         jsonb NOT NULL,                 -- form B/C đã kiểm (BR-S5E1-01…03)
  status          text NOT NULL CHECK (status IN ('drafting', 'ready', 'error', 'question', 'reply', 'refused')),
  content         jsonb,                          -- giáo án đã chuẩn hoá khoá + đáp số kho; NULL khi chưa có bản hợp lệ
  reply           jsonb,                          -- {kind: question|reply, text, field?}
  check_log       jsonb NOT NULL DEFAULT '[]',    -- lỗi kiểm máy chủ của từng lần gọi (BR-S5E1-06)
  fix_keys        int  NOT NULL DEFAULT 0,        -- số khoá phải chuẩn hoá (prompt-final §4.2)
  ai_calls        int  NOT NULL DEFAULT 0,
  version         int  NOT NULL DEFAULT 0,
  human_label     text NOT NULL DEFAULT 'ai' CHECK (human_label IN ('ai', 'approved')),
  locked_sections text[] NOT NULL DEFAULT '{}',   -- mục đã khoá (T-54 dùng khi vá)
  created_at      timestamptz NOT NULL DEFAULT now(),
  updated_at      timestamptz NOT NULL DEFAULT now(),
  FOREIGN KEY (org_id, teacher_id) REFERENCES app_user (org_id, id) ON DELETE CASCADE
);
CREATE INDEX lesson_plan_teacher_idx ON lesson_plan (org_id, teacher_id, id DESC);

-- Ảnh chụp từng phiên bản nội dung (phiên bản 1 = bản soạn đầu; T-54 thêm phiên bản khi vá, duyệt).
CREATE TABLE lesson_plan_version (
  plan_id    uuid NOT NULL REFERENCES lesson_plan (id) ON DELETE CASCADE,
  org_id     uuid NOT NULL,
  version    int  NOT NULL,
  content    jsonb NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (plan_id, version)
);

-- Lượt AI soạn của giáo viên theo ngày giờ Việt Nam (BR-S5E1-08, QS5-04: 30 lượt soạn mới + vá).
-- counted = false: lượt hỏng sau gọi lại (US-S5E1-02-FC2) không tính.
CREATE TABLE lesson_plan_turn (
  id         uuid PRIMARY KEY,
  org_id     uuid NOT NULL,
  teacher_id uuid NOT NULL,
  turn_day   date NOT NULL,
  kind       text NOT NULL CHECK (kind IN ('plan', 'patch')),
  plan_id    uuid REFERENCES lesson_plan (id) ON DELETE CASCADE,
  counted    boolean NOT NULL DEFAULT true,
  created_at timestamptz NOT NULL DEFAULT now(),
  FOREIGN KEY (org_id, teacher_id) REFERENCES app_user (org_id, id) ON DELETE CASCADE
);
CREATE INDEX lesson_plan_turn_day_idx ON lesson_plan_turn (org_id, teacher_id, turn_day);

GRANT SELECT ON lp_ppct_lesson, lp_ppct_period, lp_knowledge_unit, lp_exercise TO geo_app;
GRANT SELECT, INSERT, UPDATE ON lesson_plan, lesson_plan_version TO geo_app;
GRANT SELECT, INSERT, UPDATE ON lesson_plan_turn TO geo_app;
GRANT USAGE, SELECT ON SEQUENCE lesson_plan_code_seq TO geo_app;
