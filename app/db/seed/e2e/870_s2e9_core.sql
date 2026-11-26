-- S2-E9 (T-261126-25) · dữ liệu cho ca kiểm Biến thể đề, Xuất ảnh/PDF, Liên kết chỉ xem (US-S2E9-01, 02).
-- Chạy sau 800…850; không sửa dữ liệu của epic khác. UUID theo PLAN_code-s23 §4: F-S2-nnn → 00000000-0000-7000-8002-<12 số>,
-- khoảng 87xxxxxxx của task (fixture-ids.d/s2e9.json). Câu trắc nghiệm (US-S2E9-03) dùng móc /test-hooks/s2e8/sandbox
-- và đề luyện của seed 830, không cần dữ liệu ở đây.
--
-- Mỗi ca làm BẨN dữ liệu (thêm đề, liên kết, giới hạn thêm đề theo tài khoản) nên mỗi (TC, làn) có MỘT tổ chức riêng:
--   gốc B = 870000000 + TC × 100000 + làn × 10000      (TC 1…28; làn 0…5 = api, ui-chromium, ui-webkit, ui-iphone,
--                                                        mobile-iphone, mobile-pixel; IT dùng làn 0)
--   B+1 tổ chức · B+2 Cô Trang (chủ đề) · B+4 Cô Lan · B+5 An (học sinh) · B+6 lớp 11A1
--   đề (solution_doc) B+k · phiên bản 1 của đề B+k+5000 · bài giao BG-1 B+9001 (chỉ TC 18) · liên kết B+7001…7004 (TC 25)
--   k: 100 FX-10 · 101 FX-11 · 102 FX-12 · 103 FX-13 · 104 FX-14 (không gian, "Đã kiểm chứng") · 105 đề thuần chữ ·
--      106 đề thiết diện động · 107 FX-11 lời giải "AI soạn" chưa duyệt · 108 hình vuông cạnh 1 · 109 hình vuông cạnh 100 ·
--      110 đề miền hẹp (đúng 4 bộ khác gốc) · 200…224 "Đề số 1…25" (chỉ TC 19)

CREATE OR REPLACE FUNCTION pg_temp.s9f(n bigint) RETURNS uuid LANGUAGE sql IMMUTABLE
AS $$ SELECT format('00000000-0000-7000-8002-%s', lpad(n::text, 12, '0'))::uuid $$;
CREATE OR REPLACE FUNCTION pg_temp.s9jc(n bigint) RETURNS text LANGUAGE sql IMMUTABLE
AS $$ SELECT 'Y9' || string_agg(substr('ABCDEFGHJKLMNPQRSTUVWXYZ23456789', ((n / (32 ^ i)::bigint) % 32)::int + 1, 1), '' ORDER BY i)
      FROM generate_series(0, 3) i $$;

CREATE TEMP TABLE t9_slot ON COMMIT DROP AS
SELECT tc, l, (870000000 + tc * 100000 + l * 10000)::bigint AS b
FROM generate_series(1, 28) tc, generate_series(0, 5) l;

INSERT INTO org (id, parent_id, kind, name)
SELECT pg_temp.s9f(b + 1), '00000000-0000-7000-8000-00000000f001', 'group', format('S2-E9 TC %s làn %s', tc, l)
FROM t9_slot;

INSERT INTO app_user (id, org_id, role, account_kind, display_name)
SELECT pg_temp.s9f(s.b + u.k), pg_temp.s9f(s.b + 1), u.role::user_role, u.kind::account_kind, u.name
FROM t9_slot s,
     (VALUES (2, 'teacher', 'verified', 'Cô Trang'), (4, 'teacher', 'verified', 'Cô Lan'),
             (5, 'student', 'light', 'An')) u(k, role, kind, name);

INSERT INTO class (id, org_id, name, level, grade, join_code, teacher_id)
SELECT pg_temp.s9f(b + 6), pg_temp.s9f(b + 1), '11A1', 'thpt', 11, pg_temp.s9jc(b / 10000), pg_temp.s9f(b + 2)
FROM t9_slot;
INSERT INTO class_member (org_id, class_id, user_id, name_key)
SELECT pg_temp.s9f(b + 1), pg_temp.s9f(b + 6), pg_temp.s9f(b + 5), 'an' FROM t9_slot;

-- ---------------------------------------------------------------- đề mẫu
-- human: appr = "Giáo viên đã duyệt" · ai = "AI soạn" · none = chưa có lời giải.
CREATE TEMP TABLE t9_fx (k int, title text, statement text, figure text, steps text, human text, minutes int) ON COMMIT DROP;
INSERT INTO t9_fx VALUES
  (100, 'PL-01 Góc A của tam giác 5, 12, 13', 'Tam giác ABC có AB = 5, AC = 12, BC = 13. Tính góc A.',
   '{"plane":{"shape":"triangle","given":[{"type":"length","of":["A","B"],"value":5},{"type":"length","of":["A","C"],"value":12},{"type":"length","of":["B","C"],"value":13}],"asked":{"name":"góc A","kind":"angle","of":["B","A","C"]}}}',
   '["AB² + AC² = 25 + 144 = 169 = BC².", "Vậy tam giác ABC vuông tại A, góc A = 90°."]', 'appr', -300),
  (101, 'PL-02 Tam giác cân có góc A = 40°', 'Tam giác ABC cân tại A, góc A = 40°. Tính góc B.',
   '{"plane":{"shape":"isosceles_triangle","at":"A","given":[{"type":"angle","of":["B","A","C"],"value":40}],"asked":{"name":"góc B","kind":"angle","of":["A","B","C"]}}}',
   '["Tam giác ABC cân tại A nên góc B = góc C.", "Góc B = (180° − 40°) : 2 = 70°."]', 'appr', -290),
  (102, 'PL-03 Đường chéo hình chữ nhật', 'Hình chữ nhật ABCD có AB = 6, AD = 8. Tính AC.',
   '{"plane":{"shape":"rectangle","given":[{"type":"length","of":["A","B"],"value":6},{"type":"length","of":["A","D"],"value":8}],"asked":{"name":"AC","kind":"length","of":["A","C"]}}}',
   '["AC² = AB² + BC² = 36 + 64 = 100.", "Vậy AC = 10."]', 'appr', -280),
  (103, 'PL-04 Diện tích tam giác đều cạnh 4', 'Tam giác đều ABC cạnh 4. Tính diện tích.',
   '{"plane":{"shape":"equilateral_triangle","given":[{"type":"length","of":["A","B"],"value":4}],"asked":{"name":"S","kind":"area","of":[]}}}',
   '["Đường cao h = 4√3 : 2 = 2√3.", "Diện tích S = 4 · 2√3 : 2 = 4√3 ≈ 6,93."]', 'appr', -270),
  (104, 'SP-01 Góc giữa SC và mặt đáy',
   'Cho hình chóp S.ABCD có đáy ABCD là hình vuông cạnh a, SA vuông góc với mặt phẳng (ABCD) và SA = a√2. Tính góc giữa đường thẳng SC và mặt đáy.',
   '{"catalogCode": "HKN-02"}',
   '["SA ⊥ (ABCD) nên AC là hình chiếu của SC, góc cần tìm là SCA.", "ABCD vuông cạnh a nên AC = a√2.", "tan SCA = SA / AC = a√2 / a√2 = 1.", "Vậy góc giữa SC và mặt phẳng đáy bằng 45°."]',
   'appr', -260),
  (105, 'Chứng minh tam giác cân', 'Cho tam giác ABC có hai đường cao BH và CK bằng nhau. Chứng minh tam giác ABC cân.',
   NULL, NULL, 'none', -250),
  (106, 'Thiết diện động của hình chóp',
   'Cho hình chóp S.ABCD có đáy là hình vuông cạnh 6, SA = 8. Điểm M di động trên cạnh SA. Tìm thiết diện của hình chóp cắt bởi mặt phẳng (MBC).',
   '{"section": {"moving": "M", "on": ["S", "A"]}}', NULL, 'none', -240),
  (107, 'PL-02 bản AI soạn', 'Tam giác ABC cân tại A, góc A = 40°. Tính góc B.',
   '{"plane":{"shape":"isosceles_triangle","at":"A","given":[{"type":"angle","of":["B","A","C"],"value":40}],"asked":{"name":"góc B","kind":"angle","of":["A","B","C"]}}}',
   '["Góc B = (180° − 40°) : 2 = 70°."]', 'ai', -230),
  (108, 'Hình vuông cạnh 1', 'Hình vuông ABCD có AB = 1. Tính AC.',
   '{"plane":{"shape":"square","given":[{"type":"length","of":["A","B"],"value":1}],"asked":{"name":"AC","kind":"length","of":["A","C"]}}}',
   NULL, 'none', -220),
  (109, 'Hình vuông cạnh 100', 'Hình vuông ABCD có AB = 100. Tính AC.',
   '{"plane":{"shape":"square","given":[{"type":"length","of":["A","B"],"value":100}],"asked":{"name":"AC","kind":"length","of":["A","C"]}}}',
   NULL, 'none', -210),
  (110, 'Hình chữ nhật miền hẹp', 'Hình chữ nhật ABCD có AB = 5, AD = 12. Tính AC.',
   '{"plane":{"shape":"rectangle","given":[{"type":"length","of":["A","B"],"value":5,"min":3,"max":7},{"type":"length","of":["A","D"],"value":12,"min":12,"max":12}],"asked":{"name":"AC","kind":"length","of":["A","C"]}}}',
   NULL, 'none', -200);

CREATE TEMP TABLE t9_doc ON COMMIT DROP AS
SELECT s.tc, s.l, s.b, x.k, pg_temp.s9f(s.b + x.k) AS doc, pg_temp.s9f(s.b + x.k + 5000) AS v1,
       pg_temp.s9f(s.b + 1) AS org, pg_temp.s9f(s.b + 2) AS owner_id, x.title, x.statement, x.figure, x.steps, x.human,
       x.minutes
FROM t9_slot s, t9_fx x;
-- TC 19: Cô Trang có 25 đề để xuất 1 / 20 / 21 đề.
INSERT INTO t9_doc
SELECT s.tc, s.l, s.b, 199 + i, pg_temp.s9f(s.b + 199 + i), pg_temp.s9f(s.b + 199 + i + 5000), pg_temp.s9f(s.b + 1),
       pg_temp.s9f(s.b + 2), format('Đề số %s', i), format('Tam giác ABC có AB = %s, AC = 4, BC = 5. Tính góc A.', i + 2),
       NULL, NULL, 'none', -100 + i
FROM t9_slot s, generate_series(1, 25) i WHERE s.tc = 19;

-- Đề kho SP-01 của seed 850 (F-S2-859999990/…991) gắn với FX-14 ở TC 18 để bài giao BG-1 khóa lời giải của đề này.
INSERT INTO catalog_bank_access (org_id, bank) SELECT pg_temp.s9f(b + 1), 's2e67_present' FROM t9_slot WHERE tc = 18;

INSERT INTO solution_doc (id, org_id, owner_user_id, code, title, statement, figure, content_org_id, problem_version_id,
                          in_my_shelf, latest_version, created_at)
SELECT d.doc, d.org, d.owner_id, NULL, d.title, d.statement, d.figure::jsonb,
       CASE WHEN d.tc = 18 AND d.k = 104 THEN platform_org_id() END,
       CASE WHEN d.tc = 18 AND d.k = 104 THEN pg_temp.s9f(859999991) END,
       d.human = 'appr', CASE WHEN d.human = 'none' THEN 0 ELSE 1 END,
       :'t0'::timestamptz + make_interval(mins => d.minutes)
FROM t9_doc d;

INSERT INTO solution_version (id, org_id, doc_id, version, steps, ai_authored, author_user_id, note, human_status,
                              approved_by, approved_at, created_at)
SELECT d.v1, d.org, d.doc, 1, d.steps::jsonb, d.human = 'ai', CASE WHEN d.human = 'ai' THEN NULL ELSE d.owner_id END,
       CASE WHEN d.human = 'ai' THEN 'AI soạn' ELSE 'bản duyệt' END,
       CASE WHEN d.human = 'ai' THEN 'ai_drafted' ELSE 'teacher_approved' END::solution_human_status,
       CASE WHEN d.human = 'appr' THEN d.owner_id END,
       CASE WHEN d.human = 'appr' THEN :'t0'::timestamptz + make_interval(mins => d.minutes + 5) END,
       :'t0'::timestamptz + make_interval(mins => d.minutes + 1)
FROM t9_doc d WHERE d.human <> 'none';

-- FX-14: nhãn máy "Đã kiểm chứng" (mọi bước Đạt, đáp số khớp engine) — trigger chỉ nhận 'verified' khi có kết quả kiểm.
INSERT INTO step_check_result (org_id, version_id, step_no, status, reason)
SELECT d.org, d.v1, n, 'pass', 'Khớp hình engine dựng' FROM t9_doc d, generate_series(1, 4) n WHERE d.k = 104;
UPDATE solution_version v SET final_state = 'match', machine_status = 'verified',
                              checked_at = :'t0'::timestamptz - interval '20 minutes'
FROM t9_doc d WHERE d.k = 104 AND v.org_id = d.org AND v.id = d.v1;

-- TC 12: Cô Trang đã thêm 18 đề trong 60 phút qua (giới hạn 20, BR-S2E6-13).
INSERT INTO problem_create_event (id, org_id, user_id, channel, created_at)
SELECT pg_temp.s9f(s.b + 8000 + i), pg_temp.s9f(s.b + 1), pg_temp.s9f(s.b + 2), 'text',
       :'t0'::timestamptz - make_interval(mins => 30 - i)
FROM t9_slot s, generate_series(1, 18) i WHERE s.tc = 12;

-- TC 18: BG-1 giao FX-14 (đề kho SP-01) cho 11A1, lời giải khóa tới T0 + 2 giờ.
INSERT INTO assignment (id, org_id, kind, class_id, content_org_id, problem_id, problem_version_id, assigned_by, release_at,
                        allow_hint, allow_qa, created_at, release_rev)
SELECT pg_temp.s9f(b + 9001), pg_temp.s9f(b + 1), 'class', pg_temp.s9f(b + 6), platform_org_id(),
       pg_temp.s9f(859999990), pg_temp.s9f(859999991), pg_temp.s9f(b + 2), :'t0'::timestamptz + interval '2 hours',
       true, true, :'t0'::timestamptz - interval '3 hours', 1
FROM t9_slot WHERE tc = 18;

-- TC 25: FX-10 đã có 4 liên kết đang dùng (mã của seed không ai biết: chỉ lưu băm).
INSERT INTO problem_share_link (id, org_id, doc_id, created_by, token_hash, token_hint, days, created_at, expires_at)
SELECT pg_temp.s9f(s.b + 7000 + i), pg_temp.s9f(s.b + 1), pg_temp.s9f(s.b + 100), pg_temp.s9f(s.b + 2),
       digest(format('s2e9-seed-%s-%s', s.b, i), 'sha256'), 'seed', 7, :'t0'::timestamptz - interval '1 hour',
       :'t0'::timestamptz + interval '7 days'
FROM t9_slot s, generate_series(1, 4) i WHERE s.tc = 25;
