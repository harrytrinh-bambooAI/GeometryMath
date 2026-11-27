package vn.geo.server.aigateway.internal.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UrlPathHelper;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.common.error.ApiError;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.RequestContext;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.web.RequestIdFilter;
import vn.geo.server.iam.api.ClassAiPolicy;

/**
 * Công tắc trợ lý AI theo lớp phía học sinh (US-S3E5-04, BR-S3E5-41, 42): mỗi lượt hỏi trợ lý của
 * học sinh trên bài thuộc lớp đã tắt bị chặn TRƯỚC khi tới cổng AI — FORBIDDEN, {@code details}
 * AI_OFF_FOR_CLASS, không lượt gọi nhà cung cấp, không bản ghi ai_call_log. Kiểm ở máy chủ mỗi lượt
 * (không dựa vào giao diện); lượt đang chạy khi giáo viên tắt vẫn hoàn tất (BR-S3E5-45). Phía giáo
 * viên ("AI gộp") không đi qua bộ lọc này (BR-S3E5-46).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 23)
public class ClassAiSwitchFilter extends OncePerRequestFilter {

  public static final String REASON = "AI_OFF_FOR_CLASS";
  public static final String MSG_83 =
      "Lớp này đã tắt trợ lý AI. Em vẫn làm bài và xem gợi ý có sẵn.";

  private static final Pattern ASK =
      Pattern.compile(
          "^/api/v1/student/assignments/([0-9a-fA-F-]{36})/questions$", Pattern.CASE_INSENSITIVE);

  /**
   * Dev-REV-A1: so khớp trên đường đã chuẩn hóa như Spring MVC thấy khi chọn bộ xử lý — giải mã
   * %xx, bỏ tham số đường dẫn {@code ;...} ở mọi đoạn, gộp {@code //}, bỏ {@code /} cuối. Khớp trên
   * {@code getRequestURI()} thô thì {@code …/questions;x=1} hay {@code …/%71uestions} né được công
   * tắc mà vẫn tới bộ xử lý hỏi đáp.
   */
  private static final UrlPathHelper PATHS = new UrlPathHelper();

  static {
    PATHS.setRemoveSemicolonContent(true);
    PATHS.setUrlDecode(true);
  }

  private final ClassAiPolicy policy;
  private final JsonMapper mapper;

  public ClassAiSwitchFilter(ClassAiPolicy policy, JsonMapper mapper) {
    this.policy = policy;
    this.mapper = mapper;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !"POST".equalsIgnoreCase(request.getMethod())
        || !ASK.matcher(normalizedPath(request)).matches();
  }

  static String normalizedPath(HttpServletRequest request) {
    String path = PATHS.getPathWithinApplication(request).replaceAll("/{2,}", "/");
    return path.length() > 1 && path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    if (RequestContext.isAuthenticated()) {
      AuthPrincipal p = RequestContext.principal();
      Matcher m = ASK.matcher(normalizedPath(request));
      if (p.role() == Role.STUDENT && m.matches()) {
        UUID assignment;
        try {
          assignment = UUID.fromString(m.group(1));
        } catch (IllegalArgumentException e) {
          chain.doFilter(request, response);
          return;
        }
        if (policy.blockedForStudent(p.orgId(), p.userId(), assignment)) {
          response.setStatus(ErrorCode.FORBIDDEN.status().value());
          response.setContentType("application/json;charset=UTF-8");
          ApiError body =
              new ApiError(
                  ErrorCode.FORBIDDEN.name(),
                  MSG_83,
                  MDC.get(RequestIdFilter.MDC_KEY),
                  List.of(new ApiError.FieldIssue("ai", REASON)));
          response.getOutputStream().write(mapper.writeValueAsBytes(body));
          return;
        }
      }
    }
    chain.doFilter(request, response);
  }
}
