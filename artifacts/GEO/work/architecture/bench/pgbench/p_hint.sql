-- Gợi ý: geo.fn_get_hint đọc gợi ý mức 1 + sự kiện bền + tiến độ trong một lần gọi (một giao dịch tự động). Trọng số: 8%
\set i random(1, 19900)
SELECT org_id AS org, assignment_id AS asg, student_id AS stu FROM bench.pairs WHERE id = :i \gset
SELECT status, level, hint_source FROM geo.fn_get_hint(:org::uuid, :stu::uuid, :asg::uuid, 1::smallint, gen_random_uuid());
