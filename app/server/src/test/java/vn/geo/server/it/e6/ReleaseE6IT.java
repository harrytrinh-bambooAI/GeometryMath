package vn.geo.server.it.e6;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import vn.geo.server.assignment.internal.service.E6SandboxService;
import vn.geo.server.it.e1.AbstractE1IT;

/**
 * IT của E6: biên giờ mở tính ở CSDL theo now() (D-GEO-041, 169; QF-E6-03), nhật ký đổi giờ
 * (BR-GEO-240), quyền của vai CSDL ứng dụng geo_app (D-GEO-014, 158). Ca đổi dữ liệu dùng lớp riêng
 * của móc E6 hoặc giao dịch ROLLBACK.
 */
class ReleaseE6IT extends AbstractE1IT {

  static final UUID KHANH = id(25001);
  static final UUID BG3 = id(42);

  @Autowired E6SandboxService sandbox;

  /**
   * Trong MỘT giao dịch: đặt giờ mở BG-4 = now() + giây rồi hỏi cổng lời giải với cùng now(); luôn
   * ROLLBACK.
   */
  private String gateAt(double seconds) throws SQLException {
    try (Connection c = dataSource.getConnection()) {
      c.setAutoCommit(false);
      try (Statement st = c.createStatement()) {
        st.execute(
            "UPDATE assignment SET release_at = now() + make_interval(secs => "
                + seconds
                + ") WHERE id = '"
                + BG4
                + "'");
        var rs =
            st.executeQuery(
                "SELECT status, final_answer FROM fn_get_solution('"
                    + ORG_A
                    + "', '"
                    + KHANH
                    + "', '"
                    + BG4
                    + "')");
        rs.next();
        String s = rs.getString(1) + "|" + rs.getString(2);
        c.rollback();
        return s;
      }
    }
  }

  @Test
  @DisplayName("TC-GEO-0657 Đúng giờ mở thì mở (t = R)")
  void tc0657() throws Exception {
    assertThat(gateAt(0)).isEqualTo("OK|Sxq = 15π cm²; V = 12π cm³");
  }

  @Test
  @DisplayName("TC-GEO-0661 Một giây trước giờ mở vẫn khóa (t = R − 1 giây)")
  void tc0661() throws Exception {
    assertThat(gateAt(1)).isEqualTo("LOCKED|null");
  }

  @Test
  @DisplayName("TC-GEO-0695 Một giây sau giờ mở thì vẫn mở (t = R + 1 giây)")
  void tc0695() throws Exception {
    assertThat(gateAt(-1)).startsWith("OK|");
  }

  private record Sb(String teacher, String student, UUID assignment) {}

  private Sb sb(String release) {
    var out =
        sandbox.create(
            List.of(
                new E6SandboxService.ClassSpec(
                    "11A1",
                    "thpt",
                    List.of("An"),
                    List.of(
                        new E6SandboxService.AssignmentSpec(
                            "BG-1", "HKG-01", release, true, true)))));
    var c = out.classes().getFirst();
    return new Sb(
        out.teacher().token(), c.students().get("An").token(), c.assignments().get("BG-1"));
  }

  private int rev(Sb s) throws Exception {
    return json(get("/api/v1/teacher/assignments/" + s.assignment() + "/release", s.teacher()))
        .get("releaseRev")
        .asInt();
  }

  @Test
  @DisplayName("TC-GEO-0634 Ghi nhật ký đổi giờ: đúng một dòng mới, người đổi, giờ cũ, giờ mới")
  void tc0634() throws Exception {
    Sb s = sb("upcoming");
    String path = "/api/v1/teacher/assignments/" + s.assignment();
    Instant old =
        Instant.parse(json(get(path + "/release", s.teacher())).get("releaseAt").asString());
    Instant neu = old.plusSeconds(3600);
    var r =
        postJson(
            path + "/release",
            s.teacher(),
            "{\"releaseAt\":\"" + neu + "\",\"expectedRev\":" + rev(s) + "}");
    assertThat(r.statusCode()).isEqualTo(200);
    var items = json(get(path + "/release-log", s.teacher())).get("items");
    assertThat(items.size()).isEqualTo(1);
    assertThat(items.get(0).get("actorName").asString()).isEqualTo("Giáo viên mẫu");
    assertThat(items.get(0).get("action").asString()).isEqualTo("release_change");
    assertThat(Instant.parse(items.get(0).get("before").asString())).isEqualTo(old);
    assertThat(Instant.parse(items.get(0).get("after").asString())).isEqualTo(neu);
    assertThat(items.get(0).get("at").asString()).isNotBlank();
  }

  @Test
  @DisplayName("TC-GEO-0868 Đổi sang giờ chỉ sau giờ máy chủ một giây")
  void tc0868() throws Exception {
    Sb s = sb("upcoming");
    String path = "/api/v1/teacher/assignments/" + s.assignment();
    Instant t1 = jdbc.sql("SELECT now()").query(OffsetDateTime.class).single().toInstant();
    var r =
        postJson(
            path + "/release",
            s.teacher(),
            "{\"releaseAt\":\"" + t1.plusMillis(1500) + "\",\"expectedRev\":" + rev(s) + "}");
    assertThat(r.statusCode()).as(r.body()).isEqualTo(200);
    String sol = "/api/v1/student/assignments/" + s.assignment() + "/solution";
    assertThat(get(sol, s.student()).statusCode()).isEqualTo(403);
    Thread.sleep(1700);
    var ok = get(sol, s.student());
    assertThat(ok.statusCode()).isEqualTo(200);
    assertThat(ok.body()).contains("45°");
  }

  /**
   * Chạy từng câu dưới vai geo_app, mỗi câu trong một savepoint; trả SQLSTATE (null = thành công).
   * Cuối cùng ROLLBACK.
   */
  private List<String> asApp(String... sqls) throws SQLException {
    try (Connection c = dataSource.getConnection()) {
      c.setAutoCommit(false);
      List<String> out = new java.util.ArrayList<>();
      try (Statement st = c.createStatement()) {
        st.execute("SET LOCAL ROLE geo_app");
        for (String sql : sqls) {
          Savepoint sp = c.setSavepoint();
          try {
            st.execute(sql);
            out.add(null);
          } catch (SQLException e) {
            out.add(e.getSQLState());
            c.rollback(sp);
          }
        }
      } finally {
        c.rollback();
      }
      return out;
    }
  }

  @Test
  @DisplayName("TC-GEO-0666 Đường đọc thông thường không tới được nơi lưu lời giải")
  void tc0666() throws Exception {
    assertThat(asApp("SELECT * FROM solution LIMIT 1", "SELECT * FROM verification_report LIMIT 1"))
        .containsExactly("42501", "42501");
    List<Map<String, Object>> cols =
        jdbc.sql(
                """
                SELECT c.table_name, c.column_name FROM information_schema.columns c
                WHERE c.table_schema = 'public' AND has_column_privilege('geo_app', format('public.%I', c.table_name),
                      c.column_name, 'SELECT')
                  AND c.column_name IN ('final_answer', 'steps', 'answer_forms', 'reply_text', 'values_json')
                """)
            .query()
            .listOfRows();
    assertThat(cols).isEmpty();
  }

  @Test
  @DisplayName(
      "TC-GEO-0863 Vai CSDL của ứng dụng không tự ghi được giờ mở, tiến độ, nhãn máy, tài khoản giáo viên")
  void tc0863() throws Exception {
    long auditBefore = count("SELECT count(*) FROM audit_log");
    List<String> st =
        asApp(
            "INSERT INTO assignment (id, org_id, kind, class_id, content_org_id, problem_id, problem_version_id, assigned_by,"
                + " release_at) SELECT gen_random_uuid(), org_id, kind, class_id, content_org_id, problem_id, problem_version_id,"
                + " assigned_by, now() - interval '1 hour' FROM assignment WHERE id = '"
                + BG1
                + "'",
            "INSERT INTO app_user (id, org_id, role, account_kind, display_name) VALUES (gen_random_uuid(), '"
                + ORG_A
                + "', 'teacher', 'sample', 'GV giả')",
            "UPDATE problem_version SET verify_status = 'verified' WHERE id = '" + HKG01_V + "'",
            "UPDATE assignment_progress SET hint_level_max = 2 WHERE assignment_id = '"
                + BG1
                + "' AND user_id = '"
                + AN
                + "'",
            "INSERT INTO problem (id, org_id, subject_id, source, level, solid_kind, topic_id, title, author_user_id)"
                + " SELECT gen_random_uuid(), org_id, subject_id, source, level, solid_kind, topic_id, 'giả', author_user_id"
                + " FROM problem WHERE id = '"
                + id(31)
                + "'");
    assertThat(st).containsOnly("42501");
    assertThat(count("SELECT count(*) FROM audit_log")).isEqualTo(auditBefore);
    // đối chứng: tạo bài giao qua hàm có giờ mở tương lai thì thành công và có một dòng audit_log
    try (Connection c = dataSource.getConnection()) {
      c.setAutoCommit(false);
      try (Statement s = c.createStatement()) {
        s.execute("SET LOCAL ROLE geo_app");
        var rs =
            s.executeQuery(
                "SELECT status FROM fn_create_assignment('"
                    + ORG_A
                    + "', '"
                    + TEACHER_A
                    + "', gen_random_uuid(), '"
                    + id(11)
                    + "', '"
                    + id(100036)
                    + "', now() + interval '1 day', true, true, NULL)");
        rs.next();
        assertThat(rs.getString(1)).isEqualTo("OK");
        var n = s.executeQuery("SELECT count(*) FROM audit_log");
        n.next();
        assertThat(n.getLong(1)).isEqualTo(auditBefore + 1);
      } finally {
        c.rollback();
      }
    }
  }

  @Test
  @DisplayName("TC-GEO-0860 Nhật ký đổi giờ chỉ thêm, không sửa, không xóa")
  void tc0860() throws Exception {
    Sb s = sb("upcoming");
    var r =
        postJson(
            "/api/v1/teacher/assignments/" + s.assignment() + "/release",
            s.teacher(),
            "{\"openNow\":true,\"expectedRev\":" + rev(s) + "}");
    assertThat(r.statusCode()).isEqualTo(200);
    String where = " WHERE target_id = '" + s.assignment() + "' AND action = 'release_now'";
    assertThat(count("SELECT count(*) FROM audit_log" + where)).isEqualTo(1);
    assertThat(
            asApp(
                "UPDATE audit_log SET after_release_at = now()" + where,
                "DELETE FROM audit_log" + where))
        .doesNotContainNull();
    // kể cả vai chủ sở hữu: trigger chỉ-thêm chặn
    assertThatThrownBy(() -> jdbc.sql("DELETE FROM audit_log" + where).update())
        .isInstanceOf(Exception.class);
    assertThat(count("SELECT count(*) FROM audit_log" + where)).isEqualTo(1);
  }
}
