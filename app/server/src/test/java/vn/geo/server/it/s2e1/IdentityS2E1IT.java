package vn.geo.server.it.s2e1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.security.TokenService;
import vn.geo.server.iam.internal.code.PasswordHasher;
import vn.geo.server.iam.internal.code.RegistrationLimiter;
import vn.geo.server.iam.internal.code.UserCodes;
import vn.geo.server.iam.internal.repo.CodeAccountRepository;
import vn.geo.server.iam.internal.service.CodeAuthService;
import vn.geo.server.iam.internal.service.SessionRevocations;
import vn.geo.server.it.e1.AbstractE1IT;

/** S2-E1 (PO-61): mã người dùng không trùng, mốc hết khóa 15 phút, khôi phục hủy phiên khác. */
class IdentityS2E1IT extends AbstractE1IT {

  @Autowired CodeAuthService auth;
  @Autowired CodeAccountRepository repo;
  @Autowired SessionRevocations revocations;
  @Autowired TokenService tokenService;
  @Autowired TransactionTemplate tx;

  @Value("${geo.auth.code-pepper}")
  String pepper;

  @Value("${geo.s2e1.community-org-id}")
  UUID community;

  /** Đồng hồ giả lập cho ca đo mốc khóa (giờ máy chủ, BV3). */
  static final class MutableClock extends Clock {
    private Instant now;

    MutableClock(Instant start) {
      this.now = start;
    }

    void set(Instant t) {
      now = t;
    }

    @Override
    public ZoneId getZone() {
      return ZoneId.of("UTC");
    }

    @Override
    public Clock withZone(ZoneId zone) {
      return this;
    }

    @Override
    public Instant instant() {
      return now;
    }
  }

  private CodeAuthService serviceWith(Clock clock) {
    PasswordHasher cheap = new PasswordHasher(1, pepper.getBytes(StandardCharsets.UTF_8));
    return new CodeAuthService(
        repo,
        tokenService,
        cheap,
        new RegistrationLimiter(clock),
        revocations,
        tx,
        clock,
        community);
  }

  @Test
  @DisplayName("TC-S2E1-017 Đăng ký 1000 tài khoản: 1000 mã khác nhau, đúng dạng, không O 0 I 1")
  void tc_S2E1_017_codesNeverCollide() {
    CodeAuthService svc = serviceWith(Clock.systemUTC());
    Set<String> codes = new HashSet<>();
    List<UUID> ids = new ArrayList<>();
    try {
      for (int i = 0; i < 1000; i++) {
        // mỗi IP một lần (giới hạn 5/giờ/IP không phải đối tượng của ca này)
        CodeAuthService.Registered r =
            svc.register(
                "Khối " + i,
                i % 2 == 0 ? "teacher" : "student",
                "MatKhau#" + i,
                true,
                "1.0",
                "10.17." + (i / 250) + "." + (i % 250));
        codes.add(r.userCode());
        ids.add(r.userId());
        assertThat(UserCodes.isValidUserCode(r.userCode())).as(r.userCode()).isTrue();
        assertThat(r.userCode().substring(3)).doesNotContain("O", "0", "I", "1");
        assertThat(r.userCode()).startsWith(i % 2 == 0 ? "GV-" : "HS-");
      }
      assertThat(codes).hasSize(1000);
      long inDb =
          jdbc.sql("SELECT count(DISTINCT user_code) FROM app_user WHERE id = ANY(:ids)")
              .param("ids", ids.toArray(new UUID[0]))
              .query(Long.class)
              .single();
      assertThat(inDb).isEqualTo(1000);
    } finally {
      deleteUsers(ids);
    }
  }

  @Test
  @DisplayName(
      "TC-S2E1-026 Khóa 15 phút: T0+14:59 vẫn khóa, T0+15:00 vào được (giờ máy chủ giả lập)")
  void tc_S2E1_026_lockEndsAtFifteenMinutes() {
    MutableClock clock = new MutableClock(Instant.now().truncatedTo(ChronoUnit.MILLIS));
    CodeAuthService svc = serviceWith(clock);
    CodeAuthService.Registered r =
        svc.register("An khóa", "student", "AnHoc#2026", true, "1.0", "10.26.0.1");
    try {
      for (int i = 1; i <= 9; i++) {
        assertThatThrownBy(() -> svc.login(r.userCode(), "sai-mat-khau"))
            .isInstanceOfSatisfying(
                AppException.class,
                e -> assertThat(e.code()).isEqualTo(ErrorCode.VALIDATION_FAILED));
      }
      Instant t0 = clock.instant();
      assertThatThrownBy(() -> svc.login(r.userCode(), "sai-mat-khau"))
          .isInstanceOfSatisfying(
              AppException.class,
              e -> {
                assertThat(e.code()).isEqualTo(ErrorCode.RATE_LIMITED);
                assertThat(e.getMessage()).isEqualTo("Tài khoản tạm khóa, thử lại sau 15 phút");
              });
      clock.set(t0.plus(Duration.ofMinutes(14)).plusSeconds(59));
      assertThatThrownBy(() -> svc.login(r.userCode(), "AnHoc#2026"))
          .isInstanceOfSatisfying(
              AppException.class, e -> assertThat(e.code()).isEqualTo(ErrorCode.RATE_LIMITED));
      clock.set(t0.plus(Duration.ofMinutes(15)));
      CodeAuthService.CodeSession s = svc.login(r.userCode(), "AnHoc#2026");
      assertThat(s.token()).isNotBlank();
      assertThat(s.userId()).isEqualTo(r.userId());
    } finally {
      deleteUsers(List.of(r.userId()));
    }
  }

  @Test
  @DisplayName("TC-S2E1-038 Khôi phục ở máy A hủy phiên đang mở ở máy B (UNAUTHENTICATED)")
  void tc_S2E1_038_recoveryRevokesOtherSessions() throws Exception {
    CodeAuthService.Registered r =
        auth.register("Cô Trang IT", "teacher", "Trang@2026x", true, "1.0", "10.38.0.1");
    try {
      String machineB = auth.login(r.userCode(), "Trang@2026x").token();
      assertThat(get("/api/v1/account/me", machineB).statusCode()).isEqualTo(200);

      var rec =
          post(
              "/api/v1/auth/recover",
              null,
              mapper.writeValueAsString(
                  java.util.Map.of(
                      "userCode", r.userCode(),
                      "recoveryCode", r.recoveryCode(),
                      "newPassword", "Trang@2027y")));
      assertThat(rec.statusCode()).as(rec.body()).isEqualTo(200);
      assertThat(json(rec).get("recoveryCode").asString()).isNotEqualTo(r.recoveryCode());

      var afterB = get("/api/v1/account/me", machineB);
      assertThat(afterB.statusCode()).isEqualTo(401);
      assertThat(json(afterB).get("code").asString()).isEqualTo("UNAUTHENTICATED");

      String fresh = auth.login(r.userCode(), "Trang@2027y").token();
      var me = get("/api/v1/account/me", fresh);
      assertThat(me.statusCode()).isEqualTo(200);
      JsonNode body = json(me);
      assertThat(body.get("userCode").asString()).isEqualTo(r.userCode());
      assertThat(body.get("hasRecoveryCode").asBoolean()).isTrue();
      // Không dò "RC-" trơn: mã người dùng ngẫu nhiên có thể chứa "RC-" (xác suất 1/1024 mỗi mã).
      assertThat(me.body()).doesNotContainPattern(MigrationS2E1IT.RECOVERY_CODE);
    } finally {
      deleteUsers(List.of(r.userId()));
    }
  }

  private void deleteUsers(List<UUID> ids) {
    if (ids.isEmpty()) {
      return;
    }
    jdbc.sql("DELETE FROM app_user WHERE id = ANY(:ids)")
        .param("ids", ids.toArray(new UUID[0]))
        .update();
  }
}
