# Phiên bản: v1.0 · 2026-11-27 · runner-content-bal
# In bảng phân bố dạng × cấp × khối (markdown) của một catalog-mvp.json.
# Chạy: python3 -I distribution.py <catalog-mvp.json>
import collections
import json
import sys

ps = json.load(open(sys.argv[1], encoding="utf-8"))["problems"]
TOPICS = ["Góc", "Khoảng cách", "Thể tích", "Diện tích", "Mặt cầu"]
SOLIDS = ["pyramid", "prism", "box", "cube", "cone", "cylinder", "sphere"]
c = collections.Counter((p["topic"], p["level"], p["solid"]) for p in ps)
print("| Dạng | Cấp | " + " | ".join(SOLIDS) + " | Cộng cấp | Cộng dạng |")
print("|---|---|" + "---|" * (len(SOLIDS) + 2))
for t in TOPICS:
    tot = sum(v for (tt, _, _), v in c.items() if tt == t)
    for i, lv in enumerate(["thcs", "thpt"]):
        row = [c[(t, lv, s)] for s in SOLIDS]
        print(f"| {t if i == 0 else ''} | {lv.upper()} | " + " | ".join(str(x) for x in row)
              + f" | {sum(row)} | {tot if i == 0 else ''} |")
print(f"\nTổng: {len(ps)} đề")
