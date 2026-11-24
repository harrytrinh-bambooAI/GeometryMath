package vn.geo.server.it.e7;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.sql.DataSource;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import tools.jackson.databind.JsonNode;
import vn.geo.server.assignment.internal.repo.AssignmentRepository;
import vn.geo.server.common.db.TenantJdbc;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.RequestContext;
import vn.geo.server.common.security.Role;
import vn.geo.server.content.internal.repo.CatalogRepository;
import vn.geo.server.it.e1.AbstractE1IT;
import vn.geo.server.tracking.internal.repo.MonitorRepository;
import vn.geo.server.tracking.internal.repo.TrackingRepository;

/**
 * TC-GEO-0735 (US-GEO-704-SC4): kế hoạch thực thi (auto_explain, kể cả câu lệnh trong hàm) của các
 * truy vấn nóng trên dữ liệu thử tải 2 000 000 sự kiện (geo_bench, scripts/bench-seed.sh): không
 * quét toàn bộ bảng hơn 10 000 dòng; đọc sự kiện chỉ chạm partition trong cận thời gian.
 */
class PlansE7IT extends AbstractE1IT {

  private static String pgPort() {
    return System.getenv().getOrDefault("GEO_PG_PORT", "54329");
  }

  private static Connection bench() throws SQLException {
    return DriverManager.getConnection(
        "jdbc:postgresql://127.0.0.1:" + pgPort() + "/geo_bench", "geo", "");
  }

  private static long scalar(Connection c, String sql) throws SQLException {
    try (Statement st = c.createStatement();
        ResultSet rs = st.executeQuery(sql)) {
      rs.next();
      return rs.getLong(1);
    }
  }

  /** geo_bench đủ 2 000 000 sự kiện; chưa có thì chạy đúng lệnh của dự án. */
  private static void ensureBench() throws Exception {
    try (Connection c = bench()) {
      if (scalar(c, "SELECT count(*) FROM event_log") >= 2_000_000) {
        return;
      }
    } catch (SQLException missing) {
      // chưa có CSDL thử tải
    }
    Process p =
        new ProcessBuilder("bash", "../scripts/bench-seed.sh", "geo_bench")
            .redirectErrorStream(true)
            .start();
    String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    assertThat(p.waitFor()).as(out).isZero();
  }

  @Test
  @DisplayName("TC-GEO-0735 Truy vấn nóng không quét toàn bảng (US-GEO-704-SC4)")
  void tc0735_hotQueriesNoFullScan() throws Exception {
    ensureBench();
    try (Connection c = bench()) {
      assertThat(scalar(c, "SELECT count(*) FROM event_log")).isGreaterThanOrEqualTo(2_000_000);
      String[] k = new String[5];
      try (Statement st = c.createStatement();
          ResultSet rs =
              st.executeQuery(
                  """
                  SELECT cm.org_id, cm.user_id, a.id, cl.teacher_id,
                         (SELECT pv.id FROM problem_version pv WHERE pv.org_id = platform_org_id() LIMIT 1)
                  FROM class_member cm JOIN class cl ON cl.id = cm.class_id
                  JOIN assignment a ON a.class_id = cm.class_id
                  WHERE cm.left_at IS NULL ORDER BY cm.user_id LIMIT 1
                  """)) {
        rs.next();
        for (int i = 0; i < 5; i++) {
          k[i] = rs.getString(i + 1);
        }
      }
      UUID org = UUID.fromString(k[0]);
      UUID student = UUID.fromString(k[1]);
      UUID asg = UUID.fromString(k[2]);
      UUID teacher = UUID.fromString(k[3]);
      UUID version = UUID.fromString(k[4]);
      Instant now = Instant.now();
      Instant from = now.minusSeconds(3600L * 24 * 20);
      String inBound =
          "event_log_y"
              + new java.text.SimpleDateFormat("yyyy'm'MM").format(java.util.Date.from(now));
      Plans plans = new Plans();
      try (Connection traced = bench()) {
        try (Statement st = traced.createStatement()) {
          for (String s :
              List.of(
                  "LOAD 'auto_explain'",
                  "SET auto_explain.log_min_duration = 0",
                  "SET auto_explain.log_analyze = on",
                  "SET auto_explain.log_format = 'json'",
                  "SET auto_explain.log_nested_statements = on",
                  "SET auto_explain.log_level = 'notice'",
                  "SET client_min_messages = 'notice'")) {
            st.execute(s);
          }
        }
        DataSource ds =
            ProxyDataSourceBuilder.create(new SingleConnectionDataSource(traced, true))
                .listener(plans)
                .build();
        TenantJdbc tenant = new TenantJdbc(JdbcClient.create(ds));
        AssignmentRepository assignments = new AssignmentRepository(tenant);
        TrackingRepository tracking = new TrackingRepository(tenant);
        MonitorRepository monitor = new MonitorRepository(tenant);
        CatalogRepository catalog = new CatalogRepository(tenant);
        RequestContext.callAs(
            new AuthPrincipal(org, student, Role.STUDENT),
            () -> {
              plans.label = "Bài của em (studentPage)";
              assignments.studentPage(student, null, 21);
              plans.label = "Làm bài (studentOne)";
              assignments.studentOne(student, asg);
              plans.label = "Yêu cầu lời giải (fn_get_solution)";
              assignments.solution(student, asg);
              return null;
            });
        RequestContext.callAs(
            new AuthPrincipal(org, teacher, Role.TEACHER),
            () -> {
              plans.label = "Theo dõi lớp: đầu trang";
              assertThat(monitor.header(teacher, asg)).isPresent();
              plans.label = "Theo dõi lớp: học sinh";
              assertThat(tracking.monitorStudents(asg)).isNotEmpty();
              plans.label = "Theo dõi lớp: câu hỏi";
              monitor.questions(asg);
              plans.label = "Theo dõi: bài đã giao";
              assertThat(monitor.assigned(teacher, null, 21)).isNotEmpty();
              plans.label = "Kho đề (page)";
              catalog.page(new CatalogRepository.Filter(null, null, null), null, null, 21);
              plans.label = "Kho đề (detail)";
              catalog.detail(version);
              plans.label = "Đọc sự kiện";
              monitor.events(asg, student, from, now.plusSeconds(1), null, null, 51);
              return null;
            });
      }
      assertThat(plans.labels())
          .contains(
              "Bài của em (studentPage)",
              "Yêu cầu lời giải (fn_get_solution)",
              "Theo dõi lớp: học sinh",
              "Theo dõi: bài đã giao",
              "Kho đề (page)",
              "Đọc sự kiện");
      List<String> problems = new ArrayList<>();
      Set<String> eventRelations = new LinkedHashSet<>();
      for (String[] p : plans.captured) {
        JsonNode root = mapper.readTree(p[1]);
        JsonNode plan = root.isArray() ? root.get(0).get("Plan") : root.get("Plan");
        inspect(c, p[0], plan, problems, eventRelations);
      }
      assertThat(problems).as("quét toàn bảng lớn").isEmpty();
      // đọc sự kiện: chỉ partition trong cận [from, to) (tháng này, có thể cả tháng trước)
      String prev =
          "event_log_y"
              + new java.text.SimpleDateFormat("yyyy'm'MM").format(java.util.Date.from(from));
      assertThat(eventRelations).isNotEmpty().allMatch(r -> r.equals(inBound) || r.equals(prev));
    }
  }

  private static void inspect(
      Connection c, String label, JsonNode node, List<String> problems, Set<String> eventRels)
      throws SQLException {
    String type = node.path("Node Type").asString("");
    String rel = node.path("Relation Name").asString("");
    if (!rel.isEmpty()) {
      long rows =
          scalar(c, "SELECT reltuples::bigint FROM pg_class WHERE oid = '" + rel + "'::regclass");
      if ("Seq Scan".equals(type) && rows > 10_000) {
        problems.add(label + ": Seq Scan trên " + rel + " (" + rows + " hàng)");
      }
      if ("Đọc sự kiện".equals(label) && rel.startsWith("event_log")) {
        eventRels.add(rel);
      }
    }
    for (JsonNode child : node.path("Plans")) {
      inspect(c, label, child, problems, eventRels);
    }
  }

  /** Thu kế hoạch auto_explain (NOTICE) của từng câu lệnh JDBC, gắn nhãn lời gọi repository. */
  static final class Plans implements QueryExecutionListener {
    final List<String[]> captured = new ArrayList<>();
    String label = "?";

    Set<String> labels() {
      Set<String> s = new LinkedHashSet<>();
      captured.forEach(x -> s.add(x[0]));
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
          String msg = w.getMessage();
          int i = msg == null ? -1 : msg.indexOf('{');
          if (i >= 0 && msg.contains("\"Plan\"")) {
            captured.add(new String[] {label, msg.substring(i)});
          }
        }
      } catch (SQLException e) {
        throw new IllegalStateException(e);
      }
    }
  }
}
