package vn.geo.server.aigateway.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** PO-83: task "class_stuck" có prompt hệ thống riêng (không rơi về prompt chung) và trả JSON. */
class ClassStuckPromptTest {

  @Test
  @DisplayName("PO-83 class_stuck: prompt hệ thống riêng, đòi JSON insights, cấm tên/điểm/đáp số")
  void classStuckHasOwnSystemPrompt() {
    String sys = AnthropicPrompts.system("class_stuck", "Nhiệm vụ: gộp…", "");
    assertThat(sys).isEqualTo(AnthropicPrompts.CLASS_STUCK);
    assertThat(sys).isNotEqualTo(AnthropicPrompts.system("khong-co", "", ""));
    assertThat(sys)
        .contains("{\"insights\":[{\"step\"")
        .contains("Không nêu tên")
        .contains("không xếp hạng")
        .contains("không nêu")
        .contains("DỮ LIỆU ẩn danh");
    assertThat(AnthropicPrompts.expectsJson("class_stuck")).isTrue();
    assertThat(AnthropicPrompts.VERSION).isEqualTo("a2");
  }
}
