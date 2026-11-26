package vn.geo.server.tracking.internal.practice.service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;
import vn.geo.server.assignment.api.PracticeGrader;
import vn.geo.server.assignment.api.PracticeSets;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.tracking.internal.practice.repo.PracticeRepository;
import vn.geo.server.tracking.internal.practice.repo.PracticeRepository.AttemptRow;
import vn.geo.server.tracking.internal.practice.repo.PracticeRepository.Recorded;
import vn.geo.server.tracking.internal.practice.repo.PracticeRepository.ReportRow;

/**
 * Làm bài luyện (US-S2E8-03), kết quả (US-S2E8-04) và báo "Em nghĩ đáp số này đúng" (US-S2E8-05).
 *
 * <p>Chấm ở máy chủ; đáp số, sai số, kiểu đáp số không bao giờ có trong dữ liệu trả về cho học sinh
 * (BR-S2E8-12, 24). Chỉ kết quả Sai trừ lượt (BR-S2E8-07). Phản hồi Sai chỉ nói chưa đúng và số
 * lượt còn, không nói hướng hay độ lệch. Kết quả không có điểm, phần trăm hay xếp hạng
 * (BR-S2E8-21).
 */
@Service
public class PracticeService {

  /** Một bộ trong danh sách "Bài luyện". */
  public record SetLine(UUID id, String title, String status, String className, int itemCount) {}

  /**
   * Một câu trên màn làm bài.
   *
   * @param state todo | doing | correct | review ("xem lại cùng thầy cô")
   * @param hint gợi ý mức 1, chỉ có sau lần Sai đầu tiên
   * @param lastAnswer đáp số em nộp gần nhất (để hiện lại khi vào lại)
   */
  public record PlayItem(
      UUID assignmentId,
      int position,
      String statement,
      String problemKind,
      int maxAttempts,
      int attemptsLeft,
      String state,
      String hint,
      String lastAnswer,
      String canonical,
      boolean canReport,
      boolean reported) {}

  public record PlayView(
      UUID id, String title, String status, String className, int current, List<PlayItem> items) {}

  /** Phản hồi một lần "Kiểm tra". outcome: correct | wrong | syntax. */
  public record AttemptView(
      String outcome,
      String message,
      String reason,
      String canonical,
      String note,
      String hint,
      int attemptsLeft,
      int maxAttempts,
      String state,
      boolean canReport) {}

  /**
   * Một dòng của màn kết quả.
   *
   * @param solutionState locked | unset | open
   */
  public record ResultItem(
      int position,
      UUID assignmentId,
      String state,
      String label,
      String solutionState,
      String solutionLabel,
      Instant releaseAt) {}

  /** Kết quả: chỉ hai số câu đúng / câu cần xem lại (và số câu chưa làm); không có số tổng. */
  public record ResultView(
      UUID id,
      String title,
      String status,
      int correct,
      int review,
      int remaining,
      String summary,
      String pending,
      String note,
      List<ResultItem> items) {}

  public record ReportView(
      UUID id,
      String studentName,
      String studentCode,
      String setTitle,
      int position,
      String submitted,
      String note,
      String canonical,
      String status,
      String statusLabel,
      Instant createdAt) {}

  static final String MSG_SENT = "Đã gửi cho thầy cô";
  static final String MSG_EMPTY = "Em hãy nhập đáp số.";
  static final DateTimeFormatter VN_TIME =
      DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy").withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

  private final PracticeRepository repo;
  private final PracticeSets sets;
  private final PracticeGrader grader;
  private final JsonMapper mapper;

  public PracticeService(
      PracticeRepository repo, PracticeSets sets, PracticeGrader grader, JsonMapper mapper) {
    this.repo = repo;
    this.sets = sets;
    this.grader = grader;
    this.mapper = mapper;
  }

  // ------------------------------------------------------------------ học sinh

  public List<SetLine> mySets() {
    AuthPrincipal p = Roles.require(Role.STUDENT);
    return sets.listForStudent(p.userId()).stream()
        .map(s -> new SetLine(s.id(), s.title(), s.status(), s.className(), s.itemCount()))
        .toList();
  }

  /** Mở bài: tiến độ lưu ở máy chủ nên vào lại đúng câu, đúng số lượt còn (BR-S2E8-18). */
  public PlayView open(UUID setId) {
    AuthPrincipal p = Roles.require(Role.STUDENT);
    PracticeSets.SetView set =
        sets.forStudent(p.userId(), setId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    List<UUID> ids = set.items().stream().map(PracticeSets.Item::assignmentId).toList();
    Map<UUID, Progress> progress = progress(p.userId(), ids);
    Map<UUID, String> hints = sets.hintsForStudent(p.userId(), setId);
    Map<UUID, ReportRow> reports = new HashMap<>();
    repo.reportsOfUser(p.userId(), ids).forEach(r -> reports.put(r.assignmentId(), r));
    List<PlayItem> items = new ArrayList<>();
    int current = 0;
    for (PracticeSets.Item i : set.items()) {
      Progress g = progress.getOrDefault(i.assignmentId(), Progress.NONE);
      String state = g.state(i.maxAttempts());
      if (current == 0 && !"correct".equals(state) && !"review".equals(state)) {
        current = i.position();
      }
      boolean reported = reports.containsKey(i.assignmentId());
      items.add(
          new PlayItem(
              i.assignmentId(),
              i.position(),
              i.statement(),
              i.problemKind(),
              i.maxAttempts(),
              Math.max(0, i.maxAttempts() - g.wrong()),
              state,
              g.wrong() > 0 ? hints.get(i.assignmentId()) : null,
              g.lastAnswer(),
              g.correct() ? g.canonical() : null,
              g.wrong() > 0 && !g.correct() && !reported,
              reported));
    }
    if (current == 0) {
      current = set.items().size();
    }
    return new PlayView(set.id(), set.title(), set.status(), set.className(), current, items);
  }

  /**
   * Một lần "Kiểm tra". Thân yêu cầu chỉ dùng đáp số nộp và mã chống lặp; mọi trường khác (tol,
   * type…) bị bỏ qua (US-S2E8-02-FC18).
   */
  public AttemptView submit(UUID assignmentId, String answer, String requestKey) {
    AuthPrincipal p = Roles.require(Role.STUDENT);
    if (requestKey == null || requestKey.isBlank() || requestKey.length() > 64) {
      throw AppException.invalidField("requestKey", "Thiếu mã chống lặp của lần gửi.");
    }
    PracticeSets.Grading g =
        sets.gradingForStudent(p.userId(), assignmentId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    // gửi lặp cùng mã: trả đúng phản hồi lần đầu, không chấm lại, không trừ lượt (BR-S2E8-14)
    var replay = repo.attemptByKey(p.userId(), assignmentId, requestKey);
    if (replay.isPresent()) {
      AttemptRow a = replay.get();
      return view(g, a.outcome(), a.detail(), a.wrongAfter());
    }
    if ("closed".equals(g.setStatus())) {
      throw new AppException(ErrorCode.CONFLICT, "Bài đã đóng, em chỉ xem được kết quả.");
    }
    Progress before =
        progress(p.userId(), List.of(assignmentId)).getOrDefault(assignmentId, Progress.NONE);
    conflictIfDone(before, g.maxAttempts()); // không chấm khi đã Đúng / hết lượt (BR-S2E8-13)
    String submitted = answer == null ? "" : answer;
    PracticeGrader.Graded graded =
        grader.grade(g.answer(), g.answerKind(), g.tolerance(), g.acceptedForms(), submitted);
    ObjectNode detail = mapper.createObjectNode();
    if (graded.canonical() != null) {
      detail.put("canonical", graded.canonical());
    }
    if (graded.reason() != null) {
      detail.put("reason", graded.reason());
    }
    if (graded.note() != null) {
      detail.put("note", graded.note());
    }
    Recorded r =
        repo.record(
            p.userId(),
            assignmentId,
            requestKey,
            submitted,
            graded.outcome(),
            detail.toString(),
            g.maxAttempts(),
            "student");
    return switch (r.status()) {
      case "OK", "DUPLICATE" -> view(g, r.outcome(), r.detail(), r.wrongAfter());
      case "CONFLICT_CORRECT" -> throw new AppException(ErrorCode.CONFLICT, "Câu này đã đúng");
      default -> throw new AppException(ErrorCode.CONFLICT, "Câu này đã hết lượt thử.");
    };
  }

  /**
   * Kết quả của CHÍNH em (BR-S2E8-25); lời giải khóa tới giờ mở kể cả khi hết lượt (BR-S2E8-23).
   */
  public ResultView result(UUID setId) {
    AuthPrincipal p = Roles.require(Role.STUDENT);
    PracticeSets.SetView set =
        sets.forStudent(p.userId(), setId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    Map<UUID, Progress> progress =
        progress(p.userId(), set.items().stream().map(PracticeSets.Item::assignmentId).toList());
    int correct = 0;
    int review = 0;
    int remaining = 0;
    List<ResultItem> items = new ArrayList<>();
    for (PracticeSets.Item i : set.items()) {
      String state = progress.getOrDefault(i.assignmentId(), Progress.NONE).state(i.maxAttempts());
      String label;
      switch (state) {
        case "correct" -> {
          correct++;
          label = "Đúng";
        }
        case "review" -> {
          review++;
          label = "Cần xem lại cùng thầy cô";
        }
        default -> {
          remaining++;
          label = "Chưa làm xong";
        }
      }
      String solutionState =
          switch (i.releaseState()) {
            case "open" -> "open";
            case "unset" -> "unset";
            default -> "locked";
          };
      String solutionLabel =
          switch (solutionState) {
            case "open" -> "Lời giải đã mở";
            case "unset" -> "Thầy cô chưa đặt giờ mở";
            default -> "Lời giải mở lúc " + VN_TIME.format(i.releaseAt());
          };
      items.add(
          new ResultItem(
              i.position(),
              i.assignmentId(),
              state,
              label,
              solutionState,
              solutionLabel,
              i.releaseAt()));
    }
    String summary = correct + " câu đúng · " + review + " câu cần xem lại";
    String pending = remaining > 0 ? "Chưa làm xong: còn " + remaining + " câu" : null;
    String note =
        correct == 0 && review == 0
            ? "Em chưa làm câu nào"
            : review > 0 ? "Em hỏi thầy cô nhé" : null;
    return new ResultView(
        set.id(),
        set.title(),
        set.status(),
        correct,
        review,
        remaining,
        summary,
        pending,
        note,
        items);
  }

  /** "Em nghĩ đáp số này đúng": chỉ khi câu đã bị chấm Sai; mỗi em một báo mỗi câu (BR-S2E8-30). */
  public Map<String, String> report(UUID assignmentId, String note) {
    AuthPrincipal p = Roles.require(Role.STUDENT);
    PracticeSets.Grading g =
        sets.gradingForStudent(p.userId(), assignmentId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    String n = note == null || note.isBlank() ? null : note.strip();
    if (n != null && n.codePointCount(0, n.length()) > 200) {
      throw AppException.invalidField("note", "Ghi chú tối đa 200 ký tự");
    }
    Progress pr =
        progress(p.userId(), List.of(assignmentId)).getOrDefault(assignmentId, Progress.NONE);
    if (pr.wrong() == 0 || pr.lastWrong() == null) {
      throw new AppException(ErrorCode.CONFLICT, "Em chỉ báo được khi câu đã bị chấm Sai.");
    }
    if (pr.correct()) {
      throw new AppException(ErrorCode.CONFLICT, "Câu này đã đúng");
    }
    if (repo.insertReport(Uuid7.next(), p.userId(), g.assignmentId(), pr.lastWrong(), n) == 0) {
      throw new AppException(ErrorCode.CONFLICT, "Em đã báo câu này");
    }
    return Map.of("message", MSG_SENT); // không đáp số, không dạng chuẩn, không đúng sai (FC9)
  }

  // ------------------------------------------------------------------ giáo viên

  /** "Báo chấm sai" của các bài do giáo viên này giao (BR-S2E8-31); giáo viên khác không thấy. */
  public List<ReportView> reports() {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    Map<UUID, PracticeSets.Grading> mine = gradings(p.userId());
    return repo.reportsFor(new ArrayList<>(mine.keySet())).stream()
        .map(r -> reportView(r, mine.get(r.assignmentId())))
        .toList();
  }

  /** decision: confirm ("Xác nhận đúng") | keep ("Giữ nguyên"). */
  public ReportView decide(UUID reportId, String decision) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    if (!"confirm".equals(decision) && !"keep".equals(decision)) {
      throw AppException.invalidField("decision", "Chọn Xác nhận đúng hoặc Giữ nguyên.");
    }
    Map<UUID, PracticeSets.Grading> mine = gradings(p.userId());
    ReportRow r =
        repo.report(reportId)
            .filter(x -> mine.containsKey(x.assignmentId()))
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    PracticeSets.Grading g = mine.get(r.assignmentId());
    boolean confirm = "confirm".equals(decision);
    if (repo.decide(reportId, confirm ? "confirmed" : "kept", p.userId()) == 0) {
      throw new AppException(ErrorCode.CONFLICT, "Báo này đã được xử lý.");
    }
    if (confirm) {
      // câu của em thành Đúng; dạng viết đó thêm vào danh sách chấp nhận của câu (QA-S2E8-09)
      sets.addAcceptedForm(p.userId(), r.assignmentId(), r.submitted());
      ObjectNode detail = mapper.createObjectNode();
      detail.put("canonical", g.answer());
      repo.record(
          r.userId(),
          r.assignmentId(),
          "report:" + r.id(),
          r.submitted(),
          "correct",
          detail.toString(),
          g.maxAttempts(),
          "teacher_confirm");
    }
    return reportView(repo.report(reportId).orElse(r), g);
  }

  /** M-S2-07: số báo đã "Xác nhận đúng" trên bài của giáo viên. */
  public Map<String, Long> metrics() {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    return Map.of("confirmed", repo.confirmedCount(new ArrayList<>(gradings(p.userId()).keySet())));
  }

  // ------------------------------------------------------------------ nội bộ

  /** Tiến độ một câu của một em, suy từ các lượt thử. */
  private record Progress(
      int wrong,
      boolean correct,
      int tried,
      String lastAnswer,
      String lastWrong,
      String canonical) {
    static final Progress NONE = new Progress(0, false, 0, null, null, null);

    String state(int max) {
      if (correct) {
        return "correct";
      }
      if (wrong >= max) {
        return "review";
      }
      return tried > 0 ? "doing" : "todo";
    }
  }

  private Map<UUID, Progress> progress(UUID userId, List<UUID> assignmentIds) {
    Map<UUID, Progress> out = new HashMap<>();
    for (AttemptRow a : repo.attempts(userId, assignmentIds)) {
      Progress g = out.getOrDefault(a.assignmentId(), Progress.NONE);
      boolean wrong = "wrong".equals(a.outcome());
      boolean correct = "correct".equals(a.outcome());
      out.put(
          a.assignmentId(),
          new Progress(
              g.wrong() + (wrong ? 1 : 0),
              g.correct() || correct,
              g.tried() + 1,
              a.submitted(),
              wrong ? a.submitted() : g.lastWrong(),
              correct ? text(a.detail(), "canonical") : g.canonical()));
    }
    return out;
  }

  private static void conflictIfDone(Progress g, int max) {
    if (g.correct()) {
      throw new AppException(ErrorCode.CONFLICT, "Câu này đã đúng");
    }
    if (g.wrong() >= max) {
      throw new AppException(ErrorCode.CONFLICT, "Câu này đã hết lượt thử.");
    }
  }

  private AttemptView view(
      PracticeSets.Grading g, String outcome, String detailJson, int wrongAfter) {
    String canonical = text(detailJson, "canonical");
    String reason = text(detailJson, "reason");
    String note = text(detailJson, "note");
    int left = Math.max(0, g.maxAttempts() - wrongAfter);
    return switch (outcome) {
      case "correct" ->
          new AttemptView(
              outcome,
              canonical == null ? "Đúng rồi!" : "Đúng rồi! Dạng chuẩn: " + canonical,
              null,
              canonical,
              null,
              null,
              left,
              g.maxAttempts(),
              "correct",
              false);
      case "wrong" ->
          new AttemptView(
              outcome,
              left > 0
                  ? "Chưa đúng. Còn " + left + " lượt."
                  : "Chưa đúng. Em xem lại cùng thầy cô nhé.",
              null,
              null,
              note,
              g.hint1(),
              left,
              g.maxAttempts(),
              left > 0 ? "doing" : "review",
              true);
      default ->
          new AttemptView(
              "syntax",
              reason == null || MSG_EMPTY.equals(reason)
                  ? MSG_EMPTY
                  : "Đáp số chưa viết đúng: " + lowerFirst(reason) + ". Lượt thử chưa bị trừ.",
              reason == null ? MSG_EMPTY : reason,
              null,
              null,
              null,
              left,
              g.maxAttempts(),
              "doing",
              wrongAfter > 0);
    };
  }

  private Map<UUID, PracticeSets.Grading> gradings(UUID teacherId) {
    Map<UUID, PracticeSets.Grading> out = new HashMap<>();
    sets.gradingsOfTeacher(teacherId).forEach(g -> out.put(g.assignmentId(), g));
    return out;
  }

  private static ReportView reportView(ReportRow r, PracticeSets.Grading g) {
    String label =
        switch (r.status()) {
          case "confirmed" -> "Đã xác nhận đúng";
          case "kept" -> "Đã xem, giữ nguyên";
          default -> "Chờ xem";
        };
    return new ReportView(
        r.id(),
        r.studentName(),
        r.studentCode(),
        g.setTitle(),
        g.position(),
        r.submitted(),
        r.note(),
        g.answer(),
        r.status(),
        label,
        r.createdAt());
  }

  private String text(String json, String field) {
    if (json == null || json.isBlank()) {
      return null;
    }
    JsonNode n = mapper.readTree(json).get(field);
    return n == null || n.isNull() ? null : n.asString();
  }

  private static String lowerFirst(String s) {
    return s.isEmpty()
        ? s
        : s.substring(0, 1).toLowerCase(java.util.Locale.forLanguageTag("vi")) + s.substring(1);
  }
}
