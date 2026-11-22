SELECT l.id FROM geo.lecture l WHERE l.org_id = :'org' AND l.title LIKE '%goc%' LIMIT 10;
