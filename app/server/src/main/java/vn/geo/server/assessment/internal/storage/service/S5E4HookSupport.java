package vn.geo.server.assessment.internal.storage.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.geo.server.assessment.api.ImageStore;
import vn.geo.server.assessment.internal.rubric.repo.RubricRepository;
import vn.geo.server.assessment.internal.storage.repo.StoredImageRepository;
import vn.geo.server.assessment.internal.storage.repo.StoredImageRepository.Image;
import vn.geo.server.common.id.Uuid7;

/**
 * MÓC KIỂM THỬ S5-E4 (chỉ dev/test, PO-51): giả lập học sinh nộp ảnh (luồng chụp nộp là của
 * T-261128-59), đọc đối tượng thô trong kho, cấp URL ký với mốc cấp lùi, chạy job xoá theo một
 * trường, gửi một lượt {@code grade_ocr} có ảnh, và câu UPDATE thẳng trên tiêu chí rubric bằng vai
 * ứng dụng.
 */
@Service
@Profile({"dev", "test"})
public class S5E4HookSupport {

  private final ImageStoreService images;
  private final StoredImageRepository repo;
  private final EncryptedObjectStore store;
  private final ImagePurgeService purge;
  private final AiImageRequestService ai;
  private final RubricRepository rubrics;
  private final Clock clock;

  public S5E4HookSupport(
      ImageStoreService images,
      StoredImageRepository repo,
      EncryptedObjectStore store,
      ImagePurgeService purge,
      AiImageRequestService ai,
      RubricRepository rubrics,
      Clock clock) {
    this.images = images;
    this.repo = repo;
    this.store = store;
    this.purge = purge;
    this.ai = ai;
    this.rubrics = rubrics;
    this.clock = clock;
  }

  /** Lưu {@code count} ảnh (mỗi ảnh một bài nộp mới, trừ khi truyền submissionId). */
  @Transactional
  public Map<String, Object> store(
      UUID orgId,
      UUID assignmentId,
      UUID studentId,
      UUID submissionId,
      Integer pageNo,
      String base64,
      Integer count,
      String schoolYear) {
    byte[] bytes = Base64.getDecoder().decode(base64);
    int n = count == null ? 1 : count;
    List<UUID> subs = new ArrayList<>();
    List<UUID> ids = new ArrayList<>();
    ImageStore.Stored last = null;
    for (int i = 0; i < n; i++) {
      UUID sub = submissionId != null ? submissionId : Uuid7.next();
      last =
          images.put(
              new ImageStore.PutRequest(
                  orgId, assignmentId, sub, pageNo == null ? 1 : pageNo, studentId, bytes, null),
              schoolYear);
      subs.add(sub);
      ids.add(last.id());
    }
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("submissionId", subs.get(0));
    out.put("submissionIds", subs);
    out.put("ids", ids);
    out.put("sha256", last.sha256());
    out.put("bytes", last.bytes());
    out.put("contentType", last.contentType());
    return out;
  }

  /** Đối tượng thô trong kho (đã mã hoá) và băm đã lưu. */
  public Map<String, Object> raw(UUID orgId, UUID submissionId, int pageNo) {
    Image img = images.image(orgId, submissionId, pageNo);
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("raw", Base64.getEncoder().encodeToString(images.raw(img)));
    out.put("sha256", img.sha256());
    out.put("schoolYear", img.schoolYear());
    return out;
  }

  /** URL ký cấp lùi {@code ageSeconds} giây (người gọi lấy từ ngữ cảnh). */
  public Map<String, Object> sign(UUID submissionId, int pageNo, long ageSeconds) {
    Instant issued = clock.instant().minusSeconds(ageSeconds);
    ImageStore.SignedUrl u = images.signedUrl(submissionId, pageNo, issued);
    return Map.of("url", u.url(), "expiresAt", u.expiresAt().toString());
  }

  public Map<String, Object> ocr(UUID orgId, UUID submissionId, int pageNo) {
    return ai.ocrPage(orgId, submissionId, pageNo);
  }

  public Map<String, Object> purgeAccounts(UUID assignmentId) {
    return purge.purgeDeletedAccounts(assignmentId);
  }

  public Map<String, Object> purgeYear(UUID assignmentId, LocalDate today) {
    return purge.purgeEndedYears(today, assignmentId);
  }

  /** Số ảnh còn lại, số đối tượng còn trong kho (theo id), AuditEvent của trường. */
  public Map<String, Object> state(
      UUID orgId, UUID assignmentId, UUID submissionId, String schoolYear, List<UUID> ids) {
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("images", repo.count(orgId, schoolYear, submissionId, assignmentId));
    int objects = 0;
    for (UUID id : ids) {
      if (store.exists(orgId + "/" + id)) {
        objects++;
      }
    }
    out.put("objects", objects);
    out.put("audits", repo.audits(orgId));
    return out;
  }

  /** TC-S5E4-06: UPDATE thẳng tiêu chí của một phiên bản; trả lỗi CSDL (nếu có) và dữ liệu sau. */
  public Map<String, Object> rawRubricUpdate(UUID orgId, UUID versionId, BigDecimal points) {
    Map<String, Object> out = new LinkedHashMap<>();
    try {
      out.put("updated", rubrics.rawUpdateCriteria(orgId, versionId, points));
      out.put("error", null);
    } catch (DataAccessException e) {
      out.put("updated", 0);
      Throwable root = e.getMostSpecificCause();
      out.put("error", root == null ? e.getMessage() : root.getMessage());
    }
    List<Map<String, Object>> after = new ArrayList<>();
    for (RubricRepository.Criterion c : rubrics.criteria(orgId, versionId)) {
      after.add(Map.of("name", c.name(), "maxPoints", c.maxPoints().doubleValue()));
    }
    out.put("criteria", after);
    return out;
  }
}
