package vn.geo.server.it.s3e5;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import vn.geo.server.iam.internal.service.AuthFailurePurgeJob;
import vn.geo.server.it.e1.AbstractE1IT;

/** PO-71: hạn lưu auth_failure.ip_key (HMAC của IP) tối đa 1 giờ, dọn xóa theo giờ. */
class AuthFailurePurgeS3E5IT extends AbstractE1IT {

  @Autowired AuthFailurePurgeJob job;

  private byte[] key() {
    byte[] k = new byte[32];
    new SecureRandom().nextBytes(k);
    return k;
  }

  private void failAt(byte[] ip, int minutesAgo) {
    jdbc.sql("INSERT INTO auth_failure (at, ip_key) VALUES (:at, :ip)")
        .param("at", OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(minutesAgo))
        .param("ip", ip)
        .update();
  }

  private long rows(byte[] ip) {
    return jdbc.sql("SELECT count(*) FROM auth_failure WHERE ip_key = :ip")
        .param("ip", ip)
        .query(Long.class)
        .single();
  }

  @Test
  @DisplayName(
      "TC-S3E5-62 Công việc dọn theo giờ xóa lần thử sai (HMAC IP) cũ hơn 1 giờ, giữ hàng trong 1 giờ; chạy lặp không xóa thêm (PO-71)")
  void hourlyPurgeKeepsOnlyLastHour() {
    byte[] ip = key();
    failAt(ip, 5); // trong cửa sổ đếm 15 phút
    failAt(ip, 50); // quá 15 phút nhưng chưa quá 1 giờ
    failAt(ip, 61); // quá 1 giờ
    failAt(ip, 23 * 60); // hạn cũ 1 ngày vẫn còn: nay phải xóa
    assertThat(rows(ip)).isEqualTo(4);
    assertThat(job.runOnce()).isGreaterThanOrEqualTo(2);
    assertThat(rows(ip)).isEqualTo(2);
    job.runOnce();
    assertThat(rows(ip)).as("lũy đẳng").isEqualTo(2);
    String cron = AuthFailurePurgeJob.DEFAULT_CRON;
    assertThat(cron.split(" ")[2]).as("mỗi giờ").isEqualTo("*");
  }

  @Test
  @DisplayName(
      "TC-S3E5-62 Hàm dọn của vận hành (fn_purge_ephemeral) cũng chỉ giữ auth_failure trong 1 giờ (PO-71)")
  void opsPurgeUsesOneHour() {
    byte[] ip = key();
    failAt(ip, 10);
    failAt(ip, 90);
    jdbc.sql("SELECT auth_failure_deleted FROM fn_purge_ephemeral()").query(Long.class).single();
    assertThat(rows(ip)).isEqualTo(1);
  }
}
