-- Thăm dò trạng thái giờ mở (Q04b) khi đệm Redis trượt (ca xấu: mọi lần thăm dò xuống CSDL). Thường trực ≤ 8 giây/trang chờ (ADR-GEO-10).
\set i random(1, 19900)
SELECT org_id AS org, assignment_id AS asg, student_id AS stu FROM bench.pairs WHERE id = :i \gset
SELECT a.release_rev, CASE WHEN a.release_at IS NULL THEN 'unset' WHEN a.release_at > now() THEN 'scheduled' ELSE 'open' END,
       CASE WHEN a.release_at > now() THEN ceil(extract(epoch FROM a.release_at - now()))::int END
FROM geo.assignment a
WHERE a.org_id = :org::uuid AND a.id = :asg::uuid
  AND ((a.kind = 'class' AND EXISTS (SELECT 1 FROM geo.class_member cm WHERE cm.org_id = a.org_id AND cm.class_id = a.class_id AND cm.user_id = :stu::uuid AND cm.left_at IS NULL))
    OR (a.kind = 'self' AND a.owner_user_id = :stu::uuid));
