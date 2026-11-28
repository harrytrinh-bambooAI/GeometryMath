package vn.geo.server.assignment.internal.repo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.db.TenantJdbc;

/**
 * Bộ bài chặng 3 (S3-E2, T-261126-29) trên CÙNG mô hình {@code assignment_set / assignment_item}
 * của S2-E8 (PLAN_code-s23 A4, PO-66 G1). Giao vẫn qua {@code fn_assign_practice_set}; đổi giờ mở
 * của bộ đã giao qua {@code fn_set_release} từng hàng assignment (một cổng giờ mở duy nhất). Đọc
 * chéo lớp/thành viên (iam), đề kho (content) và lượt thử (tracking) là mô hình đọc, đánh dấu
 * sqllint:allow.
 */
@Repository
public class AssignmentSetRepository {

  public record SetRow(
      UUID id,
      UUID ownerId,
      String title,
      String status,
      UUID classId,
      String className,
      Instant dueAt,
      Instant releaseAt,
      Instant assignedAt,
      Instant closedAt,
      Instant archivedAt,
      int itemCount,
      int needsReview) {}

  public record ItemRow(
      UUID id,
      int position,
      UUID problemId,
      UUID versionId,
      String code,
      String title,
      String statement,
      boolean needsReview,
      Instant ownReleaseAt,
      UUID assignmentId,
      Instant assignmentReleaseAt,
      int releaseRev,
      String hint1) {}

  public record ProblemRow(
      UUID problemId,
      UUID versionId,
      String code,
      String title,
      String statement,
      boolean needsReview) {}

  /** Bộ học sinh thấy: lớp Đang học + tiến độ của CHÍNH em (BR-S3E2-21). */
  public record StudentSetRow(
      UUID id,
      String title,
      String status,
      UUID classId,
      String className,
      Instant dueAt,
      Instant releaseAt,
      int total,
      int done) {}

  /** Một câu học sinh thấy, kèm trạng thái của em: correct | review | doing | todo. */
  public record StudentItemRow(
      int position,
      UUID assignmentId,
      String statement,
      Instant releaseAt,
      String releaseState,
      String state) {}

  public record ClassRow(UUID id, String name) {}

  private static final String SET_SELECT =
      """
      SELECT s.id, s.owner_user_id, s.title, s.status::text, s.class_id, c.name, s.due_at, s.release_at, s.assigned_at,
             s.closed_at, s.archived_at,
             (SELECT count(*) FROM assignment_item i WHERE i.org_id = :orgId AND i.set_id = s.id),
             (SELECT count(*) FROM assignment_item i
                JOIN problem_version pv ON pv.org_id = i.content_org_id AND pv.id = i.problem_version_id
               WHERE i.org_id = :orgId AND i.set_id = s.id AND pv.verify_status = 'needs_review')
      FROM assignment_set s
      LEFT JOIN class c ON c.org_id = :orgId AND c.id = s.class_id
      WHERE s.org_id = :orgId AND s.owner_user_id = :teacherId
      -- sqllint:allow=ownership,tenant,unbounded tên lớp (iam) + nhãn máy của đề kho (content, org nền tảng); LIMIT nối ở nơi dùng
      """;

  /** "Xong" một câu: có lượt Đúng, hoặc số lượt Sai đạt số lượt tối đa (QA-S3E2-05). */
  private static final String DONE_EXPR =
      """
      (EXISTS (SELECT 1 FROM practice_attempt a WHERE a.org_id = :orgId AND a.assignment_id = i.assignment_id
                 AND a.user_id = :userId AND a.outcome = 'correct')
       OR (SELECT count(*) FROM practice_attempt a WHERE a.org_id = :orgId AND a.assignment_id = i.assignment_id
             AND a.user_id = :userId AND a.outcome = 'wrong') >= i.max_attempts)
      """;

  private final TenantJdbc jdbc;

  public AssignmentSetRepository(TenantJdbc jdbc) {
    this.jdbc = jdbc;
  }

  public Instant now() {
    return jdbc.sql("SELECT now()").query(OffsetDateTime.class).single().toInstant();
  }

  // ------------------------------------------------------------------ giáo viên

  /** archived = false: Nháp, Đã giao, Đã đóng; true: chỉ Lưu trữ (BR-S3E2-32). */
  public List<SetRow> teacherSets(UUID teacherId, boolean archived) {
    return jdbc.sql(
            SET_SELECT
                + (archived ? " AND s.status = 'archived'" : " AND s.status <> 'archived'")
                + " ORDER BY s.id DESC LIMIT 100")
        .param("teacherId", teacherId)
        .query(AssignmentSetRepository::setRow)
        .list();
  }

  public Optional<SetRow> teacherSet(UUID teacherId, UUID setId) {
    return jdbc.sql(SET_SELECT + " AND s.id = :id LIMIT 1")
        .param("teacherId", teacherId)
        .param("id", setId)
        .query(AssignmentSetRepository::setRow)
        .optional();
  }

  public List<ItemRow> items(UUID setId) {
    return jdbc.sql(
            """
            SELECT i.id, i.position, i.problem_id, i.problem_version_id, p.code, p.title, pv.statement,
                   pv.verify_status = 'needs_review', i.release_at, i.assignment_id, a.release_at,
                   COALESCE(a.release_rev, 0), h.body
            FROM assignment_item i
            JOIN problem_version pv ON pv.org_id = i.content_org_id AND pv.id = i.problem_version_id
            JOIN problem p ON p.org_id = pv.org_id AND p.id = pv.problem_id
            LEFT JOIN assignment a ON a.org_id = :orgId AND a.id = i.assignment_id
            LEFT JOIN hint h ON h.org_id = i.content_org_id AND h.problem_version_id = i.problem_version_id AND h.level = 1
            WHERE i.org_id = :orgId AND i.set_id = :setId
            ORDER BY i.position LIMIT 20
            -- sqllint:allow=ownership,tenant câu của bộ + đề kho, gợi ý mức 1 (content, org nền tảng) + giờ mở của hàng assignment
            """)
        .param("setId", setId)
        .query(AssignmentSetRepository::itemRow)
        .list();
  }

  /**
   * Đề Kho chung (bộ trường thấy) cho màn soạn; nhãn máy "Cần xem lại" = verify_status
   * needs_review.
   */
  public List<ProblemRow> candidates() {
    return jdbc.sql(
            """
            SELECT p.id, pv.id, p.code, p.title, pv.statement, pv.verify_status = 'needs_review'
            FROM problem p
            JOIN problem_version pv ON pv.org_id = p.org_id AND pv.id = p.current_version_id
            WHERE p.org_id = platform_org_id() AND p.source = 'catalog' AND p.archived_at IS NULL
              AND fn_catalog_bank_visible(:orgId, p.bank)
            ORDER BY p.code, p.id LIMIT 200
            -- sqllint:allow=ownership,tenant đề kho nền tảng thuộc bộ trường thấy (content) cho màn soạn bộ bài
            """)
        .query(AssignmentSetRepository::problemRow)
        .list();
  }

  /** Một phiên bản đề Kho chung trường thấy; ngoài phạm vi (đề riêng của người khác…) thì rỗng. */
  public Optional<ProblemRow> version(UUID versionId) {
    return jdbc.sql(
            """
            SELECT p.id, pv.id, p.code, p.title, pv.statement, pv.verify_status = 'needs_review'
            FROM problem_version pv
            JOIN problem p ON p.org_id = pv.org_id AND p.id = pv.problem_id
            WHERE pv.org_id = platform_org_id() AND pv.id = :id AND p.source = 'catalog' AND p.archived_at IS NULL
              AND fn_catalog_bank_visible(:orgId, p.bank)
            LIMIT 1
            -- sqllint:allow=ownership,tenant đề kho nền tảng thuộc bộ trường thấy (content)
            """)
        .param("id", versionId)
        .query(AssignmentSetRepository::problemRow)
        .optional();
  }

  public Optional<ClassRow> ownClass(UUID teacherId, UUID classId) {
    return jdbc.sql(
            """
            SELECT id, name FROM class
            WHERE org_id = :orgId AND id = :id AND teacher_id = :teacherId AND archived_at IS NULL
            LIMIT 1
            -- sqllint:allow=ownership lớp của giáo viên (iam) để chọn lớp giao bộ bài
            """)
        .param("teacherId", teacherId)
        .param("id", classId)
        .query((rs, i) -> new ClassRow(rs.getObject(1, UUID.class), rs.getString(2)))
        .optional();
  }

  /** Lớp của giáo viên nhưng Đã lưu trữ (BR-S3E6-14: không giao bộ mới → CONFLICT). */
  public boolean ownArchivedClass(UUID teacherId, UUID classId) {
    return jdbc.sql(
            """
            SELECT 1 FROM class
            WHERE org_id = :orgId AND id = :id AND teacher_id = :teacherId AND archived_at IS NOT NULL
            LIMIT 1
            -- sqllint:allow=ownership lớp của giáo viên (iam) đã lưu trữ, chặn giao bộ mới
            """)
        .param("teacherId", teacherId)
        .param("id", classId)
        .query(Integer.class)
        .optional()
        .isPresent();
  }

  public List<ClassRow> classesOf(UUID teacherId) {
    return jdbc.sql(
            """
            SELECT id, name FROM class
            WHERE org_id = :orgId AND teacher_id = :teacherId AND archived_at IS NULL
            ORDER BY name, id LIMIT 50
            -- sqllint:allow=ownership lớp của giáo viên (iam) để chọn lớp giao bộ bài
            """)
        .param("teacherId", teacherId)
        .query((rs, i) -> new ClassRow(rs.getObject(1, UUID.class), rs.getString(2)))
        .list();
  }

  public void insertSet(
      UUID id,
      UUID teacherId,
      String title,
      UUID classId,
      OffsetDateTime dueAt,
      OffsetDateTime releaseAt) {
    jdbc.sql(
            """
            INSERT INTO assignment_set (id, org_id, owner_user_id, class_id, title, due_at, release_at)
            VALUES (:id, :orgId, :teacherId, :classId, :title, CAST(:dueAt AS timestamptz), CAST(:releaseAt AS timestamptz))
            """)
        .param("id", id)
        .param("teacherId", teacherId)
        .param("classId", classId)
        .param("title", title)
        .param("dueAt", dueAt)
        .param("releaseAt", releaseAt)
        .update();
  }

  /** Sửa bộ còn Nháp; 0 = không phải Nháp của giáo viên này. */
  public int updateDraft(
      UUID teacherId,
      UUID setId,
      String title,
      UUID classId,
      OffsetDateTime dueAt,
      OffsetDateTime releaseAt) {
    return jdbc.sql(
            """
            UPDATE assignment_set SET title = :title, class_id = :classId, due_at = CAST(:dueAt AS timestamptz),
                   release_at = CAST(:releaseAt AS timestamptz), updated_at = now()
            WHERE org_id = :orgId AND id = :id AND owner_user_id = :teacherId AND status = 'draft'
            """)
        .param("id", setId)
        .param("teacherId", teacherId)
        .param("title", title)
        .param("classId", classId)
        .param("dueAt", dueAt)
        .param("releaseAt", releaseAt)
        .update();
  }

  public void deleteDraftItems(UUID setId) {
    jdbc.sql(
            "DELETE FROM assignment_item WHERE org_id = :orgId AND set_id = :setId AND assignment_id IS NULL")
        .param("setId", setId)
        .update();
  }

  public void insertItem(
      UUID id, UUID setId, int position, UUID problemId, UUID versionId, OffsetDateTime releaseAt) {
    jdbc.sql(
            """
            INSERT INTO assignment_item (id, org_id, set_id, position, content_org_id, problem_id, problem_version_id,
                                         release_at)
            VALUES (:id, :orgId, :setId, :position, platform_org_id(), :problemId, :versionId, CAST(:releaseAt AS timestamptz))
            """)
        .param("id", id)
        .param("setId", setId)
        .param("position", position)
        .param("problemId", problemId)
        .param("versionId", versionId)
        .param("releaseAt", releaseAt)
        .update();
  }

  /** Giờ mở chung của bộ đã giao (sau khi các hàng assignment đã đổi qua fn_set_release). */
  public void setReleaseOfSet(UUID teacherId, UUID setId, OffsetDateTime releaseAt) {
    jdbc.sql(
            """
            UPDATE assignment_set SET release_at = CAST(:releaseAt AS timestamptz), updated_at = now()
            WHERE org_id = :orgId AND id = :id AND owner_user_id = :teacherId
            """)
        .param("id", setId)
        .param("teacherId", teacherId)
        .param("releaseAt", releaseAt)
        .update();
  }

  /** Đóng bộ Đã giao (một chiều); 0 = bộ không ở trạng thái Đã giao (BR-S3E2-30). */
  public int close(UUID teacherId, UUID setId) {
    return jdbc.sql(
            """
            UPDATE assignment_set SET status = 'closed', closed_at = now(), updated_at = now()
            WHERE org_id = :orgId AND id = :id AND owner_user_id = :teacherId AND status = 'assigned'
            """)
        .param("id", setId)
        .param("teacherId", teacherId)
        .update();
  }

  /** Lưu trữ bộ Đã đóng; 0 = bộ chưa Đã đóng (BR-S3E2-32). */
  public int archive(UUID teacherId, UUID setId) {
    return jdbc.sql(
            """
            UPDATE assignment_set SET status = 'archived', archived_at = now(), updated_at = now()
            WHERE org_id = :orgId AND id = :id AND owner_user_id = :teacherId AND status = 'closed'
            """)
        .param("id", setId)
        .param("teacherId", teacherId)
        .update();
  }

  // ------------------------------------------------------------------ học sinh

  private static final String STUDENT_SET_SELECT =
      """
      SELECT s.id, s.title, s.status::text, c.id, c.name, s.due_at, s.release_at,
             (SELECT count(*) FROM assignment_item i WHERE i.org_id = :orgId AND i.set_id = s.id),
             (SELECT count(*) FROM assignment_item i WHERE i.org_id = :orgId AND i.set_id = s.id AND i.assignment_id IS NOT NULL
                 AND %s)
      FROM class_member cm
      JOIN class c ON c.org_id = :orgId AND c.id = cm.class_id
      JOIN assignment_set s ON s.org_id = :orgId AND s.class_id = cm.class_id AND s.status IN (%s)
      WHERE cm.org_id = :orgId AND cm.user_id = :userId AND cm.left_at IS NULL AND cm.status = 'active'
      -- sqllint:allow=ownership,tenant,unbounded tư cách lớp Đang học (iam, BR-S3E2-24) + lượt thử của chính em (tracking); LIMIT nối ở nơi dùng
      """;

  /** Bộ Đã giao / Đã đóng của các lớp em Đang học (danh sách chính, không có bộ Lưu trữ). */
  public List<StudentSetRow> studentSets(UUID userId) {
    return jdbc.sql(
            STUDENT_SET_SELECT.formatted(DONE_EXPR, "'assigned', 'closed'")
                + " ORDER BY c.name, c.id, s.id DESC LIMIT 200")
        .param("userId", userId)
        .query(AssignmentSetRepository::studentSetRow)
        .list();
  }

  /**
   * Một bộ em thấy (kể cả Lưu trữ: em vẫn xem kết quả, BR-S3E2-32); Nháp hay không Đang học thì
   * rỗng.
   */
  public Optional<StudentSetRow> studentSet(UUID userId, UUID setId) {
    return jdbc.sql(
            STUDENT_SET_SELECT.formatted(DONE_EXPR, "'assigned', 'closed', 'archived'")
                + " AND s.id = :id LIMIT 1")
        .param("userId", userId)
        .param("id", setId)
        .query(AssignmentSetRepository::studentSetRow)
        .optional();
  }

  public List<StudentItemRow> studentItems(UUID userId, UUID setId) {
    return jdbc.sql(
            """
            SELECT i.position, i.assignment_id, pv.statement, a.release_at,
                   CASE WHEN a.release_at IS NULL THEN 'unset' WHEN a.release_at > now() THEN 'upcoming' ELSE 'open' END,
                   CASE WHEN EXISTS (SELECT 1 FROM practice_attempt x WHERE x.org_id = :orgId AND x.assignment_id = i.assignment_id
                                       AND x.user_id = :userId AND x.outcome = 'correct') THEN 'correct'
                        WHEN (SELECT count(*) FROM practice_attempt x WHERE x.org_id = :orgId AND x.assignment_id = i.assignment_id
                                AND x.user_id = :userId AND x.outcome = 'wrong') >= i.max_attempts THEN 'review'
                        WHEN EXISTS (SELECT 1 FROM practice_attempt x WHERE x.org_id = :orgId AND x.assignment_id = i.assignment_id
                                       AND x.user_id = :userId)
                          OR EXISTS (SELECT 1 FROM assignment_progress g WHERE g.org_id = :orgId AND g.assignment_id = i.assignment_id
                                       AND g.user_id = :userId) THEN 'doing'
                        ELSE 'todo' END
            FROM assignment_item i
            JOIN problem_version pv ON pv.org_id = i.content_org_id AND pv.id = i.problem_version_id
            JOIN assignment a ON a.org_id = :orgId AND a.id = i.assignment_id
            WHERE i.org_id = :orgId AND i.set_id = :setId
            ORDER BY i.position LIMIT 20
            -- sqllint:allow=ownership,tenant,unbounded đề kho (content) + lượt thử, tiến độ của chính em (tracking) + giờ mở
            """)
        .param("userId", userId)
        .param("setId", setId)
        .query((rs, i) ->
            new StudentItemRow(
                rs.getInt(1),
                rs.getObject(2, UUID.class),
                rs.getString(3),
                instant(rs, 4),
                rs.getString(5),
                rs.getString(6)))
        .list();
  }

  // ------------------------------------------------------------------ ánh xạ

  private static SetRow setRow(ResultSet rs, int i) throws SQLException {
    return new SetRow(
        rs.getObject(1, UUID.class),
        rs.getObject(2, UUID.class),
        rs.getString(3),
        rs.getString(4),
        rs.getObject(5, UUID.class),
        rs.getString(6),
        instant(rs, 7),
        instant(rs, 8),
        instant(rs, 9),
        instant(rs, 10),
        instant(rs, 11),
        rs.getInt(12),
        rs.getInt(13));
  }

  private static ItemRow itemRow(ResultSet rs, int i) throws SQLException {
    return new ItemRow(
        rs.getObject(1, UUID.class),
        rs.getInt(2),
        rs.getObject(3, UUID.class),
        rs.getObject(4, UUID.class),
        rs.getString(5),
        rs.getString(6),
        rs.getString(7),
        rs.getBoolean(8),
        instant(rs, 9),
        rs.getObject(10, UUID.class),
        instant(rs, 11),
        rs.getInt(12),
        rs.getString(13));
  }

  private static ProblemRow problemRow(ResultSet rs, int i) throws SQLException {
    return new ProblemRow(
        rs.getObject(1, UUID.class),
        rs.getObject(2, UUID.class),
        rs.getString(3),
        rs.getString(4),
        rs.getString(5),
        rs.getBoolean(6));
  }

  private static StudentSetRow studentSetRow(ResultSet rs, int i) throws SQLException {
    return new StudentSetRow(
        rs.getObject(1, UUID.class),
        rs.getString(2),
        rs.getString(3),
        rs.getObject(4, UUID.class),
        rs.getString(5),
        instant(rs, 6),
        instant(rs, 7),
        rs.getInt(8),
        rs.getInt(9));
  }

  private static Instant instant(ResultSet rs, int col) throws SQLException {
    Timestamp t = rs.getTimestamp(col);
    return t == null ? null : t.toInstant();
  }
}
