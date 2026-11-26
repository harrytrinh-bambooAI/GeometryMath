package vn.geo.server.iam.internal.service;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import vn.geo.server.iam.internal.repo.CodeAccountRepository;

/**
 * Hủy phiên khi đổi mật khẩu bằng mã khôi phục (QA-S2E1-01, US-S2E1-03-FC3). Thẻ JWT không trạng
 * thái nên giữ mốc {@code app_user.sessions_valid_after}: thẻ có {@code iat} (giây) nhỏ hơn mốc làm
 * tròn lên giây bị từ chối. Mốc ở CSDL (bền) và trong bộ nhớ của nút (nạp lúc khởi động, cập nhật
 * khi hủy) để yêu cầu thường KHÔNG tốn thêm câu SQL. MVP một nút; nhiều nút thì phát sự kiện hủy
 * qua Redis.
 */
@Service
public class SessionRevocations {

  private final CodeAccountRepository repo;
  private final Clock clock;
  private final Map<UUID, Long> cutoffs = new ConcurrentHashMap<>();

  public SessionRevocations(CodeAccountRepository repo, Clock clock) {
    this.repo = repo;
    this.clock = clock;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void load() {
    repo.revokedSessions().forEach((id, at) -> cutoffs.put(id, cutoffSec(at)));
  }

  /** Mốc (giây, làm tròn lên) mà thẻ phát trước đó bị từ chối. */
  public static long cutoffSec(Instant validAfter) {
    if (validAfter == null) {
      return Long.MIN_VALUE;
    }
    long s = validAfter.getEpochSecond();
    return validAfter.getNano() > 0 ? s + 1 : s;
  }

  /** Thẻ phát lúc {@code iatSec} của người dùng còn hiệu lực không. */
  public boolean isValid(UUID userId, long iatSec) {
    Long cut = cutoffs.get(userId);
    return cut == null || iatSec >= cut;
  }

  /** Ghi nhận mốc hủy vừa đặt ở CSDL. */
  public void revoked(UUID userId, Instant validAfter) {
    cutoffs.put(userId, cutoffSec(validAfter));
  }

  /**
   * Trước khi phát thẻ mới cho người vừa đổi mật khẩu trong cùng một giây: chờ sang giây kế để thẻ
   * mới không rơi vào mốc hủy (tối đa 1 giây, chỉ xảy ra ngay sau khi khôi phục).
   */
  public void awaitCutoff(Instant validAfter) {
    if (validAfter == null) {
      return;
    }
    long waitMs = cutoffSec(validAfter) * 1000 - clock.millis();
    if (waitMs > 0 && waitMs <= 1_000) {
      try {
        Thread.sleep(waitMs + 5);
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
      }
    }
  }
}
