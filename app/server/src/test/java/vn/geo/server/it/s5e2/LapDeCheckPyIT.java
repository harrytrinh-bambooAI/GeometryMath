package vn.geo.server.it.s5e2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;
import vn.geo.server.exam.internal.domain.Assembler;
import vn.geo.server.exam.internal.domain.DatasetValidator;
import vn.geo.server.exam.internal.domain.ExamDataset;
import vn.geo.server.exam.internal.domain.ExamModel.Item;
import vn.geo.server.exam.internal.domain.ExamModel.Matrix;
import vn.geo.server.exam.internal.domain.VariantChecker;

/**
 * Bộ lắp đề Java (QS5-06, US-S5E2-03) đối chứng bằng {@code materials/de-thi/check.py} (Kiểm 6,
 * {@code kiem_doc_lap}, độc lập với bộ lắp): mỗi ma trận lắp 3 mã × 2 seed (như check.py) + KT15 8
 * mã seed 2026, ghi JSON ra thư mục tạm, chạy Python kiểm schema đề + mọi ràng buộc + độ khó ≤ 5 %.
 * Không cần Spring/CSDL: đọc thẳng dữ liệu đề.
 */
class LapDeCheckPyIT {

  private static final JsonMapper M = JsonMapper.builder().build();
  private static Path dir;
  private static List<Item> bank;
  private static Map<String, Matrix> matrices;

  static Path dataDir() {
    for (Path d = Path.of("").toAbsolutePath(); d != null; d = d.getParent()) {
      Path p = d.resolve("artifacts/GEO/materials/de-thi");
      if (Files.isDirectory(p)) {
        return p;
      }
    }
    throw new IllegalStateException("không thấy artifacts/GEO/materials/de-thi");
  }

  @BeforeAll
  static void load() {
    dir = dataDir();
    ExamDataset d = ExamDataset.load(dir, M);
    DatasetValidator.Result r = DatasetValidator.validate(d);
    assertThat(r.issues()).isEmpty();
    bank = r.items().stream().map(Item::of).toList();
    matrices =
        r.matrices().stream().collect(Collectors.toMap(m -> m.path("id").asString(), Matrix::of));
  }

  @Test
  @DisplayName("QS5-06 · TC-S5E2-10/12/13 đối chứng: mọi mã Java đạt check.py kiem_doc_lap")
  void javaVariantsPassCheckPy() throws Exception {
    assumeTrue(python3(), "cần python3 để chạy bộ kiểm đối chứng check.py");
    Path tmp = Files.createTempDirectory("s5e2-lapde");
    List<String> files = new ArrayList<>();
    for (Matrix mt : matrices.values()) {
      for (long seed : new long[] {2026, 7}) {
        files.addAll(write(tmp, mt, 3, seed));
      }
    }
    files.addAll(write(tmp, matrices.get("KT15-HHKG11"), 8, 2026));
    files.addAll(write(tmp, matrices.get("TN-THPT-764"), 2, 2026));
    Path script = tmp.resolve("doi_chung.py");
    Files.writeString(script, SCRIPT, StandardCharsets.UTF_8);
    List<String> cmd = new ArrayList<>(List.of("python3", "-I", script.toString(), dir.toString()));
    cmd.addAll(files);
    Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
    String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    assertThat(p.waitFor(120, TimeUnit.SECONDS)).isTrue();
    assertThat(p.exitValue()).as(out).isZero();
    assertThat(out).contains("ĐẠT " + files.size() + " mã");
  }

  @Test
  @DisplayName("BR-S5E2-07 · cùng ma trận + seed → cùng id câu và thứ tự phương án")
  void sameSeedSameSet() {
    Matrix mt = matrices.get("KT15-HHKG11");
    var a = new Assembler(mt, bank).assemble(4, 2026).variants();
    var b = new Assembler(mt, bank).assemble(4, 2026).variants();
    assertThat(M.writeValueAsString(b)).isEqualTo(M.writeValueAsString(a));
    var c = new Assembler(mt, bank).assemble(4, 7).variants();
    assertThat(M.writeValueAsString(c)).isNotEqualTo(M.writeValueAsString(a));
  }

  @Test
  @DisplayName("BR-S5E2-06 · 8 mã KT15 ≤ 5 giây, độ khó lệch ≤ 5 %, mã 101–108")
  void eightVariantsFast() {
    Matrix mt = matrices.get("KT15-HHKG11");
    long t0 = System.nanoTime();
    Assembler.Result r = new Assembler(mt, bank).assemble(8, 2026);
    long ms = (System.nanoTime() - t0) / 1_000_000;
    assertThat(ms).isLessThan(5000);
    assertThat(r.variants())
        .extracting(v -> v.path("ma_de").asString())
        .containsExactly("101", "102", "103", "104", "105", "106", "107", "108");
    List<Double> dks =
        r.variants().stream().map(v -> v.path("do_kho_du_kien").doubleValue()).toList();
    assertThat(VariantChecker.spread(dks)).isLessThanOrEqualTo(0.05);
  }

  @Test
  @DisplayName("BR-S5E2-06 · ô không có câu → báo đúng ô (MSG-S5-12)")
  void insufficientNamesCell() {
    Matrix mt = matrices.get("KT15-HHKG11");
    List<Item> thin = bank.stream().filter(i -> !"tra-loi-ngan".equals(i.type())).toList();
    try {
      new Assembler(mt, thin).assemble(1, 2026);
      org.junit.jupiter.api.Assertions.fail("phải báo thiếu câu");
    } catch (Assembler.InsufficientBankException e) {
      assertThat(mt.cells().stream().filter(c -> c.id().equals(e.cell())).findFirst())
          .get()
          .extracting(c -> c.type())
          .isEqualTo("tra-loi-ngan");
    }
  }

  private static List<String> write(Path tmp, Matrix mt, int n, long seed) throws IOException {
    List<String> out = new ArrayList<>();
    for (ObjectNode v : new Assembler(mt, bank).assemble(n, seed).variants()) {
      Path f =
          tmp.resolve(
              mt.code() + "-" + seed + "-" + n + "-" + v.path("ma_de").asString() + ".json");
      Files.writeString(f, M.writeValueAsString(v), StandardCharsets.UTF_8);
      out.add(f.toString());
    }
    return out;
  }

  private static boolean python3() {
    try {
      return new ProcessBuilder("python3", "--version").start().waitFor() == 0;
    } catch (IOException | InterruptedException e) {
      return false;
    }
  }

  /**
   * Gọi check.kiem_doc_lap + validate(schema de) cho từng mã; độ khó từng nhóm (ma trận, seed, n).
   */
  private static final String SCRIPT =
      """
      import glob, json, os, sys
      from collections import defaultdict
      D = sys.argv[1]
      sys.path.insert(0, D)
      import check
      load = lambda p: json.load(open(p, encoding="utf-8"))
      SC = load(os.path.join(D, "schema", "de.schema.json"))
      mts = {}
      for f in sorted(glob.glob(os.path.join(D, "ma-tran", "*.json"))):
          if not f.endswith("yccd.json"):
              m = load(f); mts[m["id"]] = m
      bank = []
      for f in sorted(glob.glob(os.path.join(D, "ngan-hang", "*.json"))):
          bank += load(f)["cau"]
      by = {it["id"]: it for it in bank}
      yccd = load(os.path.join(D, "ma-tran", "yccd.json"))["yccd"]
      groups = defaultdict(list)
      for f in sys.argv[2:]:
          de = load(f)
          e = check.validate(de, SC)
          if e:
              check.bad(f"schema {f}: {e[:3]}")
          check.kiem_doc_lap(de, mts[de["ma_tran"]], by, yccd)
          groups[os.path.basename(f).rsplit("-", 1)[0]].append(check.lap_de_do_kho(de) if hasattr(check, "lap_de_do_kho") else sum(q["diem"] * by[q["id_cau"]]["do_kho"] for q in de["cau"]) / sum(q["diem"] for q in de["cau"]))
      for g, dks in groups.items():
          lech = (max(dks) - min(dks)) / (sum(dks) / len(dks))
          if lech > 0.05:
              check.bad(f"{g}: độ khó lệch {lech:.3f} > 5 %")
      for s in check.LOI[:40]:
          print("LỖI", s)
      print(("ĐẠT " if not check.LOI else "CHƯA ĐẠT ") + f"{len(sys.argv) - 2} mã")
      sys.exit(1 if check.LOI else 0)
      """;
}
