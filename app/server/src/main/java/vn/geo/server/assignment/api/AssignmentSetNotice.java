package vn.geo.server.assignment.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import vn.geo.server.common.event.DomainEvent;

/**
 * Một việc của bộ bài mà học sinh Đang học của lớp cần biết (US-S3E4-03, BR-S3E4-20): vừa giao, lời
 * giải vừa mở, giờ mở vừa đổi. Module notify nghe và tạo Notification trong ứng dụng. Người nhận
 * được chốt tại lúc xảy ra sự kiện (BR-S3E4-21). Chỉ có tên bộ và giờ: không đáp số, không tên học
 * sinh (BR-S3E4-25).
 *
 * <p>US-S3E6-03 (nhắc việc): DUE_SOON (24 giờ trước hạn, chỉ em chưa xong) và SOLUTION_RELEASED
 * (tới giờ mở lời giải theo lịch) do tác vụ nhắc của assignment phát, eventId tất định theo (bộ,
 * loại, giờ) để chạy lặp không tạo thông báo thứ hai (BR-S3E6-25).
 *
 * @param releaseAt giờ mở mới (chỉ với RELEASE_TIME_CHANGED)
 * @param recipientIds học sinh Đang học của lớp lúc sự kiện xảy ra
 * @param dueAt hạn làm (chỉ với DUE_SOON)
 */
public record AssignmentSetNotice(
    UUID eventId,
    UUID orgId,
    Instant occurredAt,
    Kind kind,
    UUID setId,
    UUID classId,
    String setTitle,
    Instant releaseAt,
    List<UUID> recipientIds,
    Instant dueAt)
    implements DomainEvent {

  public enum Kind {
    ASSIGNED,
    SOLUTION_OPENED,
    RELEASE_TIME_CHANGED,
    /** US-S3E6-03: bộ sắp hết hạn (MSG-S3-95). */
    DUE_SOON,
    /** US-S3E6-03: tới giờ mở lời giải theo lịch (Mở ngay vẫn là SOLUTION_OPENED). */
    SOLUTION_RELEASED
  }

  public AssignmentSetNotice {
    recipientIds = List.copyOf(recipientIds);
  }

  /** Sự kiện S3-E4 (không có hạn làm trong câu thông báo). */
  public AssignmentSetNotice(
      UUID eventId,
      UUID orgId,
      Instant occurredAt,
      Kind kind,
      UUID setId,
      UUID classId,
      String setTitle,
      Instant releaseAt,
      List<UUID> recipientIds) {
    this(eventId, orgId, occurredAt, kind, setId, classId, setTitle, releaseAt, recipientIds, null);
  }
}
