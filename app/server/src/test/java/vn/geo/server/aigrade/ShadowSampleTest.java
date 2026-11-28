package vn.geo.server.aigrade;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.geo.server.aigrade.internal.shadow.service.ShadowPolicy;
import vn.geo.server.common.id.Uuid7;

/**
 * Lấy mẫu chạy song song sau khi bật AI đề xuất (QS5-24; T-261128-62): cố định theo mã bài, ~pct %.
 */
class ShadowSampleTest {

  @Test
  @DisplayName("QS5-24 lấy mẫu 10 % bài nộp, cố định theo mã bài; 0 % thì không lấy")
  void samplesAboutTenPercentDeterministically() {
    int hit = 0;
    int n = 20_000;
    for (int i = 0; i < n; i++) {
      UUID id = Uuid7.next();
      boolean s = ShadowPolicy.inSample(id, 10);
      assertThat(ShadowPolicy.inSample(id, 10)).isEqualTo(s);
      assertThat(ShadowPolicy.inSample(id, 0)).isFalse();
      assertThat(ShadowPolicy.inSample(id, 100)).isTrue();
      if (s) {
        hit++;
      }
    }
    assertThat(hit / (double) n).isBetween(0.08, 0.12);
  }
}
