package vn.geo.server.tracking.internal.repo;

import java.util.UUID;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.db.TenantJdbc;

/**
 * Sự kiện thao tác trên hình (E3, D-GEO-058): ghi lô mức A qua fn_record_batch (kiểm tư cách lớp
 * hoặc chủ bài tự học trong hàm), sự kiện mở bài mức B qua fn_record_durable, gợi ý qua
 * fn_get_hint. geo_app không INSERT trực tiếp event_log (RV-10).
 */
@Repository
public class FigureEventRepository {

  public record BatchResult(String status, int inserted) {}

  private final TenantJdbc jdbc;

  public FigureEventRepository(TenantJdbc jdbc) {
    this.jdbc = jdbc;
  }

  /**
   * {@code eventsJson}: mảng {at: ISO-8601, tool: 1…20}, theo thứ tự. Lũy đẳng bền theo {@code
   * batchId} (durable_request, V85; trả 'DUPLICATE' khi lô đã ghi).
   */
  public BatchResult recordTools(UUID userId, UUID assignmentId, UUID batchId, String eventsJson) {
    return jdbc.sql(
            """
            WITH e AS (SELECT x.v, x.n FROM jsonb_array_elements(CAST(:ev AS jsonb)) WITH ORDINALITY AS x(v, n))
            SELECT status, inserted FROM fn_record_batch(:orgId, :userId, :id,
                   ARRAY(SELECT CAST(e.v->>'at' AS timestamptz) FROM e ORDER BY e.n),
                   ARRAY(SELECT CAST('tool' AS event_kind) FROM e ORDER BY e.n),
                   ARRAY(SELECT CAST(e.v->>'tool' AS smallint) FROM e ORDER BY e.n), :batchId)
            """)
        .param("userId", userId)
        .param("id", assignmentId)
        .param("batchId", batchId)
        .param("ev", eventsJson)
        .query((rs, i) -> new BatchResult(rs.getString(1), rs.getInt(2)))
        .single();
  }

  public String recordOpen(UUID userId, UUID assignmentId, UUID requestId) {
    return jdbc.sql(
            "SELECT fn_record_durable(:orgId, :userId, :id, :requestId, CAST('open' AS event_kind))")
        .param("userId", userId)
        .param("id", assignmentId)
        .param("requestId", requestId)
        .query(String.class)
        .single();
  }
}
