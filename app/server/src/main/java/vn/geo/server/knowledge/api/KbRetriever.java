package vn.geo.server.knowledge.api;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Bộ truy xuất mục tài liệu của kho tri thức (US-S5E3-07, T-261128-56): tìm kiếm toàn văn tiếng
 * Việt bỏ dấu trên các tài liệu gắn cho lớp hoặc bài tại thời điểm hỏi, phiên bản Đang dùng; mục có
 * mã kiến thức trùng mã của đề được cộng điểm; tối đa 5 mục điểm ≥ ngưỡng {@code geo.kb.min-score}.
 * Danh sách rỗng = "không có mục" (BR-S5E3-10, MSG-S5-20), người gọi (hỏi đáp S5-E3, T-261128-58)
 * không gọi AI.
 */
public interface KbRetriever {

  /** Câu cho học sinh khi không có mục nào đạt ngưỡng (MSG-S5-20). */
  String NO_SECTION = "Thầy cô chưa cung cấp tài liệu cho phần này. Em hỏi thầy cô trên lớp nhé.";

  /**
   * Một mục tìm được: {@code score} = điểm {@code ts_rank_cd} chuẩn hoá (0…1) cộng điểm thưởng mã
   * kiến thức; {@code boosted} = mục có mã trùng mã của đề.
   */
  record Hit(
      UUID sectionId,
      UUID docId,
      String docCode,
      String docTitle,
      int version,
      int ord,
      String title,
      String body,
      List<String> codes,
      double score,
      boolean boosted) {}

  /**
   * Truy xuất cho một lượt hỏi.
   *
   * @param assignmentId bài đang làm (NULL = chỉ tài liệu gắn cả lớp)
   * @param knowledgeCodes mã kiến thức của đề đang làm (có thể rỗng)
   * @param at thời điểm hỏi (gắn kết hết hạn thì không dùng)
   */
  List<Hit> retrieve(
      UUID orgId,
      UUID classId,
      UUID assignmentId,
      String query,
      Collection<String> knowledgeCodes,
      Instant at);
}
