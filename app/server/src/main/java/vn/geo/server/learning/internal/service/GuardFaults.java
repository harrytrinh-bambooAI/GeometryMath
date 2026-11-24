package vn.geo.server.learning.internal.service;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Chế độ lỗi để thử của Kiểm soát đáp án (kịch bản F-GEO-117, US-GEO-609-FC6) theo người gọi. Chỉ
 * móc kiểm thử dev/test (E6GuardTestHookController) đổi được; ở prod tập này luôn rỗng.
 */
@Component
public class GuardFaults {

  private final Set<UUID> failing = ConcurrentHashMap.newKeySet();
  private final Set<UUID> bypass = ConcurrentHashMap.newKeySet();

  /** US-GEO-610-FC1: tắt bộ kiểm cho một người gọi để chứng minh bộ câu thử bắt được lần lộ. */
  public void bypass(UUID userId, boolean on) {
    if (on) {
      bypass.add(userId);
    } else {
      bypass.remove(userId);
    }
  }

  boolean bypassed(UUID userId) {
    return bypass.contains(userId);
  }

  public void set(UUID userId, boolean fail) {
    if (fail) {
      failing.add(userId);
    } else {
      failing.remove(userId);
    }
  }

  public void reset() {
    failing.clear();
    bypass.clear();
  }

  void check(UUID userId) {
    if (failing.contains(userId)) {
      throw new IllegalStateException("Kiểm soát đáp án ở chế độ lỗi để thử (F-GEO-117)");
    }
  }
}
