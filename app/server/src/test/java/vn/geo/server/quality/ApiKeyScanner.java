package vn.geo.server.quality;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.http.HttpEntity;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.error.ApiError;

/**
 * US-S3E5-03 (BR-S3E5-31): quét tĩnh mọi API của máy chủ (mọi {@code @RestController} không gắn
 * {@code @Profile} — móc kiểm thử dev/test không phải API thật), liệt kê mọi khóa JSON của phản hồi
 * và thân yêu cầu theo kiểu record (đệ quy vào record lồng, danh sách, Optional, giá trị của Map).
 * Kiểu trả về tự do (Map, JsonNode) được phủ bằng lượt gọi API thật ở {@code AllowedFieldsS3E5IT}.
 */
public final class ApiKeyScanner {

  private ApiKeyScanner() {}

  /** Khóa → nơi đầu tiên gặp ("Controller#method › Record"). */
  public static Map<String, String> scan() {
    var scanner = new ClassPathScanningCandidateComponentProvider(false);
    scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
    Map<String, String> out = new TreeMap<>();
    Set<String> seen = new HashSet<>();
    for (BeanDefinition bd : scanner.findCandidateComponents("vn.geo.server")) {
      Class<?> c = ClassUtils.resolveClassName(bd.getBeanClassName(), null);
      if (c.isAnnotationPresent(Profile.class)) {
        continue;
      }
      for (Method m : c.getDeclaredMethods()) {
        if (!AnnotatedElementUtils.hasAnnotation(m, RequestMapping.class)) {
          continue;
        }
        String where = c.getSimpleName() + "#" + m.getName();
        walk(ResolvableType.forMethodReturnType(m), out, where, seen);
        for (int i = 0; i < m.getParameterCount(); i++) {
          MethodParameter p = new MethodParameter(m, i);
          if (p.hasParameterAnnotation(RequestBody.class)) {
            walk(ResolvableType.forMethodParameter(p), out, where, seen);
          }
        }
      }
    }
    walk(ResolvableType.forClass(ApiError.class), out, "ApiError", seen);
    return out;
  }

  /** Khóa của một kiểu (dùng cho DTO giả ở ca kiểm TC-S3E5-28). */
  public static Map<String, String> keysOf(Class<?> type) {
    Map<String, String> out = new TreeMap<>();
    walk(ResolvableType.forClass(type), out, type.getSimpleName(), new HashSet<>());
    return out;
  }

  static void walk(ResolvableType t, Map<String, String> out, String where, Set<String> seen) {
    if (t == ResolvableType.NONE) {
      return;
    }
    if (t.isArray()) {
      walk(t.getComponentType(), out, where, seen);
      return;
    }
    Class<?> c = t.resolve(Object.class);
    if (HttpEntity.class.isAssignableFrom(c)
        || Optional.class.equals(c)
        || Collection.class.isAssignableFrom(c)) {
      walk(t.getGeneric(0), out, where, seen);
      return;
    }
    if (Map.class.isAssignableFrom(c)) {
      walk(t.asMap().getGeneric(1), out, where, seen);
      return;
    }
    if (!c.getName().startsWith("vn.geo.server.") || c.isEnum() || c.isInterface()) {
      return;
    }
    if (!seen.add(t.toString())) {
      return;
    }
    String here = where + " › " + c.getSimpleName();
    if (c.isRecord()) {
      for (RecordComponent rc : c.getRecordComponents()) {
        if (rc.getAccessor().isAnnotationPresent(JsonIgnore.class)) {
          continue;
        }
        out.putIfAbsent(jsonName(rc), here);
        try {
          walk(ResolvableType.forField(c.getDeclaredField(rc.getName()), t), out, here, seen);
        } catch (NoSuchFieldException e) {
          throw new IllegalStateException(e);
        }
      }
      return;
    }
    // lớp thường: thuộc tính Jackson thấy được qua getter công khai
    for (Method m : c.getMethods()) {
      if (Modifier.isStatic(m.getModifiers())
          || m.getParameterCount() != 0
          || m.getDeclaringClass() == Object.class
          || m.isAnnotationPresent(JsonIgnore.class)) {
        continue;
      }
      String n = m.getName();
      String prop = null;
      if (n.startsWith("get") && n.length() > 3) {
        prop = Character.toLowerCase(n.charAt(3)) + n.substring(4);
      } else if (n.startsWith("is") && n.length() > 2 && m.getReturnType() == boolean.class) {
        prop = Character.toLowerCase(n.charAt(2)) + n.substring(3);
      }
      if (prop != null) {
        out.putIfAbsent(prop, here);
        walk(ResolvableType.forMethodReturnType(m, c), out, here, seen);
      }
    }
  }

  private static String jsonName(RecordComponent rc) {
    JsonProperty p = rc.getAccessor().getAnnotation(JsonProperty.class);
    if (p == null) {
      try {
        p =
            rc.getDeclaringRecord()
                .getDeclaredField(rc.getName())
                .getAnnotation(JsonProperty.class);
      } catch (NoSuchFieldException e) {
        p = null;
      }
    }
    if (p == null) {
      p = rc.getAnnotation(JsonProperty.class);
    }
    return p != null && !p.value().isEmpty() ? p.value() : rc.getName();
  }
}
