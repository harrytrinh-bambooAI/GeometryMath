SELECT count(*) FROM geo.event_log e WHERE e.org_id = :'org' AND e.occurred_at > now() - interval '1 day';
