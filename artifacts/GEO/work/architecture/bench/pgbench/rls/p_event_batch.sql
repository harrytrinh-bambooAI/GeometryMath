-- Mức A: lô 50 sự kiện + cập nhật tiến độ, commit không đồng bộ (ADR-011). Trọng số: 35%
\set i random(1, 19900)
SELECT org_id AS org, assignment_id AS asg, student_id AS stu FROM bench.pairs WHERE id = :i \gset
BEGIN;
SELECT set_config('app.org_id', :org, true);
SET LOCAL synchronous_commit = off;
INSERT INTO geo.event_log (occurred_at, org_id, assignment_id, user_id, kind, arg)
SELECT now() - g * interval '100 milliseconds', :org::uuid, :asg::uuid, :stu::uuid, 'tool'::geo.event_kind, (1 + g % 7)::smallint
FROM generate_series(1, 50) g;
INSERT INTO geo.assignment_progress AS ap (org_id, assignment_id, user_id, first_opened_at, last_active_at, tool_count, active_seconds)
VALUES (:org::uuid, :asg::uuid, :stu::uuid, now(), now(), 50, 5)
ON CONFLICT (org_id, assignment_id, user_id) DO UPDATE SET last_active_at = EXCLUDED.last_active_at, tool_count = ap.tool_count + EXCLUDED.tool_count, active_seconds = ap.active_seconds + EXCLUDED.active_seconds + CASE WHEN EXCLUDED.last_active_at - ap.last_active_at BETWEEN interval '0' AND interval '30 minutes' THEN extract(epoch FROM EXCLUDED.last_active_at - ap.last_active_at)::int ELSE 0 END, updated_at = now();
COMMIT;
