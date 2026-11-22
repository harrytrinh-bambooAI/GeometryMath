-- Dữ liệu THỬ TẢI (US-GEO-108, F-GEO-097, D-GEO-049 PROVISIONAL): 50 trường · 600 lớp (12/trường) · 20.000 học sinh
-- (400/trường, ≈ 33,3/lớp) · 2.000.000 sự kiện (100/học sinh). Chỉ chạy trên CSDL thử tải riêng (scripts/bench-seed.sh
-- từ chối CSDL khác), không lẫn dữ liệu mẫu (FC1). Tên đánh số, không tên người hay tổ chức thật (FC2).
-- Chạy lại trên CSDL sạch cho cùng số trường, lớp, học sinh (SC3). Thuần SQL (không lệnh psql) để IT chạy được.
SET synchronous_commit = off;
SELECT setseed(0.42);

CREATE TEMP TABLE b_school AS
SELECT n, uuid_v7(now() - interval '200 days' + n * interval '1 second') AS id,
          uuid_v7(now() - interval '199 days' + n * interval '1 second') AS teacher_id
FROM generate_series(1, 50) n;
INSERT INTO org (id, parent_id, kind, name)
SELECT id, platform_org_id(), 'school', 'Trường thử ' || lpad(n::text, 2, '0') FROM b_school;
INSERT INTO app_user (id, org_id, role, account_kind, display_name)
SELECT teacher_id, id, 'teacher', 'sample', 'Giáo viên thử ' || lpad(n::text, 2, '0') FROM b_school;

-- 600 lớp: lớp c (1..600) thuộc trường (c-1)/12+1; mã lớp 6 ký tự [A-Z2-9], duy nhất
CREATE TEMP TABLE b_class AS
SELECT c, s.id AS org_id, s.teacher_id, uuid_v7(now() - interval '180 days' + c * interval '1 second') AS id,
       'B' || translate(lpad(c::text, 5, '0'), '01', 'XY') AS join_code,
       (CASE WHEN c % 2 = 0 THEN 'thpt' ELSE 'thcs' END)::edu_level AS level
FROM generate_series(1, 600) c JOIN b_school s ON s.n = (c - 1) / 12 + 1;
INSERT INTO class (id, org_id, name, level, grade, join_code, teacher_id)
SELECT id, org_id, 'Lớp thử ' || lpad(c::text, 3, '0'), level, CASE WHEN level = 'thpt' THEN 11 ELSE 9 END, join_code, teacher_id
FROM b_class;

-- 20.000 học sinh: học sinh k thuộc trường (k-1)/400+1, lớp thứ ((k-1)%400)%12 của trường đó
CREATE TEMP TABLE b_student AS
SELECT k, cl.org_id, cl.id AS class_id, uuid_v7(now() - interval '170 days' + k * interval '1 millisecond') AS id
FROM generate_series(1, 20000) k
JOIN b_class cl ON cl.c = ((k - 1) / 400) * 12 + ((k - 1) % 400) % 12 + 1;
INSERT INTO app_user (id, org_id, role, account_kind, display_name)
SELECT id, org_id, 'student', 'light', 'Học sinh ' || lpad(k::text, 5, '0') FROM b_student;
INSERT INTO class_member (org_id, class_id, user_id, name_key)
SELECT org_id, class_id, id, 'hoc sinh ' || lpad(k::text, 5, '0') FROM b_student;

-- 6 đề thử của kho nền tảng và 4 bài giao mỗi lớp (2.400 bài)
CREATE TEMP TABLE b_problem AS
SELECT p, uuid_v7(now() - interval '300 days' + p * interval '1 second') AS id,
          uuid_v7(now() - interval '299 days' + p * interval '1 second') AS version_id
FROM generate_series(1, 6) p;
INSERT INTO problem (id, org_id, subject_id, source, author_user_id, level, solid_kind, topic_id, title, code)
SELECT id, platform_org_id(), 1, 'catalog', '00000000-0000-7000-8000-0000000000aa', 'thpt', 'pyramid', 1,
       'Đề thử ' || p, 'BEN-' || lpad(p::text, 2, '0')
FROM b_problem;
INSERT INTO problem_version (id, org_id, problem_id, version, statement, statement_hash, created_by)
SELECT version_id, platform_org_id(), id, 1, 'Đề thử số ' || p || ': cho hình chóp đều, tính góc.',
       digest('Đề thử số ' || p, 'sha256'), '00000000-0000-7000-8000-0000000000aa'
FROM b_problem;
UPDATE problem pr SET current_version_id = b.version_id FROM b_problem b WHERE pr.id = b.id;

CREATE TEMP TABLE b_assignment AS
SELECT cl.c, j, cl.org_id, cl.id AS class_id, cl.teacher_id, b.id AS problem_id, b.version_id,
       uuid_v7(now() - interval '90 days' + (cl.c * 4 + j) * interval '1 second') AS id
FROM b_class cl CROSS JOIN generate_series(1, 4) j JOIN b_problem b ON b.p = j;
INSERT INTO assignment (id, org_id, kind, class_id, content_org_id, problem_id, problem_version_id, assigned_by, release_at,
                        created_at)
SELECT id, org_id, 'class', class_id, platform_org_id(), problem_id, version_id, teacher_id,
       CASE WHEN j % 2 = 0 THEN now() + interval '1 day' END, now() - interval '90 days'
FROM b_assignment;

-- 2.000.000 sự kiện: 100 mỗi học sinh, rải trong 30 ngày qua, trên 4 bài của lớp em
INSERT INTO event_log (occurred_at, org_id, assignment_id, user_id, kind, arg)
SELECT now() - (random() * 30) * interval '1 day', s.org_id, a.id, s.id,
       CASE WHEN e % 10 = 0 THEN 'open'::event_kind ELSE 'tool'::event_kind END,
       CASE WHEN e % 10 = 0 THEN NULL ELSE (1 + e % 7)::smallint END
FROM b_student s
CROSS JOIN generate_series(1, 100) e
JOIN b_assignment a ON a.class_id = s.class_id AND a.j = 1 + e % 4;

-- Tiến độ (mỗi học sinh × 4 bài) và một câu hỏi mỗi học sinh: để đo truy vấn nóng (TC-GEO-0802)
INSERT INTO assignment_progress (org_id, assignment_id, user_id, first_opened_at, last_active_at, hint_level_max,
                                 hint_view_count, ask_count, tool_count, active_seconds)
SELECT s.org_id, a.id, s.id, now() - interval '10 days', now() - interval '1 day', (s.k % 3)::smallint, s.k % 3, 1, 22, 600
FROM b_student s JOIN b_assignment a ON a.class_id = s.class_id;
INSERT INTO qa_turn (org_id, assignment_id, user_id, request_id, question, reply_kind, reply_text, guard_version)
SELECT s.org_id, a.id, s.id, uuid_v7(), 'Câu hỏi thử ' || s.k, 'concept', 'Trả lời thử.', 'g0-bench'
FROM b_student s JOIN b_assignment a ON a.class_id = s.class_id AND a.j = 1;

ANALYZE;
