package vn.geo.server.exam.internal.domain;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Dữ liệu đề đọc từ thư mục {@code materials/de-thi} (chỉ đọc): 4 schema, ma trận, mẫu câu, ngân
 * hàng câu, giá trị engine đã đo ({@code mau-cau/engine-values.json}), danh mục chủ đề ({@code
 * lich-su/taxonomy.json}). Các danh sách giữ thứ tự tên tệp (như {@code check.py}), có thể sửa
 * trước khi kiểm (móc kiểm thử TC-S5E2-03, 04).
 */
public final class ExamDataset {

  public final Map<String, JsonNode> schemas = new LinkedHashMap<>();
  public final List<ObjectNode> matrices = new ArrayList<>();
  public final List<ObjectNode> templates = new ArrayList<>();
  public final List<ObjectNode> items = new ArrayList<>();

  /** Khóa: id câu (hoặc id#nhãn ý cho câu đúng/sai) → giá trị engine đã đo. */
  public final Map<String, Double> engineValues = new LinkedHashMap<>();

  /** Chủ đề: id → {ten, lop}. */
  public final Map<String, Topic> topics = new LinkedHashMap<>();

  public record Topic(String id, String name, Integer grade) {}

  public static ExamDataset load(Path dir, JsonMapper mapper) {
    ExamDataset d = new ExamDataset();
    for (String n : List.of("cau", "mau_cau", "ma_tran", "de")) {
      d.schemas.put(n, read(mapper, dir.resolve("schema").resolve(n + ".schema.json")));
    }
    for (Path f : list(dir.resolve("ma-tran"))) {
      if (!f.getFileName().toString().equals("yccd.json")) {
        d.matrices.add((ObjectNode) read(mapper, f));
      }
    }
    for (Path f : list(dir.resolve("mau-cau"))) {
      if (f.getFileName().toString().startsWith("MC-")) {
        d.templates.add((ObjectNode) read(mapper, f));
      }
    }
    for (Path f : list(dir.resolve("ngan-hang"))) {
      for (JsonNode it : read(mapper, f).path("cau")) {
        d.items.add((ObjectNode) it);
      }
    }
    Path ev = dir.resolve("mau-cau").resolve("engine-values.json");
    if (Files.exists(ev)) {
      for (Map.Entry<String, JsonNode> e : read(mapper, ev).path("gia_tri").properties()) {
        JsonNode v = e.getValue().path("engine");
        if (v.isNumber()) {
          d.engineValues.put(e.getKey(), v.doubleValue());
        }
      }
    }
    Path tax = dir.resolve("lich-su").resolve("taxonomy.json");
    if (Files.exists(tax)) {
      for (JsonNode c : read(mapper, tax).path("chu_de")) {
        JsonNode lop = c.path("lop_ct2018");
        d.topics.put(
            c.path("id").asString(),
            new Topic(
                c.path("id").asString(),
                c.path("ten").asString(),
                lop.isNumber() ? lop.asInt() : null));
      }
    }
    return d;
  }

  private static List<Path> list(Path dir) {
    try (Stream<Path> s = Files.list(dir)) {
      return s.filter(p -> p.getFileName().toString().endsWith(".json")).sorted().toList();
    } catch (IOException e) {
      throw new UncheckedIOException("không đọc được thư mục dữ liệu đề " + dir, e);
    }
  }

  private static JsonNode read(JsonMapper mapper, Path f) {
    try {
      return mapper.readTree(Files.readString(f));
    } catch (IOException e) {
      throw new UncheckedIOException("không đọc được " + f, e);
    }
  }
}
