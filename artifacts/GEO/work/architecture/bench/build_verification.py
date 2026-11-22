#!/usr/bin/env python3
"""Dựng 07_verification.md từ các tệp kết quả trong bench/results (số đo thật) cộng phần lời viết tay.
Chạy sau bench/run_all.sh:  python3 bench/build_verification.py"""
import os, re

BASE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(BASE)
R = f'{BASE}/results'

def read(p):
    return open(p, encoding='utf-8').read() if os.path.exists(p) else ''

def fnum(x, d=0):
    s = f'{float(x):,.{d}f}'
    return s.replace(',', '#').replace('.', ',').replace('#', '.')

# ---------- pgbench
pb = read(f'{R}/pgbench.txt')
def pg_line(pattern):
    m = re.search(pattern + r'.*?latency average = ([\d.]+) ms tps = ([\d.]+)', pb)
    return (m.group(1), m.group(2)) if m else ('?', '?')
rowsA = []
names = {'login': 'Đăng nhập (Q01)', 'list': 'Danh sách bài của em (Q02)', 'open': 'Mở bài: Q04 + sự kiện + tiến độ', 'event_batch': 'Lô 50 sự kiện + tiến độ (Q07 + Q08), commit không đồng bộ',
         'hint': 'Xin gợi ý: Q05 + sự kiện bền + tiến độ', 'critical': 'Xem lời giải: cổng + sự kiện bền + tiến độ (commit đồng bộ)', 'gate_only': 'Chỉ cổng `fn_get_solution`',
         'qa': 'Hỏi đáp: Q12 + qa_turn + sự kiện + tiến độ', 'keyword': 'Bấm từ khóa (Q11)', 'tracking': 'Theo dõi lớp (Q09)', 'catalog': 'Kho đề (Q13)'}
for k in ['login', 'list', 'open', 'event_batch', 'hint', 'critical', 'gate_only', 'qa', 'keyword', 'tracking', 'catalog']:
    lat, tps = pg_line(rf'\n{k}\s')
    rowsA.append(f'| {names[k]} | {fnum(lat, 2)} | {fnum(tps)} |')
tblA = "| Kịch bản (16 kết nối) | Trung bình (ms) | tps |\n|---|---|---|\n" + "\n".join(rowsA)

rowsB = []
for c in [2, 4, 8, 16, 32, 64, 128, 256]:
    lat, tps = pg_line(rf'\nc={c}\s')
    rowsB.append(f'| {c} | {fnum(tps)} | {fnum(lat, 2)} |')
tblB = "| Số kết nối | tps | Trung bình (ms) |\n|---|---|---|\n" + "\n".join(rowsB)

rowsC = []
for k, label in [('gate_only', 'Chỉ cổng quyết định (đọc)'), ('critical', 'Giao dịch đầy đủ (cổng + sự kiện `unlock` bền + tiến độ)')]:
    for c in [20, 50, 100]:
        pat = rf'\n{k}\s+c={c}\s' if k == 'critical' else rf'gate_only c={c}\s'
        lat, tps = pg_line(pat)
        rowsC.append(f'| {label} | {c} | {fnum(tps)} | {fnum(lat, 2)} |')
tblC = "| Kịch bản | Kết nối | tps | Trung bình (ms) |\n|---|---|---|---|\n" + "\n".join(rowsC)

sD = pb.split('### D.')[-1].split('### E.')[0] if '### D.' in pb else ''
def g(pat, s=sD):
    m = re.search(pat, s)
    return m.group(1) if m else '?'
dlate = g(r'above the 50\.0 ms latency limit: (\d+/\d+ \([\d.]+%\))')
dlat = g(r'latency average = ([\d.]+) ms'); dstd = g(r'latency stddev = ([\d.]+) ms'); dtps = g(r'tps = ([\d.]+)')
dskip = g(r'transactions skipped: (\d+)')
rowsE = []
for c in [1, 4, 8, 16, 32, 64, 128]:
    m = re.search(rf'### E.*?\nc={c}\s.*?latency average = ([\d.]+) ms tps = ([\d.]+)', pb, re.S)
    mm = None
    for mm_ in re.finditer(rf'\nc={c}\s+[^\n]*', pb.split('### E.')[-1] if '### E.' in pb else ''):
        mm = mm_
        break
    if mm:
        t = re.search(r'latency average = ([\d.]+) ms tps = ([\d.]+)', mm.group(0))
        if t: rowsE.append(f'| {c} | {fnum(t.group(2))} | {fnum(t.group(1), 2)} |')
tblE = "| Số kết nối | tps | Trung bình (ms) |\n|---|---|---|\n" + "\n".join(rowsE)

# ---------- RLS (hai lần chạy nếu có)
rls = read(f'{R}/rls.txt')
rls_rows = []
for sec in re.split(r'\n### ', '\n' + rls):
    m = re.match(r'(list|tracking|event_batch|open)\n', sec)
    if not m: continue
    vals = re.findall(r'tps = ([\d.]+)', sec)
    if len(vals) >= 3:
        a, b, c = [float(x) for x in vals[:3]]
        rls_rows.append(f'| {m.group(1)} | {fnum(a)} | {fnum(b)} ({(b / a - 1) * 100:+.0f}%) | {fnum(c)} ({(c / b - 1) * 100:+.0f}% so với cột 2; {(c / a - 1) * 100:+.0f}% so với cột 1) |')
tblRLS = "| Kịch bản (16 kết nối) | 1. Cơ sở (không RLS, không set_config) tps | 2. Có `set_config` + giao dịch bọc, RLS tắt | 3. RLS bật |\n|---|---|---|---|\n" + "\n".join(rls_rows)
rls_check = rls.split('### Kiểm cô lập khi RLS bật')[-1].split('### EXPLAIN')[0] if '### Kiểm cô lập' in rls else ''
rls_check = "\n".join(l for l in rls_check.splitlines() if l.strip() and 'set_config' not in l and not re.match(r'^[-+ ]+$', l) and not re.match(r'^\(\d+ row', l) and not re.match(r'^[0-9a-f-]{36}$', l.strip()))

# ---------- tách quyền
iso = read(f'{R}/isolation_test.txt').strip().splitlines()
n_pass = sum(1 for l in iso if l.startswith('PASS')); n_fail = sum(1 for l in iso if l.startswith('FAIL'))
iso_md = "\n".join(f'- {l}' for l in iso)

# ---------- truy vấn
qs = read(f'{R}/queries_summary.txt')
qrows = []
mx = 0.0
for l in qs.splitlines():
    p = l.split()
    if len(p) >= 5:
        mx = max(mx, float(p[1]))
        qrows.append(f'| {p[0].split("_")[0]} | {p[1]} | {p[2]} | {p[3]} | {p[4]} |')
tblQ = "| Truy vấn | Thực thi (ms) | Lập kế hoạch (ms) | Số lần \"Seq Scan\" trong kế hoạch | Trang bộ đệm |\n|---|---|---|---|---|\n" + "\n".join(qrows)

# ---------- kế hoạch tổng quát
gp = read(f'{R}/generic_plan_check.txt')
gprows = []
for sec in re.split(r'\n== ', '\n' + gp)[1:]:
    title = sec.split('\n')[0]
    ex = re.findall(r'Execution Time: ([\d.]+) ms', sec)
    nodes = []
    for m in re.finditer(r'(Index Only Scan|Index Scan|Bitmap Heap Scan|Seq Scan) (?:using |on )(\w+)', sec):
        n = f'{m.group(1)} {m.group(2)}'
        if n not in nodes and not re.search(r'_y\d{4}m\d{2}_|_default', n) and 'Seq Scan on event_log' not in n: nodes.append(n)
    gprows.append(f'| {title} | {ex[-1] if ex else "?"} | {"; ".join(nodes[:4])} |')
tblGP = "| Truy vấn (kế hoạch tổng quát ép bằng `plan_cache_mode = force_generic_plan`) | Thực thi (ms) | Nút quét chính |\n|---|---|---|\n" + "\n".join(gprows)

# ---------- kích thước
def block(path, n=999):
    t = read(path).strip().splitlines()
    return "\n".join(l for l in t[:n] if 'NOTICE' not in l)
evc = read(f'{R}/event_index_cost.txt')
evrows = re.findall(r'\n\s*([^\n|]+?)\s*\|\s*([\d.]+ \w+)\s*\|\s*([\d.]+ \w+)\s*\|\s*(\d+)\s*\|\s*(\d+)', evc)
evtimes = dict(re.findall(r'=== (\w)[^\n]*\nTime: ([\d.]+) ms', evc))
tblEV = "| Cấu hình (1.000.000 sự kiện, chèn theo thứ tự thời gian, khóa ngẫu nhiên theo học sinh) | Bảng | Chỉ mục | Số chỉ mục | Byte/hàng (bảng + chỉ mục) | Thời gian chèn (s) |\n|---|---|---|---|---|---|\n"
tmap = {'C': evtimes.get('C'), 'A': evtimes.get('A'), 'D': evtimes.get('D'), 'E': evtimes.get('E'), 'J': evtimes.get('J'), 'B': evtimes.get('B')}
for name, ts, isz, n, tot in evrows:
    key = name.strip()[0]
    tm = tmap.get(key)
    tblEV += f'| {name.strip()} | {ts} | {isz} | {n} | {tot} | {fnum(float(tm) / 1000, 1) if tm else "?"} |\n'

lint = read(f'{R}/lint.txt').strip(); lintst = read(f'{R}/lint_selftest.txt').strip().splitlines()
lint_self_head = lintst[0] if lintst else ''
sc_md = read(f'{BASE}/catalogue_scale.md')

# ---------- lượt sửa sau RV: pgbench rút gọn (run_pgbench_rv.sh), tách quyền khi RLS bật, kiểm RLS trên geo_arch2
rv = read(f'{R}/pgbench_rv.txt')
def rv_line(pattern, txt=rv):
    m = re.search(pattern + r'[^\n]*?latency average = ([\d.]+) ms tps = ([\d.]+)', txt)
    return (m.group(1), m.group(2)) if m else ('?', '?')
names_rv = dict(names, status='Thăm dò trạng thái khóa Q04b (ca đệm trượt)', login='Đăng nhập qua `fn_student_login`', open='Mở bài: Q04 + `fn_record_durable(open)` (bền)',
                event_batch='Lô 50 sự kiện qua `fn_record_batch`, commit không đồng bộ', hint='Gợi ý qua `fn_get_hint` (đọc + ghi bền)',
                critical='Xem lời giải: `fn_get_solution` + `fn_record_durable(unlock)` (commit đồng bộ)', gate_only='Chỉ cổng `fn_get_solution`',
                qa='Hỏi đáp: Q12 + `fn_record_qa`', tracking='Theo dõi lớp (Q09 theo tên)')
rvA = []
secA = rv.split('### B.')[0]
for k in ['login', 'list', 'open', 'event_batch', 'hint', 'critical', 'gate_only', 'qa', 'keyword', 'tracking', 'catalog', 'status']:
    lat, tps = rv_line(rf'\n{k}\s', secA)
    rvA.append(f'| {names_rv[k]} | {fnum(lat, 2)} | {fnum(tps)} |')
tblRVA = "| Kịch bản (16 kết nối, 10 giây) | Trung bình (ms) | tps |\n|---|---|---|\n" + "\n".join(rvA)
secB = rv.split('### B.')[-1].split('### C.')[0] if '### B.' in rv else ''
rvB = []
for c in [8, 16, 32, 64]:
    lat, tps = rv_line(rf'\nc={c}\s', '\n' + secB)
    rvB.append(f'| {c} | {fnum(tps)} | {fnum(lat, 2)} |')
tblRVB = "| Số kết nối | tps | Trung bình (ms) |\n|---|---|---|\n" + "\n".join(rvB)
secC = rv.split('### C.')[-1].split('### D.')[0] if '### C.' in rv else ''
rvC = []
for k, label in [('gate_only', 'Chỉ cổng `fn_get_solution` (đọc)'), ('critical', 'Giao dịch đầy đủ (cổng + `unlock` bền lũy đẳng)')]:
    for c in [20, 50, 100]:
        lat, tps = rv_line(rf'{k}\s+c={c}\s', secC)
        rvC.append(f'| {label} | {c} | {fnum(tps)} | {fnum(lat, 2)} |')
tblRVC = "| Kịch bản | Kết nối | tps | Trung bình (ms) |\n|---|---|---|---|\n" + "\n".join(rvC)
secD = rv.split('### D.')[-1] if '### D.' in rv else ''
rvdlate = g(r'above the 50\.0 ms latency limit: (\d+/\d+ \([\d.]+%\))', secD); rvdlat = g(r'latency average = ([\d.]+) ms', secD)
rvdstd = g(r'latency stddev = ([\d.]+) ms', secD); rvdtps = g(r'tps = ([\d.]+)', secD); rvdskip = g(r'transactions skipped: (\d+)', secD)
iso_rls = read(f'{R}/isolation_test_rls_on.txt').strip().splitlines()
n_pass_rls = sum(1 for l in iso_rls if l.startswith('PASS')); n_fail_rls = sum(1 for l in iso_rls if l.startswith('FAIL') or 'ERROR' in l)
rls_check2 = read(f'{R}/rls_check_geo_arch2.txt').strip()
seed_txt = read(f'{BASE}/results_seed.txt')
seed_tbl = "\n".join(l for l in seed_txt.splitlines() if re.match(r'^\s*\w+\s+\|\s+\d+\s*$', l))
rvsc = read(f'{R}/rv_scale_checks.txt')

tpl = read(f'{BASE}/verification_template.md')
vals = dict(tblA=tblA, tblB=tblB, tblC=tblC, dlate=dlate, dlat=fnum(dlat, 2), dstd=fnum(dstd, 2), dtps=fnum(dtps, 0), dskip=dskip, tblE=tblE, tblRLS=tblRLS, rls_check=rls_check,
            iso_md=iso_md, n_pass=str(n_pass), n_fail=str(n_fail), tblQ=tblQ, qmax=fnum(mx, 3), tblGP=tblGP, tblEV=tblEV,
            lint=lint, lint_self_head=lint_self_head, scale=sc_md,
            sizes_initial=block(f'{R}/sizes_initial.txt'), sizes_after=block(f'{R}/sizes_after_load.txt'),
            tblRVA=tblRVA, tblRVB=tblRVB, tblRVC=tblRVC, rvdlate=rvdlate, rvdlat=fnum(rvdlat, 2) if rvdlat != '?' else '?', rvdstd=fnum(rvdstd, 2) if rvdstd != '?' else '?',
            rvdtps=fnum(rvdtps, 1) if rvdtps != '?' else '?', rvdskip=rvdskip, n_pass_rls=str(n_pass_rls), n_fail_rls=str(n_fail_rls), rls_check2=rls_check2, seed_tbl=seed_tbl)
for k, v in vals.items():
    tpl = tpl.replace('{{' + k + '}}', v)
open(f'{ROOT}/07_verification.md', 'w', encoding='utf-8').write(tpl)
print('ok', len(tpl))
