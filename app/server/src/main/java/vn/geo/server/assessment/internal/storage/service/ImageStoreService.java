package vn.geo.server.assessment.internal.storage.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.geo.server.assessment.api.ImageStore;
import vn.geo.server.assessment.internal.storage.repo.StoredImageRepository;
import vn.geo.server.assessment.internal.storage.repo.StoredImageRepository.AssignmentRef;
import vn.geo.server.assessment.internal.storage.repo.StoredImageRepository.Image;
import vn.geo.server.common.config.TimeProperties;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.RequestContext;
import vn.geo.server.common.security.Role;

/**
 * Cài đặt {@link ImageStore} (US-S5E4-08; BR-S5E4-25…27, 29): gỡ metadata → băm → mã hoá → ghi kho;
 * URL ký 5 phút chỉ cấp cho giáo viên chủ lớp hoặc chính học sinh nộp (kiểm lại mỗi lần cấp).
 */
@Service
public class ImageStoreService implements ImageStore {

  /** Tiền tố đường tải ảnh bằng URL ký (bộ lọc riêng, không cần thẻ). */
  public static final String FILE_PREFIX = "/api/v1/files/images/";

  private final StoredImageRepository repo;
  private final EncryptedObjectStore store;
  private final ImageProperties props;
  private final Clock clock;

  public ImageStoreService(
      StoredImageRepository repo, EncryptedObjectStore store, ImageProperties props, Clock clock) {
    this.repo = repo;
    this.store = store;
    this.props = props;
    this.clock = clock;
  }

  @Override
  @Transactional
  public Stored put(PutRequest r) {
    return put(r, null);
  }

  /**
   * Như {@link #put(PutRequest)}; {@code schoolYear} khác null chỉ dùng ở móc kiểm thử
   * (TC-S5E4-44).
   */
  @Transactional
  public Stored put(PutRequest r, String schoolYear) {
    if (r.pageNo() < 1 || r.pageNo() > 20) {
      throw AppException.invalidField("pageNo", "Trang từ 1 đến 20.");
    }
    AssignmentRef a =
        repo.assignment(r.orgId(), r.assignmentId())
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    MetadataStripper.Clean clean = MetadataStripper.strip(r.bytes());
    String sha = sha256(clean.bytes());
    repo.page(r.orgId(), r.submissionId(), r.pageNo())
        .ifPresent(old -> {
          store.delete(old.objectKey());
          repo.delete(r.orgId(), old.id());
        });
    UUID id = Uuid7.next();
    String key = r.orgId() + "/" + id;
    store.write(key, clean.bytes());
    repo.upsert(
        r.orgId(),
        id,
        a.id(),
        r.submissionId(),
        r.pageNo(),
        r.studentId(),
        a.classId(),
        schoolYear == null ? schoolYear(clock.instant()) : schoolYear,
        clean.contentType(),
        sha,
        clean.bytes().length,
        key);
    return new Stored(id, sha, clean.bytes().length, clean.contentType());
  }

  @Override
  public SignedUrl signedUrl(UUID submissionId, int pageNo) {
    return signedUrl(submissionId, pageNo, clock.instant());
  }

  /** URL ký cấp tại {@code issuedAt} (móc kiểm thử TC-S5E4-41 lùi mốc cấp). */
  public SignedUrl signedUrl(UUID submissionId, int pageNo, Instant issuedAt) {
    AuthPrincipal me = RequestContext.principal();
    Image img =
        repo.page(me.orgId(), submissionId, pageNo)
            .filter(i -> related(me, i))
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    long exp = issuedAt.plus(props.urlTtl()).getEpochSecond();
    String sig = store.sign(payload(img.orgId(), img.id(), exp));
    return new SignedUrl(
        FILE_PREFIX + img.id() + "?o=" + img.orgId() + "&exp=" + exp + "&sig=" + sig,
        Instant.ofEpochSecond(exp));
  }

  /**
   * Ảnh (đã gỡ metadata, đã giải mã) theo URL ký; chữ ký sai hoặc hết hạn: rỗng (bộ lọc trả 403).
   */
  Optional<Image> verify(UUID orgId, UUID imageId, long exp, String sig) {
    if (sig == null || exp <= clock.instant().getEpochSecond()) {
      return Optional.empty();
    }
    String want = store.sign(payload(orgId, imageId, exp));
    if (!MessageDigest.isEqual(
        want.getBytes(java.nio.charset.StandardCharsets.US_ASCII),
        sig.getBytes(java.nio.charset.StandardCharsets.US_ASCII))) {
      return Optional.empty();
    }
    return repo.byId(orgId, imageId);
  }

  /** Nội dung ảnh để trả qua URL ký. */
  public record Blob(byte[] bytes, String contentType) {}

  /** Mở ảnh theo URL ký; chữ ký sai hoặc hết hạn: rỗng. */
  public Optional<Blob> open(UUID orgId, UUID imageId, long exp, String sig) {
    return verify(orgId, imageId, exp, sig)
        .map(i -> new Blob(store.read(i.objectKey()), i.contentType()));
  }

  @Override
  public byte[] forAi(UUID orgId, UUID submissionId, int pageNo) {
    Image img =
        repo.page(orgId, submissionId, pageNo)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    // ảnh trong kho đã gỡ EXIF/XMP khi nhận; gỡ lại lần nữa để chắc bản gửi AI sạch (BR-S5E4-29)
    return MetadataStripper.strip(store.read(img.objectKey())).bytes();
  }

  @Override
  @Transactional
  public int deleteSubmission(UUID orgId, UUID submissionId) {
    int n = 0;
    for (Image img : repo.ofSubmission(orgId, submissionId)) {
      store.delete(img.objectKey());
      repo.delete(orgId, img.id());
      n++;
    }
    return n;
  }

  /** Lớp và năm học của một ảnh (dùng ở móc kiểm thử, việc AI). */
  public Image image(UUID orgId, UUID submissionId, int pageNo) {
    return repo.page(orgId, submissionId, pageNo)
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
  }

  /** Bản thô trong kho (đã mã hoá) — móc kiểm thử TC-S5E4-40. */
  public byte[] raw(Image img) {
    return store.raw(img.objectKey());
  }

  private boolean related(AuthPrincipal me, Image i) {
    if (me.role() == Role.STUDENT) {
      return i.studentId().equals(me.userId());
    }
    if (me.role() == Role.TEACHER) {
      return repo.classTeacher(me.orgId(), i.classId()).map(me.userId()::equals).orElse(false);
    }
    return false;
  }

  private static String payload(UUID orgId, UUID imageId, long exp) {
    return orgId + "|" + imageId + "|" + exp;
  }

  /** Năm học theo giờ Việt Nam: từ 01/08 năm y là năm học y-(y+1). */
  public static String schoolYear(Instant at) {
    LocalDate d = LocalDate.ofInstant(at, TimeProperties.VIETNAM);
    int start = d.getMonthValue() >= 8 ? d.getYear() : d.getYear() - 1;
    return start + "-" + (start + 1);
  }

  static String sha256(byte[] b) {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(b));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
