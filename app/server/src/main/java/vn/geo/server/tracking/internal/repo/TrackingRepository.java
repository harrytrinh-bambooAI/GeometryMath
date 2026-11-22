package vn.geo.server.tracking.internal.repo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.db.TenantJdbc;

/**
 * Hỏi đáp (qa_turn), tiến độ (assignment_progress), Theo dõi lớp. Ghi qua hàm fn_record_qa; đọc câu
 * trả lời đã lưu chỉ qua fn_qa_answer (D-GEO-159). Đọc nối bài giao, lớp, học sinh là mô hình đọc
 * (ADR-GEO-01, sqllint:allow=ownership).
 */
@Repository
public class TrackingRepository {

  /** Bài giao học sinh được hỏi (đã kiểm tư cách lớp) + đề để đưa vào prompt. */
  public record QaContext(String statement, boolean allowQa) {}

  public record QaRecorded(String status, Long id) {}

  public record Answer(
      long id, UUID assignmentId, Instant askedAt, String question, String kind, String reply) {}

  public record QuestionRow(
      long id, UUID assignmentId, String question, Instant askedAt, String askerName) {}

  public record ProgressRow(
      UUID assignmentId,
      UUID userId,
      String displayName,
      boolean opened,
      int hintLevel,
      int askCount,
      int toolCount,
      int activeSeconds,
      boolean solutionViewed) {}

  public record MonitorHeader(
      UUID assignmentId, String problemCode, String className, String releaseState) {}

  private final TenantJdbc jdbc;

  public TrackingRepository(TenantJdbc jdbc) {
    this.jdbc = jdbc;
  }

  public Optional<QaContext> qaContext(UUID userId, UUID assignmentId) {
    return jdbc.sql(
            """
            SELECT pv.statement, a.allow_qa
            FROM assignment a
            JOIN class_member cm ON cm.org_id = :orgId AND cm.class_id = a.class_id AND cm.user_id = :userId
                                AND cm.left_at IS NULL
            JOIN problem_version pv ON pv.org_id = a.content_org_id AND pv.id = a.problem_version_id
            WHERE a.org_id = :orgId AND a.id = :id AND a.kind = 'class'
            LIMIT 1
            -- sqllint:allow=ownership,tenant đề (nội dung nền tảng) của bài lớp em đang thuộc
            """)
        .param("userId", userId)
        .param("id", assignmentId)
        .query((rs, i) -> new QaContext(rs.getString(1), rs.getBoolean(2)))
        .optional();
  }

  public QaRecorded recordQa(
      UUID userId,
      UUID assignmentId,
      UUID requestId,
      String question,
      String reply,
      String guardVersion) {
    return jdbc.sql(
            """
            SELECT status, qa_id FROM fn_record_qa(:orgId, :userId, :id, :requestId, :question,
                   CAST('concept' AS qa_reply_kind), :reply, :guard, CAST('{}' AS uuid[]))
            """)
        .param("userId", userId)
        .param("id", assignmentId)
        .param("requestId", requestId)
        .param("question", question)
        .param("reply", reply)
        .param("guard", guardVersion)
        .query((rs, i) -> {
          long id = rs.getLong(2);
          return new QaRecorded(rs.getString(1), rs.wasNull() ? null : id);
        })
        .single();
  }

  /** Lượt hỏi đã lưu của chính học sinh với requestId này (lũy đẳng, khóa (org_id, request_id)). */
  public Optional<Long> qaIdByRequest(UUID userId, UUID assignmentId, UUID requestId) {
    return jdbc.sql(
            """
            SELECT id FROM qa_turn
            WHERE org_id = :orgId AND request_id = :requestId AND user_id = :userId AND assignment_id = :id
            LIMIT 1
            """)
        .param("userId", userId)
        .param("id", assignmentId)
        .param("requestId", requestId)
        .query(Long.class)
        .optional();
  }

  public Optional<Answer> answer(UUID userId, long id) {
    return jdbc.sql(
            """
            SELECT id, assignment_id, asked_at, question, reply_kind::text, reply_text
            FROM fn_qa_answer(:orgId, :userId, :id)
            """)
        .param("userId", userId)
        .param("id", id)
        .query((rs, i) ->
            new Answer(
                rs.getLong(1),
                rs.getObject(2, UUID.class),
                instant(rs, 3),
                rs.getString(4),
                rs.getString(5),
                rs.getString(6)))
        .optional();
  }

  /** Học sinh này có thuộc một lớp do giáo viên này dạy không. */
  public boolean isStudentOfTeacher(UUID teacherId, UUID studentId) {
    return jdbc.sql(
                """
                SELECT count(*) FROM class_member cm
                JOIN class c ON c.org_id = :orgId AND c.id = cm.class_id AND c.teacher_id = :teacherId
                WHERE cm.org_id = :orgId AND cm.user_id = :studentId AND cm.left_at IS NULL
                LIMIT 1
                -- sqllint:allow=ownership tư cách lớp (iam) để kiểm quyền xem
                """)
            .param("teacherId", teacherId)
            .param("studentId", studentId)
            .query(Long.class)
            .single()
        > 0;
  }

  /** Câu hỏi của một học sinh, mới nhất trước (không có câu trả lời: chỉ chủ đọc, D-GEO-159). */
  public List<QuestionRow> questionsOf(UUID studentId) {
    return jdbc.sql(
            """
            SELECT q.id, q.assignment_id, q.question, q.asked_at, u.display_name
            FROM qa_turn q JOIN app_user u ON u.org_id = :orgId AND u.id = q.user_id
            WHERE q.org_id = :orgId AND q.user_id = :studentId
            ORDER BY q.id DESC LIMIT 100
            -- sqllint:allow=ownership tên người hỏi (iam)
            """)
        .param("studentId", studentId)
        .query(TrackingRepository::question)
        .list();
  }

  public List<ProgressRow> progressOf(UUID studentId) {
    return jdbc.sql(
            """
            SELECT ap.assignment_id, ap.user_id, u.display_name, ap.first_opened_at IS NOT NULL,
                   ap.hint_level_max, ap.ask_count, ap.tool_count, ap.active_seconds,
                   ap.solution_viewed_at IS NOT NULL
            FROM assignment_progress ap JOIN app_user u ON u.org_id = :orgId AND u.id = ap.user_id
            WHERE ap.org_id = :orgId AND ap.user_id = :studentId
            ORDER BY ap.assignment_id DESC LIMIT 100
            -- sqllint:allow=ownership tên học sinh (iam)
            """)
        .param("studentId", studentId)
        .query(TrackingRepository::progress)
        .list();
  }

  /** Đầu trang Theo dõi lớp: chỉ bài của lớp do chính giáo viên này dạy. */
  public Optional<MonitorHeader> monitorHeader(UUID teacherId, UUID assignmentId) {
    return jdbc.sql(
            """
            SELECT a.id, p.code, c.name,
                   CASE WHEN a.release_at IS NULL THEN 'unset' WHEN a.release_at > now() THEN 'upcoming' ELSE 'open' END
            FROM assignment a
            JOIN class c ON c.org_id = :orgId AND c.id = a.class_id AND c.teacher_id = :teacherId
            JOIN problem p ON p.org_id = a.content_org_id AND p.id = a.problem_id
            WHERE a.org_id = :orgId AND a.id = :id AND a.kind = 'class'
            LIMIT 1
            -- sqllint:allow=ownership,tenant bài giao + lớp (iam) + mã đề kho (org nền tảng)
            """)
        .param("teacherId", teacherId)
        .param("id", assignmentId)
        .query((rs, i) ->
            new MonitorHeader(
                rs.getObject(1, UUID.class), rs.getString(2), rs.getString(3), rs.getString(4)))
        .optional();
  }

  /** Q09: mọi học sinh của lớp (kể cả chưa mở bài), sắp theo tên. */
  public List<ProgressRow> monitorStudents(UUID assignmentId) {
    return jdbc.sql(
            """
            SELECT a.id, u.id, u.display_name, ap.first_opened_at IS NOT NULL,
                   COALESCE(ap.hint_level_max, 0), COALESCE(ap.ask_count, 0), COALESCE(ap.tool_count, 0),
                   COALESCE(ap.active_seconds, 0), ap.solution_viewed_at IS NOT NULL
            FROM assignment a
            JOIN class_member cm ON cm.org_id = :orgId AND cm.class_id = a.class_id AND cm.left_at IS NULL
            JOIN app_user u ON u.org_id = :orgId AND u.id = cm.user_id
            LEFT JOIN assignment_progress ap ON ap.org_id = :orgId AND ap.assignment_id = a.id AND ap.user_id = cm.user_id
            WHERE a.org_id = :orgId AND a.id = :id
            ORDER BY cm.name_key, cm.user_id LIMIT 101
            -- sqllint:allow=ownership thành viên lớp, tên (iam), bài giao (assignment)
            """)
        .param("id", assignmentId)
        .query(TrackingRepository::progress)
        .list();
  }

  /** Q14: câu hỏi gần nhất của cả lớp trên một bài (không có câu trả lời, D-GEO-159). */
  public List<QuestionRow> monitorQuestions(UUID assignmentId) {
    return jdbc.sql(
            """
            SELECT q.id, q.assignment_id, q.question, q.asked_at, u.display_name
            FROM qa_turn q JOIN app_user u ON u.org_id = :orgId AND u.id = q.user_id
            WHERE q.org_id = :orgId AND q.assignment_id = :id
            ORDER BY q.id DESC LIMIT 20
            -- sqllint:allow=ownership tên người hỏi (iam)
            """)
        .param("id", assignmentId)
        .query(TrackingRepository::question)
        .list();
  }

  private static QuestionRow question(ResultSet rs, int i) throws SQLException {
    return new QuestionRow(
        rs.getLong(1),
        rs.getObject(2, UUID.class),
        rs.getString(3),
        instant(rs, 4),
        rs.getString(5));
  }

  private static ProgressRow progress(ResultSet rs, int i) throws SQLException {
    return new ProgressRow(
        rs.getObject(1, UUID.class),
        rs.getObject(2, UUID.class),
        rs.getString(3),
        rs.getBoolean(4),
        rs.getInt(5),
        rs.getInt(6),
        rs.getInt(7),
        rs.getInt(8),
        rs.getBoolean(9));
  }

  private static Instant instant(ResultSet rs, int col) throws SQLException {
    OffsetDateTime t = rs.getObject(col, OffsetDateTime.class);
    return t == null ? null : t.toInstant();
  }
}
