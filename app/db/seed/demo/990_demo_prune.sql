-- Môi trường TRÌNH DIỄN chỉ có dữ liệu mẫu (US-GEO-601-FC1, BR-GEO-221; D-GEO-054): chạy SAU bộ e2e/*.sql để gỡ mọi dữ
-- liệu chỉ dùng cho kiểm thử — trường "THCS Mẫu C" (kiểm cách ly) và các tổ chức thử kiểu group ("Nhóm thử E3…E6").
-- Dữ liệu thử tải ("Trường thử …") nằm ở CSDL riêng geo_bench nên không bao giờ có ở đây.
-- Cách chạy: scripts/seed-reset.sh geo_dev rồi psql -f db/seed/demo/990_demo_prune.sql (vai chủ sở hữu). Trong giao dịch,
-- tạm tắt trigger (session_replication_role = replica) để xóa được cả bảng chỉ-thêm của các tổ chức thử.
-- SET (không LOCAL) + RESET ở cuối: chạy được cả bằng psql -f không giao dịch lẫn trong giao dịch (soát E6 m6).
-- CHỈ chạy trên CSDL trình diễn/dev dựng từ seed: script xóa MỌI tổ chức kind = 'group' và "THCS Mẫu C".
SET session_replication_role = replica;
DO $$
DECLARE t record; v_orgs uuid[];
BEGIN
  SELECT array_agg(id) INTO v_orgs FROM org WHERE kind = 'group' OR name = 'THCS Mẫu C';
  IF v_orgs IS NULL THEN RETURN; END IF;
  FOR t IN SELECT c.table_name FROM information_schema.columns c
           JOIN information_schema.tables x ON x.table_schema = c.table_schema AND x.table_name = c.table_name
           WHERE c.table_schema = 'public' AND c.column_name = 'org_id' AND x.table_type = 'BASE TABLE'
             AND c.table_name <> 'org'
  LOOP
    EXECUTE format('DELETE FROM public.%I WHERE org_id = ANY ($1)', t.table_name) USING v_orgs;
  END LOOP;
  DELETE FROM org WHERE id = ANY (v_orgs);
END $$;
RESET session_replication_role;
