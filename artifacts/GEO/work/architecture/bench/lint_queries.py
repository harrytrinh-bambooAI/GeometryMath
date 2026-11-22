#!/usr/bin/env python3
"""Kiểm tự động "mẫu bị cấm" (04_query_catalogue.md) trên các truy vấn của catalogue và trên kế hoạch thực thi đã ghi.
Đây là bản mẫu của luật kiểm thử kiến trúc: ở sản phẩm, cùng các luật chạy trên SQL tĩnh trong mã Java (ArchUnit + bộ kiểm SQL) và
trên kế hoạch lấy từ cơ sở dữ liệu thử. Ngoại lệ ghi tại chỗ bằng dòng chú thích "-- lint-allow: R-Qnn (lý do)".
Dùng:  python3 bench/lint_queries.py   (cần psql tới $PGDATABASE (mặc định geo_arch2) để lấy số hàng ước lượng của bảng)
"""
import glob, os, re, subprocess, sys
BASE = os.path.dirname(os.path.abspath(__file__))
PG = os.environ.get('PGBIN', '/opt/homebrew/opt/postgresql@17/bin')
TENANT = ['app_user','class','class_member','problem','problem_version','scene_build','hint','solution','assignment',
          'assignment_progress','event_log','qa_turn','lecture','keyword','audit_log','share_grant']

def rows_est():
    out = subprocess.run([f'{PG}/psql','-h','127.0.0.1','-p','54329','-U','geo','-d',os.environ.get('PGDATABASE','geo_arch2'),'-Atc',
        "SELECT c.relname, greatest(c.reltuples,0)::bigint FROM pg_class c WHERE c.relnamespace='geo'::regnamespace AND c.relkind IN ('r','p')"],
        capture_output=True, text=True).stdout
    return {l.split('|')[0]: int(l.split('|')[1]) for l in out.splitlines() if '|' in l}

def strip(sql):
    sql = re.sub(r'--[^\n]*', '', sql)
    return re.sub(r'\s+', ' ', sql).strip()

fails, passes = [], 0
est = rows_est()
QDIR = os.environ.get('QDIR', f'{BASE}/queries')
for f in sorted(glob.glob(f'{QDIR}/Q*.sql')):
    name = os.path.basename(f)[:-4]
    raw = open(f).read()
    allow = set(re.findall(r'lint-allow:\s*(R-Q\d+)', raw))
    sql = strip(raw).replace(':explain', '').strip()
    low = sql.lower()
    def chk(rule, bad, msg):
        global passes
        if bad and rule not in allow: fails.append(f'{name}: {rule} {msg}')
        else: passes += 1
    is_select = low.startswith('with') or low.startswith('select')
    is_insert = low.startswith('insert')
    chk('R-Q01', re.search(r'\boffset\b', low) is not None, 'OFFSET')
    chk('R-Q03', re.search(r'select\s+\*|returning\s+\*|\.\*\s*(,|from)', low) is not None, 'SELECT * / RETURNING *')
    chk('R-Q04', is_select and 'limit' not in low and 'group by' not in low and 'for update' not in low, 'danh sách không LIMIT')
    chk('R-Q05', re.search(r'count\s*\(\s*\*\s*\)', low) is not None, 'count(*) (chỉ cho phép khi có lint-allow nêu phạm vi nhỏ)')
    chk('R-Q07', re.search(r"like\s+'%|ilike", low) is not None, "LIKE '%x' / ILIKE không có chỉ mục trigram")
    chk('R-Q08', re.search(r'(where|and)\s+(lower|upper|date|date_trunc|cast|unaccent)\s*\(\s*\w+\.?\w*\s*\)', low) is not None
                 or re.search(r"(:'?\w+'?|\$\d+|\?)(::\w+)?\s+is null\s+or\b", low) is not None, 'hàm trên cột đã đánh chỉ mục / mẫu "(tham số IS NULL OR cột = tham số)"')
    chk('R-Q09', 'event_log' in low and 'occurred_at' not in low, 'truy vấn event_log không có cận occurred_at')
    uses_t = [t for t in TENANT if re.search(rf'\bgeo\.{t}\b', low)]
    chk('R-Q10', bool(uses_t) and not re.search(r'org_id\s*(=|in\b)', low) and not (is_insert and 'org_id' in low), f'bảng của trường {uses_t} không có điều kiện org_id')
    chk('R-Q12', re.search(r"(where|and|order by|on)\s[^;]*?(->>?|@>|jsonb_path)", low) is not None, 'JSON trong WHERE/ORDER BY/ON')
    chk('R-Q14', re.search(r'\bgeo\.(solution|verification_report)\b', low) is not None, 'đọc bảng nhạy cảm ngoài hàm cổng')
    chk('R-Q17', '||' in low and ':' in low and re.search(r"'\s*\|\|\s*:", low) is not None, 'ghép chuỗi tham số vào SQL')

# kiểm kế hoạch (chỉ khi chạy mặc định)
for f in sorted(glob.glob(f'{BASE}/results/Q*.txt')) if 'QDIR' not in os.environ else []:
    name = os.path.basename(f)[:-4]
    txt = open(f).read()
    allow = set(re.findall(r'lint-allow:\s*(R-Q\d+)', open(f'{BASE}/queries/{name}.sql').read()))
    for m in re.finditer(r'Seq Scan on (\w+)', txt):
        t = m.group(1)
        base = re.sub(r'_(y\d{4}m\d{2}|default)$', '', t)
        big = est.get(t, 0) > 10000 or est.get(base, 0) > 10000
        part_empty = re.search(r'_(y\d{4}m\d{2}|default)$', t) and est.get(t, 0) <= 0
        if big and not part_empty:
            fails.append(f'{name}: R-Q18 Seq Scan trên bảng lớn {t} (~{est.get(t, est.get(base))} hàng)')
        else: passes += 1
    if re.search(r'Sort Method: external', txt): fails.append(f'{name}: R-Q06 sắp xếp tràn ra đĩa')
    else: passes += 1
    for m in re.finditer(r'Rows Removed by Filter: (\d+)', txt):
        if int(m.group(1)) > 5000 and 'R-Q06' not in allow: fails.append(f'{name}: R-Q06b lọc bỏ {m.group(1)} hàng sau khi đọc (chỉ mục chưa đủ chọn lọc)')
        else: passes += 1
print(f'lint: {passes} kiểm đạt, {len(fails)} vi phạm')
for x in fails: print('  FAIL', x)
sys.exit(1 if fails else 0)
