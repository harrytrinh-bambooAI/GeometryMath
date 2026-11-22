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
 *   <li>task "scene" (E2, tự học): mô tả dựng theo mẫu chữ của đề ở {@code mock-ai/scene.json}, ghi
 *       đè theo kịch bản của móc kiểm thử (F-GEO-111, F-GEO-112).
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
  private final JsonNode sceneRules;

  public MockAiProvider(AiProperties props, MockAiControl control) {
    this.props = props;
    this.control = control;
    this.canned = loadCanned();
    this.sceneRules = loadScene();
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
    if ("scene".equals(call.task())) {
      return mapper.writeValueAsString(scene(call.prompt(), call.scenario()));
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

  private static final Pattern STATEMENT = Pattern.compile("Đề bài: (.*)", Pattern.DOTALL);

  /** Kết quả "scene" giả lập: quy tắc đầu tiên khớp đề, thay $n, rồi ghi đè theo kịch bản. */
  ObjectNode scene(String prompt, String scenario) {
    Matcher sm = STATEMENT.matcher(prompt);
    String statement = sm.find() ? sm.group(1).strip() : prompt;
    ObjectNode out = null;
    for (JsonNode rule : sceneRules.path("rules")) {
      Matcher m = Pattern.compile(rule.path("pattern").asString()).matcher(statement);
      if (m.find()) {
        out = (ObjectNode) substitute(rule.get("response").deepCopy(), m);
        break;
      }
    }
    if (out == null) {
      out = (ObjectNode) sceneRules.path("fallback").deepCopy();
    }
    JsonNode over = scenario == null ? null : sceneRules.path("scenarios").get(scenario);
    if (over != null) {
      for (var e : over.properties()) {
        out.set(e.getKey(), e.getValue().deepCopy());
      }
    }
    return out;
  }

  private JsonNode substitute(JsonNode n, Matcher m) {
    if (n.isString()) {
      String s = n.asString();
      for (int g = m.groupCount(); g >= 1; g--) {
        String v = m.group(g) == null ? "" : m.group(g).replace(',', '.');
        s = s.replace("$" + g, v);
      }
      return mapper.getNodeFactory().stringNode(s);
    }
    if (n.isObject()) {
      ObjectNode o = (ObjectNode) n;
      for (var e : java.util.List.copyOf(o.properties())) {
        o.set(e.getKey(), substitute(e.getValue(), m));
      }
      return o;
    }
    if (n.isArray()) {
      tools.jackson.databind.node.ArrayNode a = (tools.jackson.databind.node.ArrayNode) n;
      for (int i = 0; i < a.size(); i++) {
        a.set(i, substitute(a.get(i), m));
      }
      return a;
    }
    return n;
  }

  private JsonNode loadScene() {
    try (InputStream in = MockAiProvider.class.getResourceAsStream("/mock-ai/scene.json")) {
      return in == null ? mapper.createObjectNode() : mapper.readTree(in);
    } catch (IOException e) {
      throw new IllegalStateException("không đọc được mock-ai/scene.json", e);
    }
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
