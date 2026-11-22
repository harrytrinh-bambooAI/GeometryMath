package vn.geo.server.aigateway.internal;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.stereotype.Service;
import vn.geo.server.aigateway.api.AiGateway;
import vn.geo.server.aigateway.api.AiRequest;
import vn.geo.server.aigateway.api.AiResponse;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;

/** Gọi nhà cung cấp với hết hạn chờ, ghi số lần gọi (geo.ai.calls) và độ trễ (geo.ai.latency). */
@Service
public class InstrumentedAiGateway implements AiGateway {

  private final AiProvider provider;
  private final AiProperties props;
  private final MeterRegistry meters;
  private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

  public InstrumentedAiGateway(AiProvider provider, AiProperties props, MeterRegistry meters) {
    this.provider = provider;
    this.props = props;
    this.meters = meters;
  }

  @Override
  public AiResponse complete(AiRequest request) {
    long start = System.nanoTime();
    String outcome = "success";
    Future<String> future = executor.submit(() -> provider.complete(request));
    try {
      String content = future.get(props.timeout().toMillis(), TimeUnit.MILLISECONDS);
      return new AiResponse(content, provider.name(), elapsedMillis(start));
    } catch (TimeoutException e) {
      future.cancel(true);
      outcome = "timeout";
      throw new AppException(ErrorCode.TIMEOUT, "AI phản hồi quá thời gian cho phép.", e);
    } catch (ExecutionException e) {
      outcome = "error";
      throw new AppException(ErrorCode.DEPENDENCY_UNAVAILABLE, "AI tạm thời chưa sẵn sàng.", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      outcome = "error";
      throw new AppException(ErrorCode.INTERNAL, ErrorCode.INTERNAL.defaultMessage(), e);
    } finally {
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

  private static long elapsedMillis(long startNanos) {
    return (System.nanoTime() - startNanos) / 1_000_000;
  }
}
