package vn.geo.server.it.e1;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.it.AbstractIT;

/**
 * Nền cho IT của E1: nạp DỮ LIỆU VÀNG của E2E (app/db/seed/e2e/*.sql, một nguồn duy nhất) vào
 * geo_test một lần mỗi JVM, giống scripts/seed-reset.sh (TRUNCATE mọi bảng trừ keep-tables.txt rồi
 * chạy seed với T0 = giờ máy chủ).
 */
public abstract class AbstractE1IT extends AbstractIT {

  public static final UUID ORG_A = id(1);
  public static final UUID ORG_C = id(2);
  public static final UUID TEACHER_A = id(3);
  public static final UUID TEACHER_C = id(5);
  public static final UUID AN = id(20);
  public static final UUID BINH = id(21);
  public static final UUID DUNG = id(23);
  public static final UUID CLASS_11A1 = id(10);
  public static final UUID CLASS_9C = id(12);
  public static final UUID BG1 = id(40);
  public static final UUID BG2 = id(41);
  public static final UUID BG4 = id(43);
  public static final UUID BGC = id(45);
  public static final UUID HKG01_V = id(100031);
  public static final UUID AUTHOR = UUID.fromString("00000000-0000-7000-8000-0000000000aa");

  private static volatile boolean seeded;
  protected static Instant t0;

  @Autowired protected JdbcClient jdbc;
  @Autowired protected javax.sql.DataSource dataSource;
  private final HttpClient http = HttpClient.newHttpClient();

  public static UUID id(long n) {
    return UUID.fromString(String.format("00000000-0000-7000-8000-%012d", n));
  }

  @BeforeEach
  void seedOnce() throws IOException {
    synchronized (AbstractE1IT.class) {
      if (!seeded) {
        reseed();
        seeded = true;
      }
    }
  }

  /** Đưa geo_test về trạng thái vàng (dùng khi một lớp test cần dữ liệu sạch). */
  protected void reseed() throws IOException {
    Path seedDir = Path.of("../db/seed");
    List<String> keep =
        Files.readAllLines(seedDir.resolve("keep-tables.txt")).stream()
            .map(String::strip)
            .filter(l -> !l.isEmpty() && !l.startsWith("#"))
            .toList();
    t0 = jdbc.sql("SELECT now()").query(java.time.OffsetDateTime.class).single().toInstant();
    StringBuilder sql = new StringBuilder("BEGIN;\n");
    String keepList = String.join("','", keep);
    sql.append(
        "DO $$ DECLARE t text; BEGIN SELECT string_agg(format('%I.%I', schemaname, tablename), ', ') INTO t"
            + " FROM pg_tables WHERE schemaname = 'public' AND tablename NOT IN ('"
            + keepList
            + "'); IF t IS NOT NULL THEN EXECUTE 'TRUNCATE ' || t || ' RESTART IDENTITY CASCADE'; END IF; END $$;\n");
    try (Stream<Path> files = Files.list(seedDir.resolve("e2e"))) {
      for (Path f : files.filter(p -> p.toString().endsWith(".sql")).sorted().toList()) {
        sql.append(Files.readString(f).replace(":'t0'", "'" + t0 + "'")).append('\n');
      }
    }
    sql.append("COMMIT;\n");
    try (var c = dataSource.getConnection();
        var st = c.createStatement()) {
      st.execute(sql.toString());
    } catch (java.sql.SQLException e) {
      throw new IllegalStateException("nạp seed vàng thất bại", e);
    }
  }

  protected String token(UUID org, UUID user, Role role) {
    return tokens.issue(new AuthPrincipal(org, user, role));
  }

  protected String an() {
    return token(ORG_A, AN, Role.STUDENT);
  }

  protected String teacherA() {
    return token(ORG_A, TEACHER_A, Role.TEACHER);
  }

  protected String teacherC() {
    return token(ORG_C, TEACHER_C, Role.TEACHER);
  }

  protected HttpResponse<String> postJson(
      String path, String bearer, String body, String... headers)
      throws IOException, InterruptedException {
    HttpRequest.Builder b =
        HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body));
    if (bearer != null) {
      b.header("Authorization", "Bearer " + bearer);
    }
    for (int i = 0; i + 1 < headers.length; i += 2) {
      b.header(headers[i], headers[i + 1]);
    }
    return http.send(b.build(), HttpResponse.BodyHandlers.ofString());
  }

  protected long count(String sql) {
    return jdbc.sql(sql).query(Long.class).single();
  }
}
