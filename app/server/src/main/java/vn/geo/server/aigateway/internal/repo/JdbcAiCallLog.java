package vn.geo.server.aigateway.internal.repo;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import vn.geo.server.aigateway.internal.AiCallLog;

/** Bảng ai_call_log (chỉ thêm, chia theo tháng; không chứa nội dung câu hỏi hay tên, D-GEO-067). */
@Repository
public class JdbcAiCallLog implements AiCallLog {

  private static final String INSERT =
      """
      INSERT INTO ai_call_log (occurred_at, org_id, user_id, purpose, model, prompt_version, tokens_in, tokens_out,
                               cost_micro_usd, latency_ms, status)
      VALUES (now(), :orgId, :userId, CAST(:purpose AS ai_purpose), :model, :promptVersion, :tokensIn, :tokensOut,
              :cost, :latency, CAST(:status AS ai_status))
      """;

  private final JdbcClient jdbc;

  public JdbcAiCallLog(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public void record(Entry e) {
    jdbc.sql(INSERT)
        .param("orgId", e.orgId())
        .param("userId", e.userId())
        .param("purpose", e.purpose())
        .param("model", e.model())
        .param("promptVersion", e.promptVersion())
        .param("tokensIn", e.tokensIn())
        .param("tokensOut", e.tokensOut())
        .param("cost", e.costMicro())
        .param("latency", (int) Math.min(Integer.MAX_VALUE, e.latencyMs()))
        .param("status", e.status())
        .update();
  }
}
