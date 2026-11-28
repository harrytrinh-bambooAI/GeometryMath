-- T-261128-52 (S5-E2, US-S5E2-01…04): dữ liệu đề (ma trận chuẩn, mẫu câu, ngân hàng câu) và bộ đề nhiều mã.
-- Dữ liệu đề là dữ liệu tham chiếu dùng chung mọi trường (không org_id), nạp bằng lệnh nạp có kiểm
-- (`make exam-import`, ExamImportService) — migration chỉ tạo bảng, không chứa dữ liệu (PO-54).
-- Bộ đề (exam_set) và mã đề (exam_variant) thuộc trường của giáo viên (org_id).

CREATE TABLE exam_data_version (
  version      text PRIMARY KEY,
  content_hash text NOT NULL,
  meta         jsonb NOT NULL DEFAULT '{}'::jsonb,
  report       jsonb NOT NULL DEFAULT '{}'::jsonb,
  loaded_at    timestamptz NOT NULL DEFAULT now()
);
COMMENT ON TABLE exam_data_version IS 'Phiên bản dữ liệu đề đã nạp (ví dụ "de-thi v1.0"): báo cáo nạp gần nhất, danh mục chủ đề (meta.topics).';

CREATE TABLE exam_matrix (
  code         text PRIMARY KEY,
  id           uuid NOT NULL UNIQUE,
  data_version text NOT NULL REFERENCES exam_data_version (version),
  name         text NOT NULL,
  exam_kind    text NOT NULL,
  grade        smallint NOT NULL,
  minutes      numeric(6, 2) NOT NULL,
  total_points numeric(6, 3) NOT NULL,
  body         jsonb NOT NULL,
  content_hash text NOT NULL
);
COMMENT ON TABLE exam_matrix IS 'Ma trận đề chuẩn (materials/de-thi/ma-tran), body = JSON gốc đã qua kiểm schema + tổng điểm 10 + trọng số.';

CREATE TABLE exam_item_template (
  code         text PRIMARY KEY,
  data_version text NOT NULL REFERENCES exam_data_version (version),
  topic        text NOT NULL,
  body         jsonb NOT NULL,
  content_hash text NOT NULL
);
COMMENT ON TABLE exam_item_template IS 'Mẫu câu có tham số (materials/de-thi/mau-cau).';

CREATE TABLE exam_bank_item (
  code          text PRIMARY KEY,
  data_version  text NOT NULL REFERENCES exam_data_version (version),
  template_code text NOT NULL,
  topic         text NOT NULL,
  item_type     text NOT NULL CHECK (item_type IN ('tn4', 'dung-sai', 'tra-loi-ngan', 'tu-luan')),
  level         text NOT NULL CHECK (level IN ('NB', 'TH', 'VD', 'VDC')),
  body          jsonb NOT NULL,
  content_hash  text NOT NULL
);
CREATE INDEX exam_bank_item_cell_idx ON exam_bank_item (topic, item_type, level);
COMMENT ON TABLE exam_bank_item IS 'Câu ngân hàng (materials/de-thi/ngan-hang). Chỉ câu có đáp số khớp engine (1e-6) mới được nạp (BR-S5E2-02).';

CREATE TABLE exam_set (
  id             uuid PRIMARY KEY,
  org_id         uuid NOT NULL REFERENCES org (id),
  teacher_id     uuid NOT NULL,
  class_id       uuid,
  matrix_code    text NOT NULL,
  name           text NOT NULL,
  seed           bigint NOT NULL,
  params         jsonb NOT NULL,
  status         text NOT NULL DEFAULT 'draft' CHECK (status IN ('draft', 'approved')),
  human_label    text NOT NULL DEFAULT 'may-lap' CHECK (human_label IN ('may-lap', 'giao-vien-da-duyet')),
  report         jsonb NOT NULL DEFAULT '{}'::jsonb,
  approved_by    uuid,
  approved_at    timestamptz,
  created_at     timestamptz NOT NULL DEFAULT now(),
  UNIQUE (org_id, id),
  FOREIGN KEY (org_id, teacher_id) REFERENCES app_user (org_id, id)
);
CREATE INDEX exam_set_teacher_idx ON exam_set (org_id, teacher_id, created_at DESC);
COMMENT ON TABLE exam_set IS 'Bộ đề lắp từ ma trận (US-S5E2-03). Nhãn người "Máy lắp" tới khi giáo viên duyệt thành "Giáo viên đã duyệt" (BR-S5E2-10, QS5-08); chỉ bộ đã duyệt vào ngăn "Của tôi" (BR-S5E2-11).';

CREATE TABLE exam_variant (
  id         uuid PRIMARY KEY,
  org_id     uuid NOT NULL,
  set_id     uuid NOT NULL,
  code       text NOT NULL,
  position   smallint NOT NULL,
  difficulty numeric(8, 5) NOT NULL,
  body       jsonb NOT NULL,
  updated_at timestamptz NOT NULL DEFAULT now(),
  UNIQUE (org_id, set_id, code),
  FOREIGN KEY (org_id, set_id) REFERENCES exam_set (org_id, id) ON DELETE CASCADE
);
COMMENT ON TABLE exam_variant IS 'Một mã đề (101…108): body.cau = danh sách câu theo thứ tự in, phương án đã đảo, ô ma trận của từng câu.';

GRANT SELECT ON exam_data_version, exam_matrix, exam_item_template, exam_bank_item TO geo_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON exam_set, exam_variant TO geo_app;
