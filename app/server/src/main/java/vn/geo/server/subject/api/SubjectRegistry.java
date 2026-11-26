package vn.geo.server.subject.api;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;

/**
 * Danh mục môn của khung (US-S2E2-01). Dựng một lần lúc khởi động từ mọi bean {@link
 * SubjectEngine}; kiểm từng môn TRƯỚC khi nhận bất kỳ môn nào: môn thiếu thao tác (để mặc định của
 * giao diện), {@code concepts} rỗng hoặc trùng mã thì ném {@link SubjectRegistrationException} ghi
 * tên thao tác thiếu — khung không khởi động với danh mục nạp dở (BR-S2E2-02).
 */
public final class SubjectRegistry {

  private final Map<String, SubjectEngine> byId;

  public SubjectRegistry(List<? extends SubjectEngine> engines) {
    Map<String, SubjectEngine> ok = new LinkedHashMap<>();
    for (SubjectEngine e : engines) {
      validate(e);
      if (ok.putIfAbsent(e.id(), e) != null) {
        throw new SubjectRegistrationException(e.id(), "trùng mã môn");
      }
    }
    this.byId = Collections.unmodifiableMap(ok);
  }

  /** Kiểm một môn: đủ sáu thao tác, {@code concepts} cài đặt và không rỗng. */
  public static void validate(SubjectEngine e) {
    String id = e.id();
    if (id == null || id.isBlank()) {
      throw new SubjectRegistrationException(String.valueOf(id), "thiếu mã môn");
    }
    List<String> missing = new ArrayList<>();
    for (String op : SubjectEngine.OPERATIONS) {
      if (!implemented(e, op)) {
        missing.add(op);
      }
    }
    if (!implemented(e, "concepts")) {
      missing.add("concepts");
    }
    if (!missing.isEmpty()) {
      throw new SubjectRegistrationException(id, "thiếu thao tác " + String.join(", ", missing));
    }
    List<String> concepts = e.concepts();
    if (concepts == null
        || concepts.isEmpty()
        || concepts.stream().anyMatch(c -> c == null || c.isBlank())) {
      throw new SubjectRegistrationException(id, "concepts rỗng");
    }
    if (e.kinds() == null || e.kinds().isEmpty()) {
      throw new SubjectRegistrationException(id, "kinds rỗng");
    }
  }

  private static boolean implemented(SubjectEngine e, String op) {
    for (Method m : SubjectEngine.class.getMethods()) {
      if (m.getName().equals(op)) {
        try {
          Method impl = e.getClass().getMethod(op, m.getParameterTypes());
          return impl.getDeclaringClass() != SubjectEngine.class;
        } catch (NoSuchMethodException ex) {
          return false;
        }
      }
    }
    return false;
  }

  /** Danh mục môn (SC1): mã, tên, loại đề, thao tác, khái niệm. */
  public List<SubjectInfo> catalog() {
    return byId.values().stream()
        .map(e ->
            new SubjectInfo(
                e.id(),
                e.name(),
                e.kinds().stream().map(ProblemKind::code).sorted().toList(),
                SubjectEngine.OPERATIONS,
                List.copyOf(e.concepts())))
        .toList();
  }

  public Optional<SubjectEngine> find(String id) {
    return Optional.ofNullable(id == null ? null : byId.get(id));
  }

  /** Môn theo mã; không có: NOT_FOUND (US-S2E2-01-FC3, D-GEO-143). */
  public SubjectEngine require(String id) {
    return find(id).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
  }

  /** Môn phục vụ một loại đề ({@code SubjectRegistry.forKind(Problem.kind)}, PLAN A1). */
  public SubjectEngine forKind(ProblemKind kind) {
    return byId.values().stream()
        .filter(e -> e.kinds().contains(kind))
        .findFirst()
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
  }

  /** Mô tả một môn trong danh mục. */
  public record SubjectInfo(
      String id, String name, List<String> kinds, List<String> operations, List<String> concepts) {}
}
