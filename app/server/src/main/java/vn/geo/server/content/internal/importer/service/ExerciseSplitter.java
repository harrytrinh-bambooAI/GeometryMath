package vn.geo.server.content.internal.importer.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * S2-E4 (US-S2E4-02, BR-S2E4-08, 09): tách các đoạn văn của tệp thành từng bài tập. Hàm thuần, quy
 * tắc cố định (không gọi AI — chữ đề được chép NGUYÊN VĂN từ tệp):
 *
 * <ul>
 *   <li>Một bài bắt đầu ở đoạn mở bằng "Bài 1.", "Câu 2:", "Bài tập 3)", "Ví dụ 4." (không phân
 *       biệt hoa thường); các đoạn sau thuộc bài đó tới đoạn mở bài kế tiếp.
 *   <li>Đoạn mở bằng "Lý thuyết", "Kiến thức cần nhớ", "Định nghĩa", "Định lí", "Ghi nhớ", "Tóm tắt
 *       lý thuyết" mở một phần LÝ THUYẾT: bị bỏ qua và được đếm (PO-65 Q5: bài giảng dời chặng 5).
 *   <li>Đoạn tiêu đề "Bài tập" / "Phần bài tập" / "Luyện tập" đóng phần lý thuyết.
 *   <li>Chữ đứng trước bài đầu tiên mà không thuộc phần lý thuyết (tên tệp, tiêu đề) bị bỏ qua lặng
 *       lẽ.
 * </ul>
 */
public final class ExerciseSplitter {

  private ExerciseSplitter() {}

  /** Số bài tối đa lấy từ một tệp (BR-S2E4-07). */
  public static final int MAX_ITEMS = 50;

  private static final Pattern EXERCISE =
      Pattern.compile(
          "^(Bài tập|Bài toán|Bài|Câu|Ví dụ)\\s+(\\d{1,3})\\s*[.:)]\\s*(.*)$",
          Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.DOTALL);
  private static final Pattern THEORY =
      Pattern.compile(
          "^(Phần\\s+)?([IVX\\d]+[.)]\\s*)?(Lý thuyết|Lí thuyết|Tóm tắt lý thuyết|Tóm tắt lí thuyết"
              + "|Kiến thức cần nhớ|Định nghĩa|Định lý|Định lí|Ghi nhớ)(\\b|\\s|:|\\.|$).*",
          Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.DOTALL);
  private static final Pattern EXERCISE_HEADING =
      Pattern.compile(
          "^(Phần\\s+)?([IVX\\d]+[.)]\\s*)?(Bài tập|Luyện tập|Bài tập tự luyện)\\s*[:.]?$",
          Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

  /** Một bài tập: title = "Bài 1"; text = chữ đề (không kèm nhãn "Bài 1."). */
  public record Exercise(String title, String text) {}

  /**
   * Kết quả tách: items = tối đa {@value #MAX_ITEMS} bài đầu; found = tổng số bài tìm thấy;
   * theoryBlocks = số phần lý thuyết đã bỏ qua.
   */
  public record Split(List<Exercise> items, int found, int theoryBlocks) {}

  private enum Mode {
    NONE,
    THEORY,
    EXERCISE
  }

  public static Split split(List<String> paragraphs) {
    List<Exercise> all = new ArrayList<>();
    int theory = 0;
    Mode mode = Mode.NONE;
    String title = null;
    StringBuilder body = new StringBuilder();
    for (String raw : paragraphs) {
      String p = raw == null ? "" : raw.strip();
      if (p.isEmpty()) {
        continue;
      }
      Matcher m = EXERCISE.matcher(p);
      if (m.matches()) {
        close(all, title, body);
        String word = m.group(1);
        title =
            Character.toUpperCase(word.charAt(0))
                + word.substring(1).toLowerCase()
                + " "
                + m.group(2);
        body.append(m.group(3).strip());
        mode = Mode.EXERCISE;
      } else if (THEORY.matcher(p).matches()) {
        close(all, title, body);
        title = null;
        theory++;
        mode = Mode.THEORY;
      } else if (EXERCISE_HEADING.matcher(p).matches()) {
        close(all, title, body);
        title = null;
        mode = Mode.NONE;
      } else if (mode == Mode.EXERCISE) {
        if (body.length() > 0) {
          body.append(' ');
        }
        body.append(p);
      }
    }
    close(all, title, body);
    List<Exercise> items = all.size() > MAX_ITEMS ? List.copyOf(all.subList(0, MAX_ITEMS)) : all;
    return new Split(items, all.size(), theory);
  }

  private static void close(List<Exercise> all, String title, StringBuilder body) {
    String text = body.toString().strip();
    if (title != null && !text.isEmpty()) {
      all.add(new Exercise(title, text));
    }
    body.setLength(0);
  }
}
