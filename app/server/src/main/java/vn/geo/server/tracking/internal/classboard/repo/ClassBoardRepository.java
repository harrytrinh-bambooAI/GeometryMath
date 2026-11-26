package vn.geo.server.tracking.internal.classboard.repo;

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
 * S3-E3 — đọc của Kết quả lớp (bảng lớp × bài, bằng chứng một ô, dữ liệu cho "Cả lớp vướng ở đâu").
 * Số câu truy vấn của một lần dựng bảng là hằng số, không theo số hàng (US-S3E3-01-SC10). Mọi câu
 * lọc theo tổ chức và theo lớp do chính giáo viên gọi làm chủ (BR-S3E3-07, BR-S3E1-50).
 */
@Repository
public class ClassBoardRepository {

  /** Trần đọc tương tác thô của một bộ (tối đa 20 đề × 100 em × vài tương tác). */
  static final int RAW_MAX = 20_000;

  public record ClassRow(UUID id, String name) {}

  public record SetRow(UUID id, String title) {}

  public record Column(
      int position, UUID assignmentId, String title, UUID contentOrgId, UUID problemVersionId) {}

  public record Member(UUID userId, String displayName, String userCode, boolean left) {}

  /** Một (đề, học sinh) có tương tác; correct / wrong theo lượt gửi đáp số. */
  public record Work(UUID assignmentId, UUID userId, boolean correct, boolean wrong) {}

  public record Attempt(UUID assignmentId, UUID userId, Instant at, String value, String outcome) {}

  public record HintUse(UUID assignmentId, UUID userId, Instant at, int level) {}

  public record Question(UUID assignmentId, UUID userId, Instant at, String text) {}

  public record Progress(Instant openedAt, int hintLevelMax, int hintViews) {}

  public record HintBody(UUID assignmentId, int level, String body) {}

  private final TenantJdbc jdbc;

  public ClassBoardRepository(TenantJdbc jdbc) {
    this.jdbc = jdbc;
  }

  /** Các lớp đang dạy của giáo viên (chọn lớp ở Kết quả lớp). */
  public List<ClassRow> classes(UUID teacherId) {
    return jdbc.sql(
            """
            SELECT c.id, c.name FROM class c
            WHERE c.org_id = :orgId AND c.teacher_id = :teacherId AND c.archived_at IS NULL
            ORDER BY c.name, c.id
            LIMIT 100
            -- sqllint:allow=ownership lớp của chính giáo viên (iam), chỉ đọc tên
            """)
        .param("teacherId", teacherId)
        .query((rs, i) -> new ClassRow(rs.getObject(1, UUID.class), rs.getString(2)))
        .list();
  }

  /** Lớp do giáo viên này làm chủ; rỗng = không thuộc quan hệ (NOT_FOUND). */
  public Optional<ClassRow> ownedClass(UUID teacherId, UUID classId) {
    return jdbc.sql(
            """
            SELECT c.id, c.name FROM class c
            WHERE c.org_id = :orgId AND c.id = :classId AND c.teacher_id = :teacherId
            LIMIT 1
            -- sqllint:allow=ownership kiểm quan hệ chủ lớp (iam), chỉ đọc
            """)
        .param("teacherId", teacherId)
        .param("classId", classId)
        .query((rs, i) -> new ClassRow(rs.getObject(1, UUID.class), rs.getString(2)))
        .optional();
  }

  /** Bộ Đã giao (kể cả Đã đóng) của lớp, mới giao nhất trước; bộ Nháp và lưu trữ không có bảng. */
  public List<SetRow> sets(UUID classId) {
    return jdbc.sql(
            """
            SELECT s.id, s.title FROM assignment_set s
            WHERE s.org_id = :orgId AND s.class_id = :classId AND s.status IN ('assigned', 'closed')
            ORDER BY s.assigned_at DESC NULLS LAST, s.id DESC
            LIMIT 100
            -- sqllint:allow=ownership bộ bài (assignment) của lớp đã kiểm chủ, chỉ đọc
            """)
        .param("classId", classId)
        .query((rs, i) -> new SetRow(rs.getObject(1, UUID.class), rs.getString(2)))
        .list();
  }

  /** Tiêu đề bộ + cột (đề theo thứ tự bộ) của một bộ Đã giao thuộc đúng lớp này. */
  public Optional<SetRow> set(UUID classId, UUID setId) {
    return jdbc.sql(
            """
            SELECT s.id, s.title FROM assignment_set s
            WHERE s.org_id = :orgId AND s.id = :setId AND s.class_id = :classId
              AND s.status IN ('assigned', 'closed')
            LIMIT 1
            -- sqllint:allow=ownership bộ bài (assignment) của lớp đã kiểm chủ, chỉ đọc
            """)
        .param("classId", classId)
        .param("setId", setId)
        .query((rs, i) -> new SetRow(rs.getObject(1, UUID.class), rs.getString(2)))
        .optional();
  }

  public List<Column> columns(UUID setId) {
    return jdbc.sql(
            """
            SELECT i.position, i.assignment_id, p.title, i.content_org_id, i.problem_version_id
            FROM assignment_item i
            JOIN problem p ON p.org_id = i.content_org_id AND p.id = i.problem_id
            WHERE i.org_id = :orgId AND i.set_id = :setId AND i.assignment_id IS NOT NULL
            ORDER BY i.position
            LIMIT 20
            -- sqllint:allow=ownership,tenant câu của bộ (assignment) + tên đề kho (org nền tảng), chỉ đọc
            """)
        .param("setId", setId)
        .query((rs, i) ->
            new Column(
                rs.getInt(1),
                rs.getObject(2, UUID.class),
                rs.getString(3),
                rs.getObject(4, UUID.class),
                rs.getObject(5, UUID.class)))
        .list();
  }

  /** Thành viên Đang học và Đã rời của lớp (Chờ duyệt, Bị từ chối không có hàng). */
  public List<Member> members(UUID classId) {
    return jdbc.sql(
            """
            SELECT u.id, u.display_name, u.user_code, m.status = 'left'
            FROM class_member m
            JOIN app_user u ON u.org_id = m.org_id AND u.id = m.user_id
            WHERE m.org_id = :orgId AND u.org_id = :orgId AND m.class_id = :classId AND m.status IN ('active', 'left')
            LIMIT 2000
            -- sqllint:allow=ownership thành viên lớp (iam) của lớp đã kiểm chủ: chỉ tên hiển thị + mã người dùng
            """)
        .param("classId", classId)
        .query((rs, i) ->
            new Member(
                rs.getObject(1, UUID.class), rs.getString(2), rs.getString(3), rs.getBoolean(4)))
        .list();
  }

  /** Mọi (đề, học sinh) có ít nhất một tương tác trên các đề của bộ, một câu truy vấn. */
  public List<Work> works(List<UUID> assignmentIds) {
    return jdbc.sql(
            """
            SELECT x.assignment_id, x.user_id, bool_or(x.correct), bool_or(x.wrong)
            FROM (
              SELECT pa.assignment_id, pa.user_id, pa.outcome = 'correct' AS correct, pa.outcome = 'wrong' AS wrong
              FROM practice_attempt pa WHERE pa.org_id = :orgId AND pa.assignment_id IN (:ids)
              UNION ALL
              SELECT ap.assignment_id, ap.user_id, false, false
              FROM assignment_progress ap WHERE ap.org_id = :orgId AND ap.assignment_id IN (:ids)
              UNION ALL
              SELECT q.assignment_id, q.user_id, false, false
              FROM qa_turn q WHERE q.org_id = :orgId AND q.assignment_id IN (:ids)
            ) x
            GROUP BY x.assignment_id, x.user_id
            LIMIT 40000
            -- sqllint:allow=ownership tiến độ / hỏi đáp (assignment, answers) của các đề thuộc bộ đã kiểm chủ, chỉ đọc
            """)
        .param("ids", assignmentIds)
        .query((rs, i) ->
            new Work(
                rs.getObject(1, UUID.class),
                rs.getObject(2, UUID.class),
                rs.getBoolean(3),
                rs.getBoolean(4)))
        .list();
  }

  public List<Attempt> attempts(List<UUID> assignmentIds, UUID userId) {
    return jdbc.sql(
            """
            SELECT pa.assignment_id, pa.user_id, pa.created_at, pa.submitted, pa.outcome
            FROM practice_attempt pa
            WHERE pa.org_id = :orgId AND pa.assignment_id IN (:ids)
              AND (CAST(:userId AS uuid) IS NULL OR pa.user_id = CAST(:userId AS uuid))
            ORDER BY pa.created_at, pa.id
            LIMIT :max
            """)
        .param("ids", assignmentIds)
        .param("userId", userId)
        .param("max", RAW_MAX)
        .query((rs, i) ->
            new Attempt(
                rs.getObject(1, UUID.class),
                rs.getObject(2, UUID.class),
                instant(rs, 3),
                rs.getString(4),
                rs.getString(5)))
        .list();
  }

  /** Lượt xin gợi ý (event_log kind = 'hint') từ mốc {@code from} (không quét mọi partition). */
  public List<HintUse> hints(List<UUID> assignmentIds, UUID userId, Instant from) {
    return jdbc.sql(
            """
            SELECT e.assignment_id, e.user_id, e.occurred_at, e.arg
            FROM event_log e
            WHERE e.org_id = :orgId AND e.assignment_id IN (:ids) AND e.kind = 'hint'
              AND e.occurred_at >= :from
              AND (CAST(:userId AS uuid) IS NULL OR e.user_id = CAST(:userId AS uuid))
            ORDER BY e.occurred_at, e.id
            LIMIT :max
            """)
        .param("ids", assignmentIds)
        .param("userId", userId)
        .param("from", OffsetDateTime.ofInstant(from, java.time.ZoneOffset.UTC))
        .param("max", RAW_MAX)
        .query((rs, i) ->
            new HintUse(
                rs.getObject(1, UUID.class),
                rs.getObject(2, UUID.class),
                instant(rs, 3),
                rs.getInt(4)))
        .list();
  }

  /** Câu hỏi nguyên văn của học sinh (không đọc câu trả lời: chỉ chủ câu hỏi đọc reply_text). */
  public List<Question> questions(List<UUID> assignmentIds, UUID userId) {
    return jdbc.sql(
            """
            SELECT q.assignment_id, q.user_id, q.asked_at, q.question
            FROM qa_turn q
            WHERE q.org_id = :orgId AND q.assignment_id IN (:ids)
              AND (CAST(:userId AS uuid) IS NULL OR q.user_id = CAST(:userId AS uuid))
            ORDER BY q.asked_at, q.id
            LIMIT :max
            -- sqllint:allow=ownership câu hỏi (answers) của các đề thuộc bộ đã kiểm chủ, chỉ đọc cột question
            """)
        .param("ids", assignmentIds)
        .param("userId", userId)
        .param("max", RAW_MAX)
        .query((rs, i) ->
            new Question(
                rs.getObject(1, UUID.class),
                rs.getObject(2, UUID.class),
                instant(rs, 3),
                rs.getString(4)))
        .list();
  }

  public Optional<Progress> progress(UUID assignmentId, UUID userId) {
    return jdbc.sql(
            """
            SELECT ap.first_opened_at, ap.hint_level_max, ap.hint_view_count
            FROM assignment_progress ap
            WHERE ap.org_id = :orgId AND ap.assignment_id = :id AND ap.user_id = :userId
            LIMIT 1
            -- sqllint:allow=ownership tiến độ (assignment) của một ô thuộc bộ đã kiểm chủ, chỉ đọc
            """)
        .param("id", assignmentId)
        .param("userId", userId)
        .query((rs, i) -> new Progress(instant(rs, 1), rs.getInt(2), rs.getInt(3)))
        .optional();
  }

  /** Lời gợi ý soạn sẵn của các đề trong bộ (nội dung nguyên văn cho dữ liệu thô). */
  public List<HintBody> hintBodies(UUID setId) {
    return jdbc.sql(
            """
            SELECT i.assignment_id, h.level, h.body
            FROM assignment_item i
            JOIN hint h ON h.org_id = i.content_org_id AND h.problem_version_id = i.problem_version_id
            WHERE i.org_id = :orgId AND i.set_id = :setId AND i.assignment_id IS NOT NULL
            LIMIT 100
            -- sqllint:allow=ownership,tenant gợi ý của đề kho (content, org nền tảng) thuộc bộ đã kiểm chủ, chỉ đọc
            """)
        .param("setId", setId)
        .query((rs, i) -> new HintBody(rs.getObject(1, UUID.class), rs.getInt(2), rs.getString(3)))
        .list();
  }

  /** Đề bài (chỉ đọc) cho "Mở bài làm". */
  public Optional<String> statement(UUID contentOrgId, UUID problemVersionId) {
    return jdbc.sql(
            """
            SELECT pv.statement FROM problem_version pv
            WHERE pv.org_id = :contentOrg AND pv.id = :pv
            LIMIT 1
            -- sqllint:allow=ownership,tenant đề bài của đề kho (content, org nền tảng) thuộc bộ đã kiểm chủ, chỉ đọc
            """)
        .param("contentOrg", contentOrgId)
        .param("pv", problemVersionId)
        .query(String.class)
        .optional();
  }

  private static Instant instant(ResultSet rs, int col) throws SQLException {
    OffsetDateTime t = rs.getObject(col, OffsetDateTime.class);
    return t == null ? null : t.toInstant();
  }
}
