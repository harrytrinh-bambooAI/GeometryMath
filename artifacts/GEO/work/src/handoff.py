#!/usr/bin/env python3
"""In tin handoff (ra stdout) và ghi status.md cho một epic. Dùng: handoff.py <work> <E n> <M-nnn> <dòng lint> <ghi chú NFR>"""
import re, sys, os
work, e, mid, lint, note = sys.argv[1:6]
body = open(os.path.join(work, 'src', e + '_body.md'), encoding='utf-8').read()
full = re.findall(r'(?m)^\*\*(US-GEO-(\d{3})-((?:SC|FC)\d+)) —([^\n]*)', body)
stories = sorted(set(s for _, s, _, _ in full))
sc = sum(1 for a in full if a[2].startswith('SC')); fc = len(full) - sc
def comp(items):
    out = []
    for s in stories:
        xs = [k for _, st, k, _ in items if st == s]
        if xs: out.append(s + '-' + ', '.join(xs))
    return ' · '.join(out)
prov = [a for a in full if 'PROVISIONAL' in a[3]]
refs = sorted(set(re.findall(r'(?:D-GEO-\d{3}|QA-GEO-\d\d)', ' '.join(re.findall(r'PROVISIONAL[^\n]*', ' '.join(a[3] for a in prov))))))
print('''
### %s · handoff · from: BA → to: QC
- ref: `work/epics/%s/stories_ac.md` (sinh từ `work/src/%s_body.md`)
- content: %s xong: %d story (US-GEO-%s…%s), %d AC (SC %d / FC %d). AC id: %s. PROVISIONAL (%d): %s; mã giả định: %s. %s Lint: %s.
- source: `work/04_story_map.md` (%s); `work/03_gate3_preliminary_stories.md`; `architecture/06_nfr.md`
- impact: QC đối chiếu case %s với các AC id trên; AC PROVISIONAL thì case cũng PROVISIONAL cùng mã.
- needed before: CP-3''' % (mid, e, e, e, len(stories), stories[0], stories[-1], len(full), sc, fc, comp(full), len(prov), comp(prov) or 'không có', ', '.join(refs) or '—', note, lint, e, e))
open(os.path.join(work, 'epics', e, 'status.md'), 'w', encoding='utf-8').write('''# %s — status

| Stage | Stories | AC | TC | V1–V14 | Adversarial review | R3 | Last update |
|---|---|---|---|---|---|---|---|
| BA: story Dev-Ready và AC đã bàn giao cho QC (tin %s); chờ cross-review | %d (US-GEO-%s…%s) | %d (SC %d / FC %d; PROVISIONAL %d) | (QC) | lint_stories: %s; V14 trên bản đồ: đã chạy (XF-01…09) | chưa | Not reviewed (TBD) | 22-11-2026 |
''' % (e, mid, len(stories), stories[0], stories[-1], len(full), sc, fc, len(prov), lint))
