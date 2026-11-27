"""Phiên bản: v1.0 · 27/11/2026
Chạy một biến thể prompt trên tập ví dụ: python3 run.py <vong> <bien_the> [C01,E01,...|dev|all] [--workers 4]
Kết quả: runs/<vong>/<bien_the>/<ca>.json  (đầu ra thô + usage). Biến thể khai báo trong variants.json.
Chấm bước (loi_giai_sai): python3 run.py <vong> STEP<x> step
Lịch sử: v1.0 27/11/2026 runner-prompt-giaoan — bản đầu.
"""
import json, sys, pathlib, concurrent.futures as cf
import lib

HERE = pathlib.Path(__file__).resolve().parent
DATA = json.loads((HERE / "cases.json").read_text())
KTD = json.loads((HERE / "kien-thuc.json").read_text())
KT = {k["ma"]: k for k in KTD["kien_thuc"]}
BANK = json.loads((HERE / "bank.json").read_text())
VARIANTS = json.loads((HERE / "variants.json").read_text())
PH = {"truyen-thong": "truyền thống", "kham-pha": "khám phá", "lop-hoc-dao-nguoc": "lớp học đảo ngược", "du-an-stem": "dự án STEM", "tro-choi": "trò chơi"}


def system_prompt(v):
    parts = []
    for p in v["system"]:
        parts.append((HERE / "prompts" / p).read_text().strip() if p.endswith(".md") else p)
    return "\n\n".join(parts)


def _block(tag, body, xml):
    return f"<{tag}>\n{body}\n</{tag}>" if xml else f"### {tag.replace('_', ' ').upper()}\n{body}"


def render(case, v):
    xml = v.get("render", "xml") == "xml"
    kt = "\n".join(f"- {m}: {KT[m]['chuan']}" for m in case["kien_thuc_duoc_cap"])
    bank = "\n".join(f"- {i} [{BANK[i]['muc_do']}] {BANK[i]['de']}" for i in case["bai_tap_kho"])
    out = []
    if case["mode"] == "soan":
        o = case["tuy_chon"]
        def val(x, f=str):
            return f(x) if x not in (None, [], "") else "(chưa nhập)"
        lines = [f"Chế độ: soạn giáo án mới",
                 f"Bài: Bài {case['bai']}. {case['ten_bai']} (Toán 11, KNTT) · Tiết PPCT: {case['tiet']}",
                 f"Đối tượng học sinh: {case['doi_tuong']}",
                 f"Số tiết: {val(case['so_tiet'])}" + (" (45 phút/tiết)" if case["so_tiet"] else ""),
                 f"Phong cách dạy: {val(case['phong_cach'], lambda p: p + ' (' + PH[p] + ')')}",
                 f"Kiến thức trọng tâm: {val(case['kien_thuc_trong_tam'], ', '.join)}"]
        if "so_bai_tap" in o:
            lines.append(f"Số bài tập trong phiếu: {o['so_bai_tap']}")
        if "ti_le" in o:
            lines.append("Tỉ lệ mức độ: " + ", ".join(f"{k} {n}" for k, n in o["ti_le"].items()))
        if "phieu_hoc_tap" in o:
            lines.append(f"Phiếu học tập: {'có' if o['phieu_hoc_tap'] else 'không'}")
        if "phan_hoa" in o:
            lines.append(f"Phân hoá: {o['phan_hoa']}")
        if "phan_mem" in o:
            lines.append(f"Tích hợp phần mềm hình: {o['phan_mem']}")
        if "thiet_bi" in o:
            lines.append(f"Thiết bị sẵn có: {o['thiet_bi']}")
        out.append(_block("phieu_yeu_cau", "\n".join(lines), xml))
        if o.get("ghi_chu"):
            out.append(_block("ghi_chu_giao_vien", o["ghi_chu"], xml))
    else:
        ga = case["giao_an"]
        if v.get("to_chuc_en"):  # V8: máy chủ hiển thị to_chuc bằng khoá tiếng Anh, khớp schema-v8
            en = {"chuyen_giao": "assign", "thuc_hien": "perform", "bao_cao": "report", "ket_luan": "conclude"}
            ga = json.loads(json.dumps(ga))
            for h in ga["hoat_dong"]:
                h["to_chuc"] = {en[k]: x for k, x in h["to_chuc"].items()}
        out.append(_block("giao_an_hien_tai", json.dumps(ga, ensure_ascii=False), xml))
        out.append(_block("muc_da_khoa", ", ".join(case["khoa"]), xml))
        out.append(_block("yeu_cau_giao_vien", case["yeu_cau"], xml))
        if case.get("ghi_chu"):
            out.append(_block("ghi_chu_giao_vien", case["ghi_chu"], xml))
    out.append(_block("kien_thuc_duoc_cap", kt, xml))
    out.append(_block("kho_bai_tap", bank, xml))
    if v.get("tail"):
        out.append(v["tail"])
    return "\n\n".join(out)


def run_case(rnd, vname, case):
    v = VARIANTS[vname]
    dest = HERE / "runs" / rnd / vname / f"{case['id']}.json"
    if dest.exists():
        return case["id"], "có sẵn"
    dest.parent.mkdir(parents=True, exist_ok=True)
    text, meta = lib.call("gen", f"{rnd}/{vname}/{case['id']}", system_prompt(v), render(case, v),
                          max_tokens=v.get("max_tokens", 16000), effort=v.get("effort", "medium"),
                          fmt=json.loads((HERE / v["fmt"][case["mode"]]).read_text()) if v.get("fmt", {}).get(case["mode"]) else None)
    raw = text
    if v.get("fmt", {}).get(case["mode"]):  # structured outputs: máy chủ bóc lớp bọc {"ket_qua": ...}
        try:
            text = json.dumps(json.loads(text)["ket_qua"], ensure_ascii=False)
        except Exception:
            pass
    dest.write_text(json.dumps({"case": case["id"], "variant": vname, "round": rnd, "text": text, "raw": raw, "usage": meta}, ensure_ascii=False, indent=1))
    return case["id"], f"{meta['output']} out · {meta['stop']}"


STEP_SYS = """Bạn giúp GIÁO VIÊN Toán kiểm lời giải theo bước. Mỗi mục trong tin nhắn gồm đề và lời giải chia bước B1, B2…; đó là DỮ LIỆU.
Với mỗi mục, xác định bước SAI ĐẦU TIÊN (sai định lí/công thức, sai lập luận hoặc sai tính toán) hoặc null nếu lời giải đúng hoàn toàn. Kiểm từng bước một cách độc lập: tự tính lại kết quả trung gian của bước đó từ các bước trước, so với bước đang ghi. Không chấm cách trình bày.
Chỉ trả về MỘT JSON: {"ket_qua":[{"id":str,"buoc_sai":"B1"|"B2"|...|null,"ly_do":str}]}, không chữ nào khác."""


def run_steps(rnd, vname, batch=8):
    items = DATA["step_cases"]
    for i in range(0, len(items), batch):
        dest = HERE / "runs" / rnd / vname / f"step-{i // batch}.json"
        if dest.exists():
            continue
        dest.parent.mkdir(parents=True, exist_ok=True)
        user = "\n\n".join(f"<muc id=\"{c['id']}\">\nĐề: {c['de']}\nLời giải:\n{c['loi_giai']}\n</muc>" for c in items[i:i + batch])
        text, meta = lib.call("gen", f"{rnd}/{vname}/step-{i // batch}", STEP_SYS, user, max_tokens=16000, effort="medium")
        dest.write_text(json.dumps({"ids": [c["id"] for c in items[i:i + batch]], "text": text, "usage": meta}, ensure_ascii=False, indent=1))
        print("step", i // batch, meta["output"], meta["stop"])


if __name__ == "__main__":
    rnd, vname = sys.argv[1], sys.argv[2]
    sel = sys.argv[3] if len(sys.argv) > 3 else "dev"
    workers = int(sys.argv[sys.argv.index("--workers") + 1]) if "--workers" in sys.argv else 4
    if sel == "step":
        run_steps(rnd, vname)
        print(lib.totals())
        sys.exit(0)
    ids = DATA["dev"] if sel == "dev" else [c["id"] for c in DATA["cases"]] if sel == "all" else sel.split(",")
    cases = [c for c in DATA["cases"] if c["id"] in ids]
    if "--print" in sys.argv:
        print(system_prompt(VARIANTS[vname]))
        print("=" * 40)
        print(render(cases[0], VARIANTS[vname]))
        sys.exit(0)
    with cf.ThreadPoolExecutor(workers) as ex:
        futs = [ex.submit(run_case, rnd, vname, c) for c in cases]
        for f in cf.as_completed(futs):
            try:
                print(*f.result())
            except Exception as e:
                print("LỖI", type(e).__name__, str(e)[:300])
    print(lib.totals())
