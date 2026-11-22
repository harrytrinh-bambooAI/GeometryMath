package vn.geo.server.knowledge.api;

import java.util.Optional;
import java.util.UUID;

/**
 * Tra kho bài giảng mẫu (E4, US-GEO-409, 411, 412): module khác (hỏi đáp) chỉ được nêu nguồn là một
 * bài giảng CÓ trong kho (BR-GEO-189).
 */
public interface LectureCatalog {

  /** Bài giảng theo mã ngắn (slug). */
  Optional<LectureRef> bySlug(String slug);

  /** Bài giảng theo id (nguồn đã lưu của một lượt hỏi đáp). */
  Optional<LectureRef> byId(UUID id);

  /**
   * Bài giảng làm nguồn.
   *
   * @param sourceKind teacher_authored ("Giáo án mẫu") | ai_written ("AI soạn")
   * @param sourceLabel dòng nguồn hiển thị: "Giáo án mẫu" hoặc "AI soạn"
   */
  record LectureRef(UUID id, String slug, String title, String sourceKind, String sourceLabel) {}
}
