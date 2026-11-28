package vn.geo.server.assignment.internal.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.assignment.api.PracticeGrader;
import vn.geo.server.assignment.api.PracticeSets;
import vn.geo.server.assignment.internal.repo.PracticeSetRepository;
import vn.geo.server.assignment.internal.repo.PracticeSetRepository.GradingRow;
import vn.geo.server.assignment.internal.repo.PracticeSetRepository.ItemRow;
import vn.geo.server.assignment.internal.repo.PracticeSetRepository.ProblemRow;
import vn.geo.server.assignment.internal.repo.PracticeSetRepository.SetRow;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;

/**
 * Soạn và giao bài luyện (US-S2E8-05, khung C2-08) trên MỘT mô hình bộ bài {@code assignment_set /
 * assignment_item} (BR-S2E8-32, PO-66 G1). Bài Nháp sửa tự do; giao rồi thì không sửa (BR-S2E8-29).
 * Phía module khác đọc bộ đã giao qua {@link PracticeSets}.
 */
@Service
public class PracticeSetService implements PracticeSets {

  public record ItemInput(
      UUID problemVersionId,
      String answerKind,
      BigDecimal tolerance,
      Integer maxAttempts,
      OffsetDateTime releaseAt) {}

  public record SetInput(String title, OffsetDateTime releaseAt, List<ItemInput> items) {}

  public record Candidate(
      UUID problemId,
      UUID problemVersionId,
      String code,
      String title,
      String statement,
      String problemKind,
      boolean gradable) {}

  public record ItemView(
      int position,
      UUID problemId,
      UUID problemVersionId,
      String code,
      String statement,
      String problemKind,
      String answerKind,
      BigDecimal tolerance,
      int maxAttempts,
      Instant releaseAt,
      UUID assignmentId) {}

  public record SetDetail(
      UUID id,
      String title,
      String status,
      UUID classId,
      String className,
      Instant releaseAt,
      Instant assignedAt,
      List<ItemView> items) {}

  public record SetLine(
      UUID id, String title, String status, String className, Instant releaseAt, int itemCount) {}

  public record ClassOption(UUID id, String name) {}

  /** Kết quả chấm thử ở "Xem trước như học sinh" (không ghi Attempt, BR-S2E8-28). */
  public record PreviewCheck(
      String outcome, String canonical, String reason, String note, String hint) {}

  static final String MSG_TITLE_EMPTY = "Hãy đặt tên bài";
  static final String MSG_TITLE_LONG = "Tên tối đa 80 ký tự";
  static final String MSG_ITEMS_EMPTY = "Chọn ít nhất 1 câu";
  static final String MSG_ITEMS_MANY = "Tối đa 20 câu";
  static final String MSG_ATTEMPTS = "Số lượt từ 1 đến 5";
  static final String MSG_TOLERANCE = "Sai số từ 0 đến 10";
  static final String MSG_TOLERANCE_SCALE = "Tối đa 2 chữ số thập phân";
  static final String MSG_KIND = "Kiểu đáp số là chính xác hoặc xấp xỉ";
  static final String MSG_ASSIGNED = "Bài đã giao không sửa được";
  static final String MSG_PAST = "Giờ mở đã qua, lời giải mở ngay cho học sinh. Vẫn giao?";
  static final String MSG_NO_ANSWER =
      "Đề %s chưa có đáp số máy chấm được. Thêm đáp số ở Soát lời giải.";

  private final PracticeSetRepository repo;
  private final PracticeGrader grader;
  private final JsonMapper mapper;

  public PracticeSetService(PracticeSetRepository repo, PracticeGrader grader, JsonMapper mapper) {
    this.repo = repo;
    this.grader = grader;
    this.mapper = mapper;
  }

  // ------------------------------------------------------------------ giáo viên: soạn

  /**
   * Đề kho cho màn soạn; đề chưa có đáp số máy chấm được vẫn hiện, gradable = false (BR-S2E8-27).
   */
  public List<Candidate> candidates() {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    List<ProblemRow> rows = repo.candidates();
    Map<UUID, String> refs =
        repo.references(p.userId(), rows.stream().map(ProblemRow::versionId).toList());
    return rows.stream()
        .map(r ->
            new Candidate(
                r.problemId(),
                r.versionId(),
                r.code(),
                r.title(),
                r.statement(),
                r.kind(),
                grader.gradable(refs.get(r.versionId()))))
        .toList();
  }

  public List<ClassOption> classes() {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    return repo.classesOf(p.userId()).stream().map(c -> new ClassOption(c.id(), c.name())).toList();
  }

  public List<SetLine> list() {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    return repo.teacherSets(p.userId()).stream()
        .map(s ->
            new SetLine(s.id(), s.title(), s.status(), s.className(), s.releaseAt(), s.itemCount()))
        .toList();
  }

  public SetDetail get(UUID setId) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    return detail(own(p.userId(), setId));
  }

  public SetDetail create(SetInput in) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    List<Resolved> items = validate(p.userId(), in);
    UUID id = Uuid7.next();
    repo.insertSet(id, p.userId(), in.title().strip(), in.releaseAt());
    writeItems(id, items);
    return detail(own(p.userId(), id));
  }

  public SetDetail update(UUID setId, SetInput in) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    SetRow s = own(p.userId(), setId);
    if (!"draft".equals(s.status())) {
      throw new AppException(ErrorCode.CONFLICT, MSG_ASSIGNED); // US-S2E8-05-FC1
    }
    List<Resolved> items = validate(p.userId(), in);
    if (repo.updateDraft(p.userId(), setId, in.title().strip(), in.releaseAt()) == 0) {
      throw new AppException(ErrorCode.CONFLICT, MSG_ASSIGNED);
    }
    repo.deleteItems(setId);
    writeItems(setId, items);
    return detail(own(p.userId(), setId));
  }

  public void delete(UUID setId) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    SetRow s = own(p.userId(), setId);
    if (!"draft".equals(s.status()) || repo.deleteDraft(p.userId(), setId) == 0) {
      throw new AppException(ErrorCode.CONFLICT, MSG_ASSIGNED);
    }
  }

  /** Giao cho một lớp của chính giáo viên (lớp của người khác: NOT_FOUND, US-S2E8-05-FC10). */
  public SetDetail assign(UUID setId, UUID classId, boolean confirmPastRelease) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    if (classId == null) {
      throw AppException.invalidField("classId", "Hãy chọn lớp");
    }
    own(p.userId(), setId);
    String status = repo.assign(p.userId(), setId, classId, confirmPastRelease);
    switch (status) {
      case "OK" -> {}
      case "NOT_FOUND" -> throw new AppException(ErrorCode.NOT_FOUND);
      // lớp của mình Đã lưu trữ: CONFLICT như đường tạo bộ (US-S3E6-02-FC2, BR-S3E6-14)
      case "CONFLICT_ARCHIVED" ->
          throw new AppException(ErrorCode.CONFLICT, AssignmentSetService.MSG_ARCHIVED_CLASS);
      case "CONFLICT_ASSIGNED" ->
          throw new AppException(ErrorCode.CONFLICT, "Bài này đã giao rồi.");
      case "EMPTY" -> throw AppException.invalidField("items", MSG_ITEMS_EMPTY);
      case "PAST_RELEASE" -> throw new AppException(ErrorCode.CONFLICT, MSG_PAST); // US-S2E8-05-FC4
      case "CONFLICT_DUPLICATE" ->
          throw new AppException(ErrorCode.CONFLICT, "Có đề trong bài đã được giao cho lớp này.");
      default ->
          throw AppException.invalidField(
              "items", "Có đề chưa có đáp số máy chấm được. Thêm đáp số ở Soát lời giải.");
    }
    return detail(own(p.userId(), setId));
  }

  public SetDetail close(UUID setId) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    SetRow s = own(p.userId(), setId);
    if ("draft".equals(s.status())) {
      throw new AppException(ErrorCode.CONFLICT, "Bài chưa giao nên chưa đóng được.");
    }
    repo.close(p.userId(), setId);
    return detail(own(p.userId(), setId));
  }

  /** Chấm thử một câu khi "Xem trước như học sinh": không ghi gì (US-S2E8-05-SC4). */
  public PreviewCheck previewCheck(UUID setId, Integer position, String answer) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    own(p.userId(), setId);
    ItemRow item =
        repo.items(setId).stream()
            .filter(i -> position != null && i.position() == position)
            .findFirst()
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    String reference = item.answerSnapshot();
    if (reference == null) {
      reference = repo.references(p.userId(), List.of(item.versionId())).get(item.versionId());
    }
    if (reference == null || !grader.gradable(reference)) {
      throw AppException.invalidField("items", MSG_NO_ANSWER.formatted(item.code()));
    }
    PracticeGrader.Graded g =
        grader.grade(
            reference, item.answerKind(), item.tolerance(), forms(item.acceptedForms()), answer);
    String hint = null;
    if ("wrong".equals(g.outcome())) {
      hint = repo.hint1(item.versionId()).orElse(null);
    }
    return new PreviewCheck(g.outcome(), g.canonical(), g.reason(), g.note(), hint);
  }

  // ------------------------------------------------------------------ PracticeSets (module khác)

  @Override
  public List<Summary> listForStudent(UUID studentId) {
    return repo.studentSets(studentId).stream()
        .map(s -> new Summary(s.id(), s.title(), s.status(), s.className(), s.itemCount()))
        .toList();
  }

  @Override
  public Optional<SetView> forStudent(UUID studentId, UUID setId) {
    return repo.studentSet(studentId, setId)
        .map(s ->
            new SetView(
                s.id(),
                s.title(),
                s.status(),
                s.className(),
                repo.items(setId).stream()
                    .filter(i -> i.assignmentId() != null)
                    .map(i ->
                        new Item(
                            i.assignmentId(),
                            i.position(),
                            i.statement(),
                            i.problemKind(),
                            i.maxAttempts(),
                            i.assignmentReleaseAt(),
                            i.releaseState()))
                    .toList()));
  }

  @Override
  public Optional<Grading> gradingForStudent(UUID studentId, UUID assignmentId) {
    return repo.gradingForStudent(studentId, assignmentId).map(this::grading);
  }

  @Override
  public Map<UUID, String> hintsForStudent(UUID studentId, UUID setId) {
    Map<UUID, String> out = new HashMap<>();
    for (GradingRow r : repo.gradingsOfSetForStudent(studentId, setId)) {
      if (r.hint1() != null) {
        out.put(r.assignmentId(), r.hint1());
      }
    }
    return out;
  }

  @Override
  public List<Grading> gradingsOfTeacher(UUID teacherId) {
    return repo.gradingsOfTeacher(teacherId).stream().map(this::grading).toList();
  }

  @Override
  public boolean addAcceptedForm(UUID teacherId, UUID assignmentId, String form) {
    return repo.addAcceptedForm(teacherId, assignmentId, form) > 0;
  }

  // ------------------------------------------------------------------ nội bộ

  private record Resolved(
      ProblemRow problem,
      String answerKind,
      BigDecimal tolerance,
      int maxAttempts,
      OffsetDateTime releaseAt) {}

  private SetRow own(UUID teacherId, UUID setId) {
    return repo.teacherSet(teacherId, setId)
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
  }

  private SetDetail detail(SetRow s) {
    List<ItemView> items =
        repo.items(s.id()).stream()
            .map(i ->
                new ItemView(
                    i.position(),
                    i.problemId(),
                    i.versionId(),
                    i.code(),
                    i.statement(),
                    i.problemKind(),
                    i.answerKind(),
                    i.tolerance(),
                    i.maxAttempts(),
                    i.assignmentId() != null ? i.assignmentReleaseAt() : i.releaseAt(),
                    i.assignmentId()))
            .toList();
    return new SetDetail(
        s.id(),
        s.title(),
        s.status(),
        s.classId(),
        s.className(),
        s.releaseAt(),
        s.assignedAt(),
        items);
  }

  private Grading grading(GradingRow r) {
    return new Grading(
        r.setId(),
        r.setTitle(),
        r.setStatus(),
        r.assignmentId(),
        r.position(),
        r.answer(),
        r.answerKind(),
        r.tolerance(),
        r.maxAttempts(),
        forms(r.acceptedForms()),
        r.hint1());
  }

  private List<String> forms(String json) {
    List<String> out = new ArrayList<>();
    if (json == null || json.isBlank()) {
      return out;
    }
    JsonNode n = mapper.readTree(json);
    if (n.isArray()) {
      n.forEach(x -> out.add(x.asString()));
    }
    return out;
  }

  /** BR-S2E8-26: tên 1–80 ký tự, 1–20 câu, lượt 1–5, sai số 0–10 tối đa 2 chữ số thập phân. */
  private List<Resolved> validate(UUID teacherId, SetInput in) {
    if (in == null || in.title() == null || in.title().isBlank()) {
      throw AppException.invalidField("title", MSG_TITLE_EMPTY);
    }
    String title = in.title().strip();
    if (title.codePointCount(0, title.length()) > 80) {
      throw AppException.invalidField("title", MSG_TITLE_LONG);
    }
    List<ItemInput> items = in.items() == null ? List.of() : in.items();
    if (items.isEmpty()) {
      throw AppException.invalidField("items", MSG_ITEMS_EMPTY);
    }
    if (items.size() > 20) {
      throw AppException.invalidField("items", MSG_ITEMS_MANY);
    }
    List<UUID> ids = new ArrayList<>();
    Set<UUID> seen = new HashSet<>();
    for (ItemInput i : items) {
      if (i == null || i.problemVersionId() == null) {
        throw AppException.invalidField("items", "Mỗi câu cần chọn một đề");
      }
      if (!seen.add(i.problemVersionId())) {
        throw AppException.invalidField("items", "Một đề chỉ chọn một lần trong bài");
      }
      ids.add(i.problemVersionId());
    }
    Map<UUID, ProblemRow> problems = new HashMap<>();
    repo.versions(ids).forEach(r -> problems.put(r.versionId(), r));
    Map<UUID, String> refs = repo.references(teacherId, ids);
    List<Resolved> out = new ArrayList<>();
    for (ItemInput i : items) {
      ProblemRow pr = problems.get(i.problemVersionId());
      if (pr == null) {
        throw AppException.invalidField("items", "Đề không có trong Kho đề");
      }
      if (!grader.gradable(refs.get(pr.versionId()))) {
        throw AppException.invalidField("items", MSG_NO_ANSWER.formatted(pr.code()));
      }
      int attempts = i.maxAttempts() == null ? 3 : i.maxAttempts();
      if (attempts < 1 || attempts > 5) {
        throw AppException.invalidField("maxAttempts", MSG_ATTEMPTS);
      }
      String kind = i.answerKind() == null ? "exact" : i.answerKind();
      if (!"exact".equals(kind) && !"approx".equals(kind)) {
        throw AppException.invalidField("answerKind", MSG_KIND);
      }
      BigDecimal tol = null;
      if ("approx".equals(kind)) {
        tol = i.tolerance() == null ? BigDecimal.ZERO : i.tolerance();
        if (tol.signum() < 0 || tol.compareTo(BigDecimal.TEN) > 0) {
          throw AppException.invalidField("tolerance", MSG_TOLERANCE);
        }
        if (tol.stripTrailingZeros().scale() > 2) {
          throw AppException.invalidField("tolerance", MSG_TOLERANCE_SCALE);
        }
      } // kiểu chính xác: sai số gửi lên bị bỏ qua (US-S2E8-05-FC3)
      out.add(new Resolved(pr, kind, tol, attempts, i.releaseAt()));
    }
    return out;
  }

  private void writeItems(UUID setId, List<Resolved> items) {
    int pos = 1;
    for (Resolved r : items) {
      repo.insertItem(
          Uuid7.next(),
          setId,
          pos++,
          r.problem().problemId(),
          r.problem().versionId(),
          r.answerKind(),
          r.tolerance(),
          r.maxAttempts(),
          r.releaseAt());
    }
  }
}
