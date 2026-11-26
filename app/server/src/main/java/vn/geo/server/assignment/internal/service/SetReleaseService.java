package vn.geo.server.assignment.internal.service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import vn.geo.server.assignment.internal.repo.AssignmentRepository;
import vn.geo.server.assignment.internal.repo.AssignmentSetRepository;
import vn.geo.server.assignment.internal.repo.AssignmentSetRepository.ItemRow;
import vn.geo.server.assignment.internal.repo.SetReleaseRepository;
import vn.geo.server.assignment.internal.repo.SetReleaseRepository.FollowSet;
import vn.geo.server.assignment.internal.repo.SetReleaseRepository.LockedSet;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;

/**
 * S3-E4: Tổng quan giáo viên (US-S3E4-01, C3-05) và Theo dõi nhanh (US-S3E4-02, C3-06) — em chưa mở
 * bài, "Mở ngay", "Đổi giờ" cho CẢ BỘ (BR-S3E4-17). Giờ mở luôn là giờ máy chủ (BR-S3E4-11): thân
 * yêu cầu không mang giờ nào được tin. Mỗi thao tác thành công = một giao dịch gồm đổi giờ từng đề
 * (fn_set_release), giờ của bộ, một AuditEvent và một đợt Notification (BR-S3E4-14, BR-S3E5-04).
 */
@Service
public class SetReleaseService {

  static final String MSG_63 = "Giờ mới phải sau thời điểm hiện tại. Muốn mở ngay, dùng Mở ngay.";
  static final String MSG_67 = "Lời giải đã mở, không đổi giờ được.";
  static final String MSG_REV = "Lịch mở vừa được đổi ở nơi khác. Tải lại để xem giờ mới.";
  static final String MSG_NOT_ASSIGNED = "Bộ chưa giao hoặc đã lưu trữ.";

  /** Dòng "Bài đang giao" (BR-S3E4-01): chỉ tên bộ, tên lớp, giờ và số (BR-S3E4-02). */
  public record AssignedLine(
      UUID setId,
      String title,
      UUID classId,
      String className,
      Instant dueAt,
      boolean overdue,
      int notOpenedCount,
      String releaseState,
      Instant releaseAt) {}

  public record PendingClass(UUID classId, String className, int count) {}

  public record Pending(int total, List<PendingClass> byClass) {}

  public record Overview(
      boolean hasClasses, List<AssignedLine> assigned, Pending pending, Instant serverNow) {}

  public record StudentLine(String displayName, String userCode) {}

  public record Follow(
      UUID setId,
      String title,
      String status,
      UUID classId,
      String className,
      Instant dueAt,
      boolean overdue,
      String releaseState,
      Instant releaseAt,
      int version,
      List<StudentLine> notOpened,
      Instant serverNow) {}

  private final SetReleaseRepository repo;
  private final AssignmentSetRepository sets;
  private final AssignmentRepository assignments;
  private final AuditEventService audit;
  private final TransactionTemplate tx;

  public SetReleaseService(
      SetReleaseRepository repo,
      AssignmentSetRepository sets,
      AssignmentRepository assignments,
      AuditEventService audit,
      PlatformTransactionManager tm) {
    this.repo = repo;
    this.sets = sets;
    this.assignments = assignments;
    this.audit = audit;
    this.tx = new TransactionTemplate(tm);
  }

  // ------------------------------------------------------------------ đọc

  public Overview overview() {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    Instant now = sets.now();
    List<SetReleaseRepository.ClassPending> classes = repo.classesWithPending(p.userId());
    List<PendingClass> byClass =
        classes.stream()
            .filter(c -> c.pending() > 0)
            .map(c -> new PendingClass(c.id(), c.name(), c.pending()))
            .toList();
    List<AssignedLine> assigned =
        repo.assignedSets(p.userId()).stream()
            .map(s ->
                new AssignedLine(
                    s.id(),
                    s.title(),
                    s.classId(),
                    s.className(),
                    s.dueAt(),
                    s.dueAt() != null && now.isAfter(s.dueAt()),
                    s.notOpened(),
                    state(s.lockedItems(), s.nextReleaseAt()),
                    shownRelease(s.lockedItems(), s.nextReleaseAt(), s.setReleaseAt())))
            .toList();
    return new Overview(
        !classes.isEmpty(),
        assigned,
        new Pending(byClass.stream().mapToInt(PendingClass::count).sum(), byClass),
        now);
  }

  public Follow follow(UUID setId) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    FollowSet s =
        repo.followSet(p.userId(), setId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    Instant now = sets.now();
    return new Follow(
        s.id(),
        s.title(),
        s.status(),
        s.classId(),
        s.className(),
        s.dueAt(),
        s.dueAt() != null && now.isAfter(s.dueAt()),
        state(s.lockedItems(), s.nextReleaseAt()),
        shownRelease(s.lockedItems(), s.nextReleaseAt(), s.setReleaseAt()),
        version(sets.items(setId)),
        repo.notOpened(setId, s.classId()).stream()
            .map(l -> new StudentLine(l.displayName(), l.userCode()))
            .toList(),
        now);
  }

  // ------------------------------------------------------------------ Mở ngay, Đổi giờ

  /** "Mở ngay" cả bộ (BR-S3E4-11, 13, 16): giờ mở := giờ máy chủ lúc xử lý. */
  public Follow openNow(UUID setId, String requestKey, Integer version) {
    AuthPrincipal p =
        audit.requireTeacher(AuditEventService.EARLY_RELEASE, AuditEventService.TARGET_SET);
    String key = key(requestKey);
    guarded(
        p,
        AuditEventService.EARLY_RELEASE,
        () ->
            tx.executeWithoutResult(t -> {
              LockedSet s = lock(p, setId);
              if (audit.alreadyDone(p, AuditEventService.EARLY_RELEASE, setId, key)) {
                return; // gửi lặp cùng khóa: một tác dụng, cùng kết quả (US-S3E4-02-FC2)
              }
              Instant now = sets.now();
              List<ItemRow> items = sets.items(setId);
              List<ItemRow> locked = locked(s, items, now);
              requireVersion(version, items);
              for (ItemRow i : locked) {
                apply(
                    assignments.setRelease(
                        p.userId(), i.assignmentId(), true, null, i.releaseRev()));
              }
              sets.setReleaseOfSet(p.userId(), setId, now.atOffset(ZoneOffset.UTC));
              audit.opened(p, setId, s.classId(), s.title(), s.releaseAt(), now, key);
            }));
    return follow(setId);
  }

  /** "Đổi giờ" cả bộ (BR-S3E4-12, 13, 16): giờ mới ≥ giờ máy chủ + 1 phút. */
  public Follow changeTime(UUID setId, String rawReleaseAt, String requestKey, Integer version) {
    AuthPrincipal p =
        audit.requireTeacher(AuditEventService.RELEASE_TIME_CHANGE, AuditEventService.TARGET_SET);
    String key = key(requestKey);
    guarded(
        p,
        AuditEventService.RELEASE_TIME_CHANGE,
        () ->
            tx.executeWithoutResult(t -> {
              LockedSet s = lock(p, setId);
              if (audit.alreadyDone(p, AuditEventService.RELEASE_TIME_CHANGE, setId, key)) {
                return;
              }
              Instant now = sets.now();
              List<ItemRow> items = sets.items(setId);
              List<ItemRow> locked = locked(s, items, now);
              OffsetDateTime at = parse(rawReleaseAt);
              if (!validNewRelease(now, at.toInstant())) {
                throw AppException.invalidField("releaseAt", MSG_63);
              }
              requireVersion(version, items);
              for (ItemRow i : locked) {
                apply(
                    assignments.setRelease(
                        p.userId(), i.assignmentId(), false, at, i.releaseRev()));
              }
              sets.setReleaseOfSet(p.userId(), setId, at);
              audit.timeChanged(
                  p, setId, s.classId(), s.title(), s.releaseAt(), at.toInstant(), now, key);
            }));
    return follow(setId);
  }

  /** Giờ mới hợp lệ khi sau giờ máy chủ ít nhất 1 phút (BR-S3E4-12, QA-S3E4-04). */
  static boolean validNewRelease(Instant now, Instant at) {
    return !at.isBefore(now.plusSeconds(60));
  }

  // ------------------------------------------------------------------ nội bộ

  /** Bộ của người khác / không có: NOT_FOUND kèm một bản ghi Từ chối (US-S3E4-02-FC5). */
  private void guarded(AuthPrincipal p, String action, Runnable work) {
    try {
      work.run();
    } catch (AppException e) {
      if (e.code() == ErrorCode.NOT_FOUND) {
        audit.denied(p, action, AuditEventService.TARGET_SET);
      }
      throw e;
    }
  }

  private LockedSet lock(AuthPrincipal p, UUID setId) {
    return repo.lock(p.userId(), setId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
  }

  /** Các đề đã giao còn khóa; không còn đề nào khóa → CONFLICT MSG-S3-67 (BR-S3E4-13). */
  private static List<ItemRow> locked(LockedSet s, List<ItemRow> items, Instant now) {
    if (!"assigned".equals(s.status()) && !"closed".equals(s.status())) {
      throw new AppException(ErrorCode.CONFLICT, MSG_NOT_ASSIGNED);
    }
    List<ItemRow> locked =
        items.stream()
            .filter(i -> i.assignmentId() != null)
            .filter(i -> i.assignmentReleaseAt() == null || i.assignmentReleaseAt().isAfter(now))
            .toList();
    if (locked.isEmpty()) {
      throw new AppException(ErrorCode.CONFLICT, MSG_67);
    }
    return locked;
  }

  /** Hai nơi cùng đổi: yêu cầu mang phiên bản lịch cũ thua (US-S3E4-02-FC4). */
  private static void requireVersion(Integer version, List<ItemRow> items) {
    if (version != null && version != version(items)) {
      throw new AppException(ErrorCode.CONFLICT, MSG_REV);
    }
  }

  private static void apply(AssignmentRepository.FnResult r) {
    switch (r.status()) {
      case "OK" -> {}
      case "CONFLICT_ALREADY_OPEN" -> throw new AppException(ErrorCode.CONFLICT, MSG_67);
      case "CONFLICT_REV" -> throw new AppException(ErrorCode.CONFLICT, MSG_REV);
      case "VALIDATION_PAST" -> throw AppException.invalidField("releaseAt", MSG_63);
      default -> throw new AppException(ErrorCode.NOT_FOUND);
    }
  }

  private static OffsetDateTime parse(String raw) {
    if (raw == null || raw.isBlank()) {
      throw AppException.invalidField("releaseAt", MSG_63);
    }
    try {
      return OffsetDateTime.parse(raw.strip());
    } catch (DateTimeParseException e) {
      throw AppException.invalidField("releaseAt", MSG_63);
    }
  }

  private static String key(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    String k = raw.strip();
    if (k.length() > 64) {
      throw AppException.invalidField("requestKey", "Khóa chống lặp dài quá 64 ký tự.");
    }
    return k;
  }

  /** Cùng cách tính với scheduleVersion của màn soạn bộ (S3-E2): tổng release_rev các đề. */
  private static int version(List<ItemRow> items) {
    return items.stream().mapToInt(ItemRow::releaseRev).sum();
  }

  /** Còn đề khóa: giờ mở gần nhất sắp tới; đã mở hết: giờ mở của bộ (giờ đã mở). */
  private static Instant shownRelease(
      int lockedItems, Instant nextReleaseAt, Instant setReleaseAt) {
    return lockedItems > 0 ? nextReleaseAt : setReleaseAt;
  }

  private static String state(int lockedItems, Instant nextReleaseAt) {
    if (lockedItems == 0) {
      return "open";
    }
    return nextReleaseAt == null ? "unset" : "upcoming";
  }
}
