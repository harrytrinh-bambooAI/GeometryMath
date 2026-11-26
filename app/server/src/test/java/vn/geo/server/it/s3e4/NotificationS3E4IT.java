package vn.geo.server.it.s3e4;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataIntegrityViolationException;
import vn.geo.server.assignment.api.AssignmentSetNotice;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.iam.internal.service.ClassTestWorld.World;
import vn.geo.server.notify.internal.service.NotificationService;
import vn.geo.server.notify.internal.service.NotifyHookService;

/**
 * S3-E4 — tích hợp của Notification trong ứng dụng: không có kênh gửi ngoài (US-S3E4-03-FC4), xử lý
 * lặp sự kiện (FC5), dọn thông báo quá 90 ngày (FC8).
 */
class NotificationS3E4IT extends AbstractS3E4IT {

  @Autowired NotificationService notifications;
  @Autowired NotifyHookService hooks;
  @Autowired ApplicationContext context;

  @Test
  @DisplayName(
      "TC-S3E4-36 Chỉ thông báo trong ứng dụng: giao bộ, Mở ngay, Đổi giờ không gọi kênh gửi ngoài nào; bảng thông báo không có email, số điện thoại hay mã đăng ký đẩy")
  void inAppOnly() throws Exception {
    World w = world();
    String lan = w.people().get("Cô Lan").token();
    UUID c11a1 = w.classes().get("11A1").id();
    String tuan15 = assigned(lan, c11a1, "Bộ bài tuần 15", 172_800L, 1, 2);
    String tuan16 = assigned(lan, c11a1, "Bộ bài tuần 16", 172_800L, 3, 4);
    var open = send("POST", "/api/v1/teacher/assignment-sets/" + tuan15 + "/open-now", lan, "{}");
    assertThat(open.statusCode()).as(open.body()).isEqualTo(200);
    var change =
        send(
            "POST",
            "/api/v1/teacher/assignment-sets/" + tuan16 + "/release-time",
            lan,
            "{\"releaseAt\":\"" + Instant.now().plusSeconds(90_000) + "\"}");
    assertThat(change.statusCode()).as(change.body()).isEqualTo(200);

    // Bốn sự kiện (giao × 2, mở ngay, đổi giờ) × 4 em Đang học = 16 thông báo, tất cả nằm trong
    // bảng của ứng dụng.
    assertThat(
            count(
                "SELECT count(*) FROM notification WHERE set_id IN (CAST(? AS uuid), CAST(? AS uuid))",
                tuan15,
                tuan16))
        .isEqualTo(16);
    // Không có nhà cung cấp gửi tin ngoài nào trong ứng dụng để mà gọi: số lượt gọi = 0 theo cấu
    // trúc.
    for (String name : context.getBeanDefinitionNames()) {
      Class<?> type = context.getType(name);
      String cls = (name + " " + (type == null ? "" : type.getName())).toLowerCase(Locale.ROOT);
      assertThat(cls)
          .as("bean gửi tin ngoài: " + name)
          .doesNotContainPattern(
              "mailsender|javamail|smssender|twilio|webpush|pushnotification|firebase|apnsclient");
    }
    // Bảng thông báo chỉ có các cột trong ứng dụng.
    List<String> columns =
        jdbc.sql(
                "SELECT column_name FROM information_schema.columns WHERE table_schema = 'public'"
                    + " AND table_name = 'notification' ORDER BY column_name")
            .query(String.class)
            .list();
    assertThat(columns)
        .containsExactlyInAnyOrder(
            "id",
            "org_id",
            "user_id",
            "kind",
            "event_id",
            "set_id",
            "body",
            "created_at",
            "read_at");
    assertThat(
            count(
                "SELECT count(*) FROM information_schema.columns WHERE table_schema = 'public'"
                    + " AND table_name IN ('notification', 'app_user', 'class_member')"
                    + " AND column_name ~* '(e_?mail|phone|push_?token|device_?token|fcm|apns)'"))
        .as("không cột địa chỉ gửi ngoài của học sinh")
        .isZero();
  }

  @Test
  @DisplayName(
      "TC-S3E4-37 Sự kiện \"bài mới\" xử lý lặp không tạo hai thông báo; ràng buộc duy nhất theo sự kiện và người nhận từ chối bản ghi trùng")
  void replayedEventCreatesNoDuplicate() {
    World w = world();
    UUID an = w.people().get("Bình An").id();
    UUID mai = w.people().get("Chi Mai").id();
    AssignmentSetNotice e =
        new AssignmentSetNotice(
            Uuid7.next(),
            w.orgId(),
            Instant.now(),
            AssignmentSetNotice.Kind.ASSIGNED,
            null,
            w.classes().get("11A1").id(),
            "Bộ bài tuần 16",
            null,
            List.of(an, mai));
    assertThat(notifications.handle(e)).isEqualTo(2);
    assertThat(notifications.handle(e)).as("xử lý lại cùng sự kiện").isZero();
    assertThat(count("SELECT count(*) FROM notification WHERE event_id = ?", e.eventId()))
        .isEqualTo(2);
    assertThat(
            count(
                "SELECT count(*) FROM notification WHERE event_id = ? AND user_id = ?"
                    + " AND body = 'Có bài mới: Bộ bài tuần 16' AND read_at IS NULL",
                e.eventId(),
                an))
        .isEqualTo(1);
    assertThatThrownBy(() ->
            jdbc.sql(
                    "INSERT INTO notification (id, org_id, user_id, kind, event_id, body)"
                        + " VALUES (:id, :org, :user, 'new_set', :event, 'Có bài mới: Bộ bài tuần 16')")
                .param("id", Uuid7.next())
                .param("org", w.orgId())
                .param("user", an)
                .param("event", e.eventId())
                .update())
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  @DisplayName("TC-S3E4-40 Tác vụ dọn dẹp xóa thông báo 91 ngày, giữ thông báo 89 ngày")
  void cleanupAfter90Days() {
    World w = world();
    UUID an = w.people().get("Bình An").id();
    hooks.seed(an, 1, 89);
    hooks.seed(an, 1, 91);
    assertThat(count("SELECT count(*) FROM notification WHERE user_id = ?", an)).isEqualTo(2);
    assertThat(notifications.cleanup()).isGreaterThanOrEqualTo(1);
    assertThat(count("SELECT count(*) FROM notification WHERE user_id = ?", an)).isEqualTo(1);
    assertThat(
            count(
                "SELECT count(*) FROM notification WHERE user_id = ?"
                    + " AND created_at > now() - interval '90 days'",
                an))
        .as("thông báo 89 ngày còn nguyên")
        .isEqualTo(1);
  }
}
