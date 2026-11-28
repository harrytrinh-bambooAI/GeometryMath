"""Phiên bản: v1.0 · 27/11/2026
Sinh mau-cau/*.json, ngan-hang/*.json và mau-cau/engine-values.json từ gen/mau_hh.py + gen/mau_khac.py.
Đáp số câu sinh từ mẫu MVP: gọi engine thật (tools/engine.py); dừng nếu biểu thức sympy lệch engine > 1e-6.
Chạy: python3 gen/build.py
"""
import hashlib
import json
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
DT = os.path.dirname(HERE)
sys.path.insert(0, HERE)
sys.path.insert(0, os.path.join(DT, "tools"))

import sympy as sp  # noqa: E402

import core  # noqa: E402
import mau_hh  # noqa: E402
import mau_khac  # noqa: E402

PHIEN_BAN = "v1.0 · 27/11/2026"
SPECS = os.path.join(DT, "..", "..", "..", "..", "app", "server", "src", "main", "resources", "geometry",
                     "problem-specs.json")
DON_VI = {"cm": r"\text{ cm}", "cm2": r"\text{ cm}^2", "cm3": r"\text{ cm}^3"}
DON_VI_CHU = {"cm": "cm", "cm2": "cm²", "cm3": "cm³"}
NHAN = "abcd"
# mẫu đúng/sai: số ý sai theo mã biến thể (xoay vòng để đáp án Đ/S không đoán được)
MAU_DS = [(True, False, True, False), (False, True, True, True), (True, True, False, False),
          (False, False, True, True), (True, False, False, True), (False, True, True, False)]


def h01(s):
    return int(hashlib.sha256(s.encode()).hexdigest()[:8], 16) / 0xFFFFFFFF


def kieu_of(m):
    return "goc" if m.get("don_vi") == "do" else m.get("kieu", "so")


def hien(m, e, kieu=None):
    """LaTeX một phương án/đáp án, kèm tiền tố/hậu tố/đơn vị."""
    k = kieu or kieu_of(m)
    s = core.tex(e, k)
    if k == "pt":
        s += "=0"
    s = m.get("tien_to", "") + s + m.get("hau_to", "") + DON_VI.get(m.get("don_vi", ""), "")
    return s


def do_kho(muc, loai, t_dung, key):
    d = core.DO_KHO_GOC[muc] + {"tn4": -0.05, "dung-sai": 0.0, "tra-loi-ngan": 0.05, "tu-luan": 0.05}[loai]
    if "\\sqrt" in t_dung or "\\pi" in t_dung:
        d += 0.02
    if "\\approx" in t_dung or "," in t_dung:
        d += 0.02
    d += (h01(key) - 0.5) * 0.06  # dao động nhỏ theo biến thể (độ khó dự kiến, chưa có dữ liệu thử)
    return round(min(0.95, max(0.05, d)), 3)


def engine_spec(m, env, mvp_specs, ask=None):
    src = mvp_specs[m["engine"]["mvp"]]
    i = m["engine"]["ask"] if ask is None else ask
    params = {k: core.num(core.S(v, env)) for k, v in m["engine"]["params"].items()}
    return {"template": src["template"], "params": params, "unit": src.get("unit", ""),
            "asked": [src["asked"][i]], "chips": [], "hidden": []}


def tln(m, ds, v, kieu):
    """Câu trả lời ngắn: (câu hỏi, chuỗi đáp án ≤ 4 ký tự, giá trị) hoặc None."""
    hoi = re.sub(r"\s*\(làm tròn[^)]*\)", "", m["hoi"]).rstrip()
    he_so, them = v, ""
    if m.get("don_vi") == "a":
        for k in (1, 2, 3):
            r = sp.simplify(ds / core.a ** k)
            if not r.has(core.a):
                he_so = core.num(r)
                them = r"$m\cdot a%s$. Tìm $m$" % ("" if k == 1 else "^%d" % k)
                break
        else:
            return None
    elif kieu == "goc":
        them = ""
    elif kieu in ("vecto", "pt", "ham"):
        return None
    nd_list = m.get("tln_nd", [2, 1, 0])
    exact = abs(he_so - round(he_so)) < 1e-9
    for nd in ([0] if exact else nd_list):
        s = core.tln_chuoi(he_so, nd)
        if s is not None:
            lam = "" if exact else " (làm tròn đến hàng %s)" % {0: "đơn vị", 1: "phần mười", 2: "phần trăm", 3: "phần nghìn"}[nd]
            if them:
                q = hoi + " " + them + lam + "."
            elif kieu == "goc":
                q = hoi + " bao nhiêu độ" + lam + "?"
            else:
                dv = (" (đơn vị: %s)" % DON_VI_CHU[m["don_vi"]]) if m.get("don_vi") in DON_VI_CHU else ""
                q = hoi + " bao nhiêu" + dv + lam + "?"
            return q, s, he_so
    return None


def base_item(m, ts, vi, loai, muc, env):
    return {"id": f"{m['id']}.{ {'tn4': 'TN', 'dung-sai': 'DS', 'tra-loi-ngan': 'TLN', 'tu-luan': 'TL'}[loai] }.{vi}",
            "mau_cau": m["id"], "bien_the": ts, "chu_de": m["chu_de"], "dang": m["dang"], "dang_tx": m["dang_tx"],
            "lop": m["lop"], "yccd": m["yccd"], "kien_thuc": [m["ten"]], "loai_cau": loai, "muc_do": muc,
            "nang_luc": m["nang_luc"], "thoi_gian_phut": core.THOI_GIAN[loai][muc],
            "diem": core.DIEM_MAC_DINH[loai], "boi_canh_key": core.boi_canh_key(m, ts),
            "nguon": dict(m["nguon"], mau_cau=m["id"])}


def sinh_don(m, mvp_specs, eng_jobs):
    """Câu TN4 / trả lời ngắn / tự luận từ một mẫu đơn."""
    out = []
    kieu = kieu_of(m)
    for vi, ts in enumerate(core.bien_the(m)):
        env = core.env_of(m, ts)
        ds, v0, t0, nh = core.phuong_an(m, env, kieu)
        t_dung = hien(m, ds)
        ctx = core.render(m["boi_canh"], env)
        for loai in m["loai_cau"]:
            muc = m["muc_do"][loai]
            it = base_item(m, ts, vi, loai, muc, env)
            if loai == "tn4":
                if len(nh) < 3:
                    continue
                it["de_bai"] = (ctx + " " if ctx else "") + core.render(m["hoi"], env) + ":"
                it["lua_chon"] = [{"noi_dung": t_dung, "dung": True}] + [
                    {"noi_dung": (m.get("tien_to", "") + n_["phuong_an"] + ("=0" if kieu == "pt" else "")
                                  + m.get("hau_to", "") + DON_VI.get(m.get("don_vi", ""), "")),
                     "dung": False, "loi": n_["loi"]} for n_ in nh[:3]]
                it["dap_an"] = t_dung
                it["nhieu"] = [{"phuong_an": c["noi_dung"], "loi": c["loi"]} for c in it["lua_chon"][1:]]
            elif loai == "tra-loi-ngan":
                r = tln(m, ds, v0, kieu)
                if r is None:
                    continue
                q, s, val = r
                it["de_bai"] = (ctx + " " if ctx else "") + core.render(q, env)
                it["dap_an"] = s
                it["nhieu"] = []
                it["gia_tri_chinh_xac"] = val
            else:
                if not m.get("tu_luan"):
                    continue
                env2 = dict(env, ds=ds)
                h = core.render(m["hoi"], env).removesuffix(" bằng").removesuffix(" là")
                h = re.sub(r"\s*\(làm tròn[^)]*\)", "", h)
                h = core.render(m["hoi_tl"], env) if m.get("hoi_tl") else "Tính " + h[0].lower() + h[1:] + "."
                it["de_bai"] = (ctx + " " if ctx else "") + h
                it["huong_dan_cham"] = [{"buoc": "B%d" % (i + 1), "noi_dung": core.render(b["noi_dung"], env2,
                                                                                         {"ds": kieu}),
                                         "diem": b["diem"]} for i, b in enumerate(m["tu_luan"])]
                it["dap_an"] = t_dung
                it["nhieu"] = []
            it["dap_so"] = {"gia_tri": t_dung, "so": v0 if kieu not in ("vecto", "pt", "ham") else None}
            it["do_kho"] = do_kho(muc, loai, t_dung, it["id"])
            it["dap_so_engine"] = None
            if m["nguon"]["loai"] == "mvp":
                eng_jobs.append((it, None, engine_spec(m, env, mvp_specs), v0))
            out.append(it)
    return out


def sinh_ds(d, mau_by_id, mvp_specs, eng_jobs):
    out = []
    dsm = dict(d, rang_buoc=d.get("rang_buoc", []))
    for vi, ts in enumerate(core.bien_the(dsm)):
        env = core.env_of(dsm, ts)
        pat = MAU_DS[(vi + int(h01(d["id"]) * 6)) % len(MAU_DS)]
        ys, ok = [], True
        for j, (y, dung) in enumerate(zip(d["y"], pat)):
            if "mau" in y:
                sub = mau_by_id[y["mau"]]
                env_s = core.env_of(sub, ts)
                ds, v0, t0, nh = core.phuong_an(sub, env_s, kieu_of(sub))
                t_dung, m_hien = hien(sub, ds), sub
            else:
                sub = dict(y, id=d["id"], duong=False, nhieu=y["nhieu"], kieu=y.get("kieu", d.get("kieu", "so")))
                env_s = env
                ds, v0, t0, nh = core.phuong_an(sub, env_s, sub["kieu"])
                t_dung, m_hien = hien(sub, ds), sub
            if not nh:
                ok = False
                break
            gia_tri = t_dung if dung else (m_hien.get("tien_to", "") + nh[0]["phuong_an"]
                                          + DON_VI.get(m_hien.get("don_vi", ""), ""))
            yy = {"nhan": NHAN[j], "menh_de": core.render(y["menh_de"].replace("{v}", "@@V@@"), env).replace(
                "@@V@@", gia_tri), "dung": dung, "muc_do": y["muc_do"], "gia_tri_dung": t_dung,
                "so": v0 if not isinstance(ds, sp.Tuple) else None}
            if not dung:
                yy["loi"] = nh[0]["loi"]
            ys.append(yy)
            if "mau" in y and mau_by_id[y["mau"]]["nguon"]["loai"] == "mvp":
                eng_jobs.append((None, yy, engine_spec(sub, env_s, mvp_specs), v0))
        if not ok:
            continue
        muc = max((y["muc_do"] for y in d["y"]), key=["NB", "TH", "VD", "VDC"].index)
        m_like = dict(d, loai_cau=["dung-sai"], muc_do={"dung-sai": muc},
                      nang_luc=d.get("nang_luc", "tu-duy-lap-luan"),
                      nguon={"loai": "mvp" if any("mau" in y for y in d["y"]) else "tu-soan", "ma": d["id"],
                             "ref": "ghép các ý từ " + ", ".join(sorted({y["mau"] for y in d["y"] if "mau" in y}))
                             if any("mau" in y for y in d["y"]) else "gen/mau_khac.py"})
        it = base_item(m_like, ts, vi, "dung-sai", muc, env)
        it["de_bai"] = core.render(d["boi_canh"], env) + " Xét tính đúng/sai của các mệnh đề sau."
        it["y"] = ys
        it["dap_an"] = "; ".join(f"{y['nhan']}) {'Đ' if y['dung'] else 'S'}" for y in ys)
        it["nhieu"] = [{"phuong_an": f"{y['nhan']}) {y['menh_de']}", "loi": y["loi"]} for y in ys if not y["dung"]]
        it["dap_so"] = {"gia_tri": it["dap_an"], "so": None}
        it["do_kho"] = do_kho(muc, "dung-sai", "".join(y["gia_tri_dung"] for y in ys), it["id"])
        it["dap_so_engine"] = None
        out.append(it)
    return out


def xuat_mau(m, extra):
    d = {k: v for k, v in m.items() if v is not None}
    d.update(extra)
    d["phien_ban"] = PHIEN_BAN
    return d


def main():
    mvp_specs = json.load(open(SPECS, encoding="utf-8"))
    mau_all = mau_hh.MAU + mau_khac.MAU
    mau_by_id = {m["id"]: m for m in mau_all}
    eng_jobs, cau = [], []
    os.makedirs(os.path.join(DT, "mau-cau"), exist_ok=True)
    os.makedirs(os.path.join(DT, "ngan-hang"), exist_ok=True)
    for f in os.listdir(os.path.join(DT, "mau-cau")):
        os.remove(os.path.join(DT, "mau-cau", f))
    for f in os.listdir(os.path.join(DT, "ngan-hang")):
        os.remove(os.path.join(DT, "ngan-hang", f))
    for m in mau_all:
        items = sinh_don(m, mvp_specs, eng_jobs)
        cau += items
        bt = core.bien_the(m)
        json.dump(xuat_mau(m, {"loai_mau": "don", "bien_the": bt, "so_bien_the": len(bt),
                               "so_cau_sinh": len(items)}),
                  open(os.path.join(DT, "mau-cau", m["id"] + ".json"), "w", encoding="utf-8"),
                  ensure_ascii=False, indent=1)
    for d in mau_hh.DUNG_SAI + mau_khac.DUNG_SAI:
        items = sinh_ds(d, mau_by_id, mvp_specs, eng_jobs)
        cau += items
        bt = core.bien_the(dict(d, rang_buoc=d.get("rang_buoc", [])))
        json.dump(xuat_mau(d, {"loai_mau": "dung-sai", "loai_cau": ["dung-sai"], "bien_the": bt,
                               "rang_buoc": d.get("rang_buoc", []), "dan_xuat": d.get("dan_xuat", {}),
                               "so_bien_the": len(bt), "so_cau_sinh": len(items),
                               "nguon": {"loai": "mvp" if any("mau" in y for y in d["y"]) else "tu-soan",
                                         "ma": d["id"]}}),
                  open(os.path.join(DT, "mau-cau", d["id"] + ".json"), "w", encoding="utf-8"),
                  ensure_ascii=False, indent=1)
    # engine thật cho mọi câu/ý sinh từ mẫu MVP
    import engine  # noqa: E402
    jobs = [{"id": str(i), "spec": s} for i, (_, _, s, _) in enumerate(eng_jobs)]
    res = engine.run(jobs)
    ev, loi = {}, []
    for i, (it, yy, spec, v) in enumerate(eng_jobs):
        e = res[str(i)][0]
        key = it["id"] if it else None
        if it is not None:
            it["dap_so_engine"] = e
            if it["loai_cau"] == "tra-loi-ngan":
                pass
        else:
            yy["so_engine"] = e
        if abs(e - v) > 1e-6 * max(1.0, abs(v)):
            loi.append((key or yy["menh_de"][:60], e, v))
    # gắn id câu cho các ý đúng/sai
    for it in cau:
        if it["loai_cau"] == "dung-sai":
            for y in it["y"]:
                if "so_engine" in y:
                    ev[it["id"] + "#" + y["nhan"]] = {"engine": y["so_engine"], "sympy": y["so"]}
        elif it["dap_so_engine"] is not None:
            ev[it["id"]] = {"engine": it["dap_so_engine"], "sympy": it["dap_so"]["so"],
                            "spec": None}
    if loi:
        for x in loi[:20]:
            print("LỆCH ENGINE:", x)
        raise SystemExit("engine lệch sympy: %d" % len(loi))
    json.dump({"phien_ban": PHIEN_BAN,
               "nguon": "DefaultGeometryEngine (app/server/src/main/java, biên dịch tạm 27/11/2026), build(spec, a=1) + measure",
               "so_gia_tri": len(ev), "gia_tri": ev},
              open(os.path.join(DT, "mau-cau", "engine-values.json"), "w", encoding="utf-8"), ensure_ascii=False,
              indent=0)
    by = {}
    for it in cau:
        by.setdefault(it["chu_de"], []).append(it)
    for cd, items in by.items():
        json.dump({"phien_ban": PHIEN_BAN, "chu_de": cd, "so_cau": len(items), "cau": items},
                  open(os.path.join(DT, "ngan-hang", cd + ".json"), "w", encoding="utf-8"), ensure_ascii=False,
                  indent=1)
    from collections import Counter
    print("mẫu đơn:", len(mau_all), "(MVP %d, khác %d)" % (len(mau_hh.MAU), len(mau_khac.MAU)),
          "| mẫu đúng/sai:", len(mau_hh.DUNG_SAI) + len(mau_khac.DUNG_SAI), "| câu:", len(cau),
          dict(Counter(c["loai_cau"] for c in cau)), "| giá trị engine:", len(ev))
    empty = [m["id"] for m in mau_all if not any(c["mau_cau"] == m["id"] for c in cau)]
    if empty:
        print("MẪU KHÔNG SINH ĐƯỢC CÂU:", empty)


if __name__ == "__main__":
    main()
