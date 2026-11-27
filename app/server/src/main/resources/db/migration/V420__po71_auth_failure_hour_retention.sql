-- PO-71 (PO-DECISIONS 27/11/2026): auth_failure.ip_key (HMAC của IP) giữ tối đa 1 giờ thay cho 1 ngày (BR-S3E5-37 cho phép
-- "hàng HMAC có hạn ≤ 1 giờ"); cửa sổ đếm thử sai chỉ 15 phút nên 1 giờ là đủ. Có công việc dọn xóa hằng giờ trong ứng dụng
-- (AuthFailurePurgeJob, vai geo_app) gọi fn_purge_auth_failure; fn_purge_ephemeral (geo_ops) cũng dọn theo hạn mới.
-- Không gắn vào tài khoản, không vào allowed_fields.

CREATE OR REPLACE FUNCTION fn_purge_ephemeral() RETURNS TABLE (durable_request_deleted bigint, auth_failure_deleted bigint)
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
DECLARE a bigint; b bigint;
BEGIN
  DELETE FROM durable_request d WHERE d.created_at < now() - interval '24 hours'; GET DIAGNOSTICS a = ROW_COUNT;
  DELETE FROM auth_failure f WHERE f.at < now() - interval '1 hour';             GET DIAGNOSTICS b = ROW_COUNT;
  RETURN QUERY SELECT a, b;
END $$;
COMMENT ON FUNCTION fn_purge_ephemeral IS 'Dọn khóa lũy đẳng cũ hơn 24 giờ và lần thử sai cũ hơn 1 giờ (PO-71; không phải dữ liệu sự kiện, D-GEO-128 không áp). Vai geo_ops.';

-- Dọn lần thử sai cũ hơn mốc p_cutoff (ứng dụng truyền now − 1 giờ); mốc mới hơn now − 1 giờ bị kẹp về now − 1 giờ để
-- không xóa hàng còn trong cửa sổ đếm 15 phút. Trả số hàng đã xóa.
CREATE FUNCTION fn_purge_auth_failure(p_cutoff timestamptz) RETURNS bigint
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
DECLARE n bigint;
BEGIN
  DELETE FROM auth_failure f WHERE f.at < LEAST(p_cutoff, now() - interval '1 hour'); GET DIAGNOSTICS n = ROW_COUNT;
  RETURN n;
END $$;
COMMENT ON FUNCTION fn_purge_auth_failure IS 'PO-71: hạn lưu auth_failure (HMAC IP) tối đa 1 giờ; công việc dọn hằng giờ của ứng dụng gọi.';
REVOKE ALL ON FUNCTION fn_purge_auth_failure(timestamptz) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION fn_purge_auth_failure(timestamptz) TO geo_app;

COMMENT ON TABLE auth_failure IS 'Lần đăng nhập giáo viên thất bại (D-GEO-152). Bền ở CSDL để giới hạn đúng trên mọi nút và cả khi Redis tắt (đóng khi lỗi). Hạn lưu tối đa 1 giờ (PO-71): công việc dọn hằng giờ (fn_purge_auth_failure). NGOẠI LỆ T1: không có org_id vì xảy ra trước khi biết trường.';
