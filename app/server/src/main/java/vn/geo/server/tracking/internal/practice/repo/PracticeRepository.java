package vn.geo.server.tracking.internal.practice.repo;

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
 * Lượt thử bài luyện ({@code practice_attempt}, PLAN_code-s23 A5) và báo "Em nghĩ đáp số này đúng"
 * ({@code practice_report}). Lượt thử chỉ ghi qua {@code fn_record_practice_attempt} (khóa theo câu
 * + học sinh, chống lặp theo mã, chặn sau khi Đúng / hết lượt).
 */
@Repository
public class PracticeRepository {

  public record AttemptRow(
      UUID assignmentId, String outcome, String submitted, String detail, int wrongAfter) {}

  public record Recorded(
      String status, String outcome, String submitted, String detail, int wrongAfter) {}

  public record ReportRow(
      UUID id,
      UUID assignmentId,
      UUID userId,
      String studentName,
      String studentCode,
      String submitted,
      String note,
      String status,
      Instant createdAt) {}

  private static final String REPORT_SELECT =
      """
      SELECT r.id, r.assignment_id, r.user_id, u.display_name, u.user_code, r.submitted, r.note, r.status, r.created_at
      FROM practice_report r
      JOIN app_user u ON u.org_id = :orgId AND u.id = r.user_id
      WHERE r.org_id = :orgId
      -- sqllint:allow=ownership,unbounded tên và mã người dùng của học sinh gửi báo (iam); LIMIT nối ở nơi dùng
      """;

  private final TenantJdbc jdbc;

  public PracticeRepository(TenantJdbc jdbc) {
    this.jdbc = jdbc;
  }

  /** Mọi lượt thử của một học sinh trên các câu cho trước, theo thứ tự thời gian. */
  public List<AttemptRow> attempts(UUID userId, List<UUID> assignmentIds) {
    if (assignmentIds.isEmpty()) {
      return List.of();
    }
    return jdbc.sql(
            """
            SELECT assignment_id, outcome, submitted, detail::text, wrong_after
            FROM practice_attempt
            WHERE org_id = :orgId AND user_id = :userId AND assignment_id = ANY(:ids)
            ORDER BY created_at, id LIMIT 2000
            """)
        .param("userId", userId)
        .param("ids", assignmentIds.toArray(new UUID[0]))
        .query((rs, i) ->
            new AttemptRow(
                rs.getObject(1, UUID.class),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4),
                rs.getInt(5)))
        .list();
  }

  /** Lượt đã ghi với đúng mã chống lặp này (gửi lặp), nếu có. */
  public Optional<AttemptRow> attemptByKey(UUID userId, UUID assignmentId, String key) {
    return jdbc.sql(
            """
            SELECT assignment_id, outcome, submitted, detail::text, wrong_after
            FROM practice_attempt
            WHERE org_id = :orgId AND user_id = :userId AND assignment_id = :assignmentId AND request_key = :key
            LIMIT 1
            """)
        .param("userId", userId)
        .param("assignmentId", assignmentId)
        .param("key", key)
        .query((rs, i) ->
            new AttemptRow(
                rs.getObject(1, UUID.class),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4),
                rs.getInt(5)))
        .optional();
  }

  public Recorded record(
      UUID userId,
      UUID assignmentId,
      String key,
      String submitted,
      String outcome,
      String detailJson,
      int maxAttempts,
      String source) {
    return jdbc.sql(
            """
            SELECT status, outcome, submitted, detail::text, wrong_after
            FROM fn_record_practice_attempt(:orgId, :userId, :assignmentId, :key, :submitted, :outcome,
                                            CAST(:detail AS jsonb), :max, :source)
            """)
        .param("userId", userId)
        .param("assignmentId", assignmentId)
        .param("key", key)
        .param("submitted", submitted)
        .param("outcome", outcome)
        .param("detail", detailJson)
        .param("max", maxAttempts)
        .param("source", source)
        .query((rs, i) ->
            new Recorded(
                rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getInt(5)))
        .single();
  }

  /** Báo của một học sinh trên các câu cho trước. */
  public List<ReportRow> reportsOfUser(UUID userId, List<UUID> assignmentIds) {
    if (assignmentIds.isEmpty()) {
      return List.of();
    }
    return jdbc.sql(
            REPORT_SELECT + " AND r.user_id = :userId AND r.assignment_id = ANY(:ids) LIMIT 100")
        .param("userId", userId)
        .param("ids", assignmentIds.toArray(new UUID[0]))
        .query(PracticeRepository::reportRow)
        .list();
  }

  /** Báo trên các câu cho trước (của giáo viên chủ bộ), mới nhất trước. */
  public List<ReportRow> reportsFor(List<UUID> assignmentIds) {
    if (assignmentIds.isEmpty()) {
      return List.of();
    }
    return jdbc.sql(
            REPORT_SELECT
                + " AND r.assignment_id = ANY(:ids) ORDER BY r.created_at DESC, r.id DESC LIMIT 200")
        .param("ids", assignmentIds.toArray(new UUID[0]))
        .query(PracticeRepository::reportRow)
        .list();
  }

  public Optional<ReportRow> report(UUID reportId) {
    return jdbc.sql(REPORT_SELECT + " AND r.id = :id LIMIT 1")
        .param("id", reportId)
        .query(PracticeRepository::reportRow)
        .optional();
  }

  /** Thêm báo; 0 khi em đã báo câu này (BR-S2E8-30). */
  public int insertReport(UUID id, UUID userId, UUID assignmentId, String submitted, String note) {
    return jdbc.sql(
            """
            INSERT INTO practice_report (id, org_id, assignment_id, user_id, submitted, note)
            VALUES (:id, :orgId, :assignmentId, :userId, :submitted, :note)
            ON CONFLICT (org_id, assignment_id, user_id) DO NOTHING
            """)
        .param("id", id)
        .param("assignmentId", assignmentId)
        .param("userId", userId)
        .param("submitted", submitted)
        .param("note", note)
        .update();
  }

  /** Quyết định một báo đang chờ; 0 khi báo đã được xử lý. */
  public int decide(UUID reportId, String status, UUID teacherId) {
    return jdbc.sql(
            """
            UPDATE practice_report SET status = :status, decided_at = now(), decided_by = :teacherId
            WHERE org_id = :orgId AND id = :id AND status = 'pending'
            """)
        .param("id", reportId)
        .param("status", status)
        .param("teacherId", teacherId)
        .update();
  }

  /** M-S2-07: số báo giáo viên đã "Xác nhận đúng" trên các câu cho trước. */
  public long confirmedCount(List<UUID> assignmentIds) {
    if (assignmentIds.isEmpty()) {
      return 0;
    }
    return jdbc.sql(
            """
            SELECT count(*) FROM practice_report
            WHERE org_id = :orgId AND assignment_id = ANY(:ids) AND status = 'confirmed'
            """)
        .param("ids", assignmentIds.toArray(new UUID[0]))
        .query(Long.class)
        .single();
  }

  private static ReportRow reportRow(ResultSet rs, int i) throws SQLException {
    OffsetDateTime t = rs.getObject(9, OffsetDateTime.class);
    return new ReportRow(
        rs.getObject(1, UUID.class),
        rs.getObject(2, UUID.class),
        rs.getObject(3, UUID.class),
        rs.getString(4),
        rs.getString(5),
        rs.getString(6),
        rs.getString(7),
        rs.getString(8),
        t == null ? null : t.toInstant());
  }
}
