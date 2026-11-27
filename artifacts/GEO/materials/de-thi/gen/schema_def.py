"""Phiên bản: v1.0 · 27/11/2026 — sinh schema/*.schema.json (JSON Schema 2020-12, tập con check.py tự kiểm)."""
import json
import os

DT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
LOI = {"type": "object", "required": ["loai_loi", "mo_ta"], "properties": {
    "loai_loi": {"enum": ["sai-tinh-toan", "sai-dinh-li", "sai-hinh-dung", "nham-dieu-kien", "thieu-chung-minh"]},
    "mo_ta": {"type": "string", "minLength": 5}}}
MUC = {"enum": ["NB", "TH", "VD", "VDC"]}
LOAI = {"enum": ["tn4", "dung-sai", "tra-loi-ngan", "tu-luan"]}
NL = {"enum": ["tu-duy-lap-luan", "mo-hinh-hoa", "giai-quyet-van-de", "giao-tiep", "cong-cu"]}
NGUON = {"type": "object", "required": ["loai", "ma"],
         "properties": {"loai": {"enum": ["mvp", "tu-soan", "chinh-thuc"]}, "ma": {"type": "string"}}}
YC = {"type": "array", "minItems": 1, "items": {"type": "string", "pattern": "^(10|11|12)\\.[A-Z]+\\.[0-9]+$"}}
H = "https://json-schema.org/draft/2020-12/schema"

CAU = {
    "$schema": H, "$id": "cau.schema.json", "title": "Câu trong ngân hàng đề (v1.0 · 27/11/2026)", "type": "object",
    "required": ["id", "mau_cau", "bien_the", "chu_de", "dang", "dang_tx", "lop", "yccd", "kien_thuc", "loai_cau", "muc_do",
                 "nang_luc", "thoi_gian_phut", "diem", "do_kho", "boi_canh_key", "de_bai", "dap_an", "dap_so", "nhieu",
                 "nguon", "dap_so_engine"],
    "properties": {
        "id": {"type": "string", "pattern": "^MC-[A-Z0-9-]+\\.(TN|DS|TLN|TL)\\.[0-9]+$"}, "mau_cau": {"type": "string"},
        "bien_the": {"type": "object"}, "chu_de": {"type": "string", "description": "id chủ đề theo lich-su/taxonomy.json"},
        "dang": {"type": "string", "description": "dạng chi tiết (mỗi mẫu câu một dạng); ràng buộc không trùng dạng dùng trường này"},
        "dang_tx": {"type": "string", "description": "dạng theo lich-su/taxonomy.json"}, "lop": {"enum": [10, 11, 12]},
        "yccd": YC, "kien_thuc": {"type": "array", "minItems": 1, "items": {"type": "string"}}, "loai_cau": LOAI,
        "muc_do": MUC, "nang_luc": NL, "thoi_gian_phut": {"type": "number", "exclusiveMinimum": 0},
        "diem": {"type": "number", "exclusiveMinimum": 0},
        "do_kho": {"type": "number", "minimum": 0, "maximum": 1, "description": "độ khó dự kiến = tỉ lệ làm sai dự kiến"},
        "boi_canh_key": {"type": "string"}, "de_bai": {"type": "string", "minLength": 10},
        "dap_an": {"type": "string", "minLength": 1},
        "dap_so": {"type": "object", "required": ["gia_tri", "so"],
                   "properties": {"gia_tri": {"type": "string"}, "so": {"type": ["number", "null"]}}},
        "dap_so_engine": {"type": ["number", "null"]},
        "nhieu": {"type": "array", "items": {"type": "object", "required": ["phuong_an", "loi"],
                                             "properties": {"phuong_an": {"type": "string"}, "loi": LOI}}},
        "lua_chon": {"type": "array", "minItems": 4, "maxItems": 4,
                     "items": {"type": "object", "required": ["noi_dung", "dung"],
                               "properties": {"noi_dung": {"type": "string"}, "dung": {"type": "boolean"}, "loi": LOI}}},
        "y": {"type": "array", "minItems": 4, "maxItems": 4,
              "items": {"type": "object", "required": ["nhan", "menh_de", "dung", "muc_do"],
                        "properties": {"nhan": {"enum": ["a", "b", "c", "d"]}, "menh_de": {"type": "string"},
                                       "dung": {"type": "boolean"}, "muc_do": MUC, "loi": LOI}}},
        "huong_dan_cham": {"type": "array", "minItems": 2,
                           "items": {"type": "object", "required": ["buoc", "noi_dung", "diem"],
                                     "properties": {"buoc": {"type": "string"}, "noi_dung": {"type": "string"},
                                                    "diem": {"type": "number", "exclusiveMinimum": 0}}}},
        "nguon": NGUON},
    "allOf": [
        {"if": {"properties": {"loai_cau": {"const": "tn4"}}},
         "then": {"required": ["lua_chon"], "properties": {"nhieu": {"minItems": 3, "maxItems": 3}}}},
        {"if": {"properties": {"loai_cau": {"const": "dung-sai"}}}, "then": {"required": ["y"]}},
        {"if": {"properties": {"loai_cau": {"const": "tra-loi-ngan"}}},
         "then": {"properties": {"dap_an": {"type": "string", "pattern": "^-?[0-9]+(,[0-9]+)?$", "maxLength": 4}}}},
        {"if": {"properties": {"loai_cau": {"const": "tu-luan"}}}, "then": {"required": ["huong_dan_cham"]}}]}

NH = {"type": "object", "required": ["bieu_thuc", "loi"], "properties": {"bieu_thuc": {"type": "string"}, "loi": LOI}}
MAU = {
    "$schema": H, "$id": "mau_cau.schema.json", "title": "Mẫu câu có tham số (v1.0 · 27/11/2026)", "type": "object",
    "required": ["id", "ten", "chu_de", "dang", "dang_tx", "yccd", "lop", "loai_mau", "loai_cau", "tham_so", "rang_buoc",
                 "dan_xuat", "boi_canh", "nguon", "bien_the", "so_bien_the", "so_cau_sinh", "phien_ban"],
    "properties": {
        "id": {"type": "string", "pattern": "^MC-"}, "ten": {"type": "string"}, "chu_de": {"type": "string"},
        "dang": {"type": "string"}, "dang_tx": {"type": "string"}, "yccd": YC, "lop": {"enum": [10, 11, 12]},
        "loai_mau": {"enum": ["don", "dung-sai"]}, "loai_cau": {"type": "array", "minItems": 1, "items": LOAI},
        "tham_so": {"type": "object", "description": "tên -> danh sách giá trị (chuỗi sympy); khóa '_bo' = danh sách bộ giá trị"},
        "rang_buoc": {"type": "array", "items": {"type": "string"}, "description": "biểu thức sympy phải đúng"},
        "dan_xuat": {"type": "object", "description": "đại lượng dẫn xuất: tên -> biểu thức sympy theo tham số"},
        "boi_canh": {"type": "string"}, "hoi": {"type": "string"},
        "dap_so": {"type": "string", "description": "hàm đáp số: biểu thức sympy theo tham số (góc theo độ, a là đơn vị độ dài)"},
        "nhieu": {"type": "array", "minItems": 3, "items": NH, "description": "sinh nhiễu: mỗi biểu thức gắn một lỗi"},
        "muc_do": {"type": "object"}, "engine": {"type": "object", "required": ["mvp", "ask", "params"]},
        "y": {"type": "array", "minItems": 4, "maxItems": 4}, "tu_luan": {"type": ["array", "null"]},
        "bien_the": {"type": "array", "minItems": 1}, "so_bien_the": {"type": "integer", "minimum": 1},
        "so_cau_sinh": {"type": "integer", "minimum": 1}, "nguon": NGUON, "phien_ban": {"type": "string"}},
    "allOf": [{"if": {"properties": {"loai_mau": {"const": "don"}}}, "then": {"required": ["hoi", "dap_so", "nhieu", "muc_do"]}},
              {"if": {"properties": {"loai_mau": {"const": "dung-sai"}}}, "then": {"required": ["y"]}}]}

NHOM = {"type": "object", "required": ["chu_de", "yccd", "trong_so", "can_cu"],
        "properties": {"chu_de": {"type": "string"}, "dang_tx": {"type": ["array", "null"]}, "yccd": YC,
                       "trong_so": {"type": "number", "exclusiveMinimum": 0, "maximum": 1},
                       "can_cu": {"type": "string", "minLength": 10}}}
MT = {
    "$schema": H, "$id": "ma_tran.schema.json", "title": "Ma trận đề (v1.0 · 27/11/2026)", "type": "object",
    "required": ["id", "ten", "loai_de", "lop", "thoi_gian_phut", "tong_diem", "phan", "can_cu", "gia_dinh", "cells", "bang",
                 "phien_ban"],
    "properties": {
        "id": {"type": "string"}, "ten": {"type": "string"},
        "loai_de": {"enum": ["kiem-tra-15", "giua-ki", "cuoi-ki", "thi-thu-tn-thpt"]}, "lop": {"enum": [10, 11, 12]},
        "thoi_gian_phut": {"type": "number", "exclusiveMinimum": 0}, "tong_diem": {"type": "number"},
        "phan": {"type": "array", "minItems": 1,
                 "items": {"type": "object", "required": ["id", "ten", "loai_cau", "so_cau", "diem_moi_cau"],
                           "properties": {"loai_cau": LOAI, "so_cau": {"type": "integer", "minimum": 1},
                                          "diem_moi_cau": {"type": "number", "exclusiveMinimum": 0}}}},
        "can_cu": {"type": "array", "minItems": 1}, "gia_dinh": {"type": "array"},
        "cells": {"type": "array", "minItems": 1, "items": {
            "type": "object", "required": ["id", "phan", "loai_cau", "muc_do", "so_cau", "diem", "dac_ta", "loai_o"],
            "properties": {"loai_cau": LOAI, "muc_do": MUC, "so_cau": {"type": "integer", "minimum": 1},
                           "diem": {"type": "number", "exclusiveMinimum": 0}, "dac_ta": {"type": "string", "minLength": 5},
                           "loai_o": {"enum": ["co-dinh", "xoay-vong"]}, "yccd": YC,
                           "nhom": {"type": "array", "minItems": 2, "items": NHOM}},
            "allOf": [{"if": {"properties": {"loai_o": {"const": "co-dinh"}}}, "then": {"required": ["chu_de", "yccd", "can_cu"]}},
                      {"if": {"properties": {"loai_o": {"const": "xoay-vong"}}}, "then": {"required": ["nhom"]}}]}},
        "bang": {"type": "array", "description": "hàng chủ đề × cột loại câu|mức độ (ô xoay vòng tính kì vọng theo trọng số)"}}}

DE = {
    "$schema": H, "$id": "de.schema.json", "title": "Mã đề đã lắp (v1.0 · 27/11/2026)", "type": "object",
    "required": ["ma_tran", "ma_de", "seed", "thoi_gian_phut", "tong_diem", "cau", "rang_buoc", "do_kho_du_kien"],
    "properties": {
        "ma_de": {"type": "string"}, "seed": {"type": "integer"}, "do_kho_du_kien": {"type": "number"},
        "cau": {"type": "array", "minItems": 1, "items": {
            "type": "object",
            "required": ["so", "phan", "o", "id_cau", "chu_de", "dang", "yccd", "muc_do", "loai_cau", "diem", "de_bai", "dap_an"],
            "properties": {"so": {"type": "integer", "minimum": 1}, "loai_cau": LOAI, "muc_do": MUC, "yccd": YC,
                           "phuong_an": {"type": "object", "required": ["A", "B", "C", "D"]}}}},
        "rang_buoc": {"type": "object"}}}

if __name__ == "__main__":
    os.makedirs(os.path.join(DT, "schema"), exist_ok=True)
    for n, sc in (("cau", CAU), ("mau_cau", MAU), ("ma_tran", MT), ("de", DE)):
        json.dump(sc, open(os.path.join(DT, "schema", n + ".schema.json"), "w", encoding="utf-8"), ensure_ascii=False, indent=1)
    print("schema: 4 tệp")
