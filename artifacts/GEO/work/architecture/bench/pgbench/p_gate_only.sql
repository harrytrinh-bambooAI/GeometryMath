-- Chỉ cổng quyết định (hàm fn_get_solution), không ghi. Dùng cho ca "1000 học sinh cùng mở lời giải".
\set i random(1, 19900)
SELECT org_id AS org, released_assignment_id AS asg, student_id AS stu FROM bench.pairs WHERE id = :i \gset
SELECT status, problem_version_id, final_answer FROM geo.fn_get_solution(:org::uuid, :stu::uuid, :asg::uuid);
