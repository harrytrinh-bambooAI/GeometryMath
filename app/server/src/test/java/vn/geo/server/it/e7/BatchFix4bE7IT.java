package vn.geo.server.it.e7;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.redis.RedisKeys;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.RequestContext;
import vn.geo.server.common.security.Role;
import vn.geo.server.tracking.internal.service.FigureEventService;
import vn.geo.server.tracking.internal.service.FigureEventService.ToolEvent;

/**
 * FIX4b (soát E7): lô sự kiện thao tác trên hình. Dev-E7-07 (M2): thời gian trong một lô theo quy
 * tắc 30 phút (BR-GEO-278). Dev-E7-08 (M3): lũy đẳng lô bền trong CSDL (BR-GEO-280).
 */
class BatchFix4bE7IT extends AbstractE7IT {

  @Autowired FigureEventService events;
  @Autowired StringRedisTemplate redis;
  @Autowired RedisKeys keys;

  private record Ctx(Klass k, UUID asg, UUID user, AuthPrincipal who) {}

  private Ctx ctx() {
    UUID[] o = newOrg();
    Klass k = newClass(o[0], o[1], 1);
    UUID asg = assign(k);
    UUID u = k.students().get(0);
    return new Ctx(k, asg, u, new AuthPrincipal(k.org(), u, Role.STUDENT));
  }

  private int send(Ctx c, UUID batch, List<ToolEvent> evs) throws Exception {
    return RequestContext.callAs(c.who(), () -> events.recordTools(c.asg(), batch, evs)).inserted();
  }

  private long toolCount(Ctx c) {
    return jdbc.sql(
            "SELECT tool_count FROM assignment_progress WHERE org_id = :org AND assignment_id = :asg AND user_id = :u")
        .param("org", c.k().org())
        .param("asg", c.asg())
        .param("u", c.user())
        .query(Long.class)
        .single();
  }

  private long eventRows(Ctx c) {
    return jdbc.sql(
            """
            SELECT count(*) FROM event_log WHERE org_id = :org AND assignment_id = :asg AND user_id = :u
              AND kind = 'tool' AND occurred_at >= now() - interval '2 days' AND occurred_at <= now()
            """)
        .param("org", c.k().org())
        .param("asg", c.asg())
        .param("u", c.user())
        .query(Long.class)
        .single();
  }

  @Test
  @DisplayName("Dev-E7-07 lô hai sự kiện cách 23 giờ không cộng 23 giờ vào thời gian làm bài")
  void devE707_batch23hApart() throws Exception {
    Ctx c = ctx();
    Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    send(
        c,
        Uuid7.next(),
        List.of(new ToolEvent(1, now.minus(Duration.ofHours(23))), new ToolEvent(2, now)));
    assertThat(toolCount(c)).isEqualTo(2);
    assertThat(activeSeconds(c.k().org(), c.asg(), c.user())).isZero();
  }

  @Test
  @DisplayName("Dev-E7-07 lô T0, +10′, +50′, +55′: chỉ cộng khoảng < 30 phút (15 phút)")
  void devE707_batchPairs() throws Exception {
    Ctx c = ctx();
    Instant t0 = Instant.now().minus(Duration.ofHours(2)).truncatedTo(ChronoUnit.SECONDS);
    send(
        c,
        Uuid7.next(),
        List.of(
            new ToolEvent(1, t0),
            new ToolEvent(2, t0.plusSeconds(600)),
            new ToolEvent(3, t0.plusSeconds(3000)),
            new ToolEvent(4, t0.plusSeconds(3300))));
    assertThat(activeSeconds(c.k().org(), c.asg(), c.user())).isEqualTo(900);
  }

  @Test
  @DisplayName("Dev-E7-08 gửi lại cùng batchId sau khi khóa Redis hết hạn/bị xóa không cộng đôi")
  void devE708_resendAfterRedisKeyGone() throws Exception {
    Ctx c = ctx();
    UUID batch = Uuid7.next();
    Instant now = Instant.now();
    List<ToolEvent> evs = List.of(new ToolEvent(1, now), new ToolEvent(6, now));
    assertThat(send(c, batch, evs)).isEqualTo(2);
    // khóa đường nhanh mất (hết hạn, flush, evict, hoặc chưa kịp ghi "done" khi tiến trình chết)
    redis.delete(keys.key("figbatch", c.k().org().toString(), batch.toString()));
    assertThat(send(c, batch, evs)).isZero();
    assertThat(toolCount(c)).isEqualTo(2);
    assertThat(eventRows(c)).isEqualTo(2);
  }

  @Test
  @DisplayName("Dev-E7-08 hai lần gửi đồng thời cùng batchId: ghi đúng một lần")
  void devE708_concurrentSameBatch() throws Exception {
    Ctx c = ctx();
    UUID batch = Uuid7.next();
    List<ToolEvent> evs = List.of(new ToolEvent(3, Instant.now()));
    ExecutorService pool = Executors.newFixedThreadPool(4);
    try {
      Callable<Integer> call = () -> send(c, batch, evs);
      List<Future<Integer>> fs = pool.invokeAll(List.of(call, call, call, call));
      int total = 0;
      for (Future<Integer> f : fs) {
        total += f.get();
      }
      assertThat(total).isEqualTo(1);
    } finally {
      pool.shutdownNow();
    }
    assertThat(toolCount(c)).isEqualTo(1);
    assertThat(eventRows(c)).isEqualTo(1);
  }
}
