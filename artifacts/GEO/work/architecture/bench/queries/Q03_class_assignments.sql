-- role: app
-- lint-allow: R-Q05 (đếm trong phạm vi MỘT bài, tối đa cỡ lớp hàng, quét chỉ mục)
-- Bài của lớp (giáo viên): mới nhất trước, kèm số em đã mở / đã xem lời giải (đếm trong phạm vi một bài, tối đa cỡ lớp).
:explain
SELECT a.id, a.release_at, a.release_rev,
       CASE WHEN a.release_at IS NULL THEN 'unset' WHEN a.release_at > now() THEN 'scheduled' ELSE 'open' END AS release_state,
       p.title, pv.verify_status, pv.approval_status, cnt.opened, cnt.viewed_solution
FROM geo.class c
CROSS JOIN LATERAL (
    SELECT x.id, x.org_id, x.release_at, x.release_rev, x.content_org_id, x.problem_version_id
    FROM geo.assignment x
    WHERE x.org_id = c.org_id AND x.class_id = c.id AND x.kind = 'class' AND x.id < :'cursor_max'
    ORDER BY x.id DESC LIMIT 21) a
JOIN geo.problem_version pv ON pv.org_id = a.content_org_id AND pv.id = a.problem_version_id
JOIN geo.problem p          ON p.org_id = pv.org_id AND p.id = pv.problem_id
CROSS JOIN LATERAL (
    SELECT count(*) AS opened, count(ap.solution_viewed_at) AS viewed_solution
    FROM geo.assignment_progress ap
    WHERE ap.org_id = a.org_id AND ap.assignment_id = a.id) cnt
WHERE c.org_id = :'org' AND c.id = :'class' AND c.teacher_id = :'teacher'
ORDER BY a.id DESC
LIMIT 21;
