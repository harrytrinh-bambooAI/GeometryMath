package vn.geo.server.content.internal.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Báo cáo một lần chạy bộ đề chuẩn (BR-GEO-114…117): N, tỉ lệ hình thỏa ràng buộc (M-GEO-01), tỉ lệ
 * đáp số đúng (M-GEO-04), bài sai mang "Đã kiểm chứng" (M-GEO-05), số đề đã kiểm chứng (M-GEO-07),
 * model, phiên bản prompt, phiên bản bộ đề. Phần trăm một chữ số thập phân, dấu phẩy; N = 0 ghi
 * "—". Ngưỡng là đề xuất (D-GEO-045).
 */
public record BenchmarkReport(
    int setVersion,
    String model,
    String promptVersion,
    boolean simulated,
    int n,
    int sceneOk,
    int answerOk,
    int wrongVerified,
    int verified,
    List<String> missingReference) {

  public static final String SIMULATED_NOTE =
      "Nhà cung cấp AI giả lập: số đo chưa phản ánh độ đúng của AI thật.";
  static final BigDecimal SCENE_THRESHOLD = new BigDecimal("95.0");
  static final BigDecimal ANSWER_THRESHOLD = new BigDecimal("90.0");

  /** "83,3%" hoặc "—" khi N = 0. */
  public static String percent(int k, int n) {
    if (n == 0) {
      return "—";
    }
    BigDecimal p =
        BigDecimal.valueOf(k)
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(n), 1, RoundingMode.HALF_UP);
    return p.toPlainString().replace('.', ',') + "%";
  }

  /** "đạt (ngưỡng đề xuất ≥ 95,0%)" hoặc "chưa đạt (ngưỡng đề xuất)". */
  public static String verdict(int k, int n, BigDecimal threshold) {
    if (n == 0) {
      return "chưa đạt (ngưỡng đề xuất)";
    }
    BigDecimal p =
        BigDecimal.valueOf(k)
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(n), 1, RoundingMode.HALF_UP);
    return p.compareTo(threshold) >= 0
        ? "đạt (ngưỡng đề xuất ≥ " + threshold.toPlainString().replace('.', ',') + "%)"
        : "chưa đạt (ngưỡng đề xuất)";
  }

  public String sceneLine() {
    return "Hình thỏa ràng buộc: "
        + sceneOk
        + "/"
        + n
        + " = "
        + percent(sceneOk, n)
        + (n == 0 ? "" : " · " + verdict(sceneOk, n, SCENE_THRESHOLD));
  }

  public String answerLine() {
    return "Đáp số đúng: "
        + answerOk
        + "/"
        + n
        + " = "
        + percent(answerOk, n)
        + (n == 0 ? "" : " · " + verdict(answerOk, n, ANSWER_THRESHOLD));
  }

  public String text() {
    List<String> l = new ArrayList<>();
    if (simulated) {
      l.add(SIMULATED_NOTE);
    }
    l.add("Bộ đề chuẩn phiên bản " + setVersion);
    l.add("Model: " + model);
    l.add("Phiên bản prompt: " + promptVersion);
    l.add("N = " + n);
    l.add(sceneLine());
    l.add(answerLine());
    l.add(
        "Bài sai mang \"Đã kiểm chứng\": "
            + wrongVerified
            + " · "
            + (wrongVerified == 0 ? "đạt (ngưỡng đề xuất = 0)" : "chưa đạt (ngưỡng đề xuất)"));
    l.add("Số đề đã kiểm chứng: " + verified);
    if (!missingReference.isEmpty()) {
      l.add("Thiếu đáp số tham chiếu: " + String.join(", ", missingReference));
    }
    return String.join("\n", l);
  }
}
