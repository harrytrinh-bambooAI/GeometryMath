-- FIX lô soát độc lập T-20…T-32 (REVIEW-T20-T32-20261127 mục 3, Dev-REV-B1): phòng thủ chiều sâu cho hàm SECURITY DEFINER.
-- V10 chạy REVOKE ALL ON ALL FUNCTIONS … FROM PUBLIC một lần, nhưng hàm tạo SAU V10 vẫn nhận quyền EXECUTE mặc định
-- của PUBLIC (V11, V110, V315, V386, V415, V416…). Hàm SECURITY DEFINER chạy bằng quyền chủ schema nên vai CSDL bất kỳ
-- (geo_ai, geo_content, geo_ops hoặc vai lạ) gọi được, ví dụ fn_account_purge_rows(org, người) xóa dữ liệu riêng và
-- ẩn danh hóa audit_log của người khác.
--
-- Cách làm: thu hồi EXECUTE của PUBLIC trên MỌI hàm SECURITY DEFINER trong schema public (vòng lặp trên pg_proc, nên
-- không sót hàm nào kể cả trigger), cùng hai hàm thường tạo sau V10 còn quyền PUBLIC. Quyền GRANT đích danh cho vai đã có
-- (geo_app / geo_ai / geo_content / geo_ops, xem V10 §12 và GRANT cuối mỗi migration) giữ nguyên, vì REVOKE … FROM PUBLIC
-- không đụng quyền của vai cụ thể. Không GRANT thêm:
--   * fn_account_purge_rows: chỉ gọi bên trong fn_account_delete / fn_account_purge (chạy bằng quyền chủ) — không vai nào gọi thẳng;
--   * tg_*, trg_*: hàm trigger, quyền EXECUTE chỉ xét lúc CREATE TRIGGER, không xét khi trigger chạy;
--   * fn_user_code_sha256: chỉ gọi trong fn_register_code_user / fn_account_delete (SECURITY DEFINER).
-- Mọi hàm SECURITY DEFINER đã có SET search_path = public, pg_temp; khối dưới đặt cho hàm nào thiếu (không có hàm nào
-- lúc viết, giữ để migration tự đủ).
-- Hàm SECURITY DEFINER mới sau này: IT SecurityDefinerGrantsIT (Dev-REV-B1) trượt nếu PUBLIC còn EXECUTE hoặc thiếu search_path.

DO $$
DECLARE f record;
BEGIN
  FOR f IN
    SELECT p.oid::regprocedure AS sig, p.prosecdef,
           NOT EXISTS (SELECT 1 FROM unnest(coalesce(p.proconfig, '{}')) c WHERE c LIKE 'search_path=%') AS no_path
      FROM pg_proc p JOIN pg_namespace n ON n.oid = p.pronamespace
     WHERE n.nspname = 'public' AND p.prokind = 'f'
       AND (p.prosecdef OR p.proname IN ('fn_user_code_sha256', 'trg_class_member_status_sync'))
       AND NOT EXISTS (SELECT 1 FROM pg_depend d WHERE d.objid = p.oid AND d.deptype = 'e')
  LOOP
    EXECUTE format('REVOKE ALL ON FUNCTION %s FROM PUBLIC', f.sig);
    IF f.prosecdef AND f.no_path THEN
      EXECUTE format('ALTER FUNCTION %s SET search_path = public, pg_temp', f.sig);
    END IF;
  END LOOP;
END $$;
