package vn.geo.server.assessment.internal.review;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import vn.geo.server.assessment.api.review.AiProposalSlot;
import vn.geo.server.assessment.internal.queue.web.QueueController;
import vn.geo.server.assessment.internal.review.service.ReviewService.GradeRequest;
import vn.geo.server.assessment.internal.review.web.ReviewController;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.RequestContext;
import vn.geo.server.common.security.Role;

/**
 * Điểm cắm duy nhất của đề xuất AI vào chấm tay (T-261128-62; PLAN_code-s5 §5 "AiProposalSlot*"),
 * không sửa mã của T-261128-60:
 *
 * <ul>
 *   <li>Thân {@code POST /submissions/{id}/grade}: bỏ khóa {@code acceptAll} và {@code aiActions}
 *       trước bộ chặn khóa lạ; {@code acceptAll=true} → điểm đề xuất của mọi tiêu chí (bài có cờ
 *       CONFLICT, BR-S5E6-11).
 *   <li>Phản hồi màn chấm, Duyệt và hàng chờ: thêm khóa {@code ai} của {@link AiProposalSlot}.
 * </ul>
 *
 * Không có bean {@link AiProposalSlot} (cờ AI chấm tắt) thì không làm gì.
 */
@ControllerAdvice(assignableTypes = {ReviewController.class, QueueController.class})
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AiProposalSlotAdvice implements RequestBodyAdvice, ResponseBodyAdvice<Object> {

  private static final Logger log = LoggerFactory.getLogger(AiProposalSlotAdvice.class);

  static final String ATTR_ACTIONS = AiProposalSlotAdvice.class.getName() + ".actions";

  private final ObjectProvider<AiProposalSlot> slot;
  private final JsonMapper mapper;

  public AiProposalSlotAdvice(ObjectProvider<AiProposalSlot> slot, JsonMapper mapper) {
    this.slot = slot;
    this.mapper = mapper;
  }

  // ------------------------------------------------------------------ thân yêu cầu Duyệt

  @Override
  public boolean supports(
      MethodParameter parameter,
      Type targetType,
      Class<? extends HttpMessageConverter<?>> converterType) {
    return targetType == GradeRequest.class;
  }

  @Override
  public HttpInputMessage beforeBodyRead(
      HttpInputMessage input,
      MethodParameter parameter,
      Type targetType,
      Class<? extends HttpMessageConverter<?>> converterType)
      throws IOException {
    byte[] body;
    try (InputStream in = input.getBody()) {
      body = in.readAllBytes();
    }
    JsonNode node;
    try {
      node = body.length == 0 ? null : mapper.readTree(body);
    } catch (RuntimeException e) {
      node = null;
    }
    if (node instanceof ObjectNode o && (o.has("acceptAll") || o.has("aiActions"))) {
      boolean all = o.path("acceptAll").asBoolean(false);
      JsonNode acts = o.path("aiActions");
      o.remove("acceptAll");
      o.remove("aiActions");
      AiProposalSlot s = slot.getIfAvailable();
      if (s != null) {
        Map<Integer, AiProposalSlot.Action> actions = new HashMap<>();
        if (all) {
          UUID sid = submissionId();
          ArrayNode cs = o.putArray("criteria");
          for (Map.Entry<Integer, BigDecimal> e :
              s.acceptAll(RequestContext.principal().orgId(), sid).entrySet()) {
            cs.addObject().put("ord", e.getKey()).put("points", e.getValue());
            actions.put(e.getKey(), AiProposalSlot.Action.ACCEPT);
          }
        } else if (acts.isArray()) {
          for (JsonNode a : acts) {
            try {
              actions.put(
                  a.path("ord").asInt(),
                  AiProposalSlot.Action.valueOf(
                      a.path("action").asString("").toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException e) {
              // hành động lạ: bỏ qua, máy chủ tự suy theo điểm
            }
          }
        }
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        if (ra != null) {
          ra.setAttribute(ATTR_ACTIONS, actions, RequestAttributes.SCOPE_REQUEST);
        }
      }
      body = mapper.writeValueAsBytes(o);
    }
    byte[] out = body;
    HttpHeaders headers = input.getHeaders();
    return new HttpInputMessage() {
      @Override
      public InputStream getBody() {
        return new ByteArrayInputStream(out);
      }

      @Override
      public HttpHeaders getHeaders() {
        return headers;
      }
    };
  }

  @Override
  public Object afterBodyRead(
      Object body,
      HttpInputMessage inputMessage,
      MethodParameter parameter,
      Type targetType,
      Class<? extends HttpMessageConverter<?>> converterType) {
    return body;
  }

  @Override
  public Object handleEmptyBody(
      Object body,
      HttpInputMessage inputMessage,
      MethodParameter parameter,
      Type targetType,
      Class<? extends HttpMessageConverter<?>> converterType) {
    return body;
  }

  @SuppressWarnings("unchecked")
  private static UUID submissionId() {
    RequestAttributes ra = RequestContextHolder.getRequestAttributes();
    Object vars =
        ra == null
            ? null
            : ra.getAttribute(
                HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
    return UUID.fromString(((Map<String, String>) vars).get("id"));
  }

  // ------------------------------------------------------------------ phản hồi

  @Override
  public boolean supports(
      MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
    String n = returnType.getMethod() == null ? "" : returnType.getMethod().getName();
    return n.equals("get") || n.equals("grade") || n.equals("queue");
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
    AiProposalSlot s = slot.getIfAvailable();
    if (s == null || !(body instanceof Map<?, ?> m) || !RequestContext.isAuthenticated()) {
      return body;
    }
    AuthPrincipal me = RequestContext.principal();
    if (me.role() != Role.TEACHER) {
      return body;
    }
    Map<String, Object> map = (Map<String, Object>) m;
    try {
      String method = returnType.getMethod().getName();
      if (method.equals("queue")) {
        if (map.get("items") instanceof List<?> items) {
          for (Object it : items) {
            if (it instanceof Map<?, ?> im && im.get("submissionId") instanceof String id) {
              putAi((Map<String, Object>) im, s.extras(me.orgId(), UUID.fromString(id)));
            }
          }
          // T-261128-65: bài có cờ (của hàng chờ hoặc cờ AI) lên đầu, rồi theo giờ nộp (STAGES §5
          // B2).
          // Chạy song song trước khi duyệt không có aiFlags nên không đổi thứ tự (BR-S5E6-13).
          List<Object> sorted = new ArrayList<>(items);
          sorted.sort(QUEUE_ORDER);
          map.put("items", sorted);
        }
        return body;
      }
      if (!(map.get("submissionId") instanceof String id)) {
        return body;
      }
      UUID sid = UUID.fromString(id);
      if (method.equals("grade") && map.get("grade") instanceof Map<?, ?> g) {
        Map<Integer, BigDecimal> points = new HashMap<>();
        if (g.get("criteria") instanceof List<?> cs) {
          for (Object c : cs) {
            if (c instanceof Map<?, ?> cm
                && cm.get("ord") instanceof Number ord
                && cm.get("points") instanceof Number p) {
              points.put(ord.intValue(), new BigDecimal(p.toString()));
            }
          }
        }
        Object acts =
            RequestContextHolder.getRequestAttributes() == null
                ? null
                : RequestContextHolder.getRequestAttributes()
                    .getAttribute(ATTR_ACTIONS, RequestAttributes.SCOPE_REQUEST);
        int seq = g.get("seq") instanceof Number n ? n.intValue() : 1;
        putAi(
            map,
            s.approved(
                me.orgId(),
                me.userId(),
                sid,
                seq,
                points,
                acts == null ? Map.of() : (Map<Integer, AiProposalSlot.Action>) acts));
      } else {
        putAi(map, s.extras(me.orgId(), sid));
      }
    } catch (UnsupportedOperationException e) {
      // bản đồ bất biến: giữ nguyên phản hồi
    } catch (RuntimeException e) {
      // điểm đã lưu; phần AI lỗi không được làm hỏng phản hồi chấm tay
      log.warn("Điểm cắm AI lỗi: {}", e.toString());
    }
    return body;
  }

  /**
   * Chế độ chấm tay (không dùng AI): phản hồi giữ nguyên như US-S5E4, không thêm khóa {@code ai}.
   */
  static boolean flagged(Object item) {
    if (!(item instanceof Map<?, ?> m)) {
      return false;
    }
    if (m.get("flags") instanceof List<?> f && !f.isEmpty()) {
      return true;
    }
    return m.get("ai") instanceof Map<?, ?> ai
        && ai.get("aiFlags") instanceof List<?> af
        && !af.isEmpty();
  }

  static final Comparator<Object> QUEUE_ORDER =
      Comparator.<Object>comparingInt(i -> flagged(i) ? 0 : 1)
          .thenComparing(AiProposalSlotAdvice::submittedAt)
          .thenComparing(
              i -> i instanceof Map<?, ?> m && m.get("code") instanceof String c ? c : "");

  private static Instant submittedAt(Object item) {
    try {
      return item instanceof Map<?, ?> m && m.get("submittedAt") instanceof String t
          ? Instant.parse(t)
          : Instant.EPOCH;
    } catch (DateTimeParseException e) {
      return Instant.EPOCH;
    }
  }

  private static void putAi(Map<String, Object> target, Map<String, Object> extras) {
    if (extras != null && !extras.isEmpty()) {
      target.put("ai", extras);
    }
  }
}
