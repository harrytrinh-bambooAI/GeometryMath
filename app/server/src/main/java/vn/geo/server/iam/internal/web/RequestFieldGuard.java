package vn.geo.server.iam.internal.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.common.error.AppException;

/**
 * US-S3E5-03 (BR-S3E5-33, 34, 36): thân yêu cầu JSON có khóa ngoài danh sách bị từ chối
 * VALIDATION_FAILED trước khi vào nghiệp vụ (không lưu phần nào). Áp cho thân kiểu record của mã
 * ứng dụng: khóa cấp một phải là thành phần của record (record lại bị test {@code AllowedFields*}
 * buộc nằm trong danh sách, không khớp mẫu tên cấm, không có {@code org_id}), nên email, phone,
 * schoolName, Truong, org_id gửi kèm đều bị từ chối. Thân tự do (Map, JsonNode, chuỗi) không xét:
 * đó là dữ liệu hình/đề, không phải trường của người dùng. {@code details[0].field} là khóa bị từ
 * chối.
 */
@ControllerAdvice
public class RequestFieldGuard extends RequestBodyAdviceAdapter {

  public static final String MSG = "Yêu cầu có trường không được phép thu.";

  private final JsonMapper mapper;
  private final Map<Class<?>, Optional<Set<String>>> known = new ConcurrentHashMap<>();

  public RequestFieldGuard(JsonMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public boolean supports(
      MethodParameter parameter,
      Type targetType,
      Class<? extends HttpMessageConverter<?>> converterType) {
    Class<?> raw = ResolvableType.forType(targetType).resolve();
    return raw != null
        && raw.isRecord()
        && raw.getName().startsWith("vn.geo.server.")
        && !legacyLenient(parameter);
  }

  /**
   * Hợp đồng MVP cũ "bỏ qua trường thừa, không chuyển tới nhà cung cấp AI" (TC-GEO-0119, lượt hỏi
   * trợ lý của học sinh): giữ nguyên cho tới khi FIX đổi kỳ vọng của test MVP sang
   * VALIDATION_FAILED (qc_findings Dev-S3E5-13).
   */
  static final Set<String> LEGACY_LENIENT = Set.of("/api/v1/student/assignments/{id}/questions");

  static boolean legacyLenient(MethodParameter parameter) {
    if (parameter.getMethod() == null) {
      return false;
    }
    org.springframework.web.bind.annotation.RequestMapping m =
        org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation(
            parameter.getMethod(), org.springframework.web.bind.annotation.RequestMapping.class);
    if (m == null) {
      return false;
    }
    for (String p : m.path()) {
      if (LEGACY_LENIENT.contains(p)) {
        return true;
      }
    }
    return false;
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
    Class<?> raw = ResolvableType.forType(targetType).resolve();
    check(body, raw);
    HttpHeaders headers = input.getHeaders();
    return new HttpInputMessage() {
      @Override
      public InputStream getBody() {
        return new ByteArrayInputStream(body);
      }

      @Override
      public HttpHeaders getHeaders() {
        return headers;
      }
    };
  }

  private void check(byte[] body, Class<?> raw) {
    if (body.length == 0) {
      return;
    }
    JsonNode node;
    try {
      node = mapper.readTree(body);
    } catch (RuntimeException e) {
      return; // JSON hỏng: để bộ đọc thông thường trả lỗi như trước
    }
    if (node == null || !node.isObject()) {
      return;
    }
    Optional<Set<String>> names = known.computeIfAbsent(raw, RequestFieldGuard::propertyNames);
    // Khóa là thành phần của record thì đã qua test danh sách (kể cả ngoại lệ có tên như ảnh đề
    // problem_image); khóa lạ, khóa mẫu cấm (email, schoolName, Truong) và org_id bị từ chối.
    for (String key : node.propertyNames()) {
      boolean listed = names.map(n -> n.contains(key)).orElse(true);
      if (!listed) {
        throw AppException.invalidField(key, MSG);
      }
    }
  }

  /** Tên khóa JSON của một record (tôn trọng {@code @JsonProperty}); rỗng nếu không phải record. */
  public static Optional<Set<String>> propertyNames(Class<?> type) {
    if (type == null || !type.isRecord()) {
      return Optional.empty();
    }
    Set<String> out = new LinkedHashSet<>();
    for (RecordComponent c : type.getRecordComponents()) {
      JsonProperty p = c.getAnnotation(JsonProperty.class);
      if (p == null && c.getAccessor() != null) {
        p = c.getAccessor().getAnnotation(JsonProperty.class);
      }
      out.add(p != null && !p.value().isEmpty() ? p.value() : c.getName());
    }
    return Optional.of(out);
  }
}
