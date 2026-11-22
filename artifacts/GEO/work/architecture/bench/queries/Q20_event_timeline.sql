-- role: app
-- Dòng thời gian một học sinh trong một bài (giáo viên bấm xem), keyset (occurred_at, id), CÓ cận thời gian để cắt partition.
:explain
SELECT e.id, e.occurred_at, e.kind, e.arg
FROM geo.event_log e
WHERE e.org_id = :'ev_org' AND e.assignment_id = :'ev_assignment' AND e.user_id = :'ev_student'
  AND e.occurred_at >= :'lo_ts' AND e.occurred_at < :'hi_ts'
ORDER BY e.occurred_at DESC, e.id DESC
LIMIT 51;
