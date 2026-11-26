package vn.geo.server.it.s3e5;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import vn.geo.server.iam.internal.service.AccountDeletionService;
import vn.geo.server.iam.internal.service.ClassTestWorld;
import vn.geo.server.iam.internal.service.ClassTestWorld.ClassSpec;
import vn.geo.server.iam.internal.service.ClassTestWorld.Person;
import vn.geo.server.iam.internal.service.ClassTestWorld.Spec;
import vn.geo.server.iam.internal.service.ClassTestWorld.World;
import vn.geo.server.it.e1.AbstractE1IT;

/** US-S3E5-05: xóa tài khoản (BR-S3E5-50…55) và công việc dọn sau 30 ngày. */
class AccountDeletionS3E5IT extends AbstractE1IT {

  static final UUID COMMUNITY = AllowedFieldsS3E5IT.COMMUNITY;
  static final UUID NIL = new UUID(0, 0);

  @Autowired ClassTestWorld worlds;
  @Autowired AccountDeletionService deletion;

  private World world() {
    return worlds.build(
        new Spec(
            List.of("Cô Lan", "Thầy Khôi"),
            List.of(),
            List.of(
                new ClassSpec(
                    "11A1",
                    "Cô Lan",
                    "11A1",
                    11,
                    List.of("Bình An"),
                    List.of(),
                    List.of(),
                    List.of()),
                new ClassSpec(
                    "10B",
                    "Thầy Khôi",
                    "10B",
                    10,
                    List.of("Mi Na"),
                    List.of(),
                    List.of(),
                    List.of()))));
  }

  private HttpResponse<String> del(UUID target, String token, String body) throws Exception {
    return post("/api/v1/account/users/" + target + "/delete", token, body);
  }

  private static String body(String password, String confirm) {
    return "{\"password\":\""
        + password
        + "\",\"confirm\":\""
        + confirm
        + "\",\"requestId\":\""
        + UUID.randomUUID()
        + "\"}";
  }

  private HttpResponse<String> login(String code, String password) throws Exception {
    return post(
        "/api/v1/auth/code-login",
        null,
        "{\"userCode\":\"" + code + "\",\"password\":\"" + password + "\"}");
  }

  /** Số hàng ở mọi cột uuid của mọi bảng mang giá trị {@code id}. */
  private java.util.Map<String, Long> rowsMentioning(UUID id) {
    List<String[]> cols =
        jdbc.sql(
                """
                SELECT c.table_name, c.column_name FROM information_schema.columns c
                JOIN information_schema.tables t USING (table_schema, table_name)
                WHERE c.table_schema = 'public' AND t.table_type = 'BASE TABLE' AND c.data_type = 'uuid'
                """)
            .query((rs, i) -> new String[] {rs.getString(1), rs.getString(2)})
            .list();
    java.util.Map<String, Long> hits = new java.util.TreeMap<>();
    for (String[] c : cols) {
      long n =
          jdbc.sql("SELECT count(*) FROM \"" + c[0] + "\" WHERE \"" + c[1] + "\" = :id")
              .param("id", id)
              .query(Long.class)
              .single();
      if (n > 0) {
        hits.put(c[0] + "." + c[1], n);
      }
    }
    return hits;
  }

  @Test
  @DisplayName(
      "TC-S3E5-56 Dữ liệu của tài khoản đã xóa bị xóa vật lý trong 30 ngày; AuditEvent cũ vẫn hiện với mã \"đã xóa\" (Dev-S3E5-16)")
  void deletedAccountIsPurgedAfter30Days() throws Exception {
    World w = world();
    Person an = w.people().get("Bình An");
    Person lan = w.people().get("Cô Lan");
    UUID org = w.orgId();
    // dữ liệu của em: thông báo, AuditEvent đặt lại mật khẩu có em là đối tượng
    jdbc.sql(
            "INSERT INTO notification (id, org_id, user_id, kind, event_id, body) VALUES (:id, :org, :u, 'new_set', :e, 'Bộ bài mới')")
        .param("id", UUID.randomUUID())
        .param("org", org)
        .param("u", an.id())
        .param("e", UUID.randomUUID())
        .update();
    jdbc.sql(
            """
            INSERT INTO audit_log (org_id, actor_user_id, action, target_id, actor_role, target_type, class_id, outcome)
            VALUES (:org, :lan, 'password_reset', :an, 'teacher', 'user', :c, 'success')
            """)
        .param("org", org)
        .param("lan", lan.id())
        .param("an", an.id())
        .param("c", w.classes().get("11A1").id())
        .update();
    assertThat(del(an.id(), an.token(), body(an.password(), "XÓA")).statusCode()).isEqualTo(200);
    Instant t = Instant.now();
    // ngay sau khi xóa: không đăng nhập, không còn thành viên lớp, thông báo
    assertThat(login(an.userCode(), an.password()).statusCode()).isEqualTo(400);
    assertThat(count("SELECT count(*) FROM class_member WHERE user_id = '" + an.id() + "'"))
        .isZero();
    assertThat(count("SELECT count(*) FROM notification WHERE user_id = '" + an.id() + "'"))
        .isZero();
    assertThat(
            count(
                "SELECT count(*) FROM app_user WHERE (display_name = '"
                    + an.displayName()
                    + "' OR user_code IS NOT NULL OR password_hash IS NOT NULL) AND id = '"
                    + an.id()
                    + "'"))
        .isZero();
    // trước 30 ngày công việc dọn không xóa bia mộ; ở T + 30 thì xóa hẳn
    deletion.purge(t.plus(Duration.ofDays(29)));
    assertThat(count("SELECT count(*) FROM app_user WHERE id = '" + an.id() + "'")).isEqualTo(1);
    assertThat(deletion.purge(t.plus(Duration.ofDays(30)).plusSeconds(5)))
        .isGreaterThanOrEqualTo(1);
    assertThat(rowsMentioning(an.id())).isEmpty();
    assertThat(
            count(
                "SELECT count(*) FROM audit_log WHERE org_id = '"
                    + org
                    + "' AND action = 'password_reset' AND target_id = '"
                    + NIL
                    + "' AND actor_user_id = '"
                    + lan.id()
                    + "'"))
        .isEqualTo(1);
    // Dev-S3E5-16: danh sách AuditEvent của lớp (S3-E4) vẫn hiện dòng của người đã xóa, mã "đã xóa"
    // (đối tượng là em đã xóa; người làm đã xóa giả lập bằng hàng có actor UUID 0)
    UUID c11a1 = w.classes().get("11A1").id();
    jdbc.sql(
            """
            INSERT INTO audit_log (org_id, actor_user_id, action, target_id, actor_role, target_type, class_id, outcome)
            VALUES (:org, :nil, 'password_reset', :lan, 'teacher', 'user', :c, 'success')
            """)
        .param("org", org)
        .param("nil", NIL)
        .param("lan", lan.id())
        .param("c", c11a1)
        .update();
    var log = json(get("/api/v1/teacher/classes/" + c11a1 + "/audit-events", lan.token()));
    assertThat(log.get("items").size()).as("cả hai dòng hiện").isEqualTo(2);
    var deletedActor = log.get("items").get(0);
    assertThat(deletedActor.get("actorCode").asString()).isEqualTo("đã xóa");
    assertThat(deletedActor.get("targetId").asString()).isEqualTo(lan.userCode());
    var deletedTarget = log.get("items").get(1);
    assertThat(deletedTarget.get("actorCode").asString()).isEqualTo(lan.userCode());
    assertThat(deletedTarget.get("targetId").asString()).isEqualTo("đã xóa");
  }

  @Test
  @DisplayName(
      "US-S3E5-05 FC1–FC4, SC2: chặn người khác, chữ XÓA, sai mật khẩu, lớp Đang dạy; giáo viên đã lưu trữ lớp xóa được")
  void deletionRules() throws Exception {
    World w = world();
    Person an = w.people().get("Bình An");
    Person lan = w.people().get("Cô Lan");
    Person khoi = w.people().get("Thầy Khôi");
    // FC4: cô Lan xóa tài khoản của Bình An
    HttpResponse<String> other = del(an.id(), lan.token(), body(lan.password(), "XÓA"));
    assertThat(other.statusCode()).isEqualTo(404);
    assertThat(json(other).get("code").asString()).isEqualTo("NOT_FOUND");
    // FC3: thiếu chữ XÓA hoặc gõ "xoa"
    for (String b :
        List.of(body(an.password(), "xoa"), "{\"password\":\"" + an.password() + "\"}")) {
      HttpResponse<String> r = del(an.id(), an.token(), b);
      assertThat(r.statusCode()).isEqualTo(400);
      assertThat(json(r).get("details").get(0).get("field").asString()).isEqualTo("confirm");
    }
    // FC2: sai mật khẩu, đếm vào bộ khóa đăng nhập
    HttpResponse<String> wrong = del(an.id(), an.token(), body("SaiMatKhau#1", "XÓA"));
    assertThat(wrong.statusCode()).isEqualTo(400);
    assertThat(json(wrong).get("details").get(0).get("field").asString()).isEqualTo("password");
    assertThat(count("SELECT login_fail_count FROM app_user WHERE id = '" + an.id() + "'"))
        .isEqualTo(1);
    assertThat(
            count(
                "SELECT count(*) FROM app_user WHERE id = '"
                    + an.id()
                    + "' AND deleted_at IS NULL"))
        .isEqualTo(1);
    // FC1: cô Lan còn lớp Đang dạy
    HttpResponse<String> busy = del(lan.id(), lan.token(), body(lan.password(), "XÓA"));
    assertThat(busy.statusCode()).isEqualTo(409);
    assertThat(json(busy).get("message").asString()).contains("11A1");
    assertThat(login(lan.userCode(), lan.password()).statusCode()).isEqualTo(200);
    // SC2: thầy Khôi lưu trữ 10B rồi xóa
    UUID c10 = w.classes().get("10B").id();
    assertThat(
            post("/api/v1/teacher/my-classes/" + c10 + "/archive", khoi.token(), "").statusCode())
        .isEqualTo(200);
    assertThat(del(khoi.id(), khoi.token(), body(khoi.password(), "XÓA")).statusCode())
        .isEqualTo(200);
    assertThat(login(khoi.userCode(), khoi.password()).statusCode()).isEqualTo(400);
    HttpResponse<String> recover =
        post(
            "/api/v1/auth/recover",
            null,
            "{\"userCode\":\""
                + khoi.userCode()
                + "\",\"recoveryCode\":\"RC-AAAA-BBBB-CCCC\",\"newPassword\":\"MatKhau#2027\"}");
    assertThat(recover.statusCode()).isBetween(400, 429);
    // thẻ cũ hết hiệu lực; mã không cấp lại
    assertThat(get("/api/v1/account/me", khoi.token()).statusCode()).isEqualTo(401);
    Boolean reissued =
        jdbc.sql(
                "SELECT fn_register_code_user(:id, :org, 'Cộng đồng', true, 'Người mới', :code, 'x', decode(repeat('ab', 32), 'hex'), '1.0', now())")
            .param("id", UUID.randomUUID())
            .param("org", COMMUNITY)
            .param("code", khoi.userCode())
            .query(Boolean.class)
            .single();
    assertThat(reissued).isFalse();
    assertThat(count("SELECT count(*) FROM app_user WHERE user_code = '" + khoi.userCode() + "'"))
        .isZero();
  }
}
