package vn.geo.server.assignment.internal.repo;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.db.TenantJdbc;

/**
 * Bộ bài (assignment_set, assignment_item — PLAN_code-s23 A4). Nháp ghi thẳng; giao chỉ qua {@code
 * fn_assign_practice_set} (sinh hàng assignment MVP + chụp đáp số). Đọc: mô hình đọc nối đề kho
 * (nội dung nền tảng) và lớp (iam), đánh dấu sqllint:allow.
 */
@Repository
public class PracticeSetRepository {

  public record SetRow(
      UUID id,
      String title,
      String status,
      UUID classId,
      String className,
      Instant releaseAt,
      Instant assignedAt,
      int itemCount) {}

  public record ItemRow(
      UUID id,
      int position,
      UUID problemId,
      UUID versionId,
      String code,
      String statement,
      String problemKind,
      String answerKind,
      BigDecimal tolerance,
      int maxAttempts,
      Instant releaseAt,
      UUID assignmentId,
      String answerSnapshot,
      String acceptedForms,
      Instant assignmentReleaseAt,
      String releaseState) {}

  public record ProblemRow(
      UUID problemId, UUID versionId, String code, String title, String statement, String kind) {}

  public record GradingRow(
      UUID setId,
      String setTitle,
      String setStatus,
      UUID assignmentId,
      int position,
      String answer,
      String answerKind,
      BigDecimal tolerance,
      int maxAttempts,
      String acceptedForms,
      String hint1) {}

  public record ClassRow(UUID id, String name) {}

  private static final String SET_SELECT =
      """
      SELECT s.id, s.title, s.status::text, s.class_id, c.name, s.release_at, s.assigned_at,
             (SELECT count(*) FROM assignment_item i WHERE i.org_id = :orgId AND i.set_id = s.id)
      FROM assignment_set s
      LEFT JOIN class c ON c.org_id = :orgId AND c.id = s.class_id
      WHERE s.org_id = :orgId AND s.owner_user_id = :teacherId AND s.status <> 'archived'
      -- sqllint:allow=ownership,unbounded tên lớp được giao (iam); LIMIT nối ở nơi dùng
      """;

  private static final String STUDENT_SET_SELECT =
      """
      SELECT s.id, s.title, s.status::text, s.class_id, c.name, s.release_at, s.assigned_at,
             (SELECT count(*) FROM assignment_item i WHERE i.org_id = :orgId AND i.set_id = s.id)
      FROM class_member cm
      JOIN class c ON c.org_id = :orgId AND c.id = cm.class_id
      JOIN assignment_set s ON s.org_id = :orgId AND s.class_id = cm.class_id AND s.status IN ('assigned', 'closed')
      WHERE cm.org_id = :orgId AND cm.user_id = :userId AND cm.left_at IS NULL AND cm.status = 'active'
      -- sqllint:allow=ownership,unbounded tư cách lớp Đang học (iam, BR-S2E8-19); LIMIT nối ở nơi dùng
      """;

  private static final String GRADING_SELECT =
      """
      SELECT s.id, s.title, s.status::text, i.assignment_id, i.position, i.answer_snapshot, i.answer_kind::text,
             i.tolerance, i.max_attempts, i.accepted_forms::text, h.body
      FROM assignment_item i
      JOIN assignment_set s ON s.org_id = :orgId AND s.id = i.set_id AND s.status IN ('assigned', 'closed')
      LEFT JOIN hint h ON h.org_id = i.content_org_id AND h.problem_version_id = i.problem_version_id AND h.level = 1
      -- sqllint:allow=ownership,tenant,unbounded câu + bộ + gợi ý mức 1 của đề kho (content); WHERE và LIMIT nối ở nơi dùng
      """;

  private final TenantJdbc jdbc;

  public PracticeSetRepository(TenantJdbc jdbc) {
    this.jdbc = jdbc;
  }

  // ------------------------------------------------------------------ giáo viên

  public List<SetRow> teacherSets(UUID teacherId) {
    return jdbc.sql(SET_SELECT + " ORDER BY s.id DESC LIMIT 100")
        .param("teacherId", teacherId)
        .query(PracticeSetRepository::setRow)
        .list();
  }

  public Optional<SetRow> teacherSet(UUID teacherId, UUID setId) {
    return jdbc.sql(SET_SELECT + " AND s.id = :id LIMIT 1")
        .param("teacherId", teacherId)
        .param("id", setId)
        .query(PracticeSetRepository::setRow)
        .optional();
  }

  public List<ItemRow> items(UUID setId) {
    return jdbc.sql(
            """
            SELECT i.id, i.position, i.problem_id, i.problem_version_id, p.code, pv.statement, p.kind::text,
                   i.answer_kind::text, i.tolerance, i.max_attempts, i.release_at, i.assignment_id, i.answer_snapshot,
                   i.accepted_forms::text, a.release_at,
                   CASE WHEN a.release_at IS NULL THEN 'unset' WHEN a.release_at > now() THEN 'upcoming' ELSE 'open' END
            FROM assignment_item i
            JOIN problem_version pv ON pv.org_id = i.content_org_id AND pv.id = i.problem_version_id
            JOIN problem p ON p.org_id = pv.org_id AND p.id = pv.problem_id
            LEFT JOIN assignment a ON a.org_id = :orgId AND a.id = i.assignment_id
            WHERE i.org_id = :orgId AND i.set_id = :setId
            ORDER BY i.position LIMIT 20
            -- sqllint:allow=ownership,tenant câu của bộ + đề kho (org nền tảng) + giờ mở của hàng assignment
            """)
        .param("setId", setId)
        .query(PracticeSetRepository::itemRow)
        .list();
  }

  /** Đề kho (bộ trường thấy) cho màn soạn bài; tối đa 200 đề theo mã. */
  public List<ProblemRow> candidates() {
    return jdbc.sql(
            """
            SELECT p.id, pv.id, p.code, p.title, pv.statement, p.kind::text
            FROM problem p
            JOIN problem_version pv ON pv.org_id = p.org_id AND pv.id = p.current_version_id
            WHERE p.org_id = platform_org_id() AND p.source = 'catalog' AND p.archived_at IS NULL
              AND fn_catalog_bank_visible(:orgId, p.bank)
            ORDER BY p.code, p.id LIMIT 200
            -- sqllint:allow=ownership,tenant đề kho nền tảng thuộc bộ trường thấy (content) cho màn soạn bài luyện
            """)
        .query(PracticeSetRepository::problemRow)
        .list();
  }

  /** Phiên bản đề kho theo id (chỉ đề thuộc bộ trường thấy). */
  public List<ProblemRow> versions(List<UUID> versionIds) {
    return jdbc.sql(
            """
            SELECT p.id, pv.id, p.code, p.title, pv.statement, p.kind::text
            FROM problem_version pv
            JOIN problem p ON p.org_id = pv.org_id AND p.id = pv.problem_id
            WHERE pv.org_id = platform_org_id() AND pv.id = ANY(:ids) AND p.source = 'catalog' AND p.archived_at IS NULL
              AND fn_catalog_bank_visible(:orgId, p.bank)
            LIMIT 50
            -- sqllint:allow=ownership,tenant đề kho nền tảng thuộc bộ trường thấy (content)
            """)
        .param("ids", versionIds.toArray(new UUID[0]))
        .query(PracticeSetRepository::problemRow)
        .list();
  }

  /** Đáp số tham chiếu của đề kho cho giáo viên (qua hàm SECURITY DEFINER; không cho học sinh). */
  public Map<UUID, String> references(UUID teacherId, List<UUID> versionIds) {
    Map<UUID, String> out = new HashMap<>();
    if (versionIds.isEmpty()) {
      return out;
    }
    jdbc.sql(
            "SELECT problem_version_id, final_answer FROM fn_practice_reference(:orgId, :teacherId, :ids)")
        .param("teacherId", teacherId)
        .param("ids", versionIds.toArray(new UUID[0]))
        .query((rs, i) -> {
          out.put(rs.getObject(1, UUID.class), rs.getString(2));
          return 0;
        })
        .list();
    return out;
  }

  public void insertSet(UUID id, UUID teacherId, String title, OffsetDateTime releaseAt) {
    jdbc.sql(
            """
            INSERT INTO assignment_set (id, org_id, owner_user_id, title, release_at)
            VALUES (:id, :orgId, :teacherId, :title, CAST(:releaseAt AS timestamptz))
            """)
        .param("id", id)
        .param("teacherId", teacherId)
        .param("title", title)
        .param("releaseAt", releaseAt)
        .update();
  }

  /** Sửa bộ còn Nháp; trả số hàng đổi (0 = không phải nháp của giáo viên này). */
  public int updateDraft(UUID teacherId, UUID setId, String title, OffsetDateTime releaseAt) {
    return jdbc.sql(
            """
            UPDATE assignment_set SET title = :title, release_at = CAST(:releaseAt AS timestamptz), updated_at = now()
            WHERE org_id = :orgId AND id = :id AND owner_user_id = :teacherId AND status = 'draft'
            """)
        .param("id", setId)
        .param("teacherId", teacherId)
        .param("title", title)
        .param("releaseAt", releaseAt)
        .update();
  }

  public void deleteItems(UUID setId) {
    jdbc.sql(
            "DELETE FROM assignment_item WHERE org_id = :orgId AND set_id = :setId AND assignment_id IS NULL")
        .param("setId", setId)
        .update();
  }

  public void insertItem(
      UUID id,
      UUID setId,
      int position,
      UUID problemId,
      UUID versionId,
      String answerKind,
      BigDecimal tolerance,
      int maxAttempts,
      OffsetDateTime releaseAt) {
    jdbc.sql(
            """
            INSERT INTO assignment_item (id, org_id, set_id, position, content_org_id, problem_id, problem_version_id,
                                         answer_kind, tolerance, max_attempts, release_at)
            VALUES (:id, :orgId, :setId, :position, platform_org_id(), :problemId, :versionId,
                    CAST(:answerKind AS practice_answer_kind), :tolerance, :maxAttempts, CAST(:releaseAt AS timestamptz))
            """)
        .param("id", id)
        .param("setId", setId)
        .param("position", position)
        .param("problemId", problemId)
        .param("versionId", versionId)
        .param("answerKind", answerKind)
        .param("tolerance", tolerance)
        .param("maxAttempts", maxAttempts)
        .param("releaseAt", releaseAt)
        .update();
  }

  public int deleteDraft(UUID teacherId, UUID setId) {
    return jdbc.sql(
            """
            DELETE FROM assignment_set
            WHERE org_id = :orgId AND id = :id AND owner_user_id = :teacherId AND status = 'draft'
            """)
        .param("id", setId)
        .param("teacherId", teacherId)
        .update();
  }

  /** Giao bộ: trả mã trạng thái của fn_assign_practice_set. */
  public String assign(UUID teacherId, UUID setId, UUID classId, boolean allowPast) {
    return jdbc.sql(
            "SELECT fn_assign_practice_set(:orgId, :teacherId, :setId, :classId, :allowPast, NULL)")
        .param("teacherId", teacherId)
        .param("setId", setId)
        .param("classId", classId)
        .param("allowPast", allowPast)
        .query(String.class)
        .single();
  }

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

  public List<ClassRow> classesOf(UUID teacherId) {
    return jdbc.sql(
            """
            SELECT id, name FROM class
            WHERE org_id = :orgId AND teacher_id = :teacherId AND archived_at IS NULL
            ORDER BY name, id LIMIT 50
            -- sqllint:allow=ownership lớp của giáo viên (iam) để chọn lớp giao bài luyện
            """)
        .param("teacherId", teacherId)
        .query((rs, i) -> new ClassRow(rs.getObject(1, UUID.class), rs.getString(2)))
        .list();
  }

  /** Gợi ý mức 1 của một phiên bản đề kho (cho "Xem trước như học sinh"). */
  public Optional<String> hint1(UUID versionId) {
    return jdbc.sql(
            """
            SELECT body FROM hint
            WHERE org_id = platform_org_id() AND problem_version_id = :versionId AND level = 1 LIMIT 1
            -- sqllint:allow=ownership,tenant gợi ý mức 1 của đề kho nền tảng (content)
            """)
        .param("versionId", versionId)
        .query(String.class)
        .optional();
  }

  // ------------------------------------------------------------------ học sinh

  public List<SetRow> studentSets(UUID userId) {
    return jdbc.sql(STUDENT_SET_SELECT + " ORDER BY s.id DESC LIMIT 100")
        .param("userId", userId)
        .query(PracticeSetRepository::setRow)
        .list();
  }

  public Optional<SetRow> studentSet(UUID userId, UUID setId) {
    return jdbc.sql(STUDENT_SET_SELECT + " AND s.id = :id LIMIT 1")
        .param("userId", userId)
        .param("id", setId)
        .query(PracticeSetRepository::setRow)
        .optional();
  }

  public Optional<GradingRow> gradingForStudent(UUID userId, UUID assignmentId) {
    return jdbc.sql(
            GRADING_SELECT
                + """
                JOIN class_member cm ON cm.org_id = :orgId AND cm.class_id = s.class_id AND cm.user_id = :userId
                                    AND cm.left_at IS NULL AND cm.status = 'active'
                WHERE i.org_id = :orgId AND i.assignment_id = :assignmentId
                LIMIT 1
                -- sqllint:allow=ownership,tenant cấu hình chấm của câu + tư cách lớp (iam) + gợi ý mức 1 (content)
                """)
        .param("userId", userId)
        .param("assignmentId", assignmentId)
        .query(PracticeSetRepository::gradingRow)
        .optional();
  }

  public List<GradingRow> gradingsOfSetForStudent(UUID userId, UUID setId) {
    return jdbc.sql(
            GRADING_SELECT
                + """
                JOIN class_member cm ON cm.org_id = :orgId AND cm.class_id = s.class_id AND cm.user_id = :userId
                                    AND cm.left_at IS NULL AND cm.status = 'active'
                WHERE i.org_id = :orgId AND i.set_id = :setId
                ORDER BY i.position LIMIT 20
                -- sqllint:allow=ownership,tenant cấu hình chấm của bộ + tư cách lớp (iam) + gợi ý mức 1 (content)
                """)
        .param("userId", userId)
        .param("setId", setId)
        .query(PracticeSetRepository::gradingRow)
        .list();
  }

  public List<GradingRow> gradingsOfTeacher(UUID teacherId) {
    return jdbc.sql(
            GRADING_SELECT
                + """
                WHERE i.org_id = :orgId AND s.owner_user_id = :teacherId AND i.assignment_id IS NOT NULL
                ORDER BY s.id DESC, i.position LIMIT 2000
                -- sqllint:allow=ownership,tenant cấu hình chấm các câu giáo viên đã giao + gợi ý mức 1 (content)
                """)
        .param("teacherId", teacherId)
        .query(PracticeSetRepository::gradingRow)
        .list();
  }

  /** Thêm dạng viết được chấp nhận (không thêm trùng); trả số hàng khớp chủ bộ. */
  public int addAcceptedForm(UUID teacherId, UUID assignmentId, String form) {
    return jdbc.sql(
            """
            UPDATE assignment_item i
               SET accepted_forms = CASE WHEN i.accepted_forms @> to_jsonb(CAST(:form AS text)) THEN i.accepted_forms
                                         ELSE i.accepted_forms || to_jsonb(CAST(:form AS text)) END
            WHERE i.org_id = :orgId AND i.assignment_id = :assignmentId
              AND EXISTS (SELECT 1 FROM assignment_set s
                          WHERE s.org_id = :orgId AND s.id = i.set_id AND s.owner_user_id = :teacherId)
            """)
        .param("teacherId", teacherId)
        .param("assignmentId", assignmentId)
        .param("form", form)
        .update();
  }

  private static SetRow setRow(ResultSet rs, int i) throws SQLException {
    return new SetRow(
        rs.getObject(1, UUID.class),
        rs.getString(2),
        rs.getString(3),
        rs.getObject(4, UUID.class),
        rs.getString(5),
        instant(rs, 6),
        instant(rs, 7),
        rs.getInt(8));
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
        rs.getString(8),
        rs.getBigDecimal(9),
        rs.getInt(10),
        instant(rs, 11),
        rs.getObject(12, UUID.class),
        rs.getString(13),
        rs.getString(14),
        instant(rs, 15),
        rs.getString(16));
  }

  private static ProblemRow problemRow(ResultSet rs, int i) throws SQLException {
    return new ProblemRow(
        rs.getObject(1, UUID.class),
        rs.getObject(2, UUID.class),
        rs.getString(3),
        rs.getString(4),
        rs.getString(5),
        rs.getString(6));
  }

  private static GradingRow gradingRow(ResultSet rs, int i) throws SQLException {
    return new GradingRow(
        rs.getObject(1, UUID.class),
        rs.getString(2),
        rs.getString(3),
        rs.getObject(4, UUID.class),
        rs.getInt(5),
        rs.getString(6),
        rs.getString(7),
        rs.getBigDecimal(8),
        rs.getInt(9),
        rs.getString(10),
        rs.getString(11));
  }

  private static Instant instant(ResultSet rs, int col) throws SQLException {
    OffsetDateTime t = rs.getObject(col, OffsetDateTime.class);
    return t == null ? null : t.toInstant();
  }
}
