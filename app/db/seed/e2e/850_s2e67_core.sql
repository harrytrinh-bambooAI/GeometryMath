-- S2-E6 + S2-E7 (T-261126-23) · dữ liệu cho ca kiểm Kho đề (C2-01: Của tôi / Kho chung, người soát, riêng tư, giới hạn
-- thêm đề) và Trình chiếu (C2-04). Chạy sau 800…830; không sửa dữ liệu của epic khác. UUID theo PLAN_code-s23 §4:
-- F-S2-nnn → 00000000-0000-7000-8002-<nnn 12 chữ số>, khoảng 85xxxxxxx của task (fixture-ids.d/s2e67.json).
--
-- Mỗi ca kiểm làm BẨN dữ liệu và Kho chung theo TỔ CHỨC (PLAN A10, TẠM) nên mỗi (epic, TC, làn) có MỘT tổ chức riêng:
--   gốc B = 850000000 + (epic − 6) × 5000000 + TC × 100000 + làn × 10000   (epic 6 = S2-E6, 7 = S2-E7)
--   làn 0…5 = api, ui-chromium, ui-webkit, ui-iphone, mobile-iphone, mobile-pixel (IT dùng làn 0)
--   B+1 tổ chức · B+2 Cô Trang (chủ đề) · B+3 Mai (người soát, vai content_reviewer) · B+4 Cô Lan · B+5 An (học sinh)
--   B+6 lớp 11A1 (An học) · B+7 Cô Hà (giáo viên mới, không lớp) · B+11 lớp 11A2 · B+12 lớp 11A3
--   đề (solution_doc) B+k (k 100…1099) · phiên bản 1 của đề B+k+5000 · đề xuất soát B+k+3000 · bản Kho chung B+k+7000
--   bài giao B+9001 (BG-1 11A1, mở T0 + 2 giờ) · B+9002 (BG-2 11A2, đã mở T0 − 1 giờ) · B+9003 (BG-3 11A3, chưa đặt giờ)
-- Đề kho SP-01 dùng chung cho bài giao: F-S2-859999990 (version F-S2-859999991), bộ 's2e67_present'.

CREATE OR REPLACE FUNCTION pg_temp.s67f(n bigint) RETURNS uuid LANGUAGE sql IMMUTABLE
AS $$ SELECT format('00000000-0000-7000-8002-%s', lpad(n::text, 12, '0'))::uuid $$;
-- Mã lớp duy nhất 6 ký tự [A-Z2-9] (tiền tố Z9 + 4 ký tự từ số).
CREATE OR REPLACE FUNCTION pg_temp.s67jc(n bigint) RETURNS text LANGUAGE sql IMMUTABLE
AS $$ SELECT 'Z9' || string_agg(substr('ABCDEFGHJKLMNPQRSTUVWXYZ23456789', ((n / (32 ^ i)::bigint) % 32)::int + 1, 1), '' ORDER BY i)
      FROM generate_series(0, 3) i $$;

CREATE TEMP TABLE t67_slot ON COMMIT DROP AS
SELECT e, tc, l, (850000000 + (e - 6) * 5000000 + tc * 100000 + l * 10000)::bigint AS b
FROM (SELECT 6 AS e, tc FROM generate_series(1, 37) tc UNION ALL SELECT 7, tc FROM generate_series(1, 33) tc) s,
     generate_series(0, 5) l;

INSERT INTO org (id, parent_id, kind, name)
SELECT pg_temp.s67f(b + 1), '00000000-0000-7000-8000-00000000f001', 'group', format('S2-E%s TC %s làn %s', e, tc, l)
FROM t67_slot;

INSERT INTO app_user (id, org_id, role, account_kind, display_name)
SELECT pg_temp.s67f(s.b + u.k), pg_temp.s67f(s.b + 1), u.role::user_role, u.kind::account_kind, u.name
FROM t67_slot s,
     (VALUES (2, 'teacher', 'verified', 'Cô Trang'), (3, 'teacher', 'verified', 'Mai'),
             (4, 'teacher', 'verified', 'Cô Lan'), (5, 'student', 'light', 'An'),
             (7, 'teacher', 'verified', 'Cô Hà')) u(k, role, kind, name);

-- Vai người soát nội dung do chủ sản phẩm gán (QA-S2E6-01).
INSERT INTO user_role_grant (org_id, user_id, role, granted_by)
SELECT pg_temp.s67f(b + 1), pg_temp.s67f(b + 3), 'content_reviewer', 'chủ sản phẩm (seed)' FROM t67_slot;

-- Lớp của Cô Trang; An học cả ba lớp, trừ TC-S2E6-037 (An tự học, không lớp).
INSERT INTO class (id, org_id, name, level, grade, join_code, teacher_id)
SELECT pg_temp.s67f(s.b + c.k), pg_temp.s67f(s.b + 1), c.name, 'thpt', 11, pg_temp.s67jc(s.b / 10000 * 3 + c.n), pg_temp.s67f(s.b + 2)
FROM t67_slot s, (VALUES (6, '11A1', 0), (11, '11A2', 1), (12, '11A3', 2)) c(k, name, n);
INSERT INTO class_member (org_id, class_id, user_id, name_key)
SELECT pg_temp.s67f(s.b + 1), pg_temp.s67f(s.b + c.k), pg_temp.s67f(s.b + 5), 'an'
FROM t67_slot s, (VALUES (6), (11), (12)) c(k)
WHERE NOT (s.e = 6 AND s.tc = 37);

-- ---------------------------------------------------------------- đề mẫu FX-10…FX-14
CREATE TEMP TABLE t67_fx ON COMMIT DROP AS
SELECT * FROM (VALUES
  ('FX10', 'PL-01 Góc A của tam giác 5, 12, 13', 'Tam giác ABC có AB = 5, AC = 12, BC = 13. Tính góc A.',
   '{"plane": {"code": "PL-01"}}',
   '["AB² + AC² = 25 + 144 = 169 = BC².", "Vậy tam giác ABC vuông tại A, góc A = 90°."]'),
  ('FX11', 'PL-02 Tam giác cân có góc A = 40°', 'Tam giác ABC cân tại A, góc A = 40°. Tính góc B.',
   '{"plane": {"code": "PL-02"}}',
   '["Tam giác ABC cân tại A nên góc B = góc C.", "Góc B = (180° − 40°) : 2 = 70°."]'),
  ('FX12', 'PL-03 Đường chéo hình chữ nhật', 'Hình chữ nhật ABCD có AB = 6, AD = 8. Tính AC.',
   '{"plane": {"code": "PL-03"}}',
   '["AC² = AB² + BC² = 36 + 64 = 100.", "Vậy AC = 10."]'),
  ('FX13', 'PL-04 Diện tích tam giác đều cạnh 4', 'Tam giác đều cạnh 4. Tính diện tích.',
   '{"plane": {"code": "PL-04"}}',
   '["Đường cao h = 4√3 : 2 = 2√3.", "Diện tích S = 4 · 2√3 : 2 = 4√3 ≈ 6,93."]'),
  ('FX14', 'SP-01 Góc giữa SC và mặt đáy',
   'Cho hình chóp S.ABCD có đáy ABCD là hình vuông cạnh a, SA vuông góc với mặt phẳng (ABCD) và SA = a√2. Tính góc giữa đường thẳng SC và mặt đáy.',
   '{"catalogCode": "HKN-02"}',
   '["SA ⊥ (ABCD) nên AC là hình chiếu của SC, góc cần tìm là SCA.", "ABCD vuông cạnh a nên AC = a√2.", "tan SCA = SA / AC = a√2 / a√2 = 1.", "Vậy góc giữa SC và mặt phẳng đáy bằng 45°."]')
) v(code, title, statement, figure, steps);

-- Đề theo ca: (epic, TC, k, chủ (k người dùng), mã đề, lệch giờ tạo so với T0 (phút), trục người, trạng thái gửi soát).
-- Trục người: appr = "Giáo viên đã duyệt", ai = "AI soạn", none = chưa có lời giải. Gửi soát: NULL | pending | accepted | rejected.
CREATE TEMP TABLE t67_spec (e int, tc int, k int, owner int, code text, minutes int, human text, sub text, title text) ON COMMIT DROP;
-- THREE: Cô Trang có FX-10 (09:00), FX-14 (10:00), FX-13 (11:00) — thứ tự mới nhất trước FX-13, FX-14, FX-10.
INSERT INTO t67_spec SELECT 6, tc, x.k, 2, x.code, x.m, 'appr', NULL, NULL
FROM unnest(ARRAY[1, 3, 4, 7, 13, 25, 26, 27, 36]) tc,
     (VALUES (100, 'FX10', -180), (101, 'FX14', -120), (102, 'FX13', -60)) x(k, code, m);
-- LIB: Kho chung có 2 đề đã soát (FX-11 phẳng, FX-14 không gian) của Cô Lan và 1 đề Chờ soát (FX-12).
INSERT INTO t67_spec SELECT 6, tc, x.k, 4, x.code, x.m, 'appr', x.sub, NULL
FROM unnest(ARRAY[2, 4, 32, 34, 35, 36, 37]) tc,
     (VALUES (110, 'FX11', -300, 'accepted'), (111, 'FX14', -240, 'accepted'), (112, 'FX12', -210, 'pending')) x(k, code, m, sub);
-- TC 6: Của tôi chỉ có đề phẳng.
INSERT INTO t67_spec VALUES (6, 6, 100, 2, 'FX10', -180, 'appr', NULL, NULL), (6, 6, 102, 2, 'FX13', -60, 'appr', NULL, NULL);
-- Phân trang: TC 8 = 20 đề, 9 = 21, 10 = 60, 11 = 21; TC 22: 11 đề đã duyệt, 9 đề đầu đang Chờ soát.
INSERT INTO t67_spec SELECT 6, n.tc, 1000 + i, 2, 'FX10', -600 + i, 'appr', CASE WHEN n.tc = 22 AND i <= 9 THEN 'pending' END,
                          format('Đề số %s', i)
FROM (VALUES (8, 20), (9, 21), (10, 60), (11, 21), (22, 11)) n(tc, cnt), generate_series(1, n.cnt) i;
-- FX-14 Chờ soát (14 duyệt, 15 từ chối, 18 hàng đợi, 20 lý do 4/5 ký tự); 17 đã duyệt chưa gửi; 23 đã vào Kho chung;
-- 24 bị từ chối; 16 "AI soạn"; 19 Mai là chủ FX-13 đã gửi soát; 21 hai đề Chờ soát; 29 một Chờ soát + hai bình thường.
INSERT INTO t67_spec SELECT 6, tc, 101, 2, 'FX14', -120, 'appr', 'pending', NULL FROM unnest(ARRAY[14, 15, 18, 20]) tc;
INSERT INTO t67_spec VALUES
  (6, 17, 101, 2, 'FX14', -120, 'appr', NULL, NULL),
  (6, 23, 101, 2, 'FX14', -120, 'appr', 'accepted', NULL),
  (6, 24, 101, 2, 'FX14', -120, 'appr', 'rejected', NULL),
  (6, 16, 103, 2, 'FX11', -120, 'ai', NULL, NULL),
  (6, 19, 104, 3, 'FX13', -120, 'appr', 'pending', NULL),
  (6, 21, 100, 2, 'FX10', -180, 'appr', 'pending', NULL),
  (6, 21, 101, 2, 'FX14', -120, 'appr', 'pending', NULL),
  (6, 29, 100, 2, 'FX10', -180, 'appr', NULL, NULL),
  (6, 29, 101, 2, 'FX14', -120, 'appr', 'pending', NULL),
  (6, 29, 102, 2, 'FX13', -60, 'appr', NULL, NULL);
-- S2-E7: mọi ca có FX-14 (4 bước, đã duyệt, gắn version đề kho SP-01), FX-12 chưa có lời giải, đề 30 bước.
INSERT INTO t67_spec SELECT 7, tc, x.k, 2, x.code, x.m, x.h, NULL, x.t
FROM generate_series(1, 33) tc,
     (VALUES (101, 'FX14', -120, 'appr', NULL), (105, 'FX12', -90, 'none', NULL),
             (106, 'FX14', -60, 'appr', 'SP-30 Lời giải 30 bước')) x(k, code, m, h, t);

CREATE TEMP TABLE t67_doc ON COMMIT DROP AS
SELECT s.e, s.tc, s.l, s.b, p.k, p.owner, p.human, p.sub, p.minutes, p.code,
       pg_temp.s67f(s.b + p.k) AS doc, pg_temp.s67f(s.b + p.k + 5000) AS v1, pg_temp.s67f(s.b + p.k + 3000) AS sub_id,
       pg_temp.s67f(s.b + p.k + 7000) AS lib, pg_temp.s67f(s.b + 1) AS org, pg_temp.s67f(s.b + p.owner) AS owner_id,
       COALESCE(p.title, x.title) AS title, x.statement, x.figure,
       CASE WHEN p.k = 106 AND s.e = 7
            THEN (SELECT jsonb_agg(format('Bước %s: %s', i, CASE WHEN i % 2 = 0 THEN 'AC = a√2 nên tan SCA = SA / AC.'
                                                              ELSE 'SA ⊥ (ABCD) nên AC là hình chiếu của SC.' END)
                                   ORDER BY i) FROM generate_series(1, 30) i)
            ELSE x.steps::jsonb END AS steps
FROM t67_slot s JOIN t67_spec p ON p.e = s.e AND p.tc = s.tc JOIN t67_fx x ON x.code = p.code;

-- Đề kho SP-01 dùng chung cho bài giao của S2-E7 (và TC-S2E6-036).
INSERT INTO problem (id, org_id, subject_id, source, author_user_id, level, solid_kind, topic_id, title, bank, created_at)
VALUES (pg_temp.s67f(859999990), platform_org_id(), 1, 'catalog', '00000000-0000-7000-8000-0000000000aa', 'thpt', 'pyramid', 1,
        'SP-01', 's2e67_present', :'t0'::timestamptz - interval '10 days');
INSERT INTO problem_version (id, org_id, problem_id, version, statement, statement_hash, approval_status, ai_authored,
                             created_by, created_at)
VALUES (pg_temp.s67f(859999991), platform_org_id(), pg_temp.s67f(859999990), 1,
        'Cho hình chóp S.ABCD có đáy ABCD là hình vuông cạnh a, SA vuông góc với mặt phẳng (ABCD) và SA = a√2. Tính góc giữa đường thẳng SC và mặt đáy.',
        digest('s2e67 SP-01 FX-14', 'sha256'), 'ai_drafted', true, '00000000-0000-7000-8000-0000000000aa',
        :'t0'::timestamptz - interval '10 days');
UPDATE problem SET current_version_id = pg_temp.s67f(859999991)
WHERE org_id = platform_org_id() AND id = pg_temp.s67f(859999990);
INSERT INTO catalog_bank_access (org_id, bank) SELECT pg_temp.s67f(b + 1), 's2e67_present' FROM t67_slot;

INSERT INTO solution_doc (id, org_id, owner_user_id, code, title, statement, figure, content_org_id, problem_version_id,
                          in_my_shelf, latest_version, created_at)
SELECT d.doc, d.org, d.owner_id, NULL, d.title, d.statement, d.figure::jsonb,
       CASE WHEN d.e = 7 AND d.k = 101 THEN platform_org_id() END,
       CASE WHEN d.e = 7 AND d.k = 101 THEN pg_temp.s67f(859999991) END,
       d.human = 'appr', CASE WHEN d.human = 'none' THEN 0 ELSE 1 END,
       :'t0'::timestamptz + make_interval(mins => d.minutes)
FROM t67_doc d;

INSERT INTO solution_version (id, org_id, doc_id, version, steps, ai_authored, author_user_id, note, human_status,
                              approved_by, approved_at, created_at)
SELECT d.v1, d.org, d.doc, 1, d.steps, d.human = 'ai', CASE WHEN d.human = 'ai' THEN NULL ELSE d.owner_id END,
       CASE WHEN d.human = 'ai' THEN 'AI soạn' ELSE 'bản duyệt' END,
       CASE WHEN d.human = 'ai' THEN 'ai_drafted' ELSE 'teacher_approved' END::solution_human_status,
       CASE WHEN d.human = 'appr' THEN d.owner_id END,
       CASE WHEN d.human = 'appr' THEN :'t0'::timestamptz + make_interval(mins => d.minutes + 5) END,
       :'t0'::timestamptz + make_interval(mins => d.minutes + 1)
FROM t67_doc d WHERE d.human <> 'none';

-- Đề xuất soát; đã quyết định thì người soát là Mai (B+3), trừ đề của chính Mai (không có ca nào quyết định đề của Mai).
INSERT INTO library_submission (id, org_id, doc_id, version_id, owner_user_id, status, reason, submitted_at, decided_by,
                                decided_at, library_id)
SELECT d.sub_id, d.org, d.doc, d.v1, d.owner_id, d.sub::library_submission_status,
       CASE WHEN d.sub = 'rejected' THEN 'Đáp số chưa khớp phần b' END,
       :'t0'::timestamptz - interval '30 minutes' + make_interval(secs => d.k),
       CASE WHEN d.sub <> 'pending' THEN pg_temp.s67f(d.b + 3) END,
       CASE WHEN d.sub <> 'pending' THEN :'t0'::timestamptz - interval '20 minutes' END,
       CASE WHEN d.sub = 'accepted' THEN d.lib END
FROM t67_doc d WHERE d.sub IS NOT NULL;

INSERT INTO library_problem (id, org_id, submission_id, source_doc_id, owner_user_id, title, statement, figure, kind, steps,
                             hint, machine_status, human_status, approved_code, accepted_by, accepted_at)
SELECT d.lib, d.org, d.sub_id, d.doc, d.owner_id, d.title, d.statement, d.figure::jsonb,
       CASE WHEN d.figure::jsonb ? 'plane' THEN 'plane' ELSE 'space' END::problem_kind, d.steps,
       'Xét tam giác chứa đại lượng cần tìm và các cạnh đã biết.', 'needs_review', 'teacher_approved', NULL,
       pg_temp.s67f(d.b + 3), :'t0'::timestamptz - interval '20 minutes' + make_interval(secs => d.k)
FROM t67_doc d WHERE d.sub = 'accepted';

-- TC-S2E6-033: Cô Trang đã thêm đủ 20 đề trong 60 phút qua.
INSERT INTO problem_create_event (id, org_id, user_id, channel, created_at)
SELECT pg_temp.s67f(s.b + 8000 + i), pg_temp.s67f(s.b + 1), pg_temp.s67f(s.b + 2), 'text',
       :'t0'::timestamptz - make_interval(mins => 30 - i)
FROM t67_slot s, generate_series(1, 20) i WHERE s.e = 6 AND s.tc = 33;

-- ---------------------------------------------------------------- bài giao (S2-E7 US-03; TC-S2E6-036)
CREATE TEMP TABLE t67_bg (e int, tc int, k int, class_k int, release_min int) ON COMMIT DROP;
INSERT INTO t67_bg SELECT 7, tc, 9001, 6, 120 FROM unnest(ARRAY[24, 25, 26, 29, 31, 32, 33]) tc;
INSERT INTO t67_bg SELECT 7, tc, 9002, 11, -60 FROM unnest(ARRAY[28, 31]) tc;
INSERT INTO t67_bg VALUES (7, 30, 9003, 12, NULL), (6, 36, 9001, 6, 120);

INSERT INTO assignment (id, org_id, kind, class_id, content_org_id, problem_id, problem_version_id, assigned_by, release_at,
                        allow_hint, allow_qa, created_at, release_rev)
SELECT pg_temp.s67f(s.b + g.k), pg_temp.s67f(s.b + 1), 'class', pg_temp.s67f(s.b + g.class_k), platform_org_id(),
       pg_temp.s67f(859999990), pg_temp.s67f(859999991), pg_temp.s67f(s.b + 2),
       CASE WHEN g.release_min IS NOT NULL THEN :'t0'::timestamptz + make_interval(mins => g.release_min) END,
       true, true, :'t0'::timestamptz - interval '3 hours', CASE WHEN g.release_min IS NULL THEN 0 ELSE 1 END
FROM t67_slot s JOIN t67_bg g ON g.e = s.e AND g.tc = s.tc;
