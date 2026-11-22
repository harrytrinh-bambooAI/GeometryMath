SELECT e.id FROM geo.event_log e WHERE e.org_id = :'org' AND e.user_id = :'u' LIMIT 50;
