package vn.geo.server.notify.internal.service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import vn.geo.server.assignment.api.AssignmentSetNotice;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.paging.KeysetCursor;
import vn.geo.server.common.paging.ListKind;
import vn.geo.server.common.paging.Page;
import vn.geo.server.common.paging.PageRequest;
import vn.geo.server.common.paging.Paging;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.RequestContext;
import vn.geo.server.notify.internal.repo.NotificationRepository;

/**
 * Notification trong ứng dụng (US-S3E4-03, PLAN A9). Ba loại P0 (BR-S3E4-20): bài mới, lời giải đã
 * mở, giờ mở đổi — sinh từ sự kiện miền {@link AssignmentSetNotice} của module assignment, trong
 * cùng giao dịch với thao tác của giáo viên. Chỉ trong ứng dụng: module KHÔNG có kênh gửi ngoài
 * (push, email, SMS; BR-S3E4-26). Nội dung chỉ có tên bộ và giờ (BR-S3E4-25).
 */
@Service
public class NotificationService {

  static final ListKind LIST = ListKind.byId("notify.notifications");
  static final int PAGE_SIZE = 20;
  public static final int RETENTION_DAYS = 90;
  private static final DateTimeFormatter VN =
      DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy").withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

  public record NotificationView(
      UUID id, String kind, String body, UUID setId, Instant createdAt, boolean read) {}

  public record Unread(int count) {}

  public record Marked(int updated) {}

  private final NotificationRepository repo;
  private final Paging paging;

  public NotificationService(NotificationRepository repo, Paging paging) {
    this.repo = repo;
    this.paging = paging;
  }

  // ------------------------------------------------------------------ sinh thông báo

  @EventListener
  public void on(AssignmentSetNotice e) {
    handle(e);
  }

  /** Xử lý lặp cùng sự kiện không tạo hàng thứ hai (BR-S3E4-22). Trả số thông báo mới. */
  public int handle(AssignmentSetNotice e) {
    return repo.insertAll(
        e.orgId(),
        e.eventId(),
        kind(e.kind()),
        e.setId(),
        body(e),
        e.recipientIds(),
        e.occurredAt(),
        e.kind() == AssignmentSetNotice.Kind.SOLUTION_RELEASED);
  }

  /** Câu chữ MSG-S3-68. */
  static String body(AssignmentSetNotice e) {
    return switch (e.kind()) {
      case ASSIGNED -> "Có bài mới: " + e.setTitle();
      case SOLUTION_OPENED, SOLUTION_RELEASED -> "Lời giải " + e.setTitle() + " đã mở";
      case RELEASE_TIME_CHANGED ->
          "Giờ mở lời giải " + e.setTitle() + " đổi sang " + VN.format(e.releaseAt());
      case DUE_SOON -> dueSoon(e.setTitle(), e.dueAt());
    };
  }

  /**
   * MSG-S3-95 "Bộ {tên bộ} sắp hết hạn: {HH:mm, dd/MM/yyyy}" (US-S3E6-03). Tên bộ đã bắt đầu bằng
   * "Bộ" (ví dụ "Bộ bài tuần 15") thì không lặp chữ "Bộ" — khớp câu mẫu của AC SC1.
   */
  static String dueSoon(String title, Instant dueAt) {
    String name = title.regionMatches(true, 0, "Bộ ", 0, 3) ? title : "Bộ " + title;
    return name + " sắp hết hạn: " + VN.format(dueAt);
  }

  private static String kind(AssignmentSetNotice.Kind k) {
    return switch (k) {
      case ASSIGNED -> "new_set";
      case SOLUTION_OPENED, SOLUTION_RELEASED -> "solution_open";
      case RELEASE_TIME_CHANGED -> "release_changed";
      case DUE_SOON -> "due_soon";
    };
  }

  // ------------------------------------------------------------------ người nhận đọc

  /** 20 dòng mỗi trang, mới nhất trước, con trỏ mờ, không có tổng số (BR-S3E4-24, D-GEO-142). */
  public Page<NotificationView> list(String cursor) {
    AuthPrincipal p = RequestContext.principal();
    PageRequest req = paging.request(LIST, PAGE_SIZE, cursor);
    UUID after = req.after() == null ? null : req.after().id();
    List<NotificationRepository.Row> rows = repo.page(p.userId(), after, req.fetchSize());
    return paging.page(rows, req, r -> KeysetCursor.ofId(r.id())).map(NotificationService::view);
  }

  public Unread unread() {
    return new Unread(repo.unreadCount(RequestContext.principal().userId()));
  }

  /** Thông báo của người khác: NOT_FOUND, không tiết lộ có tồn tại (US-S3E4-03-FC3). */
  public NotificationView one(UUID id) {
    AuthPrincipal p = RequestContext.principal();
    return repo.one(p.userId(), id)
        .map(NotificationService::view)
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
  }

  /** Mở thông báo = Đã đọc (BR-S3E4-23); lặp lại trả cùng kết quả. */
  public NotificationView read(UUID id) {
    AuthPrincipal p = RequestContext.principal();
    repo.markRead(p.userId(), id);
    return one(id);
  }

  public Marked readAll() {
    return new Marked(repo.markAllRead(RequestContext.principal().userId()));
  }

  /** Thông báo chỉ do hệ thống sinh từ sự kiện: không ai tạo tay được (US-S3E4-03-FC9). */
  public void rejectManualCreate() {
    RequestContext.principal();
    throw new AppException(ErrorCode.FORBIDDEN);
  }

  // ------------------------------------------------------------------ dọn dẹp

  /** Mỗi đêm xóa thông báo quá 90 ngày (BR-S3E4-27, QA-S3E4-09). Trả số hàng đã xóa. */
  @Scheduled(cron = "0 30 3 * * *", zone = "Asia/Ho_Chi_Minh")
  public int cleanup() {
    return repo.deleteOlderThan(RETENTION_DAYS);
  }

  private static NotificationView view(NotificationRepository.Row r) {
    return new NotificationView(
        r.id(), r.kind(), r.body(), r.setId(), r.createdAt(), r.readAt() != null);
  }
}
