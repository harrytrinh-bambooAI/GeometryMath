package vn.geo.server.assignment.internal.repo;

import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.id.Uuid7;

/**
 * MÓC KIỂM THỬ S2-E8 (chỉ dev/test): dựng giáo viên, lớp, học sinh riêng cho từng ca kiểm trong tổ
 * chức thử "Nhóm thử S2-E8" (seed 830_s2e8_core.sql, thấy bộ đề 's2e8_luyen'). Bài luyện thì ca
 * kiểm tự soạn và giao bằng API thật.
 */
@Repository
@Profile({"dev", "test"})
public class S2E8SandboxRepository {

  public static final UUID ORG = UUID.fromString("00000000-0000-7000-8002-000000830001");

  private final JdbcClient jdbc;

  public S2E8SandboxRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  public UUID createTeacher(String displayName) {
    UUID id = Uuid7.next();
    jdbc.sql(
            """
            INSERT INTO app_user (id, org_id, role, account_kind, display_name)
            VALUES (:id, :org, 'teacher', 'sample', :name)
            -- sqllint:allow=ownership móc kiểm thử S2-E8 dev/test: giáo viên riêng cho ca kiểm
            """)
        .param("id", id)
        .param("org", ORG)
        .param("name", displayName)
        .update();
    return id;
  }

  public void createClass(UUID id, UUID teacher, String name, String joinCode) {
    jdbc.sql(
            """
            INSERT INTO class (id, org_id, name, level, grade, join_code, teacher_id)
            VALUES (:id, :org, :name, 'thpt', 11, :code, :teacher)
            -- sqllint:allow=ownership móc kiểm thử S2-E8 dev/test: lớp riêng cho ca kiểm
            """)
        .param("id", id)
        .param("org", ORG)
        .param("name", name)
        .param("code", joinCode)
        .param("teacher", teacher)
        .update();
  }

  public UUID joinStudent(String joinCode, String nameKey, String displayName) {
    return jdbc.sql("SELECT user_id FROM fn_student_login(:code, :key, :name, :id)")
        .param("code", joinCode)
        .param("key", nameKey)
        .param("name", displayName)
        .param("id", Uuid7.next())
        .query(UUID.class)
        .single();
  }

  /**
   * Gắn mã người dùng HS- (PO-61) cho học sinh thử để màn "Báo chấm sai" hiện mã (US-S2E8-05-SC7).
   */
  public void setUserCode(UUID userId, String userCode) {
    jdbc.sql(
            """
            UPDATE app_user SET user_code = :code, password_hash = 'moc-kiem-thu', recovery_hmac = sha256(convert_to(:code, 'UTF8'))
            WHERE org_id = :org AND id = :id
            -- sqllint:allow=ownership,tenant móc kiểm thử S2-E8 dev/test: mã người dùng của học sinh thử (tổ chức thử cố định)
            """)
        .param("code", userCode)
        .param("org", ORG)
        .param("id", userId)
        .update();
  }
}
