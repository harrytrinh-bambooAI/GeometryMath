-- role: owner
-- lint-allow: R-Q04 (tra khóa chính: 1 hàng)
-- Phần ĐỌC của geo.fn_get_hint cho bài tự học (RV-15, D-GEO-151, 155): nếu bài tự học khớp một bài lớp đang khóa (Q16) thì
-- trả gợi ý của ĐỀ KHO của bài lớp đó, ngược lại gợi ý của đề tự nhập. Ca đo: bài tự học đang kế thừa khóa.
:explain
SELECT h.level, h.body, CASE WHEN l.id IS NULL THEN 'self' ELSE 'catalog_inherited' END AS hint_source
FROM geo.assignment a
LEFT JOIN geo.assignment l ON l.org_id = a.org_id AND l.id = geo.fn_self_lock_source(a.org_id, a.owner_user_id, a.problem_version_id)
JOIN geo.hint h ON h.org_id = COALESCE(l.content_org_id, a.content_org_id)
               AND h.problem_version_id = COALESCE(l.problem_version_id, a.problem_version_id) AND h.level = 1
WHERE a.org_id = :'inh_org' AND a.id = :'inh_assignment' AND a.kind = 'self' AND a.owner_user_id = :'inh_student';
