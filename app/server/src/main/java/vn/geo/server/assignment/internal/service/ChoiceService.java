package vn.geo.server.assignment.internal.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import vn.geo.server.assignment.api.PracticeSets;
import vn.geo.server.assignment.internal.repo.ChoiceRepository;
import vn.geo.server.assignment.internal.repo.ChoiceRepository.Attempt;
import vn.geo.server.assignment.internal.repo.ChoiceRepository.ItemChoice;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.RequestContext;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;

/**
 * Câu trắc nghiệm trong bài luyện (US-S2E9-03). Cấu hình trắc nghiệm là lớp phủ lên một câu của bộ
 * bài S2-E8 ({@code assignment_item_choice}, khóa bộ + đề): giáo viên soạn 2…5 lựa chọn, đúng một
 * lựa chọn đúng, số lượt 1…(số lựa chọn − 1), mặc định 1 (BR-S2E9-11, 12). Học sinh chọn một lựa
 * chọn rồi "Kiểm tra"; lượt thử ghi qua cùng đường với câu điền ({@code
 * fn_record_practice_attempt}) nên tiến độ, số lượt và màn kết quả đếm chung hai kiểu câu
 * (BR-S2E8-21). Dữ liệu trả cho học sinh không bao giờ có lựa chọn đúng (BR-S2E9-14).
 */
@Service
public class ChoiceService {

  public record OptionInput(String text, Boolean correct) {}

  public record ChoiceInput(List<OptionInput> options, Integer maxAttempts) {}

  /** Phía giáo viên: có cờ đúng. */
  public record TeacherOption(UUID id, String text, boolean correct) {}

  public record TeacherChoice(int position, int maxAttempts, List<TeacherOption> options) {}

  /** Phía học sinh: KHÔNG có cờ đúng hay vị trí đúng. */
  public record StudentOption(UUID id, String text) {}

  /**
   * @param dimmed lựa chọn em đã chọn và bị chấm Sai (làm mờ, không chọn lại)
   * @param chosen lựa chọn em đã chọn Đúng (null nếu chưa)
   */
  public record StudentChoice(
      UUID assignmentId,
      int position,
      List<StudentOption> options,
      List<UUID> dimmed,
      UUID chosen) {}

  /** Phản hồi một lần "Kiểm tra" câu trắc nghiệm. outcome: correct | wrong. */
  public record AttemptView(
      String outcome,
      String message,
      int attemptsLeft,
      int maxAttempts,
      String state,
      List<UUID> dimmed,
      boolean canReport) {}

  static final String MSG_COUNT = "Cần từ 2 đến 5 lựa chọn";
  static final String MSG_EMPTY = "Lựa chọn đang trống";
  static final String MSG_LONG = "Lựa chọn tối đa 100 ký tự";
  static final String MSG_NO_CORRECT = "Hãy chọn một đáp án đúng";
  static final String MSG_MANY_CORRECT = "Chỉ một đáp án đúng";
  static final String MSG_DUPLICATE = "Hai lựa chọn giống nhau";
  static final String MSG_ASSIGNED = "Bài đã giao không sửa được";

  private final ChoiceRepository repo;
  private final PracticeSets sets;
  private final JsonMapper mapper;

  public ChoiceService(ChoiceRepository repo, PracticeSets sets, JsonMapper mapper) {
    this.repo = repo;
    this.sets = sets;
    this.mapper = mapper;
  }

  // ------------------------------------------------------------------ giáo viên

  public List<TeacherChoice> ofSet(UUID setId) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    repo.ownSetStatus(p.userId(), setId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    List<TeacherChoice> out = new ArrayList<>();
    for (ItemChoice i : repo.itemsOfSet(setId)) {
      if (i.options() != null) {
        out.add(teacherChoice(i));
      }
    }
    return out;
  }

  /** Đặt câu ở vị trí {@code position} của bộ Nháp thành câu trắc nghiệm. */
  @Transactional
  public TeacherChoice save(UUID setId, int position, ChoiceInput in) {
    ItemChoice item = draftItem(setId, position);
    List<OptionInput> options = in == null || in.options() == null ? List.of() : in.options();
    if (options.size() < 2 || options.size() > 5) {
      throw AppException.invalidField("options", MSG_COUNT);
    }
    Set<String> seen = new HashSet<>();
    int correct = 0;
    for (OptionInput o : options) {
      String text = o == null || o.text() == null ? "" : o.text().strip();
      if (text.isEmpty()) {
        throw AppException.invalidField("options", MSG_EMPTY);
      }
      if (text.codePointCount(0, text.length()) > 100) {
        throw AppException.invalidField("options", MSG_LONG);
      }
      if (!seen.add(normalize(text))) {
        throw AppException.invalidField("options", MSG_DUPLICATE);
      }
      if (Boolean.TRUE.equals(o.correct())) {
        correct++;
      }
    }
    if (correct == 0) {
      throw AppException.invalidField("options", MSG_NO_CORRECT);
    }
    if (correct > 1) {
      throw AppException.invalidField("options", MSG_MANY_CORRECT);
    }
    int n = options.size();
    int attempts = in.maxAttempts() == null ? 1 : in.maxAttempts();
    if (attempts < 1 || attempts > n - 1) {
      throw AppException.invalidField(
          "maxAttempts", "Tối đa " + (n - 1) + " lượt cho câu " + n + " lựa chọn");
    }
    ArrayNode json = mapper.createArrayNode();
    UUID correctId = null;
    for (OptionInput o : options) {
      UUID id = UUID.randomUUID(); // mã ngẫu nhiên: không lộ thứ tự hay lựa chọn đúng
      json.addObject().put("id", id.toString()).put("text", o.text().strip());
      if (Boolean.TRUE.equals(o.correct())) {
        correctId = id;
      }
    }
    repo.replace(setId, item.problemId(), json.toString(), correctId, attempts);
    return teacherChoice(
        new ItemChoice(
            position, item.problemId(), item.assignmentId(), attempts, json.toString(), correctId));
  }

  /** Bỏ cấu hình trắc nghiệm: câu trở lại kiểu điền đáp số. */
  @Transactional
  public void remove(UUID setId, int position) {
    ItemChoice item = draftItem(setId, position);
    repo.delete(setId, item.problemId());
  }

  private ItemChoice draftItem(UUID setId, int position) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    String status =
        repo.ownSetStatus(p.userId(), setId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    if (!"draft".equals(status)) {
      throw new AppException(ErrorCode.CONFLICT, MSG_ASSIGNED);
    }
    return repo.itemsOfSet(setId).stream()
        .filter(i -> i.position() == position)
        .findFirst()
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
  }

  // ------------------------------------------------------------------ học sinh

  /** Lựa chọn của các câu trắc nghiệm trong bộ em đang làm (không có cờ đúng). */
  public List<StudentChoice> forStudent(UUID setId) {
    AuthPrincipal p = Roles.require(Role.STUDENT);
    sets.forStudent(p.userId(), setId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    List<ItemChoice> items =
        repo.itemsOfSet(setId).stream()
            .filter(i -> i.options() != null && i.assignmentId() != null)
            .toList();
    Map<UUID, List<Attempt>> attempts = new HashMap<>();
    for (Attempt a :
        repo.attempts(p.userId(), items.stream().map(ItemChoice::assignmentId).toList())) {
      attempts.computeIfAbsent(a.assignmentId(), k -> new ArrayList<>()).add(a);
    }
    List<StudentChoice> out = new ArrayList<>();
    for (ItemChoice i : items) {
      List<Attempt> mine = attempts.getOrDefault(i.assignmentId(), List.of());
      UUID chosen = null;
      for (Attempt a : mine) {
        if ("correct".equals(a.outcome())) {
          chosen = choiceId(a.detail());
        }
      }
      out.add(
          new StudentChoice(
              i.assignmentId(), i.position(), studentOptions(i.options()), dimmed(mine), chosen));
    }
    return out;
  }

  /**
   * Một lần "Kiểm tra" câu trắc nghiệm. Mã lựa chọn không thuộc câu (hoặc đã bị loại):
   * VALIDATION_FAILED, không ghi lượt (US-S2E9-03-FC6). Gửi lặp cùng mã chống lặp: trả đúng phản
   * hồi lần đầu, không trừ lượt lần hai (BR-S2E8-14).
   */
  public AttemptView submit(UUID assignmentId, UUID choiceId, String requestKey) {
    AuthPrincipal p = Roles.require(Role.STUDENT);
    if (requestKey == null || requestKey.isBlank() || requestKey.length() > 64) {
      throw AppException.invalidField("requestKey", "Thiếu mã chống lặp của lần gửi.");
    }
    PracticeSets.Grading g =
        sets.gradingForStudent(p.userId(), assignmentId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    ItemChoice item =
        repo.itemOfAssignment(assignmentId)
            .filter(i -> i.options() != null)
            .orElseThrow(
                () -> AppException.invalidField("choiceId", "Câu này không phải câu trắc nghiệm."));
    var replay = repo.attemptByKey(p.userId(), assignmentId, requestKey);
    if (replay.isPresent()) {
      return view(g, replay.get().outcome(), replay.get().wrongAfter(), p.userId(), assignmentId);
    }
    if ("closed".equals(g.setStatus())) {
      throw new AppException(ErrorCode.CONFLICT, "Bài đã đóng, em chỉ xem được kết quả.");
    }
    String text = null;
    for (StudentOption o : studentOptions(item.options())) {
      if (o.id().equals(choiceId)) {
        text = o.text();
      }
    }
    if (choiceId == null || text == null) {
      throw AppException.invalidField("choiceId", "Lựa chọn không thuộc câu này.");
    }
    List<Attempt> before = repo.attempts(p.userId(), List.of(assignmentId));
    if (dimmed(before).contains(choiceId)) {
      // bấm đôi: yêu cầu cùng mã vừa ghi xong giữa hai lần đọc -> trả lại phản hồi lần đầu
      // (BR-S2E8-14)
      var again = repo.attemptByKey(p.userId(), assignmentId, requestKey);
      if (again.isPresent()) {
        return view(g, again.get().outcome(), again.get().wrongAfter(), p.userId(), assignmentId);
      }
      throw AppException.invalidField("choiceId", "Lựa chọn này em đã chọn rồi.");
    }
    String outcome = choiceId.equals(item.correctId()) ? "correct" : "wrong";
    ObjectNode detail = mapper.createObjectNode();
    detail.put("choiceId", choiceId.toString());
    ChoiceRepository.Recorded r =
        repo.record(
            p.userId(),
            assignmentId,
            requestKey,
            text,
            outcome,
            detail.toString(),
            g.maxAttempts());
    return switch (r.status()) {
      case "OK", "DUPLICATE" -> view(g, r.outcome(), r.wrongAfter(), p.userId(), assignmentId);
      case "CONFLICT_CORRECT" -> throw new AppException(ErrorCode.CONFLICT, "Câu này đã đúng");
      default -> throw new AppException(ErrorCode.CONFLICT, "Câu này đã hết lượt thử.");
    };
  }

  /**
   * Câu của học sinh đang gọi có phải câu trắc nghiệm không (bộ chặn đường điền đáp số và nút "Em
   * nghĩ đáp số này đúng", BR-S2E9-14). Người gọi không phải học sinh: false.
   */
  public boolean isChoiceItem(UUID assignmentId) {
    if (!RequestContext.isAuthenticated() || RequestContext.principal().role() != Role.STUDENT) {
      return false;
    }
    return repo.itemOfAssignment(assignmentId).filter(i -> i.options() != null).isPresent();
  }

  // ------------------------------------------------------------------ nội bộ

  private AttemptView view(
      PracticeSets.Grading g, String outcome, int wrongAfter, UUID userId, UUID assignmentId) {
    int left = Math.max(0, g.maxAttempts() - wrongAfter);
    List<UUID> dimmed = dimmed(repo.attempts(userId, List.of(assignmentId)));
    if ("correct".equals(outcome)) {
      return new AttemptView(
          "correct", "Đúng rồi!", left, g.maxAttempts(), "correct", dimmed, false);
    }
    // Phản hồi Sai không nói lựa chọn nào đúng (BR-S2E9-14).
    return new AttemptView(
        "wrong",
        left > 0 ? "Chưa đúng. Còn " + left + " lượt." : "Chưa đúng. Em xem lại cùng thầy cô nhé.",
        left,
        g.maxAttempts(),
        left > 0 ? "doing" : "review",
        dimmed,
        false);
  }

  private List<UUID> dimmed(List<Attempt> attempts) {
    List<UUID> out = new ArrayList<>();
    for (Attempt a : attempts) {
      UUID id = "wrong".equals(a.outcome()) ? choiceId(a.detail()) : null;
      if (id != null && !out.contains(id)) {
        out.add(id);
      }
    }
    return out;
  }

  private UUID choiceId(String detailJson) {
    if (detailJson == null || detailJson.isBlank()) {
      return null;
    }
    JsonNode n = mapper.readTree(detailJson).get("choiceId");
    try {
      return n == null || n.isNull() ? null : UUID.fromString(n.asString());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  private List<StudentOption> studentOptions(String optionsJson) {
    List<StudentOption> out = new ArrayList<>();
    for (JsonNode o : mapper.readTree(optionsJson)) {
      out.add(
          new StudentOption(UUID.fromString(o.path("id").asString()), o.path("text").asString()));
    }
    return out;
  }

  private TeacherChoice teacherChoice(ItemChoice i) {
    List<TeacherOption> options = new ArrayList<>();
    for (StudentOption o : studentOptions(i.options())) {
      options.add(new TeacherOption(o.id(), o.text(), o.id().equals(i.correctId())));
    }
    return new TeacherChoice(i.position(), i.maxAttempts(), options);
  }

  /** Chuẩn hóa chữ để so trùng: NFC, gộp khoảng trắng, không phân biệt hoa thường (BR-S2E9-11). */
  static String normalize(String text) {
    return Normalizer.normalize(text, Normalizer.Form.NFC)
        .strip()
        .replaceAll("\\s+", " ")
        .toLowerCase(Locale.ROOT);
  }
}
