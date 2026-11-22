# Sinh biến thể pgbench có set_config('app.org_id') bọc giao dịch từ các kịch bản gốc (dùng cho so sánh RLS).
import os
base = os.path.join(os.path.dirname(__file__), 'pgbench')
os.makedirs(os.path.join(base, 'rls'), exist_ok=True)
for k in ['list', 'tracking', 'event_batch', 'open']:
    txt = open(os.path.join(base, 'p_%s.sql' % k)).read()
    if 'BEGIN;' in txt:
        txt = txt.replace("BEGIN;\n", "BEGIN;\nSELECT set_config('app.org_id', :org, true);\n", 1)
    else:
        lines = txt.splitlines()
        i = max(j for j, l in enumerate(lines) if l.rstrip().endswith('\\gset'))
        txt = "\n".join(lines[:i + 1]) + "\nBEGIN;\nSELECT set_config('app.org_id', :org, true);\n" + "\n".join(lines[i + 1:]) + "\nCOMMIT;\n"
    open(os.path.join(base, 'rls', 'p_%s.sql' % k), 'w').write(txt)
