package vn.geo.server.knowledge.internal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.knowledge.internal.repo.LectureRepository;

/**
 * Nạp kho bài giảng mẫu (US-GEO-412) từ JSON dạng {@code knowledge/lectures.json}. Mỗi bài cần khái
 * niệm, nội dung, nguồn và ít nhất một từ khóa (BR-GEO-200); bài nguồn "Giáo án mẫu" phải ghi tên
 * tài liệu và mục (D-GEO-007 "ghi rõ nguồn"): thiếu thì bài đó bị từ chối, kết quả nêu tên bài, các
 * bài khác nạp bình thường (US-GEO-412-FC2). Bài nạp mới luôn "chưa có người soát" (D-GEO-137).
 */
@Service
public class LectureImporter {

  /** Kết quả nạp: bài đã nạp (slug) và bài bị từ chối (tên + lý do). */
  public record Result(List<String> loaded, List<String> rejected) {}

  private final LectureRepository repo;

  public LectureImporter(LectureRepository repo) {
    this.repo = repo;
  }

  public Result load(JsonNode doc) {
    List<String> loaded = new ArrayList<>();
    List<String> rejected = new ArrayList<>();
    for (JsonNode l : doc.path("lectures")) {
      String title = l.path("title").asString("").strip();
      String slug = l.path("slug").asString("").strip();
      String body = l.path("body").asString("").strip();
      String source = l.path("source").asString("");
      String ref = l.path("sourceRef").isString() ? l.path("sourceRef").asString().strip() : "";
      List<String> keywords = new ArrayList<>();
      l.path("keywords").forEach(k -> keywords.add(k.asString().strip()));
      keywords.removeIf(String::isEmpty);
      String name = title.isEmpty() ? slug : title;
      String why = null;
      if (title.isEmpty() || body.isEmpty() || !slug.matches("[a-z0-9-]{1,80}")) {
        why = "thiếu khái niệm, nội dung hoặc mã";
      } else if (!"teacher_authored".equals(source) && !"ai_written".equals(source)) {
        why = "nguồn không hợp lệ";
      } else if ("teacher_authored".equals(source) && ref.isEmpty()) {
        why = "bài nguồn \"Giáo án mẫu\" thiếu tên tài liệu và mục";
      } else if (keywords.isEmpty()) {
        why = "thiếu từ khóa";
      }
      if (why != null) {
        rejected.add(name + ": " + why);
        continue;
      }
      UUID id = Uuid7.next();
      if (repo.insert(id, slug, title, body, source, ref.isEmpty() ? null : ref, keywords)) {
        loaded.add(slug);
      }
    }
    return new Result(List.copyOf(loaded), List.copyOf(rejected));
  }
}
