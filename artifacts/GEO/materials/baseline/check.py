#!/usr/bin/env python3
"""Kiểm danh mục nguồn baseline. Phiên bản: v1.0 · 27/11/2026
Dùng: python3 check.py [--live]   (--live: gọi lại HTTP từng link)
Thoát 0 khi mọi điều kiện "Xong khi" đạt."""
import json, sys, subprocess, pathlib

HERE = pathlib.Path(__file__).parent
LIVE = "--live" in sys.argv
COMMON = ["id", "ten", "url", "loai", "cap", "chu_de", "tin_cay", "can_cu_tin_cay", "license", "dung_cho_baseline", "http", "tra_ngay"]
SPECS = {
    "nguon-loi-giai.json": dict(fields=COMMON + ["co_loi_giai_buoc", "co_bieu_diem_buoc", "co_dap_so"],
                                extra=lambda d: [("co_bieu_diem_buoc>=5", sum(1 for x in d if x.get("co_bieu_diem_buoc") is True) >= 5)]),
    "nguon-bai-giang.json": dict(fields=COMMON + ["tac_gia_don_vi", "dinh_dang"],
                                 extra=lambda d: [("tin_cay A>=6", sum(1 for x in d if x.get("tin_cay") == "A") >= 6),
                                                  ("giao-vien>=5", sum(1 for x in d if x.get("loai") == "giao-vien") >= 5)]),
}

def _curl(url, head):
    args = ["curl", "-sL", "-o", "/dev/null", "-w", "%{http_code}", "--max-time", "20", "-A", "Mozilla/5.0", url]
    if head:
        args.insert(1, "-I")
    r = subprocess.run(args, capture_output=True, text=True)
    try:
        return int(r.stdout.strip() or 0)
    except ValueError:
        return 0

def http(url):
    # Một số trang (hanhtrangso, hocmai) trả 404 cho HEAD nhưng 200 cho GET -> thử lại bằng GET.
    c = _curl(url, head=True)
    return c if 200 <= c < 400 else _curl(url, head=False)

ok_all = True
for name, spec in SPECS.items():
    p = HERE / name
    checks = []
    try:
        data = json.loads(p.read_text(encoding="utf-8"))
    except Exception as e:
        print(f"[{name}] FAIL đọc JSON: {e}"); ok_all = False; continue
    checks.append(("so nguon>=25", len(data) >= 25))
    missing = [(x.get("id", "?"), f) for x in data for f in spec["fields"] if f not in x or x[f] in (None, "")]
    checks.append((f"du truong (thieu {len(missing)})", not missing))
    checks.append(("tin_cay thuoc A/B/C", all(x.get("tin_cay") in ("A", "B", "C") for x in data)))
    ids = [x.get("id") for x in data]; urls = [x.get("url") for x in data]
    checks.append(("id/url khong trung", len(set(ids)) == len(ids) and len(set(urls)) == len(urls)))
    codes = [http(x["url"]) if LIVE else int(x.get("http") or 0) for x in data]
    alive = sum(1 for c in codes if 200 <= c < 400)
    checks.append((f"link song {alive}/{len(data)} >=90%", data and alive / len(data) >= 0.9))
    checks += spec["extra"](data)
    md = p.with_suffix(".md")
    checks.append(("co tep .md + Phien ban", md.exists() and "Phiên bản:" in md.read_text(encoding="utf-8")))
    for label, ok in checks:
        print(f"[{name}] {'OK  ' if ok else 'FAIL'} {label}")
        ok_all &= bool(ok)
    if missing:
        print(f"  thieu: {missing[:10]}")
    dead = [(x["id"], c) for x, c in zip(data, codes) if not 200 <= c < 400]
    if dead:
        print(f"  link chet: {dead}")
sys.exit(0 if ok_all else 1)
