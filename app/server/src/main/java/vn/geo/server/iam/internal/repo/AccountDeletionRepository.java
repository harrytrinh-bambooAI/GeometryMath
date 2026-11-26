package vn.geo.server.iam.internal.repo;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** US-S3E5-05: xóa tài khoản của chính mình và dọn dữ liệu sau 30 ngày (V415). */
@Repository
public class AccountDeletionRepository {

  /** Trạng thái tài khoản cần cho bước xác nhận xóa (khóa hàng tới hết giao dịch). */
  public record Target(
      String role,
      String passwordHash,
      int loginFailCount,
      Instant loginLockedUntil,
      boolean deleted) {}

  private final JdbcClient jdbc;

  public AccountDeletionRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  public Optional<Target> lock(UUID orgId, UUID id) {
    return jdbc.sql(
            """
            SELECT role::text, password_hash, login_fail_count, login_locked_until, deleted_at IS NOT NULL
            FROM app_user WHERE org_id = :orgId AND id = :id LIMIT 1 FOR UPDATE
            """)
        .param("orgId", orgId)
        .param("id", id)
        .query((rs, i) ->
            new Target(
                rs.getString(1),
                rs.getString(2),
                rs.getInt(3),
                instant(rs.getObject(4, OffsetDateTime.class)),
                rs.getBoolean(5)))
        .optional();
  }

  /** Tên các lớp còn Đang dạy (chưa lưu trữ) của giáo viên, theo tên. */
  public List<String> activeClassNames(UUID orgId, UUID teacherId) {
    return jdbc.sql(
            """
            SELECT name FROM class
            WHERE org_id = :orgId AND teacher_id = :id AND archived_at IS NULL
            ORDER BY name LIMIT 50
            """)
        .param("orgId", orgId)
        .param("id", teacherId)
        .query(String.class)
        .list();
  }

  public boolean delete(UUID orgId, UUID id, Instant at) {
    return Boolean.TRUE.equals(
        jdbc.sql("SELECT fn_account_delete(:orgId, :id, :at)")
            .param("orgId", orgId)
            .param("id", id)
            .param("at", at.atOffset(ZoneOffset.UTC))
            .query(Boolean.class)
            .single());
  }

  /** Dọn vật lý tài khoản đã xóa từ trước {@code cutoff}; trả số tài khoản đã xóa hẳn. */
  public int purge(Instant cutoff) {
    Integer n =
        jdbc.sql("SELECT fn_account_purge(:cutoff)")
            .param("cutoff", cutoff.atOffset(ZoneOffset.UTC))
            .query(Integer.class)
            .single();
    return n == null ? 0 : n;
  }

  private static Instant instant(OffsetDateTime t) {
    return t == null ? null : t.toInstant();
  }
}
