#!/usr/bin/env python3
# Phiên bản: v1.0 · 27/11/2026 · sinh de/*.json, taxonomy.json, tan-suat.csv, tan-suat.md từ exam_data.py
# Chạy: python3 build_data.py [--no-curl]   (mặc định kiểm link bằng curl và ghi http/tra_ngay)
import json, os, sys, csv, subprocess, collections, math
from taxonomy_def import CHU_DE, DANG
from exam_data import OLD, NEW, DH

HERE = os.path.dirname(os.path.abspath(__file__))
TRA_NGAY = "2026-11-27"
NO_CURL = "--no-curl" in sys.argv

U88 = "https://toanmath.com/toanmath-pdf/tong-hop-88-de-thi-thpt-quoc-gia-mon-toan-cua-bo-gddt-2005-2026.pdf"
U1720 = "https://toanmath.com/toanmath-pdf/de-thi-thpt-quoc-gia-mon-toan-tu-nam-2017-den-nam-2020.pdf"
U51 = "https://toanmath.com/2024/07/tong-hop-51-de-thi-tot-nghiep-thpt-mon-toan-cua-bo-gddt-2016-2024.html"
UT = lambda y: f"https://toanmath.com/toanmath-pdf/de-chinh-thuc-ky-thi-tot-nghiep-thpt-nam-{y}-mon-toan.pdf"
U764 = "https://luatvietnam.vn/giao-duc/quyet-dinh-764-qd-bgddt-2024-cau-truc-de-thi-ky-thi-tot-nghiep-thpt-tu-2025-302643-d1.html"
UMH = "https://lifestyle.zingnews.vn/bo-gd-dt-cong-bo-de-minh-hoa-cau-truc-de-thi-tot-nghiep-thpt-2025-post1451684.html"
HM = "https://hocmai.vn/file.php/1/Thu_vien_de_thi/Tuyen%20sinh%20DH,%20CD/Nam_2014/Tuyen_sinh_dai_hoc"
UDH14B = HM + "/Khoi_B/Toan/De_Toan_DH_B_Ct_14.pdf"
UDH14A = HM + "/Khoi_A/Toan/TOAN_DH_AA1_CT_14_DE.pdf"
UDH13A = "https://staff.hnue.edu.vn/Portals/0/Users/DaToanA_A1Ct_DH_K13-1373008082.pdf"

_cache = {}
def http(u):
    if u in _cache: return _cache[u]
    if NO_CURL:
        c = 0
    else:
        try:
            r = subprocess.run(["curl", "-sL", "-o", "/dev/null", "-w", "%{http_code}", "--max-time", "25", "-A", "Mozilla/5.0", u],
                               capture_output=True, text=True, timeout=40)
            c = int(r.stdout.strip() or 0)
        except Exception:
            c = 0
    _cache[u] = c
    return c
def nguon(urls):
    return [{"url": u, "http": http(u), "tra_ngay": TRA_NGAY} for u in urls]

# ---- lớp, năng lực ----
def lop_of(dang_first, era):
    cd = DANG[dang_first][0]
    if era == "new":
        if dang_first == "qh.vecto": return 12
        v = CHU_DE[cd][3]
        return v if v else CHU_DE[cd][2]
    if dang_first == "qh.vecto": return 12
    return CHU_DE[cd][2]
READ = ("bbt", "do-thi", "nhan-dang", "ghep-nhom", "dau-he-so", "f'-")
MODEL = ("thuc-te", "lai-suat", "toi-uu", "van-toc", "mo-hinh")
def nang_luc(dangs, muc):
    s = " ".join(dangs)
    if any(k in s for k in MODEL): return "mo-hinh-hoa"
    if muc in ("NB", "TH") and any(k in s for k in READ): return "giao-tiep"
    cds = {DANG[d][0] for d in dangs}
    if muc == "NB": return "cong-cu"
    if cds & {"hinh-khong-gian-the-tich", "hinh-khong-gian-quan-he", "khoi-tron-xoay", "oxy-hinh-phang"} and muc == "TH": return "tu-duy-lap-luan"
    if muc == "TH": return "tu-duy-lap-luan"
    return "giai-quyet-van-de"
def muc_pos(i):
    return "NB" if i <= 20 else "TH" if i <= 35 else "VD" if i <= 45 else "VDC"

def mkcau(so, phan, loai, diem, dang, muc, era, tom_tat=None):
    ds = dang.split("+")
    for d in ds: assert d in DANG, d
    return {"so": so, "phan": phan, "loai_cau": loai, "diem": diem, "chu_de": DANG[ds[0]][0], "dang": dang,
            "lop": lop_of(ds[0], era), "muc_do": muc, "nang_luc": nang_luc(ds, muc),
            "tom_tat": tom_tat or DANG[ds[0]][1]}

exams = []
# --- TN cũ ---
SRC_OLD = {2017: [U88, U1720], 2018: [U88, U1720], 2019: [U88, U1720], 2020: [U88, U1720], 2021: [U88, U51], 2022: [U88, U51],
           2023: [UT(2023), U88], 2024: [UT(2024), U88]}
for y, (ma, s) in OLD.items():
    codes = s.split()
    assert len(codes) == 50, (y, len(codes))
    cau = [mkcau(i + 1, "P1", "tn4", 0.2, c, muc_pos(i + 1), "old") for i, c in enumerate(codes)]
    exams.append({"ma": f"tn-thpt-{y}", "ky_thi": "tn-thpt", "nam": y, "ma_de_dung": ma, "cau_truc_loai": "cu",
        "cau_truc": {"phan": [{"id": "P1", "ten": "Trắc nghiệm 4 lựa chọn", "loai_cau": "tn4", "so_cau": 50, "diem": 10.0}],
                     "thoi_gian_phut": 90, "nguon_cau_truc": "Đề thi (đếm trực tiếp 50 câu); 0,2 điểm/câu theo quy ước chung của đề trắc nghiệm 50 câu"},
        "do_tin_cay": "trung-binh: nhãn chủ đề đọc từ text đề (một số câu bị mất text do cắt trang, gắn theo suy luận); muc_do suy theo vị trí câu (1-20 NB, 21-35 TH, 36-45 VD, 46-50 VDC) vì Bộ không công bố",
        "ghi_chu": "Chỉ lấy một mã đề; các mã đề khác cùng bộ câu hỏi, đổi thứ tự/số liệu." + (" Tài liệu tổng hợp gán nhãn 'Năm 2022 - Lần 1/2' cho 2 đợt thi 2021 (toanmath xác nhận 2021 có 2 đợt, mỗi đợt 4 mã); chọn đợt 1 (giả định 'Lần 1' = đợt 1)." if y == 2021 else ""),
        "nguon": nguon(SRC_OLD[y]), "cau": cau})
# --- TN/MH mới ---
def new_exam(key, ky, nam, ma, items, src, note):
    assert len(items) == 22, (key, len(items))
    cau = []
    for i, (d, m, t) in enumerate(items, 1):
        if i <= 12: p, l, dm = "P1", "tn4", 0.25
        elif i <= 16: p, l, dm = "P2", "dung-sai", 1.0
        else: p, l, dm = "P3", "tra-loi-ngan", 0.5
        cau.append(mkcau(i, p, l, dm, d, m, "new", t))
    return {"ma": f"{ky}-{nam}", "ky_thi": ky, "nam": nam, "ma_de_dung": ma, "cau_truc_loai": "moi",
        "cau_truc": {"phan": [
            {"id": "P1", "ten": "Phần I - Trắc nghiệm nhiều lựa chọn (4 phương án, 1 đúng)", "loai_cau": "tn4", "so_cau": 12, "diem": 3.0},
            {"id": "P2", "ten": "Phần II - Trắc nghiệm đúng/sai (mỗi câu 4 ý)", "loai_cau": "dung-sai", "so_cau": 4, "diem": 4.0},
            {"id": "P3", "ten": "Phần III - Trắc nghiệm trả lời ngắn", "loai_cau": "tra-loi-ngan", "so_cau": 6, "diem": 3.0}],
            "thoi_gian_phut": 90,
            "nguon_cau_truc": "QĐ 764/QĐ-BGDĐT ngày 08/03/2024: 12 câu x 0,25 đ; 4 câu đúng/sai (đúng 1/2/3/4 ý được 0,1/0,25/0,5/1,0 đ); 6 câu x 0,5 đ; tổng 10 đ"},
        "do_tin_cay": "cao cho chủ đề (đọc từ text + ảnh đề 2025); muc_do suy theo vị trí (P1: 1-9 NB, 10-12 TH; P2: câu 1 TH, còn lại VD; P3: 3 câu đầu VD, 3 câu cuối VDC)",
        "ghi_chu": note, "nguon": nguon(src), "cau": cau}
exams.append(new_exam("2025", "tn-thpt", 2025, NEW["2025"][0], NEW["2025"][1], [UT(2025), U88, U764],
    "Đề chính thức đợt thi 6/2025; mã 0101 (24 mã đề, thứ tự câu khác nhau). Ảnh đề mã 0102-0105 đã đối chiếu số câu/cấu trúc."))
exams.append(new_exam("MH2025", "minh-hoa", 2025, NEW["MH2025"][0], NEW["MH2025"][1], [U88, UMH, U764],
    "Đề minh họa theo cấu trúc mới; ngày công bố 29/12/2023 theo bài báo; không xác minh được liệu có bản cập nhật sau QĐ 764 (08/03/2024)."))
exams.append(new_exam("2026", "tn-thpt", 2026, NEW["2026"][0], NEW["2026"][1], [UT(2026), U88],
    "NGOÀI phạm vi yêu cầu (2017-2025) nhưng nguồn có sẵn: đề chính thức đợt 1/2026 (mã 0101). Cấu trúc 12/4/6 khớp đề; chưa tra văn bản riêng cho 2026 (giả định giữ nguyên cấu trúc QĐ 764)."))
# --- ĐH ---
SRC_DH = {("dh-a", 2013): [UDH13A, U88], ("dh-a", 2014): [UDH14A, U88], ("dh-b", 2014): [UDH14B, U88]}
for (ky, y), items in sorted(DH.items(), key=lambda kv: (kv[0][1], kv[0][0])):
    cau, chung, rieng = [], 0.0, 0.0
    for so, d, dang, t, m in items:
        main = so[0] in "12345" if y <= 2011 else (so[0] in "123456" if y <= 2013 else (so[0] in "1" or True))
        # 2009-2011: câu 1-5 chung (7đ), 6-7 riêng (3đ); 2012-2013: câu 1-6 chung (7đ), 7-9 riêng (3đ); 2014: 9 câu không chia phần
        n = int(so.rstrip("ab"))
        if y <= 2011: p = "P1" if n <= 5 else "P2"
        elif y <= 2013: p = "P1" if n <= 6 else "P2"
        else: p = "P1"
        cau.append(mkcau(so, p, "tu-luan", d, dang, m, "old", t))
    s1 = round(sum(c["diem"] for c in cau if c["phan"] == "P1"), 2)
    s2 = round(sum(c["diem"] for c in cau if c["phan"] == "P2"), 2)
    ph = [{"id": "P1", "ten": "Phần chung" if y <= 2013 else "Toàn bộ đề (9 câu)", "loai_cau": "tu-luan",
           "so_cau": sum(1 for c in cau if c["phan"] == "P1"), "diem": s1}]
    if s2: ph.append({"id": "P2", "ten": "Phần riêng - chương trình chuẩn (thí sinh chọn chuẩn hoặc nâng cao)", "loai_cau": "tu-luan",
                      "so_cau": sum(1 for c in cau if c["phan"] == "P2"), "diem": s2})
    ghi = "Chỉ ghi phần riêng theo chương trình Chuẩn; phần riêng Nâng cao (3 điểm, thay thế) KHÔNG đưa vào." if y <= 2013 else "Đề 2014 không còn tách phần chung/riêng."
    if ky == "dh-a" and y >= 2012: ghi += " Đề Toán khối A và A1 là một đề chung."
    if y == 2014 and ky == "dh-a": ghi += " Câu 8, 9 nội dung chi tiết đọc từ đề PDF chính thức."
    exams.append({"ma": f"{ky}-{y}", "ky_thi": ky, "nam": y, "cau_truc_loai": "tu-luan",
        "cau_truc": {"phan": ph, "thoi_gian_phut": 180, "nguon_cau_truc": "Đề thi: 180 phút, thang 10 điểm (câu 1 gồm 2 ý 1 điểm; còn lại theo từng câu)"},
        "do_tin_cay": ("cao: đối chiếu trực tiếp với PDF chính thức của Bộ" if (ky, y) in SRC_DH and y >= 2013 and (ky, y) != ("dh-a", 2013) else
                       "cao: nội dung đọc từ text đề trong bộ tổng hợp; điểm theo thang chuẩn của đề") + "; muc_do là suy luận theo vị trí/độ khó điển hình (đề ĐH không công bố mức độ)",
        "ghi_chu": ghi, "nguon": nguon(SRC_DH.get((ky, y), [U88])), "cau": cau})

# ---- ghi de/*.json ----
os.makedirs(os.path.join(HERE, "de"), exist_ok=True)
for f in os.listdir(os.path.join(HERE, "de")):
    if f.endswith(".json"): os.remove(os.path.join(HERE, "de", f))
for e in exams:
    with open(os.path.join(HERE, "de", f"{e['ma']}.json"), "w", encoding="utf-8") as fh:
        json.dump(e, fh, ensure_ascii=False, indent=1)

# ---- taxonomy.json ----
tax = {"phien_ban": "v1.0 · 27/11/2026", "ghi_chu": "chu_de theo mạch CT2018 + chủ đề CT2006 (số phức, khối tròn xoay, Oxy). lop_ct2006 dùng cho đề ≤2024 và ĐH; lop_ct2018 dùng cho đề 2025+ (null = không còn trong CT2018). dang có thể ghép bằng dấu '+' khi một câu chứa nhiều dạng.",
       "muc_do": {"NB": "Nhận biết", "TH": "Thông hiểu", "VD": "Vận dụng", "VDC": "Vận dụng cao"},
       "nang_luc": ["tu-duy-lap-luan", "mo-hinh-hoa", "giai-quyet-van-de", "giao-tiep", "cong-cu"],
       "chu_de": []}
for k, (ten, mt, l06, l18, mach) in CHU_DE.items():
    tax["chu_de"].append({"id": k, "ten": ten, "mo_ta": mt, "lop_ct2006": l06, "lop_ct2018": l18, "mach_ct2018": mach,
                          "dang": [{"id": d, "mo_ta": t} for d, (c, t) in DANG.items() if c == k]})
with open(os.path.join(HERE, "taxonomy.json"), "w", encoding="utf-8") as fh:
    json.dump(tax, fh, ensure_ascii=False, indent=1)

# ---- tần suất ----
def group_of(e):
    if e["ky_thi"] == "tn-thpt": return "tn-thpt/" + e["cau_truc_loai"]
    return e["ky_thi"]
def agg(exs):
    d = collections.defaultdict(lambda: [0, 0.0])
    for e in exs:
        for c in e["cau"]:
            k = (e["ky_thi"], e["cau_truc_loai"], c["chu_de"], e["nam"])
            d[k][0] += 1; d[k][1] += c["diem"]
    return {k: (v[0], round(v[1], 2)) for k, v in d.items()}
A = agg(exams)
rows = sorted(A.items(), key=lambda kv: (kv[0][0], kv[0][1], kv[0][2], kv[0][3]))
with open(os.path.join(HERE, "tan-suat.csv"), "w", encoding="utf-8", newline="") as fh:
    w = csv.writer(fh)
    w.writerow(["ky_thi", "cau_truc", "chu_de", "nam", "so_cau", "diem"])
    for (ky, ct, cd, y), (n, d) in rows:
        w.writerow([ky, ct, cd, y, n, f"{d:.2f}"])

GROUPS = [("tn-thpt", "cu", "TN THPT cấu trúc cũ (50 câu TN4)", None), ("tn-thpt", "moi", "TN THPT cấu trúc mới (22 câu: 12 TN4 + 4 đúng/sai + 6 trả lời ngắn)", None),
          ("dh-a", "tu-luan", "ĐH khối A/A1 tự luận", None), ("dh-b", "tu-luan", "ĐH khối B tự luận", None)]
def years_of(ky, ct): return sorted({e["nam"] for e in exams if e["ky_thi"] == ky and e["cau_truc_loai"] == ct})
def classify(share):
    return "co-dinh" if share >= 0.9 else "xoay-vong" if share >= 0.3 else "hiem"
def slope(xs, ys):
    n = len(xs); mx = sum(xs) / n; my = sum(ys) / n
    den = sum((x - mx) ** 2 for x in xs)
    return sum((x - mx) * (y - my) for x, y in zip(xs, ys)) / den if den else 0.0

md = ["# Tần suất chủ đề theo kỳ thi", "", "Phiên bản: v1.0 · 27/11/2026 · sinh tự động bởi `build_data.py` từ `de/*.json` (không sửa tay).", "",
      "Cách đọc: ô = `số câu / điểm`. **Cố định** = xuất hiện ≥ 90 % số năm; **xoay vòng** = 30-90 %; **hiếm** = < 30 % (tính trên số năm của nhóm). ",
      "Xu hướng = độ dốc hồi quy tuyến tính của tỉ trọng điểm (điểm chủ đề / 10) theo năm, đơn vị điểm phần trăm mỗi năm (pp/năm); |dốc| < 0,3 coi là ổn định; nhóm ≤ 2 năm: không đủ dữ liệu.",
      "Lưu ý độ tin cậy: nhãn chủ đề các đề 2017-2024 gắn từ text đề (không phải bảng đặc tả của Bộ), mức độ là suy luận; đọc `README.md` mục Giới hạn.", ""]
summary = {}
for ky, ct, title, _ in GROUPS:
    ys = years_of(ky, ct)
    ex = [e for e in exams if e["ky_thi"] == ky and e["cau_truc_loai"] == ct]
    md += [f"## {title} - các năm {ys[0]}-{ys[-1]} ({len(ys)} đề)", ""]
    cds = sorted({c["chu_de"] for e in ex for c in e["cau"]}, key=lambda c: -sum(A.get((ky, ct, c, y), (0, 0))[1] for y in ys))
    md += ["| Chủ đề | " + " | ".join(str(y) for y in ys) + " | % năm | Phân loại | Xu hướng |", "|---|" + "---|" * (len(ys) + 3)]
    summary[(ky, ct)] = {}
    for c in cds:
        cells = []; pts = []
        for y in ys:
            n, d = A.get((ky, ct, c, y), (0, 0.0)); pts.append(d / 10 * 100)
            cells.append(f"{n}/{d:g}" if n else "-")
        share = sum(1 for y in ys if (ky, ct, c, y) in A) / len(ys)
        cl = classify(share)
        if len(ys) <= 2: tr = "không đủ dữ liệu"
        else:
            s = slope(ys, pts)
            tr = "tăng" if s >= 0.3 else "giảm" if s <= -0.3 else "ổn định"
            tr += f" ({s:+.2f} pp/năm)"
        summary[(ky, ct)][c] = (share, cl, tr)
        md.append(f"| {CHU_DE[c][0]} (`{c}`) | " + " | ".join(cells) + f" | {share*100:.0f} % | {cl} | {tr} |")
    md.append("")
    # mức độ theo năm
    md += ["Phân bố mức độ theo năm (số câu; mức độ là suy luận):", "", "| Năm | NB | TH | VD | VDC |", "|---|---|---|---|---|"]
    for y in ys:
        cnt = collections.Counter(c["muc_do"] for e in ex if e["nam"] == y for c in e["cau"])
        md.append(f"| {y} | {cnt['NB']} | {cnt['TH']} | {cnt['VD']} | {cnt['VDC']} |")
    md.append("")
# minh họa
mh = [e for e in exams if e["ky_thi"] == "minh-hoa"]
md += ["## Đề minh họa 2025 (cấu trúc mới, 1 đề - không phân loại cố định/xoay vòng)", "", "| Chủ đề | số câu / điểm |", "|---|---|"]
for (ky, ct, c, y), (n, d) in rows:
    if ky == "minh-hoa": md.append(f"| {CHU_DE[c][0]} | {n}/{d:g} |")
md.append("")
# ĐH A ∪ B
md += ["## ĐH (khối A ∪ B) - chủ đề xuất hiện trong ít nhất một khối mỗi năm, 2009-2014", "", "| Chủ đề | " + " | ".join(str(y) for y in range(2009, 2015)) + " | % năm | Phân loại |", "|---|" + "---|" * 8]
dh = [e for e in exams if e["ky_thi"].startswith("dh")]
dcds = sorted({c["chu_de"] for e in dh for c in e["cau"]})
for c in dcds:
    cells = []; have = 0
    for y in range(2009, 2015):
        n = sum(1 for e in dh if e["nam"] == y for q in e["cau"] if q["chu_de"] == c)
        have += n > 0; cells.append(str(n) if n else "-")
    md.append(f"| {CHU_DE[c][0]} | " + " | ".join(cells) + f" | {have/6*100:.0f} % | {classify(have/6)} |")
md.append("")
# HHKG riêng
md += ["## Hình học không gian (thể tích + quan hệ + khối tròn xoay) - dạng nào hay ra", "",
       "Đếm theo `dang` (dạng ghép bằng '+' được tính cho từng dạng thành phần). Cột = số câu có dạng đó / tổng số đề của nhóm.", ""]
HH = {"hinh-khong-gian-the-tich", "hinh-khong-gian-quan-he", "khoi-tron-xoay"}
for ky, ct, title, _ in GROUPS:
    ex = [e for e in exams if e["ky_thi"] == ky and e["cau_truc_loai"] == ct]
    cnt = collections.Counter(); nex = collections.defaultdict(set); tot = 0
    for e in ex:
        for c in e["cau"]:
            ds = c["dang"].split("+")
            if DANG[ds[0]][0] in HH or any(DANG[d][0] in HH for d in ds):
                tot += 1
                for d in ds:
                    if DANG[d][0] in HH: cnt[d] += 1; nex[d].add(e["nam"])
    md += [f"### {title}", "", f"Tổng câu hình học không gian: {tot} trong {len(ex)} đề (trung bình {tot/len(ex):.1f} câu/đề).", "",
           "| Dạng | Số câu | Số năm xuất hiện | Mô tả |", "|---|---|---|---|"]
    for d, n in cnt.most_common():
        md.append(f"| `{d}` | {n} | {len(nex[d])}/{len(ex)} | {DANG[d][1]} |")
    md.append("")
# kết luận
md += ["## Tóm tắt phân loại (cố định / xoay vòng / hiếm)", ""]
for ky, ct, title, _ in GROUPS:
    s = summary[(ky, ct)]
    cd = [CHU_DE[c][0] for c, v in s.items() if v[1] == "co-dinh"]
    xv = [CHU_DE[c][0] for c, v in s.items() if v[1] == "xoay-vong"]
    hi = [CHU_DE[c][0] for c, v in s.items() if v[1] == "hiem"]
    md += [f"- **{title}**", f"  - Cố định: {', '.join(cd) or '(không có)'}", f"  - Xoay vòng: {', '.join(xv) or '(không có)'}", f"  - Hiếm: {', '.join(hi) or '(không có)'}"]
md.append("")
with open(os.path.join(HERE, "tan-suat.md"), "w", encoding="utf-8") as fh:
    fh.write("\n".join(md))
print("OK", len(exams), "đề;", len(rows), "dòng csv")
for e in exams:
    print(e["ma"], [s["http"] for s in e["nguon"]])
