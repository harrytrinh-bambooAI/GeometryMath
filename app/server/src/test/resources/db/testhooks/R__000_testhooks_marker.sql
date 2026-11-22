-- Dấu hiệu cơ chế móc kiểm thử (D-GEO-169) đã nạp. CHỈ ở profile test (classpath kiểm thử).
-- Tệp móc thật (ví dụ fn_test_set_release(p_now), vai kiểm thử) do ARCH viết, đặt cạnh tệp này.
CREATE OR REPLACE FUNCTION geo_testhooks_loaded() RETURNS boolean
    LANGUAGE sql IMMUTABLE AS 'SELECT true';
