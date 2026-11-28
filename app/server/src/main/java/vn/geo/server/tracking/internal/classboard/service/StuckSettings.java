package vn.geo.server.tracking.internal.classboard.service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * S3-E3 — cấu hình của "Cả lớp vướng ở đâu": ngưỡng bài làm tối thiểu để gộp {@code MIN_AGGREGATE}
 * (geo.tracking.min-aggregate, mặc định 5 — PO-65 Q6, PO-66 QA-S3E3-02; không viết cứng,
 * US-S3E3-03-FC7) và đáp mẫu của bộ gộp khi nhà cung cấp AI là giả lập (D-GEO-047): kịch bản theo
 * giáo viên do móc kiểm thử dev/test đặt; không có kịch bản thì bộ gộp giả lập tự dựng nhận định từ
 * bước nhiều em dừng nhất.
 */
@Component
public class StuckSettings {

  private volatile int minAggregate;

  /**
   * PO-83 (BR-S3E3-42): trần chờ bộ gộp của cả màn, không cộng dồn qua nhiều đề và không phụ thuộc
   * {@code geo.ai.timeout} (geo.tracking.stuck-deadline, mặc định 10 giây).
   */
  private volatile Duration deadline;

  /** userId giáo viên → (vị trí đề → JSON trả lời của bộ gộp giả lập). */
  private final Map<UUID, Map<Integer, String>> scripts = new ConcurrentHashMap<>();

  public StuckSettings(
      @Value("${geo.tracking.min-aggregate:5}") int minAggregate,
      @Value("${geo.tracking.stuck-deadline:PT10S}") Duration deadline) {
    this.minAggregate = Math.max(1, minAggregate);
    this.deadline = deadline;
  }

  public Duration deadline() {
    return deadline;
  }

  /** Chỉ kiểm thử đổi lúc chạy (đo trần chờ mà không phải chờ đủ 10 giây). */
  public void deadline(Duration value) {
    this.deadline = value;
  }

  public int minAggregate() {
    return minAggregate;
  }

  /** Chỉ kiểm thử / vận hành đổi lúc chạy; giá trị khởi đầu lấy từ cấu hình. */
  public void minAggregate(int value) {
    this.minAggregate = Math.max(1, value);
  }

  public void script(UUID teacherId, Map<Integer, String> replies) {
    if (replies == null || replies.isEmpty()) {
      scripts.remove(teacherId);
    } else {
      scripts.put(teacherId, Map.copyOf(replies));
    }
  }

  String scripted(UUID teacherId, int position) {
    Map<Integer, String> m = scripts.get(teacherId);
    return m == null ? null : m.get(position);
  }
}
