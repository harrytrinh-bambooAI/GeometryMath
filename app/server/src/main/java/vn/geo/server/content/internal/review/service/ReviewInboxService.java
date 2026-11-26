package vn.geo.server.content.internal.review.service;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.security.RequestContext;
import vn.geo.server.content.internal.review.repo.ReviewInboxRepository;
import vn.geo.server.content.internal.solution.repo.SolutionRepository.DocRow;
import vn.geo.server.content.internal.solution.service.SolutionReviewService;
import vn.geo.server.content.internal.solution.service.SolutionViews.DocView;

/**
 * S2-E5: hộp duyệt chung (US-S2E5-04, BR-S2E5-20). Mỗi đề xuất của AI: Nhận (thành phiên bản mới),
 * Sửa (giao diện mở soạn với nội dung đề xuất; lưu qua {@code POST …/versions} kèm {@code
 * proposalId}), Bỏ (loại, còn dấu vết trong lịch sử, không xóa). Không gì vào lời giải khi giáo
 * viên chưa quyết.
 */
@Service
public class ReviewInboxService {

  private final ReviewInboxRepository inbox;
  private final SolutionReviewService solutions;

  public ReviewInboxService(ReviewInboxRepository inbox, SolutionReviewService solutions) {
    this.inbox = inbox;
    this.solutions = solutions;
  }

  private ReviewInboxRepository.ItemRow pending(DocRow doc, UUID itemId) {
    ReviewInboxRepository.ItemRow item =
        inbox.item(doc.id(), itemId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    if (!"pending".equals(item.status())) {
      throw new AppException(ErrorCode.CONFLICT, "Đề xuất này đã được xử lý");
    }
    return item;
  }

  @Transactional
  public DocView accept(UUID docId, UUID itemId) {
    DocRow doc = solutions.lockOwned(docId);
    ReviewInboxRepository.ItemRow item = pending(doc, itemId);
    UUID me = RequestContext.principal().userId();
    int version = solutions.acceptProposal(doc, item.id(), me);
    inbox.decide(item.id(), "accepted", me, version);
    return solutions.viewAfterChange(docId, version);
  }

  @Transactional
  public DocView dismiss(UUID docId, UUID itemId) {
    DocRow doc = solutions.lockOwned(docId);
    ReviewInboxRepository.ItemRow item = pending(doc, itemId);
    inbox.decide(item.id(), "dismissed", RequestContext.principal().userId(), null);
    return solutions.viewAfterChange(docId, null);
  }
}
