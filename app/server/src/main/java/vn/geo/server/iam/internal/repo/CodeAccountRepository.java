package vn.geo.server.iam.internal.repo;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Tài khoản đăng nhập bằng mã người dùng (S2-E1, PO-61). Đăng ký, đăng nhập, khôi phục xảy ra TRƯỚC
 * khi biết trường nên tra theo {@code user_code} (chỉ mục duy nhất toàn cục, như mã lớp D-GEO-051);
 * sau khi tra được thì mọi lệnh ghi lọc đúng {@code org_id} của hàng.
 */
@Repository
public class CodeAccountRepository {

  /** Một hàng tài khoản có mã, đã khóa FOR UPDATE trong giao dịch đang chạy. */
  public record Account(
      UUID id,
      UUID orgId,
      String role,
      String displayName,
      String userCode,
      String passwordHash,
      byte[] recoveryHmac,
      boolean mustChangePassword,
      int loginFailCount,
      Instant loginLockedUntil,
      int recoveryFailCount,
      Instant recoveryWindowStart,
      Instant recoveryLockedUntil,
      boolean deactivated,
      Instant sessionsValidAfter) {}

  public record NewAccount(
      UUID id,
      UUID orgId,
      String role,
      String accountKind,
      String displayName,
      String userCode,
      String passwordHash,
      byte[] recoveryHmac,
      String privacyVersion,
      Instant at) {}

  public record Notice(String version, String title, String body) {}

  private final JdbcClient jdbc;

  public CodeAccountRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  /**
   * Thêm tài khoản qua hàm fn_register_code_user (geo_app không INSERT thẳng app_user; hàm tạo
   * trường "Cộng đồng" nếu chưa có); false nếu mã người dùng đã có (người gọi sinh mã khác và thử
   * lại).
   */
  public boolean insert(NewAccount a, String orgName) {
    return Boolean.TRUE.equals(
        jdbc.sql(
                """
                SELECT fn_register_code_user(:id, :orgId, :orgName, :teacher, :name, :code, :hash, :rc, :pv, :at)
                """)
            .param("id", a.id())
            .param("orgId", a.orgId())
            .param("orgName", orgName)
            .param("teacher", "teacher".equals(a.role()))
            .param("name", a.displayName())
            .param("code", a.userCode())
            .param("hash", a.passwordHash())
            .param("rc", a.recoveryHmac())
            .param("pv", a.privacyVersion())
            .param("at", ts(a.at()))
            .query(Boolean.class)
            .single());
  }

  /** Tra tài khoản theo mã và khóa hàng cho tới hết giao dịch (đếm sai/khóa không bị đua). */
  public Optional<Account> lockByCode(String userCode) {
    return jdbc.sql(
            """
            SELECT id, org_id, role::text, display_name, user_code, password_hash, recovery_hmac, must_change_password,
                   login_fail_count, login_locked_until, recovery_fail_count, recovery_window_start,
                   recovery_locked_until, deactivated_at IS NOT NULL, sessions_valid_after
            FROM app_user WHERE user_code = :code LIMIT 1 FOR UPDATE
            -- sqllint:allow=tenant đăng nhập bằng mã: chưa biết trường, mã người dùng là khóa toàn cục (PO-61)
            """)
        .param("code", userCode)
        .query((rs, i) ->
            new Account(
                rs.getObject(1, UUID.class),
                rs.getObject(2, UUID.class),
                rs.getString(3),
                rs.getString(4),
                rs.getString(5),
                rs.getString(6),
                rs.getBytes(7),
                rs.getBoolean(8),
                rs.getInt(9),
                instant(rs.getObject(10, OffsetDateTime.class)),
                rs.getInt(11),
                instant(rs.getObject(12, OffsetDateTime.class)),
                instant(rs.getObject(13, OffsetDateTime.class)),
                rs.getBoolean(14),
                instant(rs.getObject(15, OffsetDateTime.class))))
        .optional();
  }

  public void setLoginFailures(UUID orgId, UUID id, int count, Instant lockedUntil) {
    jdbc.sql(
            """
            UPDATE app_user SET login_fail_count = :n, login_locked_until = :until
            WHERE org_id = :orgId AND id = :id
            """)
        .param("n", count)
        .param("until", ts(lockedUntil))
        .param("orgId", orgId)
        .param("id", id)
        .update();
  }

  public void setRecoveryFailures(
      UUID orgId, UUID id, int count, Instant windowStart, Instant until) {
    jdbc.sql(
            """
            UPDATE app_user SET recovery_fail_count = :n, recovery_window_start = :ws, recovery_locked_until = :until
            WHERE org_id = :orgId AND id = :id
            """)
        .param("n", count)
        .param("ws", ts(windowStart))
        .param("until", ts(until))
        .param("orgId", orgId)
        .param("id", id)
        .update();
  }

  /**
   * Đặt mật khẩu mới (+ mã khôi phục mới nếu có); xóa bộ đếm, bỏ cờ phải đổi mật khẩu; {@code
   * revokeBefore} khác null thì hủy mọi phiên phát trước mốc đó (QA-S2E1-01).
   */
  public void setCredentials(
      UUID orgId, UUID id, String passwordHash, byte[] recoveryHmac, Instant revokeBefore) {
    jdbc.sql(
            """
            UPDATE app_user SET password_hash = :hash, recovery_hmac = COALESCE(:rc, recovery_hmac),
                   must_change_password = false, login_fail_count = 0, login_locked_until = NULL,
                   recovery_fail_count = 0, recovery_window_start = NULL, recovery_locked_until = NULL,
                   sessions_valid_after = COALESCE(:revoke, sessions_valid_after)
            WHERE org_id = :orgId AND id = :id
            """)
        .param("hash", passwordHash)
        .param("rc", recoveryHmac)
        .param("revoke", ts(revokeBefore))
        .param("orgId", orgId)
        .param("id", id)
        .update();
  }

  /** Mọi mốc hủy phiên đang có (nạp vào bộ nhớ lúc khởi động; tập nhỏ: chỉ người đã khôi phục). */
  public java.util.Map<UUID, Instant> revokedSessions() {
    java.util.Map<UUID, Instant> out = new java.util.HashMap<>();
    jdbc.sql(
            """
            SELECT id, sessions_valid_after FROM app_user WHERE sessions_valid_after IS NOT NULL LIMIT 200000
            -- sqllint:allow=tenant nạp mốc hủy phiên của mọi trường lúc khởi động (bộ lọc phiên chạy trước TenantContext)
            """)
        .query((rs, i) -> {
          out.put(rs.getObject(1, UUID.class), instant(rs.getObject(2, OffsetDateTime.class)));
          return 0;
        })
        .list();
    return out;
  }

  /** Móc kiểm thử: hủy mọi phiên của một mã người dùng (giả lập "phiên hết hạn ở máy chủ"). */
  public int revokeSessions(String userCode, Instant at) {
    return jdbc.sql(
            """
            UPDATE app_user SET sessions_valid_after = :at WHERE user_code = :code
            -- sqllint:allow=tenant móc kiểm thử dev/test: tra theo mã người dùng toàn cục
            """)
        .param("at", ts(at))
        .param("code", userCode)
        .update();
  }

  /** Móc kiểm thử: đưa tài khoản về trạng thái "vừa chuyển từ MVP" (mật khẩu tạm, phải đổi). */
  public int setTempPassword(String userCode, String passwordHash) {
    return jdbc.sql(
            """
            UPDATE app_user SET password_hash = :hash, must_change_password = true WHERE user_code = :code
            -- sqllint:allow=tenant móc kiểm thử dev/test: tra theo mã người dùng toàn cục
            """)
        .param("hash", passwordHash)
        .param("code", userCode)
        .update();
  }

  /** Móc kiểm thử: mã nội bộ của một mã người dùng. */
  public Optional<UUID> idByCode(String userCode) {
    return jdbc.sql(
            """
            SELECT id FROM app_user WHERE user_code = :code LIMIT 1
            -- sqllint:allow=tenant móc kiểm thử dev/test: tra theo mã người dùng toàn cục
            """)
        .param("code", userCode)
        .query(UUID.class)
        .optional();
  }

  /** Lớp đang học (status active) đầu tiên của học sinh, để hiện ở góc màn như luồng MVP. */
  public Optional<String> activeClassName(UUID orgId, UUID userId) {
    return jdbc.sql(
            """
            SELECT c.name FROM class_member m JOIN class c ON c.org_id = :orgId AND c.id = m.class_id
            WHERE m.org_id = :orgId AND m.user_id = :userId AND m.status = 'active' AND m.left_at IS NULL
            ORDER BY m.joined_at, c.id LIMIT 1
            """)
        .param("orgId", orgId)
        .param("userId", userId)
        .query(String.class)
        .optional();
  }

  public Optional<Notice> currentNotice() {
    return jdbc.sql(
            "SELECT version, title, body FROM privacy_notice WHERE is_current ORDER BY version LIMIT 1")
        .query((rs, i) -> new Notice(rs.getString(1), rs.getString(2), rs.getString(3)))
        .optional();
  }

  /** Thông tin tài khoản của chính người dùng (không bao giờ trả mã khôi phục, chỉ cờ "đã có"). */
  public record Me(
      String userCode,
      String displayName,
      String role,
      boolean hasRecoveryCode,
      String privacyVersion) {}

  public Optional<Me> me(UUID orgId, UUID id) {
    return jdbc.sql(
            """
            SELECT user_code, display_name, role::text, recovery_hmac IS NOT NULL, privacy_ack_version
            FROM app_user WHERE org_id = :orgId AND id = :id LIMIT 1
            """)
        .param("orgId", orgId)
        .param("id", id)
        .query((rs, i) ->
            new Me(
                rs.getString(1),
                rs.getString(2),
                rs.getString(3),
                rs.getBoolean(4),
                rs.getString(5)))
        .optional();
  }

  /** Lớp của người dùng và trạng thái thành viên (pending, active, left, rejected; A6). */
  public record Membership(String className, String status) {}

  public java.util.List<Membership> memberships(UUID orgId, UUID userId) {
    return jdbc.sql(
            """
            SELECT c.name, m.status::text FROM class_member m JOIN class c ON c.org_id = :orgId AND c.id = m.class_id
            WHERE m.org_id = :orgId AND m.user_id = :userId ORDER BY c.name, c.id LIMIT 50
            """)
        .param("orgId", orgId)
        .param("userId", userId)
        .query((rs, i) -> new Membership(rs.getString(1), rs.getString(2)))
        .list();
  }

  static OffsetDateTime ts(Instant i) {
    return i == null ? null : i.atOffset(ZoneOffset.UTC);
  }

  static Instant instant(OffsetDateTime t) {
    return t == null ? null : t.toInstant();
  }
}
