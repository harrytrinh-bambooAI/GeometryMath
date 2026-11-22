-- =====================================================================================
-- MÓC KIỂM THỬ — CHỈ Ở PROFILE TEST (D-GEO-169, RV-24). KHÔNG BAO GIỜ đưa vào migration của profile chạy thật.
-- Đội mã đặt tệp này ở thư mục migration riêng của profile test (ví dụ src/test/resources/db/test-hooks), Flyway của profile
-- chạy thật không quét thư mục đó. Nạp sau 03_schema.sql:  psql -d geo_arch2 -f bench/test_hooks.sql
-- Mục đích: ca ranh giới "đúng R" (TC-GEO-0657, 0661, 0695, 0633, 0440) trong CSDL: đặt release_at := p_now (kể cả bằng now()
-- của giao dịch hoặc ở quá khứ), điều fn_set_release cố ý cấm. Kiểm qua API thì dùng R = giờ máy chủ + 2 giây rồi chờ (D-GEO-169).
-- =====================================================================================
SET search_path = geo, public;
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'geo_tester') THEN CREATE ROLE geo_tester NOLOGIN; END IF;
END $$;
GRANT geo_app TO geo_tester;          -- vai kiểm thử = quyền của ứng dụng + móc kiểm thử

CREATE OR REPLACE FUNCTION geo.fn_test_set_release(p_org uuid, p_assignment uuid, p_now timestamptz)
RETURNS integer LANGUAGE plpgsql SECURITY DEFINER SET search_path = geo, pg_temp
AS $$
DECLARE v_rev integer;
BEGIN
  UPDATE assignment a
     SET release_at = p_now, release_rev = a.release_rev + 1, created_at = LEAST(a.created_at, p_now)
   WHERE a.org_id = p_org AND a.id = p_assignment AND a.kind = 'class'
  RETURNING a.release_rev INTO v_rev;
  RETURN v_rev;     -- NULL nếu không có bài lớp
END $$;
COMMENT ON FUNCTION geo.fn_test_set_release IS 'Móc kiểm thử D-GEO-169: đặt giờ mở bất kỳ, bỏ qua máy trạng thái. CHỈ profile test; không ghi audit_log.';
REVOKE ALL ON FUNCTION geo.fn_test_set_release(uuid, uuid, timestamptz) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION geo.fn_test_set_release(uuid, uuid, timestamptz) TO geo_tester;
