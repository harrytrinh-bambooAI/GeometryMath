"""Sinh bo-mau/items/BM-NNN.json.
  python3 gen/build.py            -> ghi mọi bài với trang_thai "nhap"
  python3 gen/build.py --da-soat  -> chỉ gắn "da-soat" cho bài có mặt trong soat-cheo.json với ket_qua "dat"
"""
import json, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import mvp, lop11, chinhthuc  # noqa: E402

OUT = os.path.join(HERE, "..", "items")


def main():
    items = mvp.items + lop11.items + chinhthuc.items
    ok = set()
    if "--da-soat" in sys.argv:
        rep = json.load(open(os.path.join(HERE, "..", "soat-cheo.json"), encoding="utf-8"))
        ok = {r["id"] for r in rep["bai"] if r["ket_qua"] == "dat"}
    os.makedirs(OUT, exist_ok=True)
    for f in os.listdir(OUT):
        if f.endswith(".json"):
            os.remove(os.path.join(OUT, f))
    for it in items:
        if it["id"] in ok:
            it["trang_thai"] = "da-soat"
        with open(os.path.join(OUT, it["id"] + ".json"), "w", encoding="utf-8") as f:
            json.dump(it, f, ensure_ascii=False, indent=1)
            f.write("\n")
    print(f"{len(items)} bài; da-soat: {sum(1 for i in items if i['trang_thai'] == 'da-soat')}")


if __name__ == "__main__":
    main()
