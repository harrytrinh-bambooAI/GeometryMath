"""10 bài từ hai hướng dẫn chấm (HDC) chính thức tải được ngày 27/11/2026, giữ nguyên thang điểm gốc
(kể cả bậc 0,125 của Quảng Nam). Đề và thang điểm được dẫn kèm nguồn + trang; lời diễn đạt bước là
tóm tắt của HDC, không chép nguyên trình bày.
- QN: Sở GD&ĐT Quảng Nam, HDC chính thức môn Toán, tuyển sinh lớp 10 THPT công lập 2025-2026 (4 trang).
- QO: Phòng GD&ĐT Quốc Oai (Hà Nội), đề thi thử vào 10 lần 1 năm học 2024-2025 + hướng dẫn chấm (6 trang PDF) = LG-004.
"""
from _lib import S, W, build

QN_URL = "https://storage-vnportal.vnpt.vn/qnm-ubnd/6141/ẢNH/2. HDC_Toan (chung)_TS10_2526.pdf"
QO_URL = "https://cdn.thuvienphapluat.vn/uploads/Hoidapphapluat/2024/TTTT/240502/Toan/HN/de-thi-thu-toan-vao-lop-10-lan-1-nam-2024-2025-phong-gddt-quoc-oai-ha-noi.pdf"


def qn(bai, trang):
    return {"loai": "de-chinh-thuc", "ref": f"Sở GD&ĐT Quảng Nam — HDC chính thức Toán TS10 2025-2026, {bai}, trang {trang}/4 — {QN_URL} (tải 27/11/2026)"}


def qo(cau, trang):
    return {"loai": "de-chinh-thuc", "ref": f"LG-004 Phòng GD&ĐT Quốc Oai — đề thi thử vào 10 lần 1 (2024-2025) + HDC, {cau}, đề trang 1, HDC trang {trang}/6 — {QO_URL} (tải 27/11/2026)"}


OFF = "chinh-thuc"
items = []
add = items.append

add(build("BM-062", qn("Bài 2a", 1), "thcs", "Phương trình bậc hai", "NB",
    "Dùng công thức nghiệm của phương trình bậc hai, giải phương trình $x^2-5x+6=0$.",
    "$x_1=2;\\ x_2=3$", None, None, ["Công thức nghiệm $x=\\dfrac{-b\\pm\\sqrt\\Delta}{2a}$"],
    [S("$\\Delta=(-5)^2-4\\cdot6=1$.", "$\\Delta=1$", "Biệt thức", 0.25, tc="Tính đúng $\\Delta=1$ (ghi đúng công thức $\\Delta$ được 0,125)", loi=[("Chỉ ghi đúng công thức $\\Delta$, tính sai giá trị", 0.125)], so=1.0),
     S("$x_1=\\dfrac{5-1}2=2$; $x_2=\\dfrac{5+1}2=3$.", "$x_1=2;x_2=3$", "Công thức nghiệm", 0.25, tc="Đúng mỗi nghiệm được 0,125", loi=[("Đúng một nghiệm", 0.125)])],
    [W("B1", "sai-tinh-toan", "Tính $\\Delta=25+24=49$ (sai dấu $-4ac$)", {"B1": "$\\Delta=25+24=49$.", "B2": "$x=\\dfrac{5\\pm7}2$: $x_1=6$, $x_2=-1$."}, dd=0.125),
     W("B2", "sai-dinh-li", "Quên dấu trừ của $b$: $x=\\dfrac{b\\pm\\sqrt\\Delta}{2a}$ với $b=-5$", {"B2": "$x=\\dfrac{-5\\pm1}2$: $x_1=-2$, $x_2=-3$."})],
    bieu_diem=OFF, ghi_chu="Thang gốc HDC: 0,5 = 0,25 + 0,25 với điểm thành phần 0,125. S1: công thức $\\Delta$ ghi đúng nên được 0,125 theo HDC."))

add(build("BM-063", qn("Bài 2c", 1), "thcs", "Hệ thức Vi-ét", "TH",
    "Cho phương trình $x^2-5x+2=0$ có hai nghiệm $x_1,x_2$ đều khác $0$. Không giải phương trình, hãy tính $B=\\dfrac1{x_1}+\\dfrac1{x_2}$.",
    "$B=\\dfrac52$", 2.5, None, ["Hệ thức Vi-ét"],
    [S("$x_1+x_2=5$, $x_1x_2=2$.", "$S=5;P=2$", "Vi-ét", 0.25, tc="Đúng mỗi ý được 0,125", loi=[("Sai dấu tổng $x_1+x_2=-5$", 0.125)]),
     S("$B=\\dfrac{x_1+x_2}{x_1x_2}=\\dfrac52$.", "$\\dfrac52$", "Quy đồng", 0.25, tc="Biến đổi đúng $B=\\dfrac{x_1+x_2}{x_1x_2}$ (0,125); tính đúng $\\dfrac52$ (0,125)", so=2.5)],
    [W("B1", "sai-dinh-li", "Viết Vi-ét sai dấu: $x_1+x_2=-5$", {"B1": "$x_1+x_2=-5$, $x_1x_2=2$.", "B2": "$B=\\dfrac{x_1+x_2}{x_1x_2}=-\\dfrac52$."}, dd=0.25),
     W("B2", "sai-tinh-toan", "Cộng phân số sai: $\\dfrac1{x_1}+\\dfrac1{x_2}=\\dfrac1{x_1+x_2}$", {"B2": "$B=\\dfrac1{x_1+x_2}=\\dfrac15$."})],
    bieu_diem=OFF, ghi_chu="S1 theo HDC: tích đúng (0,125) + bước biến đổi $B=\\frac{x_1+x_2}{x_1x_2}$ đúng (0,125) = 0,25."))

add(build("BM-064", qn("Bài 3a", 2), "thcs", "Giải bài toán bằng cách lập hệ phương trình", "TH",
    "Một cửa hàng bán bút bi giá $5\\,000$ đồng/cây và bút máy giá $15\\,000$ đồng/cây. Một ngày chủ nhật cửa hàng bán tổng cộng $50$ cây hai loại, thu $520\\,000$ đồng. Hỏi bán được bao nhiêu cây mỗi loại?",
    "$23$ bút bi, $27$ bút máy", None, None, ["Lập hệ phương trình bậc nhất hai ẩn"],
    [S("Gọi $x,y$ là số bút bi, bút máy ($x,y\\in\\mathbb N$, $x,y\\le50$).", "Ẩn và điều kiện", "Đặt ẩn", 0.125),
     S("$x+y=50$ (1).", "$x+y=50$", "Lập phương trình", 0.125),
     S("Tiền bút bi $5\\,000x$, tiền bút máy $15\\,000y$.", "$5000x$; $15000y$", "Biểu thị đại lượng", 0.125, tc="Đúng một trong hai ý được 0,125"),
     S("$5\\,000x+15\\,000y=520\\,000$ (2).", "PT (2)", "Lập phương trình", 0.125),
     S("Từ (1), (2) có hệ phương trình.", "Hệ (*)", "Lập hệ", 0.125),
     S("Giải hệ được $x=23$, $y=27$ (thỏa mãn); kết luận.", "$x=23,y=27$", "Giải hệ", 0.125, so=23.0)],
    [W("B6", "sai-tinh-toan", "Giải hệ sai ra $x=24$, $y=26$", {"B6": "Giải hệ được $x=24$, $y=26$; vậy bán $24$ bút bi, $26$ bút máy."}),
     W("B4", "nham-dieu-kien", "Gán nhầm giá: $15\\,000x+5\\,000y=520\\,000$", {"B4": "$15\\,000x+5\\,000y=520\\,000$ (2).", "B6": "Giải được $x=27$, $y=23$; vậy bán $27$ bút bi, $23$ bút máy."})],
    bieu_diem=OFF, ghi_chu="Thang HDC 0,75 chia 6 ý × 0,125. S2: B5 (lập hệ từ (1),(2)) về hình thức vẫn đúng nhưng chứa PT sai nên không cho điểm; được B1–B3 = 0,375."))

add(build("BM-065", qn("Bài 3b", 2), "thcs", "Xác suất – không gian mẫu", "TH",
    "Một tàu điện có ba toa số $1,2,3$ mở cửa đón khách. Hương và Giang mỗi người chọn ngẫu nhiên một toa để lên. Mô tả không gian mẫu và tính xác suất của biến cố $E$: \"Hương và Giang cùng lên một toa\".",
    "$P(E)=\\dfrac13$", 1 / 3, None, ["Không gian mẫu", "Xác suất cổ điển"],
    [S("Kết quả là cặp $(a,b)$: Hương lên toa $a$, Giang lên toa $b$.", "Mô tả kết quả", "Mô tả phép thử", 0.125),
     S("$\\Omega=\\{(1,1);(1,2);(1,3);(2,1);(2,2);(2,3);(3,1);(3,2);(3,3)\\}$ (lập bảng).", "$\\Omega$ có 9 phần tử", "Liệt kê không gian mẫu", 0.25, tc="Đủ 9 phần tử (đúng 4 phần tử được 0,125)", ck=["Kí hiệu $H_iG_j$ (HDC cho cách khác đủ điểm)"]),
     S("$n(\\Omega)=9$.", "$9$", "Đếm", 0.125, so=9.0),
     S("Kết quả thuận lợi: $(1,1);(2,2);(3,3)$.", "3 kết quả", "Biến cố", 0.125, so=3.0),
     S("$P(E)=\\dfrac39=\\dfrac13$.", "$\\dfrac13$", "Xác suất cổ điển", 0.125, so=1/3)],
    [W("B2", "sai-hinh-dung", "Coi kết quả không có thứ tự: $\\Omega$ gồm 6 cặp $\\{1,1\\},\\{1,2\\},\\dots$", {"B2": "$\\Omega=\\{\\{1,1\\};\\{1,2\\};\\{1,3\\};\\{2,2\\};\\{2,3\\};\\{3,3\\}\\}$.", "B3": "$n(\\Omega)=6$.", "B5": "$P(E)=\\dfrac36=\\dfrac12$."}, dd=0.25),
     W("B2", "thieu-chung-minh", "Không mô tả/liệt kê không gian mẫu, chỉ ghi $n(\\Omega)=9$", {"B2": "(không liệt kê)"}, dd=0.5)],
    bieu_diem=OFF, ghi_chu="Hai lời giải sai cùng bước B2 nhưng khác loại lỗi. S1: B1 (0,125) và B4 (3 kết quả thuận lợi, 0,125) vẫn đúng = 0,25."))

add(build("BM-066", qn("Bài 5", 4), "thcs", "Thể tích hình trụ – hình cầu (thực tế)", "VD",
    "Một ly thủy tinh hình trụ bán kính đáy $R=3$ cm, mực nước ban đầu cao $5$ cm. Thả vào ly $6$ viên bi sắt hình cầu bán kính $r=1$ cm thì nước dâng lên, không tràn. Tính chiều cao mực nước sau khi thả bi (bỏ qua độ dày ly, làm tròn đến hàng phần trăm).",
    "$h=\\dfrac{53}9\\approx5{,}89$ cm", 53 / 9, None, ["$V_{trụ}=\\pi R^2h$", "$V_{cầu}=\\dfrac43\\pi r^3$"],
    [S("$V_1=\\pi\\cdot3^2\\cdot5=45\\pi$ cm$^3$.", "$45\\pi$", "Thể tích trụ", 0.125, so=45*3.141592653589793),
     S("$V_2=6\\cdot\\dfrac43\\pi\\cdot1^3=8\\pi$; tổng $V=V_1+V_2=53\\pi$ cm$^3$.", "$53\\pi$", "Thể tích cầu", 0.125, so=53*3.141592653589793),
     S("Gọi $h$ là chiều cao mực nước mới: $9\\pi h=53\\pi$.", "$9\\pi h=53\\pi$", "Lập phương trình", 0.125),
     S("$h=\\dfrac{53}9\\approx5{,}89$ cm.", "$5{,}89$", "Tính và làm tròn", 0.125, so=53/9)],
    [W("B2", "sai-dinh-li", "Dùng công thức diện tích mặt cầu $4\\pi r^2$ cho thể tích bi", {"B2": "$V_2=6\\cdot4\\pi\\cdot1^2=24\\pi$; $V=69\\pi$.", "B3": "$9\\pi h=69\\pi$.", "B4": "$h=\\dfrac{69}9\\approx7{,}67$ cm."}),
     W("B4", "sai-tinh-toan", "Chia sai $\\dfrac{53}9\\approx5{,}98$", {"B4": "$h=\\dfrac{53}9\\approx5{,}98$ cm."})],
    bieu_diem=OFF, ghi_chu="Bài hình không gian (trụ + cầu) duy nhất có HDC chính thức. Thang HDC 0,5 chia 4 ý × 0,125 (ý $V_2$ nằm trong ý tổng $53\\pi$)."))

add(build("BM-067", qo("Câu 1b", 2), "thcs", "Rút gọn biểu thức chứa căn", "TH",
    "Rút gọn $B=\\dfrac{2\\sqrt x}{\\sqrt x-1}-\\dfrac{\\sqrt x+1}{\\sqrt x+2}-\\dfrac{2\\sqrt x+4}{x+\\sqrt x-2}$ với $x\\ge0$, $x\\ne1$.",
    "$B=\\dfrac{\\sqrt x+3}{\\sqrt x+2}$", None, None, ["Phân tích $x+\\sqrt x-2=(\\sqrt x-1)(\\sqrt x+2)$", "Quy đồng"],
    [S("$x+\\sqrt x-2=(\\sqrt x-1)(\\sqrt x+2)$; quy đồng với mẫu chung đó.", "MTC $(\\sqrt x-1)(\\sqrt x+2)$", "Phân tích nhân tử", 0.25),
     S("Tử $=2x+4\\sqrt x-x+1-2\\sqrt x-4=x+2\\sqrt x-3$.", "$x+2\\sqrt x-3$", "Biến đổi", 0.25),
     S("$x+2\\sqrt x-3=(\\sqrt x-1)(\\sqrt x+3)$ nên $B=\\dfrac{\\sqrt x+3}{\\sqrt x+2}$.", "$\\dfrac{\\sqrt x+3}{\\sqrt x+2}$", "Rút gọn", 0.25),
     S("Kết luận $B=\\dfrac{\\sqrt x+3}{\\sqrt x+2}$ với $x\\ge0,x\\ne1$.", "KL", "Kết luận kèm ĐK", 0.25)],
    [W("B2", "sai-tinh-toan", "Khai triển sai $-(\\sqrt x+1)(\\sqrt x-1)=-x-1$", {"B2": "Tử $=2x+4\\sqrt x-x-1-2\\sqrt x-4=x+2\\sqrt x-5$.", "B3": "Không phân tích được; $B=\\dfrac{x+2\\sqrt x-5}{(\\sqrt x-1)(\\sqrt x+2)}$.", "B4": "Kết luận $B=\\dfrac{x+2\\sqrt x-5}{x+\\sqrt x-2}$."}),
     W("B4", "nham-dieu-kien", "Kết luận không kèm điều kiện, ghi \"với mọi $x\\ge0$\"", {"B4": "Vậy $B=\\dfrac{\\sqrt x+3}{\\sqrt x+2}$ với mọi $x\\ge0$."})],
    bieu_diem=OFF, ghi_chu="Thang HDC 1đ = 4 × 0,25. Biểu thức B đọc từ PDF đã kiểm lại bằng dòng biến đổi tử số trong HDC."))

add(build("BM-068", qo("Câu 1c", 2), "thcs", "Giá trị nguyên của biểu thức", "VD",
    "Cho $A=\\dfrac{\\sqrt x-5}{\\sqrt x+3}$ và $B=\\dfrac{\\sqrt x+3}{\\sqrt x+2}$ ($x\\ge0,x\\ne1$). Đặt $P=A\\cdot B$. Tìm $x$ để $P$ có giá trị là số nguyên.",
    "$x\\in\\left\\{25;\\dfrac94;\\dfrac19\\right\\}$", None, None, ["Tách phần nguyên", "Đánh giá miền giá trị"],
    [S("$P=\\dfrac{\\sqrt x-5}{\\sqrt x+2}=1-\\dfrac7{\\sqrt x+2}$.", "$P=1-\\dfrac7{\\sqrt x+2}$", "Tách phần nguyên", 0.25),
     S("$0<\\dfrac7{\\sqrt x+2}\\le\\dfrac72$ nên $\\dfrac7{\\sqrt x+2}\\in\\{1;2;3\\}$, được $x=25;\\dfrac94;\\dfrac19$ (đều thỏa ĐK).", "$x\\in\\{25;\\frac94;\\frac19\\}$", "Đánh giá miền giá trị", 0.25)],
    [W("B2", "nham-dieu-kien", "Chỉ xét ước nguyên của 7 (bỏ trường hợp $\\sqrt x$ không nguyên)", {"B2": "$\\sqrt x+2\\in$ Ư$(7)$, $\\sqrt x+2>0$ nên $\\sqrt x+2=7$, $x=25$."}),
     W("B1", "sai-tinh-toan", "Rút gọn sai $P=1-\\dfrac3{\\sqrt x+2}$", {"B1": "$P=\\dfrac{\\sqrt x-5}{\\sqrt x+2}=1-\\dfrac3{\\sqrt x+2}$.", "B2": "$0<\\dfrac3{\\sqrt x+2}\\le\\dfrac32$ nên bằng $1$, $x=1$ (loại). Không có $x$."})],
    bieu_diem=OFF))

add(build("BM-069", qo("Câu 2.1", 3), "thcs", "Giải bài toán bằng cách lập phương trình", "VD",
    "Một đội xe nhận kế hoạch chở $360$ tấn hàng, chia đều cho các xe. Lúc khởi hành có $3$ xe bị hỏng nên mỗi xe phải chở thêm $4$ tấn so với dự định. Hỏi ban đầu có bao nhiêu xe?",
    "$18$ xe", 18.0, None, ["Lập phương trình", "Phương trình bậc hai"],
    [S("Gọi số xe ban đầu là $x$ ($x\\in\\mathbb Z^+$, $x>3$).", "Ẩn + ĐK", "Đặt ẩn", 0.25),
     S("Số xe thực tế $x-3$.", "$x-3$", "Biểu thị", 0.25),
     S("Dự định mỗi xe chở $\\dfrac{360}x$ tấn.", "$\\dfrac{360}x$", "Biểu thị", 0.25),
     S("Thực tế mỗi xe chở $\\dfrac{360}{x-3}$ tấn.", "$\\dfrac{360}{x-3}$", "Biểu thị", 0.25),
     S("Phương trình $\\dfrac{360}{x-3}-\\dfrac{360}x=4$.", "PT", "Lập phương trình", 0.25),
     S("$\\Leftrightarrow4x^2-12x-1080=0$, $\\Delta'=36+4320=4356$, $\\sqrt{\\Delta'}=66$.", "$\\sqrt{\\Delta'}=66$", "Biến đổi", 0.25, so=66.0),
     S("$x_1=\\dfrac{6+66}4=18$ (TM), $x_2=-15$ (loại).", "$x=18$", "Công thức nghiệm, đối chiếu ĐK", 0.25, so=18.0),
     S("Vậy ban đầu có $18$ xe.", "$18$ xe", "Kết luận", 0.25)],
    [W("B5", "sai-hinh-dung", "Đảo hiệu: $\\dfrac{360}x-\\dfrac{360}{x-3}=4$", {"B5": "Phương trình $\\dfrac{360}x-\\dfrac{360}{x-3}=4$.", "B6": "$\\Leftrightarrow4x^2-12x+1080=0$, $\\Delta'<0$.", "B7": "Phương trình vô nghiệm.", "B8": "Không có số xe thỏa mãn."}),
     W("B7", "sai-tinh-toan", "Chia cho $2$ thay vì $a=4$: $x=\\dfrac{6+66}2=36$", {"B7": "$x_1=\\dfrac{6+66}2=36$ (TM), $x_2=-30$ (loại).", "B8": "Vậy ban đầu có $36$ xe."})],
    bieu_diem=OFF, ghi_chu="Thang HDC 2đ = 8 × 0,25."))

add(build("BM-070", qo("Câu 2.2", 3), "thcs", "Thể tích hình nón (thực tế)", "TH",
    "Một đồng hồ cát gồm hai hình nón bằng nhau; theo hình vẽ trong đề, đáy hình nón có đường kính $12$ cm và chiều cao mỗi hình nón $12$ cm. Đổ đầy cát vào một nửa rồi úp ngược cho cát chảy, mỗi phút chảy $15$ cm$^3$. Hỏi sau bao lâu cát chảy hết? (Lấy $\\pi\\approx3{,}14$, làm tròn đến phút.)",
    "$\\approx30$ phút", 30.0, None, ["$V_{nón}=\\dfrac13\\pi r^2h$"],
    [S("$r=6$ cm, $V=\\dfrac13\\cdot3{,}14\\cdot6^2\\cdot12\\approx452{,}16$ cm$^3$.", "$452{,}16$", "Thể tích nón", 0.25, so=452.16),
     S("Thời gian $452{,}16:15\\approx30$ phút.", "$\\approx30$", "Chia", 0.25, so=30.0)],
    [W("B1", "sai-dinh-li", "Dùng công thức trụ $V=\\pi r^2h$", {"B1": "$V=3{,}14\\cdot36\\cdot12=1356{,}48$ cm$^3$.", "B2": "Thời gian $\\approx90$ phút."}),
     W("B1", "sai-hinh-dung", "Lấy đường kính $12$ làm bán kính", {"B1": "$r=12$, $V=\\dfrac13\\cdot3{,}14\\cdot144\\cdot12=1808{,}64$ cm$^3$.", "B2": "Thời gian $\\approx121$ phút."})],
    bieu_diem=OFF, ghi_chu="Kích thước lấy theo hình vẽ của đề và đúng như HDC dùng ($r=6$, $h=12$). Đáp số làm tròn $30{,}144\\to30$."))

add(build("BM-071", qo("Câu 3.2b", 4), "thcs", "Hệ thức nghiệm phương trình bậc hai", "VD",
    "Cho phương trình $x^2-2(m-1)x+2m-3=0$. Tìm $m$ để phương trình có hai nghiệm phân biệt $x_1,x_2$ thỏa mãn $x_1=3x_2$.",
    "$m\\in\\left\\{3;\\dfrac53\\right\\}$", None, None, ["Điều kiện hai nghiệm phân biệt", "Nhẩm nghiệm $a+b+c=0$"],
    [S("$\\Delta'=(m-2)^2>0\\Leftrightarrow m\\ne2$.", "$m\\ne2$", "Điều kiện nghiệm phân biệt", 0.25),
     S("$a+b+c=0$ nên hai nghiệm $1$ và $2m-3$. TH1: $x_1=1,x_2=2m-3$: $1=3(2m-3)\\Rightarrow m=\\dfrac53$ (TM).", "$m=\\frac53$", "Nhẩm nghiệm", 0.25),
     S("TH2: $x_1=2m-3,x_2=1$: $2m-3=3\\Rightarrow m=3$ (TM). Kết luận $m\\in\\{3;\\frac53\\}$.", "$m\\in\\{3;\\frac53\\}$", "Xét đủ trường hợp", 0.25)],
    [W("B3", "nham-dieu-kien", "Chỉ xét một cách gán nghiệm (bỏ TH2)", {"B3": "Vậy $m=\\dfrac53$."}),
     W("B2", "sai-tinh-toan", "Giải sai $1=3(2m-3)\\Rightarrow m=\\dfrac43$", {"B2": "TH1: $1=3(2m-3)\\Rightarrow 6m=8\\Rightarrow m=\\dfrac43$.", "B3": "TH2: $m=3$. Kết luận $m\\in\\{3;\\frac43\\}$."})],
    bieu_diem=OFF, ghi_chu="Thang HDC 0,75 = 3 × 0,25. S2: TH2 đúng nhưng nằm chung ý với kết luận sai nên B3 không được điểm (0,25)."))
