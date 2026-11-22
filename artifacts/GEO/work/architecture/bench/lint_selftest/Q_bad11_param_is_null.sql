SELECT a.id FROM geo.assignment a WHERE a.org_id = :'org' AND (:'x'::text IS NULL OR a.kind::text = :'x') LIMIT 5;
