package vn.geo.server.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.error.ErrorResponseWriter;

/**
 * Kiểm thẻ Bearer, đặt {@link RequestContext}. Đường dẫn công khai cấu hình trong PUBLIC_PREFIXES.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class AuthFilter extends OncePerRequestFilter {

  static final List<String> PUBLIC_PREFIXES = List.of("/health", "/actuator", "/dev/");

  private final TokenService tokens;
  private final ErrorResponseWriter writer;

  public AuthFilter(TokenService tokens, ErrorResponseWriter writer) {
    this.tokens = tokens;
    this.writer = writer;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return PUBLIC_PREFIXES.stream().anyMatch(path::startsWith);
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String header = request.getHeader("Authorization");
    Optional<AuthPrincipal> principal =
        header != null && header.startsWith("Bearer ")
            ? tokens.verify(header.substring(7).trim())
            : Optional.empty();
    if (principal.isEmpty()) {
      writer.write(response, ErrorCode.UNAUTHENTICATED);
      return;
    }
    try {
      ScopedValue.where(RequestContext.PRINCIPAL, principal.get())
          .call(() -> {
            chain.doFilter(request, response);
            return null;
          });
    } catch (IOException | ServletException | RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new ServletException(e);
    }
  }
}
