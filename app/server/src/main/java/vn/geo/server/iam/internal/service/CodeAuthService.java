package vn.geo.server.iam.internal.service;

import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.springframework.transaction.support.TransactionTemplate;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.TokenService;
import vn.geo.server.iam.internal.code.PasswordHasher;
import vn.geo.server.iam.internal.code.RegistrationLimiter;
import vn.geo.server.iam.internal.code.UserCodes;
import vn.geo.server.iam.internal.repo.CodeAccountRepository;
import vn.geo.server.iam.internal.repo.CodeAccountRepository.Account;

/**
 * Định danh tối giản PO-61 (S2-E1): đăng ký bằng tên + vai + mật khẩu (US-S2E1-01), đăng nhập bằng
 * mã + mật khẩu có khóa 10 lần sai/15 phút theo tài khoản (US-S2E1-02), khôi phục bằng mã khôi phục
 * (US-S2E1-03), đặt mật khẩu mới lần đầu cho tài khoản chuyển từ MVP (US-S2E1-04-SC4). Luồng MVP
 * (mã giáo viên, vào lớp bằng tên + mã lớp) vẫn chạy song song (PLAN A7).
 *
 * <p>Bộ đếm sai được ghi trong giao dịch riêng rồi mới ném lỗi (lỗi không làm mất lần đếm). Mọi lỗi
 * đăng nhập cùng một câu, không lộ mã có tồn tại (BR-S2E1-08).
 */
public class CodeAuthService {

  public static final String MSG_LOGIN = "Mã hoặc mật khẩu chưa đúng";
  public static final String MSG_RECOVERY = "Mã người dùng hoặc mã khôi phục chưa đúng";
  public static final String MSG_NAME_EMPTY = "Hãy nhập tên hiển thị.";
  public static final String MSG_NAME_LONG = "Tên hiển thị tối đa 60 ký tự.";
  public static final String MSG_NAME_PII = "Tên hiển thị không được là email hay số điện thoại.";
  public static final String MSG_PASSWORD = "Mật khẩu từ 8 đến 64 ký tự.";
  public static final String MSG_ROLE = "Hãy chọn vai Giáo viên hoặc Học sinh.";
  public static final String MSG_PRIVACY = "Hãy đọc và tick thông báo riêng tư.";
  public static final String MSG_REG_LIMIT =
      "Mạng này đã tạo quá nhiều tài khoản trong một giờ. Vui lòng thử lại sau.";

  public static final int MAX_FAILS = 10;
  public static final Duration LOCK = Duration.ofMinutes(15);
  public static final Duration RECOVERY_WINDOW = Duration.ofMinutes(15);

  /** Kết quả đăng ký: mã khôi phục chỉ có ở phản hồi này (hiện đúng một lần, BR-S2E1-05). */
  public record Registered(
      String token,
      String role,
      UUID userId,
      String displayName,
      String userCode,
      String recoveryCode) {}

  /**
   * Phiên sau đăng nhập; {@code mustChangePassword} = true thì chưa có thẻ (phải đặt mật khẩu mới).
   */
  public record CodeSession(
      String token,
      String role,
      UUID userId,
      String displayName,
      String className,
      String userCode,
      boolean mustChangePassword) {}

  public record Recovered(String userCode, String role, String recoveryCode) {}

  private final CodeAccountRepository repo;
  private final TokenService tokens;
  private final PasswordHasher hasher;
  private final RegistrationLimiter limiter;
  private final SessionRevocations revocations;
  private final TransactionTemplate tx;
  private final Clock clock;
  private final UUID communityOrg;

  public CodeAuthService(
      CodeAccountRepository repo,
      TokenService tokens,
      PasswordHasher hasher,
      RegistrationLimiter limiter,
      SessionRevocations revocations,
      TransactionTemplate tx,
      Clock clock,
      UUID communityOrg) {
    this.repo = repo;
    this.tokens = tokens;
    this.hasher = hasher;
    this.limiter = limiter;
    this.revocations = revocations;
    this.tx = tx;
    this.clock = clock;
    this.communityOrg = communityOrg;
  }

  public PasswordHasher hasher() {
    return hasher;
  }

  public RegistrationLimiter limiter() {
    return limiter;
  }

  // ------------------------------------------------------------------ đăng ký (US-S2E1-01)

  public Registered register(
      String displayName,
      String role,
      String password,
      Boolean privacyAck,
      String privacyVersion,
      String clientIp) {
    String name = cleanName(displayName);
    boolean teacher = parseRole(role);
    checkPassword("password", password);
    String current = repo.currentNotice().map(CodeAccountRepository.Notice::version).orElse("1.0");
    if (!Boolean.TRUE.equals(privacyAck) || !current.equals(privacyVersion)) {
      throw AppException.invalidField("privacyAck", MSG_PRIVACY);
    }
    String ipKey = hasher.ipKey(clientIp == null ? "" : clientIp);
    limiter
        .blocked(ipKey)
        .ifPresent(wait -> {
          throw AppException.rateLimited(wait, MSG_REG_LIMIT);
        });
    String hash = hasher.hash(password);
    String recovery = UserCodes.newRecoveryCode();
    Instant now = clock.instant();
    for (int attempt = 0; attempt < 20; attempt++) {
      UUID id = Uuid7.next();
      String code = UserCodes.newUserCode(teacher);
      boolean ok =
          repo.insert(
              new CodeAccountRepository.NewAccount(
                  id,
                  communityOrg,
                  teacher ? "teacher" : "student",
                  teacher ? "verified" : "light",
                  name,
                  code,
                  hash,
                  hasher.recoveryHmac(id, recovery),
                  current,
                  now),
              "Cộng đồng");
      if (ok) {
        limiter.record(ipKey);
        Role r = teacher ? Role.TEACHER : Role.STUDENT;
        String token = tokens.issue(new AuthPrincipal(communityOrg, id, r));
        return new Registered(token, r.name(), id, name, code, recovery);
      }
    }
    throw new AppException(ErrorCode.CONFLICT, "Chưa tạo được mã người dùng, vui lòng thử lại.");
  }

  static String cleanName(String displayName) {
    String name = NameKey.clean(displayName);
    if (name.isEmpty()) {
      throw AppException.invalidField("displayName", MSG_NAME_EMPTY);
    }
    if (name.codePointCount(0, name.length()) > 60) {
      throw AppException.invalidField("displayName", MSG_NAME_LONG);
    }
    if (looksLikeContact(name)) {
      throw AppException.invalidField("displayName", MSG_NAME_PII);
    }
    return name;
  }

  /** BR-S2E1-16 (luật đầy đủ ở BR-S3E5-35): tên trông như email hoặc số điện thoại bị chặn. TẠM. */
  static boolean looksLikeContact(String name) {
    if (name.matches(".*\\S+@\\S+\\.\\S+.*")) {
      return true;
    }
    String digits = name.replaceAll("[\\s.()+-]", "");
    return digits.matches("\\d{9,}");
  }

  static boolean parseRole(String role) {
    String r = role == null ? "" : role.strip().toLowerCase(Locale.ROOT);
    return switch (r) {
      case "teacher" -> true;
      case "student" -> false;
      default -> throw AppException.invalidField("role", MSG_ROLE);
    };
  }

  static void checkPassword(String field, String password) {
    int n = password == null ? 0 : password.codePointCount(0, password.length());
    if (n < 8 || n > 64) {
      throw AppException.invalidField(field, MSG_PASSWORD);
    }
  }

  // ------------------------------------------------------------------ đăng nhập (US-S2E1-02)

  private sealed interface Outcome {}

  private record Ok(Account account) implements Outcome {}

  private record Fail(AppException error) implements Outcome {}

  public CodeSession login(String rawCode, String password) {
    Account a = authenticate(rawCode, password);
    if (a.mustChangePassword()) {
      return new CodeSession(
          null, roleOf(a).name(), a.id(), a.displayName(), null, a.userCode(), true);
    }
    return session(a);
  }

  /** Đặt mật khẩu mới lần đầu (tài khoản chuyển từ MVP, BR-S2E1-15) rồi vào luôn. */
  public CodeSession firstPassword(String rawCode, String password, String newPassword) {
    checkPassword("newPassword", newPassword);
    Account a = authenticate(rawCode, password);
    if (!a.mustChangePassword()) {
      throw new AppException(ErrorCode.VALIDATION_FAILED, MSG_LOGIN);
    }
    String hash = hasher.hash(newPassword);
    tx.executeWithoutResult(s -> repo.setCredentials(a.orgId(), a.id(), hash, null, null));
    return session(a);
  }

  private Account authenticate(String rawCode, String password) {
    Optional<String> code = UserCodes.normalizeUserCode(rawCode);
    if (code.isEmpty() || password == null || password.isEmpty()) {
      hasher.verify(password == null ? "" : password, null); // cùng thời gian với mã có thật
      throw new AppException(ErrorCode.VALIDATION_FAILED, MSG_LOGIN);
    }
    Outcome o =
        tx.execute(s -> {
          Optional<Account> found = repo.lockByCode(code.get());
          if (found.isEmpty() || found.get().deactivated()) {
            hasher.verify(password, null);
            return new Fail(new AppException(ErrorCode.VALIDATION_FAILED, MSG_LOGIN));
          }
          Account a = found.get();
          Instant now = clock.instant();
          if (a.loginLockedUntil() != null && now.isBefore(a.loginLockedUntil())) {
            Duration left = Duration.between(now, a.loginLockedUntil());
            return new Fail(AppException.rateLimited(left, lockedMessage(left)));
          }
          if (!hasher.verify(password, a.passwordHash())) {
            int n = a.loginFailCount() + 1;
            if (n >= MAX_FAILS) {
              repo.setLoginFailures(a.orgId(), a.id(), 0, now.plus(LOCK));
              return new Fail(AppException.rateLimited(LOCK, lockedMessage(LOCK)));
            }
            repo.setLoginFailures(a.orgId(), a.id(), n, null);
            return new Fail(new AppException(ErrorCode.VALIDATION_FAILED, MSG_LOGIN));
          }
          if (a.loginFailCount() != 0 || a.loginLockedUntil() != null) {
            repo.setLoginFailures(a.orgId(), a.id(), 0, null);
          }
          return new Ok(a);
        });
    return switch (o) {
      case Ok ok -> ok.account();
      case Fail f -> throw f.error();
    };
  }

  static String lockedMessage(Duration left) {
    long minutes = Math.max(1, (left.toSeconds() + 59) / 60);
    return "Tài khoản tạm khóa, thử lại sau " + minutes + " phút";
  }

  private CodeSession session(Account a) {
    revocations.awaitCutoff(a.sessionsValidAfter());
    Role role = roleOf(a);
    String token = tokens.issue(new AuthPrincipal(a.orgId(), a.id(), role));
    String className =
        role == Role.STUDENT ? repo.activeClassName(a.orgId(), a.id()).orElse(null) : null;
    return new CodeSession(
        token, role.name(), a.id(), a.displayName(), className, a.userCode(), false);
  }

  private static Role roleOf(Account a) {
    return "teacher".equals(a.role()) ? Role.TEACHER : Role.STUDENT;
  }

  // ------------------------------------------------------------------ khôi phục (US-S2E1-03)

  public Recovered recover(String rawCode, String rawRecovery, String newPassword) {
    checkPassword("newPassword", newPassword); // mật khẩu sai luật: mã khôi phục giữ hiệu lực (BV2)
    Optional<String> code = UserCodes.normalizeUserCode(rawCode);
    Optional<String> rc = UserCodes.normalizeRecoveryCode(rawRecovery);
    if (code.isEmpty()) {
      throw new AppException(ErrorCode.VALIDATION_FAILED, MSG_RECOVERY);
    }
    String newHash = hasher.hash(newPassword);
    String newRecovery = UserCodes.newRecoveryCode();
    Instant now = clock.instant();
    Outcome o =
        tx.execute(s -> {
          Optional<Account> found = repo.lockByCode(code.get());
          if (found.isEmpty() || found.get().deactivated()) {
            return new Fail(new AppException(ErrorCode.VALIDATION_FAILED, MSG_RECOVERY));
          }
          Account a = found.get();
          if (a.recoveryLockedUntil() != null && now.isBefore(a.recoveryLockedUntil())) {
            Duration left = Duration.between(now, a.recoveryLockedUntil());
            return new Fail(AppException.rateLimited(left, recoveryLockedMessage(left)));
          }
          boolean match =
              rc.isPresent()
                  && a.recoveryHmac() != null
                  && MessageDigest.isEqual(a.recoveryHmac(), hasher.recoveryHmac(a.id(), rc.get()));
          if (!match) {
            boolean fresh =
                a.recoveryWindowStart() == null
                    || !now.isBefore(a.recoveryWindowStart().plus(RECOVERY_WINDOW));
            int n = (fresh ? 0 : a.recoveryFailCount()) + 1;
            Instant start = fresh ? now : a.recoveryWindowStart();
            if (n >= MAX_FAILS) {
              repo.setRecoveryFailures(a.orgId(), a.id(), 0, null, now.plus(LOCK));
              return new Fail(AppException.rateLimited(LOCK, recoveryLockedMessage(LOCK)));
            }
            repo.setRecoveryFailures(a.orgId(), a.id(), n, start, null);
            return new Fail(new AppException(ErrorCode.VALIDATION_FAILED, MSG_RECOVERY));
          }
          repo.setCredentials(
              a.orgId(), a.id(), newHash, hasher.recoveryHmac(a.id(), newRecovery), now);
          return new Ok(a);
        });
    return switch (o) {
      case Ok ok -> {
        revocations.revoked(ok.account().id(), now);
        yield new Recovered(ok.account().userCode(), roleOf(ok.account()).name(), newRecovery);
      }
      case Fail f -> throw f.error();
    };
  }

  static String recoveryLockedMessage(Duration left) {
    long minutes = Math.max(1, (left.toSeconds() + 59) / 60);
    return "Đã thử sai mã khôi phục quá 10 lần. Thử lại sau " + minutes + " phút.";
  }

  // ------------------------------------------------------------------ văn bản riêng tư, tài khoản
  // của tôi

  public record Notice(String version, String title, String body) {}

  public record Me(
      String userCode,
      String displayName,
      String role,
      boolean hasRecoveryCode,
      String privacyVersion,
      java.util.List<Membership> classes) {}

  /** Lớp của người dùng và trạng thái thành viên (pending, active, left, rejected; A6). */
  public record Membership(String className, String status) {}

  public Notice notice() {
    return repo.currentNotice()
        .map(n -> new Notice(n.version(), n.title(), n.body()))
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Chưa có thông báo riêng tư."));
  }

  /**
   * Tài khoản của chính mình: mã người dùng, tên, vai, cờ "đã có mã khôi phục" (không bao giờ là
   * mã).
   */
  public Me me(AuthPrincipal p) {
    return repo.me(p.orgId(), p.userId())
        .map(m ->
            new Me(
                m.userCode(),
                m.displayName(),
                m.role(),
                m.hasRecoveryCode(),
                m.privacyVersion(),
                repo.memberships(p.orgId(), p.userId()).stream()
                    .map(x -> new Membership(x.className(), x.status()))
                    .toList()))
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy tài khoản."));
  }

  /**
   * Móc kiểm thử: đặt mật khẩu tạm + cờ phải đổi (giả lập tài khoản vừa chuyển từ MVP,
   * US-S2E1-04-SC4).
   */
  public int tempPasswordForTest(String rawCode, String tempPassword) {
    return UserCodes.normalizeUserCode(rawCode)
        .map(c -> repo.setTempPassword(c, hasher.hash(tempPassword)))
        .orElse(0);
  }

  /** Móc kiểm thử: hủy mọi thẻ hiện có của một mã người dùng (giả lập phiên hết hạn ở máy chủ). */
  public int revokeSessionsForTest(String rawCode) {
    Instant now = clock.instant();
    Optional<String> code = UserCodes.normalizeUserCode(rawCode);
    if (code.isEmpty()) {
      return 0;
    }
    int n = repo.revokeSessions(code.get(), now);
    repo.idByCode(code.get()).ifPresent(id -> revocations.revoked(id, now));
    return n;
  }
}
