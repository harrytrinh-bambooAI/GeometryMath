"""Phiên bản: v1.0 · 27/11/2026
Gọi engine hình học thật (DefaultGeometryEngine) để tính đáp số mẫu câu MVP.
Biên dịch app/server/src/main/java (gói geometry + subject.api + common.error, chỉ đọc) ra thư mục tạm,
rồi chạy tools/EngineHarness.java. Không sửa app/**.
"""
import glob
import json
import os
import subprocess
import tempfile

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(os.path.join(HERE, "..", "..", "..", "..", ".."))  # worktree gốc
SRC = os.path.join(ROOT, "app", "server", "src", "main", "java")
RES = os.path.join(ROOT, "app", "server", "src", "main", "resources")
M2 = os.path.expanduser("~/.m2/repository")
OUT = os.path.join(tempfile.gettempdir(), "geo-engine-dethi")
PKGS = ["vn/geo/server/geometry", "vn/geo/server/subject/api", "vn/geo/server/common/error"]
SKIP = ("GlobalExceptionHandler", "ErrorResponseWriter")
JARS = [
    "tools/jackson/core/jackson-databind/*/jackson-databind-*.jar",
    "tools/jackson/core/jackson-core/*/jackson-core-*.jar",
    "com/fasterxml/jackson/core/jackson-annotations/*/jackson-annotations-*.jar",
    "org/springframework/spring-context/*/spring-context-*.jar",
    "org/springframework/spring-core/*/spring-core-*.jar",
    "org/springframework/spring-web/*/spring-web-*.jar",
    "org/slf4j/slf4j-api/*/slf4j-api-*.jar",
]


def _cp():
    out = []
    for pat in JARS:
        c = sorted(j for j in glob.glob(os.path.join(M2, pat)) if "sources" not in j and "javadoc" not in j)
        if not c:
            raise RuntimeError("thiếu jar trong ~/.m2: " + pat)
        out.append(c[-1])
    return ":".join(out)


def compile_engine():
    """Biên dịch lại mỗi lần gọi (nguồn hiện hành), trả classpath chạy."""
    classes = os.path.join(OUT, "classes")
    os.makedirs(classes, exist_ok=True)
    srcs = []
    for p in PKGS:
        for f in glob.glob(os.path.join(SRC, p, "**", "*.java"), recursive=True):
            if not any(s in f for s in SKIP):
                srcs.append(f)
    srcs.append(os.path.join(HERE, "EngineHarness.java"))
    lst = os.path.join(OUT, "srcs.txt")
    with open(lst, "w") as fh:
        fh.write("\n".join(srcs))
    cp = _cp()
    r = subprocess.run(["javac", "-nowarn", "-d", classes, "-cp", cp, "@" + lst], capture_output=True, text=True)
    if r.returncode != 0:
        raise RuntimeError("javac lỗi:\n" + r.stderr[-2000:])
    return classes + ":" + RES + ":" + cp


def run(specs):
    """specs: list {id, spec}. Trả dict id -> list giá trị engine (a = 1, góc theo độ)."""
    cp = compile_engine()
    inp = os.path.join(OUT, "in.json")
    with open(inp, "w", encoding="utf-8") as fh:
        json.dump(specs, fh, ensure_ascii=False)
    r = subprocess.run(["java", "-cp", cp, "EngineHarness", inp], capture_output=True, text=True)
    if r.returncode != 0:
        raise RuntimeError("engine lỗi:\n" + r.stderr[-2000:])
    return {x["id"]: x["values"] for x in json.loads(r.stdout)}


if __name__ == "__main__":
    print(run([{"id": "t", "spec": {"template": "pyramid_square_sa", "params": {"a": 1, "sa": 2.449489742783178},
                                       "unit": "a", "asked": [{"name": "", "kind": "angle_line_plane",
                                                               "points": ["S", "C", "A", "B", "D"]}]}}]))
