-- role: app
-- lint-allow: R-Q04 (tra khóa chính: 1 hàng)
-- Trạng thái giờ mở (thăm dò nhẹ của trang đang mở, D-GEO-074): ba trạng thái tính bằng đồng hồ CSDL, kèm số giây còn lại để thiết bị đặt hẹn giờ.
:explain
SELECT a.release_rev,
       CASE WHEN a.release_at IS NULL THEN 'unset' WHEN a.release_at > now() THEN 'scheduled' ELSE 'open' END AS release_state,
       CASE WHEN a.release_at > now() THEN ceil(extract(epoch FROM a.release_at - now()))::int END AS release_in_seconds
FROM geo.assignment a
WHERE a.org_id = :'org' AND a.id = :'assignment'
  AND ((a.kind = 'class' AND EXISTS (SELECT 1 FROM geo.class_member cm
                                     WHERE cm.org_id = a.org_id AND cm.class_id = a.class_id AND cm.user_id = :'student' AND cm.left_at IS NULL))
    OR (a.kind = 'self' AND a.owner_user_id = :'student'));
