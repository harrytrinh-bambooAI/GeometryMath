package vn.geo.server.learning.internal.repo;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * MÓC KIỂM THỬ E4 (chỉ dùng từ controller profile dev/test): dựng lớp, học sinh, bài giao riêng cho
 * từng ca E2E trong tổ chức thử "Nhóm thử E4" (seed 401_e4_catalog.sql), đi qua đúng hàm cổng của
 * sản phẩm (fn_student_login, fn_create_assignment, fn_set_release). Lớp tạo trực tiếp vì MVP không
 * có điểm cuối tạo lớp.
 */
@Repository
public class SandboxRepository {

  public static final UUID ORG = UUID.fromString("00000000-0000-7000-8000-000000e40001");
  public static final UUID TEACHER = UUID.fromString("00000000-0000-7000-8000-000000e40003");

  private final JdbcClient jdbc;

  public SandboxRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  public void createClass(UUID id, String name, String level, String joinCode) {
    jdbc.sql(
            """
            INSERT INTO class (id, org_id, name, level, grade, join_code, teacher_id)
            VALUES (:id, :org, :name, CAST(:level AS edu_level), :grade, :code, :teacher)
            -- sqllint:allow=ownership móc kiểm thử E4 dev/test: MVP không có điểm cuối tạo lớp
            """)
        .param("id", id)
        .param("org", ORG)
        .param("name", name)
        .param("level", level)
        .param("grade", "thcs".equals(level) ? 9 : 11)
        .param("code", joinCode)
        .param("teacher", TEACHER)
        .update();
  }

  public UUID joinStudent(String joinCode, String nameKey, String displayName) {
    return jdbc.sql("SELECT user_id FROM fn_student_login(:code, :key, :name, :id)")
        .param("code", joinCode)
        .param("key", nameKey)
        .param("name", displayName)
        .param("id", vn.geo.server.common.id.Uuid7.next())
        .query(UUID.class)
        .single();
  }

  public UUID versionOf(String code) {
    return jdbc.sql(
            """
            SELECT current_version_id FROM problem
            WHERE org_id = platform_org_id() AND code = :code AND source = 'catalog' LIMIT 1
            -- sqllint:allow=ownership,tenant móc kiểm thử E4 dev/test: tra version hiện hành của đề kho
            """)
        .param("code", code)
        .query(UUID.class)
        .single();
  }

  public String createAssignment(
      UUID id, UUID classId, UUID versionId, OffsetDateTime release, boolean hint, boolean qa) {
    return jdbc.sql(
            """
            SELECT status FROM fn_create_assignment(:org, :teacher, :id, :cls, :pv, :rel, :hint, :qa, NULL)
            """)
        .param("org", ORG)
        .param("teacher", TEACHER)
        .param("id", id)
        .param("cls", classId)
        .param("pv", versionId)
        .param("rel", release)
        .param("hint", hint)
        .param("qa", qa)
        .query(String.class)
        .single();
  }

  public String openNow(UUID assignmentId) {
    return jdbc.sql("SELECT status FROM fn_set_release(:org, :teacher, :id, true, NULL, 0, NULL)")
        .param("org", ORG)
        .param("teacher", TEACHER)
        .param("id", assignmentId)
        .query(String.class)
        .single();
  }
}
