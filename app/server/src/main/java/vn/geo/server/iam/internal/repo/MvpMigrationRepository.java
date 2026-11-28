package vn.geo.server.iam.internal.repo;

import java.util.List;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Lệnh chuyển tài khoản mẫu MVP sang mô hình mã người dùng (US-S2E1-04). Chạy bởi người vận hành ở
 * dev/test (không prod, PO-51), đi qua mọi trường nên các câu tra cứu đánh dấu ngoại lệ tenant; câu
 * ghi luôn lọc đúng org_id của hàng.
 */
@Repository
public class MvpMigrationRepository {

  /** Một tài khoản MVP cần chuyển: học sinh của lớp (tên + lớp) hoặc giáo viên mẫu. */
  public record Candidate(
      UUID id, UUID orgId, String role, String displayName, String className, String userCode) {}

  private final JdbcClient jdbc;

  public MvpMigrationRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  /** Giáo viên mẫu và học sinh đang ở lớp (MVP), theo trường; {@code orgId} null = mọi trường. */
  public List<Candidate> candidates(UUID orgId) {
    return jdbc.sql(
            """
            SELECT u.id, u.org_id, u.role::text, u.display_name,
                   (SELECT c.name FROM class_member m JOIN class c ON c.org_id = m.org_id AND c.id = m.class_id
                     WHERE m.org_id = u.org_id AND m.user_id = u.id AND m.left_at IS NULL ORDER BY c.name LIMIT 1),
                   u.user_code
            FROM app_user u
            WHERE (CAST(:org AS uuid) IS NULL OR u.org_id = CAST(:org AS uuid))
              AND u.deactivated_at IS NULL
              AND ((u.role = 'teacher' AND u.account_kind = 'sample')
                OR (u.role = 'student' AND u.account_kind = 'light'
                    AND EXISTS (SELECT 1 FROM class_member m2 WHERE m2.org_id = u.org_id AND m2.user_id = u.id
                                AND m2.left_at IS NULL)))
            ORDER BY u.org_id, u.role DESC, u.display_name, u.id
            LIMIT 100000
            -- sqllint:allow=tenant lệnh vận hành chuyển dữ liệu MVP đi qua mọi trường (US-S2E1-04, dev/test)
            """)
        .param("org", orgId)
        .query((rs, i) ->
            new Candidate(
                rs.getObject(1, UUID.class),
                rs.getObject(2, UUID.class),
                rs.getString(3),
                rs.getString(4),
                rs.getString(5),
                rs.getString(6)))
        .list();
  }

  /**
   * Gắn mã + mật khẩu tạm + mã khôi phục cho hàng CHƯA có mã (chạy lại không ghi đè). false nếu
   * hàng đã có mã; ném {@link DuplicateKeyException} nếu mã người dùng vừa sinh trùng (người gọi
   * sinh lại).
   */
  public boolean assign(
      UUID orgId, UUID id, String userCode, String passwordHash, byte[] recoveryHmac) {
    return jdbc.sql(
                """
                UPDATE app_user SET user_code = :code, password_hash = :hash, recovery_hmac = :rc,
                       must_change_password = true
                WHERE org_id = :orgId AND id = :id AND user_code IS NULL
                """)
            .param("code", userCode)
            .param("hash", passwordHash)
            .param("rc", recoveryHmac)
            .param("orgId", orgId)
            .param("id", id)
            .update()
        == 1;
  }
}
