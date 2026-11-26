package vn.geo.server.it.settings;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.it.AbstractIT;

/**
 * T-261126-50: theme giao diện theo tài khoản (GET/PUT /api/v1/me/settings, bảng user_preference
 * V109). Mặc định 'c'; chỉ nhận a|b|c|d; mỗi người chỉ thấy tùy chọn của chính mình.
 */
class SettingsIT extends AbstractIT {

  @Autowired JdbcClient jdbc;
  private final HttpClient http = HttpClient.newHttpClient();

  private UUID[] newOrgWithUsers() {
    UUID org = Uuid7.next();
    UUID teacher = Uuid7.next();
    UUID student = Uuid7.next();
    jdbc.sql(
            "INSERT INTO org (id, parent_id, kind, name) VALUES (:id, platform_org_id(), 'group', 'Trường thử theme IT')")
        .param("id", org)
        .update();
    jdbc.sql(
            "INSERT INTO app_user (id, org_id, role, account_kind, display_name) VALUES (:id, :org, 'teacher', 'sample', 'Giáo viên theme')")
        .param("id", teacher)
        .param("org", org)
        .update();
    jdbc.sql(
            "INSERT INTO app_user (id, org_id, role, account_kind, display_name) VALUES (:id, :org, 'student', 'light', 'Học sinh theme')")
        .param("id", student)
        .param("org", org)
        .update();
    return new UUID[] {org, teacher, student};
  }

  private String token(UUID org, UUID user, Role role) {
    return tokens.issue(new AuthPrincipal(org, user, role));
  }

  private HttpResponse<String> put(String bearer, String body) throws Exception {
    HttpRequest r =
        HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/v1/me/settings"))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + bearer)
            .PUT(HttpRequest.BodyPublishers.ofString(body))
            .build();
    return http.send(r, HttpResponse.BodyHandlers.ofString());
  }

  private String theme(String bearer) throws Exception {
    HttpResponse<String> r = get("/api/v1/me/settings", bearer);
    assertThat(r.statusCode()).isEqualTo(200);
    return json(r).get("theme").asString();
  }

  @Test
  void defaultIsCAndSavedPerAccount() throws Exception {
    UUID[] o = newOrgWithUsers();
    String gv = token(o[0], o[1], Role.TEACHER);
    String hs = token(o[0], o[2], Role.STUDENT);
    assertThat(theme(gv)).isEqualTo("c");
    assertThat(theme(hs)).isEqualTo("c");

    assertThat(put(hs, "{\"theme\":\"a\"}").statusCode()).isEqualTo(200);
    assertThat(put(gv, "{\"theme\":\"b\"}").statusCode()).isEqualTo(200);
    assertThat(put(gv, "{\"theme\":\"d\"}").statusCode()).isEqualTo(200); // ghi đè
    assertThat(theme(hs)).isEqualTo("a");
    assertThat(theme(gv)).isEqualTo("d");
  }

  @Test
  void rejectsUnknownThemeAndAnonymous() throws Exception {
    UUID[] o = newOrgWithUsers();
    String gv = token(o[0], o[1], Role.TEACHER);
    assertThat(put(gv, "{\"theme\":\"x\"}").statusCode()).isEqualTo(400);
    assertThat(put(gv, "{}").statusCode()).isEqualTo(400);
    assertThat(theme(gv)).isEqualTo("c");
    assertThat(get("/api/v1/me/settings", null).statusCode()).isEqualTo(401);
  }

  @Test
  void accountOutsideOrgIsNotFound() throws Exception {
    UUID[] o = newOrgWithUsers();
    UUID[] other = newOrgWithUsers();
    // thẻ mang trường khác với trường của tài khoản: không ghi được hàng chéo trường
    String wrong = token(other[0], o[1], Role.TEACHER);
    assertThat(put(wrong, "{\"theme\":\"b\"}").statusCode()).isEqualTo(404);
    assertThat(theme(token(o[0], o[1], Role.TEACHER))).isEqualTo("c");
  }
}
