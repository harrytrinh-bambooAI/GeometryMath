package vn.geo.server.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.error.ErrorResponseWriter;

/**
 * Kiểm thẻ Bearer, đặt {@link RequestContext}. Đường dẫn công khai: PUBLIC_PREFIXES; móc dev/kiểm
 * thử ({@code /dev/}, {@code /test-hooks/}) chỉ công khai khi profile dev/test bật và prod không
 * bật. Ở prod các đường đó đi qua kiểm thẻ (không thẻ => 401) và không có bộ điều khiển nào phục
 * vụ.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class AuthFilter extends OncePerRequestFilter {

  static final List<String> PUBLIC_PREFIXES = List.of("/health", "/actuator", "/api/v1/auth/");
  static final List<String> DEV_ONLY_PREFIXES = List.of("/dev/", "/test-hooks/");

  private final TokenService tokens;
  private final ErrorResponseWriter writer;
  private final List<String> publicPrefixes;

  public AuthFilter(TokenService tokens, ErrorResponseWriter writer, Environment env) {
    this.tokens = tokens;
    this.writer = writer;
    this.publicPrefixes = publicPrefixes(env);
  }

  /** Đường công khai theo profile đang bật. */
  static List<String> publicPrefixes(Environment env) {
    boolean devHooks =
        env.acceptsProfiles(Profiles.of("dev | test")) && !env.acceptsProfiles(Profiles.of("prod"));
    if (!devHooks) {
      return PUBLIC_PREFIXES;
    }
    List<String> all = new ArrayList<>(PUBLIC_PREFIXES);
    all.addAll(DEV_ONLY_PREFIXES);
    return List.copyOf(all);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return publicPrefixes.stream().anyMatch(path::startsWith);
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
