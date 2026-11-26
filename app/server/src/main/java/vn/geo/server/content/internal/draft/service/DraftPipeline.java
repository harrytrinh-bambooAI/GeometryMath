package vn.geo.server.content.internal.draft.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.LongSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import vn.geo.server.aigateway.api.AiGateway;
import vn.geo.server.aigateway.api.AiRequest;
import vn.geo.server.aigateway.api.AiResponse;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.RequestContext;
import vn.geo.server.content.internal.draft.repo.DraftRepository;
import vn.geo.server.content.internal.draft.repo.DraftRepository.DraftRow;
import vn.geo.server.content.internal.draft.repo.DraftRepository.JobRow;

/**
 * S2-E3 (US-S2E3-03, 04): tác vụ bốn bước Đọc đề → Dựng hình → Giải → Kiểm chứng chạy ở máy chủ
 * (BR-S2E3-13), tuần tự; bước lỗi dừng các bước sau (BR-S2E3-11). Mỗi bước có hạn 30 giây đo bằng
 * đồng hồ tiêm được (BR-S2E3-12, QA-S2E3-05); quá hạn = lỗi TIMEOUT, kết quả các bước đã xong được
 * giữ, "Thử lại bước này" chỉ chạy lại từ bước lỗi. Trạng thái từng bước lưu ở {@code
 * problem_draft_job.stages}; tác vụ bị hủy không ghi đè được (cập nhật có điều kiện {@code state =
 * running}). AI chỉ qua {@link AiGateway} (giả lập trong kiểm thử, D-GEO-047).
 */
@Service
public class DraftPipeline {

  private static final Logger log = LoggerFactory.getLogger(DraftPipeline.class);

  /** Hạn một bước (BR-S2E3-12). */
  public static final long STAGE_LIMIT_MS = 30_000;

  private final DraftRepository drafts;
  private final DraftStages stages;
  private final JsonMapper mapper;
  private final AiGateway defaultAi;
  private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
  private final Map<UUID, Future<?>> running = new ConcurrentHashMap<>();
  private volatile LongSupplier clock = System::currentTimeMillis;
  private volatile Function<AiRequest, AiResponse> ai;

  public DraftPipeline(
      DraftRepository drafts, DraftStages stages, JsonMapper mapper, AiGateway gateway) {
    this.drafts = drafts;
    this.stages = stages;
    this.mapper = mapper;
    this.defaultAi = gateway;
    this.ai = gateway::complete;
  }

  /** Chỉ dùng trong kiểm thử: đồng hồ giả (ms) và AI giả lập riêng; null = mặc định. */
  public void overrideForTest(LongSupplier testClock, Function<AiRequest, AiResponse> testAi) {
    this.clock = testClock == null ? System::currentTimeMillis : testClock;
    this.ai = testAi == null ? defaultAi::complete : testAi;
  }

  /** Bốn bước ở trạng thái chờ. */
  ArrayNode freshStages() {
    ArrayNode arr = mapper.createArrayNode();
    for (int i = 0; i < 4; i++) {
      ObjectNode s = mapper.createObjectNode();
      s.put("key", DraftViews.STAGE_KEYS.get(i));
      s.put("state", "waiting");
      s.put("aiCalls", 0);
      arr.add(s);
    }
    return arr;
  }

  /**
   * Bắt đầu tác vụ cho nháp; nháp đã có tác vụ đang chạy thì trả tác vụ đó (bấm "Dựng hình" hai lần
   * chỉ tạo một tác vụ, BR-S2E3-13).
   */
  public JobRow start(DraftRow d, AuthPrincipal me) {
    UUID id = vn.geo.server.common.id.Uuid7.next();
    boolean created;
    try {
      created = drafts.insertJob(id, d.id(), me.userId(), mapper.writeValueAsString(freshStages()));
    } catch (org.springframework.dao.DataIntegrityViolationException e) {
      created = false;
    }
    if (!created) {
      return drafts.lastJob(d.id()).orElseThrow(() -> new AppException(ErrorCode.CONFLICT));
    }
    drafts.setStatus(d.id(), "building");
    submit(id, 0, me);
    return drafts.job(id).orElseThrow();
  }

  /** "Thử lại bước này": chạy lại từ bước lỗi; các bước đã xong giữ nguyên (BR-S2E3-12). */
  public JobRow retry(JobRow job, AuthPrincipal me) {
    if (!"failed".equals(job.state())) {
      throw new AppException(ErrorCode.CONFLICT, "Tác vụ không ở trạng thái lỗi");
    }
    ArrayNode st = (ArrayNode) mapper.readTree(job.stagesJson());
    int from = 0;
    while (from < 4 && "done".equals(st.get(from).path("state").asString())) {
      from++;
    }
    for (int i = from; i < 4; i++) {
      ObjectNode s = (ObjectNode) st.get(i);
      s.put("state", "waiting");
      s.remove("error");
      s.remove("note");
    }
    if (!drafts.updateJob(
        job.id(), "failed", "running", mapper.writeValueAsString(st), job.outcomeJson())) {
      throw new AppException(ErrorCode.CONFLICT, "Tác vụ đã đổi trạng thái");
    }
    drafts.setStatus(job.draftId(), "building");
    submit(job.id(), from, me);
    return drafts.job(job.id()).orElseThrow();
  }

  /** "Hủy": tác vụ dừng, trạng thái "đã hủy"; nháp trở lại nháp chữ (US-S2E3-03-SC5). */
  public JobRow cancel(JobRow job) {
    if ("running".equals(job.state())) {
      ArrayNode st = (ArrayNode) mapper.readTree(job.stagesJson());
      for (JsonNode s : st) {
        if ("running".equals(s.path("state").asString())) {
          ((ObjectNode) s).put("state", "waiting");
        }
      }
      if (drafts.updateJob(
          job.id(), "running", "cancelled", mapper.writeValueAsString(st), job.outcomeJson())) {
        drafts.setStatus(job.draftId(), "editing");
        Future<?> f = running.remove(job.id());
        if (f != null) {
          f.cancel(true);
        }
      }
    }
    return drafts.job(job.id()).orElseThrow();
  }

  private void submit(UUID jobId, int from, AuthPrincipal me) {
    Future<?> f =
        executor.submit(() -> {
          try {
            RequestContext.callAs(
                me,
                () -> {
                  run(jobId, from);
                  return null;
                });
          } catch (RuntimeException e) {
            log.warn("tác vụ soạn nháp {} lỗi: {}", jobId, e.toString());
          } finally {
            running.remove(jobId);
          }
        });
    running.put(jobId, f);
  }

  /** Chạy các bước từ {@code from}; dừng khi tác vụ không còn "running" (đã hủy). */
  void run(UUID jobId, int from) {
    for (int i = from; i < 4; i++) {
      JobRow job = drafts.job(jobId).orElse(null);
      if (job == null || !"running".equals(job.state())) {
        return;
      }
      ArrayNode st = (ArrayNode) mapper.readTree(job.stagesJson());
      ObjectNode outcome = (ObjectNode) mapper.readTree(job.outcomeJson());
      ObjectNode stage = (ObjectNode) st.get(i);
      stage.put("state", "running");
      if (i < 3) {
        stage.put("aiCalls", stage.path("aiCalls").asInt() + 1);
      }
      if (!drafts.updateJob(
          jobId,
          "running",
          "running",
          mapper.writeValueAsString(st),
          mapper.writeValueAsString(outcome))) {
        return;
      }
      DraftRow draft = drafts.draft(job.draftId()).orElseThrow();
      final int idx = i;
      long t0 = clock.getAsLong();
      String error = null;
      String note = null;
      Future<String> f = null;
      try {
        AuthPrincipal me = RequestContext.principal();
        Function<AiRequest, AiResponse> aiNow = ai;
        f =
            executor.submit(
                () -> RequestContext.callAs(me, () -> stages.run(idx, draft, outcome, aiNow)));
        try {
          note = f.get(STAGE_LIMIT_MS + 1_000, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
          f.cancel(true);
          error = "TIMEOUT";
        }
        if (error == null && clock.getAsLong() - t0 > STAGE_LIMIT_MS) {
          error = "TIMEOUT";
        }
      } catch (InterruptedException e) {
        f.cancel(true);
        Thread.currentThread().interrupt();
        return; // bị hủy
      } catch (java.util.concurrent.ExecutionException e) {
        Throwable c = e.getCause();
        if (c instanceof DraftStages.Unbuildable u) {
          error = "UNBUILDABLE";
          note = u.getMessage();
        } else if (c instanceof AppException a && a.code() == ErrorCode.TIMEOUT) {
          error = "TIMEOUT";
        } else {
          error = "ERROR";
          log.info(
              "bước {} của tác vụ {} lỗi: {}",
              DraftViews.STAGE_KEYS.get(idx),
              jobId,
              String.valueOf(c));
        }
      }
      if (error != null) {
        stage.put("state", "error");
        stage.put("error", error);
        stage.put(
            "note",
            switch (error) {
              case "TIMEOUT" -> "Quá 30 giây";
              case "UNBUILDABLE" -> note == null ? "Máy không dựng được hình" : note;
              default -> "Bước này lỗi";
            });
        if (drafts.updateJob(
            jobId,
            "running",
            "failed",
            mapper.writeValueAsString(st),
            mapper.writeValueAsString(outcome))) {
          drafts.setStatus(draft.id(), "UNBUILDABLE".equals(error) ? "ai_figure" : "editing");
        }
        return;
      }
      stage.put("state", "done");
      if (note != null) {
        stage.put("note", note);
      }
      boolean last = i == 3;
      if (!drafts.updateJob(
          jobId,
          "running",
          last ? "done" : "running",
          mapper.writeValueAsString(st),
          mapper.writeValueAsString(outcome))) {
        return;
      }
      if (last) {
        drafts.setStatus(draft.id(), "ready");
      }
    }
  }
}
