-- role: app
-- Bài tự học của em (cùng bảng assignment, kind='self'), keyset theo id. lock_source_assignment_id chỉ là dấu vết lúc tạo (D-GEO-151);
-- trạng thái khóa thật của từng bài tính khi mở bài (fn_solution_gate), không ở danh sách.
:explain
SELECT a.id, a.lock_source_assignment_id, p.title, pv.verify_status, ap.hint_level_max, ap.last_active_at
FROM geo.assignment a
JOIN geo.problem_version pv ON pv.org_id = a.content_org_id AND pv.id = a.problem_version_id
JOIN geo.problem p          ON p.org_id = pv.org_id AND p.id = pv.problem_id
LEFT JOIN geo.assignment_progress ap ON ap.org_id = a.org_id AND ap.assignment_id = a.id AND ap.user_id = a.owner_user_id
WHERE a.org_id = :'self_org' AND a.owner_user_id = :'self_student' AND a.kind = 'self' AND a.id < :'cursor_max'
ORDER BY a.id DESC
LIMIT 21;
