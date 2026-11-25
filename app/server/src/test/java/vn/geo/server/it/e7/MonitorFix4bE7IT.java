package vn.geo.server.it.e7;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.RequestContext;
import vn.geo.server.common.security.Role;
import vn.geo.server.tracking.internal.service.ClassMonitorService;
import vn.geo.server.tracking.internal.service.TrackingService;
import vn.geo.server.tracking.internal.service.TrackingService.Monitor;

/** FIX4b (soát E7, m3, m4, m5, m8): Theo dõi lớp ở ranh giới và đọc sự kiện. */
class MonitorFix4bE7IT extends AbstractE7IT {

  @Autowired TrackingService tracking;
  @Autowired ClassMonitorService monitor;

  private Monitor monitorOf(Klass k, UUID asg) throws Exception {
    return RequestContext.callAs(
        new AuthPrincipal(k.org(), k.teacher(), Role.TEACHER), () -> tracking.monitor(asg));
  }

  private void ask(Klass k, UUID asg, UUID user, String q) {
    String s =
        jdbc.sql(
                "SELECT status FROM fn_record_qa(:org, :user, :asg, :rq, :q, 'concept', 'Trả lời mẫu.', 'g0-fix4b')")
            .param("org", k.org())
            .param("user", user)
            .param("asg", asg)
            .param("rq", Uuid7.next())
            .param("q", q)
            .query(String.class)
            .single();
    assertThat(s).isEqualTo("OK");
  }

  @Test
  @DisplayName("FIX4b m3 lớp 101 em: bảng 100 dòng và báo bị cắt; lớp 100 em: không báo")
  void m3_studentsBoundary() throws Exception {
    UUID[] o = newOrg();
    Klass big = newClass(o[0], o[1], 101);
    Monitor m = monitorOf(big, assign(big));
    assertThat(m.students()).hasSize(TrackingService.MONITOR_STUDENTS);
    assertThat(m.studentsTruncated()).isTrue();
    Klass full = newClass(o[0], o[1], 100);
    Monitor m2 = monitorOf(full, assign(full));
    assertThat(m2.students()).hasSize(100);
    assertThat(m2.studentsTruncated()).isFalse();
  }

  @Test
  @DisplayName("FIX4b m3 bài trên 2000 câu hỏi: giữ câu MỚI nhất và báo bị cắt")
  void m3_questionsKeepNewest() throws Exception {
    UUID[] o = newOrg();
    Klass k = newClass(o[0], o[1], 1);
    UUID asg = assign(k);
    jdbc.sql(
            """
            INSERT INTO qa_turn (org_id, assignment_id, user_id, request_id, asked_at, question, reply_kind,
                                 reply_text, guard_version)
            SELECT :org, :asg, :user, gen_random_uuid(), now() - (2100 - g) * interval '1 second',
                   'Câu ' || g, 'concept', 'Trả lời mẫu.', 'g0-fix4b'
            FROM generate_series(1, 2001) g ORDER BY g
            """)
        .param("org", k.org())
        .param("asg", asg)
        .param("user", k.students().get(0))
        .update();
    Monitor m = monitorOf(k, asg);
    assertThat(m.questionsTruncated()).isTrue();
    assertThat(m.askedQuestions()).hasSize(TrackingService.MONITOR_QUESTIONS);
    assertThat(m.askedQuestions().get(0).question()).isEqualTo("Câu 2001");
    assertThat(m.questions().get(0).question()).isEqualTo("Câu 2001");
  }

  @Test
  @DisplayName("FIX4b m4 em rời lớp: thẻ \"câu hỏi\" và khối \"đang hỏi gì\" cùng một nguồn")
  void m4_singleSource() throws Exception {
    UUID[] o = newOrg();
    Klass k = newClass(o[0], o[1], 2);
    UUID asg = assign(k);
    UUID stay = k.students().get(0);
    UUID leave = k.students().get(1);
    ask(k, asg, stay, "Vì sao SA vuông góc BC?");
    ask(k, asg, leave, "Vì sao SA vuông góc BC?");
    ask(k, asg, leave, "Góc giữa hai mặt là gì?");
    jdbc.sql("UPDATE class_member SET left_at = now() WHERE org_id = :org AND user_id = :u")
        .param("org", k.org())
        .param("u", leave)
        .update();
    Monitor m = monitorOf(k, asg);
    int grouped = m.questionGroups().stream().mapToInt(g -> g.count()).sum();
    assertThat(m.questionCount()).isEqualTo(1);
    assertThat(grouped).isEqualTo(m.questionCount());
    assertThat(m.askedQuestions()).hasSize(1);
  }

  @Test
  @DisplayName("FIX4b m5 đọc sự kiện của người không thuộc lớp: 404; khoảng > 366 ngày: 400")
  void m5_eventsGuards() throws Exception {
    UUID[] o = newOrg();
    Klass k = newClass(o[0], o[1], 1);
    UUID asg = assign(k);
    AuthPrincipal teacher = new AuthPrincipal(k.org(), k.teacher(), Role.TEACHER);
    Klass other = newClass(o[0], o[1], 1);
    assertThatThrownBy(() ->
            RequestContext.callAs(
                teacher,
                () -> monitor.events(asg, other.students().get(0), null, null, null, null)))
        .isInstanceOfSatisfying(
            AppException.class, e -> assertThat(e.code()).isEqualTo(ErrorCode.NOT_FOUND));
    assertThatThrownBy(() ->
            RequestContext.callAs(
                teacher, () -> monitor.events(asg, UUID.randomUUID(), null, null, null, null)))
        .isInstanceOfSatisfying(
            AppException.class, e -> assertThat(e.code()).isEqualTo(ErrorCode.NOT_FOUND));
    Instant now = Instant.now();
    UUID u = k.students().get(0);
    assertThatThrownBy(() ->
            RequestContext.callAs(
                teacher,
                () -> monitor.events(asg, u, now.minus(Duration.ofDays(400)), now, null, null)))
        .isInstanceOfSatisfying(
            AppException.class, e -> assertThat(e.code()).isEqualTo(ErrorCode.VALIDATION_FAILED));
    var page =
        RequestContext.callAs(
            teacher,
            () -> monitor.events(asg, u, now.minus(Duration.ofDays(366)), now, null, null));
    assertThat(page.items()).isEmpty();
  }

  @Test
  @DisplayName(
      "Dev-E7-06 \"Đổi giờ\" rồi tải lại ngay: Theo dõi lớp không trả bản đệm có giờ mở cũ")
  void devE706_changeTimeBypassesCache() throws Exception {
    UUID[] o = newOrg();
    Klass k = newClass(o[0], o[1], 1);
    UUID asg = assign(k);
    Monitor before = monitorOf(k, asg); // ghi đệm 5 s
    assertThat(before.releaseState()).isEqualTo("upcoming");
    String s =
        jdbc.sql(
                "SELECT status FROM fn_set_release(:org, :t, :asg, false, now() + interval '3 hours', 0, NULL)")
            .param("org", k.org())
            .param("t", k.teacher())
            .param("asg", asg)
            .query(String.class)
            .single();
    assertThat(s).isEqualTo("OK");
    Monitor after = monitorOf(k, asg);
    assertThat(after.releaseState()).isEqualTo("upcoming");
    assertThat(after.releaseAt()).isAfter(before.releaseAt().plus(Duration.ofHours(1)));
  }
}
