package vn.geo.server.notify.internal.web;

import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.paging.Page;
import vn.geo.server.common.web.NoStore;
import vn.geo.server.notify.internal.service.NotificationService;

/**
 * Thông báo trong ứng dụng của chính người gọi (US-S3E4-03). Người nhận lấy từ thẻ, không từ đường
 * dẫn hay thân yêu cầu. Phản hồi no-store. POST lên tập hợp luôn FORBIDDEN: thông báo chỉ do hệ
 * thống sinh.
 */
@NoStore
@RestController
public class NotificationController {

  private final NotificationService service;

  public NotificationController(NotificationService service) {
    this.service = service;
  }

  @GetMapping("/api/v1/notifications")
  public Page<NotificationService.NotificationView> list(
      @RequestParam(required = false) String cursor) {
    return service.list(cursor);
  }

  @GetMapping("/api/v1/notifications/unread-count")
  public NotificationService.Unread unread() {
    return service.unread();
  }

  @GetMapping("/api/v1/notifications/{id}")
  public NotificationService.NotificationView one(@PathVariable UUID id) {
    return service.one(id);
  }

  @PostMapping("/api/v1/notifications/{id}/read")
  public NotificationService.NotificationView read(@PathVariable UUID id) {
    return service.read(id);
  }

  @PostMapping("/api/v1/notifications/read-all")
  public NotificationService.Marked readAll() {
    return service.readAll();
  }

  @PostMapping("/api/v1/notifications")
  public void create() {
    service.rejectManualCreate();
  }
}
