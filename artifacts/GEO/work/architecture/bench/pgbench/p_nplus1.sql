-- ĐỐI CHỨNG N+1 (mẫu bị cấm R-Q02): 1 + 40 câu lệnh cho trang 20 bài. Sinh bởi gen_nplus1.py
\set i random(1, 19900)
SELECT org_id AS org, student_id AS stu, class_id AS cls FROM bench.pairs WHERE id = :i \gset
SELECT array_agg(x.id) AS ids FROM (SELECT id FROM geo.assignment WHERE org_id = :org::uuid AND class_id = :cls::uuid AND kind = 'class' ORDER BY id DESC LIMIT 20) x \gset
SELECT p.title, pv.verify_status FROM geo.assignment x JOIN geo.problem_version pv ON pv.id = x.problem_version_id JOIN geo.problem p ON p.org_id = pv.org_id AND p.id = pv.problem_id WHERE x.org_id = :org::uuid AND x.id = (:ids::uuid[])[1];
SELECT ap.hint_level_max, ap.solution_viewed_at FROM geo.assignment_progress ap WHERE ap.org_id = :org::uuid AND ap.assignment_id = (:ids::uuid[])[1] AND ap.user_id = :stu::uuid;
SELECT p.title, pv.verify_status FROM geo.assignment x JOIN geo.problem_version pv ON pv.id = x.problem_version_id JOIN geo.problem p ON p.org_id = pv.org_id AND p.id = pv.problem_id WHERE x.org_id = :org::uuid AND x.id = (:ids::uuid[])[2];
SELECT ap.hint_level_max, ap.solution_viewed_at FROM geo.assignment_progress ap WHERE ap.org_id = :org::uuid AND ap.assignment_id = (:ids::uuid[])[2] AND ap.user_id = :stu::uuid;
SELECT p.title, pv.verify_status FROM geo.assignment x JOIN geo.problem_version pv ON pv.id = x.problem_version_id JOIN geo.problem p ON p.org_id = pv.org_id AND p.id = pv.problem_id WHERE x.org_id = :org::uuid AND x.id = (:ids::uuid[])[3];
SELECT ap.hint_level_max, ap.solution_viewed_at FROM geo.assignment_progress ap WHERE ap.org_id = :org::uuid AND ap.assignment_id = (:ids::uuid[])[3] AND ap.user_id = :stu::uuid;
SELECT p.title, pv.verify_status FROM geo.assignment x JOIN geo.problem_version pv ON pv.id = x.problem_version_id JOIN geo.problem p ON p.org_id = pv.org_id AND p.id = pv.problem_id WHERE x.org_id = :org::uuid AND x.id = (:ids::uuid[])[4];
SELECT ap.hint_level_max, ap.solution_viewed_at FROM geo.assignment_progress ap WHERE ap.org_id = :org::uuid AND ap.assignment_id = (:ids::uuid[])[4] AND ap.user_id = :stu::uuid;
SELECT p.title, pv.verify_status FROM geo.assignment x JOIN geo.problem_version pv ON pv.id = x.problem_version_id JOIN geo.problem p ON p.org_id = pv.org_id AND p.id = pv.problem_id WHERE x.org_id = :org::uuid AND x.id = (:ids::uuid[])[5];
SELECT ap.hint_level_max, ap.solution_viewed_at FROM geo.assignment_progress ap WHERE ap.org_id = :org::uuid AND ap.assignment_id = (:ids::uuid[])[5] AND ap.user_id = :stu::uuid;
SELECT p.title, pv.verify_status FROM geo.assignment x JOIN geo.problem_version pv ON pv.id = x.problem_version_id JOIN geo.problem p ON p.org_id = pv.org_id AND p.id = pv.problem_id WHERE x.org_id = :org::uuid AND x.id = (:ids::uuid[])[6];
SELECT ap.hint_level_max, ap.solution_viewed_at FROM geo.assignment_progress ap WHERE ap.org_id = :org::uuid AND ap.assignment_id = (:ids::uuid[])[6] AND ap.user_id = :stu::uuid;
SELECT p.title, pv.verify_status FROM geo.assignment x JOIN geo.problem_version pv ON pv.id = x.problem_version_id JOIN geo.problem p ON p.org_id = pv.org_id AND p.id = pv.problem_id WHERE x.org_id = :org::uuid AND x.id = (:ids::uuid[])[7];
SELECT ap.hint_level_max, ap.solution_viewed_at FROM geo.assignment_progress ap WHERE ap.org_id = :org::uuid AND ap.assignment_id = (:ids::uuid[])[7] AND ap.user_id = :stu::uuid;
SELECT p.title, pv.verify_status FROM geo.assignment x JOIN geo.problem_version pv ON pv.id = x.problem_version_id JOIN geo.problem p ON p.org_id = pv.org_id AND p.id = pv.problem_id WHERE x.org_id = :org::uuid AND x.id = (:ids::uuid[])[8];
SELECT ap.hint_level_max, ap.solution_viewed_at FROM geo.assignment_progress ap WHERE ap.org_id = :org::uuid AND ap.assignment_id = (:ids::uuid[])[8] AND ap.user_id = :stu::uuid;
SELECT p.title, pv.verify_status FROM geo.assignment x JOIN geo.problem_version pv ON pv.id = x.problem_version_id JOIN geo.problem p ON p.org_id = pv.org_id AND p.id = pv.problem_id WHERE x.org_id = :org::uuid AND x.id = (:ids::uuid[])[9];
SELECT ap.hint_level_max, ap.solution_viewed_at FROM geo.assignment_progress ap WHERE ap.org_id = :org::uuid AND ap.assignment_id = (:ids::uuid[])[9] AND ap.user_id = :stu::uuid;
SELECT p.title, pv.verify_status FROM geo.assignment x JOIN geo.problem_version pv ON pv.id = x.problem_version_id JOIN geo.problem p ON p.org_id = pv.org_id AND p.id = pv.problem_id WHERE x.org_id = :org::uuid AND x.id = (:ids::uuid[])[10];
SELECT ap.hint_level_max, ap.solution_viewed_at FROM geo.assignment_progress ap WHERE ap.org_id = :org::uuid AND ap.assignment_id = (:ids::uuid[])[10] AND ap.user_id = :stu::uuid;
