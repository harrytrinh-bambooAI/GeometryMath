package vn.geo.server.content.internal.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Gói nội dung Kho đề MVP (T-261126-02, PO-60): {@code content/catalog-mvp.json}. Mỗi đề có mã,
 * cấp, loại khối, dạng bài, đề, hai gợi ý, đáp số và lời giải từng bước; mô tả dựng nằm ở {@code
 * geometry/problem-specs.json} cùng mã (engine đọc). Tệp KHÔNG mang nhãn kiểm chứng: nhãn do engine
 * tính khi nạp (BR-GEO-213, D-GEO-004).
 */
public record ContentPack(int version, String bank, List<Problem> problems) {

  public static final String RESOURCE = "/content/catalog-mvp.json";

  /** Một bước lời giải (cùng dạng cột solution.steps). */
  public record Step(String title, String body, String why) {}

  /** Một đề của gói. */
  public record Problem(
      String code,
      String level,
      String solid,
      String topic,
      String statement,
      List<String> hints,
      String answer,
      List<Step> steps) {

    /** Lời giải dạng JSON của cột solution.steps (khóa title, body, why theo thứ tự). */
    public String stepsJson(JsonMapper mapper) {
      List<Map<String, String>> out = new ArrayList<>();
      for (Step s : steps) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("title", s.title());
        m.put("body", s.body());
        m.put("why", s.why());
        out.add(m);
      }
      return mapper.writeValueAsString(out);
    }
  }

  /** Đọc gói từ classpath. */
  public static ContentPack read(JsonMapper mapper) {
    try (InputStream in = ContentPack.class.getResourceAsStream(RESOURCE)) {
      if (in == null) {
        throw new IllegalStateException("không có " + RESOURCE);
      }
      return parse(mapper.readTree(in));
    } catch (IOException e) {
      throw new IllegalStateException("không đọc được " + RESOURCE, e);
    }
  }

  static ContentPack parse(JsonNode root) {
    List<Problem> list = new ArrayList<>();
    for (JsonNode p : root.path("problems")) {
      List<String> hints = new ArrayList<>();
      p.path("hints").forEach(h -> hints.add(h.asString()));
      List<Step> steps = new ArrayList<>();
      for (JsonNode s : p.path("steps")) {
        steps.add(
            new Step(
                s.path("title").asString(""),
                s.path("body").asString(""),
                s.path("why").asString("")));
      }
      list.add(
          new Problem(
              p.path("code").asString(),
              p.path("level").asString(),
              p.path("solid").asString(),
              p.path("topic").asString(),
              p.path("statement").asString(),
              List.copyOf(hints),
              p.path("answer").asString(""),
              List.copyOf(steps)));
    }
    return new ContentPack(
        root.path("version").asInt(1), root.path("bank").asString("main"), List.copyOf(list));
  }
}
