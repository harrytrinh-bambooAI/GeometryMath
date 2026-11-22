-- role: app
-- lint-allow: R-Q04 (tra khóa duy nhất join_code và (org, lớp, name_key): tối đa 1 hàng)
-- lint-allow: R-Q10 (đăng nhập chưa biết trường; tra theo mã lớp toàn cục, D-GEO-051)
-- Đăng nhập nhẹ bằng tên + mã lớp (IAM-01). Một vòng truy vấn: lớp theo mã, học sinh theo tên chuẩn hóa.
:explain
SELECT c.id AS class_id, c.org_id, c.level, cm.user_id
FROM geo.class c
LEFT JOIN geo.class_member cm
       ON cm.org_id = c.org_id AND cm.class_id = c.id AND cm.name_key = :'name_key' AND cm.left_at IS NULL
WHERE c.join_code = :'join_code' AND c.archived_at IS NULL;
