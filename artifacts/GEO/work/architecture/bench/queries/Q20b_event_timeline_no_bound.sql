-- role: app
-- lint-allow: R-Q09 (ví dụ đối chứng cố ý; không dùng trong mã)
-- ĐỐI CHỨNG (xấu): cùng truy vấn nhưng không có cận thời gian => phải thăm chỉ mục của mọi partition.
:explain
SELECT e.id, e.occurred_at, e.kind, e.arg
FROM geo.event_log e
WHERE e.org_id = :'ev_org' AND e.assignment_id = :'ev_assignment' AND e.user_id = :'ev_student'
ORDER BY e.occurred_at DESC, e.id DESC
LIMIT 51;
