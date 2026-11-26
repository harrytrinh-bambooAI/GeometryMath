package vn.geo.server.content.internal.export.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.common.error.ApiError;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.web.ClientIp;
import vn.geo.server.common.web.RequestIdFilter;
import vn.geo.server.content.internal.export.service.ShareLinkService;
import vn.geo.server.content.internal.export.service.SharePageExport;

/**
 * Đường công khai của liên kết chỉ xem: {@code GET /api/v1/share/{mã}} (US-S2E9-02-SC5, FC3…FC5).
 * Chạy TRƯỚC bộ kiểm thẻ (người mở không đăng nhập) và tự trả lời, không vào tầng MVC:
 *
 * <ul>
 *   <li>mã còn dùng được: chữ đề + cảnh hình (JSON; trình duyệt xin {@code text/html} thì nhận
 *       trang HTML tự chứa). Không lời giải, không tên hay mã giáo viên;
 *   <li>mã sai, hết hạn, đã thu hồi: cùng NOT_FOUND "Liên kết không còn dùng được" (BR-S2E9-10);
 *   <li>mọi phương thức ghi: FORBIDDEN (chỉ xem);
 *   <li>dò mã (BR-S2E9-09, PO-63): một địa chỉ mạng có {@code fail-limit} (100) lần mã sai trong
 *       cửa sổ {@code fail-window} (10 phút) thì mọi yêu cầu tiếp theo từ địa chỉ đó nhận
 *       RATE_LIMITED kèm Retry-After, kể cả với mã đúng. Bộ đếm giữ trong bộ nhớ của nút (như
 *       SolutionRateLimiter; nhiều nút thì chuyển sang Redis).
 * </ul>
 *
 * Đặt ở đây (bộ lọc riêng) vì danh sách đường công khai của AuthFilter nằm ngoài phạm vi tệp của
 * T-261126-25; đề nghị FIX thêm tiền tố {@value #PREFIX} vào AuthFilter rồi chuyển về bộ điều khiển
 * thường.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 15)
public class ShareLinkPublicFilter extends OncePerRequestFilter {

  public static final String PREFIX = "/api/v1/share/";

  private record Window(long startMs, int fails) {}

  private final ShareLinkService service;
  private final ClientIp clientIp;
  private final JsonMapper mapper;
  private final int failLimit;
  private final long windowMs;
  private final Map<String, Window> fails = new ConcurrentHashMap<>();

  public ShareLinkPublicFilter(
      ShareLinkService service,
      ClientIp clientIp,
      JsonMapper mapper,
      @Value("${geo.s2e9.share.fail-limit:100}") int failLimit,
      @Value("${geo.s2e9.share.fail-window:PT10M}") Duration failWindow) {
    this.service = service;
    this.clientIp = clientIp;
    this.mapper = mapper;
    this.failLimit = failLimit;
    this.windowMs = failWindow.toMillis();
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !request.getRequestURI().startsWith(PREFIX);
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
    response.setHeader("X-Robots-Tag", "noindex, nofollow");
    response.setHeader("Referrer-Policy", "no-referrer");
    String ip = clientIp.of(request);
    long now = System.currentTimeMillis();
    Window w = fails.get(ip);
    if (w != null && now - w.startMs() >= windowMs) {
      fails.remove(ip, w);
      w = null;
    }
    if (w != null && w.fails() >= failLimit) {
      long wait = Math.max(1, (windowMs - (now - w.startMs()) + 999) / 1000);
      response.setHeader(HttpHeaders.RETRY_AFTER, Long.toString(wait));
      error(response, ErrorCode.RATE_LIMITED, ErrorCode.RATE_LIMITED.defaultMessage());
      return;
    }
    String method = request.getMethod();
    if (!"GET".equals(method) && !"HEAD".equals(method)) {
      error(response, ErrorCode.FORBIDDEN, "Liên kết này chỉ để xem.");
      return;
    }
    String token = request.getRequestURI().substring(PREFIX.length());
    Optional<ShareLinkService.SharedView> view;
    try {
      view = token.contains("/") ? Optional.empty() : service.view(token);
    } catch (RuntimeException e) {
      error(response, ErrorCode.INTERNAL, ErrorCode.INTERNAL.defaultMessage());
      return;
    }
    if (view.isEmpty()) {
      fails.merge(ip, new Window(now, 1), (a, b) -> new Window(a.startMs(), a.fails() + 1));
      if (fails.size() > 10_000) {
        fails.entrySet().removeIf(e -> now - e.getValue().startMs() >= windowMs);
      }
      error(response, ErrorCode.NOT_FOUND, ShareLinkService.MSG_GONE);
      return;
    }
    ShareLinkService.SharedView v = view.get();
    String accept = request.getHeader(HttpHeaders.ACCEPT);
    if (accept != null && accept.contains("text/html")) {
      response.setStatus(200);
      response.setContentType("text/html;charset=UTF-8");
      response.setHeader(
          "Content-Security-Policy", "default-src 'none'; style-src 'unsafe-inline'");
      String html =
          SharePageExport.page(
              v.title(), v.statement(), v.sceneKind(), mapper.valueToTree(v.scene()));
      response.getOutputStream().write(html.getBytes(StandardCharsets.UTF_8));
      return;
    }
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("title", v.title());
    body.put("statement", v.statement());
    body.put("kind", v.kind());
    body.put("sceneKind", v.sceneKind());
    body.put("scene", v.scene());
    body.put("readOnly", true);
    response.setStatus(200);
    response.setContentType("application/json;charset=UTF-8");
    response.getOutputStream().write(mapper.writeValueAsBytes(body));
  }

  private void error(HttpServletResponse response, ErrorCode code, String message)
      throws IOException {
    response.setStatus(code.status().value());
    response.setContentType("application/json;charset=UTF-8");
    ApiError body = ApiError.of(code, message, MDC.get(RequestIdFilter.MDC_KEY));
    response.getOutputStream().write(mapper.writeValueAsBytes(body));
  }
}
