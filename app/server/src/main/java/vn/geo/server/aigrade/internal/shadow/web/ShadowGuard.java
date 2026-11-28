package vn.geo.server.aigrade.internal.shadow.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import vn.geo.server.aigrade.internal.shadow.service.ShadowPolicy;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.RequestContext;
import vn.geo.server.common.security.Role;

/**
 * Chặn đề xuất AI của bài chạy song song chưa duyệt (US-S5E6-05-SC1, BR-S5E6-13): {@code GET
 * /api/v1/submissions/{id}/proposal} của giáo viên chủ lớp trả CONFLICT kèm MSG-S5-47 trước khi bài
 * Đã duyệt. Không sửa controller đề xuất của T-261128-61. Bài không phải của giáo viên hay vai
 * khác: để controller xử lý như cũ (NOT_FOUND / FORBIDDEN).
 */
@Configuration
@ConditionalOnProperty(name = "geo.s5.ai-grading.enabled", havingValue = "true")
public class ShadowGuard implements WebMvcConfigurer, HandlerInterceptor {

  private final ShadowPolicy policy;

  public ShadowGuard(ShadowPolicy policy) {
    this.policy = policy;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(this).addPathPatterns(List.of("/api/v1/submissions/*/proposal"));
  }

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {
    if (!"GET".equals(request.getMethod()) || !RequestContext.isAuthenticated()) {
      return true;
    }
    AuthPrincipal me = RequestContext.principal();
    if (me.role() != Role.TEACHER) {
      return true;
    }
    Object vars = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
    if (!(vars instanceof Map<?, ?> m) || !(m.get("id") instanceof String id)) {
      return true;
    }
    UUID sid;
    try {
      sid = UUID.fromString(id);
    } catch (IllegalArgumentException e) {
      return true;
    }
    if (policy.hiddenFor(me.orgId(), me.userId(), sid)) {
      throw new AppException(ErrorCode.CONFLICT, ShadowPolicy.MSG_47);
    }
    return true;
  }
}
