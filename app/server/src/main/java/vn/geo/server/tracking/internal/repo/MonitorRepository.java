package vn.geo.server.tracking.internal.repo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.db.TenantJdbc;
import vn.geo.server.tracking.internal.domain.MonitorView;

/**
 * Đọc của Theo dõi lớp đầy đủ (E7): đầu trang, mọi câu hỏi của bài (gộp nhóm ở ứng dụng), danh sách
 * bài đã giao của giáo viên (Q22), đọc sự kiện theo con trỏ có cận thời gian (Q20, US-GEO-704).
 * Trang Theo dõi lớp KHÔNG đọc event_log (D-GEO-014): đọc assignment_progress, qa_turn.
 */
@Repository
public class MonitorRepository {

  public record Header(
      UUID assignmentId,
      String problemCode,
      String problemTitle,
      String className,
      Instant assignedAt,
      Instant releaseAt,
      String releaseState) {}

  public record AssignedRow(
      UUID id,
      String problemCode,
      String problemTitle,
      UUID classId,
      String className,
      Instant assignedAt,
      Instant releaseAt,
      String releaseState) {}

  public record EventRow(long id, Instant occurredAt, String kind, Integer arg) {}

  private final TenantJdbc jdbc;

  public MonitorRepository(TenantJdbc jdbc) {
    this.jdbc = jdbc;
  }

  /** Đầu trang: chỉ bài của lớp do chính giáo viên này dạy. */
  public Optional<Header> header(UUID teacherId, UUID assignmentId) {
    return jdbc.sql(
            """
            SELECT a.id, p.code, p.title, c.name, a.created_at, a.release_at,
                   CASE WHEN a.release_at IS NULL THEN 'unset' WHEN a.release_at > now() THEN 'upcoming' ELSE 'open' END
            FROM assignment a
            JOIN class c ON c.org_id = :orgId AND c.id = a.class_id AND c.teacher_id = :teacherId
            JOIN problem p ON p.org_id = a.content_org_id AND p.id = a.problem_id
            WHERE a.org_id = :orgId AND a.id = :id AND a.kind = 'class'
            LIMIT 1
            -- sqllint:allow=ownership,tenant bài giao + lớp (iam) + tên đề kho (org nền tảng)
            """)
        .param("teacherId", teacherId)
        .param("id", assignmentId)
        .query((rs, i) ->
            new Header(
                rs.getObject(1, UUID.class),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4),
                instant(rs, 5),
                instant(rs, 6),
                rs.getString(7)))
        .optional();
  }

  /** Mọi câu hỏi của lớp trên một bài (tối đa 2000, cũ trước): một câu lệnh, gộp ở ứng dụng. */
  public List<MonitorView.Asked> questions(UUID assignmentId) {
    return jdbc.sql(
            """
            SELECT q.id, q.user_id, u.display_name, q.question, q.asked_at
            FROM qa_turn q JOIN app_user u ON u.org_id = :orgId AND u.id = q.user_id
            WHERE q.org_id = :orgId AND q.assignment_id = :id
            ORDER BY q.id LIMIT 2000
            -- sqllint:allow=ownership tên người hỏi (iam)
            """)
        .param("id", assignmentId)
        .query((rs, i) ->
            new MonitorView.Asked(
                rs.getLong(1),
                rs.getObject(2, UUID.class),
                rs.getString(3),
                rs.getString(4),
                instant(rs, 5)))
        .list();
  }

  /**
   * Q22: bài đã giao của các lớp giáo viên dạy, mới giao trước (id UUIDv7), theo con trỏ. Mỗi lớp
   * đọc top-n theo chỉ mục assignment_class_idx rồi trộn (không quét bảng bài giao).
   */
  public List<AssignedRow> assigned(UUID teacherId, UUID beforeId, int fetch) {
    return jdbc.sql(
            """
            SELECT x.id, p.code, p.title, c.id, c.name, x.created_at, x.release_at,
                   CASE WHEN x.release_at IS NULL THEN 'unset' WHEN x.release_at > now() THEN 'upcoming' ELSE 'open' END
            FROM class c
            CROSS JOIN LATERAL (
              SELECT a.id, a.content_org_id, a.problem_id, a.created_at, a.release_at FROM assignment a
              WHERE a.org_id = :orgId AND a.class_id = c.id AND a.kind = 'class' AND a.archived_at IS NULL
                AND (CAST(:before AS uuid) IS NULL OR a.id < CAST(:before AS uuid))
              ORDER BY a.id DESC LIMIT :fetch) x
            JOIN problem p ON p.org_id = x.content_org_id AND p.id = x.problem_id
            WHERE c.org_id = :orgId AND c.teacher_id = :teacherId
            ORDER BY x.id DESC LIMIT :fetch
            -- sqllint:allow=ownership,tenant read model Theo dõi: lớp (iam), bài giao, tên đề kho (org nền tảng)
            """)
        .param("teacherId", teacherId)
        .param("before", beforeId)
        .param("fetch", fetch)
        .query((rs, i) ->
            new AssignedRow(
                rs.getObject(1, UUID.class),
                rs.getString(2),
                rs.getString(3),
                rs.getObject(4, UUID.class),
                rs.getString(5),
                instant(rs, 6),
                instant(rs, 7),
                rs.getString(8)))
        .list();
  }

  /**
   * Q20: sự kiện của một học sinh trên một bài trong [from, to), mới nhất trước, keyset
   * (occurred_at, id); chỉ chạm các partition trong cận thời gian (BR-GEO-282).
   */
  public List<EventRow> events(
      UUID assignmentId,
      UUID userId,
      Instant from,
      Instant to,
      Instant beforeAt,
      Long beforeId,
      int fetch) {
    return jdbc.sql(
            """
            SELECT e.id, e.occurred_at, e.kind::text, e.arg FROM event_log e
            WHERE e.org_id = :orgId AND e.assignment_id = :id AND e.user_id = :userId
              AND e.occurred_at >= :from AND e.occurred_at < :to
              AND (CAST(:beforeAt AS timestamptz) IS NULL
                   OR (e.occurred_at, e.id) < (CAST(:beforeAt AS timestamptz), CAST(:beforeId AS bigint)))
            ORDER BY e.occurred_at DESC, e.id DESC LIMIT :fetch
            """)
        .param("id", assignmentId)
        .param("userId", userId)
        .param("from", utc(from))
        .param("to", utc(to))
        .param("beforeAt", beforeAt == null ? null : utc(beforeAt))
        .param("beforeId", beforeId)
        .param("fetch", fetch)
        .query((rs, i) ->
            new EventRow(
                rs.getLong(1),
                instant(rs, 2),
                rs.getString(3),
                rs.getObject(4) == null ? null : rs.getInt(4)))
        .list();
  }

  /**
   * Sự kiện của một trường trong [from, to) để tính phiên (M-GEO-08, báo cáo nền, không phải truy
   * vấn nóng); tối đa 200 000 dòng.
   */
  public List<MonitorView.Ev> eventsForSessions(Instant from, Instant to) {
    return jdbc.sql(
            """
            SELECT e.user_id, e.assignment_id, e.kind::text, e.occurred_at FROM event_log e
            WHERE e.org_id = :orgId AND e.occurred_at >= :from AND e.occurred_at < :to
              AND e.kind <> 'scene_shown'
            ORDER BY e.occurred_at LIMIT 200000
            """)
        .param("from", utc(from))
        .param("to", utc(to))
        .query((rs, i) ->
            new MonitorView.Ev(
                rs.getObject(1, UUID.class),
                rs.getObject(2, UUID.class),
                rs.getString(3),
                instant(rs, 4)))
        .list();
  }

  private static OffsetDateTime utc(Instant t) {
    return OffsetDateTime.ofInstant(t, ZoneOffset.UTC);
  }

  private static Instant instant(ResultSet rs, int col) throws SQLException {
    OffsetDateTime t = rs.getObject(col, OffsetDateTime.class);
    return t == null ? null : t.toInstant();
  }
}
