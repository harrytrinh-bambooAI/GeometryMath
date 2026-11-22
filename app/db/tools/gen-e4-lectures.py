#!/usr/bin/env python3
"""E4 · Sinh seed E2E app/db/seed/e2e/400_e4_lectures.sql từ app/server/src/main/resources/knowledge/lectures.json
(một nguồn: LectureImporter nạp cùng tệp JSON ở môi trường thật). Chạy: python3 app/db/tools/gen-e4-lectures.py."""
import json
from pathlib import Path

app = Path(__file__).resolve().parents[2]
doc = json.loads((app / 'server/src/main/resources/knowledge/lectures.json').read_text(encoding='utf-8'))


def q(v):
    return 'NULL' if v is None else "'" + str(v).replace("'", "''") + "'"


lines = [
    '-- E4 · KHO BÀI GIẢNG MẪU (US-GEO-412; fixture F-GEO-090, F-GEO-091, F-GEO-092; D-GEO-030, 137, 181).',
    '-- SINH TỰ ĐỘNG bởi app/db/tools/gen-e4-lectures.py từ server/src/main/resources/knowledge/lectures.json: không sửa tay.',
    '-- Nội dung hệ thống của org nền tảng (mọi trường đọc được, BR-GEO-110). 7 bài "Giáo án mẫu" human_reviewed=false',
    '-- (dòng "Nội dung đang chờ giáo viên soát"); 3 bài "AI soạn". Không bài nào ở trạng thái "Giáo viên đã duyệt" (D-GEO-028).',
]
for i, lec in enumerate(doc['lectures'], start=1):
    lid = f'00000000-0000-7000-8000-{900400 + i:012d}'
    lines.append(
        'INSERT INTO lecture (id, org_id, subject_id, slug, title, body, source_kind, approval_status, source_ref, human_reviewed) '
        f"VALUES ('{lid}', platform_org_id(), 1, {q(lec['slug'])}, {q(lec['title'])}, {q(lec['body'])}, "
        f"'{lec['source']}', 'ai_drafted', {q(lec['sourceRef'])}, false);")
    for kw in lec['keywords']:
        lines.append(
            f"INSERT INTO keyword (org_id, term_key, term, lecture_id) VALUES (platform_org_id(), {q(kw.lower())}, {q(kw)}, '{lid}');")
out = app / 'db/seed/e2e/400_e4_lectures.sql'
out.write_text('\n'.join(lines) + '\n', encoding='utf-8')
print(f'{out}: {len(doc["lectures"])} bài giảng')
