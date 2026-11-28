package vn.geo.server.assessment.internal.storage.service;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import vn.geo.server.aigateway.api.AiGateway;
import vn.geo.server.aigateway.api.AiRequest;
import vn.geo.server.aigateway.api.quota.AiQuota;
import vn.geo.server.assessment.internal.storage.repo.StoredImageRepository.Image;

/**
 * Dựng và gửi yêu cầu AI có ảnh bài làm (BR-S5E4-29, QS5-30): chỉ bản phái sinh đã gỡ EXIF qua
 * {@code ImageStore.forAi}, KHÔNG kèm tên, mã người dùng, mã lớp; mỗi trang một lượt, xin lượt qua
 * {@link AiQuota#admitGrading} (hạn mức lớp + tắt khẩn) trước khi gọi. T-261128-59 dùng đường này
 * cho task {@code grade_ocr} khi có luồng nộp thật.
 */
@Service
public class AiImageRequestService {

  /** Tiền tố prompt để giả lập nhận biết yêu cầu có ảnh của S5-E4. */
  public static final String MARK = "[s5e4-ocr]";

  private final ImageStoreService images;
  private final AiQuota quota;
  private final AiGateway ai;

  public AiImageRequestService(ImageStoreService images, AiQuota quota, AiGateway ai) {
    this.images = images;
    this.quota = quota;
    this.ai = ai;
  }

  /** Prompt cho một trang: chỉ số trang và ảnh base64. */
  public static String prompt(int pageNo, String contentType, byte[] image) {
    return MARK
        + " Đọc chữ viết tay của trang "
        + pageNo
        + " (bài làm hình học). Ảnh: data:"
        + contentType
        + ";base64,"
        + Base64.getEncoder().encodeToString(image);
  }

  /** Một lượt {@code grade_ocr} cho trang của bài nộp; trả trạng thái và câu trả lời của AI. */
  public Map<String, Object> ocrPage(UUID orgId, UUID submissionId, int pageNo) {
    Image img = images.image(orgId, submissionId, pageNo);
    Map<String, Object> out = new LinkedHashMap<>();
    AiQuota.Admission a = quota.admitGrading(orgId, img.classId(), "grade_ocr");
    if (!a.allowed()) {
      out.put("status", "ai_skipped");
      out.put("reason", a.reason());
      return out;
    }
    byte[] clean = images.forAi(orgId, submissionId, pageNo);
    String reply =
        ai.complete(new AiRequest("grade_ocr", prompt(pageNo, img.contentType(), clean))).content();
    out.put("status", "done");
    out.put("reply", reply);
    return out;
  }
}
