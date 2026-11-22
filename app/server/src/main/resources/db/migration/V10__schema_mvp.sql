-- V10: lược đồ chính thức MVP, sinh bằng app/db/tools/gen-v10.sh từ artifacts/GEO/work/architecture/03_schema.sql (đề xuất ARCH, bản 22/11/2026).
-- Khác bản ARCH: schema public (seed E2E của khung làm việc trên public); platform_org_id() = ...f001 vì ...0001
-- là F-GEO-001 "THPT Mẫu A" trong app/db/seed/fixture-ids.json; bỏ REVOKE ALL ON SCHEMA.
-- =====================================================================================
-- GEO "Hình Học Không Gian Cùng AI" - lược đồ PostgreSQL cho MVP (đề xuất của ARCH)
-- Chạy: psql -v ON_ERROR_STOP=1 -h 127.0.0.1 -p 54329 -U geo -d geo_arch2 -f 03_schema.sql
-- Trạng thái: ĐỀ XUẤT. Đã kiểm chứng áp dụng sạch trên PostgreSQL 17.11 (cụm riêng của dự án, CSDL geo_arch2).
-- Quy ước (xem 02_multi_tenant.md và 01_adr.md):
--   * Khóa chính UUID do ứng dụng sinh theo UUIDv7 (sắp theo thời gian). PostgreSQL 17 chưa có uuidv7(),
--     hàm geo.uuid_v7() bên dưới CHỈ dùng cho dữ liệu thử và kiểm thử, mã Java tự sinh.
--   * Bảng sự kiện (event_log, ai_call_log, answer_guard_log), nhật ký kiểm toán, qa_turn dùng bigint identity.
--   * Mọi bảng của trường có cột org_id; org_id đứng ĐẦU mọi chỉ mục tổ hợp thuộc phạm vi trường
--     (ngoại lệ có ghi chú trực tiếp ở chỉ mục).
--   * Kiểu chặt: enum, timestamptz, CHECK; JSON chỉ cho dữ liệu chỉ đọc nguyên khối (không lọc, không sắp xếp)
--     và mọi JSON đều có cột schema_version (D-GEO-160: bảng quan hệ dùng phiên bản migration).
--   * Mỗi chỉ mục có chú thích "phục vụ Qnn" (Qnn = truy vấn trong 04_query_catalogue.md). Chỉ mục không
--     phục vụ truy vấn nào thì không tạo.
-- Nhật ký sửa (dấu vết, không phải mốc):
--   Lượt sửa gom sau phản biện RV (review/RV_findings.md, D-GEO-150…170):
--   * Khóa lời giải và cách ly: fn_get_solution_teacher chỉ đề kho (D-GEO-153, RV-04); khóa ngoại tổ hợp cho
--     assignment -> problem_version (content_org_id), lock_source, qa_turn.user_id, tác giả (RV-12); hàm cổng trả mã
--     trạng thái NOT_FOUND / LOCKED / CONFLICT_* (RV-21).
--   * Quyền ghi của geo_app siết theo D-GEO-158 (RV-10): vai geo_content nạp kho nền tảng, vai geo_ops cấp tài khoản
--     giáo viên mẫu và mã; tạo học sinh, ghi tiến độ, tạo bài giao, đổi nhãn máy chỉ qua hàm SECURITY DEFINER.
--   * Tự học: chữ ký chuẩn problem_signature (D-GEO-150), kiểm kế thừa khóa ở mỗi lần xin Gợi ý 2 / lời giải
--     (D-GEO-151), gợi ý đề kho cho bài kế thừa khóa (D-GEO-155); danh mục hình mẫu, đại lượng ẩn, bộ câu thử (D-GEO-133).
--   * Mã Giáo viên mẫu theo tài khoản, lưu băm, thu hồi, hạn dùng, giới hạn thử sai (D-GEO-152, RV-02).
--   * Lưu câu trả lời hỏi đáp (D-GEO-159), request_id lũy đẳng cho sự kiện bền, open bền (D-GEO-162),
--     scene_shown (D-GEO-165), ràng buộc "cùng đề cùng lớp" (D-GEO-161), cờ "chưa có người soát" (D-GEO-137),
--     nhật ký Kiểm soát đáp án (ADR-GEO-14, D-GEO-156), bỏ keyword_trgm_idx cùng Q11b/c (D-GEO-166).
-- =====================================================================================

CREATE EXTENSION IF NOT EXISTS unaccent;
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS btree_gin;
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS pg_stat_statements;   -- giám sát truy vấn (NFR); cần shared_preload_libraries, đã có ở app/scripts/dev-up.sh


-- ------------------------------------------------------------------ vai CSDL (mức cụm)
-- geo_app     : vai kết nối của ứng dụng. KHÔNG đọc được solution, verification_report; không ghi trực tiếp người dùng,
--               bài giao, tiến độ, sự kiện, hỏi đáp, nội dung nền tảng (D-GEO-158). Ghi các thứ đó qua hàm SECURITY DEFINER.
-- geo_ai      : vai của cổng AI phía máy chủ: đọc lời giải để sinh/kiểm câu trả lời (ADR-GEO-14), ghi lượt hỏi đáp qua hàm.
-- geo_content : vai nạp nội dung kho nền tảng (đề kho, lời giải, bài giảng, danh mục hình mẫu, bộ câu thử). Không dùng ở đường người dùng.
-- geo_ops     : vai vận hành: tạo tài khoản Giáo viên mẫu, cấp/thu hồi mã, partition, dọn khóa lũy đẳng. Không dùng ở đường người dùng.
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'geo_app')     THEN CREATE ROLE geo_app NOLOGIN; END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'geo_ai')      THEN CREATE ROLE geo_ai  NOLOGIN; END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'geo_content') THEN CREATE ROLE geo_content NOLOGIN; END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'geo_ops')     THEN CREATE ROLE geo_ops NOLOGIN; END IF;
END $$;

-- ------------------------------------------------------------------ kiểu liệt kê
CREATE TYPE org_kind          AS ENUM ('platform', 'group', 'school');
CREATE TYPE user_role         AS ENUM ('student', 'parent', 'teacher', 'school_admin', 'agency_admin', 'platform_admin'); -- PO-01 + BR-31. MVP chỉ dùng student, teacher
CREATE TYPE account_kind      AS ENUM ('light', 'sample', 'verified', 'system');
  -- light: tên + mã lớp (IAM-01); sample: Giáo viên mẫu của một trường (D-GEO-160); verified: chặng 3;
  -- system: tác giả nền tảng đứng tên kho đề (D-GEO-160), không đăng nhập được
CREATE TYPE edu_level         AS ENUM ('thcs', 'thpt');
CREATE TYPE solid_kind        AS ENUM ('pyramid', 'prism', 'box', 'cube', 'cone', 'cylinder', 'sphere'); -- D-GEO-070; Oxyz là hệ trục, không phải loại hình
CREATE TYPE problem_source    AS ENUM ('catalog', 'student_input');     -- kho đề | đề học sinh tự nhập (tự học)
CREATE TYPE verify_status     AS ENUM ('verified', 'needs_review');     -- nhãn máy (PO-02, BR-07)
CREATE TYPE approval_status   AS ENUM ('ai_drafted', 'teacher_approved'); -- nhãn người (PO-02)
CREATE TYPE coord_source      AS ENUM ('engine', 'ai');                 -- BR-06, BR-08
CREATE TYPE assignment_kind   AS ENUM ('class', 'self');                -- giao cho lớp | bài tự học
CREATE TYPE event_kind        AS ENUM ('open', 'tool', 'hint', 'ask', 'unlock', 'scene_shown');
  -- tên theo 02_domain_model.md M-GEO-14/21/22. 'tool' chỉ gồm công cụ Chọn/Điểm/Đoạn/Mặt phẳng/Mở rộng/Đo (D-GEO-058).
  -- Mức bền (ADR-GEO-11): open, hint, ask, unlock = mức B (bền, D-GEO-126, 162); tool, scene_shown = mức A (D-GEO-165).
CREATE TYPE qa_reply_kind     AS ENUM ('hint', 'concept', 'refused_answer', 'out_of_curriculum', 'explain_step');
CREATE TYPE lecture_source    AS ENUM ('teacher_authored', 'ai_written'); -- PO-34, PO-43
CREATE TYPE share_resource    AS ENUM ('problem', 'lecture');           -- mở rộng bằng ALTER TYPE ... ADD VALUE
CREATE TYPE share_status      AS ENUM ('requested', 'approved', 'rejected', 'revoked'); -- hết hạn = suy ra từ valid_until
CREATE TYPE audit_action      AS ENUM ('release_set', 'release_change', 'release_now', 'assignment_create',
                                       'verify_label_set', 'access_code_issue', 'access_code_revoke');
CREATE TYPE ai_purpose        AS ENUM ('scene', 'solution', 'hint', 'qa', 'lecture');
CREATE TYPE ai_status         AS ENUM ('ok', 'error', 'timeout', 'blocked');
CREATE TYPE guard_channel     AS ENUM ('qa', 'ai_hint');                -- ADR-GEO-14: kênh có câu chữ do AI sinh
CREATE TYPE guard_outcome     AS ENUM ('blocked', 'error_closed');      -- ADR-GEO-14: chặn theo quy tắc | bộ kiểm lỗi => đóng
CREATE TYPE probe_outcome     AS ENUM ('blocked', 'refused', 'leaked'); -- kết quả một câu thử của bộ câu thử (D-GEO-103)

-- ------------------------------------------------------------------ hàm tiện ích
-- Bọc unaccent thành IMMUTABLE để dùng trong cột sinh và chỉ mục (unaccent gốc chỉ STABLE).
CREATE FUNCTION f_unaccent(text) RETURNS text
  LANGUAGE sql IMMUTABLE PARALLEL SAFE STRICT
  AS $$ SELECT public.unaccent('public.unaccent'::regdictionary, $1) $$;

-- Định danh hàng tổ chức nền tảng: hằng số của dữ liệu tham chiếu (mục 11). Dùng trong CHECK và trong hàm, một chỗ duy nhất.
CREATE FUNCTION platform_org_id() RETURNS uuid
  LANGUAGE sql IMMUTABLE PARALLEL SAFE
  AS $$ SELECT '00000000-0000-7000-8000-00000000f001'::uuid $$;

-- UUIDv7 chỉ để sinh dữ liệu thử/kiểm thử. Mã Java sinh UUIDv7 phía ứng dụng (ADR-GEO-06).
CREATE FUNCTION uuid_v7(p_ts timestamptz DEFAULT clock_timestamp()) RETURNS uuid
  LANGUAGE sql VOLATILE PARALLEL SAFE
  AS $$
    SELECT encode(
      set_bit(set_bit(
        overlay(uuid_send(gen_random_uuid())
                PLACING substring(int8send((extract(epoch FROM p_ts) * 1000)::bigint) FROM 3)
                FROM 1 FOR 6),
        52, 1), 53, 1), 'hex')::uuid
  $$;

CREATE FUNCTION tg_immutable_cols() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE c text; o jsonb := to_jsonb(OLD); n jsonb := to_jsonb(NEW);
BEGIN
  FOREACH c IN ARRAY TG_ARGV LOOP
    IF o -> c IS DISTINCT FROM n -> c THEN
      RAISE EXCEPTION 'cột % của %.% là bất biến (BR-11/BR-22): tạo version mới', c, TG_TABLE_SCHEMA, TG_TABLE_NAME
        USING ERRCODE = 'integrity_constraint_violation';
    END IF;
  END LOOP;
  RETURN NEW;
END $$;

CREATE FUNCTION tg_append_only() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
  RAISE EXCEPTION '%.% chỉ được thêm, không sửa/xóa', TG_TABLE_SCHEMA, TG_TABLE_NAME
    USING ERRCODE = 'integrity_constraint_violation';
END $$;

-- Chặn ghi nội dung nền tảng (org_id = platform) bởi vai không phải geo_content (D-GEO-158, RV-10 (2)).
-- Chạy với quyền người gọi: trong hàm SECURITY DEFINER (chủ là superuser) thì cho qua vì hàm tự kiểm.
CREATE FUNCTION tg_platform_write_guard() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
  IF NEW.org_id = platform_org_id()
     AND NOT pg_has_role(current_user, 'geo_content', 'MEMBER')
     AND NOT COALESCE((SELECT r.rolsuper FROM pg_catalog.pg_roles r WHERE r.rolname = current_user), false) THEN
    RAISE EXCEPTION 'chỉ vai nạp nội dung (geo_content) ghi được nội dung nền tảng ở %.% (D-GEO-158)', TG_TABLE_SCHEMA, TG_TABLE_NAME
      USING ERRCODE = 'insufficient_privilege';
  END IF;
  RETURN NEW;
END $$;

-- =====================================================================================
-- 1. TỔ CHỨC, DANH MỤC DÙNG CHUNG, NGƯỜI DÙNG  [02_multi_tenant.md §1]
-- =====================================================================================
CREATE TABLE org (
  id         uuid PRIMARY KEY,
  parent_id  uuid REFERENCES org (id),
  kind       org_kind NOT NULL,
  name       text NOT NULL CHECK (length(name) BETWEEN 1 AND 120),
  created_at timestamptz NOT NULL DEFAULT now(),
  CONSTRAINT org_tree_ck CHECK ((kind = 'platform') = (parent_id IS NULL)),
  CONSTRAINT org_platform_id_ck CHECK ((kind = 'platform') = (id = platform_org_id()))
);
COMMENT ON TABLE org IS 'Cây tổ chức. Một hàng platform duy nhất (id = platform_org_id()) sở hữu nội dung dùng chung (kho đề, bài giảng mẫu). Bảng nhỏ, đọc được ở bộ nhớ đệm tiến trình.';
CREATE UNIQUE INDEX org_single_platform_uq ON org ((true)) WHERE kind = 'platform';  -- ràng buộc toàn vẹn, không phục vụ truy vấn đọc
-- Không có chỉ mục theo parent_id: MVP không có truy vấn đi theo cây (02_multi_tenant.md §1).

CREATE TABLE subject (
  id   smallint PRIMARY KEY,
  code text NOT NULL UNIQUE CHECK (code ~ '^[a-z_]{2,30}$'),
  name text NOT NULL
);
COMMENT ON TABLE subject IS 'Môn học; mỗi môn là một module (DOMAIN-PROFILE.md §10). MVP: hình học không gian. Dữ liệu dùng chung, không có org_id.';

CREATE TABLE problem_topic (
  id         smallint PRIMARY KEY,
  subject_id smallint NOT NULL REFERENCES subject (id),
  slug       text NOT NULL CHECK (slug ~ '^[a-z0-9_]{2,40}$'),
  name       text NOT NULL,
  UNIQUE (subject_id, slug)
);
COMMENT ON TABLE problem_topic IS 'Dạng bài = chủ đề theo D-GEO-070 nên là bảng tra thay vì enum. Dùng để lọc kho đề (Q13).';

-- D-GEO-133: danh mục hình mẫu (D-GEO-056: chóp, lăng trụ, hộp gồm lập phương, nón, trụ, cầu). Dùng chung, không org_id.
CREATE TABLE solid_template (
  id             smallint PRIMARY KEY,
  code           text NOT NULL UNIQUE CHECK (code ~ '^[a-z0-9_]{2,40}$'),
  solid_kind     solid_kind NOT NULL,
  name           text NOT NULL CHECK (length(name) BETWEEN 1 AND 120),
  schema_version smallint NOT NULL DEFAULT 1,
  param_spec     jsonb NOT NULL     -- tên tham số, giá trị mặc định (D-GEO-109, US-GEO-205), nhãn đỉnh chuẩn dùng cho chữ ký; đọc nguyên khối
);
COMMENT ON TABLE solid_template IS 'Danh mục hình mẫu (D-GEO-133). Màn khám phá hình mẫu (US-GEO-205) và chữ ký chuẩn của đề (problem_signature, D-GEO-150) đều trỏ về đây. Nội dung do geo_content nạp; lược đồ không cố định danh sách.';

CREATE TABLE app_user (
  id            uuid PRIMARY KEY,
  org_id        uuid NOT NULL REFERENCES org (id),
  role          user_role NOT NULL,
  account_kind  account_kind NOT NULL DEFAULT 'light',
  display_name  text NOT NULL CHECK (length(display_name) BETWEEN 1 AND 60),
  created_at    timestamptz NOT NULL DEFAULT now(),
  deactivated_at timestamptz,
  UNIQUE (org_id, id),          -- đích của khóa ngoại tổ hợp (bảo đảm cùng trường)
  CONSTRAINT app_user_kind_ck CHECK (
       (account_kind = 'light'  AND role = 'student')
    OR (account_kind = 'sample' AND role = 'teacher' AND org_id <> platform_org_id())
    OR (account_kind = 'system' AND role = 'teacher' AND org_id =  platform_org_id())
    OR (account_kind = 'verified'))
);
COMMENT ON TABLE app_user IS 'Người dùng thuộc đúng một trường; tác giả nền tảng (account_kind=system) thuộc hàng platform (D-GEO-160). MVP không lưu email/số điện thoại. geo_app KHÔNG có INSERT: học sinh tạo qua fn_student_login (chỉ student/light), giáo viên mẫu qua fn_create_sample_teacher (vai geo_ops).';

-- D-GEO-152: mã Giáo viên mẫu theo TỪNG tài khoản, lưu băm, thu hồi được, có hạn dùng.
-- Mã đưa cho giáo viên: 12 ký tự base32 ngẫu nhiên (≈ 60 bit, ví dụ dữ liệu mẫu F-GEO-003), gõ vào MỘT ô. 4 ký tự đầu (selector)
-- chỉ dùng để tra hàng (duy nhất, sinh lại khi trùng); cả mã vẫn bí mật nên độ khó đoán giữ 60 bit. CSDL chỉ lưu
-- HMAC-SHA256(pepper, toàn bộ mã) với khóa bí mật (pepper) ở cấu hình ứng dụng, không ở CSDL (ADR-GEO-07). Không lưu mã rõ.
CREATE TABLE teacher_access_code (
  id             uuid PRIMARY KEY,
  org_id         uuid NOT NULL,
  user_id        uuid NOT NULL,
  selector       text NOT NULL CHECK (selector ~ '^[A-Z2-7]{4}$'),   -- 4 ký tự đầu của mã
  verifier_hmac  bytea NOT NULL CHECK (length(verifier_hmac) = 32),     -- HMAC-SHA256(pepper[pepper_kid], toàn bộ mã đã viết hoa)
  pepper_kid     smallint NOT NULL,                 -- mã khóa pepper để xoay khóa
  issued_at      timestamptz NOT NULL DEFAULT now(),
  expires_at     timestamptz NOT NULL,
  revoked_at     timestamptz,
  last_used_at   timestamptz,
  FOREIGN KEY (org_id, user_id) REFERENCES app_user (org_id, id),
  CONSTRAINT teacher_code_window_ck CHECK (expires_at > issued_at)
);
COMMENT ON TABLE teacher_access_code IS 'Mã truy cập Giáo viên mẫu (D-GEO-073, D-GEO-152, RV-02). Chỉ đọc qua fn_teacher_login; cấp/thu hồi qua hàm của geo_ops. geo_app không có quyền nào trên bảng.';
-- Q24 đăng nhập giáo viên: tra theo selector. NGOẠI LỆ org_id đứng đầu: trước đăng nhập chưa biết trường (như mã lớp, D-GEO-051).
CREATE UNIQUE INDEX teacher_access_code_selector_uq ON teacher_access_code (selector);

-- D-GEO-152: đếm lần thử sai mã giáo viên theo IP và theo tài khoản (cửa sổ 15 phút). Không thuộc trường (trước đăng nhập).
CREATE TABLE auth_failure (
  id      bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  at      timestamptz NOT NULL DEFAULT now(),
  ip_key  bytea NOT NULL CHECK (length(ip_key) = 32),   -- HMAC của địa chỉ IP (không lưu IP rõ)
  user_id uuid                                         -- tài khoản khi selector khớp; NULL khi selector sai
);
COMMENT ON TABLE auth_failure IS 'Lần đăng nhập giáo viên thất bại (D-GEO-152). Bền ở CSDL để giới hạn đúng trên mọi nút và cả khi Redis tắt (đóng khi lỗi). Dọn hàng cũ hơn 1 ngày bằng fn_purge_ephemeral (geo_ops). NGOẠI LỆ T1: không có org_id vì xảy ra trước khi biết trường.';
-- Q24 đếm thử sai theo IP trong 15 phút
CREATE INDEX auth_failure_ip_idx ON auth_failure (ip_key, at);
-- Q24 đếm thử sai theo tài khoản trong 15 phút
CREATE INDEX auth_failure_user_idx ON auth_failure (user_id, at) WHERE user_id IS NOT NULL;

-- =====================================================================================
-- 2. LỚP VÀ THÀNH VIÊN LỚP
-- =====================================================================================
CREATE TABLE class (
  id         uuid PRIMARY KEY,
  org_id     uuid NOT NULL REFERENCES org (id),
  name       text NOT NULL CHECK (length(name) BETWEEN 1 AND 60),
  level      edu_level NOT NULL,
  grade      smallint NOT NULL CHECK (grade BETWEEN 6 AND 12),
  join_code  text NOT NULL CHECK (join_code ~ '^[A-Z2-9]{6}$'),
  teacher_id uuid NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now(),
  archived_at timestamptz,
  UNIQUE (org_id, id),
  FOREIGN KEY (org_id, teacher_id) REFERENCES app_user (org_id, id)
);
COMMENT ON TABLE class IS 'Lớp. Mã lớp là toàn cục (đăng nhập nhẹ chưa biết trường trước khi tra mã lớp).';
-- Q01 đăng nhập: tra lớp theo mã. NGOẠI LỆ org_id đứng đầu: chưa biết org_id, mã lớp là khóa tra cứu toàn cục (D-GEO-051).
CREATE UNIQUE INDEX class_join_code_uq ON class (join_code);
-- Q17 danh sách lớp của giáo viên; Q22 bài đã giao của giáo viên (đi qua các lớp của mình)
CREATE INDEX class_teacher_idx ON class (org_id, teacher_id, id);

CREATE TABLE class_member (
  org_id    uuid NOT NULL,
  class_id  uuid NOT NULL,
  user_id   uuid NOT NULL,
  name_key  text NOT NULL CHECK (name_key = lower(name_key) AND length(name_key) BETWEEN 1 AND 60),
  joined_at timestamptz NOT NULL DEFAULT now(),
  left_at   timestamptz,
  PRIMARY KEY (org_id, class_id, user_id),               -- EXISTS tư cách lớp trong Q04, Q05, Q06, Q07 và các hàm cổng
  FOREIGN KEY (org_id, class_id) REFERENCES class (org_id, id),
  FOREIGN KEY (org_id, user_id)  REFERENCES app_user (org_id, id)
);
COMMENT ON TABLE class_member IS 'Thành viên lớp. name_key = tên hiển thị đã chuẩn hóa (không dấu, chữ thường) do ứng dụng tính, duy nhất trong lớp (D-GEO-106: cùng tên là cùng một học sinh). Giả mạo tên bạn cùng lớp là rủi ro đã biết (D-GEO-149). geo_app không INSERT: tạo qua fn_student_login.';
-- Q01 đăng nhập theo tên chuẩn hóa; Q09 theo dõi lớp sắp theo tên A → Z, keyset (name_key, user_id) (D-GEO-121, 167)
CREATE UNIQUE INDEX class_member_name_uq ON class_member (org_id, class_id, name_key) INCLUDE (user_id) WHERE left_at IS NULL;
-- Q02/Q04 lớp của một học sinh (lọc bài theo lớp); Q16 các lớp em đang thuộc
CREATE INDEX class_member_user_idx ON class_member (org_id, user_id, class_id) WHERE left_at IS NULL;

-- =====================================================================================
-- 3. NỘI DUNG: ĐỀ (bất biến theo phiên bản), HÌNH, GỢI Ý, CHỮ KÝ  |  LỜI GIẢI Ở BẢNG RIÊNG
-- org_id của nội dung dùng chung = hàng platform; của đề học sinh tự nhập = trường của em.
-- =====================================================================================
CREATE TABLE problem (
  id                 uuid PRIMARY KEY,
  org_id             uuid NOT NULL REFERENCES org (id),
  subject_id         smallint NOT NULL REFERENCES subject (id),
  source             problem_source NOT NULL,
  owner_user_id      uuid,                                  -- chỉ đề tự nhập: học sinh chủ đề
  author_user_id     uuid,                                  -- đề kho: tác giả nền tảng đứng tên (D-GEO-160, BR-10)
  level              edu_level NOT NULL,
  solid_kind         solid_kind NOT NULL,
  topic_id           smallint REFERENCES problem_topic (id),
  title              text NOT NULL CHECK (length(title) BETWEEN 1 AND 200),
  current_version_id uuid,                                  -- FK thêm sau (vòng tham chiếu)
  created_at         timestamptz NOT NULL DEFAULT now(),
  archived_at        timestamptz,
  UNIQUE (org_id, id),
  UNIQUE (org_id, id, owner_user_id),                       -- đích khóa ngoại "bài tự học dùng đề của chính em"
  FOREIGN KEY (org_id, owner_user_id)  REFERENCES app_user (org_id, id),
  FOREIGN KEY (org_id, author_user_id) REFERENCES app_user (org_id, id),
  CONSTRAINT problem_owner_ck  CHECK ((source = 'student_input') = (owner_user_id IS NOT NULL)),
  CONSTRAINT problem_scope_ck  CHECK ((source = 'catalog') = (org_id = platform_org_id())),   -- MVP: kho = nền tảng; đề tự nhập = trường
  CONSTRAINT problem_author_ck CHECK (source <> 'catalog' OR author_user_id IS NOT NULL),     -- D-GEO-160
  CONSTRAINT problem_topic_ck  CHECK (source <> 'catalog' OR topic_id IS NOT NULL)            -- RV-31: con trỏ Q13 không gặp NULL
);
COMMENT ON TABLE problem IS 'Đề bài (định danh ổn định). Nội dung nằm ở problem_version (bất biến). Kho đề: source=catalog, org_id=platform, đứng tên tác giả nền tảng (D-GEO-160). Chặng 2 (đề của giáo viên thật thuộc trường) sẽ nới problem_scope_ck.';
-- Q13 kho đề có lọc cấp/loại hình/dạng bài, phân trang theo khóa. Chỉ mục một phần cho kho đề đang dùng.
CREATE INDEX problem_catalog_filter_idx ON problem (org_id, level, solid_kind, topic_id, id)
  WHERE source = 'catalog' AND archived_at IS NULL;
CREATE TRIGGER problem_platform_guard BEFORE INSERT OR UPDATE ON problem
  FOR EACH ROW EXECUTE FUNCTION tg_platform_write_guard();

CREATE TABLE problem_version (
  id             uuid PRIMARY KEY,
  org_id         uuid NOT NULL,
  problem_id     uuid NOT NULL,
  version        integer NOT NULL CHECK (version >= 1),
  statement      text NOT NULL CHECK (length(statement) BETWEEN 1 AND 8000),
  statement_hash bytea NOT NULL CHECK (length(statement_hash) = 32),   -- sha256 của đề đã chuẩn hóa: phép khớp PHỤ của tự học (D-GEO-150)
  verify_status  verify_status NOT NULL DEFAULT 'needs_review',
  verification_report_id uuid,                                         -- báo cáo đã quyết nhãn máy (D-GEO-158); FK thêm sau
  approval_status approval_status NOT NULL DEFAULT 'ai_drafted',
  ai_authored    boolean NOT NULL DEFAULT true,
  created_by     uuid,
  created_at     timestamptz NOT NULL DEFAULT now(),
  UNIQUE (org_id, id),
  UNIQUE (problem_id, version),                                        -- ràng buộc toàn vẹn (không trùng số version)
  UNIQUE (org_id, problem_id, id),                                     -- đích khóa ngoại tổ hợp assignment -> (nội dung, đề, version)
  FOREIGN KEY (org_id, problem_id) REFERENCES problem (org_id, id),
  FOREIGN KEY (org_id, created_by) REFERENCES app_user (org_id, id),
  CONSTRAINT pv_verified_needs_report_ck CHECK (verify_status <> 'verified' OR verification_report_id IS NOT NULL)
);
COMMENT ON TABLE problem_version IS 'Phiên bản đề. statement, statement_hash, version, problem_id bất biến (trigger). Nhãn máy verify_status chỉ thành verified khi trỏ tới báo cáo kiểm chứng của CHÍNH version này có result=verified (trigger tg_pv_verify_guard, D-GEO-158); geo_app không UPDATE cột nhãn.';
CREATE TRIGGER problem_version_immutable BEFORE UPDATE ON problem_version
  FOR EACH ROW EXECUTE FUNCTION tg_immutable_cols('problem_id', 'version', 'statement', 'statement_hash', 'org_id');
CREATE TRIGGER problem_version_platform_guard BEFORE INSERT OR UPDATE ON problem_version
  FOR EACH ROW EXECUTE FUNCTION tg_platform_write_guard();
-- Q16 phép khớp phụ của tự học: đề kho (org_id = platform) có cùng băm đề chuẩn hóa. org_id đứng đầu (hằng nền tảng).
CREATE INDEX problem_version_hash_idx ON problem_version (org_id, statement_hash);

ALTER TABLE problem ADD CONSTRAINT problem_current_version_fk
  FOREIGN KEY (current_version_id) REFERENCES problem_version (id) DEFERRABLE INITIALLY DEFERRED;

-- D-GEO-150: chữ ký chuẩn của đề = (hình mẫu, bộ tham số đã quy về a = 1, đại lượng đề hỏi), không phụ thuộc tên điểm,
-- cách viết, chữ thêm. Engine tính từ bản dựng hình (scene_build) sau khi đề đã được dựng; đề không dựng được hình
-- thì KHÔNG có hàng ở đây (và không có lời giải tự học). Thuật toán chuẩn hóa có phiên bản (sig_version): chỉ so khớp
-- chữ ký cùng phiên bản. Chi tiết thuật toán: 01_adr.md ADR-GEO-15.
CREATE TABLE problem_signature (
  org_id             uuid NOT NULL,
  problem_version_id uuid NOT NULL,
  sig_version        smallint NOT NULL CHECK (sig_version >= 1),
  template_id        smallint NOT NULL REFERENCES solid_template (id),
  params_norm        jsonb NOT NULL,            -- tham số đã chia cho a và làm tròn theo ADR-GEO-15; đọc nguyên khối
  asked_quantity     text NOT NULL CHECK (length(asked_quantity) BETWEEN 1 AND 200),  -- biểu thức theo nhãn đỉnh chuẩn của hình mẫu
  signature          bytea NOT NULL CHECK (length(signature) = 32),                   -- sha256(sig_version|template|params_norm|asked_quantity)
  schema_version     smallint NOT NULL DEFAULT 1,
  computed_at        timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (org_id, problem_version_id, sig_version),
  FOREIGN KEY (org_id, problem_version_id) REFERENCES problem_version (org_id, id)
);
COMMENT ON TABLE problem_signature IS 'Chữ ký chuẩn của version đề (D-GEO-150, D-GEO-133). Không chứa đáp số. Phép khớp tự học ↔ đề kho của bài giao đang khóa: Q16 (fn_self_lock_source).';
-- Q16 tìm đề kho có cùng chữ ký (org_id = platform đứng đầu)
CREATE INDEX problem_signature_lookup_idx ON problem_signature (org_id, signature, sig_version);
CREATE TRIGGER problem_signature_platform_guard BEFORE INSERT OR UPDATE ON problem_signature
  FOR EACH ROW EXECUTE FUNCTION tg_platform_write_guard();

CREATE TABLE scene_build (
  id                 uuid PRIMARY KEY,
  org_id             uuid NOT NULL,
  problem_version_id uuid NOT NULL,
  build_no           smallint NOT NULL CHECK (build_no >= 1),
  coord_source       coord_source NOT NULL,      -- 'ai' => hiện cảnh báo chưa kiểm chứng (BR-08)
  schema_version     smallint NOT NULL DEFAULT 1,
  spec               jsonb NOT NULL,             -- ràng buộc mô tả hình (SceneSpec), chỉ đọc nguyên khối
  coords             jsonb NOT NULL,             -- tọa độ đã tính: dữ kiện của đề, không phải đáp số (D-GEO-154)
  hidden_measures    text[] NOT NULL DEFAULT '{}' CHECK (cardinality(hidden_measures) <= 20),
    -- D-GEO-024, D-GEO-133: đại lượng mà công cụ đo phải ẩn giá trị trước giờ mở (biểu thức theo nhãn đỉnh của đề, không có giá trị)
  created_at         timestamptz NOT NULL DEFAULT now(),
  UNIQUE (org_id, problem_version_id, build_no),  -- Q04 lấy bản dựng mới nhất của version (ORDER BY build_no DESC LIMIT 1)
  FOREIGN KEY (org_id, problem_version_id) REFERENCES problem_version (org_id, id)
);
COMMENT ON TABLE scene_build IS 'Bản dựng hình của một version đề (SceneSpec). spec/coords là JSON chỉ đọc nguyên khối. hidden_measures chỉ là danh sách biểu thức (không giá trị); D-GEO-154: M-GEO-06 đếm "lộ" khi hệ thống hiển thị giá trị đề hỏi.';
CREATE TRIGGER scene_build_platform_guard BEFORE INSERT OR UPDATE ON scene_build
  FOR EACH ROW EXECUTE FUNCTION tg_platform_write_guard();

CREATE TABLE hint (
  org_id             uuid NOT NULL,
  problem_version_id uuid NOT NULL,
  level              smallint NOT NULL CHECK (level IN (1, 2)),   -- MVP hai mức (PO-39); mức 3 là lời giải
  body               text NOT NULL CHECK (length(body) BETWEEN 1 AND 4000),
  PRIMARY KEY (org_id, problem_version_id, level),                -- Q05 lấy gợi ý theo mức
  FOREIGN KEY (org_id, problem_version_id) REFERENCES problem_version (org_id, id)
);
COMMENT ON TABLE hint IS 'Gợi ý mức 1, 2. Gợi ý đề kho do Giáo viên mẫu soạn; gợi ý đề tự nhập do AI sinh và PHẢI qua Kiểm soát đáp án (ADR-GEO-14, kênh ai_hint) trước khi ghi. Bài tự học kế thừa khóa dùng gợi ý của đề kho, không dùng gợi ý AI (D-GEO-155, fn_get_hint). Học sinh chỉ nhận gợi ý qua fn_get_hint.';
CREATE TRIGGER hint_platform_guard BEFORE INSERT OR UPDATE ON hint
  FOR EACH ROW EXECUTE FUNCTION tg_platform_write_guard();

CREATE TABLE verification_report (
  id                 uuid PRIMARY KEY,
  org_id             uuid NOT NULL,
  problem_version_id uuid NOT NULL,
  checked_at         timestamptz NOT NULL DEFAULT now(),
  scene_ok           boolean NOT NULL,
  answer_ok          boolean NOT NULL,
  result             verify_status NOT NULL,
  schema_version     smallint NOT NULL DEFAULT 1,
  checks             jsonb NOT NULL,             -- từng phép kiểm, chứa giá trị tính (lộ đáp số) => bảng nhạy cảm
  UNIQUE (org_id, id),
  FOREIGN KEY (org_id, problem_version_id) REFERENCES problem_version (org_id, id),
  CONSTRAINT verification_label_ck CHECK (result <> 'verified' OR (scene_ok AND answer_ok))   -- BR-07
);
COMMENT ON TABLE verification_report IS 'Báo cáo kiểm chứng (BR-07). Chứa giá trị tính nên coi là nhạy cảm như lời giải; geo_app bị REVOKE. Nhãn máy của version chỉ đổi qua fn_set_verify_status kèm id báo cáo (D-GEO-158).';
-- Không có chỉ mục phụ: chỉ đọc theo khóa chính (trigger nhãn) và theo lô khi đo bộ đề chuẩn.
CREATE TRIGGER verification_report_platform_guard BEFORE INSERT OR UPDATE ON verification_report
  FOR EACH ROW EXECUTE FUNCTION tg_platform_write_guard();

ALTER TABLE problem_version ADD CONSTRAINT problem_version_report_fk
  FOREIGN KEY (org_id, verification_report_id) REFERENCES verification_report (org_id, id);

-- Nhãn "Đã kiểm chứng" cần báo cáo của CHÍNH version này với result = verified (BR-07, D-GEO-004, D-GEO-158).
-- Khi INSERT version mới chưa thể có báo cáo (báo cáo trỏ về version) nên mọi version sinh ra là needs_review.
CREATE FUNCTION tg_pv_verify_guard() RETURNS trigger LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp AS $$
BEGIN
  IF NEW.verification_report_id IS NOT NULL AND NOT EXISTS (
       SELECT 1 FROM verification_report r
       WHERE r.org_id = NEW.org_id AND r.id = NEW.verification_report_id AND r.problem_version_id = NEW.id) THEN
    RAISE EXCEPTION 'verification_report_id không thuộc version đề này' USING ERRCODE = 'check_violation';
  END IF;
  IF NEW.verify_status = 'verified' AND NOT EXISTS (
       SELECT 1 FROM verification_report r
       WHERE r.org_id = NEW.org_id AND r.id = NEW.verification_report_id AND r.problem_version_id = NEW.id AND r.result = 'verified') THEN
    RAISE EXCEPTION 'nhãn "Đã kiểm chứng" cần báo cáo kiểm chứng verified của chính version (BR-07, D-GEO-158)' USING ERRCODE = 'check_violation';
  END IF;
  RETURN NEW;
END $$;
CREATE TRIGGER problem_version_verify_guard BEFORE INSERT OR UPDATE OF verify_status, verification_report_id ON problem_version
  FOR EACH ROW EXECUTE FUNCTION tg_pv_verify_guard();

-- Bảng NHẠY CẢM: geo_app không có quyền nào (xem phần quyền cuối file).
CREATE TABLE solution (
  org_id             uuid NOT NULL,
  problem_version_id uuid NOT NULL,
  final_answer       text NOT NULL CHECK (length(final_answer) BETWEEN 1 AND 500),
  steps              jsonb NOT NULL,             -- các bước + chỉ dẫn vẽ thêm từng bước (PO-36); đọc nguyên khối
  schema_version     smallint NOT NULL DEFAULT 1,
  answer_forms       jsonb NOT NULL DEFAULT '[]', -- ADR-GEO-14: các dạng tương đương của đáp số (biểu thức chuẩn, giá trị số theo bộ tham số) do bộ kiểm chứng tính
  answer_forms_version smallint NOT NULL DEFAULT 1,
  model              text,
  prompt_version     text,
  author_user_id     uuid,                       -- lời giải kho đứng tên tác giả nền tảng (D-GEO-160)
  ai_authored        boolean NOT NULL DEFAULT true,      -- BR-09
  created_at         timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (org_id, problem_version_id),              -- Q06 lấy lời giải (chỉ qua hàm có kiểm giờ mở)
  FOREIGN KEY (org_id, problem_version_id) REFERENCES problem_version (org_id, id),
  FOREIGN KEY (org_id, author_user_id) REFERENCES app_user (org_id, id)
);
COMMENT ON TABLE solution IS 'LỜI GIẢI + ĐÁP SỐ + dạng tương đương, bảng riêng tách khỏi đề (brief §7). geo_app bị REVOKE; học sinh chỉ nhận qua fn_get_solution, giáo viên chỉ đề kho qua fn_get_solution_teacher (D-GEO-153); cổng AI (geo_ai) đọc để Kiểm soát đáp án. Sửa lời giải = version đề mới (BR-11).';
CREATE TRIGGER solution_immutable BEFORE UPDATE ON solution
  FOR EACH ROW EXECUTE FUNCTION tg_immutable_cols('problem_version_id', 'final_answer', 'steps', 'org_id');
CREATE TRIGGER solution_platform_guard BEFORE INSERT OR UPDATE ON solution
  FOR EACH ROW EXECUTE FUNCTION tg_platform_write_guard();

-- =====================================================================================
-- 4. BÀI GIAO, TIẾN ĐỘ
-- =====================================================================================
CREATE TABLE assignment (
  id                        uuid PRIMARY KEY,           -- UUIDv7: thứ tự id = thứ tự thời gian giao, dùng làm cursor
  org_id                    uuid NOT NULL,
  kind                      assignment_kind NOT NULL,
  class_id                  uuid,
  owner_user_id             uuid,                       -- chỉ bài tự học
  content_org_id            uuid NOT NULL,              -- tổ chức sở hữu đề: platform (bài lớp) hoặc chính trường (bài tự học)
  problem_id                uuid NOT NULL,              -- D-GEO-161: "cùng đề" = cùng problem
  problem_version_id        uuid NOT NULL,              -- version cố định khi giao (D-GEO-062)
  assigned_by               uuid NOT NULL,
  release_at                timestamptz,                -- NULL = chưa đặt (lời giải vẫn khóa, BR-03). Chỉ đổi qua fn_set_release.
  release_rev               integer NOT NULL DEFAULT 0, -- tăng mỗi lần đổi giờ mở: khóa vô hiệu hóa bộ nhớ đệm, khóa lạc quan
  allow_hint                boolean NOT NULL DEFAULT true,   -- LRN-02; MVP không đổi sau khi giao (D-GEO-114)
  allow_qa                  boolean NOT NULL DEFAULT true,
  lock_source_assignment_id uuid,                       -- DẤU VẾT: bài lớp khóa mà bài tự học khớp lúc tạo; quyết định thật kiểm lại mỗi lần (D-GEO-151)
  created_at                timestamptz NOT NULL DEFAULT now(),
  archived_at               timestamptz,
  UNIQUE (org_id, id),
  FOREIGN KEY (org_id, class_id)      REFERENCES class (org_id, id),
  FOREIGN KEY (org_id, owner_user_id) REFERENCES app_user (org_id, id),
  FOREIGN KEY (org_id, assigned_by)   REFERENCES app_user (org_id, id),
  -- RV-12: đề của bài giao trỏ đúng (tổ chức nội dung, đề, version); tổ chức nội dung chỉ là platform hoặc chính trường (CHECK dưới)
  FOREIGN KEY (content_org_id, problem_id, problem_version_id) REFERENCES problem_version (org_id, problem_id, id),
  -- bài tự học chỉ dùng đề tự nhập của chính em (MATCH SIMPLE: bỏ qua với bài lớp vì owner_user_id NULL)
  FOREIGN KEY (org_id, problem_id, owner_user_id) REFERENCES problem (org_id, id, owner_user_id),
  FOREIGN KEY (org_id, lock_source_assignment_id) REFERENCES assignment (org_id, id),          -- RV-12: nguồn khóa cùng trường
  CONSTRAINT assignment_kind_ck CHECK (
    (kind = 'class' AND class_id IS NOT NULL AND owner_user_id IS NULL AND lock_source_assignment_id IS NULL)
 OR (kind = 'self'  AND class_id IS NULL     AND owner_user_id IS NOT NULL AND release_at IS NULL)),
  CONSTRAINT assignment_content_ck CHECK (
    (kind = 'class' AND content_org_id = platform_org_id())      -- MVP: bài lớp chỉ giao đề kho (E5)
 OR (kind = 'self'  AND content_org_id = org_id)),
  CONSTRAINT assignment_release_ck CHECK (release_at IS NULL OR release_at >= created_at)  -- RV-10 (5)
);
COMMENT ON TABLE assignment IS 'Bài giao (PO-25: giao từng đề). release_at là giờ mở lời giải. geo_app KHÔNG INSERT/UPDATE: tạo qua fn_create_assignment (kiểm giáo viên của lớp, giờ mở ở tương lai, CONFLICT khi giao trùng, ghi audit_log) hoặc fn_create_self_assignment; đổi giờ qua fn_set_release.';
-- Q02 (bài của học sinh qua các lớp của em), Q03 (bài của lớp), Q22 (bài đã giao của giáo viên): theo lớp, mới nhất trước, keyset theo id
CREATE INDEX assignment_class_idx ON assignment (org_id, class_id, id DESC) WHERE kind = 'class';
-- Q02b danh sách bài tự học của học sinh (keyset theo id)
CREATE INDEX assignment_self_idx  ON assignment (org_id, owner_user_id, id DESC) WHERE kind = 'self';
-- D-GEO-161 (RV-16): cùng problem cho cùng lớp, bài chưa lưu trữ => CONFLICT; cũng phục vụ Q16 (bài lớp theo đề)
CREATE UNIQUE INDEX assignment_class_problem_uq ON assignment (org_id, class_id, problem_id) WHERE kind = 'class' AND archived_at IS NULL;

CREATE TABLE assignment_progress (
  org_id            uuid NOT NULL,
  assignment_id     uuid NOT NULL,
  user_id           uuid NOT NULL,
  first_opened_at   timestamptz,
  last_active_at    timestamptz NOT NULL,
  hint_level_max    smallint NOT NULL DEFAULT 0 CHECK (hint_level_max BETWEEN 0 AND 2),
  hint_view_count   integer NOT NULL DEFAULT 0 CHECK (hint_view_count >= 0),
  ask_count         integer NOT NULL DEFAULT 0 CHECK (ask_count >= 0),
  tool_count        integer NOT NULL DEFAULT 0 CHECK (tool_count >= 0),
  active_seconds    integer NOT NULL DEFAULT 0 CHECK (active_seconds >= 0),   -- khoảng nghỉ >= 30 phút 0 giây mở phiên mới (D-GEO-059, 144); M-GEO-23
  solution_viewed_at timestamptz,   -- thời điểm sự kiện 'unlock' đầu tiên
  updated_at        timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (org_id, assignment_id, user_id),          -- Q08 upsert, Q09 trang theo dõi lớp, Q02 nối tiến độ
  FOREIGN KEY (org_id, assignment_id) REFERENCES assignment (org_id, id),
  FOREIGN KEY (org_id, user_id)       REFERENCES app_user (org_id, id)
);
COMMENT ON TABLE assignment_progress IS 'Bộ đếm phi chuẩn hóa theo (bài, học sinh). geo_app KHÔNG ghi trực tiếp (RV-10 (4)): chỉ các hàm ghi sự kiện (fn_record_batch, fn_record_durable, fn_get_hint, fn_record_qa) cập nhật trong cùng giao dịch với sự kiện, qua _progress_apply. Tính lại được hoàn toàn từ event_log (đối soát).';
ALTER TABLE assignment_progress SET (fillfactor = 80);   -- HOT update: mọi cột đếm không nằm trong chỉ mục

-- =====================================================================================
-- 5. SỰ KIỆN (chia theo tháng), KHÓA LŨY ĐẲNG, HỎI ĐÁP, KIỂM SOÁT ĐÁP ÁN
-- =====================================================================================
CREATE TABLE event_log (
  id            bigint GENERATED ALWAYS AS IDENTITY,
  occurred_at   timestamptz NOT NULL,
  org_id        uuid NOT NULL,
  assignment_id uuid NOT NULL,
  user_id       uuid NOT NULL,
  kind          event_kind NOT NULL,
  arg           smallint,
    -- kind='hint': mức 1|2; kind='tool': mã công cụ 1 Chọn, 2 Điểm, 3 Đoạn, 4 Mặt phẳng, 5 Mở rộng, 6 Đo dài, 7 Đo góc (D-GEO-058);
    -- kind='scene_shown': thời lượng từ lúc gửi đề tới lúc hình hiện, đơn vị 100 ms (D-GEO-165, M-GEO-09); loại khác: NULL
  CONSTRAINT event_arg_ck CHECK (
       (kind = 'hint' AND arg IN (1, 2))
    OR (kind = 'tool' AND arg BETWEEN 1 AND 20)
    OR (kind = 'scene_shown' AND arg >= 0)
    OR (kind NOT IN ('hint', 'tool', 'scene_shown') AND arg IS NULL))
) PARTITION BY RANGE (occurred_at);
COMMENT ON TABLE event_log IS 'Nhật ký tương tác chỉ thêm (PRF-01), chia theo tháng. Cố ý KHÔNG khóa chính, KHÔNG khóa ngoại, KHÔNG JSON: ghi hàng loạt phải rẻ; một chỉ mục btree mỗi partition. geo_app KHÔNG INSERT trực tiếp: ghi qua fn_record_batch (mức A, có kiểm tư cách lớp, RV-26) và các hàm mức B. MVP không dọn partition (D-GEO-128).';
COMMENT ON COLUMN event_log.id IS 'Dùng làm cầu nối thứ tự trong cursor (occurred_at, id); không có chỉ mục riêng.';
-- Q20 dòng thời gian của một học sinh trong một bài (giáo viên bấm xem chi tiết), keyset (occurred_at, id)
CREATE INDEX event_log_timeline_idx ON event_log (org_id, assignment_id, user_id, occurred_at DESC, id DESC);
CREATE TABLE event_log_default PARTITION OF event_log DEFAULT;
COMMENT ON TABLE event_log_default IS 'Lưới an toàn khi quên tạo partition. Phải luôn rỗng (NFR-GEO-045).';

-- RV-18, D-GEO-162: khóa lũy đẳng cho sự kiện bền open, hint, unlock (ask dùng qa_turn.request_id). Gửi lại cùng request_id không đếm hai lần.
CREATE TABLE durable_request (
  org_id     uuid NOT NULL,
  request_id uuid NOT NULL,
  kind       event_kind NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (org_id, request_id)                       -- Q08 (chèn ON CONFLICT DO NOTHING trong hàm mức B)
);
COMMENT ON TABLE durable_request IS 'Khóa lũy đẳng request_id của sự kiện mức B (RV-18). Không phải dữ liệu sự kiện (D-GEO-128 không áp): fn_purge_ephemeral (geo_ops) xóa khóa cũ hơn 24 giờ; thiết bị không gửi lại quá 24 giờ.';

CREATE TABLE ai_call_log (
  id            bigint GENERATED ALWAYS AS IDENTITY,
  occurred_at   timestamptz NOT NULL,
  org_id        uuid NOT NULL,
  user_id       uuid,
  purpose       ai_purpose NOT NULL,
  model         text NOT NULL,
  prompt_version text NOT NULL,
  tokens_in     integer NOT NULL CHECK (tokens_in >= 0),
  tokens_out    integer NOT NULL CHECK (tokens_out >= 0),
  cost_micro_usd bigint NOT NULL CHECK (cost_micro_usd >= 0),
  latency_ms    integer NOT NULL CHECK (latency_ms >= 0),
  status        ai_status NOT NULL
) PARTITION BY RANGE (occurred_at);
COMMENT ON TABLE ai_call_log IS 'Chi phí từng lượt gọi AI (TRU-03). Chỉ thêm, chia theo tháng, không chỉ mục. Không chứa nội dung câu hỏi hay tên học sinh (D-GEO-067).';
CREATE TABLE ai_call_log_default PARTITION OF ai_call_log DEFAULT;

-- ADR-GEO-14 (D-GEO-156): mỗi lần Kiểm soát đáp án chặn hoặc đóng do lỗi ghi một dòng. Không lưu nội dung câu trả lời bị chặn
-- (tránh thành kho đáp số); chỉ mã quy tắc và phiên bản bộ kiểm.
CREATE TABLE answer_guard_log (
  id            bigint GENERATED ALWAYS AS IDENTITY,
  occurred_at   timestamptz NOT NULL,
  org_id        uuid NOT NULL,
  assignment_id uuid,
  channel       guard_channel NOT NULL,
  outcome       guard_outcome NOT NULL,
  rule_code     text NOT NULL CHECK (rule_code ~ '^(R[0-9]{1,2}[a-z]?|ERR_[A-Z_]{2,30})$'),
  guard_version text NOT NULL CHECK (length(guard_version) BETWEEN 1 AND 40),
  latency_ms    integer NOT NULL CHECK (latency_ms >= 0),
  probe_run_id  bigint                       -- khi chạy bộ câu thử (cổng phát hành M-GEO-06)
) PARTITION BY RANGE (occurred_at);
COMMENT ON TABLE answer_guard_log IS 'Nhật ký chặn của Kiểm soát đáp án (ADR-GEO-14). Chỉ thêm, chia theo tháng, không chỉ mục (báo cáo quét một partition, chạy nền). Không user_id, không nội dung (D-GEO-067).';
CREATE TABLE answer_guard_log_default PARTITION OF answer_guard_log DEFAULT;

CREATE TABLE qa_turn (
  id            bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  org_id        uuid NOT NULL,
  assignment_id uuid NOT NULL,
  user_id       uuid NOT NULL,
  request_id    uuid NOT NULL,                  -- lũy đẳng cho sự kiện 'ask' (RV-18)
  asked_at      timestamptz NOT NULL DEFAULT now(),
  question      text NOT NULL CHECK (length(question) BETWEEN 1 AND 1000),
  reply_kind    qa_reply_kind NOT NULL,
  reply_text    text NOT NULL CHECK (length(reply_text) BETWEEN 1 AND 4000),  -- câu trả lời ĐÃ qua Kiểm soát đáp án (D-GEO-159)
  guard_version text NOT NULL CHECK (length(guard_version) BETWEEN 1 AND 40),
  source_lecture_ids uuid[] NOT NULL DEFAULT '{}' CHECK (cardinality(source_lecture_ids) <= 5),
  FOREIGN KEY (org_id, assignment_id) REFERENCES assignment (org_id, id),
  FOREIGN KEY (org_id, user_id)       REFERENCES app_user (org_id, id),    -- RV-12
  UNIQUE (org_id, request_id)                                              -- lũy đẳng (fn_record_qa: ON CONFLICT DO NOTHING)
);
COMMENT ON TABLE qa_turn IS 'Câu hỏi của học sinh, câu trả lời đã qua Kiểm soát đáp án (D-GEO-159, US-GEO-409, 410) và nguồn đã dùng. Chỉ chủ câu hỏi đọc reply_text: geo_app không có SELECT cột này, đọc qua fn_qa_history (Q21). Lưu giữ theo D-GEO-128 (MVP chưa dọn). Ghi qua fn_record_qa (vai geo_ai).';
-- Q14 các câu hỏi gần nhất của cả lớp trong một bài, mới nhất trước, keyset theo id
CREATE INDEX qa_turn_assignment_idx ON qa_turn (org_id, assignment_id, id DESC);
-- Q21 lịch sử hỏi đáp của một học sinh trên mọi bài (US-GEO-410, RV-19)
CREATE INDEX qa_turn_user_idx ON qa_turn (org_id, user_id, id DESC);

-- D-GEO-133, D-GEO-103: bộ câu thử moi đáp án (F-GEO-210…229 của QC; gồm ca "giả làm giáo viên", D-GEO-152) và kết quả từng lượt chạy.
CREATE TABLE probe_prompt (
  code            text PRIMARY KEY CHECK (code ~ '^F-GEO-[0-9]{3}$'),
  technique_group text NOT NULL CHECK (length(technique_group) BETWEEN 1 AND 60),
  prompt          text NOT NULL CHECK (length(prompt) BETWEEN 1 AND 2000),
  target_org_id   uuid,
  target_problem_version_id uuid,                -- đề kho dùng làm bài đang khóa của câu thử
  active          boolean NOT NULL DEFAULT true,
  FOREIGN KEY (target_org_id, target_problem_version_id) REFERENCES problem_version (org_id, id)
);
COMMENT ON TABLE probe_prompt IS 'Bộ câu thử chính thức (D-GEO-103, D-GEO-133). Nội dung câu do QC soạn (review/QC_test_design.md), geo_content nạp. Dữ liệu kiểm thử, không thuộc trường.';
CREATE TABLE probe_run (
  id            bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  run_label     text NOT NULL CHECK (length(run_label) BETWEEN 1 AND 80),
  prompt_code   text NOT NULL REFERENCES probe_prompt (code),
  executed_at   timestamptz NOT NULL DEFAULT now(),
  model         text NOT NULL,
  prompt_version text NOT NULL,
  guard_version text NOT NULL,
  outcome       probe_outcome NOT NULL,
  rule_code     text
);
COMMENT ON TABLE probe_run IS 'Kết quả bộ câu thử theo lượt chạy: cổng phát hành MVP = 0 hàng outcome=leaked trong lượt (MVP §9, NFR-GEO-020).';
-- Q25 báo cáo cổng phát hành của một lượt chạy
CREATE INDEX probe_run_label_idx ON probe_run (run_label, prompt_code);

-- =====================================================================================
-- 6. BÀI GIẢNG VÀ TỪ KHÓA (tìm kiếm văn bản không dấu)
-- =====================================================================================
CREATE TABLE lecture (
  id              uuid PRIMARY KEY,
  org_id          uuid NOT NULL REFERENCES org (id),
  subject_id      smallint NOT NULL REFERENCES subject (id),
  slug            text NOT NULL CHECK (slug ~ '^[a-z0-9-]{1,80}$'),
  title           text NOT NULL CHECK (length(title) BETWEEN 1 AND 200),
  body            text NOT NULL CHECK (length(body) BETWEEN 1 AND 20000),
  source_kind     lecture_source NOT NULL,
  approval_status approval_status NOT NULL DEFAULT 'ai_drafted',
  source_ref      text,                                   -- "tên tài liệu, mục" (PO-34 ghi rõ nguồn)
  human_reviewed  boolean NOT NULL DEFAULT false,         -- false => nhãn "chưa có người soát" (D-GEO-137)
  created_at      timestamptz NOT NULL DEFAULT now(),
  search_tsv      tsvector GENERATED ALWAYS AS (
                    setweight(to_tsvector('simple', f_unaccent(lower(title))), 'A') ||
                    setweight(to_tsvector('simple', f_unaccent(lower(body))),  'B')) STORED,
  UNIQUE (org_id, id),
  UNIQUE (org_id, slug),
  CONSTRAINT lecture_source_ck CHECK (source_kind <> 'teacher_authored' OR source_ref IS NOT NULL)
);
COMMENT ON TABLE lecture IS 'Bài giảng khái niệm (PO-34, PO-43, PO-49). MVP: nội dung dùng chung của nền tảng, geo_content nạp (geo_app chỉ đọc). Bảy bài "Giáo án mẫu" lấy từ bản mẫu tương tác, human_reviewed=false (D-GEO-137).';
-- Q12 hỏi đáp tìm nguồn: GIN (org_id, tsvector) nhờ btree_gin (giữ theo D-GEO-166)
CREATE INDEX lecture_search_idx ON lecture USING gin (org_id, search_tsv);

CREATE TABLE keyword (
  org_id     uuid NOT NULL,
  term_key   text NOT NULL CHECK (term_key = lower(term_key) AND length(term_key) BETWEEN 1 AND 80),
  term       text NOT NULL,
  lecture_id uuid NOT NULL,
  PRIMARY KEY (org_id, term_key),                         -- Q11 bấm từ khóa -> bài giảng (khớp chính xác)
  FOREIGN KEY (org_id, lecture_id) REFERENCES lecture (org_id, id)
);
COMMENT ON TABLE keyword IS 'Từ khóa bấm được -> bài giảng (PO-34). term_key chuẩn hóa do ứng dụng tính. Ô tìm gần đúng (Q11b, Q11c) không thuộc MVP (D-GEO-166) nên không có chỉ mục trigram.';
-- Q11d từ khóa liên quan của một bài giảng: tra theo lecture_id, đã sắp theo term_key
CREATE INDEX keyword_lecture_idx ON keyword (org_id, lecture_id, term_key);

-- =====================================================================================
-- 7. CẤP PHÉP LIÊN TRƯỜNG (chừa chỗ cho chặng 7, MVP không có logic)
-- =====================================================================================
CREATE TABLE share_grant (
  id                  uuid PRIMARY KEY,
  org_id              uuid NOT NULL REFERENCES org (id),            -- trường SỞ HỮU tài nguyên (bên cấp)
  grantee_org_id      uuid NOT NULL REFERENCES org (id),            -- trường được cấp
  resource_kind       share_resource NOT NULL,
  resource_id         uuid NOT NULL,
  purpose             text NOT NULL CHECK (length(purpose) BETWEEN 5 AND 500),   -- BR-28: có mục đích
  status              share_status NOT NULL DEFAULT 'requested',
  requested_by        uuid NOT NULL,
  requested_at        timestamptz NOT NULL DEFAULT now(),
  grantor_approved_at timestamptz,
  grantee_approved_at timestamptz,
  valid_from          timestamptz,
  valid_until         timestamptz,                                  -- BR-28: có thời hạn
  revoked_at          timestamptz,
  revoked_by          uuid,
  FOREIGN KEY (org_id, requested_by) REFERENCES app_user (org_id, id),
  CONSTRAINT share_distinct_orgs_ck CHECK (org_id <> grantee_org_id),
  CONSTRAINT share_window_ck CHECK (valid_until IS NULL OR valid_from IS NULL OR valid_until > valid_from),
  CONSTRAINT share_approved_ck CHECK (status <> 'approved' OR
     (grantor_approved_at IS NOT NULL AND grantee_approved_at IS NOT NULL AND valid_from IS NOT NULL AND valid_until IS NOT NULL)),
  CONSTRAINT share_revoked_ck CHECK ((status = 'revoked') = (revoked_at IS NOT NULL))
);
COMMENT ON TABLE share_grant IS 'Cấp phép liên trường tường minh (02_multi_tenant.md §5). MVP không có hàng nào và không có quyền ghi của geo_app; bảng tồn tại để chặng 7 không đổi lược đồ.';
-- Q18 "trường X có được đọc tài nguyên R không". NGOẠI LỆ org_id đứng đầu: khóa tra là trường nhận.
CREATE INDEX share_grant_lookup_idx ON share_grant (grantee_org_id, resource_kind, resource_id) WHERE status = 'approved';

-- =====================================================================================
-- 8. NHẬT KÝ KIỂM TOÁN (thao tác nhạy cảm)
-- =====================================================================================
CREATE TABLE audit_log (
  id            bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  occurred_at   timestamptz NOT NULL DEFAULT now(),
  org_id        uuid NOT NULL,
  actor_user_id uuid NOT NULL,
  action        audit_action NOT NULL,
  target_id     uuid NOT NULL,           -- bài giao | version đề | mã truy cập
  before_release_at timestamptz,
  after_release_at  timestamptz,
  ref_id        uuid,                    -- báo cáo kiểm chứng (verify_label_set), tài khoản (access_code_*)
  request_id    uuid
);
COMMENT ON TABLE audit_log IS 'Dấu vết thao tác nhạy cảm: tạo bài giao có giờ mở, đặt/đổi giờ, mở ngay (BR-03, BR-GEO-240), đổi nhãn máy, cấp/thu hồi mã giáo viên. Chỉ thêm (trigger chặn UPDATE/DELETE); chỉ hàm SECURITY DEFINER ghi (geo_app không INSERT).';
CREATE TRIGGER audit_log_append_only BEFORE UPDATE OR DELETE ON audit_log
  FOR EACH ROW EXECUTE FUNCTION tg_append_only();
-- Q15 lịch sử đổi giờ mở của một bài, mới nhất trước
CREATE INDEX audit_log_target_idx ON audit_log (org_id, target_id, id DESC);

-- =====================================================================================
-- 9. HÀM NGHIỆP VỤ Ở TẦNG DỮ LIỆU (BR-01, BR-03, D-GEO-150…162)
-- Quy ước mã trạng thái trả về (ứng dụng ánh xạ, 04_query_catalogue.md §6):
--   OK · DUPLICATE (lặp lại lũy đẳng, trả 200 như OK) · NOT_FOUND (404) · LOCKED / FORBIDDEN / HINT_DISABLED / QA_DISABLED /
--   LEVEL_ORDER (403 FORBIDDEN) · CONFLICT_REV / CONFLICT_ALREADY_OPEN / CONFLICT_DUPLICATE (409 CONFLICT, D-GEO-143) ·
--   VALIDATION_PAST / VALIDATION_KIND (400 VALIDATION_FAILED) · RATE_LIMITED (429) · INVALID_CODE (401) · NO_SOLUTION, NO_HINT (404)
-- =====================================================================================

-- ---------------------------------------------------------------- 9.1 Đăng nhập (D-GEO-127: đường tiền xác thực qua hàm, chịu được RLS)
-- Q01: học sinh vào lớp bằng mã lớp + tên. Chưa có tên trong lớp thì tạo đúng một học sinh student/light (D-GEO-106, D-GEO-158).
CREATE FUNCTION fn_student_login(p_join_code text, p_name_key text, p_display_name text, p_new_user_id uuid)
RETURNS TABLE (status text, org_id uuid, class_id uuid, level edu_level, user_id uuid, created boolean)
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
#variable_conflict use_column
DECLARE c record; v_user uuid;
BEGIN
  SELECT x.id, x.org_id, x.level INTO c FROM class x WHERE x.join_code = p_join_code AND x.archived_at IS NULL;
  IF c.id IS NULL THEN
    RETURN QUERY SELECT 'NOT_FOUND'::text, NULL::uuid, NULL::uuid, NULL::edu_level, NULL::uuid, false; RETURN;
  END IF;
  SELECT cm.user_id INTO v_user FROM class_member cm
   WHERE cm.org_id = c.org_id AND cm.class_id = c.id AND cm.name_key = p_name_key AND cm.left_at IS NULL;
  IF v_user IS NULL AND p_new_user_id IS NOT NULL THEN
    -- hai thiết bị cùng tên cùng lúc: tuần tự hóa theo (lớp, tên) rồi kiểm lại
    PERFORM pg_advisory_xact_lock(hashtextextended(c.id::text || '|' || p_name_key, 0));
    SELECT cm.user_id INTO v_user FROM class_member cm
     WHERE cm.org_id = c.org_id AND cm.class_id = c.id AND cm.name_key = p_name_key AND cm.left_at IS NULL;
    IF v_user IS NULL THEN
      INSERT INTO app_user (id, org_id, role, account_kind, display_name) VALUES (p_new_user_id, c.org_id, 'student', 'light', p_display_name);
      INSERT INTO class_member (org_id, class_id, user_id, name_key) VALUES (c.org_id, c.id, p_new_user_id, p_name_key);
      RETURN QUERY SELECT 'OK'::text, c.org_id, c.id, c.level, p_new_user_id, true; RETURN;
    END IF;
  END IF;
  IF v_user IS NULL THEN
    RETURN QUERY SELECT 'NOT_FOUND'::text, c.org_id, c.id, c.level, NULL::uuid, false; RETURN;
  END IF;
  RETURN QUERY SELECT 'OK'::text, c.org_id, c.id, c.level, v_user, false;
END $$;
COMMENT ON FUNCTION fn_student_login IS 'Q01 + tạo học sinh. Đường DUY NHẤT tạo app_user/class_member từ ứng dụng; chỉ tạo role=student, account_kind=light (D-GEO-158). Không giới hạn thử sai riêng (D-GEO-107 giữ cho học sinh).';

-- Q24: Giáo viên mẫu đăng nhập bằng mã (D-GEO-152, 173). Ứng dụng lấy selector = 4 ký tự đầu, tính HMAC-SHA256(pepper[kid], mã)
-- và HMAC(pepper, IP); CSDL không thấy mã rõ hay IP rõ. Sau 10 lần sai trong 15 phút theo IP => RATE_LIMITED, kể cả khi lần này đúng
-- (D-GEO-173). Thêm (D-GEO-152 "hoặc theo tài khoản"): khi selector khớp một mã đã cấp, lần sai cũng tính vào tài khoản đó, chặn dò
-- phân tán qua nhiều IP. Ngưỡng cố định theo D-GEO-152 (không nhận tham số từ ứng dụng).
CREATE FUNCTION fn_teacher_login(p_selector text, p_verifier_hmac bytea, p_ip_key bytea)
RETURNS TABLE (status text, org_id uuid, user_id uuid)
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
#variable_conflict use_column
DECLARE k record; v_fail int;
BEGIN
  SELECT count(*) INTO v_fail FROM (SELECT 1 FROM auth_failure f
     WHERE f.ip_key = p_ip_key AND f.at > now() - interval '15 minutes' LIMIT 10) x;
  IF v_fail >= 10 THEN RETURN QUERY SELECT 'RATE_LIMITED'::text, NULL::uuid, NULL::uuid; RETURN; END IF;
  SELECT t.id, t.org_id, t.user_id, t.verifier_hmac, t.expires_at, t.revoked_at, u.deactivated_at, u.role, u.account_kind
    INTO k
    FROM teacher_access_code t JOIN app_user u ON u.org_id = t.org_id AND u.id = t.user_id
   WHERE t.selector = p_selector;
  IF k.id IS NOT NULL THEN
    SELECT count(*) INTO v_fail FROM (SELECT 1 FROM auth_failure f
       WHERE f.user_id = k.user_id AND f.at > now() - interval '15 minutes' LIMIT 10) x;
    IF v_fail >= 10 THEN RETURN QUERY SELECT 'RATE_LIMITED'::text, NULL::uuid, NULL::uuid; RETURN; END IF;
  END IF;
  IF k.id IS NULL OR k.verifier_hmac <> p_verifier_hmac OR k.revoked_at IS NOT NULL OR k.expires_at <= now()
     OR k.deactivated_at IS NOT NULL OR k.role <> 'teacher' OR k.account_kind <> 'sample' THEN
    INSERT INTO auth_failure (ip_key, user_id) VALUES (p_ip_key, k.user_id);
    RETURN QUERY SELECT 'INVALID_CODE'::text, NULL::uuid, NULL::uuid; RETURN;
  END IF;
  UPDATE teacher_access_code t SET last_used_at = now() WHERE t.id = k.id;
  RETURN QUERY SELECT 'OK'::text, k.org_id, k.user_id;
END $$;
COMMENT ON FUNCTION fn_teacher_login IS 'Đăng nhập Giáo viên mẫu (D-GEO-152, RV-02). Đếm thử sai bền ở CSDL (auth_failure) nên đúng trên mọi nút và khi Redis tắt. Ứng dụng không phân biệt "selector sai" với "bí mật sai" trong thông báo.';

-- ---------------------------------------------------------------- 9.2 Vận hành (geo_ops): giáo viên mẫu và mã
CREATE FUNCTION fn_create_sample_teacher(p_org uuid, p_user_id uuid, p_display_name text)
RETURNS text LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM org o WHERE o.id = p_org AND o.kind = 'school') THEN RETURN 'NOT_FOUND'; END IF;
  INSERT INTO app_user (id, org_id, role, account_kind, display_name) VALUES (p_user_id, p_org, 'teacher', 'sample', p_display_name);
  RETURN 'OK';
END $$;

CREATE FUNCTION fn_issue_teacher_code(p_org uuid, p_user uuid, p_code_id uuid, p_selector text, p_verifier_hmac bytea,
                                      p_pepper_kid smallint, p_expires_at timestamptz, p_actor uuid)
RETURNS text LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM app_user u WHERE u.org_id = p_org AND u.id = p_user AND u.role = 'teacher'
                 AND u.account_kind = 'sample' AND u.deactivated_at IS NULL) THEN RETURN 'NOT_FOUND'; END IF;
  IF p_expires_at <= now() THEN RETURN 'VALIDATION_PAST'; END IF;
  INSERT INTO teacher_access_code (id, org_id, user_id, selector, verifier_hmac, pepper_kid, expires_at)
  VALUES (p_code_id, p_org, p_user, p_selector, p_verifier_hmac, p_pepper_kid, p_expires_at);
  INSERT INTO audit_log (org_id, actor_user_id, action, target_id, ref_id) VALUES (p_org, p_actor, 'access_code_issue', p_code_id, p_user);
  RETURN 'OK';
END $$;

CREATE FUNCTION fn_revoke_teacher_code(p_code_id uuid, p_actor uuid)
RETURNS text LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
DECLARE v_org uuid; v_user uuid;
BEGIN
  UPDATE teacher_access_code t SET revoked_at = now() WHERE t.id = p_code_id AND t.revoked_at IS NULL RETURNING t.org_id, t.user_id INTO v_org, v_user;
  IF v_org IS NULL THEN RETURN 'NOT_FOUND'; END IF;
  INSERT INTO audit_log (org_id, actor_user_id, action, target_id, ref_id) VALUES (v_org, p_actor, 'access_code_revoke', p_code_id, v_user);
  RETURN 'OK';
END $$;

-- ---------------------------------------------------------------- 9.3 Nhãn máy (D-GEO-158 (3))
CREATE FUNCTION fn_set_verify_status(p_org uuid, p_problem_version uuid, p_report uuid, p_actor uuid)
RETURNS text LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
DECLARE v_result verify_status;
BEGIN
  SELECT r.result INTO v_result FROM verification_report r
   WHERE r.org_id = p_org AND r.id = p_report AND r.problem_version_id = p_problem_version;
  IF v_result IS NULL THEN RETURN 'NOT_FOUND'; END IF;
  UPDATE problem_version pv SET verify_status = v_result, verification_report_id = p_report
   WHERE pv.org_id = p_org AND pv.id = p_problem_version;
  INSERT INTO audit_log (org_id, actor_user_id, action, target_id, ref_id) VALUES (p_org, p_actor, 'verify_label_set', p_problem_version, p_report);
  RETURN 'OK';
END $$;
COMMENT ON FUNCTION fn_set_verify_status IS 'Đường DUY NHẤT đổi nhãn máy: nhãn = result của báo cáo kiểm chứng thuộc chính version (BR-07, D-GEO-004, D-GEO-158). Vai geo_ai (bộ kiểm chứng của tự học) và geo_content.';

-- ---------------------------------------------------------------- 9.4 Tự học: kế thừa khóa theo chữ ký chuẩn (D-GEO-150, 151)
-- Q16: bài giao lớp ĐANG KHÓA (chưa đặt giờ hoặc chưa tới giờ) của bất kỳ lớp nào học sinh đang thuộc, mà đề kho của nó có
-- cùng chữ ký chuẩn (cùng sig_version) với version đề tự nhập, hoặc (khớp phụ) cùng băm đề chuẩn hóa. Trả id bài lớp hoặc NULL.
CREATE FUNCTION fn_self_lock_source(p_org uuid, p_user uuid, p_self_pv uuid)
RETURNS uuid LANGUAGE plpgsql STABLE SECURITY DEFINER SET search_path = public, pg_temp
-- plpgsql (không phải sql): hàm sql SECURITY DEFINER không được nội tuyến và lập kế hoạch lại thân mỗi lần gọi (≈ 0,45 ms đo ở
-- Q05b bản đầu); plpgsql giữ kế hoạch đã chuẩn bị trong phiên.
AS $$
DECLARE v uuid;
BEGIN
  SELECT x.id INTO v FROM (
    SELECT a.id
    FROM problem_signature me
    JOIN problem_signature cat ON cat.org_id = platform_org_id() AND cat.signature = me.signature AND cat.sig_version = me.sig_version
    JOIN problem_version cpv   ON cpv.org_id = cat.org_id AND cpv.id = cat.problem_version_id
    JOIN class_member cm       ON cm.org_id = p_org AND cm.user_id = p_user AND cm.left_at IS NULL
    JOIN assignment a          ON a.org_id = cm.org_id AND a.class_id = cm.class_id AND a.problem_id = cpv.problem_id
                              AND a.kind = 'class' AND a.archived_at IS NULL
    WHERE me.org_id = p_org AND me.problem_version_id = p_self_pv
      AND (a.release_at IS NULL OR a.release_at > now())
    UNION ALL
    SELECT a.id
    FROM problem_version me
    JOIN problem_version cpv   ON cpv.org_id = platform_org_id() AND cpv.statement_hash = me.statement_hash
    JOIN class_member cm       ON cm.org_id = p_org AND cm.user_id = p_user AND cm.left_at IS NULL
    JOIN assignment a          ON a.org_id = cm.org_id AND a.class_id = cm.class_id AND a.problem_id = cpv.problem_id
                              AND a.kind = 'class' AND a.archived_at IS NULL
    WHERE me.org_id = p_org AND me.id = p_self_pv
      AND (a.release_at IS NULL OR a.release_at > now())
  ) x
  LIMIT 1;
  RETURN v;
END $$;
COMMENT ON FUNCTION fn_self_lock_source IS 'Phép kiểm kế thừa khóa của tự học (D-GEO-150, 151). Gọi ở MỖI lần xin gợi ý và lời giải của bài tự học, không chỉ lúc tạo (RV-03): dán trước, giao sau vẫn khóa.';

-- ---------------------------------------------------------------- 9.5 Bài giao (D-GEO-158 (5), D-GEO-161)
CREATE FUNCTION fn_create_assignment(p_org uuid, p_actor uuid, p_assignment_id uuid, p_class uuid, p_problem_version uuid,
                                     p_release_at timestamptz, p_allow_hint boolean, p_allow_qa boolean, p_request uuid DEFAULT NULL)
RETURNS TABLE (status text, release_rev integer)
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
#variable_conflict use_column
DECLARE c record; v_problem uuid; e record;
BEGIN
  SELECT x.teacher_id, x.archived_at INTO c FROM class x WHERE x.org_id = p_org AND x.id = p_class;
  IF c.teacher_id IS NULL OR c.archived_at IS NOT NULL THEN RETURN QUERY SELECT 'NOT_FOUND'::text, NULL::int; RETURN; END IF;
  IF c.teacher_id <> p_actor THEN RETURN QUERY SELECT 'FORBIDDEN'::text, NULL::int; RETURN; END IF;
  SELECT pv.problem_id INTO v_problem
    FROM problem_version pv JOIN problem p ON p.org_id = pv.org_id AND p.id = pv.problem_id
   WHERE pv.org_id = platform_org_id() AND pv.id = p_problem_version AND p.source = 'catalog' AND p.archived_at IS NULL;
  IF v_problem IS NULL THEN RETURN QUERY SELECT 'NOT_FOUND'::text, NULL::int; RETURN; END IF;
  IF p_release_at IS NOT NULL AND p_release_at <= now() THEN RETURN QUERY SELECT 'VALIDATION_PAST'::text, NULL::int; RETURN; END IF;
  -- gửi lại cùng id (thử lại sau hết thời gian chờ): lũy đẳng
  SELECT a.class_id, a.assigned_by, a.release_rev INTO e FROM assignment a WHERE a.org_id = p_org AND a.id = p_assignment_id;
  IF e.class_id IS NOT NULL THEN
    IF e.class_id = p_class AND e.assigned_by = p_actor THEN RETURN QUERY SELECT 'DUPLICATE'::text, e.release_rev; RETURN; END IF;
    RETURN QUERY SELECT 'CONFLICT_DUPLICATE'::text, NULL::int; RETURN;
  END IF;
  BEGIN
    INSERT INTO assignment (id, org_id, kind, class_id, content_org_id, problem_id, problem_version_id, assigned_by,
                            release_at, allow_hint, allow_qa)
    VALUES (p_assignment_id, p_org, 'class', p_class, platform_org_id(), v_problem, p_problem_version, p_actor,
            p_release_at, p_allow_hint, p_allow_qa);
  EXCEPTION WHEN unique_violation THEN
    RETURN QUERY SELECT 'CONFLICT_DUPLICATE'::text, NULL::int; RETURN;      -- D-GEO-161: cùng problem, cùng lớp
  END;
  INSERT INTO audit_log (org_id, actor_user_id, action, target_id, before_release_at, after_release_at, request_id)
  VALUES (p_org, p_actor, 'assignment_create', p_assignment_id, NULL, p_release_at, p_request);
  RETURN QUERY SELECT 'OK'::text, 0;
END $$;
COMMENT ON FUNCTION fn_create_assignment IS 'Đường DUY NHẤT tạo bài giao lớp (D-GEO-158 (5), RV-10): giáo viên của lớp, đề kho, giờ mở NULL hoặc ở tương lai, CONFLICT_DUPLICATE khi cùng problem cùng lớp (D-GEO-161), luôn ghi audit_log.';

CREATE FUNCTION fn_create_self_assignment(p_org uuid, p_user uuid, p_assignment_id uuid, p_problem_version uuid)
RETURNS TABLE (status text, lock_source_assignment_id uuid)
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
#variable_conflict use_column
DECLARE v_problem uuid; v_lock uuid;
BEGIN
  SELECT pv.problem_id INTO v_problem
    FROM problem_version pv JOIN problem p ON p.org_id = pv.org_id AND p.id = pv.problem_id
   WHERE pv.org_id = p_org AND pv.id = p_problem_version AND p.source = 'student_input' AND p.owner_user_id = p_user;
  IF v_problem IS NULL THEN RETURN QUERY SELECT 'NOT_FOUND'::text, NULL::uuid; RETURN; END IF;
  v_lock := fn_self_lock_source(p_org, p_user, p_problem_version);
  INSERT INTO assignment (id, org_id, kind, owner_user_id, content_org_id, problem_id, problem_version_id, assigned_by, lock_source_assignment_id)
  VALUES (p_assignment_id, p_org, 'self', p_user, p_org, v_problem, p_problem_version, p_user, v_lock)
  ON CONFLICT (id) DO NOTHING;
  RETURN QUERY SELECT 'OK'::text, v_lock;
END $$;

-- Q10: đặt / đổi / mở ngay giờ mở lời giải, máy trạng thái 02_domain_model.md §2 và D-GEO-041…043:
--   chưa đặt -> sắp mở (R > now) · sắp mở -> sắp mở (R' > now) · chưa đặt|sắp mở -> đã mở (mở ngay: R := now())
--   · đã mở -> không đổi nữa (D-GEO-042 A). Kiểm giáo viên của lớp, khóa lạc quan release_rev, luôn ghi audit_log.
-- Mã trạng thái (RV-21): NOT_FOUND (không có bài lớp trong trường) · FORBIDDEN (có bài, người gọi không phải giáo viên của lớp)
--   · CONFLICT_ALREADY_OPEN · CONFLICT_REV (D-GEO-143) · VALIDATION_PAST · OK. Luôn kèm release_rev/release_at hiện hành khi CONFLICT.
CREATE FUNCTION fn_set_release(p_org uuid, p_actor uuid, p_assignment uuid,
                               p_open_now boolean, p_new_release_at timestamptz, p_expected_rev integer,
                               p_request uuid DEFAULT NULL)
RETURNS TABLE (status text, release_rev integer, release_at timestamptz)
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
#variable_conflict use_column
DECLARE r record; v_new timestamptz; v_rev integer; v_act audit_action;
BEGIN
  SELECT a.release_at AS rel, a.release_rev AS rev, c.teacher_id INTO r
  FROM assignment a JOIN class c ON c.org_id = a.org_id AND c.id = a.class_id
  WHERE a.org_id = p_org AND a.id = p_assignment AND a.kind = 'class'
  FOR UPDATE OF a;
  IF r.teacher_id IS NULL THEN RETURN QUERY SELECT 'NOT_FOUND'::text, NULL::int, NULL::timestamptz; RETURN; END IF;
  IF r.teacher_id <> p_actor THEN RETURN QUERY SELECT 'FORBIDDEN'::text, NULL::int, NULL::timestamptz; RETURN; END IF;
  IF r.rel IS NOT NULL AND r.rel <= now() THEN RETURN QUERY SELECT 'CONFLICT_ALREADY_OPEN'::text, r.rev, r.rel; RETURN; END IF;
  IF r.rev <> p_expected_rev THEN RETURN QUERY SELECT 'CONFLICT_REV'::text, r.rev, r.rel; RETURN; END IF;
  IF p_open_now THEN
    v_new := now(); v_act := 'release_now';
  ELSE
    IF p_new_release_at IS NULL OR p_new_release_at <= now() THEN RETURN QUERY SELECT 'VALIDATION_PAST'::text, r.rev, r.rel; RETURN; END IF;
    v_new := p_new_release_at;
    v_act := CASE WHEN r.rel IS NULL THEN 'release_set' ELSE 'release_change' END;
  END IF;
  UPDATE assignment a SET release_at = v_new, release_rev = a.release_rev + 1
   WHERE a.org_id = p_org AND a.id = p_assignment RETURNING a.release_rev INTO v_rev;
  INSERT INTO audit_log (org_id, actor_user_id, action, target_id, before_release_at, after_release_at, request_id)
  VALUES (p_org, p_actor, v_act, p_assignment, r.rel, v_new, p_request);
  RETURN QUERY SELECT 'OK'::text, v_rev, v_new;
END $$;
COMMENT ON FUNCTION fn_set_release IS 'Đường DUY NHẤT sửa assignment.release_at. Giao dịch ngắn, khóa một hàng. Sau khi thành công ứng dụng xóa khóa đệm t:{org}:rel:{assignment} và phát kênh rel (ADR-GEO-10).';

-- ---------------------------------------------------------------- 9.6 Cổng lời giải (BR-01, ANS-02, D-GEO-150…153, 157)
-- Quyết định "được xem" LUÔN tính ở CSDL theo (org, user, assignment), không bao giờ đệm (D-GEO-157).
--  - bài lớp: em phải là thành viên lớp (không thì NOT_FOUND); release_at đã đặt và <= now() (không thì LOCKED)
--  - bài tự học: em là chủ (không thì NOT_FOUND); đề có chữ ký chuẩn (đề không dựng được hình không có lời giải tự học, D-GEO-150);
--    đã xem Gợi ý 2 (BR-05, D-GEO-023); KHÔNG khớp bài lớp đang khóa nào của em (fn_self_lock_source, kiểm mỗi lần, D-GEO-151)
CREATE FUNCTION fn_solution_gate(p_org uuid, p_user uuid, p_assignment uuid)
RETURNS TABLE (status text, content_org_id uuid, problem_version_id uuid, release_rev integer, lock_source uuid)
LANGUAGE plpgsql STABLE SECURITY DEFINER SET search_path = public, pg_temp
AS $$
#variable_conflict use_column
DECLARE a record; v_lock uuid;
BEGIN
  SELECT x.kind, x.class_id, x.owner_user_id, x.content_org_id AS corg, x.problem_version_id AS pv, x.release_at AS rel, x.release_rev AS rev
    INTO a FROM assignment x WHERE x.org_id = p_org AND x.id = p_assignment;
  IF a.kind IS NULL THEN RETURN QUERY SELECT 'NOT_FOUND'::text, NULL::uuid, NULL::uuid, NULL::int, NULL::uuid; RETURN; END IF;
  IF a.kind = 'class' THEN
    IF NOT EXISTS (SELECT 1 FROM class_member cm WHERE cm.org_id = p_org AND cm.class_id = a.class_id AND cm.user_id = p_user AND cm.left_at IS NULL) THEN
      RETURN QUERY SELECT 'NOT_FOUND'::text, NULL::uuid, NULL::uuid, NULL::int, NULL::uuid; RETURN;
    END IF;
    IF a.rel IS NULL OR a.rel > now() THEN
      RETURN QUERY SELECT 'LOCKED'::text, NULL::uuid, NULL::uuid, a.rev, NULL::uuid; RETURN;
    END IF;
    RETURN QUERY SELECT 'OK'::text, a.corg, a.pv, a.rev, NULL::uuid; RETURN;
  END IF;
  IF a.owner_user_id <> p_user THEN RETURN QUERY SELECT 'NOT_FOUND'::text, NULL::uuid, NULL::uuid, NULL::int, NULL::uuid; RETURN; END IF;
  IF NOT EXISTS (SELECT 1 FROM problem_signature s WHERE s.org_id = p_org AND s.problem_version_id = a.pv)
     OR NOT EXISTS (SELECT 1 FROM assignment_progress ap WHERE ap.org_id = p_org AND ap.assignment_id = p_assignment
                    AND ap.user_id = p_user AND ap.hint_level_max >= 2) THEN
    RETURN QUERY SELECT 'LOCKED'::text, NULL::uuid, NULL::uuid, a.rev, NULL::uuid; RETURN;
  END IF;
  v_lock := fn_self_lock_source(p_org, p_user, a.pv);
  IF v_lock IS NOT NULL THEN RETURN QUERY SELECT 'LOCKED'::text, NULL::uuid, NULL::uuid, a.rev, v_lock; RETURN; END IF;
  RETURN QUERY SELECT 'OK'::text, a.corg, a.pv, a.rev, NULL::uuid;
END $$;
COMMENT ON FUNCTION fn_solution_gate IS 'Cổng quyết định dùng chung cho fn_get_solution, fn_record_durable(unlock) và cổng AI (bật/tắt Kiểm soát đáp án theo trạng thái khóa).';

CREATE FUNCTION fn_get_solution(p_org uuid, p_user uuid, p_assignment uuid)
RETURNS TABLE (status text, problem_version_id uuid, final_answer text, steps jsonb, schema_version smallint, ai_authored boolean, release_rev integer)
LANGUAGE plpgsql STABLE SECURITY DEFINER SET search_path = public, pg_temp
AS $$
#variable_conflict use_column
DECLARE g record;
BEGIN
  SELECT * INTO g FROM fn_solution_gate(p_org, p_user, p_assignment);
  IF g.status <> 'OK' THEN
    RETURN QUERY SELECT g.status, NULL::uuid, NULL::text, NULL::jsonb, NULL::smallint, NULL::boolean, g.release_rev; RETURN;
  END IF;
  RETURN QUERY SELECT 'OK'::text, s.problem_version_id, s.final_answer, s.steps, s.schema_version, s.ai_authored, g.release_rev
    FROM solution s WHERE s.org_id = g.content_org_id AND s.problem_version_id = g.problem_version_id;
  IF NOT FOUND THEN
    RETURN QUERY SELECT 'NO_SOLUTION'::text, NULL::uuid, NULL::text, NULL::jsonb, NULL::smallint, NULL::boolean, g.release_rev;
  END IF;
END $$;
COMMENT ON FUNCTION fn_get_solution IS 'Đường DUY NHẤT để học sinh nhận lời giải (BR-01, ANS-02). Luôn trả đúng 1 hàng: status + nội dung chỉ khi OK. SECURITY DEFINER vì geo_app không có SELECT trên solution.';

-- Q19b: giáo viên xem lời giải — CHỈ đề kho nền tảng (D-GEO-153, RV-04); không bao giờ đề tự nhập hay nội dung trường khác.
CREATE FUNCTION fn_get_solution_teacher(p_user_org uuid, p_user uuid, p_problem_version uuid)
RETURNS TABLE (problem_version_id uuid, final_answer text, steps jsonb, schema_version smallint, ai_authored boolean)
LANGUAGE plpgsql STABLE SECURITY DEFINER SET search_path = public, pg_temp
AS $$
#variable_conflict use_column
BEGIN
  RETURN QUERY
  SELECT s.problem_version_id, s.final_answer, s.steps, s.schema_version, s.ai_authored
  FROM app_user u
  JOIN problem_version pv ON pv.org_id = platform_org_id() AND pv.id = p_problem_version
  JOIN problem p          ON p.org_id = pv.org_id AND p.id = pv.problem_id AND p.source = 'catalog'
  JOIN solution s         ON s.org_id = pv.org_id AND s.problem_version_id = pv.id
  WHERE u.org_id = p_user_org AND u.id = p_user AND u.role = 'teacher' AND u.deactivated_at IS NULL
    AND u.account_kind IN ('sample', 'verified');                -- PO-18; tài khoản system (tác giả) không đăng nhập
END $$;
COMMENT ON FUNCTION fn_get_solution_teacher IS 'Giáo viên xem lời giải đề kho (KNW-02). Màn xem trước KHÔNG gọi hàm này (D-GEO-153, Q23).';

-- ---------------------------------------------------------------- 9.7 Ghi sự kiện và tiến độ (D-GEO-158 (4), RV-17, 18, 26)
-- Cập nhật bộ đếm tiến độ: dùng chung cho mọi hàm ghi sự kiện. Thời gian làm bài: cộng độ dài khoảng sự kiện cộng khoảng
-- nghỉ từ hoạt động trước nếu < 30 phút (khoảng nghỉ >= 30 phút 0 giây mở phiên mới: D-GEO-144, RV-17).
CREATE FUNCTION _progress_apply(p_org uuid, p_assignment uuid, p_user uuid, p_first timestamptz, p_last timestamptz,
                                p_tools int, p_hint_level smallint, p_hint_views int, p_asks int, p_open boolean, p_unlock boolean)
RETURNS void LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
BEGIN
  INSERT INTO assignment_progress AS ap
         (org_id, assignment_id, user_id, first_opened_at, last_active_at, hint_level_max, hint_view_count, ask_count, tool_count,
          active_seconds, solution_viewed_at)
  VALUES (p_org, p_assignment, p_user, CASE WHEN p_open THEN p_first END, p_last, p_hint_level, p_hint_views, p_asks, p_tools,
          GREATEST(0, extract(epoch FROM p_last - p_first))::int, CASE WHEN p_unlock THEN p_last END)
  ON CONFLICT (org_id, assignment_id, user_id) DO UPDATE SET
         first_opened_at    = COALESCE(ap.first_opened_at, EXCLUDED.first_opened_at),
         last_active_at     = GREATEST(ap.last_active_at, EXCLUDED.last_active_at),
         hint_level_max     = GREATEST(ap.hint_level_max, EXCLUDED.hint_level_max),
         hint_view_count    = ap.hint_view_count + EXCLUDED.hint_view_count,
         ask_count          = ap.ask_count + EXCLUDED.ask_count,
         tool_count         = ap.tool_count + EXCLUDED.tool_count,
         active_seconds     = ap.active_seconds + EXCLUDED.active_seconds
                              + CASE WHEN p_first - ap.last_active_at >= interval '0' AND p_first - ap.last_active_at < interval '30 minutes'
                                     THEN extract(epoch FROM p_first - ap.last_active_at)::int ELSE 0 END,
         solution_viewed_at = COALESCE(ap.solution_viewed_at, EXCLUDED.solution_viewed_at),
         updated_at         = now();
END $$;

-- Q07 + Q08: lô sự kiện mức A (tool, scene_shown) của MỘT học sinh, một câu INSERT có kiểm tư cách lớp (RV-26) + một upsert tiến độ.
-- Ứng dụng đặt SET LOCAL synchronous_commit = off trước khi gọi (ADR-GEO-11); lũy đẳng lô bằng batch_id ở Redis (ADR-GEO-11).
CREATE FUNCTION fn_record_batch(p_org uuid, p_user uuid, p_assignment uuid,
                                p_occurred_at timestamptz[], p_kind event_kind[], p_arg smallint[])
RETURNS TABLE (status text, inserted integer)
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
#variable_conflict use_column
DECLARE v_n int; v_tools int; v_min timestamptz; v_max timestamptz;
BEGIN
  IF cardinality(p_kind) IS NULL OR cardinality(p_kind) = 0 OR cardinality(p_kind) > 200
     OR cardinality(p_kind) <> cardinality(p_occurred_at) OR cardinality(p_kind) <> cardinality(p_arg)
     OR EXISTS (SELECT 1 FROM unnest(p_kind) k WHERE k NOT IN ('tool', 'scene_shown')) THEN
    RETURN QUERY SELECT 'VALIDATION_KIND'::text, 0; RETURN;
  END IF;
  WITH ins AS (
    INSERT INTO event_log (occurred_at, org_id, assignment_id, user_id, kind, arg)
    SELECT LEAST(GREATEST(e.t, now() - interval '24 hours'), now()), p_org, p_assignment, p_user, e.k, e.a
    FROM unnest(p_occurred_at, p_kind, p_arg) AS e(t, k, a)
    WHERE EXISTS (SELECT 1 FROM assignment x WHERE x.org_id = p_org AND x.id = p_assignment
                    AND ((x.kind = 'class' AND EXISTS (SELECT 1 FROM class_member cm WHERE cm.org_id = x.org_id AND cm.class_id = x.class_id
                                                        AND cm.user_id = p_user AND cm.left_at IS NULL))
                      OR (x.kind = 'self' AND x.owner_user_id = p_user)))
    RETURNING occurred_at, kind)
  SELECT count(*), count(*) FILTER (WHERE ins.kind = 'tool'), min(ins.occurred_at), max(ins.occurred_at)
    INTO v_n, v_tools, v_min, v_max FROM ins;
  IF v_n = 0 THEN RETURN QUERY SELECT 'NOT_FOUND'::text, 0; RETURN; END IF;
  PERFORM _progress_apply(p_org, p_assignment, p_user, v_min, v_max, v_tools, 0::smallint, 0, 0, false, false);
  RETURN QUERY SELECT 'OK'::text, v_n;
END $$;

-- Sự kiện mức B 'open' và 'unlock' (D-GEO-162): một giao dịch đồng bộ, lũy đẳng theo request_id (RV-18).
-- 'unlock' chỉ ghi khi cổng cho phép (không thể có "đã xem lời giải" khi bài còn khóa). 'hint' ghi trong fn_get_hint, 'ask' trong fn_record_qa.
CREATE FUNCTION fn_record_durable(p_org uuid, p_user uuid, p_assignment uuid, p_request uuid, p_kind event_kind,
                                  p_occurred_at timestamptz DEFAULT NULL)
RETURNS text LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
DECLARE v_status text; v_n int; v_at timestamptz := LEAST(GREATEST(COALESCE(p_occurred_at, now()), now() - interval '24 hours'), now());
BEGIN
  IF p_kind NOT IN ('open', 'unlock') OR p_request IS NULL THEN RETURN 'VALIDATION_KIND'; END IF;
  IF p_kind = 'unlock' THEN
    SELECT g.status INTO v_status FROM fn_solution_gate(p_org, p_user, p_assignment) g;
    IF v_status <> 'OK' THEN RETURN v_status; END IF;
  ELSIF NOT EXISTS (SELECT 1 FROM assignment x WHERE x.org_id = p_org AND x.id = p_assignment
                      AND ((x.kind = 'class' AND EXISTS (SELECT 1 FROM class_member cm WHERE cm.org_id = x.org_id AND cm.class_id = x.class_id
                                                          AND cm.user_id = p_user AND cm.left_at IS NULL))
                        OR (x.kind = 'self' AND x.owner_user_id = p_user))) THEN
    RETURN 'NOT_FOUND';
  END IF;
  INSERT INTO durable_request (org_id, request_id, kind) VALUES (p_org, p_request, p_kind) ON CONFLICT DO NOTHING;
  GET DIAGNOSTICS v_n = ROW_COUNT;
  IF v_n = 0 THEN RETURN 'DUPLICATE'; END IF;
  INSERT INTO event_log (occurred_at, org_id, assignment_id, user_id, kind) VALUES (v_at, p_org, p_assignment, p_user, p_kind);
  PERFORM _progress_apply(p_org, p_assignment, p_user, v_at, v_at, 0, 0::smallint, 0, 0, p_kind = 'open', p_kind = 'unlock');
  RETURN 'OK';
END $$;

-- Q05: lấy gợi ý theo mức VÀ ghi nhận đã xem trong cùng giao dịch (BR-04, D-GEO-108, RV-10 (4), RV-15).
--  - bài lớp: thành viên lớp, bài cho phép gợi ý; gợi ý của đề kho được giao
--  - bài tự học: chủ bài; nếu khớp bài lớp đang khóa (kiểm mỗi lần, D-GEO-151) thì dùng gợi ý của đề kho đó (D-GEO-155), ngược lại gợi ý của đề tự nhập
--  - mức xin <= mức đã xem + 1 (LEVEL_ORDER); lũy đẳng theo request_id (gửi lại: trả lại gợi ý, không đếm lại)
CREATE FUNCTION fn_get_hint(p_org uuid, p_user uuid, p_assignment uuid, p_level smallint, p_request uuid)
RETURNS TABLE (status text, level smallint, body text, hint_source text)
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
#variable_conflict use_column
DECLARE a record; v_max smallint; v_src_org uuid; v_src_pv uuid; v_source text; v_lock uuid; v_body text; v_n int;
BEGIN
  IF p_level NOT IN (1, 2) OR p_request IS NULL THEN RETURN QUERY SELECT 'VALIDATION_KIND'::text, NULL::smallint, NULL::text, NULL::text; RETURN; END IF;
  SELECT x.kind, x.class_id, x.owner_user_id, x.content_org_id AS corg, x.problem_version_id AS pv, x.allow_hint
    INTO a FROM assignment x WHERE x.org_id = p_org AND x.id = p_assignment;
  IF a.kind IS NULL
     OR (a.kind = 'class' AND NOT EXISTS (SELECT 1 FROM class_member cm WHERE cm.org_id = p_org AND cm.class_id = a.class_id
                                           AND cm.user_id = p_user AND cm.left_at IS NULL))
     OR (a.kind = 'self' AND a.owner_user_id <> p_user) THEN
    RETURN QUERY SELECT 'NOT_FOUND'::text, NULL::smallint, NULL::text, NULL::text; RETURN;
  END IF;
  IF a.kind = 'class' AND NOT a.allow_hint THEN RETURN QUERY SELECT 'HINT_DISABLED'::text, NULL::smallint, NULL::text, NULL::text; RETURN; END IF;
  SELECT ap.hint_level_max INTO v_max FROM assignment_progress ap
   WHERE ap.org_id = p_org AND ap.assignment_id = p_assignment AND ap.user_id = p_user;
  IF p_level > COALESCE(v_max, 0) + 1 THEN RETURN QUERY SELECT 'LEVEL_ORDER'::text, NULL::smallint, NULL::text, NULL::text; RETURN; END IF;
  IF a.kind = 'class' THEN
    v_src_org := a.corg; v_src_pv := a.pv; v_source := 'assignment';
  ELSE
    v_lock := fn_self_lock_source(p_org, p_user, a.pv);
    IF v_lock IS NOT NULL THEN
      SELECT l.content_org_id, l.problem_version_id INTO v_src_org, v_src_pv FROM assignment l WHERE l.org_id = p_org AND l.id = v_lock;
      v_source := 'catalog_inherited';
    ELSE
      v_src_org := a.corg; v_src_pv := a.pv; v_source := 'self';
    END IF;
  END IF;
  SELECT h.body INTO v_body FROM hint h WHERE h.org_id = v_src_org AND h.problem_version_id = v_src_pv AND h.level = p_level;
  IF v_body IS NULL THEN RETURN QUERY SELECT 'NO_HINT'::text, NULL::smallint, NULL::text, NULL::text; RETURN; END IF;
  INSERT INTO durable_request (org_id, request_id, kind) VALUES (p_org, p_request, 'hint') ON CONFLICT DO NOTHING;
  GET DIAGNOSTICS v_n = ROW_COUNT;
  IF v_n = 1 THEN
    INSERT INTO event_log (occurred_at, org_id, assignment_id, user_id, kind, arg) VALUES (now(), p_org, p_assignment, p_user, 'hint', p_level);
    PERFORM _progress_apply(p_org, p_assignment, p_user, now(), now(), 0, p_level, 1, 0, false, false);
  END IF;
  RETURN QUERY SELECT 'OK'::text, p_level, v_body, v_source;
END $$;
COMMENT ON FUNCTION fn_get_hint IS 'Đường DUY NHẤT học sinh nhận gợi ý và đường DUY NHẤT nâng hint_level_max (điều kiện mở lời giải tự học): mức chỉ tăng khi gợi ý thật sự được trả.';

-- Lượt hỏi đáp: ghi câu hỏi + câu trả lời đã qua Kiểm soát đáp án + sự kiện 'ask' + tiến độ, lũy đẳng theo request_id (D-GEO-159, RV-18).
CREATE FUNCTION fn_record_qa(p_org uuid, p_user uuid, p_assignment uuid, p_request uuid, p_question text, p_reply_kind qa_reply_kind,
                             p_reply_text text, p_guard_version text, p_sources uuid[] DEFAULT '{}')
RETURNS TABLE (status text, qa_id bigint)
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
#variable_conflict use_column
DECLARE a record; v_id bigint;
BEGIN
  SELECT x.kind, x.class_id, x.owner_user_id, x.allow_qa INTO a FROM assignment x WHERE x.org_id = p_org AND x.id = p_assignment;
  IF a.kind IS NULL
     OR (a.kind = 'class' AND NOT EXISTS (SELECT 1 FROM class_member cm WHERE cm.org_id = p_org AND cm.class_id = a.class_id
                                           AND cm.user_id = p_user AND cm.left_at IS NULL))
     OR (a.kind = 'self' AND a.owner_user_id <> p_user) THEN
    RETURN QUERY SELECT 'NOT_FOUND'::text, NULL::bigint; RETURN;
  END IF;
  IF a.kind = 'class' AND NOT a.allow_qa THEN RETURN QUERY SELECT 'QA_DISABLED'::text, NULL::bigint; RETURN; END IF;
  INSERT INTO qa_turn (org_id, assignment_id, user_id, request_id, question, reply_kind, reply_text, guard_version, source_lecture_ids)
  VALUES (p_org, p_assignment, p_user, p_request, p_question, p_reply_kind, p_reply_text, p_guard_version, p_sources)
  ON CONFLICT (org_id, request_id) DO NOTHING
  RETURNING id INTO v_id;
  IF v_id IS NULL THEN
    SELECT q.id INTO v_id FROM qa_turn q WHERE q.org_id = p_org AND q.request_id = p_request AND q.user_id = p_user;
    RETURN QUERY SELECT 'DUPLICATE'::text, v_id; RETURN;
  END IF;
  INSERT INTO event_log (occurred_at, org_id, assignment_id, user_id, kind) VALUES (now(), p_org, p_assignment, p_user, 'ask');
  PERFORM _progress_apply(p_org, p_assignment, p_user, now(), now(), 0, 0::smallint, 0, 1, false, false);
  RETURN QUERY SELECT 'OK'::text, v_id;
END $$;

-- Q21: lịch sử hỏi đáp của CHÍNH học sinh trên mọi bài, mới nhất trước, keyset theo id (US-GEO-410, D-GEO-159).
CREATE FUNCTION fn_qa_history(p_org uuid, p_user uuid, p_before bigint DEFAULT NULL, p_limit integer DEFAULT 21)
RETURNS TABLE (id bigint, assignment_id uuid, asked_at timestamptz, question text, reply_kind qa_reply_kind, reply_text text, source_lecture_ids uuid[])
LANGUAGE plpgsql STABLE SECURITY DEFINER SET search_path = public, pg_temp
AS $$
#variable_conflict use_column
BEGIN
  RETURN QUERY
  SELECT q.id, q.assignment_id, q.asked_at, q.question, q.reply_kind, q.reply_text, q.source_lecture_ids
  FROM qa_turn q
  WHERE q.org_id = p_org AND q.user_id = p_user AND q.id < COALESCE(p_before, 9223372036854775807)
  ORDER BY q.id DESC
  LIMIT LEAST(GREATEST(p_limit, 1), 101);    -- trần trang 100 + 1 (D-GEO-168)
END $$;

-- =====================================================================================
-- 10. QUẢN LÝ PARTITION THEO THÁNG, DỌN KHÓA TẠM
-- =====================================================================================
CREATE FUNCTION ensure_monthly_partitions(p_parent regclass, p_ahead int DEFAULT 2, p_back int DEFAULT 0)
RETURNS SETOF text LANGUAGE plpgsql AS $$
DECLARE m date; lo timestamptz; hi timestamptz; nm text; dflt regclass; n bigint; base text;
BEGIN
  base := split_part(p_parent::text, '.', 2); IF base = '' THEN base := p_parent::text; END IF;
  dflt := to_regclass(format('public.%I_default', base));
  FOR m IN SELECT generate_series(date_trunc('month', now())::date - (p_back || ' months')::interval,
                                  date_trunc('month', now())::date + (p_ahead || ' months')::interval, interval '1 month')::date
  LOOP
    lo := m::timestamptz; hi := (m + interval '1 month')::timestamptz;
    nm := format('%s_y%sm%s', base, to_char(m, 'YYYY'), to_char(m, 'MM'));
    IF to_regclass('public.' || nm) IS NULL THEN
      IF dflt IS NOT NULL THEN
        EXECUTE format('SELECT count(*) FROM %s WHERE occurred_at >= %L AND occurred_at < %L', dflt, lo, hi) INTO n;
        IF n > 0 THEN RAISE EXCEPTION 'default partition chứa % dòng của %: xử lý tay trước', n, nm; END IF;
      END IF;
      EXECUTE format('CREATE TABLE public.%I PARTITION OF %s FOR VALUES FROM (%L) TO (%L)', nm, p_parent, lo, hi);
      RETURN NEXT nm;
    END IF;
  END LOOP;
END $$;
COMMENT ON FUNCTION ensure_monthly_partitions IS 'Chạy theo lịch (hằng ngày, tạo trước 2 tháng) và lúc khởi động (vai geo_ops). Giám sát: số partition tương lai >= 1 và *_default rỗng.';

CREATE FUNCTION drop_old_partitions(p_parent regclass, p_keep_months int)
RETURNS SETOF text LANGUAGE plpgsql AS $$
DECLARE r record; cutoff timestamptz := date_trunc('month', now()) - (p_keep_months || ' months')::interval;
        hi timestamptz;
BEGIN
  FOR r IN SELECT c.oid::regclass AS child, pg_get_expr(c.relpartbound, c.oid) AS bound
           FROM pg_inherits i JOIN pg_class c ON c.oid = i.inhrelid
           WHERE i.inhparent = p_parent AND c.relispartition AND pg_get_expr(c.relpartbound, c.oid) <> 'DEFAULT'
  LOOP
    hi := (regexp_match(r.bound, 'TO \(''([^'']+)''\)'))[1]::timestamptz;
    IF hi <= cutoff THEN
      EXECUTE format('ALTER TABLE %s DETACH PARTITION %s', p_parent, r.child);
      EXECUTE format('DROP TABLE %s', r.child);
      RETURN NEXT r.child::text;
    END IF;
  END LOOP;
END $$;
COMMENT ON FUNCTION drop_old_partitions IS 'Cơ chế dọn theo tháng có sẵn nhưng MVP KHÔNG chạy (D-GEO-128: chưa quyết thời hạn lưu giữ). Không cấp cho vai nào; chỉ chủ lược đồ chạy tay khi ChuDauTu chốt chính sách.';

CREATE FUNCTION fn_purge_ephemeral() RETURNS TABLE (durable_request_deleted bigint, auth_failure_deleted bigint)
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
DECLARE a bigint; b bigint;
BEGIN
  DELETE FROM durable_request d WHERE d.created_at < now() - interval '24 hours'; GET DIAGNOSTICS a = ROW_COUNT;
  DELETE FROM auth_failure f WHERE f.at < now() - interval '24 hours';           GET DIAGNOSTICS b = ROW_COUNT;
  RETURN QUERY SELECT a, b;
END $$;
COMMENT ON FUNCTION fn_purge_ephemeral IS 'Dọn khóa lũy đẳng và lần thử sai cũ (không phải dữ liệu sự kiện, D-GEO-128 không áp). Lịch hằng giờ, vai geo_ops.';

SELECT ensure_monthly_partitions('public.event_log', 2, 4);
SELECT ensure_monthly_partitions('public.ai_call_log', 2, 4);
SELECT ensure_monthly_partitions('public.answer_guard_log', 2, 0);

-- =====================================================================================
-- 11. DỮ LIỆU THAM CHIẾU TỐI THIỂU
-- =====================================================================================
INSERT INTO org (id, parent_id, kind, name) VALUES (platform_org_id(), NULL, 'platform', 'Nền tảng GEO');
-- D-GEO-160: tác giả nền tảng đứng tên kho đề và lời giải kho (tài khoản hệ thống, không đăng nhập được).
INSERT INTO app_user (id, org_id, role, account_kind, display_name)
VALUES ('00000000-0000-7000-8000-0000000000aa', platform_org_id(), 'teacher', 'system', 'Giáo viên mẫu (tác giả kho đề)');
INSERT INTO subject (id, code, name) VALUES (1, 'solid_geometry', 'Hình học không gian');
INSERT INTO problem_topic (id, subject_id, slug, name) VALUES
  (1, 1, 'angle',    'Góc'),
  (2, 1, 'distance', 'Khoảng cách'),
  (3, 1, 'volume',   'Thể tích'),
  (4, 1, 'area',     'Diện tích'),
  (5, 1, 'sphere',   'Mặt cầu');   -- D-GEO-070: "dạng bài" = chủ đề

-- =====================================================================================
-- 12. QUYỀN (tách quyền ở tầng dữ liệu, D-GEO-158)
-- =====================================================================================
GRANT USAGE ON SCHEMA public TO geo_app, geo_ai, geo_content, geo_ops;
REVOKE ALL ON ALL TABLES IN SCHEMA public FROM PUBLIC;
REVOKE ALL ON ALL FUNCTIONS IN SCHEMA public FROM PUBLIC;

-- ---- geo_app: đọc mọi bảng không nhạy cảm; ghi tối thiểu (chỉ dữ liệu tự học của chính trường và lớp)
GRANT SELECT ON org, subject, problem_topic, solid_template, app_user, class, class_member, problem, problem_version,
                scene_build, hint, assignment, assignment_progress, event_log, lecture, keyword, share_grant, audit_log TO geo_app;
-- qa_turn: mọi cột TRỪ reply_text (chỉ chủ câu hỏi đọc, qua fn_qa_history; D-GEO-159)
GRANT SELECT (id, org_id, assignment_id, user_id, request_id, asked_at, question, reply_kind, guard_version, source_lecture_ids) ON qa_turn TO geo_app;
-- Đề tự nhập (tự học): problem_scope_ck buộc source=student_input ở trường; trigger chặn hàng nền tảng; version luôn needs_review lúc tạo
GRANT INSERT ON problem, problem_version, scene_build, hint, problem_signature, class TO geo_app;
GRANT UPDATE (display_name, deactivated_at) ON app_user TO geo_app;
GRANT UPDATE (name, archived_at) ON class TO geo_app;
GRANT UPDATE (left_at) ON class_member TO geo_app;
GRANT UPDATE (current_version_id, archived_at, title) ON problem TO geo_app;   -- trigger chặn hàng nền tảng
-- KHÔNG: INSERT app_user/class_member/assignment/assignment_progress/event_log/qa_turn/audit_log; UPDATE verify_status,
-- approval_status (D-GEO-028: MVP không có duyệt của người), allow_hint/allow_qa (D-GEO-114), lecture/keyword (nội dung nền tảng).
-- KHÔNG có quyền nào trên solution, verification_report, teacher_access_code, auth_failure, durable_request, answer_guard_log, probe_*.
GRANT EXECUTE ON FUNCTION f_unaccent(text), platform_org_id() TO geo_app, geo_ai, geo_content, geo_ops;
GRANT EXECUTE ON FUNCTION
  fn_student_login(text, text, text, uuid),
  fn_teacher_login(text, bytea, bytea),
  fn_self_lock_source(uuid, uuid, uuid),
  fn_create_assignment(uuid, uuid, uuid, uuid, uuid, timestamptz, boolean, boolean, uuid),
  fn_create_self_assignment(uuid, uuid, uuid, uuid),
  fn_set_release(uuid, uuid, uuid, boolean, timestamptz, integer, uuid),
  fn_solution_gate(uuid, uuid, uuid),
  fn_get_solution(uuid, uuid, uuid),
  fn_get_solution_teacher(uuid, uuid, uuid),
  fn_record_batch(uuid, uuid, uuid, timestamptz[], event_kind[], smallint[]),
  fn_record_durable(uuid, uuid, uuid, uuid, event_kind, timestamptz),
  fn_get_hint(uuid, uuid, uuid, smallint, uuid),
  fn_qa_history(uuid, uuid, bigint, integer) TO geo_app;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA public TO geo_app, geo_ai, geo_content, geo_ops;

-- ---- geo_ai: cổng AI. Đọc lời giải để Kiểm soát đáp án (ADR-GEO-14); ghi chi phí, nhật ký chặn; lời giải/báo cáo của đề tự nhập.
GRANT SELECT ON solution, verification_report, problem_version, problem_signature, scene_build, hint, lecture, keyword, assignment TO geo_ai;
GRANT INSERT ON ai_call_log, answer_guard_log, verification_report, solution, hint TO geo_ai;   -- trigger chặn hàng nền tảng
GRANT EXECUTE ON FUNCTION fn_record_qa(uuid, uuid, uuid, uuid, text, qa_reply_kind, text, text, uuid[]),
                          fn_set_verify_status(uuid, uuid, uuid, uuid),
                          fn_solution_gate(uuid, uuid, uuid) TO geo_ai;

-- ---- geo_content: nạp kho nền tảng (không dùng ở đường người dùng)
GRANT SELECT, INSERT ON solid_template, problem, problem_version, problem_signature, scene_build, hint, solution,
                        verification_report, lecture, keyword, probe_prompt TO geo_content;
GRANT SELECT ON org, app_user, subject, problem_topic TO geo_content;
GRANT UPDATE (current_version_id, archived_at, title) ON problem TO geo_content;
GRANT UPDATE (approval_status) ON problem_version TO geo_content;
GRANT UPDATE (answer_forms, answer_forms_version) ON solution TO geo_content;
GRANT UPDATE (title, body, source_ref, approval_status, human_reviewed) ON lecture TO geo_content;
GRANT UPDATE (active) ON probe_prompt TO geo_content;
GRANT EXECUTE ON FUNCTION fn_set_verify_status(uuid, uuid, uuid, uuid) TO geo_content;

-- ---- geo_ops: vận hành
GRANT EXECUTE ON FUNCTION fn_create_sample_teacher(uuid, uuid, text),
                          fn_issue_teacher_code(uuid, uuid, uuid, text, bytea, smallint, timestamptz, uuid),
                          fn_revoke_teacher_code(uuid, uuid),
                          ensure_monthly_partitions(regclass, int, int),
                          fn_purge_ephemeral() TO geo_ops;
GRANT SELECT ON probe_prompt, probe_run TO geo_ops;
GRANT INSERT ON probe_run TO geo_ops;
GRANT SELECT (id, org_id, user_id, selector, issued_at, expires_at, revoked_at, last_used_at) ON teacher_access_code TO geo_ops;

-- =====================================================================================
-- 13. ROW-LEVEL SECURITY (D-GEO-127): chính sách có sẵn, CHƯA BẬT trong tệp này.
-- Bật bằng bench/rls_enable.sql (ALTER TABLE ... ENABLE ROW LEVEL SECURITY) sau khi đo chi phí trên khung mã thật.
-- Ứng dụng đặt set_config('app.org_id', :org, true) đầu mỗi giao dịch; không đặt => 0 hàng (đóng khi thiếu ngữ cảnh).
-- Hàm SECURITY DEFINER (chủ là superuser) bỏ qua RLS và vẫn tự lọc org_id (lớp phòng thủ thứ nhất).
-- =====================================================================================
DO $$
DECLARE t text;
BEGIN
  FOREACH t IN ARRAY ARRAY['app_user','class','class_member','assignment','assignment_progress','event_log','qa_turn','audit_log'] LOOP
    EXECUTE format('CREATE POLICY tenant_iso ON public.%I FOR ALL TO geo_app
                    USING (org_id = (SELECT NULLIF(current_setting(''app.org_id'', true), '''')::uuid))
                    WITH CHECK (org_id = (SELECT NULLIF(current_setting(''app.org_id'', true), '''')::uuid))', t);
  END LOOP;
  FOREACH t IN ARRAY ARRAY['problem','problem_version','scene_build','hint','problem_signature'] LOOP
    EXECUTE format('CREATE POLICY tenant_or_platform ON public.%I FOR ALL TO geo_app
                    USING (org_id IN (platform_org_id(), (SELECT NULLIF(current_setting(''app.org_id'', true), '''')::uuid)))
                    WITH CHECK (org_id = (SELECT NULLIF(current_setting(''app.org_id'', true), '''')::uuid))', t);
  END LOOP;
END $$;

-- ANALYZE bỏ khỏi migration (chạy trong giao dịch của Flyway).
