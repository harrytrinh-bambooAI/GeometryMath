-- Mức A: lô 50 sự kiện + tiến độ qua geo.fn_record_batch (Q07 + Q08, có kiểm tư cách lớp), commit không đồng bộ (ADR-GEO-11). Trọng số: 35%
\set i random(1, 19900)
SELECT org_id AS org, assignment_id AS asg, student_id AS stu FROM bench.pairs WHERE id = :i \gset
BEGIN;
SET LOCAL synchronous_commit = off;
SELECT status, inserted FROM geo.fn_record_batch(:org::uuid, :stu::uuid, :asg::uuid,
       ARRAY(SELECT now() - g * interval '100 milliseconds' FROM generate_series(1, 50) g),
       ARRAY(SELECT 'tool'::geo.event_kind FROM generate_series(1, 50) g),
       ARRAY(SELECT (1 + g % 7)::smallint FROM generate_series(1, 50) g));
COMMIT;
