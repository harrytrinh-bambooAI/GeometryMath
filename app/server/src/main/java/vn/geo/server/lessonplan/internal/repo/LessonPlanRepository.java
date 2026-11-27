package vn.geo.server.lessonplan.internal.repo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.db.TenantJdbc;
import vn.geo.server.common.id.Uuid7;

/**
 * Bảng lesson_plan, lesson_plan_version, lesson_plan_turn (V525). Mọi câu lọc theo trường; đọc /
 * ghi giáo án lọc thêm theo chính giáo viên chủ (quyền theo quan hệ: giáo viên khác NOT_FOUND).
 */
@Repository
public class LessonPlanRepository {

  /** Một giáo án (nội dung JSON giữ nguyên chuỗi; dịch vụ tự đọc). */
  public record Row(
      UUID id,
      UUID teacherId,
      String code,
      String periodId,
      String requestJson,
      String status,
      String contentJson,
      String replyJson,
      String checkLogJson,
      int fixKeys,
      int aiCalls,
      int version,
      String humanLabel,
      List<String> lockedSections,
      Instant createdAt,
      Instant updatedAt) {}

  private static final String COLS =
      "id, teacher_id, code, period_id, request::text AS request, status, content::text AS content,"
          + " reply::text AS reply, check_log::text AS check_log, fix_keys, ai_calls, version,"
          + " human_label, locked_sections, created_at, updated_at";

  private final TenantJdbc jdbc;
  private final org.springframework.jdbc.core.simple.JdbcClient raw;

  public LessonPlanRepository(
      TenantJdbc jdbc, org.springframework.jdbc.core.simple.JdbcClient raw) {
    this.jdbc = jdbc;
    this.raw = raw;
  }

  /** Tạo giáo án "Đang soạn"; mã GA-nnnn cấp từ dãy số. */
  public Row insert(UUID teacherId, String periodId, String requestJson) {
    UUID id = Uuid7.next();
    jdbc.sql(
            """
            INSERT INTO lesson_plan (id, org_id, teacher_id, code, period_id, request, status)
            VALUES (:id, :orgId, :teacher, 'GA-' || lpad(nextval('lesson_plan_code_seq')::text, 4, '0'),
                    :period, CAST(:request AS jsonb), 'drafting')
            """)
        .param("id", id)
        .param("teacher", teacherId)
        .param("period", periodId)
        .param("request", requestJson)
        .update();
    return byId(teacherId, id).orElseThrow();
  }

  public Optional<Row> byId(UUID teacherId, UUID id) {
    return jdbc.sql(
            "SELECT "
                + COLS
                + " FROM lesson_plan WHERE org_id = :orgId AND teacher_id = :teacher AND id = :id"
                + " LIMIT 1")
        .param("teacher", teacherId)
        .param("id", id)
        .query((rs, i) -> row(rs))
        .optional();
  }

  public Optional<Row> byCode(UUID teacherId, String code) {
    return jdbc.sql(
            "SELECT "
                + COLS
                + " FROM lesson_plan WHERE org_id = :orgId AND teacher_id = :teacher"
                + " AND code = :code LIMIT 1")
        .param("teacher", teacherId)
        .param("code", code)
        .query((rs, i) -> row(rs))
        .optional();
  }

  /** Bắt đầu lại một lượt soạn (Thử lại): trạng thái "Đang soạn", xoá câu hỏi lại cũ. */
  public void restart(UUID id) {
    jdbc.sql(
            "UPDATE lesson_plan SET status = 'drafting', reply = NULL, updated_at = now()"
                + " WHERE org_id = :orgId AND id = :id")
        .param("id", id)
        .update();
  }

  /**
   * Kết thúc một lượt soạn. {@code contentJson} khác null = bản hợp lệ, lưu thành phiên bản mới.
   */
  public void finish(
      UUID id,
      String status,
      String contentJson,
      String replyJson,
      String checkLogJson,
      int fixKeys,
      int aiCalls) {
    jdbc.sql(
            """
            UPDATE lesson_plan SET status = :status,
                   content = COALESCE(CAST(:content AS jsonb), content),
                   reply = CAST(:reply AS jsonb), check_log = CAST(:log AS jsonb),
                   fix_keys = fix_keys + :fix, ai_calls = ai_calls + :calls,
                   version = version + CASE WHEN CAST(:content AS jsonb) IS NULL THEN 0 ELSE 1 END,
                   updated_at = now()
            WHERE org_id = :orgId AND id = :id
            """)
        .param("id", id)
        .param("status", status)
        .param("content", contentJson)
        .param("reply", replyJson)
        .param("log", checkLogJson)
        .param("fix", fixKeys)
        .param("calls", aiCalls)
        .update();
    if (contentJson != null) {
      jdbc.sql(
              """
              INSERT INTO lesson_plan_version (plan_id, org_id, version, content)
              SELECT id, org_id, version, content FROM lesson_plan WHERE org_id = :orgId AND id = :id
              """)
          .param("id", id)
          .update();
    }
  }

  // ---------------------------------------------------------------- lượt AI soạn (BR-S5E1-08)

  /** Số lượt đang tính của giáo viên trong ngày (giờ Việt Nam). */
  public int turnsOn(UUID teacherId, LocalDate day) {
    return jdbc.sql(
            "SELECT count(*) FROM lesson_plan_turn WHERE org_id = :orgId AND teacher_id = :teacher"
                + " AND turn_day = :day AND counted")
        .param("teacher", teacherId)
        .param("day", day)
        .query(Integer.class)
        .single();
  }

  public UUID addTurn(UUID teacherId, LocalDate day, String kind, UUID planId) {
    UUID id = Uuid7.next();
    jdbc.sql(
            """
            INSERT INTO lesson_plan_turn (id, org_id, teacher_id, turn_day, kind, plan_id)
            VALUES (:id, :orgId, :teacher, :day, :kind, :plan)
            """)
        .param("id", id)
        .param("teacher", teacherId)
        .param("day", day)
        .param("kind", kind)
        .param("plan", planId)
        .update();
    return id;
  }

  /** Lượt hỏng sau gọi lại không tính vào hạn mức (US-S5E1-02-FC2). */
  public void uncount(UUID turnId) {
    jdbc.sql("UPDATE lesson_plan_turn SET counted = false WHERE org_id = :orgId AND id = :id")
        .param("id", turnId)
        .update();
  }

  // ---------------------------------------------------------------- móc kiểm thử (dev/test)

  /** Móc kiểm thử: thêm n lượt đã tính trong ngày cho giáo viên (US-S5E1-02-FC4). */
  public void seedTurns(UUID orgId, UUID teacherId, LocalDate day, int n) {
    raw.sql(
            """
            INSERT INTO lesson_plan_turn (id, org_id, teacher_id, turn_day, kind)
            SELECT gen_random_uuid(), :orgId, :teacher, :day, 'plan' FROM generate_series(1, :n)
            """)
        .param("orgId", orgId)
        .param("teacher", teacherId)
        .param("day", day)
        .param("n", n)
        .update();
  }

  /** Móc kiểm thử: số liệu nội bộ của một giáo án (khoá đã chuẩn hoá, số lần gọi, nhật ký kiểm). */
  public Optional<java.util.Map<String, Object>> internals(UUID orgId, UUID id) {
    return raw.sql(
            "SELECT fix_keys, ai_calls, check_log::text AS check_log, content::text AS content"
                + " FROM lesson_plan WHERE org_id = :orgId AND id = :id LIMIT 1")
        .param("orgId", orgId)
        .param("id", id)
        .query((rs, i) -> {
          java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
          m.put("fixKeys", rs.getInt("fix_keys"));
          m.put("aiCalls", rs.getInt("ai_calls"));
          m.put("checkLog", rs.getString("check_log"));
          m.put("content", rs.getString("content"));
          return m;
        })
        .optional();
  }

  private static Row row(ResultSet rs) throws SQLException {
    java.sql.Array locked = rs.getArray("locked_sections");
    return new Row(
        rs.getObject("id", UUID.class),
        rs.getObject("teacher_id", UUID.class),
        rs.getString("code"),
        rs.getString("period_id"),
        rs.getString("request"),
        rs.getString("status"),
        rs.getString("content"),
        rs.getString("reply"),
        rs.getString("check_log"),
        rs.getInt("fix_keys"),
        rs.getInt("ai_calls"),
        rs.getInt("version"),
        rs.getString("human_label"),
        locked == null ? List.of() : List.of((String[]) locked.getArray()),
        rs.getTimestamp("created_at").toInstant(),
        rs.getTimestamp("updated_at").toInstant());
  }
}
