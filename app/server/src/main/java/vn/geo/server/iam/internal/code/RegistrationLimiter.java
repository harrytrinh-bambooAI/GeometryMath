package vn.geo.server.iam.internal.code;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Giới hạn tạo tài khoản theo địa chỉ mạng (PO-63, BR-S2E1-06): tối đa 5 lần mỗi giờ (cửa sổ
 * trượt). Bộ đếm chỉ ở bộ nhớ, khóa là HMAC của IP, mục cũ hơn 1 giờ tự dọn — không lưu thành bảng
 * (BR-S3E5-37). MVP một nút; nhiều nút thì chuyển sang Redis có TTL 1 giờ.
 */
public final class RegistrationLimiter {

  public static final int MAX = 5;
  public static final Duration WINDOW = Duration.ofHours(1);

  private final Clock clock;
  private final Map<String, Deque<Long>> hits = new ConcurrentHashMap<>();

  public RegistrationLimiter(Clock clock) {
    this.clock = clock;
  }

  /** Thời gian phải chờ nếu IP đã đủ 5 lần trong giờ; rỗng nếu còn được đăng ký. */
  public Optional<Duration> blocked(String ipKey) {
    long now = clock.millis();
    Deque<Long> q = hits.get(ipKey);
    if (q == null) {
      return Optional.empty();
    }
    synchronized (q) {
      prune(q, now);
      if (q.size() < MAX) {
        return Optional.empty();
      }
      long waitMs = WINDOW.toMillis() - (now - q.peekFirst());
      return Optional.of(Duration.ofMillis(Math.max(1000, waitMs)));
    }
  }

  /** Ghi một lần đăng ký thành công. */
  public void record(String ipKey) {
    long now = clock.millis();
    if (hits.size() > 10_000) {
      sweep(now);
    }
    Deque<Long> q = hits.computeIfAbsent(ipKey, k -> new ArrayDeque<>());
    synchronized (q) {
      prune(q, now);
      q.addLast(now);
    }
  }

  /** Móc kiểm thử (dev/test): lùi các mốc của một IP để giả lập "đã chờ đủ Retry-After". */
  public void age(String ipKey, Duration by) {
    Deque<Long> q = hits.get(ipKey);
    if (q == null) {
      return;
    }
    synchronized (q) {
      Deque<Long> moved = new ArrayDeque<>();
      for (Long t : q) {
        moved.addLast(t - by.toMillis());
      }
      q.clear();
      q.addAll(moved);
    }
  }

  public void reset(String ipKey) {
    hits.remove(ipKey);
  }

  private static void prune(Deque<Long> q, long now) {
    while (!q.isEmpty() && now - q.peekFirst() >= WINDOW.toMillis()) {
      q.pollFirst();
    }
  }

  private void sweep(long now) {
    hits.entrySet()
        .removeIf(e -> {
          Deque<Long> q = e.getValue();
          synchronized (q) {
            prune(q, now);
            return q.isEmpty();
          }
        });
  }
}
