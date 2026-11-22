package vn.geo.server.content.internal.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.content.internal.repo.ContentRepository;

/**
 * Nạp kho đề và bộ đề chuẩn từ tệp nguồn {@code materials/Ngan-hang-bai-tap-Hinh-khong-gian.xlsx}
 * (US-GEO-106). Một đề, một bản ghi cho cả kho đề và bộ đề chuẩn (BR-GEO-118); nạp lại cùng nội
 * dung không tạo bản sao; bộ đề đổi nội dung thì tăng phiên bản (BR-GEO-121). Cột "Trạng thái kiểm
 * chứng" KHÔNG được nạp làm nhãn máy (BR-GEO-120, D-GEO-004): đề mới luôn "Cần xem lại" cho tới khi
 * engine chạy. Đề thiếu đáp số vẫn nạp, được liệt kê trong kết quả (US-GEO-106-FC1).
 */
@Service
public class CatalogImporter {

  public record Result(int setVersion, List<String> codes, List<String> problems) {}

  private static final Map<String, String> SOLID =
      Map.of(
          "Hình chóp", "pyramid",
          "Hình lập phương", "cube",
          "Lăng trụ đứng", "prism",
          "Hình nón", "cone",
          "Hình trụ", "cylinder",
          "Hình cầu", "sphere");

  private final ContentRepository repo;
  private final JsonMapper mapper;
  private final String model;
  private final String promptVersion;

  /**
   * @param model, promptVersion nguồn gốc lời giải ghi kèm (BR-GEO-113 "Lời giải có model, phiên
   *     bản prompt, tác giả"; US-GEO-104-SC5): model và phiên bản prompt của cổng AI đang cấu hình.
   */
  public CatalogImporter(
      ContentRepository repo,
      JsonMapper mapper,
      @Value("${geo.ai.model}") String model,
      @Value("${geo.ai.prompt-version}") String promptVersion) {
    this.repo = repo;
    this.mapper = mapper;
    this.model = model;
    this.promptVersion = promptVersion;
  }

  @Transactional
  public Result importXlsx(Path file) throws IOException {
    Map<String, List<Map<String, String>>> book = XlsxSheets.read(file);
    List<Map<String, String>> bt = book.getOrDefault("Bai tap", List.of());
    Map<String, List<Map<String, String>>> steps = new LinkedHashMap<>();
    for (Map<String, String> r : skipHeader(book.getOrDefault("Loi giai", List.of()))) {
      String code = r.getOrDefault("A", "");
      if (!code.isEmpty()) {
        steps
            .computeIfAbsent(code, k -> new ArrayList<>())
            .add(
                Map.of(
                    "title", r.getOrDefault("C", ""),
                    "body", r.getOrDefault("D", ""),
                    "why", r.getOrDefault("E", "")));
      }
    }
    UUID author = repo.platformAuthor();
    List<String> codes = new ArrayList<>();
    List<String> problems = new ArrayList<>();
    List<Object[]> items = new ArrayList<>();
    StringBuilder content = new StringBuilder();
    for (Map<String, String> r : skipHeader(bt)) {
      String code = r.getOrDefault("A", "");
      if (code.isEmpty()) {
        continue;
      }
      String statement = r.getOrDefault("E", "");
      String answer = r.getOrDefault("H", "");
      // cột J "Trạng thái kiểm chứng" cố ý KHÔNG đọc (BR-GEO-120)
      UUID[] ids = upsertProblem(r, code, statement, author);
      UUID problemId = ids[0];
      UUID versionId = ids[1];
      if (ids[2] != null) {
        repo.insertHint(versionId, 1, r.getOrDefault("F", ""));
        repo.insertHint(versionId, 2, r.getOrDefault("G", ""));
      }
      boolean missing = answer.isBlank();
      if (missing) {
        problems.add(code + ": thiếu đáp số tham chiếu");
      } else {
        List<AnswerExpr.Quantity> qs = AnswerExpr.parse(answer);
        repo.putSolution(
            versionId,
            answer,
            mapper.writeValueAsString(steps.getOrDefault(code, List.of())),
            mapper.writeValueAsString(AnswerExpr.forms(qs)),
            model,
            promptVersion);
      }
      codes.add(code);
      items.add(new Object[] {code, problemId, versionId, missing});
      content.append(code).append('\u0001').append(statement).append('\u0001').append(answer);
      content.append('\u0002');
    }
    byte[] hash = sha256(content.toString());
    var latest = repo.latestSet();
    int set;
    if (latest.isPresent() && MessageDigest.isEqual(latest.get().hash(), hash)) {
      set = latest.get().version();
    } else {
      set = latest.map(s -> s.version() + 1).orElse(1);
      repo.insertSet(set, hash, file.getFileName().toString());
      for (Object[] it : items) {
        repo.insertItem(set, (String) it[0], (UUID) it[1], (UUID) it[2], (Boolean) it[3]);
      }
    }
    return new Result(set, codes, problems);
  }

  /** [problemId, versionId, versionId nếu vừa tạo version mới]. */
  private UUID[] upsertProblem(Map<String, String> r, String code, String statement, UUID author) {
    var existing = repo.problemByCode(code);
    if (existing.isPresent()
        && normalize(existing.get().statement()).equals(normalize(statement))) {
      return new UUID[] {existing.get().problemId(), existing.get().versionId(), null};
    }
    UUID problemId;
    int version;
    if (existing.isPresent()) {
      problemId = existing.get().problemId();
      version = existing.get().version() + 1;
    } else {
      problemId = Uuid7.next();
      version = 1;
      String level = "THPT".equalsIgnoreCase(r.getOrDefault("B", "")) ? "thpt" : "thcs";
      String solid = SOLID.getOrDefault(r.getOrDefault("D", ""), "pyramid");
      Short topic = repo.topicId(r.getOrDefault("C", "")).orElse(null);
      repo.insertProblem(problemId, code, level, solid, topic, author);
    }
    UUID versionId = Uuid7.next();
    repo.insertVersion(
        versionId, problemId, version, statement, sha256(normalize(statement)), author);
    return new UUID[] {problemId, versionId, versionId};
  }

  private static List<Map<String, String>> skipHeader(List<Map<String, String>> rows) {
    return rows.isEmpty() ? rows : rows.subList(1, rows.size());
  }

  /** Chuẩn hóa đề trước khi băm: bỏ khoảng trắng đầu/cuối, gộp khoảng trắng (khớp seed). */
  static String normalize(String s) {
    return s == null ? "" : s.strip().replaceAll("\\s+", " ");
  }

  static byte[] sha256(String s) {
    try {
      return MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
