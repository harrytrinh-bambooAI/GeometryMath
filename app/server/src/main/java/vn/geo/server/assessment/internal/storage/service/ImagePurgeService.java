package vn.geo.server.assessment.internal.storage.service;

import java.time.LocalDate;
import java.time.Month;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.geo.server.assessment.api.SubmissionDataEraser;
import vn.geo.server.assessment.internal.storage.repo.StoredImageRepository;
import vn.geo.server.assessment.internal.storage.repo.StoredImageRepository.Image;

/**
 * Job xoá ảnh bài làm (BR-S5E4-28; QS5-01, QS5-14): (a) học sinh đã xoá tài khoản → xoá ảnh gốc,
 * phái sinh và gọi mọi {@link SubmissionDataEraser} (bản đọc chữ, đề xuất AI, đánh giá tiêu chí;
 * điểm duyệt giữ ẩn danh); (b) từ 31/07 hằng năm xoá mọi ảnh của năm học vừa kết thúc. Mỗi lần xoá
 * ghi AuditEvent số lượng (assessment_audit), không ghi nội dung.
 */
@Service
public class ImagePurgeService {

  private static final Logger log = LoggerFactory.getLogger(ImagePurgeService.class);
  private static final int BATCH = 5000;

  private final StoredImageRepository repo;
  private final EncryptedObjectStore store;
  private final List<SubmissionDataEraser> erasers;

  public ImagePurgeService(
      StoredImageRepository repo, EncryptedObjectStore store, List<SubmissionDataEraser> erasers) {
    this.repo = repo;
    this.store = store;
    this.erasers = erasers;
  }

  /**
   * (a) theo tài khoản; {@code scope} khác null chỉ xét ảnh của một bài có chấm (móc kiểm thử: E2E
   * chạy song song chung trường).
   */
  @Transactional
  public Map<String, Object> purgeDeletedAccounts(UUID scope) {
    List<Image> imgs = repo.ofDeletedStudents(scope, BATCH);
    Map<String, Integer> perStudent = new LinkedHashMap<>();
    for (Image i : imgs) {
      store.delete(i.objectKey());
      repo.delete(i.orgId(), i.id());
      perStudent.merge(i.orgId() + "/" + i.studentId(), 1, Integer::sum);
    }
    int erased = 0;
    for (Map.Entry<String, Integer> e : perStudent.entrySet()) {
      String[] k = e.getKey().split("/");
      UUID org = UUID.fromString(k[0]);
      UUID student = UUID.fromString(k[1]);
      int rows = 0;
      for (SubmissionDataEraser x : erasers) {
        rows += x.eraseStudent(org, student);
      }
      erased += rows;
      repo.audit(org, "image_purge_account", student, "images", e.getValue());
    }
    if (!imgs.isEmpty()) {
      log.info("Xoá ảnh theo tài khoản: {} ảnh, {} học sinh", imgs.size(), perStudent.size());
    }
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("images", imgs.size());
    out.put("students", perStudent.size());
    out.put("rows", erased);
    return out;
  }

  /** (b) theo năm học tại ngày {@code today} (giờ Việt Nam); {@code scope} như (a). */
  @Transactional
  public Map<String, Object> purgeEndedYears(LocalDate today, UUID scope) {
    int endYear =
        today.isBefore(LocalDate.of(today.getYear(), Month.JULY, 31))
            ? today.getYear() - 1
            : today.getYear();
    List<Image> imgs = repo.ofEndedYears(endYear, scope, BATCH);
    Map<UUID, Integer> perOrg = new LinkedHashMap<>();
    Set<String> years = new LinkedHashSet<>();
    for (Image i : imgs) {
      store.delete(i.objectKey());
      repo.delete(i.orgId(), i.id());
      perOrg.merge(i.orgId(), 1, Integer::sum);
      years.add(i.schoolYear());
    }
    int rows = 0;
    for (String y : years) {
      for (SubmissionDataEraser x : erasers) {
        rows += x.eraseSchoolYear(y);
      }
    }
    perOrg.forEach(
        (org, n) -> repo.audit(org, "image_purge_year", null, String.join(",", years), n));
    if (!imgs.isEmpty()) {
      log.info("Xoá ảnh theo năm học {}: {} ảnh", years, imgs.size());
    }
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("images", imgs.size());
    out.put("years", List.copyOf(years));
    out.put("rows", rows);
    return out;
  }
}
