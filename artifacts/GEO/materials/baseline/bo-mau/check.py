#!/usr/bin/env python3
"""Kiểm bộ mẫu baseline (bo-mau/items/BM-*.json). Exit 0 khi đạt mọi điều kiện:
- 50 <= số bài <= 80; id không trùng, khớp tên tệp
- đủ trường theo schema (bài, bước, lời giải sai); giá trị enum hợp lệ
- tổng điểm các bước = tong_diem; thang tự soạn: mọi điểm là bội 0,25
- mỗi bài >= 2 lời giải sai, buoc_sai tồn tại, khác loại lỗi, 0 <= diem_dung < tong_diem
- bài MVP: dap_so.so khớp dap_so_engine (sai số 1e-6) và khớp engine-values.json
- >= 5 bài bieu_diem = "chinh-thuc"
"""
import glob, json, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
EPS = 1e-6
TOP = ["id", "nguon", "cap", "chu_de", "muc_do", "de_bai", "dap_so", "dap_so_engine", "kien_thuc",
       "tong_diem", "bieu_diem", "buoc", "loi_giai_sai", "trang_thai", "ghi_chu"]
STEP = ["ma", "noi_dung", "ket_qua_trung_gian", "kien_thuc", "diem", "tieu_chi_dat", "cach_khac", "loi_thuong_gap"]
WRONG = ["id", "buoc_sai", "loai_loi", "mo_ta", "loi_giai", "diem_dung"]
LOAI_NGUON = {"mvp", "lop11", "de-chinh-thuc"}
MUC = {"NB", "TH", "VD", "VDC"}
LOAI_LOI = {"sai-dinh-li", "sai-tinh-toan", "thieu-chung-minh", "sai-hinh-dung", "nham-dieu-kien"}
num = (int, float)


def is_q(x):  # bội 0,25
    return abs(x * 4 - round(x * 4)) < 1e-9


def main():
    errs = []
    files = sorted(glob.glob(os.path.join(HERE, "items", "BM-*.json")))
    ids, n_ct = set(), 0
    eng_path = os.path.join(HERE, "engine-values.json")
    eng = json.load(open(eng_path, encoding="utf-8")) if os.path.exists(eng_path) else {}
    eng_vals = {round(q["v"], 6) for v in eng.values() for q in v}
    if not 50 <= len(files) <= 80:
        errs.append(f"số bài {len(files)} ngoài [50;80]")
    for fp in files:
        name = os.path.basename(fp)[:-5]
        try:
            it = json.load(open(fp, encoding="utf-8"))
        except Exception as e:
            errs.append(f"{name}: JSON lỗi {e}")
            continue
        E = lambda m: errs.append(f"{name}: {m}")
        miss = [k for k in TOP if k not in it]
        if miss:
            E(f"thiếu trường {miss}")
            continue
        if it["id"] != name:
            E("id khác tên tệp")
        if it["id"] in ids:
            E("id trùng")
        ids.add(it["id"])
        ng = it["nguon"]
        if not isinstance(ng, dict) or ng.get("loai") not in LOAI_NGUON or not ng.get("ref"):
            E("nguon sai")
        if it["muc_do"] not in MUC:
            E("muc_do sai")
        if it["bieu_diem"] not in {"chinh-thuc", "tu-soan"}:
            E("bieu_diem sai")
        if it["trang_thai"] not in {"nhap", "da-soat"}:
            E("trang_thai sai")
        for k in ["cap", "chu_de", "de_bai"]:
            if not isinstance(it[k], str) or not it[k].strip():
                E(f"{k} rỗng")
        ds = it["dap_so"]
        if not isinstance(ds, dict) or "gia_tri" not in ds or "so" not in ds or not ds["gia_tri"]:
            E("dap_so thiếu gia_tri/so")
            ds = {"so": None}
        if ds["so"] is not None and not isinstance(ds["so"], num):
            E("dap_so.so không phải số")
        if not isinstance(it["kien_thuc"], list) or not it["kien_thuc"]:
            E("kien_thuc rỗng")
        if it["bieu_diem"] == "chinh-thuc":
            n_ct += 1
            if ng.get("loai") != "de-chinh-thuc":
                E("biểu điểm chính thức nhưng nguồn không phải de-chinh-thuc")
        if ng.get("loai") == "mvp":
            de = it["dap_so_engine"]
            if not isinstance(de, num) or not isinstance(ds["so"], num) or abs(de - ds["so"]) > EPS:
                E(f"dap_so.so {ds['so']} không khớp dap_so_engine {de}")
            elif eng and round(de, 6) not in eng_vals:
                E("dap_so_engine không có trong engine-values.json")
        elif it["dap_so_engine"] is not None:
            E("dap_so_engine phải null cho bài không phải MVP")
        steps = it["buoc"]
        if not isinstance(steps, list) or len(steps) < 2:
            E("cần >= 2 bước")
            continue
        codes, tot = [], 0.0
        for st in steps:
            m = [k for k in STEP if k not in st]
            if m:
                E(f"bước thiếu {m}")
                continue
            codes.append(st["ma"])
            if not isinstance(st["diem"], num) or st["diem"] <= 0:
                E(f"{st['ma']}: diem không hợp lệ")
                continue
            tot += st["diem"]
            if it["bieu_diem"] == "tu-soan" and not is_q(st["diem"]):
                E(f"{st['ma']}: điểm tự soạn không phải bội 0,25")
            if not isinstance(st["cach_khac"], list) or not isinstance(st["loi_thuong_gap"], list):
                E(f"{st['ma']}: cach_khac/loi_thuong_gap phải là list")
            for l in st["loi_thuong_gap"]:
                if not isinstance(l, dict) or "mo_ta" not in l or not isinstance(l.get("tru"), num) or l["tru"] > st["diem"] + 1e-9:
                    E(f"{st['ma']}: loi_thuong_gap sai dạng hoặc trừ quá điểm bước")
            for k in ["noi_dung", "ket_qua_trung_gian", "kien_thuc", "tieu_chi_dat"]:
                if not str(st[k]).strip():
                    E(f"{st['ma']}: {k} rỗng")
        if len(set(codes)) != len(codes):
            E("mã bước trùng")
        if not isinstance(it["tong_diem"], num) or abs(tot - it["tong_diem"]) > 1e-9:
            E(f"tổng điểm bước {tot} != tong_diem {it['tong_diem']}")
        ws = it["loi_giai_sai"]
        if not isinstance(ws, list) or len(ws) < 2:
            E("cần >= 2 lời giải sai")
            continue
        loai, wid = set(), set()
        for w in ws:
            m = [k for k in WRONG if k not in w]
            if m:
                E(f"lời giải sai thiếu {m}")
                continue
            if w["id"] in wid:
                E("id lời giải sai trùng")
            wid.add(w["id"])
            if w["buoc_sai"] not in codes:
                E(f"{w['id']}: buoc_sai {w['buoc_sai']} không tồn tại")
            if w["loai_loi"] not in LOAI_LOI:
                E(f"{w['id']}: loai_loi sai")
            loai.add(w["loai_loi"])
            dd = w["diem_dung"]
            if not isinstance(dd, num) or dd < 0 or dd >= it["tong_diem"] - 1e-9:
                E(f"{w['id']}: diem_dung {dd} ngoài [0; tong_diem)")
            if not str(w["loi_giai"]).strip() or not str(w["mo_ta"]).strip():
                E(f"{w['id']}: loi_giai/mo_ta rỗng")
        if len(loai) < 2:
            E("hai lời giải sai phải khác loại lỗi")
    if n_ct < 5:
        errs.append(f"chỉ {n_ct} bài biểu điểm chính thức (< 5)")
    for e in errs:
        print("LỖI", e)
    st = {}
    for fp in files:
        it = json.load(open(fp, encoding="utf-8"))
        st[it["trang_thai"]] = st.get(it["trang_thai"], 0) + 1
    print(f"{len(files)} bài · {n_ct} biểu điểm chính thức · trạng thái {st} · {len(errs)} lỗi")
    return 1 if errs else 0


if __name__ == "__main__":
    sys.exit(main())
