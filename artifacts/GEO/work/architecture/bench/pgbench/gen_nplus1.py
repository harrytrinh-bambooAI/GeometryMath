# Sinh kịch bản N+1 mô phỏng: 1 câu lấy 20 id bài rồi 20 x 2 câu lẻ (đề + tiến độ) thay vì một câu JOIN như Q02.
import os
out = ["-- ĐỐI CHỨNG N+1 (mẫu bị cấm R-Q02): 1 + 40 câu lệnh cho trang 20 bài. Sinh bởi gen_nplus1.py",
       "\\set i random(1, 19900)",
       "SELECT org_id AS org, student_id AS stu, class_id AS cls FROM bench.pairs WHERE id = :i \\gset",
       "SELECT array_agg(x.id) AS ids FROM (SELECT id FROM geo.assignment WHERE org_id = :org::uuid AND class_id = :cls::uuid AND kind = 'class' ORDER BY id DESC LIMIT 20) x \\gset"]
for k in range(1, 11):
    out.append(f"SELECT p.title, pv.verify_status FROM geo.assignment x JOIN geo.problem_version pv ON pv.id = x.problem_version_id JOIN geo.problem p ON p.org_id = pv.org_id AND p.id = pv.problem_id WHERE x.org_id = :org::uuid AND x.id = (:ids::uuid[])[{k}];")
    out.append(f"SELECT ap.hint_level_max, ap.solution_viewed_at FROM geo.assignment_progress ap WHERE ap.org_id = :org::uuid AND ap.assignment_id = (:ids::uuid[])[{k}] AND ap.user_id = :stu::uuid;")
open(os.path.join(os.path.dirname(__file__), 'p_nplus1.sql'), 'w').write("\n".join(out) + "\n")
