"""Phiên bản: v1.0 · 27/11/2026
Bộ lắp đề: ma trận + số mã đề + seed → các mã đề tương đương (đề + đáp án + bảng đặc tả, JSON + Markdown).

  python3 lap_de.py ma-tran/TN-THPT-764.json --so-ma 4 --seed 2026 [--out de-mau/TN-THPT-764]

Quy trình mỗi mã: bốc chủ đề cho ô xoay vòng theo trọng số → điền từng ô bằng câu trong ngân hàng (đúng chủ đề,
loại câu, mức độ, YCCĐ của ô; ưu tiên câu chưa dùng ở mã trước) → kiểm ràng buộc; lỗi thì bốc lại.
Ràng buộc: phủ đặc tả 100 %, khớp ma trận, không trùng dạng, tổng điểm = ma trận, tổng thời gian ≤ thời gian làm bài,
đáp án A/B/C/D lệch ≤ 1 câu, không câu nào lộ đáp số câu khác, độ khó dự kiến các mã lệch ≤ 5 %.
"""
import argparse
import glob
import json
import os
import random
import re
from collections import Counter

HERE = os.path.dirname(os.path.abspath(__file__))
PB = "v1.0 · 27/11/2026"
LETTERS = "ABCD"
SO_LAN_THU = 600


def load_bank(root=HERE):
    cau = []
    for f in sorted(glob.glob(os.path.join(root, "ngan-hang", "*.json"))):
        cau += json.load(open(f, encoding="utf-8"))["cau"]
    return cau


def lua_chon_o(cell):
    """Các phương án điền ô: list (chu_de, dang_tx|None, yccd, trong_so)."""
    if cell["loai_o"] == "co-dinh":
        return [(cell["chu_de"], cell.get("dang_tx"), cell["yccd"], 1.0)]
    return [(g["chu_de"], g.get("dang_tx"), g["yccd"], g["trong_so"]) for g in cell["nhom"]]


def hop_le(it, cell, cd, dt, yc):
    return (it["chu_de"] == cd and it["loai_cau"] == cell["loai_cau"] and it["muc_do"] == cell["muc_do"]
            and (not dt or it["dang_tx"] in dt) and bool(set(it["yccd"]) & set(yc)))


def do_kho_de(qs):
    tong = sum(q["diem"] for q in qs)
    return sum(q["diem"] * q["do_kho"] for q in qs) / tong


def _norm(s):
    return re.sub(r"\s+|\\text\{[^}]*\}|\\,|[{}$]", "", s or "")


def kiem_de(de, mt):
    """Kiểm một mã đề theo ma trận. Trả dict tên ràng buộc -> (đạt, chi tiết)."""
    qs = de["cau"]
    cells = {c["id"]: c for c in mt["cells"]}
    r = {}
    # phủ đặc tả + khớp ma trận
    loi = []
    dem = Counter(q["o"] for q in qs)
    for cid, c in cells.items():
        if dem[cid] != c["so_cau"]:
            loi.append(f"ô {cid}: {dem[cid]}/{c['so_cau']} câu")
    for q in qs:
        c = cells.get(q["o"])
        if c is None:
            loi.append(f"câu {q['so']}: ô lạ")
            continue
        ok = any(hop_le(q, c, cd, dt, yc) for cd, dt, yc, _ in lua_chon_o(c))
        if not ok or abs(q["diem"] - c["diem"]) > 1e-9 or q["phan"] != c["phan"]:
            loi.append(f"câu {q['so']}: không khớp ô {c['id']}")
    phu = sum(1 for cid, c in cells.items() if dem[cid] == c["so_cau"]) / len(cells)
    r["phu_dac_ta_100"] = (phu == 1.0 and not loi, f"phủ {phu * 100:.0f}% ô; " + ("; ".join(loi) or "khớp"))
    by_p = Counter(q["phan"] for q in qs)
    sai_p = [p["id"] for p in mt["phan"] if by_p[p["id"]] != p["so_cau"]]
    r["khop_ma_tran"] = (not loi and not sai_p, "khớp số câu từng phần" if not sai_p else "lệch phần " + ",".join(sai_p))
    d = Counter(q["dang"] for q in qs)
    trung = [k for k, v in d.items() if v > 1]
    r["khong_trung_dang"] = (not trung, ",".join(trung) or "không trùng")
    td = round(sum(q["diem"] for q in qs), 6)
    r["tong_diem"] = (abs(td - mt["tong_diem"]) < 1e-9, f"{td} / {mt['tong_diem']}")
    tt = round(sum(q["thoi_gian_phut"] for q in qs), 2)
    r["tong_thoi_gian"] = (tt <= mt["thoi_gian_phut"], f"{tt} phút dự kiến / {mt['thoi_gian_phut']} phút")
    ans = Counter(q["dap_an"] for q in qs if q["loai_cau"] == "tn4")
    if ans:
        vals = [ans.get(L, 0) for L in LETTERS]
        r["can_bang_dap_an"] = (max(vals) - min(vals) <= 1, dict(zip(LETTERS, vals)))
    else:
        r["can_bang_dap_an"] = (True, "không có câu TN4")
    lo = []
    keys = Counter(q["boi_canh_key"] for q in qs)
    lo += [f"chung bối cảnh {k}" for k, v in keys.items() if v > 1]
    for qi in qs:
        a = _norm(qi.get("dap_so_hien", ""))
        if len(a) < 5 or re.fullmatch(r"[-\d,.]+", a):
            continue
        for qj in qs:
            if qj is not qi and a in _norm(qj["de_bai"]):
                lo.append(f"đáp số câu {qi['so']} xuất hiện trong đề câu {qj['so']}")
    r["khong_lo_dap_so"] = (not lo, "; ".join(lo) or "không lộ")
    return r


def lap_mot(mt, bank, rng, dung_roi, d_ref=None):
    cells = mt["cells"]
    for lan in range(SO_LAN_THU):
        chon = {}
        for c in cells:
            opts = lua_chon_o(c)
            opts = [o for o in opts if any(hop_le(it, c, o[0], o[1], o[2]) for it in bank)]
            if not opts:
                raise SystemExit(f"ô {c['id']} không có câu nào trong ngân hàng")
            chon[c["id"]] = rng.choices(opts, weights=[o[3] for o in opts])[0]
        cand = {c["id"]: [it for it in bank if hop_le(it, c, *chon[c["id"]][:3])] for c in cells}
        order = sorted(cells, key=lambda c: len(cand[c["id"]]))
        dang, ctx, mau, ho, picked, ok = set(), set(), set(), set(), {}, True
        for c in order:
            pool = [it for it in cand[c["id"]] if it["dang"] not in dang and it["boi_canh_key"] not in ctx
                    and it["mau_cau"] not in mau]
            moi = [it for it in pool if it["id"] not in dung_roi]
            pool = moi or pool
            khac_ho = [it for it in pool if it["boi_canh_key"].split("|")[0] not in ho]
            pool = khac_ho or pool  # ưu tiên hình/bối cảnh khác họ để đề đa dạng
            if not pool:
                ok = False
                break
            it = rng.choice(pool)
            picked[c["id"]] = it
            dang.add(it["dang"])
            ctx.add(it["boi_canh_key"])
            mau.add(it["mau_cau"])
            ho.add(it["boi_canh_key"].split("|")[0])
        if not ok:
            continue
        de = dung_de(mt, cells, picked, chon, rng)
        r = kiem_de(de, mt)
        if not all(v[0] for v in r.values()):
            continue
        dk = do_kho_de(de["cau"])
        if d_ref is not None and abs(dk - d_ref) / d_ref > 0.025:
            continue
        de["do_kho_du_kien"] = round(dk, 4)
        de["rang_buoc"] = {k: {"dat": v[0], "chi_tiet": v[1]} for k, v in r.items()}
        de["so_lan_boc"] = lan + 1
        return de
    raise SystemExit(f"không lắp được mã đề sau {SO_LAN_THU} lần bốc (ma trận {mt['id']})")


def dung_de(mt, cells, picked, chon, rng):
    tn = [c["id"] for c in cells if c["loai_cau"] == "tn4"]
    n = len(tn)
    letters = list(LETTERS) * (n // 4) + rng.sample(LETTERS, n % 4)
    rng.shuffle(letters)
    pos = dict(zip(tn, letters))
    qs = []
    phan_order = [p["id"] for p in mt["phan"]]
    by_phan = {p: [c for c in cells if c["phan"] == p] for p in phan_order}
    so = 0
    for p in phan_order:
        cs = by_phan[p][:]
        rng.shuffle(cs)  # đảo thứ tự câu trong phần
        for c in cs:
            it = picked[c["id"]]
            so += 1
            q = {"so": so, "phan": p, "o": c["id"], "loai_o": c["loai_o"], "id_cau": it["id"], "mau_cau": it["mau_cau"],
                 "chu_de": it["chu_de"], "dang": it["dang"], "dang_tx": it["dang_tx"],
                 "yccd": sorted(set(it["yccd"]) & set(chon[c["id"]][2])), "muc_do": it["muc_do"],
                 "loai_cau": it["loai_cau"], "nang_luc": it["nang_luc"], "diem": c["diem"],
                 "thoi_gian_phut": it["thoi_gian_phut"], "do_kho": it["do_kho"], "boi_canh_key": it["boi_canh_key"],
                 "de_bai": it["de_bai"], "dap_so_hien": it["dap_so"]["gia_tri"], "nguon": it["nguon"],
                 "dap_so_engine": it.get("dap_so_engine")}
            if it["loai_cau"] == "tn4":
                L = pos[c["id"]]
                sai = it["lua_chon"][1:]
                rng.shuffle(sai)
                pa, nh, k = {}, [], 0
                for X in LETTERS:
                    if X == L:
                        pa[X] = it["lua_chon"][0]["noi_dung"]
                    else:
                        pa[X] = sai[k]["noi_dung"]
                        nh.append({"phuong_an": X, "noi_dung": sai[k]["noi_dung"], "loi": sai[k]["loi"]})
                        k += 1
                q.update(phuong_an=pa, dap_an=L, nhieu=nh)
            elif it["loai_cau"] == "dung-sai":
                q.update(y=[{k: y[k] for k in ("nhan", "menh_de", "dung", "muc_do", "loi") if k in y} for y in it["y"]],
                         dap_an=it["dap_an"], nhieu=it["nhieu"])
            elif it["loai_cau"] == "tra-loi-ngan":
                q.update(dap_an=it["dap_an"])
            else:
                f = c["diem"] / sum(b["diem"] for b in it["huong_dan_cham"])
                q.update(dap_an=it["dap_an"],
                         huong_dan_cham=[dict(b, diem=round(b["diem"] * f, 4)) for b in it["huong_dan_cham"]])
            qs.append(q)
    return {"ma_tran": mt["id"], "ten": mt["ten"], "thoi_gian_phut": mt["thoi_gian_phut"], "tong_diem": mt["tong_diem"],
            "phan": mt["phan"], "cau": qs, "chu_de_xoay_vong": {cid: chon[cid][0] for cid in chon
                                                                 if next(c for c in cells if c["id"] == cid)["loai_o"] == "xoay-vong"}}


def lap(mt, so_ma, seed, bank=None):
    bank = bank if bank is not None else load_bank()
    rng = random.Random(seed)
    des, dung_roi, d_ref = [], set(), None
    for k in range(so_ma):
        de = lap_mot(mt, bank, rng, dung_roi, d_ref)
        de["ma_de"] = str(101 + k)
        de["seed"] = seed
        if d_ref is None:
            d_ref = de["do_kho_du_kien"]
        dung_roi |= {q["id_cau"] for q in de["cau"]}
        des.append(de)
    dks = [d["do_kho_du_kien"] for d in des]
    lech = (max(dks) - min(dks)) / (sum(dks) / len(dks))
    bao_cao = {"ma_tran": mt["id"], "so_ma": so_ma, "seed": seed, "do_kho": dks, "lech_do_kho": round(lech, 4),
               "dat_do_kho_5pt": lech <= 0.05,
               "cau_dung_lai_giua_cac_ma": sum(v - 1 for v in Counter(q["id_cau"] for d in des for q in d["cau"]).values() if v > 1),
               "rang_buoc_tung_ma": {d["ma_de"]: {k: v["dat"] for k, v in d["rang_buoc"].items()} for d in des}}
    return des, bao_cao


# ------------------------------------------------------------------ Markdown
def md_de(de, mt, yccd):
    L = [f"# {de['ten']}", "", f"**Mã đề {de['ma_de']}** · Thời gian làm bài {de['thoi_gian_phut']} phút · Thang điểm {de['tong_diem']:g} · "
         f"seed {de['seed']} · ma trận `{de['ma_tran']}` ({PB})", ""]
    for p in de["phan"]:
        L += [f"## {p['ten']} ({p['so_cau']} câu × {p['diem_moi_cau']:g} điểm)", ""]
        for q in [q for q in de["cau"] if q["phan"] == p["id"]]:
            L.append(f"**Câu {q['so']}.** {q['de_bai']}")
            L.append("")
            if q["loai_cau"] == "tn4":
                for X in LETTERS:
                    L.append(f"- **{X}.** ${q['phuong_an'][X]}$")
            elif q["loai_cau"] == "dung-sai":
                for y in q["y"]:
                    L.append(f"- {y['nhan']}) {y['menh_de']}")
            L.append("")
    L += ["---", "", "## Đáp án", "", "| Câu | Phần | Đáp án |", "|---|---|---|"]
    for q in de["cau"]:
        a = q["dap_an"] if q["loai_cau"] != "tu-luan" else f"${q['dap_an']}$ (xem hướng dẫn chấm)"
        L.append(f"| {q['so']} | {q['phan']} | {a} |")
    tl = [q for q in de["cau"] if q["loai_cau"] == "tu-luan"]
    if tl:
        L += ["", "### Hướng dẫn chấm tự luận", ""]
        for q in tl:
            L.append(f"**Câu {q['so']}** ({q['diem']:g} điểm)")
            for b in q["huong_dan_cham"]:
                L.append(f"- {b['buoc']} ({b['diem']:g} đ): {b['noi_dung']}")
            L.append("")
    L += ["", "## Bảng đặc tả", "", "| Câu | Ô ma trận | Loại ô | Chủ đề | Dạng | YCCĐ | Mức độ | Loại câu | Điểm | Mẫu câu | Nguồn |",
          "|---|---|---|---|---|---|---|---|---|---|---|"]
    for q in de["cau"]:
        L.append(f"| {q['so']} | {q['o']} | {q['loai_o']} | {q['chu_de']} | {q['dang']} | {', '.join(q['yccd'])} | {q['muc_do']} | "
                 f"{q['loai_cau']} | {q['diem']:g} | {q['mau_cau']} | {q['nguon']['loai']} {q['nguon']['ma']} |")
    L += ["", "YCCĐ dùng trong đề:", ""]
    for code in sorted({c for q in de["cau"] for c in q["yccd"]}):
        L.append(f"- `{code}`: {yccd.get(code, '')}")
    L += ["", "## Phương án nhiễu (mỗi nhiễu gắn một lỗi)", ""]
    for q in de["cau"]:
        for n in q.get("nhieu", []):
            ten = n.get("phuong_an", "")
            ten = ten if len(ten) <= 3 else ten.split(")")[0] + ")"
            L.append(f"- Câu {q['so']} {ten}: `{n['loi']['loai_loi']}` — {n['loi']['mo_ta']}")
    L += ["", "## Ràng buộc đã kiểm", "", "| Ràng buộc | Đạt | Chi tiết |", "|---|---|---|"]
    for k, v in de["rang_buoc"].items():
        L.append(f"| {k} | {'✅' if v['dat'] else '❌'} | {v['chi_tiet']} |")
    L.append(f"| do_kho_du_kien | – | {de['do_kho_du_kien']} (tỉ lệ sai dự kiến, trung bình theo điểm) |")
    L.append("")
    return "\n".join(L)


def main():
    ap = argparse.ArgumentParser(description="Lắp đề từ ma trận")
    ap.add_argument("ma_tran")
    ap.add_argument("--so-ma", type=int, default=4)
    ap.add_argument("--seed", type=int, default=2026)
    ap.add_argument("--out", default=None)
    a = ap.parse_args()
    mt = json.load(open(a.ma_tran, encoding="utf-8"))
    yccd = json.load(open(os.path.join(HERE, "ma-tran", "yccd.json"), encoding="utf-8"))["yccd"]
    des, bc = lap(mt, a.so_ma, a.seed)
    out = a.out or os.path.join(HERE, "de-mau", mt["id"])
    os.makedirs(out, exist_ok=True)
    for de in des:
        json.dump(de, open(os.path.join(out, f"ma-{de['ma_de']}.json"), "w", encoding="utf-8"), ensure_ascii=False, indent=1)
        open(os.path.join(out, f"ma-{de['ma_de']}.md"), "w", encoding="utf-8").write(md_de(de, mt, yccd))
    json.dump(bc, open(os.path.join(out, "bao-cao.json"), "w", encoding="utf-8"), ensure_ascii=False, indent=1)
    print(json.dumps({k: bc[k] for k in ("ma_tran", "so_ma", "do_kho", "lech_do_kho", "dat_do_kho_5pt", "cau_dung_lai_giua_cac_ma")},
                     ensure_ascii=False))


if __name__ == "__main__":
    main()
