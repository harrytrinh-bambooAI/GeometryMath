-- Danh sách bài của em (Q02), trang đầu. Trọng số: 8%
\set i random(1, 19900)
SELECT org_id AS org, student_id AS stu FROM bench.pairs WHERE id = :i \gset
BEGIN;
SELECT set_config('app.org_id', :org, true);
SELECT a.id, a.class_id, a.release_at, p.title, pv.verify_status, ap.hint_level_max, ap.solution_viewed_at
FROM geo.class_member mc
CROSS JOIN LATERAL (SELECT x.id, x.org_id, x.class_id, x.release_at, x.problem_version_id FROM geo.assignment x
   WHERE x.org_id = mc.org_id AND x.class_id = mc.class_id AND x.kind = 'class' AND x.id < 'ffffffff-ffff-ffff-ffff-ffffffffffff' ORDER BY x.id DESC LIMIT 21) a
JOIN geo.problem_version pv ON pv.id = a.problem_version_id JOIN geo.problem p ON p.org_id = pv.org_id AND p.id = pv.problem_id
LEFT JOIN geo.assignment_progress ap ON ap.org_id = a.org_id AND ap.assignment_id = a.id AND ap.user_id = :stu::uuid
WHERE mc.org_id = :org::uuid AND mc.user_id = :stu::uuid AND mc.left_at IS NULL ORDER BY a.id DESC LIMIT 21;
COMMIT;
