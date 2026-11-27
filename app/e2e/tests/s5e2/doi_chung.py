"""Bộ kiểm đối chứng S5-E2 (T-261128-52): chạy check.kiem_doc_lap (Kiểm 6 của materials/de-thi/check.py, độc lập
với bộ lắp đề) + schema đề trên các mã đề máy chủ trả về; độ khó các mã cùng nhóm (ma trận, seed) lệch ≤ 5 %.
  python3 -I doi_chung.py <de-thi> <ma-de.json>...   → exit 0 = ĐẠT
"""
import glob
import json
import os
import sys
from collections import defaultdict

D = sys.argv[1]
sys.path.insert(0, D)
import check  # noqa: E402


def load(p):
    return json.load(open(p, encoding="utf-8"))


SC = load(os.path.join(D, "schema", "de.schema.json"))
mts = {}
for f in sorted(glob.glob(os.path.join(D, "ma-tran", "*.json"))):
    if not f.endswith("yccd.json"):
        m = load(f)
        mts[m["id"]] = m
bank = []
for f in sorted(glob.glob(os.path.join(D, "ngan-hang", "*.json"))):
    bank += load(f)["cau"]
by = {it["id"]: it for it in bank}
yccd = load(os.path.join(D, "ma-tran", "yccd.json"))["yccd"]
groups = defaultdict(list)
for f in sys.argv[2:]:
    de = load(f)
    e = check.validate(de, SC)
    if e:
        check.bad(f"schema {os.path.basename(f)}: {e[:3]}")
    check.kiem_doc_lap(de, mts[de["ma_tran"]], by, yccd)
    groups[os.path.basename(f).rsplit("-", 1)[0]].append(
        sum(q["diem"] * by[q["id_cau"]]["do_kho"] for q in de["cau"]) / sum(q["diem"] for q in de["cau"]))
for g, dks in groups.items():
    lech = (max(dks) - min(dks)) / (sum(dks) / len(dks))
    if lech > 0.05:
        check.bad(f"{g}: độ khó lệch {lech:.3f} > 5 %")
for s in check.LOI[:40]:
    print("LỖI", s)
print(("ĐẠT " if not check.LOI else "CHƯA ĐẠT ") + f"{len(sys.argv) - 2} mã")
sys.exit(1 if check.LOI else 0)
