"""Phiên bản: v1.0 · 27/11/2026
Sinh schema-so.json (JSON Schema cho structured outputs, output_config.format) khớp prompts/schema.md.
Gốc bọc {"ket_qua": anyOf[lesson|patch|question|reply]}; run.py bóc lớp bọc.
Lịch sử: v1.0 27/11/2026 runner-prompt-giaoan — bản đầu.
"""
import json, pathlib


def S():
    return {"type": "string"}


def obj(props):
    return {"type": "object", "properties": props, "required": list(props), "additionalProperties": False}


def arr(it):
    return {"type": "array", "items": it}


def enum(*v):
    return {"type": "string", "enum": list(v)}


de_xuat = obj({"vi_tri": S(), "van_de": S(), "goi_y": S(), "ly_do": S()})
cau = obj({"so": {"type": "integer"}, "nguon": S(), "muc_do": enum("NB", "TH", "VD", "VDC"), "de": S(), "kt": arr(S()),
           "dap_so": {"type": "null"}, "buoc_goi_y": arr(S())})
to_chuc = obj({"chuyen_giao": S(), "thuc_hien": S(), "bao_cao": S(), "ket_luan": S()})
hd = obj({"ma": S(), "loai": enum("mo-dau", "hinh-thanh", "luyen-tap", "van-dung"), "ten": S(), "thoi_luong_phut": {"type": "integer"},
          "o_nha": {"type": "boolean"}, "muc_tieu": S(), "noi_dung": S(), "san_pham": S(), "to_chuc": to_chuc,
          "kien_thuc": arr(obj({"kt": S(), "phat_bieu": S()}))})
lesson = obj({"kind": enum("lesson"),
              "meta": obj({"bai": S(), "tiet": S(), "doi_tuong": enum("co-ban", "kha", "chuyen"),
                           "phong_cach": enum("truyen-thong", "kham-pha", "lop-hoc-dao-nguoc", "du-an-stem", "tro-choi"),
                           "so_tiet": {"type": "integer"}, "tong_phut": {"type": "integer"}}),
              "muc_tieu": obj({"kien_thuc": arr(obj({"noi_dung": S(), "kt": arr(S())})), "nang_luc": arr(S()), "pham_chat": arr(S())}),
              "thiet_bi": obj({"giao_vien": arr(S()), "hoc_sinh": arr(S())}),
              "hoat_dong": arr(hd), "phieu_hoc_tap": arr(obj({"ma": S(), "cau": arr(cau)})), "unsure": arr(S()), "de_xuat": arr(de_xuat)})
val = {"anyOf": [S(), {"type": "integer"}, {"type": "boolean"}, {"type": "null"}, cau, to_chuc]}
patch = obj({"kind": enum("patch"), "patches": arr(obj({"op": enum("replace", "add", "remove"), "path": S(), "before": val, "after": val, "ly_do": S()})),
             "de_xuat": arr(de_xuat), "unsure": arr(S())})
question = obj({"kind": enum("question"), "text": S()})
reply = obj({"kind": enum("reply"), "text": S(), "de_xuat": arr(de_xuat)})
val = {"anyOf": [S(), {"type": "integer"}, {"type": "null"}, cau]}
patch = obj({"kind": enum("patch"), "patches": arr(obj({"op": enum("replace", "add", "remove"), "path": S(), "before": val, "after": val, "ly_do": S()})), "de_xuat": arr(de_xuat), "unsure": arr(S())})
wrap = lambda *alts: {"type": "object", "properties": {"ket_qua": {"anyOf": list(alts)}}, "required": ["ket_qua"], "additionalProperties": False}
lesson_q = dict(lesson, properties=dict(lesson["properties"], cau_hoi=S()), required=lesson["required"] + ["cau_hoi"])
lesson_q["properties"]["meta"]["properties"]["doi_tuong"] = S()
lesson_q["properties"]["meta"]["properties"]["phong_cach"] = S()
for name, root in (("schema-so-soan.json", {"type": "object", "properties": {"ket_qua": lesson_q}, "required": ["ket_qua"], "additionalProperties": False}), ("schema-so-sua.json", wrap(patch, question, reply))):
    (pathlib.Path(__file__).resolve().parent / name).write_text(json.dumps(root, ensure_ascii=False))
    print(name, len(json.dumps(root)))
