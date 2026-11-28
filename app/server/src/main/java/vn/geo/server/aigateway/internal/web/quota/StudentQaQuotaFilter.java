package vn.geo.server.aigateway.internal.web.quota;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.aigateway.internal.InstrumentedAiGateway;
import vn.geo.server.aigateway.internal.service.quota.AiQuotaService;
import vn.geo.server.aigateway.internal.service.quota.AiQuotaService.ClassRef;
import vn.geo.server.aigateway.internal.web.ClassAiSwitchFilter;
import vn.geo.server.common.error.ApiError;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.RequestContext;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.web.RequestIdFilter;

/**
 * Hạn mức và tắt khẩn cho lượt hỏi trợ lý của học sinh trên bài thuộc lớp (US-S5E5-04-FC1, FC4,
 * SC4, SC5; BR-S5E5-12, 16). Chạy SAU công tắc lớp S3 ({@link ClassAiSwitchFilter}) và TRƯỚC bộ xử
 * lý hỏi đáp: tắt khẩn → FORBIDDEN "AI đang tắt" (như lớp tắt AI), lớp hết 300 hoặc em hết 30 lượt
 * → RATE_LIMITED kèm Retry-After, không gọi AI; nhận thì tính lượt. Sau lượt, cộng token thật đã
 * ghi ở ai_call_log vào trần token của lớp (cảnh báo 80 % một lần). Gửi đáp số không đi qua đây nên
 * vẫn chấm tự động bình thường. Bài tự học (không thuộc lớp) không tính hạn mức lớp.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 24)
public class StudentQaQuotaFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(StudentQaQuotaFilter.class);
  private static final Pattern ASK =
      Pattern.compile(
          "^/api/v1/student/assignments/([0-9a-fA-F-]{36})/questions$", Pattern.CASE_INSENSITIVE);

  private final AiQuotaService quota;
  private final JsonMapper mapper;

  public StudentQaQuotaFilter(AiQuotaService quota, JsonMapper mapper) {
    this.quota = quota;
    this.mapper = mapper;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !"POST".equalsIgnoreCase(request.getMethod())
        || !ASK.matcher(ClassAiSwitchFilter.normalizedPathOf(request)).matches();
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    if (!RequestContext.isAuthenticated() || RequestContext.principal().role() != Role.STUDENT) {
      chain.doFilter(request, response);
      return;
    }
    AuthPrincipal p = RequestContext.principal();
    Matcher m = ASK.matcher(ClassAiSwitchFilter.normalizedPathOf(request));
    UUID assignment;
    try {
      assignment = m.matches() ? UUID.fromString(m.group(1)) : null;
    } catch (IllegalArgumentException e) {
      assignment = null;
    }
    Optional<ClassRef> klass =
        assignment == null ? Optional.empty() : quota.classOfAsk(p.orgId(), p.userId(), assignment);
    if (klass.isEmpty()) {
      chain.doFilter(request, response);
      return;
    }
    ClassRef c = klass.get();
    try {
      quota.takeStudentQa(
          p.orgId(),
          c.id(),
          c.name(),
          p.userId(),
          request.getHeader(InstrumentedAiGateway.LANE_HEADER));
    } catch (AppException e) {
      write(response, e);
      return;
    }
    Instant start = Instant.now().minusMillis(5);
    chain.doFilter(request, response);
    try {
      quota.addTokensSince(p.orgId(), c.id(), p.userId(), start);
    } catch (RuntimeException e) {
      log.warn("không cộng được token AI của lớp: {}", e.toString());
    }
  }

  private void write(HttpServletResponse response, AppException e) throws IOException {
    response.setStatus(e.code().status().value());
    if (e.retryAfter() != null) {
      long s = Math.max(1, (e.retryAfter().toMillis() + 999) / 1000);
      response.setHeader(HttpHeaders.RETRY_AFTER, Long.toString(s));
    }
    response.setContentType("application/json;charset=UTF-8");
    List<ApiError.FieldIssue> details =
        e.retryAfter() == null
            ? List.of(new ApiError.FieldIssue("ai", "AI_OFF_SYSTEM"))
            : List.of();
    ApiError body =
        new ApiError(e.code().name(), e.getMessage(), MDC.get(RequestIdFilter.MDC_KEY), details);
    response.getOutputStream().write(mapper.writeValueAsBytes(body));
  }
}
