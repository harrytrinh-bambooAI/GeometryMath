-- role: app
-- lint-allow: R-Q05 (đếm trong phạm vi MỘT bài và MỘT lớp, tối đa cỡ lớp hàng)
-- Các thẻ tổng hợp của Theo dõi lớp (M-GEO-21, 22, 23): LEFT JOIN bộ đếm tiến độ của MỘT bài (RV-30) nên khi chưa em nào mở
-- vẫn trả 1 hàng với class_size (thẻ "0/5"). Không đụng event_log.
:explain
SELECT count(ap.first_opened_at) AS opened,
       count(*) FILTER (WHERE ap.hint_view_count > 0) AS used_hint,
       COALESCE(sum(ap.ask_count), 0) AS asks,
       COALESCE(sum(ap.active_seconds), 0) AS active_seconds_total,
       count(ap.solution_viewed_at) AS viewed_solution,
       (SELECT count(*) FROM geo.class_member cm WHERE cm.org_id = a.org_id AND cm.class_id = a.class_id AND cm.left_at IS NULL) AS class_size
FROM geo.assignment a
JOIN geo.class c ON c.org_id = a.org_id AND c.id = a.class_id AND c.teacher_id = :'teacher'
LEFT JOIN geo.assignment_progress ap ON ap.org_id = a.org_id AND ap.assignment_id = a.id
WHERE a.org_id = :'org' AND a.id = :'assignment'
GROUP BY a.org_id, a.class_id;
