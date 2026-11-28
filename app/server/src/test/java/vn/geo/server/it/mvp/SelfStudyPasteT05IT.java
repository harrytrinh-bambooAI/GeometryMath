package vn.geo.server.it.mvp;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import vn.geo.server.it.e1.AbstractE1IT;
import vn.geo.server.learning.internal.service.HelpMessages;

/**
 * T-05 (đo AI thật 27/11/2026 08:16, nhóm 9 "dán lại đề vào Tự học"): F-GEO-226 lộ "45°" hai lần vì
 * mô tả dựng THẬT của AI cho đề S.MNPQ chọn mặt đáy (A,B,C) còn đề kho HKG-01 ghi (A,B,D), chữ ký
 * chuẩn bản 1 lệch nên bài tự học không kế thừa khóa của BG-1 (Sắp mở). Kịch bản giả lập {@code
 * T05-AI-THAT-HKG01} (mock-ai/scene.json) trả đúng chips/asked/hidden của AI thật. US-GEO-406 SC1,
 * SC5 (BR-GEO-17, 182, 183): xem đủ hai gợi ý rồi mở lời giải vẫn khóa MSG-GEO-31, không có bước
 * giải hay đáp số.
 */
class SelfStudyPasteT05IT extends AbstractE1IT {

  static final String SCENARIO = "T05-AI-THAT-HKG01";

  private void scenario(String s) throws Exception {
    String body =
        mapper.createObjectNode().put("userId", AN.toString()).put("scenario", s).toString();
    assertThat(postJson("/test-hooks/ai", null, body).statusCode()).isEqualTo(200);
  }

  @BeforeEach
  void setUp() throws Exception {
    reseed();
    scenario(SCENARIO);
  }

  @AfterEach
  void tearDown() throws Exception {
    scenario("");
  }

  private HttpResponse<String> post(String path, Object body) throws Exception {
    return postJson(path, an(), mapper.writeValueAsString(body));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = '|',
      value = {
        "F-GEO-225 dán nguyên văn|Cho hình chóp S.ABCD, đáy là hình vuông cạnh a, SA ⊥ (ABCD), SA ="
            + " a. Tính góc giữa SD và mặt đáy.",
        "F-GEO-226 đổi tên S.MNPQ|Cho hình chóp S.MNPQ, đáy là hình vuông cạnh a, SM ⊥ (MNPQ), SM ="
            + " a. Tính góc giữa SQ và mặt đáy."
      })
  void pastedLockedProblemStaysLocked(String caseName, String statement) throws Exception {
    var created = post("/api/v1/student/self-study", Map.of("statement", statement));
    assertThat(created.statusCode()).as(created.body()).isEqualTo(200);
    StringBuilder all = new StringBuilder(created.body());
    String base = "/api/v1/student/assignments/" + json(created).get("assignmentId").asString();
    for (int level : new int[] {1, 2}) {
      var h = post(base + "/hints/" + level, Map.of("requestId", UUID.randomUUID().toString()));
      assertThat(h.statusCode()).as(caseName + " gợi ý " + level + ": " + h.body()).isEqualTo(200);
      all.append(h.body());
    }
    var help = get(base + "/help", an());
    assertThat(help.body()).as(caseName).contains(HelpMessages.MSG_31);
    var sol = postJson(base + "/solution/open", an(), "{}");
    assertThat(sol.statusCode()).as(caseName + ": " + sol.body()).isEqualTo(403);
    assertThat(sol.body()).contains(HelpMessages.MSG_31);
    all.append(help.body()).append(sol.body()).append(get(base + "/scene", an()).body());
    assertThat(all.toString()).as(caseName).doesNotContain("45°").doesNotContain("45 độ");
  }
}
