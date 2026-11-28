package vn.geo.server.exam.draft;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import vn.geo.server.exam.internal.draft.service.EngineAnswer;

/** US-S5E2-06: ô đáp số của đề nháp hiện giá trị engine ở dạng đọc được (T-261128-63). */
class EngineAnswerDraftTest {

  @Test
  void lengthsBecomeSurdForms() {
    assertThat(EngineAnswer.format(Math.sqrt(6) / 3, "dist_point_plane", true)).isEqualTo("a√6/3");
    assertThat(EngineAnswer.format(Math.sqrt(2) / 2, "dist_point_plane", true)).isEqualTo("a√2/2");
    assertThat(EngineAnswer.format(Math.sqrt(3) / 2, "dist_point_plane", true)).isEqualTo("a√3/2");
    assertThat(EngineAnswer.format(2 * Math.sqrt(5) / 5, "dist_point_plane", true))
        .isEqualTo("2a√5/5");
    assertThat(EngineAnswer.format(1.0, "length", true)).isEqualTo("a");
    assertThat(EngineAnswer.format(3.0, "length", false)).isEqualTo("3");
    assertThat(EngineAnswer.format(1.0, "length", false)).isEqualTo("1");
  }

  @Test
  void anglesVolumesAndFallback() {
    assertThat(EngineAnswer.format(45.0, "angle_line_plane", true)).isEqualTo("45°");
    assertThat(EngineAnswer.format(Math.sqrt(2) / 6, "pyramid_volume", true)).isEqualTo("a³√2/6");
    assertThat(EngineAnswer.format(Math.PI, "length", false)).isEqualTo("3,1416");
    assertThat(EngineAnswer.format(Double.NaN, "length", false)).isNull();
  }
}
