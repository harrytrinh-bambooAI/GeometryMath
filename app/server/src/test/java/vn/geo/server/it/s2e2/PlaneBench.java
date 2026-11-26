package vn.geo.server.it.s2e2;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import vn.geo.server.content.internal.plane.service.PlaneBenchCalc;
import vn.geo.server.content.internal.plane.service.PlaneBenchRunner;
import vn.geo.server.content.internal.plane.service.PlaneBenchmarkLoader;
import vn.geo.server.it.e1.AbstractE1IT;

/**
 * Bộ đo hình phẳng S2-E2 — {@code make bench-s2} (US-S2E2-05). KHÔNG chạy trong {@code make verify}
 * (tên không khớp *IT/*Test). Nạp bộ đề chuẩn (content/plane/plane-benchmark.json; lỗi dữ liệu thì
 * dừng), chạy mọi đề qua khung môn học với AI giả lập, xuất {@code
 * artifacts/GEO/evidence/S2-BENCH-<yyyymmdd-HHmm>/report.md + report.json}; rớt một ngưỡng thì thất
 * bại (mã thoát ≠ 0).
 */
@Tag("bench")
class PlaneBench extends AbstractE1IT {

  @Autowired PlaneBenchmarkLoader loader;
  @Autowired PlaneBenchRunner runner;

  @Test
  void benchS2() throws Exception {
    PlaneBenchmarkLoader.LoadReport load = loader.loadDefault();
    System.out.println(
        "bench-s2 nạp: "
            + load.loaded().size()
            + " đề, lỗi "
            + load.errors()
            + ", cảnh báo "
            + load.warnings());
    PlaneBenchCalc.Report r = runner.run();
    String stamp =
        LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
            .format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"));
    String outBase = System.getenv().getOrDefault("GEO_BENCH_OUT", "../../artifacts/GEO/evidence");
    Path dir = Path.of(outBase, "S2-BENCH-" + stamp).toAbsolutePath().normalize();
    Files.createDirectories(dir);
    StringBuilder md = new StringBuilder(r.markdown());
    md.append("\n## Nạp bộ đề\n\n- Đã nạp: ").append(load.loaded().size()).append(" đề\n");
    md.append("- Theo dạng: ").append(load.byForm()).append('\n');
    load.errors()
        .forEach(e ->
            md.append("- Lỗi ").append(e.code()).append(": ").append(e.message()).append('\n'));
    load.warnings().forEach(w -> md.append("- Cảnh báo: ").append(w).append('\n'));
    Files.writeString(dir.resolve("report.md"), md.toString());
    Map<String, Object> json = new LinkedHashMap<>();
    json.put("report", r);
    json.put("load", load);
    Files.writeString(
        dir.resolve("report.json"),
        mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json));
    System.out.println(md);
    System.out.println("bench-s2 báo cáo: " + dir);
    assertThat(load.exitCode()).as("lệnh nạp bộ đề chuẩn").isZero();
    assertThat(r.exitCode()).as("ba ngưỡng bench-s2").isZero();
  }
}
