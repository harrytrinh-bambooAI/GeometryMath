package vn.geo.server.it.s3e5;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;
import vn.geo.server.iam.api.ClassAiPolicy;
import vn.geo.server.it.e1.AbstractE1IT;

/** US-S3E5-02 (cờ đã đọc chỉ ba giá trị) và US-S3E5-04 (công tắc AI không đụng phía giáo viên). */
class PrivacyAiS3E5IT extends AbstractE1IT {

  @Autowired ClassAiPolicy policy;

  @Test
  @DisplayName("TC-S3E5-17 Bản ghi cờ chỉ có ba giá trị")
  void ackRecordHasThreeValues() throws Exception {
    HttpResponse<String> r =
        postJson(
            "/api/v1/auth/register",
            null,
            """
            {"displayName":"Thu Hà","role":"student","password":"MatKhau#2026","privacyAck":true,"privacyVersion":"1.0"}
            """,
            "X-Forwarded-For",
            "10.251.17.3");
    assertThat(r.statusCode()).as(r.body()).isEqualTo(200);
    JsonNode reg = json(r);
    UUID id = UUID.fromString(reg.get("userId").asString());
    // bản ghi cờ: mã người dùng (khóa hàng) + phiên bản + giờ máy chủ; không cột IP, thiết bị,
    // trình duyệt
    String row =
        jdbc.sql(
                "SELECT privacy_ack_version || '|' || (privacy_ack_at IS NOT NULL) FROM app_user WHERE id = :id")
            .param("id", id)
            .query(String.class)
            .single();
    assertThat(row).isEqualTo("1.0|true");
    Set<String> cols =
        new TreeSet<>(
            jdbc.sql(
                    "SELECT column_name FROM information_schema.columns WHERE table_name = 'app_user'")
                .query(String.class)
                .list());
    assertThat(cols.stream().filter(c -> c.startsWith("privacy")).toList())
        .containsExactlyInAnyOrder("privacy_ack_version", "privacy_ack_at");
    assertThat(cols)
        .noneMatch(c -> c.contains("ip") && !c.contains("recipient"))
        .noneMatch(c -> c.contains("device") || c.contains("agent") || c.contains("browser"));
    JsonNode mine = json(get("/api/v1/account/privacy", reg.get("token").asString()));
    Set<String> keys = new TreeSet<>(mine.propertyNames());
    assertThat(keys)
        .containsExactlyInAnyOrder("version", "title", "body", "ackedVersion", "required");
    assertThat(mine.get("ackedVersion").asString()).isEqualTo("1.0");
    assertThat(mine.get("required").asBoolean()).isFalse();
  }

  @Test
  @DisplayName("TC-S3E5-51 \"AI gộp\" của giáo viên không bị tắt theo công tắc lớp")
  void teacherSideAiIsNotSwitchedOff() throws Exception {
    jdbc.sql("UPDATE class SET ai_enabled = false WHERE id = :id").param("id", CLASS_11A1).update();
    try {
      assertThat(policy.blockedForStudent(ORG_A, AN, BG1)).isTrue();
      assertThat(policy.blockedForStudent(ORG_A, TEACHER_A, BG1)).isFalse();
      long calls = count("SELECT count(*) FROM ai_call_log WHERE user_id = '" + AN + "'");
      HttpResponse<String> asked =
          post(
              "/api/v1/student/assignments/" + BG1 + "/questions",
              an(),
              "{\"question\":\"Gợi ý giúp em\",\"requestId\":\"" + UUID.randomUUID() + "\"}");
      assertThat(asked.statusCode()).isEqualTo(403);
      assertThat(json(asked).get("details").get(0).get("message").asString())
          .isEqualTo("AI_OFF_FOR_CLASS");
      assertThat(count("SELECT count(*) FROM ai_call_log WHERE user_id = '" + AN + "'"))
          .isEqualTo(calls);
      // phía giáo viên không đi qua công tắc: cùng đường dẫn, vai giáo viên bị chặn theo VAI
      // (FORBIDDEN không kèm AI_OFF_FOR_CLASS), không phải theo công tắc lớp
      HttpResponse<String> teacher =
          post(
              "/api/v1/student/assignments/" + BG1 + "/questions",
              teacherA(),
              "{\"question\":\"x\",\"requestId\":\"" + UUID.randomUUID() + "\"}");
      assertThat(teacher.body()).doesNotContain("AI_OFF_FOR_CLASS");
    } finally {
      jdbc.sql("UPDATE class SET ai_enabled = true WHERE id = :id")
          .param("id", CLASS_11A1)
          .update();
    }
    assertThat(policy.blockedForStudent(ORG_A, AN, BG1)).isFalse();
    assertThat(List.of(BG1)).isNotEmpty();
  }

  /**
   * Dev-REV-A1 (bản soát T-20…T-32 mục 1): đường dẫn biến thể mà Spring MVC vẫn đưa vào bộ xử lý
   * hỏi đáp (tham số đường dẫn {@code ;}, ký tự mã hóa %xx, {@code //}, {@code /} cuối, UUID chữ
   * hoa) không được né công tắc AI theo lớp: hoặc FORBIDDEN AI_OFF_FOR_CLASS, hoặc không tới bộ xử
   * lý; tuyệt đối không thêm lượt ai_call_log.
   */
  @Test
  @DisplayName("Dev-REV-A1 Biến thể đường dẫn không né công tắc AI theo lớp")
  void pathVariantsCannotBypassClassAiSwitch() throws Exception {
    jdbc.sql("UPDATE class SET ai_enabled = false WHERE id = :id").param("id", CLASS_11A1).update();
    try {
      String id = BG1.toString();
      List<String> variants =
          List.of(
              "/api/v1/student/assignments/" + id + "/questions",
              "/api/v1/student/assignments/" + id + "/questions;x=1",
              "/api/v1/student/assignments;x=1/" + id + "/questions",
              "/api/v1/student/assignments/" + id + ";x=1/questions",
              "/api/v1/student/assignments/" + id + "/%71uestions",
              "/api/v1/student/assignments/" + id.replace("-", "%2D") + "/questions",
              "/api/v1/student//assignments/" + id + "/questions",
              "/api/v1//student/assignments/" + id + "//questions",
              "/api/v1/student/assignments/" + id + "/questions/",
              "/api/v1/student/assignments/" + id.toUpperCase() + "/questions",
              "/api/v1/student/assignments/" + id + "%2Fquestions",
              "/API/v1/student/assignments/" + id + "/questions");
      List<String> leaks = new java.util.ArrayList<>();
      for (String path : variants) {
        long before = count("SELECT count(*) FROM ai_call_log WHERE user_id = '" + AN + "'");
        HttpResponse<String> r =
            post(
                path,
                an(),
                "{\"question\":\"Gợi ý giúp em\",\"requestId\":\"" + UUID.randomUUID() + "\"}");
        long after = count("SELECT count(*) FROM ai_call_log WHERE user_id = '" + AN + "'");
        boolean blocked = r.statusCode() == 403 && r.body().contains("AI_OFF_FOR_CLASS");
        boolean notRouted = r.statusCode() >= 400 && r.statusCode() != 403;
        if (after != before || !(blocked || notRouted)) {
          leaks.add(path + " -> " + r.statusCode() + " " + r.body());
        }
      }
      assertThat(leaks).as("biến thể né công tắc AI").isEmpty();
    } finally {
      jdbc.sql("UPDATE class SET ai_enabled = true WHERE id = :id")
          .param("id", CLASS_11A1)
          .update();
    }
  }
}
