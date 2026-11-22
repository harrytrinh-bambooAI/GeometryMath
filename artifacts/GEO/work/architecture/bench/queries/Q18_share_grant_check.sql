-- role: app
-- Chặng 7 (chừa chỗ): trường nhận có cấp phép còn hiệu lực cho tài nguyên không.
:explain
SELECT g.id, g.valid_until
FROM geo.share_grant g
WHERE g.grantee_org_id = :'grantee_org' AND g.resource_kind = 'lecture'
  AND g.resource_id = :'grant_res'
  AND g.status = 'approved' AND g.valid_from <= now() AND g.valid_until > now()
LIMIT 1;
