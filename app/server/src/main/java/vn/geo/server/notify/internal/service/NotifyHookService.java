package vn.geo.server.notify.internal.service;

import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import vn.geo.server.notify.internal.repo.NotifyHookRepository;

/**
 * MÓC KIỂM THỬ S3-E4 (chỉ dev/test, PO-51): dựng sẵn n thông báo Chưa đọc cho một người (biên 99 /
 * 100, phân trang 45 dòng, tuổi 89 / 91 ngày) và chạy tác vụ dọn dẹp ngay.
 */
@Service
@Profile({"dev", "test"})
public class NotifyHookService {

  private final NotifyHookRepository repo;
  private final NotificationService notifications;

  public NotifyHookService(NotifyHookRepository repo, NotificationService notifications) {
    this.repo = repo;
    this.notifications = notifications;
  }

  public int seed(UUID userId, int count, int ageDays) {
    int n = Math.max(0, Math.min(count, 500));
    for (int i = 0; i < n; i++) {
      repo.insert(userId, "Có bài mới: Bộ thử " + (i + 1), ageDays, n - i);
    }
    return n;
  }

  public int cleanup() {
    return notifications.cleanup();
  }
}
