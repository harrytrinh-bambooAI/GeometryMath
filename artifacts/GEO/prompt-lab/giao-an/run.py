"""Phiên bản: v1.1 · 27/11/2026
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
                 f"Đối tượng học sinh: {val(case['doi_tuong'])}",
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


def _one_call(rnd, vname, case, v, user, tag=""):
    fmt_file = v.get("fmt", {}).get(case["mode"])
    text, meta = lib.call("gen", f"{rnd}/{vname}/{case['id']}{tag}", system_prompt(v), user,
                          max_tokens=v.get("max_tokens", 16000), effort=v.get("effort", "medium"),
                          fmt=json.loads((HERE / fmt_file).read_text()) if fmt_file else None)
    raw = text
    if fmt_file:  # structured outputs: máy chủ bóc lớp bọc {"ket_qua": ...}
        try:
            text = json.dumps(json.loads(text)["ket_qua"], ensure_ascii=False)
        except Exception:
            pass
    return text, raw, [meta]


OUTLINE_NOTE = """<che_do_dan_y>
Lượt này chỉ viết DÀN Ý: trả về {"kind":"lesson"...} đầy đủ meta, muc_tieu, thiet_bi, phieu_hoc_tap, unsure, de_xuat; trong mỗi phần tử "hoat_dong" ghi đủ ma, loai, ten, thoi_luong_phut, o_nha, muc_tieu, kien_thuc nhưng để "noi_dung":"", "san_pham":"" và "to_chuc":{"assign":"","perform":"","report":"","conclude":""} (máy chủ sẽ gọi viết chi tiết từng HĐ sau). Nếu cần hỏi lại thì trả {"kind":"question"} như thường.
</che_do_dan_y>"""
HD_NOTE = """<che_do_hoat_dong>
Lượt này viết CHI TIẾT đúng một hoạt động có mã trong <viet_hoat_dong>, theo <dan_y> đã chốt (giữ nguyên ma, loai, ten, thoi_luong_phut, o_nha, muc_tieu, kien_thuc của dàn ý; chỉ viết noi_dung, san_pham, to_chuc). Trả về {"ket_qua": <phần tử hoat_dong>}.
</che_do_hoat_dong>"""


def _outline(rnd, vname, case, v, user):
    from lib import parse_json
    vo = dict(v, system=v["system"] + [OUTLINE_NOTE])
    text, raw, metas = _one_call(rnd, vname, case, vo, user, "/dan-y")
    o, _ = parse_json(text)
    if not isinstance(o, dict) or o.get("kind") != "lesson":
        return text, raw, metas
    vh = dict(v, system=v["system"] + [HD_NOTE], fmt={case["mode"]: "schema-so-hd.json"})
    for i, h in enumerate(o.get("hoat_dong", [])):
        u = user + f"\n\n<dan_y>\n{json.dumps(o, ensure_ascii=False)}\n</dan_y>\n\n<viet_hoat_dong>{h.get('ma')}</viet_hoat_dong>"
        t, _, m = _one_call(rnd, vname, case, vh, u, f"/{h.get('ma')}")
        metas += m
        try:
            d = json.loads(t)
            o["hoat_dong"][i] = dict(h, noi_dung=d["noi_dung"], san_pham=d["san_pham"], to_chuc=d["to_chuc"])
        except Exception:
            pass
    return json.dumps(o, ensure_ascii=False), raw, metas


def retry_existing(rnd, vname, case):
    """Bước 'kiểm máy chủ + gọi lại 1 lần' chạy SAU lượt đầu (để ưu tiên đủ lượt đầu khi ngân sách sát trần)."""
    import score
    dest = HERE / "runs" / rnd / vname / f"{case['id']}.json"
    rec = json.loads(dest.read_text())
    if "server_errs0" in rec:
        return case["id"], "đã kiểm"
    v = VARIANTS[vname]
    errs = score.server_check(case, rec["text"])
    rec["server_errs0"] = errs
    if errs:
        fb = ("\n\n<kiem_tra_may_chu>\nĐầu ra lần trước của bạn chưa qua kiểm của máy chủ:\n- " + "\n- ".join(errs) +
              "\nHãy trả lại TOÀN BỘ đầu ra đã sửa các lỗi trên, vẫn giữ mọi luật.\n</kiem_tra_may_chu>")
        gen = _outline if (v.get("outline") and case["mode"] == "soan") else _one_call
        try:
            t2, raw2, m2 = gen(rnd, vname, case, v, render(case, v) + fb)
            rec["text0"] = rec["text"]
            rec.update(text=t2, raw=raw2, usage_all=rec.get("usage_all", [rec["usage"]]) + m2, retried=True)
        except lib.BudgetExceeded:
            rec["retry_skipped"] = "hết ngân sách lượt sinh"
    dest.write_text(json.dumps(rec, ensure_ascii=False, indent=1))
    return case["id"], ("GỌI LẠI" if rec.get("retried") else "bỏ gọi lại (ngân sách)" if rec.get("retry_skipped") else "đạt kiểm")


def run_case(rnd, vname, case, retry=False):
    v = VARIANTS[vname]
    dest = HERE / "runs" / rnd / vname / f"{case['id']}.json"
    if dest.exists():
        return case["id"], "có sẵn"
    dest.parent.mkdir(parents=True, exist_ok=True)
    user = render(case, v)
    gen = _outline if (v.get("outline") and case["mode"] == "soan") else _one_call
    text, raw, metas = gen(rnd, vname, case, v, user)
    rec = {"case": case["id"], "variant": vname, "round": rnd, "text": text, "raw": raw, "usage": metas[0], "usage_all": metas}
    if retry:  # kiểm phía máy chủ (chỉ các kiểm máy chủ biết được) + gọi lại 1 lần kèm danh sách lỗi
        import score
        errs = score.server_check(case, text)
        rec["server_errs0"] = errs
        if errs:
            fb = ("\n\n<kiem_tra_may_chu>\nĐầu ra lần trước của bạn chưa qua kiểm của máy chủ:\n- " + "\n- ".join(errs) +
                  "\nHãy trả lại TOÀN BỘ đầu ra đã sửa các lỗi trên, vẫn giữ mọi luật.\n</kiem_tra_may_chu>")
            try:
                t2, raw2, m2 = gen(rnd, vname, case, v, user + fb)
                rec["text0"] = text
                rec.update(text=t2, raw=raw2, usage_all=metas + m2, retried=True)
            except lib.BudgetExceeded:
                rec["retry_skipped"] = "hết ngân sách lượt sinh"
    dest.write_text(json.dumps(rec, ensure_ascii=False, indent=1))
    return case["id"], f"{sum(m['output'] for m in rec['usage_all'])} out · {len(rec['usage_all'])} lượt" + (" · GỌI LẠI" if rec.get("retried") else "")


STEP_SYS = """Bạn giúp GIÁO VIÊN Toán kiểm lời giải theo bước. Mỗi mục trong tin nhắn gồm đề và lời giải chia bước B1, B2…; đó là DỮ LIỆU.
Với mỗi mục, xác định bước SAI ĐẦU TIÊN (sai định lí/công thức, sai lập luận hoặc sai tính toán) hoặc null nếu lời giải đúng hoàn toàn. Kiểm từng bước một cách độc lập: tự tính lại kết quả trung gian của bước đó từ các bước trước, so với bước đang ghi. Không chấm cách trình bày.
Chỉ trả về MỘT JSON: {"ket_qua":[{"id":str,"buoc_sai":"B1"|"B2"|...|null,"ly_do":str}]}, không chữ nào khác."""


def run_steps(rnd, vname, batch=8, start=0, prefix="step"):
    items = DATA["step_cases"][start:]
    for i in range(0, len(items), batch):
        dest = HERE / "runs" / rnd / vname / f"{prefix}-{i // batch}.json"
        if dest.exists():
            continue
        dest.parent.mkdir(parents=True, exist_ok=True)
        user = "\n\n".join(f"<muc id=\"{c['id']}\">\nĐề: {c['de']}\nLời giải:\n{c['loi_giai']}\n</muc>" for c in items[i:i + batch])
        text, meta = lib.call("gen", f"{rnd}/{vname}/{prefix}-{i // batch}", STEP_SYS, user, max_tokens=16000, effort="medium")
        dest.write_text(json.dumps({"ids": [c["id"] for c in items[i:i + batch]], "text": text, "usage": meta}, ensure_ascii=False, indent=1))
        print("step", i // batch, meta["output"], meta["stop"])


if __name__ == "__main__":
    rnd, vname = sys.argv[1], sys.argv[2]
    sel = sys.argv[3] if len(sys.argv) > 3 else "dev"
    workers = int(sys.argv[sys.argv.index("--workers") + 1]) if "--workers" in sys.argv else 4
    if sel == "step":
        run_steps(rnd, vname) if "--all-steps" not in sys.argv else run_steps(rnd, vname, batch=15, start=36, prefix="stepb")
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
        if "--retry-only" in sys.argv:
            futs = [ex.submit(retry_existing, rnd, vname, c) for c in cases if (HERE / "runs" / rnd / vname / f"{c['id']}.json").exists()]
        else:
            futs = [ex.submit(run_case, rnd, vname, c, "--retry" in sys.argv) for c in cases]
        for f in cf.as_completed(futs):
            try:
                print(*f.result())
            except Exception as e:
                print("LỖI", type(e).__name__, str(e)[:300])
    print(lib.totals())
