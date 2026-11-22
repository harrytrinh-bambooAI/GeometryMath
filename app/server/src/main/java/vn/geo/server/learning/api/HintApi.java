package vn.geo.server.learning.api;

import java.util.UUID;

/**
 * Xin gợi ý theo mức (E4, US-GEO-404, 406): điểm cuối {@code POST .../hints/{level}} của trang Làm
 * bài (module tracking) ủy quyền cho giao diện này.
 */
public interface HintApi {

  HintResult hint(UUID assignmentId, int level, UUID requestId);

  /**
   * @param title "Gợi ý 1 · Hướng nghĩ" | "Gợi ý 2 · Bước kế tiếp"
   * @param k tiến độ k/3 sau lần xin này (M-GEO-14)
   */
  record HintResult(int level, String title, String body, int k) {}
}
