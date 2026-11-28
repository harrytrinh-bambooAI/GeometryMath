package vn.geo.server.aigateway.internal.repo.quota;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.id.Uuid7;

/**
 * SQL của công tắc, bộ đếm và hạn mức AI chặng 5 (US-S5E5-04). Mọi câu lệnh trên bảng theo trường
 * lọc {@code org_id = :orgId} với mã trường đã xác thực do tầng dịch vụ truyền vào (bộ lọc chạy
 * trước khi bộ xử lý của Spring dựng ngữ cảnh trường).
 */
@Repository
public class QuotaRepository {

  /** Công tắc lớp (không có dòng = mặc định QS5-26: AI chấm tắt, Trợ lý bật). */
  public record Switch(boolean aiGrading, boolean assistant) {}

  /** Lớp: tên, giáo viên chủ, AI phía học sinh (class.ai_enabled, S3-E5). */
  public record ClassInfo(UUID id, String name, UUID teacherId, boolean studentAi) {}

  private final JdbcClient jdbc;

  public QuotaRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  // ------------------------------------------------------------------ lớp

  public Optional<ClassInfo> classInfo(UUID orgId, UUID classId) {
    return jdbc.sql(
            """
            SELECT c.id, c.name, c.teacher_id, c.ai_enabled FROM class c
            WHERE c.org_id = :orgId AND c.id = :classId LIMIT 1
            -- sqllint:allow=ownership đọc lớp (iam): tên, giáo viên chủ, công tắc AI phía học sinh (US-S3E5-04)
            """)
        .param("orgId", orgId)
        .param("classId", classId)
        .query((rs, i) ->
            new ClassInfo(
                rs.getObject(1, UUID.class),
                rs.getString(2),
                rs.getObject(3, UUID.class),
                rs.getBoolean(4)))
        .optional();
  }

  /** Lớp của một bài được giao mà học sinh là thành viên (bài tự học: rỗng). */
  public Optional<UUID> classOfAssignment(UUID orgId, UUID studentId, UUID assignmentId) {
    return jdbc.sql(
            """
            SELECT a.class_id FROM assignment a
            WHERE a.org_id = :orgId AND a.id = :aid AND a.kind = 'class' AND a.class_id IS NOT NULL
              AND EXISTS (SELECT 1 FROM class_member m WHERE m.org_id = :orgId AND m.class_id = a.class_id
                          AND m.user_id = :userId LIMIT 1)
            LIMIT 1
            -- sqllint:allow=ownership lớp của bài (assignment, iam) để tính hạn mức hỏi đáp theo lớp (BR-S5E5-12)
            """)
        .param("orgId", orgId)
        .param("aid", assignmentId)
        .param("userId", studentId)
        .query(UUID.class)
        .optional();
  }

  // ------------------------------------------------------------------ công tắc lớp

  public Switch classSwitch(UUID orgId, UUID classId) {
    return jdbc.sql(
            """
            SELECT s.ai_grading, s.assistant FROM ai_class_switch s
            WHERE s.org_id = :orgId AND s.class_id = :classId LIMIT 1
            """)
        .param("orgId", orgId)
        .param("classId", classId)
        .query((rs, i) -> new Switch(rs.getBoolean(1), rs.getBoolean(2)))
        .optional()
        .orElse(new Switch(false, true));
  }

  public void saveSwitch(UUID orgId, UUID classId, Switch s, UUID actor) {
    jdbc.sql(
            """
            INSERT INTO ai_class_switch (org_id, class_id, ai_grading, assistant, updated_by, updated_at)
            VALUES (:orgId, :classId, :grading, :assistant, :actor, now())
            ON CONFLICT (org_id, class_id) DO UPDATE
              SET ai_grading = EXCLUDED.ai_grading, assistant = EXCLUDED.assistant,
                  updated_by = EXCLUDED.updated_by, updated_at = now()
            """)
        .param("orgId", orgId)
        .param("classId", classId)
        .param("grading", s.aiGrading())
        .param("assistant", s.assistant())
        .param("actor", actor)
        .update();
  }

  public void audit(
      UUID orgId,
      UUID actor,
      String role,
      UUID classId,
      String name,
      boolean before,
      boolean after) {
    jdbc.sql(
            """
            INSERT INTO ai_switch_audit (id, org_id, actor_id, actor_role, class_id, switch_name, before_on, after_on)
            VALUES (:id, :orgId, :actor, :role, :classId, :name, :before, :after)
            """)
        .param("id", Uuid7.next())
        .param("orgId", orgId)
        .param("actor", actor)
        .param("role", role)
        .param("classId", classId)
        .param("name", name)
        .param("before", before)
        .param("after", after)
        .update();
  }

  /** Nhật ký đổi công tắc của một lớp, mới nhất trước. */
  public List<Map<String, Object>> auditOf(UUID orgId, UUID classId, int limit) {
    return jdbc.sql(
            """
            SELECT a.actor_id, a.actor_role, a.switch_name, a.before_on, a.after_on, a.at
            FROM ai_switch_audit a WHERE a.org_id = :orgId AND a.class_id = :classId
            ORDER BY a.at DESC, a.id DESC LIMIT :limit
            """)
        .param("orgId", orgId)
        .param("classId", classId)
        .param("limit", limit)
        .query((rs, i) -> {
          Map<String, Object> m = new LinkedHashMap<>();
          m.put("actorId", rs.getObject(1, UUID.class));
          m.put("actorRole", rs.getString(2));
          m.put("switch", rs.getString(3));
          m.put("before", rs.getBoolean(4));
          m.put("after", rs.getBoolean(5));
          m.put("at", rs.getTimestamp(6).toInstant());
          return m;
        })
        .list();
  }

  // ------------------------------------------------------------------ bộ đếm

  public long counter(UUID orgId, String scope, UUID scopeId, String metric, LocalDate day) {
    return jdbc.sql(
            """
            SELECT coalesce(max(u.n), 0) FROM ai_usage_counter u
            WHERE u.org_id = :orgId AND u.scope = :scope AND u.scope_id = :id AND u.metric = :metric AND u.day = :day
            """)
        .param("orgId", orgId)
        .param("scope", scope)
        .param("id", scopeId)
        .param("metric", metric)
        .param("day", day)
        .query(Long.class)
        .single();
  }

  /** Cộng {@code delta} rồi trả giá trị mới (nguyên tử nhờ ON CONFLICT … RETURNING). */
  public long add(
      UUID orgId, String scope, UUID scopeId, String metric, LocalDate day, long delta) {
    return jdbc.sql(
            """
            INSERT INTO ai_usage_counter (org_id, day, scope, scope_id, metric, n)
            VALUES (:orgId, :day, :scope, :id, :metric, :delta)
            ON CONFLICT (org_id, scope, scope_id, metric, day) DO UPDATE SET n = ai_usage_counter.n + EXCLUDED.n
            RETURNING n
            """)
        .param("orgId", orgId)
        .param("day", day)
        .param("scope", scope)
        .param("id", scopeId)
        .param("metric", metric)
        .param("delta", delta)
        .query(Long.class)
        .single();
  }

  /** Đặt giá trị bộ đếm (móc kiểm thử dựng "đã dùng n lượt hôm nay"). */
  public void set(UUID orgId, String scope, UUID scopeId, String metric, LocalDate day, long n) {
    jdbc.sql(
            """
            INSERT INTO ai_usage_counter (org_id, day, scope, scope_id, metric, n)
            VALUES (:orgId, :day, :scope, :id, :metric, :n)
            ON CONFLICT (org_id, scope, scope_id, metric, day) DO UPDATE SET n = EXCLUDED.n
            """)
        .param("orgId", orgId)
        .param("day", day)
        .param("scope", scope)
        .param("id", scopeId)
        .param("metric", metric)
        .param("n", n)
        .update();
  }

  /** Tổng theo chỉ số của một lớp từ ngày {@code from} (báo cáo "Dùng AI", BR-S5E5-13). */
  public Map<String, Long> classTotals(UUID orgId, UUID classId, LocalDate from) {
    Map<String, Long> out = new LinkedHashMap<>();
    jdbc.sql(
            """
            SELECT u.metric, sum(u.n) FROM ai_usage_counter u
            WHERE u.org_id = :orgId AND u.scope = 'class' AND u.scope_id = :classId AND u.day >= :from
            GROUP BY u.metric ORDER BY u.metric LIMIT 100
            """)
        .param("orgId", orgId)
        .param("classId", classId)
        .param("from", from)
        .query((rs, i) -> out.put(rs.getString(1), rs.getLong(2)))
        .list();
    return out;
  }

  /** Lượt soạn giáo án/vá của giáo viên trong ngày (bảng lesson_plan_turn của S5-E1). */
  public int lessonTurns(UUID orgId, UUID teacherId, LocalDate day) {
    return jdbc.sql(
            """
            SELECT count(*) FROM lesson_plan_turn t
            WHERE t.org_id = :orgId AND t.teacher_id = :teacher AND t.turn_day = :day AND t.counted
            -- sqllint:allow=ownership,unbounded lượt soạn giáo án (lessonplan) cộng vào hạn mức soạn chung 30/ngày (BR-S5E5-12)
            """)
        .param("orgId", orgId)
        .param("teacher", teacherId)
        .param("day", day)
        .query(Integer.class)
        .single();
  }

  /** Token (vào + ra) các lượt gọi AI của một người từ {@code since}. */
  public long tokensSince(UUID orgId, UUID userId, Instant since) {
    return jdbc.sql(
            """
            SELECT coalesce(sum(l.tokens_in + l.tokens_out), 0) FROM ai_call_log l
            WHERE l.org_id = :orgId AND l.user_id = :userId AND l.occurred_at >= :since
            -- sqllint:allow=unbounded tổng token của MỘT lượt hỏi vừa xong (một người, vài giây)
            """)
        .param("orgId", orgId)
        .param("userId", userId)
        .param("since", Timestamp.from(since))
        .query(Long.class)
        .single();
  }

  /** Ghi cảnh báo một lần mỗi (lớp, ngày, loại). true nếu là lần đầu. */
  public boolean warnOnce(UUID orgId, UUID classId, LocalDate day, String kind) {
    return jdbc.sql(
                """
                INSERT INTO ai_quota_warning (org_id, class_id, day, kind) VALUES (:orgId, :classId, :day, :kind)
                ON CONFLICT DO NOTHING
                """)
            .param("orgId", orgId)
            .param("classId", classId)
            .param("day", day)
            .param("kind", kind)
            .update()
        > 0;
  }

  /** Thông báo trong ứng dụng (kênh S3-E4) tới giáo viên chủ lớp. */
  public void notifyTeacher(UUID orgId, UUID teacherId, String body) {
    jdbc.sql(
            """
            INSERT INTO notification (id, org_id, user_id, kind, event_id, set_id, body)
            VALUES (:id, :orgId, :userId, 'ai_quota', :event, NULL, :body)
            -- sqllint:allow=ownership cảnh báo hạn mức AI gửi qua kênh thông báo trong ứng dụng S3-E4 (BR-S5E5-12)
            """)
        .param("id", Uuid7.next())
        .param("orgId", orgId)
        .param("userId", teacherId)
        .param("event", UUID.randomUUID())
        .param("body", body)
        .update();
  }

  // ------------------------------------------------------------------ đọc lại, việc nền

  public int ocrRetries(UUID orgId, UUID submissionId) {
    return jdbc.sql(
            "SELECT coalesce(max(r.n), 0) FROM ai_ocr_retry r WHERE r.org_id = :orgId"
                + " AND r.submission_id = :sub")
        .param("orgId", orgId)
        .param("sub", submissionId)
        .query(Integer.class)
        .single();
  }

  /** Tăng số lần đọc lại nếu còn dưới 3; trả số mới hoặc -1 nếu đã đủ 3. */
  public int incOcr(UUID orgId, UUID submissionId) {
    return jdbc.sql(
            """
            INSERT INTO ai_ocr_retry (org_id, submission_id, n) VALUES (:orgId, :sub, 1)
            ON CONFLICT (org_id, submission_id) DO UPDATE SET n = ai_ocr_retry.n + 1
              WHERE ai_ocr_retry.n < 3
            RETURNING n
            """)
        .param("orgId", orgId)
        .param("sub", submissionId)
        .query(Integer.class)
        .optional()
        .orElse(-1);
  }

  public void setOcr(UUID orgId, UUID submissionId, int n) {
    jdbc.sql(
            """
            INSERT INTO ai_ocr_retry (org_id, submission_id, n) VALUES (:orgId, :sub, :n)
            ON CONFLICT (org_id, submission_id) DO UPDATE SET n = EXCLUDED.n
            """)
        .param("orgId", orgId)
        .param("sub", submissionId)
        .param("n", n)
        .update();
  }

  public UUID insertJob(UUID orgId, UUID classId, String task, UUID refId) {
    UUID id = Uuid7.next();
    jdbc.sql(
            """
            INSERT INTO ai_pending_job (id, org_id, class_id, task, ref_id) VALUES (:id, :orgId, :classId, :task, :ref)
            """)
        .param("id", id)
        .param("orgId", orgId)
        .param("classId", classId)
        .param("task", task)
        .param("ref", refId)
        .update();
    return id;
  }

  public Optional<String> jobStatus(UUID orgId, UUID jobId) {
    return jdbc.sql(
            "SELECT j.status FROM ai_pending_job j WHERE j.org_id = :orgId AND j.id = :id LIMIT 1")
        .param("orgId", orgId)
        .param("id", jobId)
        .query(String.class)
        .optional();
  }

  public void finishJob(UUID orgId, UUID jobId) {
    jdbc.sql(
            """
            UPDATE ai_pending_job SET status = 'done', updated_at = now()
            WHERE org_id = :orgId AND id = :id AND status = 'pending'
            """)
        .param("orgId", orgId)
        .param("id", jobId)
        .update();
  }

  /** Huỷ mọi việc chấm nền đang chờ của lớp (BR-S5E5-11). Trả số việc bị huỷ. */
  public int cancelPendingGrading(UUID orgId, UUID classId) {
    return jdbc.sql(
            """
            UPDATE ai_pending_job SET status = 'cancelled', updated_at = now()
            WHERE org_id = :orgId AND class_id = :classId AND status = 'pending'
            """)
        .param("orgId", orgId)
        .param("classId", classId)
        .update();
  }
}
