package vn.geo.server.content.internal.variant.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tìm và thay số liệu của đề trong câu chữ (US-S2E9-01, BR-S2E9-01: giữ cấu trúc đề, chỉ đổi số).
 * Hàm thuần. Một dữ kiện chỉ được đổi khi tìm thấy đúng con số của nó trong đề bài (ưu tiên chỗ có
 * nhãn "AB = 6", không thì con số đứng riêng đầu tiên còn trống); không tìm thấy thì dữ kiện đó giữ
 * nguyên để chữ đề và hình luôn khớp nhau.
 */
public final class StatementNumbers {

  private static final String NUM = "(\\d+(?:[.,]\\d+)?)";
  private static final Pattern TOKEN =
      Pattern.compile("(?<![\\p{L}\\p{N},.√^_])" + NUM + "(?![\\p{L}\\p{N}]|[.,]\\d)");

  private StatementNumbers() {}

  /** Một dữ kiện số của đề: vị trí trong mô tả dựng, loại, các điểm, giá trị gốc. */
  public record Param(int index, String type, List<String> of, double value) {}

  /** Chỗ con số của dữ kiện {@code index} nằm trong đề bài: [start, end). */
  public record Located(int index, int start, int end) {}

  public static List<Located> locate(String statement, List<Param> params) {
    List<Located> out = new ArrayList<>();
    Set<Integer> used = new HashSet<>();
    List<Param> rest = new ArrayList<>();
    for (Param p : params) {
      Located l = byLabel(statement, p, used);
      if (l != null) {
        out.add(l);
      } else {
        rest.add(p);
      }
    }
    for (Param p : rest) {
      Matcher m = TOKEN.matcher(statement);
      while (m.find()) {
        if (!used.contains(m.start(1)) && same(m.group(1), p.value())) {
          used.add(m.start(1));
          out.add(new Located(p.index(), m.start(1), m.end(1)));
          break;
        }
      }
    }
    out.sort(Comparator.comparingInt(Located::start));
    return out;
  }

  private static Located byLabel(String statement, Param p, Set<Integer> used) {
    String label;
    List<String> of = p.of() == null ? List.of() : p.of();
    switch (p.type()) {
      case "length" -> {
        if (of.size() != 2) {
          return null;
        }
        String a = Pattern.quote(of.get(0));
        String b = Pattern.quote(of.get(1));
        label = "(?<![\\p{L}\\p{N}])(?:" + a + b + "|" + b + a + ")\\s*(?:=|bằng|dài)\\s*";
      }
      case "radius" -> label = "(?:bán kính|(?<![\\p{L}\\p{N}])R)\\s*(?:=|bằng)?\\s*";
      case "height" -> label = "(?:chiều cao|đường cao)\\s*(?:=|bằng)?\\s*";
      case "angle" -> {
        if (of.size() != 3) {
          return null;
        }
        String a = Pattern.quote(of.get(0));
        String o = Pattern.quote(of.get(1));
        String b = Pattern.quote(of.get(2));
        label = "góc\\s*(?:" + a + o + b + "|" + b + o + a + "|" + o + ")\\s*(?:=|bằng)?\\s*";
      }
      default -> {
        return null;
      }
    }
    Matcher m = Pattern.compile(label + NUM + "(?![\\p{L}\\p{N}]|[.,]\\d)").matcher(statement);
    while (m.find()) {
      if (!used.contains(m.start(1)) && same(m.group(1), p.value())) {
        used.add(m.start(1));
        return new Located(p.index(), m.start(1), m.end(1));
      }
    }
    return null;
  }

  /** Thay con số ở các chỗ đã định vị bằng giá trị mới (theo vị trí dữ kiện). */
  public static String rewrite(String statement, List<Located> located, Map<Integer, Double> next) {
    List<Located> order = new ArrayList<>(located);
    order.sort(Comparator.comparingInt(Located::start).reversed());
    StringBuilder sb = new StringBuilder(statement);
    for (Located l : order) {
      Double v = next.get(l.index());
      if (v != null) {
        sb.replace(l.start(), l.end(), format(v));
      }
    }
    return sb.toString();
  }

  /** Số kiểu Việt Nam: dấu phẩy thập phân, tối đa một chữ số thập phân, bỏ ",0". */
  public static String format(double v) {
    if (Math.abs(v - Math.rint(v)) < 1e-9) {
      return Long.toString((long) Math.rint(v));
    }
    return String.format(Locale.ROOT, "%.1f", v).replace('.', ',');
  }

  private static boolean same(String token, double value) {
    try {
      return Math.abs(Double.parseDouble(token.replace(',', '.')) - value) < 1e-9;
    } catch (NumberFormatException e) {
      return false;
    }
  }
}
