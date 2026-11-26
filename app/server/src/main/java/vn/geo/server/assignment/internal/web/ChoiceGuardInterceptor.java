package vn.geo.server.assignment.internal.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import vn.geo.server.assignment.internal.service.ChoiceService;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;

/**
 * Bộ chặn cho câu trắc nghiệm trên hai đường của câu điền đáp số (S2-E8, module tracking):
 *
 * <ul>
 *   <li>{@code POST …/practice-items/{id}/attempts}: câu trắc nghiệm không nhận đáp số gõ tay
 *       (VALIDATION_FAILED, không ghi lượt) — phải chọn lựa chọn qua {@code choice-attempts};
 *   <li>{@code POST …/practice-items/{id}/report}: câu trắc nghiệm không có "Em nghĩ đáp số này
 *       đúng" (CONFLICT, BR-S2E9-14, US-S2E9-03-SC6).
 * </ul>
 *
 * Đặt ở module assignment vì bộ điều khiển bài luyện của tracking nằm ngoài phạm vi tệp của
 * T-261126-25 (PLAN_code-s23 §5); đề nghị FIX đưa hai kiểm tra này vào PracticeService.
 */
@Configuration
public class ChoiceGuardInterceptor implements HandlerInterceptor, WebMvcConfigurer {

  private static final Pattern PATH =
      Pattern.compile("^/api/v1/student/practice-items/([0-9a-fA-F-]{36})/(attempts|report)$");

  private final ChoiceService service;

  public ChoiceGuardInterceptor(ChoiceService service) {
    this.service = service;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(this).addPathPatterns("/api/v1/student/practice-items/**");
  }

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {
    if (!"POST".equals(request.getMethod())) {
      return true;
    }
    Matcher m = PATH.matcher(request.getRequestURI());
    if (!m.matches()) {
      return true;
    }
    UUID assignmentId;
    try {
      assignmentId = UUID.fromString(m.group(1));
    } catch (IllegalArgumentException e) {
      return true;
    }
    if (!service.isChoiceItem(assignmentId)) {
      return true;
    }
    if ("report".equals(m.group(2))) {
      throw new AppException(
          ErrorCode.CONFLICT, "Câu trắc nghiệm không có nút Em nghĩ đáp số này đúng.");
    }
    throw AppException.invalidField("answer", "Câu trắc nghiệm: em hãy chọn một lựa chọn.");
  }
}
