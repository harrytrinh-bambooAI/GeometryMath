package vn.geo.server.common.dev.web;

import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.TokenService;

/** CHỈ profile "dev": phát thẻ tùy ý để thử bằng curl. Không bao giờ bật ở môi trường thật. */
@RestController
@Profile("dev")
public class DevTokenController {

  private final TokenService tokens;

  public DevTokenController(TokenService tokens) {
    this.tokens = tokens;
  }

  /** ttlSeconds: tùy chọn; âm = thẻ đã hết hạn (ca phiên hết hạn, US-GEO-101-FC4). */
  public record DevTokenRequest(UUID orgId, UUID userId, Role role, Long ttlSeconds) {}

  @PostMapping("/dev/token")
  public Map<String, String> token(@RequestBody DevTokenRequest r) {
    AuthPrincipal p = new AuthPrincipal(r.orgId(), r.userId(), r.role());
    return Map.of(
        "token",
        r.ttlSeconds() == null
            ? tokens.issue(p)
            : tokens.issue(p, java.time.Duration.ofSeconds(r.ttlSeconds())));
  }
}
