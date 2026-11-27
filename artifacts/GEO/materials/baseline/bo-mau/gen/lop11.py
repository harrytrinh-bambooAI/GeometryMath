"""31 bài Toán 11 từ artifacts/GEO/materials/lop-11/bai-tap/ (tài liệu tự soạn của dự án, đã soát chéo
305/305). Lời giải được chia lại thành bước có điểm; thang điểm tự soạn theo kiểu HDC Sở (bội 0,25)."""
import os, re
from math import sqrt, atan, acos, asin, degrees, pi
from _lib import S, W, build

HERE = os.path.dirname(os.path.abspath(__file__))
BT = os.path.normpath(os.path.join(HERE, "..", "..", "..", "lop-11", "bai-tap"))
C4 = "chuong-04-quan-he-song-song"
C7 = "chuong-07-quan-he-vuong-goc"


def src(rel, n):
    p = os.path.join(BT, rel)
    ver = "?"
    with open(p, encoding="utf-8") as f:
        for line in f:
            m = re.match(r"Phiên bản:\s*(.+)", line)
            if m:
                ver = m.group(1).strip()
                break
    return {"loai": "lop11", "ref": f"artifacts/GEO/materials/lop-11/bai-tap/{rel} — bài {n} ({ver})"}


items = []
add = items.append
B22, B23, B24, B25, B26, B27, OT7 = (f"{C7}/{x}.md" for x in [
    "bai-22-hai-duong-thang-vuong-goc", "bai-23-duong-thang-vuong-goc-voi-mat-phang",
    "bai-24-phep-chieu-vuong-goc-goc-giua-duong-thang-va-mat-phang", "bai-25-hai-mat-phang-vuong-goc",
    "bai-26-khoang-cach", "bai-27-the-tich", "on-tap-chuong"])
B10, B11, B12, B13, OT4 = (f"{C4}/{x}.md" for x in [
    "bai-10-duong-thang-va-mat-phang-trong-khong-gian", "bai-11-hai-duong-thang-song-song",
    "bai-12-duong-thang-va-mat-phang-song-song", "bai-13-hai-mat-phang-song-song", "on-tap-chuong"])
ANG = "Góc giữa hai đường thẳng"
ALP = "Góc giữa đường thẳng và mặt phẳng"
CM = "chứng minh (không có đáp số số)"
a35 = degrees(atan(1 / sqrt(2)))

# ---------- Chương 7 ----------
add(build("BM-031", src(B22, 3), "thpt", ANG, "TH",
    "Cho hình lập phương $ABCD.A'B'C'D'$. Tính góc giữa hai đường thẳng $AB'$ và $BC'$.",
    "$60^\\circ$", 60.0, None, ["Đưa về hai đường thẳng cắt nhau bằng phép song song", "Tam giác đều"],
    [S("$ABC'D'$ là hình bình hành nên $BC'\\parallel AD'$; góc $(AB',BC')=(AB',AD')$.", "$(AB',BC')=(AB',AD')$", "Góc giữa hai đường thẳng qua đường song song", 0.25),
     S("$AB'=B'D'=D'A=a\\sqrt2$ (đường chéo các mặt) nên $\\triangle AB'D'$ đều.", "$\\triangle AB'D'$ đều", "Đường chéo mặt", 0.5),
     S("$\\widehat{B'AD'}=60^\\circ$; góc cần tìm là $60^\\circ$.", "$60^\\circ$", "Tam giác đều", 0.25, so=60.0)],
    [W("B1", "sai-hinh-dung", "Cho $BC'\\parallel AD$ (nhầm đường song song) rồi suy ra góc $(AB',AD)=90^\\circ$", {"B1": "$BC'\\parallel AD$ nên góc $(AB',BC')=(AB',AD)$.", "B2": "$AD\\perp(ABB'A')$ nên $AD\\perp AB'$.", "B3": "Góc cần tìm là $90^\\circ$."}),
     W("B2", "sai-tinh-toan", "Lấy $B'D'=a\\sqrt3$ nên tam giác không đều, $\\cos=\\dfrac14$", {"B2": "$AB'=AD'=a\\sqrt2$, $B'D'=a\\sqrt3$.", "B3": "$\\cos\\widehat{B'AD'}=\\dfrac{2+2-3}{4}=\\dfrac14$, góc $\\approx75{,}52^\\circ$."})]))

add(build("BM-032", src(B22, 6), "thpt", ANG, "VD",
    "Cho tứ diện đều $ABCD$ cạnh $a$, $M$ là trung điểm $BC$. Tính côsin của góc giữa hai đường thẳng $AB$ và $DM$.",
    "$\\dfrac{\\sqrt3}{6}$", sqrt(3) / 6, None, ["Đường trung bình", "Định lí côsin", "Trung tuyến tam giác đều"],
    [S("Gọi $P$ trung điểm $AC$: $PM\\parallel AB$, $PM=\\dfrac a2$; góc $(AB,DM)$ bằng góc giữa $MP$ và $MD$.", "$(AB,DM)=(MP,MD)$", "Đường trung bình", 0.25, so=0.5),
     S("$DM=DP=\\dfrac{a\\sqrt3}2$ (trung tuyến tam giác đều).", "$DM=DP=\\dfrac{a\\sqrt3}2$", "Trung tuyến tam giác đều", 0.25, so=sqrt(3)/2),
     S("$\\cos\\widehat{PMD}=\\dfrac{MP^2+MD^2-PD^2}{2MP\\cdot MD}=\\dfrac{a^2/4}{2\\cdot\\frac a2\\cdot\\frac{a\\sqrt3}2}=\\dfrac{\\sqrt3}6$.", "$\\dfrac{\\sqrt3}6$", "Định lí côsin", 0.5, ck=["Vectơ: $\\vec{AB}\\cdot\\vec{DM}=\\dfrac{a^2}4$, $\\cos=\\dfrac{a^2/4}{a\\cdot a\\sqrt3/2}$"], so=sqrt(3)/6)],
    [W("B3", "sai-dinh-li", "Viết sai định lí côsin (đổi dấu cạnh đối diện)", {"B3": "$\\cos\\widehat{PMD}=\\dfrac{MP^2+MD^2+PD^2}{2MP\\cdot MD}=\\dfrac{7a^2/4}{a^2\\sqrt3/2}=\\dfrac{7\\sqrt3}{6}$."}),
     W("B2", "sai-tinh-toan", "Lấy $DP=a$ (nhầm với cạnh)", {"B2": "$DM=\\dfrac{a\\sqrt3}2$, $DP=a$.", "B3": "$\\cos\\widehat{PMD}=\\dfrac{\\frac{a^2}4+\\frac{3a^2}4-a^2}{\\dots}=0$, góc $90^\\circ$."})]))

add(build("BM-033", src(B22, 7), "thpt", ANG, "VDC",
    "Tứ diện $ABCD$ có $AB=CD=a$; $M,N$ lần lượt là trung điểm $BC,AD$ và $MN=\\dfrac{a\\sqrt3}2$. Tính góc giữa $AB$ và $CD$.",
    "$60^\\circ$", 60.0, None, ["Đường trung bình", "Định lí côsin", "Góc giữa hai đường thẳng thuộc $[0^\\circ;90^\\circ]$"],
    [S("$P$ trung điểm $AC$: $PM\\parallel AB$, $PM=\\dfrac a2$; $PN\\parallel CD$, $PN=\\dfrac a2$.", "$PM=PN=\\dfrac a2$", "Đường trung bình", 0.25, so=0.5),
     S("Góc $(AB,CD)$ bằng $\\widehat{MPN}$ hoặc bù với nó.", "$(AB,CD)=\\widehat{MPN}$ hoặc $180^\\circ-\\widehat{MPN}$", "Góc giữa hai đường thẳng", 0.25),
     S("$\\cos\\widehat{MPN}=\\dfrac{a^2/4+a^2/4-3a^2/4}{a^2/2}=-\\dfrac12\\Rightarrow\\widehat{MPN}=120^\\circ$.", "$\\widehat{MPN}=120^\\circ$", "Định lí côsin", 0.25, so=120.0),
     S("Góc giữa hai đường thẳng thuộc $[0^\\circ;90^\\circ]$ nên bằng $60^\\circ$.", "$60^\\circ$", "Điều kiện góc", 0.25, so=60.0)],
    [W("B4", "nham-dieu-kien", "Kết luận góc giữa hai đường thẳng là $120^\\circ$", {"B4": "Vậy góc giữa $AB$ và $CD$ bằng $120^\\circ$."}),
     W("B3", "sai-tinh-toan", "Tính sai tử số: $\\dfrac{a^2}4+\\dfrac{a^2}4-\\dfrac{3a^2}4=\\dfrac{a^2}4$", {"B3": "$\\cos\\widehat{MPN}=\\dfrac{a^2/4}{a^2/2}=\\dfrac12\\Rightarrow\\widehat{MPN}=60^\\circ$.", "B4": "Góc cần tìm là $60^\\circ$."})],
    ghi_chu="S2: lỗi tính ở B3 nhưng đáp số cuối trùng đáp số đúng — dùng để kiểm AI có bắt bước sai khi đáp số đúng; B4 không được điểm vì suy từ B3 sai."))

add(build("BM-034", src(B23, 7), "thpt", "Đường thẳng vuông góc với mặt phẳng", "VDC",
    "Tứ diện $OABC$ có $OA,OB,OC$ đôi một vuông góc, $H$ là trực tâm tam giác $ABC$. Chứng minh $OH\\perp(ABC)$.",
    "$OH\\perp(ABC)$ (chứng minh)", None, None, ["Dấu hiệu đường thẳng vuông góc mặt phẳng", "Trực tâm"],
    [S("$OA\\perp OB$, $OA\\perp OC$ nên $OA\\perp(OBC)\\Rightarrow OA\\perp BC$.", "$OA\\perp BC$", "Dấu hiệu vuông góc", 0.25),
     S("$AH\\perp BC$ (trực tâm); $OA,AH$ cắt nhau trong $(OAH)$ nên $BC\\perp(OAH)\\Rightarrow BC\\perp OH$.", "$BC\\perp OH$", "Dấu hiệu vuông góc", 0.25),
     S("Tương tự $OB\\perp(OAC)\\Rightarrow OB\\perp AC$, $BH\\perp AC$ nên $AC\\perp(OBH)\\Rightarrow AC\\perp OH$.", "$AC\\perp OH$", "Dấu hiệu vuông góc", 0.25),
     S("$BC,AC$ cắt nhau trong $(ABC)$ nên $OH\\perp(ABC)$.", "$OH\\perp(ABC)$", "Dấu hiệu vuông góc", 0.25, tc="Nêu đủ hai đường cắt nhau trong $(ABC)$")],
    [W("B4", "thieu-chung-minh", "Chỉ có $OH\\perp BC$ đã kết luận $OH\\perp(ABC)$ (bỏ B3, dùng một đường)", {"B3": "(bỏ qua)", "B4": "$OH\\perp BC$, $BC\\subset(ABC)$ nên $OH\\perp(ABC)$."}, dd=0.5),
     W("B2", "sai-dinh-li", "Suy $BC\\perp(OAH)$ chỉ từ $OA\\perp BC$ (một đường)", {"B2": "$OA\\perp BC$ nên $BC\\perp(OAH)$, do đó $BC\\perp OH$."}, dd=0.75)],
    ghi_chu=CM + ". S2: B3, B4 vẫn đúng và độc lập (lỗi ở B2 không lan) → 0,75."))

add(build("BM-035", src(B24, 3), "thpt", ALP, "TH",
    "Chóp $S.ABCD$ đáy hình vuông cạnh $a$, $SA\\perp(ABCD)$, $SA=a$. Tính góc giữa $SC$ và mặt đáy.",
    "$\\arctan\\dfrac{1}{\\sqrt2}\\approx35{,}26^\\circ$", a35, None, ["Hình chiếu vuông góc", "tan"],
    [S("$AC$ là hình chiếu của $SC$ lên $(ABCD)$; góc cần tìm là $\\widehat{SCA}$.", "$\\widehat{SCA}$", "Hình chiếu", 0.25),
     S("$AC=a\\sqrt2$.", "$AC=a\\sqrt2$", "Đường chéo hình vuông", 0.25, so=sqrt(2)),
     S("$\\tan\\widehat{SCA}=\\dfrac{a}{a\\sqrt2}=\\dfrac1{\\sqrt2}\\Rightarrow\\widehat{SCA}\\approx35{,}26^\\circ$.", "$\\tan=\\dfrac1{\\sqrt2}$", "tan", 0.5, so=a35)],
    [W("B3", "sai-dinh-li", "Dùng $\\sin\\widehat{SCA}=\\dfrac{SA}{AC}$", {"B3": "$\\sin\\widehat{SCA}=\\dfrac{SA}{AC}=\\dfrac1{\\sqrt2}\\Rightarrow 45^\\circ$."}),
     W("B1", "sai-hinh-dung", "Lấy góc $\\widehat{SCB}$ (với cạnh $BC$) làm góc với mặt đáy", {"B1": "Góc cần tìm là $\\widehat{SCB}$.", "B3": "$SB=a\\sqrt2$, $\\tan\\widehat{SCB}=\\dfrac{SB}{BC}=\\sqrt2\\Rightarrow\\approx54{,}74^\\circ$."}, dd=0.25)]))

add(build("BM-036", src(B24, 7), "thpt", ALP, "VD",
    "Chóp $S.ABCD$ đáy hình vuông cạnh $a$, $SA\\perp(ABCD)$, $SA=a$. Tính $\\sin$ của góc giữa $SC$ và $(SAB)$.",
    "$\\dfrac{\\sqrt3}{3}$", sqrt(3) / 3, None, ["Dấu hiệu vuông góc", "Hình chiếu", "sin"],
    [S("$BC\\perp AB$, $BC\\perp SA$ nên $BC\\perp(SAB)$; hình chiếu của $C$ là $B$.", "$BC\\perp(SAB)$", "Dấu hiệu vuông góc", 0.25),
     S("Góc giữa $SC$ và $(SAB)$ là $\\widehat{CSB}$.", "$\\widehat{CSB}$", "Hình chiếu", 0.25),
     S("$SC=\\sqrt{a^2+2a^2}=a\\sqrt3$, $BC=a$.", "$SC=a\\sqrt3$", "Pythagore", 0.25, so=sqrt(3)),
     S("$\\sin\\widehat{CSB}=\\dfrac{BC}{SC}=\\dfrac{\\sqrt3}3$.", "$\\dfrac{\\sqrt3}3$", "sin", 0.25, so=sqrt(3)/3)],
    [W("B4", "sai-dinh-li", "Dùng $\\sin=\\dfrac{SB}{SC}$ (cạnh kề)", {"B4": "$\\sin\\widehat{CSB}=\\dfrac{SB}{SC}=\\dfrac{a\\sqrt2}{a\\sqrt3}=\\dfrac{\\sqrt6}3$."}),
     W("B3", "sai-tinh-toan", "Tính $SC=a\\sqrt2$ (dùng $AB$ thay $AC$)", {"B3": "$SC=\\sqrt{a^2+a^2}=a\\sqrt2$.", "B4": "$\\sin\\widehat{CSB}=\\dfrac{a}{a\\sqrt2}=\\dfrac{\\sqrt2}2$."})]))

add(build("BM-037", src(B25, 5), "thpt", "Góc nhị diện", "VD",
    "Chóp $S.ABCD$ đáy hình vuông cạnh $a$, $SA\\perp(ABCD)$, $SA=a\\sqrt3$. Tính số đo góc nhị diện $[S,BC,A]$.",
    "$60^\\circ$", 60.0, None, ["Góc phẳng nhị diện", "tan"],
    [S("$BC\\perp(SAB)$ nên $BC\\perp SB$ và $BC\\perp AB$.", "$BC\\perp SB$, $BC\\perp AB$", "Dấu hiệu vuông góc", 0.25),
     S("$\\widehat{SBA}$ là góc phẳng nhị diện của $[S,BC,A]$.", "$\\widehat{SBA}$", "Góc phẳng nhị diện", 0.25),
     S("$\\tan\\widehat{SBA}=\\dfrac{a\\sqrt3}a=\\sqrt3\\Rightarrow 60^\\circ$.", "$60^\\circ$", "tan", 0.5, so=60.0)],
    [W("B2", "sai-hinh-dung", "Lấy góc $\\widehat{SCA}$ làm góc phẳng nhị diện", {"B2": "Góc phẳng nhị diện là $\\widehat{SCA}$.", "B3": "$\\tan\\widehat{SCA}=\\dfrac{a\\sqrt3}{a\\sqrt2}\\Rightarrow\\approx50{,}77^\\circ$."}),
     W("B3", "sai-tinh-toan", "Đảo tỉ số tan", {"B3": "$\\tan\\widehat{SBA}=\\dfrac{AB}{SA}=\\dfrac1{\\sqrt3}\\Rightarrow30^\\circ$."})]))

add(build("BM-038", src(B25, 6), "thpt", "Góc giữa hai mặt phẳng", "VD",
    "Chóp $S.ABCD$ đáy hình vuông cạnh $a$, $SA\\perp(ABCD)$, $SA=a$. Tính $\\tan$ của góc giữa $(SBD)$ và $(ABCD)$.",
    "$\\sqrt2$", sqrt(2), None, ["Góc giữa hai mặt phẳng qua giao tuyến", "tan"],
    [S("$O=AC\\cap BD$: $AO\\perp BD$, $SO\\perp BD$ (vì $BD\\perp(SAC)$); giao tuyến là $BD$.", "$AO\\perp BD$, $SO\\perp BD$", "Ba đường vuông góc", 0.25),
     S("Góc cần tìm là $\\widehat{SOA}$.", "$\\widehat{SOA}$", "Góc giữa hai mặt phẳng", 0.25),
     S("$AO=\\dfrac{a\\sqrt2}2$, $\\tan\\widehat{SOA}=\\dfrac{SA}{AO}=\\sqrt2$.", "$\\sqrt2$", "tan", 0.5, so=sqrt(2))],
    [W("B2", "sai-hinh-dung", "Lấy góc $\\widehat{SBA}$ (giao tuyến không phải $AB$)", {"B2": "Góc cần tìm là $\\widehat{SBA}$.", "B3": "$\\tan\\widehat{SBA}=\\dfrac aa=1$."}),
     W("B3", "sai-tinh-toan", "Lấy $AO=a\\sqrt2$", {"B3": "$AO=a\\sqrt2$, $\\tan\\widehat{SOA}=\\dfrac{\\sqrt2}2$."})]))

add(build("BM-039", src(B26, 3), "thpt", "Khoảng cách từ điểm đến mặt phẳng", "TH",
    "Chóp $S.ABC$, $SA\\perp(ABC)$, $SA=a$, tam giác $ABC$ vuông tại $B$, $AB=a$. Tính $d(A,(SBC))$.",
    "$\\dfrac{a\\sqrt2}{2}$", sqrt(2) / 2, None, ["Dựng khoảng cách", "Hệ thức lượng"],
    [S("$BC\\perp AB$, $BC\\perp SA$ nên $BC\\perp(SAB)$.", "$BC\\perp(SAB)$", "Dấu hiệu vuông góc", 0.25),
     S("Kẻ $AH\\perp SB$; $AH\\perp BC$ nên $AH\\perp(SBC)$, $d=AH$.", "$d=AH$", "Dựng khoảng cách", 0.25),
     S("$SB=a\\sqrt2$, $AH=\\dfrac{SA\\cdot AB}{SB}=\\dfrac{a\\sqrt2}2$.", "$\\dfrac{a\\sqrt2}2$", "Hệ thức lượng", 0.5, so=sqrt(2)/2)],
    [W("B3", "sai-dinh-li", "Dùng $AH=\\dfrac{SB}{SA\\cdot AB}$ (đảo công thức)", {"B3": "$AH=\\dfrac{SA\\cdot SB}{AB}=a\\sqrt2$."}),
     W("B2", "thieu-chung-minh", "Kẻ $AH\\perp SB$ rồi khẳng định $AH\\perp(SBC)$ không nêu $AH\\perp BC$", {"B2": "Kẻ $AH\\perp SB$ thì $d(A,(SBC))=AH$."}, dd=0.75)]))

add(build("BM-040", src(B26, 5), "thpt", "Khoảng cách giữa hai đường thẳng chéo nhau", "VD",
    "Hình lập phương $ABCD.A'B'C'D'$ cạnh $a$. Tính khoảng cách giữa hai đường thẳng chéo nhau $AB$ và $A'D'$.",
    "$a$", 1.0, None, ["Đoạn vuông góc chung"],
    [S("$AA'\\perp AB$ và $AA'\\perp A'D'$.", "$AA'$ vuông góc cả hai", "Cạnh bên hình lập phương", 0.5),
     S("$AA'$ cắt $AB$ tại $A$, cắt $A'D'$ tại $A'$ nên là đoạn vuông góc chung.", "Đoạn vuông góc chung $AA'$", "Định nghĩa", 0.25),
     S("$d(AB,A'D')=AA'=a$.", "$a$", "Kết luận", 0.25, so=1.0)],
    [W("B1", "sai-hinh-dung", "Lấy $AD'$ làm đoạn vuông góc chung", {"B1": "$AD'$ nối $A\\in AB$ với $D'\\in A'D'$.", "B2": "Coi $AD'$ là đoạn vuông góc chung.", "B3": "$d=AD'=a\\sqrt2$."}),
     W("B2", "thieu-chung-minh", "Không chỉ ra $AA'$ cắt cả hai đường", {"B2": "(không nêu)"}, dd=0.75)]))

add(build("BM-041", src(B26, 6), "thpt", "Khoảng cách từ điểm đến mặt phẳng", "VDC",
    "Tứ diện $OABC$ có $OA,OB,OC$ đôi một vuông góc, $OA=OB=OC=a$. Tính $d(O,(ABC))$.",
    "$\\dfrac{a\\sqrt3}{3}$", sqrt(3) / 3, None, ["Dựng khoảng cách", "Hệ thức lượng"],
    [S("Kẻ $OK\\perp BC$, $K$ trung điểm $BC$; $\\dfrac1{OK^2}=\\dfrac2{a^2}$.", "$OK^2=\\dfrac{a^2}2$", "Hệ thức lượng", 0.25, so=sqrt(2)/2),
     S("$BC\\perp(OAK)$; kẻ $OH\\perp AK$ thì $OH\\perp(ABC)$.", "$d=OH$", "Dựng khoảng cách", 0.25),
     S("$\\dfrac1{OH^2}=\\dfrac1{a^2}+\\dfrac2{a^2}=\\dfrac3{a^2}$.", "$\\dfrac1{OH^2}=\\dfrac3{a^2}$", "Hệ thức lượng", 0.25, so=3.0),
     S("$OH=\\dfrac{a\\sqrt3}3$.", "$\\dfrac{a\\sqrt3}3$", "Tính toán", 0.25, so=sqrt(3)/3)],
    [W("B3", "sai-tinh-toan", "Cộng sai: $\\dfrac1{a^2}+\\dfrac2{a^2}=\\dfrac2{a^2}$", {"B3": "$\\dfrac1{OH^2}=\\dfrac2{a^2}$.", "B4": "$OH=\\dfrac{a\\sqrt2}2$."}),
     W("B2", "sai-hinh-dung", "Coi $OK$ là khoảng cách", {"B2": "$OK\\perp BC$ nên $d(O,(ABC))=OK$.", "B3": "(bỏ)", "B4": "$d=\\dfrac{a\\sqrt2}2$."})]))

add(build("BM-042", src(B26, 7), "thpt", "Khoảng cách từ điểm đến mặt phẳng", "VD",
    "Chóp $S.ABCD$ đáy hình vuông cạnh $3$, $SA\\perp(ABCD)$, $SA=3$. Tính $d(A,(SBD))$.",
    "$\\sqrt3$", sqrt(3), None, ["Tứ diện vuông"],
    [S("$AS,AB,AD$ đôi một vuông góc nên $\\dfrac1{d^2}=\\dfrac1{AS^2}+\\dfrac1{AB^2}+\\dfrac1{AD^2}$.", "Công thức tứ diện vuông", "Tứ diện vuông", 0.5, ck=["Dựng $AO\\perp BD$, $AH\\perp SO$: $d=\\dfrac{SA\\cdot AO}{SO}$"]),
     S("$\\dfrac1{d^2}=\\dfrac13\\Rightarrow d=\\sqrt3$.", "$\\sqrt3$", "Tính toán", 0.5, so=sqrt(3))],
    [W("B2", "sai-tinh-toan", "Quên khai căn: $d=3$", {"B2": "$\\dfrac1{d^2}=\\dfrac13\\Rightarrow d=3$."}),
     W("B1", "sai-dinh-li", "Dùng $d=\\dfrac{SA\\cdot AB}{SB}$ (khoảng cách tới $(SBC)$)", {"B1": "$d=\\dfrac{SA\\cdot AB}{SB}$.", "B2": "$d=\\dfrac{9}{3\\sqrt2}=\\dfrac{3\\sqrt2}2$."})]))

add(build("BM-043", src(B27, 5), "thpt", "Thể tích khối chóp", "VD",
    "Chóp $S.ABCD$ đáy hình vuông cạnh $a$, $SA\\perp(ABCD)$, $SC$ tạo với đáy góc $45^\\circ$. Tính thể tích khối chóp.",
    "$\\dfrac{a^3\\sqrt2}{3}$", sqrt(2) / 3, None, ["Góc giữa đường thẳng và mặt phẳng", "$V=\\dfrac13Bh$"],
    [S("Góc giữa $SC$ và đáy là $\\widehat{SCA}=45^\\circ$.", "$\\widehat{SCA}=45^\\circ$", "Góc với mặt phẳng", 0.25),
     S("$AC=a\\sqrt2$, tam giác $SAC$ vuông cân nên $SA=a\\sqrt2$.", "$SA=a\\sqrt2$", "Tam giác vuông cân", 0.25, so=sqrt(2)),
     S("$V=\\dfrac13a^2\\cdot a\\sqrt2=\\dfrac{a^3\\sqrt2}3$.", "$\\dfrac{a^3\\sqrt2}3$", "Thể tích", 0.5, so=sqrt(2)/3)],
    [W("B3", "sai-dinh-li", "Quên hệ số $\\dfrac13$ của khối chóp", {"B3": "$V=a^2\\cdot a\\sqrt2=a^3\\sqrt2$."}),
     W("B2", "sai-tinh-toan", "Lấy $SA=AC=a$ (nhầm $AC=a$)", {"B2": "$AC=a$ nên $SA=a$.", "B3": "$V=\\dfrac{a^3}3$."})]))

add(build("BM-044", src(B27, 6), "thpt", "Tỉ số thể tích", "VD",
    "Chóp $S.ABC$; $M$ là trung điểm $SB$, $N\\in SC$ với $SN=2NC$. Tính $\\dfrac{V_{S.AMN}}{V_{S.ABC}}$.",
    "$\\dfrac13$", 1 / 3, None, ["Tỉ số thể tích (Simson)"],
    [S("$\\dfrac{V_{S.AMN}}{V_{S.ABC}}=\\dfrac{SA}{SA}\\cdot\\dfrac{SM}{SB}\\cdot\\dfrac{SN}{SC}$.", "Công thức tỉ số", "Tỉ số thể tích", 0.25),
     S("$\\dfrac{SM}{SB}=\\dfrac12$, $SN=2NC\\Rightarrow\\dfrac{SN}{SC}=\\dfrac23$.", "$\\dfrac12$; $\\dfrac23$", "Tỉ số đoạn", 0.5, so=2/3),
     S("Tỉ số $=\\dfrac12\\cdot\\dfrac23=\\dfrac13$.", "$\\dfrac13$", "Tính", 0.25, so=1/3)],
    [W("B2", "nham-dieu-kien", "Đọc $SN=2NC$ thành $\\dfrac{SN}{SC}=\\dfrac12$", {"B2": "$\\dfrac{SM}{SB}=\\dfrac12$, $\\dfrac{SN}{SC}=\\dfrac12$.", "B3": "Tỉ số $=\\dfrac14$."}),
     W("B1", "sai-dinh-li", "Cộng tỉ số thay vì nhân", {"B1": "$\\dfrac{V_{S.AMN}}{V_{S.ABC}}=\\dfrac{SM}{SB}+\\dfrac{SN}{SC}-1$.", "B3": "Tỉ số $=\\dfrac12+\\dfrac23-1=\\dfrac16$."}, dd=0.5)]))

add(build("BM-045", src(B27, 7), "thpt", "Thể tích khối chóp", "VDC",
    "Tính thể tích khối tứ diện đều cạnh $a$.",
    "$\\dfrac{a^3\\sqrt2}{12}$", sqrt(2) / 12, None, ["Chân đường cao tứ diện đều là trọng tâm đáy", "$V=\\dfrac13Bh$"],
    [S("$S_{BCD}=\\dfrac{a^2\\sqrt3}4$; chân đường cao $H$ là trọng tâm, $HB=\\dfrac{a\\sqrt3}3$.", "$HB=\\dfrac{a\\sqrt3}3$", "Trọng tâm tam giác đều", 0.25, so=sqrt(3)/3),
     S("$AH=\\sqrt{a^2-\\dfrac{a^2}3}=\\dfrac{a\\sqrt6}3$.", "$AH=\\dfrac{a\\sqrt6}3$", "Pythagore", 0.25, so=sqrt(6)/3),
     S("$V=\\dfrac13\\cdot\\dfrac{a^2\\sqrt3}4\\cdot\\dfrac{a\\sqrt6}3=\\dfrac{a^3\\sqrt2}{12}$.", "$\\dfrac{a^3\\sqrt2}{12}$", "Thể tích", 0.5, so=sqrt(2)/12)],
    [W("B1", "sai-dinh-li", "Lấy $HB$ bằng đường cao tam giác đều $\\dfrac{a\\sqrt3}2$", {"B1": "$HB=\\dfrac{a\\sqrt3}2$.", "B2": "$AH=\\sqrt{a^2-\\dfrac{3a^2}4}=\\dfrac a2$.", "B3": "$V=\\dfrac13\\cdot\\dfrac{a^2\\sqrt3}4\\cdot\\dfrac a2=\\dfrac{a^3\\sqrt3}{24}$."}),
     W("B3", "sai-tinh-toan", "Rút gọn sai $\\sqrt{18}=2\\sqrt3$", {"B3": "$V=\\dfrac{a^3\\sqrt{18}}{36}=\\dfrac{a^3\\sqrt3}{18}$."})]))

add(build("BM-046", src(B27, 8), "thpt", "Thể tích khối chóp", "VDC",
    "Chóp $S.ABCD$ đáy hình vuông cạnh $2a$, tam giác $SAB$ đều và nằm trong mặt phẳng vuông góc với đáy. Tính thể tích theo $a$.",
    "$\\dfrac{4\\sqrt3a^3}{3}$", 4 * sqrt(3) / 3, None, ["Hai mặt phẳng vuông góc", "$V=\\dfrac13Bh$"],
    [S("$H$ trung điểm $AB$: $SH\\perp AB$, $SH=\\dfrac{2a\\sqrt3}2=a\\sqrt3$.", "$SH=a\\sqrt3$", "Đường cao tam giác đều", 0.25, so=sqrt(3)),
     S("$(SAB)\\perp(ABCD)$, giao tuyến $AB$, $SH\\perp AB$ nên $SH\\perp(ABCD)$.", "$SH\\perp(ABCD)$", "Định lí hai mặt phẳng vuông góc", 0.25),
     S("$V=\\dfrac13\\cdot4a^2\\cdot a\\sqrt3=\\dfrac{4\\sqrt3a^3}3$.", "$\\dfrac{4\\sqrt3a^3}3$", "Thể tích", 0.5, so=4*sqrt(3)/3)],
    [W("B3", "sai-tinh-toan", "Lấy diện tích đáy $2a^2$ (quên bình phương $2$)", {"B3": "$V=\\dfrac13\\cdot2a^2\\cdot a\\sqrt3=\\dfrac{2\\sqrt3a^3}3$."}),
     W("B2", "thieu-chung-minh", "Không chứng minh $SH\\perp(ABCD)$, chỉ ghi \"$SH$ là đường cao\"", {"B2": "$SH$ là đường cao của hình chóp."}, dd=0.75)]))

add(build("BM-047", src(OT7, 3), "thpt", "Khoảng cách từ điểm đến mặt phẳng", "VD",
    "Chóp $S.ABCD$ đáy hình chữ nhật $AB=a$, $AD=2a$, $SA\\perp(ABCD)$, $SA=a$. Chứng minh $(SAB)\\perp(SBC)$ và tính $d(A,(SBC))$.",
    "$\\dfrac{a\\sqrt2}{2}$", sqrt(2) / 2, None, ["Hai mặt phẳng vuông góc", "Dựng khoảng cách"],
    [S("$BC\\perp AB$, $BC\\perp SA\\Rightarrow BC\\perp(SAB)$; $BC\\subset(SBC)$ nên $(SBC)\\perp(SAB)$.", "$(SBC)\\perp(SAB)$", "Mặt phẳng chứa đường vuông góc", 0.5),
     S("Kẻ $AH\\perp SB$; $AH\\perp BC$ nên $AH\\perp(SBC)$.", "$d=AH$", "Dựng khoảng cách", 0.25),
     S("$SB=a\\sqrt2$, $AH=\\dfrac{a^2}{a\\sqrt2}=\\dfrac{a\\sqrt2}2$.", "$\\dfrac{a\\sqrt2}2$", "Hệ thức lượng", 0.25, so=sqrt(2)/2)],
    [W("B3", "nham-dieu-kien", "Dùng $AD=2a$ thay $AB$ trong tam giác $SAB$", {"B3": "$AH=\\dfrac{SA\\cdot AD}{\\sqrt{SA^2+AD^2}}=\\dfrac{2a}{\\sqrt5}$."}),
     W("B1", "sai-dinh-li", "Kết luận $(SAB)\\perp(SBC)$ vì chúng có chung $SB$", {"B1": "$(SAB)$ và $(SBC)$ cắt nhau theo $SB$ nên vuông góc."}, dd=0.5)]))

add(build("BM-048", src(OT7, 5), "thpt", "Góc giữa hai mặt phẳng", "VD",
    "Tứ diện đều $S.ABC$ cạnh $a$. Tính góc giữa mặt bên $(SBC)$ và đáy $(ABC)$.",
    "$\\arccos\\dfrac13\\approx70{,}53^\\circ$", degrees(acos(1 / 3)), None, ["Góc giữa hai mặt phẳng", "Trọng tâm"],
    [S("$H$ trọng tâm $\\triangle ABC$ (chân đường cao); $M$ trung điểm $BC$: $AM\\perp BC$, $SM\\perp BC$.", "$AM\\perp BC$, $SM\\perp BC$", "Trung tuyến tam giác đều", 0.25),
     S("Góc cần tìm là $\\widehat{SMH}$.", "$\\widehat{SMH}$", "Góc giữa hai mặt phẳng", 0.25),
     S("$HM=\\dfrac13AM=\\dfrac{a\\sqrt3}6$, $SM=\\dfrac{a\\sqrt3}2$.", "$HM=\\dfrac{a\\sqrt3}6$", "Trọng tâm", 0.25, so=sqrt(3)/6),
     S("$\\cos\\widehat{SMH}=\\dfrac{HM}{SM}=\\dfrac13\\Rightarrow\\approx70{,}53^\\circ$.", "$\\cos=\\dfrac13$", "cos", 0.25, so=degrees(acos(1/3)))],
    [W("B3", "sai-dinh-li", "Lấy $HM=\\dfrac23AM$ (nhầm vị trí trọng tâm)", {"B3": "$HM=\\dfrac23AM=\\dfrac{a\\sqrt3}3$.", "B4": "$\\cos\\widehat{SMH}=\\dfrac23\\Rightarrow\\approx48{,}19^\\circ$."}),
     W("B2", "sai-hinh-dung", "Lấy góc $\\widehat{SAM}$ (cạnh bên với đáy)", {"B2": "Góc cần tìm là $\\widehat{SAH}$.", "B4": "$\\cos\\widehat{SAH}=\\dfrac{AH}{SA}=\\dfrac{\\sqrt3}3\\Rightarrow\\approx54{,}74^\\circ$."}, dd=0.5)]))

add(build("BM-049", src(OT7, 7), "thpt", "Khoảng cách từ điểm đến mặt phẳng", "VDC",
    "Chóp $S.ABCD$ đáy hình chữ nhật $AB=3$, $AD=6$, $SA\\perp(ABCD)$, $SA=3$. Tính $d(A,(SBD))$.",
    "$2$", 2.0, None, ["Tứ diện vuông"],
    [S("$AS,AB,AD$ đôi một vuông góc: $\\dfrac1{d^2}=\\dfrac1{SA^2}+\\dfrac1{AB^2}+\\dfrac1{AD^2}$.", "Công thức", "Tứ diện vuông", 0.25),
     S("$\\dfrac1{d^2}=\\dfrac19+\\dfrac19+\\dfrac1{36}=\\dfrac14$.", "$\\dfrac14$", "Tính", 0.5, so=0.25),
     S("$d=2$.", "$2$", "Kết luận", 0.25, so=2.0)],
    [W("B2", "sai-tinh-toan", "Quy đồng sai: $\\dfrac19+\\dfrac19+\\dfrac1{36}=\\dfrac3{36}$", {"B2": "$\\dfrac1{d^2}=\\dfrac{3}{36}=\\dfrac1{12}$.", "B3": "$d=2\\sqrt3$."}),
     W("B1", "nham-dieu-kien", "Coi đáy là hình vuông cạnh 3 (bỏ $AD=6$)", {"B1": "$\\dfrac1{d^2}=\\dfrac3{SA^2}$ (đáy vuông cạnh 3).", "B2": "$\\dfrac1{d^2}=\\dfrac13$.", "B3": "$d=\\sqrt3$."})]))

# ---------- Chương 4 ----------
add(build("BM-050", src(B10, 6), "thpt", "Giao điểm đường thẳng và mặt phẳng", "VD",
    "Chóp $S.ABCD$ đáy hình bình hành tâm $O$; $M$ là trung điểm $SC$. Tìm giao điểm $I$ của $AM$ và $(SBD)$, tính $\\dfrac{AI}{AM}$.",
    "$I=AM\\cap SO$, $\\dfrac{AI}{AM}=\\dfrac23$", 2 / 3, None, ["Giao tuyến hai mặt phẳng", "Trọng tâm tam giác"],
    [S("$AM\\subset(SAC)$, $(SAC)\\cap(SBD)=SO$.", "$(SAC)\\cap(SBD)=SO$", "Giao tuyến", 0.25),
     S("Trong $(SAC)$, $AM\\cap SO=I$; $I\\in SO\\subset(SBD)$ nên $I=AM\\cap(SBD)$.", "$I=AM\\cap SO$", "Giao điểm", 0.25),
     S("$AM,SO$ là hai trung tuyến của $\\triangle SAC$ nên $I$ là trọng tâm, $\\dfrac{AI}{AM}=\\dfrac23$.", "$\\dfrac23$", "Trọng tâm", 0.5, so=2/3)],
    [W("B3", "sai-dinh-li", "Lấy tỉ số trọng tâm là $\\dfrac12$", {"B3": "$I$ là trọng tâm nên $\\dfrac{AI}{AM}=\\dfrac12$."}),
     W("B1", "sai-hinh-dung", "Lấy giao tuyến $(SAC)\\cap(SBD)=SB$", {"B1": "$(SAC)\\cap(SBD)=SB$.", "B2": "$I=AM\\cap SB$.", "B3": "Không xác định được tỉ số ($AM$ và $SB$ chéo nhau)."})]))

add(build("BM-051", src(B11, 7), "thpt", "Hai đường thẳng song song", "VDC",
    "Tứ diện $ABCD$, $G_1,G_2$ là trọng tâm $\\triangle ABC$, $\\triangle ABD$. Chứng minh $G_1G_2\\parallel CD$ và tính $\\dfrac{G_1G_2}{CD}$.",
    "$\\dfrac13$", 1 / 3, None, ["Trọng tâm", "Định lí Thalès đảo"],
    [S("$E$ trung điểm $AB$: $G_1\\in EC$, $\\dfrac{EG_1}{EC}=\\dfrac13$; $G_2\\in ED$, $\\dfrac{EG_2}{ED}=\\dfrac13$.", "$\\dfrac{EG_1}{EC}=\\dfrac{EG_2}{ED}=\\dfrac13$", "Trọng tâm", 0.5),
     S("Trong $\\triangle ECD$, theo Thalès đảo $G_1G_2\\parallel CD$.", "$G_1G_2\\parallel CD$", "Thalès đảo", 0.25),
     S("$\\triangle EG_1G_2\\sim\\triangle ECD$ tỉ số $\\dfrac13$: $\\dfrac{G_1G_2}{CD}=\\dfrac13$.", "$\\dfrac13$", "Đồng dạng", 0.25, so=1/3)],
    [W("B1", "sai-dinh-li", "Lấy $\\dfrac{EG_1}{EC}=\\dfrac23$ (nhầm tỉ số từ đỉnh)", {"B1": "$\\dfrac{EG_1}{EC}=\\dfrac{EG_2}{ED}=\\dfrac23$.", "B3": "$\\dfrac{G_1G_2}{CD}=\\dfrac23$."}, dd=0.25),
     W("B2", "thieu-chung-minh", "Khẳng định $G_1G_2\\parallel CD$ không nêu tỉ số bằng nhau / Thalès", {"B2": "Dễ thấy $G_1G_2\\parallel CD$."}, dd=0.75)],
    ghi_chu="S1: kết luận song song (B2) vẫn đúng do hai tỉ số bằng nhau → được 0,25."))

add(build("BM-052", src(B12, 5), "thpt", "Đường thẳng song song mặt phẳng", "VD",
    "Chóp $S.ABCD$ đáy hình bình hành, $M$ là trung điểm $SA$. Chứng minh $SC\\parallel(MBD)$.",
    "$SC\\parallel(MBD)$ (chứng minh)", None, None, ["Đường trung bình", "Dấu hiệu đường thẳng song song mặt phẳng"],
    [S("$O=AC\\cap BD$ là trung điểm $AC$.", "$O$ trung điểm $AC$", "Hình bình hành", 0.25),
     S("$OM$ là đường trung bình $\\triangle SAC$ nên $OM\\parallel SC$, $OM\\subset(MBD)$.", "$OM\\parallel SC$", "Đường trung bình", 0.5),
     S("$SC\\not\\subset(MBD)$ nên $SC\\parallel(MBD)$.", "$SC\\parallel(MBD)$", "Dấu hiệu", 0.25, tc="Phải nêu $SC\\not\\subset(MBD)$")],
    [W("B3", "thieu-chung-minh", "Không nêu $SC\\not\\subset(MBD)$", {"B3": "Vậy $SC\\parallel(MBD)$."}),
     W("B2", "sai-hinh-dung", "Dùng $MB$ làm đường trung bình song song $SC$", {"B2": "$MB$ là đường trung bình nên $MB\\parallel SC$."})],
    ghi_chu=CM + ". S1: chỉ mất 0,25 của B3."))

add(build("BM-053", src(B13, 6), "thpt", "Định lí Thalès trong không gian", "VD",
    "Ba mặt phẳng song song $(P),(Q),(R)$ cắt hai cát tuyến lần lượt tại $A,B,C$ và $A',B',C'$. Biết $\\dfrac{AB}{BC}=\\dfrac23$ và $A'C'=15$. Tính $A'B'$.",
    "$6$", 6.0, None, ["Định lí Thalès trong không gian"],
    [S("$\\dfrac{A'B'}{B'C'}=\\dfrac{AB}{BC}=\\dfrac23$.", "$\\dfrac{A'B'}{B'C'}=\\dfrac23$", "Thalès", 0.5),
     S("$\\dfrac{A'B'}{A'C'}=\\dfrac25$ nên $A'B'=6$.", "$6$", "Tỉ lệ", 0.5, so=6.0)],
    [W("B2", "sai-tinh-toan", "Lấy $\\dfrac{A'B'}{A'C'}=\\dfrac23$", {"B2": "$A'B'=\\dfrac23\\cdot15=10$."}),
     W("B1", "sai-dinh-li", "Đảo tỉ số: $\\dfrac{A'B'}{B'C'}=\\dfrac32$", {"B1": "$\\dfrac{A'B'}{B'C'}=\\dfrac{BC}{AB}=\\dfrac32$.", "B2": "$A'B'=\\dfrac35\\cdot15=9$."})]))

add(build("BM-054", src(OT4, 4), "thpt", "Giao tuyến – định lí giao tuyến song song", "VD",
    "Chóp $S.ABCD$ đáy hình bình hành. $M$ là trung điểm $SC$. Mặt phẳng $(ABM)$ cắt $SD$ tại $N$. Chứng minh $N$ là trung điểm $SD$.",
    "$N$ là trung điểm $SD$ (chứng minh)", None, None, ["Định lí giao tuyến", "Đường trung bình"],
    [S("$AB\\parallel CD\\subset(SCD)$ nên $AB\\parallel(SCD)$.", "$AB\\parallel(SCD)$", "Dấu hiệu", 0.25),
     S("$(ABM)\\cap(SCD)=MN$ nên $MN\\parallel AB\\parallel CD$.", "$MN\\parallel CD$", "Định lí giao tuyến", 0.5),
     S("Trong $\\triangle SCD$, $M$ trung điểm $SC$, $MN\\parallel CD$ nên $N$ trung điểm $SD$.", "$N$ trung điểm $SD$", "Đường trung bình", 0.25)],
    [W("B2", "thieu-chung-minh", "Khẳng định $MN\\parallel CD$ không dựa vào định lí giao tuyến", {"B2": "$MN\\parallel CD$."}, dd=0.5),
     W("B3", "sai-dinh-li", "Dùng Thalès sai: kết luận $SN=2ND$", {"B3": "$MN\\parallel CD$ nên $\\dfrac{SN}{ND}=\\dfrac{SC}{SM}=2$."})],
    ghi_chu=CM + ". S1: B3 vẫn đúng logic nên được 0,25."))

add(build("BM-055", src(OT4, 7), "thpt", "Mặt phẳng song song – thiết diện", "VDC",
    "Chóp $S.ABCD$ đáy hình bình hành có diện tích $36$. $M\\in SA$ với $SM=\\dfrac13SA$; mặt phẳng $(\\alpha)$ qua $M$ song song $(ABCD)$ cắt $SB,SC,SD$ tại $N,P,Q$. Tính diện tích $MNPQ$.",
    "$4$", 4.0, None, ["Thalès", "Tỉ số diện tích hình đồng dạng $k^2$"],
    [S("$(\\alpha)\\parallel(ABCD)$ nên $\\dfrac{SM}{SA}=\\dfrac{SN}{SB}=\\dfrac{SP}{SC}=\\dfrac{SQ}{SD}=\\dfrac13$.", "$k=\\dfrac13$", "Thalès", 0.25, so=1/3),
     S("$MNPQ$ đồng dạng $ABCD$ tỉ số $k=\\dfrac13$.", "Đồng dạng tỉ số $\\dfrac13$", "Đồng dạng", 0.25),
     S("$S_{MNPQ}=k^2S_{ABCD}=\\dfrac19\\cdot36=4$.", "$4$", "Tỉ số diện tích", 0.5, so=4.0)],
    [W("B3", "sai-dinh-li", "Dùng tỉ số diện tích bằng $k$", {"B3": "$S_{MNPQ}=k\\cdot S_{ABCD}=12$."}),
     W("B1", "nham-dieu-kien", "Đọc $SM=\\dfrac13SA$ thành $k=\\dfrac23$ (tính từ $A$)", {"B1": "$\\dfrac{SM}{SA}=\\dfrac23$.", "B2": "Đồng dạng tỉ số $\\dfrac23$.", "B3": "$S=\\dfrac49\\cdot36=16$."})]))

add(build("BM-056", src(B11, 6), "thpt", "Đường trung bình trong tứ diện", "TH",
    "Tứ diện $ABCD$ có $M,N,P,Q$ lần lượt là trung điểm $AB,BC,CD,DA$; $AC=6$, $BD=8$. Tính chu vi tứ giác $MNPQ$.",
    "$14$", 14.0, None, ["Đường trung bình tam giác"],
    [S("$MN=PQ=\\dfrac{AC}2=3$.", "$3$", "Đường trung bình", 0.25, so=3.0),
     S("$NP=MQ=\\dfrac{BD}2=4$.", "$4$", "Đường trung bình", 0.25, so=4.0),
     S("Chu vi $=2(3+4)=14$.", "$14$", "Chu vi", 0.5, so=14.0)],
    [W("B3", "sai-tinh-toan", "Chỉ cộng hai cạnh: $3+4=7$", {"B3": "Chu vi $=3+4=7$."}),
     W("B1", "sai-dinh-li", "Lấy $MN=AC=6$ (quên một nửa)", {"B1": "$MN=PQ=AC=6$.", "B3": "Chu vi $=2(6+4)=20$."}, dd=0.25)]))

# ---------- Đại số / giải tích (bước rõ) ----------
OT = lambda ch: f"{ch}/on-tap-chuong.md"
add(build("BM-057", src(OT("chuong-01-ham-so-luong-giac-va-phuong-trinh-luong-giac"), 4), "thpt", "Phương trình lượng giác", "VD",
    "Giải phương trình $2\\cos^2x-3\\cos x+1=0$.",
    "$x=k2\\pi$ hoặc $x=\\pm\\dfrac\\pi3+k2\\pi$ ($k\\in\\mathbb Z$)", None, None, ["Đặt ẩn phụ", "Phương trình $\\cos x=m$"],
    [S("Đặt $t=\\cos x\\in[-1;1]$: $2t^2-3t+1=0\\Rightarrow t=1$ hoặc $t=\\dfrac12$.", "$t\\in\\{1;\\frac12\\}$", "Phương trình bậc hai", 0.5, tc="Có điều kiện $t\\in[-1;1]$ và hai nghiệm đúng"),
     S("$\\cos x=1\\Rightarrow x=k2\\pi$.", "$x=k2\\pi$", "PT lượng giác cơ bản", 0.25),
     S("$\\cos x=\\dfrac12\\Rightarrow x=\\pm\\dfrac\\pi3+k2\\pi$.", "$x=\\pm\\frac\\pi3+k2\\pi$", "PT lượng giác cơ bản", 0.25)],
    [W("B1", "sai-tinh-toan", "Giải sai PT bậc hai: $t=-1$ hoặc $t=-\\dfrac12$", {"B1": "$t=-1$ hoặc $t=-\\dfrac12$.", "B2": "$\\cos x=-1\\Rightarrow x=\\pi+k2\\pi$.", "B3": "$\\cos x=-\\dfrac12\\Rightarrow x=\\pm\\dfrac{2\\pi}3+k2\\pi$."}),
     W("B3", "sai-dinh-li", "Viết nghiệm $\\cos x=\\dfrac12$ như phương trình sin", {"B3": "$\\cos x=\\dfrac12\\Rightarrow x=\\dfrac\\pi3+k2\\pi$ hoặc $x=\\pi-\\dfrac\\pi3+k2\\pi$."})],
    ghi_chu="Đáp số là họ nghiệm, không có giá trị số duy nhất."))

add(build("BM-058", src(OT("chuong-02-day-so-cap-so-cong-cap-so-nhan"), 4), "thpt", "Cấp số cộng", "VD",
    "Tìm $n$ để $1+3+5+\\dots+(2n-1)=169$.",
    "$n=13$", 13.0, None, ["Tổng $n$ số hạng đầu cấp số cộng"],
    [S("Các số hạng lập cấp số cộng $u_1=1$, $d=2$, $u_n=2n-1$.", "$u_1=1,d=2$", "Cấp số cộng", 0.25),
     S("$S_n=\\dfrac{n(1+2n-1)}2=n^2$.", "$S_n=n^2$", "Tổng cấp số cộng", 0.5),
     S("$n^2=169\\Rightarrow n=13$ (loại $n=-13$).", "$n=13$", "Điều kiện $n\\in\\mathbb N^*$", 0.25, so=13.0)],
    [W("B2", "sai-dinh-li", "Dùng $S_n=n(u_1+u_n)$ (thiếu chia 2)", {"B2": "$S_n=n(1+2n-1)=2n^2$.", "B3": "$2n^2=169$ — không có $n$ nguyên; kết luận $n\\approx9{,}19$."}),
     W("B3", "nham-dieu-kien", "Giữ cả nghiệm $n=-13$", {"B3": "$n^2=169\\Rightarrow n=\\pm13$."})]))

add(build("BM-059", src(OT("chuong-06-ham-so-mu-va-logarit"), 4), "thpt", "Phương trình logarit", "VD",
    "Giải phương trình $\\log_3(x^2-3x+2)=\\log_3(x+10)$.",
    "$x=2\\pm2\\sqrt3$", None, None, ["Điều kiện xác định logarit", "Phương trình $\\log_a f=\\log_a g$"],
    [S("Điều kiện $x^2-3x+2>0$ và $x+10>0$.", "ĐK", "Điều kiện logarit", 0.25),
     S("PT $\\Leftrightarrow x^2-3x+2=x+10\\Leftrightarrow x^2-4x-8=0\\Leftrightarrow x=2\\pm2\\sqrt3$.", "$x=2\\pm2\\sqrt3$", "Phương trình bậc hai", 0.5),
     S("Hai nghiệm thỏa $x+10>0$ (và khi đó $x^2-3x+2=x+10>0$): nhận cả hai.", "$x=2\\pm2\\sqrt3$", "Đối chiếu điều kiện", 0.25)],
    [W("B3", "nham-dieu-kien", "Loại nghiệm âm $2-2\\sqrt3$ vì nghĩ $x$ phải dương", {"B3": "$2-2\\sqrt3<0$ nên loại; nghiệm $x=2+2\\sqrt3$."}),
     W("B2", "sai-tinh-toan", "Chuyển vế sai: $x^2-2x-8=0$", {"B2": "$x^2-3x+2=x+10\\Leftrightarrow x^2-2x-8=0\\Leftrightarrow x=4$ hoặc $x=-2$.", "B3": "Thử ĐK: $x=4$ và $x=-2$ đều thỏa; nghiệm $x\\in\\{4;-2\\}$."})],
    ghi_chu="Hai nghiệm; không có giá trị số duy nhất."))

add(build("BM-060", src(OT("chuong-05-gioi-han-ham-so-lien-tuc"), 5), "thpt", "Hàm số liên tục", "VD",
    "Tìm $a$ để $f(x)=\\begin{cases}\\dfrac{\\sqrt{x+3}-2}{x-1}&x\\ne1\\\\ a&x=1\\end{cases}$ liên tục tại $x=1$.",
    "$a=\\dfrac14$", 0.25, None, ["Định nghĩa liên tục", "Nhân liên hợp"],
    [S("Cần $\\lim_{x\\to1}f(x)=f(1)=a$.", "$\\lim f=a$", "Định nghĩa liên tục", 0.25),
     S("$\\dfrac{\\sqrt{x+3}-2}{x-1}=\\dfrac{1}{\\sqrt{x+3}+2}$ ($x\\ne1$).", "$\\dfrac1{\\sqrt{x+3}+2}$", "Nhân liên hợp", 0.5),
     S("Giới hạn $=\\dfrac14$, vậy $a=\\dfrac14$.", "$\\dfrac14$", "Thay giá trị", 0.25, so=0.25)],
    [W("B2", "sai-tinh-toan", "Nhân liên hợp sai dấu: được $\\dfrac1{\\sqrt{x+3}-2}$", {"B2": "$\\dfrac{\\sqrt{x+3}-2}{x-1}=\\dfrac1{\\sqrt{x+3}-2}$.", "B3": "Giới hạn không tồn tại hữu hạn; không có $a$."}),
     W("B1", "sai-dinh-li", "Cho rằng chỉ cần $f(1)$ xác định nên mọi $a$ đều được", {"B1": "$f(1)=a$ xác định nên $f$ liên tục với mọi $a$.", "B3": "Vậy mọi $a\\in\\mathbb R$."}, dd=0.5)],
    ghi_chu="S2: B2 (rút gọn) đúng và độc lập → 0,5."))

add(build("BM-061", src(OT("chuong-08-quy-tac-tinh-xac-suat"), 5), "thpt", "Quy tắc nhân xác suất", "VD",
    "Một cầu thủ sút phạt đền thành công với xác suất $0{,}8$. Sút $3$ lần độc lập, tính xác suất thành công đúng $2$ lần.",
    "$0{,}384$", 0.384, None, ["Quy tắc nhân (biến cố độc lập)", "Quy tắc cộng (xung khắc)"],
    [S("Có $3$ cách chọn lần trượt.", "$3$ cách", "Đếm", 0.25),
     S("Mỗi cách có xác suất $0{,}8\\cdot0{,}8\\cdot0{,}2=0{,}128$.", "$0{,}128$", "Quy tắc nhân", 0.5, so=0.128),
     S("Các cách xung khắc nên $P=3\\cdot0{,}128=0{,}384$.", "$0{,}384$", "Quy tắc cộng", 0.25, so=0.384)],
    [W("B1", "thieu-chung-minh", "Bỏ qua số cách chọn lần trượt (chỉ tính một thứ tự)", {"B1": "(không xét thứ tự)", "B3": "$P=0{,}128$."}, dd=0.5),
     W("B2", "sai-tinh-toan", "Nhân sai: $0{,}8^2\\cdot0{,}2=0{,}16$", {"B2": "$0{,}8\\cdot0{,}8\\cdot0{,}2=0{,}16$.", "B3": "$P=3\\cdot0{,}16=0{,}48$."})],
    ghi_chu="S1: B2 đúng → 0,5."))
