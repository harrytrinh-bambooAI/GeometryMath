package vn.geo.server.aigrade.internal.shadow.web;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import vn.geo.server.aigrade.internal.proposal.web.ProposalController;
import vn.geo.server.aigrade.internal.shadow.service.ShadowPolicy;
import vn.geo.server.common.security.RequestContext;

/**
 * Tín hiệu AI của một bài có chấm ({@code GET …/ai-signals}, T-261128-61) bỏ các bài chạy song song
 * chưa duyệt (BR-S5E6-13: không cờ hay thứ tự ưu tiên có nguồn AI trước khi duyệt).
 */
@ControllerAdvice(assignableTypes = ProposalController.class)
public class ShadowSignalsAdvice implements ResponseBodyAdvice<Object> {

  private final ShadowPolicy policy;

  public ShadowSignalsAdvice(ShadowPolicy policy) {
    this.policy = policy;
  }

  @Override
  public boolean supports(
      MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
    return returnType.getMethod() != null && "signals".equals(returnType.getMethod().getName());
  }

  @Override
  public Object beforeBodyWrite(
      Object body,
      MethodParameter returnType,
      MediaType selectedContentType,
      Class<? extends HttpMessageConverter<?>> selectedConverterType,
      ServerHttpRequest request,
      ServerHttpResponse response) {
    if (!(body instanceof Map<?, ?> m)
        || !(m.get("items") instanceof List<?> items)
        || !RequestContext.isAuthenticated()) {
      return body;
    }
    UUID org = RequestContext.principal().orgId();
    List<?> kept =
        items.stream()
            .filter(it ->
                !(it instanceof Map<?, ?> im
                    && im.get("submissionId") instanceof String id
                    && policy.hiddenFor(org, null, UUID.fromString(id))))
            .toList();
    if (kept.size() == items.size()) {
      return body;
    }
    return Map.of("assignmentId", m.get("assignmentId"), "items", kept);
  }
}
