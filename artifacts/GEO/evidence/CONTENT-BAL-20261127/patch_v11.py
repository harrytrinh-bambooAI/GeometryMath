# Phiên bản: v1.1 · 2026-11-27 · runner-content-bal-fix
# Áp bản sửa v1.1 (HKN-52, 69, 71, 75, 77 từ new_problems.py) vào catalog-mvp.json và
# problem-specs.json tại chỗ. Chạy một lần: python3 -I patch_v11.py
import json
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
from new_problems import PROBLEMS  # noqa: E402

RES = os.path.abspath(os.path.join(HERE, "../../../../app/server/src/main/resources"))
CAT = os.path.join(RES, "content/catalog-mvp.json")
SPEC = os.path.join(RES, "geometry/problem-specs.json")
CODES = ["HKN-52", "HKN-69", "HKN-71", "HKN-75", "HKN-77"]
new = {p["code"]: p for p in PROBLEMS if p["code"] in CODES}

cat = json.load(open(CAT, encoding="utf-8"))
for i, p in enumerate(cat["problems"]):
    if p["code"] in new:
        n = new[p["code"]]
        cat["problems"][i] = {k: n[k] for k in
                              ("code", "level", "solid", "topic", "statement", "hints", "answer", "steps")}
cat["_doc"] = cat["_doc"].replace(
    "Phiên bản: v1.1 · 2026-11-27 (",
    "Phiên bản: v1.2 · 2026-11-27 (v1.2: sửa chip HKN-52, gợi ý HKN-69/71/75, đổi HKN-77 sang "
    "thể tích cầu; ", 1)
with open(CAT, "w", encoding="utf-8") as f:
    json.dump(cat, f, ensure_ascii=False, indent=1)
    f.write("\n")

text = open(SPEC, encoding="utf-8").read()
for c in ("HKN-52", "HKN-77"):
    line = f'  "{c}": {json.dumps(new[c]["spec"], ensure_ascii=False)}'
    text, k = re.subn(rf'^  "{c}": .*?(?=,?$)', lambda m: line, text, count=1, flags=re.M)
    assert k == 1
json.loads(text)
open(SPEC, "w", encoding="utf-8").write(text)
print("đã sửa", CODES)
