-- Dữ liệu thử tải (brief §6 câu 5, giả định): 50 trường, 600 lớp, 20.000 học sinh, ~2 triệu sự kiện.
-- Chạy sau 03_schema.sql, trên geo_arch2:  psql -v ON_ERROR_STOP=1 -d geo_arch2 -f bench/01_seed.sql
-- Lượt sửa sau RV: tác giả nền tảng có sẵn trong lược đồ (D-GEO-160); nhãn máy đặt qua báo cáo kiểm chứng (D-GEO-158);
-- mỗi lớp không giao trùng problem (D-GEO-161); chữ ký chuẩn (D-GEO-150); mã giáo viên lưu băm (D-GEO-152); câu trả lời hỏi đáp (D-GEO-159).
-- Phân bố: 30 lớp lớn (~60 em), còn lại 18-45 em; bài giao 4-12 mỗi lớp trong 90 ngày; sự kiện tập trung 19-21 giờ
-- (50% sự kiện) để mô phỏng đỉnh buổi tối. Dữ liệu sinh bằng SQL, không dùng tên người hay tổ chức thật.
\timing on
SET search_path = geo, public;
SELECT setseed(0.42);
SET synchronous_commit = off;
SET maintenance_work_mem = '512MB';

-- ---------- tổ chức: 5 nhóm x 10 trường
CREATE TEMP TABLE t_org AS
SELECT n, geo.uuid_v7(now() - interval '200 days' + n * interval '1 minute') AS id
FROM generate_series(1, 55) n;           -- 1..5 nhóm, 6..55 trường
INSERT INTO org (id, parent_id, kind, name)
SELECT o.id, '00000000-0000-7000-8000-000000000001', 'group', 'Nhóm trường mẫu ' || o.n FROM t_org o WHERE o.n <= 5;
INSERT INTO org (id, parent_id, kind, name)
SELECT o.id, g.id, 'school', 'Trường mẫu ' || (o.n - 5)
FROM t_org o JOIN t_org g ON g.n = 1 + ((o.n - 6) / 10) WHERE o.n > 5;

-- ---------- giáo viên: 4 Giáo viên mẫu mỗi trường (tác giả nền tảng '...00aa' đã có trong lược đồ, D-GEO-160)
CREATE TEMP TABLE t_teacher AS
SELECT s.n AS school_n, t.k, geo.uuid_v7(now() - interval '190 days' + (s.n * 10 + t.k) * interval '1 second') AS id, s.id AS org_id
FROM t_org s CROSS JOIN generate_series(1, 4) t(k) WHERE s.n > 5;
INSERT INTO app_user (id, org_id, role, account_kind, display_name)
SELECT id, org_id, 'teacher', 'sample', 'Giáo viên mẫu ' || school_n || '.' || k FROM t_teacher;
-- mã Giáo viên mẫu theo tài khoản (D-GEO-152): selector = 4 ký tự đầu của mã + HMAC của cả mã (dữ liệu thử: băm của chuỗi xác định)
INSERT INTO teacher_access_code (id, org_id, user_id, selector, verifier_hmac, pepper_kid, expires_at)
SELECT geo.uuid_v7(), org_id, id,
       (SELECT string_agg(substr('ABCDEFGHIJKLMNOPQRSTUVWXYZ234567', (1 + ((x.n * 7919) / (32 ^ d)::int) % 32)::int, 1), '' ORDER BY d DESC) FROM generate_series(0, 3) d),
       digest('secret-' || id::text, 'sha256'), 1, now() + interval '180 days'
FROM (SELECT t.*, row_number() OVER (ORDER BY t.id) AS n FROM t_teacher t) x;
-- danh mục hình mẫu (D-GEO-133): dữ liệu thử, nội dung thật do geo_content nạp theo US-GEO-205
INSERT INTO solid_template (id, code, solid_kind, name, param_spec)
SELECT n, 'tpl_' || k, k::solid_kind, 'Hình mẫu ' || k, jsonb_build_object('schema_version', 1, 'params', jsonb_build_array('a', 'h'), 'defaults', jsonb_build_object('a', 1, 'h', 1))
FROM unnest(ARRAY['pyramid','prism','box','cube','cone','cylinder','sphere']) WITH ORDINALITY AS t(k, n);

-- ---------- lớp: 600, mỗi trường 12
CREATE TEMP TABLE t_class AS
SELECT i, ((i - 1) % 50) + 6 AS school_n,
       geo.uuid_v7(now() - interval '180 days' + i * interval '1 second') AS id,
       (CASE WHEN i <= 30 THEN 55 + random() * 10 ELSE 18 + random() * 28 END) AS raw_sz
FROM generate_series(1, 600) i;
CREATE TEMP TABLE t_class2 AS
SELECT c.i, c.id, o.id AS org_id, tt.id AS teacher_id,
       round(c.raw_sz * 20000 / (SELECT sum(raw_sz) FROM t_class))::int AS sz,
       (CASE WHEN c.i % 3 = 0 THEN 'thcs' ELSE 'thpt' END)::edu_level AS level
FROM t_class c
JOIN t_org o ON o.n = c.school_n
JOIN t_teacher tt ON tt.school_n = c.school_n AND tt.k = 1 + (((c.i - 1) / 50) % 4);
ALTER TABLE t_class2 ADD COLUMN cum_end int;
UPDATE t_class2 t SET cum_end = x.ce FROM (SELECT i, sum(sz) OVER (ORDER BY i) ce FROM t_class2) x WHERE x.i = t.i;
UPDATE t_class2 SET cum_end = 20000 WHERE i = 600 AND cum_end < 20000;
INSERT INTO class (id, org_id, name, level, grade, join_code, teacher_id)
SELECT id, org_id,
       (CASE WHEN level = 'thcs' THEN '9' ELSE '11' END) || 'A' || i,
       level, CASE WHEN level = 'thcs' THEN 9 ELSE 11 END,
       -- mã lớp 6 ký tự A-Z2-9 xác định theo i
       translate(upper(substr(md5('lop' || i), 1, 6)), '01', 'HK'),
       teacher_id
FROM t_class2;

-- ---------- học sinh 20.000 và thành viên lớp
CREATE TEMP TABLE t_student AS
SELECT j, geo.uuid_v7(now() - interval '170 days' + j * interval '1 second') AS id, c.id AS class_id, c.org_id, c.i AS class_i
FROM generate_series(1, 20000) j
JOIN LATERAL (SELECT * FROM t_class2 WHERE cum_end >= j ORDER BY i LIMIT 1) c ON true;
INSERT INTO app_user (id, org_id, role, account_kind, display_name)
SELECT id, org_id, 'student', 'light', 'Học sinh ' || j FROM t_student;
INSERT INTO class_member (org_id, class_id, user_id, name_key, joined_at)
SELECT org_id, class_id, id, 'hoc sinh ' || j, now() - interval '160 days' FROM t_student;

-- ---------- kho đề dùng chung: 60 đề (org = platform) và 8.000 đề tự nhập
CREATE TEMP TABLE t_problem AS
SELECT n, geo.uuid_v7(now() - interval '150 days' + n * interval '1 second') AS id,
       geo.uuid_v7(now() - interval '149 days' + n * interval '1 second') AS vid,
       CASE WHEN n <= 60 THEN 'catalog' ELSE 'student_input' END::problem_source AS source,
       (n <= 60) AS is_cat,
       (CASE WHEN n % 2 = 0 THEN 'thpt' ELSE 'thcs' END)::edu_level AS level,
       (ARRAY['pyramid','prism','box','cube','cone','cylinder','sphere'])[1 + (n % 7)]::solid_kind AS sk,
       1 + (n % 5) AS topic
FROM generate_series(1, 8060) n;
-- chủ sở hữu đề tự nhập: học sinh thứ (n-60)*2
INSERT INTO problem (id, org_id, subject_id, source, owner_user_id, author_user_id, level, solid_kind, topic_id, title)
SELECT p.id, CASE WHEN p.is_cat THEN '00000000-0000-7000-8000-000000000001'::uuid ELSE s.org_id END, 1, p.source,
       CASE WHEN p.is_cat THEN NULL ELSE s.id END, CASE WHEN p.is_cat THEN '00000000-0000-7000-8000-0000000000aa'::uuid END, p.level, p.sk, p.topic,
       CASE WHEN p.is_cat THEN 'Đề kho ' || p.n ELSE 'Đề tự nhập ' || p.n END
FROM t_problem p LEFT JOIN t_student s ON s.j = (p.n - 60) * 2 AND NOT p.is_cat;
INSERT INTO problem_version (id, org_id, problem_id, version, statement, statement_hash, verify_status, approval_status, ai_authored, created_by)
SELECT p.vid, pr.org_id, p.id, 1,
       'Cho hình ' || p.sk || ' số ' || p.n || ', tính khoảng cách, góc hoặc thể tích theo yêu cầu. ' || repeat('Nội dung đề mẫu. ', 12),
       digest('de-' || p.n, 'sha256'),
       'needs_review'::verify_status,      -- version mới luôn needs_review; nhãn đặt qua báo cáo bên dưới (D-GEO-158)
       CASE WHEN p.is_cat THEN 'teacher_approved' ELSE 'ai_drafted' END::approval_status, true,
       CASE WHEN p.is_cat THEN '00000000-0000-7000-8000-0000000000aa'::uuid ELSE pr.owner_user_id END
FROM t_problem p JOIN problem pr ON pr.id = p.id;
UPDATE problem SET current_version_id = p.vid FROM t_problem p WHERE problem.id = p.id;
INSERT INTO scene_build (id, org_id, problem_version_id, build_no, coord_source, hidden_measures, spec, coords)
SELECT geo.uuid_v7(), pv.org_id, pv.id, 1, CASE WHEN p.n % 12 = 0 THEN 'ai' ELSE 'engine' END::coord_source,
       ARRAY['angle(line(S,C),plane(A,B,C,D))', 'angle(line(S,A),plane(A,B,C,D))'],
       jsonb_build_object('schema_version', 1, 'type', 'constraints', 'items', (SELECT jsonb_agg('c' || g) FROM generate_series(1, 6) g)),
       jsonb_build_object('schema_version', 1, 'vertices', (SELECT jsonb_agg(jsonb_build_object('n', chr(64 + g), 'x', g, 'y', g * 2, 'z', 0)) FROM generate_series(1, 8) g))
FROM t_problem p JOIN problem_version pv ON pv.id = p.vid;
INSERT INTO hint (org_id, problem_version_id, level, body)
SELECT pv.org_id, pv.id, l, 'Gợi ý mức ' || l || ': ' || repeat('hướng nghĩ. ', 10)
FROM t_problem p JOIN problem_version pv ON pv.id = p.vid CROSS JOIN generate_series(1, 2) l;
INSERT INTO solution (org_id, problem_version_id, final_answer, steps, answer_forms, model, prompt_version, author_user_id)
SELECT pv.org_id, pv.id, 'a√' || (p.n % 7 + 2) || '/' || (p.n % 5 + 2),
       jsonb_build_object('schema_version', 1, 'steps', (SELECT jsonb_agg(jsonb_build_object('title', 'Bước ' || g, 'content', repeat('lời giải chi tiết ', 12), 'principle', 'nguyên lý', 'draw', jsonb_build_array('M', 'N'))) FROM generate_series(1, 5) g)),
       jsonb_build_array(jsonb_build_object('kind', 'expr', 'value', 'a*sqrt(' || (p.n % 7 + 2) || ')/' || (p.n % 5 + 2)),
                         jsonb_build_object('kind', 'numeric_a1', 'value', sqrt(p.n % 7 + 2) / (p.n % 5 + 2))),
       'mock-model', 'p1', CASE WHEN p.is_cat THEN '00000000-0000-7000-8000-0000000000aa'::uuid END
FROM t_problem p JOIN problem_version pv ON pv.id = p.vid;
INSERT INTO verification_report (id, org_id, problem_version_id, scene_ok, answer_ok, result, checks)
SELECT geo.uuid_v7(), pv.org_id, pv.id, true, p.n % 10 <> 0, CASE WHEN p.n % 10 = 0 THEN 'needs_review' ELSE 'verified' END::verify_status,
       jsonb_build_object('schema_version', 1, 'computed', 'giá trị')
FROM t_problem p JOIN problem_version pv ON pv.id = p.vid;
-- nhãn máy = kết quả báo cáo của chính version (trigger tg_pv_verify_guard; ở ứng dụng là fn_set_verify_status)
UPDATE problem_version pv SET verify_status = r.result, verification_report_id = r.id
FROM verification_report r WHERE r.org_id = pv.org_id AND r.problem_version_id = pv.id;
-- chữ ký chuẩn (D-GEO-150): đề kho và 90% đề tự nhập (10% "không dựng được hình" không có chữ ký). Chữ ký thử = băm của số đề;
-- bài tự học kế thừa khóa được gán lại chữ ký của đề kho bên dưới (sau khi có bài giao).
INSERT INTO problem_signature (org_id, problem_version_id, sig_version, template_id, params_norm, asked_quantity, signature)
SELECT pv.org_id, pv.id, 1, 1 + (p.n % 7), jsonb_build_object('a', 1, 'h', round((1 + (p.n % 9) * 0.25)::numeric, 6)),
       'angle(line(apex,v3),plane(base))', digest('sig-' || p.n, 'sha256')
FROM t_problem p JOIN problem_version pv ON pv.id = p.vid
WHERE p.is_cat OR p.n % 10 <> 5;

-- ---------- bài giảng 300 và từ khóa 900 (kho bài giảng mẫu)
INSERT INTO lecture (id, org_id, subject_id, slug, title, body, source_kind, approval_status, source_ref)
SELECT geo.uuid_v7(now() - interval '140 days' + n * interval '1 second'), '00000000-0000-7000-8000-000000000001', 1,
       'khai-niem-' || n,
       (ARRAY['Góc giữa đường thẳng và mặt phẳng','Khoảng cách từ điểm đến mặt phẳng','Đường vuông góc chung','Thể tích khối chóp','Thiết diện của hình chóp','Hai mặt phẳng vuông góc','Hình chiếu vuông góc','Định lý ba đường vuông góc'])[1 + (n % 8)] || ' ' || n,
       'Bài giảng khái niệm số ' || n || '. ' || (ARRAY['đường thẳng vuông góc với mặt phẳng','góc nhị diện giữa hai mặt','khoảng cách giữa hai đường thẳng chéo nhau','công thức thể tích bằng một phần ba diện tích đáy nhân chiều cao','mặt phẳng cắt hình chóp theo thiết diện','hình chiếu của điểm lên mặt phẳng'])[1 + (n % 6)] || '. ' || repeat('Giải thích chi tiết khái niệm hình học không gian. ', 25),
       CASE WHEN n % 3 = 0 THEN 'ai_written' ELSE 'teacher_authored' END::lecture_source,
       CASE WHEN n % 3 = 0 THEN 'ai_drafted' ELSE 'teacher_approved' END::approval_status,
       CASE WHEN n % 3 = 0 THEN NULL ELSE 'Giáo án mẫu, mục ' || n END
FROM generate_series(1, 300) n;
UPDATE lecture SET human_reviewed = true WHERE slug NOT IN (SELECT 'khai-niem-' || g FROM generate_series(1, 7) g);  -- 7 bài "chưa có người soát" (D-GEO-137)
INSERT INTO keyword (org_id, term_key, term, lecture_id)
SELECT l.org_id, geo.f_unaccent(lower(t.term)) || ' ' || row_number() OVER (), t.term, l.id
FROM (SELECT l2.*, row_number() OVER (ORDER BY created_at) rn FROM lecture l2) l
CROSS JOIN LATERAL (VALUES ('Đường vuông góc'), ('Góc nhị diện'), ('Hình chiếu')) t(term);

-- ---------- bài giao cho lớp: 4-12 mỗi lớp
-- mỗi lớp không giao trùng problem (D-GEO-161): prob_n = 1 + (7i + 13g) mod 60 khác nhau với g = 1..12 (13 nguyên tố cùng nhau với 60)
CREATE TEMP TABLE t_assign AS
SELECT row_number() OVER () AS rn, c.i AS class_i, c.id AS class_id, c.org_id, c.teacher_id,
       now() - random() * interval '90 days' AS created_at,
       1 + ((c.i * 7 + g * 13) % 60) AS prob_n, random() AS r
FROM t_class2 c CROSS JOIN LATERAL generate_series(1, 4 + floor(random() * 9)::int) g;
ALTER TABLE t_assign ADD COLUMN id uuid, ADD COLUMN release_at timestamptz;
UPDATE t_assign SET id = geo.uuid_v7(created_at),
       release_at = CASE WHEN r < 0.15 THEN NULL
                         WHEN r < 0.75 THEN created_at + (1 + random() * 2) * interval '1 day'
                         ELSE now() + random() * interval '3 days' END;
INSERT INTO assignment (id, org_id, kind, class_id, content_org_id, problem_id, problem_version_id, assigned_by, release_at, release_rev, allow_hint, allow_qa, created_at)
SELECT a.id, a.org_id, 'class', a.class_id, '00000000-0000-7000-8000-000000000001', p.id, p.vid, a.teacher_id, a.release_at, (random() * 2)::int, true, random() < 0.9, a.created_at
FROM t_assign a JOIN t_problem p ON p.n = a.prob_n;

-- ---------- tiến độ: 85% thành viên có hàng tiến độ mỗi bài (đã mở ít nhất một lần)
CREATE TEMP TABLE t_prog AS
SELECT a.org_id, a.id AS assignment_id, s.id AS user_id, a.created_at,
       a.release_at,
       floor(random() * 12 + random() * 6)::int AS ops,
       least(2, floor(random() * 3))::int AS hint_max,
       floor(random() * 3)::int AS ask,
       random() AS rr
FROM t_assign a JOIN t_student s ON s.class_id = a.class_id
WHERE random() < 0.85;
ALTER TABLE t_prog ADD COLUMN hints int, ADD COLUMN sol boolean;
UPDATE t_prog SET hints = hint_max + (CASE WHEN hint_max > 0 AND rr > 0.7 THEN 1 ELSE 0 END),
                  sol = (release_at IS NOT NULL AND release_at < now() AND rr < 0.55);
INSERT INTO assignment_progress (org_id, assignment_id, user_id, first_opened_at, last_active_at, hint_level_max, hint_view_count, ask_count, tool_count, active_seconds, solution_viewed_at, updated_at)
SELECT org_id, assignment_id, user_id, created_at + interval '1 hour' * (1 + rr * 30), least(now(), created_at + interval '1 hour' * (2 + rr * 70)),
       hint_max, hints, ask, ops, least(3600, (ops * 15 + hints * 45 + ask * 60 + 120 + rr * 300)::int), CASE WHEN sol THEN least(now(), release_at + interval '5 minutes' * (rr * 10)) END, now()
FROM t_prog;

-- ---------- sự kiện ~2 triệu, nhất quán với bộ đếm tiến độ
-- n sự kiện mỗi (bài, em) = 1 open + ops + hints + qa + (1 nếu đã xem lời giải)
INSERT INTO event_log (occurred_at, org_id, assignment_id, user_id, kind, arg)
SELECT least(now() - interval '1 second',
         date_trunc('day', p.created_at) +
         ((floor(random() * 3))::int * interval '1 day') +
         CASE WHEN random() < 0.5 THEN interval '19 hours' + random() * interval '2 hours' ELSE random() * interval '24 hours' END) AS occurred_at,
       p.org_id, p.assignment_id, p.user_id, e.kind, e.arg
FROM t_prog p
CROSS JOIN LATERAL (
  SELECT 'open'::event_kind AS kind, NULL::smallint AS arg, 0 AS g
  UNION ALL SELECT 'tool', (1 + (g % 7))::smallint, g FROM generate_series(1, p.ops) g
  UNION ALL SELECT 'hint', (1 + (g % 2))::smallint, g FROM generate_series(1, p.hints) g
  UNION ALL SELECT 'ask', NULL, g FROM generate_series(1, p.ask) g
  UNION ALL SELECT 'unlock', NULL, 0 WHERE p.sol
) e;

INSERT INTO qa_turn (org_id, assignment_id, user_id, request_id, asked_at, question, reply_kind, reply_text, guard_version, source_lecture_ids)
SELECT p.org_id, p.assignment_id, p.user_id, gen_random_uuid(), least(now(), p.created_at + interval '1 hour' * (3 + g * 2 + p.rr * 20)),
       'Câu hỏi số ' || g || ' về khái niệm hình học', (ARRAY['hint','concept','refused_answer','out_of_curriculum','explain_step'])[1 + (g % 5)]::qa_reply_kind,
       'Câu trả lời đã qua Kiểm soát đáp án: ' || repeat('giải thích khái niệm. ', 15), 'g1',
       ARRAY[(SELECT id FROM lecture OFFSET (abs(hashtext(p.user_id::text)) + g) % 300 LIMIT 1)]
FROM t_prog p CROSS JOIN LATERAL generate_series(1, p.ask) g;

-- ---------- bài tự học: 8.000 bài, 5% kế thừa khóa của một bài lớp (PO-16)
INSERT INTO assignment (id, org_id, kind, owner_user_id, content_org_id, problem_id, problem_version_id, assigned_by, release_at, created_at, lock_source_assignment_id)
SELECT geo.uuid_v7(now() - random() * interval '60 days'), s.org_id, 'self', s.id, s.org_id, p.id, p.vid, s.id, NULL, now() - random() * interval '60 days',
       CASE WHEN p.n % 20 = 0 THEN (SELECT a.id FROM t_assign a WHERE a.class_id = s.class_id ORDER BY a.rn LIMIT 1) END
FROM t_problem p JOIN t_student s ON s.j = (p.n - 60) * 2 WHERE p.n > 60;
-- bài tự học "dán lại đề lớp": chữ ký = chữ ký của đề kho của bài lớp nguồn (khớp theo chữ ký chuẩn dù văn bản khác, D-GEO-150)
UPDATE problem_signature ps SET signature = cs.signature
FROM assignment sa JOIN assignment la ON la.org_id = sa.org_id AND la.id = sa.lock_source_assignment_id
     JOIN problem_signature cs ON cs.org_id = la.content_org_id AND cs.problem_version_id = la.problem_version_id
WHERE sa.kind = 'self' AND ps.org_id = sa.org_id AND ps.problem_version_id = sa.problem_version_id;
INSERT INTO assignment_progress (org_id, assignment_id, user_id, first_opened_at, last_active_at, hint_level_max, hint_view_count, ask_count, tool_count)
SELECT a.org_id, a.id, a.owner_user_id, a.created_at, a.created_at + interval '10 minutes', (random() * 2)::int, (random() * 3)::int, 0, (random() * 15)::int
FROM assignment a WHERE a.kind = 'self';

-- ---------- nhật ký kiểm toán đổi giờ mở (mẫu) và cấp phép liên trường (mẫu, chặng 7)
INSERT INTO audit_log (occurred_at, org_id, actor_user_id, action, target_id, before_release_at, after_release_at)
SELECT now() - random() * interval '30 days', a.org_id, a.assigned_by, 'release_change', a.id, a.release_at, a.release_at + interval '1 day'
FROM assignment a WHERE a.kind = 'class' AND a.release_rev > 0 AND random() < 0.5;
INSERT INTO share_grant (id, org_id, grantee_org_id, resource_kind, resource_id, purpose, status, requested_by, grantor_approved_at, grantee_approved_at, valid_from, valid_until)
SELECT geo.uuid_v7(), s1.id, s2.id, 'lecture', l.id, 'Dùng chung bài giảng cho nhóm học liên trường', 'approved',
       (SELECT id FROM t_teacher WHERE org_id = s1.id AND k = 1), now(), now(), now() - interval '1 day', now() + interval '60 days'
FROM t_org s1 JOIN t_org s2 ON s2.n = s1.n + 1 CROSS JOIN LATERAL (SELECT id FROM lecture OFFSET s1.n LIMIT 1) l
WHERE s1.n BETWEEN 6 AND 40;

-- ---------- thống kê
RESET synchronous_commit;
VACUUM (ANALYZE);
SELECT 'org' t, count(*) FROM org UNION ALL SELECT 'class', count(*) FROM class UNION ALL
SELECT 'student', count(*) FROM app_user WHERE role = 'student' UNION ALL SELECT 'assignment_class', count(*) FROM assignment WHERE kind = 'class' UNION ALL
SELECT 'assignment_self', count(*) FROM assignment WHERE kind = 'self' UNION ALL SELECT 'progress', count(*) FROM assignment_progress UNION ALL
SELECT 'event_log', count(*) FROM event_log UNION ALL SELECT 'qa_turn', count(*) FROM qa_turn UNION ALL SELECT 'problem', count(*) FROM problem UNION ALL
SELECT 'lecture', count(*) FROM lecture UNION ALL SELECT 'keyword', count(*) FROM keyword;
