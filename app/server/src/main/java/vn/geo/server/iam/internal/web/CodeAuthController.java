package vn.geo.server.iam.internal.web;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.security.RequestContext;
import vn.geo.server.common.web.ClientIp;
import vn.geo.server.common.web.NoStore;
import vn.geo.server.iam.internal.service.CodeAuthService;

/**
 * S2-E1: đăng ký bằng tên + vai, đăng nhập bằng mã + mật khẩu, khôi phục bằng mã khôi phục, văn bản
 * riêng tư, thông tin tài khoản của chính mình. Thân yêu cầu chỉ nhận các trường khai báo dưới đây
 * (PO-61, PO-64: không email, số điện thoại, ngày sinh, trường, ảnh).
 */
@RestController
public class CodeAuthController {

  public record Register(
      String displayName,
      String role,
      String password,
      Boolean privacyAck,
      String privacyVersion) {}

  public record Login(String userCode, String password) {}

  public record FirstPassword(String userCode, String password, String newPassword) {}

  public record Recover(String userCode, String recoveryCode, String newPassword) {}

  private final CodeAuthService auth;
  private final ClientIp clientIp;

  public CodeAuthController(CodeAuthService auth, ClientIp clientIp) {
    this.auth = auth;
    this.clientIp = clientIp;
  }

  @NoStore
  @PostMapping("/api/v1/auth/register")
  public CodeAuthService.Registered register(@RequestBody Register b, HttpServletRequest req) {
    return auth.register(
        b.displayName(),
        b.role(),
        b.password(),
        b.privacyAck(),
        b.privacyVersion(),
        clientIp.of(req));
  }

  @NoStore
  @PostMapping("/api/v1/auth/code-login")
  public CodeAuthService.CodeSession login(@RequestBody Login b) {
    return auth.login(b.userCode(), b.password());
  }

  @NoStore
  @PostMapping("/api/v1/auth/first-password")
  public CodeAuthService.CodeSession firstPassword(@RequestBody FirstPassword b) {
    return auth.firstPassword(b.userCode(), b.password(), b.newPassword());
  }

  @NoStore
  @PostMapping("/api/v1/auth/recover")
  public CodeAuthService.Recovered recover(@RequestBody Recover b) {
    return auth.recover(b.userCode(), b.recoveryCode(), b.newPassword());
  }

  /** Văn bản thông báo riêng tư hiện hành (tick "đã đọc" ở màn Đăng ký trỏ tới văn bản này). */
  @GetMapping("/api/v1/auth/privacy-notice")
  public CodeAuthService.Notice notice() {
    return auth.notice();
  }

  /**
   * Tài khoản của chính mình: mã người dùng, tên, vai, cờ "đã có mã khôi phục" (không bao giờ là
   * mã).
   */
  @NoStore
  @GetMapping("/api/v1/account/me")
  public CodeAuthService.Me me() {
    return auth.me(RequestContext.principal());
  }

  /** Móc kiểm thử S2-E1 (chỉ dev/test, không có ở prod). */
  @RestController
  @Profile({"dev", "test"})
  public static class CodeAuthTestHooks {
    private final CodeAuthService auth;

    public CodeAuthTestHooks(CodeAuthService auth) {
      this.auth = auth;
    }

    public record Age(String ip, long seconds) {}

    /**
     * Giả lập đã chờ đủ {@code seconds} cho bộ đếm đăng ký của một IP (BV7: "chờ theo
     * Retry-After").
     */
    @PostMapping("/test-hooks/s2e1/register-limit/age")
    public Map<String, Object> age(@RequestBody Age b) {
      auth.limiter().age(auth.hasher().ipKey(b.ip()), Duration.ofSeconds(b.seconds()));
      return Map.of("ok", true);
    }

    public record Temp(String userCode, String tempPassword) {}

    /**
     * Giả lập tài khoản vừa chuyển từ MVP: mật khẩu tạm + phải đặt mật khẩu mới ở lần đăng nhập
     * đầu.
     */
    @PostMapping("/test-hooks/s2e1/temp-password")
    public Map<String, Object> temp(@RequestBody Temp b) {
      return Map.of("updated", auth.tempPasswordForTest(b.userCode(), b.tempPassword()));
    }

    public record Revoke(String userCode) {}

    /** Giả lập phiên hết hạn ở máy chủ (US-S2E1-02-FC5): hủy mọi thẻ hiện có của mã người dùng. */
    @PostMapping("/test-hooks/s2e1/revoke-sessions")
    public Map<String, Object> revoke(@RequestBody Revoke b) {
      return Map.of("revoked", auth.revokeSessionsForTest(b.userCode()));
    }
  }
}
