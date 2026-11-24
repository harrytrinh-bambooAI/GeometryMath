package vn.geo.server.it.e7;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.redis.RedisKeys;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.RequestContext;
import vn.geo.server.common.security.Role;
import vn.geo.server.testing.SqlStatementCounter;
import vn.geo.server.tracking.internal.domain.MonitorView;
import vn.geo.server.tracking.internal.service.ClassMonitorService;

/** E7: ghi sự kiện, phiên làm bài, partition theo tháng, Theo dõi lớp không đọc event_log. */
class EventsE7IT extends AbstractE7IT {

  @Autowired ClassMonitorService monitor;
  @Autowired SqlStatementCounter counter;
  @Autowired StringRedisTemplate redis;
  @Autowired RedisKeys keys;

  private List<MonitorView.Ev> eventsOf(UUID org, UUID asg, UUID user) {
    return jdbc.sql(
            """
            SELECT user_id, assignment_id, kind::text, occurred_at FROM event_log
            WHERE org_id = :org AND assignment_id = :asg AND user_id = :user
              AND occurred_at >= now() - interval '2 days' AND occurred_at <= now()
            """)
        .param("org", org)
        .param("asg", asg)
        .param("user", user)
        .query((rs, i) ->
            new MonitorView.Ev(
                rs.getObject(1, UUID.class),
                rs.getObject(2, UUID.class),
                rs.getString(3),
                rs.getObject(4, OffsetDateTime.class).toInstant()))
        .list();
  }

  /** Một học sinh, sự kiện tại các mốc lệch {@code offsets} (giây) tính từ T0 = 2 giờ trước. */
  private int[] sessionsAndMinutes(long... offsets) {
    UUID[] o = newOrg();
    Klass k = newClass(o[0], o[1], 1);
    UUID asg = assign(k);
    UUID u = k.students().get(0);
    Instant t0 =
        Instant.now().minus(Duration.ofHours(2)).truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
    open(k.org(), u, asg, t0);
    for (long off : offsets) {
      tool(k.org(), u, asg, t0.plusSeconds(off));
    }
    List<MonitorView.Session> s = MonitorView.sessions(eventsOf(k.org(), asg, u));
    return new int[] {s.size(), MonitorView.minutes(activeSeconds(k.org(), asg, u))};
  }

  @Test
  @DisplayName("TC-GEO-0725 Ranh giới phiên làm bài: T0, +10 phút, +45 phút (US-GEO-703-SC5)")
  void tc0725_sessionBoundary() {
    assertThat(sessionsAndMinutes(600, 2700)).containsExactly(2, 10);
  }

  @Test
  @DisplayName("TC-GEO-0740 Khoảng nghỉ 29 phút 59 giây vẫn cùng phiên (US-GEO-703-SC8)")
  void tc0740_sameSession() {
    assertThat(sessionsAndMinutes(29 * 60 + 59)).containsExactly(1, 30);
  }

  @Test
  @DisplayName("TC-GEO-0741 Khoảng nghỉ đúng 30 phút 0 giây mở phiên mới (US-GEO-703-SC9)")
  void tc0741_newSession() {
    assertThat(sessionsAndMinutes(30 * 60)).containsExactly(2, 0);
  }

  @Test
  @DisplayName("TC-GEO-0724 Tỉ lệ phiên có thao tác trên hình 100,0% → 80,0% (US-GEO-703-SC4)")
  void tc0724_toolSessionRate() {
    UUID[] o = newOrg();
    Klass k = newClass(o[0], o[1], 5);
    UUID asg = assign(k);
    Instant t = Instant.now().minus(Duration.ofMinutes(50));
    for (int i = 0; i < 4; i++) { // An, Bình, Chi, Dũng: mở bài, có thao tác
      open(k.org(), k.students().get(i), asg, t);
      tool(k.org(), k.students().get(i), asg, t.plusSeconds(60));
    }
    Instant from = Instant.now().minus(Duration.ofHours(3));
    AuthPrincipal teacher = new AuthPrincipal(k.org(), k.teacher(), Role.TEACHER);
    var before =
        RequestContext.callAs(
            teacher, () -> monitor.sessionReport(from, Instant.now().plusSeconds(1)));
    assertThat(before.sessions()).isEqualTo(4);
    assertThat(before.rate()).isEqualTo("100,0%");
    // Hà chỉ xoay hình: chỉ có sự kiện mở bài (xoay không gửi sự kiện)
    open(k.org(), k.students().get(4), asg, t.plusSeconds(120));
    var after =
        RequestContext.callAs(
            teacher, () -> monitor.sessionReport(from, Instant.now().plusSeconds(1)));
    assertThat(after.sessions()).isEqualTo(5);
    assertThat(after.toolSessions()).isEqualTo(4);
    assertThat(after.rate()).isEqualTo("80,0%");
    assertThat(after.threshold()).isEqualTo("ngưỡng đề xuất ≥ 60,0%");
    var empty =
        RequestContext.callAs(teacher, () -> monitor.sessionReport(from, from.plusSeconds(1)));
    assertThat(empty.rate()).isEqualTo("—");
  }

  private String partitionOf(long eventId) {
    return jdbc.sql(
            "SELECT tableoid::regclass::text FROM event_log WHERE id = :id AND occurred_at > now() - interval '2 days'")
        .param("id", eventId)
        .query(String.class)
        .single();
  }

  private String monthPartition(String shift) {
    return jdbc.sql(
            "SELECT format('event_log_y%sm%s', to_char(now() + CAST(:s AS interval), 'YYYY'), to_char(now() + CAST(:s AS interval), 'MM'))")
        .param("s", shift)
        .query(String.class)
        .single();
  }

  @Test
  @DisplayName("TC-GEO-0732 Sự kiện vào đúng phần theo thời gian (US-GEO-704-SC1)")
  void tc0732_currentMonthPartition() {
    UUID[] o = newOrg();
    Klass k = newClass(o[0], o[1], 1);
    UUID asg = assign(k);
    tool(k.org(), k.students().get(0), asg, Instant.now());
    long id =
        jdbc.sql(
                "SELECT max(id) FROM event_log WHERE org_id = :org AND occurred_at > now() - interval '1 hour'")
            .param("org", k.org())
            .query(Long.class)
            .single();
    assertThat(partitionOf(id)).isEqualTo(monthPartition("0 days"));
    assertThat(count("SELECT count(*) FROM event_log_default")).isZero();
  }

  @Test
  @DisplayName("TC-GEO-0733 Luôn có phần cho tháng sau tháng hiện tại (US-GEO-704-SC2)")
  void tc0733_futurePartition() {
    long n =
        jdbc.sql(
                """
                SELECT count(*) FROM pg_inherits i JOIN pg_class c ON c.oid = i.inhrelid
                WHERE i.inhparent = 'event_log'::regclass AND c.relname = :nm
                """)
            .param("nm", monthPartition("1 month"))
            .query(Long.class)
            .single();
    assertThat(n).isEqualTo(1);
  }

  @Test
  @DisplayName("TC-GEO-0737 Giờ thiết bị ở tương lai bị kẹp về giờ nhận (US-GEO-704-FC2)")
  void tc0737_futureDeviceClockClamped() throws Exception {
    UUID[] o = newOrg();
    Klass k = newClass(o[0], o[1], 1);
    UUID asg = assign(k);
    UUID ha = k.students().get(0);
    Instant future = Instant.now().plus(Duration.ofDays(92));
    Instant before = Instant.now();
    var r =
        postJson(
            "/api/v1/student/assignments/" + asg + "/figure-events",
            token(k.org(), ha, Role.STUDENT),
            "{\"batchId\":\""
                + UUID.randomUUID()
                + "\",\"events\":[{\"tool\":2,\"at\":\""
                + future
                + "\"}]}");
    assertThat(r.statusCode()).as(r.body()).isEqualTo(200);
    Instant after = Instant.now();
    var row =
        jdbc.sql(
                """
                SELECT id, occurred_at FROM event_log
                WHERE org_id = :org AND user_id = :u AND kind = 'tool' AND occurred_at > now() - interval '1 hour'
                """)
            .param("org", k.org())
            .param("u", ha)
            .query((rs, i) ->
                new Object[] {rs.getLong(1), rs.getObject(2, OffsetDateTime.class).toInstant()})
            .single();
    Instant at = (Instant) row[1];
    assertThat(at).isBetween(before.minusSeconds(2), after.plusSeconds(2));
    assertThat(partitionOf((Long) row[0])).isEqualTo(monthPartition("0 days"));
    assertThat(count("SELECT count(*) FROM event_log_default")).isZero();
  }

  private void dropCache(UUID org, UUID asg) {
    redis.delete(keys.key("t", org.toString(), "mon", asg.toString()));
  }

  @Test
  @DisplayName(
      "TC-GEO-0712 Theo dõi lớp: cùng số câu lệnh cho lớp 5 và 34 học sinh, không đọc event_log (US-GEO-701-FC6)")
  void tc0712_fixedStatementsNoEventLog() throws Exception {
    // lớp 34 học sinh của Giáo viên mẫu (cùng trường với 11A1), mỗi em có sự kiện
    Klass big = newClass(ORG_A, TEACHER_A, 34);
    UUID asg = assign(big);
    for (UUID u : big.students()) {
      open(ORG_A, u, asg, Instant.now().minusSeconds(300));
      tool(ORG_A, u, asg, Instant.now().minusSeconds(200));
    }
    dropCache(ORG_A, BG1);
    dropCache(ORG_A, asg);
    String small = "e7-mon-5-" + Uuid7.next();
    String large = "e7-mon-34-" + Uuid7.next();
    var r5 = get("/api/v1/teacher/assignments/" + BG1 + "/monitor", teacherA(), small);
    var r34 = get("/api/v1/teacher/assignments/" + asg + "/monitor", teacherA(), large);
    assertThat(r5.statusCode()).as(r5.body()).isEqualTo(200);
    assertThat(r34.statusCode()).as(r34.body()).isEqualTo(200);
    assertThat(json(r5).get("studentCount").asInt()).isEqualTo(5);
    assertThat(json(r34).get("studentCount").asInt()).isEqualTo(34);
    assertThat(json(r34).get("openedCount").asInt()).isEqualTo(34);
    assertThat(counter.count(small)).as("%s", counter.statements(small)).isPositive();
    assertThat(counter.count(large))
        .as("%s / %s", counter.statements(small), counter.statements(large))
        .isEqualTo(counter.count(small));
    for (String s : counter.statements(small)) {
      assertThat(s.toLowerCase()).doesNotContain("event_log");
    }
    for (String s : counter.statements(large)) {
      assertThat(s.toLowerCase()).doesNotContain("event_log");
    }
  }

  @Test
  @DisplayName("Thời điểm cho trước nằm ngoài [now − 24 giờ, now] bị kẹp (ADR-GEO-06)")
  void clampPast() {
    UUID[] o = newOrg();
    Klass k = newClass(o[0], o[1], 1);
    UUID asg = assign(k);
    tool(k.org(), k.students().get(0), asg, Instant.now().minus(Duration.ofDays(40)));
    Instant at =
        jdbc.sql(
                "SELECT occurred_at FROM event_log WHERE org_id = :org AND occurred_at > now() - interval '2 days'")
            .param("org", k.org())
            .query(OffsetDateTime.class)
            .single()
            .toInstant();
    assertThat(at).isAfter(Instant.now().minus(Duration.ofHours(25)));
    assertThat(OffsetDateTime.ofInstant(at, ZoneOffset.UTC)).isNotNull();
  }
}
