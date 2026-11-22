package vn.geo.server.common.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Bộ phát và kiểm thẻ JWT HS256 (chỉ nhận đúng alg=HS256). Claim: sub=userId, org=orgId, role, iat,
 * exp. Đây là khung; luồng đăng nhập thật thuộc module iam.
 */
@Component
public class TokenService {

  private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();
  private static final String HEADER =
      B64.encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));

  private final byte[] key;
  private final AuthProperties props;
  private final JsonMapper mapper;
  private final Clock clock;

  public TokenService(AuthProperties props, JsonMapper mapper, Clock clock) {
    this.key = props.secret().getBytes(StandardCharsets.UTF_8);
    this.props = props;
    this.mapper = mapper;
    this.clock = clock;
  }

  public String issue(AuthPrincipal p) {
    return issue(p, props.ttl());
  }

  /** Phát thẻ với thời hạn riêng (profile dev/test dùng để tạo thẻ sắp hết hoặc đã hết hạn). */
  public String issue(AuthPrincipal p, java.time.Duration ttl) {
    long now = clock.instant().getEpochSecond();
    Map<String, Object> claims =
        Map.of(
            "sub", p.userId().toString(),
            "org", p.orgId().toString(),
            "role", p.role().name(),
            "iat", now,
            "exp", now + ttl.toSeconds());
    String payload = B64.encodeToString(mapper.writeValueAsBytes(claims));
    String signingInput = HEADER + "." + payload;
    return signingInput + "." + B64.encodeToString(hmac(signingInput));
  }

  /** Trả rỗng nếu thẻ sai định dạng, sai chữ ký, hết hạn hoặc thiếu claim. */
  public Optional<AuthPrincipal> verify(String token) {
    try {
      String[] parts = token.split("\\.", -1);
      if (parts.length != 3 || !HEADER.equals(parts[0])) {
        return Optional.empty();
      }
      byte[] given = Base64.getUrlDecoder().decode(parts[2]);
      if (!MessageDigest.isEqual(given, hmac(parts[0] + "." + parts[1]))) {
        return Optional.empty();
      }
      Map<?, ?> claims = mapper.readValue(Base64.getUrlDecoder().decode(parts[1]), Map.class);
      long exp = ((Number) claims.get("exp")).longValue();
      if (clock.instant().getEpochSecond() >= exp) {
        return Optional.empty();
      }
      return Optional.of(
          new AuthPrincipal(
              UUID.fromString((String) claims.get("org")),
              UUID.fromString((String) claims.get("sub")),
              Role.valueOf((String) claims.get("role"))));
    } catch (RuntimeException e) {
      return Optional.empty();
    }
  }

  private byte[] hmac(String data) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(key, "HmacSHA256"));
      return mac.doFinal(data.getBytes(StandardCharsets.US_ASCII));
    } catch (java.security.GeneralSecurityException e) {
      throw new IllegalStateException(e);
    }
  }
}
