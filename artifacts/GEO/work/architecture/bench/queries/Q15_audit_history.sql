-- role: app
-- Lịch sử đổi giờ mở của một bài, mới nhất trước.
:explain
SELECT al.id, al.occurred_at, al.actor_user_id, al.action, al.before_release_at, al.after_release_at
FROM geo.audit_log al
WHERE al.org_id = :'org' AND al.target_id = :'assignment' AND al.id < 9223372036854775807
ORDER BY al.id DESC
LIMIT 21;
