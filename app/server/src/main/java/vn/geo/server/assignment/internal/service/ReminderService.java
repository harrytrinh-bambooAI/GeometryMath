package vn.geo.server.assignment.internal.service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import vn.geo.server.assignment.api.AssignmentSetNotice;
import vn.geo.server.assignment.internal.repo.ReminderRepository;
import vn.geo.server.common.event.DomainEvents;

/**
 * US-S3E6-03 — tác vụ nhắc việc trong ứng dụng, chạy mỗi phút (FC7: sai số ≤ 1 phút). Không gửi gì
 * ra ngoài: chỉ phát {@link AssignmentSetNotice} để module notify tạo Notification (PO-65 Q9).
 *
 * <ul>
 *   <li>Sắp hết hạn (BR-S3E6-20…23): 24 giờ trước {@code due_at}, chỉ em Đang học chưa xong; giờ
 *       nhắc rơi vào 21:00–06:00 giờ Việt Nam được hoãn tới 06:00 (đúng 21:00:00 đã yên lặng, đúng
 *       06:00:00 được gửi). Bộ giao khi còn dưới 24 giờ, bộ không có hạn: không nhắc. Đổi hạn: nhắc
 *       tính lại từ hạn mới (hạn cũ không còn trong dữ liệu nên nhắc cũ chưa gửi coi như hủy).
 *   <li>Lời giải đã mở theo lịch (BR-S3E6-24): tới {@code release_at} của bộ, mỗi em Đang học một
 *       thông báo; em đã nhận "lời giải đã mở" của bộ (Mở ngay) thì thôi.
 * </ul>
 *
 * Chạy lặp không gửi hai lần (BR-S3E6-25): eventId tất định theo (loại, bộ, giờ) + ràng buộc duy
 * nhất của bảng notification.
 */
@Service
public class ReminderService {

  static final ZoneId VN = ZoneId.of("Asia/Ho_Chi_Minh");
  static final LocalTime QUIET_FROM = LocalTime.of(21, 0);
  static final LocalTime QUIET_TO = LocalTime.of(6, 0);
  static final Duration LEAD = Duration.ofHours(24);

  public record Run(int dueSoonEvents, int releasedEvents) {}

  private final ReminderRepository repo;
  private final DomainEvents events;

  public ReminderService(ReminderRepository repo, DomainEvents events) {
    this.repo = repo;
    this.events = events;
  }

  @Scheduled(cron = "${geo.s3e6-reminders.cron:0 * * * * *}", zone = "Asia/Ho_Chi_Minh")
  public void tick() {
    runAt(Instant.now());
  }

  /** Một lượt của tác vụ ở giờ {@code now} (IT và móc kiểm thử giả lập đồng hồ qua tham số này). */
  public Run runAt(Instant now) {
    int due = 0;
    for (ReminderRepository.DueSet s : repo.dueWithinDay(now)) {
      if (sendAt(s.dueAt().minus(LEAD)).isAfter(now)) {
        continue;
      }
      List<UUID> to = repo.unfinishedStudents(s.orgId(), s.classId(), s.id());
      if (to.isEmpty()) {
        continue;
      }
      events.publish(
          new AssignmentSetNotice(
              eventId("due_soon", s.id(), s.dueAt()),
              s.orgId(),
              now,
              AssignmentSetNotice.Kind.DUE_SOON,
              s.id(),
              s.classId(),
              s.title(),
              null,
              to,
              s.dueAt()));
      due++;
    }
    int released = 0;
    for (ReminderRepository.ReleasedSet s : repo.releasedWithinDay(now)) {
      List<UUID> to = repo.activeStudents(s.orgId(), s.classId());
      if (to.isEmpty()) {
        continue;
      }
      events.publish(
          new AssignmentSetNotice(
              eventId("solution_released", s.id(), s.releaseAt()),
              s.orgId(),
              now,
              AssignmentSetNotice.Kind.SOLUTION_RELEASED,
              s.id(),
              s.classId(),
              s.title(),
              s.releaseAt(),
              to));
      released++;
    }
    return new Run(due, released);
  }

  /** Giờ gửi của một giờ nhắc theo giờ yên lặng 21:00–06:00 giờ Việt Nam (BR-S3E6-21). */
  static Instant sendAt(Instant remindAt) {
    ZonedDateTime local = remindAt.atZone(VN);
    LocalTime t = local.toLocalTime();
    if (!t.isBefore(QUIET_FROM)) {
      return local.toLocalDate().plusDays(1).atTime(QUIET_TO).atZone(VN).toInstant();
    }
    if (t.isBefore(QUIET_TO)) {
      return local.toLocalDate().atTime(QUIET_TO).atZone(VN).toInstant();
    }
    return remindAt;
  }

  static UUID eventId(String kind, UUID setId, Instant at) {
    return UUID.nameUUIDFromBytes(
        (kind + "|" + setId + "|" + at.toEpochMilli()).getBytes(StandardCharsets.UTF_8));
  }
}
