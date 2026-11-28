package vn.geo.server.assignment.internal.service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import vn.geo.server.assignment.internal.repo.ReleaseRepository;
import vn.geo.server.assignment.internal.repo.ReleaseRepository.ClassOption;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.content.api.BankAccess;

/**
 * E6: màn "Chi tiết đề &amp; giao bài" (US-GEO-602…604), thẻ vàng giờ mở và nhật ký đổi giờ
 * (US-GEO-605), dòng "Hôm nay em có n bài cần làm" (US-GEO-606-SC7, SC8), trạng thái khóa của màn
 * xem trước (US-GEO-607, D-GEO-139, 146). Mọi mốc giờ là giờ máy chủ; serverNow đi kèm để thiết bị
 * tính "Còn …" mà không tin đồng hồ của mình (BR-GEO-231).
 */
@Service
public class ReleaseService {

  public record Problem(UUID versionId, String code, String title, String statement) {}

  public record ExistingAssignment(UUID id, String releaseState, Instant releaseAt) {}

  /** label = "{tên lớp} · {cấp} ({số học sinh} HS)" (BR-GEO-223). */
  public record ClassChoice(
      UUID id,
      String name,
      String level,
      int studentCount,
      String label,
      ExistingAssignment assignment) {}

  public record AssignOptions(Problem problem, List<ClassChoice> classes, Instant serverNow) {}

  public record ReleaseView(
      UUID id,
      UUID classId,
      String className,
      String problemCode,
      String releaseState,
      Instant releaseAt,
      int releaseRev,
      boolean allowHint,
      boolean allowQa,
      Instant serverNow) {}

  public record LogItem(
      long id, Instant at, String actorName, String action, Instant before, Instant after) {}

  public record Todo(int count) {}

  public record PreviewLock(
      String state, Instant releaseAt, Instant serverNow, UUID assignmentId) {}

  private final ReleaseRepository repo;
  private final BankAccess bank;

  public ReleaseService(ReleaseRepository repo, BankAccess bank) {
    this.repo = repo;
    this.bank = bank;
  }

  public AssignOptions options(UUID versionId) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    bank.requireVisible(versionId); // quyền bộ đề (FIX3, Dev-E6-01)
    ReleaseRepository.CatalogProblem cp =
        repo.catalogProblem(versionId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    List<ClassChoice> classes =
        repo.classOptions(p.userId(), cp.problemId()).stream().map(ReleaseService::choice).toList();
    return new AssignOptions(
        new Problem(cp.versionId(), cp.code(), cp.title(), cp.statement()), classes, repo.now());
  }

  public ReleaseView release(UUID assignmentId) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    ReleaseRepository.ReleaseRow r =
        repo.release(p.userId(), assignmentId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    return new ReleaseView(
        r.id(),
        r.classId(),
        r.className(),
        r.problemCode(),
        r.releaseState(),
        r.releaseAt(),
        r.releaseRev(),
        r.allowHint(),
        r.allowQa(),
        repo.now());
  }

  public List<LogItem> log(UUID assignmentId) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    repo.release(p.userId(), assignmentId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    return repo.releaseLog(assignmentId).stream()
        .map(l -> new LogItem(l.id(), l.at(), l.actorName(), l.action(), l.before(), l.after()))
        .toList();
  }

  public Todo todo() {
    AuthPrincipal p = Roles.require(Role.STUDENT);
    return new Todo(repo.todoCount(p.userId()));
  }

  /**
   * Trạng thái khóa của màn xem trước (BR-GEO-246): đề đã giao cho lớp đang chọn → theo chính bài
   * giao đó (D-GEO-139); chưa giao và đã chọn giờ ở bước 3 → Sắp mở với giờ đó (D-GEO-146); còn lại
   * → Chưa đặt giờ.
   */
  public PreviewLock previewLock(UUID versionId, UUID classId, OffsetDateTime releaseAt) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    bank.requireVisible(versionId); // quyền bộ đề (FIX3, Dev-E6-01)
    ReleaseRepository.CatalogProblem cp =
        repo.catalogProblem(versionId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    Instant now = repo.now();
    if (classId != null) {
      for (ClassOption c : repo.classOptions(p.userId(), cp.problemId())) {
        if (c.id().equals(classId) && c.assignmentId() != null) {
          return new PreviewLock(c.releaseState(), c.releaseAt(), now, c.assignmentId());
        }
      }
    }
    if (releaseAt != null) {
      Instant r = releaseAt.toInstant();
      // giờ chọn đã qua: bài chưa giao được với giờ này (VALIDATION_PAST) → Chưa đặt giờ, không
      // hiện "Đã mở" cho đề chưa giao (soát E6 m7)
      if (r.isAfter(now)) {
        return new PreviewLock("upcoming", r, now, null);
      }
    }
    return new PreviewLock("unset", null, now, null);
  }

  private static ClassChoice choice(ClassOption c) {
    String level = "thcs".equals(c.level()) ? "THCS" : "THPT";
    return new ClassChoice(
        c.id(),
        c.name(),
        c.level(),
        c.studentCount(),
        c.name() + " · " + level + " (" + c.studentCount() + " HS)",
        c.assignmentId() == null
            ? null
            : new ExistingAssignment(c.assignmentId(), c.releaseState(), c.releaseAt()));
  }
}
