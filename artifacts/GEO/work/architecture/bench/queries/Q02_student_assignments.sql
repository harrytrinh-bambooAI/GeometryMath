-- role: app
-- Bài của em (LRN-01): mọi bài lớp của các lớp em đang học, mới nhất trước, phân trang theo khóa (cursor = assignment.id).
-- Trang đầu: cursor = ffffffff-ffff-ffff-ffff-ffffffffffff. Trang kế: cursor = id của dòng cuối trang trước. LIMIT = size + 1.
:explain
SELECT a.id, a.class_id, a.release_at,
       CASE WHEN a.release_at IS NULL THEN 'unset' WHEN a.release_at > now() THEN 'scheduled' ELSE 'open' END AS release_state,
       p.title, p.solid_kind, pv.verify_status, pv.approval_status,
       ap.hint_level_max, ap.solution_viewed_at, ap.last_active_at
FROM geo.class_member mc
CROSS JOIN LATERAL (
    SELECT x.id, x.org_id, x.class_id, x.release_at, x.content_org_id, x.problem_version_id
    FROM geo.assignment x
    WHERE x.org_id = mc.org_id AND x.class_id = mc.class_id AND x.kind = 'class' AND x.id < :'cursor_max'
    ORDER BY x.id DESC LIMIT 21) a
JOIN geo.problem_version pv ON pv.org_id = a.content_org_id AND pv.id = a.problem_version_id
JOIN geo.problem p          ON p.org_id = pv.org_id AND p.id = pv.problem_id
LEFT JOIN geo.assignment_progress ap
       ON ap.org_id = a.org_id AND ap.assignment_id = a.id AND ap.user_id = :'student'
WHERE mc.org_id = :'org' AND mc.user_id = :'student' AND mc.left_at IS NULL
ORDER BY a.id DESC
LIMIT 21;
