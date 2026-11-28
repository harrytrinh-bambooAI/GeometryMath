package vn.geo.server.lessonplan.internal.list.web;

import java.util.Map;
import java.util.UUID;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import vn.geo.server.lessonplan.internal.list.service.LessonListService;
import vn.geo.server.lessonplan.internal.web.LessonPlanController;

/**
 * Thêm trường duyệt / phiên bản (US-S5E1-05-SC1 "GET trả approvedVersion"; Hoàn tác của US-S5E1-04)
 * vào phản hồi {@code GET /api/v1/teacher/lesson-plans/{id}} của màn kết quả (T-53) mà không sửa bộ
 * dựng phản hồi của T-53.
 */
@ControllerAdvice(assignableTypes = LessonPlanController.class)
public class LessonViewAdvice implements ResponseBodyAdvice<Object> {

  private final LessonListService service;

  public LessonViewAdvice(LessonListService service) {
    this.service = service;
  }

  @Override
  public boolean supports(
      MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
    return returnType.getMethod() != null && "view".equals(returnType.getMethod().getName());
  }

  @Override
  @SuppressWarnings("unchecked")
  public Object beforeBodyWrite(
      Object body,
      MethodParameter returnType,
      MediaType selectedContentType,
      Class<? extends HttpMessageConverter<?>> selectedConverterType,
      ServerHttpRequest request,
      ServerHttpResponse response) {
    if (body instanceof Map<?, ?> m && m.get("draftId") instanceof String id) {
      try {
        ((Map<String, Object>) m).putAll(service.approval(UUID.fromString(id)));
      } catch (UnsupportedOperationException e) {
        // bản đồ bất biến: giữ nguyên phản hồi
      }
    }
    return body;
  }
}
