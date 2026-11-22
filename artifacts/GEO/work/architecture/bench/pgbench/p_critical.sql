-- Mức B: xem lời giải = cổng (fn_get_solution) + sự kiện bền 'unlock' lũy đẳng (fn_record_durable), commit đồng bộ. Trọng số: 5% (đỉnh giờ mở: nhân lên)
\set i random(1, 19900)
SELECT org_id AS org, released_assignment_id AS asg, student_id AS stu FROM bench.pairs WHERE id = :i \gset
BEGIN;
SELECT status, problem_version_id FROM geo.fn_get_solution(:org::uuid, :stu::uuid, :asg::uuid);
SELECT geo.fn_record_durable(:org::uuid, :stu::uuid, :asg::uuid, gen_random_uuid(), 'unlock');
COMMIT;
