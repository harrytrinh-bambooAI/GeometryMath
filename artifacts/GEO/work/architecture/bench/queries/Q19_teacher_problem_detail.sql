-- role: app
-- Giáo viên xem một đề trong kho (KNW-02, LRN-04): đề + nhãn + hình + gợi ý. Lời giải lấy riêng bằng geo.fn_get_solution_teacher (Q19b).
:explain
SELECT p.id, p.title, p.level, p.solid_kind, p.topic_id,
       pv.id AS problem_version_id, pv.statement, pv.verify_status, pv.approval_status, pv.ai_authored,
       sb.coord_source, sb.spec, sb.coords, h.hints
FROM geo.problem p
JOIN geo.problem_version pv ON pv.id = p.current_version_id
CROSS JOIN LATERAL (
    SELECT s.coord_source, s.spec, s.coords
    FROM geo.scene_build s
    WHERE s.org_id = pv.org_id AND s.problem_version_id = pv.id
    ORDER BY s.build_no DESC LIMIT 1) sb
CROSS JOIN LATERAL (
    SELECT array_agg(x.body ORDER BY x.level) AS hints
    FROM geo.hint x
    WHERE x.org_id = pv.org_id AND x.problem_version_id = pv.id) h
WHERE p.org_id = :'platform' AND p.id = :'problem' AND p.source = 'catalog' AND p.archived_at IS NULL;
