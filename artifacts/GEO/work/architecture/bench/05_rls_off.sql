DO $$ DECLARE t text; BEGIN
  FOREACH t IN ARRAY ARRAY['class','class_member','app_user','assignment','assignment_progress','event_log','qa_turn','audit_log','problem','problem_version','scene_build','hint','lecture','keyword'] LOOP
    EXECUTE format('DROP POLICY IF EXISTS tenant_iso ON geo.%I', t);
    EXECUTE format('DROP POLICY IF EXISTS tenant_or_platform ON geo.%I', t);
    EXECUTE format('ALTER TABLE geo.%I DISABLE ROW LEVEL SECURITY', t);
  END LOOP; END $$;
