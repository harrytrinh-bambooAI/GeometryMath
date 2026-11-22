package vn.geo.server.common.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.common.id.Uuid7;

class TokenServiceTest {

  private static final String SECRET = "0123456789abcdef0123456789abcdef";
  private final JsonMapper mapper = JsonMapper.builder().build();
  private final Clock clock = Clock.fixed(Instant.parse("2026-05-01T00:00:00Z"), ZoneOffset.UTC);
  private final TokenService svc =
      new TokenService(new AuthProperties(SECRET, Duration.ofHours(1)), mapper, clock);
  private final AuthPrincipal who = new AuthPrincipal(Uuid7.next(), Uuid7.next(), Role.TEACHER);

  @Test
  void roundTrip() {
    assertThat(svc.verify(svc.issue(who))).contains(who);
  }

  @Test
  void rejectsExpired() {
    String token = svc.issue(who);
    Clock later = Clock.fixed(clock.instant().plus(Duration.ofHours(1)), ZoneOffset.UTC);
    var expired = new TokenService(new AuthProperties(SECRET, Duration.ofHours(1)), mapper, later);
    assertThat(expired.verify(token)).isEmpty();
  }

  @Test
  void rejectsWrongSecret() {
    var other =
        new TokenService(new AuthProperties("x".repeat(32), Duration.ofHours(1)), mapper, clock);
    assertThat(other.verify(svc.issue(who))).isEmpty();
  }

  @Test
  void rejectsTamperedPayload() {
    String[] p = svc.issue(who).split("\\.");
    String forged =
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(
                ("{\"sub\":\""
                        + UUID.randomUUID()
                        + "\",\"org\":\""
                        + UUID.randomUUID()
                        + "\",\"role\":\"AGENCY_ADMIN\",\"exp\":9999999999}")
                    .getBytes());
    assertThat(svc.verify(p[0] + "." + forged + "." + p[2])).isEmpty();
  }

  @Test
  void rejectsAlgNoneAndGarbage() {
    String none =
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString("{\"alg\":\"none\",\"typ\":\"JWT\"}".getBytes());
    String payload = svc.issue(who).split("\\.")[1];
    assertThat(svc.verify(none + "." + payload + ".")).isEmpty();
    assertThat(svc.verify("a.b")).isEmpty();
    assertThat(svc.verify("")).isEmpty();
    assertThat(svc.verify("a.b.c.d")).isEmpty();
  }

  @Test
  void shortSecretRefused() {
    assertThatThrownBy(() -> new AuthProperties("short", null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void tenantContextComesOnlyFromBoundPrincipal() throws Exception {
    assertThatThrownBy(TenantContext::orgId).isInstanceOf(IllegalStateException.class);
    UUID org = RequestContext.callAs(who, TenantContext::orgId);
    assertThat(org).isEqualTo(who.orgId());
    assertThat(RequestContext.isAuthenticated()).isFalse();
  }
}
