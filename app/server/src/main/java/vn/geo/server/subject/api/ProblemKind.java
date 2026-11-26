package vn.geo.server.subject.api;

import java.util.Locale;

/**
 * Loại đề (US-S2E2-02, BR-S2E2-04; PLAN_code-s23 A2): {@code space} khi có điểm ngoài mặt phẳng đáy
 * (mọi đề MVP), {@code plane} khi mọi điểm nằm trong một mặt phẳng. Giá trị lưu ở cột {@code
 * problem.kind} (V130). TẠM · cao · stories_ac S2-E2 ghi {@code space} (PLAN A2 ghi "solid"; theo
 * spec).
 */
public enum ProblemKind {
  SPACE,
  PLANE;

  /** Giá trị ở CSDL và JSON: chữ thường. */
  public String code() {
    return name().toLowerCase(Locale.ROOT);
  }

  public static ProblemKind of(String code) {
    return valueOf(code.strip().toUpperCase(Locale.ROOT));
  }
}
