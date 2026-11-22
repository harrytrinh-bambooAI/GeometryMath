-- Giáo viên mở trang theo dõi lớp (Q09, sắp theo tên, keyset (name_key, user_id)). Trọng số: 3%
\set i random(1, 600)
SELECT org_id AS org, teacher_id AS tch, assignment_id AS asg FROM bench.tcls WHERE id = :i \gset
SELECT u.id, u.display_name, cm.name_key, ap.first_opened_at, ap.last_active_at, ap.hint_level_max, ap.hint_view_count, ap.ask_count, ap.tool_count, ap.active_seconds, ap.solution_viewed_at
FROM geo.assignment a JOIN geo.class c ON c.org_id = a.org_id AND c.id = a.class_id AND c.teacher_id = :tch::uuid
JOIN geo.class_member cm ON cm.org_id = a.org_id AND cm.class_id = a.class_id AND cm.left_at IS NULL AND (cm.name_key, cm.user_id) > ('', '00000000-0000-0000-0000-000000000000'::uuid)
JOIN geo.app_user u ON u.org_id = cm.org_id AND u.id = cm.user_id
LEFT JOIN geo.assignment_progress ap ON ap.org_id = a.org_id AND ap.assignment_id = a.id AND ap.user_id = cm.user_id
WHERE a.org_id = :org::uuid AND a.id = :asg::uuid ORDER BY cm.name_key, cm.user_id LIMIT 101;
