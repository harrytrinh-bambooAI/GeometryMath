package vn.geo.server.exam.internal.draft.service;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.aigateway.api.AiGateway;
import vn.geo.server.aigateway.api.AiRequest;
import vn.geo.server.aigateway.api.quota.AiQuota;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.exam.internal.draft.repo.ExamDraftRepository;
import vn.geo.server.exam.internal.draft.repo.ExamDraftRepository.Batch;
import vn.geo.server.exam.internal.draft.repo.ExamDraftRepository.Item;
import vn.geo.server.exam.internal.draft.repo.ExamDraftRepository.Source;
import vn.geo.server.geometry.api.GeometryEngine;

/**
 * Trợ lý soạn đề nháp từ tài liệu Đang dùng của giáo viên (US-S5E2-06, luồng D, C5-08):
 *
 * <ol>
 *   <li>Nguồn chỉ là tài liệu của chính giáo viên (khác: NOT_FOUND) có bản Đang dùng (không:
 *       CONFLICT MSG-S5-15) — kiểm TRƯỚC khi tính lượt hay gọi AI (BR-S5E2-14);
 *   <li>một lượt soạn của giáo viên (BR-S5E1-08: hết 30 lượt = RATE_LIMITED MSG-S5-06, không gọi
 *       AI);
 *   <li>task {@code exam_draft} qua cổng AI; mỗi đề qua đường đọc–dựng–giải–kiểm của engine ({@link
 *       GeometryEngine#verify}): dựng được và đáp số AI khớp engine = "Đã kiểm chứng" chọn sẵn;
 *       không thì "Cần xem lại", đặt cuối, không chọn sẵn (BR-S5E2-15). Đáp số lưu và hiện luôn là
 *       đáp số engine;
 *   <li>nhãn người "AI soạn" kèm nguồn "TL-01 v2"; chỉ vào ngân hàng "Của tôi" khi giáo viên Duyệt
 *       từng đề (BR-S5E2-16).
 * </ol>
 */
@Service
public class ExamDraftService {

  public static final String TASK = "exam_draft";
  public static final String MSG_14 =
      "Đề nháp chưa qua kiểm chứng: không chọn sẵn, đặt cuối danh sách.";
  public static final String MSG_15 = "Chưa có tài liệu Đang dùng nào để soạn đề.";
  public static final String REASON_UNBUILDABLE = "máy không dựng được hình";
  public static final String REASON_MISMATCH = "đáp số AI khác máy tính";
  public static final String REASON_NO_ANSWER = "AI không ghi đáp số";
  public static final String HUMAN_AI = "AI soạn";
  public static final String HUMAN_APPROVED = "Giáo viên đã duyệt";
  public static final String MACHINE_OK = "Đã kiểm chứng";
  public static final String MACHINE_REVIEW = "Cần xem lại";
  public static final int MAX_COUNT = 10;
  private static final int MAX_DOC_CHARS = 20_000;

  public record CreateRequest(UUID docId, UUID classId, Integer count) {}

  private final ExamDraftRepository repo;
  private final AiGateway ai;
  private final AiQuota quota;
  private final GeometryEngine engine;
  private final JsonMapper mapper;
  private final Clock clock;

  public ExamDraftService(
      ExamDraftRepository repo,
      AiGateway ai,
      AiQuota quota,
      GeometryEngine engine,
      JsonMapper mapper,
      Clock clock) {
    this.repo = repo;
    this.ai = ai;
    this.quota = quota;
    this.engine = engine;
    this.mapper = mapper;
    this.clock = clock;
  }

  @Transactional
  public Map<String, Object> create(CreateRequest in) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    if (in == null || in.docId() == null) {
      throw AppException.invalidField("docId", "Chọn tài liệu nguồn.");
    }
    if (in.classId() == null) {
      throw AppException.invalidField("classId", "Chọn lớp.");
    }
    int count = in.count() == null ? 5 : in.count();
    if (count < 1 || count > MAX_COUNT) {
      throw AppException.invalidField("count", "Số đề từ 1 đến " + MAX_COUNT + ".");
    }
    Source src =
        repo.source(me.orgId(), me.userId(), in.docId())
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    if (!repo.ownsClass(me.orgId(), me.userId(), in.classId())) {
      throw new AppException(ErrorCode.NOT_FOUND);
    }
    if (src.version() == null || src.content() == null) {
      throw new AppException(ErrorCode.CONFLICT, MSG_15);
    }
    if (!quota.assistantOn(me.orgId(), in.classId(), TASK)) {
      throw new AppException(ErrorCode.CONFLICT, "Trợ lý AI của lớp đang tắt.");
    }
    quota.takeComposeTurn(me.orgId(), me.userId(), in.classId(), TASK);
    String content = src.content();
    if (content.length() > MAX_DOC_CHARS) {
      content = content.substring(0, MAX_DOC_CHARS);
    }
    String prompt =
        "Số đề: "
            + count
            + "\n<tai_lieu ma=\""
            + src.code()
            + " v"
            + src.version()
            + "\">\n"
            + content
            + "\n</tai_lieu>";
    String reply = ai.complete(new AiRequest(TASK, prompt)).content();
    List<Checked> drafts = check(parse(reply), count);
    if (drafts.isEmpty()) {
      throw new AppException(
          ErrorCode.DEPENDENCY_UNAVAILABLE, "AI chưa soạn được đề nào. Thầy cô thử lại.");
    }
    // đề đạt kiểm chứng trước, giữ thứ tự AI trong từng nhóm; đề "Cần xem lại" ở cuối
    List<Checked> ordered = new ArrayList<>(drafts);
    ordered.sort(Comparator.comparingInt(c -> c.verified() ? 0 : 1));
    UUID batchId = Uuid7.next();
    repo.insertBatch(
        me.orgId(), batchId, me.userId(), in.classId(), src.id(), src.code(), src.version(), count);
    int ord = 1;
    for (Checked c : ordered) {
      repo.insertItem(
          me.orgId(),
          Uuid7.next(),
          batchId,
          me.userId(),
          ord++,
          c.statement(),
          c.section(),
          c.sceneJson(),
          c.aiAnswer(),
          c.engineAnswer(),
          c.verified() ? "verified" : "needs_review",
          c.reason());
    }
    return view(repo.batch(me.orgId(), me.userId(), batchId).orElseThrow(), me);
  }

  public Map<String, Object> get(UUID batchId) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    Batch b =
        repo.batch(me.orgId(), me.userId(), batchId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    return view(b, me);
  }

  @Transactional
  public Map<String, Object> approve(UUID itemId) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    Item it =
        repo.item(me.orgId(), me.userId(), itemId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    if ("approved".equals(it.status())) {
      return item(it);
    }
    if (!"verified".equals(it.machineLabel()) || it.engineAnswer() == null) {
      throw new AppException(
          ErrorCode.CONFLICT, "Đề \"Cần xem lại\" chưa duyệt được: máy chưa kiểm chứng đáp số.");
    }
    if (!repo.approve(me.orgId(), me.userId(), itemId, clock.instant())) {
      throw new AppException(ErrorCode.CONFLICT);
    }
    return item(repo.item(me.orgId(), me.userId(), itemId).orElseThrow());
  }

  /** Ngân hàng "Của tôi" của trợ lý: đề nháp giáo viên đã duyệt. */
  public Map<String, Object> bank() {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    List<Map<String, Object>> items = new ArrayList<>();
    for (Item it : repo.bank(me.orgId(), me.userId())) {
      items.add(item(it));
    }
    return Map.of("items", items);
  }

  // ---------------------------------------------------------------------------------------------

  /** Một đề sau kiểm chứng của engine. */
  record Checked(
      String statement,
      String section,
      String sceneJson,
      String aiAnswer,
      String engineAnswer,
      boolean verified,
      String reason) {}

  private JsonNode parse(String reply) {
    try {
      String s = reply == null ? "" : reply.strip();
      int i = s.indexOf('{');
      int j = s.lastIndexOf('}');
      return i >= 0 && j > i ? mapper.readTree(s.substring(i, j + 1)) : mapper.createObjectNode();
    } catch (RuntimeException e) {
      return mapper.createObjectNode();
    }
  }

  private static String text(JsonNode n, String field, int max) {
    String s = n.path(field).asString("").strip();
    return s.length() > max ? s.substring(0, max) : s;
  }

  /** Đọc–dựng–giải–kiểm từng đề bằng engine; đáp số chỉ lấy từ engine. */
  List<Checked> check(JsonNode root, int count) {
    List<Checked> out = new ArrayList<>();
    for (JsonNode d : root.path("drafts")) {
      if (out.size() >= count) {
        break;
      }
      String statement = text(d, "statement", 2000);
      if (statement.isEmpty()) {
        continue;
      }
      String section = text(d, "section", 200);
      String aiAnswer = text(d, "answer", 200);
      JsonNode scene = d.path("scene");
      String sceneJson = scene.isObject() ? mapper.writeValueAsString(scene) : null;
      String engineAnswer = null;
      boolean verified = false;
      String reason;
      GeometryEngine.ProblemSpec spec = null;
      try {
        if (scene.isObject()) {
          spec = engine.specFrom(scene);
        }
      } catch (RuntimeException e) {
        spec = null;
      }
      if (spec == null || spec.asked().size() != 1 || !engine.knows(spec.template())) {
        reason = REASON_UNBUILDABLE;
      } else {
        try {
          GeometryEngine.AskedQuantity aq = spec.asked().get(0);
          double value = engine.measure(engine.build(spec, 1), aq);
          engineAnswer = EngineAnswer.format(value, aq.kind(), spec.parametric());
          if (aiAnswer.isEmpty()) {
            reason = REASON_NO_ANSWER;
          } else {
            GeometryEngine.Verification v = engine.verify(spec, aiAnswer);
            if (!v.sceneOk()) {
              reason = REASON_UNBUILDABLE;
              engineAnswer = null;
            } else if (v.answerOk() && "verified".equals(v.status())) {
              verified = true;
              reason = null;
            } else {
              reason = REASON_MISMATCH;
            }
          }
        } catch (RuntimeException e) {
          reason = REASON_UNBUILDABLE;
          engineAnswer = null;
        }
      }
      if (engineAnswer == null) {
        verified = false;
        if (reason == null) {
          reason = REASON_UNBUILDABLE;
        }
      }
      out.add(
          new Checked(
              statement,
              section.isEmpty() ? null : section,
              sceneJson,
              aiAnswer.isEmpty() ? null : aiAnswer,
              engineAnswer,
              verified,
              reason));
    }
    return out;
  }

  private Map<String, Object> view(Batch b, AuthPrincipal me) {
    List<Map<String, Object>> items = new ArrayList<>();
    boolean anyReview = false;
    for (Item it : repo.items(me.orgId(), b.id())) {
      items.add(item(it));
      anyReview |= !"verified".equals(it.machineLabel());
    }
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", b.id().toString());
    m.put("classId", b.classId().toString());
    Map<String, Object> source = new LinkedHashMap<>();
    source.put("docId", b.docId().toString());
    source.put("docCode", b.docCode());
    source.put("version", b.docVersion());
    source.put("label", b.docCode() + " v" + b.docVersion());
    m.put("source", source);
    m.put("requested", b.requested());
    m.put("createdAt", b.createdAt().toString());
    m.put("items", items);
    m.put("message", anyReview ? MSG_14 : null);
    return m;
  }

  private Map<String, Object> item(Item it) {
    boolean verified = "verified".equals(it.machineLabel());
    boolean approved = "approved".equals(it.status());
    String label = it.docCode() + " v" + it.docVersion();
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", it.id().toString());
    m.put("batchId", it.batchId().toString());
    m.put("ord", it.ord());
    m.put("statement", it.statement());
    m.put("sourceLabel", label);
    m.put("sourceSection", it.sourceSection());
    m.put("source", it.sourceSection() == null ? label : label + " · " + it.sourceSection());
    m.put("machineLabel", verified ? MACHINE_OK : MACHINE_REVIEW);
    m.put("machineState", it.machineLabel());
    m.put("reviewReason", it.reviewReason());
    m.put("humanLabel", approved ? HUMAN_APPROVED : HUMAN_AI);
    m.put("status", it.status());
    m.put("preselected", verified && !approved);
    m.put("answer", it.engineAnswer());
    m.put("answerSource", it.engineAnswer() == null ? null : "engine");
    m.put("aiAnswer", it.aiAnswer());
    m.put("approvedAt", it.approvedAt() == null ? null : it.approvedAt().toString());
    return m;
  }
}
