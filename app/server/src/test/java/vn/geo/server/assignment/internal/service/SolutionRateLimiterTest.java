package vn.geo.server.assignment.internal.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** FIX4a — soát E6 m7: bộ giới hạn lời giải dọn người dùng đã hết cửa sổ. Dev-E6-08. */
class SolutionRateLimiterTest {

  @Test
  @DisplayName(
      "Dev-E6-08 người dùng hết cửa sổ 10 giây được dọn khỏi bộ nhớ; còn trong cửa sổ thì giữ")
  void sweepRemovesIdleUsers() {
    SolutionRateLimiter l = new SolutionRateLimiter();
    for (int i = 0; i < 10; i++) {
      l.check(UUID.randomUUID());
    }
    assertThat(l.size()).isEqualTo(10);
    l.sweep(System.currentTimeMillis());
    assertThat(l.size()).isEqualTo(10);
    l.sweep(System.currentTimeMillis() + SolutionRateLimiter.WINDOW_MS + 1);
    assertThat(l.size()).isZero();
  }
}
