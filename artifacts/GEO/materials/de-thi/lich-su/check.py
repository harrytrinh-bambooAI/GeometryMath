#!/usr/bin/env python3
# Phiên bản: v1.0 · 27/11/2026 · kiểm hồ sơ đề lịch sử. exit 0 = đạt.
# Dùng: python3 check.py [--live]   (--live: curl lại mọi nguồn, ngoài việc đọc http đã ghi)
import json, os, sys, csv, glob, collections, subprocess

HERE = os.path.dirname(os.path.abspath(__file__))
errs = []
def err(m): errs.append(m)

tax = json.load(open(os.path.join(HERE, "taxonomy.json"), encoding="utf-8"))
CD = {c["id"] for c in tax["chu_de"]}
DG = {d["id"] for c in tax["chu_de"] for d in c["dang"]}
MUC = set(tax["muc_do"]); NL = set(tax["nang_luc"])
LOAI = {"tn4", "dung-sai", "tra-loi-ngan", "tu-luan"}
KY = {"tn-thpt", "dh-a", "dh-a1", "dh-b", "minh-hoa"}
FIELDS = ["so", "phan", "loai_cau", "diem", "chu_de", "dang", "lop", "muc_do", "nang_luc", "tom_tat"]

exams = []
for f in sorted(glob.glob(os.path.join(HERE, "de", "*.json"))):
    try:
        e = json.load(open(f, encoding="utf-8"))
    except Exception as ex:
        err(f"{os.path.basename(f)}: JSON lỗi {ex}"); continue
    exams.append((os.path.basename(f), e))

# 1. số lượng đề
tn = [e for _, e in exams if e.get("ky_thi") == "tn-thpt"]
dh = [e for _, e in exams if str(e.get("ky_thi", "")).startswith("dh")]
if len(exams) < 12: err(f"cần >= 12 đề, có {len(exams)}")
if len(tn) < 6: err(f"cần >= 6 đề TN THPT, có {len(tn)}")
if len(dh) < 4: err(f"cần >= 4 đề ĐH tự luận, có {len(dh)}")

for fn, e in exams:
    p = fn
    for k in ("ma", "ky_thi", "nam", "cau_truc", "nguon", "cau"):
        if k not in e: err(f"{p}: thiếu trường {k}")
    if e.get("ky_thi") not in KY: err(f"{p}: ky_thi lạ {e.get('ky_thi')}")
    if e.get("ma") != fn[:-5]: err(f"{p}: ma != tên tệp")
    phan = {ph["id"]: ph for ph in e.get("cau_truc", {}).get("phan", [])}
    # 2. đủ trường từng câu
    seen = collections.defaultdict(lambda: [0, 0.0])
    for c in e.get("cau", []):
        for k in FIELDS:
            if k not in c or c[k] in (None, ""): err(f"{p} câu {c.get('so')}: thiếu {k}")
        if c.get("loai_cau") not in LOAI: err(f"{p} câu {c.get('so')}: loai_cau lạ")
        if c.get("lop") not in (10, 11, 12): err(f"{p} câu {c.get('so')}: lop lạ")
        if c.get("muc_do") not in MUC: err(f"{p} câu {c.get('so')}: muc_do lạ")
        if c.get("nang_luc") not in NL: err(f"{p} câu {c.get('so')}: nang_luc lạ")
        if len(str(c.get("tom_tat", ""))) > 220: err(f"{p} câu {c.get('so')}: tom_tat quá dài")
        # 4. chu_de thuộc taxonomy
        if c.get("chu_de") not in CD: err(f"{p} câu {c.get('so')}: chu_de '{c.get('chu_de')}' ngoài taxonomy")
        for d in str(c.get("dang", "")).split("+"):
            if d not in DG: err(f"{p} câu {c.get('so')}: dang '{d}' ngoài taxonomy")
        if c.get("phan") not in phan: err(f"{p} câu {c.get('so')}: phan '{c.get('phan')}' không có trong cau_truc")
        else:
            seen[c["phan"]][0] += 1; seen[c["phan"]][1] += c.get("diem", 0)
    # 3. tổng điểm khớp cau_truc
    tot = 0.0
    for pid, ph in phan.items():
        n, s = seen[pid]
        if n != ph["so_cau"]: err(f"{p}: phần {pid} có {n} câu, cau_truc ghi {ph['so_cau']}")
        if abs(s - ph["diem"]) > 1e-6: err(f"{p}: phần {pid} tổng điểm {s:g}, cau_truc ghi {ph['diem']:g}")
        tot += ph["diem"]
    if abs(tot - 10) > 1e-6: err(f"{p}: tổng điểm cau_truc {tot:g} != 10")
    if e.get("cau_truc_loai") == "moi":
        if [phan[k]["so_cau"] for k in ("P1", "P2", "P3")] != [12, 4, 6]: err(f"{p}: cấu trúc mới phải 12/4/6")
    # 5. nguồn
    ok = [s for s in e.get("nguon", []) if 200 <= int(s.get("http", 0)) < 400 and s.get("url") and s.get("tra_ngay")]
    if not ok: err(f"{p}: không có nguồn http 2xx/3xx")
    if "--live" in sys.argv:
        live = 0
        for s in e.get("nguon", []):
            r = subprocess.run(["curl", "-sL", "-o", "/dev/null", "-w", "%{http_code}", "--max-time", "25", "-A", "Mozilla/5.0", s["url"]], capture_output=True, text=True)
            live += 200 <= int(r.stdout.strip() or 0) < 400
        if not live: err(f"{p}: --live: không nguồn nào truy cập được")

# 6. tan-suat.csv khớp dữ liệu
calc = collections.defaultdict(lambda: [0, 0.0])
for _, e in exams:
    for c in e.get("cau", []):
        k = (e["ky_thi"], e["cau_truc_loai"], c["chu_de"], str(e["nam"]))
        calc[k][0] += 1; calc[k][1] += c["diem"]
calc = {k: (v[0], round(v[1], 2)) for k, v in calc.items()}
try:
    rows = list(csv.DictReader(open(os.path.join(HERE, "tan-suat.csv"), encoding="utf-8")))
    got = {(r["ky_thi"], r["cau_truc"], r["chu_de"], r["nam"]): (int(r["so_cau"]), round(float(r["diem"]), 2)) for r in rows}
    if len(got) != len(rows): err("tan-suat.csv: có dòng trùng khóa")
    for k in set(calc) | set(got):
        if calc.get(k) != got.get(k): err(f"tan-suat.csv lệch tại {k}: tính lại {calc.get(k)} / csv {got.get(k)}")
except FileNotFoundError:
    err("thiếu tan-suat.csv")
for need in ("tan-suat.md", "README.md", "taxonomy.json"):
    if not os.path.exists(os.path.join(HERE, need)): err(f"thiếu {need}")

if errs:
    print(f"FAIL: {len(errs)} lỗi")
    for m in errs[:60]: print(" -", m)
    sys.exit(1)
print(f"PASS: {len(exams)} đề (TN THPT {len(tn)}, ĐH {len(dh)}, minh họa {len(exams)-len(tn)-len(dh)}), "
      f"{sum(len(e['cau']) for _, e in exams)} câu, csv {len(got)} dòng khớp.")
sys.exit(0)
