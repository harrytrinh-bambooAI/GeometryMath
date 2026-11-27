#!/usr/bin/env python3
"""Kiểm máy đặc tả chặng 5 (S5-E*). Phiên bản: v1.0 · 2026-11-28

Chạy từ gốc repo: python3 artifacts/GEO/work/s5/check_s5.py   (exit 0 = đạt)

Kiểm:
 1. Mỗi story có dòng "Ước lượng: dev X ngày · test Y ngày", 1 ≤ X ≤ 3, Y ≤ 0,3 × X.
 2. Mỗi story có ≥ 1 AC; mỗi AC có ≥ 1 TC (cột Remark của test_cases.md dẫn mã AC).
 3. Mã US, AC, BR (dòng định nghĩa), TC không trùng trên toàn chặng.
 4. Mỗi TC có cột "Tự động hóa" = E2E-UI hoặc E2E-API; bảng TC đúng 13 cột.
 5. Tổng trong estimate-s5.md (từng story, từng epic, toàn chặng, số US/AC/TC) khớp tổng cộng từ các tệp epic.
 6. Tệp đầu ra bắt buộc có mặt và có dòng "Phiên bản:".
Chỉ đọc tệp, không sửa gì.
"""
import re
import sys
from pathlib import Path

HERE = Path(__file__).resolve().parent
WORK = HERE.parent
EPICS = sorted(p for p in (WORK / "epics").glob("S5-E*") if p.is_dir())
EPS = 1e-9
errors = []


def num(s):
    return float(s.replace(",", "."))


def err(msg):
    errors.append(msg)


# ---------- 6. tệp bắt buộc ----------
for name in ("brief-s5.md", "estimate-s5.md", "questions-s5.md", "review-s5.md"):
    p = HERE / name
    if not p.exists():
        err(f"thiếu {p.relative_to(WORK)}")
    elif "Phiên bản:" not in p.read_text(encoding="utf-8").splitlines()[0]:
        err(f"{name}: dòng đầu không có 'Phiên bản:'")
if not EPICS:
    err("không thấy thư mục epics/S5-E*")

RE_US = re.compile(r"^## (US-S5E\d+-\d{2}) — (.+)$")
RE_EST = re.compile(r"Ước lượng: dev ([\d,\.]+) ngày · test ([\d,\.]+) ngày")
RE_AC = re.compile(r"^\*\*(US-S5E\d+-\d{2}-(?:SC|FC)\d+) — ")
RE_BR = re.compile(r"^\| (BR-S5E\d+-\d{2}) \|")
RE_TC = re.compile(r"^\| (TC-S5E\d+-\d{2,3}) \|")
RE_ACREF = re.compile(r"US-S5E\d+-\d{2}-(?:SC|FC)\d+")

all_us, all_ac, all_br, all_tc = [], [], [], []
stories = {}          # us -> (epic, dev, test)
epic_tot = {}         # epic -> [nus, nac, ntc, dev, test]
ac_tc = {}            # ac -> count of TC

for ed in EPICS:
    epic = ed.name
    sa, tcf = ed / "stories_ac.md", ed / "test_cases.md"
    for f in (sa, tcf):
        if not f.exists():
            err(f"{epic}: thiếu {f.name}")
    if not (sa.exists() and tcf.exists()):
        continue
    cur = None
    ests = {}
    acs_of = {}
    for line in sa.read_text(encoding="utf-8").splitlines():
        m = RE_US.match(line)
        if m:
            cur = m.group(1)
            all_us.append(cur)
            acs_of[cur] = []
            continue
        if cur is None:
            continue
        m = RE_EST.search(line)
        if m:
            if cur in ests:
                err(f"{cur}: hai dòng Ước lượng")
            ests[cur] = (num(m.group(1)), num(m.group(2)))
        m = RE_AC.match(line)
        if m:
            ac = m.group(1)
            if not ac.startswith(cur + "-"):
                err(f"{ac} nằm dưới story {cur}")
            all_ac.append(ac)
            acs_of[cur].append(ac)
        m = RE_BR.match(line)
        if m:
            all_br.append(m.group(1))
    # 1, 2 (AC)
    dsum = tsum = 0.0
    for us in acs_of:
        if us not in ests:
            err(f"{us}: thiếu dòng 'Ước lượng: dev X ngày · test Y ngày'")
            continue
        d, t = ests[us]
        if not (1 - EPS <= d <= 3 + EPS):
            err(f"{us}: dev {d} ngoài [1, 3]")
        if t > 0.3 * d + EPS:
            err(f"{us}: test {t} > 0,3 × dev {d}")
        if t <= 0:
            err(f"{us}: test phải > 0")
        if not acs_of[us]:
            err(f"{us}: không có AC")
        stories[us] = (epic, d, t)
        dsum += d
        tsum += t
    # test cases
    ntc = 0
    for line in tcf.read_text(encoding="utf-8").splitlines():
        m = RE_TC.match(line)
        if not m:
            continue
        tc = m.group(1)
        ntc += 1
        all_tc.append(tc)
        cells = [c.strip() for c in line.strip().strip("|").split("|")]
        if len(cells) != 13:
            err(f"{tc}: bảng có {len(cells)} cột, cần 13 (ô chứa dấu | ?)")
            continue
        if cells[6] not in ("E2E-UI", "E2E-API"):
            err(f"{tc}: Tự động hóa = '{cells[6]}', cần E2E-UI hoặc E2E-API")
        refs = RE_ACREF.findall(cells[12])
        if not refs:
            err(f"{tc}: Remark không dẫn mã AC")
        for r in refs:
            ac_tc[r] = ac_tc.get(r, 0) + 1
    nac = sum(len(v) for v in acs_of.values())
    epic_tot[epic] = [len(acs_of), nac, ntc, dsum, tsum]

# 2 (TC phủ AC)
for ac in all_ac:
    if ac_tc.get(ac, 0) < 1:
        err(f"{ac}: không có TC")
for r in ac_tc:
    if r not in all_ac:
        err(f"TC dẫn mã AC không tồn tại: {r}")

# 3 trùng mã
for label, lst in (("US", all_us), ("AC", all_ac), ("BR", all_br), ("TC", all_tc)):
    seen = set()
    for x in lst:
        if x in seen:
            err(f"mã {label} trùng: {x}")
        seen.add(x)

# 5 estimate
est = HERE / "estimate-s5.md"
if est.exists():
    txt = est.read_text(encoding="utf-8")
    row_story = re.compile(r"^\| (S5-E\d+) \| (US-S5E\d+-\d{2}) \| .*? \| ([\d,\.]+) \| ([\d,\.]+) \| ([\d,\.]+) \|$")
    row_epic = re.compile(r"^\| \*\*(S5-E\d+) cộng\*\* \| \| \| \*\*([\d,\.]+)\*\* \| \*\*([\d,\.]+)\*\* \| \*\*([\d,\.]+)\*\* \|$")
    row_sum = re.compile(r"^\| (S5-E\d+) \| .*? \| (\d+) \| (\d+) \| (\d+) \| ([\d,\.]+) \| ([\d,\.]+) \| ([\d,\.]+) \| ([\d,\.]+) \|$")
    row_total = re.compile(r"^\| \*\*Toàn chặng\*\* \| \| \*\*(\d+)\*\* \| \*\*(\d+)\*\* \| \*\*(\d+)\*\* \| \*\*([\d,\.]+)\*\* \| \*\*([\d,\.]+)\*\* \| \*\*([\d,\.]+)\*\* \| \*\*([\d,\.]+)\*\* \|$")
    row_total2 = re.compile(r"^\| \*\*Toàn chặng\*\* \| \| \| \*\*([\d,\.]+)\*\* \| \*\*([\d,\.]+)\*\* \| \*\*([\d,\.]+)\*\* \|$")
    seen_story, seen_epic, seen_sum = set(), set(), set()
    tot_ok = tot2_ok = False
    G = [0, 0, 0, 0.0, 0.0]
    for v in epic_tot.values():
        for i in range(5):
            G[i] += v[i]

    def close(a, b):
        return abs(a - b) < 0.005

    for line in txt.splitlines():
        m = row_story.match(line)
        if m:
            ep, us, d, t, s = m.group(1), m.group(2), num(m.group(3)), num(m.group(4)), num(m.group(5))
            seen_story.add(us)
            if us not in stories:
                err(f"estimate: {us} không có trong tệp epic")
            else:
                e0, d0, t0 = stories[us]
                if ep != e0 or not close(d, d0) or not close(t, t0) or not close(s, d0 + t0):
                    err(f"estimate: {us} lệch ({ep} {d}/{t}/{s} ≠ {e0} {d0}/{t0}/{d0 + t0})")
            continue
        m = row_epic.match(line)
        if m:
            ep = m.group(1)
            seen_epic.add(ep)
            v = epic_tot.get(ep)
            if not v or not (close(num(m.group(2)), v[3]) and close(num(m.group(3)), v[4]) and close(num(m.group(4)), v[3] + v[4])):
                err(f"estimate: dòng cộng {ep} lệch tổng từ tệp epic")
            continue
        m = row_sum.match(line)
        if m:
            ep = m.group(1)
            seen_sum.add(ep)
            v = epic_tot.get(ep)
            vals = [int(m.group(2)), int(m.group(3)), int(m.group(4)), num(m.group(5)), num(m.group(6)), num(m.group(7)), num(m.group(8))]
            if not v or vals[:3] != v[:3] or not (close(vals[3], v[3]) and close(vals[4], v[4]) and close(vals[5], v[3] + v[4]) and close(vals[6], v[3] + v[4])):
                err(f"estimate: dòng tóm tắt {ep} lệch ({vals} ≠ {v})")
            continue
        m = row_total.match(line)
        if m:
            tot_ok = True
            vals = [int(m.group(1)), int(m.group(2)), int(m.group(3)), num(m.group(4)), num(m.group(5)), num(m.group(6)), num(m.group(7))]
            if vals[:3] != G[:3] or not (close(vals[3], G[3]) and close(vals[4], G[4]) and close(vals[5], G[3] + G[4]) and close(vals[6], G[3] + G[4])):
                err(f"estimate: dòng Toàn chặng (tóm tắt) lệch ({vals} ≠ {G})")
            continue
        m = row_total2.match(line)
        if m:
            tot2_ok = True
            if not (close(num(m.group(1)), G[3]) and close(num(m.group(2)), G[4]) and close(num(m.group(3)), G[3] + G[4])):
                err("estimate: dòng Toàn chặng (chi tiết) lệch")
    for us in stories:
        if us not in seen_story:
            err(f"estimate: thiếu dòng của {us}")
    for ep in epic_tot:
        if ep not in seen_epic:
            err(f"estimate: thiếu dòng cộng của {ep}")
        if ep not in seen_sum:
            err(f"estimate: thiếu dòng tóm tắt của {ep}")
    if not tot_ok:
        err("estimate: thiếu dòng Toàn chặng ở bảng tóm tắt")
    if not tot2_ok:
        err("estimate: thiếu dòng Toàn chặng ở bảng chi tiết")

# ---------- báo cáo ----------
G = [0, 0, 0, 0.0, 0.0]
for v in epic_tot.values():
    for i in range(5):
        G[i] += v[i]
api = ui = 0
for ed in EPICS:
    f = ed / "test_cases.md"
    if f.exists():
        for line in f.read_text(encoding="utf-8").splitlines():
            if RE_TC.match(line):
                if "| E2E-API |" in line:
                    api += 1
                elif "| E2E-UI |" in line:
                    ui += 1
devs = [v[1] for v in stories.values()]
print(f"Epic {len(epic_tot)} · US {G[0]} · AC {G[1]} · BR {len(all_br)} · TC {G[2]} (E2E-API {api}, E2E-UI {ui})")
for ep, v in sorted(epic_tot.items()):
    print(f"  {ep}: US {v[0]} · AC {v[1]} · TC {v[2]} · dev {v[3]:g} · test {v[4]:g} · tổng {v[3] + v[4]:g}")
print(f"Toàn chặng: dev {G[3]:g} · test {G[4]:g} · tổng {G[3] + G[4]:g} ngày công")
if devs:
    print(f"Cỡ story (dev): nhỏ nhất {min(devs):g} · lớn nhất {max(devs):g} · trung bình {sum(devs) / len(devs):.2f}")
if errors:
    print(f"\nKHÔNG ĐẠT: {len(errors)} lỗi")
    for e in errors:
        print(" -", e)
    sys.exit(1)
print("ĐẠT")
