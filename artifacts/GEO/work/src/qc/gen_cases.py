#!/usr/bin/env python3
"""QC: sinh work/epics/E<n>/test_cases.md từ AC của BA + bảng phân loại của QC + case viết tay.

Nguồn (QC sửa ở đây, KHÔNG sửa tay tệp sinh ra):
  work/src/qc/E<n>_meta.txt   mỗi dòng: TC | AC | ưu tiên | loại | tầng | tài khoản | CL [| ghi chú]   (# là chú thích)
  work/src/qc/E<n>_extra.md   case viết tay (biên, bảng quyết định, NFR), dưới heading "## US-GEO-nnn" hoặc "## X"
  work/src/qc/E<n>_override.txt  chỉnh của QC trên case sinh ra khi AC chưa kịp đổi theo một D-GEO:
                              TC-GEO-nnnn | Cột | sub | cũ ⇒ mới | nguồn   (thay chuỗi; không còn khớp thì in INFO, không lỗi)
                              TC-GEO-nnnn | Cột | set | văn bản | nguồn     (đặt lại cả ô; dùng cho bước kỹ thuật của QC)
                              TC-GEO-nnnn | Cột | add | văn bản | nguồn     (thêm một gạch đầu dòng, hoặc '; văn bản' ở Remark)
  work/src/qc/E<n>_changelog.txt  mỗi dòng một hàng Change log; {tc} và {ac} thay bằng số hiện tại
Đầu vào chỉ đọc: work/epics/E<n>/stories_ac.md (BA), work/05_dictionary.md (BA).
Dùng:  python3 gen_cases.py E1            sinh tệp, in dòng tóm tắt; thoát 1 nếu độ phủ < 100% hoặc id sai
       python3 gen_cases.py E1 --init     in khung meta (TC id tuần tự, AC id, tiêu đề) để QC điền
"""
import re, sys, os, collections
HERE = os.path.dirname(os.path.abspath(__file__)); GEO = os.path.normpath(os.path.join(HERE, '..', '..', '..'))
EPIC_TITLE = {'E1': 'Nền tảng, cổng AI, bộ đề chuẩn, thử tải', 'E2': 'Hình đúng: dựng hình, hình mẫu, kiểm chứng, nhãn', 'E3': 'Thao tác hình 3D trên thiết bị cảm ứng',
              'E4': 'Gợi ý, lời giải đồng bộ hình, hỏi đáp, tự học', 'E5': 'Kho đề của Giáo viên mẫu', 'E6': 'Giao bài và khóa lời giải', 'E7': 'Theo dõi lớp và ghi sự kiện'}
PRI = {'H': 'High', 'M': 'Medium', 'L': 'Low'}
LOAI = {'CN': 'chức năng', 'PQ': 'phân quyền', 'DL': 'dữ liệu trả về', 'PCN': 'phi chức năng', 'TB': 'thiết bị'}
TANG = {'DV': 'đơn vị', 'API': 'tích hợp API', 'DB': 'tích hợp CSDL', 'E2E': 'E2E trình duyệt', 'TAI': 'tải', 'TC': 'thủ công trên thiết bị'}
COLS = ['No', 'Tiêu đề', 'Priority', 'Type', 'Loại', 'Tầng tự động hóa', 'User Type/Account Permission', 'Pre-condition', 'Test data/Input data', 'Test step', 'Expected output', 'Remark']
# Cột "Tự động hóa" (PO-50, Đợt 0): cách tự động hóa chính của case, đầu vào trực tiếp của bộ E2E Playwright.
# Mặc định suy từ "Tầng tự động hóa" và "Loại"; QC chỉnh từng case ở E<n>_auto.txt (TC-GEO-nnnn | E2E-UI/E2E-API/IT/Manual | lý do).
AUTO = ('E2E-UI', 'E2E-API', 'IT', 'Manual')
COLS_OUT = COLS[:6] + ['Tự động hóa'] + COLS[6:]
LY_DO_THIET_BI = 'cần thiết bị thật (bàn phím ảo, cảm ứng hoặc màn hình của máy thật); Playwright giả lập không thay được'
LY_DO_TAI = 'thử tải bằng Gatling (D-GEO-170) trên môi trường thử tải, chạy theo lịch riêng, không nằm trong bộ E2E / IT của mỗi bản dựng'

def auto_default(tang, loai):
    t = set(tang)
    if TANG['TAI'] in t: return 'Manual — ' + LY_DO_TAI
    if TANG['E2E'] in t or (TANG['TC'] in t and TANG['API'] in t): return 'E2E-UI'
    if TANG['TC'] in t: return ('Manual — ' + LY_DO_THIET_BI) if loai == LOAI['TB'] else 'E2E-UI'
    if TANG['DB'] in t or TANG['DV'] in t: return 'IT'
    if TANG['API'] in t: return 'E2E-API'
    return 'Manual — chưa phân loại'

def load_auto(epic, errs):
    """E<n>_auto.txt: TC-GEO-nnnn | E2E-UI / E2E-API / IT / Manual | lý do (bắt buộc với Manual; với loại khác là ghi chú)."""
    m = {}; p = os.path.join(HERE, f'{epic}_auto.txt')
    if not os.path.exists(p): return m
    for ln, line in enumerate(open(p, encoding='utf-8'), 1):
        if not line.strip() or line.lstrip().startswith('#'): continue
        q = [x.strip() for x in line.rstrip('\n').split(' | ')]
        if len(q) < 2 or q[1] not in AUTO: errs.append(f'auto dòng {ln}: cần "TC-GEO-nnnn | {" / ".join(AUTO)} | lý do"'); continue
        why = ' | '.join(q[2:]).strip()
        if q[1] == 'Manual' and not why: errs.append(f'auto dòng {ln}: Manual phải có lý do'); continue
        m[q[0]] = q[1] + (f' — {why}' if why and q[1] == 'Manual' else '')
    return m
GLOSS = collections.OrderedDict([
    ('TC-GEO-', ('`TC-GEO-nnnn`', 'test case; hàng trăm là epic, 0800…0899 là case xuyên epic', 'cột No')),
    ('US-GEO-', ('`US-GEO-nnn` · `SCn` / `FCn`', 'story của BA · tiêu chí thành công / thất bại của story đó', 'cột Remark; `stories_ac.md` cùng thư mục')),
    ('F-GEO-', ('`F-GEO-nnn`', 'hàng dữ liệu mẫu: 001…199 ở `work/fixture.md` (BA), 200…299 ở `review/QC_test_design.md` §6 (QC), 300…399 dữ liệu thử tải (ARCH)', 'cột Test data')),
    ('MSG-GEO-', ('`MSG-GEO-nn`', 'mã thông báo; câu chữ nguyên văn in ngay trước mã, lấy từ `work/05_dictionary.md`', 'cột Expected output')),
    ('NFR-GEO-', ('`NFR-GEO-nnn`', 'yêu cầu phi chức năng của ARCH (`work/architecture/06_nfr.md`); ngưỡng là đề xuất', 'cột Remark')),
    ('D-GEO-', ('`D-GEO-nnn`', 'quyết định hoặc giả định của PO (`work/03_decisions.md`)', 'cột Remark, sau chữ PROVISIONAL')),
    ('QA-GEO-', ('`QA-GEO-nn`', 'hỏi đáp còn mở của BA / QC / ARCH (`questions.md` cùng thư mục)', 'cột Remark')),
    ('M-GEO-', ('`M-GEO-nn`', 'công thức chỉ số, một công thức mỗi con số (`work/02_domain_model.md` §3)', 'cột Expected output')),
    ('BR-GEO-', ('`BR-GEO-nn`', 'quy tắc nghiệp vụ (`work/02_gate2_brd.md`; 100…199 do BA làm rõ)', 'cột Remark')),
    ('REQ-GEO-', ('`REQ-GEO-nn`', 'yêu cầu thô (`work/00_gate0_intake_voc.md`)', 'cột Remark')),
    ('ADR-GEO-', ('`ADR-GEO-nn`', 'quyết định kiến trúc (`work/architecture/01_adr.md`)', 'cột Remark')),
    ('CL-FN-', ('`CL-FN-nn`', 'nhóm kiểm tra chức năng (`review/QC_test_design.md` §1)', 'cột Remark')),
    ('CL-UI-', ('`CL-UI-nn`', 'nhóm kiểm tra giao diện và thiết bị', 'cột Remark')),
    ('CL-X-', ('`CL-X-nn`', 'nhóm kiểm tra xuyên suốt: tải, kế hoạch truy vấn, bộ quét SQL, dữ liệu trẻ em', 'cột Remark')),
    ('HKG-', ('`HKG-nn`', 'mã bài của Kho đề', 'dữ liệu')),
    ('KS-', ('`KS-n`', 'quy tắc Kiểm soát đáp án theo D-GEO-156 (`review/QC_test_design.md` §6; chi tiết ở ADR-GEO-14 khi ARCH công bố)', 'cột Remark')),
    ('BG-', ('`BG-n`, `BG-C`', 'bài giao mẫu của `work/fixture.md` mục 3', 'dữ liệu')),
    ('BV-', ('`BV-n`', 'ca biên giờ mở (`review/QC_test_design.md` §4)', 'cột Remark')),
    ('DT-', ('`DT-n`', 'dòng bảng quyết định khóa lời giải × vai × trạng thái (`review/QC_test_design.md` §3)', 'cột Remark')),
    ('KG-', ('`KG-nn`', 'known gap (`work/known_gaps.md`)', 'cột Remark')),
    ('Q-CDT-', ('`Q-CDT-nn`', 'câu hỏi cho chủ đầu tư', 'cột Remark')), ('Q-GV-', ('`Q-GV-nn`', 'câu hỏi cho giáo viên cố vấn', 'cột Remark')), ('Q-TL-', ('`Q-TL-nn`', 'câu hỏi cho TechLead', 'cột Remark')),
    ('PO-', ('`PO-nn`', 'quyết định trong sổ quyết định của chủ sản phẩm', 'cột Remark')),
    ('SRC-', ('`SRC-nnn`', 'dòng nguồn (`work/00_sources.md`)', 'cột Remark')),
    ('RV-GEO-', ('`RV-GEO-nn`', 'phát hiện của phản biện RV (`review/RV_findings.md`)', 'cột Remark')),
    ('M-', ('`M-nnn`', 'tin nhắn trên bảng chung `board/BOARD.md`', 'cột Remark')),
    ('L', None), ])
FAM_RE = re.compile(r'\b([A-Z]{1,4}(?:-[A-Z]{2,5})?[-_])(?=\d)')

def split_top(text, seps=('; ',)):
    """tách theo dấu phân cách ở mức ngoài cùng (không tách trong ngoặc đơn hay ngoặc kép)."""
    out, cur, depth, q, i = [], '', 0, False, 0
    while i < len(text):
        ch = text[i]
        if ch == '"': q = not q
        elif ch == '(' and not q: depth += 1
        elif ch == ')' and not q and depth: depth -= 1
        hit = next((s for s in seps if depth == 0 and not q and text.startswith(s, i)), None)
        if hit: out.append(cur.strip()); cur = ''; i += len(hit); continue
        cur += ch; i += 1
    if cur.strip(): out.append(cur.strip())
    return [o for o in out if o]

def cell(s): return s.replace('|', '\\|').replace('\n', ' ').strip()
def cap(x): return x[:1].upper() + x[1:]
def bullets(text): return '<br/>'.join('• ' + cap(cell(x.rstrip('.'))) for x in split_top(text)) or '—'
def steps(text):
    parts = []
    for p in split_top(text, ('; ', ' rồi ', ', sau đó ')): parts.append(p[0].upper() + p[1:] if p else p)
    return '<br/>'.join(f'{n}. {cell(p.rstrip("."))}' for n, p in enumerate(parts, 1))

def parse_stories(path):
    stories, acs, cur_story, cur = collections.OrderedDict(), collections.OrderedDict(), None, None
    field = None
    for line in open(path, encoding='utf-8').read().split('\n'):
        m = re.match(r'^## (US-GEO-\d{3}) — (.+)$', line)
        if m: cur_story = m.group(1); stories[cur_story] = m.group(2).strip(); cur = None; continue
        m = re.match(r'^(~~)?\*\*(US-GEO-(\d{3})-((?:SC|FC)\d+)) — (.+?)\*\*(.*)$', line)
        if m:
            rest = m.group(6); prov = re.search(r'PROVISIONAL\s+([^·~]+)', rest)
            cur = dict(id=m.group(2), story='US-GEO-' + m.group(3), key=f'{m.group(3)}-{m.group(4)}', kind=m.group(4)[:2], title=m.group(5).strip(),
                       prov=prov.group(1).strip() if prov else '', removed=bool(m.group(1)), src=rest, Given='', When='', Then='')
            acs[cur['key']] = cur; field = None; continue
        if cur is None: continue
        m = re.match(r'^- (Given|When|Then):\s*(.*)$', line)
        if m: field = m.group(1); cur[field] = m.group(2).strip(); continue
        if field and line.startswith('  ') and line.strip(): cur[field] += '; ' + line.strip().lstrip('-•').strip(); continue
        if not line.strip() or line.startswith('- **') or line.startswith('#'): field = None
    return stories, acs

def load_msgs():
    msgs = {}
    p = os.path.join(GEO, 'work', '05_dictionary.md')
    if os.path.exists(p):
        for line in open(p, encoding='utf-8'):
            m = re.match(r'^\| (MSG-GEO-\d+) \|[^|]*\| ("[^|]*?") \|', line)
            if m: msgs[m.group(1)] = m.group(2)
    return msgs

def main():
    epic = sys.argv[1]; n = epic[1]; lo, hi = int(n) * 100, int(n) * 100 + 99
    edir = os.path.join(GEO, 'work', 'epics', epic); stories, acs = parse_stories(os.path.join(edir, 'stories_ac.md'))
    if '--init' in sys.argv:
        for i, a in enumerate(acs.values()): print(f"{lo + i:04d} | {a['key']} | M | CN | API | ? | FN-01   # {a['title']}{' [PROV ' + a['prov'] + ']' if a['prov'] else ''}")
        return 0
    msgs = load_msgs(); errs = []
    def inline_msg(t): return re.sub(r'MSG-GEO-\d+', lambda m: f'{msgs[m.group(0)]} ({m.group(0)})' if m.group(0) in msgs else m.group(0), t)
    rows_by_story = collections.OrderedDict((s, []) for s in stories); rows_by_story['X'] = []
    cases = []   # dict(id, refs(set of AC keys), nfr(set), pri, loai, tang, prov, story)
    for ln, line in enumerate(open(os.path.join(HERE, f'{epic}_meta.txt'), encoding='utf-8'), 1):
        line = line.split('#')[0].strip()
        if not line: continue
        p = [x.strip() for x in line.split('|')]
        if len(p) < 7: errs.append(f'meta dòng {ln}: thiếu cột'); continue
        tc, key, pri, loai, tang, acct, cl = p[:7]; note = p[7] if len(p) > 7 else ''
        a = acs.get(key)
        if not a: errs.append(f'meta dòng {ln}: AC {key} không có trong stories_ac.md'); continue
        tangs = [TANG[t] for t in tang.split('+')]; eng = 'TC' not in tang.split('+')
        remark = f"{a['story']} · {key.split('-')[1]}; CL-{cl}" + (f"; PROVISIONAL {a['prov']}" if a['prov'] else '') + ('; ENG' if eng else '') + (f'; {note}' if note else '')
        blob = ' '.join((a['Given'], a['When'], a['Then']))
        fids = list(collections.OrderedDict.fromkeys(re.findall(r'F-GEO-\d{3}(?:…\d{3})?', blob)))
        lits = list(collections.OrderedDict.fromkeys(re.findall(r'`[^`]+`', a['When']) + re.findall(r'(?:gõ|nhập|dán|hỏi|tên)[^"`;]{0,25}("[^"]+")', a['When'])))
        data = '<br/>'.join(([f"• Hàng dữ liệu: {', '.join(fids)}"] if fids else []) + [f'• Giá trị nhập: {cell(x)}' for x in lits[:6]]) or '• Không có dữ liệu nhập riêng'
        typ = 'Normal (Success case)' if a['kind'] == 'SC' else 'Abnormal (Fail case/Edge case)'
        status_pre = 'N/A — tiêu chí đã bị BA gạch bỏ<br/>' if a['removed'] else ''
        row = [f'TC-GEO-{tc}', cell(a['title']), PRI[pri], typ, LOAI[loai], ' + '.join(tangs), cell(acct), status_pre + bullets(a['Given']), data, steps(a['When']), bullets(inline_msg(a['Then'])), remark]
        rows_by_story[a['story']].append(row)
        cases.append(dict(id=row[0], refs={key}, nfr=set(re.findall(r'NFR-GEO-\d{3}', remark + a['src'])), pri=PRI[pri], loai=LOAI[loai], tang=tangs, prov='PROVISIONAL' in remark, story=a['story'], extra=False))
    xp = os.path.join(HERE, f'{epic}_extra.md'); sec = 'X'
    if os.path.exists(xp):
        for ln, line in enumerate(open(xp, encoding='utf-8'), 1):
            m = re.match(r'^## (US-GEO-\d{3}|X)\b', line)
            if m: sec = m.group(1); continue
            if not line.startswith('| TC-GEO-'): continue
            c = [x.strip() for x in re.split(r'(?<!\\)\|', line.strip()[1:-1])]
            if len(c) != len(COLS): errs.append(f'extra dòng {ln}: {len(c)} cột, cần {len(COLS)}'); continue
            if sec not in rows_by_story: errs.append(f'extra dòng {ln}: story {sec} không có'); continue
            refs = {f'{s}-{k}' for s, k in re.findall(r'US-GEO-(\d{3}) · ((?:SC|FC)\d+)', c[-1])}
            for r in refs:
                if r not in acs: errs.append(f'extra dòng {ln}: AC {r} không có trong stories_ac.md')
            nfr = set(re.findall(r'NFR-GEO-\d{3}', c[-1]))
            if not refs and not nfr and not re.search(r'(BR|REQ|D)-GEO-\d+', c[-1]): errs.append(f'extra dòng {ln}: {c[0]} không trỏ về AC, NFR, BR, REQ hay D')
            rows_by_story[sec].append(c)
            cases.append(dict(id=c[0], refs=refs, nfr=nfr, pri=c[2], loai=c[4], tang=[t.strip() for t in c[5].split('+')], prov='PROVISIONAL' in c[-1], story=sec, extra=True))
    # ---------- chỉnh của QC (override)
    rowmap = {r[0]: r for rs in rows_by_story.values() for r in rs}
    op = os.path.join(HERE, f'{epic}_override.txt'); n_ovr = 0; stale = []
    if os.path.exists(op):
        for ln, line in enumerate(open(op, encoding='utf-8'), 1):
            if not line.strip() or line.lstrip().startswith('#'): continue
            p = [x.strip() for x in line.rstrip('\n').split(' | ')]
            if len(p) < 5: errs.append(f'override dòng {ln}: cần 5 cột'); continue
            tc, col, mode, text, src = p[0], p[1], p[2], ' | '.join(p[3:-1]), p[-1]
            r = rowmap.get(tc)
            if r is None: errs.append(f'override dòng {ln}: {tc} không có'); continue
            if col not in COLS: errs.append(f'override dòng {ln}: cột {col} không có'); continue
            i = COLS.index(col); text = text.replace('|', '\\|')
            if mode == 'sub':
                old, new = [x.strip() for x in text.split('⇒', 1)]
                if old in r[i]: r[i] = r[i].replace(old, new); n_ovr += 1
                else: stale.append(f'{tc} {col}: "{old[:40]}"')
            elif mode == 'set': r[i] = text; n_ovr += 1
            elif mode == 'add':
                r[i] = r[i] + ('; ' + text if col == 'Remark' else ('<br/>• ' + cap(text) if r[i] not in ('—', '') else '• ' + cap(text))); n_ovr += 1
            else: errs.append(f'override dòng {ln}: kiểu {mode} không có'); continue
            if src and src not in r[-1] and col != 'Remark': r[-1] += f'; chỉnh theo {src}'
    for c in cases:
        r = rowmap[c['id']]; c['pri'] = r[2]; c['loai'] = r[4]; c['tang'] = [t.strip() for t in r[5].split('+')]; c['prov'] = 'PROVISIONAL' in r[-1]
        c['nfr'] |= set(re.findall(r'NFR-GEO-\d{3}', r[-1]))
    # ---------- cột Tự động hóa (PO-50)
    amap = load_auto(epic, errs)
    for k in amap:
        if k not in rowmap: errs.append(f'auto: {k} không có')
    for c in cases:
        r = rowmap[c['id']]; c['auto'] = amap.get(c['id']) or auto_default(c['tang'], c['loai'])
        r.insert(6, cell(c['auto']))
    ids = [c['id'] for c in cases]
    for d in sorted({i for i in ids if ids.count(i) > 1}): errs.append(f'trùng id {d}')
    for i in ids:
        v = int(i.split('-')[-1])
        if not (lo <= v <= hi or 800 <= v <= 899): errs.append(f'{i} ngoài dải của {epic} ({lo:04d}…{hi:04d}) và dải xuyên epic 0800…0899')
    active = {k: a for k, a in acs.items() if not a['removed']}
    cover = {k: sorted(c['id'] for c in cases if k in c['refs']) for k in acs}
    missing = [k for k in active if not cover[k]]
    for k in missing: errs.append(f'AC US-GEO-{k} chưa có case')
    notrace = [c['id'] for c in cases if not c['refs']]
    orphan = [c['id'] for c in cases if not c['refs'] and not c['nfr']]
    for i in orphan: errs.append(f'{i} không trỏ về tiêu chí hay NFR-GEO nào')
    # ---------- viết tệp
    L = [f'# {epic} — test case ({EPIC_TITLE[epic]})', '',
         f'> Chủ sở hữu: QC. **Tệp sinh ra, không sửa tay**: sửa `work/src/qc/{epic}_meta.txt` (phân loại từng tiêu chí), `work/src/qc/{epic}_extra.md` (case viết tay) hoặc `work/src/qc/{epic}_override.txt` (chỉnh của QC theo một D-GEO khi AC chưa kịp đổi; mỗi chỉnh ghi nguồn ở Remark) rồi chạy `python3 work/src/qc/gen_cases.py {epic}`. Tiền điều kiện, bước và kỳ vọng của case bám tiêu chí lấy nguyên từ Given / When / Then của `stories_ac.md` (BA); giá trị cụ thể là của `work/fixture.md` và công thức M-GEO. Id ổn định; tiêu chí bị gạch bỏ thì case giữ id với N/A. AI ở MVP là giả lập (D-GEO-047): không có kỳ vọng về chất lượng AI thật.', '']
    body = []
    body += ['## 2. Test case', '', '### Cách đọc một test case',
             '- Mỗi dòng là một case. Chuẩn bị theo **Pre-condition**, dùng giá trị ở **Test data**, làm lần lượt các bước đánh số ở **Test step**, rồi so với **Expected output**.',
             '- **Expected output**: mỗi gạch đầu dòng là một điều phải đúng; chữ trong ngoặc kép và giá trị phải khớp nguyên văn. Case đạt khi mọi gạch đầu dòng đều đúng.',
             '- **Loại**: chức năng · phân quyền · dữ liệu trả về (soát thân phản hồi của máy chủ, không chỉ giao diện) · phi chức năng · thiết bị.',
             '- **Tầng tự động hóa**: đơn vị · tích hợp API · tích hợp CSDL · E2E trình duyệt (Playwright) · tải (Gatling) · thủ công trên thiết bị; đếm câu lệnh JDBC dùng datasource-proxy (D-GEO-170). Case có chữ ENG ở Remark cần công cụ kỹ thuật và thuộc tờ kỹ thuật; case còn lại người kiểm thử chạy trên màn.',
             '- **Tự động hóa** (PO-50): cách tự động hóa chính của case, đầu vào của bộ E2E. `E2E-UI` · Playwright qua trình duyệt, theo đúng bước trên màn và kỳ vọng nhìn thấy (phần "máy chủ trả …" của kỳ vọng thì soát thêm qua phản hồi mạng mà trang nhận) · `E2E-API` · gọi HTTP thẳng tới máy chủ, soát `code` và thân phản hồi · `IT` · kiểm thử tích hợp phía máy chủ (CSDL, bộ quét, tầng dịch vụ) · `Manual` · người chạy, lý do ghi ngay sau.',
             '- **PROVISIONAL**: kỳ vọng theo một giả định chưa được xác nhận, mã ghi ngay sau. Vẫn chạy; nếu màn chỉ khác ở đúng điểm đó thì ghi lại điều thấy và đánh dấu Blocked, không đánh Fail.',
             '- Thực tế khác kỳ vọng thì ghi vào vòng thực thi, không sửa ô Expected.', '']
    hdr = '| ' + ' | '.join(COLS_OUT) + ' |'; sep = '|' + '---|' * len(COLS_OUT)
    for s, rows in rows_by_story.items():
        if not rows: continue
        body += [f'### {s} — {stories[s]}' if s != 'X' else '### X — Case xuyên suốt và phi chức năng (trỏ về NFR-GEO, BR-GEO hoặc REQ-GEO)', '', hdr, sep]
        body += ['| ' + ' | '.join(r) + ' |' for r in sorted(rows, key=lambda r: r[0])] + ['']
    cnt = collections.Counter
    by_pri, by_loai = cnt(c['pri'] for c in cases), cnt(c['loai'] for c in cases); by_tang = cnt(t for c in cases for t in c['tang']); by_auto = cnt(c['auto'].split(' — ')[0] for c in cases)
    nprov = sum(c['prov'] for c in cases); fwd = 100.0 * (len(active) - len(missing)) / max(1, len(active)); rev = 100.0 * (len(cases) - len(notrace)) / max(1, len(cases)); rev2 = 100.0 * (len(cases) - len(orphan)) / max(1, len(cases))
    summ = ['## 1. Tóm tắt', '', '| Story | Số tiêu chí (SC / FC) | Số case | PROVISIONAL |', '|---|---|---|---|']
    for s in stories:
        k = [a for a in acs.values() if a['story'] == s]; cs = [c for c in cases if c['story'] == s]
        summ.append(f"| {s} {stories[s]} | {len(k)} ({sum(a['kind'] == 'SC' for a in k)} / {sum(a['kind'] == 'FC' for a in k)}) | {len(cs)} | {sum(c['prov'] for c in cs)} |")
    xs = [c for c in cases if c['story'] == 'X']
    if xs: summ.append(f"| X (xuyên suốt, phi chức năng) | — | {len(xs)} | {sum(c['prov'] for c in xs)} |")
    summ += [f"| **Tổng** | **{len(acs)}** | **{len(cases)}** | **{nprov}** |", '',
             '| Theo ưu tiên | Số case |', '|---|---|'] + [f'| {p} | {by_pri.get(p, 0)} |' for p in ('High', 'Medium', 'Low')] + ['',
             '| Theo loại | Số case |', '|---|---|'] + [f'| {v} | {by_loai.get(v, 0)} |' for v in LOAI.values()] + ['',
             '| Theo tầng tự động hóa (một case có thể ở hai tầng) | Số case |', '|---|---|'] + [f'| {v} | {by_tang.get(v, 0)} |' for v in TANG.values()] + ['',
             '| Theo Tự động hóa (một case một loại) | Số case |', '|---|---|'] + [f'| {v} | {by_auto.get(v, 0)} |' for v in AUTO] + [f'| **Tổng** | **{len(cases)}** |', '']
    trace = ['## 3. Đối chiếu hai chiều AC ↔ TC', '', f'### 3.1 Chiều xuôi: tiêu chí → case ({len(active) - len(missing)} / {len(active)} tiêu chí còn hiệu lực có case, {fwd:.1f}%)', '',
             '| User story | Criterion ID | Kind | What it checks | Status | Test cases |', '|---|---|---|---|---|---|']
    for k, a in acs.items():
        trace.append(f"| {a['story']} | {a['id']} | {'Success' if a['kind'] == 'SC' else 'Failure'} | {cell(a['title'])} | {'Removed' if a['removed'] else 'Active'}{' · PROVISIONAL' if a['prov'] else ''} | {', '.join(cover[k]) or '**THIẾU**'} |")
    trace += ['', f'### 3.2 Chiều ngược: case → tiêu chí hoặc NFR ({len(cases) - len(orphan)} / {len(cases)} case trỏ về một tiêu chí của BA hoặc một NFR-GEO, {rev2:.1f}%; riêng trỏ về tiêu chí của BA: {len(cases) - len(notrace)} / {len(cases)}, {rev:.1f}%)', '',
              'Kiểm bằng `gen_cases.py`: mỗi case bám tiêu chí lấy đúng một tiêu chí từ bảng phân loại; case viết tay ghi tiêu chí ở Remark và bộ sinh từ chối id tiêu chí không tồn tại.', '']
    if notrace:
        trace += ['Case không trỏ về tiêu chí nào của BA (loại X: chỉ trỏ về NFR-GEO, BR-GEO, REQ-GEO hoặc D-GEO; đã gửi BA xem có cần tiêu chí không, xem `qc_findings.md`):', '', '| Case | Trỏ về |', '|---|---|']
        for c in cases:
            if not c['refs']:
                rk = next(r[-1] for rs in rows_by_story.values() for r in rs if r[0] == c['id'])
                trace.append(f"| {c['id']} | {', '.join(sorted(set(re.findall(r'(?:NFR|BR|REQ|D)-GEO-[0-9]+', rk))))} |")
        trace.append('')
    nfr_all = sorted({x for c in cases for x in c['nfr']})
    trace += ['### 3.3 NFR-GEO được case trong tệp này nhắc tới', '', (', '.join(nfr_all) or '(không có)'), '', '### 3.4 Khoảng trống', '',
              ('- Tiêu chí chưa có case: ' + ', '.join('US-GEO-' + k for k in missing)) if missing else '- Không tiêu chí nào thiếu case.',
              '- Tiêu chí không kiểm được như đang viết, mâu thuẫn, thiếu giá trị: xem `qc_findings.md` cùng thư mục.', '- Ca hiếm đã hoãn: `work/backlog_cases.md`.', '']
    text_wo_gloss = '\n'.join(L + summ + body + trace)
    fams = sorted(set(FAM_RE.findall(text_wo_gloss)))
    gl = ['## 0. Reader guide and glossary', '', '| Id / abbreviation | Meaning | Where used |', '|---|---|---|']
    for f in GLOSS:
        if GLOSS[f] and (f in fams or f in ('TC-GEO-', 'US-GEO-', 'F-GEO-')): gl.append('| ' + ' | '.join(GLOSS[f]) + ' |')
    gl += ['| PROVISIONAL · ENG · X · N/A | kỳ vọng theo giả định chưa xác nhận · case cần công cụ kỹ thuật · case xuyên suốt không bám một tiêu chí · case giữ id nhưng không chạy | cột Remark |',
           '| R · t · T0 | giờ mở lời giải (`release_at`) · giờ máy chủ · giờ máy chủ lúc bắt đầu ca kiểm | ca giờ mở |',
           '| Chưa đặt giờ · Sắp mở · Đã mở | ba trạng thái giờ mở: chưa có giờ (khóa) · t < R (khóa) · t ≥ R (được xem) | kỳ vọng |',
           '| Đã kiểm chứng · Cần xem lại · AI soạn | nhãn máy do engine tính · nhãn máy · nhãn người | kỳ vọng |', '']
    known = set(GLOSS) | {'UTF-', 'HTTP-', 'ISO-', 'ID-', 'X-', 'FN-', 'UI-'}
    for f in fams:
        if f not in known: errs.append(f"họ mã '{f}nn' chưa có trong bảng giải thích (thêm vào GLOSS)")
    tail = ['## 4. Dữ liệu và môi trường', '', '- Dữ liệu: `work/fixture.md` (BA), `review/QC_test_design.md` §6 (QC), `work/architecture/bench/` (ARCH, thử tải).',
            '- Môi trường, người thực thi, công cụ, known gaps, quyết định đang dùng: `review/test_plan.md` §3, §5, §6, §7.', '',
            '## 5. Change log', '', '| No | Thay đổi | Bởi |', '|---|---|---|']
    cp = os.path.join(HERE, f'{epic}_changelog.txt')
    clines = [l.strip() for l in open(cp, encoding='utf-8') if l.strip() and not l.startswith('#')] if os.path.exists(cp) else [f'bản đầu của goal run: {{tc}} case cho {{ac}} tiêu chí']
    tail += [f'| {i} | {cell(t.replace("{tc}", str(len(cases))).replace("{ac}", str(len(acs))))} | QC |' for i, t in enumerate(clines, 1)] + ['']
    open(os.path.join(edir, 'test_cases.md'), 'w', encoding='utf-8').write('\n'.join(L + gl + summ + body + trace + tail))
    for e in errs: print('ERR', e)
    for x in stale: print('INFO override không còn khớp (AC đã đổi? gỡ dòng override):', x)
    print(f"{epic}: AC {len(acs)} (active {len(active)}) · TC {len(cases)} (bám tiêu chí {sum(not c['extra'] for c in cases)}, viết tay {sum(c['extra'] for c in cases)}) · "
          f"High {by_pri.get('High', 0)} / Medium {by_pri.get('Medium', 0)} / Low {by_pri.get('Low', 0)} · " + ' / '.join(f'{v} {by_loai.get(v, 0)}' for v in LOAI.values()) +
          ' · Tự động hóa ' + ' / '.join(f'{v} {by_auto.get(v, 0)}' for v in AUTO) +
          f" · PROVISIONAL {nprov} · override {n_ovr} (không khớp {len(stale)}) · AC→TC {fwd:.1f}% · TC→AC {rev:.1f}% · TC→AC hoặc NFR {rev2:.1f}% · lỗi {len(errs)}")
    return 1 if errs else 0
if __name__ == '__main__': sys.exit(main())
