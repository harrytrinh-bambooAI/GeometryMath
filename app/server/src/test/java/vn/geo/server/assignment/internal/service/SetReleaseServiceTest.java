package vn.geo.server.assignment.internal.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Biên của "Đổi giờ" (BR-S3E4-12, QA-S3E4-04): giờ mới sau giờ máy chủ ít nhất đúng 1 phút. */
class SetReleaseServiceTest {

  @Test
  @DisplayName(
      "TC-S3E4-17 (biên chính xác) giờ máy chủ 21:00:00: 21:01:00 được nhận, 21:00:59 bị từ chối")
  void newReleaseMustBeAtLeastOneMinuteAhead() {
    Instant now = Instant.parse("2026-12-05T14:00:00Z"); // 21:00:00 giờ Việt Nam
    assertThat(SetReleaseService.validNewRelease(now, Instant.parse("2026-12-05T14:01:00Z")))
        .isTrue();
    assertThat(SetReleaseService.validNewRelease(now, Instant.parse("2026-12-05T14:00:59Z")))
        .isFalse();
    assertThat(SetReleaseService.validNewRelease(now, now)).isFalse();
    assertThat(SetReleaseService.validNewRelease(now, Instant.parse("2026-12-06T12:00:00Z")))
        .isTrue();
  }
}
