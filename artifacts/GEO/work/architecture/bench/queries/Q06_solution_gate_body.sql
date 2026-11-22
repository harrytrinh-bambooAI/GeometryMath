-- role: owner
-- lint-allow: R-Q04 (tra khóa chính: 1 hàng)
-- lint-allow: R-Q14 (thân của hàm SECURITY DEFINER duy nhất được đọc bảng solution)
-- Nhánh bài lớp của geo.fn_solution_gate + geo.fn_get_solution, viết thành một câu để xem kế hoạch (chạy với vai chủ vì geo_app
-- KHÔNG đọc được solution). Đề của bài lớp luôn là đề kho: content_org_id = platform (CHECK assignment_content_ck).
:explain
SELECT s.problem_version_id, s.final_answer, s.steps, s.schema_version, s.ai_authored, a.release_rev
FROM geo.assignment a
JOIN geo.solution s ON s.org_id = a.content_org_id AND s.problem_version_id = a.problem_version_id
WHERE a.org_id = :'org' AND a.id = :'assignment'
  AND a.kind = 'class'
  AND EXISTS (SELECT 1 FROM geo.class_member cm
              WHERE cm.org_id = a.org_id AND cm.class_id = a.class_id AND cm.user_id = :'student' AND cm.left_at IS NULL)
  AND a.release_at IS NOT NULL AND a.release_at <= now();
