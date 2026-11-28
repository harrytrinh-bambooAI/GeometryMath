package vn.geo.server.it.mvp;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.content.internal.service.SceneChecker;
import vn.geo.server.geometry.internal.DefaultGeometryEngine;

/**
 * T-05 (đo AI thật 27/11/2026 08:16, MVP-METRICS-20261127-0816): 9/52 đề khối tròn (HKG-05, 06,
 * HKN-40…46) có đáp số đúng nhưng hình trượt ràng buộc. Gốc: prompt "solution" của bộ đề chuẩn (a1)
 * không nêu tên điểm phụ của khối tròn mà đề không đặt tên (tâm đáy, điểm trên đường tròn đáy, tâm
 * đáy kia, tâm mặt cầu ngoại tiếp), nên AI thật bỏ qua (HKN-41, 43, 44, 46 chỉ có O) hoặc đặt tên
 * khác (A, A′ thay M, M′; O thay I) — phép kiểm theo tên đỉnh của bộ dựng thiếu điểm. Prompt a2 nêu
 * tên mặc định. Dữ liệu là đầu ra THẬT của claude-sonnet-5-5 đã ghi ({@code
 * mvp/t05-ai-solution-round.json}); phép kiểm là đúng phép của bộ đo ({@link BenchSceneCheck}),
 * không nới ngưỡng.
 */
class RoundSolidSceneT05Test {

  static final List<String> FAILED_0816 =
      List.of(
          "HKG-05", "HKG-06", "HKN-40", "HKN-41", "HKN-42", "HKN-43", "HKN-44", "HKN-45", "HKN-46");

  private final JsonMapper mapper = JsonMapper.builder().build();
  private final BenchSceneCheck check =
      new BenchSceneCheck(new SceneChecker(mapper), new DefaultGeometryEngine(mapper));

  private JsonNode resource(String path) throws Exception {
    try (InputStream in = RoundSolidSceneT05Test.class.getResourceAsStream(path)) {
      return mapper.readTree(in);
    }
  }

  private Set<String> sceneSpecCodes() throws Exception {
    Set<String> out = new TreeSet<>();
    resource("/content/scene-specs.json").properties().forEach(e -> out.add(e.getKey()));
    return out;
  }

  private boolean ok(String code, JsonNode reply) throws Exception {
    return check.ok(code, sceneSpecCodes().contains(code), reply.get("points"));
  }

  @Test
  @DisplayName("T-05 tái hiện: đầu ra thật với prompt a1 của 9 đề khối tròn trượt ràng buộc hình")
  void a1RealOutputsFail() throws Exception {
    JsonNode a1 = resource("/mvp/t05-ai-solution-round.json").get("a1");
    for (String code : FAILED_0816) {
      assertThat(ok(code, a1.get(code))).as(code + " " + a1.get(code)).isFalse();
    }
  }

  @Test
  @DisplayName("T-05 sau sửa: đầu ra thật với prompt a2 của mọi đề khối tròn thỏa ràng buộc hình")
  void a2RealOutputsPass() throws Exception {
    JsonNode a2 = resource("/mvp/t05-ai-solution-round.json").get("a2");
    assertThat(a2.properties()).hasSizeGreaterThanOrEqualTo(FAILED_0816.size());
    for (var e : a2.properties()) {
      assertThat(ok(e.getKey(), e.getValue())).as(e.getKey() + " " + e.getValue()).isTrue();
    }
  }
}
