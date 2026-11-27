package vn.geo.server.assignment.internal.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** T-05 [minor]: ghi chú báo cáo bộ câu thử theo nhà cung cấp AI thật hay giả lập. */
class ProbeRunnerNoteTest {

  @Test
  @DisplayName("T-05 note: mock ghi 'giả lập', anthropic ghi 'AI thật (anthropic)'")
  void noteByProvider() {
    assertThat(ProbeRunner.note("mock")).isEqualTo(ProbeRunner.NOTE);
    assertThat(ProbeRunner.note("anthropic"))
        .contains("AI thật (anthropic)")
        .doesNotContain("giả lập");
  }
}
