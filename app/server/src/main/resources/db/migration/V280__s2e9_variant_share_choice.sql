-- S2-E9 (T-261126-25, PLAN_code-s23 §2, §4; US-S2E9-01…03). Chủ: content (biến thể, liên kết chỉ xem, sổ xác nhận xuất),
-- assignment (câu trắc nghiệm của bộ bài).
--   problem_variant        (content)   : một biến thể (ProblemVariant) của một đề "Của tôi" (solution_doc): đề mới (doc_id) sinh
--                                        từ đề gốc (source_doc_id) theo hạt giống; params = bộ số liệu mới; answer do ENGINE
--                                        tính lại (answer_source luôn 'engine', BR-S2E9-01). Không sửa, không xóa.
--   problem_share_link     (content)   : liên kết chỉ xem của một đề (BR-S2E9-09, 10). Chỉ lưu SHA-256 của mã (mã 256 bit
--                                        chỉ hiện một lần lúc tạo); hạn 1/7/30 ngày; thu hồi = đặt revoked_at. Người mở
--                                        liên kết không đăng nhập đọc qua fn_share_link_view (chỉ chữ đề + mô tả hình).
--                                        PLAN §2 ghi "dùng share_grant sẵn có": share_grant là cấp phép LIÊN TRƯỜNG (bắt buộc
--                                        trường nhận, hai bên duyệt) nên không biểu diễn được liên kết công khai; TẠM dùng
--                                        bảng riêng (ghi ở qc_findings S2-E9).
--   export_confirm_log     (content)   : lần giáo viên xác nhận "Vẫn xuất" kèm lời giải khi đề đang giao còn khóa (BR-S2E9-08).
--   assignment_item_choice (assignment): cấu hình trắc nghiệm của một câu trong bộ bài, khóa (bộ, đề) nên giữ nguyên khi bộ
--                                        Nháp ghi lại danh sách câu. options = [{id, text}] 2…5 lựa chọn; correct_id = lựa
--                                        chọn đúng (BR-S2E9-11). Không bao giờ đưa correct_id vào dữ liệu trả cho học sinh.

CREATE TABLE problem_variant (
  id            uuid PRIMARY KEY,
  org_id        uuid NOT NULL,
  source_doc_id uuid NOT NULL,
  doc_id        uuid NOT NULL,
  owner_user_id uuid NOT NULL,
  seed          bigint NOT NULL,
  ordinal       smallint NOT NULL CHECK (ordinal BETWEEN 1 AND 10),
  params        jsonb NOT NULL CHECK (jsonb_typeof(params) = 'array'),
  answer        text NOT NULL CHECK (length(answer) BETWEEN 1 AND 100),
  answer_value  double precision,
  answer_source text NOT NULL DEFAULT 'engine' CHECK (answer_source = 'engine'),
  created_at    timestamptz NOT NULL DEFAULT now(),
  UNIQUE (org_id, doc_id),
  FOREIGN KEY (org_id, source_doc_id) REFERENCES solution_doc (org_id, id),
  FOREIGN KEY (org_id, doc_id) REFERENCES solution_doc (org_id, id),
  FOREIGN KEY (org_id, owner_user_id) REFERENCES app_user (org_id, id)
);
CREATE INDEX problem_variant_source_idx ON problem_variant (org_id, source_doc_id, created_at DESC, id DESC);
COMMENT ON TABLE problem_variant IS 'Biến thể đề (S2-E9): đề mới sinh từ đề gốc theo hạt giống; đáp số do engine tính lại.';

CREATE TABLE problem_share_link (
  id         uuid PRIMARY KEY,
  org_id     uuid NOT NULL,
  doc_id     uuid NOT NULL,
  created_by uuid NOT NULL,
  token_hash bytea NOT NULL UNIQUE CHECK (octet_length(token_hash) = 32),
  token_hint text NOT NULL CHECK (length(token_hint) BETWEEN 1 AND 8),
  days       smallint NOT NULL CHECK (days IN (1, 7, 30)),
  created_at timestamptz NOT NULL,
  expires_at timestamptz NOT NULL,
  revoked_at timestamptz,
  UNIQUE (org_id, id),
  FOREIGN KEY (org_id, doc_id) REFERENCES solution_doc (org_id, id),
  FOREIGN KEY (org_id, created_by) REFERENCES app_user (org_id, id),
  CONSTRAINT problem_share_link_window_ck CHECK (expires_at > created_at)
);
CREATE INDEX problem_share_link_doc_idx ON problem_share_link (org_id, doc_id, created_at DESC, id DESC);
COMMENT ON TABLE problem_share_link IS 'Liên kết chỉ xem của đề (S2-E9). Chỉ lưu băm của mã; đọc công khai qua fn_share_link_view.';

-- Đường DUY NHẤT cho người mở liên kết (không đăng nhập, không có mã trường): chỉ chữ đề, mô tả hình và nhãn máy của đề.
-- Mã sai, hết hạn (p_now >= expires_at) hay đã thu hồi đều không trả hàng nào (BR-S2E9-10: không phân biệt).
CREATE FUNCTION fn_share_link_view(p_hash bytea, p_now timestamptz)
RETURNS TABLE (title text, statement text, figure jsonb, machine_status text)
LANGUAGE sql STABLE SECURITY DEFINER SET search_path = public, pg_temp
AS $$
  SELECT d.title, d.statement, d.figure,
         (SELECT v.machine_status::text FROM solution_version v
           WHERE v.org_id = d.org_id AND v.doc_id = d.id AND v.version = d.latest_version)
  FROM problem_share_link l
  JOIN solution_doc d ON d.org_id = l.org_id AND d.id = l.doc_id
  WHERE l.token_hash = p_hash AND l.revoked_at IS NULL AND l.expires_at > p_now
$$;
COMMENT ON FUNCTION fn_share_link_view IS 'Đọc đề qua liên kết chỉ xem (S2-E9): không lời giải, không danh tính giáo viên.';

CREATE TABLE export_confirm_log (
  id         uuid PRIMARY KEY,
  org_id     uuid NOT NULL,
  user_id    uuid NOT NULL,
  doc_ids    jsonb NOT NULL CHECK (jsonb_typeof(doc_ids) = 'array'),
  created_at timestamptz NOT NULL,
  FOREIGN KEY (org_id, user_id) REFERENCES app_user (org_id, id)
);
CREATE INDEX export_confirm_log_user_idx ON export_confirm_log (org_id, user_id, created_at DESC);
COMMENT ON TABLE export_confirm_log IS 'Giáo viên xác nhận xuất kèm lời giải khi đề đang giao còn khóa (BR-S2E9-08).';

CREATE TABLE assignment_item_choice (
  org_id     uuid NOT NULL,
  set_id     uuid NOT NULL,
  problem_id uuid NOT NULL,
  options    jsonb NOT NULL CHECK (jsonb_typeof(options) = 'array' AND jsonb_array_length(options) BETWEEN 2 AND 5),
  correct_id uuid NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (org_id, set_id, problem_id),
  FOREIGN KEY (org_id, set_id) REFERENCES assignment_set (org_id, id) ON DELETE CASCADE
);
COMMENT ON TABLE assignment_item_choice IS 'Câu trắc nghiệm của bộ bài (S2-E9): 2…5 lựa chọn, đúng một lựa chọn đúng. correct_id chỉ dùng ở máy chủ.';

-- Bộ Nháp ghi lại danh sách câu (xóa rồi chèn): số lượt của câu trắc nghiệm không vượt số lựa chọn − 1 (BR-S2E9-12).
CREATE FUNCTION tg_assignment_item_choice_attempts() RETURNS trigger
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp AS $$
DECLARE n integer;
BEGIN
  SELECT jsonb_array_length(c.options) INTO n FROM assignment_item_choice c
   WHERE c.org_id = NEW.org_id AND c.set_id = NEW.set_id AND c.problem_id = NEW.problem_id;
  IF n IS NOT NULL AND NEW.max_attempts > n - 1 THEN
    NEW.max_attempts := n - 1;
  END IF;
  RETURN NEW;
END $$;
CREATE TRIGGER assignment_item_choice_attempts BEFORE INSERT ON assignment_item
  FOR EACH ROW EXECUTE FUNCTION tg_assignment_item_choice_attempts();

-- ---------------------------------------------------------------- quyền theo vai (mẫu V160, V180)
GRANT SELECT, INSERT ON problem_variant, export_confirm_log TO geo_app;
GRANT SELECT, INSERT ON problem_share_link TO geo_app;
GRANT UPDATE (revoked_at) ON problem_share_link TO geo_app;
GRANT SELECT, INSERT, DELETE ON assignment_item_choice TO geo_app;
GRANT UPDATE (max_attempts) ON assignment_item TO geo_app;
REVOKE ALL ON FUNCTION fn_share_link_view(bytea, timestamptz) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION fn_share_link_view(bytea, timestamptz) TO geo_app;
