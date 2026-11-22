package vn.geo.server.it.e1;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.content.internal.service.CatalogImporter;

/**
 * US-GEO-102-SC4 (mã lưu băm) và US-GEO-104-SC5 (chủ sở hữu, phiên bản lược đồ, nguồn gốc lời
 * giải).
 */
@ExtendWith(OutputCaptureExtension.class)
class IdentityE1IT extends AbstractE1IT {

  private static final String CODE = "GQ7KX2MTB4HN";
  private static final String IP = "198.51.100.72";
  private static final UUID CLASS_9B = id(11);

  @Autowired CatalogImporter importer;

  @Test
  @DisplayName("TC-GEO-0172 Máy chủ không lưu mã rõ (US-GEO-102-SC4)")
  void tc0172_codeNeverStoredOrLogged(CapturedOutput output) throws Exception {
    postJson("/test-hooks/auth-failures/reset", null, "{\"ips\":[\"" + IP + "\"]}");
    var r =
        postJson(
            "/api/v1/auth/teacher", null, "{\"code\":\"" + CODE + "\"}", "X-Forwarded-For", IP);
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

  /**
   * TC-GEO-0127: bản ghi do CODE THẬT tạo (không đọc seed): giáo viên mẫu giao bài qua API, An hỏi
   * qua API (lượt hỏi + sự kiện), bộ nạp kho đề nạp HKG-01 (bản sửa nhẹ nên tạo phiên bản và lời
   * giải mới). Dữ liệu hình (scene_build) chưa có đường tạo trong E1 (engine thuộc E2) nên kiểm bản
   * seed. Cuối ca đưa geo_test về trạng thái vàng cho lớp khác.
   */
  @Test
  @DisplayName(
      "TC-GEO-0127 Bản ghi có chủ sở hữu, phiên bản lược đồ, nguồn gốc lời giải (US-GEO-104-SC5)")
  void tc0127_ownerSchemaVersionProvenance() throws Exception {
    reseed();
    try {
      // 1. Bài giao: Giáo viên mẫu (F-GEO-003) giao HKG-01 cho lớp 9B qua API
      UUID bg = Uuid7.next();
      var created =
          postJson(
              "/api/v1/teacher/assignments",
              teacherA(),
              "{\"assignmentId\":\""
                  + bg
                  + "\",\"classId\":\""
                  + CLASS_9B
                  + "\",\"problemVersionId\":\""
                  + HKG01_V
                  + "\"}");
      assertThat(created.statusCode()).isEqualTo(201);
      Map<String, Object> row =
          jdbc.sql(
                  "SELECT a.org_id, a.assigned_by, o.name FROM assignment a JOIN org o ON o.id = a.org_id WHERE a.id = :id")
              .param("id", bg)
              .query()
              .singleRow();
      assertThat(row.get("org_id")).isEqualTo(ORG_A);
      assertThat(row.get("name")).isEqualTo("THPT Mẫu A");
      assertThat(row.get("assigned_by")).isEqualTo(TEACHER_A);

      // 2. Sự kiện của An: một lượt hỏi qua API => qa_turn + event_log 'ask', đều thuộc THPT Mẫu A
      UUID rid = Uuid7.next();
      var asked =
          postJson(
              "/api/v1/student/assignments/" + BG1 + "/questions",
              an(),
              "{\"question\":\"Vì sao A là hình chiếu của S?\",\"requestId\":\"" + rid + "\"}");
      assertThat(asked.statusCode()).isEqualTo(200);
      assertThat(
              jdbc.sql(
                      "SELECT o.name FROM qa_turn q JOIN org o ON o.id = q.org_id WHERE q.request_id = :r AND q.user_id = :u")
                  .param("r", rid)
                  .param("u", AN)
                  .query(String.class)
                  .single())
          .isEqualTo("THPT Mẫu A");
      List<UUID> eventOrgs =
          jdbc.sql(
                  "SELECT org_id FROM event_log WHERE user_id = :u AND kind = 'ask' AND occurred_at >= :t")
              .param("u", AN)
              .param("t", java.sql.Timestamp.from(t0))
              .query(UUID.class)
              .list();
      assertThat(eventOrgs).isNotEmpty().containsOnly(ORG_A);

      // 3. Lời giải HKG-01 do bộ nạp kho đề ghi (bản sửa nhẹ => phiên bản mới)
      var imported =
          importer.importXlsx(
              // ô E2 = đề HKG-01 (chuỗi inline, chữ có dấu ở dạng thực thể XML): thêm tiền tố
              BenchmarkE1IT.variant(
                  x -> x.replaceFirst("(<c r=\"E2\"[^>]*><is><t>)", "$1(sửa nhẹ) ")));
      assertThat(imported.codes()).contains("HKG-01");
      UUID newVersion =
          jdbc.sql(
                  "SELECT current_version_id FROM problem WHERE org_id = platform_org_id() AND code = 'HKG-01'")
              .query(UUID.class)
              .single();
      assertThat(newVersion).as("bộ nạp tạo phiên bản mới").isNotEqualTo(HKG01_V);
      Map<String, Object> sol =
          jdbc.sql(
                  "SELECT model, prompt_version, author_user_id, schema_version FROM solution WHERE problem_version_id = :pv")
              .param("pv", newVersion)
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

      // 4. Dữ liệu hình HKG-01 (seed; chưa có đường tạo trong E1) mang số phiên bản lược đồ
      assertThat(
              count(
                  "SELECT count(*) FROM scene_build WHERE problem_version_id = '"
                      + HKG01_V
                      + "' AND schema_version = 1"))
          .isEqualTo(1);
    } finally {
      reseed();
    }
  }
}
