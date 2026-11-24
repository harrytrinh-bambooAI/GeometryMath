package vn.geo.server.assignment.internal.service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.assignment.internal.repo.AssignmentRepository;
import vn.geo.server.assignment.internal.repo.AssignmentRepository.ClassAssignmentRow;
import vn.geo.server.assignment.internal.repo.AssignmentRepository.FnResult;
import vn.geo.server.assignment.internal.repo.AssignmentRepository.StudentRow;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.paging.KeysetCursor;
import vn.geo.server.common.paging.ListKind;
import vn.geo.server.common.paging.Page;
import vn.geo.server.common.paging.PageRequest;
import vn.geo.server.common.paging.Paging;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.content.api.BankAccess;

/**
 * Bài giao (US-GEO-101 "Bài của em", US-GEO-104 cách ly, US-GEO-102-FC2 học sinh không đặt giờ).
 * Tài nguyên ngoài lớp/trường của người gọi trả {@code NOT_FOUND} không kèm dữ liệu (BR-GEO-112).
 */
@Service
public class AssignmentService {

  static final ListKind STUDENT_LIST = ListKind.byId("assignment.student");
  static final ListKind CLASS_LIST = ListKind.byId("assignment.class");

  /** Tiến độ k/3 và nhãn (M-GEO-14). */
  public record Progress(int k, String label) {}

  public record StudentAssignment(
      UUID id,
      String problemCode,
      String statement,
      String className,
      Instant releaseAt,
      String releaseState,
      boolean allowHint,
      boolean allowQa,
      String verifyStatus,
      Progress progress,
      String title,
      String solidKind,
      String topic) {}

  public record ClassAssignment(
      UUID id, String problemCode, Instant releaseAt, String releaseState, int releaseRev) {}

  public record Created(UUID id, int releaseRev) {}

  public record Release(int releaseRev, Instant releaseAt) {}

  public record SolutionView(String finalAnswer, JsonNode steps) {}

  /** Thông điệp (nguyên văn work/05_dictionary.md). */
  static final String MSG_42 = "Đề này đã được giao cho lớp %s.";

  static final String MSG_43 =
      "Giờ mở lời giải phải sau thời điểm hiện tại. Muốn mở luôn, hãy chọn “Ngay bây giờ”.";
  static final String MSG_48 = "Lời giải đã mở nên không đổi giờ hay khóa lại được.";
  static final String MSG_49 = "Giờ mở vừa được thay đổi ở nơi khác. Hãy tải lại để xem giờ mới.";
  static final String MSG_EMPTY = "Hãy chọn ngày và giờ mở lời giải.";

  private final AssignmentRepository repo;
  private final vn.geo.server.assignment.internal.repo.ReleaseRepository release;
  private final Paging paging;
  private final JsonMapper mapper;
  private final SolutionRateLimiter limiter;

  private final BankAccess bank;

  public AssignmentService(
      AssignmentRepository repo,
      vn.geo.server.assignment.internal.repo.ReleaseRepository release,
      Paging paging,
      JsonMapper mapper,
      SolutionRateLimiter limiter,
      BankAccess bank) {
    this.limiter = limiter;
    this.repo = repo;
    this.release = release;
    this.paging = paging;
    this.mapper = mapper;
    this.bank = bank;
  }

  public Page<StudentAssignment> myAssignments(Integer limit, String cursor) {
    AuthPrincipal p = Roles.require(Role.STUDENT);
    PageRequest req = paging.request(STUDENT_LIST, limit, cursor);
    UUID after = req.after() == null ? null : req.after().id();
    var rows = repo.studentPage(p.userId(), after, req.fetchSize());
    return paging.page(rows, req, r -> KeysetCursor.of(r.id())).map(AssignmentService::view);
  }

  public StudentAssignment myAssignment(UUID id) {
    AuthPrincipal p = Roles.require(Role.STUDENT);
    return repo.studentOne(p.userId(), id)
        .map(AssignmentService::view)
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
  }

  public SolutionView mySolution(UUID id) {
    AuthPrincipal p = Roles.require(Role.STUDENT);
    limiter.check(p.userId()); // NFR-GEO-056: 5 yêu cầu / 10 giây
    AssignmentRepository.Solution s = repo.solution(p.userId(), id);
    return switch (s.status()) {
      case "OK" -> new SolutionView(s.finalAnswer(), mapper.readTree(s.steps()));
      case "LOCKED" -> throw new AppException(ErrorCode.FORBIDDEN, "Lời giải chưa mở.");
      default -> throw new AppException(ErrorCode.NOT_FOUND);
    };
  }

  public Page<ClassAssignment> classAssignments(UUID classId, Integer limit, String cursor) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    // con trỏ kiểm trước (gắn trường + loại danh sách): con trỏ của trường/danh sách khác =>
    // INVALID_CURSOR
    PageRequest req = paging.request(CLASS_LIST, limit, cursor);
    if (!repo.isTeacherOfClass(p.userId(), classId)) {
      throw new AppException(ErrorCode.NOT_FOUND);
    }
    UUID after = req.after() == null ? null : req.after().id();
    var rows = repo.classPage(classId, after, req.fetchSize());
    return paging.page(rows, req, r -> KeysetCursor.of(r.id())).map(AssignmentService::view);
  }

  public Created create(
      UUID assignmentId,
      UUID classId,
      UUID problemVersionId,
      OffsetDateTime releaseAt,
      Boolean allowHint,
      Boolean allowQa) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    if (classId == null || problemVersionId == null) {
      throw new AppException(ErrorCode.VALIDATION_FAILED, "Cần chọn lớp và đề.");
    }
    // quyền bộ đề kiểm ở dịch vụ, độc lập với fn_create_assignment (FIX3; migration sau có thể định
    // nghĩa lại hàm SQL)
    bank.requireVisible(problemVersionId);
    UUID id = assignmentId == null ? Uuid7.next() : assignmentId;
    FnResult r =
        repo.create(
            p.userId(),
            id,
            classId,
            problemVersionId,
            releaseAt,
            allowHint == null || allowHint,
            allowQa == null || allowQa);
    return switch (r.status()) {
      case "OK", "DUPLICATE" -> new Created(id, r.releaseRev());
      case "CONFLICT_DUPLICATE" ->
          throw new AppException(
              ErrorCode.CONFLICT,
              String.format(MSG_42, release.classNameOfTeacher(p.userId(), classId).orElse("")));
      case "VALIDATION_PAST" -> throw AppException.invalidField("releaseAt", MSG_43);
      // lớp của giáo viên khác cùng trường cũng như không tồn tại (BR-GEO-20, BR-GEO-112)
      default -> throw new AppException(ErrorCode.NOT_FOUND);
    };
  }

  public Release setRelease(UUID id, boolean openNow, OffsetDateTime releaseAt, Integer rev) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    if (!openNow && releaseAt == null) {
      // không có thao tác xóa giờ đã đặt (US-GEO-605-FC4)
      throw AppException.invalidField("releaseAt", MSG_EMPTY);
    }
    FnResult r = repo.setRelease(p.userId(), id, openNow, releaseAt, rev == null ? 0 : rev);
    return switch (r.status()) {
      case "OK" -> new Release(r.releaseRev(), r.releaseAt());
      case "CONFLICT_ALREADY_OPEN" -> throw new AppException(ErrorCode.CONFLICT, MSG_48);
      case "CONFLICT_REV" -> throw new AppException(ErrorCode.CONFLICT, MSG_49);
      case "VALIDATION_PAST" -> throw AppException.invalidField("releaseAt", MSG_43);
      default -> throw new AppException(ErrorCode.NOT_FOUND);
    };
  }

  static Progress progress(boolean opened, int hintLevel, boolean solutionViewed) {
    if (solutionViewed) {
      return new Progress(3, "Đã xem lời giải");
    }
    return switch (hintLevel) {
      case 2 -> new Progress(2, "Đã xem gợi ý 2");
      case 1 -> new Progress(1, "Đã xem gợi ý 1");
      default -> new Progress(0, opened ? "Đã mở bài" : "Chưa bắt đầu");
    };
  }

  private static StudentAssignment view(StudentRow r) {
    return new StudentAssignment(
        r.id(),
        r.problemCode(),
        r.statement(),
        r.className(),
        r.releaseAt(),
        r.releaseState(),
        r.allowHint(),
        r.allowQa(),
        r.verifyStatus(),
        progress(r.opened(), r.hintLevel(), r.solutionViewed()),
        r.title(),
        r.solidKind(),
        r.topic());
  }

  private static ClassAssignment view(ClassAssignmentRow r) {
    return new ClassAssignment(
        r.id(), r.problemCode(), r.releaseAt(), r.releaseState(), r.releaseRev());
  }
}
