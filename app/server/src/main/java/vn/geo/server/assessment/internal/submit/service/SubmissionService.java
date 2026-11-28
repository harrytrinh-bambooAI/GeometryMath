package vn.geo.server.assessment.internal.submit.service;

import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import vn.geo.server.assessment.api.ImageStore;
import vn.geo.server.assessment.internal.ocr.service.OcrService;
import vn.geo.server.assessment.internal.storage.service.EncryptedObjectStore;
import vn.geo.server.assessment.internal.storage.service.HeicStripper;
import vn.geo.server.assessment.internal.storage.service.ImageStoreService;
import vn.geo.server.assessment.internal.storage.service.MetadataStripper;
import vn.geo.server.assessment.internal.submit.repo.SubmissionRepository;
import vn.geo.server.assessment.internal.submit.repo.SubmissionRepository.Assignment;
import vn.geo.server.assessment.internal.submit.repo.SubmissionRepository.Page;
import vn.geo.server.assessment.internal.submit.repo.SubmissionRepository.Sub;
import vn.geo.server.assessment.internal.submit.repo.SubmissionRepository.Upload;
import vn.geo.server.assessment.internal.submit.repo.SubmissionRepository.Version;
import vn.geo.server.common.config.TimeProperties;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.RequestContext;
import vn.geo.server.common.security.Role;

/**
 * Nộp bài viết tay (US-S5E4-03, 09): thông báo riêng tư S5 trước lần nộp đầu (BR-S5E4-11); tải từng
 * trang theo đoạn (≤ 8 MB/trang, 1–10 trang; BR-S5E4-07); nộp kèm khóa lũy đẳng sinh trên máy (gửi
 * lại cùng khóa trả 200 với bài đã có; BR-S5E4-09); nộp lại trước hạn tạo phiên bản mới, bản cũ và
 * ảnh giữ nguyên; sau hạn: cho nộp muộn thì gắn nhãn theo giờ máy chủ, không thì CONFLICT MSG-S5-34
 * (BR-S5E4-10). Ảnh gốc qua {@link ImageStore} (gỡ EXIF, mã hoá, băm); HEIC lưu đúng tệp gốc kèm
 * bản phái sinh JPEG do máy (Safari) dựng (BR-S5E4-08). Sau khi ghi, xếp việc đọc chữ nền.
 */
@Service
public class SubmissionService {

  private static final DateTimeFormatter VN_TIME =
      DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy").withZone(TimeProperties.VIETNAM);

  private final SubmissionRepository repo;
  private final ImageStoreService images;
  private final EncryptedObjectStore store;
  private final OcrService ocr;
  private final TransactionTemplate tx;
  private final Clock clock;

  public SubmissionService(
      SubmissionRepository repo,
      ImageStoreService images,
      EncryptedObjectStore store,
      OcrService ocr,
      TransactionTemplate tx,
      Clock clock) {
    this.repo = repo;
    this.images = images;
    this.store = store;
    this.ocr = ocr;
    this.tx = tx;
    this.clock = clock;
  }

  private static AuthPrincipal student() {
    AuthPrincipal me = RequestContext.principal();
    if (me.role() != Role.STUDENT) {
      throw new AppException(ErrorCode.FORBIDDEN);
    }
    return me;
  }

  // ------------------------------------------------------------------ thông báo riêng tư

  public Map<String, Object> privacy() {
    AuthPrincipal me = student();
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("version", SubmitRules.PRIVACY_VERSION);
    m.put("text", SubmitRules.MSG_36);
    m.put("acked", repo.acked(me.orgId(), me.userId(), SubmitRules.PRIVACY_VERSION));
    return m;
  }

  public record AckRequest(String version) {}

  public Map<String, Object> ack(AckRequest req) {
    AuthPrincipal me = student();
    if (req == null || !SubmitRules.PRIVACY_VERSION.equals(req.version())) {
      throw AppException.invalidField("version", "Phiên bản thông báo không đúng. Tải lại trang.");
    }
    repo.ack(me.orgId(), me.userId(), SubmitRules.PRIVACY_VERSION);
    return privacy();
  }

  // ------------------------------------------------------------------ tải theo đoạn

  public record UploadRequest(String contentType, Long sizeBytes) {}

  public Map<String, Object> startUpload(UploadRequest req) {
    AuthPrincipal me = student();
    String ct = req == null || req.contentType() == null ? "" : req.contentType().strip();
    if (!List.of("image/jpeg", "image/png", "image/heic").contains(ct)) {
      throw AppException.invalidField("contentType", "Ảnh chỉ nhận JPEG, PNG hoặc HEIC.");
    }
    long size = req.sizeBytes() == null ? 0 : req.sizeBytes();
    if (size < 1 || size > SubmitRules.MAX_PAGE_BYTES) {
      throw AppException.invalidField("sizeBytes", "Mỗi trang tối đa 8 MB.");
    }
    UUID id = Uuid7.next();
    repo.insertUpload(me.orgId(), id, me.userId(), ct, (int) size);
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("uploadId", id.toString());
    m.put("partBytes", SubmitRules.PART_BYTES);
    m.put("sizeBytes", size);
    return m;
  }

  public Map<String, Object> uploadPart(UUID uploadId, int offset, byte[] chunk) {
    AuthPrincipal me = student();
    Upload u =
        repo.upload(me.orgId(), me.userId(), uploadId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    if (chunk == null || chunk.length == 0 || offset < 0) {
      throw AppException.invalidField("offset", "Đoạn tải không hợp lệ.");
    }
    if ((long) offset + chunk.length > u.sizeBytes()) {
      throw AppException.invalidField("sizeBytes", "Mỗi trang tối đa 8 MB.");
    }
    if (repo.appendUpload(me.orgId(), uploadId, offset, chunk) != 1) {
      throw new AppException(ErrorCode.CONFLICT, "Đoạn tải lệch vị trí. Tải lại trang này.");
    }
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("uploadId", uploadId.toString());
    m.put("received", offset + chunk.length);
    m.put("sizeBytes", u.sizeBytes());
    return m;
  }

  // ------------------------------------------------------------------ nộp

  public record PageInput(String uploadId, String derivedUploadId, Double clarity) {}

  public record SubmitRequest(String assignmentId, String idempotencyKey, List<PageInput> pages) {}

  /** Kết quả nộp: {@code created} = bài/phiên bản mới (201), false = gửi lại cùng khóa (200). */
  public record Result(boolean created, Map<String, Object> body) {}

  private record Bytes(byte[] original, String contentType, byte[] derived) {}

  public Result submit(SubmitRequest req) {
    AuthPrincipal me = student();
    if (req == null) {
      throw AppException.invalidField("pages", "Cần ít nhất một trang.");
    }
    String key = req.idempotencyKey() == null ? "" : req.idempotencyKey().strip();
    if (key.isEmpty() || key.length() > 80) {
      throw AppException.invalidField("idempotencyKey", "Thiếu khóa gửi bài.");
    }
    Optional<Version> replay = repo.byKey(me.orgId(), me.userId(), key);
    if (replay.isPresent()) {
      return new Result(false, view(me, replay.get().submissionId()));
    }
    UUID assignmentId = uuid(req.assignmentId(), "assignmentId");
    Assignment a =
        repo.assignment(me.orgId(), assignmentId)
            .filter(x -> repo.activeMember(me.orgId(), x.classId(), me.userId()))
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    if (!repo.acked(me.orgId(), me.userId(), SubmitRules.PRIVACY_VERSION)) {
      throw AppException.invalidField("privacy", SubmitRules.MSG_PRIVACY);
    }
    List<PageInput> pages = req.pages() == null ? List.of() : req.pages();
    if (pages.isEmpty() || pages.size() > SubmitRules.MAX_PAGES) {
      throw AppException.invalidField("pages", "Mỗi bài nộp từ 1 đến 10 trang.");
    }
    for (int i = 0; i < pages.size(); i++) {
      if (!SubmitRules.clear(pages.get(i).clarity())) {
        throw AppException.invalidField("pages[" + i + "].clarity", SubmitRules.blurry(i + 1));
      }
    }
    if ("returned".equals(a.status())) {
      throw new AppException(ErrorCode.CONFLICT, SubmitRules.MSG_RETURNED);
    }
    Instant now = clock.instant();
    Integer late = SubmitRules.lateSeconds(a.dueAt(), now);
    if (late != null && !a.allowLate()) {
      throw new AppException(ErrorCode.CONFLICT, SubmitRules.MSG_34);
    }
    List<Bytes> files = new ArrayList<>();
    for (int i = 0; i < pages.size(); i++) {
      files.add(bytesOf(me, pages.get(i), i));
    }
    String year = ImageStoreService.schoolYear(now);
    UUID[] made = new UUID[2]; // submissionId, versionId
    Boolean created =
        tx.execute(st -> {
          repo.lockAssignment(me.orgId(), a.id());
          Optional<Version> again = repo.byKey(me.orgId(), me.userId(), key);
          if (again.isPresent()) { // bấm Nộp hai lần cùng lúc
            made[0] = again.get().submissionId();
            return false;
          }
          Optional<Sub> existing = repo.ofStudent(me.orgId(), a.id(), me.userId());
          if (existing.isPresent() && late != null) {
            throw new AppException(
                ErrorCode.CONFLICT, "Đã quá hạn nộp. Bài nộp trước của em được giữ để chấm.");
          }
          UUID subId = existing.map(Sub::id).orElseGet(Uuid7::next);
          int version = existing.map(s -> s.currentVersion() + 1).orElse(1);
          if (existing.isEmpty()) {
            repo.insertSubmission(
                me.orgId(),
                subId,
                a.id(),
                me.userId(),
                a.classId(),
                repo.nextCode(me.orgId(), a.id()),
                year);
          }
          UUID verId = Uuid7.next();
          repo.insertVersion(
              me.orgId(), verId, subId, me.userId(), version, key, files.size(), now, late, year);
          for (int i = 0; i < files.size(); i++) {
            store(me, a, verId, i + 1, files.get(i), year);
          }
          repo.setCurrentVersion(me.orgId(), subId, version);
          for (PageInput p : pages) {
            repo.deleteUpload(me.orgId(), UUID.fromString(p.uploadId()));
            if (p.derivedUploadId() != null && !p.derivedUploadId().isBlank()) {
              repo.deleteUpload(me.orgId(), UUID.fromString(p.derivedUploadId()));
            }
          }
          made[0] = subId;
          made[1] = verId;
          TransactionSynchronizationManager.registerSynchronization(
              new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                  ocr.schedule(me, subId, verId);
                }
              });
          return true;
        });
    return new Result(Boolean.TRUE.equals(created), view(me, made[0]));
  }

  private Bytes bytesOf(AuthPrincipal me, PageInput p, int i) {
    if (p == null) {
      throw AppException.invalidField("pages[" + i + "]", "Thiếu ảnh trang " + (i + 1) + ".");
    }
    Upload u = complete(me, uuid(p.uploadId(), "pages[" + i + "].uploadId"), i);
    byte[] original = repo.uploadBytes(me.orgId(), u.id());
    byte[] derived = null;
    if ("image/heic".equals(u.contentType())) {
      if (p.derivedUploadId() == null || p.derivedUploadId().isBlank()) {
        throw AppException.invalidField(
            "pages[" + i + "].derivedUploadId", "Ảnh HEIC cần kèm bản JPEG để đọc chữ.");
      }
      Upload d = complete(me, uuid(p.derivedUploadId(), "pages[" + i + "].derivedUploadId"), i);
      if (!"image/jpeg".equals(d.contentType())) {
        throw AppException.invalidField(
            "pages[" + i + "].derivedUploadId", "Bản phái sinh phải là JPEG.");
      }
      derived = repo.uploadBytes(me.orgId(), d.id());
    }
    return new Bytes(original, u.contentType(), derived);
  }

  private Upload complete(AuthPrincipal me, UUID id, int i) {
    Upload u =
        repo.upload(me.orgId(), me.userId(), id)
            .orElseThrow(() ->
                AppException.invalidField(
                    "pages[" + i + "].uploadId", "Ảnh trang " + (i + 1) + " chưa tải lên."));
    if (u.received() != u.sizeBytes()) {
      throw AppException.invalidField(
          "pages[" + i + "].uploadId", "Ảnh trang " + (i + 1) + " chưa tải xong.");
    }
    return u;
  }

  /** Lưu ảnh một trang của phiên bản: JPEG/PNG qua ImageStore; HEIC gốc + phái sinh JPEG. */
  private void store(AuthPrincipal me, Assignment a, UUID verId, int pageNo, Bytes f, String year) {
    if (f.derived() == null) {
      images.put(
          new ImageStore.PutRequest(
              me.orgId(), a.id(), verId, pageNo, me.userId(), f.original(), f.contentType()));
      return;
    }
    if (f.original().length < 12
        || f.original()[4] != 'f'
        || f.original()[5] != 't'
        || f.original()[6] != 'y'
        || f.original()[7] != 'p') {
      throw AppException.invalidField("pages", "Ảnh HEIC không hợp lệ.");
    }
    // T-261128-60: gỡ mục Exif/XMP của ảnh gốc HEIC trước khi lưu; băm tính sau khi gỡ
    // (BR-S5E4-27).
    byte[] original = HeicStripper.strip(f.original());
    UUID origId = Uuid7.next();
    String origKey = me.orgId() + "/" + origId;
    store.write(origKey, original);
    repo.insertImage(
        me.orgId(),
        origId,
        a.id(),
        verId,
        pageNo,
        "original",
        me.userId(),
        a.classId(),
        year,
        "image/heic",
        SubmitHashes.sha256(original),
        original.length,
        origKey);
    MetadataStripper.Clean clean = MetadataStripper.strip(f.derived());
    UUID derId = Uuid7.next();
    String derKey = me.orgId() + "/" + derId;
    store.write(derKey, clean.bytes());
    repo.insertImage(
        me.orgId(),
        derId,
        a.id(),
        verId,
        pageNo,
        "derived",
        me.userId(),
        a.classId(),
        year,
        clean.contentType(),
        SubmitHashes.sha256(clean.bytes()),
        clean.bytes().length,
        derKey);
  }

  // ------------------------------------------------------------------ xem

  /** Bài nộp của học sinh cho một bài có chấm (rỗng nếu chưa nộp). */
  public Map<String, Object> mine(UUID assignmentId) {
    AuthPrincipal me = student();
    Map<String, Object> m = new LinkedHashMap<>();
    Assignment a =
        repo.assignment(me.orgId(), assignmentId)
            .filter(x -> repo.activeMember(me.orgId(), x.classId(), me.userId()))
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    m.put("assignmentId", a.id().toString());
    m.put("title", a.title());
    m.put("dueAt", a.dueAt().toString());
    m.put("allowLate", a.allowLate());
    m.put("privacyAcked", repo.acked(me.orgId(), me.userId(), SubmitRules.PRIVACY_VERSION));
    repo.ofStudent(me.orgId(), a.id(), me.userId())
        .ifPresent(s -> m.put("submission", view(me, s.id())));
    return m;
  }

  /** Xem bài nộp: chính học sinh nộp hoặc giáo viên chủ lớp; ngoài quan hệ NOT_FOUND. */
  public Map<String, Object> get(UUID submissionId) {
    return view(RequestContext.principal(), submissionId);
  }

  private Map<String, Object> view(AuthPrincipal me, UUID submissionId) {
    Sub s =
        repo.submission(me.orgId(), submissionId)
            .filter(x -> related(me, x))
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    boolean teacher = me.role() == Role.TEACHER;
    List<Version> versions = repo.versions(me.orgId(), s.id());
    Version cur =
        versions.stream()
            .filter(v -> v.version() == s.currentVersion())
            .findFirst()
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", s.id().toString());
    m.put("code", s.code());
    m.put("assignmentId", s.assignmentId().toString());
    // T-261128-60: học sinh chỉ thấy "Đã nộp · Đang chờ thầy cô chấm" tới khi bài Đã trả
    // (BR-S5E4-21).
    String state = repo.gradingState(me.orgId(), s.id());
    boolean returned = "returned".equals(state);
    m.put("status", returned ? "returned" : "submitted");
    m.put("statusLabel", returned ? "Đã trả" : "Đã nộp");
    m.put(
        "message",
        returned
            ? "Thầy cô đã trả bài. Mở Kết quả để xem điểm và lời giải."
            : "Đã nộp lúc " + VN_TIME.format(cur.submittedAt()) + ". Đang chờ thầy cô chấm.");
    if (teacher) {
      m.put("studentId", s.studentId().toString());
      m.put("gradingStatus", state);
      m.put(
          "gradingStatusLabel",
          switch (state) {
            case "approved" -> "Đã duyệt";
            case "returned" -> "Đã trả";
            default -> "Chờ chấm";
          });
    }
    m.putAll(version(me, cur));
    if (teacher) {
      List<Map<String, Object>> vs = new ArrayList<>();
      for (Version v : versions) {
        vs.add(version(me, v));
      }
      m.put("versions", vs);
    }
    return m;
  }

  private Map<String, Object> version(AuthPrincipal me, Version v) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("version", v.version());
    m.put("versionId", v.id().toString());
    m.put("submittedAt", v.submittedAt().toString());
    m.put("late", v.lateSeconds() != null);
    String label = SubmitRules.lateLabel(v.lateSeconds());
    if (label != null) {
      m.put("lateLabel", label);
    }
    List<Map<String, Object>> pages = new ArrayList<>();
    for (Page p : repo.pages(me.orgId(), v.id())) {
      if (!"original".equals(p.kind())) {
        pages.stream()
            .filter(x -> x.get("pageNo").equals(p.pageNo()))
            .findFirst()
            .ifPresent(x -> x.put("derivedContentType", p.contentType()));
        continue;
      }
      Map<String, Object> x = new LinkedHashMap<>();
      x.put("pageNo", p.pageNo());
      x.put("contentType", p.contentType());
      x.put("sha256", p.sha256());
      x.put("bytes", p.bytes());
      pages.add(x);
    }
    m.put("pages", pages);
    return m;
  }

  private boolean related(AuthPrincipal me, Sub s) {
    if (me.role() == Role.STUDENT) {
      return s.studentId().equals(me.userId());
    }
    if (me.role() == Role.TEACHER) {
      return repo.classTeacher(me.orgId(), s.classId()).map(me.userId()::equals).orElse(false);
    }
    return false;
  }

  private static UUID uuid(String s, String field) {
    try {
      return UUID.fromString(s == null ? "" : s.strip());
    } catch (IllegalArgumentException e) {
      throw AppException.invalidField(field, "Mã không hợp lệ.");
    }
  }
}
