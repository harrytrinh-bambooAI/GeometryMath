package vn.geo.server.quality;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * TC-GEO-0801: mỗi câu mẫu cấm (thêm vào một tệp thử của một module) làm bộ quét báo đúng một lỗi
 * và bản dựng không qua (SqlScanTest đòi 0 vi phạm); gỡ câu mẫu thì 0 lỗi. Dùng cấu hình thật của
 * dự án.
 */
class SqlLintPatternsTest {

  private static final Map<String, String> SAMPLES =
      Map.of(
          "NO_OFFSET",
          "SELECT id FROM sample_item WHERE org_id = :orgId ORDER BY id LIMIT 20 OFFSET 40",
          "NO_SELECT_STAR",
          "SELECT * FROM sample_item WHERE org_id = :orgId LIMIT 20",
          "NO_LEADING_WILDCARD_LIKE",
          "SELECT id FROM sample_item WHERE org_id = :orgId AND title LIKE '%x' LIMIT 20",
          "LARGE_TABLE_NEEDS_LIMIT",
          "SELECT id FROM sample_item WHERE org_id = :orgId",
          "TENANT_FILTER_REQUIRED",
          "SELECT id FROM sample_item WHERE id = :id LIMIT 1",
          "TABLE_OWNERSHIP",
          "SELECT id FROM org WHERE id = :id",
          "FORBIDDEN_TABLE",
          "SELECT final_answer FROM solution WHERE org_id = :orgId AND problem_version_id = :v");

  @Test
  @DisplayName("TC-GEO-0801 Bộ quét SQL bắt các mẫu cấm")
  void tc0801_eachForbiddenPatternIsOneViolation(@TempDir Path dir) throws Exception {
    var cfg = SqlLint.Config.load(Path.of("quality/sql-lint.properties"));
    Path file = null;
    for (var e : SAMPLES.entrySet()) {
      // bảng nhạy cảm thử từ module sở hữu (content) để chỉ còn đúng luật FORBIDDEN_TABLE
      String module = e.getKey().equals("FORBIDDEN_TABLE") ? "content" : "sample";
      Path src = dir.resolve("vn/geo/server/" + module + "/repo");
      Files.createDirectories(src);
      if (file != null) {
        Files.delete(file);
      }
      file = src.resolve("Probe.java");
      Files.writeString(
          file,
          "package vn.geo.server.sample.repo;\nclass Probe {\n  static final String Q = \""
              + e.getValue().replace("\"", "\\\"")
              + "\";\n}\n");
      var result = SqlLint.scan(dir, cfg);
      assertThat(result.violations()).as(e.getKey()).hasSize(1);
      assertThat(result.violations().get(0).rule()).as(e.getKey()).isEqualTo(e.getKey());
    }
    // gỡ câu mẫu: 0 lỗi
    Files.delete(file);
    file = dir.resolve("vn/geo/server/sample/repo/Probe.java");
    Files.writeString(
        file,
        "package vn.geo.server.sample.repo;\nclass Probe {\n  static final String Q = \"SELECT id FROM sample_item WHERE org_id = :orgId ORDER BY id LIMIT 20\";\n}\n");
    assertThat(SqlLint.scan(dir, cfg).violations()).isEmpty();
  }
}
