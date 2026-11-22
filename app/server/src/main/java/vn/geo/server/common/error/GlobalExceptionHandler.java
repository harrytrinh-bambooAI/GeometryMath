package vn.geo.server.common.error;

import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import vn.geo.server.common.web.BodyTooLargeException;
import vn.geo.server.common.web.HttpProperties;
import vn.geo.server.common.web.RequestIdFilter;

/** Mô hình lỗi thống nhất: mọi lỗi ra ngoài đều là {@link ApiError}, không lộ chi tiết nội bộ. */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private final Duration defaultRetryAfter;

  public GlobalExceptionHandler(HttpProperties http) {
    this.defaultRetryAfter = http.retryAfter();
  }

  @ExceptionHandler(AppException.class)
  ResponseEntity<Object> handleApp(AppException ex) {
    if (ex.code().status().is5xxServerError()) {
      log.warn("Lỗi ứng dụng {}: {}", ex.code(), ex.getMessage(), ex);
    } else {
      log.debug("Lỗi ứng dụng {}: {}", ex.code(), ex.getMessage());
    }
    return build(ex.code(), ex.getMessage(), List.of(), ex.retryAfter());
  }

  @ExceptionHandler({QueryTimeoutException.class})
  ResponseEntity<Object> handleDbTimeout(QueryTimeoutException ex) {
    log.warn("Truy vấn quá thời gian: {}", ex.getMessage());
    return build(ErrorCode.TIMEOUT, ErrorCode.TIMEOUT.defaultMessage(), List.of());
  }

  /**
   * Hết thời gian chờ lấy kết nối từ nhóm (Hikari connection-timeout 2 giây, CONFIG.md; Hikari ném
   * {@link java.sql.SQLTransientConnectionException}): thất bại nhanh bằng 503 SERVICE_OVERLOADED
   * kèm Retry-After (ADR-GEO-04, NFR-GEO-055). Lỗi tài nguyên CSDL khác: 503
   * DEPENDENCY_UNAVAILABLE.
   */
  @ExceptionHandler(DataAccessResourceFailureException.class)
  ResponseEntity<Object> handleDbDown(DataAccessResourceFailureException ex) {
    for (Throwable t = ex; t != null; t = t.getCause()) {
      if (t instanceof java.sql.SQLTransientConnectionException) {
        log.warn("Không lấy được kết nối CSDL trong thời hạn: {}", ex.getMessage());
        return build(
            ErrorCode.SERVICE_OVERLOADED,
            ErrorCode.SERVICE_OVERLOADED.defaultMessage(),
            List.of(),
            null);
      }
    }
    log.error("Cơ sở dữ liệu không sẵn sàng", ex);
    return build(
        ErrorCode.DEPENDENCY_UNAVAILABLE,
        ErrorCode.DEPENDENCY_UNAVAILABLE.defaultMessage(),
        List.of());
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<Object> handleAny(Exception ex) {
    log.error("Lỗi không lường trước", ex);
    return build(ErrorCode.INTERNAL, ErrorCode.INTERNAL.defaultMessage(), List.of());
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    List<ApiError.FieldIssue> issues =
        ex.getBindingResult().getFieldErrors().stream()
            .map(f -> new ApiError.FieldIssue(f.getField(), String.valueOf(f.getDefaultMessage())))
            .toList();
    return build(ErrorCode.VALIDATION_FAILED, ErrorCode.VALIDATION_FAILED.defaultMessage(), issues);
  }

  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    for (Throwable t = ex; t != null; t = t.getCause()) {
      if (t instanceof BodyTooLargeException) {
        return build(
            ErrorCode.PAYLOAD_TOO_LARGE, ErrorCode.PAYLOAD_TOO_LARGE.defaultMessage(), List.of());
      }
    }
    return build(ErrorCode.VALIDATION_FAILED, "Nội dung gửi lên không đọc được.", List.of());
  }

  /** Mọi lỗi còn lại của khung Spring (404, 405, 415, thiếu tham số...) đi qua đây. */
  @Override
  protected ResponseEntity<Object> handleExceptionInternal(
      Exception ex, Object body, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
    ErrorCode code = ErrorCode.forStatus(status.value());
    return build(code, code.defaultMessage(), List.of());
  }

  private ResponseEntity<Object> build(
      ErrorCode code, String message, List<ApiError.FieldIssue> details) {
    return build(code, message, details, null);
  }

  private ResponseEntity<Object> build(
      ErrorCode code, String message, List<ApiError.FieldIssue> details, Duration retryAfter) {
    ApiError body = new ApiError(code.name(), message, MDC.get(RequestIdFilter.MDC_KEY), details);
    ResponseEntity.BodyBuilder b = ResponseEntity.status(code.status());
    if (code.retryable()) {
      b.header(HttpHeaders.RETRY_AFTER, retryAfterSeconds(retryAfter, defaultRetryAfter));
    }
    return b.body(body);
  }

  /** Giây nguyên, làm tròn lên, tối thiểu 1 (RFC 9110 §10.2.3). */
  public static String retryAfterSeconds(Duration value, Duration fallback) {
    Duration d = value != null ? value : fallback;
    long ms = Math.max(1, d.toMillis());
    return Long.toString(Math.max(1, (ms + 999) / 1000));
  }
}
