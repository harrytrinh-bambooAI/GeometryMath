"""Phiên bản: v1.1 · 27/11/2026
Gọi Anthropic Messages API (urllib), ghi usage.jsonl, chặn ngân sách cứng.
- Khoá đọc từ GEO_AI_API_KEY / ANTHROPIC_API_KEY trong môi trường; KHÔNG in/ghi khoá.
- Trần: 100 lượt sinh (role=gen), 2.500.000 token API (input+output+cache_write+cache_read, sinh + chấm).
Lịch sử: v1.0 27/11/2026 runner-prompt-giaoan — bản đầu.
"""
import json, os, time, threading, urllib.request, urllib.error, pathlib, re

HERE = pathlib.Path(__file__).resolve().parent
USAGE = HERE / "usage.jsonl"
GEN_MODEL = "claude-sonnet-5-5"
JUDGE_MODEL = "claude-opus-5-5"
MAX_GEN = 300  # v1.1: owner duyệt 300 lượt sinh (gồm 98 lượt vòng trước)
MAX_TOKENS_TOTAL = 6_000_000  # v1.1: trần 6M (gồm 1,84M vòng trước)
_lock = threading.Lock()
_inflight_gen = 0


class BudgetExceeded(Exception):
    pass


def totals():
    gen = tok = judge = 0
    if USAGE.exists():
        for line in USAGE.read_text().splitlines():
            if not line.strip():
                continue
            u = json.loads(line)
            t = u["input"] + u["output"] + u["cache_write"] + u["cache_read"]
            tok += t
            if u["role"] == "gen":
                gen += 1
            else:
                judge += 1
    return {"gen_calls": gen, "judge_calls": judge, "tokens": tok}


def _key():
    k = os.environ.get("GEO_AI_API_KEY") or os.environ.get("ANTHROPIC_API_KEY")
    if not k:
        raise SystemExit("KHÔNG CÓ KHOÁ API (GEO_AI_API_KEY/ANTHROPIC_API_KEY) — dừng theo brief.")
    return k


def call(role, task, system, user, max_tokens=16000, effort="medium", fmt=None, model=None):
    """role: 'gen' | 'judge'. Trả (text, meta)."""
    global _inflight_gen
    model = model or (GEN_MODEL if role == "gen" else JUDGE_MODEL)
    with _lock:
        t = totals()
        if t["tokens"] >= MAX_TOKENS_TOTAL:
            raise BudgetExceeded(f"tổng token {t['tokens']} ≥ {MAX_TOKENS_TOTAL}")
        if role == "gen":
            if t["gen_calls"] + _inflight_gen >= MAX_GEN:
                raise BudgetExceeded(f"đã đủ {MAX_GEN} lượt sinh")
            _inflight_gen += 1
    try:
        body = {"model": model, "max_tokens": max_tokens,
                "system": [{"type": "text", "text": system, "cache_control": {"type": "ephemeral"}}],
                "messages": [{"role": "user", "content": user}],
                "output_config": {"effort": effort}}
        if fmt:
            body["output_config"]["format"] = {"type": "json_schema", "schema": fmt}
        data = json.dumps(body).encode()
        last = None
        for attempt in range(4):
            req = urllib.request.Request("https://api.anthropic.com/v1/messages", data=data, method="POST",
                                         headers={"content-type": "application/json", "x-api-key": _key(),
                                                  "anthropic-version": "2023-06-01"})
            t0 = time.time()
            try:
                with urllib.request.urlopen(req, timeout=600) as r:
                    resp = json.loads(r.read())
                break
            except urllib.error.HTTPError as e:
                msg = e.read().decode(errors="replace")[:500]
                last = f"HTTP {e.code}: {msg}"
                if e.code in (429, 500, 502, 503, 529) and attempt < 3:
                    time.sleep(5 * (attempt + 1))
                    continue
                raise RuntimeError(last)
            except (urllib.error.URLError, TimeoutError) as e:
                last = str(e)
                if attempt < 3:
                    time.sleep(5)
                    continue
                raise RuntimeError(last)
        u = resp.get("usage", {})
        rec = {"ts": time.strftime("%Y-%m-%dT%H:%M:%S"), "role": role, "task": task, "model": resp.get("model", model),
               "input": u.get("input_tokens", 0), "output": u.get("output_tokens", 0),
               "cache_write": u.get("cache_creation_input_tokens", 0) or 0, "cache_read": u.get("cache_read_input_tokens", 0) or 0,
               "stop": resp.get("stop_reason"), "sec": round(time.time() - t0, 1)}
        with _lock:
            with USAGE.open("a") as f:
                f.write(json.dumps(rec, ensure_ascii=False) + "\n")
        text = "".join(b.get("text", "") for b in resp.get("content", []) if b.get("type") == "text")
        return text, rec
    finally:
        if role == "gen":
            with _lock:
                _inflight_gen -= 1


def parse_json(text):
    """(obj, strict): strict=True khi toàn bộ văn bản là MỘT JSON hợp lệ."""
    s = text.strip()
    try:
        return json.loads(s), True
    except Exception:
        pass
    m = re.search(r"\{.*\}", s, re.S)
    if m:
        try:
            return json.loads(m.group(0)), False
        except Exception:
            pass
    # sửa tất định phía máy chủ: CHỈ khi thiếu dấu đóng ở cuối (không có cặp ngoặc lệch) thì nối thêm dấu đóng
    st, ins, esc = [], False, False
    for ch in s:
        if ins:
            if esc:
                esc = False
            elif ch == "\\":
                esc = True
            elif ch == '"':
                ins = False
            continue
        if ch == '"':
            ins = True
        elif ch in "{[":
            st.append(ch)
        elif ch in "}]":
            if not st or {"{": "}", "[": "]"}[st.pop()] != ch:
                return None, False
    if st and not ins and s.startswith("{"):
        try:
            return json.loads(s + "".join({"{": "}", "[": "]"}[c] for c in reversed(st))), False
        except Exception:
            pass
    return None, False
