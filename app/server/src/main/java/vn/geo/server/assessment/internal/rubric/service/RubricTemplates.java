package vn.geo.server.assessment.internal.rubric.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Hai mẫu rubric hệ thống (BR-S5E4-02) suy từ {@code materials/baseline/KPI.md} v1.0 §1 trên thang
 * 10, kèm hướng dẫn chấm theo 5 luật chặn KPI §2.
 */
public final class RubricTemplates {

  private RubricTemplates() {}

  /** Một tiêu chí: tên, mô tả mức đạt, điểm tối đa, cờ phụ thuộc hướng. */
  public record Criterion(
      String name, String description, BigDecimal maxPoints, boolean direction) {}

  /** Mẫu: mã, tên hiển thị, tiêu chí. */
  public record Template(String code, String label, List<Criterion> criteria) {}

  /** KPI §2: luật chặn dùng làm hướng dẫn chấm. */
  public static final List<String> GUIDE =
      List.of(
          "Sai hướng → 0 điểm các bước phụ thuộc hướng đó.",
          "Đúng kết quả nhưng lập luận sai/thiếu → điểm kết quả tối đa 50 %.",
          "Bước sai → bước sau phụ thuộc = 0; bước độc lập vẫn tính (lệch cho phép ±0,25).",
          "Bài hình không gian không có hình / hình sai → mất phần hình + các bước dựa trên hình sai.",
          "Dùng định lí / công thức ngoài kho kiến thức → bước đó 0 điểm, gắn cờ \"bịa kiến thức\".");

  private static Criterion c(String name, String desc, String pts, boolean dir) {
    return new Criterion(name, desc, new BigDecimal(pts), dir);
  }

  public static final Map<String, Template> ALL =
      Map.of(
          "tinh-toan",
          new Template(
              "tinh-toan",
              "Tính toán",
              List.of(
                  c("Hướng", "Chọn đúng hướng giải (dựng hình, công thức cần dùng).", "2", false),
                  c("Các bước", "Các bước biến đổi, tính toán đúng theo hướng đã chọn.", "4", true),
                  c("Kết quả", "Đáp số đúng, đơn vị và dạng gọn.", "2", false),
                  c("Hình", "Hình vẽ đúng, đủ yếu tố cần dựng.", "1", false),
                  c("Trình bày", "Trình bày rõ, lập luận có căn cứ.", "1", false))),
          "chung-minh",
          new Template(
              "chung-minh",
              "Chứng minh",
              List.of(
                  c("Hướng", "Chọn đúng hướng chứng minh.", "2.5", false),
                  c("Lập luận", "Chuỗi lập luận đúng, đủ, có căn cứ.", "5", true),
                  c("Hình", "Hình vẽ đúng, đủ yếu tố cần dựng.", "1.5", false),
                  c("Trình bày", "Trình bày rõ, kết luận đúng điều cần chứng minh.", "1", false))));
}
