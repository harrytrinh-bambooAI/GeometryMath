package vn.geo.server.knowledge.internal.kb.qa.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.aigateway.api.AiGateway;
import vn.geo.server.aigateway.api.AiRequest;
import vn.geo.server.aigateway.api.quota.AiQuota;
import vn.geo.server.answers.api.KsS5Firewall;
import vn.geo.server.answers.api.KsS5Firewall.Locked;
import vn.geo.server.assignment.api.HelpLevelApi;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.knowledge.api.KbRetriever;
import vn.geo.server.knowledge.internal.kb.qa.repo.KbQaRepository;
import vn.geo.server.knowledge.internal.kb.qa.repo.KbQaRepository.Context;
import vn.geo.server.knowledge.internal.kb.qa.service.KbQaPrompt.Ref;

/**
 * Hỏi đáp của học sinh theo kho đã gắn (US-S5E3-03), kiểm ở máy chủ trước khi gọi AI (US-S5E3-04,
 * BR-S5E3-13) và bộ kiểm đầu ra đầy đủ (US-S5E3-05). Thứ tự một lượt:
 *
 * <ol>
 *   <li>học sinh Đang học của lớp có bài (không: NOT_FOUND) → công tắc AI của lớp (S3-E5) → đã đọc
 *       thông báo riêng tư MSG-S5-26 → mức trợ giúp (Tắt: MSG-S5-21);
 *   <li>KS-4 trước khi gọi AI (câu hỏi đưa giá trị để xác nhận);
 *   <li>truy xuất ≤ 5 mục (tài liệu Đang dùng gắn lớp/bài + bài giảng mẫu đã duyệt); lọc mục có đáp
 *       số của bài đang khoá (báo giáo viên); không còn mục: MSG-S5-20, không gọi AI;
 *   <li>gọi task {@code kb_qa} (không danh tính; câu hỏi trong thẻ dữ liệu); nguồn phải thuộc danh
 *       sách đã gửi; kiểu trả lời phải được mức trợ giúp cho phép;
 *   <li>bộ kiểm KS-1…KS-4 + nội dung riêng, tự nó ≤ 200 ms, lỗi/quá giờ thì đóng (KS-5).
 * </ol>
 *
 * Mỗi lượt ghi một AIPolicyDecision (và AIInteraction khi lượt có câu hiện cho học sinh).
 */
@Service
public class KbQaService {

  public static final String PRIVACY_VERSION = "privacy-qa-s5-1.0";
  public static final String MSG_20 = KbRetriever.NO_SECTION;
  public static final String MSG_21 =
      "Thầy cô đang tắt Hỏi đáp cho bài này. Em vẫn làm bài và xem gợi ý như thường.";
  public static final String MSG_24 =
      "Câu trả lời chưa an toàn để hiện. Em thử hỏi cách khác hoặc xem gợi ý.";
  public static final String MSG_26 =
      "Câu em hỏi AI và câu trả lời được lưu tới hết năm học; thầy cô lớp em đọc được để giúp em.";

  /** Thông điệp lớp tắt AI của S3-E5 (MSG-83, ClassAiSwitchFilter). */
  public static final String MSG_CLASS_OFF =
      "Lớp này đã tắt trợ lý AI. Em vẫn làm bài và xem gợi ý có sẵn.";

  static final String REFUSED =
      "Câu trả lời có thể làm lộ đáp số hoặc thông tin riêng của bài nên chưa hiện được. Em xem"
          + " lại gợi ý của bài hoặc hỏi về khái niệm, công thức liên quan nhé.";
  static final String REFUSED_KS4 =
      "Thầy cô muốn em tự kiểm tra kết quả của mình. Em xem lại từng bước theo gợi ý của bài nhé.";
  static final String HINT_ONLY =
      "Bài này thầy cô chỉ mở gợi ý. Em mở mục Gợi ý của bài để xem gợi ý hiện có, hoặc hỏi cách"
          + " làm từng bước nhé.";

  static final long CHECK_BUDGET_MS = 200;
  private static final int MAX_SECTIONS = 5;
  private static final Set<String> KINDS = Set.of("concept", "hint", "explain_step");
  private static final ZoneId VN = ZoneId.of("Asia/Ho_Chi_Minh");

  public record AskRequest(String assignmentId, String question) {}

  private final KbQaRepository repo;
  private final KbRetriever retriever;
  private final HelpLevelApi help;
  private final KsS5Firewall firewall;
  private static final Logger log = LoggerFactory.getLogger(KbQaService.class);

  private final AiGateway ai;
  private final AiQuota quota;
  private final KbQaProbe probe;
  private final JsonMapper mapper;
  private final Clock clock;
  private final double minScore;

  public KbQaService(
      KbQaRepository repo,
      KbRetriever retriever,
      HelpLevelApi help,
      KsS5Firewall firewall,
      AiGateway ai,
      AiQuota quota,
      KbQaProbe probe,
      JsonMapper mapper,
      Clock clock,
      @Value("${geo.kb.min-score:0.1}") double minScore) {
    this.repo = repo;
    this.retriever = retriever;
    this.help = help;
    this.firewall = firewall;
    this.ai = ai;
    this.quota = quota;
    this.probe = probe;
    this.mapper = mapper;
    this.clock = clock;
    this.minScore = minScore;
  }

  // ---------------------------------------------------------------- thông báo riêng tư

  public Map<String, Object> privacy() {
    AuthPrincipal me = Roles.require(Role.STUDENT);
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("version", PRIVACY_VERSION);
    m.put("text", MSG_26);
    m.put("acked", repo.acked(me.orgId(), me.userId(), PRIVACY_VERSION));
    return m;
  }

  public Map<String, Object> ack(String version) {
    AuthPrincipal me = Roles.require(Role.STUDENT);
    if (!PRIVACY_VERSION.equals(version)) {
      throw AppException.invalidField("version", "Phiên bản thông báo không đúng.");
    }
    repo.ack(me.orgId(), me.userId(), PRIVACY_VERSION);
    return privacy();
  }

  // ---------------------------------------------------------------- một lượt hỏi

  /** Một lượt bị chặn / được phép sau các bước kiểm. */
  private record Outcome(String kind, String reply, String message, Map<String, Object> source) {}

  public Map<String, Object> ask(AskRequest req) {
    AuthPrincipal me = Roles.require(Role.STUDENT);
    UUID org = me.orgId();
    if (req == null || blank(req.assignmentId())) {
      throw AppException.invalidField("assignmentId", "Chọn bài.");
    }
    UUID target;
    try {
      target = UUID.fromString(req.assignmentId().strip());
    } catch (IllegalArgumentException e) {
      throw new AppException(ErrorCode.NOT_FOUND);
    }
    String question = req.question() == null ? "" : req.question().strip();
    if (question.isEmpty() || question.length() > 2000) {
      throw AppException.invalidField("question", "Câu hỏi từ 1 đến 2 000 ký tự.");
    }
    Context ctx = repo.context(org, me.userId(), target).orElse(null);
    if (ctx == null || !ctx.member()) {
      if (ctx != null) {
        decide(org, null, me.userId(), target, "blocked", "membership", null);
      }
      throw new AppException(ErrorCode.NOT_FOUND);
    }
    if (Boolean.FALSE.equals(ctx.aiEnabled())) {
      decide(org, null, me.userId(), target, "blocked", "class_switch", null);
      throw new AppException(ErrorCode.FORBIDDEN, MSG_CLASS_OFF);
    }
    if (!repo.acked(org, me.userId(), PRIVACY_VERSION)) {
      throw new AppException(ErrorCode.CONFLICT, MSG_26);
    }
    HelpLevelApi.Policy policy = help.policy(org, target, ctx.targetKind());
    UUID interaction = Uuid7.next();
    if (HelpLevelApi.OFF.equals(policy.helpLevel())) {
      return finish(
          me,
          ctx,
          target,
          interaction,
          question,
          new Outcome("off", MSG_21, MSG_21, null),
          "blocked",
          "help_level",
          null);
    }
    Locked locked = ctx.locked() ? locked(ctx) : null;
    String pre;
    try {
      pre = firewall.preCheck(question, locked);
    } catch (RuntimeException e) {
      return closed(me, ctx, target, interaction, question, "ERR_GUARD", null);
    }
    if (pre != null) {
      return finish(
          me,
          ctx,
          target,
          interaction,
          question,
          new Outcome("refused_answer", REFUSED_KS4, null, null),
          "blocked",
          pre,
          0);
    }
    // truy xuất: tài liệu gắn lớp/bài (Đang dùng) + bài giảng mẫu đã duyệt; lọc mục có đáp số
    Instant now = clock.instant();
    UUID assignmentForGrants = "practice".equals(ctx.targetKind()) ? target : null;
    List<Ref> refs = new ArrayList<>();
    for (KbRetriever.Hit h :
        retriever.retrieve(
            org, ctx.classId(), assignmentForGrants, question, policy.knowledgeCodes(), now)) {
      if (locked != null && firewall.leaks(h.title() + "\n" + h.body(), locked) != null) {
        repo.warn(org, Uuid7.next(), target, h.docId(), h.version(), h.sectionId());
        continue;
      }
      refs.add(Ref.of(h));
    }
    if (refs.size() < MAX_SECTIONS) {
      for (KbQaRepository.Lecture l :
          repo.approvedLectures(org, question, minScore, MAX_SECTIONS - refs.size())) {
        if (locked != null && firewall.leaks(l.title() + "\n" + l.body(), locked) != null) {
          continue;
        }
        refs.add(Ref.of(l));
      }
    }
    if (refs.isEmpty()) {
      return finish(
          me,
          ctx,
          target,
          interaction,
          question,
          new Outcome("no_source", MSG_20, MSG_20, null),
          "blocked",
          "no_source",
          null);
    }
    // hạn mức HS 30 / lớp 300 và tắt khẩn kb_qa như hỏi trợ lý trên bài (REVIEW-S5 #2)
    UUID quotaClass = quota.takeStudentAsk(org, ctx.classId(), me.userId()) ? ctx.classId() : null;
    Instant askedAt = Instant.now().minusMillis(5);
    String prompt = KbQaPrompt.build(policy.helpLevel(), refs, question);
    probe.sent(interaction, prompt);
    String content = ai.complete(new AiRequest("kb_qa", prompt)).content();
    if (quotaClass != null) {
      try {
        quota.addTokensSince(org, quotaClass, me.userId(), askedAt);
      } catch (RuntimeException e) {
        log.warn("không cộng được token AI của lớp: {}", e.toString());
      }
    }
    KbQaPrompt.Reply r = KbQaPrompt.parse(mapper, content);
    // bộ kiểm đầu ra (KS-5: lỗi hoặc tự nó quá 200 ms thì đóng)
    long started = System.nanoTime();
    String rule;
    try {
      probe.beforeCheck(me.userId());
      rule =
          firewall.check(
              question,
              r.reply(),
              locked,
              secrets(policy, ctx, peers(org, me.userId(), ctx, target)));
    } catch (RuntimeException e) {
      return closed(me, ctx, target, interaction, question, "ERR_GUARD", ms(started));
    }
    int checkMs = ms(started);
    if (checkMs > CHECK_BUDGET_MS) {
      return closed(me, ctx, target, interaction, question, "ERR_TIMEOUT", checkMs);
    }
    if (rule != null) {
      return finish(
          me,
          ctx,
          target,
          interaction,
          question,
          new Outcome("refused_answer", "KS-4".equals(rule) ? REFUSED_KS4 : REFUSED, null, null),
          "blocked",
          rule,
          checkMs);
    }
    if ("refuse".equals(r.kind())) {
      return finish(
          me,
          ctx,
          target,
          interaction,
          question,
          new Outcome("refused_answer", r.reply().isBlank() ? REFUSED : r.reply(), null, null),
          "allowed",
          null,
          checkMs);
    }
    Ref src = r.source() == null ? null : Ref.find(refs, r.source());
    if (!KINDS.contains(r.kind()) || src == null || r.reply().isBlank()) {
      return finish(
          me,
          ctx,
          target,
          interaction,
          question,
          new Outcome("no_source", MSG_20, MSG_20, null),
          "blocked",
          "invalid_source",
          checkMs);
    }
    if (!HelpLevelApi.allows(policy.helpLevel(), r.kind())) {
      return finish(
          me,
          ctx,
          target,
          interaction,
          question,
          new Outcome("hint", HINT_ONLY, null, null),
          "blocked",
          "kind_not_allowed",
          checkMs);
    }
    return finish(
        me,
        ctx,
        target,
        interaction,
        question,
        new Outcome(r.kind(), r.reply(), null, src.source()),
        "allowed",
        null,
        checkMs);
  }

  private Map<String, Object> closed(
      AuthPrincipal me,
      Context ctx,
      UUID target,
      UUID interaction,
      String q,
      String code,
      Integer checkMs) {
    return finish(
        me,
        ctx,
        target,
        interaction,
        q,
        new Outcome("error_closed", MSG_24, MSG_24, null),
        "error_closed",
        code,
        checkMs);
  }

  private Map<String, Object> finish(
      AuthPrincipal me,
      Context ctx,
      UUID target,
      UUID interaction,
      String question,
      Outcome o,
      String decision,
      String rule,
      Integer checkMs) {
    UUID org = me.orgId();
    repo.interaction(
        org,
        interaction,
        me.userId(),
        ctx.classId(),
        target,
        ctx.targetKind(),
        question,
        o.kind(),
        o.reply(),
        o.source() == null ? null : mapper.writeValueAsString(o.source()));
    decide(org, interaction, me.userId(), target, decision, rule, checkMs);
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("interactionId", interaction);
    m.put("kind", o.kind());
    m.put("reply", o.reply());
    if (o.message() != null) {
      m.put("message", o.message());
    }
    m.put("retry", "error_closed".equals(o.kind()));
    m.put("source", o.source());
    return m;
  }

  private void decide(
      UUID org,
      UUID interaction,
      UUID student,
      UUID target,
      String decision,
      String rule,
      Integer checkMs) {
    repo.decision(
        org,
        Uuid7.next(),
        interaction,
        student,
        target,
        decision,
        rule,
        checkMs,
        KsS5Firewall.VERSION);
    repo.count(org, LocalDate.ofInstant(clock.instant(), VN), decision, rule);
  }

  // ---------------------------------------------------------------- mở nguồn

  /** Mục được dẫn của một lượt (chỉ mục đó, không mục nào khác; US-S5E3-03-SC2). */
  public Map<String, Object> source(UUID interactionId) {
    AuthPrincipal me = Roles.require(Role.STUDENT);
    KbQaRepository.Stored s =
        repo.stored(me.orgId(), interactionId)
            .filter(x -> x.studentId().equals(me.userId()) && x.sourceJson() != null)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    JsonNode src = mapper.readTree(s.sourceJson());
    boolean lecture = "lecture".equals(src.path("type").asString(""));
    UUID id = UUID.fromString(src.path(lecture ? "lectureId" : "sectionId").asString(""));
    KbQaRepository.Section sec =
        (lecture ? repo.lecture(me.orgId(), id) : repo.section(me.orgId(), id))
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("type", src.path("type").asString(""));
    m.put("typeLabel", src.path("typeLabel").asString(""));
    m.put("label", src.path("label").asString(""));
    m.put("docTitle", src.path(lecture ? "title" : "docTitle").asString(""));
    m.put("version", sec.version());
    m.put("sectionTitle", sec.title());
    m.put("body", sec.body());
    return m;
  }

  // ---------------------------------------------------------------- giáo viên: cảnh báo

  public Map<String, Object> warnings(UUID targetId) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    if (!repo.teacherOwns(me.orgId(), me.userId(), targetId)) {
      throw new AppException(ErrorCode.NOT_FOUND);
    }
    List<Map<String, Object>> items = new ArrayList<>();
    for (KbQaRepository.Warning w : repo.warnings(me.orgId(), targetId)) {
      Map<String, Object> m = new LinkedHashMap<>();
      m.put("docCode", w.docCode());
      m.put("version", w.version());
      m.put("sectionTitle", w.sectionTitle());
      m.put("hits", w.hits());
      m.put(
          "message",
          "Tài liệu " + w.docCode() + " v" + w.version() + " có đáp số của bài đang khoá");
      items.add(m);
    }
    return Map.of("items", items);
  }

  // ---------------------------------------------------------------- tiện ích

  private Locked locked(Context ctx) {
    List<String> steps = new ArrayList<>();
    if (ctx.stepsJson() != null) {
      for (JsonNode st : mapper.readTree(ctx.stepsJson())) {
        steps.add(st.path("body").asString(""));
      }
    }
    List<String> forms = new ArrayList<>();
    if (ctx.formsJson() != null) {
      for (JsonNode f : mapper.readTree(ctx.formsJson())) {
        if (f.isString()) {
          forms.add(f.asString());
        }
      }
    }
    if (ctx.finalAnswer() == null && forms.isEmpty()) {
      return null;
    }
    return new Locked(ctx.finalAnswer(), steps, forms);
  }

  /** Bài của bạn (BN-02…) chỉ có ở bài có chấm (Dev-S5E3-13). */
  private List<String> peers(UUID org, UUID user, Context ctx, UUID target) {
    return "graded".equals(ctx.targetKind()) ? repo.peerTexts(org, user, target) : List.of();
  }

  private static List<String> secrets(HelpLevelApi.Policy policy, Context ctx, List<String> peers) {
    List<String> out = new ArrayList<>(peers);
    if (policy.privateNote() != null) {
      out.add(policy.privateNote());
    }
    if (ctx.rubricText() != null) {
      out.add(ctx.rubricText());
    }
    return out;
  }

  private static int ms(long startedNanos) {
    return (int) ((System.nanoTime() - startedNanos) / 1_000_000);
  }

  private static boolean blank(String s) {
    return s == null || s.isBlank();
  }
}
