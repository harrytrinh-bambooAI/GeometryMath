# Phiên bản: v1.1 · 2026-11-27 · runner-content-bal
# Tự kiểm độc lập đáp số 38 đề mới: dựng tọa độ bằng Python (không dùng engine Java), đo đại lượng
# theo định nghĩa hình học, so với biểu thức đáp số "py" tại a = 1, 2, 3 (đề không tham số: một lần).
# Chạy: python3 -I verify_new.py   (thoát 1 nếu có đại lượng sai)
import math
import os
import re
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from new_problems import PROBLEMS  # noqa: E402


def param(expr, a):
    # "a√6/2", "2a", "3√2", "a" -> số
    e = re.sub(r'√(\d+)', r'math.sqrt(\1)', expr)
    e = re.sub(r'(\d)a', r'\1*a', e)
    e = re.sub(r'a(math)', r'a*\1', e)
    e = re.sub(r'(\d)(math)', r'\1*\2', e)
    return eval(e, {"math": math, "a": a})


def sub(p, q): return [p[i] - q[i] for i in range(3)]
def dot(p, q): return sum(p[i] * q[i] for i in range(3))
def cross(p, q): return [p[1]*q[2] - p[2]*q[1], p[2]*q[0] - p[0]*q[2], p[0]*q[1] - p[1]*q[0]]
def norm(p): return math.sqrt(dot(p, p))
def dist(p, q): return norm(sub(p, q))


def build(t, P):
    if t in ("pyramid", "pyramid_square_sa"):
        a = P["a"]
        pts = {"A": [0, 0, 0], "B": [a, 0, 0], "C": [a, a, 0], "D": [0, a, 0]}
        if t == "pyramid":
            pts["S"] = [a/2, a/2, P["h"]]
            pts["O"] = [a/2, a/2, 0]
        else:
            pts["S"] = [0, 0, P["sa"]]
        return pts
    if t.startswith("prism"):
        h = P["h"]
        if t == "prism":
            ab = P["a"]
            c = [ab/2, ab*math.sqrt(3)/2]
        else:
            ab = P["ab"]
            c = [0, P["ac"]]
        pts = {}
        for k, (x, y) in {"A": [0, 0], "B": [ab, 0], "C": c}.items():
            pts[k] = [x, y, 0]
            pts[k + "′"] = [x, y, h]
        return pts
    if t in ("box", "cube", "sphere_circum_cube"):
        a = P["a"]
        b = P.get("b", a)
        c = P.get("c", a)
        pts = {}
        for k, (x, y) in {"A": [0, 0], "B": [a, 0], "C": [a, b], "D": [0, b]}.items():
            pts[k] = [x, y, 0]
            pts[k + "′"] = [x, y, c]
        if t == "sphere_circum_cube":
            pts["I"] = [a/2, a/2, a/2]
        return pts
    if t in ("cone", "cone_rl"):
        r = P["r"]
        h = P["h"] if t == "cone" else math.sqrt(P["l"]**2 - r*r)
        return {"S": [0, 0, h], "O": [0, 0, 0], "M": [r, 0, 0]}
    if t == "cylinder":
        return {"O": [0, 0, 0], "O′": [0, 0, P["h"]], "M": [P["r"], 0, 0], "M′": [P["r"], 0, P["h"]]}
    if t == "sphere":
        return {"O": [0, 0, 0], "M": [P["r"], 0, 0]}
    raise ValueError(t)


def tri_area(p, q, r): return norm(cross(sub(q, p), sub(r, p))) / 2
def poly_area(ps): return sum(tri_area(ps[0], ps[i], ps[i+1]) for i in range(1, len(ps) - 1))


def plane_dist(x, p, q, r):
    n = cross(sub(q, p), sub(r, p))
    return abs(dot(sub(x, p), n)) / norm(n)


def circum_r(ps):
    # tâm: giải hệ tuyến tính bằng khử Gauss (độc lập với công thức tích có hướng bên Java)
    p0 = ps[0]
    M = [[2*(p[i] - p0[i]) for i in range(3)] + [dot(p, p) - dot(p0, p0)] for p in ps[1:]]
    for c in range(3):
        piv = max(range(c, 3), key=lambda r: abs(M[r][c]))
        M[c], M[piv] = M[piv], M[c]
        for r in range(3):
            if r != c:
                f = M[r][c] / M[c][c]
                M[r] = [M[r][k] - f*M[c][k] for k in range(4)]
    ctr = [M[i][3] / M[i][i] for i in range(3)]
    rs = [dist(ctr, p) for p in ps]
    assert max(rs) - min(rs) < 1e-9
    return rs[0]


def measure(pts, kind, n):
    q = [pts[x] for x in n]
    if kind == "angle":
        u, v = sub(q[0], q[1]), sub(q[2], q[1])
        return math.degrees(math.acos(dot(u, v) / (norm(u) * norm(v))))
    if kind in ("sphere_radius", "sphere_volume", "sphere_area"):
        R = dist(q[0], q[1]) if len(q) == 2 else circum_r(q)
        return {"sphere_radius": R, "sphere_volume": 4/3*math.pi*R**3, "sphere_area": 4*math.pi*R*R}[kind]
    if kind in ("cyl_lateral", "cyl_volume"):
        h, r = dist(q[0], q[1]), dist(q[0], q[2])
        return 2*math.pi*r*h if kind == "cyl_lateral" else math.pi*r*r*h
    if kind in ("cone_lateral", "cone_volume", "cone_total_area"):
        r, l, h = dist(q[1], q[2]), dist(q[0], q[2]), dist(q[0], q[1])
        return {"cone_lateral": math.pi*r*l, "cone_volume": math.pi*r*r*h/3,
                "cone_total_area": math.pi*r*(r + l)}[kind]
    if kind in ("volume_prism", "pyramid_volume"):
        b = q[:-1]
        v = poly_area(b) * plane_dist(q[-1], *b[:3])
        return v if kind == "volume_prism" else v/3
    if kind == "pyramid_lateral":
        s, b = q[0], q[1:]
        return sum(tri_area(s, b[i], b[(i+1) % len(b)]) for i in range(len(b)))
    if kind == "prism_lateral":
        b = q[:-1]
        per = sum(dist(b[i], b[(i+1) % len(b)]) for i in range(len(b)))
        return per * plane_dist(q[-1], *b[:3])
    raise ValueError(kind)


bad = 0
total = 0
for p in PROBLEMS:
    sp = p["spec"]
    parametric = any('a' in v for v in sp["params"].values())
    rows = []
    for a in ([1, 2, 3] if parametric else [1]):
        P = {k: param(v, a) for k, v in sp["params"].items()}
        pts = build(sp["template"], P)
        for i, q in enumerate(sp["asked"]):
            got = measure(pts, q["kind"], q["points"])
            want = eval(p["py"][i], {"pi": math.pi, "a": a})
            ok = abs(got - want) <= 1e-9 * max(1, abs(want))
            bad += not ok
            total += 1
            rows.append(f"{q['name'] or '-'}@a={a}: {got:.6f}/{want:.6f} {'OK' if ok else 'SAI'}")
    print(p["code"], p["topic"], p["level"], p["solid"], "|", p["answer"], "|", "; ".join(rows))
print(f"Tự kiểm: {total - bad}/{total} giá trị khớp (đề × đại lượng × bộ a), SAI = {bad}")
sys.exit(1 if bad else 0)
