-- T-261128-56 · S5-E3 kho tri thức của giáo viên (US-S5E3-01, 02, 07). Module knowledge (gói internal/kb).
--   kb_document          tài liệu (KnowledgeItem) có chủ sở hữu là một giáo viên, mã TL-nn theo giáo viên (BR-S5E3-01).
--   kb_document_version  phiên bản (KnowledgeVersion): Nháp → Chờ duyệt → Đang dùng → Lưu trữ; tối đa một bản Đang dùng mỗi
--                        tài liệu; bản đã từng Đang dùng là bất biến (BR-S5E3-01, 02); tệp gốc giữ nguyên (docx/pdf/md ≤ 10 MB);
--                        băm nội dung sau chuẩn hoá để đối chiếu trùng (BR-S5E3-04).
--   kb_section           mục có tiêu đề tách từ nội dung (BR-S5E3-03) + mã kiến thức KT.. trong mục; tsvector bỏ dấu (QS5-09,
--                        BR-S5E3-24) qua kb_norm (chữ thường, bỏ dấu, số mũ ²³ → 23, ký tự khác chữ/số → khoảng trắng).
--   kb_grant             gắn tài liệu (không phải phiên bản) cho lớp hoặc một bài của lớp, thời hạn tuỳ chọn (BR-S5E3-06, 07).
--   kb_upload            lượt tải tệp theo đoạn (thân yêu cầu máy chủ ≤ 1 MB, như S2-E4): giữ tạm tới khi tạo tài liệu.

CREATE FUNCTION kb_norm(text) RETURNS text
  LANGUAGE sql IMMUTABLE PARALLEL SAFE STRICT
  AS $$ SELECT btrim(regexp_replace(
           f_unaccent(lower(translate($1, '⁰¹²³⁴⁵⁶⁷⁸⁹₀₁₂₃₄₅₆₇₈₉', '01234567890123456789'))),
           '[^a-z0-9]+', ' ', 'g')) $$;

CREATE TABLE kb_document (
  id          uuid PRIMARY KEY,
  org_id      uuid NOT NULL,
  owner_id    uuid NOT NULL,
  code        text NOT NULL CHECK (code ~ '^TL-[0-9]{2,4}$'),
  title       text NOT NULL CHECK (length(title) BETWEEN 1 AND 200),
  ai_authored boolean NOT NULL DEFAULT false,          -- nhãn người "AI soạn" tới khi giáo viên duyệt (BR-S5E3-05)
  archived_at timestamptz,                              -- Lưu trữ toàn bộ: mọi gắn kết hết hiệu lực ngay (BR-S5E3-07)
  created_at  timestamptz NOT NULL DEFAULT now(),
  updated_at  timestamptz NOT NULL DEFAULT now(),
  UNIQUE (org_id, id),
  UNIQUE (org_id, owner_id, code),
  FOREIGN KEY (org_id, owner_id) REFERENCES app_user (org_id, id)
);
COMMENT ON TABLE kb_document IS 'Tài liệu kho tri thức của một giáo viên (US-S5E3-01). Mã TL-nn tăng theo chủ sở hữu.';

CREATE TABLE kb_document_version (
  org_id       uuid NOT NULL,
  doc_id       uuid NOT NULL,
  version      int  NOT NULL CHECK (version >= 1),
  status       text NOT NULL CHECK (status IN ('draft', 'pending', 'active', 'archived')),
  was_active   boolean NOT NULL DEFAULT false,
  source_kind  text NOT NULL CHECK (source_kind IN ('manual', 'file', 'lesson_plan', 'ai')),
  source_label text,                                    -- ví dụ "Giáo án GA-0001 v2", "Phieu-on-chuong-VII.docx"
  file_name    text,
  file_mime    text,
  file_size    int CHECK (file_size IS NULL OR file_size BETWEEN 1 AND 10485760),
  file_bytes   bytea,                                   -- tệp gốc giữ nguyên, tải lại được (US-S5E3-01-SC1)
  content_md   text NOT NULL CHECK (length(content_md) BETWEEN 1 AND 2000000),
  content_hash text NOT NULL CHECK (content_hash ~ '^[0-9a-f]{64}$'),
  created_by   uuid NOT NULL,
  created_at   timestamptz NOT NULL DEFAULT now(),
  activated_at timestamptz,
  archived_at  timestamptz,
  PRIMARY KEY (org_id, doc_id, version),
  FOREIGN KEY (org_id, doc_id) REFERENCES kb_document (org_id, id) ON DELETE CASCADE
);
-- BR-S5E3-02: tại một thời điểm tối đa một phiên bản Đang dùng
CREATE UNIQUE INDEX kb_version_one_active_uq ON kb_document_version (org_id, doc_id) WHERE status = 'active';
-- BR-S5E3-04: đối chiếu trùng theo băm
CREATE INDEX kb_version_hash_idx ON kb_document_version (org_id, content_hash);

-- BR-S5E3-01: nội dung của phiên bản đã rời Nháp (Chờ duyệt, Đang dùng, Lưu trữ) hoặc từng Đang dùng không đổi được.
CREATE FUNCTION tg_kb_version_immutable() RETURNS trigger
  LANGUAGE plpgsql AS $$
BEGIN
  IF (OLD.was_active OR OLD.status <> 'draft')
     AND (NEW.content_md IS DISTINCT FROM OLD.content_md
          OR NEW.content_hash IS DISTINCT FROM OLD.content_hash
          OR NEW.file_bytes IS DISTINCT FROM OLD.file_bytes) THEN
    RAISE EXCEPTION 'kb_document_version %/% bất biến', OLD.doc_id, OLD.version USING ERRCODE = 'check_violation';
  END IF;
  IF OLD.was_active AND NOT NEW.was_active THEN
    RAISE EXCEPTION 'was_active không quay lại false' USING ERRCODE = 'check_violation';
  END IF;
  RETURN NEW;
END $$;
CREATE TRIGGER kb_version_immutable BEFORE UPDATE ON kb_document_version
  FOR EACH ROW EXECUTE FUNCTION tg_kb_version_immutable();

CREATE TABLE kb_section (
  id         uuid PRIMARY KEY,
  org_id     uuid NOT NULL,
  doc_id     uuid NOT NULL,
  version    int  NOT NULL,
  ord        int  NOT NULL CHECK (ord >= 1),
  title      text NOT NULL CHECK (length(title) BETWEEN 1 AND 300),
  body       text NOT NULL,
  codes      text[] NOT NULL DEFAULT '{}',              -- mã kiến thức KT11-… nêu trong mục (BR-S5E3-24)
  search_tsv tsvector GENERATED ALWAYS AS (
               setweight(to_tsvector('simple', kb_norm(title)), 'A') ||
               setweight(to_tsvector('simple', kb_norm(body)), 'B')) STORED,
  UNIQUE (org_id, doc_id, version, ord),
  FOREIGN KEY (org_id, doc_id, version) REFERENCES kb_document_version (org_id, doc_id, version) ON DELETE CASCADE
);
CREATE INDEX kb_section_search_idx ON kb_section USING gin (org_id, search_tsv);

CREATE TABLE kb_grant (
  id            uuid PRIMARY KEY,
  org_id        uuid NOT NULL,
  doc_id        uuid NOT NULL,
  class_id      uuid NOT NULL,
  assignment_id uuid,                                   -- NULL = cả lớp; có = riêng một bài của lớp
  valid_from    timestamptz,
  valid_to      timestamptz,
  created_by    uuid NOT NULL,
  created_at    timestamptz NOT NULL DEFAULT now(),
  revoked_at    timestamptz,
  CHECK (valid_from IS NULL OR valid_to IS NULL OR valid_to > valid_from),
  FOREIGN KEY (org_id, doc_id) REFERENCES kb_document (org_id, id) ON DELETE CASCADE,
  FOREIGN KEY (org_id, class_id) REFERENCES class (org_id, id) ON DELETE CASCADE,
  FOREIGN KEY (org_id, assignment_id) REFERENCES assignment (org_id, id) ON DELETE CASCADE
);
CREATE INDEX kb_grant_class_idx ON kb_grant (org_id, class_id) WHERE revoked_at IS NULL;
CREATE INDEX kb_grant_doc_idx ON kb_grant (org_id, doc_id);

CREATE TABLE kb_upload (
  id         uuid PRIMARY KEY,
  org_id     uuid NOT NULL,
  owner_id   uuid NOT NULL,
  file_name  text NOT NULL CHECK (length(file_name) BETWEEN 1 AND 200),
  size_bytes int  NOT NULL CHECK (size_bytes BETWEEN 1 AND 10485760),
  file_tmp   bytea,
  created_at timestamptz NOT NULL DEFAULT now(),
  FOREIGN KEY (org_id, owner_id) REFERENCES app_user (org_id, id)
);

GRANT EXECUTE ON FUNCTION kb_norm(text) TO geo_app, geo_ai;
GRANT SELECT, INSERT, UPDATE ON kb_document, kb_document_version, kb_grant TO geo_app;
GRANT SELECT, INSERT ON kb_section TO geo_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON kb_upload TO geo_app;
GRANT SELECT ON kb_document, kb_document_version, kb_section, kb_grant TO geo_ai;
