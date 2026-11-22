package vn.geo.server.assignment.internal.repo;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.id.Uuid7;

/**
 * MÓC KIỂM THỬ E6 (chỉ dev/test): dựng giáo viên, lớp, học sinh, bài giao riêng cho từng ca kiểm
 * trong tổ chức thử "Nhóm thử E6" (seed 600_e6_core.sql). Bài giao tạo qua fn_create_assignment như
 * đường thật; riêng móc điều khiển giờ mở (D-GEO-169) ghi thẳng release_at để ca biên giờ mở chạy
 * trong vài giây thay vì chờ thật.
 */
@Repository
@Profile({"dev", "test"})
public class E6SandboxRepository {

  public static final UUID ORG = UUID.fromString("00000000-0000-7000-8000-000000e60001");

  private final JdbcClient jdbc;

  public E6SandboxRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  public UUID createTeacher(String displayName) {
    UUID id = Uuid7.next();
    jdbc.sql(
            """
            INSERT INTO app_user (id, org_id, role, account_kind, display_name)
            VALUES (:id, :org, 'teacher', 'sample', :name)
            -- sqllint:allow=ownership móc kiểm thử E6 dev/test: MVP không có điểm cuối tạo giáo viên
            """)
        .param("id", id)
        .param("org", ORG)
        .param("name", displayName)
        .update();
    return id;
  }

  public void createClass(UUID id, UUID teacher, String name, String level, String joinCode) {
    jdbc.sql(
            """
            INSERT INTO class (id, org_id, name, level, grade, join_code, teacher_id)
            VALUES (:id, :org, :name, CAST(:level AS edu_level), :grade, :code, :teacher)
            -- sqllint:allow=ownership móc kiểm thử E6 dev/test: MVP không có điểm cuối tạo lớp
            """)
        .param("id", id)
        .param("org", ORG)
        .param("name", name)
        .param("level", level)
        .param("grade", "thcs".equals(level) ? 9 : 11)
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

  public UUID versionOf(String code) {
    return jdbc.sql(
            """
            SELECT current_version_id FROM problem
            WHERE org_id = platform_org_id() AND code = :code AND source = 'catalog' LIMIT 1
            -- sqllint:allow=ownership,tenant móc kiểm thử E6 dev/test: tra version hiện hành của đề kho
            """)
        .param("code", code)
        .query(UUID.class)
        .single();
  }

  public String createAssignment(
      UUID teacher,
      UUID id,
      UUID classId,
      UUID versionId,
      OffsetDateTime release,
      boolean hint,
      boolean qa) {
    return jdbc.sql(
            "SELECT status FROM fn_create_assignment(:org, :teacher, :id, :cls, :pv, :rel, :hint, :qa, NULL)")
        .param("org", ORG)
        .param("teacher", teacher)
        .param("id", id)
        .param("cls", classId)
        .param("pv", versionId)
        .param("rel", release)
        .param("hint", hint)
        .param("qa", qa)
        .query(String.class)
        .single();
  }

  public String openNow(UUID teacher, UUID assignmentId) {
    return jdbc.sql("SELECT status FROM fn_set_release(:org, :teacher, :id, true, NULL, 0, NULL)")
        .param("org", ORG)
        .param("teacher", teacher)
        .param("id", assignmentId)
        .query(String.class)
        .single();
  }

  /**
   * D-GEO-169: đặt giờ mở = giờ máy chủ + n giây (n có thể âm tới lúc tạo bài). Chỉ móc kiểm thử;
   * đường thật là fn_set_release. Trả giờ mở mới.
   */
  public OffsetDateTime setReleaseIn(UUID assignmentId, double seconds) {
    return jdbc.sql(
            """
            UPDATE assignment SET release_at = GREATEST(created_at, now() + make_interval(secs => :s)),
                                  release_rev = release_rev + 1
            WHERE id = :id AND kind = 'class'
            RETURNING release_at
            -- sqllint:allow=ownership,tenant móc kiểm thử E6 dev/test (D-GEO-169): ca biên giờ mở
            """)
        .param("id", assignmentId)
        .param("s", seconds)
        .query(OffsetDateTime.class)
        .single();
  }
}
