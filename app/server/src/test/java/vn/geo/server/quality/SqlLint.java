package vn.geo.server.quality;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Bộ quét SQL tĩnh: trích mọi chuỗi SQL trong mã Java và áp luật chống "smell" truy vấn. Luật:
 * NO_OFFSET, NO_SELECT_STAR, NO_LEADING_WILDCARD_LIKE, LARGE_TABLE_NEEDS_LIMIT,
 * TENANT_FILTER_REQUIRED, TABLE_OWNERSHIP, FORBIDDEN_TABLE, EVENT_TIME_BOUND (E7, BR-GEO-282: đọc
 * bảng sự kiện chia theo tháng phải có cận thời gian trên occurred_at; bí danh {@code timebound}).
 * Ngoại lệ có chủ đích: chú thích trong SQL {@code -- sqllint:allow=<luật>[,<luật>]} (bí danh:
 * {@code unbounded} = LARGE_TABLE_NEEDS_LIMIT, {@code tenant} = TENANT_FILTER_REQUIRED, {@code
 * ownership} = TABLE_OWNERSHIP). FORBIDDEN_TABLE không có ngoại lệ.
 *
 * <p>Tên bảng so khớp không kèm tiền tố schema ({@code geo.problem} = {@code problem}). Khóa cấu
 * hình hỗ trợ: {@link Config#SUPPORTED_KEYS} và {@code owner.<bảng>}; khóa lạ làm quét thất bại
 * (không bao giờ bỏ qua âm thầm).
 */
public final class SqlLint {

  public record Config(
      Set<String> largeTables,
      Set<String> tenantTables,
      Map<String, String> owners,
      Set<String> forbiddenAppTables) {

    public static final Set<String> SUPPORTED_KEYS =
        Set.of("large.tables", "tenant.tables", "forbidden.app.tables");

    public Config(Set<String> largeTables, Set<String> tenantTables, Map<String, String> owners) {
      this(largeTables, tenantTables, owners, Set.of());
    }

    public static Config load(Path file) throws IOException {
      Properties p = new Properties();
      try (var in = Files.newBufferedReader(file)) {
        p.load(in);
      }
      return from(p);
    }

    public static Config from(Properties p) {
      Map<String, String> owners = new HashMap<>();
      for (String k : p.stringPropertyNames()) {
        if (k.startsWith("owner.")) {
          owners.put(k.substring(6).toLowerCase(), p.getProperty(k).trim());
        } else if (!SUPPORTED_KEYS.contains(k)) {
          throw new IllegalArgumentException(
              "Khóa sql-lint chưa được SqlLint hỗ trợ: '"
                  + k
                  + "' (hỗ trợ: "
                  + SUPPORTED_KEYS
                  + ", owner.<bảng>)");
        }
      }
      return new Config(
          list(p.getProperty("large.tables")),
          list(p.getProperty("tenant.tables")),
          owners,
          list(p.getProperty("forbidden.app.tables")));
    }

    private static Set<String> list(String v) {
      if (v == null || v.isBlank()) {
        return Set.of();
      }
      Set<String> s = new HashSet<>();
      Arrays.stream(v.split(","))
          .map(x -> x.trim().toLowerCase())
          .filter(x -> !x.isEmpty())
          .forEach(s::add);
      return s;
    }
  }

  public record Violation(String file, int line, String rule, String message) {
    @Override
    public String toString() {
      return file + ":" + line + " [" + rule + "] " + message;
    }
  }

  public record Literal(int line, String text) {}

  public record ScanResult(int statements, List<Violation> violations) {}

  private static final Pattern IS_SQL =
      Pattern.compile(
          "^\\s*(?:(?:with\\b.*\\bselect\\b.*)|(?:select\\b.*\\bfrom\\b.*)|(?:insert\\s+into\\b.*)|(?:update\\s+\\S+\\s+set\\b.*)|(?:delete\\s+from\\b.*))$",
          Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
  private static final Pattern TABLE_REF =
      Pattern.compile("\\b(?:from|join|into|update)\\s+([a-z_][a-z0-9_.]*)");
  private static final Pattern ALLOW = Pattern.compile("sqllint:allow=([a-z_,]+)");
  private static final Map<String, String> ALLOW_ALIASES =
      Map.of(
          "unbounded", "large_table_needs_limit",
          "tenant", "tenant_filter_required",
          "ownership", "table_ownership",
          "timebound", "event_time_bound");

  /** Bảng chia partition theo thời gian: SELECT phải có cận dưới occurred_at (BR-GEO-282). */
  static final Set<String> TIME_BOUND_TABLES = Set.of("event_log");

  // FIX4b m7: cận phải là vị từ (sau WHERE/AND/ON), không chỉ là chữ "occurred_at >" ở bất kỳ đâu
  // (SELECT, ORDER BY)
  private static final Pattern TIME_BOUND =
      Pattern.compile("(?i)\\b(where|and|on)\\s+(\\w+\\.)?occurred_at\\s*(>=|>|between\\b)");
  private static final Set<String> NOT_SUPPRESSIBLE = Set.of("forbidden_table");

  /** Quét mọi .java dưới root (ví dụ src/main/java). */
  public static ScanResult scan(Path root, Config cfg) throws IOException {
    List<Violation> all = new ArrayList<>();
    int count = 0;
    try (Stream<Path> files = Files.walk(root)) {
      for (Path f : files.filter(p -> p.toString().endsWith(".java")).sorted().toList()) {
        String rel = root.relativize(f).toString();
        for (Literal lit : extractLiterals(Files.readString(f))) {
          if (looksLikeSql(lit.text())) {
            count++;
            all.addAll(lint(lit.text(), moduleOf(rel), cfg, rel, lit.line()));
          }
        }
      }
    }
    return new ScanResult(count, all);
  }

  /** Module = thư mục ngay dưới vn/geo/server. */
  static String moduleOf(String relPath) {
    String marker = "vn/geo/server/";
    int i = relPath.indexOf(marker);
    String rest = i >= 0 ? relPath.substring(i + marker.length()) : relPath;
    int slash = rest.indexOf('/');
    return slash < 0 ? "" : rest.substring(0, slash);
  }

  public static boolean looksLikeSql(String text) {
    return IS_SQL.matcher(text).matches();
  }

  public static List<Violation> lint(
      String rawSql, String module, Config cfg, String file, int line) {
    Set<String> allowed = new HashSet<>();
    Matcher am = ALLOW.matcher(rawSql.toLowerCase());
    while (am.find()) {
      for (String a : am.group(1).split(",")) {
        allowed.add(ALLOW_ALIASES.getOrDefault(a, a));
      }
    }
    allowed.removeAll(NOT_SUPPRESSIBLE);
    String sql =
        rawSql
            .toLowerCase()
            .replaceAll("--[^\\n]*", " ")
            .replaceAll("/\\*.*?\\*/", " ")
            .replaceAll("\\s+", " ")
            .trim();
    String forTables = sql.replaceAll("\\bfor update\\b", " ").replaceAll("\\bdo update\\b", " ");
    List<String> tables = new ArrayList<>();
    Matcher tm = TABLE_REF.matcher(forTables);
    while (tm.find()) {
      String t = tm.group(1);
      tables.add(t.substring(t.lastIndexOf('.') + 1));
    }
    boolean isSelect = sql.startsWith("select") || sql.startsWith("with");
    boolean isInsert = sql.startsWith("insert");
    List<Violation> v = new ArrayList<>();
    Check c =
        (rule, msg) -> {
          if (!allowed.contains(rule.toLowerCase())) {
            v.add(new Violation(file, line, rule, msg + " :: " + abbreviate(sql)));
          }
        };

    if (Pattern.compile("\\boffset\\b").matcher(sql).find()) {
      c.add("NO_OFFSET", "Cấm OFFSET; dùng phân trang theo khóa (keyset).");
    }
    if (Pattern.compile("\\bselect (distinct )?\\*|[\\s,(]\\w+\\.\\*|\\breturning \\*")
        .matcher(sql)
        .find()) {
      c.add("NO_SELECT_STAR", "Cấm SELECT * / alias.* / RETURNING *; liệt kê cột cần dùng.");
    }
    if (Pattern.compile(
            "\\bi?like\\s+(any\\s*\\()?\\(?\\s*(e)?'%|\\bi?like\\s+(concat\\()?\\s*'%'\\s*(\\|\\||,)")
        .matcher(sql)
        .find()) {
      c.add(
          "NO_LEADING_WILDCARD_LIKE",
          "Cấm LIKE bắt đầu bằng %; không dùng được chỉ mục (cần trigram/FTS có duyệt).");
    }
    if (isSelect && !Pattern.compile("\\blimit\\b").matcher(sql).find()) {
      for (String t : tables) {
        if (cfg.largeTables().contains(t)) {
          c.add("LARGE_TABLE_NEEDS_LIMIT", "SELECT trên bảng lớn '" + t + "' phải có LIMIT.");
          break;
        }
      }
    }
    if (isSelect
        && tables.stream().anyMatch(TIME_BOUND_TABLES::contains)
        && !TIME_BOUND.matcher(sql).find()) {
      c.add(
          "EVENT_TIME_BOUND",
          "Đọc bảng sự kiện phải có cận thời gian (occurred_at >= …) để chỉ chạm partition cần thiết.");
    }
    long tenantRefs = tables.stream().filter(cfg.tenantTables()::contains).count();
    if (tenantRefs > 0) {
      if (isInsert) {
        if (!Pattern.compile("insert into [a-z_.0-9]+ ?\\([^)]*\\borg_id\\b").matcher(sql).find()) {
          c.add("TENANT_FILTER_REQUIRED", "INSERT vào bảng thuộc trường phải ghi cột org_id.");
        }
      } else {
        Matcher m = Pattern.compile("\\borg_id = :orgid\\b").matcher(sql);
        long found = m.results().count();
        if (found < tenantRefs) {
          c.add(
              "TENANT_FILTER_REQUIRED",
              "Bảng thuộc trường phải lọc 'org_id = :orgId' (mỗi bảng một điều kiện; :orgId do TenantJdbc cấp).");
        }
      }
    }
    for (String t : tables) {
      if (cfg.forbiddenAppTables().contains(t)) {
        c.add(
            "FORBIDDEN_TABLE",
            "Mã ứng dụng không được tham chiếu bảng nhạy cảm '"
                + t
                + "'; đi qua hàm cổng của CSDL (không có ngoại lệ).");
      }
    }
    for (String t : tables) {
      String owner = cfg.owners().get(t);
      if (owner != null && !owner.equals(module)) {
        c.add(
            "TABLE_OWNERSHIP",
            "Bảng '"
                + t
                + "' thuộc module '"
                + owner
                + "', không được truy cập từ '"
                + module
                + "'.");
      }
    }
    return v;
  }

  private interface Check {
    void add(String rule, String message);
  }

  private static String abbreviate(String s) {
    return s.length() > 120 ? s.substring(0, 120) + "..." : s;
  }

  /**
   * Trích chuỗi ký tự và text block khỏi mã Java (bỏ qua chú thích), gộp các literal nối bằng '+'.
   */
  public static List<Literal> extractLiterals(String src) {
    List<int[]> spans = new ArrayList<>(); // start, end(exclusive), line
    List<String> texts = new ArrayList<>();
    int n = src.length();
    int line = 1;
    int i = 0;
    while (i < n) {
      char ch = src.charAt(i);
      if (ch == '\n') {
        line++;
        i++;
      } else if (src.startsWith("//", i)) {
        while (i < n && src.charAt(i) != '\n') {
          i++;
        }
      } else if (src.startsWith("/*", i)) {
        int end = src.indexOf("*/", i + 2);
        end = end < 0 ? n : end + 2;
        for (int k = i; k < end; k++) {
          if (src.charAt(k) == '\n') {
            line++;
          }
        }
        i = end;
      } else if (src.startsWith("\"\"\"", i)) {
        int startLine = line;
        int j = i + 3;
        StringBuilder sb = new StringBuilder();
        while (j < n && !src.startsWith("\"\"\"", j)) {
          char d = src.charAt(j);
          if (d == '\\' && j + 1 < n) {
            sb.append(src.charAt(j + 1));
            j += 2;
            continue;
          }
          if (d == '\n') {
            line++;
          }
          sb.append(d);
          j++;
        }
        spans.add(new int[] {i, Math.min(j + 3, n), startLine});
        texts.add(sb.toString());
        i = Math.min(j + 3, n);
      } else if (ch == '"') {
        int j = i + 1;
        StringBuilder sb = new StringBuilder();
        while (j < n && src.charAt(j) != '"' && src.charAt(j) != '\n') {
          if (src.charAt(j) == '\\' && j + 1 < n) {
            sb.append(src.charAt(j + 1));
            j += 2;
          } else {
            sb.append(src.charAt(j++));
          }
        }
        spans.add(new int[] {i, Math.min(j + 1, n), line});
        texts.add(sb.toString());
        i = Math.min(j + 1, n);
      } else if (ch == '\'') {
        int j = i + 1;
        while (j < n && src.charAt(j) != '\'' && src.charAt(j) != '\n') {
          j += src.charAt(j) == '\\' ? 2 : 1;
        }
        i = Math.min(j + 1, n);
      } else {
        i++;
      }
    }
    List<Literal> out = new ArrayList<>();
    for (int k = 0; k < spans.size(); k++) {
      StringBuilder merged = new StringBuilder(texts.get(k));
      int startLine = spans.get(k)[2];
      while (k + 1 < spans.size()
          && src.substring(spans.get(k)[1], spans.get(k + 1)[0]).matches("\\s*\\+\\s*")) {
        k++;
        merged.append(texts.get(k));
      }
      out.add(new Literal(startLine, merged.toString()));
    }
    return out;
  }

  private SqlLint() {}
}
