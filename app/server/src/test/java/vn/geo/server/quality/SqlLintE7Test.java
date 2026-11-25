package vn.geo.server.quality;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** E7 (US-GEO-704-FC1): bộ kiểm truy vấn chặn đọc bảng sự kiện không có cận thời gian và OFFSET. */
class SqlLintE7Test {

  private static SqlLint.Config cfg() throws Exception {
    return SqlLint.Config.load(Path.of("quality/sql-lint.properties"));
  }

  @Test
  @DisplayName("TC-GEO-0736 Truy vấn sự kiện không có cận thời gian hoặc dùng OFFSET bị báo lỗi")
  void tc0736_noTimeBoundOrOffsetFails(@TempDir Path dir) throws Exception {
    Path pkg = Files.createDirectories(dir.resolve("vn/geo/server/tracking/internal/repo"));
    Files.writeString(
        pkg.resolve("BadEvents.java"),
        """
        package vn.geo.server.tracking.internal.repo;
        class BadEvents {
          String noBound = \"""
              SELECT e.id, e.kind FROM event_log e
              WHERE e.org_id = :orgId AND e.user_id = :userId ORDER BY e.occurred_at DESC LIMIT 50
              \""";
          String offset = \"""
              SELECT e.id, e.kind FROM event_log e
              WHERE e.org_id = :orgId AND e.occurred_at >= :from AND e.occurred_at < :to
              ORDER BY e.occurred_at DESC LIMIT 50 OFFSET 50
              \""";
        }
        """);
    var result = SqlLint.scan(dir, cfg());
    assertThat(result.statements()).isEqualTo(2);
    assertThat(result.violations())
        .extracting(SqlLint.Violation::rule)
        .containsExactlyInAnyOrder("EVENT_TIME_BOUND", "NO_OFFSET");
  }

  @Test
  @DisplayName("US-GEO-704 truy vấn sự kiện có cận thời gian, keyset thì qua")
  void boundedKeysetPasses() throws Exception {
    var v =
        SqlLint.lint(
            """
            SELECT e.id FROM event_log e WHERE e.org_id = :orgId AND e.occurred_at >= :from
            AND e.occurred_at < :to ORDER BY e.occurred_at DESC, e.id DESC LIMIT 51
            """,
            "tracking",
            cfg(),
            "X.java",
            1);
    assertThat(v).isEmpty();
  }

  @Test
  @DisplayName(
      "FIX4b m7 \"occurred_at >\" ngoài vị từ (SELECT, ORDER BY) không được tính là cận thời gian")
  void boundOutsideWhereFails() throws Exception {
    var v =
        SqlLint.lint(
            """
            SELECT e.id, e.occurred_at > now() - interval '1 day' AS recent FROM event_log e
            WHERE e.org_id = :orgId AND e.user_id = :userId ORDER BY e.occurred_at DESC LIMIT 51
            """,
            "tracking",
            cfg(),
            "X.java",
            1);
    assertThat(v).extracting(SqlLint.Violation::rule).contains("EVENT_TIME_BOUND");
  }
}
