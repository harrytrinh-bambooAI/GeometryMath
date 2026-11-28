package vn.geo.server.learning.internal.repo;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Số đo M-GEO-09 (US-GEO-401-SC4): thời lượng từ lúc gửi đề tới lúc hình hiện của bài tự học (sự
 * kiện 'scene_shown', đơn vị 100 ms) và số lượt dựng hình lỗi (lượt gọi AI mục đích 'scene' không
 * thành công) trong một kỳ. Chỉ số hệ thống, gộp mọi trường, không trả dữ liệu của trường nào.
 */
@Repository
public class SceneTimingRepository {

  private final JdbcClient system;

  public SceneTimingRepository(JdbcClient system) {
    this.system = system;
  }

  /** Thời lượng (100 ms) của các sự kiện scene_shown trong [from, to). */
  public List<Long> sceneShownTenths(Instant from, Instant to) {
    return system
        .sql(
            """
            SELECT e.arg FROM event_log e
            WHERE e.kind = 'scene_shown' AND e.occurred_at >= :from AND e.occurred_at < :to
            ORDER BY e.arg LIMIT 100000
            -- sqllint:allow=ownership,tenant chỉ số hệ thống M-GEO-09 (gộp mọi trường, chỉ thời lượng)
            """)
        .param("from", OffsetDateTime.ofInstant(from, ZoneOffset.UTC))
        .param("to", OffsetDateTime.ofInstant(to, ZoneOffset.UTC))
        .query(Long.class)
        .list();
  }

  /** Số lượt dựng hình lỗi trong [from, to). */
  public long failedBuilds(Instant from, Instant to) {
    return system
        .sql(
            """
            SELECT count(*) FROM ai_call_log c
            WHERE c.purpose = 'scene' AND c.status <> 'ok' AND c.occurred_at >= :from AND c.occurred_at < :to
            -- sqllint:allow=ownership,tenant,unbounded chỉ số hệ thống M-GEO-09 (đếm gộp, không trả dữ liệu)
            """)
        .param("from", OffsetDateTime.ofInstant(from, ZoneOffset.UTC))
        .param("to", OffsetDateTime.ofInstant(to, ZoneOffset.UTC))
        .query(Long.class)
        .single();
  }
}
