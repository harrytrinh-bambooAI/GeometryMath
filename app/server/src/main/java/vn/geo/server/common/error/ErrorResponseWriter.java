package vn.geo.server.common.error;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.common.web.HttpProperties;
import vn.geo.server.common.web.RequestIdFilter;

/** Ghi ApiError trực tiếp ra phản hồi, cho các bộ lọc chạy trước tầng MVC. */
@Component
public class ErrorResponseWriter {

  private final JsonMapper mapper;
  private final HttpProperties http;

  public ErrorResponseWriter(JsonMapper mapper, HttpProperties http) {
    this.mapper = mapper;
    this.http = http;
  }

  public void write(HttpServletResponse response, ErrorCode code) throws IOException {
    response.setStatus(code.status().value());
    if (code.retryable()) {
      response.setHeader(
          HttpHeaders.RETRY_AFTER,
          GlobalExceptionHandler.retryAfterSeconds(null, http.retryAfter()));
    }
    response.setContentType("application/json;charset=UTF-8");
    ApiError body = ApiError.of(code, code.defaultMessage(), MDC.get(RequestIdFilter.MDC_KEY));
    response.getOutputStream().write(mapper.writeValueAsBytes(body));
  }
}
