-- E1 · DỮ LIỆU VÀNG (artifacts/GEO/work/fixture.md, bản 22/11/2026): trường, người dùng, mã giáo viên (chỉ băm),
-- lớp, học sinh, sáu đề kho HKG-01…06 (đề, gợi ý, lời giải, nhãn máy), bài giao BG-1…5, BG-C, tiến độ + câu hỏi trên BG-1.
-- UUID theo app/db/seed/fixture-ids.json. Mốc giờ tương đối T0 (:'t0'). Không có mã giáo viên rõ ở đây (US-GEO-102-SC4):
-- verifier = HMAC-SHA256(pepper dev "dev-only-code-pepper-0123456789abcdef", mã viết hoa), selector = 4 ký tự đầu.

-- ---------------------------------------------------------------- nền tảng (dữ liệu tham chiếu của V10, bị TRUNCATE khi reset)
INSERT INTO org (id, parent_id, kind, name) VALUES ('00000000-0000-7000-8000-00000000f001', NULL, 'platform', 'Nền tảng GEO');
INSERT INTO app_user (id, org_id, role, account_kind, display_name)
VALUES ('00000000-0000-7000-8000-0000000000aa', '00000000-0000-7000-8000-00000000f001', 'teacher', 'system', 'Giáo viên mẫu'); -- F-GEO-008

-- ---------------------------------------------------------------- §1 trường, giáo viên, mã, lớp, học sinh
INSERT INTO org (id, parent_id, kind, name) VALUES
  ('00000000-0000-7000-8000-000000000001', '00000000-0000-7000-8000-00000000f001', 'school', 'THPT Mẫu A'),  -- F-GEO-001
  ('00000000-0000-7000-8000-000000000002', '00000000-0000-7000-8000-00000000f001', 'school', 'THCS Mẫu C');  -- F-GEO-002

INSERT INTO app_user (id, org_id, role, account_kind, display_name) VALUES
  ('00000000-0000-7000-8000-000000000003', '00000000-0000-7000-8000-000000000001', 'teacher', 'sample', 'Giáo viên mẫu'), -- F-GEO-003
  ('00000000-0000-7000-8000-000000000005', '00000000-0000-7000-8000-000000000002', 'teacher', 'sample', 'Giáo viên C');   -- F-GEO-005

INSERT INTO teacher_access_code (id, org_id, user_id, selector, verifier_hmac, pepper_kid, issued_at, expires_at, revoked_at) VALUES
  -- F-GEO-003: còn hạn, chưa thu hồi
  ('00000000-0000-7000-8000-000000300003', '00000000-0000-7000-8000-000000000001', '00000000-0000-7000-8000-000000000003', 'GQ7K',
   decode('a42a80e60433f28d58770fb260d011c96f123dce630d8ce81792193286cc3ed6', 'hex'), 1, :'t0'::timestamptz - interval '1 day', :'t0'::timestamptz + interval '180 days', NULL),
  -- F-GEO-005
  ('00000000-0000-7000-8000-000000300005', '00000000-0000-7000-8000-000000000002', '00000000-0000-7000-8000-000000000005', 'RW3N',
   decode('3beabfc3e557882e1b5688e2997fe6b85ff291fb0c597e8917a8d2ea2d88a2e4', 'hex'), 1, :'t0'::timestamptz - interval '1 day', :'t0'::timestamptz + interval '180 days', NULL),
  -- F-GEO-006: từng cấp cho F-GEO-003, đã thu hồi
  ('00000000-0000-7000-8000-000000300006', '00000000-0000-7000-8000-000000000001', '00000000-0000-7000-8000-000000000003', 'HX4Q',
   decode('02564170f1b33431521a478f5f0a47ada46105b2b2d33556c6fe0a018fb377a0', 'hex'), 1, :'t0'::timestamptz - interval '30 days', :'t0'::timestamptz + interval '150 days', :'t0'::timestamptz - interval '10 days'),
  -- F-GEO-007: cấp cho F-GEO-003, hết hạn lúc T0 − 1 phút
  ('00000000-0000-7000-8000-000000300007', '00000000-0000-7000-8000-000000000001', '00000000-0000-7000-8000-000000000003', 'T5BN',
   decode('d00f01d02bb8ad13b2d488f4dfc1d7b0b134a308516b52987e0fabf0ca3a50f8', 'hex'), 1, :'t0'::timestamptz - interval '30 days', :'t0'::timestamptz - interval '1 minute', NULL);

INSERT INTO class (id, org_id, name, level, grade, join_code, teacher_id) VALUES
  ('00000000-0000-7000-8000-000000000010', '00000000-0000-7000-8000-000000000001', '11A1', 'thpt', 11, 'HK7M2A', '00000000-0000-7000-8000-000000000003'),
  ('00000000-0000-7000-8000-000000000011', '00000000-0000-7000-8000-000000000001', '9B',   'thcs',  9, 'HK9B4T', '00000000-0000-7000-8000-000000000003'),
  ('00000000-0000-7000-8000-000000000012', '00000000-0000-7000-8000-000000000002', '9C',   'thcs',  9, 'MC9C6R', '00000000-0000-7000-8000-000000000005');

-- Học sinh: 11A1 (F-GEO-020…024), 9B (F-GEO-025), 9C (F-GEO-026; "An" cố ý trùng tên)
INSERT INTO app_user (id, org_id, role, account_kind, display_name)
SELECT s.id::uuid, s.org::uuid, 'student', 'light', s.name FROM (VALUES
  ('00000000-0000-7000-8000-000000000020', '00000000-0000-7000-8000-000000000001', 'An'),
  ('00000000-0000-7000-8000-000000000021', '00000000-0000-7000-8000-000000000001', 'Bình'),
  ('00000000-0000-7000-8000-000000000022', '00000000-0000-7000-8000-000000000001', 'Chi'),
  ('00000000-0000-7000-8000-000000000023', '00000000-0000-7000-8000-000000000001', 'Dũng'),
  ('00000000-0000-7000-8000-000000000024', '00000000-0000-7000-8000-000000000001', 'Hà'),
  ('00000000-0000-7000-8000-000000025001', '00000000-0000-7000-8000-000000000001', 'Khánh'),
  ('00000000-0000-7000-8000-000000025002', '00000000-0000-7000-8000-000000000001', 'Linh'),
  ('00000000-0000-7000-8000-000000025003', '00000000-0000-7000-8000-000000000001', 'Minh'),
  ('00000000-0000-7000-8000-000000025004', '00000000-0000-7000-8000-000000000001', 'Ngân'),
  ('00000000-0000-7000-8000-000000025005', '00000000-0000-7000-8000-000000000001', 'Phúc'),
  ('00000000-0000-7000-8000-000000026001', '00000000-0000-7000-8000-000000000002', 'An'),
  ('00000000-0000-7000-8000-000000026002', '00000000-0000-7000-8000-000000000002', 'Quân'),
  ('00000000-0000-7000-8000-000000026003', '00000000-0000-7000-8000-000000000002', 'Thảo')) AS s(id, org, name);

-- name_key = tên chuẩn hóa (không dấu, chữ thường, khoảng trắng gộp), như ứng dụng tính (iam NameKey)
INSERT INTO class_member (org_id, class_id, user_id, name_key)
SELECT u.org_id, c.id, u.id, lower(f_unaccent(u.display_name))
FROM app_user u
JOIN class c ON c.org_id = u.org_id AND c.id = CASE
       WHEN u.id::text LIKE '00000000-0000-7000-8000-00000000002_' THEN '00000000-0000-7000-8000-000000000010'::uuid
       WHEN u.id::text LIKE '00000000-0000-7000-8000-000000025%'  THEN '00000000-0000-7000-8000-000000000011'::uuid
       WHEN u.id::text LIKE '00000000-0000-7000-8000-000000026%'  THEN '00000000-0000-7000-8000-000000000012'::uuid END
WHERE u.role = 'student';

-- ---------------------------------------------------------------- §2 sáu đề kho (nguyên văn tệp nguồn), problem id = F-GEO-031…036
CREATE TEMP TABLE t_p (code text, n int, level edu_level, kind solid_kind, topic smallint, statement text, h1 text, h2 text,
                       answer text, steps jsonb) ON COMMIT DROP;
INSERT INTO t_p VALUES
 ('HKG-01', 31, 'thpt', 'pyramid', 1,
  'Cho hình chóp S.ABCD, đáy là hình vuông cạnh a, SA ⊥ (ABCD), SA = a. Tính góc giữa SD và mặt đáy.',
  'Góc giữa đường thẳng và mặt phẳng là góc giữa đường đó và hình chiếu của nó trên mặt phẳng.',
  'Hình chiếu của S lên mặt đáy là điểm nào? Từ đó suy ra hình chiếu của SD.', '45°',
  '[{"title":"Tìm hình chiếu của SD","body":"SA ⊥ (ABCD) nên A là hình chiếu của S. Hình chiếu của SD là AD.","why":"Muốn tìm góc giữa đường và mặt, việc đầu tiên là tìm hình chiếu của đường lên mặt."},
    {"title":"Xác định góc cần tìm","body":"Góc giữa SD và (ABCD) là góc SDA. Tam giác SAD vuông tại A.","why":"Có hình chiếu rồi thì góc cần tìm nằm trong một tam giác vuông."},
    {"title":"Tính góc","body":"tan SDA = SA / AD = a / a = 1, suy ra góc SDA = 45°.","why":"Trong tam giác vuông, tan của góc nhọn bằng cạnh đối chia cạnh kề."}]'),
 ('HKG-02', 32, 'thpt', 'cube', 2,
  'Cho hình lập phương ABCD.A′B′C′D′ cạnh a. Tính khoảng cách giữa AC và B′D′.',
  'AC và B′D′ nằm trong hai mặt đáy (ABCD) và (A′B′C′D′). Hai mặt này có quan hệ gì?',
  'Khoảng cách giữa hai đường chéo nhau nằm trong hai mặt phẳng song song bằng khoảng cách giữa hai mặt phẳng đó.', 'a',
  '[{"title":"Nhận xét vị trí","body":"AC ⊂ (ABCD), B′D′ ⊂ (A′B′C′D′), và hai mặt phẳng này song song.","why":"Hai đường chéo nhau thường được đưa về hai mặt phẳng song song chứa chúng."},
    {"title":"Tính khoảng cách","body":"Khoảng cách giữa hai mặt đáy bằng cạnh a. Vậy d(AC, B′D′) = a.","why":"Đoạn nối tâm hai đáy vuông góc với cả AC và B′D′."}]'),
 ('HKG-03', 33, 'thcs', 'prism', 3,
  'Lăng trụ đứng tam giác có đáy là tam giác đều cạnh 4 cm, chiều cao 6 cm. Tính thể tích.',
  'Thể tích lăng trụ bằng diện tích đáy nhân chiều cao.', 'Diện tích tam giác đều cạnh a là a²√3 / 4.', '24√3 cm³',
  '[{"title":"Tính diện tích đáy","body":"S = 4²·√3 / 4 = 4√3 cm².","why":"Đáy là tam giác đều nên dùng công thức a²√3 / 4."},
    {"title":"Tính thể tích","body":"V = S · h = 4√3 · 6 = 24√3 cm³.","why":"Lăng trụ đứng: V = diện tích đáy × chiều cao."}]'),
 ('HKG-04', 34, 'thcs', 'cone', 3,
  'Hình nón có bán kính đáy 3 cm, đường sinh 5 cm. Tính diện tích xung quanh và thể tích.',
  'Diện tích xung quanh hình nón: πrl.', 'Chiều cao tính từ tam giác vuông tạo bởi bán kính, chiều cao, đường sinh.',
  'Sxq = 15π cm²; V = 12π cm³',
  '[{"title":"Diện tích xung quanh","body":"Sxq = π·3·5 = 15π cm².","why":"Công thức πrl với l là đường sinh."},
    {"title":"Thể tích","body":"h = √(5² − 3²) = 4 cm. V = ⅓·π·3²·4 = 12π cm³.","why":"Cần chiều cao trước khi tính thể tích."}]'),
 ('HKG-05', 35, 'thcs', 'cylinder', 4,
  'Hình trụ có chiều cao 10 cm, bán kính đáy 4 cm. Tính diện tích toàn phần.',
  'Diện tích toàn phần gồm diện tích xung quanh và hai đáy.', 'Stp = 2πr(r + h).', '112π cm²',
  '[{"title":"Áp dụng công thức","body":"Stp = 2π·4·(4 + 10) = 112π cm².","why":"Gộp diện tích xung quanh 2πrh và hai đáy 2πr²."}]'),
 ('HKG-06', 36, 'thpt', 'sphere', 5,
  'Mặt cầu ngoại tiếp hình lập phương cạnh a. Tính bán kính và thể tích khối cầu.',
  'Tâm mặt cầu là tâm hình lập phương.', 'Bán kính bằng nửa đường chéo của hình lập phương.', 'R = a√3/2; V = (√3/2)·π·a³',
  '[{"title":"Bán kính","body":"Đường chéo hình lập phương bằng a√3, nên R = a√3 / 2.","why":"Mặt cầu ngoại tiếp đi qua cả 8 đỉnh."},
    {"title":"Thể tích","body":"V = 4/3·π·R³ = (√3/2)·π·a³.","why":"Thay R vào công thức thể tích khối cầu."}]');

INSERT INTO problem (id, org_id, subject_id, source, author_user_id, level, solid_kind, topic_id, title, code, created_at)
SELECT format('00000000-0000-7000-8000-%s', lpad(p.n::text, 12, '0'))::uuid, platform_org_id(), 1, 'catalog',
       '00000000-0000-7000-8000-0000000000aa', p.level, p.kind, p.topic, p.code, p.code, :'t0'::timestamptz - interval '30 days'
FROM t_p p;
INSERT INTO problem_version (id, org_id, problem_id, version, statement, statement_hash, created_by, created_at)
SELECT format('00000000-0000-7000-8000-%s', lpad((100000 + p.n)::text, 12, '0'))::uuid, platform_org_id(),
       format('00000000-0000-7000-8000-%s', lpad(p.n::text, 12, '0'))::uuid, 1, p.statement,
       digest(regexp_replace(btrim(p.statement), '\s+', ' ', 'g'), 'sha256'),
       '00000000-0000-7000-8000-0000000000aa', :'t0'::timestamptz - interval '30 days'
FROM t_p p;
UPDATE problem pr SET current_version_id = format('00000000-0000-7000-8000-%s', lpad((100000 + p.n)::text, 12, '0'))::uuid
FROM t_p p WHERE pr.org_id = platform_org_id() AND pr.code = p.code;
INSERT INTO hint (org_id, problem_version_id, level, body)
SELECT platform_org_id(), format('00000000-0000-7000-8000-%s', lpad((100000 + p.n)::text, 12, '0'))::uuid, l.level,
       CASE l.level WHEN 1 THEN p.h1 ELSE p.h2 END
FROM t_p p CROSS JOIN (VALUES (1::smallint), (2::smallint)) AS l(level);
INSERT INTO solution (org_id, problem_version_id, final_answer, steps, model, prompt_version, author_user_id, ai_authored)
SELECT platform_org_id(), format('00000000-0000-7000-8000-%s', lpad((100000 + p.n)::text, 12, '0'))::uuid, p.answer, p.steps,
       'gia-lap-1', 'p1', '00000000-0000-7000-8000-0000000000aa', true
FROM t_p p;
-- Bản dựng hình (SceneSpec, a = 1): dữ liệu hình mang số phiên bản lược đồ (US-GEO-104-SC5); tọa độ là dữ kiện, không phải đáp số
INSERT INTO scene_build (id, org_id, problem_version_id, build_no, coord_source, schema_version, spec, coords, hidden_measures)
SELECT format('00000000-0000-7000-8000-%s', lpad((300000 + p.n)::text, 12, '0'))::uuid, platform_org_id(),
       format('00000000-0000-7000-8000-%s', lpad((100000 + p.n)::text, 12, '0'))::uuid, 1, 'engine', 1,
       jsonb_build_object('schema_version', 1, 'solid', p.kind::text, 'params', jsonb_build_object('a', 1)),
       CASE p.code
         WHEN 'HKG-01' THEN '{"A":[0,0,0],"B":[1,0,0],"C":[1,1,0],"D":[0,1,0],"S":[0,0,1]}'::jsonb
         WHEN 'HKG-02' THEN '{"A":[0,0,0],"B":[1,0,0],"C":[1,1,0],"D":[0,1,0],"A′":[0,0,1],"B′":[1,0,1],"C′":[1,1,1],"D′":[0,1,1]}'::jsonb
         WHEN 'HKG-03' THEN '{"A":[0,0,0],"B":[4,0,0],"C":[2,3.4641016151,0],"A′":[0,0,6],"B′":[4,0,6],"C′":[2,3.4641016151,6]}'::jsonb
         WHEN 'HKG-04' THEN '{"O":[0,0,0],"S":[0,0,4],"M":[3,0,0]}'::jsonb
         WHEN 'HKG-05' THEN '{"O":[0,0,0],"O′":[0,0,10],"M":[4,0,0],"M′":[4,0,10]}'::jsonb
         ELSE '{"A":[0,0,0],"B":[1,0,0],"C":[1,1,0],"D":[0,1,0],"A′":[0,0,1],"B′":[1,0,1],"C′":[1,1,1],"D′":[0,1,1],"I":[0.5,0.5,0.5]}'::jsonb
       END,
       CASE p.code WHEN 'HKG-01' THEN ARRAY['góc SDA', 'góc ASD'] ELSE '{}'::text[] END
FROM t_p p;

-- Nhãn máy như sau một lần chạy engine (fixture §2: cả sáu "Đã kiểm chứng"; nhãn trong tệp nguồn KHÔNG dùng, D-GEO-004)
INSERT INTO verification_report (id, org_id, problem_version_id, scene_ok, answer_ok, result, checks)
SELECT format('00000000-0000-7000-8000-%s', lpad((200000 + p.n)::text, 12, '0'))::uuid, platform_org_id(),
       format('00000000-0000-7000-8000-%s', lpad((100000 + p.n)::text, 12, '0'))::uuid, true, true, 'verified',
       '{"source":"seed E1: kết quả engine kỳ vọng của fixture"}'
FROM t_p p;
UPDATE problem_version pv SET verify_status = 'verified',
       verification_report_id = format('00000000-0000-7000-8000-%s', lpad((200000 + p.n)::text, 12, '0'))::uuid
FROM t_p p WHERE pv.org_id = platform_org_id() AND pv.id = format('00000000-0000-7000-8000-%s', lpad((100000 + p.n)::text, 12, '0'))::uuid;

-- ---------------------------------------------------------------- §3 bài giao (F-GEO-040…045); created_at trước mọi giờ mở
INSERT INTO assignment (id, org_id, kind, class_id, content_org_id, problem_id, problem_version_id, assigned_by, release_at,
                        allow_hint, allow_qa, created_at)
SELECT a.id::uuid, a.org::uuid, 'class', a.cls::uuid, platform_org_id(),
       format('00000000-0000-7000-8000-%s', lpad(a.pn::text, 12, '0'))::uuid,
       format('00000000-0000-7000-8000-%s', lpad((100000 + a.pn)::text, 12, '0'))::uuid,
       a.by::uuid, a.rel, a.hint, a.qa, :'t0'::timestamptz - interval '2 days' + a.k * interval '1 minute'
FROM (VALUES
  (1, '00000000-0000-7000-8000-000000000040', '00000000-0000-7000-8000-000000000001', '00000000-0000-7000-8000-000000000010', 31, '00000000-0000-7000-8000-000000000003', :'t0'::timestamptz + interval '60 minutes', true, true),
  (2, '00000000-0000-7000-8000-000000000041', '00000000-0000-7000-8000-000000000001', '00000000-0000-7000-8000-000000000010', 32, '00000000-0000-7000-8000-000000000003', NULL::timestamptz, true, true),
  (3, '00000000-0000-7000-8000-000000000042', '00000000-0000-7000-8000-000000000001', '00000000-0000-7000-8000-000000000010', 33, '00000000-0000-7000-8000-000000000003', :'t0'::timestamptz - interval '60 minutes', true, true),
  (4, '00000000-0000-7000-8000-000000000043', '00000000-0000-7000-8000-000000000001', '00000000-0000-7000-8000-000000000011', 34, '00000000-0000-7000-8000-000000000003', :'t0'::timestamptz + interval '60 seconds', true, true),
  (5, '00000000-0000-7000-8000-000000000044', '00000000-0000-7000-8000-000000000001', '00000000-0000-7000-8000-000000000011', 35, '00000000-0000-7000-8000-000000000003', NULL::timestamptz, false, false),
  (6, '00000000-0000-7000-8000-000000000045', '00000000-0000-7000-8000-000000000002', '00000000-0000-7000-8000-000000000012', 34, '00000000-0000-7000-8000-000000000005', NULL::timestamptz, true, true)
) AS a(k, id, org, cls, pn, by, rel, hint, qa);
UPDATE assignment SET release_rev = 1 WHERE release_at IS NOT NULL;

-- ---------------------------------------------------------------- §4 tiến độ 11A1 trên BG-1 (F-GEO-050…054) và của An trên BG-3
INSERT INTO assignment_progress (org_id, assignment_id, user_id, first_opened_at, last_active_at, hint_level_max, hint_view_count,
                                 ask_count, tool_count, active_seconds, solution_viewed_at)
SELECT '00000000-0000-7000-8000-000000000001', p.a::uuid, p.u::uuid, :'t0'::timestamptz - p.mins * interval '1 minute' - interval '5 minutes',
       :'t0'::timestamptz - interval '5 minutes', p.hint, p.hint, p.asks, p.tools, p.mins * 60, p.sol
FROM (VALUES
  ('00000000-0000-7000-8000-000000000040', '00000000-0000-7000-8000-000000000020', 2::smallint, 3, 4, 18, NULL::timestamptz), -- An
  ('00000000-0000-7000-8000-000000000040', '00000000-0000-7000-8000-000000000021', 1::smallint, 2, 2, 11, NULL::timestamptz), -- Bình
  ('00000000-0000-7000-8000-000000000040', '00000000-0000-7000-8000-000000000022', 0::smallint, 0, 6,  9, NULL::timestamptz), -- Chi
  ('00000000-0000-7000-8000-000000000040', '00000000-0000-7000-8000-000000000023', 2::smallint, 2, 3, 21, NULL::timestamptz), -- Dũng
  ('00000000-0000-7000-8000-000000000042', '00000000-0000-7000-8000-000000000020', 2::smallint, 0, 0, 12, :'t0'::timestamptz - interval '30 minutes') -- An, BG-3: 3/3
) AS p(a, u, hint, asks, tools, mins, sol);
-- Hà (F-GEO-054) chưa mở BG-1: không có hàng tiến độ.

-- Câu hỏi trên BG-1 (F-GEO-055…058): An 3, Bình 2, Dũng 2 (= 7)
INSERT INTO qa_turn (org_id, assignment_id, user_id, request_id, asked_at, question, reply_kind, reply_text, guard_version)
SELECT '00000000-0000-7000-8000-000000000001', '00000000-0000-7000-8000-000000000040', q.u::uuid,
       format('00000000-0000-7000-8000-%s', lpad((550000 + q.k)::text, 12, '0'))::uuid,
       :'t0'::timestamptz - (30 - q.k) * interval '1 minute', q.question, q.kind::qa_reply_kind, q.reply, 'g0-e1'
FROM (VALUES
  (1, '00000000-0000-7000-8000-000000000020', 'Vì sao A là hình chiếu của S?', 'concept', 'Vì SA vuông góc với mặt đáy nên chân đường vuông góc hạ từ S xuống mặt đáy là A (nguồn: Giáo án mẫu).'),
  (2, '00000000-0000-7000-8000-000000000023', 'Vì sao A là hình chiếu của S?', 'concept', 'Đường thẳng SA vuông góc với mặt phẳng đáy tại A, nên A là hình chiếu của S (nguồn: Giáo án mẫu).'),
  (3, '00000000-0000-7000-8000-000000000021', 'Góc cần tìm là góc nào trên hình?', 'hint', 'Em hãy tìm hình chiếu của SD lên mặt đáy trước.'),
  (4, '00000000-0000-7000-8000-000000000021', 'Góc cần tìm là góc nào trên hình?', 'hint', 'Góc giữa đường và mặt là góc giữa đường đó và hình chiếu của nó.'),
  (5, '00000000-0000-7000-8000-000000000020', 'Góc cần tìm là góc nào trên hình?', 'hint', 'Em xem lại Gợi ý 2 nhé.'),
  (6, '00000000-0000-7000-8000-000000000020', 'Cho em xin đáp số luôn được không?', 'refused_answer', 'Thầy cô chưa mở lời giải. Em thử làm theo Gợi ý 2 nhé.'),
  (7, '00000000-0000-7000-8000-000000000023', 'Tan của một góc là gì?', 'concept', 'Trong tam giác vuông, tan của góc nhọn bằng cạnh đối chia cạnh kề (Tỉ số lượng giác trong tam giác vuông, AI soạn).')
) AS q(k, u, question, kind, reply);
