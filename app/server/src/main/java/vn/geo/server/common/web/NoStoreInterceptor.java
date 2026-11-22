package vn.geo.server.common.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Áp {@link NoStore}: đặt tiêu đề TRƯỚC khi controller chạy, nên phản hồi lỗi do {@code
 * GlobalExceptionHandler} sinh ra cho endpoint đó cũng không bị lưu đệm.
 */
public class NoStoreInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {
    if (handler instanceof HandlerMethod hm && isNoStore(hm)) {
      CacheHeaders.noStore(response);
    }
    return true;
  }

  static boolean isNoStore(HandlerMethod hm) {
    return hm.hasMethodAnnotation(NoStore.class)
        || AnnotatedElementUtils.hasAnnotation(hm.getBeanType(), NoStore.class);
  }
}
