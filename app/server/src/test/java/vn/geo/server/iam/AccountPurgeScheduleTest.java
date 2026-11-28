package vn.geo.server.iam;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import vn.geo.server.iam.internal.service.AccountPurgeJob;

/**
 * Dev-REV-A3 (bản soát T-20…T-32 mục 4): việc dọn tài khoản đã xóa chạy theo lịch cron giờ cố định
 * (mặc định 03:00 giờ VN), cấu hình được, không phụ thuộc giờ khởi động máy chủ.
 */
class AccountPurgeScheduleTest {

  private static final ZoneId VN = ZoneId.of("Asia/Ho_Chi_Minh");

  @Test
  @DisplayName("Dev-REV-A3 Lịch dọn là cron cấu hình được, mặc định 03:00 giờ VN")
  void cronIsConfigurableDefault3amVietnam() throws Exception {
    Scheduled s = AccountPurgeJob.class.getMethod("runOnce").getAnnotation(Scheduled.class);
    assertThat(s).isNotNull();
    assertThat(s.cron()).isEqualTo("${geo.s3e5.account-purge-cron:0 0 3 * * *}");
    assertThat(s.zone()).isEqualTo("Asia/Ho_Chi_Minh");
    assertThat(s.fixedDelay()).isEqualTo(-1);
    assertThat(s.initialDelay()).isEqualTo(-1);
    String yml = Files.readString(Path.of("src/main/resources/application.yml"));
    assertThat(yml)
        .contains("account-purge-cron: ${GEO_ACCOUNT_PURGE_CRON:0 0 3 * * *}")
        .doesNotContain("account-purge-interval");
  }

  @Test
  @DisplayName("Dev-REV-A3 Máy chủ khởi động lại mỗi ngày vẫn chạy dọn trong vòng 24 giờ")
  void dailyRestartStillRunsWithin24h() {
    CronExpression cron = CronExpression.parse(AccountPurgeJob.DEFAULT_CRON);
    // khởi động lại mỗi ngày lúc 02:59, 03:00:01, 14:00 giờ VN: lần chạy kế tiếp luôn ≤ 24 giờ sau
    for (LocalDateTime boot :
        new LocalDateTime[] {
          LocalDateTime.of(2026, 11, 27, 2, 59),
          LocalDateTime.of(2026, 11, 27, 3, 0, 1),
          LocalDateTime.of(2026, 11, 27, 14, 0)
        }) {
      ZonedDateTime at = boot.atZone(VN);
      ZonedDateTime next = cron.next(at);
      assertThat(next).isNotNull();
      assertThat(next.getHour()).isEqualTo(3);
      assertThat(next.getMinute()).isZero();
      assertThat(java.time.Duration.between(at, next))
          .isLessThanOrEqualTo(java.time.Duration.ofHours(24));
    }
  }
}
