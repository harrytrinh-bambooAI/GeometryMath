# Phiên bản: v1.0 · 2026-11-27 · runner-content-bal
# Gộp 38 đề mới vào catalog-mvp.json và problem-specs.json (giữ định dạng: catalog indent=1,
# specs một dòng mỗi mã). Chạy một lần; chạy lại sẽ báo trùng mã và dừng.
import json
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
from new_problems import PROBLEMS  # noqa: E402

ROOT = os.path.abspath(os.path.join(HERE, "../../../.."))
RES = os.path.join(ROOT, "app/server/src/main/resources")
CAT = os.path.join(RES, "content/catalog-mvp.json")
SPEC = os.path.join(RES, "geometry/problem-specs.json")

cat = json.load(open(CAT, encoding="utf-8"))
codes = {p["code"] for p in cat["problems"]}
assert not codes & {p["code"] for p in PROBLEMS}, "trùng mã — đã gộp rồi?"
cat["_doc"] = cat["_doc"].replace(
    "Phiên bản: v1.0 · 2026-11-26 · T-261126-02 (PO-60, MVP.md §5 P0).",
    "Phiên bản: v1.1 · 2026-11-27 (v1.1: cân chuyên đề, thêm HKN-47…84 — mỗi dạng 16–20 đề, "
    "đủ THCS và THPT; v1.0 · 2026-11-26 · T-261126-02, PO-60, MVP.md §5 P0).")
assert cat["_doc"].startswith("Phiên bản: v1.1")
for p in PROBLEMS:
    cat["problems"].append({k: p[k] for k in
                            ("code", "level", "solid", "topic", "statement", "hints", "answer", "steps")})
with open(CAT, "w", encoding="utf-8") as f:
    json.dump(cat, f, ensure_ascii=False, indent=1)
    f.write("\n")

text = open(SPEC, encoding="utf-8").read().rstrip()
assert text.endswith("}")
body = text[:-1].rstrip()
lines = [f'  "{p["code"]}": {json.dumps(p["spec"], ensure_ascii=False)}' for p in PROBLEMS]
text = body + ",\n" + ",\n".join(lines) + "\n}\n"
old_doc = '"_doc": "E2: mô tả dựng'
assert old_doc in text
text = text.replace(old_doc, '"_doc": "Phiên bản: v1.1 · 2026-11-27 (thêm mô tả dựng HKN-47…84 và các loại '
                    'đại lượng sphere_area, cyl_lateral, cyl_volume, cone_total_area, pyramid_volume, '
                    'pyramid_lateral, prism_lateral; mặt cầu nhận bốn điểm). E2: mô tả dựng', 1)
json.loads(text)
open(SPEC, "w", encoding="utf-8").write(text)
print("đã gộp", len(PROBLEMS), "đề; tổng", len(cat["problems"]))
