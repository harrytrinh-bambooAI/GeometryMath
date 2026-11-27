package vn.geo.server.aigateway.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.geo.server.tracking.internal.classboard.domain.StuckDigest;

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

  @Test
  @DisplayName(
      "PO-83 class_stuck: StuckDigest.parse đọc được đúng định dạng prompt hệ thống yêu cầu")
  void parseReadsTheFormatThePromptAsks() {
    String line =
        AnthropicPrompts.CLASS_STUCK
            .lines()
            .filter(l -> l.startsWith("Trả: "))
            .findFirst()
            .orElseThrow();
    // Điền chỗ giữ chỗ của mẫu trong prompt bằng giá trị cụ thể rồi cho parse đọc.
    String sample =
        line.substring("Trả: ".length())
            .replace("<số bước>", "1")
            .replace("<một câu>", "Nhiều em dừng ở bước dựng hình phụ.")
            .replace("<số thứ tự bài làm>", "1,2");
    var out = StuckDigest.parse(sample, 3);
    assertThat(out).isNotNull().hasSize(1);
    assertThat(out.getFirst().step()).isEqualTo(1);
    assertThat(out.getFirst().works()).containsExactly(1, 2);
    assertThat(out.getFirst().text()).contains("dựng hình phụ");
  }
}
