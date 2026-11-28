package vn.geo.server.content.internal.plane.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Tính ba chỉ số của {@code make bench-s2} (US-S2E2-05, BR-S2E2-15/16) — hàm thuần: M-S2-01 = đề
 * thỏa ràng buộc / tổng (≥ 95 %), M-S2-02 = đề đáp số khớp mẫu / tổng (≥ 90 %), M-S2-03 = số đề sai
 * mà nhãn máy "Đã kiểm chứng" (= 0). So ngưỡng bằng số nguyên (dấu ≥, không lệch do làm tròn). Bộ
 * rỗng: tỉ lệ "—", rớt, thông báo "bộ đề rỗng".
 */
public final class PlaneBenchCalc {

  public static final String EMPTY = "bộ đề rỗng";

  private PlaneBenchCalc() {}

  /** Kết quả một đề. */
  public record Item(String code, boolean sceneOk, boolean answerOk, boolean labeledVerified) {}

  /** Một đề rớt kèm lý do. */
  public record Failure(String code, String reason) {}

  /** Báo cáo (BR-S2E2-16): ngày chạy, số đề, ba chỉ số, đạt/rớt, danh sách đề rớt, mã thoát. */
  public record Report(
      String runAt,
      int total,
      int sceneOk,
      int answerOk,
      int wrongLabeled,
      String m1,
      String m2,
      String m3,
      boolean m1Pass,
      boolean m2Pass,
      boolean m3Pass,
      List<Failure> failed,
      String message,
      int exitCode) {

    public boolean pass() {
      return exitCode == 0;
    }

    /** Báo cáo dạng Markdown (report.md). */
    public String markdown() {
      StringBuilder sb = new StringBuilder();
      sb.append("# bench-s2 · bộ đề chuẩn hình phẳng (AI giả lập)\n\n");
      sb.append("- Ngày chạy: ").append(runAt).append('\n');
      sb.append("- Số đề: ").append(total).append('\n');
      if (message != null) {
        sb.append("- Thông báo: ").append(message).append('\n');
      }
      sb.append("\n| Chỉ số | Giá trị | Ngưỡng | Kết quả |\n|---|---|---|---|\n");
      sb.append(row("M-S2-01 thỏa ràng buộc", m1 + frac(sceneOk), "≥ 95 %", m1Pass));
      sb.append(row("M-S2-02 đáp số khớp mẫu", m2 + frac(answerOk), "≥ 90 %", m2Pass));
      sb.append(row("M-S2-03 sai mang nhãn", m3, "= 0", m3Pass));
      sb.append("\n## Đề rớt\n\n");
      if (failed.isEmpty()) {
        sb.append("(rỗng)\n");
      }
      for (Failure f : failed) {
        sb.append("- ").append(f.code()).append(": ").append(f.reason()).append('\n');
      }
      sb.append("\nMã thoát: ").append(exitCode).append('\n');
      return sb.toString();
    }

    private String frac(int k) {
      return total == 0 ? "" : " (" + k + "/" + total + ")";
    }

    private static String row(String name, String value, String limit, boolean ok) {
      return "| " + name + " | " + value + " | " + limit + " | " + (ok ? "đạt" : "rớt") + " |\n";
    }
  }

  public static Report evaluate(List<Item> items, String runAt) {
    int n = items.size();
    int scene = 0;
    int answer = 0;
    int wrong = 0;
    List<Failure> failed = new ArrayList<>();
    for (Item it : items) {
      List<String> why = new ArrayList<>();
      if (it.sceneOk()) {
        scene++;
      } else {
        why.add("không thỏa ràng buộc");
      }
      if (it.answerOk()) {
        answer++;
      } else {
        why.add("đáp số lệch mẫu");
        if (it.labeledVerified()) {
          wrong++;
          why.add("sai mang nhãn");
        }
      }
      if (!why.isEmpty()) {
        failed.add(new Failure(it.code(), String.join("; ", why)));
      }
    }
    if (n == 0) {
      return new Report(runAt, 0, 0, 0, 0, "—", "—", "0", false, false, true, List.of(), EMPTY, 1);
    }
    boolean p1 = scene * 100L >= 95L * n;
    boolean p2 = answer * 100L >= 90L * n;
    boolean p3 = wrong == 0;
    return new Report(
        runAt,
        n,
        scene,
        answer,
        wrong,
        percent(scene, n),
        percent(answer, n),
        String.valueOf(wrong),
        p1,
        p2,
        p3,
        List.copyOf(failed),
        null,
        p1 && p2 && p3 ? 0 : 1);
  }

  /** "96,7 %" (một chữ số thập phân, dấu phẩy). */
  public static String percent(int k, int n) {
    return String.format(Locale.ROOT, "%.1f", 100.0 * k / n).replace('.', ',') + " %";
  }
}
