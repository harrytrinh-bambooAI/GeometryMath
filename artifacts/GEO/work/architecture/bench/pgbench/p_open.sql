-- Mở bài: chi tiết bài (Q04) + sự kiện bền 'open' (fn_record_durable, D-GEO-162). Trọng số: 10%
\set i random(1, 19900)
SELECT org_id AS org, assignment_id AS asg, student_id AS stu FROM bench.pairs WHERE id = :i \gset
BEGIN;
SELECT a.id, a.release_at, pv.statement, pv.verify_status, p.title, sb.coords, sb.hidden_measures
FROM geo.assignment a JOIN geo.problem_version pv ON pv.org_id = a.content_org_id AND pv.id = a.problem_version_id JOIN geo.problem p ON p.org_id = pv.org_id AND p.id = pv.problem_id
CROSS JOIN LATERAL (SELECT s.coords, s.hidden_measures FROM geo.scene_build s WHERE s.org_id = pv.org_id AND s.problem_version_id = pv.id ORDER BY s.build_no DESC LIMIT 1) sb
WHERE a.org_id = :org::uuid AND a.id = :asg::uuid AND a.kind = 'class'
  AND EXISTS (SELECT 1 FROM geo.class_member cm WHERE cm.org_id = a.org_id AND cm.class_id = a.class_id AND cm.user_id = :stu::uuid AND cm.left_at IS NULL);
SELECT geo.fn_record_durable(:org::uuid, :stu::uuid, :asg::uuid, gen_random_uuid(), 'open');
COMMIT;
