-- Giáo viên duyệt kho đề (Q13). Trọng số: 5%
SELECT p.id, p.title, p.level, p.solid_kind, p.topic_id, pv.verify_status, pv.approval_status
FROM geo.problem p JOIN geo.problem_version pv ON pv.id = p.current_version_id
WHERE p.org_id = '00000000-0000-7000-8000-000000000001' AND p.source = 'catalog' AND p.archived_at IS NULL AND p.level = 'thpt' AND p.solid_kind = 'box'
  AND (p.level, p.solid_kind, p.topic_id, p.id) > ('thpt', 'box', 0, '00000000-0000-0000-0000-000000000000')
ORDER BY p.level, p.solid_kind, p.topic_id, p.id LIMIT 21;
