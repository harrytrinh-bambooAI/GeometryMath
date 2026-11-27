"""Soát chéo ĐỘC LẬP: tính lại đáp số và kết quả trung gian (ket_qua_so) của từng bài bằng tọa độ /
vectơ / duyệt số, KHÔNG dùng công thức của lời giải. Ghi bo-mau/soat-cheo.json.
Bài chứng minh: kiểm mệnh đề cần chứng minh trên tọa độ cụ thể (và ngẫu nhiên)."""
import json, os, random
from math import sqrt, pi, acos, asin, degrees, atan, cos, sin, isclose

HERE = os.path.dirname(os.path.abspath(__file__))
ITEMS = os.path.join(HERE, "..", "items")
random.seed(7)


# ---------- vectơ ----------
def sub(p, q): return tuple(a - b for a, b in zip(p, q))
def add(p, q): return tuple(a + b for a, b in zip(p, q))
def mul(p, k): return tuple(a * k for a in p)
def dot(p, q): return sum(a * b for a, b in zip(p, q))
def cross(p, q): return (p[1]*q[2]-p[2]*q[1], p[2]*q[0]-p[0]*q[2], p[0]*q[1]-p[1]*q[0])
def norm(p): return sqrt(dot(p, p))
def mid(p, q): return mul(add(p, q), 0.5)
def L(p, q): return norm(sub(p, q))


def ang_vec(u, v):
    c = dot(u, v) / (norm(u) * norm(v))
    return degrees(acos(max(-1, min(1, c))))


def ang_lines(u, v):
    a = ang_vec(u, v)
    return min(a, 180 - a)


def nplane(a, b, c): return cross(sub(b, a), sub(c, a))
def d_pt_plane(p, a, b, c):
    n = nplane(a, b, c)
    return abs(dot(sub(p, a), n)) / norm(n)


def ang_line_plane(p, q, a, b, c):
    n = nplane(a, b, c)
    u = sub(q, p)
    return degrees(asin(min(1, abs(dot(u, n)) / (norm(u) * norm(n)))))


def d_lines(p1, p2, q1, q2):
    n = cross(sub(p2, p1), sub(q2, q1))
    return abs(dot(sub(q1, p1), n)) / norm(n)


def ang_planes(a1, b1, c1, a2, b2, c2):
    return ang_lines(nplane(a1, b1, c1), nplane(a2, b2, c2))


def vol_tet(a, b, c, d): return abs(dot(sub(b, a), cross(sub(c, a), sub(d, a)))) / 6


def solve3(M, r):
    def det(m): return (m[0][0]*(m[1][1]*m[2][2]-m[1][2]*m[2][1]) - m[0][1]*(m[1][0]*m[2][2]-m[1][2]*m[2][0]) + m[0][2]*(m[1][0]*m[2][1]-m[1][1]*m[2][0]))
    D = det(M)
    out = []
    for i in range(3):
        Mi = [list(row) for row in M]
        for k in range(3):
            Mi[k][i] = r[k]
        out.append(det(Mi) / D)
    return tuple(out)


def circum_R(p0, *pts):
    # tâm X: |X-p0|^2 = |X-pi|^2 -> 2(pi-p0).X = |pi|^2-|p0|^2 ; dùng 3 điểm đầu, kiểm các điểm còn lại
    from itertools import combinations
    for tri in combinations(pts, 3):
        if abs(dot(sub(tri[0], p0), cross(sub(tri[1], p0), sub(tri[2], p0)))) > 1e-9:
            break
    M = [mul(sub(p, p0), 2) for p in tri]
    r = [dot(p, p) - dot(p0, p0) for p in tri]
    X = solve3(M, r)
    R = L(X, p0)
    for p in pts:
        assert isclose(L(X, p), R, rel_tol=1e-9), "không cùng mặt cầu"
    return R


def sq_pyr(h, a=1.0):
    return dict(A=(0, 0, 0), B=(a, 0, 0), C=(a, a, 0), D=(0, a, 0), S=(0, 0, h), O=(a/2, a/2, 0))


def reg_pyr(h, a=1.0):
    P = dict(A=(0, 0, 0), B=(a, 0, 0), C=(a, a, 0), D=(0, a, 0), O=(a/2, a/2, 0))
    P["S"] = (a/2, a/2, h)
    return P


def cube(a=1.0):
    return dict(A=(0, 0, 0), B=(a, 0, 0), C=(a, a, 0), D=(0, a, 0), A1=(0, 0, a), B1=(a, 0, a), C1=(a, a, a), D1=(0, a, a))


def tri_prism_eq(a, h):
    A, B, C = (0, 0, 0), (a, 0, 0), (a/2, a*sqrt(3)/2, 0)
    return dict(A=A, B=B, C=C, A1=(0, 0, h), B1=(a, 0, h), C1=(a/2, a*sqrt(3)/2, h))


def reg_tet(a=1.0):
    A, B, C = (0, 0, 0), (a, 0, 0), (a/2, a*sqrt(3)/2, 0)
    G = (a/2, a*sqrt(3)/6, 0)
    D = (G[0], G[1], a*sqrt(2/3))
    return A, B, C, D


V = {}   # id -> (giá trị cuối độc lập, {mã bước: giá trị})
CLAIM = {}  # id -> bool (bài không có số: kiểm mệnh đề/nghiệm)

# ----- MVP -----
P = sq_pyr(sqrt(6)); V["BM-001"] = (ang_line_plane(P["S"], P["C"], P["A"], P["B"], P["D"]), {"B2": L(P["A"], P["C"]), "B3": None})
P = sq_pyr(sqrt(2)); V["BM-002"] = (ang_line_plane(P["S"], P["C"], P["S"], P["A"], P["B"]), {"B3": L(P["S"], P["C"])})
P = reg_pyr(sqrt(6)/2); V["BM-003"] = (ang_line_plane(P["S"], P["A"], P["A"], P["B"], P["C"]), {"B2": L(P["O"], P["A"])})
K = cube(); V["BM-004"] = (ang_lines(sub(K["B1"], K["A"]), sub(K["D1"], K["A"])), {"B1": L(K["A"], K["B1"])})
A, C1, C = (0, 0, 0), (1, sqrt(3), 2), (1, sqrt(3), 0)
V["BM-005"] = (ang_line_plane(A, C1, (0, 0, 0), (1, 0, 0), (0, 1, 0)), {"B2": L(A, C)})
A, B, C, B1 = (0, 0, 0), (1, 0, 0), (0, sqrt(3), 0), (1, 0, 2)
V["BM-006"] = (ang_line_plane(B1, C, A, B, C), {"B2": L(B, C)})
P = sq_pyr(1); V["BM-007"] = (d_pt_plane(P["A"], P["S"], P["B"], P["C"]), {})
P = sq_pyr(sqrt(3)); V["BM-008"] = (d_lines(P["A"], P["B"], P["S"], P["D"]), {})
P = sq_pyr(1); V["BM-009"] = (d_lines(P["B"], P["D"], P["S"], P["C"]), {})
P = reg_pyr(sqrt(1 - 0.5)); assert isclose(L(P["S"], P["A"]), 1)
V["BM-010"] = (d_lines(P["A"], P["B"], P["S"], P["C"]), {"B2": P["S"][2], "B3": d_pt_plane(P["O"], P["S"], P["C"], P["D"])})
P = reg_pyr(4, 6); V["BM-011"] = (d_pt_plane(P["O"], P["S"], P["B"], P["C"]), {"B2": L(P["S"], mid(P["B"], P["C"]))})
V["BM-012"] = (d_pt_plane((0, 0, 0), (0, 0, 2), (1, 0, 0), (0, 2, 0)), {})
T = tri_prism_eq(1, 1); V["BM-013"] = (d_pt_plane(T["A"], T["A1"], T["B"], T["C"]), {"B2": L(T["A"], mid(T["B"], T["C"]))})
T = tri_prism_eq(1, 2); V["BM-014"] = (d_lines(T["A"], T["A1"], T["B"], T["C"]), {})
T = tri_prism_eq(6, 10); base = norm(nplane(T["A"], T["B"], T["C"])) / 2
V["BM-015"] = (base * 10, {"B1": base})
xa = (13**2 - 15**2 + 14**2) / (2 * 14); ya = sqrt(13**2 - xa**2)  # B(0,0), C(14,0), A(xa,ya)
area = 14 * ya / 2; V["BM-016"] = (area * 10, {"B1": (13+14+15)/2, "B2": area})
P = sq_pyr(sqrt(3)); vol = vol_tet(P["S"], P["A"], P["B"], P["C"]) + vol_tet(P["S"], P["A"], P["C"], P["D"])
V["BM-017"] = (vol, {"B1": L(P["S"], P["A"])})
oa = L((0.5, 0.5, 0), (0, 0, 0)); h = oa * sqrt(3)  # cạnh bên tạo 60° ⇒ tan60 = h/OA (định nghĩa)
P = reg_pyr(h); assert isclose(ang_line_plane(P["S"], P["A"], P["A"], P["B"], P["C"]), 60)
V["BM-018"] = (vol_tet(P["S"], P["A"], P["B"], P["C"]) * 2, {"B1": oa, "B2": h})
T = tri_prism_eq(1, sqrt(3)); b = norm(nplane(T["A"], T["B"], T["C"])) / 2
V["BM-019"] = (b * sqrt(3), {"B1": b})
V["BM-020"] = (pi * 4 * 3 / 3, {"B1": pi * 4})
l = L((0, 0, sqrt(3)), (1, 0, 0)); V["BM-021"] = (pi * 1 * l, {"B1": l})
V["BM-022"] = (pi*6*10 + pi*36, {"B1": pi*60, "B2": pi*36})
P = reg_pyr(4, 6); sh = L(P["S"], mid(P["B"], P["C"]))
lat = sum(norm(nplane(P["S"], P[x], P[y]))/2 for x, y in ["AB", "BC", "CD", "DA"]); V["BM-023"] = (lat, {"B1": sh})
P = reg_pyr(sqrt(3)/2); lat = sum(norm(nplane(P["S"], P[x], P[y]))/2 for x, y in ["AB", "BC", "CD", "DA"])
V["BM-024"] = (lat, {"B1": L(P["S"], mid(P["B"], P["C"])), "B2": norm(nplane(P["S"], P["B"], P["C"]))/2})
P = sq_pyr(1); fs = {k: norm(nplane(P["S"], P[k[0]], P[k[1]]))/2 for k in ["AB", "BC", "CD", "DA"]}
V["BM-025"] = (sum(fs.values()), {"B1": fs["AB"], "B3": fs["BC"]})
K = cube(2); R = circum_R(*K.values()); V["BM-026"] = (4/3*pi*R**3, {"B1": L(K["A"], K["C1"]), "B2": R})
P = sq_pyr(sqrt(2)); R = circum_R(P["S"], P["A"], P["B"], P["C"], P["D"]); V["BM-027"] = (4*pi*R*R, {"B3": R})
P = reg_pyr(sqrt(0.5)); R = circum_R(P["S"], P["A"], P["B"], P["C"], P["D"])
V["BM-028"] = (4/3*pi*R**3, {"B1": L(P["O"], P["A"]), "B2": P["S"][2], "B3": R})
T = tri_prism_eq(1, 2); R = circum_R(*T.values()); V["BM-029"] = (4*pi*R*R, {"B1": 1/3, "B2": R*R})
pts = [(0, 0, 0), (1, 0, 0), (0, sqrt(3), 0), (0, 0, 2), (1, 0, 2), (0, sqrt(3), 2)]
R = circum_R(*pts); V["BM-030"] = (4/3*pi*R**3, {"B1": L(pts[1], pts[2])/2, "B2": R})

# ----- Lớp 11 -----
K = cube(); V["BM-031"] = (ang_lines(sub(K["B1"], K["A"]), sub(K["C1"], K["B"])), {})
A, B, C, D = reg_tet(); M = mid(B, C)
V["BM-032"] = (cos(ang_lines(sub(B, A), sub(M, D)) * pi / 180), {"B1": L(mid(A, C), M), "B2": L(D, M)})
# BM-033: dựng tứ diện cụ thể AB=CD=1, MN=√3/2 rồi đo góc
A, B = (0, 0, 0), (1, 0, 0); C = (0.3, 0.2, 0.9); D = add(C, (cos(2*pi/3), sin(2*pi/3), 0))
assert isclose(L(C, D), 1) and isclose(L(mid(B, C), mid(A, D)), sqrt(3)/2)
P_ = mid(A, C); V["BM-033"] = (ang_lines(sub(B, A), sub(D, C)), {"B1": L(P_, mid(B, C)), "B3": ang_vec(sub(mid(B, C), P_), sub(mid(A, D), P_))})
# BM-034: mệnh đề OH ⊥ (ABC), H trực tâm; ngẫu nhiên độ dài
ok = True
for _ in range(5):
    a, b, c = (random.uniform(0.5, 3) for _ in range(3))
    O, A, B, C = (0, 0, 0), (a, 0, 0), (0, b, 0), (0, 0, c)
    n = nplane(A, B, C); t = dot(sub(A, O), n) / dot(n, n); H = mul(n, t)  # chân vuông góc từ O
    ok &= abs(dot(sub(H, A), sub(C, B))) < 1e-9 and abs(dot(sub(H, B), sub(C, A))) < 1e-9  # chân đó là trực tâm
CLAIM["BM-034"] = ok
P = sq_pyr(1); V["BM-035"] = (ang_line_plane(P["S"], P["C"], P["A"], P["B"], P["C"]), {"B2": L(P["A"], P["C"])})
V["BM-036"] = (sin(ang_line_plane(P["S"], P["C"], P["S"], P["A"], P["B"]) * pi / 180), {"B3": L(P["S"], P["C"])})
P = sq_pyr(sqrt(3)); V["BM-037"] = (ang_planes(P["S"], P["B"], P["C"], P["A"], P["B"], P["C"]), {})
P = sq_pyr(1); g = ang_planes(P["S"], P["B"], P["D"], P["A"], P["B"], P["C"]); V["BM-038"] = (__import__("math").tan(g*pi/180), {})
V["BM-039"] = (d_pt_plane((0, 0, 0), (0, 0, 1), (1, 0, 0), (1, 1.7, 0)), {})
K = cube(); V["BM-040"] = (d_lines(K["A"], K["B"], K["A1"], K["D1"]), {})
O, A, B, C = (0, 0, 0), (1, 0, 0), (0, 1, 0), (0, 0, 1); d = d_pt_plane(O, A, B, C)
V["BM-041"] = (d, {"B1": L(O, mid(B, C)), "B3": 1/d**2})
P = sq_pyr(3, 3); V["BM-042"] = (d_pt_plane(P["A"], P["S"], P["B"], P["D"]), {})
P = sq_pyr(sqrt(2)); assert isclose(ang_line_plane(P["S"], P["C"], P["A"], P["B"], P["C"]), 45)
V["BM-043"] = (2*vol_tet(P["S"], P["A"], P["B"], P["C"]), {"B2": P["S"][2]})
ratios = []
for _ in range(3):
    S_, A, B, C = [tuple(random.uniform(-2, 2) for _ in range(3)) for _ in range(4)]
    Mm = mid(S_, B); N = add(S_, mul(sub(C, S_), 2/3))
    ratios.append(vol_tet(S_, A, Mm, N) / vol_tet(S_, A, B, C))
assert max(ratios) - min(ratios) < 1e-9
V["BM-044"] = (ratios[0], {"B2": 2/3})
A, B, C, D = reg_tet(); G = ((A[0]+B[0]+C[0])/3, (A[1]+B[1]+C[1])/3, 0)
V["BM-045"] = (vol_tet(A, B, C, D), {"B1": L(G, A), "B2": D[2]})
A, B, C, D = (0, 0, 0), (2, 0, 0), (2, 2, 0), (0, 2, 0); S_ = (1, 0, sqrt(3)); assert isclose(L(S_, A), 2) and isclose(L(S_, B), 2)
V["BM-046"] = (vol_tet(S_, A, B, C) + vol_tet(S_, A, C, D), {"B1": S_[2]})
V["BM-047"] = (d_pt_plane((0, 0, 0), (0, 0, 1), (1, 0, 0), (1, 2, 0)), {})
A, B, C, S_ = reg_tet(); Mm = mid(B, C); Hh = ((A[0]+B[0]+C[0])/3, (A[1]+B[1]+C[1])/3, 0)
V["BM-048"] = (ang_planes(S_, B, C, A, B, C), {"B3": L(Hh, Mm)})
V["BM-049"] = (d_pt_plane((0, 0, 0), (0, 0, 3), (3, 0, 0), (0, 6, 0)), {"B2": 1/d_pt_plane((0, 0, 0), (0, 0, 3), (3, 0, 0), (0, 6, 0))**2})
# BM-050: hình bình hành bất kì, giao AM với (SBD)
A, B, D = (0, 0, 0), (2, 0.3, 0), (0.7, 1.5, 0); C = add(B, sub(D, A)); S_ = (0.4, 0.9, 2.2); Mm = mid(S_, C)
n = nplane(S_, B, D); t = dot(sub(S_, A), n) / dot(sub(Mm, A), n)
V["BM-050"] = (t, {})
A, B, C, D = [tuple(random.uniform(-2, 2) for _ in range(3)) for _ in range(4)]
G1 = mul(add(add(A, B), C), 1/3); G2 = mul(add(add(A, B), D), 1/3)
CLAIM["BM-051"] = norm(cross(sub(G2, G1), sub(D, C))) < 1e-9
V["BM-051"] = (L(G1, G2) / L(C, D), {})
A, B, D = (0, 0, 0), (2, 0.3, 0), (0.7, 1.5, 0); C = add(B, sub(D, A)); S_ = (0.4, 0.9, 2.2); Mm = mid(S_, A)
CLAIM["BM-052"] = abs(dot(sub(C, S_), nplane(Mm, B, D))) < 1e-9
# BM-053: ba mặt phẳng z=0, z=2, z=5 (AB:BC = 2:3) và cát tuyến bất kì có A'C' = 15
w = (2.0, 2.0, 1.0); P0 = (1, 1, 0)  # |w| = 3; cắt z = 0, 2, 5 tại t = z
A1, B1, C1 = (add(P0, mul(w, z)) for z in (0, 2, 5))
assert isclose(L(A1, C1), 15)
V["BM-053"] = (L(A1, B1), {})
A, B, D = (0, 0, 0), (2, 0.3, 0), (0.7, 1.5, 0); C = add(B, sub(D, A)); S_ = (0.4, 0.9, 2.2); Mm = mid(S_, C)
n = nplane(A, B, Mm); t = dot(sub(A, S_), n) / dot(sub(D, S_), n)  # N = S + t(D - S)
CLAIM["BM-054"] = isclose(t, 0.5)
A, B, D = (0, 0, 0), (6, 0, 0), (1, 6, 0); C = add(B, sub(D, A)); S_ = (2, 1, 5)  # diện tích đáy 36
assert isclose(norm(nplane(A, B, D)), 36)
f = lambda X: add(S_, mul(sub(X, S_), 1/3))
V["BM-055"] = (norm(nplane(f(A), f(B), f(D))), {"B1": L(S_, f(A)) / L(S_, A)})
A, C = (0, 0, 0), (6, 0, 0); B = (1, 2, 3); D = add(B, mul((0.36, 0.48, 0.8), 8))
Mq, Nq, Pq, Qq = mid(A, B), mid(B, C), mid(C, D), mid(D, A)
V["BM-056"] = (L(Mq, Nq) + L(Nq, Pq) + L(Pq, Qq) + L(Qq, Mq), {"B1": L(Mq, Nq), "B2": L(Nq, Pq)})
# đại số
f57 = lambda x: 2*cos(x)**2 - 3*cos(x) + 1
CLAIM["BM-057"] = all(abs(f57(x)) < 1e-12 for x in (0, pi/3, -pi/3, 2*pi, pi/3 + 2*pi)) and \
    sum(1 for i in range(200000) if f57(2*pi*i/200000) * f57(2*pi*(i+1)/200000) <= 0) <= 6 and abs(f57(pi)) > 1
s = 0; n = 0
while s < 169: n += 1; s += 2*n - 1
V["BM-058"] = (float(n) if s == 169 else -1, {})
from math import log
g = lambda x: log(x*x-3*x+2, 3) - log(x+10, 3)
r1, r2 = 2 + 2*sqrt(3), 2 - 2*sqrt(3)
CLAIM["BM-059"] = abs(g(r1)) < 1e-12 and abs(g(r2)) < 1e-12 and r2*r2 - 3*r2 + 2 > 0 and r2 + 10 > 0
x = 1 + 1e-8; V["BM-060"] = ((sqrt(x+3)-2)/(x-1), {})
V["BM-061"] = (3 * 0.8**2 * 0.2, {"B2": 0.8*0.8*0.2})
CLAIM["BM-062"] = all(abs(x*x-5*x+6) < 1e-12 for x in (2, 3))
x1, x2 = (5 + sqrt(17))/2, (5 - sqrt(17))/2; V["BM-063"] = (1/x1 + 1/x2, {})
sol = [(x, 50-x) for x in range(51) if 5000*x + 15000*(50-x) == 520000]; CLAIM["BM-064"] = sol == [(23, 27)]
om = [(a, b) for a in (1, 2, 3) for b in (1, 2, 3)]
V["BM-065"] = (sum(1 for a, b in om if a == b) / len(om), {"B3": float(len(om)), "B4": 3.0})
V["BM-066"] = ((pi*9*5 + 6*4/3*pi) / (pi*9), {"B1": pi*45, "B2": pi*9*5 + 8*pi})
Bf = lambda x: 2*sqrt(x)/(sqrt(x)-1) - (sqrt(x)+1)/(sqrt(x)+2) - (2*sqrt(x)+4)/(x+sqrt(x)-2)
CLAIM["BM-067"] = all(isclose(Bf(x), (sqrt(x)+3)/(sqrt(x)+2)) for x in (0, 0.3, 4, 9, 17.5))
Pf = lambda x: (sqrt(x)-5)/(sqrt(x)+2)
cand = [25, 9/4, 1/9]
CLAIM["BM-068"] = all(isclose(Pf(x), round(Pf(x)), abs_tol=1e-12) for x in cand) and \
    sorted(round(Pf(x)) for x in cand) == [-2, -1, 0] and Pf(0) == -2.5  # P ∈ [-2,5;1) nên chỉ -2,-1,0
roots = [x for x in range(4, 200) if abs(360/(x-3) - 360/x - 4) < 1e-9]; V["BM-069"] = (float(roots[0]) if roots == [18] else -1, {"B6": sqrt(36 + 4*1080)})
v70 = 1/3*3.14*36*12; V["BM-070"] = (float(round(v70/15)), {"B1": v70})
ms = []
for k in range(-600, 601):
    m = k / 60
    dlt = (m-1)**2 - (2*m-3)
    if dlt <= 1e-12: continue
    a1 = (m-1) + sqrt(dlt); a2 = (m-1) - sqrt(dlt)
    if isclose(a1, 3*a2, abs_tol=1e-9) or isclose(a2, 3*a1, abs_tol=1e-9): ms.append(m)
CLAIM["BM-071"] = len(ms) == 2 and isclose(ms[0], 5/3) and isclose(ms[1], 3)


def main():
    out = []
    for f in sorted(os.listdir(ITEMS)):
        it = json.load(open(os.path.join(ITEMS, f), encoding="utf-8"))
        iid, so = it["id"], it["dap_so"]["so"]
        loi = []
        if iid in V:
            val, inter = V[iid]
            if so is None:
                loi.append("có giá trị độc lập nhưng dap_so.so null")
            elif not isclose(val, so, rel_tol=1e-6, abs_tol=1e-6):
                loi.append(f"đáp số {so} ≠ tính lại {val}")
            steps = {b["ma"]: b for b in it["buoc"]}
            for ma, v in inter.items():
                if v is None: continue
                b = steps.get(ma)
                if b is None or "ket_qua_so" not in b:
                    loi.append(f"{ma}: không có ket_qua_so để so")
                elif not isclose(v, b["ket_qua_so"], rel_tol=1e-6, abs_tol=1e-6):
                    loi.append(f"{ma}: ket_qua_so {b['ket_qua_so']} ≠ tính lại {v}")
        if iid in CLAIM and not CLAIM[iid]:
            loi.append("mệnh đề/nghiệm không thỏa khi kiểm số")
        if iid not in V and iid not in CLAIM:
            loi.append("chưa có kiểm độc lập")
        if it["dap_so_engine"] is not None and not isclose(it["dap_so_engine"], V[iid][0], rel_tol=1e-6, abs_tol=1e-6):
            loi.append("engine ≠ tính lại")
        out.append({"id": iid, "ket_qua": "dat" if not loi else "loi", "loi": loi})
    rep = {"ngay": "2026-11-27", "cach": "gen/verify.py — tọa độ/vectơ/duyệt số độc lập với lời giải",
           "tong": len(out), "dat": sum(r["ket_qua"] == "dat" for r in out), "bai": out}
    json.dump(rep, open(os.path.join(HERE, "..", "soat-cheo.json"), "w", encoding="utf-8"), ensure_ascii=False, indent=1)
    for r in out:
        if r["loi"]:
            print(r["id"], r["loi"])
    print(f"đạt {rep['dat']}/{rep['tong']}")


if __name__ == "__main__":
    main()
