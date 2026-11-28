package vn.geo.server.learning.internal.repo;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * MÓC KIỂM THỬ E4 (chỉ dùng từ controller profile dev/test): dựng lớp, học sinh, bài giao riêng cho
 * từng ca E2E trong tổ chức thử "Nhóm thử E4" (seed 401_e4_catalog.sql), đi qua đúng hàm cổng của
 * sản phẩm (fn_student_login, fn_create_assignment, fn_set_release). Lớp tạo trực tiếp vì MVP không
 * có điểm cuối tạo lớp.
 */
@Profile({"dev", "test"}) // FIX4b m2: móc kiểm thử không lọt prod
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

  /**
   * Móc điều khiển giờ mở (dev/test): giờ mở đã hẹn của bài giao thử coi như vừa trôi qua — đặt
   * release_at = now() − 1 giây (lùi created_at theo, ràng buộc release_at ≥ created_at), giữ
   * nguyên release_rev (không phải thao tác "Mở ngay" của giáo viên). Chỉ bài lớp của tổ chức thử
   * E4 có giờ mở ở tương lai.
   */
  public int releasePassed(UUID assignmentId) {
    return jdbc.sql(
            """
            UPDATE assignment SET release_at = now() - interval '1 second',
                                  created_at = LEAST(created_at, now() - interval '2 seconds')
            WHERE org_id = :org AND id = :id AND kind = 'class' AND release_at > now()
            -- sqllint:allow=ownership,tenant móc kiểm thử E4 dev/test: giờ mở hẹn coi như đã qua (TC-GEO-0486)
            """)
        .param("org", ORG)
        .param("id", assignmentId)
        .update();
  }

  /**
   * Số sự kiện một loại của một học sinh trong một bài giao thử E4 (đếm event_log, TC-GEO-0445).
   */
  public long countEvents(UUID assignmentId, UUID userId, String kind) {
    return jdbc.sql(
            """
            SELECT count(*) FROM event_log
            WHERE org_id = :org AND assignment_id = :id AND user_id = :user AND kind::text = :kind
            -- sqllint:allow=ownership,tenant,unbounded,timebound móc kiểm thử E4 dev/test: đếm sự kiện của bài giao thử
            """)
        .param("org", ORG)
        .param("id", assignmentId)
        .param("user", userId)
        .param("kind", kind)
        .query(Long.class)
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
