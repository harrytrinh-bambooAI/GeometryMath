-- role: owner
-- lint-allow: R-Q14 (thân của hàm SECURITY DEFINER fn_get_solution_teacher)
-- lint-allow: R-Q04 (tra khóa chính: 1 hàng)
-- Thân của geo.fn_get_solution_teacher (D-GEO-153, RV-04): giáo viên (mẫu hoặc đã xác nhận, PO-18) chỉ xem lời giải của ĐỀ KHO nền tảng
-- (problem.source = 'catalog', org_id = platform). Đề tự nhập hay nội dung trường khác: 0 hàng.
:explain
SELECT s.problem_version_id, s.final_answer, s.steps, s.schema_version, s.ai_authored
FROM geo.app_user u
JOIN geo.problem_version pv ON pv.org_id = :'platform' AND pv.id = :'problem_pv'
JOIN geo.problem p          ON p.org_id = pv.org_id AND p.id = pv.problem_id AND p.source = 'catalog'
JOIN geo.solution s         ON s.org_id = pv.org_id AND s.problem_version_id = pv.id
WHERE u.org_id = :'org' AND u.id = :'teacher' AND u.role = 'teacher' AND u.deactivated_at IS NULL
  AND u.account_kind IN ('sample', 'verified');
