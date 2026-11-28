#!/usr/bin/env python3
"""Kiểm BUG-LIST.md / BUG-LIST.html (E2E-S23-20261128). Thoát 0 khi đạt, 1 khi trượt.
- Mỗi lỗi (mã BUG-S23-NNN) có >= 1 ảnh data URI trong cả .md và .html.
- Không dẫn link / đường dẫn tới tệp cục bộ: mọi `](` và `src=` đều là `data:`; không có `href=` trỏ tệp.
- Mã lỗi không trùng (mỗi mã đúng 1 mục chi tiết).
- Tổng số lỗi khớp REPORT.md ("Tổng lỗi").
Phiên bản: v1.0 · 2026-11-28
"""
import os, re, sys

here = os.path.dirname(os.path.abspath(__file__))
md = open(os.path.join(here, 'BUG-LIST.md'), encoding='utf-8').read()
ht = open(os.path.join(here, 'BUG-LIST.html'), encoding='utf-8').read()
rep = open(os.path.join(here, 'REPORT.md'), encoding='utf-8').read()
errs = []
ID = r'BUG-S23-\d{3}'

# Mục chi tiết
md_secs = re.split(rf'(?m)^## (?=({ID}))', md)
md_ids = re.findall(rf'(?m)^## ({ID})\b', md)
ht_ids = re.findall(rf'<section class="bug" id="({ID})"', ht)
for name, ids in (('md', md_ids), ('html', ht_ids)):
    dup = {i for i in ids if ids.count(i) > 1}
    if dup:
        errs.append(f'{name}: mã lỗi trùng {sorted(dup)}')
if set(md_ids) != set(ht_ids):
    errs.append(f'md và html lệch mã lỗi: {sorted(set(md_ids) ^ set(ht_ids))}')

# Ảnh mỗi lỗi
for i in md_ids:
    body = re.search(rf'(?ms)^## {i}\b.*?(?=^## |\Z)', md).group(0)
    if not re.search(r'!\[[^\]]*\]\(data:image/(png|jpeg);base64,[A-Za-z0-9+/=]{100,}\)', body):
        errs.append(f'md {i}: thiếu ảnh data URI')
for i in ht_ids:
    body = re.search(rf'(?s)<section class="bug" id="{i}">.*?</section>', ht).group(0)
    if not re.search(r'<img[^>]+src="data:image/(png|jpeg);base64,[A-Za-z0-9+/=]{100,}"', body):
        errs.append(f'html {i}: thiếu ảnh data URI')

# Không link tệp cục bộ
for m in re.finditer(r'\]\(([^)]{0,40})', md):
    if not m.group(1).startswith('data:'):
        errs.append(f'md: link không phải data URI: ]({m.group(1)}')
for m in re.finditer(r'\b(src|href)\s*=\s*["\']?([^"\'\s>]{0,40})', ht):
    if not m.group(2).startswith('data:') and not m.group(2).startswith('#'):
        errs.append(f'html: {m.group(1)}= không phải data URI: {m.group(2)}')
for name, s in (('md', md), ('html', ht)):
    if re.search(r'file://', s):
        errs.append(f'{name}: có file://')

# Ảnh <= 300 KB (theo base64 giải mã ước tính)
for name, s in (('md', md), ('html', ht)):
    for b in re.findall(r'data:image/(?:png|jpeg);base64,([A-Za-z0-9+/=]+)', s):
        if len(b) * 3 // 4 > 300_000:
            errs.append(f'{name}: ảnh > 300 KB')

# Tổng khớp REPORT
m = re.search(r'\*\*Tổng lỗi\*\*\s*\|\s*\*\*(\d+)\*\*', rep)
if not m:
    errs.append('REPORT.md: không tìm thấy dòng "Tổng lỗi"')
elif int(m.group(1)) != len(md_ids):
    errs.append(f'REPORT tổng lỗi {m.group(1)} != BUG-LIST {len(md_ids)}')
mm = re.search(r'Tổng lỗi: \*\*(\d+)\*\*', md)
if not mm or int(mm.group(1)) != len(md_ids):
    errs.append('BUG-LIST.md: dòng "Tổng lỗi" lệch số mục')

if errs:
    print('TRƯỢT'); [print(' -', e) for e in errs]; sys.exit(1)
print(f'ĐẠT: {len(md_ids)} lỗi, mỗi lỗi có ảnh data URI ở .md và .html, không link tệp, mã không trùng, khớp REPORT')
