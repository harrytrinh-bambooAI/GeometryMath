package vn.geo.server.iam.internal.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.common.error.ApiError;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.security.RequestContext;
import vn.geo.server.common.web.RequestIdFilter;
import vn.geo.server.iam.internal.service.PrivacyService;

/**
 * US-S3E5-02-FC3 (BR-S3E5-24, QA-S3E5-04): tài khoản có mã chưa bấm "Đã đọc" bản hiện hành thì mọi
 * API (trừ đăng nhập và tài khoản của tôi) trả FORBIDDEN, {@code details} nêu cần đọc thông báo.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 22)
public class PrivacyGateFilter extends OncePerRequestFilter {

  public static final String REASON = "PRIVACY_ACK_REQUIRED";

  private final PrivacyService privacy;
  private final JsonMapper mapper;

  public PrivacyGateFilter(PrivacyService privacy, JsonMapper mapper) {
    this.privacy = privacy;
    this.mapper = mapper;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return !path.startsWith("/api/")
        || path.startsWith("/api/v1/auth/")
        || path.startsWith("/api/v1/account/");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    if (RequestContext.isAuthenticated() && privacy.required(RequestContext.principal())) {
      response.setStatus(ErrorCode.FORBIDDEN.status().value());
      response.setContentType("application/json;charset=UTF-8");
      ApiError body =
          new ApiError(
              ErrorCode.FORBIDDEN.name(),
              PrivacyService.MSG_84,
              MDC.get(RequestIdFilter.MDC_KEY),
              List.of(new ApiError.FieldIssue("privacy", REASON)));
      response.getOutputStream().write(mapper.writeValueAsBytes(body));
      return;
    }
    chain.doFilter(request, response);
  }
}
