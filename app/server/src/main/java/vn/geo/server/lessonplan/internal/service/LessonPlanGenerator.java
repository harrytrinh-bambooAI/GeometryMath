package vn.geo.server.lessonplan.internal.service;

import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import vn.geo.server.aigateway.api.AiGateway;
import vn.geo.server.aigateway.api.AiRequest;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.RequestContext;
import vn.geo.server.lessonplan.internal.repo.LessonCatalogRepository.Exercise;
import vn.geo.server.lessonplan.internal.repo.LessonPlanRepository;

/**
 * Chạy nền một lượt soạn (US-S5E1-02): gọi task {@code lesson_plan} qua cổng AI, chuẩn hoá + kiểm
 * tất định, gọi lại ĐÚNG một lần (kèm danh sách lỗi; JSON hỏng/cổng lỗi: cùng đầu vào), điền đáp số
 * từ kho. Hai lần đều hỏng: trạng thái "Lỗi", không lưu bản hỏng, lượt không tính hạn mức.
 */
@Component
public class LessonPlanGenerator {

  /** Thông điệp an toàn của cổng AI khi nhà cung cấp từ chối (InstrumentedAiGateway). */
  static final String REFUSAL_MESSAGE = "AI không xử lý được yêu cầu này.";

  private static final Logger log = LoggerFactory.getLogger(LessonPlanGenerator.class);

  /**
   * Đầu vào một lượt.
   *
   * @param message tin nhắn user đã dựng
   * @param soTiet số tiết
   * @param allowedKt mã kiến thức đã cấp
   * @param bank kho bài tập đã cấp
   */
  public record Job(
      UUID planId,
      UUID turnId,
      String message,
      int soTiet,
      Set<String> allowedKt,
      List<Exercise> bank) {}

  private final AiGateway ai;
  private final LessonPlanRepository plans;
  private final JsonMapper mapper;
  private final LessonCheck checker;
  private final MeterRegistry meters;
  private final org.springframework.beans.factory.ObjectProvider<LessonPlanTestHookService> hooks;
  private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

  public LessonPlanGenerator(
      AiGateway ai,
      LessonPlanRepository plans,
      JsonMapper mapper,
      MeterRegistry meters,
      org.springframework.beans.factory.ObjectProvider<LessonPlanTestHookService> hooks) {
    this.ai = ai;
    this.plans = plans;
    this.mapper = mapper;
    this.checker = new LessonCheck(mapper);
    this.meters = meters;
    this.hooks = hooks;
  }

  /** Chạy nền dưới danh tính của giáo viên (cổng AI ghi ai_call_log theo người gọi). */
  public void submit(AuthPrincipal me, Job job) {
    executor.submit(() -> {
      try {
        RequestContext.callAs(
            me,
            () -> {
              run(job);
              return null;
            });
      } catch (RuntimeException e) {
        log.warn("soạn giáo án {} lỗi: {}", job.planId(), e.toString());
        RequestContext.callAs(
            me,
            () -> {
              plans.finish(job.planId(), "error", null, null, "[]", 0, 0);
              plans.uncount(job.turnId());
              return null;
            });
      }
    });
  }

  void run(Job job) {
    Set<String> bankCodes = job.bank().stream().map(Exercise::code).collect(Collectors.toSet());
    ArrayNode logs = mapper.createArrayNode();
    int calls = 0;
    int fixKeys = 0;
    String message = job.message();
    LessonCheck.Result result = null;
    for (int attempt = 1; attempt <= 2; attempt++) {
      calls++;
      String text;
      String sent = message;
      hooks.ifAvailable(h -> h.recordSent(job.planId(), sent));
      try {
        text = ai.complete(new AiRequest("lesson_plan", message)).content();
      } catch (AppException e) {
        if (REFUSAL_MESSAGE.equals(e.getMessage())) {
          logs.addObject().put("attempt", attempt).putArray("errors").add("refusal");
          plans.finish(job.planId(), "refused", null, null, json(logs), fixKeys, calls);
          plans.uncount(job.turnId());
          return;
        }
        logs.addObject().put("attempt", attempt).putArray("errors").add("AI_UNAVAILABLE");
        result = null;
        continue; // cổng AI hết giờ / lỗi: gọi lại một lần với cùng đầu vào (PO-78)
      }
      result = checker.check(text, job.soTiet(), job.allowedKt(), bankCodes);
      fixKeys += result.fixKeys();
      ArrayNode errs = logs.addObject().put("attempt", attempt).putArray("errors");
      result.errors().forEach(errs::add);
      if (result.ok()) {
        break;
      }
      if (!result.parseError()) {
        message = LessonMessage.withErrors(job.message(), result.errors());
      }
    }
    if (fixKeys > 0) {
      meters.counter("geo.lessonplan.fix_keys").increment(fixKeys);
    }
    if (result == null || !result.ok()) {
      plans.finish(job.planId(), "error", null, null, json(logs), fixKeys, calls);
      plans.uncount(job.turnId());
      return;
    }
    ObjectNode node = result.node();
    String kind = result.kind();
    if ("lesson".equals(kind)) {
      fillAnswers(node, job.bank());
      plans.finish(job.planId(), "ready", json(node), null, json(logs), fixKeys, calls);
    } else {
      ObjectNode reply = mapper.createObjectNode().put("kind", kind);
      reply.put("text", node.path("text").asString(""));
      if (node.hasNonNull("field")) {
        reply.put("field", node.path("field").asString(""));
      }
      if (node.has("de_xuat")) {
        reply.set("de_xuat", node.get("de_xuat"));
      }
      plans.finish(
          job.planId(),
          "question".equals(kind) ? "question" : "reply",
          null,
          json(reply),
          json(logs),
          fixKeys,
          calls);
    }
  }

  /**
   * Đáp số do máy chủ điền (BR-S5E1-07): câu thuộc kho lấy đáp số kho; câu "moi" hoặc kho không có
   * đáp số → nhãn máy "Cần xem lại", ô đáp số trống (engine chưa nối cho câu tự soạn).
   */
  private static void fillAnswers(ObjectNode lesson, List<Exercise> bank) {
    java.util.Map<String, Exercise> byCode =
        bank.stream().collect(Collectors.toMap(Exercise::code, e -> e, (a, b) -> a));
    for (var ph : lesson.path("phieu_hoc_tap")) {
      for (var c : ph.path("cau")) {
        if (c instanceof ObjectNode q) {
          Exercise e = byCode.get(q.path("nguon").asString(""));
          if (e != null && e.answer() != null) {
            q.put("dap_so", e.answer());
            q.put("dap_so_nguon", "kho");
          } else {
            q.putNull("dap_so");
            q.put("dap_so_nguon", "can-xem-lai");
          }
        }
      }
    }
  }

  private String json(Object node) {
    return mapper.writeValueAsString(node);
  }
}
