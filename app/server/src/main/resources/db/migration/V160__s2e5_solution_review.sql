-- S2-E5 (T-261126-20, PLAN_code-s23 §2; US-S2E5-01…04). Chủ: module content.
-- Soát và duyệt lời giải có phiên bản (khung C2-03):
--   solution_doc            : sổ lời giải của MỘT đề do một giáo viên sở hữu (BR-S2E5-06). latest_version là khóa lạc quan
--                             (BR-S2E5-05). figure: mô tả để engine dựng hình và tính lại đáp số ({"catalogCode": "HKN-02"} hoặc
--                             {"plane": {…}}); NULL = máy không có đáp số đối chiếu. problem_version_id (tùy chọn): đề kho mà
--                             sổ này gắn, dùng để ghim phiên bản lời giải vào bài giao.
--   solution_version        : mỗi lần lưu một phiên bản v1, v2…; steps bất biến (BR-S2E5-02, 22). Nhãn hai trục:
--                             machine_status chỉ do engine gắn (BR-S2E5-12, 15; trigger chặn 'verified' khi kết quả kiểm bước
--                             không đủ điều kiện), human_status do người (BR-S2E5-13).
--   step_check_result       : StepCheck của từng bước của một phiên bản (BR-S2E5-07…10). Không có hàng = "Chưa kiểm".
--   review_inbox_item       : đề xuất của AI chờ nhận / sửa / bỏ trong hộp duyệt (BR-S2E5-20). Không xóa.
--   solution_assignment_pin : bài giao gắn với MỘT phiên bản lời giải cố định (BR-S2E5-22). Ghim lúc tạo bài giao bằng
--                             trigger: phiên bản "Giáo viên đã duyệt" hiện hành của sổ mà người giao sở hữu cho đúng version đề.
--                             Đổi ghim ("Áp dụng cho lớp", US-S2E5-05) thuộc T-261126-22.
-- Học sinh không đọc được bước giải hay StepCheck qua các bảng này: mọi API của module đòi vai giáo viên, vai ứng dụng
-- geo_app không có SELECT trên cột nhạy cảm (đọc qua fn_solution_content_teacher, chỉ chủ sổ); đường học sinh nhận lời
-- giải vẫn là fn_get_solution của MVP (không đổi).

CREATE TYPE solution_human_status AS ENUM ('ai_drafted', 'editing', 'teacher_approved', 'superseded');
CREATE TYPE step_check_status     AS ENUM ('pass', 'fail', 'unverifiable');
CREATE TYPE review_item_status    AS ENUM ('pending', 'accepted', 'dismissed');

CREATE TABLE solution_doc (
  id                 uuid PRIMARY KEY,
  org_id             uuid NOT NULL REFERENCES org (id),
  owner_user_id      uuid NOT NULL,
  code               text CHECK (code IS NULL OR length(code) BETWEEN 1 AND 40),
  title              text NOT NULL CHECK (length(title) BETWEEN 1 AND 200),
  statement          text NOT NULL CHECK (length(statement) BETWEEN 1 AND 8000),
  figure             jsonb CHECK (figure IS NULL OR jsonb_typeof(figure) = 'object'),
  content_org_id     uuid,
  problem_version_id uuid,
  in_my_shelf        boolean NOT NULL DEFAULT false,       -- đề vào ngăn "Của tôi" sau lần duyệt đầu (BR-S2E5-19)
  latest_version     integer NOT NULL DEFAULT 0 CHECK (latest_version >= 0),
  created_at         timestamptz NOT NULL DEFAULT now(),
  UNIQUE (org_id, id),
  FOREIGN KEY (org_id, owner_user_id) REFERENCES app_user (org_id, id),
  FOREIGN KEY (content_org_id, problem_version_id) REFERENCES problem_version (org_id, id)
);
CREATE INDEX solution_doc_owner_idx ON solution_doc (org_id, owner_user_id, created_at DESC, id DESC);
-- Một giáo viên có nhiều nhất một sổ cho một version đề: trigger ghim bài giao chọn được đúng một sổ.
CREATE UNIQUE INDEX solution_doc_pv_uq ON solution_doc (org_id, owner_user_id, content_org_id, problem_version_id)
  WHERE problem_version_id IS NOT NULL;

CREATE TABLE solution_version (
  id             uuid PRIMARY KEY,
  org_id         uuid NOT NULL,
  doc_id         uuid NOT NULL,
  version        integer NOT NULL CHECK (version >= 1),
  steps          jsonb NOT NULL CHECK (jsonb_typeof(steps) = 'array' AND jsonb_array_length(steps) BETWEEN 1 AND 30),
  ai_authored    boolean NOT NULL DEFAULT false,
  author_user_id uuid,                                      -- NULL khi AI soạn
  note           text NOT NULL CHECK (length(note) BETWEEN 1 AND 200),
  human_status   solution_human_status NOT NULL,
  machine_status verify_status NOT NULL DEFAULT 'needs_review',
  final_state    text CHECK (final_state IS NULL OR final_state IN ('match', 'mismatch', 'unknown')),
  final_note     text,
  checked_at     timestamptz,
  approved_by    uuid,
  approved_code  text,
  approved_at    timestamptz,
  created_at     timestamptz NOT NULL DEFAULT now(),
  UNIQUE (org_id, id),
  UNIQUE (doc_id, version),
  FOREIGN KEY (org_id, doc_id) REFERENCES solution_doc (org_id, id),
  FOREIGN KEY (org_id, author_user_id) REFERENCES app_user (org_id, id),
  FOREIGN KEY (org_id, approved_by) REFERENCES app_user (org_id, id),
  CONSTRAINT solution_version_author_ck CHECK (ai_authored = (author_user_id IS NULL)),
  CONSTRAINT solution_version_approved_ck CHECK ((approved_at IS NULL) = (approved_by IS NULL)),
  CONSTRAINT solution_version_human_ck CHECK (
    (human_status IN ('teacher_approved', 'superseded')) = (approved_at IS NOT NULL))
);
COMMENT ON TABLE solution_version IS 'Phiên bản lời giải (S2-E5). doc_id, version, steps bất biến; không xóa. machine_status chỉ do engine gắn (trigger tg_solution_version_machine_guard).';
CREATE TRIGGER solution_version_immutable BEFORE UPDATE ON solution_version
  FOR EACH ROW EXECUTE FUNCTION tg_immutable_cols('doc_id', 'version', 'steps', 'org_id', 'ai_authored', 'author_user_id');

CREATE TABLE step_check_result (
  org_id     uuid NOT NULL,
  version_id uuid NOT NULL,
  step_no    smallint NOT NULL CHECK (step_no BETWEEN 1 AND 30),
  status     step_check_status NOT NULL,
  reason     text NOT NULL CHECK (length(reason) BETWEEN 1 AND 300),
  written    double precision,
  computed   double precision,
  checked_at timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (org_id, version_id, step_no),
  FOREIGN KEY (org_id, version_id) REFERENCES solution_version (org_id, id)
);

-- Nhãn máy "Đã kiểm chứng" (BR-S2E5-12): không bước Không đạt, ít nhất một bước Đạt, đáp số cuối khớp engine.
-- Mọi phiên bản sinh ra là 'needs_review'; không lệnh UPDATE nào đặt được 'verified' khi kết quả kiểm không đủ.
CREATE FUNCTION tg_solution_version_machine_guard() RETURNS trigger
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp AS $$
BEGIN
  IF NEW.machine_status = 'verified' AND (
       TG_OP = 'INSERT'
       OR NEW.final_state IS DISTINCT FROM 'match'
       OR EXISTS (SELECT 1 FROM step_check_result r
                  WHERE r.org_id = NEW.org_id AND r.version_id = NEW.id AND r.status = 'fail')
       OR NOT EXISTS (SELECT 1 FROM step_check_result r
                      WHERE r.org_id = NEW.org_id AND r.version_id = NEW.id AND r.status = 'pass')) THEN
    RAISE EXCEPTION 'nhãn "Đã kiểm chứng" cần kết quả kiểm bước đạt và đáp số khớp engine (BR-S2E5-12)'
      USING ERRCODE = 'check_violation';
  END IF;
  RETURN NEW;
END $$;
CREATE TRIGGER solution_version_machine_guard BEFORE INSERT OR UPDATE OF machine_status, final_state ON solution_version
  FOR EACH ROW EXECUTE FUNCTION tg_solution_version_machine_guard();

CREATE TABLE review_inbox_item (
  id             uuid PRIMARY KEY,
  org_id         uuid NOT NULL,
  doc_id         uuid NOT NULL,
  after_step     smallint NOT NULL CHECK (after_step BETWEEN 0 AND 30),   -- chèn bước mới sau bước này
  content        text NOT NULL CHECK (length(btrim(content)) BETWEEN 1 AND 2000),
  status         review_item_status NOT NULL DEFAULT 'pending',
  decided_by     uuid,
  decided_at     timestamptz,
  result_version integer,
  created_at     timestamptz NOT NULL DEFAULT now(),
  UNIQUE (org_id, id),
  FOREIGN KEY (org_id, doc_id) REFERENCES solution_doc (org_id, id),
  FOREIGN KEY (org_id, decided_by) REFERENCES app_user (org_id, id),
  CONSTRAINT review_inbox_decided_ck CHECK ((status = 'pending') = (decided_at IS NULL))
);
CREATE INDEX review_inbox_doc_idx ON review_inbox_item (org_id, doc_id, created_at DESC, id DESC);

CREATE TABLE solution_assignment_pin (
  org_id        uuid NOT NULL,
  assignment_id uuid NOT NULL,
  doc_id        uuid NOT NULL,
  version_id    uuid NOT NULL,
  pinned_at     timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (org_id, assignment_id),
  FOREIGN KEY (org_id, assignment_id) REFERENCES assignment (org_id, id),
  FOREIGN KEY (org_id, doc_id) REFERENCES solution_doc (org_id, id),
  FOREIGN KEY (org_id, version_id) REFERENCES solution_version (org_id, id)
);
CREATE INDEX solution_assignment_pin_doc_idx ON solution_assignment_pin (org_id, doc_id);

-- Ghim lúc giao: bài lớp mới của giáo viên G cho version đề V nhận phiên bản lời giải "Giáo viên đã duyệt" hiện hành
-- của sổ mà G sở hữu cho V (nếu có). Chỉ INSERT … SELECT, không ném lỗi: không ảnh hưởng luồng giao bài MVP.
CREATE FUNCTION tg_assignment_pin_solution() RETURNS trigger LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp AS $$
BEGIN
  IF NEW.kind = 'class' THEN
    INSERT INTO solution_assignment_pin (org_id, assignment_id, doc_id, version_id)
    SELECT NEW.org_id, NEW.id, d.id, v.id
    FROM solution_doc d
    JOIN solution_version v ON v.org_id = d.org_id AND v.doc_id = d.id AND v.human_status = 'teacher_approved'
    WHERE d.org_id = NEW.org_id AND d.owner_user_id = NEW.assigned_by
      AND d.content_org_id = NEW.content_org_id AND d.problem_version_id = NEW.problem_version_id
    ORDER BY v.version DESC
    LIMIT 1
    ON CONFLICT DO NOTHING;
  END IF;
  RETURN NEW;
END $$;
CREATE TRIGGER assignment_pin_solution AFTER INSERT ON assignment
  FOR EACH ROW EXECUTE FUNCTION tg_assignment_pin_solution();

-- ---------------------------------------------------------------- cổng đọc nội dung nhạy cảm
-- Bước giải, lý do / giá trị của StepCheck, ghi chú đối chiếu đáp số và nội dung đề xuất của AI lộ lời giải và đáp số:
-- vai ứng dụng geo_app KHÔNG có SELECT trên các cột đó (cùng nguyên tắc với bảng solution của MVP, TC-GEO-0666). Đường
-- đọc duy nhất là hàm dưới đây: chỉ trả hàng khi p_user là giáo viên đang hoạt động và là CHỦ sổ lời giải.
CREATE FUNCTION fn_solution_content_teacher(p_org uuid, p_user uuid, p_doc uuid, p_version integer)
RETURNS TABLE (steps jsonb, checks jsonb, proposals jsonb, final_note text)
LANGUAGE plpgsql STABLE SECURITY DEFINER SET search_path = public, pg_temp
AS $$
#variable_conflict use_column
BEGIN
  RETURN QUERY
  SELECT v.steps,
         COALESCE((SELECT jsonb_agg(jsonb_build_object('no', r.step_no, 'status', r.status, 'reason', r.reason,
                                                       'written', r.written, 'computed', r.computed) ORDER BY r.step_no)
                   FROM step_check_result r WHERE r.org_id = v.org_id AND r.version_id = v.id), '[]'::jsonb),
         COALESCE((SELECT jsonb_agg(jsonb_build_object('id', i.id, 'afterStep', i.after_step, 'content', i.content)
                                    ORDER BY i.created_at DESC, i.id DESC)
                   FROM review_inbox_item i
                   WHERE i.org_id = d.org_id AND i.doc_id = d.id AND i.status = 'pending'), '[]'::jsonb),
         v.final_note
  FROM solution_doc d
  JOIN app_user u ON u.org_id = d.org_id AND u.id = d.owner_user_id AND u.role = 'teacher' AND u.deactivated_at IS NULL
  JOIN solution_version v ON v.org_id = d.org_id AND v.doc_id = d.id AND v.version = p_version
  WHERE d.org_id = p_org AND d.id = p_doc AND d.owner_user_id = p_user;
END $$;
COMMENT ON FUNCTION fn_solution_content_teacher IS 'Đường DUY NHẤT để ứng dụng đọc bước giải, StepCheck và đề xuất của AI của một phiên bản lời giải (S2-E5): chỉ giáo viên chủ sổ. Học sinh không có đường nào tới các bảng này.';

-- Chép kết quả kiểm của các bước 1…p_max_step sang phiên bản mới của CÙNG sổ (BR-S2E5-10); dữ liệu không rời CSDL.
CREATE FUNCTION fn_solution_copy_checks(p_org uuid, p_from uuid, p_to uuid, p_max_step integer)
RETURNS integer
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
DECLARE n integer;
BEGIN
  INSERT INTO step_check_result (org_id, version_id, step_no, status, reason, written, computed, checked_at)
  SELECT r.org_id, b.id, r.step_no, r.status, r.reason, r.written, r.computed, r.checked_at
  FROM step_check_result r
  JOIN solution_version a ON a.org_id = r.org_id AND a.id = r.version_id
  JOIN solution_version b ON b.org_id = a.org_id AND b.doc_id = a.doc_id AND b.id = p_to
  WHERE r.org_id = p_org AND r.version_id = p_from AND r.step_no <= p_max_step;
  GET DIAGNOSTICS n = ROW_COUNT;
  RETURN n;
END $$;

-- Quyền theo vai (mẫu V10). geo_app: ghi phiên bản / kết quả kiểm, đọc siêu dữ liệu; KHÔNG đọc cột nhạy cảm
-- (solution_version.steps, final_note; step_check_result.reason, written, computed; review_inbox_item.content).
GRANT SELECT, INSERT ON solution_doc TO geo_app;
GRANT UPDATE (latest_version, in_my_shelf) ON solution_doc TO geo_app;
GRANT INSERT ON solution_version, step_check_result, review_inbox_item TO geo_app;
GRANT SELECT (id, org_id, doc_id, version, ai_authored, author_user_id, note, human_status, machine_status, final_state,
              checked_at, approved_by, approved_code, approved_at, created_at) ON solution_version TO geo_app;
GRANT UPDATE (human_status, machine_status, final_state, final_note, checked_at, approved_by, approved_code, approved_at)
  ON solution_version TO geo_app;
GRANT SELECT (org_id, version_id, step_no, status, checked_at) ON step_check_result TO geo_app;
GRANT DELETE ON step_check_result TO geo_app;
GRANT SELECT (id, org_id, doc_id, after_step, status, decided_by, decided_at, result_version, created_at)
  ON review_inbox_item TO geo_app;
GRANT UPDATE (status, decided_by, decided_at, result_version) ON review_inbox_item TO geo_app;
GRANT SELECT ON solution_assignment_pin TO geo_app;
REVOKE ALL ON FUNCTION fn_solution_content_teacher(uuid, uuid, uuid, integer),
                       fn_solution_copy_checks(uuid, uuid, uuid, integer) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION fn_solution_content_teacher(uuid, uuid, uuid, integer),
                          fn_solution_copy_checks(uuid, uuid, uuid, integer) TO geo_app;
