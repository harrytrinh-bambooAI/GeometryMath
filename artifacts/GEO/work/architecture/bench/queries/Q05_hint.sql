-- role: app
-- lint-allow: R-Q04 (tra khóa chính (org, version, level): 1 hàng)
-- Phần ĐỌC của geo.fn_get_hint cho bài lớp (BR-04): bài cho phép gợi ý, em thuộc lớp, mức <= mức đã xem + 1; gợi ý của đề kho được giao.
-- Hàm ghi thêm sự kiện 'hint' + tiến độ trong cùng giao dịch (đo end-to-end ở Q05c).
:explain
SELECT h.level, h.body
FROM geo.assignment a
JOIN geo.hint h ON h.org_id = a.content_org_id AND h.problem_version_id = a.problem_version_id AND h.level = 2
WHERE a.org_id = :'org' AND a.id = :'assignment' AND a.kind = 'class' AND a.allow_hint
  AND EXISTS (SELECT 1 FROM geo.class_member cm
              WHERE cm.org_id = a.org_id AND cm.class_id = a.class_id AND cm.user_id = :'student' AND cm.left_at IS NULL)
  AND 2 <= 1 + COALESCE((SELECT ap.hint_level_max FROM geo.assignment_progress ap
                         WHERE ap.org_id = a.org_id AND ap.assignment_id = a.id AND ap.user_id = :'student'), 0);
