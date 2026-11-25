package vn.geo.server.assignment.internal.service;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import vn.geo.server.common.error.AppException;

/**
 * Giới hạn yêu cầu lời giải của một người dùng (NFR-GEO-056, D-GEO-125 đề xuất): tối đa 5 yêu cầu
 * trong cửa sổ trượt 10 giây; quá thì {@code RATE_LIMITED} kèm Retry-After. Giữ trong bộ nhớ của
 * nút (MVP một nút; nhiều nút thì chuyển sang Redis). Không đổi quyền xem: bị chặn chỉ là chưa trả
 * lời lần đó.
 */
@Component
public class SolutionRateLimiter {

  static final int MAX = 5;
  static final long WINDOW_MS = 10_000;

  private final Map<UUID, Deque<Long>> hits = new ConcurrentHashMap<>();

  private final java.util.concurrent.atomic.AtomicInteger calls =
      new java.util.concurrent.atomic.AtomicInteger();

  public void check(UUID userId) {
    long now = System.currentTimeMillis();
    if ((calls.incrementAndGet() & 255) == 0) {
      sweep(now); // soát E6 m7: dọn người dùng đã hết cửa sổ, map không lớn mãi
    }
    Deque<Long> q = hits.computeIfAbsent(userId, k -> new ArrayDeque<>());
    synchronized (q) {
      while (!q.isEmpty() && now - q.peekFirst() >= WINDOW_MS) {
        q.pollFirst();
      }
      if (q.size() >= MAX) {
        long wait = Math.max(1, (WINDOW_MS - (now - q.peekFirst()) + 999) / 1000);
        throw AppException.rateLimited(Duration.ofSeconds(wait));
      }
      q.addLast(now);
    }
  }

  /** Bỏ các mục không còn yêu cầu nào trong cửa sổ. */
  void sweep(long now) {
    hits.entrySet()
        .removeIf(e -> {
          Deque<Long> q = e.getValue();
          synchronized (q) {
            return q.isEmpty() || now - q.peekLast() >= WINDOW_MS;
          }
        });
  }

  int size() {
    return hits.size();
  }
}
