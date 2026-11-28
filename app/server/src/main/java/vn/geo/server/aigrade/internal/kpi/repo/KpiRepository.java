package vn.geo.server.aigrade.internal.kpi.repo;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.id.Uuid7;

/**
 * Số liệu KPI theo bài {@code ai_kpi_paper} và trạng thái cổng lớp {@code ai_class_gate} (V825;
 * T-261128-65). Dựng lại số liệu từ cặp chạy song song (V755), đề xuất AI và tín hiệu engine
 * (V725), hành động duyệt (V755) — chỉ thêm bài chưa có.
 */
@Repository
public class KpiRepository {

  private static final OffsetDateTime NEG_INF =
      OffsetDateTime.of(1, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

  private final JdbcClient jdbc;

  public KpiRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  /** Hàng số liệu (criteria là JSON thô). */
  public record PaperRow(
      UUID submissionId,
      String topic,
      String criteria,
      Boolean resultOk,
      int invented,
      int blocked,
      boolean noGrade,
      Integer reviewSeconds,
      Instant createdAt) {}

  /** Trạng thái cổng; {@code since} = null nghĩa là tính từ đầu. */
  public record Gate(
      Instant openedAt,
      Instant since,
      int failStreak,
      int batchesDone,
      Instant closedAt,
      String closeReason) {}

  public record ClassRow(UUID id, String name, UUID teacherId) {}

  public Optional<ClassRow> classInfo(UUID orgId, UUID classId) {
    return jdbc.sql(
            """
            SELECT c.id, c.name, c.teacher_id FROM class c WHERE c.org_id = :orgId AND c.id = :classId LIMIT 1
            -- sqllint:allow=ownership đọc lớp (iam): tên, giáo viên chủ lớp cho báo cáo khớp AI và thông báo cổng
            """)
        .param("orgId", orgId)
        .param("classId", classId)
        .query((rs, i) ->
            new ClassRow(rs.getObject(1, UUID.class), rs.getString(2), rs.getObject(3, UUID.class)))
        .optional();
  }

  // ------------------------------------------------------------------ dựng số liệu từ dữ liệu thật

  /** Bài song song đã có điểm AI ở mọi tiêu chí → một hàng shadow (bỏ qua bài đã có). */
  public int refreshShadow(UUID orgId, UUID classId) {
    return jdbc.sql(
            """
            INSERT INTO ai_kpi_paper (org_id, submission_id, kind, class_id, topic, criteria, result_ok, invented, blocked, no_grade)
            SELECT p.org_id, p.submission_id, 'shadow', p.class_id,
                   coalesce(left(max(pt.name), 80), 'khác'),
                   jsonb_agg(jsonb_build_object('max', p.max_points, 'ai', p.ai_points, 'gv', p.teacher_points) ORDER BY p.ord),
                   CASE max(es.status)
                     WHEN 'match' THEN bool_and(p.name <> 'Kết quả' OR p.ai_points >= p.max_points)
                     WHEN 'mismatch' THEN bool_and(p.name <> 'Kết quả' OR p.ai_points < p.max_points)
                   END,
                   coalesce(max(inv.n), 0), 0, false
            FROM ai_shadow_pair p
            LEFT JOIN engine_signal es ON es.org_id = p.org_id AND es.proposal_id = p.proposal_id
            LEFT JOIN LATERAL (
              SELECT count(*) AS n FROM criterion_assessment ca
              WHERE ca.org_id = p.org_id AND ca.proposal_id = p.proposal_id AND ca.flags @> '["bịa kiến thức"]'::jsonb
            ) inv ON true
            LEFT JOIN graded_assignment g ON g.org_id = p.org_id AND g.id = p.assignment_id
            LEFT JOIN problem_version pv ON pv.id = g.problem_version_id
            LEFT JOIN problem pr ON pr.id = pv.problem_id
            LEFT JOIN problem_topic pt ON pt.id = pr.topic_id
            WHERE p.org_id = :orgId AND p.class_id = :classId
              AND NOT EXISTS (SELECT 1 FROM ai_kpi_paper k WHERE k.org_id = :orgId AND k.submission_id = p.submission_id AND k.kind = 'shadow')
            GROUP BY p.org_id, p.submission_id, p.class_id
            HAVING bool_and(p.ai_points IS NOT NULL)
            ON CONFLICT DO NOTHING
            -- sqllint:allow=tenant,ownership read model: dạng bài của đề (assessment, content; ngân hàng dùng chung không lọc org) và tín hiệu engine, cờ bịa kiến thức (aigrade)
            """)
        .param("orgId", orgId)
        .param("classId", classId)
        .update();
  }

  /** Bài song song mà AI không chấm (đề xuất mới nhất no_grade) → hàng shadow no_grade. */
  public int refreshNoGrade(UUID orgId, UUID classId) {
    return jdbc.sql(
            """
            INSERT INTO ai_kpi_paper (org_id, submission_id, kind, class_id, topic, no_grade)
            SELECT DISTINCT ON (a.submission_id) a.org_id, a.submission_id, 'shadow', a.class_id, 'khác', true
            FROM ai_grade_proposal a
            JOIN graded_assignment g ON g.org_id = a.org_id AND g.id = a.assignment_id AND g.grading_mode = 'parallel'
            WHERE a.org_id = :orgId AND a.class_id = :classId AND a.status = 'no_grade'
              AND NOT EXISTS (SELECT 1 FROM ai_grade_proposal b WHERE b.org_id = :orgId AND b.submission_id = a.submission_id AND b.status = 'proposed')
              AND NOT EXISTS (SELECT 1 FROM ai_kpi_paper k WHERE k.org_id = :orgId AND k.submission_id = a.submission_id AND k.kind = 'shadow')
            ORDER BY a.submission_id, a.created_at DESC
            ON CONFLICT DO NOTHING
            -- sqllint:allow=tenant,ownership read model: chế độ chấm của bài có chấm (assessment)
            """)
        .param("orgId", orgId)
        .param("classId", classId)
        .update();
  }

  /** Bài AI đề xuất đã duyệt lần đầu → hàng proposal (tiêu chí bị sửa = sửa hoặc bỏ). */
  public int refreshProposal(UUID orgId, UUID classId) {
    return jdbc.sql(
            """
            INSERT INTO ai_kpi_paper (org_id, submission_id, kind, class_id, topic, criteria, review_seconds)
            SELECT r.org_id, r.submission_id, 'proposal', a.class_id, 'khác',
                   jsonb_agg(jsonb_build_object('max', r.max_points, 'edited', r.action <> 'accept') ORDER BY r.ord),
                   max(rs.secs)
            FROM ai_review_action r
            JOIN ai_grade_proposal a ON a.org_id = r.org_id AND a.id = r.proposal_id
            LEFT JOIN LATERAL (
              SELECT floor(extract(epoch FROM ag.approved_at - o.opened_at))::int AS secs
              FROM grade_review_open o
              JOIN approved_grade ag ON ag.org_id = o.org_id AND ag.submission_id = o.submission_id AND ag.seq = 1
              WHERE o.org_id = r.org_id AND o.submission_id = r.submission_id AND ag.approved_at >= o.opened_at
              LIMIT 1
            ) rs ON true
            WHERE r.org_id = :orgId AND a.class_id = :classId AND r.grade_seq = 1
              AND NOT EXISTS (SELECT 1 FROM ai_kpi_paper k WHERE k.org_id = :orgId AND k.submission_id = r.submission_id AND k.kind = 'proposal')
            GROUP BY r.org_id, r.submission_id, a.class_id
            ON CONFLICT DO NOTHING
            -- sqllint:allow=tenant,ownership điều kiện org nối theo hành động duyệt; KPI (g) đọc giờ mở màn chấm + giờ Duyệt đầu (assessment, V847)
            """)
        .param("orgId", orgId)
        .param("classId", classId)
        .update();
  }

  // ------------------------------------------------------------------ đọc số liệu

  public List<PaperRow> papers(UUID orgId, UUID classId, String kind, Instant after) {
    return jdbc.sql(
            """
            SELECT k.submission_id, k.topic, k.criteria::text AS criteria, k.result_ok, k.invented, k.blocked, k.no_grade,
                   k.review_seconds, k.created_at
            FROM ai_kpi_paper k
            WHERE k.org_id = :orgId AND k.class_id = :classId AND k.kind = :kind AND k.created_at > :after
            ORDER BY k.created_at, k.submission_id
            """)
        .param("orgId", orgId)
        .param("classId", classId)
        .param("kind", kind)
        .param("after", after == null ? NEG_INF : OffsetDateTime.ofInstant(after, ZoneOffset.UTC))
        .query((rs, i) ->
            new PaperRow(
                rs.getObject("submission_id", UUID.class),
                rs.getString("topic"),
                rs.getString("criteria"),
                (Boolean) rs.getObject("result_ok"),
                rs.getInt("invented"),
                rs.getInt("blocked"),
                rs.getBoolean("no_grade"),
                (Integer) rs.getObject("review_seconds"),
                rs.getObject("created_at", OffsetDateTime.class).toInstant()))
        .list();
  }

  // ------------------------------------------------------------------ cổng

  /** Khoá hàng cổng của lớp (tạo nếu chưa có). */
  public Gate lockGate(UUID orgId, UUID classId) {
    jdbc.sql(
            """
            INSERT INTO ai_class_gate (org_id, class_id) VALUES (:orgId, :classId) ON CONFLICT DO NOTHING
            """)
        .param("orgId", orgId)
        .param("classId", classId)
        .update();
    return jdbc.sql(
            """
            SELECT g.opened_at, g.since, g.fail_streak, g.batches_done, g.closed_at, g.close_reason
            FROM ai_class_gate g WHERE g.org_id = :orgId AND g.class_id = :classId FOR UPDATE
            """)
        .param("orgId", orgId)
        .param("classId", classId)
        .query((rs, i) ->
            new Gate(
                instant(rs.getObject("opened_at", OffsetDateTime.class)),
                "-infinity".equals(rs.getString("since"))
                    ? null
                    : instant(rs.getObject("since", OffsetDateTime.class)),
                rs.getInt("fail_streak"),
                rs.getInt("batches_done"),
                instant(rs.getObject("closed_at", OffsetDateTime.class)),
                rs.getString("close_reason")))
        .single();
  }

  public Optional<Instant> openedAt(UUID orgId, UUID classId) {
    return jdbc.sql(
            "SELECT g.opened_at FROM ai_class_gate g WHERE g.org_id = :orgId AND g.class_id = :classId")
        .param("orgId", orgId)
        .param("classId", classId)
        .query((rs, i) -> Optional.ofNullable(instant(rs.getObject(1, OffsetDateTime.class))))
        .optional()
        .flatMap(o -> o);
  }

  public void open(UUID orgId, UUID classId) {
    jdbc.sql(
            """
            UPDATE ai_class_gate SET opened_at = clock_timestamp(), fail_streak = 0, batches_done = 0, updated_at = now()
            WHERE org_id = :orgId AND class_id = :classId
            """)
        .param("orgId", orgId)
        .param("classId", classId)
        .update();
  }

  public void progress(UUID orgId, UUID classId, int streak, int batches) {
    jdbc.sql(
            """
            UPDATE ai_class_gate SET fail_streak = :s, batches_done = :b, updated_at = now()
            WHERE org_id = :orgId AND class_id = :classId
            """)
        .param("orgId", orgId)
        .param("classId", classId)
        .param("s", streak)
        .param("b", batches)
        .update();
  }

  public void close(UUID orgId, UUID classId, String reason) {
    jdbc.sql(
            """
            UPDATE ai_class_gate SET opened_at = NULL, since = clock_timestamp(), closed_at = clock_timestamp(), close_reason = :r,
                   fail_streak = 0, batches_done = 0, updated_at = now()
            WHERE org_id = :orgId AND class_id = :classId
            """)
        .param("orgId", orgId)
        .param("classId", classId)
        .param("r", reason.length() > 300 ? reason.substring(0, 300) : reason)
        .update();
  }

  /** Thông báo trong ứng dụng (kênh S3-E4) tới giáo viên chủ lớp: cổng tự đóng. */
  public void notifyTeacher(UUID orgId, UUID teacherId, String body) {
    jdbc.sql(
            """
            INSERT INTO notification (id, org_id, user_id, kind, event_id, set_id, body)
            VALUES (:id, :orgId, :userId, 'ai_gate', :event, NULL, :body)
            -- sqllint:allow=ownership cổng AI đề xuất tự đóng: báo giáo viên qua kênh thông báo trong ứng dụng S3-E4 (BR-S5E6-17)
            """)
        .param("id", Uuid7.next())
        .param("orgId", orgId)
        .param("userId", teacherId)
        .param("event", UUID.randomUUID())
        .param("body", body)
        .update();
  }

  // ------------------------------------------------------------------ móc kiểm thử

  /** Chỉ móc kiểm thử dev/test: thêm một bài số liệu với giờ tạo là giờ máy chủ lúc gọi. */
  public void insertPaper(
      UUID orgId,
      UUID classId,
      String kind,
      String topic,
      String criteriaJson,
      Boolean resultOk,
      int invented,
      boolean noGrade,
      Integer reviewSeconds) {
    jdbc.sql(
            """
            INSERT INTO ai_kpi_paper (org_id, submission_id, kind, class_id, topic, criteria, result_ok, invented, no_grade, review_seconds)
            VALUES (:orgId, :sid, :kind, :classId, :topic, CAST(:crit AS jsonb), :ok, :inv, :ng, :rs)
            """)
        .param("orgId", orgId)
        .param("sid", Uuid7.next())
        .param("kind", kind)
        .param("classId", classId)
        .param("topic", topic)
        .param("crit", criteriaJson)
        .param("ok", resultOk)
        .param("inv", invented)
        .param("ng", noGrade)
        .param("rs", reviewSeconds)
        .update();
  }

  private static Instant instant(OffsetDateTime t) {
    return t == null ? null : t.toInstant();
  }
}
