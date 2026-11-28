package vn.geo.server.aigateway.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.geometry.api.GeometryEngine;
import vn.geo.server.geometry.internal.DefaultGeometryEngine;

/**
 * T-05: prompt "solution" của bộ đề chuẩn phải nêu tên MỌI điểm của bộ dựng khối tròn (tên phép
 * kiểm ràng buộc dùng), lấy từ chính engine để prompt và bộ dựng không lệch nhau.
 */
class AnthropicPromptsRoundTest {

  @Test
  @DisplayName("T-05 prompt a2 nêu tên mặc định của mọi điểm bộ dựng nón, trụ, cầu")
  void roundTemplatePointNamesInBenchmarkPrompt() {
    GeometryEngine engine = new DefaultGeometryEngine(JsonMapper.builder().build());
    String prompt = AnthropicPrompts.system("solution", "Mã bài: X\nĐề bài: …", "");
    assertThat(prompt).isEqualTo(AnthropicPrompts.SOLUTION_BENCHMARK);
    String rule = prompt.substring(prompt.indexOf("Hình tròn xoay và mặt cầu"));
    for (String t : List.of("cone", "cone_rl", "cylinder", "sphere", "sphere_circum_cube")) {
      Map<String, Double> p = new LinkedHashMap<>();
      for (String k : engine.paramKeys(t)) {
        p.put(k, "l".equals(k) ? 2.0 : 1.0);
      }
      for (String name : engine.build(t, p).points().keySet()) {
        if (!"sphere_circum_cube".equals(t) || "I".equals(name)) {
          assertThat(rule).as(t + " " + name).contains(name + " (");
        }
      }
    }
    assertThat(AnthropicPrompts.VERSION).isEqualTo("a2");
  }
}
