-- role: app
-- Chi tiết bài cho học sinh: đề + bản dựng hình + danh sách đại lượng ẩn (biểu thức, không giá trị; D-GEO-024, 133), KHÔNG lời giải, KHÔNG gợi ý.
-- Kiểm tư cách lớp ngay trong truy vấn (BR-12). Tọa độ là dữ kiện của đề (D-GEO-154).
:explain
SELECT a.id, a.release_at,
       CASE WHEN a.release_at IS NULL THEN 'unset' WHEN a.release_at > now() THEN 'scheduled' ELSE 'open' END AS release_state,
       a.release_rev, a.allow_hint, a.allow_qa,
       pv.id AS problem_version_id, pv.statement, pv.verify_status, pv.approval_status, pv.ai_authored,
       p.title, p.solid_kind, sb.coord_source, sb.schema_version, sb.spec, sb.coords, sb.hidden_measures
FROM geo.assignment a
JOIN geo.problem_version pv ON pv.org_id = a.content_org_id AND pv.id = a.problem_version_id
JOIN geo.problem p          ON p.org_id = pv.org_id AND p.id = pv.problem_id
CROSS JOIN LATERAL (
    SELECT s.coord_source, s.schema_version, s.spec, s.coords, s.hidden_measures
    FROM geo.scene_build s
    WHERE s.org_id = pv.org_id AND s.problem_version_id = pv.id
    ORDER BY s.build_no DESC LIMIT 1) sb
WHERE a.org_id = :'org' AND a.id = :'assignment'
  AND ((a.kind = 'class' AND EXISTS (SELECT 1 FROM geo.class_member cm
                                     WHERE cm.org_id = a.org_id AND cm.class_id = a.class_id AND cm.user_id = :'student' AND cm.left_at IS NULL))
    OR (a.kind = 'self' AND a.owner_user_id = :'student'));
