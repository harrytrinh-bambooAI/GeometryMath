package vn.geo.server.it.s3e6;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;
import vn.geo.server.common.security.Role;
import vn.geo.server.iam.internal.service.ClassTestWorld;
import vn.geo.server.iam.internal.service.ClassTestWorld.ClassSpec;
import vn.geo.server.iam.internal.service.ClassTestWorld.Person;
import vn.geo.server.iam.internal.service.ClassTestWorld.Spec;
import vn.geo.server.iam.internal.service.ClassTestWorld.World;
import vn.geo.server.it.e1.AbstractE1IT;

/**
 * US-S3E6-01 (chữa bài từ Kết quả lớp sang Trình chiếu) và US-S3E6-02 (xuất kết quả lớp, lưu trữ
 * lớp). Dữ liệu: F-S3 (cô Lan, 11A1, bộ tuần 15, bảng F-S3-051) chỉ đọc; ca lưu trữ dựng thế giới
 * riêng.
 */
class ClassResultsS3E6IT extends AbstractE1IT {

  static final UUID COMMUNITY = UUID.fromString("00000000-0000-7000-8002-000000000100");
  static final UUID LAN = s3(1);
  static final UUID KHOI = s3(2);
  static final UUID BINH_AN = s3(20);
  static final UUID C11A1 = s3(10);
  static final UUID SET15 = s3(50);
  static final String BASE = "/api/v1/teacher/class-results/" + C11A1 + "/sets/" + SET15;

  @Autowired ClassTestWorld worlds;
  private final HttpClient http = HttpClient.newHttpClient();

  static UUID s3(long n) {
    return UUID.fromString(String.format("00000000-0000-7000-8003-%012d", n));
  }

  String lan() {
    return token(COMMUNITY, LAN, Role.TEACHER);
  }

  HttpResponse<byte[]> raw(String path, String bearer) throws Exception {
    return http.send(
        HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
            .header("Authorization", "Bearer " + bearer)
            .GET()
            .build(),
        HttpResponse.BodyHandlers.ofByteArray());
  }

  long events() {
    return count(
        "SELECT count(*) FROM present_from_results_event WHERE teacher_id = '" + LAN + "'");
  }

  @Test
  @DisplayName("TC-S3E6-04 Mỗi lần chữa bài ghi một sự kiện đo")
  void eachPresentRecordsOneEvent() throws Exception {
    jdbc.sql("DELETE FROM present_from_results_event").update();
    HttpResponse<String> first = post(BASE + "/items/3/present", lan(), "{\"step\":2}");
    assertThat(first.statusCode()).as(first.body()).isEqualTo(200);
    JsonNode p = json(first);
    assertThat(p.get("startStep").asInt()).isEqualTo(2);
    assertThat(p.get("steps").size()).isGreaterThanOrEqualTo(2);
    // bấm lại ngay (trong 5 giây): cùng phiên, không thêm sự kiện
    HttpResponse<String> again = post(BASE + "/items/3/present", lan(), "{\"step\":2}");
    assertThat(json(again).get("sessionId").asString()).isEqualTo(p.get("sessionId").asString());
    assertThat(events()).isEqualTo(1);
    // 10 giây sau bấm lại: sự kiện thứ hai
    jdbc.sql(
            "UPDATE present_from_results_event SET created_at = created_at - interval '10 seconds'")
        .update();
    assertThat(post(BASE + "/items/3/present", lan(), "{\"step\":2}").statusCode()).isEqualTo(200);
    assertThat(events()).isEqualTo(2);
    // mỗi sự kiện chỉ có mã giáo viên, mã đề, giờ máy chủ
    assertThat(
            jdbc.sql(
                    "SELECT column_name FROM information_schema.columns WHERE table_name = 'present_from_results_event' ORDER BY ordinal_position")
                .query(String.class)
                .list())
        .containsExactly("id", "org_id", "teacher_id", "problem_version_id", "created_at");
    // phản hồi chiếu không có tên hay mã học sinh
    String body = again.body();
    assertThat(body).doesNotContain("Bình An", "HS-", "Khánh Linh");
    // người khác / học sinh: không phiên, không sự kiện
    assertThat(
            post(BASE + "/items/3/present", token(COMMUNITY, KHOI, Role.TEACHER), "{}")
                .statusCode())
        .isEqualTo(404);
    assertThat(
            post(BASE + "/items/3/present", token(COMMUNITY, BINH_AN, Role.STUDENT), "{}")
                .statusCode())
        .isEqualTo(403);
    // bước của thẻ không còn: mở từ bước 1 kèm MSG-S3-92
    JsonNode gone = json(post(BASE + "/items/3/present", lan(), "{\"step\":99}"));
    assertThat(gone.get("startStep").asInt()).isEqualTo(1);
    assertThat(gone.get("notice").asString())
        .isEqualTo("Đề đã được sửa. Mở Trình chiếu từ đầu đề.");
    assertThat(events()).isEqualTo(2);
  }

  @Test
  @DisplayName(
      "US-S3E6-02 xuất CSV: đúng hàng/cột F-S3-051, tên tệp, giới hạn 10 lần/giờ, chỉ chủ lớp")
  void exportCsv() throws Exception {
    jdbc.sql("DELETE FROM class_export_log").update();
    HttpResponse<byte[]> r = raw(BASE + "/export", lan());
    assertThat(r.statusCode()).isEqualTo(200);
    assertThat(r.headers().firstValue("Content-Type").orElse("")).startsWith("text/csv");
    assertThat(r.headers().firstValue("Content-Disposition").orElse(""))
        .contains("ket-qua-11A1-bo-bai-tuan-15-");
    byte[] b = r.body();
    assertThat(b[0] & 0xff).isEqualTo(0xEF); // BOM UTF-8
    List<String> lines = List.of(new String(b, StandardCharsets.UTF_8).substring(1).split("\r\n"));
    assertThat(lines.get(0)).isEqualTo("Tên hiển thị,Mã người dùng,Đã rời,Đề 1,Đề 2,Đề 3,Đề 4");
    assertThat(lines)
        .anyMatch(
            l -> l.startsWith("Khánh Linh,HS-7T7B-98QZ,,Chưa làm,Chưa làm,Chưa làm,Chưa làm"));
    assertThat(lines).anyMatch(l -> l.startsWith("Hà My,") && l.endsWith("Đúng,Đúng,Đúng,Đúng"));
    String all = String.join("\n", lines);
    assertThat(all).doesNotContain("HK-", "Cô Lan", "%", "Tổng");
    // chỉ chủ lớp; học sinh FORBIDDEN
    assertThat(raw(BASE + "/export", token(COMMUNITY, KHOI, Role.TEACHER)).statusCode())
        .isEqualTo(404);
    assertThat(raw(BASE + "/export", token(COMMUNITY, BINH_AN, Role.STUDENT)).statusCode())
        .isEqualTo(403);
    // 10 lần / giờ
    for (int i = 0; i < 9; i++) {
      assertThat(raw(BASE + "/export", lan()).statusCode()).isEqualTo(200);
    }
    HttpResponse<byte[]> limited = raw(BASE + "/export", lan());
    assertThat(limited.statusCode()).isEqualTo(429);
    assertThat(limited.headers().firstValue("Retry-After")).isPresent();
    assertThat(new String(limited.body(), StandardCharsets.UTF_8))
        .contains("Đã xuất quá nhiều lần. Vui lòng thử lại sau ít phút.");
    jdbc.sql("UPDATE class_export_log SET created_at = created_at - interval '61 minutes'")
        .update();
    assertThat(raw(BASE + "/export", lan()).statusCode()).isEqualTo(200);
  }

  @Test
  @DisplayName(
      "US-S3E6-02 lưu trữ lớp: Chờ duyệt bị từ chối, mã ngừng dùng, không đặt lại mật khẩu; bỏ lưu trữ có mã mới")
  void archiveAndUnarchive() throws Exception {
    World w =
        worlds.build(
            new Spec(
                List.of("Cô Lan"),
                List.of("Gia Bảo"),
                List.of(
                    new ClassSpec(
                        "11A2",
                        "Cô Lan",
                        "11A2",
                        11,
                        List.of("Quốc Bảo", "Thảo Vy"),
                        List.of("Hoàng Nam"),
                        List.of(),
                        List.of()))));
    Person lan = w.people().get("Cô Lan");
    Person vy = w.people().get("Thảo Vy");
    Person bao = w.people().get("Gia Bảo");
    UUID c = w.classes().get("11A2").id();
    String oldCode = w.classes().get("11A2").code();
    String base = "/api/v1/teacher/my-classes/" + c;
    assertThat(post(base + "/archive", lan.token(), "").statusCode()).isEqualTo(200);
    assertThat(json(get(base, lan.token())).get("status").asString()).isEqualTo("archived");
    assertThat(json(get(base + "/pending", lan.token())).get("items").size()).isZero();
    assertThat(
            count(
                "SELECT count(*) FROM class_member WHERE class_id = '"
                    + c
                    + "' AND status = 'rejected'"))
        .isEqualTo(1);
    // mã cũ như mã sai; không đặt lại mật khẩu
    assertThat(
            post("/api/v1/student/class-joins", bao.token(), "{\"code\":\"" + oldCode + "\"}")
                .statusCode())
        .isEqualTo(400);
    assertThat(
            post(base + "/students/" + vy.id() + "/password-reset", lan.token(), "").statusCode())
        .isEqualTo(404);
    // học sinh thấy lớp đã lưu trữ
    JsonNode mine = json(get("/api/v1/student/classes", vy.token()));
    assertThat(mine.get("items").get(0).get("archived").asBoolean()).isTrue();
    // Kết quả lớp vẫn chọn được lớp đã lưu trữ
    assertThat(get("/api/v1/teacher/class-results/classes", lan.token()).body())
        .contains(c.toString());
    // bỏ lưu trữ: Đang dạy, mã mới
    JsonNode back = json(post(base + "/unarchive", lan.token(), ""));
    assertThat(back.get("status").asString()).isEqualTo("active");
    assertThat(back.get("code").asString()).isNotEqualTo(oldCode).startsWith("HK-");
  }
}
