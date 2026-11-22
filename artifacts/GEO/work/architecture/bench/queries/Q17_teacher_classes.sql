-- role: app
-- lint-allow: R-Q05 (đếm thành viên trong phạm vi MỘT lớp)
-- Màn đầu của giáo viên: các lớp của mình kèm sĩ số, phân trang theo id lớp.
:explain
SELECT c.id, c.name, c.level, c.grade, c.join_code, m.members
FROM geo.class c
CROSS JOIN LATERAL (SELECT count(*) AS members FROM geo.class_member cm
                    WHERE cm.org_id = c.org_id AND cm.class_id = c.id AND cm.left_at IS NULL) m
WHERE c.org_id = :'org' AND c.teacher_id = :'teacher' AND c.archived_at IS NULL AND c.id > '00000000-0000-0000-0000-000000000000'
ORDER BY c.id
LIMIT 21;
