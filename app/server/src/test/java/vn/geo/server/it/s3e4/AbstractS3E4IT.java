package vn.geo.server.it.s3e4;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import vn.geo.server.iam.internal.service.ClassTestWorld;
import vn.geo.server.iam.internal.service.ClassTestWorld.ClassSpec;
import vn.geo.server.iam.internal.service.ClassTestWorld.Spec;
import vn.geo.server.iam.internal.service.ClassTestWorld.World;
import vn.geo.server.it.e1.AbstractE1IT;

/**
 * Nền IT của S3-E4 / US-S3E5-01: mỗi ca dựng "thế giới" riêng (móc S3-E1: giáo viên, lớp, học sinh
 * có mã người dùng) trong trường Cộng đồng, soạn và giao bộ bằng API thật với đề kho 's3_tuan'
 * (seed 910).
 */
public abstract class AbstractS3E4IT extends AbstractE1IT {

  /** Phiên bản đề TUAN (seed 910): 910101…910106. */
  protected static String version(int n) {
    return "00000000-0000-7000-8003-0000009101%02d".formatted(n);
  }

  @Autowired protected ClassTestWorld worlds;

  private final HttpClient http = HttpClient.newHttpClient();

  protected World world() {
    return worlds.build(
        new Spec(
            List.of("Cô Lan", "Thầy Khôi"),
            List.of(),
            List.of(
                new ClassSpec(
                    "11A1",
                    "Cô Lan",
                    "11A1",
                    11,
                    List.of("Bình An", "Chi Mai", "Đức Huy", "Gia Bảo"),
                    List.of("Thu Hà"),
                    List.of("Văn Tú"),
                    List.of()),
                new ClassSpec(
                    "11A2",
                    "Cô Lan",
                    "11A2",
                    11,
                    List.of("Quốc Bảo"),
                    List.of(),
                    List.of(),
                    List.of()),
                new ClassSpec(
                    "10B",
                    "Thầy Khôi",
                    "10B",
                    10,
                    List.of("Mi Na"),
                    List.of(),
                    List.of(),
                    List.of()))));
  }

  protected HttpResponse<String> send(String method, String path, String token, String body)
      throws Exception {
    HttpRequest.Builder b =
        HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
            .header("Content-Type", "application/json")
            .method(
                method,
                body == null
                    ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofString(body));
    if (token != null) {
      b.header("Authorization", "Bearer " + token);
    }
    return http.send(b.build(), HttpResponse.BodyHandlers.ofString());
  }

  /** Soạn và giao một bộ cho lớp; giờ mở sau releaseInSeconds giây (null = chưa đặt giờ). */
  protected String assigned(
      String teacher, UUID classId, String title, Long releaseInSeconds, int... problems)
      throws Exception {
    StringBuilder items = new StringBuilder();
    for (int n : problems) {
      items
          .append(items.isEmpty() ? "" : ",")
          .append("{\"problemVersionId\":\"")
          .append(version(n))
          .append("\"}");
    }
    String release =
        releaseInSeconds == null
            ? "null"
            : "\"" + Instant.now().plusSeconds(releaseInSeconds) + "\"";
    var created =
        send(
            "POST",
            "/api/v1/teacher/assignment-sets",
            teacher,
            "{\"title\":\"%s\",\"classId\":\"%s\",\"dueAt\":null,\"releaseAt\":%s,\"items\":[%s]}"
                .formatted(title, classId, release, items));
    assertThat(created.statusCode()).as(created.body()).isEqualTo(201);
    String setId = json(created).get("id").asString();
    var a =
        send(
            "POST",
            "/api/v1/teacher/assignment-sets/" + setId + "/assign",
            teacher,
            "{\"requestKey\":\"" + UUID.randomUUID() + "\"}");
    assertThat(a.statusCode()).as(a.body()).isEqualTo(200);
    return setId;
  }

  protected long count(String sql, Object... params) {
    var q = jdbc.sql(sql);
    for (int i = 0; i < params.length; i++) {
      q = q.param(i + 1, params[i]);
    }
    return q.query(Long.class).single();
  }
}
