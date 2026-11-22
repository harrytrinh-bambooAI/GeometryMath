-- role: app
-- Danh sách bài đã giao của giáo viên trên MỌI lớp của mình (US-GEO-701-SC6, D-GEO-129, RV-19), mới nhất trước, keyset theo
-- assignment.id (UUIDv7), trang 20 (LIMIT 21), trần 100. Mỗi lớp lấy riêng 21 dòng theo chỉ mục rồi gộp (như Q02), tránh sắp toàn bộ.
:explain
SELECT a.id, a.class_id, c.name AS class_name, a.release_at,
       CASE WHEN a.release_at IS NULL THEN 'unset' WHEN a.release_at > now() THEN 'scheduled' ELSE 'open' END AS release_state,
       p.title, p.solid_kind
FROM geo.class c
CROSS JOIN LATERAL (
    SELECT x.id, x.class_id, x.release_at, x.content_org_id, x.problem_id
    FROM geo.assignment x
    WHERE x.org_id = c.org_id AND x.class_id = c.id AND x.kind = 'class' AND x.id < :'cursor_max'
    ORDER BY x.id DESC LIMIT 21) a
JOIN geo.problem p ON p.org_id = a.content_org_id AND p.id = a.problem_id
WHERE c.org_id = :'org' AND c.teacher_id = :'teacher' AND c.archived_at IS NULL
ORDER BY a.id DESC
LIMIT 21;
