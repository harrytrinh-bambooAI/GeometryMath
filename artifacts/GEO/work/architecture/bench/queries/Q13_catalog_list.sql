-- role: app
-- Kho đề có lọc cấp + loại hình (dạng bài tùy chọn: thêm 'AND p.topic_id = ...' khi có). Thứ tự duyệt = (level, solid_kind, topic_id, id).
-- Trang đầu cursor = (thcs, pyramid, 0, 0000...). Trang kế: bộ bốn của dòng cuối.
:explain
SELECT p.id, p.title, p.level, p.solid_kind, p.topic_id, pv.verify_status, pv.approval_status
FROM geo.problem p
JOIN geo.problem_version pv ON pv.id = p.current_version_id
WHERE p.org_id = :'platform' AND p.source = 'catalog' AND p.archived_at IS NULL
  AND p.level = 'thpt' AND p.solid_kind = 'box'
  AND (p.level, p.solid_kind, p.topic_id, p.id) > ('thpt', 'box', 0, '00000000-0000-0000-0000-000000000000')
ORDER BY p.level, p.solid_kind, p.topic_id, p.id
LIMIT 21;
