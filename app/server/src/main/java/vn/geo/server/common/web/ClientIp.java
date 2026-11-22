package vn.geo.server.common.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Địa chỉ IP của máy khách. Chỉ tin tiêu đề {@code X-Forwarded-For} khi {@code
 * geo.http.trust-forwarded-for=true} (sau proxy tin cậy; profile dev/test dùng để mô phỏng nhiều
 * địa chỉ IP trong E2E, TC-GEO-0179). Mặc định dùng địa chỉ kết nối.
 */
@Component
public class ClientIp {

  private final boolean trustForwardedFor;

  public ClientIp(@Value("${geo.http.trust-forwarded-for:false}") boolean trustForwardedFor) {
    this.trustForwardedFor = trustForwardedFor;
  }

  public String of(HttpServletRequest request) {
    if (trustForwardedFor) {
      String xff = request.getHeader("X-Forwarded-For");
      if (xff != null && !xff.isBlank()) {
        return xff.split(",")[0].trim();
      }
    }
    return request.getRemoteAddr();
  }
}
