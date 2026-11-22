-- role: app
-- Trang theo dõi lớp: danh sách em + tiến độ, sắp theo tên A → Z (D-GEO-121), phân trang theo khóa (name_key, user_id) (D-GEO-167, RV-36),
-- trang 100 (LIMIT 101), không giới hạn cỡ lớp. Không đụng event_log (NFR-GEO-047). Trang đầu: cursor = ('', 0000...).
:explain
SELECT u.id AS user_id, u.display_name, cm.name_key,
       ap.first_opened_at, ap.last_active_at, ap.hint_level_max, ap.hint_view_count, ap.ask_count, ap.tool_count, ap.active_seconds, ap.solution_viewed_at
FROM geo.assignment a
JOIN geo.class c ON c.org_id = a.org_id AND c.id = a.class_id AND c.teacher_id = :'teacher'
JOIN geo.class_member cm ON cm.org_id = a.org_id AND cm.class_id = a.class_id AND cm.left_at IS NULL
                        AND (cm.name_key, cm.user_id) > ('', '00000000-0000-0000-0000-000000000000'::uuid)
JOIN geo.app_user u ON u.org_id = cm.org_id AND u.id = cm.user_id
LEFT JOIN geo.assignment_progress ap ON ap.org_id = a.org_id AND ap.assignment_id = a.id AND ap.user_id = cm.user_id
WHERE a.org_id = :'org' AND a.id = :'assignment'
ORDER BY cm.name_key, cm.user_id
LIMIT 101;
