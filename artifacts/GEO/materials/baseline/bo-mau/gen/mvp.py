"""30 bài từ Kho đề MVP (catalog-mvp.json v1.2 · 27/11/2026); dap_so_engine lấy từ engine-values.json
(engine thật DefaultGeometryEngine.measure tại a = 1, biên dịch từ nguồn hiện hành 27/11/2026)."""
import json, os
from math import sqrt, pi, atan, degrees
from _lib import S, W, build

HERE = os.path.dirname(os.path.abspath(__file__))
ENG = json.load(open(os.path.join(HERE, "..", "engine-values.json"), encoding="utf-8"))


def E(code, name=""):
    for q in ENG[code]:
        if q["name"] == name:
            return q["v"]
    raise KeyError((code, name))


def src(code):
    return {"loai": "mvp", "ref": f"app/server/src/main/resources/content/catalog-mvp.json#{code} (v1.2 · 27/11/2026) + geometry/problem-specs.json#{code} (v1.1)"}


CHA = "Mặt cầu"
items = []
add = items.append

# ---------------- GÓC ----------------
add(build("BM-001", src("HKN-01"), "thpt", "Góc giữa đường thẳng và mặt phẳng", "TH",
    "Cho hình chóp $S.ABCD$ có đáy $ABCD$ là hình vuông cạnh $a$, $SA\\perp(ABCD)$ và $SA=a\\sqrt6$. Tính góc giữa đường thẳng $SC$ và mặt phẳng $(ABCD)$.",
    "$60^\\circ$", 60.0, E("HKN-01"),
    ["Góc giữa đường thẳng và mặt phẳng là góc giữa nó và hình chiếu của nó", "Tỉ số lượng giác trong tam giác vuông"],
    [S("$SA\\perp(ABCD)$ nên $AC$ là hình chiếu của $SC$ lên $(ABCD)$; góc cần tìm là $\\widehat{SCA}$.", "$(SC,(ABCD))=\\widehat{SCA}$", "Hình chiếu vuông góc của đường xiên", 0.25),
     S("$AC$ là đường chéo hình vuông cạnh $a$: $AC=a\\sqrt2$.", "$AC=a\\sqrt2$", "Đường chéo hình vuông", 0.25, so=sqrt(2)),
     S("$\\tan\\widehat{SCA}=\\dfrac{SA}{AC}=\\dfrac{a\\sqrt6}{a\\sqrt2}=\\sqrt3\\Rightarrow\\widehat{SCA}=60^\\circ$.", "$\\tan\\widehat{SCA}=\\sqrt3$, góc $60^\\circ$", "tan trong tam giác vuông", 0.5, so=60.0)],
    [W("B1", "sai-dinh-li", "Lấy góc $\\widehat{ASC}$ (góc giữa $SC$ và đường vuông góc $SA$) làm góc với mặt phẳng", {"B1": "Góc giữa $SC$ và $(ABCD)$ là $\\widehat{ASC}$.", "B3": "$\\tan\\widehat{ASC}=\\dfrac{AC}{SA}=\\dfrac{1}{\\sqrt3}\\Rightarrow$ góc $30^\\circ$."}, dd=0.25),
     W("B2", "sai-tinh-toan", "Tính sai $AC=a$ (nhầm đường chéo với cạnh)", {"B2": "$AC=a$.", "B3": "$\\tan\\widehat{SCA}=\\dfrac{a\\sqrt6}{a}=\\sqrt6\\Rightarrow\\widehat{SCA}\\approx67{,}79^\\circ$."})],
    ghi_chu="S1: B2 ($AC=a\\sqrt2$) vẫn đúng và độc lập nên được 0,25."))

add(build("BM-002", src("HKN-07"), "thpt", "Góc giữa đường thẳng và mặt phẳng", "VD",
    "Cho hình chóp $S.ABCD$ có đáy $ABCD$ là hình vuông cạnh $a$, $SA\\perp(ABCD)$ và $SA=a\\sqrt2$. Tính góc giữa đường thẳng $SC$ và mặt phẳng $(SAB)$.",
    "$30^\\circ$", 30.0, E("HKN-07"),
    ["Đường thẳng vuông góc mặt phẳng", "Góc giữa đường thẳng và mặt phẳng", "Định lí Pythagore"],
    [S("$BC\\perp AB$, $BC\\perp SA$ nên $BC\\perp(SAB)$.", "$BC\\perp(SAB)$", "Dấu hiệu đường thẳng vuông góc mặt phẳng", 0.25),
     S("Hình chiếu của $C$ lên $(SAB)$ là $B$, nên hình chiếu của $SC$ là $SB$; góc cần tìm là $\\widehat{CSB}$.", "$(SC,(SAB))=\\widehat{CSB}$", "Hình chiếu vuông góc", 0.25),
     S("$SC=\\sqrt{SA^2+AC^2}=\\sqrt{2a^2+2a^2}=2a$.", "$SC=2a$", "Pythagore trong tam giác $SAC$", 0.25, ck=["$SB=a\\sqrt3$, $SC=\\sqrt{SB^2+BC^2}=2a$"], so=2.0),
     S("$\\sin\\widehat{CSB}=\\dfrac{BC}{SC}=\\dfrac a{2a}=\\dfrac12\\Rightarrow\\widehat{CSB}=30^\\circ$.", "$30^\\circ$", "sin trong tam giác vuông tại $B$", 0.25, ck=["$\\tan\\widehat{CSB}=\\dfrac{BC}{SB}=\\dfrac1{\\sqrt3}$"], so=30.0)],
    [W("B2", "sai-hinh-dung", "Chọn nhầm góc $\\widehat{CSA}$ (hình chiếu lên đáy thay vì lên $(SAB)$)", {"B2": "Góc cần tìm là $\\widehat{CSA}$.", "B4": "$\\cos\\widehat{CSA}=\\dfrac{SA}{SC}=\\dfrac{\\sqrt2}{2}\\Rightarrow 45^\\circ$."}, dd=0.5),
     W("B3", "sai-tinh-toan", "Tính $SC=\\sqrt{SA^2+AB^2}=a\\sqrt3$ (dùng nhầm $AB$ thay $AC$)", {"B3": "$SC=\\sqrt{SA^2+AB^2}=a\\sqrt3$.", "B4": "$\\sin\\widehat{CSB}=\\dfrac{a}{a\\sqrt3}\\Rightarrow\\widehat{CSB}\\approx35{,}26^\\circ$."})],
    ghi_chu="S1: B1 và B3 đúng, độc lập với góc chọn nhầm nên được 0,5."))

add(build("BM-003", src("HKN-10"), "thpt", "Góc giữa đường thẳng và mặt phẳng", "TH",
    "Cho hình chóp tứ giác đều $S.ABCD$ có cạnh đáy $a$, $O$ là tâm đáy và $SO=\\dfrac{a\\sqrt6}{2}$. Tính góc giữa cạnh bên $SA$ và mặt đáy.",
    "$60^\\circ$", 60.0, E("HKN-10"),
    ["Chân đường cao hình chóp đều là tâm đáy", "Góc giữa đường thẳng và mặt phẳng"],
    [S("$SO\\perp(ABCD)$ nên hình chiếu của $SA$ là $OA$; góc cần tìm là $\\widehat{SAO}$.", "$\\widehat{SAO}$", "Hình chiếu vuông góc", 0.25),
     S("$OA=\\dfrac{AC}2=\\dfrac{a\\sqrt2}2$.", "$OA=\\dfrac{a\\sqrt2}{2}$", "Đường chéo hình vuông", 0.25, so=sqrt(2)/2),
     S("$\\tan\\widehat{SAO}=\\dfrac{SO}{OA}=\\dfrac{a\\sqrt6/2}{a\\sqrt2/2}=\\sqrt3\\Rightarrow 60^\\circ$.", "$60^\\circ$", "tan trong tam giác vuông", 0.5, so=60.0)],
    [W("B2", "sai-tinh-toan", "Lấy $OA=a\\sqrt2$ (quên chia đôi đường chéo)", {"B2": "$OA=a\\sqrt2$.", "B3": "$\\tan\\widehat{SAO}=\\dfrac{a\\sqrt6/2}{a\\sqrt2}=\\dfrac{\\sqrt3}2\\Rightarrow\\approx40{,}89^\\circ$."}),
     W("B1", "sai-dinh-li", "Lấy góc ở đỉnh $\\widehat{ASO}$ làm góc giữa $SA$ và đáy", {"B1": "Góc cần tìm là $\\widehat{ASO}$.", "B3": "$\\tan\\widehat{ASO}=\\dfrac{OA}{SO}=\\dfrac1{\\sqrt3}\\Rightarrow 30^\\circ$."}, dd=0.25)],
    ghi_chu="S2: B2 đúng, độc lập nên được 0,25."))

add(build("BM-004", src("HKN-19"), "thpt", "Góc giữa hai đường thẳng", "TH",
    "Cho hình lập phương $ABCD.A'B'C'D'$ cạnh $a$. Tính góc giữa hai đường thẳng $AB'$ và $AD'$.",
    "$60^\\circ$", 60.0, E("HKN-19"),
    ["Đường chéo mặt hình lập phương", "Góc giữa hai đường thẳng thuộc $[0^\\circ;90^\\circ]$"],
    [S("$AB'$, $AD'$, $B'D'$ là đường chéo của ba mặt hình vuông cạnh $a$ nên đều bằng $a\\sqrt2$.", "$AB'=AD'=B'D'=a\\sqrt2$", "Đường chéo hình vuông", 0.5, so=sqrt(2)),
     S("Tam giác $AB'D'$ đều nên $\\widehat{B'AD'}=60^\\circ$; đó là góc giữa hai đường thẳng.", "$60^\\circ$", "Tam giác đều", 0.5, ck=["Dùng tọa độ: $\\cos=\\dfrac{|\\vec{AB'}\\cdot\\vec{AD'}|}{|\\vec{AB'}||\\vec{AD'}|}=\\dfrac12$"], so=60.0)],
    [W("B1", "sai-hinh-dung", "Coi $B'D'$ là đường chéo của khối ($a\\sqrt3$)", {"B1": "$AB'=AD'=a\\sqrt2$, $B'D'=a\\sqrt3$.", "B2": "$\\cos\\widehat{B'AD'}=\\dfrac{2a^2+2a^2-3a^2}{2\\cdot2a^2}=\\dfrac14\\Rightarrow\\approx75{,}52^\\circ$."}),
     W("B2", "nham-dieu-kien", "Lấy góc giữa hai vectơ $\\vec{B'A},\\vec{AD'}$ ($120^\\circ$) mà không đưa về $[0^\\circ;90^\\circ]$", {"B2": "Góc giữa hai vectơ $\\vec{B'A}$ và $\\vec{AD'}$ là $180^\\circ-60^\\circ=120^\\circ$, vậy góc giữa hai đường thẳng là $120^\\circ$."})]))

add(build("BM-005", src("HKN-26"), "thpt", "Góc giữa đường thẳng và mặt phẳng", "TH",
    "Cho hình hộp chữ nhật $ABCD.A'B'C'D'$ có $AB=a$, $AD=a\\sqrt3$, $AA'=2a$. Tính góc giữa đường chéo $AC'$ và mặt phẳng $(ABCD)$.",
    "$45^\\circ$", 45.0, E("HKN-26"),
    ["Góc giữa đường thẳng và mặt phẳng", "Định lí Pythagore"],
    [S("$CC'\\perp(ABCD)$ nên hình chiếu của $AC'$ là $AC$; góc cần tìm là $\\widehat{C'AC}$.", "$\\widehat{C'AC}$", "Hình chiếu vuông góc", 0.25),
     S("$AC=\\sqrt{AB^2+AD^2}=\\sqrt{a^2+3a^2}=2a$.", "$AC=2a$", "Pythagore", 0.25, so=2.0),
     S("$CC'=2a=AC$ nên tam giác $ACC'$ vuông cân tại $C$, $\\widehat{C'AC}=45^\\circ$.", "$45^\\circ$", "Tam giác vuông cân / tan", 0.5, so=45.0)],
    [W("B2", "sai-tinh-toan", "Lấy $AC=AD=a\\sqrt3$ (nhầm đường chéo với cạnh)", {"B2": "$AC=a\\sqrt3$.", "B3": "$\\tan\\widehat{C'AC}=\\dfrac{2a}{a\\sqrt3}\\Rightarrow\\approx49{,}11^\\circ$."}),
     W("B1", "sai-dinh-li", "Lấy góc $\\widehat{C'AB}$ (với một cạnh đáy) làm góc với mặt phẳng", {"B1": "Góc cần tìm là $\\widehat{C'AB}$.", "B3": "$AC'=2a\\sqrt2$, $\\cos\\widehat{C'AB}=\\dfrac{AB}{AC'}=\\dfrac1{2\\sqrt2}\\Rightarrow\\approx69{,}30^\\circ$."}, dd=0.25)]))

add(build("BM-006", src("HKN-35"), "thpt", "Góc giữa đường thẳng và mặt phẳng", "TH",
    "Cho lăng trụ đứng $ABC.A'B'C'$ có đáy là tam giác vuông tại $A$, $AB=a$, $AC=a\\sqrt3$, $AA'=2a$. Tính góc giữa đường thẳng $B'C$ và mặt phẳng $(ABC)$.",
    "$45^\\circ$", 45.0, E("HKN-35"),
    ["Lăng trụ đứng: cạnh bên vuông góc đáy", "Góc giữa đường thẳng và mặt phẳng"],
    [S("$BB'\\perp(ABC)$ nên hình chiếu của $B'C$ là $BC$; góc cần tìm là $\\widehat{B'CB}$.", "$\\widehat{B'CB}$", "Hình chiếu vuông góc", 0.25),
     S("$BC=\\sqrt{AB^2+AC^2}=\\sqrt{a^2+3a^2}=2a$.", "$BC=2a$", "Pythagore", 0.25, so=2.0),
     S("$BB'=BC=2a$ nên $\\tan\\widehat{B'CB}=1$, góc bằng $45^\\circ$.", "$45^\\circ$", "tan", 0.5, so=45.0)],
    [W("B2", "sai-tinh-toan", "Trừ thay vì cộng: $BC=\\sqrt{3a^2-a^2}=a\\sqrt2$", {"B2": "$BC=\\sqrt{AC^2-AB^2}=a\\sqrt2$.", "B3": "$\\tan\\widehat{B'CB}=\\dfrac{2a}{a\\sqrt2}=\\sqrt2\\Rightarrow\\approx54{,}74^\\circ$."}),
     W("B1", "sai-hinh-dung", "Lấy $AC$ làm hình chiếu của $B'C$ (sai chân hình chiếu của $B'$)", {"B1": "Hình chiếu của $B'C$ lên $(ABC)$ là $AC$, góc cần tìm là $\\widehat{B'CA}$.", "B3": "$AB'=a\\sqrt5$, $\\tan\\widehat{B'CA}=\\dfrac{AB'}{AC}=\\dfrac{\\sqrt5}{\\sqrt3}\\Rightarrow\\approx52{,}24^\\circ$."}, dd=0.25)]))

# ---------------- KHOẢNG CÁCH ----------------
add(build("BM-007", src("HKN-03"), "thpt", "Khoảng cách từ điểm đến mặt phẳng", "TH",
    "Cho hình chóp $S.ABCD$ có đáy là hình vuông cạnh $a$, $SA\\perp(ABCD)$ và $SA=a$. Tính khoảng cách từ $A$ đến mặt phẳng $(SBC)$.",
    "$\\dfrac{a\\sqrt2}{2}$", sqrt(2) / 2, E("HKN-03"),
    ["Dựng đường vuông góc từ điểm đến mặt phẳng", "Hệ thức lượng trong tam giác vuông"],
    [S("$BC\\perp AB$, $BC\\perp SA$ nên $BC\\perp(SAB)$.", "$BC\\perp(SAB)$", "Dấu hiệu vuông góc", 0.25),
     S("Kẻ $AH\\perp SB$; $AH\\subset(SAB)$ nên $AH\\perp BC$, do đó $AH\\perp(SBC)$, $d(A,(SBC))=AH$.", "$d=AH$", "Đường vuông góc mặt phẳng", 0.25, tc="Có dựng $AH\\perp SB$ VÀ nêu lí do $AH\\perp BC$"),
     S("Tam giác $SAB$ vuông cân tại $A$: $AH=\\dfrac{SB}2=\\dfrac{a\\sqrt2}2$.", "$AH=\\dfrac{a\\sqrt2}2$", "Hệ thức lượng $\\dfrac1{AH^2}=\\dfrac1{SA^2}+\\dfrac1{AB^2}$", 0.5, ck=["$\\dfrac1{AH^2}=\\dfrac1{a^2}+\\dfrac1{a^2}$"], so=sqrt(2)/2)],
    [W("B2", "sai-hinh-dung", "Coi $AB$ là khoảng cách vì $AB\\perp BC$", {"B2": "$AB\\perp BC$ nên $d(A,(SBC))=AB$.", "B3": "$d(A,(SBC))=a$."}),
     W("B3", "sai-tinh-toan", "Quên khai căn: từ $AH^2=\\dfrac{a^2}2$ suy ra $AH=\\dfrac a2$", {"B3": "$\\dfrac1{AH^2}=\\dfrac2{a^2}\\Rightarrow AH=\\dfrac a2$."})]))

add(build("BM-008", src("HKN-05"), "thpt", "Khoảng cách giữa hai đường thẳng chéo nhau", "VD",
    "Cho hình chóp $S.ABCD$ có đáy là hình vuông cạnh $a$, $SA\\perp(ABCD)$ và $SA=a\\sqrt3$. Tính khoảng cách giữa hai đường thẳng $AB$ và $SD$.",
    "$\\dfrac{a\\sqrt3}{2}$", sqrt(3) / 2, E("HKN-05"),
    ["Khoảng cách giữa đường thẳng và mặt phẳng song song", "Hệ thức lượng tam giác vuông"],
    [S("$AB\\parallel CD\\subset(SCD)$ nên $AB\\parallel(SCD)$ và $d(AB,SD)=d(A,(SCD))$.", "$d(AB,SD)=d(A,(SCD))$", "Đường thẳng song song mặt phẳng", 0.25),
     S("$CD\\perp(SAD)$; kẻ $AK\\perp SD$ thì $AK\\perp CD$, nên $AK\\perp(SCD)$.", "$d=AK$", "Dựng đường vuông góc", 0.25),
     S("$\\dfrac1{AK^2}=\\dfrac1{SA^2}+\\dfrac1{AD^2}=\\dfrac1{3a^2}+\\dfrac1{a^2}=\\dfrac4{3a^2}\\Rightarrow AK=\\dfrac{a\\sqrt3}2$.", "$AK=\\dfrac{a\\sqrt3}{2}$", "Hệ thức lượng", 0.5, so=sqrt(3)/2)],
    [W("B1", "sai-dinh-li", "Lấy $AD$ làm đoạn vuông góc chung ($AD$ không vuông góc với $SD$)", {"B1": "$AD\\perp AB$ và $AD$ cắt $SD$ tại $D$ nên $AD$ là đoạn vuông góc chung.", "B2": "Vậy $d(AB,SD)=AD$.", "B3": "$d(AB,SD)=a$."}),
     W("B3", "sai-tinh-toan", "Cộng phân số sai: $\\dfrac1{3a^2}+\\dfrac1{a^2}=\\dfrac2{4a^2}$", {"B3": "$\\dfrac1{AK^2}=\\dfrac1{3a^2}+\\dfrac1{a^2}=\\dfrac2{4a^2}\\Rightarrow AK=a\\sqrt2$."})]))

add(build("BM-009", src("HKN-09"), "thpt", "Khoảng cách giữa hai đường thẳng chéo nhau", "VD",
    "Cho hình chóp $S.ABCD$ có đáy là hình vuông cạnh $a$, $SA\\perp(ABCD)$, $SA=a$. Tính khoảng cách giữa hai đường thẳng chéo nhau $BD$ và $SC$.",
    "$\\dfrac{a\\sqrt6}{6}$", sqrt(6) / 6, E("HKN-09"),
    ["Đoạn vuông góc chung", "Tam giác đồng dạng"],
    [S("$BD\\perp AC$, $BD\\perp SA$ nên $BD\\perp(SAC)$ tại $O$. Kẻ $OH\\perp SC$ ($H\\in SC$) thì $OH\\perp BD$, $OH\\perp SC$: $OH$ là đoạn vuông góc chung.", "$d(BD,SC)=OH$", "Đoạn vuông góc chung", 0.5, tc="Chỉ ra được $BD\\perp(SAC)$ và $OH$ vuông góc cả hai đường"),
     S("$\\triangle CHO\\sim\\triangle CAS$ (chung góc $C$, cùng vuông) nên $\\dfrac{OH}{SA}=\\dfrac{OC}{SC}$, $OH=\\dfrac{SA\\cdot OC}{SC}$.", "$OH=\\dfrac{SA\\cdot OC}{SC}$", "Tam giác đồng dạng", 0.25, ck=["$OH=OC\\sin\\widehat{SCA}$"]),
     S("$OC=\\dfrac{a\\sqrt2}2$, $SC=a\\sqrt3$, nên $OH=\\dfrac{a\\cdot a\\sqrt2/2}{a\\sqrt3}=\\dfrac{a\\sqrt6}6$.", "$OH=\\dfrac{a\\sqrt6}6$", "Tính toán", 0.25, so=sqrt(6)/6)],
    [W("B2", "sai-dinh-li", "Lập sai tỉ số đồng dạng: $\\dfrac{OH}{OC}=\\dfrac{SA}{AC}$", {"B2": "$\\dfrac{OH}{OC}=\\dfrac{SA}{AC}\\Rightarrow OH=\\dfrac{OC\\cdot SA}{AC}$.", "B3": "$OH=\\dfrac{a\\sqrt2/2\\cdot a}{a\\sqrt2}=\\dfrac a2$."}),
     W("B3", "sai-tinh-toan", "Tính $SC=2a$ (sai Pythagore)", {"B3": "$OC=\\dfrac{a\\sqrt2}2$, $SC=2a$, $OH=\\dfrac{a\\sqrt2}4$."})]))

add(build("BM-010", src("HKN-13"), "thpt", "Khoảng cách giữa hai đường thẳng chéo nhau", "VDC",
    "Cho hình chóp tứ giác đều $S.ABCD$ có tất cả các cạnh bằng $a$. Tính khoảng cách giữa hai đường thẳng $AB$ và $SC$.",
    "$\\dfrac{a\\sqrt6}{3}$", sqrt(6) / 3, E("HKN-13"),
    ["Đưa khoảng cách hai đường chéo nhau về điểm – mặt", "Tỉ số khoảng cách", "Hệ thức lượng"],
    [S("$AB\\parallel(SCD)$ nên $d(AB,SC)=d(A,(SCD))$; $AO\\cap(SCD)=C$, $AC=2OC$ nên $d(A,(SCD))=2d(O,(SCD))$.", "$d=2d(O,(SCD))$", "Tỉ số khoảng cách", 0.25),
     S("$SO=\\sqrt{SA^2-OA^2}=\\sqrt{a^2-\\dfrac{a^2}2}=\\dfrac{a\\sqrt2}2$.", "$SO=\\dfrac{a\\sqrt2}2$", "Pythagore", 0.25, so=sqrt(2)/2),
     S("Gọi $M$ trung điểm $CD$, $OM=\\dfrac a2$; $\\dfrac1{d_O^2}=\\dfrac1{SO^2}+\\dfrac1{OM^2}=\\dfrac2{a^2}+\\dfrac4{a^2}=\\dfrac6{a^2}$, $d_O=\\dfrac{a\\sqrt6}6$.", "$d(O,(SCD))=\\dfrac{a\\sqrt6}6$", "Hệ thức lượng", 0.25, so=sqrt(6)/6),
     S("$d(AB,SC)=2\\cdot\\dfrac{a\\sqrt6}6=\\dfrac{a\\sqrt6}3$.", "$\\dfrac{a\\sqrt6}3$", "Kết luận", 0.25, so=sqrt(6)/3)],
    [W("B1", "nham-dieu-kien", "Bỏ tỉ số: coi $d(A,(SCD))=d(O,(SCD))$", {"B1": "$AB\\parallel(SCD)$ nên $d(AB,SC)=d(A,(SCD))=d(O,(SCD))$.", "B4": "$d(AB,SC)=\\dfrac{a\\sqrt6}6$."}, dd=0.5),
     W("B2", "sai-tinh-toan", "Lấy $SO=\\dfrac{a\\sqrt3}2$ (nhầm với trung đoạn)", {"B2": "$SO=\\dfrac{a\\sqrt3}2$.", "B3": "$\\dfrac1{d_O^2}=\\dfrac4{3a^2}+\\dfrac4{a^2}=\\dfrac{16}{3a^2}$, $d_O=\\dfrac{a\\sqrt3}4$.", "B4": "$d(AB,SC)=\\dfrac{a\\sqrt3}2$."})],
    ghi_chu="S1: B2, B3 đúng và độc lập với tỉ số nên được 0,5."))

add(build("BM-011", src("HKN-16"), "thpt", "Khoảng cách từ điểm đến mặt phẳng", "TH",
    "Hình chóp tứ giác đều $S.ABCD$ có cạnh đáy $6$ cm, chiều cao $SO=4$ cm. Tính khoảng cách từ tâm đáy $O$ đến mặt bên $(SBC)$.",
    "$2{,}4$ cm", 2.4, E("HKN-16"),
    ["Dựng khoảng cách qua trung điểm cạnh đáy", "Hệ thức lượng tam giác vuông"],
    [S("Gọi $M$ trung điểm $BC$ thì $BC\\perp OM$, $BC\\perp SO$ nên $BC\\perp(SOM)$; kẻ $OH\\perp SM$ thì $OH\\perp(SBC)$.", "$d=OH$", "Dựng đường vuông góc", 0.5),
     S("$OM=3$ cm, $SM=\\sqrt{4^2+3^2}=5$ cm.", "$OM=3$, $SM=5$", "Pythagore", 0.25, so=5.0),
     S("$OH=\\dfrac{SO\\cdot OM}{SM}=\\dfrac{12}5=2{,}4$ cm.", "$OH=2{,}4$", "Hệ thức lượng", 0.25, so=2.4)],
    [W("B1", "sai-hinh-dung", "Lấy $OM$ làm khoảng cách từ $O$ đến $(SBC)$", {"B1": "$OM\\perp BC$ nên $d(O,(SBC))=OM$.", "B3": "$d=OM=3$ cm."}, dd=0.25),
     W("B2", "sai-tinh-toan", "Lấy $OM=3\\sqrt2$ (nhầm với $OB$)", {"B2": "$OM=3\\sqrt2$, $SM=\\sqrt{16+18}=\\sqrt{34}$.", "B3": "$OH=\\dfrac{4\\cdot3\\sqrt2}{\\sqrt{34}}\\approx2{,}91$ cm."})],
    ghi_chu="S1: B2 ($SM=5$) vẫn đúng nên được 0,25."))

add(build("BM-012", src("HKN-27"), "thpt", "Khoảng cách từ điểm đến mặt phẳng", "VD",
    "Cho hình hộp chữ nhật $ABCD.A'B'C'D'$ có $AB=a$, $AD=2a$, $AA'=2a$. Tính khoảng cách từ $A$ đến mặt phẳng $(A'BD)$.",
    "$\\dfrac{a\\sqrt6}{3}$", sqrt(6) / 3, E("HKN-27"),
    ["Tứ diện vuông: $\\dfrac1{d^2}=\\dfrac1{x^2}+\\dfrac1{y^2}+\\dfrac1{z^2}$"],
    [S("$AB$, $AD$, $AA'$ đôi một vuông góc tại $A$ (tứ diện vuông $A.A'BD$).", "Tứ diện vuông tại $A$", "Hình hộp chữ nhật", 0.25),
     S("$\\dfrac1{d^2}=\\dfrac1{AB^2}+\\dfrac1{AD^2}+\\dfrac1{AA'^2}$.", "Công thức tứ diện vuông", "Tứ diện vuông", 0.25, ck=["Dựng $AK\\perp BD$, $AH\\perp A'K$"]),
     S("$\\dfrac1{d^2}=\\dfrac1{a^2}+\\dfrac1{4a^2}+\\dfrac1{4a^2}=\\dfrac3{2a^2}\\Rightarrow d=\\dfrac{a\\sqrt6}3$.", "$d=\\dfrac{a\\sqrt6}3$", "Tính toán", 0.5, so=sqrt(6)/3)],
    [W("B3", "sai-tinh-toan", "Bình phương sai: $\\dfrac1{(2a)^2}=\\dfrac1{2a^2}$", {"B3": "$\\dfrac1{d^2}=\\dfrac1{a^2}+\\dfrac1{2a^2}+\\dfrac1{2a^2}=\\dfrac2{a^2}\\Rightarrow d=\\dfrac{a\\sqrt2}2$."}),
     W("B2", "sai-dinh-li", "Dùng sai công thức $\\dfrac1d=\\dfrac1{AB}+\\dfrac1{AD}+\\dfrac1{AA'}$", {"B2": "$\\dfrac1d=\\dfrac1{AB}+\\dfrac1{AD}+\\dfrac1{AA'}$.", "B3": "$\\dfrac1d=\\dfrac1a+\\dfrac1{2a}+\\dfrac1{2a}=\\dfrac2a\\Rightarrow d=\\dfrac a2$."})]))

add(build("BM-013", src("HKN-32"), "thpt", "Khoảng cách từ điểm đến mặt phẳng", "VD",
    "Cho lăng trụ tam giác đều $ABC.A'B'C'$ có cạnh đáy $a$ và $AA'=a$. Tính khoảng cách từ $A$ đến mặt phẳng $(A'BC)$.",
    "$\\dfrac{a\\sqrt{21}}{7}$", sqrt(21) / 7, E("HKN-32"),
    ["Dựng khoảng cách", "Đường cao tam giác đều", "Hệ thức lượng"],
    [S("$M$ trung điểm $BC$: $AM\\perp BC$, $AA'\\perp BC$ nên $BC\\perp(A'AM)$; kẻ $AH\\perp A'M$ thì $AH\\perp(A'BC)$.", "$d=AH$", "Dựng đường vuông góc", 0.5),
     S("$AM=\\dfrac{a\\sqrt3}2$.", "$AM=\\dfrac{a\\sqrt3}2$", "Đường cao tam giác đều", 0.25, so=sqrt(3)/2),
     S("$\\dfrac1{AH^2}=\\dfrac1{a^2}+\\dfrac4{3a^2}=\\dfrac7{3a^2}\\Rightarrow AH=\\dfrac{a\\sqrt{21}}7$.", "$AH=\\dfrac{a\\sqrt{21}}7$", "Hệ thức lượng", 0.25, so=sqrt(21)/7)],
    [W("B2", "sai-tinh-toan", "Lấy $AM=a\\sqrt3$ (quên chia 2)", {"B2": "$AM=a\\sqrt3$.", "B3": "$\\dfrac1{AH^2}=\\dfrac1{a^2}+\\dfrac1{3a^2}=\\dfrac4{3a^2}\\Rightarrow AH=\\dfrac{a\\sqrt3}2$."}),
     W("B1", "sai-hinh-dung", "Coi $AM$ là khoảng cách từ $A$ đến $(A'BC)$", {"B1": "$AM\\perp BC$ nên $d(A,(A'BC))=AM$.", "B3": "$d=AM=\\dfrac{a\\sqrt3}2$."}, dd=0.25)]))

add(build("BM-014", src("HKN-33"), "thpt", "Khoảng cách giữa hai đường thẳng chéo nhau", "TH",
    "Cho lăng trụ tam giác đều $ABC.A'B'C'$ có cạnh đáy $a$ và $AA'=2a$. Tính khoảng cách giữa hai đường thẳng $AA'$ và $BC$.",
    "$\\dfrac{a\\sqrt3}{2}$", sqrt(3) / 2, E("HKN-33"),
    ["Đoạn vuông góc chung", "Đường cao tam giác đều"],
    [S("$M$ trung điểm $BC$: $AM\\perp BC$ và $AM\\perp AA'$ (vì $AA'\\perp(ABC)$), $AM$ cắt cả hai đường nên là đoạn vuông góc chung.", "$d=AM$", "Đoạn vuông góc chung", 0.5),
     S("$AM=\\dfrac{a\\sqrt3}2$.", "$\\dfrac{a\\sqrt3}2$", "Đường cao tam giác đều", 0.5, so=sqrt(3)/2)],
    [W("B1", "sai-dinh-li", "Lấy $AB$ làm đoạn vuông góc chung ($AB$ không vuông góc $BC$)", {"B1": "$AB$ nối $AA'$ với $BC$ và $AB\\perp AA'$ nên $d(AA',BC)=AB$.", "B2": "$d=AB=a$."}),
     W("B2", "sai-tinh-toan", "Nhầm đường cao với bán kính ngoại tiếp: $AM=\\dfrac{a\\sqrt3}3$", {"B2": "$AM=\\dfrac{a\\sqrt3}3$."})]))

# ---------------- THỂ TÍCH ----------------
add(build("BM-015", src("HKN-31"), "thcs", "Thể tích lăng trụ", "NB",
    "Một lăng trụ đứng tam giác $ABC.A'B'C'$ có đáy là tam giác đều cạnh $6$ cm và chiều cao $10$ cm. Tính thể tích lăng trụ.",
    "$90\\sqrt3$ cm$^3$", 90 * sqrt(3), E("HKN-31", "V"),
    ["Diện tích tam giác đều $\\dfrac{a^2\\sqrt3}4$", "$V=S_{đáy}\\cdot h$"],
    [S("$S_{đáy}=\\dfrac{6^2\\sqrt3}4=9\\sqrt3$ cm$^2$.", "$9\\sqrt3$", "Diện tích tam giác đều", 0.5, so=9*sqrt(3)),
     S("$V=S\\cdot h=9\\sqrt3\\cdot10=90\\sqrt3$ cm$^3$.", "$90\\sqrt3$", "Thể tích lăng trụ", 0.5, so=90*sqrt(3))],
    [W("B1", "sai-tinh-toan", "Chia sai: $\\dfrac{36\\sqrt3}4=12\\sqrt3$", {"B1": "$S=\\dfrac{36\\sqrt3}4=12\\sqrt3$.", "B2": "$V=120\\sqrt3$ cm$^3$."}),
     W("B2", "sai-dinh-li", "Dùng công thức khối chóp $V=\\dfrac13Sh$", {"B2": "$V=\\dfrac13\\cdot9\\sqrt3\\cdot10=30\\sqrt3$ cm$^3$."})]))

add(build("BM-016", src("HKN-37"), "thcs", "Thể tích lăng trụ", "TH",
    "Lăng trụ đứng $ABC.A'B'C'$ có đáy là tam giác với $AB=13$ cm, $BC=14$ cm, $CA=15$ cm và chiều cao $10$ cm. Tính thể tích lăng trụ.",
    "$840$ cm$^3$", 840.0, E("HKN-37", "V"),
    ["Công thức Heron", "$V=S_{đáy}\\cdot h$"],
    [S("Nửa chu vi $p=\\dfrac{13+14+15}2=21$.", "$p=21$", "Nửa chu vi", 0.25, so=21.0),
     S("$S=\\sqrt{21\\cdot8\\cdot7\\cdot6}=\\sqrt{7056}=84$ cm$^2$.", "$S=84$", "Heron", 0.5, ck=["Hạ đường cao từ $A$ xuống $BC$: $h_a=12$, $S=\\dfrac12\\cdot14\\cdot12$"], so=84.0),
     S("$V=84\\cdot10=840$ cm$^3$.", "$840$", "Thể tích lăng trụ", 0.25, so=840.0)],
    [W("B2", "sai-tinh-toan", "Khai căn sai $\\sqrt{7056}=86$", {"B2": "$S=\\sqrt{7056}=86$.", "B3": "$V=860$ cm$^3$."}),
     W("B1", "sai-dinh-li", "Dùng chu vi $42$ thay nửa chu vi trong Heron", {"B1": "$p=13+14+15=42$.", "B2": "$S=\\sqrt{42\\cdot29\\cdot28\\cdot27}\\approx959{,}6$.", "B3": "$V\\approx9596$ cm$^3$."})]))

add(build("BM-017", src("HKN-51"), "thpt", "Thể tích khối chóp", "NB",
    "Cho hình chóp $S.ABCD$ có đáy là hình vuông cạnh $a$, $SA\\perp(ABCD)$ và $SA=a\\sqrt3$. Tính thể tích khối chóp $S.ABCD$ theo $a$.",
    "$\\dfrac{a^3\\sqrt3}{3}$", sqrt(3) / 3, E("HKN-51", "V"),
    ["$V=\\dfrac13Bh$"],
    [S("$SA\\perp(ABCD)$ nên chiều cao là $SA=a\\sqrt3$.", "$h=a\\sqrt3$", "Chiều cao khối chóp", 0.25, so=sqrt(3)),
     S("$S_{ABCD}=a^2$.", "$a^2$", "Diện tích hình vuông", 0.25, so=1.0),
     S("$V=\\dfrac13a^2\\cdot a\\sqrt3=\\dfrac{a^3\\sqrt3}3$.", "$\\dfrac{a^3\\sqrt3}3$", "Thể tích khối chóp", 0.5, so=sqrt(3)/3)],
    [W("B3", "sai-dinh-li", "Quên hệ số $\\dfrac13$", {"B3": "$V=a^2\\cdot a\\sqrt3=a^3\\sqrt3$."}),
     W("B1", "sai-hinh-dung", "Lấy $SC$ làm chiều cao", {"B1": "Chiều cao là $SC=\\sqrt{3a^2+2a^2}=a\\sqrt5$.", "B3": "$V=\\dfrac13a^2\\cdot a\\sqrt5=\\dfrac{a^3\\sqrt5}3$."}, dd=0.25)]))

add(build("BM-018", src("HKN-52"), "thpt", "Thể tích khối chóp", "TH",
    "Cho hình chóp tứ giác đều $S.ABCD$ có cạnh đáy $a$, $O$ là tâm đáy và cạnh bên tạo với mặt đáy góc $60^\\circ$. Tính thể tích khối chóp theo $a$.",
    "$\\dfrac{a^3\\sqrt6}{6}$", sqrt(6) / 6, E("HKN-52", "V"),
    ["Góc giữa cạnh bên và đáy", "$V=\\dfrac13Bh$"],
    [S("$OA=\\dfrac{AC}2=\\dfrac{a\\sqrt2}2$.", "$OA=\\dfrac{a\\sqrt2}2$", "Đường chéo hình vuông", 0.25, so=sqrt(2)/2),
     S("Góc giữa $SA$ và đáy là $\\widehat{SAO}=60^\\circ$, $SO=OA\\tan60^\\circ=\\dfrac{a\\sqrt6}2$.", "$SO=\\dfrac{a\\sqrt6}2$", "tan", 0.25, so=sqrt(6)/2),
     S("$V=\\dfrac13a^2\\cdot\\dfrac{a\\sqrt6}2=\\dfrac{a^3\\sqrt6}6$.", "$\\dfrac{a^3\\sqrt6}6$", "Thể tích", 0.5, so=sqrt(6)/6)],
    [W("B2", "sai-dinh-li", "Dùng sai tỉ số lượng giác: $SO=\\dfrac{OA}{\\tan60^\\circ}$", {"B2": "$SO=\\dfrac{OA}{\\tan60^\\circ}=\\dfrac{a\\sqrt6}6$.", "B3": "$V=\\dfrac13a^2\\cdot\\dfrac{a\\sqrt6}6=\\dfrac{a^3\\sqrt6}{18}$."}),
     W("B1", "sai-tinh-toan", "Lấy $OA=a\\sqrt2$ (quên chia đôi)", {"B1": "$OA=a\\sqrt2$.", "B2": "$SO=a\\sqrt2\\cdot\\sqrt3=a\\sqrt6$.", "B3": "$V=\\dfrac{a^3\\sqrt6}3$."})]))

add(build("BM-019", src("HKN-53"), "thpt", "Thể tích lăng trụ", "NB",
    "Cho lăng trụ đứng $ABC.A'B'C'$ có đáy là tam giác đều cạnh $a$ và $AA'=a\\sqrt3$. Tính thể tích khối lăng trụ theo $a$.",
    "$\\dfrac{3a^3}{4}$", 0.75, E("HKN-53", "V"),
    ["$V=Bh$", "Lăng trụ đứng: chiều cao bằng cạnh bên"],
    [S("$S_{ABC}=\\dfrac{a^2\\sqrt3}4$.", "$\\dfrac{a^2\\sqrt3}4$", "Diện tích tam giác đều", 0.25, so=sqrt(3)/4),
     S("Lăng trụ đứng nên chiều cao $h=AA'=a\\sqrt3$.", "$h=a\\sqrt3$", "Lăng trụ đứng", 0.25, so=sqrt(3)),
     S("$V=\\dfrac{a^2\\sqrt3}4\\cdot a\\sqrt3=\\dfrac{3a^3}4$.", "$\\dfrac{3a^3}4$", "Thể tích lăng trụ", 0.5, so=0.75)],
    [W("B2", "nham-dieu-kien", "Coi như lăng trụ xiên, lấy $h=AA'\\sin60^\\circ$", {"B2": "$h=AA'\\sin60^\\circ=\\dfrac{3a}2$.", "B3": "$V=\\dfrac{a^2\\sqrt3}4\\cdot\\dfrac{3a}2=\\dfrac{3\\sqrt3a^3}8$."}),
     W("B1", "sai-dinh-li", "Dùng $S=\\dfrac{a^2\\sqrt3}2$ cho tam giác đều", {"B1": "$S_{ABC}=\\dfrac{a^2\\sqrt3}2$.", "B3": "$V=\\dfrac{3a^3}2$."}, dd=0.25)]))

add(build("BM-020", src("HKN-56"), "thpt", "Thể tích khối nón", "NB",
    "Cho hình nón đỉnh $S$, tâm đáy $O$, bán kính đáy $OM=2a$ và chiều cao $SO=3a$. Tính thể tích khối nón theo $a$.",
    "$4\\pi a^3$", 4 * pi, E("HKN-56", "V"),
    ["$V=\\dfrac13\\pi r^2h$"],
    [S("$B=\\pi(2a)^2=4\\pi a^2$.", "$4\\pi a^2$", "Diện tích hình tròn", 0.5, so=4*pi),
     S("$V=\\dfrac13\\cdot4\\pi a^2\\cdot3a=4\\pi a^3$.", "$4\\pi a^3$", "Thể tích khối nón", 0.5, so=4*pi)],
    [W("B1", "sai-tinh-toan", "Bình phương sai $(2a)^2=2a^2$", {"B1": "$B=\\pi(2a)^2=2\\pi a^2$.", "B2": "$V=\\dfrac13\\cdot2\\pi a^2\\cdot3a=2\\pi a^3$."}),
     W("B2", "sai-dinh-li", "Dùng công thức khối trụ $V=Bh$", {"B2": "$V=4\\pi a^2\\cdot3a=12\\pi a^3$."})]))

# ---------------- DIỆN TÍCH ----------------
add(build("BM-021", src("HKN-41"), "thpt", "Diện tích xung quanh hình nón", "NB",
    "Cho hình nón đỉnh $S$, tâm đáy $O$, bán kính đáy $a$ và chiều cao $SO=a\\sqrt3$. Tính diện tích xung quanh của hình nón.",
    "$2\\pi a^2$", 2 * pi, E("HKN-41", "Sxq"),
    ["$l=\\sqrt{r^2+h^2}$", "$S_{xq}=\\pi rl$"],
    [S("$l=\\sqrt{a^2+3a^2}=2a$.", "$l=2a$", "Pythagore", 0.5, so=2.0),
     S("$S_{xq}=\\pi\\cdot a\\cdot2a=2\\pi a^2$.", "$2\\pi a^2$", "Diện tích xung quanh hình nón", 0.5, so=2*pi)],
    [W("B2", "sai-dinh-li", "Dùng chiều cao thay đường sinh: $S_{xq}=\\pi rh$", {"B2": "$S_{xq}=\\pi rh=\\pi a\\cdot a\\sqrt3=\\sqrt3\\pi a^2$."}),
     W("B1", "sai-tinh-toan", "Quên khai căn: $l=4a$", {"B1": "$l=a^2+3a^2\\to l=4a$.", "B2": "$S_{xq}=\\pi\\cdot a\\cdot4a=4\\pi a^2$."})]))

add(build("BM-022", src("HKN-60"), "thcs", "Diện tích toàn phần hình nón", "NB",
    "Một hình nón có bán kính đáy $OM=6$ cm và đường sinh $SM=10$ cm. Tính diện tích toàn phần của hình nón (kết quả theo $\\pi$).",
    "$96\\pi$ cm$^2$", 96 * pi, E("HKN-60", "Stp"),
    ["$S_{xq}=\\pi rl$", "$S_{tp}=S_{xq}+S_{đáy}$"],
    [S("$S_{xq}=\\pi\\cdot6\\cdot10=60\\pi$ cm$^2$.", "$60\\pi$", "Diện tích xung quanh", 0.25, so=60*pi),
     S("$S_{đáy}=\\pi\\cdot6^2=36\\pi$ cm$^2$.", "$36\\pi$", "Diện tích hình tròn", 0.25, so=36*pi),
     S("$S_{tp}=60\\pi+36\\pi=96\\pi$ cm$^2$.", "$96\\pi$", "Diện tích toàn phần", 0.5, so=96*pi)],
    [W("B1", "sai-dinh-li", "Dùng công thức hình trụ $S_{xq}=2\\pi rl$", {"B1": "$S_{xq}=2\\pi\\cdot6\\cdot10=120\\pi$.", "B3": "$S_{tp}=120\\pi+36\\pi=156\\pi$ cm$^2$."}, dd=0.25),
     W("B3", "nham-dieu-kien", "Cộng hai đáy như hình trụ", {"B3": "$S_{tp}=60\\pi+2\\cdot36\\pi=132\\pi$ cm$^2$."})],
    ghi_chu="S1: B2 đúng, độc lập nên được 0,25."))

add(build("BM-023", src("HKN-62"), "thcs", "Diện tích xung quanh hình chóp đều", "TH",
    "Một hình chóp tứ giác đều $S.ABCD$ có cạnh đáy $6$ cm, $O$ là tâm đáy và chiều cao $SO=4$ cm. Tính diện tích xung quanh của hình chóp.",
    "$60$ cm$^2$", 60.0, E("HKN-62", "Sxq"),
    ["Trung đoạn hình chóp đều", "$S_{xq}=p\\cdot d$"],
    [S("$H$ trung điểm $BC$: $OH=3$, $SH=\\sqrt{4^2+3^2}=5$ cm (trung đoạn).", "$SH=5$", "Pythagore", 0.5, so=5.0),
     S("Nửa chu vi đáy $p=\\dfrac{4\\cdot6}2=12$ cm.", "$p=12$", "Chu vi", 0.25, so=12.0),
     S("$S_{xq}=p\\cdot d=12\\cdot5=60$ cm$^2$.", "$60$", "Diện tích xung quanh hình chóp đều", 0.25, ck=["$4\\cdot\\dfrac12\\cdot6\\cdot5$"], so=60.0)],
    [W("B1", "sai-hinh-dung", "Dùng chiều cao $SO$ làm trung đoạn", {"B1": "Trung đoạn $=SO=4$ cm.", "B3": "$S_{xq}=12\\cdot4=48$ cm$^2$."}, dd=0.25),
     W("B3", "sai-dinh-li", "Nhân chu vi (không phải nửa chu vi) với trung đoạn", {"B3": "$S_{xq}=24\\cdot5=120$ cm$^2$."})],
    ghi_chu="S1: B2 đúng, độc lập nên được 0,25."))

add(build("BM-024", src("HKN-66"), "thpt", "Diện tích xung quanh hình chóp đều", "TH",
    "Cho hình chóp tứ giác đều $S.ABCD$ có cạnh đáy $a$, $O$ là tâm đáy và $SO=\\dfrac{a\\sqrt3}2$. Tính diện tích xung quanh của hình chóp theo $a$.",
    "$2a^2$", 2.0, E("HKN-66", "Sxq"),
    ["Trung đoạn", "Diện tích tam giác"],
    [S("$H$ trung điểm $BC$, $OH=\\dfrac a2$, $SH=\\sqrt{\\dfrac{3a^2}4+\\dfrac{a^2}4}=a$.", "$SH=a$", "Pythagore", 0.5, so=1.0),
     S("$S_{SBC}=\\dfrac12\\cdot BC\\cdot SH=\\dfrac{a^2}2$.", "$\\dfrac{a^2}2$", "Diện tích tam giác", 0.25, so=0.5),
     S("$S_{xq}=4\\cdot\\dfrac{a^2}2=2a^2$.", "$2a^2$", "Tổng bốn mặt bên", 0.25, so=2.0)],
    [W("B1", "sai-tinh-toan", "Trừ thay cộng trong Pythagore", {"B1": "$SH=\\sqrt{\\dfrac{3a^2}4-\\dfrac{a^2}4}=\\dfrac{a\\sqrt2}2$.", "B2": "$S_{SBC}=\\dfrac{a^2\\sqrt2}4$.", "B3": "$S_{xq}=a^2\\sqrt2$."}),
     W("B2", "sai-dinh-li", "Quên hệ số $\\dfrac12$ trong diện tích tam giác", {"B2": "$S_{SBC}=BC\\cdot SH=a^2$.", "B3": "$S_{xq}=4a^2$."})]))

add(build("BM-025", src("HKN-67"), "thpt", "Diện tích các mặt bên hình chóp", "VD",
    "Cho hình chóp $S.ABCD$ có đáy là hình vuông cạnh $a$, $SA\\perp(ABCD)$ và $SA=a$. Tính tổng diện tích bốn mặt bên của hình chóp theo $a$.",
    "$(1+\\sqrt2)a^2$", 1 + sqrt(2), E("HKN-67", "Sxq"),
    ["Định lí ba đường vuông góc", "Diện tích tam giác vuông"],
    [S("$\\triangle SAB$, $\\triangle SAD$ vuông tại $A$: mỗi tam giác có diện tích $\\dfrac{a^2}2$.", "$S_{SAB}=S_{SAD}=\\dfrac{a^2}2$", "Tam giác vuông", 0.25, so=0.5),
     S("$BC\\perp(SAB)\\Rightarrow BC\\perp SB$; tương tự $CD\\perp SD$: $\\triangle SBC$ vuông tại $B$, $\\triangle SCD$ vuông tại $D$.", "$\\triangle SBC$ vuông tại $B$, $\\triangle SCD$ vuông tại $D$", "Ba đường vuông góc", 0.25),
     S("$SB=SD=a\\sqrt2$ nên $S_{SBC}=S_{SCD}=\\dfrac{a\\cdot a\\sqrt2}2$.", "$\\dfrac{a^2\\sqrt2}2$", "Diện tích tam giác vuông", 0.25, so=sqrt(2)/2),
     S("$S=2\\cdot\\dfrac{a^2}2+2\\cdot\\dfrac{a^2\\sqrt2}2=(1+\\sqrt2)a^2$.", "$(1+\\sqrt2)a^2$", "Cộng", 0.25, so=1+sqrt(2))],
    [W("B2", "sai-hinh-dung", "Cho $\\triangle SBC$ vuông tại $C$ (sai đỉnh góc vuông)", {"B2": "$\\triangle SBC$ vuông tại $C$, $\\triangle SCD$ vuông tại $C$.", "B3": "$SC=a\\sqrt3$, $S_{SBC}=S_{SCD}=\\dfrac{a\\cdot a\\sqrt3}2$.", "B4": "$S=a^2+a^2\\sqrt3$."}),
     W("B4", "sai-tinh-toan", "Cộng sai: $2\\cdot\\dfrac{a^2\\sqrt2}2=2a^2\\sqrt2$", {"B4": "$S=a^2+2a^2\\sqrt2=(1+2\\sqrt2)a^2$."})]))

# ---------------- MẶT CẦU ----------------
add(build("BM-026", src("HKN-45"), "thpt", CHA, "TH",
    "Cho hình lập phương $ABCD.A'B'C'D'$ cạnh $2$ cm. Tính bán kính mặt cầu ngoại tiếp hình lập phương và thể tích khối cầu tương ứng.",
    "$R=\\sqrt3$ cm; $V=4\\sqrt3\\pi$ cm$^3$", 4 * sqrt(3) * pi, E("HKN-45", "V"),
    ["Tâm mặt cầu ngoại tiếp hình lập phương là tâm hình lập phương", "$V=\\dfrac43\\pi R^3$"],
    [S("Đường chéo $AC'=2\\sqrt3$ cm.", "$AC'=2\\sqrt3$", "Đường chéo hình lập phương $a\\sqrt3$", 0.25, so=2*sqrt(3)),
     S("Tâm là trung điểm $AC'$, $R=\\dfrac{AC'}2=\\sqrt3$ cm.", "$R=\\sqrt3$", "Mặt cầu ngoại tiếp", 0.25, so=sqrt(3)),
     S("$V=\\dfrac43\\pi(\\sqrt3)^3=\\dfrac43\\pi\\cdot3\\sqrt3=4\\sqrt3\\pi$ cm$^3$.", "$4\\sqrt3\\pi$", "Thể tích khối cầu", 0.5, so=4*sqrt(3)*pi)],
    [W("B2", "sai-dinh-li", "Lấy đường chéo làm bán kính", {"B2": "$R=AC'=2\\sqrt3$ cm.", "B3": "$V=\\dfrac43\\pi(2\\sqrt3)^3=32\\sqrt3\\pi$ cm$^3$."}),
     W("B3", "sai-tinh-toan", "Lũy thừa sai $(\\sqrt3)^3=3$", {"B3": "$V=\\dfrac43\\pi\\cdot3=4\\pi$ cm$^3$."})],
    ghi_chu="Engine đo hai đại lượng (R = 1,7320508 và V); dap_so.so là V."))

add(build("BM-027", src("HKN-79"), "thpt", CHA, "VD",
    "Cho hình chóp $S.ABCD$ có đáy là hình vuông cạnh $a$, $SA\\perp(ABCD)$ và $SA=a\\sqrt2$. Tính bán kính và diện tích mặt cầu ngoại tiếp hình chóp theo $a$.",
    "$R=a$; $S=4\\pi a^2$", 4 * pi, E("HKN-79", "S"),
    ["Điểm nhìn đoạn thẳng dưới góc vuông", "$S=4\\pi R^2$"],
    [S("$SA\\perp AC$; $BC\\perp(SAB)\\Rightarrow BC\\perp SB$; $CD\\perp(SAD)\\Rightarrow CD\\perp SD$.", "$\\widehat{SAC}=\\widehat{SBC}=\\widehat{SDC}=90^\\circ$", "Ba đường vuông góc", 0.25),
     S("$A,B,D$ nhìn $SC$ dưới góc vuông nên $S,A,B,C,D$ nằm trên mặt cầu đường kính $SC$; tâm là trung điểm $SC$.", "Tâm là trung điểm $SC$", "Mặt cầu đường kính", 0.25),
     S("$SC=\\sqrt{SA^2+AC^2}=\\sqrt{2a^2+2a^2}=2a$, $R=a$.", "$R=a$", "Pythagore", 0.25, so=1.0),
     S("$S=4\\pi R^2=4\\pi a^2$.", "$4\\pi a^2$", "Diện tích mặt cầu", 0.25, so=4*pi)],
    [W("B2", "sai-hinh-dung", "Lấy tâm đáy $O$ làm tâm mặt cầu", {"B2": "Tâm mặt cầu là tâm $O$ của đáy.", "B3": "$R=OA=\\dfrac{a\\sqrt2}2$.", "B4": "$S=4\\pi\\cdot\\dfrac{a^2}2=2\\pi a^2$."}),
     W("B4", "sai-dinh-li", "Dùng công thức diện tích hình tròn $\\pi R^2$", {"B4": "$S=\\pi R^2=\\pi a^2$."})]))

add(build("BM-028", src("HKN-80"), "thpt", CHA, "VD",
    "Cho hình chóp tứ giác đều $S.ABCD$ có tất cả các cạnh bằng $a$, $O$ là tâm đáy. Tính bán kính và thể tích khối cầu ngoại tiếp hình chóp theo $a$.",
    "$R=\\dfrac{a\\sqrt2}{2}$; $V=\\dfrac{\\sqrt2\\pi a^3}{3}$", sqrt(2) * pi / 3, E("HKN-80", "V"),
    ["Tâm mặt cầu cách đều các đỉnh", "$V=\\dfrac43\\pi R^3$"],
    [S("$OA=\\dfrac{AC}2=\\dfrac{a\\sqrt2}2$.", "$OA=\\dfrac{a\\sqrt2}2$", "Đường chéo hình vuông", 0.25, so=sqrt(2)/2),
     S("$SO=\\sqrt{SA^2-OA^2}=\\sqrt{a^2-\\dfrac{a^2}2}=\\dfrac{a\\sqrt2}2$.", "$SO=\\dfrac{a\\sqrt2}2$", "Pythagore", 0.25, so=sqrt(2)/2),
     S("$OS=OA=OB=OC=OD=\\dfrac{a\\sqrt2}2$ nên $O$ là tâm, $R=\\dfrac{a\\sqrt2}2$.", "$R=\\dfrac{a\\sqrt2}2$", "Điểm cách đều năm đỉnh", 0.25, tc="Phải chỉ ra $OS=OA$ (cách đều cả đỉnh $S$)", so=sqrt(2)/2),
     S("$V=\\dfrac43\\pi\\left(\\dfrac{a\\sqrt2}2\\right)^3=\\dfrac43\\pi\\cdot\\dfrac{a^3\\sqrt2}4=\\dfrac{\\sqrt2\\pi a^3}3$.", "$\\dfrac{\\sqrt2\\pi a^3}3$", "Thể tích khối cầu", 0.25, so=sqrt(2)*pi/3)],
    [W("B3", "thieu-chung-minh", "Khẳng định tâm là $O$ mà không chỉ ra $OS=OA$ (đáp số vẫn đúng)", {"B3": "Tâm mặt cầu là tâm $O$ của đáy, $R=OA=\\dfrac{a\\sqrt2}2$."}, dd=0.75),
     W("B4", "sai-tinh-toan", "Lũy thừa sai $\\left(\\dfrac{\\sqrt2}2\\right)^3=\\dfrac{\\sqrt2}8$", {"B4": "$V=\\dfrac43\\pi\\cdot\\dfrac{a^3\\sqrt2}8=\\dfrac{\\sqrt2\\pi a^3}6$."})],
    ghi_chu="S1: lỗi thiếu lập luận chỉ mất điểm B3; B4 tính đúng nên vẫn được 0,25 (tổng 0,75)."))

add(build("BM-029", src("HKN-82"), "thpt", CHA, "VD",
    "Cho lăng trụ đứng $ABC.A'B'C'$ có đáy là tam giác đều cạnh $a$ và $AA'=2a$. Tính diện tích mặt cầu ngoại tiếp lăng trụ theo $a$.",
    "$\\dfrac{16\\pi a^2}{3}$", 16 * pi / 3, E("HKN-82", "S"),
    ["Mặt cầu ngoại tiếp lăng trụ đứng: $R^2=r^2+\\left(\\dfrac h2\\right)^2$", "$S=4\\pi R^2$"],
    [S("Bán kính đường tròn ngoại tiếp đáy $r=\\dfrac{a\\sqrt3}3$, $r^2=\\dfrac{a^2}3$.", "$r^2=\\dfrac{a^2}3$", "Bán kính ngoại tiếp tam giác đều", 0.25, so=1/3),
     S("$R^2=r^2+\\left(\\dfrac{AA'}2\\right)^2=\\dfrac{a^2}3+a^2=\\dfrac{4a^2}3$.", "$R^2=\\dfrac{4a^2}3$", "Tâm là trung điểm đoạn nối hai tâm đáy", 0.5, so=4/3),
     S("$S=4\\pi R^2=\\dfrac{16\\pi a^2}3$.", "$\\dfrac{16\\pi a^2}3$", "Diện tích mặt cầu", 0.25, so=16*pi/3)],
    [W("B2", "sai-dinh-li", "Dùng cả chiều cao $h$ thay vì $\\dfrac h2$", {"B2": "$R^2=r^2+AA'^2=\\dfrac{a^2}3+4a^2=\\dfrac{13a^2}3$.", "B3": "$S=\\dfrac{52\\pi a^2}3$."}),
     W("B1", "sai-tinh-toan", "Lấy $r$ bằng đường cao tam giác đều $\\dfrac{a\\sqrt3}2$", {"B1": "$r=\\dfrac{a\\sqrt3}2$, $r^2=\\dfrac{3a^2}4$.", "B2": "$R^2=\\dfrac{3a^2}4+a^2=\\dfrac{7a^2}4$.", "B3": "$S=7\\pi a^2$."})]))

add(build("BM-030", src("HKN-83"), "thpt", CHA, "VDC",
    "Cho lăng trụ đứng $ABC.A'B'C'$ có đáy vuông tại $A$, $AB=a$, $AC=a\\sqrt3$, $AA'=2a$. Tính bán kính và thể tích khối cầu ngoại tiếp lăng trụ theo $a$.",
    "$R=a\\sqrt2$; $V=\\dfrac{8\\sqrt2\\pi a^3}{3}$", 8 * sqrt(2) * pi / 3, E("HKN-83", "V"),
    ["Tâm ngoại tiếp tam giác vuông là trung điểm cạnh huyền", "$R^2=r^2+(h/2)^2$", "$V=\\dfrac43\\pi R^3$"],
    [S("$BC=\\sqrt{a^2+3a^2}=2a$, tâm ngoại tiếp đáy là trung điểm $BC$, $r=a$.", "$r=a$", "Tam giác vuông", 0.25, so=1.0),
     S("$R^2=r^2+\\left(\\dfrac{AA'}2\\right)^2=a^2+a^2=2a^2$, $R=a\\sqrt2$.", "$R=a\\sqrt2$", "Mặt cầu ngoại tiếp lăng trụ đứng", 0.25, so=sqrt(2)),
     S("$V=\\dfrac43\\pi(a\\sqrt2)^3=\\dfrac43\\pi\\cdot2\\sqrt2a^3=\\dfrac{8\\sqrt2\\pi a^3}3$.", "$\\dfrac{8\\sqrt2\\pi a^3}3$", "Thể tích khối cầu", 0.5, so=8*sqrt(2)*pi/3)],
    [W("B1", "sai-dinh-li", "Lấy trọng tâm làm tâm đường tròn ngoại tiếp đáy: $r=\\dfrac23AM=\\dfrac{2a}3$", {"B1": "Tâm ngoại tiếp đáy là trọng tâm, $r=\\dfrac23\\cdot AM=\\dfrac{2a}3$.", "B2": "$R^2=\\dfrac{4a^2}9+a^2=\\dfrac{13a^2}9$, $R=\\dfrac{a\\sqrt{13}}3$.", "B3": "$V=\\dfrac43\\pi\\left(\\dfrac{a\\sqrt{13}}3\\right)^3=\\dfrac{52\\sqrt{13}\\pi a^3}{81}$."}),
     W("B3", "sai-tinh-toan", "Lũy thừa sai $(a\\sqrt2)^3=2a^3$", {"B3": "$V=\\dfrac43\\pi\\cdot2a^3=\\dfrac{8\\pi a^3}3$."})],
    ghi_chu="Engine đo R = 1,4142136 và V; dap_so.so là V."))
