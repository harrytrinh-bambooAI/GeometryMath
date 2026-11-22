package vn.geo.server.iam.internal.service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.error.Messages;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.common.security.TokenService;
import vn.geo.server.iam.internal.repo.AuthRepository;

/**
 * Vào lớp của học sinh (US-GEO-101) và đăng nhập Giáo viên mẫu (US-GEO-102).
 *
 * <p>Mã giáo viên không bao giờ được lưu hay ghi nhật ký: chỉ HMAC-SHA256(pepper, mã) đi xuống CSDL
 * (D-GEO-152); IP cũng chỉ đi dưới dạng HMAC. Mã sai, đã thu hồi, quá hạn cùng một câu MSG-GEO-12
 * (BR-GEO-285). Quá 10 lần sai/15 phút theo IP hoặc theo tài khoản: RATE_LIMITED + MSG-GEO-59
 * (BR-GEO-286, D-GEO-185). Học sinh không có giới hạn thử sai riêng (D-GEO-107).
 */
@Service
public class AuthService {

  public record Session(
      String token, String role, UUID userId, String displayName, String className) {}

  public record ClassItem(UUID id, String name, String level) {}

  private static final int NAME_MAX = 60;
  private static final Duration RETRY_AFTER = Duration.ofMinutes(15);

  private final AuthRepository repo;
  private final TokenService tokens;
  private final byte[] pepper;

  public AuthService(
      AuthRepository repo, TokenService tokens, @Value("${geo.auth.code-pepper}") String pepper) {
    if (pepper == null || pepper.length() < 32) {
      throw new IllegalArgumentException("geo.auth.code-pepper phải dài tối thiểu 32 ký tự");
    }
    this.repo = repo;
    this.tokens = tokens;
    this.pepper = pepper.getBytes(StandardCharsets.UTF_8);
  }

  public Session studentJoin(String displayName, String joinCode) {
    String name = NameKey.clean(displayName);
    if (name.isEmpty()) {
      throw AppException.invalidField("displayName", Messages.MSG_09);
    }
    if (name.codePointCount(0, name.length()) > NAME_MAX) {
      throw AppException.invalidField("displayName", Messages.MSG_10);
    }
    String code = joinCode == null ? "" : joinCode.strip().toUpperCase(Locale.ROOT);
    if (!code.matches("[A-Z2-9]{6}")) {
      throw AppException.invalidField("joinCode", Messages.MSG_11);
    }
    AuthRepository.StudentLogin r = repo.studentLogin(code, NameKey.of(name), name, Uuid7.next());
    if (!"OK".equals(r.status())) {
      throw AppException.invalidField("joinCode", Messages.MSG_11);
    }
    String token = tokens.issue(new AuthPrincipal(r.orgId(), r.userId(), Role.STUDENT));
    return new Session(
        token,
        Role.STUDENT.name(),
        r.userId(),
        repo.displayName(r.orgId(), r.userId()).orElse(name),
        repo.className(r.orgId(), r.classId()).orElse(""));
  }

  public Session teacherLogin(String rawCode, String clientIp) {
    String code = rawCode == null ? "" : rawCode.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
    String selector = code.length() >= 4 ? code.substring(0, 4) : code;
    AuthRepository.TeacherLogin r = repo.teacherLogin(selector, hmac(code), hmac("ip:" + clientIp));
    switch (r.status()) {
      case "OK" -> {
        String token = tokens.issue(new AuthPrincipal(r.orgId(), r.userId(), Role.TEACHER));
        return new Session(
            token,
            Role.TEACHER.name(),
            r.userId(),
            repo.displayName(r.orgId(), r.userId()).orElse(""),
            null);
      }
      case "RATE_LIMITED" -> throw AppException.rateLimited(RETRY_AFTER, Messages.MSG_59);
      default -> throw AppException.invalidField("code", Messages.MSG_12);
    }
  }

  public List<ClassItem> myClasses() {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    return repo.teacherClasses(p.userId()).stream()
        .map(c -> new ClassItem(c.id(), c.name(), c.level()))
        .toList();
  }

  public int leaveClass(String joinCode, String displayName) {
    return repo.leaveClass(
        joinCode == null ? "" : joinCode.strip().toUpperCase(Locale.ROOT), NameKey.of(displayName));
  }

  public int resetAuthFailures() {
    return repo.resetAuthFailures();
  }

  private byte[] hmac(String data) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(pepper, "HmacSHA256"));
      return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    } catch (java.security.GeneralSecurityException e) {
      throw new AppException(ErrorCode.INTERNAL, ErrorCode.INTERNAL.defaultMessage(), e);
    }
  }
}
