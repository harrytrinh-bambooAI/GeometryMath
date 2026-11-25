package vn.geo.server.it.e3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import vn.geo.server.common.db.TenantJdbc;
import vn.geo.server.common.redis.RedisKeys;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.RequestContext;
import vn.geo.server.common.security.Role;
import vn.geo.server.it.e1.AbstractE1IT;
import vn.geo.server.learning.api.HintApi;
import vn.geo.server.tracking.internal.repo.FigureEventRepository;
import vn.geo.server.tracking.internal.service.FigureEventService;
import vn.geo.server.tracking.internal.service.FigureEventService.ToolEvent;

/**
 * Soát E3 #6 (ca Dev-E3-07, qc_findings.md của E3): khóa chống ghi trùng của lô sự kiện thao tác
 * chỉ đánh dấu "đã ghi" SAU KHI ghi CSDL thành công. Ghi lỗi lần đầu (CSDL giả lập hỏng) thì lần
 * gửi lại CÙNG batchId phải được ghi; gửi lại lần nữa thì không ghi trùng.
 */
class FigureBatchE3IT extends AbstractE1IT {

  @Autowired TenantJdbc tenantJdbc;
  @Autowired StringRedisTemplate redis;
  @Autowired RedisKeys keys;
  @Autowired HintApi hints;

  @Test
  @DisplayName(
      "Dev-E3-07 lô ghi lỗi lần đầu: gửi lại cùng batchId được ghi đủ, lần sau không trùng")
  void devE307_retryAfterDbFailure() throws Exception {
    AtomicBoolean failNext = new AtomicBoolean(true);
    FigureEventRepository flaky =
        new FigureEventRepository(tenantJdbc) {
          @Override
          public BatchResult recordTools(
              UUID userId, UUID assignmentId, UUID batchId, String eventsJson) {
            if (failNext.getAndSet(false)) {
              throw new DataAccessResourceFailureException("CSDL giả lập hỏng");
            }
            return super.recordTools(userId, assignmentId, batchId, eventsJson);
          }
        };
    FigureEventService service = new FigureEventService(flaky, redis, keys, mapper, hints);
    AuthPrincipal an = new AuthPrincipal(ORG_A, AN, Role.STUDENT);
    UUID batch = UUID.randomUUID();
    List<ToolEvent> events =
        List.of(new ToolEvent(1, Instant.now()), new ToolEvent(6, Instant.now()));
    long before = count("SELECT count(*) FROM event_log WHERE kind = 'tool'");

    assertThatThrownBy(
            () -> RequestContext.callAs(an, () -> service.recordTools(BG1, batch, events)))
        .isInstanceOf(DataAccessResourceFailureException.class);
    var second = RequestContext.callAs(an, () -> service.recordTools(BG1, batch, events));
    assertThat(second.inserted()).isEqualTo(2);
    var third = RequestContext.callAs(an, () -> service.recordTools(BG1, batch, events));
    assertThat(third.inserted()).isZero();
    assertThat(count("SELECT count(*) FROM event_log WHERE kind = 'tool'")).isEqualTo(before + 2);
  }
}
