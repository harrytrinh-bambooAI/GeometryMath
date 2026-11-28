#!/usr/bin/env python3
"""Phần trăm tiến độ chặng 5 từ .claude/queue.md (task T-261128-*), cân theo giờ ước trong tiêu đề (`· 4h/10M ·`).
Task đang chạy tính 50 %. In bảng + tổng. Dùng: python3 .claude/briefs/progress_s5.py"""
import re, pathlib
q = pathlib.Path(__file__).resolve().parents[1] / "queue.md"
HEAD = re.compile(r"^## (T-261128-\d+) · (.+?) · (.+?) \[(\w+)\]\s*$")
rows = []
for line in q.read_text(encoding="utf8").splitlines():
    m = HEAD.match(line)
    if not m:
        continue
    tid, epic, rest, st = m.groups()
    h = re.search(r"([\d.]+)h/", rest)
    hours = float(h.group(1)) if h else (0.0 if epic.startswith("GATE") else 1.0)
    rows.append((tid, epic, st, hours, rest.split(" · ")[-1][:60]))
W = {"done": 1.0, "skip": 1.0, "running": 0.5}
tot = sum(r[3] for r in rows)
got = sum(r[3] * W.get(r[2], 0.0) for r in rows)
code = [r for r in rows if r[1].startswith("S5") and "Tester" not in r[4]]
print(f"{'Task':<13}{'Epic':<12}{'Trạng thái':<10}{'Giờ':>5}  Nội dung")
for r in rows:
    print(f"{r[0]:<13}{r[1]:<12}{r[2]:<10}{r[3]:>5}  {r[4]}")
cd = sum(1 for r in code if r[2] == "done")
print(f"\nTask code S5 xong: {cd}/{len(code)} · Theo giờ ước (đang chạy = 50 %): {got:.1f}/{tot:.1f} giờ = {100*got/tot:.0f} %")
