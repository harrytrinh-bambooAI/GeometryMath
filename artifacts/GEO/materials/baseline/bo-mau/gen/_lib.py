"""Thư viện nhỏ để sinh items/BM-NNN.json (bộ mẫu baseline kiểm AI).

Quy ước điểm cho lời giải sai (diem_dung): mặc định = tổng điểm các bước TRƯỚC bước sai
(bước sai và các bước phụ thuộc kết quả của nó không được điểm). Khi bước sau độc lập với
bước sai hoặc lỗi chỉ là thiếu lập luận, điểm được ghi tay (tham số dd) và giải thích trong mo_ta.
"""
from math import isclose


class S:
    """Một bước lời giải chuẩn."""

    def __init__(self, noi, kq, kt, d, tc=None, ck=None, loi=None, so=None):
        self.noi, self.kq, self.kt, self.d = noi, kq, kt, d
        self.tc = tc or f"Có lập luận đúng và nêu được: {kq}"
        self.ck = ck or []
        self.loi = loi or []
        self.so = so


class W:
    """Một lời giải sai: lỗi cài ở bước `buoc`; rep thay nội dung các bước (theo mã)."""

    def __init__(self, buoc, loai, mo_ta, rep, dd=None):
        self.buoc, self.loai, self.mo_ta, self.rep, self.dd = buoc, loai, mo_ta, rep, dd


def build(id, nguon, cap, chu_de, muc_do, de_bai, gia_tri, so, engine, kien_thuc, steps,
          wrongs, bieu_diem="tu-soan", ghi_chu=""):
    buoc = []
    for i, s in enumerate(steps, 1):
        b = {"ma": f"B{i}", "noi_dung": s.noi, "ket_qua_trung_gian": s.kq, "kien_thuc": s.kt,
             "diem": s.d, "tieu_chi_dat": s.tc, "cach_khac": list(s.ck),
             "loi_thuong_gap": [{"mo_ta": m, "tru": t} for m, t in s.loi]}
        if s.so is not None:
            b["ket_qua_so"] = s.so
        buoc.append(b)
    codes = [b["ma"] for b in buoc]
    sai = []
    for j, w in enumerate(wrongs, 1):
        assert w.buoc in codes, (id, w.buoc)
        for k in w.rep:
            assert k in codes, (id, k)
        lines = [f"{b['ma']}: {w.rep.get(b['ma'], b['noi_dung'])}" for b in buoc]
        idx = codes.index(w.buoc)
        dd = w.dd if w.dd is not None else sum(b["diem"] for b in buoc[:idx])
        sai.append({"id": f"S{j}", "buoc_sai": w.buoc, "loai_loi": w.loai, "mo_ta": w.mo_ta,
                    "loi_giai": "\n".join(lines), "diem_dung": round(dd, 4)})
        # lỗi cài cũng là lỗi thường gặp của bước đó
        st = buoc[idx]
        if not any(l["mo_ta"] == w.mo_ta for l in st["loi_thuong_gap"]):
            st["loi_thuong_gap"].append({"mo_ta": w.mo_ta, "tru": st["diem"]})
    tong = round(sum(b["diem"] for b in buoc), 4)
    return {"id": id, "nguon": nguon, "cap": cap, "chu_de": chu_de, "muc_do": muc_do,
            "de_bai": de_bai, "dap_so": {"gia_tri": gia_tri, "so": so},
            "dap_so_engine": engine, "kien_thuc": kien_thuc, "tong_diem": tong,
            "bieu_diem": bieu_diem, "buoc": buoc, "loi_giai_sai": sai,
            "trang_thai": "nhap", "ghi_chu": ghi_chu}
