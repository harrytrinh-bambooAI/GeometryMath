package vn.geo.server.assessment.internal.storage.web;

import java.util.Map;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.assessment.api.ImageStore;
import vn.geo.server.common.web.NoStore;

/**
 * Cấp URL ký 5 phút cho ảnh một trang bài nộp (US-S5E4-08; BR-S5E4-26): giáo viên chủ lớp hoặc
 * chính học sinh nộp; ngoài quan hệ NOT_FOUND, không có URL trong thân (US-S5E4-08-FC1).
 */
@RestController
@ConditionalOnProperty(name = "geo.s5.grading.enabled", havingValue = "true")
public class SubmissionImageController {

  private final ImageStore images;

  public SubmissionImageController(ImageStore images) {
    this.images = images;
  }

  @NoStore
  @GetMapping("/api/v1/submissions/{submissionId}/pages/{pageNo}/url")
  public Map<String, Object> url(@PathVariable UUID submissionId, @PathVariable int pageNo) {
    ImageStore.SignedUrl u = images.signedUrl(submissionId, pageNo);
    return Map.of("url", u.url(), "expiresAt", u.expiresAt().toString());
  }
}
