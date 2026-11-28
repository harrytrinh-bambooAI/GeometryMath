package vn.geo.server.subject.internal;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.subject.api.SubjectEngine;
import vn.geo.server.subject.api.SubjectEngine.ParsedProblem;
import vn.geo.server.subject.api.SubjectEngine.ProblemInput;
import vn.geo.server.subject.api.SubjectRegistry;

/**
 * Gọi thao tác của môn qua khung, có rào cách ly (US-S2E2-01-FC4): môn ném lỗi thì trả lỗi dịch vụ
 * (DEPENDENCY_UNAVAILABLE), quá thời gian thì TIMEOUT (D-GEO-143); môn khác vẫn phục vụ.
 */
@Service
public class SubjectService {

  private static final Logger LOG = LoggerFactory.getLogger(SubjectService.class);

  private final SubjectRegistry registry;
  private final Duration timeout;
  private final ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor();

  public SubjectService(
      SubjectRegistry registry, @Value("${geo.subject.timeout:5s}") Duration timeout) {
    this.registry = registry;
    this.timeout = timeout;
  }

  public SubjectRegistry registry() {
    return registry;
  }

  /** Phân tích rồi giải đề bằng môn {@code subjectId}; môn không có: NOT_FOUND. */
  public Solved solve(String subjectId, ProblemInput input) {
    SubjectEngine e = registry.require(subjectId);
    return guarded(
        e,
        x -> {
          ParsedProblem p = x.parse(input);
          return new Solved(p.kind().code(), x.solve(p));
        });
  }

  /** Phân tích rồi dựng cảnh. */
  public Rendered render(String subjectId, ProblemInput input) {
    SubjectEngine e = registry.require(subjectId);
    return guarded(
        e,
        x -> {
          ParsedProblem p = x.parse(input);
          return new Rendered(p.kind().code(), x.render(p), x.hint(p));
        });
  }

  /** Chạy một thao tác của môn trong rào cách ly (luồng riêng, giới hạn thời gian). */
  public <T> T guarded(SubjectEngine e, Function<SubjectEngine, T> op) {
    Future<T> f = pool.submit(() -> op.apply(e));
    try {
      return f.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
    } catch (TimeoutException ex) {
      f.cancel(true);
      LOG.warn("Môn {} quá thời gian {}", e.id(), timeout);
      throw new AppException(ErrorCode.TIMEOUT);
    } catch (ExecutionException ex) {
      if (ex.getCause() instanceof AppException app) {
        throw app;
      }
      LOG.warn("Môn {} lỗi khi xử lý: {}", e.id(), String.valueOf(ex.getCause()));
      throw new AppException(ErrorCode.DEPENDENCY_UNAVAILABLE);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new AppException(ErrorCode.DEPENDENCY_UNAVAILABLE);
    }
  }

  /** Kết quả giải: loại đề và lời giải của engine. */
  public record Solved(String kind, SubjectEngine.Solution solution) {}

  /** Kết quả dựng: loại đề, cảnh, gợi ý. */
  public record Rendered(
      String kind, SubjectEngine.Rendering rendering, List<SubjectEngine.Hint> hints) {}

  /** Danh mục môn dựng một lần lúc khởi động; môn lỗi làm khởi động thất bại (BR-S2E2-02). */
  @Configuration
  static class Config {
    @Bean
    SubjectRegistry subjectRegistry(List<SubjectEngine> engines) {
      return new SubjectRegistry(engines);
    }
  }
}
