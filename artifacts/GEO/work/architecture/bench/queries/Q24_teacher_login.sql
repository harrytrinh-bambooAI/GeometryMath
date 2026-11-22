-- role: owner
-- lint-allow: R-Q10 (trước đăng nhập chưa biết trường: tra theo selector và theo khóa IP, D-GEO-152)
-- lint-allow: R-Q04 (tra khóa duy nhất selector: 1 hàng)
-- lint-allow: R-Q05 (đếm có LIMIT 10 trên khoảng chỉ mục (ip_key, at) và (user_id, at): tối đa 10 hàng)
-- Các câu đọc bên trong geo.fn_teacher_login: đếm lần sai theo IP và theo tài khoản trong 15 phút (dừng ở 10), tra mã theo selector.
:explain
SELECT (SELECT count(*) FROM (SELECT 1 FROM geo.auth_failure f WHERE f.ip_key = decode(:'ip_key', 'hex') AND f.at > now() - interval '15 minutes' LIMIT 10) x) AS fail_ip,
       (SELECT count(*) FROM (SELECT 1 FROM geo.auth_failure f WHERE f.user_id = t.user_id AND f.at > now() - interval '15 minutes' LIMIT 10) y) AS fail_acct,
       t.id, t.org_id, t.user_id, t.verifier_hmac = decode(:'verifier', 'hex') AS ok, t.expires_at, t.revoked_at, u.account_kind
FROM geo.teacher_access_code t
JOIN geo.app_user u ON u.org_id = t.org_id AND u.id = t.user_id
WHERE t.selector = :'selector';
