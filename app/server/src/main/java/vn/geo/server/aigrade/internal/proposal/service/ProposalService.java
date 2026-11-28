package vn.geo.server.aigrade.internal.proposal.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.aigateway.api.AiGateway;
import vn.geo.server.aigateway.api.AiRequest;
import vn.geo.server.aigateway.api.quota.AiQuota;
import vn.geo.server.aigrade.api.AiGradeSignals;
import vn.geo.server.aigrade.internal.engine.service.EngineMatcher;
import vn.geo.server.aigrade.internal.proposal.repo.ProposalRepository;
import vn.geo.server.aigrade.internal.proposal.repo.ProposalRepository.Candidate;
import vn.geo.server.aigrade.internal.proposal.repo.ProposalRepository.CriterionRow;
import vn.geo.server.aigrade.internal.proposal.repo.ProposalRepository.Proposal;
import vn.geo.server.aigrade.internal.proposal.repo.ProposalRepository.SubRef;
import vn.geo.server.assessment.api.ImageStore;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.RequestContext;
import vn.geo.server.common.security.Role;

/**
 * AI đề xuất điểm theo tiêu chí (US-S5E6-01) và tín hiệu engine (US-S5E6-03). Việc nền: bài có chấm
 * chế độ "AI đề xuất", bản đọc chữ mới nhất của phiên bản chấm đã đọc được và chưa có đề xuất → xin
 * lượt qua {@link AiQuota#admitGrading} (mỗi trang một lượt, hạn mức lớp + tắt khẩn) → task {@code
 * grade} (gọi lại một lần khi lỗi hoặc kiểm cứng sai) → {@link GradeRules} → so đáp số với engine
 * ({@link EngineMatcher}) → lưu bất biến. Chỉ giáo viên chủ lớp đọc đề xuất; học sinh FORBIDDEN.
 */
@Service
public class ProposalService implements AiGradeSignals {

  private static final Logger log = LoggerFactory.getLogger(ProposalService.class);

  /** Ngưỡng độ tin cậy tổng (BR-S5E6-05, QS5-22). */
  public static final double MIN_CONFIDENCE = 0.6;

  public static final String REASON_UNAVAILABLE = "AI tạm thời không phản hồi";

  /** MSG-S5-45. */
  static String msgNoGrade(String reason) {
    return "AI không chấm bài này: " + reason + ". Thầy cô chấm tay nhé.";
  }

  /** MSG-S5-46. */
  static String msgMismatch(String engine) {
    return "Đáp số trong bài khác kết quả máy tính (" + engine + ").";
  }

  private final ProposalRepository repo;
  private final ProposalWriter writer;
  private final AiQuota quota;
  private final AiGateway ai;
  private final ImageStore images;
  private final JsonMapper mapper;
  private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
  private final Set<String> running = ConcurrentHashMap.newKeySet();

  public ProposalService(
      ProposalRepository repo,
      ProposalWriter writer,
      AiQuota quota,
      AiGateway ai,
      ImageStore images,
      JsonMapper mapper) {
    this.repo = repo;
    this.writer = writer;
    this.quota = quota;
    this.ai = ai;
    this.images = images;
    this.mapper = mapper;
  }

  // ------------------------------------------------------------------ việc nền

  /** Xếp chấm nền mọi bài đang chờ (gọi từ job). Trả số việc mới xếp. */
  public int schedulePending(int limit) {
    int n = 0;
    for (Candidate c : repo.pending(limit)) {
      if (schedule(c)) {
        n++;
      }
    }
    return n;
  }

  private static String key(Candidate c) {
    return c.versionId() + "#" + c.ocrVersion();
  }

  private boolean schedule(Candidate c) {
    String k = key(c);
    if (!running.add(k)) {
      return false;
    }
    executor.submit(() -> {
      try {
        gradeAs(c);
      } catch (RuntimeException e) {
        log.warn("Chấm nền đề xuất AI lỗi: {}", e.toString());
      } finally {
        running.remove(k);
      }
    });
    return true;
  }

  /** Chấm ngay một bài đang chờ (móc kiểm thử); false nếu không có việc chờ. */
  public boolean runNow(UUID orgId, UUID submissionId) {
    var c = repo.pendingOf(orgId, submissionId);
    if (c.isEmpty() || !running.add(key(c.get()))) {
      return false;
    }
    try {
      gradeAs(c.get());
    } finally {
      running.remove(key(c.get()));
    }
    return true;
  }

  /**
   * Lượt gọi AI chạy dưới danh tính học sinh nộp (như việc đọc chữ nền), không có tên trong nội
   * dung.
   */
  private void gradeAs(Candidate c) {
    RequestContext.callAs(
        new AuthPrincipal(c.orgId(), c.studentId(), Role.STUDENT),
        () -> {
          grade(c);
          return null;
        });
  }

  void grade(Candidate c) {
    List<GradeRules.Criterion> rubric =
        repo.criteria(c.orgId(), c.rubricVersionId()).stream()
            .map(r -> new GradeRules.Criterion(r.ord(), r.name(), r.max(), r.directionDependent()))
            .toList();
    int rubricVersion = repo.rubricVersion(c.orgId(), c.rubricVersionId());
    JsonNode pages = mapper.readTree(c.ocrPages());
    String engineAnswer =
        repo.engineAnswer(c.orgId(), c.teacherId(), c.problemVersionId()).orElse(null);
    EngineMatcher.Result engine = EngineMatcher.compare(GradePrompt.lines(pages), engineAnswer);
    String model = ai.model();
    String promptVersion = GradePrompt.VERSION + "/" + ai.promptVersion();

    // xin lượt: mỗi trang một lượt (BR-S5E5-12)
    for (int p = 0; p < Math.max(1, c.pageCount()); p++) {
      AiQuota.Admission a = quota.admitGrading(c.orgId(), c.classId(), "grade");
      if (!a.allowed()) {
        writer.save(
            c, rubricVersion, noGrade(a.reason(), 0, 0), engine, null, model, promptVersion);
        return;
      }
    }
    UUID job = quota.registerJob(c.orgId(), c.classId(), "grade", c.submissionId());
    try {
      String prompt = prompt(c, rubric, pages, engineAnswer, true);
      Result r = call(prompt, rubric, c.scale());
      if (!quota.jobActive(c.orgId(), job)) {
        return; // lớp tắt AI chấm giữa chừng: huỷ, không lưu
      }
      quota.addTokens(c.orgId(), c.classId(), r.tokens());
      writer.save(c, rubricVersion, r, engine, engineAnswer, model, promptVersion);
    } finally {
      quota.finishJob(c.orgId(), job);
    }
  }

  /** Kết quả một lượt chấm (đã gọi lại nếu cần). */
  record Result(
      String status, String reason, GradeRules.Outcome outcome, int attempts, int tokens) {}

  private static Result noGrade(String reason, int attempts, int tokens) {
    return new Result("no_grade", reason, null, attempts, tokens);
  }

  private Result call(String prompt, List<GradeRules.Criterion> rubric, BigDecimal scale) {
    Set<String> known = repo.knowledgeCodes();
    String lastReason = REASON_UNAVAILABLE;
    int tokens = 0;
    for (int attempt = 1; attempt <= 2; attempt++) { // gọi lại một lần (BR-S5E6-02, PO-78)
      String content;
      try {
        content = ai.complete(new AiRequest("grade", prompt)).content();
      } catch (RuntimeException e) {
        log.info("grade lần {} lỗi: {}", attempt, e.getClass().getSimpleName());
        lastReason = REASON_UNAVAILABLE;
        continue;
      }
      tokens += (prompt.length() + content.length()) / 4; // ước tính (AiResponse chưa trả token)
      JsonNode reply;
      try {
        reply = mapper.readTree(content);
      } catch (RuntimeException e) {
        lastReason = GradeRules.REASON_BROKEN;
        continue;
      }
      if ("refuse".equals(reply.path("kind").asString(""))) {
        String why = reply.path("reason").asString("AI từ chối chấm");
        return noGrade(why.length() > 200 ? why.substring(0, 200) : why, attempt, tokens);
      }
      GradeRules.Outcome o = GradeRules.apply(rubric, scale, reply, known);
      if (!o.valid()) {
        lastReason = o.invalidReason();
        continue;
      }
      if (o.confidence() < MIN_CONFIDENCE) {
        return noGrade(
            "độ tin cậy thấp ("
                + String.format(Locale.ROOT, "%.2f", o.confidence()).replace('.', ',')
                + ")",
            attempt,
            tokens);
      }
      return new Result("proposed", null, o, attempt, tokens);
    }
    return noGrade(lastReason, 2, tokens);
  }

  /** Tin nhắn task grade của một bài đang chờ (đọc lại ảnh khi {@code withImages}). */
  private String prompt(
      Candidate c,
      List<GradeRules.Criterion> rubric,
      JsonNode pages,
      String engineAnswer,
      boolean withImages) {
    List<byte[]> imgs = new ArrayList<>();
    if (withImages) {
      for (int p = 1; p <= c.pageCount(); p++) {
        try {
          imgs.add(images.forAi(c.orgId(), c.versionId(), p));
        } catch (RuntimeException e) {
          imgs.add(null); // không có ảnh JPEG/PNG gửi được: chấm trên bản đọc chữ
        }
      }
    }
    return GradePrompt.build(
        repo.statement(c.problemVersionId()).orElse(null),
        engineAnswer,
        rubric,
        c.scale(),
        c.ocrVersion(),
        pages,
        imgs);
  }

  /** Tin nhắn (không ảnh) sẽ gửi cho bài đang chờ — móc kiểm thử soát danh tính. */
  public String previewPrompt(UUID orgId, UUID submissionId) {
    Candidate c =
        repo.currentOf(orgId, submissionId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    List<GradeRules.Criterion> rubric =
        repo.criteria(c.orgId(), c.rubricVersionId()).stream()
            .map(r -> new GradeRules.Criterion(r.ord(), r.name(), r.max(), r.directionDependent()))
            .toList();
    String engineAnswer =
        repo.engineAnswer(c.orgId(), c.teacherId(), c.problemVersionId()).orElse(null);
    return prompt(c, rubric, mapper.readTree(c.ocrPages()), engineAnswer, false);
  }

  // ------------------------------------------------------------------ API giáo viên

  private SubRef teacherSub(UUID submissionId) {
    AuthPrincipal me = RequestContext.principal();
    if (me.role() != Role.TEACHER) {
      throw new AppException(ErrorCode.FORBIDDEN);
    }
    SubRef s =
        repo.submission(me.orgId(), submissionId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    if (!me.userId().equals(s.teacherId())) {
      throw new AppException(ErrorCode.NOT_FOUND);
    }
    return s;
  }

  /** Đề xuất mới nhất của phiên bản chấm (US-S5E6-01, 03); "pending" khi chưa chấm xong. */
  public Map<String, Object> get(UUID submissionId) {
    SubRef s = teacherSub(submissionId);
    UUID org = RequestContext.principal().orgId();
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("submissionId", s.id().toString());
    m.put("submissionVersion", s.currentVersion());
    var opt = repo.latest(org, s.id(), s.currentVersion());
    if (opt.isEmpty()) {
      m.put("status", "pending");
      m.put("statusLabel", "Đang chờ AI đề xuất");
      m.put("flags", List.of());
      return m;
    }
    Proposal p = opt.get();
    boolean noGrade = "no_grade".equals(p.status());
    m.put("proposalId", p.id().toString());
    m.put("status", p.status());
    m.put("statusLabel", noGrade ? FLAG_NO_GRADE : "AI đề xuất");
    m.put("flags", flags(p));
    if (noGrade) {
      m.put("reason", p.reason());
      m.put("message", msgNoGrade(p.reason()));
    } else {
      m.put("total", p.total());
      m.put("scale", p.scale());
      m.put("confidence", p.confidence());
      List<Map<String, Object>> cs = new ArrayList<>();
      for (CriterionRow c : repo.criteriaOf(org, p.id())) {
        Map<String, Object> x = new LinkedHashMap<>();
        x.put("ord", c.ord());
        x.put("name", c.name());
        x.put("maxPoints", c.max());
        x.put("aiPoints", c.aiPoints());
        x.put("points", c.points());
        x.put("reason", c.reason());
        x.put("region", c.region() == null ? null : mapper.readTree(c.region()));
        x.put("confidence", c.confidence());
        x.put("errorLabel", c.errorLabel());
        x.put("adjustments", mapper.readTree(c.adjustments()));
        x.put("flags", mapper.readTree(c.flags()));
        cs.add(x);
      }
      m.put("criteria", cs);
      m.put("steps", mapper.readTree(p.steps()));
    }
    m.put("engine", engine(p));
    Map<String, Object> v = new LinkedHashMap<>();
    v.put(
        "problemVersionId", p.problemVersionId() == null ? null : p.problemVersionId().toString());
    v.put("rubricVersionId", p.rubricVersionId().toString());
    v.put("rubricVersion", p.rubricVersion());
    v.put("submissionVersion", p.submissionVersion());
    v.put("ocrVersion", p.ocrVersion());
    v.put("task", "grade");
    v.put("model", p.model());
    v.put("promptVersion", p.promptVersion());
    v.put("attempts", p.attempts());
    v.put("tokens", p.tokens());
    m.put("versions", v);
    m.put("createdAt", p.createdAt().toString());
    return m;
  }

  private Map<String, Object> engine(Proposal p) {
    Map<String, Object> e = new LinkedHashMap<>();
    String st = p.engineStatus() == null ? EngineMatcher.NO_ENGINE : p.engineStatus();
    e.put("status", st);
    e.put("statusLabel", EngineMatcher.label(st));
    e.put("engineAnswer", p.engineAnswer());
    e.put("studentAnswer", p.studentAnswer());
    e.put("rule", p.engineRule());
    e.put("disagree", Boolean.TRUE.equals(p.disagree()));
    if (EngineMatcher.MISMATCH.equals(st)) {
      e.put("message", msgMismatch(p.engineAnswer()));
    }
    if (EngineMatcher.NOT_FOUND.equals(st)) {
      e.put("note", EngineMatcher.NOTE_NOT_FOUND);
    }
    return e;
  }

  private List<String> flags(Proposal p) {
    List<String> out = new ArrayList<>();
    if ("no_grade".equals(p.status())) {
      out.add(FLAG_NO_GRADE);
    }
    if (Boolean.TRUE.equals(p.disagree())) {
      out.add(FLAG_DISAGREE);
    }
    for (JsonNode f : mapper.readTree(p.flags())) {
      if (!out.contains(f.asString(""))) {
        out.add(f.asString(""));
      }
    }
    return out;
  }

  /** Tín hiệu AI của các bài nộp một bài có chấm, bài có cờ xếp trước (STAGES §5 B2). */
  public Map<String, Object> assignmentSignals(UUID assignmentId) {
    AuthPrincipal me = RequestContext.principal();
    if (me.role() != Role.TEACHER) {
      throw new AppException(ErrorCode.FORBIDDEN);
    }
    if (!repo.assignmentTeacher(me.orgId(), assignmentId).map(me.userId()::equals).orElse(false)) {
      throw new AppException(ErrorCode.NOT_FOUND);
    }
    List<Map<String, Object>> items = new ArrayList<>();
    forAssignment(me.orgId(), assignmentId).values().stream()
        .sorted(
            Comparator.comparing((Summary x) -> !x.flagged())
                .thenComparing(x -> x.submissionId().toString()))
        .forEach(x -> {
          Map<String, Object> i = new LinkedHashMap<>();
          i.put("submissionId", x.submissionId().toString());
          i.put("status", x.status());
          i.put("reason", x.reason());
          i.put("total", x.total());
          i.put("engineStatus", x.engineStatus());
          i.put("flags", x.flags());
          i.put("flagged", x.flagged());
          items.add(i);
        });
    return Map.of("assignmentId", assignmentId.toString(), "items", items);
  }

  @Override
  public Map<UUID, Summary> forAssignment(UUID orgId, UUID assignmentId) {
    Map<UUID, Summary> out = new LinkedHashMap<>();
    for (Proposal p : repo.latestOfAssignment(orgId, assignmentId)) {
      out.put(
          p.submissionId(),
          new Summary(
              p.submissionId(),
              p.status(),
              p.reason(),
              p.total(),
              p.engineStatus() == null ? EngineMatcher.NO_ENGINE : p.engineStatus(),
              flags(p)));
    }
    return out;
  }
}
