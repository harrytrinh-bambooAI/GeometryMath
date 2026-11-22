package vn.geo.server.learning.internal.service;

import java.text.Normalizer;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.geometry.api.AnswerMatch;
import vn.geo.server.learning.api.AnswerGuardApi;
import vn.geo.server.learning.internal.repo.HelpRepository;

/**
 * Kiểm soát đáp án tối thiểu của E4 (xem {@link AnswerGuardApi}). Khi lời giải còn khóa: các dạng
 * viết của đáp số (giá trị sau dấu "=", dạng độ "45°" / "45 độ" / radian / chữ, giá trị thập phân
 * 2–4 chữ số với dấu phẩy và dấu chấm), các kết luận "vế trái = vế phải cuối" trong từng bước (ví
 * dụ "tan SDA = 1") và nguyên văn từng bước. So khớp không phân biệt hoa thường, bỏ khoảng trắng.
 * Không bắt đáp số một ký tự (ví dụ "a", F-GEO-116: việc của bộ kiểm đầy đủ E6).
 */
@Service
public class AnswerGuard implements AnswerGuardApi {

  static final String VERSION = "g1-e4";

  private static final Pattern DEG = Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*°");
  private static final Map<String, List<String>> SPECIAL_DEG =
      Map.of(
          "30", List.of("π/6", "ba mươi độ"),
          "45", List.of("π/4", "bốn mươi lăm độ"),
          "60", List.of("π/3", "sáu mươi độ"),
          "90", List.of("π/2", "chín mươi độ"),
          "120", List.of("2π/3", "một trăm hai mươi độ"));

  private final HelpRepository repo;
  private final JsonMapper mapper;

  public AnswerGuard(HelpRepository repo, JsonMapper mapper) {
    this.repo = repo;
    this.mapper = mapper;
  }

  @Override
  public String version() {
    return VERSION;
  }

  @Override
  public Screened screen(UUID assignmentId, String reply) {
    AuthPrincipal p = Roles.require(Role.STUDENT);
    HelpRepository.GuardContext g = repo.guardContext(p.userId(), assignmentId);
    if (!"OK".equals(g.status()) || !g.locked() || reply == null) {
      return new Screened(false, reply);
    }
    Set<String> terms = new LinkedHashSet<>();
    addAnswer(terms, g.finalAnswer());
    addAnswer(terms, g.ownFinalAnswer());
    addSteps(terms, g.stepsJson());
    addSteps(terms, g.ownStepsJson());
    String r = norm(reply);
    for (String t : terms) {
      String n = norm(t);
      if (n.length() >= 2 && r.contains(n)) {
        List<HelpRepository.SeenHint> seen = repo.seenHints(p.userId(), assignmentId);
        String highest = seen.isEmpty() ? null : seen.get(seen.size() - 1).body();
        return new Screened(true, HelpMessages.msg35(highest));
      }
    }
    return new Screened(false, reply);
  }

  static String norm(String s) {
    return Normalizer.normalize(s, Normalizer.Form.NFC)
        .toLowerCase(Locale.ROOT)
        .replaceAll("\\s+", "");
  }

  /** Dạng viết của đáp số (có thể nhiều đại lượng "Sxq = …; V = …"). */
  static void addAnswer(Set<String> terms, String answer) {
    if (answer == null || answer.isBlank()) {
      return;
    }
    for (String part : answer.split(";")) {
      String v = part.contains("=") ? part.substring(part.lastIndexOf('=') + 1) : part;
      v = v.strip();
      if (v.length() < 2) {
        continue;
      }
      terms.add(v);
      Matcher d = DEG.matcher(v);
      if (d.find()) {
        String n = d.group(1).replace(',', '.');
        terms.add(n + " độ");
        terms.addAll(SPECIAL_DEG.getOrDefault(n, List.of()));
        continue;
      }
      try {
        double x = new AnswerMatch.Quantity("", v, 0).valueAt(1);
        if (Double.isFinite(x) && Math.abs(x - Math.rint(x)) > 1e-9) {
          for (int dp = 2; dp <= 4; dp++) {
            String f = String.format(Locale.ROOT, "%." + dp + "f", x);
            terms.add(f);
            terms.add(f.replace('.', ','));
          }
        }
      } catch (RuntimeException e) {
        // biểu thức AnswerMatch không đọc được: chỉ so nguyên văn
      }
    }
  }

  /** Nguyên văn từng bước và các kết luận "vế trái = vế phải cuối" của bước. */
  void addSteps(Set<String> terms, String stepsJson) {
    if (stepsJson == null) {
      return;
    }
    JsonNode steps = mapper.readTree(stepsJson);
    for (JsonNode st : steps) {
      String body = st.path("body").asString("");
      if (body.isBlank()) {
        continue;
      }
      terms.add(body);
      for (String clause : body.split("[,;.]|suy ra|Vậy")) {
        String[] parts = clause.split("=");
        if (parts.length >= 2) {
          String lhs = parts[0].strip();
          String rhs = parts[parts.length - 1].strip();
          if (!lhs.isEmpty() && lhs.length() <= 24 && !rhs.isEmpty()) {
            terms.add(lhs + " = " + rhs);
          }
        }
      }
    }
  }
}
