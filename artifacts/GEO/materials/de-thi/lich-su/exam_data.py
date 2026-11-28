# Phiên bản: v1.0 · 27/11/2026 · dữ liệu gắn nhãn từng câu (đầu vào của build_data.py)
# Nguồn nội dung câu: text trích từ bộ đề tổng hợp (toanmath.com, PDF 424 trang) + PDF chính thức ĐH 2014 (hocmai) + đáp án 2013A (HNUE).
# Nhãn chu_de/dang/muc_do do người gắn (agent) đọc đề rồi gán; muc_do và nang_luc là SUY LUẬN (đề không công bố).

# ---------- Đề TN THPT cấu trúc cũ (50 câu x 0,2 đ), mã đề 101 ----------
OLD = {
 2017: ("101", """ml.pt-mu nh.co-ban sp.co-ban hs.bbt-cuc-tri dt.nhan-dang ml.bien-doi sp.tinh-toan hs.don-dieu-ham ox.mp-co-ban ox.mp-vtpt tx.tru dt.tiem-can hs.don-dieu-ham ud.the-tich-xoay ml.bien-doi ml.tap-xac-dinh ml.bpt-log th.khai-niem ox.mp-pt ox.dt-pt
 th.chop sp.pt-bac-hai hs.gtln-doan ml.tap-xac-dinh nh.tp-tinh-chat tx.cau nh.co-ban dt.dau-he-so ox.mc-pt sp.bieu-dien tx.non nh.tp-phan-tu hs.gtln-m ox.dt-tong-hop ml.lai-suat
 sp.tim-z ox.vitri hs.don-dieu-m ml.pt-m hs.cuc-tri-duong-thang
 ud.van-toc-quang-duong ml.bien-doi th.chop-goc th.ty-so ox.mc-cuc-tri
 sp.dem-so-phuc ml.vdc dt.tuong-giao-m hs.f'-do-thi tx.thiet-dien"""),
 2018: ("101", """tk.to-hop-co-ban ox.mp-vtpt hs.do-thi-cuc-tri hs.don-dieu-bbt ud.dien-tich ml.bien-doi nh.co-ban ox.dt-vtcp sp.co-ban tx.cau dt.nhan-dang ox.toa-do-diem gh.day-so ml.pt-mu th.chop ml.lai-suat dt.so-nghiem-bbt dt.tiem-can qh.goc ox.mp-song-song
 tk.xs-co-dien nh.tp-co-ban hs.gtln-doan sp.tim-z qh.goc nh.tp-huu-ti th.lang-tru-thuc-te tk.nhi-thuc qh.khoang-cach sp.tap-hop-diem hs.toi-uu ud.van-toc-quang-duong ox.dt-tong-hop ml.pt-m hs.don-dieu-m
 hs.cuc-tri-m qh.goc-mp sp.dem-so-phuc ox.mc-vdc dt.tiep-tuyen-vdc ud.dien-tich-vdc th.lang-tru-vdc tk.xs-co-dien ml.vdc dt.tiem-can-vdc
 ml.pt-m ox.mc-vdc nh.f-ham-an ox.dt-vdc hs.f'-do-thi"""),
 2019: ("101", """ox.mp-vtpt ml.bien-doi hs.bbt-cuc-tri ml.pt-mu ds.cap-so-cong dt.nhan-dang ox.dt-vtcp tx.non tk.to-hop-co-ban ox.toa-do-diem nh.tp-tinh-chat th.lang-tru sp.co-ban hs.don-dieu-bbt nh.co-ban hs.bbt-cuc-tri qh.goc sp.pt-bac-hai ml.dao-ham hs.gtln-doan
 ox.mc-co-ban th.lang-tru hs.cuc-tri-f' ml.bien-doi sp.bieu-dien ml.pt-log tx.thuc-te dt.tiem-can ud.dien-tich ox.mp-pt nh.co-ban nh.f-ham-an ox.dt-pt sp.tim-z hs.don-dieu-bbt
 hs.f'-do-thi tk.xs-co-dien tx.thiet-dien ml.pt-m qh.goc nh.f-ham-an ox.dt-cuc-tri dt.so-nghiem-bbt sp.tap-hop-diem ud.dien-tich-vdc
 hs.f'-bbt th.lang-tru-vdc ox.mc-vdc dt.tuong-giao-m ml.pt-m"""),
 2020: ("101 (đợt 1)", """dt.nhan-dang ml.pt-mu hs.bbt-cuc-tri hs.don-dieu-bbt th.hop sp.co-ban tx.tru tx.cau ml.bien-doi ox.mc-co-ban dt.tiem-can tx.non ml.pt-log nh.co-ban tk.to-hop-co-ban dt.so-nghiem-bbt ox.toa-do-diem th.chop ox.dt-vtcp ox.mp-pt
 ds.cap-so-nhan sp.tinh-toan nh.tp-tinh-chat sp.bieu-dien ml.tap-xac-dinh dt.tuong-giao qh.goc nh.tp-tinh-chat ud.dien-tich ox.mp-pt sp.pt-bac-hai ox.dt-pt hs.cuc-tri-f' ml.bpt-mu tx.non
 hs.gtln-doan sp.tinh-toan ml.bien-doi nh.tp-phan-tu hs.don-dieu-m ml.lai-suat qh.khoang-cach qh.khoang-cach hs.ham-hop dt.dau-he-so
 tk.xs-co-dien th.chop-vdc ml.vdc ml.pt-bpt-nguyen dt.so-nghiem-bbt"""),
 2021: ("101 (đợt 1; tài liệu tổng hợp ghi nhãn 'Năm 2022 - Lần 1')", """ml.bpt-mu nh.tp-tinh-chat ox.mc-pt ox.dt-pt hs.cuc-tri-f' dt.nhan-dang dt.co-ban tk.to-hop-co-ban sp.co-ban ml.dao-ham nh.co-ban ox.vecto hs.don-dieu-bbt hs.don-dieu-do-thi ml.pt-log nh.tp-tinh-chat th.hop ml.tap-xac-dinh tx.cau dt.tiem-can
 ml.bien-doi th.chop ox.mp-vtpt tx.tru sp.tinh-toan ds.cap-so-nhan nh.co-ban sp.bieu-dien dt.dau-he-so tk.xs-co-dien hs.gtln-doan ox.mp-pt qh.goc ox.mp-pt sp.tinh-toan
 qh.khoang-cach ml.bien-doi nh.tp-tinh-chat nh.f-ham-an ml.pt-bpt-nguyen dt.so-nghiem-bbt tx.thiet-dien sp.pt-bac-hai-m sp.cuc-tri-mo-dun ox.dt-tong-hop
 hs.f-vdc ml.vdc th.hop-vdc ox.mc-cuc-tri hs.ham-hop"""),
 2022: ("101", """nh.tp-tinh-chat th.lang-tru nh.tp-tinh-chat nh.co-ban hs.bbt-cuc-tri ox.mc-co-ban ox.toa-do-diem th.chop ds.cap-so-nhan tx.tru dt.tiem-can ml.bpt-log dt.nhan-dang sp.co-ban dt.so-nghiem-bbt ml.tap-xac-dinh ml.bien-doi tk.to-hop-co-ban hs.don-dieu-bbt ox.mp-co-ban
 ml.pt-mu hs.do-thi-cuc-tri ox.dt-vtcp tx.non sp.bieu-dien sp.tinh-toan nh.co-ban ml.dao-ham ox.dt-pt hs.gtln-doan ml.tap-xac-dinh sp.pt-bac-hai th.lang-tru-goc qh.goc nh.co-ban
 hs.don-dieu-ham ox.mp-pt tk.xs-co-dien ml.pt-bpt-nguyen hs.gtln-m nh.f-ham-an ox.mp-vdc tx.noi-tiep-cau ml.vdc sp.cuc-tri-mo-dun
 th.lang-tru-vdc hs.f'-bbt sp.dem-so-phuc ox.mc-cuc-tri hs.cuc-tri-m"""),
 2023: ("101", """ml.bpt-mu nh.co-ban tk.to-hop-co-ban nh.co-ban ml.dao-ham ml.bpt-log dt.so-nghiem-bbt dt.tiem-can th.ty-so nh.tp-tinh-chat sp.bieu-dien hs.don-dieu-bbt tx.tru tx.non sp.tinh-toan th.chop ml.khai-niem ds.so-hang ox.mc-co-ban ox.vecto
 sp.co-ban nh.tp-tinh-chat ml.bpt-log dt.nhan-dang ox.mp-co-ban dt.dau-he-so ox.dt-pt dt.so-nghiem-bbt ml.bien-doi ox.mp-pt ox.mp-khoang-cach dt.tuong-giao hs.don-dieu-ham qh.goc tk.xs-co-dien
 sp.pt-bac-hai nh.tp-do-thi qh.goc ml.pt-bpt-nguyen ud.dien-tich hs.don-dieu-m nh.f-ham-an sp.tap-hop-diem th.chop-vdc ox.mc-vdc
 sp.pt-bac-hai-m ml.pt-bpt-nguyen tx.noi-tiep-cau ox.mc-cuc-tri hs.cuc-tri-m"""),
 2024: ("101", """sp.co-ban nh.co-ban ox.dt-vtcp tx.tru ds.cap-so-cong ml.bien-doi hs.do-thi-cuc-tri th.lang-tru nh.tp-tinh-chat dt.tiem-can sp.tinh-toan nh.co-ban nh.tp-tinh-chat hs.don-dieu-bbt ml.bpt-log dt.nhan-dang ox.mc-pt ml.pt-mu hs.don-dieu-ham ml.khai-niem
 ml.dao-ham tx.non hs.bbt-cuc-tri ox.vecto ox.mp-pt th.chop tk.to-hop-co-ban sp.bieu-dien qh.goc ox.mp-khoang-cach sp.tinh-toan tk.dem ud.van-toc-quang-duong ox.toa-do-diem qh.khoang-cach
 hs.gtln-doan hs.f'-do-thi ml.bien-doi nh.tp-phan-tu ml.pt-bpt-nguyen hs.f'-bbt ml.pt-m ud.dien-tich-vdc th.lang-tru-vdc ox.dt-vdc
 ml.pt-bpt-nguyen sp.pt-bac-hai-m th.chop-vdc ox.mc-cuc-tri hs.f'-bbt"""),
}

# ---------- Đề TN cấu trúc mới (22 câu): (dang, muc, tom_tat) theo thứ tự câu 1..22 ----------
NEW = {
 "2025": ("0101", [
  ("qh.vecto","NB","Phát biểu đúng về tổng các vectơ trong hình lăng trụ"),
  ("qh.song-song","NB","Đường thẳng AB song song với mặt phẳng nào trong hình hộp"),
  ("tke.ghep-nhom","NB","Tứ phân vị thứ ba của mẫu số liệu ghép nhóm (thời lượng cuộc gọi)"),
  ("ox.dt-vtcp","NB","Vectơ chỉ phương của đường thẳng cho bằng phương trình chính tắc"),
  ("nh.co-ban","NB","Họ nguyên hàm của f(x)=x^2"),
  ("ds.cap-so-cong","NB","Số hạng u5 của cấp số cộng u1=4, d=-3"),
  ("lg.pt-co-ban","NB","Tập nghiệm của phương trình sin x = 0"),
  ("ud.dien-tich","NB","Diện tích hình phẳng giới hạn bởi y=2x-3, trục hoành, x=1, x=2"),
  ("ox.mp-pt","NB","Phương trình mặt phẳng qua điểm A với vectơ pháp tuyến cho trước"),
  ("ml.pt-log","NB","Nghiệm của phương trình log3(2x-1)=2"),
  ("dt.tiem-can","NB","Tiệm cận đứng của hàm phân thức từ đồ thị"),
  ("th.chop","TH","Thể tích khối chóp có SA vuông góc đáy, đáy tam giác vuông"),
  ("hs.dao-ham-dung-sai","TH","Hàm f(x)=x^3-12x-8: đạo hàm, nghiệm f'=0, giá trị, GTLN trên [-3;3] (4 ý đúng/sai)"),
  ("ml.mo-hinh","VD","Nồng độ thuốc tồn dư y'(t)=k.y(t): mô hình mũ, tìm k và dự báo (4 ý đúng/sai)"),
  ("ud.van-toc-quang-duong+ox.vecto","VD","Vật chuyển động trong Oxyz, v(t)=bt+300: quãng đường, vectơ vận tốc (4 ý đúng/sai)"),
  ("xs.dieu-kien","VD","Phần mềm đánh dấu tin nhắn quảng cáo: xác suất có điều kiện, toàn phần (4 ý đúng/sai)"),
  ("tk.xs-co-dien","VD","Xếp 6 số vào 6 vị trí thành các cấp số cộng: xác suất"),
  ("hs.mo-hinh","VD","Doanh thu F(x), chi phí G(x): tìm số sản phẩm để lợi nhuận lớn nhất"),
  ("pt.he-bpt-2-an","VD","Thực đơn nước chanh - khoai chiên: quy hoạch tuyến tính tìm doanh thu lớn nhất"),
  ("qh.khoang-cach","VDC","Khoảng cách giữa AC và SD của chóp đáy hình thoi"),
  ("tk.dem","VDC","Xếp 7 quyển sách vào 4 ngăn, mỗi ngăn có ít nhất 1 quyển: đếm/xác suất"),
  ("ud.the-tich-xoay","VDC","Thể tích chân đế: chóp cụt trừ phần khoét dạng chỏm cầu"),
 ]),
 "MH2025": ("MH 2025 (công bố 29/12/2023)", [
  ("nh.co-ban","NB","Nguyên hàm của f(x)=e^x"),
  ("ud.dien-tich","NB","Hình phẳng giới hạn bởi y=f(x)>0, trục hoành, x=a, x=b (công thức tích phân)"),
  ("tke.ghep-nhom","NB","So sánh hai mẫu số liệu ghép nhóm M1, M2 bằng số đặc trưng"),
  ("ox.dt-pt","NB","Phương trình đường thẳng qua M với vectơ chỉ phương u"),
  ("dt.dau-he-so","NB","Mệnh đề đúng về hàm phân thức từ đồ thị"),
  ("ml.bpt-log","NB","Tập nghiệm của BPT log2(x-1)<3"),
  ("ox.mp-vtpt","NB","Vectơ pháp tuyến của mặt phẳng (P)"),
  ("qh.vuong-goc","NB","Mặt phẳng nào vuông góc với (ABCD) trong chóp S.ABCD"),
  ("ml.pt-mu","NB","Nghiệm của phương trình 2^x=6"),
  ("ds.cap-so-cong","TH","Số hạng u5 của cấp số cộng u1=1, u2=3"),
  ("qh.vecto","TH","Phát biểu đúng về vectơ trong hình hộp"),
  ("hs.don-dieu-do-thi","TH","Hàm số đồng biến trên khoảng nào (từ đồ thị)"),
  ("hs.dao-ham-dung-sai","TH","Hàm f(x)=2cos x+x: giá trị, đạo hàm, nghiệm f'=0, GTLN (4 ý đúng/sai)"),
  ("ud.van-toc-quang-duong","VD","Ô tô nhập làn cao tốc, v(t)=at+b: tìm a, b, quãng đường (4 ý đúng/sai)"),
  ("xs.dieu-kien","VD","Khảo sát khách hàng 'sẽ mua/không mua': xác suất có điều kiện (4 ý đúng/sai)"),
  ("ox.mc-vdc","VD","Thiên thạch và hệ thống quan sát trong Oxyz: khoảng cách, mặt cầu (4 ý đúng/sai)"),
  ("qh.khoang-cach","VD","Khoảng cách giữa AA' và BC của lăng trụ đứng"),
  ("tk.dem","VD","Trò chơi 4 trụ: tổng số thử thách nhỏ nhất của đường đi qua mọi trụ"),
  ("ox.mc-vdc","VD","GPS: xác định vị trí M từ 4 vệ tinh, khoảng cách tới O"),
  ("ud.dien-tich","VDC","Diện tích sân chơi giới hạn bởi các parabol"),
  ("hs.mo-hinh","VDC","Doanh thu F(x), chi phí bình quân G(x): tối ưu lợi nhuận"),
  ("xs.toan-phan","VDC","Hai hộp bóng: chuyển 1 quả từ hộp I sang II rồi lấy ra, xác suất"),
 ]),
 "2026": ("0101 (đợt 1)", [
  ("ds.cap-so-cong","NB","Số hạng u2 của cấp số cộng u1=5, d=-1"),
  ("dh1.quy-tac","NB","Đạo hàm của f(x)+g(x) khi biết f', g'"),
  ("nh.co-ban","NB","Phát biểu đúng về nguyên hàm [3+f(x)]dx"),
  ("ml.pt-log","NB","Nghiệm của phương trình log3(x-1)=1"),
  ("ox.vecto","NB","Tọa độ vectơ AB"),
  ("xs.doc-lap","NB","Xác suất giao của hai biến cố độc lập"),
  ("qh.vecto","NB","Vectơ bằng vectơ AB trong hình lập phương"),
  ("pt.he-bpt-2-an","NB","Cặp số là nghiệm của hệ BPT bậc nhất hai ẩn"),
  ("nh.co-ban","NB","F(x)=4x^3 là nguyên hàm của hàm nào"),
  ("dt.tiem-can","TH","Tiệm cận ngang từ BBT"),
  ("ds.cap-so-nhan","TH","Số hạng u4 của cấp số nhân"),
  ("tke.ghep-nhom","TH","Số đặc trưng của mẫu ghép nhóm (thời gian học trực tuyến)"),
  ("xs.dieu-kien","TH","Ứng dụng AI sàng lọc bệnh: xác suất có điều kiện (4 ý đúng/sai)"),
  ("hs.dao-ham-dung-sai","VD","Hàm f(x)=x^3/3-5x^2+9x+8: đạo hàm, cực trị, giá trị (4 ý đúng/sai)"),
  ("ud.van-toc-quang-duong","VD","Pin năng lượng mặt trời: F(t) từ tốc độ lưu trữ f(t) (4 ý đúng/sai)"),
  ("ox.mc-vdc","VD","Vành đai bảo vệ và máy bay không người lái trong Oxyz (4 ý đúng/sai)"),
  ("qh.khoang-cach","VD","Khoảng cách từ P đến mặt phẳng (MED) trong hình lập phương"),
  ("tk.dem","VD","Điền 15 số vào 15 ô của trò chơi thỏa điều kiện"),
  ("ud.the-tich-xoay","VDC","Thể tích hạt cườm: quay nửa elip quanh Ox"),
  ("tk.dem","VDC","Gắn 12 bóng đèn (4 đỏ, 8 xanh) lên đa giác đều 12 cạnh"),
  ("pt.he-bac-nhat","VDC","Bảng bán hàng 4 ngày: lập hệ phương trình tìm doanh thu ngày thứ Bảy"),
  ("hs.mo-hinh","VDC","Chi phí sản xuất và doanh thu chế biến nông sản: tối ưu lợi nhuận"),
 ]),
}
# 2026: danh sách trên có 21 phần tử vì câu 12 và 13 gộp đủ 22? -> xem assert trong build_data.py (đã chèn đủ 22 ở NEW_2026_FIX)

# ---------- Đề ĐH tự luận 2009-2014: (nhãn câu, điểm, dang, tóm tắt, mức độ) ----------
def _ks(t): return ("1a",1,"dt.khao-sat",t,"NB")
DH = {
 ("dh-a",2009): [
  ("1a",1,"dt.khao-sat","Khảo sát hàm y=(x+2)/(2x+3)","NB"),
  ("1b",1,"dt.tiep-tuyen","Tiếp tuyến cắt hai trục tạo tam giác OAB cân tại O","VD"),
  ("2a",1,"lg.pt","PT lượng giác dạng phân thức theo sin x, cos x","TH"),
  ("2b",1,"pt.vo-ti","PT chứa căn bậc ba và căn bậc hai","VD"),
  ("3",1,"nh.tp-luong-giac","Tích phân (cos^3 x - 1)cos^2 x","TH"),
  ("4",1,"th.chop-goc+qh.goc","Thể tích chóp đáy hình thang vuông, biết góc hai mặt phẳng và hai mặt phẳng cùng vuông góc đáy","VD"),
  ("5",1,"bdt.cm","Chứng minh BĐT ba biến dương với x(x+y+z)=3yz","VDC"),
  ("6a",1,"oxy.hinh-chu-nhat","Viết phương trình cạnh AB của hình chữ nhật biết tâm và điều kiện trung điểm CD","VD"),
  ("6b",1,"ox.mc-vitri","Chứng minh mặt phẳng cắt mặt cầu theo đường tròn, tìm tâm và bán kính","TH"),
  ("7",1,"sp.pt-bac-hai","Hai nghiệm phức của z^2+2z+10=0, tính |z1|^2+|z2|^2","NB")],
 ("dh-b",2009): [
  ("1a",1,"dt.khao-sat","Khảo sát hàm y=2x^4-4x^2","NB"),
  ("1b",1,"dt.tuong-giao-m","Tìm m để x^2|x^2-2|=m có đúng 6 nghiệm","VD"),
  ("2a",1,"lg.pt","PT lượng giác phối hợp sin, cos, bậc ba và cung gấp","VD"),
  ("2b",1,"pt.he","Hệ phương trình hai ẩn đối xứng dạng xy+x+1=7y","VD"),
  ("3",1,"nh.tp-phan-tu","Tích phân (3+ln x)/(x+1)^2","TH"),
  ("4",1,"th.lang-tru-goc","Thể tích tứ diện A'ABC của lăng trụ, B' chiếu lên trọng tâm","VD"),
  ("5",1,"bdt.cuc-tri","GTNN của A theo x,y với (x+y)^3+4xy>=2","VDC"),
  ("6a",1,"oxy.duong-tron","Đường tròn tiếp xúc hai đường thẳng với tâm thuộc đường tròn cho trước","VD"),
  ("6b",1,"ox.mp-pt","Mặt phẳng qua A, B cách đều C và D","TH"),
  ("7",1,"sp.tim-z","Tìm z thỏa |z-(2+i)|=căn10 và z.z̄=25","TH")],
 ("dh-a",2010): [
  ("1a",1,"dt.khao-sat","Khảo sát hàm bậc ba y=x^3-2x^2+(1-m)x+m khi m=1","NB"),
  ("1b",1,"dt.tuong-giao-m","Đồ thị cắt Ox tại 3 điểm phân biệt thỏa x1^2+x2^2+x3^2<4","VD"),
  ("2a",1,"lg.pt","PT lượng giác có tan x và sin(x+pi/4)","VD"),
  ("2b",1,"pt.bpt-vo-ti","BPT vô tỉ (x-căn x)/(1-căn(2(x^2-x+1)))>=1","VD"),
  ("3",1,"nh.tp-mu","Tích phân hàm chứa e^x","TH"),
  ("4",1,"th.chop+qh.khoang-cach","Thể tích S.CDNM và khoảng cách DM với SC (đáy hình vuông)","VD"),
  ("5",1,"pt.he","Hệ phương trình chứa căn (4x^2+1)x+(y-3)căn(5-2y)=0","VDC"),
  ("6a",1,"oxy.duong-tron","Đường tròn tiếp xúc d1, cắt d2 tạo tam giác vuông, diện tích cho trước","VD"),
  ("6b",1,"ox.mp-khoang-cach","Khoảng cách từ M thuộc đường thẳng đến mặt phẳng biết MC","TH"),
  ("7",1,"sp.co-ban","Phần ảo của z=(căn2+i)^2(1-căn2 i)","NB")],
 ("dh-b",2010): [
  ("1a",1,"dt.khao-sat","Khảo sát hàm y=(2x+1)/(x+1)","NB"),
  ("1b",1,"dt.tuong-giao-m","y=-2x+m cắt (C) tại A,B, diện tích OAB cho trước","VD"),
  ("2a",1,"lg.pt","PT (sin2x+cos2x)cos x+2cos2x-sin x=0","TH"),
  ("2b",1,"pt.vo-ti","PT vô tỉ căn(3x+1)-căn(6-x)+3x^2-14x-8=0","VD"),
  ("3",1,"nh.tp-log","Tích phân ln x/(x(2+ln x)^2)","TH"),
  ("4",1,"th.lang-tru-goc+tx.noi-tiep-cau","Thể tích lăng trụ tam giác đều và bán kính mặt cầu ngoại tiếp tứ diện GABC","VD"),
  ("5",1,"bdt.cuc-tri","GTNN của M với a+b+c=1, a,b,c>=0","VDC"),
  ("6a",1,"oxy.tam-giac","Tam giác vuông tại A biết C, phân giác trong, diện tích: viết PT cạnh BC","VD"),
  ("6b",1,"ox.mp-pt","Tìm b,c biết (ABC)⊥(P) và d(O,(ABC))=1/3","VD"),
  ("7",1,"sp.tap-hop-diem","Tập hợp điểm biểu diễn z thỏa |z-i|=|(1+i)z|","TH")],
 ("dh-a",2011): [
  ("1a",1,"dt.khao-sat","Khảo sát hàm y=(-x+1)/(2x-1)","NB"),
  ("1b",1,"dt.tuong-giao-m+dt.tiep-tuyen","y=x+m cắt (C) tại A,B; tìm m để k1+k2 lớn nhất","VD"),
  ("2a",1,"lg.pt","PT lượng giác có 1+cot^2 x ở mẫu","TH"),
  ("2b",1,"pt.he","Hệ phương trình bậc cao 5x^2y-4xy^2+3y^3-2(x+y)=0","VD"),
  ("3",1,"nh.tp-luong-giac","Tích phân (x sinx+(x+1)cosx)/(x sinx+cosx)","VD"),
  ("4",1,"th.chop+qh.khoang-cach","Chóp S.ABC hai mặt bên vuông góc đáy: thể tích S.BCNM, khoảng cách AB-SN","VD"),
  ("5",1,"bdt.cuc-tri","GTNN của P=x/(2x+3y)+y/(y+z)+z/(z+x) với x,y,z thuộc [1;4]","VDC"),
  ("6a",1,"oxy.duong-tron","Điểm M thuộc đường thẳng, tiếp tuyến MA, MB tới đường tròn, diện tích MAIB","VD"),
  ("6b",1,"ox.vitri","Tìm M thuộc (P) sao cho MA=MB=3","TH"),
  ("7",1,"sp.tim-z","Tìm z thỏa z^2=|z|^2+z̄","TH")],
 ("dh-b",2011): [
  ("1a",1,"dt.khao-sat","Khảo sát hàm trùng phương y=x^4-2(m+1)x^2+m khi m=1","NB"),
  ("1b",1,"hs.cuc-tri-hinh-hoc","Ba cực trị A,B,C thỏa OA=BC","VD"),
  ("2a",1,"lg.pt","PT sin2x cosx+sinx cosx=cos2x+sinx+cosx","TH"),
  ("2b",1,"pt.vo-ti","PT vô tỉ 3căn(2+x)-6căn(2-x)+4căn(4-x^2)=10-3x","VD"),
  ("3",1,"nh.tp-luong-giac","Tích phân (1+x sin x)/cos^2 x","TH"),
  ("4",1,"th.lang-tru-goc+qh.khoang-cach","Lăng trụ đáy hình chữ nhật: thể tích và khoảng cách B1 đến (A1BD)","VD"),
  ("5",1,"bdt.cuc-tri","GTNN của P=4(a^3/b^3+b^3/a^3)-9(a^2/b^2+b^2/a^2) với ràng buộc a,b","VDC"),
  ("6a",1,"oxy.duong-thang","Tìm N thuộc d sao cho ON cắt Δ tại M thỏa OM.ON=8","VD"),
  ("6b",1,"ox.vitri","Tìm M thuộc (P) sao cho MI⊥Δ và MI=4căn14","TH"),
  ("7",1,"sp.tim-z","Tìm z biết z-(5+i căn3)/z-1=0","TH")],
 ("dh-a",2012): [
  ("1a",1,"dt.khao-sat","Khảo sát hàm trùng phương y=x^4-2(m+1)x^2+m^2 khi m=0","NB"),
  ("1b",1,"hs.cuc-tri-hinh-hoc","Ba cực trị tạo tam giác vuông","VD"),
  ("2",1,"lg.pt","PT căn3 sin2x+cos2x=2cosx-1","TH"),
  ("3",1,"pt.he","Hệ phương trình x^3-3x^2-9x+22=y^3+3y^2-9y, x^2+y^2-x+y=1/2","VD"),
  ("4",1,"nh.tp-log","Tích phân (1+ln(x+1))/x^2","TH"),
  ("5",1,"th.chop-goc+qh.khoang-cach","Thể tích S.ABC và khoảng cách SA-BC (H trên AB, HA=2HB)","VD"),
  ("6",1,"bdt.cuc-tri","GTNN của P=3^|x-y|+3^|y-z|+3^|z-x|-căn(6x^2+6y^2+6z^2) với x+y+z=0","VDC"),
  ("7",1,"oxy.hinh-vuong","Hình vuông ABCD, M trung điểm BC, CN=2ND: tìm A","VD"),
  ("8",1,"ox.mc-pt","Mặt cầu tâm I cắt đường thẳng tại A,B sao cho IAB vuông tại I","TH"),
  ("9",1,"tk.nhi-thuc","Tìm số hạng chứa x^5 trong khai triển nhị thức Newton với n từ 5C(n,n-1)=C(n,3)","TH")],
 ("dh-b",2012): [
  ("1a",1,"dt.khao-sat","Khảo sát hàm y=x^3-3mx^2+3m^3 khi m=1","NB"),
  ("1b",1,"hs.cuc-tri-hinh-hoc","Hai cực trị A,B với diện tích tam giác OAB=48","VD"),
  ("2",1,"lg.pt","PT 2(cosx+căn3 sinx)cosx=cosx-căn3 sinx+1","TH"),
  ("3",1,"pt.bpt-vo-ti","BPT x+1+căn(x^2-4x+1)>=3căn x","VD"),
  ("4",1,"nh.tp-huu-ti","Tích phân x^3/(x^4+3x^2+2)","TH"),
  ("5",1,"th.chop+qh.vuong-goc","Chóp tam giác đều: chứng minh SC⊥(ABH), thể tích S.ABH","VD"),
  ("6",1,"bdt.cuc-tri","GTLN của P=x^5+y^5+z^5 với x+y+z=0, x^2+y^2+z^2=1","VDC"),
  ("7",1,"oxy.duong-tron","Đường tròn tâm thuộc (C2), tiếp xúc d, cắt (C1) tại A,B với AB⊥d","VD"),
  ("8",1,"ox.mc-pt","Mặt cầu qua A,B tâm thuộc đường thẳng d","TH"),
  ("9",1,"tk.xs-co-dien","Xác suất 4 học sinh gọi lên bảng có cả nam lẫn nữ (15 nam, 10 nữ)","TH")],
 ("dh-a",2013): [
  ("1a",1,"dt.khao-sat","Khảo sát hàm y=-x^3+3x^2+3mx-1 khi m=0","NB"),
  ("1b",1,"hs.don-dieu-m","Hàm số nghịch biến trên (0;+∞)","TH"),
  ("2",1,"lg.pt","PT 1+tan x=2căn2 sin(x+pi/4)","TH"),
  ("3",1,"pt.he","Hệ phương trình chứa căn bậc hai và căn bậc bốn","VD"),
  ("4",1,"nh.tp-phan-tu","Tích phân (x^2-1)/x^2 . ln x (từng phần)","TH"),
  ("5",1,"th.chop+qh.khoang-cach","Thể tích S.ABC và khoảng cách C đến (SAB), tam giác SBC đều","VD"),
  ("6",1,"bdt.cuc-tri","GTNN của P với (a+b)(b+c)=4c^2","VDC"),
  ("7",1,"oxy.hinh-chu-nhat","Hình chữ nhật ABCD, C thuộc d, tìm B và C","VD"),
  ("8",1,"ox.mp-pt+ox.vitri","Mặt phẳng qua A vuông góc Δ; tìm M thuộc Δ với AM cho trước","TH"),
  ("9",1,"tk.xs-co-dien","Số tự nhiên 3 chữ số phân biệt từ 1..7: số phần tử và xác suất số chẵn","TH")],
 ("dh-b",2013): [
  ("1a",1,"dt.khao-sat","Khảo sát hàm y=2x^3-3(m+1)x^2+6mx khi m=-1","NB"),
  ("1b",1,"hs.cuc-tri-hinh-hoc","Hai cực trị A,B sao cho AB vuông góc y=x+2","VD"),
  ("2",1,"lg.pt","PT sin5x+2cos^2 x=1","TH"),
  ("3",1,"pt.he","Hệ phương trình hai ẩn kết hợp căn thức","VD"),
  ("4",1,"nh.tp-vo-ti","Tích phân x.căn(2-x^2)","TH"),
  ("5",1,"th.chop+qh.khoang-cach","Chóp S.ABCD mặt bên SAB đều vuông góc đáy: thể tích, khoảng cách A đến (SCD)","VD"),
  ("6",1,"bdt.cuc-tri","GTLN của P=4/căn(a^2+b^2+c^2+4)-9/((a+b)căn((a+2c)(b+2c)))","VDC"),
  ("7",1,"oxy.hinh-thang","Hình thang cân hai đường chéo vuông góc, AD=3BC: tìm C và D","VD"),
  ("8",1,"ox.dt-pt","Đường thẳng qua A vuông góc (P); điểm đối xứng của A qua (P)","TH"),
  ("9",1,"tk.xs-co-dien","Hai hộp bi, lấy mỗi hộp 1 viên: xác suất cùng màu","TH")],
 ("dh-a",2014): [
  ("1a",1,"dt.khao-sat","Khảo sát hàm y=(x+2)/(x-1)","NB"),
  ("1b",1,"dt.diem-tren-do-thi","Tìm M thuộc (C) với khoảng cách đến đường thẳng y=-x bằng căn2","VD"),
  ("2",1,"lg.pt","PT sin x+4cos x=2+sin2x","TH"),
  ("3",1,"ud.dien-tich","Diện tích hình phẳng giới hạn bởi y=x^2-x+3 và y=2x+1","TH"),
  ("4a",0.5,"sp.tim-z","Tìm phần thực, phần ảo của z từ z+(2+i)z̄=3+5i","NB"),
  ("4b",0.5,"tk.xs-co-dien","Hộp 16 thẻ, chọn 4 thẻ: xác suất cùng số chẵn","TH"),
  ("5",1,"ox.mp-pt+ox.vitri","Giao điểm d và (P); mặt phẳng chứa d vuông góc (P)","TH"),
  ("6",1,"th.chop+qh.khoang-cach","Chóp S.ABCD đáy vuông: thể tích và khoảng cách A đến (SBD)","VD"),
  ("7",1,"oxy.hinh-vuong","Hình vuông ABCD, M trung điểm AB, AN=3NC: viết PT đường thẳng CD","VD"),
  ("8",1,"pt.he","Hệ phương trình chứa căn x(12-y)... và x^3-8x-1=2căn(y-2)","VDC"),
  ("9",1,"bdt.cuc-tri","GTLN của P với x^2+y^2+z^2=2, x,y,z>=0","VDC")],
 ("dh-b",2014): [
  ("1a",1,"dt.khao-sat","Khảo sát hàm y=x^3-3mx+1 khi m=1","NB"),
  ("1b",1,"hs.cuc-tri-hinh-hoc","Hai cực trị B,C sao cho tam giác ABC cân tại A(2;3)","VD"),
  ("2",1,"lg.pt","PT căn2(sin x-2cos x)=2-sin2x","TH"),
  ("3",1,"nh.tp-huu-ti","Tích phân (x^2+3x+1)/(x^2+x)","TH"),
  ("4a",0.5,"sp.tim-z","Môđun của z thỏa 2z+3(1-i)z̄=1-9i","NB"),
  ("4b",0.5,"tk.xs-co-dien","Chọn 3 hộp sữa từ 5 cam, 4 dâu, 3 nho: xác suất đủ 3 loại","TH"),
  ("5",1,"ox.mp-pt+ox.toa-do-diem","Mặt phẳng qua A vuông góc d; hình chiếu của A trên d","TH"),
  ("6",1,"th.lang-tru-goc+qh.khoang-cach","Lăng trụ đáy đều, A' chiếu lên trung điểm AB: thể tích, khoảng cách B đến (ACC'A')","VD"),
  ("7",1,"oxy.hinh-binh-hanh","Hình bình hành ABCD với M, H, G cho trước: tìm B và D","VD"),
  ("8",1,"pt.he","Hệ phương trình (1-y)căn(x-y)+x=2+(x-y-1)căn y ...","VDC"),
  ("9",1,"bdt.cuc-tri","GTNN của biểu thức với a,b,c không âm, (a+b)c>0","VDC")],
}
