package vn.geo.server.notify.internal.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** PO-69: MSG-S3-95 không lặp "Bộ " khi tên bộ đã bắt đầu bằng "Bộ " (US-S3E6-03-SC1). */
class DueSoonMessageTest {

  private static final Instant DUE = Instant.parse("2026-12-04T13:00:00Z"); // 20:00 giờ VN

  @Test
  @DisplayName(
      "PO-69 MSG-S3-95: tên bắt đầu bằng \"Bộ \" thì không thêm \"Bộ \"; tên khác thì thêm")
  void noDoubleBo() {
    assertThat(NotificationService.dueSoon("Bộ bài tuần 15", DUE))
        .isEqualTo("Bộ bài tuần 15 sắp hết hạn: 20:00, 04/12/2026");
    assertThat(NotificationService.dueSoon("bộ ôn chương 2", DUE))
        .isEqualTo("bộ ôn chương 2 sắp hết hạn: 20:00, 04/12/2026");
    assertThat(NotificationService.dueSoon("Ôn tập chương 2", DUE))
        .isEqualTo("Bộ Ôn tập chương 2 sắp hết hạn: 20:00, 04/12/2026");
    assertThat(NotificationService.dueSoon("Bộđề", DUE)).startsWith("Bộ Bộđề ");
    assertThat(NotificationService.dueSoon("Bộ bài tuần 15", DUE)).doesNotContain("Bộ Bộ");
  }
}
