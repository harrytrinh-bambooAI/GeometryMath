package vn.geo.server.it.s3e6;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataIntegrityViolationException;
import vn.geo.server.assignment.internal.service.ReminderService;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.iam.internal.service.ClassTestWorld.ClassSpec;
import vn.geo.server.iam.internal.service.ClassTestWorld.Spec;
import vn.geo.server.iam.internal.service.ClassTestWorld.World;
import vn.geo.server.it.s3e4.AbstractS3E4IT;

/**
 * US-S3E6-03 — tác vụ nhắc việc trong ứng dụng (T-261126-31, mở rộng): sắp hết hạn 24 giờ trước hạn
 * cho em chưa xong, giờ yên lặng 21:00–06:00, chạy lặp, lời giải mở theo lịch, đổi hạn, bộ giao
 * muộn, bộ không hạn, em không Đang học, bộ Đã đóng, chỉ trong ứng dụng, sai số ≤ 1 phút. Đồng hồ
 * giả lập qua {@link ReminderService#runAt(Instant)}; các mốc ở tháng 12/2026 nên tác vụ chạy nền
 * theo giờ thật không chạm tới.
 */
class ReminderS3E6IT extends AbstractS3E4IT {

  private static final ZoneId VN = ZoneId.of("Asia/Ho_Chi_Minh");
  private static final List<String> ACTIVE_8 =
      List.of(
          "Bình An",
          "Chi Mai",
          "Đức Huy",
          "Gia Bảo",
          "Hà My",
          "Khánh Linh",
          "Minh Quân",
          "Ngọc Anh");
  private static final List<String> DONE_4 = List.of("Bình An", "Đức Huy", "Hà My", "Minh Quân");
  private static final List<String> UNFINISHED_4 =
      List.of("Chi Mai", "Gia Bảo", "Khánh Linh", "Ngọc Anh");

  @Autowired ReminderService reminders;
  @Autowired ApplicationContext context;

  /** F-S3-010…030: 11A1 của cô Lan, 8 em Đang học, 3 Chờ duyệt, 1 Đã rời, 1 Bị từ chối. */
  private World world11A1() {
    return worlds.build(
        new Spec(
            List.of("Cô Lan"),
            List.of(),
            List.of(
                new ClassSpec(
                    "11A1",
                    "Cô Lan",
                    "11A1",
                    11,
                    ACTIVE_8,
                    List.of("Hoàng Nam", "Thu Hà", "Phương Vy"),
                    List.of("Văn Tú"),
                    List.of("Lê Bình")))));
  }

  private static Instant vn(int day, int hour, int minute, int second) {
    return LocalDateTime.of(2026, 12, day, hour, minute, second).atZone(VN).toInstant();
  }

  private static OffsetDateTime odt(Instant t) {
    return OffsetDateTime.ofInstant(t, ZoneOffset.UTC);
  }

  private String lan(World w) {
    return w.people().get("Cô Lan").token();
  }

  private UUID c11a1(World w) {
    return w.classes().get("11A1").id();
  }

  /** Bộ "Bộ bài tuần 15" (2 đề) Đã giao, hạn {@code due} (null = không hạn). */
  private UUID setWithDue(World w, String title, Instant due) throws Exception {
    return setWithDue(w, title, due, 1, 2);
  }

  /** Như trên với hai đề khác (một đề chỉ giao một lần cho một lớp). */
  private UUID setWithDue(World w, String title, Instant due, int... problems) throws Exception {
    UUID set = UUID.fromString(assigned(lan(w), c11a1(w), title, null, problems));
    jdbc.sql("UPDATE assignment_set SET due_at = :due WHERE id = :id")
        .param("due", due == null ? null : odt(due))
        .param("id", set)
        .update();
    return set;
  }

  /** Em đã xong mọi đề của bộ (F-S3-051): một lượt Đúng trên từng đề. */
  private void finish(World w, UUID set, String student) {
    List<UUID> assignments =
        jdbc.sql(
                "SELECT assignment_id FROM assignment_item WHERE set_id = :s AND assignment_id IS NOT NULL")
            .param("s", set)
            .query(UUID.class)
            .list();
    assertThat(assignments).hasSize(2);
    for (UUID a : assignments) {
      jdbc.sql(
              """
              INSERT INTO practice_attempt (id, org_id, assignment_id, user_id, request_key, submitted, outcome, wrong_after)
              VALUES (:id, :org, :a, :u, :k, '1', 'correct', 0)
              """)
          .param("id", Uuid7.next())
          .param("org", w.orgId())
          .param("a", a)
          .param("u", w.people().get(student).id())
          .param("k", "it-s3e6-" + UUID.randomUUID().toString().substring(0, 8))
          .update();
    }
  }

  /** Tên học sinh → số thông báo loại {@code kind} của bộ. */
  private Map<String, Long> received(World w, UUID set, String kind) {
    Map<UUID, String> names =
        w.people().entrySet().stream()
            .collect(Collectors.toMap(e -> e.getValue().id(), Map.Entry::getKey));
    return jdbc
        .sql("SELECT user_id FROM notification WHERE set_id = :s AND kind = :k ORDER BY user_id")
        .param("s", set)
        .param("k", kind)
        .query(UUID.class)
        .list()
        .stream()
        .collect(Collectors.groupingBy(names::get, Collectors.counting()));
  }

  private List<String> bodies(UUID set, String kind) {
    return jdbc.sql("SELECT DISTINCT body FROM notification WHERE set_id = :s AND kind = :k")
        .param("s", set)
        .param("k", kind)
        .query(String.class)
        .list();
  }

  private static Map<String, Long> once(List<String> names) {
    return names.stream().collect(Collectors.toMap(n -> n, n -> 1L));
  }

  @Test
  @DisplayName(
      "TC-S3E6-27 Nhắc sắp hết hạn tới đúng bốn em chưa xong: Chi Mai, Gia Bảo, Khánh Linh, Ngọc Anh mỗi em một MSG-S3-95")
  void dueSoonToUnfinished() throws Exception {
    World w = world11A1();
    UUID set = setWithDue(w, "Bộ bài tuần 15", vn(4, 20, 0, 0));
    DONE_4.forEach(n -> finish(w, set, n));
    reminders.runAt(vn(3, 20, 0, 0));
    assertThat(received(w, set, "due_soon")).isEqualTo(once(UNFINISHED_4));
    assertThat(bodies(set, "due_soon"))
        .containsExactly("Bộ bài tuần 15 sắp hết hạn: 20:00, 04/12/2026");
  }

  @Test
  @DisplayName("TC-S3E6-28 Em đã xong bộ không nhận nhắc: Bình An, Đức Huy, Hà My, Minh Quân")
  void finishedGetNothing() throws Exception {
    World w = world11A1();
    UUID set = setWithDue(w, "Bộ bài tuần 15", vn(4, 20, 0, 0));
    DONE_4.forEach(n -> finish(w, set, n));
    reminders.runAt(vn(3, 20, 0, 0));
    assertThat(received(w, set, "due_soon")).doesNotContainKeys(DONE_4.toArray(String[]::new));
  }

  @Test
  @DisplayName(
      "TC-S3E6-29 Biên giờ yên lặng: nhắc 20:59:59 gửi ngay, 21:00:00 hoãn tới 06:00 hôm sau, 06:00:00 gửi ngay")
  void quietHourBoundaries() throws Exception {
    World w = world11A1();
    UUID a = setWithDue(w, "Bộ A", vn(4, 20, 59, 59));
    UUID b = setWithDue(w, "Bộ B", vn(8, 21, 0, 0), 3, 4);
    UUID c = setWithDue(w, "Bộ C", vn(11, 6, 0, 0), 5, 6);
    reminders.runAt(vn(3, 20, 59, 59));
    assertThat(received(w, a, "due_soon")).isEqualTo(once(ACTIVE_8));
    reminders.runAt(vn(7, 21, 0, 0));
    assertThat(received(w, b, "due_soon")).as("21:00:00 đã yên lặng").isEmpty();
    reminders.runAt(vn(8, 5, 59, 0));
    assertThat(received(w, b, "due_soon")).as("chưa tới 06:00").isEmpty();
    reminders.runAt(vn(8, 6, 0, 0));
    assertThat(received(w, b, "due_soon")).isEqualTo(once(ACTIVE_8));
    reminders.runAt(vn(10, 6, 0, 0));
    assertThat(received(w, c, "due_soon")).isEqualTo(once(ACTIVE_8));
  }

  @Test
  @DisplayName(
      "TC-S3E6-30 Nhắc rơi giữa đêm (02:00) được hoãn tới 06:00: Gia Bảo nhận một MSG-S3-95")
  void midnightDeferred() throws Exception {
    World w = world11A1();
    UUID set = setWithDue(w, "Bộ bài tuần 15", vn(5, 2, 0, 0));
    reminders.runAt(vn(4, 2, 0, 0));
    assertThat(received(w, set, "due_soon")).isEmpty();
    reminders.runAt(vn(4, 6, 0, 0));
    assertThat(received(w, set, "due_soon")).containsEntry("Gia Bảo", 1L);
  }

  @Test
  @DisplayName(
      "TC-S3E6-31 Tác vụ chạy lặp không gửi hai lần; ràng buộc duy nhất từ chối bản ghi trùng")
  void replayNoDuplicate() throws Exception {
    World w = world11A1();
    UUID set = setWithDue(w, "Bộ bài tuần 15", vn(4, 20, 0, 0));
    DONE_4.forEach(n -> finish(w, set, n));
    for (int i = 0; i < 4; i++) {
      reminders.runAt(vn(3, 20, 0, 0).plusSeconds(i * 60L));
    }
    assertThat(received(w, set, "due_soon")).isEqualTo(once(UNFINISHED_4));
    UUID event =
        jdbc.sql(
                "SELECT event_id FROM notification WHERE set_id = :s AND kind = 'due_soon' LIMIT 1")
            .param("s", set)
            .query(UUID.class)
            .single();
    assertThatThrownBy(() ->
            jdbc.sql(
                    "INSERT INTO notification (id, org_id, user_id, kind, event_id, set_id, body)"
                        + " VALUES (:id, :org, :user, 'due_soon', :event, :set, 'x')")
                .param("id", Uuid7.next())
                .param("org", w.orgId())
                .param("user", w.people().get("Gia Bảo").id())
                .param("event", event)
                .param("set", set)
                .update())
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  @DisplayName(
      "TC-S3E6-32 Tới giờ mở lời giải theo lịch: 8 em Đang học mỗi em một \"Lời giải Bộ bài tuần 15 đã mở\", 3 em Chờ duyệt không nhận, chạy lại vẫn một")
  void scheduledSolutionOpen() throws Exception {
    World w = world11A1();
    UUID set = setWithDue(w, "Bộ bài tuần 15", null);
    jdbc.sql("UPDATE assignment_set SET release_at = :r WHERE id = :id")
        .param("r", odt(vn(6, 8, 0, 0)))
        .param("id", set)
        .update();
    reminders.runAt(vn(6, 7, 59, 0));
    assertThat(received(w, set, "solution_open")).isEmpty();
    reminders.runAt(vn(6, 8, 0, 0));
    reminders.runAt(vn(6, 8, 1, 0));
    assertThat(received(w, set, "solution_open")).isEqualTo(once(ACTIVE_8));
    assertThat(bodies(set, "solution_open")).containsExactly("Lời giải Bộ bài tuần 15 đã mở");
  }

  @Test
  @DisplayName(
      "Dev-REV-B5 Duyệt theo trang tới hết: cỡ trang 1 vẫn nhắc đủ ba bộ sắp hết hạn (hai bộ cùng giờ hạn) và hai bộ mở lời giải")
  void pagesThroughAllSets() throws Exception {
    World w = world11A1();
    UUID a = setWithDue(w, "Bộ A", vn(4, 20, 0, 0), 1, 2);
    UUID b = setWithDue(w, "Bộ B", vn(4, 20, 0, 0), 3, 4);
    UUID c = setWithDue(w, "Bộ C", vn(4, 20, 30, 0), 5, 6);
    for (UUID s : List.of(a, b)) {
      jdbc.sql("UPDATE assignment_set SET release_at = :r WHERE id = :id")
          .param("r", odt(vn(3, 20, 0, 0)))
          .param("id", s)
          .update();
    }
    ReminderService.Run run = reminders.runAt(vn(3, 20, 30, 0), 1);
    assertThat(run.dueSoonEvents()).isGreaterThanOrEqualTo(3);
    assertThat(run.releasedEvents()).isGreaterThanOrEqualTo(2);
    for (UUID s : List.of(a, b, c)) {
      assertThat(received(w, s, "due_soon")).as("bộ " + s).isEqualTo(once(ACTIVE_8));
    }
    for (UUID s : List.of(a, b)) {
      assertThat(received(w, s, "solution_open")).as("bộ " + s).isEqualTo(once(ACTIVE_8));
    }
  }

  @Test
  @DisplayName(
      "TC-S3E6-33 Giáo viên đổi hạn: lúc 03/12 20:00 không nhắc; lúc 04/12 20:00 bốn em chưa xong nhận nhắc hạn \"20:00, 05/12/2026\"")
  void dueChanged() throws Exception {
    World w = world11A1();
    UUID set = setWithDue(w, "Bộ bài tuần 15", vn(4, 20, 0, 0));
    DONE_4.forEach(n -> finish(w, set, n));
    // Cô Lan đổi hạn (cột due_at như API sửa hạn của S3-E2 ghi).
    jdbc.sql("UPDATE assignment_set SET due_at = :due WHERE id = :id")
        .param("due", odt(vn(5, 20, 0, 0)))
        .param("id", set)
        .update();
    reminders.runAt(vn(3, 20, 0, 0));
    assertThat(received(w, set, "due_soon")).isEmpty();
    reminders.runAt(vn(4, 20, 0, 0));
    assertThat(received(w, set, "due_soon")).isEqualTo(once(UNFINISHED_4));
    assertThat(bodies(set, "due_soon"))
        .containsExactly("Bộ bài tuần 15 sắp hết hạn: 20:00, 05/12/2026");
  }

  @Test
  @DisplayName(
      "TC-S3E6-34 Bộ giao khi còn dưới 24 giờ đến hạn: chỉ có \"Có bài mới\", không có MSG-S3-95")
  void assignedLate() throws Exception {
    World w = world11A1();
    UUID set = setWithDue(w, "Bộ bài tuần 15", vn(4, 20, 0, 0));
    // Giờ giao 03/12/2026 21:00 (còn 23 giờ).
    jdbc.sql("UPDATE assignment_set SET assigned_at = :a WHERE id = :id")
        .param("a", odt(vn(3, 21, 0, 0)))
        .param("id", set)
        .update();
    for (Instant t : List.of(vn(3, 21, 0, 0), vn(4, 6, 0, 0), vn(4, 12, 0, 0), vn(4, 19, 59, 0))) {
      reminders.runAt(t);
    }
    assertThat(received(w, set, "due_soon")).isEmpty();
    assertThat(
            jdbc.sql("SELECT DISTINCT kind FROM notification WHERE set_id = :s")
                .param("s", set)
                .query(String.class)
                .list())
        .containsExactly("new_set");
  }

  @Test
  @DisplayName("TC-S3E6-35 Bộ không có hạn: chạy mỗi giờ trong 48 giờ giả lập, không có MSG-S3-95")
  void noDue() throws Exception {
    World w = world11A1();
    UUID set = setWithDue(w, "Bộ bài tuần 16", null);
    for (int h = 0; h < 48; h++) {
      reminders.runAt(vn(3, 0, 0, 0).plusSeconds(h * 3600L));
    }
    assertThat(received(w, set, "due_soon")).isEmpty();
  }

  @Test
  @DisplayName(
      "TC-S3E6-36 Em Chờ duyệt, Đã rời, Bị từ chối không nhận thông báo nào của bộ tuần 15")
  void notActiveGetNothing() throws Exception {
    World w = world11A1();
    UUID set = setWithDue(w, "Bộ bài tuần 15", vn(4, 20, 0, 0));
    jdbc.sql("UPDATE assignment_set SET release_at = :r WHERE id = :id")
        .param("r", odt(vn(3, 20, 0, 0)))
        .param("id", set)
        .update();
    reminders.runAt(vn(3, 20, 0, 0));
    assertThat(received(w, set, "due_soon")).isEqualTo(once(ACTIVE_8));
    long others =
        count(
            "SELECT count(*) FROM notification WHERE set_id = ? AND user_id IN (?, ?, ?, ?, ?)",
            set,
            w.people().get("Hoàng Nam").id(),
            w.people().get("Thu Hà").id(),
            w.people().get("Phương Vy").id(),
            w.people().get("Văn Tú").id(),
            w.people().get("Lê Bình").id());
    assertThat(others).isZero();
  }

  @Test
  @DisplayName("TC-S3E6-37 Bộ Đã đóng lúc 03/12 10:00 thì 20:00 không nhắc")
  void closedNoReminder() throws Exception {
    World w = world11A1();
    UUID set = setWithDue(w, "Bộ bài tuần 15", vn(4, 20, 0, 0));
    var r = send("POST", "/api/v1/teacher/assignment-sets/" + set + "/close", lan(w), "{}");
    assertThat(r.statusCode()).as(r.body()).isIn(200, 204);
    reminders.runAt(vn(3, 20, 0, 0));
    assertThat(received(w, set, "due_soon")).isEmpty();
  }

  @Test
  @DisplayName(
      "TC-S3E6-39 Chỉ nhắc trong ứng dụng: chạy nhắc SC1, SC3, SC6 không có kênh gửi ngoài nào; mọi nhắc nằm trong bảng notification")
  void inAppOnly() throws Exception {
    World w = world11A1();
    UUID sc1 = setWithDue(w, "Bộ bài tuần 15", vn(4, 20, 0, 0));
    UUID sc3 = setWithDue(w, "Bộ C", vn(11, 6, 0, 0), 3, 4);
    UUID sc6 = setWithDue(w, "Bộ bài tuần 17", null, 5, 6);
    jdbc.sql("UPDATE assignment_set SET release_at = :r WHERE id = :id")
        .param("r", odt(vn(6, 8, 0, 0)))
        .param("id", sc6)
        .update();
    reminders.runAt(vn(3, 20, 0, 0));
    reminders.runAt(vn(6, 8, 0, 0));
    reminders.runAt(vn(10, 6, 0, 0));
    assertThat(received(w, sc1, "due_soon")).hasSize(8);
    assertThat(received(w, sc3, "due_soon")).hasSize(8);
    assertThat(received(w, sc6, "solution_open")).hasSize(8);
    for (String name : context.getBeanDefinitionNames()) {
      Class<?> type = context.getType(name);
      String cls = (name + " " + (type == null ? "" : type.getName())).toLowerCase(Locale.ROOT);
      assertThat(cls)
          .as("bean gửi tin ngoài: " + name)
          .doesNotContainPattern(
              "mailsender|javamail|smssender|twilio|webpush|pushnotification|firebase|apnsclient");
    }
  }

  @Test
  @DisplayName(
      "TC-S3E6-40 Giờ gửi nhắc đúng 24 giờ trước hạn: tác vụ mỗi phút 19:58…20:02, thông báo tạo trong [20:00:00, 20:01:00)")
  void exactMinute() throws Exception {
    World w = world11A1();
    UUID set = setWithDue(w, "Bộ bài tuần 15", vn(4, 20, 0, 0));
    for (int m = 58; m <= 62; m++) {
      Instant t = vn(3, 19, 0, 0).plusSeconds(m * 60L);
      reminders.runAt(t);
      if (t.isBefore(vn(3, 20, 0, 0))) {
        assertThat(received(w, set, "due_soon")).as("trước 20:00 không có nhắc").isEmpty();
      }
    }
    List<OffsetDateTime> created =
        jdbc.sql("SELECT created_at FROM notification WHERE set_id = :s AND kind = 'due_soon'")
            .param("s", set)
            .query(OffsetDateTime.class)
            .list();
    assertThat(created).hasSize(8);
    for (OffsetDateTime c : created) {
      assertThat(c.toInstant()).isBetween(vn(3, 20, 0, 0), vn(3, 20, 0, 59));
    }
  }

  // ------------------------------------------------------------------ PO-68 (BR-S3E6-21)

  private void releaseAt(UUID set, Instant at) {
    jdbc.sql("UPDATE assignment_set SET release_at = :r WHERE id = :id")
        .param("r", odt(at))
        .param("id", set)
        .update();
  }

  private static Map<String, Long> times(List<String> names, long n) {
    return names.stream().collect(Collectors.toMap(x -> x, x -> n));
  }

  @Test
  @DisplayName(
      "PO-68 (a) giờ yên lặng chỉ áp cho nhắc hạn: lời giải mở theo lịch lúc 23:00 được báo ngay lúc 23:00")
  void quietHoursDoNotDeferSolutionOpen() throws Exception {
    World w = world11A1();
    UUID set = setWithDue(w, "Bộ bài tuần 15", null);
    releaseAt(set, vn(6, 23, 0, 0));
    reminders.runAt(vn(6, 23, 0, 0));
    assertThat(received(w, set, "solution_open")).isEqualTo(once(ACTIVE_8));
  }

  @Test
  @DisplayName(
      "PO-68 (b) lời giải mở theo lịch chỉ xét bộ có giờ mở trong 1 ngày gần nhất: quá 1 ngày thì không báo")
  void solutionWindowIsOneDay() throws Exception {
    World w = world11A1();
    UUID set = setWithDue(w, "Bộ bài tuần 15", null);
    releaseAt(set, vn(6, 8, 0, 0));
    reminders.runAt(vn(7, 8, 0, 1));
    assertThat(received(w, set, "solution_open")).isEmpty();
    reminders.runAt(vn(7, 7, 59, 0)); // còn trong 1 ngày
    assertThat(received(w, set, "solution_open")).isEqualTo(once(ACTIVE_8));
  }

  @Test
  @DisplayName(
      "PO-68 (c) em đã nhận \"Mở ngay\" không nhận thông báo mở lời giải lần hai khi tác vụ lịch chạy")
  void openNowNotNotifiedTwice() throws Exception {
    World w = world11A1();
    UUID set = setWithDue(w, "Bộ bài tuần 15", null);
    releaseAt(set, Instant.now().plus(Duration.ofHours(30)));
    var open = send("POST", "/api/v1/teacher/assignment-sets/" + set + "/open-now", lan(w), "{}");
    assertThat(open.statusCode()).as(open.body()).isEqualTo(200);
    assertThat(received(w, set, "solution_open")).isEqualTo(once(ACTIVE_8));
    reminders.runAt(Instant.now().plusSeconds(60));
    reminders.runAt(Instant.now().plusSeconds(120));
    assertThat(received(w, set, "solution_open")).isEqualTo(once(ACTIVE_8));
  }

  @Test
  @DisplayName(
      "PO-68 (d) lùi hạn sau khi đã nhắc: nhắc thêm đúng một lần theo hạn mới; mỗi (em, bộ, hạn) một thông báo")
  void postponedAfterReminderRemindsOnceMore() throws Exception {
    World w = world11A1();
    UUID set = setWithDue(w, "Bộ bài tuần 15", vn(4, 20, 0, 0));
    reminders.runAt(vn(3, 20, 0, 0));
    assertThat(received(w, set, "due_soon")).isEqualTo(once(ACTIVE_8));
    jdbc.sql("UPDATE assignment_set SET due_at = :due WHERE id = :id")
        .param("due", odt(vn(5, 20, 0, 0)))
        .param("id", set)
        .update();
    reminders.runAt(vn(3, 20, 30, 0));
    assertThat(received(w, set, "due_soon"))
        .as("chưa tới giờ nhắc của hạn mới")
        .isEqualTo(once(ACTIVE_8));
    reminders.runAt(vn(4, 20, 0, 0));
    reminders.runAt(vn(4, 20, 1, 0));
    assertThat(received(w, set, "due_soon")).isEqualTo(times(ACTIVE_8, 2));
    assertThat(bodies(set, "due_soon"))
        .containsExactlyInAnyOrder(
            "Bộ bài tuần 15 sắp hết hạn: 20:00, 04/12/2026",
            "Bộ bài tuần 15 sắp hết hạn: 20:00, 05/12/2026");
  }
}
