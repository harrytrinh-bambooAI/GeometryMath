-- role: app
-- Hỏi đáp tìm nguồn (PO-33): từ câu hỏi lấy tối đa 5 bài giảng làm nguồn. v2: xếp hạng trên TẬP ỨNG VIÊN GIỚI HẠN 200 (không xếp hạng mọi bài khớp),
-- rồi mới lấy đoạn đầu thân bài (600 ký tự) của đúng 5 bài được chọn. Lịch sử tối ưu: queries/history/Q12_v1_rank_all_matches.sql.
:explain
WITH q AS (SELECT websearch_to_tsquery('simple', geo.f_unaccent(lower(:'q'))) AS tsq),
cand AS MATERIALIZED (
    SELECT l.id, l.search_tsv
    FROM geo.lecture l, q
    WHERE l.org_id = :'platform' AND l.search_tsv @@ q.tsq
    LIMIT 200),
top5 AS (
    SELECT c.id
    FROM cand c, q
    ORDER BY ts_rank(c.search_tsv, q.tsq) DESC, c.id
    LIMIT 5)
SELECT l.id, l.title, l.source_kind, l.approval_status, l.source_ref, left(l.body, 600) AS excerpt
FROM top5 t
JOIN geo.lecture l ON l.org_id = :'platform' AND l.id = t.id;
