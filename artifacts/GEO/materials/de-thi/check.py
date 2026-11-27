"""Phiên bản: v1.0 · 27/11/2026
Kiểm máy hệ dữ liệu đề. Exit 0 khi:
 1. schema hợp lệ (mau-cau, ngan-hang, ma-tran, de-mau) + nhãn khớp lich-su/taxonomy.json và ma-tran/yccd.json;
 2. đủ số lượng: ≥ 4 ma trận (KT15, GK2, CK2, TN 764), ≥ 30 mẫu MVP, ≥ 20 mẫu ngoài hình không gian;
 3. đáp số: tính lại từ hàm đáp số trong mau-cau/*.json (sympy) khớp ngân hàng; mẫu ngoài HHKG khớp phép kiểm độc lập
    (gen/mau_khac.py KIEM); câu sinh từ mẫu MVP khớp engine thật (chạy lại engine nếu có javac/java, sai số 1e-6);
 4. 100 % nhiễu gắn một lỗi; trả lời ngắn ≤ 4 ký tự;
 5. ma trận: tổng điểm, số câu từng phần, trọng số xoay vòng (tổng 1, có căn cứ);
 6. mỗi ma trận lắp được ≥ 3 mã đề với 2 seed; mọi ràng buộc đạt (kiểm lại độc lập, không dùng kiem_de của lap_de);
 7. đề mẫu thi thử TN (de-mau/TN-THPT-764/ma-101.md) và README có dòng phiên bản.
Tùy chọn: --no-engine (bỏ chạy lại engine, dùng giá trị đã lưu).
"""
import glob
import json
import math
import os
import re
import shutil
import sys
from collections import Counter

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, "gen"))
sys.path.insert(0, os.path.join(HERE, "tools"))
sys.path.insert(0, HERE)

LOI = []
OK = []


def bad(msg):
    LOI.append(msg)


def good(msg):
    OK.append(msg)


# ------------------------------------------------------------------ mini JSON Schema (tập con 2020-12)
def _type_ok(v, t):
    m = {"object": dict, "array": list, "string": str, "boolean": bool, "null": type(None)}
    if t == "number":
        return isinstance(v, (int, float)) and not isinstance(v, bool)
    if t == "integer":
        return isinstance(v, int) and not isinstance(v, bool)
    return isinstance(v, m[t])


def validate(v, s, path="$"):
    errs = []
    if "type" in s:
        ts = s["type"] if isinstance(s["type"], list) else [s["type"]]
        if not any(_type_ok(v, t) for t in ts):
            return [f"{path}: kiểu {type(v).__name__} ∉ {ts}"]
    if "enum" in s and v not in s["enum"]:
        errs.append(f"{path}: {v!r} ∉ enum")
    if "const" in s and v != s["const"]:
        errs.append(f"{path}: ≠ const")
    if isinstance(v, str):
        if len(v) < s.get("minLength", 0) or len(v) > s.get("maxLength", 10 ** 9):
            errs.append(f"{path}: độ dài chuỗi")
        if "pattern" in s and not re.search(s["pattern"], v):
            errs.append(f"{path}: không khớp pattern {s['pattern']}")
    if isinstance(v, (int, float)) and not isinstance(v, bool):
        if "minimum" in s and v < s["minimum"]:
            errs.append(f"{path}: < minimum")
        if "maximum" in s and v > s["maximum"]:
            errs.append(f"{path}: > maximum")
        if "exclusiveMinimum" in s and v <= s["exclusiveMinimum"]:
            errs.append(f"{path}: ≤ exclusiveMinimum")
    if isinstance(v, list):
        if len(v) < s.get("minItems", 0) or len(v) > s.get("maxItems", 10 ** 9):
            errs.append(f"{path}: số phần tử {len(v)}")
        if "items" in s:
            for i, x in enumerate(v):
                errs += validate(x, s["items"], f"{path}[{i}]")
    if isinstance(v, dict):
        for r in s.get("required", []):
            if r not in v:
                errs.append(f"{path}: thiếu {r}")
        for k, sub in s.get("properties", {}).items():
            if k in v:
                errs += validate(v[k], sub, f"{path}.{k}")
    for sub in s.get("allOf", []):
        if "if" in sub:
            if not validate(v, sub["if"], path):
                errs += validate(v, sub.get("then", {}), path)
        else:
            errs += validate(v, sub, path)
    return errs


def load(p):
    return json.load(open(p, encoding="utf-8"))


def main():
    use_engine = "--no-engine" not in sys.argv
    SC = {n: load(os.path.join(HERE, "schema", n + ".schema.json")) for n in ("cau", "mau_cau", "ma_tran", "de")}
    tax = load(os.path.join(HERE, "lich-su", "taxonomy.json"))
    tax_dang = {c["id"]: {d["id"] for d in c["dang"]} for c in tax["chu_de"]}
    yccd = load(os.path.join(HERE, "ma-tran", "yccd.json"))["yccd"]

    # ---------------------------------------------------------------- 1. schema + nhãn
    maus = {}
    for f in sorted(glob.glob(os.path.join(HERE, "mau-cau", "MC-*.json"))):
        m = load(f)
        maus[m["id"]] = m
        e = validate(m, SC["mau_cau"])
        if e:
            bad(f"schema mẫu {m['id']}: {e[:3]}")
    bank = []
    for f in sorted(glob.glob(os.path.join(HERE, "ngan-hang", "*.json"))):
        bank += load(f)["cau"]
    for it in bank:
        e = validate(it, SC["cau"])
        if e:
            bad(f"schema câu {it['id']}: {e[:3]}")
    mts = {}
    for f in sorted(glob.glob(os.path.join(HERE, "ma-tran", "*.json"))):
        if f.endswith("yccd.json"):
            continue
        m = load(f)
        mts[m["id"]] = m
        e = validate(m, SC["ma_tran"])
        if e:
            bad(f"schema ma trận {m['id']}: {e[:3]}")
    for obj in list(maus.values()) + bank:
        if obj["chu_de"] not in tax_dang:
            bad(f"{obj['id']}: chủ đề {obj['chu_de']} không có trong taxonomy")
        elif obj["dang_tx"] not in tax_dang[obj["chu_de"]]:
            bad(f"{obj['id']}: dạng {obj['dang_tx']} không thuộc chủ đề {obj['chu_de']} trong taxonomy")
        for c in obj["yccd"]:
            if c not in yccd:
                bad(f"{obj['id']}: YCCĐ {c} không có trong yccd.json")
    for m in mts.values():
        for c in m["cells"]:
            for g in ([c] if c["loai_o"] == "co-dinh" else c["nhom"]):
                if g["chu_de"] not in tax_dang:
                    bad(f"{m['id']}/{c['id']}: chủ đề lạ {g['chu_de']}")
                for d in g.get("dang_tx") or []:
                    if d not in tax_dang.get(g["chu_de"], ()):
                        bad(f"{m['id']}/{c['id']}: dạng lạ {d}")
                for y in g["yccd"]:
                    if y not in yccd:
                        bad(f"{m['id']}/{c['id']}: YCCĐ lạ {y}")
    good(f"schema + nhãn: {len(maus)} mẫu, {len(bank)} câu, {len(mts)} ma trận")

    # ---------------------------------------------------------------- 2. số lượng
    HH = {"hinh-khong-gian-quan-he", "hinh-khong-gian-the-tich", "khoi-tron-xoay"}
    mvp_don = [m for m in maus.values() if m["nguon"]["loai"] == "mvp" and m["loai_mau"] == "don"]
    khac = [m for m in maus.values() if m["chu_de"] not in HH]
    need = {"KT15-HHKG11", "GK2-L11", "CK2-L11", "TN-THPT-764"}
    if not need <= set(mts):
        bad(f"thiếu ma trận {need - set(mts)}")
    if len(mvp_don) < 30:
        bad(f"mẫu MVP {len(mvp_don)} < 30")
    if len(khac) < 20:
        bad(f"mẫu ngoài HHKG {len(khac)} < 20")
    mvp_codes = {m["nguon"]["ma"] for m in mvp_don}
    good(f"số lượng: {len(mts)} ma trận, {len(mvp_don)} mẫu MVP (từ {len(mvp_codes)} đề MVP), {len(khac)} mẫu ngoài HHKG, "
         f"{len(maus)} mẫu tổng, {len(bank)} câu ngân hàng")

    # ---------------------------------------------------------------- 3. đáp số
    import core
    import mau_khac
    import sympy as sp
    by_id = {it["id"]: it for it in bank}
    n_re, n_kiem, n_eng = 0, 0, 0
    for it in bank:
        if it["loai_cau"] == "dung-sai":
            continue
        m = maus[it["mau_cau"]]
        env = core.env_of(m, it["bien_the"])
        ds = core.simp(core.S(m["dap_so"], env))
        if it["dap_so"]["so"] is not None:
            v = core.num(ds)
            if abs(v - it["dap_so"]["so"]) > 1e-9 * max(1, abs(v)):
                bad(f"{it['id']}: hàm đáp số mẫu cho {v} ≠ ngân hàng {it['dap_so']['so']}")
            n_re += 1
        kiem = mau_khac.KIEM.get(m["id"], "khong")
        if m["nguon"]["loai"] == "tu-soan":
            E = {k: float(sp.N(v)) if not isinstance(v, sp.Tuple) else v for k, v in env.items()
                 if not getattr(v, "free_symbols", None)}
            if m["id"] == "MC-NH-01":
                f = core.S("p*x**2+q*x+r", env)
                if sp.simplify(sp.diff(ds, core.x) - f) != 0:
                    bad(f"{it['id']}: đạo hàm nguyên hàm ≠ f")
                n_kiem += 1
            elif m["id"] == "MC-DH-02":
                fx = core.S("x**2+p*x+q", env)
                x0 = env["x0"]
                ok = sp.simplify(ds.subs(core.x, x0) - fx.subs(core.x, x0)) == 0 and \
                    sp.simplify(sp.diff(ds, core.x) - sp.diff(fx, core.x).subs(core.x, x0)) == 0
                if not ok:
                    bad(f"{it['id']}: tiếp tuyến không qua tiếp điểm hoặc sai hệ số góc")
                n_kiem += 1
            elif m["id"] in ("MC-OX-01", "MC-OX-03"):
                coef = [env["p"], env["q"], env["r"]]
                if list(ds) != coef:
                    bad(f"{it['id']}: vectơ ≠ hệ số")
                n_kiem += 1
            elif m["id"] == "MC-OX-02":
                M = (env["x0"], env["y0"], env["z0"])
                if sp.simplify(ds.subs({core.x: M[0], core.y: M[1], core.z: M[2]})) != 0 or \
                        [ds.coeff(s) for s in (core.x, core.y, core.z)] != [env["p"], env["q"], env["r"]]:
                    bad(f"{it['id']}: mặt phẳng không qua M hoặc sai VTPT")
                n_kiem += 1
            elif m["id"] == "MC-OX-04":
                if list(ds) != [env["x2"] - env["x1"], env["y2"] - env["y1"], env["z2"] - env["z1"]]:
                    bad(f"{it['id']}: tọa độ vectơ sai")
                n_kiem += 1
            elif callable(kiem):
                w = kiem(E)
                v = core.num(ds)
                tol = mau_khac.KIEM_TOL.get(m["id"], 1e-6)
                if w is None or abs(w - v) > tol * max(1, abs(v)):
                    bad(f"{it['id']}: kiểm độc lập {w} ≠ {v}")
                n_kiem += 1
            else:
                bad(f"{m['id']}: thiếu phép kiểm độc lập")
        if it["loai_cau"] == "tra-loi-ngan":
            s = it["dap_an"]
            if len(s) > 4:
                bad(f"{it['id']}: đáp án TLN quá 4 ký tự")
            val = it["gia_tri_chinh_xac"]
            if abs(float(s.replace(",", ".")) - val) > 0.5 * 10 ** -(len(s.split(",")[1]) if "," in s else 0) + 1e-9:
                bad(f"{it['id']}: đáp án TLN {s} không phải làm tròn của {val}")
        if it["loai_cau"] == "tn4":
            vals = [c["noi_dung"] for c in it["lua_chon"]]
            if len(set(vals)) != 4 or sum(c["dung"] for c in it["lua_chon"]) != 1 or it["lua_chon"][0]["noi_dung"] != it["dap_an"]:
                bad(f"{it['id']}: phương án trùng hoặc sai số đáp án đúng")
    good(f"đáp số: {n_re} câu tính lại từ hàm đáp số của mẫu; {n_kiem} câu ngoài HHKG qua phép kiểm độc lập")

    # engine
    mvp_items = [it for it in bank if it["nguon"]["loai"] == "mvp"]
    stored = load(os.path.join(HERE, "mau-cau", "engine-values.json"))["gia_tri"]
    jobs = []
    import build
    mvp_specs = json.load(open(build.SPECS, encoding="utf-8"))
    for it in mvp_items:
        if it["loai_cau"] == "dung-sai":
            ds_m = maus[it["mau_cau"]]
            for y, yy in zip(ds_m["y"], it["y"]):
                if "mau" not in y:
                    continue
                sub = maus[y["mau"]]
                env = core.env_of(sub, it["bien_the"])
                v = core.num(core.simp(core.S(sub["dap_so"], env)))
                key = it["id"] + "#" + yy["nhan"]
                jobs.append((key, build.engine_spec(sub, env, mvp_specs), v))
        else:
            m = maus[it["mau_cau"]]
            env = core.env_of(m, it["bien_the"])
            jobs.append((it["id"], build.engine_spec(m, env, mvp_specs), it["dap_so"]["so"]))
    live = None
    if use_engine and shutil.which("javac") and shutil.which("java"):
        import engine
        try:
            live = engine.run([{"id": str(i), "spec": s} for i, (_, s, _) in enumerate(jobs)])
        except Exception as ex:  # noqa: BLE001
            bad(f"không chạy được engine: {ex}")
    for i, (key, spec, v) in enumerate(jobs):
        e = live[str(i)][0] if live else stored.get(key, {}).get("engine")
        if e is None:
            bad(f"{key}: thiếu giá trị engine")
            continue
        if abs(e - v) > 1e-6 * max(1.0, abs(v)):
            bad(f"{key}: engine {e} ≠ đáp số {v}")
        if key in by_id and by_id[key]["dap_so_engine"] is not None and abs(by_id[key]["dap_so_engine"] - e) > 1e-9:
            bad(f"{key}: dap_so_engine lưu {by_id[key]['dap_so_engine']} ≠ engine chạy lại {e}")
        n_eng += 1
    good(f"engine: {n_eng} giá trị câu/ý từ mẫu MVP khớp engine (1e-6) — "
         + ("chạy lại engine thật" if live else "dùng giá trị đã lưu (không chạy lại)"))

    # ---------------------------------------------------------------- 4. nhiễu gắn lỗi
    n_nh, n_ok = 0, 0
    for it in bank:
        for n in it["nhieu"]:
            n_nh += 1
            l_ = n.get("loi") or {}
            n_ok += bool(l_.get("loai_loi") and len(l_.get("mo_ta", "")) >= 5)
        for c in it.get("lua_chon", []):
            if not c["dung"] and not c.get("loi"):
                bad(f"{it['id']}: phương án nhiễu không gắn lỗi")
        for y in it.get("y", []):
            if not y["dung"] and not y.get("loi"):
                bad(f"{it['id']}: ý sai {y['nhan']} không gắn lỗi")
        if it["loai_cau"] == "tn4" and len(it["nhieu"]) != 3:
            bad(f"{it['id']}: TN4 cần 3 nhiễu")
        if it["loai_cau"] == "dung-sai" and len(it["nhieu"]) != sum(1 for y in it["y"] if not y["dung"]):
            bad(f"{it['id']}: số nhiễu ≠ số ý sai")
    for m in maus.values():
        for n in m.get("nhieu", []) + [x for y in m.get("y", []) for x in y.get("nhieu", [])]:
            if not n.get("loi", {}).get("loai_loi"):
                bad(f"mẫu {m['id']}: biểu thức nhiễu không gắn lỗi")
    if n_nh != n_ok:
        bad(f"nhiễu gắn lỗi {n_ok}/{n_nh}")
    good(f"nhiễu: {n_ok}/{n_nh} nhiễu gắn một lỗi (100 %)")

    # ---------------------------------------------------------------- 5. ma trận
    for m in mts.values():
        td = sum(c["diem"] * c["so_cau"] for c in m["cells"])
        if abs(td - m["tong_diem"]) > 1e-9:
            bad(f"{m['id']}: tổng điểm ô {td} ≠ {m['tong_diem']}")
        for p in m["phan"]:
            cs = [c for c in m["cells"] if c["phan"] == p["id"]]
            if sum(c["so_cau"] for c in cs) != p["so_cau"] or any(c["loai_cau"] != p["loai_cau"] or abs(c["diem"] - p["diem_moi_cau"]) > 1e-9 for c in cs):
                bad(f"{m['id']}: phần {p['id']} lệch số câu/loại/điểm")
        for c in m["cells"]:
            if c["loai_o"] == "xoay-vong":
                s = sum(g["trong_so"] for g in c["nhom"])
                if abs(s - 1) > 2e-3:
                    bad(f"{m['id']}/{c['id']}: tổng trọng số {s}")
                if any(not re.search(r"tn-thpt-20\d\d|minh-hoa-2025|TN 2017", g["can_cu"]) for g in c["nhom"]):
                    bad(f"{m['id']}/{c['id']}: thiếu căn cứ trọng số")
    good("ma trận: tổng điểm, số câu từng phần, trọng số xoay vòng (tổng 1, có căn cứ) đạt")

    # ---------------------------------------------------------------- 6. lắp đề (kiểm lại độc lập)
    import lap_de
    bank_by = {it["id"]: it for it in bank}
    for mid, m in mts.items():
        for seed in (2026, 7):
            des, bc = lap_de.lap(m, 3, seed, bank)
            for de in des:
                e = validate(de, SC["de"])
                if e:
                    bad(f"schema đề {mid}/{de['ma_de']}: {e[:3]}")
                kiem_doc_lap(de, m, bank_by, yccd)
            dks = [lap_de.do_kho_de(d["cau"]) for d in des]
            lech = (max(dks) - min(dks)) / (sum(dks) / len(dks))
            if lech > 0.05:
                bad(f"{mid} seed {seed}: độ khó các mã lệch {lech:.3f} > 5 %")
        good(f"lắp đề {mid}: 3 mã × 2 seed, mọi ràng buộc đạt; độ khó lệch {lech * 100:.1f} % (seed 7)")

    # ---------------------------------------------------------------- 7. đề mẫu + README
    md = os.path.join(HERE, "de-mau", "TN-THPT-764", "ma-101.md")
    if not os.path.exists(md) or "Bảng đặc tả" not in open(md, encoding="utf-8").read():
        bad("thiếu đề mẫu thi thử TN de-mau/TN-THPT-764/ma-101.md")
    rd = os.path.join(HERE, "README.md")
    if not os.path.exists(rd) or "Phiên bản: v1.0 · 27/11/2026" not in open(rd, encoding="utf-8").read().splitlines()[0]:
        bad("README.md thiếu dòng phiên bản đầu tệp")
    for f in glob.glob(os.path.join(HERE, "de-mau", "*", "ma-*.json")):
        de = load(f)
        if validate(de, SC["de"]):
            bad(f"schema đề mẫu {f}")
        kiem_doc_lap(de, mts[de["ma_tran"]], bank_by, yccd)
    good("đề mẫu + README có mặt; đề mẫu trong de-mau/ qua kiểm độc lập")

    for s in OK:
        print("✅", s)
    for s in LOI[:60]:
        print("❌", s)
    print(f"\nKẾT QUẢ: {'ĐẠT' if not LOI else 'CHƯA ĐẠT'} ({len(LOI)} lỗi)")
    sys.exit(1 if LOI else 0)


def kiem_doc_lap(de, m, bank_by, yccd):
    tag = f"{m['id']}/{de['ma_de']}/seed{de['seed']}"
    qs = de["cau"]
    cells = {c["id"]: c for c in m["cells"]}
    cnt = Counter(q["o"] for q in qs)
    # phủ đặc tả 100 % + khớp ma trận
    for cid, c in cells.items():
        if cnt[cid] != c["so_cau"]:
            bad(f"{tag}: ô {cid} có {cnt[cid]} câu")
    for q in qs:
        c = cells[q["o"]]
        it = bank_by[q["id_cau"]]
        opts = [(c["chu_de"], c.get("dang_tx"), c["yccd"])] if c["loai_o"] == "co-dinh" else \
            [(g["chu_de"], g.get("dang_tx"), g["yccd"]) for g in c["nhom"]]
        if not any(it["chu_de"] == cd and (not dt or it["dang_tx"] in dt) and set(it["yccd"]) & set(yc) for cd, dt, yc in opts):
            bad(f"{tag}: câu {q['so']} không thuộc đặc tả ô {c['id']}")
        if it["loai_cau"] != c["loai_cau"] or it["muc_do"] != c["muc_do"] or q["phan"] != c["phan"] or abs(q["diem"] - c["diem"]) > 1e-9:
            bad(f"{tag}: câu {q['so']} lệch loại/mức độ/phần/điểm so với ô {c['id']}")
        if not q["yccd"] or any(y not in yccd for y in q["yccd"]):
            bad(f"{tag}: câu {q['so']} thiếu YCCĐ đặc tả")
        if it["loai_cau"] == "tn4":
            if q["phuong_an"][q["dap_an"]] != it["dap_an"] or sorted(q["phuong_an"].values()) != sorted(c_["noi_dung"] for c_ in it["lua_chon"]):
                bad(f"{tag}: câu {q['so']} đáp án/phương án không khớp ngân hàng")
    for p in m["phan"]:
        if sum(1 for q in qs if q["phan"] == p["id"]) != p["so_cau"]:
            bad(f"{tag}: phần {p['id']} sai số câu")
    if [q["so"] for q in qs] != list(range(1, len(qs) + 1)):
        bad(f"{tag}: đánh số câu không liên tục")
    # không trùng dạng
    d = Counter(bank_by[q["id_cau"]]["dang"] for q in qs)
    if any(v > 1 for v in d.values()):
        bad(f"{tag}: trùng dạng {[k for k, v in d.items() if v > 1]}")
    # tổng điểm, thời gian
    if abs(sum(q["diem"] for q in qs) - m["tong_diem"]) > 1e-9:
        bad(f"{tag}: tổng điểm")
    if sum(bank_by[q["id_cau"]]["thoi_gian_phut"] for q in qs) > m["thoi_gian_phut"] + 1e-9:
        bad(f"{tag}: tổng thời gian vượt")
    # đáp án A/B/C/D
    a = Counter(q["dap_an"] for q in qs if q["loai_cau"] == "tn4")
    if a and max(a.get(L, 0) for L in "ABCD") - min(a.get(L, 0) for L in "ABCD") > 1:
        bad(f"{tag}: phân bố đáp án {dict(a)}")
    # không lộ đáp số: không chung bối cảnh; đáp số (biểu thức) không xuất hiện trong đề câu khác
    k = Counter(bank_by[q["id_cau"]]["boi_canh_key"] for q in qs)
    if any(v > 1 for v in k.values()):
        bad(f"{tag}: hai câu chung bối cảnh (lộ dữ kiện/đáp số)")

    def norm(s):
        return re.sub(r"\s+|\\text\{[^}]*\}|\\,|[{}$]", "", s)
    for qi in qs:
        ans = norm(bank_by[qi["id_cau"]]["dap_so"]["gia_tri"])
        if len(ans) < 5 or re.fullmatch(r"[-\d,.]+", ans):
            continue
        for qj in qs:
            if qj is not qi and ans in norm(qj["de_bai"]):
                bad(f"{tag}: đáp số câu {qi['so']} lộ trong câu {qj['so']}")
    # tự luận: thang điểm hướng dẫn chấm bằng điểm ô
    for q in qs:
        if q["loai_cau"] == "tu-luan" and abs(sum(b["diem"] for b in q["huong_dan_cham"]) - q["diem"]) > 1e-6:
            bad(f"{tag}: hướng dẫn chấm câu {q['so']} không cộng đủ điểm")


if __name__ == "__main__":
    main()
