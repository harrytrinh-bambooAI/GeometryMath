package vn.geo.server.aigateway.internal;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import vn.geo.server.aigateway.internal.AiProvider.Simulation;

/**
 * Trạng thái điều khiển của nhà cung cấp giả lập (chỉ móc kiểm thử dev/test đổi): chế độ lỗi theo
 * người gọi và "làn" kiểm thử (để các test E2E chạy song song không giẫm nhau), đáp số kịch bản cho
 * bộ đề chuẩn (F-GEO-070), và các prompt nhà cung cấp đã nhận (để kiểm BR-GEO-108).
 */
@Component
public class MockAiControl {

  private volatile Simulation global = Simulation.OK;
  private final Map<String, Simulation> byKey = new ConcurrentHashMap<>();
  private final Map<String, String> answerOverrides = new ConcurrentHashMap<>();
  private final Deque<String> received = new ArrayDeque<>();

  public static String key(String userId, String lane) {
    return (userId == null ? "*" : userId) + "|" + (lane == null ? "" : lane);
  }

  public Simulation simulationFor(String userId, String lane) {
    Simulation s = byKey.get(key(userId, lane));
    return s != null ? s : global;
  }

  public void set(String userId, String lane, Simulation mode) {
    if (userId == null && lane == null) {
      global = mode;
    } else if (mode == Simulation.OK) {
      byKey.remove(key(userId, lane));
    } else {
      byKey.put(key(userId, lane), mode);
    }
  }

  public void overrideAnswer(String problemCode, String answer) {
    if (answer == null) {
      answerOverrides.remove(problemCode);
    } else {
      answerOverrides.put(problemCode, answer);
    }
  }

  public String answerOverride(String problemCode) {
    return answerOverrides.get(problemCode);
  }

  synchronized void received(String prompt) {
    received.addLast(prompt);
    while (received.size() > 200) {
      received.removeFirst();
    }
  }

  /** Prompt nhà cung cấp giả lập đã nhận, cũ trước mới sau. */
  public synchronized List<String> receivedPrompts() {
    return List.copyOf(received);
  }

  public synchronized void reset() {
    global = Simulation.OK;
    byKey.clear();
    answerOverrides.clear();
    received.clear();
  }
}
