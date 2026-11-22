package vn.geo.server.knowledge.internal.web;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.knowledge.internal.service.LectureService;

/**
 * Kho bài giảng mẫu (E4, US-GEO-411, 412): từ khóa bấm được và hộp bài giảng. Mọi vai đã đăng nhập.
 */
@RestController
public class LectureController {

  private final LectureService service;

  public LectureController(LectureService service) {
    this.service = service;
  }

  /**
   * Danh sách từ khóa (thiết bị gạch chân chấm cụm từ trùng trong gợi ý, lời giải, câu trả lời).
   */
  @GetMapping("/api/v1/lectures/keywords")
  public Map<String, List<LectureService.Keyword>> keywords() {
    return Map.of("items", service.keywords());
  }

  /** Bấm một cụm từ: bài giảng của từ khóa đó, hoặc NOT_FOUND (MSG-GEO-39). */
  @GetMapping("/api/v1/lectures/by-term")
  public LectureService.LectureView byTerm(@RequestParam String term) {
    return service.byTerm(term);
  }

  /** Bài giảng theo mã (bấm tên bài giảng ở dòng "Nguồn" của câu trả lời). */
  @GetMapping("/api/v1/lectures/{slug}")
  public LectureService.LectureView bySlug(@PathVariable String slug) {
    return service.bySlugView(slug);
  }
}
