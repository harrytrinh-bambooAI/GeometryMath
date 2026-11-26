-- S2-E4 (T-261126-24, PLAN_code-s23 §2, §4; US-S2E4-01…03). Chủ: module content.
-- Chat chỉnh nháp đề, import docx/pdf tách bài tập, đối chiếu trùng với kho.
--   problem_draft_chat : lịch sử trao đổi của MỘT nháp đề (problem_draft của S2-E3). role = teacher | ai | system;
--                        kind = message (tin giáo viên) | question (AI hỏi lại, BR-S2E4-01) | proposal (đề xuất có
--                        trước / sau / nguồn / chỗ chưa chắc, BR-S2E4-02) | note (ghi chú hệ thống: đã hoàn tác…).
--                        proposal_state = pending | accepted | rejected; decided_note = "đã nhận", "đã bác",
--                        "đã sửa tay từ 14 thành 15". Số lượt chat của nháp = số dòng role = 'teacher' (tối đa 30,
--                        BR-S2E4-06). Không xóa.
--   problem_draft_edit : ngăn xếp hoàn tác của nháp (BR-S2E4-03): mỗi lần nhận đề xuất ghi ảnh chụp trạng thái TRƯỚC khi
--                        đổi (chữ đề, sổ lời giải, mô tả hình). Chỉ giữ 10 bước gần nhất; hoàn tác xóa bước trên cùng.
--   import_job         : một lần nạp tệp docx/pdf (BR-S2E4-07…10). Tệp gửi theo đoạn vào file_tmp (≤ 10 MB) và bị XÓA
--                        ngay sau khi tách (không lưu tệp gốc). status = uploading | parsed | rejected (kèm error).
--                        notes = các dòng báo ("Phần lý thuyết chưa hỗ trợ ở chặng này", "Tệp có macro, đã bỏ qua"…).
--   import_item        : từng bài tập máy tách được (tối đa 50 mỗi tệp). norm_key = sha-256 của chữ đề đã chuẩn hóa
--                        (BR-S2E4-11) để nhận ra lần import sau của cùng bài; draft_id = nháp sinh ra khi giáo viên tick.
-- AI không tự lưu (BR-S2E4-04): nhận đề xuất chỉ đổi NHÁP; sổ lời giải của nháp (solution_doc, chưa duyệt) được đồng bộ
-- chữ đề / mô tả hình và nhãn máy về "Cần xem lại" (BR-S2E4-05) — cần quyền UPDATE ba cột dưới đây.

CREATE TABLE problem_draft_chat (
  id             uuid PRIMARY KEY,
  org_id         uuid NOT NULL,
  draft_id       uuid NOT NULL,
  owner_user_id  uuid NOT NULL,
  seq            integer NOT NULL CHECK (seq >= 1),
  role           text NOT NULL CHECK (role IN ('teacher', 'ai', 'system')),
  kind           text NOT NULL CHECK (kind IN ('message', 'question', 'proposal', 'note')),
  content        text NOT NULL CHECK (length(content) BETWEEN 1 AND 2000),
  proposal       jsonb CHECK (proposal IS NULL OR jsonb_typeof(proposal) = 'object'),
  proposal_state text CHECK (proposal_state IS NULL OR proposal_state IN ('pending', 'accepted', 'rejected')),
  decided_note   text CHECK (decided_note IS NULL OR length(decided_note) BETWEEN 1 AND 500),
  created_at     timestamptz NOT NULL DEFAULT now(),
  UNIQUE (org_id, id),
  UNIQUE (org_id, draft_id, seq),
  FOREIGN KEY (org_id, draft_id) REFERENCES problem_draft (org_id, id),
  FOREIGN KEY (org_id, owner_user_id) REFERENCES app_user (org_id, id),
  CONSTRAINT problem_draft_chat_proposal_ck CHECK (
    (kind = 'proposal') = (proposal IS NOT NULL) AND (kind = 'proposal') = (proposal_state IS NOT NULL))
);

CREATE TABLE problem_draft_edit (
  id         uuid PRIMARY KEY,
  org_id     uuid NOT NULL,
  draft_id   uuid NOT NULL,
  seq        integer NOT NULL CHECK (seq >= 1),
  chat_id    uuid,
  snapshot   jsonb NOT NULL CHECK (jsonb_typeof(snapshot) = 'object'),
  created_at timestamptz NOT NULL DEFAULT now(),
  UNIQUE (org_id, draft_id, seq),
  FOREIGN KEY (org_id, draft_id) REFERENCES problem_draft (org_id, id)
);

CREATE TABLE import_job (
  id            uuid PRIMARY KEY,
  org_id        uuid NOT NULL REFERENCES org (id),
  owner_user_id uuid NOT NULL,
  file_name     text NOT NULL CHECK (length(file_name) BETWEEN 1 AND 200),
  file_type     text CHECK (file_type IS NULL OR file_type IN ('docx', 'pdf')),
  size_bytes    integer NOT NULL CHECK (size_bytes BETWEEN 1 AND 10485760),
  file_tmp      bytea,                                    -- tệp đang nhận theo đoạn; NULL ngay sau khi tách / từ chối
  status        text NOT NULL DEFAULT 'uploading' CHECK (status IN ('uploading', 'parsed', 'rejected')),
  error         text CHECK (error IS NULL OR length(error) BETWEEN 1 AND 300),
  notes         jsonb NOT NULL DEFAULT '[]' CHECK (jsonb_typeof(notes) = 'array'),
  found         integer NOT NULL DEFAULT 0 CHECK (found >= 0),
  created_at    timestamptz NOT NULL DEFAULT now(),
  updated_at    timestamptz NOT NULL DEFAULT now(),
  UNIQUE (org_id, id),
  FOREIGN KEY (org_id, owner_user_id) REFERENCES app_user (org_id, id)
);
CREATE INDEX import_job_owner_idx ON import_job (org_id, owner_user_id, created_at DESC, id DESC);

CREATE TABLE import_item (
  org_id        uuid NOT NULL,
  job_id        uuid NOT NULL,
  idx           smallint NOT NULL CHECK (idx BETWEEN 1 AND 50),
  owner_user_id uuid NOT NULL,
  title         text NOT NULL CHECK (length(title) BETWEEN 1 AND 60),
  body          text NOT NULL CHECK (length(body) BETWEEN 1 AND 8000),
  norm_key      text NOT NULL CHECK (length(norm_key) = 64),
  draft_id      uuid,
  created_at    timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (org_id, job_id, idx),
  FOREIGN KEY (org_id, job_id) REFERENCES import_job (org_id, id),
  FOREIGN KEY (org_id, draft_id) REFERENCES problem_draft (org_id, id)
);
CREATE INDEX import_item_owner_key_idx ON import_item (org_id, owner_user_id, norm_key) WHERE draft_id IS NOT NULL;

GRANT SELECT, INSERT, UPDATE ON problem_draft_chat, import_job, import_item TO geo_app;
GRANT SELECT, INSERT, DELETE ON problem_draft_edit TO geo_app;
GRANT UPDATE (statement, title, figure) ON solution_doc TO geo_app;
