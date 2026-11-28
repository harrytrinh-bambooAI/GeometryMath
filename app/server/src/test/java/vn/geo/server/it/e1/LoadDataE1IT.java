package vn.geo.server.it.e1;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import tools.jackson.databind.JsonNode;
import vn.geo.server.assignment.internal.repo.AssignmentRepository;
import vn.geo.server.common.db.TenantJdbc;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.RequestContext;
import vn.geo.server.common.security.Role;
import vn.geo.server.iam.internal.repo.AuthRepository;
import vn.geo.server.tracking.internal.repo.TrackingRepository;

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
      String joinCode;
      String nameKey;
      String displayName;
      long qaId;
      try (Statement st = c.createStatement();
          ResultSet rs =
              st.executeQuery(
                  """
                  SELECT cm.org_id, cm.user_id, cm.class_id, a.id, cl.teacher_id, cl.join_code, cm.name_key,
                         u.display_name,
                         (SELECT coalesce(max(q.id), 0) FROM qa_turn q WHERE q.org_id = cm.org_id AND q.user_id = cm.user_id)
                  FROM class_member cm JOIN class cl ON cl.id = cm.class_id
                  JOIN app_user u ON u.id = cm.user_id
                  JOIN assignment a ON a.class_id = cm.class_id
                  WHERE cm.left_at IS NULL
                  ORDER BY cm.user_id LIMIT 1
                  """)) {
        rs.next();
        org = rs.getString(1);
        student = rs.getString(2);
        klass = rs.getString(3);
        assignment = rs.getString(4);
        teacher = rs.getString(5);
        joinCode = rs.getString(6);
        nameKey = rs.getString(7);
        displayName = rs.getString(8);
        qaId = rs.getLong(9);
      }
      UUID orgId = UUID.fromString(org);
      UUID studentId = UUID.fromString(student);
      UUID classId = UUID.fromString(klass);
      UUID assignmentId = UUID.fromString(assignment);
      UUID teacherId = UUID.fromString(teacher);

      // Gọi đúng repository của mã E1 (SQL thật, kể cả câu lệnh bên trong hàm plpgsql) trên
      // geo_bench.
      // auto_explain ghi kế hoạch thực thi (ANALYZE, JSON) của mọi câu lệnh, kể cả lồng trong hàm,
      // thành NOTICE gửi về đúng câu lệnh JDBC vừa chạy.
      PlanCollector plans = new PlanCollector();
      try (Connection traced = bench()) {
        try (Statement st = traced.createStatement()) {
          st.execute("LOAD 'auto_explain'");
          st.execute("SET auto_explain.log_min_duration = 0");
          st.execute("SET auto_explain.log_analyze = on");
          st.execute("SET auto_explain.log_buffers = on");
          st.execute("SET auto_explain.log_format = 'json'");
          st.execute("SET auto_explain.log_nested_statements = on");
          st.execute("SET auto_explain.log_level = 'notice'");
          st.execute("SET client_min_messages = 'notice'");
        }
        DataSource ds =
            ProxyDataSourceBuilder.create(new SingleConnectionDataSource(traced, true))
                .listener(plans)
                .build();
        JdbcClient jdbcClient = JdbcClient.create(ds);
        TenantJdbc tenant = new TenantJdbc(jdbcClient);
        AssignmentRepository assignments = new AssignmentRepository(tenant);
        TrackingRepository tracking = new TrackingRepository(tenant);
        AuthRepository auth = new AuthRepository(jdbcClient, tenant);

        RequestContext.callAs(
            new AuthPrincipal(orgId, studentId, Role.STUDENT),
            () -> {
              plans.label("Q02 bài của em (studentPage)");
              assignments.studentPage(studentId, null, 21);
              plans.label("fn_qa_answer (answer)");
              tracking.answer(studentId, qaId);
              return null;
            });
        RequestContext.callAs(
            new AuthPrincipal(orgId, teacherId, Role.TEACHER),
            () -> {
              plans.label("Q03 bài của lớp (classPage)");
              assignments.classPage(classId, null, 21);
              plans.label("isTeacherOfClass");
              assertThat(assignments.isTeacherOfClass(teacherId, classId)).isTrue();
              plans.label("monitorHeader");
              assertThat(tracking.monitorHeader(teacherId, assignmentId)).isPresent();
              plans.label("Q09 theo dõi lớp (monitorStudents)");
              assertThat(tracking.monitorStudents(assignmentId)).isNotEmpty();
              plans.label("Q14 câu hỏi của lớp (monitorQuestions)");
              tracking.monitorQuestions(assignmentId);
              plans.label("Q17 lớp của giáo viên (teacherClasses)");
              assertThat(auth.teacherClasses(teacherId)).isNotEmpty();
              return null;
            });
        // đăng nhập: trước khi biết trường (không cần ngữ cảnh)
        plans.label("Q01 fn_student_login (tên đã có trong lớp)");
        assertThat(auth.studentLogin(joinCode, nameKey, displayName, Uuid7.next()).userId())
            .isEqualTo(studentId);
        plans.label("Q24 fn_teacher_login (mã sai)");
        auth.teacherLogin("ZZZZ", new byte[32], new byte[32]);
      }
      // Q20 dòng thời gian: E1 chưa có repository đọc (thuộc E3); giữ truy vấn tham chiếu của danh
      // mục.
      plans.explainRaw(
          c,
          "Q20 dòng thời gian (danh mục, chưa có repo)",
          """
          SELECT occurred_at, kind FROM event_log
          WHERE org_id = '%1$s' AND assignment_id = '%3$s' AND user_id = '%2$s'
          ORDER BY occurred_at DESC, id DESC LIMIT 50
          """
              .formatted(org, student, assignment));

      assertThat(plans.labelsWithPlans())
          .as("mỗi lời gọi repository có kế hoạch thực thi")
          .contains(
              "Q02 bài của em (studentPage)",
              "fn_qa_answer (answer)",
              "Q03 bài của lớp (classPage)",
              "isTeacherOfClass",
              "monitorHeader",
              "Q09 theo dõi lớp (monitorStudents)",
              "Q14 câu hỏi của lớp (monitorQuestions)",
              "Q17 lớp của giáo viên (teacherClasses)",
              "Q01 fn_student_login (tên đã có trong lớp)",
              "Q24 fn_teacher_login (mã sai)",
              "Q20 dòng thời gian (danh mục, chưa có repo)");
      List<String> problems = new ArrayList<>();
      for (PlanCollector.Captured p : plans.captured) {
        JsonNode root = mapper.readTree(p.json());
        JsonNode plan = root.isArray() ? root.get(0).get("Plan") : root.get("Plan");
        inspect(c, p.label(), plan, problems);
      }
      assertThat(problems).as("kế hoạch quét tuần tự / lọc mã trường sau quét").isEmpty();
    }
  }

  /** Thu kế hoạch auto_explain (NOTICE) của từng câu lệnh JDBC, gắn nhãn lời gọi repository. */
  static final class PlanCollector implements QueryExecutionListener {
    record Captured(String label, String json) {}

    final List<Captured> captured = new ArrayList<>();
    private String label = "?";

    void label(String l) {
      label = l;
    }

    java.util.Set<String> labelsWithPlans() {
      java.util.Set<String> s = new java.util.LinkedHashSet<>();
      captured.forEach(x -> s.add(x.label()));
      return s;
    }

    @Override
    public void beforeQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {}

    @Override
    public void afterQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
      try {
        for (SQLWarning w = execInfo.getStatement().getWarnings();
            w != null;
            w = w.getNextWarning()) {
          add(w.getMessage());
        }
      } catch (SQLException e) {
        throw new IllegalStateException(e);
      }
    }

    private void add(String msg) {
      int i = msg == null ? -1 : msg.indexOf('{');
      if (i >= 0 && msg.contains("\"Plan\"")) {
        captured.add(new Captured(label, msg.substring(i)));
      }
    }

    void explainRaw(Connection c, String l, String sql) throws SQLException {
      try (Statement st = c.createStatement();
          ResultSet rs = st.executeQuery("EXPLAIN (ANALYZE, BUFFERS, FORMAT JSON) " + sql)) {
        rs.next();
        captured.add(new Captured(l, rs.getString(1)));
      }
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
