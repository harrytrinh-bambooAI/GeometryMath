package vn.geo.server.tracking.internal.classboard.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * S3-E3 — đầu vào và đầu ra của bộ gộp "Cả lớp vướng ở đâu" (thuần, không I/O).
 *
 * <p>Đầu vào ẩn danh (BR-S3E3-36): chỉ mã đề (vị trí trong bộ), số bước và nội dung câu hỏi / đáp
 * số đã che; mỗi bài làm chỉ mang số thứ tự. Không tên hiển thị, mã người dùng, mã lớp hay tên giáo
 * viên.
 *
 * <p>Đầu ra: JSON {@code {"insights":[{"step":1,"text":"…","works":[1,2]}]}}. Nhận định không còn
 * bài làm căn cứ hợp lệ bị loại; n = số căn cứ hợp lệ (BR-S3E3-33); câu có phần trăm / xếp hạng bị
 * cắt (BR-S3E3-03); nhãn do nhà cung cấp tự gắn bị bỏ.
 */
public final class StuckDigest {

  /** Bài làm ẩn danh của một em trên một đề. */
  public record Sheet(List<Integer> hintLevels, List<String> questions, List<String> wrongAnswers) {

    /** Bước em dừng (TẠM): mức gợi ý cao nhất đã xin; 0 = còn ở bước đọc đề. */
    public int stoppedStep() {
      return hintLevels.stream().mapToInt(Integer::intValue).max().orElse(0);
    }
  }

  /** Nhận định đã lọc: bước, câu chữ, số thứ tự (từ 1) các bài làm căn cứ. */
  public record Insight(int step, String text, List<Integer> works) {}

  private static final JsonMapper MAPPER = JsonMapper.builder().build();
  private static final int TEXT_MAX = 300;

  private StuckDigest() {}

  public static String prompt(int position, List<Sheet> sheets) {
    StringBuilder b = new StringBuilder();
    b.append(
            "Nhiệm vụ: gộp các bài làm ẩn danh của một đề thành nhận định \"nhiều em dừng ở bước nào\".\n")
        .append(
            "Chỉ trả JSON {\"insights\":[{\"step\":<số bước>,\"text\":\"<một câu>\",\"works\":[<số thứ tự bài làm căn cứ>]}]}.\n")
        .append("Không nêu tên, không phần trăm, không xếp hạng, không gắn nhãn kiểm chứng.\n")
        .append("Mã đề: D")
        .append(position)
        .append('\n');
    int i = 0;
    for (Sheet s : sheets) {
      i++;
      b.append("Bài làm ").append(i).append(": bước dừng ").append(s.stoppedStep());
      if (!s.hintLevels().isEmpty()) {
        b.append("; gợi ý đã xin mức ").append(s.hintLevels());
      }
      for (String q : s.questions()) {
        b.append("; câu hỏi: \"").append(clip(BoardRules.mask(q))).append('"');
      }
      for (String a : s.wrongAnswers()) {
        b.append("; đáp số chưa khớp: \"").append(clip(BoardRules.mask(a))).append('"');
      }
      b.append('\n');
    }
    return b.toString();
  }

  /** Đáp mẫu của bộ gộp giả lập khi ca kiểm không đặt kịch bản: bước nhiều em dừng nhất. */
  public static String mockReply(List<Sheet> sheets) {
    int best = 0;
    int bestCount = -1;
    for (int step = 0; step <= 2; step++) {
      int n = 0;
      for (Sheet s : sheets) {
        if (s.stoppedStep() == step) {
          n++;
        }
      }
      if (n > bestCount) {
        best = step;
        bestCount = n;
      }
    }
    StringBuilder works = new StringBuilder();
    for (int i = 0; i < sheets.size(); i++) {
      if (sheets.get(i).stoppedStep() == best) {
        works.append(works.isEmpty() ? "" : ",").append(i + 1);
      }
    }
    String text =
        best == 0
            ? "Nhiều em dừng ngay ở bước đọc đề, chưa xin gợi ý nào."
            : "Nhiều em dừng ở bước " + best + ", sau khi xin gợi ý mức " + best + ".";
    return "{\"insights\":[{\"step\":"
        + best
        + ",\"text\":\""
        + text
        + "\",\"works\":["
        + works
        + "]}]}";
  }

  /** Đọc trả lời của bộ gộp; null khi không phải JSON đúng dạng (coi như bộ gộp lỗi). */
  public static List<Insight> parse(String content, int sheetCount) {
    JsonNode root;
    try {
      String c = content == null ? "" : content.strip();
      int from = c.indexOf('{');
      int to = c.lastIndexOf('}');
      if (from < 0 || to <= from) {
        return null;
      }
      root = MAPPER.readTree(c.substring(from, to + 1));
    } catch (RuntimeException e) {
      return null;
    }
    JsonNode list = root.path("insights");
    if (!list.isArray()) {
      return null;
    }
    List<Insight> out = new ArrayList<>();
    for (JsonNode n : list) {
      String text = BoardRules.cleanInsight(n.path("text").asString(""));
      Set<Integer> works = new LinkedHashSet<>();
      boolean unknown = false;
      for (JsonNode w : n.path("works")) {
        int i = w.asInt(-1);
        if (i >= 1 && i <= sheetCount) {
          works.add(i);
        } else {
          unknown = true;
        }
      }
      if (text.isBlank() || works.isEmpty() || unknown) {
        continue; // hết câu chữ hợp lệ, không có căn cứ hoặc dẫn bài làm không tồn tại: loại
        // (BR-S3E3-33)
      }
      out.add(new Insight(Math.max(0, n.path("step").asInt(0)), text, List.copyOf(works)));
    }
    return out;
  }

  public static String sha256(String s) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  private static String clip(String s) {
    String t = s.replace('\n', ' ').replace('"', '\'');
    return t.length() > TEXT_MAX ? t.substring(0, TEXT_MAX) : t;
  }
}
