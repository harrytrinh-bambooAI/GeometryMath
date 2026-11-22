package vn.geo.server.it.e1;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import vn.geo.server.aigateway.internal.MockAiControl;
import vn.geo.server.common.security.Role;

/** US-GEO-103: mọi lượt gọi AI qua cổng, mỗi lượt đúng một bản ghi, không gửi định danh. */
class AiGatewayE1IT extends AbstractE1IT {

  @Autowired MockAiControl mock;

  @AfterEach
  void resetMock() throws Exception {
    postJson("/test-hooks/ai/reset", null, "{}");
  }

  private Map<String, Object> lastCall() {
    return jdbc.sql(
            """
            SELECT purpose::text AS purpose, model, prompt_version, cost_micro_usd, status::text AS status,
                   latency_ms, user_id, org_id
            FROM ai_call_log ORDER BY id DESC LIMIT 1
            """)
        .query()
        .singleRow();
  }

  @Test
  @DisplayName("TC-GEO-0116 Một lượt gọi, một bản ghi đủ trường (US-GEO-103-SC1)")
  void tc0116_oneCallOneRecord() throws Exception {
    long before = count("SELECT count(*) FROM ai_call_log");
    var r =
        postJson(
            "/api/v1/student/assignments/" + BG1 + "/questions",
            token(ORG_A, DUNG, Role.STUDENT),
            "{\"question\":\"Tan của một góc là gì?\"}");
    assertThat(r.statusCode()).isEqualTo(200);
    assertThat(count("SELECT count(*) FROM ai_call_log")).isEqualTo(before + 1);
    var row = lastCall();
    assertThat(row.get("purpose")).isEqualTo("qa");
    assertThat(row.get("model")).isEqualTo("gia-lap-1");
    assertThat(row.get("prompt_version")).isEqualTo("p1");
    assertThat(((Number) row.get("cost_micro_usd")).longValue() / 1e6).isEqualTo(0.12);
    assertThat(row.get("status")).isEqualTo("ok");
    assertThat(((Number) row.get("latency_ms")).intValue()).isGreaterThanOrEqualTo(0);
    assertThat(row.get("user_id")).isEqualTo(DUNG);
  }

  @Test
  @DisplayName("TC-GEO-0117 Đổi model bằng cấu hình (US-GEO-103-SC2)")
  void tc0117_modelFromConfig() throws Exception {
    String dung = token(ORG_A, DUNG, Role.STUDENT);
    String q = "{\"question\":\"Tan của một góc là gì?\"}";
    postJson("/api/v1/student/assignments/" + BG1 + "/questions", dung, q);
    long oldId = jdbc.sql("SELECT max(id) FROM ai_call_log").query(Long.class).single();
    assertThat(lastCall().get("model")).isEqualTo("gia-lap-1");

    postJson("/test-hooks/ai", null, "{\"model\":\"gia-lap-2\"}");
    var r = postJson("/api/v1/student/assignments/" + BG1 + "/questions", dung, q);
    assertThat(r.statusCode()).isEqualTo(200);
    assertThat(lastCall().get("model")).isEqualTo("gia-lap-2");
    assertThat(
            jdbc.sql("SELECT model FROM ai_call_log WHERE id = :id")
                .param("id", oldId)
                .query(String.class)
                .single())
        .isEqualTo("gia-lap-1");
  }

  @Test
  @DisplayName("TC-GEO-0119 Không gửi định danh học sinh cho nhà cung cấp (US-GEO-103-SC4)")
  void tc0119_noIdentityToProvider() throws Exception {
    // thân yêu cầu cố kèm mã trường: phải bị bỏ qua và không đi tới nhà cung cấp
    var r =
        postJson(
            "/api/v1/student/assignments/" + BG1 + "/questions",
            an(),
            "{\"question\":\"Vì sao A là hình chiếu của S?\",\"orgId\":\"" + ORG_C + "\"}");
    assertThat(r.statusCode()).isEqualTo(200);
    List<String> prompts = mock.receivedPrompts();
    String last = prompts.get(prompts.size() - 1);
    assertThat(last).contains("Vì sao A là hình chiếu của S?");
    for (String forbidden :
        List.of(
            "An",
            "HK7M2A",
            "11A1",
            "THPT Mẫu A",
            AN.toString(),
            ORG_A.toString(),
            ORG_C.toString(),
            CLASS_11A1.toString())) {
      assertThat(last).as("prompt không được chứa %s", forbidden).doesNotContain(forbidden);
    }
  }

  @Test
  @DisplayName("TC-GEO-0121 Nhà cung cấp quá thời gian (US-GEO-103-FC2)")
  void tc0121_timeout() throws Exception {
    postJson(
        "/test-hooks/ai",
        null,
        "{\"mode\":\"timeout\",\"userId\":\"" + AN + "\",\"timeoutMs\":400}");
    long before = count("SELECT count(*) FROM qa_turn");
    long t = System.nanoTime();
    var r =
        postJson(
            "/api/v1/student/assignments/" + BG1 + "/questions",
            an(),
            "{\"question\":\"Vì sao A là hình chiếu của S?\"}");
    long ms = (System.nanoTime() - t) / 1_000_000;
    assertThat(r.statusCode()).isEqualTo(504);
    assertThat(json(r).get("code").asString()).isEqualTo("TIMEOUT");
    assertThat(ms).as("không quay vô hạn").isLessThan(5_000);
    assertThat(lastCall().get("status")).isEqualTo("timeout");
    assertThat(count("SELECT count(*) FROM qa_turn"))
        .as("không lưu câu trả lời dở")
        .isEqualTo(before);
  }
}
