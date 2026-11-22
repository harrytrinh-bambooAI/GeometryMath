package vn.geo.server.quality;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/** Quét toàn bộ SQL trong src/main/java; build đỏ nếu có truy vấn "smell". */
class SqlScanTest {

  @Test
  void mainSourcesHaveNoSqlSmells() throws Exception {
    var cfg = SqlLint.Config.load(Path.of("quality/sql-lint.properties"));
    var result = SqlLint.scan(Path.of("src/main/java"), cfg);
    assertThat(result.statements())
        .as("bộ quét phải tìm thấy câu SQL (tránh quét hụt)")
        .isPositive();
    assertThat(result.violations()).as("Vi phạm quy ước SQL").isEmpty();
  }
}
