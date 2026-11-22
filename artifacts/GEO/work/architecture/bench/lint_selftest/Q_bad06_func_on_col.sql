SELECT cm.user_id FROM geo.class_member cm WHERE cm.org_id = :'org' AND lower(cm.name_key) = :'n' LIMIT 1;
