package vn.geo.server.iam.internal.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.RequestContext;
import vn.geo.server.iam.internal.code.PasswordHasher;
import vn.geo.server.iam.internal.repo.AccountDeletionRepository;
import vn.geo.server.iam.internal.repo.AccountDeletionRepository.Target;
import vn.geo.server.iam.internal.repo.CodeAccountRepository;

/**
 * US-S3E5-05: xóa tài khoản của chính mình (BR-S3E5-50…55).
 *
 * <ul>
 *   <li>Chỉ chủ tài khoản: mã trên đường dẫn khác người gọi thì NOT_FOUND (không lộ tài khoản có
 *       tồn tại, QA-S3E1-04).
 *   <li>Phải gõ đúng "XÓA" và nhập lại mật khẩu; sai mật khẩu tính vào bộ đếm khóa đăng nhập 10
 *       lần/15 phút (BR-S2E1-09), ghi trong giao dịch riêng rồi mới ném lỗi.
 *   <li>PO-72: học sinh vào lớp bằng tên (tài khoản không có mật khẩu) chỉ cần gõ "XÓA": không bước
 *       mật khẩu, không xét và không tăng bộ đếm sai (BR-S3E5-50, -55).
 *   <li>Giáo viên còn lớp Đang dạy: CONFLICT nêu tên các lớp (BR-S3E5-52).
 *   <li>Xóa: bia mộ ẩn danh + xóa ngay bài làm, lượt hỏi AI, thành viên lớp; mọi phiên bị hủy; mã
 *       không cấp lại. Dọn vật lý sau 30 ngày ở {@link #purge(Instant)}.
 * </ul>
 */
@Service
public class AccountDeletionService {

  public static final String CONFIRM_WORD = "XÓA";
  public static final Duration RETENTION = Duration.ofDays(30);
  public static final String MSG_CONFIRM = "Gõ chữ XÓA để xác nhận.";
  public static final String MSG_PASSWORD = "Mật khẩu chưa đúng.";
  public static final String MSG_ACTIVE_CLASSES =
      "Cần lưu trữ các lớp còn Đang dạy trước khi xóa tài khoản: ";

  public record Deleted(boolean deleted) {}

  private final AccountDeletionRepository repo;
  private final CodeAccountRepository accounts;
  private final PasswordHasher hasher;
  private final SessionRevocations revocations;
  private final TransactionTemplate tx;
  private final Clock clock;

  public AccountDeletionService(
      AccountDeletionRepository repo,
      CodeAccountRepository accounts,
      PasswordHasher hasher,
      SessionRevocations revocations,
      TransactionTemplate tx,
      Clock clock) {
    this.repo = repo;
    this.accounts = accounts;
    this.hasher = hasher;
    this.revocations = revocations;
    this.tx = tx;
    this.clock = clock;
  }

  private sealed interface Outcome permits Done, Fail {}

  private record Done(boolean deleted) implements Outcome {}

  private record Fail(AppException error) implements Outcome {}

  public Deleted delete(UUID userId, String password, String confirm) {
    AuthPrincipal p = RequestContext.principal();
    if (!p.userId().equals(userId)) {
      throw new AppException(ErrorCode.NOT_FOUND);
    }
    if (!CONFIRM_WORD.equals(confirm == null ? null : confirm.strip())) {
      throw AppException.invalidField("confirm", MSG_CONFIRM);
    }
    Instant now = clock.instant();
    Outcome o =
        tx.execute(s -> {
          Target t = repo.lock(p.orgId(), p.userId()).orElse(null);
          if (t == null || t.deleted()) {
            return new Done(false); // đã xóa (bấm hai lần): không làm gì thêm
          }
          if (t.passwordHash() == null && "student".equals(t.role())) {
            // PO-72: không có mật khẩu (vào lớp bằng tên) — gõ XÓA là đủ, không có bộ đếm sai
            return new Done(repo.delete(p.orgId(), p.userId(), now));
          }
          if (password == null || password.isEmpty()) {
            return new Fail(AppException.invalidField("password", MSG_PASSWORD));
          }
          if (t.loginLockedUntil() != null && now.isBefore(t.loginLockedUntil())) {
            Duration left = Duration.between(now, t.loginLockedUntil());
            return new Fail(AppException.rateLimited(left, CodeAuthService.lockedMessage(left)));
          }
          if (!hasher.verify(password, t.passwordHash())) {
            int n = t.loginFailCount() + 1;
            if (n >= CodeAuthService.MAX_FAILS) {
              accounts.setLoginFailures(p.orgId(), p.userId(), 0, now.plus(CodeAuthService.LOCK));
              return new Fail(
                  AppException.rateLimited(
                      CodeAuthService.LOCK, CodeAuthService.lockedMessage(CodeAuthService.LOCK)));
            }
            accounts.setLoginFailures(p.orgId(), p.userId(), n, null);
            return new Fail(AppException.invalidField("password", MSG_PASSWORD));
          }
          if ("teacher".equals(t.role())) {
            List<String> active = repo.activeClassNames(p.orgId(), p.userId());
            if (!active.isEmpty()) {
              return new Fail(
                  new AppException(
                      ErrorCode.CONFLICT, MSG_ACTIVE_CLASSES + String.join(", ", active) + "."));
            }
          }
          return new Done(repo.delete(p.orgId(), p.userId(), now));
        });
    if (o instanceof Fail f) {
      throw f.error();
    }
    revocations.revoked(p.userId(), now);
    return new Deleted(true);
  }

  /** Dọn vật lý tài khoản đã xóa quá 30 ngày tính tới {@code now} (giờ máy chủ). */
  public int purge(Instant now) {
    return repo.purge(now.minus(RETENTION));
  }
}
