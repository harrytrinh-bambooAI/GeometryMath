package vn.geo.server.iam.internal.repo;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Cờ "đã đọc thông báo riêng tư" theo phiên bản (US-S3E5-02, PO-64). */
@Repository
public class PrivacyRepository {

  /** Tài khoản có mã (chỉ tài khoản này bị chặn khi chưa đọc) và phiên bản đã đọc. */
  public record AckState(boolean codeAccount, String ackVersion) {}

  public record Notice(String version, String title, String body) {}

  private final JdbcClient jdbc;

  public PrivacyRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  public Optional<AckState> state(UUID orgId, UUID userId) {
    return jdbc.sql(
            """
            SELECT user_code IS NOT NULL, privacy_ack_version FROM app_user
            WHERE org_id = :orgId AND id = :id LIMIT 1
            """)
        .param("orgId", orgId)
        .param("id", userId)
        .query((rs, i) -> new AckState(rs.getBoolean(1), rs.getString(2)))
        .optional();
  }

  /** Tài khoản có mã chưa đọc bản hiện hành (nạp lúc khởi động). */
  public java.util.List<UUID> codeUsersNotAcked(String current) {
    return jdbc.sql(
            """
            SELECT id FROM app_user
            WHERE user_code IS NOT NULL AND privacy_ack_version IS DISTINCT FROM :v AND deactivated_at IS NULL
            LIMIT 200000
            -- sqllint:allow=tenant nạp tập "phải đọc thông báo riêng tư" của mọi trường lúc khởi động (bộ lọc chạy trước TenantContext)
            """)
        .param("v", current)
        .query(UUID.class)
        .list();
  }

  public Optional<String> currentVersion() {
    return jdbc.sql("SELECT version FROM privacy_notice WHERE is_current ORDER BY version LIMIT 1")
        .query(String.class)
        .optional();
  }

  public Optional<Notice> notice(String version) {
    return jdbc.sql("SELECT version, title, body FROM privacy_notice WHERE version = :v LIMIT 1")
        .param("v", version)
        .query((rs, i) -> new Notice(rs.getString(1), rs.getString(2), rs.getString(3)))
        .optional();
  }

  public int ack(UUID orgId, UUID userId, String version, Instant at) {
    return jdbc.sql(
            """
            UPDATE app_user SET privacy_ack_version = :v, privacy_ack_at = :at
            WHERE org_id = :orgId AND id = :id
            """)
        .param("v", version)
        .param("at", at == null ? null : at.atOffset(ZoneOffset.UTC))
        .param("orgId", orgId)
        .param("id", userId)
        .update();
  }
}
