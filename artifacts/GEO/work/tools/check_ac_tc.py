#!/usr/bin/env python3
"""QC: kiểm độc lập AC ↔ TC cho các epic của GEO (không dùng lại mã của gen_cases.py).

Kiểm:
  1. Mọi mã AC còn hiệu lực trong work/epics/E<n>/stories_ac.md xuất hiện ở ít nhất một TC (cột Remark của test_cases.md).
  2. Mọi TC có trường "Tự động hóa" với giá trị E2E-UI, E2E-API, IT hoặc Manual; Manual phải kèm lý do ("Manual — <lý do>").
  3. Mã TC không trùng trong toàn bộ các epic; mã AC mà TC trỏ tới phải có thật.
Dùng:
  python3 -I work/tools/check_ac_tc.py                       kiểm cả 7 epic, in lỗi, thoát 1 nếu có lỗi
  python3 -I work/tools/check_ac_tc.py E1                    chỉ kiểm các epic nêu
  python3 -I work/tools/check_ac_tc.py --trace work/traceability.md   kiểm rồi ghi bảng AC → TC
Chạy từ thư mục artifacts/GEO hoặc bất kỳ đâu (đường dẫn tính theo vị trí tệp này).
"""
import os, re, sys, collections, datetime

HERE = os.path.dirname(os.path.abspath(__file__))
WORK = os.path.normpath(os.path.join(HERE, '..'))
AUTO = ('E2E-UI', 'E2E-API', 'IT', 'Manual')
AC_RE = re.compile(r'^(~~)?\*\*(US-GEO-\d{3}-(?:SC|FC)\d+) — (.+?)\*\*')
REF_RE = re.compile(r'US-GEO-(\d{3})(?: · |-)((?:SC|FC)\d+)')


def split_row(line):
    return [x.strip() for x in re.split(r'(?<!\\)\|', line.strip()[1:-1])]


def read_acs(epic):
    acs = collections.OrderedDict()
    for line in open(os.path.join(WORK, 'epics', epic, 'stories_ac.md'), encoding='utf-8'):
        m = AC_RE.match(line)
        if m:
            acs[m.group(2)] = dict(title=m.group(3).strip(), removed=bool(m.group(1)))
    return acs


def read_tcs(epic):
    tcs = []
    hdr = None
    for ln, line in enumerate(open(os.path.join(WORK, 'epics', epic, 'test_cases.md'), encoding='utf-8'), 1):
        if line.startswith('| No |'):
            hdr = split_row(line)
            continue
        if not line.startswith('| TC-GEO-') or hdr is None or 'Remark' not in hdr:
            continue
        c = split_row(line)
        if len(c) != len(hdr):
            continue  # hàng của bảng khác (đối chiếu), không phải hàng case
        row = dict(zip(hdr, c))
        refs = sorted({f'US-GEO-{s}-{k}' for s, k in REF_RE.findall(row['Remark'])})
        tcs.append(dict(id=row['No'], title=row.get('Tiêu đề', ''), auto=row.get('Tự động hóa'), refs=refs,
                        other=sorted(set(re.findall(r'(?:NFR|BR|REQ|D)-GEO-\d+', row['Remark']))), line=ln, epic=epic))
    return tcs


def main(argv):
    trace = None
    if '--trace' in argv:
        i = argv.index('--trace'); trace = argv[i + 1]; argv = argv[:i] + argv[i + 2:]
    epics = [a for a in argv if re.fullmatch(r'E[1-7]', a)] or [f'E{n}' for n in range(1, 8)]
    errs, data, seen = [], collections.OrderedDict(), {}
    for e in epics:
        acs, tcs = read_acs(e), read_tcs(e)
        if not acs: errs.append(f'{e}: không đọc được AC nào trong stories_ac.md')
        if not tcs: errs.append(f'{e}: không đọc được TC nào trong test_cases.md')
        cover = {k: [] for k in acs}
        for t in tcs:
            if t['id'] in seen: errs.append(f"{e}: {t['id']} trùng với {seen[t['id']]}")
            seen[t['id']] = e
            a = t['auto']
            if a is None: errs.append(f"{e}: {t['id']} (dòng {t['line']}) không có cột Tự động hóa")
            elif a.split(' — ')[0] not in AUTO: errs.append(f"{e}: {t['id']} Tự động hóa = '{a[:30]}' không thuộc {', '.join(AUTO)}")
            elif a.startswith('Manual') and not a.startswith('Manual — ') or a.strip() in ('Manual', 'Manual —'):
                errs.append(f"{e}: {t['id']} Manual chưa ghi lý do")
            if not t['refs'] and not t['other']:
                errs.append(f"{e}: {t['id']} không trỏ về AC hay NFR / BR / REQ / D-GEO nào")
            for r in t['refs']:
                if r in cover: cover[r].append(t['id'])
                else: t.setdefault('xref', []).append(r)   # AC của epic khác: kiểm sau khi đọc đủ
        for k, a in acs.items():
            if not a['removed'] and not cover[k]: errs.append(f'{e}: AC {k} chưa có TC nào')
        data[e] = dict(acs=acs, tcs=tcs, cover=cover)
    all_ac = {k for d in data.values() for k in d['acs']}
    for d in data.values():
        for t in d['tcs']:
            for r in t.get('xref', []):
                if r not in all_ac and len(epics) == 7: errs.append(f"{t['epic']}: {t['id']} trỏ tới AC {r} không có")
                elif len(epics) < 7: print(f"INFO {t['epic']}: {t['id']} trỏ AC {r} ngoài các epic đang kiểm (không kiểm được khi chạy một phần)")
    n_ac = sum(sum(not a['removed'] for a in d['acs'].values()) for d in data.values())
    n_tc = sum(len(d['tcs']) for d in data.values())
    by_auto = collections.Counter(t['auto'].split(' — ')[0] for d in data.values() for t in d['tcs'] if t['auto'])
    if trace: write_trace(trace, data, n_ac, n_tc, by_auto)
    for x in errs: print('LỖI', x)
    print(f"check_ac_tc: epic {', '.join(epics)} · AC còn hiệu lực {n_ac} · TC {n_tc} · " + ' / '.join(f'{k} {by_auto.get(k, 0)}' for k in AUTO) + f' · lỗi {len(errs)}')
    return 1 if errs else 0


def write_trace(path, data, n_ac, n_tc, by_auto):
    L = ['# GEO — truy vết AC → TC (7 epic)', '',
         f'> Chủ sở hữu: QC. **Tệp sinh ra**: `python3 -I work/tools/check_ac_tc.py --trace work/traceability.md` (đọc `work/epics/E<n>/stories_ac.md` và `test_cases.md`). Cập nhật {datetime.date.today().strftime("%d-%m-%Y")}. Nguồn sự thật của case: `work/src/qc/` → `work/epics/E<n>/test_cases.md`.', '',
         '## 0. Reader guide and glossary', '',
         '| Id / abbreviation | Meaning | Where used |', '|---|---|---|',
         '| `US-GEO-nnn-SCn` / `FCn` | tiêu chí thành công / thất bại của story (BA, `stories_ac.md`) | cột AC |',
         '| `TC-GEO-nnnn` | test case (QC, `test_cases.md`); 0800…0899 là dải xuyên epic | cột TC |',
         '| E2E-UI · E2E-API · IT · Manual | cách tự động hóa chính: Playwright qua trình duyệt · gọi HTTP thẳng · kiểm thử tích hợp phía máy chủ · người chạy (có lý do) | cột Tự động hóa của case |', '',
         '## 1. Tổng', '', '| Epic | AC còn hiệu lực | AC có TC | TC | E2E-UI | E2E-API | IT | Manual |', '|---|---|---|---|---|---|---|---|']
    for e, d in data.items():
        act = [k for k, a in d['acs'].items() if not a['removed']]
        c = collections.Counter(t['auto'].split(' — ')[0] for t in d['tcs'] if t['auto'])
        L.append(f"| {e} | {len(act)} | {sum(1 for k in act if d['cover'][k])} | {len(d['tcs'])} | " + ' | '.join(str(c.get(k, 0)) for k in AUTO) + ' |')
    L.append(f"| **Tổng** | **{n_ac}** | **{sum(1 for d in data.values() for k, a in d['acs'].items() if not a['removed'] and d['cover'][k])}** | **{n_tc}** | " + ' | '.join(f'**{by_auto.get(k, 0)}**' for k in AUTO) + ' |')
    L += ['', '## 2. AC → TC', '']
    tc_auto = {t['id']: t['auto'].split(' — ')[0] for d in data.values() for t in d['tcs'] if t['auto']}
    for e, d in data.items():
        L += [f'### {e}', '', '| AC | Tiêu đề | Trạng thái | TC (Tự động hóa) |', '|---|---|---|---|']
        for k, a in d['acs'].items():
            tcs = ', '.join(f'{x} ({tc_auto.get(x, "?")})' for x in sorted(set(d['cover'][k]))) or '**THIẾU**'
            L.append(f"| {k} | {a['title'].replace('|', '/')} | {'Removed' if a['removed'] else 'Active'} | {tcs} |")
        xs = [t for t in d['tcs'] if not t['refs']]
        if xs:
            L += ['', f'Case không bám tiêu chí của {e} (loại X, trỏ NFR / BR / REQ / D):', '', '| TC | Trỏ về | Tự động hóa |', '|---|---|---|']
            L += [f"| {t['id']} | {', '.join(t['other']) or '(D-GEO)'} | {tc_auto.get(t['id'], '?')} |" for t in xs]
        L.append('')
    os.makedirs(os.path.dirname(os.path.abspath(path)), exist_ok=True)
    open(path, 'w', encoding='utf-8').write('\n'.join(L) + '\n')


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
