package vn.geo.server.geometry.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.geometry.api.GeometryEngine;

/**
 * Câu mở sau FIX T-05: bộ dựng "sphere" đặt tên tâm O (điểm trên mặt cầu M); đề tự đặt tâm I (điểm
 * trên mặt cầu A) trước đây trượt kiểm hình vì "không có đỉnh I". Ánh xạ tên lạ về mặt cầu của
 * hình.
 */
class SphereCenterNameTest {

  private final JsonMapper mapper = JsonMapper.builder().build();
  private final DefaultGeometryEngine engine = new DefaultGeometryEngine(mapper);

  private GeometryEngine.ProblemSpec spec(String center, String onSphere) {
    return engine.specFrom(
        mapper.readTree(
            """
            {"template":"sphere","params":{"r":"3"},"unit":"cm",
             "asked":[{"name":"R","kind":"sphere_radius","points":["%s","%s"]},
                      {"name":"V","kind":"sphere_volume","points":["%s","%s"]}]}
            """
                .formatted(center, onSphere, center, onSphere)));
  }

  @Test
  @DisplayName("Đề cầu tâm I (điểm A trên mặt cầu): dựng, kiểm hình và kiểm đáp số đạt như tâm O")
  void sphereWithCenterI() {
    GeometryEngine.Verification v = engine.verify(spec("I", "A"), "R = 3 cm; V = 36π cm³");
    assertThat(v.sceneOk()).isTrue();
    assertThat(v.answerOk()).isTrue();
    GeometryEngine.Verification wrong = engine.verify(spec("I", "A"), "R = 3 cm; V = 30π cm³");
    assertThat(wrong.answerOk()).isFalse();
    assertThat(engine.signature(spec("I", "A")).orElseThrow().canonical())
        .isEqualTo(engine.signature(spec("O", "M")).orElseThrow().canonical());
    assertThat(engine.verify(spec("I", "M"), "R = 3 cm; V = 36π cm³").answerOk()).isTrue();
  }
}
