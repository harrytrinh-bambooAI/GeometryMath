-- T-261126-28 · S3-E1 + US-S3E5-02: bổ sung dữ liệu mẫu chung F-S3 (910_s3_core.sql) theo cột mới của V315.
-- Chỉ dev/E2E. Không đổi hàng của task khác ngoài ba lớp F-S3 (năm học + cách vào lớp bằng mã, câu hỏi mở của T-42).
-- * Lớp F-S3-010…012: năm học 2026–2027 (F-S3-010), vào lớp bằng mã HK- có duyệt (join_mode = 'code').
-- * Em Chờ duyệt của 910 đã có left_at (trigger V315: chỉ Đang học mới có left_at NULL).
-- * Văn bản thông báo riêng tư bản 1.1 (KHÔNG hiện hành): ca US-S3E5-02-SC4/FC4 bật bản 1.1 riêng cho một người dùng
--   qua móc /test-hooks/s3e5/privacy/current, không đổi bản hiện hành của mọi người.

UPDATE class SET academic_year = '2026–2027', join_mode = 'code'
WHERE org_id = '00000000-0000-7000-8002-000000000100'
  AND id IN ('00000000-0000-7000-8003-000000000010', '00000000-0000-7000-8003-000000000011',
             '00000000-0000-7000-8003-000000000012');

INSERT INTO privacy_notice (version, title, body, is_current)
SELECT '1.1', 'Thông báo riêng tư', body || E'\nBản 1.1: thêm mục công tắc trợ lý AI theo lớp do thầy cô quyết định.', false
FROM privacy_notice WHERE version = '1.0'
ON CONFLICT (version) DO NOTHING;
