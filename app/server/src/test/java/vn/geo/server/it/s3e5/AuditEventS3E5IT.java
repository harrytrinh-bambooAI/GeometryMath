package vn.geo.server.it.s3e5;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.geo.server.assignment.internal.repo.AuditEventRepository;
import vn.geo.server.iam.internal.service.ClassTestWorld.World;
import vn.geo.server.it.s3e4.AbstractS3E4IT;

/**
 * US-S3E5-01 — tích hợp của AuditEvent trên audit_log: nhật ký chỉ thêm (FC4), đếm PASSWORD_RESET
 * thành công theo lớp bằng chỉ mục (FC9, M-S3-05).
 */
class AuditEventS3E5IT extends AbstractS3E4IT {

  private static final String RESET = "/api/v1/teacher/my-classes/%s/students/%s/password-reset";

  private void reset(World w, String teacher, String klass, String student) throws Exception {
    var r =
        send(
            "POST",
            RESET.formatted(w.classes().get(klass).id(), w.people().get(student).id()),
            w.people().get(teacher).token(),
            null);
    assertThat(r.statusCode()).as(r.body()).isEqualTo(200);
  }

  private long resets(World w, String klass) {
    return jdbc.sql(AuditEventRepository.COUNT_SUCCESS)
        .param("orgId", w.orgId())
        .param("classId", w.classes().get(klass).id())
        .param("action", "password_reset")
        .query(Long.class)
        .single();
  }

  @Test
  @DisplayName(
      "TC-S3E5-10 Nhật ký chỉ thêm: không có đường API sửa / xóa; UPDATE và DELETE bằng tài khoản CSDL của ứng dụng bị từ chối; bản ghi nguyên vẹn")
  void appendOnly() throws Exception {
    World w = world();
    String lan = w.people().get("Cô Lan").token();
    UUID c11a1 = w.classes().get("11A1").id();
    reset(w, "Cô Lan", "11A1", "Chi Mai");
    var log = send("GET", "/api/v1/teacher/classes/" + c11a1 + "/audit-events", lan, null);
    assertThat(log.statusCode()).as(log.body()).isEqualTo(200);
    assertThat(json(log).get("items").size()).isEqualTo(1);
    long id = json(log).get("items").get(0).get("id").asLong();

    String one = "/api/v1/teacher/classes/" + c11a1 + "/audit-events/" + id;
    for (String method : new String[] {"PUT", "PATCH", "DELETE"}) {
      var r = send(method, one, lan, "DELETE".equals(method) ? null : "{\"result\":\"DENIED\"}");
      assertThat(r.statusCode()).as(method + " " + one).isIn(403, 404, 405);
    }
    var all = send("DELETE", "/api/v1/teacher/classes/" + c11a1 + "/audit-events", lan, null);
    assertThat(all.statusCode()).isIn(403, 404, 405);

    // Chủ schema (tài khoản IT) cũng bị trigger chỉ-thêm chặn.
    assertThatThrownBy(() ->
            jdbc.sql("UPDATE audit_log SET outcome = 'denied' WHERE id = :id")
                .param("id", id)
                .update())
        .as("UPDATE bị trigger chặn")
        .isInstanceOf(Exception.class);
    assertThatThrownBy(
            () -> jdbc.sql("DELETE FROM audit_log WHERE id = :id").param("id", id).update())
        .as("DELETE bị trigger chặn")
        .isInstanceOf(Exception.class);
    // Vai CSDL của ứng dụng (geo_app) không có quyền UPDATE, DELETE, INSERT thẳng trên audit_log.
    for (String sql :
        new String[] {
          "UPDATE audit_log SET outcome = 'denied' WHERE id = " + id,
          "DELETE FROM audit_log WHERE id = " + id,
          "INSERT INTO audit_log (org_id, actor_user_id, action, target_id) SELECT org_id,"
              + " actor_user_id, action, target_id FROM audit_log WHERE id = "
              + id
        }) {
      try (var c = dataSource.getConnection();
          var st = c.createStatement()) {
        c.setAutoCommit(false);
        st.execute("SET LOCAL ROLE geo_app");
        assertThatThrownBy(() -> st.executeUpdate(sql))
            .as("geo_app: " + sql)
            .isInstanceOf(java.sql.SQLException.class)
            .hasMessageContaining("permission denied");
        c.rollback();
      }
    }
    assertThat(
            count(
                "SELECT count(*) FROM audit_log WHERE id = ? AND outcome = 'success'"
                    + " AND action = 'password_reset'",
                id))
        .as("bản ghi nguyên vẹn")
        .isEqualTo(1);
  }

  @Test
  @DisplayName(
      "TC-S3E5-15 Đếm PASSWORD_RESET thành công theo lớp phục vụ M-S3-05: 4, 1, 0; không tính bản ghi Từ chối; kế hoạch truy vấn dùng chỉ mục")
  void countByClassUsesIndex() throws Exception {
    World w = world();
    for (String s : new String[] {"Bình An", "Chi Mai", "Đức Huy", "Gia Bảo"}) {
      reset(w, "Cô Lan", "11A1", s);
    }
    reset(w, "Cô Lan", "11A2", "Quốc Bảo");
    // Bản ghi Từ chối: thầy Khôi mở ngay bộ của cô Lan, và đặt lại mật khẩu học sinh lớp 11A1.
    String set =
        assigned(
            w.people().get("Cô Lan").token(),
            w.classes().get("11A1").id(),
            "Bộ bài tuần 15",
            172_800L,
            1);
    String khoi = w.people().get("Thầy Khôi").token();
    assertThat(
            send("POST", "/api/v1/teacher/assignment-sets/" + set + "/open-now", khoi, "{}")
                .statusCode())
        .isEqualTo(404);
    assertThat(
            send(
                    "POST",
                    RESET.formatted(w.classes().get("11A1").id(), w.people().get("Bình An").id()),
                    khoi,
                    null)
                .statusCode())
        .isEqualTo(404);
    assertThat(
            count(
                "SELECT count(*) FROM audit_log WHERE actor_user_id = ? AND outcome = 'denied'",
                w.people().get("Thầy Khôi").id()))
        .isEqualTo(1);

    assertThat(resets(w, "11A1")).isEqualTo(4);
    assertThat(resets(w, "11A2")).isEqualTo(1);
    assertThat(resets(w, "10B")).isZero();

    String explain =
        "EXPLAIN "
            + AuditEventRepository.COUNT_SUCCESS
                .replace(":orgId", "'" + w.orgId() + "'")
                .replace(":classId", "'" + w.classes().get("11A1").id() + "'")
                .replace(":action", "'password_reset'");
    StringBuilder plan = new StringBuilder();
    try (var c = dataSource.getConnection();
        var st = c.createStatement()) {
      c.setAutoCommit(false);
      st.execute("SET LOCAL enable_seqscan = off");
      try (var rs = st.executeQuery(explain)) {
        while (rs.next()) {
          plan.append(rs.getString(1)).append('\n');
        }
      }
      c.rollback();
    }
    assertThat(plan.toString())
        .as("kế hoạch truy vấn")
        .contains("audit_log_class")
        .doesNotContain("Seq Scan");
  }
}
