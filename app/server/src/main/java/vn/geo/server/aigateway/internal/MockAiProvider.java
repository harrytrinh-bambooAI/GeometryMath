package vn.geo.server.aigateway.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Nhà cung cấp GIẢ LẬP (D-GEO-047): trả nội dung soạn sẵn, không gọi AI thật.
 *
 * <ul>
 *   <li>task "solution" (bộ đề chuẩn): đáp số + tọa độ soạn sẵn theo mã bài ở {@code
 *       mock-ai/benchmark.json}, có thể ghi đè từng đề (F-GEO-070) qua {@link MockAiControl}.
 *   <li>task khác (hỏi đáp…): câu trả lời chung, không chứa đáp số.
 * </ul>
 *
 * Chế độ lỗi/quá thời gian do móc kiểm thử đặt (US-GEO-103-FC1, FC2).
 */
@Component
@ConditionalOnProperty(name = "geo.ai.provider", havingValue = "mock", matchIfMissing = true)
public class MockAiProvider implements AiProvider {

  static final String QA_REPLY =
      "[Trợ lý giả lập] Em hãy đọc lại dữ kiện của đề và thử tìm mối liên hệ giữa các yếu tố đã cho"
          + " nhé.";
  private static final Pattern CODE = Pattern.compile("Mã bài: ([A-Z]{2,5}-[0-9]{2,4})");

  private final AiProperties props;
  private final MockAiControl control;
  private final JsonMapper mapper = JsonMapper.builder().build();
  private final Map<String, JsonNode> canned;

  public MockAiProvider(AiProperties props, MockAiControl control) {
    this.props = props;
    this.control = control;
    this.canned = loadCanned();
  }

  @Override
  public String name() {
    return "mock";
  }

  @Override
  public String complete(ProviderCall call) throws Exception {
    control.received(call.prompt());
    if (!props.mockLatency().isZero()) {
      Thread.sleep(props.mockLatency());
    }
    switch (call.simulation()) {
      case ERROR -> throw new IllegalStateException("nhà cung cấp giả lập: chế độ lỗi");
      case TIMEOUT -> {
        Thread.sleep(600_000);
        return "";
      }
      default -> {
        // tiếp tục
      }
    }
    if ("solution".equals(call.task())) {
      Matcher m = CODE.matcher(call.prompt());
      String code = m.find() ? m.group(1) : "";
      JsonNode base = canned.get(code);
      ObjectNode out = mapper.createObjectNode();
      String override = control.answerOverride(code);
      out.put(
          "answer",
          override != null ? override : base == null ? "" : base.get("answer").asString());
      if (base != null && base.has("points")) {
        out.set("points", base.get("points"));
      }
      return mapper.writeValueAsString(out);
    }
    return QA_REPLY;
  }

  private Map<String, JsonNode> loadCanned() {
    try (InputStream in = MockAiProvider.class.getResourceAsStream("/mock-ai/benchmark.json")) {
      if (in == null) {
        return Map.of();
      }
      JsonNode root = mapper.readTree(in);
      java.util.HashMap<String, JsonNode> m = new java.util.HashMap<>();
      for (var e : root.properties()) {
        m.put(e.getKey(), e.getValue());
      }
      return Map.copyOf(m);
    } catch (IOException e) {
      throw new IllegalStateException("không đọc được mock-ai/benchmark.json", e);
    }
  }
}
