package vn.geo.server.assignment.internal.repo;

import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.id.Uuid7;

/**
 * MÓC KIỂM THỬ S3-E2 (chỉ dev/test): giáo viên, lớp, học sinh riêng cho từng ca kiểm trong tổ chức
 * thử "Nhóm thử S3-E2" (seed 920_s3e2_core.sql, thấy bộ đề 's3e2_kho'). Bộ bài thì ca kiểm tự soạn
 * và giao bằng API thật.
 */
@Repository
@Profile({"dev", "test"})
public class S3E2SandboxRepository {

  public static final UUID ORG = UUID.fromString("00000000-0000-7000-8003-000000920001");

  private final JdbcClient jdbc;

  public S3E2SandboxRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  public UUID createTeacher(String displayName) {
    UUID id = Uuid7.next();
    jdbc.sql(
            """
            INSERT INTO app_user (id, org_id, role, account_kind, display_name)
            VALUES (:id, :org, 'teacher', 'sample', :name)
            -- sqllint:allow=ownership móc kiểm thử S3-E2 dev/test: giáo viên riêng cho ca kiểm
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
            -- sqllint:allow=ownership móc kiểm thử S3-E2 dev/test: lớp riêng cho ca kiểm
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

  /** Thành viên Chờ duyệt (pending, V110) — học sinh xin vào lớp chưa được duyệt (BR-S3E2-24). */
  public void setPending(UUID classId, UUID userId) {
    jdbc.sql(
            """
            UPDATE class_member SET status = 'pending'
            WHERE org_id = :org AND class_id = :classId AND user_id = :userId
            -- sqllint:allow=ownership,tenant móc kiểm thử S3-E2 dev/test: em Chờ duyệt (tổ chức thử cố định)
            """)
        .param("org", ORG)
        .param("classId", classId)
        .param("userId", userId)
        .update();
  }
}
