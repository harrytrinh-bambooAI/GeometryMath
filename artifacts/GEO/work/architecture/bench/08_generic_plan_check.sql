-- Kiểm kế hoạch TỔNG QUÁT (generic plan): JDBC chuyển sang kế hoạch tổng quát sau vài lần thực thi câu lệnh chuẩn bị. Ép bằng
-- plan_cache_mode = force_generic_plan, chạy EXPLAIN (ANALYZE, BUFFERS) EXECUTE cho các truy vấn có tham số kiểu khác nhau,
-- xác nhận kế hoạch vẫn dùng chỉ mục và thời gian không xấu đi.
\i bench/02_params.sql
SET ROLE geo_app;
SET plan_cache_mode = force_generic_plan;
\echo '== Q02 bài của em'
PREPARE g02(uuid, uuid, uuid) AS
SELECT a.id, a.class_id, a.release_at, p.title, p.solid_kind, pv.verify_status, pv.approval_status, ap.hint_level_max, ap.solution_viewed_at, ap.last_active_at
FROM geo.class_member mc
CROSS JOIN LATERAL (SELECT x.id, x.org_id, x.class_id, x.release_at, x.problem_version_id FROM geo.assignment x
    WHERE x.org_id = mc.org_id AND x.class_id = mc.class_id AND x.kind = 'class' AND x.id < $3 ORDER BY x.id DESC LIMIT 21) a
JOIN geo.problem_version pv ON pv.id = a.problem_version_id JOIN geo.problem p ON p.org_id = pv.org_id AND p.id = pv.problem_id
LEFT JOIN geo.assignment_progress ap ON ap.org_id = a.org_id AND ap.assignment_id = a.id AND ap.user_id = $2
WHERE mc.org_id = $1 AND mc.user_id = $2 AND mc.left_at IS NULL ORDER BY a.id DESC LIMIT 21;
EXECUTE g02(:'org', :'student', :'cursor_max');
EXPLAIN (ANALYZE, BUFFERS) EXECUTE g02(:'org', :'student', :'cursor_max');
\echo '== Q09 theo dõi lớp'
PREPARE g09(uuid, uuid, uuid, uuid) AS
SELECT u.id, u.display_name, ap.first_opened_at, ap.last_active_at, ap.hint_level_max, ap.hint_view_count, ap.ask_count, ap.tool_count, ap.active_seconds, ap.solution_viewed_at
FROM geo.assignment a JOIN geo.class c ON c.org_id = a.org_id AND c.id = a.class_id AND c.teacher_id = $3
JOIN geo.class_member cm ON cm.org_id = a.org_id AND cm.class_id = a.class_id AND cm.left_at IS NULL AND cm.user_id > $4
JOIN geo.app_user u ON u.org_id = cm.org_id AND u.id = cm.user_id
LEFT JOIN geo.assignment_progress ap ON ap.org_id = a.org_id AND ap.assignment_id = a.id AND ap.user_id = cm.user_id
WHERE a.org_id = $1 AND a.id = $2 ORDER BY cm.user_id LIMIT 101;
EXPLAIN (ANALYZE, BUFFERS) EXECUTE g09(:'org', :'assignment', :'teacher', '00000000-0000-0000-0000-000000000000');
\echo '== Q13 kho đề (kiểu enum làm tham số)'
PREPARE g13(uuid, geo.edu_level, geo.solid_kind, smallint, uuid) AS
SELECT p.id, p.title, p.level, p.solid_kind, p.topic_id, pv.verify_status, pv.approval_status
FROM geo.problem p JOIN geo.problem_version pv ON pv.id = p.current_version_id
WHERE p.org_id = $1 AND p.source = 'catalog' AND p.archived_at IS NULL AND p.level = $2 AND p.solid_kind = $3
  AND (p.level, p.solid_kind, p.topic_id, p.id) > ($2, $3, $4, $5)
ORDER BY p.level, p.solid_kind, p.topic_id, p.id LIMIT 21;
EXPLAIN (ANALYZE, BUFFERS) EXECUTE g13(:'platform', 'thpt', 'box', 0::smallint, '00000000-0000-0000-0000-000000000000');
\echo '== Q20 dòng thời gian (khoảng thời gian làm tham số)'
PREPARE g20(uuid, uuid, uuid, timestamptz, timestamptz) AS
SELECT e.id, e.occurred_at, e.kind, e.arg FROM geo.event_log e
WHERE e.org_id = $1 AND e.assignment_id = $2 AND e.user_id = $3 AND e.occurred_at >= $4 AND e.occurred_at < $5
ORDER BY e.occurred_at DESC, e.id DESC LIMIT 51;
EXPLAIN (ANALYZE, BUFFERS) EXECUTE g20(:'ev_org', :'ev_assignment', :'ev_student', :'lo_ts', :'hi_ts');
\echo '== Q12 tìm nguồn (chuỗi tìm kiếm làm tham số)'
PREPARE g12(uuid, text) AS
WITH q AS (SELECT websearch_to_tsquery('simple', geo.f_unaccent(lower($2))) AS tsq),
cand AS MATERIALIZED (SELECT l.id, l.search_tsv FROM geo.lecture l, q WHERE l.org_id = $1 AND l.search_tsv @@ q.tsq LIMIT 200),
top5 AS (SELECT c.id FROM cand c, q ORDER BY ts_rank(c.search_tsv, q.tsq) DESC, c.id LIMIT 5)
SELECT l.id, l.title, l.source_kind, l.approval_status, l.source_ref, left(l.body, 600) FROM top5 t JOIN geo.lecture l ON l.org_id = $1 AND l.id = t.id;
EXPLAIN (ANALYZE, BUFFERS) EXECUTE g12(:'platform', :'q');
\echo '== Q16 trùng đề đang khóa (băm làm tham số)'
PREPARE g16(text, uuid, uuid) AS
SELECT a.id, a.release_at FROM geo.problem_version pv JOIN geo.assignment a ON a.problem_version_id = pv.id
WHERE pv.statement_hash = decode($1, 'hex') AND a.org_id = $2 AND a.kind = 'class'
  AND a.class_id = ANY (ARRAY(SELECT cm.class_id FROM geo.class_member cm WHERE cm.org_id = $2 AND cm.user_id = $3 AND cm.left_at IS NULL))
  AND (a.release_at IS NULL OR a.release_at > now()) ORDER BY a.id DESC LIMIT 1;
EXPLAIN (ANALYZE, BUFFERS) EXECUTE g16(:'hash', :'org', :'student');
