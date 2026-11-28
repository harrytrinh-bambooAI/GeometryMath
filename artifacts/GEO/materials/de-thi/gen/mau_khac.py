"""Phiên bản: v1.0 · 27/11/2026
Mẫu câu ngoài hình không gian mà các ma trận cần (đại số, giải tích, Oxyz, xác suất, thống kê).
Đáp số: biểu thức sympy `dap_so` theo tham số. Kiểm độc lập: hàm KIEM[id](env) tính lại bằng cách khác
(duyệt, mô phỏng, tích phân số, thay nghiệm) — check.py so hai giá trị (sai số 1e-6).
"""
import itertools
import math
from fractions import Fraction as Fr

from mau_hh import L, N  # noqa: F401


def mk(id, ten, chu_de, dang, dang_tx, yccd, lop, tham_so, dan_xuat, boi_canh, hoi, dap_so, nhieu, muc_do,
       loai=("tn4", "tra-loi-ngan"), kieu="so", tien_to="", hau_to="", rang_buoc=(), tl=None,
       nang_luc="tu-duy-lap-luan", duong=False, tln_nd=(2, 1, 0), hoi_tl=None):
    return {"id": id, "ten": ten, "chu_de": chu_de, "dang": dang, "dang_tx": dang_tx, "yccd": yccd, "lop": lop,
            "nang_luc": nang_luc, "ho_hinh": "TK" if id.startswith("MC-TK") else id, "loai_cau": list(loai), "muc_do": muc_do,
            "nguon": {"loai": "tu-soan", "ma": id, "ref": "gen/mau_khac.py (soạn mới theo dạng trong lich-su/taxonomy.json)"},
            "tham_so": tham_so, "rang_buoc": list(rang_buoc), "dan_xuat": dan_xuat, "boi_canh": boi_canh,
            "hoi": hoi, "dap_so": dap_so, "nhieu": nhieu, "kieu": kieu, "tien_to": tien_to, "hau_to": hau_to,
            "duong": duong, "tu_luan": tl, "hoi_tl": hoi_tl, "tln_nd": list(tln_nd), "kiem": "gen/mau_khac.py:KIEM['%s']" % id}


TK_BO = [{"n1": 3, "n2": 7, "n3": 12, "n4": 6, "n5": 2}, {"n1": 2, "n2": 8, "n3": 10, "n4": 5, "n5": 5},
         {"n1": 4, "n2": 6, "n3": 9, "n4": 7, "n5": 4}, {"n1": 5, "n2": 9, "n3": 10, "n4": 4, "n5": 2}]
TK_DX = {"g0": "x0", "g1": "x0+d", "g2": "x0+2*d", "g3": "x0+3*d", "g4": "x0+4*d", "g5": "x0+5*d",
         "NN": "n1+n2+n3+n4+n5"}
TK_CTX = (r"Bảng sau thống kê một mẫu số liệu ghép nhóm:" "\n\n"
          r"| Nhóm | $[{g0};{g1})$ | $[{g1};{g2})$ | $[{g2};{g3})$ | $[{g3};{g4})$ | $[{g4};{g5})$ |" "\n"
          r"|---|---|---|---|---|---|" "\n"
          r"| Tần số | ${n1}$ | ${n2}$ | ${n3}$ | ${n4}$ | ${n5}$ |" "\n")
MEAN = "(n1*(x0+d/2)+n2*(x0+3*d/2)+n3*(x0+5*d/2)+n4*(x0+7*d/2)+n5*(x0+9*d/2))/NN"
TS_TK = {"x0": ["10", "20", "40"], "d": ["2", "5", "10"], "_bo": TK_BO}

MAU = [
    # ------------------------------------------------------------------ thống kê
    mk("MC-TK-01", "Số trung bình của mẫu ghép nhóm", "thong-ke", "tke.trung-binh", "tke.ghep-nhom", ["11.TK.1"], 11,
       TS_TK, TK_DX, TK_CTX, r"Số trung bình của mẫu số liệu (làm tròn đến hàng phần trăm) là", MEAN,
       [N("(n1*x0+n2*(x0+d)+n3*(x0+2*d)+n4*(x0+3*d)+n5*(x0+4*d))/NN", "sai-dinh-li", "Dùng đầu mút trái của nhóm thay cho giá trị đại diện"),
        N("(n1*(x0+d/2)+n2*(x0+3*d/2)+n3*(x0+5*d/2)+n4*(x0+7*d/2)+n5*(x0+9*d/2))/5", "sai-dinh-li", "Chia cho số nhóm (5) thay cho cỡ mẫu"),
        N("(x0+x0+5*d)/2", "sai-dinh-li", "Lấy trung điểm của toàn bộ khoảng số liệu")],
       {"tn4": "NB", "tra-loi-ngan": "TH"}, kieu="xap-xi:2", tln_nd=(1, 0)),
    mk("MC-TK-02", "Mốt của mẫu ghép nhóm", "thong-ke", "tke.mot", "tke.ghep-nhom", ["11.TK.1"], 11, TS_TK, TK_DX,
       TK_CTX, r"Mốt của mẫu số liệu (làm tròn đến hàng phần trăm) là",
       "x0+2*d+(n3-n2)/((n3-n2)+(n3-n4))*d",
       [N("x0+2*d+d/2", "sai-dinh-li", "Lấy giá trị đại diện của nhóm chứa mốt"),
        N("x0+2*d", "sai-dinh-li", "Lấy đầu mút trái của nhóm chứa mốt"),
        N("x0+2*d+(n3-n2)/(n3-n4)*d", "sai-tinh-toan", "Sai mẫu số công thức mốt (chỉ dùng $n_3-n_4$)")],
       {"tn4": "TH", "tra-loi-ngan": "TH"}, kieu="xap-xi:2", tln_nd=(1, 0)),
    mk("MC-TK-03", "Khoảng biến thiên của mẫu ghép nhóm", "thong-ke", "tke.khoang-bien-thien", "tke.ghep-nhom",
       ["12.TK.1"], 12, TS_TK, TK_DX, TK_CTX, r"Khoảng biến thiên của mẫu số liệu là", "5*d",
       [N("4*d", "sai-tinh-toan", "Lấy đầu mút trái nhóm cuối trừ đầu mút trái nhóm đầu"),
        N("n3-n5", "sai-dinh-li", "Lấy hiệu tần số lớn nhất và nhỏ nhất"),
        N("x0+5*d", "sai-dinh-li", "Lấy giá trị lớn nhất của mẫu (quên trừ giá trị nhỏ nhất)")],
       {"tn4": "NB", "tra-loi-ngan": "NB"}),
    # ------------------------------------------------------------------ nguyên hàm, tích phân
    mk("MC-NH-01", "Nguyên hàm của đa thức", "nguyen-ham-tich-phan", "nh.nguyen-ham-da-thuc", "nh.co-ban",
       ["12.NH.1"], 12, {"p": ["3", "6"], "q": ["2", "4", "-2"], "r": ["1", "5", "-3"]}, {"fx": "p*x**2+q*x+r"},
       r"Cho hàm số $f(x)={fx}$.", r"Họ nguyên hàm của $f(x)$ là", "p*x**3/3+q*x**2/2+r*x",
       [N("2*p*x+q", "sai-dinh-li", "Lấy đạo hàm thay vì nguyên hàm"),
        N("p*x**3+q*x**2+r*x", "sai-dinh-li", "Quên chia cho số mũ mới"),
        N("p*x**3/3+q*x**2/2", "sai-tinh-toan", "Bỏ sót nguyên hàm của hằng số")],
       {"tn4": "NB"}, loai=("tn4",), kieu="ham", hau_to="+C"),
    mk("MC-NH-02", "Tích phân của hàm bậc nhất", "nguyen-ham-tich-phan", "nh.tich-phan-bac-nhat", "nh.tp-co-ban",
       ["12.NH.2"], 12, {"p": ["2", "4", "6"], "q": ["1", "3", "-1"], "m": ["0", "1"], "n": ["2", "3"]},
       {"fx": "p*x+q"}, "", r"Giá trị của $\displaystyle\int_{{m}}^{{n}}({fx})\,dx$ bằng",
       "p*(n**2-m**2)/2+q*(n-m)",
       [N("p*n**2/2+q*n", "sai-dinh-li", "Chỉ thay cận trên, quên trừ $F(m)$"),
        N("p*(n**2-m**2)+q*(n-m)", "sai-dinh-li", "Quên chia 2 khi lấy nguyên hàm của $x$"),
        N("p*(n**2-m**2)/2+q", "sai-tinh-toan", "Lấy nguyên hàm của hằng số $q$ là $q$")],
       {"tn4": "NB", "tra-loi-ngan": "TH"}, rang_buoc=["m<n"]),
    mk("MC-NH-03", "Tính chất tích phân", "nguyen-ham-tich-phan", "nh.tinh-chat", "nh.tp-tinh-chat", ["12.NH.2"], 12,
       {"u": ["2", "3", "-1"], "v": ["4", "-2"], "al": ["2", "3"], "be": ["1", "-1", "2"]}, {},
       r"Cho $\displaystyle\int_0^1 f(x)\,dx={u}$ và $\displaystyle\int_0^1 g(x)\,dx={v}$.",
       r"Giá trị của $\displaystyle\int_0^1\big[{al}f(x)+({be})g(x)\big]dx$ bằng", "al*u+be*v",
       [N("u+v", "sai-dinh-li", "Bỏ quên hệ số khi tách tích phân"),
        N("al*u-be*v", "sai-tinh-toan", "Sai dấu số hạng thứ hai"),
        N("al*be*u*v", "sai-dinh-li", "Coi tích phân của tổng bằng tích các tích phân")],
       {"tn4": "NB", "tra-loi-ngan": "NB"}),
    # ------------------------------------------------------------------ dãy số
    mk("MC-DS-01", "Số hạng tổng quát cấp số cộng", "day-so-cap-so", "ds.csc-so-hang", "ds.cap-so-cong", ["11.DS.1"],
       11, {"u1": ["2", "-3", "5"], "d": ["3", "4", "-2"], "n": ["10", "15", "20"]}, {},
       r"Cho cấp số cộng $(u_n)$ có $u_1={u1}$ và công sai $d={d}$.", r"Số hạng $u_{{n}}$ bằng", "u1+(n-1)*d",
       [N("u1+n*d", "sai-dinh-li", "Dùng công thức $u_n=u_1+nd$"),
        N("n*(2*u1+(n-1)*d)/2", "sai-dinh-li", "Tính tổng $S_n$ thay cho $u_n$"),
        N("u1+(n-2)*d", "sai-tinh-toan", "Đếm sai số công sai cần cộng")],
       {"tn4": "NB", "tra-loi-ngan": "NB"}),
    mk("MC-DS-02", "Số hạng cấp số nhân", "day-so-cap-so", "ds.csn-so-hang", "ds.cap-so-nhan", ["11.DS.2"], 11,
       {"u1": ["2", "3", "-1"], "q": ["2", "3", "-2"], "n": ["5", "6"]}, {},
       r"Cho cấp số nhân $(u_n)$ có $u_1={u1}$ và công bội $q={q}$.", r"Số hạng $u_{{n}}$ bằng", "u1*q**(n-1)",
       [N("u1*q**n", "sai-dinh-li", "Dùng công thức $u_n=u_1q^n$"),
        N("u1+(n-1)*q", "sai-dinh-li", "Nhầm sang công thức cấp số cộng"),
        N("u1*(q**n-1)/(q-1)", "sai-dinh-li", "Tính tổng $S_n$ thay cho $u_n$")],
       {"tn4": "TH", "tra-loi-ngan": "TH"}),
    mk("MC-DS-03", "Tổng n số hạng đầu cấp số cộng", "day-so-cap-so", "ds.csc-tong", "ds.cap-so-cong", ["11.DS.1"],
       11, {"u1": ["1", "2", "5"], "d": ["2", "3"], "n": ["10", "12"]}, {},
       r"Cho cấp số cộng $(u_n)$ có $u_1={u1}$ và công sai $d={d}$.",
       r"Tổng ${n}$ số hạng đầu tiên $S_{{n}}$ bằng", "n*(2*u1+(n-1)*d)/2",
       [N("n*(2*u1+n*d)/2", "sai-dinh-li", "Dùng $u_n=u_1+nd$ khi tính tổng"),
        N("n*(u1+(n-1)*d)", "sai-dinh-li", "Nhân số số hạng với số hạng cuối"),
        N("u1+(n-1)*d", "sai-dinh-li", "Tính $u_n$ thay cho $S_n$")],
       {"tn4": "TH", "tra-loi-ngan": "TH"}, duong=True),
    # ------------------------------------------------------------------ mũ, lôgarit
    mk("MC-ML-01", "Phương trình lôgarit cơ bản", "ham-so-mu-logarit", "ml.pt-log-co-ban", "ml.pt-log", ["11.ML.3"], 11,
       {"b": ["2", "3"], "r": ["2", "3"], "p": ["1", "2"], "q": ["1", "-1", "3"]}, {"ux": "p*x+q"}, "",
       r"Nghiệm của phương trình $\log_{{b}}({ux})={r}$ là", "(b**r-q)/p",
       [N("(r*b-q)/p", "sai-dinh-li", r"Hiểu $\log_b u=r$ thành $u=rb$"),
        N("(b**r+q)/p", "sai-tinh-toan", "Chuyển vế sai dấu"),
        N("(r**b-q)/p", "sai-dinh-li", r"Đảo cơ số và số mũ: $u=r^b$")],
       {"tn4": "NB", "tra-loi-ngan": "NB", "tu-luan": "TH"}, kieu="so", tien_to="x=", loai=("tn4", "tra-loi-ngan", "tu-luan"),
       rang_buoc=["p*((b**r-q)/p)+q>0"], hoi_tl=r"Giải phương trình $\log_{{b}}({ux})={r}$.",
       tl=[{"noi_dung": r"Điều kiện: ${ux}>0$.", "diem": 0.25},
           {"noi_dung": r"Phương trình tương đương ${ux}={b}^{{r}}$.", "diem": 0.5},
           {"noi_dung": r"Suy ra $x={ds}$, thỏa mãn điều kiện. Vậy nghiệm là $x={ds}$.", "diem": 0.25}]),
    mk("MC-ML-02", "Phương trình mũ cơ bản", "ham-so-mu-logarit", "ml.pt-mu-co-ban", "ml.pt-mu", ["11.ML.3"], 11,
       {"b": ["2", "3", "5"], "t": ["2", "3", "4"], "q": ["1", "-1", "2"]}, {"M": "b**t", "ex": "x+q"}, "",
       r"Nghiệm của phương trình ${b}^{{ex}}={M}$ là", "t-q",
       [N("t+q", "sai-tinh-toan", "Chuyển vế sai dấu"),
        N("M/b-q", "sai-dinh-li", r"Coi $b^u=M\Leftrightarrow u=\dfrac Mb$"),
        N("t", "sai-tinh-toan", "Quên số hạng $q$ trong số mũ")],
       {"tn4": "NB", "tra-loi-ngan": "NB"}, tien_to="x="),
    mk("MC-ML-03", "Biến đổi biểu thức lôgarit", "ham-so-mu-logarit", "ml.bien-doi-log", "ml.bien-doi", ["11.ML.1"], 11,
       {"u": ["2", "3"], "v": ["-1", "5"], "p": ["2", "3"], "q": ["1", "4"]}, {},
       r"Cho $\log_a x={u}$ và $\log_a y={v}$ ($a,x,y>0$, $a\ne1$).",
       r"Giá trị của $\log_a\left(x^{{p}}y^{{q}}\right)$ bằng", "p*u+q*v",
       [N("p*u+v", "sai-dinh-li", "Quên hệ số mũ của $y$"),
        N("p*u-q*v", "sai-tinh-toan", "Biến lôgarit của tích thành hiệu"),
        N("(p+q)*(u+v)", "sai-dinh-li", "Cộng các số mũ rồi nhân với tổng lôgarit")],
       {"tn4": "TH", "tra-loi-ngan": "TH"}),
    mk("MC-ML-04", "Lãi kép: số năm để vốn gấp đôi", "ham-so-mu-logarit", "ml.lai-kep", "ml.lai-suat", ["11.ML.4"], 11,
       {"r": ["5", "6", "7", "8"]}, {},
       r"Một người gửi tiết kiệm với lãi suất ${r}\%$ một năm, lãi kép theo năm, không rút tiền.",
       r"Số năm ít nhất để số tiền (cả vốn và lãi) vượt gấp đôi số vốn ban đầu là",
       "ceiling(log(2)/log(1+r/100))",
       [N("floor(log(2)/log(1+r/100))", "nham-dieu-kien", "Làm tròn xuống thay vì lấy số nguyên nhỏ nhất thỏa mãn"),
        N("ceiling(100/r)", "sai-dinh-li", r"Tính như lãi đơn: $n\cdot r\%\ge 100\%$"),
        N("ceiling(log(3)/log(1+r/100))", "nham-dieu-kien", "Nhầm điều kiện gấp đôi thành tổng tiền gấp ba")],
       {"tn4": "TH", "tra-loi-ngan": "VD"}, nang_luc="mo-hinh-hoa", duong=True),
    mk("MC-ML-05", "Số nghiệm nguyên của bất phương trình lôgarit", "ham-so-mu-logarit", "ml.bpt-log-nguyen",
       "ml.bpt-log", ["11.ML.3"], 11, {"b": ["2", "3"], "t": ["2", "3"], "c": ["1", "-2", "4"]}, {"ux": "x-c"}, "",
       r"Số nghiệm nguyên của bất phương trình $\log_{{b}}({ux})<{t}$ là", "b**t-1",
       [N("b**t", "nham-dieu-kien", "Tính cả đầu mút $x=c$ (quên điều kiện $x>c$)"),
        N("t*b-1", "sai-dinh-li", r"Hiểu $\log_b u<t\Leftrightarrow u<tb$"),
        N("b**t+c", "nham-dieu-kien", "Quên điều kiện xác định, đếm từ $x=0$")],
       {"tn4": "TH", "tra-loi-ngan": "VD", "tu-luan": "VD"}, loai=("tn4", "tra-loi-ngan", "tu-luan"), duong=True,
       tl=[{"noi_dung": r"Điều kiện: $x>{c}$.", "diem": 0.25},
           {"noi_dung": r"Vì cơ số ${b}>1$ nên bất phương trình $\Leftrightarrow {ux}<{b}^{{t}}$, tức ${c}<x<{c}+{b}^{{t}}$.", "diem": 0.5},
           {"noi_dung": r"Các số nguyên thỏa mãn là $x={c}+1,\dots,{c}+{b}^{{t}}-1$; có ${ds}$ số.", "diem": 0.25}]),
    mk("MC-ML-06", "Lôgarit với cơ số là lũy thừa", "ham-so-mu-logarit", "ml.doi-co-so", "ml.bien-doi", ["11.ML.1"], 11,
       {"u": ["2", "3", "-1", "1/2"], "p": ["2", "3"], "q": ["1", "2", "4"]}, {},
       r"Cho $\log_a b={u}$ ($a,b>0$, $a\ne1$).", r"Giá trị của $\log_{a^{{p}}}\left(b^{{q}}\right)$ bằng", "q*u/p",
       [N("q*u*p", "sai-dinh-li", r"Nhân với số mũ của cơ số thay vì chia"),
        N("p*u/q", "sai-dinh-li", r"Đảo vị trí hai số mũ"),
        N("q*u", "sai-dinh-li", r"Bỏ quên số mũ của cơ số")],
       {"tn4": "TH", "tra-loi-ngan": "TH"}, rang_buoc=["p!=q"]),
    mk("MC-ML-07", "Phương trình mũ đưa về cùng cơ số", "ham-so-mu-logarit", "ml.pt-mu-cung-co-so", "ml.pt-mu",
       ["11.ML.3"], 11, {"b": ["2", "3", "5"], "p": ["2", "3"], "q": ["1", "-1"], "r": ["3", "-2", "4"]},
       {"e1": "p*x+q", "e2": "x+r"}, "", r"Nghiệm của phương trình ${b}^{{e1}}={b}^{{e2}}$ là", "(r-q)/(p-1)",
       [N("(r+q)/(p-1)", "sai-tinh-toan", "Chuyển vế sai dấu hằng số"),
        N("(r-q)/(p+1)", "sai-tinh-toan", "Chuyển vế sai dấu số hạng chứa $x$"),
        N("(r-q)/p", "sai-tinh-toan", "Quên trừ $x$ ở vế phải")],
       {"tn4": "TH", "tra-loi-ngan": "TH"}, tien_to="x="),
    # ------------------------------------------------------------------ hàm số, đồ thị
    mk("MC-HS-01", "Tiệm cận đứng của hàm phân thức bậc nhất", "ham-so-do-thi", "dt.tiem-can-dung", "dt.tiem-can",
       ["12.HS.3"], 12, {"p": ["1", "2", "3"], "q": ["1", "-4"], "r": ["1", "2"], "s": ["-2", "3", "4"]},
       {"tu": "p*x+q", "mau": "r*x+s"}, "", r"Tiệm cận đứng của đồ thị hàm số $y=\dfrac{{{tu}}}{{{mau}}}$ là",
       "-s/r",
       [N("s/r", "sai-tinh-toan", "Sai dấu khi giải mẫu bằng 0"),
        N("p/r", "sai-dinh-li", "Nhầm sang tiệm cận ngang"),
        N("-q/p", "sai-dinh-li", "Lấy nghiệm của tử số")],
       {"tn4": "NB"}, loai=("tn4",), tien_to="x=", rang_buoc=["p*s-q*r!=0"]),
    mk("MC-HS-02", "Giá trị cực đại của hàm bậc ba", "ham-so-ung-dung-dao-ham", "hs.gia-tri-cuc-dai",
       "hs.cuc-tri-f'", ["12.HS.1"], 12, {"p": ["1", "4", "9"], "q": ["1", "5", "-3"]}, {"fx": "x**3-3*p*x+q"},
       r"Cho hàm số $y={fx}$.", r"Giá trị cực đại của hàm số bằng", "2*p*sqrt(p)+q",
       [N("q-2*p*sqrt(p)", "sai-dinh-li", "Nhầm giá trị cực tiểu với giá trị cực đại"),
        N("-sqrt(p)", "sai-dinh-li", "Nhầm điểm cực đại với giá trị cực đại"),
        N("p*sqrt(p)+q", "sai-tinh-toan", r"Tính sai $(-\sqrt p)^3$ khi thay vào hàm số")],
       {"tn4": "TH", "tra-loi-ngan": "TH"}),
    mk("MC-HS-03", "GTNN của hàm bậc ba trên đoạn", "ham-so-ung-dung-dao-ham", "hs.gtnn-doan", "hs.gtln-doan",
       ["12.HS.2"], 12, {"c": ["1", "5", "-3"], "n": ["2", "3"]}, {"fx": "x**3-3*x+c"},
       r"Cho hàm số $f(x)={fx}$.", r"Giá trị nhỏ nhất của $f(x)$ trên đoạn $[0;{n}]$ bằng", "c-2",
       [N("c", "sai-dinh-li", "Chỉ so sánh giá trị tại hai đầu mút"),
        N("n**3-3*n+c", "sai-dinh-li", "Nhầm sang giá trị lớn nhất"),
        N("c+2", "sai-tinh-toan", "Tính sai $f(1)$")],
       {"tn4": "TH", "tra-loi-ngan": "TH"}),
    mk("MC-HS-04", "Hộp không nắp có thể tích lớn nhất", "ham-so-ung-dung-dao-ham", "hs.toi-uu-hop", "hs.toi-uu",
       ["12.HS.4"], 12, {"Lc": ["6", "12", "18", "30"]}, {},
       r"Từ một tấm bìa hình vuông cạnh ${Lc}$ cm, người ta cắt bỏ bốn hình vuông bằng nhau ở bốn góc rồi gấp lại thành một chiếc hộp không nắp.",
       r"Thể tích lớn nhất của chiếc hộp (đơn vị $\text{cm}^3$) bằng", "2*Lc**3/27",
       [N("Lc**3/16", "sai-tinh-toan", r"Chọn cạnh cắt $x=\dfrac L4$ (trung điểm miền) thay vì giải $V'(x)=0$"),
        N("Lc**3/27", "sai-tinh-toan", r"Chọn cạnh cắt $x=\dfrac L3$"),
        N("4*Lc**3/27", "sai-dinh-li", r"Lập sai thể tích $V=x(L-x)^2$ (chỉ trừ một lần)")],
       {"tn4": "VD", "tra-loi-ngan": "VD"}, nang_luc="mo-hinh-hoa", duong=True),
    mk("MC-HS-05", "Lợi nhuận lớn nhất", "ham-so-ung-dung-dao-ham", "hs.mo-hinh-loi-nhuan", "hs.mo-hinh", ["12.HS.4"],
       12, {"_bo": [{"pp": 100, "kk": 1, "cc": 40, "F": 500}, {"pp": 80, "kk": 2, "cc": 20, "F": 200},
                    {"pp": 120, "kk": 1, "cc": 60, "F": 600}, {"pp": 90, "kk": 3, "cc": 30, "F": 100}]},
       {"gia": "pp-kk*x", "cp": "cc*x+F"},
       r"Một xưởng bán $x$ sản phẩm với giá $({gia})$ nghìn đồng mỗi sản phẩm; chi phí sản xuất là $({cp})$ nghìn đồng.",
       r"Lợi nhuận lớn nhất (nghìn đồng) bằng", "(pp-cc)**2/(4*kk)-F",
       [N("(pp-cc)**2/(4*kk)", "sai-tinh-toan", "Quên trừ chi phí cố định"),
        N("(pp-cc)**2/(2*kk)-F", "sai-tinh-toan", r"Tính sai $x$ tối ưu ($x=\dfrac{p-c}{k}$)"),
        N("pp**2/(4*kk)-F", "sai-dinh-li", "Quên chi phí biến đổi khi lập hàm lợi nhuận")],
       {"tn4": "VD", "tra-loi-ngan": "VD"}, nang_luc="mo-hinh-hoa", loai=("tra-loi-ngan",)),
    # ------------------------------------------------------------------ ứng dụng tích phân
    mk("MC-UD-01", "Diện tích giữa parabol và đường thẳng", "ung-dung-tich-phan", "ud.dien-tich-parabol",
       "ud.dien-tich", ["12.UD.1"], 12, {"m": ["1", "2", "3", "4", "6"]}, {}, "",
       r"Diện tích hình phẳng giới hạn bởi đồ thị $y=x^2$ và đường thẳng $y={m}x$ bằng", "m**3/6",
       [N("m**3/3", "sai-dinh-li", r"Chỉ tính $\int_0^m x^2dx$ (quên đường thẳng)"),
        N("m**3/2", "sai-dinh-li", r"Chỉ tính $\int_0^m mx\,dx$ (quên parabol)"),
        N("m**2/6", "sai-tinh-toan", "Sai lũy thừa khi thay cận")],
       {"tn4": "NB", "tra-loi-ngan": "VD"}, duong=True),
    mk("MC-UD-02", "Thể tích khối tròn xoay quanh Ox", "ung-dung-tich-phan", "ud.the-tich-xoay-can", "ud.the-tich-xoay",
       ["12.UD.2"], 12, {"k": ["1", "2", "3"], "t": ["1", "2", "3"]}, {}, "",
       r"Thể tích khối tròn xoay tạo thành khi quay hình phẳng giới hạn bởi $y=\sqrt{{{k}x}}$, $y=0$, $x={t}$ quanh trục $Ox$ bằng",
       "pi*k*t**2/2",
       [N("k*t**2/2", "sai-dinh-li", r"Quên hệ số $\pi$"),
        N("pi*sqrt(k)*2*t**Rational(3,2)/3", "sai-dinh-li", r"Không bình phương hàm số: $\pi\int\sqrt{kx}\,dx$"),
        N("pi*k*t**2", "sai-tinh-toan", "Quên chia 2 khi lấy nguyên hàm")],
       {"tn4": "TH", "tra-loi-ngan": "VDC"}, duong=True, tln_nd=(1, 0)),
    # ------------------------------------------------------------------ Oxyz
    mk("MC-OX-01", "Vectơ pháp tuyến của mặt phẳng", "oxyz-mat-phang", "ox.vtpt", "ox.mp-vtpt", ["12.OX.2"], 12,
       {"p": ["1", "2", "3"], "q": ["-1", "2"], "r": ["3", "-4"], "s": ["5", "-7"]},
       {"mp": "p*x+q*y+r*z+s"}, r"Trong không gian $Oxyz$, cho mặt phẳng $(P): {mp}=0$.",
       r"Một vectơ pháp tuyến của $(P)$ là", "Tuple(p,q,r)",
       [N("Tuple(p,q,s)", "sai-dinh-li", "Lấy hệ số tự do thay cho hệ số của $z$"),
        N("Tuple(q,r,s)", "sai-dinh-li", "Bỏ hệ số của $x$, lấy ba hệ số cuối"),
        N("Tuple(p,q,-r)", "sai-tinh-toan", "Chép sai dấu hệ số của $z$")],
       {"tn4": "NB"}, loai=("tn4",), kieu="vecto", tien_to=r"\vec n="),
    mk("MC-OX-02", "Phương trình mặt phẳng qua điểm có VTPT", "oxyz-mat-phang", "ox.pt-mp-diem-vtpt", "ox.mp-pt",
       ["12.OX.2"], 12, {"x0": ["1", "-2"], "y0": ["2", "0"], "z0": ["-1", "3"], "p": ["1", "2"], "q": ["3", "-1"], "r": ["2"]},
       {}, r"Trong không gian $Oxyz$, cho điểm $M({x0};{y0};{z0})$ và vectơ $\vec n=({p};{q};{r})$.",
       r"Mặt phẳng đi qua $M$ và nhận $\vec n$ làm vectơ pháp tuyến có phương trình", "p*x+q*y+r*z-(p*x0+q*y0+r*z0)",
       [N("p*x+q*y+r*z+(p*x0+q*y0+r*z0)", "sai-tinh-toan", "Sai dấu hệ số tự do"),
        N("x0*x+y0*y+z0*z-(p*x0+q*y0+r*z0)", "sai-dinh-li", "Đảo vai trò tọa độ điểm và vectơ pháp tuyến"),
        N("p*x+q*y+r*z", "sai-tinh-toan", "Quên hệ số tự do (coi mặt phẳng đi qua gốc tọa độ)")],
       {"tn4": "NB"}, loai=("tn4",), kieu="pt", rang_buoc=["p*x0+q*y0+r*z0!=0"]),
    mk("MC-OX-03", "Vectơ chỉ phương của đường thẳng", "oxyz-duong-thang", "ox.vtcp", "ox.dt-vtcp", ["12.OX.3"], 12,
       {"x0": ["1", "-2"], "y0": ["3", "-1"], "z0": ["2", "-4"], "p": ["2", "-3"], "q": ["1", "5"], "r": ["-1", "4"]},
       {"X": "x-x0", "Y": "y-y0", "Z": "z-z0"},
       r"Trong không gian $Oxyz$, cho đường thẳng $d:\dfrac{{{X}}}{{{p}}}=\dfrac{{{Y}}}{{{q}}}=\dfrac{{{Z}}}{{{r}}}$.",
       r"Một vectơ chỉ phương của $d$ là", "Tuple(p,q,r)",
       [N("Tuple(x0,y0,z0)", "sai-dinh-li", "Lấy tọa độ điểm thuộc $d$ làm vectơ chỉ phương"),
        N("Tuple(-x0,-y0,-z0)", "sai-dinh-li", "Lấy các số trên tử (đổi dấu) làm vectơ chỉ phương"),
        N("Tuple(p,q,-r)", "sai-tinh-toan", "Chép sai dấu một thành phần")],
       {"tn4": "NB"}, loai=("tn4",), kieu="vecto", tien_to=r"\vec u=",
       rang_buoc=["Abs(p*y0-q*x0)+Abs(q*z0-r*y0)+Abs(p*z0-r*x0)>0"]),
    mk("MC-OX-04", "Tọa độ vectơ từ hai điểm", "oxyz-toa-do-vecto", "ox.toa-do-vecto", "ox.vecto", ["12.OX.1"], 12,
       {"_bo": [{"x1": 1, "y1": 2, "z1": -1, "x2": 3, "y2": -1, "z2": 4}, {"x1": -2, "y1": 0, "z1": 3, "x2": 1, "y2": 5, "z2": -2},
                {"x1": 4, "y1": -3, "z1": 2, "x2": 0, "y2": 1, "z2": 5}, {"x1": 2, "y1": 2, "z1": 0, "x2": -1, "y2": 4, "z2": 3}]},
       {}, r"Trong không gian $Oxyz$, cho hai điểm $A({x1};{y1};{z1})$ và $B({x2};{y2};{z2})$.",
       r"Tọa độ của vectơ $\overrightarrow{AB}$ là", "Tuple(x2-x1,y2-y1,z2-z1)",
       [N("Tuple(x1-x2,y1-y2,z1-z2)", "sai-dinh-li", "Lấy tọa độ điểm đầu trừ điểm cuối"),
        N("Tuple(x1+x2,y1+y2,z1+z2)", "sai-dinh-li", "Cộng tọa độ hai điểm"),
        N("Tuple((x1+x2)/2,(y1+y2)/2,(z1+z2)/2)", "sai-dinh-li", "Nhầm sang tọa độ trung điểm")],
       {"tn4": "NB"}, loai=("tn4",), kieu="vecto"),
    mk("MC-OX-05", "Tích vô hướng hai vectơ", "oxyz-toa-do-vecto", "ox.tich-vo-huong", "ox.vecto", ["12.OX.1"], 12,
       {"u1": ["1", "2"], "u2": ["-1", "3"], "u3": ["2", "-2"], "v1": ["3", "-1"], "v2": ["2"], "v3": ["1", "4"]}, {},
       r"Trong không gian $Oxyz$, cho $\vec u=({u1};{u2};{u3})$ và $\vec v=({v1};{v2};{v3})$.",
       r"Tích vô hướng $\vec u\cdot\vec v$ bằng", "u1*v1+u2*v2+u3*v3",
       [N("u1*v1+u2*v2", "sai-tinh-toan", "Bỏ sót thành phần thứ ba"),
        N("u1+v1+u2+v2+u3+v3", "sai-dinh-li", "Cộng các thành phần thay vì nhân"),
        N("u1*v1-u2*v2+u3*v3", "sai-tinh-toan", "Sai dấu thành phần thứ hai")],
       {"tn4": "TH", "tra-loi-ngan": "TH"}),
    mk("MC-OX-06", "Bán kính mặt cầu qua một điểm", "oxyz-mat-cau", "ox.mc-ban-kinh", "ox.mc-pt", ["12.OX.4"], 12,
       {"_bo": [{"i1": 1, "i2": -2, "i3": 3, "m1": 3, "m2": 0, "m3": 4}, {"i1": 0, "i2": 2, "i3": -1, "m1": 2, "m2": 5, "m3": 1},
                {"i1": -1, "i2": 1, "i3": 2, "m1": 3, "m2": -1, "m3": 0}, {"i1": 2, "i2": 3, "i3": 1, "m1": 5, "m2": 1, "m3": -1}]},
       {}, r"Trong không gian $Oxyz$, cho mặt cầu $(S)$ có tâm $I({i1};{i2};{i3})$ và đi qua điểm $M({m1};{m2};{m3})$.",
       r"Bán kính của $(S)$ (làm tròn đến hàng phần trăm) bằng", "sqrt((m1-i1)**2+(m2-i2)**2+(m3-i3)**2)",
       [N("(m1-i1)**2+(m2-i2)**2+(m3-i3)**2", "sai-tinh-toan", "Quên lấy căn bậc hai"),
        N("Abs(m1-i1)+Abs(m2-i2)+Abs(m3-i3)", "sai-dinh-li", "Cộng trị tuyệt đối hiệu tọa độ"),
        N("sqrt((m1+i1)**2+(m2+i2)**2+(m3+i3)**2)", "sai-tinh-toan", "Cộng tọa độ thay vì trừ")],
       {"tn4": "TH", "tra-loi-ngan": "VD"}, kieu="xap-xi:2", duong=True),
    # ------------------------------------------------------------------ lượng giác, đạo hàm
    mk("MC-LG-01", "Nghiệm dương nhỏ nhất của phương trình sin cơ bản", "luong-giac", "lg.sin-nghiem-duong",
       "lg.pt-co-ban", ["11.LG.1"], 11, {"s": ["1/2", "sqrt(2)/2", "sqrt(3)/2"]}, {}, "",
       r"Nghiệm dương nhỏ nhất của phương trình $\sin x={s}$ là", "asin(s)",
       [N("acos(s)", "sai-dinh-li", r"Nhầm $\sin$ với $\cos$ khi tra giá trị đặc biệt"),
        N("pi-asin(s)", "sai-dinh-li", r"Lấy nghiệm thuộc họ $x=\pi-\alpha+k2\pi$"),
        N("2*pi-asin(s)", "sai-dinh-li", r"Lấy $-\alpha+2\pi$ (nhầm với họ nghiệm của $\cos$)"),
        N("asin(s)+pi", "sai-dinh-li", r"Dùng chu kì $\pi$ của $\tan$")],
       {"tn4": "NB"}, loai=("tn4",), tien_to="x=", duong=True),
    mk("MC-DH-01", "Đạo hàm tại một điểm", "dao-ham-co-ban", "dh.dao-ham-tai-diem", "dh1.quy-tac", ["11.DH.1"], 11,
       {"p": ["1", "2"], "q": ["-3", "1"], "r": ["4", "-2"], "x0": ["1", "2", "-1"]}, {"fx": "p*x**3+q*x**2+r*x"},
       r"Cho hàm số $f(x)={fx}$.", r"Giá trị $f'({x0})$ bằng", "3*p*x0**2+2*q*x0+r",
       [N("p*x0**3+q*x0**2+r*x0", "sai-dinh-li", "Tính $f(x_0)$ thay cho $f'(x_0)$"),
        N("p*x0**2+q*x0+r", "sai-dinh-li", "Hạ bậc nhưng không nhân với số mũ"),
        N("3*p*x0**2+q*x0+r", "sai-tinh-toan", "Quên nhân 2 khi lấy đạo hàm $x^2$")],
       {"tn4": "NB", "tra-loi-ngan": "NB"}),
    mk("MC-DH-02", "Phương trình tiếp tuyến của parabol", "dao-ham-co-ban", "dh.tiep-tuyen", "dh1.quy-tac",
       ["11.DH.2"], 11, {"p": ["2", "-4", "1"], "q": ["1", "-3"], "x0": ["1", "2", "-1"]}, {"fx": "x**2+p*x+q", "kk": "2*x0+p", "y0": "x0**2+p*x0+q"},
       r"Cho hàm số $y={fx}$ có đồ thị $(C)$.", r"Tiếp tuyến của $(C)$ tại điểm có hoành độ $x={x0}$ có phương trình",
       "(2*x0+p)*(x-x0)+x0**2+p*x0+q",
       [N("(2*x0+p)*x+x0**2+p*x0+q", "sai-dinh-li", r"Quên trừ $x_0$: viết $y=f'(x_0)x+f(x_0)$"),
        N("(x0**2+p*x0+q)*(x-x0)+2*x0+p", "sai-dinh-li", r"Đổi vai trò $f'(x_0)$ và $f(x_0)$"),
        N("(x0+p)*(x-x0)+x0**2+p*x0+q", "sai-tinh-toan", r"Lấy đạo hàm của $x^2$ là $x$")],
       {"tn4": "TH", "tu-luan": "VD"}, loai=("tn4", "tu-luan"), kieu="ham", tien_to="y=",
       hoi_tl=r"Viết phương trình tiếp tuyến của $(C)$ tại điểm có hoành độ $x={x0}$.",
       tl=[{"noi_dung": r"$y'=2x+({p})$, hệ số góc $k=y'({x0})={kk}$.", "diem": 0.25},
           {"noi_dung": r"Tung độ tiếp điểm $y_0=y({x0})={y0}$.", "diem": 0.25},
           {"noi_dung": r"Phương trình tiếp tuyến $y=k(x-x_0)+y_0$, tức $y={ds}$.", "diem": 0.5}]),
    mk("MC-DH-03", "Đạo hàm cấp hai tại một điểm", "dao-ham-co-ban", "dh.dao-ham-cap-hai", "dh1.quy-tac", ["11.DH.3"],
       11, {"p": ["1", "2", "-1"], "q": ["2", "-3"], "x0": ["1", "2"]}, {"fx": "p*x**4+q*x**3"},
       r"Cho hàm số $f(x)={fx}$.", r"Giá trị $f''({x0})$ bằng", "12*p*x0**2+6*q*x0",
       [N("4*p*x0**3+3*q*x0**2", "sai-dinh-li", "Chỉ tính đạo hàm cấp một"),
        N("12*p*x0**2+3*q*x0", "sai-tinh-toan", "Quên nhân 2 khi lấy đạo hàm lần hai của $x^3$"),
        N("4*p*x0**2+3*q*x0", "sai-dinh-li", "Hạ bậc hai lần nhưng không nhân các số mũ")],
       {"tn4": "TH", "tra-loi-ngan": "TH"}),
    mk("MC-DH-04", "Đạo hàm của hàm căn bậc hai tại một điểm", "dao-ham-co-ban", "dh.dao-ham-can", "dh1.quy-tac",
       ["11.DH.1"], 11, {"p": ["2", "3", "4"], "q": ["1", "5"], "x0": ["0", "1", "2"]}, {"ux": "p*x+q"},
       r"Cho hàm số $f(x)=\sqrt{{{ux}}}$.", r"Giá trị $f'({x0})$ (làm tròn đến hàng phần trăm) bằng",
       "p/(2*sqrt(p*x0+q))",
       [N("1/(2*sqrt(p*x0+q))", "sai-dinh-li", r"Quên nhân đạo hàm của biểu thức trong căn"),
        N("p/sqrt(p*x0+q)", "sai-tinh-toan", r"Quên hệ số $\dfrac12$ của đạo hàm $\sqrt u$"),
        N("sqrt(p*x0+q)", "sai-dinh-li", r"Tính $f(x_0)$ thay cho $f'(x_0)$")],
       {"tn4": "TH", "tra-loi-ngan": "TH"}, kieu="xap-xi:2", duong=True),
    mk("MC-DH-05", "Vận tốc tức thời của chuyển động", "dao-ham-co-ban", "dh.van-toc-tuc-thoi", "dh1.quy-tac",
       ["11.DH.1"], 11, {"p": ["3", "6"], "q": ["9", "12", "15"], "t0": ["2", "3", "5"]}, {"st": "t**3-p*t**2+q*t"},
       r"Một vật chuyển động theo phương trình $s(t)={st}$ ($s$ tính bằng mét, $t$ tính bằng giây).",
       r"Vận tốc tức thời của vật tại thời điểm $t={t0}$ (m/s) bằng", "3*t0**2-2*p*t0+q",
       [N("t0**3-p*t0**2+q*t0", "sai-dinh-li", "Tính quãng đường $s(t_0)$ thay cho vận tốc"),
        N("6*t0-2*p", "sai-dinh-li", "Tính gia tốc $s''(t_0)$ thay cho vận tốc"),
        N("3*t0**2-p*t0+q", "sai-tinh-toan", r"Lấy đạo hàm của $pt^2$ là $pt$")],
       {"tn4": "TH", "tra-loi-ngan": "TH"}, nang_luc="mo-hinh-hoa"),
    # ------------------------------------------------------------------ bất phương trình, hệ (lớp 10)
    mk("MC-PT-01", "Nghiệm nguyên lớn nhất theo y của bất phương trình bậc nhất hai ẩn", "phuong-trinh-he-bpt",
       "pt.bpt-2-an-nghiem", "pt.he-bpt-2-an", ["10.BPT.1"], 10,
       {"p": ["2", "3"], "q": ["3", "4"], "r": ["13", "20", "25"], "x0": ["1", "2"]}, {"bt": "p*x+q*y"},
       r"Cho bất phương trình bậc nhất hai ẩn ${bt}\le {r}$.",
       r"Với $x={x0}$, số nguyên $y$ lớn nhất để $({x0};y)$ là nghiệm của bất phương trình là", "floor((r-p*x0)/q)",
       [N("ceiling((r-p*x0)/q)", "nham-dieu-kien", "Làm tròn lên (không thỏa bất phương trình)"),
        N("floor((r+p*x0)/q)", "sai-tinh-toan", "Chuyển vế sai dấu số hạng $px_0$"),
        N("floor(r/q)", "sai-tinh-toan", "Quên số hạng $px_0$")],
       {"tn4": "NB", "tra-loi-ngan": "NB"}, rang_buoc=["(r-p*x0)/q-floor((r-p*x0)/q)>0"]),
    mk("MC-PT-02", "Giá trị lớn nhất trên miền nghiệm (quy hoạch tuyến tính)", "phuong-trinh-he-bpt", "pt.qhtt-max",
       "pt.he-bpt-2-an", ["10.BPT.1"], 10,
       {"_bo": [{"s": 4, "t": 6, "al": 3, "be": 5}, {"s": 5, "t": 8, "al": 2, "be": 3}, {"s": 6, "t": 10, "al": 4, "be": 7},
                {"s": 3, "t": 5, "al": 5, "be": 6}]}, {},
       r"Một xưởng sản xuất $x$ sản phẩm loại I và $y$ sản phẩm loại II mỗi ngày ($x,y\ge0$), thỏa mãn $x+y\le {s}$ và $x+2y\le {t}$. Lợi nhuận là $F={al}x+{be}y$ (triệu đồng).",
       r"Lợi nhuận lớn nhất mỗi ngày (triệu đồng) bằng", "Max(al*s, be*t/2, al*(2*s-t)+be*(t-s))",
       [N("Max(al*s, be*t/2)", "sai-dinh-li", "Bỏ sót đỉnh là giao điểm của hai đường thẳng"),
        N("al*s+be*t/2", "nham-dieu-kien", "Cộng giá trị tại hai đỉnh trên hai trục (điểm không thuộc miền)"),
        N("Min(al*s, be*t/2, al*(2*s-t)+be*(t-s))", "sai-dinh-li", "Lấy giá trị nhỏ nhất thay vì lớn nhất")],
       {"tn4": "VD", "tra-loi-ngan": "VD"}, nang_luc="mo-hinh-hoa", rang_buoc=["s<t", "t<2*s"], duong=True),
    mk("MC-PT-03", "Bài toán hệ phương trình bậc nhất hai ẩn", "phuong-trinh-he-bpt", "pt.he-bac-nhat-thuc-te",
       "pt.he-bac-nhat", ["10.HPT.1"], 10,
       {"_bo": [{"x0": 120, "y0": 80, "pp": 50, "qq": 80}, {"x0": 35, "y0": 65, "pp": 30, "qq": 45},
                {"x0": 240, "y0": 160, "pp": 20, "qq": 35}, {"x0": 75, "y0": 125, "pp": 60, "qq": 100}]},
       {"NN": "x0+y0", "TT": "pp*x0+qq*y0"},
       r"Một buổi biểu diễn bán được ${NN}$ vé gồm hai loại: vé loại I giá ${pp}$ nghìn đồng và vé loại II giá ${qq}$ nghìn đồng; tổng tiền thu được là ${TT}$ nghìn đồng.",
       r"Số vé loại I đã bán là", "x0",
       [N("y0", "sai-dinh-li", "Nhầm ẩn: trả lời số vé loại II"),
        N("TT/(pp+qq)", "sai-dinh-li", "Chia tổng tiền cho tổng hai giá vé"),
        N("NN/2", "sai-dinh-li", "Chia đều tổng số vé cho hai loại")],
       {"tra-loi-ngan": "VDC"}, loai=("tra-loi-ngan",), nang_luc="mo-hinh-hoa", duong=True),
    # ------------------------------------------------------------------ tổ hợp, xác suất
    mk("MC-XS-01", "Xác suất có điều kiện từ bảng số liệu", "xac-suat-co-dieu-kien", "xs.dieu-kien-bang",
       "xs.dieu-kien", ["12.XS.1"], 12,
       {"_bo": [{"nAB": 12, "nAb": 8, "naB": 18, "nab": 12}, {"nAB": 15, "nAb": 10, "naB": 5, "nab": 10},
                {"nAB": 9, "nAb": 6, "naB": 21, "nab": 4}, {"nAB": 14, "nAb": 7, "naB": 6, "nab": 13}]},
       {"nB": "nAB+naB", "nA": "nAB+nAb", "NN": "nAB+nAb+naB+nab"},
       r"Một lớp có ${NN}$ học sinh. Trong đó có ${nAB}$ bạn vừa thích bóng đá vừa thích cầu lông, ${nAb}$ bạn thích bóng đá nhưng không thích cầu lông, ${naB}$ bạn thích cầu lông nhưng không thích bóng đá, còn lại không thích môn nào. Chọn ngẫu nhiên một học sinh; gọi $A$: \"thích bóng đá\", $B$: \"thích cầu lông\".",
       r"Xác suất $P(A\mid B)$ bằng", "nAB/nB",
       [N("nAB/NN", "sai-dinh-li", r"Tính $P(AB)$ thay cho $P(A\mid B)$"),
        N("nAB/nA", "sai-dinh-li", r"Đổi vai trò, tính $P(B\mid A)$"),
        N("nB/NN", "sai-dinh-li", r"Tính $P(B)$")],
       {"tn4": "TH", "tra-loi-ngan": "TH"}, duong=True),
    mk("MC-XS-02", "Công thức xác suất toàn phần", "xac-suat-co-dieu-kien", "xs.toan-phan-may", "xs.toan-phan",
       ["12.XS.2"], 12, {"p": ["3/5", "2/5", "7/10"], "u": ["1/10", "1/20"], "v": ["3/100", "1/25"]},
       {"P1": "100*p", "P2": "100-100*p", "U": "100*u", "V": "100*v"},
       r"Một nhà máy có hai dây chuyền: dây chuyền I làm ra ${P1}\%$ sản phẩm với tỉ lệ phế phẩm ${U}\%$, dây chuyền II làm ra ${P2}\%$ sản phẩm với tỉ lệ phế phẩm ${V}\%$. Lấy ngẫu nhiên một sản phẩm của nhà máy.",
       r"Xác suất sản phẩm đó là phế phẩm (làm tròn đến hàng phần trăm) bằng", "p*u+(1-p)*v",
       [N("(u+v)/2", "sai-dinh-li", "Lấy trung bình cộng hai tỉ lệ, bỏ qua tỉ trọng"),
        N("p*u", "sai-dinh-li", "Chỉ tính phế phẩm của dây chuyền I"),
        N("u+v", "sai-dinh-li", "Cộng hai tỉ lệ phế phẩm")],
       {"tn4": "VD", "tra-loi-ngan": "VDC"}, kieu="xap-xi:4", duong=True, tln_nd=(2,)),
    mk("MC-XS-03", "Xác suất hai biến cố độc lập", "xac-suat-co-dieu-kien", "xs.doc-lap-tich", "xs.doc-lap",
       ["12.XS.1"], 12, {"pa": ["0.6", "0.7", "0.8"], "pb": ["0.5", "0.9"]}, {},
       r"Cho hai biến cố độc lập $A$, $B$ với $P(A)={pa:xap-xi:2}$ và $P(B)={pb:xap-xi:2}$.",
       r"Xác suất $P(AB)$ bằng", "pa*pb",
       [N("pa+pb", "sai-dinh-li", "Cộng hai xác suất"),
        N("pa+pb-pa*pb", "sai-dinh-li", r"Nhầm sang $P(A\cup B)$"),
        N("1-pa*pb", "sai-dinh-li", "Lấy xác suất biến cố đối của $AB$")],
       {"tn4": "NB", "tra-loi-ngan": "NB"}, kieu="xap-xi:2", duong=True),
    mk("MC-XS-04", "Xác suất chọn hai viên bi cùng màu", "to-hop-xac-suat", "tk.xs-bi-cung-mau", "tk.xs-co-dien",
       ["10.XS.1", "11.XS.3"], 10, {"m": ["4", "5", "6"], "n": ["3", "5", "7"]}, {},
       r"Một hộp có ${m}$ viên bi đỏ và ${n}$ viên bi xanh. Lấy ngẫu nhiên đồng thời hai viên bi.",
       r"Xác suất để hai viên bi lấy ra cùng màu (làm tròn đến hàng phần trăm) bằng",
       "(binomial(m,2)+binomial(n,2))/binomial(m+n,2)",
       [N("(m**2+n**2)/(m+n)**2", "nham-dieu-kien", "Tính như lấy có hoàn lại"),
        N("binomial(m,2)/binomial(m+n,2)", "sai-dinh-li", "Chỉ tính trường hợp cùng đỏ"),
        N("m*n/binomial(m+n,2)", "sai-dinh-li", "Tính xác suất hai bi khác màu")],
       {"tn4": "TH", "tra-loi-ngan": "VD"}, kieu="xap-xi:2", duong=True, rang_buoc=["m!=n"]),
    mk("MC-XS-05", "Đếm số có ba chữ số khác nhau", "to-hop-xac-suat", "tk.dem-so-chu-so", "tk.dem", ["10.TH.1"], 10,
       {"n": ["5", "6", "7", "8"]}, {},
       r"Từ các chữ số $0,1,2,\dots,{n}$, lập các số tự nhiên có ba chữ số đôi một khác nhau.",
       r"Số các số lập được là", "n*n*(n-1)",
       [N("(n+1)*n*(n-1)", "nham-dieu-kien", "Quên loại trường hợp chữ số 0 đứng đầu"),
        N("n*(n+1)**2", "nham-dieu-kien", "Cho phép các chữ số lặp lại"),
        N("binomial(n+1,3)", "sai-dinh-li", "Dùng tổ hợp thay cho chỉnh hợp (bỏ thứ tự)")],
       {"tn4": "TH", "tra-loi-ngan": "VD"}, duong=True),
    mk("MC-XS-06", "Công thức cộng xác suất", "to-hop-xac-suat", "xs11.cong", "tk.xs-co-dien", ["11.XS.1"], 11,
       {"_bo": [{"pa": "0.5", "pb": "0.4", "pab": "0.1"}, {"pa": "0.6", "pb": "0.3", "pab": "0.2"},
                {"pa": "0.45", "pb": "0.35", "pab": "0.15"}, {"pa": "0.7", "pb": "0.5", "pab": "0.4"}]}, {},
       r"Cho hai biến cố $A$, $B$ với $P(A)={pa:xap-xi:2}$, $P(B)={pb:xap-xi:2}$ và $P(AB)={pab:xap-xi:2}$.",
       r"Xác suất $P(A\cup B)$ bằng", "pa+pb-pab",
       [N("pa+pb", "sai-dinh-li", r"Quên trừ $P(AB)$"),
        N("pa+pb-pa*pb", "nham-dieu-kien", "Coi $A$, $B$ độc lập khi chưa có giả thiết"),
        N("pa*pb", "sai-dinh-li", "Nhân hai xác suất")],
       {"tn4": "NB", "tra-loi-ngan": "TH"}, kieu="xap-xi:2", duong=True),
    mk("MC-XS-07", "Công thức nhân: ít nhất một lần trúng", "to-hop-xac-suat", "xs11.nhan-it-nhat",
       "tk.xs-co-dien", ["11.XS.2"], 11, {"p": ["0.6", "0.7", "0.8"], "q": ["0.5", "0.9"]}, {},
       r"Hai xạ thủ độc lập cùng bắn vào một bia, mỗi người bắn một viên. Xác suất bắn trúng của người thứ nhất là ${p:xap-xi:2}$, của người thứ hai là ${q:xap-xi:2}$.",
       r"Xác suất để bia bị trúng ít nhất một viên (làm tròn đến hàng phần trăm) bằng", "1-(1-p)*(1-q)",
       [N("p+q", "sai-dinh-li", "Cộng hai xác suất (hai biến cố không xung khắc)"),
        N("p*q", "sai-dinh-li", "Tính xác suất cả hai cùng trúng"),
        N("1-p*q", "sai-dinh-li", "Lấy biến cố đối của \"cả hai cùng trúng\"")],
       {"tn4": "TH", "tra-loi-ngan": "TH", "tu-luan": "VD"}, kieu="xap-xi:2", duong=True,
       loai=("tn4", "tra-loi-ngan", "tu-luan"),
       tl=[{"noi_dung": r"Gọi $A_1$, $A_2$ là biến cố người thứ nhất, thứ hai bắn trúng; $A_1$, $A_2$ độc lập. Biến cố đối của \"ít nhất một viên trúng\" là $\overline{A_1}\,\overline{A_2}$.", "diem": 0.5},
           {"noi_dung": r"$P(\overline{A_1}\,\overline{A_2})=P(\overline{A_1})P(\overline{A_2})=(1-{p:xap-xi:2})(1-{q:xap-xi:2})$.", "diem": 0.25},
           {"noi_dung": r"Xác suất cần tìm bằng $1-(1-{p:xap-xi:2})(1-{q:xap-xi:2})={ds}$.", "diem": 0.25}]),
    mk("MC-XS-08", "Xác suất ít nhất một lần xuất hiện mặt sáu", "to-hop-xac-suat", "xs11.it-nhat-mot-lan",
       "tk.xs-co-dien", ["11.XS.2"], 11, {"n": ["2", "3", "4"]}, {},
       r"Gieo một con xúc xắc cân đối và đồng chất ${n}$ lần liên tiếp.",
       r"Xác suất để mặt sáu chấm xuất hiện ít nhất một lần (làm tròn đến hàng phần trăm) bằng", "1-(Rational(5,6))**n",
       [N("n/6", "sai-dinh-li", "Cộng xác suất của các lần gieo (các biến cố không xung khắc)"),
        N("(Rational(1,6))**n", "sai-dinh-li", "Tính xác suất mặt sáu xuất hiện ở mọi lần"),
        N("1-(Rational(1,6))**n", "sai-dinh-li", "Lấy sai biến cố đối (\"không lần nào ra mặt sáu\" thành \"mọi lần ra mặt sáu\")")],
       {"tn4": "TH", "tra-loi-ngan": "TH", "tu-luan": "VD"}, kieu="xap-xi:2", duong=True,
       loai=("tn4", "tra-loi-ngan", "tu-luan"),
       tl=[{"noi_dung": r"Gọi $A_i$: \"lần thứ $i$ ra mặt sáu\"; các $A_i$ độc lập. Biến cố đối của \"ít nhất một lần ra mặt sáu\" là \"không lần nào ra mặt sáu\".", "diem": 0.5},
           {"noi_dung": r"Xác suất không lần nào ra mặt sáu là $\left(\dfrac56\right)^{{n}}$ (công thức nhân).", "diem": 0.25},
           {"noi_dung": r"Xác suất cần tìm bằng $1-\left(\dfrac56\right)^{{n}}\approx{ds}$.", "diem": 0.25}]),
]

# ---------------------------------------------------------------- câu đúng/sai (ý tự sinh)
DUNG_SAI = [
    {"id": "MC-DS-HS", "ten": "Đúng/sai: hàm bậc ba", "chu_de": "ham-so-ung-dung-dao-ham", "dang": "ds.ham-bac-ba",
     "dang_tx": "hs.dao-ham-dung-sai", "yccd": ["12.HS.1", "12.HS.2"], "lop": 12, "ho_hinh": "MC-DS-HS",
     "tham_so": {"m": ["1", "2"], "c": ["1", "4", "-2"]}, "dan_xuat": {"fx": "x**3-3*m*x**2+c"},
     "boi_canh": r"Cho hàm số $f(x)={fx}$.",
     "y": [{"menh_de": r"$f'(1)={v}$.", "dap_so": "3-6*m", "muc_do": "NB",
            "nhieu": [N("3-3*m", "sai-tinh-toan", r"Lấy đạo hàm của $3mx^2$ là $3mx$")]},
           {"menh_de": r"Hàm số nghịch biến trên khoảng $(0;{v})$.", "dap_so": "2*m", "muc_do": "TH",
            "nhieu": [N("m", "sai-tinh-toan", r"Giải sai phương trình $f'(x)=0$ (được $x=m$)")]},
           {"menh_de": r"Giá trị cực tiểu của hàm số bằng ${v}$.", "dap_so": "c-4*m**3", "muc_do": "TH",
            "nhieu": [N("c", "sai-dinh-li", "Nhầm giá trị cực đại $f(0)$ với giá trị cực tiểu")]},
           {"menh_de": r"Giá trị lớn nhất của $f(x)$ trên đoạn $[-1;1]$ bằng ${v}$.", "dap_so": "c", "muc_do": "VD",
            "nhieu": [N("1-3*m+c", "sai-dinh-li", "Chỉ lấy giá trị tại đầu mút phải $f(1)$")]}]},
    {"id": "MC-DS-UD", "ten": "Đúng/sai: vận tốc - quãng đường", "chu_de": "ung-dung-tich-phan",
     "dang": "ds.van-toc-quang-duong", "dang_tx": "ud.van-toc-quang-duong", "yccd": ["12.UD.3", "12.NH.2"], "lop": 12,
     "ho_hinh": "MC-DS-UD", "tham_so": {"p": ["10", "12", "20", "15"], "q": ["2", "4", "5"]}, "dan_xuat": {"vt": "p-q*t"},
     "boi_canh": r"Một ô tô đang chạy thì người lái đạp phanh; từ thời điểm đó ô tô chuyển động chậm dần đều với vận tốc $v(t)={vt}$ (m/s), $t$ tính bằng giây.",
     "y": [{"menh_de": r"Vận tốc của ô tô lúc bắt đầu đạp phanh là ${v}$ m/s.", "dap_so": "p", "muc_do": "NB",
            "nhieu": [N("q", "sai-dinh-li", "Nhầm gia tốc với vận tốc ban đầu")]},
           {"menh_de": r"Ô tô dừng hẳn sau ${v}$ giây kể từ lúc đạp phanh.", "dap_so": "p/q", "muc_do": "TH",
            "nhieu": [N("q/p", "sai-tinh-toan", "Giải sai phương trình $v(t)=0$")]},
           {"menh_de": r"Quãng đường ô tô đi được từ lúc đạp phanh đến khi dừng là ${v}$ m.", "dap_so": "p**2/(2*q)",
            "muc_do": "VD", "nhieu": [N("p**2/q", "sai-dinh-li", r"Quên chia 2 khi lấy nguyên hàm của $qt$")]},
           {"menh_de": r"Trong giây đầu tiên sau khi đạp phanh, ô tô đi được ${v}$ m.", "dap_so": "p-q/2",
            "muc_do": "VD", "nhieu": [N("p-q", "sai-dinh-li", "Lấy $v(1)$ thay cho quãng đường")]}]},
    {"id": "MC-DS-XS", "ten": "Đúng/sai: xác suất toàn phần và Bayes", "chu_de": "xac-suat-co-dieu-kien",
     "dang": "ds.bayes", "dang_tx": "xs.dieu-kien", "yccd": ["12.XS.1", "12.XS.2"], "lop": 12, "ho_hinh": "MC-DS-XS",
     "tham_so": {"p": ["3/5", "2/5", "7/10"], "u": ["1/10", "1/20"], "v": ["1/25", "3/100"]},
     "dan_xuat": {"P1": "100*p", "U": "100*u", "V": "100*v"},
     "boi_canh": r"Một bệnh viện nhận bệnh nhân từ hai khu vực: khu vực I chiếm ${P1}\%$; tỉ lệ mắc bệnh X ở khu vực I là ${U}\%$, ở khu vực II là ${V}\%$. Chọn ngẫu nhiên một bệnh nhân; gọi $H$: \"bệnh nhân đến từ khu vực I\", $A$: \"bệnh nhân mắc bệnh X\".",
     "kieu": "xap-xi:4",
     "y": [{"menh_de": r"$P(\overline H)={v}$.", "dap_so": "1-p", "muc_do": "NB",
            "nhieu": [N("p", "sai-dinh-li", r"Nhầm $P(\overline H)$ với $P(H)$")]},
           {"menh_de": r"$P(AH)={v}$.", "dap_so": "p*u", "muc_do": "TH",
            "nhieu": [N("u", "sai-dinh-li", r"Quên nhân $P(H)$: lấy $P(A\mid H)$")]},
           {"menh_de": r"$P(A)={v}$.", "dap_so": "p*u+(1-p)*v", "muc_do": "VD",
            "nhieu": [N("(u+v)/2", "sai-dinh-li", "Lấy trung bình cộng không theo tỉ trọng")]},
           {"menh_de": r"$P(H\mid A)={v}$ (làm tròn đến hàng phần nghìn).", "dap_so": "p*u/(p*u+(1-p)*v)", "muc_do": "VD",
            "nhieu": [N("u", "sai-dinh-li", r"Nhầm $P(H\mid A)$ với $P(A\mid H)$")], "kieu": "xap-xi:3"}]},
    {"id": "MC-DS-ML", "ten": "Đúng/sai: mô hình tăng trưởng mũ", "chu_de": "ham-so-mu-logarit",
     "dang": "ds.mo-hinh-mu", "dang_tx": "ml.mo-hinh", "yccd": ["11.ML.4", "11.ML.3"], "lop": 11, "ho_hinh": "MC-DS-ML",
     "tham_so": {"N0": ["100", "500", "1000"], "T": ["2", "3", "5"], "k": ["3", "4"]}, "dan_xuat": {"M": "2**k", "kT": "k*T"},
     "boi_canh": r"Số lượng vi khuẩn trong một mẻ nuôi cấy sau $t$ giờ là $N(t)={N0}\cdot 2^{\frac{t}{{T}}}$.",
     "y": [{"menh_de": r"Số vi khuẩn ban đầu là ${v}$.", "dap_so": "N0", "muc_do": "NB",
            "nhieu": [N("2*N0", "sai-tinh-toan", r"Tính $2^0=2$")]},
           {"menh_de": r"Sau ${T}$ giờ, số vi khuẩn là ${v}$.", "dap_so": "2*N0", "muc_do": "TH",
            "nhieu": [N("N0*T", "sai-dinh-li", "Coi số vi khuẩn tăng tuyến tính theo thời gian")]},
           {"menh_de": r"Sau ${kT}$ giờ, số vi khuẩn là ${v}$.", "dap_so": "N0*2**k", "muc_do": "TH",
            "nhieu": [N("N0*2*k", "sai-dinh-li", r"Nhân $2k$ thay cho lũy thừa $2^k$")]},
           {"menh_de": r"Thời gian để số vi khuẩn gấp ${M}$ lần ban đầu là ${v}$ giờ.", "dap_so": "k*T", "muc_do": "VD",
            "nhieu": [N("M*T/2", "sai-dinh-li", "Coi tăng tuyến tính: gấp đôi sau $T$ giờ thì gấp $M$ lần sau $\\dfrac{MT}{2}$ giờ")]}]},
    {"id": "MC-DS-OX", "ten": "Đúng/sai: mặt cầu và mặt phẳng", "chu_de": "oxyz-mat-cau", "dang": "ds.mat-cau-mat-phang",
     "dang_tx": "ox.mc-vdc", "yccd": ["12.OX.4", "12.OX.2"], "lop": 12, "ho_hinh": "MC-DS-OX",
     "tham_so": {"_bo": [{"i1": 1, "i2": -2, "i3": 3, "R": 5, "dd": 4}, {"i1": 2, "i2": 1, "i3": -1, "R": 3, "dd": -6},
                         {"i1": 0, "i2": 3, "i3": 1, "R": 4, "dd": -2}, {"i1": -1, "i2": 2, "i3": 2, "R": 5, "dd": 7}]},
     "dan_xuat": {"X": "(x-i1)**2", "Y": "(y-i2)**2", "Z": "(z-i3)**2", "RR": "R**2", "mp": "x+2*y+2*z+dd",
                  "dist": "Abs(i1+2*i2+2*i3+dd)/3"},
     "boi_canh": r"Trong không gian $Oxyz$, cho mặt cầu $(S):{X:nguyen}+{Y:nguyen}+{Z:nguyen}={RR}$ và mặt phẳng $(P):{mp}=0$.",
     "y": [{"menh_de": r"Mặt cầu $(S)$ có tâm $I{v}$.", "dap_so": "Tuple(i1,i2,i3)", "muc_do": "NB", "kieu": "vecto",
            "nhieu": [N("Tuple(-i1,-i2,-i3)", "sai-tinh-toan", "Lấy sai dấu tọa độ tâm")]},
           {"menh_de": r"Mặt cầu $(S)$ có bán kính bằng ${v}$.", "dap_so": "R", "muc_do": "NB",
            "nhieu": [N("R**2", "sai-tinh-toan", "Quên lấy căn bậc hai của vế phải")]},
           {"menh_de": r"Khoảng cách từ tâm $I$ đến $(P)$ bằng ${v}$.", "dap_so": "dist", "muc_do": "TH",
            "nhieu": [N("Abs(i1+2*i2+2*i3+dd)", "sai-dinh-li", "Quên chia cho độ dài vectơ pháp tuyến")]},
           {"menh_de": r"$(P)$ cắt $(S)$ theo một đường tròn có bán kính bằng ${v}$.", "dap_so": "sqrt(R**2-dist**2)",
            "muc_do": "VD", "nhieu": [N("R-dist", "sai-dinh-li", "Lấy hiệu $R-d$ thay vì dùng định lí Pythagore")]}],
     "rang_buoc": ["dist<R"]},
    {"id": "MC-DS-DH", "ten": "Đúng/sai: đạo hàm và tiếp tuyến", "chu_de": "dao-ham-co-ban", "dang": "ds.dao-ham",
     "dang_tx": "dh1.quy-tac", "yccd": ["11.DH.1", "11.DH.2", "11.DH.3"], "lop": 11, "ho_hinh": "MC-DS-DH",
     "tham_so": {"p": ["1", "-2", "3"], "q": ["2", "-1"], "x0": ["1", "2"]}, "dan_xuat": {"fx": "x**3+p*x**2+q"},
     "boi_canh": r"Cho hàm số $f(x)={fx}$.",
     "y": [{"menh_de": r"$f'(1)={v}$.", "dap_so": "3+2*p", "muc_do": "NB",
            "nhieu": [N("3+p", "sai-tinh-toan", r"Lấy đạo hàm của $px^2$ là $px$")]},
           {"menh_de": r"$f''(1)={v}$.", "dap_so": "6+2*p", "muc_do": "TH",
            "nhieu": [N("6+p", "sai-tinh-toan", r"Lấy đạo hàm cấp hai của $px^2$ là $p$")]},
           {"menh_de": r"Tiếp tuyến của đồ thị tại điểm có hoành độ ${x0}$ có hệ số góc ${v}$.", "dap_so": "3*x0**2+2*p*x0",
            "muc_do": "TH", "nhieu": [N("x0**3+p*x0**2+q", "sai-dinh-li", r"Lấy $f(x_0)$ làm hệ số góc")]},
           {"menh_de": r"Tiếp tuyến đó cắt trục $Oy$ tại điểm có tung độ ${v}$.",
            "dap_so": "x0**3+p*x0**2+q-(3*x0**2+2*p*x0)*x0", "muc_do": "VD",
            "nhieu": [N("x0**3+p*x0**2+q+(3*x0**2+2*p*x0)*x0", "sai-tinh-toan", "Sai dấu khi khai triển $y=k(x-x_0)+y_0$ tại $x=0$")]}]},
    {"id": "MC-DS-X11", "ten": "Đúng/sai: hai xạ thủ (quy tắc cộng, nhân xác suất)", "chu_de": "to-hop-xac-suat",
     "dang": "ds.xs-xa-thu", "dang_tx": "tk.xs-co-dien", "yccd": ["11.XS.1", "11.XS.2"], "lop": 11,
     "ho_hinh": "MC-DS-X11", "tham_so": {"p": ["0.6", "0.7", "0.8"], "q": ["0.5", "0.9"]}, "dan_xuat": {},
     "boi_canh": r"Hai xạ thủ độc lập cùng bắn vào một bia, mỗi người một viên, xác suất trúng lần lượt là ${p:xap-xi:2}$ và ${q:xap-xi:2}$.",
     "kieu": "xap-xi:2",
     "y": [{"menh_de": r"Xác suất cả hai cùng bắn trúng là ${v}$.", "dap_so": "p*q", "muc_do": "NB",
            "nhieu": [N("p+q-1", "sai-tinh-toan", "Trừ thay vì nhân khi tính xác suất giao")]},
           {"menh_de": r"Xác suất không ai bắn trúng là ${v}$.", "dap_so": "(1-p)*(1-q)", "muc_do": "TH",
            "nhieu": [N("1-p*q", "sai-dinh-li", "Lấy biến cố đối của \"cả hai cùng trúng\"")]},
           {"menh_de": r"Xác suất có đúng một người bắn trúng là ${v}$.", "dap_so": "p*(1-q)+q*(1-p)", "muc_do": "VD",
            "nhieu": [N("p*(1-q)", "sai-dinh-li", "Chỉ tính trường hợp người thứ nhất trúng")]},
           {"menh_de": r"Xác suất bia bị trúng ít nhất một viên là ${v}$.", "dap_so": "1-(1-p)*(1-q)", "muc_do": "VD",
            "nhieu": [N("p+q", "sai-dinh-li", "Cộng hai xác suất (hai biến cố không xung khắc)")]}]},
]


# ---------------------------------------------------------------- kiểm độc lập (tính lại bằng cách khác)
def _f(e):
    return float(e)


def _mean(E):
    ns = [int(E["n%d" % i]) for i in range(1, 6)]
    x0, d = _f(E["x0"]), _f(E["d"])
    vals = []
    for i, n in enumerate(ns):
        vals += [x0 + d * i + d / 2] * n  # khai triển mẫu theo giá trị đại diện
    return sum(vals) / len(vals)


def _mode(E):
    ns = [int(E["n%d" % i]) for i in range(1, 6)]
    x0, d = _f(E["x0"]), _f(E["d"])
    m = max(range(5), key=lambda i: ns[i])
    lo = ns[m - 1] if m > 0 else 0
    hi = ns[m + 1] if m < 4 else 0
    return x0 + m * d + (ns[m] - lo) / ((ns[m] - lo) + (ns[m] - hi)) * d


def _simpson(f, lo, hi, n=2000):
    h = (hi - lo) / n
    s = f(lo) + f(hi) + sum((4 if i % 2 else 2) * f(lo + i * h) for i in range(1, n))
    return s * h / 3


def _dmax(f, lo, hi, n=200000):
    return max(f(lo + (hi - lo) * i / n) for i in range(n + 1))


def _qhtt2(E):
    """Duyệt mọi giao điểm của các cặp đường biên, giữ điểm thuộc miền nghiệm, lấy F lớn nhất (độc lập công thức)."""
    s, t, al, be = (Fr(str(E[k])) for k in ("s", "t", "al", "be"))
    lines = [(1, 0, 0), (0, 1, 0), (1, 1, s), (1, 2, t)]  # a·x + b·y = c
    best = None
    for (a1, b1, c1), (a2, b2, c2) in itertools.combinations(lines, 2):
        det = a1 * b2 - a2 * b1
        if det == 0:
            continue
        x = Fr(c1 * b2 - c2 * b1) / det
        y = Fr(a1 * c2 - a2 * c1) / det
        if x >= 0 and y >= 0 and x + y <= s and x + 2 * y <= t:
            v = al * x + be * y
            best = v if best is None or v > best else best
    return float(best)


def _bi(E):
    m, n = int(E["m"]), int(E["n"])
    balls = ["d"] * m + ["x"] * n
    pairs = list(itertools.combinations(range(m + n), 2))
    return sum(1 for i, j in pairs if balls[i] == balls[j]) / len(pairs)


def _dem(E):
    n = int(E["n"])
    return sum(1 for t in itertools.permutations(range(n + 1), 3) if t[0] != 0)


def _lai(E):
    r = _f(E["r"]) / 100
    v, k = 1.0, 0
    while v <= 2:
        v *= 1 + r
        k += 1
    return k


def _bpt_log(E):
    b, t, c = int(E["b"]), int(E["t"]), int(E["c"])
    return sum(1 for x in range(c - 50, c + 200) if x - c > 0 and math.log(x - c, b) < t - 1e-12)


def _xs_tp(E):
    p, u, v = Fr(str(E["p"])), Fr(str(E["u"])), Fr(str(E["v"]))
    return float(p * u + (1 - p) * v)


def _shoot(E):
    p, q = _f(E["p"]), _f(E["q"])
    tot = 0.0
    for a, b in itertools.product([0, 1], repeat=2):  # liệt kê 4 kết cục
        pr = (p if a else 1 - p) * (q if b else 1 - q)
        if a or b:
            tot += pr
    return tot


def _xx(E):
    n = int(E["n"])
    om = list(itertools.product(range(1, 7), repeat=n))
    return sum(1 for w in om if 6 in w) / len(om)


def _ml07(E):
    b, p, q, r = (_f(E[k]) for k in ("b", "p", "q", "r"))
    xx = (r - q) / (p - 1)
    return xx if abs(b ** (p * xx + q) - b ** (xx + r)) < 1e-9 * b ** (xx + r) else None


KIEM = {
    "MC-ML-06": lambda E: math.log((2.0 ** _f(E["u"])) ** _f(E["q"])) / math.log(2.0 ** _f(E["p"])),
    "MC-ML-07": _ml07, "MC-XS-08": _xx,
    "MC-DH-04": lambda E: (lambda f, x0, h: (f(x0 + h) - f(x0 - h)) / (2 * h))(
        lambda x: math.sqrt(_f(E["p"]) * x + _f(E["q"])), _f(E["x0"]), 1e-6),
    "MC-DH-05": lambda E: (lambda f, x0, h: (f(x0 + h) - f(x0 - h)) / (2 * h))(
        lambda x: x ** 3 - _f(E["p"]) * x ** 2 + _f(E["q"]) * x, _f(E["t0"]), 1e-5),
    "MC-TK-01": _mean, "MC-TK-02": _mode,
    "MC-TK-03": lambda E: (_f(E["x0"]) + 5 * _f(E["d"])) - _f(E["x0"]),
    "MC-NH-01": lambda E: None,  # kiểm bằng đạo hàm (check.py: diff(dap_so) == f)
    "MC-NH-02": lambda E: _simpson(lambda x: _f(E["p"]) * x + _f(E["q"]), _f(E["m"]), _f(E["n"])),
    "MC-NH-03": lambda E: _f(E["al"]) * _f(E["u"]) + _f(E["be"]) * _f(E["v"]),
    "MC-DS-01": lambda E: [(_f(E["u1"]) + i * _f(E["d"])) for i in range(int(E["n"]))][-1],
    "MC-DS-02": lambda E: [(_f(E["u1"]) * _f(E["q"]) ** i) for i in range(int(E["n"]))][-1],
    "MC-DS-03": lambda E: sum(_f(E["u1"]) + i * _f(E["d"]) for i in range(int(E["n"]))),
    "MC-ML-01": lambda E: (_f(E["b"]) ** _f(E["r"]) - _f(E["q"])) / _f(E["p"]) if abs(math.log(_f(E["p"]) * ((_f(E["b"]) ** _f(E["r"]) - _f(E["q"])) / _f(E["p"])) + _f(E["q"]), _f(E["b"])) - _f(E["r"])) < 1e-9 else None,
    "MC-ML-02": lambda E: math.log(_f(E["b"]) ** _f(E["t"]), _f(E["b"])) - _f(E["q"]),
    "MC-ML-03": lambda E: math.log(2.0 ** (_f(E["p"]) * _f(E["u"])) * 2.0 ** (_f(E["q"]) * _f(E["v"])), 2.0),
    "MC-ML-04": _lai, "MC-ML-05": _bpt_log,
    "MC-HS-01": lambda E: -_f(E["s"]) / _f(E["r"]),
    "MC-HS-02": lambda E: max((x ** 3 - 3 * _f(E["p"]) * x + _f(E["q"])) for x in [-math.sqrt(_f(E["p"]))]),
    "MC-HS-03": lambda E: min(x ** 3 - 3 * x + _f(E["c"]) for x in [i / 100000 * _f(E["n"]) for i in range(100001)]),
    "MC-HS-04": lambda E: _dmax(lambda x: x * (_f(E["Lc"]) - 2 * x) ** 2, 0, _f(E["Lc"]) / 2),
    "MC-HS-05": lambda E: _dmax(lambda x: x * (_f(E["pp"]) - _f(E["kk"]) * x) - _f(E["cc"]) * x - _f(E["F"]), 0, _f(E["pp"]) / _f(E["kk"])),
    "MC-UD-01": lambda E: _simpson(lambda x: _f(E["m"]) * x - x * x, 0, _f(E["m"])),
    "MC-UD-02": lambda E: math.pi * _simpson(lambda x: _f(E["k"]) * x, 0, _f(E["t"])),
    "MC-OX-01": None, "MC-OX-02": None, "MC-OX-03": None, "MC-OX-04": None,
    "MC-OX-05": lambda E: sum(_f(E["u%d" % i]) * _f(E["v%d" % i]) for i in (1, 2, 3)),
    "MC-OX-06": lambda E: math.dist([_f(E["i1"]), _f(E["i2"]), _f(E["i3"])], [_f(E["m1"]), _f(E["m2"]), _f(E["m3"])]),
    "MC-LG-01": lambda E: min(x for x in [math.asin(_f(E["s"])), math.pi - math.asin(_f(E["s"]))] if x > 0),
    "MC-DH-01": lambda E: (lambda f, x0, h: (f(x0 + h) - f(x0 - h)) / (2 * h))(
        lambda x: _f(E["p"]) * x ** 3 + _f(E["q"]) * x ** 2 + _f(E["r"]) * x, _f(E["x0"]), 1e-5),
    "MC-DH-02": None,
    "MC-DH-03": lambda E: (lambda f, x0, h: (f(x0 + h) - 2 * f(x0) + f(x0 - h)) / h ** 2)(
        lambda x: _f(E["p"]) * x ** 4 + _f(E["q"]) * x ** 3, _f(E["x0"]), 1e-4),
    "MC-PT-01": lambda E: max(y for y in range(-100, 100) if _f(E["p"]) * _f(E["x0"]) + _f(E["q"]) * y <= _f(E["r"])),
    "MC-PT-02": _qhtt2,
    "MC-PT-03": lambda E: next(x for x in range(0, 2000) if (lambda y: y >= 0 and _f(E["pp"]) * x + _f(E["qq"]) * y == _f(E["TT"]))(_f(E["NN"]) - x)),
    "MC-XS-01": lambda E: _f(E["nAB"]) / (_f(E["nAB"]) + _f(E["naB"])),
    "MC-XS-02": _xs_tp,
    "MC-XS-03": lambda E: _f(E["pa"]) * _f(E["pb"]),
    "MC-XS-04": _bi, "MC-XS-05": _dem,
    "MC-XS-06": lambda E: _f(E["pa"]) + _f(E["pb"]) - _f(E["pab"]),
    "MC-XS-07": _shoot,
}
# sai số cho phép của các kiểm bằng phương pháp số (mặc định 1e-6)
KIEM_TOL = {"MC-DH-04": 1e-6, "MC-DH-05": 1e-6, "MC-HS-03": 1e-6, "MC-HS-04": 1e-4, "MC-HS-05": 1e-4, "MC-DH-01": 1e-6, "MC-DH-03": 1e-4,
            "MC-PT-02": 1e-6}
