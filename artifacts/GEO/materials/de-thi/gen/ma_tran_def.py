"""Phiên bản: v1.0 · 27/11/2026
Sinh ma-tran/yccd.json và 4 ma trận chuẩn ma-tran/*.json.
Trọng số ô xoay vòng tính từ hồ sơ đề lịch sử lich-su/de/*.json (đã chốt, chỉ đọc); căn cứ ghi vào từng ô.
Chạy: python3 gen/ma_tran_def.py
"""
import glob
import json
import os
from collections import Counter

HERE = os.path.dirname(os.path.abspath(__file__))
DT = os.path.dirname(HERE)
LS = os.path.join(DT, "lich-su", "de")
PB = "v1.0 · 27/11/2026"

YCCD = {
    "10.BPT.1": "Biểu diễn miền nghiệm của bất phương trình, hệ bất phương trình bậc nhất hai ẩn; vận dụng vào bài toán thực tiễn (tìm giá trị lớn nhất, nhỏ nhất của biểu thức F = ax + by).",
    "10.HPT.1": "Giải hệ phương trình bậc nhất hai, ba ẩn; vận dụng giải bài toán thực tiễn.",
    "10.TH.1": "Vận dụng quy tắc cộng, quy tắc nhân, hoán vị, chỉnh hợp, tổ hợp để đếm.",
    "10.XS.1": "Tính xác suất của biến cố theo định nghĩa cổ điển (dùng phương pháp tổ hợp).",
    "11.LG.1": "Giải phương trình lượng giác cơ bản sin x = m, cos x = m, tan x = m.",
    "11.DS.1": "Nhận biết cấp số cộng; tính số hạng tổng quát, tổng n số hạng đầu.",
    "11.DS.2": "Nhận biết cấp số nhân; tính số hạng tổng quát, tổng n số hạng đầu.",
    "11.TK.1": "Tính số trung bình, trung vị, tứ phân vị, mốt của mẫu số liệu ghép nhóm.",
    "11.ML.1": "Sử dụng tính chất của phép tính lũy thừa, lôgarit để biến đổi, tính giá trị biểu thức.",
    "11.ML.3": "Giải phương trình, bất phương trình mũ, lôgarit ở dạng đơn giản.",
    "11.ML.4": "Giải quyết vấn đề thực tiễn gắn với hàm số mũ, lôgarit (lãi suất, tăng trưởng).",
    "11.VG.1": "Xác định và tính được góc giữa đường thẳng và mặt phẳng.",
    "11.VG.2": "Xác định và tính được khoảng cách từ một điểm đến một mặt phẳng.",
    "11.VG.3": "Xác định và tính được khoảng cách giữa hai đường thẳng chéo nhau trong trường hợp đơn giản.",
    "11.VG.4": "Sử dụng quan hệ vuông góc (định lí Pythagore trong không gian) để tính độ dài đoạn thẳng.",
    "11.TT.1": "Tính thể tích khối chóp, khối lăng trụ, khối hộp.",
    "11.TT.2": "Tính diện tích xung quanh, diện tích toàn phần của hình chóp, hình lăng trụ, hình hộp.",
    "11.XS.1": "Nhận biết biến cố hợp, biến cố giao; sử dụng công thức cộng xác suất.",
    "11.XS.2": "Nhận biết biến cố độc lập; sử dụng công thức nhân xác suất.",
    "11.XS.3": "Tính xác suất bằng phương pháp tổ hợp hoặc sơ đồ hình cây.",
    "11.DH.1": "Tính đạo hàm của hàm đa thức bằng quy tắc; tính đạo hàm tại một điểm.",
    "11.DH.2": "Viết phương trình tiếp tuyến của đồ thị hàm số tại một điểm (ý nghĩa hình học của đạo hàm).",
    "11.DH.3": "Tính đạo hàm cấp hai của hàm số đơn giản.",
    "12.HS.1": "Xét tính đơn điệu, tìm cực trị của hàm số bằng đạo hàm.",
    "12.HS.2": "Tìm giá trị lớn nhất, giá trị nhỏ nhất của hàm số trên một đoạn.",
    "12.HS.3": "Xác định đường tiệm cận đứng, tiệm cận ngang của đồ thị hàm số.",
    "12.HS.4": "Vận dụng đạo hàm giải bài toán tối ưu trong thực tiễn.",
    "12.NH.1": "Tìm nguyên hàm của hàm số đơn giản (đa thức, lũy thừa).",
    "12.NH.2": "Tính tích phân bằng định nghĩa và tính chất.",
    "12.UD.1": "Tính diện tích hình phẳng giới hạn bởi các đồ thị bằng tích phân.",
    "12.UD.2": "Tính thể tích khối tròn xoay khi quay hình phẳng quanh trục Ox.",
    "12.UD.3": "Vận dụng tích phân giải bài toán chuyển động (vận tốc - quãng đường).",
    "12.OX.1": "Xác định tọa độ vectơ, thực hiện phép toán vectơ, tích vô hướng trong không gian Oxyz.",
    "12.OX.2": "Nhận biết vectơ pháp tuyến; lập phương trình mặt phẳng.",
    "12.OX.3": "Nhận biết vectơ chỉ phương; lập phương trình đường thẳng.",
    "12.OX.4": "Xác định tâm, bán kính mặt cầu; vị trí tương đối của mặt cầu và mặt phẳng.",
    "12.XS.1": "Tính xác suất có điều kiện; nhận biết biến cố độc lập.",
    "12.XS.2": "Sử dụng công thức xác suất toàn phần và công thức Bayes.",
    "12.TK.1": "Tính khoảng biến thiên, khoảng tứ phân vị, phương sai của mẫu số liệu ghép nhóm.",
    "12.TX.1": "Tính diện tích, thể tích hình nón, hình trụ, hình cầu (CT2006; CT2018 học ở lớp 9, còn trong đề TN cũ).",
}

DE = {os.path.basename(f)[:-5]: json.load(open(f, encoding="utf-8")) for f in sorted(glob.glob(os.path.join(LS, "*.json")))}
MOI = {"tn-thpt-2025": 1.0, "tn-thpt-2026": 1.0, "minh-hoa-2025": 0.5}  # MH tính nửa trọng số (đề minh họa)
CU = [k for k in DE if k.startswith("tn-thpt-") and DE[k]["cau_truc_loai"] == "cu"]


def xuat_hien(chu_de, phan=None, dang_tx=None, des=MOI):
    out = []
    for k in des:
        for c in DE[k]["cau"]:
            if c["chu_de"] != chu_de or (phan and c["phan"] != phan):
                continue
            if dang_tx and not any(d in c["dang"].split("+") for d in dang_tx):
                continue
            out.append((k, c["so"], c["dang"]))
    return out


def can_cu_str(app, des):
    by = {k: [f"câu {s} ({d})" for kk, s, d in app if kk == k] for k in des}
    return "; ".join(f"{k}: " + (", ".join(v) if v else "không có") for k, v in by.items())


def nhom_moi(phan, items):
    """items: [(chu_de, dang_tx|None, yccd)] → nhóm có trọng số từ 2025, 2026 (×1) và MH 2025 (×0,5), làm trơn +0,5."""
    raw = []
    for cd, dt, yc in items:
        app = xuat_hien(cd, phan, dt)
        w = sum(MOI[k] for k, _, _ in app) + 0.5
        raw.append({"chu_de": cd, "dang_tx": dt, "yccd": yc, "w": w,
                    "can_cu": f"Phần {phan}: " + can_cu_str(app, MOI) + f" → điểm thô {w:g} (2025, 2026 ×1; MH ×0,5; +0,5 làm trơn)"})
    s = sum(r["w"] for r in raw)
    return [{"chu_de": r["chu_de"], "dang_tx": r["dang_tx"], "yccd": r["yccd"], "trong_so": round(r["w"] / s, 4),
             "can_cu": r["can_cu"]} for r in raw]


def nhom_cu_dang(items):
    """Trọng số theo số câu có dạng tương ứng trong 8 đề TN cũ 2017-2024 (+1 làm trơn)."""
    raw = []
    for cd, dt, yc in items:
        app = xuat_hien(cd, None, dt, CU)
        w = len(app) + 1
        raw.append({"chu_de": cd, "dang_tx": dt, "yccd": yc, "w": w,
                    "can_cu": f"TN 2017-2024 (8 đề): {len(app)} câu dạng {'/'.join(dt or [cd])} (+1 làm trơn)"})
    s = sum(r["w"] for r in raw)
    return [{"chu_de": r["chu_de"], "dang_tx": r["dang_tx"], "yccd": r["yccd"], "trong_so": round(r["w"] / s, 4),
             "can_cu": r["can_cu"]} for r in raw]


def nhom_chu_de_moi(items):
    """Trọng số theo số câu của chủ đề trong cả đề 2025, 2026 (×1) và MH (×0,5), +0,5 làm trơn (không tách phần)."""
    raw = []
    for cd, dt, yc in items:
        app = xuat_hien(cd, None, dt)
        w = sum(MOI[k] for k, _, _ in app) + 0.5
        raw.append({"chu_de": cd, "dang_tx": dt, "yccd": yc, "w": w,
                    "can_cu": "Cả đề: " + can_cu_str(app, MOI) + f" → điểm thô {w:g}"})
    s = sum(r["w"] for r in raw)
    return [{"chu_de": r["chu_de"], "dang_tx": r["dang_tx"], "yccd": r["yccd"], "trong_so": round(r["w"] / s, 4),
             "can_cu": r["can_cu"]} for r in raw]


def o(id, phan, loai, muc, diem, dac_ta, chu_de=None, yccd=None, dang_tx=None, nhom=None, can_cu=None):
    c = {"id": id, "phan": phan, "loai_cau": loai, "muc_do": muc, "so_cau": 1, "diem": diem, "dac_ta": dac_ta}
    if nhom:
        c.update(loai_o="xoay-vong", nhom=nhom)
    else:
        c.update(loai_o="co-dinh", chu_de=chu_de, yccd=yccd, dang_tx=dang_tx, can_cu=can_cu)
    return c


def cc_moi(chu_de, phan, dt=None):
    app = xuat_hien(chu_de, phan, dt)
    n = len({k for k, _, _ in app})
    return f"Có mặt ở phần {phan} trong {n}/3 đề cấu trúc mới — " + can_cu_str(app, MOI)


def cc_cu(chu_de, dt):
    app = xuat_hien(chu_de, None, dt, CU)
    n = len({k for k, _, _ in app})
    return f"TN 2017-2024: dạng {'/'.join(dt)} có {len(app)} câu, xuất hiện {n}/8 năm (lich-su/tan-suat.md mục HHKG)"


QH, TT, ML = "hinh-khong-gian-quan-he", "hinh-khong-gian-the-tich", "ham-so-mu-logarit"
XS11, DH, XS12 = "to-hop-xac-suat", "dao-ham-co-ban", "xac-suat-co-dieu-kien"


def tn_764():
    P1, P2, P3 = 0.25, 1.0, 0.5
    cells = [
        o("P1-01", "P1", "tn4", "TH", P1, "Tính góc hoặc khoảng cách trong hình chóp/lăng trụ có cạnh vuông góc với đáy.", QH,
          ["11.VG.1", "11.VG.2", "11.VG.4"], None, can_cu=cc_moi(QH, "P1")),
        o("P1-02", "P1", "tn4", "TH", P1, "Hình học không gian mức thông hiểu: quan hệ vuông góc hoặc thể tích/diện tích khối đa diện.",
          nhom=nhom_moi("P1", [(QH, None, ["11.VG.1", "11.VG.2", "11.VG.3", "11.VG.4"]), (TT, None, ["11.TT.1", "11.TT.2"])])),
        o("P1-03", "P1", "tn4", "NB", P1, "Đọc bảng tần số ghép nhóm, tính số đặc trưng đơn giản.", "thong-ke",
          ["11.TK.1", "12.TK.1"], can_cu=cc_moi("thong-ke", "P1")),
        o("P1-04", "P1", "tn4", "NB", P1, "Nguyên hàm, tích phân cơ bản.", "nguyen-ham-tich-phan", ["12.NH.1", "12.NH.2"],
          can_cu=cc_moi("nguyen-ham-tich-phan", "P1")),
        o("P1-05", "P1", "tn4", "NB", P1, "Số hạng của cấp số cộng/cấp số nhân.", "day-so-cap-so", ["11.DS.1", "11.DS.2"],
          can_cu=cc_moi("day-so-cap-so", "P1")),
        o("P1-06", "P1", "tn4", "NB", P1, "Phương trình mũ, lôgarit cơ bản.", ML, ["11.ML.3", "11.ML.1"], can_cu=cc_moi(ML, "P1")),
        o("P1-07", "P1", "tn4", "NB", P1, "Đường tiệm cận của đồ thị hàm phân thức.", "ham-so-do-thi", ["12.HS.3"],
          can_cu=cc_moi("ham-so-do-thi", "P1")),
        o("P1-08", "P1", "tn4", "NB", P1, "Diện tích hình phẳng hoặc tích phân cơ bản.",
          nhom=nhom_moi("P1", [("ung-dung-tich-phan", None, ["12.UD.1", "12.UD.2"]), ("nguyen-ham-tich-phan", None, ["12.NH.1", "12.NH.2"])])),
        o("P1-09", "P1", "tn4", "NB", P1, "Oxyz: vectơ pháp tuyến/chỉ phương, phương trình mặt phẳng, tọa độ vectơ.",
          nhom=nhom_moi("P1", [("oxyz-mat-phang", None, ["12.OX.2"]), ("oxyz-duong-thang", None, ["12.OX.3"]),
                               ("oxyz-toa-do-vecto", None, ["12.OX.1"])])),
        o("P1-10", "P1", "tn4", "NB", P1, "Một nội dung lớp 10-11 hoặc xác suất cơ bản (xoay vòng theo đề lịch sử).",
          nhom=nhom_moi("P1", [("luong-giac", None, ["11.LG.1"]), (DH, None, ["11.DH.1"]), (XS12, None, ["12.XS.1"]),
                               ("phuong-trinh-he-bpt", None, ["10.BPT.1"])])),
        o("P1-11", "P1", "tn4", "TH", P1, "Oxyz (tích vô hướng, mặt cầu) hoặc cấp số (thông hiểu).",
          nhom=nhom_moi("P1", [("oxyz-toa-do-vecto", None, ["12.OX.1"]), ("oxyz-mat-cau", None, ["12.OX.4"]),
                               ("day-so-cap-so", None, ["11.DS.1", "11.DS.2"])])),
        o("P1-12", "P1", "tn4", "TH", P1, "Thông hiểu: cực trị/GTNN, biến đổi lôgarit, thể tích, số đặc trưng mẫu ghép nhóm.",
          nhom=nhom_moi("P1", [("ham-so-ung-dung-dao-ham", None, ["12.HS.1", "12.HS.2"]), (ML, None, ["11.ML.1", "11.ML.3", "11.ML.4"]),
                               (TT, None, ["11.TT.1", "11.TT.2"]), ("thong-ke", None, ["11.TK.1", "12.TK.1"])])),
        o("P2-01", "P2", "dung-sai", "VD", P2, "Đúng/sai về hàm số: đạo hàm, đơn điệu, cực trị, GTLN.", "ham-so-ung-dung-dao-ham",
          ["12.HS.1", "12.HS.2"], can_cu=cc_moi("ham-so-ung-dung-dao-ham", "P2")),
        o("P2-02", "P2", "dung-sai", "VD", P2, "Đúng/sai về chuyển động: vận tốc, quãng đường bằng tích phân.", "ung-dung-tich-phan",
          ["12.UD.3", "12.NH.2"], can_cu=cc_moi("ung-dung-tich-phan", "P2")),
        o("P2-03", "P2", "dung-sai", "VD", P2, "Đúng/sai về xác suất có điều kiện, toàn phần, Bayes.", XS12, ["12.XS.1", "12.XS.2"],
          can_cu=cc_moi(XS12, "P2")),
        o("P2-04", "P2", "dung-sai", "VD", P2, "Đúng/sai: mô hình mũ hoặc mặt cầu - mặt phẳng trong Oxyz.",
          nhom=nhom_moi("P2", [(ML, None, ["11.ML.3", "11.ML.4"]), ("oxyz-mat-cau", None, ["12.OX.4", "12.OX.2"])])),
        o("P3-01", "P3", "tra-loi-ngan", "VD", P3, "Tính khoảng cách/góc trong không gian (đáp số làm tròn).", QH,
          ["11.VG.1", "11.VG.2", "11.VG.3"], can_cu=cc_moi(QH, "P3")),
        o("P3-02", "P3", "tra-loi-ngan", "VD", P3, "Đếm, xác suất cổ điển.", XS11, ["10.TH.1", "10.XS.1", "11.XS.3"],
          can_cu=cc_moi(XS11, "P3")),
        o("P3-03", "P3", "tra-loi-ngan", "VD", P3, "Bài toán tối ưu thực tiễn bằng đạo hàm.", "ham-so-ung-dung-dao-ham", ["12.HS.4"],
          can_cu=cc_moi("ham-so-ung-dung-dao-ham", "P3")),
        o("P3-04", "P3", "tra-loi-ngan", "VDC", P3, "Thể tích khối tròn xoay/diện tích bằng tích phân.", "ung-dung-tich-phan",
          ["12.UD.1", "12.UD.2"], can_cu=cc_moi("ung-dung-tich-phan", "P3")),
        o("P3-05", "P3", "tra-loi-ngan", "VDC", P3, "Vận dụng cao: xác suất toàn phần hoặc hệ phương trình thực tiễn.",
          nhom=nhom_moi("P3", [(XS12, None, ["12.XS.2"]), ("phuong-trinh-he-bpt", None, ["10.HPT.1", "10.BPT.1"])])),
        o("P3-06", "P3", "tra-loi-ngan", "VD", P3, "Vận dụng: quy hoạch tuyến tính, mặt cầu, đếm/xác suất.",
          nhom=nhom_moi("P3", [("phuong-trinh-he-bpt", None, ["10.BPT.1"]), ("oxyz-mat-cau", None, ["12.OX.4"]),
                               (XS11, None, ["10.TH.1", "10.XS.1", "11.XS.3"])])),
    ]
    return {"id": "TN-THPT-764", "ten": "Thi thử tốt nghiệp THPT môn Toán (định dạng QĐ 764/QĐ-BGDĐT)", "loai_de": "thi-thu-tn-thpt",
            "lop": 12, "thoi_gian_phut": 90, "tong_diem": 10.0,
            "phan": [{"id": "P1", "ten": "Phần I - Trắc nghiệm nhiều lựa chọn", "loai_cau": "tn4", "so_cau": 12, "diem_moi_cau": 0.25},
                     {"id": "P2", "ten": "Phần II - Trắc nghiệm đúng/sai", "loai_cau": "dung-sai", "so_cau": 4, "diem_moi_cau": 1.0,
                      "thang_diem_y": {"1": 0.1, "2": 0.25, "3": 0.5, "4": 1.0}},
                     {"id": "P3", "ten": "Phần III - Trắc nghiệm trả lời ngắn", "loai_cau": "tra-loi-ngan", "so_cau": 6, "diem_moi_cau": 0.5}],
            "can_cu": ["QĐ 764/QĐ-BGDĐT ngày 08/03/2024: 90 phút; 12 TN4 × 0,25; 4 đúng/sai (0,1/0,25/0,5/1,0); 6 trả lời ngắn × 0,5 (lich-su/README.md).",
                       "Chủ đề và vị trí: lich-su/de/tn-thpt-2025.json, tn-thpt-2026.json, minh-hoa-2025.json; lich-su/tan-suat.md mục cấu trúc mới."],
            "gia_dinh": ["Cấu trúc mới chỉ có 2 đề chính thức (2025, 2026) + 1 đề minh họa: ô 'cố định' = chủ đề có ở phần đó trong ≥ 2/3 đề; trọng số xoay vòng = (2025 ×1 + 2026 ×1 + MH ×0,5 + 0,5 làm trơn), chuẩn hóa trong nhóm. Cần cập nhật khi có đề 2027.",
                         "Điểm thô của nhóm xoay vòng đếm mọi câu của chủ đề trong phần đó, kể cả câu đã ứng với ô cố định cùng chủ đề (ví dụ nguyên hàm ở P1-04 và P1-08), nên nghiêng về chủ đề hay ra; một chủ đề có thể vào đề hai lần (khác dạng).",
                         "Mức độ của câu đúng/sai ghi theo ý khó nhất (ý d, VD); ý a NB, ý b-c TH.",
                         "Mức độ trong đề lịch sử là suy luận theo vị trí (lich-su/README.md), không phải đặc tả của Bộ."],
            "cells": cells}


def gk2_11():
    P1, P2, P3 = 0.25, 1.0, 0.5
    g = nhom_cu_dang([(QH, ["qh.goc"], ["11.VG.1"]), (QH, ["qh.khoang-cach"], ["11.VG.2", "11.VG.3"])])
    cells = [
        o("P1-01", "P1", "tn4", "NB", P1, "Giải phương trình mũ cơ bản.", ML, ["11.ML.3"], ["ml.pt-mu"], can_cu=cc_moi(ML, None)),
        o("P1-02", "P1", "tn4", "NB", P1, "Giải phương trình lôgarit cơ bản.", ML, ["11.ML.3"], ["ml.pt-log"], can_cu=cc_moi(ML, "P1", ["ml.pt-log"])),
        o("P1-03", "P1", "tn4", "TH", P1, "Biến đổi biểu thức lôgarit.", ML, ["11.ML.1"], can_cu="Chủ đề mũ-lôgarit có mặt 3/3 đề cấu trúc mới; dạng ml.bien-doi trong đề cũ"),
        o("P1-04", "P1", "tn4", "TH", P1, "Bất phương trình lôgarit hoặc bài toán lãi suất.", ML, ["11.ML.3", "11.ML.4"],
          ["ml.bpt-log", "ml.lai-suat"], can_cu="Dạng ml.bpt-log/ml.lai-suat trong đề TN cũ"),
        o("P1-05", "P1", "tn4", "NB", P1, "Tính độ dài đoạn thẳng nhờ quan hệ vuông góc.", QH, ["11.VG.4"], ["qh.vuong-goc"], can_cu=cc_cu(QH, ["qh.vuong-goc"])),
        o("P1-06", "P1", "tn4", "NB", P1, "Nhận biết góc giữa cạnh bên và đáy.", QH, ["11.VG.1"], ["qh.goc"], can_cu=cc_cu(QH, ["qh.goc"])),
        o("P1-07", "P1", "tn4", "TH", P1, "Tính góc giữa đường thẳng và mặt phẳng.", QH, ["11.VG.1"], ["qh.goc"], can_cu=cc_cu(QH, ["qh.goc"])),
        o("P1-08", "P1", "tn4", "TH", P1, "Tính khoảng cách từ điểm đến mặt phẳng.", QH, ["11.VG.2"], ["qh.khoang-cach"], can_cu=cc_cu(QH, ["qh.khoang-cach"])),
        o("P1-09", "P1", "tn4", "TH", P1, "Góc hoặc khoảng cách (xoay vòng theo tần suất dạng HHKG).", nhom=g),
        o("P1-10", "P1", "tn4", "NB", P1, "Thể tích khối chóp/lăng trụ/hộp bằng công thức.", TT, ["11.TT.1"], can_cu=cc_cu(TT, ["th.chop", "th.lang-tru", "th.hop"])),
        o("P1-11", "P1", "tn4", "VD", P1, "Vận dụng: khoảng cách giữa hai đường chéo nhau hoặc góc phức hợp.",
          nhom=nhom_cu_dang([(QH, ["qh.khoang-cach"], ["11.VG.2", "11.VG.3"]), (QH, ["qh.goc"], ["11.VG.1"])])),
        o("P1-12", "P1", "tn4", "TH", P1, "Phương trình/bất phương trình mũ - lôgarit (thông hiểu).", ML, ["11.ML.3", "11.ML.1", "11.ML.4"],
          can_cu="Mũ-lôgarit: 8/50 câu mỗi năm trong đề TN cũ (lich-su/tan-suat.md)"),
        o("P2-01", "P2", "dung-sai", "VD", P2, "Đúng/sai: mô hình tăng trưởng mũ.", ML, ["11.ML.3", "11.ML.4"], can_cu=cc_moi(ML, "P2")),
        o("P2-02", "P2", "dung-sai", "VD", P2, "Đúng/sai: độ dài, góc, khoảng cách, thể tích trong một hình chóp/lăng trụ.", QH,
          ["11.VG.1", "11.VG.2", "11.VG.3", "11.VG.4", "11.TT.1"], can_cu=cc_cu(QH, ["qh.goc", "qh.khoang-cach"])),
        o("P2-03", "P2", "dung-sai", "VD", P2, "Đúng/sai: hình chóp/lăng trụ/hộp khác ý P2-02.", QH,
          ["11.VG.1", "11.VG.2", "11.VG.3", "11.VG.4", "11.TT.1"], can_cu=cc_cu(QH, ["qh.goc", "qh.khoang-cach"])),
        o("P2-04", "P2", "dung-sai", "VD", P2, "Đúng/sai: hình không gian thứ ba.", QH,
          ["11.VG.1", "11.VG.2", "11.VG.3", "11.VG.4", "11.TT.1", "11.TT.2"], can_cu=cc_cu(QH, ["qh.goc", "qh.khoang-cach"])),
        o("P3-01", "P3", "tra-loi-ngan", "VD", P3, "Vận dụng mũ-lôgarit (lãi kép, số nghiệm nguyên).", ML, ["11.ML.3", "11.ML.4"],
          can_cu=cc_moi(ML, None)),
        o("P3-02", "P3", "tra-loi-ngan", "TH", P3, "Tính giá trị biểu thức lôgarit.", ML, ["11.ML.1", "11.ML.3"], can_cu=cc_moi(ML, None)),
        o("P3-03", "P3", "tra-loi-ngan", "VD", P3, "Tính khoảng cách (đáp số làm tròn).", QH, ["11.VG.2", "11.VG.3"], ["qh.khoang-cach"],
          can_cu=cc_moi(QH, "P3")),
        o("P3-04", "P3", "tra-loi-ngan", "VD", P3, "Góc hoặc khoảng cách mức vận dụng.", nhom=g),
        o("P3-05", "P3", "tra-loi-ngan", "TH", P3, "Thể tích khối đa diện (đáp số làm tròn).", TT, ["11.TT.1", "11.TT.2"], can_cu=cc_cu(TT, ["th.chop", "th.lang-tru", "th.hop"])),
        o("P3-06", "P3", "tra-loi-ngan", "TH", P3, "Độ dài hoặc góc mức thông hiểu.",
          nhom=nhom_cu_dang([(QH, ["qh.vuong-goc"], ["11.VG.4"]), (QH, ["qh.goc"], ["11.VG.1"])])),
    ]
    return {"id": "GK2-L11", "ten": "Kiểm tra giữa học kì II - Toán 11 (Hàm số mũ - lôgarit; Quan hệ vuông góc)", "loai_de": "giua-ki",
            "lop": 11, "thoi_gian_phut": 90, "tong_diem": 10.0,
            "phan": [{"id": "P1", "ten": "Phần I - Trắc nghiệm nhiều lựa chọn", "loai_cau": "tn4", "so_cau": 12, "diem_moi_cau": 0.25},
                     {"id": "P2", "ten": "Phần II - Trắc nghiệm đúng/sai", "loai_cau": "dung-sai", "so_cau": 4, "diem_moi_cau": 1.0,
                      "thang_diem_y": {"1": 0.1, "2": 0.25, "3": 0.5, "4": 1.0}},
                     {"id": "P3", "ten": "Phần III - Trắc nghiệm trả lời ngắn", "loai_cau": "tra-loi-ngan", "so_cau": 6, "diem_moi_cau": 0.5}],
            "can_cu": ["Định dạng 3 phần lấy theo QĐ 764 (đã đọc). CV 7991/BGDĐT-GDTrH (17/12/2024) về đề kiểm tra định kỳ: CHƯA đọc toàn văn (lich-su/README.md).",
                       "Phạm vi: Toán 11 CT2018 học kì II, chương Hàm số mũ - lôgarit và Quan hệ vuông góc (đến hết khoảng cách, thể tích)."],
            "gia_dinh": ["Giả định trường dùng định dạng giống đề TN (12/4/6) cho đề giữa kì; nếu trường dùng 7 điểm TN + 3 điểm tự luận thì chọn ma trận CK2-L11 làm mẫu.",
                         "Trọng số xoay vòng HHKG lấy theo tần suất dạng trong 8 đề TN 2017-2024 (không có dữ liệu đề kiểm tra lớp 11)."],
            "cells": cells}


def ck2_11():
    P1, P2, P3, P4 = 0.25, 1.0, 0.5, 1.5
    cells = [
        o("P1-01", "P1", "tn4", "NB", P1, "Phương trình mũ/lôgarit cơ bản.", ML, ["11.ML.3"], can_cu=cc_moi(ML, "P1")),
        o("P1-02", "P1", "tn4", "TH", P1, "Biến đổi lôgarit, bất phương trình lôgarit, lãi suất.", ML, ["11.ML.1", "11.ML.3", "11.ML.4"], can_cu=cc_moi(ML, None)),
        o("P1-03", "P1", "tn4", "TH", P1, "Góc giữa đường thẳng và mặt phẳng.", QH, ["11.VG.1"], ["qh.goc"], can_cu=cc_cu(QH, ["qh.goc"])),
        o("P1-04", "P1", "tn4", "TH", P1, "Khoảng cách từ điểm đến mặt phẳng.", QH, ["11.VG.2"], ["qh.khoang-cach"], can_cu=cc_cu(QH, ["qh.khoang-cach"])),
        o("P1-05", "P1", "tn4", "NB", P1, "Thể tích khối đa diện.", TT, ["11.TT.1"], can_cu=cc_cu(TT, ["th.chop", "th.lang-tru", "th.hop"])),
        o("P1-06", "P1", "tn4", "NB", P1, "Công thức cộng xác suất.", XS11, ["11.XS.1"], can_cu=cc_moi(XS11, None)),
        o("P1-07", "P1", "tn4", "TH", P1, "Công thức nhân xác suất cho biến cố độc lập.", XS11, ["11.XS.2"], can_cu=cc_moi(XS11, None)),
        o("P1-08", "P1", "tn4", "NB", P1, "Đạo hàm tại một điểm.", DH, ["11.DH.1"], can_cu=cc_moi(DH, "P1")),
        o("P1-09", "P1", "tn4", "TH", P1, "Đạo hàm cấp hai.", DH, ["11.DH.3"], can_cu=cc_moi(DH, "P1")),
        o("P1-10", "P1", "tn4", "TH", P1, "Phương trình tiếp tuyến.", DH, ["11.DH.2"], can_cu=cc_moi(DH, "P1")),
        o("P1-11", "P1", "tn4", "TH", P1, "Thông hiểu một trong bốn mạch của học kì II (xoay vòng).",
          nhom=nhom_chu_de_moi([(ML, None, ["11.ML.1", "11.ML.3", "11.ML.4"]), (QH, None, ["11.VG.1", "11.VG.2", "11.VG.3", "11.VG.4"]),
                                (XS11, None, ["11.XS.1", "11.XS.2", "11.XS.3", "10.XS.1"]), (DH, None, ["11.DH.1", "11.DH.2", "11.DH.3"])])),
        o("P1-12", "P1", "tn4", "VD", P1, "Vận dụng: khoảng cách hoặc góc trong không gian.",
          nhom=nhom_cu_dang([(QH, ["qh.khoang-cach"], ["11.VG.2", "11.VG.3"]), (QH, ["qh.goc"], ["11.VG.1"])])),
        o("P2-01", "P2", "dung-sai", "VD", P2, "Đúng/sai: xác suất (cộng, nhân) hoặc đạo hàm - tiếp tuyến.",
          nhom=nhom_chu_de_moi([(XS11, None, ["11.XS.1", "11.XS.2"]), (DH, None, ["11.DH.1", "11.DH.2", "11.DH.3"])])),
        o("P2-02", "P2", "dung-sai", "VD", P2, "Đúng/sai: hình không gian hoặc mô hình mũ.",
          nhom=nhom_chu_de_moi([(QH, None, ["11.VG.1", "11.VG.2", "11.VG.3", "11.VG.4", "11.TT.1", "11.TT.2"]), (ML, None, ["11.ML.3", "11.ML.4"])])),
        o("P3-01", "P3", "tra-loi-ngan", "VD", P3, "Khoảng cách (đáp số làm tròn).", QH, ["11.VG.2", "11.VG.3"], ["qh.khoang-cach"], can_cu=cc_moi(QH, "P3")),
        o("P3-02", "P3", "tra-loi-ngan", "VD", P3, "Lãi kép/bất phương trình lôgarit.", ML, ["11.ML.3", "11.ML.4"], can_cu=cc_moi(ML, None)),
        o("P3-03", "P3", "tra-loi-ngan", "TH", P3, "Xác suất (cộng, nhân).", XS11, ["11.XS.1", "11.XS.2"], can_cu=cc_moi(XS11, "P3")),
        o("P3-04", "P3", "tra-loi-ngan", "TH", P3, "Đạo hàm cấp hai tại một điểm.", DH, ["11.DH.3", "11.DH.1"], can_cu=cc_moi(DH, None)),
        o("P4-01", "P4", "tu-luan", "VD", P4, "Tự luận: dựng và tính khoảng cách (trình bày lập luận vuông góc).", QH, ["11.VG.2", "11.VG.3"],
          ["qh.khoang-cach"], can_cu=cc_moi(QH, "P3")),
        o("P4-02", "P4", "tu-luan", "VD", P4, "Tự luận: bất phương trình lôgarit, tiếp tuyến hoặc xác suất.",
          nhom=nhom_chu_de_moi([(ML, None, ["11.ML.3"]), (DH, None, ["11.DH.2"]), (XS11, None, ["11.XS.2"])])),
    ]
    return {"id": "CK2-L11", "ten": "Kiểm tra cuối học kì II - Toán 11 (7 điểm trắc nghiệm + 3 điểm tự luận)", "loai_de": "cuoi-ki",
            "lop": 11, "thoi_gian_phut": 90, "tong_diem": 10.0,
            "phan": [{"id": "P1", "ten": "Phần I - Trắc nghiệm nhiều lựa chọn", "loai_cau": "tn4", "so_cau": 12, "diem_moi_cau": 0.25},
                     {"id": "P2", "ten": "Phần II - Trắc nghiệm đúng/sai", "loai_cau": "dung-sai", "so_cau": 2, "diem_moi_cau": 1.0,
                      "thang_diem_y": {"1": 0.1, "2": 0.25, "3": 0.5, "4": 1.0}},
                     {"id": "P3", "ten": "Phần III - Trắc nghiệm trả lời ngắn", "loai_cau": "tra-loi-ngan", "so_cau": 4, "diem_moi_cau": 0.5},
                     {"id": "P4", "ten": "Phần IV - Tự luận", "loai_cau": "tu-luan", "so_cau": 2, "diem_moi_cau": 1.5}],
            "can_cu": ["Phạm vi: Toán 11 CT2018 cả học kì II (mũ - lôgarit, quan hệ vuông góc, xác suất, đạo hàm).",
                       "Ba dạng trắc nghiệm theo QĐ 764; tỉ lệ 7 điểm trắc nghiệm + 3 điểm tự luận là giả định (CV 7991 chưa đọc toàn văn)."],
            "gia_dinh": ["Tỉ lệ 7/3 và số câu mỗi phần là giả định của nhóm, giáo viên đổi được qua form (số câu, điểm mỗi phần).",
                         "Trọng số xoay vòng theo số câu của chủ đề trong đề 2025, 2026 (×1) và MH 2025 (×0,5); dạng HHKG theo đề TN 2017-2024.",
                         "Hướng dẫn chấm tự luận có thang 1,0 trong ngân hàng; bộ lắp nhân hệ số 1,5 theo điểm ô."],
            "cells": cells}


def kt15():
    cells = [
        o("K-01", "P1", "tn4", "NB", 1.0, "Thể tích khối chóp/lăng trụ/hộp bằng công thức.", TT, ["11.TT.1"], can_cu=cc_cu(TT, ["th.chop", "th.lang-tru", "th.hop"])),
        o("K-02", "P1", "tn4", "NB", 1.0, "Độ dài đoạn thẳng nhờ quan hệ vuông góc.", QH, ["11.VG.4"], ["qh.vuong-goc"], can_cu=cc_cu(QH, ["qh.vuong-goc"])),
        o("K-03", "P1", "tn4", "TH", 1.0, "Góc giữa đường thẳng và mặt phẳng.", QH, ["11.VG.1"], ["qh.goc"], can_cu=cc_cu(QH, ["qh.goc"])),
        o("K-04", "P1", "tn4", "TH", 1.0, "Góc, khoảng cách hoặc thể tích (xoay vòng theo tần suất dạng HHKG).",
          nhom=nhom_cu_dang([(QH, ["qh.goc"], ["11.VG.1"]), (QH, ["qh.khoang-cach"], ["11.VG.2", "11.VG.3"]),
                             (TT, ["th.chop"], ["11.TT.1", "11.TT.2"]), (TT, ["th.lang-tru"], ["11.TT.1", "11.TT.2"]),
                             (TT, ["th.hop"], ["11.TT.1", "11.TT.2"])])),
        o("K-05", "P1", "tn4", "TH", 1.0, "Khoảng cách từ điểm đến mặt phẳng.", QH, ["11.VG.2"], ["qh.khoang-cach"], can_cu=cc_cu(QH, ["qh.khoang-cach"])),
        o("K-06", "P1", "tn4", "VD", 1.0, "Vận dụng: khoảng cách hai đường chéo nhau hoặc góc phức hợp.",
          nhom=nhom_cu_dang([(QH, ["qh.khoang-cach"], ["11.VG.2", "11.VG.3"]), (QH, ["qh.goc"], ["11.VG.1"])])),
        o("K-07", "P2", "tra-loi-ngan", "NB", 2.0, "Thể tích khối lăng trụ/hộp (đáp số số).",
          nhom=nhom_cu_dang([(TT, ["th.lang-tru"], ["11.TT.1"]), (TT, ["th.hop"], ["11.TT.1"])])),
        o("K-08", "P2", "tra-loi-ngan", "VD", 2.0, "Tính khoảng cách (đáp số làm tròn).", QH, ["11.VG.2", "11.VG.3"], ["qh.khoang-cach"],
          can_cu=cc_cu(QH, ["qh.khoang-cach"])),
    ]
    return {"id": "KT15-HHKG11", "ten": "Kiểm tra 15 phút - Toán 11, chương Quan hệ vuông góc trong không gian", "loai_de": "kiem-tra-15",
            "lop": 11, "thoi_gian_phut": 15, "tong_diem": 10.0,
            "phan": [{"id": "P1", "ten": "Phần I - Trắc nghiệm nhiều lựa chọn", "loai_cau": "tn4", "so_cau": 6, "diem_moi_cau": 1.0},
                     {"id": "P2", "ten": "Phần II - Trả lời ngắn", "loai_cau": "tra-loi-ngan", "so_cau": 2, "diem_moi_cau": 2.0}],
            "can_cu": ["Một chương HHKG lớp 11 (CT2018): góc, khoảng cách, thể tích.",
                       "Trọng số xoay vòng theo tần suất dạng HHKG trong 8 đề TN 2017-2024 (lich-su/tan-suat.md mục HHKG)."],
            "gia_dinh": ["Số câu và điểm (6 × 1,0 + 2 × 2,0) là giả định; thời gian dự kiến tổng 15 phút theo bảng thời gian/câu của gen/core.py."],
            "cells": cells}


def bang(mt):
    """Hàng chủ đề × cột (loại câu, mức độ): ô cố định đếm 1; ô xoay vòng chia theo trọng số (kì vọng)."""
    rows = {}
    for c in mt["cells"]:
        col = f"{c['loai_cau']}|{c['muc_do']}"
        items = [(c["chu_de"], 1.0)] if c["loai_o"] == "co-dinh" else [(g["chu_de"], g["trong_so"]) for g in c["nhom"]]
        for cd, w in items:
            r = rows.setdefault(cd, {})
            r[col] = round(r.get(col, 0) + w * c["so_cau"], 4)
    return [{"chu_de": cd, "o": v, "tong_cau_ki_vong": round(sum(v.values()), 4)} for cd, v in rows.items()]


def main():
    os.makedirs(os.path.join(DT, "ma-tran"), exist_ok=True)
    json.dump({"phien_ban": PB, "nguon": "Chương trình GDPT môn Toán 2018 (TT 32/2018/TT-BGDĐT), diễn đạt lại ngắn gọn theo mạch nội dung; 12.TX.1 theo CT2006.",
               "yccd": YCCD}, open(os.path.join(DT, "ma-tran", "yccd.json"), "w", encoding="utf-8"), ensure_ascii=False, indent=1)
    for mt in (kt15(), gk2_11(), ck2_11(), tn_764()):
        mt["phien_ban"] = PB
        mt["bang"] = bang(mt)
        mt["tong_cau"] = sum(c["so_cau"] for c in mt["cells"])
        json.dump(mt, open(os.path.join(DT, "ma-tran", mt["id"] + ".json"), "w", encoding="utf-8"), ensure_ascii=False, indent=1)
        print(mt["id"], mt["tong_cau"], "câu,", round(sum(c["diem"] * c["so_cau"] for c in mt["cells"]), 4), "điểm,",
              sum(1 for c in mt["cells"] if c["loai_o"] == "xoay-vong"), "ô xoay vòng")


if __name__ == "__main__":
    main()
