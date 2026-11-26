package vn.geo.server.aigateway.internal;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import vn.geo.server.aigateway.api.AiGateway;
import vn.geo.server.aigateway.api.AiRequest;
import vn.geo.server.aigateway.api.AiResponse;
import vn.geo.server.aigateway.internal.AiProvider.ProviderCall;
import vn.geo.server.aigateway.internal.AiProvider.Simulation;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.id.PlatformOrg;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.RequestContext;

/**
 * Cổng AI duy nhất (BR-GEO-106): gọi nhà cung cấp với hết hạn chờ, ghi đúng một bản ghi ai_call_log
 * cho mỗi lượt kể cả lượt lỗi (BR-GEO-107), đo số lần gọi (geo.ai.calls) và độ trễ
 * (geo.ai.latency). Nhà cung cấp chỉ nhận mục đích, model và nội dung, không nhận định danh
 * (BR-GEO-108). Cổng không tự thử lại (D-GEO-072): người dùng bấm "Thử lại"; nhà cung cấp thật chỉ
 * thử lại có giới hạn lỗi tạm thời (429/5xx) bên trong trần {@code geo.ai.timeout}. Token và chi
 * phí lấy từ usage thật khi nhà cung cấp có (AnthropicAiProvider), ngược lại ước lượng.
 */
@Service
public class InstrumentedAiGateway implements AiGateway {

  /** Tiêu đề chọn "làn" kiểm thử cho chế độ của nhà cung cấp giả lập (chỉ ý nghĩa ở dev/test). */
  public static final String LANE_HEADER = "X-Geo-Test-Lane";

  private static final Logger log = LoggerFactory.getLogger(InstrumentedAiGateway.class);
  private static final Set<String> PURPOSES = Set.of("scene", "solution", "hint", "qa", "lecture");

  private final AiProvider provider;
  private final AiSettings settings;
  private final MeterRegistry meters;
  private final AiCallLog callLog;
  private final MockAiControl control;
  private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

  public InstrumentedAiGateway(
      AiProvider provider,
      AiSettings settings,
      MeterRegistry meters,
      AiCallLog callLog,
      MockAiControl control) {
    this.provider = provider;
    this.settings = settings;
    this.meters = meters;
    this.callLog = callLog;
    this.control = control;
  }

  @Override
  public String model() {
    return settings.model();
  }

  @Override
  public String promptVersion() {
    return settings.promptVersion();
  }

  @Override
  public AiResponse complete(AiRequest request) {
    long start = System.nanoTime();
    String outcome = "success";
    AuthPrincipal who = RequestContext.isAuthenticated() ? RequestContext.principal() : null;
    String model = settings.model();
    String promptVersion = settings.promptVersion();
    Simulation sim =
        control.simulationFor(who == null ? null : who.userId().toString(), currentLane());
    String scenario =
        control.scenarioFor(who == null ? null : who.userId().toString(), currentLane());
    long delay = control.delayFor(who == null ? null : who.userId().toString(), currentLane());
    ProviderCall call =
        new ProviderCall(request.task(), model, request.prompt(), sim, scenario, delay);
    Future<AiProvider.Result> future = executor.submit(() -> provider.call(call));
    String content = "";
    AiProvider.Usage usage = null;
    String servedModel = null;
    try {
      AiProvider.Result res = future.get(settings.timeout().toMillis(), TimeUnit.MILLISECONDS);
      content = res.content() == null ? "" : res.content();
      usage = res.usage();
      servedModel = res.model();
      return new AiResponse(content, provider.name(), elapsedMillis(start));
    } catch (TimeoutException e) {
      future.cancel(true);
      outcome = "timeout";
      throw new AppException(ErrorCode.TIMEOUT, "AI phản hồi quá thời gian cho phép.", e);
    } catch (ExecutionException e) {
      if (e.getCause() instanceof java.net.http.HttpTimeoutException) {
        outcome = "timeout"; // nhà cung cấp thật hết giờ ở mọi lần thử: cùng lỗi quá thời gian
        throw new AppException(ErrorCode.TIMEOUT, "AI phản hồi quá thời gian cho phép.", e);
      }
      outcome = "error";
      // không đưa thông điệp của nhà cung cấp ra ngoài (có thể chứa nội dung phản hồi thô)
      log.warn(
          "nhà cung cấp AI lỗi: provider={} task={} lỗi={}",
          provider.name(),
          request.task(),
          e.getCause() == null ? "?" : e.getCause().getClass().getSimpleName());
      throw new AppException(ErrorCode.DEPENDENCY_UNAVAILABLE, "AI tạm thời chưa sẵn sàng.", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      outcome = "error";
      throw new AppException(ErrorCode.INTERNAL, ErrorCode.INTERNAL.defaultMessage(), e);
    } finally {
      long ms = elapsedMillis(start);
      record(
          who,
          request,
          servedModel == null || servedModel.isBlank() ? model : servedModel,
          promptVersion,
          content,
          usage,
          ms,
          outcome);
      meters
          .counter(
              "geo.ai.calls",
              "provider",
              provider.name(),
              "task",
              request.task(),
              "outcome",
              outcome)
          .increment();
      Timer.builder("geo.ai.latency")
          .tag("provider", provider.name())
          .tag("outcome", outcome)
          .register(meters)
          .record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
    }
  }

  private void record(
      AuthPrincipal who,
      AiRequest request,
      String model,
      String promptVersion,
      String content,
      AiProvider.Usage usage,
      long ms,
      String outcome) {
    // "ocr" (E4: đọc chữ từ ảnh đề để dựng hình, US-GEO-402) ghi mục đích 'scene' của ai_purpose
    String purpose =
        "ocr".equals(request.task())
            ? "scene"
            : PURPOSES.contains(request.task()) ? request.task() : "qa";
    String status =
        switch (outcome) {
          case "success" -> "ok";
          case "timeout" -> "timeout";
          default -> "error";
        };
    UUID org = who == null ? PlatformOrg.ID : who.orgId();
    try {
      callLog.record(
          new AiCallLog.Entry(
              org,
              who == null ? null : who.userId(),
              purpose,
              model,
              promptVersion,
              usage == null
                  ? tokens(request.prompt())
                  : usage.inputTokens() + usage.cacheReadTokens() + usage.cacheWriteTokens(),
              usage == null ? tokens(content) : usage.outputTokens(),
              !"ok".equals(status) ? 0L : settings.costMicro(model, usage),
              ms,
              status));
    } catch (RuntimeException e) {
      log.warn("không ghi được ai_call_log: {}", e.toString());
    }
  }

  /** Ước lượng token (≈ 4 ký tự một token) khi nhà cung cấp không trả số token thật (mock). */
  private static int tokens(String s) {
    return s == null ? 0 : (s.length() + 3) / 4;
  }

  private static String currentLane() {
    if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes a) {
      return a.getRequest().getHeader(LANE_HEADER);
    }
    return null;
  }

  private static long elapsedMillis(long startNanos) {
    return (System.nanoTime() - startNanos) / 1_000_000;
  }
}
