package vn.geo.server.learning.internal.repo;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.db.TenantJdbc;

/**
 * Kiểm soát đáp án đầy đủ (E6): ngữ cảnh lời giải đang khóa qua hàm cổng V60 (không bao giờ gửi
 * xuống thiết bị) và nhật ký chặn answer_guard_log (không user_id, không nội dung; D-GEO-067).
 */
@Repository
public class GuardRepository {

  /** forms: JSON mảng chuỗi (solution.answer_forms). */
  public record Context(
      String status,
      boolean locked,
      String finalAnswer,
      String steps,
      String forms,
      String ownFinalAnswer,
      String ownSteps,
      String ownForms) {}

  public record Catalog(String finalAnswer, String steps, String forms) {}

  private final TenantJdbc jdbc;

  public GuardRepository(TenantJdbc jdbc) {
    this.jdbc = jdbc;
  }

  public Context context(UUID userId, UUID assignmentId) {
    return jdbc.sql(
            """
            SELECT status, locked, final_answer, steps::text, answer_forms::text,
                   own_final_answer, own_steps::text, own_answer_forms::text
            FROM fn_guard_context(:orgId, :userId, :id)
            """)
        .param("userId", userId)
        .param("id", assignmentId)
        .query((rs, i) ->
            new Context(
                rs.getString(1),
                rs.getBoolean(2),
                rs.getString(3),
                rs.getString(4),
                rs.getString(5),
                rs.getString(6),
                rs.getString(7),
                rs.getString(8)))
        .single();
  }

  public Optional<Catalog> catalog(UUID userId, UUID problemVersionId) {
    return jdbc.sql(
            """
            SELECT final_answer, steps::text, answer_forms::text
            FROM fn_guard_context_catalog(:orgId, :userId, :pv)
            """)
        .param("userId", userId)
        .param("pv", problemVersionId)
        .query((rs, i) -> new Catalog(rs.getString(1), rs.getString(2), rs.getString(3)))
        .optional();
  }

  /** Một lần chặn hoặc một lần đóng khi lỗi (BR-GEO-305). */
  public void log(UUID assignmentId, String outcome, String rule, String version, int latencyMs) {
    jdbc.sql(
            "SELECT fn_guard_log(:orgId, :id, CAST(:outcome AS guard_outcome), :rule, :version, :ms)")
        .param("id", assignmentId)
        .param("outcome", outcome)
        .param("rule", rule)
        .param("version", version)
        .param("ms", Math.max(0, latencyMs))
        .query(Integer.class)
        .single();
  }
}
