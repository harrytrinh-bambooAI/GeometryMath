"""Phiên bản: v1.0 · 27/11/2026
Lõi sinh câu từ mẫu câu (mau_cau): đánh giá tham số bằng sympy, định dạng LaTeX kiểu Việt Nam,
sinh phương án nhiễu (mỗi nhiễu gắn một lỗi), câu đúng/sai, trả lời ngắn (≤ 4 ký tự), tự luận.
Dùng chung cho build.py, lap_de.py (không) và check.py (tính lại đáp số).
"""
import itertools
import math
import re

import sympy as sp

a = sp.Symbol("a", positive=True)
x, y, z, t = sp.symbols("x y z t", real=True)
GEN = {x: sp.Rational(137, 100), y: sp.Rational(211, 100), z: sp.Rational(-73, 100), t: sp.Rational(53, 100)}
BASE = {"a": a, "x": x, "y": y, "z": z, "Tuple": sp.Tuple, "Max": sp.Max, "Min": sp.Min, "pi": sp.pi, "sqrt": sp.sqrt, "atan": sp.atan, "acos": sp.acos, "asin": sp.asin,
        "log": sp.log, "exp": sp.exp, "Rational": sp.Rational, "binomial": sp.binomial,
        "factorial": sp.factorial, "Abs": sp.Abs, "E": sp.E, "floor": sp.floor, "ceiling": sp.ceiling}

DIEM_MAC_DINH = {"tn4": 0.25, "dung-sai": 1.0, "tra-loi-ngan": 0.5, "tu-luan": 1.0}
# thời gian dự kiến (phút) theo loại câu × mức độ; căn cứ: 90 phút / 22 câu của QĐ 764 phân bổ theo mức độ
THOI_GIAN = {
    "tn4": {"NB": 1.0, "TH": 1.5, "VD": 2.5, "VDC": 3.5},
    "dung-sai": {"NB": 4.0, "TH": 5.0, "VD": 6.0, "VDC": 7.0},
    "tra-loi-ngan": {"NB": 2.0, "TH": 3.0, "VD": 4.0, "VDC": 6.0},
    "tu-luan": {"NB": 4.0, "TH": 6.0, "VD": 8.0, "VDC": 10.0},
}
# độ khó dự kiến (tỉ lệ học sinh trả lời sai, 0..1) theo mức độ
DO_KHO_GOC = {"NB": 0.20, "TH": 0.40, "VD": 0.60, "VDC": 0.80}


def S(expr, env):
    """sympify biểu thức chuỗi trong môi trường tham số."""
    if isinstance(expr, (int, float)):
        return sp.nsimplify(expr)
    loc = dict(BASE)
    loc.update(env)
    return sp.sympify(expr, locals=loc)


def simp(e):
    e = sp.nsimplify(e) if isinstance(e, float) else e
    try:
        e2 = sp.radsimp(sp.simplify(e))
    except Exception:  # noqa: BLE001
        e2 = e
    return e2


def num(e):
    """Giá trị số tại a = 1 (biến x, y, z thay giá trị tổng quát; vectơ: ghép các thành phần)."""
    e = sp.sympify(e)
    if isinstance(e, sp.Tuple):
        return sum(num(c) * (1000.0 ** i) for i, c in enumerate(e))
    return float(sp.N(e.subs(a, 1).subs(GEN), 30))


def env_of(mau, ts):
    """ts: dict tên -> chuỗi giá trị. Trả env sympy gồm tham số + đại lượng dẫn xuất."""
    env = {k: sp.nsimplify(S(v, {}), rational=True) if "." in str(v) else S(v, {}) for k, v in ts.items()}
    for k, v in mau.get("dan_xuat", {}).items():
        env[k] = simp(S(v, env))
    return env


def bien_the(mau):
    """Liệt kê mọi tổ hợp tham số trong miền thỏa ràng buộc (thứ tự ổn định)."""
    keys = list(mau["tham_so"].keys())
    out = []
    for combo in itertools.product(*[mau["tham_so"][k] for k in keys]):
        ts = {}
        for k, c in zip(keys, combo):
            if k.startswith("_bo"):
                ts.update({kk: str(vv) for kk, vv in c.items()})
            else:
                ts[k] = str(c)
        env = env_of(mau, ts)
        def _ok(r):
            v = S(r, env)
            return bool(v) if isinstance(v, bool) else bool(v.subs(a, 1))
        if all(_ok(r) for r in mau.get("rang_buoc", [])):
            out.append(ts)
    return out


def _sqrt_first(s):
    s = re.sub(r"\\sqrt\{(\d+)\} a", r"a\\sqrt{\1}", s)
    s = re.sub(r"\\sqrt\{(\d+)\} \\pi", r"\\sqrt{\1}\\pi", s)
    return s


def dec(x, nd):
    """Số thập phân kiểu Việt Nam, bỏ số 0 thừa."""
    s = f"{round(x + 0.0, nd):.{nd}f}"
    if "." in s:
        s = s.rstrip("0").rstrip(".")
    if s == "-0":
        s = "0"
    return s.replace(".", ",")


def tex(e, kieu="so"):
    """LaTeX một giá trị. kieu: 'goc' (độ), 'so' (mặc định)."""
    e = simp(e)
    if kieu == "goc":
        v = num(e)
        if e.is_Rational or abs(v - round(v)) < 1e-9:
            return f"{int(round(v))}^\\circ"
        return "\\approx " + dec(v, 2).replace(",", "{,}") + "^\\circ"
    if kieu == "nguyen":
        return sp.latex(e).replace("\\frac", "\\dfrac")
    if kieu == "vecto":
        return "(" + ";".join(tex(c) for c in e) + ")"
    if kieu.startswith("xap-xi"):
        nd = int(kieu.split(":")[1])
        return dec(num(e), nd).replace(",", "{,}")
    cands = [e, sp.expand(e)]
    try:
        cands.append(sp.radsimp(sp.expand(e)))
    except Exception:  # noqa: BLE001
        pass
    s = min((sp.latex(c) for c in cands), key=lambda q: (len(q) + (8 if "\\left(" in q else 0)))
    s = s.replace("\\frac", "\\dfrac")
    s = re.sub(r"(\d) (\\pi|\\sqrt|[a-z])", r"\1\2", s)
    m2 = re.fullmatch(r"- ([^+\-]+) \+ ([^+\-]+)", s)
    if m2:
        s = m2.group(2) + " - " + m2.group(1)
    s = _sqrt_first(s)
    s = s.replace("\\log", "\\log")
    return s


def phuong_an(mau, env, kieu):
    """Đáp án đúng + danh sách nhiễu hợp lệ (khác nhau, khác đáp án, cùng dấu nếu yêu cầu dương)."""
    ds = simp(S(mau["dap_so"], env))
    v0 = num(ds)
    t0 = tex(ds, kieu)
    seen_v = [v0]
    seen_t = {t0}
    nh = []
    for n in mau["nhieu"]:
        e = simp(S(n["bieu_thuc"], env))
        try:
            v = num(e)
        except Exception:  # noqa: BLE001
            continue
        if not math.isfinite(v) or (mau.get("duong", True) and v <= 0):
            continue
        t = tex(e, kieu)
        if any(abs(v - w) < 1e-6 * max(1, abs(w)) for w in seen_v) or t in seen_t:
            continue
        seen_v.append(v)
        seen_t.add(t)
        nh.append({"phuong_an": t, "gia_tri": v, "loi": n["loi"]})
    return ds, v0, t0, nh


def tln_chuoi(v, nd):
    """Chuỗi trả lời ngắn theo phiếu QĐ 764: tối đa 4 ký tự (dấu -, chữ số, dấu phẩy)."""
    s = dec(v, nd)
    return s if len(s) <= 4 else None


def render(tpl, env, kieu_map=None):
    """Thay {ten} bằng LaTeX của env[ten]; {ten:goc} theo kiểu góc; {ten:xap-xi:2}."""
    def rep(m):
        name, _, k = m.group(1).partition(":")
        if name not in env:
            return m.group(0)
        return tex(env[name], k or (kieu_map or {}).get(name, "so"))
    return re.sub(r"\{([A-Za-z_][A-Za-z0-9_]*(?::[a-z\-]+(?::\d)?)?)\}", rep, tpl)


def boi_canh_key(mau, ts):
    return mau.get("ho_hinh", mau["id"]) + "|" + ",".join(f"{k}={ts[k]}" for k in sorted(ts))
