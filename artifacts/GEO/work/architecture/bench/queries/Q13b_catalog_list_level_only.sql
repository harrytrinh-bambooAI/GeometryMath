-- role: app
-- Kho đề chỉ lọc theo cấp (phần đầu của chỉ mục).
:explain
SELECT p.id, p.title, p.level, p.solid_kind, p.topic_id, pv.verify_status, pv.approval_status
FROM geo.problem p
JOIN geo.problem_version pv ON pv.id = p.current_version_id
WHERE p.org_id = :'platform' AND p.source = 'catalog' AND p.archived_at IS NULL AND p.level = 'thcs'
  AND (p.level, p.solid_kind, p.topic_id, p.id) > ('thcs', 'cube', 2, '00000000-0000-0000-0000-000000000000')
ORDER BY p.level, p.solid_kind, p.topic_id, p.id
LIMIT 21;
