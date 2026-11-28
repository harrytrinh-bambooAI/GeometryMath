package vn.geo.server.knowledge.internal.kb.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import vn.geo.server.knowledge.internal.kb.repo.KbRepository.Section;

/**
 * Tách nội dung Markdown thành mục có tiêu đề (BR-S5E3-03) và băm nội dung sau chuẩn hoá
 * (BR-S5E3-04). Hàm thuần.
 *
 * <ul>
 *   <li>Mỗi dòng tiêu đề Markdown ({@code #…######}) mở một mục; thân = các dòng tới tiêu đề kế.
 *   <li>Tiêu đề không có thân (ví dụ tiêu đề tài liệu ngay trước tiêu đề con) không thành mục.
 *   <li>Chữ trước tiêu đề đầu tiên thành mục mang tên tài liệu.
 *   <li>Mã kiến thức dạng {@code KT11-C07-CT-003} trong tiêu đề hoặc thân gắn vào mục.
 * </ul>
 */
public final class KbSplitter {

  private KbSplitter() {}

  static final Pattern HEADING = Pattern.compile("^\\s{0,3}(#{1,6})\\s+(.+?)\\s*#*\\s*$");
  static final Pattern CODE = Pattern.compile("\\bKT\\d{2}(?:-[A-Z0-9]+)+\\b");
  static final int MAX_TITLE = 300;
  public static final int MAX_SECTIONS = 2000;

  public static List<Section> split(String markdown, String docTitle) {
    List<Section> out = new ArrayList<>();
    String title = null;
    StringBuilder body = new StringBuilder();
    for (String line : markdown.split("\\R", -1)) {
      Matcher m = HEADING.matcher(line);
      if (m.matches()) {
        flush(out, title == null ? docTitle : title, body);
        title = m.group(2).strip();
        body.setLength(0);
      } else {
        body.append(line).append('\n');
      }
    }
    flush(out, title == null ? docTitle : title, body);
    return out.size() > MAX_SECTIONS ? out.subList(0, MAX_SECTIONS) : out;
  }

  private static void flush(List<Section> out, String title, StringBuilder body) {
    String b = body.toString().strip();
    if (b.isEmpty()) {
      return;
    }
    String t = title.length() > MAX_TITLE ? title.substring(0, MAX_TITLE) : title;
    out.add(new Section(out.size() + 1, t, b, codes(t + "\n" + b)));
  }

  static List<String> codes(String text) {
    LinkedHashSet<String> s = new LinkedHashSet<>();
    Matcher m = CODE.matcher(text);
    while (m.find()) {
      s.add(m.group());
    }
    return List.copyOf(s);
  }

  /** Băm SHA-256 của nội dung sau chuẩn hoá: NFC, chữ thường, gộp khoảng trắng. */
  public static String hash(String content) {
    String n =
        Normalizer.normalize(content, Normalizer.Form.NFC)
            .toLowerCase(Locale.ROOT)
            .replaceAll("[\\s\\u00A0]+", " ")
            .strip();
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256").digest(n.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
