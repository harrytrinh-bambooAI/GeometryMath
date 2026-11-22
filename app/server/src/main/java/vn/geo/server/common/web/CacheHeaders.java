package vn.geo.server.common.web;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;

/** Tiêu đề lưu đệm dùng chung (D-GEO-145). */
public final class CacheHeaders {

  /** Giá trị Cache-Control cho phản hồi nhạy cảm. */
  public static final String NO_STORE = "no-store";

  /** Đặt tiêu đề không lưu đệm lên phản hồi (ghi đè giá trị cũ). */
  public static void noStore(HttpServletResponse response) {
    response.setHeader(HttpHeaders.CACHE_CONTROL, NO_STORE);
    response.setHeader(HttpHeaders.PRAGMA, "no-cache");
  }

  private CacheHeaders() {}
}
