package vn.geo.server.it.s3e5;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.geo.server.iam.internal.service.ClassTestWorld.World;
import vn.geo.server.it.s3e4.AbstractS3E4IT;

/**
 * US-S3E5-01-FC1 (BR-S3E5-04): việc ghi AuditEvent được giả lập báo lỗi (trigger tạm ở CSDL). Mở
 * ngay và Đổi giờ (module assignment) nằm cùng giao dịch với AuditEvent nên quay lui trọn vẹn. Đặt
 * lại mật khẩu (module iam) phát sự kiện TRONG giao dịch đổi mật khẩu (FIX Dev-S3E5-08) nên cũng
 * quay lui: mật khẩu không đổi, không có lượt đặt lại, phiên của em vẫn dùng được.
 */
class AuditFailureS3E5IT extends AbstractS3E4IT {

  /**
   * Giả lập "ghi AuditEvent báo lỗi" ngay ở CSDL (không đổi ngữ cảnh Spring: ngữ cảnh mới sẽ xóa
   * sạch geo_test): trigger tạm chặn mọi hàng AuditEvent của đúng lớp thử, gỡ ở finally.
   */
  private void failAuditWritesOf(UUID classId) {
    jdbc.sql(
            "CREATE TRIGGER it_s3e5_audit_down BEFORE INSERT ON audit_log FOR EACH ROW WHEN"
                + " (NEW.class_id = '"
                + classId
                + "') EXECUTE FUNCTION tg_append_only()")
        .update();
  }

  private String passwordHashOf(UUID userId) {
    return jdbc.sql("SELECT password_hash FROM app_user WHERE id = ?")
        .param(userId)
        .query(String.class)
        .optional()
        .orElse(null);
  }

  @Test
  @DisplayName(
      "TC-S3E5-07 Ghi nhật ký lỗi thì thao tác không xảy ra: Mở ngay / Đổi giờ / đặt lại mật khẩu quay lui trọn giao dịch (Dev-S3E5-08)")
  void auditFailureRollsBackOperation() throws Exception {
    World w = world();
    String lan = w.people().get("Cô Lan").token();
    String an = w.people().get("Bình An").token();
    UUID c11a1 = w.classes().get("11A1").id();
    String set = assigned(lan, c11a1, "Bộ bài tuần 15", 172_800L, 1, 2);
    var before = json(send("GET", "/api/v1/teacher/assignment-sets/" + set + "/follow", lan, null));
    assertThat(before.get("releaseState").asString()).isEqualTo("upcoming");

    failAuditWritesOf(c11a1);
    try {
      var open = send("POST", "/api/v1/teacher/assignment-sets/" + set + "/open-now", lan, "{}");
      assertThat(open.statusCode()).as(open.body()).isEqualTo(500);
      var change =
          send(
              "POST",
              "/api/v1/teacher/assignment-sets/" + set + "/release-time",
              lan,
              "{\"releaseAt\":\"" + Instant.now().plusSeconds(90_000) + "\"}");
      assertThat(change.statusCode()).as(change.body()).isEqualTo(500);

      var after =
          json(send("GET", "/api/v1/teacher/assignment-sets/" + set + "/follow", lan, null));
      assertThat(after.get("releaseState").asString())
          .as("lời giải vẫn khóa")
          .isEqualTo("upcoming");
      assertThat(after.get("releaseAt").asString())
          .as("giờ mở giữ nguyên")
          .isEqualTo(before.get("releaseAt").asString());
      assertThat(after.get("version").asInt()).isEqualTo(before.get("version").asInt());
      assertThat(count("SELECT count(*) FROM audit_log WHERE class_id = ?", c11a1))
          .as("không có AuditEvent")
          .isZero();
      var list = json(send("GET", "/api/v1/notifications", an, null));
      assertThat(list.get("items").size()).as("chỉ có thông báo bài mới").isEqualTo(1);
      assertThat(list.get("items").get(0).get("body").asString())
          .isEqualTo("Có bài mới: Bộ bài tuần 15");

      UUID mai = w.people().get("Chi Mai").id();
      String hashBefore = passwordHashOf(mai);
      var reset =
          send(
              "POST",
              "/api/v1/teacher/my-classes/%s/students/%s/password-reset"
                  .formatted(c11a1, w.people().get("Chi Mai").id()),
              lan,
              null);
      assertThat(reset.statusCode()).as("đặt lại mật khẩu khi ghi nhật ký lỗi").isEqualTo(500);
      assertThat(reset.body()).doesNotContain("temporaryPassword").doesNotContain("tempPassword");
      assertThat(count("SELECT count(*) FROM audit_log WHERE class_id = ?", c11a1))
          .as("không có AuditEvent PASSWORD_RESET")
          .isZero();
      assertThat(passwordHashOf(mai)).as("mật khẩu không đổi (Dev-S3E5-08)").isEqualTo(hashBefore);
      assertThat(count("SELECT count(*) FROM student_password_reset WHERE student_id = ?", mai))
          .as("không có lượt đặt lại")
          .isZero();
      var maiSession =
          send("GET", "/api/v1/notifications", w.people().get("Chi Mai").token(), null);
      assertThat(maiSession.statusCode()).as("phiên của Chi Mai không bị hủy").isEqualTo(200);
    } finally {
      jdbc.sql("DROP TRIGGER IF EXISTS it_s3e5_audit_down ON audit_log").update();
    }
  }
}
