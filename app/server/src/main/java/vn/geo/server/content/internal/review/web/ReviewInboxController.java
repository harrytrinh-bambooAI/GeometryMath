package vn.geo.server.content.internal.review.web;

import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.web.NoStore;
import vn.geo.server.content.internal.review.service.ReviewInboxService;
import vn.geo.server.content.internal.solution.service.SolutionViews.DocView;

/** S2-E5: hộp duyệt — nhận / bỏ đề xuất của AI (US-S2E5-04-SC4, SC6). No-store. */
@RestController
@NoStore
public class ReviewInboxController {

  private final ReviewInboxService inbox;

  public ReviewInboxController(ReviewInboxService inbox) {
    this.inbox = inbox;
  }

  @PostMapping("/api/v1/teacher/solutions/{id}/proposals/{itemId}/accept")
  public DocView accept(@PathVariable UUID id, @PathVariable UUID itemId) {
    return inbox.accept(id, itemId);
  }

  @PostMapping("/api/v1/teacher/solutions/{id}/proposals/{itemId}/dismiss")
  public DocView dismiss(@PathVariable UUID id, @PathVariable UUID itemId) {
    return inbox.dismiss(id, itemId);
  }
}
