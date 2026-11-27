# Phiên bản: v1.0 · 27/11/2026 · định nghĩa danh mục chủ đề/dạng (đầu vào của build_data.py)
# CHU_DE: id -> (tên, mô tả, lớp theo CT2006 [đề ≤2024 & ĐH], lớp theo CT2018 [đề 2025+], mạch CT2018)
CHU_DE = {
 "ham-so-ung-dung-dao-ham": ("Ứng dụng đạo hàm", "Đơn điệu, cực trị, GTLN/GTNN, tối ưu, hàm hợp/hàm ẩn, mô hình hàm số thực tế", 12, 12, "Giải tích - Hàm số"),
 "ham-so-do-thi": ("Hàm số và đồ thị", "Nhận dạng/khảo sát đồ thị, tiệm cận, tương giao, tiếp tuyến, số nghiệm theo BBT", 12, 12, "Giải tích - Hàm số"),
 "ham-so-mu-logarit": ("Hàm số mũ - logarit", "Lũy thừa, mũ, logarit, PT/BPT mũ-log, lãi suất, mô hình mũ", 12, 11, "Giải tích - Mũ & Logarit"),
 "nguyen-ham-tich-phan": ("Nguyên hàm - tích phân", "Nguyên hàm, tính tích phân (tính chất, đổi biến, từng phần), tích phân hàm ẩn", 12, 12, "Giải tích - Nguyên hàm, tích phân"),
 "ung-dung-tich-phan": ("Ứng dụng tích phân", "Diện tích hình phẳng, thể tích tròn xoay, vận tốc - quãng đường", 12, 12, "Giải tích - Nguyên hàm, tích phân"),
 "so-phuc": ("Số phức", "Khái niệm, phép toán, biểu diễn, PT bậc hai, tập hợp điểm, cực trị môđun", 12, None, "Chương trình 2006 (không còn trong CT2018)"),
 "hinh-khong-gian-the-tich": ("HHKG - thể tích khối đa diện", "Thể tích khối chóp/lăng trụ/hộp, tỉ số thể tích, khái niệm khối đa diện", 12, 12, "Hình học - Hình học không gian"),
 "hinh-khong-gian-quan-he": ("HHKG - quan hệ song song, vuông góc", "Góc, khoảng cách, quan hệ vuông góc/song song, vectơ trong không gian", 11, 11, "Hình học - Hình học không gian"),
 "khoi-tron-xoay": ("Khối tròn xoay", "Nón, trụ, cầu: công thức, thiết diện, nội/ngoại tiếp, thực tế", 12, None, "Chương trình 2006 (CT2018 chỉ còn ở dạng ứng dụng tích phân)"),
 "oxyz-toa-do-vecto": ("Oxyz - tọa độ điểm, vectơ", "Tọa độ điểm/vectơ, hình chiếu, trung điểm, tích vô hướng", 12, 12, "Hình học - PP tọa độ trong không gian"),
 "oxyz-mat-phang": ("Oxyz - mặt phẳng", "Phương trình mặt phẳng, vectơ pháp tuyến, khoảng cách, bài toán nâng cao", 12, 12, "Hình học - PP tọa độ trong không gian"),
 "oxyz-duong-thang": ("Oxyz - đường thẳng", "Phương trình đường thẳng, vectơ chỉ phương, bài toán nâng cao", 12, 12, "Hình học - PP tọa độ trong không gian"),
 "oxyz-mat-cau": ("Oxyz - mặt cầu", "Phương trình mặt cầu, vị trí tương đối, cực trị, mô hình thực tế", 12, 12, "Hình học - PP tọa độ trong không gian"),
 "oxyz-tong-hop": ("Oxyz - tổng hợp", "Vị trí tương đối, bài toán kết hợp nhiều yếu tố", 12, 12, "Hình học - PP tọa độ trong không gian"),
 "oxy-hinh-phang": ("Hình học phẳng Oxy", "Tọa độ trong mặt phẳng: đường thẳng, đường tròn, tam giác, tứ giác, elip (đề ĐH)", 10, 10, "Hình học - PP tọa độ trong mặt phẳng"),
 "luong-giac": ("Lượng giác", "Công thức lượng giác, PT lượng giác, hàm số lượng giác", 11, 11, "Đại số - Hàm số lượng giác"),
 "to-hop-xac-suat": ("Tổ hợp - xác suất cổ điển", "Quy tắc đếm, hoán vị, chỉnh hợp, tổ hợp, nhị thức Newton, xác suất cổ điển", 11, 11, "Thống kê & Xác suất / Đại số tổ hợp"),
 "xac-suat-co-dieu-kien": ("Xác suất có điều kiện", "Biến cố độc lập, xác suất có điều kiện, công thức xác suất toàn phần, Bayes", 12, 12, "Thống kê & Xác suất"),
 "thong-ke": ("Thống kê mẫu ghép nhóm", "Khoảng biến thiên, tứ phân vị, phương sai, độ lệch chuẩn của mẫu ghép nhóm", 12, 12, "Thống kê & Xác suất"),
 "day-so-cap-so": ("Dãy số - cấp số", "Dãy số, cấp số cộng, cấp số nhân", 11, 11, "Đại số - Dãy số"),
 "gioi-han-lien-tuc": ("Giới hạn - liên tục", "Giới hạn dãy số/hàm số, hàm liên tục", 11, 11, "Giải tích - Giới hạn"),
 "dao-ham-co-ban": ("Đạo hàm cơ bản", "Quy tắc tính đạo hàm (lớp 11)", 11, 11, "Giải tích - Đạo hàm"),
 "phuong-trinh-he-bpt": ("Phương trình, hệ, bất phương trình đại số", "PT/BPT vô tỉ, hệ phương trình, hệ BPT bậc nhất hai ẩn (quy hoạch tuyến tính)", 10, 10, "Đại số"),
 "bat-dang-thuc-cuc-tri": ("Bất đẳng thức - cực trị đại số", "Chứng minh BĐT, GTLN/GTNN biểu thức nhiều biến (câu phân loại cao của đề ĐH)", 10, 10, "Đại số"),
}
# DANG: id -> (chu_de, mô tả)
DANG_TXT = """
hs.bbt-cuc-tri|ham-so-ung-dung-dao-ham|Đọc BBT/đồ thị: điểm cực trị, giá trị cực đại/cực tiểu
hs.cuc-tri-f'|ham-so-ung-dung-dao-ham|Số điểm cực trị khi cho f'(x) (bảng xét dấu hoặc công thức)
hs.do-thi-cuc-tri|ham-so-ung-dung-dao-ham|Số điểm cực trị đọc từ đồ thị
hs.don-dieu-bbt|ham-so-ung-dung-dao-ham|Khoảng đơn điệu từ BBT/bảng xét dấu
hs.don-dieu-do-thi|ham-so-ung-dung-dao-ham|Khoảng đơn điệu từ đồ thị
hs.don-dieu-ham|ham-so-ung-dung-dao-ham|Xét tính đơn điệu hàm cho bằng công thức hoặc f'(x)
hs.gtln-doan|ham-so-ung-dung-dao-ham|GTLN/GTNN trên một đoạn
hs.gtln-m|ham-so-ung-dung-dao-ham|GTLN/GTNN chứa tham số
hs.don-dieu-m|ham-so-ung-dung-dao-ham|Tính đơn điệu chứa tham số m
hs.cuc-tri-m|ham-so-ung-dung-dao-ham|Cực trị chứa tham số m (đếm giá trị nguyên)
hs.cuc-tri-hinh-hoc|ham-so-ung-dung-dao-ham|Cực trị với điều kiện hình học (tam giác vuông/cân, diện tích, vuông góc)
hs.cuc-tri-duong-thang|ham-so-ung-dung-dao-ham|Đường thẳng đi qua hai điểm cực trị
hs.toi-uu|ham-so-ung-dung-dao-ham|Bài toán tối ưu thực tế
hs.mo-hinh|ham-so-ung-dung-dao-ham|Mô hình hàm số thực tế: doanh thu, chi phí, lợi nhuận
hs.dao-ham-dung-sai|ham-so-ung-dung-dao-ham|Đạo hàm, nghiệm f'(x)=0, GTLN trên đoạn (dạng đúng/sai)
hs.f'-do-thi|ham-so-ung-dung-dao-ham|Cho đồ thị/BBT của f'(x), suy ra tính chất của f hoặc hàm liên quan
hs.f'-bbt|ham-so-ung-dung-dao-ham|Cho BBT/đồ thị f hoặc f', xét hàm hợp g(x) (VDC)
hs.ham-hop|ham-so-ung-dung-dao-ham|Hàm hợp/hàm ẩn: đếm cực trị, nghiệm, giá trị tham số
hs.f-vdc|ham-so-ung-dung-dao-ham|Hàm bậc ba/bốn với điều kiện cực trị hoặc giá trị (VDC)
dt.nhan-dang|ham-so-do-thi|Nhận dạng đồ thị hoặc BBT của hàm bậc ba/bốn/phân thức
dt.tiem-can|ham-so-do-thi|Tiệm cận đứng/ngang
dt.tiem-can-vdc|ham-so-do-thi|Tiệm cận kết hợp hình học (VDC)
dt.tuong-giao|ham-so-do-thi|Tương giao đồ thị, số giao điểm
dt.tuong-giao-m|ham-so-do-thi|Tương giao chứa tham số (số nghiệm, điều kiện hình học)
dt.so-nghiem-bbt|ham-so-do-thi|Số nghiệm PT dạng f(x)=m, f(g(x))=m từ BBT/đồ thị
dt.tiep-tuyen|ham-so-do-thi|Tiếp tuyến của đồ thị
dt.tiep-tuyen-vdc|ham-so-do-thi|Tiếp tuyến, điều kiện cắt đồ thị (VDC)
dt.dau-he-so|ham-so-do-thi|Dấu các hệ số, mệnh đề đúng từ đồ thị
dt.co-ban|ham-so-do-thi|Tính chất cơ bản (giao trục, giá trị tại điểm)
dt.khao-sat|ham-so-do-thi|Khảo sát sự biến thiên và vẽ đồ thị (câu 1a của đề ĐH)
dt.diem-tren-do-thi|ham-so-do-thi|Tìm điểm thuộc đồ thị thỏa điều kiện (khoảng cách...)
ml.pt-mu|ham-so-mu-logarit|Giải PT mũ
ml.bpt-mu|ham-so-mu-logarit|Giải BPT mũ
ml.pt-log|ham-so-mu-logarit|Giải PT logarit
ml.bpt-log|ham-so-mu-logarit|Giải BPT logarit
ml.bien-doi|ham-so-mu-logarit|Biến đổi, tính giá trị biểu thức logarit/lũy thừa
ml.tap-xac-dinh|ham-so-mu-logarit|Tập xác định hàm lũy thừa/mũ/logarit
ml.dao-ham|ham-so-mu-logarit|Đạo hàm hàm lũy thừa/mũ/logarit
ml.khai-niem|ham-so-mu-logarit|Khái niệm, giá trị hàm số mũ/lũy thừa
ml.lai-suat|ham-so-mu-logarit|Lãi suất, tăng trưởng (ứng dụng thực tế)
ml.mo-hinh|ham-so-mu-logarit|Mô hình mũ trong thực tế (phân rã, nồng độ)
ml.pt-m|ham-so-mu-logarit|PT/BPT mũ-log chứa tham số
ml.pt-bpt-nguyen|ham-so-mu-logarit|Đếm số nguyên thỏa PT/BPT mũ-log
ml.vdc|ham-so-mu-logarit|Bài toán mũ-logarit nâng cao (hai ẩn, min/max)
nh.co-ban|nguyen-ham-tich-phan|Nguyên hàm cơ bản
nh.tp-tinh-chat|nguyen-ham-tich-phan|Tích phân/nguyên hàm dùng tính chất, đổi cận
nh.tp-co-ban|nguyen-ham-tich-phan|Tính tích phân cơ bản
nh.tp-huu-ti|nguyen-ham-tich-phan|Tích phân hàm hữu tỉ
nh.tp-vo-ti|nguyen-ham-tich-phan|Tích phân hàm chứa căn
nh.tp-mu|nguyen-ham-tich-phan|Tích phân hàm chứa e^x
nh.tp-log|nguyen-ham-tich-phan|Tích phân hàm chứa logarit
nh.tp-luong-giac|nguyen-ham-tich-phan|Tích phân hàm lượng giác
nh.tp-phan-tu|nguyen-ham-tich-phan|Tích phân/nguyên hàm từng phần
nh.tp-do-thi|nguyen-ham-tich-phan|Tích phân đọc từ đồ thị
nh.f-ham-an|nguyen-ham-tich-phan|Tích phân/nguyên hàm hàm ẩn (cho f', F, điều kiện)
ud.dien-tich|ung-dung-tich-phan|Diện tích hình phẳng
ud.dien-tich-vdc|ung-dung-tich-phan|Diện tích hình phẳng nâng cao (hai đồ thị, tham số)
ud.the-tich-xoay|ung-dung-tich-phan|Thể tích khối tròn xoay
ud.van-toc-quang-duong|ung-dung-tich-phan|Vận tốc - quãng đường, đại lượng biến thiên theo thời gian
sp.co-ban|so-phuc|Phần thực/ảo, liên hợp, môđun, số thuần ảo
sp.bieu-dien|so-phuc|Điểm biểu diễn số phức
sp.tinh-toan|so-phuc|Cộng trừ nhân chia số phức
sp.tim-z|so-phuc|Tìm số phức thỏa điều kiện
sp.pt-bac-hai|so-phuc|Nghiệm phức của PT bậc hai
sp.pt-bac-hai-m|so-phuc|PT bậc hai trên tập số phức chứa tham số
sp.tap-hop-diem|so-phuc|Tập hợp điểm biểu diễn
sp.cuc-tri-mo-dun|so-phuc|Cực trị môđun số phức
sp.dem-so-phuc|so-phuc|Đếm số phức thỏa điều kiện
th.khai-niem|hinh-khong-gian-the-tich|Khái niệm khối đa diện, mặt đối xứng
th.chop|hinh-khong-gian-the-tich|Thể tích khối chóp
th.chop-goc|hinh-khong-gian-the-tich|Thể tích chóp khi cho góc/quan hệ vuông góc
th.lang-tru|hinh-khong-gian-the-tich|Thể tích khối lăng trụ
th.lang-tru-goc|hinh-khong-gian-the-tich|Thể tích lăng trụ khi cho góc/hình chiếu
th.lang-tru-thuc-te|hinh-khong-gian-the-tich|Thể tích lăng trụ, bài toán thực tế
th.hop|hinh-khong-gian-the-tich|Thể tích hình hộp/lập phương
th.ty-so|hinh-khong-gian-the-tich|Tỉ số thể tích, chia khối
th.chop-vdc|hinh-khong-gian-the-tich|Thể tích khối chóp (VDC)
th.lang-tru-vdc|hinh-khong-gian-the-tich|Thể tích lăng trụ/khối đa diện (VDC)
th.hop-vdc|hinh-khong-gian-the-tich|Thể tích hình hộp (VDC)
qh.goc|hinh-khong-gian-quan-he|Góc giữa đường thẳng với mặt phẳng, hai đường thẳng
qh.goc-mp|hinh-khong-gian-quan-he|Góc giữa hai mặt phẳng
qh.khoang-cach|hinh-khong-gian-quan-he|Khoảng cách (điểm - mặt phẳng, hai đường chéo nhau)
qh.vuong-goc|hinh-khong-gian-quan-he|Quan hệ vuông góc, chứng minh vuông góc
qh.song-song|hinh-khong-gian-quan-he|Quan hệ song song
qh.vecto|hinh-khong-gian-quan-he|Vectơ trong không gian
tx.non|khoi-tron-xoay|Hình nón: thể tích, diện tích xung quanh
tx.tru|khoi-tron-xoay|Hình trụ: thể tích, diện tích xung quanh
tx.cau|khoi-tron-xoay|Mặt cầu/khối cầu: diện tích, thể tích, bán kính
tx.thiet-dien|khoi-tron-xoay|Thiết diện của nón/trụ
tx.noi-tiep-cau|khoi-tron-xoay|Nón/trụ nội tiếp cầu, mặt cầu ngoại tiếp
tx.thuc-te|khoi-tron-xoay|Khối tròn xoay trong bài toán thực tế
ox.toa-do-diem|oxyz-toa-do-vecto|Tọa độ điểm: hình chiếu, trung điểm, đối xứng
ox.vecto|oxyz-toa-do-vecto|Tọa độ vectơ, tích vô hướng
ox.mp-co-ban|oxyz-mat-phang|Mặt phẳng: phương trình đặc biệt, điểm thuộc mặt phẳng
ox.mp-vtpt|oxyz-mat-phang|Vectơ pháp tuyến của mặt phẳng
ox.mp-pt|oxyz-mat-phang|Viết phương trình mặt phẳng
ox.mp-song-song|oxyz-mat-phang|Mặt phẳng song song/qua điểm
ox.mp-khoang-cach|oxyz-mat-phang|Khoảng cách tới mặt phẳng
ox.mp-vdc|oxyz-mat-phang|Mặt phẳng nâng cao
ox.dt-vtcp|oxyz-duong-thang|Vectơ chỉ phương của đường thẳng
ox.dt-pt|oxyz-duong-thang|Viết phương trình đường thẳng
ox.dt-tong-hop|oxyz-duong-thang|Đường thẳng thỏa điều kiện (cắt, vuông góc, nằm trong)
ox.dt-cuc-tri|oxyz-duong-thang|Đường thẳng, khoảng cách lớn nhất/nhỏ nhất
ox.dt-vdc|oxyz-duong-thang|Đường thẳng nâng cao (VDC)
ox.mc-co-ban|oxyz-mat-cau|Tâm, bán kính mặt cầu
ox.mc-pt|oxyz-mat-cau|Viết phương trình mặt cầu
ox.mc-vitri|oxyz-mat-cau|Mặt phẳng cắt mặt cầu (đường tròn giao)
ox.mc-cuc-tri|oxyz-mat-cau|Mặt cầu và cực trị khoảng cách/diện tích
ox.mc-vdc|oxyz-mat-cau|Mặt cầu nâng cao hoặc mô hình thực tế (GPS, vệ tinh, vành đai)
ox.vitri|oxyz-tong-hop|Vị trí tương đối, tìm điểm thỏa điều kiện
oxy.duong-thang|oxy-hinh-phang|Đường thẳng trong mặt phẳng, điểm thuộc đường thẳng
oxy.duong-tron|oxy-hinh-phang|Đường tròn: viết phương trình, tiếp xúc, tiếp tuyến
oxy.tam-giac|oxy-hinh-phang|Tam giác: tìm tọa độ đỉnh, viết phương trình cạnh
oxy.hinh-vuong|oxy-hinh-phang|Hình vuông
oxy.hinh-chu-nhat|oxy-hinh-phang|Hình chữ nhật
oxy.hinh-thang|oxy-hinh-phang|Hình thang (cân)
oxy.hinh-binh-hanh|oxy-hinh-phang|Hình bình hành
lg.pt|luong-giac|Giải phương trình lượng giác
lg.pt-co-ban|luong-giac|Phương trình lượng giác cơ bản (đề TN)
tk.to-hop-co-ban|to-hop-xac-suat|Quy tắc đếm, hoán vị, chỉnh hợp, tổ hợp
tk.nhi-thuc|to-hop-xac-suat|Nhị thức Newton, hệ số/số hạng trong khai triển
tk.xs-co-dien|to-hop-xac-suat|Xác suất cổ điển (chọn đồ vật, lập số)
tk.dem|to-hop-xac-suat|Bài toán đếm nâng cao (xếp, chia nhóm, trò chơi)
xs.dieu-kien|xac-suat-co-dieu-kien|Xác suất có điều kiện, bảng hai chiều, Bayes
xs.toan-phan|xac-suat-co-dieu-kien|Công thức xác suất toàn phần
xs.doc-lap|xac-suat-co-dieu-kien|Biến cố độc lập, xác suất giao
tke.ghep-nhom|thong-ke|Số đặc trưng mẫu số liệu ghép nhóm
ds.cap-so-cong|day-so-cap-so|Cấp số cộng
ds.cap-so-nhan|day-so-cap-so|Cấp số nhân
ds.so-hang|day-so-cap-so|Số hạng của dãy số
gh.day-so|gioi-han-lien-tuc|Giới hạn dãy số/hàm số
dh1.quy-tac|dao-ham-co-ban|Quy tắc đạo hàm
pt.vo-ti|phuong-trinh-he-bpt|Giải PT vô tỉ
pt.bpt-vo-ti|phuong-trinh-he-bpt|Giải BPT vô tỉ
pt.he|phuong-trinh-he-bpt|Giải hệ phương trình
pt.he-bpt-2-an|phuong-trinh-he-bpt|Hệ BPT bậc nhất hai ẩn, quy hoạch tuyến tính
pt.he-bac-nhat|phuong-trinh-he-bpt|Hệ phương trình bậc nhất nhiều ẩn (bài toán thực tế)
bdt.cm|bat-dang-thuc-cuc-tri|Chứng minh bất đẳng thức
bdt.cuc-tri|bat-dang-thuc-cuc-tri|GTLN/GTNN biểu thức nhiều biến có ràng buộc
"""
DANG = {}
for _l in DANG_TXT.strip().split("\n"):
    _k, _cd, _t = _l.split("|")
    assert _cd in CHU_DE, (_k, _cd)
    DANG[_k] = (_cd, _t)
