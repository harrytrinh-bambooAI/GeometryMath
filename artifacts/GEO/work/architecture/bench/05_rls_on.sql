-- Bật RLS thử nghiệm: chính sách theo cột org_id đọc từ app.org_id (đặt bằng set_config(..., true) trong giao dịch).
-- Dạng viết (SELECT current_setting(...)) để PostgreSQL tính MỘT lần (InitPlan), không tính theo từng hàng.
DO $$
DECLARE t text; platform uuid := '00000000-0000-7000-8000-000000000001';
BEGIN
  FOREACH t IN ARRAY ARRAY['class','class_member','app_user','assignment','assignment_progress','event_log','qa_turn','audit_log'] LOOP
    EXECUTE format('ALTER TABLE geo.%I ENABLE ROW LEVEL SECURITY', t);
    EXECUTE format('CREATE POLICY tenant_iso ON geo.%I FOR ALL TO geo_app USING (org_id = (SELECT current_setting(''app.org_id'')::uuid)) WITH CHECK (org_id = (SELECT current_setting(''app.org_id'')::uuid))', t);
  END LOOP;
  FOREACH t IN ARRAY ARRAY['problem','problem_version','scene_build','hint','lecture','keyword'] LOOP
    EXECUTE format('ALTER TABLE geo.%I ENABLE ROW LEVEL SECURITY', t);
    EXECUTE format('CREATE POLICY tenant_or_platform ON geo.%I FOR ALL TO geo_app USING (org_id IN (%L::uuid, (SELECT current_setting(''app.org_id'')::uuid))) WITH CHECK (org_id IN (%L::uuid, (SELECT current_setting(''app.org_id'')::uuid)))', t, platform, platform);
  END LOOP;
END $$;
