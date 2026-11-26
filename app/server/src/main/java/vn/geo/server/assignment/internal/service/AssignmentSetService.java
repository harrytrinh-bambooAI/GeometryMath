package vn.geo.server.assignment.internal.service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.assignment.api.PracticeGrader;
import vn.geo.server.assignment.internal.repo.AssignmentRepository;
import vn.geo.server.assignment.internal.repo.AssignmentSetRepository;
import vn.geo.server.assignment.internal.repo.AssignmentSetRepository.ItemRow;
import vn.geo.server.assignment.internal.repo.AssignmentSetRepository.ProblemRow;
import vn.geo.server.assignment.internal.repo.AssignmentSetRepository.SetRow;
import vn.geo.server.assignment.internal.repo.AssignmentSetRepository.StudentSetRow;
import vn.geo.server.assignment.internal.repo.PracticeSetRepository;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;

/**
 * S3-E2 (T-261126-29): bộ bài chặng 3 — soạn (C3-03), lịch mở lời giải theo bộ hoặc từng bài, Bài
 * của em theo bộ (C3-08), đóng và lưu trữ — trên MỘT mô hình bộ bài với bài luyện S2-E8
 * (BR-S3E2-07, PO-66 G1). Hạn làm chỉ là nhãn (BR-S3E2-03). Dữ liệu cho học sinh không có đáp số;
 * lời giải chỉ qua cổng fn_get_solution.
 */
@Service
public class AssignmentSetService {

  public record ItemInput(UUID problemVersionId, OffsetDateTime releaseAt) {}

  public record SetInput(
      String title,
      UUID classId,
      OffsetDateTime dueAt,
      OffsetDateTime releaseAt,
      List<ItemInput> items) {}

  public record Candidate(
      UUID problemId,
      UUID problemVersionId,
      String code,
      String title,
      String statement,
      boolean needsReview,
      boolean gradable) {}

  public record ClassOption(UUID id, String name) {}

  public record Options(List<ClassOption> classes, List<Candidate> problems, Instant serverNow) {}

  public record ItemView(
      int position,
      UUID problemId,
      UUID problemVersionId,
      String code,
      String title,
      String statement,
      boolean needsReview,
      Instant ownReleaseAt,
      Instant releaseAt,
      String releaseState,
      UUID assignmentId) {}

  public record SetDetail(
      UUID id,
      String title,
      String status,
      UUID classId,
      String className,
      Instant dueAt,
      boolean overdue,
      Instant releaseAt,
      boolean releaseBeforeDue,
      int scheduleVersion,
      Instant assignedAt,
      Instant closedAt,
      Instant archivedAt,
      int needsReviewCount,
      List<ItemView> items,
      Instant serverNow) {}

  public record SetLine(
      UUID id,
      String title,
      String status,
      UUID classId,
      String className,
      Instant dueAt,
      boolean overdue,
      Instant releaseAt,
      int itemCount,
      int needsReviewCount) {}

  public record PreviewItem(
      int position, String statement, Instant releaseAt, String releaseState, String hint) {}

  public record Preview(
      UUID id,
      String title,
      String className,
      Instant dueAt,
      boolean overdue,
      List<PreviewItem> items) {}

  public record StudentSet(
      UUID id,
      String title,
      String status,
      Instant dueAt,
      boolean overdue,
      Instant releaseAt,
      String releaseState,
      int done,
      int total) {}

  public record StudentGroup(UUID classId, String className, List<StudentSet> sets) {}

  public record StudentHome(List<StudentGroup> groups, Instant serverNow) {}

  public record StudentItem(
      int position,
      UUID assignmentId,
      String statement,
      Instant releaseAt,
      String releaseState,
      String state) {}

  public record StudentSetDetail(
      UUID id,
      String title,
      String status,
      String className,
      Instant dueAt,
      boolean overdue,
      Instant releaseAt,
      String releaseState,
      int done,
      int total,
      List<StudentItem> items,
      Instant serverNow) {}

  public record SolutionView(
      int position, String releaseState, Instant releaseAt, String finalAnswer, JsonNode steps) {}

  /** Thông điệp MSG-S3-21…28 (stories_ac.md S3-E2, bảng "Thông điệp"). */
  static final String MSG_23 = "Bộ bài cần từ 1 đến 20 đề.";

  static final String MSG_24 = "Đề này đã có trong bộ.";
  static final String MSG_25 = "Chưa chọn lớp để giao.";
  static final String MSG_26 = "Lời giải đã mở, không đổi giờ được.";
  static final String MSG_REV = "Giờ mở vừa được thay đổi ở nơi khác. Hãy tải lại để xem giờ mới.";
  static final String MSG_TITLE = "Hãy đặt tên bộ bài (tối đa 80 ký tự).";
  static final String MSG_RELEASE_EMPTY = "Hãy chọn ngày và giờ mở lời giải.";
  static final String MSG_RELEASE_BAD = "Giờ mở lời giải không hợp lệ.";
  static final String MSG_RELEASE_PAST = "Giờ mở lời giải phải sau thời điểm hiện tại.";
  static final String MSG_NO_ANSWER =
      "Đề %s chưa có đáp số máy chấm được. Thêm đáp số ở Soát lời giải.";

  private final AssignmentSetRepository repo;
  private final PracticeSetRepository practice;
  private final AssignmentRepository assignments;
  private final PracticeGrader grader;
  private final JsonMapper mapper;

  public AssignmentSetService(
      AssignmentSetRepository repo,
      PracticeSetRepository practice,
      AssignmentRepository assignments,
      PracticeGrader grader,
      JsonMapper mapper) {
    this.repo = repo;
    this.practice = practice;
    this.assignments = assignments;
    this.grader = grader;
    this.mapper = mapper;
  }

  // ------------------------------------------------------------------ giáo viên: soạn

  public Options options() {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    List<ProblemRow> rows = repo.candidates();
    Map<UUID, String> refs =
        practice.references(p.userId(), rows.stream().map(ProblemRow::versionId).toList());
    List<Candidate> problems =
        rows.stream()
            .map(r ->
                new Candidate(
                    r.problemId(),
                    r.versionId(),
                    r.code(),
                    r.title(),
                    r.statement(),
                    r.needsReview(),
                    grader.gradable(refs.get(r.versionId()))))
            .toList();
    List<ClassOption> classes =
        repo.classesOf(p.userId()).stream().map(c -> new ClassOption(c.id(), c.name())).toList();
    return new Options(classes, problems, repo.now());
  }

  public List<SetLine> list(boolean archived) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    Instant now = repo.now();
    return repo.teacherSets(p.userId(), archived).stream()
        .map(s ->
            new SetLine(
                s.id(),
                s.title(),
                s.status(),
                s.classId(),
                s.className(),
                s.dueAt(),
                overdue(s.dueAt(), now),
                s.releaseAt(),
                s.itemCount(),
                s.needsReview()))
        .toList();
  }

  public SetDetail get(UUID setId) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    return detail(own(p.userId(), setId));
  }

  @Transactional
  public SetDetail create(SetInput in) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    String title = title(in);
    UUID classId = classOf(p.userId(), in.classId()); // lớp người khác: NOT_FOUND (US-S3E2-01-FC5)
    List<ProblemRow> items = resolve(in.items());
    UUID id = Uuid7.next();
    repo.insertSet(id, p.userId(), title, classId, in.dueAt(), in.releaseAt());
    writeItems(id, items, in.items());
    return detail(own(p.userId(), id));
  }

  @Transactional
  public SetDetail update(UUID setId, SetInput in) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    requireDraft(own(p.userId(), setId));
    String title = title(in);
    UUID classId = classOf(p.userId(), in.classId());
    List<ProblemRow> items = resolve(in.items());
    if (repo.updateDraft(p.userId(), setId, title, classId, in.dueAt(), in.releaseAt()) == 0) {
      throw new AppException(ErrorCode.CONFLICT, "Bộ đã giao không sửa được đề.");
    }
    repo.deleteDraftItems(setId);
    writeItems(setId, items, in.items());
    return detail(own(p.userId(), setId));
  }

  /** Thêm một đề vào bộ Nháp (nút "Thêm đề"): trùng → MSG-S3-24, đề thứ 21 → MSG-S3-23. */
  @Transactional
  public SetDetail addItem(UUID setId, UUID versionId) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    SetRow s = own(p.userId(), setId);
    requireDraft(s);
    ProblemRow pr = visible(versionId);
    List<ItemRow> items = repo.items(setId);
    if (items.stream().anyMatch(i -> i.problemId().equals(pr.problemId()))) {
      throw AppException.invalidField("items", MSG_24);
    }
    if (items.size() >= 20) {
      throw AppException.invalidField("items", MSG_23);
    }
    repo.insertItem(Uuid7.next(), setId, items.size() + 1, pr.problemId(), pr.versionId(), null);
    return detail(own(p.userId(), setId));
  }

  /**
   * Giao bộ (BR-S3E2-02, 06): idempotent theo khóa yêu cầu — bấm lặp khi bộ đã Đã giao trả lại bộ,
   * không giao lần hai. Chưa chọn lớp → MSG-S3-25; 0 hoặc trên 20 đề → MSG-S3-23.
   */
  @Transactional
  public SetDetail assign(UUID setId, String requestKey, boolean confirmPastRelease) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    SetRow s = own(p.userId(), setId);
    if (!"draft".equals(s.status())) {
      if (requestKey != null && !requestKey.isBlank() && "assigned".equals(s.status())) {
        return detail(s); // bấm lặp (US-S3E2-01-FC8)
      }
      throw new AppException(ErrorCode.CONFLICT, "Bộ này đã giao rồi.");
    }
    if (s.classId() == null) {
      throw AppException.invalidField("classId", MSG_25);
    }
    if (s.itemCount() < 1 || s.itemCount() > 20) {
      throw AppException.invalidField("items", MSG_23);
    }
    String status = practice.assign(p.userId(), setId, s.classId(), confirmPastRelease);
    switch (status) {
      case "OK" -> {}
      case "NOT_FOUND" -> throw new AppException(ErrorCode.NOT_FOUND);
      case "CONFLICT_ASSIGNED" -> {
        if (requestKey != null && !requestKey.isBlank()) {
          return detail(own(p.userId(), setId));
        }
        throw new AppException(ErrorCode.CONFLICT, "Bộ này đã giao rồi.");
      }
      case "EMPTY" -> throw AppException.invalidField("items", MSG_23);
      case "PAST_RELEASE" ->
          throw new AppException(
              ErrorCode.CONFLICT, "Giờ mở đã qua, lời giải mở ngay cho học sinh. Vẫn giao?");
      case "CONFLICT_DUPLICATE" ->
          throw new AppException(ErrorCode.CONFLICT, "Có đề trong bộ đã được giao cho lớp này.");
      default ->
          throw AppException.invalidField(
              "items", "Có đề chưa có đáp số máy chấm được. Thêm đáp số ở Soát lời giải.");
    }
    return detail(own(p.userId(), setId));
  }

  /**
   * Đặt / đổi giờ mở lời giải CHUNG của bộ (BR-S3E2-10, 14, 15). Bộ Nháp: ghi thẳng. Bộ đã giao:
   * từng câu theo giờ của bộ đổi qua fn_set_release (một cổng giờ mở); câu nào đã mở → CONFLICT
   * MSG-S3-26; version khác phiên bản lịch hiện hành → CONFLICT (lần đến sau thua).
   */
  @Transactional
  public SetDetail schedule(UUID setId, String rawReleaseAt, Boolean setFlag, Integer version) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    SetRow s = own(p.userId(), setId);
    OffsetDateTime at = parseRelease(rawReleaseAt, Boolean.TRUE.equals(setFlag));
    if ("draft".equals(s.status())) {
      repo.setReleaseOfSet(p.userId(), setId, at);
      return detail(own(p.userId(), setId));
    }
    if ("archived".equals(s.status())) {
      throw new AppException(ErrorCode.CONFLICT, "Bộ đã lưu trữ.");
    }
    if (at == null) {
      throw AppException.invalidField("releaseAt", MSG_RELEASE_EMPTY);
    }
    List<ItemRow> followers =
        repo.items(setId).stream()
            .filter(i -> i.assignmentId() != null && i.ownReleaseAt() == null)
            .toList();
    Instant now = repo.now();
    for (ItemRow i : followers) {
      if (i.assignmentReleaseAt() != null && !i.assignmentReleaseAt().isAfter(now)) {
        throw new AppException(ErrorCode.CONFLICT, MSG_26); // US-S3E2-02-FC5
      }
    }
    if (version != null && version != scheduleVersion(repo.items(setId))) {
      throw new AppException(ErrorCode.CONFLICT, MSG_REV); // US-S3E2-02-FC6
    }
    for (ItemRow i : followers) {
      AssignmentRepository.FnResult r =
          assignments.setRelease(p.userId(), i.assignmentId(), false, at, i.releaseRev());
      switch (r.status()) {
        case "OK" -> {}
        case "CONFLICT_ALREADY_OPEN" -> throw new AppException(ErrorCode.CONFLICT, MSG_26);
        case "CONFLICT_REV" -> throw new AppException(ErrorCode.CONFLICT, MSG_REV);
        case "VALIDATION_PAST" -> throw AppException.invalidField("releaseAt", MSG_RELEASE_PAST);
        default -> throw new AppException(ErrorCode.NOT_FOUND);
      }
    }
    repo.setReleaseOfSet(p.userId(), setId, at);
    return detail(own(p.userId(), setId));
  }

  /** Đóng bộ Đã giao (BR-S3E2-30, 33): một chiều; lặp lại trả cùng kết quả; Nháp → CONFLICT. */
  @Transactional
  public SetDetail close(UUID setId) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    SetRow s = own(p.userId(), setId);
    if ("closed".equals(s.status())) {
      return detail(s);
    }
    if (!"assigned".equals(s.status()) || repo.close(p.userId(), setId) == 0) {
      throw new AppException(ErrorCode.CONFLICT, "Chỉ bộ Đã giao mới đóng được.");
    }
    return detail(own(p.userId(), setId));
  }

  /** Lưu trữ bộ Đã đóng (BR-S3E2-32, 33); lặp lại trả cùng kết quả. */
  @Transactional
  public SetDetail archive(UUID setId) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    SetRow s = own(p.userId(), setId);
    if ("archived".equals(s.status())) {
      return detail(s);
    }
    if (!"closed".equals(s.status()) || repo.archive(p.userId(), setId) == 0) {
      throw new AppException(ErrorCode.CONFLICT, "Chỉ bộ Đã đóng mới lưu trữ được.");
    }
    return detail(own(p.userId(), setId));
  }

  /**
   * "Xem trước như học sinh" (BR-S3E2-05): dựng đúng nội dung màn học sinh, không ghi tiến độ,
   * không tạo thông báo; chấm thử dùng lại /practice-sets/{id}/preview/check (không ghi Attempt).
   */
  public Preview preview(UUID setId) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    SetRow s = own(p.userId(), setId);
    Instant now = repo.now();
    List<PreviewItem> items = new ArrayList<>();
    for (ItemRow i : repo.items(setId)) {
      Instant rel = effectiveRelease(i, s);
      items.add(new PreviewItem(i.position(), i.statement(), rel, state(rel, now), i.hint1()));
    }
    return new Preview(s.id(), s.title(), s.className(), s.dueAt(), overdue(s.dueAt(), now), items);
  }

  // ------------------------------------------------------------------ học sinh

  /** Bài của em theo lớp rồi theo bộ (BR-S3E2-20, 21, 24): chỉ tiến độ của chính em. */
  public StudentHome studentHome() {
    AuthPrincipal p = Roles.require(Role.STUDENT);
    Instant now = repo.now();
    Map<UUID, StudentGroup> groups = new LinkedHashMap<>();
    for (StudentSetRow r : repo.studentSets(p.userId())) {
      StudentGroup g =
          groups.computeIfAbsent(
              r.classId(), k -> new StudentGroup(r.classId(), r.className(), new ArrayList<>()));
      g.sets().add(studentSet(r, now));
    }
    return new StudentHome(List.copyOf(groups.values()), now);
  }

  public StudentSetDetail studentSet(UUID setId) {
    AuthPrincipal p = Roles.require(Role.STUDENT);
    StudentSetRow r =
        repo.studentSet(p.userId(), setId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND)); // Nháp, Chờ duyệt: FC1, FC2
    Instant now = repo.now();
    StudentSet v = studentSet(r, now);
    List<StudentItem> items =
        repo.studentItems(p.userId(), setId).stream()
            .map(i ->
                new StudentItem(
                    i.position(),
                    i.assignmentId(),
                    i.statement(),
                    i.releaseAt(),
                    i.releaseState(),
                    i.state()))
            .toList();
    return new StudentSetDetail(
        v.id(),
        v.title(),
        v.status(),
        r.className(),
        v.dueAt(),
        v.overdue(),
        v.releaseAt(),
        v.releaseState(),
        v.done(),
        v.total(),
        items,
        now);
  }

  /**
   * Lời giải của một câu theo giờ MÁY CHỦ (BR-S3E2-11): Chưa đặt giờ / Sắp mở trả trạng thái, không
   * có đáp số hay bước giải; Đã mở mới qua cổng fn_get_solution.
   */
  public SolutionView studentSolution(UUID setId, int position) {
    AuthPrincipal p = Roles.require(Role.STUDENT);
    repo.studentSet(p.userId(), setId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    var item =
        repo.studentItems(p.userId(), setId).stream()
            .filter(i -> i.position() == position)
            .findFirst()
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    AssignmentRepository.Solution s = assignments.solution(p.userId(), item.assignmentId());
    if ("OK".equals(s.status())) {
      return new SolutionView(
          position, "open", item.releaseAt(), s.finalAnswer(), mapper.readTree(s.steps()));
    }
    if ("LOCKED".equals(s.status())) {
      String st = item.releaseAt() == null ? "unset" : "upcoming";
      return new SolutionView(position, st, item.releaseAt(), null, null);
    }
    throw new AppException(ErrorCode.NOT_FOUND);
  }

  // ------------------------------------------------------------------ nội bộ

  private SetRow own(UUID teacherId, UUID setId) {
    return repo.teacherSet(teacherId, setId)
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND)); // người khác: NOT_FOUND
  }

  private static void requireDraft(SetRow s) {
    if (!"draft".equals(s.status())) {
      throw new AppException(ErrorCode.CONFLICT, "Bộ đã giao không sửa được đề.");
    }
  }

  private static String title(SetInput in) {
    if (in == null || in.title() == null || in.title().isBlank()) {
      throw AppException.invalidField("title", MSG_TITLE);
    }
    String t = in.title().strip();
    if (t.codePointCount(0, t.length()) > 80) {
      throw AppException.invalidField("title", MSG_TITLE);
    }
    return t;
  }

  private UUID classOf(UUID teacherId, UUID classId) {
    if (classId == null) {
      return null; // Nháp chưa chọn lớp (US-S3E2-01-FC4)
    }
    return repo.ownClass(teacherId, classId)
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND))
        .id();
  }

  private ProblemRow visible(UUID versionId) {
    if (versionId == null) {
      throw AppException.invalidField("items", "Mỗi đề cần chọn từ Kho đề.");
    }
    // đề Của tôi của giáo viên khác, đề bộ trường không thấy: như không tồn tại (US-S3E2-01-FC6)
    return repo.version(versionId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
  }

  private List<ProblemRow> resolve(List<ItemInput> in) {
    List<ItemInput> items = in == null ? List.of() : in;
    if (items.size() > 20) {
      throw AppException.invalidField("items", MSG_23);
    }
    List<ProblemRow> out = new ArrayList<>();
    Set<UUID> seen = new HashSet<>();
    for (ItemInput i : items) {
      ProblemRow pr = visible(i == null ? null : i.problemVersionId());
      if (!seen.add(pr.problemId())) {
        throw AppException.invalidField("items", MSG_24);
      }
      out.add(pr);
    }
    return out;
  }

  private void writeItems(UUID setId, List<ProblemRow> items, List<ItemInput> in) {
    for (int k = 0; k < items.size(); k++) {
      ProblemRow pr = items.get(k);
      repo.insertItem(
          Uuid7.next(), setId, k + 1, pr.problemId(), pr.versionId(), in.get(k).releaseAt());
    }
  }

  private static OffsetDateTime parseRelease(String raw, boolean setFlag) {
    if (raw == null || raw.isBlank()) {
      if (setFlag) {
        throw AppException.invalidField("releaseAt", MSG_RELEASE_EMPTY);
      }
      return null;
    }
    try {
      return OffsetDateTime.parse(raw.strip());
    } catch (DateTimeParseException e) {
      throw AppException.invalidField("releaseAt", MSG_RELEASE_BAD); // US-S3E2-02-FC9
    }
  }

  private static int scheduleVersion(List<ItemRow> items) {
    return items.stream().mapToInt(ItemRow::releaseRev).sum();
  }

  private static Instant effectiveRelease(ItemRow i, SetRow s) {
    if (i.assignmentId() != null) {
      return i.assignmentReleaseAt();
    }
    return i.ownReleaseAt() != null ? i.ownReleaseAt() : s.releaseAt();
  }

  private static String state(Instant rel, Instant now) {
    if (rel == null) {
      return "unset";
    }
    return rel.isAfter(now) ? "upcoming" : "open";
  }

  private static boolean overdue(Instant due, Instant now) {
    return due != null && now.isAfter(due);
  }

  private SetDetail detail(SetRow s) {
    Instant now = repo.now();
    List<ItemRow> rows = repo.items(s.id());
    List<ItemView> items = new ArrayList<>();
    boolean early = false;
    for (ItemRow i : rows) {
      Instant rel = effectiveRelease(i, s);
      items.add(
          new ItemView(
              i.position(),
              i.problemId(),
              i.versionId(),
              i.code(),
              i.title(),
              i.statement(),
              i.needsReview(),
              i.ownReleaseAt(),
              rel,
              state(rel, now),
              i.assignmentId()));
      early |= beforeDue(rel, s.dueAt());
    }
    if (rows.isEmpty()) {
      early = beforeDue(s.releaseAt(), s.dueAt());
    }
    return new SetDetail(
        s.id(),
        s.title(),
        s.status(),
        s.classId(),
        s.className(),
        s.dueAt(),
        overdue(s.dueAt(), now),
        s.releaseAt(),
        early,
        scheduleVersion(rows),
        s.assignedAt(),
        s.closedAt(),
        s.archivedAt(),
        s.needsReview(),
        items,
        now);
  }

  /** Giờ mở sớm hơn hạn từ 1 phút trở lên (BR-S3E2-13, QA-S3E2-03). */
  static boolean beforeDue(Instant release, Instant due) {
    return release != null && due != null && !release.plusSeconds(60).isAfter(due);
  }

  private StudentSet studentSet(StudentSetRow r, Instant now) {
    return new StudentSet(
        r.id(),
        r.title(),
        r.status(),
        r.dueAt(),
        overdue(r.dueAt(), now),
        r.releaseAt(),
        state(r.releaseAt(), now),
        r.done(),
        r.total());
  }
}
