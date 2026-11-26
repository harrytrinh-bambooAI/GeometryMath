package vn.geo.server.iam.internal.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.error.ErrorResponseWriter;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.RequestContext;
import vn.geo.server.iam.internal.service.SessionRevocations;

/**
 * Chạy NGAY SAU bộ lọc kiểm thẻ của khung: thẻ đã đúng chữ ký nhưng phát trước mốc hủy phiên của
 * người dùng (đổi mật khẩu bằng mã khôi phục, QA-S2E1-01) thì trả UNAUTHENTICATED như thẻ hết hạn.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 21)
public class SessionGuardFilter extends OncePerRequestFilter {

  private final SessionRevocations revocations;
  private final ErrorResponseWriter writer;
  private final JsonMapper mapper;

  public SessionGuardFilter(
      SessionRevocations revocations, ErrorResponseWriter writer, JsonMapper mapper) {
    this.revocations = revocations;
    this.writer = writer;
    this.mapper = mapper;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    if (RequestContext.isAuthenticated()) {
      AuthPrincipal p = RequestContext.principal();
      long iat = issuedAt(request.getHeader("Authorization"));
      if (!revocations.isValid(p.userId(), iat)) {
        writer.write(response, ErrorCode.UNAUTHENTICATED);
        return;
      }
    }
    chain.doFilter(request, response);
  }

  /** iat của thẻ Bearer (chữ ký đã được khung kiểm); không đọc được thì coi như rất cũ. */
  private long issuedAt(String header) {
    try {
      String token = header.substring(7).trim();
      String payload = token.split("\\.", -1)[1];
      Map<?, ?> claims = mapper.readValue(Base64.getUrlDecoder().decode(payload), Map.class);
      return ((Number) claims.get("iat")).longValue();
    } catch (RuntimeException e) {
      return Long.MIN_VALUE + 1;
    }
  }
}
