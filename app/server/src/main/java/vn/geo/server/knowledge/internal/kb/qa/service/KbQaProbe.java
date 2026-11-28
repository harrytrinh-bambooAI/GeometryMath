package vn.geo.server.knowledge.internal.kb.qa.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Điểm quan sát / gây lỗi của hỏi đáp theo kho cho móc kiểm thử (chỉ móc profile dev/test đặt giá
 * trị; ở prod luôn rỗng, không ảnh hưởng): sổ yêu cầu đã gửi AI theo lượt (soát "yêu cầu ở giả lập
 * không chứa …", FC3, FC4, US-S5E3-05-FC1, FC2), bộ kiểm ném lỗi (KS-5, US-S5E3-05-FC3), bộ kiểm
 * chậm (biên 200 ms, US-S5E3-05-FC4). Sổ yêu cầu giữ trong bộ nhớ, tối đa 500 lượt gần nhất.
 */
@Component
public class KbQaProbe {

  private final Map<UUID, String> prompts =
      new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<UUID, String> e) {
          return size() > 500;
        }
      };

  private final Map<UUID, Long> delays = new ConcurrentHashMap<>();
  private final Set<UUID> faulty = ConcurrentHashMap.newKeySet();

  synchronized void sent(UUID interactionId, String prompt) {
    prompts.put(interactionId, prompt);
  }

  /** Yêu cầu đã gửi AI của một lượt (null = lượt không gọi AI). */
  public synchronized String prompt(UUID interactionId) {
    return prompts.get(interactionId);
  }

  public void delay(UUID userId, long ms) {
    if (ms <= 0) {
      delays.remove(userId);
    } else {
      delays.put(userId, Math.min(ms, 5_000L));
    }
  }

  public void fault(UUID userId, boolean on) {
    if (on) {
      faulty.add(userId);
    } else {
      faulty.remove(userId);
    }
  }

  /** Gọi trong vùng đo của bộ kiểm (trước khi kiểm câu trả lời). */
  void beforeCheck(UUID userId) {
    if (faulty.contains(userId)) {
      throw new IllegalStateException("bộ kiểm giả lập: lỗi");
    }
    Long d = delays.get(userId);
    if (d != null) {
      try {
        Thread.sleep(d);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }
}
