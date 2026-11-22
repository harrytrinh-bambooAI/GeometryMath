package vn.geo.server.it.e1;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

/**
 * US-GEO-102-SC4 (mã lưu băm) và US-GEO-104-SC5 (chủ sở hữu, phiên bản lược đồ, nguồn gốc lời
 * giải).
 */
@ExtendWith(OutputCaptureExtension.class)
class IdentityE1IT extends AbstractE1IT {

  private static final String CODE = "GQ7KX2MTB4HN";

  @Test
  @DisplayName("TC-GEO-0172 Máy chủ không lưu mã rõ (US-GEO-102-SC4)")
  void tc0172_codeNeverStoredOrLogged(CapturedOutput output) throws Exception {
    postJson("/test-hooks/auth-failures/reset", null, "{}");
    var r =
        postJson(
            "/api/v1/auth/teacher",
            null,
            "{\"code\":\"" + CODE + "\"}",
            "X-Forwarded-For",
            "198.51.100.72");
    assertThat(r.statusCode()).isEqualTo(200);
    assertThat(json(r).get("displayName").asString()).isEqualTo("Giáo viên mẫu");

    String hex = HexFormat.of().formatHex(CODE.getBytes(StandardCharsets.US_ASCII));
    List<String> tables =
        jdbc.sql("SELECT tablename FROM pg_tables WHERE schemaname = 'public' ORDER BY tablename")
            .query(String.class)
            .list();
    assertThat(tables).contains("teacher_access_code", "auth_failure", "app_user");
    for (String t : tables) {
      long hits =
          jdbc.sql(
                  "SELECT count(*) FROM public.\""
                      + t
                      + "\" x WHERE position(:code in x::text) > 0 OR position(:hex in x::text) > 0")
              .param("code", CODE)
              .param("hex", hex)
              .query(Long.class)
              .single();
      assertThat(hits).as("bảng %s chứa mã rõ", t).isZero();
    }
    // chỉ có băm: verifier 32 byte của selector GQ7K
    assertThat(
            count(
                "SELECT count(*) FROM teacher_access_code WHERE selector = 'GQ7K' AND length(verifier_hmac) = 32"))
        .isEqualTo(1);
    assertThat(output.getAll()).doesNotContain(CODE);
  }

  @Test
  @DisplayName(
      "TC-GEO-0127 Bản ghi có chủ sở hữu, phiên bản lược đồ, nguồn gốc lời giải (US-GEO-104-SC5)")
  void tc0127_ownerSchemaVersionProvenance() {
    Map<String, Object> bg1 =
        jdbc.sql("SELECT org_id, assigned_by FROM assignment WHERE id = :id")
            .param("id", BG1)
            .query()
            .singleRow();
    assertThat(bg1.get("org_id")).isEqualTo(ORG_A);
    assertThat(bg1.get("assigned_by")).isEqualTo(TEACHER_A);
    assertThat(
            jdbc.sql("SELECT name FROM org WHERE id = :id")
                .param("id", ORG_A)
                .query(String.class)
                .single())
        .isEqualTo("THPT Mẫu A");

    // sự kiện của An (lượt hỏi + tiến độ) thuộc trường của An
    assertThat(count("SELECT count(*) FROM qa_turn WHERE user_id = '" + AN + "'")).isPositive();
    assertThat(
            count(
                "SELECT count(*) FROM qa_turn WHERE user_id = '"
                    + AN
                    + "' AND org_id <> '"
                    + ORG_A
                    + "'"))
        .isZero();
    assertThat(
            count(
                "SELECT count(*) FROM assignment_progress WHERE user_id = '"
                    + AN
                    + "' AND org_id <> '"
                    + ORG_A
                    + "'"))
        .isZero();

    Map<String, Object> sol =
        jdbc.sql(
                "SELECT model, prompt_version, author_user_id, schema_version FROM solution WHERE problem_version_id = :pv")
            .param("pv", HKG01_V)
            .query()
            .singleRow();
    assertThat(sol.get("model")).isEqualTo("gia-lap-1");
    assertThat(sol.get("prompt_version")).isEqualTo("p1");
    assertThat(sol.get("author_user_id")).isEqualTo(AUTHOR).isNotEqualTo(TEACHER_A);
    assertThat(((Number) sol.get("schema_version")).intValue()).isEqualTo(1);
    assertThat(
            jdbc.sql(
                    "SELECT account_kind::text FROM app_user WHERE id = :id AND org_id = platform_org_id()")
                .param("id", AUTHOR)
                .query(String.class)
                .single())
        .isEqualTo("system");
    assertThat(
            count(
                "SELECT count(*) FROM scene_build WHERE problem_version_id = '"
                    + HKG01_V
                    + "' AND schema_version = 1"))
        .isEqualTo(1);
  }
}
