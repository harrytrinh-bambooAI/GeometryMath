package vn.geo.server.tracking.internal.repo;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.id.Uuid7;

/**
 * MÓC KIỂM THỬ E7 (chỉ dùng từ controller profile dev/test): dựng giáo viên, lớp, học sinh, bài
 * giao, tiến độ và câu hỏi riêng cho từng ca E2E trong tổ chức thử "Nhóm thử E7" (seed
 * 700_e7_core.sql). Học sinh, bài giao, giờ mở đi qua đúng hàm cổng (fn_student_login,
 * fn_create_assignment, fn_set_release); tiến độ và câu hỏi ghi thẳng để dựng lại F-GEO-050…058 (18
 * phút, 11 phút… không chờ thật). Giáo viên và lớp tạo trực tiếp vì MVP không có điểm cuối tạo.
 */
@Repository
public class E7SandboxRepository {

  public static final UUID ORG = UUID.fromString("00000000-0000-7000-8000-000000e70001");

  private final JdbcClient jdbc;

  public E7SandboxRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  public UUID createTeacher(String name) {
    UUID id = Uuid7.next();
    jdbc.sql(
            """
            INSERT INTO app_user (id, org_id, role, account_kind, display_name)
            VALUES (:id, :org, 'teacher', 'sample', :name)
            -- sqllint:allow=ownership móc kiểm thử E7 dev/test: giáo viên riêng của một ca
            """)
        .param("id", id)
        .param("org", ORG)
        .param("name", name)
        .update();
    return id;
  }

  public void createClass(UUID id, UUID teacher, String name, String level, String joinCode) {
    jdbc.sql(
            """
            INSERT INTO class (id, org_id, name, level, grade, join_code, teacher_id)
            VALUES (:id, :org, :name, CAST(:level AS edu_level), :grade, :code, :teacher)
            -- sqllint:allow=ownership móc kiểm thử E7 dev/test: MVP không có điểm cuối tạo lớp
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
            -- sqllint:allow=ownership,tenant móc kiểm thử E7 dev/test: tra version hiện hành của đề kho
            """)
        .param("code", code)
        .query(UUID.class)
        .single();
  }

  public String createAssignment(UUID teacher, UUID id, UUID classId, UUID pv, OffsetDateTime rel) {
    return jdbc.sql(
            "SELECT status FROM fn_create_assignment(:org, :teacher, :id, :cls, :pv, :rel, true, true, NULL)")
        .param("org", ORG)
        .param("teacher", teacher)
        .param("id", id)
        .param("cls", classId)
        .param("pv", pv)
        .param("rel", rel)
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

  /** Tiến độ như sau {@code mins} phút làm bài, kết thúc 5 phút trước (fixture §4). */
  public void progress(
      UUID assignmentId, UUID userId, int hint, int asks, int tools, int mins, boolean solution) {
    jdbc.sql(
            """
            INSERT INTO assignment_progress (org_id, assignment_id, user_id, first_opened_at, last_active_at,
                   hint_level_max, hint_view_count, ask_count, tool_count, active_seconds, solution_viewed_at)
            VALUES (:org, :id, :user, now() - :mins * interval '1 minute' - interval '5 minutes',
                    now() - interval '5 minutes', CAST(:hint AS smallint), :hint, :asks, :tools, :mins * 60,
                    CASE WHEN :solution THEN now() - interval '5 minutes' END)
            """)
        .param("org", ORG)
        .param("id", assignmentId)
        .param("user", userId)
        .param("hint", hint)
        .param("asks", asks)
        .param("tools", tools)
        .param("mins", mins)
        .param("solution", solution)
        .update();
  }

  public void question(UUID assignmentId, UUID userId, String text, int minutesAgo) {
    jdbc.sql(
            """
            INSERT INTO qa_turn (org_id, assignment_id, user_id, request_id, asked_at, question, reply_kind,
                                 reply_text, guard_version)
            VALUES (:org, :id, :user, :rq, now() - :ago * interval '1 minute', :q, 'concept',
                    'Câu trả lời mẫu của ca kiểm E7.', 'g0-e7')
            """)
        .param("org", ORG)
        .param("id", assignmentId)
        .param("user", userId)
        .param("rq", Uuid7.next())
        .param("ago", minutesAgo)
        .param("q", text)
        .update();
  }

  /** Số sự kiện theo loại của một học sinh trên một bài (trong 2 ngày gần nhất). */
  public java.util.Map<String, Long> eventCounts(UUID assignmentId, UUID userId) {
    java.util.Map<String, Long> m = new java.util.TreeMap<>();
    jdbc.sql(
            """
            SELECT kind::text, count(*) FROM event_log
            WHERE org_id = :org AND assignment_id = :id AND user_id = :user
              AND occurred_at >= now() - interval '2 days'
            GROUP BY kind LIMIT 20
            """)
        .param("org", ORG)
        .param("id", assignmentId)
        .param("user", userId)
        .query((rs, i) -> m.put(rs.getString(1), rs.getLong(2)))
        .list();
    return m;
  }
}
