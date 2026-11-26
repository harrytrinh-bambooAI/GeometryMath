package vn.geo.server.iam;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import vn.geo.server.iam.internal.code.PasswordHasher;
import vn.geo.server.iam.internal.code.RegistrationLimiter;
import vn.geo.server.iam.internal.code.UserCodes;
import vn.geo.server.iam.internal.service.SessionRevocations;

/** Đơn vị S2-E1: dạng mã, chuẩn hóa mã gõ tay, băm mật khẩu, giới hạn đăng ký, mốc hủy phiên. */
class CodeIdentityUnitTest {

  @Test
  void userCodeShapeAndAlphabet() {
    for (int i = 0; i < 500; i++) {
      String gv = UserCodes.newUserCode(true);
      String hs = UserCodes.newUserCode(false);
      assertThat(UserCodes.isValidUserCode(gv)).isTrue();
      assertThat(gv).startsWith("GV-");
      assertThat(hs).startsWith("HS-");
      assertThat(UserCodes.newRecoveryCode())
          .matches("RC-[A-HJ-NP-Z2-9]{4}-[A-HJ-NP-Z2-9]{4}-[A-HJ-NP-Z2-9]{4}");
    }
    assertThat(UserCodes.ALPHABET).hasSize(32).doesNotContain("O", "0", "I", "1");
  }

  @Test
  void normalizesTypedCodes() {
    assertThat(UserCodes.normalizeUserCode(" hs-3f8a-2l6p ")).contains("HS-3F8A-2L6P");
    assertThat(UserCodes.normalizeUserCode("hs3f8a2l6p")).contains("HS-3F8A-2L6P");
    assertThat(UserCodes.normalizeUserCode("XX-3F8A-2L6P")).isEmpty();
    assertThat(UserCodes.normalizeUserCode(null)).isEmpty();
    assertThat(UserCodes.normalizeRecoveryCode("rc-4f7h 2k9m 6q3v")).contains("RC-4F7H-2K9M-6Q3V");
    assertThat(UserCodes.normalizeRecoveryCode("4F7H2K9M6Q3V")).contains("RC-4F7H-2K9M-6Q3V");
    assertThat(UserCodes.normalizeRecoveryCode("RC-4F7H")).isEmpty();
  }

  @Test
  void passwordHashVerifies() {
    PasswordHasher h =
        new PasswordHasher(
            1000, "pepper-0123456789abcdef0123456789".getBytes(StandardCharsets.UTF_8));
    String stored = h.hash("AnHoc#2026");
    assertThat(stored).startsWith("pbkdf2-sha256$1000$");
    assertThat(h.verify("AnHoc#2026", stored)).isTrue();
    assertThat(h.verify("anhoc#2026", stored)).isFalse(); // phân biệt hoa thường (BR-S2E1-07)
    assertThat(h.verify("AnHoc#2026", null)).isFalse();
    UUID a = UUID.randomUUID();
    UUID b = UUID.randomUUID();
    assertThat(h.recoveryHmac(a, "RC-AAAA-BBBB-CCCC"))
        .hasSize(32)
        .isNotEqualTo(h.recoveryHmac(b, "RC-AAAA-BBBB-CCCC"));
  }

  @Test
  void registrationLimiterFivePerHour() {
    Clock clock = Clock.fixed(Instant.parse("2026-11-26T08:00:00Z"), ZoneOffset.UTC);
    RegistrationLimiter l = new RegistrationLimiter(clock);
    for (int i = 0; i < 5; i++) {
      assertThat(l.blocked("ip")).isEmpty();
      l.record("ip");
    }
    assertThat(l.blocked("ip"))
        .hasValueSatisfying(d -> assertThat(d).isEqualTo(Duration.ofHours(1)));
    assertThat(l.blocked("other")).isEmpty();
    l.age("ip", Duration.ofHours(1));
    assertThat(l.blocked("ip")).isEmpty();
  }

  @Test
  void cutoffRoundsUpToNextSecond() {
    assertThat(SessionRevocations.cutoffSec(Instant.ofEpochSecond(100, 1))).isEqualTo(101);
    assertThat(SessionRevocations.cutoffSec(Instant.ofEpochSecond(100))).isEqualTo(100);
    assertThat(SessionRevocations.cutoffSec(null)).isEqualTo(Long.MIN_VALUE);
  }
}
