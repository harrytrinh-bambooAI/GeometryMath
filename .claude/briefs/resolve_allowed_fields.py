#!/usr/bin/env python3
"""Gỡ xung đột gộp ở allowed_fields.tsv: giữ cả hai phía (HEAD trước), bỏ dòng trùng khóa (cột 1–3, không tính dòng #).
Dòng "Phiên bản" xung đột: giữ bản HEAD. In số dòng trùng đã bỏ. Dùng: resolve_allowed_fields.py <tệp>"""
import sys

p = sys.argv[1]
L = open(p, encoding="utf8").read().split("\n")
out, i = [], 0
while i < len(L):
    if L[i].startswith("<<<<<<<"):
        j = L.index("=======", i)
        k = next(x for x in range(j, len(L)) if L[x].startswith(">>>>>>>"))
        ours, theirs = L[i + 1:j], L[j + 1:k]
        if any("Phiên bản" in x for x in ours) and len(ours) <= 2:
            out += ours
        else:
            out += ours + theirs
        i = k + 1
    else:
        out.append(L[i])
        i += 1
seen, final, dropped = set(), [], 0
for line in out:
    if line and not line.startswith("#"):
        key = tuple(line.split("\t")[:3])
        if key in seen:
            dropped += 1
            continue
        seen.add(key)
    final.append(line)
open(p, "w", encoding="utf8").write("\n".join(final))
print(f"đã gỡ; bỏ {dropped} dòng trùng khóa")
