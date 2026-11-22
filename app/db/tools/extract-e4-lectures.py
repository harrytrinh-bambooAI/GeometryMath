#!/usr/bin/env python3
"""E4 · Trích 10 bài giảng của bản mẫu tương tác (Design/prototype/index.html, khối KB) thành
app/server/src/main/resources/knowledge/lectures.json (US-GEO-412, D-GEO-030, 137). Chạy một lần từ gốc repo:
python3 app/db/tools/extract-e4-lectures.py. Tệp JSON là nguồn của LectureImporter và của seed E2E."""
import json
import re
import sys
from pathlib import Path

root = Path(sys.argv[1]) if len(sys.argv) > 1 else Path(__file__).resolve().parents[3]
s = (root / 'Design/prototype/index.html').read_text(encoding='utf-8')
blk = s[s.index('const KB={'):s.index('const KBP=')]
SLUG = {
    'dvm': 'duong-thang-vuong-goc-voi-mat-phang', 'hc': 'hinh-chieu-vuong-goc', 'gdm': 'goc-giua-duong-thang-va-mat-phang',
    'kcss': 'khoang-cach-giua-hai-mat-phang-song-song', 'ttlt': 'the-tich-khoi-lang-tru',
    'sxqn': 'dien-tich-xung-quanh-hinh-non', 'dttp': 'dien-tich-toan-phan-hinh-tru',
    'tslg': 'ti-so-luong-giac-trong-tam-giac-vuong', 'cheo': 'hai-duong-thang-cheo-nhau', 'mcnt': 'mat-cau-ngoai-tiep',
}
out = []
for k, slug in SLUG.items():
    m = re.search(r"\n " + k + r":\{t:'([^']*)',src:'(gv|ai)'(?:,sec:'([^']*)')?.*?terms:\[([^\]]*)\],\s*body:\[(.*?)\],\s*sum:",
                  blk, re.S)
    t, src, sec, terms, body = m.groups()
    kws = [t.lower()] + re.findall(r"'([^']*)'", terms)
    paras = re.findall(r"'((?:[^'\\]|\\.)*)'", body)
    out.append({
        'slug': slug, 'title': t,
        'source': 'teacher_authored' if src == 'gv' else 'ai_written',
        'sourceRef': f'Giáo án Hình không gian 11A1 (bản mẫu tương tác), {sec}' if src == 'gv' else None,
        'keywords': list(dict.fromkeys(kws)),
        'body': '\n\n'.join(paras),
    })
doc = {
    '_doc': 'E4 · Kho bài giảng mẫu (US-GEO-412, F-GEO-090, F-GEO-091; D-GEO-030, 137, 181): 7 bài nguồn "Giáo án mẫu" '
            '(teacher_authored, chưa có người soát) và 3 bài "AI soạn" (ai_written). Nội dung lấy từ bản mẫu tương tác '
            'Design/prototype/index.html (tham chiếu hành vi; dữ liệu mẫu PROVISIONAL). Không chứa đề, bước giải hay đáp số '
            'của đề kho (BR-GEO-201). Nạp bằng LectureImporter (vai geo_content); seed E2E '
            'app/db/seed/e2e/400_e4_lectures.sql sinh từ tệp này bằng app/db/tools/gen-e4-lectures.py.',
    'lectures': out,
}
dst = root / 'app/server/src/main/resources/knowledge/lectures.json'
dst.write_text(json.dumps(doc, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
for lec in out:
    print(lec['slug'], lec['source'], len(lec['body']), lec['keywords'])
