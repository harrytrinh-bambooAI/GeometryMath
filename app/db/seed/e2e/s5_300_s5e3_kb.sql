-- T-261128-56 (S5-E3): dữ liệu mẫu kho tri thức F-S5-040 (cô Lan) và F-S5-041 (thầy Khôi). Mã cố định: fixture-ids.d/s5.json
-- (F-S5-040 = tài liệu TL-01, F-S5-041 = TL-09) + fixture-ids.d/s5e3.json (TL-02, TL-03, gắn kết).
--   TL-01 "Ghi chú Khoảng cách 11A1": v1 Lưu trữ, v2 Đang dùng (mục "Hệ thức 1/AH²" gắn KT11-C07-CT-003), gắn lớp 11A1.
--   TL-02 "Phiếu ôn chương VII": v1 Chờ duyệt.
--   TL-03 "Bài giảng AI · Góc giữa hai mặt phẳng": v1 Nháp, nhãn "AI soạn" (BR-S5E3-05).
--   TL-09 "Hệ thức lượng 10B" (thầy Khôi): v1 Đang dùng, gắn 10B.
-- Băm nội dung: sha256 của nội dung chữ thường, gộp khoảng trắng (như KbSplitter.hash).

CREATE TEMP TABLE s5e3_seed_doc (id uuid, owner uuid, code text, title text, ai boolean);
INSERT INTO s5e3_seed_doc VALUES
  ('00000000-0000-7000-8005-000000000040', '00000000-0000-7000-8003-000000000001', 'TL-01', 'Ghi chú Khoảng cách 11A1', false),
  ('00000000-0000-7000-8005-300000000402', '00000000-0000-7000-8003-000000000001', 'TL-02', 'Phiếu ôn chương VII', false),
  ('00000000-0000-7000-8005-300000000403', '00000000-0000-7000-8003-000000000001', 'TL-03', 'Bài giảng AI · Góc giữa hai mặt phẳng', true),
  ('00000000-0000-7000-8005-000000000041', '00000000-0000-7000-8003-000000000002', 'TL-09', 'Hệ thức lượng 10B', false);

INSERT INTO kb_document (id, org_id, owner_id, code, title, ai_authored)
SELECT id, '00000000-0000-7000-8002-000000000100', owner, code, title, ai FROM s5e3_seed_doc
ON CONFLICT DO NOTHING;

-- (tài liệu, phiên bản, trạng thái, từng dùng, nội dung Markdown)
CREATE TEMP TABLE s5e3_seed_ver (doc uuid, owner uuid, version int, status text, was_active boolean, kind text, md text);
INSERT INTO s5e3_seed_ver VALUES
  ('00000000-0000-7000-8005-000000000040', '00000000-0000-7000-8003-000000000001', 1, 'archived', true, 'manual',
   E'# Ghi chú Khoảng cách 11A1\n\n## Khoảng cách từ điểm đến mặt phẳng\nKhoảng cách từ điểm M đến mặt phẳng (P) bằng độ dài MH, với H là hình chiếu vuông góc của M lên (P).\n'),
  ('00000000-0000-7000-8005-000000000040', '00000000-0000-7000-8003-000000000001', 2, 'active', true, 'manual',
   E'# Ghi chú Khoảng cách 11A1\n\n## Khoảng cách từ điểm đến mặt phẳng\nKhoảng cách từ điểm M đến mặt phẳng (P) bằng độ dài MH, với H là hình chiếu vuông góc của M lên (P). Muốn tính khoảng cách, dựng hình chiếu rồi tính độ dài đoạn vuông góc.\n\n## Hệ thức 1/AH²\nMã KT11-C07-CT-003. Nếu SA ⊥ (ABC), AK ⊥ BC, AH ⊥ SK thì AH ⊥ (SBC) và 1/AH² = 1/SA² + 1/AK². Dùng để tính khoảng cách từ A đến (SBC).\n\n## Góc giữa hai mặt phẳng\nGóc giữa hai mặt phẳng là góc giữa hai đường thẳng lần lượt vuông góc với giao tuyến, nằm trong hai mặt.\n'),
  ('00000000-0000-7000-8005-300000000402', '00000000-0000-7000-8003-000000000001', 1, 'pending', false, 'manual',
   E'# Phiếu ôn chương VII\n\n## Câu 1\nCho hình chóp S.ABC có SA vuông góc với đáy. Chứng minh BC vuông góc với mặt phẳng (SAB).\n'),
  ('00000000-0000-7000-8005-300000000403', '00000000-0000-7000-8003-000000000001', 1, 'draft', false, 'ai',
   E'# Bài giảng AI · Góc giữa hai mặt phẳng\n\n## Định nghĩa\nGóc giữa hai mặt phẳng là góc giữa hai đường thẳng lần lượt vuông góc với hai mặt phẳng đó.\n\n## Cách xác định\nTìm giao tuyến, dựng hai đường thẳng cùng vuông góc với giao tuyến tại một điểm, nằm trong hai mặt.\n'),
  ('00000000-0000-7000-8005-000000000041', '00000000-0000-7000-8003-000000000002', 1, 'active', true, 'manual',
   E'# Hệ thức lượng 10B\n\n## Định lí côsin\na² = b² + c² − 2bc·cos A.\n');

INSERT INTO kb_document_version (org_id, doc_id, version, status, was_active, source_kind, content_md, content_hash,
                                 created_by, activated_at, archived_at)
SELECT '00000000-0000-7000-8002-000000000100', doc, version, status, was_active, kind, md,
       encode(sha256(convert_to(btrim(regexp_replace(lower(md), '\s+', ' ', 'g')), 'UTF8')), 'hex'),
       owner, CASE WHEN was_active THEN now() - interval '7 days' END,
       CASE WHEN status = 'archived' THEN now() - interval '3 days' END
FROM s5e3_seed_ver
ON CONFLICT DO NOTHING;

-- Mục: tách theo tiêu đề "## " như KbSplitter (tiêu đề tài liệu "# " không có thân, không thành mục).
INSERT INTO kb_section (id, org_id, doc_id, version, ord, title, body, codes)
SELECT gen_random_uuid(), '00000000-0000-7000-8002-000000000100', v.doc, v.version, p.ord,
       split_part(p.part, E'\n', 1),
       btrim(substr(p.part, length(split_part(p.part, E'\n', 1)) + 2)),
       COALESCE(ARRAY(SELECT DISTINCT m[1] FROM regexp_matches(p.part, '(KT\d{2}(?:-[A-Z0-9]+)+)', 'g') AS m), '{}')
FROM s5e3_seed_ver v
CROSS JOIN LATERAL (
  SELECT ord - 1 AS ord, part FROM unnest(string_to_array(v.md, E'\n## ')) WITH ORDINALITY AS t(part, ord)
) p
WHERE p.ord >= 1
  AND NOT EXISTS (SELECT 1 FROM kb_section s WHERE s.org_id = '00000000-0000-7000-8002-000000000100'
                    AND s.doc_id = v.doc AND s.version = v.version);

INSERT INTO kb_grant (id, org_id, doc_id, class_id, created_by)
VALUES ('00000000-0000-7000-8005-300000000410', '00000000-0000-7000-8002-000000000100', '00000000-0000-7000-8005-000000000040',
        '00000000-0000-7000-8003-000000000010', '00000000-0000-7000-8003-000000000001'),
       ('00000000-0000-7000-8005-300000000419', '00000000-0000-7000-8002-000000000100', '00000000-0000-7000-8005-000000000041',
        '00000000-0000-7000-8003-000000000012', '00000000-0000-7000-8003-000000000002')
ON CONFLICT DO NOTHING;

DROP TABLE s5e3_seed_doc, s5e3_seed_ver;
