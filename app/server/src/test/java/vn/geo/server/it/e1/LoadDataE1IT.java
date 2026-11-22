package vn.geo.server.it.e1;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import tools.jackson.databind.JsonNode;

/**
 * US-GEO-108 (dữ liệu thử tải bằng một lệnh) và TC-GEO-0802 (truy vấn nóng không quét tuần tự bảng
 * lớn). Chạy đúng lệnh {@code scripts/bench-seed.sh} trên CSDL thử tải riêng geo_bench của cụm đang
 * dùng.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LoadDataE1IT extends AbstractE1IT {

  private static Map<String, Long> firstRun;

  private static String port() {
    return System.getenv().getOrDefault("GEO_PG_PORT", "54329");
  }

  static Map<String, Long> runSeedCommand() throws IOException, InterruptedException {
    Process p =
        new ProcessBuilder("bash", "../scripts/bench-seed.sh", "geo_bench")
            .redirectErrorStream(true)
            .start();
    String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    assertThat(p.waitFor()).as(out).isZero();
    Map<String, Long> counts = new LinkedHashMap<>();
    for (String line : out.split("\n")) {
      String[] kv = line.strip().split(" ");
      if (kv.length == 2 && kv[1].matches("\\d+")) {
        counts.put(kv[0], Long.parseLong(kv[1]));
      }
    }
    return counts;
  }

  static Connection bench() throws SQLException {
    return DriverManager.getConnection(
        "jdbc:postgresql://127.0.0.1:" + port() + "/geo_bench", "geo", "");
  }

  static long scalar(Connection c, String sql) throws SQLException {
    try (Statement st = c.createStatement();
        ResultSet rs = st.executeQuery(sql)) {
      rs.next();
      return rs.getLong(1);
    }
  }

  @Test
  @Order(1)
  @DisplayName("TC-GEO-0156 Sinh đủ quy mô bằng một lệnh (US-GEO-108-SC1)")
  void tc0156_fullScaleOneCommand() throws Exception {
    reseed();
    firstRun = runSeedCommand();
    assertThat(firstRun.get("schools")).isEqualTo(50);
    assertThat(firstRun.get("classes")).isEqualTo(600);
    assertThat(firstRun.get("students")).isEqualTo(20_000);
    assertThat(firstRun.get("events")).isBetween(1_900_000L, 2_100_000L);
  }

  @Test
  @Order(2)
  @DisplayName("TC-GEO-0157 Phân bố theo công thức (US-GEO-108-SC2)")
  void tc0157_distribution() throws Exception {
    try (Connection c = bench()) {
      double classesPerSchool =
          scalar(c, "SELECT count(*) FROM class")
              / (double) scalar(c, "SELECT count(*) FROM org WHERE kind = 'school'");
      double studentsPerSchool =
          scalar(c, "SELECT count(*) FROM app_user WHERE role = 'student'") / 50.0;
      double studentsPerClass =
          scalar(c, "SELECT count(*) FROM class_member")
              / (double) scalar(c, "SELECT count(*) FROM class");
      double eventsPerStudent =
          scalar(c, "SELECT count(*) FROM event_log")
              / (double) scalar(c, "SELECT count(*) FROM app_user WHERE role = 'student'");
      assertThat(classesPerSchool).isBetween(12 * 0.95, 12 * 1.05);
      assertThat(studentsPerSchool).isBetween(400 * 0.95, 400 * 1.05);
      assertThat(studentsPerClass).isBetween(33.3 * 0.95, 33.3 * 1.05);
      assertThat(eventsPerStudent).isBetween(100 * 0.95, 100 * 1.05);
      // mỗi trường đúng 12 lớp, mỗi lớp 33–34 em
      assertThat(
              scalar(
                  c,
                  "SELECT count(*) FROM (SELECT org_id FROM class GROUP BY org_id HAVING count(*) <> 12) x"))
          .isZero();
      assertThat(
              scalar(
                  c,
                  "SELECT count(*) FROM (SELECT class_id FROM class_member GROUP BY class_id HAVING count(*) NOT BETWEEN 33 AND 34) x"))
          .isZero();
    }
  }

  @Test
  @Order(3)
  @DisplayName("TC-GEO-0802 Truy vấn nóng không quét tuần tự bảng lớn (NFR-GEO-042)")
  void tc0802_hotQueriesUseIndexes() throws Exception {
    try (Connection c = bench()) {
      String org;
      String student;
      String klass;
      String assignment;
      String teacher;
      try (Statement st = c.createStatement();
          ResultSet rs =
              st.executeQuery(
                  """
                  SELECT cm.org_id, cm.user_id, cm.class_id, a.id, cl.teacher_id
                  FROM class_member cm JOIN class cl ON cl.id = cm.class_id
                  JOIN assignment a ON a.class_id = cm.class_id
                  ORDER BY cm.user_id LIMIT 1
                  """)) {
        rs.next();
        org = rs.getString(1);
        student = rs.getString(2);
        klass = rs.getString(3);
        assignment = rs.getString(4);
        teacher = rs.getString(5);
      }
      // Truy vấn nóng của mã E1 (cùng SQL với repo, tham số gắn sẵn)
      Map<String, String> hot = new LinkedHashMap<>();
      hot.put(
          "Q02 bài của em",
          """
          SELECT a.id, p.code, pv.statement, c.name, a.release_at, a.release_rev, a.allow_hint, a.allow_qa,
                 ap.first_opened_at IS NOT NULL, COALESCE(ap.hint_level_max, 0)
          FROM class_member cm
          JOIN class c ON c.org_id = '%1$s' AND c.id = cm.class_id
          JOIN assignment a ON a.org_id = '%1$s' AND a.class_id = cm.class_id AND a.kind = 'class' AND a.archived_at IS NULL
          JOIN problem_version pv ON pv.org_id = a.content_org_id AND pv.id = a.problem_version_id
          JOIN problem p ON p.org_id = pv.org_id AND p.id = pv.problem_id
          LEFT JOIN assignment_progress ap ON ap.org_id = '%1$s' AND ap.assignment_id = a.id AND ap.user_id = cm.user_id
          WHERE cm.org_id = '%1$s' AND cm.user_id = '%2$s' AND cm.left_at IS NULL
          ORDER BY a.id DESC LIMIT 21
          """);
      hot.put(
          "Q09 theo dõi lớp",
          """
          SELECT a.id, u.id, u.display_name, ap.first_opened_at IS NOT NULL, COALESCE(ap.hint_level_max, 0)
          FROM assignment a
          JOIN class_member cm ON cm.org_id = '%1$s' AND cm.class_id = a.class_id AND cm.left_at IS NULL
          JOIN app_user u ON u.org_id = '%1$s' AND u.id = cm.user_id
          LEFT JOIN assignment_progress ap ON ap.org_id = '%1$s' AND ap.assignment_id = a.id AND ap.user_id = cm.user_id
          WHERE a.org_id = '%1$s' AND a.id = '%3$s'
          ORDER BY cm.name_key, cm.user_id LIMIT 101
          """);
      hot.put(
          "Q14 câu hỏi của lớp",
          """
          SELECT q.id, q.question, q.asked_at, u.display_name
          FROM qa_turn q JOIN app_user u ON u.org_id = '%1$s' AND u.id = q.user_id
          WHERE q.org_id = '%1$s' AND q.assignment_id = '%3$s' ORDER BY q.id DESC LIMIT 20
          """);
      hot.put(
          "Q03 bài của lớp",
          """
          SELECT a.id, p.code, a.release_at FROM assignment a
          JOIN problem p ON p.org_id = a.content_org_id AND p.id = a.problem_id
          WHERE a.org_id = '%1$s' AND a.class_id = '%4$s' AND a.kind = 'class' AND a.archived_at IS NULL
          ORDER BY a.id DESC LIMIT 21
          """);
      hot.put(
          "Q17 lớp của giáo viên",
          "SELECT id, name FROM class WHERE org_id = '%1$s' AND teacher_id = '%5$s' AND archived_at IS NULL ORDER BY name, id LIMIT 100");
      hot.put(
          "Q20 dòng thời gian",
          """
          SELECT occurred_at, kind FROM event_log
          WHERE org_id = '%1$s' AND assignment_id = '%3$s' AND user_id = '%2$s'
          ORDER BY occurred_at DESC, id DESC LIMIT 50
          """);
      List<String> problems = new ArrayList<>();
      for (var e : hot.entrySet()) {
        String sql = e.getValue().formatted(org, student, assignment, klass, teacher);
        String planJson;
        try (Statement st = c.createStatement();
            ResultSet rs = st.executeQuery("EXPLAIN (ANALYZE, BUFFERS, FORMAT JSON) " + sql)) {
          rs.next();
          planJson = rs.getString(1);
        }
        JsonNode plan = mapper.readTree(planJson).get(0).get("Plan");
        inspect(c, e.getKey(), plan, problems);
      }
      assertThat(problems).as("kế hoạch quét tuần tự / lọc mã trường sau quét").isEmpty();
    }
  }

  private void inspect(Connection c, String q, JsonNode node, List<String> problems)
      throws SQLException {
    String type = node.path("Node Type").asString("");
    String rel = node.path("Relation Name").asString("");
    if (!rel.isEmpty()) {
      long rows =
          scalar(c, "SELECT reltuples::bigint FROM pg_class WHERE oid = '" + rel + "'::regclass");
      if ("Seq Scan".equals(type) && rows > 10_000) {
        problems.add(q + ": Seq Scan trên " + rel + " (" + rows + " hàng)");
      }
      if (rows > 10_000
          && node.path("Filter").asString("").contains("org_id")
          && node.path("Rows Removed by Filter").asLong(0) > 0) {
        // tra theo khóa chính rồi kiểm org_id (0 hàng bị loại) không phải "lọc sau khi quét"
        problems.add(q + ": lọc org_id sau khi quét " + rel);
      }
    }
    for (JsonNode child : node.path("Plans")) {
      inspect(c, q, child, problems);
    }
  }

  @Test
  @Order(4)
  @DisplayName("TC-GEO-0158 Chạy lại cho cùng quy mô (US-GEO-108-SC3)")
  void tc0158_rerunSameScale() throws Exception {
    Map<String, Long> second = runSeedCommand();
    assertThat(second.get("schools")).isEqualTo(firstRun.get("schools"));
    assertThat(second.get("classes")).isEqualTo(firstRun.get("classes"));
    assertThat(second.get("students")).isEqualTo(firstRun.get("students"));
    assertThat(second.get("events")).isBetween(1_900_000L, 2_100_000L);
  }

  @Test
  @Order(5)
  @DisplayName("TC-GEO-0159 Không lẫn vào dữ liệu mẫu (US-GEO-108-FC1)")
  void tc0159_notMixedIntoSampleData() throws Exception {
    // dữ liệu mẫu (geo_test, nạp seed vàng trước lệnh sinh) không đổi
    assertThat(count("SELECT count(*) FROM org WHERE kind = 'school'")).isEqualTo(2);
    assertThat(count("SELECT count(*) FROM class WHERE org_id = '" + ORG_A + "'")).isEqualTo(2);
    assertThat(
            count(
                "SELECT count(*) FROM app_user WHERE org_id = '"
                    + ORG_A
                    + "' AND role = 'student'"))
        .isEqualTo(10);
    // lệnh từ chối ghi vào CSDL không phải thử tải
    Process p =
        new ProcessBuilder("bash", "../scripts/bench-seed.sh", "geo_test")
            .redirectErrorStream(true)
            .start();
    String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    assertThat(p.waitFor()).isEqualTo(2);
    assertThat(out).contains("chỉ ghi vào CSDL geo_bench");
    assertThat(count("SELECT count(*) FROM org WHERE kind = 'school'")).isEqualTo(2);
  }

  @Test
  @Order(6)
  @DisplayName("TC-GEO-0160 Tên sinh ra không phải tên thật (US-GEO-108-FC2)")
  void tc0160_numberedNames() throws Exception {
    try (Connection c = bench()) {
      assertThat(
              scalar(
                  c,
                  "SELECT count(*) FROM org WHERE kind = 'school' AND name !~ '^Trường thử [0-9]{2}$'"))
          .isZero();
      assertThat(
              scalar(
                  c,
                  "SELECT count(*) FROM app_user WHERE role = 'student' AND display_name !~ '^Học sinh [0-9]{5}$'"))
          .isZero();
      assertThat(scalar(c, "SELECT count(*) FROM org WHERE name = 'Trường thử 01'")).isEqualTo(1);
      assertThat(scalar(c, "SELECT count(*) FROM app_user WHERE display_name = 'Học sinh 00001'"))
          .isEqualTo(1);
    }
  }
}
