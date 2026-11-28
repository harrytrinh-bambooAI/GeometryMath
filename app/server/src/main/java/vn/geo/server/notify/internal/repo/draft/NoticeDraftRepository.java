package vn.geo.server.notify.internal.repo.draft;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.id.Uuid7;

/**
 * SQL của nháp thông báo trợ lý soạn (US-S5E5-01) và báo cáo dùng trợ lý (US-S5E5-04-SC6). Đọc chéo
 * có chủ đích: lớp, thành viên, tên học sinh (iam), bộ bài có hạn (assignment), giáo án
 * (lessonplan) — chỉ đọc.
 */
@Repository
public class NoticeDraftRepository {

  public record Draft(
      UUID id,
      UUID teacherId,
      UUID classId,
      String kind,
      UUID refId,
      String aiText,
      String body,
      String unsure,
      String status,
      boolean edited,
      Integer recipients,
      Instant sentAt,
      Instant createdAt) {}

  public record Fact(UUID id, String title, Instant dueAt) {}

  public record Student(UUID id, String displayName, String userCode) {}

  private final JdbcClient jdbc;

  public NoticeDraftRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  /** Tên lớp nếu lớp do chính giáo viên dạy (người khác, lớp lạ: rỗng → NOT_FOUND). */
  public Optional<String> ownedClassName(UUID orgId, UUID teacherId, UUID classId) {
    return jdbc.sql(
            """
            SELECT c.name FROM class c
            WHERE c.org_id = :orgId AND c.id = :classId AND c.teacher_id = :teacher LIMIT 1
            -- sqllint:allow=ownership đọc lớp (iam) để kiểm chủ lớp trước khi soạn / gửi thông báo
            """)
        .param("orgId", orgId)
        .param("classId", classId)
        .param("teacher", teacherId)
        .query(String.class)
        .optional();
  }

  public String displayName(UUID orgId, UUID userId) {
    return jdbc.sql(
            """
            SELECT u.display_name FROM app_user u WHERE u.org_id = :orgId AND u.id = :id LIMIT 1
            -- sqllint:allow=ownership tên người gửi (giáo viên) in dưới thông báo (BR-S5E5-15)
            """)
        .param("orgId", orgId)
        .param("id", userId)
        .query(String.class)
        .optional()
        .orElse("");
  }

  /** Mọi học sinh từng ở lớp (mọi trạng thái): danh sách tên / mã để gỡ trước và sau AI. */
  public List<Student> roster(UUID orgId, UUID classId) {
    return jdbc.sql(
            """
            SELECT u.id, u.display_name, u.user_code FROM class_member m
            JOIN app_user u ON u.org_id = :orgId AND u.id = m.user_id
            WHERE m.org_id = :orgId AND m.class_id = :classId
            ORDER BY u.id LIMIT 500
            -- sqllint:allow=ownership tên và mã học sinh của lớp (iam) để gỡ khỏi nội dung gửi AI (BR-S5E5-14)
            """)
        .param("orgId", orgId)
        .param("classId", classId)
        .query(
            (rs, i) -> new Student(rs.getObject(1, UUID.class), rs.getString(2), rs.getString(3)))
        .list();
  }

  /** Học sinh Đang học của lớp tại thời điểm gửi (BR-S5E5-02). */
  public List<UUID> activeStudents(UUID orgId, UUID classId) {
    return jdbc.sql(
            """
            SELECT m.user_id FROM class_member m
            WHERE m.org_id = :orgId AND m.class_id = :classId AND m.status = 'active' AND m.left_at IS NULL
            ORDER BY m.user_id LIMIT 500
            -- sqllint:allow=ownership thành viên Đang học của lớp (iam) để chốt người nhận thông báo
            """)
        .param("orgId", orgId)
        .param("classId", classId)
        .query(UUID.class)
        .list();
  }

  /** Bộ bài đã giao cho lớp (dữ kiện: tên bài, hạn). */
  public Optional<Fact> set(UUID orgId, UUID classId, UUID setId) {
    return jdbc.sql(
            """
            SELECT s.id, s.title, s.due_at FROM assignment_set s
            WHERE s.org_id = :orgId AND s.id = :id AND s.class_id = :classId AND s.status <> 'draft' LIMIT 1
            -- sqllint:allow=ownership dữ kiện bộ bài (assignment) điền vào nháp thông báo (BR-S5E5-01)
            """)
        .param("orgId", orgId)
        .param("id", setId)
        .param("classId", classId)
        .query((rs, i) -> new Fact(rs.getObject(1, UUID.class), rs.getString(2), instant(rs, 3)))
        .optional();
  }

  /** Bộ bài đã giao của lớp, mới nhất trước (nguồn chọn của màn soạn). */
  public List<Fact> sets(UUID orgId, UUID classId) {
    return jdbc.sql(
            """
            SELECT s.id, s.title, s.due_at FROM assignment_set s
            WHERE s.org_id = :orgId AND s.class_id = :classId AND s.status <> 'draft' AND s.archived_at IS NULL
            ORDER BY s.id DESC LIMIT 50
            -- sqllint:allow=ownership danh sách bộ bài (assignment) để chọn khi soạn thông báo
            """)
        .param("orgId", orgId)
        .param("classId", classId)
        .query((rs, i) -> new Fact(rs.getObject(1, UUID.class), rs.getString(2), instant(rs, 3)))
        .list();
  }

  public UUID insert(
      UUID orgId,
      UUID teacherId,
      UUID classId,
      String kind,
      UUID refId,
      String aiText,
      String body,
      String unsureJson) {
    UUID id = Uuid7.next();
    jdbc.sql(
            """
            INSERT INTO notice_draft (id, org_id, teacher_id, class_id, kind, ref_id, ai_text, body, unsure)
            VALUES (:id, :orgId, :teacher, :classId, :kind, :ref, :ai, :body, CAST(:unsure AS jsonb))
            """)
        .param("id", id)
        .param("orgId", orgId)
        .param("teacher", teacherId)
        .param("classId", classId)
        .param("kind", kind)
        .param("ref", refId)
        .param("ai", aiText)
        .param("body", body)
        .param("unsure", unsureJson)
        .update();
    return id;
  }

  public Optional<Draft> find(UUID orgId, UUID teacherId, UUID id) {
    return jdbc.sql(
            """
            SELECT d.id, d.teacher_id, d.class_id, d.kind, d.ref_id, d.ai_text, d.body, d.unsure::text, d.status,
                   d.edited, d.recipients, d.sent_at, d.created_at
            FROM notice_draft d WHERE d.org_id = :orgId AND d.id = :id AND d.teacher_id = :teacher LIMIT 1
            """)
        .param("orgId", orgId)
        .param("id", id)
        .param("teacher", teacherId)
        .query(NoticeDraftRepository::row)
        .optional();
  }

  public void updateBody(UUID orgId, UUID id, String body) {
    jdbc.sql(
            """
            UPDATE notice_draft SET body = :body, edited = edited OR body <> :body, updated_at = now()
            WHERE org_id = :orgId AND id = :id AND status = 'draft'
            """)
        .param("orgId", orgId)
        .param("id", id)
        .param("body", body)
        .update();
  }

  /** Chuyển nháp sang Đã gửi đúng một lần (gửi lặp: 0 hàng). */
  public int markSent(UUID orgId, UUID id, int recipients) {
    return jdbc.sql(
            """
            UPDATE notice_draft SET status = 'sent', recipients = :n, sent_at = now(), updated_at = now()
            WHERE org_id = :orgId AND id = :id AND status = 'draft'
            """)
        .param("orgId", orgId)
        .param("id", id)
        .param("n", recipients)
        .update();
  }

  /**
   * Thông báo trong ứng dụng (kênh S3-E4) tới từng học sinh; event_id = mã nháp (không gửi trùng).
   */
  public int deliver(UUID orgId, UUID draftId, String body, List<UUID> users) {
    int n = 0;
    for (UUID u : users) {
      n +=
          jdbc.sql(
                  """
                  INSERT INTO notification (id, org_id, user_id, kind, event_id, set_id, body)
                  VALUES (:id, :orgId, :userId, 'class_notice', :event, NULL, :body)
                  ON CONFLICT (org_id, event_id, user_id) DO NOTHING
                  """)
              .param("id", Uuid7.next())
              .param("orgId", orgId)
              .param("userId", u)
              .param("event", draftId)
              .param("body", body)
              .update();
    }
    return n;
  }

  // ------------------------------------------------------------------ báo cáo dùng trợ lý

  /** [số nháp, số đã gửi, số gửi sau khi sửa] thông báo của giáo viên từ {@code since}. */
  public long[] noticeStats(UUID orgId, UUID teacherId, Instant since) {
    return jdbc.sql(
            """
            SELECT count(*), count(*) FILTER (WHERE d.status = 'sent'),
                   count(*) FILTER (WHERE d.status = 'sent' AND d.edited)
            FROM notice_draft d WHERE d.org_id = :orgId AND d.teacher_id = :teacher AND d.created_at >= :since
            """)
        .param("orgId", orgId)
        .param("teacher", teacherId)
        .param("since", Timestamp.from(since))
        .query((rs, i) -> new long[] {rs.getLong(1), rs.getLong(2), rs.getLong(3)})
        .single();
  }

  /** [số nháp, số đã duyệt, số duyệt sau khi sửa (có bản vá hoặc sửa tay: version > 1)] giáo án. */
  public long[] lessonStats(UUID orgId, UUID teacherId, Instant since) {
    return jdbc.sql(
            """
            SELECT count(*), count(*) FILTER (WHERE p.human_label = 'approved'),
                   count(*) FILTER (WHERE p.human_label = 'approved' AND p.version > 1)
            FROM lesson_plan p WHERE p.org_id = :orgId AND p.teacher_id = :teacher AND p.created_at >= :since
            -- sqllint:allow=ownership đếm giáo án nháp / đã duyệt (lessonplan) cho báo cáo dùng trợ lý (BR-S5E5-17)
            """)
        .param("orgId", orgId)
        .param("teacher", teacherId)
        .param("since", Timestamp.from(since))
        .query((rs, i) -> new long[] {rs.getLong(1), rs.getLong(2), rs.getLong(3)})
        .single();
  }

  /** Chỉ móc kiểm thử dev/test: một giáo án mẫu (đã duyệt / có sửa) cho báo cáo dùng trợ lý. */
  public void seedLessonPlan(UUID orgId, UUID teacherId, boolean approved, boolean edited) {
    jdbc.sql(
            """
            INSERT INTO lesson_plan (id, org_id, teacher_id, code, period_id, request, status, content,
                                     version, human_label)
            VALUES (:id, :orgId, :teacher, 'GA-' || nextval('lesson_plan_code_seq'),
                    (SELECT p.id FROM lp_ppct_period p ORDER BY p.id LIMIT 1), '{}', 'ready', '{}',
                    :version, :label)
            -- sqllint:allow=ownership móc kiểm thử dev/test: giáo án mẫu cho báo cáo dùng trợ lý (BR-S5E5-17)
            """)
        .param("id", Uuid7.next())
        .param("orgId", orgId)
        .param("teacher", teacherId)
        .param("version", edited ? 2 : 1)
        .param("label", approved ? "approved" : "ai")
        .update();
  }

  private static Draft row(ResultSet rs, int i) throws SQLException {
    return new Draft(
        rs.getObject(1, UUID.class),
        rs.getObject(2, UUID.class),
        rs.getObject(3, UUID.class),
        rs.getString(4),
        rs.getObject(5, UUID.class),
        rs.getString(6),
        rs.getString(7),
        rs.getString(8),
        rs.getString(9),
        rs.getBoolean(10),
        (Integer) rs.getObject(11),
        instant(rs, 12),
        instant(rs, 13));
  }

  private static Instant instant(ResultSet rs, int col) throws SQLException {
    Timestamp t = rs.getTimestamp(col);
    return t == null ? null : t.toInstant();
  }
}
