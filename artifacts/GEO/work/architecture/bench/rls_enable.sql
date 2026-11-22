-- Bật Row-Level Security (D-GEO-127) cho các bảng đã có chính sách ở 03_schema.sql §13.
-- Đề xuất là một migration riêng, chạy SAU khi đội mã đo chi phí trên khung mã thật (mọi truy cập bọc trong withTenant(ctx) đặt app.org_id).
-- Gỡ: thay ENABLE bằng DISABLE.
SET search_path = geo, public;
DO $$
DECLARE t text;
BEGIN
  FOREACH t IN ARRAY ARRAY['app_user','class','class_member','assignment','assignment_progress','event_log','qa_turn','audit_log',
                           'problem','problem_version','scene_build','hint','problem_signature'] LOOP
    EXECUTE format('ALTER TABLE geo.%I ENABLE ROW LEVEL SECURITY', t);
  END LOOP;
END $$;
