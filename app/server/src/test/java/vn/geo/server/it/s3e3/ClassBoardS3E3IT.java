package vn.geo.server.it.s3e3;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;
import vn.geo.server.aigateway.internal.MockAiControl;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.it.e1.AbstractE1IT;
import vn.geo.server.testing.SqlStatementCounter;
import vn.geo.server.tracking.internal.classboard.service.S3E3SandboxService;
import vn.geo.server.tracking.internal.classboard.service.S3E3SandboxService.ClassSpec;
import vn.geo.server.tracking.internal.classboard.service.S3E3SandboxService.EventSpec;
import vn.geo.server.tracking.internal.classboard.service.S3E3SandboxService.SetSpec;
import vn.geo.server.tracking.internal.classboard.service.S3E3SandboxService.StudentSpec;
import vn.geo.server.tracking.internal.classboard.service.StuckSettings;

/**
 * S3-E3 — tích hợp: tải và số câu truy vấn của bảng lớp × bài (US-S3E3-01-SC10), dữ liệu gửi bộ gộp
 * ẩn danh (US-S3E3-03-SC12, FC8), ngưỡng MIN_AGGREGATE là cấu hình (FC7), đầu vào không đổi thì
 * không gọi lại bộ gộp (FC9).
 */
class ClassBoardS3E3IT extends AbstractE1IT {

  static final List<String> NAMES =
      List.of(
          "Bình An",
          "Chi Mai",
          "Đức Huy",
          "Gia Bảo",
          "Hà My",
          "Khánh Linh",
          "Minh Quân",
          "Ngọc Anh");

  @Autowired S3E3SandboxService sandbox;
  @Autowired SqlStatementCounter counter;
  @Autowired MockAiControl mock;
  @Autowired StuckSettings settings;

  static List<StudentSpec> students(List<String> names) {
    return names.stream().map(n -> new StudentSpec(n, "active", false)).toList();
  }

  /** Bảng F-S3-051 (11A1 × bộ tuần 15) kèm các tương tác thêm của ca kiểm. */
  S3E3SandboxService.SandboxOut board051(List<EventSpec> events) {
    Map<String, List<String>> cells = new LinkedHashMap<>();
    cells.put("Bình An", List.of("Đúng", "Xem lại", "Đúng", "Xem lại"));
    cells.put("Chi Mai", List.of("Đúng", "Xem lại", "Xem lại", "Chưa làm"));
    cells.put("Đức Huy", List.of("Đúng", "Đúng", "Xem lại", "Đúng"));
    cells.put("Gia Bảo", List.of("Xem lại", "Xem lại", "Đang làm", "Chưa làm"));
    cells.put("Hà My", List.of("Đúng", "Đúng", "Đúng", "Đúng"));
    cells.put("Khánh Linh", List.of("Chưa làm", "Chưa làm", "Chưa làm", "Chưa làm"));
    cells.put("Minh Quân", List.of("Đúng", "Xem lại", "Xem lại", "Xem lại"));
    cells.put("Ngọc Anh", List.of("Đúng", "Đúng", "Xem lại", "Đang làm"));
    return sandbox.create(
        List.of(
            new ClassSpec(
                "11A1",
                students(NAMES),
                List.of(new SetSpec("Bộ bài tuần 15", "assigned", 4, cells, events)),
                0)),
        List.of());
  }

  static String base(S3E3SandboxService.SandboxOut sb) {
    var c = sb.classes().getFirst();
    return "/api/v1/teacher/class-results/" + c.id() + "/sets/" + c.sets().getFirst().id();
  }

  List<String> stuckPrompts() {
    return mock.receivedPrompts().stream().filter(p -> p.startsWith("Nhiệm vụ: gộp")).toList();
  }

  JsonNode item(JsonNode stuck, int position) {
    for (JsonNode i : stuck.get("items")) {
      if (i.get("position").asInt() == position) {
        return i;
      }
    }
    throw new AssertionError("không có đề " + position);
  }

  @Test
  @DisplayName(
      "TC-S3E3-10 lớp 100 học sinh × bộ 20 đề: 100 hàng một trang, số câu truy vấn không theo số hàng, p95 ≤ 2 giây")
  void hundredStudentsOnePageConstantQueries() throws Exception {
    var big =
        sandbox.create(
            List.of(
                new ClassSpec(
                    "11B9",
                    List.of(),
                    List.of(new SetSpec("Bộ 20 đề", "assigned", 20, Map.of(), List.of())),
                    100)),
            List.of());
    var small =
        sandbox.create(
            List.of(
                new ClassSpec(
                    "11B8",
                    students(List.of("Bình An", "Chi Mai")),
                    List.of(new SetSpec("Bộ 20 đề", "assigned", 20, Map.of(), List.of())),
                    0)),
            List.of());
    List<Long> millis = new ArrayList<>();
    String lastId = null;
    for (int i = 0; i < 30; i++) {
      lastId = "s3e3-big-" + Uuid7.next();
      long start = System.nanoTime();
      var r = get(base(big) + "/board", big.teacher().token(), lastId);
      millis.add((System.nanoTime() - start) / 1_000_000);
      assertThat(r.statusCode()).as(r.body()).isEqualTo(200);
      JsonNode body = json(r);
      assertThat(body.get("rows").size()).isEqualTo(100);
      assertThat(body.get("columns").size()).isEqualTo(20);
      assertThat(body.hasNonNull("nextCursor")).isFalse();
    }
    String smallId = "s3e3-small-" + Uuid7.next();
    var s = get(base(small) + "/board", small.teacher().token(), smallId);
    assertThat(json(s).get("rows").size()).isEqualTo(2);
    assertThat(counter.count(lastId))
        .as("100 hàng phải chạy cùng số câu với 2 hàng: %s", counter.statements(lastId))
        .isEqualTo(counter.count(smallId));
    millis.sort(Long::compare);
    assertThat(millis.get((int) Math.ceil(millis.size() * 0.95) - 1)).isLessThanOrEqualTo(2000L);
  }

  @Test
  @DisplayName(
      "TC-S3E3-45 dữ liệu gửi bộ gộp là ẩn danh: không tên, không mã HS-, không mã lớp, không tên giáo viên; số điện thoại thành [đã ẩn]")
  void aggregatorInputIsAnonymous() throws Exception {
    var sb =
        board051(
            List.of(
                new EventSpec(
                    "Chi Mai", 3, "ask", null, "Em là Mai, số em 0912345678", null, "15:12")));
    mock.reset();
    var r = get(base(sb) + "/stuck", sb.teacher().token());
    assertThat(r.statusCode()).as(r.body()).isEqualTo(200);
    List<String> sent = stuckPrompts();
    assertThat(sent).hasSize(4); // Đề 1, 2, 3 có 7 bài làm, Đề 4 có 5: mỗi đề một lượt gọi
    String all = String.join("\n", sent);
    assertThat(all).contains("Mã đề: D").contains("[đã ẩn]").doesNotContain("0912345678");
    for (String name : NAMES) {
      assertThat(all).doesNotContain(name);
    }
    assertThat(all).doesNotContain("HS-").doesNotContain("HK-").doesNotContain("Cô Lan");
    assertThat(all).doesNotContain("11A1").doesNotContain(sb.teacher().id().toString());
    for (var p : sb.classes().getFirst().students().values()) {
      assertThat(all).doesNotContain(p.userCode()).doesNotContain(p.id().toString());
    }
  }

  @Test
  @DisplayName(
      "TC-S3E3-53 ngưỡng MIN_AGGREGATE là cấu hình: 3 thì đề có 3 bài làm được gộp, 5 thì chỉ dữ liệu thô và banner ghi 5")
  void thresholdComesFromConfiguration() throws Exception {
    Map<String, List<String>> cells = new LinkedHashMap<>();
    cells.put("Bình An", List.of("Xem lại"));
    cells.put("Chi Mai", List.of("Đúng"));
    cells.put("Đức Huy", List.of("Đang làm"));
    var sb =
        sandbox.create(
            List.of(
                new ClassSpec(
                    "11A1",
                    students(NAMES),
                    List.of(new SetSpec("Bộ một đề", "assigned", 1, cells, List.of())),
                    0)),
            List.of());
    int original = settings.minAggregate();
    try {
      settings.minAggregate(3);
      JsonNode three = json(get(base(sb) + "/stuck", sb.teacher().token()));
      assertThat(three.get("minAggregate").asInt()).isEqualTo(3);
      assertThat(item(three, 1).get("mode").asString()).isEqualTo("insight");
      assertThat(three.get("insights").size()).isPositive();

      settings.minAggregate(5);
      JsonNode five = json(get(base(sb) + "/stuck", sb.teacher().token()));
      assertThat(five.get("minAggregate").asInt()).isEqualTo(5);
      assertThat(item(five, 1).get("mode").asString()).isEqualTo("raw");
      assertThat(item(five, 1).get("works").asInt()).isEqualTo(3);
      assertThat(five.get("insights").size()).isZero();
    } finally {
      settings.minAggregate(original);
    }
  }

  @Test
  @DisplayName(
      "TC-S3E3-54 chuỗi giống email trong câu hỏi bị che thành [đã ẩn] trước khi gửi bộ gộp")
  void emailIsMaskedBeforeAggregation() throws Exception {
    var sb =
        board051(
            List.of(
                new EventSpec(
                    "Đức Huy",
                    3,
                    "ask",
                    null,
                    "gửi lời giải vào mai@example.org nhé",
                    null,
                    "15:12")));
    mock.reset();
    assertThat(get(base(sb) + "/stuck", sb.teacher().token()).statusCode()).isEqualTo(200);
    String all = String.join("\n", stuckPrompts());
    assertThat(all).contains("gửi lời giải vào [đã ẩn] nhé").doesNotContain("mai@example.org");
  }

  @Test
  @DisplayName(
      "TC-S3E3-55 dữ liệu đầu vào không đổi thì không gọi lại bộ gộp; nhận định giữ nguyên, giờ cập nhật theo lần làm mới")
  void unchangedInputDoesNotCallAggregatorAgain() throws Exception {
    var sb = board051(List.of());
    mock.reset();
    JsonNode first = json(get(base(sb) + "/stuck", sb.teacher().token()));
    int afterFirst = stuckPrompts().size();
    assertThat(afterFirst).isEqualTo(4);
    Thread.sleep(5);
    JsonNode second = json(get(base(sb) + "/stuck", sb.teacher().token()));
    JsonNode third = json(get(base(sb) + "/stuck", sb.teacher().token()));
    assertThat(stuckPrompts()).hasSize(afterFirst);
    assertThat(second.get("insights")).isEqualTo(first.get("insights"));
    assertThat(third.get("insights")).isEqualTo(first.get("insights"));
    assertThat(java.time.Instant.parse(third.get("updatedAt").asString()))
        .isAfter(java.time.Instant.parse(first.get("updatedAt").asString()));
  }
}
