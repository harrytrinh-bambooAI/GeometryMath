package vn.geo.server.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.geo.server.common.id.Uuid7;

/** Gán mã yêu cầu (X-Request-Id), đưa vào MDC cho nhật ký và ghi một dòng nhật ký truy cập. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

  public static final String HEADER = "X-Request-Id";
  public static final String MDC_KEY = "requestId";
  private static final Pattern SAFE = Pattern.compile("[A-Za-z0-9_-]{8,64}");
  private static final Logger access = LoggerFactory.getLogger("geo.access");

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String incoming = request.getHeader(HEADER);
    String id =
        incoming != null && SAFE.matcher(incoming).matches() ? incoming : Uuid7.next().toString();
    MDC.put(MDC_KEY, id);
    response.setHeader(HEADER, id);
    long start = System.nanoTime();
    try {
      chain.doFilter(request, response);
    } finally {
      long ms = (System.nanoTime() - start) / 1_000_000;
      String path = request.getRequestURI();
      if (path.startsWith("/health") || path.startsWith("/actuator")) {
        access.debug("{} {} {} {}ms", request.getMethod(), path, response.getStatus(), ms);
      } else {
        access.info("{} {} {} {}ms", request.getMethod(), path, response.getStatus(), ms);
      }
      MDC.remove(MDC_KEY);
    }
  }
}
