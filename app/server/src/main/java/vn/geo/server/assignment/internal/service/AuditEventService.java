package vn.geo.server.assignment.internal.service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import vn.geo.server.assignment.api.AssignmentSetNotice;
import vn.geo.server.assignment.internal.repo.AuditEventRepository;
import vn.geo.server.assignment.internal.repo.SetReleaseRepository;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.event.DomainEvents;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.RequestContext;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.iam.api.StudentPasswordResetEvent;

/**
 * AuditEvent chặng 3 (US-S3E5-01): EARLY_RELEASE, RELEASE_TIME_CHANGE, PASSWORD_RESET trên
 * audit_log (PLAN A8). Bản ghi Thành công nằm CÙNG giao dịch với thao tác (BR-S3E5-04); bản ghi Từ
 * chối (BR-S3E5-07) ghi ở giao dịch riêng để không mất khi yêu cầu bị ném lỗi. Không có đường sửa
 * hay xóa (BR-S3E5-05). Cũng là nơi phát sự kiện miền của bộ bài cho module notify (BR-S3E4-14).
 */
@Service
public class AuditEventService {

  public static final String EARLY_RELEASE = "early_release";
  public static final String RELEASE_TIME_CHANGE = "release_time_change";
  public static final String PASSWORD_RESET = "password_reset";
  public static final String TARGET_SET = "assignment_set";
  public static final String TARGET_USER = "user";

  /** Đúng 11 trường được phép của BR-S3E5-02 (US-S3E5-01-SC4); giờ là giờ máy chủ UTC. */
  public record AuditEventView(
      long id,
      String type,
      String actorCode,
      String actorRole,
      String targetType,
      String targetId,
      UUID classId,
      Instant before,
      Instant after,
      String result,
      Instant at) {}

  private final AuditEventRepository repo;
  private final SetReleaseRepository sets;
  private final DomainEvents events;
  private final TransactionTemplate separateTx;

  public AuditEventService(
      AuditEventRepository repo,
      SetReleaseRepository sets,
      DomainEvents events,
      PlatformTransactionManager tm) {
    this.repo = repo;
    this.sets = sets;
    this.events = events;
    this.separateTx = new TransactionTemplate(tm);
    this.separateTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
  }

  /** Giáo viên đang gọi; sai vai thì ghi bản ghi Từ chối rồi FORBIDDEN (US-S3E4-02-FC6). */
  public AuthPrincipal requireTeacher(String action, String targetType) {
    AuthPrincipal p = RequestContext.principal();
    if (p.role() != Role.TEACHER) {
      denied(p, action, targetType);
      throw new AppException(ErrorCode.FORBIDDEN);
    }
    return p;
  }

  /** Bản ghi Từ chối: chỉ loại, người gọi, loại đối tượng, giờ (BR-S3E5-07, M-S3-06). */
  public void denied(AuthPrincipal p, String action, String targetType) {
    separateTx.executeWithoutResult(
        s -> repo.recordDenied(p.userId(), roleOf(p), action, targetType));
  }

  /** Mở ngay thành công: một AuditEvent + một đợt thông báo "lời giải đã mở" (BR-S3E4-14). */
  public void opened(
      AuthPrincipal p,
      UUID setId,
      UUID classId,
      String title,
      Instant before,
      Instant now,
      String requestKey) {
    repo.recordSuccess(
        p.userId(), roleOf(p), EARLY_RELEASE, TARGET_SET, setId, classId, before, now, requestKey);
    publish(p, AssignmentSetNotice.Kind.SOLUTION_OPENED, setId, classId, title, null, now);
  }

  /** Đổi giờ mở của bộ ĐÃ GIAO (BR-S3E5-06): một AuditEvent + một đợt thông báo "giờ mở đổi". */
  public void timeChanged(
      AuthPrincipal p,
      UUID setId,
      UUID classId,
      String title,
      Instant before,
      Instant after,
      Instant now,
      String requestKey) {
    repo.recordSuccess(
        p.userId(),
        roleOf(p),
        RELEASE_TIME_CHANGE,
        TARGET_SET,
        setId,
        classId,
        before,
        after,
        requestKey);
    publish(p, AssignmentSetNotice.Kind.RELEASE_TIME_CHANGED, setId, classId, title, after, now);
  }

  /** Vừa giao bộ: thông báo "bài mới" (không phải AuditEvent). */
  public void assigned(AuthPrincipal p, UUID setId, UUID classId, String title, Instant now) {
    publish(p, AssignmentSetNotice.Kind.ASSIGNED, setId, classId, title, null, now);
  }

  public boolean alreadyDone(AuthPrincipal p, String action, UUID setId, String requestKey) {
    return requestKey != null && repo.exists(p.userId(), action, setId, requestKey);
  }

  /** Nhật ký của lớp mình (BR-S3E5-08): học sinh FORBIDDEN, giáo viên khác NOT_FOUND. */
  public List<AuditEventView> classLog(UUID classId) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    if (!repo.ownsClass(p.userId(), classId)) {
      throw new AppException(ErrorCode.NOT_FOUND);
    }
    return repo.ofClass(classId).stream().map(AuditEventService::view).toList();
  }

  /**
   * Giáo viên vừa đặt lại mật khẩu cho học sinh lớp mình (iam, US-S3E1-04): ghi PASSWORD_RESET. Sự
   * kiện không mang mật khẩu hay băm nên bản ghi cũng không có (BR-S3E5-03). Chạy đồng bộ trong
   * giao dịch đổi mật khẩu của iam (Dev-S3E5-08): ghi lỗi thì mật khẩu không đổi (BR-S3E5-04).
   */
  @EventListener
  public void on(StudentPasswordResetEvent e) {
    repo.recordSuccess(
        e.actorId(),
        "teacher",
        PASSWORD_RESET,
        TARGET_USER,
        e.studentId(),
        e.classId(),
        null,
        null,
        null);
  }

  private void publish(
      AuthPrincipal p,
      AssignmentSetNotice.Kind kind,
      UUID setId,
      UUID classId,
      String title,
      Instant releaseAt,
      Instant now) {
    events.publish(
        new AssignmentSetNotice(
            Uuid7.next(),
            p.orgId(),
            now,
            kind,
            setId,
            classId,
            title,
            releaseAt,
            sets.activeStudents(classId)));
  }

  private static String roleOf(AuthPrincipal p) {
    return p.role().name().toLowerCase(Locale.ROOT);
  }

  private static AuditEventView view(AuditEventRepository.EventRow r) {
    String target =
        TARGET_USER.equals(r.targetType()) && r.targetUserCode() != null
            ? r.targetUserCode()
            : String.valueOf(r.targetId());
    return new AuditEventView(
        r.id(),
        r.action().toUpperCase(Locale.ROOT),
        r.actorCode(),
        r.actorRole() == null ? null : r.actorRole().toUpperCase(Locale.ROOT),
        r.targetType(),
        target,
        r.classId(),
        r.before(),
        r.after(),
        "success".equals(r.outcome()) ? "SUCCESS" : "DENIED",
        r.at());
  }
}
