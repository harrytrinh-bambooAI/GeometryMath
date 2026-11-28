package vn.geo.server.quality;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import vn.geo.server.iam.internal.code.FieldNames;

/**
 * US-S3E5-03: danh sách trường dữ liệu được phép thu ({@code allowed_fields.tsv} cạnh tệp này, máy
 * đọc được, BR-S3E5-30). Mỗi dòng: {@code loại<TAB>thực thể<TAB>trường<TAB>lý do<TAB>mã quyết
 * định}.
 *
 * <ul>
 *   <li>{@code db}: thực thể = bảng (bảng con phân vùng {@code _yYYYYmMM}, {@code _default} tính
 *       theo bảng cha), trường = cột.
 *   <li>{@code api}: thực thể = {@code api}, trường = khóa JSON của phản hồi hoặc thân yêu cầu (mọi
 *       cấp).
 * </ul>
 *
 * Tên khớp mẫu cấm (BR-S3E5-33) chỉ được có mặt khi lý do bắt đầu bằng "NGOẠI LỆ" (ngoại lệ có tên,
 * ví dụ ảnh đề {@code problem_image} PO-65 Q4). Mã trường kỹ thuật {@code org_id} chỉ được ở loại
 * {@code db} (BR-S3E5-36).
 */
public final class AllowedFields {

  public static final Path FILE = Path.of("src/test/java/vn/geo/server/quality/allowed_fields.tsv");
  public static final String EXCEPTION = "NGOẠI LỆ";
  private static final Pattern DECISION = Pattern.compile("PO-\\d{2,3}");
  private static final Pattern PARTITION = Pattern.compile("_(y\\d{4}m\\d{2}|default)$");

  public record Entry(
      int line, String kind, String entity, String field, String reason, String decision) {
    String key() {
      return kind + ":" + entity + "." + field;
    }
  }

  private final List<Entry> entries;

  private AllowedFields(List<Entry> entries) {
    this.entries = entries;
  }

  public static AllowedFields load() {
    try {
      return parse(Files.readAllLines(FILE));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /** Bỏ dòng trống và dòng chú thích {@code #}; dòng thiếu cột vẫn giữ để {@link #lint} báo. */
  public static AllowedFields parse(List<String> lines) {
    List<Entry> out = new ArrayList<>();
    for (int i = 0; i < lines.size(); i++) {
      String l = lines.get(i);
      if (l.isBlank() || l.startsWith("#")) {
        continue;
      }
      String[] c = l.split("\t", -1);
      out.add(new Entry(i + 1, col(c, 0), col(c, 1), col(c, 2), col(c, 3), col(c, 4)));
    }
    return new AllowedFields(out);
  }

  private static String col(String[] c, int i) {
    return i < c.length ? c[i].strip() : "";
  }

  public List<Entry> entries() {
    return entries;
  }

  /** Lỗi của tệp danh sách (BR-S3E5-32, 33, 36): mỗi lỗi một chuỗi nêu số dòng. */
  public List<String> lint() {
    List<String> problems = new ArrayList<>();
    Set<String> seen = new TreeSet<>();
    for (Entry e : entries) {
      String at = "dòng " + e.line() + " (" + e.entity() + "." + e.field() + ")";
      if (!Set.of("db", "api").contains(e.kind()) || e.entity().isEmpty() || e.field().isEmpty()) {
        problems.add(at + ": thiếu loại/thực thể/trường");
      }
      if (e.reason().isEmpty()) {
        problems.add(at + ": thiếu lý do");
      }
      if (!DECISION.matcher(e.decision()).matches()) {
        problems.add(at + ": thiếu mã quyết định dạng PO-nn");
      }
      if (!seen.add(e.key())) {
        problems.add(at + ": trùng dòng");
      }
      Optional<String> bad = FieldNames.forbiddenPattern(e.field());
      if (bad.isPresent() && !e.reason().startsWith(EXCEPTION)) {
        problems.add(at + ": tên khớp mẫu cấm '" + bad.get() + "' mà không phải ngoại lệ có tên");
      }
      if ("api".equals(e.kind()) && FieldNames.isTechnical(e.field())) {
        problems.add(at + ": mã trường kỹ thuật không được ra API");
      }
    }
    return problems;
  }

  /** Tên bảng gốc của một bảng con phân vùng. */
  public static String baseTable(String table) {
    return PARTITION.matcher(table).replaceFirst("");
  }

  /**
   * Cột ({@code bảng.cột}) không có trong danh sách hoặc khớp mẫu cấm mà không là ngoại lệ có tên.
   * Kết quả: tên → lý do.
   */
  public Map<String, String> outsideDb(Collection<String> tableDotColumns) {
    Map<String, Entry> db = new TreeMap<>();
    entries.stream()
        .filter(e -> "db".equals(e.kind()))
        .forEach(e -> db.put(e.entity() + "." + e.field(), e));
    Map<String, String> out = new TreeMap<>();
    for (String tc : tableDotColumns) {
      int dot = tc.indexOf('.');
      String name = baseTable(tc.substring(0, dot)) + tc.substring(dot);
      String col = tc.substring(dot + 1);
      Entry e = db.get(name);
      out.putAll(judge(name, col, e));
    }
    return out;
  }

  /** Khóa JSON không có trong danh sách {@code api} hoặc khớp mẫu cấm / là mã trường kỹ thuật. */
  public Map<String, String> outsideApi(Collection<String> keys) {
    Map<String, Entry> api = new TreeMap<>();
    entries.stream().filter(e -> "api".equals(e.kind())).forEach(e -> api.put(e.field(), e));
    Map<String, String> out = new TreeMap<>();
    for (String k : keys) {
      if (FieldNames.isTechnical(k)) {
        out.put(k, "mã trường kỹ thuật không được ra API (BR-S3E5-36)");
        continue;
      }
      out.putAll(judge(k, k, api.get(k)));
    }
    return out;
  }

  private static Map<String, String> judge(String name, String field, Entry e) {
    Optional<String> bad = FieldNames.forbiddenPattern(field);
    if (bad.isPresent() && (e == null || !e.reason().startsWith(EXCEPTION))) {
      return Map.of(name, "khớp mẫu cấm '" + bad.get() + "' (BR-S3E5-33)");
    }
    if (e == null) {
      return Map.of(name, "ngoài danh sách allowed_fields (BR-S3E5-31)");
    }
    return Map.of();
  }
}
