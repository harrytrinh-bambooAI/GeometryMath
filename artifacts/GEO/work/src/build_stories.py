#!/usr/bin/env python3
"""Ghép tệp story của một epic: phần đầu chung (story_header.md) + bảng thông điệp trích từ
05_dictionary.md cho các MSG-GEO mà epic dùng + thân epic (E<n>_body.md) -> epics/E<n>/stories_ac.md.
Nguồn sự thật: work/src/E<n>_body.md, work/src/story_header.md, work/05_dictionary.md.
Dùng: python3 work/src/build_stories.py <thư mục work> [E1 E2 ...]   (in thống kê story / AC)
"""
import re, sys, os
work = sys.argv[1]; epics = sys.argv[2:] or ['E%d' % i for i in range(1, 8)]
src = os.path.join(work, 'src')
header = open(os.path.join(src, 'story_header.md'), encoding='utf-8').read()
msgs = {}
for line in open(os.path.join(work, '05_dictionary.md'), encoding='utf-8'):
    m = re.match(r'\| (MSG-GEO-\d\d) \|', line)
    if m:
        c = [x.strip() for x in line.strip().strip('|').split('|')]
        msgs[m.group(1)] = (c[1], c[2], c[4])
for e in epics:
    p = os.path.join(src, e + '_body.md')
    if not os.path.exists(p): continue
    body = open(p, encoding='utf-8').read()
    title = body.split('\n', 1)[0].lstrip('# ').strip(); body = body.split('\n', 1)[1]
    used = sorted(set(re.findall(r'MSG-GEO-\d\d', body)))
    rows = ['| Mã | Loại | Câu chữ (nguyên văn) | Mã lỗi API |', '|---|---|---|---|']
    for u in used:
        if u not in msgs: sys.exit('%s: %s không có trong 05_dictionary.md' % (e, u))
        rows.append('| %s | %s | %s | %s |' % ((u,) + msgs[u]))
    out = ('# %s\n\n> Tệp sinh bởi `work/src/build_stories.py` từ `work/src/%s_body.md`, `work/src/story_header.md` và `work/05_dictionary.md`; '
           'không sửa tay, sửa nguồn rồi chạy lại. Chủ sở hữu: BA.\n\n' % (title, e)) + header.rstrip() + \
          '\n\n### Thông điệp dùng trong tệp này (chép từ `work/05_dictionary.md`)\n\n' + '\n'.join(rows) + '\n\n---\n' + body
    open(os.path.join(work, 'epics', e, 'stories_ac.md'), 'w', encoding='utf-8').write(out)
    stories = re.findall(r'(?m)^## (US-GEO-\d{3}) —', body)
    acs = re.findall(r'(?m)^\*\*(US-GEO-\d{3}-(SC|FC)\d+) —[^\n]*', body)
    full = re.findall(r'(?m)^\*\*(US-GEO-\d{3}-(?:SC|FC)\d+) —([^\n]*)', body)
    ids = [a for a, _ in full]
    dup = sorted(set(i for i in ids if ids.count(i) > 1))
    prov = [a for a, t in full if 'PROVISIONAL' in t]
    nosrc = [a for a, t in full if 'Nguồn:' not in t]
    sc = sum(1 for a in ids if '-SC' in a); fc = len(ids) - sc
    print('%s: %d story, %d AC (SC %d / FC %d), PROVISIONAL %d, trùng id %s, thiếu nguồn %s' % (e, len(stories), len(ids), sc, fc, len(prov), dup, nosrc))
    if '--list' in sys.argv:
        print('  AC:', ', '.join(ids)); print('  PROVISIONAL:', ', '.join(prov))
