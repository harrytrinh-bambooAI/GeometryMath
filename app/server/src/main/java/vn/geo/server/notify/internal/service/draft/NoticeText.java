package vn.geo.server.notify.internal.service.draft;

import java.text.Normalizer;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import vn.geo.server.common.config.TimeProperties;

/**
 * Xử lý văn bản nháp thông báo (US-S5E5-01; BR-S5E5-01, 03, 14). Thuần logic, không I/O:
 *
 * <ul>
 *   <li>{@link #scrub}: TRƯỚC khi gửi AI, thay mọi tên hiển thị và mã người dùng của học sinh trong
 *       lớp (so cả bản bỏ dấu) bằng "[HS]".
 *   <li>{@link #check}: SAU khi AI trả, gỡ tên/mã học sinh và thay giờ/ngày sai bằng dữ kiện đúng
 *       của máy chủ; câu bị sửa gắn "AI chưa chắc".
 * </ul>
 */
public final class NoticeText {

  public static final String PLACEHOLDER = "[HS]";
  public static final String UNSURE = "AI chưa chắc";
  public static final int MAX = 1000;

  static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");
  static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final Pattern TIME =
      Pattern.compile(
          "(?<![\\d:/])(\\d{1,2})(?::(\\d{2})|\\s*(?:h|giờ)(?!\\p{L})\\s*(\\d{2})?)(?![\\d])");
  private static final Pattern DATE =
      Pattern.compile("(?<![\\d/])(\\d{1,2})/(\\d{1,2})/(\\d{4})(?![\\d/])");
  private static final String[] WEEKDAYS = {
    "thứ Hai", "thứ Ba", "thứ Tư", "thứ Năm", "thứ Sáu", "thứ Bảy", "Chủ nhật"
  };

  private NoticeText() {}

  /** Một câu của nháp; {@code unsure} = máy chủ đã sửa chỗ này ("AI chưa chắc"). */
  public record Sentence(String text, boolean unsure, String why) {}

  /** Chữ thường, bỏ dấu tiếng Việt (đ → d), gộp khoảng trắng. */
  public static String fold(String s) {
    String n =
        Normalizer.normalize(s == null ? "" : s, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
    return n.replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
  }

  /** Thay tên hiển thị / mã học sinh (có dấu hoặc bỏ dấu, không phân biệt hoa thường) bằng [HS]. */
  public static String scrub(String text, Collection<String> names) {
    if (text == null || text.isBlank()) {
      return text == null ? "" : text;
    }
    String out = text;
    for (String name : sorted(names)) {
      out = replaceFolded(out, name, PLACEHOLDER);
    }
    return out;
  }

  /** Văn bản có chứa tên/mã học sinh nào không (so bỏ dấu). */
  public static boolean mentions(String text, Collection<String> names) {
    String f = fold(text);
    for (String n : names) {
      String fn = fold(n).trim();
      if (fn.length() >= 2
          && Pattern.compile("(?<![\\p{L}\\d])" + Pattern.quote(fn) + "(?![\\p{L}\\d])")
              .matcher(f)
              .find()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Kiểm từng câu AI trả: gỡ tên học sinh (BR-S5E5-14), thay giờ/ngày lệch hạn của máy chủ
   * (BR-S5E5-03). {@code due} null = loại thông báo không có hạn (không kiểm giờ).
   */
  public static List<Sentence> check(
      List<String> sentences, Collection<String> names, Instant due) {
    List<Sentence> out = new ArrayList<>();
    ZonedDateTime d = due == null ? null : due.atZone(TimeProperties.VIETNAM);
    for (String raw : sentences) {
      if (raw == null || raw.isBlank()) {
        continue;
      }
      String s = raw.trim();
      List<String> why = new ArrayList<>();
      if (mentions(s, names)) {
        for (String n : sorted(names)) {
          s = removeName(s, n);
        }
        why.add("tên học sinh");
      }
      if (d != null) {
        String fixed = fixTimes(s, d);
        if (!fixed.equals(s)) {
          s = fixed;
          why.add("giờ hạn");
        }
        String fixedDate = fixDates(s, d);
        if (!fixedDate.equals(s)) {
          s = fixedDate;
          why.add("ngày hạn");
        }
      }
      s = s.replaceAll("\\s{2,}", " ").trim();
      if (!s.isEmpty()) {
        s = Character.toUpperCase(s.charAt(0)) + s.substring(1);
      }
      out.add(new Sentence(s, !why.isEmpty(), why.isEmpty() ? null : String.join(", ", why)));
    }
    return out;
  }

  /** "20:00, 11/12/2026" (giờ Việt Nam). */
  public static String dueLabel(Instant due) {
    ZonedDateTime d = due.atZone(TimeProperties.VIETNAM);
    return HM.format(d) + ", " + DMY.format(d);
  }

  public static String weekday(Instant due) {
    return WEEKDAYS[due.atZone(TimeProperties.VIETNAM).getDayOfWeek().getValue() - 1];
  }

  public static String join(List<Sentence> sentences) {
    return String.join(" ", sentences.stream().map(Sentence::text).toList());
  }

  private static String fixTimes(String s, ZonedDateTime d) {
    Matcher m = TIME.matcher(s);
    StringBuilder b = new StringBuilder();
    while (m.find()) {
      int h = Integer.parseInt(m.group(1));
      String mm = m.group(2) != null ? m.group(2) : m.group(3);
      int min = mm == null ? 0 : Integer.parseInt(mm);
      boolean ok = h == d.getHour() && min == d.getMinute();
      m.appendReplacement(b, Matcher.quoteReplacement(ok ? m.group() : HM.format(d)));
    }
    m.appendTail(b);
    return b.toString();
  }

  private static String fixDates(String s, ZonedDateTime d) {
    Matcher m = DATE.matcher(s);
    StringBuilder b = new StringBuilder();
    while (m.find()) {
      int day = Integer.parseInt(m.group(1));
      int mon = Integer.parseInt(m.group(2));
      boolean ok =
          day == d.getDayOfMonth()
              && mon == d.getMonthValue()
              && Integer.parseInt(m.group(3)) == d.getYear();
      m.appendReplacement(b, Matcher.quoteReplacement(ok ? m.group() : DMY.format(d)));
    }
    m.appendTail(b);
    return b.toString();
  }

  private static String removeName(String s, String name) {
    String f = fold(name).trim();
    if (f.length() < 2) {
      return s;
    }
    // bỏ cả "và"/"," nối theo sau tên ("Nhắc Chi Mai và cả lớp …" → "Nhắc cả lớp …")
    return replaceFolded(s, name, "", "(\\s*(,|và|va)\\s+)?");
  }

  private static String replaceFolded(String text, String name, String repl) {
    return replaceFolded(text, name, repl, "");
  }

  /** Thay theo bản bỏ dấu: NFD giữ độ dài theo ký tự gốc nên so khớp trên từng vị trí. */
  private static String replaceFolded(String text, String name, String repl, String tail) {
    String fn = fold(name).trim();
    if (fn.length() < 2) {
      return text;
    }
    StringBuilder folded = new StringBuilder();
    List<Integer> map = new ArrayList<>();
    for (int i = 0; i < text.length(); i++) {
      String f = fold(String.valueOf(text.charAt(i)));
      for (int k = 0; k < f.length(); k++) {
        folded.append(f.charAt(k));
        map.add(i);
      }
    }
    Matcher m =
        Pattern.compile("(?<![\\p{L}\\d])" + Pattern.quote(fn) + "(?![\\p{L}\\d])" + tail)
            .matcher(folded);
    StringBuilder b = new StringBuilder();
    int last = 0;
    while (m.find()) {
      int start = map.get(m.start());
      int end = m.end() >= map.size() ? text.length() : map.get(m.end());
      b.append(text, last, start).append(repl);
      last = end;
    }
    b.append(text.substring(last));
    return b.toString();
  }

  private static List<String> sorted(Collection<String> names) {
    return names.stream()
        .filter(n -> n != null && !n.isBlank())
        .sorted((a, c) -> Integer.compare(c.length(), a.length()))
        .toList();
  }
}
