-- Bấm từ khóa (Q11). Trọng số: 12%
\set i random(1, 900)
SELECT term_key AS tk FROM bench.terms WHERE id = :i \gset
SELECT l.id, l.title, l.body, l.source_kind, l.approval_status, l.source_ref FROM geo.keyword k JOIN geo.lecture l ON l.org_id = k.org_id AND l.id = k.lecture_id
WHERE k.org_id = '00000000-0000-7000-8000-000000000001' AND k.term_key = :tk;
