SELECT sb.id FROM geo.scene_build sb WHERE sb.org_id = :'org' AND sb.coords ->> 'x' = '1' LIMIT 5;
