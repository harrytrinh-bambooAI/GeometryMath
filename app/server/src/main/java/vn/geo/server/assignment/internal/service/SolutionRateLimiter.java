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

  public void check(UUID userId) {
    long now = System.currentTimeMillis();
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
}
