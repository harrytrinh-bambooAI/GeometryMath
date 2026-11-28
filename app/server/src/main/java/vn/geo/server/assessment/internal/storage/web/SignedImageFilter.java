package vn.geo.server.assessment.internal.storage.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.geo.server.assessment.internal.storage.service.ImageStoreService;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.error.ErrorResponseWriter;

/**
 * Tải ảnh bằng URL ký (BR-S5E4-26): {@code GET /api/v1/files/images/{id}?o=&exp=&sig=}. Chữ ký là
 * quyền (không cần thẻ, chạy trước bộ lọc xác thực); sai chữ ký hoặc quá hạn 5 phút: 403. Ảnh trả
 * là bản đã gỡ EXIF/XMP, giải mã ở máy chủ, {@code Cache-Control: no-store}.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 15)
@ConditionalOnProperty(name = "geo.s5.grading.enabled", havingValue = "true")
public class SignedImageFilter extends OncePerRequestFilter {

  private final ImageStoreService images;
  private final ErrorResponseWriter errors;

  public SignedImageFilter(ImageStoreService images, ErrorResponseWriter errors) {
    this.images = images;
    this.errors = errors;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !request.getRequestURI().startsWith(ImageStoreService.FILE_PREFIX);
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {
    if (!"GET".equals(req.getMethod())) {
      errors.write(res, ErrorCode.FORBIDDEN);
      return;
    }
    Optional<ImageStoreService.Blob> img;
    try {
      UUID id =
          UUID.fromString(req.getRequestURI().substring(ImageStoreService.FILE_PREFIX.length()));
      UUID org = UUID.fromString(req.getParameter("o"));
      long exp = Long.parseLong(req.getParameter("exp"));
      img = images.open(org, id, exp, req.getParameter("sig"));
    } catch (RuntimeException e) {
      img = Optional.empty();
    }
    if (img.isEmpty()) {
      errors.write(res, ErrorCode.FORBIDDEN);
      return;
    }
    byte[] body = img.get().bytes();
    res.setStatus(200);
    res.setContentType(img.get().contentType());
    res.setHeader("Cache-Control", "no-store");
    res.setHeader("X-Content-Type-Options", "nosniff");
    res.setContentLength(body.length);
    res.getOutputStream().write(body);
  }
}
